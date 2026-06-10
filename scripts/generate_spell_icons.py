"""
Generates 800x800 spell icon PNGs — each spell is unique.
- Minecraft-style blocky character
- 4 pose variants per spell type
- Seeded RNG per spell_id for unique particle placement
- School-specific backgrounds + atmospheric effects
- Tier indicator (diamond symbols)

Usage:
    python scripts/generate_spell_icons.py [--force]

Requires: pip install Pillow
"""

import json
import math
import random
import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFilter
except ImportError:
    print("ERROR: Pillow not installed. Run: pip install Pillow")
    sys.exit(1)

FORCE = "--force" in sys.argv
S = 800

PALETTE = {
    "fire":       ("#1A0500", "#FF5500"),
    "water":      ("#001525", "#0088FF"),
    "wind":       ("#081510", "#55FFCC"),
    "earth":      ("#120B00", "#AA8833"),
    "lightning":  ("#070710", "#FFDD00"),
    "ice":        ("#000F1A", "#88CCFF"),
    "lava":       ("#1A0800", "#FF6600"),
    "necromancy": ("#07000F", "#BB44FF"),
    "lumamancy":  ("#101005", "#FFFFAA"),
}

ROOT        = Path(__file__).parent.parent
SPELLS_JSON = ROOT / "src/main/resources/data/sihriya/spells.json"
OUT_DIR     = ROOT / "src/main/resources/assets/sihriya/textures/gui/spell_icons"


def h(hx):
    s = hx.lstrip("#")
    return tuple(int(s[i:i+2], 16) for i in (0, 2, 4))

def lerp(c1, c2, t):
    return tuple(int(a + (b - a) * t) for a, b in zip(c1, c2))

def dark(c, t=0.68):
    return lerp(c, (0, 0, 0), t)

def bright(c, t=0.35):
    return lerp(c, (255, 255, 255), t)


# ── Background ────────────────────────────────────────────────────────────────

def make_bg(school, rng):
    bg_hex, ac_hex = PALETTE.get(school, ("#111111", "#888888"))
    bg, ac = h(bg_hex), h(ac_hex)
    img = Image.new("RGB", (S, S), bg)
    d = ImageDraw.Draw(img)
    cx = cy = S // 2
    for i in range(80, 0, -1):
        r = int(S * 0.88 * i / 80)
        t = (1 - i / 80) ** 1.9
        d.ellipse([cx-r, cy-r, cx+r, cy+r], fill=lerp(lerp(bg, ac, 0.22), bg, t))
    _bg_school(d, school, lerp(ac, (0, 0, 0), 0.52), rng)
    return img


