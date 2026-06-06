"""
Sihriya Spell Icon Generator v2
Uses game-icons.net SVGs instead of procedural generation.
"""

import json, math, os, re, random, xml.etree.ElementTree as ET
from svg.path import parse_path
from PIL import Image, ImageDraw, ImageFilter

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
# 1. Build master list of all SVGs with keyword tags
# ========================================================================
def build_svg_index():
    index = {}   # name -> { path, tags }
    for root, dirs, files in os.walk(GAME_ICONS_DIR):
        for f in files:
            if not f.endswith('.svg'):
                continue
            name = f[:-4]
            path = os.path.join(root, f)
            tags = set(re.split(r'[-_\s]+', name.lower()))
            index[name] = {'path': path, 'tags': tags}
    return index

# ========================================================================
# 2. Keyword mapping for each spell
# ========================================================================
SCHOOL_KEYWORDS = {
    "fire":       ["fire","flame","flame","burn","spark","ember","inferno","volcano","heat","magma","blaze"],
    "water":      ["water","wave","aqua","drop","rain","splash","flood","tide","river","lake","sea","ocean","bubble"],
    "wind":       ["wind","air","gust","breeze","tornado","hurricane","storm","cloud","sky","feather","wing","whirlwind"],
    "earth":      ["stone","rock","earth","mountain","crystal","gem","diamond","mineral","cliff","boulder","sand","mud","ore"],
    "lightning":  ["lightning","thunder","bolt","shock","zap","spark","plasma","storm","strike","flash","speed"],
    "ice":        ["ice","frost","snow","cold","freeze","glacier","crystal","hail","winter","shard","chill"],
    "lava":       ["lava","volcano","magma","melt","molten","fire","blast","explosion","eruption","ash","cinder","furnace"],
    "necromancy": ["skull","bone","death","skeleton","ghost","spirit","shadow","darkness","grave","undead","curse","soul","poison","decay","drain","bat","evil"],
    "lumamancy":  ["light","holy","angel","star","sun","shine","glow","radiant","beam","ray","aura","bless","heal","bright","diamond","crystal","sparkle"],
}

TYPE_KEYWORDS = {
    "PROJECTILE": ["arrow","missile","bolt","shot","bullet","spark","blast","burst","ray","beam","strike"],
    "ZONE":       ["circle","ring","aura","wave","field","area","nova","explosion","burst","wall","barrier","cage","prison","bubble"],
    "BUFF":       ["shield","armor","buff","boost","power","strength","heart","star","cross","plus","upgrade","heal","regen"],
    "SUMMON":     ["summon","gate","portal","pentacle","diamond","crystal","eye","spawn","creature","beast","golem"],
    "ULTIMATE":   ["crown","star","sun","moon","eye","diamond","crystal","nova","explosion","skull","angel","winged"],
}

def score_svg_for_spell(svg_name, svg_tags, spell):
    """Score how well an SVG matches a spell."""
    score = 0
    spell_lower = spell["id"].lower().replace('.', '-')
    parts = set(spell_lower.split('-'))

    # Exact name match (highest priority)
    if svg_name == spell_lower:
        score += 100
    if spell_lower in svg_name or svg_name in spell_lower:
        score += 50

    # Spell name parts match SVG name
    for part in parts:
        if part in svg_name:
            score += 20

    # School keywords match SVG tags
    school = spell["school"]
    for kw in SCHOOL_KEYWORDS.get(school, []):
        if kw in svg_tags:
            score += 5

    # Type keywords match SVG tags
    for kw in TYPE_KEYWORDS.get(spell["type"], []):
        if kw in svg_tags:
            score += 3

    return score

