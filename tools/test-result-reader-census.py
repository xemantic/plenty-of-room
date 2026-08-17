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
# Self-test for tools/result-reader-census.py (task P-22).
#
#     tools/test-result-reader-census.py
#
# The census decides whether a re-emission of a result file can move anything else in the
# repository.  `C-0073` answered that question with a grep and got it wrong (`CH-0092`), so the
# replacement is only worth having if its own failure modes are pinned: a missed edge lets a
# stale file stand for an iteration, and a spurious edge (a KDoc mention counted as a read)
# makes the census useless for certifying that a propagation closes.  Both are silent.
#
# Fixtures are in-memory Kotlin source strings; nothing here reads the checkout except the
# last group, which asserts the three counts `CH-0092` published against the real tree.
import importlib.util
import os
import sys

_here = os.path.dirname(os.path.abspath(__file__))
_spec = importlib.util.spec_from_file_location(
    "result_reader_census", os.path.join(_here, "result-reader-census.py")
)
census = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(census)

failures = 0
checks = 0


def check(label, condition, detail=""):
    global failures, checks
    checks += 1
    if condition:
        print("ok   %s" % label)
    else:
        failures += 1
        print("FAIL %s%s" % (label, ("  --  " + detail) if detail else ""))


def eq(label, actual, expected):
    check(label, actual == expected, "expected %r, got %r" % (expected, actual))


# --- comment stripping ---------------------------------------------------------------------
# A KDoc block is where every study announces the file it emits, so if comments survive the
# census counts every study as a reader of its own output and of everything it discusses.

eq(
    "a line comment is removed",
    census.strip_comments('val a = 1 // File("gpd/results/T-1-x.json")\nval b = 2').strip(),
    "val a = 1\nval b = 2",
)
eq(
    "a block comment is removed",
    census.strip_comments('/** Emits `gpd/results/T-1-x.json`. */\nval a = 1').strip(),
    "val a = 1",
)
check(
    "a // inside a string literal is not a comment",
    'https://x' in census.strip_comments('val a = "https://x"'),
)
check(
    "a nested block comment is removed whole",
    census.strip_comments('/* a /* b */ c */ val d = 1').strip() == "val d = 1",
)
check(
    "a raw string survives stripping",
    'a // b' in census.strip_comments('val a = """a // b"""'),
)

# --- what counts as a read -----------------------------------------------------------------
# P2: a result file named in prose is not opened.  Twenty-odd sources carry exactly this.

eq(
    "a literal File path is a reference",
    census.file_literals('val f = File("gpd/results/T-1d-scf-density-profile.json")'),
    ["T-1d-scf-density-profile.json"],
)
eq(
    "CH-0092's shape -- File(directory, name) -- is a reference",
    census.file_literals('val f = File(directory, "T-1d-scf-density-profile.json")'),
    ["T-1d-scf-density-profile.json"],
)
eq(
    "the same shape broken across lines is a reference",
    census.file_literals(
        'val f = File(\n    directory, "T-1f-mean-field-fluctuation-corrections.json"\n)'
    ),
    ["T-1f-mean-field-fluctuation-corrections.json"],
)
eq(
    "a nested call in the argument list does not confuse the scan",
    census.file_literals('readScf(File(dir(root), "T-1d-scf-density-profile.json"))'),
    ["T-1d-scf-density-profile.json"],
)
eq(
    "a result file named in a prose string is NOT a reference",
    census.file_literals(
        'val s = "read from gpd/results/T-130-crossbar-array-placement.json, keyed on"'
    ),
    [],
)
eq(
    "a result file named in a KDoc comment is NOT a reference",
    census.file_literals(
        '/** Emits `gpd/results/T-1f-mean-field-fluctuation-corrections.json`. */\nval a = 1'
    ),
    [],
)
eq(
    "a wildcard mention is not a file",
    census.file_literals('val s = File("gpd/results/T-130-*.json")'),
    [],
)
eq(
    "a non-result File is ignored",
    census.file_literals('val f = File("build/tmp/scratch.json")'),
    [],
)

