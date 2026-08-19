#!/usr/bin/env python3
"""T-234 — census of the premises `C-0140` and `C-0141` withdrew, across the corpus.

Iteration 33 filed two structural corrections and neither swept what it moved:

  * `C-0141` — the cross-section every four-layer claim is written on is **not a honeycomb**
    (every four-layer `edgeY` is exactly 1.5x too small, the footprint ordering between
    `15 x 4` and `10 x 6` REVERSES, and `C-0116`'s interlayer-coupling threshold for
    `15 x 4` moves inside the measured band); and a honeycomb face carries exactly ONE
    rooting azimuth per helix, at 30 degrees, with NO perpendicular root anywhere.
  * `C-0140` — a honeycomb x-raster carries BOTH turn senses, so there is no uniform
    honeycomb row length at all, and design (i) is a p7560 design.

`CLAUDE.md`: *a discharge is invisible to whoever files the removal.*  This tool is the
mechanical half of the sweep.  It

  * reproduces the census by regular expression (`--census`), over five named premise
    families, so the denominator is derived rather than remembered;
  * carries the per-occurrence classification as retained data
    (`tools/T-234-classification.json`), because the class is a READING and must be
    inspectable and falsifiable one occurrence at a time;
  * and gates it (`--check`): every occurrence classified `MOVED` or `DISCHARGED` must
    carry a pointer to the claim or challenge that moved it, within `POINTER_WINDOW`
    characters, or be struck.

The five families:

  FOOTPRINT   a four-layer cross-section's plan geometry, or a dishing/threshold number
              solved on it.  `C-0141` moves every one of them.
  WIDTH       a honeycomb row length asserted as a uniform tile width.  `C-0140` shows
              no uniform honeycomb row length exists.
  AZIMUTH     a perpendicular rooting azimuth on a honeycomb face, or the 60-degree
              azimuth, or `CH-0151`'s 132/90 census.  `C-0141` withdraws all three.
  SCAFFOLD    the scaffold design (i) is folded from.  `C-0140` derives p7560 where the
              corpus carries p8064.
  PLACEMENT   a statement that this repository has no honeycomb station lattice, plan
              ceiling or placement family.  `C-0141` supplies all three.

The four classes:

  MOVED        the passage asserts a premise the two claims withdrew.  **Strike or point.**
  DISCHARGED   the passage asserts the ABSENCE of something `C-0141` now supplies.
               **Also strike or point** -- the same repair, the opposite direction.
  RECORD       a synthesis claim recording what a past pass carried, or a verbatim source
               quotation.  The sentence was true when written and is a historical record.
               **Leave alone, with the reason recorded.**
  CORRECT      the occurrence is inside the correcting claim or challenge itself, or it
               states the corrected value.  **Leave alone.**
  OUT_OF_SCOPE the token matched and the statement is about something else -- a
               square-lattice width, a junction-routing azimuth, a format string.
               **Leave alone, with the reason recorded.**

Verified by `--self-test`.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
CLASSIFICATION = os.path.join(HERE, "T-234-classification.json")

#: The claims and challenges that withdrew the premises.  A pointer to any of them, within
#: POINTER_WINDOW characters after the occurrence, discharges it.
POINTERS = (
    "C-0140", "C-0141", "CH-0172", "CH-0173", "CH-0174", "CH-0175", "CH-0180", "CH-0181", "T-233",
    "T-234",
)
POINTER_WINDOW = 900

#: A claim whose HEADLINE carries an annotation banner is a repaired claim: a reader cannot reach
#: any number in it without passing the banner.  Requiring a pointer within POINTER_WINDOW of every
#: occurrence instead would demand sprinkling pointers through result tables, which is not how this
#: corpus annotates and which damages the tables it would be sprinkled through.  So a headline
#: pointer discharges the whole file -- and the window is deliberately short, so that a pointer
#: buried in a body section cannot pass as one.
HEADLINE_WINDOW = 3000

CLASSES = ("MOVED", "DISCHARGED", "RECORD", "CORRECT", "OUT_OF_SCOPE")
#: The classes the gate requires a pointer for.
ADDRESSED = ("MOVED", "DISCHARGED")

#: The two outward-facing documents.  `T-233` owns them; this task produces their list and
#: does not edit them, so their debt is reported on its own line and does NOT fail the gate.
#: `CLAUDE.md`: a check that can never come back clean cannot be a gate.
DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")

#: Identifiers that look numeric and are not quantities.  Blanked before matching, so that
#: `T-132` cannot be read as `CH-0151`'s 132-station census.  Length-preserving, or every
#: reported offset below the first identifier would be wrong.
_ID_PATTERNS = [
    re.compile(r"\b(?:CH|C|P|T|S)-\d{1,4}[a-z]?\b"),
]

STRIKE = re.compile(r"~~.*?~~", re.DOTALL)

#: Context a match must ALSO satisfy, on its own line, for the families whose token is
#: ambiguous.  `112 bp` is the square lattice's buildable width as well as the honeycomb's
#: nominal row, and `perpendicular` is a junction-routing word.
_HONEYCOMB = r"honeycomb|four-layer|four layer|15 . 4|10 . 6"
_AZIMUTH_CTX = r"honeycomb|azimuth|oblique|top face|top-face|station|sublattice|15 . 4|10 . 6"

FAMILIES = (
    (
        "FOOTPRINT",
        r"38\.08 [x×] 38\.04|38\.08 [x×] 25\.36|1 448\.5632|965\.7088|0\.666666667"
        r"|third of the footprint|38 [x×] 25 nm|0\.0577199433|0\.00874363524"
        r"|0\.0788618807|3\.29690337",
        None,
    ),
    ("WIDTH", r"\bdrawable\b|\b119 bp\b|\b40\.46\b|\b112 bp\b", _HONEYCOMB),
    (
        "AZIMUTH",
        r"perpendicular root|perpendicular azimuth|oblique versus perpendicular"
        r"|±60°|k_z\(60°\)|\b132\b|perpendicular one",
        _AZIMUTH_CTX,
    ),
    ("SCAFFOLD", r"p8064", None),
    (
        "PLACEMENT",
        r"single-layer square-lattice|single-layer\n\s*square-lattice"
        r"|no station lattice, no plan ceiling|never priced an oblique",
        None,
    ),
)


def in_scope(path: str) -> bool:
    return path.startswith("gpd/claims/") or path in ("TASKS.md",) + DELIVERABLES


def blank_identifiers(text: str) -> str:
    """Replace every task/claim/challenge identifier by spaces of the SAME length."""
    out = text
    for pattern in _ID_PATTERNS:
        out = pattern.sub(lambda m: " " * (m.end() - m.start()), out)
    return out


def struck_spans(text: str):
    return [(m.start(), m.end()) for m in STRIKE.finditer(text)]


def is_struck(spans, offset: int) -> bool:
    return any(lo <= offset < hi for lo, hi in spans)


def has_pointer(text: str, offset: int, window: int = POINTER_WINDOW) -> bool:
    """Is a pointer to a correcting claim within `window` characters AFTER the occurrence?

    Forward only: an annotation follows the sentence it annotates, and a pointer belonging
    to a PREVIOUS occurrence must not discharge the next one.
    """
    ahead = text[offset : offset + window]
    return any(p in ahead for p in POINTERS)


def headline_pointer(text: str, path: str = "gpd/claims/x.md", window: int = HEADLINE_WINDOW) -> bool:
    """Does the file's own headline block carry a pointer to a correcting claim?

    **Claims only.**  A claim is one argument under one headline, so a banner there is passed by
    every reader of every number in it.  `TASKS.md` is a register of hundreds of independent rows
    and a deliverable is a document of independent sections: a pointer at the top of either
    discharges nothing, and letting it would make the gate vacuous on the two largest files.
    """
    if not path.startswith("gpd/claims/"):
        return False
    return any(p in text[:window] for p in POINTERS)


def occurrences(text: str):
    """(family, line, offset, token) for every match in one file, in file order.

    Identifiers are blanked before matching and the ORIGINAL text supplies the line, so an
    offset reported here indexes the file as it is on disk.
    """
    hunted = blank_identifiers(text)
    lines = text.split("\n")
    found = []
    for family, pattern, context in FAMILIES:
        for match in re.finditer(pattern, hunted):
            line_index = text.count("\n", 0, match.start())
            if context and not re.search(context, lines[line_index], re.I):
                continue
            found.append((match.start(), family, line_index + 1, match.group(0)))
    found.sort()
    return [(f, l, o, t) for o, f, l, t in found]


def corpus_files(root: str):
    """Every markdown file in the corpus, tracked **or not yet committed**.

    `CLAUDE.md` records that `tools/check-corpus-links.py` lists its corpus with `git ls-files` and
    therefore **skips uncommitted files in the checkout** — so the run an agent makes on its own
    work is the blind one, and this tool inherited the defect: on its first run it could not see the
    claim that was about to raise it.  `--others --exclude-standard` adds exactly the untracked,
    non-ignored files, which is what an in-progress claim is.  The tree walk remains as the fallback
    for a snapshot with no `.git`.
    """
    try:
        tracked = subprocess.run(
            ["git", "ls-files", "*.md"], cwd=root, capture_output=True, text=True, check=True
        ).stdout.split()
        untracked = subprocess.run(
            ["git", "ls-files", "--others", "--exclude-standard", "*.md"],
            cwd=root, capture_output=True, text=True, check=True,
        ).stdout.split()
        if tracked:
            return sorted(set(tracked) | set(untracked))
    except (OSError, subprocess.CalledProcessError):
        pass
    found = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [
            d for d in dirnames if d not in (".git", "build") and not d.startswith("build-")
        ]
        for name in filenames:
            if name.endswith(".md"):
                found.append(os.path.relpath(os.path.join(dirpath, name), root))
    return sorted(found)


def census(root: str):
    records = []
    for path in corpus_files(root):
        if not in_scope(path):
            continue
        try:
            with open(os.path.join(root, path), encoding="utf-8") as handle:
                text = handle.read()
        except OSError:
            continue
        lines = text.split("\n")
        spans = struck_spans(text)
        banner = headline_pointer(text, path)
        for index, (family, line, offset, token) in enumerate(occurrences(text)):
            records.append(
                {
                    "file": path,
                    "index": index,
                    "family": family,
                    "line": line,
                    "token": token,
                    "pointer": has_pointer(text, offset),
                    "headlinePointer": banner,
                    "struck": is_struck(spans, offset),
                    "deliverable": path in DELIVERABLES,
                    "text": lines[line - 1].strip()[:300],
                }
            )
    return records


def load_classification(path: str = CLASSIFICATION):
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


def classify(records, table):
    problems = []
    for record in records:
        entry = table.get(record["file"], {}).get(str(record["index"]))
        if entry is None:
            record["class"] = None
            record["why"] = None
            problems.append(
                "unclassified: {}#{} line {} [{}] {!r}".format(
                    record["file"], record["index"], record["line"], record["family"],
                    record["token"],
                )
            )
            continue
        if entry["class"] not in CLASSES:
            problems.append(
                "unknown class {!r} at {}#{}".format(entry["class"], record["file"], record["index"])
            )
        record["class"] = entry["class"]
        record["why"] = entry.get("why")
    seen = {(r["file"], r["index"]) for r in records}
    for path, entries in table.items():
        for index in entries:
            if (path, int(index)) not in seen:
                problems.append("stale classification: {}#{} has no occurrence".format(path, index))
    return records, problems


def check(root: str) -> int:
    records = census(root)
    records, problems = classify(records, load_classification())
    unpointed = [
        r
        for r in records
        if r["class"] in ADDRESSED
        and not r["pointer"]
        and not r["struck"]
        and not r["headlinePointer"]
    ]
    gate = [r for r in unpointed if not r["deliverable"]]
    debt = [r for r in unpointed if r["deliverable"]]
    counts = {c: sum(1 for r in records if r["class"] == c) for c in CLASSES}
    print(
        "{} occurrence(s) in {} file(s)".format(
            len(records), len({r["file"] for r in records})
        )
    )
    print("  " + "  ".join("{} {}".format(c, counts[c]) for c in CLASSES))
    for family, _pattern, _context in FAMILIES:
        print(
            "  {:<12} {}".format(family, sum(1 for r in records if r["family"] == family))
        )
    for problem in problems:
        print("PROBLEM  " + problem)
    for record in gate:
        print(
            "UNPOINTED  {}:{} #{} [{} {}]".format(
                record["file"], record["line"], record["index"], record["class"],
                record["family"],
            )
        )
    for record in debt:
        print(
            "T-233-DEBT  {}:{} #{} [{} {}]  {}".format(
                record["file"], record["line"], record["index"], record["class"],
                record["family"], record["token"],
            )
        )
    print("T-233 debt {} occurrence(s) in the two deliverables, which this task does NOT edit"
          .format(len(debt)))
    print("GATE {} defect(s)".format(len(problems) + len(gate)))
    return 1 if problems or gate else 0


def main(argv) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=ROOT)
    parser.add_argument("--census", action="store_true", help="print the raw census as JSON")
    parser.add_argument("--check", action="store_true", help="the gate")
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

    def ok(name, condition):
        if not condition:
            failures.append(name)

    # --- blank_identifiers is length preserving, which every offset below depends on
    text = "T-132 and CH-0151 and C-0140 and 132 stations"
    ok("blank keeps length", len(blank_identifiers(text)) == len(text))
    ok("blank removes T-132", "T-132" not in blank_identifiers(text))
    ok("blank keeps the bare 132", "132 stations" in blank_identifiers(text))
    ok("blank removes CH-0151", "CH-0151" not in blank_identifiers(text))
    ok("blank keeps P-22 out", "P-22" not in blank_identifiers("P-22"))

    # --- the AZIMUTH family must not fire on a task identifier
    ok(
        "T-132 is not a 132-station census",
        occurrences("| T-132 | a station census of something |") == [],
    )
    ok(
        "a bare 132 with station context does fire",
        [r[0] for r in occurrences("the top face supplies 132 stations on a honeycomb")]
        == ["AZIMUTH"],
    )

    # --- WIDTH needs honeycomb context: the square lattice's own 112 bp must not fire
    ok(
        "112 bp on the square lattice does not fire",
        occurrences("the buildable seamless square-lattice row is 112 bp = 38.08 nm") == [],
    )
    ok(
        "112 bp on the honeycomb does fire",
        [r[0] for r in occurrences("the honeycomb tile is 15 rows x 4 layers x 112 bp")]
        == ["WIDTH"],
    )

    # --- FOOTPRINT needs no context: its tokens are unique to the four-layer line
    ok(
        "the reversed footprint fires",
        [r[0] for r in occurrences("38.08 × 25.36 nm")] == ["FOOTPRINT"],
    )
    ok(
        "the threshold fires",
        [r[0] for r in occurrences("f = 0.0788618807")] == ["FOOTPRINT"],
    )
    ok("SCAFFOLD fires on p8064", [r[0] for r in occurrences("folded from p8064")] == ["SCAFFOLD"])
    ok(
        "PLACEMENT fires on the absence statement",
        [r[0] for r in occurrences("every placement here is single-layer square-lattice")]
        == ["PLACEMENT"],
    )

    # --- occurrences are returned in FILE ORDER, because the classification is keyed on index
    multi = "0.0788618807 then p8064 then 38.08 × 25.36"
    ok("file order", [r[0] for r in occurrences(multi)] == ["FOOTPRINT", "SCAFFOLD", "FOOTPRINT"])

    # --- lines are 1-based and correct
    ok("line numbers", occurrences("x\ny\np8064")[0][1] == 3)

    # --- the pointer window is FORWARD only
    body = "0.0788618807 is the threshold. Annotated by C-0141."
    ok("forward pointer found", has_pointer(body, 0))
    ok(
        "a pointer BEHIND does not discharge",
        not has_pointer("C-0141 said so. " + " " * 950 + "0.0788618807", 966),
    )
    ok("no pointer is no pointer", not has_pointer("0.0788618807 stands alone", 0))
    ok("window is respected", not has_pointer("0.0788618807" + "x" * 2000 + "C-0141", 0))

    # --- the headline pointer discharges a file, and only from its own headline
    ok("headline pointer found", headline_pointer("# C-0116 - x\n> Annotated: C-0141\n"))
    ok("no headline pointer", not headline_pointer("# C-0116 - x\n> nothing here\n"))
    ok(
        "a pointer past the headline window is not a headline pointer",
        not headline_pointer("# C-0116\n" + "x" * 4000 + "C-0141"),
    )
    ok(
        "TASKS.md gets NO headline discharge",
        not headline_pointer("# TASKS\n> C-0141\n", "TASKS.md"),
    )
    ok(
        "a deliverable gets NO headline discharge",
        not headline_pointer("# ANSWERS\n> C-0141\n", "ANSWERS.md"),
    )
    ok("HEADLINE_WINDOW is short", HEADLINE_WINDOW <= 4000)

    # --- strike detection
    struck = "~~0.0788618807~~ and 0.0788618807"
    spans = struck_spans(struck)
    ok("first is struck", is_struck(spans, 2))
    ok("second is not struck", not is_struck(spans, struck.rindex("0.0788618807")))

    # --- the corpus includes uncommitted files: this tool's own claim was invisible at first
    listing = corpus_files(ROOT)
    ok("corpus is non-empty", len(listing) > 100)
    ok("corpus contains a claim", any(f.startswith("gpd/claims/") for f in listing))
    ok(
        "corpus contains THIS task's own claim, tracked or not",
        "gpd/claims/C-0144-honeycomb-correction-supersession.md" in listing,
    )
    ok("corpus is sorted and unique", listing == sorted(set(listing)))

    # --- scope
    ok("a claim is in scope", in_scope("gpd/claims/C-0116-composite-fraction-threshold.md"))
    ok("TASKS.md is in scope", in_scope("TASKS.md"))
    ok("ANSWERS.md is in scope", in_scope("ANSWERS.md"))
    ok("a task file is NOT in scope", not in_scope("gpd/tasks/T-219-x.md"))
    ok("a challenge is NOT in scope", not in_scope("gpd/challenges/CH-0174-x.md"))
    ok("the journal is NOT in scope", not in_scope("JOURNAL.md"))

    # --- classify reports both an unclassified occurrence and a stale entry
    records = [{"file": "a.md", "index": 0, "line": 1, "family": "SCAFFOLD", "token": "p8064"}]
    _, problems = classify(list(records), {})
    ok("unclassified is reported", any(p.startswith("unclassified") for p in problems))
    _, problems = classify(
        list(records), {"a.md": {"0": {"class": "MOVED", "why": "x"}, "1": {"class": "MOVED"}}}
    )
    ok("stale is reported", any(p.startswith("stale") for p in problems))
    _, problems = classify(list(records), {"a.md": {"0": {"class": "NONSENSE"}}})
    ok("unknown class is reported", any(p.startswith("unknown class") for p in problems))

    # --- ADDRESSED is exactly the two repair classes
    ok("ADDRESSED", set(ADDRESSED) == {"MOVED", "DISCHARGED"})
    ok("every ADDRESSED is a CLASS", all(a in CLASSES for a in ADDRESSED))

    for failure in failures:
        print("FAIL  " + failure)
    print("self-test: {} failure(s)".format(len(failures)))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
