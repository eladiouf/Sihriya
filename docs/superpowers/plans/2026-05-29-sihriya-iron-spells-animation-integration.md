# Iron's Spells + Animation Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter le système d'animations Epic Fight (inspiré EFISCompat) et le bridge optionnel Iron's Spells à Sihriya, en superposition du système natif existant.

**Architecture:** 3 couches — système natif (1-6, mana STAT Mod, particules glow) inchangé, animations Epic Fight via AnimationRegistryEvent + playAnimationSynchronized (inspiré EFISCompat patterns), compat Iron's Spells optionnelle via détection runtime et events Iron's.

**Tech Stack:** Forge 1.20.1, Epic Fight API (AnimationRegistryEvent, StaticAnimation, ActionAnimation, ServerPlayerPatch), Iron's Spells API (SchoolType, SpellRegistry, SpellOnCastEvent, SpellPreCastEvent), CurseMaven.

---

## File Structure

### New files:
- `src/main/java/tong/sihriya/animation/SihriyaAnimations.java` — registration des animations Epic Fight (inspiré EFISCompat Animation.java)
- `src/main/java/tong/sihriya/animation/SihriyaAnimationPlayer.java` — joue animation via ServerPlayerPatch.playAnimationSynchronized()
- `src/main/java/tong/sihriya/animation/SpellAnimationLoader.java` — charge JSON mapping spell → animation
- `src/main/java/tong/sihriya/compat/IronsSpellsCompat.java` — détection runtime, registration écoles/sorts, event hooks
- `src/main/resources/data/sihriya/sihriya_spell_animations/defaults.json` — mapping par défaut

### Modified files:
- `build.gradle` — ajout dépendance Iron's Spells CurseMaven
- `src/main/resources/META-INF/mods.toml` — Iron's Spells en optional
- `src/main/java/tong/sihriya/Sihriya.java` — init IronsSpellsCompat
- `src/main/java/tong/sihriya/core/SpellCastHandler.java` — appels animations
- `src/main/java/tong/sihriya/integration/EpicFightIntegration.java` — intégration animation player

---

### Task 1: Ajouter Iron's Spells dans build.gradle + mods.toml

**Files:**
- Modify: `build.gradle:52-67`
- Modify: `src/main/resources/META-INF/mods.toml`

- [ ] **Step 1: Ajouter la dépendance CurseMaven dans build.gradle**

Ajouter après la ligne `implementation project(':STAT_MOD')` :

```groovy
    // Iron's Spells 'n Spellbooks (optional — compatibilité grimoires + API)
    compileOnly fg.deobf("curse.maven:irons-spells-n-spellbooks-855414:7907340")
    runtimeOnly fg.deobf("curse.maven:irons-spells-n-spellbooks-855414:7907340")
```

- [ ] **Step 2: Ajouter Iron's Spells en dépendance optionnelle dans mods.toml**

Ajouter dans `[[dependencies.sihriya]]` :

```toml
[[dependencies.sihriya]]
    modId="irons_spellbooks"
    mandatory=false
    versionRange="[3.15,)"
    ordering="NONE"
    side="BOTH"
```

- [ ] **Step 3: Commit**

```bash
git add build.gradle src/main/resources/META-INF/mods.toml
git commit -m "build: add Iron's Spells as optional dependency via CurseMaven"
```

---

### Task 2: Créer SihriyaAnimations.java (Registration Epic Fight)

**Files:**
- Create: `src/main/java/tong/sihriya/animation/SihriyaAnimations.java`

- [ ] **Step 1: Créer la classe avec les champs d'AnimationAccessor**

