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
# T-283 -- the RESIDUE predicate's false-positive rate over the queue's own history.
#
#     tools/T-283-residue-history.py [--json] [--limit N]
#
# `P-30` left the residue -- a row whose PROSE carries a closing word that is not its verdict --
# printed and NOT gated, because it *cannot be made clean*: `T-261`'s acceptance criterion quotes
# `ANSWERED`, `UPHELD` and `RESOLVED` as DATA.  That ground was right about the predicate it had
# and not about the question, because those three words are already BACKTICKED in the row as
# committed.  The candidate is one line -- blank inline code spans before the whole-row scan.
#
# `CLAUDE.md` is explicit that a drift checker's FALSE positives cost more than its true ones and
# that this is a RATE, so the measurement is the task and the gate is only its conclusion.  Every
# revision of `TASKS.md` is scanned in BOTH readings, and every distinct row that fires is
# classified BY HAND here, with its reason.  A row that fires and is not in the table makes this
# tool exit 1 -- `C-0176`'s `--check` discipline applied to a history walk, so the measurement
# cannot silently grow a new unexamined firing.
import argparse
import json
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

sys.path.insert(0, HERE)
import queue_verdicts as _verdicts

TRUE_POSITIVE = "TRUE"
FALSE_POSITIVE = "FALSE"

#: Every distinct row that fires the BLANKED predicate anywhere in the queue's history, read by
#: hand, with the closing word it carries and the repair that exists for it.  A repair that would
#: falsify something the row says makes the row a FALSE positive and the gate unbuyable.
CLASSIFICATION = {
    "T-111": (
        TRUE_POSITIVE,
        "`**ANSWERED by C-0053**` in mid-cell prose while the row leads with `TODO`; the row was "
        "later rewritten and the clause is gone",
    ),
    "T-183": (
        TRUE_POSITIVE,
        "*RESOLVED* in ITALICS, quoting what another document said, while the row led with "
        "`TODO`; backticking it is the corpus's own idiom for a quoted token and falsifies "
        "nothing",
    ),
    "T-231": (
        TRUE_POSITIVE,
        "*the honeycomb station lattice was ANSWERED by C-0141* -- the shape `CLAUDE.md` records "
        "verbatim, whose prescribed repair is to lower-case the word",
    ),
    "T-261": (
        TRUE_POSITIVE,
        "its own TITLE read *a challenge the corpus has since ANSWERED*; lower-cased in the "
        "`P-30` document repair, and its three quoted status words are backticked and therefore "
        "blanked",
    ),
    "T-268": (
        TRUE_POSITIVE,
        "*`CH-0207` **CLOSED and REPAIRED*** (a challenge) and *`P1` was found ALREADY "
        "DISCHARGED* (a deliverable), both lower-cased in the `P-30` document repair",
    ),
    "T-272": (
        TRUE_POSITIVE,
        "*`P2` is DISCHARGED over the whole corpus* (a deliverable), lower-cased in the `P-30` "
        "document repair",
    ),
    "T-280": (
        TRUE_POSITIVE,
        "*Candidate 1 ... is **DONE*** (a candidate of a remedy), lower-cased in the `P-30` "
        "document repair",
    ),
}

#: Rows that fire the UNBLANKED predicate and not the blanked one.  Each is a removal the candidate
#: makes, and a removal is correct only where the closing word is genuinely quoted data -- `F4`.
#: A removal that hid a live inconsistency would falsify the candidate.
REMOVALS = {
    "T-256": (
        "the whole firing is a verbatim quotation of a tool's own output line, "
        "`line 965 STALE-OPEN CH-0187 CLOSED`, inside backticks -- quoted data, and the removal "
        "is a TRUE negative",
    ),
    "T-261": (
        "its acceptance criterion quotes `ANSWERED`, `UPHELD` and `RESOLVED` as DATA; this is the "
        "standing counter-example `P-30` left the line advisory for, and the removal is what "
        "makes the gate buyable",
    ),
}


def _residue(text, blank):
    """[(id, verdict phrase, whole-row reading)] under one of the two readings."""
    out = []
    for line in text.splitlines():
        match = _verdicts.TASK_ROW.match(line.strip())
        if not match:
            continue
        body = _verdicts.blank_struck(match.group(2))
        if blank:
            body = _verdicts.blank_code_spans(body)
        verdicts = _verdicts.row_verdicts(match.group(2))
        if not verdicts:
            continue
        scanned = "CLOSED" if _verdicts.UNQUALIFIED_CLOSING_WORD.search(body) else "OPEN"
        if scanned != verdicts[0][1]:
            out.append((match.group(1), verdicts[0][0], scanned))
    return out


