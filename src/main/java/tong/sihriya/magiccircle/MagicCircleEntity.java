package tong.sihriya.magiccircle;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class MagicCircleEntity extends Entity {
    private static final EntityDataAccessor<String> SCHOOL =
        SynchedEntityData.defineId(MagicCircleEntity.class, EntityDataSerializers.STRING);

    private MagicCircleAnimation animation;
    private int lifetime;

    public MagicCircleEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.lifetime = 40;
        this.animation = new MagicCircleAnimation(lifetime);
        this.noCulling = true;
    }

    public MagicCircleEntity(EntityType<?> type, Level level, String schoolId, int duration) {
        this(type, level);
        setSchool(schoolId);
        this.lifetime = duration;
        this.animation = new MagicCircleAnimation(duration);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SCHOOL, "fire");
    }

    @Override
    public void tick() {
        super.tick();
        animation.tick(tickCount);
        if (level().isClientSide) {
            emitGlowParticles();
        }
        if (tickCount >= lifetime) {
            remove(RemovalReason.DISCARDED);
        }
    }

    private void emitGlowParticles() {
        double r = animation.getRadius();

        // Outer ring — defines the circle perimeter
        int outerCount = animation.getAlpha() > 0.8f ? 22 : 14;
        Vec3[] outer = CircleShape.circlePoints(r, outerCount, animation.getRotationRunes());
        for (Vec3 p : outer) {
            level().addParticle(ParticleTypes.END_ROD, true,
                getX() + p.x, getY() + 0.1, getZ() + p.z,
                0, 0.01, 0);
        }

        // Inner counter-rotating ring
        Vec3[] inner = CircleShape.circlePoints(r * 0.55, 10, animation.getRotationGeometry());
        for (Vec3 p : inner) {
            level().addParticle(ParticleTypes.END_ROD, true,
                getX() + p.x, getY() + 0.2, getZ() + p.z,
                0, 0.018, 0);
        }

        // Rising sparkles inside the circle
        int sparkleCount = 3 + random.nextInt(4);
        for (int i = 0; i < sparkleCount; i++) {
            double sx = (random.nextDouble() - 0.5) * r * 1.4;
            double sz = (random.nextDouble() - 0.5) * r * 1.4;
            level().addParticle(ParticleTypes.END_ROD, true,
                getX() + sx, getY() + 0.1 + random.nextDouble() * 0.3,
                getZ() + sz,
                0, 0.03 + random.nextDouble() * 0.04, 0);
        }
    }

    public String getSchool() { return entityData.get(SCHOOL); }
    public void setSchool(String id) { entityData.set(SCHOOL, id); }
    public MagicCircleAnimation getAnimation() { return animation; }
    public int getLifetime() { return lifetime; }
    public void setLifetime(int t) { this.lifetime = t; this.animation = new MagicCircleAnimation(t); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("school")) setSchool(tag.getString("school"));
        if (tag.contains("lifetime")) setLifetime(tag.getInt("lifetime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putString("school", getSchool());
        tag.putInt("lifetime", lifetime);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return (Packet<ClientGamePacketListener>) NetworkHooks.getEntitySpawningPacket(this);
    }
}
