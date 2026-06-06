# Guide Développeur — Sihriya

Bienvenue sur **Sihriya** (سحرية), un mod Forge 1.20.1 qui ajoute un système de magie élémentaire complet, **addon profond d'Epic Fight et STAT Mod**.

---

## 1. Présentation générale

Sihriya est un **addon** des deux mods suivants, tous deux **obligatoires** :

```
STAT_MOD/
├── STAT_MOD/     (mod RPG — 23 stats, fatigue, soif, perks, skills Epic Fight)
└── SIHRIYA/      (addon magie — écoles, sorts, mana, animations Epic Fight)
```

**Lien avec STAT Mod** : **OBLIGATOIRE**. Sihriya exploite 100% des stats de STAT Mod :
- `FIRE_AFFINITY`, `WATER_AFFINITY`, `AIR_AFFINITY`, `EARTH_AFFINITY`, `ARCANE_POWER` → scaling des sorts
- `MANA_POOL` → capacité de mana du joueur (mana max = `BASE_MAX_MANA + bonus MANA_POOL`)
- `CASTING_SPEED` → vitesse d'incantation
- `MAGIC_RESISTANCE` → résistance magique
- `ERUDITION` → bonus XP écoles
- `WILLPOWER` → réduction lock-out mana
- `ActionXpHelper` → XP aux stats à chaque cast de sort
- **Perks magiques** (actuellement "standby") → implémentés par Sihriya

**Minecraft** : 1.20.1
**Forge** : 47.4.20
**Epic Fight** : 20.14.17 (obligatoire, JAR local dans `libs/`)
**STAT Mod** : 1.0.0+ (obligatoire, source dans `../STAT_MOD/`)

**Contenu actuel** : 9 écoles et 252 sorts data-driven, soit 28 sorts par école
répartis en T1/T2/T3/T4/T5 = 6/8/8/4/2.

---

## 2. Architecture du code

```
src/main/java/tong/sihriya/
├── Sihriya.java                     # Classe principale @Mod
├── core/
│   ├── ManaManager.java             # Capability — mana du joueur (driven par MANA_POOL)
│   ├── ManaProvider.java            # Attache la capability au joueur
│   ├── SchoolProgression.java       # Capability — niveaux écoles, sorts appris
│   ├── SchoolProgressionProvider.java
│   ├── CapabilityHandler.java       # Enregistre les capabilities sur Forge
│   ├── PlayerLoginHandler.java      # Première connexion : école basée sur stat la plus haute + sorts T1
│   ├── SpellCastHandler.java        # Exécution des sorts (mana, cooldown, effets, castBySchool)
│   ├── TierUnlockHandler.java       # Vérifie les paliers 25/50/75/100 pour débloquer les tiers
│   └── MeditationHandler.java       # V → regen mana (0.1%/tick)
├── data/
│   ├── SchoolRegistry.java          # Modèle des écoles (data-driven, 9 écoles)
│   ├── SpellRegistry.java           # Modèle des sorts (data-driven)
│   └── DataLoader.java              # Charge les JSON (avec fallback en dur)
├── network/
│   ├── NetworkHandler.java          # Canal réseau Forge
│   ├── ManaSyncPacket.java          # Sync mana serveur → client
│   └── SchoolSyncPacket.java        # Sync écoles/sorts serveur → client
├── client/
│   ├── ClientManaData.java          # Cache mana côté client
│   ├── ClientSchoolData.java        # Cache écoles côté client
│   ├── KeyBindings.java             # Touches : 1-6 (écoles), Shift+1-3 (avancées), V (méditation), G (grimoire), R (roue)
│   ├── SchoolKeyHandler.java        # Handler touches 1-6 + Shift+1-3 → SchoolCastPacket
│   ├── ClientSetup.java             # Enregistrement clés + overlays
│   └── gui/
│       └── ManaOverlay.java         # HUD barre de mana
├── epicfight/
│   ├── ActiveSpellSkill.java        # Skill Epic Fight → lance un sort Sihriya
│   ├── SkillMappingManager.java     # Enregistre les sorts via SkillBuildEvent
│   ├── StatPassthroughHandler.java  # Passe les bonus STAT Mod aux dégâts Epic Fight
│   └── package-info.java
├── integration/
│   ├── STATModIntegration.java      # Bridge vers STAT Mod API (obligatoire)
│   ├── EpicFightIntegration.java    # Intégration Epic Fight (animations)
│   ├── EpicFightEffects.java        # Effets élémentaires sur attaques physiques
│   └── SihriyaPerks.java            # Perks magiques (stub, 15 perks définis)
├── animation/
│   ├── SpellAnimation.java          # Donnée d'animation (record)
│   ├── SpellAnimationManager.java   # Registry d'animations par type de sort
│   └── package-info.java
└── projectile/
    ├── SpellProjectile.java         # Projectile magique (ThrowableProjectile)
    └── package-info.java
```

