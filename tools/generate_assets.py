"""Generate the small pixel-art set used by Nightmare Core.

The four supplied source images are copied unchanged. The remaining item icons
are hand-built 16x16 pixel art so the new items match the pack's dark palette.
Run from the project root:

    python tools/generate_assets.py
"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "design" / "source"
ITEM_DIR = ROOT / "src" / "main" / "resources" / "assets" / "nightmare_core" / "textures" / "item"
SLOT_DIR = ROOT / "src" / "main" / "resources" / "assets" / "nightmare_core" / "textures" / "slot"
MODEL_DIR = ROOT / "src" / "main" / "resources" / "assets" / "nightmare_core" / "models" / "item"

ITEM_DIR.mkdir(parents=True, exist_ok=True)
SLOT_DIR.mkdir(parents=True, exist_ok=True)
MODEL_DIR.mkdir(parents=True, exist_ok=True)

OUTLINE = (20, 13, 28, 255)


def canvas():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def finish(im, outline=OUTLINE):
    """Add a one-pixel dark outline around every opaque area."""
    alpha = im.getchannel("A")
    grown = alpha.filter(ImageFilter.MaxFilter(3))
    outline_layer = Image.new("RGBA", im.size, outline)
    outline_layer.putalpha(grown)
    return Image.alpha_composite(outline_layer, im)


def shade_inside(im, color, predicate):
    """Overlay a translucent color only inside existing pixels."""
    alpha = im.getchannel("A")
    overlay = Image.new("RGBA", im.size, color)
    mask = Image.new("L", im.size, 0)
    pixels = mask.load()
    src = alpha.load()
    for y in range(16):
        for x in range(16):
            if src[x, y] and predicate(x, y):
                pixels[x, y] = 255
    overlay.putalpha(Image.composite(overlay.getchannel("A"), Image.new("L", im.size, 0), mask))
    return Image.alpha_composite(im, overlay)


def ember_heart():
    im = canvas()
    d = ImageDraw.Draw(im)
    # A small flame licking out of the heart.
    d.polygon([(6, 2), (8, 0), (9, 3), (11, 1), (10, 5)], fill=(255, 112, 54, 255))
    d.polygon([(7, 2), (8, 4), (6, 5)], fill=(255, 209, 102, 255))
    d.ellipse([2, 3, 7, 8], fill=(196, 48, 76, 255))
    d.ellipse([8, 3, 13, 8], fill=(196, 48, 76, 255))
    d.polygon([(2, 6), (13, 6), (8, 13)], fill=(151, 31, 68, 255))
    # Lower shading and hot highlights.
    for y in range(7, 14):
        d.line([(max(2, 8 - (13 - y)), y), (min(13, 8 + (13 - y)), y)],
               fill=(116, 26, 67, 255))
    d.point([(4, 4), (5, 3), (10, 3), (11, 4)], fill=(255, 185, 92, 255))
    d.point([(7, 7), (8, 7), (8, 8)], fill=(255, 226, 150, 255))
    d.line([(8, 9), (7, 11)], fill=(58, 20, 48, 255))
    return finish(im)


def dreamrend_fang():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(4, 2), (7, 2), (9, 4), (11, 7), (12, 11), (11, 13),
               (9, 14), (8, 12), (9, 10), (8, 7), (6, 5), (3, 4)],
              fill=(224, 214, 194, 255))
    d.polygon([(5, 3), (8, 3), (9, 5), (10, 8), (10, 11), (9, 12),
               (9, 9), (8, 6), (6, 4)], fill=(246, 239, 220, 255))
    # Inner curve and a magical fracture.
    d.line([(8, 5), (7, 8), (8, 10), (7, 12)], fill=(91, 46, 78, 255))
    d.line([(10, 4), (9, 6), (11, 7)], fill=(143, 59, 103, 255))
    d.point([(4, 4), (5, 5), (6, 5)], fill=(120, 94, 82, 255))
    d.point([(10, 12), (9, 12)], fill=(38, 24, 40, 255))
    return finish(im)


def fleet_feather():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(11, 1), (13, 2), (12, 4), (9, 7), (6, 10), (4, 13),
               (2, 14), (2, 12), (5, 8), (8, 4), (9, 2)],
              fill=(104, 205, 220, 255))
    d.polygon([(10, 2), (12, 3), (11, 4), (9, 6), (8, 5)],
              fill=(181, 247, 245, 255))
    d.line([(11, 3), (4, 13)], fill=(230, 255, 249, 255))
    # Barbs.
    for x, y in [(9, 5), (8, 6), (7, 7), (6, 8), (5, 10), (4, 11)]:
        d.point([(x + 1, y), (x, y + 1)], fill=(59, 125, 184, 255))
    d.line([(12, 2), (13, 1)], fill=(183, 156, 255, 255))
    d.point([(13, 4), (14, 5)], fill=(227, 219, 255, 255))
    return finish(im)


def vigil_carapace():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(2, 3), (13, 3), (13, 8), (8, 14), (2, 8)],
              fill=(103, 116, 132, 255))
    d.polygon([(3, 4), (12, 4), (12, 8), (8, 12), (3, 8)],
              fill=(131, 146, 160, 255))
    d.line([(3, 6), (12, 6)], fill=(56, 64, 78, 255))
    d.line([(4, 9), (12, 9)], fill=(56, 64, 78, 255))
    d.polygon([(7, 6), (9, 7), (8, 10), (6, 8)], fill=(170, 50, 66, 255))
    d.point([(8, 7)], fill=(255, 206, 144, 255))
    d.point([(4, 5), (11, 5), (5, 10), (11, 10)], fill=(194, 205, 214, 255))
    return finish(im)


def stonebound_pendant():
    im = canvas()
    d = ImageDraw.Draw(im)
    # Chain.
    d.arc([3, 0, 12, 8], 200, 340, fill=(103, 95, 121, 255), width=1)
    d.point([(2, 5), (13, 5)], fill=(126, 115, 150, 255))
    # Stone body.
    d.polygon([(8, 5), (11, 8), (10, 12), (8, 14), (6, 12), (5, 8)],
              fill=(44, 42, 58, 255))
    d.polygon([(8, 7), (10, 9), (8, 12), (6, 9)], fill=(77, 215, 186, 255))
    d.point([(7, 8), (8, 8)], fill=(202, 255, 235, 255))
    d.line([(8, 12), (8, 13)], fill=(155, 124, 255, 255))
    return finish(im)


def echo_bell():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.arc([6, 0, 10, 4], 180, 360, fill=(166, 142, 90, 255), width=1)
    d.polygon([(5, 4), (11, 4), (12, 8), (13, 10), (3, 10), (4, 8)],
              fill=(190, 146, 63, 255))
    d.line([(4, 7), (12, 7)], fill=(120, 83, 38, 255))
    d.line([(4, 10), (12, 10)], fill=(90, 61, 31, 255))
    d.ellipse([7, 10, 9, 13], fill=(87, 58, 30, 255))
    d.point([(6, 5), (7, 5), (8, 8)], fill=(255, 224, 147, 255))
    d.arc([0, 4, 5, 11], 270, 90, fill=(106, 220, 224, 255), width=1)
    d.arc([11, 4, 16, 11], 90, 270, fill=(106, 220, 224, 255), width=1)
    d.point([(1, 3), (14, 12)], fill=(206, 255, 252, 255))
    return finish(im)


def nightmare_prism():
    im = canvas()
    d = ImageDraw.Draw(im)
    # Main crystal.
    d.polygon([(8, 1), (14, 7), (8, 14), (2, 7)], fill=(184, 58, 104, 255))
    d.polygon([(8, 2), (8, 13), (3, 7)], fill=(217, 79, 126, 255))
    d.polygon([(8, 2), (13, 7), (8, 13)], fill=(117, 205, 237, 255))
    d.polygon([(8, 4), (8, 11), (5, 7)], fill=(250, 228, 255, 255))
    d.line([(8, 1), (8, 14)], fill=(246, 241, 255, 255))
    d.line([(2, 7), (14, 7)], fill=(66, 40, 103, 255))
    # Floating shards.
    d.polygon([(2, 2), (3, 0), (4, 2), (3, 4)], fill=(240, 147, 190, 255))
    d.polygon([(13, 12), (15, 10), (15, 14)], fill=(125, 224, 240, 255))
    d.point([(3, 12), (12, 3)], fill=(255, 255, 255, 255))
    return finish(im)


def dream_anchor():
    im = canvas()
    d = ImageDraw.Draw(im)
    steel = (134, 146, 170, 255)
    darksteel = (71, 78, 101, 255)
    # Ring, shaft, crossbar, curved arms.
    d.ellipse([6, 0, 10, 4], outline=steel, width=1)
    d.rectangle([7, 3, 9, 11], fill=steel)
    d.rectangle([3, 6, 13, 7], fill=steel)
    d.line([(7, 3), (8, 5), (9, 3)], fill=darksteel)
    d.line([(4, 8), (4, 11), (7, 13), (9, 13), (12, 11), (12, 8)],
           fill=steel, width=1)
    d.line([(3, 8), (3, 10), (4, 11)], fill=darksteel)
    d.line([(13, 8), (13, 10), (12, 11)], fill=darksteel)
    d.polygon([(8, 8), (10, 10), (8, 12), (6, 10)], fill=(161, 111, 255, 255))
    d.point([(8, 9)], fill=(224, 211, 255, 255))
    d.point([(2, 5), (14, 5)], fill=(101, 221, 232, 255))
    return finish(im)


def dreamhunter_eye():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(2, 8), (5, 4), (8, 3), (11, 4), (14, 8),
               (11, 12), (8, 13), (5, 12)], fill=(126, 73, 158, 255))
    d.polygon([(3, 8), (6, 5), (8, 4), (10, 5), (13, 8),
               (10, 11), (8, 12), (6, 11)], fill=(236, 225, 246, 255))
    d.ellipse([5, 5, 11, 11], fill=(78, 206, 214, 255))
    d.ellipse([6, 6, 10, 10], fill=(28, 138, 170, 255))
    d.ellipse([7, 4, 9, 12], fill=(29, 18, 45, 255))
    d.point([(7, 6), (8, 6), (9, 7)], fill=(228, 255, 252, 255))
    d.point([(4, 3), (12, 3), (4, 13), (12, 13)], fill=(242, 184, 91, 255))
    d.line([(3, 9), (6, 12)], fill=(177, 126, 255, 255))
    return finish(im)


def ember_blade():
    im = canvas()
    d = ImageDraw.Draw(im)
    # Blade, hilt, pommel.
    d.polygon([(12, 1), (14, 2), (7, 10), (5, 8)], fill=(238, 139, 70, 255))
    d.polygon([(12, 2), (13, 3), (6, 9), (5, 8)], fill=(255, 222, 160, 255))
    d.line([(5, 9), (7, 11)], fill=(91, 53, 54, 255))
    d.line([(4, 8), (8, 12)], fill=(196, 158, 91, 255))
    d.point([(3, 12), (4, 11), (10, 4), (11, 3)], fill=(255, 112, 54, 255))
    d.point([(9, 5), (8, 6)], fill=(255, 246, 210, 255))
    d.point([(2, 13), (1, 14)], fill=(156, 92, 255, 255))
    return finish(im)


def blessing_bag():
    im = canvas()
    d = ImageDraw.Draw(im)
    # Drawstring pouch.
    d.polygon([(3, 6), (5, 4), (11, 4), (13, 6), (12, 12), (10, 14),
               (6, 14), (4, 12)], fill=(119, 55, 121, 255))
    d.polygon([(5, 6), (7, 5), (9, 5), (11, 6), (10, 11), (8, 12), (6, 11)],
              fill=(176, 88, 137, 255))
    d.line([(5, 5), (11, 5)], fill=(222, 179, 91, 255))
    d.line([(4, 7), (12, 7)], fill=(115, 78, 43, 255))
    d.line([(6, 5), (5, 2)], fill=(220, 172, 84, 255))
    d.line([(10, 5), (11, 2)], fill=(220, 172, 84, 255))
    d.point([(8, 5), (8, 8), (8, 10)], fill=(255, 224, 144, 255))
    d.point([(3, 9), (13, 11), (4, 2), (12, 2)], fill=(188, 157, 255, 255))
    return finish(im)


def ashen_cinder():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(8, 1), (12, 6), (11, 12), (6, 15), (4, 9), (6, 4)], fill=(92, 44, 38, 255))
    d.polygon([(8, 3), (10, 7), (9, 12), (6, 13), (6, 9), (7, 5)], fill=(238, 117, 48, 255))
    d.line([(8, 5), (8, 10)], fill=(255, 224, 150, 255))
    d.point([(5, 3), (11, 4), (12, 14), (4, 13)], fill=(187, 78, 53, 255))
    return finish(im)


def tidal_tear():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(8, 1), (11, 7), (10, 12), (8, 14), (5, 11), (5, 7)], fill=(44, 108, 155, 255))
    d.polygon([(8, 4), (9, 8), (8, 11), (7, 8)], fill=(120, 206, 232, 255))
    d.point([(6, 6), (10, 10)], fill=(218, 250, 255, 255))
    return finish(im)


def rift_shard():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(8, 1), (13, 6), (10, 13), (5, 14), (3, 8)], fill=(72, 40, 111, 255))
    d.polygon([(8, 3), (11, 7), (8, 11), (5, 7)], fill=(168, 105, 224, 255))
    d.line([(8, 4), (8, 10)], fill=(244, 224, 255, 255))
    d.point([(4, 4), (12, 12), (4, 12)], fill=(119, 73, 179, 255))
    return finish(im)


def vigil_eye():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.ellipse([2, 4, 14, 12], fill=(219, 194, 92, 255))
    d.ellipse([4, 5, 12, 11], fill=(250, 244, 217, 255))
    d.ellipse([6, 6, 10, 10], fill=(60, 44, 89, 255))
    d.ellipse([7, 6, 9, 8], fill=(255, 255, 255, 255))
    d.point([(3, 3), (13, 3), (3, 13), (13, 13)], fill=(156, 126, 48, 255))
    return finish(im)


def hollow_shell():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(8, 2), (12, 5), (13, 10), (9, 14), (5, 13), (3, 8), (5, 4)],
              fill=(94, 88, 103, 255))
    d.polygon([(8, 4), (11, 6), (11, 10), (8, 12), (6, 10), (6, 6)], fill=(48, 43, 53, 255))
    d.point([(5, 3), (12, 12), (4, 11)], fill=(152, 146, 162, 255))
    return finish(im)


def ember_crown():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(2, 12), (3, 4), (6, 8), (8, 2), (10, 8), (13, 4), (14, 12)],
              fill=(117, 57, 31, 255))
    d.polygon([(3, 12), (4, 6), (6, 9), (8, 4), (10, 9), (12, 6), (13, 12)],
              fill=(238, 157, 54, 255))
    d.line([(4, 12), (12, 12)], fill=(255, 224, 142, 255))
    d.point([(8, 2), (3, 4), (13, 4)], fill=(255, 92, 42, 255))
    d.point([(6, 7), (10, 7)], fill=(255, 246, 205, 255))
    return finish(im)


def tidal_pendant():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.arc([3, 1, 13, 11], 200, 340, fill=(183, 158, 84, 255), width=1)
    d.polygon([(8, 6), (11, 10), (10, 14), (6, 14), (5, 10)], fill=(39, 103, 152, 255))
    d.polygon([(8, 8), (9, 11), (8, 13), (7, 11)], fill=(151, 226, 244, 255))
    d.line([(5, 7), (4, 3)], fill=(207, 183, 95, 255))
    d.line([(11, 7), (12, 3)], fill=(207, 183, 95, 255))
    return finish(im)


def rift_eye():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.ellipse([1, 1, 14, 14], fill=(55, 37, 81, 255))
    d.ellipse([3, 3, 12, 12], fill=(116, 71, 162, 255))
    d.polygon([(4, 8), (7, 4), (11, 5), (13, 8), (10, 12), (6, 12)], fill=(230, 207, 255, 255))
    d.ellipse([7, 6, 10, 10], fill=(49, 28, 71, 255))
    d.point([(8, 7), (5, 3), (12, 12)], fill=(255, 255, 255, 255))
    return finish(im)


def vigil_lantern():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.rectangle([5, 2, 11, 4], fill=(96, 77, 41, 255))
    d.rectangle([4, 4, 12, 13], fill=(69, 52, 39, 255))
    d.rectangle([6, 5, 10, 11], fill=(255, 224, 116, 255))
    d.rectangle([6, 12, 10, 14], fill=(126, 97, 49, 255))
    d.line([(7, 2), (8, 0), (9, 2)], fill=(207, 177, 84, 255))
    d.point([(7, 7), (9, 9), (7, 10)], fill=(255, 250, 208, 255))
    return finish(im)


def hollow_charm():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.line([(3, 3), (8, 1), (13, 3)], fill=(170, 146, 91, 255))
    d.polygon([(8, 4), (12, 7), (11, 13), (8, 15), (5, 12), (4, 7)],
              fill=(85, 80, 94, 255))
    d.polygon([(8, 6), (10, 8), (9, 12), (7, 11), (6, 8)], fill=(39, 36, 46, 255))
    d.point([(5, 5), (11, 12), (7, 14)], fill=(160, 153, 169, 255))
    return finish(im)


def abyssal_crown():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(2, 13), (3, 4), (6, 8), (8, 1), (10, 8), (13, 4), (15, 13)],
              fill=(24, 18, 38, 255))
    d.polygon([(3, 12), (4, 6), (6, 9), (8, 3), (10, 9), (12, 6), (13, 12)],
              fill=(76, 35, 111, 255))
    d.line([(4, 12), (13, 12)], fill=(205, 166, 76, 255))
    d.point([(8, 1), (3, 4), (13, 4)], fill=(167, 104, 226, 255))
    d.point([(6, 8), (10, 8), (8, 10)], fill=(236, 214, 139, 255))
    return finish(im)


def void_gauntlet():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(4, 14), (3, 8), (5, 5), (8, 4), (10, 6), (12, 5), (14, 8),
               (14, 13), (11, 15)], fill=(49, 34, 72, 255))
    d.polygon([(5, 9), (7, 6), (9, 7), (11, 7), (13, 9), (12, 12), (8, 13)],
              fill=(91, 58, 133, 255))
    for x, y in [(6, 8), (9, 5), (12, 8), (10, 11), (7, 12)]:
        d.point([(x, y)], fill=(202, 156, 255, 255))
    d.line([(4, 15), (13, 15)], fill=(166, 121, 66, 255))
    return finish(im)


def fate_dice():
    im = canvas()
    d = ImageDraw.Draw(im)
    d.polygon([(3, 5), (8, 2), (13, 5), (13, 12), (8, 15), (3, 12)],
              fill=(223, 212, 187, 255))
    d.polygon([(3, 5), (8, 8), (8, 15), (3, 12)], fill=(188, 174, 149, 255))
    d.polygon([(8, 8), (13, 5), (13, 12), (8, 15)], fill=(244, 235, 214, 255))
    for x, y in [(6, 7), (10, 8), (9, 11), (5, 10)]:
        d.point([(x, y)], fill=(69, 39, 86, 255))
    d.point([(8, 5), (4, 13)], fill=(148, 91, 214, 255))
    return finish(im)


ICONS = {
    "ember_heart": ember_heart,
    "dreamrend_fang": dreamrend_fang,
    "fleet_feather": fleet_feather,
    "vigil_carapace": vigil_carapace,
    "stonebound_pendant": stonebound_pendant,
    "echo_bell": echo_bell,
    "dreamhunter_eye": dreamhunter_eye,
    "nightmare_prism": nightmare_prism,
    "dream_anchor": dream_anchor,
    "ember_blade": ember_blade,
    "blessing_bag": blessing_bag,
    "ashen_cinder": ashen_cinder,
    "tidal_tear": tidal_tear,
    "rift_shard": rift_shard,
    "vigil_eye": vigil_eye,
    "hollow_shell": hollow_shell,
    "ember_crown": ember_crown,
    "tidal_pendant": tidal_pendant,
    "rift_eye": rift_eye,
    "vigil_lantern": vigil_lantern,
    "hollow_charm": hollow_charm,
    "abyssal_crown": abyssal_crown,
    "void_gauntlet": void_gauntlet,
    "fate_dice": fate_dice,
}


def tint_slot(img):
    """Lift the supplied ghostly slot glyphs so they read on Curios' dark UI."""
    tinted = Image.new("RGBA", img.size, (0, 0, 0, 0))
    src = img.load()
    dst = tinted.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            light = 135 + int(105 * (r / 255.0))
            dst[x, y] = (light + 20, light, light + 28, a)
    return tinted


