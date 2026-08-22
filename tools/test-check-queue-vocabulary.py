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
# P-29, re-anchored and extended by `P-31` -- the MUTATION test for the queue's vocabulary gate.
#
#     tools/test-check-queue-vocabulary.py
#
# `C-0127`'s standard: a predicate's self-tests are worth nothing unless CHANGING the predicate
# fails a NAMED one.  `CLAUDE.md` adds the direction that is never written -- a predicate carrying
# exclusions has TWO mutation directions, and the widening one is the one nobody tests.  Each row
# below rewrites the subject's source in a throwaway copy of the tree, runs the gate's own
# `--selftest` there, and requires it to FAIL.
#
# `P-31` -- WHY THIS FILE WAS RED FOR A WHOLE ITERATION.  `P-30` (`C-0178`) lifted the shared
# predicate out of `tools/check-queue-vocabulary.py` into `tools/queue_verdicts.py`, and four of
# the six anchors below went looking for text that had moved one file across.  The harness said so
# -- `ANCHOR ... anchor occurs 0 times, expected 1`, which is exactly what an anchor assertion is
# for -- and two subsequent claims excluded the Gradle task rather than reading it, each on the
# ground that the red was *"a concurrent agent's in-flight file"*.  It was not: `git archive
# 9620d3e` reproduces it, and 9620d3e is `P-30`'s own commit.  **A mutation anchor is a reference
# into somebody else's source, and a refactor orphans it silently unless the harness asserts the
# anchor count.**  `tools/P-31-harness-census.py` now makes that a build failure for every harness
# in the tree at once.
#
# `P-31` -- AND THE SIXTH ROW'S `SURVIVES` WAS NOT A SURVIVOR.  This harness copied `tools/` FLAT
# into a scratch directory and copied only `trace-answers.py`, so the mutant could not import
# `queue_verdicts` at all and the gate resolved its queue as `<tmp>/../TASKS.md`, which does not
# exist.  The run died before any named test ran, printed no `SELFTEST FAIL:` line, and was
# reported as a survivor.  That is `CH-0237` in the quiet direction: there a broken fixture made
# every row read *killed*, here it made one read *survived*.  The fixture now reproduces
# `<tmp>/tools/*.py` beside `<tmp>/TASKS.md`, and a BASELINE is measured in an unmutated copy and
# subtracted, so `killed by N` means *this mutation broke something*.
#
# `P-31` -- WHAT THE CORRECTED FIXTURE THEN SHOWED.  The whole-set mutation (row 5) is killed by
# two named tests, so the vocabulary is load-bearing as a SET.  Not one of its ELEVEN members was:
# deleting `"DONE"` from `CLOSING_VERDICTS` failed nothing at all, and so did eight of the other
# ten.  That is `C-0176`'s and `T-225`'s per-classification standard -- *every classification, in
# both directions, must fail a named test when changed on its own* -- reached on the vocabulary
# this gate exists to hold closed.  The per-member rows are DERIVED from the gate's own two sets,
# so a phrase the queue coins tomorrow is mutation-tested the day it is declared; the named tests
# that kill them carry their phrases as LITERALS, because a test generated from the set under test
# disappears with the member it was meant to hold open.

import os
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

VERDICTS = "queue_verdicts.py"
READER = "trace-answers.py"
GATE = "check-queue-vocabulary.py"
# `T-289`.  `queue_verdicts` imports the WIDTH GATE's own cell reader, so that "what is a cell"
# has one definition in this tree; the fixture therefore has to carry it too.  This tuple is a
# DEPENDENCY DECLARATION and a new import edge in the subject invalidates it -- which it did, and
# it said so: `C-0185`'s baseline assertion turned what would have been *12 mutations, 12
# survivors* into `the UNMUTATED copy crashed, so nothing below is a measurement`.
TABLES = "check-markdown-tables.py"

# Every file the gate's `--selftest` touches, at the path it expects to find it at.  A subprocess
# harness must reproduce the tree's own LAYOUT, not merely its contents (`CH-0237`).
FIXTURE_TOOLS = (VERDICTS, READER, GATE, TABLES)
FIXTURE_ROOT_FILES = ("TASKS.md",)