def _bg_school(d, school, color, rng):
    """School-specific background decoration, seeded for slight variation."""
    rot = rng.uniform(0, math.pi * 2)  # rotation offset for variety

    if school == "fire":
        for i in range(7):
            x = int(80 + i * 92 + rng.randint(-15, 15))
            fh = int(32 + rng.randint(0, 45))
            pts = [(x-16, S-6), (x, S-6-fh), (x+16, S-6)]
            d.polygon(pts, fill=color)

    elif school == "water":
        for i in range(4):
            y = S - 38 - i * 28 + rng.randint(-8, 8)
            d.arc([20, y-28, S-20, y+28], start=0, end=180, fill=color, width=4)

    elif school == "wind":
        cx, cy = S // 2, S // 2
        for start in [rot, rot + 2.09, rot + 4.19]:
            pts = []
            for step in range(48):
                a = start + step * 0.16
                r = 18 + step * 9.0
                pts.append((int(cx + r * math.cos(a)), int(cy + r * math.sin(a))))
            for i in range(len(pts) - 1):
                d.line([pts[i], pts[i+1]], fill=color, width=3)

    elif school == "earth":
        for x in range(0, S, int(32 + rng.randint(0, 10))):
            bh = int(16 + rng.randint(0, 32))
            d.rectangle([x+3, S-bh-3, x+28, S-3], outline=color, width=3)

    elif school == "lightning":
        for side in [-1, 1]:
            pts = []
            y = 20
            x = 400 + side * (280 + rng.randint(-20, 20))
            for _ in range(5):
                pts.append((x, y))
                x += side * rng.randint(-40, 40)
                y += rng.randint(50, 80)
            for i in range(len(pts) - 1):
                d.line([pts[i], pts[i+1]], fill=color, width=5)

    elif school == "ice":
        for cx2, cy2 in [(200, 600), (400, 680), (600, 600)]:
            cx2 += rng.randint(-20, 20); cy2 += rng.randint(-10, 10)
            for ang in range(0, 360, 60):
                r = math.radians(ang + rot * 30)
                x2 = int(cx2 + 55 * math.cos(r)); y2 = int(cy2 + 55 * math.sin(r))
                d.line([cx2, cy2, x2, y2], fill=color, width=3)
                mx = int(cx2 + 28 * math.cos(r)); my = int(cy2 + 28 * math.sin(r))
                pr = r + math.pi / 2
                d.line([int(mx+12*math.cos(pr)), int(my+12*math.sin(pr)),
                        int(mx-12*math.cos(pr)), int(my-12*math.sin(pr))], fill=color, width=3)

    elif school == "lava":
        for i in range(7):
            x = int(70 + i * 95 + rng.randint(-20, 20))
            yb = int(720 + rng.randint(-20, 20))
            d.ellipse([x-12, yb-26, x+12, yb], fill=color)
            d.ellipse([x-7,  yb,    x+7,  yb+20], fill=color)

    elif school == "necromancy":
        for bx, by in [(55, 55), (745, 55), (55, 745), (745, 745)]:
            bx += rng.randint(-15, 15); by += rng.randint(-15, 15)
            d.line([bx-20, by, bx+20, by], fill=color, width=6)
            d.line([bx, by-20, bx, by+20], fill=color, width=6)
            d.ellipse([bx-8, by-8, bx+8, by+8], fill=color)

    elif school == "lumamancy":
        for _ in range(8):
            sx = rng.randint(20, S-20); sy = rng.randint(20, S-20)
            for gr in range(10, 0, -3):
                t = (10 - gr) / 10
                c = lerp(color, bright(h(PALETTE["lumamancy"][1]), 0.4), t)
                d.ellipse([sx-gr, sy-gr, sx+gr, sy+gr], fill=c)


# ── Minecraft character constants (800×800) ───────────────────────────────────

CX   = S // 2   # 400
HH   = 34       # head half (68×68 head)
HY1  = 65       # head top Y
HY2  = HY1 + HH * 2   # 133, head bottom

BW   = 26       # body half-width (52px total)
BY1  = HY2 + 10        # 143, body top
BY2  = BY1 + 180       # 323, body bottom

ARM_T = 28      # arm thickness
SHY   = BY1 + 14       # 157, shoulder Y
LSX   = CX - BW - 6 - ARM_T // 2   # 354, left  shoulder center X
RSX   = CX + BW + 6 + ARM_T // 2   # 446, right shoulder center X

LEG_T = 26      # leg thickness
LLX   = CX - BW // 2   # 387, left  leg center X
RLX   = CX + BW // 2   # 413, right leg center X
LEG_H = 172     # leg height → foot at Y = 323+172 = 495

LW_B  = 6       # block outline width


def blk(d, x1, y1, x2, y2, fill, outline):
    d.rectangle([x1, y1, x2, y2], fill=fill, outline=outline, width=LW_B)


def blk_arm(d, sx, sy, ex, ey, thick, fill, outline):
    dx, dy = ex - sx, ey - sy
    L = math.hypot(dx, dy)
    if L < 1: return
    nx, ny = -dy / L * thick / 2, dx / L * thick / 2
    pts = [(round(sx+nx), round(sy+ny)), (round(sx-nx), round(sy-ny)),
           (round(ex-nx), round(ey-ny)), (round(ex+nx), round(ey+ny))]
    d.polygon(pts, fill=fill, outline=outline, width=LW_B)


# ── Leg helpers ───────────────────────────────────────────────────────────────

def _legs_stand(d, fill, ol):
    blk_arm(d, LLX, BY2, LLX,      BY2+LEG_H, LEG_T, fill, ol)
    blk_arm(d, RLX, BY2, RLX,      BY2+LEG_H, LEG_T, fill, ol)

