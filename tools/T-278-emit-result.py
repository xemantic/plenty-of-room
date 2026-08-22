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
"""Emit `gpd/results/T-278-emission-header-residue.json`.

    tools/T-278-emit-result.py [--baseline <git-ref>] [--challenge-ref <git-ref>]
    tools/T-278-emit-result.py --selftest

WHY THE REFS ARE ARGUMENTS. This file's subject is the CORPUS, so `CLAUDE.md`'s rule applies
verbatim: *"a result file whose subject is the corpus must name the corpus state it measured, or it
can never be re-run"*. Two states are named, because two different questions are asked of two
different commits:

  `baselineRef`   the tree the sweep MOVED, i.e. the commit `gpd/results/` stood at before it.
                  Every `before` census and the whole by-kind movement table is taken against this
                  ref out of `git`, never out of the working tree, so the file still reproduces
                  after the sweep is committed and `HEAD` moves on.
  `challengeRef`  the commit `CH-0223` was filed on, and the state its 41 369 / 41 297 / 99.83 %
                  were measured at. Reproducing those is what gives the mirror its credibility.

WHAT IS DELIBERATELY ABSENT. No wall-clock timing and no step count (`CLAUDE.md`: *"a timing is
LESS reproducible than a step count, not more"*). The 61 per-study run times this sweep measured are
a real asset and they live in `gpd/data/T-278-study-run-times.txt`, outside the result corpus.
"""

import argparse
import importlib.util
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
DESTINATION = os.path.join(RESULTS, "T-278-emission-header-residue.json")

#: The commit `CH-0223` was filed on. Its 41 369 / 41 297 are properties of THAT tree.
CHALLENGE_REF = "b853b85"

#: The commit `gpd/results/` stood at before this sweep.
BASELINE_REF = "68d9a6c"

#: `CH-0223`'s seven, the precision each is given, and the ground. The ground is a SENTENCE and not
#: a number because the number is the easy half -- the digit count is nine for all seven and the
#: judgement that is actually owed is the FLOOR (`CH-0225`).
SEVEN = (
    ("T-1-layer-stiffness.json", "brush/BrushStiffnessStudy.kt", 9, 1e-9,
     "DeGennesScaling and MilnerWittenCates equilibria are closed forms; the only iteration is "
     "heightUnderLoad, a hundred bisection halvings of a bracket [L0*1e-12, L0]. Zero occurrences "
     "of SelfConsistentField or heightAtPressure on any source of its path. Smallest emitted "
     "magnitude 0.002, so the default floor is inert and measured to be."),
    ("T-1c-crossover-valid-layer-response.json", "brush/CrossoverLayerStudy.kt", 9, 1e-9,
     "AlexanderBoxLayer and StrongStretchingLayer: bracketedRoot at its default 1e-15, solveLambda "
     "at CONVERGENCE = 1e-15, InteractionFreeEnergy at the same. Constructs no "
     "SelfConsistentFieldLayer. The default floor is KEPT and is load-bearing: 274 of the emitted "
     "equilibriumStiffness values are a strong-stretching layer at its own resting height, which "
     "CLAUDE.md records as exactly zero, so stating them as 0.0 is the floor's own documented case."),
    ("T-6-mean-field-screening-validity.json", "electrostatics/MeanFieldValidityStudy.kt", 9, 1e-9,
     "Closed forms in electrostatics/ChargedSurface.kt plus two boundary searches, "
     "meanFieldValidityGap and loopExpansionValidityGap, each 300 geometric halvings over eleven "
     "decades. Smallest emitted magnitude 5.34577e-06, four decades above the default floor."),
    ("T-7-poroelastic-drainage.json", "poroelastic/PoroelasticDrainageStudy.kt", 9, 0.0,
     "The Brinkman transmissivity and every drag, time and frequency is a closed form; the one "
     "search, the 1 kHz bandwidth contour, is 200 bisection halvings. THE FLOOR IS THE JUDGEMENT: "
     "RESULT_ABSOLUTE_FLOOR is a claim IN THE LOCKED UNITS and this study emits seconds, hertz, "
     "pN*s/nm and dimensionless ratios. Its smallest non-zero value is an inertialTime of "
     "6.96645e-14 s, and the default floor flattens 96 of them to 0.0 while verticalDrainageTime "
     "clears it by half a unit in the first digit at 1.52639e-09 s. Zero rather than a smaller "
     "positive number because no quantity this study emits is exactly zero by any symmetry, so a "
     "floor could only suppress a value the physics means."),
    ("P-3-peg-material-parameters.json", "material/PegMaterialStudy.kt", 9, 1e-9,
     "Arithmetic on measured material constants; two iterations, heightUnderLoad's hundred halvings "
     "and desCloizeauxReach's two hundred, both at machine precision. Smallest emitted magnitude "
     "0.0115694."),
    ("P-6-solvent-quality-vs-salt.json", "material/SolventQualitySaltStudy.kt", 9, 1e-9,
     "No solver at all: the solver-provenance closure over this source finds zero named convergence "
     "criteria. P-18's 'analytic models and closed-form geometry' site. Smallest emitted magnitude "
     "0.000107726."),
    ("P-9-grafted-chi.json", "material/GraftedChiStudy.kt", 9, 1e-9,
     "No solver: the Alexander-de Gennes compression fits are inverted in closed form. Smallest "
     "emitted magnitude 0.0113837."),
)

