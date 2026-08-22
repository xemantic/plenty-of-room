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
# T-272 `P2` -- generate `structure/ResultInputs.kt`, one typed handle per committed result file.
#
#     tools/T-272-emit-result-inputs.py            write the Kotlin
#     tools/T-272-emit-result-inputs.py --check    exit 1 if the committed Kotlin is stale
#     tools/T-272-emit-result-inputs.py --selftest
#
# WHY IT IS GENERATED. The registry IS a census of `gpd/results/`, and `CLAUDE.md` records what
# happens to a census maintained by hand: it stops. Generating it makes "a new result file has no
# handle" a `--check` failure rather than a thing somebody notices.
#
# THE ONE IRREGULARITY IS A FINDING. A handle is named for its task id, which is unique over the
# corpus with exactly one exception: `T-119` writes BOTH `T-119-literature-queries.json` (a
# EuropePMC survey, in `tools/`) and `T-119-unused-junction-site.json` (a Kotlin study). Those two
# get their slug appended. The collision is not cosmetic -- `tools/reemission-order.py` keys its
# whole topological sort on `tag_of`, which maps both files to `T-119`, so the sorter cannot tell
# a read of one from a read of the other.
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULTS = os.path.join(ROOT, "gpd", "results")
TARGET = os.path.join(ROOT, "src", "main", "kotlin", "structure", "ResultInputs.kt")

LICENCE = """/*
 * Copyright 2026 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
"""


def task_id(name):
    """`T-1d-scf-density-profile.json` -> `T-1d`, the stable handle a task is known by."""
    parts = name.split("-")
    return "%s-%s" % (parts[0], parts[1])


def property_names(files):
    """{file name: Kotlin property}, disambiguating a task id that names two files."""
    counts = {}
    for name in files:
        counts[task_id(name)] = counts.get(task_id(name), 0) + 1
    names = {}
    for name in files:
        tag = task_id(name)
        base = tag.replace("-", "_").upper()
        if counts[tag] > 1:
            slug = name[: -len(".json")][len(tag) + 1 :]
            base = base + "_" + re.sub(r"[^A-Za-z0-9]+", "_", slug).upper()
        names[name] = base
    return names


def render(files):
    names = property_names(files)
    lines = [LICENCE, "", "package com.xemantic.nano.plentyofroom.structure", "",
             "import java.io.File", "",
             "/** The directory every committed result file of this repository lives in. */",
             'const val RESULT_DIRECTORY: String = "gpd/results"', "",
             "/**",
             " * A committed result file, as a handle rather than as a path.",
             " *",
             " * `T-272`'s `P2`, and step 6 of [ARCHITECTURE.md](../../../../../../../ARCHITECTURE.md):",
             " * *\"studies should declare typed input handles; then the census,"
             " `tools/reemission-order.py`'s*",
             " * *topological sort and staleness detection are free.\"*",
             " *",
             " * Before this, 74 study sources built their inputs as `File(\"gpd/results/…json\")`, so"
             " the",
             " * dependency graph had to be **derived** by a static analysis of the Kotlin"
             " (`P-22`/`C-0082`)",
             " * -- and `C-0073` audited that graph with a `grep` and reported **one** reader of `T-1d`"
             " where",
             " * there are three, because a path assembled from a directory in the caller and a name"
             " in a",
             " * helper is invisible to a search for either half (`CH-0092`). The derivation stays,"
             " and is",
             " * still the authority; what changes is that a handle is a **declaration** the"
             " derivation can",
             " * read directly, so the two can be asserted equal.",
             " *",
             " * @param tag the task id that owns the file -- the handle `tools/reemission-order.py`"
             " sorts on.",
             " * @param fileName the committed file's own name, without the directory.",
             " */",
             "data class ResultInput(val tag: String, val fileName: String) {", "",
             "    /** The repository-relative path, which is the only place the directory is spelled. */",
             "    val path: String get() = \"$RESULT_DIRECTORY/$fileName\"", "",
             "    /** The file, relative to the working directory a study is run from. */",
             "    fun file(): File = File(path)", "",
             "    /**",
             "     * The file inside an explicitly given directory.",
             "     *",
             "     * This is the shape `window/ResynthesisInputs.kt` uses and the shape `CH-0092`'s"
             " missing",
             "     * edges were assembled in: a directory in the caller, a name in a helper. With a"
             " handle",
             "     * the name is no longer in the helper.",
             "     */",
             "    fun file(directory: File): File = File(directory, fileName)", "",
             "    /** The file's text. */",
             "    fun readText(): String = file().readText()",
             "}", "",
             "/**",
             " * Every committed result file of this repository, one handle each.",
             " *",
             " * **Generated** by `tools/T-272-emit-result-inputs.py`, which is also a `--check`:"
             " a result",
             " * file with no handle fails it. A registry of this shape maintained by hand is a"
             " census that",
             " * stops, which is the failure `CLAUDE.md` records for every named set in this tree.",
             " */",
             "object ResultInputs {", ""]
    for name in files:
        lines.append('    val %s: ResultInput = ResultInput("%s", "%s")'
                     % (names[name], task_id(name), name))
    lines += ["", "    /** Every handle, in the order the files sort. */",
              "    val all: List<ResultInput> = listOf(",
              "        " + ", ".join(names[name] for name in files) + "",
              "    )", "",
              "    /** The handle a task id names, or `null` where a task id names two files. */",
              "    fun ofTag(tag: String): ResultInput? =",
              "        all.singleOrNull { it.tag == tag }", "}", ""]
    text = "\n".join(lines)
    return _wrap(text)


