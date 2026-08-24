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
# T-295 -- EVERY MUTATION'S DISCRIMINATING INPUT, CLASSIFIED: FIXTURE OR COMMITTED ARTIFACT.
#
#     tools/T-295-mutation-input-census.py               the census, human-readable
#     tools/T-295-mutation-input-census.py --check       exit 1 on an undeclared corpus dependency
#     tools/T-295-mutation-input-census.py --json        the census as JSON, for the emitter
#     tools/T-295-mutation-input-census.py --self-test   the named tests
#     tools/T-295-mutation-input-census.py --tree DIR    census a checkout other than this one
#
# WHY THIS EXISTS.  `P-31` asks whether a harness's reference INTO ITS SUBJECT still resolves.
# This asks the other half: whether the INPUT THAT KILLS a mutation is a fixture the test
# constructs, or a committed artifact the test happens to read.  The second kind expires when the
# corpus is repaired -- silently, on a correct predicate, and `C-0192` §8 is the instance:
# repairing 21 queue rows (`T-292`) took `tools/T-283-mutation-test.py` from 0 survivors to 1,
# because the committed `TASKS.md` was the only input on which a row's LEFTMOST verdict and its
# LAST verdict were different objects.  `P-31` resolves anchors and could not have seen it: the
# anchor resolved perfectly, and what had gone was the DISCRIMINATOR.
#
# THE MEASUREMENT IS A PAIRED EXPERIMENT, NOT A STATIC READING.  Two copies of the tree are made:
# a CONTROL, faithful, and a TREATMENT in which every committed artifact outside `tools/` and the
# build infrastructure is emptied.  Every harness runs in both, unmodified, and its own printed
# per-mutation row is read.  Every harness already SUBTRACTS the failures of an unmutated baseline
# (`CH-0237`), so a named test that cannot run against an emptied corpus is subtracted in the
# treatment arm rather than counted -- which is exactly the behaviour wanted: a test that cannot
# discriminate on an emptied corpus cannot kill anything there.
#
#   killed in control AND in treatment   FIXTURE  -- some killer survives the corpus being emptied
#   killed in control, NOT in treatment  CORPUS   -- its only discriminator is committed state
#   not killed in control                SURVIVOR -- the harness's own gate owns this one
#   killed only in treatment             REVIVED  -- reported; nothing should produce it
#
# WHY EMPTYING AND NOT SOME OTHER CORPUS.  Emptying is the MAXIMAL perturbation, so a mutation
# still killed under it is killed by something that is not the corpus.  The residual risk is a
# false NEGATIVE -- a corpus-reading test that still discriminates on an empty corpus -- and it is
# bounded by the reconstruction test below, which is the one instance the class is known to have.
#
# NO STATIC CALL GRAPH.  `CLAUDE.md`: *a static call graph over FILES is not a conservative
# approximation, it is noise*.  Deciding "does this named test read `TASKS.md`" by reading source
# would need a cross-module closure over eight tools; the experiment answers the question the
# census is actually asking, which is not *does it read one* but *does it NEED one*.
#
# WHAT IS DECLARED AND WHAT IS DERIVED.  The harness list is `P-31`'s -- imported, not copied, so
# a harness written tomorrow fails `P-31`'s own discovery gate rather than being invisible here.
# The build-infrastructure carve-out is declared.  The row shapes are declared as regular
# expressions and CROSS-CHECKED against each harness's own summary line, so a harness that
# changes its output makes the census refuse rather than under-count.  Everything else is run.

import argparse
import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

# --- what counts as the corpus ------------------------------------------------------------------
#
# Everything in the checkout EXCEPT these is a committed artifact for the purposes of this census:
# data the harnesses' subjects read.  `tools/` is the subject itself and the harnesses copy it;
# `gradle/` and the wrapper files are how a run starts at all.  The carve-out is declared because
# it is a judgement, and it is small enough to read.
KEEP_DIRECTORIES = ("tools", "gradle")
KEEP_FILES = ("build.gradle.kts", "settings.gradle.kts", "gradle.properties",
              "gradlew", "gradlew.bat")
# Never copied into either tree: build output and version control.  `.git` is excluded from BOTH
# arms, so no harness can tell the two apart by it.
SKIP_DIRECTORIES = (".git", "build", ".gradle", ".idea", ".kotlin", "__pycache__")

# --- the declared exemptions ---------------------------------------------------------------------
#
# A mutation whose only discriminator is committed corpus state, RECORDED BY NAME so that its
# expiry is loud rather than silent.  The key is `(harness, mutation label)` -- the label and not
# the index, because a table gains rows and an index is a dated object (`C-0176`).  A declaration
# is checked in BOTH directions: an undeclared CORPUS row fails the gate, and a declared row that
# is not corpus-dependent fails it too, because a stale exemption is how a registry rots.
CORPUS_DEPENDENT_BY_DESIGN = {}


# --- reading a harness's own per-mutation rows ----------------------------------------------------
#
# Six shapes, because these harnesses were written by different tasks against different subjects.
# Each yields (label, number of named tests that failed); a survivor is a row whose count is zero,
# in every shape, which is what makes one classifier serve all twelve.
ROW_SHAPES = (
    # `killed by 4  named test(s)  NAME` -- T-278, test-check-queue-vocabulary
    ("killed-by", re.compile(r"^killed by\s+(\d+)\s+named test\(s\)\s+(.*)$"), "count-first"),
    # `killed  gate  6  reader  0   NAME` -- P-30, T-281, T-283, T-289 (two suites, summed)
    ("killed-pair", re.compile(r"^killed\s+\w+\s+(\d+)\s+\w+\s+(\d+)\s+(.*)$"), "pair"),
    # `killed  12   NAME` -- T-292
    ("killed-n", re.compile(r"^killed\s+(\d+)\s+(.*)$"), "count-first"),
    ("survived", re.compile(r"^SURVIVED\s+(.*)$"), "zero"),
    ("survives", re.compile(r"^SURVIVES\s+(.*?)\s+no named test failed\s*$"), "zero"),
    # `NARROW  the rule removed        fails  8  killer; killer` -- T-234, T-280
    ("kind-row", re.compile(r"^(NARROW|WIDEN)\s+(.*?)\s+fails\s+(\d+)\s+(.*)$"), "kind"),
    # `    2 named test(s) fail  <-  NAME` -- T-249, T-250
    ("arrow", re.compile(r"^\s+(\d+) named test\(s\) fail\s+<-\s+(.*)$"), "count-first"),
    # `  narrowed back to ...        9 of 31 fail` -- T-225
    ("of-row", re.compile(r"^\s{2}(\S.*?)\s{2,}(\d+) of \d+ fail\s*$"), "label-first"),
)

# A harness's OWN count of its rows, in the two shapes that print one.  Parsed rather than
# transcribed, and asserted against the number of rows this census parsed: a harness that changes
# its output must make the census REFUSE, not silently drop rows.
SUMMARY_COUNT = re.compile(r"^#\s*(\d+) mutation\(s\)", re.MULTILINE)
SUMMARY_COVERAGE = re.compile(r"coverage[^\n]*?, (\d+) mutations", re.MULTILINE)
SUMMARY_RETIRED = re.compile(r"(\d+) retired")


#: The shapes that print ONLY for a survivor.  Derived from `ROW_SHAPES`'s own third field rather
#: than listed, because a second list is invisible to a mutation test of either copy.  A survivor
#: shape is CONTINGENT: a harness declares it and prints it only when something survives, and this
#: corpus has none -- so its absence is not a stale declaration and must not be gated as one.
SURVIVOR_ONLY_SHAPES = {name for name, _pattern, kind in ROW_SHAPES if kind == "zero"}

