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
# T-273 -- every claim and challenge IDENTIFIER cited in the corpus resolves to a file that exists.
#
#     tools/check-corpus-identifiers.py             checks the corpus, exit 1 on any defect
#     tools/check-corpus-identifiers.py --selftest  runs the self-tests
#
# WHY THIS EXISTS. `C-0083` gates a claim's FILENAME and `tools/check-corpus-links.py` gates a
# relative LINK. A bare `` `CH-0133` `` in a sentence is neither, so it is invisible to every gate
# in this tree -- and `T-268` cited two of them, `CH-0132` and `CH-0133`, as though they were
# challenges. They are not: both were **reserved** by `T-201` in iteration 24 and never filed.
# Nine occurrences accumulated, one of them in `CLAUDE.md`, before a claim re-derived the census
# and found nothing behind the ID.
#
# It is the same class as the slug defect one level down: a writer reconstructs an identifier from
# the finding's SUBJECT, and the number that comes to mind is the one that was reserved beside it.
#
# THE DISCRIMINATOR, AND WHY THIS CAN BE A GATE AT ALL. A corpus that reserves numbers in writing
# has legitimate mentions of identifiers that do not exist, and there are exactly two kinds:
#
#   RELEASED   "`CH-0208` was reserved for this claim and is RELEASED UNUSED"
#   ABSENT     "there is no `CH-0133` -- the corpus's highest challenge is `CH-0209`"
#
# Both are statements ABOUT the non-existence and neither is a citation OF the thing. Measured over
# the corpus the two contexts account for every legitimate mention -- 12 released and, after this
# task's repair, 9 absent -- and what is left is the defect. Without them the gate would fire on
# 21 correct sentences, which is the rate at which a gate gets switched off (`C-0127`).
#
# SCOPE. `gpd/claims/`, `gpd/tasks/`, `gpd/challenges/`, and the root Markdown documents. NOT
# `JOURNAL.md`, which is a dated history: a journal entry naming a number that was later renumbered
# is a correct record of what happened, and rewriting it would be the one thing this repository
# forbids. NOT `tools/`, whose checkers carry deliberately impossible fixtures (`C-9999`).
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

_IDENTIFIER = re.compile(r"\b(CH-\d{4}|C-\d{4})\b")
# The two legitimate ways to name an identifier that does not exist. Searched in a window around
# the occurrence rather than on the line, because this corpus writes with semantic line breaks and
# a reservation's verb is routinely a line above its number.
_LEGITIMATE = re.compile(
    r"released|unused|reserved|not used|never raised|never filed|"
    r"there is no|no such|does not exist|do not exist",
    re.IGNORECASE)
_WINDOW = 160

# `JOURNAL.md` is a dated history and is deliberately out of scope; see the header.
EXCLUDED_ROOT_DOCUMENTS = {"JOURNAL.md"}


def existing_identifiers(root=ROOT):
    """{'C-0001', 'CH-0001', ...} -- every identifier that has a file."""
    found = set()
    for directory, pattern in (("claims", r"(C-\d{4})-"), ("challenges", r"(CH-\d{4})-")):
        path = os.path.join(root, "gpd", directory)
        for name in os.listdir(path):
            match = re.match(pattern, name)
            if match and name.endswith(".md"):
                found.add(match.group(1))
    return found


def unresolved_in(text, known):
    """[(identifier, offset)] for every cited identifier in `text` that resolves to nothing.

    An occurrence whose surrounding window says the identifier was released, reserved or does not
    exist is a statement ABOUT the absence and not a citation OF the thing, so it is not a defect.
    """
    occurrences = [(m.group(1), m.start(), m.end()) for m in _IDENTIFIER.finditer(text)
                   if m.group(1) not in known]
    # The exemption is per (DOCUMENT, IDENTIFIER), not per occurrence. Once a document has said
    # that an identifier was released or does not exist, every later mention of it in that document
    # is discussing the absence -- `CLAUDE.md`'s entry about this very defect names `CH-0133` three
    # times, and only the first is within a window of the words "no such challenge exists".
    # Per-occurrence, the gate fires on the sentence that records the defect, which is the failure
    # `C-0083` describes and `T-249` hit from the other side.
    declared = {identifier for identifier, start, end in occurrences
                if _LEGITIMATE.search(text[max(0, start - _WINDOW): end + _WINDOW])}
    return [(identifier, start) for identifier, start, _ in occurrences
            if identifier not in declared]


