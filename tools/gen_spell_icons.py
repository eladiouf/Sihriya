"""
Sihriya Spell Icon Generator
Generates 252+ unique procedural spell icons as a sprite sheet.
Output: assets/sihriya/textures/gui/spell_icons.png + spell_icon_map.json
"""

import json, math, os
from PIL import Image, ImageDraw

ICON_SIZE = 32
COLS = 28
ROWS = 9

SCHOOL_COLORS_RGB = {
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

def darken(c, factor=0.4):
    return tuple(int(v * factor) for v in c)

def lighten(c, factor=1.6):
    return tuple(min(255, int(v * factor)) for v in c)

def with_alpha(c, a=255):
    return c + (a,)

def rgba(c, a=255):
    return (c[0], c[1], c[2], a)

def draw_arrow(draw, cx, cy, size, c):
    s = size * 0.35
    pts = [(cx, cy - s), (cx - s * 0.6, cy + s * 0.5), (cx + s * 0.6, cy + s * 0.5)]
    draw.polygon(pts, fill=rgba(c, 180))

def draw_circle_cross(draw, cx, cy, size, c):
    r = size * 0.35
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=rgba(c, 200), width=2)
    draw.line([cx - r, cy, cx + r, cy], fill=rgba(c, 180), width=1)
    draw.line([cx, cy - r, cx, cy + r], fill=rgba(c, 180), width=1)

def draw_star(draw, cx, cy, size, c):
    s = size * 0.38
    pts = []
    for i in range(10):
        angle = math.pi / 2 - i * math.pi / 5
        r = s if i % 2 == 0 else s * 0.4
        pts.append((cx + r * math.cos(angle), cy - r * math.sin(angle)))
    draw.polygon(pts, fill=rgba(c, 180))

def draw_diamond(draw, cx, cy, size, c):
    s = size * 0.35
    pts = [(cx, cy - s), (cx + s, cy), (cx, cy + s), (cx - s, cy)]
    draw.polygon(pts, fill=rgba(c, 180))
    inner = [(cx, cy - s * 0.5), (cx + s * 0.5, cy), (cx, cy + s * 0.5), (cx - s * 0.5, cy)]
    draw.polygon(inner, fill=rgba(c, 60))

def draw_hexagon(draw, cx, cy, size, c):
    s = size * 0.35
    pts = []
    for i in range(6):
        angle = math.pi / 6 + i * math.pi / 3
        pts.append((cx + s * math.cos(angle), cy - s * math.sin(angle)))
    draw.polygon(pts, fill=rgba(c, 200))
    inner = [(cx, cy - s * 0.6), (cx + s * 0.52, cy), (cx, cy + s * 0.6), (cx - s * 0.52, cy)]
    draw.polygon(inner, fill=rgba(c, 60))

TYPE_DRAW = {
    "PROJECTILE": draw_arrow,
    "ZONE":       draw_circle_cross,
    "BUFF":       draw_star,
    "SUMMON":     draw_diamond,
    "ULTIMATE":   draw_hexagon,
}

