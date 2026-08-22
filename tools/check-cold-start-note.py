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
# P-29 -- `TASKS.md`'s cold-start heading names the iteration the journal is actually at.
#
#     tools/check-cold-start-note.py            checks, exit 1 if the heading is behind
#     tools/check-cold-start-note.py --selftest runs the self-tests
#
# WHY THIS EXISTS.  `P-28` (`C-0163`) found `## Start here -- the state after iteration 26` standing
# at the head of a queue that was at **iteration 38**, retitled it, and wrote down why it matters:
# a cold session is told to trust a reading order dated before four of the corrections it carries,
# so a mis-titled note is worse than a stale one.  **Four iterations later it read "after iteration
# 38" again.**  Nothing had gone wrong that anybody could see -- the five numbered items were being
# annotated in place, correctly, exactly as before -- because **a date in a heading has no owner**:
# it is not a number a tracer can attribute to a claim, not a status word a queue checker reads, and
# not a link.  Every other self-description in this repository that recurred twice was mechanised;
# this one was written down twice instead, which is `CLAUDE.md`'s own *a convention is not a
# mechanism* for the fifth time.
#
# THE PREDICATE.  The heading's iteration number must be at least the highest `## Iteration N` in
# `JOURNAL.md`.  Not equality: the journal is appended before the heading is retitled within one
# iteration, and both land in the same commit, so `>=` is the condition that is true of every
# COMMITTED state while being false of exactly the drift this exists to catch.  It is deliberately
# NOT a check that the note's CONTENT is fresh -- no predicate can see that, and `P-28`'s finding is
# precisely that the content was fine and the date was not.

import argparse
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
QUEUE = os.path.join(ROOT, "TASKS.md")
JOURNAL = os.path.join(ROOT, "JOURNAL.md")

_HEADING = re.compile(r"^##\s+Start here\s+—\s+the state after iteration\s+(\d+)\s*$", re.MULTILINE)
_JOURNAL_ITERATION = re.compile(r"^##\s+Iteration\s+(\d+)\b", re.MULTILINE)


def heading_iteration(queue_text):
    """The iteration the cold-start heading claims, or None if there is no such heading."""
    match = _HEADING.search(queue_text)
    return int(match.group(1)) if match else None


def journal_iteration(journal_text):
    """The highest iteration the journal records, or None if it records none."""
    found = [int(n) for n in _JOURNAL_ITERATION.findall(journal_text)]
    return max(found) if found else None


def defects(queue_text, journal_text):
    """[(kind, detail)] -- empty when the heading is at or ahead of the journal."""
    heading = heading_iteration(queue_text)
    journal = journal_iteration(journal_text)
    if heading is None:
        return [("NO-HEADING", "TASKS.md has no `## Start here — the state after iteration N` heading")]
    if journal is None:
        return [("NO-JOURNAL", "JOURNAL.md records no `## Iteration N` entry")]
    if heading < journal:
        return [(
            "STALE-HEADING",
            "the cold-start heading says iteration {} and JOURNAL.md is at {} -- "
            "retitle it, and check the reading order still reads true".format(heading, journal),
        )]
    return []


def _selftest():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    heading = "## Start here — the state after iteration 42\n"
    journal42 = "## Iteration 41 — x\n## Iteration 42 — y\n"

    check("a heading level with the journal passes", defects(heading, journal42) == [])
    check(
        "a heading AHEAD of the journal passes -- the note may be written first",
        defects("## Start here — the state after iteration 43\n", journal42) == [],
    )
    check(
        "a heading BEHIND the journal is a defect",
        [k for k, _ in defects("## Start here — the state after iteration 38\n", journal42)]
        == ["STALE-HEADING"],
    )
    check(
        "and the live instance is the one that recurred",
        "says iteration 38" in defects("## Start here — the state after iteration 38\n", journal42)[0][1],
    )
    check(
        "the journal maximum is taken, not the last entry",
        journal_iteration("## Iteration 42 — y\n## Iteration 7 — z\n") == 42,
    )
    check("a missing heading is a defect", [k for k, _ in defects("", journal42)] == ["NO-HEADING"])
    check(
        "a missing journal is a defect and not a pass",
        [k for k, _ in defects(heading, "")] == ["NO-JOURNAL"],
    )
    check(
        "an em dash is required -- a hyphen is a different heading",
        heading_iteration("## Start here - the state after iteration 42\n") is None,
    )
    check(
        "the heading is matched anywhere in the file, not only at the top",
        heading_iteration("# TASKS\n\nprose\n\n" + heading) == 42,
    )

    for failure in failures:
        print("SELFTEST FAIL: {}".format(failure))
    print("# {} self-test(s), {} failure(s)".format(9, len(failures)))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--queue", default=QUEUE)
    parser.add_argument("--journal", default=JOURNAL)
    parser.add_argument("--selftest", action="store_true")
    args = parser.parse_args(argv)

    if args.selftest:
        return _selftest()

    queue_text = open(args.queue, encoding="utf-8").read()
    journal_text = open(args.journal, encoding="utf-8").read()
    found = defects(queue_text, journal_text)
    for kind, detail in found:
        print("{}\t{}".format(kind, detail))
    print(
        "# {} defect(s); cold-start heading at iteration {}, journal at {}".format(
            len(found), heading_iteration(queue_text), journal_iteration(journal_text)
        )
    )
    return 1 if found else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
