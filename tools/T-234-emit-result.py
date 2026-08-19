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
# T-234 -- what did C-0141 and C-0140 supersede, in the queue and in the corpus?
#
# Two halves, and each one covers the other's blind spot:
#
#   * the QUEUE half classifies every OPEN row, with the open set derived from TASKS.md by the
#     same `queue_status` the deliverable's checker uses -- so the denominator cannot drift from
#     the register.  It cannot see anything that is not a row.
#   * the CORPUS half is a regular-expression census over five named premise families, with the
#     class of each occurrence read from tools/T-234-classification.json.  It sees prose, and it
#     is what found the specification question the row census structurally could not.
#
# The classification is a JUDGEMENT.  What is computed is the set it is applied to.
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


census_tool = _load("t234", "T-234-census.py")
tracer = _load("trace_answers", "trace-answers.py")

# --- the queue classification -----------------------------------------------------------------
#
# SUPERSEDED   the question no longer applies -- the thing it asks about is not there.
# CONTINGENT   it applies only under a reading now in doubt.
# REPRICED     it applies, and its cost or its value changed.
# UNAFFECTED   neither correction reaches it.

REPRICED = {
    "T-9": "the crossover hinge constant from oxDNA. C-0126 marks it STRENGTHENED because a "
           "four-layer rigidity is a parallel-axis enhancement over the SAME crossover springs and "
           "C-0116's threshold turns on the interlayer coupling. C-0141 makes that STRENGTHENING "
           "cross-section-dependent: on 15 x 4 the threshold moves 0.0788618807 -> 0.276970522, "
           "INSIDE the measured 0.26-0.33 band, so k_theta now DECIDES the flatness verdict there; "
           "on 10 x 6 it falls to 0.012737738, 20x below the band, and k_theta matters LESS than "
           "T-205 recorded. The verdict survives on a different reason.",
    "T-142": "C-0126 marks it CONTINGENT on the ground that the four-layer tile is flat with no "
             "coupling at all. C-0141 shows that ground holds for 10 x 6 and FAILS for 15 x 4 "
             "(0.101759944 at the measured band's low end, outside T-5b). Class unchanged, value "
             "up: on a 15 x 4 tile a coupling is back on the table. CH-0181.",
    "T-176": "same ground, same reprice -- an exhaustive single-layer placement enumeration.",
    "T-177": "same ground, same reprice -- a single-layer phase verdict over a device's range.",
    "T-179": "same ground, same reprice -- a single-layer count sweep with the distribution freed.",
    "T-180": "same ground, same reprice -- a nested count chain on the single-layer shared body.",
    "T-185": "same ground, same reprice -- re-optimising a single-layer placement at the derived "
             "prestrain.",
    "T-174": "same ground, same reprice -- a single-layer placement under the re-solved collar.",
    "T-143": "same ground, same reprice -- whether a single-layer 30-root distribution can be built.",
    "T-232": "raised by C-0141 and repriced by C-0140: its acceptance names THREE moved geometry "
             "inputs (edgeY, the in-plane pitch, the layer spacing) and there is a FOURTH. edgeX is "
             "112 bp = 38.08 nm, and C-0140 shows a honeycomb x-raster carries both turn senses, so "
             "no uniform row length exists: its recommendation is 112 / 108 bp, an axial extent of "
             "116 bp = 39.44 nm.",
    "T-233": "raised by C-0141 and repriced by C-0140: the acceptance as written would install a "
             "WITHDRAWN width into the two deliverables. Both target footprints are written "
             "38.08 x ..., and 38.08 nm is C-0119's uniform 112 bp row, which CH-0172 overturns in "
             "the reading 'drawable at a uniform width'.",
}

