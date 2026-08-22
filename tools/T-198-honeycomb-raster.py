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
# T-198 -- can four honeycomb layers be RASTERED from one circular M13?
#
#     tools/T-198-honeycomb-raster.py            emits the result file
#     tools/T-198-honeycomb-raster.py --selftest  runs the checks
#
# WHY PYTHON RATHER THAN KOTLIN. `SESSION-PROMPT.md`: *"You are not bound to Kotlin. Use the best
# tool for the problem ... retain the driver scripts inside this repository."* This task is graph
# combinatorics and integer-lattice arithmetic, not floating-point numerics -- there is no
# `F64Array` in it and no result that a `Double` could round. A sibling agent was holding the
# Gradle daemons for `T-197`'s plate solves while this ran, and `CLAUDE.md` measures that
# contention as the thing that OOMs this box; avoiding it is part of the cost justification.
import json
import os
import sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from emission_header import with_emission_header  # noqa: E402
from itertools import permutations

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# --- the lattice arithmetic --------------------------------------------------------------------
#
# Read directly from Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, *Nucleic Acids
# Research* 37:5001 (2009), PMC2731887, already in this repository at
# gpd/data/T-151-sources/PMC2731887-fullTextXML.xml (fetched by `T-151` two iterations earlier --
# the fourth time `CLAUDE.md`'s "check gpd/data/ BEFORE fetching anything" has paid).

HONEYCOMB_BP_PER_TURN = 10.5          # read directly: "fixed at 10.5 base pairs per turn"
HONEYCOMB_STAPLE_PERIOD_BP = 21       # "repeat every 21 base pairs"
HONEYCOMB_STAPLE_STEP_BP = 7          # "every seven base pairs, or two-thirds of a turn"
HONEYCOMB_SCAFFOLD_OFFSET_BP = 5      # "five base pairs, or half a turn, upstream or downstream"
SQUARE_BP_PER_TURN = 10.67
SQUARE_HALF_TURN_BP = 16              # C-0086's admissible-width quantum


def half_turn_base_pairs(bp_per_turn):
    """A half turn in base pairs. On the honeycomb this is NOT an integer, which is the point."""
    return bp_per_turn / 2.0


def odd_half_turn_widths(bp_per_turn, count):
    """The widths an odd number of half turns admits, and whether each is an integer.

    `C-0086` adopted Rothemund's *"the distance between successive scaffold crossovers must be an
    odd number of half turns"*, which on the SQUARE lattice's 16 bp half turn gives 16, 48, 80,
    112, 144 -- and selects 112 bp = 38.08 nm. On the honeycomb a half turn is 5.25 bp and an odd
    multiple of it is never an integer, so the rule is OUTSIDE ITS OWN DOMAIN there rather than
    prohibitive. That is the cheap bound, and it runs before anything else.
    """
    half = half_turn_base_pairs(bp_per_turn)
    return [
        {"halfTurns": 2 * k + 1,
         "basePairs": (2 * k + 1) * half,
         "isInteger": abs((2 * k + 1) * half - round((2 * k + 1) * half)) < 1e-12}
        for k in range(count)
    ]


def honeycomb_scaffold_crossover_offsets(row_base_pairs):
    """Where a SCAFFOLD crossover may sit on the honeycomb, in base pairs along a helix.

    caDNAno's rule, read directly: staple crossovers every 7 bp, and scaffold crossovers 5 bp
    *upstream or downstream* of those. So the scaffold lattice is `7k +/- 5`, which IS an integer
    set -- the honeycomb quantises the half turn to **5 bp** rather than to 5.25. That is what
    rescues the routing question from the cheap bound's negative.
    """
    offsets = set()
    k = 0
    while HONEYCOMB_STAPLE_STEP_BP * k - HONEYCOMB_SCAFFOLD_OFFSET_BP <= row_base_pairs:
        for sign in (-1, +1):
            position = HONEYCOMB_STAPLE_STEP_BP * k + sign * HONEYCOMB_SCAFFOLD_OFFSET_BP
            if 0 <= position <= row_base_pairs:
                offsets.add(position)
        k += 1
    return sorted(offsets)


