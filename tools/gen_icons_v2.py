"""
Sihriya Spell Icon Generator v2 - Enhanced matching with curated icon lists.
Uses game-icons.net SVGs with school-specific icon pools.
"""

import json, math, os, re, random, xml.etree.ElementTree as ET
from svg.path import parse_path
from PIL import Image, ImageDraw

ICON_SIZE = 32
COLS = 28
ROWS = 9

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
GAME_ICONS_DIR = os.path.join(SCRIPT_DIR, "game-icons", "icons-master")

SCHOOL_COLORS = {
    "fire":       (255,  69,   0),
    "water":      ( 51, 153, 255),
    "wind":       (144, 238, 144),
    "earth":      (139,  69,  19),
    "lightning":  (255, 215,   0),
    "ice":        (173, 216, 230),
    "lava":       (255,  34,   0),
    "necromancy": (123,   0, 204),
    "lumamancy":  (255, 216, 102),
}

SCHOOL_ORDER = ["fire", "water", "wind", "earth", "lightning", "ice", "lava", "necromancy", "lumamancy"]

# ========================================================================
# Curated icon pools per school - hand-picked from game-icons.net ~1088 candidates
# ========================================================================
CURATED_ICONS = {
    "fire": [
        "fire","flame","fireball","fire-bomb","fire-breath","fire-dash","fire-gem","fire-punch",
        "fire-ray","fire-ring","fire-shield","fire-shrine","fire-spell-cast","fire-tail","fire-wave",
        "fire-zone","fire-ace","fire-axe","fire-bottle","fire-bowl","fire-flower","fire-iris",
        "fire-silhouette","burn","blaze","campfire","celebration-fire","small-fire","flaming-arrow",
        "flint-spark","burning-blobs","burning-dot","burning-ember","burning-meteor","burning-round-shot",
        "burning-skull","fireflake","lantern-flame","candle-flame","fireplace","flamer","flamethrower",
        "flame","flame-claws","flame-spin","flame-tunnel","fluffy-flame","heat","volcano",
        "eruption","explosion-rays","bright-explosion","crowned-explosion","ion-cannon-blast",
        "spiky-explosion","firework-rocket","spark-spirit","blaster","laser-blast","laser-burst",
        "lava","inferno","infernal","wildfires",
    ],
    "water": [
        "water","water-bolt","water-bottle","water-diviner-stick","water-drop","water-flask",
        "water-fountain","water-gallon","water-gun","water-mill","water-polo","water-recycling",
        "water-splash","water-tank","water-tower","wave-crest","waves","wave-strike","drop",
        "droplets","droplet-splash","bubbles","boiling-bubbles","bubble-field","splash",
        "splashy-stream","spray","rain","fog","cloud-ring","mist","flood","tsunami","tide",
        "big-wave","sea-dragon","aqua","waterfall","ice","iceberg","icebergs","ice-bolt","ice-bomb",
        "ice-cube","ice-cubes","ice-shield","ice-spear","ice-spell-cast","frostfire",
        "frozen-arrow","frozen-ring","holy-water","healing","mouth-watering","grouped-drops",
        "pourinh-chalice","pouring-pot","chalice-drops","frozen","snowing","cold-heart",
        "health-potion","potion-ball","potion-of-madness","round-potion","standing-potion",
        "chemical-drop","medicine",
    ],
    "wind": [
        "wind-hole","windmill","windpump","wind-slap","windsock","wind-turbine","windy-stripes",
        "windchimes","feather","feathered-wing","wing-cloak","winged-arrow","winged-emblem",
        "winged-scepter","winged-shield","winged-sword","wingfoot","feather-wound",
        "two-feathers","spear-feather","tornado","half-tornado","tornado-discs","whirlwind",
        "stomp-tornado","pentarrows-tornado","cloud-ring","cloud","cirrus","sky","wing",
        "fairy-wings","angel-wings","fluffy-wing","spiky-wing","liberty-wing","bat-wing",
        "batwing-emblem","air","gust","breeze","storm","thunder","sound-waves","wave",
        "airy","upgrade","speed","fast","arrow-wings","plane-wing","feather",
        "bird","bird-claw","bird-limb","bird-mask","bird-scepter","bird-twitter",
        "swallow","sparrow","hummingbird","flying","flight","soar","ascend",
    ],
    "earth": [
        "stone","rock","mountain","cliff","crystal","crystal-ball","crystal-bars","crystal-cluster",
        "crystal-earrings","crystal-eye","crystal-growth","crystal-shine","crystal-shrine",
        "crystal-wand","diamond","diamond-hard","diamond-hilt","diamond-ring","diamonds",
        "diamond-trophy","big-diamond-ring","cut-diamond","striking-diamonds",
        "earth-crack","earth-spit","earth-worm","boulder","stone-block","stone-bridge",
        "stone-bust","stone-crafting","stone-path","stone-pile","stone-spear","stone-sphere",
        "stone-stack","stone-tablet","stone-throne","stone-tower","stone-wall","stone-wheel",
        "boulder-dash","falling-rocks","rock","rock-golem","metal-golem-head",
        "golem-head","golem","mineral-heart","mineral-pearls","ore","gem","gem-chain",
        "gem-necklace","gem-pendant","gems","crystalize","floating-crystal",
        "geode","stalactites","tremor","quake","quake-stomp","fissure","shatter",
        "shatter-glass","shattered-sword","crush","weight-crush","magnet",
        "magnet-blast","mountain-cave","stone-axe","dripping-stone",
    ],
    "lightning": [
        "lightning-arc","lightning-bow","lightning-branches","lightning-dissipation",
        "lightning-dome","lightning-electron","lightning-flame","lightning-frequency",
        "lightning-helix","lightning-mask","lightning-saber","lightning-shadow",
        "lightning-shield","lightning-shout","lightning-slashes","lightning-spanner",
        "lightning-storm","lightning-tear","lightning-tree","lightning-trio",
        "bolt","bolter-gun","bolt-bomb","bolt-cutter","bolt-drop","bolt-eye",
        "bolt-saw","bolt-shield","bolt-spell-cast","arcing-bolt","plasma-bolt",
        "bottled-bolt","chain-lightning","heavy-lightning","power-lightning",
        "focused-lightning","sonic-lightning","spark","sparkles","spark-spirit",
        "spark-plug","sparky-bomb","circle-sparks","flint-spark","laser-sparks",
        "thunder","thunderball","thunder-blade","thunder-skull","thunder-struck",
        "lightning","electric","electrical","storm","thunderstorm","zap","shock",
        "charged-arrow","energy-arrow","energy-shield","energy-sword","static",
        "static-waves","plasma","discharge","flash","flash-grenade","speed",
        "acceleration","fast-forward","power","laser-blast","laserburn","laser-burst",
        "beam-satellite","beams-aura","beam-wake","ringed-beam",
    ],
    "ice": [
        "ice","iceberg","icebergs","ice-bolt","ice-bomb","ice-cube","ice-cubes",
        "ice-golem","ice-iris","ice-shield","ice-skate","ice-spear","ice-spell-cast",
        "frost","frostfire","frost-aura","frozen","frozen-arrow","frozen-ring",
        "freeze","cold","cold-heart","cold-snap","chill","icicle","icicles-aura",
        "hail","hail-storm","blizzard","snow","snowing","snow-crystal","snow-flake",
        "snow-storm","glacier","glacial","shard","shard-sword","crystal","crystal-bars",
        "crystal-cluster","crystal-shine","floating-crystal","melting-ice-cube",
        "ice-cream-cone","ice-cream-scoop","ice-pop","ice-skate","polar-star",
        "winter","absolute-zero","cryo","frozen-heart","skull-slices",
        "diamond-prison","diamond","diamond-hard","stalactites",
        "crystal-eye","crystal-growth","crystalize",
    ],
    "lava": [
        "lava","volcano","smoking-volcano","eruption","fire","fire-bomb","fire-bottle",
        "fire-bowl","fire-breath","fire-dash","fire-punch","fire-silhouette","fire-axe",
        "magma","magma-armor","magma-heart","magma-shield","magma-golem","magma-grenade",
        "molten","melt","melted","heat","heat-haze","burn","burning-blobs","burning-dot",
        "burning-ember","burning-meteor","burning-round-shot","burning-skull",
        "blast","blaster","explosion","explosion-rays","spiky-explosion","crowned-explosion",
        "corner-explosion","goo-explosion","bright-explosion","ion-cannon-blast",
        "mine-explosion","burst","burst-blob","smoke-bomb","smoke","ash",
        "steam-blast","flame","flame-claws","flame-spin","flame-tunnel",
        "flamethrower","flamer","fire-spell-cast","fire-ring","fire-wave",
        "fire-zone","firework-rocket","lava-flow","cinder","ember",
        "inferno","infernal-pillar","volcanic","volcanic-rock",
    ],
    "necromancy": [
        "skull","skull-bolt","skull-crack","skull-crossed-bones","skull-in-jar","skull-mask",
        "skull-ring","skull-sabertooth","skull-shield","skull-signet","skull-slices",
        "skull-staff","skull-with-syringe","skulls","triple-skulls","alien-skull",
        "animal-skull","broken-skull","burning-skull","cracked-alien-skull",
        "daemon-skull","desert-skull","dread-skull","fanged-skull","goo-skull",
        "happy-skull","harry-potter-skull","horned-skull","leaky-skull",
        "medal-skull","piece-skull","pirate-skull","sharped-teeth-skull",
        "spade-skull","stoned-skull","surprised-skull","tentacles-skull",
        "thunder-skull","t-rex-skull","william-tell-skull","condylura-skull",
        "diablo-skull","crowned-skull","death","death-juice","death-skull",
        "death-star","ghost","ghost-ally","floating-ghost","spirit","soul",
        "soul-vessel","ribcage","skeletal-hand","bone","hand","hand-of-god",
        "severed-hand","evil-hand","shadow","shadow-follower","shadow-grasp",
        "darkness","grave","graveyard","hasty-grave","grave-flowers","tombstone",
        "tombstone","curse","cursed-star","evil","evil-book","evil-eyes",
        "evil-moon","evil-tower","evil-wings","bat","bat","bat-blade","bat-mask",
        "moon-bats","swamp-bat","bat-wing","batwing-emblem","evil-bat","spider",
        "spider-alt","spider-bot","spider-eye","spider-face","spider-mask",
        "spider-web","angular-spider","hanging-spider","long-legged-spider",
        "masked-spider","poison","poison-bottle","poison-cloud","poison-gas",
        "soul","soul-vessel","blood","bleeding-eye","bleeding-heart","bloody-stash",
        "bloody-sword","drain","marrow-drain","decay","rot","corruption",
        "scythe","soul-scythe","necromancy","undead","zombie","wraith",
        "spectral","phantom","apparition",
    ],
    "lumamancy": [
        "star","star-altar","star-cycle","star-flag","star-formation","star-gate",
        "star-key","star-medal","star-prominences","star-pupil","star-satellites",
        "star-shuriken","star-skull","stars-stack","star-struck","star-swirl",
        "allied-star","barbed-star","beveled-star","dripping-star","falling-star",
        "flat-star","flexible-star","flower-star","knocked-out-stars",
        "law-star","moebius-star","ninja-star","north-star-shuriken",
        "polar-star","rainbow-star","round-star","sea-star","seven-pointed-star",
        "wrapping-star","light","light-bulb","lighthouse","light-projector",
        "sun","sunbeams","sun-cloud","sundial","sunflower","sun-priest",
        "sun-radiations","sunrise","sunrise","sunset","sun-spear",
        "azure-calendar-sun","barbed-sun","boomerang-sun","forward-sun",
        "gooey-eyed-sun","heraldic-sun","striped-sun","ubisoft-sun",
        "healing","healing-shield","holy","holy-grail","holy-hand-grenade",
        "holy-oak","holy-symbol","holy-water","angel-outfit","angel-wings",
        "angel-wings","heaven-gate","bless","blessing","prayer","prayer-beads",
        "shining-claw","shining-heart","shining-sword","glow","glowing-artifact",
        "glowing-hands","aura","beams-aura","book-aura","icicles-aura",
        "rear-aura","beam","beam-satellite","beam-wake","bright-explosion",
        "ray","ray-gun","sun-ray","light-beam","radiant","dazzling",
        "blinding","purification","cleanse","portal","magic-portal","magic-gate",
        "heaven-gate","star-gate","temple-gate","gate","open-gate","triple-gate",
        "diamond","diamond-hard","cut-diamond","crystal","crystal-ball",
        "crystal-shine","crystal-shrine","crystal-wand","crystalize",
        "book-aura","book-cover","bookshelf","open-book","spell-book",
        "white-book","secret-book","scroll-quill","scroll-unfurled",
        "enlightenment","miracle","miracle","salvation","redemption",
        "resurrection","revive",
    ],
}

