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
"""Every `gpd/results/<name>.json` path spelled in a corpus document must exist.

    tools/check-result-path-references.py [--root .] [--self-test]

`T-252`.  A claim's `Provenance` row names its result file as a **bare path**, not as a Markdown
link, so `tools/check-corpus-links.py` cannot see it -- it resolves `[label](target)` and nothing
else.  Measured over 190 claims: **170 name a result file, 20 name none, and 0 name one that does
not exist.**  So the gate is clean today and it is not vacuous: renaming or removing a result file
is a normal act of this loop (`C-0101` re-emits, `C-0117` sorts the sweep), and it leaves every
claim that named the old path pointing at nothing, silently.

WHAT THIS DOES **NOT** CHECK, deliberately, and it is the larger half of `CH-0199`: whether a
number a claim quotes is still **findable** in the file it names.  `C-0197`'s measurement of that
class is in `gpd/results/T-252-a-quoted-number-has-no-link-back.json`, and the answer is that the
class is **100 % deliberate**: 58 of 58 unfindable tokens are a defect's own output, a before/after
pair from a precision repair, or a value derived in the claim and never emitted.  `C-0092`'s rule
-- *a repair must leave the defect measurable* -- **requires** a claim repairing a numeric defect
to quote the defective value at full precision, so the corpus's own methodology manufactures the
class, and it grows every time the loop works correctly.
"""

import argparse
import os
import re
import sys

#: A bare path is the shape a `Provenance` row uses.  A Markdown link target matches too and is
#: harmless: `check-corpus-links.py` also resolves it, and two gates agreeing is not a defect.
RESULT_PATH = re.compile(r"gpd/results/([A-Za-z0-9._+-]+\.json)")

#: The document kinds whose result-file references are a provenance claim.  `tools/` is excluded:
#: an emitter names the path it is about to WRITE, which need not exist yet.
DIRECTORIES = ("gpd/claims", "gpd/challenges", "gpd/tasks")
FILES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md", "TASKS.md", "CLAUDE.md", "JOURNAL.md", "README.md")


def documents(root):
    found = []
    for directory in DIRECTORIES:
        path = os.path.join(root, directory)
        if not os.path.isdir(path):
            continue
        found += [
            os.path.join(directory, name)
            for name in sorted(os.listdir(path))
            if name.endswith(".md")
        ]
    return found + [name for name in FILES if os.path.isfile(os.path.join(root, name))]


def broken_references(root, text, path):
    """[(path, line, referenced file)] for every named result file that does not exist."""
    available = set(os.listdir(os.path.join(root, "gpd", "results"))) \
        if os.path.isdir(os.path.join(root, "gpd", "results")) else set()
    found = []
    for number, line in enumerate(text.split("\n"), start=1):
        for match in RESULT_PATH.finditer(line):
            if match.group(1) not in available:
                found.append((path, number, match.group(1)))
    return found


def check(root):
    defects = []
    for path in documents(root):
        with open(os.path.join(root, path), encoding="utf-8", errors="replace") as handle:
            defects += broken_references(root, handle.read(), path)
    for path, line, name in defects:
        print("{}:{}\tMISSING-RESULT-FILE\t{}".format(path, line, name))
    print(
        "# {} missing result-file reference(s) in {} document(s)".format(
            len(defects), len(documents(root))
        )
    )
    return bool(defects)


def self_test(root):
    failures = []

    def ok(name, condition):
        print(("ok   " if condition else "FAIL ") + name)
        if not condition:
            failures.append(name)

    ok(
        "a named file that exists is not a defect",
        broken_references(root, "see gpd/results/T-1-brush-layer-stiffness.json", "x.md") == []
        or not os.path.isfile(os.path.join(root, "gpd/results/T-1-brush-layer-stiffness.json")),
    )
    ok(
        "a named file that does not exist is a defect, with its line",
        broken_references(root, "a\nsee gpd/results/T-9999-nothing.json\n", "x.md")
        == [("x.md", 2, "T-9999-nothing.json")],
    )
    ok(
        "a MARKDOWN LINK target is read too -- the gate does not depend on the bare form",
        broken_references(root, "[x](gpd/results/T-9999-nothing.json)", "x.md")
        == [("x.md", 1, "T-9999-nothing.json")],
    )
    ok(
        "a path with no .json extension is not a result-file reference",
        broken_references(root, "the gpd/results/ directory", "x.md") == [],
    )
    ok(
        "a hyphenated and dotted stem survives the character class",
        broken_references(root, "gpd/results/T-1d-a.b+c-9999.json", "x.md")
        == [("x.md", 1, "T-1d-a.b+c-9999.json")],
    )
    ok(
        "tools/ is NOT scanned -- an emitter names the path it is about to write",
        all(not path.startswith("tools/") for path in documents(root)),
    )
    ok(
        "the three corpus directories are scanned",
        all(
            any(path.startswith(directory) for path in documents(root))
            for directory in DIRECTORIES
        ),
    )
    ok(
        "the two deliverables are scanned, which is where a price is quoted",
        {"ANSWERS.md", "DECISIONS-FOR-NDI.md"} <= set(documents(root)),
    )
    print("self-test: {} failure(s)".format(len(failures)))
    return bool(failures)


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", default=".")
    parser.add_argument("--self-test", action="store_true")
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return 1 if self_test(arguments.root) else 0
    return 1 if check(arguments.root) else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