def scanned_files(root=ROOT):
    """The corpus this gate is about: the three gpd directories plus the root documents."""
    files = []
    for directory in ("claims", "tasks", "challenges"):
        base = os.path.join("gpd", directory)
        full = os.path.join(root, base)
        if not os.path.isdir(full):
            continue
        files += [os.path.join(base, name) for name in sorted(os.listdir(full))
                  if name.endswith(".md")]
    files += [name for name in sorted(os.listdir(root))
              if name.endswith(".md") and name not in EXCLUDED_ROOT_DOCUMENTS
              and os.path.isfile(os.path.join(root, name))]
    return files


def _selftest():
    failures = []

    def check(name, actual, expected):
        ok = actual == expected
        print("{} {}".format("ok  " if ok else "FAIL", name))
        if not ok:
            print("     expected {!r}\n     actual   {!r}".format(expected, actual))
            failures.append(name)

    known = {"C-0001", "CH-0001"}

    check("an identifier that resolves is not reported",
          unresolved_in("as [`C-0001`](C-0001-x.md) shows", known), [])
    check("a bare identifier that resolves to nothing IS reported",
          [i for i, _ in unresolved_in("the integral rendering (`CH-0133`)", known)],
          ["CH-0133"])
    # The live defect this gate was written for: `T-268` cited two numbers that were reserved in
    # iteration 24 and never filed.
    check("and so is a second one in the same sentence",
          [i for i, _ in unresolved_in("at their own call sites (`CH-0132`), see `CH-0133`", known)],
          ["CH-0132", "CH-0133"])

    # The two legitimate contexts. Without these the gate fires on 21 correct sentences.
    check("a RELEASED reservation is not a citation",
          unresolved_in("`CH-0208` was reserved for this claim and is RELEASED UNUSED", known), [])
    check("nor is a reservation table row",
          unresolved_in("| E | `T-201` | `C-0115` | `CH-0132`, `CH-0133` — both released unused |",
                        known), [])
    check("nor is a sentence saying the identifier does not exist",
          unresolved_in("**There is no `CH-0133`.** `P1` cites it for the rendering.", known), [])
    check("nor a struck one beside its correction",
          unresolved_in("(~~`CH-0132`~~ — there is no `CH-0132` either)", known), [])
    # The window is a WINDOW and not a line: this corpus uses semantic line breaks, so a
    # reservation's verb is routinely a line above its number.
    check("the context window crosses a semantic line break",
          unresolved_in("both were reserved by `T-201` in iteration 24\nand never filed: `CH-0132`",
                        known), [])
    # ... and the exemption is per DOCUMENT and per IDENTIFIER, so a document that declares one
    # identifier absent has not excused a different one.
    check("declaring one identifier absent does not excuse another",
          [i for i, _ in unresolved_in("there is no `CH-0132`." + " " * 400 + "see `CH-0133`",
                                       known)],
          ["CH-0133"])
    check("but a second mention of the SAME identifier is discussing it",
          unresolved_in("there is no `CH-0133`." + " " * 400 + "a bare `CH-0133` is not a link",
                        known),
          [])

    check("a four-digit number that is not an identifier is ignored",
          unresolved_in("the 0133 mV contact potential", known), [])
    check("an identifier inside a longer token is ignored",
          unresolved_in("XC-0133Y", known), [])

    # `JOURNAL.md` is a dated history: rewriting it is the one thing this repository forbids.
    check("the journal is out of scope", "JOURNAL.md" in scanned_files(), False)
    check("but the other root documents are not", "CLAUDE.md" in scanned_files(), True)
    check("and the three gpd directories are in scope",
          all(any(f.startswith(os.path.join("gpd", d)) for f in scanned_files())
              for d in ("claims", "tasks", "challenges")), True)

    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    known = existing_identifiers()
    files = scanned_files()
    defects = 0
    for relative in files:
        path = os.path.join(ROOT, relative)
        try:
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
        except OSError:
            continue
        for identifier, offset in unresolved_in(text, known):
            line = text.count("\n", 0, offset) + 1
            print("{}:{}\tDANGLING-ID\t{}".format(relative, line, identifier))
            defects += 1
    sys.stdout.flush()
    print("# {} dangling identifier(s) in {} file(s); {} claims and challenges exist".format(
        defects, len(files), len(known)), file=sys.stderr)
    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