```java
package tong.sihriya.animation;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import tong.sihriya.Sihriya;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.gameasset.Armatures;

import static yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL;
import static yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME;

public class SihriyaAnimations {
    // Chanting (incantation)
    public static AnimationAccessor<StaticAnimation> CHANT_ONE_HAND;
    public static AnimationAccessor<StaticAnimation> CHANT_TWO_HAND;
    public static AnimationAccessor<StaticAnimation> CHANT_STAFF;

    // Casting (lancer)
    public static AnimationAccessor<StaticAnimation> CAST_ONE_HAND;
    public static AnimationAccessor<StaticAnimation> CAST_TWO_HAND;
    public static AnimationAccessor<StaticAnimation> CAST_STAFF;

    // Continuous
    public static AnimationAccessor<StaticAnimation> CONTINUOUS_TWO_HAND;

    // Spéciaux (flying, ascension)
    public static AnimationAccessor<ActionAnimation> CAST_FLYING;
    public static AnimationAccessor<ActionAnimation> CAST_ASCENSION;

    @SubscribeEvent
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(Sihriya.MODID, SihriyaAnimations::build);
    }

    public static void build(AnimationManager.AnimationBuilder builder) {
        CHANT_ONE_HAND = builder.nextAccessor("biped/living/sihriya_chant_one_hand",
            a -> new StaticAnimation(true, a, Armatures.BIPED));
        CHANT_TWO_HAND = builder.nextAccessor("biped/living/sihriya_chant_two_hand",
            a -> new StaticAnimation(true, a, Armatures.BIPED));
        CHANT_STAFF = builder.nextAccessor("biped/living/sihriya_chant_staff",
            a -> new StaticAnimation(true, a, Armatures.BIPED));

        CAST_ONE_HAND = builder.nextAccessor("biped/living/sihriya_cast_one_hand",
            a -> new StaticAnimation(false, a, Armatures.BIPED));
        CAST_TWO_HAND = builder.nextAccessor("biped/living/sihriya_cast_two_hand",
            a -> new StaticAnimation(false, a, Armatures.BIPED));
        CAST_STAFF = builder.nextAccessor("biped/living/sihriya_cast_staff",
            a -> new StaticAnimation(false, a, Armatures.BIPED));

        CONTINUOUS_TWO_HAND = builder.nextAccessor("biped/living/sihriya_continuous_two_hand",
            a -> new StaticAnimation(true, a, Armatures.BIPED));

        CAST_FLYING = builder.nextAccessor("biped/living/sihriya_cast_flying",
            a -> new ActionAnimation(0F, a, Armatures.BIPED)
                .addProperty(MOVE_VERTICAL, true)
                .addProperty(NO_GRAVITY_TIME, TimePairList.create(0F, Float.MAX_VALUE)));

        CAST_ASCENSION = builder.nextAccessor("biped/living/sihriya_cast_ascension",
            a -> new ActionAnimation(0F, a, Armatures.BIPED)
                .addProperty(MOVE_VERTICAL, true)
                .addProperty(NO_GRAVITY_TIME, TimePairList.create(0F, 5F)));
    }

    public static AnimationAccessor<?> getByName(String name) {
        return switch (name) {
            case "CHANT_ONE_HAND" -> CHANT_ONE_HAND;
            case "CHANT_TWO_HAND" -> CHANT_TWO_HAND;
            case "CHANT_STAFF" -> CHANT_STAFF;
            case "CAST_ONE_HAND" -> CAST_ONE_HAND;
            case "CAST_TWO_HAND" -> CAST_TWO_HAND;
            case "CAST_STAFF" -> CAST_STAFF;
            case "CONTINUOUS_TWO_HAND" -> CONTINUOUS_TWO_HAND;
            case "CAST_FLYING" -> CAST_FLYING;
            case "CAST_ASCENSION" -> CAST_ASCENSION;
            default -> null;
        };
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/animation/SihriyaAnimations.java
git commit -m "feat: Epic Fight animation registry for spell casting (chant/cast/special)"
```

---

### Task 3: Créer SihriyaAnimationPlayer.java

**Files:**
- Create: `src/main/java/tong/sihriya/animation/SihriyaAnimationPlayer.java`

- [ ] **Step 1: Créer la classe avec SpellPhase enum + play()**

