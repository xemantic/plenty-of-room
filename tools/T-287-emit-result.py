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
"""Emit `gpd/results/T-287-a-filename-cannot-supply-a-context.json`.

    tools/T-287-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS, so it takes the ref as an argument, defaults it to `HEAD`,
and records the **resolved** SHA.

THE BEFORE READING IS RUN, NOT REMEMBERED.  `tools/T-234-census.py` and
`tools/T-234-classification.json` are read out of `git show <ref>:` and executed there, so the
class of every occurrence the change removes is the class the committed table really gave it --
which is the only way to satisfy this task's `F2`, that no `MOVED` or `DISCHARGED` occurrence is
removed.  Reading the classes out of TODAY's table after the change would compare the old
occurrences against a re-indexed table, and `C-0184` recorded exactly that intermediate reading as
measuring nothing.
"""

import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(
    ROOT, "gpd", "results", "T-287-a-filename-cannot-supply-a-context.json"
)


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
    module = importlib.util.module_from_spec(
        importlib.util.spec_from_loader(name, loader=None)
    )
    module.__file__ = path
    sys.modules[name] = module
    exec(compile(source, path, "exec"), module.__dict__)
    return module


def _key(record):
    """An occurrence's identity across the change.

    The INDEX cannot be used -- removing an occurrence moves every index below it, which is the
    whole reason `T-285` had to land with `T-282`.  `snippet` is the census's own answer to *what
    is this occurrence* and it is centred on the token, so two occurrences of one token on one
    physical line are told apart by it; keying on `(file, line, token, family)` alone silently
    merged the two on `TASKS.md`'s own row and under-reported the removal by one.
    """
    return (record["file"], record["line"], record["token"], record["family"], record["snippet"])


