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
# T-272 -- who DECLARES an emission header and whose committed result file CARRIES one.
#
#     tools/T-272-header-census.py            the four states, and the residue with its cost
#     tools/T-272-header-census.py --check    exit 1 on a DECLARED-NOT-EMITTED study
#     tools/T-272-header-census.py --selftest
#
# WHY THIS EXISTS. `P3` and `P4` put a `lattice` tag and a `regime` block on every emitted record,
# and the declaration is cheap while the emission is a full corpus re-run -- 411 measured minutes
# over 71 of 124 emitting studies with 53 untimed, at least seven hours a pass. So a partial
# delivery is expected, and the ONE state that must not be silent is the one where a study's
# source says something its committed file does not:
#
#   BOTH       the study declares a header and its result file carries one          -- done
#   DECLARED   the source declares one, the committed file does not                 -- owed a re-run
#   EMITTED    the file carries one and the source no longer declares it            -- a REGRESSION
#   NEITHER    neither                                                              -- not started
#
# `CLAUDE.md` records the failure this replaces: a study edited and not re-run is exactly the
# staleness `C-0101` left in `T-157` for six iterations, and the only reason it was ever found was
# a reproduction residual nobody was reading. A count that a `--check` can fail on is cheaper.
#
# It reads the SOURCE for the declaration and the ARTIFACT for the emission, which is the whole
# point: a census that asked one of them twice could not see a disagreement between them.
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
SOURCES = os.path.join(ROOT, "src", "main", "kotlin")

DECLARATION = re.compile(r"\.withEmissionHeader\(")


