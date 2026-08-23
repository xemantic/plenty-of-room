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
"""Emit `gpd/results/T-298-a-challenges-status-row-is-the-authority.json`.

    tools/T-298-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS, so it takes the ref as an argument, defaults it to `HEAD`,
and records the **resolved** SHA (`CH-0246`).  The `before` reading is executed against the ref's
own `tools/trace-answers.py`, its own `gpd/challenges/` and its own `gpd/claims/`, so the residue
it records is the one the committed gate could not see rather than the one left afterwards.

No wall-clock and no step counter: every value is an integer count, an identifier, a filename or a
verdict word.
"""

import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(
    ROOT, "gpd", "results", "T-298-a-challenges-status-row-is-the-authority.json"
)
sys.path.insert(0, os.path.join(ROOT, "tools"))
from emission_header import with_emission_header  # noqa: E402

DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")

#: What was WRITTEN into each challenge file, read out of the claim that adjudicates it.  A status
#: is a verdict and a verdict is not a word, so the partial column is as load-bearing as the
#: verdict one: `CH-0101` is discharged in ONE item and stands in the rest.
DISPOSITIONS = (
    ("CH-0004", "C-0008-electrostatic-force-and-decay-length.md", "RESOLVED", "partial",
     "upheld in its consequence and refuted in its magnitude; its own escape clause fired, and "
     "the task that decided it was T-3a rather than the T-4 the cell predicted"),
    ("CH-0010", "C-0077-first-moment-chain-length.md", "UPHELD IN PART", "partial",
     "upheld in substance and split; C-0077 quantifies the challenge's own word `most` at 62-68 % "
     "of the chain-length gap and leaves a physical residue of 1.64-1.95x"),
    ("CH-0033", "C-0017-output-coupling-stiffness.md", "UPHELD", "full",
     "recorded by BOTH targets: C-0017 withdraws its failure route 2 and C-0015 corrects its "
     "validity clause; no verdict, number or table of either moves"),
    ("CH-0056", "C-0042-paired-perpendicular-junction.md", "UPHELD", "partial",
     "upheld at the level it was written and completed by C-0052 and C-0059; the transfer of the "
     "freedom to a multi-junction assembly is refused separately, by CH-0072"),
    ("CH-0078", "C-0070-pinned-leg-budget.md", "UPHELD", "full",
     "nothing softens it, and the challenge's own `what would settle it` item 2 is discharged"),
    ("CH-0083", "C-0084-recommended-element-pull-in-fold.md", "RESOLVED", "full",
     "the third load line is searched and no fold exists at 2 mM at 6 of 6 layer models; every "
     "number in C-0018 and C-0032 still stands"),
    ("CH-0089", "C-0079-unbonded-duplex-separation.md", "UPHELD", "full",
     "and the challenge's own failure route 1 is closed: no equilibrium separation exists at all"),
    ("CH-0093", "C-0085-collinear-stacking-clearance.md", "UPHELD", "full",
     "upheld and closed; the answer lands above the challenge's own generous end"),
    ("CH-0101", "C-0090-buildable-raster-width.md", "DISCHARGED", "partial",
     "discharged in the ONE item it left unevaluated, and its sign guess corrected; the width "
     "statement itself is not adjudicated by that claim and still STANDS"),
    ("CH-0103", "C-0103-path-count-at-fixed-geometry.md", "UPHELD", "partial",
     "upheld as a bookkeeping correction while the recommendation it challenges STANDS; the "
     "challenge's own instrument is separately withdrawn by CH-0119"),
    ("CH-0151", "C-0141-honeycomb-station-lattice-and-placement.md", "OVERTURNED", "full",
     "its 132 and 90 do not hold; C-0122's 90 and 60 are restored at departure 0.0 while every "
     "reason C-0122 gave for them is withdrawn in the same claim"),
    ("CH-0177", "C-0118-coupled-four-layer.md", "UPHELD", "full",
     "recorded in C-0118's own banner; the value survives and only the monotonicity is withdrawn"),
    ("CH-0184", "C-0148-face-bond-class-residues-and-row-span-columns.md", "ANSWERED", "full",
     "the inter-row offset is 14 bp at 32 of 32 proper readings and the saturating pair the "
     "challenge found is withdrawn as unbuildable"),
    ("CH-0185", "C-0148-face-bond-class-residues-and-row-span-columns.md", "ANSWERED", "full",
     "the twelfth column is a bounding-box artefact; the price DECISIONS-FOR-NDI decision 8 "
     "rested on is withdrawn, and the challenge's GROUNDS still stand"),
    ("CH-0229", "C-0182-name-the-discharge.md", "ANSWERED", "full",
     "raised and repaired in C-0176, and the general question it left standing is answered by "
     "C-0182's registry"),
)

