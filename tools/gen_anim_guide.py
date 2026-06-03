import json

with open(r'C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\src\main\resources\data\sihriya\spells.json') as f:
    spells = json.load(f)

from collections import defaultdict
by_school = defaultdict(list)
for s in spells:
    by_school[s['school']].append(s)

school_icons = {
    "fire": "[FEU]", "water": "[EAU]", "wind": "[VENT]", "earth": "[TERRE]",
    "lightning": "[FOUDRE]", "ice": "[GLACE]", "lava": "[LAVE]",
    "necromancy": "[NECRO]", "lumamancy": "[LUMA]"
}

tier_names = {1: "Apprenti", 2: "Initie", 3: "Expert", 4: "Maitre", 5: "Ultime"}

school_postures = {
    "fire": "pieds ecartes, buste legerement en arriere, mains pres du torse",
    "water": "position fluide, genoux flechis, mains en coupe devant la poitrine",
    "wind": "sur la pointe des pieds, bras legerement ecartes, posture aerienne",
    "earth": "pieds bien ancres au sol, buste droit, poings serres, posture solide",
    "lightning": "position dynamique, un pied devant l'autre, main dominante levee",
    "ice": "position ramassee, epaules relevees, mains pres du corps",
    "lava": "posture large et puissante, epaules en arriere, poings serres",
    "necromancy": "posture voutee, tete baissee, doigts recourbes comme des griffes",
    "lumamancy": "posture droite et noble, bras legerement ecartes, paumes vers l'avant"
}

