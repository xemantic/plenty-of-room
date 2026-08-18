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
# T-205 -- what did the four-layer line supersede in the queue?
#
# The classification is a JUDGEMENT and is recorded here rather than computed; what IS computed is
# the open set it is applied to, read from TASKS.md by the same `queue_status` the deliverable's
# checker uses, so the denominator cannot drift from the register.
import importlib.util
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_spec = importlib.util.spec_from_file_location(
    "trace_answers", os.path.join(ROOT, "tools", "trace-answers.py")
)
tracer = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(tracer)

# CONTINGENT: the task is well posed and its answer only matters if the tile stays SINGLE-LAYER.
# The four-layer tile is flat with NO coupling at all (C-0109, C-0116), so a coupling placement,
# phase or distribution optimisation on the 2 nm sheet is work on a body §3's own thickness row
# contradicts. It is NOT dead -- NDI has not ruled -- and that is exactly why it must be marked
# rather than struck.
CONTINGENT = {
    "T-142": "the flat 30-root design at the 5 nm layer -- a single-layer placement verdict",
    "T-143": "whether a 2.057 peak-ratio distribution on 30 single-layer upward roots can be built",
    "T-174": "a different 34-root placement under the re-solved collar -- single-layer",
    "T-176": "an exhaustive 34-root enumeration at phases 0 and 16 -- single-layer",
    "T-177": "whether the recommended phase survives a device's range -- single-layer",
    "T-179": "a count sweep with the distribution freed -- single-layer array",
    "T-180": "a nested count chain on the shared-body topology -- single-layer host",
    "T-185": "re-optimising the placement at the derived row-end prestrain -- single-layer",
}

# STRENGTHENED: the four-layer line makes the task MORE load-bearing than when it was queued.
STRENGTHENED = {
    "T-9": "the crossover hinge constant from oxDNA. A four-layer tile's rigidity is a "
           "parallel-axis enhancement over the SAME crossover springs, and C-0116 shows the "
           "flatness verdict turns on the interlayer coupling fraction -- so k_theta and the "
           "crossover's vertical compliance now carry MORE of the answer than they did on the "
           "single-layer sheet, not less.",
    "T-189": "whether the 112 bp raster row can be twist-corrected. It was queued against the "
             "row-end prestrain on a single-layer sheet; the four-layer tile is built from the "
             "same rows, and C-0119 establishes the honeycomb's own 21 bp register, so the "
             "question survives the tile change and acquires a second lattice.",
}

# UNAFFECTED: the task is about the layer, the field, the joint or the harness, and the tile's
# thickness does not enter. Listed so the sweep's denominator is visible rather than implied.
UNAFFECTED_NOTE = (
    "the remaining open items are about the polymer layer, the electrostatic field, a joint or "
    "coupling ELEMENT, a material constant, or the harness -- none is a function of the tile's "
    "layer count. They are neither contingent nor strengthened and are left exactly as they are."
)


def main():
    with open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8") as handle:
        queue = handle.read()
    statuses = tracer.queue_status(queue)
    open_ids = sorted(k for k, v in statuses.items() if v == "OPEN")

    classified = {}
    for identifier, reason in CONTINGENT.items():
        classified[identifier] = {"class": "CONTINGENT", "reason": reason,
                                  "stillOpen": identifier in open_ids}
    for identifier, reason in STRENGTHENED.items():
        classified[identifier] = {"class": "STRENGTHENED", "reason": reason,
                                  "stillOpen": identifier in open_ids}

    unclassified = [i for i in open_ids if i not in classified]

    result = {
        "task": "T-205",
        "claim": "C-0126",
        "what": (
            "What the four-layer line (C-0109 to C-0123) did to the OPEN QUEUE. A classification, "
            "not a calculation: the open set is read from TASKS.md by the same queue_status the "
            "deliverable's checker uses, so the denominator cannot drift from the register, and "
            "the class of each item is a recorded judgement with its reason. Units: none."
        ),
        "verificationType": "logical",
        "maturity": "TRL 1-3, and below it: nothing here is physics.",
        "openItems": len(open_ids),
        "classified": classified,
        "counts": {
            "CONTINGENT": sum(1 for v in classified.values() if v["class"] == "CONTINGENT"),
            "STRENGTHENED": sum(1 for v in classified.values() if v["class"] == "STRENGTHENED"),
            "UNAFFECTED": len(unclassified),
        },
        "unaffected": unclassified,
        "unaffectedNote": UNAFFECTED_NOTE,
        "theConditionEveryContingentItemHangsOn": (
            "SS3's parameter row says 'Tile thickness ~10 nm (single-layer honeycomb)', which cannot "
            "hold both ways; every structural claim before iteration 23 took the 2 nm reading, and "
            "NDI's answer to decision 5 resolves it toward the thick one BY IMPLICATION rather than "
            "by ruling. Decision 7 (the 10 x 6 cross-section) is with NDI and unanswered. So the "
            "tile's layer count is a SPECIFICATION that is currently ambiguous, and eight open "
            "tasks are well posed only on one side of it."
        ),
        "whyMarkedAndNotStruck": (
            "CLAUDE.md: 'strike a discharged item, never delete it -- a list that only ever grows "
            "is not a record and a list that silently shrinks is worse'. These are not discharged: "
            "they are CONTINGENT, which is a third state, and inventing the word is the same move "
            "C-0071 made for DISCHARGED when the queue needed a status it did not have."
        ),
        "findings": {
            "aClaimCanSupersedeATask": (
                "CLAUDE.md records 'a discharge is invisible to whoever files the removal' -- a "
                "CLAIM that removes a branch does not look at the questions that branch raised. "
                "This is the same failure one level up: a claim that changes the BODY does not "
                "look at the tasks that optimise a coupling on the old one. Eight of 57 open items "
                "are in that position and none of the six claims that put them there noticed."
            ),
            "theSweepIsCheapAndTheAlternativeIsNot": (
                "It is a read. The alternative is an agent picking up T-176 -- an exhaustive "
                "enumeration over 163 296 placements -- for a tile that may not be the one built."
            ),
            "andTwoTasksGOTMOREIMPORTANT": (
                "T-9 and T-189. A supersession sweep that only looks for work to cancel will miss "
                "the items a result makes MORE load-bearing, and T-9 -- the crossover hinge "
                "constant from oxDNA -- is now the input the four-layer rigidity and C-0116's "
                "threshold both rest on."
            ),
        },
        "validity": [
            "The classification is a judgement, not a computation, and it is recorded per item so "
            "a reader can disagree with any single row without discarding the sweep.",
            "CONTINGENT is not KILLED. If SS3's tile stays single-layer -- or if NDI declines the "
            "thicker tile -- every one of the eight is live again exactly as written.",
            "The sweep classifies against the four-layer line ONLY. An item unaffected by the tile "
            "may still be superseded by something else, and this does not look.",
        ],
    }
    destination = os.path.join(ROOT, "gpd", "results", "T-205-four-layer-supersession.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    print("open {} | contingent {} | strengthened {} | unaffected {}".format(
        len(open_ids), result["counts"]["CONTINGENT"], result["counts"]["STRENGTHENED"],
        result["counts"]["UNAFFECTED"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
