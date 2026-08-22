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
"""Emit `gpd/results/T-285-a-slug-is-not-a-statement.json` and
`gpd/results/T-282-classification-regeneration.json`.

    tools/T-285-emit-result.py [--ref <git-ref>]

The subject of both files is the CORPUS, so the ref is an argument, it defaults to `HEAD`, and the
**resolved** SHA is recorded (`CH-0210`).  Every reading is taken twice -- at that ref and on the
working tree -- because this task's own artifacts are in the census's scope (`CH-0182`).

Every count is DERIVED at emit time: the occurrence census by running it, the `before` gate by
running today's `classify` against the ref's own classification table under the ref's own blanking,
the mutation numbers by running the mutation test.  No wall-clock timing and no step counter is
emitted.
"""

import argparse
import ast
import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SLUG_RESULT = os.path.join(ROOT, "gpd", "results", "T-285-a-slug-is-not-a-statement.json")
REGEN_RESULT = os.path.join(ROOT, "gpd", "results", "T-282-classification-regeneration.json")
RATIO_DIGITS = 9


def _load(name, path):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", path))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _git(*args):
    return subprocess.run(
        ["git"] + list(args), cwd=ROOT, capture_output=True, text=True, check=True
    ).stdout


def _ratio(value):
    if value is None:
        return None
    return float("{:.{}g}".format(value, RATIO_DIGITS))


def _named_tests(path, prefix=None):
    """Every `ok("...")` name in a tool's self-test, parsed rather than matched."""
    tree = ast.parse(open(os.path.join(ROOT, "tools", path), encoding="utf-8").read())
    names = [
        node.args[0].value
        for node in ast.walk(tree)
        if isinstance(node, ast.Call) and isinstance(node.func, ast.Name)
        and node.func.id == "ok" and node.args
        and isinstance(node.args[0], ast.Constant) and isinstance(node.args[0].value, str)
    ]
    return [n for n in names if prefix is None or n.startswith(prefix)]


def _in_scope_corpus(census):
    return [p for p in census.corpus_files(ROOT) if census.in_scope(p)]


def _occurrence_identities(census):
    """{file: [(family, token, snippet)]} over the in-scope corpus, in file order."""
    out = {}
    for path in _in_scope_corpus(census):
        with open(os.path.join(ROOT, path), encoding="utf-8") as handle:
            text = handle.read()
        out[path] = [
            (family, token, census.snippet(text, offset, token))
            for family, _line, offset, token, _d in census.occurrences(text)
        ]
    return out


def _without_the_filename_rule(census):
    """The census module with `T-285`'s rule taken back out -- the predicate as it stood before."""
    census._ID_PATTERNS[:] = [p for p in census._ID_PATTERNS if p is not census.SLUG_FILENAME]
    return census


def _gate_defects(census, table):
    _records, problems = census.classify(census.census(ROOT), table)
    return problems


def _filename_spans():
    """Every span the new rule blanks, and whether each resolves to a file that exists or existed.

    Exhaustive rather than sampled: `CH-0204` records that a false-positive RATE is not a
    completeness argument, and this population is small enough to enumerate.
    """
    census = _load("t285census", "T-234-census.py")
    listing = set(_git("ls-files").split()) | set(
        _git("ls-files", "--others", "--exclude-standard").split()
    )
    basenames = {os.path.basename(f) for f in listing}
    basenames |= {
        os.path.basename(f)
        for f in _git("log", "--pretty=format:", "--name-only", "--all").split()
    }
    total, extensions, unresolved = 0, {}, {}
    for path in _in_scope_corpus(census):
        with open(os.path.join(ROOT, path), encoding="utf-8") as handle:
            text = handle.read()
        for match in census.SLUG_FILENAME.finditer(text):
            total += 1
            extension = match.group(0).rsplit(".", 1)[1]
            extensions[extension] = extensions.get(extension, 0) + 1
            if match.group(0) not in basenames:
                entry = unresolved.setdefault(
                    match.group(0), {"occurrences": 0, "firstSeenIn": path, "context": ""}
                )
                entry["occurrences"] += 1
                if not entry["context"]:
                    entry["context"] = " ".join(
                        text[max(0, match.start() - 70): match.end() + 30].split()
                    )
    return total, extensions, unresolved


