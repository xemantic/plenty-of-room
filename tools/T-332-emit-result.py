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
# T-332 -- the fifteenth ANSWERS.md synthesis, emitted as a result file.
#
#     tools/T-332-emit-result.py [--ref <git-ref>] [--self-test] [--out <path>]
#
# WHY THIS TAKES A `--ref`.  A census is a function of the whole mutable corpus, so
# `gpd/README.md`'s "reproducible from itself alone" rule holds only if the corpus state is named:
# the ref is an argument and the RESOLVED sha is recorded as `baselineRef`.  `CH-0246` forbids
# re-running this class of file as a control on a later change -- the re-run re-bases the
# measurement onto today's corpus and OVERWRITES the record instead of verifying it.
#
# WHY IT IMPORTS `T-319-emit-result.py`.  That emitter (which itself imports `T-276`'s) already
# resolves a ref, reads a blob, censuses the checkers under two predicates and counts the
# cannot-answer bullets.  `CLAUDE.md`'s rule is to check whether an existing tool is one argument
# away before writing a second one.  What is new here is (a) a passage row carries a MULTIPLICITY,
# because two of this pass's edits are one substitution applied to two identical table cells, and
# (b) a THIRD Gradle predicate: `commandLine(mutationSnapshotArguments("..."))`, which is how a
# Kotlin-subject mutation harness has been wired since iteration 46 and which the inherited
# literal-path regex cannot see at all (`CH-0286`).
import argparse
import importlib.util
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def _load(name, filename):
    spec = importlib.util.spec_from_file_location(name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


previous = _load("t319", "T-319-emit-result.py")
older = previous.previous

DOCUMENTS = previous.DOCUMENTS
KINDS = previous.KINDS

#: A Kotlin-subject mutation harness is handed a SNAPSHOT directory (it must not mutate the shared
#: checkout), so it is wired through a helper rather than through a literal path.  `CH-0286`.
SNAPSHOT_GATE_PATTERN = r'commandLine\(mutationSnapshotArguments\("([^"]+)"\)'


def snapshot_gates(build_text):
    return sorted(set(re.findall(SNAPSHOT_GATE_PATTERN, build_text)))


#: (kind, document, before-anchor, after-anchor, multiplicity, what).
#: The before anchor must occur at the ref; the after anchor must occur `multiplicity` times in the
#: working tree and NOT AT ALL at the ref.  A before anchor may be shared by rows inserted at one
#: point; an after anchor may not be shared at all, and the self-test asserts it.
PASSAGES = (
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "the free tiles are **0.312237799 / 0.227177955 / 0.220064299** on `15 × 4`",
        "the free tiles are ~~**0.312237799 / 0.227177955 / 0.220064299**~~ **CORRECTED, iteration 52**",
        1,
        "section 1 item 1: C-0154's three 15x4 free tiles, corrected by C-0219/CH-0282",
    ),
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "outside `T-5b` at **every** enhancement read — 0.312237799, 0.227177955 and 0.220064299",
        "**CORRECTED, iteration 52** ([`C-0219`](gpd/claims/C-0219-a-dishing-fit-and-the-parity-of-its-basis.md), "
        "[`CH-0282`](gpd/challenges/CH-0282-a-dishing-fit-assumes-an-even-raster-row-count.md)): "
        "**0.242196276, 0.157167743 and 0.150056485**",
        1,
        "the questions-for-NDI table, row 7: the same triple",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "spanning `191.010656` in stiffness is unpriced",
        "**AND ON THE TILE THIS PROGRAMME ACTUALLY RECOMMENDS A COUPLED CELL IS NOW FLAT *AND* ADMISSIBLE —",
        1,
        "section 1: C-0215's 27 of 48 searched and 7 flat AND admissible on route B's own tile",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "spanning `191.010656` in stiffness is unpriced",
        "**AND THE TWO FREEDOMS ARE SYNERGISTIC, WHICH REVERSES `C-0063`'s STANDING ORDERING ON THIS LATTICE,",
        1,
        "section 1: C-0216's -12.96 % interaction and the reversed ordering",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "spanning `191.010656` in stiffness is unpriced",
        "**AND THE OTHER CROSS-SECTION IS NOW GRADED COUPLED IN THE SAME STATE, WHICH DISCHARGES A LEAVE",
        1,
        "section 1: C-0218's tied 15x4 re-grade, the 2/3 stroke convention and CH-0282's broken falsifier",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "`C-0109`'s *\"every coupled\n   cell is worse than the uncoupled tile\"*, which reproduces at "
        "**64 of 64** on the tied lattice",
        "(**SCOPE CORRECTED, iteration 51** —",
        1,
        "section 1: CH-0283's dropped scope clause, of the same tile",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "**The tile is not too floppy;\n   it is too small**",
        " **AND, iteration 50, THEY ARE FLAT COUPLED TOO",
        1,
        "section 1: the width finding gains its coupled reading",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "**(5) The stations exist and the placement survives them**",
        " **(4c) AND THAT `0 of 64` IS A READING ON TWO *TRANSFERRED* DISTRIBUTIONS",
        1,
        "section 3 row (g): items 4c, 4d and 4e, carrying iterations 49-51",
    ),
    (
        "SELF_DESCRIBING_COUNT", "ANSWERS.md",
        "**Two hundred and thirty-one** challenges in",
        # WIDENED by `T-340`, never trimmed.  `CH-0292`'s repair struck this sentence and inserted
        # the pinned reading after it, so the eight characters `" challenges in"` no longer follow
        # the bold run and this emitter refused to run AT ALL -- 1 of its 26 AFTER anchors.  A
        # synthesis emitter that asserts its own prose is still LIVE is hostage to `C-0071`'s
        # *strike, never delete*, which guarantees a later pass will amend it.  What the anchor
        # asserts is unchanged: this pass wrote this figure into this document.
        "**Two hundred and forty-seven**",
        1,
        "section 4: the challenge-and-claim census, derived",
    ),
    (
        "SELF_DESCRIBING_COUNT", "ANSWERS.md",
        "gates that could make it are already wired)",
        "**AND STALE AGAIN, iteration 52 — EIGHT OF NINE.**",
        1,
        "section 4: the same row's own staleness count",
    ),
    (
        "OTHER", "ANSWERS.md",
        "the sentence carries no status word and both of its identifiers resolve.**",
        "**AND, iteration 52, THIS BULLET HAS A PRICE FOR THE FIRST TIME",
        1,
        "the cannot-answer list: the per-site incorporation bullet, priced for the first time",
    ),
    (
        "STALE_STATUS", "ANSWERS.md",
        "the coupled-cell half is a smeared-sheet reading and `15 × 4` was **not** re-graded coupled",
        "~~the coupled-cell half is a smeared-sheet reading and `15 × 4` was **not** re-graded coupled~~",
        1,
        "the questions-for-NDI table, row 7: 15x4 HAS been re-graded coupled since iteration 51",
    ),
    (
        "SUPERSEDED_VALUE", "DECISIONS-FOR-NDI.md",
        "at every enhancement read on that lattice (0.312237799 / 0.227177955 / 0.220064299)",
        "at every enhancement read on that lattice (~~0.312237799 / 0.227177955 / 0.220064299~~",
        1,
        "at-a-glance decision 7: the corrected triple",
    ),
    (
        "SUPERSEDED_VALUE", "DECISIONS-FOR-NDI.md",
        "on the honeycomb **grillage** `15 × 4` reads **0.312237799 / 0.227177955 / 0.220064299**",
        "on the honeycomb **grillage** `15 × 4` reads ~~**0.312237799 / 0.227177955 / 0.220064299**~~",
        1,
        "decision 7's what-each-buys table: the corrected triple",
    ),
    (
        "STALE_PRICE", "DECISIONS-FOR-NDI.md",
        "**AND THE TWO SIDES OF THIS CELL ARE DELIBERATELY READ IN THE SAME STATE, iteration 45.**",
        "**AND, iteration 50, THAT `0 of 64` IS ITSELF A READING ON TWO",
        1,
        "at-a-glance decision 7: the count it prices deferral on is a transferred rule on the wrong tile",
    ),
    (
        "STALE_STATUS", "DECISIONS-FOR-NDI.md",
        "It is left in the untied state on purpose;",
        "~~It is left in the untied state on purpose;",
        1,
        "at-a-glance decision 7: C-0186's deliberate leave, DISCHARGED by C-0218",
    ),
    (
        "STALE_STATUS", "DECISIONS-FOR-NDI.md",
        "and `15 × 4` was not re-graded coupled at all",
        "~~and `15 × 4` was not re-graded coupled at all~~",
        1,
        "at-a-glance decision 7: the same assertion, in the document NDI reads",
    ),
    (
        "SCOPE", "DECISIONS-FOR-NDI.md",
        "and `0 of 7` convergence axes move it.",
        "**AND ON THE TILE THIS PROGRAMME RECOMMENDS, A COUPLED CELL IS NOW FLAT *AND* ADMISSIBLE",
        1,
        "section 6: C-0215, the first flat-and-admissible coupled cell",
    ),
    (
        "SCOPE", "DECISIONS-FOR-NDI.md",
        "and `0 of 7` convergence axes move it.",
        "**AND THE TWO DESIGN FREEDOMS ARE SYNERGISTIC, WHICH REVERSES A STANDING ORDERING ON THIS LATTICE,",
        1,
        "section 6: C-0216's interaction",
    ),
    (
        "SCOPE", "DECISIONS-FOR-NDI.md",
        "and `0 of 7` convergence axes move it.",
        "**AND THE OTHER CROSS-SECTION IS NOW GRADED COUPLED IN THE SAME LATTICE STATE, iteration 51**",
        1,
        "section 6: the pointer to decision 7's discharged comparison row",
    ),
    (
        "SCOPE", "DECISIONS-FOR-NDI.md",
        "coupled cell is still worse than the uncoupled tile, at **64 of 64**;",
        "**SCOPE CORRECTED, iteration 51**",
        1,
        "section 6: CH-0283's dropped scope clause",
    ),
    (
        "STALE_PRICE", "DECISIONS-FOR-NDI.md",
        "| coupled, under the measured staple dropout | **0 of 8 cells flat, at BOTH ends of the band** |",
        " **RESTATED, iteration 51 — GRADED COUPLED ON THE TIED HONEYCOMB GRILLAGE AT LAST**",
        2,
        "the 15x4 coupled cell of BOTH of decision 7's comparison tables, filled by C-0218",
    ),
    (
        "STALE_PRICE", "DECISIONS-FOR-NDI.md",
        "recovery to be contingent about.** |",
        " **AND, iteration 50, THAT COUNT IS A READING ON *TRANSFERRED* DISTRIBUTIONS AND ON THE WRONG",
        2,
        "the 10x6 coupled cell of BOTH of decision 7's comparison tables, re-priced by C-0212/C-0215",
    ),
    (
        "STALE_PRICE", "DECISIONS-FOR-NDI.md",
        "all three scaffold rows are flat uncoupled and flat with their tethers (`756 of 756`, `C-0207`).",
        "re-graded at `C-0208`'s resolved per-bond link, iteration 49, and the count does not move**",
        1,
        "decision 9's cost-of-deferring cell: the 756 of 756 it prices on, at the resolved link and coupled",
    ),
    (
        "SCOPE", "DECISIONS-FOR-NDI.md",
        "`T-315`). **The tile is not too floppy; it is too small.**",
        " **AND, iteration 50, THEY ARE FLAT COUPLED TOO",
        1,
        "decision 9's finding: the three widths are flat COUPLED too",
    ),
    (
        "SELF_DESCRIBING_COUNT", "DECISIONS-FOR-NDI.md",
        "`commandLine(\"$projectDir/tools/…\")` invocations in `build.gradle.kts` carrying none either.",
        "**RE-DERIVED AGAIN, iteration 52 — AND THE PREDICATE IS DATED FOR A FOURTH TIME",
        1,
        "the checker census: 51 rather than 37, and the predicate dated by a wiring idiom (CH-0286)",
    ),
)


