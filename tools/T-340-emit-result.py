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
# T-340 -- what a working-tree reading IS, measured over the whole corpus.
#
#     tools/T-340-emit-result.py [--ref <git-ref>] [--out <path>] [--self-test]
#
# THE CRUX THE ROW DEMANDED, AND WHY IT IS A MEASUREMENT.  `T-340` asks whether a working-tree
# reading has any LEGITIMATE use: if it does, the fix is a key a gate can refuse; if it does not,
# the fix is to stop emitting it.  Answered over every committed result file rather than by
# preference -- and the answer is BOTH, split by what the reading is OF.
#
#   kind A  the AFTER half of a before/after measurement of a repair the pass itself performs.
#           The BEFORE is pinned at the file's own `baselineRef`; the AFTER cannot be pinned,
#           because an emitter runs before its own commit exists.  Legitimate, and required:
#           `C-0092`'s *a repair must leave the defect measurable*.
#   kind B  a rival ABSOLUTE reading of a DECLARED REGISTRY QUANTITY -- a count the deliverable
#           prints about itself -- whose pinned counterpart already answers the same question.
#           Two files, and both are what `CH-0292` and `CH-0293` are about.
#   kind C  a reading of a corpus the pass does not change: stable today by luck, and pinnable at
#           the pass's own commit.
#
# WHY REMOVAL AND NOT A RENAMED KEY, for kind B.  A synthesis wanting to state what its OWN pass
# will look like is asking for a number that is unpinnable PRECISELY BECAUSE the pass's own files
# are about to land, so it is stale before the commit that carries it.  A QUANTITY THAT CANNOT BE
# TRUE AT THE MOMENT IT IS WRITTEN IS NOT A QUANTITY.  A renamed key is one a gate must keep
# refusing for ever; a removed one leaves `T-336`'s arm C with an EMPTY POPULATION BY
# CONSTRUCTION, which is what makes `T-339` a one-constant promotion instead of a widening.
#
# WHY IT TAKES A `--ref`, PINNED.  This file's subject is the CORPUS (`CH-0246`): re-running it
# with a default of `HEAD` re-bases the measurement onto today's corpus and OVERWRITES the record
# instead of checking it.  The ref is an argument, its default is a sha, and the RESOLVED sha is
# recorded as `baselineRef`.
"""Emit gpd/results/T-340-a-working-tree-reading-at-the-emitter.json."""

import argparse
import importlib.util
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_OUT = os.path.join(
    ROOT, "gpd", "results", "T-340-a-working-tree-reading-at-the-emitter.json")

#: `91f9a48` carries this task's Formulate and Plan and is the state the crux was measured at,
#: BEFORE any repair.  Pinned rather than defaulted to `HEAD` (`CH-0246`).
DEFAULT_REF = "91f9a48"


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


census = _load("t340_census", "T-336-pinned-count-census.py")
header = _load("t340_header", "emission_header.py")

