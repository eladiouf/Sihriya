# Guide d'Animations Sihriya — 252 Sorts

> Ce document décrit en détail chaque animation pour Blender/Blockbench.
> Chaque sort a : **pose de cast**, **mouvement des mains**, **énergie**, **release**, **follow-through**.
> Durées en ticks (20 ticks = 1 seconde).

---

## Conventions

- **CastTime 0** = animation instantanée (pas de chant, geste sec)
- **CastTime 1-10** = cast rapide (1-2 keyframes)
- **CastTime 10-30** = cast moyen (3-5 keyframes)
- **CastTime 30-60** = cast lent/ultime (5-10 keyframes, effet dramatique)
- **PROJECTILE** = bras tendu vers la cible, énergie qui part
- **ZONE** = geste vers le sol ou cercle autour du lanceur
- **BUFF** = geste vers soi-même ou auto-cast
- **SUMMON** = geste vers le sol, invocation
- **ULTIMATE** = animation longue et spectaculaire

---

## 🔥 FEU (28 sorts)

### T1

#### fire.spark (PROJECTILE, cast=0)
**Animation :** Bras droit tendu vers la cible, paume ouverte. Geste sec et rapide — le poignet claque vers l'avant. Flamme naît au bout des doigts et jaillit. Retour immédiat en position neutre.
- **Keyframes :** 2 (neutre → release)
- **Énergie :** Particules feu naissent au poignet au frame 0, jaillissent au frame 1

#### fire.living_torch (BUFF, cast=10)
**Animation :** Le lanceur croise les bras devant sa poitrine, paumes vers le bas. Les bras s'écartent lentement en arc, comme qui dévoile un manteau. Flamme s'allume autour du corps.
- **Keyframes :** 3 (neutre → bras croisés → bras écartés)
- **Énergie :** Aura de feu doux qui pulse autour du joueur

#### fire.fireball (PROJECTILE, cast=10)
**Animation :** Bras droit ramené en arrière (comme un lanceur de balle). Main gauche devant pour viser. Bras droit avance en moulinet — la paume s'ouvre au sommet. Boule de feu se forme entre les mains puis jaillit.
- **Keyframes :** 4 (neutre → charge → lance → follow-through)
- **Énergie :** Sphère rouge/orange qui grossit entre les mains, jaillit vers la cible

#### fire.dancing_embers (BUFF, cast=10)
**Animation :** Le lanceur lève les deux bras au-dessus de la tête, paumes face au ciel. Les doigts s'ouvrent et se ferment comme qui attrape quelque chose. Braises dansent autour du corps.
- **Keyframes :** 3 (neutre → bras levés → pulsation)
- **Énergie :** Petites braises orange qui orbitent autour du joueur

#### fire.fire_piston_strike (PROJECTILE, cast=0)
**Animation :** Coup de poing direct avec le poing droit. Le bras tendu, rotation du buste. Flamme éclate au point d'impact.
- **Keyframes :** 2 (neutre → impact)
- **Énergie :** Flash de feu au poing au moment de l'impact

#### fire.brands_mark (PROJECTILE, cast=10)
**Animation :** Bras droit tendu, index pointé vers la cible. Le lanceur trace un symbole brûlant dans l'air avec son doigt. Le symbole jaillit vers la cible.
- **Keyframes :** 4 (neutre → bras tendu → trace symbole → release)
- **Énergie :** Rune de feu qui se forme dans l'air puis vole vers la cible

### T2

#### fire.wall_of_flames (ZONE, cast=20)
**Animation :** Le lanceur s'accroupit, paumes au sol. Les deux mains remontent lentement en arc devant lui, comme qui soulève un rideau. Un mur de flammes jaillit du sol.
- **Keyframes :** 4 (neutre → accroupi → mains au sol → remontée)
- **Énergie :** Mur de flammes qui pousse du sol vers le ciel

#### fire.fiery_breath (ZONE, cast=0)
**Animation :** Le lanceur penche la tête en arrière, gorge exposée, puis projette la tête vers l'avant. Bouche grande ouverte, souffle de feu.
- **Keyframes :** 3 (neutre → tête en arrière → souffle)
- **Énergie :** Cône de flammes sortant de la bouche

#### fire.fire_nova (ZONE, cast=15)
**Animation :** Le lanceur lève le bras droit au ciel, paume ouverte. Rotation lente du buste. Explosion circulaire de feu depuis le lanceur.
- **Keyframes :** 4 (neutre → bras levé → rotation → nova)
- **Énergie :** Onde de choc de feu qui s'étend en cercle

#### fire.blazing_armor (BUFF, cast=20)
**Animation :** Le lanceur passe les mains sur son torse, comme qui enfile une armure. Chaque passage laisse une traînée de flammes. Les épaules s'écarquillent.
- **Keyframes :** 5 (neutre → main droite sur torse → main gauche → épaules → pose)
- **Énergie :** Armure de feu visible qui se forme couche par couche

#### fire.flying_ashes (ZONE, cast=15)
**Animation :** Le lanceur souffle vers l'avant, paume ouverte. Cendres incandescentes volent vers la zone cible.
- **Keyframes :** 3 (neutre → souffle → cendres)
- **Énergie :** Nuage de cendres orange qui obscurcit la zone

#### fire.fire_lance (PROJECTILE, cast=10)
**Animation :** Le lanceur tend le bras droit comme s'il tenait une lance. Le bras avance en ligne droite, rotation du poignet. Lance de feu jaillit.
- **Keyframes :** 3 (neutre → charge → lancer)
- **Énergie :** Longue lance de feu qui traverse l'écran

#### fire.minor_eruption (ZONE, cast=10)
**Animation :** Le lanceur frappe le sol avec le pied droit. Petites gerbes de feu jaillissent autour.
- **Keyframes :** 3 (neutre → frappe → éruption)
- **Énergie :** Geysers de lave miniatures

#### fire.flame_dance (ZONE, cast=10)
**Animation :** Le lanceur fait une pirouette, bras écartés. Flammes dansent autour de lui en spirale.
- **Keyframes :** 4 (neutre → début rotation → pleine rotation → arrêt)
- **Énergie :** Spirale de flammes qui tourne autour du lanceur

### T3

#### fire.comet (PROJECTILE, cast=30)
**Animation :** Le lanceur lève les deux bras au ciel. L'air vibre au-dessus de lui. Les bras descendent lentement en arc vers la cible. Une comète de feu se forme en hauteur puis fonce vers le sol.
- **Keyframes :** 5 (neutre → bras au ciel → charge → release → impact)
- **Énergie :** Boule de feu massive qui descend du ciel

#### fire.infernal_pillar (ZONE, cast=10)
**Animation :** Le lanceur frappe le sol avec les deux poings. Un pilier de flammes jaillit sous la cible.
- **Keyframes :** 3 (neutre → frappe → pilier)
- **Énergie :** Colonne de feu qui monte du sol

#### fire.ash_storm (ZONE, cast=30)
**Animation :** Le lanceur lève les bras en croix. Tourbillon de cendres se forme autour de lui puis se propage.
- **Keyframes :** 4 (neutre → bras en croix → tourbillon → expansion)
- **Énergie :** Tempête de cendres qui obscurcit tout

#### fire.fusion (BUFF, cast=20)
**Animation :** Le lanceur serre les poings contre sa poitrine. Le corps entier s'enflamme doucement. Les yeux brillent. Expansion d'énergie.
- **Keyframes :** 4 (neutre → poings serrés → embrasement → pose héroïque)
- **Énergie :** Aura de feu intense, yeux lumineux

#### fire.furnace (ZONE, cast=20)
**Animation :** Le lanceur trace un cercle devant lui avec les deux mains. Le cercle s'enflamme et projette de la chaleur.
- **Keyframes :** 4 (neutre → trace cercle → ignition → chaleur)
- **Énergie :** Fournaise ouverte qui projette de la chaleur

#### fire.dragons_breath (ZONE, cast=20)
**Animation :** Le lanceur prend une grande inspiration, poitrine gonflée. Puis projette un souffle de feu en arc devant lui. Tête suit le mouvement.
- **Keyframes :** 4 (neutre → inspiration → souffle → fin)
- **Énergie :** Large cône de feu doré

#### fire.fire_wheel (PROJECTILE, cast=15)
**Animation :** Le lanceur lance une roue de feu avec un mouvement de frisbee. Bras droit part en arrière puis avance en arc.
- **Keyframes :** 4 (neutre → charge → lancer → follow-through)
- **Énergie :** Roue de feu qui tourne en volant

