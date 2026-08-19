#!/usr/bin/env python3
"""T-220 — census and classification of `C-0005`'s 123–214 % one-loop ratio across the corpus.

`CH-0167` establishes that the ratio is an error bar on the **LEVEL** of the electrostatic
force, and that the corpus quotes it as the uncertainty a **stability margin** must be read
against.  This tool

  * reproduces the census mechanically (`--census`),
  * carries the per-occurrence classification as retained data (`tools/T-220-classification.json`),
  * and gates it (`--check`): every occurrence classified `HELD` or `SHAPE` must carry a
    pointer to `CH-0167` within `POINTER_WINDOW` characters of the ratio itself.

The classification is a **reading**, not a derivation, and it is retained as data so that the
reading is inspectable and falsifiable one occurrence at a time.  The four classes are:

  HELD   the qualifier is attached to a stability floor, a coupling margin, a fold/pull-in
         margin, or a window edge built on one — read at the force-pinned operating point,
         where `|F_es| = 100 pN + P(g)A` is fixed by a mechanical balance and a multiplier on
         the level is absorbed into the bias.  **Wrong error bar. Restate or point.**
  SHAPE  the qualifier is attached to a ratio, a departure, a taper, a dishing fraction or a
         comparison between states read on one field, where the level enters as a COMMON
         FACTOR and divides out.  **Also the wrong error bar, for a different reason.**
  LEVEL  the qualifier is attached to a force, a pressure, a bias, a well depth or a decay
         length — a quantity that IS a level.  **Right error bar. Leave alone.**
  META   a sentence about `C-0005`, `CH-0019`, `T-50` or `CH-0167` itself, or a bare `CITED`
         provenance row.  **Not a transfer at all. Leave alone.**
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys

RATIO = re.compile(r"123[–-]214")
POINTER = "CH-0167"
POINTER_WINDOW = 900

#: The corpus `CH-0167` names, and the only corpus this task edits: the 31 claims plus the three
#: documents a reader outside the programme sees.  Deliberately EXCLUDED, with the reason:
#:   `JOURNAL.md`             — an append-only history; rewriting it would falsify the record
#:   `gpd/tasks/`             — a Formulate/Plan record of what was believed when the task was set
#:   `gpd/challenges/`        — including `CH-0167` itself, which states the ratio correctly
#:   `gpd/data/`, `third-party/`, `COMPARISON-p1-p2.md`, `CLAUDE.md`
#: `CLAUDE.md` is excluded because its three occurrences are inside `C-0005`'s own entries, which
#: state the quantity — they are `META` by construction and no banner of it is attached to a margin.
def in_scope(path: str) -> bool:
    return path.startswith("gpd/claims/") or path in (
        "ANSWERS.md",
        "DECISIONS-FOR-NDI.md",
        "TASKS.md",
    )


STRIKE = re.compile(r"~~.*?~~", re.DOTALL)


def struck_spans(text: str) -> list[tuple[int, int]]:
    return [(m.start(), m.end()) for m in STRIKE.finditer(text)]


def is_struck(spans: list[tuple[int, int]], offset: int) -> bool:
    return any(lo <= offset < hi for lo, hi in spans)

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
CLASSIFICATION = os.path.join(HERE, "T-220-classification.json")

CLASSES = ("HELD", "SHAPE", "LEVEL", "META")
ADDRESSED = ("HELD", "SHAPE")


def corpus_files(root: str) -> list[str]:
    """Every tracked markdown file, with a tree walk fallback for a snapshot with no `.git`."""
    try:
        out = subprocess.run(
            ["git", "ls-files", "*.md"], cwd=root, capture_output=True, text=True, check=True
        ).stdout.split()
        if out:
            return sorted(out)
    except (OSError, subprocess.CalledProcessError):
        pass
    found = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in (".git", "build") and not d.startswith("build-")]
        for name in filenames:
            if name.endswith(".md"):
                found.append(os.path.relpath(os.path.join(dirpath, name), root))
    return sorted(found)


def occurrences(text: str) -> list[tuple[int, int, int]]:
    """(index within file, line number, character offset) for every match, in file order."""
    result = []
    for index, match in enumerate(RATIO.finditer(text)):
        line = text.count("\n", 0, match.start()) + 1
        result.append((index, line, match.start()))
    return result


def has_pointer(text: str, offset: int, window: int = POINTER_WINDOW) -> bool:
    """Is a `CH-0167` pointer within `window` characters AFTER the ratio?

    Only forward: a restatement follows the sentence it restates, and a pointer belonging to a
    *previous* occurrence must not discharge the next one.
    """
    return POINTER in text[offset : offset + window]


def census(root: str) -> list[dict]:
    records = []
    for path in corpus_files(root):
        if not in_scope(path):
            continue
        full = os.path.join(root, path)
        try:
            text = open(full, encoding="utf-8").read()
        except OSError:
            continue
        lines = text.split("\n")
        spans = struck_spans(text)
        for index, line, offset in occurrences(text):
            records.append(
                {
                    "file": path,
                    "index": index,
                    "line": line,
                    "pointer": has_pointer(text, offset),
                    "struck": is_struck(spans, offset),
                    "text": lines[line - 1][:400],
                }
            )
    return records


def load_classification(path: str = CLASSIFICATION) -> dict:
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def classify(records: list[dict], table: dict) -> tuple[list[dict], list[str]]:
    """Attach a class to every record. Returns (records, problems)."""
    problems = []
    for record in records:
        entry = table.get(record["file"], {}).get(str(record["index"]))
        if entry is None:
            record["class"] = None
            record["why"] = None
            problems.append(f"unclassified: {record['file']}#{record['index']} line {record['line']}")
            continue
        if entry["class"] not in CLASSES:
            problems.append(f"unknown class {entry['class']!r} at {record['file']}#{record['index']}")
        record["class"] = entry["class"]
        record["why"] = entry["why"]
    seen = {(r["file"], r["index"]) for r in records}
    for path, entries in table.items():
        for index in entries:
            if (path, int(index)) not in seen:
                problems.append(f"stale classification: {path}#{index} has no occurrence")
    return records, problems


def check(root: str) -> int:
    records = census(root)
    records, problems = classify(records, load_classification())
    missing = [
        r for r in records if r["class"] in ADDRESSED and not r["pointer"] and not r["struck"]
    ]
    counts = {c: sum(1 for r in records if r["class"] == c) for c in CLASSES}
    print(f"{len(records)} occurrence(s) in {len({r['file'] for r in records})} file(s)")
    print("  " + "  ".join(f"{c} {counts[c]}" for c in CLASSES))
    for problem in problems:
        print(f"PROBLEM  {problem}")
    for record in missing:
        print(f"UNPOINTED  {record['file']}:{record['line']} #{record['index']} [{record['class']}]")
    print(f"GATE {len(problems) + len(missing)} defect(s)")
    return 1 if problems or missing else 0


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=ROOT)
    parser.add_argument("--census", action="store_true", help="print the raw census as JSON")
    parser.add_argument("--check", action="store_true", help="gate: every HELD/SHAPE carries a pointer")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args(argv)
    if args.self_test:
        return self_test()
    if args.census:
        print(json.dumps(census(args.root), indent=2, ensure_ascii=False))
        return 0
    return check(args.root)


# --------------------------------------------------------------------------- self-tests

def self_test() -> int:
    failures = []

    def check_that(name, condition):
        if not condition:
            failures.append(name)

    # the pattern
    check_that("en dash matches", RATIO.search("123–214 %") is not None)
    check_that("hyphen matches", RATIO.search("123-214 %") is not None)
    check_that("spaced does not match", RATIO.search("123 - 214") is None)
    check_that("other ratio does not match", RATIO.search("123-215") is None)

    # occurrence indexing
    text = "a 123–214 b\nc\nd 123-214 e 123–214 f\n"
    occ = occurrences(text)
    check_that("three occurrences", len(occ) == 3)
    check_that("indices are 0,1,2", [o[0] for o in occ] == [0, 1, 2])
    check_that("lines are 1,3,3", [o[1] for o in occ] == [1, 3, 3])

    # the pointer window is FORWARD only
    forward = "123–214 % (restated, CH-0167)"
    check_that("forward pointer found", has_pointer(forward, forward.index("123")))
    backward = "CH-0167 says so. Later: 123–214 %"
    check_that("backward pointer not found", not has_pointer(backward, backward.index("123–214")))
    far = "123–214 %" + ("x" * 2000) + "CH-0167"
    check_that("pointer beyond the window not found", not has_pointer(far, 0))
    check_that("window boundary is exclusive of the tail", has_pointer("123–214" + "x" * 10 + POINTER, 0))

    # scope
    check_that("a claim is in scope", in_scope("gpd/claims/C-0017-x.md"))
    check_that("ANSWERS is in scope", in_scope("ANSWERS.md"))
    check_that("TASKS is in scope", in_scope("TASKS.md"))
    check_that("DECISIONS is in scope", in_scope("DECISIONS-FOR-NDI.md"))
    check_that("the journal is out of scope", not in_scope("JOURNAL.md"))
    check_that("a task file is out of scope", not in_scope("gpd/tasks/T-50-x.md"))
    check_that("a challenge is out of scope", not in_scope("gpd/challenges/CH-0167-x.md"))
    check_that("CLAUDE.md is out of scope", not in_scope("CLAUDE.md"))

    # struck spans — a withdrawn sentence is addressed by having been struck
    struck_text = "a ~~123–214 % here~~ b 123–214 % there"
    spans = struck_spans(struck_text)
    check_that("first is struck", is_struck(spans, struck_text.index("123")))
    check_that("second is not struck", is_struck(spans, struck_text.rindex("123")) is False)
    check_that("a multi-line strike is one span", len(struck_spans("~~a\nb~~")) == 1)
    check_that("no strike gives no spans", struck_spans("plain") == [])

    # classification bookkeeping
    records = [{"file": "a.md", "index": 0, "line": 1, "pointer": True, "struck": False, "text": ""}]
    table = {"a.md": {"0": {"class": "HELD", "why": "w"}}}
    got, problems = classify([dict(r) for r in records], table)
    check_that("classified cleanly", not problems and got[0]["class"] == "HELD")
    _, problems = classify([dict(r) for r in records], {})
    check_that("unclassified is a problem", any("unclassified" in p for p in problems))
    _, problems = classify([dict(r) for r in records], {"a.md": {"0": {"class": "HELD", "why": "w"}, "1": {"class": "LEVEL", "why": "w"}}})
    check_that("stale classification is a problem", any("stale" in p for p in problems))
    _, problems = classify([dict(r) for r in records], {"a.md": {"0": {"class": "NOPE", "why": "w"}}})
    check_that("unknown class is a problem", any("unknown class" in p for p in problems))

    # the four classes, and that only two of them are gated
    check_that("four classes", set(CLASSES) == {"HELD", "SHAPE", "LEVEL", "META"})
    check_that("LEVEL is not gated", "LEVEL" not in ADDRESSED)
    check_that("META is not gated", "META" not in ADDRESSED)
    check_that("HELD is gated", "HELD" in ADDRESSED)
    check_that("SHAPE is gated", "SHAPE" in ADDRESSED)

    # the retained table is loadable and every entry names a class and a reason
    table = load_classification()
    for path, entries in table.items():
        for index, entry in entries.items():
            check_that(f"{path}#{index} has a class", entry.get("class") in CLASSES)
            check_that(f"{path}#{index} has a reason", bool(entry.get("why")))

    for failure in failures:
        print(f"FAIL  {failure}")
    total = 34 + 2 * sum(len(v) for v in table.values())
    print(f"{len(failures)} failure(s) of {total} check(s)")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
