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
# T-334 -- MUTATION COVERAGE FOR `tools/T-334-gate-census.py`.
#
#     tools/T-334-mutation-test.py
#
# `C-0176`'s standard in BOTH directions: every rule must fail a NAMED test when it is reverted
# and when it is over-widened.  A table that only ever narrows becomes a pattern, and a pattern is
# what a per-rule judgement refuses.  Six rows below RESTORE a predecessor's defect -- the
# literal-only tool pattern (`CH-0286`), the missing reachability filter, the `--self-test`
# exclusion, the disjointness assumption -- and five over-widen a rule.
#
# `C-0185`/`CH-0237`'s subtracted baseline runs FIRST and its named failures are printed: without
# it a fixture defect reads as `0 survivors` (the quiet direction) or as every row killed by one
# and the same error (the loud one), and the headline means nothing either way.  The anchor count
# is asserted at exactly 1 per row, which is `C-0185`'s other half -- a harness that does not
# assert its anchors reports `killed` off a mutation that never applied.
#
# The row shape is `killed-by`, declared in `tools/P-31-harness-census.py`'s `HARNESSES` table
# (`T-306`: a harness's output is an INTERFACE and it is declared, never inferred).  The killers
# go on their OWN lines, because printed after the name they are captured as part of the LABEL and
# `tools/T-295-mutation-input-census.py` refuses the harness for *row labels drift*.
"""Mutation coverage for tools/T-334-gate-census.py."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SUBJECT = "T-334-gate-census.py"

#: (name, file, old, new).  `old` must occur EXACTLY once in the subject.
MUTATIONS = (
    # --- the reachability filter, which is the whole finding -----------------------------------
    (
        "NARROW the reachability split away, so an unreachable Exec task counts as build-failing",
        SUBJECT,
        '        (reachable if row["reachable"] else unreachable).update(row["tools"])',
        "        reachable.update(row[\"tools\"])",
    ),
    (
        "NARROW dependsOn to nothing, so every Exec task reads as unreachable",
        SUBJECT,
        "    match = _TEST_DEPENDS_ON.search(text)\n    if not match:\n        return set()",
        "    match = None\n    if not match:\n        return set()",
    ),
    (
        "WIDEN dependsOn to every quoted string in the build script",
        SUBJECT,
        '    return set(re.findall(r\'"([^"]+)"\', match.group(1)))',
        '    return set(re.findall(r\'"([^"]+)"\', text))',
    ),
    (
        "NARROW the dependsOn scan onto the RAW text, so a commented-out entry counts",
        SUBJECT,
        "    text = p31.strip_kotlin_comments(build_text)\n    match = _TEST_DEPENDS_ON.search(text)",
        "    text = build_text\n    match = _TEST_DEPENDS_ON.search(text)",
    ),
    # --- CH-0286's own defect, restored --------------------------------------------------------
    (
        "NARROW the in-span tool pattern back to a LITERAL path, which is CH-0286's defect",
        SUBJECT,
        '_TOOL_IN_SPAN = re.compile(r\'"[^"]*?([A-Za-z0-9_.\\-]+\\.(?:py|sh))"\')',
        '_TOOL_IN_SPAN = re.compile(r\'"\\$projectDir/tools/([A-Za-z0-9_.\\-]+\\.(?:py|sh))"\')',
    ),
    (
        "NARROW route B by the --self-test flag, so a failing self-test stops counting",
        SUBJECT,
        '        (reachable if row["reachable"] else unreachable).update(row["tools"])',
        '        if "--self-test" in row["arguments"]:\n            continue\n'
        '        (reachable if row["reachable"] else unreachable).update(row["tools"])',
    ),
    # --- the union, and the disjointness C-0210 asserted ---------------------------------------
    (
        "WIDEN the union into a SUM, which is the disjointness C-0210 asserted and does not hold",
        SUBJECT,
        "    return sorted(set(route_a(verify_text)) | set(reachable))",
        "    return sorted(route_a(verify_text)) + sorted(reachable)",
    ),
    # --- route A: a use, not a mention ---------------------------------------------------------
    (
        "WIDEN route A to every word of a line, so an echo counts as a run",
        SUBJECT,
        "        for word in p31.shell_command_words(verify_text)",
        "        for word in verify_text.split()",
    ),
    # --- the commandLine span: a use, not a description ----------------------------------------
    (
        "WIDEN the commandLine span to the whole script, so a description counts as a wiring",
        SUBJECT,
        '        out.append((start, text[start:i]))\n        start = text.find("commandLine(", i)',
        '        out.append((start, text))\n        start = text.find("commandLine(", i)',
    ),
    (
        "NARROW the Exec-block bounds to the whole file, so a description leaks across tasks",
        SUBJECT,
        "        (start, registered[index + 1][0] if index + 1 < len(registered) else len(text), name)",
        "        (start, len(text), name)",
    ),
    # --- arm 1, which is the deliverable -------------------------------------------------------
    (
        "NARROW arm 1 to one direction, so a BY-HAND harness wired into :test passes",
        SUBJECT,
        "    for name in sorted(by_hand - unreachable):",
        "    for name in sorted(set()):",
    ),
    (
        "NARROW arm 1's other direction, so an undeclared unreachable task passes",
        SUBJECT,
        "    for name in sorted(unreachable - by_hand):",
        "    for name in sorted(set()):",
    ),
    (
        "NARROW the BY-HAND parse so the sentinel NAME is not resolved and no row is declared",
        SUBJECT,
        "            elif isinstance(shape, ast.Name):\n"
        "                shapes.append(sentinel if shape.id == \"BY_HAND\" else shape.id)",
        "            elif isinstance(shape, ast.Name):\n                pass",
    ),
    (
        "WIDEN the BY-HAND parse to every row, declared or not",
        SUBJECT,
        "        if sentinel in shapes:\n            out.append(elements[0].value)",
        "        if True:\n            out.append(elements[0].value)",
    ),
    (
        "NARROW the sentinel to a hardcoded literal instead of reading it from the table",
        SUBJECT,
        "            if \"BY_HAND\" in targets and isinstance(node.value, ast.Constant):\n"
        "                sentinel = node.value.value",
        "            if False:\n                sentinel = node.value.value",
    ),
    # --- arms 2 and 3 --------------------------------------------------------------------------
    (
        "NARROW arm 2 away, so a description may name a tool its task does not run",
        SUBJECT,
        "            if named not in row[\"tools\"]:",
        "            if False:",
    ),
    (
        "NARROW arm 3 away, so an invoked tool that does not exist passes",
        SUBJECT,
        "    for name in reading[\"missing\"]:",
        "    for name in []:",
    ),
    (
        "NARROW arm 3's executable half away",
        SUBJECT,
        "    for name in reading[\"notExecutable\"]:",
        "    for name in []:",
    ),
    # --- the refusals, and the residue ---------------------------------------------------------
    (
        "NARROW the ./gradlew test refusal away, so route B rests on a premise nobody checked",
        SUBJECT,
        '    if "./gradlew test" not in verify_text:',
        "    if False:",
    ),
    (
        "NARROW the pipefail refusal away",
        SUBJECT,
        '    if "set -euo pipefail" not in verify_text:',
        "    if False:",
    ),
    (
        "NARROW the import residue to one step instead of the closure",
        SUBJECT,
        "        following -= seen\n        seen |= following\n        frontier = following",
        "        following -= seen\n        seen |= following\n        frontier = set()",
    ),
    (
        "WIDEN the residue to include what an invocation already names",
        SUBJECT,
        "    return sorted(seen - set(seed))",
        "    return sorted(seen)",
    ),
    # --- the decomposition ---------------------------------------------------------------------
    (
        "NARROW the decomposition by dropping the overlap term, so the three no longer sum",
        SUBJECT,
        '            "overlapAssertedAway": -len(reading["overlap"]),',
        '            "overlapAssertedAway": 0,',
    ),
)


def _run(work):
    run = subprocess.run(
        [sys.executable, os.path.join(work, "tools", SUBJECT), "--self-test"],
        capture_output=True, text=True, cwd=work,
    )
    named = [line[6:].strip() for line in run.stdout.splitlines() if line.startswith("FAIL ")]
    finished = any(line.startswith("# ") and "self-test(s)" in line
                   for line in run.stdout.splitlines())
    return run.returncode, named, finished


def main():
    work = tempfile.mkdtemp(prefix="T-334-mutation.")
    try:
        shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
        # `CH-0237`.  The UNMUTATED copy runs first and its named failures are subtracted; the
        # local is named for what it is because `tools/P-31-harness-census.py` DERIVES
        # `measuresBaseline` from a harness's own identifiers.
        baseline_code, baseline_failures, baseline_finished = _run(work)
        if baseline_code != 0 or not baseline_finished:
            print("BASELINE IS NOT GREEN -- nothing below is a measurement")
            for name in baseline_failures:
                print("   baseline failure:", name)
            return 2
        print("baseline: green, 0 named failures")
        source = open(os.path.join(ROOT, "tools", SUBJECT), encoding="utf-8").read()
        survivors = 0
        for name, _path, old, _new in MUTATIONS:
            count = source.count(old)
            if count != 1:
                print("ANCHOR  %-70s occurs %d times, expected 1" % (name, count))
                survivors += 1
        target = os.path.join(work, "tools", SUBJECT)
        for name, _path, old, new in MUTATIONS:
            if source.count(old) != 1:
                continue
            open(target, "w", encoding="utf-8").write(source.replace(old, new, 1))
            code, named, finished = _run(work)
            open(target, "w", encoding="utf-8").write(source)
            # A CRASH is not a named test: a suite that stops prints a traceback and exits 1
            # exactly as one failing test does, so the completion line is what separates them
            # (`T-306`).  An unfinished suite is a SURVIVOR.
            if code == 0 or not finished:
                survivors += 1
                print("%-8s %-70s %s" % (
                    "SURVIVES", name,
                    "no named test failed" if finished else "the suite did not finish"))
            else:
                print("killed by %d named test(s)  %s" % (len(named), name))
                for failing in named[:3]:
                    print("        FAIL: %s" % failing)
        print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), survivors))
        return 1 if survivors else 0
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
