package tong.sihriya.client.particle.magiccircle;

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
import tong.sihriya.registry.SihriyaParticles;

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
        String school = getSchool();
        var particleType = SihriyaParticles.getForSchool(school);

        // Ring particles — always, more at peak
        int ringCount = animation.getAlpha() > 0.8f ? 14 : 8;
        Vec3[] points = CircleShape.circlePoints(animation.getRadius(), ringCount, animation.getRotationRunes());
        for (Vec3 p : points) {
            level().addParticle(particleType, false,
                getX() + p.x, getY() + 0.15, getZ() + p.z,
                0, 0.015, 0);
        }

        // Orbiting inner particles — rotating faster
        Vec3[] innerPoints = CircleShape.circlePoints(
            animation.getRadius() * 0.6f, 6, animation.getRotationGeometry());
        for (Vec3 p : innerPoints) {
            level().addParticle(particleType, false,
                getX() + p.x, getY() + 0.3, getZ() + p.z,
                0, 0.025, 0);
        }

        // Random inner sparkles
        int sparkleCount = 2 + random.nextInt(4);
        for (int i = 0; i < sparkleCount; i++) {
            double sx = (random.nextDouble() - 0.5) * animation.getRadius() * 2;
            double sz = (random.nextDouble() - 0.5) * animation.getRadius() * 2;
            level().addParticle(particleType, false,
                getX() + sx, getY() + 0.2 + random.nextDouble() * 0.6,
                getZ() + sz, 0, 0.02, 0);
        }

        // Ascending glow motes
        if (random.nextFloat() < 0.4f) {
            double ax = (random.nextDouble() - 0.5) * animation.getRadius() * 1.5;
            double az = (random.nextDouble() - 0.5) * animation.getRadius() * 1.5;
            level().addParticle(particleType, false,
                getX() + ax, getY() + 0.1, getZ() + az,
                0, 0.04 + random.nextDouble() * 0.04, 0);
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