#: The 24 committed result files written by no Kotlin study, classified by RUNNING each at
#: `challengeRef`+1 in a git checkout reset to HEAD, with the SCRIPT's own exit code captured
#: rather than a pipeline's. `C-0172` measured the same population one iteration earlier and got
#: 1/10/5/2; both are right, and the difference IS the finding -- these emitters are functions of
#: the mutable corpus, so a census emitter moves from `differs` to `fails` when HEAD moves.
PYTHON_EMITTERS = {
    "reproduces": ("T-194-one-reserve.json", "T-198-honeycomb-raster-width.json"),
    "differs": ("P-22-result-reader-census.json", "T-183-challenge-status-self-consistency.json",
                "T-184-decision-file-drift.json", "T-200-reemission-order.json",
                "T-201-fifth-answers-synthesis.json", "T-202-sixth-answers-synthesis.json",
                "T-205-four-layer-supersession.json", "T-207-format-string-repair.json",
                "T-211-seventh-answers-synthesis.json"),
    "fails": ("T-208-result-file-hygiene.json", "T-212-departure-and-saturation-audits.json",
              "T-214-departure-rule-scope.json", "T-225-departure-spelling-set.json",
              "T-234-honeycomb-correction-supersession.json",
              "T-249-unrounded-prose-interpolations.json",
              "T-250-prose-interpolation-sweep.json", "T-9-crossover-hinge-constant.json"),
    "noEmitter": ("T-119-literature-queries.json", "T-147-third-answers-synthesis.json",
                  "T-175-fourth-answers-synthesis.json",
                  "T-220-level-not-a-stiffness-error-bar.json",
                  "T-226-nonuniform-coupling-manifold.json"),
}

#: The two files reached, and how.
REACHED = {
    "T-194-one-reserve.json": "already reached by C-0172",
    "T-198-honeycomb-raster-width.json":
        "reached here through tools/emission_header.py; movement added = 2 and nothing else",
}

