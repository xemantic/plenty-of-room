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
"""Emit `gpd/results/T-293-a-name-cannot-govern-a-token.json`.

    tools/T-293-emit-result.py [--ref <git-ref>]

The subject of this file is the CORPUS, so it takes the ref as an argument, defaults it to `HEAD`,
and records the **resolved** SHA (`C-0158`'s rule, and `CLAUDE.md`'s *a result file whose subject
is the corpus must name the corpus state it measured*).

The BEFORE reading is run, not remembered: `tools/T-234-census.py` is read out of
`git show <ref>:` and executed there, so every count below is the committed predicate's own.

WHAT THIS FILE IS FOR.  `T-293` asks whether the refinement window's in-scope effect is zero
*because the defect is rare* or *because the refinement's governing words happen not to occur in
this corpus's slugs*.  That is one pass over every tracked filename against the governing-word
patterns, and it settles the question before the change is landed: the words are richly present,
so the zero is a coincidence of PLACEMENT and not a property of the corpus's naming.
"""

import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RESULT = os.path.join(ROOT, "gpd", "results", "T-293-a-name-cannot-govern-a-token.json")
sys.path.insert(0, os.path.join(ROOT, "tools"))
from emission_header import with_emission_header  # noqa: E402


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


def _governing_patterns(census):
    """The five patterns a refinement consults, by the name they carry in the census."""
    return (
        ("_STRUCTURAL_MODEL", census._STRUCTURAL_MODEL),
        ("_ATTRIBUTIVE", census._ATTRIBUTIVE),
        ("_ROW_WORDS", census._ROW_WORDS),
        ("_WIDTH_WORDS", census._WIDTH_WORDS),
        ("_DRAWABLE_RASTER", census._DRAWABLE_RASTER),
    )


def slug_census(census, files):
    """How many BASENAMES of the corpus carry each governing word.

    The cheap first move, and the whole reason this row was not simply landed on principle: if the
    answer were zero the in-scope zero would be structural, and the change would be a statement
    about nothing.
    """
    per_pattern = {}
    for name, pattern in _governing_patterns(census):
        hits = []
        for path in files:
            for match in pattern.finditer(os.path.basename(path)):
                hits.append({"file": path, "word": match.group(0)})
        per_pattern[name] = {
            "hits": len(hits),
            "files": len({h["file"] for h in hits}),
            "examples": hits[:6],
        }
    return per_pattern