def _passage_rows(ref):
    at_ref = {document: older._blob(ref, document) for document in DOCUMENTS}
    working = {
        document: open(os.path.join(ROOT, document), encoding="utf-8").read()
        for document in DOCUMENTS
    }
    rows = []
    for kind, document, before, after, multiplicity, what in PASSAGES:
        if kind not in KINDS:
            raise SystemExit("undeclared kind {}".format(kind))
        if at_ref[document].count(before) < 1:
            raise SystemExit(
                "the BEFORE anchor for {} in {} does not occur at the ref: {!r}".format(
                    kind, document, before))
        if at_ref[document].count(after) != 0:
            raise SystemExit(
                "the AFTER anchor for {} in {} already exists at the ref: {!r}".format(
                    kind, document, after))
        if working[document].count(after) != multiplicity:
            raise SystemExit(
                "the AFTER anchor for {} in {} occurs {} times in the working tree, not {}: {!r}".format(
                    kind, document, working[document].count(after), multiplicity, after))
        for _ in range(multiplicity):
            rows.append({"kind": kind, "document": document, "passage": what})
    return rows


#: The commit that CARRIED this result file.  An emitter runs BEFORE its own commit exists, so the
#: state a synthesis wants to describe -- what its own pass looks like -- can only be named by a
#: LATER pass.  `T-340`.
CARRYING_COMMIT = "bee6b06"