# --- read against write --------------------------------------------------------------------

eq(
    "a File written through a val is a write, not a read",
    census.written_literals(
        'val output = File("gpd/results/T-1-layer-stiffness.json")\noutput.writeText(s)'
    ),
    {"T-1-layer-stiffness.json"},
)
eq(
    "a File written directly is a write",
    census.written_literals('File("gpd/results/T-1e-first-moment-convention.json").writeText(s)'),
    {"T-1e-first-moment-convention.json"},
)
eq(
    "a File bound to a val that is never written is a read",
    census.written_literals(
        'private val UPSTREAM = File("gpd/results/T-4-maximum-usable-bias.json")\nval x = UPSTREAM'
    ),
    set(),
)
eq(
    "the study's own output is not counted among its reads",
    census.read_literals(
        'val up = File("gpd/results/T-4-maximum-usable-bias.json")\n'
        'val output = File("gpd/results/T-21-concentrated-crossover.json")\n'
        "output.writeText(s)"
    ),
    {"T-4-maximum-usable-bias.json"},
)

# --- task identity -------------------------------------------------------------------------

eq("a task id is the first two dash components", census.task_id("T-1d-scf-density-profile.json"), "T-1d")
eq("a three-digit task id", census.task_id("T-125-upward-root-placement.json"), "T-125")
eq("a process task id", census.task_id("P-18-determined-precision.json"), "P-18")
eq("a suffixed task id", census.task_id("T-5b-tile-flatness.json"), "T-5b")

# --- declared sources ----------------------------------------------------------------------

eq(
    "an abbreviated sources declaration parses to task ids",
    census.declared_sources('"sources" to "gpd/results/T-1d, T-14, T-1f, T-3b"'),
    {"T-1d", "T-14", "T-1f", "T-3b"},
)
eq(
    "a concatenated multi-line sources declaration parses",
    census.declared_sources(
        '"sources" to "gpd/results/T-1d-scf-density-profile.json, " +\n'
        '        "gpd/results/T-3-stroke-and-blocking-force.json, " +\n'
        '        "gpd/results/T-14-crossover-phase-and-registration.json"'
    ),
    {"T-1d", "T-3", "T-14"},
)
eq(
    "a study with no sources parameter declares nothing",
    census.declared_sources('val output = File("gpd/results/T-1-layer-stiffness.json")'),
    None,
)

# --- the reachability closure ----------------------------------------------------------------
# This is the whole of CH-0092: the study names no file and the helper names no directory.

STUDY = """
package com.xemantic.nano.plentyofroom.window
import java.io.File
fun main() {
    val inputs = ResynthesisInputs.read(File("gpd/results"))
    val output = File("gpd/results/T-25-window-resynthesis.json")
    output.writeText(render(inputs))
}
"""
HELPER = """
package com.xemantic.nano.plentyofroom.window
import java.io.File
class ResynthesisInputs {
    companion object {
        fun read(directory: File) = File(directory, "T-1d-scf-density-profile.json")
    }
}
"""
UNRELATED = """
package com.xemantic.nano.plentyofroom.brush
import java.io.File
fun somethingElse() = File("gpd/results/T-1f-mean-field-fluctuation-corrections.json")
"""