#### fire.furnace_heart (BUFF, cast=20)
**Animation :** Le lanceur pose les deux mains sur son cœur. Lumière rouge intense brille à travers les doigts. Les mains s'écartent — un cœur de fournaise palpite.
- **Keyframes :** 4 (neutre → mains sur cœur → pulse → expansion)
- **Énergie :** Cœur de magma visible à travers la poitrine

### T4

#### fire.summoned_phoenix (SUMMON, cast=40)
**Animation :** Le lanceur lève les bras au ciel, paumes ouvertes. Lent cercle des bras. Une colonne de feu descend du ciel. Le phénix en émerge, ailes déployées.
- **Keyframes :** 6 (neutre → bras au ciel → cercle → colonne → phénix émerge → ailes)
- **Énergie :** Colonne de feu dorée, phénix enflammé

#### fire.meteor_shower (ZONE, cast=40)
**Animation :** Le lanceur lève les deux bras, paumes face au ciel. Les doigts s'agrippent comme qui tire des fils. Des météores tombent du ciel.
- **Keyframes :** 5 (neutre → bras levés → agrippement → météores → impacts)
- **Énergie :** Pluie de météores enflammés

#### fire.piercing_meteor (PROJECTILE, cast=30)
**Animation :** Le lanceur pointe le doigt vers la cible. Un météore se forme au-dessus de sa tête, puis fonce en ligne droite vers la cible.
- **Keyframes :** 4 (neutre → pointe → météore → impact)
- **Énergie :** Météore qui traverse l'air avec une traînée de feu

#### fire.infinite_brazier (ZONE, cast=40)
**Animation :** Le lanceur s'agenouille, mains au sol. Un brasier permanent s'allume sous lui et s'étend.
- **Keyframes :** 5 (neutre → agenouille → mains au sol → ignition → brasier)
- **Énergie :** Brasier permanent qui brûle le sol

### T5 — Ultime

#### fire.blazing_sun (ULTIMATE, cast=60)
**Animation :** Le lanceur s'élève lentement du sol. Les bras s'écartent en croix. Le corps s'embrase. Un soleil de feu se forme au-dessus de lui. Les bras se referment vers le bas — le soleil explose en onde de choc.
- **Keyframes :** 8 (neutre → élévation → bras en croix → embrasement → soleil → charge → explosion → descente)
- **Énergie :** Soleil de feu miniature au-dessus du lanceur, onde de choc de flammes

#### fire.apocalypse_ignited (ULTIMATE, cast=60)
**Animation :** Le lanceur lève un poing au ciel. Le ciel devient rouge. Des fissures de lave apparaissent au sol. Le poing descend — apocalypse de feu.
- **Keyframes :** 8 (neutre → poing au ciel → ciel rouge → fissures → charge → descente → explosion → calme)
- **Énergie :** Ciel enflammé, fissures de lave, pluie de feu

---

## 💧 EAU (28 sorts)

### T1

#### water.water_jet (PROJECTILE, cast=0)
**Animation :** Bras droit tendu, paume ouverte. Geste sec vers l'avant. Jet d'eau comprimé jaillit de la paume.
- **Keyframes :** 2 (neutre → release)
- **Énergie :** Jet d'eau bleu intense, haute pression

#### water.walk_on_water (BUFF, cast=20)
**Animation :** Le lanceur regarde le sol, mains ouvertes vers le bas. Gestes circulaires lents. L'eau sous les pieds se solidifie.
- **Keyframes :** 4 (neutre → regard au sol → gestes → solidification)
- **Énergie :** Eau qui se cristallise sous les pieds

#### water.air_bubble (BUFF, cast=10)
**Animation :** Le lanceur souffle doucement, une bulle d'air se forme devant sa bouche et l'entoure.
- **Keyframes :** 3 (neutre → souffle → bulle)
- **Énergie :** Bulle translucide autour de la tête

#### water.sharp_drop (PROJECTILE, cast=0)
**Animation :** Mouvement de fouet avec le bras droit. L'eau se condense en lame et jaillit.
- **Keyframes :** 2 (neutre → fouet)
- **Énergie :** Lame d'eau tranchante

#### water.fog (ZONE, cast=15)
**Animation :** Le lanceur souffle doucement, paumes vers l'avant. Brume se forme et s'étend.
- **Keyframes :** 3 (neutre → souffle → brume)
- **Énergie :** Brouillard dense qui s'étend

#### water.holy_water (PROJECTILE, cast=5)
**Animation :** Le lanceur bénit l'eau entre ses mains (geste de prière), puis la projette.
- **Keyframes :** 3 (neutre → bénédiction → lancer)
- **Énergie :** Eau lumineuse qui vole vers la cible

### T2

#### water.liquid_shield (BUFF, cast=15)
**Animation :** Le lanceur trace un cercle devant lui. L'eau se condense en bouclier liquide.
- **Keyframes :** 3 (neutre → cercle → bouclier)
- **Énergie :** Bouclier d'eau qui pulse

#### water.crushing_wave (ZONE, cast=20)
**Animation :** Le lanceur lève les bras en arrière, puis les projette vers l'avant. Une vague massive déferle.
- **Keyframes :** 4 (neutre → charge → projection → vague)
- **Énergie :** Vague bleue qui écrase tout

#### water.water_prison (ZONE, cast=10)
**Animation :** Le lanceur ferme le poing. L'eau entoure la cible dans une sphère.
- **Keyframes :** 3 (neutre → poing fermé → prison)
- **Énergie :** Sphère d'eau emprisonnante

#### water.healing_rain (BUFF, cast=30)
**Animation :** Le lanceur lève les bras au ciel, paumes ouvertes. Pluie douce tombe sur lui.
- **Keyframes :** 4 (neutre → bras au ciel → prière → pluie)
- **Énergie :** Gouttes de pluie lumineuse

#### water.ascending_current (BUFF, cast=5)
**Animation :** Mouvement ascendant rapide avec les deux mains. Courant d'eau pousse vers le haut.
- **Keyframes :** 2 (neutre → ascension)
- **Énergie :** Colonne d'eau ascendante

#### water.water_lace (PROJECTILE, cast=10)
**Animation :** Le lanceur lance un fil d'eau qui s'enroule autour de la cible et tire.
- **Keyframes :** 3 (neutre → lancer → traction)
- **Énergie :** Fil d'eau qui se resserre

#### water.aquatic_purge (BUFF, cast=10)
**Animation :** Le lanceur passe les mains sur son corps de haut en bas. L'eau purifie.
- **Keyframes :** 3 (neutre → passage → purification)
- **Énergie :** Eau cristalline qui nettoie

#### water.pressure_wave (ZONE, cast=5)
**Animation :** Clap sec avec les deux mains. Onde de pression d'eau.
- **Keyframes :** 2 (neutre → clap)
- **Énergie :** Onde de choc bleue

### T3

#### water.maelstrom (ZONE, cast=20)
**Animation :** Le lanceur tourne sur lui-même, bras écartés. Un tourbillon d'eau se forme.
- **Keyframes :** 4 (neutre → rotation → tourbillon → expansion)
- **Énergie :** Maelström qui aspire tout

#### water.deluge (ZONE, cast=40)
**Animation :** Le lanceur lève les bras au ciel. Pluie torrentielle s'abat sur la zone.
- **Keyframes :** 5 (neutre → bras au ciel → nuages → pluie → déluge)
- **Énergie :** Pluie battante, inondation

#### water.geysers (ZONE, cast=6)
**Animation :** Le lanceur frappe le sol. Geysers jaillissent sous les ennemis.
- **Keyframes :** 2 (neutre → frappe)
- **Énergie :** Colonnes d'eau qui jaillissent du sol

#### water.liquid_form (BUFF, cast=20)
**Animation :** Le lanceur devient translucide. Son corps se liquéfie partiellement.
- **Keyframes :** 4 (neutre → début liquéfaction → pleine transformation → pose)
- **Énergie :** Corps d'eau translucide

#### water.abyssal_well (ZONE, cast=20)
**Animation :** Le lanceur pointe le sol. Un puits sans fond s'ouvre sous la cible.
- **Keyframes :** 3 (neutre → pointe → puits)
- **Énergie :** Trou sombre d'eau profonde

#### water.water_mirror (BUFF, cast=15)
**Animation :** Le lanceur trace un miroir d'eau devant lui. Reflets dansent.
- **Keyframes :** 3 (neutre → trace → miroir)
- **Énergie :** Surface d'eau réfléchissante

#### water.liquid_blade (BUFF, cast=20)
**Animation :** Le lanceur passe la main le long de son arme. L'eau se solidifie en lame.
- **Keyframes :** 3 (neutre → passage → lame)
- **Énergie :** Lame d'eau sur l'arme

#### water.local_flood (ZONE, cast=30)
**Animation :** Le lanceur accroupi, mains au sol. L'eau monte lentement dans la zone.
- **Keyframes :** 4 (neutre → accroupi → mains au sol → inondation)
- **Énergie :** Eau qui monte progressivement

