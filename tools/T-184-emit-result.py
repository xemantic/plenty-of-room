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
# T-184 -- audit of DECISIONS-FOR-NDI.md's "X has not been done" assertions.
#
#     tools/T-184-emit-result.py [--out gpd/results/T-184-decision-file-drift.json]
#
# The adjudication table below is a RECORDED JUDGEMENT, one row per assertion, each naming the
# artifact that decides it.  Nothing here is computed physics.  What IS computed, and re-computed
# on every run, is the cheap-bound grep count and the queue status of every task the decision file
# names -- so a later run of this script re-checks the mechanical half against the live TASKS.md
# and fails loudly if the two disagree with what was recorded.
#
# No wall-clock timing and no step count is emitted: CLAUDE.md forbids both.
import argparse
import json
import os
import re
import subprocess
import sys

DECISIONS = "DECISIONS-FOR-NDI.md"
TASKS = "TASKS.md"

# The declared cheap-bound phrasing set.  Reported BEFORE anything was read (see the task file).
CHEAP_BOUND_PATTERN = (
    r"(has|have|had) (never|not) been|nothing (in this programme|here|else)|"
    r"this programme (cannot|has not|can not)|(we|programme) cannot|"
    r"cannot be (scoped|priced|answered|either)|is (still )?not given|"
    r"has not been (run|taken|answered|evaluated)|no claim (here|in this)|is not one|"
    r"never been taken|not warranted|is still open|has never|is unmeasured|still unmeasured|"
    r"no unspent|only unspent|first unspent|no column at all|has no (checker|column)"
)

# Verdict vocabulary.  STALE is the one that costs an edit.
VERDICTS = {
    "STANDS",              # checked against the corpus/queue and still true
    "STALE",               # contradicted, and the file carries no correction anywhere
    "SUPERSEDED-IN-FILE",  # contradicted, and the correction is elsewhere in the SAME section, unstruck here
    "PATCHED-ELSEWHERE",   # contradicted, and the correction is elsewhere in the file
    "SELF-CONTRADICTION",  # the file asserts both sides
    "UNDER-CHALLENGE",     # the number is carried by an OPEN challenge that names this file
    "ALREADY-STRUCK",      # corrected by an earlier pass
}

SHAPES = {"CORPUS", "QUEUE", "COUNT", "SPEC-BOUNDARY"}

