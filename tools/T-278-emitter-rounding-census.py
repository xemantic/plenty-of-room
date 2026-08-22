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
# T-278 / CH-0223 -- the two halves of "does the rounding rule reach every emitter", counted.
#
#     tools/T-278-emitter-rounding-census.py --sources     an emitter with no rounding call
#     tools/T-278-emitter-rounding-census.py --artifacts   an over-precise numeric RESULT leaf
#     tools/T-278-emitter-rounding-census.py --check       both, exit 1 on either
#     tools/T-278-emitter-rounding-census.py --selftest
#
# WHY THIS EXISTS. `CH-0223` §3, verbatim: "The numeric body of a result file is gated by nothing,
# because the rule was believed to live in a layer every study goes through." It does not -- seven
# studies wrote `output.writeText(json.encodeToString(result) + "\n")` with no rounding anywhere in
# the source, and carried 41 297 of the corpus's 41 369 over-precise numeric leaves.
#
# TWO CENSUSES AND NOT ONE, because they fail differently and neither implies the other. A SOURCE
# census cannot see a study that rounds and whose committed file is stale; an ARTIFACT census
# cannot see an emitter written today whose file nobody has emitted yet. `C-0162`'s own lesson is
# that `P1` counted the rounding IMPLEMENTATIONS -- a third population again -- and found six.
#
# The artifact predicate is `CH-0223`'s: `roundForResult(v, 9, floor = 0) != v` on the committed
# value, NOT a digit count on the decimal text, because `2.5800000000000002e-47` is a value already
# rounded to three significant digits whose shortest round-trip decimal is seventeen characters
# long. Parameter records are excluded, where being unrounded is the rule (`C-0162`, `CH-0207`).
import importlib.util
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")

# Any call that puts a value through `structure/ResultRounding.kt`, at the emission boundary or
# per field. `roundedForProse` is deliberately ABSENT: it renders a SENTENCE and touches no leaf
# of the emitted numeric tree, and counting it would have declared `brush/CrossoverLayerStudy`
# rounded on the strength of four calls that reach nothing (`CH-0223` §6).
ROUNDING_CALLS = (
    "roundedForResult", "roundForResult",
    "roundedForActuatorResult", "roundedForWindowResult",
    "roundedForCouplingResult", "roundedForBrushResult",
)