def _wrap(text):
    """Break the `all` list so no generated line exceeds 100 characters."""
    out = []
    for line in text.split("\n"):
        if len(line) <= 100 or not line.startswith("        "):
            out.append(line)
            continue
        current = "       "
        for token in line.strip().split(" "):
            if len(current) + 1 + len(token) > 100:
                out.append(current)
                current = "       "
            current += " " + token
        out.append(current)
    return "\n".join(out)


def committed_files(directory=RESULTS, root=ROOT):
    """The result files git TRACKS, falling back to the directory listing outside a checkout.

    `git ls-files` and not `os.listdir`: a concurrent agent's in-flight, untracked result file is
    not a committed one, and generating a handle for it would put another agent's work in this
    file's `--check`. The fallback exists because `tools/verify.sh` runs its gates inside a
    snapshot with no `.git` at all, which is the blind spot `C-0083` measured in the link checker.
    """
    import subprocess

    try:
        listed = subprocess.run(
            ["git", "-C", root, "ls-files", "gpd/results"],
            capture_output=True, text=True, check=True
        ).stdout.split()
        names = sorted(os.path.basename(p) for p in listed if p.endswith(".json"))
        if names:
            return names
    except (OSError, subprocess.CalledProcessError):
        pass
    return sorted(name for name in os.listdir(directory) if name.endswith(".json"))


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    check("task id", task_id("T-1d-scf-density-profile.json"), "T-1d")
    check("task id of a P task", task_id("P-22-result-reader-census.json"), "P-22")
    names = property_names(["T-1d-scf-density-profile.json", "T-9b-crossover-vertical-compliance.json"])
    check("a unique id becomes its own property", names["T-1d-scf-density-profile.json"], "T_1D")
    check("a lettered id keeps its letter",
          names["T-9b-crossover-vertical-compliance.json"], "T_9B")
    collided = property_names(
        ["T-119-literature-queries.json", "T-119-unused-junction-site.json", "T-1-x.json"]
    )
    check("a collided id takes its slug",
          collided["T-119-literature-queries.json"], "T_119_LITERATURE_QUERIES")
    check("and so does the other one",
          collided["T-119-unused-junction-site.json"], "T_119_UNUSED_JUNCTION_SITE")
    check("an uncollided neighbour is unaffected", collided["T-1-x.json"], "T_1")
    rendered = render(["T-1-layer-stiffness.json"])
    check("the handle is rendered",
          'val T_1: ResultInput = ResultInput("T-1", "T-1-layer-stiffness.json")' in rendered, True)
    check("the directory is spelled once", rendered.count('"gpd/results"'), 1)
    check("no line exceeds a hundred characters",
          max(len(line) for line in rendered.split("\n")) <= 100, True)
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    text = render(committed_files())
    if "--check" in argv:
        current = open(TARGET, encoding="utf-8").read() if os.path.exists(TARGET) else ""
        if current != text:
            print("structure/ResultInputs.kt is stale; run tools/T-272-emit-result-inputs.py")
            return 1
        print("structure/ResultInputs.kt is current (%d handles)" % len(committed_files()))
        return 0
    open(TARGET, "w", encoding="utf-8").write(text)
    print("wrote %s (%d handles)" % (TARGET, len(committed_files())))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