### T4

#### water.tidal_wave (ZONE, cast=40)
**Animation :** Le lanceur lève les bras très haut. Une vague gigantesque se forme derrière lui. Les bras descendent — la vague déferle.
- **Keyframes :** 6 (neutre → bras au ciel → vague se forme → charge → descente → déferlement)
- **Énergie :** Vague colossale

#### water.ice_armor (BUFF, cast=30)
**Animation :** Le lanceur passe les mains sur son corps. L'eau se solidifie en armure de glace.
- **Keyframes :** 5 (neutre → passage → gel → armure → pose)
- **Énergie :** Armure de glace cristalline

#### water.black_tide (ZONE, cast=40)
**Animation :** Le lanceur étend les bras. Marée noire s'étend sur le sol.
- **Keyframes :** 4 (neutre → bras étendus → marée → expansion)
- **Énergie :** Eau sombre et toxique

#### water.evaporation (ZONE, cast=20)
**Animation :** Le lanceur lève les mains. L'eau de la zone s'évapore violemment.
- **Keyframes :** 3 (neutre → mains levées → évaporation)
- **Énergie :** Vapeur brûlante

### T5 — Ultime

#### water.eye_of_deluge (ULTIMATE, cast=60)
**Animation :** Le lanceur s'élève. Un œil de tempête aquatique se forme autour de lui. Pluie, vent, vagues — chaos aquatique.
- **Keyframes :** 8 (neutre → élévation → œil se forme → rotation → expansion → chaos → contraction → calme)
- **Énergie :** Œil de tempête d'eau géant

#### water.primeval_deluge (ULTIMATE, cast=60)
**Animation :** Le lanceur ouvre les bras grand. L'eau monte de partout — déluge primordial.
- **Keyframes :** 8 (neutre → bras ouverts → eau monte → déluge → immersion → émergence → récession → calme)
- **Énergie :** Inondation totale de la zone

---

## 🌪️ VENT (28 sorts)

### T1

#### wind.gust (PROJECTILE, cast=0)
**Animation :** Mouvement de chasse-mouche avec la main droite. Rafale de vent jaillit.
- **Keyframes :** 2 (neutre → chasse)
- **Énergie :** Rafale visible (lignes de vent)

#### wind.wind_step (BUFF, cast=0)
**Animation :** Pas latéral rapide. Le corps se déplace instantanément avec une traînée de vent.
- **Keyframes :** 2 (neutre → dash)
- **Énergie :** Traînée de vent bleu clair

#### wind.air_shield (BUFF, cast=10)
**Animation :** Le lanceur lève la main, doigts écartés. Tourbillon d'air se forme en bouclier.
- **Keyframes :** 3 (neutre → main levée → bouclier)
- **Énergie :** Bouclier de vent visible

#### wind.whisper (BUFF, cast=0)
**Animation :** Le lanceur porte un doigt à ses lèvres. Silence.
- **Keyframes :** 2 (neutre → doigt aux lèvres)
- **Énergie :** Subtile ondulation d'air

#### wind.air_scissors (PROJECTILE, cast=5)
**Animation :** Mouvement de ciseaux avec index et majeur. Lames d'air jaillissent.
- **Keyframes :** 2 (neutre → ciseaux)
- **Énergie :** Deux lames d'air croisées

#### wind.protective_breath (BUFF, cast=5)
**Animation :** Le lanceur souffle doucement autour de lui. Mur de vent protecteur.
- **Keyframes :** 2 (neutre → souffle)
- **Énergie :** Tourbillon protecteur

### T2

#### wind.whistle (PROJECTILE, cast=5)
**Animation :** Le lanceur siffle — doigts dans la bouche ou lèvres pincées. Projectile sonique.
- **Keyframes :** 2 (neutre → sifflement)
- **Énergie :** Onde sonique visible

#### wind.air_blade (PROJECTILE, cast=10)
**Animation :** Le lanceur fait un mouvement de coupe avec le plat de la main. Lame d'air comprimé.
- **Keyframes :** 3 (neutre → charge → coupe)
- **Énergie :** Lame d'air tranchante et invisible

#### wind.brief_flight (BUFF, cast=20)
**Animation :** Le lanceur s'accroupit puis bondit. Des courants d'air le portent.
- **Keyframes :** 4 (neutre → accroupi → bond → vol)
- **Énergie :** Courants d'air sous les pieds

#### wind.whirlwind (ZONE, cast=10)
**Animation :** Le lanceur tourne sur lui-même. Tornade miniature se forme autour de lui.
- **Keyframes :** 3 (neutre → rotation → tornade)
- **Énergie :** Tornade de vent

#### wind.minor_trombe (ZONE, cast=15)
**Animation :** Le lanceur pointe le sol. Trombe d'eau se forme.
- **Keyframes :** 3 (neutre → pointe → trombe)
- **Énergie :** Trombe d'eau/vent

#### wind.wind_feather (BUFF, cast=15)
**Animation :** Le lanceur passe la main sur ses pieds. Léger comme une plume.
- **Keyframes :** 3 (neutre → passage → légèreté)
- **Énergie :** Plumes de vent sous les pieds

#### wind.enhanced_throw (BUFF, cast=10)
**Animation :** Le lanceur mime un lancer. Vent augmente la puissance.
- **Keyframes :** 3 (neutre → mime → renforcement)
- **Énergie :** Tourbillon autour du bras

#### wind.whip_strike (ZONE, cast=5)
**Animation :** Mouvement de fouet avec le bras. Coup de vent tranchant.
- **Keyframes :** 2 (neutre → fouet)
- **Énergie :** Ligne de vent qui claque

### T3

#### wind.dust_storm (ZONE, cast=30)
**Animation :** Le lanceur lève les bras. Tempête de poussière se forme.
- **Keyframes :** 4 (neutre → bras levés → tempête → expansion)
- **Énergie :** Tourbillon de sable et vent

#### wind.wind_speed (BUFF, cast=20)
**Animation :** Le lanceur tape ses pieds au sol. Vent accélérateur.
- **Keyframes :** 3 (neutre → tape → accélération)
- **Énergie :** Lignes de vitesse autour du corps

#### wind.wind_cage (ZONE, cast=10)
**Animation :** Le lanceur ferme le poing. Cage de vent emprisonne la cible.
- **Keyframes :** 3 (neutre → poing → cage)
- **Énergie :** Mur de vent autour de la cible

#### wind.storm_blades (BUFF, cast=20)
**Animation :** Le lanceur fait des moulinets avec les bras. Lames de vent orbitent autour de lui.
- **Keyframes :** 4 (neutre → moulinets → lames → orbite)
- **Énergie :** Lames de vent qui tournent

#### wind.silent_gust (PROJECTILE, cast=5)
**Animation :** Geste discret, presque invisible. Rafale silencieuse.
- **Keyframes :** 2 (neutre → geste)
- **Énergie :** Rafale quasi-invisible

#### wind.wind_circle (ZONE, cast=20)
**Animation :** Le lanceur trace un cercle avec le pied. Mur de vent circulaire.
- **Keyframes :** 3 (neutre → cercle → mur)
- **Énergie :** Cercle de vent protecteur

#### wind.impaling_wind (PROJECTILE, cast=10)
**Animation :** Le lanceur pointe avec deux doigts. Vent qui transperce.
- **Keyframes :** 3 (neutre → pointe → transpercement)
- **Énergie :** Aiguille de vent

#### wind.brutal_ascension (BUFF, cast=10)
**Animation :** Le lanceur bondit violemment. Courants violents le propulsent.
- **Keyframes :** 3 (neutre → bond → ascension)
- **Énergie :** Explosion de vent sous les pieds

### T4

#### wind.hurricane (ZONE, cast=40)
**Animation :** Le lanceur lève les bras. Ouragan se forme autour de lui.
- **Keyframes :** 5 (neutre → bras levés → ouragan → expansion → chaos)
- **Énergie :** Ouragan géant

#### wind.ascension (BUFF, cast=20)
**Animation :** Le lanceur s'élève lentement. Courants d'air le portent.
- **Keyframes :** 4 (neutre → élévation → vol → plané)
- **Énergie :** Plateforme de vent

#### wind.air_vacuum (ZONE, cast=30)
**Animation :** Le lanceur ouvre grand les bras. Vide d'air se forme.
- **Keyframes :** 4 (neutre → bras ouverts → vide → implosion)
- **Énergie :** Zone de vide qui aspire

#### wind.wind_corridor (ZONE, cast=20)
**Animation :** Le lanceur trace un couloir devant lui. Vent accélérateur.
- **Keyframes :** 3 (neutre → trace → couloir)
- **Énergie :** Tunnel de vent

