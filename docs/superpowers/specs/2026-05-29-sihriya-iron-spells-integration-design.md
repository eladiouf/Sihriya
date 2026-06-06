# Sihriya — Design Document V3 : Iron's Spells + EFISCompat + STAT Mod

## Objectif

Intégration en **superposition** de 3 systèmes pour Sihriya (Forge 1.20.1) :

- **Système natif** (touches 1-6, ManaManager STAT Mod, SchoolKeyHandler) — **inchangé**
- **Iron's Spells 'n Spellbooks** (v3.15.6) — compatibilité optionnelle (grimoires, API)
- **EFISCompat** — patterns d'animations Epic Fight pour les sorts
- **STAT Mod** — stats, perks, progression (déjà intégré)

---

## 1. Architecture (Superposition)

```
┌──────────────────────────────────────────────────────────────┐
│                    Sihriya Native System                      │
│  Touches 1-6 + Shift+1-3 | SchoolKeyHandler | ManaManager   │
│  SpellCastHandler | SchoolProgression | TierUnlockHandler    │
│  252 sorts JSON | 9 écoles | Particules custom glow          │
├──────────────────────────────────────────────────────────────┤
│                  Iron's Spells Compat (optionnel)              │
│  IronsSpellsCompat.java                                       │
│  ├─ Enregistre nos 9 écoles comme SchoolType Iron's Spells   │
│  ├─ Enregistre nos 252 sorts comme AbstractSpell Iron's      │
│  └─ Permet nos sorts dans les grimoires Iron's Spells        │
├──────────────────────────────────────────────────────────────┤
│                Epic Fight Animation Layer                      │
│  SihriyaAnimations.java (← inspiré EFISCompat Animation.java) │
│  ├─ Enregistre StaticAnimation via AnimationRegistryEvent     │
│  ├─ SpellAnimationLoader.json : spell → chant/cast/continuous │
│  └─ Joué via ServerPlayerPatch.playAnimationSynchronized()    │
├──────────────────────────────────────────────────────────────┤
│                   STAT Mod (obligatoire)                       │
│  STATModIntegration | SihriyaPerks | StatPassthroughHandler   │
│  MANA_POOL → maxMana | CASTING_SPEED → castTime              │
│  FIRE_AFFINITY → scaling feu | ...                            │
└──────────────────────────────────────────────────────────────┘
```

### Principe

| Système | Remplacé ? | Dépendance |
|---------|-----------|------------|
| ManaManager | **Non** (STAT Mod) | STAT Mod |
| SchoolKeyHandler (1-6) | **Non** | — |
| SchoolProgression | **Non** | STAT Mod |
| Particules glow | **Non** (custom) | — |
| **Iron's Spells** | Compat uniquement | Optionnel |
| **Animations Epic Fight** | Nouveau | Epic Fight |
| **STAT Mod** | Déjà intégré | Obligatoire |

---

## 2. Dépendances

### build.gradle

```gradle
repositories {
    maven { url "https://cursemaven.com" }
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    // Existants
    implementation fg.deobf("blank:epic-fight-20.14.17-mc1.20.1-forge:20.14.17")
    implementation project(':STAT_MOD')

    // Iron's Spells (compileOnly — optional runtime via mods.toml)
    compileOnly fg.deobf("curse.maven:irons-spells-n-spellbooks-855414:7907340")
    runtimeOnly fg.deobf("curse.maven:irons-spells-n-spellbooks-855414:7907340")
}
```

### mods.toml

```toml
[[dependencies.sihriya]]
    modId="irons_spellbooks"
    mandatory=false  # Optionnel
    versionRange="[3.15,)"
    ordering="NONE"
    side="BOTH"

[[dependencies.sihriya]]
    modId="epicfight"
    mandatory=true
    versionRange="[20.14,)"
    ordering="NONE"
    side="BOTH"

[[dependencies.sihriya]]
    modId="statmod"
    mandatory=true
    versionRange="[1.0,)"
    ordering="NONE"
    side="BOTH"
```

---

## 3. Système d'Animations (inspiré EFISCompat)

### 3.1 Registration des animations

```java
// SihriyaAnimations.java — inspiré de efiscompat Animation.java
@SubscribeEvent
public static void registerAnimations(AnimationRegistryEvent event) {
    event.newBuilder(Sihriya.MODID, SihriyaAnimations::build);
}

public static void build(AnimationManager.AnimationBuilder builder) {
    // Chanting (incantation)
    CHANT_ONE_HAND = builder.nextAccessor("biped/living/sihriya_chant_one_hand",
        a -> new StaticAnimation(true, a, Armatures.BIPED));
    CHANT_TWO_HAND = builder.nextAccessor("biped/living/sihriya_chant_two_hand",
        a -> new StaticAnimation(true, a, Armatures.BIPED));
    CHANT_STAFF = builder.nextAccessor("biped/living/sihriya_chant_staff",
        a -> new StaticAnimation(true, a, Armatures.BIPED));

    // Casting (lancer)
    CAST_ONE_HAND = builder.nextAccessor("biped/living/sihriya_cast_one_hand",
        a -> new StaticAnimation(false, a, Armatures.BIPED));
    CAST_TWO_HAND = builder.nextAccessor("biped/living/sihriya_cast_two_hand",
        a -> new StaticAnimation(false, a, Armatures.BIPED));
    CAST_STAFF = builder.nextAccessor("biped/living/sihriya_cast_staff",
        a -> new StaticAnimation(false, a, Armatures.BIPED));

    // Spéciaux (inspiré ActionAnimation EFISCompat)
    CAST_FLYING = builder.nextAccessor("biped/living/sihriya_cast_flying",
        a -> new ActionAnimation(0F, a, Armatures.BIPED)
            .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
            .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME,
                TimePairList.create(0F, Float.MAX_VALUE)));
}
```

