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
# T-337 -- MUTATION COVERAGE FOR `tools/T-337-verdict-exceedance-census.py`.
#
#     tools/T-337-mutation-test.py
#
# `C-0176`'s standard in BOTH directions: the rows below NARROW rules back to the defect they
# exist to prevent -- a single p90 key, a population C keyed on falsiness rather than on
# absence, a residue that swallows a gated file's defect -- and WIDEN others, because a table
# that only ever narrows becomes a pattern.
#
# `C-0185`/`CH-0237`'s subtracted baseline runs FIRST and prints its named failures: without it
# a fixture defect reads as `0 survivors`, or as every row killed by one and the same error.
# Each anchor is asserted at exactly 1 occurrence (`C-0185`), and an unfinished suite is a
# SURVIVOR rather than a kill (`T-306`) -- a crash is not a named test.
#
# The row shape is `killed-by`, declared in `tools/P-31-harness-census.py`'s `HARNESSES` table
# (`T-306`: a harness's output is an INTERFACE, declared and never inferred), with the killers
# printed on their own lines so they are not captured as part of the label.
"""Mutation coverage for tools/T-337-verdict-exceedance-census.py."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SUBJECT = "T-337-verdict-exceedance-census.py"

MUTATIONS = (
    # --- the p90 the verdict is READ ON ------------------------------------------------------
    (
        "NARROW the p90 candidates to p90OverStroke, the key the searched studies do NOT use",
        SUBJECT,
        'P90_KEYS = ("p90OverStroke", "searchedP90", "p90")',
        'P90_KEYS = ("p90OverStroke",)',
    ),
    (
        "WIDEN p90_field to take the FIRST candidate present, without reconciling the verdict",
        SUBJECT,
        "        if not isinstance(verdict, bool):\n            return (key, value)\n"
        "        if (value < TOLERANCE) == verdict:\n            return (key, value)",
        "        return (key, value)",
    ),
    (
        "INVERT the reconciliation, so a field on the WRONG side of the tolerance is chosen",
        SUBJECT,
        "        if (value < TOLERANCE) == verdict:",
        "        if (value < TOLERANCE) != verdict:",
    ),
    # --- population C, which is an ABSENCE and not a falsiness -------------------------------
    (
        "NARROW population C to a FALSY exceedance, so an exceedance of exactly zero is an absence",
        SUBJECT,
        '    return [row for row in rows if row["exceedance"] is None]',
        '    return [row for row in rows if not row["exceedance"]]',
    ),
    (
        "WIDEN the exceedance reader to admit a bool, which JSON `true` would satisfy",
        SUBJECT,
        "            if isinstance(exceedance, bool) or not isinstance(exceedance, (int, float)):\n"
        "                exceedance = None",
        "            if not isinstance(exceedance, (int, float)):\n"
        "                exceedance = None",
    ),
    # --- the determinacy rule ------------------------------------------------------------------
    (
        "NARROW the determinacy to a FIXED 4 000, so a record graded at another n is misread",
        SUBJECT,
        "        count = backed if backed else realisations",
        "        count = realisations",
    ),
    (
        "INVERT the determinacy verdict, so a reading inside its own interval reads DETERMINED",
        SUBJECT,
        "            determinacy=resolution.determinacy(x, count, confidence, TOLERANCE),",
        "            determinacy=(\"DETERMINED\"\n"
        "                         if resolution.determinacy(x, count, confidence, TOLERANCE)\n"
        "                         == \"UNDETERMINED\" else \"UNDETERMINED\"),",
    ),
    # --- the join, which must be an EQUALITY and must refuse an ambiguous donor set ------------
    (
        "WIDEN the join to resolve an AMBIGUOUS donor set to its first value",
        SUBJECT,
        "                        joinedExceedance=values[0] if len(values) == 1 else None,",
        "                        joinedExceedance=values[0],",
    ),
    (
        "WIDEN the join to rows that already carry an exceedance, so a donor overrides a record",
        SUBJECT,
        '        if row["exceedance"] is not None or row["p90"] is None:',
        '        if row["p90"] is None:',
    ),
    # --- the gate's SCOPE and its residue, which are complementary by construction -------------
    (
        "WIDEN the residue to every withheld verdict, so a GATED file's defect reads as residue",
        SUBJECT,
        '    return [row for row in population_c(rows) if row["tag"] not in set(gated)]',
        "    return list(population_c(rows))",
    ),
    (
        "NARROW the gate's defect count to the withheld verdicts alone, dropping two kinds",
        SUBJECT,
        '    return (len(defects["naked"]) + len(defects["unidentified"])\n'
        '            + len(defects["disagreeing"]))',
        '    return len(defects["naked"])',
    ),
    (
        "WIDEN the gate's scope to every reported file, which it cannot make clean",
        SUBJECT,
        '    inside = [row for row in rows if row["tag"] in set(gated)]',
        "    inside = list(rows)",
    ),
    # --- the corpus the census reads ------------------------------------------------------------
    (
        "NARROW `documents` to skip a missing file instead of refusing, so a census can shrink",
        SUBJECT,
        '        if not matches:\n            raise SystemExit('
        '"no committed result file for %s under %s" % (tag, root))',
        "        if not matches:\n            continue",
    ),
    (
        "NARROW the donor index to the reported files, understating what is recoverable",
        SUBJECT,
        '    for path in sorted(glob.glob(os.path.join(root, "gpd/results/*.json"))):',
        '    for path in sorted(glob.glob(os.path.join(root, "gpd/results/T-3*.json"))):',
    ),
)


def _run(work):
    run = subprocess.run(
        [sys.executable, os.path.join(work, "tools", SUBJECT), "--self-test",
         "--root", os.path.join(work, "corpus")],
        capture_output=True, text=True, cwd=work,
    )
    named = [line[5:].strip() for line in run.stdout.splitlines() if line.startswith("FAIL ")]
    finished = any(line.startswith("# ") and "self-test(s)" in line
                   for line in run.stdout.splitlines())
    return run.returncode, named, finished


def main():
    work = tempfile.mkdtemp(prefix="T-337-mutation.")
    try:
        shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
        # The subject's live-corpus arms are part of its contract, so the fixture carries the
        # committed `gpd/results/` -- `C-0195`: a fixture layout is a dependency declaration,
        # and an arm that silently skips lands in the baseline unnoticed.
        os.makedirs(os.path.join(work, "corpus", "gpd"))
        shutil.copytree(os.path.join(ROOT, "gpd", "results"),
                        os.path.join(work, "corpus", "gpd", "results"))
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
                print("ANCHOR  %-78s occurs %d times, expected 1" % (name, count))
                survivors += 1
        target = os.path.join(work, "tools", SUBJECT)
        for name, _path, old, new in MUTATIONS:
            if source.count(old) != 1:
                continue
            open(target, "w", encoding="utf-8").write(source.replace(old, new, 1))
            code, named, finished = _run(work)
            open(target, "w", encoding="utf-8").write(source)
            if code == 0 or not finished:
                survivors += 1
                print("%-8s %-78s %s" % (
                    "SURVIVES", name,
                    "no named test failed" if finished else "the suite did not finish"))
            else:
                print("killed by %d named test(s)  %s" % (len(named), name))
                for failing in named[:2]:
                    print("        FAIL: %s" % failing)
        print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), survivors))
        return 1 if survivors else 0
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
