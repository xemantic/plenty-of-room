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
# T-334 -- HOW MANY DISTINCT TOOLS CAN FAIL A `tools/verify.sh` RUN, DERIVED RATHER THAN TYPED.
#
#     tools/T-334-gate-census.py                  # the census over the working tree
#     tools/T-334-gate-census.py --ref 71d126e    # ... at a named git ref
#     tools/T-334-gate-census.py --check          # the three gated invariants
#     tools/T-334-gate-census.py --self-test      # named self-tests, no repository read
#
# WHY THIS EXISTS.  The number has been answered four times by four predicates and three of the
# answers were wrong:
#
#   `CH-0222`  the number was right and the predicate was wrong
#   `CH-0243`  the predicate was a FILENAME PREFIX (`ls tools/check-*.py`)
#   `C-0210`   replaced it with INVOCATIONS IN ONE FILE, and named that a predicate about a FILE
#              where the question is about a RUN -- and published `16 + 1 + 20` = 37
#   `CH-0286`  found that its third derivation is a regular expression over a LITERAL, so the
#              twelve Kotlin-subject harnesses wired `commandLine(mutationSnapshotArguments(...))`
#              are invisible to it, and published `18 + 21 + 12` = 51
#
# `CH-0286` is right about the literal and stops one level short of the question.  An `Exec` task
# that `:test` does not depend on is never executed by `./gradlew test`, so an invocation inside
# one cannot fail a `tools/verify.sh` run at all -- `build.gradle.kts`'s own comment above
# `mutationSnapshotArguments` says *"registered here ... so they are runnable by name and NOT
# reachable from `:test`"*.  So `51` OVERCOUNTS by exactly the twelve, where `37` UNDERCOUNTED.
#
# THE PREDICATE, and it is about a RUN.  A tool `T` under `tools/` can fail a DEFAULT
# `tools/verify.sh` run -- no arguments, under its `set -euo pipefail` -- iff either
#
#   route A  `tools/verify.sh` executes `tools/T` as the COMMAND WORD of a comment-stripped line
#            of its own body, or
#   route B  `tools/verify.sh` runs `./gradlew test` and `tools/T` is named inside the balanced
#            `commandLine(...)` span of an `Exec` task REACHABLE FROM `:test`.
#
# The answer is the union, de-duplicated by basename.  Two properties are deliberate:
#
#   * NO `--self-test` FILTER.  A failing self-test fails the same run.  That filter separates
#     gates over the corpus from self-tests over fixtures, which is a different question and is
#     reported as its own row, never as the answer.
#   * IT READS `dependsOn`, NOT `commandLine`.  Both wiring shapes are seen, because
#     `tools/P-31-harness-census.py`'s span predicate sees both; whether the task RUNS is then a
#     separate fact, read from the one place that decides it.
#
# WHY IT IMPORTS `tools/P-31-harness-census.py` RATHER THAN EXTENDING IT.  `P-31`'s subject is
# mutation HARNESSES, not gates, and widening its `--check` would make it a census of two
# populations; and `P-31-harness-census.py` is itself a declared mutation subject of
# `tools/T-306-mutation-test.py`, so editing it risks orphaning transcribed anchors -- the exact
# failure `P-31` exists to catch.  Importing keeps *use-not-mention* resolution
# (`strip_kotlin_comments`, `command_line_spans`, `shell_command_words`, all `C-0206`'s) as ONE
# implementation, which is the corpus's own idiom (`T-332` imports `T-319` imports `T-276`).
#
# WHAT THE PREDICATE DELIBERATELY EXCLUDES, printed beside every count because `C-0209` requires
# it -- a gate that can come clean must say what it does not reach:
#
#   * `Exec` tasks registered and not reachable from `:test`     -- Gradle does not run them
#   * modules reached only by IMPORT from a tool on either route -- no invocation names them
#   * `tools/snapshot.sh`, which `tools/verify.sh` SOURCES        -- the command word is `source`
#   * everything `./gradlew test` runs that is not a `tools/` script (the Kotlin suite)
#   * a NON-DEFAULT invocation: `--no-checks` and any `--drop` set `checks="no"` and delete route
#     A entirely, and the trailing `"$@"` reaches `./gradlew test`, so `-x <task>` can delete a
#     route-B member
#
# THE GATE (`--check`) IS THE DELIVERABLE, NOT THE NUMBER.  A corrected `44` that has to be
# re-typed next pass has retired nothing.  Arm 1 is what makes the mistake unrepeatable: the set
# of `Exec` tasks unreachable from `:test` must EQUAL the set of harnesses `P-31` declares
# `BY-HAND`, in both directions -- so a helper-wired harness cannot be added and stay green
# without being declared, and once it is declared the census subtracts it by construction.  The
# two sides share nothing: one is a Kotlin `dependsOn` list, the other a hand-written Python
# table cross-checked by `tools/T-295-mutation-input-census.py` against each harness's own printed
# usage line.
#
# It is deliberately NOT a gate on the wording of `DECISIONS-FOR-NDI.md`.  A gate parsing
# *"`18 + 21 + 12` = FIFTY-ONE"* out of prose would be a gate on a NUMERAL, which is the class of
# predicate this tool exists to retire.
"""The census of tools that can fail a tools/verify.sh run, by reachability."""

