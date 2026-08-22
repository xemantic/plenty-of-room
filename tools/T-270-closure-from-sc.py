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
# T-270 / C-0164 -- caDNAno's +-5 bp scaffold closure, read out of a scadnano `.sc`
# file by an implementation that shares no code with the Kotlin one.
#
#     python3 tools/T-270-closure-from-sc.py gpd/designs/gen1-block-honeycomb-10x6-102-109.sc
#     python3 tools/T-270-closure-from-sc.py --self-test
#
# Why it exists: `design/DesignLatticeRules.kt` answers `C-0148`'s closure from an
# imported design, and the object it is checked against -- `HoneycombRasterResidues` --
# is in the same tree, written by the same programme. This is the same arithmetic in
# another language, reading only the committed file, so that the agreement is between
# two implementations rather than inside one.
#
# The arithmetic, and the two conventions that are the whole content:
#
#   level  the axial position of a raster crossover. It is the edge of the axial window
#          the helix TURNS at -- `end` for a forward domain, `start` for a reverse one --
#          and the two sides of one crossover must agree on it (asserted below). The
#          OFFSET the file records is `level - 1` for a forward domain and `level` for a
#          reverse one, so reading offsets perturbs half the residues by one.
#   class  the neighbour class of the bond, from `grid_position` through the inverse of
#          scadnano's own published honeycomb `grid_position -> position` map.
#
# Closure: `(level - 7*class) mod 21` must lie in `{b0+5, b0-5}` for ONE `b0`, because
# one lattice constant serves the whole design. A global datum shift moves every residue
# alike, so the verdict is convention-free in the file's own origin.

import json
import math
import sys

STEP_BP = 7
PERIOD_BP = 21
SCAFFOLD_OFFSET_BP = 5


def honeycomb_cell(h, v):
    """scadnano's honeycomb grid position as this corpus's integer cross-section cell."""
    if h % 2 == 0:
        y = -(3 * v + v % 2)
    else:
        y = -(3 * v - v % 2 + 1)
    return (h, y)


def sublattice(cell):
    x, y = cell
    if y % 3 == 0 and (x - y // 3) % 2 == 0:
        return "A"
    if y % 3 == 2 and (x - (y - 2) // 3) % 2 == 0:
        return "B"
    raise ValueError("(%d, %d) is not a honeycomb lattice site" % (x, y))


def azimuth_degrees(dx, dy):
    """x is in units of d*sqrt(3)/2 and y in units of d/2, so the six bonds are 60 deg apart."""
    raw = math.degrees(math.atan2(dy / 2.0, dx * math.sqrt(3.0) / 2.0)) % 360.0
    nearest = round(raw)
    if abs(raw - nearest) > 1e-9 or nearest % 60 != 30:
        raise ValueError("offset (%d, %d) is not on a honeycomb bond azimuth" % (dx, dy))
    return nearest % 360


def bond_class(cell, azimuth):
    """Class INCREASES as the azimuth DECREASES by 120 deg: one class step is +7 bp and B-DNA
    is right-handed. Class zero is 330 deg on sublattice A and 150 deg on B -- the same bond."""
    reference = 330 if sublattice(cell) == "A" else 150
    steps = (reference - azimuth) / 120.0
    nearest = round(steps)
    if abs(steps - nearest) > 1e-9:
        raise ValueError("azimuth %s is not a whole class step from %d" % (azimuth, reference))
    return nearest % 3


def reduced_residues(design):
    cells = [honeycomb_cell(*helix["grid_position"]) for helix in design["helices"]]
    scaffolds = [s for s in design["strands"] if s.get("is_scaffold")]
    if len(scaffolds) != 1:
        raise ValueError("a raster this reads has exactly one scaffold, found %d" % len(scaffolds))
    domains = scaffolds[0]["domains"]
    residues = []
    for here, there in zip(domains, domains[1:]):
        if here["helix"] == there["helix"]:
            continue
        level = here["end"] if here["forward"] else here["start"]
        other = there["start"] if there["forward"] else there["end"]
        if level != other:
            raise ValueError("the two sides of a crossover disagree: %d against %d" % (level, other))
        a, b = cells[here["helix"]], cells[there["helix"]]
        klass = bond_class(a, azimuth_degrees(b[0] - a[0], b[1] - a[1]))
        residues.append((level - STEP_BP * klass) % PERIOD_BP)
    return residues


def admitted(b0):
    return {(b0 + SCAFFOLD_OFFSET_BP) % PERIOD_BP, (b0 - SCAFFOLD_OFFSET_BP) % PERIOD_BP}


def closure(residues):
    distinct = sorted(set(residues))
    candidates = [b0 for b0 in range(PERIOD_BP) if set(distinct) <= admitted(b0)]
    forced = min(sum(1 for r in residues if r not in admitted(b0)) for b0 in range(PERIOD_BP))
    return distinct, candidates, forced


def report(path):
    with open(path) as handle:
        design = json.load(handle)
    if design.get("grid") != "honeycomb":
        print("%s: grid '%s' -- this rule is a honeycomb statement" % (path, design.get("grid")))
        return 0
    residues = reduced_residues(design)
    distinct, candidates, forced = closure(residues)
    print("%s" % path)
    print("  raster crossovers          %d" % len(residues))
    print("  distinct reduced residues  %s" % distinct)
    print("  b0 candidates              %s" % candidates)
    print("  forced crossovers          %d" % forced)
    print("  closes                     %s" % bool(candidates))
    return 0 if candidates else 1


def self_test():
    failures = []

    def check(name, condition):
        print(("ok   " if condition else "FAIL ") + name)
        if not condition:
            failures.append(name)

    # the grid map is scadnano's, and every position it returns is a site of the lattice
    check("grid (0, 0) is the origin cell", honeycomb_cell(0, 0) == (0, 0))
    check("odd h steps the y datum", honeycomb_cell(1, 0) == (1, -1))
    check("every (h, v) in a 12x12 block is a lattice site",
          all(sublattice(honeycomb_cell(h, v)) in ("A", "B")
              for h in range(12) for v in range(12)))
    # the azimuths are the odd multiples of 30, and a vertical bond is 90 or 270
    check("the (0, +2) bond is 90 degrees", azimuth_degrees(0, 2) == 90)
    check("the (0, -2) bond is 270 degrees", azimuth_degrees(0, -2) == 270)
    check("class zero on A is its 330 degree bond", bond_class((0, 0), 330) == 0)
    # closure arithmetic
    check("two residues ten apart close", closure([4, 14])[1] == [9])
    check("three distinct residues cannot close", closure([4, 14, 5])[1] == [])
    check("and the third is the one that must be forced", closure([4, 14, 5])[2] == 1)
    return 1 if failures else 0


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--self-test":
        sys.exit(self_test())
    if len(sys.argv) < 2:
        print(__doc__ or "usage: T-270-closure-from-sc.py <design.sc> | --self-test")
        sys.exit(2)
    sys.exit(max(report(path) for path in sys.argv[1:]))