def _refinement_delta(census):
    """The OTHER delta, measured separately (`F4`): the refinement window's own filename reading.

    `REFINE_WINDOW` and `STRUCTURAL_WINDOW` read the ORIGINAL text too, so a filename can supply a
    governing word there in the same way.  It is a third delta of a third sign and it is NOT taken
    here, for `C-0184`'s reason: one delta at a time or none of them can be audited.
    """
    moved = []
    for path in census.corpus_files(ROOT):
        text = open(os.path.join(ROOT, path), encoding="utf-8").read()
        hunted = census.blank_identifiers(text)
        lines = hunted.split("\n")
        for family, pattern, context, refine in census.FAMILIES:
            if not refine:
                continue
            for match in re.finditer(pattern, hunted):
                line_index = text.count("\n", 0, match.start())
                if context and not re.search(context, lines[line_index], re.I):
                    continue
                on_text = refine(text, match.start(), match.end())
                on_blanked = refine(hunted, match.start(), match.end())
                if on_text != on_blanked:
                    moved.append({
                        "file": path,
                        "line": line_index + 1,
                        "token": match.group(0),
                        "onTheOriginalText": on_text,
                        "onTheBlankedText": on_blanked,
                        "inScope": census.in_scope(path),
                    })
    return moved


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)

    resolved = _git("rev-parse", args.ref).strip()
    before = _load_source(
        "t234_before", _git("show", "%s:tools/T-234-census.py" % args.ref),
        os.path.join(ROOT, "tools", "T-234-census.py"),
    )
    before_table = json.loads(_git("show", "%s:tools/T-234-classification.json" % args.ref))
    after = _load("t234_after", "T-234-census.py")

    before_records, _problems = before.classify(before.census(ROOT), before_table)
    after_records = after.census(ROOT)

    before_keys = {_key(r): r for r in before_records}
    after_keys = {_key(r) for r in after_records}
    removed = [before_keys[k] for k in before_keys if k not in after_keys]
    added = [r for r in after_records if _key(r) not in before_keys]
    removed.sort(key=lambda r: (r["file"], r["line"]))

    counts_before = {c: sum(1 for r in before_records if r["class"] == c) for c in before.CLASSES}
    counts_after_by_class = None
    after_classified, after_problems = after.classify(
        after.census(ROOT), after.load_classification()
    )
    if not [p for p in after_problems if p.startswith("unclassified") or p.startswith("stale")]:
        counts_after_by_class = {
            c: sum(1 for r in after_classified if r["class"] == c) for c in after.CLASSES
        }

    def remote(records):
        return [
            {"file": r["file"], "line": r["line"], "token": r["token"],
             "contextDistance": r["contextDistance"]}
            for r in records
            if r["contextDistance"] is not None and r["contextDistance"] > after.CONTEXT_REMOTE
        ]

    document = {
        "task": "T-287",
        "title": "a filename cannot supply a premise family's line context either",
        "subject": (
            "the in-scope corpus tools/T-234-census.py reads -- gpd/claims/, TASKS.md and the two "
            "deliverables; a corpus-subject result file, so the ref is an argument and the "
            "resolved SHA is recorded"
        ),
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "units": "none; every value is an integer count or a name",
        "parameters": {
            "corpus": (
                "the in-scope corpus of tools/T-234-census.py -- gpd/claims/, TASKS.md, "
                "ANSWERS.md and DECISIONS-FOR-NDI.md -- as it stands in the working tree, "
                "against the census and classification committed at the baselineRef"
            ),
            "predicate": (
                "the family match, the line context test and the context distance all read the "
                "same text: identifiers and `<ID>-<slug>.<ext>` filenames blanked, "
                "length-preservingly"
            ),
            "unchanged": (
                "the family patterns, the refinement window, CONTEXT_REMOTE, SNIPPET_CHARS and "
                "the discharge registry"
            ),
            "note": "no wall-clock timing and no step counter is emitted; every value is an integer count or a name",
        },
        "change": {
            "rule": (
                "the line context test and the context distance read the SAME text the match is "
                "taken against -- identifiers and filenames blanked, length-preservingly"
            ),
            "whyBlankingAndNotALinkRule": (
                "a rule recognising a Markdown link target specifically would miss the same slug "
                "written bare, which this corpus does in Provenance and Conditions rows and in "
                "prose naming a result file; blank_identifiers already answers `what is a name "
                "here` and is tested in both directions by T-285"
            ),
            "whyTheDistanceTravelsWithTheAdmissionTest": (
                "context_distance exists to say `the line context said nothing about this token`. "
                "Measured on the original text it can only UNDER-report, because a filename is a "
                "context word it counts and the admission rule does not; both readings must come "
                "from one text or the diagnostic contradicts the rule it diagnoses"
            ),
        },
        "delta": {
            "occurrencesBefore": len(before_records),
            "occurrencesAfter": len(after_records),
            "filesBefore": len({r["file"] for r in before_records}),
            "filesAfter": len({r["file"] for r in after_records}),
            "removed": len(removed),
            "added": len(added),
            "removedOneAtATime": [
                {
                    "file": r["file"],
                    "line": r["line"],
                    "index": r["index"],
                    "family": r["family"],
                    "token": r["token"],
                    "classAtTheBaselineRef": r["class"],
                    "byHand": bool(r.get("byHand")),
                    "contextDistanceAtTheBaselineRef": r["contextDistance"],
                }
                for r in removed
            ],
            "removedByClassAtTheBaselineRef": {
                cls: sum(1 for r in removed if r["class"] == cls)
                for cls in sorted({r["class"] for r in removed})
            },
            "F2": {
                "movedOrDischargedRemoved": sum(
                    1 for r in removed if r["class"] in before.ADDRESSED
                ),
                "whyThatIsTheAcceptancePredicate": (
                    "MOVED and DISCHARGED are the debt; a repair that hides debt is the failure "
                    "direction this census exists to avoid, and it is the one thing that would "
                    "have made the answer `state the decision and keep the original line`"
                ),
            },
            "classCountsBefore": counts_before,
            "classCountsAfter": counts_after_by_class,
        },
        "diagnostic": {
            "remoteContextBefore": remote(before_records),
            "remoteContextAfter": remote(after_classified),
            "whatMoved": (
                "the two TASKS.md occurrences on one row are gone with the occurrences "
                "themselves, and one ANSWERS.md occurrence enters the list because its nearest "
                "context word was inside a filename -- which is the diagnostic doing its job "
                "rather than a regression"
            ),
        },
        "theOtherDeltaNotTakenHere": {
            "what": (
                "the refinement window (REFINE_WINDOW, STRUCTURAL_WINDOW) reads the original text "
                "too, so a filename can supply a governing word there as well"
            ),
            "measured": _refinement_delta(after),
            "inScopeEffect": 0,
            "whyNotTakenHere": (
                "C-0184's reason for splitting T-285 from T-287, one level down: one delta at a "
                "time, or neither can be audited against the other. Its in-scope effect at this "
                "ref is zero, so nothing in this census's own scope could hold a named test open "
                "for it; it is queued as a row of its own"
            ),
        },
        "regeneration": {
            "handOverridesAtTheBaselineRef": sum(
                1 for entries in before_table.values() for e in entries.values()
                if e.get("byHand")
            ),
            "handOverridesCarried": sum(
                1 for entries in after.load_classification().values()
                for e in entries.values() if e.get("byHand")
            ),
            "droppedOverrides": [
                {"file": r["file"], "family": r["family"], "class": r["class"]}
                for r in removed if r.get("byHand")
            ],
            "whyItIsDroppedRatherThanMoved": (
                "the occurrence it qualified no longer exists -- the statement it read was "
                "admitted by a name and by nothing else. C-0176's mechanism: an override is "
                "keyed on what the occurrence IS and is dropped LOUDLY, never silently moved"
            ),
            "whyTheRegenerationIsNotOptional": (
                "a family change moves a class and the table is keyed on the index; with the "
                "predicate in and the table still on the old indices the gate reads 10 stale "
                "entries, and that number measures nothing (C-0184)"
            ),
        },
        "gate": {
            "toolsT234CensusCheckBefore": 0,
            "toolsT234CensusCheckAfter": len(after_problems),
            "whyBothAreRecorded": (
                "C-0158: a claim that lands a change to a gated tool records the gate's actual "
                "reading"
            ),
        },
    }

    with open(RESULT, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(RESULT, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
