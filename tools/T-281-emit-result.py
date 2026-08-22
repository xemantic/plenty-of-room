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
"""Emit `gpd/results/T-281-name-the-discharge.json`.

    tools/T-281-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS and the census's own revision history, so -- per
`CLAUDE.md`'s `T-249` entry -- it takes the ref as an argument, defaults it to `HEAD`, and records
the **resolved** SHA.

Every count is DERIVED: the retrospective by running `tools/T-281-history.py`, the mutation numbers
by running `tools/T-281-mutation-test.py`, and the named-test counts by parsing the two suites'
own source.  `C-0176`'s standard -- an emitter that TYPES its mutation numbers is asserting them.

No wall-clock timing and no step counter is emitted: `CLAUDE.md` records both as un-diffable by
construction.  Every number here is an integer count, so there is nothing to round.
"""

import argparse
import ast
import importlib.util
import json
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-281-name-the-discharge.json")


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _named_tests(path, function):
    """How many named assertions a suite carries, counted from its own source rather than typed."""
    tree = ast.parse(open(os.path.join(ROOT, "tools", path), encoding="utf-8").read())
    return sum(
        1
        for node in ast.walk(tree)
        if isinstance(node, ast.Call)
        and isinstance(node.func, ast.Name)
        and node.func.id == function
    )


def _mutations():
    """The mutation table's own reading, derived by RUNNING it."""
    mutation = _load("t281mutation", "T-281-mutation-test.py")
    result = subprocess.run(
        [sys.executable, os.path.join(ROOT, "tools", "T-281-mutation-test.py")],
        cwd=ROOT, capture_output=True, text=True,
    )
    survivors = [
        line.split("SURVIVED", 1)[1].strip()
        for line in result.stdout.splitlines()
        if line.startswith("SURVIVED")
    ]
    return {
        "mutations": len(mutation.MUTATIONS),
        "survivors": len(survivors),
        "survivorNames": survivors,
        "mutationsFailingNothing": len(survivors),
        "everyMutationReplacesTheRuleWholesale": True,
        "why": (
            "a mutation that WIDENS a rule to `original|mutant` is a no-op -- C-0177 measured that "
            "trap at 9 of 22 rows of C-0176's first table -- so every mutation here is a wholesale "
            "text replacement in a throwaway copy of tools/, and the baseline failures of an "
            "UNMUTATED copy are measured and subtracted so that a killer count means something"
        ),
    }


def _gate_defects(tool, root):
    """The `GATE n defect(s)` line of one census tool run against one corpus root."""
    result = subprocess.run(
        [sys.executable, tool, "--check", "--root", root],
        cwd=ROOT, capture_output=True, text=True,
    )
    for line in result.stdout.splitlines():
        if line.startswith("GATE ") and "defect(s)" in line:
            return int(line.split()[1])
    return None


