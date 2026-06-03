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
        if (random.nextInt(3) != 0) return;
        String school = getSchool();
        var particleType = SihriyaParticles.getForSchool(school);
        Vec3[] points = CircleShape.circlePoints(animation.getRadius(), 8, animation.getRotation());
        for (Vec3 p : points) {
            level().addParticle(particleType, false,
                getX() + p.x, getY() + 0.1, getZ() + p.z,
                0, 0, 0);
        }
        // Extra sparkles
        if (random.nextInt(5) == 0) {
            double sx = (random.nextDouble() - 0.5) * animation.getRadius() * 2;
            double sz = (random.nextDouble() - 0.5) * animation.getRadius() * 2;
            level().addParticle(particleType, false,
                getX() + sx, getY() + 0.3 + random.nextDouble(),
                getZ() + sz, 0, 0.01, 0);
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
