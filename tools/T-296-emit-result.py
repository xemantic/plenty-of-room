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
"""Emit `gpd/results/T-296-zero-loop-raster-turn.json`.

    tools/T-296-emit-result.py
    tools/T-296-emit-result.py --selftest

`T-296` asks whether a honeycomb raster turn is BUILT with zero unpaired nucleotides -- which is
what makes it a covalent tie, and therefore what `C-0175` section 9, `C-0180` section 4 and
`C-0190` all rest on.

WHY PYTHON AND NOT A KOTLIN STUDY. Every number here is a closed form, an integer, or a field read
back out of a committed result file. The only iterative step is an inverse Langevin by bisection,
which is twenty lines and is CROSS-CHECKED against `T-230`'s own committed records -- so the
mirror is held to the original rather than trusted. A Kotlin study would need a `ResultInputs`
handle and therefore an edit to a shared main source a sibling agent owns this iteration, for no
numeric gain. `CLAUDE.md`: a cross-language mirror of a numeric rule is a numeric claim and needs
its own tests.

NOTHING IS TRANSCRIBED. The three geometric constants are parsed out of the Kotlin sources that
declare them, so a change there fails this emitter instead of ageing quietly out of it.
"""

import argparse
import importlib.util
import json
import math
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
DESIGNS = os.path.join(ROOT, "gpd", "designs")
DESTINATION = os.path.join(RESULTS, "T-296-zero-loop-raster-turn.json")

GEN1_TILE = os.path.join(ROOT, "src", "main", "kotlin", "structure", "Gen1Tile.kt")
MEASURED_BACKBONE = os.path.join(ROOT, "src", "main", "kotlin", "anchoring", "MeasuredBackbone.kt")

THERMAL_ENERGY = 4.141947          # pN*nm at 300 K, the project's locked constant
RISE = 0.34                        # nm per base pair
HELICES = 60                       # both 60-helix cross-sections
BUILT_LOOP = 28                    # nt per helix, the caDNAno blocks' own allotment
NOMINAL_WIDTH = 40.0               # nm, section 3's tile footprint

#: The scaffolds this programme and the built blocks use.
SCAFFOLDS = (
    ("M13mp18", 7249),
    ("p7560 -- the 60-helix designs, including (i) 15 x 4 and (ii) 10 x 6", 7560),
    ("p8064 -- the 64-helix designs", 8064),
)

#: `CLAUDE.md`'s zero-force ssDNA Kuhn bracket and the INEXTENSIBLE contour that travels with it.
KUHN_LENGTHS = (2.10, 2.84)
CONTOUR_PER_NUCLEOTIDE = (0.65, 0.70)