#: The kind each file's working-tree reading is, with the EVIDENCE that says so.  A classification
#: is a judgement and it is published with its ground, never as a bare label (`C-0176`).  The keys
#: are result-file basenames without the `.json`.
KINDS = {
    "P-30-queue-row-coverage": (
        "A", "workingTreeRepairedReader is the open-row count AFTER this pass's own repair of the "
             "reader, against the pre-repair reading the same file records"),
    "T-207-format-string-repair": (
        "C", "a prose note that a nine-digit departure is still in the tree; no numeric leaf"),
    "T-250-prose-interpolation-sweep": (
        "A", "afterInTheWorkingTree against beforeAtHead -- the pass's own prose repair"),
    "T-280-debt-line-as-a-ratio": (
        "C", "priceInTheTree is a corpus census this pass does not change, beside atTheBaselineRef"),
    "T-281-name-the-discharge": (
        "A", "preExistingDefectsBefore/AfterOnTheWorkingTree, 21 against 21 -- the repair moved no "
             "pre-existing defect, which is the finding"),
    "T-282-classification-regeneration": (
        "A", "gateWithThePredicateAloneOnTheWorkingTree 9 against 8 at the baseline ref, with the "
             "reason in quietTree"),
    "T-285-a-slug-is-not-a-statement": (
        "A", "gateDefectsBefore/AfterOnTheWorkingTree, 21 against 0 -- the pass's own repair"),
    "T-286-a-regime-is-a-set": (
        "C", "workingTreeCorpus is a census of result files this pass does not change"),
    "T-289-a-verdict-in-the-wrong-column": (
        "A", "atTheWorkingTree against atTheBaselineRef throughout, over the pass's own predicate"),
    "T-292-the-column-repair": (
        "A", "whyTheWorkingTreeReadingDiffers is prose naming which rows moved and why"),
    "T-327-the-resolution-of-the-flatness-census": (
        "C", "atThisPassesTree reads result files this pass does not re-emit; identical to its own "
             "baselineRef reading today, which is luck and not design"),
    "T-332-fifteenth-answers-synthesis": (
        "B", "workingTreeBeforeThisClaimsOwnFiles records the challenge-and-claim census, a "
             "DECLARED registry quantity, at a tree -- and as a HARDCODED literal at that"),
    "T-334-the-gate-census-by-reachability": (
        "B", "atThisPassesTree records the gate-census union and the naming predicate, both "
             "DECLARED registry quantities, at a tree that resolves nowhere"),
}

#: Where a NON-PROSE consumer of a working-tree leaf could live.  Prose -- a claim, a challenge, a
#: task file, a deliverable, a journal entry -- is excluded by declaration: a sentence about the
#: defect is not a reader of it, and counting one would make every repair look load-bearing.
READER_SCOPE = ("src", "tools", "build.gradle.kts")

#: What an occurrence inside `READER_SCOPE` IS, declared BY PATH and with its evidence.  A regex
#: over filenames guessed three of these wrong on its first run -- `tools/T-289-column-history.py`
#: WRITES its key at line 173 and a `.md` template under `tools/` is prose -- so the roles are
#: declared and an occurrence matching no row REFUSES rather than defaulting to `CONSUMER`
#: (`C-0182`: an absence read as an answer).  The refusal is what makes *0 consumers* a partition
#: backed by evidence instead of a verdict handed down by a regular expression.
READER_ROLES = (
    (re.compile(r"-emit-result\.py$"), "EMITTER-WRITES",
     "an emitter, which WRITES the key rather than reading a value from it"),
    (re.compile(r"T-289-column-history\.py$"), "EMITTER-WRITES",
     "T-289's own history record: line 173 writes onTheWorkingTree into it"),
    (re.compile(r"T-336-pinned-count-census\.py$"), "CENSUS-CLASSIFIES",
     "the census: it classifies the key and never consumes the value"),
    (re.compile(r"T-336-mutation-test\.py$"), "CENSUS-CLASSIFIES",
     "the census's own mutation anchors, which quote the rule they replace"),
    (re.compile(r"\.md$"), "PROSE",
     "a markdown template under tools/, prose by the same declaration that excludes gpd/"),
)


def reader_role(path):
    """The declared role of an occurrence, or a REFUSAL -- never a default (`C-0182`)."""
    for pattern, role, evidence in READER_ROLES:
        if pattern.search(path):
            return role, evidence
    raise KeyError(
        "T-340 refuses the unclassified occurrence %r: say what it is -- an emitter writing the "
        "key, a census classifying it, prose, or a CONSUMER whose own answer changes with the "
        "value. A consumer inverts this task's recommendation from REMOVE to RENAME, so it may "
        "not be inferred from a filename" % (path,))


def _walk(node, path=()):
    if isinstance(node, dict):
        for key, value in node.items():
            for item in _walk(value, path + (key,)):
                yield item
    elif isinstance(node, list):
        for index, value in enumerate(node):
            for item in _walk(value, path + (str(index),)):
                yield item
    else:
        yield path, node