def _legs_spread(d, fill, ol):
    blk_arm(d, LLX, BY2, LLX-25,   BY2+LEG_H, LEG_T, fill, ol)
    blk_arm(d, RLX, BY2, RLX+25,   BY2+LEG_H, LEG_T, fill, ol)

def _legs_wide(d, fill, ol):
    blk_arm(d, LLX, BY2, LLX-55,   BY2+LEG_H, LEG_T, fill, ol)
    blk_arm(d, RLX, BY2, RLX+55,   BY2+LEG_H, LEG_T, fill, ol)

def _legs_run(d, fill, ol):
    blk_arm(d, LLX, BY2, LLX-42,   BY2+LEG_H, LEG_T, fill, ol)  # back leg
    blk_arm(d, RLX, BY2, RLX+32,   BY2+LEG_H, LEG_T, fill, ol)  # forward leg


# ── Pose functions (4 variants each) ─────────────────────────────────────────

def pose_projectile(d, color, fill, v, rng):
    blk(d, CX-HH, HY1, CX+HH, HY2, fill, color)
    blk(d, CX-BW, BY1, CX+BW, BY2, fill, color)
    if v == 0:  # throw right, running
        blk_arm(d, LSX, SHY, LSX-36,  SHY+42, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+100, SHY-18, ARM_T, fill, color)
        _legs_run(d, fill, color)
        px, py = RSX+118, SHY-26
    elif v == 1:  # throw up-right, legs spread
        blk_arm(d, LSX, SHY, LSX-44,  SHY+50, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+58,  SHY-82, ARM_T, fill, color)
        _legs_spread(d, fill, color)
        px, py = RSX+72, SHY-98
    elif v == 2:  # mirrored — left arm throws left
        blk_arm(d, LSX, SHY, LSX-100, SHY-18, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+36,  SHY+42, ARM_T, fill, color)
        _legs_run(d, fill, color)
        px, py = LSX-118, SHY-26
    else:  # spinning — arm up-forward, other back-down
        blk_arm(d, LSX, SHY, LSX-28,  SHY-88, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+85,  SHY+38, ARM_T, fill, color)
        _legs_wide(d, fill, color)
        px, py = LSX-40, SHY-104
    psize = 20 + rng.randint(0, 16)
    d.ellipse([px-psize, py-psize, px+psize, py+psize],
              fill=color, outline=bright(color, 0.3), width=4)
    for i in range(4):
        dl = 35 + i * 18
        d.line([px - dl, py + i*6, px - dl - 24, py + i*6], fill=color, width=3)


