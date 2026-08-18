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
# T-183 -- emits gpd/results/T-183-challenge-status-self-consistency.json.
#
#     tools/T-183-emit-result.py
#
# Retained rather than run once and discarded, per SESSION-PROMPT.md: everything built on behalf
# of this project stays in this project, and every number in `C-0113` has to be reproducible from
# the checkout by whoever reads it next.
#
# It emits NO wall-clock timing and NO step count.  `CLAUDE.md`: a timing is less reproducible
# than a step count, not more, and one such field makes a result file permanently un-diffable.
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_spec = importlib.util.spec_from_file_location(
    "trace_answers", os.path.join(ROOT, "tools", "trace-answers.py")
)
tracer = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(tracer)

CHALLENGES = os.path.join(ROOT, "gpd", "challenges")
ANSWERS = os.path.join(ROOT, "ANSWERS.md")
QUEUE = os.path.join(ROOT, "TASKS.md")


def corpus_coverage():
    """How much of the challenge corpus declares a status, and how much the README indexes."""
    statuses = tracer.challenge_statuses(CHALLENGES)
    with open(os.path.join(CHALLENGES, "README.md"), encoding="utf-8") as handle:
        index = handle.read()
    indexed = {m.group(1) for m in re.finditer(r"^\|\s*\[`(CH-\d{1,4})`\]", index, re.M)}
    declared = {k: v for k, v in statuses.items() if v != "UNKNOWN"}
    return {
        "challengeFileCount": len(statuses),
        "withDeclaredStatus": len(declared),
        "withoutDeclaredStatus": len(statuses) - len(declared),
        "indexedInReadme": len(indexed),
        "absentFromReadme": len(set(statuses) - indexed),
        "declaredOpen": sum(1 for v in declared.values() if v == "OPEN"),
        "declaredClosed": sum(1 for v in declared.values() if v == "CLOSED"),
    }


def window_sweep(answers_text):
    """The false-positive count against the real deliverable at each verdict window.

    This is the measurement that chose `_VERDICT_WINDOW`, and it is the reason the number is 80
    rather than a taste.  Reported at every rung so the choice can be re-audited without re-running
    anything: the misattribution that had to be excluded survives every window above 80.
    """
    original = tracer._VERDICT_WINDOW
    sweep = {}
    try:
        for window in (40, 60, 80, 120, 200, 400, 100000):
            tracer._VERDICT_WINDOW = window
            found = tracer.self_contradictions(answers_text)
            sweep[str(window)] = {
                "subjects": sorted(c.task for c in found),
                "count": len(found),
            }
    finally:
        tracer._VERDICT_WINDOW = original
    return sweep


def selftest_count():
    """The self-tests the tracer carries, counted by running them rather than by reading them."""
    completed = subprocess.run(
        [sys.executable, os.path.join(ROOT, "tools", "test-trace-answers.py")],
        capture_output=True,
        text=True,
        check=True,
    )
    return sum(1 for line in completed.stdout.splitlines() if line.startswith("ok   "))


def main():
    with open(ANSWERS, encoding="utf-8") as handle:
        answers_text = handle.read()
    with open(QUEUE, encoding="utf-8") as handle:
        queue_text = handle.read()

    statuses = tracer.challenge_statuses(CHALLENGES)
    references = re.findall(r"`(CH-\d{1,4})`", answers_text)

    result = {
        "task": "T-183",
        "claim": "C-0113",
        "what": (
            "The deliverable's self-consistency check extended from TASK identifiers to CHALLENGE "
            "identifiers, with a corpus comparison against each challenge file's own **Status** "
            "row. Process task: it computes no physical quantity. Units: none. See "
            "tools/trace-answers.py, tools/test-trace-answers.py and C-0106, which raised it."
        ),
        "verificationType": "logical, with executable self-tests",
        "parameters": {
            "verdictWindowCharacters": tracer._VERDICT_WINDOW,
            "openAssertionWindowCharacters": tracer._OPEN_WINDOW,
            "challengeStatusAuthority": "each gpd/challenges/CH-*.md file's own **Status** table row",
            "challengeStatusReadCaseSensitively": False,
            "proseVerdictWordsReadCaseSensitively": True,
            "openWinsATieInAChallengeStatusCell": True,
        },
        "corpus": corpus_coverage(),
        "deliverable": {
            "challengeReferences": len(references),
            "distinctChallengesReferenced": len(set(references)),
            "taskReferences": len(
                re.findall(r"`(T-\d{1,4}[a-z]?|P-\d{1,4})`", answers_text)
            ),
        },
        "verdicts": {
            "openAssertions": len(tracer.open_assertions(answers_text)),
            "staleTaskStatuses": len(tracer.stale_statuses(answers_text, queue_text)),
            "staleChallengeStatuses": len(
                tracer.stale_challenge_statuses(answers_text, statuses)
            ),
            "selfContradictions": len(tracer.self_contradictions(answers_text)),
        },
        "windowSweep": window_sweep(answers_text),
        "selfTests": {
            "count": selftest_count(),
            "allPass": True,
        },
    }
    destination = os.path.join(
        ROOT, "gpd", "results", "T-183-challenge-status-self-consistency.json"
    )
    with open(destination, "w", encoding="utf-8") as handle:
        json.dump(result, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("wrote {}".format(destination))
    return 0


if __name__ == "__main__":
    sys.exit(main())
