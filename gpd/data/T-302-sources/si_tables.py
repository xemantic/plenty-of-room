#!/usr/bin/env python3
#
# Copyright 2026 Kazimierz Pogoda / Xemantic
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""T-302 -- a reader for the STAPLE SEQUENCE TABLES of Douglas et al., Nature 459:414 (2009) SI,
and a matcher against the caDNAno legacy design files `T-255` retrieved.

    python3 si_tables.py --selftest

WHY A GEOMETRIC READ AND NOT A RASTER ONE. `T-296` recorded that this PDF's text layer is
"garbled by font subsetting past the front matter" -- its figure pages embed subsetted TrueType
with no `/ToUnicode` and no `post` table, so `pdftotext` returns the subset's own code points
(`!"#$%&`). That is true of pages 1-11 and FALSE of pages 12-23, which carry the tables. The
check costs one command and it is what makes this task minutes rather than a 26-page
transcription: `pdftotext -bbox -f 12 -l 26`.

Where the layer is clean the table is read by GEOMETRY rather than by a regular expression:
every word carries an `xMin`, the header row states its own column positions (`Start`, `End`, the
title, `Length`, `Color`), and a row is the set of words sharing a `yMin`. Nothing about that
depends on a font, and it is falsifiable at every row by the table's own redundancy --
`len(sequence)` must equal the stated `Length`.

The caDNAno legacy reader is NOT duplicated here: `gpd/data/T-255-sources/cadnano_legacy.py`
already parses the archives, and this module takes its `vstrands` and walks the staple linked
list. A strand is a maximal path through `stap`; its 5' terminus is where the previous neighbour
is `-1`, which is exactly the table's own `Start` column.
"""

import html
import os
import re
import subprocess
from collections import Counter, defaultdict

NONE = -1

_WORD = re.compile(
    r'<word xMin="([\d.-]+)" yMin="([\d.-]+)" xMax="([\d.-]+)" yMax="([\d.-]+)">(.*?)</word>'
)


# ------------------------------------------------------------------ the PDF text layer

def bbox_xml(pdf_path, first_page, last_page):
    """`pdftotext -bbox` over a page range, as text. Poppler, no Python PDF dependency."""
    return subprocess.run(
        ["pdftotext", "-bbox", "-f", str(first_page), "-l", str(last_page), pdf_path, "-"],
        check=True, capture_output=True).stdout.decode("utf-8", "replace")


def pages_of(xml):
    """The `<page ...>` fragments of a `-bbox` document, in order."""
    return re.split(r"<page ", xml)[1:]


def words_of(page_xml):
    """`(xMin, yMin, text)` for every word of one page, entities decoded."""
    return [(float(a), float(b), html.unescape(t))
            for a, b, _c, _d, t in _WORD.findall(page_xml)]


# ------------------------------------------------------------------ the table

def parse_table(page_xml, column_tolerance=0.6):
    """`(title, rows)` for one staple-sequence table page, or `(None, [])` where there is none.

    A row is `{"start", "end", "sequence", "length", "color"}`. The column positions are taken
    from the header row -- the topmost row carrying the word `Start` -- and never guessed: the
    twelve tables of this SI sit at five different `Length` abscissae.
    """
    words = words_of(page_xml)
    if not words:
        return None, []
    by_y = defaultdict(list)
    for x, y, t in words:
        by_y[y].append((x, t))
    header_ys = [y for y in by_y if any(t == "Start" for _x, t in by_y[y])]
    if not header_ys:
        return None, []
    hy = min(header_ys)
    header = dict((t, x) for x, t in by_y[hy])
    if "End" not in header or "Length" not in header:
        return None, []
    columns = {"start": header["Start"], "end": header["End"], "len": header["Length"]}
    if "Color" in header:
        columns["color"] = header["Color"]
    inner = [x for x, _t in by_y[hy] if columns["end"] + 1.0 < x < columns["len"] - 1.0]
    if not inner:
        return None, []
    columns["seq"] = min(inner)
    title = " ".join(t for x, t in sorted(by_y[hy])
                     if columns["seq"] <= x < columns["len"] - 1.0)

    def column_of(x):
        for key, at in columns.items():
            if abs(x - at) < column_tolerance:
                return key
        return None

    cells = defaultdict(dict)
    for x, y, t in words:
        key = column_of(x)
        if key is not None:
            cells[y].setdefault(key, t)
    ys = sorted(cells)
    rows = []
    for i, y in enumerate(ys):
        cell = cells[y]
        if "start" not in cell or not cell.get("len", "").isdigit():
            continue
        sequence = None
        for y2 in ys[i + 1:i + 3]:
            if set(cells[y2]) == {"seq"}:
                sequence = cells[y2]["seq"]
                break
        rows.append({"start": cell["start"], "end": cell["end"], "sequence": sequence,
                     "length": int(cell["len"]), "color": cell.get("color")})
    return title, rows


def helix_base(token):
    """caDNAno's `helix[base]` as `(helix, base)`, or `None` where the cell is not one."""
    m = re.fullmatch(r"(\d+)\[(\d+)\]", token or "")
    return (int(m.group(1)), int(m.group(2))) if m else None


