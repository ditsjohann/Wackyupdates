#!/usr/bin/env python3
"""
WackyUpdate-V1 asset generator.

Generates every JSON (blockstates, models, loot tables, recipes, tags, worldgen, lang) and
procedurally paints every texture for the mod. Re-run after editing:

    python3 tools/generate_assets.py

Requires Pillow (pip install pillow). Output goes to src/main/resources.
Swap any generated PNG for hand-made art later - nothing else depends on how they look.
"""
import colorsys
import json
import math
import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "wackyupdate"
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data", NS)
MC_DATA = os.path.join(ROOT, "data", "minecraft")

COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray",
          "cyan", "purple", "blue", "brown", "green", "red", "black"]


# --------------------------------------------------------------------------------------------- io
def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def save(img, *parts):
    path = os.path.join(ASSETS, "textures", *parts) + ".png"
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def m(name):
    return f"{NS}:{name}"


# ---------------------------------------------------------------------------------------- colours
def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def hsv(h, s, v, a=255):
    r, g, b = colorsys.hsv_to_rgb(h % 1.0, s, v)
    return (int(r * 255), int(g * 255), int(b * 255), a)


SPEC_DARK = hexc("1d4e5e")
SPEC_MID = hexc("2f97a8")
SPEC_LIGHT = hexc("6fdbe6")
SPEC_HI = hexc("dafcff")


def new(w=16, h=16, fill=(0, 0, 0, 0)):
    return Image.new("RGBA", (w, h), fill)


def noise_fill(img, palette, rng, box=None, weights=None):
    x0, y0, x1, y1 = box or (0, 0, img.width, img.height)
    px = img.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = rng.choices(palette, weights=weights)[0]


# --------------------------------------------------------------------------------- block textures
def tex_budding_speculum():
    rng = random.Random(1)
    img = new()
    noise_fill(img, [SPEC_DARK, shade(SPEC_DARK, 1.15), SPEC_MID], rng, weights=[4, 3, 2])
    px = img.load()
    # faceted crystal plates
    for _ in range(5):
        cx, cy = rng.randrange(16), rng.randrange(16)
        for i in range(-3, 4):
            x, y = (cx + i) % 16, (cy + abs(i)) % 16
            px[x, y] = SPEC_LIGHT
            px[x, (y + 1) % 16] = SPEC_MID
    for _ in range(6):
        px[rng.randrange(16), rng.randrange(16)] = SPEC_HI
    for _ in range(3):  # prismatic glints
        px[rng.randrange(16), rng.randrange(16)] = hsv(rng.random(), 0.5, 1.0)
    return img


def tex_crystal_cross(height, spikes, seed):
    rng = random.Random(seed)
    img = new()
    d = ImageDraw.Draw(img)
    for i in range(spikes):
        cx = 8 + (i - (spikes - 1) / 2) * 3 + rng.randint(-1, 1)
        h = max(3, height - abs(i - (spikes - 1) / 2) * 3 + rng.randint(-1, 1))
        w = 2 if h < 7 else 3
        top = 16 - h
        d.polygon([(cx - w, 15), (cx + w, 15), (cx + w, top + w), (cx, top), (cx - w, top + w)], fill=SPEC_MID)
        d.line([(cx - w, 15), (cx - w, top + w), (cx, top)], fill=SPEC_LIGHT)
        d.line([(cx + w, 15), (cx + w, top + w)], fill=SPEC_DARK)
        d.point((cx, top), fill=SPEC_HI)
        if h > 6:
            d.point((cx - 1, top + 3), fill=hsv(rng.random(), 0.4, 1.0))
    return img


def tex_prism_glass():
    # Pale and mostly see-through; the block is tinted per colour and rendered full-bright.
    rng = random.Random(2)
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = (235, 240, 245, 105)
    for i in range(16):  # soft diagonal shine
        for off in (0, 1):
            x, y = i, (i + 5 + off) % 16
            if 2 < x < 14:
                px[x, y] = (255, 255, 255, 170)
    for i in range(9, 14):
        px[i, 16 - i] = (255, 255, 255, 160)
    for _ in range(6):
        px[rng.randrange(1, 15), rng.randrange(1, 15)] = (255, 255, 255, 220)
    return img


def tex_speculum_block():
    rng = random.Random(3)
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            # diamond facets
            fx, fy = (x % 8) - 3.5, (y % 8) - 3.5
            t = (abs(fx) + abs(fy)) / 7.0
            c = mix(SPEC_LIGHT, SPEC_DARK, t)
            if fx + fy < -1:
                c = shade(c, 1.15)
            px[x, y] = c
    for _ in range(5):
        px[rng.randrange(16), rng.randrange(16)] = SPEC_HI
    return img


def tex_polished_speculum():
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            t = (x + y) / 30.0
            px[x, y] = mix(SPEC_LIGHT, SPEC_MID, t)
    for i in range(16):
        px[i, 0] = px[0, i] = SPEC_HI
        px[i, 15] = px[15, i] = SPEC_DARK
    for i in range(3, 7):
        px[i, 9 - i] = SPEC_HI
    return img


def tex_speculum_bricks():
    rng = random.Random(4)
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            row = y // 4
            off = 4 if row % 2 else 0
            mortar = y % 4 == 3 or (x + off) % 8 == 7
            if mortar:
                px[x, y] = SPEC_DARK
            else:
                c = rng.choice([SPEC_MID, SPEC_MID, shade(SPEC_MID, 1.1), SPEC_LIGHT])
                if y % 4 == 0:
                    c = SPEC_LIGHT
                px[x, y] = c
    return img