---

## 3. Data-driven (JSON)

Tout le contenu est défini dans `src/main/resources/data/sihriya/` :

### `schools.json`

```json
{
  "id": "fire",
  "name": "Feu",
  "starting": true,
  "color": "FF4500",
  "unlock": null
}
```

| Champ | Description |
|-------|-------------|
| `id` | Identifiant unique |
| `starting` | `true` = école de départ possible |
| `unlock` | Condition de déblocage (null si disponible dès le début). Supporte `"and"` et `"or"` pour les conditions composées |

**9 écoles** : fire, water, wind, earth, lightning, ice, lava, necromancy, lumamancy

**Écoles de base (1-6)** : Feu, Eau, Vent, Terre, Foudre, Glace
**Écoles avancées (Shift+1-3)** : Lave, Nécromancie, Lumamancie

**Conditions de déblocage** : les écoles avancées utilisent des conditions composées avec `"and"` et `"or"`. Exemple :
- Lave : Feu ≥ 50 **ET** Terre ≥ 50
- Nécromancie : condition spécifique
- Lumamancie : condition spécifique

### `spells.json`

```json
{
  "id": "fire.fireball",
  "school": "fire",
  "tier": 1,
  "manaCost": 15,
  "cooldown": 30,
  "type": "PROJECTILE",
  "effects": [
    {"type": "damage", "baseValue": 6.0, "scaling": 0.1, "duration": 0},
    {"type": "burn", "baseValue": 2.0, "scaling": 0.05, "duration": 100}
  ]
}
```

| Champ | Description |
|-------|-------------|
| `id` | `{école}.{nom}` (ex: `fire.fireball`, `water.water_bolt`) |
| `tier` | 1-4 (T1 gratuit, T2 parchemin, T3 grimoire, T4/Ultime grimoire rare) |
| `type` | `PROJECTILE`, `ZONE`, `BUFF`, `SUMMON`, `ULTIMATE` |
| `effects[].type` | `damage`, `burn`, `slow`, `knockback`, `stun`, `freeze`, `chain`, `heal` |
| `scaling` | Multiplicateur par niveau d'école |
| `duration` | Durée en ticks (20 ticks = 1 seconde) |

---

## 4. Système de Mana (unifié avec STAT Mod)