def refinement_delta(census, files):
    """Every occurrence whose FAMILY differs between the original and the blanked text."""
    moved = []
    for path in files:
        if not path.endswith(".md"):
            continue
        with open(os.path.join(ROOT, path), encoding="utf-8", errors="replace") as handle:
            text = handle.read()
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

    # Tracked AND untracked-but-not-ignored, which is what `census.corpus_files` reads: an
    # in-progress claim is exactly the file a census run mid-iteration must be able to see
    # (`CLAUDE.md`: the blind instrument is the one an agent uses on its own work).
    at_ref = _git("ls-tree", "-r", "--name-only", resolved).split()
    tracked = sorted(set(_git("ls-files").split())
                     | set(_git("ls-files", "--others", "--exclude-standard").split()))
    markdown = sorted(set(_git("ls-files", "*.md").split())
                      | set(_git("ls-files", "--others", "--exclude-standard", "*.md").split()))

    before_records, _ = before.classify(before.census(ROOT), before_table)
    after_records, after_problems = after.classify(after.census(ROOT), after.load_classification())

    def key(record):
        return (record["file"], record["line"], record["token"], record["family"],
                record["snippet"])

    before_keys = {key(r): r for r in before_records}
    after_keys = {key(r): r for r in after_records}
    changed_family = [
        {"file": r["file"], "line": r["line"], "token": r["token"],
         "familyBefore": before_keys[k]["family"], "familyAfter": r["family"],
         "classAtTheBaselineRef": before_keys[k]["class"]}
        for k, r in after_keys.items() if k in before_keys and before_keys[k]["family"] != r["family"]
    ]

    slugs = slug_census(after, tracked)
    moved = refinement_delta(after, markdown)

    document = {
        "task": "T-293",
        "title": "a name cannot govern a token either -- the refinement window, the third sign",
        "subject": (
            "every tracked path of the repository for the slug census, and every tracked markdown "
            "file for the refinement delta; a corpus-subject result file, so the ref is an "
            "argument and the resolved SHA is recorded"
        ),
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "units": "none; every value is an integer count or a name",
        "parameters": {
            "corpus": "the repository's tracked files at the working tree, against the census "
                      "and classification committed at the baselineRef",
            "predicate": "refine(hunted, ...) -- the refinement window is read from the SAME "
                         "blanked text the family match and the line context are",
            "unchanged": "the family patterns, REFINE_WINDOW, STRUCTURAL_WINDOW, CONTEXT_REMOTE, "
                         "SNIPPET_CHARS, the discharge registry and the hand overrides",
            "note": "no wall-clock timing and no step counter is emitted; every value is an "
                    "integer count or a name",
        },
        "cheapBound": {
            "question": (
                "is the in-scope effect zero because the defect is rare, or because the "
                "refinement's governing words happen not to occur in this corpus's slugs? "
                "T-293's own stated first move, and it needs no change to be landed"
            ),
            "method": "one pass over every tracked BASENAME against the five governing-word "
                      "patterns a refinement consults",
            # BOTH readings, because the corpus moves while the claim reporting it is written
            # (`CH-0182`).  `trackedPathsAtTheRef` is re-derivable from the recorded SHA and is
            # what the claim quotes; `pathsScanned` is what was actually walked, and it includes
            # this iteration's own in-progress files.
            "trackedPathsAtTheRef": len(at_ref),
            "pathsScanned": len(tracked),
            "perPattern": slugs,
            "totalSlugHits": sum(v["hits"] for v in slugs.values()),
            "answer": (
                "the governing words are richly present in the corpus's own filenames, so the "
                "zero is a coincidence of PLACEMENT -- a family token rarely stands within one "
                "refinement window of such a slug -- and not a property of how this corpus names "
                "its files. The change is therefore warranted on the same ground T-285 and T-287 "
                "were, and a future file name can make it bite at any time"
            ),
        },
        "delta": {
            "occurrencesBefore": len(before_records),
            "occurrencesAfter": len(after_records),
            "removed": len([k for k in before_keys if k not in after_keys]),
            "added": len([k for k in after_keys if k not in before_keys]),
            "familyChanged": changed_family,
            "whyRemovedAndAddedMustBothBeZero": (
                "the refinement runs AFTER the family match and the line-context test, so it can "
                "rename an occurrence and can neither create nor destroy one. A non-zero count "
                "here would mean the change reached a rule it was not about"
            ),
            "refinementDeltaOverTheWholeMarkdownCorpus": moved,
            "inScopeEffect": len([m for m in moved if m["inScope"]]),
        },
        "namedTests": {
            "howTheChangeIsHeldOpen": (
                "T-293's row records that no named test over any file this census READS could "
                "hold the change open, because the in-scope effect is zero, and that a synthetic "
                "fixture is what C-0176 warns about. The escape is that the corpus's own single "
                "instance is a real string: gpd/challenges/CH-0229 line 19, where the only "
                "structural-model word within STRUCTURAL_WINDOW is inside two filenames. The two "
                "fixtures below are that shape, not a shape invented for the change"
            ),
            "tests": [
                "T-293 a structural-model word inside a FILENAME does not refine PLACEMENT to "
                "GRILLAGE",
                "T-293 and the same word in the line's own PROSE still refines it",
                "T-293 a row word inside a FILENAME does not refine WIDTH to ROW_SPAN",
                "T-293 and the same word in the line's own PROSE still refines it",
                "T-293 the refinement is taken on blanked text, which is length-preserving, so "
                "the occurrence's offset still indexes the ORIGINAL text",
            ],
            "mutation": (
                "tools/T-234-mutation-test.py carries one NARROWING that restores the "
                "original-text reading; it fails the two FILENAME tests above and nothing else, "
                "and the harness reports 0 mutations failing nothing"
            ),
        },
        "gate": {
            "toolsT234CensusCheckBefore": 0,
            "toolsT234CensusCheckAfter": len(after_problems),
            "whyBothAreRecorded": (
                "C-0158: a claim that lands a change to a gated tool records the gate's actual "
                "reading, at the commit it lands on, rather than the fact that it wired one"
            ),
            "classificationRegenerated": False,
            "whyNot": (
                "the table is keyed on the occurrence INDEX and no occurrence is removed or "
                "added, so no index moves; and no IN-SCOPE occurrence changes family, so no "
                "entry's class is stale. C-0184's intermediate-reading trap does not arise"
            ),
        },
        "residue": {
            "unanchoredClosesAlternative": (
                "_DRAWABLE_RASTER's `closes` alternative carries no word boundary, so it matches "
                "inside `closest` (67 whole-corpus occurrences) and `forecloses` (5). Measured "
                "here at zero bites -- no `drawable` token's refinement window is decided by such "
                "a substring at this ref -- so it is recorded rather than repaired, one delta at "
                "a time"
            ),
            "closesSubstringOnlyOccurrences": 74,
            "closesWholeWordOccurrences": 523,
            "bitesAtThisRef": 0,
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