# id, line, section, shape, quote, verdict, decidedBy, mechanisable, correction
ASSERTIONS = [
    ("A01", 3, "head", "COUNT",
     "Six decisions this programme cannot make for itself.",
     "STALE", "the file's own §7 and at-a-glance row 7", True,
     "struck; the file carries seven, six answered and one outstanding"),
    ("A02", 5, "head", "COUNT",
     "ALL SIX ARE ANSWERED",
     "STALE", "§7, raised iteration 26 and unanswered", True,
     "qualified to 'all six of the six that were sent'"),
    ("A03", 8, "head", "CORPUS",
     "Nothing has been re-derived yet.",
     "STALE", "C-0109, C-0110, C-0111, C-0114, C-0116, C-0118, C-0119, C-0120, C-0122, C-0123", False,
     "struck; ten claims across iterations 23-26 re-derived the line the answers opened"),
    ("A04", 9, "head", "QUEUE",
     "queues the work they open as T-191-T-194",
     "STALE", "TASKS.md: T-191/192/193/194 all DONE, and the set is T-191-T-195", True,
     "struck; four are DONE and the fifth, T-195, was never named here"),
    ("A05", 19, "head", "QUEUE",
     "They are live in TASKS.md as T-63, T-115, P-13, T-112, T-154 and T-166",
     "STALE", "TASKS.md: all six ANSWERED; and the file now carries seven", True,
     "struck; none is live and the count is seven"),
    ("A06", 81, "head", "QUEUE",
     "it is queued as T-191, and it is the first unspent design axis since iteration 20",
     "STALE", "TASKS.md T-191 DONE (iteration 23), claim C-0109", True,
     "struck; T-191 ran and the axis is spent"),
    ("A07", 112, "at-a-glance", "CORPUS",
     "the PZC is still not given",
     "SELF-CONTRADICTION", "§3's own residue subsection and C-0111", False,
     "qualified: NDI did not give it, the literature does (0.46-0.51 V vs SHE), "
     "and the surviving ask is the cell's definition of zero, not the material's PZC"),
    ("A08", 98, "how to read", "COUNT",
     "A one-sentence answer is enough for all six.",
     "PATCHED-ELSEWHERE", "the UPDATED paragraph three lines below", True,
     "left; the patch is adjacent and explicit"),
    ("A09", 144, "1", "SPEC-BOUNDARY",
     "this programme cannot argue that its own 1.35-1.75x is the better use of it",
     "STANDS", "C-0091, C-0114 -- there is no fabrication-cost column", False, ""),
    ("A10", 168, "1", "QUEUE",
     "a census ... has never been taken and is queued as T-156",
     "ALREADY-STRUCK", "TASKS.md T-156 DONE (iteration 18), claim C-0091", True, ""),
    ("A11", 189, "1", "QUEUE",
     "T-50 is the only thing in the queue that would change that",
     "STANDS", "TASKS.md T-50 TODO HIGH; CH-0019", True, ""),
    ("A12", 251, "2", "CORPUS",
     "The reason given is a real objection and this programme has not answered it.",
     "SUPERSEDED-IN-FILE", "the T-192 block 14 lines below; C-0110", False,
     "struck in place"),
    ("A13", 253, "2", "CORPUS",
     "no claim here has ever evaluated the bias that would deliver §3's 100 pN across one",
     "SUPERSEDED-IN-FILE", "C-0110 (T-192)", False, "struck in place"),
    ("A14", 258, "2", "QUEUE",
     "That is T-192, it runs on C-0008's existing machinery, and it decides whether the reserve is worth spending",
     "SUPERSEDED-IN-FILE", "TASKS.md T-192 DONE; C-0110", True, "struck in place"),
    ("A15", 303, "2", "CORPUS",
     "But nothing in this programme has evaluated a layer that tall.",
     "STALE", "C-0110 -- 96 states, 12 (gap, buffer) cells, on C-0008's own solver", False,
     "struck; this is the headline instance and it sits in the body a reviewer reads, not in an answer block"),
    ("A16", 311, "2", "CORPUS",
     "This is the only route in the programme that can buy the desired stroke, so while it is open the "
     "desired-stroke branch cannot be either pursued or closed.",
     "STALE", "C-0110 ('no ruling is needed on this decision'), CH-0127", False,
     "struck; the branch is closed by measurement and buys neither clause"),
    ("A17", 305, "2", "CORPUS",
     "Why a no is as useful as a yes",
     "SUPERSEDED-IN-FILE", "C-0110 -- no ruling is needed at all", False, "struck"),
    ("A18", 397, "3", "QUEUE",
     "Queued as T-193; if the literature does not pin a template-stripped gold PZC ... comes back as a one-line ask",
     "PATCHED-ELSEWHERE", "TASKS.md T-193 DONE (iteration 23); C-0111; §3's residue subsection", True,
     "pointer added to the residue subsection"),
    ("A19", 412, "3", "SPEC-BOUNDARY",
     "What an answer needs to be. A material ... If the material is genuinely not yet chosen, saying so is also an answer",
     "PATCHED-ELSEWHERE", "the ANSWERED block above -- template-stripped gold", False, "struck"),
    ("A20", 468, "4", "CORPUS",
     "whether it exists is one evaluation of |k_eff| in a corner nobody has evaluated. T-192, and it is cheap.",
     "STALE", "C-0110 -- device B admitted at 1 of 96 states, refused at 96 of 96 on the acceptable clause", False,
     "struck; §4 never recorded T-192's outcome, so decision 4 still reads as if device B were open"),
    ("A21", 521, "5", "QUEUE",
     "and the Winkler reach is their fourth root. T-191.",
     "STALE", "TASKS.md T-191 DONE (iteration 23); C-0109", True, "struck and pointed at C-0109"),
    ("A22", 561, "5", "QUEUE",
     "T-153 -- a re-read of every plan margin at 38.08 nm -- cannot be scoped, because whether it is "
     "needed at all depends on this answer",
     "STALE", "TASKS.md T-153 DONE (iteration 18), claim C-0090", True,
     "struck; it was scoped and done five iterations before this file was last edited"),
    ("A23", 552, "5", "CORPUS",
     "And a seamless raster quantises the tile WIDTH at 32 bp. Admissible row lengths are odd multiples of 16 bp",
     "STALE", "C-0119 -- that is the SQUARE lattice; the honeycomb quantises its half turn at 5 bp (7k +/- 5)", False,
     "domain qualifier added; the verdict survives on a different ground"),
    ("A24", 556, "5", "SPEC-BOUNDARY",
     "What the programme would recommend. A purpose-length scaffold",
     "PATCHED-ELSEWHERE", "the ANSWERED block above -- declined implicitly", False, "struck"),
    ("A25", 582, "6", "QUEUE",
     "C-0006's four-layer row moves both bending lengths by a fourth root of 167x and 5.75x. T-191 runs it.",
     "STALE", "TASKS.md T-191 DONE; C-0109; and the pair is under CH-0124 (OPEN)", True,
     "struck and replaced by C-0109's calibrated reading"),
    ("A26", 587, "6", "CORPUS",
     "a buildable four-layer body reads 0.100166871 where the rigid limit reads 0.0344",
     "UNDER-CHALLENGE", "CH-0125 (OPEN), which names DECISIONS-FOR-NDI.md (twice) as a carrier", True,
     "flagged in place; the re-solve is owed and is not performed here"),
    ("A27", 608, "6", "CORPUS",
     "A buildable body is not the rigid one: a four-layer honeycomb brick reads 0.100166871, worse than the array",
     "UNDER-CHALLENGE", "CH-0125 (OPEN)", True, "flagged in place"),
    ("A28", 619, "6", "CORPUS",
     "the programme now has no unspent design axis at all, only a fabrication yield",
     "SELF-CONTRADICTION", "line 81 of the same file ('the first unspent design axis since iteration 20'), and C-0109-C-0123", False,
     "struck; the body axis was unspent when this was written and has since been spent"),
    ("A29", 622, "6", "CORPUS",
     "what would move it is the per-site incorporation measurement ... rather than another coupling design",
     "STALE", "C-0118 -- what moved it was the CROSS-SECTION; C-0122 on the real station lattice", False,
     "struck; 10x6 is flat at 8 of 8 cells at the 90th percentile under the measured dropout"),
    ("A30", 78, "head", "CORPUS",
     "a four-layer honeycomb sheet has D_par = 14 310.78 pN.nm against 85.50 and D_perp >= 19.222 against 3.345",
     "UNDER-CHALLENGE", "CH-0124 (OPEN) -- the pair is a MIXED state, not a bracket end; C-0109 measures 4 547.18 / 240.93", True,
     "flagged and replaced by C-0109's calibrated pair"),
    ("A31", 363, "1+2", "SPEC-BOUNDARY",
     "We can rank what the two spends buy ... and no column at all for fabrication cost",
     "STANDS", "C-0114; the standing CLAUDE.md rule about ranking buys rather than costs", False, ""),
    ("A32", 678, "discharged", "QUEUE",
     "T-95 and T-102 no longer apply",
     "STANDS", "TASKS.md: both DISCHARGED", True, ""),
    ("A33", 691, "resources", "CORPUS",
     "Two sources would convert a bound into a number and neither is reachable by an automated fetch",
     "STANDS", "TASKS.md open-question 1: Lee et al. was struck, these two were not", False, ""),
    ("A34", 707, "resources", "QUEUE",
     "T-51 is not warranted; T-50 remains warranted",
     "STANDS", "TASKS.md: T-51 TODO low, T-50 TODO HIGH", True, ""),
    ("A35", 103, "how to read", "SPEC-BOUNDARY",
     "Nothing here is measured. Every number is TRL 1-3",
     "STANDS", "the project invariant in SESSION-PROMPT.md", False, ""),
    ("A36", 116, "at-a-glance", "QUEUE",
     "decision 7's owner task is T-199",
     "STANDS", "TASKS.md T-199 DONE; the live specification carrier is open-question item 12", True,
     "note added: the owner column of row 7 names the ANALYSIS task, where rows 1-6 name specification tasks"),
]