#: `P-31`'s sentinel for a harness that does not run bare at all.  Spelled here so that this
#: census can recognise it in a declaration; the declaration itself lives in `P-31`'s table.
BY_HAND = "BY-HAND"


def parse_rows(output, shapes=None):
    """[(label, named tests failed)] for every per-mutation row a harness printed.

    `shapes` is the harness's OWN declared row shapes, from `P-31`'s table.  Given, only those are
    tried -- so a harness that changes to ANOTHER harness's shape reads as nothing and refuses,
    rather than being read silently under different semantics (`T-306`).  Omitted, every shape is
    tried, which is what the self-tests of the individual shapes want.
    """
    rows = []
    for line in output.splitlines():
        for _name, pattern, shape in ROW_SHAPES:
            if shapes is not None and _name not in shapes:
                continue
            match = pattern.match(line)
            if not match:
                continue
            if shape == "pair":
                rows.append((match.group(3).strip(),
                             int(match.group(1)) + int(match.group(2))))
            elif shape == "count-first":
                rows.append((match.group(2).strip(), int(match.group(1))))
            elif shape == "label-first":
                rows.append((match.group(1).strip(), int(match.group(2))))
            elif shape == "kind":
                rows.append((match.group(1) + " " + match.group(2).strip(),
                             int(match.group(3))))
            else:
                rows.append((match.group(1).strip(), 0))
            break
    return rows


def matched_shapes(output, shapes=None):
    """The set of row-shape names that matched at least one line of `output`."""
    found = set()
    for line in output.splitlines():
        for name, pattern, _kind in ROW_SHAPES:
            if shapes is not None and name not in shapes:
                continue
            if pattern.match(line):
                found.add(name)
                break
    return found


def undeclared_shape_rows(output, shapes):
    """How many lines this census CAN read but the harness did not declare a shape for."""
    return len(parse_rows(output)) - len(parse_rows(output, shapes))


def shape_refusals(output, shapes):
    """[refusal] for a harness whose printed output disagrees with its declared row shapes.

    Both directions, which is the whole point of a declaration.  A row in an UNDECLARED shape is
    the collision this task exists for; a DECLARED shape that never prints is a stale declaration
    -- except a survivor shape, which is contingent by construction and derived as such.
    """
    if BY_HAND in shapes:
        return []
    refusals = []
    undeclared = undeclared_shape_rows(output, shapes)
    if undeclared:
        refusals.append(
            "printed %d row(s) in a shape it does not declare: %s. A harness's printed output is "
            "an interface (T-306); declare the shape in P-31's HARNESSES table, or print a "
            "declared one" % (undeclared,
                              ", ".join(sorted(matched_shapes(output) - set(shapes))) or "?"))
    for name in sorted(set(shapes) - matched_shapes(output, shapes)):
        if name in SURVIVOR_ONLY_SHAPES:
            continue
        refusals.append(
            "declares the row shape %r and printed none: a declaration that has stopped being "
            "true is how a registry rots (C-0182)" % name)
    return refusals


def treat_as_by_hand(declared, derived):
    """True when a harness is BY HAND and there is nothing for this census to reconcile.

    A conjunction, deliberately.  Either half alone would let the census skip a harness it should
    be reading: the DECLARATION alone would skip one whose table has gone stale, and the
    DERIVATION alone would skip one that printed a usage line for some other reason.  Pulled out
    of `census` so that a mutation of it fails a named test -- `census` itself runs every harness
    in two arms and cannot be a fixture.
    """
    return declared and derived


#: A kind prefix a harness prepends to its own mutation name.  `C-0176`'s NARROW/WIDEN direction
#: is part of the ROW and not part of the name, so it comes off before the comparison below.
KIND_PREFIXES = ("NARROW ", "WIDEN ")


def label_refusals(labels, names):
    """[refusal] for a parsed row label that is not one of the harness's own mutation names.

    THE FOURTH COLLISION (`T-306`).  `tools/T-298-mutation-test.py` printed its killers on the
    same line as the name, and the `killed-by` shape captures everything after the count -- so the
    label was `name + padding + killers`, the killers differ once the corpus is emptied, and the
    census reported *row labels drift* and refused the harness.  That row format was iteration
    47's own repair of the THIRD collision: a harness moved to a declared shape, and the move
    introduced this.

    The comparison is a PREFIX, not an equality, because a harness pads its name into a column and
    may truncate it there; what a prefix cannot tolerate is anything printed AFTER the name, which
    is exactly the defect.  It runs on the CONTROL ARM ALONE, so it catches the class at authoring
    time rather than when two arms happen to disagree -- and the names come from `P-31`'s own
    adapter, which already reads every harness's mutation table.  A harness whose adapter supplies
    no names (an `attributes` shape, a `BY-HAND` one) is not checked.
    """
    if not names:
        return []
    bad = []
    for label in labels:
        # BOTH readings, because a kind prefix is ambiguous with a name that opens with the same
        # word: four of `T-289`'s own mutations are called *"NARROW the status column back to
        # …"*, so stripping unconditionally would refuse a harness for naming its rows after the
        # direction they go in.  Either reading matching is enough -- the check is a necessary
        # condition on the label, not a parse of it.
        candidates = [label]
        for prefix in KIND_PREFIXES:
            if label.startswith(prefix):
                candidates.append(label[len(prefix):])
        if not any(text and any(name.startswith(text) for name in names)
                   for text in candidates):
            bad.append(label)
    if not bad:
        return []
    return ["%d row label(s) are not one of this harness's own mutation names, the first being "
            "%r: a row must carry its name and nothing after it, or the label picks up whatever "
            "else the line holds and drifts between the two arms (T-306)"
            % (len(bad), bad[0][:120])]


def drift_refusal(control_label, treatment_label):
    """The refusal for two arms whose row labels disagree, SHOWING where they disagree.

    The message used to print both labels truncated to 40 characters, and the one instance the
    corpus has produced was identical for the first 40 -- so the report printed two strings that
    look the same and said they differ.  A report whose truncation hides the thing it is
    reporting is `C-0177`'s *a gate that cannot fail*, read on the OUTPUT instead of the exit code.
    """
    position = 0
    while (position < len(control_label) and position < len(treatment_label)
           and control_label[position] == treatment_label[position]):
        position += 1
    return ("row labels differ at character %d: control %r against treatment %r"
            % (position + 1, control_label[position:position + 60],
               treatment_label[position:position + 60]))


def by_hand_refusals(declared, derived):
    """[refusal] where `P-31`'s BY-HAND declaration and this census's derivation disagree.

    `C-0182`'s registry, checked in both directions.  The DERIVED reading (`T-301`) is the
    harness's own usage line, which cannot go stale; the DECLARED one is `P-31`'s table, which
    can.  Keeping both and asserting they agree is what makes the staleness loud instead of
    turning the derivation off.
    """
    if declared and not derived:
        return ["is declared BY-HAND in P-31's table and RAN: the declaration is stale, or the "
                "harness has stopped taking an argument"]
    if derived and not declared:
        return ["printed its own usage line, so it takes an argument, and P-31's table does not "
                "declare it BY-HAND"]
    return []


def declared_count(output):
    """The harness's own row count, or None where its summary does not print one."""
    summary = SUMMARY_COUNT.search(output)
    coverage = SUMMARY_COVERAGE.search(output)
    if summary is None and coverage is None:
        return None
    total = int(summary.group(1)) if summary else int(coverage.group(1))
    retired = SUMMARY_RETIRED.search(output)
    # A RETIRED row is one the harness declines to run, so it prints no verdict line for it.
    return total - (int(retired.group(1)) if retired else 0)