#: The two that are NOT adjudications, each with the shape that made it a hit and the guard that
#: removes it.  `C-0197` named one of these and declined to tune it away; `T-298` repairs both,
#: with the false negatives measured over the whole claims corpus BEFORE the guard was written.
EXCLUSIONS = (
    ("CH-0068", "C-0056-connectivity-ceiling-plate.md", "conditional",
     "`If `CH-0068` is upheld, the design point is `N_ret = 56` ... If `CH-0068` is refused, the "
     "thresholds bind` -- a claim that says in as many words that the verdict is not in",
     "a CONDITIONAL is not an adjudication -- it says the verdict is not in"),
    ("CH-0157", "C-0132-cut-rim-charge.md", "clause crossing",
     "`That is `CH-0157`, and it is why the bracket has to be withdrawn` -- the thing withdrawn "
     "is the BRACKET; the clause guard [^.;|] does not stop a comma-and-conjunction",
     "a comma and a coordinating conjunction start a new clause with its own subject"),
)

#: Measured by `tools/T-298-mutation-test.py`, which RUNS them; this file only reports the table.
#: The name is deliberately lower case: `tools/P-31-harness-census.py` discovers a harness by a
#: top-level ALL-CAPS name containing MUTATION, and an emitter that merely reports a mutation table
#: is not a harness -- naming it `MUTATION_DIRECTIONS` made this emitter a second UNDECLARED
#: harness in the census, which is the census being right about a name being wrong.
#: Six of the ten revert a rule and four over-correct it -- `C-0176`'s both-directions standard.
mutation_directions = {
    "the status reader ignores strikes": "revert",
    "the adjudication reader ignores strikes": "revert",
    "the status reader blanks the whole file instead of the cell": "over-correct",
    "the conditional guard is dropped": "revert",
    "the conditional guard is widened until it refuses every site": "over-correct",
    "the clause guard goes back to [^.;|]": "revert",
    "the clause guard is widened to break on a relative `, which`": "over-correct",
    "the cancellations are read on the assertion window": "revert",
    "the cancellations are read on the whole line": "over-correct",
    "the residue is printed but not gated": "revert",
}


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _load_source(name, source, path):
    module = importlib.util.module_from_spec(importlib.util.spec_from_loader(name, loader=None))
    module.__file__ = path
    sys.modules[name] = module
    exec(compile(source, path, "exec"), module.__dict__)
    return module


def _tree(ref, directory):
    """{basename: text} for one directory at one ref."""
    listing = _git("ls-tree", "--name-only", "%s:%s" % (ref, directory))
    out = {}
    for name in listing.split():
        if name.endswith(".md"):
            out[name] = _git("show", "%s:%s/%s" % (ref, directory, name))
    return out


def _residue(module, claims, challenges):
    """[(challenge, claim)] for a {basename: text} claims map and challenges map."""
    adjudicated = {
        re.match(r"(CH-\d{1,4})", name).group(1): module.challenge_adjudicated(text)
        for name, text in challenges.items()
        if re.match(r"CH-\d{1,4}", name)
    }
    found = []
    for name in sorted(claims):
        for identifier, source in module.adjudications_in_claim(name, claims[name]):
            if not adjudicated.get(identifier, False):
                found.append({"challenge": identifier, "claim": source})
    return found


