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
#
# `rows` is what the harness PRINTS, one shape name per row kind, and it is the interface
# `tools/T-295-mutation-input-census.py` reads: that census runs every harness in two arms and
# classifies each mutation from the harness's own per-mutation rows.  It used to try all of its
# patterns against every line, first match wins -- so a harness that changed to ANOTHER harness's
# shape was read silently with different semantics, and three collisions in two iterations each
# came of the format being guessed rather than declared (`T-306`).  The names are the census's
# own; `BY_HAND` is the sentinel for a harness that does not run bare at all.
BY_HAND = "BY-HAND"
HARNESSES = (
    ("test-check-queue-vocabulary.py", "TEXT-ANCHOR", "name_file_old_new",
     "check-queue-vocabulary.py + queue_verdicts.py", ("killed-by", "survives")),
    ("P-30-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "queue_verdicts.py + trace-answers.py + check-queue-vocabulary.py",
     ("killed-pair", "survived")),
    ("T-281-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "census_discharges.py + T-234-census.py", ("killed-pair", "survived")),
    ("T-283-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "queue_verdicts.py + check-queue-vocabulary.py", ("killed-pair", "survived")),
    ("T-289-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "queue_verdicts.py + check-queue-vocabulary.py", ("killed-pair", "survived")),
    ("T-292-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "T-292-column-repair.py + check-queue-vocabulary.py", ("killed-n", "survived")),
    ("T-295-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "T-295-mutation-input-census.py", ("killed-by", "survived")),
    ("T-234-mutation-test.py", "TEXT-ANCHOR", "kind_name_path_subs",
     "T-234-census.py + T-234-emit-classification.py", ("kind-row",)),
    ("T-280-mutation-test.py", "TEXT-ANCHOR", "kind_name_subs",
     "T-234-census.py", ("kind-row",)),
    ("T-278-mutation-test.py", "ATTRIBUTE", "attributes",
     "T-278-emitter-rounding-census.py + T-278-rounding-simulation.py",
     ("killed-by", "survived")),
    ("T-225-mutation-test.py", "ATTRIBUTE", "attributes",
     "check-result-file-hygiene.py", ("of-row",)),
    ("T-249-mutation-test.py", "REIMPLEMENTATION", "attributes",
     "check-result-file-hygiene.py", ("arrow",)),
    ("T-250-mutation-test.py", "REIMPLEMENTATION", "attributes",
     "check-result-file-hygiene.py", ("arrow",)),
    ("T-297-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "src/main/kotlin/tile/CrossoverCommonMode.kt + src/main/kotlin/tile/HoneycombGrillage.kt",
     (BY_HAND,)),
    ("T-298-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new", "trace-answers.py",
     ("killed-by", "survives")),
    ("T-306-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "P-31-harness-census.py + T-295-mutation-input-census.py", ("killed-by", "survived")),
    # The corpus's SECOND harness over a Kotlin subject, moved into `tools/` by `T-305`.  Five
    # fields per row where the Python harnesses have three or four, hence its own adapter; and
    # `BY_HAND`, because one mutation is one Gradle `test` run and it takes a snapshot directory.
    ("T-299-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/HoneycombRasterTurnTethers.kt + "
     "src/main/kotlin/tile/HoneycombGrillage.kt", (BY_HAND,)),
    # The THIRD Kotlin-subject harness, moved into `tools/` by `T-308`.  Same five-field row
    # shape and the same `BY_HAND` sentinel as `T-299`'s, and for the same two reasons: one
    # mutation is one Gradle `test` run, and it takes a snapshot directory because it mutates a
    # source a shared checkout must not have edited under it.
    ("T-304-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/RasterTurnAnchorAzimuth.kt", (BY_HAND,)),
    # The FOURTH, written in the iteration it is declared in -- which is what `T-305` and `T-308`
    # cost an iteration each for want of.
    ("T-307-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/UniformRasterTetherSpans.kt", (BY_HAND,)),
    # `T-310`.  Two Kotlin subjects, and the second is the SHARED lattice: a mutation of
    # `HoneycombGrillage`'s new per-bond branch is the only evidence that its `null` default is
    # still the object four claims measured.
    ("T-310-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/CrossoverLinkResolution.kt", (BY_HAND,)),
    # `T-309`.  Written during `T-303` and declared two rows later than its number suggests: the
    # registry belonged to another agent that iteration, so the harness was retained in
    # `gpd/data/` and moved here by the queue item the move was owed to.  The ordinals in the
    # comments above count DECLARATION order, which is why this one is last and not third.
    ("T-303-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/CrossoverLinkStiffness.kt", (BY_HAND,)),
    # `T-315`.  The subject is the ONE builder and the ONE census `T-315` adds; the per-bond
    # link itself is `T-310`'s subject and is mutation-tested there, so this row holds open the
    # two things a re-grade owns -- that its defaults are the standing lattice's bit for bit,
    # and that the `116 bp` block's own bond census does not transfer to a shorter row.
    ("T-315-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/ResolvedLinkUniformRaster.kt", (BY_HAND,)),
    # `T-313`.  A Python subject with an INLINE `--selftest`, so the harness runs the subject
    # itself rather than a sibling test file -- and its fixture is bigger than a `tools/`-only
    # copy, because eleven of that self-test's assertions read the corpus.
    ("T-313-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new", "check-corpus-links.py",
     ("killed-by", "survives")),
    # `T-316`.  One subject and no shared source: the task edits none, and the per-bond link its
    # census is read at is `T-310`'s subject and is mutation-tested there.  What these rows hold
    # open is that the smoothed search's bank is the same object the grading reads, that the
    # percentile objective is the order statistic it says it is and is rounded at the decision
    # precision, and that the record's fields report the quantities they are named for.
    ("T-316-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/SearchedDistribution.kt", (BY_HAND,)),
    # `T-322`.  One subject and no shared source, like `T-316`'s.  What these rows hold open is
    # the three things a coupled census read on the WRONG tile silently inherits -- the station
    # ladder and the phase rule that picks a placement on it, the transferred ratio band that is
    # the cheap bound, and the reader that takes the uncoupled reference out of `C-0211`'s
    # committed cells at the WORST of its twelve chain corners -- plus `CH-0272`'s conjunction.
    ("T-322-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/RouteBCoupled.kt", (BY_HAND,)),
    # `T-323`.  One subject and no shared source, like `T-316`'s -- and `countPhaseSplit` is
    # REUSED rather than copied, so its arithmetic is `T-178`'s subject and only the one line
    # that maps this task's two factors onto it is mutated here.  What these rows hold open is
    # that the placement family is the PRODUCT of its row option sets and enumerates in the order
    # its tie-break is written on, that the bank SLICE is the placement it names, that every
    # search decision is taken at six significant digits with a key tie-break, and that the
    # 2 x 2's two orderings are the two orderings they are labelled as.  `T-328`/`T-329`
    # (`C-0217`) re-anchored two rows onto the ONE function the decision rule now lives in and
    # added nine, six for the comparator and the argmin every selection site now calls and three
    # for the identity report that replaced two printable ulp residuals.
    ("T-323-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/JointPlacementDistribution.kt", (BY_HAND,)),
    # `T-294`.  One subject and no shared source: the task edits none, and the per-bond link its
    # resolved arm is graded at is `T-310`'s subject and is mutation-tested there.  What these
    # rows hold open is the FOUR censuses that do not transfer between the two 60-helix
    # cross-sections -- the bond split, the tie split, the enhancement's two silent argument
    # mistakes and the normalising stroke -- plus the least-squares face basis that the first
    # block with an ODD raster-row count made necessary, and `C-0104`'s trap in the surrogate.
    ("T-294-mutation-test.py", "TEXT-ANCHOR", "id_file_old_new_what",
     "src/main/kotlin/tile/CrossSectionTiedRegrade.kt", (BY_HAND,)),
    # `T-321`.  A Python subject with an INLINE `--self-test`, like `T-313`'s, so the harness runs
    # the subject itself rather than a sibling test file.  Its fixture is a `tools/`-only copy and
    # that is a DECLARATION rather than a convenience: the probe's self-tests read `tools/` and
    # their own `TemporaryDirectory` and nothing else, which is why they stay green in
    # `T-295`'s emptied arm.
    ("T-321-mutation-test.py", "TEXT-ANCHOR", "name_file_old_new",
     "T-321-dynamic-guard-probe.py", ("killed-by", "survives")),
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


def declared_row_shapes(basename):
    """The printed row shapes a harness declares, from `HARNESSES`.

    A mutation harness's printed OUTPUT is an interface: `tools/T-295-mutation-input-census.py`
    runs every harness in two arms and classifies each mutation from the harness's own rows.  It
    read them by trying every one of its eight patterns against every line, first match wins --
    so a harness that changed to ANOTHER harness's shape was read silently with different
    semantics, and three collisions in two iterations each came of the format being guessed.
    """
    for row in HARNESSES:
        if row[0] == basename:
            return tuple(row[4])
    return ()


def census_row_shape_names():
    """The row-shape names `T-295`'s census knows, LOADED from it rather than copied.

    A duplicated rule is invisible to a mutation test of either copy (`CLAUDE.md`), and this is
    the one place the two tools have to agree about a vocabulary.  Loaded lazily and by file spec,
    so there is no module-level cycle: the census loads this table the same way.
    """
    import importlib.util
    spec = importlib.util.spec_from_file_location(
        "p31census", os.path.join(HERE, "T-295-mutation-input-census.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return {name for name, _pattern, _shape in module.ROW_SHAPES}


def strip_kotlin_comments(text):
    """`text` with `//` and `/* */` comments blanked, length-preservingly.

    Length-preserving because the spans below are found by index and a shortened source would
    move every one of them -- `CLAUDE.md`'s own note about blanking a census's matches.
    """
    out, i, n = [], 0, len(text)
    while i < n:
        two = text[i:i + 2]
        if two == "//":
            end = text.find("\n", i)
            end = n if end < 0 else end
            out.append(" " * (end - i))
            i = end
        elif two == "/*":
            end = text.find("*/", i + 2)
            end = n if end < 0 else end + 2
            out.append("".join(c if c == "\n" else " " for c in text[i:end]))
            i = end
        elif text[i] == '"':
            end = i + 1
            while end < n and text[end] != '"':
                end += 2 if text[end] == "\\" else 1
            end = min(end + 1, n)
            out.append(text[i:end])
            i = end
        else:
            out.append(text[i])
            i += 1
    return "".join(out)


def command_line_spans(build_text):
    """The argument text of every `commandLine(...)` call, comments already blanked.

    A DESCRIPTION is prose on an executable line, so comment-stripping alone does not reach it:
    `description = "Runs tools/h.py, ..."` names a harness and runs nothing.  What makes an
    occurrence a USE is the call it sits in, and the span is balanced rather than line-based
    because a `commandLine` may be written over several lines.
    """
    text = strip_kotlin_comments(build_text)
    spans, start = [], text.find("commandLine(")
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
        spans.append(text[start:i])
        start = text.find("commandLine(", i)
    return spans


def strip_shell_comments(line):
    """One shell line with an unquoted `#` and everything after it removed."""
    quote = None
    for index, character in enumerate(line):
        if quote:
            if character == quote:
                quote = None
        elif character in "'\"":
            quote = character
        elif character == "#":
            return line[:index]
    return line


def shell_command_words(verify_text):
    """The first word of every comment-stripped line of a shell script.

    Only the command word counts.  `echo "skip with: tools/h.py"` names a harness in an ARGUMENT,
    and `tools/verify.sh`'s own header names half the checkers in prose; neither runs anything.
    """
    words = []
    for line in verify_text.splitlines():
        stripped = strip_shell_comments(line).strip()
        if not stripped:
            continue
        word = stripped.split()[0].strip("\"'")
        if word:
            words.append(word)
    return words


def wired_in(basename, build_text, verify_text):
    """Where a harness is RUN from, of `build.gradle.kts` and `tools/verify.sh`.

    A USE, never a MENTION (`T-301`).  The predicate was `basename in build_text or basename in
    verify_text`, and the census went `13 of 14` -> `14 of 14` -> `13 of 14` on the wording of one
    comment with no wiring changed -- so a harness could be reported wired because somebody
    explained why it is not, and `build.gradle.kts` duly carried a note saying that a basename was
    being withheld from a comment for that reason.  A predicate that forces the prose around it to
    be written defensively is measuring the prose.
    """
    places = []
    if any(basename in span for span in command_line_spans(build_text)):
        places.append("build.gradle.kts")
    if any(word.endswith(basename) for word in shell_command_words(verify_text)):
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
    elif shape == "id_file_old_new_what":
        # `(id, file, anchor, replacement, what it breaks)`.  A Kotlin harness names what each
        # mutation BREAKS as a fifth field, because its rows cannot be read off a Python
        # predicate's own vocabulary the way `name_file_old_new`'s can.
        for ident, filename, old, _new, _what in module.MUTATIONS:
            rows.append((harness, ident,
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

    for basename, kind, shape, subjects, _rows in HARNESSES:
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
            "rowShapes": list(declared_row_shapes(basename)),
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

    # --- wiring: a USE, never a MENTION (`T-301`) ---
    #
    # `wired_in` was `basename in build_text or basename in verify_text`, and the census duly went
    # `13 of 14` -> `14 of 14` -> `13 of 14` on the WORDING OF ONE COMMENT with no wiring changed.
    # Live at the commit this repairs: `tools/T-283-mutation-test.py` was reported wired in
    # `tools/verify.sh` off a sentence in a comment there, and `build.gradle.kts` carried a note
    # explaining that `T-297`'s basename is deliberately NOT spelled, because the substring test
    # would read the explanation as the wiring.  A checker whose predicate forces the prose around
    # it to be written defensively is not a checker.
    check(
        "P-31 a harness named in build.gradle.kts is wired there",
        wired_in("h.py", 'commandLine("$projectDir/tools/h.py")', "") == ["build.gradle.kts"],
    )
    check(
        "P-31 a harness named in neither file is wired nowhere",
        wired_in("h.py", "", "") == [],
    )
    check(
        "P-31 a harness named only in a `//` COMMENT of the build script is NOT wired — the "
        "instance this predicate was repaired for",
        wired_in("h.py", "        // the h.py harness takes an argument and stays by hand\n", "")
        == [],
    )
    # CONSTRUCTED (`C-0161`), and it is what makes the blanking load-bearing: without a
    # `commandLine(` INSIDE the comment there is no span to find, so a predicate that never
    # blanked a comment would pass the row above.  A commented-out wiring is exactly the shape a
    # build script accumulates -- the wiring that was tried, failed and was left in place.
    check(
        "P-31 a COMMENTED-OUT commandLine is not a wiring: the `//` blanking is what makes the "
        "span search see a comment at all",
        wired_in("h.py", '        // commandLine("$projectDir/tools/h.py")\n', "") == [],
    )
    check(
        "P-31 a harness named only in a `/* */` block comment is not wired either",
        wired_in("h.py", "/*\n * h.py runs by hand.\n */\n", "") == [],
    )
    check(
        "P-31 and a commandLine inside a `/* */` BLOCK comment — a whole wiring block left in "
        "the file — is not a wiring either",
        wired_in("h.py", '/*\n * commandLine("$projectDir/tools/h.py")\n */\n', "") == [],
    )
    check(
        "P-31 a harness named only in a DESCRIPTION string is not wired: a description is prose "
        "on an executable line, which comment-stripping alone cannot reach",
        wired_in("h.py", '    description = "Runs tools/h.py, the mutation test"\n', "") == [],
    )
    check(
        "P-31 a `commandLine` carrying an ARGUMENT after the path is still a use",
        wired_in("h.py", 'commandLine("$projectDir/tools/h.py", snapshot)', "")
        == ["build.gradle.kts"],
    )
    check(
        "P-31 a commandLine SPANNING lines is a use — the span is balanced, not line-based",
        wired_in("h.py", 'commandLine(\n    "$projectDir/tools/h.py",\n    "--check",\n)', "")
        == ["build.gradle.kts"],
    )
    check(
        "P-31 a shell line whose COMMAND WORD is the harness is a use",
        wired_in("h.py", "", "    tools/h.py --check\n") == ["tools/verify.sh"],
    )
    check(
        "P-31 a shell command word carrying a `$root` prefix and quotes is a use too",
        wired_in("h.py", "", '    "$root/tools/h.py" --self-test\n') == ["tools/verify.sh"],
    )
    check(
        "P-31 a harness named in a `#` COMMENT of the shell script is not wired",
        wired_in("h.py", "", "# repairing the queue took tools/h.py from 0 survivors to 1\n")
        == [],
    )
    # CONSTRUCTED: a comment whose `#` abuts the path, so the command word IS the path unless the
    # comment is stripped.  The row above passes at any stripping, because `#` is then the first
    # word; this one is the only shape the stripping is load-bearing on -- and it is the shape a
    # commented-out invocation actually takes.
    check(
        "P-31 a COMMENTED-OUT shell invocation whose `#` abuts the path is not wired: the "
        "comment stripping is what the command word rule rests on",
        wired_in("h.py", "", "    #tools/h.py --check\n") == [],
    )
    check(
        "P-31 a harness named in an ECHO rather than run is not wired — the argument position "
        "is prose, and only the command word is a use",
        wired_in("h.py", "", '    echo "skip with: tools/h.py --no-checks"\n') == [],
    )
    check(
        "P-31 wiring in BOTH files is reported as both",
        wired_in("h.py", 'commandLine("tools/h.py")', "    tools/h.py\n")
        == ["build.gradle.kts", "tools/verify.sh"],
    )
    # The corpus's own reading, asserted rather than described: at the commit this lands on,
    # `tools/verify.sh` mentions exactly one harness and runs none of them.
    check(
        "P-31 in THIS tree no mutation harness is run from tools/verify.sh, though one is "
        "named in a comment there — which the old substring predicate read as wiring",
        [row["harness"] for row in census() if "tools/verify.sh" in row["wiredIn"]] == [],
    )
    # THE INSTANCE THIS PREDICATE WAS REPAIRED FOR, against the COMMITTED past rather than a
    # fixture.  `342d7ad` is iteration 47's assembled HEAD, where `tools/verify.sh` names
    # `T-283-mutation-test.py` in a comment explaining a defect and runs no harness at all -- so
    # the substring predicate reported it wired there.  Pinned, because a self-test that reads a
    # mutable artifact expires the moment the defect it asserts is repaired (`CLAUDE.md`), and
    # skipped visibly where git cannot supply the file.
    _old_verify = subprocess.run(
        ["git", "-C", ROOT, "show", "342d7ad:tools/verify.sh"], capture_output=True, text=True)
    if _old_verify.returncode == 0 and _old_verify.stdout:
        check(
            "P-31 HISTORICAL: at 342d7ad tools/verify.sh NAMES T-283-mutation-test.py and RUNS "
            "no harness, so the substring predicate reported a comment as a wiring and this one "
            "does not",
            "T-283-mutation-test.py" in _old_verify.stdout
            and wired_in("T-283-mutation-test.py", "", _old_verify.stdout) == [],
        )
    else:
        print("# the historical wiring check at 342d7ad was SKIPPED: no git repository at {}"
              .format(ROOT), file=sys.stderr)

    # --- the declared PRINTED ROW SHAPE, which is the interface a sibling census reads ---
    check(
        "P-31 every declared harness declares the printed row shape(s) it emits, because a "
        "census that GUESSES a harness's output format reads a changed one silently",
        [row[0] for row in HARNESSES if not declared_row_shapes(row[0])] == [],
    )
    check(
        "P-31 every declared row shape is a name T-295's census knows, LOADED from it rather "
        "than copied — a duplicated vocabulary is invisible to a mutation test of either copy",
        [(row[0], name) for row in HARNESSES for name in declared_row_shapes(row[0])
         if name != BY_HAND and name not in census_row_shape_names()] == [],
    )
    check(
        "P-31 BY-HAND is a SENTINEL and not a row shape: a harness that declares it declares "
        "nothing else, because it prints no row this census can pair",
        [row[0] for row in HARNESSES
         if BY_HAND in declared_row_shapes(row[0]) and len(declared_row_shapes(row[0])) != 1]
        == [],
    )
    check(
        "P-31 a harness's declared shapes are reported on its census row, so the JSON carries "
        "the contract the other census reads",
        all(tuple(row["rowShapes"]) == declared_row_shapes(row["harness"]) for row in census()),
    )
    # The row above compares the census against the LOOKUP, so it passes even if the lookup
    # answers every question with the first row of the table.  This one compares the lookup
    # against the TABLE, which is the premise the derivation rests on (`CLAUDE.md`: assert the
    # premise, never the derivation's own output).
    check(
        "P-31 declared_row_shapes answers for the harness it is ASKED about, row by row against "
        "the table itself",
        all(declared_row_shapes(row[0]) == tuple(row[4]) for row in HARNESSES),
    )
    check(
        "P-31 and two harnesses declaring different shapes get different answers, so a lookup "
        "that ignored its argument could not pass",
        len({declared_row_shapes(row[0]) for row in HARNESSES}) > 1,
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
        [basename for basename, _kind, _shape, _subjects, _rows in HARNESSES
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

    # The THIRD state (`T-301`, `C-0182`): a harness that takes a snapshot directory because it
    # mutates Kotlin.  It is wired -- on its own Gradle task, by name -- and it is deliberately
    # out of `:test`, because one of its mutations is one Gradle `test` run.  A report with two
    # states could only call that a defect.
    by_hand = [row["harness"] for row in rows if BY_HAND in row["rowShapes"]]
    if by_hand:
        print("# BY HAND, and not a defect: {} — each takes a snapshot directory and is kept out "
              "of `:test`; run it by name with -PmutationSnapshot=<dir>".format(
                  ", ".join(by_hand)))

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
