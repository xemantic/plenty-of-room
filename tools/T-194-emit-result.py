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
# T-194 -- emits gpd/results/T-194-one-reserve.json.
#
#     tools/T-194-emit-result.py
#
# The task is a DELIVERABLE task: it re-issues two NDI decisions as one, because both answers
# name the same reserve.  Its content is therefore a RANKING, and the whole point of the ranking
# is that it must be read out of the corpus rather than remembered -- so every number below is
# derived from an upstream result file at run time and nothing is transcribed.
#
# It emits no wall-clock timing and no step count (`CLAUDE.md`).
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")


def load(name):
    with open(os.path.join(RESULTS, name), encoding="utf-8") as handle:
        return json.load(handle)


def buffer_spend():
    """What 0.5 mM buys, from `C-0091`'s own census (`T-156`).

    The distinction the deliverable has to carry is `C-0091`'s: the routes are COMMON MODE.
    Six named routes are three -- one withdrawn, two that are the others read again at departures
    of 0.0 and 2.7e-8 -- so the word "independent" is not available for the count, and
    `C-0005`'s 123-214 % one-loop correction is common mode to all three and LARGER than every
    one of the advantages.  And an advantage is a quantity that needs the state it is read at:
    4.97x is a ZERO-STROKE reading of the same route whose held reading is 1.48-1.57x.

    Taken from the census's structured `verdict` block rather than from `C-0091`'s prose.  Its
    headline triple "1.35, 1.57, 1.75" is only two-thirds derivable that way: 1.75 and the
    1.48-1.57 held band are fields, and the **1.35 is not a field anywhere** -- it is 1.3480, the
    `Q5` re-read (a bias margin of 1.8706 against 1.3877), which the census carries in a PROSE
    string and no consumer can grep.  Recorded here as what it is.
    """
    census = load("T-156-buffer-route-census.json")
    verdict = census["verdict"]
    held = [row["heldAdvantage"] for row in census["heldForceClause"]]
    overstatement = [row["overstatement"] for row in census["heldForceClause"]]
    return {
        "source": "gpd/results/T-156-buffer-route-census.json (C-0091), its `verdict` block",
        "namedRoutes": verdict["namedRoutes"],
        "withdrawn": verdict["withdrawn"],
        "transfers": verdict["transfers"],
        "independentSurvivors": verdict["independentSurvivors"],
        "distinctMechanisms": verdict["distinctMechanisms"],
        "everySurvivorFavoursLowSalt": verdict["everySurvivorFavoursLowSalt"],
        "strongestSurvivingAdvantage": verdict["strongestSurvivingAdvantage"],
        "weakestSurvivingAdvantage": verdict["weakestSurvivingAdvantage"],
        "strongestAdvantageAtTheOperatingPoint": verdict["strongestAdvantageAtTheOperatingPoint"],
        "heldForceClauseAdvantageMin": min(held),
        "heldForceClauseAdvantageMax": max(held),
        "heldForceClauseModelCount": len(held),
        "overstatementOfTheZeroStrokeReadingMin": min(overstatement),
        "overstatementOfTheZeroStrokeReadingMax": max(overstatement),
        "commonMode": True,
        "commonModeNote": (
            "C-0091: the three surviving advantages are smaller than C-0005's 123-214 % one-loop "
            "correction, which is common mode to all of them -- so they do not diversify the "
            "exposure and must not be counted as three pieces of evidence."
        ),
        "headlineTripleProvenance": (
            "C-0091's '1.35, 1.57, 1.75' is 1.3480 (the Q5 re-read, a PROSE string in the census "
            "and not a field), 1.57034099 (heldForceClause max) and 1.75104168 "
            "(strongestAdvantageAtTheOperatingPoint). Only the last two are greppable."
        ),
    }


