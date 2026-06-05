package tong.sihriya.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tong.sihriya.Sihriya;
import tong.sihriya.magiccircle.MagicCircleEntity;
import tong.sihriya.projectile.SpellProjectile;

public class SihriyaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Sihriya.MODID);

    public static final RegistryObject<EntityType<MagicCircleEntity>> MAGIC_CIRCLE =
        ENTITIES.register("magic_circle", () ->
            EntityType.Builder.<MagicCircleEntity>of(MagicCircleEntity::new, MobCategory.MISC)
                .sized(8.0f, 0.5f)
                .noSummon()
                .build(Sihriya.MODID + ":magic_circle")
        );

    public static final RegistryObject<EntityType<SpellProjectile>> SPELL_PROJECTILE =
        ENTITIES.register("spell_projectile", () ->
            EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(64)
                .updateInterval(2)
                .build(Sihriya.MODID + ":spell_projectile")
        );
}