### 3.2 Mapping spells → animations (JSON)

Fichier : `data/sihriya/sihriya_spell_animations/<school>.json`

```json
{
  "spells": {
    "fire.fireball": {
      "chant_animation":  "CHANT_ONE_HAND",
      "cast_animation":   "CAST_ONE_HAND",
      "staff_chant":      "CHANT_STAFF",
      "staff_cast":       "CAST_STAFF",
      "duration": 15
    },
    "fire.meteor": {
      "chant_animation":  "CHANT_TWO_HAND",
      "cast_animation":   "CAST_TWO_HAND",
      "staff_chant":      "CHANT_STAFF",
      "staff_cast":       "CAST_STAFF",
      "duration": 40
    }
  },
  "default": {
    "chant_animation":  "CHANT_ONE_HAND",
    "cast_animation":   "CAST_ONE_HAND",
    "staff_chant":      "CHANT_STAFF",
    "staff_cast":       "CAST_STAFF"
  }
}
```

### 3.3 Déclenchement des animations

```java
// Dans SpellCastHandler (casting natif 1-6) :
if (!player.level().isClientSide) {
    SihriyaAnimationPlayer.play(player, spellId, SpellPhase.CHANT);
    // ... cast time ...
    SihriyaAnimationPlayer.play(player, spellId, SpellPhase.CAST);
}

// Via Iron's Spells events (quand le sort vient d'un grimoire) :
@SubscribeEvent
public static void beforeSpellCast(SpellPreCastEvent event) {
    if (event.getEntity() instanceof ServerPlayer sp) {
        SihriyaAnimationPlayer.play(sp, event.getSpellId(), SpellPhase.CHANT);
    }
}

@SubscribeEvent
public static void onSpellCast(SpellOnCastEvent event) {
    if (event.getEntity() instanceof ServerPlayer sp) {
        SihriyaAnimationPlayer.play(sp, event.getSpellId(), SpellPhase.CAST);
    }
}
```

---

## 4. Compatibilité Iron's Spells

### 4.1 Détection (runtime)

```java
// IronsSpellsCompat.java
public class IronsSpellsCompat {
    private static boolean detected = false;

    public static void init() {
        try {
            Class.forName("io.redspace.ironsspellbooks.IronsSpellbooks");
            detected = true;
            registerSchools();
            registerSpells();
        } catch (ClassNotFoundException e) {
            detected = false;
        }
    }
}
```

### 4.2 Enregistrement des écoles

Nos 9 écoles Sihriya deviennent des `SchoolType` Iron's Spells :

```java
private static void registerSchools() {
    // Via SchoolRegistry.register() Iron's Spells API
    for (var school : SchoolRegistry.getAll()) {
        SchoolType schoolType = new SchoolType(
            ResourceLocation.fromNamespaceAndPath(Sihriya.MODID, school.id),
            school.displayName,
            school.color,
            school.targetingColor,
            school.damageType
        );
        // Register schoolType...
    }
}
```

### 4.3 Enregistrement des sorts (optionnel)

Si on veut que nos sorts apparaissent dans les grimoires Iron's Spells :

```java
// AbstractSpell wrapper pour chaque sort Sihriya
public class SihriyaSpellWrapper extends AbstractSpell {
    private final SpellData spellData;

    public SihriyaSpellWrapper(SpellData data) {
        this.spellData = data;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return ResourceLocation.fromNamespaceAndPath(
            Sihriya.MODID, spellData.id);
    }

    @Override
    public CastType getCastType() { /* ... */ }

    @Override
    public int getManaCost(int level) { return spellData.manaCost; }

    @Override
    public void onCast(...) {
        // Délègue à notre SpellCastHandler
        SpellCastHandler.executeSpell(world, player, spellData.id, level);
    }
}
```

### 4.4 Ce qu'on NE fait PAS

- On ne remplace PAS ManaManager par MagicData Iron's Spells
- On ne remplace PAS SchoolKeyHandler par leur GUI
- On ne remplace PAS notre HUD mana
- Nos sorts restent castables via 1-6 sans Iron's Spells

---

## 5. Particules & Effets visuels

### 5.1 Système existant (inchangé)

