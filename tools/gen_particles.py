"""
Generate beautiful particle textures — 64x64 glow sparks with soft halo + sharp core.
Per-school colored. Also generates a shared white sprite sheet for animations.
"""
import os, math
from PIL import Image, ImageDraw, ImageFilter

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
PDIR = os.path.join(PROJECT_ROOT, "src", "main", "resources", "assets", "sihriya", "textures", "particle")

SZ = 64
CX, CY = SZ // 2, SZ // 2

SCHOOL_COLORS = {
    "fire":       (255,  69,   0),
    "water":      ( 51, 153, 255),
    "wind":       (144, 238, 144),
    "earth":      (139,  69,  19),
    "lightning":  (255, 215,   0),
    "ice":        (173, 216, 230),
    "lava":       (255,  34,   0),
    "necromancy": (140,  20, 220),
    "lumamancy":  (255, 220,  80),
}
ALIAS = {"necro": "necromancy", "lumi": "lumamancy"}


def make_particle(color, name):
    """Create a beautiful spark particle: soft radial glow + sharp core + 4-ray cross."""
    img = Image.new("RGBA", (SZ, SZ), (0, 0, 0, 0))

    # Layer 1: soft outer glow (large, faint)
    glow_outer = Image.new("RGBA", (SZ, SZ), (0, 0, 0, 0))
    d_outer = ImageDraw.Draw(glow_outer)
    for r in range(SZ//2-2, SZ//2-20, -1):
        t = r / (SZ//2)
        a = int(30 * (1-t)**3)
        if a > 0:
            d_outer.ellipse([CX-r, CY-r, CX+r, CY+r],
                          fill=(color[0], color[1], color[2], a))
    glow_outer = glow_outer.filter(ImageFilter.GaussianBlur(12))

    # Layer 2: medium glow ring
    glow_mid = Image.new("RGBA", (SZ, SZ), (0, 0, 0, 0))
    d_mid = ImageDraw.Draw(glow_mid)
    for r in range(SZ//2-5, 2, -1):
        t = r / (SZ//2)
        a = int(80 * (1-t)**2)
        if a > 0:
            d_mid.ellipse([CX-r, CY-r, CX+r, CY+r],
                         fill=(color[0], color[1], color[2], a))
    glow_mid = glow_mid.filter(ImageFilter.GaussianBlur(4))

    # Layer 3: sharp spark core (4-ray star)
    spark = Image.new("RGBA", (SZ, SZ), (0, 0, 0, 0))
    d_spark = ImageDraw.Draw(spark)
    # Main cross
    for angle in [0, 90]:
        ra = math.radians(angle)
        dx = math.cos(ra)
        dy = math.sin(ra)
        for i in range(SZ//2):
            t = i / (SZ//2)
            a = int(255 * (1-t)**1.5)
            if a > 0:
                x = int(CX + dx * i)
                y = int(CY + dy * i)
                d_spark.line([x, y, x, y], fill=(color[0], color[1], color[2], a), width=3)
                d_spark.line([x, y, x, y], fill=(color[0], color[1], color[2], a), width=3)
                # Opposite direction too
                x2 = int(CX - dx * i)
                y2 = int(CY - dy * i)
                d_spark.line([x2, y2, x2, y2], fill=(color[0], color[1], color[2], a), width=3)
    # Diagonal rays (fainter)
    for angle in [45, 135]:
        ra = math.radians(angle)
        dx = math.cos(ra)
        dy = math.sin(ra)
        for i in range(SZ//3):
            t = i / (SZ//3)
            a = int(160 * (1-t)**2)
            if a > 0:
                x = int(CX + dx * i)
                y = int(CY + dy * i)
                d_spark.line([x, y, x, y], fill=(color[0], color[1], color[2], a), width=2)
                x2 = int(CX - dx * i)
                y2 = int(CY - dy * i)
                d_spark.line([x2, y2, x2, y2], fill=(color[0], color[1], color[2], a), width=2)
    # Core bright dot
    d_spark.ellipse([CX-3, CY-3, CX+3, CY+3], fill=color+(255,))
    spark = spark.filter(ImageFilter.GaussianBlur(0.5))

    # Composite all layers
    result = Image.new("RGBA", (SZ, SZ), (0, 0, 0, 0))
    result = Image.alpha_composite(result, glow_outer)
    result = Image.alpha_composite(result, glow_mid)
    result = Image.alpha_composite(result, spark)

    return result


def generate():
    for school, color in SCHOOL_COLORS.items():
        img = make_particle(color, school)
        path = os.path.join(PDIR, f"glow_spark_{school}.png")
        img.save(path)
        print(f"  glow_spark_{school}.png ({SZ}x{SZ})")

    # Aliases
    import shutil
    for alias, full in ALIAS.items():
        shutil.copy2(os.path.join(PDIR, f"glow_spark_{full}.png"),
                     os.path.join(PDIR, f"glow_spark_{alias}.png"))

    print("Done! Particle textures generated.")


if __name__ == "__main__":
    generate()