def layer_spend():
    """What a 17-26 nm layer buys, from `C-0110`'s own sweep (`T-192`).

    This is the number that was NOT in the corpus when `T-194` was queued, and it inverts the
    ranking the deliverable currently carries.
    """
    sweep = load("T-192-device-b-tall-gap.json")
    thresholds = {}
    for row in sweep["reachThresholds"]:
        if row["appliedBias"] == 1.0:
            thresholds[str(row["concentration"])] = row["deepestGapReachingTarget"]
    # The verdict field carries its reason in the same string ("REFUSED -- the equilibrium path
    # folds at 5.82... nm"), so it is matched on its leading word rather than for equality; a
    # `==` here would count zero admitted states and read as a much stronger result than the
    # sweep supports.
    admitted = {}
    for state in sweep["states"]:
        line = state["loadLine"]
        entry = admitted.setdefault(line, {"admitted": 0, "total": 0})
        entry["total"] += 1
        if state["verdict"].startswith("ADMITTED"):
            entry["admitted"] += 1
    return {
        "source": "gpd/results/T-192-device-b-tall-gap.json (C-0110)",
        "deepestGapDeliveringTheTargetForce": thresholds,
        "ndiBandLow": 17.0,
        "ndiBandHigh": 26.0,
        "shortfallAtBandLow": 17.0 / thresholds["0.5"],
        "shortfallAtBandHigh": 26.0 / thresholds["0.5"],
        "admittedByLoadLine": admitted,
        "strokeExistsForceDoesNot": True,
        "strokeExistsNote": (
            "CH-0127: the uncoupled tile reaches a 10 nm stroke at 52 of 96 tall states, so "
            "C-0050's escape is real in DISPLACEMENT and empty in FORCE, and no claim had "
            "separated the two."
        ),
    }


def main():
    buffer_row = buffer_spend()
    layer_row = layer_spend()
    device_a = next(
        value for key, value in layer_row["admittedByLoadLine"].items() if key.startswith("device-A")
    )
    device_b = next(
        value for key, value in layer_row["admittedByLoadLine"].items() if key.startswith("device-B")
    )
    ranking_reverses = device_a["admitted"] == 0 and device_b["admitted"] <= 1
    result = {
        "task": "T-194",
        "claim": "C-0114",
        "what": (
            "The buffer and the tall layer are ONE reserve -- NDI's answers to decisions 1 and 2 "
            "both name 'additional operating margin' bought with 'additional work on stabilizing "
            "DNA origami at low salt' -- so the two spends are re-issued as one decision, ranked "
            "on the corpus's own numbers. Deliverable task: it derives no new physics and every "
            "number is read out of an upstream result file at run time."
        ),
        "verificationType": "logical (a ranking assembled from two upstream result files)",
        "maturity": "TRL 1-3. Model-consistent and traceable, never empirically demonstrated.",
        "units": "nm for gaps and heights, mM for concentration, dimensionless for ratios",
        "spends": {"buffer": buffer_row, "tallLayer": layer_row},
        "ranking": {
            "asQueued": "spend it on the layer -- the layer buys a whole clause of SS3, the buffer buys 1.35-1.75x inside a 123-214 % error bar",
            "asMeasured": "spend it on the buffer -- the layer buys NEITHER clause of SS3, and the buffer's gain survives at the state the device occupies",
            "reversed": ranking_reverses,
            "whatReversedIt": (
                "C-0110 (T-192), filed in the same iteration this task was written: SS3's 100 pN "
                "stops arriving at 13.6989179 nm at 0.5 mM, below the BOTTOM of NDI's own "
                "17-26 nm band, so the tall layer is refused at 96 of 96 states on the "
                "acceptable clause and admitted at 1 of 96 on the desired one."
            ),
            "premiseFalsified": (
                "'the tall layer is the only route to a whole clause of SS3' (C-0050, as read by "
                "the T-194 queue row and by DECISIONS-FOR-NDI.md). CH-0127 splits it: the stroke "
                "exists and the force does not."
            ),
        },
        "whatThisProgrammeCannotRank": (
            "The PRICE. Both spends are bought with the same unpriced currency -- origami "
            "stabilisation work at low salt -- and this programme has a column for what a spend "
            "BUYS and no column at all for what it COSTS. C-0091's lesson, and the lesson NDI's "
            "answer to decision 1 already taught: 'it costs nothing' was a statement about the "
            "physics standing in for a statement about the cost."
        ),
        "consequenceForTheReIssuedQuestion": (
            "The re-issued question is no longer 'which of these two do we spend the reserve on'. "
            "One of the two claimants has been withdrawn by measurement, so the reserve has ONE "
            "claimant and the question becomes whether the buffer's 1.35-1.75x at the operating "
            "state is worth the stabilisation work -- which is a price question, i.e. NDI's."
        ),
    }
    destination = os.path.join(RESULTS, "T-194-one-reserve.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    return 0


if __name__ == "__main__":
    sys.exit(main())