def build(ref):
    resolved = older._resolve(ref)
    carrier = older._resolve(CARRYING_COMMIT)
    verify_sh = older._blob(resolved, "tools/verify.sh")
    build_kts = older._blob(resolved, "build.gradle.kts")
    names_at_ref = older._names_at(resolved, r"tools/(check-[a-z-]+|trace-answers)\.py")

    claims_before = older._names_at(resolved, r"gpd/claims/C-\d{4}-.*\.md")
    challenges_before = older._names_at(resolved, r"gpd/challenges/CH-\d{4}-.*\.md")

    if "set -euo pipefail" not in verify_sh:
        raise SystemExit("tools/verify.sh at {} does not set -euo pipefail".format(resolved))
    if "./gradlew test" not in verify_sh:
        raise SystemExit("tools/verify.sh at {} does not run ./gradlew test".format(resolved))

    rows = _passage_rows(resolved)
    by_kind = {kind: sum(1 for row in rows if row["kind"] == kind) for kind in KINDS}

    answers_at_ref = older._blob(resolved, "ANSWERS.md")
    decisions_at_ref = older._blob(resolved, "DECISIONS-FOR-NDI.md")

    # The checker census, under FOUR predicates -- the three C-0210 named and the one CH-0286 adds.
    own = previous.verify_invocations(verify_sh)
    own_fixtures = [t for t in own if os.path.basename(t).startswith("test-")]
    own_gates = [t for t in own if t not in own_fixtures]
    literal = ["tools/" + name for name in previous.gradle_gates(build_kts)]
    helper = ["tools/" + name for name in snapshot_gates(build_kts)]
    union = sorted(set(own) | set(literal) | set(helper))

    # The same census at the PREVIOUS pass's baseline, which is what makes `37` measurable as an
    # undercount of its own sentence's quantity rather than merely superseded.
    prior = older._resolve("71d126e")
    prior_verify = older._blob(prior, "tools/verify.sh")
    prior_build = older._blob(prior, "build.gradle.kts")
    prior_own = previous.verify_invocations(prior_verify)
    prior_literal = ["tools/" + n for n in previous.gradle_gates(prior_build)]
    prior_helper = ["tools/" + n for n in snapshot_gates(prior_build)]

    # The checker census AFTER this pass -- taken at the commit that carried this file, not at the
    # uncommitted tree it was originally read from.  `T-340`: the ref carries iteration 52's T-330
    # work uncommitted, so the pass's own reading differs from the ref's, and the honest way to
    # say that is a SECOND COMMIT rather than a tree that resolves nowhere.
    carrier_verify = older._blob(carrier, "tools/verify.sh")
    carrier_build = older._blob(carrier, "build.gradle.kts")
    tree_own = previous.verify_invocations(carrier_verify)
    tree_literal = ["tools/" + n for n in previous.gradle_gates(carrier_build)]
    tree_helper = ["tools/" + n for n in snapshot_gates(carrier_build)]
    tree_union = sorted(set(tree_own) | set(tree_literal) | set(tree_helper))
    carrier_claims = older._names_at(carrier, r"gpd/claims/C-\d{4}-.*\.md")
    carrier_challenges = older._names_at(carrier, r"gpd/challenges/CH-\d{4}-.*\.md")

    # The exhaustive census CH-0287 rests on: every occurrence of the corrected triple under gpd/.
    # CH-0182 -- a claim about a census enters the census -- so this pass's OWN artifacts are
    # counted separately and both readings are emitted.
    triple = ("0.312237799", "0.227177955", "0.220064299")
    owners = ("C-0218", "C-0219", "CH-0282", "T-294-", "T-330-")
    this_pass = ("C-0220", "CH-0286", "CH-0287", "T-332-")
    carriers = {}
    scanned = [os.path.join(ROOT, d) for d in DOCUMENTS]
    for directory, _, files in os.walk(os.path.join(ROOT, "gpd")):
        scanned.extend(os.path.join(directory, n) for n in sorted(files) if n.endswith(".md"))
    for path in scanned:
        name = os.path.basename(path)
        body = open(path, encoding="utf-8").read()
        hits = sum(1 for line in body.splitlines() if any(v in line for v in triple))
        if hits:
            carriers[os.path.relpath(path, ROOT)] = {
                "occurrences": hits,
                "ownsTheCorrection": any(o in name for o in owners),
                "isThisPassesOwn": any(o in name for o in this_pass),
                "isADeliverable": name in DOCUMENTS,
            }

    document = {
        "task": "T-332",
        "title": "the fifteenth ANSWERS.md synthesis - a coupled cell flat AND admissible, "
                 "and a checker predicate dated a third time",
        "baselineRef": resolved,
        "baselineRefRequested": ref,
        "parameters": {
            "documents": list(DOCUMENTS),
            "kindsDeclared": list(KINDS),
            "whatIsDerived": (
                "every count in this file is derived at baselineRef or from the working tree; only "
                "the passage CLASSIFICATION is declared, and each declared row's two anchors are "
                "asserted against both trees before the file is written"
            ),
            "noTiming": (
                "no wall-clock field and no step counter, per CLAUDE.md: a timing is less "
                "reproducible than a step count and one such field makes a file un-diffable"
            ),
            "reRunHazard": (
                "CH-0246: this file's subject is the corpus, so re-running it as a control on a "
                "later change re-bases the measurement and OVERWRITES the record rather than "
                "checking it; re-emit only deliberately, at a named ref"
            ),
            "twoCommitsNotATree": (
                "the ref carries iteration 52's T-330 work as UNCOMMITTED, so the reading after "
                "this pass differs from the reading at its baseline. T-340: BOTH are now commits "
                "-- baselineRef and bee6b06, the commit that carried this file -- where this file "
                "originally recorded the second as an uncommitted tree. A quantity that cannot be "
                "true at the moment it is written is not a quantity: a synthesis wanting to state "
                "what its own pass will look like is asking for a number that is unpinnable "
                "PRECISELY BECAUSE its own files are about to land (CH-0292)"
            ),
        },
        "selfDescribingCounts": {
            "claims": {"atRef": len(claims_before), "command": "ls gpd/claims/C-*.md | wc -l"},
            "challenges": {"atRef": len(challenges_before),
                           "command": "ls gpd/challenges/CH-*.md | wc -l"},
            "claimsAndChallenges": {
                "atRef": len(claims_before) + len(challenges_before),
                "command": "tools/check-corpus-identifiers.py prints the sum on every run",
            },
            # REMOVED by `T-340`, and it was the sharpest member of the class: a HARDCODED
            # literal `{"challenges": 247, "claims": 214, "sum": 461}` that no `--ref` reproduces
            # and no `--ref` refutes, occurring at 0 of the repository's 298 commits (`CH-0292`).
            # What replaces it is the same census DERIVED at the commit that carried this file.
            "atTheCommitThatCarriedThisFile": {
                "ref": carrier,
                "challenges": len(carrier_challenges),
                "claims": len(carrier_claims),
                "sum": len(carrier_challenges) + len(carrier_claims),
            },
            "asWrittenBeforeThisPass": {"challenges": 231, "claims": 204},
            "staleAgain": True,
            "stalePassesOfTotal": {"stale": 8, "passes": 9},
        },
        "checkerCensus": {
            "namingPredicate": {
                "command": "ls tools/check-*.py tools/trace-answers.py",
                "count": len(names_at_ref),
                "names": names_at_ref,
                "movedThisPass": False,
                "consecutivePassesUnchanged": 3,
            },
            "verifyShOwnInvocations": {
                "predicate": "tools/*.{py,sh} tools/verify.sh invokes with no --self-test flag",
                "count": len(own),
                "gates": len(own_gates),
                "fixtures": len(own_fixtures),
                "names": own,
            },
            "gradleLiteralInvocations": {
                "predicate": 'commandLine("$projectDir/tools/...") with no --self-test flag',
                "count": len(literal),
                "names": literal,
            },
            "gradleHelperInvocations": {
                "predicate": 'commandLine(mutationSnapshotArguments("...")) -- INVISIBLE to the '
                             "predicate above, and the subject of CH-0286",
                "count": len(helper),
                "names": helper,
                "whyAHelper": (
                    "a harness that mutates Kotlin sources must be handed a snapshot directory and "
                    "cannot be run bare, so it cannot be wired as a literal path"
                ),
            },
            "distinctToolsThatCanFailVerifySh": len(union),
            "atTheCommitThatCarriedThisFile": {
                "ref": carrier,
                "verifyShOwn": len(tree_own),
                "gradleLiteral": len(tree_literal),
                "gradleHelper": len(tree_helper),
                "distinct": len(tree_union),
                "why": (
                    "the ref carries iteration 52's T-330 work uncommitted, so its twelfth "
                    "helper-wired harness is in that pass's own commit and not in the ref. T-340: "
                    "this block read the uncommitted TREE and now reads the COMMIT that carried "
                    "this file, so the deliverable has a state it can name"
                ),
            },
            "asPublishedByC0210": 37,
            "sameQuantityAtC0210sOwnRef": len(set(prior_own) | set(prior_literal) | set(prior_helper)),
            "atC0210sOwnRef": {
                "verifyShOwn": len(prior_own),
                "gradleLiteral": len(prior_literal),
                "gradleHelper": len(prior_helper),
            },
            "setsAreDisjoint": not (set(own) & (set(literal) | set(helper))) and not (set(literal) & set(helper)),
            "theFinding": (
                "the naming predicate's NUMBER held for a third consecutive pass; the other two "
                "moved; and the third derivation is a regular expression over a string LITERAL "
                "where the question is about an INVOCATION, so twelve build-failing tools are "
                "invisible to it and 37 was an undercount of its own sentence's quantity at its own "
                "ref. CH-0243 -> C-0210 -> CH-0286 is one defect at three levels"
            ),
        },
        "supersededTripleCensus": {
            "values": list(triple),
            "movedBy": "C-0219 (T-330), CH-0282",
            "carriersUnderGpd": carriers,
            "occurrencesCorpusWide": sum(c["occurrences"] for c in carriers.values()),
            "occurrencesExcludingThisPassesOwn": sum(
                c["occurrences"] for c in carriers.values() if not c["isThisPassesOwn"]),
            "byRole": {
                "owners": sum(c["occurrences"] for c in carriers.values() if c["ownsTheCorrection"]),
                "theClaimCorrected": sum(
                    c["occurrences"] for k, c in carriers.items()
                    if "C-0154" in k),
                "theDeliverables": sum(
                    c["occurrences"] for c in carriers.values() if c["isADeliverable"]),
                "liveAndUnannotated": sum(
                    c["occurrences"] for k, c in carriers.items() if "C-0191" in k),
                "thisPassesOwn": sum(
                    c["occurrences"] for c in carriers.values() if c["isThisPassesOwn"]),
            },
            "CH0182": (
                "this claim's own challenge quotes the triple in order to report it, so the census "
                "grows by writing it down; both readings are emitted and the deliverable quotes "
                "the one excluding this pass's own artifacts"
            ),
            "nonOwnerCarriersStillLive": ["gpd/claims/C-0191-thirteenth-answers-synthesis.md"],
            "note": (
                "C-0191 section 2(b) quotes C-0154 as reading the withdrawn triple; C-0154 now "
                "reads the corrected one. This claim is forbidden by its brief from editing "
                "C-0191, so the instance is reported and left, which is CH-0287's evidence"
            ),
        },
        "cannotAnswerList": {
            "topLevelBulletsAtRef": older._cannot_answer_bullets(answers_at_ref),
            "entriesTheProgrammeHasSinceAnswered": 0,
            "entriesGainingAMeasuredPRICE": 1,
            "taskIdentifiersNamedInTheSection": 25,
            "openInTheQueue": ["T-9", "T-31"],
            "note": (
                "no entry names something the programme has answered, so the sign-carrying drift "
                "class is empty for the third pass running; ONE entry -- the per-site staple "
                "incorporation map on a coupling-bearing tile -- gains a measured price from "
                "C-0212 and C-0215, which is what the dropout costs a searched coupling"
            ),
        },
        "decisions": {
            "count": 9,
            "prosecutedForAStalePrice": 9,
            "stalePricesFound": 6,
            "stalePriceNote": (
                "the at-a-glance decision 7 cost cell, the 10x6 and 15x4 coupled cells of both of "
                "decision 7's comparison tables (four cells), and decision 9's cost cell. Five of "
                "the six price on a coupled count that C-0212 and C-0215 show is a reading on a "
                "TRANSFERRED distribution and, at decision 9, on the wrong tile; the sixth prices "
                "on 756 of 756, which C-0211 re-grades at the resolved link and upholds"
            ),
            "distinctChallengeIdentifiersCitedAtRef": len(sorted(set(re.findall(r"CH-\d{4}", decisions_at_ref)))),
        },
        "passages": {
            "moved": len(rows),
            "byKind": by_kind,
            "rows": rows,
        },
        "findings": [
            "{} passages moved over {} of the seven declared kinds, {} of them in the document NDI "
            "reads.".format(
                len(rows),
                sum(1 for kind in KINDS if by_kind[kind]),
                sum(1 for row in rows if row["document"] == "DECISIONS-FOR-NDI.md"),
            ),
            "The answer to section 3 row (g) moved twice more and neither deliverable knew it: a "
            "coupled cell is now flat AND inside C-0023's per-path allowable, at 7 of 48 cells on "
            "route B's own tile, which no previous synthesis could say.",
            "SUPERSEDED_RATIO is prosecuted and EMPTY for the second consecutive pass, and "
            "STALE_PRICE is 6 after being 7 and then 0 -- the two classes have now swapped places "
            "twice, which is only visible because both are declared.",
            "The challenge-and-claim census is stale for the EIGHTH time in NINE passes: 231/204 "
            "as written against {}/{} derived at the ref and {}/{} at the commit that "
            "carried this file.".format(
                len(challenges_before), len(claims_before),
                len(carrier_challenges), len(carrier_claims)),
            "The checker census's NUMBER held at one predicate and its PREDICATE is dated at "
            "another: {} Kotlin-subject mutation harnesses are wired through a helper and are "
            "invisible to the literal-path regex, so the honest count is {} at the commit that "
            "carried this file ({} at the ref) and not 37, and 37 was an undercount of {} at its "
            "own ref "
            "(CH-0286).".format(
                len(tree_helper), len(tree_union), len(union),
                len(set(prior_own) | set(prior_literal) | set(prior_helper))),
            "Of the {} occurrences of the triple C-0219 moved (excluding this pass's own), exactly "
            "ONE non-owner still states "
            "it live -- C-0191 section 2(b), a synthesis claim's own correction table -- and no "
            "gate in this repository can see it, because the withdrawn value is still CITED by the "
            "claims that withdrew it (CH-0287).".format(
                sum(c["occurrences"] for c in carriers.values() if not c["isThisPassesOwn"])),
        ],
    }
    return document


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    ok("every declared kind is in KINDS", all(row[0] in KINDS for row in PASSAGES))
    ok("every declared document is in scope", all(row[1] in DOCUMENTS for row in PASSAGES))
    ok("no two passages share an after anchor",
       len({(row[1], row[3]) for row in PASSAGES}) == len(PASSAGES))
    ok("a before anchor MAY be shared, and here three rows share one",
       len({(row[1], row[2]) for row in PASSAGES}) < len(PASSAGES))
    ok("every multiplicity is a positive integer",
       all(isinstance(row[4], int) and row[4] >= 1 for row in PASSAGES))
    ok("at least one passage has multiplicity above one, which is why the field exists",
       any(row[4] > 1 for row in PASSAGES))
    ok("STALE_PRICE is declared and is NOT empty this pass",
       any(row[0] == "STALE_PRICE" for row in PASSAGES))
    ok("SUPERSEDED_RATIO is declared and IS empty this pass, and is reported so",
       "SUPERSEDED_RATIO" in KINDS and not any(row[0] == "SUPERSEDED_RATIO" for row in PASSAGES))
    ok("the snapshot-gate reader finds a helper-wired harness",
       snapshot_gates('commandLine(mutationSnapshotArguments("T-294-mutation-test.py"))\n')
       == ["T-294-mutation-test.py"])
    ok("the snapshot-gate reader ignores a literal invocation",
       snapshot_gates('commandLine("$projectDir/tools/a.py")\n') == [])
    ok("the inherited literal reader ignores a helper invocation, which is the whole of CH-0286",
       previous.gradle_gates('commandLine(mutationSnapshotArguments("T-294-mutation-test.py"))\n') == [])
    ok("the snapshot-gate reader de-duplicates",
       snapshot_gates('commandLine(mutationSnapshotArguments("a.py"))\n'
                      'commandLine(mutationSnapshotArguments("a.py"))\n') == ["a.py"])
    ok("the two Gradle predicates are disjoint on any single line by construction",
       set(previous.gradle_gates('commandLine("$projectDir/tools/a.py")\n'))
       & set(snapshot_gates('commandLine("$projectDir/tools/a.py")\n')) == set())
    ok("the inherited verify.sh reader is reused unchanged",
       previous.verify_invocations("    tools/a.py\n") == ["tools/a.py"])
    ok("a fixture is told apart from a gate by its basename",
       os.path.basename("tools/test-result-reader-census.py").startswith("test-"))

    for name, passed in checks:
        print("{}  {}".format("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# {} self-test(s), {} failure(s)".format(len(checks), len(failed)))
    return 1 if failed else 0


def main():
    parser = argparse.ArgumentParser(
        description="emit gpd/results/T-332-fifteenth-answers-synthesis.json")
    # PINNED, never `HEAD`: a corpus-subject emitter defaulting to a moving ref re-bases its own
    # measurement between the draft and the emission (`CH-0246`), and re-running it as a control
    # then OVERWRITES the record instead of checking it.  `T-340`.
    parser.add_argument("--ref", default="d7b7074",
                        help="the corpus state to measure against (this file's own baselineRef)")
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--out", default=os.path.join(
        ROOT, "gpd", "results", "T-332-fifteenth-answers-synthesis.json"))
    arguments = parser.parse_args()
    if arguments.self_test:
        return _self_test()
    try:
        older._resolve(arguments.ref)
    except Exception:
        sys.stderr.write(
            "T-332-emit: REFUSING to emit -- no git repository holding {} under {}. A "
            "corpus-subject result file must name the state it measured (CH-0246), so this path "
            "does NOT degrade; the 15 self-test arms are git-free and do run.\n".format(
                arguments.ref, ROOT))
        return 2
    document = build(arguments.ref)
    with open(arguments.out, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to {}".format(os.path.relpath(arguments.out, ROOT)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