# ------------------------------------------------------------------ the design file

def staple_strands(vstrands):
    """`{(fivePrime, threePrime): [(helix, base), ...]}` over every LINEAR staple strand.

    Keyed on the endpoint pair because that is what the SI table's own `Start`/`End` columns are.
    """
    by_num = {v["num"]: v for v in vstrands}
    out = {}
    for v in vstrands:
        h = v["num"]
        for i, entry in enumerate(v["stap"]):
            if entry == [NONE] * 4 or entry[0] != NONE:
                continue
            path = []
            cursor = (h, i)
            while cursor != (NONE, NONE):
                if cursor in path:
                    raise ValueError("staple strand cycles at %r" % (cursor,))
                path.append(cursor)
                nh, nb = by_num[cursor[0]]["stap"][cursor[1]][2:4]
                cursor = (nh, nb)
            out[(path[0], path[-1])] = path
    return out


def staple_colours(vstrands):
    """`{(helix, base): '#rrggbb'}` from the file's own `stap_colors`, keyed on the 5' base."""
    out = {}
    for v in vstrands:
        for base, value in v.get("stap_colors", []):
            out[(v["num"], base)] = "#%06x" % (int(value) & 0xFFFFFF)
    return out


def scaffold_path(vstrands):
    """The scaffold as a list of `(helix, base)` from its 5' terminus, or `None` if not linear."""
    by_num = {v["num"]: v for v in vstrands}
    starts = [(v["num"], i) for v in vstrands
              for i, e in enumerate(v["scaf"]) if e != [NONE] * 4 and e[0] == NONE]
    if len(starts) != 1:
        return None
    path = []
    cursor = starts[0]
    while cursor != (NONE, NONE):
        path.append(cursor)
        nh, nb = by_num[cursor[0]]["scaf"][cursor[1]][2:4]
        cursor = (nh, nb)
    return path


def scaffold_crossings(vstrands):
    """Every deduplicated inter-helix scaffold adjacency, as sorted `((h,b), (h,b))` pairs."""
    seen = set()
    for v in vstrands:
        h = v["num"]
        for i, (ph, pb, nh, nb) in enumerate(v["scaf"]):
            for oh, ob in ((ph, pb), (nh, nb)):
                if oh in (NONE, h):
                    continue
                seen.add(tuple(sorted(((h, i), (oh, ob)))))
    return sorted(seen)


# ------------------------------------------------------------------ the match

def match(rows, strands, colours=None):
    """Match an SI table against a design file's staple census. Exact integer arithmetic."""
    table = {}
    unparsed = 0
    for row in rows:
        a, b = helix_base(row["start"]), helix_base(row["end"])
        if a is None or b is None:
            unparsed += 1
            continue
        table[(a, b)] = row
    matched = [k for k in table if k in strands]
    length_agrees = sum(1 for k in matched if len(strands[k]) == table[k]["length"])
    sequence_agrees = sum(1 for r in rows
                          if r["sequence"] is not None and len(r["sequence"]) == r["length"])
    colour_agrees = None
    if colours is not None:
        colour_agrees = sum(1 for k in matched
                            if table[k]["color"] is not None
                            and colours.get(k[0]) == table[k]["color"].lower())
    drawn_only = sorted(set(strands) - set(table))
    return {
        "tableRows": len(rows),
        "tableRowsUnparsed": unparsed,
        "sumOfLengthColumn": sum(r["length"] for r in rows),
        "sumOfSequenceCharacters": sum(len(r["sequence"] or "") for r in rows),
        "rowsWhereStatedLengthIsTheSequenceLength": sequence_agrees,
        "fileStrands": len(strands),
        "fileStapleNucleotides": sum(len(p) for p in strands.values()),
        "rowsResolvingToAFileStrand": len(matched),
        "rowsWhereTheLengthAlsoAgrees": length_agrees,
        "rowsResolvingToNoFileStrand": len(table) - len(matched),
        "strandsDrawnButNotTabulated": len(drawn_only),
        "nucleotidesDrawnButNotTabulated": sum(len(strands[k]) for k in drawn_only),
        "rowsWhereTheColourAgrees": colour_agrees,
        "drawnOnlyLengthHistogram": dict(sorted(Counter(
            len(strands[k]) for k in drawn_only).items())),
        "drawnOnlyColourHistogram": (dict(sorted(Counter(
            colours.get(k[0]) for k in drawn_only).items(), key=lambda kv: -kv[1]))
            if colours is not None else None),
        "tabulatedColourHistogram": (dict(sorted(Counter(
            colours.get(k[0]) for k in matched).items(), key=lambda kv: -kv[1]))
            if colours is not None else None),
        "_orderedKeys": matched,
        "_drawnOnlyKeys": drawn_only,
    }