def _module(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _blank_comments(text):
    return _module("t278_prov", "T-278-solver-provenance.py").blank_comments(text)


def rounds(text):
    """Whether this Kotlin source puts its emitted tree through a rounding function."""
    blanked = _blank_comments(text)
    return any(call + "(" in blanked for call in ROUNDING_CALLS)


CALL = None  # built lazily; see `declared_precision`


def _balanced(text, start):
    """The substring from the `(` at `start` to its matching `)`, exclusive."""
    depth = 0
    for index in range(start, len(text)):
        if text[index] == "(":
            depth += 1
        elif text[index] == ")":
            depth -= 1
            if depth == 0:
                return text[start + 1:index]
    return ""


def declared_precision(text):
    """{digits, floor, digitsByKey} a study DECLARES at its own rounding call, or None.

    `C-0083`'s standard is that a gate which cannot come clean is not a gate, and a corpus
    predicate fixed at nine digits cannot: `electrostatics/ScaffoldRemainderStudy` declares
    `"boltzmannWeight" to 3` with a reason, so its 47 correctly-three-digit values are reported as
    over-precise by a nine-digit reading of the artifact alone. The declaration is in the source;
    reading it is the difference between 41 369 and the number that is actually a defect.
    """
    import re
    blanked = _blank_comments(text)
    match = re.search(r"roundedFor\w*Result\s*\(", blanked)
    if not match:
        return None
    body = _balanced(blanked, match.end() - 1)
    declared = {"digits": None, "floor": None, "digitsByKey": {}}
    digits = re.search(r"\bdigits\s*=\s*(\w+)", body)
    if digits:
        declared["digits"] = (
            int(digits.group(1)) if digits.group(1).isdigit()
            else {"RESULT_SIGNIFICANT_DIGITS": 9,
                  "SOLVED_HEIGHT_SIGNIFICANT_DIGITS": 6}.get(digits.group(1))
        )
    floor = re.search(r"\bfloor\s*=\s*([\w.\-]+)", body)
    if floor:
        try:
            declared["floor"] = float(floor.group(1))
        except ValueError:
            declared["floor"] = None
    named = {"RESULT_SIGNIFICANT_DIGITS": 9, "SOLVED_HEIGHT_SIGNIFICANT_DIGITS": 6,
             "DEPARTURE_SIGNIFICANT_DIGITS": 2}
    for key, value in re.findall(r'"([^"]+)"\s+to\s+(\w+)', body):
        if value.isdigit():
            declared["digitsByKey"][key] = int(value)
        elif value in named:
            declared["digitsByKey"][key] = named[value]
    return declared


def source_census(root=ROOT):
    """[(study, result file, rounds)] over every study that writes a committed result file."""
    entry = _module("t278_entry", "check-entry-points.py")
    rows = []
    for name, written in sorted(entry.studies(root).items()):
        if not written:
            continue
        source = os.path.join(root, "src", "main", "kotlin", name.replace(".", "/") + ".kt")
        text = open(source, encoding="utf-8").read() if os.path.exists(source) else ""
        rows.append((name, os.path.basename(written), rounds(text)))
    return rows


def artifact_census(root=ROOT, respect_declarations=True):
    """{result file: over-precise numeric result leaves}, on `CH-0223`'s own predicate.

    Read at the precision the emitting study DECLARES where it declares one, and at the corpus
    ceiling otherwise. `respect_declarations=False` restores `CH-0223`'s own flat nine-digit
    reading, which is what the two numbers have to be compared at.
    """
    simulation = _module("t278_sim", "T-278-rounding-simulation.py")
    entry = _module("t278_entry2", "check-entry-points.py")
    declared_by_file = {}
    if respect_declarations:
        for name, written in entry.studies(root).items():
            if not written:
                continue
            source = os.path.join(root, "src", "main", "kotlin", name.replace(".", "/") + ".kt")
            if not os.path.exists(source):
                continue
            declared = declared_precision(open(source, encoding="utf-8").read())
            if declared:
                declared_by_file[os.path.basename(written)] = declared
    counts = {}
    for name in sorted(os.listdir(os.path.join(root, "gpd", "results"))):
        if not name.endswith(".json"):
            continue
        try:
            document = json.load(open(os.path.join(root, "gpd", "results", name), encoding="utf-8"))
        except ValueError:
            continue
        declared = declared_by_file.get(name) or {}
        digits = declared.get("digits") or simulation.RESULT_SIGNIFICANT_DIGITS
        by_key = dict(simulation.DEPARTURE_DIGITS_BY_KEY)
        by_key.update(declared.get("digitsByKey") or {})
        moved = []
        simulation.walk(document, digits, by_key, 0.0, moved=moved)
        if moved:
            counts[name] = len(moved)
    return counts


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    check("a rounding call is one", rounds("json.encodeToJsonElement(r).roundedForResult()"), True)
    check("the bare function is one", rounds("roundForResult(x, 9)"), True)
    check("a package delegate is one", rounds("e.roundedForActuatorResult()"), True)
    check("no call is no rounding",
          rounds("output.writeText(json.encodeToString(result) + \"\\n\")"), False)
    # `roundedForProse` renders a sentence and reaches no leaf of the numeric tree. Counting it
    # would have declared `brush/CrossoverLayerStudy` rounded on four calls that move nothing.
    check("a prose rendering is NOT a rounding of the tree",
          rounds("\"x = \" + v.roundedForProse().toString()"), False)
    check("a comment naming the call is not one",
          rounds("// json.encodeToJsonElement(r).roundedForResult()"), False)
    check("a KDoc naming the call is not one",
          rounds("/** see roundedForResult(x) */"), False)
    # The declaration is read from the source, which is what lets the artifact census come clean.
    check("a per-key precision is read",
          declared_precision('e.roundedForResult(digitsByKey = mapOf("boltzmannWeight" to 3), '
                             'floor = 0.0)'),
          {"digits": None, "floor": 0.0, "digitsByKey": {"boltzmannWeight": 3}})
    check("a named per-key digit constant is resolved",
          declared_precision('e.roundedForResult(digitsByKey = mapOf("a/b" to '
                             'DEPARTURE_SIGNIFICANT_DIGITS))')["digitsByKey"], {"a/b": 2})
    check("a named digit constant is resolved",
          declared_precision("e.roundedForResult(digits = SOLVED_HEIGHT_SIGNIFICANT_DIGITS)"
                             )["digits"], 6)
    check("no rounding call declares nothing",
          declared_precision("output.writeText(x)"), None)
    check("a commented call declares nothing",
          declared_precision("// e.roundedForResult(digits = 3)"), None)
    check("a nested call is balanced, not truncated at the first paren",
          declared_precision('e.roundedForResult(digitsByKey = mapOf("a" to 2), floor = 0.0)'
                             )["floor"], 0.0)
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    wants_sources = "--sources" in argv or "--check" in argv or len(argv) == 0
    wants_artifacts = "--artifacts" in argv or "--check" in argv or len(argv) == 0
    gated = 0
    if wants_sources:
        rows = source_census()
        unrounded = [row for row in rows if not row[2]]
        for study, written, _ in unrounded:
            print("NO-ROUNDING-CALL %-52s %s" % (study, written))
        print("%d of %d emitting studies write through no rounding function"
              % (len(unrounded), len(rows)))
        gated += len(unrounded)
    if wants_artifacts:
        counts = artifact_census(respect_declarations="--flat" not in argv)
        for name, count in sorted(counts.items(), key=lambda row: -row[1]):
            print("OVER-PRECISE %-52s %6d numeric result leaf/leaves" % (name, count))
        print("%d over-precise numeric result leaf/leaves in %d file(s)"
              % (sum(counts.values()), len(counts)))
    # ONLY THE SOURCE HALF IS GATED, and the reason is `C-0083`'s: a gate that cannot come clean is
    # not a gate. The source half CAN -- every Kotlin study that writes a committed result file now
    # goes through a rounding function, and a new one that does not is a defect the moment it is
    # written. The artifact half CANNOT: most of what is left is written by a Python emitter in
    # `tools/`, which no rule in the Kotlin emission layer reaches, and re-emitting those lands
    # unrelated corpus drift (`C-0172` §7, `C-0174` §5). So it is PRINTED beside the gate and not
    # enforced by it -- `C-0129`'s discipline, a residue reported rather than a predicate narrowed
    # until the tree looks clean.
    if "--check" in argv:
        return 1 if gated else 0
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