- **Capacité** : `ManaManager` attaché au joueur via Capability Forge
- **Mana max** : `BASE_MAX_MANA (50) + bonus MANA_POOL` (stat index 14, STAT Mod)
  - `MANA_POOL` donne **+1 mana max par niveau** (jusqu'à +100 au niveau 100)
- **Régénération passive** : **0.1% du mana max par tick** (2% par seconde)
- **Méditation** : touche V → regen accélérée + animation Epic Fight + Slowness III
- **Blocage ultime** : 30 secondes sans regen après un sort ultime (réduit par WILLPOWER)
- **Sync** : paquet `ManaSyncPacket` envoyé au client toutes les 20 ticks
- **XP STAT Mod** : chaque sort lancé donne `ActionXpHelper.awardXp(player, MANA_POOL, ...)`

### Méditation (animation Epic Fight)
Le joueur appuie sur **V** → animation de méditation Epic Fight + regen accélérée + Slowness III.

### Sommeil
Se coucher dans un lit → mana restauré à 100%.

---

## 5. Écoles et progression

### Départ

À la première connexion, `PlayerLoginHandler` choisit l'école de départ **en fonction de la stat STAT Mod la plus élevée** du joueur (ex: si `FIRE_AFFINITY` est la plus haute → école Feu). En cas d'égalité, le choix est aléatoire parmi les ex aequo. Le joueur reçoit 2 sorts T1 de cette école.

### Niveaux & Paliers (TierUnlockHandler)

Chaque école monte de 0 à 100 via l'utilisation de ses sorts (5 XP par cast + bonus ERUDITION). `TierUnlockHandler` vérifie les paliers à chaque cast :

| Niveau école | Déblocage | Synergie STAT Mod |
|-------------|-----------|-------------------|
| 1 (départ) | 2 sorts T1 aléatoires | Bonus XP stat correspondante |
| 25 | Tous les T1 + accès T2 | Débloque un perk magique |
| 50 | Accès T3 + école avancée possible | +Niveaux dans la stat STAT Mod |
| 75 | Accès T4 | Débloque un perk avancé |
| 100 | Sort ultime | Perk ultime + bonus stat massif |

### Déblocage des écoles avancées

Vérifié par `TierUnlockHandler` à chaque utilisation de sort. Les conditions sont lues depuis `schools.json` et supportent la logique `"and"` et `"or"` :

```json
{"or": [{"and": [{"school": "fire", "level": 50}, {"school": "earth", "level": 50}]}, ...]}
```

---

## 6. Interface

### Touches

| Touche | Action |
|--------|--------|
| **1** | Lancer sort École de Feu |
| **2** | Lancer sort École d'Eau |
| **3** | Lancer sort École de Vent |
| **4** | Lancer sort École de Terre |
| **5** | Lancer sort École de Foudre (si débloquée) |
| **6** | Lancer sort École de Glace (si débloquée) |
| **Shift+1** | Lancer sort École de Lave (si débloquée) |
| **Shift+2** | Lancer sort École de Nécromancie (si débloquée) |
| **Shift+3** | Lancer sort École de Lumamancie (si débloquée) |
| **V** | Méditer (regen mana + animation Epic Fight) |
| **G** | Ouvrir le grimoire |
| **R** (maintenu) | Roue des sorts |

Chaque touche d'école lance le **meilleur sort connu** de cette école avec **l'animation Epic Fight** correspondante.

### HUD Mana

Barre bleue en haut à gauche sous la barre de faim. Devient rouge si le mana est bloqué. La capacité max est influencée par la stat `MANA_POOL` de STAT Mod.

---

## 7. Bridge STAT Mod (STATModIntegration)

La classe `STATModIntegration` est le pont entre Sihriya et l'API STAT Mod. Elle détecte STAT Mod au chargement via `Class.forName("tong.statmod.stats.StatType")`.

**Mapping des stats** :

| École Sihriya | Stat STAT Mod |
|---------------|---------------|
| fire | `FIRE_AFFINITY` |
| water | `WATER_AFFINITY` |
| wind | `AIR_AFFINITY` |
| earth | `EARTH_AFFINITY` |
| lightning | `ARCANE_POWER` |
| ice | `WATER_AFFINITY` |
| lava | `FIRE_AFFINITY` + `EARTH_AFFINITY` |
| necromancy | (stat dédiée ou `ARCANE_POWER`) |
| lumamancy | (stat dédiée ou `ARCANE_POWER`) |

Le bonus est de +0.2% par niveau de stat, appliqué au scaling du sort.

---

## 8. Intégration Epic Fight (EpicFightIntegration)

### Animations (uniques par sort)
Chaque sort a sa propre animation Epic Fight définie dans `assets/sihriya/animations/`. Utilisation du système `Layer.Priority` d'Epic Fight. La stat `CASTING_SPEED` de STAT Mod réduit la durée d'incantation.

### Combat passif
`EpicFightIntegration` écoute `LivingHurtEvent`. Quand un joueur avec une école active frappe une entité :
- Chance d'appliquer un effet élémentaire = `FIRE_AFFINITY.level%` pour le feu, etc.
- Effet dépend de l'école active (Feu → brûlure, Vent → knockback, etc.)
- L'intensité scale avec le niveau de la **stat STAT Mod** correspondante
- Le % scale avec le niveau du joueur (pas de fixe à 15%)

### Skills Epic Fight (ActiveSpellSkill)
Chaque sort Sihriya est enregistré comme un **Skill Epic Fight** via `SkillBuildEvent` dans `SkillMappingManager`. Le skill utilise `SkillCategories.BASIC_ATTACK` et le type `ONE_SHOT`. L'exécution passe par `SpellCastHandler.castSpell()`.

### Casting
Les sorts sont lancés via les touches 1-6 (+ Shift+1-3 pour les avancées). Chaque cast joue une animation Epic Fight et peut produire des projectiles avec le système de projectile Epic Fight.

---

## 9. Perks magiques (intégration STAT Mod)

STAT Mod a une infrastructure de perks complète (42 perks en 3 tiers par stat). Les perks magiques sont actuellement "standby". Sihriya va les implémenter dans `integration/SihriyaPerks.java` :

| Perk | Stat | Niveau | Effet |
|------|------|--------|-------|
| Combustion | FIRE_AFFINITY | 20 | +25% dégâts feu |
| Inferno | FIRE_AFFINITY | 50 | Zone de feu AoE |
| Pyromania | FIRE_AFFINITY | 80 | Brûlure se propage |
| Geyser | WATER_AFFINITY | 20 | +25% durée slow |
| Tourbillon | WATER_AFFINITY | 50 | Pousse les ennemis |
| Tsunami | WATER_AFFINITY | 80 | Stun + dégâts zone |
| Rafale | AIR_AFFINITY | 20 | +50% knockback |
| Tempête | AIR_AFFINITY | 50 | Tornade aspirante |
| Ouragan | AIR_AFFINITY | 80 | Knockback + AoE |
| Sismique | EARTH_AFFINITY | 20 | +25% durée stun |
| Rocher | EARTH_AFFINITY | 50 | Bouclier de pierre |
| Cataclysme | EARTH_AFFINITY | 80 | AoE stun + dégâts |
| Foudre | ARCANE_POWER | 20 | +25% dégâts magiques |
| Tempête | ARCANE_POWER | 50 | Chaîne +2 cibles |
| Cataclysme | ARCANE_POWER | 80 | AoE foudre géant |

---

## 10. Points d'entrée pour le nouveau développeur

### Fichiers clés à connaître

| Fichier | Rôle |
|---------|------|
| `SpellCastHandler.java` | Cœur du système — exécute les sorts, gère mana/cooldown/effets |
| `ManaManager.java` | Capability mana — régénération, dépense, sync |
| `MeditationHandler.java` | Méditation — regen accélérée + animation Epic Fight |
| `TierUnlockHandler.java` | Vérifie les paliers 25/50/75/100 pour débloquer les tiers |
| `PlayerLoginHandler.java` | Première connexion — choisit l'école selon la stat la plus haute |
| `STATModIntegration.java` | Bridge vers l'API STAT Mod |
| `EpicFightIntegration.java` | Intégration Epic Fight (animations, effets passifs) |

### Ajouter une école

1. Ajouter l'entrée dans `schools.json` (avec conditions `"and"`/`"or"` si avancée)
2. Ajouter les sorts correspondants dans `spells.json`
3. Ajouter le mapping de stat dans `STATModIntegration` si nécessaire
4. Ajouter la touche dans `KeyBindings.java`

### Ajouter un sort

1. Ajouter dans `spells.json`
2. Les effets sont gérés dans `SpellCastHandler.executeEffects()` — ajoute un `case` si nouveau type d'effet

### Ajouter un nouvel effet

1. Créer la méthode dans `SpellCastHandler` (ex: `applyPoison`)
2. Ajouter le `case "poison"` dans le switch
3. Utiliser le type dans `spells.json`

### Ajouter un perk magique

1. Ajouter l'entrée dans l'enum `Perk` de STAT Mod (ou dans `SihriyaPerks` si séparé)
2. Implémenter l'effet dans `PerkEffectHandler`
3. Définir la condition de déblocage (stat + niveau)
4. Associer au palier dans `TierUnlockHandler`

### Builder le mod

```bash
cd chemin/vers/SIHRIYA
.\gradlew build
```

Le JAR se trouve dans `build/libs/sihriya-1.0.0.jar`.

### Tester

```bash
.\gradlew runClient
```

### Commandes utiles en jeu

- `/sihriya` — commandes de debug (TODO)
- Logs : `forge.logging.console.level=debug` dans `build.gradle`

---

## 11. Dépendances

| Mod | Requis | Version |
|-----|--------|---------|
| Forge | ✅ Oui | 47.4.20 |
| Epic Fight | ✅ **Obligatoire** | 20.14.17 (JAR dans `libs/`) |
| STAT Mod | ✅ **Obligatoire** | 1.0.0+ (source dans `../STAT_MOD/`) |
| Minecraft | ✅ Oui | 1.20.1 |

---

## 12. Notes techniques

- **Réseau** : Utilise l'API Forge `SimpleChannel` (ancienne API, pas `CustomPayloadEvent`)
- **ResourceLocation** : Le constructeur `new ResourceLocation(String, String)` est déprécié mais fonctionne en 1.20.1
- **Capabilities** : Système Forge standard attaché au joueur via `AttachCapabilitiesEvent`
- **JSON** : chargé via `DataLoader` avec fallback hardcodé si les fichiers sont absents
- **Java** : 17 (toolchain du build.gradle)
- **Mana regen** : 0.1% du max par tick passif, accéléré en méditation
- **Conditions composées** : les `unlock` dans `schools.json` supportent `"and"` et `"or"` imbriqués

---

*Bon courage et bienvenue sur Sihriya !*