import argparse
import ast
import importlib.util
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

BUILD = "build.gradle.kts"
VERIFY = "tools/verify.sh"
HARNESS_CENSUS = "tools/P-31-harness-census.py"


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


p31 = _load("t334_p31", "P-31-harness-census.py")


# --- the state a census is read at ---------------------------------------------------------

class Tree(object):
    """One state of the repository -- the working tree, or a resolved git ref.

    `CH-0246`: a result file whose subject is the CORPUS is a function of a mutable object, so the
    state has to be an argument and the RESOLVED sha has to be recordable.  A default of `HEAD`
    that silently re-bases the measurement is the thing that forbids re-running such a file as a
    control.
    """

    def __init__(self, ref=None, root=ROOT):
        self.root = root
        self.ref = None if ref is None else self._resolve(ref)

    def _resolve(self, ref):
        return subprocess.run(
            ["git", "-C", self.root, "rev-parse", ref],
            capture_output=True, text=True, check=True,
        ).stdout.strip()

    @property
    def label(self):
        return "working tree" if self.ref is None else self.ref[:7]

    def read(self, path):
        """The text of `path`, or None where this state does not carry it."""
        if self.ref is None:
            full = os.path.join(self.root, path)
            if not os.path.exists(full):
                return None
            return open(full, encoding="utf-8", errors="replace").read()
        shown = subprocess.run(
            ["git", "-C", self.root, "show", "%s:%s" % (self.ref, path)],
            capture_output=True, text=True,
        )
        return shown.stdout if shown.returncode == 0 else None

    def tools(self):
        """`{basename: executable}` for every file directly under `tools/`."""
        if self.ref is None:
            directory = os.path.join(self.root, "tools")
            return {
                name: os.access(os.path.join(directory, name), os.X_OK)
                for name in os.listdir(directory)
                if os.path.isfile(os.path.join(directory, name))
            }
        listing = subprocess.run(
            ["git", "-C", self.root, "ls-tree", self.ref, "tools/"],
            capture_output=True, text=True, check=True,
        ).stdout
        entries = {}
        for line in listing.splitlines():
            head, _, path = line.partition("\t")
            fields = head.split()
            if len(fields) >= 2 and fields[1] == "blob":
                entries[os.path.basename(path)] = fields[0] == "100755"
        return entries


# --- route B: `build.gradle.kts` -------------------------------------------------------------

#: A tool named inside a `commandLine(...)` span, whatever brought it there.  It matches the
#: literal `commandLine("$projectDir/tools/x.py")` and the helper
#: `commandLine(mutationSnapshotArguments("x.py"))` alike, which is the whole point: the SHAPE of
#: the wiring is not the question, and `CH-0286` is the second census dated by assuming it is.
_TOOL_IN_SPAN = re.compile(r'"[^"]*?([A-Za-z0-9_.\-]+\.(?:py|sh))"')

_EXEC_TASK = re.compile(r'tasks\.register<Exec>\("([^"]+)"\)')
_TEST_DEPENDS_ON = re.compile(
    r'tasks\.named\("test"\)\s*\{\s*dependsOn\((.*?)\)\s*\}', re.S
)
_DESCRIPTION = re.compile(r'description\s*=\s*((?:"(?:[^"\\]|\\.)*"\s*\+?\s*)+)')
_ANY_TOOL = re.compile(r'([A-Za-z0-9_.\-]+\.(?:py|sh))')


def _spans_with_offsets(build_text):
    """`(offset, span)` for every balanced `commandLine(...)`, over the comment-blanked text.

    `p31.command_line_spans` returns the spans and not where they are, and this census has to
    attribute each one to the `Exec` task that owns it.  The scan is `p31`'s, character for
    character; the only addition is the offset.
    """
    text = p31.strip_kotlin_comments(build_text)
    out, start = [], text.find("commandLine(")
    while start >= 0:
        i, depth, quoted, n = start + len("commandLine("), 1, False, len(text)
        while i < n and depth:
            character = text[i]
            if quoted:
                if character == "\\":
                    i += 1
                elif character == '"':
                    quoted = False
            elif character == '"':
                quoted = True
            elif character == "(":
                depth += 1
            elif character == ")":
                depth -= 1
            i += 1
        out.append((start, text[start:i]))
        start = text.find("commandLine(", i)
    return out


def test_dependencies(build_text):
    """Every task name `:test` depends on, comments blanked."""
    text = p31.strip_kotlin_comments(build_text)
    match = _TEST_DEPENDS_ON.search(text)
    if not match:
        return set()
    return set(re.findall(r'"([^"]+)"', match.group(1)))


