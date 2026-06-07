package tong.sihriya.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SpellRegistry.SpellType;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SihriyaIronsSpellRegister {
    static final DeferredRegister<AbstractSpell> SPELL_REGISTER =
        DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, Sihriya.MODID);

    static {
        loadAndRegister();
    }

    private static void loadAndRegister() {
        try (InputStream is = SihriyaIronsSpellRegister.class
                .getResourceAsStream("/data/sihriya/spells.json")) {
            if (is == null) {
                Sihriya.LOGGER.warn("SihriyaIronsSpellRegister: spells.json not found, using minimal defaults");
                registerFallback();
                return;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            int count = 0;
            for (var elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();
                String school = obj.get("school").getAsString();
                int tier = obj.get("tier").getAsInt();
                int manaCost = obj.get("manaCost").getAsInt();
                int cooldown = obj.get("cooldown").getAsInt();
                int castTime = obj.has("castTime") ? obj.get("castTime").getAsInt() : 0;
                SpellType spellType;
                try {
                    spellType = SpellType.valueOf(obj.get("type").getAsString().toUpperCase());
                } catch (Exception e) {
                    spellType = SpellType.PROJECTILE;
                }
                final String fId = id, fSchool = school;
                final int fTier = tier, fMana = manaCost, fCooldown = cooldown;
                final int fCastTime = castTime;
                final SpellType fType = spellType;
                SPELL_REGISTER.register(id, () ->
                    new SihriyaIronsSpell(fId, fSchool, fTier, fMana, fCooldown, fCastTime, fType));
                count++;
            }
            Sihriya.LOGGER.info("SihriyaIronsSpellRegister: queued {} spells for IS registry", count);
        } catch (Exception e) {
            Sihriya.LOGGER.error("SihriyaIronsSpellRegister: failed to read spells.json", e);
            registerFallback();
        }
    }

    private static void registerFallback() {
        SPELL_REGISTER.register("fireball", () ->
            new SihriyaIronsSpell("fireball", "fire", 1, 15, 30, 0, SpellType.PROJECTILE));
        SPELL_REGISTER.register("water_bolt", () ->
            new SihriyaIronsSpell("water_bolt", "water", 1, 12, 25, 0, SpellType.PROJECTILE));
        SPELL_REGISTER.register("wind_gust", () ->
            new SihriyaIronsSpell("wind_gust", "wind", 1, 10, 20, 0, SpellType.PROJECTILE));
    }

    public static void register(IEventBus bus) {
        SPELL_REGISTER.register(bus);
    }
}