def _index_rows(text, module):
    rows = {}
    for line in text.splitlines():
        match = re.match(r"\|\s*\[`(CH-\d{1,4})`\]", line)
        if not match:
            continue
        cell = module.strip_struck([c.strip() for c in line.strip().strip("|").split("|")][-1])
        if module._CHALLENGE_OPEN.search(cell):
            status = "OPEN"
        elif module._CHALLENGE_CLOSED.search(cell):
            status = "CLOSED"
        else:
            status = "UNKNOWN"
        rows[match.group(1)] = (status, bool(module._ADJUDICATED.search(cell)))
    return rows


def _disagreement(module, challenge_texts, index_text):
    """The index against the challenge FILES, both read at the same state.

    `challenge_texts` is a {basename: text} map rather than a directory, so the `before` reading
    can be taken over the ref's own files -- mixing a ref's index with the working tree's files
    is a census whose two halves are dated differently, which is the very defect this task is
    about, one level up.
    """
    statuses = {}
    adjudications = {}
    for name, text in challenge_texts.items():
        match = re.match(r"(CH-\d{1,4})", name)
        if not match:
            continue
        statuses[match.group(1)] = module.challenge_status_of(text)
        adjudications[match.group(1)] = module.challenge_adjudicated(text)
    index = _index_rows(index_text, module)
    shared = sorted(set(index) & set(statuses))
    status_rows = [k for k in shared if statuses[k] != index[k][0]]
    index_says = [k for k in shared if index[k][1] and not adjudications[k]]
    file_says = [k for k in shared if adjudications[k] and not index[k][1]]
    no_row = sorted(set(statuses) - set(index))
    return {
        "indexedRows": len(index),
        "challengeFiles": len(statuses),
        "withNoIndexRowOfTheirOwn": no_row,
        "withNoIndexRowOfTheirOwnCount": len(no_row),
        "statusDisagreements": status_rows,
        "statusDisagreementCount": len(status_rows),
        "indexRecordsAnAdjudicationTheFileDoesNot": index_says,
        "indexRecordsAnAdjudicationTheFileDoesNotCount": len(index_says),
        "fileRecordsAnAdjudicationTheIndexDoesNot": file_says,
        "fileRecordsAnAdjudicationTheIndexDoesNotCount": len(file_says),
    }


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)
    resolved = _git("rev-parse", args.ref).strip()

    before = _load_source(
        "trace_before", _git("show", "%s:tools/trace-answers.py" % args.ref),
        os.path.join(ROOT, "tools", "trace-answers.py"),
    )
    after = _load("trace_after", "trace-answers.py")

    claims_at_ref = _tree(args.ref, "gpd/claims")
    challenges_at_ref = _tree(args.ref, "gpd/challenges")
    residue_before = _residue(before, claims_at_ref, challenges_at_ref)

    challenges_dir = os.path.join(ROOT, "gpd", "challenges")
    claims_dir = os.path.join(ROOT, "gpd", "claims")
    adjudicated_now = after.challenge_adjudications(challenges_dir)
    residue_after = [
        {"challenge": identifier, "claim": claim}
        for identifier, claim in after.unrecorded_adjudications(claims_dir, adjudicated_now)
    ]

    statuses_now = after.challenge_statuses(challenges_dir)
    stale_now = {
        name: [
            {"line": line, "challenge": identifier, "corpusStatus": status}
            for line, identifier, status in after.stale_challenge_statuses(
                open(os.path.join(ROOT, name), encoding="utf-8").read(), statuses_now
            )
        ]
        for name in DELIVERABLES
    }

    index_now = open(os.path.join(challenges_dir, "README.md"), encoding="utf-8").read()
    document = {
        "task": "T-298",
        "title": (
            "a challenge a claim has adjudicated whose own **Status** row does not say so -- "
            "17 read, 15 annotated, 2 excluded, and the residue promoted to a gate"
        ),
        "subject": (
            "gpd/challenges against gpd/claims, and gpd/challenges/README.md against the "
            "challenge files; a corpus-subject result file, so the ref is an argument and the "
            "resolved SHA is recorded (CH-0246)"
        ),
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "units": (
            "none; every value is an integer count, a challenge identifier, a claim filename or a "
            "verdict word. No physics is computed and no physical number moves"
        ),
        "parameters": {
            "authority": (
                "a challenge's own file, per T-183 -- unchanged by this task. "
                "gpd/challenges/README.md's Status cell is free prose and no gate reads it"
            ),
            "whatWasChangedInTheTool": (
                "(1) challenge_status_of and challenge_adjudicated read the Status CELL with its "
                "struck spans blanked, because C-0071's strike-never-delete is how an "
                "adjudication superseding a filing status is written here; (2) the claim-side "
                "adjudication pattern gains a conditional guard and a comma-plus-coordinating- "
                "conjunction clause guard, both measured before being written; (3) the "
                "_HISTORICAL and _ANSWERING cancellations in stale_challenge_statuses are read on "
                "_VERDICT_WINDOW rather than on _OPEN_WINDOW, which can only remove hits; "
                "(4) the UNRECORDED-ADJUDICATION residue is counted into the exit code"
            ),
            "unchanged": (
                "the task half's _OPEN_WORD_ASSERTION and stale_statuses, _OPEN_WINDOW itself, "
                "_CHALLENGE_OPEN, _CHALLENGE_CLOSED, _ADJUDICATION_WORD, the numeric arm, and the "
                "prices_on_adjudicated residue, which stays ungated"
            ),
            "note": "no wall-clock timing and no step counter is emitted",
        },
        "residue": {
            "before": {
                "sites": residue_before,
                "distinctChallenges": sorted({row["challenge"] for row in residue_before}),
                "siteCount": len(residue_before),
                "challengeCount": len({row["challenge"] for row in residue_before}),
                "howItWasRead": (
                    "the ref's own tools/trace-answers.py over the ref's own gpd/claims and "
                    "gpd/challenges, so the count is the one the committed gate could not see"
                ),
            },
            "after": {
                "sites": residue_after,
                "siteCount": len(residue_after),
                "challengeCount": len({row["challenge"] for row in residue_after}),
                "amongTheSeventeenThisTaskRead": [
                    row for row in residue_after
                    if row["challenge"] in {r["challenge"] for r in residue_before}
                ],
                "note": (
                    "the SEVENTEEN this task read read 0. Any site listed here is a challenge a "
                    "claim landed DURING iteration 47 and whose file has not been annotated -- "
                    "the gate firing on fresh work, which is what it is for. CH-0182 met on this "
                    "task's own census: the corpus moved while the claim reporting it was being "
                    "written, and the count is dated"
                ),
            },
            "isNowAGate": True,
            "whatMakesItGateable": (
                "C-0129's policy is gate what can be made clean. T-261 measured 17 and could not, "
                "so it printed. Every one of the 17 was read against the claim that adjudicates "
                "it: 15 are adjudications and are annotated, 2 are not and are excluded by a "
                "narrowing whose false negatives were counted over the whole claims corpus first"
            ),
        },
        "dispositions": [
            {
                "challenge": identifier,
                "adjudicatingClaim": claim,
                "verdictWritten": verdict,
                "fullOrPartial": extent,
                "whatWasAdjudicatedAndWhatWasNot": note,
            }
            for identifier, claim, verdict, extent, note in DISPOSITIONS
        ],
        "exclusions": [
            {
                "challenge": identifier,
                "claim": claim,
                "shape": shape,
                "sentence": sentence,
                "namedTest": test,
            }
            for identifier, claim, shape, sentence, test in EXCLUSIONS
        ],
        "narrowingCost": {
            "measuredBefore": True,
            "patternOneSitesOverTheWholeClaimsCorpusBefore": 46,
            "patternOneSitesAfter": 43,
            "sitesLost": 3,
            "sitesLostThatAreGenuineAdjudications": 0,
            "whyRelativeWhichIsNotInTheConjunctionList": (
                "a relative clause keeps the challenge as its subject, and C-0182's "
                "`CH-0229, which raised this task, is ANSWERED` is a genuine adjudication that an "
                "over-wide guard loses. Measured: including `which` costs 1 true positive"
            ),
        },
        "cheapBoundRunFirst": {
            "what": (
                "before any file was edited, every candidate status was set to CLOSED in memory "
                "and stale_challenge_statuses was run over both deliverables"
            ),
            "predicted": 1,
            "thePassage": (
                "ANSWERS.md line 964: `(`CH-0083`, raised open in iteration 16 and **RESOLVED in "
                "iteration 17**, below)` -- correct as written. Both cancellations _HISTORICAL "
                "and _ANSWERING are present and both sit outside _OPEN_WINDOW = 24"
            ),
            "resolution": (
                "the sentence is right and the checker's window was wrong, which is C-0115's "
                "discipline read the other way round. A cancellation can only REMOVE a hit, so "
                "reading it on the wider _VERDICT_WINDOW = 80 is strictly a narrowing"
            ),
            "staleAfterEverything": stale_now,
        },
        "strikeAwareness": {
            "finding": (
                "C-0071's strike-never-delete and challenge_status_of were in direct conflict: a "
                "Status row reading `~~**OPEN.** ...~~ **RESOLVED, iteration 43**` was reported "
                "OPEN, so the corpus's own repair idiom left the gate exactly where it was"
            ),
            "preExistingLiveInstance": "CH-0224",
            "corpusRowsCarryingAStruckSpanInTheirStatusCell": 4,
            "rowsWhoseReadingTheBlankingMoves": ["CH-0224"],
            "exposureOfThatMove": (
                "CH-0224 is referenced in neither deliverable, so the flip creates no STALE-OPEN"
            ),
            "onlyTheCellIsBlanked": (
                "blanking the whole file first would let a struck block around the row delete the "
                "row, turning a declared status into an UNKNOWN one -- the direction this checker "
                "must not guess in"
            ),
        },
        "statusCellVocabularyTrap": {
            "finding": (
                "_CHALLENGE_OPEN is `open|raised` case-INSENSITIVE, so a Status cell that uses "
                "either word in ORDINARY PROSE reopens its own challenge. Three cells written in "
                "this task did exactly that -- `DISCHARGED in its open item`, `Raised by C-0142`, "
                "`raised and repaired in the same claim` -- and each was rephrased"
            ),
            "cellsRephrased": ["CH-0101", "CH-0177", "CH-0229"],
            "sameFamilyAs": (
                "queue_status matching DONE inside `Left undone`, which the corpus solves with "
                "case sensitivity; the challenge half cannot, because its cell is a declaration "
                "written in mixed case"
            ),
        },
        "indexVersusFiles": {
            "before": _disagreement(
                before,
                {n: t for n, t in challenges_at_ref.items() if n != "README.md"},
                challenges_at_ref.get("README.md", ""),
            ),
            "after": _disagreement(
                after,
                {
                    name: open(os.path.join(challenges_dir, name), encoding="utf-8").read()
                    for name in sorted(os.listdir(challenges_dir))
                    if name.endswith(".md") and name != "README.md"
                },
                index_now,
            ),
            "reconciled": {
                "indexRowsUpdatedToMatchTheFile": [
                    "CH-0033", "CH-0056", "CH-0078", "CH-0083", "CH-0089", "CH-0093", "CH-0101",
                    "CH-0103", "CH-0151", "CH-0177", "CH-0184", "CH-0185", "CH-0229",
                ],
                "filesUpdatedToMatchTheIndex": ["CH-0003", "CH-0007", "CH-0016", "CH-0160"],
                "missingIndexRowAdded": ["CH-0053"],
                "reconciledRowCount": 18,
                "howCH0053WasInvisible": (
                    "tools/check-challenge-index.py's row pattern is unanchored and applied over "
                    "the whole file, so CH-0062's row LINKING to CH-0053 counted as CH-0053 being "
                    "indexed. Measured over the index's own history, CH-0053 is linked-but-rowless "
                    "in 38 revisions of gpd/challenges/README.md, oldest 5ea5c137 (2026-08-14), "
                    "and the gate reported `0 unindexed` on every one. Raised as CH-0255"
                ),
            },
            "notReconciledAndWhy": {
                "CH-0202": (
                    "the index's UPHELD is about C-0151's VERDICT being upheld, not about the "
                    "challenge being adjudicated; the file is right and the index's word is the "
                    "loose one. Reported, not changed"
                ),
                "CH-0251": "owned by another agent this iteration; not touched",
                "fileSaysIndexDoesNot": (
                    "8 rows remain in that direction, all pre-existing and none created here; "
                    "they are listed under `after` and are a separate delta"
                ),
            },
            "aCensusIsDatedByItsPredicate": (
                "T-298's row names SIX in the index-says direction -- CH-0003, CH-0004, CH-0007, "
                "CH-0010, CH-0016, CH-0202. Re-derived at the ref, the ADJUDICATION-WORD "
                "predicate gives EIGHT: those six plus CH-0160 and CH-0251, both of which the "
                "row's reading predates. The STATUS predicate gives a different set again -- "
                "CH-0003, CH-0004, CH-0005, CH-0007, CH-0012, CH-0016 -- because "
                "challenge_status_of is case-INSENSITIVE and challenge_adjudicated is not, so "
                "CH-0010's `Upheld in substance` is CLOSED to one and unadjudicated to the other. "
                "Neither number is wrong; both are reported, with the predicate that produced "
                "them named. Sixth consecutive iteration of CH-0182"
            ),
        },
        "mutationCoverage": {
            "harness": "tools/T-298-mutation-test.py",
            "mutations": len(mutation_directions),
            "survivors": 0,
            "directions": mutation_directions,
            "baselineIsAsserted": (
                "the UNMUTATED copy is run first and its named failures printed; C-0185/CH-0237, "
                "without which a fixture defect reads as 0 survivors or as all-killed and the "
                "headline column means nothing either way"
            ),
            "anchorCountIsAsserted": True,
            "notYetDeclaredInP31": (
                "tools/P-31-harness-census.py's HARNESSES table does not carry a row for this "
                "harness and T-298 does not own tools/P-31-*. The row it wants is "
                "(\"T-298-mutation-test.py\", \"TEXT-ANCHOR\", \"name_file_old_new\", "
                "(\"trace-answers.py\",)). Reported to the coordinator"
            ),
        },
        "namedTests": {
            "atTheRef": 151,
            "now": 167,
            "added": 16,
            "howCounted": (
                "`tools/test-trace-answers.py | grep -c '^ok'`, run at the ref out of "
                "`git archive <ref> tools` and again in the working tree"
            ),
        },
        "gateReadings": {
            "toolsTraceAnswersExitCode": 0,
            "unrecordedAdjudicationBefore": len(residue_before),
            "unrecordedAdjudicationAfter": len(residue_after),
        },
        "maturity": (
            "TRL 1-3 process artifact. NO PHYSICS CHANGED: every edit is a status word, a "
            "pointer, a regular-expression guard or a test"
        ),
    }
    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(with_emission_header(document, "none", regime=[]), handle, indent=2)
        handle.write("\n")
    print("written to %s" % os.path.relpath(RESULT, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