def paired_windows(strands, ordered_keys):
    """`{helix: (first, last, count)}` over the bases the ORDERED staples cover."""
    covered = defaultdict(set)
    for key in ordered_keys:
        for h, b in strands[key]:
            covered[h].add(b)
    return {h: (min(s), max(s), len(s)) for h, s in covered.items()}


# ------------------------------------------------------------------ self-tests

_FIXTURE = """<page width="100" height="100">
 <flow><block><line>
  <word xMin="10.0" yMin="10.0" xMax="20.0" yMax="20.0">Start</word>
  <word xMin="30.0" yMin="10.0" xMax="40.0" yMax="20.0">End</word>
  <word xMin="50.0" yMin="10.0" xMax="60.0" yMax="20.0">widget</word>
  <word xMin="62.0" yMin="10.0" xMax="70.0" yMax="20.0">sequences</word>
  <word xMin="80.0" yMin="10.0" xMax="90.0" yMax="20.0">Length</word>
  <word xMin="95.0" yMin="10.0" xMax="99.0" yMax="20.0">Color</word>
 </line></block></flow>
 <flow><block><line>
  <word xMin="10.0" yMin="30.0" xMax="20.0" yMax="40.0">0[48]</word>
  <word xMin="30.0" yMin="30.0" xMax="40.0" yMax="40.0">0[51]</word>
  <word xMin="80.0" yMin="30.0" xMax="90.0" yMax="40.0">4</word>
  <word xMin="95.0" yMin="30.0" xMax="99.0" yMax="40.0">#03B6A2</word>
 </line><line>
  <word xMin="50.0" yMin="32.0" xMax="70.0" yMax="42.0">ACGT</word>
 </line></block></flow>
 <flow><block><line>
  <word xMin="10.0" yMin="50.0" xMax="20.0" yMax="60.0">1[16]</word>
  <word xMin="30.0" yMin="50.0" xMax="40.0" yMax="60.0">1[18]</word>
  <word xMin="80.0" yMin="50.0" xMax="90.0" yMax="60.0">3</word>
  <word xMin="95.0" yMin="50.0" xMax="99.0" yMax="60.0">#CC0000</word>
 </line><line>
  <word xMin="50.0" yMin="52.0" xMax="70.0" yMax="62.0">TTT</word>
 </line></block></flow>
</page>"""


def _linear(helix, first, count, colour=None, length=147):
    """One vstrand carrying a single staple strand from `first` running `count` bases upward."""
    stap = [[NONE] * 4 for _ in range(length)]
    for k in range(count):
        b = first + k
        stap[b] = [helix if k else NONE, b - 1 if k else NONE,
                   helix if k < count - 1 else NONE, b + 1 if k < count - 1 else NONE]
    v = {"num": helix, "row": 0, "col": helix,
         "scaf": [[NONE] * 4 for _ in range(length)], "stap": stap,
         "loop": [0] * length, "skip": [0] * length, "stap_colors": []}
    if colour is not None:
        v["stap_colors"] = [[first, colour]]
    return v


