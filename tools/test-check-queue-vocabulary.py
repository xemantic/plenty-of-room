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
# P-29 -- the MUTATION test for `tools/check-queue-vocabulary.py`.
#
#     tools/test-check-queue-vocabulary.py
#
# `C-0127`'s standard: a predicate's self-tests are worth nothing unless CHANGING the predicate
# fails a NAMED one.  `CLAUDE.md` adds the direction that is never written -- a predicate carrying
# exclusions has TWO mutation directions, and the widening one is the one nobody tests.  Each row
# below rewrites the tool's source, runs its own `--selftest`, and requires it to FAIL.
#
# The last mutation is the one that matters.  It does not touch the predicate at all: it declares
# the iteration-41 coinage `SECOND DELIVERABLE ANSWERED` as NOT CLOSING, which is what an author
# in a hurry would do to make the gate pass.  The agreement check refuses it, because
# `trace-answers.queue_status` still reads it CLOSED -- so the list edit alone cannot silence the
# defect, and `_NOT_CLOSED_QUALIFIER` has to be taught the word too.  That is the second sense
# `CLAUDE.md` asks for every coined status word to be tested in.

import os
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
TOOL = os.path.join(HERE, "check-queue-vocabulary.py")

MUTATIONS = [
    (
        "predicate WIDENED: a bold run anywhere in the cell counts as a verdict",
        "bold = _LEADING_BOLD.match(cell)",
        "bold = _LEADING_BOLD.search(cell)",
    ),
    (
        "predicate WIDENED: no word bound, so a bold prose sentence is a verdict",
        "MAX_WORDS = 6",
        "MAX_WORDS = 99",
    ),
    (
        "predicate NARROWED: a verdict must be the whole cell",
        "if CLOSING_WORD.search(phrase) and len(phrase.split()) <= MAX_WORDS:",
        "if CLOSING_WORD.search(phrase) and cell.strip() == '**' + phrase + '**':",
    ),
    (
        "strike-through no longer blanked, so a WITHDRAWN verdict is read as live",
        'return re.sub(r"~~.*?~~", lambda m: " " * len(m.group(0)), text, flags=re.DOTALL)',
        "return text",
    ),
    (
        "the vocabulary is opened, so an undeclared coinage passes",
        "known = CLOSING_VERDICTS | NOT_CLOSING_VERDICTS\n"
        "    return [(i, p) for i, p in leading_verdicts(queue_text) if p not in known]",
        "return []",
    ),
    (
        "THE LIST EDIT: the coinage declared NOT CLOSING, which the reader contradicts",
        '    "PARTLY DONE",\n})',
        '    "PARTLY DONE",\n    "SECOND DELIVERABLE ANSWERED",\n})',
    ),
]


def main():
    baseline = subprocess.run(
        [sys.executable, TOOL, "--selftest"], capture_output=True, text=True
    )
    if baseline.returncode != 0:
        print("BASELINE FAILS -- mutation testing is meaningless:\n" + baseline.stdout)
        return 1

    source = open(TOOL, encoding="utf-8").read()
    failures = 0
    for name, old, new in MUTATIONS:
        if source.count(old) != 1:
            print("ANCHOR  {:<70} anchor occurs {} times, expected 1".format(name, source.count(old)))
            failures += 1
            continue
        directory = tempfile.mkdtemp(prefix="queue-vocab-mutation.")
        try:
            # the tool imports `trace-answers` from its own directory
            shutil.copy(os.path.join(HERE, "trace-answers.py"), directory)
            mutant = os.path.join(directory, "check-queue-vocabulary.py")
            open(mutant, "w", encoding="utf-8").write(source.replace(old, new))
            run = subprocess.run(
                [sys.executable, mutant, "--selftest"], capture_output=True, text=True
            )
            named = [
                line for line in run.stdout.splitlines() if line.startswith("SELFTEST FAIL:")
            ]
            if run.returncode == 0 or not named:
                print("SURVIVES  {:<70} no named test failed".format(name))
                failures += 1
            else:
                print("killed by {:<2} named test(s)  {}".format(len(named), name))
                for line in named:
                    print("            " + line)
        finally:
            shutil.rmtree(directory, ignore_errors=True)

    print("# {} mutation(s), {} survivor(s)".format(len(MUTATIONS), failures))
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
