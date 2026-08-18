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
# P-26 -- every challenge file appears in gpd/challenges/README.md's index.
#
#     tools/check-challenge-index.py             checks, exit 1 on any gap
#     tools/check-challenge-index.py --backfill  prints the missing rows, newest last
#     tools/check-challenge-index.py --selftest  runs the self-tests
#
# WHY THIS EXISTS. `gpd/challenges/README.md` opens by saying a challenge names *"the claim it
# contradicts, the result that contradicts it, the methodological grounds ... and the resolution"* --
# and its index had **66 rows against 124 files**, missing everything from `CH-0050` on. Three
# separate claims noticed in passing (`C-0113` measured 65 of 111, `C-0122` re-measured it, `C-0128`
# reported it as a process note) and none of the three could act on it, because each was in the
# middle of something else.
#
# It is `C-0083`'s Markdown-table class and `C-0122`'s broken-link class a third time: silent at the
# point of writing, invisible to every other checker, and cheap to mechanise. An index that is half
# the corpus is worse than no index, because a reader who finds `CH-0100` in it will believe
# `CH-0151`'s absence means it does not exist.
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CHALLENGES = os.path.join(ROOT, "gpd", "challenges")
INDEX = os.path.join(CHALLENGES, "README.md")

_ROW = re.compile(r"\[`(CH-\d{1,4})`\]")
_FIELD = re.compile(r"^\|\s*\*\*(Against|Raised by|Grounds|Status)\*\*\s*\|(.*?)\|?\s*$", re.M)


def indexed(index_text):
    """The challenge IDs the index carries, in order of first appearance."""
    seen = []
    for match in _ROW.finditer(index_text):
        if match.group(1) not in seen:
            seen.append(match.group(1))
    return seen


def on_disk(directory=CHALLENGES):
    """{id: filename} for every challenge file."""
    found = {}
    if not os.path.isdir(directory):
        return found
    for name in sorted(os.listdir(directory)):
        match = re.match(r"(CH-\d{1,4})-.*\.md$", name)
        if match:
            found[match.group(1)] = name
    return found


def fields_of(text):
    """The header fields a challenge file declares, as {label: cell}.

    Read from the file's own two-column header table -- the same place
    `tools/trace-answers.py` reads `**Status**` from, so the two instruments cannot disagree about
    what a challenge says about itself.
    """
    return {label: cell.strip() for label, cell in _FIELD.findall(text)}


def row_for(identifier, filename, text):
    """One index row, built from the challenge's OWN header fields.

    Deliberately mechanical: the 66 hand-written rows carry prose a generator cannot reproduce, and
    inventing some is worse than transcribing what the file already states. A backfilled row says
    where to look and what the file claims about itself, and nothing more.
    """
    fields = fields_of(text)
    against = fields.get("Against", "—")
    grounds = fields.get("Grounds", fields.get("Raised by", "—"))
    status = fields.get("Status", "—")
    # A bare `|` inside a cell would split the row -- `C-0083`'s class, which this tool must not
    # reintroduce while fixing a different one.
    def safe(cell):
        return cell.replace("|", r"\|")
    return "| [`{}`]({}) | {} | {} | {} |".format(
        identifier, filename, safe(against), safe(grounds), safe(status)
    )


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append(name)
            print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
        else:
            print("ok   {}".format(name))

    check("an index row is recognised",
          indexed("| [`CH-0001`](CH-0001-x.md) | a | b | c |"), ["CH-0001"])
    check("a duplicate id is counted once",
          indexed("[`CH-0001`](a.md) and [`CH-0001`](a.md)"), ["CH-0001"])
    check("prose naming a challenge without linking it is not an index row",
          indexed("see CH-0002 for the detail"), [])
    header = (
        "# CH-0009 - something\n\n| | |\n|---|---|\n"
        "| **Against** | `C-0001` |\n"
        "| **Raised by** | `C-0002` |\n"
        "| **Grounds** | methodological |\n"
        "| **Status** | **UPHELD** |\n"
    )
    check("the header fields are read", fields_of(header)["Status"], "**UPHELD**")
    check("a backfilled row carries the file's own fields",
          row_for("CH-0009", "CH-0009-x.md", header),
          "| [`CH-0009`](CH-0009-x.md) | `C-0001` | methodological | **UPHELD** |")
    check("a missing Grounds falls back to Raised by",
          row_for("CH-0009", "CH-0009-x.md",
                  "| **Against** | `C-0001` |\n| **Status** | raised |\n"
                  "| **Raised by** | `C-0002` |\n"),
          "| [`CH-0009`](CH-0009-x.md) | `C-0001` | `C-0002` | raised |")
    check("a bare pipe in a cell is escaped, not left to split the row",
          row_for("CH-0009", "CH-0009-x.md", "| **Against** | a|b |\n| **Status** | s |\n"),
          r"| [`CH-0009`](CH-0009-x.md) | a\|b | — | s |")
    check("a file with no header at all still yields a row",
          row_for("CH-0009", "CH-0009-x.md", "prose only"),
          "| [`CH-0009`](CH-0009-x.md) | — | — | — |")
    check("the real corpus has challenge files", len(on_disk()) > 0, True)

    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    with open(INDEX, encoding="utf-8") as handle:
        index_text = handle.read()
    listed = set(indexed(index_text))
    files = on_disk()
    missing = [i for i in sorted(files) if i not in listed]
    orphaned = sorted(listed - set(files))

    if "--backfill" in argv:
        for identifier in missing:
            with open(os.path.join(CHALLENGES, files[identifier]), encoding="utf-8") as handle:
                print(row_for(identifier, files[identifier], handle.read()))
        return 0

    for identifier in missing:
        print("{}\tUNINDEXED\t{}".format(files[identifier], identifier))
    for identifier in orphaned:
        print("README.md\tINDEXED-BUT-ABSENT\t{}".format(identifier))
    sys.stdout.flush()
    print(
        "# {} challenge file(s), {} indexed, {} unindexed, {} indexed but absent".format(
            len(files), len(listed & set(files)), len(missing), len(orphaned)
        ),
        file=sys.stderr,
    )
    return 1 if (missing or orphaned) else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