graph = census.build_census(
    {
        "src/main/kotlin/window/WindowResynthesisStudy.kt": STUDY,
        "src/main/kotlin/window/ResynthesisInputs.kt": HELPER,
        "src/main/kotlin/brush/Other.kt": UNRELATED,
    }
)
study = graph["studies"]["window/WindowResynthesisStudy.kt"]
eq("the transitive read is found", set(study["reads"]), {"T-1d-scf-density-profile.json"})
eq("the write is found", study["writes"], "T-25-window-resynthesis.json")
eq(
    "the transitive edge is recorded as transitive, not direct",
    (study["directReads"], study["transitiveReads"]),
    ([], ["T-1d-scf-density-profile.json"]),
)
check(
    "a file in another package that is not referenced is NOT read",
    "T-1f-mean-field-fluctuation-corrections.json" not in study["reads"],
)
eq(
    "the readers of a file are the inverse of the reads",
    graph["readersOf"]["T-1d-scf-density-profile.json"],
    ["window/WindowResynthesisStudy.kt"],
)
check(
    "the study itself never names the file it reads -- CH-0092's whole point",
    "T-1d-scf-density-profile.json" not in census.file_literals(STUDY),
)

# --- the two regressions P4 demands ------------------------------------------------------------

MISDECLARED = STUDY.replace(
    "fun main() {", 'val p = mapOf("sources" to "gpd/results/T-1d, T-3b")\nfun main() {'
)
problems = census.check_declarations(
    census.build_census(
        {
            "src/main/kotlin/window/WindowResynthesisStudy.kt": MISDECLARED,
            "src/main/kotlin/window/ResynthesisInputs.kt": HELPER,
        }
    )
)
check(
    "a study that DECLARES a file it does not read fails the check",
    any("T-3b" in p for p in problems),
    repr(problems),
)

UNDECLARED = STUDY.replace(
    "fun main() {", 'val p = mapOf("sources" to "gpd/results/T-14")\nfun main() {'
)
problems = census.check_declarations(
    census.build_census(
        {
            "src/main/kotlin/window/WindowResynthesisStudy.kt": UNDECLARED,
            "src/main/kotlin/window/ResynthesisInputs.kt": HELPER,
        }
    )
)
check(
    "a study that READS a file it does not declare fails the check",
    any("T-1d" in p for p in problems),
    repr(problems),
)

HONEST = STUDY.replace(
    "fun main() {", 'val p = mapOf("sources" to "gpd/results/T-1d")\nfun main() {'
)
eq(
    "an honest declaration passes",
    census.check_declarations(
        census.build_census(
            {
                "src/main/kotlin/window/WindowResynthesisStudy.kt": HONEST,
                "src/main/kotlin/window/ResynthesisInputs.kt": HELPER,
            }
        )
    ),
    [],
)

# --- drift against a baseline ------------------------------------------------------------------

baseline = census.build_census(
    {
        "src/main/kotlin/window/WindowResynthesisStudy.kt": STUDY,
        "src/main/kotlin/window/ResynthesisInputs.kt": HELPER,
    }
)
moved = census.build_census(
    {
        "src/main/kotlin/window/WindowResynthesisStudy.kt": STUDY,
        "src/main/kotlin/window/ResynthesisInputs.kt": HELPER.replace("T-1d-scf-density-profile", "T-1f-mean-field-fluctuation-corrections"),
    }
)
check(
    "a changed read set of a KNOWN study is drift and fails",
    census.check_drift(moved, baseline) != [],
)
eq("an unchanged census does not drift", census.check_drift(baseline, baseline), [])
added = dict(baseline["studies"])
new = census.build_census(
    {
        "src/main/kotlin/window/WindowResynthesisStudy.kt": STUDY,
        "src/main/kotlin/window/ResynthesisInputs.kt": HELPER,
        "src/main/kotlin/brush/NewStudy.kt": (
            "package com.xemantic.nano.plentyofroom.brush\nimport java.io.File\n"
            'fun main() { val o = File("gpd/results/T-99-x.json"); o.writeText("") }\n'
        ),
    }
)
eq(
    "a NEW study is reported but is not drift -- a sibling adding work must not fail verify.sh",
    census.check_drift(new, baseline),
    [],
)
check(
    "and the new study is reported",
    census.new_studies(new, baseline) == ["brush/NewStudy.kt"],
    repr(census.new_studies(new, baseline)),
)

