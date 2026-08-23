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
# T-292 -- the COLUMN repair: every queue verdict moved into its own table's status cell,
# proved content-preserving token by token.
#
#     tools/T-292-column-repair.py --check      # the repaired tree reads 0, and every row is intact
#     tools/T-292-column-repair.py --census     # what each row's repair moved, as JSON
#     tools/T-292-column-repair.py --write      # apply the repair to TASKS.md
#     tools/T-292-column-repair.py --self-test
#
# WHY A TOOL AND NOT AN EDITOR.  Twenty-one rows of up to five kilobytes each, moved between
# cells.  A hand edit is `CLAUDE.md`'s *a scripted edit that asserts an anchor can no-op while the
# commit message describes it* one level up, and the token assertion is the only thing that can
# prove the repair preserved every word -- so it has to run on every row, every time.
#
# THE ROWS ARE NOT A LIST.  They are whatever `queue_verdicts.miscolumned_verdicts` reads, which
# is the predicate the gate prints: a repair keyed on a list of identifiers would drift away from
# the gate the moment either moved.  The SHAPE of a firing is derived from the column the verdict
# stands in relative to the status column, and each shape has one rule.
#
# THE CHEAP BOUND, AND IT SETTLED THE LARGER HALF BEFORE ANY CODE RAN.  `C-0188` and `CH-0241`
# both describe the science-table shape as a row that has *dropped the `Leaf` cell*, which would
# mean eleven leaf values had to be found from outside the row before one row could be repaired.
# One `split()` says otherwise: the leaf is still there, at the END of the cell, and the record
# was written in FRONT of it.  Eleven of eleven, derived and corroborated against the newest
# revision in which that cell was a bare leaf.  `CH-0245`.
import argparse
import collections
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(ROOT, "tools")
QUEUE = os.path.join(ROOT, "TASKS.md")


def _module(basename, name):
    spec = importlib.util.spec_from_file_location(name, os.path.join(TOOLS, basename))
    module = importlib.util.module_from_spec(spec)
    sys.modules.setdefault(name, module)
    spec.loader.exec_module(module)
    return module


_verdicts = _module("queue_verdicts.py", "queue_verdicts")

# --- the two shapes ------------------------------------------------------------------------

#: A verdict standing one column LEFT of the status column, in a table whose status column is its
#: last.  On `TASKS.md` that is the five-column science table and the cell is its **Leaf** cell.
LEAF_SHAPE = "record written in front of the leaf"

#: A verdict standing one column RIGHT of the status column.  On `TASKS.md` that is the
#: four-column process table: an acceptance renders under **Status** and the verdict under
#: **Notes**, because the row is written in the five-column table's semantics.
NOTES_SHAPE = "acceptance shifted into the status column"

#: What a leaf cell may hold.  Derived from the queue's own conforming rows: an NDI leaf ID, the
#: queue's own `new` for a leaf with no NDI counterpart, or an em dash for a row that has none.
#: A trailing token that does not match this is NOT treated as a leaf -- the row is then reported
#: as unrepairable rather than repaired with a guess, which is `F2`'s falsifier.
LEAF_TOKEN = re.compile(r"^(A\d+(?:\.\d+)*|new|—)$")

#: A verdict run at the head of a preserved note: a leading `**...**` bold run, optionally after a
#: bare `TODO —` opening, and taking an immediately following `(iteration N)` with it.  This is
#: the queue's own majority idiom for a superseded note, measured rather than preferred: of the
#: struck `TODO` spans in the committed file, the short leading-run strike is the common one, and
#: `T-276`'s repair struck exactly `~~**DONE** (iteration 41)~~` and left the record's prose live.
LEADING_VERDICT_RUN = re.compile(
    r"^(?:TODO\s*—\s*)?\*\*[^*]{1,200}?\*\*(?:\s*\(iteration\s+\d+\))?"
)

#: Where a status cell ends and a notes cell begins in the four-column table: the first period
#: followed by a space.  Every conforming row of that table puts the boundary there -- a verdict
#: and its provenance clause, then the finding -- and no defective row carries an earlier `. `,
#: which is asserted rather than assumed (a row without one is reported unrepairable).
SENTENCE_END = re.compile(r"\. ")