def pose_zone(d, color, fill, v, rng):
    blk(d, CX-HH, HY1, CX+HH, HY2, fill, color)
    blk(d, CX-BW, BY1, CX+BW, BY2, fill, color)
    if v == 0:   # T-pose horizontal
        blk_arm(d, LSX, SHY, LSX-105, SHY+8,  ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+105, SHY+8,  ARM_T, fill, color)
    elif v == 1: # V upward
        blk_arm(d, LSX, SHY, LSX-88,  SHY-55, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+88,  SHY-55, ARM_T, fill, color)
    elif v == 2: # V downward (casting down)
        blk_arm(d, LSX, SHY, LSX-88,  SHY+70, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+88,  SHY+70, ARM_T, fill, color)
    else:        # one up one down — body twisted
        blk_arm(d, LSX, SHY, LSX-22,  SHY-110, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+22,  SHY+110, ARM_T, fill, color)
    _legs_wide(d, fill, color)
    zr = int(55 + rng.randint(0, 20))
    zy = BY2 + 90 + rng.randint(0, 20)
    rot = rng.uniform(0, math.pi / 9)
    d.ellipse([CX-zr, zy-zr//3, CX+zr, zy+zr//3], outline=color, width=5)
    for ang in range(0, 360, 36):
        r = math.radians(ang + math.degrees(rot))
        x1 = int(CX+(zr+5)*math.cos(r));  y1 = int(zy+(zr//3+5)*math.sin(r))
        x2 = int(CX+(zr+22)*math.cos(r)); y2 = int(zy+(zr//3+22)*math.sin(r))
        d.line([x1, y1, x2, y2], fill=color, width=4)


def pose_buff(d, color, fill, v, rng):
    blk(d, CX-HH, HY1, CX+HH, HY2, fill, color)
    blk(d, CX-BW, BY1, CX+BW, BY2, fill, color)
    if v == 0:   # both arms raised high
        blk_arm(d, LSX, SHY, LSX-28,  HY1-14, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+28,  HY1-14, ARM_T, fill, color)
    elif v == 1: # left up, right punching sideways
        blk_arm(d, LSX, SHY, LSX-14,  HY1-18, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+110, SHY+20, ARM_T, fill, color)
    elif v == 2: # both flexed 45° up-out (victory)
        blk_arm(d, LSX, SHY, LSX-80,  SHY-68, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+80,  SHY-68, ARM_T, fill, color)
    else:        # arms crossed on chest (coiled power)
        blk_arm(d, LSX, SHY, CX+BW+20, SHY+40, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, CX-BW-20, SHY+40, ARM_T, fill, color)
    _legs_spread(d, fill, color)
    mid_y = (HY1 + BY2) // 2
    n_sparks = 8 + rng.randint(0, 6)
    base_angle = rng.uniform(0, math.pi * 2)
    for i in range(n_sparks):
        a = base_angle + (i / n_sparks) * math.pi * 2
        for dist in (105, 125):
            x1 = int(CX + dist*math.cos(a));      y1 = int(mid_y + dist*0.55*math.sin(a))
            x2 = int(CX + (dist+18)*math.cos(a)); y2 = int(mid_y + (dist+18)*0.55*math.sin(a))
            d.line([x1, y1, x2, y2], fill=color, width=4)


def pose_summon(d, color, fill, v, rng):
    blk(d, CX-HH, HY1, CX+HH, HY2, fill, color)
    blk(d, CX-BW, BY1, CX+BW, BY2, fill, color)
    if v == 0:   # arms angled down-out
        blk_arm(d, LSX, SHY, LSX-75,  SHY+78, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+75,  SHY+78, ARM_T, fill, color)
        cy_circle = BY2 + 100
    elif v == 1: # arms straight down
        blk_arm(d, LSX, SHY, LSX-16,  BY2+42, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+16,  BY2+42, ARM_T, fill, color)
        cy_circle = BY2 + 100
    elif v == 2: # arms forward-low (pushing energy down)
        blk_arm(d, LSX, SHY, LSX-68,  SHY+95, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+68,  SHY+95, ARM_T, fill, color)
        cy_circle = BY2 + 120
    else:        # arms raised — circle ABOVE head
        blk_arm(d, LSX, SHY, LSX-22,  HY1-12, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+22,  HY1-12, ARM_T, fill, color)
        cy_circle = HY1 - 80
    _legs_spread(d, fill, color)
    sr = int(62 + rng.randint(0, 22))
    rot = rng.uniform(0, math.pi / 6)
    d.ellipse([CX-sr, cy_circle-sr//3, CX+sr, cy_circle+sr//3], outline=color, width=5)
    d.ellipse([CX-sr//2, cy_circle-sr//6, CX+sr//2, cy_circle+sr//6], outline=color, width=3)
    for ang in range(0, 360, 60):
        r = math.radians(ang + math.degrees(rot))
        x = int(CX + sr*math.cos(r)); y = int(cy_circle + sr//3*math.sin(r))
        d.ellipse([x-7, y-7, x+7, y+7], fill=color)


def pose_ultimate(d, color, fill, v, rng):
    blk(d, CX-HH, HY1, CX+HH, HY2, fill, color)
    blk(d, CX-BW, BY1, CX+BW, BY2, fill, color)
    if v == 0:   # arms swept back, running
        blk_arm(d, LSX, SHY, LSX-108, SHY-32, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+105, SHY-26, ARM_T, fill, color)
        _legs_run(d, fill, color)
    elif v == 1: # arms X-crossed in front
        blk_arm(d, LSX, SHY, RSX+34,  SHY+72, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, LSX-34,  SHY+72, ARM_T, fill, color)
        _legs_wide(d, fill, color)
    elif v == 2: # both arms thrust up-diagonal (unleashing upward)
        blk_arm(d, LSX, SHY, LSX-72,  HY1-55, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+72,  HY1-55, ARM_T, fill, color)
        _legs_spread(d, fill, color)
    else:        # one arm point forward, one back — charge direction
        blk_arm(d, LSX, SHY, LSX-110, SHY+18, ARM_T, fill, color)
        blk_arm(d, RSX, SHY, RSX+110, SHY-10, ARM_T, fill, color)
        _legs_run(d, fill, color)
    bcx, bcy = CX, (HY1 + BY2) // 2
    n_rays = 12 + rng.randint(0, 6)
    base = rng.uniform(0, math.pi * 2)
    for i in range(n_rays):
        a = base + (i / n_rays) * math.pi * 2
        r1 = int(110 + rng.randint(0, 20))
        r2 = r1 + int(25 + rng.randint(0, 20))
        x1 = int(bcx + r1*math.cos(a)); y1 = int(bcy + r1*math.sin(a))
        x2 = int(bcx + r2*math.cos(a)); y2 = int(bcy + r2*math.sin(a))
        d.line([x1, y1, x2, y2], fill=color, width=4)


POSES = {
    "PROJECTILE": pose_projectile,
    "ZONE":       pose_zone,
    "BUFF":       pose_buff,
    "SUMMON":     pose_summon,
    "ULTIMATE":   pose_ultimate,
}


# ── Foreground particles (school-specific, seeded) ───────────────────────────

def draw_particles(d, color, school, tier, rng):
    """Unique floating particles per school drawn in foreground."""
    char_cy = (HY1 + BY2 + LEG_H) // 2  # approx character mid Y
    n = 6 + tier * 4 + rng.randint(0, 8)

    positions = []
    for _ in range(n):
        a = rng.uniform(0, math.pi * 2)
        r = rng.uniform(130, 340)
        px = int(CX + r * math.cos(a))
        py = int(char_cy + r * math.sin(a))
        psize = int(10 + tier * 5 + rng.randint(0, 18))
        positions.append((px, py, psize))

    for px, py, ps in positions:
        _particle(d, school, color, px, py, ps, rng)


def _particle(d, school, color, px, py, ps, rng):
    if school == "fire":
        pts = [(px, py-ps), (px-ps//2, py+ps//3), (px+ps//2, py+ps//3)]
        d.polygon(pts, fill=color)

    elif school == "water":
        d.arc([px-ps, py-ps//2, px+ps, py+ps//2], start=0, end=180, fill=color, width=4)
        d.line([px-ps, py, px+ps, py], fill=color, width=3)

    elif school == "wind":
        for i in range(3):
            a = rng.uniform(0, math.pi)
            r = ps // 2
            d.arc([px-r, py-r, px+r, py+r], start=int(math.degrees(a)),
                  end=int(math.degrees(a))+120, fill=color, width=3)

    elif school == "earth":
        # diamond
        pts = [(px, py-ps), (px+ps//2, py), (px, py+ps), (px-ps//2, py)]
        d.polygon(pts, outline=color, width=3)

    elif school == "lightning":
        # mini zigzag bolt
        x0, y0 = px, py - ps
        pts = [(x0, y0), (x0+ps//3, y0+ps//2), (x0-ps//4, y0+ps//2), (x0+ps//4, y0+ps)]
        for i in range(len(pts)-1):
            d.line([pts[i], pts[i+1]], fill=color, width=4)

    elif school == "ice":
        # 6-branch asterisk
        for ang in range(0, 360, 60):
            r = math.radians(ang)
            x2 = int(px + ps * math.cos(r)); y2 = int(py + ps * math.sin(r))
            d.line([px, py, x2, y2], fill=color, width=3)
            mid_x = int(px + ps//2 * math.cos(r)); mid_y = int(py + ps//2 * math.sin(r))
            pr = r + math.pi/2
            d.line([int(mid_x+ps//4*math.cos(pr)), int(mid_y+ps//4*math.sin(pr)),
                    int(mid_x-ps//4*math.cos(pr)), int(mid_y-ps//4*math.sin(pr))],
                   fill=color, width=2)

    elif school == "lava":
        d.ellipse([px-ps//2, py-ps, px+ps//2, py], fill=color)
        d.ellipse([px-ps//3, py, px+ps//3, py+ps//2], fill=color)

    elif school == "necromancy":
        d.line([px-ps, py, px+ps, py], fill=color, width=4)
        d.line([px, py-ps, px, py+ps], fill=color, width=4)
        for ex, ey in [(px-ps, py), (px+ps, py), (px, py-ps), (px, py+ps)]:
            d.ellipse([ex-5, ey-5, ex+5, ey+5], fill=color)

    elif school == "lumamancy":
        # 4-point star sparkle
        for ang in [0, 90, 180, 270]:
            r = math.radians(ang)
            x2 = int(px + ps * math.cos(r)); y2 = int(py + ps * math.sin(r))
            d.line([px, py, x2, y2], fill=color, width=4)
        for ang in [45, 135, 225, 315]:
            r = math.radians(ang)
            x2 = int(px + ps//2 * math.cos(r)); y2 = int(py + ps//2 * math.sin(r))
            d.line([px, py, x2, y2], fill=color, width=2)


# ── Tier indicator ────────────────────────────────────────────────────────────

def draw_tier(d, color, tier):
    """Diamond symbols in top-right corner indicating tier (1–5)."""
    ds = 14   # diamond half-size
    gap = 6
    total_w = tier * (ds*2 + gap) - gap
    x0 = S - 18 - total_w
    y0 = 18
    for i in range(tier):
        x = x0 + i * (ds*2 + gap) + ds
        pts = [(x, y0), (x+ds, y0+ds), (x, y0+ds*2), (x-ds, y0+ds)]
        d.polygon(pts, fill=color, outline=bright(color, 0.25), width=2)


# ── Icon assembly ──────────────────────────────────────────────────────────────

def _draw_mc(draw, color, pose_fn, v, rng):
    fill = dark(color, 0.70)
    pose_fn(draw, color, fill, v, rng)


def generate_icon(school, spell_type, tier, spell_id):
    rng = random.Random(spell_id)          # deterministic seed per spell
    variant = rng.randint(0, 3)            # 4 pose variants

    _, ac_hex = PALETTE.get(school, ("#111111", "#FFFFFF"))
    color = h(ac_hex)

    bg = make_bg(school, rng)

    # Glow layer
    glow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    _draw_mc(ImageDraw.Draw(glow), color,
             POSES.get(spell_type, pose_buff), variant, random.Random(spell_id))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=12))

    # Sharp layer
    sharp = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    _draw_mc(ImageDraw.Draw(sharp), color,
             POSES.get(spell_type, pose_buff), variant, random.Random(spell_id))

    img = bg.convert("RGBA")
    img.paste(glow,  (0, 0), glow)
    img.paste(sharp, (0, 0), sharp)

    # Foreground particles (draw on RGBA before flattening)
    pfg = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    draw_particles(ImageDraw.Draw(pfg), color, school, tier, random.Random(spell_id + "_p"))
    img.paste(pfg, (0, 0), pfg)

    # Tier indicator + border
    d = ImageDraw.Draw(img)
    draw_tier(d, color, tier)
    d.rectangle([0, 0, S-1, S-1], outline=(*color, 220), width=5)
    d.rectangle([6, 6, S-7, S-7], outline=(*lerp(color, (0,0,0), 0.45), 160), width=2)

    return img.convert("RGB")


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    if not SPELLS_JSON.is_file():
        print(f"ERROR: spells.json not found: {SPELLS_JSON}")
        sys.exit(1)

    with open(SPELLS_JSON, encoding="utf-8") as f:
        spells = json.load(f)

    OUT_DIR.mkdir(exist_ok=True)
    generated = skipped = 0
    total = len(spells)

    for i, spell in enumerate(spells, 1):
        out_file = OUT_DIR / f"{spell['id'].replace('.', '_')}.png"
        if out_file.exists() and not FORCE:
            skipped += 1
            continue

        img = generate_icon(
            spell["school"],
            spell.get("type", "PROJECTILE"),
            spell.get("tier", 1),
            spell["id"],
        )
        img.save(out_file)
        generated += 1
        if generated % 25 == 0:
            print(f"  {generated}/{total - skipped} generated...")

    print(f"generate_spell_icons: {generated} generated ({S}×{S}), {skipped} skipped")


if __name__ == "__main__":
    main()