### T5 — Ultime

#### wind.eye_of_storm (ULTIMATE, cast=60)
**Animation :** Le lanceur s'élève au centre d'une tempête. Calme au centre, chaos autour.
- **Keyframes :** 8 (neutre → élévation → tempête se forme → rotation → expansion → chaos → contraction → calme)
- **Énergie :** Œil de tempête géant

#### wind.ether_breath (ULTIMATE, cast=40)
**Animation :** Le lanceur inspire profondément. Vent éthéré se forme. Souffle divin.
- **Keyframes :** 6 (neutre → inspiration → éther → souffle → onde → calme)
- **Énergie :** Souffle éthéré doré

---

## 🪨 TERRE (28 sorts)

### T1

#### earth.rock_strike (PROJECTILE, cast=6)
**Animation :** Le lanceur ramène le bras en arrière, puis frappe vers l'avant. Un rocher se forme au poing et jaillit.
- **Keyframes :** 3 (neutre → charge → frappe)
- **Énergie :** Rocher qui jaillit du poing

#### earth.stone_skin (BUFF, cast=20)
**Animation :** Le lanceur passe les mains sur son corps. La peau se couvre de pierre.
- **Keyframes :** 4 (neutre → passage → pierre → pose)
- **Énergie :** Armure de pierre sur la peau

#### earth.tremor (ZONE, cast=10)
**Animation :** Le lanceur frappe le sol avec le pied. Onde sismique.
- **Keyframes :** 3 (neutre → frappe → onde)
- **Énergie :** Onde qui se propage au sol

#### earth.clay_projectile (PROJECTILE, cast=0)
**Animation :** Mouvement de lancer d'argile. Boule de terre.
- **Keyframes :** 2 (neutre → lancer)
- **Énergie :** Boule de terre compacte

#### earth.earth_sense (BUFF, cast=20)
**Animation :** Le lanceur s'accroupit, mains au sol. Sensation de la terre.
- **Keyframes :** 3 (neutre → accroupi → connexion)
- **Énergie :** Lignes de force dans le sol

#### earth.blinding_dust (PROJECTILE, cast=0)
**Animation :** Mouvement de balayage. Nuage de poussière.
- **Keyframes :** 2 (neutre → balayage)
- **Énergie :** Nuage de poussière

### T2

#### earth.stone_wall (ZONE, cast=10)
**Animation :** Le lanceur lève les mains du sol. Mur de pierre émerge.
- **Keyframes :** 3 (neutre → mains au sol → mur)
- **Énergie :** Mur de pierre qui monte

#### earth.rock_spike (PROJECTILE, cast=6)
**Animation :** Le lanceur pointe le sol. Pointe de roche jaillit vers la cible.
- **Keyframes :** 3 (neutre → pointe → spike)
- **Énergie :** Pointe de roche aiguisée

#### earth.stone_roots (ZONE, cast=10)
**Animation :** Le lanceur ferme le poing. Racines de pierre émergent du sol.
- **Keyframes :** 3 (neutre → poing → racines)
- **Énergie :** Racines pétrifiées

#### earth.absorption (BUFF, cast=20)
**Animation :** Le lanceur s'accroupit, mains au sol. Énergie de la terre le régénère.
- **Keyframes :** 3 (neutre → accroupi → absorption)
- **Énergie :** Lumière verte du sol

#### earth.stone_vise (PROJECTILE, cast=10)
**Animation :** Le lanceur fait un geste de pince. Pierre étreint la cible.
- **Keyframes :** 3 (neutre → pince → étreinte)
- **Énergie :** Pierre qui serre

#### earth.trench (ZONE, cast=15)
**Animation :** Le lanceur trace une ligne au sol. Tranchée s'ouvre.
- **Keyframes :** 3 (neutre → trace → tranchée)
- **Énergie :** Fissure dans le sol

#### earth.sand_armor (BUFF, cast=15)
**Animation :** Le lanceur se couvre de sable. Armure de sable.
- **Keyframes :** 3 (neutre → couverture → armure)
- **Énergie :** Tourbillon de sable

#### earth.shield_strike (PROJECTILE, cast=0)
**Animation :** Coup de bouclier de terre. Frappe directe.
- **Keyframes :** 2 (neutre → frappe)
- **Énergie :** Impact de pierre

### T3

#### earth.fortress (ZONE, cast=10)
**Animation :** Le lanceur lève les bras. Forteresse de pierre émerge.
- **Keyframes :** 4 (neutre → bras levés → forteresse → complète)
- **Énergie :** Murs de pierre qui montent

#### earth.avalanche (ZONE, cast=20)
**Animation :** Le lanceur pointe la montagne (ou le ciel). Avalanche de rochers.
- **Keyframes :** 4 (neutre → pointe → rochers → avalanche)
- **Énergie :** Pluie de rochers

#### earth.fissure (ZONE, cast=20)
**Animation :** Le lanceur frappe le sol avec le poing. Fissure profonde s'ouvre.
- **Keyframes :** 3 (neutre → frappe → fissure)
- **Énergie :** Fissure sombre dans le sol

#### earth.stone_golem (SUMMON, cast=40)
**Animation :** Le lanceur assemble des pierres devant lui. Golem prend forme.
- **Keyframes :** 5 (neutre → assemblage → formation → golem → animation)
- **Énergie :** Golem de pierre qui se lève

#### earth.rock_path (ZONE, cast=20)
**Animation :** Le lanceur trace un chemin. Rochers émergent du sol.
- **Keyframes :** 3 (neutre → trace → chemin)
- **Énergie :** Plateformes de pierre

#### earth.targeted_quake (ZONE, cast=15)
**Animation :** Le lanceur pointe la cible. Tremblement ciblé.
- **Keyframes :** 3 (neutre → pointe → secousse)
- **Énergie :** Secousse localisée

#### earth.stone_statue (PROJECTILE, cast=15)
**Animation :** Le lanceur pointe la cible. La cible se pétrifie.
- **Keyframes :** 3 (neutre → pointe → pétrification)
- **Énergie :** Pierre qui envahit la cible

#### earth.drill_through (PROJECTILE, cast=10)
**Animation :** Le lanceur fait un mouvement de vrille. Perceuse de terre.
- **Keyframes :** 3 (neutre → vrille → percement)
- **Énergie :** Foret de roche

### T4

#### earth.earthquake (ZONE, cast=40)
**Animation :** Le lanceur frappe le sol avec les deux poings. Séisme massif.
- **Keyframes :** 5 (neutre → charge → frappe → séisme → chaos)
- **Énergie :** Onde sismique massive

#### earth.earth_column (PROJECTILE, cast=10)
**Animation :** Le lanceur pointe le sol. Colonne de terre jaillit vers la cible.
- **Keyframes :** 3 (neutre → pointe → colonne)
- **Énergie :** Colonne de pierre qui perce le ciel

#### earth.earth_magnet (ZONE, cast=30)
**Animation :** Le lanceur ouvre les bras. Pierre attire tout vers le centre.
- **Keyframes :** 4 (neutre → bras ouverts → attraction → compression)
- **Énergie :** Champ magnétique de pierre

#### earth.lithic_regen (BUFF, cast=30)
**Animation :** Le lanceur s'enveloppe de pierre. Régénération lente.
- **Keyframes :** 4 (neutre → enveloppement → pierre → régénération)
- **Énergie :** Lumière verte dans la pierre

### T5 — Ultime

#### earth.titan_awakening (ULTIMATE, cast=60)
**Animation :** Le lanceur s'accroupit. La terre tremble. Un titan de pierre émerge du sol et fusionne avec le lanceur.
- **Keyframes :** 8 (neutre → accroupi → tremblement → titan émerge → fusion → titan-complet → pose → retour)
- **Énergie :** Titan de pierre géant

#### earth.earth_mother_wrath (ULTIMATE, cast=60)
**Animation :** Le lanceur lève les bras. La terre entière se soulève. Colère de la terre.
- **Keyframes :** 8 (neutre → bras au ciel → terre se soulève → chaos → destruction → récession → calme → retour)
- **Énergie :** Destruction sismique totale

---

## ⚡ FOUDRE (28 sorts)

### T1

#### lightning.electric_arc (PROJECTILE, cast=0)
**Animation :** Bras droit tendu, doigt pointé. Arc électrique jaillit du bout du doigt.
- **Keyframes :** 2 (neutre → arc)
- **Énergie :** Éclair bleu/blanc

#### lightning.discharge (BUFF, cast=40)
**Animation :** Le lanceur serre les poings. Énergie s'accumule. Décharge électrique.
- **Keyframes :** 4 (neutre → poings serrés → accumulation → décharge)
- **Énergie :** Éclairs qui jaillissent du corps