def generate_icon(spell_id, school, tier, spell_type, index, school_idx):
    img = Image.new("RGBA", (ICON_SIZE, ICON_SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    color = SCHOOL_COLORS_RGB.get(school, (128, 128, 128))
    cx, cy = ICON_SIZE // 2, ICON_SIZE // 2

    bg_dark = darken(color, 0.3)
    bg_mid = darken(color, 0.55)
    bg_radius = ICON_SIZE // 2 - 1

    for i in range(bg_radius, 0, -1):
        t = i / bg_radius
        r = int(bg_dark[0] + (bg_mid[0] - bg_dark[0]) * (1 - t))
        g = int(bg_dark[1] + (bg_mid[1] - bg_dark[1]) * (1 - t))
        b = int(bg_dark[2] + (bg_mid[2] - bg_dark[2]) * (1 - t))
        draw.ellipse([cx - i, cy - i, cx + i, cy + i], fill=(r, g, b, 255))

    glow = lighten(color, 1.3)
    draw.ellipse([cx - bg_radius, cy - bg_radius, cx + bg_radius, cy + bg_radius],
                 outline=rgba(glow), width=2)

    rune_angle = (index * 47) % 360
    rune_dist = ICON_SIZE * 0.22
    rx = cx + rune_dist * math.cos(math.radians(rune_angle))
    ry = cy + rune_dist * math.sin(math.radians(rune_angle))
    rune_c = lighten(color, 1.8)
    rs = 3
    draw.ellipse([rx - rs, ry - rs, rx + rs, ry + rs], fill=rgba(rune_c, 120))
    draw.line([cx, cy, rx, ry], fill=rgba(rune_c, 80), width=1)

    symbol_draw = TYPE_DRAW.get(spell_type)
    if symbol_draw:
        symbol_draw(draw, cx, cy, ICON_SIZE * 0.9, lighten(color, 2.0))

    tier_c = lighten(color, 2.2)
    dot_spacing = 4
    start_x = cx - (tier - 1) * dot_spacing // 2
    for i in range(tier):
        dx = start_x + i * dot_spacing
        dy = ICON_SIZE - 5
        draw.ellipse([dx - 1.5, dy - 1.5, dx + 1.5, dy + 1.5], fill=rgba(tier_c, 220))

    border_width = 1 + tier // 2
    border_c = lighten(color, 1.0 + tier * 0.15)
    draw.ellipse([cx - bg_radius, cy - bg_radius, cx + bg_radius, cy + bg_radius],
                 outline=rgba(border_c, 150), width=border_width)

    return img

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, ".."))
    spells_path = os.path.join(project_root, "src", "main", "resources", "data", "sihriya", "spells.json")

    with open(spells_path, "r") as f:
        spells = json.load(f)

    spells_by_school = {s: [] for s in SCHOOL_ORDER}
    for s in spells:
        school = s["school"]
        if school in spells_by_school:
            spells_by_school[school].append(s)

    atlas_width = COLS * ICON_SIZE
    atlas_height = ROWS * ICON_SIZE
    atlas = Image.new("RGBA", (atlas_width, atlas_height), (0, 0, 0, 0))

    icon_map = {}

    for row_idx, school in enumerate(SCHOOL_ORDER):
        school_spells = spells_by_school.get(school, [])
        school_spells.sort(key=lambda s: (s["tier"], s["id"]))
        for col_idx, spell in enumerate(school_spells):
            x = col_idx * ICON_SIZE
            y = row_idx * ICON_SIZE
            icon = generate_icon(
                spell_id=spell["id"],
                school=spell["school"],
                tier=spell["tier"],
                spell_type=spell["type"],
                index=col_idx,
                school_idx=row_idx,
            )
            atlas.paste(icon, (x, y), icon)
            icon_map[spell["id"]] = {
                "row": row_idx,
                "col": col_idx,
                "x": x,
                "y": y,
                "u": x / atlas_width,
                "v": y / atlas_height,
                "w": ICON_SIZE / atlas_width,
                "h": ICON_SIZE / atlas_height,
                "school": school,
            }

    texture_dir = os.path.join(project_root, "src", "main", "resources", "assets", "sihriya", "textures", "gui")
    os.makedirs(texture_dir, exist_ok=True)
    atlas_path = os.path.join(texture_dir, "spell_icons.png")
    atlas.save(atlas_path)

    map_path = os.path.join(texture_dir, "spell_icon_map.json")
    with open(map_path, "w") as f:
        json.dump(icon_map, f, indent=2)

    total = sum(len(v) for v in spells_by_school.values())
    print(f"Generated {total} icons -> {atlas_path}")
    print(f"Map file     -> {map_path}")
    print(f"Atlas: {atlas_width}x{atlas_height}px")

if __name__ == "__main__":
    main()