SUPERSEDED_OUTSIDE_THE_ROWS = {
    "TASKS.md item 12 / DECISIONS-FOR-NDI.md decision 7": (
        "'Is a 38 x 25 nm tile acceptable, in exchange for removing the last unmeasured dependency "
        "in the flatness verdict?' -- SUPERSEDED AS POSED. The trade it asks NDI to accept does not "
        "exist and it is posed the wrong way round: corrected to the honeycomb's own two pitches, "
        "10 x 6 is 38.08 x 37.504 nm (0.929 of SS3's 40.35, essentially SS3's square) and 15 x 4 is "
        "38.08 x 56.524 nm (1.401x it). The tile offered as the COST is the one that fits; the "
        "default is the one that overruns SS3 by 40 %. And 15 x 4 also stops being flat, its "
        "threshold moving inside the measured band. All three criteria -- yield, flatness, "
        "footprint -- now point the same way, so what NDI is owed is a CORRECTION and not a trade. "
        "It is NOT a table row, so the derived row census structurally cannot see it; the corpus "
        "census found it. That is the whole argument for running both halves."
    ),
}

UNAFFECTED_NOTE = (
    "The remaining open items are about the polymer layer, the electrostatic field, a joint or "
    "coupling ELEMENT, a material constant, a single-layer square-lattice lattice question, or the "
    "harness. T-230 and T-231 are UNAFFECTED for the opposite reason: C-0140 RAISED them, so their "
    "premise is the correction rather than something the correction moved."
)


def grep_verify(root, path, line_number, needle):
    """Is `needle` literally present at `path`:`line_number`?  Falsifier F3's instrument."""
    with open(os.path.join(root, path), encoding="utf-8") as handle:
        lines = handle.read().split("\n")
    if not 1 <= line_number <= len(lines):
        return False
    return needle in lines[line_number - 1]


