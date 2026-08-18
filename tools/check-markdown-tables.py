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
# Check that every GitHub-flavoured Markdown table in the repository renders (task P-23).
#
#     tools/check-markdown-tables.py [paths...]        # default: every tracked *.md
#
# `C-0067` found that the longest answer in this repository's PRIMARY DELIVERABLE had never
# rendered as a table cell, and `CLAUDE.md` carries the lesson: a Markdown table row must be one
# physical line, which is the single place semantic line breaks do not apply.  That lesson was
# recorded and the defect kept happening, in the CLAIMS — which are the artifacts NDI reads, and
# where a mangled table is a mangled piece of evidence.
#
# It is machine-checkable, so it should be machined.  Three defect kinds are live in this
# repository and all three are invisible to a writer reading their own source:
#
#   ROW     a body row whose cell count disagrees with the header — a missing cell, or a bare
#           `|` inside the row.  GFM pads or truncates silently, so the row still "renders".
#   HEADER  a bare `|` in the HEADER, which widens the whole table.  This is the nastiest kind:
#           every correct body row is then the odd one out, so a naive checker blames the wrong
#           lines.  The separator row is the tie-breaker — it is written by hand to match the
#           INTENDED width and never contains prose, so where header and separator disagree it
#           is the header that is wrong.
#
# A `|` inside inline code is NOT protected: GFM splits table cells before it parses backticks.
# The only way to write a literal pipe in a cell is `\|`, and that works inside code spans too.
#
# Exit status is 1 if any defect is found, so this is usable as a gate.
# Verified by tools/test-check-markdown-tables.py.
import argparse
import os
import re
import subprocess
import sys
from collections import namedtuple

Defect = namedtuple("Defect", "line kind width header_width text")

# Split on a pipe that is not backslash-escaped.  `\|` is the only correct way to write a
# literal pipe in a cell, and it is what the well-formed rows in this repository already use.
_UNESCAPED_PIPE = re.compile(r"(?<!\\)\|")

_SEPARATOR = re.compile(r"^\|?\s*:?-{1,}:?\s*(\|\s*:?-{1,}:?\s*)*\|?$")

_FENCE = re.compile(r"^\s*(```|~~~)")


def cells(line):
    """The cells of a table row, stripped, with the optional outer pipes removed."""
    text = line.strip()
    if text.startswith("|"):
        text = text[1:]
    if text.endswith("|") and not text.endswith(r"\|"):
        text = text[:-1]
    return [cell.strip() for cell in _UNESCAPED_PIPE.split(text)]


def is_separator(line):
    """Whether this is a GFM header separator (`|---|---|`, alignment colons allowed)."""
    text = line.strip()
    if not text or "-" not in text:
        return False
    return bool(_SEPARATOR.match(text))


def _blocks(lines):
    """Yield (start_index, [lines]) for each run of consecutive pipe lines outside a fence."""
    block, start, fenced = [], None, False
    for index, line in enumerate(lines):
        if _FENCE.match(line):
            fenced = not fenced
            if block:
                yield start, block
                block, start = [], None
            continue
        if fenced:
            continue
        if line.strip().startswith("|"):
            if not block:
                start = index
            block.append(line)
        elif block:
            yield start, block
            block, start = [], None
    if block:
        yield start, block


def defects(text):
    """Every table defect in a Markdown document, as a list of `Defect`."""
    lines = text.split("\n")
    found = []
    for start, block in _blocks(lines):
        if len(block) < 2 or not is_separator(block[1]):
            # Not a GFM table: a header must be followed immediately by a separator.
            continue
        header_width = len(cells(block[0]))
        separator_width = len(cells(block[1]))
        if header_width != separator_width:
            # The separator is hand-written to the intended width and carries no prose, so
            # where the two disagree the header is what is wrong — most often a bare `|`.
            found.append(
                Defect(start + 1, "HEADER", header_width, separator_width, block[0].strip())
            )
            continue
        for offset, row in enumerate(block[2:], start=2):
            if is_separator(row):
                continue
            width = len(cells(row))
            if width != header_width:
                found.append(
                    Defect(start + offset + 1, "ROW", width, header_width, row.strip())
                )
    return found


