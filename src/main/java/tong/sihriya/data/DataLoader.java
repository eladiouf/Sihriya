package tong.sihriya.data;

import com.google.gson.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolRegistry.*;
import tong.sihriya.data.SpellRegistry.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DataLoader extends SimplePreparableReloadListener<JsonObject> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadAll() {
        loadSchools();
        loadSpells();
        Sihriya.LOGGER.info("Loaded {} schools and {} spells", SchoolRegistry.size(), SpellRegistry.size());
    }

    private static void loadSchools() {
        try (InputStream is = DataLoader.class.getResourceAsStream("/data/sihriya/schools.json")) {
            if (is == null) {
                Sihriya.LOGGER.warn("schools.json not found, using defaults");
                registerDefaultSchools();
                return;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (var elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();
                String name = obj.get("name").getAsString();
                boolean starting = obj.get("starting").getAsBoolean();
                String color = obj.get("color").getAsString();
                UnlockCondition unlock = null;
                if (obj.has("unlock") && !obj.get("unlock").isJsonNull()) {
                    JsonObject u = obj.get("unlock").getAsJsonObject();
                    String type = u.get("type").getAsString();
                    JsonArray ids = u.get("schoolIds").getAsJsonArray();
                    JsonArray lvls = u.get("levels").getAsJsonArray();
                    String[] sIds = new String[ids.size()];
                    int[] lvlsArr = new int[lvls.size()];
                    for (int i = 0; i < ids.size(); i++) sIds[i] = ids.get(i).getAsString();
                    for (int i = 0; i < lvls.size(); i++) lvlsArr[i] = lvls.get(i).getAsInt();
                    unlock = new UnlockCondition(type, sIds, lvlsArr);
                }
                SchoolRegistry.register(new SchoolData(id, name, starting, color, unlock));
            }
        } catch (Exception e) {
            Sihriya.LOGGER.error("Failed to load schools.json", e);
            registerDefaultSchools();
        }
    }

    private static void loadSpells() {
        try (InputStream is = DataLoader.class.getResourceAsStream("/data/sihriya/spells.json")) {
            if (is == null) {
                Sihriya.LOGGER.warn("spells.json not found, using defaults");
                registerDefaultSpells();
                return;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (var elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();
                String school = obj.get("school").getAsString();
                int tier = obj.get("tier").getAsInt();
                int manaCost = obj.get("manaCost").getAsInt();
                int cooldown = obj.get("cooldown").getAsInt();
                SpellType type = SpellType.valueOf(obj.get("type").getAsString().toUpperCase());
                List<SpellEffect> effects = new ArrayList<>();
                JsonArray effArr = obj.getAsJsonArray("effects");
                if (effArr != null) {
                    for (var e : effArr) {
                        JsonObject eo = e.getAsJsonObject();
                        effects.add(new SpellEffect(
                            eo.get("type").getAsString(),
                            eo.get("baseValue").getAsFloat(),
                            eo.get("scaling").getAsFloat(),
                            eo.get("duration").getAsInt()
                        ));
                    }
                }
                SpellRegistry.register(new SpellData(id, school, tier, manaCost, cooldown, type, effects));
            }
        } catch (Exception e) {
            Sihriya.LOGGER.error("Failed to load spells.json", e);
            registerDefaultSpells();
        }
    }

    private static void registerDefaultSchools() {
        SchoolRegistry.register(new SchoolData("fire", "Feu", true, "FF4500", null));
        SchoolRegistry.register(new SchoolData("water", "Eau", true, "3399FF", null));
        SchoolRegistry.register(new SchoolData("wind", "Vent", true, "90EE90", null));
        SchoolRegistry.register(new SchoolData("earth", "Terre", true, "8B4513", null));
        SchoolRegistry.register(new SchoolData("lightning", "Foudre", false, "FFD700",
            new UnlockCondition("or", new String[]{"fire","wind"}, new int[]{50,50})));
        SchoolRegistry.register(new SchoolData("ice", "Glace", false, "ADD8E6",
            new UnlockCondition("level", new String[]{"water"}, new int[]{50})));
    }

    private static void registerDefaultSpells() {
        registerDefaultSchoolSpells("fire", new String[][]{
            {"fireball", "1", "15", "30", "PROJECTILE", "damage:6:0.1:0", "burn:2:0.05:100"}
        });
        registerDefaultSchoolSpells("water", new String[][]{
            {"water_bolt", "1", "12", "25", "PROJECTILE", "damage:4:0.08:0", "slow:0:0:60"}
        });
        registerDefaultSchoolSpells("wind", new String[][]{
            {"wind_gust", "1", "10", "20", "PROJECTILE", "damage:3:0.05:0", "knockback:2:0.02:0"}
        });
        registerDefaultSchoolSpells("earth", new String[][]{
            {"earth_spike", "1", "14", "35", "PROJECTILE", "damage:8:0.12:0", "stun:0:0:40"}
        });
        registerDefaultSchoolSpells("lightning", new String[][]{
            {"lightning_bolt", "1", "20", "40", "PROJECTILE", "damage:10:0.15:0", "chain:3:0.05:0"}
        });
        registerDefaultSchoolSpells("ice", new String[][]{
            {"ice_shard", "1", "16", "30", "PROJECTILE", "damage:5:0.1:0", "freeze:0:0:80"}
        });
    }

    private static void registerDefaultSchoolSpells(String school, String[][] spellDefs) {
        for (String[] def : spellDefs) {
            String id = def[0];
            int tier = Integer.parseInt(def[1]);
            int manaCost = Integer.parseInt(def[2]);
            int cooldown = Integer.parseInt(def[3]);
            SpellType type = SpellType.valueOf(def[4]);
            List<SpellEffect> effects = new ArrayList<>();
            for (int i = 5; i < def.length; i++) {
                String[] parts = def[i].split(":");
                effects.add(new SpellEffect(parts[0], Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]), Integer.parseInt(parts[3])));
            }
            SpellRegistry.register(new SpellData(id, school, tier, manaCost, cooldown, type, effects));
        }
    }

    @Override
    protected JsonObject prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return new JsonObject();
    }

    @Override
    protected void apply(JsonObject json, ResourceManager resourceManager, ProfilerFiller profiler) {}
}
