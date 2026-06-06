package tong.sihriya.client.particle;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.magiccircle.MagicCircleEntity;
import tong.sihriya.registry.SihriyaEntities;

import java.util.Random;

public class SpellParticleHelper {
    private static final Random RANDOM = new Random();

    public static void spawnCircleAround(LivingEntity caster, String schoolId, int duration) {
        if (!caster.level().isClientSide) return;
        MagicCircleEntity circle = new MagicCircleEntity(
            SihriyaEntities.MAGIC_CIRCLE.get(), caster.level(), schoolId, duration
        );
        circle.setPos(caster.getX(), caster.getY() + 0.5, caster.getZ());
        caster.level().addFreshEntity(circle);
    }

    public static void spawnCircleAt(Level level, Vec3 pos, String schoolId, int duration) {
        if (!level.isClientSide) return;
        MagicCircleEntity circle = new MagicCircleEntity(
            SihriyaEntities.MAGIC_CIRCLE.get(), level, schoolId, duration
        );
        circle.setPos(pos.x, pos.y, pos.z);
        level.addFreshEntity(circle);
    }

    public static void spawnGlowBurst(Level level, Vec3 pos, String schoolId, int count) {
        for (int i = 0; i < count; i++) {
            double dx = (RANDOM.nextDouble() - 0.5) * 2;
            double dy = RANDOM.nextDouble() * 1.5;
            double dz = (RANDOM.nextDouble() - 0.5) * 2;
            level.addParticle(ParticleTypes.END_ROD, true,
                pos.x + dx, pos.y + dy, pos.z + dz,
                0, 0.02, 0);
        }
    }
}