def assign_icons(svg_index, spells):
    """Assign best SVG to each spell."""
    assignments = {}
    used_svgs = set()

    # First pass: exact or high-confidence matches
    for spell in spells:
        best_name = None
        best_score = -1
        for name, info in svg_index.items():
            if name in used_svgs:
                continue
            s = score_svg_for_spell(name, info['tags'], spell)
            if s > best_score:
                best_score = s
                best_name = name

        if best_name and best_score > 5:
            assignments[spell["id"]] = {
                'svg_name': best_name,
                'svg_path': svg_index[best_name]['path'],
            }
            used_svgs.add(best_name)
        else:
            assignments[spell["id"]] = None

    # Second pass: unassigned spells get random matching
    for spell in spells:
        if assignments[spell["id"]] is not None:
            continue
        best_name = None
        best_score = -1
        for name, info in svg_index.items():
            if name in used_svgs:
                continue
            s = score_svg_for_spell(name, info['tags'], spell)
            if s > best_score:
                best_score = s
                best_name = name

        if best_name and best_score >= 0:
            assignments[spell["id"]] = {
                'svg_name': best_name,
                'svg_path': svg_index[best_name]['path'],
            }
            used_svgs.add(best_name)
        else:
            # Last resort: any unused SVG
            for name, info in svg_index.items():
                if name not in used_svgs:
                    assignments[spell["id"]] = {
                        'svg_name': name,
                        'svg_path': info['path'],
                    }
                    used_svgs.add(name)
                    break

    return assignments