def exec_tasks(build_text):
    """`[{task, tools, reachable, description}]`, one per `tasks.register<Exec>` block."""
    text = p31.strip_kotlin_comments(build_text)
    registered = [(m.start(), m.group(1)) for m in _EXEC_TASK.finditer(text)]
    reachable = test_dependencies(build_text)
    bounds = [
        (start, registered[index + 1][0] if index + 1 < len(registered) else len(text), name)
        for index, (start, name) in enumerate(registered)
    ]

    def owner(offset):
        chosen = None
        for start, _end, name in bounds:
            if start < offset:
                chosen = name
            else:
                break
        return chosen

    by_task = dict((name, []) for _s, _e, name in bounds)
    arguments = dict((name, []) for _s, _e, name in bounds)
    for offset, span in _spans_with_offsets(build_text):
        name = owner(offset)
        if name is not None:
            by_task[name].extend(_TOOL_IN_SPAN.findall(span))
            arguments[name].extend(
                value for value in re.findall(r'"((?:[^"\\]|\\.)*)"', span)
                if value.startswith("-")
            )
    rows = []
    for start, end, name in bounds:
        described = _DESCRIPTION.search(text[start:end])
        rows.append({
            "task": name,
            "tools": sorted(set(by_task[name])),
            "arguments": sorted(set(arguments[name])),
            "reachable": name in reachable,
            "described": sorted(set(_ANY_TOOL.findall(described.group(1)))) if described else [],
        })
    return rows


def route_b(build_text):
    """`(reachable, unreachable)` -- distinct tool basenames, split by whether `:test` runs them."""
    reachable, unreachable = set(), set()
    for row in exec_tasks(build_text):
        (reachable if row["reachable"] else unreachable).update(row["tools"])
    return sorted(reachable), sorted(unreachable)


# --- route A: `tools/verify.sh` ---------------------------------------------------------------

def route_a(verify_text):
    """Distinct tool basenames `tools/verify.sh` runs as a COMMAND WORD of its own body."""
    return sorted({
        os.path.basename(word)
        for word in p31.shell_command_words(verify_text)
        if word.startswith("tools/") or "/tools/" in word
    })


# --- the union, which is the answer -----------------------------------------------------------

def union(verify_text, build_text):
    reachable, _unreachable = route_b(build_text)
    return sorted(set(route_a(verify_text)) | set(reachable))


# --- the four historical predicates, kept so the drift is visible -----------------------------

#: `C-0210`'s SECOND predicate -- *"tools verify.sh invokes with no --self-test flag"* -- is
#: `route_a` itself, because every tool verify.sh runs with a self-test flag it also runs without
#: one; it is not re-implemented here, it is the same list.
_NAMED = re.compile(r"^(check-.*\.py|trace-answers\.py)$")
_LITERAL_GATE = re.compile(r'commandLine\("\$projectDir/tools/([^"]+)"((?:, "[^"]+")*)\)')
_HELPER_GATE = re.compile(r'commandLine\(mutationSnapshotArguments\("([^"]+)"\)')
_SELF_TEST_FLAGS = ("--self-test", "--selftest")


def naming_predicate(tool_names):
    """`ls tools/check-*.py tools/trace-answers.py` -- `CH-0222`'s and `CH-0243`'s predicate."""
    return sorted(name for name in tool_names if _NAMED.match(name))


def gradle_literal_without_self_test(build_text):
    """`C-0210`'s third predicate: a LITERAL path, no self-test flag."""
    text = p31.strip_kotlin_comments(build_text)
    return sorted({
        name for name, arguments in _LITERAL_GATE.findall(text)
        if not any(flag in arguments for flag in _SELF_TEST_FLAGS)
    })


def gradle_helper(build_text):
    """`CH-0286`'s fourth predicate: the helper shape, invisible to the third."""
    return sorted(set(_HELPER_GATE.findall(p31.strip_kotlin_comments(build_text))))


# --- the residue, printed beside the count ----------------------------------------------------

_IMPORT_SPEC = re.compile(r'spec_from_file_location\([^)]*?"(?:tools/)?([A-Za-z0-9_.\-]+\.py)"')
_PLAIN_IMPORT = re.compile(r'^\s*(?:import|from)\s+([a-z_][a-z0-9_]*)\b', re.M)


def import_edges(tree, basename):
    """The `tools/` modules `basename` loads -- by `spec_from_file_location` or by plain import."""
    if not basename.endswith(".py"):
        return set()
    source = tree.read("tools/" + basename)
    if source is None:
        return set()
    present = tree.tools()
    found = set(_IMPORT_SPEC.findall(source))
    for name in _PLAIN_IMPORT.findall(source):
        if name + ".py" in present:
            found.add(name + ".py")
    return found - {basename}