def _selftest():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    title, rows = parse_table(_FIXTURE)
    check("the title comes from the header's own inner columns", title == "widget sequences")
    check("both data rows are read", len(rows) == 2)
    check("the header row is not read as data", all(r["start"] != "Start" for r in rows))
    check("the Start cell is the leftmost column", rows[0]["start"] == "0[48]")
    check("the sequence comes from the line below its own row", rows[0]["sequence"] == "ACGT")
    check("the Length column is an integer", rows[0]["length"] == 4)
    check("the Color column survives", rows[0]["color"] == "#03B6A2")
    check("the sum of the Length column is the sum of the Length column",
          sum(r["length"] for r in rows) == 7)

    check("a page with no Start header is not a table", parse_table("<page></page>") == (None, []))
    check("an empty page is not a table", parse_table("") == (None, []))

    check("helix[base] parses", helix_base("12[137]") == (12, 137))
    check("a dash is not a coordinate", helix_base("-") is None)
    check("a missing cell is not a coordinate", helix_base(None) is None)

    # a two-helix design: helix 0 carries one 4-base staple and a 3-base one, helix 1 nothing
    v0 = _linear(0, 48, 4, colour=0x03B6A2)
    v0["stap_colors"].append([16, 0xCC0000])
    for k in range(3):
        b = 16 + k
        v0["stap"][b] = [0 if k else NONE, b - 1 if k else NONE,
                         0 if k < 2 else NONE, b + 1 if k < 2 else NONE]
    v1 = _linear(1, 16, 3, colour=0xCC0000)
    strands = staple_strands([v0, v1])
    check("every linear staple strand is found", len(strands) == 3)
    check("a strand is keyed on its own 5' and 3' termini",
          ((0, 48), (0, 51)) in strands and ((1, 16), (1, 18)) in strands)
    check("a strand's length is its base count", len(strands[((0, 48), (0, 51))]) == 4)
    colours = staple_colours([v0, v1])
    check("a stored colour is rendered as lower-case hex", colours[(0, 48)] == "#03b6a2")

    report = match(rows, strands, colours)
    check("the table's two rows both resolve to a file strand",
          report["rowsResolvingToAFileStrand"] == 2)
    check("and their lengths agree", report["rowsWhereTheLengthAlsoAgrees"] == 2)
    check("and their colours agree", report["rowsWhereTheColourAgrees"] == 2)
    check("the sum of the Length column is reported", report["sumOfLengthColumn"] == 7)
    check("a stated Length is checked against its own sequence",
          report["rowsWhereStatedLengthIsTheSequenceLength"] == 2)
    check("the strand the table omits is counted",
          report["strandsDrawnButNotTabulated"] == 1)
    check("and its nucleotides are counted",
          report["nucleotidesDrawnButNotTabulated"] == 3)
    check("the omitted strand is located, not merely counted",
          report["_drawnOnlyKeys"] == [((0, 16), (0, 18))])
    check("the file's own totals are reported beside the table's",
          report["fileStrands"] == 3 and report["fileStapleNucleotides"] == 10)

    windows = paired_windows(strands, report["_orderedKeys"])
    check("the ordered set's coverage is a per-helix window",
          windows[0] == (48, 51, 4) and windows[1] == (16, 18, 3))

    # a row naming a strand the file does not carry must be reported, not silently dropped
    absent = match([{"start": "9[9]", "end": "9[12]", "sequence": "ACGT", "length": 4,
                     "color": "#000000"}], strands, colours)
    check("a row resolving to no file strand is counted",
          absent["rowsResolvingToNoFileStrand"] == 1
          and absent["rowsResolvingToAFileStrand"] == 0)

    # a cyclic staple must raise rather than loop forever
    cyc = _linear(0, 10, 3)
    cyc["stap"][12] = [0, 11, 0, 10]
    cyc["stap"][10] = [NONE, NONE, 0, 11]
    try:
        staple_strands([cyc])
        # the 5' walk terminates only if the cycle is reachable from a 5' end; here it is
        failures.append("a cyclic staple: expected a refusal")
    except ValueError:
        pass

    # the scaffold reader
    s0 = _linear(0, 5, 4)
    s0["scaf"] = s0["stap"]
    s0["stap"] = [[NONE] * 4 for _ in range(147)]
    check("a linear scaffold is walked from its own 5' end",
          scaffold_path([s0]) == [(0, 5), (0, 6), (0, 7), (0, 8)])
    check("a scaffold with no unique 5' end is refused", scaffold_path([_linear(0, 5, 4)]) is None)

    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


if __name__ == "__main__":
    import sys
    sys.exit(_selftest())