# ========================================================================
# 3. Render SVG path to Pillow image
# ========================================================================
def render_svg_to_image(svg_path, target_size, color):
    """Parse SVG path data and render to a colored RGBA image."""
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

    # Collect path data from all path elements
    path_data = []
    for p in path_els:
        d = p.get('d', '')
        fill = p.get('fill', '#fff')
        opacity = float(p.get('opacity', '1'))
        if d and (fill == '#fff' or fill == '#ffffff' or fill.startswith('#') or fill == 'none'):
            # Skip the background rect (usually no fill or black fill)
            if 'M0 0' in d[:10] or 'M0,0' in d[:10]:
                continue
            path_data.append(d)

    if not path_data:
        # Fallback: use all paths
        for p in path_els:
            d = p.get('d', '')
            if d and 'M0 0' not in d[:10] and 'M0,0' not in d[:10]:
                path_data.append(d)

    if not path_data:
        print(f"  WARNING: No valid paths found in {svg_path}")
        return None

    # Render to a high-res image first, then downscale
    scale = 4
    render_size = target_size * scale
    img = Image.new("RGBA", (render_size, render_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    for d in path_data:
        try:
            parsed = parse_path(d)
            # Convert path to polygon points
            points = []
            resolution = 64
            for i in range(resolution + 1):
                t = i / resolution
                pt = parsed.point(t)
                points.append((pt.real, pt.imag))

            # Scale to fit render_size
            xs = [p[0] for p in points]
            ys = [p[1] for p in points]
            if not xs:
                continue
            min_x, max_x = min(xs), max(xs)
            min_y, max_y = min(ys), max(ys)
            pw = max_x - min_x
            ph = max_y - min_y
            if pw == 0 or ph == 0:
                continue

            padding = 0.05 * max(pw, ph)
            scale_x = (render_size - 2) / (pw + 2 * padding)
            scale_y = (render_size - 2) / (ph + 2 * padding)
            s = min(scale_x, scale_y)

            scaled = [(
                int((x - min_x - pw/2) * s + render_size / 2),
                int((y - min_y - ph/2) * s + render_size / 2)
            ) for x, y in points]

            draw.polygon(scaled, fill=color + (255,))
        except Exception as e:
            print(f"  WARNING: Path parse error: {e}")
            continue

    # Downscale with anti-aliasing
    img = img.resize((target_size, target_size), Image.LANCZOS)
    return img

# ========================================================================
# 4. Main generation
# ========================================================================
def main():
    spells_path = os.path.join(PROJECT_ROOT, "src", "main", "resources", "data", "sihriya", "spells.json")
    with open(spells_path, "r") as f:
        spells = json.load(f)

    print(f"Building SVG index from {GAME_ICONS_DIR}...")
    svg_index = build_svg_index()
    print(f"  Found {len(svg_index)} SVGs")

    print("Assigning icons to spells...")
    assignments = assign_icons(svg_index, spells)

    # Stats
    assigned = sum(1 for v in assignments.values() if v is not None)
    print(f"  Assigned: {assigned}/{len(spells)}")

    # Group by school
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
            print(f"  {row_idx+1}/{ROWS} {school}/{col_idx+1}/{len(school_spells)}: {spell['id']}...", end=" ")
            x = col_idx * ICON_SIZE
            y = row_idx * ICON_SIZE

            # Create icon canvas
            icon = Image.new("RGBA", (ICON_SIZE, ICON_SIZE), (0, 0, 0, 0))
            icon_draw = ImageDraw.Draw(icon)
            cx, cy = ICON_SIZE // 2, ICON_SIZE // 2

            # Background circle with gradient
            bg_r = ICON_SIZE // 2 - 1
            for i in range(bg_r, 0, -1):
                t = i / bg_r
                r = int(bg_dark[0] + (bg_mid[0] - bg_dark[0]) * (1 - t))
                g = int(bg_dark[1] + (bg_mid[1] - bg_dark[1]) * (1 - t))
                b = int(bg_dark[2] + (bg_mid[2] - bg_dark[2]) * (1 - t))
                icon_draw.ellipse([cx - i, cy - i, cx + i, cy + i], fill=(r, g, b, 255))

            # Render SVG icon
            assign = assignments.get(spell["id"])
            if assign:
                svg_img = render_svg_to_image(assign['svg_path'], ICON_SIZE, color)
                if svg_img:
                    icon = Image.alpha_composite(icon, svg_img)
                print(f"{assign['svg_name']}")
            else:
                print("NO MATCH")

            # Type symbol overlay (subtle)
            type_c = tuple(min(255, int(v * 1.8)) for v in color)
            draw_type_symbol(icon_draw, cx, cy, spell["type"], type_c)

            # Tier dots
            tier_c = tuple(min(255, int(v * 2.2)) for v in color)
            draw_tier_dots(icon_draw, cx, cy, spell["tier"], tier_c)

            # Border
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
                "school": school, "svg": assign["svg_name"] if assign else None,
            }

    # Save
    tex_dir = os.path.join(PROJECT_ROOT, "src", "main", "resources", "assets", "sihriya", "textures", "gui")
    os.makedirs(tex_dir, exist_ok=True)
    atlas.save(os.path.join(tex_dir, "spell_icons.png"))
    with open(os.path.join(tex_dir, "spell_icon_map.json"), "w") as f:
        json.dump(icon_map, f, indent=2)

    total_spells = sum(len(v) for v in spells_by_school.values())
    print(f"\nDone! {total_spells} icons -> spell_icons.png + spell_icon_map.json")
    print(f"Atlas: {atlas_width}x{atlas_height} px")

def draw_type_symbol(draw, cx, cy, stype, c):
    """Draw subtle type symbol on icon."""
    a = 100  # alpha for subtle overlay
    if stype == "PROJECTILE":
        s = 6
        pts = [(cx, cy - s), (cx - 4, cy + 3), (cx + 4, cy + 3)]
        draw.polygon(pts, fill=c + (a,))
    elif stype == "ZONE":
        r = 5
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=c + (a,), width=1)
    elif stype == "BUFF":
        pts = []
        for i in range(10):
            angle = math.pi / 2 - i * math.pi / 5
            r = 6 if i % 2 == 0 else 2.5
            pts.append((cx + r * math.cos(angle), cy - r * math.sin(angle)))
        draw.polygon(pts, fill=c + (a,))
    elif stype == "SUMMON":
        s = 5
        pts = [(cx, cy - s), (cx + s, cy), (cx, cy + s), (cx - s, cy)]
        draw.polygon(pts, fill=c + (a,))
    elif stype == "ULTIMATE":
        pts = []
        for i in range(6):
            angle = math.pi / 6 + i * math.pi / 3
            r = 6
            pts.append((cx + r * math.cos(angle), cy - r * math.sin(angle)))
        draw.polygon(pts, fill=c + (a,))

def draw_tier_dots(draw, cx, cy, tier, c):
    dot_spacing = 4
    start_x = cx - (tier - 1) * dot_spacing // 2
    for i in range(tier):
        dx = start_x + i * dot_spacing
        dy = ICON_SIZE - 4
        draw.ellipse([dx - 1.5, dy - 1.5, dx + 1.5, dy + 1.5], fill=c + (220,))

if __name__ == "__main__":
    main()