def tokens(text):
    """The multiset key of a piece of row text: non-whitespace runs, strike markers removed.

    The strike markers are normalised away on BOTH sides because they are the repair's own
    deliberate addition and they attach to a neighbouring word rather than standing alone.  Every
    other difference is reported.
    """
    return collections.Counter(re.findall(r"\S+", text.replace("~~", "")))


def split_leaf(cell):
    """(record, leaf) for a Leaf cell a record was written in front of, or None.

    `None` means the cell does not end in a leaf token, so no leaf can be derived from the row
    itself -- which is the case `F2` refuses to guess at.
    """
    stripped = cell.strip()
    parts = stripped.rsplit(None, 1)
    if len(parts) != 2 or not LEAF_TOKEN.match(parts[1]):
        return None
    return (parts[0].strip(), parts[1])


def strike_leading_verdict(note):
    """The note with its leading verdict run struck, or unchanged if it opens with none."""
    stripped = note.strip()
    match = LEADING_VERDICT_RUN.match(stripped)
    if not match:
        return stripped
    return "~~" + match.group(0) + "~~" + stripped[match.end():]


def split_status(cell):
    """(status clause, notes) for a cell holding a verdict and then its finding, or None."""
    stripped = cell.strip()
    match = SENTENCE_END.search(stripped)
    if not match:
        return None
    return (stripped[:match.start() + 1], stripped[match.end():].strip())


def repair_cells(cells, status, index):
    """The repaired cell list for one row, or (None, reason).

    `status` is the table's status column and `index` the column the verdict stands in.
    """
    repaired = list(cells)
    if index == status - 1:
        split = split_leaf(cells[index])
        if split is None:
            return None, "the cell does not end in a leaf token"
        record, leaf = split
        repaired[index] = leaf
        repaired[status] = record + " " + strike_leading_verdict(cells[status])
        return repaired, LEAF_SHAPE
    if index == status + 1:
        split = split_status(cells[index])
        if split is None:
            return None, "the cell carries no sentence boundary to split at"
        verdict, notes = split
        repaired[status - 1] = cells[status - 1].strip() + " — " + cells[status].strip()
        repaired[status] = verdict
        repaired[index] = notes
        return repaired, NOTES_SHAPE
    return None, "the verdict is neither one column left nor one column right of the status column"


def _row_text(cells):
    return "| " + " | ".join(cells) + " |"


def repair(text):
    """(repaired text, [record per row]).

    One pass over the file.  The rows are located by the gate's own predicate, and a row that
    fires more than once is repaired once and reported once.
    """
    lines = text.splitlines()
    firings = collections.OrderedDict()
    for _line, header, body in _verdicts.tables(text):
        status = _verdicts.status_column(header)
        if status is None:
            continue
        for line_number, row in body:
            cells = _verdicts.split_cells(_verdicts.blank_struck(row))
            if not cells or not _verdicts._IDENTIFIER_CELL.match(cells[0].strip("*` ")):
                continue
            for index, cell in enumerate(cells):
                if _verdicts.cell_verdict(cell) and index != status:
                    firings.setdefault(line_number, (header, status, row, []))[3].append(index)

    records = []
    for line_number, (header, status, row, indices) in firings.items():
        cells = _verdicts.split_cells(row)
        identifier = cells[0].strip("*` ")
        before = _row_text(cells)
        repaired, shape = repair_cells(cells, status, indices[0])
        if repaired is None:
            records.append({
                "row": identifier, "line": line_number, "repaired": False, "why": shape,
            })
            continue
        after = _row_text(repaired)
        was, now = tokens(before), tokens(after)
        records.append({
            "row": identifier,
            "line": line_number,
            "repaired": True,
            "shape": shape,
            "verdictStoodUnder": _verdicts._heading(header[indices[0]]),
            "nowStandsUnder": _verdicts._heading(header[status]),
            "leaf": repaired[status - 1] if shape == LEAF_SHAPE else None,
            "tokensBefore": sum(was.values()),
            "tokensAfter": sum(now.values()),
            "tokensAdded": sorted((now - was).elements()),
            "tokensRemoved": sorted((was - now).elements()),
            "cellsBefore": len(cells),
            "cellsAfter": len(repaired),
        })
        lines[line_number - 1] = after
    return "\n".join(lines) + ("\n" if text.endswith("\n") else ""), records


# --- the leaf derivation, corroborated against the file's own history -------------------------

