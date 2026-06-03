# Plan UI - Sihriya

Objectif : rendre l'interface de Sihriya lisible en combat, utile hors combat, et coherente avec le systeme de magie existant. L'UI doit rester rapide : touches directes pour caster, roue pour choisir une ecole, grimoire pour consulter la progression.

## Etat actuel

### Assets et VFX termines
- [x] Icones des 252 sorts regenerees via scripts.
- [x] Atlas `spell_icons.png` et mapping `spell_icon_map.json`.
- [x] 36 textures de cercles magiques : 9 ecoles x 4 layers.
- [x] 11 textures de particules : 9 couleurs ecole + variantes generiques.
- [x] Scripts utiles : `tools/gen_icons_gameicons.py`, `tools/gen_magic_circles.py`, `tools/gen_particles.py`.

### Cercles magiques termines
- [x] 4 layers par ecole avec rotations independantes.
- [x] `MagicCircleAnimation.java` : rotation, vitesse et sens par layer.
- [x] `MagicCircleRenderer.java` : rendu des 4 quads superposes.
- [x] `MagicCircleEntity.java` : particules de perimetre, orbitales, ascension et sparkles.

### HUD et roue deja presents
- [x] `SpellWheelScreen.java` : roue de 9 ecoles ouverte avec R.
- [x] `ManaOverlay.java` : barre de mana avec etat normal/verrouille.
- [x] `ClientSetup.java` : enregistrement des overlays et touches.
- [x] `SchoolKeyHandler.java` : touches 1-6, Shift+1-3, roue R.

## Principes UI

- L'UI de combat doit occuper peu d'espace et ne jamais masquer le centre de l'ecran hors action volontaire du joueur.
- Les couleurs d'ecoles doivent etre constantes entre HUD, roue, grimoire, particules et cercles magiques.
- Les informations permanentes doivent etre minimales : mana, ecole active, cooldown important.
- Le grimoire doit etre l'ecran de reference : details, progression, sorts connus, sorts verrouilles.
- Toute action client doit rester validee serveur : l'UI affiche, le serveur decide si le cast/deblocage est possible.

## Priorite 1 - Stabiliser l'UI de combat

### 1. Roue des ecoles
- [x] Bloquer le cast si l'ecole est verrouillee cote client, tout en gardant la validation serveur.
- [x] Afficher un etat clair pour les ecoles verrouillees : icone desaturee, cadenas, niveau requis.
- [x] Afficher le meilleur sort qui sera lance pour l'ecole survolee : nom, tier, mana, cooldown.
- [x] Corriger les libelles : `Necromancie`, `Lumiere` doivent passer par `lang` ou par les donnees d'ecole, pas etre hardcodes.
- [x] Ajouter une annulation explicite : relacher R au centre ne cast rien.

Critere d'acceptation :
- R maintenu ouvre la roue.
- Souris sur une ecole debloquee puis relache R envoie un seul `SchoolCastPacket`.
- Souris au centre ou ecole verrouillee ne caste rien.
- Les 9 ecoles ont une couleur, une icone et un texte coherents.

### 2. HUD mana
- [x] Ajouter une animation legere sur variation de mana : perte, regen, verrouillage.
- [x] Eviter les libelles bruts d'id (`fire`, `water`) : utiliser les noms traduits.
- [x] Afficher le verrouillage mana avec une jauge ou timer compact.
- [ ] Verifier la position avec HUD vanilla : pas de chevauchement avec faim, armure, effets, chat.

Critere d'acceptation :
- Le HUD reste lisible en 854x480, 1920x1080 et GUI scale auto.
- Mana normal, mana faible et mana verrouille sont distinguables sans lire le texte.

### 3. Cooldown de combat
- [x] Creer `ActiveSpellHud.java`.
- [x] Afficher le sort recemment lance ou le prochain sort de l'ecole active.
- [x] Ajouter cooldown visuel, cout mana et etat pret.
- [ ] Masquer l'overlay si aucun sort pertinent n'est disponible.
- [x] Remplacer le cooldown optimiste client par une confirmation serveur quand un packet dedie existe.

Critere d'acceptation :
- Apres un cast, le joueur voit quel sort est en cooldown et combien de temps il reste.

## Priorite 2 - Grimoire