def _blank_comments(text):
    """`text` with every comment replaced by spaces, borrowed from the header transformer."""
    import importlib.util

    path = os.path.join(ROOT, "tools", "T-272-add-emission-header.py")
    spec = importlib.util.spec_from_file_location("t272_header", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module.blank_comments(text)

BOTH, DECLARED, EMITTED, NEITHER = "BOTH", "DECLARED-NOT-EMITTED", "EMITTED-NOT-DECLARED", "NEITHER"


def declares(text):
    """Whether this Kotlin source puts an emission header on its own write.

    Comments are blanked first. Every study in this tree announces its own emission in a KDoc
    line, and a census that counted `* the header is added by .withEmissionHeader(...)` as a
    declaration would report the residue as smaller than it is -- which is the one direction a
    residue count must never fail in.
    """
    return bool(DECLARATION.search(_blank_comments(text)))


def carries(document):
    """Whether a parsed result file carries an `emission` block with BOTH of its keys.

    Both, never either: a `lattice` without a `regime` is a file that answers `P3` and not `P4`,
    and reporting it as done would make the residue of `P4` invisible.

    The block is namespaced. A top-level `lattice` is NOT a header -- `T-152` has carried one since
    long before this task, holding a list of the lattice quantities it tabulates -- so reading the
    bare key would count that file as done and no file as colliding.
    """
    if not isinstance(document, dict):
        return False
    header = document.get("emission")
    return isinstance(header, dict) and "lattice" in header and "regime" in header


def state(declared, carried):
    if declared and carried:
        return BOTH
    if declared:
        return DECLARED
    if carried:
        return EMITTED
    return NEITHER


def studies(root=ROOT):
    """{study: written result file}, borrowed from `check-entry-points.py` so the two agree."""
    import importlib.util

    path = os.path.join(root, "tools", "check-entry-points.py")
    spec = importlib.util.spec_from_file_location("check_entry_points", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return {name: written for name, written in module.studies(root).items() if written}


def census(root=ROOT):
    rows = []
    written = {}
    for name, output in sorted(studies(root).items()):
        source = os.path.join(root, "src", "main", "kotlin", name.replace(".", "/") + ".kt")
        text = open(source, encoding="utf-8").read() if os.path.exists(source) else ""
        artifact = os.path.join(root, output)
        document = None
        if os.path.exists(artifact):
            try:
                document = json.load(open(artifact, encoding="utf-8"))
            except ValueError:
                document = None
        written[os.path.basename(output)] = True
        rows.append(
            {
                "study": name,
                "resultFile": os.path.basename(output),
                "declares": declares(text),
                "carries": carries(document),
                "state": state(declares(text), carries(document)),
            }
        )
    # Everything committed that no Kotlin study writes -- `C-0162`'s sixteen Python emitters plus
    # whatever else. No rule in the Kotlin emission layer reaches any of them.
    outside = []
    for name in sorted(os.listdir(os.path.join(root, "gpd", "results"))):
        if not name.endswith(".json") or name in written:
            continue
        try:
            document = json.load(open(os.path.join(root, "gpd", "results", name), encoding="utf-8"))
        except ValueError:
            document = None
        outside.append({"resultFile": name, "carries": carries(document)})
    return rows, outside


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    check("a declaration is the call", declares("x.withEmissionHeader(LatticeTag.SQUARE, null)"), True)
    check("a comment naming the call is not one",
          declares("// x.withEmissionHeader(LatticeTag.SQUARE, null)"), False)
    check("a KDoc naming the call is not one",
          declares("/** see x.withEmissionHeader(a, b) */"), False)
    check("no call, no declaration", declares("x.roundedForResult()"), False)
    check("both keys inside the block is carried",
          carries({"emission": {"lattice": "square", "regime": None}}), True)
    check("one key is not", carries({"emission": {"lattice": "square"}}), False)
    check("regime alone is not", carries({"emission": {"regime": None}}), False)
    check("a non-object is not", carries([1, 2]), False)
    # `T-152` carries a top-level `lattice` of its own and is NOT a header.
    check("a bare top-level lattice is not a header",
          carries({"lattice": ["a table"], "regime": "MUSHROOM"}), False)
    check("an emission block that is not an object is not a header",
          carries({"emission": "yes"}), False)
    check("state both", state(True, True), BOTH)
    check("state declared", state(True, False), DECLARED)
    check("state emitted", state(False, True), EMITTED)
    check("state neither", state(False, False), NEITHER)
    # The regression state must be distinguishable from the residue state -- they are opposite
    # defects and only one of them is a debt.
    check("the two one-sided states differ", state(True, False) != state(False, True), True)
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def literal_reads(root=ROOT):
    """{source: {result file}} still READ by a `File("gpd/results/…")` literal -- `T-272`'s `P2`.

    The read/write distinction is `tools/result-reader-census.py`'s, borrowed rather than
    reimplemented: a `File(...)` whose binding reaches `.writeText` is the study's own OUTPUT and
    keeps its literal, because `tools/check-entry-points.py` finds a study's result file by
    exactly that reading.
    """
    import importlib.util

    path = os.path.join(root, "tools", "result-reader-census.py")
    spec = importlib.util.spec_from_file_location("result_reader_census", path)
    census_module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(census_module)
    found = {}
    for base, _, names in os.walk(os.path.join(root, "src", "main", "kotlin")):
        for name in sorted(names):
            if not name.endswith(".kt"):
                continue
            source = os.path.join(base, name)
            reads = census_module.read_literals(open(source, encoding="utf-8").read())
            if reads:
                found[os.path.relpath(source, root)] = sorted(reads)
    return found


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    if "--reads" in argv:
        found = literal_reads()
        for source, reads in sorted(found.items()):
            print("READS-BY-PATH %s %s" % (source, ", ".join(reads)))
        print("%d main source(s) read a result file by path rather than by handle" % len(found))
        return 1 if found else 0
    rows, outside = census()
    counts = {}
    for row in rows:
        counts[row["state"]] = counts.get(row["state"], 0) + 1
    print("%d studies write a committed result file" % len(rows))
    for name in (BOTH, DECLARED, EMITTED, NEITHER):
        print("  %-22s %d" % (name, counts.get(name, 0)))
    print(
        "%d committed result files are written by no Kotlin study; %d of them carry a header"
        % (len(outside), sum(1 for row in outside if row["carries"]))
    )
    if "--verbose" in argv:
        for row in rows:
            if row["state"] != BOTH:
                print("  %-22s %s" % (row["state"], row["resultFile"]))
    if "--check" in argv:
        bad = [row for row in rows if row["state"] == EMITTED]
        for row in bad:
            print("REGRESSION %s carries a header its study no longer declares" % row["resultFile"])
        return 1 if bad else 0
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