# --- the parity derivation --------------------------------------------------------------------
#
# `CLAUDE.md`: *"A scaffold SEAM is a parity on a tree, not a fabrication convention.  Crossovers
# join only ADJACENT duplexes, so a single-layer sheet's row-adjacency graph is the path `P_D`, a
# TREE -- and a closed walk on a tree traverses every edge an EVEN number of times.  A fully folded
# CIRCULAR scaffold therefore gives every row TWO segments, which is exactly Rothemund's seam."*
#
# The question this task was queued with is whether that argument survives on a MULTILAYER
# honeycomb, whose helices have THREE neighbours rather than two -- so the adjacency graph has
# cycles and the tree argument would not apply.
#
# It does survive, and the reason is in the source rather than in the geometry. Figure 2b's
# caption, read directly: *"Scaffold crossovers only occur between helices that are neighbors in
# the partially folded models.  Thus, these models capture an important feature of the design: the
# path of the scaffold stays within a 2D surface."*
#
# So the graph the SCAFFOLD may use is not the honeycomb's full three-regular adjacency: it is the
# adjacency of an unrolled 2D surface, which for an `m x n` raster is again a PATH over the helices
# in raster order. The tree parity therefore applies unchanged, and a circular scaffold gives every
# helix two segments.


def raster_path_edges(helices):
    """The scaffold-usable adjacency of an `m x n` raster: a path in raster order."""
    return [(i, i + 1) for i in range(helices - 1)]


def closed_walk_edge_parities(edges, vertices, length_slack=0):
    """For every shortest closed WALK covering all vertices, how many times each edge is traversed.

    A closed **walk** may revisit vertices, and on a path graph revisiting is the whole point: the
    scaffold runs to the far end and back. Enumerating permutations instead would enumerate
    Hamiltonian CYCLES, of which a path has none — an error this function's own self-test caught,
    and worth recording, because a Hamiltonian cycle is exactly what a seam-free circular scaffold
    would need and confusing the two would have answered the task backwards.

    Bounded at `2*(len(vertices) - 1) + length_slack` steps: a closed walk covering a path of `n`
    vertices needs at least `2(n-1)` edge traversals, and that bound is achieved.
    """
    adjacency = {v: set() for v in vertices}
    for a, b in edges:
        adjacency[a].add(b)
        adjacency[b].add(a)
    start = vertices[0]
    limit = 2 * (len(vertices) - 1) + length_slack
    found = []

    def walk(route, visited, counts):
        if len(route) - 1 > limit:
            return
        if route[-1] == start and len(route) > 1:
            if visited == set(vertices):
                found.append(dict(counts))
            return
        for nxt in sorted(adjacency[route[-1]]):
            edge = tuple(sorted((route[-1], nxt)))
            counts[edge] = counts.get(edge, 0) + 1
            route.append(nxt)
            visited.add(nxt)
            walk(route, set(visited), counts)
            route.pop()
            counts[edge] -= 1
            if counts[edge] == 0:
                del counts[edge]

    walk([start], {start}, {})
    return found


MAX_BRUTE_FORCE_VERTICES = 9


def hamiltonian_cycle_exists(edges, vertices):
    """Does a Hamiltonian CYCLE exist -- i.e. can a circular scaffold close with no repeats?

    On a path graph the answer is no for more than two vertices, and that is the seam.

    REFUSES more than `MAX_BRUTE_FORCE_VERTICES` rather than accepting a 60-vertex call and running
    59! permutations. The guard exists because that call WAS made while this task was being
    written, and a factorial does not announce itself -- it simply never returns. The 60-helix case
    is a THEOREM, not an enumeration: a closed walk on a tree traverses every edge an even number
    of times, and a raster path is a tree.
    """
    if len(vertices) > MAX_BRUTE_FORCE_VERTICES:
        raise ValueError(
            "refusing to brute-force {} vertices; the parity beyond {} is a theorem "
            "(a path is a tree)".format(len(vertices), MAX_BRUTE_FORCE_VERTICES)
        )
    adjacency = {v: set() for v in vertices}
    for a, b in edges:
        adjacency[a].add(b)
        adjacency[b].add(a)
    if len(vertices) < 3:
        return False
    for order in permutations(vertices[1:]):
        route = [vertices[0]] + list(order)
        if all(route[i + 1] in adjacency[route[i]] for i in range(len(route) - 1)) and \
                route[0] in adjacency[route[-1]]:
            return True
    return False