# Shared pool for fallback (any leftover good icons)
SHARED_POOL = [
    "shield","shield-bash","shield-bounces","shieldcomb","shield-disabled","shield-echoes",
    "shield-impact","shield-opposition","shield-reflect","arrow","arrow-cluster","arrow-cursor",
    "arrow-dunk","arrow-flights","arrowhead","arrow-scope","arrows-shield","arrow-wings",
    "barbed-arrow","bow-arrow","branch-arrow","broadhead-arrow","broken-arrow","chained-arrow-heads",
    "charged-arrow","chemical-arrow","crosshair-arrow","cupidon-arrow","energy-arrow",
    "fast-arrow","flaming-arrow","frayed-arrow","heavy-arrow","interleaved-arrows",
    "lob-arrow","paper-arrow","plain-arrow","rapidshare-arrow","return-arrow","save-arrow",
    "slicing-arrow","smash-arrows","spine-arrow","spiral-arrow","split-arrows",
    "spotted-arrowhead","striking-arrows","supersonic-arrow","target-arrows",
    "thorned-arrow","tron-arrow","wide-arrow-dunk","zig-arrow","circle","circle-cage",
    "plain-circle","meeple-circle","swirl-ring","ring","ring-box","ringed-tentacle",
    "ring-mould","linked-rings","perpendicular-rings","power-ring","transportation-rings",
    "armor","armor-blueprint","armor-cuisses","armor-downgrade","armored-boomerang",
    "armored-pants","armor-punch","armor-upgrade","armor-vest","belt-armor","cape-armor",
    "chest-armor","layered-armor","leather-armor","leg-armor","ninja-armor","ribbon-shield",
    "riot-shield","roman-shield","rosa-shield","round-shield","shield","shoulder-armor",
    "spiked-armor","spiked-shoulder-armor","trench-body-armor","tribal-shield",
    "viking-shield","heart","heart-armor","heart-battery","heart-beats","heart-bottle",
    "heartburn","heart-drop","heart-earrings","heart-inside","heart-key","heart-minus",
    "heart-necklace","heart-organ","heart-plus","hearts","heart-shield","heart-stake",
    "heart-tower","heart-wings","ball-heart","bleeding-heart","broken-heart",
    "broken-heart-zone","centaur-heart","chained-heart","chewed-heart","cold-heart",
    "crowned-heart","glass-heart","half-heart","locked-heart","mineral-heart",
    "nested-hearts","opposite-hearts","paw-heart","pierced-heart","shattered-heart",
    "shining-heart","techno-heart","templar-heart","tentacle-heart","wrapped-heart",
    "potion","health-potion","potion-ball","potion-of-madness","round-potion","standing-potion",
    "magic-potion","pouring-chalice","pouring-pot","chalice-drops","flask",
    "rune-stone","rune-sword","glyph","sacred","magic","magic-gate","magic-portal",
    "magic-shield","spell-book","spell-cast","bolt-spell-cast","crystal-wand",
    "sorcery","enchant",
]