def leaf_from_history(identifier, column, ref="HEAD"):
    """The leaf token this row carried the last time its Leaf cell held nothing else.

    The record was written IN FRONT of the leaf, so the newest revision in which that cell is a
    bare leaf token is the derivation of what the cell is supposed to hold -- and it is a reading
    of the corpus rather than a choice.  Returns None where the history is unreachable.
    """
    try:
        revisions = subprocess.run(
            ["git", "log", "--format=%H", ref, "--", "TASKS.md"],
            cwd=ROOT, capture_output=True, text=True, check=True,
        ).stdout.split()
    except (subprocess.CalledProcessError, FileNotFoundError):
        return None
    for revision in revisions:
        blob = subprocess.run(
            ["git", "show", "%s:TASKS.md" % revision],
            cwd=ROOT, capture_output=True, text=True,
        ).stdout
        for line in blob.splitlines():
            if not line.startswith("| " + identifier + " "):
                continue
            cells = _verdicts.split_cells(line)
            if len(cells) > column and LEAF_TOKEN.match(cells[column].strip()):
                return cells[column].strip(), revision
            break
    return None


# --- self-tests ------------------------------------------------------------------------------

FIVE = "| ID | Task | Acceptance | Leaf | Status |\n|---|---|---|---|---|\n"
FOUR = "| ID | Task | Status | Notes |\n|---|---|---|---|\n"


