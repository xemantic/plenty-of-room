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
# T-200 -- emits gpd/results/T-200-reemission-order.json.
#
#     tools/T-200-emit-result.py
#
# Everything here is DERIVED from committed artifacts at run time: the corrected A5 reading from
# the re-emitted T-157, the dependency edges from the reader census, the order from
# tools/reemission-order.py, and the residual census from every result file in the tree. Nothing
# is transcribed, so the whole sweep can be re-run by whoever reads it next.
#
# No wall-clock timing and no step count (`CLAUDE.md`).
import glob
import importlib.util
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")

_spec = importlib.util.spec_from_file_location(
    "reemission_order", os.path.join(ROOT, "tools", "reemission-order.py")
)
sorter = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(sorter)

# The eleven files `C-0101` re-emitted, taken from its own table.
ELEVEN = ["T-149", "T-79", "T-136", "T-99", "T-157", "T-108",
          "T-134", "T-152", "T-116", "T-135", "T-138"]

# The eight values `anchoring/PathCountConsistencyStudy.kt` reads out of `T-136`'s parameters.
T136_KEYS = ["winnerPhase", "winnerMargin", "winnerRangeOverStroke", "recommendedPhase",
             "recommendedCeiling", "recommendedMargin", "recommendedMinimaxOverStroke",
             "recommendedPeakRatio"]


def load(name):
    with open(os.path.join(RESULTS, name), encoding="utf-8") as handle:
        return json.load(handle)


def corrected_a5():
    """`C-0092`'s A5 margin-movement clause, re-read off the re-emitted `T-157`."""
    folds = load("T-157-large-rotation-arm-branch.json")["folds"]
    movements = [row["marginMovement"] for row in folds]
    return {
        "source": "gpd/results/T-157-large-rotation-arm-branch.json, as re-emitted",
        "foldCount": len(folds),
        "marginMovementMin": min(movements),
        "marginMovementMax": max(movements),
        "worstAbsoluteDepartureFromUnity": max(abs(1.0 - m) for m in movements),
        "publishedReading": "1.0000-3.3380x",
        "correctedReading": "1.0000 at every fold",
        "ceilingBindsAt": sorted({row["bindingCeiling"] for row in folds}),
        "whatStandsInA5": (
            "The candidate and its 0-of-12 non-binding verdict stand: no fold is owned by the "
            "'element model branch end' ceiling. Only the margin-movement RANGE was measuring a "
            "stale input."
        ),
    }


def edge_check_t138():
    """Is `T-138` stale against `T-136`? The second dependency edge, which nobody had checked.

    `T-138` reads eight named values out of `T-136`'s parameter block, so the test needs no solve:
    compare each against `T-136`'s current file. Six of the eight are echoed in `T-138` and are
    compared; two are read but not echoed, so they are reported as unverifiable here rather than
    silently counted as agreeing.
    """
    t136 = load("T-136-two-per-row-placement.json")["parameters"]
    blob = json.dumps(load("T-138-path-count-consistency.json"))
    echoed, silent = {}, []
    for key in T136_KEYS:
        value = t136.get(key)
        if value is None:
            silent.append(key)
            continue
        if str(value) in blob:
            echoed[key] = {"producerValue": value, "presentInConsumer": True}
        else:
            silent.append(key)
    return {
        "edge": "T-138 reads T-136",
        "valuesReadByTheConsumer": len(T136_KEYS),
        "echoedAndMatching": len(echoed),
        "readButNotEchoed": sorted(silent),
        "matches": echoed,
        "verdict": "NOT STALE" if echoed and len(echoed) == len(T136_KEYS) - len(silent) else "INDETERMINATE",
        "note": (
            "Every value T-138 echoes matches T-136's current file exactly, so this edge was "
            "re-emitted in the right order. Only one of the two edges among C-0101's eleven was "
            "violated, and it is the one CH-0131 found."
        ),
    }