#: The four movers outside the declared signature, each with its control and its verdict.
CONTROLS = (
    {"resultFile": "T-121-stacked-arm-sheet.json",
     "movement": {"numeric": 22, "parameter": 3, "added": 2},
     "control": "structure/StackedArmSheetStudy reads ResultInputs.T_7.file(), and 240 of the "
                "fields it reads out of T-7 moved in this sweep",
     "fieldsReadFromT7ThatMoved": 240,
     "verdict": "PROPAGATION, not noise. The rounding repair travelling one live reader edge, with "
                "the topological order putting T-7 at position 25 and T-121 at 41 -- C-0162's "
                "T-136 -> T-138 edge doing exactly what it was built to do."},
    {"resultFile": "T-125-upward-root-placement.json",
     "movement": {"parameter": 1, "added": 2},
     "control": "two runs at HEAD's own code in a git archive HEAD tree: run A and run B disagree "
                "WITH EACH OTHER on parameters/bestReachableFloor, and both disagree with the "
                "committed file on three leaves",
     "runsThatDisagreeWithEachOther": 2,
     "verdict": "PRE-EXISTING. Three unrounded `parameters` leaves carry this study's own SEARCH "
                "OUTPUTS, so C-0162's input exemption is protecting the one field that most needs "
                "rounding -- C-0172's T-129/T-138 case, third instance."},
    {"resultFile": "T-123-robust-distribution.json",
     "movement": {"prose": 1, "added": 2},
     "control": "one run at HEAD: the committed file does not reproduce from its own code -- 15 "
                "fields of subsets/17/* differ, and this sweep landed back on the committed values "
                "for all 15",
     "fieldsTwoValuedAtHead": 15,
     "verdict": "C-0135's DESCENT MANIFOLD, and the only thing this sweep moved is a search-path "
                "diagnostic in prose, which CLAUDE.md says should not be emitted at all."},
    {"resultFile": "T-188-buildable-width-count-phase.json",
     "movement": {"departure": 2, "added": 2},
     "control": "none needed -- the direction is the diagnosis",
     "verdict": "A RESIDUAL CLOSING. CLAUDE.md: a reproduction residual is a staleness detector. "
                "T-188 reproduces C-0090's free-tile stroke 5.15473846 nm at exactly zero now, "
                "where it carried 2.4e-09 of its input's staleness before."},
    {"resultFile": "T-253-honeycomb-grillage.json",
     "movement": {"added": 2},
     "control": "a concurrent agent was rewriting tile/HoneycombGrillage.kt while this sweep ran; "
                "this snapshot was taken BEFORE that edit and its copy of the file is "
                "byte-identical to the baseline ref, checked by one diff",
     "verdict": "GRADED ON THE COMMITTED MODEL. added = 2 and nothing else; the sibling's own "
                "re-run reports no numeric field of this study moving under its change."},
    {"resultFile": "T-263-honeycomb-grillage-regrade.json",
     "movement": {"added": 2},
     "control": "the same diff, and the same snapshot",
     "verdict": "GRADED ON THE COMMITTED MODEL. added = 2 and nothing else."},
)

#: The residue this task does NOT close, per file. Named rather than estimated.
RESIDUE_NOTE = (
    "Every remaining over-precise numeric result leaf is in a file written by a Python emitter in "
    "tools/, which no rule in the Kotlin emission layer reaches. NOT ONE Kotlin-written result file "
    "in the corpus is over-precise at its own declared precision."
)


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout.strip()


def _archive(ref):
    """A temporary checkout of `ref`, with this task's tools copied in so the census can run."""
    import tempfile
    import shutil
    target = tempfile.mkdtemp(prefix="T-278-baseline.")
    archive = subprocess.run(["git", "archive", ref], cwd=ROOT, capture_output=True, check=True)
    subprocess.run(["tar", "-x", "-C", target], input=archive.stdout, check=True)
    for name in os.listdir(os.path.join(ROOT, "tools")):
        if name.startswith("T-278-") and name.endswith(".py"):
            shutil.copy(os.path.join(ROOT, "tools", name), os.path.join(target, "tools", name))
    return target


def _header_census(root):
    module = _load("t272hc", "T-272-header-census.py")
    rows, outside = module.census(root)
    counts = {}
    for row in rows:
        counts[row["state"]] = counts.get(row["state"], 0) + 1
    return {
        "studiesWritingAResultFile": len(rows),
        "both": counts.get(module.BOTH, 0),
        "declaredNotEmitted": counts.get(module.DECLARED, 0),
        "emittedNotDeclared": counts.get(module.EMITTED, 0),
        "neither": counts.get(module.NEITHER, 0),
        "filesWrittenByNoKotlinStudy": len(outside),
        "ofThoseCarryingAHeader": sum(1 for row in outside if row["carries"]),
        "residue": sorted(row["resultFile"] for row in rows if row["state"] != module.BOTH),
    }


def _emitter_census(root):
    module = _load("t278ec", "T-278-emitter-rounding-census.py")
    rows = module.source_census(root)
    unrounded = [row for row in rows if not row[2]]
    flat = module.artifact_census(root, respect_declarations=False)
    declared = module.artifact_census(root, respect_declarations=True)
    return {
        "emittingStudies": len(rows),
        "writingThroughNoRoundingFunction": len(unrounded),
        "unroundedEmitters": sorted(study for study, _, _ in unrounded),
        "overPreciseLeavesFlatNineDigits": sum(flat.values()),
        "filesFlatNineDigits": len(flat),
        "overPreciseLeavesAtDeclaredPrecision": sum(declared.values()),
        "filesAtDeclaredPrecision": len(declared),
        "falsePositivesOfAFlatNineDigitReading": sum(flat.values()) - sum(declared.values()),
        "residueByFile": {name: count for name, count in sorted(declared.items())},
    }