```java
package tong.sihriya.animation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tong.sihriya.Sihriya;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SihriyaAnimationPlayer {
    public enum SpellPhase {
        CHANT,
        CAST,
        CONTINUOUS
    }

    public static void play(ServerPlayer player, String spellId, SpellPhase phase) {
        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch == null) return;

        String animName = resolveAnimation(spellId, phase);
        if (animName == null) return;

        var accessor = SihriyaAnimations.getByName(animName);
        if (accessor == null) return;

        Sihriya.LOGGER.debug("Playing animation {} for spell {} phase {}", animName, spellId, phase);
        if (accessor.get() instanceof StaticAnimation anim) {
            if (phase == SpellPhase.CHANT) {
                patch.playAnimationSynchronized(anim, 0F);
            } else {
                patch.playAnimationSynchronized(anim, 0);
            }
        }
    }

    private static String resolveAnimation(String spellId, SpellPhase phase) {
        // D'abord chercher dans le SpellAnimationLoader (mapping JSON)
        String mapped = SpellAnimationLoader.getAnimation(spellId, phase);
        if (mapped != null) return mapped;

        // Fallback : basé sur le type de sort
        var spell = tong.sihriya.data.SpellRegistry.get(spellId);
        if (spell == null) return null;

        return switch (phase) {
            case CHANT -> "CHANT_ONE_HAND";
            case CAST -> "CAST_ONE_HAND";
            case CONTINUOUS -> "CONTINUOUS_TWO_HAND";
        };
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/animation/SihriyaAnimationPlayer.java
git commit -m "feat: animation player with phase-aware resolution and JSON fallback"
```

---

### Task 4: Créer SpellAnimationLoader.java + JSON mapping

**Files:**
- Create: `src/main/java/tong/sihriya/animation/SpellAnimationLoader.java`

- [ ] **Step 1: Créer le loader inspiré de EFISCompat SpellAnimationLoader**

```java
package tong.sihriya.animation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import tong.sihriya.Sihriya;

import java.util.HashMap;
import java.util.Map;

public class SpellAnimationLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<String, SpellAnimSet> REGISTRY = new HashMap<>();

    public SpellAnimationLoader() {
        super(GSON, "sihriya_spell_animations");
    }

    public record SpellAnimSet(
        String chant,
        String cast,
        String continuous,
        String staffChant,
        String staffCast
    ) {
        public static SpellAnimSet EMPTY = new SpellAnimSet(null, null, null, null, null);

        public boolean isEmpty() {
            return chant == null && cast == null && continuous == null
                && staffChant == null && staffCast == null;
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList,
                         ResourceManager resourceManager, ProfilerFiller profiler) {
        REGISTRY.clear();

        resourceList.forEach((location, json) -> {
            JsonObject root = json.getAsJsonObject();
            if (!root.has("spells")) return;

            JsonObject spells = root.getAsJsonObject("spells");
            spells.entrySet().forEach(entry -> {
                String spellName = entry.getKey();
                JsonObject data = entry.getValue().getAsJsonObject();
                try {
                    SpellAnimSet set = new SpellAnimSet(
                        getStringOrNull(data, "chant_animation"),
                        getStringOrNull(data, "cast_animation"),
                        getStringOrNull(data, "continuous_animation"),
                        getStringOrNull(data, "staff_chant"),
                        getStringOrNull(data, "staff_cast")
                    );
                    REGISTRY.put(spellName, set);
                } catch (Exception e) {
                    Sihriya.LOGGER.warn("Failed to load anim for spell '{}': {}", spellName, e.getMessage());
                }
            });
        });
    }

    public static String getAnimation(String spellId, SihriyaAnimationPlayer.SpellPhase phase) {
        SpellAnimSet set = REGISTRY.get(spellId);
        if (set == null || set.isEmpty()) {
            set = REGISTRY.get("default");
        }
        if (set == null || set.isEmpty()) return null;

        return switch (phase) {
            case CHANT -> set.chant();
            case CAST -> set.cast();
            case CONTINUOUS -> set.continuous();
        };
    }

    private static String getStringOrNull(JsonObject obj, String key) {
        if (!obj.has(key)) return null;
        String val = obj.get(key).getAsString();
        return val.isEmpty() ? null : val;
    }
}
```

