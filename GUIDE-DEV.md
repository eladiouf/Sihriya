# Guide Développeur — Sihriya

Bienvenue sur **Sihriya** (سحرية), un mod Forge 1.20.1 qui ajoute un système de magie élémentaire complet compatible avec **Epic Fight** et optionnellement avec **STAT Mod**.

---

## 1. Présentation générale

Sihriya est un **mod indépendant** qui se place à côté de STAT Mod dans l'arborescence :

```
STAT_MOD/
├── STAT_MOD/     (mod combat physique — stats, fatigue, soif, arbres)
└── SIHRIYA/      (mod magie — écoles, sorts, mana, roue)
```

**Lien avec STAT Mod** : optionnel. Si STAT Mod est présent dans le dossier `mods`, Sihriya détecte ses stats magiques (`FIRE_AFFINITY`, `WATER_AFFINITY`, etc.) et les utilise pour le *scaling* des sorts. Sinon, le scaling se fait uniquement sur le niveau de l'école.

**Minecraft** : 1.20.1  
**Forge** : 47.4.20  
**Epic Fight** : 20.14.17 (optionnel, JAR local dans `libs/`)

---

## 2. Architecture du code

```
src/main/java/tong/sihriya/
├── Sihriya.java                     # Classe principale @Mod
├── core/
│   ├── ManaManager.java             # Capability — mana du joueur
│   ├── ManaProvider.java            # Attache la capability au joueur
│   ├── SchoolProgression.java       # Capability — niveaux écoles, sorts appris
│   ├── SchoolProgressionProvider.java
│   ├── CapabilityHandler.java       # Enregistre les capabilities sur Forge
│   ├── PlayerLoginHandler.java      # Première connexion : init école aléatoire + sorts T1
│   ├── SpellCastHandler.java        # Exécution des sorts (mana, cooldown, effets)
│   ├── RightClickCastHandler.java   # Clic droit main vide → lance le sort
│   └── MeditationHandler.java       # Sneak → regen mana ; sommeil → full mana
├── data/
│   ├── SchoolRegistry.java          # Modèle des écoles (data-driven)
│   ├── SpellRegistry.java           # Modèle des sorts (data-driven)
│   └── DataLoader.java              # Charge les JSON (avec fallback en dur)
├── network/
│   ├── NetworkHandler.java          # Canal réseau Forge
│   ├── ManaSyncPacket.java          # Sync mana serveur → client
│   └── SchoolSyncPacket.java        # Sync écoles/sorts serveur → client
├── client/
│   ├── ClientManaData.java          # Cache mana côté client
│   ├── ClientSchoolData.java        # Cache écoles côté client
│   ├── KeyBindings.java             # Touches : R (roue), V (méditation)
│   ├── ClientSetup.java             # Enregistrement clés + overlays
│   ├── SpellWheelInputHandler.java  # R maintenu → ouvre/ferme la roue
│   └── gui/
│       ├── SpellWheelScreen.java    # Écran roue circulaire de sélection
│       └── ManaOverlay.java         # HUD barre de mana
└── integration/
    ├── SihriyaAPI.java              # Bridge vers STAT Mod (réflexion)
    └── EpicFightEffects.java        # Effets élémentaires sur attaques physiques
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
| `unlock` | Condition de déblocage (null si disponible dès le début) |

**Écoles avancées** : Foudre se débloque quand Feu ≥ 50 OU Vent ≥ 50. Glace se débloque quand Eau ≥ 50.

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

## 4. Système de Mana

- **Capacité** : `ManaManager` attaché au joueur via Capability Forge
- **Mana max** : 50 de base (augmentable via STAT Mod)
- **Régénération** : uniquement via méditation (sneak) ou sommeil
- **Blocage ultime** : 30 secondes sans regen après un sort ultime
- **Sync** : paquet `ManaSyncPacket` envoyé au client toutes les 20 ticks

### Méditation

Le joueur s'accroupit (touche Shift ou V) → regen passif de 0.5 mana/tick + effet Slowness III.

### Sommeil

Se coucher dans un lit → mana restauré à 100%.

---

## 5. Écoles et progression

### Départ

1. À la première connexion, le joueur reçoit une école de départ aléatoire parmi celles marquées `starting: true`
2. 2 sorts T1 de cette école lui sont donnés aléatoirement

### Niveaux

Chaque école monte de 0 à 100 via l'utilisation de ses sorts (5 XP par cast).

| Niveau | Déblocage |
|--------|-----------|
| 1 | 2 sorts T1 aléatoires de l'école |
| 25 | Tous les T1 + accès T2 |
| 50 | Accès T3 + école avancée (Foudre/Glace) si conditions remplies |
| 75 | Accès T4 |
| 100 | Sort ultime |

### Déblocage des écoles avancées

Vérifié dans `SpellCastHandler.checkSchoolUnlocks()` à chaque utilisation de sort. Les conditions sont lues depuis `schools.json`.

---

## 6. Interface

### Roue des sorts (touche R maintenue)

- Ouvre une roue circulaire
- Affiche les sorts appris de l'école active
- Sélection à la souris
- Au relâchement de R, le sort sélectionné est lancé

### HUD Mana

Barre bleue en haut à gauche sous la barre de faim. Devient rouge si le mana est bloqué.

### Touches

| Touche | Action |
|--------|--------|
| **R** (maintenu) | Ouvrir la roue des sorts |
| **V** ou **Shift** | Méditer (regen mana) |
| **Clic droit** (main vide) | Lancer le sort actif |

---

## 7. Bridge STAT Mod

La classe `SihriyaAPI` détecte STAT Mod au chargement via `Class.forName("tong.statmod.stats.StatType")`.

**Mapping des stats** :

| École Sihriya | Stat STAT Mod |
|---------------|---------------|
| fire | `FIRE_AFFINITY` |
| water | `WATER_AFFINITY` |
| wind | `AIR_AFFINITY` |
| earth | `EARTH_AFFINITY` |
| lightning | `ARCANE_POWER` |
| ice | `WATER_AFFINITY` |

Le bonus est de +0.2% par niveau de stat, appliqué au scaling du sort.

---

## 8. Intégration Epic Fight

`EpicFightEffects` écoute `LivingHurtEvent`. Quand un joueur avec une école active frappe une entité :

- 15% de chance d'appliquer un effet élémentaire
- Effet dépend de l'école active (Feu → brûlure, Vent → knockback, etc.)
- L'intensité scale avec le niveau de l'école

**Casting** : le clic droit ne conflict pas avec Epic Fight (clic gauche pour attaquer).

---

## 9. Points d'entrée pour le nouveau développeur

### Ajouter une école

1. Ajouter l'entrée dans `schools.json`
2. Ajouter les sorts correspondants dans `spells.json`

### Ajouter un sort

1. Ajouter dans `spells.json`
2. Les effets sont gérés dans `SpellCastHandler.executeEffects()` — ajoute un `case` si nouveau type d'effet

### Ajouter un nouvel effet

1. Créer la méthode dans `SpellCastHandler` (ex: `applyPoison`)
2. Ajouter le `case "poison"` dans le switch
3. Utiliser le type dans `spells.json`

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

## 10. Dépendances

| Mod | Requis | Version |
|-----|--------|---------|
| Forge | ✅ Oui | 47.4.20 |
| Epic Fight | ❌ Optionnel | 20.14.17 (JAR dans `libs/`) |
| STAT Mod | ❌ Optionnel | 1.0.0+ |
| Minecraft | ✅ Oui | 1.20.1 |

---

## 11. Notes techniques

- **Réseau** : Utilise l'API Forge `SimpleChannel` (ancienne API, pas `CustomPayloadEvent`)
- **ResourceLocation** : Le constructeur `new ResourceLocation(String, String)` est déprécié mais fonctionne en 1.20.1
- **Capabilities** : Système Forge standard attaché au joueur via `AttachCapabilitiesEvent`
- **JSON** : chargé via `DataLoader` avec fallback hardcodé si les fichiers sont absents
- **Java** : 17 (toolchain du build.gradle)

---

*Bon courage et bienvenue sur Sihriya !*