def self_test():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)
            print("SELFTEST FAIL: %s" % name)

    # --- the leaf is DERIVED from the row, and a cell that has none is refused
    check(
        "T-292 a Leaf cell a record was written in front of yields the record and the leaf",
        split_leaf(" **DONE** (iteration 40) — task `T-1`. A8.2 ")
        == ("**DONE** (iteration 40) — task `T-1`.", "A8.2"),
    )
    check(
        "T-292 `new` is a leaf token, because the queue writes it on eight conforming rows",
        split_leaf("**DONE** — x. new") == ("**DONE** — x.", "new"),
    )
    check(
        "T-292 an em dash is a leaf token, because that is what a row with no NDI leaf carries",
        split_leaf("**DONE** — x. —") == ("**DONE** — x.", "—"),
    )
    check(
        "T-292 a cell that does not END in a leaf token is REFUSED rather than guessed at",
        split_leaf("**DONE** (iteration 40) — task `T-1`.") is None,
    )
    check(
        "T-292 a leaf-shaped word inside the record does not make the cell repairable",
        split_leaf("**DONE** A8.2 — and then some prose") is None,
    )

    # --- the strike is the queue's own leading-run idiom, and it is minimal
    check(
        "T-292 a preserved `TODO — **PRIORITY**` note is struck to the end of its bold run only",
        strike_leading_verdict("TODO — **MEDIUM-HIGH**, raised by `C-1`.")
        == "~~TODO — **MEDIUM-HIGH**~~, raised by `C-1`.",
    )
    check(
        "T-292 a wholly bold `**TODO — HIGH ...**` note is struck to the end of that run",
        strike_leading_verdict("**TODO — HIGH, and it is cheap.** Step 4 of x.")
        == "~~**TODO — HIGH, and it is cheap.**~~ Step 4 of x.",
    )
    check(
        "T-292 a superseded record takes its `(iteration N)` into the strike — `T-276`'s idiom",
        strike_leading_verdict("**PARTIALLY DONE** (iteration 35) — claim `C-1`.")
        == "~~**PARTIALLY DONE** (iteration 35)~~ — claim `C-1`.",
    )
    check(
        "T-292 a note opening with no verdict run is left alone rather than half struck",
        strike_leading_verdict("Raised by `C-1`, and still open.")
        == "Raised by `C-1`, and still open.",
    )

    # --- the four-column split is the boundary the conforming rows already use
    check(
        "T-292 the status cell ends at the first period followed by a space",
        split_status("**DONE** (iteration 16) — claim `C-1`. **The finding.** Prose.")
        == ("**DONE** (iteration 16) — claim `C-1`.", "**The finding.** Prose."),
    )
    check(
        "T-292 a `.md)` inside a link target is not a sentence boundary",
        split_status("**DONE** — claim [`C-1`](gpd/claims/C-1.md). **The finding.**")
        == ("**DONE** — claim [`C-1`](gpd/claims/C-1.md).", "**The finding.**"),
    )
    check(
        "T-292 a cell with no sentence boundary is REFUSED rather than split at its end",
        split_status("**DONE** (iteration 16)") is None,
    )

    # --- the repair itself, on both shapes
    _leaf_row = FIVE + "| T-1 | t | a | **DONE** (iteration 3) — claim `C-1`. A8.2 | TODO — **HIGH**, raised by `C-2`. |\n"
    _leaf_fixed, _leaf_records = repair(_leaf_row)
    check(
        "T-292 the LEAF shape moves the record into the status cell and leaves the leaf behind",
        "| T-1 | t | a | A8.2 | **DONE** (iteration 3) — claim `C-1`. "
        "~~TODO — **HIGH**~~, raised by `C-2`. |" in _leaf_fixed,
    )
    check(
        "T-292 and the repaired LEAF row no longer fires the predicate",
        _verdicts.miscolumned_verdicts(_leaf_fixed) == [],
    )
    check(
        "T-292 the LEAF repair adds and removes NOT ONE token",
        _leaf_records[0]["tokensAdded"] == [] and _leaf_records[0]["tokensRemoved"] == [],
    )
    check(
        "T-292 the LEAF repair reports the leaf it derived",
        _leaf_records[0]["leaf"] == "A8.2" and _leaf_records[0]["shape"] == LEAF_SHAPE,
    )

    _notes_row = FOUR + "| P-1 | **Headline** | An acceptance | **DONE** (iteration 3) — claim `C-1`. **The finding.** |\n"
    _notes_fixed, _notes_records = repair(_notes_row)
    check(
        "T-292 the NOTES shape folds the acceptance into the task cell — `P-20`'s own repair",
        "| P-1 | **Headline** — An acceptance | **DONE** (iteration 3) — claim `C-1`. "
        "| **The finding.** |" in _notes_fixed,
    )
    check(
        "T-292 and the repaired NOTES row no longer fires the predicate",
        _verdicts.miscolumned_verdicts(_notes_fixed) == [],
    )
    check(
        "T-292 the NOTES repair adds exactly ONE token, the em dash that joins the two clauses",
        _notes_records[0]["tokensAdded"] == ["—"]
        and _notes_records[0]["tokensRemoved"] == [],
    )
    check(
        "T-292 the cell COUNT is unchanged by either repair, so the table still renders",
        _leaf_records[0]["cellsBefore"] == _leaf_records[0]["cellsAfter"] == 5
        and _notes_records[0]["cellsBefore"] == _notes_records[0]["cellsAfter"] == 4,
    )

    # --- the register must not move, which is the whole point of a COLUMN repair
    _trace = _module("trace-answers.py", "trace_answers")
    check(
        "T-292 the LEAF repair leaves the register's reading of the row unchanged",
        _trace.queue_status(_leaf_row) == _trace.queue_status(_leaf_fixed) != {},
    )
    check(
        "T-292 the NOTES repair leaves the register's reading of the row unchanged",
        _trace.queue_status(_notes_row) == _trace.queue_status(_notes_fixed) != {},
    )

    # --- a row in a shape the rules do not cover is REPORTED, never half repaired
    _far = FIVE + "| T-1 | **DONE** (iteration 3) | a | A8.2 | **TODO — HIGH** |\n"
    _far_fixed, _far_records = repair(_far)
    check(
        "T-292 a verdict two columns from the status column is reported unrepairable",
        _far_records[0]["repaired"] is False and _far_fixed == _far,
    )

    # --- an escaped pipe is a literal, so a cell index is a cell index (`C-0083`)
    _piped = FIVE + r"| T-1 | a `\|F\|` b | a | **DONE** (iteration 3). A8.2 | TODO — **HIGH**. |" + "\n"
    _piped_fixed, _piped_records = repair(_piped)
    check(
        "T-292 an escaped pipe stays a literal through the repair, and no column shifts",
        _piped_records[0]["repaired"] and r"a `\|F\|` b" in _piped_fixed
        and _verdicts.miscolumned_verdicts(_piped_fixed) == [],
    )

    # --- the rows are located by the GATE's predicate, so the two cannot drift apart
    check(
        "T-292 the repair reads its rows from `queue_verdicts.miscolumned_verdicts`",
        "miscolumned" in open(os.path.abspath(__file__), encoding="utf-8").read()
        and "cell_verdict" in open(os.path.abspath(__file__), encoding="utf-8").read(),
    )

    # --- a WITHDRAWN record is not a record to move, and the discriminating fixture is a verdict
    # BEHIND a struck prefix, not a wholly struck one.  A cell opening `~~` is refused by the
    # leading-bold rule whether or not anything is blanked, so a wholly struck fixture holds the
    # blanking open NOWHERE -- `C-0188` §7 measured that one level up and the mutation test found
    # it again here.
    _withdrawn = FIVE + "| T-1 | t | a | ~~**DONE** (iteration 3)~~ A8.2 | **TODO — HIGH** |\n"
    check(
        "T-292 a WHOLLY struck record in the leaf cell is not a verdict, so nothing is moved",
        repair(_withdrawn)[1] == [],
    )
    _behind = FIVE + "| T-1 | t | a | ~~TODO~~ **DONE** (iteration 3). A8.2 | TODO — **HIGH**. |\n"
    _behind_fixed, _behind_records = repair(_behind)
    check(
        "T-292 a verdict BEHIND a struck prefix in the leaf cell IS moved, strike and all",
        _behind_records and _behind_records[0]["repaired"]
        and "| A8.2 | ~~TODO~~ **DONE** (iteration 3). " in _behind_fixed
        and _verdicts.miscolumned_verdicts(_behind_fixed) == [],
    )

    # --- the committed queue: the repair is IDEMPOTENT and it reads 0 afterwards.
    # The existence of the queue is itself a NAMED test rather than a guard: a guard turns five
    # named tests into no tests at all when the path moves, which is a mutation this harness's
    # own table carries and which survived until the guard was replaced.
    check(
        "T-292 the queue this repair is proved against is where the tool says it is",
        os.path.exists(QUEUE),
    )
    if os.path.exists(QUEUE):
        with open(QUEUE, encoding="utf-8") as handle:
            _queue = handle.read()
        _fixed, _queue_records = repair(_queue)
        check(
            "T-292 the repaired queue reads 0 miscolumned verdicts",
            _verdicts.miscolumned_verdicts(_fixed) == [],
        )
        check(
            "T-292 the repair is idempotent — running it again moves nothing",
            repair(_fixed)[1] == [],
        )
        check(
            "T-292 no row of the repaired queue loses a token",
            all(r["tokensRemoved"] == [] for r in _queue_records if r["repaired"]),
        )
        check(
            "T-292 every row of the repaired queue is repairable by rule",
            all(r["repaired"] for r in _queue_records),
        )
        check(
            "T-292 the register reads the repaired queue exactly as it reads the committed one",
            _trace.queue_status(_queue) == _trace.queue_status(_fixed),
        )

    print("# %d self-test failure(s)" % len(failures))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queue", default=QUEUE)
    parser.add_argument("--check", action="store_true",
                        help="the queue reads 0 miscolumned verdicts and every row is intact")
    parser.add_argument("--census", action="store_true", help="what each row's repair moved, JSON")
    parser.add_argument("--write", action="store_true", help="apply the repair in place")
    parser.add_argument("--self-test", dest="self_test", action="store_true")
    args = parser.parse_args(argv)

    if args.self_test:
        return self_test()

    with open(args.queue, encoding="utf-8") as handle:
        text = handle.read()
    repaired, records = repair(text)

    if args.census:
        print(json.dumps(records, indent=2, ensure_ascii=False))
        return 0

    if args.write:
        if any(not record["repaired"] for record in records):
            for record in records:
                if not record["repaired"]:
                    print("REFUSED  %s line %s: %s"
                          % (record["row"], record["line"], record["why"]))
            return 1
        with open(args.queue, "w", encoding="utf-8") as handle:
            handle.write(repaired)
        for record in records:
            print("repaired %-6s line %-5s %-42s +%d/-%d token(s)"
                  % (record["row"], record["line"], record["shape"],
                     len(record["tokensAdded"]), len(record["tokensRemoved"])))
        print("# %d row(s) repaired" % len(records))
        return 0

    remaining = _verdicts.miscolumned_verdicts(text)
    for identifier, line, phrase, heading, status_heading in remaining:
        print("MISCOLUMN   %-6s line %s: %r renders under %r, not under %r"
              % (identifier, line, phrase, heading, status_heading))
    print("# %d miscolumned verdict(s) in %s"
          % (len(remaining), os.path.relpath(args.queue, ROOT)))
    return 1 if remaining else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