# ========================================================================
# SVG rendering
# ========================================================================
def render_svg_to_image(svg_path, target_size, color):
    tree = ET.parse(svg_path)
    root = tree.getroot()
    vb = root.get('viewBox')
    if vb:
        parts = [float(x) for x in vb.split()]
        svg_w, svg_h = parts[2], parts[3]
    else:
        svg_w = float(root.get('width', 512))
        svg_h = float(root.get('height', 512))

    ns = {'svg': 'http://www.w3.org/2000/svg'}
    path_els = root.findall('.//svg:path', ns)
    if not path_els:
        path_els = root.findall('.//{http://www.w3.org/2000/svg}path')

    path_data = []
    for p in path_els:
        d = p.get('d', '')
        fill = p.get('fill', '#fff').lower()
        if d and ('m0 0' not in d[:6].lower() and 'm0,0' not in d[:6].lower()):
            if fill in ('#fff', '#ffffff', '#fff', 'white', '#fff') or fill == 'none':
                path_data.append(d)

    if not path_data:
        for p in path_els:
            d = p.get('d', '')
            if d and 'm0 0' not in d[:6].lower():
                path_data.append(d)

    if not path_data:
        return None

    scale = 4
    render_size = target_size * scale
    img = Image.new("RGBA", (render_size, render_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    for d in path_data:
        try:
            parsed = parse_path(d)
            all_points = []
            for i in range(65):
                t = i / 64
                pt = parsed.point(t)
                all_points.append((pt.real, pt.imag))

            xs = [p[0] for p in all_points]
            ys = [p[1] for p in all_points]
            if not xs:
                continue
            min_x, max_x = min(xs), max(xs)
            min_y, max_y = min(ys), max(ys)
            pw = max_x - min_x
            ph = max_y - min_y
            if pw == 0 or ph == 0:
                continue

            pad = 0.05 * max(pw, ph)
            s = min((render_size - 2) / (pw + 2 * pad), (render_size - 2) / (ph + 2 * pad))

            scaled = [(
                int((x - min_x - pw / 2) * s + render_size / 2),
                int((y - min_y - ph / 2) * s + render_size / 2)
            ) for x, y in all_points]

            draw.polygon(scaled, fill=color + (255,))
        except Exception:
            continue

    img = img.resize((target_size, target_size), Image.LANCZOS)
    return img

# ========================================================================
# Build index of available SVGs
# ========================================================================
def build_svg_index(curated_names):
    index = {}
    for root, dirs, files in os.walk(GAME_ICONS_DIR):
        for f in files:
            if not f.endswith('.svg'):
                continue
            name = f[:-4]
            if name not in curated_names:
                continue
            path = os.path.join(root, f)
            tags = set(re.split(r'[-_\s]+', name.lower()))
            index[name] = {'path': path, 'tags': tags}
    return index

# ========================================================================
# Matching
# ========================================================================
def find_best_icon(spell, pool_icons, used_names):
    """Find best icon for a spell from the pool, avoiding used_names."""
    spell_id = spell["id"]
    school = spell["school"]
    stype = spell["type"]
    tier = spell["tier"]

    # Extract words from spell id
    parts = spell_id.replace('.', '-').lower().split('-')
    parts_set = set(parts)

    best = None
    best_score = -1

    for name, info in pool_icons.items():
        if name in used_names:
            continue

        svg_tags = info['tags']
        score = 0

        # Exact match on any word part
        for part in parts:
            if part == name:
                score += 80
            elif len(part) > 3 and part in name:
                score += 25
            elif name in part:
                score += 15

        # School keyword match
        school_kws = {
            "fire": ["fire","flame","burn","blaze","inferno","volcano","heat"],
            "water": ["water","aqua","wave","rain","drop","splash","ice","frost"],
            "wind": ["wind","air","gust","tornado","feather","wing","cloud"],
            "earth": ["earth","stone","rock","mountain","crystal","diamond","gem"],
            "lightning": ["lightning","thunder","bolt","spark","storm","plasma"],
            "ice": ["ice","frost","snow","cold","freeze","crystal","shard"],
            "lava": ["lava","volcano","magma","fire","explosion","blast","melt"],
            "necromancy": ["skull","death","ghost","soul","shadow","grave","bone","bat","spider","evil"],
            "lumamancy": ["star","light","sun","holy","angel","beam","ray","aura","heal","crystal"],
        }

        for kw in school_kws.get(school, []):
            if kw in svg_tags:
                score += 15

        # Type keywords
        type_kws = {
            "PROJECTILE": ["arrow","bolt","missile","shot","ray","beam","blast","spark","bullet"],
            "ZONE": ["circle","ring","wave","aura","field","nova","explosion","wall","bubble"],
            "BUFF": ["shield","armor","star","heart","boost","heal","cross","power"],
            "SUMMON": ["gate","portal","diamond","crystal","pentacle","summon","skull","eye"],
            "ULTIMATE": ["crown","star","sun","moon","explosion","skull","nova","diamond"],
        }
        for kw in type_kws.get(stype, []):
            if kw in svg_tags:
                score += 8

        # Penalize very short names not matching
        if len(name) <= 2 and name not in parts_set:
            score -= 20

        # Slight uniqueness bonus for longer/more specific names
        score += min(len(name) * 0.5, 5)

        if score > best_score:
            best_score = score
            best = name

    # Fallback: any unused icon
    if best is None:
        for name in pool_icons:
            if name not in used_names:
                best = name
                break

    return best

# ========================================================================
# Drawing helpers
# ========================================================================
def draw_type_symbol(draw, cx, cy, stype, c):
    a = 80
    if stype == "PROJECTILE":
        s = 5; pts = [(cx, cy - s), (cx - 3, cy + 3), (cx + 3, cy + 3)]
        draw.polygon(pts, fill=c + (a,))
    elif stype == "ZONE":
        r = 4
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=c + (a,), width=1)
    elif stype == "BUFF":
        pts = []
        for i in range(10):
            angle = math.pi / 2 - i * math.pi / 5
            r = 5 if i % 2 == 0 else 2
            pts.append((cx + r * math.cos(angle), cy - r * math.sin(angle)))
        draw.polygon(pts, fill=c + (a,))
    elif stype == "SUMMON":
        s = 4; pts = [(cx, cy - s), (cx + s, cy), (cx, cy + s), (cx - s, cy)]
        draw.polygon(pts, fill=c + (a,))
    elif stype == "ULTIMATE":
        pts = []
        for i in range(6):
            angle = math.pi / 6 + i * math.pi / 3
            r = 5
            pts.append((cx + r * math.cos(angle), cy - r * math.sin(angle)))
        draw.polygon(pts, fill=c + (a,))

def draw_tier_dots(draw, cx, cy, tier, c):
    ds = 4
    sx = cx - (tier - 1) * ds // 2
    for i in range(tier):
        dx = sx + i * ds; dy = ICON_SIZE - 4
        draw.ellipse([dx - 1.5, dy - 1.5, dx + 1.5, dy + 1.5], fill=c + (220,))

# ========================================================================
# Main
# ========================================================================
def main():
    random.seed(42)

    spells_path = os.path.join(PROJECT_ROOT, "src", "main", "resources", "data", "sihriya", "spells.json")
    with open(spells_path, "r") as f:
        spells = json.load(f)

    # Build master set of all curated names
    all_curated = set()
    for school, icons in CURATED_ICONS.items():
        all_curated.update(icons)
    all_curated.update(SHARED_POOL)
    print(f"Curated icon names: {len(all_curated)}")

    # Build SVG index only for curated names
    svg_index = build_svg_index(all_curated)
    print(f"Found in game-icons pack: {len(svg_index)}")

    # Build per-school pools
    school_pools = {}
    for school in SCHOOL_ORDER:
        names = [n for n in CURATED_ICONS.get(school, []) if n in svg_index]
        pool = {n: svg_index[n] for n in names}
        school_pools[school] = pool
        print(f"  {school}: {len(pool)} icons")

    shared = {n: svg_index[n] for n in SHARED_POOL if n in svg_index}
    print(f"  shared: {len(shared)} icons")

    # Assign icons
    assignments = {}
    used_names = set()
    missed = []

    for school in SCHOOL_ORDER:
        school_spells = [s for s in spells if s["school"] == school]
        school_spells.sort(key=lambda s: (s["tier"], s["id"]))

        pool = dict(school_pools.get(school, {}))
        pool.update(shared)

        for spell in school_spells:
            name = find_best_icon(spell, pool, used_names)
            if name:
                assignments[spell["id"]] = {
                    'svg_name': name,
                    'svg_path': svg_index[name]['path'],
                }
                used_names.add(name)
            else:
                missed.append(spell["id"])
                assignments[spell["id"]] = None

    # Handle any missed
    if missed:
        print(f"\nWARNING: {len(missed)} spells without icons!")
        for sid in missed:
            # Assign any remaining unused icon
            for name, info in svg_index.items():
                if name not in used_names:
                    assignments[sid] = {'svg_name': name, 'svg_path': info['path']}
                    used_names.add(name)
                    print(f"  {sid} -> {name} (fallback)")
                    break

    # Generate atlas
    spells_by_school = {s: [] for s in SCHOOL_ORDER}
    for s in spells:
        if s["school"] in spells_by_school:
            spells_by_school[s["school"]].append(s)

    atlas_width = COLS * ICON_SIZE
    atlas_height = ROWS * ICON_SIZE
    atlas = Image.new("RGBA", (atlas_width, atlas_height), (0, 0, 0, 0))
    icon_map = {}

    for row_idx, school in enumerate(SCHOOL_ORDER):
        school_spells = spells_by_school.get(school, [])
        school_spells.sort(key=lambda s: (s["tier"], s["id"]))
        color = SCHOOL_COLORS.get(school, (200, 200, 200))
        bg_dark = tuple(int(v * 0.3) for v in color)
        bg_mid = tuple(int(v * 0.5) for v in color)

        for col_idx, spell in enumerate(school_spells):
            x = col_idx * ICON_SIZE
            y = row_idx * ICON_SIZE
            cx, cy = ICON_SIZE // 2, ICON_SIZE // 2

            icon = Image.new("RGBA", (ICON_SIZE, ICON_SIZE), (0, 0, 0, 0))
            icon_draw = ImageDraw.Draw(icon)

            bg_r = ICON_SIZE // 2 - 1
            for i in range(bg_r, 0, -1):
                t = i / bg_r
                r = int(bg_dark[0] + (bg_mid[0] - bg_dark[0]) * (1 - t))
                g = int(bg_dark[1] + (bg_mid[1] - bg_dark[1]) * (1 - t))
                b = int(bg_dark[2] + (bg_mid[2] - bg_dark[2]) * (1 - t))
                icon_draw.ellipse([cx - i, cy - i, cx + i, cy + i], fill=(r, g, b, 255))

            assign = assignments.get(spell["id"])
            svg_name = "none"
            if assign:
                svg_img = render_svg_to_image(assign['svg_path'], ICON_SIZE, color)
                if svg_img:
                    icon = Image.alpha_composite(icon, svg_img)
                svg_name = assign['svg_name']

            type_c = tuple(min(255, int(v * 1.8)) for v in color)
            draw_type_symbol(icon_draw, cx, cy, spell["type"], type_c)

            tier_c = tuple(min(255, int(v * 2.2)) for v in color)
            draw_tier_dots(icon_draw, cx, cy, spell["tier"], tier_c)

            bw = 1 + spell["tier"] // 2
            bc = tuple(min(255, int(v * (1.0 + spell["tier"] * 0.15))) for v in color)
            icon_draw.ellipse([cx - bg_r, cy - bg_r, cx + bg_r, cy + bg_r],
                              outline=bc + (180,), width=bw)

            atlas.paste(icon, (x, y), icon)
            icon_map[spell["id"]] = {
                "row": row_idx, "col": col_idx,
                "x": x, "y": y,
                "u": x / atlas_width, "v": y / atlas_height,
                "w": ICON_SIZE / atlas_width, "h": ICON_SIZE / atlas_height,
                "school": school, "svg": svg_name,
            }

    tex_dir = os.path.join(PROJECT_ROOT, "src", "main", "resources", "assets", "sihriya", "textures", "gui")
    os.makedirs(tex_dir, exist_ok=True)
    atlas.save(os.path.join(tex_dir, "spell_icons.png"))
    map_path = os.path.join(tex_dir, "spell_icon_map.json")
    with open(map_path, "w") as f:
        json.dump(icon_map, f, indent=2)

    total_spells = sum(len(v) for v in spells_by_school.values())
    print(f"\nDone! {total_spells} icons -> spell_icons.png")
    print(f"Map -> spell_icon_map.json")
    print(f"Atlas: {atlas_width}x{atlas_height} px")

if __name__ == "__main__":
    main()
