#!/usr/bin/env python3
"""T-255 -- digitise panels (d) and (e) of Figure 2 of the caDNAno paper (Douglas, Marblestone,
Teerapittayanon, Vazquez, Church & Shih, *Nucleic Acids Res.* 37:5001), rendered from the
publisher PDF at 600 dpi.

Panel (d) is `% yield (gel)`; panel (e) is `% yield after purification (TEM)`. Both are bar charts
over the seven block designs i..vii, with horizontal rules at 0, 15, 30, 45 and 60.

The scale is taken from the BASELINE and the 60 rule, and the answer is cross-checked against the
paper's own two ORDINAL statements: `(i)` has the greatest gel yield and `(ii)` the greatest TEM
yield after purification. `CLAUDE.md`: a transcription from a figure is checkable, and it must be.
"""
import json
import subprocess
import sys
from PIL import Image

PDF = "cadnano-NAR-gkp436.pdf"
PAGE = 4
DPI = 600
LABELS = ["i", "ii", "iii", "iv", "v", "vi", "vii"]


def render(out):
    subprocess.run(["pdftoppm", "-r", str(DPI), "-f", str(PAGE), "-l", str(PAGE), "-png",
                    PDF, out], check=True)
    return out + "-%d.png" % PAGE


def orange(px):
    r, g, b = px[:3]
    return r > 200 and 100 < g < 190 and b < 110


def panel_bars(im, box):
    """Find the bar tops in a panel, in image rows, plus the baseline."""
    x0, y0, x1, y1 = box
    cols = {}
    for x in range(x0, x1):
        top = None
        bot = None
        for y in range(y0, y1):
            if orange(im.getpixel((x, y))):
                if top is None:
                    top = y
                bot = y
        if top is not None and bot - top > 5:
            cols[x] = (top, bot)
    # group contiguous columns into bars
    bars = []
    run = []
    prev = None
    for x in sorted(cols):
        if prev is not None and x - prev > 3:
            bars.append(run)
            run = []
        run.append(x)
        prev = x
    if run:
        bars.append(run)
    out = []
    for run in bars:
        if len(run) < 20:
            continue
        tops = [cols[x][0] for x in run]
        bots = [cols[x][1] for x in run]
        out.append({"xMin": run[0], "xMax": run[-1],
                    "top": sorted(tops)[len(tops) // 2],
                    "bottom": sorted(bots)[len(bots) // 2]})
    return out


def gridlines(im, box):
    """Rows of the horizontal rules, found by counting dark pixels across the WHOLE plot width.

    Probing a narrow strip does not work: a tall bar hides whichever rules it crosses, so the
    detector must read a row that a bar cannot occlude along its whole length."""
    x0, y0, x1, y1 = box
    span = x1 - x0
    rows = []
    for y in range(y0, y1):
        dark = sum(1 for x in range(x0, x1)
                   if sum(im.getpixel((x, y))[:3]) < 400)
        if dark > 0.55 * span:
            rows.append(y)
    groups, run = [], []
    for y in rows:
        if run and y - run[-1] > 3:
            groups.append(sum(run) // len(run))
            run = []
        run.append(y)
    if run:
        groups.append(sum(run) // len(run))
    return groups


def main():
    png = render("/tmp/T-255-nar")
    im = Image.open(png).convert("RGB")
    w, h = im.size
    XL = int(w * 0.70)
    ys = sorted({y for y in range(int(h * 0.55), int(h * 0.95))
                 for x in range(XL, w)
                 if orange(im.getpixel((x, y)))})
    splits = [i for i in range(1, len(ys)) if ys[i] - ys[i - 1] > 200]
    assert len(splits) == 1, "expected exactly two orange bands, got %d" % (len(splits) + 1)
    bands = [(ys[0], ys[splits[0] - 1]), (ys[splits[0]], ys[-1])]
    result = {}
    names = ("gelYieldPercent", "temYieldAfterPurificationPercent")
    for i, (name, (ytop, ybot)) in enumerate(zip(names, bands)):
        top = ytop - 450 if i == 0 else (bands[i - 1][1] + ytop) // 2
        region = (XL, top, w - 1, ybot + 40)
        bars = panel_bars(im, region)
        assert len(bars) == 7, "%s: expected 7 bars, got %d" % (name, len(bars))
        left = min(b["xMin"] for b in bars) - 20
        right = max(b["xMax"] for b in bars) + 20
        rules = gridlines(im, (left, top, right, ybot + 40))
        assert len(rules) >= 3, "%s: expected at least three rules, got %d" % (name, len(rules))
        base = max(rules)                      # the zero rule
        # the axis ladder is 15 units per rule; recover the unit from the SPACINGS, so that a rule
        # occluded by a tall bar costs nothing and no rule has to be identified by its label
        gaps = sorted(b - a for a, b in zip(rules, rules[1:]))
        unit = min(g for g in gaps if g > 50)
        for g in gaps:
            assert abs(g / unit - round(g / unit)) < 0.05, \
                "%s: rule spacing %d is not a whole multiple of %d" % (name, g, unit)
        scale = 15.0 / unit
        result[name] = {LABELS[k]: round((base - b["top"]) * scale, 1)
                        for k, b in enumerate(bars)}
        result[name + "Provenance"] = {
            "zeroRuleRow": base, "ruleSpacingPx": unit, "rulesFoundRows": rules,
            "percentPerPixel": round(scale, 6),
        }
    d = result["gelYieldPercent"]
    e = result["temYieldAfterPurificationPercent"]
    assert max(d, key=d.get) == "i", "the paper says design (i) has the greatest gel yield"
    assert max(e, key=e.get) == "ii", \
        "the paper says design (ii) is the most robust after purification"
    result["crossChecks"] = {
        "paperSaysGreatestGelYieldIsI": max(d, key=d.get) == "i",
        "paperSaysMostRobustAfterPurificationIsII": max(e, key=e.get) == "ii",
    }
    print(json.dumps(result, indent=1))
    json.dump(result, open("cadnano-NAR-fig2de-digitised.json", "w"), indent=1)


if __name__ == "__main__":
    main()
