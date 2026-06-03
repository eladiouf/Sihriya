"""
4-layer animated magic circles with SGA (Minecraft Enchantment Font) runic writing.
Mystic scripture, astrological symbols, sacred geometry, unique center sigils.
"""
import os, math
from PIL import Image, ImageDraw, ImageFilter

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
ASSETS = os.path.join(PROJECT_ROOT, "src", "main", "resources", "assets", "sihriya", "textures", "magiccircle")

SZ = 1024
CX, CY = SZ // 2, SZ // 2


class Layer:
    def __init__(self):
        self.img = Image.new("RGBA", (SZ, SZ), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.img)

    def pol(self, r, deg):
        ra = math.radians(deg - 90)
        return (CX + r * math.cos(ra), CY + r * math.sin(ra))

    def ring(self, r, w=2, a=255):
        self.d.ellipse([CX-r, CY-r, CX+r, CY+r], outline=(255,255,255,a), width=w)

    def dash(self, r, w, seg, gap, a=255, ph=0):
        step = seg + gap; i = ph
        while i < 360:
            s, e = i, min(i+seg, 360)
            self.d.arc([CX-r, CY-r, CX+r, CY+r], math.radians(s-90),
                       math.radians(e-90), fill=(255,255,255,a), width=w)
            i += step

    def dot(self, r, deg, sz=3, a=255):
        x, y = self.pol(r, deg)
        self.d.ellipse([x-sz, y-sz, x+sz, y+sz], fill=(255,255,255,a))

    def dots(self, r, n, sz=3, ph=0, a=255):
        for i in range(n):
            self.dot(r, ph+i*360/n, sz, a)

    def line_p(self, r1, d1, r2, d2, w=2, a=255):
        x1, y1 = self.pol(r1, d1); x2, y2 = self.pol(r2, d2)
        self.d.line([x1, y1, x2, y2], fill=(255,255,255,a), width=w)

    def arc_p(self, r, start, end, w=2, a=255):
        self.d.arc([CX-r, CY-r, CX+r, CY+r],
                   math.radians(start-90), math.radians(end-90),
                   fill=(255,255,255,a), width=w)

    # ─── SGA (Standard Galactic Alphabet / Minecraft Enchantment Font) ───

    def _rot(self, dx, dy, deg):
        ra = math.radians(deg)
        return (dx*math.cos(ra)-dy*math.sin(ra), dx*math.sin(ra)+dy*math.cos(ra))

    def sga(self, r, deg, char_id, sz=16, a=255):
        """Draw a Standard Galactic Alphabet character at polar position."""
        x, y = self.pol(r, deg)
        s = sz

        def L(dx1, dy1, dx2, dy2, w=2):
            rx1, ry1 = self._rot(dx1, dy1, deg-90)
            rx2, ry2 = self._rot(dx2, dy2, deg-90)
            self.d.line([x+rx1*s, y+ry1*s, x+rx2*s, y+ry2*s], fill=(255,255,255,a), width=w)

        def R(dx1, dy1, dx2, dy2, w=2):
            rx1, ry1 = self._rot(dx1, dy1, deg-90)
            rx2, ry2 = self._rot(dx2, dy2, deg-90)
            x0, y0 = min(rx1, rx2)*s, min(ry1, ry2)*s
            x1, y1 = max(rx1, rx2)*s, max(ry1, ry2)*s
            self.d.rectangle([x+x0, y+y0, x+x1, y+y1],
                           outline=(255,255,255,a), width=w)

        # SGA glyphs (10 most recognizable from Minecraft's enchanting table)
        if char_id == 0:    # ᒲ — box with bottom bar
            R(-0.5, -0.8, 0.5, 0.8, 2)
            L(-0.5, 0.8, 0.5, 0.8, 2)
        elif char_id == 1:  # ᖋ — pill with bars
            R(-0.25, -0.8, 0.25, 0.8, 2)
            L(-0.25, -0.5, 0.25, -0.5, 2)
            L(-0.25, 0.5, 0.25, 0.5, 2)
        elif char_id == 2:  # ᓵ — backward C with cross
            L(-0.4, -0.8, -0.4, 0.8, 2)
            L(-0.4, -0.8, 0.3, -0.8, 2)
            L(-0.4, 0.8, 0.3, 0.8, 2)
            L(0.3, -0.5, 0.3, 0.5, 2)
        elif char_id == 3:  # ᒣ — box with center dot
            R(-0.5, -0.8, 0.5, 0.8, 2)
            self.d.ellipse([x-2, y-2, x+2, y+2], fill=(255,255,255,a))
        elif char_id == 4:  # E — 3 horiz + vertical
            L(0, -1, 0, 1, 2)
            L(0, -0.8, 0.4, -0.8, 2)
            L(0, 0, 0.4, 0, 2)
            L(0, 0.8, 0.4, 0.8, 2)
        elif char_id == 5:  # ᒥ — square, vertical through top
            R(-0.4, -0.3, 0.4, 0.8, 2)
            L(-0.4, -0.3, 0, -1.0, 2)
        elif char_id == 6:  # ᒷ — square, bars top+bottom
            R(-0.4, -0.5, 0.4, 0.5, 2)
            L(-0.5, -0.8, 0.5, -0.8, 2)
            L(-0.5, 0.8, 0.5, 0.8, 2)
        elif char_id == 7:  # X — diagonal cross
            L(-0.6, -0.8, 0.6, 0.8, 2)
            L(0.6, -0.8, -0.6, 0.8, 2)
        elif char_id == 8:  # Y — V + vertical
            L(-0.5, -0.6, 0, -1.0, 2)
            L(0.5, -0.6, 0, -1.0, 2)
            L(0, -0.6, 0, 0.8, 2)
        elif char_id == 9:  # Z — zigzag
            L(-0.4, -0.8, 0.4, -0.8, 2)
            L(0.4, -0.8, -0.4, 0.8, 2)
            L(-0.4, 0.8, 0.4, 0.8, 2)
        elif char_id == 10: # N — vertical + diagonal
            L(-0.4, -0.8, -0.4, 0.8, 2)
            L(-0.4, -0.8, 0.4, 0.8, 2)
            L(0.4, -0.8, 0.4, 0.8, 2)
        elif char_id == 11: # W — W shape
            L(-0.5, -0.8, -0.2, 0.5, 2)
            L(-0.2, 0.5, 0, -0.3, 2)
            L(0, -0.3, 0.2, 0.5, 2)
            L(0.2, 0.5, 0.5, -0.8, 2)
        elif char_id == 12: # P — P-like
            L(-0.4, -0.8, -0.4, 0.8, 2)
            L(-0.4, -0.8, 0.3, -0.8, 2)
            L(0.3, -0.8, 0.3, -0.1, 2)
            L(-0.4, -0.1, 0.3, -0.1, 2)
        elif char_id == 13: # K — left vert + right chevron  
            L(-0.4, -0.8, -0.4, 0.8, 2)
            L(-0.4, 0, 0.4, -0.8, 2)
            L(-0.4, 0, 0.4, 0.8, 2)
        elif char_id == 14: # T — T shape
            L(0, -0.8, 0, 0.8, 2)
            L(-0.5, -0.8, 0.5, -0.8, 2)
        elif char_id == 15: # L — L shape  
            L(-0.4, -0.8, -0.4, 0.8, 2)
            L(-0.4, 0.8, 0.4, 0.8, 2)

    def sga_ring(self, r, n, char_ids, sz=16, a=255, ph=0):
        for i in range(n):
            self.sga(r, ph+i*360/n, char_ids[i%len(char_ids)], sz, a)

    # ─── MYSTIC SCRIPTURE ───

    def scripture(self, r, count, w=1, a=255, ph=0):
        """Draw arc segments that look like mystical text."""
        for i in range(count):
            deg = ph + i*360/count
            self.arc_p(r, deg-2.5, deg+2.5, w, a)
            if i % 3 == 0:
                self.dot(r-6, deg, 1, a//2)
            if i % 4 == 0:
                self.dot(r+5, deg+3, 1, a//2)

    # ─── ASTROLOGICAL SYMBOLS ───

    def sym(self, r, deg, sid, sz=20, a=255):
        x, y = self.pol(r, deg); s = sz; col = (255,255,255,a)

        if sid == 0:  # ☉
            self.d.ellipse([x-s, y-s, x+s, y+s], outline=col, width=3)
            self.d.ellipse([x-s//3, y-s//3, x+s//3, y+s//3], fill=col)
        elif sid == 1:  # ☽
            self.d.ellipse([x-s, y-s, x+s, y+s], outline=col, width=3)
            self.d.ellipse([x-s//3, y-s, x+s, y+s], fill=(0,0,0,0), outline=col, width=2)
        elif sid == 2:  # ♂
            self.d.ellipse([x-s, y-s, x+s, y+s], outline=col, width=3)
            self.d.line([x+s//2, y-s//2, x+int(s*2.2), y-int(s*2.2)], fill=col, width=3)
        elif sid == 3:  # ♀
            self.d.ellipse([x-s, y-s, x+s, y+s], outline=col, width=3)
            self.d.line([x, y+s, x, y+int(s*2.2)], fill=col, width=3)
            self.d.line([x-int(s*0.6), y+int(s*1.6), x+int(s*0.6), y+int(s*1.6)], fill=col, width=2)
        elif sid == 4:  # ♃
            self.d.line([x-int(s*0.5), y-s, x-int(s*0.5), y+s], fill=col, width=3)
            self.d.line([x-int(s*0.5), y-s, x+s, y-s], fill=col, width=3)
            self.d.line([x-int(s*0.5), y+s//3, x+int(s*0.7), y+s//3], fill=col, width=3)
        elif sid == 5:  # ☿
            self.d.ellipse([x-s, y-s, x+s, y+s], outline=col, width=3)
            self.d.line([x-s//3, y-s//2, x, y-int(s*1.8)], fill=col, width=2)
            self.d.line([x+s//3, y-s//2, x, y-int(s*1.8)], fill=col, width=2)
            self.d.line([x, y+s, x, y+int(s*2.2)], fill=col, width=3)
            self.d.line([x-int(s*0.6), y+int(s*1.6), x+int(s*0.6), y+int(s*1.6)], fill=col, width=2)
        elif sid == 6:  # ♄
            self.d.line([x-int(s*0.5), y-int(s*1.5), x+int(s*0.5), y-int(s*1.5)], fill=col, width=3)
            self.d.line([x, y-int(s*1.5), x, y-int(s*0.3)], fill=col, width=3)
            self.d.arc([x-s, y-s*0.3, x+s, y+s], math.radians(80), math.radians(280), fill=col, width=3)
        elif sid == 7:  # △
            pts = [(x, y-int(s*1.3)), (x-int(s*1.1), y+int(s*0.8)), (x+int(s*1.1), y+int(s*0.8))]
            self.d.polygon(pts, outline=col, width=3)
        elif sid == 8:  # ▽
            pts = [(x, y+int(s*1.3)), (x-int(s*1.1), y-int(s*0.8)), (x+int(s*1.1), y-int(s*0.8))]
            self.d.polygon(pts, outline=col, width=3)
        elif sid == 9:  # ✡
            for mul in [1, -1]:
                pts = [(x, y+int(s*1.25)*mul), (x+int(s*1.08), y-int(s*0.63)*mul), (x-int(s*1.08), y-int(s*0.63)*mul)]
                self.d.polygon(pts, outline=col, width=3)
        elif sid == 10:  # ⛤
            pts = [(x+s*math.cos(math.radians(d-90)), y+s*math.sin(math.radians(d-90))) for d in [54,126,198,270,342]]
            self.d.polygon(pts, outline=col, width=3)
        elif sid == 11:  # ◆
            pts = [(x, y-int(s*1.3)), (x+int(s*0.8), y), (x, y+int(s*1.3)), (x-int(s*0.8), y)]
            self.d.polygon(pts, outline=col, width=3)
        elif sid == 12:  # ✚
            self.d.line([x-int(s*0.6), y, x+int(s*0.6), y], fill=col, width=3)
            self.d.line([x, y-int(s*0.6), x, y+int(s*0.6)], fill=col, width=3)
        elif sid == 13:  # ○
            self.d.ellipse([x-int(s*0.7), y-int(s*0.7), x+int(s*0.7), y+int(s*0.7)], outline=col, width=3)
        elif sid == 14:  # 👁
            self.d.ellipse([x-int(s*1.2), y-int(s*0.5), x+int(s*1.2), y+int(s*0.5)], outline=col, width=3)
            self.d.ellipse([x-int(s*0.3), y-int(s*0.4), x+int(s*0.3), y+int(s*0.4)], fill=col)
        elif sid == 15:  # spiral
            for i in range(30):
                t1, t2 = i/30, (i+1)/30; r1, r2 = s*0.15+t1*s*0.9, s*0.15+t2*s*0.9
                a1, a2 = t1*540-90, t2*540-90
                self.d.line([x+r1*math.cos(math.radians(a1)), y+r1*math.sin(math.radians(a1)),
                           x+r2*math.cos(math.radians(a2)), y+r2*math.sin(math.radians(a2))], fill=col, width=3)
        elif sid == 16:  # ☠
            self.d.ellipse([x-int(s*0.7), y-int(s*0.5), x+int(s*0.7), y+int(s*0.5)], outline=col, width=3)
            self.d.ellipse([x-int(s*0.25), y-int(s*0.15), x-int(s*0.05), y+int(s*0.1)], fill=col)
            self.d.ellipse([x+int(s*0.05), y-int(s*0.15), x+int(s*0.25), y+int(s*0.1)], fill=col)
            self.d.line([x-int(s*0.25), y+int(s*0.3), x+int(s*0.25), y+int(s*0.3)], fill=col, width=2)
        elif sid == 17:  # ⚡
            pts = [(x-int(s*0.2), y-int(s*1.2)), (x+int(s*0.4), y-int(s*0.3)), (x-int(s*0.3), y-int(s*0.3)),
                   (x+int(s*0.2), y+int(s*1.2)), (x+int(s*0.05), y+int(s*0.3)), (x-int(s*0.05), y+int(s*0.3))]
            self.d.polygon(pts, outline=col, width=3)
        elif sid == 18:  # 〰
            for j in range(3):
                self.d.arc([x-int(s*0.8+j*6), y-int(s*0.8+j*6), x+int(s*0.8+j*6), y+int(s*0.8+j*6)],
                          math.radians(15), math.radians(165), fill=col, width=3)

    def sym_ring(self, r, n, sids, sz=20, a=255, ph=0):
        for i in range(n):
            self.sym(r, ph+i*360/n, sids[i%len(sids)], sz, a)


# ═══════════════════════════════ LAYERS ═══════════════════════════════

def make_layer0(school, sga_ids, a=150):
    """SGA runes + scripture ring — rotates clockwise (slow)."""
    L = Layer()
    r = 440
    L.ring(r, 4, a)
    L.ring(r-8, 1, a-30)
    L.dash(r-4, 1, 3, 6, a-50)
    L.dots(r, 36, 1, a=a-60)
    # SGA enchantment characters around the ring
    L.sga_ring(r, 16, sga_ids, sz=16, a=a, ph=11)
    L.scripture(r-20, 48, w=1, a=a-60, ph=3)
    L.scripture(r+15, 36, w=1, a=a-70, ph=8)
    return L.img

def make_layer1(school, sym_ids, a=160):
    """Astrological symbols — rotates counter-clockwise (medium)."""
    L = Layer()
    r = 330
    L.ring(r+6, 3, a)
    L.ring(r-6, 3, a-20)
    L.dash(r, 1, 5, 5, a-40, ph=5)
    L.dots(r, 24, 2, a=a-50, ph=3)
    L.sym_ring(r, 12, sym_ids, sz=18, a=a, ph=15)
    L.scripture(r-18, 60, w=1, a=a-80, ph=7)
    return L.img

def make_layer2(school, a=170):
    """Sacred geometry — rotates clockwise (fast)."""
    L = Layer()
    r = 230
    L.ring(r+8, 4, a)
    L.ring(r-8, 4, a)
    L.dash(r, 2, 8, 3, a-20, ph=0)
    L.dots(r, 16, 3, a=a-40, ph=5)
    L.dots(r-16, 16, 2, a=a-60, ph=15)
    for i in range(16):
        deg = i*22.5+5
        L.line_p(r-8, deg, r+8, deg, 2, a-60)
    for i in range(8):
        deg = i*45+22
        L.arc_p(r-16, deg-8, deg+8, 2, a-50)
    L.scripture(r-22, 32, w=1, a=a-80, ph=11)
    return L.img

def make_layer3_center(school, a=200):
    """Center sigil — rotates very slowly."""
    L = Layer()
    r = 140
    L.ring(r, 4, a-20)
    L.ring(r-8, 2, a-40)
    L.dash(r-4, 1, 4, 5, a-50)
    L.dots(r, 8, 3, a=a-40, ph=22)

    if school == "fire":
        for i in range(8):
            deg = i*45+22
            L.line_p(25, deg-8, r-12, deg, 3, a-20)
            L.line_p(25, deg+8, r-12, deg, 3, a-20)
        L.sym(0, 0, 7, sz=40, a=a)
        L.sym(0, 0, 0, sz=18, a=a-20)
        L.ring(80, 2, a-30)

    elif school == "water":
        for i in range(3):
            off = i*15-15
            L.arc_p(r-15, -60+off, 60+off, 3, a-30)
        L.sym(0, 0, 8, sz=40, a=a)
        L.sym(5, 90, 1, sz=22, a=a-20)
        L.ring(80, 2, a-30)

    elif school == "wind":
        for arm in range(3):
            base = arm*120+60
            cx2, cy2 = L.pol(60, base)
            for j in range(20):
                t1, t2 = j/20, (j+1)/20; r1, r2 = 8+t1*60, 8+t2*60
                a1, a2 = base+t1*200, base+t2*200
                L.d.line([cx2+r1*math.cos(math.radians(a1-90)), cy2+r1*math.sin(math.radians(a1-90)),
                         cx2+r2*math.cos(math.radians(a2-90)), cy2+r2*math.sin(math.radians(a2-90))],
                        fill=(255,255,255,a-20), width=3)
        L.ring(100, 2, a-30)

    elif school == "earth":
        for sz_val, al in [(r-12, a-30), (r-35, a-20), (r-58, a-10)]:
            pts = [(CX, CY-sz_val), (CX+int(sz_val*0.6), CY), (CX, CY+int(sz_val*0.6)), (CX-int(sz_val*0.6), CY)]
            L.d.polygon(pts, outline=(255,255,255,al), width=3)
        L.sym(0, 0, 11, sz=30, a=a-10)

    elif school == "lightning":
        pts = [(CX-4, CY-r+10), (CX+8, CY-r//2), (CX-5, CY-r//2),
               (CX+6, CY+r-10), (CX+2, CY+r//2), (CX-2, CY+r//2)]
        L.d.polygon(pts, outline=(255,255,255,a), width=4)
        for br in [(60, -1), (-40, 1)]:
            deg = br[0]; px, py = L.pol(r-40, deg)
            px2, py2 = L.pol(r-80, deg+br[1]*10)
            L.d.line([(px,py),(px2,py2)], fill=(255,255,255,a-40), width=2)
        L.ring(70, 2, a-30)

    elif school == "ice":
        L.sym(0, 0, 9, sz=50, a=a)
        for i in range(6):
            deg = i*60
            L.line_p(15, deg, r-12, deg, 3, a-10)
            L.line_p(r-40, deg-15, r-70, deg-10, 2, a-40)
            L.line_p(r-40, deg+15, r-70, deg+10, 2, a-40)
        L.ring(60, 2, a-30)

    elif school == "lava":
        L.ring(r-15, 4, a-20); L.ring(r-30, 2, a-40)
        for i in range(6):
            base = i*60+10
            pts = [(CX, CY)]
            for j in range(1, 5):
                dd = base + (j%2*2-1)*j*3; rr = 10 + j*(r-50)/4
                px, py = L.pol(rr, dd); pts.append((px, py))
            L.d.line(pts, fill=(255,255,255,a-40), width=3)
        L.sym(0, 0, 7, sz=30, a=a)

    elif school == "necromancy":
        L.sym(0, 0, 10, sz=55, a=a)
        L.ring(r-15, 2, a-30); L.ring(r-35, 2, a-40)
        L.sym(r-25, 270, 16, sz=25, a=a-10)
        L.sym(r-10, 270, 14, sz=14, a=a-20)
        L.dots(r-25, 10, 2, ph=18, a=a-60)

    elif school == "lumamancy":
        for i in range(8):
            deg = i*45+22
            L.line_p(15, deg, r-12, deg, 3, a)
            L.line_p(r-50, deg-5, r-80, deg-8, 2, a-30)
            L.line_p(r-50, deg+5, r-80, deg+8, 2, a-30)
        for i in range(8):
            deg = i*45+45
            L.line_p(15, deg, r-30, deg, 1, a-10)
        L.sym(0, 0, 0, sz=40, a=a)
        L.sym(0, 0, 12, sz=20, a=a-10)
        L.ring(70, 2, a-30)

    return L.img


def generate():
    # SGA character IDs per school (16 chars each, cycling)
    sga_sets = {
        "fire":       [0,3,7,10,14,2,5,9,12,1,4,8,11,15,6,13],
        "water":      [2,5,8,12,15,1,4,7,11,0,3,6,10,14,9,13],
        "wind":       [10,12,15,1,4,7,9,11,14,0,2,5,8,13,3,6],
        "earth":      [6,8,11,14,1,3,5,7,10,13,15,0,2,4,9,12],
        "lightning":  [7,11,14,2,6,9,12,15,1,4,8,10,13,0,3,5],
        "ice":        [9,12,15,3,6,8,11,14,1,4,7,10,13,0,2,5],
        "lava":       [0,4,8,12,15,2,5,9,13,1,3,7,10,14,6,11],
        "necromancy": [14,13,9,5,2,0,15,12,8,4,1,10,6,3,11,7],
        "lumamancy":  [0,6,12,3,9,15,5,11,1,7,13,4,10,2,8,14],
    }

    sym_sets = {
        "fire":       [0,2,7,10,5,0,7,2,10,7,0,2],
        "water":      [1,3,8,18,4,1,8,3,18,8,1,3],
        "wind":       [15,5,13,12,15,5,13,12,15,5,13,12],
        "earth":      [11,4,13,11,4,13,11,4,13,11,4,13],
        "lightning":  [17,0,6,2,10,17,0,6,2,10,17,0],
        "ice":        [9,6,8,13,9,6,8,13,9,6,8,13],
        "lava":       [7,2,5,0,10,7,2,5,0,10,7,2],
        "necromancy": [10,6,14,16,15,10,6,14,16,15,10,6],
        "lumamancy":  [0,12,9,13,5,0,12,9,13,5,0,12],
    }

    schools = ["fire","water","wind","earth","lightning","ice","lava","necromancy","lumamancy"]

    for s in schools:
        print(f"  {s}...")
        make_layer0(s, sga_sets[s][:16]).save(os.path.join(ASSETS, f"{s}_0.png"))
        make_layer1(s, sym_sets[s][:12]).save(os.path.join(ASSETS, f"{s}_1.png"))
        make_layer2(s).save(os.path.join(ASSETS, f"{s}_2.png"))
        make_layer3_center(s).save(os.path.join(ASSETS, f"{s}_3.png"))

    # lumamancy -> lumagie alias
    import shutil
    for i in range(4):
        shutil.copy2(os.path.join(ASSETS, f"lumamancy_{i}.png"),
                     os.path.join(ASSETS, f"lumagie_{i}.png"))

    print("Done!")


if __name__ == "__main__":
    generate()
