# Plan UI — Sihriya

## Fait (commit en cours)

### Icônes & Assets
- [x] Icônes 252 sorts régénérées (Pillow + svg.path)
- [x] 36 textures de cercles magiques (9 écoles × 4 layers, SGA + symboles + sigils)
- [x] 11 textures de particules (9 couleurs école + spark générique) retravaillées
- [x] Scripts de génération : `tools/gen_icons_gameicons.py`, `tools/gen_magic_circles.py`, `tools/gen_particles.py`

### Cercles Magiques (refonte complète)
- [x] 4 layers par école animés indépendamment :
  - Layer 0 : SGA + écriture mystique → ↻ clockwise lent
  - Layer 1 : symboles astrologiques → ↺ counter-clockwise moyen
  - Layer 2 : géométrie sacrée → ↻ clockwise rapide
  - Layer 3 : sigil central → ↻ très lent
- [x] `MagicCircleAnimation.java` — 4 rotations indépendantes, vitesses/sens opposés
- [x] `MagicCircleRenderer.java` — 4 quads superposés avec rotation séparée
- [x] `MagicCircleEntity.java` — particules améliorées (14 périmètre + 6 orbitales + ascension + sparkles)

### Particules
- [x] Textures 64×64 (halo gaussien + étoile 4 branches + cœur brillant)
- [x] `SchoolGlowParticle.java` — pulsation de taille, fade in/out, flottement sinusoïdal
- [x] JSONs mis à jour pour textures par école

### UI / HUD
- [x] `SpellWheelScreen.java` — roue 9 écoles (touche R maintenue, souris = direction, relâcher = cast)
- [x] `ManaOverlay.java` — barre de mana ornée avec dégradé
- [x] `ClientSetup.java` — enregistrement SPELL_WHEEL
- [x] `SchoolKeyHandler.java` — handler touche R pour ouvrir la roue

---

## À faire

### UI
- [ ] `GrimoireScreen.java` — livre de sorts avec onglets par école (touche G)
  - Fond parchemin, onglets colorés par école
  - Liste des sorts avec icônes, coût mana, cooldown
  - Indicateur de déblocage (niveau requis)
- [ ] `ActiveSpellHud.java` — overlay sort actif + cooldown circulaire
- [ ] Toast notifications — popup quand une école/tier est débloqué
- [ ] Écran config mod avec YACL (options VFX, particles on/off, etc.)

### Gameplay
- [ ] Effets `summon` et `wall` dans `SpellCastHandler.java` (actuellement TODO)
- [ ] Implémentation des 15 perks Sihriya (`SihriyaPerks.java`)
- [ ] Animations Epic Fight réelles (API stub actuellement dans `EpicFightIntegration.java`)

### Polish
- [ ] Sons de cast par école
- [ ] Équilibrage mana/cooldowns
- [ ] Traductions complètes fr/en
- [ ] Tests en jeu

---

## Architecture UI (résumé)

```
Joueur appuie R → SpellWheelScreen s'ouvre
  ├── render() → overlay sombre + 9 icônes en cercle
  ├── tick() → détecte relâchement R → envoie SchoolCastPacket
  └── SpellCastHandler (serveur) → lance le meilleur sort dispo
  
Joueur appuie G → GrimoireScreen (à venir)
  └── Onglets par école, liste des sorts, progression

HUD permanent :
  ├── ManaOverlay (haut-gauche)
  ├── ActiveSpellHud (bas-droite) (à venir)
  └── Notifications toast (à venir)
```