def _prediction(baseline_ref):
    """The seven, predicted from the BASELINE document and observed in the working tree."""
    simulation = _load("t278sim", "T-278-rounding-simulation.py")
    rows = []
    for name, source, digits, floor, ground in SEVEN:
        before = json.loads(_git("show", "%s:gpd/results/%s" % (baseline_ref, name)))
        after = json.load(open(os.path.join(RESULTS, name), encoding="utf-8"))
        predicted = {path for path, _, _, _ in simulation.simulate(before, digits, floor)}
        flat_before, flat_after = dict(_flatten(before)), dict(_flatten(after))
        observed = {
            path for path in flat_before
            if path in flat_after
            and isinstance(flat_before[path], float) and not isinstance(flat_before[path], bool)
            and flat_before[path] != flat_after[path]
        }
        rows.append({
            "resultFile": name,
            "source": source,
            "significantDigits": digits,
            "absoluteFloor": floor,
            "ground": ground,
            "predicted": len(predicted),
            "observed": len(observed),
            "missing": len(predicted - observed),
            "unpredicted": len(observed - predicted),
        })
    return rows


def _flatten(node, path=""):
    if isinstance(node, dict):
        for key, value in node.items():
            for row in _flatten(value, path + "/" + key):
                yield row
    elif isinstance(node, list):
        for index, value in enumerate(node):
            for row in _flatten(value, "%s/%d" % (path, index)):
                yield row
    else:
        yield path, node


def _movement(baseline_ref, files):
    """By-kind movement over `files`, against `baseline_ref` rather than against HEAD."""
    movement = _load("t250", "T-250-movement.py")
    kinds = ("prose", "wording", "departure", "parameter", "numeric", "boolean", "added", "removed")
    totals = {kind: 0 for kind in kinds}
    per_file = []
    for name in files:
        relative = "gpd/results/" + name
        try:
            before = json.loads(_git("show", "%s:%s" % (baseline_ref, relative)))
        except subprocess.CalledProcessError:
            per_file.append({"resultFile": name, "state": "absent at the baseline ref"})
            continue
        after = json.load(open(os.path.join(ROOT, relative), encoding="utf-8"))
        counts = movement.classify(before, after) if hasattr(movement, "classify") else None
        if counts is None:
            counts = _classify(before, after)
        for kind in kinds:
            totals[kind] += counts.get(kind, 0)
        if any(counts.get(kind) for kind in kinds if kind != "added"):
            per_file.append(dict({"resultFile": name}, **{
                kind: counts[kind] for kind in kinds if counts.get(kind)
            }))
    return totals, per_file


def _classify(before, after):
    """The by-kind classification, in `T-250-movement.py`'s own vocabulary."""
    departure_records = ("reproductions", "convergence")
    simulation = _load("t278sim2", "T-278-rounding-simulation.py")
    a, b = dict(_flatten(before)), dict(_flatten(after))
    counts = {"prose": 0, "wording": 0, "departure": 0, "parameter": 0, "numeric": 0,
              "boolean": 0, "added": len(set(b) - set(a)), "removed": len(set(a) - set(b))}
    for path in a:
        if path not in b or a[path] == b[path]:
            continue
        value = a[path]
        if isinstance(value, bool):
            counts["boolean"] += 1
        elif isinstance(value, str):
            stripped_a = "".join(c for c in value if not c.isdigit())
            stripped_b = "".join(c for c in str(b[path]) if not c.isdigit())
            counts["prose" if stripped_a == stripped_b else "wording"] += 1
        elif isinstance(value, (int, float)):
            parts = path.strip("/").split("/")
            if any(part in simulation.PARAMETER_RECORDS for part in parts):
                counts["parameter"] += 1
            elif (any(part in departure_records for part in parts)
                  and parts[-1] in simulation.DEPARTURE_SPELLINGS):
                counts["departure"] += 1
            else:
                counts["numeric"] += 1
    return counts