# `third-party/` holds the problem definition AS RECEIVED, unmodified — a standing invariant of
# this repository rather than a preference.  Its §6 task table has a row that does not render, and
# it must keep it.  A checker that reports a defect nobody is permitted to fix can never come back
# clean, and a check that never comes back clean cannot be a gate — so the exclusion is what makes
# this usable, not a weakening of it.  Matched at the path root only, so a `gpd/third-party/`
# (which does not exist, and would be ours) would still be checked.
_EXCLUDED_ROOTS = ("third-party",)


def is_excluded(path):
    """Whether this path is outside the checker's remit.

    Normalised first, because the two ways this tool learns its paths do not agree: `git ls-files`
    gives `third-party/x.md` and the no-git fallback's `os.walk(".")` gives `./third-party/x.md`.
    The `./` defeated a bare root comparison, so the gate reported the one defect it is forbidden
    to fix — and it did so only inside a verification SNAPSHOT, which has no `.git` and is
    therefore precisely where the fallback is the live path and where the gate is used.
    """
    parts = [p for p in os.path.normpath(path).replace(os.sep, "/").split("/") if p not in ("", ".")]
    return bool(parts) and parts[0] in _EXCLUDED_ROOTS


def merge_paths(tracked, present):
    """The default sweep: tracked plus untracked-but-present, excluded paths removed, sorted.

    The checker used to list only `git ls-files`, so in the CHECKOUT it skipped untracked files
    while in a verification SNAPSHOT — which has no `.git` — the fallback walked the tree and
    checked everything.  A new claim is untracked until it is staged, so a local run reported
    clean and the snapshot found a defect in that very file: the tool disagreeing with itself
    about its own remit.  Merging the two makes the environments agree, and it is the direction
    that catches more rather than less.
    """
    return sorted({p for p in list(tracked) + list(present) if not is_excluded(p)})


def untracked_markdown():
    """Markdown git can see but is not tracking — new claims, in-flight drafts."""
    try:
        out = subprocess.run(
            ["git", "ls-files", "--others", "--exclude-standard", "*.md"],
            capture_output=True, text=True, check=True,
        ).stdout
        return [p for p in out.split("\n") if p]
    except (subprocess.CalledProcessError, FileNotFoundError):
        return []


def tracked_markdown():
    """Every tracked `*.md` path, so untracked scratch files are not reported."""
    try:
        out = subprocess.run(
            ["git", "ls-files", "*.md"], capture_output=True, text=True, check=True
        ).stdout
        return [p for p in out.split("\n") if p]
    except (subprocess.CalledProcessError, FileNotFoundError):
        # A verification snapshot has no `.git`, so this is the live path wherever the gate runs.
        # `os.walk(".")` prefixes every path with `./`; normalise it away here as well as in
        # `is_excluded`, so the two sources of paths are interchangeable for a caller too.
        found = []
        for root, dirs, names in os.walk("."):
            dirs[:] = [d for d in dirs if d not in (".git", "build") and not d.startswith("build-")]
            for name in names:
                if name.endswith(".md"):
                    path = os.path.relpath(os.path.join(root, name), ".")
                    found.append(path.replace(os.sep, "/"))
        return sorted(found)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="*", help="default: every tracked *.md")
    arguments = parser.parse_args(argv)

    # An explicit path is always honoured; the exclusion applies to the default sweep, so that
    # `tools/check-markdown-tables.py third-party/…` can still be used to look.
    paths = arguments.paths or merge_paths(tracked_markdown(), untracked_markdown())
    total = 0
    for path in paths:
        with open(path, encoding="utf-8") as handle:
            found = defects(handle.read())
        for defect in found:
            total += 1
            print(
                "{}:{}\t{}\t{} cells against {}\t{}".format(
                    path,
                    defect.line,
                    defect.kind,
                    defect.width,
                    defect.header_width,
                    defect.text[:90],
                )
            )
    print(
        "# {} table defect(s) in {} file(s)".format(total, len(paths)),
        file=sys.stderr,
    )
    return 1 if total else 0


if __name__ == "__main__":
    sys.exit(main())