# --- write bookkeeping ---------------------------------------------------------------------

two_writers = census.build_census(
    {
        "src/main/kotlin/a/OneStudy.kt": (
            "package com.xemantic.nano.plentyofroom.a\nimport java.io.File\n"
            'fun main() { val o = File("gpd/results/T-9-x.json"); o.writeText("") }\n'
        ),
        "src/main/kotlin/b/TwoStudy.kt": (
            "package com.xemantic.nano.plentyofroom.b\nimport java.io.File\n"
            'fun main() { val o = File("gpd/results/T-9-x.json"); o.writeText("") }\n'
        ),
    }
)
check(
    "two studies writing one result file is a defect",
    census.check_writes(two_writers) != [],
)

# --- against the real tree: CH-0092's published counts ---------------------------------------
# P1.  If the derivation reproduces C-0073's 1 and 0 it is a grep in different clothing.

root = os.path.dirname(_here)
_window = os.path.join(root, "src", "main", "kotlin", "window")
if not all(
    os.path.exists(os.path.join(_window, name))
    for name in (
        "ResynthesisInputs.kt",
        "WindowResynthesisStudy.kt",
        "SecondResynthesisStudy.kt",
        "DesignWindowStudy.kt",
    )
):
    print()
    print("SKIPPED the real-tree group: window/ is incomplete (a --drop-file, presumably)")
    print("%d checks passed" % checks)
    sys.exit(1 if failures else 0)

real = census.census_of_tree(root)
readers = real["readersOf"]
eq(
    "CH-0092: T-1d has three readers",
    sorted(os.path.basename(p) for p in readers.get("T-1d-scf-density-profile.json", [])),
    ["DesignWindowStudy.kt", "SecondResynthesisStudy.kt", "WindowResynthesisStudy.kt"],
)
eq(
    "CH-0092: T-1f has two readers",
    sorted(
        os.path.basename(p)
        for p in readers.get("T-1f-mean-field-fluctuation-corrections.json", [])
    ),
    ["SecondResynthesisStudy.kt", "WindowResynthesisStudy.kt"],
)
# `CH-0092` recorded T-2 as having no readers, and that stopped being true in iteration 18:
# `T-156`'s route census reads `T-2`'s bias clauses to prove they are `T-3`'s own numbers
# (`C-0091`). The assertion is a fact about the tree, not a rule, so it is updated rather than
# relaxed — and the census is exactly the mechanism that is supposed to notice.
eq(
    "CH-0092: T-2's only reader is T-156's route census",
    sorted(os.path.basename(p) for p in readers.get("T-2-design-window.json", [])),
    ["BufferRouteCensusStudy.kt"],
)
check(
    "the real tree passes its own declaration check",
    census.check_declarations(real) == [],
    repr(census.check_declarations(real)),
)
check(
    "the real tree passes its own write check",
    census.check_writes(real) == [],
    repr(census.check_writes(real)),
)

# The grep IS a lower bound -- it finds every edge whose path is a single literal, and misses
# only the assembled ones.  So the derived census must be a strict superset of it, and any
# literal a grep finds in a study that the census does not attribute to that study is a
# derivation bug rather than a discovery.
missed = []
for path, record in real["studies"].items():
    with open(os.path.join(root, "src", "main", "kotlin", path), encoding="utf-8") as handle:
        text = handle.read()
    for name in census.file_literals(text):
        if name not in record["reads"] and name != record["writes"]:
            missed.append("%s: %s" % (path, name))
eq("the derived census is a superset of what a naive grep finds", missed, [])
check(
    "and it finds strictly more -- the transitive edges a grep cannot see",
    sum(len(r["transitiveReads"]) for r in real["studies"].values()) > 0,
)

print()
if failures:
    print("%d of %d checks FAILED" % (failures, checks))
    sys.exit(1)
print("%d checks passed" % checks)
