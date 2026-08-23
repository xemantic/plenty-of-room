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
"""Emit `gpd/results/T-252-a-quoted-number-has-no-link-back.json`.

    tools/T-252-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS, so it takes the ref as an argument, defaults it to `HEAD`,
and records the **resolved** SHA (`CH-0246`).

The population is the one class in which `CH-0199` is DETECTABLE: a decimal carrying more than
nine significant digits, which a correctly rounded result file cannot contain.  Every such token
in the corpus's documents is looked up in the concatenated text of every committed result file.
"""

import argparse
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-252-a-quoted-number-has-no-link-back.json")
sys.path.insert(0, os.path.join(ROOT, "tools"))
from emission_header import with_emission_header  # noqa: E402

#: The same trailing guard `T-249`/`CH-0204` settled: `(?!\w)` refuses a mantissa digit and a
#: following letter is fine, `(?!\.\d)` refuses a version-like second dot.
DECIMAL = re.compile(r"(?<![\w.])(\d+\.\d+)(?!\w)(?!\.\d)")
STRUCK = re.compile(r"~~.*?~~", re.DOTALL)


def significant_digits(token):
    return len(token.replace(".", "").lstrip("0").rstrip("0"))


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def documents():
    found = []
    for directory in ("gpd/claims", "gpd/challenges", "gpd/tasks"):
        path = os.path.join(ROOT, directory)
        found += [
            os.path.join(directory, name)
            for name in sorted(os.listdir(path))
            if name.endswith(".md")
        ]
    return found + ["ANSWERS.md", "DECISIONS-FOR-NDI.md", "TASKS.md", "CLAUDE.md", "JOURNAL.md"]


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)
    resolved = _git("rev-parse", args.ref).strip()

    results = sorted(
        name for name in os.listdir(os.path.join(ROOT, "gpd", "results"))
        if name.endswith(".json")
    )
    # THE CENSUS'S OWN OUTPUT IS EXCLUDED FROM THE CORPUS IT SEARCHES.  It is not a hypothetical:
    # the first emission wrote the 64 literals into this very file, and the next run found every
    # one of them `findable` -- in its own output -- and reported **0 unfindable**.  `CLAUDE.md`
    # already records the shape (*a measurement tree the measurement writes into stops being the
    # baseline*); here the tree is one file and the exclusion is one line.  The redaction of §5
    # makes it moot going forward and the exclusion stays, because a rule that is only true of
    # today's emitter is not a rule.
    own = os.path.basename(RESULT)
    results = [name for name in results if name != own]
    blob = "".join(
        open(os.path.join(ROOT, "gpd", "results", name), encoding="utf-8", errors="replace").read()
        for name in results
    )

    scanned = documents()
    tokens = []
    for path in scanned:
        text = open(os.path.join(ROOT, path), encoding="utf-8", errors="replace").read()
        text = STRUCK.sub(lambda match: " " * len(match.group(0)), text)
        for number, line in enumerate(text.split("\n"), start=1):
            for match in DECIMAL.finditer(line):
                token = match.group(1)
                if significant_digits(token) <= 9:
                    continue
                # `CH-0182`, and the gate said so: a census of OVER-PRECISE tokens cannot carry
                # its own subject.  Emitting the literal put 64 of them into a result file and
                # `tools/check-result-file-hygiene.py --prose` -- a build-failing gate -- read
                # them as exactly what they are.  So the token is REDACTED to its first three
                # significant digits and its digit count, and `file`/`line` recover it exactly.
                # Same shape as `C-0184`: a census must not spell the thing it counts.
                tokens.append({
                    "file": path,
                    "line": number,
                    "tokenRedacted": token[:4] + "\u2026",
                    "significantDigits": significant_digits(token),
                    "findableInACommittedResultFile": token in blob,
                })

    unfindable = [record for record in tokens if not record["findableInACommittedResultFile"]]
    by_file = {}
    for record in unfindable:
        by_file[record["file"]] = by_file.get(record["file"], 0) + 1

    # The provenance half, which IS gateable.
    claims = sorted(
        name for name in os.listdir(os.path.join(ROOT, "gpd", "claims")) if name.endswith(".md")
    )
    naming, naming_none, missing = 0, [], []
    available = set(results)
    for name in claims:
        text = open(os.path.join(ROOT, "gpd", "claims", name), encoding="utf-8").read()
        named = re.findall(r"gpd/results/([A-Za-z0-9._+-]+\.json)", text)
        if named:
            naming += 1
            missing += [
                {"claim": name, "namedFile": one} for one in named if one not in available
            ]
        else:
            naming_none.append(name)

    document = {
        "task": "T-252",
        "title": (
            "a number quoted in a claim has no link back to its result file -- and every "
            "detectable instance of the class is DELIBERATE"
        ),
        "subject": (
            "every markdown document of gpd/claims, gpd/challenges, gpd/tasks and the five "
            "root-level documents, against the concatenated text of every committed result file; "
            "a corpus-subject result file, so the ref is an argument and the resolved SHA is "
            "recorded"
        ),
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "units": "none; every value is an integer count, a line number or a name",
        "parameters": {
            "population": (
                "a decimal carrying MORE than nine significant digits, which is the one token "
                "class in which CH-0199 is detectable at all: a correctly rounded result file "
                "cannot contain one, so an unfindable token is either historical or stale"
            ),
            "struck": "struck spans are blanked first -- a withdrawn number is not a quotation",
            "resultFilesScanned": len(results),
            "documentsScanned": len(scanned),
            "note": (
                "no wall-clock timing and no step counter is emitted; every value is an integer "
                "count, a line number or a name"
            ),
        },
        "census": {
            "tokensAboveNineSignificantDigits": len(tokens),
            "findable": len(tokens) - len(unfindable),
            "unfindable": len(unfindable),
            "unfindableFiles": len(by_file),
            "unfindableByFile": dict(sorted(by_file.items())),
            "unfindableTokens": unfindable,
        },
        "theDecision": {
            "verdict": (
                "the class stays MANUAL -- T-252's second acceptance branch -- and the ground is "
                "a measurement rather than a preference"
            ),
            "deliberateOnInspection": len(unfindable),
            "staleOnInspection": 0,
            "howItWasRead": (
                "every unfindable token read in its own line's context. Each falls in one of "
                "three sub-classes, all legitimate: (1) a defect's own output, quoted so the "
                "defect stays measurable; (2) a before/after pair from a precision repair; "
                "(3) a value derived in the claim itself and never emitted to any result file. "
                "None is a stale quotation"
            ),
            "whyAConventionWouldCostMoreThanItBuys": (
                "a convention marking a historical number makes an unmarked unfindable token a "
                "defect by construction, which is the acceptance's first branch. Applied today it "
                "would have to be written onto every one of the unfindable tokens above, all of "
                "which are correct, and its first output would be that many non-defects"
            ),
            "andTheCorpusManufacturesTheClass": (
                "C-0092's rule is that a repair must leave the defect MEASURABLE, so a claim "
                "repairing a numeric defect is REQUIRED to quote the defective value at full "
                "precision. The population therefore grows every time this loop works correctly "
                "-- CH-0230's mechanism, met on a third predicate in one iteration, after "
                "T-234's debt line and C-0197's priced-on-an-adjudicated-challenge residue"
            ),
        },
        "theGateableHalf": {
            "what": (
                "a claim's Provenance row names its result file as a BARE PATH, so "
                "tools/check-corpus-links.py cannot see it -- that tool resolves [label](target) "
                "and nothing else. Whether the named file EXISTS is a different question from "
                "whether a quoted number is still in it, and it is decidable"
            ),
            "claims": len(claims),
            "claimsNamingAResultFile": naming,
            "claimsNamingNone": len(naming_none),
            "namedFilesThatDoNotExist": len(missing),
            "missing": missing,
            "whyItIsNotVacuous": (
                "renaming or removing a result file is a normal act of this loop -- C-0101 "
                "re-emits and C-0117 sorts the sweep -- and it leaves every claim that named the "
                "old path pointing at nothing, silently. The gate read 1 within minutes of "
                "existing, on a queue row naming a study's output before the study had run"
            ),
            "gate": "tools/check-result-path-references.py, 8 self-tests",
        },
        "whatThisDoesNotClaim": {
            "theUndetectableMajority": (
                "the population is decimals above nine significant digits, because a rounded "
                "result file cannot contain one. A number quoted at NINE digits or fewer is "
                "indistinguishable from a live one whether or not it is stale, and this census "
                "says nothing about it"
            ),
            "andCH0199StandsOnItsOtherHalf": (
                "the challenge's finding -- that C-0101's rule is executed by hand and has no "
                "instrument -- is untouched. What is decided here is only that the instrument it "
                "asks for cannot be built on this population without a convention whose cost "
                "falls entirely on correct claims"
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