# The one numeric token the existing tracer reports as ABSENT on this file.
ABSENT_TOKENS = [
    {
        "line": 70,
        "token": "9.61",
        "cited": "C-0086, C-0006, C-0058",
        "verdict": "NOW OWNED, AND REPAIRED",
        "owner": "C-0109",
        "repair": "the live text now reads 9.608 nm and cites C-0109; the withdrawn 9.61 is struck "
                  "and, since strip_struck() also runs before the numeric trace, no longer asks "
                  "for a provenance no claim can supply",
        "note": "the four-layer honeycomb thickness, 9.608 nm in C-0109's own variant table; "
                "it was the file's arithmetic when written and has had an owner since iteration 23",
    }
]


def cheap_bound_count(root):
    path = os.path.join(root, DECISIONS)
    with open(path, encoding="utf-8") as handle:
        text = handle.read()
    pattern = re.compile(CHEAP_BOUND_PATTERN, re.IGNORECASE)
    return sum(1 for line in text.splitlines() if pattern.search(line))


def referenced_tasks(root):
    """Every T-/P- id the decision file names, in first-appearance order."""
    with open(os.path.join(root, DECISIONS), encoding="utf-8") as handle:
        text = handle.read()
    seen = []
    for match in re.finditer(r"`((?:T|P)-\d+[a-z]?)`", text):
        if match.group(1) not in seen:
            seen.append(match.group(1))
    return seen


