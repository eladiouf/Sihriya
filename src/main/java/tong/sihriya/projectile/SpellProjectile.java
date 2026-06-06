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
    private static final int MAX_LIFETIME_TICKS = 120;

    public SpellProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SpellProjectile(Level level, LivingEntity shooter, float damage, String schoolId, String spellId) {
        super(tong.sihriya.registry.SihriyaEntities.SPELL_PROJECTILE.get(), shooter, level);
        setDamage(damage);
        setSchoolId(schoolId);
        setSpellId(spellId);
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
        super.tick();
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

            if (result.getType() == HitResult.Type.ENTITY) {
                var entityHit = (EntityHitResult) result;
                if (entityHit.getEntity() instanceof LivingEntity target) {
                    if (this.getOwner() instanceof LivingEntity owner) {
                        float finalDamage = getDamage();
                        if (target instanceof net.minecraft.world.entity.player.Player targetPlayer) {
                            float resistance = STATModIntegration.getMagicResistance(targetPlayer);
                            finalDamage = getDamage() * (1 - resistance);
                        }
                        target.hurt(owner.damageSources().indirectMagic(this, owner), finalDamage);
                        if (appliesIgnitionOnImpact()) {
                            target.setRemainingFireTicks(100);
                        }
                    }
                }
            }

            var server = level().getServer();
            if (server != null) {
                for (var p : server.getPlayerList().getPlayers()) {
                    ServerPlayer sp = (ServerPlayer) p;
                    NetworkHandler.sendToPlayer(new VFXTriggerPacket(
                        getSpellId(), getSchoolId(), getId(),
                        hitPos.x, hitPos.y, hitPos.z
                    ), sp);
                }
            }

            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCHOOL_ID, "");
        this.entityData.define(SPELL_ID, "");
        this.entityData.define(DAMAGE, 10.0f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("SchoolId", getSchoolId());
        tag.putString("SpellId", getSpellId());
        tag.putFloat("Damage", getDamage());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSchoolId(tag.getString("SchoolId"));
        setSpellId(tag.getString("SpellId"));
        setDamage(tag.contains("Damage") ? tag.getFloat("Damage") : 10.0f);
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