### 4. `GrimoireScreen.java`
- [x] Ouvrir avec G.
- [x] Onglets par ecole avec couleur et statut : debloquee, verrouillee, niveau.
- [x] Liste des sorts par tier.
- [x] Pour chaque sort : icone, nom, type, cout mana, cooldown, statut connu/verrouille.
- [x] Panneau detail : effets, scaling, condition de deblocage.
- [x] Ajouter descriptions lisibles avec fallback genere depuis les effets.
- [ ] Ajouter descriptions manuscrites sort par sort quand les traductions existent.
- [x] Recherche ou filtre simple par ecole/tier si la liste devient trop dense.
- [x] Ajouter une barre de progression d'ecole 0-100.

Critere d'acceptation :
- Le joueur peut comprendre quoi monter pour debloquer le prochain tier ou la prochaine ecole.
- Le grimoire fonctionne sans caster et sans modifier l'etat serveur.

### 5. Donnees necessaires au grimoire
- [x] Exposer cote client les niveaux d'ecoles, sorts connus et sorts disponibles.
- [x] Ajouter ou verifier les traductions `lang` pour ecoles, sorts et types.
- [x] Centraliser les couleurs et noms d'ecoles pour eviter les duplications entre HUD/roue/grimoire.

Critere d'acceptation :
- Pas de noms hardcodes dans les ecrans sauf fallback technique.

## Priorite 3 - Feedback joueur

### 6. Notifications
- [x] Notification quand une ecole est debloquee.
- [ ] Notification quand un tier est debloque.
- [x] Notification quand un sort est appris.
- [x] Message discret quand un cast echoue : mana insuffisant, mana bloque, ecole verrouillee, aucun sort.
- [x] Ajouter une confirmation serveur pour les echecs de cooldown exacts.

Critere d'acceptation :
- Les echecs de cast sont comprehensibles sans regarder les logs.

### 7. Sons UI et cast
- [x] Son court a l'ouverture/fermeture de la roue.
- [x] Son de selection dans les ecrans UI.
- [x] Son de confirmation/echec de cast via retour serveur.
- [x] Toggle sons UI dans les options client.
- [ ] Son par ecole au cast, si disponible.
- [x] Volume configurable via les categories Minecraft.

## Priorite 4 - Configuration et polish

### 8. Ecran de configuration
- [x] Decider si YACL vaut la dependance. Premiere version sans dependance externe.
- [x] Ajouter un ecran d'options accessible depuis le grimoire.
- [x] Options minimum : HUD mana, HUD sort actif, notifications, HUD compact, mouvements reduits, sons UI, couleurs alternatives.
- [x] Appliquer les options sans casser le comportement serveur.
- [x] Ajouter une persistance Forge config pour conserver les options entre sessions.

### 9. Accessibilite et lisibilite
- [x] Mode couleurs alternatives pour ecoles proches : foudre/lumamancie, eau/glace.
- [ ] Textes traduits FR/EN.
- [ ] Aucun texte ne doit sortir de son conteneur en petite resolution.
- [ ] Contraste suffisant sur overlay sombre et HUD.

## Hors scope UI, mais bloqueurs possibles

- [ ] `SpellCastHandler.java` : effets `summon` et `wall`.
- [ ] `SihriyaPerks.java` : implementation des perks.
- [ ] `EpicFightIntegration.java` : remplacer les stubs par l'API reelle quand l'integration est prete.
- [ ] Equilibrage mana/cooldowns apres tests en jeu.

## Ordre d'implementation recommande

1. Stabiliser `SpellWheelScreen.java` et les donnees client necessaires.
2. Ameliorer `ManaOverlay.java`.
3. Ajouter `ActiveSpellHud.java`.
4. Implementer `GrimoireScreen.java`.
5. Ajouter notifications et messages d'echec.
6. Ajouter options config et polish.
7. Faire une passe de tests en jeu et ajuster.

## Tests manuels minimum

- Lancer `./gradlew build`.
- Tester en jeu avec une ecole debloquee et une ecole verrouillee.
- Tester R : selection, annulation au centre, relachement, spam rapide.
- Tester 1-6 et Shift+1-3.
- Tester mana normal, mana insuffisant, cooldown actif, mana verrouille.
- Tester resolutions basse et haute avec plusieurs GUI scales.