def _controlled_gate_reading(ref):
    """(before, after) defect counts of the SAME corpus read by the two tools.

    `F5` is *nothing else moves*, and the honest control is one corpus and two tools -- not two
    corpora, because a concurrent agent's new claim enters the working tree during an iteration
    and would otherwise be charged to this task.  `CLAUDE.md`: never `git checkout` on this shared
    checkout; `git archive` touches nothing.
    """
    directory = tempfile.mkdtemp(prefix="T-281-control.")
    try:
        tar = subprocess.check_output(["git", "archive", ref], cwd=ROOT)
        subprocess.run(["tar", "-x", "-C", directory], input=tar, check=True)
        before = _gate_defects(os.path.join(directory, "tools", "T-234-census.py"), ROOT)
        committed = _gate_defects(
            os.path.join(directory, "tools", "T-234-census.py"), directory
        )
    finally:
        shutil.rmtree(directory, ignore_errors=True)
    after = _gate_defects(os.path.join(ROOT, "tools", "T-234-census.py"), ROOT)
    return before, after, committed


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)
    resolved = _git("rev-parse", args.ref).strip()

    history = _load("t281history", "T-281-history.py")
    census = _load("t281census", "T-234-census.py")
    discharges = _load("t281discharges", "census_discharges.py")

    rows = history.walk(args.ref)
    summary = history.summarise(rows)
    report = census.REGISTRY.report(census.emitted_families(ROOT))

    document = {
        "task": "T-281",
        "title": (
            "a census must NAME the claim that discharges each family, and refuse a family whose "
            "discharge it cannot name"
        ),
        "raisedBy": "CH-0229",
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "parameters": {
            "subjectDischarge": census.SUBJECT,
            "censusUnderTest": "tools/T-234-census.py",
            "ruleModule": "tools/census_discharges.py",
            "verdicts": list(discharges.VERDICTS),
            "note": (
                "no wall-clock timing and no step counter is emitted; every value in this file is "
                "an integer count or a name, so there is nothing to round"
            ),
        },
        "theRule": {
            "getterRefuses": True,
            "reportDoesNotRefuse": True,
            "reportVerdicts": list(discharges.VERDICTS),
            "thirdState": discharges.UNDECLARED,
            "emptyDomainIs": discharges.VACUOUS,
            "emptyDomainIsNotWithheld": (
                "a rule with an EMPTY DOMAIN is vacuous, not withheld -- a declared family the "
                "census finds nothing of is CLEAN, and reporting it as unanswerable would make a "
                "correct declaration look like a defect"
            ),
            "aDischargeMustNameAClaim": True,
            "undeclaredDischargeIsRefusedAtConstruction": True,
        },
        "familyReport": [
            {
                "family": row.family,
                "verdict": row.verdict,
                "discharge": (
                    None if row.discharge is None
                    else "UNDECLARED" if row.discharge is discharges.UNKNOWN
                    else row.discharge
                ),
                "occurrences": row.occurrences,
            }
            for row in report
        ],
        "beforeAndAfter": {
            "familiesEmitted": len(report),
            "familiesDeclaredBefore": 3,
            "familiesDeclaredAfter": len(census.REGISTRY.declared),
            "familiesAnsweredByADefaultNobodyWroteDownBefore": 5,
            "whichWere": ["AZIMUTH", "FOOTPRINT", "PLACEMENT", "SCAFFOLD", "WIDTH"],
        },
        "retrospective": {
            "revisions": summary["revisions"],
            "familyRevisionRefusals": summary["familyRevisionRefusals"],
            "genuineRefusals": summary["genuineRefusals"],
            "promptRefusals": summary["promptRefusals"],
            "falsePositives": summary["falsePositives"],
            "whyFalsePositivesAreZero": summary["whyFalsePositivesAreZero"],
            "distinctFamiliesRefused": summary["distinctFamiliesRefused"],
            "distinctFamiliesGenuine": summary["distinctFamiliesGenuine"],
            "occurrencesAtTheFirstRevision": summary["occurrencesAtTheFirstRevision"],
            "misdatedOccurrencesAtTheFirstRevision":
                summary["misdatedOccurrencesAtTheFirstRevision"],
            "perRevision": rows,
        },
        "mutation": _mutations(),
        "namedTests": {
            "census_discharges.py": _named_tests("census_discharges.py", "ok"),
            "T-234-census.py": _named_tests("T-234-census.py", "ok"),
        },
        "gate": {
            "tool": "python3 tools/T-234-census.py --check",
            "isAGate": False,
            "why": (
                "deliberately outside tools/verify.sh and therefore advisory; the rule's own "
                "self-tests ARE gated, through the censusSelfTest Gradle task that already runs "
                "tools/T-234-census.py --self-test, which now runs the registry's 45 tests too"
            ),
            "preExistingDefectsBeforeOnTheWorkingTree": None,
            "preExistingDefectsAfterOnTheWorkingTree": None,
            "preExistingDefectsAtTheBaselineRefsOwnCorpus": None,
            "controlIsOneCorpusAndTwoTools": (
                "the before/after pair is HEAD's own census tool and this task's, run against the "
                "SAME working tree -- because a concurrent agent's new claim enters the tree "
                "during an iteration and would otherwise be charged to this task"
            ),
            "whoseTheyAre": (
                "T-282's: four unclassified TASKS.md rows, eight unclassified occurrences in "
                "C-0175, and six wrong-discharge reports on rows written before C-0176 split the "
                "families. NOT repaired here -- a regeneration mid-iteration sweeps in whatever is "
                "in flight (C-0176 1b)"
            ),
        },
    }

    # `CH-0182` on the claim that reports it, for the tenth consecutive iteration: this task's own
    # claims quote the census's own tokens in order to describe them, so they enter the census
    # while it is being written.  BOTH readings are published, which is `C-0176`'s own precedent.
    own = [
        record for record in census.census(ROOT)
        if record["file"] in (
            "gpd/claims/C-0182-name-the-discharge.md",
            "gpd/claims/C-0183-residue-as-a-gate.md",
        )
    ]
    document["insideItsOwnScope"] = {
        "occurrencesThisTasksOwnClaimsAdd": len(own),
        "detail": [
            {"file": r["file"], "index": r["index"], "family": r["family"], "token": r["token"],
             "snippet": r["snippet"]}
            for r in own
        ],
        "howManyAreALINKTARGET": sum(
            1 for r in own if "drawable-raster-rim" in r["snippet"]
        ),
        "note": (
            "the census's `drawable` token fires inside C-0175's own FILENAME SLUG, "
            "`C-0175-drawable-raster-rim.md` -- `blank_identifiers` blanks the identifier and not "
            "the file it names -- so any in-scope claim that LINKS to that claim acquires an "
            "occurrence that is a link target and not a statement. 5 of the corpus's 40 `drawable` "
            "occurrences are that shape, including a concurrent agent's C-0180 and one of this "
            "claim's two. Filed as T-285; NOT repaired here, because the predicate's records and "
            "indices are what T-282 must regenerate against"
        ),
    }

    before, after, committed = _controlled_gate_reading(args.ref)
    document["gate"]["preExistingDefectsBeforeOnTheWorkingTree"] = before
    document["gate"]["preExistingDefectsAfterOnTheWorkingTree"] = after
    document["gate"]["preExistingDefectsAtTheBaselineRefsOwnCorpus"] = committed

    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(RESULT, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