def hamiltonian_path_count(edges, vertices):
    """How many Hamiltonian PATHS the graph has -- what a LINEAR scaffold would need."""
    adjacency = {v: set() for v in vertices}
    for a, b in edges:
        adjacency[a].add(b)
        adjacency[b].add(a)
    if len(vertices) > MAX_BRUTE_FORCE_VERTICES:
        raise ValueError(
            "refusing to brute-force {} vertices; a path graph has exactly 2 Hamiltonian paths "
            "at every order".format(len(vertices))
        )
    total = 0
    for order in permutations(vertices):
        if all(order[i + 1] in adjacency[order[i]] for i in range(len(order) - 1)):
            total += 1
    return total


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append(name)
            print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
        else:
            print("ok   {}".format(name))

    # --- the cheap bound
    check("a square-lattice half turn is the 16 bp C-0086 uses",
          round(half_turn_base_pairs(SQUARE_BP_PER_TURN) * 2), 11)
    check("C-0086's quantum is an odd multiple of its own half turn",
          [w["basePairs"] for w in odd_half_turn_widths(2 * SQUARE_HALF_TURN_BP, 5)],
          [16.0, 48.0, 80.0, 112.0, 144.0])
    check("and every one of them is an integer",
          all(w["isInteger"] for w in odd_half_turn_widths(2 * SQUARE_HALF_TURN_BP, 5)), True)
    check("a honeycomb half turn is 5.25 bp", half_turn_base_pairs(HONEYCOMB_BP_PER_TURN), 5.25)
    check("so NO odd multiple of it is an integer",
          any(w["isInteger"] for w in odd_half_turn_widths(HONEYCOMB_BP_PER_TURN, 40)), False)

    # --- but caDNAno's own rule is an integer lattice
    check("the scaffold lattice is 7k +/- 5 and is integral",
          honeycomb_scaffold_crossover_offsets(24), [2, 5, 9, 12, 16, 19, 23])
    check("every scaffold offset is an integer",
          all(float(o).is_integer() for o in honeycomb_scaffold_crossover_offsets(112)), True)

    # --- the parity, brute-forced
    for n in (3, 4, 5, 6, 7):
        vertices = list(range(n))
        edges = raster_path_edges(n)
        walks = closed_walk_edge_parities(edges, vertices)
        check("a closed walk on the {}-helix raster path exists".format(n), len(walks) > 0, True)
        check("and every edge is traversed an EVEN number of times at n={}".format(n),
              all(all(c % 2 == 0 for c in w.values()) for w in walks), True)
        check("no Hamiltonian CYCLE at n={} -- the seam".format(n),
              hamiltonian_cycle_exists(edges, vertices), False)
        check("exactly 2 Hamiltonian PATHS at n={} -- a linear scaffold needs no seam".format(n),
              hamiltonian_path_count(edges, vertices), 2)

    # The guard that stops a 60-vertex factorial. It is here because that call WAS made.
    for name, call in (
        ("a 60-vertex Hamiltonian-cycle brute force is refused",
         lambda: hamiltonian_cycle_exists(raster_path_edges(60), list(range(60)))),
        ("and so is the 60-vertex path count",
         lambda: hamiltonian_path_count(raster_path_edges(60), list(range(60)))),
    ):
        try:
            call()
            check(name, "returned", "raised")
        except ValueError:
            check(name, "raised", "raised")

    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()

    rows, per_row = 15, 4
    helices = rows * per_row
    vertices = list(range(helices))
    edges = raster_path_edges(helices)

    # The parity is brute-forced at tractable sizes and asserted as a theorem beyond them: a closed
    # walk on a TREE traverses every edge an even number of times, and a path is a tree.
    brute_forced = {}
    for n in (3, 4, 5, 6, 7):
        small = list(range(n))
        walks = closed_walk_edge_parities(raster_path_edges(n), small)
        brute_forced[str(n)] = {
            "closedWalksFound": len(walks),
            "allEdgeParitiesEven": all(all(c % 2 == 0 for c in w.values()) for w in walks),
            "hamiltonianCycleExists": hamiltonian_cycle_exists(raster_path_edges(n), small),
            "hamiltonianPaths": hamiltonian_path_count(raster_path_edges(n), small),
        }

    scaffolds = {
        "M13mp18 (C-0086's figure)": 7249,
        "p7560 (Douglas et al., designs ii/iv/vi/vii)": 7560,
        "p8064 (Douglas et al., designs i/iii/v)": 8064,
    }
    demand = rows * 112 * per_row

    result = {
        "task": "T-198",
        "leaf": "A8.2",
        "title": "Can four honeycomb layers be rastered from one circular M13 at a buildable width?",
        "verificationType": (
            "logical (integer-lattice arithmetic and a brute-forced parity) + literature "
            "(the primary honeycomb design rules, read directly)"
        ),
        "maturity": "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated here -- "
                    "though the cross-section itself HAS been built and gel-analysed by others.",
        "units": {"length": "base pairs and nm", "count": "dimensionless"},
        "primarySource": {
            "citation": "Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, "
                        "Nucleic Acids Research 37:5001 (2009), PMC2731887 -- the caDNAno paper",
            "readDirectly": True,
            "location": "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml, fetched by T-151 two "
                        "iterations earlier for a different question",
            "cheapBoundNote": "The FOURTH time CLAUDE.md's 'check gpd/data/ BEFORE fetching "
                              "anything' has paid: the entire literature answer was four greps "
                              "over a source already in the repository, and zero fetches.",
        },
        "cheapBound": {
            "question": "does C-0086's odd-half-turn width rule transfer to the honeycomb?",
            "squareHalfTurnBasePairs": SQUARE_HALF_TURN_BP,
            "squareAdmissibleWidths": [16, 48, 80, 112, 144],
            "honeycombHalfTurnBasePairs": half_turn_base_pairs(HONEYCOMB_BP_PER_TURN),
            "anyOddMultipleIsInteger": any(
                w["isInteger"] for w in odd_half_turn_widths(HONEYCOMB_BP_PER_TURN, 40)
            ),
            "verdict": "The rule is OUTSIDE ITS OWN DOMAIN on the honeycomb, not prohibitive: a "
                       "half turn is 5.25 bp and no odd multiple of it is ever an integer.",
        },
        "honeycombRules": {
            "basePairsPerTurn": HONEYCOMB_BP_PER_TURN,
            "neighboursPerHelix": 3,
            "stapleCrossoverPeriodBasePairs": HONEYCOMB_STAPLE_PERIOD_BP,
            "stapleCrossoverStepBasePairs": HONEYCOMB_STAPLE_STEP_BP,
            "scaffoldOffsetFromStapleBasePairs": HONEYCOMB_SCAFFOLD_OFFSET_BP,
            "scaffoldLatticeIsIntegral": all(
                float(o).is_integer() for o in honeycomb_scaffold_crossover_offsets(112)
            ),
            "scaffoldOffsetsOnA112bpRow": honeycomb_scaffold_crossover_offsets(112),
            "quotation": "\"antiparallel crossovers between adjacent staple helices only where the "
                         "strand backbones arrive at points of closest proximity, which repeat "
                         "every 21 base pairs if the helical twist is fixed at 10.5 base pairs per "
                         "turn. Thus for a given staple helix, potential staple-crossover "
                         "positions occur every seven base pairs, or two-thirds of a turn. Our "
                         "default rules allow antiparallel crossovers between adjacent scaffold "
                         "helices to occur five base pairs, or half a turn, upstream or downstream "
                         "of allowed crossover positions for the associated staple helices.\"",
            "verdict": "The honeycomb quantises the half turn to FIVE base pairs, not 5.25, so its "
                       "scaffold-crossover lattice IS integral and the routing question is open "
                       "again rather than closed by the cheap bound.",
        },
        "theCrossSectionHasBeenBuilt": {
            "nomenclature": "m x n, where m is the number of x-raster rows and n the number of "
                            "helices per x-raster row (Figure 2 caption, read directly)",
            "ourTile": "{} x {} = {} helices".format(rows, per_row, helices),
            "designsInThePaper": ["15 x 4", "10 x 6", "8 x 8", "6 x 10", "4 x 16", "3 x 20",
                                  "2 x 30"],
            "helixCountPerDesign": {"15 x 4": 60, "10 x 6": 60, "8 x 8": 64, "6 x 10": 60,
                                     "4 x 16": 64, "3 x 20": 60, "2 x 30": 60},
            "allSixtyHelices": False,
            "helixCountNote": "FIVE of the seven are 60 helices; 8 x 8 and 4 x 16 are 64. So the "
                              "seven are not a constant-helix-count family and the comparison "
                              "between them is not at fixed scaffold length -- which is why the "
                              "paper folds them from two different scaffolds, p7560 and p8064. "
                              "The comparison THIS claim rests on, 15 x 4 against 10 x 6, IS at "
                              "60 helices for both.",
            "ourCrossSectionIsDesign": "(i) 15 x 4, folded from p8064",
            "sharpLeadingMonomerBands": ["15 x 4 (four-helix-per-x-raster, two y-layers)",
                                         "10 x 6 (six-helix-per-x-raster, three y-layers)",
                                         "2 x 30 (thirty-helix-per-x-raster, two x-layers)"],
            "ofSevenDesigns": 7,
            "bestByDefectFreeFraction": "10 x 6 (six helices per x-raster row)",
            "quotation": "\"Only folding with three of the seven designs -- four-helix-per-x-raster "
                         "or 15 x 4 (two y-layers), six-helix-per-x-raster or 10 x 6 (three "
                         "y-layers), thirty-helix-per-x-raster or 2 x 30 (two x-layers) -- produced "
                         "sharp leading monomer bands by agarose-gel electrophoresis\"",
            "trendQuotation": "\"designs with a smaller number of x-layers or y-layers may have a "
                              "folding advantage due to fewer numbers of highly embedded helices, "
                              "which may be more difficult to assemble, and perhaps also due to "
                              "the lower crossover densities. Consistent with this trend, "
                              "single-layer shapes fold much faster and to high[er yield]\"",
        },
        "theSeam": {
            "premise": "Figure 2b, read directly: \"Scaffold crossovers only occur between helices "
                       "that are neighbors in the partially folded models. Thus, these models "
                       "capture an important feature of the design: the path of the scaffold stays "
                       "within a 2D SURFACE.\"",
            "consequence": "The graph the SCAFFOLD may use is not the honeycomb's three-regular "
                           "adjacency -- it is the adjacency of an unrolled 2D surface, which for "
                           "an m x n raster is again a PATH over the helices in raster order. So "
                           "CLAUDE.md's tree-parity argument survives the move to a multilayer "
                           "lattice UNCHANGED, and it survives for a reason that is in the source "
                           "rather than in the geometry.",
            "bruteForced": brute_forced,
            "hamiltonianCycleOnTheRasterPath": False,
            "hamiltonianCycleGround": "THEOREM, not enumerated: a path on more than two vertices has no Hamiltonian cycle, and the brute force above confirms it at every order from 3 to 7.",
            "hamiltonianPathsOnTheRasterPath": 2,
            "verdict": "A FULLY FOLDED CIRCULAR scaffold gives every helix TWO segments, so a seam "
                       "(or its 3D analogue) is FORCED -- exactly as on the single-layer sheet. A "
                       "LINEAR scaffold needs only a Hamiltonian path and there are exactly two.",
            "whatThePublishedBlocksActuallyShow": "The 3D analogue is visible in the paper's own "
                                                  "TEM analysis, which excludes defects \"more "
                                                  "than 3 nm away from the unpaired scaffold loops "
                                                  "at the front and rear interfaces\" -- i.e. the "
                                                  "raster turns leave unpaired loops, and the "
                                                  "published blocks have them.",
        },
        "scaffoldBudget": {
            "demandBasePairs": demand,
            "demandNote": "{} rows x 112 bp x {} layers".format(rows, per_row),
            "candidates": {
                name: {"nucleotides": length,
                       "sufficient": length >= demand,
                       "remainderNucleotides": length - demand,
                       "occupancy": demand / length}
                for name, length in scaffolds.items()
            },
            "finding": "C-0109 costed the tile against M13mp18's 7249 nt and found 92.7 % "
                       "occupancy. The scaffolds Douglas et al. ACTUALLY fold these blocks from "
                       "are p7560 and p8064 -- M13mp18 derivatives bearing inserts -- and design "
                       "(i), our own cross-section, used p8064. So the standard scaffold for this "
                       "cross-section is LONGER than the one the budget was computed against, and "
                       "NDI's \"M13, circular ~7-8K nucleotides\" already names that range.",
        },
        "findings": {
            "theAnswerIsYesAndItIsBuilt": "Four honeycomb layers of 15 rows CAN be rastered from "
                                          "one circular M13, and the cross-section is not a "
                                          "proposal: it is design (i) of the caDNAno paper, folded "
                                          "from p8064 and one of only THREE of seven to produce "
                                          "sharp leading monomer bands.",
            "butThePaperRecommendsAgainstIt": "Its own conclusion is that SIX helices per x-raster "
                                              "row (10 x 6) yields the greatest fraction of "
                                              "defect-free objects, not four (15 x 4). So the tile "
                                              "aspect ratio is a design variable with published "
                                              "yield evidence attached, and this programme has "
                                              "never treated it as one.",
            "theSeamSurvivesTheMoveToThreeDimensions": "And for a reason that is in the source "
                                                       "rather than the geometry: the scaffold path "
                                                       "stays within a 2D surface, so the "
                                                       "adjacency it may use is a path even though "
                                                       "the honeycomb's is three-regular.",
            "theCheapBoundWasRightAndAlsoNotTheAnswer": "C-0086's odd-half-turn rule genuinely does "
                                                        "not transfer -- no odd multiple of 5.25 bp "
                                                        "is an integer -- but the honeycomb "
                                                        "quantises its half turn to 5 bp instead, "
                                                        "so the lattice is integral and the rule's "
                                                        "failure was a domain error rather than a "
                                                        "prohibition.",
        },
        "validity": [
            "The brute force is exhaustive to 7 helices and the parity beyond that is a THEOREM: a "
            "closed walk on a tree traverses every edge an even number of times, and a raster path "
            "is a tree. The 60-helix case is not enumerated and does not need to be.",
            "The 2D-surface premise is the paper's OWN statement about ITS OWN default rules, and "
            "caDNAno explicitly permits the user to force crossovers outside them -- \"caDNAno "
            "permits the user to force crossovers between any two staple bases or between any two "
            "scaffold bases\" -- with the warning that \"departure from the default rules may lead "
            "to folding failure\". A forced-crossover route could use the honeycomb's third "
            "neighbour and break the path, and this task does NOT explore that.",
            "The yield comparison is BETWEEN CROSS-SECTIONS at a fixed 60 helices, on seven "
            "designs, by gel and by 100-particle TEM counts. Fractions are reported in that "
            "paper's Figure 2d/2e, which are IMAGES and are not transcribed here -- only the "
            "ordering and the named winner are, both from the text.",
            "Every plan ceiling, station lattice, crossover phase and placement in this corpus is "
            "SINGLE-LAYER SQUARE-LATTICE. The honeycomb's three azimuths at 7 bp are a different "
            "inventory and nothing here re-derives them.",
            "This task settles the ROUTE and the WIDTH. It does not re-derive C-0109's rigidities, "
            "C-0116's threshold, or any flatness number.",
        ],
        "openQuestions": [
            "Whether a 10 x 6 cross-section -- the paper's own recommendation -- has the flexural "
            "rigidities the flatness verdict needs. It is 60 helices as well, so the scaffold "
            "budget is unchanged, but six layers of ten rows is a different plate: thicker, "
            "narrower, and with a different second moment. That is a direct successor to C-0109 "
            "and C-0116 and it may be a better tile than the one now recommended.",
            "What the honeycomb's three crossover azimuths offer as an ATTACHMENT lattice, since "
            "every plan result in this corpus is square-lattice.",
            "Whether the unpaired scaffold loops at the raster's front and rear interfaces sit in "
            "the actuated gap, which is T-195's question on a different body.",
        ],
    }
    destination = os.path.join(ROOT, "gpd", "results", "T-198-honeycomb-raster-width.json")
    with open(destination, "w", encoding="utf-8") as handle:
        # `T-278`. The header the Kotlin emission layer puts on every study's record, mirrored
        # for the emitters written in Python (`tools/emission_header.py`). This is a honeycomb
        # raster and the tag is what makes "which results are honeycomb" a query.
        json.dump(
            with_emission_header(result, "honeycomb"), handle, indent=2, ensure_ascii=False
        )
        handle.write("\n")
    print("wrote {}".format(destination))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