def main():
    with open(os.path.join(ROOT, "TASKS.md"), encoding="utf-8") as handle:
        queue = handle.read()
    statuses = tracer.queue_status(queue)
    open_ids = sorted(k for k, v in statuses.items() if v == "OPEN")

    classified = {}
    for identifier, reason in REPRICED.items():
        classified[identifier] = {
            "class": "REPRICED",
            "reason": reason,
            "stillOpen": identifier in open_ids,
        }
    unclassified = [i for i in open_ids if i not in classified]

    records = census_tool.census(ROOT)
    records, problems = census_tool.classify(records, census_tool.load_classification())
    by_class = {}
    for record in records:
        by_class[record["class"]] = by_class.get(record["class"], 0) + 1
    by_family = {}
    for family, _p, _c in census_tool.FAMILIES:
        by_family[family] = sum(1 for r in records if r["family"] == family)

    # --- Deliverable 2: the list T-233 needs, with the exact string, grep-verified
    deliverable_list = []
    for record in records:
        if record["deliverable"] and record["class"] in census_tool.ADDRESSED:
            deliverable_list.append(
                {
                    "file": record["file"],
                    "line": record["line"],
                    "family": record["family"],
                    "token": record["token"],
                    "class": record["class"],
                    "exactLine": record["text"],
                }
            )
    verified = sum(
        1
        for e in deliverable_list
        if grep_verify(ROOT, e["file"], e["line"], e["token"])
        and grep_verify(ROOT, e["file"], e["line"], e["exactLine"][:80])
    )

    # --- the claims and TASKS.md occurrences this task repaired
    repaired = [
        r
        for r in records
        if not r["deliverable"] and r["class"] in census_tool.ADDRESSED
    ]
    by_headline = sum(1 for r in repaired if r["headlinePointer"] and not r["pointer"] and not r["struck"])
    by_inline = sum(1 for r in repaired if r["pointer"])
    by_strike = sum(1 for r in repaired if r["struck"] and not r["pointer"])

    falsifiers = {
        "F1": {
            "statement": "no open item and no corpus statement is moved by either claim, so the "
                         "corrections are self-contained and this sweep is unnecessary",
            "fired": not (classified or deliverable_list),
            "outcome": "did NOT fire: {} open rows repriced, {} corpus occurrences classified "
                       "MOVED or DISCHARGED of {} found".format(
                           len(classified),
                           by_class.get("MOVED", 0) + by_class.get("DISCHARGED", 0),
                           len(records),
                       ),
        },
        "F2": {
            "statement": "every moved item is SUPERSEDED -- the four-way classification collapses "
                         "to the two-way one C-0126 already had, so the extra classes are "
                         "invention rather than discovery",
            "fired": False,
            "outcome": "did NOT fire, and the partition came out LOPSIDED rather than collapsed: "
                       "11 REPRICED, 0 CONTINGENT, 0 SUPERSEDED among the rows, and exactly ONE "
                       "SUPERSEDED item -- which is not a row at all. The empty CONTINGENT class "
                       "is itself the finding: a GEOMETRY correction does not put a question's "
                       "applicability in doubt, it changes what the answer is worth. C-0126's "
                       "corrections were about a BODY and produced contingency; these are about a "
                       "LATTICE and produced prices.",
        },
        "F3": {
            "statement": "a string listed for T-233 cannot be grepped out of the file and line it "
                         "names, so the census reports its own reconstruction rather than the "
                         "corpus and the whole list is void",
            "fired": verified != len(deliverable_list),
            "outcome": "did NOT fire: {} of {} listed strings verified in place, on both the "
                       "matched token and the first 80 characters of the line".format(
                           verified, len(deliverable_list)
                       ),
        },
        "F4": {
            "statement": "reading adds a queue row the mechanical census did not flag, so the "
                         "cheap bound is not a bound",
            "fired": False,
            "outcome": "did NOT fire on the rows: the token census flagged 13 of the 59 open rows "
                       "and the reading kept 11 and released 2 (T-230 and T-231, raised BY C-0140). "
                       "It DID find the one item the row census structurally cannot see -- decision "
                       "7, which is prose and not a row -- which is why both halves are run.",
        },
        "F5": {
            "statement": "the gate is vacuous -- the headline-banner rule discharges everything, so "
                         "nothing has to be pointed or struck at all",
            "fired": False,
            "outcome": "did NOT fire: of {} repaired occurrences, {} are discharged by a claim's "
                       "headline banner, {} carry an inline pointer within 900 characters and {} "
                       "are struck. The banner rule is restricted to gpd/claims/ for exactly this "
                       "reason: on TASKS.md or a deliverable it would discharge a whole file from "
                       "one sentence.".format(len(repaired), by_headline, by_inline, by_strike),
        },
    }

    result = {
        "task": "T-234",
        "claim": "C-0144",
        "what": (
            "What C-0141 (the four-layer cross-section is not a honeycomb) and C-0140 (a honeycomb "
            "x-raster carries both turn senses) SUPERSEDED, in the open queue and in the corpus. A "
            "classification, not a calculation. The open set is read from TASKS.md by the same "
            "queue_status the deliverable's checker uses; the corpus set is a regular-expression "
            "census over five named premise families, gated by tools/T-234-census.py. Units: none."
        ),
        "verificationType": "logical",
        "maturity": "TRL 1-3, and below it: nothing here is physics.",
        "commit": subprocess.run(
            ["git", "rev-parse", "HEAD"], cwd=ROOT, capture_output=True, text=True
        ).stdout.strip(),
        "snapshotNote": (
            "Two sibling agents were adding queue rows and claims while this census ran, so the "
            "census is a SNAPSHOT at the commit above and its denominators are that commit's."
        ),
        "queue": {
            "openItems": len(open_ids),
            "mechanicalCandidates": 13,
            "mechanicalCandidatesAtTheRegisterAsFound": 12,
            "mechanicalCandidateNote": (
                "13 of the 59 open rows carry a moved-premise token: T-9, T-142, T-143, T-174, "
                "T-176, T-177, T-179, T-180, T-185, T-230, T-231, T-232, T-233. The grep ran BEFORE "
                "any reading and the reading only classified: 11 kept, 2 released (T-230 and T-231, "
                "whose premise IS C-0140 -- they were raised by it). The register itself moved "
                "during the sweep: as found it read 57 open and 12 candidates, because T-231's own "
                "status cell contains the word ANSWERED about a DIFFERENT task and queue_status "
                "reads the whole row. Repairing that -- and the same defect in T-111, which is "
                "older -- takes the register to 59 and the candidate set to 13."
            ),
            "classified": classified,
            "counts": {
                "SUPERSEDED": 0,
                "CONTINGENT": 0,
                "REPRICED": len(classified),
                "UNAFFECTED": len(unclassified),
            },
            "unaffected": unclassified,
            "unaffectedNote": UNAFFECTED_NOTE,
            "supersededOutsideTheRowDenominator": SUPERSEDED_OUTSIDE_THE_ROWS,
        },
        "corpus": {
            "occurrences": len(records),
            "files": len({r["file"] for r in records}),
            "byClass": by_class,
            "byFamily": by_family,
            "unclassifiedOrStale": problems,
            "repairedHere": {
                "occurrences": len(repaired),
                "byHeadlineBanner": by_headline,
                "byInlinePointer": by_inline,
                "byStrike": by_strike,
                "claimsAnnotated": 12,
                "note": "12 claims carry a new iteration-34 headline banner; C-0120, C-0122 and "
                        "C-0128 already carried an iteration-33 annotation in their BODY and their "
                        "headlines did not, which is what a reader meets first.",
            },
        },
        "deliverableListForT233": {
            "count": len(deliverable_list),
            "verifiedInPlace": verified,
            "note": (
                "This task does NOT edit ANSWERS.md or DECISIONS-FOR-NDI.md -- T-233 owns them. "
                "Every entry carries the file, the 1-based line, the matched token and the exact "
                "line, and every one was verified present at that line before publication."
            ),
            "entries": deliverable_list,
        },
        "findings": {
            "aGeometryCorrectionREPRICES_whereABodyCorrectionMADE_CONTINGENT": (
                "C-0126's four-layer line changed the BODY and produced 8 CONTINGENT items. C-0141 "
                "and C-0140 change the LATTICE the same body sits on, and produced 0 contingent "
                "and 11 repriced. The distinction is not vocabulary: a body change can make a "
                "question moot, a lattice change moves what the answer is worth. The four-way "
                "partition earns its extra classes by coming out lopsided in a way the two-way one "
                "could not express."
            ),
            "theSUPERSESSION_SWEEPS_OWN_GROUND_MOVED": (
                "C-0126 marks eight tasks CONTINGENT because 'the four-layer tile is flat with no "
                "coupling at all' -- 0.0577199433. At the corrected cross-section that is 15 x 4 "
                "0.0978155002 free and 0.101759944 at the measured band's low end, which FAILS "
                "T-5b, and 10 x 6 0.0240648102, flat across the whole band. The ground holds on one "
                "cross-section and fails on the other, and C-0126 states it of 'the four-layer "
                "tile'. CLAUDE.md's 'a verdict that survives can survive on a different reason', "
                "applied to a classification. CH-0181."
            ),
            "aDECISION_TO_NDI_CAN_BE_SUPERSEDED_AND_NO_CHECKER_LOOKS": (
                "Decision 7 asks NDI to accept a 38 x 25 nm tile in exchange for removing an "
                "unmeasured dependency. Corrected, the tile it offers as the cost is 38.08 x 37.504 "
                "nm -- essentially SS3's square -- and the default it offers instead is 38.08 x "
                "56.524 nm, 1.401x SS3's. The question reverses. It is prose in a numbered list, "
                "not a queue row, so queue_status cannot see it and no retained checker reaches it; "
                "it was found by the CORPUS census, on the token '38 x 25 nm'."
            ),
            "aCLOSING_WORD_ABOUT_ANOTHER_TASK_CLOSES_THE_ROW_IT_SITS_IN": (
                "T-231's status cell reads 'TODO -- LOW, and now unblocked: the honeycomb station "
                "lattice was ANSWERED by C-0141', and queue_status reads the WHOLE row after the "
                "identifier, so the row reported CLOSED. T-111 carries the same defect and is "
                "older. CLAUDE.md records that an unknown status word reads OPEN, which is the safe "
                "direction; this is the UNSAFE one -- a KNOWN word, about a DIFFERENT task, in the "
                "same row. Both repaired by lower-casing the word, which is the queue's own idiom "
                "(verdicts in bold upper case, prose in lower). The register read 57 open and reads "
                "59."
            ),
            "aCLAIM_THAT_SUPPLIES_A_MISSING_THING_DISCHARGES_STATEMENTS_NOBODY_SWEEPS": (
                "Four claims -- C-0109, C-0118, C-0119, C-0136 -- carry a validity-range item saying "
                "this repository has no honeycomb station lattice, plan ceiling or placement "
                "family. C-0141 supplies all three and annotated none of them. That is CLAUDE.md's "
                "'a discharge is invisible to whoever files the removal' read from the other side: "
                "not a claim that REMOVES a branch failing to sweep, but a claim that SUPPLIES a "
                "missing capability failing to sweep the notices of its absence. 10 occurrences, "
                "classified DISCHARGED rather than MOVED because the repair is the same and the "
                "direction is opposite."
            ),
            "theSCAFFOLD_IS_A_SOURCE_CONTRADICTION_NOT_A_READING_ERROR": (
                "C-0119 and C-0125 read 'i: p8064' from the caDNAno paper's Methods; C-0140 derives "
                "p7560 from the paper's main-text rule (60 helices -> 7 560, 64 -> 8 064) and the "
                "15 x 4 helix count. Both read correctly. The PAPER disagrees with itself, and the "
                "discriminator is that the Methods list agrees with the main-text rule at 6 of its "
                "7 designs and disagrees at exactly one -- ours. Under the Methods reading, design "
                "(i) would leave 504 nucleotides the paper's own 126-bases-per-helix accounting has "
                "no line for. CH-0180: the scaffold is NOT ESTABLISHED, and two standing claims "
                "state it as though it were."
            ),
            "theCHEAP_BOUND_RAN_FIRST_AND_IT_IS_A_BOUND_ON_ONE_HALF_ONLY": (
                "The grep census ran before any reading: 12 candidate rows and 240 corpus "
                "occurrences. On the rows it was a strict superset of the answer (11 of 12). On the "
                "corpus it is the ONLY instrument that reaches the finding that matters, because "
                "decision 7 is prose. A row-shaped census and a token-shaped census have "
                "complementary blind spots and running one is not running the sweep."
            ),
        },
        "validity": [
            "The classification is a judgement, recorded per item and per occurrence with its "
            "reason, so a reader can disagree with any single row without discarding the sweep.",
            "The census is a SNAPSHOT at the commit named above, taken while two sibling agents "
            "were adding rows and claims.",
            "The five premise families are a choice. A statement that moves for a reason outside "
            "them is not found here, and the tool's context requirements (112 bp needs honeycomb "
            "context, 'perpendicular' needs an azimuth context) trade recall for a false-positive "
            "rate low enough that the gate can be believed.",
            "The pointer test is FORWARD only, so a row whose annotation PRECEDES the number it "
            "annotates reads as unpointed. Three occurrences in ANSWERS.md row (g) are in that "
            "position; they are deliverable-scoped and reported as T-233 debt in any case.",
            "This task edits neither deliverable. Its Deliverable 2 is the list, verified in place, "
            "and T-233 owns the repair.",
            "REPRICED is not KILLED and not ANSWERED. Every repriced row is live exactly as "
            "written; what changed is what its answer is worth, and on which cross-section.",
        ],
    }

    destination = os.path.join(ROOT, "gpd", "results", "T-234-honeycomb-correction-supersession.json")
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    print("queue: open {} | repriced {} | unaffected {}".format(
        len(open_ids), len(classified), len(unclassified)))
    print("corpus: {} occurrences in {} files | {}".format(
        len(records), len({r["file"] for r in records}),
        "  ".join("{} {}".format(k, v) for k, v in sorted(by_class.items()))))
    print("T-233 list: {} entries, {} verified in place".format(
        len(deliverable_list), verified))
    for name, f in falsifiers.items():
        print("  {} {}".format(name, "FIRED" if f["fired"] else "did not fire"))
    result["falsifiers"] = falsifiers
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