def import_only_residue(tree, seed):
    """Modules a tool on either route loads and that NO invocation names.

    A defect in one of these fails the run exactly as a defect in a gate does, and no predicate
    over an invocation can see it.  It is a residue and not a count, which is why it is printed
    and not summed.
    """
    seen, frontier = set(seed), set(seed)
    while frontier:
        following = set()
        for basename in frontier:
            following |= import_edges(tree, basename)
        following -= seen
        seen |= following
        frontier = following
    return sorted(seen - set(seed))


# --- arm 1: `BY-HAND` is the declaration the reachability set must agree with ------------------

def declared_by_hand(harness_source):
    """The harnesses `P-31`'s `HARNESSES` table declares `BY-HAND`, parsed rather than executed.

    Parsed with `ast` so the table can be read at a historical ref without running that ref's
    code, and so an implicit string concatenation in a row is folded exactly as the interpreter
    folds it (`CH-0238`: a regular expression over the first literal reads a truncated name).
    """
    tree = ast.parse(harness_source)
    sentinel = "BY-HAND"
    rows = None
    for node in tree.body:
        if isinstance(node, ast.Assign):
            targets = [t.id for t in node.targets if isinstance(t, ast.Name)]
            if "BY_HAND" in targets and isinstance(node.value, ast.Constant):
                sentinel = node.value.value
            if "HARNESSES" in targets:
                rows = node.value
    if rows is None:
        return []
    out = []
    for row in getattr(rows, "elts", []):
        elements = getattr(row, "elts", [])
        if len(elements) < 5 or not isinstance(elements[0], ast.Constant):
            continue
        shapes = []
        for shape in getattr(elements[4], "elts", []):
            if isinstance(shape, ast.Constant):
                shapes.append(shape.value)
            elif isinstance(shape, ast.Name):
                shapes.append(sentinel if shape.id == "BY_HAND" else shape.id)
        if sentinel in shapes:
            out.append(elements[0].value)
    return sorted(out)


# --- the census ---------------------------------------------------------------------------------

def census(tree):
    build_text = tree.read(BUILD)
    verify_text = tree.read(VERIFY)
    harness_source = tree.read(HARNESS_CENSUS)
    if build_text is None or verify_text is None:
        raise SystemExit("%s does not carry %s and %s" % (tree.label, BUILD, VERIFY))
    if "set -euo pipefail" not in verify_text:
        raise SystemExit("%s at %s does not set -euo pipefail" % (VERIFY, tree.label))
    if "./gradlew test" not in verify_text:
        raise SystemExit("%s at %s does not run ./gradlew test" % (VERIFY, tree.label))
    a = route_a(verify_text)
    reachable, unreachable = route_b(build_text)
    both = sorted(set(a) & set(reachable))
    whole = sorted(set(a) | set(reachable))
    present = tree.tools()
    return {
        "state": tree.label,
        "ref": tree.ref,
        "routeA": a,
        "routeBReachable": reachable,
        "routeBUnreachable": unreachable,
        "overlap": both,
        "union": whole,
        "execTasks": exec_tasks(build_text),
        "testDependsOn": sorted(test_dependencies(build_text)),
        "namingPredicate": naming_predicate(present),
        "gradleLiteralNoSelfTest": gradle_literal_without_self_test(build_text),
        "gradleHelper": gradle_helper(build_text),
        "declaredByHand": declared_by_hand(harness_source) if harness_source else [],
        "importOnlyResidue": import_only_residue(tree, whole),
        "sourcedNotInvoked": sorted({
            os.path.basename(m) for m in re.findall(
                r'source\s+"?\$\{?\w+\}?/(tools/[A-Za-z0-9_.\-]+\.sh)', verify_text
            )
        }),
        "missing": sorted(name for name in whole if name not in present),
        "notExecutable": sorted(
            name for name in whole if name in present and not present[name]
        ),
    }


def decomposition(published, reading, counted_helper=True):
    """How a shape-based published figure differs from the reachability union, term by term.

    A published figure of this family is built as `|route A| + |shape set|`, where the shape set is
    the LITERAL Gradle invocations carrying no self-test flag, plus -- for `CH-0286`'s figures and
    not for `C-0210`'s -- the HELPER-wired ones.  `counted_helper` says which, because `37` counted
    the literal shape only and `43`, `50` and `51` counted both, and the same three terms cannot be
    written for both without saying so.

    Three signed terms, and they can NEARLY CANCEL: at `C-0210`'s own ref they sum to `-1`, so
    `CH-0286`'s `43` sat within one of the true `42` by coincidence, and at this tree the same
    three sum to `-7`.  `CLAUDE.md`'s *decompose a ratio before predicting it*, on a difference of
    two censuses.
    """
    shape = set(reading["gradleLiteralNoSelfTest"])
    if counted_helper:
        shape |= set(reading["gradleHelper"])
    unreachable = set(reading["routeBUnreachable"])
    reachable = set(reading["routeBReachable"])
    return {
        "published": published,
        "union": len(reading["union"]),
        "difference": len(reading["union"]) - published,
        "reconstructsPublished": len(reading["routeA"]) + len(shape) == published,
        "terms": {
            "unreachableCounted": -len(shape & unreachable),
            "gradleToolsTheShapePredicateMisses": len(reachable - shape),
            "overlapAssertedAway": -len(reading["overlap"]),
        },
    }


