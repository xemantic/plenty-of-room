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
# `T-321` -- MUTATION COVERAGE FOR `tools/T-321-dynamic-guard-probe.py`.
#
#     tools/T-321-mutation-test.py
#
# `C-0176`'s standard, in BOTH directions: every rule must fail a NAMED test when it is reverted
# AND when it is over-widened.  The probe's whole claim is that it can see a write a CHECKSUM
# cannot, so the rows that matter most are the ones that narrow it back to a checksum -- if
# `moved_paths` compares sizes only, or `wrote` forgets `touched`, the instrument silently becomes
# the one `CH-0268` already had, and the reading falls from 7 to 5 with nothing to say so.
#
# `C-0185`/`CH-0237`'s baseline check runs FIRST and refuses outright if it is not green: without
# it a fixture defect reads as `0 survivor(s)` (the quiet direction) or as every row surviving
# (the loud one), and the headline column means nothing either way.  The anchor count is asserted
# at exactly 1 per row, which is `C-0185`'s other half -- a harness that does not assert its
# anchors reports `killed` off a mutation that never applied.
#
# THE FIXTURE LAYOUT IS A DEPENDENCY DECLARATION (`C-0195`).  This harness copies `tools/` and
# nothing else, and that is sufficient because the subject's self-tests reach outside `tools/`
# nowhere: `cli_guard.writers()` lists `tools/`, `observe` is called on `tools/`, and every
# filesystem test builds its own `TemporaryDirectory`.  If a future self-test reads `gpd/` or
# `src/`, this copy stops being the tree it needs and the baseline check is what will say so.
#
# ONE RULE IS DELIBERATELY NOT MUTATED, and saying which is the point of `C-0161`.  `observe`
# prunes `__pycache__` directories from the walk (`name not in IGNORED`); reverting that changes
# no verdict, because `ignored(relative)` filters the same files one level down.  It is an
# optimisation whose observable effect is DUPLICATED, and a duplicated rule is invisible to a
# mutation of either copy -- so it is named here rather than reported as a survivor.
"""Mutation coverage for T-321's dynamic guard probe."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SUBJECT = "T-321-dynamic-guard-probe.py"

#: (name, file, old, new).  `old` must occur EXACTLY once in the subject.
MUTATIONS = (
    # --- the instrument's own noise floor -------------------------------------------------------
    (
        "NARROW the observer's footprint filter to nothing",
        SUBJECT,
        '    return any(part in IGNORED for part in parts) or relative_path.endswith(".pyc")',
        "    return False",
    ),
    (
        "NARROW the footprint filter to the .pyc suffix, dropping the cache directory",
        SUBJECT,
        "    return any(part in IGNORED for part in parts) or",
        "    return False or",
    ),
    (
        "NARROW the footprint filter to the cache directory, dropping the .pyc suffix",
        SUBJECT,
        'for part in parts) or relative_path.endswith(".pyc")',
        "for part in parts)",
    ),
    (
        "WIDEN the footprint filter from a path COMPONENT to a substring",
        SUBJECT,
        "    parts = relative_path.replace(os.sep, \"/\").split(\"/\")\n"
        "    return any(part in IGNORED for part in parts)",
        "    parts = [relative_path]\n"
        "    return any(part in relative_path for part in IGNORED)",
    ),
    # --- the detector, which is the whole difference from a checksum probe ----------------------
    (
        "NARROW the detector to PRESENCE, which is a checksum probe with no hashing",
        SUBJECT,
        "                  if before.get(path) != after.get(path))",
        "                  if (path in before) != (path in after))",
    ),
    (
        "NARROW the detector to SIZE, which cannot see a byte-identical rewrite",
        SUBJECT,
        "    return sorted(path for path in set(before) | set(after)\n"
        "                  if before.get(path) != after.get(path))",
        "    return sorted(path for path in set(before) | set(after)\n"
        "                  if before.get(path, (0,))[0] != after.get(path, (0,))[0])",
    ),
    # --- the classifier -------------------------------------------------------------------------
    (
        "NARROW the classifier so nothing is ever TOUCHED",
        SUBJECT,
        '        "touched": sorted(path for path in both\n'
        "                          if before_digest[path] == after_digest[path]),",
        '        "touched": [],',
    ),
    (
        "INVERT the CONTENT test, so a moved byte reads as a rewrite and back",
        SUBJECT,
        '        "content": sorted(path for path in both\n'
        "                          if before_digest[path] != after_digest[path]),",
        '        "content": sorted(path for path in both\n'
        "                          if before_digest[path] == after_digest[path]),",
    ),
    # --- the two readings -------------------------------------------------------------------------
    (
        "NARROW `wrote` back to the checksum reading by dropping TOUCHED",
        SUBJECT,
        '    return bool(record["created"] or record["content"]\n'
        '                or record["touched"] or record["deleted"])',
        '    return bool(record["created"] or record["content"]\n'
        '                or record["deleted"])',
    ),
    (
        "WIDEN the checksum reading to include TOUCHED, collapsing the two arms into one",
        SUBJECT,
        '    return bool(record["created"] or record["content"] or record["deleted"])',
        '    return bool(record["created"] or record["content"] or record["deleted"]\n'
        '                or record["touched"])',
    ),
    # --- the second under-report: a run that died before it could write --------------------------
    (
        "NARROW a refusal to exit 0, so this repository's own exit 2 reads as a crash",
        SUBJECT,
        '    return not wrote(record) and record["exitCode"] not in NOT_A_FAILURE',
        '    return not wrote(record) and record["exitCode"] != 0',
    ),
    (
        "WIDEN the not-a-failure set to admit exit 1, so a crash reads as a refusal",
        SUBJECT,
        "NOT_A_FAILURE = (0, 2)",
        "NOT_A_FAILURE = (0, 1, 2)",
    ),
    (
        "DROP the wrote-nothing precondition, so a tool that crashed AFTER writing reads silent",
        SUBJECT,
        "    return not wrote(record) and record[\"exitCode\"]",
        "    return True and record[\"exitCode\"]",
    ),
    # --- the instrument's measured resolution ------------------------------------------------------
    (
        "SILENCE the mtime granularity measurement",
        SUBJECT,
        "    return min(steps) if steps else None",
        "    return None",
    ),
    (
        "SILENCE the interpreter-startup measurement, removing the margin the floor is judged by",
        SUBJECT,
        "    return (time.monotonic() - started) / samples",
        "    return 0.0",
    ),
    # --- housekeeping the readings depend on --------------------------------------------------------
    (
        "DROP digest's existence guard, so an absent path reads as an unreadable one",
        SUBJECT,
        "        if not os.path.isfile(path):\n            continue\n",
        "",
    ),
    (
        "DROP this probe's own argument refusal",
        SUBJECT,
        "    cli_guard.refuse_unknown_arguments(",
        "    (lambda *a, **k: None)(",
    ),
    (
        "WIDEN the recognised set to admit anything, so the refusal never fires",
        SUBJECT,
        'recognised=("--self-test", "--probe", "--ref", "--argument", "--timeout"),',
        'recognised=("--self-test", "--probe", "--ref", "--argument", "--timeout", "--nonsense"),',
    ),
    (
        "DROP the bare-invocation refusal, so running the probe with no argument does something",
        SUBJECT,
        '[--self-test | --probe [--ref R]]\\n")\n    raise SystemExit(2)',
        '[--self-test | --probe [--ref R]]\\n")\n    raise SystemExit(0)',
    ),
    (
        "REMOVE the granularity fallback, so one unresolvable value STOPS the whole suite",
        SUBJECT,
        "    return measured or FALLBACK_GRANULARITY_NS",
        "    return measured",
    ),
    (
        "WIDEN the fallback so it overrides a measured granularity too",
        SUBJECT,
        "    return measured or FALLBACK_GRANULARITY_NS",
        "    return FALLBACK_GRANULARITY_NS",
    ),
)


def _run(work):
    run = subprocess.run(
        [sys.executable, os.path.join(work, "tools", SUBJECT), "--self-test"],
        capture_output=True, text=True, cwd=work,
    )
    named = [line[4:].strip() for line in run.stdout.splitlines() if line.startswith("FAIL")]
    finished = any(line.startswith("# ") and "self-test(s)" in line
                   for line in run.stdout.splitlines())
    return run.returncode, named, finished


def main():
    work = tempfile.mkdtemp(prefix="T-321-mutation.")
    try:
        shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
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
            # `T-306`: a CRASH is not a named test.  A mutation that stops the suite exits
            # non-zero exactly as a failing named test does, so the suite's own completion line
            # is what separates them, and an unfinished run is reported as a SURVIVOR.
            if code == 0 or not finished:
                survivors += 1
                # `C-0206`: a harness's row shape is an INTERFACE that
                # `tools/T-295-mutation-input-census.py` parses, and its `survives` shape ends at
                # `no named test failed`.  So the row keeps that ending whatever the reason, and
                # the reason goes on its own line -- a shape the census does not know makes it
                # refuse the whole harness rather than drop the row.
                print("%-8s %-70s %s" % ("SURVIVES", name, "no named test failed"))
                if not finished:
                    print("        NOTE: the suite did not finish; a crash is not a named "
                          "test, so this counts as a survivor (T-306)")
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