#: A harness that REFUSES to run without an argument prints its usage and exits non-zero.  That is
#: a THIRD state, and it has to be one: this census's two states can only report such a harness as
#: a defect, which is `C-0182`'s finding — *a report needs a third state, and confusing `VACUOUS`
#: with `UNDECLARED` hands somebody a verdict they were never given*.
#:
#: DERIVED and not declared, deliberately.  A declared list of by-hand harnesses is a dated object
#: (`C-0176`); the harness's **own** usage line cannot go stale, and it is already in the control
#: arm's output because the census ran it.  `tools/T-297-mutation-test.py` is the first such
#: harness in this corpus: it mutates **Kotlin** sources, so it takes a snapshot directory rather
#: than editing a shared checkout, and `T-301` carries the same question asked of `P-31`'s
#: `wired_in`.
BY_HAND_USAGE = re.compile(r"^usage:", re.MULTILINE)


def takes_an_argument(control_output):
    """Did the harness decline to run at all, by printing its own usage line?"""
    return bool(BY_HAND_USAGE.search(control_output))


def reconcile(control_rows, treatment_rows, stated):
    """([mutation record], [refusal]) for one harness's two arms.

    Separated from the run so that the RECONCILIATION -- the three ways a pair of readings can
    fail to be comparable at all -- is a pure function with fixtures of its own.  A census that
    silently drops rows is worse than one that refuses: `C-0177`'s *a gate that cannot fail*, met
    on the reading rather than on the exit code.
    """
    refusals = []
    if not control_rows:
        refusals.append("the control run printed no per-mutation row this census can read")
    if len(control_rows) != len(treatment_rows):
        refusals.append("control printed %d rows and treatment %d"
                        % (len(control_rows), len(treatment_rows)))
    if stated is None:
        # `T-306`.  Without a stated count a PARTIAL shape change drops the same rows from both
        # arms, the two lengths agree, and nothing above can see it -- measured live at three of
        # the fourteen harnesses that run bare.  The count is the only cross-check that is not a
        # comparison of the census with itself.
        refusals.append("the harness states no count of its own, so a partial shape change would "
                        "drop the same rows from both arms silently: print a "
                        "`# N mutation(s), M survivor(s)` summary line")
    elif stated != len(control_rows):
        refusals.append("the harness states %d mutations and this census read %d rows"
                        % (stated, len(control_rows)))
    if refusals:
        return [], refusals
    mutations = []
    for (label, control_kills), (other, treatment_kills) in zip(control_rows, treatment_rows):
        if label != other:
            return [], [drift_refusal(label, other)]
        mutations.append({
            "label": label,
            "controlKillers": control_kills,
            "treatmentKillers": treatment_kills,
            "verdict": verdict(control_kills, treatment_kills),
        })
    return mutations, []


def exit_code(defect_count, checking):
    """1 only when a defect was found AND this run is a gate.

    `C-0177`: read the return the PROCESS exits on, not the one the docstring describes -- and
    return a boolean decision rather than a count, because `sys.exit(n)` truncates modulo 256.
    """
    return 1 if (defect_count and checking) else 0


def verdict(control_kills, treatment_kills):
    """FIXTURE / CORPUS / SURVIVOR / REVIVED for one mutation, from its two readings.

    The whole classification, in one place, so that a mutation of it fails a named test.
    """
    if control_kills > 0 and treatment_kills > 0:
        return "FIXTURE"
    if control_kills > 0:
        return "CORPUS"
    if treatment_kills > 0:
        return "REVIVED"
    return "SURVIVOR"


# --- the two trees --------------------------------------------------------------------------------

def is_corpus_path(relative):
    """True when a path relative to the checkout root is a committed ARTIFACT, not the subject."""
    parts = relative.split(os.sep)
    if parts[0] in KEEP_DIRECTORIES:
        return False
    if len(parts) == 1 and parts[0] in KEEP_FILES:
        return False
    return True


def neutralised_content(basename):
    """What an emptied corpus file holds.

    A `.json` file becomes `{}` and not the empty string, because a reader that raises on a
    truncated file makes its whole suite fail identically in the treatment arm -- which the
    baseline subtraction would then hide, and every mutation of that harness would read CORPUS.
    """
    return "{}\n" if basename.endswith(".json") else ""


def build_tree(destination, neutralise, source=ROOT):
    """A copy of `source` at `destination`; with every corpus file emptied when `neutralise`.

    Written as one walk rather than copy-then-overwrite: the treatment tree never holds the
    99 MB of `gpd/data/` it is about to throw away.
    """
    emptied = 0
    for directory, subdirectories, files in os.walk(source):
        subdirectories[:] = [name for name in subdirectories
                             if name not in SKIP_DIRECTORIES and not name.startswith("build-")]
        relative = os.path.relpath(directory, source)
        target = destination if relative == "." else os.path.join(destination, relative)
        os.makedirs(target, exist_ok=True)
        for name in files:
            source_path = os.path.join(directory, name)
            if os.path.islink(source_path):
                continue
            target_path = os.path.join(target, name)
            key = name if relative == "." else os.path.join(relative, name)
            if neutralise and is_corpus_path(key):
                with open(target_path, "w", encoding="utf-8") as handle:
                    handle.write(neutralised_content(name))
                emptied += 1
            else:
                shutil.copy2(source_path, target_path)
    return emptied


def run_harness(tree, basename, arguments=()):
    """Whatever one harness printed, run inside `tree` so its own ROOT is that tree."""
    result = subprocess.run(
        [sys.executable, os.path.join(tree, "tools", basename)] + list(arguments),
        cwd=tree, capture_output=True, text=True, timeout=3600)
    return result.stdout + result.stderr


# --- the census -------------------------------------------------------------------------------

