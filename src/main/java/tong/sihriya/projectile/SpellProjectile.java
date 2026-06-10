package tong.sihriya.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.VFXTriggerPacket;
import tong.sihriya.network.NetworkHandler;

public class SpellProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<String> SCHOOL_ID =
        SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SPELL_ID =
        SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DAMAGE =
        SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> CHARGING =
        SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CHARGE_SCALE =
        SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.FLOAT);
    private static final int MAX_LIFETIME_TICKS = 800;

    private int chargeTicks = 0;
    private int initialChargeTicks = 0;
    private double chargeStartY = 4.0;
    private double chargeEndY = 8.0;
    private boolean positionLocked = false;
    private double lockedX;
    private double lockedY;
    private double lockedZ;

    // Crater multi-tick (blazing_sun)
    private boolean craterActive = false;
    private double craterX, craterY, craterZ;
    private int craterStep = -1;
    private static final int CRATER_RADIUS = 80;

    public SpellProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SpellProjectile(Level level, LivingEntity shooter, float damage, String schoolId, String spellId) {
        super(tong.sihriya.registry.SihriyaEntities.SPELL_PROJECTILE.get(), shooter, level);
        setDamage(damage);
        setSchoolId(schoolId);
        setSpellId(spellId);
    }

    public void startCharge(int ticks) {
        this.chargeTicks = ticks;
        this.initialChargeTicks = ticks;
        this.chargeStartY = 4.0;
        this.chargeEndY = 8.0;
        this.setNoGravity(true);
        this.entityData.set(CHARGING, true);
        this.entityData.set(CHARGE_SCALE, 1.0f);
        this.positionLocked = true;
        this.lockedX = this.getX();
        this.lockedY = this.getY();
        this.lockedZ = this.getZ();
    }

    public void startCharge(int ticks, double startY, double endY) {
        this.chargeTicks = ticks;
        this.initialChargeTicks = ticks;
        this.chargeStartY = startY;
        this.chargeEndY = endY;
        this.setNoGravity(true);
        this.entityData.set(CHARGING, true);
        this.entityData.set(CHARGE_SCALE, 1.0f);
        this.positionLocked = true;
        this.lockedX = this.getX();
        this.lockedY = this.getY();
        this.lockedZ = this.getZ();
    }

    public float getChargeProgress() {
        if (initialChargeTicks <= 0) return 1.0f;
        return Math.min(1.0f, (initialChargeTicks - chargeTicks) / (float) initialChargeTicks);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public float getChargeScale() {
        return this.entityData.get(CHARGE_SCALE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, Math.max(0.0f, damage));
    }

    public void setSchoolId(String schoolId) {
        this.entityData.set(SCHOOL_ID, schoolId == null ? "" : schoolId);
    }

    public void setSpellId(String spellId) {
        this.entityData.set(SPELL_ID, spellId == null ? "" : spellId);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public String getSpellId() {
        return this.entityData.get(SPELL_ID);
    }

    public String getSchoolId() {
        return this.entityData.get(SCHOOL_ID);
    }

    @Override
    public void tick() {
        if (chargeTicks > 0) {
            chargeTicks--;
            this.setNoGravity(true);
            float progress = getChargeProgress();
            this.entityData.set(CHARGE_SCALE, 1.0f + progress * 7.0f);
            // Hover at locked position (ne suit pas le joueur)
            float p = getChargeProgress();
            double yOff = chargeStartY + (chargeEndY - chargeStartY) * p;
            this.setPos(lockedX, lockedY + (yOff - chargeStartY), lockedZ);
            if (chargeTicks == 0) {
                this.entityData.set(CHARGING, false);
                this.setNoGravity(false);
                var look = this.getOwner() != null
                    ? this.getOwner().getLookAngle()
                    : new net.minecraft.world.phys.Vec3(0, 0, 1);
                this.shoot(look.x, look.y, look.z, 3.0f, 0.0f);
            }
            // Spawn particles during charge
            if (this.level().isClientSide) {
                for (int i = 0; i < 4; i++) {
                    var particleType = tong.sihriya.registry.SihriyaParticles.getForSchool(getSchoolId());
                    double rx = (random.nextDouble() - 0.5) * 1.5;
                    double ry = (random.nextDouble() - 0.5) * 1.5;
                    double rz = (random.nextDouble() - 0.5) * 1.5;
                    this.level().addParticle(particleType, true,
                        this.getX() + rx, this.getY() + ry, this.getZ() + rz,
                        0, 0.02, 0);
                }
            }
            return;
        }

        super.tick();

        // Cratère blazing_sun : traitement multi-tick (1 tranche X par tick)
        if (craterActive && !this.level().isClientSide) {
            tickCrater();
            return;
        }

        if (this.level().isClientSide && this.tickCount % 3 == 0) {
            var particleType = tong.sihriya.registry.SihriyaParticles.getForSchool(getSchoolId());
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(particleType, true,
                    this.getX() + (random.nextDouble() - 0.5) * 0.2,
                    this.getY() + (random.nextDouble() - 0.5) * 0.2,
                    this.getZ() + (random.nextDouble() - 0.5) * 0.2,
                    0, 0.01, 0);
            }
        }
        if (!this.level().isClientSide && this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            var hitPos = result.getLocation();
            float damage = getDamage();

            // Direct hit damage
            if (result.getType() == HitResult.Type.ENTITY) {
                var entityHit = (EntityHitResult) result;
                if (entityHit.getEntity() instanceof LivingEntity target) {
                    if (this.getOwner() instanceof LivingEntity owner) {
                        float finalDamage = damage;
                        if (target instanceof net.minecraft.world.entity.player.Player targetPlayer) {
                            float resistance = STATModIntegration.getMagicResistance(targetPlayer);
                            finalDamage = damage * (1 - resistance);
                        }
                        target.hurt(owner.damageSources().indirectMagic(this, owner), finalDamage);
                        if (appliesIgnitionOnImpact()) {
                            target.setRemainingFireTicks(100);
                        }
                    }
                }
            }

            // Explosion massive pour blazing_sun, grosse pour les autres
            if (damage >= 40) {
                String sid = getSpellId();
                boolean isBlazingSun = "fire.blazing_sun".equals(sid);
                float explosionPower = isBlazingSun ? 100.0f : Math.min(8.0f, damage / 8.0f);
                float aoeRadius = isBlazingSun ? explosionPower * 1.5f : explosionPower * 1.5f;
                boolean fire = "fire".equals(getSchoolId()) || "lava".equals(getSchoolId());

                if (isBlazingSun) {
                    // Cratère sphérique traité en multi-tick (voir tickCrater)
                    this.craterX = hitPos.x;
                    this.craterY = hitPos.y;
                    this.craterZ = hitPos.z;
                    this.craterStep = 0;
                    this.craterActive = true;
                    this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    this.setNoGravity(true);
                } else {
                    var explosion = new net.minecraft.world.level.Explosion(
                        this.level(), this, null, null,
                        hitPos.x, hitPos.y, hitPos.z,
                        explosionPower, fire,
                        net.minecraft.world.level.Explosion.BlockInteraction.DESTROY
                    );
                    net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this.level(), explosion);
                    explosion.explode();
                    explosion.finalizeExplosion(true);
                }

                // Apply burn to all nearby entities
                for (var entity : this.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    new net.minecraft.world.phys.AABB(hitPos, hitPos).inflate(aoeRadius),
                    e -> e.isAlive() && e != this.getOwner())) {

                    float dist = (float) entity.position().distanceTo(hitPos);
                    float falloff = 1.0f - Math.min(1.0f, dist / aoeRadius);
                    float aoeDamage = damage * 0.5f * falloff;
                    entity.hurt(this.damageSources().explosion(null), aoeDamage);

                    if (appliesIgnitionOnImpact()) {
                        entity.setRemainingFireTicks((int)(200 * falloff));
                    }
                }
            }

            var server = level().getServer();
            if (server != null) {
                int vfxEntityId = "fire.blazing_sun".equals(getSpellId()) ? -1 : getId();
                for (var p : server.getPlayerList().getPlayers()) {
                    ServerPlayer sp = (ServerPlayer) p;
                    NetworkHandler.sendToPlayer(new VFXTriggerPacket(
                        getSpellId(), getSchoolId(), vfxEntityId,
                        hitPos.x, hitPos.y, hitPos.z
                    ), sp);
                }
            }

            if (!this.craterActive) {
                this.discard();
            }
        }
    }

    private void tickCrater() {
        int r = CRATER_RADIUS;
        int rSq = r * r;
        int bx = net.minecraft.util.Mth.floor(craterX);
        int by = net.minecraft.util.Mth.floor(craterY);
        int bz = net.minecraft.util.Mth.floor(craterZ);
        var level = this.level();
        var bedrock = net.minecraft.world.level.block.Blocks.BEDROCK;
        var airState = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        var pos = new net.minecraft.core.BlockPos.MutableBlockPos();

        // Centre → extérieur : 0, +1, -1, +2, -2, ...
        int dx;
        if (craterStep == 0) {
            dx = 0;
        } else if (craterStep % 2 == 1) {
            dx = (craterStep / 2) + 1;
        } else {
            dx = -(craterStep / 2);
        }

        if (Math.abs(dx) > r) {
            craterActive = false;
            this.discard();
            return;
        }

        int dxSq = dx * dx;
        int x = bx + dx;
        for (int dz = -r; dz <= r; dz++) {
            int dzSq = dz * dz;
            int hSq = dxSq + dzSq;
            if (hSq > rSq) continue;
            int maxDy = (int) Math.sqrt(rSq - hSq);
            int z = bz + dz;
            for (int dy = -maxDy; dy <= maxDy; dy++) {
                pos.set(x, by + dy, z);
                var state = level.getBlockState(pos);
                if (state.isAir() || state.is(bedrock)) continue;
                level.setBlock(pos, airState, 1 | 2 | 4 | 16);
            }
        }

        craterStep++;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCHOOL_ID, "");
        this.entityData.define(SPELL_ID, "");
        this.entityData.define(DAMAGE, 10.0f);
        this.entityData.define(CHARGING, false);
        this.entityData.define(CHARGE_SCALE, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("SchoolId", getSchoolId());
        tag.putString("SpellId", getSpellId());
        tag.putFloat("Damage", getDamage());
        tag.putFloat("ChargeScale", getChargeScale());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSchoolId(tag.getString("SchoolId"));
        setSpellId(tag.getString("SpellId"));
        setDamage(tag.contains("Damage") ? tag.getFloat("Damage") : 10.0f);
        if (tag.contains("ChargeScale")) {
            this.entityData.set(CHARGE_SCALE, tag.getFloat("ChargeScale"));
        }
    }

    private boolean appliesIgnitionOnImpact() {
        String school = getSchoolId();
        if ("fire".equals(school) || "lava".equals(school)) {
            return true;
        }

        var spell = SpellRegistry.get(getSpellId());
        return spell != null && spell.effects.stream().anyMatch(effect -> "burn".equals(effect.type));
    }
}