#: This file's own basename.  A census over a corpus that contains the census destroys itself
#: (`CH-0182`), and this one does it in the sharpest available way: the emitted record's own
#: `armC/atTheWorkingTree` key MATCHES the working-tree key expression, so the file enters its own
#: population the moment it exists and the count differs between a first emission and a second.
#: The discharge is to NAME the exclusion rather than to rename the key -- the key is honest, and
#: an unnamed exclusion is the thing `C-0182` refuses.
SELF = "T-340-a-working-tree-reading-at-the-emitter"


def population(documents, exclude=(SELF,)):
    """`{basename: {keys, numericLeaves, registryLeaves}}` over `{name: parsed json}`."""
    out = {}
    for name in sorted(documents):
        if name in exclude:
            continue
        keys, leaves, floats, registry = set(), 0, 0, 0
        for path, value in _walk(documents[name]):
            if not any(census._WORKING_TREE_KEY.search(key) for key in path):
                continue
            keys.update(key for key in path if census._WORKING_TREE_KEY.search(key))
            # Two counts, because they differ and a census must say which it published: the arms
            # of `T-336` walk INTEGER leaves, and a hand census over the working tree that counted
            # floats too reported 199 where the integer count at this ref is 155.
            if isinstance(value, (int, float)) and not isinstance(value, bool):
                floats += 1
            if isinstance(value, int) and not isinstance(value, bool):
                leaves += 1
                if census.registry_quantity_of(path) is not None:
                    registry += 1
        if keys:
            out[name] = {"keys": sorted(keys), "numericLeaves": leaves,
                         "numericLeavesIncludingFloats": floats, "registryLeaves": registry}
    return out


def _documents_at(tree):
    out = {}
    for name in census._result_names(tree):
        text = tree.read(os.path.join(census.RESULTS, name))
        if text is None:
            continue
        try:
            out[name[:-5] if name.endswith(".json") else name] = json.loads(text)
        except ValueError:
            continue
    return out


def readers(root=ROOT):
    """Every NON-PROSE occurrence of a working-tree key name, with what it is.

    An emitter WRITING the key and the census CLASSIFYING it are not consumers of the value: a
    consumer is something whose own answer changes with the number.  Both are listed anyway, so
    the zero is a partition and not an assertion.
    """
    rows = []
    for name in census.WORKING_TREE_KEY_NAMES:
        for scope in READER_SCOPE:
            target = os.path.join(root, scope)
            if not os.path.exists(target):
                continue
            found = subprocess.run(["grep", "-rIn", "--", name, target],
                                   capture_output=True, text=True).stdout.splitlines()
            for line in found:
                path = os.path.relpath(line.split(":", 1)[0], root)
                role, evidence = reader_role(path)
                rows.append({"key": name, "path": path, "role": role, "evidence": evidence})
    return rows


def arm_c(tree):
    """Arm C's population at a state: the registry quantities recorded at an uncommitted tree."""
    return [{"file": name, "path": path, "value": value, "quantity": q.name}
            for name, path, value, q in census.working_tree_records(tree) if q is not None]


