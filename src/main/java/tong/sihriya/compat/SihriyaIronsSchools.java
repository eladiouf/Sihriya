package tong.sihriya.compat;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolColors;

import java.util.function.Supplier;

public class SihriyaIronsSchools {
    public static final DeferredRegister<SchoolType> SCHOOL_REGISTER =
        DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, Sihriya.MODID);

    public static final RegistryObject<SchoolType> FIRE = SCHOOL_REGISTER.register("fire", () ->
        school("fire", ISSDamageTypes.FIRE_MAGIC,
            AttributeRegistry.FIRE_SPELL_POWER, AttributeRegistry.FIRE_MAGIC_RESIST,
            SoundRegistry.FIREBALL_START));

    public static final RegistryObject<SchoolType> WATER = SCHOOL_REGISTER.register("water", () ->
        school("water", ISSDamageTypes.ICE_MAGIC,
            AttributeRegistry.ICE_SPELL_POWER, AttributeRegistry.ICE_MAGIC_RESIST,
            SoundRegistry.RAY_OF_FROST));

    public static final RegistryObject<SchoolType> WIND = SCHOOL_REGISTER.register("wind", () ->
        school("wind", ISSDamageTypes.EVOCATION_MAGIC,
            AttributeRegistry.EVOCATION_SPELL_POWER, AttributeRegistry.EVOCATION_MAGIC_RESIST,
            SoundRegistry.GUST_CAST));

    public static final RegistryObject<SchoolType> EARTH = SCHOOL_REGISTER.register("earth", () ->
        school("earth", ISSDamageTypes.NATURE_MAGIC,
            AttributeRegistry.NATURE_SPELL_POWER, AttributeRegistry.NATURE_MAGIC_RESIST,
            SoundRegistry.EARTHQUAKE_CAST));

    public static final RegistryObject<SchoolType> LIGHTNING = SCHOOL_REGISTER.register("lightning", () ->
        school("lightning", ISSDamageTypes.LIGHTNING_MAGIC,
            AttributeRegistry.LIGHTNING_SPELL_POWER, AttributeRegistry.LIGHTNING_MAGIC_RESIST,
            SoundRegistry.LIGHTNING_LANCE_CAST));

    public static final RegistryObject<SchoolType> ICE = SCHOOL_REGISTER.register("ice", () ->
        school("ice", ISSDamageTypes.ICE_MAGIC,
            AttributeRegistry.ICE_SPELL_POWER, AttributeRegistry.ICE_MAGIC_RESIST,
            SoundRegistry.ICE_BLOCK_CAST));

    public static final RegistryObject<SchoolType> LAVA = SCHOOL_REGISTER.register("lava", () ->
        school("lava", ISSDamageTypes.FIRE_MAGIC,
            AttributeRegistry.FIRE_SPELL_POWER, AttributeRegistry.FIRE_MAGIC_RESIST,
            SoundRegistry.FIRE_BREATH_LOOP));

    public static final RegistryObject<SchoolType> NECROMANCY = SCHOOL_REGISTER.register("necromancy", () ->
        school("necromancy", ISSDamageTypes.BLOOD_MAGIC,
            AttributeRegistry.BLOOD_SPELL_POWER, AttributeRegistry.BLOOD_MAGIC_RESIST,
            SoundRegistry.RAISE_DEAD_START));

    public static final RegistryObject<SchoolType> LUMAMANCY = SCHOOL_REGISTER.register("lumamancy", () ->
        school("lumamancy", ISSDamageTypes.HOLY_MAGIC,
            AttributeRegistry.HOLY_SPELL_POWER, AttributeRegistry.HOLY_MAGIC_RESIST,
            SoundRegistry.DIVINE_SMITE_CAST));

    private static SchoolType school(String id,
                                     ResourceKey<DamageType> damage,
                                     Supplier<Attribute> power,
                                     Supplier<Attribute> resist,
                                     RegistryObject<?> sound) {
        float[] rgb = SchoolColors.get(id);
        int color = ((int)(rgb[0] * 255) << 16) | ((int)(rgb[1] * 255) << 8) | (int)(rgb[2] * 255);
        Component displayName = Component.translatable("school." + Sihriya.MODID + "." + id)
            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
        return new SchoolType(
            new ResourceLocation(Sihriya.MODID, id),
            focusTag(id),
            displayName,
            power,
            resist,
            () -> (net.minecraft.sounds.SoundEvent) sound.get(),
            damage
        );
    }

    private static TagKey<Item> focusTag(String school) {
        return ItemTags.create(new ResourceLocation(Sihriya.MODID, "focus/" + school));
    }

    public static void register(IEventBus bus) {
        SCHOOL_REGISTER.register(bus);
    }
}
