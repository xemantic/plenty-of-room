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
# T-272 `P2` -- rewrite a study's result-file READS as typed handles.
#
#     tools/T-272-typed-input-handles.py            rewrite the tree
#     tools/T-272-typed-input-handles.py --dry-run  report what would change
#     tools/T-272-typed-input-handles.py --selftest
#
# TWO SHAPES, AND THE SECOND IS THE ONE THAT MATTERS.
#
#     File("gpd/results/T-3b-….json")   ->  ResultInputs.T_3B.file()
#     File(directory, "T-1d-….json")    ->  ResultInputs.T_1D.file(directory)
#
# The second is `CH-0092`'s: a path assembled from a directory in the CALLER and a name in a
# HELPER is invisible to a grep for either half, and it is how `C-0073` reported one reader of
# `T-1d` where there are three. Twenty of the census's sixty-one read edges are of that shape.
#
# WRITES ARE LEFT ALONE, DELIBERATELY. A study's own output stays `File("gpd/results/…")`, so
# `tools/check-entry-points.py` -- which finds a write by following the binding to `.writeText` --
# is untouched, and every remaining literal in the tree is a write by construction. That is what
# makes "a read is a handle" checkable by grep afterwards.
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = os.path.join(ROOT, "src", "main", "kotlin")

DIRECT = re.compile(r'File\(\s*"gpd/results/([A-Za-z0-9._-]+\.json)"\s*\)')
JOINED = re.compile(r'File\(\s*([A-Za-z][A-Za-z0-9_.]*)\s*,\s*"([A-Za-z0-9._-]+\.json)"\s*\)')
MULTILINE_JOINED = re.compile(
    r'File\(\s*\n\s*([A-Za-z][A-Za-z0-9_.]*)\s*,\s*\n?\s*"([A-Za-z0-9._-]+\.json)"\s*\n?\s*\)'
)

IMPORT = "import com.xemantic.nano.plentyofroom.structure.ResultInputs"


def _handles(root=ROOT):
    """{file name: property}, read from the GENERATED registry rather than recomputed.

    A converter that recomputed the property name from the file name would be a second
    implementation of `T-272-emit-result-inputs.py`'s collision rule, and the two would drift on
    the next collision. Read the declaration.
    """
    target = os.path.join(root, "src", "main", "kotlin", "structure", "ResultInputs.kt")
    text = open(target, encoding="utf-8").read()
    pairs = re.findall(r'val ([A-Z0-9_]+): ResultInput = ResultInput\("[^"]+", "([^"]+)"\)', text)
    return {name: prop for prop, name in pairs}


def written_names(text):
    """The result files this source WRITES, which must keep their literal."""
    import importlib.util

    path = os.path.join(ROOT, "tools", "result-reader-census.py")
    spec = importlib.util.spec_from_file_location("result_reader_census", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module.written_literals(text)


def convert(text, handles, written):
    """`text` with every READ rewritten as a handle. Returns (text, number rewritten)."""
    count = [0]

    def direct(match):
        name = match.group(1)
        if name in written or name not in handles:
            return match.group(0)
        count[0] += 1
        return "ResultInputs.%s.file()" % handles[name]

    def joined(match):
        directory, name = match.group(1), match.group(2)
        if name in written or name not in handles:
            return match.group(0)
        count[0] += 1
        return "ResultInputs.%s.file(%s)" % (handles[name], directory)

    text = DIRECT.sub(direct, text)
    text = MULTILINE_JOINED.sub(joined, text)
    text = JOINED.sub(joined, text)
    return text, count[0]


def add_import(text):
    if IMPORT in text or "package com.xemantic.nano.plentyofroom.structure" in text:
        return text
    lines = text.split("\n")
    indices = [i for i, line in enumerate(lines) if line.startswith("import ")]
    before = [i for i in indices if lines[i] < IMPORT]
    lines.insert((max(before) + 1) if before else min(indices), IMPORT)
    return "\n".join(lines)


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    handles = {"T-3b-x.json": "T_3B", "T-1d-y.json": "T_1D", "T-9-z.json": "T_9"}
    out, n = convert('val f = File("gpd/results/T-3b-x.json")', handles, set())
    check("a direct read becomes a handle", out, "val f = ResultInputs.T_3B.file()")
    check("and is counted", n, 1)
    out, n = convert('val f = File(directory, "T-1d-y.json")', handles, set())
    check("a joined read carries its directory", out, "val f = ResultInputs.T_1D.file(directory)")
    out, n = convert(
        'val f = File(\n            resultsDirectory,\n            "T-1d-y.json"\n        )',
        handles, set()
    )
    check("a joined read broken over lines too", out, "val f = ResultInputs.T_1D.file(resultsDirectory)")
    # A WRITE keeps its literal, which is what `check-entry-points.py` follows.
    out, n = convert('val output = File("gpd/results/T-9-z.json")', handles, {"T-9-z.json"})
    check("a write is left alone", out, 'val output = File("gpd/results/T-9-z.json")')
    check("and is not counted", n, 0)
    # A file with no handle is left alone rather than rewritten to a name that does not exist.
    out, n = convert('File("gpd/results/T-999-none.json")', handles, set())
    check("an unknown file keeps its literal", out, 'File("gpd/results/T-999-none.json")')
    # A prose mention is not a `File(` construction and must not move.
    out, n = convert('"read from gpd/results/T-3b-x.json"', handles, set())
    check("prose is untouched", n, 0)
    check("the import is added", IMPORT in add_import("package a\n\nimport b.C\n"), True)
    check(
        "and withheld inside its own package",
        IMPORT in add_import("package com.xemantic.nano.plentyofroom.structure\n\nimport b.C\n"),
        False,
    )
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    dry = "--dry-run" in argv
    handles = _handles()
    total = 0
    files = 0
    for base, _, names in os.walk(SOURCES):
        for name in sorted(names):
            if not name.endswith(".kt"):
                continue
            path = os.path.join(base, name)
            text = open(path, encoding="utf-8").read()
            converted, count = convert(text, handles, written_names(text))
            if not count:
                continue
            converted = add_import(converted)
            files += 1
            total += count
            print("%s%s: %d read(s)" % ("would rewrite " if dry else "rewrote ", path, count))
            if not dry:
                open(path, "w", encoding="utf-8").write(converted)
    print("%d read(s) in %d file(s)" % (total, files))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
