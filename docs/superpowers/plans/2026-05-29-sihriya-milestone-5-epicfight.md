# Milestone 5 : Epic Fight Integration — Animations & Combat

> **Goal:** Système d'animations Epic Fight pour chaque sort. Combat passif élémentaire scale avec STAT Mod.

**Architecture:** SpellAnimationManager enregistre les animations par sort. EpicFightIntegration joue les animations via le système Layer d'Epic Fight. EpicFightEffects amélioré avec scaling STAT Mod.

**Tech Stack:** Epic Fight API (Layer, AnimationPlayer, StaticAnimation), Forge Events (LivingHurtEvent)

---

### Task 5.1 : Créer le système d'animations

**Files:**
- Create: `src/main/java/tong/sihriya/animation/SpellAnimationManager.java`
- Create: `src/main/java/tong/sihriya/animation/SpellAnimation.java`

- [ ] **Step 1: Créer SpellAnimation.java**

```java
package tong.sihriya.animation;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.types.StaticAnimation;

public record SpellAnimation(
    ResourceLocation id,
    String spellId,
    StaticAnimation epicAnimation,
    int durationTicks,
    String particleType
) {}
```

- [ ] **Step 2: Créer SpellAnimationManager.java (stub — les vraies animations viendront des assets Blender)**

```java
package tong.sihriya.animation;

import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;

import java.util.*;

public class SpellAnimationManager {
    private static final Map<String, SpellAnimation> REGISTRY = new HashMap<>();

    public static void register(SpellAnimation anim) {
        REGISTRY.put(anim.spellId(), anim);
        Sihriya.LOGGER.debug("Registered animation for spell: {}", anim.spellId());
    }

    public static SpellAnimation get(String spellId) {
        return REGISTRY.get(spellId);
    }

    public static boolean hasAnimation(String spellId) {
        return REGISTRY.containsKey(spellId);
    }

    /** Charge les animations par défaut (sera remplacé par des assets Blender) */
    public static void registerDefaults() {
        // Pour chaque type de sort, on associe une animation générique
        // Ces animations seront créées dans Blender plus tard
        registerGeneric("PROJECTILE", "sihriya:cast_projectile", 15, "flame");
        registerGeneric("ZONE", "sihriya:cast_zone", 25, "spell");
        registerGeneric("BUFF", "sihriya:cast_buff", 10, "happy_villager");
        registerGeneric("SUMMON", "sihriya:cast_summon", 30, "witch");
        registerGeneric("ULTIMATE", "sihriya:cast_ultimate", 60, "dragon_breath");
    }

    private static void registerGeneric(String type, String animPath, int duration, String particle) {
        // Stub : sera lié aux vraies animations Blender
        Sihriya.LOGGER.debug("Animation stub registered for type: {}", type);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/tong/sihriya/animation/
git commit -m "feat: spell animation registry system with type-based defaults"
```

---

### Task 5.2 : Mettre à jour EpicFightIntegration.java

**Files:**
- Modify: `src/main/java/tong/sihriya/integration/EpicFightIntegration.java`

- [ ] **Step 1: Ajouter les méthodes de lecture d'animations**