- `SchoolGlowParticle` — particules glow additive par école
- `MagicCircleEntity` — cercle magique animé
- `SpellParticleHelper` — spawn cercles + burst

### 5.2 Améliorations via Iron's Spells API

Étudier les patterns de particules d'Iron's Spells pour améliorer les nôtres :

| Particule Iron's Spells | Inspi pour Sihriya |
|------------------------|-------------------|
| `ShockwaveParticle` | Effet d'impact au sol (anneau grandissant) |
| `BlastwaveParticle` | Onde de choc élémentaire |
| `FogParticle` | Nuage de fumée/brume |
| `FireParticle` | Plus de variants de flammes |
| `ElectricityParticle` | Arc électrique |
| `WispParticle` | Particules guides |

---

## 6. Intégrations existantes (inchangées)

### STAT Mod
- `STATModIntegration.java` — bridge complet
- `ManaManager` — maxMana via MANA_POOL
- `TierUnlockHandler` — paliers 25/50/75/100
- `SihriyaPerks` — 15 perks (stubs)
- `SchoolProgression` — levels via XP

### Epic Fight
- `EpicFightIntegration.java` — détection et init
- `SkillMappingManager` — mapping sorts → skills Epic Fight
- `ActiveSpellSkill` — encapsulation skill
- `EpicFightEffects` — effets élémentaires passifs

---

## 7. Structure des fichiers (mise à jour)

```
src/main/java/tong/sihriya/
├── Sihriya.java
├── animation/
│   ├── SihriyaAnimations.java        ★ NOUVEAU (inspiré EFISCompat Animation.java)
│   ├── SihriyaAnimationPlayer.java   ★ NOUVEAU : joue animation sur joueur
│   ├── SpellAnimationLoader.java     ★ NOUVEAU : charge mapping JSON
│   ├── SpellAnimationManager.java    (existant)
│   └── SpellAnimation.java           (existant)
├── compat/
│   └── IronsSpellsCompat.java        ★ NOUVEAU : bridge Iron's Spells optionnel
├── core/   ...(existant)
├── data/   ...(existant)
├── network/ ...(existant)
├── client/
│   ├── particle/  ...(existant)
│   └── ...(existant)
├── integration/
│   ├── STATModIntegration.java       (existant)
│   ├── EpicFightIntegration.java     (existant)
│   └── SihriyaPerks.java            (existant)
└── registry/
    └── SihriyaEntities.java          (existant)
```

---

## 8. Ordre d'implémentation

### Milestone 1 : Dépendances & Animations
1. Ajouter Iron's Spells dans build.gradle (compileOnly)
2. Créer SihriyaAnimations.java (registration Epic Fight)
3. Créer SihriyaAnimationPlayer.java (playAnimationSynchronized)
4. Créer SpellAnimationLoader.java (JSON mapping → animation)
5. Créer les JSON de mapping pour les 252 sorts

### Milestone 2 : Bridge Iron's Spells
6. Créer IronsSpellsCompat.java (détection runtime)
7. Enregistrer les 9 écoles Sihriya comme SchoolType
8. Optionnel : envelopper nos sorts en AbstractSpell
9. Hooks SpellPreCastEvent/SpellOnCastEvent → animations + particules

### Milestone 3 : Amélioration particules
10. Étudier ShockwaveParticle/BlastwaveParticle Iron's Spells
11. Ajouter nouveaux types de particules si besoin

### Milestone 4 : Polish
12. Tester compatibilité avec/sans Iron's Spells
13. Tester animations avec Epic Fight
14. Tester scaling STAT Mod avec les nouveaux systèmes
15. Update GUIDE-DEV.md

---

## 9. Flux complet (exemple)

```
Joueur appuie sur 1 (Feu)
       ↓
SchoolKeyHandler détecte la touche
       ↓
SpellCastHandler.castSpell(player, "fire.fireball")
       ↓
├─ ManaManager.consumeMana(player, 12)  ← STAT Mod MANA_POOL
├─ SihriyaAnimationPlayer.play(player, "fire.fireball", CHANT)  ← Epic Fight
├─ [castTime ticks plus tard]
├─ SihriyaAnimationPlayer.play(player, "fire.fireball", CAST)   ← Epic Fight
├─ SpellParticleHelper.spawnCircleAround(player, "fire", 40)     ← Particules glow
├─ STATModIntegration.awardXp(player, FIRE_AFFINITY)             ← STAT Mod XP
└─ TierUnlockHandler.checkUnlock(player, "fire")                 ← Paliers

=== Si Iron's Spells est installé ===
└─ IronsSpellsCompat déclenche aussi SpellOnCastEvent
   → nos particules + animations jouent aussi via les events Iron's
```

---

## 10. Non-Goals

- Remplacer ManaManager par MagicData Iron's Spells
- Remplacer SchoolKeyHandler par la GUI Iron's Spells
- Remplacer notre HUD mana
- Dépendre obligatoirement d'Iron's Spells
- Réécrire les 252 sorts existants
- Modifier STAT Mod ou Epic Fight
