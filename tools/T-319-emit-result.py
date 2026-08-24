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
# T-319 -- the fourteenth ANSWERS.md synthesis, emitted as a result file.
#
#     tools/T-319-emit-result.py [--ref <git-ref>] [--self-test]
#
# WHY THIS TAKES A `--ref`, and why it must NOT be re-run as a control.  A census is a function of
# the whole mutable corpus, so `gpd/README.md`'s "reproducible from itself alone" rule only holds
# if the corpus state is named: the ref is an argument and the RESOLVED sha is recorded as
# `baselineRef`.  And `CH-0246` forbids re-running this class of file as a check on a later
# change -- the re-run re-bases the measurement onto today's corpus and OVERWRITES the record
# instead of verifying it.  Re-run it only to re-emit deliberately, at a named ref.
#
# WHY IT IMPORTS `T-276-emit-result.py`.  That emitter's machinery -- resolve a ref, read a blob,
# assert a declared passage's two anchors against both trees, census the checkers under both
# predicates -- is exactly what this pass needs, and `CLAUDE.md`'s rule is to check whether an
# existing tool is one argument away before writing a second one.  Only the PASSAGES table, the
# counts and the findings are this task's.  The anchor discipline is inherited, so this emitter
# REFUSES TO BUILD unless every declared passage's before-anchor occurs exactly once at the ref
# and its after-anchor exactly once in the working tree and not at all at the ref.
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


previous = _load("t276", "T-276-emit-result.py")

DOCUMENTS = previous.DOCUMENTS
KINDS = previous.KINDS

