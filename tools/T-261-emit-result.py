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
"""Emit `gpd/results/T-261-a-price-on-an-adjudicated-challenge.json`.

    tools/T-261-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS, so it takes the ref as an argument, defaults it to `HEAD`,
and records the **resolved** SHA.

`CH-0246` applies to this file too and is stated in it: re-running it with the default `--ref`
re-bases the measurement onto today's corpus.  The `before` reading is therefore taken by
executing `tools/trace-answers.py` **out of `git show <ref>:`**, so the four defects it records
are the four the committed gate could not see.
"""

import argparse
import importlib.util
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(
    ROOT, "gpd", "results", "T-261-a-price-on-an-adjudicated-challenge.json"
)
sys.path.insert(0, os.path.join(ROOT, "tools"))
from emission_header import with_emission_header  # noqa: E402

DELIVERABLES = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _load_source(name, source, path):
    module = importlib.util.module_from_spec(importlib.util.spec_from_loader(name, loader=None))
    module.__file__ = path
    sys.modules[name] = module
    exec(compile(source, path, "exec"), module.__dict__)
    return module


def _stale(module, statuses, text):
    return [
        {"line": line, "challenge": identifier, "corpusStatus": status}
        for line, identifier, status in module.stale_challenge_statuses(text, statuses)
    ]


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)
    resolved = _git("rev-parse", args.ref).strip()

    before = _load_source(
        "trace_before", _git("show", "%s:tools/trace-answers.py" % args.ref),
        os.path.join(ROOT, "tools", "trace-answers.py"),
    )
    after = _load("trace_after", "trace-answers.py")

    statuses = after.challenge_statuses(os.path.join(ROOT, "gpd", "challenges"))
    adjudicated = after.challenge_adjudications(os.path.join(ROOT, "gpd", "challenges"))

    # The four defects, read at the ref's own DOCUMENTS, so that the count is the one the
    # committed gate was blind to rather than the one left after this task repaired them.
    documents_at_ref = {
        name: _git("show", "%s:%s" % (args.ref, name)) for name in DELIVERABLES
    }
    documents_now = {
        name: open(os.path.join(ROOT, name), encoding="utf-8").read() for name in DELIVERABLES
    }

    blindness = {}
    for name, text in documents_at_ref.items():
        blindness[name] = {
            "committedPredicate": _stale(before, statuses, text),
            "repairedPredicate": _stale(after, statuses, text),
        }
    afterRepair = {
        name: _stale(after, statuses, text) for name, text in documents_now.items()
    }

    priced = {
        name: [
            {"line": line, "challenge": identifier}
            for line, identifier in after.prices_on_adjudicated(text, adjudicated)
        ]
        for name, text in documents_now.items()
    }
    unrecorded = after.unrecorded_adjudications(
        os.path.join(ROOT, "gpd", "claims"), adjudicated
    )

    document = {
        "task": "T-261",
        "title": (
            "a synthesis rests a number on a challenge the corpus has since answered -- the arm "
            "already existed and was blind to the challenge vocabulary's own open word"
        ),
        "subject": (
            "ANSWERS.md and DECISIONS-FOR-NDI.md against gpd/challenges and gpd/claims; a "
            "corpus-subject result file, so the ref is an argument and the resolved SHA is "
            "recorded"
        ),
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "units": "none; every value is an integer count, a line number or a name",
        "parameters": {
            "authority": (
                "a challenge's own file, per T-183 -- gpd/challenges/README.md's Status cell is "
                "free prose and this tool has never read it"
            ),
            "predicateChange": (
                "stale_challenge_statuses reads _CHALLENGE_OPEN_ASSERTION, which is "
                "_OPEN_WORD_ASSERTION plus the challenge vocabulary's own open word `raised`, "
                "guarded against the provenance idioms `raised by|in|as|at|and|against`"
            ),
            "unchanged": (
                "the task half's _OPEN_WORD_ASSERTION, the _HISTORICAL and _ANSWERING guards, "
                "_OPEN_WINDOW, challenge_status_of and the numeric arm"
            ),
            "note": (
                "no wall-clock timing and no step counter is emitted; every value is an integer "
                "count, a line number or a name"
            ),
        },
        "theArmAlreadyExisted": {
            "finding": (
                "T-261 asks for an arm that flags a deliverable passage citing a challenge as the "
                "source of a number where that challenge is adjudicated. The arm exists -- "
                "stale_challenge_statuses, T-183 -- and it inherited _OPEN_WORD_ASSERTION, which "
                "was written for a TASK's status vocabulary: open, unmeasured, unanswered, TODO. "
                "A challenge's own open state is RAISED, which is in neither list, so a "
                "deliverable calling an UPHELD challenge *raised* was invisible to a WIRED gate"
            ),
            "whyASeparatePatternAndNotAWidening": (
                "`raised by` is how this corpus states a challenge's PROVENANCE and TASKS.md is "
                "full of it, so widening the shared list would put a provenance idiom into the "
                "task half. T-183 already pinned the same separation for "
                "_OPEN_WORD_ASSERTION against _OPEN_WORD_VERDICT"
            ),
            "blindnessAtTheRef": blindness,
            "afterTheRepairInTheWorkingTree": afterRepair,
            "falsePositivesOnTheRepairedPredicate": 0,
            "howTheFalsePositiveRateWasMeasured": (
                "every hit read by hand: all four are the identical string `(`CH-0240`, raised)`, "
                "and CH-0240's own Status row reads **UPHELD** (C-0190, T-291, iteration 45). "
                "The `raised by` guard removes none of the four and is precautionary"
            ),
        },
        "residueOne": {
            "what": (
                "a number attributed to an ADJUDICATED challenge with no claim named inside a "
                "200-character window -- the shape CH-0203 describes, and what decision 8 was"
            ),
            "perDocument": priced,
            "whyItIsNotAGate": (
                "CH-0230's mechanism, and it is structural rather than fixable: a correcting "
                "sentence has to NAME the challenge in order to withdraw it, so the corrections "
                "land in the census the gate would fire on. The naive form -- any number near any "
                "adjudicated challenge -- reads 34 over the two documents and is almost entirely "
                "corrections; requiring that no claim be named in the window takes it to the "
                "count above, of which the hand reading finds 3 corrections and 2 "
                "cross-references"
            ),
            "policy": (
                "C-0129: gate what can be made clean and print the rest beside it, ungated. The "
                "line is printed unconditionally and counted in no exit code"
            ),
        },
        "residueTwo": {
            "what": (
                "a challenge a CLAIM adjudicates whose own **Status** row does not say so -- the "
                "INPUT defect, because stale_challenge_statuses reads that row as the authority"
            ),
            "challenges": sorted({identifier for identifier, _ in unrecorded}),
            "sites": [
                {"challenge": identifier, "claim": claim} for identifier, claim in unrecorded
            ],
            "theRowsOwnLiveInstance": (
                "CH-0185. C-0148 says `CH-0185` is ANSWERED -- the twelfth column is a box "
                "artefact -- and the challenge file still reads **raised**, so this tool reports "
                "it OPEN and a deliverable may rest a price on it indefinitely. That is why "
                "nothing caught decision 8, and it is a defect of the AUTHORITY rather than of "
                "the predicate"
            ),
            "falsePositives": 1,
            "falsePositiveDetail": (
                "CH-0157 in C-0132: *`CH-0157`, and it is why the bracket has to be withdrawn* -- "
                "the BRACKET is withdrawn, not the challenge. The clause guard [^.;|] does not "
                "stop a comma-and-conjunction, and it is reported rather than tuned away, "
                "because a guard narrowed to one observed case is a test written to the shape of "
                "the change (C-0176)"
            ),
            "whyItIsNotAGate": (
                "closing it means editing challenge files -- one Status row per challenge, each "
                "needing the adjudicating claim read -- which is its own task and its own delta"
            ),
        },
        "whatThisDoesNotClaim": {
            "theDeliverableArmIsNotDelivered": (
                "T-261's acceptance names a gate on a deliverable passage. It is REFUSED as a "
                "gate, with the measurement, and shipped as the audit the same acceptance permits"
            ),
            "theWordPriceIsStillInvisible": (
                "decision 8's own price was written *six flat cells of eight against three*, and "
                "tokens() has min_digits=2, so the words carry no token at all. Only the 0.07 nm "
                "in the same sentence is a token. A price written in words is invisible to every "
                "numeric arm of this tool, and no arm added here changes that"
            ),
        },
    }

    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(with_emission_header(document, "none", []), handle, indent=2,
                  ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(RESULT, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