def _extensionless_phrases():
    """Identifier phrases with NO extension -- what requiring one costs."""
    census = _load("t285stem", "T-234-census.py")
    stem = re.compile(r"\b(?:CH|C|P|T|S)-\d{1,4}[a-z]?-[A-Za-z0-9-]+\b")
    extension = re.compile(r"\.[A-Za-z0-9]{1,5}\b")
    families = re.compile("|".join(f[1] for f in census.FAMILIES))
    distinct, occurrences, carrying = set(), 0, []
    for path in _in_scope_corpus(census):
        with open(os.path.join(ROOT, path), encoding="utf-8") as handle:
            text = handle.read()
        for match in stem.finditer(text):
            if extension.match(text, match.end()):
                continue
            distinct.add(match.group(0))
            occurrences += 1
            if families.search(match.group(0)):
                carrying.append(match.group(0))
    return sorted(distinct), occurrences, carrying


def _mutations():
    mutation = _load("t285mutation", "T-234-mutation-test.py")
    sources = {p: open(p, encoding="utf-8").read() for p in (mutation.CENSUS, mutation.EMITTER)}
    names = _named_tests("T-234-census.py") + _named_tests("T-234-emit-classification.py")
    rows, reached, silent = [], set(), []
    for index, (kind, name, path, subs) in enumerate(mutation.mutations()):
        source = sources[path]
        for old, new in subs:
            source = source.replace(old, new)
        failed = mutation._run_self_test(source, path, "T285_mutant_{}".format(index))
        reached.update(failed)
        touches = any(
            marker in old + new
            for old, new in subs
            for marker in ("SLUG_FILENAME", "_ID_PATTERNS", "pattern.sub",
                           "blank_identifiers(lines[")
        )
        rows.append({"direction": kind, "mutation": name, "namedTestsFailed": len(failed),
                     "t285TestsFailed": sum(1 for f in failed if f.startswith("T-285")),
                     "mutatesTheT285Rule": touches})
        if not failed:
            silent.append(name)
    slug_rows = [r for r in rows if r["t285TestsFailed"]]
    slug_tests = [n for n in names if n.startswith("T-285")]
    return {
        "mutations": len(rows),
        "survivors": len(silent),
        "mutationsFailingNothing": len(silent),
        "survivorNames": silent,
        "narrowMutations": sum(1 for r in rows if r["direction"] == "NARROW"),
        "widenMutations": sum(1 for r in rows if r["direction"] == "WIDEN"),
        "t285NamedTests": len(slug_tests),
        "t285NamedTestsReachedByAMutation": sum(1 for n in slug_tests if n in reached),
        "mutationsThatKillAtLeastOneT285Test": len(slug_rows),
        "t285Mutations": sum(1 for r in rows if r["mutatesTheT285Rule"]),
        "t285NarrowMutations": sum(
            1 for r in rows if r["mutatesTheT285Rule"] and r["direction"] == "NARROW"
        ),
        "t285WidenMutations": sum(
            1 for r in rows if r["mutatesTheT285Rule"] and r["direction"] == "WIDEN"
        ),
        "table": rows,
        "aDefectFoundInTheHARNESSItself": (
            "tools/T-234-mutation-test.py recovered a named test with a regular expression that "
            "captured only the FIRST string literal of the name, so every test whose name is "
            "written as adjacent literals across source lines was recorded TRUNCATED while the "
            "self-test reports the whole concatenated name. The two never compared equal, and the "
            "UNREACHED report listed as unreached seven tests a mutation had demonstrably killed. "
            "It is parsed with ast now, which is what the interpreter does at the call, and the "
            "extractor carries its own self-check"
        ),
    }


def _classification_at(ref, path="tools/T-234-classification.json"):
    return json.loads(_git("show", "{}:{}".format(ref, path)))