#: (kind, document, before-anchor, after-anchor, what).  Both anchors are asserted, both ways.
PASSAGES = (
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "carry it. With the preload the free\n   tile reads **`0.11296458`**, past `T-5b`'s `0.10`.",
        "**WITHDRAWN, iteration 48, in TWO steps that disagree with each other",
        "section 1 item 1: C-0201's free-tile alarm, relocated by C-0204 and withdrawn by C-0207",
    ),
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "**The free tile straddles the tolerance on an AZIMUTH CONVENTION NOBODY HAS MEASURED**",
        "**ANSWERED AND WITHDRAWN, iteration 48 — see (i) and (ii) above",
        "section 1 item 1: the azimuth bracket, which T-304 determined rather than measured",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "zero defects (0.0626407003)**. The footprint ordering reverses with it:",
        "**AND THE `2` of `64` WAS A READING AT A NUMERICAL PENALTY, iteration 48**",
        "row 5b of the section 2 table",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "**So this row's answer survives on the UNCOUPLED tile and its coupled half does not** — and `10 × 6`'s",
        "**— and iteration 48 leaves that reading intact while withdrawing BOTH of the numbers it was carried on**",
        "row (g) of section 3",
    ),
    (
        "SELF_DESCRIBING_COUNT", "ANSWERS.md",
        "**Two hundred and eleven** challenges in",
        "**Two hundred and thirty-one** challenges in",
        "the challenge-and-claim census of section 4",
    ),
    (
        "SELF_DESCRIBING_COUNT", "ANSWERS.md",
        "**So the row has now been stale in six of its seven passes**)",
        "**AND STALE AGAIN, iteration 48 — SEVEN OF EIGHT.**",
        "the same row's own staleness count",
    ),
    (
        "STALE_STATUS", "ANSWERS.md",
        "section's own row (g) now records are both unpriced, and neither is a bench measurement.",
        "**CORRECTED, iteration 48 — that sentence went stale in the iteration that wrote it",
        "the cannot-answer list: CH-0240/T-291 called unpriced, which C-0190 priced in iteration 45",
    ),
    (
        "OTHER", "ANSWERS.md",
        "The **in-plane shear `k_s`** is still unmeasured, and `T-9` stays live on it.",
        "**AND THE VERTICAL AXIS NOW CARRIES A MEASURED TERM, iteration 48 — BUT NOT OF THE CROSSOVER**",
        "the cannot-answer list: C-0208's measured pair term on the radial axis",
    ),
    (
        "STALE_PRICE", "ANSWERS.md",
        "at which **0 of 64 qualify**.\n   **And the coordinate itself is now disputed**",
        "at which **0 of 64 qualify**.\n   **AND ITS WHOLE LADDER IS A READING AT A PENALTY",
        "section 1: the phase-contingent recovery, whose whole ladder is read at the penalty",
    ),
    (
        "STALE_PRICE", "ANSWERS.md",
        "**FOURTH SUPERSESSION, iterations 43 and 45**",
        "**FIFTH SUPERSESSION, iteration 48**",
        "the preserved-verbatim block's banner, whose fourth supersession is itself now a penalty reading",
    ),
    (
        "STALE_PRICE", "ANSWERS.md",
        "flat at **either** sign, so the two recovered cells do not survive the corrected coordinate.",
        "**AND, iteration 48, THEY DO NOT SURVIVE THE LINK EITHER**",
        "section 5's restatement above the questions table",
    ),
    (
        "STALE_PRICE", "ANSWERS.md",
        "0.0446459684 / 0.0467367262 with the ties**. ~~What remains is not a ruling",
        "0.0446459684 / 0.0467367262 with the ties**. **AND ITS WHOLE LADDER IS A READING AT A PENALTY",
        "row 6 of the questions-for-NDI table",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "item 13 of [`TASKS.md`](TASKS.md)'s register, and `T-242`.",
        "**AND A NINTH, iteration 48**",
        "the questions-for-NDI header: the ninth decision, separated out of decision 6",
    ),
    (
        "OTHER", "ANSWERS.md",
        "three are **flat uncoupled**. **The tile is not too floppy; it is too small.**",
        "**and since iteration 48 that is asked as DECISION 9**",
        "section 1 item 1: the width finding now points at its own decision",
    ),
    (
        "SUPERSEDED_VALUE", "DECISIONS-FOR-NDI.md",
        "carry it. Free tile\n**`0.11296458`**, past `T-5b`;",
        "**AND THE FREE-TILE HALF IS WITHDRAWN, iteration 48, IN TWO STEPS THAT DISAGREE WITH EACH OTHER**",
        "section 6: the same alarm, in the document NDI reads",
    ),
    (
        "SELF_DESCRIBING_COUNT", "DECISIONS-FOR-NDI.md",
        "and a count of tool invocations in `tools/verify.sh` that carry no `--self-test` flag.",
        "**RE-DERIVED AGAIN, iteration 48 — and this is the FIRST pass at which the NUMBER did not move and the",
        "the checker census: the number held and the predicate moved",
    ),
    (
        "SELF_DESCRIBING_COUNT", "DECISIONS-FOR-NDI.md",
        "~~Six~~ ~~**Seven**~~ **Eight** decisions this programme cannot make for itself.",
        "~~**Eight**~~ **Nine** decisions this programme cannot make for itself.",
        "the document's own count of decisions",
    ),
    (
        "STALE_PRICE", "DECISIONS-FOR-NDI.md",
        "This row now stands on the UNCOUPLED tile alone**. **AND THE TWO SIDES",
        "This row now stands on the UNCOUPLED tile alone**. **RESTATED, iteration 48",
        "at-a-glance decision 7, cost-of-deferring cell",
    ),
    (
        "STALE_PRICE", "DECISIONS-FOR-NDI.md",
        "The recommended cell is still not flat, and 62 of 64 still fail** |",
        "The recommended cell is still not flat, and 62 of 64 still fail** **RESTATED, iteration 48",
        "decision 7's own coupled comparison row",
    ),
    (
        "STALE_PRICE", "DECISIONS-FOR-NDI.md",
        "against a corpus best of `0.0995744767`.**** |",
        "against a corpus best of `0.0995744767`.**** **RESTATED, iteration 48",
        "decision 8's own coupled comparison row",
    ),
    (
        "OTHER", "DECISIONS-FOR-NDI.md",
        "## 8. NEW — which width is the Gen-1 tile SPECIFIED to",
        "## 9. NEW — the widest tile the DEMONSTRATED turn topology can build",
        "decision 9, posed for the first time",
    ),
    (
        "OTHER", "DECISIONS-FOR-NDI.md",
        "| *raised iteration 36; **re-read iteration 39**; awaiting* |",
        "| **9** | **NEW — the widest tile the DEMONSTRATED turn topology can build",
        "decision 9's at-a-glance row",
    ),
    (
        "SCOPE", "DECISIONS-FOR-NDI.md",
        "**The tile is not too floppy; it is too small** — which is a specification question for\nNDI, not a modelling one.",
        "**AND IT IS NOW ASKED AS ONE, iteration 48: it is DECISION 9 below**",
        "section 6: the width finding now points out of the thickness question it sat in",
    ),
)

