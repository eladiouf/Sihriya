# Sihriya → Addon Epic Fight — Design Document

## Objectif
Transformer Sihriya en véritable addon d'Epic Fight : les animations et le système de combat général sont pris en charge par Epic Fight.

## Décisions prises

### Dépendance
- Epic Fight devient **obligatoire** (hard dependency)
- `mandatory=true` dans `mods.toml`

### Contrôle des sorts
- 1 touche par école (6 touches max)
- Touches 1 à 6 : Feu, Eau, Vent, Terre, Foudre, Glace
- Chaque touche lance le meilleur sort connu de l'école correspondante
- Pas d'intégration dans le menu des skills Epic Fight (touches dédiées)
- Le clic droit pour lancer disparaît → remplacé par les touches d'école

### Animations
- **Animations uniques par sort** (pas d'animation générique)
- Chaque sort a sa propre animation Epic Fight
- Utilisation du système de calques d'Epic Fight (`Layer.Priority`)
- Les sorts lancent des projectiles (système Epic Fight / entités personnalisées)
- Effets de particules par école

### Mana
- **Les deux** : régénération passive lente + méditation (touche V ou sneak) pour regen rapide
- La méditation joue une animation Epic Fight (pose assise/méditation)
- Système de mana Sihriya conservé tel quel

### Combat
- Les attaques physiques Epic Fight conservent les effets passifs élémentaires
- Le système de combat Epic Fight (dash, esquive, posture) reste intact
- Sihriya ajoute la couche magique par-dessus

### Systèmes conservés (inchangés)
- ManaManager, ManaProvider, ManaOverlay
- SchoolProgression, SchoolProgressionProvider
- SpellRegistry, SchoolRegistry, DataLoader
- Intégration StatMod (SihriyaAPI)

### Systèmes à modifier/supprimer
- `RightClickCastHandler.java` → supprimer (remplacé par touches écoles)
- `SpellWheelScreen.java` → supprimer ou mettre en second plan
- `SpellWheelInputHandler.java` → modifier pour les nouvelles touches
- `KeyBindings.java` → ajouter 6 touches d'école
- `SpellCastHandler.java` → conserver la logique, changer le déclencheur
- `EpicFightEffects.java` → améliorer (passif élémentaire, plus riche)
- `ClientSetup.java` → ajouter les nouvelles touches
- `PlayerLoginHandler.java` → adapter (plus de starting school random ?)

### Nouveaux fichiers à créer
- `integration/EpicFightAnimationManager.java` — gestion des animations par sort
- `integration/SpellSkill.java` — skills Epic Fight customs pour chaque sort
- `integration/EpicFightProjectile.java` — projectiles avec animations Epic Fight
- `client/SchoolKeyHandler.java` — gestionnaire des touches 1-6
- `assets/sihriya/animations/` — animations Blender pour chaque sort

## Prochaines étapes
1. Finaliser le design détaillé (comportement de chaque sort)
2. Créer le plan d'implémentation
3. Implémenter les changements
