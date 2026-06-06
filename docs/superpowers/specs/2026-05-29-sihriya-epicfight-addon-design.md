# Sihriya — Design Document V2

## Objectif
Mod magie Forge 1.20.1, addon profond d'**Epic Fight** (animations/combat) et **STAT Mod** (stats/perks/progression).

---

## 1. Les 9 Écoles

| # | École | Sorts | Déblocage | Stat STAT Mod |
|---|-------|-------|-----------|---------------|
| 1 | 🔥 Feu | 14 | Départ | FIRE_AFFINITY |
| 2 | 💧 Eau | 14 | Départ | WATER_AFFINITY |
| 3 | 🌪️ Vent | 14 | Départ | AIR_AFFINITY |
| 4 | 🪨 Terre | 14 | Départ | EARTH_AFFINITY |
| 5 | ⚡ Foudre | 14 | Feu 50 OU Vent 50 | ARCANE_POWER |
| 6 | 🧊 Glace | 14 | Eau 50 | WATER_AFFINITY |
| 7 | 🌋 Lave | 14 | Feu 50 ET Terre 50 | FIRE_AFFINITY + EARTH_AFFINITY |
| 8 | 💀 Nécromancie | 14 | Terre 50 ET Foudre 50 | ARCANE_POWER |
| 9 | ✨ Lumagie | 14 | Eau 50 ET Vent 50 | ERUDITION |

**Total : 126 sorts** (14 par école, répartis en T1-T4 + Ultime)

---

## 2. Architecture du mod (refonte)

```
src/main/java/tong/sihriya/
├── Sihriya.java                     # @Mod — setup STAT Mod + Epic Fight
│
├── core/
│   ├── ManaManager.java             # Mana piloté par MANA_POOL (STAT Mod)
│   ├── ManaProvider.java            # Capability mana
│   ├── SchoolProgression.java       # Niveaux écoles, sorts appris
│   ├── SchoolProgressionProvider.java
│   ├── CapabilityHandler.java       # Enregistrement capabilities
│   ├── TierUnlockHandler.java       # ★ NOUVEAU : paliers 25/50/75/100
│   ├── PlayerLoginHandler.java      # Init école via stat STAT Mod la plus haute
│   ├── SpellCastHandler.java        # Exécution sorts (trigger : touches 1-6)
│   └── MeditationHandler.java       # Sneak/V → méditation animée Epic Fight
│
├── data/
│   ├── SchoolRegistry.java          # Modèle école (data-driven JSON)
│   ├── SpellRegistry.java           # Modèle sort (data-driven JSON)
│   └── DataLoader.java              # Charge JSON + fallback hardcodé
│
├── network/
│   ├── NetworkHandler.java          # Canal Forge SimpleChannel
│   ├── ManaSyncPacket.java
│   ├── SchoolSyncPacket.java
│   └── SchoolCastPacket.java        # ★ NOUVEAU : packet action touche école
│
├── client/
│   ├── ClientManaData.java          # Cache mana client
│   ├── ClientSchoolData.java        # Cache écoles client
│   ├── KeyBindings.java             # ★ REFONTE : touches 1-6 écoles + V méditation
│   ├── ClientSetup.java             # Setup clés + overlays
│   ├── SchoolKeyHandler.java        # ★ NOUVEAU : handler touches 1-6
│   └── gui/
│       └── ManaOverlay.java         # HUD mana (max piloté par MANA_POOL)
│
├── animation/
│   ├── SpellAnimationManager.java   # ★ NOUVEAU : registre animations Epic Fight
│   └── SpellAnimation.java          # ★ NOUVEAU : data class anim par sort
│
├── projectile/
│   ├── SpellProjectile.java         # ★ NOUVEAU : entité projectile magique
│   └── SpellProjectileRenderer.java # ★ NOUVEAU : renderer projectile
│
└── integration/
    ├── STATModIntegration.java      # ★ NOUVEAU : bridge complet STAT Mod
    ├── EpicFightIntegration.java     # ★ NOUVEAU : bridge animations/combat Epic Fight
    └── SihriyaPerks.java            # ★ NOUVEAU : 15 perks magiques STAT Mod
```

---

## 3. Data-driven (spells.json étendu)

```json
{
  "id": "fire.fireball",
  "school": "fire",
  "tier": 1,
  "manaCost": 15,
  "cooldown": 30,
  "type": "PROJECTILE",
  "animation": "sihriya:cast_fireball",
  "animation_time": 15,
  "particle": "flame",
  "cast_time": 10,
  "effects": [
    {"type": "damage", "baseValue": 12.0, "scaling": 0.15, "scalingStat": "FIRE_AFFINITY", "duration": 0},
    {"type": "burn", "baseValue": 3.0, "scaling": 0.05, "scalingStat": "FIRE_AFFINITY", "duration": 100}
  ]
}
```