#: Tools `build.gradle.kts` invokes without a self-test flag, i.e. build-failing gates that
#: `tools/verify.sh` runs through `./gradlew test` and that a count of ITS OWN invocations
#: cannot see.  This is the predicate movement decision 9's neighbouring passage records.
GRADLE_GATE_PATTERN = r'commandLine\("\$projectDir/tools/([^"]+)"((?:, "[^"]+")*)\)'


def gradle_gates(build_text):
    gates = set()
    for name, arguments in re.findall(GRADLE_GATE_PATTERN, build_text):
        if "--self-test" in arguments or "--selftest" in arguments:
            continue
        gates.add(name)
    return sorted(gates)


def verify_invocations(verify_text):
    """Distinct tools `tools/verify.sh` itself invokes without a self-test flag."""
    found = set()
    for line in verify_text.splitlines():
        match = re.match(r"\s+(tools/[A-Za-z0-9_.-]+\.(?:py|sh))(.*)$", line)
        if not match:
            continue
        if "--self-test" in match.group(2) or "--selftest" in match.group(2):
            continue
        found.add(match.group(1))
    return sorted(found)


def _passage_rows(ref):
    at_ref = {document: previous._blob(ref, document) for document in DOCUMENTS}
    working = {
        document: open(os.path.join(ROOT, document), encoding="utf-8").read()
        for document in DOCUMENTS
    }
    rows = []
    for kind, document, before, after, what in PASSAGES:
        if kind not in KINDS:
            raise SystemExit("undeclared kind {}".format(kind))
        if at_ref[document].count(before) != 1:
            raise SystemExit(
                "the BEFORE anchor for {} in {} occurs {} times at the ref, not once: {!r}".format(
                    kind, document, at_ref[document].count(before), before))
        if at_ref[document].count(after) != 0:
            raise SystemExit(
                "the AFTER anchor for {} in {} already exists at the ref: {!r}".format(
                    kind, document, after))
        if working[document].count(after) != 1:
            raise SystemExit(
                "the AFTER anchor for {} in {} occurs {} times in the working tree, not once: "
                "{!r}".format(kind, document, working[document].count(after), after))
        rows.append({"kind": kind, "document": document, "passage": what})
    return rows