def _declared_phrases():
    """The gate's own vocabulary, in its two senses, read from the gate.

    DERIVED rather than typed, so a phrase the queue coins tomorrow is mutation-tested the day it
    is declared.  `CLAUDE.md`: *"every spelling the corpus uses" is a census that stopped.*
    """
    sys.path.insert(0, HERE)
    gate = __import__("check-queue-vocabulary")
    return (
        [(p, "CLOSING_VERDICTS") for p in sorted(gate.CLOSING_VERDICTS)]
        + [(p, "NOT_CLOSING_VERDICTS") for p in sorted(gate.NOT_CLOSING_VERDICTS)]
    )


# (name, file, text replaced, text it is replaced BY).  The `old` text must occur EXACTLY once in
# that file, which is asserted: a mutation that silently fails to apply is a survivor for the wrong
# reason, and is exactly `CLAUDE.md`'s *a scripted edit that asserts an anchor can no-op*.
#
# Every row REPLACES its rule wholesale.  None widens a rule to `original|mutant`, which is a no-op
# this corpus has measured three times (`C-0176` 9 of 22 rows, `C-0177` 2 of 6).
BASE_MUTATIONS = [
    (
        # RE-ANCHORED by `P-31`: `check-queue-vocabulary.py` -> `queue_verdicts.py`, and the local
        # was renamed `cell` -> `stripped`.  Meaning unchanged.
        "predicate WIDENED: a bold run anywhere in the cell counts as a verdict",
        VERDICTS,
        "bold = _LEADING_BOLD.match(stripped)",
        "bold = _LEADING_BOLD.search(stripped)",
    ),
    (
        # RE-ANCHORED by `P-31`: same text, other file.
        "predicate WIDENED: no word bound, so a bold prose sentence is a verdict",
        VERDICTS,
        "MAX_WORDS = 6",
        "MAX_WORDS = 99",
    ),
    (
        # RE-ANCHORED by `P-31`: the guard was inverted into an early return when it moved.
        # Meaning unchanged -- a verdict must be the WHOLE cell, so `**DONE** (iteration 3)` stops
        # being one.
        "predicate NARROWED: a verdict must be the whole cell",
        VERDICTS,
        "    if len(phrase.split()) > MAX_WORDS or not CLOSING_WORD.search(phrase):\n"
        "        return None\n",
        "    if len(phrase.split()) > MAX_WORDS or not CLOSING_WORD.search(phrase):\n"
        "        return None\n"
        '    if stripped != "**" + phrase + "**":\n'
        "        return None\n",
    ),
    (
        # RE-ANCHORED by `P-31`: same text, other file.
        "strike-through no longer blanked, so a WITHDRAWN verdict is read as live",
        VERDICTS,
        '    return re.sub(r"~~.*?~~", lambda m: " " * len(m.group(0)), text, flags=re.DOTALL)',
        "    return text",
    ),
    (
        # NOT re-anchored: this anchor still resolved.  Its `SURVIVES` was the broken fixture.
        "the vocabulary is opened, so an undeclared coinage passes",
        GATE,
        "known = CLOSING_VERDICTS | NOT_CLOSING_VERDICTS\n"
        "    return [(i, p) for i, p in leading_verdicts(queue_text) if p not in known]",
        "return []",
    ),
    (
        # RE-ANCHORED by `P-31`: `T-283`/`P-30` appended `"TODO"` after `"PARTLY DONE"`, so the
        # anchor is now the set's last member.  Meaning unchanged: this is the edit an author in a
        # hurry would make to silence the gate, and the agreement check refuses it, because
        # `trace-answers.queue_status` still reads the coinage CLOSED.
        "THE LIST EDIT: the coinage declared NOT CLOSING, which the reader contradicts",
        GATE,
        '    "TODO",\n})',
        '    "TODO",\n    "SECOND DELIVERABLE ANSWERED",\n})',
    ),
]

# `P-31` -- RETIRED MUTATIONS.  None.  Every one of `P-29`'s six meanings still exists in the code;
# four moved file and two changed shape, and all six are re-anchored above.  A retirement would be
# recorded here BY NAME with its reason rather than by deletion, so that a shrinking table is a
# statement and not an accident (`C-0071`'s *strike, never delete*, applied to a mutation list).
RETIRED = []