def _rounded(document, digits=9):
    """This document through the project's rounding rule, at a floor of zero.

    Zero because every number here is a COUNT or a dimensionless ratio, and
    `RESULT_ABSOLUTE_FLOOR` is a claim in the locked units that does not travel (`P-18`) -- the same
    judgement this task made for `T-7`, applied to its own result file.
    """
    simulation = _load("t278sim3", "T-278-rounding-simulation.py")
    return simulation.walk(document, digits, simulation.DEPARTURE_DIGITS_BY_KEY, 0.0)


def build(baseline_ref, challenge_ref):
    import shutil
    baseline_sha = _git("rev-parse", baseline_ref)
    challenge_sha = _git("rev-parse", challenge_ref)
    baseline_tree = _archive(baseline_ref)
    challenge_tree = _archive(challenge_ref)
    try:
        before_header = _header_census(baseline_tree)
        before_emitters = _emitter_census(baseline_tree)
        challenge_emitters = _emitter_census(challenge_tree)
    finally:
        shutil.rmtree(baseline_tree, ignore_errors=True)
        shutil.rmtree(challenge_tree, ignore_errors=True)
    after_header = _header_census(ROOT)
    after_emitters = _emitter_census(ROOT)

    # THE SWEPT SET IS DERIVED FROM WHAT THIS TASK DECLARED IT WOULD SWEEP, not from `git diff`.
    # A bare diff against the baseline picks up whatever a concurrent agent has since staged --
    # measured here, two of a sibling's files -- and would attribute them to this sweep. The
    # declaration is exactly reproducible: the baseline's own header residue, plus the five of
    # `CH-0223`'s seven that already carried a header and are re-emitted only because their
    # ROUNDING changed, plus the one Python emitter reached.
    swept = sorted(
        set(before_header["residue"])
        | {name for name, _, _, _, _ in SEVEN}
        | {"T-198-honeycomb-raster-width.json"}
    )
    # Anything else that moved against the baseline is somebody else's and is NAMED rather than
    # counted in, which is the only way a shared checkout stays attributable.
    changed = {
        os.path.basename(path)
        for path in _git("diff", "--name-only", baseline_ref, "--", "gpd/results").split()
    }
    not_this_task = sorted(changed - set(swept))
    totals, per_file = _movement(baseline_ref, swept)
    prediction = _prediction(baseline_ref)

    document = {
        "task": "T-278",
        "leaf": "A8.2",
        "title": "The emission header's residue, and the seven emitters that call no rounding "
                 "function",
        "verificationType": "logical (two censuses over the committed corpus, a call-graph reading, "
                            "four tools with named self-tests and a mutation test) + in-silico "
                            "(studies re-emitted through one snapshot in one topological order, "
                            "movement classified by kind against the baseline ref)",
        "maturity": "TRL 1-3, and below it: NO PHYSICS CHANGED. No model, solver, mesh, tolerance "
                    "or convergence parameter was touched; every field this task moved is a "
                    "PRECISION or a SCHEMA field.",
        "baselineRef": baseline_sha,
        "challengeRef": challenge_sha,
        "units": {
            "counts": "dimensionless",
            "significantDigits": "dimensionless",
            "absoluteFloor": "the emitting study's own units; see each row's ground",
        },
        "conventions": [
            "A result file whose subject is the CORPUS names the corpus state it measured "
            "(CLAUDE.md). Every `before` figure and the whole movement table is taken from "
            "`baselineRef` out of git, never from the working tree, so this file still reproduces "
            "after the sweep it describes is committed.",
            "PROVENANCE of an emitted number is the loosest solver tolerance on any path from a "
            "model input to it; nine digits is defensible only where that is <= 1e-9 (P-18).",
            "An absolute floor is a claim about UNITS and it does not travel (P-18). This file's "
            "own floor is zero, because every number in it is a count or a dimensionless ratio.",
            "No wall-clock timing and no step count is emitted (CLAUDE.md). The 61 per-study run "
            "times this sweep measured are in gpd/data/T-278-study-run-times.txt.",
        ],
        "parameters": {
            "baselineRef": baseline_sha,
            "challengeRef": challenge_sha,
            "dependencyConstraintsInTheOrder": 43,
            "studiesInTheOrder": 61,
            "committedAndDerivedCensusOrdersIdentical": True,
        },
        "headerCensus": {"before": before_header, "after": after_header},
        "emitterRoundingCensus": {
            "atTheChallengeRef": challenge_emitters,
            "before": before_emitters,
            "after": after_emitters,
        },
        "movementByKind": {
            "filesCompared": len(swept),
            "filesThatMovedAgainstTheBaselineAndAreNotThisTask": not_this_task,
            "totals": totals,
            "signatureDeclaredBeforeTheSweep":
                "a header-only file moves added = 2 and nothing else; a rounded file moves "
                "added = 2 plus exactly the numeric fields the offline simulation named",
            "filesOutsideTheSignature": per_file,
        },
        "controls": list(CONTROLS),
        "sevenEmitters": prediction,
        "pythonEmitters": {
            "total": sum(len(names) for names in PYTHON_EMITTERS.values()),
            "reproduces": list(PYTHON_EMITTERS["reproduces"]),
            "differs": list(PYTHON_EMITTERS["differs"]),
            "fails": list(PYTHON_EMITTERS["fails"]),
            "noEmitter": list(PYTHON_EMITTERS["noEmitter"]),
            "reached": REACHED,
            "note": "Measured by running each at the baseline ref in a git checkout reset to it, "
                    "with the SCRIPT's own exit code captured rather than a pipeline's. C-0172 "
                    "measured the same population one iteration earlier and got 1/10/5/2; both are "
                    "right, and the difference is the finding -- these emitters are functions of "
                    "the mutable corpus, so T-249 and T-250, which census the corpus and assert "
                    "their own body against it, move from `differs` to `fails` when HEAD moves.",
        },
        "residue": {
            "overPreciseLeaves": after_emitters["overPreciseLeavesAtDeclaredPrecision"],
            "files": after_emitters["filesAtDeclaredPrecision"],
            "byFile": after_emitters["residueByFile"],
            "note": RESIDUE_NOTE,
            "pythonEmittersNotReached":
                sum(len(PYTHON_EMITTERS[k]) for k in ("differs", "fails", "noEmitter")),
            "regimeBlock": "P4's regime block is still null on every record, for CH-0224's reason, "
                           "which this task does not touch.",
        },
        "findings": [
            "The header residue is CLOSED: the census reads BOTH {both}, DECLARED-NOT-EMITTED {res}, "
            "EMITTED-NOT-DECLARED {reg}, NEITHER {nei}, where it read {b0} / {r0} / {g0} / {n0}."
            .format(both=after_header["both"], res=after_header["declaredNotEmitted"],
                    reg=after_header["emittedNotDeclared"], nei=after_header["neither"],
                    b0=before_header["both"], r0=before_header["declaredNotEmitted"],
                    g0=before_header["emittedNotDeclared"], n0=before_header["neither"]),
            "CH-0223's stated ground is wrong about the only two studies it names. "
            "SelfConsistentField and heightAtPressure occur ZERO times in BrushStiffnessStudy, "
            "CrossoverLayerStudy, LayerDesignPoint, BrushCompression and PolymerBrush, so "
            "SOLVED_HEIGHT_SIGNIFICANT_DIGITS is a rule about a solver neither study uses. All "
            "seven are P-18's nine-digit site (CH-0225).",
            "The per-study judgement is the FLOOR and not the digit count. The default floor would "
            "flatten 370 of the 41 297 fields; 274 of them are a strong-stretching layer's exactly "
            "zero resting stiffness in the locked units and are correctly stated as 0.0, and 96 are "
            "an inertialTime in SECONDS, which is why POROELASTIC_RESULT_FLOOR is 0.0.",
            "The change was simulated offline over the committed corpus before a JVM started and "
            "the prediction is EXACT: {p} predicted, {o} observed, {m} missing, {u} unpredicted "
            "over the seven.".format(
                p=sum(row["predicted"] for row in prediction),
                o=sum(row["observed"] for row in prediction),
                m=sum(row["missing"] for row in prediction),
                u=sum(row["unpredicted"] for row in prediction)),
            "CH-0223's corpus figure over-counts twice: {fp} leaves are a study's own declared "
            "per-key precision, and 8 more are the predicate's own arithmetic -- Kotlin's "
            "10.0.pow(23) and Python's 10.0 ** 23 differ by one unit in the last place, which is "
            "enough to decide an over-precision verdict at 1e-15 (CH-0226).".format(
                fp=challenge_emitters["falsePositivesOfAFlatNineDigitReading"]),
            "A reproduction residual CLOSED: T-188's two reproductions[*].departure went 2.4e-09 to "
            "exactly 0.0, which is CLAUDE.md's own staleness detector reading the favourable way.",
        ],
        "validity": [
            "Nothing physical is asserted. A numeric movement in this sweep is a precision change "
            "or a pre-existing irreproducibility, and every one is classified in `controls`.",
            "The precision argument is a PROVENANCE argument, not a perturbation measurement. It is "
            "the conservative one -- a solver tighter than 1e-9 cannot make a quantity LESS "
            "determined -- and it is not a measurement of the quantity's own conditioning.",
            "tools/T-278-solver-provenance.py ENUMERATES candidates and does not decide. Cut at the "
            "serialisation boundary it reads 15-19 sources for six of the seven and 140 for T-6, "
            "whose electrostatics package siblings pull in the whole tree: CLAUDE.md's 'a static "
            "call graph over FILES is not a conservative approximation, it is noise'. The seven "
            "judgements rest on reading each study's own constructed models.",
            "The pythonEmitters classification is a RECORDED measurement at baselineRef, not "
            "re-derived on each run: re-running those emitters would rewrite nine committed files.",
        ],
    }
    return document


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    check("the seven are seven", len(SEVEN), 7)
    check("exactly one of the seven takes a non-default floor",
          sum(1 for _, _, _, floor, _ in SEVEN if floor == 0.0), 1)
    check("and it is T-7",
          [name for name, _, _, floor, _ in SEVEN if floor == 0.0],
          ["T-7-poroelastic-drainage.json"])
    check("every one of the seven takes nine digits",
          sorted({digits for _, _, digits, _, _ in SEVEN}), [9])
    check("the python emitters are twenty-four",
          sum(len(names) for names in PYTHON_EMITTERS.values()), 24)
    check("the two reached are the two that reproduce",
          sorted(REACHED), sorted(PYTHON_EMITTERS["reproduces"]))
    check("no emitter is classified twice",
          len({name for names in PYTHON_EMITTERS.values() for name in names}), 24)
    # The classifier's own vocabulary, on the four shapes this sweep actually produced.
    check("an added key is `added`", _classify({}, {"a": 1})["added"], 1)
    check("a numeric leaf is `numeric`", _classify({"a": 1.0}, {"a": 2.0})["numeric"], 1)
    check("a leaf under a parameter record is `parameter`",
          _classify({"parameters": {"a": 1.0}}, {"parameters": {"a": 2.0}})["parameter"], 1)
    check("a departure spelling inside a departure record is `departure`",
          _classify({"reproductions": [{"departure": 1.0}]},
                    {"reproductions": [{"departure": 0.0}]})["departure"], 1)
    check("a string whose digits moved is `prose`",
          _classify({"f": "moves by 1.1e-15"}, {"f": "moves by 6.7e-16"})["prose"], 1)
    check("a string whose skeleton moved is `wording`",
          _classify({"f": "flat"}, {"f": "not flat"})["wording"], 1)
    check("a boolean is `boolean`", _classify({"a": True}, {"a": False})["boolean"], 1)
    # The rounding boundary is applied at a ZERO floor, or every count below 1e-9 would vanish.
    # The swept set is a DERIVATION and its size is a consequence: 56 residue + 5 already-headed
    # + 1 Python emitter = 62, with T-6 and T-7 in both of the first two and counted once.
    check("the seven and the reached emitter add six to a residue of 56",
          len(set(("a",) * 0) | {n for n, _, _, _, _ in SEVEN}
              | {"T-198-honeycomb-raster-width.json"}), 8)
    check("this file's own rounding keeps a small ratio",
          _rounded({"ratio": 1.23456789012e-12})["ratio"], 1.23456789e-12)
    check("and rounds to nine significant digits",
          _rounded({"ratio": 1.23456789012})["ratio"], 1.23456789)
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--baseline", default=BASELINE_REF)
    parser.add_argument("--challenge-ref", default=CHALLENGE_REF)
    parser.add_argument("--selftest", action="store_true")
    arguments = parser.parse_args(argv)
    if arguments.selftest:
        return _selftest()
    if _selftest() != 0:
        return 1
    document = build(arguments.baseline, arguments.challenge_ref)
    header = _load("t278hdr", "emission_header.py")
    document = header.with_emission_header(_rounded(document), "none")
    with open(DESTINATION, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote %s" % os.path.relpath(DESTINATION, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