def _load_rounding():
    path = os.path.join(ROOT, "tools", "T-278-rounding-simulation.py")
    spec = importlib.util.spec_from_file_location("t296rounding", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def kotlin_constant(path, name):
    """The `Double` value of `const val <name>` in a Kotlin source, parsed rather than copied."""
    source = open(path).read()
    match = re.search(
        r"const\s+val\s+%s\s*:\s*Double\s*=\s*([-0-9.eE+]+)" % re.escape(name), source)
    if match is None:
        raise LookupError("no `const val %s: Double` in %s" % (name, path))
    return float(match.group(1))


# --------------------------------------------------------------- the reach bound, re-derived

def turn_phosphate_span(interhelical, phosphate_radius, exit_degrees, entry_degrees):
    """`HoneycombTurnLoop.turnPhosphateSpan`, mirrored, with no axial offset."""
    a = math.radians(exit_degrees)
    b = math.radians(entry_degrees)
    x = interhelical + phosphate_radius * math.cos(b) - phosphate_radius * math.cos(a)
    y = phosphate_radius * math.sin(b) - phosphate_radius * math.sin(a)
    return math.sqrt(x * x + y * y)


def minimum_unpaired_nucleotides(span, step):
    """The smallest `n` with `(n + 1) * step >= span`. Below it the turn closes at NO conformation."""
    n = 0
    while (n + 1) * step < span:
        n += 1
        if n > 10000:
            raise ValueError("span %r is beyond any buildable loop" % span)
    return n


# ------------------------------------------------------------- the freely jointed chain, mirrored

def langevin(u):
    """`L(u) = coth(u) - 1/u`, guarded at both ends exactly as the Kotlin is."""
    if u < 0.0:
        raise ValueError("the Langevin argument must not be negative, was: %r" % u)
    if u < 0.5:
        u2 = u * u
        return u / 3.0 - u2 * u / 45.0 + 2.0 * u2 * u2 * u / 945.0 - u2 * u2 * u2 * u / 4725.0
    return 1.0 / math.tanh(u) - 1.0 / u


def log_sinh_over_u(u):
    """`ln(sinh(u)/u)`, guarded: `sinh` overflows above `u ~ 710`."""
    if u < 1e-8:
        return u * u / 6.0
    if u > 20.0:
        return u - math.log(2.0 * u) + math.log(1.0 - math.exp(-2.0 * u))
    return math.log(math.sinh(u) / u)


def inverse_langevin(x, iterations=200):
    """Bisection on a strictly increasing function -- for bisection the bracket width IS the error."""
    if not (0.0 <= x < 1.0):
        raise ValueError("the extension ratio must be in [0, 1), was: %r" % x)
    if x == 0.0:
        return 0.0
    low, high = 0.0, 1.0
    while langevin(high) < x:
        high *= 2.0
        if high > 1e18:
            raise ValueError("the extension ratio %r is not reachable" % x)
    for _ in range(iterations):
        mid = 0.5 * (low + high)
        if langevin(mid) < x:
            low = mid
        else:
            high = mid
    return 0.5 * (low + high)


def turn_loop_state(span, nucleotides, kuhn, contour_per_nucleotide):
    contour = nucleotides * contour_per_nucleotide
    if span >= contour:
        return None
    x = span / contour
    u = inverse_langevin(x)
    return {
        "unpairedNucleotidesPerTurn": nucleotides,
        "kuhnLength": kuhn,
        "contourPerNucleotide": contour_per_nucleotide,
        "spanNm": span,
        "contourLength": contour,
        "extensionRatio": x,
        "tensionPicoNewton": THERMAL_ENERGY * u / kuhn,
        "freeEnergyThermal": contour / kuhn * (x * u - log_sinh_over_u(u)),
    }


# ------------------------------------------------------------------------- the `.sc` designs

def loopout_census(path):
    design = json.load(open(path))
    scaffolds = [s for s in design["strands"] if s.get("is_scaffold")]
    census = {
        "path": os.path.relpath(path, ROOT),
        "grid": design.get("grid"),
        "helices": len(design.get("helices", [])),
        "strands": len(design["strands"]),
        "scaffoldStrands": len(scaffolds),
    }
    domains = 0
    loopouts = 0
    for strand in scaffolds:
        for part in strand["domains"]:
            if "loopout" in part:
                loopouts += 1
            else:
                domains += 1
    census["scaffoldDomains"] = domains
    census["scaffoldLoopouts"] = loopouts
    return census


def reference_rectangle_census():
    """The FIELD's own generator, not this repository's -- `CLAUDE.md`'s calibration rule."""
    try:
        import scadnano as sc
        from scadnano import origami_rectangle as orect
    except ImportError:
        return {"available": False}
    design = orect.create(num_helices=8, num_cols=16, assign_seq=False)
    scaffolds = [s for s in design.strands if s.is_scaffold]
    loopouts = sum(
        1 for s in scaffolds for part in s.domains if isinstance(part, sc.Loopout))
    domains = sum(
        1 for s in scaffolds for part in s.domains if isinstance(part, sc.Domain))
    return {
        "available": True,
        "generator": "scadnano.origami_rectangle.create(num_helices=8, num_cols=16)",
        "scaffoldStrands": len(scaffolds),
        "scaffoldDomains": domains,
        "scaffoldLoopouts": loopouts,
    }


# ----------------------------------------------------------------------- committed result files

def result(tag):
    for name in os.listdir(RESULTS):
        if name.startswith(tag + "-") and name.endswith(".json"):
            return json.load(open(os.path.join(RESULTS, name)))
    raise LookupError("no committed result file for %s" % tag)


def leaf(document, path):
    node = document
    for part in path.strip("/").split("/"):
        node = node[int(part)] if isinstance(node, list) else node[part]
    return node


def numeric_leaf(document, path):
    """`leaf`, coerced to a float.

    `CLAUDE.md` records the census: 49 committed result files render their parameter block as JSON
    NUMBERS and 59 as STRINGS, a per-study convention nobody wrote down. `T-230` and `T-245` are
    both on the string side, so a reproduction that reads one back has to coerce -- and coercing
    is honest here precisely because the comparison is asserted at the file's own EMISSION
    precision, which is what the string carries.
    """
    return float(leaf(document, path))


def prose(value, digits=9):
    """`value` rounded for a SENTENCE.

    `CLAUDE.md`: the serialisation boundary dispatches on the JSON type and passes strings
    through, so a number interpolated into a `findings` sentence carries `repr`'s full round-trip
    precision into a file that declares nine significant digits. The cure is necessarily per call
    site, and this is the call site.
    """
    rounding = _load_rounding()
    return repr(rounding.round_for_result(float(value), digits, 0.0))


def departure(here, there):
    """Absolute where the expectation is exactly zero -- `CLAUDE.md`, the fired-falsifier entry."""
    if there == 0.0:
        return abs(here - there)
    return abs(here - there) / abs(there)


# ------------------------------------------------------------------------------------ the build

def build():
    interhelical = kotlin_constant(GEN1_TILE, "INTERHELICAL_HONEYCOMB")
    phosphate_radius = kotlin_constant(
        MEASURED_BACKBONE, "B_SOUTH_POPULATION_PHOSPHATE_RADIUS")
    step_south = kotlin_constant(MEASURED_BACKBONE, "STEP_SOUTH")
    step_south_sd = kotlin_constant(MEASURED_BACKBONE, "STEP_SOUTH_SD")
    step_south_p99 = kotlin_constant(MEASURED_BACKBONE, "STEP_SOUTH_P99")
    step_north = kotlin_constant(MEASURED_BACKBONE, "STEP_NORTH")

    aligned = turn_phosphate_span(interhelical, phosphate_radius, 0.0, 180.0)
    worst = turn_phosphate_span(interhelical, phosphate_radius, 180.0, 0.0)
    aligned_sigma = (aligned - step_south) / step_south_sd
    reach_worst = minimum_unpaired_nucleotides(worst, step_south)

    t230 = result("T-230")
    t245 = result("T-245")
    t254 = result("T-254")
    t279 = result("T-279")
    t291 = result("T-291")

    paired = int(numeric_leaf(t245, "/closingFamily/19/scaffoldNucleotides"))
    spare_m13 = numeric_leaf(t245, "/verdict/scaffoldSpareOnM13")

    # --- the budget on the DRAWABLE raster, which post-dates C-0147's uniform-row reading
    budgets = []
    for name, length in SCAFFOLDS:
        needed = paired + HELICES * BUILT_LOOP
        affordable = (length - paired) // HELICES
        budgets.append({
            "scaffold": name,
            "nucleotides": length,
            "raster": "102 / 109 (C-0151, the drawable two-length raster)",
            "pairedNucleotides": paired,
            "atTheBuiltAllowanceRequired": needed,
            "atTheBuiltAllowanceSpare": length - needed,
            "atTheBuiltAllowanceFits": length >= needed,
            "largestAffordableLoopPerTurn": int(affordable),
        })

    # --- route B on the SAME scaffolds: a uniform row, whose length the loop buys down
    route_b = []
    for name, length in SCAFFOLDS:
        for loop in (BUILT_LOOP, reach_worst):
            row = length // HELICES - loop
            route_b.append({
                "scaffold": name,
                "nucleotides": length,
                "loopPerTurn": loop,
                "loopProvenance": ("the caDNAno blocks' own 126 = 98 + 28 allotment, READ DIRECTLY"
                                   if loop == BUILT_LOOP
                                   else "this task's re-derived reach bound at the worst azimuth"),
                "maximumUniformRowBasePairs": int(row),
                "rowSpanNm": row * RISE,
                "departureFromNominalPercent": 100.0 * (row * RISE - NOMINAL_WIDTH) / NOMINAL_WIDTH,
            })

    # --- what each affordable loop COSTS, at the worst azimuth, over the whole convention bracket
    loops = []
    interesting = sorted({b["largestAffordableLoopPerTurn"] for b in budgets} | {BUILT_LOOP})
    for nucleotides in interesting:
        for kuhn in KUHN_LENGTHS:
            for contour in CONTOUR_PER_NUCLEOTIDE:
                state = turn_loop_state(worst, nucleotides, kuhn, contour)
                if state is not None:
                    state["spanCase"] = "worst azimuth"
                    loops.append(state)

    def band(nucleotides, key):
        values = [r[key] for r in loops if r["unpairedNucleotidesPerTurn"] == nucleotides]
        return min(values), max(values)

    # --- the conditionality, read out of the committed files rather than restated
    untied = leaf(t254, "/stiffness/2/freeDishingWithoutTies")
    tied = leaf(t254, "/stiffness/2/freeDishingWithTies")
    ratio = leaf(t254, "/stiffness/2/ratio")
    ceiling = leaf(t254, "/ceilings/2/allTurnsCeilingOverStroke")

    conditional = [
        {
            "claim": "C-0175 section 9 -- the 59 turn ties",
            "onRouteA": ("the recommended block dishes %s of the stroke with the ties against %s "
                         "without them, a ratio of %s"
                         % (prose(tied), prose(untied), prose(ratio))),
            "onRouteB": ("there are no ties: the free tile is %s, which is C-0167's own untied "
                         "reference" % prose(untied)),
            "whatSurvives": "the untied column, which is C-0167's own and was never in doubt",
            "whatDoesNot": "the 1.12x, the 435 + 59 element split, and every number taken on it",
        },
        {
            "claim": "C-0175 section 8 / CH-0228 -- the allowed 8.57142857 degree prestrain",
            "onRouteA": ("a triangle-inequality ceiling of %s of the stroke over all 59 turns"
                         % prose(ceiling)),
            "onRouteB": "no covalent tie carries an azimuth, so the load does not exist",
            "whatSurvives": "nothing of the load; the free tile stands at the untied value",
            "whatDoesNot": "the ceiling, the 59-site census and the three swept sign assignments",
        },
        {
            "claim": "C-0180 section 4 -- the coupled recovery",
            "onRouteA": "%s flat at the 90th percentile" % leaf(t279, "/verdict/tiedCellsFlatAtP90"),
            "onRouteB": "%s" % leaf(t279, "/verdict/untiedCellsFlatAtP90"),
            "whatSurvives": "C-0167's 0 of 64, which is what the untied lattice already said",
            "whatDoesNot": "the two recovered cells, %s and %s"
                           % (leaf(t279, "/findings/2"), leaf(t279, "/verdict/medianRatioRange")),
        },
        {
            "claim": "C-0190 -- the per-beam twist that replaces the roll",
            "onRouteA": "%s; free tile %s of the stroke at f = 0.30, phase +1"
                        % (leaf(t291, "/verdict/flatAtBothSigns"), prose(0.296735462)),
            "onRouteB": ("a tether demands NO azimuth at either end, so there is no roll to be "
                         "common-mode and no twist to replace it"),
            "whatSurvives": "the geometry -- CH-0240's antipodality and the u* = 1.37990892 stationary point",
            "whatDoesNot": "the 17.1428571 degree demand, the 8.31368089 kT ceiling and the 0 of 64 grading",
        },
        {
            "claim": "C-0151 / C-0148 -- the drawable raster and its b0",
            "onRouteA": "102 / 109 is selected BECAUSE it closes on caDNAno's +-5 bp rule",
            "onRouteB": ("a turn through unpaired scaffold is not a crossover, so no residue "
                         "condition binds it and every row length is admissible"),
            "whatSurvives": "the closure arithmetic, as a statement about route A",
            "whatDoesNot": "its status as a SELECTION -- on route B there is nothing to select against",
        },
    ]

    reference_rectangle = reference_rectangle_census()
    designs = [
        loopout_census(os.path.join(DESIGNS, "gen1-block-honeycomb-10x6-102-109.sc")),
        loopout_census(os.path.join(DESIGNS, "gen1-sheet-square-15x112.sc")),
        loopout_census(os.path.join(DESIGNS, "third-party",
                                    "scadnano-origami-rectangle-16x8.sc")),
    ]

    reproductions = [
        {"what": "C-0147's aligned turn span, the n = 0 case that IS a scaffold crossover",
         "here": aligned, "there": numeric_leaf(t230, "/parameters/crossoverSpanNm"),
         "relativeDeparture": departure(aligned, numeric_leaf(t230, "/parameters/crossoverSpanNm")),
         "source": "gpd/results/T-230-honeycomb-turn-loop-slack.json"},
        {"what": "its distance from the MEASURED C2'-endo phosphodiester step, in sigma",
         "here": aligned_sigma, "there": numeric_leaf(t230, "/parameters/crossoverSpanSigma"),
         "relativeDeparture": departure(aligned_sigma, numeric_leaf(t230, "/parameters/crossoverSpanSigma")),
         "source": "gpd/results/T-230-honeycomb-turn-loop-slack.json"},
        {"what": "the worst-azimuth span, d + 2 r_P",
         "here": worst, "there": numeric_leaf(t230, "/parameters/worstAzimuthSpanNm"),
         "relativeDeparture": departure(worst, numeric_leaf(t230, "/parameters/worstAzimuthSpanNm")),
         "source": "gpd/results/T-230-honeycomb-turn-loop-slack.json"},
        {"what": "the reach bound at the worst azimuth, in nucleotides",
         "here": float(reach_worst),
         "there": numeric_leaf(t230, "/parameters/reachBoundWorstAzimuthNucleotides"),
         "relativeDeparture": departure(
             float(reach_worst),
             numeric_leaf(t230, "/parameters/reachBoundWorstAzimuthNucleotides")),
         "source": "gpd/results/T-230-honeycomb-turn-loop-slack.json"},
        {"what": "the drawable raster's paired scaffold on 10 x 6",
         "here": float(paired), "there": 6330.0,
         "relativeDeparture": departure(float(paired), 6330.0),
         "source": "gpd/results/T-245-closing-raster-selection.json"},
        {"what": "its spare on M13mp18",
         "here": float(spare_m13), "there": float(7249 - paired),
         "relativeDeparture": departure(float(spare_m13), float(7249 - paired)),
         "source": "gpd/results/T-245-closing-raster-selection.json"},
        {"what": "C-0151's own 15 nt per helix afforded by M13 at 102 / 109",
         "here": float(budgets[0]["largestAffordableLoopPerTurn"]), "there": 15.0,
         "relativeDeparture": departure(
             float(budgets[0]["largestAffordableLoopPerTurn"]), 15.0),
         "source": "gpd/results/T-245-closing-raster-selection.json, findings/THE_BUDGETS"},
        {"what": "C-0140's widest uniform row on M13 at the built allowance",
         "here": float([r for r in route_b
                        if r["scaffold"] == "M13mp18" and r["loopPerTurn"] == BUILT_LOOP
                        ][0]["maximumUniformRowBasePairs"]),
         "there": 92.0,
         "relativeDeparture": departure(
             float([r for r in route_b
                    if r["scaffold"] == "M13mp18" and r["loopPerTurn"] == BUILT_LOOP
                    ][0]["maximumUniformRowBasePairs"]), 92.0),
         "source": "gpd/results/T-230-honeycomb-turn-loop-slack.json"},
        {"what": "the built turn's tension at the worst azimuth, the tight end of the bracket",
         "here": band(BUILT_LOOP, "tensionPicoNewton")[1], "there": 1.46667915,
         "relativeDeparture": departure(band(BUILT_LOOP, "tensionPicoNewton")[1], 1.46667915),
         "source": "gpd/results/T-230-honeycomb-turn-loop-slack.json, loops"},
        {"what": "the built turn's stored free energy, the loose end of the bracket",
         "here": band(BUILT_LOOP, "freeEnergyThermal")[0], "there": 0.518481856,
         "relativeDeparture": departure(band(BUILT_LOOP, "freeEnergyThermal")[0], 0.518481856),
         "source": "gpd/results/T-230-honeycomb-turn-loop-slack.json, loops"},
        {"what": "the caDNAno blocks' own accounting, 98 + 28 against 126",
         "here": 98.0 + 28.0, "there": 126.0,
         "relativeDeparture": departure(98.0 + 28.0, 126.0),
         "source": "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml"},
        {"what": "60 helices at that allotment against p7560, and 64 against p8064",
         "here": float(60 * 126), "there": 7560.0,
         "relativeDeparture": departure(float(60 * 126), 7560.0),
         "source": "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml"},
    ]

    m13 = budgets[0]
    p8064 = budgets[2]

    # What route B's turn set COSTS the block, against the two comparands C-0190 already uses.
    TURNS = 59
    twist_ceiling = 8.31368089          # C-0190, the block's rigid-duplex twist ceiling, k_BT
    crossover_column = 7.99969697       # C-0079, one crossover column of the host sheet, k_BT
    block_energy = []
    for nucleotides in interesting:
        low, high = band(nucleotides, "freeEnergyThermal")
        block_energy.append({
            "unpairedNucleotidesPerTurn": nucleotides,
            "turns": TURNS,
            "perTurnThermalLow": low,
            "perTurnThermalHigh": high,
            "overTheBlockThermalLow": TURNS * low,
            "overTheBlockThermalHigh": TURNS * high,
            "againstTheTwistCeilingLow": TURNS * low / twist_ceiling,
            "againstTheTwistCeilingHigh": TURNS * high / twist_ceiling,
            "inHostSheetCrossoverColumnsLow": TURNS * low / crossover_column,
            "inHostSheetCrossoverColumnsHigh": TURNS * high / crossover_column,
            "tensionPicoNewtonLow": band(nucleotides, "tensionPicoNewton")[0],
            "tensionPicoNewtonHigh": band(nucleotides, "tensionPicoNewton")[1],
        })

    document = {
        "task": "T-296",
        "leaf": "A8.2",
        "title": ("Whether a honeycomb raster turn is BUILT with zero unpaired nucleotides, and "
                  "what the tie set is conditional on if it is not"),
        "verificationType": (
            "literature (the built precedent's own scaffold accounting and the loops' own stated "
            "purpose, both READ DIRECTLY from primary sources already in gpd/data/, plus a "
            "recorded existence sweep) + logical (a covalent reach bound on the MEASURED "
            "backbone, exact integer scaffold arithmetic and an exact freely-jointed-chain law) + "
            "in-silico (parsing this repository's own committed .sc designs and the field's own "
            "reference generator)"),
        "maturity": (
            "TRL 1-3 -- model-consistent and traceable, NOTHING HERE IS MEASURED except the "
            "constants and the published designs. The verdict on drawability is a DESIGN "
            "statement; the verdict on folding is an EXISTENCE statement about the literature, "
            "and no folding experiment is reported here or anywhere for this motif on this lattice"),
        "units": {
            "length": "nm",
            "force": "pN",
            "energy": "pN*nm and k_BT",
            "count": "nucleotides, base pairs, designs, queries",
        },
        "conventions": [
            "rise 0.34 nm/bp; k_BT = 4.141947 pN*nm at 300 K",
            ("a turn joins the END of one helix to the END of the next along the raster path; a "
             "helix carries a front and a rear unpaired fragment of L/2, so a TURN's slack is the "
             "PER-HELIX allotment L -- which is how the built blocks' own accounting is written"),
            ("route A = every turn a scaffold crossover, zero unpaired slack; route B = every turn "
             "an unpaired loop (C-0140's and C-0147's names, kept)"),
            ("the ssDNA Kuhn length is the ZERO-FORCE end of CLAUDE.md's 2x method-systematic "
             "bracket, 2.10-2.84 nm, and the contour per nucleotide that travels with it is the "
             "INEXTENSIBLE 0.65-0.70 nm/nt; the force-spectroscopy Kuhn and the extensible contour "
             "are never mixed with them"),
        ],
        "parameters": {
            "interhelicalDistanceNm": interhelical,
            "phosphateRadiusNm": phosphate_radius,
            "measuredStepSouthNm": step_south,
            "measuredStepSouthSdNm": step_south_sd,
            "measuredStepSouthP99Nm": step_south_p99,
            "measuredStepNorthNm": step_north,
            "helices": HELICES,
            "crossSection": "10 x 6",
            "raster": "102 / 109 (C-0151, drawable)",
            "builtLoopNucleotidesPerHelix": BUILT_LOOP,
            "nominalWidthNm": NOMINAL_WIDTH,
            "hostSheetCrossoverColumnThermal": 7.99969697,
            "routeATwistCeilingThermal": 8.31368089,
            "ssdnaContourAtTheBuiltAllowanceNm": "18.2 to 19.6",
            "selfTestFieldsComparedAgainstT230": 252,
            "constantsParsedFrom": [
                "src/main/kotlin/structure/Gen1Tile.kt",
                "src/main/kotlin/anchoring/MeasuredBackbone.kt",
            ],
        },
        "sources": [
            {"what": ("the honeycomb blocks' per-helix scaffold accounting, its SCOPE over all "
                      "seven cross-sections, and the two scaffolds"),
             "citation": ("Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, "
                          "Nucleic Acids Research 37:5001-5006 (2009) -- the caDNAno paper"),
             "path": "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml",
             "readAs": "read directly"},
            {"what": "the unpaired loops' own STATED PURPOSE, and the alternative to them",
             "citation": ("Ke, Douglas, Liu, Sharma, Cheng, Liu, Yan & Shih, J. Am. Chem. Soc. "
                          "131:15903 (2009) -- multilayer DNA origami on a square lattice"),
             "path": "gpd/data/T-246-sources/PMC2821935.txt",
             "readAs": "read directly"},
            {"what": "the field's CURRENT anti-stacking remedy, and that it is on the STAPLES",
             "citation": "DNA Origami Design: A How-To Tutorial (2024)",
             "path": "gpd/data/T-151-sources/PMC11419732-fullTextXML.xml",
             "readAs": "read directly"},
            {"what": "the 28 nt allowance recurring on a second, later honeycomb object",
             "citation": "Ke, Bellot, Voigt, Fradkov & Shih, Chem. Sci. 3:2587 (2012)",
             "path": "gpd/data/T-246-sources/PMC3957201.txt",
             "readAs": "read directly"},
            {"what": ("the honeycomb design rule itself -- the +-5 bp scaffold-crossover rule, "
                      "the loops' own stated purpose, and that the loops link HELIX ENDS"),
             "citation": ("Douglas, Dietz, Liedl, Hogberg, Graf & Shih, Nature 459:414-418 (2009) "
                          "-- the honeycomb block paper"),
             "path": "gpd/data/T-296-sources/PMC2688462-douglas2009.txt",
             "readAs": "read directly (main text and Methods; the strand diagrams are in "
                       "Supplementary Note S2)"},
            {"what": ("the built 10 x 6 block's OWN strand diagram -- where the scaffold turns, "
                      "where the staples stop, and therefore where the duplex ends"),
             "citation": ("Douglas et al., Nature 459:414 (2009), Supplementary Figure S4, "
                          "`monolith design schematic` -- whose cross-section inset is 10 rows of "
                          "6 helices, i.e. THE cross-section this programme recommends, and which "
                          "the caDNAno paper names as `10 x 6 (analyzed independently in ref. 14)`"),
             "path": "gpd/data/T-296-sources/douglas2009-SI.pdf (page 4); rendered at "
                     "gpd/data/T-296-sources/T-296-douglas-figS4-left-rim.png and "
                     "T-296-douglas-figS4-right-rim.png",
             "readAs": "read directly (rendered from the PDF and read off the axis ticks; the "
                       "PDF has no usable text layer, so the numbers are read from the figure)"},
            {"what": "the existence sweep, 30 queries in 9 named families",
             "citation": "EuropePMC REST search",
             "path": "gpd/data/T-296-sources/europepmc-queries.json",
             "readAs": "read directly (titles and abstracts); no full text fetched"},
        ],
        "quotations": {
            "the allotment, and its scope over all seven cross-sections": (
                "The shapes were folded either from a 7560-base scaffold into 60 parallel helices "
                "or from an 8064-base scaffold into 64 parallel helices to create "
                "number-of-rows versus number-of-helices-per-x-raster-row combinations of 15 x 4, "
                "10 x 6, 8 x 8, 6 x 10, 4 x 16, 3 x 20, 2 x 30. Each helix was allotted 126 bases "
                "of scaffold. Of those 126 bases, 98 were paired with complementary staples, and "
                "the remaining 28 bases were divided into front and rear unpaired loop fragments "
                "at the ends of each helix."),
            "the loops' own stated PURPOSE": (
                "Unpaired scaffold bases often are introduced at the ends of helices (as unpaired "
                "loops) to minimize undesired multimerization."),
            "and the alternative the same paragraph names": (
                "Alternatively, if a seam composed of scaffold crossovers is implemented on the "
                "inside of the structure, then a circular scaffold path can be accommodated "
                "without the need for the long unpaired loop."),
            "how much loop a multilayer origami actually spends": (
                "Target structures were designed so that 90-97% of the scaffold strand should be "
                "paired with staple strands. The remaining scaffold material was designed as "
                "unpaired loops at the ends of the helices."),
            "the honeycomb paper's own sentence, and it says OFTEN": (
                "Sometimes staple crossovers are removed at the edges of the shapes to allow "
                "adjustment of staple lengths to preferred values. Unpaired scaffold bases often "
                "are introduced at the ends of helices to minimize undesired multimerization, or "
                "else to accommodate later addition of connecting staple strands that mediate "
                "desired multimerization."),
            "what the built blocks put at a raster turn": (
                "Cylinders represent double helices, with loops of unpaired scaffold strand "
                "linking the ends of adjacent helices."),
            "and the rule that permits the alternative": (
                "Crossovers between adjacent scaffold helices are permitted at positions "
                "displaced upstream or downstream of the corresponding staple-crossover points by "
                "5 base pairs or a half-turn. The first steps in the design process are carving "
                "away duplex segments from the block to define the target shape, and then "
                "introducing scaffold crossovers at a subset of allowed positions so as to create "
                "a singular scaffold path that visits all remaining duplex segments."),
            "the field's current remedy, and it is on the STAPLES": (
                "the exposed cylinder ends at the edges of DNA nanostructures are prone to "
                "reversible, low-energy stacking ... it may lead to uncontrolled agglomeration. A "
                "tried-and-tested of way to prevent stacking is to place a 4+ base poly-T loop on "
                "the staples when they jump between helices at the cylinder ends."),
        },
        "cheapBound": {
            "statement": ("a turn of n unpaired nucleotides reaches (n + 1) x step, so a turn of "
                          "ZERO reaches exactly one step -- and the zero-loop turn IS a scaffold "
                          "crossover, whose span is d - 2 r_P"),
            "alignedSpanNm": aligned,
            "measuredStepNm": step_south,
            "sigmaFromTheMeasuredStep": aligned_sigma,
            "insideTheNinetyNinthPercentile": aligned < step_south_p99,
            "worstAzimuthSpanNm": worst,
            "reachBoundAtTheWorstAzimuth": reach_worst,
            "verdict": ("the zero-loop turn is REACHABLE on measured constants alone, at "
                        "+1.5 sigma of a 13 084-linkage crystallographic survey and inside its "
                        "99th percentile -- so nothing covalent refuses it, and the question is "
                        "entirely one of precedent"),
        },
        "designs": designs,
        "referenceGenerator": reference_rectangle,
        "builtPrecedent": {
            "lattice": "honeycomb",
            "designs": 7,
            "crossSections": "15 x 4, 10 x 6, 8 x 8, 6 x 10, 4 x 16, 3 x 20, 2 x 30",
            "allotmentPerHelix": 126,
            "pairedPerHelix": 98,
            "unpairedPerHelix": BUILT_LOOP,
            "pairedFraction": 98.0 / 126.0,
            "scopeIsAllSeven": True,
            "scopeEvidence": ("ONE sentence covers all seven -- the list of combinations and the "
                              "allotment are the same sentence pair, and 60 x 126 = 7560 and "
                              "64 x 126 = 8064 are both exact, so the allotment IS the scaffold "
                              "divided by the helix count"),
            "designsWithZeroLoopRasterTurns": 0,
            "comparisonPairedFractionSquareLatticeMultilayer": "0.90 to 0.97",
            "comparisonNote": ("the same laboratory, the same year, on the square lattice, built "
                               "multilayer cuboids at 90-97 % paired -- so the honeycomb blocks' "
                               "77.8 % is the SLACK EXTREME of the built multilayer family, not "
                               "its centre"),
        },
        "builtGeometry": {
            "source": "Douglas et al. 2009, Supplementary Figure S4, the monolith = the 10 x 6 block",
            "readAs": "read directly off the figure's own base-pair axis",
            "scaffoldOccupies": "14 to 140",
            "scaffoldNucleotidesPerHelix": 126,
            "stapledDuplexOccupies": "28 to 126",
            "duplexBasePairs": 98,
            "unpairedFlankPerHelixEnd": 14,
            "unpairedBetweenTwoDUPLEXEnds": 28,
            "duplexSpanNm": 98 * RISE,
            "unpairedFlankNm": 14 * RISE,
            "whereTheCOVALENTLINKSITS": (
                "at base 14 and base 140 -- the ENDS OF THE SCAFFOLD's occupancy, which is 14 bp "
                "OUTBOARD of the duplex end at each of the two helices it joins"),
            "whyThisIsNotACOVALENTTIE": (
                "the corpus's tie is a covalent element between two DUPLEX ends at s = +-L/2. In "
                "the built block the scaffold does turn without a topological loopout, and the "
                "two DUPLEXES it joins are 28 unpaired nucleotides apart -- 14 on each side -- so "
                "what stands between the two rim nodes is ssDNA, not a bond. C-0147's 28 nt "
                "tether is the right object and the figure confirms where it sits"),
            "andWhyTheBLOCKCANBEUNIFORM": (
                "an unpaired base has no azimuth, so the +-5 bp residue condition cannot bind a "
                "turn flanked by 14 unpaired nucleotides -- which is exactly how a block whose "
                "turn sense alternates is folded with all sixty helices at ONE length. The built "
                "design BUYS freedom from the residue condition and PAYS 28 nt of scaffold for it; "
                "route A pays no scaffold and must satisfy the condition, which is why C-0151's "
                "raster is two-length"),
            "reproducesThePapersAccounting": 98 + 28 == 126,
        },
        "motifCensus": [
            {"statement": "the covalent link a zero-loop turn needs is REACHABLE",
             "lattice": "honeycomb",
             "status": "DERIVED, on measured constants alone",
             "evidence": ("d - 2 r_P = %s nm against T-71's measured C2'-endo phosphodiester step, "
                          "+1.5 sigma and inside the 99th percentile of 13 084 crystallographic "
                          "linkages" % prose(aligned))},
            {"statement": "a scaffold crossover at a HELIX END, i.e. a raster turn with zero "
                          "unpaired scaffold",
             "lattice": "square, single layer",
             "status": "DEMONSTRATED, and it is the field's reference default",
             "evidence": ("scadnano's own origami_rectangle generator emits Rothemund's rectangle "
                          "with %d scaffold domains and ZERO loopouts, and Rothemund's rectangles "
                          "are the most-folded objects in the field"
                          % reference_rectangle["scaffoldDomains"]
                          if reference_rectangle.get("available") else "scadnano not importable")},
            {"statement": "a scaffold crossover between two adjacent HONEYCOMB helices",
             "lattice": "honeycomb",
             "status": "DEMONSTRATED, at INTERIOR positions",
             "evidence": ("the +-5 bp rule is the honeycomb design method's own primitive for "
                          "stepping the scaffold path between helices -- READ DIRECTLY from "
                          "Douglas et al. 2009 -- and the six shapes built by it fold")},
            {"statement": ("a scaffold turn at a rim with NO topological loopout, on the "
                           "honeycomb"),
             "lattice": "honeycomb",
             "status": "DEMONSTRATED -- and it does NOT make a covalent tie",
             "evidence": ("the built 10 x 6 block's own strand diagram turns the scaffold at base "
                          "14 and base 140 with no loopout drawn; what makes it a tether rather "
                          "than a tie is that the 14 bases nearest the turn on each helix carry "
                          "no staple, so the two DUPLEX ends are 28 nt apart")},
            {"statement": "the CONJUNCTION: a scaffold crossover at a DUPLEX end on the honeycomb "
                          "lattice, i.e. a zero-loop raster turn",
             "lattice": "honeycomb",
             "status": "NOT FOUND",
             "evidence": ("30 queries in 9 named families return 186 unique records, 7 of which "
                          "name the honeycomb lattice and none of which reports one; the seven "
                          "built honeycomb blocks all leave 14 unpaired bases at every helix end, "
                          "and their own paper says so in as many words AND draws it")},
        ],
        "scaffoldBudget": budgets,
        "routeB": route_b,
        "loops": loops,
        "blockEnergy": block_energy,
        "conditional": conditional,
        "verdict": {
            "isTheZeroLoopTurnREACHABLE": True,
            "isItDRAWABLE": True,
            "drawableEvidence": ("this repository's own committed gen1-block-honeycomb-10x6-102-109.sc "
                                 "carries one scaffold strand, 60 domains and ZERO loopouts, and "
                                 "C-0151 shows the 102 / 109 raster closes on caDNAno's +-5 bp "
                                 "rule with zero forced crossovers"),
            "isItBUILTOnTheHoneycombLattice": False,
            "publishedHoneycombDesignsWithZeroLoopRasterTurns": 0,
            "yieldEvidenceOnTheHoneycombLattice": "none, in either direction",
            "yieldEvidenceOnAnotherLattice": ("the single-layer square lattice, where the raster "
                                              "turn IS this motif: the field's own reference "
                                              "generator emits it with zero loopouts, and "
                                              "Rothemund's rectangles are the most-folded objects "
                                              "in the field"),
            "theLoopsStatedPurpose": "to minimize undesired multimerization -- NOT turn closure",
            "canRouteADischargeThatPurpose": ("yes, and on the staples: the field's current "
                                              "tutorial calls a 4+ base poly-T loop on the staples "
                                              "at the cylinder ends tried-and-tested, which costs "
                                              "zero scaffold"),
            "theTwoDesignsAreSeparatedByTheSCAFFOLD": True,
            "atTheBuiltAllowanceTheDrawableRasterNeeds": p8064["scaffold"],
            "atTheBuiltAllowanceM13Shortfall": -m13["atTheBuiltAllowanceSpare"],
            "atTheBuiltAllowanceP8064Spare": p8064["atTheBuiltAllowanceSpare"],
            "largestLoopM13AffordsOnTheDrawableRaster": m13["largestAffordableLoopPerTurn"],
            "answer": ("route A is reachable, drawable and lattice-legal, and it is NOT built: no "
                       "published honeycomb origami turns its raster with zero unpaired scaffold, "
                       "and the seven that exist all spend 28 nt per helix for a stated reason "
                       "that is aggregation and not closure. So C-0175 section 9, C-0180 "
                       "section 4 and C-0190 are claims about a DRAWABLE design that nobody has "
                       "folded, and each is conditional on exactly the numbers listed under "
                       "`conditional`"),
        },
        "existenceSweep": {},
        "reproductions": reproductions,
        "falsifiers": [
            {"id": "F1",
             "statement": "the n = 0 span falls OUTSIDE the measured step's 99th percentile, so "
                          "the zero-loop turn is not reachable and the answer is `they cannot`",
             "fired": aligned >= step_south_p99,
             "evidence": ("d - 2 r_P = %s nm against a measured C2'-endo step of %s +- %s and a "
                          "99th percentile of %s: +1.5 sigma and inside"
                          % (prose(aligned), prose(step_south), prose(step_south_sd),
                             prose(step_south_p99)))},
            {"id": "F2",
             "statement": "the committed honeycomb .sc carries a loopout, so the corpus's own "
                          "artifact is not the design it claims",
             "fired": designs[0]["scaffoldLoopouts"] > 0,
             "evidence": ("one scaffold strand, %d domains, %d loopouts"
                          % (designs[0]["scaffoldDomains"], designs[0]["scaffoldLoopouts"]))},
            {"id": "F3",
             "statement": "the 126 = 98 + 28 allotment is design-specific rather than covering all "
                          "seven cross-sections",
             "fired": False,
             "evidence": ("ONE sentence names all seven combinations and the allotment; 60 x 126 "
                          "= 7560 and 64 x 126 = 8064 are both exact, so the allotment IS the "
                          "scaffold divided by the helix count, and Figure S4 draws it")},
            {"id": "F4",
             "statement": "a published honeycomb origami with zero-loop raster turns is found, so "
                          "the answer is `they can, with yield evidence`",
             "declared": "OPEN",
             "fired": False,
             "evidence": ("30 queries in 9 named families, 186 unique records, 7 naming the "
                          "honeycomb lattice, 0 reporting a honeycomb raster turn between two "
                          "DUPLEX ends; and the one design whose strand diagram was read puts "
                          "14 unpaired bases at every helix end")},
            {"id": "F5",
             "statement": "the primary source names turn CLOSURE as the loops' purpose, so the "
                          "loops are load-bearing and the answer is `they cannot`",
             "fired": False,
             "evidence": ("Douglas et al. 2009 verbatim: `Unpaired scaffold bases OFTEN are "
                          "introduced at the ends of helices TO MINIMIZE UNDESIRED "
                          "MULTIMERIZATION, or else to accommodate later addition of connecting "
                          "staple strands that mediate desired multimerization` -- an aggregation "
                          "remedy and an assembly handle, and the word is `often`")},
            {"id": "F6",
             "statement": "at the built 28 nt allowance the drawable 102 / 109 raster fits "
                          "M13mp18, so the two designs are not separated by the scaffold",
             "declared": "OPEN",
             "fired": m13["atTheBuiltAllowanceFits"],
             "evidence": ("it needs %d nt and M13mp18 has 7249: short by %d. p7560 is short by "
                          "%d and p8064 has %d spare"
                          % (m13["atTheBuiltAllowanceRequired"],
                             -m13["atTheBuiltAllowanceSpare"],
                             -budgets[1]["atTheBuiltAllowanceSpare"],
                             p8064["atTheBuiltAllowanceSpare"]))},
            {"id": "F7",
             "statement": "removing the 59 ties reverses a flatness verdict at the FREE-TILE "
                          "level, so the conditionality is not merely quantitative",
             "declared": "OPEN",
             "fired": (untied >= 0.10) != (tied >= 0.10),
             "evidence": ("%s untied against %s tied, both inside T-5b's 0.10. At the COUPLED "
                          "level the verdict does move, and that is C-0180's own %s against %s "
                          "read backwards"
                          % (prose(untied), prose(tied),
                             leaf(t279, "/verdict/tiedCellsFlatAtP90"),
                             leaf(t279, "/verdict/untiedCellsFlatAtP90")))},
        ],
        "findings": {
            "THE_CHEAP_BOUND_DOES_NOT_REFUSE": (
                "d - 2 r_P = %s nm sits at +1.5 sigma of T-71's MEASURED C2'-endo phosphodiester "
                "step and inside its 99th percentile, so nothing covalent forbids a honeycomb "
                "raster turn with zero unpaired nucleotides. The question was never geometry."
                % prose(aligned)),
            "AND_NEITHER_DOES_THE_TOOL": (
                "this repository's own committed gen1-block-honeycomb-10x6-102-109.sc carries one "
                "scaffold strand, 60 domains and ZERO loopouts, and the field's own reference "
                "generator emits Rothemund's rectangle the same way. The motif is the reference "
                "default on the square lattice."),
            "WHAT_THE_BUILT_BLOCK_ACTUALLY_DOES": (
                "read off Figure S4 of the paper that built the 10 x 6 block: the scaffold "
                "occupies bases 14 to 140 of each helix -- 126, exactly the allotment -- and the "
                "staples occupy 28 to 126, exactly the 98. So the scaffold TURNS at the rim with "
                "no topological loopout, and the two DUPLEX ends it joins are 28 unpaired "
                "nucleotides apart, 14 on each side. The tether is real and the figure says where "
                "it sits: the covalent link is 14 bp = 4.76 nm OUTBOARD of the duplex end."),
            "AND_WHY_THAT_IS_A_PURCHASE_RATHER_THAN_A_NECESSITY": (
                "an unpaired base has no azimuth, so caDNAno's +-5 bp residue condition cannot "
                "bind a turn flanked by unpaired scaffold. The built block BUYS a uniform 98 bp "
                "row on a lattice that carries both turn senses, and PAYS 28 nt per helix for it. "
                "Route A pays no scaffold and must close the residue condition instead, which is "
                "exactly why C-0151's raster is two-length."),
            "THE_LOOPS_STATED_PURPOSE_IS_NOT_CLOSURE": (
                "Douglas et al. 2009 and Ke et al. 2009 carry the same sentence: unpaired "
                "scaffold bases OFTEN are introduced at the ends of helices TO MINIMIZE UNDESIRED "
                "MULTIMERIZATION. That is an aggregation remedy, and the field's current tutorial "
                "calls a 4+ base poly-T loop ON THE STAPLES at the cylinder ends tried-and-tested "
                "-- a remedy that costs zero scaffold. So route A does not have to leave the "
                "purpose undischarged."),
            "AND_28_nt_IS_THE_SLACK_EXTREME_OF_THE_BUILT_FAMILY": (
                "the honeycomb blocks are 98/126 = 77.8 %% paired; the same laboratory in the same "
                "year built multilayer square-lattice cuboids at 90-97 %% paired. 28 nt is not the "
                "centre of the built multilayer practice, it is its slack end."),
            "THE_TWO_DESIGNS_ARE_SEPARATED_BY_THE_SCAFFOLD": (
                "on C-0151's drawable 102 / 109 raster the paired total is %d nt. At the built "
                "28 nt allowance the block needs %d and M13mp18 is short by %d, p7560 by %d, and "
                "p8064 has %d spare -- so the built allowance on the recommended raster is a "
                "p8064 design and not an M13 one. Inverted, M13 affords %d nt per turn, p7560 %d "
                "and p8064 exactly the built %d."
                % (paired, m13["atTheBuiltAllowanceRequired"], -m13["atTheBuiltAllowanceSpare"],
                   -budgets[1]["atTheBuiltAllowanceSpare"], p8064["atTheBuiltAllowanceSpare"],
                   m13["largestAffordableLoopPerTurn"],
                   budgets[1]["largestAffordableLoopPerTurn"],
                   p8064["largestAffordableLoopPerTurn"])),
            "AND_A_TETHER_IS_NOT_FREE_EITHER": (
                "at M13's %d nt affordance the turn carries %s-%s pN and stores %s-%s k_BT, so "
                "the 59 turns store %s-%s k_BT over the block -- %s to %s times C-0190's whole "
                "rigid-duplex twist ceiling for route A, and %s to %s crossover columns of the "
                "host sheet C-0079 measures. Route B's turn set is the more expensive of the two "
                "in stored energy, on this scaffold."
                % ((m13["largestAffordableLoopPerTurn"],)
                   + tuple(prose(block_energy[0][key]) for key in (
                       "tensionPicoNewtonLow", "tensionPicoNewtonHigh",
                       "perTurnThermalLow", "perTurnThermalHigh",
                       "overTheBlockThermalLow", "overTheBlockThermalHigh",
                       "againstTheTwistCeilingLow", "againstTheTwistCeilingHigh",
                       "inHostSheetCrossoverColumnsLow",
                       "inHostSheetCrossoverColumnsHigh")))),
            "THE_ANSWER": (
                "route A is reachable, drawable and lattice-legal, and NOBODY HAS FOLDED IT ON "
                "THE HONEYCOMB. The built precedent is unanimous the other way at 7 of 7 "
                "cross-sections, for a stated reason that is aggregation and not closure. So "
                "C-0175 section 9, C-0180 section 4 and C-0190 are correct about a design that "
                "has not been demonstrated, and each is conditional on exactly what `conditional` "
                "lists."),
        },
        "validity": [
            ("TRL 1-3. The drawability verdict is a statement about a DESIGN FILE and a LATTICE "
             "RULE; the folding verdict is a statement about the LITERATURE. Neither is a folding "
             "experiment, and this repository cannot run one."),
            ("A negative existence result is only as strong as its query set. The 30 query strings "
             "are retained in gpd/data/T-296-sources/query.py and one paper naming a "
             "honeycomb-lattice origami whose raster turns carry no unpaired scaffold refutes it."),
            ("The reach bound reads the crossover span as d - 2 r_P, the two backbones antipodal "
             "on the line of centres. That is C-0147's convention and it is a BRACKET END: the "
             "measured interhelical distance is a Bragg lattice constant, and CLAUDE.md records "
             "that the LOCAL separation at a crossover is smaller. Reading it the other way makes "
             "the span shorter and the turn easier, so the verdict has a known sign."),
            ("The scaffold budget assumes the loop is spent UNIFORMLY, L/2 at each helix end, "
             "which is how the built blocks spend theirs. A design that pays only at the turns "
             "that need it would afford more."),
            ("Nothing here re-opens the raster, the cross-section, the placement search, the "
             "distribution rule or any number of C-0175, C-0180 or C-0190 -- every one of those "
             "is correct about route A. What is established is which design they are about."),
        ],
        "openQuestions": [
            ("Whether route B's tile is gradable at all at section 3's footprint. At the built "
             "allowance a uniform honeycomb row is 92 bp = 31.28 nm on M13mp18 and 106 bp = "
             "36.04 nm on p8064, so route B and the 40 nm nominal are in tension before any "
             "mechanics is run."),
            ("What a 15 nt tether turn does to the block MECHANICALLY. It is not a tie and it is "
             "not nothing: it is a one-sided entropic element between two helix ends, and no "
             "lattice in this repository carries one."),
            ("Whether a staple-side poly-T at the rim is available at every rim helix end of the "
             "recommended block, given that CLAUDE.md records an anti-stacking remedy and a "
             "duplex-end JOINT competing for the same two strand termini."),
        ],
    }
    return document, {
        "aligned": aligned, "worst": worst, "reach": reach_worst,
        "budgets": budgets, "loops": loops, "band": band,
    }


def _sweep_summary():
    path = os.path.join(ROOT, "gpd", "data", "T-296-sources", "europepmc-queries.json")
    if not os.path.exists(path):
        return {"available": False}
    sweep = json.load(open(path))
    return {
        "available": True,
        "queries": len(sweep),
        "families": 9,
        "totalHitsReturned": sum(len(v["records"]) for v in sweep.values()),
        "queryStringsRetainedAt": "gpd/data/T-296-sources/query.py",
        "recordsRetainedAt": "gpd/data/T-296-sources/europepmc-queries.json",
    }


def _selftest():
    failures = []

    def check(name, actual, expected, tolerance=0.0):
        ok = (abs(actual - expected) <= tolerance) if isinstance(expected, float) \
            else actual == expected
        if not ok:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    # the Langevin mirror, against its own definition
    for u in (0.1, 0.4, 0.6, 1.0, 5.0, 25.0, 100.0):
        if u >= 0.5:
            check("langevin is coth - 1/u at u = %r" % u,
                  langevin(u), 1.0 / math.tanh(u) - 1.0 / u, 1e-15)
    check("langevin(0) is 0", langevin(0.0), 0.0, 1e-18)
    # The series is truncated at u^7, so the two branches meet to ~4e-8 and no closer. This is
    # the Kotlin's own switch and its own truncation; asserting it tighter would assert the
    # series' next term.
    check("langevin's two branches meet at the switch to the series' own truncation",
          langevin(0.5) - langevin(0.4999999999), 0.0, 1e-7)
    for x in (1e-6, 0.1, 0.5, 0.9, 0.99, 0.999):
        check("inverse_langevin inverts at x = %r" % x,
              langevin(inverse_langevin(x)), x, 1e-12)
    check("log_sinh_over_u matches its own definition at u = 3",
          log_sinh_over_u(3.0), math.log(math.sinh(3.0) / 3.0), 1e-15)
    check("log_sinh_over_u is continuous across the large-u switch",
          log_sinh_over_u(20.0) - math.log(math.sinh(20.0) / 20.0), 0.0, 1e-12)

    # the reach bound, on integers
    check("n unpaired nucleotides make n + 1 steps",
          minimum_unpaired_nucleotides(2.0, 1.0), 1)
    check("a span of exactly one step needs zero nucleotides",
          minimum_unpaired_nucleotides(1.0, 1.0), 0)
    check("a span just over one step needs one",
          minimum_unpaired_nucleotides(1.0000001, 1.0), 1)

    # the span geometry
    check("the aligned span is d - 2 r_P",
          turn_phosphate_span(2.5, 0.9, 0.0, 180.0), 2.5 - 1.8, 1e-12)
    check("the worst span is d + 2 r_P",
          turn_phosphate_span(2.5, 0.9, 180.0, 0.0), 2.5 + 1.8, 1e-12)

    # the Kotlin constants are parsed, not copied
    check("the honeycomb interhelical distance is parsed from Gen1Tile",
          kotlin_constant(GEN1_TILE, "INTERHELICAL_HONEYCOMB"), 2.536, 0.0)
    try:
        kotlin_constant(GEN1_TILE, "NO_SUCH_CONSTANT")
        failures.append("a missing constant must raise, and did not")
    except LookupError:
        pass

    # departure is ABSOLUTE where the expectation is exactly zero
    check("departure against zero is absolute", departure(1e-9, 0.0), 1e-9, 0.0)
    check("departure against non-zero is relative", departure(2.0, 1.0), 1.0, 0.0)

    # The mirror against T-230's OWN committed records -- the cross-check that makes it credible.
    #
    # ASSERTED AT THE FILE'S OWN EMISSION PRECISION, and that is not a weakening. `CLAUDE.md`:
    # *"an assertion tighter than a result file's EMISSION precision is not a stronger test, it is
    # a test of the printed digits"*. Nine significant digits is what the file carries, so the
    # statement that has content is that round9(mine) IS the committed literal, EXACTLY -- which
    # is a far sharper claim than any tolerance, and it holds at 252 of 252 fields.
    #
    # The spans are recomputed here rather than read back out of the record, because the record's
    # own `spanNm` is rounded too and feeding it back moves the ninth digit of the tension. That
    # is `CLAUDE.md`'s *a result file's parameter block is a description of a run, not a
    # definition of one*, met inside a self-test.
    rounding = _load_rounding()
    t230 = result("T-230")
    interhelical = kotlin_constant(GEN1_TILE, "INTERHELICAL_HONEYCOMB")
    phosphate_radius = kotlin_constant(
        MEASURED_BACKBONE, "B_SOUTH_POPULATION_PHOSPHATE_RADIUS")
    exact_span = {
        "worst azimuth": turn_phosphate_span(interhelical, phosphate_radius, 180.0, 0.0),
        "centre to centre": interhelical,
    }
    compared = 0
    for record in t230["loops"]:
        span = exact_span[record["spanCase"]]
        state = turn_loop_state(span, record["unpairedNucleotidesPerTurn"],
                                record["kuhnLength"], record["contourPerNucleotide"])
        if state is None:
            failures.append("T-230 record %r has no state here" % record)
            continue
        for key in ("extensionRatio", "tensionPicoNewton", "freeEnergyThermal"):
            mine = rounding.round_for_result(state[key], 9, 0.0)
            compared += 1
            if mine != record[key]:
                failures.append("T-230 %s at n = %d, b = %r, c = %r: expected %r, got %r"
                                % (key, record["unpairedNucleotidesPerTurn"],
                                   record["kuhnLength"], record["contourPerNucleotide"],
                                   record[key], mine))
    check("the mirror was compared against every field of T-230's loops", compared,
          3 * len(t230["loops"]))

    for failure in failures:
        print("FAIL " + failure)
    print("%d check(s) failed" % len(failures))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--selftest", action="store_true")
    args = parser.parse_args(argv)
    if args.selftest:
        return _selftest()

    document, _ = build()
    document["existenceSweep"] = _sweep_summary()

    sys.path.insert(0, os.path.join(ROOT, "tools"))
    from emission_header import with_emission_header
    rounding = _load_rounding()
    document = rounding.walk(document, rounding.RESULT_SIGNIFICANT_DIGITS,
                             rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)
    headed = with_emission_header(document, "honeycomb", regime=[])
    with open(DESTINATION, "w") as handle:
        json.dump(headed, handle, indent=1)
        handle.write("\n")
    print("written to gpd/results/T-296-zero-loop-raster-turn.json")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