def per_classification_mutations():
    """One mutation per declared vocabulary phrase: delete it from the set it is declared in.

    `T-225`'s per-name standard.  A whole-set mutation can be killed by two tests while every
    individual classification is held open by none, and that is what this gate measured before
    `P-31`: 9 of its 11 members failed nothing when deleted.
    """
    return [
        (
            "CLASSIFICATION dropped from {}: {!r}".format(where, phrase),
            GATE,
            '    "{}",\n'.format(phrase),
            "",
        )
        for phrase, where in _declared_phrases()
    ]


def _fixture():
    """A throwaway copy of the tree, in the LAYOUT the subject expects (`CH-0237`)."""
    directory = tempfile.mkdtemp(prefix="queue-vocab-mutation.")
    os.makedirs(os.path.join(directory, "tools"))
    for name in FIXTURE_TOOLS:
        shutil.copy(os.path.join(HERE, name), os.path.join(directory, "tools", name))
    for name in FIXTURE_ROOT_FILES:
        shutil.copy(os.path.join(ROOT, name), os.path.join(directory, name))
    return directory


def _named_failures(directory):
    """(named self-tests that FAIL in this copy, why it crashed if it did).

    **A CRASH IS NOT A KILL.**  That is the whole of `CH-0237`, and it is the trap this harness
    fell into: a mutant that could not import its subject exited non-zero, printed no
    `SELFTEST FAIL:` line, and was reported as a SURVIVOR.  Counting it as a KILL instead would be
    the same mistake with the other sign -- `CH-0237`'s own instance, where 24 rows were killed by
    one `FileNotFoundError`.  So a crash is neither: it is returned separately and refused.
    """
    run = subprocess.run(
        [sys.executable, os.path.join(directory, "tools", GATE), "--selftest"],
        capture_output=True,
        text=True,
    )
    named = {line.strip() for line in run.stdout.splitlines()
             if line.startswith("SELFTEST FAIL:")}
    crashed = None
    if run.returncode != 0 and not named:
        crashed = (run.stderr.strip().splitlines() or ["(no stderr)"])[-1]
    return named, crashed


def _baseline():
    """The named failures of an UNMUTATED copy, which every killer count is measured against."""
    directory = _fixture()
    try:
        named, crashed = _named_failures(directory)
        if crashed:
            raise AssertionError(
                "the UNMUTATED copy crashed, so nothing below is a measurement: " + crashed)
        return named
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main():
    mutations = BASE_MUTATIONS + per_classification_mutations()
    baseline = _baseline()
    print("# baseline in an unmutated copy: {} pre-existing failure(s), SUBTRACTED from every "
          "killer count below".format(len(baseline)))
    for name in sorted(baseline):
        print("  BASELINE  " + name)

    sources = {
        name: open(os.path.join(HERE, name), encoding="utf-8").read()
        for name in FIXTURE_TOOLS
    }
    failures = 0
    for name, filename, old, new in mutations:
        occurrences = sources[filename].count(old)
        if occurrences != 1:
            print("ANCHOR    {:<70} occurs {} times in {}, expected 1".format(
                name, occurrences, filename))
            failures += 1
            continue
        directory = _fixture()
        try:
            path = os.path.join(directory, "tools", filename)
            open(path, "w", encoding="utf-8").write(sources[filename].replace(old, new))
            named, crashed = _named_failures(directory)
            named -= baseline
            if crashed:
                print("CRASHED   {:<70} {}".format(name, crashed))
                print("            a crash is neither a kill nor a survival — the mutant never "
                      "reached the named tests, and this row measures nothing")
                failures += 1
            elif not named:
                print("SURVIVES  {:<70} no named test failed".format(name))
                failures += 1
            else:
                print("killed by {:<2} named test(s)  {}".format(len(named), name))
                for line in sorted(named)[:3]:
                    print("            " + line)
        finally:
            shutil.rmtree(directory, ignore_errors=True)

    for name, reason in RETIRED:
        print("RETIRED   {:<70} {}".format(name, reason))
    print("# {} mutation(s) ({} base + {} per-classification), {} retired, {} survivor(s)".format(
        len(mutations), len(BASE_MUTATIONS), len(mutations) - len(BASE_MUTATIONS),
        len(RETIRED), failures))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
