"""
Sihriya VFX Texture Generator v2
Generates high-quality procedural VFX textures with Perlin noise, fractals, gradients.
Output: assets/sihriya/textures/vfx/*.png
"""

import json, math, os, struct, zlib
from noise import pnoise2, snoise2

ICON_SIZE = 32

OUT_DIR = os.path.join(os.path.dirname(__file__), "..",
    "src", "main", "resources", "assets", "sihriya", "textures", "vfx")

SCHOOL_COLORS = {
    "fire":       (255, 89, 0),
    "water":      (51, 153, 255),
    "wind":       (144, 238, 144),
    "earth":      (139, 69, 19),
    "lightning":  (255, 215, 0),
    "ice":        (173, 216, 230),
    "lava":       (255, 34, 0),
    "necromancy": (123, 0, 204),
    "lumamancy":  (255, 216, 102),
}

def write_png(path, pixels, w, h):
    """Write RGBA pixel data (list of (r,g,b,a) tuples) to a PNG file."""
    raw = b''
    for y in range(h):
        raw += b'\x00'
        for x in range(w):
            px = pixels[y * w + x]
            raw += struct.pack('BBBB', px[0], px[1], px[2], px[3])
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', ihdr)
    png += chunk(b'IDAT', zlib.compress(raw))
    png += chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(png)

def lerp(a, b, t):
    return a + (b - a) * t

def clamp(v):
    return max(0, min(255, int(v)))

def gen_beam_texture():
    """64x16 beam texture with Perlin noise glow."""
    w, h = 64, 16
    pixels = []
    for y in range(h):
        for x in range(w):
            dx = (x - w/2) / (w/2)
            dy = (y - h/2) / (h/2)
            edge = 1 - math.sqrt(dx*dx + dy*dy)
            noise = pnoise2(x * 0.1, y * 0.1, octaves=2)
            val = clamp((edge + noise * 0.15) * 255)
            pixels.append((val, val, val, clamp(edge * 255)))
    write_png(os.path.join(OUT_DIR, "beam.png"), pixels, w, h)
    print(f"  beam.png ({w}x{h})")

def gen_noise_texture():
    """64x64 noise texture for shader perturbation."""
    w, h = 64, 64
    pixels = []
    for y in range(h):
        for x in range(w):
            n = clamp((snoise2(x * 0.05, y * 0.05, octaves=4) * 0.5 + 0.5) * 255)
            pixels.append((n, n, n, 255))
    write_png(os.path.join(OUT_DIR, "noise.png"), pixels, w, h)
    print(f"  noise.png ({w}x{h})")

def gen_glow_ramp():
    """16x256 glow ramp for Fresnel shader."""
    w, h = 16, 256
    pixels = []
    for y in range(h):
        t = y / 255
        val = clamp(t * t * (3 - 2 * t) * 255)
        for x in range(w):
            pixels.append((val, val, val, clamp(t * 255)))
    write_png(os.path.join(OUT_DIR, "glow_ramp.png"), pixels, w, h)
    print(f"  glow_ramp.png ({w}x{h})")

def gen_lightning_texture():
    """32x64 lightning bolt texture."""
    w, h = 32, 64
    pixels = []
    for y in range(h):
        for x in range(w):
            center = abs(x - w/2) / (w/2)
            n = snoise2(x * 0.2, y * 0.3, octaves=3)
            glow = 1 - center * center + n * 0.2
            val = clamp(glow * 255)
            pixels.append((val, val, val, clamp(glow * 255)))
    write_png(os.path.join(OUT_DIR, "lightning.png"), pixels, w, h)
    print(f"  lightning.png ({w}x{h})")

def gen_school_aura(school, color):
    """64x64 aura texture per school."""
    w, h = 64, 64
    pixels = []
    for y in range(h):
        for x in range(w):
            dx = (x - w/2) / (w/2)
            dy = (y - h/2) / (h/2)
            d = math.sqrt(dx*dx + dy*dy)
            n = snoise2(x * 0.08, y * 0.08, octaves=3)
            ring = max(0, 1 - abs(d - 0.6) * 3) * (0.8 + n * 0.2)
            glow = max(0, 1 - d * 0.8) * 0.3
            val = ring + glow
            r = clamp(color[0] * val)
            g = clamp(color[1] * val)
            b = clamp(color[2] * val)
            a = clamp(val * 180)
            pixels.append((r, g, b, a))
    write_png(os.path.join(OUT_DIR, f"aura_{school}.png"), pixels, w, h)
    print(f"  aura_{school}.png ({w}x{h})")

def gen_school_shield(school, color):
    """256x256 shield texture per school."""
    w, h = 256, 256
    pixels = []
    for y in range(h):
        for x in range(w):
            dx = (x - w/2) / (w/2)
            dy = (y - h/2) / (h/2)
            d = math.sqrt(dx*dx + dy*dy)
            n = snoise2(x * 0.04, y * 0.04, octaves=2)
            angle = math.atan2(dy, dx)
            hex_pattern = abs(math.cos(angle * 3)) * 0.5 + 0.5
            shell = max(0, 1 - abs(d - 0.85) * 8) * (0.9 + n * 0.1)
            inner = max(0, 1 - d * 0.8) * 0.15 * hex_pattern
            val = shell + inner
            r = clamp(color[0] * val)
            g = clamp(color[1] * val)
            b = clamp(color[2] * val)
            a = clamp(val * 200)
            pixels.append((r, g, b, a))
    write_png(os.path.join(OUT_DIR, f"shield_{school}.png"), pixels, w, h)
    print(f"  shield_{school}.png ({w}x{h})")

def gen_school_rune(school, color):
    """128x128 rune texture per school."""
    w, h = 128, 128
    pixels = []
    for y in range(h):
        for x in range(w):
            dx = (x - w/2) / (w/2)
            dy = (y - h/2) / (h/2)
            d = math.sqrt(dx*dx + dy*dy)
            angle = math.atan2(dy, dx)
            n = snoise2(x * 0.06, y * 0.06, octaves=2)
            outer_ring = max(0, 1 - abs(d - 0.8) * 6)
            inner_ring = max(0, 1 - abs(d - 0.4) * 6)
            radial = abs(math.sin(angle * 6 + n)) * 0.5 + 0.5
            symbol = outer_ring * (0.8 + n * 0.2) + inner_ring * 0.5 + radial * 0.2 * max(0, 1 - d)
            r = clamp(color[0] * symbol * 1.2)
            g = clamp(color[1] * symbol * 1.2)
            b = clamp(color[2] * symbol * 1.2)
            a = clamp(symbol * 200)
            pixels.append((r, g, b, a))
    write_png(os.path.join(OUT_DIR, f"rune_{school}.png"), pixels, w, h)
    print(f"  rune_{school}.png ({w}x{h})")

def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Generating VFX textures...")
    gen_beam_texture()
    gen_noise_texture()
    gen_glow_ramp()
    gen_lightning_texture()
    for school, color in SCHOOL_COLORS.items():
        gen_school_aura(school, color)
        gen_school_shield(school, color)
        gen_school_rune(school, color)
    print(f"\nAll textures saved to {OUT_DIR}")

if __name__ == "__main__":
    main()