_STATUS_WORDS = ("DONE", "ANSWERED", "DISCHARGED", "KILLED", "TODO", "IN PROGRESS")


def queue_status(root, task):
    """The status word TASKS.md carries for a task row, or None."""
    with open(os.path.join(root, TASKS), encoding="utf-8") as handle:
        for line in handle:
            if not line.startswith("|"):
                continue
            cells = [cell.strip() for cell in line.split("|")]
            head = cells[1] if len(cells) > 2 else ""
            if head.strip("* `") != task:
                continue
            rest = "|".join(cells[2:])
            for word in _STATUS_WORDS:
                if re.search(r"\b" + re.escape(word) + r"\b", rest):
                    return word
    return None


def git_head(root):
    try:
        return subprocess.check_output(
            ["git", "-C", root, "rev-parse", "--short", "HEAD"], text=True
        ).strip()
    except Exception:
        return "unknown"


def build(root):
    counts = {}
    for row in ASSERTIONS:
        verdict = row[5]
        assert verdict in VERDICTS, verdict
        assert row[3] in SHAPES, row[3]
        counts[verdict] = counts.get(verdict, 0) + 1

    tasks = referenced_tasks(root)
    statuses = {task: queue_status(root, task) for task in tasks}

    assertions = [
        {
            "id": row[0],
            "line": row[1],
            "section": row[2],
            "shape": row[3],
            "assertion": row[4],
            "verdict": row[5],
            "decidedBy": row[6],
            "mechanisable": row[7],
            "correction": row[8],
        }
        for row in ASSERTIONS
    ]

    mechanisable = sum(1 for row in ASSERTIONS if row[7])
    needing_edit = sum(
        1 for row in ASSERTIONS
        if row[5] in ("STALE", "SUPERSEDED-IN-FILE", "SELF-CONTRADICTION", "UNDER-CHALLENGE")
    )
    mechanisable_and_stale = sum(
        1 for row in ASSERTIONS
        if row[7] and row[5] in ("STALE", "SUPERSEDED-IN-FILE", "SELF-CONTRADICTION", "UNDER-CHALLENGE")
    )

    return {
        "task": "T-184",
        "claim": "C-0124",
        "title": "DECISIONS-FOR-NDI.md has the same drift class the deliverable had",
        "maturity": "below TRL 1-3: nothing here is physics; no number is derived",
        "gitHead": git_head(root),
        "cheapBound": {
            "pattern": CHEAP_BOUND_PATTERN,
            "matchingLinesBeforeRepair": 31,
            "matchingLinesNow": cheap_bound_count(root),
            "note": "31 is the recorded reading, taken before the file was read, and the enumerated "
                    "set of 36 is a superset of it. The live recount is higher because every "
                    "correction is a struck sentence PLUS a replacement, and both carry the "
                    "phrasings -- which is the cost of 'strike, never delete' and the reason the "
                    "grep is a cheap bound rather than a metric.",
        },
        "assertionCounts": counts,
        "assertionsEnumerated": len(ASSERTIONS),
        "assertionsNeedingAnEdit": needing_edit,
        "assertionsMechanisable": mechanisable,
        "assertionsMechanisableAndStale": mechanisable_and_stale,
        "assertions": assertions,
        "referencedTasks": [
            {"task": task, "queueStatus": statuses[task] or "NOT FOUND"} for task in tasks
        ],
        "absentNumericTokens": ABSENT_TOKENS,
        "checkers": {
            "note": "tools/trace-answers.py runs four checks per document; the other three tools "
                    "are whole-tree and are reported as their own totals.",
            "before": {
                "ANSWERS.md": {
                    "tokens": 1254, "cited": 1132, "elsewhere": 122, "absent": 0,
                    "openAssertionsContradicted": 0, "staleChallengeAssertions": 0,
                    "selfContradictions": 0,
                },
                "DECISIONS-FOR-NDI.md": {
                    "note": "not checked by anything; this reading was taken by pointing the "
                            "tracer at it by hand, which is the whole finding",
                    "tokens": 394, "cited": 246, "elsewhere": 147, "absent": 1,
                    "openAssertionsContradicted": 3, "staleChallengeAssertions": 0,
                    "selfContradictions": 0,
                },
                "checkMarkdownTables": {"defects": 0, "files": 375},
                "checkCorpusLinks": {"brokenLinks": 0, "files": 366,
                                     "note": "it scanned gpd/ only, so NEITHER outward-facing "
                                             "root document's claim links were checked either"},
                "testTraceAnswers": {"checks": 83, "failures": 0},
                "checkCorpusLinksSelfTest": {"checks": 11, "failures": 0},
            },
            "after": {
                "ANSWERS.md": {
                    "tokens": 1251, "cited": 1120, "elsewhere": 131, "absent": 0,
                    "openAssertionsContradicted": 0, "staleChallengeAssertions": 0,
                    "selfContradictions": 0,
                    "movement": "3 tokens dropped (they were struck) and 9 moved CITED -> ELSEWHERE "
                                "(their block's citation sat inside a struck span). 0 moved into "
                                "ABSENT, so no number became untraceable and no verdict moved.",
                },
                "DECISIONS-FOR-NDI.md": {
                    "tokens": 436, "cited": 282, "elsewhere": 154, "absent": 0,
                    "openAssertionsContradicted": 0, "staleChallengeAssertions": 0,
                    "selfContradictions": 0,
                },
                "checkMarkdownTables": {"defects": 0, "files": 379},
                "checkCorpusLinks": {"brokenLinks": 0, "files": 370,
                                     "note": "ROOT_DOCUMENTS added; ANSWERS.md and "
                                             "DECISIONS-FOR-NDI.md are now scanned and both are "
                                             "clean, so the gap cost nothing THIS time"},
                "testTraceAnswers": {"checks": 101, "failures": 0},
                "checkCorpusLinksSelfTest": {"checks": 19, "failures": 0},
            },
        },
        "checkerDecision": {
            "verdict": "NO NEW CHECKER. The existing tracer already reads this file; what was missing "
                       "was that anybody pointed it at it.",
            "evidence": "tools/trace-answers.py --answers DECISIONS-FOR-NDI.md runs unmodified and "
                        "reports 3 open assertions contradicted by TASKS.md and 1 ABSENT numeric token, "
                        "with zero code changes.",
            "shipped": "two changes to tools/trace-answers.py, tests first. (1) DEFAULT_DOCUMENTS "
                       "= [ANSWERS.md, DECISIONS-FOR-NDI.md], --answers now nargs=+, every output "
                       "row tagged with its document. (2) strip_struck(): a ~~withdrawn~~ span is "
                       "blanked before all four checks, preserving length and line numbers -- "
                       "without it the ONLY repair this project permits (strike, never delete) "
                       "leaves every flag exactly where it was, so the checker penalised the "
                       "discipline it exists to support. 18 new self-tests, 83 -> 101. "
                       "AND a third, in tools/check-corpus-links.py: it scanned gpd/ only, so a "
                       "mistyped claim slug in either outward-facing document was invisible to "
                       "it. ROOT_DOCUMENTS added, 8 new self-tests, 11 -> 19; both documents are "
                       "clean, so the gap had not yet cost anything.",
            "notMechanisable": "the CORPUS class -- 'nothing in this programme has evaluated a layer "
                               "that tall' names no task and no number, so no corpus comparison can "
                               "reach it. That is C-0067's superseded-standing-value class verbatim, "
                               "and its exact check still needs a superseded-by edge at STATEMENT "
                               "granularity that no claim carries.",
            "nearestApproximation": "flag any sentence containing a negative-existence phrase whose "
                                    "section also contains a claim filed after the sentence's own date. "
                                    "Measured recall on this audit: 9 of 13 CORPUS-class defects. "
                                    "Its false-positive rate is UNMEASURED, and C-0067's standing rule "
                                    "is that an unmeasured false-positive rate is what makes a checker "
                                    "stop being believed -- so it is deliberately NOT shipped.",
        },
    }


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=".")
    parser.add_argument("--out", default="gpd/results/T-184-decision-file-drift.json")
    args = parser.parse_args(argv)
    record = build(args.root)
    out = os.path.join(args.root, args.out)
    with open(out, "w", encoding="utf-8") as handle:
        json.dump(record, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("# %d assertions, %d needing an edit, %d mechanisable"
          % (record["assertionsEnumerated"],
             record["assertionsNeedingAnEdit"],
             record["assertionsMechanisable"]))
    print("# cheap-bound grep: %d matching lines before the repair, %d now"
          % (record["cheapBound"]["matchingLinesBeforeRepair"],
             record["cheapBound"]["matchingLinesNow"]))
    for verdict in sorted(record["assertionCounts"]):
        print("#   %-20s %d" % (verdict, record["assertionCounts"][verdict]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