- [ ] **Step 2: Créer le JSON de mapping par défaut**

Fichier : `src/main/resources/data/sihriya/sihriya_spell_animations/defaults.json`

```json
{
  "spells": {
    "default": {
      "chant_animation": "CHANT_ONE_HAND",
      "cast_animation": "CAST_ONE_HAND",
      "continuous_animation": "CONTINUOUS_TWO_HAND",
      "staff_chant": "CHANT_STAFF",
      "staff_cast": "CAST_STAFF"
    }
  }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/tong/sihriya/animation/SpellAnimationLoader.java src/main/resources/data/sihriya/sihriya_spell_animations/defaults.json
git commit -m "feat: JSON spell animation loader with default fallback mapping"
```

---

### Task 5: Intégrer les animations dans SpellCastHandler + Sihriya.java

**Files:**
- Modify: `src/main/java/tong/sihriya/core/SpellCastHandler.java`
- Modify: `src/main/java/tong/sihriya/Sihriya.java`
- Modify: `src/main/java/tong/sihriya/integration/EpicFightIntegration.java`

- [ ] **Step 1: Ajouter l'appel d'animation dans SpellCastHandler.castSpell()**

Dans `SpellCastHandler.java`, ajouter après la vérification du mana et avant l'exécution des effets :

```java
// Jouer l'animation de chant (côté serveur seulement)
if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
    tong.sihriya.animation.SihriyaAnimationPlayer.play(sp, spellId,
        tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase.CHANT);
}

// ... (cast time plus tard, après le scheduler) ...

// Après l'exécution des effets, jouer l'animation de cast
if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
    tong.sihriya.animation.SihriyaAnimationPlayer.play(sp, spellId,
        tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase.CAST);
}
```

- [ ] **Step 2: Ajouter init de l'animation loader dans EpicFightIntegration.init()**

Dans `EpicFightIntegration.java`, après `SpellAnimationManager.registerDefaults()` :

```java
// Enregistrer le SpellAnimationLoader (reload listener)
net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
    (net.minecraftforge.event.AddReloadListenerEvent event) -> {
        event.addListener(new tong.sihriya.animation.SpellAnimationLoader());
    });
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/tong/sihriya/core/SpellCastHandler.java src/main/java/tong/sihriya/integration/EpicFightIntegration.java
git commit -m "feat: integrate animation player into spell cast flow and resource reload"
```

---

### Task 6: Créer IronsSpellsCompat.java (détection + schools)

**Files:**
- Create: `src/main/java/tong/sihriya/compat/IronsSpellsCompat.java`

- [ ] **Step 1: Créer la classe avec détection runtime et init**