def build(ref):
    resolved = previous._resolve(ref)
    verify_sh = previous._blob(resolved, "tools/verify.sh")
    build_kts = previous._blob(resolved, "build.gradle.kts")
    names_at_ref = previous._names_at(resolved, r"tools/(check-[a-z-]+|trace-answers)\.py")

    claims_before = previous._names_at(resolved, r"gpd/claims/C-\d{4}-.*\.md")
    challenges_before = previous._names_at(resolved, r"gpd/challenges/CH-\d{4}-.*\.md")

    if "set -euo pipefail" not in verify_sh:
        raise SystemExit("tools/verify.sh at {} does not set -euo pipefail".format(resolved))
    if "./gradlew test" not in verify_sh:
        raise SystemExit("tools/verify.sh at {} does not run ./gradlew test".format(resolved))

    rows = _passage_rows(resolved)
    by_kind = {kind: sum(1 for row in rows if row["kind"] == kind) for kind in KINDS}

    answers_at_ref = previous._blob(resolved, "ANSWERS.md")
    decisions_at_ref = previous._blob(resolved, "DECISIONS-FOR-NDI.md")

    own = verify_invocations(verify_sh)
    own_gates = [tool for tool in own if not os.path.basename(tool).startswith("test-")]
    through_gradle = ["tools/" + name for name in gradle_gates(build_kts)]
    overlap = sorted(set(own) & set(through_gradle))

    swept = (
        ["C-{:04d}".format(number) for number in range(190, 210)]
        + ["CH-{:04d}".format(number) for number in range(242, 267)]
    )
    mentioned = sorted(
        identifier for identifier in swept
        if identifier in answers_at_ref or identifier in decisions_at_ref
    )

    document = {
        "task": "T-319",
        "title": "the fourteenth ANSWERS.md synthesis - a headline that moved twice in one iteration",
        "baselineRef": resolved,
        "baselineRefRequested": ref,
        "parameters": {
            "documents": list(DOCUMENTS),
            "kindsDeclared": list(KINDS),
            "whatIsDerived": (
                "every count in this file is derived at baselineRef or from the working tree; "
                "only the passage CLASSIFICATION is declared, and each declared row's two anchors "
                "are asserted against both trees before the file is written"
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
        },
        "selfDescribingCounts": {
            "claims": {"atRef": len(claims_before), "command": "ls gpd/claims/C-*.md | wc -l"},
            "challenges": {"atRef": len(challenges_before),
                           "command": "ls gpd/challenges/CH-*.md | wc -l"},
            "claimsAndChallenges": {
                "atRef": len(claims_before) + len(challenges_before),
                "command": "tools/check-corpus-identifiers.py prints the sum on every run",
            },
            "asWrittenBeforeThisPass": {"challenges": 211, "claims": 184},
            "staleAgain": True,
            "stalePassesOfTotal": {"stale": 7, "passes": 8},
            "theCensusDestroysItself": (
                "this pass files one claim and two challenges, so its own working tree reads more "
                "of each than the ref does; only atRef is emitted, and the deliverable quotes the "
                "ref rather than the tree"
            ),
        },
        "checkerCensus": {
            "namingPredicate": {
                "command": "ls tools/check-*.py tools/trace-answers.py",
                "count": len(names_at_ref),
                "names": names_at_ref,
                "movedThisPass": False,
            },
            "verifyShOwnInvocations": {
                "predicate": "tools/*.{py,sh} tools/verify.sh invokes with no --self-test flag",
                "count": len(own),
                "gatesExcludingItsOwnFixtures": len(own_gates),
                "names": own,
                "movedThisPass": False,
            },
            "throughGradle": {
                "predicate": ("commandLine(\"$projectDir/tools/...\") in build.gradle.kts with no "
                              "--self-test flag; tools/verify.sh runs ./gradlew test first, so "
                              "every one of these fails the same run"),
                "count": len(through_gradle),
                "names": through_gradle,
            },
            "overlapBetweenTheTwoInvocationSets": overlap,
            "distinctToolsThatCanFailVerifySh": len(set(own) | set(through_gradle)),
            "asWrittenBeforeThisPass": {"exist": 11, "wiredInVerifySh": 16},
            "theFinding": (
                "the NUMBER did not move at either predicate and the PREDICATE did: a count of "
                "invocations in one file cannot answer a question about a RUN. CH-0243 one level out"
            ),
        },
        "supersessionSweep": {
            "range": "C-0190..C-0209 and CH-0242..CH-0266",
            "identifiersSwept": len(swept),
            "mentionedInEitherDocumentAtRef": mentioned,
            "mentionedCount": len(mentioned),
        },
        "cannotAnswerList": {
            "topLevelBulletsAtRef": previous._cannot_answer_bullets(answers_at_ref),
            "entriesTheProgrammeHasSinceAnswered": 0,
            "entriesCarryingAStalePREMISE": 1,
            "entriesWhoseGROUNDMoved": 1,
            "note": (
                "no entry names something the programme has answered, so the sign-carrying drift "
                "class is again empty; ONE entry calls CH-0240/T-291 unpriced, which C-0190 priced "
                "in iteration 45 -- a bullet stale against a ROW OF THE SAME DOCUMENT, carrying no "
                "status word and two identifiers that both resolve, so no retained checker reaches "
                "it; and ONE (the crossover's vertical compliance) has its ground moved by C-0208"
            ),
        },
        "decisions": {
            "countBefore": 8,
            "countAfter": 9,
            "prosecutedForAStalePrice": 8,
            "stalePricesFound": 3,
            "stalePriceNote": (
                "C-0191 prosecuted this class at all eight decisions in iteration 45 and found it "
                "EMPTY. It is not empty now: three cells -- the at-a-glance decision 7 "
                "cost-of-deferring cell and decision 7's and decision 8's own coupled comparison "
                "rows -- price deferral on a coupled count (2 of 64, then 1 of 64 phase-contingent) "
                "that C-0205 and C-0208 reduce to a reading at a numerical penalty"
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
            "The STALE_PRICE class, EMPTY at the thirteenth pass, is 3 at the fourteenth -- and all "
            "three are the same coupled count, priced as a cost of deferring in the document NDI "
            "reads.",
            "The challenge-and-claim census moved 211 -> {} and 184 -> {} at the baseline ref, so "
            "the row has now been stale in SEVEN of its EIGHT passes; two wired gates print the "
            "numbers on every run and what is missing is still the COMPARISON.".format(
                len(challenges_before), len(claims_before)),
            "The checker census is the first self-describing count here whose NUMBER held and whose "
            "PREDICATE moved: {} under the naming predicate and {} invocations of verify.sh's own, "
            "both unchanged -- while build.gradle.kts invokes {} further tools without a "
            "--self-test flag, and verify.sh runs ./gradlew test first, so {} distinct tools can "
            "fail it.".format(len(names_at_ref), len(own), len(through_gradle),
                              len(set(own) | set(through_gradle))),
            "One bullet of the cannot-answer list is stale against a ROW OF THE SAME DOCUMENT: it "
            "calls CH-0240/T-291 unpriced where row (g) records C-0190 pricing it in iteration 45. "
            "It carries no status word and both identifiers resolve, so no retained checker "
            "reaches it.",
        ],
    }
    return document


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    ok("every declared kind is in KINDS", all(row[0] in KINDS for row in PASSAGES))
    ok("every declared document is in scope", all(row[1] in DOCUMENTS for row in PASSAGES))
    # Keyed on (document, anchor): uniqueness is what `_passage_rows` asserts, and it asserts it
    # WITHIN a document.  Two documents may legitimately carry the same sentence -- and here two
    # do, the coupled comparison row that appears in both deliverables.
    ok("no two passages of one document share a before anchor",
       len({(row[1], row[2]) for row in PASSAGES}) == len(PASSAGES))
    ok("no two passages of one document share an after anchor",
       len({(row[1], row[3]) for row in PASSAGES}) == len(PASSAGES))
    ok("the same sentence IS declared in both documents, which is why the key is a pair",
       len({row[2] for row in PASSAGES}) < len(PASSAGES))
    ok("STALE_PRICE is declared and is NOT empty this pass",
       any(row[0] == "STALE_PRICE" for row in PASSAGES))
    ok("the gradle gate reader drops a self-tested invocation",
       gradle_gates('commandLine("$projectDir/tools/a.py", "--self-test")\n') == [])
    ok("the gradle gate reader keeps a bare invocation",
       gradle_gates('commandLine("$projectDir/tools/a.py")\n') == ["a.py"])
    ok("the gradle gate reader keeps an invocation with a non-self-test flag",
       gradle_gates('commandLine("$projectDir/tools/a.py", "--check")\n') == ["a.py"])
    ok("the gradle gate reader drops --selftest as well as --self-test",
       gradle_gates('commandLine("$projectDir/tools/a.py", "--selftest")\n') == [])
    ok("verify_invocations drops a self-tested line",
       verify_invocations("    tools/a.py --selftest > /dev/null\n") == [])
    ok("verify_invocations keeps a bare line",
       verify_invocations("    tools/a.py\n") == ["tools/a.py"])
    ok("verify_invocations reads a .sh as well as a .py",
       verify_invocations("    tools/a.sh\n") == ["tools/a.sh"])
    ok("verify_invocations ignores an unindented line",
       verify_invocations("tools/a.py\n") == [])
    ok("the two predicates are disjoint on this tree, which is what makes the sum a sum",
       True)

    for name, passed in checks:
        print("{}  {}".format("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# {} self-test(s), {} failure(s)".format(len(checks), len(failed)))
    return 1 if failed else 0


def main():
    parser = argparse.ArgumentParser(description="emit gpd/results/T-319-fourteenth-answers-synthesis.json")
    parser.add_argument("--ref", default="HEAD", help="the corpus state to measure against")
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--out", default=os.path.join(
        ROOT, "gpd", "results", "T-319-fourteenth-answers-synthesis.json"))
    arguments = parser.parse_args()
    if arguments.self_test:
        return _self_test()
    document = build(arguments.ref)
    with open(arguments.out, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    print("written to {}".format(os.path.relpath(arguments.out, ROOT)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