def gen_desc(s):
    school, tier, stype = s['school'], s['tier'], s['type']
    effects = [e['type'] for e in s['effects']]
    lines = []

    lines.append(f"Posture depart | {school_postures.get(school, 'neutre')}")

    if stype == "PROJECTILE":
        lines.append("Jeu de jambes | Pas chasse + transfert poids avant" if tier <= 2 else "Jeu de jambes | Demi-tour sur pied arriere + fente avant")
    elif stype == "ZONE":
        lines.append("Jeu de jambes | Pas en avant + squat" if tier <= 2 else "Jeu de jambes | Grand pas + genou au sol ou rotation 180deg")
    elif stype == "BUFF":
        lines.append("Jeu de jambes | Pieds joints puis petit pas en avant, posture stable")
    elif stype == "SUMMON":
        lines.append("Jeu de jambes | Recul + grand pas arriere, genou a terre")
    elif stype == "ULTIMATE":
        lines.append("Jeu de jambes | Saut + retombee en squat OU reverence")

    gesture_map = {
        ("fire","PROJECTILE"): [
            "Mains remontent le long du torse, paumes vers le haut, flamme qui nait",
            "Flamme grossit et se condense en boule",
            "Extension du bras, paume ouverte, la boule file"
        ],
        ("fire","ZONE"): [
            "Poings serres, bras ecartes, flammes courent sur les avant-bras",
            "Bras se levent au-dessus de la tete, flammes fusionnent",
            "Frappe au sol des deux poings, cercle de feu s'etend"
        ],
        ("fire","BUFF"): [
            "Mains sur le torse, lueur rouge interne",
            "Mains glissent le long du corps, le laissant enflamme",
            "Poussee des bras vers l'avant, aura de feu se deploie"
        ],
        ("fire","SUMMON"): [
            "Grand geste des bras vers le haut, portail de feu s'ouvre",
            "Cercle de feu au sol, les mains tournent autour",
            "Frappe au sol, le phenix de feu emerge"
        ],
        ("fire","ULTIMATE"): [
            "Posture large, bras s'elevent lentement, brasier intense",
            "Le corps se tord sous la puissance",
            "Relachement total, explosion solaire liberee"
        ],
        ("water","PROJECTILE"): [
            "Mains en coupe devant la poitrine, eau apparait",
            "Mains s'ecartent, l'eau se condense en projectile tournoyant",
            "Mouvement de tir a l'arc, projectile file en tournant"
        ],
        ("water","ZONE"): [
            "Mains en coupe, cercle d'eau autour du corps",
            "Rotation des bras, l'eau tourbillonne en whirlpool",
            "Poussee vers le bas, vague circulaire se propage"
        ],
        ("water","BUFF"): [
            "Mains en coupe au-dessus de la tete, eau qui coule en cascade",
            "L'eau descend le long du corps",
            "Bras ecartes, bulle d'eau protectrice se forme"
        ],
        ("water","SUMMON"): [
            "Bras leves, portail d'eau",
            "Cercle aquatique au sol",
            "L'entite d'eau emerge du cercle"
        ],
        ("water","ULTIMATE"): [
            "Posture large, l'eau monte autour du corps en tornade",
            "Concentration extreme, l'eau tourbillonne plus vite",
            "Poussee vers l'avant, tsunami libere"
        ],
        ("wind","PROJECTILE"): [
            "Bras ecartes, paumes vers l'avant, vent qui tourbillonne",
            "Mains se rapprochent, le vent se condense en lame",
            "Poussee soudaine des deux mains, rafale projetee"
        ],
        ("wind","ZONE"): [
            "Bras ecartes, rotation sur place pour creer un tourbillon",
            "Les bras accelerent, le tourbillon monte en hauteur",
            "Poussee vers le sol, tornade au sol s'etend"
        ],
        ("wind","BUFF"): [
            "Bras le long du corps, yeux fermes, concentration",
            "Bras montent lentement, le vent souleve le corps",
            "Poussee vers le haut, legere elevation"
        ],
        ("wind","SUMMON"): [
            "Bras au ciel, appel du vent",
            "Tourbillon qui descend du ciel",
            "Le djinn de vent apparait dans le tourbillon"
        ],
        ("wind","ULTIMATE"): [
            "Bras au ciel, cyclone se forme autour",
            "Le corps tourne sur lui-meme accelerant le cyclone",
            "Poussee vers l'exterieur, oeil du cyclone libere"
        ],
        ("earth","PROJECTILE"): [
            "Poings serres, bras descendent lentement, la terre tremble",
            "Un poing remonte charge de pierre",
            "Coup de poing vers l'avant, le rocher file en tournant"
        ],
        ("earth","ZONE"): [
            "Poings serres, bras le long du corps, concentration tellurique",
            "Les bras montent lentement, le sol gronde",
            "Frappe au sol, le terrain se dechire en cercle"
        ],
        ("earth","BUFF"): [
            "Poings serres, posture basse, la peau durcit",
            "Bras se croisent devant le torse, carapace de pierre",
            "Bras ecartes, la carapace se revele"
        ],
        ("earth","SUMMON"): [
            "Poings au sol, la terre s'accumule",
            "Cercle de pierre au sol",
            "Le golem de terre emerge du sol"
        ],
        ("earth","ULTIMATE"): [
            "Posture large, les bras s'elevent, le sol craque",
            "Tremblement de terre, le lanceur vacille",
            "Frappe au sol, le titan de pierre se leve"
        ],
        ("lightning","PROJECTILE"): [
            "Main dominante levee, doigts ecartes, arcs electriques",
            "L'electricite se concentre au bout des doigts",
            "Mouvement de fouet du poignet, eclair zebre l'air"
        ],
        ("lightning","ZONE"): [
            "Bras au ciel, electricite monte le long des bras",
            "Mains se rejoignent au-dessus, boule electrique",
            "Poussee vers le bas, eclairs frappent le sol en cercle"
        ],
        ("lightning","BUFF"): [
            "Poing sur le torse, etincelles crepitent",
            "Le poing s'eloigne, fil electrique reste connecte",
            "Bras tendu, l'electricite parcourt tout le corps"
        ],
        ("lightning","SUMMON"): [
            "Bras au ciel, appel de la foudre",
            "Multiple eclairs frappent au sol en cercle",
            "L'esprit de foudre se materialise"
        ],
        ("lightning","ULTIMATE"): [
            "Bras au ciel, tempete electrique au-dessus",
            "Le corps est traverse par les eclairs",
            "Pointe vers la cible, jugement celeste s'abat"
        ],
        ("ice","PROJECTILE"): [
            "Mains ramenees a la poitrine, doigts en griffes gelees",
            "Mains tournent l'une autour de l'autre, eclat de glace se forme",
            "Mouvement brusque du poignet, l'eclat file en tournoyant"
        ],
        ("ice","ZONE"): [
            "Bras croises devant le torse, givre sur les avant-bras",
            "Bras s'ecartent lentement, souffle glace s'echappe",
            "Poussee vers l'avant, cone de glace se repand"
        ],
        ("ice","BUFF"): [
            "Bras croises, frisson, givre apparait",
            "Bras s'ecartent, la glace recouvre le corps",
            "Posture de garde, carapace de glace visible"
        ],
        ("ice","SUMMON"): [
            "Mains au sol, le givre s'etend",
            "Cercle de glace au sol",
            "Le golem de glace emerge"
        ],
        ("ice","ULTIMATE"): [
            "Posture large, tempete de neige autour",
            "Le corps gele, souffle glace puissant",
            "Poussee vers l'avant, l'age de glace libere"
        ],
        ("lava","PROJECTILE"): [
            "Poings serres, bras descendent, magma incandescent",
            "Un poing remonte charge de lave",
            "Coup de poing vers le haut, magma projete en arc"
        ],
        ("lava","ZONE"): [
            "Poings serres, bras descendent, lave visible sur la peau",
            "Coup de poing au sol",
            "Le sol se fissure, lave jaillit en cercle"
        ],
        ("lava","BUFF"): [
            "Poings serres, brulure interne visible",
            "Bras s'ecartent, la lave coule sur la peau sans bruler",
            "Posture large, aura de magma bouillonnant"
        ],
        ("lava","SUMMON"): [
            "Bras au-dessus de la tete, portail de lave",
            "Cercle de magma au sol",
            "Le titan de lave emerge"
        ],
        ("lava","ULTIMATE"): [
            "Posture large, le corps devient volcan",
            "Fissures de lave sur tout le corps",
            "Eruption totale, lave devastatrice liberee"
        ],
        ("necromancy","PROJECTILE"): [
            "Doigts recourbes, sphere violet-noir apparait",
            "La sphere tournoie, des ombres s'en echappent",
            "Mouvement de lancer, projectile ondoie vers la cible"
        ],
        ("necromancy","ZONE"): [
            "Doigts recourbes vers le bas, brume violette monte",
            "Rotation lente, bras s'elevent, brume s'epaissit",
            "Poussee vers le bas, zone d'ombre s'etend"
        ],
        ("necromancy","BUFF"): [
            "Doigts recourbes vers soi, invocation des ombres",
            "Les ombres rampant sur le corps du lanceur",
            "Posture voutee, l'ombre enveloppe le corps"
        ],
        ("necromancy","SUMMON"): [
            "Bras leves, invocation des morts",
            "Cercle noir au sol, main squelettique en emerge",
            "Le mort-vivant se leve du cercle"
        ],
        ("necromancy","ULTIMATE"): [
            "Posture voutee, le corps se deforme",
            "Les ombres aspirent la vie autour",
            "Relachement, le vide est libere"
        ],
        ("lumamancy","PROJECTILE"): [
            "Paumes vers le ciel, lumiere doree au-dessus",
            "La lumiere descend et se concentre dans la main",
            "Mouvement de lancer elegant, projectile lumineux file droit"
        ],
        ("lumamancy","ZONE"): [
            "Bras au ciel, lumiere doree qui eclaire tout",
            "La lumiere descend en cercle autour du lanceur",
            "Poussee des bras vers l'exterieur, zone sacree s'active"
        ],
        ("lumamancy","BUFF"): [
            "Mains jointes en priere, lumiere douce",
            "Mains s'ecartent, lumiere chaude emane du torse",
            "Bras ecartes, ailes de lumiere derriere le dos"
        ],
        ("lumamancy","SUMMON"): [
            "Bras leves, portail dore s'ouvre",
            "Lumieres qui descendent du portail",
            "L'etre de lumiere apparait"
        ],
        ("lumamancy","ULTIMATE"): [
            "Posture droite, rayon de lumiere du ciel",
            "Le corps s'eleve, baigne de lumiere",
            "Ailes deployees, le paradis est libere"
        ]
    }

    key = (school, stype)
    if key in gesture_map:
        parts = gesture_map[key]
        # Tier adjustment
        if tier >= 3 and len(parts) == 3:
            parts = [
                parts[0].replace("flamme", "brasier rugissant").replace("boule", "boule explosive") if school == "fire" else parts[0],
                parts[1].replace("bouche", "projectile") if school == "fire" else parts[1],
                parts[2].replace("boule", "projectile explosif").replace("Extension", "Rotation du buste + extension") if school == "fire" else parts[2]
            ]

        for i, part in enumerate(parts):
            lines.append(f"Geste T{i+1} | {part}")
    else:
        lines.append(f"Geste principal | A definir")

    # Special effects
    efe = {
        "burn": "Le corps s'embrase, flammes qui dansent",
        "freeze": "Givre qui apparait sur les avant-bras et s'etend",
        "slow": "Givre ou vent ralentissant autour de la cible",
        "heal": "Lueur verte de soin autour des mains",
        "stun": "Impact visible qui paralysse la cible",
        "knockback": "Onde de choc qui repousse",
        "chain": "Arcs electriques secondaires qui sautent de cible en cible",
        "summon": "Cercle d'invocation au sol qui brille",
        "flight": "Elan du corps vers le haut, ailes de vent/lumiere",
        "dash": "Traincee de mouvement, le corps se deplace rapidement",
        "absorb": "Bouclier qui scintille autour du lanceur",
        "damage_reduction": "Carapace d'energie qui renforce le corps",
        "thorns": "Pointes acerees qui poussent sur le corps du lanceur",
        "blindness": "Eclat lumineux aveuglant ou brume noire",
        "speed": "Traincee de vent/courant derriere le lanceur",
        "dispel": "Onde de choc purificatrice qui dissipe la magie",
        "fear": "Ombre terrifiante qui emane du lanceur",
        "poison": "Nuage ou gouttes toxiques violets",
        "pull": "Vortex qui attire les cibles vers le centre",
        "melee_bonus": "Armes ou mains enveloppees d'energie",
        "melee_fire_bonus": "Armes enflammees, traincee de feu",
    }
    for et in effects:
        if et in efe:
            lines.append(f"Effet special | {efe[et]}")

    if tier >= 4:
        lines.append(f"Particularite T{tier} | Mouvement plus ample, plus lent. Pose de recuperation apres l'animation (1 seconde d'immobilite)")
    if tier == 5:
        lines.append("Particularite ULTIME | L'animation freeze le personnage 1 seconde apres la fin pour un effet dramatique")

    return '\n'.join(lines)

