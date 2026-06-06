package tong.sihriya.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tong.sihriya.Sihriya;

public class SihriyaParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Sihriya.MODID);

    public static final RegistryObject<SimpleParticleType> FIRE_GLOW =
        PARTICLES.register("fire_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> WATER_GLOW =
        PARTICLES.register("water_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> WIND_GLOW =
        PARTICLES.register("wind_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> EARTH_GLOW =
        PARTICLES.register("earth_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LIGHTNING_GLOW =
        PARTICLES.register("lightning_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ICE_GLOW =
        PARTICLES.register("ice_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LAVA_GLOW =
        PARTICLES.register("lava_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> NECRO_GLOW =
        PARTICLES.register("necro_glow", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LUMI_GLOW =
        PARTICLES.register("lumi_glow", () -> new SimpleParticleType(false));

    public static SimpleParticleType getForSchool(String schoolId) {
        return switch (schoolId) {
            case "fire" -> FIRE_GLOW.get();
            case "water" -> WATER_GLOW.get();
            case "wind" -> WIND_GLOW.get();
            case "earth" -> EARTH_GLOW.get();
            case "lightning" -> LIGHTNING_GLOW.get();
            case "ice" -> ICE_GLOW.get();
            case "lava" -> LAVA_GLOW.get();
            case "necromancy" -> NECRO_GLOW.get();
            case "lumamancy" -> LUMI_GLOW.get();
            default -> FIRE_GLOW.get();
        };
    }
}