def tex_prism_tiles():
    img = new()
    px = img.load()
    hues = [0.0, 0.14, 0.33, 0.6]
    for y in range(16):
        for x in range(16):
            q = (x // 8) + 2 * (y // 8)
            lx, ly = x % 8, y % 8
            if lx == 7 or ly == 7:
                px[x, y] = hexc("e8eef2")
                continue
            c = hsv(hues[q] + (lx + ly) * 0.012, 0.55, 0.95)
            if lx + ly < 3:
                c = hsv(hues[q], 0.25, 1.0)
            px[x, y] = c
    return img


def tex_compacted_feet():
    rng = random.Random(5)
    img = new()
    noise_fill(img, [hexc("d99a8c"), hexc("cf8e80"), hexc("e3a99a")], rng)
    px = img.load()
    toe = hexc("a8625a")
    pad = hexc("b9706a")
    for (fx, fy) in [(1, 1), (9, 0), (4, 8), (12, 9)]:
        for dx, dy in [(0, 0), (2, -1), (4, 0)]:
            px[(fx + dx) % 16, (fy + dy + 1) % 16] = toe
        for dx in range(0, 5):
            for dy in range(3, 6):
                if not (dx in (0, 4) and dy == 5):
                    px[(fx + dx) % 16, (fy + dy) % 16] = pad
    return img


SLICK_BARK = [hexc("e9dcb4"), hexc("dfcf9f"), hexc("efe4c4")]
SLICK_STREAK = hexc("8fd6c8")


def tex_slick_log():
    rng = random.Random(6)
    img = new()
    noise_fill(img, SLICK_BARK, rng)
    px = img.load()
    for x in (2, 7, 11, 14):
        for y in range(16):
            if rng.random() < 0.75:
                px[x, y] = hexc("cdb986")
    for _ in range(3):
        x, y = rng.randrange(16), rng.randrange(14)
        px[x, y] = px[x, y + 1] = SLICK_STREAK
    for _ in range(2):
        x, y = rng.randrange(1, 15), rng.randrange(1, 15)
        px[x, y] = hexc("a8915c")
    return img


def tex_log_top(bark, inner):
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if x in (0, 15) or y in (0, 15):
                px[x, y] = bark
            else:
                px[x, y] = shade(inner, 0.9) if int(d) % 3 == 0 else inner
    return img


def tex_stripped_log():
    rng = random.Random(7)
    img = new()
    noise_fill(img, [hexc("f5ecd0"), hexc("efe3c0"), hexc("f9f2dc")], rng)
    px = img.load()
    for x in (4, 10):
        for y in range(16):
            px[x, y] = hexc("e6d6aa")
    return img


def tex_planks(seed=8, glossy=True):
    rng = random.Random(seed)
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            c = rng.choice([hexc("f0e2bb"), hexc("eadab0"), hexc("f4e8c8")])
            if y % 4 == 3:
                c = hexc("c9b27c")
            seam = (x + (y // 4) * 5) % 16 == 0
            if seam:
                c = hexc("cdb783")
            if glossy and y % 4 == 0:
                c = hexc("fbf5e2")
            px[x, y] = c
    return img


def tex_slick_floor():
    img = tex_planks(9)
    px = img.load()
    for i in range(16):  # wax shine
        for k in (0, 1):
            x, y = i, (12 - i + k) % 16
            px[x, y] = mix(px[x, y], (255, 255, 255, 255), 0.55)
    return img


def tex_slick_leaves():
    rng = random.Random(10)
    img = new()
    px = img.load()
    pal = [hexc("6fd6c2"), hexc("58c2ae"), hexc("8be6d4"), hexc("45a896")]
    for y in range(16):
        for x in range(16):
            if rng.random() < 0.16:
                continue
            px[x, y] = rng.choice(pal)
    for _ in range(4):
        px[rng.randrange(16), rng.randrange(16)] = hexc("fff3b0")  # tiny blossoms
    return img


def tex_slick_sapling():
    img = new()
    d = ImageDraw.Draw(img)
    d.line([(8, 15), (8, 6)], fill=hexc("d9c79a"))
    d.line([(8, 10), (5, 7)], fill=hexc("d9c79a"))
    for (x, y) in [(7, 4), (8, 4), (9, 4), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (7, 6), (9, 6),
                   (4, 6), (5, 6), (3, 7), (4, 7), (5, 7), (10, 8), (11, 8), (11, 7), (12, 8)]:
        d.point((x, y), fill=hexc("6fd6c2"))
    d.point((8, 3), fill=hexc("8be6d4"))
    d.line([(8, 11), (11, 8)], fill=hexc("d9c79a"))
    return img


def tex_lantern_block():
    """UV regions used by the lantern model: body sides (0,2)-(6,9), body top (0,9)-(6,15),
    cap sides (6,2)-(10,4), cap top (6,4)-(10,8), handle (11,0)-(13,2)."""
    img = new()
    px = img.load()
    frame = hexc("3c4a52")
    for y in range(2, 9):
        for x in range(0, 6):
            edge = x in (0, 5) or y in (2, 8)
            px[x, y] = frame if edge else (SPEC_HI if (x + y) % 3 else SPEC_LIGHT)
    for y in range(9, 15):
        for x in range(0, 6):
            px[x, y] = frame if x in (0, 5) or y in (9, 14) else SPEC_MID
    for y in range(2, 4):
        for x in range(6, 10):
            px[x, y] = hexc("56656d")
    for y in range(4, 8):
        for x in range(6, 10):
            px[x, y] = hexc("4a5860") if x in (6, 9) or y in (4, 7) else hexc("62727a")
    for y in range(0, 2):
        for x in range(11, 13):
            px[x, y] = hexc("2f3a40")
    return img


def tex_lantern_item():
    img = new()
    d = ImageDraw.Draw(img)
    d.rectangle([6, 1, 9, 2], outline=hexc("2f3a40"))
    d.rectangle([6, 3, 9, 4], fill=hexc("56656d"))
    d.rectangle([5, 5, 10, 13], fill=SPEC_LIGHT, outline=hexc("3c4a52"))
    d.rectangle([6, 6, 9, 12], fill=SPEC_HI)
    d.point((7, 8), fill=hexc("fff59a"))
    d.point((8, 10), fill=hexc("fff59a"))
    d.rectangle([5, 14, 10, 14], fill=hexc("3c4a52"))
    return img


def tex_firefly_jar():
    """UV regions: jar side (0,0)-(8,11), lid top (8,0)-(14,6), lid side (8,6)-(14,8), jar bottom (0,11)-(8,16)."""
    rng = random.Random(11)
    img = new()
    px = img.load()
    for y in range(0, 11):
        for x in range(0, 8):
            edge = x in (0, 7) or y == 10
            px[x, y] = (200, 230, 240, 200) if edge else (30, 45, 70, 140)
    for _ in range(5):
        px[rng.randrange(1, 7), rng.randrange(1, 10)] = hexc("fff27a")
    for y in range(0, 6):
        for x in range(8, 14):
            px[x, y] = rng.choice([hexc("a8784c"), hexc("9a6c42"), hexc("b88a5a")])
    for y in range(6, 8):
        for x in range(8, 14):
            px[x, y] = hexc("8a5e38")
    for y in range(11, 16):
        for x in range(0, 8):
            px[x, y] = (200, 230, 240, 200)
    return img


def tex_metal(seed=12):
    rng = random.Random(seed)
    img = new()
    noise_fill(img, [hexc("9aa3a8"), hexc("8a9398"), hexc("aab3b8")], rng)
    px = img.load()
    for x in range(16):
        px[x, 0] = hexc("d8e0e4")
        px[x, 1] = hexc("c2cacf")
        px[x, 15] = hexc("5e676c")
    return img


def tex_boost_pad():
    img = new(fill=hexc("2c2f3a"))
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = hexc("4a4f60")
    # two chevrons pointing north (up in the image)
    for base in (4, 10):
        for i in range(6):
            for t in (0, 1):
                y = base + i - 2 + t
                for x in (7 - i, 8 + i):
                    if 0 <= y < 16 and 1 <= x < 15:
                        px[x, y] = hexc("ffb12b") if t == 0 else hexc("ff7a1a")
    return img


def tex_boost_side():
    img = new(fill=hexc("2c2f3a"))
    px = img.load()
    for x in range(16):
        px[x, 15] = hexc("ff9a2a")
    return img


def tex_projector_side():
    rng = random.Random(13)
    img = tex_metal(13)
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = hexc("4d565b")
    for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        px[x, y] = hexc("e8eef0")
    for y in (6, 8, 10):
        for x in range(4, 12):
            px[x, y] = hexc("3a4146")
    return img


def tex_projector_front(lit):
    img = tex_metal(14)
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 5.5:
                if lit:
                    px[x, y] = hsv(math.atan2(y - 7.5, x - 7.5) / (2 * math.pi), 0.45 if d > 2 else 0.1, 1.0)
                else:
                    px[x, y] = mix(SPEC_DARK, SPEC_MID, d / 5.5)
            elif d < 6.8:
                px[x, y] = hexc("3a4146")
    px[5, 5] = px[6, 5] = (255, 255, 255, 255)
    return img


def tex_projector_back():
    img = tex_metal(15)
    px = img.load()
    for y in range(2, 14, 2):
        for x in range(2, 14):
            px[x, y] = hexc("3a4146")
    return img


def tex_mirror_face():
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            t = (x + (15 - y)) / 30.0
            c = mix(hexc("f4feff"), hexc("8fd3dc"), t)
            if (x - y) % 9 in (0, 1):
                c = mix(c, (255, 255, 255, 255), 0.6)
            px[x, y] = c
    for i in range(16):
        px[i, 0] = px[0, i] = px[i, 15] = px[15, i] = hexc("2b6b78")
    return img


def tex_mirror_frame():
    img = tex_metal(16)
    px = img.load()
    for i in range(16):
        px[i, 0] = px[0, i] = px[i, 15] = px[15, i] = SPEC_MID
    for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        px[x, y] = SPEC_LIGHT
    return img


def tex_splitter_glass():
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            px[x, y] = (210, 240, 250, 210) if edge else (220, 245, 255, 40)
    for i in range(3, 8):
        px[i, 10 - i] = (255, 255, 255, 140)
    return img


def tex_splitter_core():
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = hsv((x + y) / 32.0, 0.55, 1.0)
    return img


def tex_splitter_out():
    img = new(fill=hexc("ffb12b"))
    px = img.load()
    for i in range(16):
        px[i, 0] = px[0, i] = px[i, 15] = px[15, i] = hexc("ff7a1a")
    return img


def tex_receptor(lit):
    img = tex_metal(17)
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = hexc("4d565b")
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 5.2:
                px[x, y] = hexc("1b2226") if not lit else mix(hexc("ffffff"), hexc("ffe27a"), d / 5.2)
            elif d < 6.3:
                px[x, y] = hexc("3a4146")
    if not lit:
        px[6, 6] = px[7, 6] = hexc("3a4a52")
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        px[x, y] = hexc("c62828") if not lit else hexc("ff5252")
    return img


def tex_receptor_gem():
    """Overlay ring tinted with the filter colour."""
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.2 <= d < 6.3:
                px[x, y] = (255, 255, 255, 255) if (x + y) % 3 else (225, 225, 225, 255)
    return img


# ---------------------------------------------------------------------------------- item textures
def tex_shard():
    img = new()
    d = ImageDraw.Draw(img)
    d.polygon([(8, 1), (12, 6), (10, 14), (6, 14), (4, 6)], fill=SPEC_MID)
    d.polygon([(8, 1), (8, 14), (6, 14), (4, 6)], fill=SPEC_LIGHT)
    d.line([(8, 1), (4, 6), (6, 14)], fill=SPEC_HI)
    d.line([(8, 1), (12, 6), (10, 14)], fill=SPEC_DARK)
    d.point((7, 5), fill=hexc("ffd1f4"))
    d.point((9, 9), fill=hexc("fff7b0"))
    return img


def tex_lens():
    img = new()
    d = ImageDraw.Draw(img)
    d.ellipse([2, 1, 12, 11], outline=hexc("b87333"), width=2)
    d.ellipse([4, 3, 10, 9], fill=(130, 220, 235, 200))
    d.point((5, 4), fill=(255, 255, 255, 255))
    d.point((6, 4), fill=(255, 255, 255, 255))
    d.line([(10, 10), (14, 14)], fill=hexc("6b4423"), width=2)
    d.point((8, 7), fill=hexc("ffb7ef"))
    return img


def tex_armor_icon(kind):
    img = new()
    d = ImageDraw.Draw(img)
    fill, edge, hi = SPEC_LIGHT, SPEC_DARK, SPEC_HI
    if kind == "helmet":
        d.rectangle([3, 4, 12, 8], fill=fill, outline=edge)
        d.rectangle([3, 9, 5, 11], fill=fill, outline=edge)
        d.rectangle([10, 9, 12, 11], fill=fill, outline=edge)
        d.line([(4, 5), (9, 5)], fill=hi)
    elif kind == "chestplate":
        d.polygon([(2, 2), (6, 2), (8, 4), (10, 2), (14, 2), (14, 6), (12, 6), (12, 14), (4, 14), (4, 6), (2, 6)], fill=fill, outline=edge)
        d.line([(5, 7), (5, 12)], fill=hi)
    elif kind == "leggings":
        d.rectangle([3, 2, 12, 5], fill=fill, outline=edge)
        d.rectangle([3, 6, 6, 14], fill=fill, outline=edge)
        d.rectangle([9, 6, 12, 14], fill=fill, outline=edge)
        d.line([(4, 3), (11, 3)], fill=hi)
    else:
        d.rectangle([2, 7, 6, 12], fill=fill, outline=edge)
        d.rectangle([9, 7, 13, 12], fill=fill, outline=edge)
        d.rectangle([1, 12, 6, 13], fill=fill, outline=edge)
        d.rectangle([9, 12, 14, 13], fill=fill, outline=edge)
        d.point((3, 8), fill=hi)
        d.point((10, 8), fill=hi)
    # a prismatic sparkle
    img.putpixel((11, 4) if kind != "boots" else (12, 9), hexc("ffd1f4"))
    return img


def tex_mite_foot():
    img = new()
    d = ImageDraw.Draw(img)
    d.line([(4, 3), (9, 9)], fill=hexc("6e6560"), width=2)
    d.line([(9, 9), (12, 13)], fill=hexc("5a524e"), width=2)
    for (x, y) in [(11, 14), (13, 14), (14, 12)]:
        d.point((x, y), fill=hexc("d99a8c"))
    d.ellipse([10, 11, 13, 14], fill=hexc("d99a8c"))
    d.point((4, 3), fill=hexc("9a908a"))
    return img


def tex_rollerskates():
    img = new()
    d = ImageDraw.Draw(img)
    d.polygon([(4, 2), (9, 2), (9, 8), (13, 9), (13, 11), (3, 11), (3, 8)], fill=hexc("e04a8c"), outline=hexc("8a1f50"))
    d.line([(5, 4), (8, 4)], fill=hexc("ffffff"))
    d.line([(5, 6), (8, 6)], fill=hexc("ffffff"))
    d.rectangle([3, 11, 13, 12], fill=hexc("d99a8c"))
    for x in (4, 11):
        d.ellipse([x - 1, 12, x + 1, 14], fill=hexc("6fdbe6"), outline=hexc("1d4e5e"))
    return img


def tex_tonic():
    img = new()
    d = ImageDraw.Draw(img)
    d.rectangle([7, 1, 8, 3], fill=hexc("a8784c"))
    d.rectangle([6, 4, 9, 5], fill=(220, 240, 250, 200))
    d.ellipse([3, 5, 12, 14], fill=(220, 240, 250, 200))
    for y in range(8, 14):
        for x in range(4, 12):
            if math.hypot(x - 7.5, y - 9.5) < 4.2:
                d.point((x, y), fill=hsv((x + y) / 14.0, 0.6, 1.0))
    d.point((5, 8), fill=(255, 255, 255, 255))
    return img


def tex_dummy_item():
    img = new()
    d = ImageDraw.Draw(img)
    burlap = hexc("c9a66b")
    d.rectangle([5, 1, 10, 5], fill=burlap, outline=hexc("8a6a3a"))
    d.point((6, 3), fill=hexc("3a2a1a"))
    d.point((9, 3), fill=hexc("3a2a1a"))
    d.rectangle([5, 6, 10, 10], fill=burlap, outline=hexc("8a6a3a"))
    d.line([(4, 6), (2, 10)], fill=burlap, width=2)
    d.line([(11, 6), (13, 10)], fill=burlap, width=2)
    d.line([(6, 11), (5, 15)], fill=burlap, width=2)
    d.line([(9, 11), (10, 15)], fill=burlap, width=2)
    d.line([(7, 7), (8, 9)], fill=hexc("d23c3c"))
    return img


# ------------------------------------------------------------------------------- entity textures
def paint_box(img, u, v, w, h, d, color_fn):
    """Paints a ModelPart box at (u,v) of size w*h*d. color_fn(face, x, y, fw, fh) -> rgba."""
    px = img.load()
    faces = {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "west": (u, v + d, d, h), "north": (u + d, v + d, w, h),
        "east": (u + d + w, v + d, d, h), "south": (u + 2 * d + w, v + d, w, h),
    }
    for face, (fx, fy, fw, fh) in faces.items():
        for y in range(fh):
            for x in range(fw):
                if 0 <= fx + x < img.width and 0 <= fy + y < img.height:
                    px[fx + x, fy + y] = color_fn(face, x, y, fw, fh)


def flat(pal, seed):
    rng = random.Random(seed)
    return lambda face, x, y, fw, fh: rng.choice(pal)


def ent_gravel_mite():
    img = new(32, 32)
    body = [hexc("7d7470"), hexc("8a817c"), hexc("6e6561")]

    def body_fn(face, x, y, fw, fh):
        if face == "top" and y % 2 == 0:
            return hexc("5e5652")
        return random.Random(x * 31 + y * 7).choice(body)

    def head_fn(face, x, y, fw, fh):
        if face == "north" and y == 0 and x in (0, fw - 1):
            return hexc("1a1a1a")
        return hexc("5e5652")

    paint_box(img, 0, 0, 5, 3, 6, body_fn)
    paint_box(img, 0, 9, 3, 2, 2, head_fn)
    paint_box(img, 10, 9, 3, 1, 1, lambda *a: hexc("d99a8c"))  # pink little feet!
    return img


def ent_prism_snail():
    img = new(64, 32)
    paint_box(img, 0, 0, 4, 2, 12, flat([hexc("c9b28f"), hexc("bfa684"), hexc("d4bf9c")], 1))
    paint_box(img, 0, 14, 1, 4, 1, lambda f, x, y, fw, fh: hexc("1a1a1a") if y == 0 else hexc("c9b28f"))

    def shell(face, x, y, fw, fh):
        # pale crystal shell with a spiral; tinted by the snail's colour at render time
        cx, cy = (fw - 1) / 2, (fh - 1) / 2
        a = math.atan2(y - cy, x - cx)
        r = math.hypot(x - cx, y - cy)
        band = int((r * 1.6 + a / math.pi * 2) % 3)
        return [hexc("ffffff"), hexc("dce4ea"), hexc("b8c4cc")][band]

    paint_box(img, 0, 16, 6, 7, 7, shell)
    return img


def ent_rollypin():
    img = new(64, 64)
    scale_pal = [hexc("8b5a2b"), hexc("9c6a36"), hexc("7a4c22")]

    def scales(face, x, y, fw, fh):
        if (x + (y // 2) * 2) % 4 == 0 or y % 2 == 1 and x % 4 == 2:
            return hexc("5e3b17")
        return scale_pal[(x * 3 + y) % 3]

    def head(face, x, y, fw, fh):
        if face == "north" and y == 1 and x in (0, fw - 1):
            return hexc("1a1a1a")
        return hexc("d9a066")

    paint_box(img, 0, 0, 7, 6, 12, scales)
    paint_box(img, 0, 20, 5, 4, 5, head)
    paint_box(img, 22, 20, 2, 2, 2, lambda *a: hexc("c48850"))
    paint_box(img, 0, 30, 4, 3, 9, scales)
    paint_box(img, 40, 20, 2, 4, 2, lambda *a: hexc("c48850"))
    paint_box(img, 0, 44, 8, 8, 8, scales)
    return img


def ent_glowtoad(glow):
    img = new(64, 32)
    if glow:
        paint_box(img, 32, 0, 4, 2, 3, lambda f, x, y, fw, fh: hexc("f7e26b") if f in ("bottom", "north", "west", "east") else (0, 0, 0, 0))
        paint_box(img, 20, 13, 2, 2, 2, lambda f, x, y, fw, fh: hexc("ffd84a") if f == "north" and (x, y) == (0, 0) else (0, 0, 0, 0))
        return img
    skin = [hexc("4f7a3a"), hexc("5c8a44"), hexc("436832")]
    rng = random.Random(3)

    def warts(face, x, y, fw, fh):
        if rng.random() < 0.1:
            return hexc("7aa356")
        return rng.choice(skin)

    paint_box(img, 0, 0, 7, 4, 9, warts)
    paint_box(img, 0, 13, 6, 3, 4, warts)
    paint_box(img, 20, 13, 2, 2, 2, lambda f, x, y, fw, fh: hexc("1a1a1a") if f == "north" and (x, y) == (0, 0) else hexc("c9a43a"))
    paint_box(img, 32, 0, 4, 2, 3, lambda *a: hexc("d9c25a"))
    paint_box(img, 0, 22, 2, 3, 2, warts)
    paint_box(img, 10, 22, 3, 2, 5, warts)
    return img


def ent_shardling(glow):
    img = new(64, 64)
    if glow:
        paint_box(img, 0, 16, 6, 6, 6, lambda f, x, y, fw, fh: hexc("e8ffff") if f == "north" and y == 2 and x in (1, 4) else (0, 0, 0, 0))
        paint_box(img, 30, 0, 2, 6, 2, lambda f, x, y, fw, fh: (150, 255, 255, 255) if y < 2 else (0, 0, 0, 0))
        return img

    def crystal(seed):
        rng = random.Random(seed)

        def fn(face, x, y, fw, fh):
            t = (x + y) / max(1, fw + fh)
            c = mix(SPEC_LIGHT, SPEC_DARK, t)
            if rng.random() < 0.08:
                return SPEC_HI
            return c
        return fn

    def head(face, x, y, fw, fh):
        if face == "north" and y == 2 and x in (1, 4):
            return hexc("0f2a33")
        return crystal(9)(face, x, y, fw, fh)

    paint_box(img, 0, 0, 8, 9, 6, crystal(1))
    paint_box(img, 0, 16, 6, 6, 6, head)
    paint_box(img, 30, 0, 2, 6, 2, crystal(2))
    paint_box(img, 30, 10, 2, 4, 2, crystal(3))
    paint_box(img, 40, 0, 3, 11, 3, crystal(4))
    paint_box(img, 40, 16, 3, 9, 3, crystal(5))
    return img


def biped(img, head_fn, body_fn, arm_fn, leg_fn, limb=4):
    paint_box(img, 0, 0, 8, 8, 8, head_fn)
    paint_box(img, 16, 16, 8, 12, 4, body_fn)
    paint_box(img, 40, 16, limb, 12, limb, arm_fn)
    paint_box(img, 0, 16, limb, 12, limb, leg_fn)
    if img.height >= 64:
        paint_box(img, 32, 48, limb, 12, limb, arm_fn)
        paint_box(img, 16, 48, limb, 12, limb, leg_fn)


def ent_skate_ghoul(glow):
    img = new(64, 64)
    if glow:
        paint_box(img, 0, 0, 8, 8, 8, lambda f, x, y, fw, fh: hexc("ff5fb0") if f == "north" and y == 3 and x in (1, 2, 5, 6) else (0, 0, 0, 0))
        return img
    skin = [hexc("5d7a5a"), hexc("6a8866"), hexc("52704f")]
    rng = random.Random(21)

    def head(face, x, y, fw, fh):
        if face == "north":
            if y == 3 and x in (1, 2, 5, 6):
                return hexc("2a0f1a")
            if y == 6 and 2 <= x <= 5:
                return hexc("3a2a2a")
        if y < 2:  # backwards cap
            return hexc("e04a8c") if face != "south" or y else hexc("ffd84a")
        return rng.choice(skin)

    def body(face, x, y, fw, fh):
        if y < 8:
            stripe = (x + y) % 6 < 2
            return hexc("ffd84a") if stripe else hexc("2f6fb0")
        return hexc("3a3f4a")

    def arm(face, x, y, fw, fh):
        return hexc("2f6fb0") if y < 4 else rng.choice(skin)

    def leg(face, x, y, fw, fh):
        return hexc("3a3f4a") if y < 10 else hexc("e04a8c")

    biped(img, head, body, arm, leg)
    return img


def ent_ragdoll_dummy():
    img = new(64, 64)
    burlap = [hexc("c9a66b"), hexc("bf9a5e"), hexc("d4b37a")]
    rng = random.Random(30)

    def cloth(face, x, y, fw, fh):
        if (x + y) % 5 == 0:
            return hexc("a8854f")
        return rng.choice(burlap)

    def head(face, x, y, fw, fh):
        if face == "north":
            if (x, y) in [(1, 2), (2, 3), (1, 4), (3, 2), (3, 4), (5, 3), (6, 3)]:
                return hexc("3a2a1a")  # X eye and a button eye
            if y == 6 and 2 <= x <= 5:
                return hexc("d23c3c")  # stitched mouth
        return cloth(face, x, y, fw, fh)

    def body(face, x, y, fw, fh):
        if face == "north" and abs(x - 3.5) + abs(y - 4) < 2.5:
            return hexc("d23c3c")  # target
        return cloth(face, x, y, fw, fh)

    biped(img, head, body, cloth, cloth)
    return img


def armor_layer(kind):
    img = new(64, 32)
    rng = random.Random(40 + len(kind))

    def glass(face, x, y, fw, fh):
        t = (x + y) / max(1, fw + fh)
        c = mix(SPEC_HI, SPEC_MID, t)
        if (x + 2 * y) % 7 == 0:
            c = hsv(rng.random(), 0.45, 1.0)
        if x == 0 or y == 0:
            c = SPEC_DARK
        return c

    if kind == "speculum_1":
        paint_box(img, 0, 0, 8, 8, 8, lambda f, x, y, fw, fh: (0, 0, 0, 0) if f == "north" and 2 <= y <= 4 and 1 <= x <= 6 else glass(f, x, y, fw, fh))
        paint_box(img, 16, 16, 8, 12, 4, glass)
        paint_box(img, 40, 16, 4, 12, 4, glass)
        paint_box(img, 0, 16, 4, 12, 4, lambda f, x, y, fw, fh: glass(f, x, y, fw, fh) if y >= 7 else (0, 0, 0, 0))
    elif kind == "speculum_2":
        paint_box(img, 16, 16, 8, 12, 4, lambda f, x, y, fw, fh: glass(f, x, y, fw, fh) if y >= 7 else (0, 0, 0, 0))
        paint_box(img, 0, 16, 4, 12, 4, lambda f, x, y, fw, fh: glass(f, x, y, fw, fh) if y < 9 else (0, 0, 0, 0))
    else:  # rollerskates: boots only (bottom of the leg box on layer 1)
        def skate(face, x, y, fw, fh):
            if y < 7:
                return (0, 0, 0, 0)
            if y >= 11:
                return hexc("6fdbe6") if x % 3 == 1 else hexc("d99a8c")
            return hexc("e04a8c") if y != 8 else hexc("ffffff")
        paint_box(img, 0, 16, 4, 12, 4, skate)
    return img


def particle_firefly():
    img = new(8, 8)
    px = img.load()
    for y in range(8):
        for x in range(8):
            d = math.hypot(x - 3.5, y - 3.5)
            a = max(0, 1 - d / 3.6)
            px[x, y] = (255, 255, 255, int(255 * a ** 1.4))
    return img


def particle_spark(frame):
    img = new(8, 8)
    px = img.load()
    size = 3 - frame
    for i in range(-size, size + 1):
        px[3 + i, 3] = (255, 255, 255, 255)
        px[3, 3 + i] = (255, 255, 255, 255)
    if size >= 2:
        px[2, 2] = px[4, 4] = px[2, 4] = px[4, 2] = (255, 255, 255, 160)
    return img


def icon():
    img = new(128, 128, hexc("15202b"))
    d = ImageDraw.Draw(img)
    for i in range(7):
        d.polygon([(20 + i * 14, 0), (34 + i * 14, 0), (0 + i * 14, 128), (-14 + i * 14, 128)], fill=hsv(i / 7, 0.55, 0.45))
    crystal = tex_crystal_cross(15, 3, 9).resize((96, 96), Image.NEAREST)
    img.alpha_composite(crystal, (16, 12))
    return img


# ------------------------------------------------------------------------------------- json bits
def bs(name, variants):
    write_json(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": variants})


def bmodel(name, obj):
    write_json(os.path.join(ASSETS, "models", "block", name + ".json"), obj)


def imodel(name, obj):
    write_json(os.path.join(ASSETS, "models", "item", name + ".json"), obj)


def item_generated(name, texture=None):
    imodel(name, {"parent": "minecraft:item/generated", "textures": {"layer0": texture or m("item/" + name)}})


def item_block(name, model=None):
    imodel(name, {"parent": model or m("block/" + name)})


def cube_all(name, texture=None):
    bmodel(name, {"parent": "minecraft:block/cube_all", "textures": {"all": texture or m("block/" + name)}})
    bs(name, {"": {"model": m("block/" + name)}})
    item_block(name)


def loot_self(name, block=None):
    write_json(os.path.join(DATA, "loot_table", "blocks", name + ".json"), {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": m(block or name)}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
        "random_sequence": m("blocks/" + name)})


SILK = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
    {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}


def loot_silk_only(name):
    write_json(os.path.join(DATA, "loot_table", "blocks", name + ".json"), {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": m(name)}], "conditions": [SILK]}],
        "random_sequence": m("blocks/" + name)})


def recipe(name, obj):
    write_json(os.path.join(DATA, "recipe", name + ".json"), obj)


def shaped(name, pattern, key, result, count=1, components=None):
    res = {"id": result, "count": count}
    if components:
        res["components"] = components
    recipe(name, {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
                  "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in key.items()},
                  "result": res})


def shapeless(name, ingredients, result, count=1):
    recipe(name, {"type": "minecraft:crafting_shapeless", "category": "misc",
                  "ingredients": [({"tag": v[1:]} if v.startswith("#") else {"item": v}) for v in ingredients],
                  "result": {"id": result, "count": count}})


_written_tags = {}


def tag(registry, namespace_dir, name, values):
    path = os.path.join(namespace_dir, "tags", registry, name + ".json")
    merged = _written_tags.setdefault(path, [])
    merged += [v for v in values if v not in merged]
    write_json(path, {"replace": False, "values": merged})


def entity_loot(name, pools):
    write_json(os.path.join(DATA, "loot_table", "entities", name + ".json"),
               {"type": "minecraft:entity", "pools": pools, "random_sequence": m("entities/" + name)})


def drop_pool(item, lo, hi, looting=True, extra_functions=None):
    funcs = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]
    if looting:
        funcs.append({"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
                      "count": {"type": "minecraft:uniform", "min": 0, "max": 1}})
    funcs += extra_functions or []
    return {"rolls": 1, "entries": [{"type": "minecraft:item", "name": item, "functions": funcs}]}


# ======================================================================================= V2 aquatic
def scales(pal, seed):
    rng = random.Random(seed)

    def fn(face, x, y, fw, fh):
        c = pal[(x + y * 2) % len(pal)] if rng.random() < 0.7 else rng.choice(pal)
        if face == "bottom":
            c = mix(c, (255, 255, 255, 255), 0.45)
        return c
    return fn


def with_eye(base, eye=hexc("101010"), at=(0, 0)):
    def fn(face, x, y, fw, fh):
        if face in ("west", "east") and (x, y) == ((fw - 1 - at[0]) if face == "west" else at[0], at[1]):
            return eye
        return base(face, x, y, fw, fh)
    return fn


def fin(color):
    return lambda face, x, y, fw, fh: color if (x + y) % 3 else shade(color, 0.8)


GLIMMER_HUES = [0.52, 0.9, 0.12, 0.75]


def ent_glimmerfin(variant, glow):
    img = new(32, 32)
    h = GLIMMER_HUES[variant]
    if glow:
        stripe = hsv(h, 0.35, 1.0)
        paint_box(img, 0, 0, 2, 3, 6, lambda f, x, y, fw, fh: stripe if f in ("west", "east") and y == 1 else (0, 0, 0, 0))
        paint_box(img, 16, 0, 0, 3, 3, lambda f, x, y, fw, fh: stripe if (x + y) % 2 == 0 else (0, 0, 0, 0))
        return img
    body = scales([hsv(h, 0.65, 0.75), hsv(h, 0.55, 0.85), hsv(h + 0.03, 0.7, 0.65)], 50 + variant)
    paint_box(img, 0, 0, 2, 3, 6, with_eye(body, at=(0, 0)))
    paint_box(img, 16, 0, 0, 3, 3, fin(hsv(h, 0.4, 0.95)))
    paint_box(img, 16, 6, 0, 1, 3, fin(hsv(h, 0.4, 0.95)))
    return img


def ent_carp():
    img = new(64, 32)
    body = scales([hexc("e0a420"), hexc("f2bf3a"), hexc("c98a12"), hexc("f7d264")], 60)
    paint_box(img, 0, 0, 4, 5, 9, body)
    paint_box(img, 0, 14, 3, 4, 3, with_eye(scales([hexc("d4961a"), hexc("e6ab2a")], 61), at=(0, 1)))
    paint_box(img, 28, 0, 0, 5, 5, fin(hexc("f08a24")))
    paint_box(img, 28, 10, 0, 2, 6, fin(hexc("f08a24")))
    return img


def ent_sea_bunny():
    img = new(32, 32)
    rng = random.Random(62)
    paint_box(img, 0, 0, 3, 2, 5, lambda f, x, y, fw, fh: hexc("1a1a1a") if rng.random() < 0.12 else rng.choice([hexc("ffffff"), hexc("f2f2f0"), hexc("fffbe8")]))
    paint_box(img, 0, 8, 1, 2, 1, lambda f, x, y, fw, fh: hexc("1a1a1a") if y == 0 else hexc("f7e27a"))
    paint_box(img, 6, 8, 2, 1, 1, lambda *a: hexc("fff6c8"))
    return img


def ent_hermit_crab():
    img = new(64, 32)
    red = scales([hexc("d8553a"), hexc("c94a30"), hexc("e8704f")], 63)
    paint_box(img, 0, 0, 4, 2, 3, red)
    paint_box(img, 14, 0, 2, 2, 2, lambda f, x, y, fw, fh: hexc("f2d7a6") if f == "north" else hexc("d8553a"))
    paint_box(img, 0, 5, 1, 2, 1, lambda f, x, y, fw, fh: hexc("101010") if y == 0 else hexc("e8704f"))
    paint_box(img, 0, 8, 3, 1, 1, lambda *a: hexc("c94a30"))

    def shell(face, x, y, fw, fh):  # pale spiral, tinted per crab
        cx, cy = (fw - 1) / 2, (fh - 1) / 2
        r = math.hypot(x - cx, y - cy)
        a = math.atan2(y - cy, x - cx)
        return [hexc("ffffff"), hexc("e3e3e3"), hexc("c8c8c8")][int(r * 1.4 + a / math.pi * 1.5) % 3]
    paint_box(img, 0, 12, 6, 5, 6, shell)
    return img


def ent_seahorse():
    img = new(32, 32)
    body = scales([hexc("f28c28"), hexc("ffa640"), hexc("e07a1a")], 64)

    def ridged(face, x, y, fw, fh):
        return hexc("c9631a") if y % 2 == 0 and face in ("south",) else body(face, x, y, fw, fh)
    paint_box(img, 0, 0, 2, 6, 2, ridged)
    paint_box(img, 8, 0, 2, 2, 3, with_eye(body, at=(1, 0)))
    paint_box(img, 20, 0, 1, 1, 2, lambda *a: hexc("ffb450"))
    paint_box(img, 0, 8, 1, 4, 1, ridged)
    paint_box(img, 4, 8, 1, 2, 1, ridged)
    paint_box(img, 8, 6, 0, 3, 2, fin(hexc("ffd25a")))
    return img


def ent_drift_jelly(glow):
    img = new(64, 32)
    if glow:
        def rim(face, x, y, fw, fh):
            if face in ("north", "south", "west", "east") and y == fh - 1:
                return hexc("d6a6ff")
            if face == "top" and (x + y) % 5 == 0:
                return hexc("b8f4ff")
            return (0, 0, 0, 0)
        paint_box(img, 0, 0, 8, 4, 8, rim)
        paint_box(img, 32, 0, 1, 9, 1, lambda f, x, y, fw, fh: hexc("ffb8ff") if y % 3 == 0 else (0, 0, 0, 0))
        return img

    def bell(face, x, y, fw, fh):
        t = y / max(1, fh - 1)
        return (210, 160, 255, int(150 + 60 * (1 - t))) if (x + y) % 4 else (240, 210, 255, 210)
    paint_box(img, 0, 0, 8, 4, 8, bell)
    paint_box(img, 32, 0, 1, 9, 1, lambda f, x, y, fw, fh: (230, 180, 255, 170))
    paint_box(img, 36, 0, 2, 7, 2, lambda f, x, y, fw, fh: (255, 200, 240, 190) if y % 2 else (240, 170, 230, 190))
    return img


def ent_stingray():
    img = new(64, 32)
    rng = random.Random(65)
    top = [hexc("8a7a63"), hexc("7d6e58"), hexc("97876e")]

    def body(face, x, y, fw, fh):
        if face == "bottom":
            return hexc("e8e1d6")
        if face == "top" and rng.random() < 0.08:
            return hexc("4a4034")
        return rng.choice(top)
    paint_box(img, 0, 0, 4, 2, 9, with_eye(body, at=(1, 0)))
    paint_box(img, 0, 12, 5, 1, 8, body)
    paint_box(img, 26, 0, 1, 1, 10, lambda f, x, y, fw, fh: hexc("3d342a") if (f in ("west", "east") and x > 6) else hexc("6e604c"))
    return img


def ent_piranha(glow):
    img = new(32, 32)
    if glow:
        paint_box(img, 0, 0, 2, 4, 5, lambda f, x, y, fw, fh: hexc("ff3030") if f in ("west", "east") and (x, y) == ((fw - 1) if f == "west" else 0, 0) else (0, 0, 0, 0))
        return img

    def body(face, x, y, fw, fh):
        if face in ("west", "east"):
            if y >= fh - 2:
                return hexc("d22f2f") if (x + y) % 3 else hexc("b02424")
            return hexc("8f9aa3") if (x + y) % 3 else hexc("7a858e")
        if face == "bottom":
            return hexc("d22f2f")
        return hexc("6f7a83")
    paint_box(img, 0, 0, 2, 4, 5, body)
    paint_box(img, 0, 9, 2, 1, 2, lambda f, x, y, fw, fh: hexc("ffffff") if f in ("north", "west", "east") and x % 2 == 0 else hexc("b02424"))
    paint_box(img, 14, 0, 0, 4, 3, fin(hexc("5e6870")))
    paint_box(img, 14, 7, 0, 2, 3, fin(hexc("5e6870")))
    return img


def ent_barracuda():
    img = new(64, 32)

    def body(face, x, y, fw, fh):
        if face == "bottom":
            return hexc("eef2f5")
        if face in ("west", "east") and x % 4 == 0 and y < fh - 1:
            return hexc("2f3a45")
        if face == "top":
            return hexc("4d5a66")
        return hexc("c3ccd3") if (x + y) % 3 else hexc("b0bac2")
    paint_box(img, 0, 0, 3, 3, 14, body)
    paint_box(img, 0, 17, 2, 2, 4, with_eye(body, eye=hexc("f2d23a"), at=(1, 0)))
    paint_box(img, 12, 17, 2, 1, 4, lambda f, x, y, fw, fh: hexc("ffffff") if f in ("west", "east") and x % 2 == 0 else hexc("c3ccd3"))
    paint_box(img, 34, 0, 0, 5, 5, fin(hexc("3c4854")))
    paint_box(img, 34, 10, 0, 2, 4, fin(hexc("3c4854")))
    return img


def ent_angler(glow):
    img = new(64, 64)
    if glow:
        paint_box(img, 48, 0, 2, 2, 2, lambda *a: hexc("b6fff0"))
        paint_box(img, 0, 0, 10, 8, 12, lambda f, x, y, fw, fh: hexc("7fffd4") if f in ("west", "east") and (x, y) in ((2, 1), (fw - 3, 1)) else (0, 0, 0, 0))
        return img
    rng = random.Random(66)
    dark = [hexc("1b1f2e"), hexc("232838"), hexc("161925")]

    def skin(face, x, y, fw, fh):
        if rng.random() < 0.05:
            return hexc("3a4060")
        return rng.choice(dark)
    paint_box(img, 0, 0, 10, 8, 12, skin)

    def jaw(face, x, y, fw, fh):
        if face in ("north", "west", "east") and y == 0 and x % 2 == 0:
            return hexc("e8e4d8")
        return rng.choice(dark)
    paint_box(img, 0, 20, 9, 2, 9, jaw)
    paint_box(img, 44, 0, 1, 6, 1, lambda *a: hexc("2b3045"))
    paint_box(img, 48, 0, 2, 2, 2, lambda *a: hexc("7fffd4"))
    paint_box(img, 0, 32, 0, 6, 6, fin(hexc("2b3045")))
    return img


def item_fish(body_col, fin_col, eye=hexc("101010"), cooked=False, long=False):
    img = new()
    d = ImageDraw.Draw(img)
    if long:
        d.polygon([(1, 9), (4, 7), (12, 7), (14, 8), (12, 10), (4, 10)], fill=body_col)
        d.polygon([(12, 8), (15, 5), (15, 12)], fill=fin_col)
        d.point((3, 8), fill=eye)
    else:
        d.ellipse([2, 5, 11, 11], fill=body_col)
        d.polygon([(10, 8), (14, 4), (14, 12)], fill=fin_col)
        d.point((4, 7), fill=eye)
    if cooked:
        for x in range(3, 11, 3):
            d.line([(x, 6), (x + 1, 10)], fill=shade(body_col, 0.7))
    return img


def item_bucket(fish_col):
    img = new()
    d = ImageDraw.Draw(img)
    d.polygon([(2, 4), (13, 4), (12, 14), (3, 14)], fill=hexc("a7a7a7"), outline=hexc("5d5d5d"))
    d.rectangle([3, 4, 12, 6], fill=hexc("3f76e4"))
    d.ellipse([5, 6, 10, 10], fill=fish_col)
    d.line([(2, 4), (13, 4)], fill=hexc("d8d8d8"))
    d.arc([3, 0, 12, 8], 180, 360, fill=hexc("5d5d5d"))
    return img


def item_simple(shape, col, col2):
    img = new()
    d = ImageDraw.Draw(img)
    if shape == "gel":
        d.ellipse([3, 5, 12, 13], fill=col)
        d.ellipse([5, 6, 8, 9], fill=col2)
    elif shape == "barb":
        d.line([(3, 13), (12, 3)], fill=col, width=2)
        d.polygon([(12, 1), (14, 4), (11, 4)], fill=col2)
        for i in range(4, 12, 2):
            d.point((i, 15 - i - 1), fill=col2)
    elif shape == "tooth":
        d.polygon([(5, 3), (11, 3), (8, 14)], fill=col, outline=col2)
    elif shape == "lure":
        d.line([(8, 1), (8, 8)], fill=col2)
        d.ellipse([5, 8, 11, 14], fill=col)
        d.ellipse([6, 9, 8, 11], fill=(255, 255, 255, 255))
    elif shape == "shell":
        d.polygon([(3, 12), (13, 12), (11, 5), (8, 3), (5, 5)], fill=col, outline=col2)
        for i in range(5, 12, 2):
            d.line([(8, 4), (i, 12)], fill=col2)
    return img


def tex_anemone():
    img = new()
    d = ImageDraw.Draw(img)
    for i, x in enumerate(range(2, 15, 2)):
        h = 8 + (i * 5) % 6
        col = hexc("ff6fb5") if i % 2 else hexc("ff9ad0")
        d.line([(8, 15), (x, 15 - h)], fill=col)
        d.point((x, 15 - h), fill=hexc("fff0a0"))
    d.rectangle([6, 13, 10, 15], fill=hexc("b0306f"))
    return img


def tex_jelly_lamp():
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            d = math.hypot(x - 7.5, y - 7.5)
            px[x, y] = (230, 150, 255, 230) if edge else (240, int(170 + 40 * (d / 11)), 255, 170)
    for (x, y) in [(4, 4), (5, 4), (4, 5), (10, 9), (11, 10)]:
        px[x, y] = (255, 255, 255, 240)
    return img


def tex_angler_lantern():
    img = new()
    px = img.load()
    for y in range(16):
        for x in range(16):
            if y < 10 and x < 8:  # side
                edge = x in (0, 7) or y in (0, 9)
                d = math.hypot(x - 3.5, y - 5)
                px[x, y] = hexc("2b3045") if edge else (mix(hexc("e6fffa"), hexc("4fd6b8"), min(1, d / 4)))
            elif y < 8 and 8 <= x < 16:  # top/bottom
                px[x, y] = hexc("2b3045") if x in (8, 15) or y in (0, 7) else hexc("3a4060")
            elif y >= 10 and x < 8:  # cap side
                px[x, y] = hexc("1b1f2e")
    return img


def particle_plankton():
    img = new(4, 4)
    px = img.load()
    for y in range(4):
        for x in range(4):
            d = math.hypot(x - 1.5, y - 1.5)
            px[x, y] = (255, 255, 255, int(255 * max(0, 1 - d / 2.2)))
    return img


def aquatic_assets():
    for v in range(4):
        save(ent_glimmerfin(v, False), "entity", f"glimmerfin_{v}")
        save(ent_glimmerfin(v, True), "entity", f"glimmerfin_glow_{v}")
    save(ent_carp(), "entity", "sunscale_carp")
    save(ent_sea_bunny(), "entity", "sea_bunny")
    save(ent_hermit_crab(), "entity", "hermit_crab")
    save(ent_seahorse(), "entity", "seahorse")
    save(ent_drift_jelly(False), "entity", "drift_jelly")
    save(ent_drift_jelly(True), "entity", "drift_jelly_glow")
    save(ent_stingray(), "entity", "stingray")
    save(ent_piranha(False), "entity", "piranha")
    save(ent_piranha(True), "entity", "piranha_glow")
    save(ent_barracuda(), "entity", "reef_barracuda")
    save(ent_angler(False), "entity", "deep_angler")
    save(ent_angler(True), "entity", "deep_angler_glow")

    save(item_fish(hsv(0.52, 0.6, 0.8), hsv(0.52, 0.35, 1.0)), "item", "glimmerfin")
    save(item_fish(hexc("e0a420"), hexc("f08a24")), "item", "sunscale_carp")
    save(item_fish(hexc("c98a4a"), hexc("a8703a"), cooked=True), "item", "cooked_sunscale_carp")
    save(item_fish(hexc("8f9aa3"), hexc("d22f2f")), "item", "piranha")
    save(item_fish(hexc("b08a6a"), hexc("8a6a4a"), cooked=True), "item", "cooked_piranha")
    save(item_fish(hexc("e8b0a0"), hexc("c3ccd3"), long=True), "item", "barracuda_fillet")
    save(item_fish(hexc("c9906a"), hexc("a8703a"), cooked=True, long=True), "item", "cooked_barracuda_fillet")
    save(item_simple("gel", (220, 150, 255, 220), (250, 230, 255, 255)), "item", "jelly_gel")
    save(item_simple("barb", hexc("6e604c"), hexc("3d342a")), "item", "stingray_barb")
    save(item_simple("tooth", hexc("f4f0e4"), hexc("b8b0a0")), "item", "piranha_tooth")
    save(item_simple("lure", hexc("7fffd4"), hexc("2b3045")), "item", "angler_lure")
    save(item_simple("shell", hexc("f2d7a6"), hexc("c49a6a")), "item", "hermit_shell")
    for name, col in (("glimmerfin", hsv(0.52, 0.6, 0.9)), ("sunscale_carp", hexc("e0a420")), ("seahorse", hexc("f28c28")),
                      ("sea_bunny", hexc("ffffff")), ("piranha", hexc("d22f2f"))):
        save(item_bucket(col), "item", name + "_bucket")
    save(tex_anemone(), "block", "glow_anemone")
    save(tex_jelly_lamp(), "block", "jelly_lamp")
    save(tex_angler_lantern(), "block", "angler_lantern")
    save(particle_plankton(), "particle", "plankton")
    write_json(os.path.join(ASSETS, "particles", "plankton.json"), {"textures": [m("plankton")]})

    # blocks
    bmodel("glow_anemone", {"parent": "minecraft:block/cross", "textures": {"cross": m("block/glow_anemone")}})
    bs("glow_anemone", {"": {"model": m("block/glow_anemone")}})
    item_generated("glow_anemone", m("block/glow_anemone"))
    bmodel("jelly_lamp", {"parent": "minecraft:block/cube_all", "textures": {"all": m("block/jelly_lamp")}})
    bs("jelly_lamp", {"": {"model": m("block/jelly_lamp")}})
    item_block("jelly_lamp")
    lt = "#t"
    six = ("down", "up", "north", "south", "west", "east")
    bmodel("angler_lantern", {"parent": "minecraft:block/block", "ambientocclusion": False,
                              "textures": {"t": m("block/angler_lantern"), "particle": m("block/angler_lantern")},
                              "elements": [
                                  {"from": [4, 0, 4], "to": [12, 8, 12], "faces": {
                                      "down": {"texture": lt, "uv": [8, 0, 16, 8]}, "up": {"texture": lt, "uv": [8, 0, 16, 8]},
                                      **{d: {"texture": lt, "uv": [0, 1, 8, 9]} for d in ("north", "south", "west", "east")}}},
                                  {"from": [5, 8, 5], "to": [11, 10, 11], "faces": {
                                      "up": {"texture": lt, "uv": [9, 1, 15, 7]},
                                      **{d: {"texture": lt, "uv": [0, 10, 6, 12]} for d in ("north", "south", "west", "east")}}}]})
    bs("angler_lantern", {"": {"model": m("block/angler_lantern")}})
    item_block("angler_lantern")

    for name in ("glimmerfin", "sunscale_carp", "cooked_sunscale_carp", "piranha", "cooked_piranha", "barracuda_fillet",
                 "cooked_barracuda_fillet", "jelly_gel", "stingray_barb", "piranha_tooth", "angler_lure", "hermit_shell",
                 "glimmerfin_bucket", "sunscale_carp_bucket", "seahorse_bucket", "sea_bunny_bucket", "piranha_bucket"):
        item_generated(name)
    for egg in ("glimmerfin", "sunscale_carp", "sea_bunny", "hermit_crab", "seahorse", "drift_jelly", "stingray", "piranha",
                "reef_barracuda", "deep_angler"):
        imodel(egg + "_spawn_egg", {"parent": "minecraft:item/template_spawn_egg"})

    # loot
    for name in ("angler_lantern", "jelly_lamp"):
        loot_self(name)
    loot_silk_only("glow_anemone")
    on_fire = {"function": "minecraft:furnace_smelt", "conditions": [{"condition": "minecraft:entity_properties", "entity": "this",
                                                                      "predicate": {"flags": {"is_on_fire": True}}}]}
    entity_loot("glimmerfin", [drop_pool(m("glimmerfin"), 1, 1, looting=False)])
    entity_loot("sunscale_carp", [drop_pool(m("sunscale_carp"), 1, 1, looting=False, extra_functions=[on_fire])])
    entity_loot("sea_bunny", [drop_pool("minecraft:slime_ball", 0, 1)])
    entity_loot("hermit_crab", [drop_pool(m("hermit_shell"), 0, 1)])
    entity_loot("seahorse", [])
    entity_loot("drift_jelly", [drop_pool(m("jelly_gel"), 1, 2)])
    entity_loot("stingray", [drop_pool(m("stingray_barb"), 0, 1)])
    entity_loot("piranha", [drop_pool(m("piranha"), 1, 1, looting=False, extra_functions=[on_fire]), drop_pool(m("piranha_tooth"), 0, 1)])
    entity_loot("reef_barracuda", [drop_pool(m("barracuda_fillet"), 1, 2, extra_functions=[on_fire])])
    entity_loot("deep_angler", [drop_pool(m("angler_lure"), 1, 1, looting=False), drop_pool("minecraft:bone_meal", 1, 3)])

    # recipes
    for raw, cooked in (("sunscale_carp", "cooked_sunscale_carp"), ("piranha", "cooked_piranha"), ("barracuda_fillet", "cooked_barracuda_fillet")):
        for kind, t in (("smelting", 200), ("smoking", 100), ("campfire_cooking", 600)):
            recipe(f"{cooked}_from_{kind}", {"type": f"minecraft:{kind}", "category": "food", "ingredient": {"item": m(raw)},
                                             "result": {"id": m(cooked)}, "experience": 0.35, "cookingtime": t})
    shaped("angler_lantern", [" N ", "GLG", " G "], {"N": "minecraft:iron_nugget", "G": "minecraft:glass", "L": m("angler_lure")}, m("angler_lantern"))
    shaped("jelly_lamp", ["JJ", "JJ"], {"J": m("jelly_gel")}, m("jelly_lamp"))
    shapeless("bone_meal_from_hermit_shell", [m("hermit_shell")], "minecraft:bone_meal", 3)

    # tags
    sea = [m(n) for n in ("glimmerfin", "sunscale_carp", "sea_bunny", "seahorse", "drift_jelly", "stingray", "piranha",
                          "reef_barracuda", "deep_angler")]
    tag("entity_type", os.path.join(ROOT, "data", NS), "aquatic_predators", [
        m("reef_barracuda"), m("piranha"), "minecraft:dolphin", "minecraft:axolotl", "minecraft:guardian",
        "minecraft:elder_guardian", "minecraft:drowned"])
    tag("entity_type", MC_DATA, "can_breathe_under_water", sea + [m("hermit_crab")])
    tag("entity_type", MC_DATA, "aquatic", sea)
    tag("entity_type", MC_DATA, "axolotl_hunt_targets", [m("glimmerfin"), m("sunscale_carp"), m("piranha")])
    tag("entity_type", MC_DATA, "axolotl_always_hostiles", [m("reef_barracuda"), m("deep_angler")])
    tag("block", MC_DATA, "mineable/pickaxe", [m("angler_lantern")])

    # worldgen: glowing anemone reefs
    wg = os.path.join(DATA, "worldgen")
    anemone = {"Name": m("glow_anemone"), "Properties": {"waterlogged": "true"}}
    write_json(os.path.join(wg, "configured_feature", "glow_anemone_reef.json"), {"type": "minecraft:random_patch", "config": {
        "tries": 28, "xz_spread": 6, "y_spread": 2,
        "feature": {"feature": {"type": "minecraft:simple_block", "config": {"to_place": {"type": "minecraft:simple_state_provider", "state": anemone}}},
                    "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                        {"type": "minecraft:matching_blocks", "blocks": "minecraft:water"},
                        {"type": "minecraft:would_survive", "state": anemone}]}}]}}})
    write_json(os.path.join(wg, "placed_feature", "glow_anemone_reef.json"), {"feature": m("glow_anemone_reef"), "placement": [
        {"type": "minecraft:rarity_filter", "chance": 3},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
        {"type": "minecraft:biome"}]})

    lang = {
        "itemGroup.wackyupdate.aquatic": "WackyUpdate: Aquatic",
        "entity.wackyupdate.glimmerfin": "Glimmerfin", "entity.wackyupdate.sunscale_carp": "Sunscale Carp",
        "entity.wackyupdate.sea_bunny": "Sea Bunny", "entity.wackyupdate.hermit_crab": "Hermit Crab",
        "entity.wackyupdate.seahorse": "Seahorse", "entity.wackyupdate.drift_jelly": "Drift Jelly",
        "entity.wackyupdate.stingray": "Stingray", "entity.wackyupdate.piranha": "Piranha",
        "entity.wackyupdate.reef_barracuda": "Reef Barracuda", "entity.wackyupdate.deep_angler": "Deep Angler",
        "item.wackyupdate.glimmerfin": "Glimmerfin", "item.wackyupdate.sunscale_carp": "Raw Sunscale Carp",
        "item.wackyupdate.cooked_sunscale_carp": "Cooked Sunscale Carp", "item.wackyupdate.piranha": "Raw Piranha",
        "item.wackyupdate.cooked_piranha": "Cooked Piranha", "item.wackyupdate.barracuda_fillet": "Raw Barracuda Fillet",
        "item.wackyupdate.cooked_barracuda_fillet": "Cooked Barracuda Fillet", "item.wackyupdate.jelly_gel": "Jelly Gel",
        "item.wackyupdate.stingray_barb": "Stingray Barb", "item.wackyupdate.piranha_tooth": "Piranha Tooth",
        "item.wackyupdate.angler_lure": "Angler Lure", "item.wackyupdate.hermit_shell": "Hermit Shell",
        "item.wackyupdate.glimmerfin_bucket": "Bucket of Glimmerfin", "item.wackyupdate.sunscale_carp_bucket": "Bucket of Sunscale Carp",
        "item.wackyupdate.seahorse_bucket": "Bucket of Seahorse", "item.wackyupdate.sea_bunny_bucket": "Bucket of Sea Bunny",
        "item.wackyupdate.piranha_bucket": "Bucket of Piranha",
        "block.wackyupdate.angler_lantern": "Angler Lantern", "block.wackyupdate.jelly_lamp": "Jelly Lamp",
        "block.wackyupdate.glow_anemone": "Glow Anemone",
    }
    for egg in ("glimmerfin", "sunscale_carp", "sea_bunny", "hermit_crab", "seahorse", "drift_jelly", "stingray", "piranha",
                "reef_barracuda", "deep_angler"):
        lang[f"item.wackyupdate.{egg}_spawn_egg"] = lang[f"entity.wackyupdate.{egg}"] + " Spawn Egg"
    return lang


# ============================================================================================ main
def main():
    # ---------------------------------------------------------------- textures
    save(tex_budding_speculum(), "block", "budding_speculum")
    save(tex_crystal_cross(15, 3, 20), "block", "speculum_cluster")
    save(tex_crystal_cross(11, 2, 21), "block", "large_speculum_bud")
    save(tex_crystal_cross(8, 2, 22), "block", "medium_speculum_bud")
    save(tex_crystal_cross(5, 1, 23), "block", "small_speculum_bud")
    save(tex_prism_glass(), "block", "prism_glass")
    save(tex_speculum_block(), "block", "speculum_block")
    save(tex_polished_speculum(), "block", "polished_speculum")
    save(tex_speculum_bricks(), "block", "speculum_bricks")
    save(tex_prism_tiles(), "block", "prism_tiles")
    save(tex_compacted_feet(), "block", "compacted_feet_block")
    save(tex_slick_log(), "block", "slickwood_log")
    save(tex_log_top(hexc("dfcf9f"), hexc("f3e6c0")), "block", "slickwood_log_top")
    save(tex_stripped_log(), "block", "stripped_slickwood_log")
    save(tex_log_top(hexc("efe3c0"), hexc("f7eed4")), "block", "stripped_slickwood_log_top")
    save(tex_planks(), "block", "slickwood_planks")
    save(tex_slick_floor(), "block", "slickwood_floor")
    save(tex_slick_leaves(), "block", "slickwood_leaves")
    save(tex_slick_sapling(), "block", "slickwood_sapling")
    save(tex_lantern_block(), "block", "speculum_lantern")
    save(tex_firefly_jar(), "block", "firefly_jar")
    save(tex_metal(), "block", "skate_rail")
    save(tex_boost_pad(), "block", "boost_pad")
    save(tex_boost_side(), "block", "boost_pad_side")
    save(tex_projector_side(), "block", "prism_projector_side")
    save(tex_projector_front(False), "block", "prism_projector_front")
    save(tex_projector_front(True), "block", "prism_projector_front_on")
    save(tex_projector_back(), "block", "prism_projector_back")

    save(tex_mirror_face(), "block", "speculum_mirror_face")
    save(tex_mirror_frame(), "block", "speculum_mirror_frame")
    save(tex_splitter_glass(), "block", "prism_splitter")
    save(tex_splitter_core(), "block", "prism_splitter_core")
    save(tex_splitter_out(), "block", "prism_splitter_out")
    save(tex_receptor(False), "block", "light_receptor")
    save(tex_receptor(True), "block", "light_receptor_on")
    save(tex_receptor_gem(), "block", "light_receptor_gem")

    save(tex_shard(), "item", "speculum_shard")
    save(tex_lens(), "item", "spectre_lens")
    for kind in ("helmet", "chestplate", "leggings", "boots"):
        save(tex_armor_icon(kind), "item", "speculum_" + kind)
    save(tex_mite_foot(), "item", "mite_foot")
    save(tex_rollerskates(), "item", "rollerskates")
    save(tex_tonic(), "item", "prism_tonic")
    save(tex_dummy_item(), "item", "ragdoll_dummy")
    save(tex_lantern_item(), "item", "speculum_lantern")

    save(ent_gravel_mite(), "entity", "gravel_mite")
    save(ent_prism_snail(), "entity", "prism_snail")
    save(ent_rollypin(), "entity", "rollypin")
    save(ent_glowtoad(False), "entity", "glowtoad")
    save(ent_glowtoad(True), "entity", "glowtoad_glow")
    save(ent_shardling(False), "entity", "shardling")
    save(ent_shardling(True), "entity", "shardling_glow")
    save(ent_skate_ghoul(False), "entity", "skate_ghoul")
    save(ent_skate_ghoul(True), "entity", "skate_ghoul_glow")
    save(ent_ragdoll_dummy(), "entity", "ragdoll_dummy")

    save(armor_layer("speculum_1"), "models", "armor", "speculum_layer_1")
    save(armor_layer("speculum_2"), "models", "armor", "speculum_layer_2")
    save(armor_layer("rollerskate"), "models", "armor", "rollerskate_layer_1")
    save(new(64, 32), "models", "armor", "rollerskate_layer_2")

    save(particle_firefly(), "particle", "firefly")
    for i in range(4):
        save(particle_spark(i), "particle", f"prism_spark_{i}")
    write_json(os.path.join(ASSETS, "particles", "firefly.json"), {"textures": [m("firefly")]})
    write_json(os.path.join(ASSETS, "particles", "prism_spark.json"), {"textures": [m(f"prism_spark_{i}") for i in range(4)]})
    icon().save(os.path.join(ASSETS, "icon.png"))

    # ---------------------------------------------------------------- simple cubes
    for name in ("budding_speculum", "speculum_block", "polished_speculum", "speculum_bricks", "prism_tiles",
                 "compacted_feet_block", "slickwood_planks"):
        cube_all(name)
    bmodel("slickwood_leaves", {"parent": "minecraft:block/cube_all", "textures": {"all": m("block/slickwood_leaves")}})
    bs("slickwood_leaves", {"": {"model": m("block/slickwood_leaves")}})
    item_block("slickwood_leaves")

    # mite gravel looks *exactly* like gravel
    bs("mite_gravel", {"": {"model": "minecraft:block/gravel"}})
    item_block("mite_gravel", "minecraft:block/gravel")

    # crystals
    facing_rot = {"up": {}, "down": {"x": 180}, "north": {"x": 90}, "south": {"x": 90, "y": 180},
                  "east": {"x": 90, "y": 90}, "west": {"x": 90, "y": 270}}
    for name in ("speculum_cluster", "large_speculum_bud", "medium_speculum_bud", "small_speculum_bud"):
        bmodel(name, {"parent": "minecraft:block/cross", "textures": {"cross": m("block/" + name)}})
        bs(name, {f"facing={f}": dict(model=m("block/" + name), **r) for f, r in facing_rot.items()})
        item_generated(name, m("block/" + name))

    # prism glass: tinted full-bright glass
    faces = {d: {"texture": "#glass", "tintindex": 0, "cullface": d} for d in ("down", "up", "north", "south", "west", "east")}
    bmodel("prism_glass", {"parent": "minecraft:block/block", "textures": {"glass": m("block/prism_glass"), "particle": m("block/prism_glass")},
                           "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces}]})
    bs("prism_glass", {"": {"model": m("block/prism_glass")}})
    item_block("prism_glass")

    # logs
    for name in ("slickwood_log", "stripped_slickwood_log"):
        bmodel(name, {"parent": "minecraft:block/cube_column", "textures": {"end": m(f"block/{name}_top"), "side": m("block/" + name)}})
        bs(name, {"axis=y": {"model": m("block/" + name)}, "axis=z": {"model": m("block/" + name), "x": 90},
                  "axis=x": {"model": m("block/" + name), "x": 90, "y": 90}})
        item_block(name)

    # sapling
    bmodel("slickwood_sapling", {"parent": "minecraft:block/cross", "textures": {"cross": m("block/slickwood_sapling")}})
    bs("slickwood_sapling", {"": {"model": m("block/slickwood_sapling")}})
    item_generated("slickwood_sapling", m("block/slickwood_sapling"))

    # slab / stairs / fence
    planks = m("block/slickwood_planks")
    bmodel("slickwood_slab", {"parent": "minecraft:block/slab", "textures": {"bottom": planks, "top": planks, "side": planks}})
    bmodel("slickwood_slab_top", {"parent": "minecraft:block/slab_top", "textures": {"bottom": planks, "top": planks, "side": planks}})
    bs("slickwood_slab", {"type=bottom": {"model": m("block/slickwood_slab")}, "type=top": {"model": m("block/slickwood_slab_top")},
                          "type=double": {"model": m("block/slickwood_planks")}})
    item_block("slickwood_slab")
    for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
        bmodel("slickwood_stairs" + suffix, {"parent": "minecraft:block/" + parent, "textures": {"bottom": planks, "top": planks, "side": planks}})
    stairs = {}
    ys = {"east": 0, "south": 90, "west": 180, "north": 270}
    for facing, base in ys.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                model = m("block/slickwood_stairs" + ("_inner" if shape.startswith("inner") else "_outer" if shape.startswith("outer") else ""))
                y = base
                if half == "bottom" and shape.endswith("left"):
                    y = base - 90
                if half == "top" and shape.endswith("right"):
                    y = base + 90
                v = {"model": model}
                y %= 360
                if y:
                    v["y"] = y
                if half == "top":
                    v["x"] = 180
                if "x" in v or "y" in v:
                    v["uvlock"] = True
                stairs[f"facing={facing},half={half},shape={shape}"] = v
    bs("slickwood_stairs", stairs)
    item_block("slickwood_stairs")
    for suffix, parent in (("_post", "fence_post"), ("_side", "fence_side"), ("_inventory", "fence_inventory")):
        bmodel("slickwood_fence" + suffix, {"parent": "minecraft:block/" + parent, "textures": {"texture": planks}})
    write_json(os.path.join(ASSETS, "blockstates", "slickwood_fence.json"), {"multipart": [
        {"apply": {"model": m("block/slickwood_fence_post")}},
        {"when": {"north": "true"}, "apply": {"model": m("block/slickwood_fence_side"), "uvlock": True}},
        {"when": {"east": "true"}, "apply": {"model": m("block/slickwood_fence_side"), "y": 90, "uvlock": True}},
        {"when": {"south": "true"}, "apply": {"model": m("block/slickwood_fence_side"), "y": 180, "uvlock": True}},
        {"when": {"west": "true"}, "apply": {"model": m("block/slickwood_fence_side"), "y": 270, "uvlock": True}}]})
    item_block("slickwood_fence", m("block/slickwood_fence_inventory"))

    horizontal = {"north": 0, "east": 90, "south": 180, "west": 270}

    # ramp: a 45 degree slope over a stepped body, rising towards north in the base model
    def pf(uv=None):
        f = {"texture": "#planks"}
        if uv:
            f["uv"] = uv
        return f
    ramp_elements = [
        {"from": [0, 7.95, -3.31], "to": [16, 8.05, 19.31], "rotation": {"origin": [8, 8, 8], "axis": "x", "angle": 45},
         "faces": {"up": pf([0, 0, 16, 16]), "down": pf([0, 0, 16, 16])}},
        {"from": [0, 0, 0], "to": [16, 4, 12], "faces": {"down": pf(), "north": pf(), "west": pf(), "east": pf(), "south": pf()}},
        {"from": [0, 4, 0], "to": [16, 8, 8], "faces": {"north": pf(), "west": pf(), "east": pf(), "south": pf()}},
        {"from": [0, 8, 0], "to": [16, 12, 4], "faces": {"north": pf(), "west": pf(), "east": pf(), "south": pf()}},
        {"from": [0, 12, 0], "to": [16, 16, 0.5], "faces": {"north": pf(), "west": pf(), "east": pf()}},
    ]
    bmodel("slickwood_ramp", {"parent": "minecraft:block/block", "textures": {"planks": m("block/slickwood_floor"), "particle": planks},
                              "elements": ramp_elements})
    bs("slickwood_ramp", {f"facing={f}": ({"model": m("block/slickwood_ramp"), "y": y} if y else {"model": m("block/slickwood_ramp")})
                          for f, y in horizontal.items()})
    item_block("slickwood_ramp")

    bmodel("slickwood_floor", {"parent": "minecraft:block/block", "textures": {"floor": m("block/slickwood_floor"), "particle": m("block/slickwood_floor")},
                               "elements": [{"from": [0, 0, 0], "to": [16, 2, 16], "faces": {
                                   "down": {"texture": "#floor", "cullface": "down"}, "up": {"texture": "#floor"},
                                   "north": {"texture": "#floor", "uv": [0, 14, 16, 16], "cullface": "north"},
                                   "south": {"texture": "#floor", "uv": [0, 14, 16, 16], "cullface": "south"},
                                   "west": {"texture": "#floor", "uv": [0, 14, 16, 16], "cullface": "west"},
                                   "east": {"texture": "#floor", "uv": [0, 14, 16, 16], "cullface": "east"}}}]})
    bs("slickwood_floor", {"": {"model": m("block/slickwood_floor")}})
    item_block("slickwood_floor")

    def box(frm, to, tex="#t"):
        return {"from": frm, "to": to, "faces": {d: {"texture": tex} for d in ("down", "up", "north", "south", "west", "east")}}

    bmodel("skate_rail", {"parent": "minecraft:block/block", "textures": {"t": m("block/skate_rail"), "particle": m("block/skate_rail")},
                          "elements": [box([6.5, 4, 0], [9.5, 7, 16]), box([7, 0, 3], [9, 4, 5]), box([7, 0, 11], [9, 4, 13])]})
    bs("skate_rail", {"axis=z": {"model": m("block/skate_rail")}, "axis=x": {"model": m("block/skate_rail"), "y": 90}})
    item_block("skate_rail")

    bmodel("boost_pad", {"parent": "minecraft:block/block", "textures": {"top": m("block/boost_pad"), "side": m("block/boost_pad_side"), "particle": m("block/boost_pad")},
                         "elements": [{"from": [0, 0, 0], "to": [16, 1, 16], "faces": {
                             "down": {"texture": "#side", "cullface": "down"}, "up": {"texture": "#top"},
                             "north": {"texture": "#side", "uv": [0, 15, 16, 16]}, "south": {"texture": "#side", "uv": [0, 15, 16, 16]},
                             "west": {"texture": "#side", "uv": [0, 15, 16, 16]}, "east": {"texture": "#side", "uv": [0, 15, 16, 16]}}}]})
    bs("boost_pad", {f"facing={f}": ({"model": m("block/boost_pad"), "y": y} if y else {"model": m("block/boost_pad")})
                     for f, y in horizontal.items()})
    item_block("boost_pad")

    # lantern
    def lantern_elements(dy):
        lt = "#lantern"
        return [
            {"from": [5, 1 + dy, 5], "to": [11, 8 + dy, 11], "faces": {
                "down": {"texture": lt, "uv": [0, 9, 6, 15]}, "up": {"texture": lt, "uv": [0, 9, 6, 15]},
                **{d: {"texture": lt, "uv": [0, 2, 6, 9]} for d in ("north", "south", "west", "east")}}},
            {"from": [6, 8 + dy, 6], "to": [10, 10 + dy, 10], "faces": {
                "down": {"texture": lt, "uv": [6, 4, 10, 8]}, "up": {"texture": lt, "uv": [6, 4, 10, 8]},
                **{d: {"texture": lt, "uv": [6, 2, 10, 4]} for d in ("north", "south", "west", "east")}}},
            {"from": [7, 10 + dy, 8], "to": [9, 12 + dy + (4 if dy else 0), 8], "rotation": {"angle": 45, "axis": "y", "origin": [8, 8, 8]},
             "faces": {"north": {"texture": lt, "uv": [11, 0, 13, 2]}, "south": {"texture": lt, "uv": [11, 0, 13, 2]}}},
            {"from": [8, 10 + dy, 7], "to": [8, 12 + dy + (4 if dy else 0), 9], "rotation": {"angle": 45, "axis": "y", "origin": [8, 8, 8]},
             "faces": {"west": {"texture": lt, "uv": [11, 0, 13, 2]}, "east": {"texture": lt, "uv": [11, 0, 13, 2]}}},
        ]
    for name, dy in (("speculum_lantern", 0), ("speculum_lantern_hanging", 1)):
        bmodel(name, {"parent": "minecraft:block/block", "ambientocclusion": False,
                      "textures": {"lantern": m("block/speculum_lantern"), "particle": m("block/speculum_lantern")},
                      "elements": lantern_elements(dy)})
    bs("speculum_lantern", {"hanging=false": {"model": m("block/speculum_lantern")}, "hanging=true": {"model": m("block/speculum_lantern_hanging")}})
    item_generated("speculum_lantern")

    # firefly jar
    jt = "#jar"
    bmodel("firefly_jar", {"parent": "minecraft:block/block", "ambientocclusion": False,
                           "textures": {"jar": m("block/firefly_jar"), "particle": m("block/firefly_jar")},
                           "elements": [
                               {"from": [4, 0, 4], "to": [12, 10, 12], "faces": {
                                   "down": {"texture": jt, "uv": [0, 11, 8, 16], "cullface": "down"}, "up": {"texture": jt, "uv": [0, 11, 8, 16]},
                                   **{d: {"texture": jt, "uv": [0, 0, 8, 10]} for d in ("north", "south", "west", "east")}}},
                               {"from": [5, 10, 5], "to": [11, 11, 11], "faces": {
                                   "up": {"texture": jt, "uv": [8, 0, 14, 6]},
                                   **{d: {"texture": jt, "uv": [8, 6, 14, 7]} for d in ("north", "south", "west", "east")}}}]})
    bs("firefly_jar", {"": {"model": m("block/firefly_jar")}})
    item_block("firefly_jar")

    # prism projector (front = up in the base model)
    for name, front in (("prism_projector", "prism_projector_front"), ("prism_projector_on", "prism_projector_front_on")):
        side = m("block/prism_projector_side")
        bmodel(name, {"parent": "minecraft:block/cube", "textures": {
            "particle": side, "up": m("block/" + front), "down": m("block/prism_projector_back"),
            "north": side, "south": side, "east": side, "west": side}})
    proj = {}
    for f, r in facing_rot.items():
        for powered in ("false", "true"):
            proj[f"facing={f},powered={powered}"] = dict(model=m("block/prism_projector" + ("_on" if powered == "true" else "")), **r)
    bs("prism_projector", proj)
    item_block("prism_projector")

    # ---- light network
    six = ("down", "up", "north", "south", "west", "east")
    bmodel("speculum_mirror", {"parent": "minecraft:block/block", "textures": {
        "frame": m("block/speculum_mirror_frame"), "mirror": m("block/speculum_mirror_face"), "particle": m("block/speculum_mirror_face")},
        "elements": [
            {"from": [2, 0, 2], "to": [14, 2, 14], "faces": {d: {"texture": "#frame", "cullface": "down"} if d == "down" else {"texture": "#frame"} for d in six}},
            {"from": [1, 2, 1], "to": [15, 14, 15], "faces": {d: {"texture": "#mirror" if d == "up" else "#frame"} for d in six}},
            {"from": [0, 14, 0], "to": [16, 15, 16], "faces": {d: {"texture": "#mirror" if d == "up" else "#frame"} for d in six}}]})
    bs("speculum_mirror", {f"facing={f}": dict(model=m("block/speculum_mirror"), **r) for f, r in facing_rot.items()})
    item_block("speculum_mirror")

    bmodel("prism_splitter", {"parent": "minecraft:block/block", "textures": {
        "glass": m("block/prism_splitter"), "core": m("block/prism_splitter_core"), "out": m("block/prism_splitter_out"),
        "particle": m("block/prism_splitter")},
        "elements": [
            {"from": [5, 5, 5], "to": [11, 11, 11], "faces": {d: {"texture": "#core"} for d in six}},
            {"from": [6, 11, 6], "to": [10, 15.9, 10], "faces": {d: {"texture": "#out"} for d in six}},
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {d: {"texture": "#glass", "cullface": d} for d in six}}]})
    bs("prism_splitter", {f"facing={f}": dict(model=m("block/prism_splitter"), **r) for f, r in facing_rot.items()})
    item_block("prism_splitter")

    for name, tex in (("light_receptor", "light_receptor"), ("light_receptor_on", "light_receptor_on")):
        bmodel(name, {"parent": "minecraft:block/block", "textures": {"side": m("block/" + tex), "gem": m("block/light_receptor_gem"), "particle": m("block/" + tex)},
                      "elements": [
                          {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {d: {"texture": "#side", "cullface": d} for d in six}},
                          {"from": [-0.01, -0.01, -0.01], "to": [16.01, 16.01, 16.01], "faces": {d: {"texture": "#gem", "tintindex": 0, "cullface": d} for d in six}}]})
    bs("light_receptor", {f"power={p}": {"model": m("block/light_receptor" + ("_on" if p else ""))} for p in range(16)})
    item_block("light_receptor")

    # plain items
    for name in ("speculum_shard", "spectre_lens", "speculum_helmet", "speculum_chestplate", "speculum_leggings",
                 "speculum_boots", "mite_foot", "rollerskates", "prism_tonic", "ragdoll_dummy"):
        item_generated(name)
    imodel("spectre_lens", {"parent": "minecraft:item/handheld", "textures": {"layer0": m("item/spectre_lens")}})
    for egg in ("gravel_mite", "prism_snail", "rollypin", "glowtoad", "shardling", "skate_ghoul"):
        imodel(egg + "_spawn_egg", {"parent": "minecraft:item/template_spawn_egg"})

    # ---------------------------------------------------------------- loot tables
    for name in ("speculum_lantern", "compacted_feet_block", "slickwood_log", "stripped_slickwood_log", "slickwood_planks",
                 "slickwood_sapling", "slickwood_ramp", "slickwood_floor", "speculum_block", "polished_speculum",
                 "speculum_bricks", "prism_tiles", "firefly_jar", "skate_rail", "boost_pad", "slickwood_stairs",
                 "slickwood_fence", "prism_projector", "speculum_mirror", "prism_splitter", "light_receptor"):
        loot_self(name)
    for name in ("large_speculum_bud", "medium_speculum_bud", "small_speculum_bud"):
        loot_silk_only(name)
    write_json(os.path.join(DATA, "loot_table", "blocks", "speculum_cluster.json"), {
        "type": "minecraft:block", "random_sequence": m("blocks/speculum_cluster"),
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": m("speculum_cluster"), "conditions": [SILK]},
            {"type": "minecraft:item", "name": m("speculum_shard"),
             "conditions": [{"condition": "minecraft:match_tool", "predicate": {"items": "#minecraft:cluster_max_harvestables"}}],
             "functions": [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"}]}]}]}]})
    write_json(os.path.join(DATA, "loot_table", "blocks", "prism_glass.json"), {
        "type": "minecraft:block", "random_sequence": m("blocks/prism_glass"),
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": m("prism_glass"), "functions": [
            {"function": "minecraft:copy_state", "block": m("prism_glass"), "properties": ["color"]}]}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    write_json(os.path.join(DATA, "loot_table", "blocks", "mite_gravel.json"), {
        "type": "minecraft:block", "random_sequence": m("blocks/mite_gravel"),
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": m("mite_gravel"), "conditions": [SILK]},
            {"type": "minecraft:item", "name": "minecraft:gravel"}]}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    write_json(os.path.join(DATA, "loot_table", "blocks", "slickwood_slab.json"), {
        "type": "minecraft:block", "random_sequence": m("blocks/slickwood_slab"),
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": m("slickwood_slab"), "functions": [
            {"function": "minecraft:set_count", "count": 2, "add": False,
             "conditions": [{"condition": "minecraft:block_state_property", "block": m("slickwood_slab"), "properties": {"type": "double"}}]},
            {"function": "minecraft:explosion_decay"}]}]}]})
    shears_or_silk = {"condition": "minecraft:any_of", "terms": [
        {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}}, SILK]}
    write_json(os.path.join(DATA, "loot_table", "blocks", "slickwood_leaves.json"), {
        "type": "minecraft:block", "random_sequence": m("blocks/slickwood_leaves"),
        "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": m("slickwood_leaves"), "conditions": [shears_or_silk]},
                {"type": "minecraft:item", "name": m("slickwood_sapling"), "conditions": [
                    {"condition": "minecraft:survives_explosion"},
                    {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.05, 0.0625, 0.083333336, 0.1]}]}]}]},
            {"rolls": 1, "conditions": [{"condition": "minecraft:inverted", "term": shears_or_silk}],
             "entries": [{"type": "minecraft:item", "name": "minecraft:stick",
                          "conditions": [{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1]}],
                          "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}},
                                        {"function": "minecraft:explosion_decay"}]}]}]})

    entity_loot("gravel_mite", [drop_pool(m("mite_foot"), 1, 2)])
    entity_loot("prism_snail", [drop_pool("minecraft:slime_ball", 0, 1)])
    entity_loot("rollypin", [drop_pool("minecraft:leather", 0, 2)])
    entity_loot("glowtoad", [drop_pool("minecraft:glowstone_dust", 0, 2), drop_pool("minecraft:slime_ball", 0, 1, looting=False)])
    entity_loot("skate_ghoul", [drop_pool("minecraft:rotten_flesh", 0, 2), drop_pool(m("mite_foot"), 0, 1)])
    entity_loot("shardling", [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": m("speculum_shard")}],
         "conditions": [{"condition": "minecraft:killed_by_player"},
                        {"condition": "minecraft:random_chance_with_enchanted_bonus", "enchantment": "minecraft:looting",
                         "unenchanted_chance": 0.2, "enchanted_chance": {"type": "minecraft:linear", "base": 0.25, "per_level_above_first": 0.05}}]},
        drop_pool(m("prism_glass"), 0, 2, extra_functions=[
            {"function": "minecraft:set_components", "components": {"minecraft:block_state": {"color": "light_blue"}}}])])

    # ---------------------------------------------------------------- recipes
    S = m("speculum_shard")
    shaped("speculum_helmet", ["SSS", "S S"], {"S": S}, m("speculum_helmet"))
    shaped("speculum_chestplate", ["S S", "SSS", "SSS"], {"S": S}, m("speculum_chestplate"))
    shaped("speculum_leggings", ["SSS", "S S", "S S"], {"S": S}, m("speculum_leggings"))
    shaped("speculum_boots", ["S S", "S S"], {"S": S}, m("speculum_boots"))
    shaped("speculum_lantern", ["NNN", "NSN", "NGN"], {"N": "minecraft:iron_nugget", "S": S, "G": "minecraft:glowstone_dust"}, m("speculum_lantern"))
    shaped("spectre_lens", [" S ", "SPS", " C "], {"S": S, "P": "minecraft:glass_pane", "C": "minecraft:copper_ingot"}, m("spectre_lens"))
    shaped("prism_glass", ["GGG", "GSG", "GGG"], {"G": "minecraft:glass", "S": S}, m("prism_glass"), 8,
           {"minecraft:block_state": {"color": "white"}})
    shaped("prism_tiles", ["PP", "PP"], {"P": m("prism_glass")}, m("prism_tiles"), 4)
    shaped("speculum_block", ["SS", "SS"], {"S": S}, m("speculum_block"))
    shaped("polished_speculum", ["BB", "BB"], {"B": m("speculum_block")}, m("polished_speculum"), 4)
    shaped("speculum_bricks", ["BB", "BB"], {"B": m("polished_speculum")}, m("speculum_bricks"), 4)
    shaped("prism_projector", ["ISI", "IRI", "III"], {"I": "minecraft:iron_ingot", "S": S, "R": "minecraft:redstone_block"}, m("prism_projector"))
    shaped("speculum_mirror", [" I ", "SPS", " I "], {"I": "minecraft:iron_ingot", "S": S, "P": "minecraft:glass_pane"}, m("speculum_mirror"), 2)
    shaped("prism_splitter", [" P ", "PSP", " P "], {"P": m("prism_glass"), "S": S}, m("prism_splitter"))
    shaped("light_receptor", [" G ", "RSR", "III"], {"G": "minecraft:glass", "R": "minecraft:redstone", "S": S, "I": "minecraft:iron_ingot"}, m("light_receptor"))
    shaped("rollerskates", [" B ", "IFI"], {"B": "minecraft:leather_boots", "I": "minecraft:iron_ingot", "F": m("compacted_feet_block")}, m("rollerskates"))
    shapeless("mite_feet_from_block", [m("compacted_feet_block")], m("mite_foot"), 10)
    shapeless("slickwood_planks", ["#" + m("slickwood_logs")], m("slickwood_planks"), 4)
    shaped("slickwood_ramp", ["  P", " PP", "PPP"], {"P": m("slickwood_planks")}, m("slickwood_ramp"), 6)
    shapeless("slickwood_floor", [m("slickwood_slab"), m("slickwood_slab"), "minecraft:honeycomb"], m("slickwood_floor"), 6)
    shaped("slickwood_slab", ["PPP"], {"P": m("slickwood_planks")}, m("slickwood_slab"), 6)
    shaped("slickwood_stairs", ["P  ", "PP ", "PPP"], {"P": m("slickwood_planks")}, m("slickwood_stairs"), 4)
    shaped("slickwood_fence", ["PSP", "PSP"], {"P": m("slickwood_planks"), "S": "minecraft:stick"}, m("slickwood_fence"), 3)
    shaped("skate_rail", ["III", "N N"], {"I": "minecraft:iron_ingot", "N": "minecraft:iron_nugget"}, m("skate_rail"), 6)
    shaped("boost_pad", ["SRS", "III"], {"S": "minecraft:sugar", "R": "minecraft:redstone", "I": "minecraft:iron_ingot"}, m("boost_pad"), 2)
    shapeless("firefly_jar", ["minecraft:glass_bottle", "minecraft:glow_berries", "minecraft:glowstone_dust"], m("firefly_jar"))
    shapeless("prism_tonic", ["minecraft:glass_bottle", S, "minecraft:glow_berries", "minecraft:sugar"], m("prism_tonic"))
    shaped("ragdoll_dummy", [" W ", "SHS", " S "], {"W": "#minecraft:wool", "S": "minecraft:stick", "H": "minecraft:hay_block"}, m("ragdoll_dummy"))

    # ---------------------------------------------------------------- tags
    tag("block", MC_DATA, "mineable/pickaxe", [m(n) for n in (
        "budding_speculum", "speculum_cluster", "large_speculum_bud", "medium_speculum_bud", "small_speculum_bud",
        "speculum_lantern", "speculum_block", "polished_speculum", "speculum_bricks", "prism_tiles", "skate_rail",
        "boost_pad", "prism_projector", "speculum_mirror", "prism_splitter", "light_receptor")])
    tag("block", MC_DATA, "mineable/axe", [m(n) for n in (
        "slickwood_log", "stripped_slickwood_log", "slickwood_planks", "slickwood_ramp", "slickwood_floor",
        "slickwood_slab", "slickwood_stairs", "slickwood_fence")])
    tag("block", MC_DATA, "mineable/shovel", [m("mite_gravel"), m("compacted_feet_block")])
    tag("block", MC_DATA, "mineable/hoe", [m("slickwood_leaves")])
    tag("block", MC_DATA, "logs", [m("slickwood_log"), m("stripped_slickwood_log")])
    tag("block", MC_DATA, "logs_that_burn", [m("slickwood_log"), m("stripped_slickwood_log")])
    tag("block", MC_DATA, "planks", [m("slickwood_planks")])
    tag("block", MC_DATA, "leaves", [m("slickwood_leaves")])
    tag("block", MC_DATA, "saplings", [m("slickwood_sapling")])
    tag("block", MC_DATA, "wooden_fences", [m("slickwood_fence")])
    tag("block", MC_DATA, "wooden_slabs", [m("slickwood_slab")])
    tag("block", MC_DATA, "wooden_stairs", [m("slickwood_stairs")])
    tag("block", MC_DATA, "crystal_sound_blocks", [m("speculum_block"), m("polished_speculum"), m("speculum_bricks")])
    tag("block", MC_DATA, "impermeable", [m("prism_glass")])
    tag("block", os.path.join(ROOT, "data", NS), "skateable", [m(n) for n in (
        "slickwood_planks", "slickwood_ramp", "slickwood_floor", "slickwood_slab", "slickwood_stairs", "skate_rail",
        "boost_pad", "stripped_slickwood_log", "polished_speculum", "prism_tiles")] + [
        "minecraft:smooth_stone", "minecraft:smooth_stone_slab", "minecraft:polished_andesite", "minecraft:polished_diorite",
        "minecraft:polished_granite", "minecraft:smooth_quartz", "minecraft:packed_ice", "minecraft:blue_ice"])
    tag("block", os.path.join(ROOT, "data", NS), "rainbow_lenses", [m(n) for n in (
        "speculum_cluster", "large_speculum_bud", "medium_speculum_bud", "small_speculum_bud", "budding_speculum",
        "speculum_block", "polished_speculum", "speculum_bricks", "prism_tiles")] + ["minecraft:amethyst_cluster", "minecraft:amethyst_block"])
    tag("block", os.path.join(ROOT, "data", NS), "slickwood_logs", [m("slickwood_log"), m("stripped_slickwood_log")])
    tag("item", os.path.join(ROOT, "data", NS), "slickwood_logs", [m("slickwood_log"), m("stripped_slickwood_log")])
    tag("item", MC_DATA, "logs", [m("slickwood_log"), m("stripped_slickwood_log")])
    tag("item", MC_DATA, "logs_that_burn", [m("slickwood_log"), m("stripped_slickwood_log")])
    tag("item", MC_DATA, "planks", [m("slickwood_planks")])
    tag("item", MC_DATA, "leaves", [m("slickwood_leaves")])
    tag("item", MC_DATA, "saplings", [m("slickwood_sapling")])
    tag("item", MC_DATA, "wooden_fences", [m("slickwood_fence")])
    tag("item", MC_DATA, "wooden_slabs", [m("slickwood_slab")])
    tag("item", MC_DATA, "wooden_stairs", [m("slickwood_stairs")])
    tag("item", MC_DATA, "trimmable_armor", [m(f"speculum_{p}") for p in ("helmet", "chestplate", "leggings", "boots")])
    tag("item", MC_DATA, "head_armor", [m("speculum_helmet")])
    tag("item", MC_DATA, "chest_armor", [m("speculum_chestplate")])
    tag("item", MC_DATA, "leg_armor", [m("speculum_leggings")])
    tag("item", MC_DATA, "foot_armor", [m("speculum_boots"), m("rollerskates")])
    tag("item", MC_DATA, "enchantable/head_armor", [m("speculum_helmet")])
    tag("item", MC_DATA, "enchantable/chest_armor", [m("speculum_chestplate")])
    tag("item", MC_DATA, "enchantable/leg_armor", [m("speculum_leggings")])
    tag("item", MC_DATA, "enchantable/foot_armor", [m("speculum_boots"), m("rollerskates")])
    tag("item", MC_DATA, "enchantable/armor", [m(f"speculum_{p}") for p in ("helmet", "chestplate", "leggings", "boots")] + [m("rollerskates")])
    tag("item", MC_DATA, "enchantable/durability", [m(f"speculum_{p}") for p in ("helmet", "chestplate", "leggings", "boots")] + [m("rollerskates"), m("spectre_lens")])
    tag("entity_type", os.path.join(ROOT, "data", NS), "ragdolls", [
        "minecraft:zombie", "minecraft:husk", "minecraft:drowned", "minecraft:skeleton", "minecraft:stray",
        "minecraft:wither_skeleton", "minecraft:bogged", m("skate_ghoul")])
    tag("entity_type", MC_DATA, "undead", [m("skate_ghoul")])
    tag("entity_type", MC_DATA, "arthropod", [m("gravel_mite")])

    # ---------------------------------------------------------------- worldgen
    wg = os.path.join(DATA, "worldgen")
    write_json(os.path.join(wg, "configured_feature", "speculum_pocket.json"), {"type": m("speculum_pocket"), "config": {}})
    write_json(os.path.join(wg, "placed_feature", "speculum_pocket.json"), {"feature": m("speculum_pocket"), "placement": [
        {"type": "minecraft:rarity_filter", "chance": 12},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": -56}, "max_inclusive": {"absolute": 6}}},
        {"type": "minecraft:biome"}]})
    write_json(os.path.join(wg, "configured_feature", "mite_gravel.json"), {"type": "minecraft:ore", "config": {
        "size": 6, "discard_chance_on_air_exposure": 0.0,
        "targets": [{"target": {"predicate_type": "minecraft:block_match", "block": "minecraft:gravel"},
                     "state": {"Name": m("mite_gravel")}}]}})
    write_json(os.path.join(wg, "placed_feature", "mite_gravel.json"), {"feature": m("mite_gravel"), "placement": [
        {"type": "minecraft:count", "count": 10},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": -64}, "max_inclusive": {"absolute": 128}}},
        {"type": "minecraft:biome"}]})
    write_json(os.path.join(wg, "configured_feature", "slickwood_tree.json"), {"type": "minecraft:tree", "config": {
        "ignore_vines": True, "force_dirt": False,
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
        "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:dirt"}},
        "trunk_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": m("slickwood_log"), "Properties": {"axis": "y"}}},
        "foliage_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": m("slickwood_leaves"),
                             "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}},
        "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 6, "height_rand_a": 2, "height_rand_b": 1},
        "foliage_placer": {"type": "minecraft:blob_foliage_placer", "radius": 2, "offset": 0, "height": 3},
        "decorators": []}})
    write_json(os.path.join(wg, "placed_feature", "slickwood_tree.json"), {"feature": m("slickwood_tree"), "placement": [
        {"type": "minecraft:rarity_filter", "chance": 7},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
        {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
        {"type": "minecraft:biome"},
        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
                                                                   "state": {"Name": m("slickwood_sapling"), "Properties": {"stage": "0"}}}}]})

    # ---------------------------------------------------------------- lang
    lang = {
        "itemGroup.wackyupdate": "WackyUpdate",
        "block.wackyupdate.budding_speculum": "Budding Speculum",
        "block.wackyupdate.speculum_cluster": "Speculum Cluster",
        "block.wackyupdate.large_speculum_bud": "Large Speculum Bud",
        "block.wackyupdate.medium_speculum_bud": "Medium Speculum Bud",
        "block.wackyupdate.small_speculum_bud": "Small Speculum Bud",
        "block.wackyupdate.prism_glass": "Prism Glass",
        "block.wackyupdate.speculum_lantern": "Speculum Lantern",
        "block.wackyupdate.mite_gravel": "Gravel",
        "block.wackyupdate.compacted_feet_block": "Compacted Feet Block",
        "block.wackyupdate.slickwood_log": "Slickwood Log",
        "block.wackyupdate.stripped_slickwood_log": "Stripped Slickwood Log",
        "block.wackyupdate.slickwood_planks": "Slickwood Planks",
        "block.wackyupdate.slickwood_leaves": "Slickwood Leaves",
        "block.wackyupdate.slickwood_sapling": "Slickwood Sapling",
        "block.wackyupdate.slickwood_ramp": "Slickwood Ramp",
        "block.wackyupdate.slickwood_floor": "Slickwood Skate Floor",
        "block.wackyupdate.slickwood_slab": "Slickwood Slab",
        "block.wackyupdate.slickwood_stairs": "Slickwood Stairs",
        "block.wackyupdate.slickwood_fence": "Slickwood Fence",
        "block.wackyupdate.speculum_block": "Block of Speculum",
        "block.wackyupdate.polished_speculum": "Polished Speculum",
        "block.wackyupdate.speculum_bricks": "Speculum Bricks",
        "block.wackyupdate.prism_tiles": "Prism Tiles",
        "block.wackyupdate.firefly_jar": "Firefly Jar",
        "block.wackyupdate.skate_rail": "Grind Rail",
        "block.wackyupdate.boost_pad": "Boost Pad",
        "block.wackyupdate.prism_projector": "Prism Projector",
        "item.wackyupdate.speculum_shard": "Speculum Shard",
        "item.wackyupdate.spectre_lens": "Spectre Lens",
        "item.wackyupdate.spectre_lens.tooltip": "Use: Spectre Sight - reveals creatures nearby",
        "item.wackyupdate.spectre_lens.tooltip2": "Hold with a shovel in the other hand to spot gravel mites",
        "item.wackyupdate.speculum_helmet": "Speculum Helmet",
        "item.wackyupdate.speculum_chestplate": "Speculum Chestplate",
        "item.wackyupdate.speculum_leggings": "Speculum Leggings",
        "item.wackyupdate.speculum_boots": "Speculum Boots",
        "item.wackyupdate.mite_foot": "Mite Foot",
        "item.wackyupdate.mite_foot.tooltip": "Sneak + use with 10 to squish them into a block",
        "item.wackyupdate.rollerskates": "Rollerskates",
        "item.wackyupdate.rollerskates.tooltip": "Zoom on slickwood, ramps, rails & smooth stone",
        "item.wackyupdate.rollerskates.tooltip2": "Tap sneak mid-air off a skate surface for tricks",
        "item.wackyupdate.prism_tonic": "Prism Tonic",
        "item.wackyupdate.prism_tonic.tooltip": "Prismatic (3:00)",
        "item.wackyupdate.ragdoll_dummy": "Ragdoll Dummy",
        "item.wackyupdate.ragdoll_dummy.tooltip": "Punch it! Sneak-punch to pick it back up",
        "item.wackyupdate.gravel_mite_spawn_egg": "Gravel Mite Spawn Egg",
        "item.wackyupdate.prism_snail_spawn_egg": "Prism Snail Spawn Egg",
        "item.wackyupdate.rollypin_spawn_egg": "Rollypin Spawn Egg",
        "item.wackyupdate.glowtoad_spawn_egg": "Glowtoad Spawn Egg",
        "item.wackyupdate.shardling_spawn_egg": "Shardling Spawn Egg",
        "item.wackyupdate.skate_ghoul_spawn_egg": "Skate Ghoul Spawn Egg",
        "entity.wackyupdate.gravel_mite": "Gravel Mite",
        "entity.wackyupdate.prism_snail": "Prism Snail",
        "entity.wackyupdate.rollypin": "Rollypin",
        "entity.wackyupdate.glowtoad": "Glowtoad",
        "entity.wackyupdate.shardling": "Shardling",
        "entity.wackyupdate.skate_ghoul": "Skate Ghoul",
        "entity.wackyupdate.ragdoll": "Ragdoll",
        "effect.wackyupdate.prismatic": "Prismatic",
        "message.wackyupdate.projector.mode": "Lightshow: %s",
        "message.wackyupdate.projector.phase": "Beat offset: %s/4",
        "lightshow.wackyupdate.sweep": "Sweep",
        "lightshow.wackyupdate.spin": "Spin",
        "lightshow.wackyupdate.strobe": "Strobe",
        "lightshow.wackyupdate.fan": "Fan",
        "lightshow.wackyupdate.disco": "Disco",
        "lightshow.wackyupdate.focus": "Focus (laser)",
        "lightshow.wackyupdate.music": "Music (note blocks & jukeboxes)",
        "block.wackyupdate.speculum_mirror": "Speculum Mirror",
        "block.wackyupdate.prism_splitter": "Prism Splitter",
        "block.wackyupdate.light_receptor": "Light Receptor",
        "message.wackyupdate.receptor.filter": "Receptor reacts to: %s",
        "message.wackyupdate.receptor.any": "any colour",
        "message.wackyupdate.trick": "Trick! +%2$s XP",
        "message.wackyupdate.trick_combo": "%sx Trick Combo! +%s XP",
        "message.wackyupdate.need_feet": "Need %s more feet",
        "message.wackyupdate.dummy_hit": "Dummy took %s damage",
        "message.wackyupdate.glimmer_night": "It's a Glimmer Night... the fireflies are out.",
    }
    lang.update(aquatic_assets())
    write_json(os.path.join(ASSETS, "lang", "en_us.json"), lang)
    print("assets generated")


if __name__ == "__main__":
    main()
