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
# T-327 -- MUTATION COVERAGE FOR `tools/T-327-flatness-resolution.py`.
#
#     tools/T-327-mutation-test.py
#
# `C-0176`'s standard in BOTH directions.  The rows below NARROW a rule back to a predecessor's
# defect -- `C-0221` section 5's own factor of ten, the boolean-on-the-leaf reading, a SEARCH axis
# standing in for a discretisation one, the p90's departure standing in for the nominal's -- and
# WIDEN five others, because a table that only ever narrows becomes a pattern.
#
# `C-0185`/`CH-0237`'s subtracted baseline runs FIRST and its named failures are printed: without
# it a fixture defect reads as `0 survivors` or as every row killed by one and the same error.
# The anchor count is asserted at exactly 1 per row (`C-0185`), and an unfinished suite is a
# SURVIVOR rather than a kill (`T-306`) -- a crash is not a named test.
#
# The row shape is `killed-by`, declared in `tools/P-31-harness-census.py`'s `HARNESSES` table
# (`T-306`: a harness's output is an INTERFACE, declared and never inferred), with the killers on
# their own lines so they are not captured as part of the label.
"""Mutation coverage for tools/T-327-flatness-resolution.py."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SUBJECT = "T-327-flatness-resolution.py"

#: (name, file, old, new).  `old` must occur EXACTLY once in the subject.
MUTATIONS = (
    # --- the identity the whole instrument rests on --------------------------------------------
    (
        "NARROW the identity to a STRICT inequality, so an exceedance of exactly 0.10 reads not flat",
        SUBJECT,
        "                if (exceedance <= TOLERANCE + 1e-12) != value:",
        "                if (exceedance < TOLERANCE) != value:",
    ),
    (
        "INVERT the identity, so a flat verdict is read as an exceedance ABOVE the tolerance",
        SUBJECT,
        "                if (exceedance <= TOLERANCE + 1e-12) != value:",
        "                if (exceedance >= TOLERANCE) != value:",
    ),
    (
        "WIDEN the verdict booleans past the flat-at-p90 family, so beatsUncoupledAtP90 is tested",
        SUBJECT,
        '            if isinstance(value, bool) and key.lower().startswith("flat") '
        'and "p90" in key.lower()}',
        '            if isinstance(value, bool) and "p90" in key.lower()}',
    ),
    # --- the census predicate, which is C-0221 section 5's and must stay verbatim ---------------
    (
        "NARROW the boolean test onto the LEAF's own record key, losing reading 2b entirely",
        SUBJECT,
        "            if not any(isinstance(x, bool) for x in parent.values()):",
        "            if not isinstance(parent.get(leaf), bool):",
    ),
    (
        "WIDEN the census past its own [0.09, 0.11] window",
        SUBJECT,
        "\n            if not 0.09 <= value <= 0.11:",
        "\n            if False:",
    ),
    (
        "WIDEN the census key filter, so every numeric leaf of a verdict record is a reading",
        SUBJECT,
        '\n            if not (leaf.endswith("OverStroke") or "ishing" in leaf):',
        "\n            if False:",
    ),
    (
        "NARROW the census sort away, so the tightest reading is whatever the walk found first",
        SUBJECT,
        "    rows.sort(key=lambda row: (row.relative, row.tag, row.path))",
        "    pass",
    ),
    # --- the resolution ------------------------------------------------------------------------
    (
        "NARROW the interval to ONE tail, so a marginal reading resolves on half the evidence",
        SUBJECT,
        "    inside = lower_tail >= alpha and upper_tail >= alpha",
        "    inside = lower_tail >= alpha",
    ),
    (
        "WIDEN alpha to the whole complement, so the interval is one-sided at twice the level",
        SUBJECT,
        "    alpha = (1.0 - confidence) / 2.0\n    lower_tail = binom_cdf(x, n, p0)",
        "    alpha = 1.0 - confidence\n    lower_tail = binom_cdf(x, n, p0)",
    ),
    (
        "INVERT the determinacy, so a reading indistinguishable from the tolerance reads DETERMINED",
        SUBJECT,
        'return "UNDETERMINED" if inside else "DETERMINED"',
        'return "DETERMINED" if inside else "UNDETERMINED"',
    ),
    (
        "NARROW the one-sided p to the lower tail whatever the verdict claims",
        SUBJECT,
        "    return binom_cdf(x, n, p0) if flat else 1.0 - binom_cdf(x - 1, n, p0)",
        "    return binom_cdf(x, n, p0)",
    ),
    # --- the realisation count is BACKED OUT, never assumed -------------------------------------
    (
        "WIDEN the realisation count to a DEFAULT where the record states none",
        SUBJECT,
        "    if not isinstance(standard_error, (int, float)) or standard_error <= 0.0:\n"
        "        return None",
        "    if not isinstance(standard_error, (int, float)) or standard_error <= 0.0:\n"
        "        return 4000",
    ),
    # --- which axes may enter a resolution ------------------------------------------------------
    (
        "WIDEN a resolution to accept a SEARCH axis, which is the search's variance",
        SUBJECT,
        '    return kind == "DISCRETISATION"',
        '    return kind in ("DISCRETISATION", "SEARCH", "PARAMETER", "PENALTY")',
    ),
    (
        "NARROW the axis rules so an unrecognised axis is GUESSED as a discretisation one",
        SUBJECT,
        '    return "UNCLASSIFIED"',
        '    return "DISCRETISATION"',
    ),
    (
        "NARROW the SEARCH rules away, so a training-realisation axis reads as a discretisation one",
        SUBJECT,
        '    ("SEARCH", ("realisation", "descent", "sweeps", "screening")),',
        '    ("SEARCH", ()),',
    ),
    # --- population B, where the transfer this whole task is about would come back ---------------
    (
        "NARROW population B's axis filter away, so the p90's departure stands in for the nominal's",
        SUBJECT,
        '        if "nominal" not in quantity and "no defect" not in quantity:\n            continue',
        "        if False:\n            continue",
    ),
    (
        "WIDEN population B past the discretisation filter, so a SEARCH axis supplies a resolution",
        SUBJECT,
        '        if not enters_a_resolution(axis_kind(record.get("axis") or "")):\n            continue',
        "        if False:\n            continue",
    ),
    (
        "WIDEN a missing nominal axis into a verdict, so an untestable reading reads DETERMINED",
        SUBJECT,
        '                    "determinacy": ("NO-AXIS" if worst is None else',
        '                    "determinacy": ("DETERMINED" if worst is None else',
    ),
    # --- the paired ordering ---------------------------------------------------------------------
    (
        "INVERT the paired win count, so the subject's LOSSES are counted as wins",
        SUBJECT,
        "    return int(round((1.0 - fraction_worse) * realisations))",
        "    return int(round(fraction_worse * realisations))",
    ),
    # --- the binomial arithmetic --------------------------------------------------------------------
    (
        "NARROW the binomial cdf's lower guard, so a negative count reads as certainty",
        SUBJECT,
        "    if k < 0:\n        return 0.0",
        "    if k < 0:\n        return 1.0",
    ),
    (
        "WIDEN the Clopper-Pearson lower limit off the boundary of the parameter space",
        SUBJECT,
        "    low = 0.0 if x == 0 else _bisect",
        "    low = 1e-9 if x == 0 else _bisect",
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
    work = tempfile.mkdtemp(prefix="T-327-mutation.")
    try:
        shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
        # The live-corpus arms are part of the subject's contract, so the fixture carries the
        # committed `gpd/results/` rather than letting them skip -- `C-0195`: a fixture layout is
        # a dependency declaration, and a silently skipped arm lands in the baseline unnoticed.
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