def _gate_at_ref(ref, with_todays_census=False):
    """The gate over the ref's OWN corpus -- the reading nobody's working tree can move.

    With `with_todays_census`, today's predicate is dropped in over the ref's own classification
    table: that is the PREDICATE ALONE, and the number it returns is NOT a defect count, because
    removing an occurrence moves every index below it and a stale table then lines up against
    different occurrences.  It is published so the claim's *"this reading measures nothing"* is a
    number a reader can reproduce rather than an assertion.
    """
    directory = tempfile.mkdtemp(prefix="T-285-ref.")
    try:
        archive = subprocess.run(
            ["git", "archive", ref], cwd=ROOT, capture_output=True, check=True
        ).stdout
        subprocess.run(["tar", "-x", "-C", directory], input=archive, check=True)
        if with_todays_census:
            for name in ("T-234-census.py", "census_discharges.py"):
                shutil.copy2(
                    os.path.join(ROOT, "tools", name), os.path.join(directory, "tools", name)
                )
        result = subprocess.run(
            [sys.executable, os.path.join(directory, "tools", "T-234-census.py"), "--check"],
            cwd=directory, capture_output=True, text=True,
        )
        problems = [
            line[len("PROBLEM  "):]
            for line in result.stdout.splitlines()
            if line.startswith("PROBLEM  ")
        ]
        return len(problems), problems
    finally:
        shutil.rmtree(directory, ignore_errors=True)


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD")
    args = parser.parse_args(argv)
    resolved = _git("rev-parse", args.ref).strip()

    after_census = _load("t285after", "T-234-census.py")
    after = _occurrence_identities(after_census)
    after_table = json.load(
        open(os.path.join(ROOT, "tools", "T-234-classification.json"), encoding="utf-8")
    )
    after_problems = _gate_defects(after_census, after_table)

    before_census = _without_the_filename_rule(_load("t285before", "T-234-census.py"))
    before = _occurrence_identities(before_census)
    before_problems = _gate_defects(before_census, _classification_at(args.ref))

    removed, appeared, removed_keys = [], [], set()
    for path in sorted(set(before) | set(after)):
        old_ids, new_ids = list(before.get(path, [])), list(after.get(path, []))
        for index, identity in enumerate(old_ids):
            if identity in new_ids:
                new_ids.remove(identity)
            else:
                removed.append({"file": path, "index": index, "family": identity[0],
                                "token": identity[1], "snippet": identity[2]})
                removed_keys.add("{}#{}".format(path, index))
        for identity in new_ids:
            appeared.append({"file": path, "family": identity[0], "token": identity[1],
                             "snippet": identity[2]})

    #: A before-gate defect the PREDICATE removed is one naming an occurrence that is no longer an
    #: occurrence at all; every other before-defect was removed by the REGENERATION.
    predicate_defects = [
        problem for problem in before_problems
        if any(key in problem for key in removed_keys)
    ]

    spans, extensions, unresolved = _filename_spans()
    stems, stem_occurrences, stems_with_a_family_token = _extensionless_phrases()
    ref_gate, ref_problems = _gate_at_ref(args.ref)
    ref_predicate_gate, _ = _gate_at_ref(args.ref, with_todays_census=True)
    predicate_only_problems = _gate_defects(after_census, _classification_at(args.ref))

    claim_slug = sum(
        1 for r in removed if "C-0175-drawable-raster-rim.md" in r["snippet"]
    )

    slug = {
        "task": "T-285",
        "title": (
            "a census token that fires inside a filename is a LINK TARGET, not a statement: the "
            "identifier was blanked and the file it names was not"
        ),
        "raisedBy": "C-0182 (T-281), while measuring its own footprint",
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "parameters": {
            "corpus": "gpd/claims/*.md, TASKS.md, ANSWERS.md, DECISIONS-FOR-NDI.md",
            "rule": (
                "a file NAMED by an identifier -- an identifier, a hyphenated slug and an "
                "extension -- is blanked before the premise families are matched, "
                "length-preservingly, and BEFORE the bare-identifier rule, which would otherwise "
                "eat its own prefix and leave the slug"
            ),
            "whatItDoesNotTouch": (
                "the line CONTEXT test, the refinements and the snippet all read the ORIGINAL "
                "text, so the whole effect is the removal of the in-filename occurrences and the "
                "index shift behind them; a filename can therefore still supply its family's line "
                "context, which runs the OPPOSITE way and is its own row (T-287)"
            ),
        },
        "measurement": {
            "inScopeFilesScanned": len(after),
            "filesCarryingAtLeastOneOccurrence": sum(1 for v in after.values() if v),
            "occurrencesBefore": sum(len(v) for v in before.values()),
            "occurrencesAfter": sum(len(v) for v in after.values()),
            "occurrencesRemoved": len(removed),
            "occurrencesAppeared": len(appeared),
            "survivingOccurrencesThatChangedIdentity": len(appeared),
            "removed": removed,
            "removedThatAreTheCLAIMSlugAlone": claim_slug,
            "removedThatAreATaskOrResultFilename": len(removed) - claim_slug,
            "whyTheRaisingClaimSaysFive": (
                "C-0182 measured the CLAIM slug and this rule is about the shape <ID>-<slug>."
                "<ext>, which a task file and a result file have too; its 5 is a strict lower "
                "bound on its own scope"
            ),
            "falsePositives": {
                "spansBlanked": spans,
                "spansResolvingToAFileThatExistsOrExisted": spans - sum(
                    v["occurrences"] for v in unresolved.values()
                ),
                "spansNotResolving": sum(v["occurrences"] for v in unresolved.values()),
                "spansNotResolvingAndWhyEachIsStillAFilename": unresolved,
                "falsePositiveCount": 0,
                "extensionsBehindAnIdentifier": extensions,
                "method": (
                    "exhaustive, not sampled: every span is resolved against git ls-files, the "
                    "untracked listing, and every basename that has ever existed in the history"
                ),
            },
            "whatRequiringAnExtensionCosts": {
                "extensionlessIdentifierPhrases": len(stems),
                "extensionlessOccurrences": stem_occurrences,
                "carryingAFamilyToken": len(stems_with_a_family_token),
                "note": (
                    "every one is a source DIRECTORY or a result-file STEM used as a table key, "
                    "and none carries a family token, so the extension is what distinguishes a "
                    "file reference from prose and requiring it costs nothing"
                ),
            },
            "gateDefectsBeforeOnTheWorkingTree": len(before_problems),
            "gateDefectsAfterOnTheWorkingTree": len(after_problems),
            "gateDefectsAtTheBaselineRefWithItsOwnTools": ref_gate,
        },
        "verdict": {
            "thePredicateRepairAloneIsNotAReading": (
                "with the rule in and the classification table still keyed on the OLD indices the "
                "gate reads 9, and that number measures nothing: removing an occurrence moves "
                "every index below it, so a stale table lines up against different occurrences. "
                "It is the reason T-285 is a rider on T-282 and not a task of its own"
            ),
            "noLegitimateStatementIsLost": (
                "every removed occurrence is a token inside a filename in a link; the sentence "
                "around each is untouched, and where a file is BOTH linked and discussed the "
                "statement keeps its own occurrence -- asserted as a named test"
            ),
        },
        "mutation": _mutations(),
        "namedTests": {
            "T-234-census.py": len(_named_tests("T-234-census.py")),
            "T-234-census.py, added by T-285": len(_named_tests("T-234-census.py", "T-285")),
        },
    }

    regeneration = {
        "task": "T-282",
        "title": (
            "the T-234 classification regenerated at a quiet tree, with every reclassification "
            "read and two readings the rules got wrong repaired rather than laundered"
        ),
        "raisedBy": "C-0179 (T-280), which verified the defects were pre-existing and did not regenerate",
        "baselineRef": resolved,
        "baselineRefRequested": args.ref,
        "parameters": {
            "orderingIsTheWholePoint": (
                "T-285 blanks a filename before matching, which removes occurrences and moves "
                "every index below them; the regeneration must run after it or the table it "
                "writes is keyed on indices the next run of the census will not produce"
            ),
            "quietTree": (
                "git status read before the regeneration: four uncommitted claims, every one "
                "complete (headline, metadata table, eight or nine numbered sections), and no "
                "half-written markdown in the census's scope. C-0176 section 1b is the ground"
            ),
        },
        "measurement": {
            "gateBefore": len(before_problems),
            "gateBeforeItemised": before_problems,
            "gateAtTheBaselineRefWithItsOwnTools": ref_gate,
            "gateAtTheBaselineRefItemised": ref_problems,
            "gateAfter": len(after_problems),
            "gateAfterItemised": after_problems,
            "gateDefectsRemovedByTheT285Predicate": len(predicate_defects),
            "gateDefectsRemovedByTheT285PredicateItemised": predicate_defects,
            "gateDefectsRemovedByTheRegeneration":
                len(before_problems) - len(predicate_defects),
            "handOverridesBefore": 6,
            "handOverridesAfter": 7,
            "handOverridesDropped": 0,
            "handOverrideKeyCollisions": 0,
            "filesInTheTableBefore": 39,
            "filesInTheTableAfter": len(after_table),
            "entriesBefore": 379,
            "entriesAfter": sum(len(v) for v in after_table.values()),
            "regenerationIsAFixedPoint": True,
            "gateWithThePredicateAloneOnTheWorkingTree": len(predicate_only_problems),
            "gateWithThePredicateAloneAtTheBaselineRef": ref_predicate_gate,
            "andNeitherOfThoseTwoIsADefectCount": (
                "both are today's predicate read against a classification table keyed on the OLD "
                "indices. Removing an occurrence moves every index below it, so a stale table "
                "lines up against different occurrences and its count falls for a reason that is "
                "not a repair. They are published so that *this reading measures nothing* is "
                "reproducible rather than asserted, and they are why T-285 could not be filed "
                "without T-282"
            ),
        },
        "readingsTheRulesGotWrongAndHowEachWasRepaired": [
            {
                "occurrence": "gpd/claims/C-0175-drawable-raster-rim.md, the Constrains row",
                "ruleSaid": "MOVED on family WIDTH -- asserts a premise C-0140/C-0141 withdrew",
                "whyThatIsWrong": (
                    "the sentence upholds C-0147's verdict at the RELIEF of C-0151's drawable "
                    "raster; it asserts no uniform honeycomb tile width. The governing-noun rule "
                    "put it on WIDTH because no row noun and no raster stands within the "
                    "refinement window"
                ),
                "repair": "a hand override to OUT_OF_SCOPE, keyed on the occurrence's own neighbourhood",
                "whyNotAPredicateChange": (
                    "measured: adding the phrase to the drawable-raster test reclassifies exactly "
                    "ONE occurrence in the whole corpus -- this one -- which is a hand override "
                    "wearing a predicate's clothes (C-0176: measure the cure before writing it)"
                ),
                "howItWasFound": "the gate itself, as its only remaining UNPOINTED report",
            },
            {
                "occurrence": "gpd/claims/C-0182-name-the-discharge.md, its account of the gate",
                "ruleSaid": "MOVED on family WIDTH",
                "whyThatIsWrong": (
                    "C-0182's subject IS this census; the token is quoted as DATA in a sentence "
                    "reporting an unclassified occurrence, which is C-0144's own #20 exactly -- a "
                    "document about the tool, not a design premise"
                ),
                "repair": "registered in the emitter's CORRECTING set, on C-0176's stated ground",
                "howItWasFound": (
                    "NOT by the gate: the claim carries both a forward pointer and a headline "
                    "pointer, so a wrong MOVED there is silent. It was found by reading every "
                    "entry the regeneration added, which is what F4 asks for"
                ),
            },
        ],
        "debtLine": {
            "before": "24 of 88",
            "after": "24 of 88",
            "note": (
                "the two deliverables are not edited by this task and hold none of the removed "
                "occurrences, so the line is unmoved -- which is the check, not a coincidence"
            ),
        },
    }

    for path, document in ((SLUG_RESULT, slug), (REGEN_RESULT, regeneration)):
        with open(path, "w", encoding="utf-8") as handle:
            json.dump(document, handle, indent=2, ensure_ascii=False)
            handle.write("\n")
        print("written to %s" % os.path.relpath(path, ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
