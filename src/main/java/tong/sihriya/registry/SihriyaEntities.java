package tong.sihriya.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tong.sihriya.Sihriya;
import tong.sihriya.client.particle.magiccircle.MagicCircleEntity;

public class SihriyaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Sihriya.MODID);

    public static final RegistryObject<EntityType<MagicCircleEntity>> MAGIC_CIRCLE =
        ENTITIES.register("magic_circle", () ->
            EntityType.Builder.<MagicCircleEntity>of(MagicCircleEntity::new, MobCategory.MISC)
                .sized(6.0f, 0.1f)
                .noSummon()
                .build(Sihriya.MODID + ":magic_circle")
        );
}