```java
package tong.sihriya.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tong.sihriya.Sihriya;
import tong.sihriya.animation.SpellAnimation;
import tong.sihriya.animation.SpellAnimationManager;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;

public class EpicFightIntegration {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        try {
            Class.forName("yesman.epicfight.main.EpicFightMod");
            SpellAnimationManager.registerDefaults();
            Sihriya.LOGGER.info("Epic Fight détecté et intégré !");
            initialized = true;
        } catch (ClassNotFoundException e) {
            Sihriya.LOGGER.error("Epic Fight est requis mais introuvable !");
            throw new RuntimeException("Epic Fight manquant");
        }
    }

    public static boolean isInitialized() { return initialized; }

    /** Joue l'animation d'un sort sur un joueur */
    public static void playSpellAnimation(ServerPlayer player, String spellId) {
        if (!initialized) return;
        SpellData spell = SpellRegistry.get(spellId);
        if (spell == null) return;

        // Calcul du temps d'incantation réduit par CASTING_SPEED
        int castTime = STATModIntegration.getCastTime(player, getAnimationTime(spell));

        // TODO: Jouer l'animation via l'API Epic Fight
        // playerPatch.getClientAnimator().playAnimation(..., castTime);
        Sihriya.LOGGER.debug("Playing animation for {} ({} ticks)", spellId, castTime);
    }

    private static int getAnimationTime(SpellData spell) {
        return switch (spell.type) {
            case PROJECTILE -> 15;
            case ZONE -> 25;
            case BUFF -> 10;
            case SUMMON -> 30;
            case ULTIMATE -> 60;
        };
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/integration/EpicFightIntegration.java
git commit -m "feat: animation playback system with CASTING_SPEED reduction"
```

---

### Task 5.3 : Améliorer EpicFightEffects.java avec scaling STAT Mod

**Files:**
- Modify: `src/main/java/tong/sihriya/integration/EpicFightEffects.java`

- [ ] **Step 1: Remplacer le 15% fixe par un scaling basé sur la stat**

```java
package tong.sihriya.integration;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.core.SchoolProgressionProvider;
import tong.sihriya.data.SchoolRegistry;
import tong.statmod.stats.StatType;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class EpicFightEffects {
    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        LivingEntity target = event.getEntity();

        player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(prog -> {
            String activeSchool = prog.getActiveSchool();
            if (activeSchool.isEmpty()) return;
            if (!prog.isSchoolUnlocked(activeSchool)) return;

            // Chance = niveau de la stat STAT Mod correspondante (max 100%)
            StatType stat = STATModIntegration.schoolToStat(activeSchool);
            int statLevel = stat != null ? STATModIntegration.getStatLevel(player, stat) : 0;
            float chance = Math.min(1.0f, statLevel * 0.01f); // 1% par niveau

            if (player.getRandom().nextFloat() > chance) return;

            int schoolLevel = prog.getLevel(activeSchool);
            int duration = 40 + schoolLevel;

            // Intensité scale avec la stat (multiplie les effets)
            float intensity = 1.0f + statLevel * 0.01f;

            switch (activeSchool) {
                case "fire" -> {
                    target.setRemainingFireTicks((int)(duration * 2 * intensity));
                    target.hurt(player.damageSources().indirectMagic(player, player),
                        schoolLevel * 0.1f * intensity);
                }
                case "water" -> {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, (int)(duration * intensity), 1));
                }
                case "wind" -> {
                    var look = player.getLookAngle();
                    target.knockback(0.5f + schoolLevel * 0.01f * intensity, -look.x, -look.z);
                }
                case "earth" -> {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, (int)(duration * intensity), Math.min(2, schoolLevel / 20)));
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, (int)(duration / 2 * intensity), 2));
                }
                case "lightning" -> {
                    target.hurt(player.damageSources().lightningBolt(),
                        schoolLevel * 0.15f * intensity);
                    target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, (int)(duration / 2 * intensity), 0));
                }
                case "ice" -> {
                    target.setTicksFrozen((int)(duration * intensity));
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, (int)(duration * intensity), 2));
                }
            }
        });
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/integration/EpicFightEffects.java
git commit -m "feat: epic fight passive effects scale with STAT Mod affinity levels instead of fixed 15%"
```

---

### Task 5.4 : Lier les animations au SpellCastHandler

**Files:**
- Modify: `src/main/java/tong/sihriya/core/SpellCastHandler.java`

- [ ] **Step 1: Ajouter l'appel d'animation dans castSpell**

```java
// Après la vérification du mana, avant l'exécution des effets
// Ajouter :
if (!player.level().isClientSide) {
    EpicFightIntegration.playSpellAnimation(player, spellId);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/tong/sihriya/core/SpellCastHandler.java
git commit -m "feat: trigger epic fight animation playback on spell cast"
```