def build(ref=DEFAULT_REF):
    baseline = census.Tree(ref)
    tree = census.Tree()
    before = population(_documents_at(baseline))
    after = population(_documents_at(tree))
    reader_rows = readers()
    consumers = [row for row in reader_rows if row["role"] == "CONSUMER"]
    by_kind = {}
    for name in before:
        by_kind.setdefault(KINDS.get(name, ("UNCLASSIFIED", ""))[0], []).append(name)
    document = {
        "task": "T-340",
        "title": ("whether a working-tree reading has any legitimate use - measured over every "
                  "committed result file, and answered BOTH ways by what the reading is OF"),
        "baselineRef": baseline.ref,
        "baselineRefRequested": ref,
        "parameters": {
            "workingTreeKeyExpression": census._WORKING_TREE_KEY.pattern,
            "howTheExpressionWasDerived": (
                "from the 23 such key names the corpus carries at 91f9a48, listed in "
                "tools/T-336-pinned-count-census.py's WORKING_TREE_KEY_NAMES and held against all "
                "23 by a named test -- a predicate is dated by its premise set (CH-0182), so what "
                "it was measured against travels with it"),
            "kindRule": (
                "A: the AFTER half of a before/after measurement of a repair the pass performs, "
                "un-pinnable because an emitter runs before its own commit exists. "
                "B: a rival ABSOLUTE reading of a declared registry quantity, whose pinned "
                "counterpart already answers the same question. "
                "C: a reading of a corpus the pass does not change, pinnable at its own commit"),
            "readerScope": list(READER_SCOPE),
            "selfExclusion": (
                "this file is excluded from its own population by name: its own "
                "armC/atTheWorkingTree key matches the working-tree key expression, so it enters "
                "the census the moment it exists and the count would differ between a first "
                "emission and a second. CH-0182, discharged by NAMING the exclusion"),
            "whyProseIsExcludedFromTheReaderScope": (
                "a sentence ABOUT the defect is not a reader OF it, and counting one would make "
                "every repair look load-bearing"),
            "whyThisRef": (
                "91f9a48 carries this task's Formulate and Plan and is the state the crux was "
                "measured at, BEFORE any repair. Pinned rather than defaulted to HEAD: a "
                "corpus-subject emitter defaulting to a moving ref re-bases its own measurement "
                "between the draft and the emission (CH-0246)"),
            "reRunHazard": (
                "CH-0246: this file's subject is the corpus, so re-running it as a control on a "
                "later change re-bases the measurement and OVERWRITES the record rather than "
                "checking it. Re-emit only deliberately, at the named ref"),
        },
        "theCrux": {
            "question": ("does a working-tree reading have any legitimate use -- if it does the "
                         "fix is a key a gate can refuse, if it does not the fix is to stop "
                         "emitting it"),
            "answer": ("BOTH, and the discriminator is what the reading is OF rather than where "
                       "it is written. Kind A is legitimate and un-pinnable by construction; kind "
                       "B is a rival reading of a count the deliverable prints about itself, and "
                       "for it the answer is REMOVAL"),
            "whyRemovalAndNotARenamedKey": (
                "a synthesis wanting to state what its own pass will look like is asking for a "
                "number that is unpinnable PRECISELY BECAUSE the pass's own files are about to "
                "land, so it is stale before the commit that carries it. A quantity that cannot "
                "be true at the moment it is written is not a quantity. A renamed key is one a "
                "gate must keep refusing for ever; a removed one leaves arm C an EMPTY POPULATION "
                "BY CONSTRUCTION"),
            "andTheStateITWASREACHINGFORBECOMESNAMEABLEONECOMMITLATER": (
                "an emitter cannot name its own commit, but a LATER pass can -- which is why the "
                "replacement is not a rename but a different, pinned measurement: the same census "
                "at bee6b06 for T-332 and at bb678d2 for T-334"),
        },
        "populationAtTheRef": {
            "files": len(before),
            "distinctKeyNames": len(set(k for row in before.values() for k in row["keys"])),
            "numericLeaves": sum(row["numericLeaves"] for row in before.values()),
            "numericLeavesIncludingFloats":
                sum(row["numericLeavesIncludingFloats"] for row in before.values()),
            "registryLeaves": sum(row["registryLeaves"] for row in before.values()),
            "byKind": dict((kind, sorted(names)) for kind, names in sorted(by_kind.items())),
            "perFile": dict(
                (name, dict(row, kind=KINDS.get(name, ("UNCLASSIFIED", ""))[0],
                            evidence=KINDS.get(name, ("", "no evidence line declared"))[1]))
                for name, row in sorted(before.items())),
        },
        "populationAfterTheRepair": {
            "files": len(after),
            "numericLeaves": sum(row["numericLeaves"] for row in after.values()),
            "registryLeaves": sum(row["registryLeaves"] for row in after.values()),
            "whatChanged": sorted(set(before) - set(after)) or sorted(
                name for name in before
                if name in after and before[name]["registryLeaves"] != after[name]["registryLeaves"]),
        },
        "readers": {
            "nonProseConsumers": len(consumers),
            "consumers": consumers,
            "occurrencesByRole": dict(
                (role, sum(1 for row in reader_rows if row["role"] == role))
                for role in sorted(set(row["role"] for row in reader_rows))),
            "whatThisDecides": (
                "removing a working-tree block breaks nothing downstream, so the repair is a "
                "deletion rather than a schema migration -- and if this number were not zero the "
                "recommendation would invert from REMOVE to RENAME"),
        },
        "armC": {
            "predicate": ("no result file records a DECLARED REGISTRY QUANTITY under a "
                          "working-tree key -- scoped by the KEY and not by CENSUS_MARKERS"),
            "whyKeyScopedAndNotFamilyScoped": (
                "gpd/results/T-327-the-resolution-of-the-flatness-census.json carries a 173-entry "
                "atThisPassesTree block and none of the three census markers, so a family-scoped "
                "arm would have had a measured hole on its first day"),
            "atTheRef": arm_c(baseline),
            "atTheWorkingTree": arm_c(tree),
            "gatedAt": "tools/T-336-pinned-count-census.py --check",
        },
        "theReproducibilityTest": {
            "rule": ("gpd/README.md requires a result file to be reproducible from it alone; "
                     "re-run the emitter at the file's own recorded baselineRef and diff"),
            "beforeTheRepair": [
                {"file": "T-334-the-gate-census-by-reachability.json", "ref": "d9a3522",
                 "movedLeaves": 55, "insideAWorkingTreeBlock": 55, "outside": 0},
                {"file": "T-327-the-resolution-of-the-flatness-census.json", "ref": "86b3bbd",
                 "movedLeaves": 0, "insideAWorkingTreeBlock": 0, "outside": 0},
                {"file": "T-332-fifteenth-answers-synthesis.json", "ref": "d7b7074",
                 "movedLeaves": None, "insideAWorkingTreeBlock": None, "outside": None,
                 "why": ("the emitter REFUSED to run: 1 of its 26 declared AFTER anchors no longer "
                         "occurs, and it is the passage CH-0292's own repair struck")},
            ],
            "afterTheRepair": [
                {"file": "T-334-the-gate-census-by-reachability.json", "ref": "d9a3522",
                 "removed": 155, "added": 157, "changed": 4,
                 "removedOutsideAWorkingTreeBlock": 0,
                 "what": ("the atThisPassesTree block, replaced by the same census at bb678d2; the "
                          "four changed leaves are armOneAtThreeRefs[2], whose two counts go 12 to "
                          "13 exactly as CH-0293 says they must")},
                {"file": "T-332-fifteenth-answers-synthesis.json", "ref": "d7b7074",
                 "removed": 9, "added": 11, "changed": 2,
                 "removedOutsideAWorkingTreeBlock": 0,
                 "what": ("the two tree blocks, replaced by the same counts at bee6b06; the two "
                          "changed leaves are prose findings that quoted the tree reading")},
            ],
        },
        "theTwoRecordsWereNOTALIKE": {
            "T-332": ("the removed value was a HARDCODED literal -- 247/214/461 typed into the "
                      "emitter -- so no --ref reproduces it and no --ref refutes it, and it "
                      "occurs at 0 of the repository's 298 commits (CH-0292). Its checker-census "
                      "tree reading, by contrast, was RIGHT: 18/21/12/51 at the tree and the same "
                      "at bee6b06"),
            "T-334": ("the removed value was a live derivation, and four of its thirteen leaves "
                      "were WRONG at the moment they were committed, because a sibling agent "
                      "added and wired the thirteenth helper harness in that same commit "
                      "(CH-0293). Its HEADLINE, 46, was right at bb678d2"),
            "theLesson": ("one was right and one was wrong and NO READER COULD TELL WHICH, because "
                          "neither named a state. Unpinnable is a defect independently of whether "
                          "the value happens to be correct"),
        },
        "findings": [],
    }
    document["findings"] = [
        "The crux is answered BOTH ways and the split is measured: {} files carry a working-tree "
        "reading, {} of them legitimately (kind A, the after half of a repair the pass performs, "
        "which cannot be pinned because an emitter runs before its own commit exists) and {} of "
        "them as a rival reading of a count the deliverable prints about itself.".format(
            len(before), len(by_kind.get("A", [])), len(by_kind.get("B", []))),
        "The row said TWO records; the population is {} files, {} distinct key names and {} "
        "numeric leaves, and the census that raised the row could see only four of the files "
        "because it is scoped by CENSUS_MARKERS.".format(
            len(before),
            len(set(k for row in before.values() for k in row["keys"])),
            sum(row["numericLeaves"] for row in before.values())),
        "Non-prose consumers of a working-tree leaf: {}. Nothing in src/, tools/ or "
        "build.gradle.kts reads one except the emitter that writes it and the census that "
        "classifies it, so removal is a deletion and not a schema migration.".format(
            len(consumers)),
        "The mechanical test needs no taste at all: a working-tree block is exactly the part of a "
        "corpus-subject file that makes it irreproducible from itself. T-334 re-run at its own "
        "baselineRef moved 55 leaves, 55 of 55 inside a working-tree block, its pinned half at 0.",
        "A quantity that cannot be true at the moment it is written is not a quantity, and the "
        "state it was reaching for becomes nameable one commit later -- which is why the "
        "replacement is a different PINNED measurement and not a renamed key.",
        "Arm C's population goes from {} leaves at the ref to {} at the repaired tree, which is "
        "what makes T-339 a one-constant promotion rather than a widening.".format(
            len(arm_c(baseline)), len(arm_c(tree))),
    ]
    return header.with_emission_header(document, "none", regime=[])