Nouveaux champs :
- `animation` → ResourceLocation animation Epic Fight
- `animation_time` → durée animation en ticks
- `particle` → type de particule
- `cast_time` → temps d'incantation en ticks (réduit par CASTING_SPEED)
- `effects[].scalingStat` → quelle stat STAT Mod scale cet effet

---

## 4. Mapping STAT Mod → Sihriya

| École | Stat primaire | Scaling | Stat secondaire | Effet |
|-------|---------------|---------|-----------------|-------|
| Feu | FIRE_AFFINITY | +0.5%/niv | CASTING_SPEED | -vitesse incantation |
| Eau | WATER_AFFINITY | +0.5%/niv | WILLPOWER | -lockout mana |
| Vent | AIR_AFFINITY | +0.3%/niv | AGILITY | +vitesse déplacement |
| Terre | EARTH_AFFINITY | +0.3%/niv | PHYSICAL_RESISTANCE | +résistance physique |
| Foudre | ARCANE_POWER | +0.3%/niv | PRECISION | +crit chance |
| Glace | WATER_AFFINITY | +0.5%/niv | MAGIC_RESISTANCE | -dégâts magiques subis |
| Lave | FIRE+EARTH | (moyenne) | PHYSICAL_ENDURANCE | +PV max |
| Nécromancie | ARCANE_POWER | +0.3%/niv | WILLPOWER | -fatigue/corruption |
| Lumagie | ERUDITION | +0.5%/niv | MANA_POOL | +mana max |

**Mana :** `maxMana = 50 + StatCalculator.getManaBonus(MANA_POOL.level)`
**Incantation :** `castTime = baseCastTime * (1 - CASTING_SPEED.level * 0.003)`
**XP :** chaque cast → `ActionXpHelper.awardXp(player, statIndex, XpTier.COMMON)`

---

## 5. Contrôle (Touches)

| Touche | Action |
|--------|--------|
| **1** | Lancer sort Feu |
| **2** | Lancer sort Eau |
| **3** | Lancer sort Vent |
| **4** | Lancer sort Terre |
| **5** | Lancer sort Foudre |
| **6** | Lancer sort Glace |
| **G** (maintenu) | Roue secondaire pour Nécromancie/Lumagie/Lave |
| **V** | Méditation (regen mana + animation Epic Fight) |

Si l'école n'est pas débloquée, la touche est inactive. La touche G ouvre une petite roue pour les 3 écoles avancées (Nécromancie, Lumagie, Lave).

---

## 6. Paliers de déblocage (TierUnlockHandler)

| Niveau école | Déblocage | Bonus STAT Mod |
|-------------|-----------|----------------|
| 1 (départ) | 2 sorts T1 aléatoires | XP stat primaire |
| 25 | Tous les T1 + accès T2 | Perk magique T1 |
| 50 | Accès T3 + école avancée conditionnelle | +5 niveaux stat primaire |
| 75 | Accès T4 | Perk magique T2 |
| 100 | Sort ultime | Perk ultime + 10 niveaux stat |

**Écoles avancées déblocables :**
- Foudre : Feu ≥ 50 OU Vent ≥ 50
- Glace : Eau ≥ 50
- Lave : Feu ≥ 50 ET Terre ≥ 50
- Nécromancie : Terre ≥ 50 ET Foudre ≥ 50
- Lumagie : Eau ≥ 50 ET Vent ≥ 50

---

## 7. Perks magiques (SihriyaPerks)

Les 15 perks magiques (3 par stat élémentaire) utilisent l'infrastructure STAT Mod :

**FIRE_AFFINITY :** Combustion (20) → Inferno (50) → Pyromania (80)
**WATER_AFFINITY :** Geyser (20) → Tourbillon (50) → Tsunami (80)
**AIR_AFFINITY :** Rafale (20) → Tempête (50) → Ouragan (80)
**EARTH_AFFINITY :** Sismique (20) → Rocher (50) → Cataclysme (80)
**ARCANE_POWER :** Foudre (20) → Tempête (50) → Cataclysme (80)

---

## 8. Prochaines étapes (ordre d'implémentation)

1. **Dépendances** — build.gradle + mods.toml (Epic Fight + STAT Mod obligatoires)
2. **STATModIntegration** — bridge complet au lieu de la réflexion actuelle
3. **TierUnlockHandler** — paliers de déblocage
4. **Mana refonte** — piloté par MANA_POOL
5. **KeyBindings + SchoolKeyHandler** — touches 1-6
6. **SpellCastHandler** — nouveau trigger + scaling stats
7. **EpicFightIntegration + animations** — système d'animations
8. **SihriyaPerks** — 15 perks magiques
9. **SpellProjectile** — projectiles magiques
10. **Nettoyage** — supprimer RightClickCastHandler, SpellWheelScreen, etc.
11. **Data** — générer les 126 sorts en JSON