# --- the gate -------------------------------------------------------------------------------

def check(tree):
    """The three invariants, every one able to fail and every one clean at `HEAD`."""
    reading = census(tree)
    defects = []
    by_hand = set(reading["declaredByHand"])
    unreachable = set(reading["routeBUnreachable"])
    for name in sorted(unreachable - by_hand):
        defects.append(
            "UNDECLARED-UNREACHABLE  tools/%s -- an Exec task runs it and :test does not depend "
            "on that task, so it CANNOT fail a verify.sh run, and P-31 does not declare it "
            "BY-HAND. Declare it, or wire the task into :test" % name
        )
    for name in sorted(by_hand - unreachable):
        defects.append(
            "DECLARED-BUT-REACHABLE  tools/%s -- P-31 declares it BY-HAND (it does not run bare) "
            "and :test depends on the task that runs it, so the build runs it bare" % name
        )
    for row in reading["execTasks"]:
        for named in row["described"]:
            if named not in row["tools"]:
                defects.append(
                    "DESCRIPTION-NAMES-WHAT-IT-DOES-NOT-RUN  %s -- its description names "
                    "tools/%s and its commandLine runs %s"
                    % (row["task"], named, ", ".join(row["tools"]) or "nothing")
                )
    for name in reading["missing"]:
        defects.append("MISSING  tools/%s is invoked and does not exist at %s"
                       % (name, reading["state"]))
    for name in reading["notExecutable"]:
        defects.append("NOT-EXECUTABLE  tools/%s is invoked and carries no executable bit" % name)
    return reading, defects


# --- reporting ------------------------------------------------------------------------------

def report(tree, published=None):
    reading = census(tree)
    print("the tools that can fail a DEFAULT `tools/verify.sh` run, at %s" % reading["state"])
    if reading["ref"]:
        print("  resolved ref: %s" % reading["ref"])
    print()
    print("  %-56s %s" % ("route A -- verify.sh's own command words", len(reading["routeA"])))
    print("  %-56s %s" % ("route B -- Exec tasks reachable from :test", len(reading["routeBReachable"])))
    print("  %-56s %s" % ("            of which also on route A (NOT disjoint)", len(reading["overlap"])))
    print("  %-56s %s" % ("UNION -- distinct tools that can fail the run", len(reading["union"])))
    print()
    print("  the four predicates this replaces, at the same state:")
    print("    %-54s %s" % ("ls tools/check-*.py tools/trace-answers.py", len(reading["namingPredicate"])))
    print("    %-54s %s" % ("verify.sh's own invocations", len(reading["routeA"])))
    print("    %-54s %s" % ('commandLine("$projectDir/tools/..."), no self-test',
                            len(reading["gradleLiteralNoSelfTest"])))
    print("    %-54s %s" % ('commandLine(mutationSnapshotArguments("..."))', len(reading["gradleHelper"])))
    print()
    print("  NOT counted, and why (a gate that comes clean must say what it does not reach):")
    print("    %-54s %s" % ("Exec tasks NOT reachable from :test", len(reading["routeBUnreachable"])))
    for name in reading["routeBUnreachable"]:
        print("        %s" % name)
    print("    %-54s %s" % ("modules reached ONLY by import", len(reading["importOnlyResidue"])))
    for name in reading["importOnlyResidue"]:
        print("        %s" % name)
    print("    %-54s %s" % ("sourced by verify.sh, never a command word",
                            len(reading["sourcedNotInvoked"])))
    for name in reading["sourcedNotInvoked"]:
        print("        %s" % name)
    print("    %-54s %s" % ("the Kotlin suite ./gradlew test runs", "not a tool, not counted"))
    print("    %-54s %s" % ("a NON-DEFAULT invocation (--no-checks, --drop, -x)", "out of scope"))
    print()
    print("  arm 1 -- P-31 declares BY-HAND: %d; unreachable: %d; equal: %s"
          % (len(reading["declaredByHand"]), len(reading["routeBUnreachable"]),
             set(reading["declaredByHand"]) == set(reading["routeBUnreachable"])))
    if published is not None:
        split = decomposition(published, reading)
        print()
        print("  against the published %d: difference %+d, decomposed" % (published, split["difference"]))
        for term, value in sorted(split["terms"].items()):
            print("    %-54s %+d" % (term, value))
    print("# %d distinct tool(s) can fail a default tools/verify.sh run at %s"
          % (len(reading["union"]), reading["state"]))
    return 0


# --- self-tests -------------------------------------------------------------------------------

