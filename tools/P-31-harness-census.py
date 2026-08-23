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
# P-31 -- EVERY MUTATION HARNESS'S REFERENCES INTO ITS SUBJECT, RESOLVED.
#
#     tools/P-31-harness-census.py              the census, human-readable
#     tools/P-31-harness-census.py --check      exit 1 on any unresolved reference
#     tools/P-31-harness-census.py --json       the census as JSON, for the emitter
#     tools/P-31-harness-census.py --self-test  the named tests
#     tools/P-31-harness-census.py --tree DIR   census a checkout other than this one
#
# WHY THIS EXISTS.  A mutation harness is a reference into somebody ELSE's source: a literal
# ANCHOR of text to be replaced, or the NAME of an attribute to be reassigned.  A refactor of the
# subject orphans that reference, and the harness then measures nothing -- while its headline
# ("N mutations, 0 survivors") can stay unchanged.  `CH-0237` recorded one direction of this in
# iteration 43: a fixture that stopped matching the tree made all 24 rows of a table read *killed*
# off one `FileNotFoundError`.  `P-31` is the other direction and the one that actually blocked
# the build: `P-30` lifted the queue's verdict predicate into `tools/queue_verdicts.py`, four of
# `tools/test-check-queue-vocabulary.py`'s six anchors went looking for text that had moved one
# file across, and the wired Gradle task went red at `9620d3e` -- `P-30`'s own commit.  It stayed
# red for a whole iteration because two claims each excluded the task on the ground that the red
# was *"a concurrent agent's in-flight file"*, and each was looking at the other.
#
# THAT HARNESS SHOUTED ONLY BECAUSE IT ASSERTS `source.count(old) == 1`.  A harness that does not
# assert its anchor count reads `killed` off a mutation that never applied.  So the class is
# SILENT IN PRINCIPLE, and a convention -- *re-run the mutation tests after a refactor* -- is what
# `CLAUDE.md` has recorded five times as not being a mechanism.  This is the mechanism: one pass
# over every harness in `tools/`, resolving every reference it makes into its subject, wired as a
# build failure.
#
# WHY AN ANCHOR COUNT IS `n/a` FOR SOME HARNESSES.  A harness that mutates by REASSIGNING a name
# on the imported subject, rather than by replacing text in it, is loud about a rename for free: a
# renamed attribute makes the assignment a no-op, the mutant is then identical to the original,
# and it fails no named test -- which every harness here already reports as a SURVIVOR and exits 1
# on.  So `assertsAnchorCount` is reported as `n/a` where a harness declares no text anchors, and
# the column is not a defect count.  What those harnesses still needed, and now have, is a
# BASELINE: `CH-0237`'s residue was that nothing asserted the unmutated subject passes.
#
# WHAT IS DERIVED AND WHAT IS DECLARED.  The harness TABLE below is declared -- which files are
# harnesses, and how each one's mutation list is shaped.  Everything else is derived by reading
# the harness: its anchors and target files come out of its own table, its symbol references out
# of its own syntax tree, whether it asserts an anchor count out of an AST pattern, whether it
# measures a baseline out of its own identifiers, and whether it is wired out of
# `build.gradle.kts` and `tools/verify.sh`.  A declared harness whose adapter stops working is a
# hard error, not a zero.

import argparse
import ast
import io
import json
import os
import subprocess
import sys
import tempfile
import tokenize

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