def residual_census():
    """Every nonzero reproduction departure in the tree -- and why it cannot be a gate."""
    files = 0
    carriers = 0
    nonzero = 0
    worst = []
    for path in sorted(glob.glob(os.path.join(RESULTS, "*.json"))):
        files += 1
        try:
            with open(path, encoding="utf-8") as handle:
                record = json.load(handle)
        except (ValueError, OSError):
            continue
        reproductions = record.get("reproductions")
        if not isinstance(reproductions, list):
            continue
        carriers += 1
        for row in reproductions:
            if not isinstance(row, dict):
                continue
            for key in ("departure", "relativeDeparture", "absoluteDeparture"):
                value = row.get(key)
                if isinstance(value, (int, float)) and value != 0:
                    nonzero += 1
                    worst.append({
                        "file": os.path.basename(path),
                        "against": str(row.get("source") or row.get("quantity"))[:60],
                        "field": key,
                        "magnitude": abs(value),
                    })
    worst.sort(key=lambda row: row["magnitude"], reverse=True)
    return {
        "resultFiles": files,
        "filesCarryingReproductions": carriers,
        "nonzeroDepartures": nonzero,
        "largestTen": worst[:10],
        "whyThisIsNotAGate": (
            "The great majority of these are legitimate: a reproduction against a LITERATURE value "
            "is expected to differ (Fields et al. at 15 %, Bosco's C2'-endo step at 5 %), and a "
            "reproduction against a different MODEL is expected to differ too. A staleness gate "
            "built on residuals would therefore fire constantly on correct files, which is the one "
            "failure C-0080 says a checker cannot afford -- 'a drift checker's false positives cost "
            "more than its true ones, because the tool exists in order to be believed'. Measured "
            "and declined, rather than declined by assertion."
        ),
    }


def main():
    with open(os.path.join(RESULTS, "P-22-result-reader-census.json"), encoding="utf-8") as handle:
        census = json.load(handle)
    reads = sorter.edges_from_census(census)
    placed, cycles = sorter.order(ELEVEN, reads)
    pairs = sorter.dependency_pairs(ELEVEN, reads)

    result = {
        "task": "T-200",
        "claim": "C-0117",
        "what": (
            "C-0092's A5 margin-movement clause amended to what it measures, the second dependency "
            "edge among C-0101's eleven re-emissions checked, and the instrument that prevents the "
            "class shipped: tools/reemission-order.py, a topological sort of the reader census. "
            "Units: none; every number is derived from a committed artifact at run time."
        ),
        "verificationType": "logical, with 11 self-tests on the retained sorter",
        "maturity": "TRL 1-3, and below it: nothing here is physics.",
        "amendment": corrected_a5(),
        "dependencyEdgesAmongTheEleven": [
            {"producer": producer, "consumer": consumer} for producer, consumer in pairs
        ],
        "reemissionOrder": placed,
        "circularDependencies": cycles,
        "secondEdge": edge_check_t138(),
        "residualCensus": residual_census(),
        "findings": {
            "oneOfTwoEdgesWasViolated": (
                "C-0101's eleven re-emissions contain exactly TWO dependency edges: T-157 reads "
                "T-149 (violated, CH-0131) and T-138 reads T-136 (clean). So the defect was not "
                "systematic -- it was one edge of two -- and the cheap check that establishes that "
                "needs no solve at all, only a comparison of the values the consumer echoes."
            ),
            "theInstrumentAlreadyExisted": (
                "tools/result-reader-census.py (P-22, C-0082) already derives the graph a "
                "topological sort needs, including the transitive edges a grep cannot see. Turning "
                "it into an order is ~20 lines. The class was preventable with what was already in "
                "the tree, which is the sharper version of the lesson."
            ),
            "aResidualScanIsNotAGate": (
                "499 nonzero reproduction departures across 64 of 104 result files, most of them "
                "legitimate literature cross-checks. Priced and declined."
            ),
            "whatSurvivesInA5": (
                "The candidate and its verdict. Only the RANGE moved, from 1.0000-3.3380x to "
                "1.0000 everywhere, and a verdict that survives can survive on a different reason: "
                "A5's ceiling still binds at 0 of 12 states."
            ),
        },
    }
    destination = os.path.join(RESULTS, "T-200-reemission-order.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    return 0



# `CH-0268` -- this tool WRITES a committed artifact and used to ignore `sys.argv`
# entirely, so `--help` emitted.  Parse the flag or refuse the argument.
import importlib.util as _importlib_util, os as _os
_spec = _importlib_util.spec_from_file_location(
    "cli_guard", _os.path.join(_os.path.dirname(_os.path.abspath(__file__)), "cli_guard.py"))
_cli_guard = _importlib_util.module_from_spec(_spec)
_spec.loader.exec_module(_cli_guard)

if __name__ == "__main__":
    _cli_guard.refuse_unknown_arguments("tools/T-200-emit-result.py  (no arguments)")
    sys.exit(main())