#### lightning.antenna (ZONE, cast=20)
**Animation :** Le lanceur pointe le ciel. Paratonnerre attire la foudre.
- **Keyframes :** 3 (neutre → pointe → foudre)
- **Énergie :** Colonne de foudre qui descend

#### lightning.bouncing_spark (PROJECTILE, cast=0)
**Animation :** Geste de lancer. Étincelle qui rebondit.
- **Keyframes :** 2 (neutre → lancer)
- **Énergie :** Étincelle qui rebondit entre cibles

#### lightning.static_charge (BUFF, cast=10)
**Animation :** Le lanceur frotte ses mains. Charge statique.
- **Keyframes :** 3 (neutre → frottement → charge)
- **Énergie :** Étincelles entre les mains

#### lightning.electric_detection (BUFF, cast=10)
**Animation :** Le lanceur ferme les yeux. Sensation électrique.
- **Keyframes :** 3 (neutre → yeux fermés → détection)
- **Énergie :** Ondes électriques autour de la tête

### T2

#### lightning.chain_lightning (PROJECTILE, cast=10)
**Animation :** Le lanceur lance un éclair qui rebondit de cible en cible.
- **Keyframes :** 3 (neutre → lancer → rebonds)
- **Énergie :** Chaîne d'éclairs

#### lightning.lightning_armor (BUFF, cast=20)
**Animation :** Le lanceur se couvre d'éclairs. Armure foudroyante.
- **Keyframes :** 4 (neutre → couverture → armure → pulsation)
- **Énergie :** Éclairs qui courent sur le corps

#### lightning.acceleration (BUFF, cast=10)
**Animation :** Le lanceur tape ses pieds. Électricité accélère.
- **Keyframes :** 3 (neutre → tape → accélération)
- **Énergie :** Lignes d'éclairs sous les pieds

#### lightning.paralysis (PROJECTILE, cast=5)
**Animation :** Geste sec. Éclair paralysant.
- **Keyframes :** 2 (neutre → éclair)
- **Énergie :** Éclair qui paralyse

#### lightning.lightning_link (PROJECTILE, cast=10)
**Animation :** Le lanceur tend la main. Lien électrique vers la cible.
- **Keyframes :** 3 (neutre → main tendue → lien)
- **Énergie :** Fil d'électricité

#### lightning.faraday_cage (BUFF, cast=15)
**Animation :** Le lanceur trace un cage autour de lui. Protection électrique.
- **Keyframes :** 3 (neutre → trace → cage)
- **Énergie :** Cage d'éclairs

#### lightning.electro_muscle (BUFF, cast=0)
**Animation :** Le lanceur serre les poings. Muscles électrifiés.
- **Keyframes :** 2 (neutre → poings)
- **Énergie :** Éclairs sur les muscles

#### lightning.underground_arc (PROJECTILE, cast=10)
**Animation :** Le lanceur frappe le sol. Arc souterrain.
- **Keyframes :** 3 (neutre → frappe → arc)
- **Énergie :** Éclair qui court dans le sol

### T3

#### lightning.electric_storm (ZONE, cast=30)
**Animation :** Le lanceur lève les bras. Tempête d'éclairs.
- **Keyframes :** 4 (neutre → bras levés → tempête → éclairs)
- **Énergie :** Pluie d'éclairs

#### lightning.surge (ZONE, cast=15)
**Animation :** Le lanceur projette les mains vers l'avant. Surgissement électrique.
- **Keyframes :** 3 (neutre → projection → surge)
- **Énergie :** Onde électrique

#### lightning.emp (ZONE, cast=20)
**Animation :** Le lanceur tape ses mains ensemble. Impulsion électromagnétique.
- **Keyframes :** 3 (neutre → clap → impulsion)
- **Énergie :** Onde EMP visible

#### lightning.guided_lightning (PROJECTILE, cast=0)
**Animation :** Le lanceur pointe. Éclair guidé frappe la cible.
- **Keyframes :** 2 (neutre → pointe)
- **Énergie :** Éclair qui cherche sa cible

#### lightning.plasma_orb (ZONE, cast=20)
**Animation :** Le lanceur forme une sphère entre ses mains. Orbe de plasma.
- **Keyframes :** 3 (neutre → formation → orbe)
- **Énergie :** Sphère de plasma bleu/violet

#### lightning.electric_conversion (BUFF, cast=10)
**Animation :** Le lanceur absorbe l'électricité. Conversion en énergie vitale.
- **Keyframes :** 3 (neutre → absorption → conversion)
- **Énergie :** Électricité qui se transforme en lumière verte

#### lightning.fan_arc (ZONE, cast=5)
**Animation :** Le lanceur étend les bras. Arcs en éventail.
- **Keyframes :** 2 (neutre → éventail)
- **Énergie :** Éclairs en éventail

#### lightning.electrostatic_storm (ZONE, cast=20)
**Animation :** Le lanceur lève les mains. Tempête électrostatique.
- **Keyframes :** 3 (neutre → mains levées → tempête)
- **Énergie :** Étincelles partout

### T4

#### lightning.storm_call (ZONE, cast=40)
**Animation :** Le lanceur lève les bras au ciel. Appel de tempête.
- **Keyframes :** 5 (neutre → bras au ciel → nuages → éclairs → tempête)
- **Énergie :** Tempête d'éclairs

#### lightning.conduit (BUFF, cast=30)
**Animation :** Le lanceur devient un conducteur. Éclairs le traversent.
- **Keyframes :** 4 (neutre → conducteur → éclairs → puissance)
- **Énergie :** Éclairs qui traversent le corps

#### lightning.rainbow_arc (PROJECTILE, cast=20)
**Animation :** Le lanceur fait un arc-en-ciel avec son bras. Arc multicolore.
- **Keyframes :** 3 (neutre → arc → multicolore)
- **Énergie :** Arc-en-ciel d'électricité

#### lightning.storm_heart (BUFF, cast=20)
**Animation :** Le lanceur pose la main sur son cœur. Cœur de tempête.
- **Keyframes :** 3 (neutre → main sur cœur → tempête)
- **Énergie :** Cœur d'éclairs

### T5 — Ultime