def logo(core):
    """A 128x128 mod-list logo built from the supplied core icon."""
    size = 128
    base = Image.new("RGBA", (size, size), (10, 7, 17, 255))
    pixels = base.load()
    for y in range(size):
        for x in range(size):
            dx = (x - 64) / 64
            dy = (y - 64) / 64
            dist = min(1.0, (dx * dx + dy * dy) ** 0.5)
            shade = int(30 * (1.0 - dist))
            pixels[x, y] = (17 + shade // 2, 9 + shade // 4, 28 + shade, 255)

    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    gdraw.ellipse([22, 22, 106, 106], fill=(122, 48, 102, 150))
    glow = glow.filter(ImageFilter.GaussianBlur(11))
    base = Image.alpha_composite(base, glow)

    icon = core.resize((72, 72), Image.Resampling.NEAREST)
    base.alpha_composite(icon, (28, 28))

    draw = ImageDraw.Draw(base)
    draw.rectangle([2, 2, 125, 125], outline=(89, 54, 120, 255), width=1)
    draw.rectangle([5, 5, 122, 122], outline=(48, 30, 70, 255), width=1)
    for x, y in [(16, 18), (111, 22), (18, 111), (112, 108), (64, 9), (64, 119)]:
        draw.point([(x, y)], fill=(222, 190, 255, 255))
        draw.point([(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)],
                   fill=(103, 73, 138, 255))
    return base


def main():
    # Supplied art: keep it pixel-for-pixel rather than resampling it.
    for source, target in [
        ("item_core.png", ITEM_DIR / "nightmare_core.png"),
        ("item_fragment.png", ITEM_DIR / "nightmare_fragment.png"),
    ]:
        img = Image.open(SRC / source).convert("RGBA")
        if img.size != (16, 16):
            img = img.resize((16, 16), Image.Resampling.NEAREST)
        img.save(target, optimize=True)

    for source, target in [
        ("slot_nightmare.png", SLOT_DIR / "nightmare.png"),
        ("slot_blessing.png", SLOT_DIR / "blessing.png"),
    ]:
        img = Image.open(SRC / source).convert("RGBA")
        if img.size != (16, 16):
            img = img.resize((16, 16), Image.Resampling.NEAREST)
        tint_slot(img).save(target, optimize=True)

    for name, factory in ICONS.items():
        factory().save(ITEM_DIR / f"{name}.png", optimize=True)

    for name in ["nightmare_core", "nightmare_fragment", *ICONS.keys()]:
        model = (
            '{\n'
            '  "parent": "minecraft:item/generated",\n'
            '  "textures": {\n'
            f'    "layer0": "nightmare_core:item/{name}"\n'
            '  }\n'
            '}\n'
        )
        (MODEL_DIR / f"{name}.json").write_text(model, encoding="utf-8")

    core = Image.open(ITEM_DIR / "nightmare_core.png").convert("RGBA")
    logo(core).save(ROOT / "src" / "main" / "resources" / "logo.png", optimize=True)
    print("Generated textures, models, and logo.")


if __name__ == "__main__":
    main()