def walk(limit=0, ref="HEAD"):
    revisions = subprocess.check_output(
        ["git", "log", "--format=%H", ref, "--", "TASKS.md"], cwd=ROOT, text=True
    ).split()
    if limit:
        revisions = revisions[:limit]
    rows = []
    for sha in revisions:
        text = subprocess.check_output(
            ["git", "show", "%s:TASKS.md" % sha], cwd=ROOT, text=True, errors="replace"
        )
        plain = _residue(text, blank=False)
        blanked = _residue(text, blank=True)
        rows.append(
            {
                "commit": sha[:7],
                "unblanked": [r[0] for r in plain],
                "blanked": [r[0] for r in blanked],
                "removed": sorted({r[0] for r in plain} - {r[0] for r in blanked}),
            }
        )
    return rows


def residue_at(ref):
    """The BLANKED residue of `TASKS.md` as it stands at one commit."""
    text = subprocess.check_output(
        ["git", "show", "%s:TASKS.md" % ref], cwd=ROOT, text=True, errors="replace"
    )
    return [r[0] for r in _residue(text, blank=True)]


def summarise(rows, working_tree):
    unblanked = sum(len(row["unblanked"]) for row in rows)
    blanked = sum(len(row["blanked"]) for row in rows)
    fired = sorted({i for row in rows for i in row["blanked"]})
    removed = sorted({i for row in rows for i in row["removed"]})
    unclassified = [i for i in fired if i not in CLASSIFICATION]
    unexplained = [i for i in removed if i not in REMOVALS]
    false_positives = [i for i in fired if CLASSIFICATION.get(i, (None,))[0] == FALSE_POSITIVE]
    return {
        "revisions": len(rows),
        "rowInstancesUnblanked": unblanked,
        "rowInstancesBlanked": blanked,
        "distinctRowsUnblanked": sorted({i for row in rows for i in row["unblanked"]}),
        "distinctRowsBlanked": fired,
        "rowsTheBlankingRemoves": removed,
        "truePositives": [i for i in fired if CLASSIFICATION.get(i, (None,))[0] == TRUE_POSITIVE],
        "falsePositives": false_positives,
        "falsePositiveRate": (len(false_positives) / len(fired)) if fired else None,
        "unclassified": unclassified,
        "unexplainedRemovals": unexplained,
        "residueOnTheWorkingTree": working_tree,
    }


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--limit", type=int, default=0, help="check only the newest N revisions")
    parser.add_argument("--ref", default="HEAD", help="the corpus state to walk (default HEAD)")
    args = parser.parse_args(argv)

    rows = walk(args.limit, args.ref)
    working = [r[0] for r in _residue(open(os.path.join(ROOT, "TASKS.md"),
                                           encoding="utf-8").read(), blank=True)]
    summary = summarise(rows, working)

    if args.json:
        print(json.dumps({"revisions": rows, "summary": summary}, indent=2))
    else:
        for row in rows:
            if row["blanked"] or row["removed"]:
                print(
                    "%s  fires %-24s removed by the blanking %s"
                    % (row["commit"], ",".join(row["blanked"]) or "-",
                       ",".join(row["removed"]) or "-")
                )
        print(
            "# %d revision(s) of TASKS.md; %d row-instance(s) fire the UNBLANKED predicate over "
            "%d distinct row(s), %d over %d under the BLANKED one"
            % (
                summary["revisions"], summary["rowInstancesUnblanked"],
                len(summary["distinctRowsUnblanked"]), summary["rowInstancesBlanked"],
                len(summary["distinctRowsBlanked"]),
            )
        )
        for identifier in summary["distinctRowsBlanked"]:
            verdict, reason = CLASSIFICATION.get(identifier, ("UNCLASSIFIED", "—"))
            print("#   %-6s %-5s %s" % (identifier, verdict, reason[:120]))
        for identifier in summary["rowsTheBlankingRemoves"]:
            print("#   removed %-6s %s" % (identifier,
                                           REMOVALS.get(identifier, ("UNEXPLAINED",))[0][:120]))
        print(
            "# false positives: %d of %d distinct rows; residue on the working tree: %d row(s)"
            % (len(summary["falsePositives"]), len(summary["distinctRowsBlanked"]),
               len(summary["residueOnTheWorkingTree"]))
        )

    defects = summary["unclassified"] + summary["unexplainedRemovals"]
    for identifier in defects:
        print("UNCLASSIFIED  %s fires or is removed and is in neither hand table" % identifier)
    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