_FIXTURE_BUILD = '''
tasks.register<Exec>("testAlpha") {
    description = "Runs tools/alpha.py, the alpha gate"
    commandLine("$projectDir/tools/alpha.py", "--check")
}

tasks.register<Exec>("testBeta") {
    description = "Runs tools/beta.py <snapshot>; not in :test"
    commandLine(mutationSnapshotArguments("beta.py"))
}

tasks.register<Exec>("testDeltaTask") {
    commandLine("$projectDir/tools/delta.py")
}

tasks.register<Exec>("testGamma") {
    description = "Runs tools/gamma.py"
    commandLine("$projectDir/tools/gamma.py", "--self-test")
}

tasks.named("test") {
    dependsOn(
        "testAlpha",
        // "testBeta" is deliberately NOT here
        "testDeltaTask",
        "testGamma"
    )
}
'''

_FIXTURE_VERIFY = '''#!/usr/bin/env bash
set -euo pipefail
source "$root/tools/snapshot.sh"
./gradlew test "$@"
tools/alpha.py --check
tools/delta.py
echo "skip with: tools/epsilon.py"
'''


class _StubTree(object):
    """A repository state held in memory, so every gate arm is reachable from a named test.

    `C-0161`: a mutation that fails nothing is usually a FIXTURE that could not discriminate.  The
    three `--check` arms read a whole tree, so without a stub none of them is held open by
    anything, and the mutation table would report a gate nobody tests as load-bearing.
    """

    label = "stub"
    ref = None

    def __init__(self, by_hand=("beta.py",), describe_ghost=False, drop=None,
                 not_executable=None, verify=None):
        self.by_hand = list(by_hand)
        self.describe_ghost = describe_ghost
        self.drop = drop
        self.not_executable = not_executable
        self.verify = _FIXTURE_VERIFY if verify is None else verify
        self.sources = {
            "alpha.py": "import mid\n",
            "mid.py": "import leaf\n",
            "leaf.py": "import os\n",
        }

    def read(self, path):
        if path == BUILD:
            build = _FIXTURE_BUILD
            if self.describe_ghost:
                build = build.replace('"Runs tools/alpha.py, the alpha gate"',
                                      '"Runs tools/alpha.py and tools/ghost.py"')
            return build
        if path == VERIFY:
            return self.verify
        if path == HARNESS_CENSUS:
            rows = "".join('    ("%s", "K", "S", "t", (BY_HAND,)),\n' % name
                           for name in self.by_hand)
            return 'BY_HAND = "BY-HAND"\nHARNESSES = (\n%s)\n' % rows
        if path.startswith("tools/"):
            return self.sources.get(path[len("tools/"):])
        return None

    def tools(self):
        present = {
            "alpha.py": True, "beta.py": True, "gamma.py": True, "delta.py": True,
            "mid.py": True, "leaf.py": True, "snapshot.sh": True,
        }
        if self.drop:
            present.pop(self.drop, None)
        if self.not_executable:
            present[self.not_executable] = False
        return present


def _refuses(tree):
    try:
        census(tree)
    except SystemExit:
        return True
    return False