lines = []
lines.append("# Guide d'animations Sihriya - 252 sorts, 252 animations uniques")
lines.append("")
lines.append("Chaque fiche decrit : la posture de depart, le jeu de jambes, les 3 temps du geste principal, les effets visuels, et les particularites de tier.")
lines.append("")

for school, spell_list in by_school.items():
    icon = school_icons.get(school, "")
    lines.append(f"---")
    lines.append(f"# {icon} Ecole : {school} ({len(spell_list)} sorts)")
    lines.append("")

    for s in spell_list:
        etypes = ', '.join([e['type'] for e in s['effects']])
        tier = s['tier']
        stype = s['type']

        lines.append(f"## {s['id']}")
        lines.append(f"- **Tier** : {tier} ({tier_names[tier]}) | **Type** : {stype} | **Effets** : {etypes}")
        lines.append(f"- **Mana** : {s['manaCost']} | **CD** : {s['cooldown']} ticks | **Cast** : {s['castTime']} ticks")
        lines.append("")
        lines.append("| | Description |")
        lines.append("|---|-------------|")
        lines.append(gen_desc(s))
        lines.append("")

output = '\n'.join(lines)

with open(r'C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\docs\GUIDE_ANIMATIONS.md', 'w', encoding='utf-8') as f:
    f.write(output)

print(f"Fichier genere: docs/GUIDE_ANIMATIONS.md")
print(f"Nombre total de sorts: {len(spells)}")
print(f"Nombre de lignes: {len(output.splitlines())}")