def _p31():
    spec = importlib.util.spec_from_file_location(
        "t295p31", os.path.join(HERE, "P-31-harness-census.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def harness_names(tree=ROOT):
    """The harnesses to census: `P-31`'s declared table, imported rather than copied.

    `P-31` already gates *every mutation harness in `tools/` is declared*, so this census inherits
    that guarantee instead of keeping a second list that could fall behind it -- `CLAUDE.md`'s own
    *a duplicated rule is invisible to a mutation test of either copy*.  An UNDECLARED harness is
    therefore `P-31`'s defect and not this census's; `main` says so where one exists, rather than
    censusing a file `P-31` has already refused.
    """
    del tree
    return [row[0] for row in _p31().HARNESSES]


def harness_row_shapes(basename):
    """The row shapes `P-31`'s table declares for one harness, imported rather than copied."""
    return _p31().declared_row_shapes(basename)


def harness_mutation_names(basename, tree=ROOT):
    """The mutation names `P-31`'s adapter reads out of one harness's own table, or ().

    Empty for an `attributes` shape (the harness mutates by reassigning a name, so there is no
    table of rows to read) and for a `BY-HAND` one (this census never reads its rows).  Those are
    the harnesses `label_refusals` cannot check, and it says so by checking nothing.
    """
    p31 = _p31()
    row = next((r for r in p31.HARNESSES if r[0] == basename), None)
    if row is None or row[2] == "attributes" or p31.BY_HAND in row[4]:
        return ()
    tools = os.path.join(tree, "tools")
    return tuple({r[1] for r in p31._adapt(row[2], p31._import(tools, basename), basename)})


def census(tree=ROOT, only=None, progress=None):
    """One row per mutation of every harness, with its control and treatment readings."""
    control = tempfile.mkdtemp(prefix="T-295-control.")
    treatment = tempfile.mkdtemp(prefix="T-295-treatment.")
    try:
        build_tree(control, neutralise=False, source=tree)
        emptied = build_tree(treatment, neutralise=True, source=tree)
        rows = []
        for basename in harness_names(tree):
            if only and basename not in only:
                continue
            if progress:
                progress(basename)
            shapes = harness_row_shapes(basename)
            control_output = run_harness(control, basename)
            treatment_output = run_harness(treatment, basename)
            stated = declared_count(control_output)
            # The third state, DERIVED from the harness's own usage line (`T-301`) and
            # CROSS-CHECKED against `P-31`'s declaration (`T-306`): the derivation cannot go
            # stale and the declaration can, so keeping both and asserting they agree is what
            # makes a stale declaration loud rather than turning the derivation off.
            derived = takes_an_argument(control_output) and not parse_rows(control_output)
            declared = BY_HAND in shapes
            hand = by_hand_refusals(declared, derived)
            if treat_as_by_hand(declared, derived) and not hand:
                rows.append({
                    "harness": basename,
                    "rowShapes": list(shapes),
                    "statedMutations": stated,
                    "mutations": [],
                    "refusals": [],
                    "byHand": ("the harness takes an argument and printed its own usage line, so it "
                               "is run by hand and this census has nothing to reconcile"),
                })
                continue
            control_rows = parse_rows(control_output, shapes)
            mutations, refusals = reconcile(
                control_rows, parse_rows(treatment_output, shapes), stated)
            rows.append({
                "harness": basename,
                "rowShapes": list(shapes),
                "statedMutations": stated,
                "mutations": mutations,
                "refusals": (hand + shape_refusals(control_output, shapes)
                             + label_refusals([label for label, _ in control_rows],
                                              harness_mutation_names(basename, tree))
                             + refusals),
            })
        return {"harnesses": rows, "corpusFilesEmptied": emptied}
    finally:
        shutil.rmtree(control, ignore_errors=True)
        shutil.rmtree(treatment, ignore_errors=True)


def defects(reading):
    """[(kind, harness, label, detail)] -- everything `--check` fails on."""
    found = []
    declared = dict(CORPUS_DEPENDENT_BY_DESIGN)
    seen = set()
    for row in reading["harnesses"]:
        for refusal in row["refusals"]:
            found.append(("REFUSED", row["harness"], "", refusal))
        for mutation in row["mutations"]:
            key = (row["harness"], mutation["label"])
            if mutation["verdict"] == "CORPUS":
                seen.add(key)
                if key not in declared:
                    found.append((
                        "CORPUS", row["harness"], mutation["label"],
                        "killed by %d named test(s) in the control and by NONE once the corpus is "
                        "emptied: its only discriminator is committed state, so a repair of that "
                        "state expires it silently. Construct the fixture (C-0161), or declare it "
                        "in CORPUS_DEPENDENT_BY_DESIGN with a reason"
                        % mutation["controlKillers"]))
            elif mutation["verdict"] == "SURVIVOR":
                found.append(("SURVIVOR", row["harness"], mutation["label"],
                              "fails no named test in either arm; the harness's own gate owns "
                              "this, and until it is repaired the mutation cannot be classified"))
            elif mutation["verdict"] == "REVIVED":
                found.append(("REVIVED", row["harness"], mutation["label"],
                              "killed only once the corpus is emptied, which nothing should "
                              "produce: the treatment arm is not a subset of the control"))
    for key in declared:
        if key not in seen:
            found.append(("STALE", key[0], key[1],
                          "declared corpus-dependent and it is not: %s. A declaration that has "
                          "stopped being true is how a registry rots (C-0182)" % declared[key]))
    return found


# --- the reconstruction: the one instance the class is known to have --------------------------
#
# `C-0192` §8 in a scratch tree, run rather than remembered.  `T-283`'s twelfth mutation flips the
# residue arm from a row's LEFTMOST verdict to its LAST.  Three states, and the census must give a
# different verdict at each:
#
#   the fixture kept, a REPAIRED queue        FIXTURE   -- `C-0192` constructed the two-verdict row
#   the fixture REMOVED, a repaired queue     SURVIVOR  -- the state `C-0192` §8 found
#   the fixture removed, a PRE-REPAIR queue   CORPUS    -- what this census exists for
#
# THE THREE QUEUES ARE CONSTRUCTED, NOT READ.  `C-0161`, applied to this census's own tests: a
# reconstruction that read the live `TASKS.md` would give a different answer inside this census's
# own treatment arm -- which is exactly the defect under study, one level up, and the first run of
# `tools/T-295-mutation-test.py` inside `--check` duly reported two of its own rows as survivors.
# The pre-repair queue's shape is `C-0188`'s: a verdict in the `Leaf` cell and the priority note
# in the status cell, which is what all 21 repaired rows looked like.
#
# THE HISTORICAL CHECK IS SEPARATE AND IS PINNED.  `historical_verdict` runs the third state
# against `git show 7f7957d:TASKS.md` -- the real pre-repair queue -- and `CLAUDE.md`'s *pin the
# ref* is why it is a SHA and not `HEAD`: a self-test that reads a mutable artifact expires the
# moment the defect it asserts is repaired.  It needs a git repository, so it is asserted only
# where one is available, and every mutation of this block is held open by the constructed states.
RECONSTRUCTION_REF = "7f7957d"
RECONSTRUCTION_HARNESS = "T-283-mutation-test.py"
RECONSTRUCTION_LABEL = "leftmost"
FIXTURE_BLOCK_START = (
    '    check(\n        "T-292 the residue reads a row\'s LEFTMOST verdict, on a CONSTRUCTED '
    'two-verdict row",')
FIXTURE_BLOCK_END = "    # The blanking is the GATE's scan and not the READER's."

# `C-0188`'s own shape: the live verdict opens the `Leaf` cell and the preserved priority note
# opens the status cell, so the row carries TWO verdicts and `verdicts[0]` and `verdicts[-1]`
# differ.  Twenty-one rows of the committed queue looked like this until `T-292`.
QUEUE_BEFORE_REPAIR = (
    "| ID | Task | Acceptance | Leaf | Status |\n"
    "|---|---|---|---|---|\n"
    "| T-1 | a | b | **DONE** (iteration 3) | TODO — **HIGH** |\n"
)
# The same row after the column repair: one live verdict, so the two readings coincide.
QUEUE_AFTER_REPAIR = (
    "| ID | Task | Acceptance | Leaf | Status |\n"
    "|---|---|---|---|---|\n"
    "| T-1 | a | b | — | **DONE** (iteration 3) |\n"
)

# One row of the harness's table, selected by NAME, so a reordering of that table is loud.
DRIVER = '''import importlib.util, os, sys
here = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("h", os.path.join(here, "%s"))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
rows = [row for row in module.MUTATIONS if "%s" in row[0]]
assert len(rows) == 1, "the reconstruction\'s target mutation is not uniquely named"
module.MUTATIONS = rows
sys.exit(module.main([]))
''' % (RECONSTRUCTION_HARNESS, RECONSTRUCTION_LABEL)


def strip_fixture(gate_source):
    """`check-queue-vocabulary.py` without the two named tests `C-0192` added.

    Anchored at both ends and asserted to occur exactly once, so the excision cannot silently
    no-op -- which is `P-31`'s own subject, met inside a test of it.
    """
    if gate_source.count(FIXTURE_BLOCK_START) != 1 or gate_source.count(FIXTURE_BLOCK_END) != 1:
        raise AssertionError("the reconstruction's fixture-block anchors do not resolve")
    start, end = gate_source.find(FIXTURE_BLOCK_START), gate_source.find(FIXTURE_BLOCK_END)
    if start >= end:
        raise AssertionError("the reconstruction's fixture-block anchors are out of order")
    return gate_source[:start] + gate_source[end:]


def _reconstruction_tree(queue_text, strip):
    directory = tempfile.mkdtemp(prefix="T-295-reconstruction.")
    tools = os.path.join(directory, "tools")
    os.makedirs(tools)
    for name in os.listdir(HERE):
        path = os.path.join(HERE, name)
        if name.endswith(".py") and os.path.isfile(path):
            shutil.copy2(path, os.path.join(tools, name))
    with open(os.path.join(directory, "TASKS.md"), "w", encoding="utf-8") as handle:
        handle.write(queue_text)
    if strip:
        gate = os.path.join(tools, "check-queue-vocabulary.py")
        with open(gate, encoding="utf-8") as handle:
            source = handle.read()
        with open(gate, "w", encoding="utf-8") as handle:
            handle.write(strip_fixture(source))
    with open(os.path.join(tools, "T-295-reconstruction-driver.py"), "w",
              encoding="utf-8") as handle:
        handle.write(DRIVER)
    return directory


def state_verdict(queue_text, strip):
    """This census's verdict for `T-283`'s leftmost-verdict mutation, in a tree built from
    `queue_text` with `C-0192`'s constructed fixture optionally excised.

    Measured by running the harness's own machinery over exactly one of its rows, in both arms,
    and classifying the pair with the census's own `verdict` -- so the demonstration exercises the
    instrument rather than a paraphrase of it.
    """
    control = _reconstruction_tree(queue_text, strip)
    treatment = _reconstruction_tree("", strip)
    try:
        control_rows = parse_rows(run_harness(control, "T-295-reconstruction-driver.py"))
        treatment_rows = parse_rows(run_harness(treatment, "T-295-reconstruction-driver.py"))
        if len(control_rows) != 1 or len(treatment_rows) != 1:
            raise AssertionError("the reconstruction driver did not run exactly one mutation")
        return verdict(control_rows[0][1], treatment_rows[0][1])
    finally:
        shutil.rmtree(control, ignore_errors=True)
        shutil.rmtree(treatment, ignore_errors=True)


def reconstruction():
    """{state: verdict} for the three constructed states above, MEASURED by running each."""
    return {
        "fixtureKeptQueueAfterRepair": state_verdict(QUEUE_AFTER_REPAIR, strip=False),
        "fixtureRemovedQueueAfterRepair": state_verdict(QUEUE_AFTER_REPAIR, strip=True),
        "fixtureRemovedQueueBeforeRepair": state_verdict(QUEUE_BEFORE_REPAIR, strip=True),
    }


def historical_verdict(repository=ROOT):
    """The third state against the REAL pre-repair queue, or None where git cannot supply it."""
    older = subprocess.run(["git", "-C", repository, "show", "%s:TASKS.md" % RECONSTRUCTION_REF],
                           capture_output=True, text=True)
    if older.returncode != 0 or not older.stdout:
        return None
    return state_verdict(older.stdout, strip=True)


# --- the self-tests ----------------------------------------------------------------------------

def _queue_verdicts(queue_text):
    """The verdicts of the single task row of a constructed queue, through the gate's own reader.

    Used only to assert the two fixtures above are the two shapes they are named for; a fixture
    whose premise is not asserted is a fixture that can rot into agreeing with itself.
    """
    verdicts_module = importlib.util.spec_from_file_location(
        "t295verdicts", os.path.join(HERE, "queue_verdicts.py"))
    module = importlib.util.module_from_spec(verdicts_module)
    verdicts_module.loader.exec_module(module)
    rows = module.task_rows(queue_text)
    if len(rows) != 1:
        raise AssertionError("a constructed queue fixture must hold exactly one task row")
    return module.row_verdicts(rows[0][1])


def _selftest(fast=False, repository=ROOT):
    failures, ran = [], []

    def check(name, condition):
        ran.append(name)
        if not condition:
            failures.append(name)

    # --- one classifier over six output shapes; a survivor is a zero in every one of them ---
    check(
        "T-295 the `killed by N named test(s)` shape is read, at either spacing",
        parse_rows("killed by 4  named test(s)  a widened predicate\n"
                   "killed by 1 named test(s)  a narrowed one")
        == [("a widened predicate", 4), ("a narrowed one", 1)],
    )
    check(
        "T-295 the two-suite `killed  gate N  reader M` shape SUMS its two counts",
        parse_rows("killed  gate  6  reader  2   the blanking is a no-op")
        == [("the blanking is a no-op", 8)],
    )
    check(
        "T-295 the single-count `killed  N` shape is read",
        parse_rows("killed   3   the leaf derivation removed") == [("the leaf derivation removed", 3)],
    )
    check(
        "T-295 a SURVIVED row is a zero, which is what makes one classifier serve every shape",
        parse_rows("SURVIVED                    the rule nothing holds open")
        == [("the rule nothing holds open", 0)],
    )
    check(
        "T-295 a SURVIVES row loses its `no named test failed` tail and its padding",
        parse_rows("SURVIVES  the whole-set mutation" + " " * 30 + " no named test failed")
        == [("the whole-set mutation", 0)],
    )
    check(
        "T-295 the NARROW/WIDEN `fails N` shape keeps its direction in the label",
        parse_rows("NARROW  the window shrunk to nothing        fails 11  T-260 a; T-260 b")
        == [("NARROW the window shrunk to nothing", 11)],
    )
    check(
        "T-295 the `N named test(s) fail  <-  NAME` shape is read",
        parse_rows("    2 named test(s) fail  <-  threshold widened to six digits")
        == [("threshold widened to six digits", 2)],
    )
    check(
        "T-295 the `NAME  N of M fail` shape is read, label first",
        parse_rows("  drop     relativeSpread                  1 of 31 fail")
        == [("drop     relativeSpread", 1)],
    )
    check(
        "T-295 a harness's own SUMMARY line is not a mutation row, and an unindented "
        "`N of M fail` is not one either — only a harness's own indented table row is",
        parse_rows("# 12 mutation(s), 0 survivor(s)\n"
                   "baseline: 0 of 31 GATE_TESTS fail\n"
                   "stats over the corpus 3 of 31 fail") == [],
    )
    check(
        "T-295 an indented KILLER line under a row is not itself a row",
        parse_rows("            SELFTEST FAIL: T-283 a double-backticked span is blanked too")
        == [],
    )

    # --- T-306: the shapes are DECLARED per harness, not guessed over all of them -----------
    #
    # `parse_rows` tried all eight patterns against every line, first match wins, so a harness
    # that changed to ANOTHER harness's shape was read silently with different semantics.  Three
    # collisions in two iterations came of the format being guessed; the declaration lives in
    # `P-31`'s own table, which is already gated against becoming a census that stopped.
    check(
        "T-306 a harness parsed with its OWN declared shape reads its rows",
        parse_rows("killed   3   the leaf derivation removed", ("killed-n",))
        == [("the leaf derivation removed", 3)],
    )
    check(
        "T-306 and the SAME line parsed under a different harness's declared shape reads NOTHING "
        "— which is what makes a changed output a refusal instead of a silent reading",
        parse_rows("killed   3   the leaf derivation removed", ("killed-by",)) == [],
    )
    check(
        "T-306 an undeclared shape is COUNTED, so the refusal can name what happened rather "
        "than saying the harness printed no row",
        undeclared_shape_rows("killed   3   a row\nkilled by 1 named test(s)  another",
                              ("killed-n",)) == 1,
    )
    check(
        "T-306 a harness printing only its declared shapes has no undeclared rows",
        undeclared_shape_rows("killed   3   a row", ("killed-n",)) == 0,
    )
    check(
        "T-306 a row in an undeclared shape is a REFUSAL",
        shape_refusals("killed by 1 named test(s)  a row", ("killed-n",)) != [],
    )
    check(
        "T-306 a DECLARED shape that the harness never printed is a refusal too — a declaration "
        "that has stopped being true is how a registry rots (C-0182)",
        shape_refusals("killed   3   a row", ("killed-n", "kind-row")) != [],
    )
    check(
        "T-306 but a SURVIVOR shape is CONTINGENT and its absence is not a defect: it prints "
        "only when a mutation survives, and this corpus has none",
        shape_refusals("killed   3   a row", ("killed-n", "survived")) == [],
    )
    check(
        "T-306 the survivor shapes are DERIVED from the census's own table — the `zero` kind — "
        "and not a second list that could fall behind it",
        SURVIVOR_ONLY_SHAPES == {"survived", "survives"},
    )
    check(
        "T-306 a BY-HAND declaration is not a row shape and asks nothing of the output",
        shape_refusals("usage: tools/h.py <snapshot-dir>", (BY_HAND,)) == [],
    )

    # --- T-306, the FOURTH collision: the label a row carries must be the MUTATION'S NAME -----
    #
    # `tools/T-298-mutation-test.py` printed `killed by N named test(s)  <name padded>  <killers>`
    # and the `killed-by` shape captures everything after the count -- so the label was the name
    # PLUS the names of the tests that failed, which differ between the two arms.  The census duly
    # reported *row labels drift*, refused the harness, and printed `0` in a count column, which
    # reads exactly like a harness with nothing to measure.  That row format was iteration 47's
    # own repair of collision (3): the harness was moved to a declared shape and the move
    # introduced this.  The check below would have caught it in the CONTROL ARM ALONE, at
    # authoring time, with no drift needed -- the label is compared against the harness's own
    # mutation table, which `P-31`'s adapter already reads.
    check(
        "T-306 a label that IS the mutation's own name is clean",
        label_refusals(["the rule removed"], ("the rule removed", "another")) == [],
    )
    check(
        "T-306 a TRUNCATED label is clean, because a harness pads its name into a column",
        label_refusals(["the rule remo"], ("the rule removed",)) == [],
    )
    check(
        "T-306 a NARROW/WIDEN kind prefix is not part of the name and is stripped before the "
        "comparison",
        label_refusals(["WIDEN the rule removed"], ("the rule removed",)) == [],
    )
    check(
        "T-306 a mutation NAME that itself opens with NARROW is not mangled by the kind-prefix "
        "stripping — four of T-289's own rows are named for the direction they go in, and the "
        "census refused that whole harness on its first real run",
        label_refusals(["NARROW the status column back to the LAST cell"],
                       ("NARROW the status column back to the LAST cell",)) == [],
    )
    check(
        "T-306 a label carrying the KILLERS after the name is refused — the fourth collision, "
        "caught in the control arm alone with no drift needed",
        label_refusals(["the rule removed        trace-answers a; trace-answers b"],
                       ("the rule removed",)) != [],
    )
    check(
        "T-306 an EMPTY label is refused rather than matching every name by prefix",
        label_refusals([""], ("the rule removed",)) != [],
    )
    check(
        "T-306 a harness whose shape supplies no mutation names is not checked, because there "
        "is nothing to check it against",
        label_refusals(["anything at all"], ()) == [],
    )

    # --- T-306: a drift report must SHOW the difference it is reporting -----------------------
    check(
        "T-306 a drift refusal names the position the two labels first differ at, because both "
        "were truncated to 40 characters and printed identical",
        "differ at character 41" in drift_refusal("a" * 40 + "X", "a" * 40 + "Y"),
    )
    check(
        "T-306 and it carries the two TAILS from that position, not the two identical heads",
        "'X'" in drift_refusal("a" * 40 + "X", "a" * 40 + "Y")
        and "'Y'" in drift_refusal("a" * 40 + "X", "a" * 40 + "Y"),
    )

    # --- the harness's own count, parsed rather than transcribed ---
    check(
        "T-295 a `# N mutation(s)` summary states the count",
        declared_count("# 12 mutation(s), 0 survivor(s)") == 12,
    )
    check(
        "T-295 a RETIRED row prints no verdict line, so it is subtracted from the stated count",
        declared_count("# 17 mutation(s) (6 base + 11 per-classification), 2 retired, 0 "
                       "survivor(s)") == 15,
    )
    check(
        "T-295 a `coverage, N mutations` summary states the count too",
        declared_count("-- T-234 mutation coverage, 56 mutations over 193 named tests --") == 56,
    )
    check(
        "T-295 a harness that states no count is None rather than zero",
        declared_count("-- T-249 mutation coverage of --prose, over 35 named tests --") is None,
    )

    # --- the classification itself ---
    check(
        "T-295 killed in BOTH arms is FIXTURE — a killer survives the corpus being emptied",
        verdict(3, 1) == "FIXTURE",
    )
    check(
        "T-295 killed in the control ALONE is CORPUS — this is the whole census",
        verdict(1, 0) == "CORPUS",
    )
    check(
        "T-295 killed in NEITHER arm is a SURVIVOR, which the harness's own gate owns",
        verdict(0, 0) == "SURVIVOR",
    )
    check(
        "T-295 killed only once the corpus is emptied is REVIVED and is reported",
        verdict(0, 2) == "REVIVED",
    )

    # --- what counts as the corpus ---
    check(
        "T-295 a file under tools/ is the SUBJECT, not a committed artifact",
        not is_corpus_path(os.path.join("tools", "check-queue-vocabulary.py")),
    )
    check(
        "T-295 the queue, the deliverables and gpd/ are committed artifacts",
        is_corpus_path("TASKS.md") and is_corpus_path("ANSWERS.md")
        and is_corpus_path(os.path.join("gpd", "results", "T-1.json"))
        and is_corpus_path(os.path.join("src", "main", "kotlin", "A.kt")),
    )
    check(
        "T-295 the build files are infrastructure and are kept in both arms",
        not is_corpus_path("build.gradle.kts")
        and not is_corpus_path(os.path.join("gradle", "libs.versions.toml")),
    )
    check(
        "T-295 a JSON artifact is emptied to `{}` and not to nothing, so its reader still parses",
        neutralised_content("T-1.json") == "{}\n" and neutralised_content("TASKS.md") == "",
    )

    # --- the two trees, built on a synthetic source ---
    source = tempfile.mkdtemp(prefix="T-295-source.")
    treatment = tempfile.mkdtemp(prefix="T-295-built.")
    try:
        os.makedirs(os.path.join(source, "tools"))
        os.makedirs(os.path.join(source, "gpd", "results"))
        os.makedirs(os.path.join(source, "build-X"))
        open(os.path.join(source, "tools", "gate.py"), "w").write("SUBJECT")
        open(os.path.join(source, "TASKS.md"), "w").write("| T-1 | a | **DONE** |")
        open(os.path.join(source, "gpd", "results", "T-1.json"), "w").write('{"a": 1}')
        open(os.path.join(source, "build-X", "junk"), "w").write("junk")
        shutil.rmtree(treatment)
        emptied = build_tree(treatment, neutralise=True, source=source)
        check(
            "T-295 the treatment tree keeps the SUBJECT byte for byte",
            open(os.path.join(treatment, "tools", "gate.py")).read() == "SUBJECT",
        )
        check(
            "T-295 and empties every committed artifact, counting them",
            open(os.path.join(treatment, "TASKS.md")).read() == ""
            and open(os.path.join(treatment, "gpd", "results", "T-1.json")).read() == "{}\n"
            and emptied == 2,
        )
        check(
            "T-295 a build directory is copied into NEITHER arm, so no harness can tell them "
            "apart by it",
            not os.path.exists(os.path.join(treatment, "build-X")),
        )
    finally:
        shutil.rmtree(source, ignore_errors=True)
        shutil.rmtree(treatment, ignore_errors=True)

    # --- the reconciliation: the three ways two arms can fail to be comparable ---
    check(
        "T-295 two arms of equal length and matching labels reconcile into one row each",
        reconcile([("a", 3)], [("a", 1)], 1)
        == ([{"label": "a", "controlKillers": 3, "treatmentKillers": 1, "verdict": "FIXTURE"}], []),
    )
    check(
        "T-295 a harness whose two arms print DIFFERENT row counts is refused, not zipped",
        reconcile([("a", 1), ("b", 1)], [("a", 1)], None)[1] != [],
    )
    check(
        "T-295 a harness that states MORE mutations than this census read is refused — a "
        "harness that changes its output must make the census refuse, never drop rows silently",
        reconcile([("a", 1)], [("a", 1)], 12)[1] != [],
    )
    check(
        "T-295 row labels that DRIFT between the arms are refused: the two readings would "
        "otherwise be paired by position across two different tables",
        reconcile([("a", 1)], [("b", 0)], 1) == ([], [
            "row labels differ at character 1: control 'a' against treatment 'b'"]),
    )
    check(
        "T-295 a harness that printed nothing this census can read is refused — read with a "
        "STATED count of zero, so that only the empty-rows guard can be what refuses it",
        reconcile([], [], 0)[1] != [],
    )
    check(
        "T-306 a harness that states NO count of its own is refused, because a partial shape "
        "change then drops the same rows from both arms and nothing can see it",
        reconcile([("a", 1)], [("a", 1)], None)[1] != [],
    )
    check(
        "T-306 and the hole is real rather than hypothetical: two SHORT arms of equal length "
        "with no stated count reconciled CLEANLY before this",
        reconcile([("a", 1), ("b", 1)], [("a", 1), ("b", 1)], 2)[1] == [],
    )
    check(
        "T-306 a BY-HAND declaration and a harness that RAN disagree, and the declaration is "
        "the stale one",
        by_hand_refusals(declared=True, derived=False) != [],
    )
    check(
        "T-306 a harness that printed a usage line and is NOT declared BY-HAND is a defect in "
        "the other direction — derived and declared are cross-checked, C-0182's registry",
        by_hand_refusals(declared=False, derived=True) != [],
    )
    check(
        "T-306 the by-hand short circuit needs BOTH the declaration and the derivation: either "
        "half alone would let this census skip a harness it should be reading",
        treat_as_by_hand(True, True)
        and not treat_as_by_hand(True, False)
        and not treat_as_by_hand(False, True)
        and not treat_as_by_hand(False, False),
    )
    check(
        "T-306 and the two agreeing, either way, is clean",
        by_hand_refusals(declared=True, derived=True) == []
        and by_hand_refusals(declared=False, derived=False) == [],
    )

    # --- the exit code, which is the wiring ---
    check(
        "T-295 --check exits 1 on a defect and 0 without one",
        exit_code(1, True) == 1 and exit_code(0, True) == 0,
    )
    check(
        "T-295 a plain run reports and never gates, at any defect count",
        exit_code(7, False) == 0,
    )
    check(
        "T-295 the exit decision is a BOOLEAN of the count, so 256 defects do not exit 0",
        exit_code(256, True) == 1,
    )

    # --- the gate, in both directions ---
    def reading(vd, label="a row", harness="H.py", refusals=()):
        return {"harnesses": [{"harness": harness, "statedMutations": 1, "refusals": list(refusals),
                               "mutations": [{"label": label, "controlKillers": 1,
                                              "treatmentKillers": 0, "verdict": vd}]}],
                "corpusFilesEmptied": 0}

    check(
        "T-295 a FIXTURE-backed table is clean",
        defects(reading("FIXTURE")) == [],
    )
    check(
        "T-295 an UNDECLARED corpus dependency is a defect — the gate",
        [d[0] for d in defects(reading("CORPUS"))] == ["CORPUS"],
    )
    check(
        "T-295 a SURVIVOR is a defect too: until the harness is repaired it cannot be classified",
        [d[0] for d in defects(reading("SURVIVOR"))] == ["SURVIVOR"],
    )
    check(
        "T-295 a REVIVED row is reported rather than silently passing",
        [d[0] for d in defects(reading("REVIVED"))] == ["REVIVED"],
    )
    check(
        "T-295 a harness this census cannot READ is a REFUSAL, never a clean row",
        [d[0] for d in defects(reading("FIXTURE", refusals=("printed no row",)))] == ["REFUSED"],
    )

    # --- T-301: the THIRD state, for a harness that takes an argument -------------------------
    #
    # `tools/T-297-mutation-test.py` mutates KOTLIN sources, so it takes a snapshot directory
    # rather than editing a shared checkout, and it prints its usage when run bare.  With two
    # states this census can only call that a REFUSAL, which is a defect, and a gate that cannot
    # come clean is not a gate (`C-0083`).  DERIVED from the harness's own usage line rather than
    # declared, because a declared list is a dated object (`C-0176`).
    check(
        "T-301 a control run that printed a usage line means the harness takes an argument",
        takes_an_argument("usage: tools/T-297-mutation-test.py <snapshot-dir>\n"),
    )
    check(
        "T-301 and a control run that printed rows but no usage line does NOT",
        not takes_an_argument("NARROW  something  fails  2  a test; another\n"),
    )
    check(
        "T-301 the usage line must OPEN a line -- prose mentioning usage is not a refusal to run",
        not takes_an_argument("this harness documents its usage: run it by hand\n"),
    )
    check(
        "T-301 an EMPTY control with no usage line is still a REFUSAL, which is the direction that "
        "must not be widened away",
        [d[0] for d in defects(reading("FIXTURE", refusals=("printed no row",)))] == ["REFUSED"],
    )
    check(
        "T-301 a by-hand harness is not a defect",
        defects({"harnesses": [{"harness": "H.py", "statedMutations": 0, "refusals": [],
                                "mutations": [], "byHand": "takes an argument"}],
                 "corpusFilesEmptied": 0}) == [],
    )
    saved = dict(CORPUS_DEPENDENT_BY_DESIGN)
    try:
        CORPUS_DEPENDENT_BY_DESIGN.clear()
        CORPUS_DEPENDENT_BY_DESIGN[("H.py", "a row")] = "declared for this test"
        check(
            "T-295 a DECLARED corpus dependency is admitted — recorded by name, so its expiry "
            "is loud",
            defects(reading("CORPUS")) == [],
        )
        check(
            "T-295 and a declaration that has STOPPED being true is a defect in the other "
            "direction, because a stale exemption is how a registry rots",
            [d[0] for d in defects(reading("FIXTURE"))] == ["STALE"],
        )
    finally:
        CORPUS_DEPENDENT_BY_DESIGN.clear()
        CORPUS_DEPENDENT_BY_DESIGN.update(saved)
    check(
        "T-295 the shipped registry declares nothing, so every mutation in this tree must be "
        "held open by a fixture",
        CORPUS_DEPENDENT_BY_DESIGN == {},
    )

    # --- the harness list is P-31's, not a second copy of it ---
    check(
        "T-295 the harness list is P-31's own declared table, so a harness written tomorrow "
        "fails P-31's discovery gate rather than being invisible here",
        harness_names() == [row[0] for row in _p31().HARNESSES],
    )
    check(
        "T-295 this census's OWN mutation harness is one of them",
        "T-295-mutation-test.py" in harness_names(),
    )
    check(
        "T-295 harness_names does not READ the tree — an undeclared harness is P-31's defect and "
        "not this census's, so a sibling's in-flight harness cannot make this census refuse a "
        "file P-31 has already refused",
        harness_names(os.path.join(HERE, "no-such-checkout")) == harness_names(),
    )

    # --- the reconstruction: the instrument, against the instance it was written for ---
    check(
        "T-295 the fixture-block excision is anchored at both ends and removes the two named "
        "tests C-0192 added",
        "CONSTRUCTED two-verdict row" not in strip_fixture(
            open(os.path.join(HERE, "check-queue-vocabulary.py"), encoding="utf-8").read()),
    )
    raised = False
    try:
        strip_fixture("nothing here\n")
    except AssertionError:
        raised = True
    check(
        "T-295 and an excision whose anchors do not resolve REFUSES rather than no-opping",
        raised,
    )
    # `C-0179`: two guards for one constraint make a mutation of either a no-op. The ordering
    # guard below already refuses a MISSING anchor, so the count assertion is load-bearing only
    # on a DUPLICATED one -- where an unguarded excision would silently cut the wrong span.
    twice = False
    try:
        strip_fixture(FIXTURE_BLOCK_START + "\na\n" + FIXTURE_BLOCK_START + "\nb\n"
                      + FIXTURE_BLOCK_END + "\n")
    except AssertionError:
        twice = True
    check(
        "T-295 an excision whose anchor occurs TWICE refuses too, which is the only shape the "
        "count assertion is load-bearing on",
        twice,
    )
    check(
        "T-295 the reconstruction's ref is a PINNED commit and not a symbolic name — CLAUDE.md's "
        "own cure for a self-test that reads a mutable artifact",
        re.fullmatch(r"[0-9a-f]{7,40}", RECONSTRUCTION_REF) is not None,
    )
    check(
        "T-295 the PRE-REPAIR queue fixture carries TWO verdicts and the repaired one carries "
        "ONE, which is the whole difference C-0192 removed from the committed file",
        len(_queue_verdicts(QUEUE_BEFORE_REPAIR)) == 2
        and len(_queue_verdicts(QUEUE_AFTER_REPAIR)) == 1,
    )
    states = None if fast else reconstruction()
    if states is not None:
        check(
            "T-295 RECONSTRUCTION: with C-0192's constructed fixture in place the "
            "leftmost-verdict mutation is FIXTURE-backed against a repaired queue",
            states["fixtureKeptQueueAfterRepair"] == "FIXTURE",
        )
        check(
            "T-295 RECONSTRUCTION: remove that fixture and it SURVIVES against a repaired queue "
            "— the state C-0192 §8 found, which the harness's own gate catches",
            states["fixtureRemovedQueueAfterRepair"] == "SURVIVOR",
        )
        check(
            "T-295 RECONSTRUCTION: remove it and put a PRE-REPAIR queue back and the census "
            "reads CORPUS — the committed file is then the only discriminator, which is the "
            "defect this census exists to see and which P-31's anchor resolution cannot",
            states["fixtureRemovedQueueBeforeRepair"] == "CORPUS",
        )
        historical = historical_verdict(repository)
        if historical is None:
            # Visible rather than silent: the constructed states carry the demonstration, and this
            # one adds the REAL pre-repair queue on top of them. A skip that prints nothing is how
            # a test stops being run without anybody noticing.
            # To stderr, so that `tools/verify.sh`'s `--self-test > /dev/null` cannot hide it.
            # A `verify.sh` snapshot carries no `.git`, so this line is expected there and the
            # three CONSTRUCTED states are what carry the demonstration in that run.
            print("# the historical check at {} was SKIPPED: no git repository at {}"
                  .format(RECONSTRUCTION_REF, repository), file=sys.stderr)
        else:
            check(
                "T-295 and the same reading on the REAL pre-repair queue at 7f7957d, so the "
                "instrument is checked against the instance it was written for and not only "
                "against its own fixture",
                historical == "CORPUS",
            )

    for failure in failures:
        print("SELFTEST FAIL: {}".format(failure))
    print("# {} self-test(s), {} failure(s)".format(len(ran), len(failures)))
    return bool(failures)


def main(argv):
    parser = argparse.ArgumentParser(
        description="every mutation's discriminating input, classified (T-295)")
    parser.add_argument("--tree", default=ROOT)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--harness", action="append", default=None)
    parser.add_argument("--self-test", "--selftest", dest="selftest", action="store_true")
    # The reconstruction is six harness runs.  `--fast` omits it, for a mutation table that does
    # not target it; `tools/verify.sh` runs the whole thing.
    parser.add_argument("--fast", action="store_true")
    # Where the reconstruction reads the PRE-REPAIR queue from. Distinct from `--tree`, which is
    # the checkout being censused: a mutated copy of this file has no `.git` of its own.
    parser.add_argument("--repository", default=ROOT)
    args = parser.parse_args(argv)

    if args.selftest:
        return 1 if _selftest(fast=args.fast, repository=args.repository) else 0

    reading = census(args.tree, only=set(args.harness) if args.harness else None,
                     progress=None if args.json else lambda name: print(
                         "  running {} in both arms...".format(name), file=sys.stderr))
    if args.json:
        print(json.dumps(reading, indent=2))
        return 0

    print("{:<34} {:>9} {:>8} {:>7} {:>8}".format(
        "harness", "mutations", "fixture", "corpus", "other"))
    total = {"FIXTURE": 0, "CORPUS": 0, "SURVIVOR": 0, "REVIVED": 0}
    for row in reading["harnesses"]:
        counts = {key: 0 for key in total}
        for mutation in row["mutations"]:
            counts[mutation["verdict"]] += 1
            total[mutation["verdict"]] += 1
        # A REFUSED harness has no readings, and `0` in a COUNT column reads exactly like a
        # harness with nothing to measure -- which is how `T-298`'s ten uncensused mutations
        # printed as a clean row of zeros.  `C-0182`'s third state, met in a table.
        print("{:<34} {:>9} {:>8} {:>7} {:>8}".format(
            row["harness"],
            "REFUSED" if row["refusals"] else len(row["mutations"]),
            "-" if row["refusals"] else counts["FIXTURE"],
            "-" if row["refusals"] else counts["CORPUS"],
            "-" if row["refusals"] else counts["SURVIVOR"] + counts["REVIVED"]))
    by_hand = [row["harness"] for row in reading["harnesses"] if row.get("byHand")]
    if by_hand:
        print("# BY HAND, not censused and NOT a defect: {} — each printed its own usage line, so "
              "it takes an argument and this census has nothing to reconcile (T-301)".format(
                  ", ".join(by_hand)))

    undeclared = _p31().undeclared_harnesses(args.tree)
    if undeclared:
        print("# NOT CENSUSED, because P-31 has already refused them: {} — declare them in "
              "P-31's own table and this census picks them up".format(", ".join(undeclared)))

    found = defects(reading)
    for kind, harness, label, detail in found:
        print("{:<9} {}  {}".format(kind, harness, label[:74]))
        print("          {}".format(detail))

    print("# {} mutation(s) over {} harness(es); {} fixture-backed, {} corpus-dependent, "
          "{} survivor(s), {} revived".format(
              sum(len(row["mutations"]) for row in reading["harnesses"]),
              len(reading["harnesses"]) - len(by_hand), total["FIXTURE"], total["CORPUS"],
              total["SURVIVOR"], total["REVIVED"]))
    print("# {} committed artifact(s) emptied in the treatment arm; {} defect(s)".format(
        reading["corpusFilesEmptied"], len(found)))
    return exit_code(len(found), args.check)


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