#: A synthetic reading: two tools on route A (`a`, `d`), two reachable (`a`, `b`), one unreachable
#: (`z`, helper-wired), an overlap of one (`a`), and a literal shape set of one (`b`).  The union
#: is `{a, b, d}` = 3; a publisher counting both shapes writes `2 + 2` = 4 and one counting the
#: literal shape only writes `2 + 1` = 3.
_SYNTHETIC = {
    "routeA": ["a", "d"], "union": ["a", "b", "d"], "routeBUnreachable": ["z"],
    "routeBReachable": ["a", "b"], "overlap": ["a"],
    "gradleLiteralNoSelfTest": ["b"], "gradleHelper": ["z"],
}


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    ok("T-334 a task in dependsOn is reachable and one omitted from it is not",
       [row["reachable"] for row in exec_tasks(_FIXTURE_BUILD)] == [True, False, True, True])
    ok("T-334 the HELPER wiring is resolved to its tool, which is CH-0286's whole finding",
       exec_tasks(_FIXTURE_BUILD)[1]["tools"] == ["beta.py"])
    ok("T-334 the LITERAL wiring is resolved to its tool",
       exec_tasks(_FIXTURE_BUILD)[0]["tools"] == ["alpha.py"])
    ok("T-334 route B EXCLUDES a helper-wired task :test does not depend on -- the twelve",
       route_b(_FIXTURE_BUILD) == (["alpha.py", "delta.py", "gamma.py"], ["beta.py"]))
    ok("T-334 route B carries a --self-test invocation, because a failing self-test fails the run",
       "gamma.py" in route_b(_FIXTURE_BUILD)[0])
    ok("T-334 a task wired ONLY by a helper and then added to dependsOn becomes reachable",
       route_b(_FIXTURE_BUILD.replace('// "testBeta" is deliberately NOT here', '"testBeta",'))
       == (["alpha.py", "beta.py", "delta.py", "gamma.py"], []))
    ok("T-334 a COMMENTED-OUT dependsOn entry does not make a task reachable",
       "testBeta" not in test_dependencies(_FIXTURE_BUILD))
    ok("T-334 a commented-out commandLine is not a wiring, which is P-31's blanking",
       exec_tasks('tasks.register<Exec>("t") {\n    // commandLine("$projectDir/tools/a.py")\n}\n'
                  'tasks.named("test") { dependsOn("t") }')[0]["tools"] == [])
    ok("T-334 a DESCRIPTION naming a tool is not a wiring -- C-0206's use, not mention",
       route_b('tasks.register<Exec>("t") {\n    description = "Runs tools/ghost.py"\n'
               '    commandLine("$projectDir/tools/real.py")\n}\n'
               'tasks.named("test") { dependsOn("t") }') == (["real.py"], []))
    ok("T-334 route A takes verify.sh's command words and not its echoes",
       route_a(_FIXTURE_VERIFY) == ["alpha.py", "delta.py"])
    ok("T-334 a SOURCED shell library is not a command word, so it is residue and not a count",
       "snapshot.sh" not in route_a(_FIXTURE_VERIFY))
    ok("T-334 the union DE-DUPLICATES a tool on both routes -- the sets are not disjoint",
       union(_FIXTURE_VERIFY, _FIXTURE_BUILD) == ["alpha.py", "delta.py", "gamma.py"])
    ok("T-334 the union is smaller than the sum exactly by the overlap",
       len(union(_FIXTURE_VERIFY, _FIXTURE_BUILD))
       == len(route_a(_FIXTURE_VERIFY)) + len(route_b(_FIXTURE_BUILD)[0]) - 2)
    ok("T-334 one tool invoked by two reachable tasks counts ONCE",
       route_b('tasks.register<Exec>("a") { commandLine("$projectDir/tools/x.py", "--self-test") }\n'
               'tasks.register<Exec>("b") { commandLine("$projectDir/tools/x.py", "--check") }\n'
               'tasks.named("test") { dependsOn("a", "b") }') == (["x.py"], []))
    ok("T-334 the naming predicate is a FILENAME PREFIX and is kept only so the drift is visible",
       naming_predicate({"check-a.py": True, "trace-answers.py": True, "T-1-census.py": True})
       == ["check-a.py", "trace-answers.py"])
    ok("T-334 the literal predicate drops a --self-test invocation, which is why it is not the answer",
       gradle_literal_without_self_test(_FIXTURE_BUILD) == ["alpha.py", "delta.py"])
    ok("T-334 the literal predicate cannot see the helper shape at all -- CH-0286",
       "beta.py" not in gradle_literal_without_self_test(_FIXTURE_BUILD))
    ok("T-334 the helper predicate sees only the helper shape",
       gradle_helper(_FIXTURE_BUILD) == ["beta.py"])
    ok("T-334 BY-HAND is parsed from the table through the SENTINEL name, not by a literal",
       declared_by_hand('BY_HAND = "BY-HAND"\nHARNESSES = (\n'
                        '    ("a.py", "K", "S", "t", ("killed-by",)),\n'
                        '    ("b.py", "K", "S", "t", (BY_HAND,)),\n)\n') == ["b.py"])
    ok("T-334 a row whose name is written as two adjacent literals is folded, as ast folds it",
       declared_by_hand('BY_HAND = "BY-HAND"\nHARNESSES = (\n'
                        '    ("long-" "name.py", "K", "S", "t", (BY_HAND,)),\n)\n')
       == ["long-name.py"])
    ok("T-334 a table with no BY-HAND row yields the empty declaration, not an error",
       declared_by_hand('BY_HAND = "BY-HAND"\nHARNESSES = (\n'
                        '    ("a.py", "K", "S", "t", ("killed-by",)),\n)\n') == [])
    ok("T-334 renaming the sentinel VALUE does not break the parse, because it is read from source",
       declared_by_hand('BY_HAND = "MANUAL"\nHARNESSES = (\n'
                        '    ("b.py", "K", "S", "t", (BY_HAND,)),\n)\n') == ["b.py"])
    ok("T-334 the decomposition of a shape-based figure has THREE signed terms",
       sorted(decomposition(4, _SYNTHETIC)["terms"])
       == ["gradleToolsTheShapePredicateMisses", "overlapAssertedAway", "unreachableCounted"])
    ok("T-334 the decomposition's terms sum to the difference it decomposes",
       (lambda split: sum(split["terms"].values()) == split["difference"])(
           decomposition(4, _SYNTHETIC)))
    ok("T-334 the decomposition reconstructs the figure it decomposes from its own two parts",
       decomposition(4, _SYNTHETIC)["reconstructsPublished"])
    ok("T-334 a figure that counted the LITERAL shape only decomposes differently, and C-0210's "
       "37 is exactly that case",
       decomposition(3, _SYNTHETIC, counted_helper=False)["terms"]["unreachableCounted"] == 0
       and sum(decomposition(3, _SYNTHETIC, counted_helper=False)["terms"].values())
       == decomposition(3, _SYNTHETIC, counted_helper=False)["difference"])
    ok("T-334 a plain sibling import is an edge only where the module exists under tools/",
       _PLAIN_IMPORT.findall("import queue_verdicts\nimport os\n") == ["queue_verdicts", "os"])
    ok("T-334 an Exec block's description is read only up to the NEXT registered task",
       exec_tasks(_FIXTURE_BUILD)[0]["described"] == ["alpha.py"])
    ok("T-334 a task carrying NO description describes nothing, rather than borrowing the next "
       "task's -- the bound is where the next registration begins",
       exec_tasks(_FIXTURE_BUILD)[2]["described"] == [])
    ok("T-334 the invocation's own FLAGS are carried, so the --self-test distinction C-0210 "
       "filtered on is derivable from the same pass and is a stated choice, not an omission",
       [row["arguments"] for row in exec_tasks(_FIXTURE_BUILD)]
       == [["--check"], [], [], ["--self-test"]])
    ok("T-334 route B carries a task whose ONLY invocation is a self-test",
       "gamma.py" in route_b(_FIXTURE_BUILD)[0]
       and exec_tasks(_FIXTURE_BUILD)[3]["arguments"] == ["--self-test"])
    ok("T-334 the sentinel is read from the table, so a row spelling a RENAMED sentinel "
       "literally is still declared",
       declared_by_hand('BY_HAND = "MANUAL"\nHARNESSES = (\n'
                        '    ("b.py", "K", "S", "t", ("MANUAL",)),\n'
                        '    ("c.py", "K", "S", "t", ("BY-HAND",)),\n)\n') == ["b.py"])

    stub = _StubTree()
    ok("T-334 the import residue is the CLOSURE and not one step of it",
       import_only_residue(stub, ["alpha.py"]) == ["leaf.py", "mid.py"])
    ok("T-334 a module already on a route is not residue, because an invocation names it",
       import_only_residue(stub, ["alpha.py", "mid.py", "leaf.py"]) == [])
    ok("T-334 the gate is CLEAN on a tree whose BY-HAND declaration matches its unreachable set",
       check(_StubTree())[1] == [])
    ok("T-334 arm 1 fires on an UNREACHABLE task whose tool is not declared BY-HAND",
       any("UNDECLARED-UNREACHABLE" in defect
           for defect in check(_StubTree(by_hand=[]))[1]))
    ok("T-334 arm 1 fires in the OTHER direction too -- BY-HAND and reachable is a bare run",
       any("DECLARED-BUT-REACHABLE" in defect
           for defect in check(_StubTree(by_hand=["beta.py", "alpha.py"]))[1]))
    ok("T-334 arm 2 fires where a description names a tool the commandLine does not run",
       any("DESCRIPTION-NAMES-WHAT-IT-DOES-NOT-RUN" in defect
           for defect in check(_StubTree(describe_ghost=True))[1]))
    ok("T-334 arm 3 fires on an invoked tool that does not exist",
       any("MISSING" in defect for defect in check(_StubTree(drop="delta.py"))[1]))
    ok("T-334 arm 3 fires on an invoked tool carrying no executable bit",
       any("NOT-EXECUTABLE" in defect
           for defect in check(_StubTree(not_executable="delta.py"))[1]))
    ok("T-334 the census REFUSES a verify.sh that does not run ./gradlew test",
       _refuses(_StubTree(verify=_FIXTURE_VERIFY.replace("./gradlew test \"$@\"", ":"))))
    ok("T-334 the census REFUSES a verify.sh that does not set -euo pipefail",
       _refuses(_StubTree(verify=_FIXTURE_VERIFY.replace("set -euo pipefail", "set -u"))))

    for name, passed in checks:
        print("%s  %s" % ("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# %d self-test(s), %d failure(s)" % (len(checks), len(failed)))
    return 1 if failed else 0


def main(argv=None):
    parser = argparse.ArgumentParser(
        description="How many distinct tools can fail a tools/verify.sh run, by reachability."
    )
    parser.add_argument("--ref", default=None,
                        help="a git ref to read the census at (default: the working tree)")
    parser.add_argument("--check", action="store_true",
                        help="the three gated invariants; exit 1 on any defect")
    parser.add_argument("--published", type=int, default=None,
                        help="decompose a published figure against the reachability union")
    parser.add_argument("--self-test", dest="self_test", action="store_true",
                        help="named self-tests over in-memory fixtures; reads no repository state")
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return _self_test()
    tree = Tree(arguments.ref)
    if arguments.check:
        reading, defects = check(tree)
        for defect in defects:
            print(defect)
        print("# %d defect(s) at %s; %d unreachable Exec task(s), %d declared BY-HAND, "
              "%d distinct tool(s) can fail a default tools/verify.sh run"
              % (len(defects), reading["state"], len(reading["routeBUnreachable"]),
                 len(reading["declaredByHand"]), len(reading["union"])))
        return 1 if defects else 0
    return report(tree, arguments.published)


if __name__ == "__main__":
    sys.exit(main())