```java
package tong.sihriya.compat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tong.sihriya.Sihriya;
import tong.sihriya.animation.SihriyaAnimationPlayer;
import tong.sihriya.data.SchoolRegistry;

public class IronsSpellsCompat {
    private static boolean detected = false;

    public static void init() {
        try {
            Class.forName("io.redspace.ironsspellbooks.IronsSpellbooks");
            detected = true;
            Sihriya.LOGGER.info("Iron's Spells 'n Spellbooks détecté ! Compatibilité activée.");
            MinecraftForge.EVENT_BUS.register(new IronsSpellsCompat());
        } catch (ClassNotFoundException e) {
            detected = false;
            Sihriya.LOGGER.info("Iron's Spells 'n Spellbooks non détecté. Compatibilité désactivée.");
        }
    }

    public static boolean isDetected() {
        return detected;
    }

    @SubscribeEvent
    public void onSpellPreCast(io.redspace.ironsspellbooks.api.events.SpellPreCastEvent event) {
        if (!detected) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        String spellId = event.getSpellId();
        // Ne jouer que si le sort est un sort Sihriya (préfixe namespace)
        if (!spellId.startsWith(Sihriya.MODID + ".")) return;

        SihriyaAnimationPlayer.play(sp, spellId,
            SihriyaAnimationPlayer.SpellPhase.CHANT);
    }

    @SubscribeEvent
    public void onSpellCast(io.redspace.ironsspellbooks.api.events.SpellOnCastEvent event) {
        if (!detected) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        String spellId = event.getSpellId();
        if (!spellId.startsWith(Sihriya.MODID + ".")) return;

        SihriyaAnimationPlayer.play(sp, spellId,
            SihriyaAnimationPlayer.SpellPhase.CAST);

        // Déclencher nos particules glow
        var spell = tong.sihriya.data.SpellRegistry.get(spellId);
        if (spell != null) {
            tong.sihriya.network.NetworkHandler.sendToPlayer(
                new tong.sihriya.network.SpellParticlePacket(spellId, spell.school),
                sp);
        }
    }
}
```

- [ ] **Step 2: Appeler IronsSpellsCompat.init() dans Sihriya.java**

Dans `Sihriya.java`, ajouter après `EpicFightIntegration.init()` :

```java
tong.sihriya.compat.IronsSpellsCompat.init();
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/tong/sihriya/compat/IronsSpellsCompat.java src/main/java/tong/sihriya/Sihriya.java
git commit -m "feat: Iron's Spells runtime detection with animation + particle hooks via events"
```

---

### Task 7: Enregistrer les écoles Sihriya dans le registre Iron's Spells

**Files:**
- Modify: `src/main/java/tong/sihriya/compat/IronsSpellsCompat.java`

- [ ] **Step 1: Ajouter la méthode registerSchools()**

Dans `IronsSpellsCompat.java`, ajouter :

```java
private static void registerSchools() {
    try {
        var schoolRegistry = io.redspace.ironsspellbooks.api.registry.SchoolRegistry.getInstance();
        for (var schoolEntry : SchoolRegistry.getAll().entrySet()) {
            String id = schoolEntry.getKey();
            var school = schoolEntry.getValue();

            var schoolType = new io.redspace.ironsspellbooks.api.spells.SchoolType(
                new net.minecraft.resources.ResourceLocation(Sihriya.MODID, id),
                net.minecraft.network.chat.Component.translatable("school." + Sihriya.MODID + "." + id),
                school.color,
                new org.joml.Vector3f(
                    (school.color >> 16 & 0xFF) / 255f,
                    (school.color >> 8 & 0xFF) / 255f,
                    (school.color & 0xFF) / 255f
                ),
                io.redspace.ironsspellbooks.damage.SpellDamageSource.DamageType.FIRE // fallback
            );
            // Register via schoolRegistry
        }
    } catch (Exception e) {
        Sihriya.LOGGER.error("Failed to register Sihriya schools in Iron's Spells", e);
    }
}
```

**Note:** SchoolRegistry n'a pas de méthode `register()` publique directe. Cette étape peut nécessiter d'utiliser les events Forge ou d'étudier comment les addons Iron's Spells enregistrent leurs écoles. Si l'API ne le permet pas, on skip cette étape et on garde juste les event hooks.

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/compat/IronsSpellsCompat.java
git commit -m "feat: register Sihriya schools in Iron's Spells SchoolRegistry"
```

---

### Task 8: Build, test et correction

**Files:**
- Build output

- [ ] **Step 1: Build avec Iron's Spells**

```bash
./gradlew build 2>&1
```
Expected: BUILD SUCCESSFUL. Si erreurs, les corriger.

- [ ] **Step 2: Build sans Iron's Spells (simulation)**

Vérifier que le mod compile aussi sans Iron's Spells (le `compileOnly` + try/catch garantit ça).

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "build: fix compilation with Iron's Spells optional dependency"
```