def _self_test():
    checks = []

    def ok(name, passed):
        checks.append((name, bool(passed)))

    kind_a = {"baselineRef": "0" * 40,
              "measurement": {"gateDefectsBeforeOnTheWorkingTree": 21,
                              "gateDefectsAfterOnTheWorkingTree": 0}}
    kind_b = {"baselineRef": "0" * 40,
              "selfDescribingCounts": {"workingTreeBeforeThisClaimsOwnFiles":
                                       {"challenges": 247, "claims": 214}}}
    pinned = {"baselineRef": "0" * 40, "selfDescribingCounts": {"challenges": {"atRef": 246}}}

    ok("T-340-emit the population counts a file's working-tree keys and its numeric leaves",
       population({"a": kind_a})["a"] == {
           "keys": ["gateDefectsAfterOnTheWorkingTree", "gateDefectsBeforeOnTheWorkingTree"],
           "numericLeaves": 2, "numericLeavesIncludingFloats": 2, "registryLeaves": 0})
    ok("T-340-emit the integer count and the count including floats are BOTH published, because "
       "they differ and a census must say which figure it is quoting",
       population({"f": {"priceInTheTree": {"n": 3, "ratio": 1.5}}})["f"]
       == {"keys": ["priceInTheTree"], "numericLeaves": 1,
           "numericLeavesIncludingFloats": 2, "registryLeaves": 0})
    ok("T-340-emit an occurrence matching no declared role REFUSES rather than defaulting to "
       "CONSUMER, because a consumer inverts the recommendation",
       _refuses(lambda: reader_role("tools/something-nobody-classified.py")))
    ok("T-340-emit every declared role carries its evidence, and the roles are the four declared",
       all(evidence.strip() for _p, _r, evidence in READER_ROLES)
       and set(role for _p, role, _e in READER_ROLES)
       == {"EMITTER-WRITES", "CENSUS-CLASSIFIES", "PROSE"})
    ok("T-340-emit a history tool that WRITES the key is not a consumer of it -- a filename regex "
       "read tools/T-289-column-history.py as a consumer on its first run",
       reader_role("tools/T-289-column-history.py")[0] == "EMITTER-WRITES"
       and reader_role("tools/C-0156-claim-template.md")[0] == "PROSE")
    ok("T-340-emit a file carrying NO working-tree key is not in the population at all",
       population({"a": pinned}) == {})
    ok("T-340-emit this file is excluded from its own population BY NAME, because its own armC "
       "key matches the expression and the census would otherwise destroy itself (CH-0182)",
       population({SELF: kind_a}) == {} and population({SELF: kind_a}, exclude=())[SELF])
    ok("T-340-emit a kind-B file's registry leaves are counted and a kind-A file's are not -- "
       "which is the whole of the discriminator",
       population({"b": kind_b})["b"]["registryLeaves"] == 2
       and population({"a": kind_a})["a"]["registryLeaves"] == 0)
    ok("T-340-emit every classified file carries an EVIDENCE line, never a bare label",
       all(kind in ("A", "B", "C") and evidence.strip()
           for kind, evidence in KINDS.values()))
    ok("T-340-emit the kind split over the declared table is 7 A, 2 B, 4 C",
       [sum(1 for kind, _ in KINDS.values() if kind == letter) for letter in "ABC"] == [7, 2, 4])
    ok("T-340-emit prose is excluded from the reader scope by DECLARATION, so a sentence about "
       "the defect is never counted as a reader of it",
       "gpd" not in READER_SCOPE and set(READER_SCOPE) == {"src", "tools", "build.gradle.kts"})
    ok("T-340-emit the working-tree key expression is the census's own, not a second copy",
       census._WORKING_TREE_KEY.pattern
       == "WorkingTree|workingTree|ThisPassesTree|InTheTree|quietTree")
    ok("T-340-emit the default ref is a pinned sha and never a moving HEAD (CH-0246)",
       DEFAULT_REF != "HEAD" and re.match(r"^[0-9a-f]{7,40}$", DEFAULT_REF))

    for name, passed in checks:
        print("%s  %s" % ("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# %d self-test(s), %d failure(s)" % (len(checks), len(failed)))
    return 1 if failed else 0


def _refuses(thunk):
    try:
        thunk()
    except KeyError:
        return True
    return False


def repository_available(ref=DEFAULT_REF):
    try:
        census.Tree(ref)
    except Exception:
        return False
    return True


def main(argv=None):
    parser = argparse.ArgumentParser(
        description="emit gpd/results/T-340-a-working-tree-reading-at-the-emitter.json")
    parser.add_argument("--ref", default=DEFAULT_REF,
                        help="the corpus state to measure at (default: %s, pinned)" % DEFAULT_REF)
    parser.add_argument("--out", default=DEFAULT_OUT, help="where to write the result file")
    parser.add_argument("--self-test", dest="self_test", action="store_true",
                        help="named self-tests over in-memory fixtures; reads no git")
    arguments = parser.parse_args(argv)
    if arguments.self_test:
        return _self_test()
    if not repository_available(arguments.ref):
        sys.stderr.write(
            "T-340-emit: REFUSING to emit -- no git repository holding %s under %s. A "
            "corpus-subject result file must name the state it measured (CH-0246), so this path "
            "does NOT degrade; the self-test arms are git-free and do run.\n"
            % (arguments.ref, ROOT))
        return 2
    document = build(arguments.ref)
    with open(arguments.out, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=1, ensure_ascii=False)
        handle.write("\n")
    print("written to %s" % os.path.relpath(arguments.out, ROOT))
    print("# %d file(s) carry a working-tree reading at %s; arm C: %d leaf(s) there, %d at the "
          "repaired tree" % (document["populationAtTheRef"]["files"], arguments.ref,
                             len(document["armC"]["atTheRef"]),
                             len(document["armC"]["atTheWorkingTree"])))
    return 0


if __name__ == "__main__":
    sys.exit(main())