#### lightning.celestial_judgment (ULTIMATE, cast=100)
**Animation :** Le lanceur s'élève. Le ciel s'ouvre. Jugement divin de foudre.
- **Keyframes :** 10 (neutre → élévation → ciel s'ouvre → charge → accumulation → jugement → éclairs → chaos → récession → calme)
- **Énergie :** Colonne de foudre divine

#### lightning.wrath_of_zeus (ULTIMATE, cast=100)
**Animation :** Le lanceur lève le poing. Foudre de Zeus.
- **Keyframes :** 10 (neutre → poing au ciel → Zeus → foudre → destruction → chaos → récession → calme → retour → pose)
- **Énergie :** Foudre mythologique

---

## 🟤 LAVE (28 sorts)

### T1

#### lava.lava_jet (PROJECTILE, cast=5)
**Animation :** Bras tendu, paume ouverte. Jet de lave.
- **Keyframes :** 2 (neutre → jet)
- **Énergie :** Jet de lave orange/rouge

#### lava.burning_puddle (ZONE, cast=10)
**Animation :** Le lanceur verse de la lave au sol.
- **Keyframes :** 3 (neutre → versement → flaque)
- **Énergie :** Flaque de lave

#### lava.magma_shield (BUFF, cast=15)
**Animation :** Le lanceur forme un bouclier de magma.
- **Keyframes :** 3 (neutre → formation → bouclier)
- **Énergie :** Bouclier de magma incandescent

#### lava.lava_splash (ZONE, cast=0)
**Animation :** Frappe au sol. Éclaboussures de lave.
- **Keyframes :** 2 (neutre → frappe)
- **Énergie :** Gouttes de lave

#### lava.igneous_rock (PROJECTILE, cast=0)
**Animation :** Lancer de roche volcanique.
- **Keyframes :** 2 (neutre → lancer)
- **Énergie :** Roche enflammée

#### lava.heat_hindrance (ZONE, cast=15)
**Animation :** Le lanceur génère une chaleur intense. Ralentissement.
- **Keyframes :** 3 (neutre → chaleur → zone)
- **Énergie :** Onde de chaleur

### T2

#### lava.volcanic_fissure (ZONE, cast=20)
**Animation :** Le lanceur trace une fissure. Lave en jaillit.
- **Keyframes :** 3 (neutre → fissure → lave)
- **Énergie :** Fissure volcanique

#### lava.magma_bomb (PROJECTILE, cast=10)
**Animation :** Le lanceur lance une bombe de magma.
- **Keyframes :** 3 (neutre → charge → lancer)
- **Énergie :** Bombe de magma qui explose

#### lava.magma_armor (BUFF, cast=20)
**Animation :** Le lanceur se couvre de magma. Armure brûlante.
- **Keyframes :** 4 (neutre → couverture → armure → refroidissement)
- **Énergie :** Armure de magma

#### lava.lava_walk (BUFF, cast=10)
**Animation :** Le lanceur passe les mains sur ses pieds. Immunité lave.
- **Keyframes :** 3 (neutre → passage → protection)
- **Énergie :** Aura de protection thermique

#### lava.volcanic_tears (PROJECTILE, cast=10)
**Animation :** Le lanceur verse des larmes de lave.
- **Keyframes :** 3 (neutre → larmes → projection)
- **Énergie :** Gouttes de lave brûlantes

#### lava.lava_scar (ZONE, cast=10)
**Animation :** Le lanceur marque le sol. Scarification de lave.
- **Keyframes :** 3 (neutre → marquage → scar)
- **Énergie :** Marque brûlante au sol

#### lava.slag_armor (BUFF, cast=20)
**Animation :** Le lanceur se couvre de scories. Armure de résidus.
- **Keyframes :** 3 (neutre → couverture → armure)
- **Énergie :** Armure de scories

#### lava.smoke_breath (ZONE, cast=10)
**Animation :** Le lanceur souffle de la fumée volcanique.
- **Keyframes :** 3 (neutre → souffle → fumée)
- **Énergie :** Nuage de fumée toxique

### T3

#### lava.magma_wall (ZONE, cast=15)
**Animation :** Le lanceur lève les mains. Mur de magma émerge.
- **Keyframes :** 3 (neutre → mains levées → mur)
- **Énergie :** Mur de magma

#### lava.lava_rain (ZONE, cast=30)
**Animation :** Le lanceur lève les bras. Pluie de lave.
- **Keyframes :** 4 (neutre → bras levés → nuages → pluie)
- **Énergie :** Pluie de lave

#### lava.incandescent_ground (ZONE, cast=20)
**Animation :** Le lanceur frappe le sol. Sol incandescent.
- **Keyframes :** 3 (neutre → frappe → incandescence)
- **Énergie :** Sol qui brûle

#### lava.magma_wave (ZONE, cast=20)
**Animation :** Le lanceur projette une vague de magma.
- **Keyframes :** 4 (neutre → charge → projection → vague)
- **Énergie :** Vague de magma

#### lava.rock_fusion (ZONE, cast=15)
**Animation :** Le lanceur fusionne les roches. Magma se forme.
- **Keyframes :** 3 (neutre → fusion → magma)
- **Énergie :** Roches qui fondent

#### lava.pyroclastic_flow (ZONE, cast=20)
**Animation :** Le lanceur libère un flux pyroclastique.
- **Keyframes :** 4 (neutre → charge → libération → flux)
- **Énergie :** Nuée ardente

#### lava.magma_grenade (PROJECTILE, cast=10)
**Animation :** Le lanceur lance une grenade de magma.
- **Keyframes :** 3 (neutre → charge → lancer)
- **Énergie :** Grenade qui explose en magma

#### lava.lava_pit (ZONE, cast=20)
**Animation :** Le lanceur ouvre un piège de lave.
- **Keyframes :** 3 (neutre → ouverture → piège)
- **Énergie :** Fosse de lave

### T4

#### lava.eruption (ZONE, cast=40)
**Animation :** Le lanceur lève les bras. Éruption volcanique.
- **Keyframes :** 5 (neutre → bras au ciel → charge → éruption → chaos)
- **Énergie :** Éruption massive

#### lava.magmatic_form (BUFF, cast=30)
**Animation :** Le lanceur devient magma. Forme magmatique.
- **Keyframes :** 5 (neutre → transformation → magma → pose → pulsation)
- **Énergie :** Corps de magma vivant

#### lava.magma_heart (BUFF, cast=30)
**Animation :** Le lanceur pose la main sur son cœur. Cœur de magma.
- **Keyframes :** 4 (neutre → main sur cœur → magma → pulsation)
- **Énergie :** Cœur de magma visible

#### lava.total_fusion (BUFF, cast=20)
**Animation :** Le lanceur fusionne totalement avec la lave.
- **Keyframes :** 4 (neutre → fusion → totale → pose)
- **Énergie :** Être de lave pur

### T5 — Ultime

#### lava.volcanic_awakening (ULTIMATE, cast=60)
**Animation :** Le lanceur s'accroupit. La terre tremble. Volcan se réveille.
- **Keyframes :** 8 (neutre → accroupi → tremblement → volcan → éruption → chaos → récession → calme)
- **Énergie :** Éruption volcanique totale

#### lava.cataclysmic_eruption (ULTIMATE, cast=60)
**Animation :** Le lanceur lève les bras. Éruption cataclysmique.
- **Keyframes :** 8 (neutre → bras au ciel → charge → cataclysme → destruction → chaos → récession → calme)
- **Énergie :** Destruction volcanique absolue

---

## ❄️ GLACE (28 sorts)

### T1

#### ice.frost_breath (ZONE, cast=0)
**Animation :** Souffle glacé. Bouche ouverte, vapeur froide.
- **Keyframes :** 2 (neutre → souffle)
- **Énergie :** Cône de givre

#### ice.ice_arrow (PROJECTILE, cast=5)
**Animation :** Le lanceur forme une flèche de glace et la tire.
- **Keyframes :** 3 (neutre → formation → tir)
- **Énergie :** Flèche de glace

#### ice.slippery_ground (ZONE, cast=10)
**Animation :** Le lanceur pointe le sol. Glace au sol.
- **Keyframes :** 3 (neutre → pointe → glace)
- **Énergie :** Sol glacé

#### ice.shiver (PROJECTILE, cast=0)
**Animation :** Frisson. Énergie froide jaillit.
- **Keyframes :** 2 (neutre → frisson)
- **Énergie :** Onde de froid

#### ice.ground_frost (ZONE, cast=5)
**Animation :** Le lanceur touche le sol. Givre se propage.
- **Keyframes :** 2 (neutre → contact)
- **Énergie :** Givre qui se propage

#### ice.frozen_breath (ZONE, cast=0)
**Animation :** Souffle qui gèle tout.
- **Keyframes :** 2 (neutre → souffle)
- **Énergie :** Souffle cristallin

### T2

#### ice.ice_prison (ZONE, cast=10)
**Animation :** Le lanceur ferme le poing. Prison de glace.
- **Keyframes :** 3 (neutre → poing → prison)
- **Énergie :** Cage de glace

#### ice.frost_armor (BUFF, cast=20)
**Animation :** Le lanceur se couvre de givre. Armure de glace.
- **Keyframes :** 4 (neutre → couverture → armure → cristallisation)
- **Énergie :** Armure de glace

#### ice.stalactites (ZONE, cast=10)
**Animation :** Le lanceur pointe le ciel. Stalactites tombent.
- **Keyframes :** 3 (neutre → pointe → stalactites)
- **Énergie :** Stalactites de glace

#### ice.frost_steps (BUFF, cast=10)
**Animation :** Le lanceur tape ses pieds. Pas de givre.
- **Keyframes :** 2 (neutre → tape)
- **Énergie :** Givre sous les pieds

#### ice.frost_crystal (ZONE, cast=15)
**Animation :** Le lanceur forme un cristal de givre.
- **Keyframes :** 3 (neutre → formation → cristal)
- **Énergie :** Cristal de glace

#### ice.iceberg_lance (PROJECTILE, cast=10)
**Animation :** Le lanceur lance un iceberg miniature.
- **Keyframes :** 3 (neutre → charge → lancer)
- **Énergie :** Iceberg volant

#### ice.rapid_cooling (ZONE, cast=5)
**Animation :** Le lanceur refroidit la zone instantanément.
- **Keyframes :** 2 (neutre → refroidissement)
- **Énergie :** Onde de froid

#### ice.ice_thorns (BUFF, cast=10)
**Animation :** Le lanceur se couvre d'épines de glace.
- **Keyframes :** 3 (neutre → couverture → épines)
- **Énergie :** Épines de glace

### T3

#### ice.blizzard (ZONE, cast=30)
**Animation :** Le lanceur lève les bras. Blizzard se forme.
- **Keyframes :** 4 (neutre → bras levés → blizzard → chaos)
- **Énergie :** Tempête de neige

#### ice.freeze (ZONE, cast=15)
**Animation :** Le lanceur pointe la cible. Gel instantané.
- **Keyframes :** 3 (neutre → pointe → gel)
- **Énergie :** Glace qui envahit

#### ice.ice_wall (ZONE, cast=10)
**Animation :** Le lanceur lève les mains. Mur de glace.
- **Keyframes :** 3 (neutre → mains levées → mur)
- **Énergie :** Mur de glace

#### ice.frozen_heart (BUFF, cast=20)
**Animation :** Le lanceur pose la main sur son cœur. Cœur de glace.
- **Keyframes :** 3 (neutre → main sur cœur → glace)
- **Énergie :** Cœur de cristal

#### ice.cold_room (ZONE, cast=20)
**Animation :** Le lanceur abaisse la température. Chambre froide.
- **Keyframes :** 3 (neutre → abaissement → froide)
- **Énergie :** Zone de froid intense

#### ice.hail_wall (ZONE, cast=15)
**Animation :** Le lanceur crée un mur de grêlons.
- **Keyframes :** 3 (neutre → formation → mur)
- **Énergie :** Mur de grêle

#### ice.frost_link (PROJECTILE, cast=10)
**Animation :** Le lanceur crée un lien de givre vers la cible.
- **Keyframes :** 3 (neutre → lien → gel)
- **Énergie :** Chaîne de glace

#### ice.frozen_heart_v2 (BUFF, cast=20)
**Animation :** Version améliorée. Cœur de glace pur.
- **Keyframes :** 4 (neutre → purification → cristallisation → cœur)
- **Énergie :** Cristal de glace pur

### T4

#### ice.frostbite_storm (ZONE, cast=40)
**Animation :** Le lanceur lève les bras. Tempête de gelure.
- **Keyframes :** 5 (neutre → bras levés → tempête → gelure → chaos)
- **Énergie :** Tempête de glace

#### ice.glacial_avalanche (ZONE, cast=30)
**Animation :** Le lanceur pointe. Avalanche glaciaire.
- **Keyframes :** 4 (neutre → pointe → avalanche → chaos)
- **Énergie :** Avalanche de glace

#### ice.diamond_prison (PROJECTILE, cast=20)
**Animation :** Le lanceur crée une prison de diamant.
- **Keyframes :** 4 (neutre → formation → cristallisation → prison)
- **Énergie :** Prison de diamant de glace

#### ice.absolute_zero (ZONE, cast=20)
**Animation :** Le lanceur abaisse tout au zéro absolu.
- **Keyframes :** 4 (neutre → abaissement → zéro → gel)
- **Énergie :** Froid extrême

### T5 — Ultime

#### ice.ice_age (ULTIMATE, cast=60)
**Animation :** Le lanceur s'élève. L'ère glaciaire commence.
- **Keyframes :** 8 (neutre → élévation → glaciation → expansion → ère glaciaire → chaos → récession → calme)
- **Énergie :** Glaciation totale

#### ice.eternal_winter (ULTIMATE, cast=60)
**Animation :** Le lanceur ouvre les bras. Hiver éternel.
- **Keyframes :** 8 (neutre → bras ouverts → hiver → expansion → éternité → chaos → récession → calme)
- **Énergie :** Hiver permanent

---

## 💀 NÉCROMANCIE (28 sorts)

### T1

#### necromancy.drain_vital (PROJECTILE, cast=10)
**Animation :** Le lanceur tend la main vers la cible. Drain de vitalité.
- **Keyframes :** 3 (neutre → main tendue → drain)
- **Énergie :** Flux vert qui va de la cible au lanceur

#### necromancy.death_touch (PROJECTILE, cast=0)
**Animation :** Toucher mortel. Contact bref et fatal.
- **Keyframes :** 2 (neutre → toucher)
- **Énergie :** Aura noire au bout des doigts

#### necromancy.death_veil (BUFF, cast=15)
**Animation :** Le lanceur se couvre d'un voile de mort.
- **Keyframes :** 3 (neutre → voile → invisibilité)
- **Énergie :** Ombre qui enveloppe

#### necromancy.broken_bones (PROJECTILE, cast=0)
**Animation :** Geste sec. Os brisés.
- **Keyframes :** 2 (neutre → geste)
- **Énergie :** Onde sombre

#### necromancy.empty_gaze (PROJECTILE, cast=5)
**Animation :** Le lanceur fixe la cible. Regard vide.
- **Keyframes :** 2 (neutre → regard)
- **Énergie :** Yeux vides et lumineux

#### necromancy.putrid_hand (PROJECTILE, cast=0)
**Animation :** Main pourrie qui touche.
- **Keyframes :** 2 (neutre → toucher)
- **Énergie :** Aura putride

### T2

#### necromancy.corrupted_resurrection (SUMMON, cast=40)
**Animation :** Le lanceur lève les bras. Résurrection corrompue.
- **Keyframes :** 5 (neutre → bras levés → invocation → résurrection → mort-vivant)
- **Énergie :** Colonne d'énergie noire

#### necromancy.lethal_curse (PROJECTILE, cast=15)
**Animation :** Le lanceur trace une rune maudite. Malédiction létale.
- **Keyframes :** 3 (neutre → rune → malédiction)
- **Énergie :** Rune sombre qui vole

#### necromancy.soul_rend (PROJECTILE, cast=10)
**Animation :** Le lanceur arrache l'âme de la cible.
- **Keyframes :** 3 (neutre → arrachement → âme)
- **Énergie :** Âme qui se sépare du corps

#### necromancy.soul_chains (PROJECTILE, cast=10)
**Animation :** Le lanceur lance des chaînes spectrales.
- **Keyframes :** 3 (neutre → lancer → chaînes)
- **Énergie :** Chaînes spectrales

#### necromancy.suffering_transfer (BUFF, cast=10)
**Animation :** Le lanceur transfère sa souffrance.
- **Keyframes :** 3 (neutre → transfert → libération)
- **Énergie :** Flux de douleur

#### necromancy.grave_call (ZONE, cast=15)
**Animation :** Le lanceur appelle du tombeau.
- **Keyframes :** 3 (neutre → appel → émergence)
- **Énergie :** Mains qui sortent du sol

#### necromancy.cold_fingers (PROJECTILE, cast=5)
**Animation :** Doigts glacés qui touchent.
- **Keyframes :** 2 (neutre → toucher)
- **Énergie :** Doigts spectraux

#### necromancy.blood_ritual (BUFF, cast=20)
**Animation :** Le lanceur trace un cercle de sang.
- **Keyframes :** 4 (neutre → trace → cercle → rituel)
- **Énergie :** Cercle de sang luminescent

### T3

#### necromancy.army_of_dead (SUMMON, cast=40)
**Animation :** Le lanceur lève les bras. Armée des morts.
- **Keyframes :** 5 (neutre → bras levés → invocation → armée → marche)
- **Énergie :** Armée de squelettes

#### necromancy.soul_corruption (PROJECTILE, cast=15)
**Animation :** Le lanceur corrompt l'âme de la cible.
- **Keyframes :** 3 (neutre → corruption → âme)
- **Énergie :** Âme qui noircit

#### necromancy.rift (ZONE, fail=20)
**Animation :** Le lanceur ouvre une faille.
- **Keyframes :** 3 (neutre → ouverture → faille)
- **Énergie :** Fissure dans le réel

#### necromancy.terror (ZONE, cast=20)
**Animation :** Le lanceur projette de la terreur.
- **Keyframes :** 3 (neutre → projection → terreur)
- **Énergie :** Onde de terreur noire

#### necromancy.spectral_knight (SUMMON, cast=40)
**Animation :** Le lanceur invoque un chevalier spectral.
- **Keyframes :** 5 (neutre → invocation → spectre → armure → chevalier)
- **Énergie :** Chevalier spectralement lumineux

#### necromancy.decomposition (PROJECTILE, cast=15)
**Animation :** Le lanceur décompose la cible.
- **Keyframes :** 3 (neutre → décomposition → pourriture)
- **Énergie :** Chair qui se décompose

#### necromancy.voices_of_dead (SUMMON, cast=20)
**Animation :** Le lanceur invoque les voix des morts.
- **Keyframes :** 3 (neutre → invocation → voix)
- **Énergie :** Spectres qui murmurent

#### necromancy.mark_of_sacrifice (PROJECTILE, cast=10)
**Animation :** Le lanceur marque la cible du sceau de sacrifice.
- **Keyframes :** 3 (neutre → marquage → sceau)
- **Énergie :** Rune de sacrifice

### T4

#### necromancy.the_void (ZONE, cast=30)
**Animation :** Le lanceur ouvre le vide.
- **Keyframes :** 4 (neutre → ouverture → vide → aspiration)
- **Énergie :** Trou noir qui aspire

#### necromancy.shroud_of_shadows (BUFF, cast=20)
**Animation :** Le lanceur se enveloppe d'ombres.
- **Keyframes :** 3 (neutre → ombres → invisibilité)
- **Énergie :** Cape d'ombres

#### necromancy.soul_scythe (BUFF, cast=30)
**Animation :** Le lanceur forme une faux d'âmes.
- **Keyframes :** 4 (neutre → formation → faux → pose)
- **Énergie :** Faux spectralement lumineuse

#### necromancy.necropolis (ZONE, cast=40)
**Animation :** Le lanceur invoque une nécropole.
- **Keyframes :** 5 (neutre → invocation → nécropole → émergence → complète)
- **Énergie :** Nécropole spectrale

### T5 — Ultime

#### necromancy.hand_of_nox (ULTIMATE, cast=60)
**Animation :** Le lanceur lève la main. Main de Nox émerge.
- **Keyframes :** 8 (neutre → main levée → Nox émerge → main géante → destruction → chaos → récession → calme)
- **Énergie :** Main géante de ténèbres

#### necromancy.macabre_dance (ULTIMATE, cast=60)
**Animation :** Le lanceur danse. Danse macabre.
- **Keyframes :** 8 (neutre → danse → macabre → morts → danse → chaos → récession → calme)
- **Énergie :** Danse de morts-vivants

---

## ✨ LUMAMANCIE (28 sorts)

### T1

#### lumamancy.light_shard (PROJECTILE, cast=5)
**Animation :** Le lanceur lance un éclat de lumière.
- **Keyframes :** 2 (neutre → lancer)
- **Énergie :** Éclat lumineux doré

#### lumamancy.sacred_light (BUFF, cast=20)
**Animation :** Le lanceur baigne dans la lumière sacrée.
- **Keyframes :** 3 (neutre → lumière → bain)
- **Énergie :** Lumière dorée

#### lumamancy.light_purification (BUFF, cast=10)
**Animation :** Le lanceur se purifie avec la lumière.
- **Keyframes :** 3 (neutre → purification → lumière)
- **Énergie :** Lumière purificatrice

#### lumamancy.blinding_ray (PROJECTILE, cast=5)
**Animation :** Le lanceur projette un rayon aveuglant.
- **Keyframes :** 2 (neutre → rayon)
- **Énergie :** Rayon de lumière intense

#### lumamancy.glowing_wisp (BUFF, cast=10)
**Animation :** Le lanceur invoque une volonté lumineuse.
- **Keyframes :** 3 (neutre → invocation → wisp)
- **Énergie :** Orbe lumineux

#### lumamancy.minor_blessing (BUFF, cast=10)
**Animation :** Le lanceur bénit légèrement.
- **Keyframes :** 2 (neutre → bénédiction)
- **Énergie :** Lumière douce

### T2

#### lumamancy.sacred_aura (BUFF, cast=20)
**Animation :** Le lanceur émet une aura sacrée.
- **Keyframes :** 3 (neutre → aura → expansion)
- **Énergie :** Aura dorée

#### lumamancy.blessed_heal (BUFF, cast=20)
**Animation :** Le lanceur invoque une guérison bénite.
- **Keyframes :** 3 (neutre → invocation → guérison)
- **Énergie :** Lumière de guérison

#### lumamancy.divine_shield (BUFF, cast=15)
**Animation :** Le lanceur crée un bouclier divin.
- **Keyframes :** 3 (neutre → formation → bouclier)
- **Énergie :** Bouclier de lumière

#### lumamancy.blessing (BUFF, cast=20)
**Animation :** Le lanceur bénit avec puissance.
- **Keyframes :** 4 (neutre → bénédiction → lumière → expansion)
- **Énergie :** Bénédiction lumineuse

#### lumamancy.light_sword (BUFF, cast=15)
**Animation :** Le lanceur forme une épée de lumière.
- **Keyframes :** 3 (neutre → formation → épée)
- **Énergie :** Épée de lumière

#### lumamancy.sacred_rampart (ZONE, cast=15)
**Animation :** Le lanceur crée un rempart sacré.
- **Keyframes :** 3 (neutre → formation → rempart)
- **Énergie :** Mur de lumière

#### lumamancy.minor_purification (BUFF, cast=5)
**Animation :** Purification mineure.
- **Keyframes :** 2 (neutre → purification)
- **Énergie :** Lumière purificatrice

#### lumamancy.dazzling_flash (ZONE, cast=5)
**Animation :** Le lanceur crée un flash éblouissant.
- **Keyframes :** 2 (neutre → flash)
- **Énergie :** Flash de lumière aveuglant

### T3

#### lumamancy.greater_purification (ZONE, cast=30)
**Animation :** Le lanceur purifie une zone entière.
- **Keyframes :** 4 (neutre → purification → zone → lumière)
- **Énergie :** Zone de purification

#### lumamancy.healing_wave (ZONE, cast=20)
**Animation :** Le lanceur envoie une vague de guérison.
- **Keyframes :** 3 (neutre → vague → guérison)
- **Énergie :** Vague de lumière verte

#### lumamancy.holy_strike (PROJECTILE, cast=20)
**Animation :** Le lanceur frappe avec la lumière sainte.
- **Keyframes :** 3 (neutre → charge → frappe)
- **Énergie :** Frappe de lumière dorée

#### lumamancy.sanctuary (ZONE, cast=30)
**Animation :** Le lanceur crée un sanctuaire.
- **Keyframes :** 4 (neutre → formation → sanctuaire → lumière)
- **Énergie :** Sanctuaire lumineux

#### lumamancy.pillar_of_light (ZONE, cast=20)
**Animation :** Le lanceur invoque un pilier de lumière.
- **Keyframes :** 3 (neutre → invocation → pilier)
- **Énergie :** Colonne de lumière

#### lumamancy.minor_angel (SUMMON, cast=40)
**Animation :** Le lanceur invoque un ange mineur.
- **Keyframes :** 5 (neutre → invocation → ange → ailes → complète)
- **Énergie :** Ange lumineux

#### lumamancy.redemption (BUFF, cast=30)
**Animation :** Le lanceur rachète une âme.
- **Keyframes :** 4 (neutre → rachat → lumière → âme)
- **Énergie :** Lumière de rédemption

#### lumamancy.avenging_light (PROJECTILE, cast=15)
**Animation :** Le lanceur lance la lumière vengeresse.
- **Keyframes :** 3 (neutre → charge → lancer)
- **Énergie :** Lumière vengeresse

### T4

#### lumamancy.resurrection (BUFF, cast=60)
**Animation :** Le lanceur ressuscite un allié.
- **Keyframes :** 6 (neutre → invocation → lumière → résurrection → âme → vie)
- **Énergie :** Lumière de résurrection

#### lumamancy.judgment (ZONE, cast=40)
**Animation :** Le lanceur juge au nom de la lumière.
- **Keyframes :** 5 (neutre → jugement → lumière → verdict → exécution)
- **Énergie :** Jugement divin

#### lumamancy.celestial_portal (ZONE, cast=40)
**Animation :** Le lanceur ouvre un portail céleste.
- **Keyframes :** 5 (neutre → ouverture → portail → lumière → guérison)
- **Énergie :** Portail de lumière

#### lumamancy.aura_of_purity (BUFF, cast=30)
**Animation :** Le lanceur émet une aura de pureté.
- **Keyframes :** 4 (neutre → aura → pureté → expansion)
- **Énergie :** Aura de pureté

### T5 — Ultime

#### lumamancy.miracle_of_aelira (ULTIMATE, cast=100)
**Animation :** Le lanceur invoque le miracle d'Aelira.
- **Keyframes :** 10 (neutre → invocation → Aelira → miracle → lumière → guérison → résurrection → chaos → récession → calme)
- **Énergie :** Miracle divin

#### lumamancy.paradise_found (ULTIMATE, cast=60)
**Animation :** Le lanceur ouvre les portes du paradis.
- **Keyframes :** 8 (neutre → ouverture → paradis → lumière → guérison → résurrection → invulnérabilité → calme)
- **Énergie :** Paradis lumineux

---

## Notes techniques pour Blender

### Timing
- **1 tick Minecraft = 0.05 seconde** (20 ticks/seconde)
- CastTime 60 = 3 secondes d'animation
- CastTime 0 = geste instantané (2-3 frames)

### Format Epic Fight
- Animation au format `.json` (Epic Fight)
- Squelette BIPED (humanoïde standard)
- Bones : root, head, body, left_arm, right_arm, left_leg, right_leg
- Export via addon Epic Fight pour Blender

### Particules
- Les particules ne sont PAS dans l'animation — elles sont gérées par le code Java
- L'animation donne la **pose** et le **timing**
- Les particules sont ajoutées via `SpellParticlePacket`

### Hiérarchie des poses
1. **Neutre** → position de base
2. **Charge** → préparation (bras en arrière, accroupi, etc.)
3. **Release** → l'exécution (frappe, lancer, souffle)
4. **Follow-through** → le retour après l'action