# --- the declared table ------------------------------------------------------------------------
#
# `kind` is what the harness's references INTO ITS SUBJECT are made of:
#
#   TEXT-ANCHOR      a literal string of the subject's source, replaced wholesale
#   ATTRIBUTE        a name on the imported subject module, reassigned or read
#   REIMPLEMENTATION the mutant is a hand-written Python function; the only references into the
#                    subject are the names it imports from it, which are ATTRIBUTE references too
#
# `shape` names the adapter that reads the harness's own mutation table.
HARNESSES = (
    ("test-check-queue-vocabulary.py", "TEXT-ANCHOR", "name_file_old_new",
     "check-queue-vocabulary.py + queue_verdicts.py"),
    ("P-30-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "queue_verdicts.py + trace-answers.py + check-queue-vocabulary.py"),
    ("T-281-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "census_discharges.py + T-234-census.py"),
    ("T-283-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "queue_verdicts.py + check-queue-vocabulary.py"),
    ("T-289-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "queue_verdicts.py + check-queue-vocabulary.py"),
    ("T-292-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "T-292-column-repair.py + check-queue-vocabulary.py"),
    ("T-295-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "T-295-mutation-input-census.py"),
    ("T-234-mutation-test.py", "TEXT-ANCHOR", "kind_name_path_subs",
     "T-234-census.py + T-234-emit-classification.py"),
    ("T-280-mutation-test.py", "TEXT-ANCHOR", "kind_name_subs",
     "T-234-census.py"),
    ("T-278-mutation-test.py", "ATTRIBUTE", "attributes",
     "T-278-emitter-rounding-census.py + T-278-rounding-simulation.py"),
    ("T-225-mutation-test.py", "ATTRIBUTE", "attributes",
     "check-result-file-hygiene.py"),
    ("T-249-mutation-test.py", "REIMPLEMENTATION", "attributes",
     "check-result-file-hygiene.py"),
    ("T-250-mutation-test.py", "REIMPLEMENTATION", "attributes",
     "check-result-file-hygiene.py"),
    ("T-297-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "src/main/kotlin/tile/CrossoverCommonMode.kt + src/main/kotlin/tile/HoneycombGrillage.kt"),
    ("T-298-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new", "trace-answers.py"),
)

# Which module file each ATTRIBUTE receiver in a harness stands for.  A receiver not named here is
# not a reference into a subject (a local, a record, a namespace of the harness's own).
RECEIVERS = {
    "T-278-mutation-test.py": {
        "census": "T-278-emitter-rounding-census.py",
        "simulation": "T-278-rounding-simulation.py",
    },
    "T-225-mutation-test.py": {
        "module": "check-result-file-hygiene.py",
        "hygiene": "check-result-file-hygiene.py",
        "m": "check-result-file-hygiene.py",
    },
    "T-249-mutation-test.py": {"module": "check-result-file-hygiene.py"},
    "T-250-mutation-test.py": {"module": "check-result-file-hygiene.py"},
}


# --- derivations -------------------------------------------------------------------------------

def asserts_anchor_count(source):
    """True when the harness compares `<something>.count(...)` against 1.

    That comparison is the whole reason this repository saw the `P-30` orphan at all: without it a
    mutation that fails to apply is indistinguishable from one that applies and is killed.
    """
    tree = ast.parse(source)

    def _is_count(node):
        return (isinstance(node, ast.Call) and isinstance(node.func, ast.Attribute)
                and node.func.attr == "count")

    # Names bound from a `.count(...)` call.  Four of this repository's harnesses bind the count
    # to a local and compare the local, and the census's own first run duly reported all four as
    # not asserting their anchors -- a false negative about exactly the property under study.
    from_count = set()
    for node in ast.walk(tree):
        if isinstance(node, ast.Assign) and _is_count(node.value):
            for target in node.targets:
                if isinstance(target, ast.Name):
                    from_count.add(target.id)

    for node in ast.walk(tree):
        if not isinstance(node, ast.Compare) or len(node.comparators) != 1:
            continue
        left = node.left
        if not (_is_count(left) or (isinstance(left, ast.Name) and left.id in from_count)):
            continue
        right = node.comparators[0]
        if isinstance(right, ast.Constant) and right.value == 1:
            return True
    return False


def measures_baseline(source):
    """True when the harness names a baseline in CODE (not in a comment or a string).

    `CH-0237`: a mutation table's killer counts are only evidence if an UNMUTATED copy passes.
    Derived from the harness's own identifiers, because a comment saying *baseline* is exactly the
    thing that is not a measurement.
    """
    for token in tokenize.generate_tokens(io.StringIO(source).readline):
        if token.type == tokenize.NAME and "baseline" in token.string.lower():
            return True
    return False


def symbol_references(source, receivers):
    """[(receiver, attribute)] for every attribute of a declared receiver the harness names.

    Both directions count: an assignment (`census.ROUNDING_CALLS = ...`, `setattr(m, "X", ...)`)
    is a mutation, and a read (`module.PROSE_NUMBER`) is what a re-implementation harness builds
    its mutant out of.  Either one is a reference into the subject and either one is orphaned by a
    rename.
    """
    found = set()
    for node in ast.walk(ast.parse(source)):
        if isinstance(node, ast.Attribute) and isinstance(node.value, ast.Name):
            if node.value.id in receivers:
                found.add((node.value.id, node.attr))
        elif (isinstance(node, ast.Call) and isinstance(node.func, ast.Name)
                and node.func.id == "setattr" and len(node.args) >= 2
                and isinstance(node.args[0], ast.Name) and node.args[0].id in receivers
                and isinstance(node.args[1], ast.Constant)
                and isinstance(node.args[1].value, str)):
            found.add((node.args[0].id, node.args[1].value))
    return sorted(found)


def defines_symbol(source, name):
    """True when `name` is a top-level assignment, def or class of this source.

    Static rather than by import: importing a checker to ask whether it still has an attribute is
    a side effect for a question about syntax, and `CLAUDE.md` records what an emitter that runs
    for a census can do to a tree.
    """
    try:
        tree = ast.parse(source)
    except SyntaxError:
        return False
    for node in tree.body:
        if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef, ast.ClassDef)):
            if node.name == name:
                return True
        elif isinstance(node, ast.Assign):
            for target in node.targets:
                if isinstance(target, ast.Name) and target.id == name:
                    return True
        elif isinstance(node, ast.AnnAssign):
            if isinstance(node.target, ast.Name) and node.target.id == name:
                return True
    return False


def unresolved_anchors(anchors, read):
    """[(harness, name, target, occurrences)] for every anchor that does not occur EXACTLY once.

    `anchors` is [(harness, name, target basename, anchor text)]; `read` maps a basename to its
    source.  Two occurrences is as much a defect as none: a replacement that hits twice is not the
    mutation the row's name describes.
    """
    bad = []
    for harness, name, target, text in anchors:
        source = read(target)
        occurrences = -1 if source is None else source.count(text)
        if occurrences != 1:
            bad.append((harness, name, target, occurrences))
    return bad


def unresolved_symbols(symbols, read):
    """[(harness, receiver, attribute, target)] for a symbol the subject does not define."""
    bad = []
    for harness, receiver, attribute, target in symbols:
        source = read(target)
        if source is None or not defines_symbol(source, attribute):
            bad.append((harness, receiver, attribute, target))
    return bad


def discovers_harnesses(sources):
    """[basename] for every file in `tools/` that IS a mutation harness, however it is named.

    Two rules, because the naming convention has one exception and a convention is not a
    mechanism: a basename ending `mutation-test.py`, OR a file declaring a top-level ALL-CAPS name
    containing `MUTATION` -- which is how `tools/test-check-queue-vocabulary.py` (`BASE_MUTATIONS`)
    is found.  Emitters that merely *report* a mutation table declare lower-case names
    (`_mutations`, `mutation_coverage`) and are correctly not harnesses.

    This is what stops the declared table above from being a census that stopped: a harness
    somebody writes tomorrow and does not declare fails the gate rather than being invisible to it.
    """
    found = []
    for basename, source in sorted(sources.items()):
        if not basename.endswith(".py"):
            continue
        if basename.endswith("mutation-test.py"):
            found.append(basename)
            continue
        try:
            tree = ast.parse(source)
        except SyntaxError:
            continue
        for node in tree.body:
            names = []
            if isinstance(node, ast.Assign):
                names = [t.id for t in node.targets if isinstance(t, ast.Name)]
            if any(name.isupper() and "MUTATION" in name for name in names):
                found.append(basename)
                break
    return found


def wired_in(basename, build_text, verify_text):
    """Where a harness is run from, of `build.gradle.kts` and `tools/verify.sh`."""
    places = []
    if basename in build_text:
        places.append("build.gradle.kts")
    if basename in verify_text:
        places.append("tools/verify.sh")
    return places


# --- the adapters ------------------------------------------------------------------------------

def _import(tools, basename):
    import importlib.util
    spec = importlib.util.spec_from_file_location(
        "p31_" + basename.replace("-", "_").replace(".py", ""), os.path.join(tools, basename)
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


# A harness whose table predates the per-row target file.  `P-29`'s rows were `(name, old, new)`
# with ONE implicit subject; recording that here is what lets the census be run against the
# committed past, which is where the defect it exists for actually is.
LEGACY_IMPLICIT_TARGET = {
    "test-check-queue-vocabulary.py": "check-queue-vocabulary.py",
}


def _archive(ref, repository=ROOT):
    """A `git archive` of `ref`, or None where git cannot supply one."""
    directory = tempfile.mkdtemp(prefix="P-31-archive.")
    archive = subprocess.run(
        ["git", "-C", repository, "archive", ref], capture_output=True
    )
    if archive.returncode != 0:
        return None
    extract = subprocess.run(
        ["tar", "-x", "-C", directory], input=archive.stdout, capture_output=True
    )
    return directory if extract.returncode == 0 else None


def _adapt(shape, module, harness):
    """[(harness, mutation name, target basename, anchor text)] from a harness's own table."""
    rows = []
    if shape == "name_file_old_new":
        table = list(getattr(module, "MUTATIONS", []) or getattr(module, "BASE_MUTATIONS", []))
        if hasattr(module, "BASE_MUTATIONS"):
            table = list(module.BASE_MUTATIONS) + list(module.per_classification_mutations())
        for row in table:
            if len(row) == 3:
                name, old, _new = row
                filename = LEGACY_IMPLICIT_TARGET[harness]
            else:
                name, filename, old, _new = row
            # A subject need not be a Python module in `tools/`: `T-297-mutation-test.py` mutates
            # Kotlin under `src/`.  A declared path keeps its path and is resolved from the tree
            # root; a bare basename keeps the historical `tools/` reading.
            rows.append((harness, name,
                         filename if "/" in filename else os.path.basename(filename), old))
    elif shape == "kind_name_path_subs":
        for _kind, name, path, subs in module.mutations():
            for old, _new in subs:
                rows.append((harness, name, os.path.basename(path), old))
    elif shape == "kind_name_subs":
        for _kind, name, subs in module.mutations():
            for old, _new in subs:
                rows.append((harness, name, os.path.basename(module.CENSUS), old))
    elif shape == "attributes":
        return []
    else:
        raise AssertionError("unknown adapter shape " + shape)
    return rows


def census(tree=ROOT, strict=True):
    """One row per declared harness, everything but the table itself derived.

    `strict` is the difference between censusing THIS tree, where a declared harness that is
    missing is a hard error, and censusing a HISTORICAL one, where a harness that had not been
    written yet is simply absent.
    """
    tools = os.path.join(tree, "tools")
    cache = {}
    # A harness imports its subject by name through `sys.path`, so a census of a second tree in
    # one process would otherwise be handed the FIRST tree's modules out of `sys.modules` -- the
    # same class of defect this whole task is about, one level up.
    for cached in ("check-queue-vocabulary", "queue_verdicts", "trace-answers"):
        sys.modules.pop(cached, None)

    def read(basename):
        if basename not in cache:
            path = os.path.join(tools, basename)
            if not os.path.exists(path):
                # a subject outside `tools/`, declared with its path from the tree root
                path = os.path.join(tree, basename)
            cache[basename] = (open(path, encoding="utf-8").read()
                               if os.path.exists(path) else None)
        return cache[basename]

    build_text = read_file(os.path.join(tree, "build.gradle.kts"))
    verify_text = read_file(os.path.join(tools, "verify.sh"))

    sys.path.insert(0, tools)
    rows = []

    for basename, kind, shape, subjects in HARNESSES:
        source = read(basename)
        if source is None:
            if strict:
                raise AssertionError("declared harness is missing: " + basename)
            continue
        anchors = _adapt(shape, _import(tools, basename), basename) if shape != "attributes" else []
        receivers = RECEIVERS.get(basename, {})
        symbols = [(basename, receiver, attribute, receivers[receiver])
                   for receiver, attribute in symbol_references(source, receivers)]
        rows.append({
            "harness": basename,
            "kind": kind,
            "subjects": subjects,
            "anchorsDeclared": len(anchors),
            "anchorsUnresolved": len(unresolved_anchors(anchors, read)),
            "symbolsDeclared": len(symbols),
            "symbolsUnresolved": len(unresolved_symbols(symbols, read)),
            "assertsAnchorCount": asserts_anchor_count(source),
            "measuresBaseline": measures_baseline(source),
            "wiredIn": wired_in(basename, build_text, verify_text),
            "_anchorDefects": unresolved_anchors(anchors, read),
            "_symbolDefects": unresolved_symbols(symbols, read),
        })
    return rows


def undeclared_harnesses(tree=ROOT):
    """[basename] for every discovered harness that no row of `HARNESSES` declares."""
    tools = os.path.join(tree, "tools")
    sources = {}
    for basename in os.listdir(tools):
        path = os.path.join(tools, basename)
        if basename.endswith(".py") and os.path.isfile(path):
            sources[basename] = open(path, encoding="utf-8").read()
    declared = {row[0] for row in HARNESSES}
    return [b for b in discovers_harnesses(sources) if b not in declared]


def read_file(path):
    return open(path, encoding="utf-8").read() if os.path.exists(path) else ""


# --- the self-tests ----------------------------------------------------------------------------

def _selftest():
    failures, ran = [], []

    def check(name, condition):
        ran.append(name)
        if not condition:
            failures.append(name)

    def read_of(mapping):
        return lambda basename: mapping.get(basename)

    # --- an anchor resolves, or it does not ---
    check(
        "P-31 an anchor occurring exactly once resolves",
        unresolved_anchors([("h", "m", "s.py", "needle")], read_of({"s.py": "a needle b"})) == [],
    )
    check(
        "P-31 a SYNTHETIC ORPHAN — the anchor's text has moved away — is reported",
        unresolved_anchors([("h", "m", "s.py", "needle")], read_of({"s.py": "a b"}))
        == [("h", "m", "s.py", 0)],
    )
    check(
        "P-31 an anchor occurring TWICE is a defect too — the replacement is not the named one",
        unresolved_anchors([("h", "m", "s.py", "n")], read_of({"s.py": "n n"}))
        == [("h", "m", "s.py", 2)],
    )
    check(
        "P-31 an anchor whose TARGET FILE is gone is reported rather than skipped",
        unresolved_anchors([("h", "m", "gone.py", "n")], read_of({}))
        == [("h", "m", "gone.py", -1)],
    )

    # --- a symbol reference resolves against the subject's own syntax ---
    check(
        "P-31 a module-level assignment defines a symbol",
        defines_symbol("A = 1\n", "A") and not defines_symbol("A = 1\n", "B"),
    )
    check(
        "P-31 a module-level def defines a symbol",
        defines_symbol("def f():\n    pass\n", "f"),
    )
    check(
        "P-31 a name bound only INSIDE a function does not define a module symbol",
        not defines_symbol("def f():\n    A = 1\n", "A"),
    )
    check(
        "P-31 a RENAMED subject symbol is an unresolved reference",
        unresolved_symbols([("h", "module", "OLD", "s.py")], read_of({"s.py": "NEW = 1\n"}))
        == [("h", "module", "OLD", "s.py")],
    )
    check(
        "P-31 an attribute ASSIGNED on a declared receiver is a reference",
        symbol_references("def m(census):\n    census.CALLS = ()\n", {"census": "x.py"})
        == [("census", "CALLS")],
    )
    check(
        "P-31 an attribute READ from a declared receiver is a reference too",
        symbol_references("x = module.PROSE_NUMBER\n", {"module": "x.py"})
        == [("module", "PROSE_NUMBER")],
    )
    check(
        "P-31 a `setattr` with a literal name is a reference",
        symbol_references('setattr(m, "KEYS", ())\n', {"m": "x.py"}) == [("m", "KEYS")],
    )
    check(
        "P-31 an attribute on an UNDECLARED receiver is not a reference into a subject",
        symbol_references("other.THING\n", {"module": "x.py"}) == [],
    )

    # --- the two reported properties, derived rather than declared ---
    check(
        "P-31 a harness comparing an anchor count against 1 asserts its anchor",
        asserts_anchor_count("if text.count(old) != 1:\n    pass\n"),
    )
    check(
        "P-31 a harness that only replaces does NOT assert its anchor",
        not asserts_anchor_count("text = text.replace(old, new)\n"),
    )
    check(
        "P-31 a count compared against something OTHER than 1 is not an anchor assertion",
        not asserts_anchor_count("if text.count(old) != 2:\n    pass\n"),
    )
    # Found by the census's own first run: FOUR harnesses that plainly assert their anchor were
    # reported as not asserting it, because they bind the count to a local first.  A derivation is
    # a number like any other, and a false NEGATIVE here would have said *this harness would fail
    # silently* about the very harness whose shout started this task.
    check(
        "P-31 an anchor count bound to a LOCAL and then compared is still an assertion",
        asserts_anchor_count(
            "occurrences = text.count(old)\nif occurrences != 1:\n    pass\n"),
    )
    check(
        "P-31 a local compared against 1 that came from something OTHER than a count is not one",
        not asserts_anchor_count("occurrences = len(x)\nif occurrences != 1:\n    pass\n"),
    )
    check(
        "P-31 a baseline named in CODE counts",
        measures_baseline("baseline = run()\n"),
    )
    check(
        "P-31 a baseline named only in a COMMENT does not count",
        not measures_baseline("# we should measure a baseline one day\nx = 1\n"),
    )
    check(
        "P-31 a baseline named only in a STRING does not count",
        not measures_baseline('DOC = "measures a baseline"\n'),
    )

    # --- wiring ---
    check(
        "P-31 a harness named in build.gradle.kts is wired there",
        wired_in("h.py", 'commandLine("$projectDir/tools/h.py")', "") == ["build.gradle.kts"],
    )
    check(
        "P-31 a harness named in neither file is wired nowhere",
        wired_in("h.py", "", "") == [],
    )

    # --- the declared table is not allowed to become a census that stopped ---
    check(
        "P-31 a file named `*-mutation-test.py` is discovered as a harness",
        discovers_harnesses({"X-mutation-test.py": "x = 1\n"}) == ["X-mutation-test.py"],
    )
    check(
        "P-31 a harness named otherwise is discovered by its ALL-CAPS mutation table",
        discovers_harnesses({"test-x.py": "BASE_MUTATIONS = []\n"}) == ["test-x.py"],
    )
    check(
        "P-31 an EMITTER that merely reports a mutation table is not a harness",
        discovers_harnesses({"X-emit-result.py": "def _mutations():\n    return []\n"}) == [],
    )
    check(
        "P-31 every mutation harness in this tree is DECLARED in the table above",
        undeclared_harnesses() == [],
    )
    # Found by the build: `tools/T-280-mutation-test.py` had never been invoked as a command, so
    # its executable bit had never mattered, and wiring it into Gradle's `commandLine` -- which
    # execs the path directly, as every other checker in `build.gradle.kts` does -- failed with
    # `A problem occurred starting process`.  A build premise that is a file MODE is exactly the
    # kind nothing was checking.
    check(
        "P-31 every declared harness is executable, because the build execs its path directly",
        [basename for basename, _kind, _shape, _subjects in HARNESSES
         if not os.access(os.path.join(HERE, basename), os.X_OK)] == [],
    )

    # --- the historical demonstration: the gate must catch the defect it was written for ---
    # `9620d3e` is `P-30`'s own commit and the one the wired Gradle task went red at.  A gate that
    # cannot report the instance that motivated it is an argument, not an instrument.  The tree is
    # materialised with `git archive`, so this asserts against the COMMITTED past and not against
    # anything in the working tree.
    _archived = _archive("9620d3e")
    if _archived:
        _rows = census(_archived, strict=False)
        _orphaned = {row["harness"]: row["anchorsUnresolved"] for row in _rows}
        check(
            "P-31 the census reports FIVE orphaned anchors in the harness that went red at "
            "P-30's own commit — the same five that harness itself printed as ANCHOR rows",
            _orphaned.get("test-check-queue-vocabulary.py") == 5,
        )
        check(
            "P-31 and no OTHER harness was orphaned at that commit",
            sum(count for harness, count in _orphaned.items()
                if harness != "test-check-queue-vocabulary.py") == 0,
        )

    # --- the tree itself: this is the gate, asserted as a named test ---
    rows = census()
    check(
        "P-31 every declared harness resolves every anchor it declares, in this tree",
        sum(row["anchorsUnresolved"] for row in rows) == 0,
    )
    check(
        "P-31 every declared harness resolves every symbol it names, in this tree",
        sum(row["symbolsUnresolved"] for row in rows) == 0,
    )
    check(
        "P-31 every declared TEXT-ANCHOR harness declares at least one anchor",
        all(row["anchorsDeclared"] > 0 for row in rows if row["kind"] == "TEXT-ANCHOR"),
    )
    check(
        "P-31 every declared ATTRIBUTE or REIMPLEMENTATION harness names at least one symbol",
        all(row["symbolsDeclared"] > 0 for row in rows if row["kind"] != "TEXT-ANCHOR"),
    )

    for failure in failures:
        print("SELFTEST FAIL: {}".format(failure))
    print("# {} self-test(s), {} failure(s)".format(len(ran), len(failures)))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description="the mutation-harness census (P-31)")
    parser.add_argument("--tree", default=ROOT)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--self-test", "--selftest", dest="selftest", action="store_true")
    args = parser.parse_args(argv)

    if args.selftest:
        return _selftest()

    # A tree other than this one is a HISTORICAL tree, where a harness that had not been
    # written yet is absent rather than missing.
    rows = census(args.tree, strict=os.path.abspath(args.tree) == ROOT)
    if args.json:
        print(json.dumps([{k: v for k, v in row.items() if not k.startswith("_")}
                          for row in rows], indent=2))
        return 0

    print("{:<34} {:<16} {:>7} {:>7} {:>4} {:>4}  {}".format(
        "harness", "kind", "anchors", "symbols", "cnt", "base", "wired in"))
    for row in rows:
        print("{:<34} {:<16} {:>3}/{:<3} {:>3}/{:<3} {:>4} {:>4}  {}".format(
            row["harness"], row["kind"],
            row["anchorsDeclared"] - row["anchorsUnresolved"], row["anchorsDeclared"],
            row["symbolsDeclared"] - row["symbolsUnresolved"], row["symbolsDeclared"],
            "yes" if row["assertsAnchorCount"]
            else ("n/a" if row["anchorsDeclared"] == 0 else "NO"),
            "yes" if row["measuresBaseline"] else "NO",
            ", ".join(row["wiredIn"]) or "NOWHERE — runs only when somebody remembers"))

    defects = 0
    for row in rows:
        for harness, name, target, occurrences in row["_anchorDefects"]:
            print("ORPHAN  {}  anchor of {!r} occurs {} times in {}, expected 1".format(
                harness, name[:60], occurrences, target))
            defects += 1
        for harness, receiver, attribute, target in row["_symbolDefects"]:
            print("ORPHAN  {}  names {}.{}, which {} does not define".format(
                harness, receiver, attribute, target))
            defects += 1

    undeclared = undeclared_harnesses(args.tree)
    for basename in undeclared:
        print("UNDECLARED  {}  is a mutation harness and is in no row of HARNESSES: declare it, "
              "with its adapter shape, or the census cannot see it".format(basename))
        defects += 1

    unwired = [row["harness"] for row in rows if not row["wiredIn"]]
    print("# {} harness(es); {} anchor(s) and {} symbol(s) into their subjects; {} unresolved"
          .format(len(rows), sum(r["anchorsDeclared"] for r in rows),
                  sum(r["symbolsDeclared"] for r in rows), defects))
    print("# wired: {} of {}{}".format(
        len(rows) - len(unwired), len(rows),
        "" if not unwired else "; unwired: " + ", ".join(unwired)))
    return 1 if (defects and args.check) else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
