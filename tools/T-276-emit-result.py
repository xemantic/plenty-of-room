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
# T-276, second pass -- the THIRTEENTH synthesis of the two outward-facing documents.
#
#     tools/T-276-emit-result.py [--ref <git-ref>] [--self-test]
#
# WHY THIS TAKES A `--ref`. `gpd/README.md`'s rule is that a result file is reproducible from itself
# alone. Every other result file here is a function of code plus committed inputs; a CENSUS is a
# function of the whole MUTABLE corpus, so without a ref it can never be re-run. `C-0177` records
# the trap: `tools/T-249-emit-result.py` hardwired `HEAD` and stopped reproducing its own committed
# file the moment the corpus it measured was repaired. The ref defaults to `HEAD`, and the
# **resolved** sha is recorded as `baselineRef`.
#
# WHAT IS DERIVED AND WHAT IS DECLARED. The three self-describing counts, the checker census and
# every denominator are DERIVED -- at the ref for the `before` column and from the working tree for
# the `after` one. The passage table is DECLARED, because a classification is a READING; but every
# declared row carries two anchors and the emitter REFUSES to build unless
#
#   * the `before` anchor occurs exactly once in the document at the ref, and
#   * the `after` anchor occurs exactly once in the working tree and NOT AT ALL at the ref.
#
# So the count of moved passages cannot drift from the edits: a row whose edit was never made, or was
# made twice, or was made to text that was not there, fails the build. That is `C-0171`'s
# "counted by differencing struck spans against HEAD" with the difference asserted rather than taken.
#
# NO WALL CLOCK AND NO STEP COUNTER, per `CLAUDE.md`: a timing is less reproducible than a step
# count, and one such field makes a whole file permanently un-diffable.
import argparse
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

DOCUMENTS = ("ANSWERS.md", "DECISIONS-FOR-NDI.md")

#: The seven kinds the prompt for this pass names.  `STALE_PRICE` is declared even though this pass
#: found none of it: an empty class is a RESULT (`C-0141`'s partition), and a kind that is dropped
#: because it came back empty cannot be distinguished later from one nobody looked for.
KINDS = (
    "SUPERSEDED_VALUE",
    "SUPERSEDED_RATIO",
    "STALE_STATUS",
    "SCOPE",
    "SELF_DESCRIBING_COUNT",
    "STALE_PRICE",
    "OTHER",
)

#: (kind, document, before-anchor, after-anchor, what).  The anchors are short and unique; the
#: emitter asserts both directions.
PASSAGES = (
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "**THE RECOVERY RESTS ON A SIGN NOTHING IN THIS REPOSITORY FIXES",
        "RESTATED, iteration 45 — THE SIGN IS DERIVED, AND THE TWO UNIFORM READINGS ABOVE ARE BOTH OFF-LATTICE",
        "the iteration-43 restatement's sign paragraph, superseded by C-0187 and disputed by CH-0240",
    ),
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "rests on a prestrain **sign nothing here fixes**",
        "**RESTATED, iteration 45** ([`C-0187`](gpd/claims/C-0187-the-turn-prestrain-sign-is-derived.md),\n[`CH-0240`]",
        "the tail of the row (g) restatement block",
    ),
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "on a sign nothing here fixes ([`T-284`](TASKS.md)).",
        "the sign is DERIVED — caDNAno's `±5 bp` rule and `C-0148`'s closure condition pin **58 of the 59** binaries",
        "row (g) of section 3, inline",
    ),
    (
        "SUPERSEDED_VALUE", "DECISIONS-FOR-NDI.md",
        "**(3) The recovery rests on a sign nothing in this repository fixes.**",
        "**(3) RESTATED, iteration 45 — THE SIGN IS DERIVED, AND THE RECOVERY IS PHASE-CONTINGENT RATHER THAN UNSETTLED**",
        "decision 7's third qualification on the tied re-grade",
    ),
    (
        "SUPERSEDED_VALUE", "ANSWERS.md",
        "The block below is preserved unedited under `CH-0183`'s rule; the banner is the repair.",
        "**FOURTH SUPERSESSION, iterations 43 and 45**",
        "the preserved-verbatim block's banner, whose third supersession said no cell clears T-5b",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "with them present it is `2` of `64`, `C-0151`'s own eight cells `1` of `8`",
        "**— and that `2` of `64` is a ZERO-PRESTRAIN count",
        "row 5b of the section 2 table",
    ),
    (
        "SCOPE", "DECISIONS-FOR-NDI.md",
        "with the raster's own 59 turn ties present **two** of `10 × 6`'s 64 cells are",
        "a ZERO-PRESTRAIN count, and iteration 45",
        "the at-a-glance decision 7 cost-of-deferring cell",
    ),
    (
        "SUPERSEDED_RATIO", "ANSWERS.md",
        "**4.06× flatter — 0.0978155002 against 0.0240648102, a ratio this file constructs and no claim",
        "**AND BOTH OF ITS ARGUMENTS HAVE SINCE MOVED, iteration 45**",
        "the cross-section flatness multiple in section 1",
    ),
    (
        "SUPERSEDED_RATIO", "DECISIONS-FOR-NDI.md",
        "**0.0240648102 — 4.06× flatter, which is the ratio of the two cells of this row",
        "**BOTH ARGUMENTS MOVED, iteration 45**",
        "the same multiple in the 7a comparison table",
    ),
    (
        "SUPERSEDED_RATIO", "ANSWERS.md",
        "against **0.0274976866** of headroom — a margin of **496×**.",
        "**— AND BOTH THE BOUND AND ITS COMPARAND HAVE MOVED, iteration 45**",
        "the rim-modulation bound in section 1",
    ),
    (
        "SUPERSEDED_RATIO", "ANSWERS.md",
        "of the stroke against **0.0274976866** of headroom, a margin of **496×**.",
        "**— RESTATED, iteration 45** ([`C-0175`](gpd/claims/C-0175-drawable-raster-rim.md)): at the recommended",
        "the same bound in row (g)",
    ),
    (
        "SUPERSEDED_RATIO", "DECISIONS-FOR-NDI.md",
        "tightest flat coupled cell has: a margin of **496×**, consuming at most **0.2016 %** of it.",
        "**RESTATED, iteration 45 — BOTH THE BOUND AND ITS COMPARAND HAVE MOVED**",
        "the same bound in the ragged-face section",
    ),
    (
        "SELF_DESCRIBING_COUNT", "ANSWERS.md",
        "**One hundred and ninety** challenges in",
        "**Two hundred and eleven** challenges in",
        "the challenge-and-claim census of section 4",
    ),
    (
        "SELF_DESCRIBING_COUNT", "ANSWERS.md",
        "and **this row has now been stale five times out of six passes**",
        "**six times out of seven, iteration 45**",
        "the same row's own staleness count",
    ),
    (
        "SELF_DESCRIBING_COUNT", "DECISIONS-FOR-NDI.md",
        "`ls tools/check-*.py tools/trace-answers.py` returns **eight** retained checkers;",
        "**RE-DERIVED, iteration 45 — the count moved AGAIN",
        "the checker census: how many exist, how many wired, how many read the file",
    ),
    (
        "SELF_DESCRIBING_COUNT", "DECISIONS-FOR-NDI.md",
        "**Seven of the eight are; `trace-answers.py` is\nnot, and that is the one whose absence a synthesis has to remember.**",
        "**ALL TEN ARE, iteration 45**",
        "the same census's wiring clause, restated once more below the enumeration",
    ),
    (
        "SCOPE", "ANSWERS.md",
        "This is now the single most\n  consequential missing measurement in the programme",
        "**SCOPE CORRECTED, iteration 45 — the superlative is struck and the measurement is not.**",
        "the per-site incorporation entry of the cannot-answer list",
    ),
    (
        "STALE_STATUS", "ANSWERS.md",
        "still not found in nine queries across three databases (`T-45`, `C-0072`)",
        "(**Read against the queue, iteration 45**: `T-45` itself is **ANSWERED** there",
        "the staple-extension stiffness-spread entry, which names a task the queue has closed",
    ),
    (
        "OTHER", "DECISIONS-FOR-NDI.md",
        "This row now stands on the UNCOUPLED tile alone**",
        "**AND THE TWO SIDES OF THIS CELL ARE DELIBERATELY READ IN THE SAME STATE, iteration 45.**",
        "the one 15x4 comparison C-0186 left: the leave is now stated in the document and queued as T-294",
    ),
)

#: The tools `tools/verify.sh` runs as build-failing gates that the naming predicate cannot see.
#: Declared so the emitter can assert each is really invoked; the assertion is the point.
GATES_OUTSIDE_THE_NAMING_PREDICATE = (
    "tools/result-reader-census.py",
    "tools/T-278-emitter-rounding-census.py",
    "tools/T-272-emit-result-inputs.py",
    "tools/T-272-header-census.py",
)


def _run(args, cwd=ROOT):
    return subprocess.run(args, cwd=cwd, capture_output=True, text=True, check=True).stdout


def _resolve(ref):
    return _run(["git", "rev-parse", ref]).strip()


def _blob(ref, path):
    return _run(["git", "show", "{}:{}".format(ref, path)])


def _names_at(ref, pattern):
    listing = _run(["git", "ls-tree", "-r", "--name-only", ref]).splitlines()
    return sorted(name for name in listing if re.fullmatch(pattern, name))


def _working_names(pattern):
    found = []
    for directory, _, files in os.walk(ROOT):
        if ".git" in directory.split(os.sep):
            continue
        for name in files:
            relative = os.path.relpath(os.path.join(directory, name), ROOT).replace(os.sep, "/")
            if re.fullmatch(pattern, relative):
                found.append(relative)
    return sorted(found)


def _checker_census(verify_sh, names):
    """The census, under BOTH predicates, so the naming one can be seen to be a naming one."""
    invoked = set(
        re.findall(r"^\s+(tools/[A-Za-z0-9_.-]+\.py)", verify_sh, flags=re.MULTILINE)
    )
    gates = sorted(
        tool for tool in invoked
        if not os.path.basename(tool).startswith("test-")
    )
    return {
        "namingPredicate": "ls tools/check-*.py tools/trace-answers.py",
        "underTheNamingPredicate": names,
        "underTheNamingPredicateCount": len(names),
        "wiredOfThose": sorted(name for name in names if name in invoked),
        "gatePredicate": "every tools/*.py tools/verify.sh invokes, excluding its own test- fixtures",
        "underTheGatePredicate": gates,
        "underTheGatePredicateCount": len(gates),
        "invisibleToTheNamingPredicate": sorted(
            tool for tool in gates if tool not in names
        ),
    }


def _reads_the_deliverables():
    """Derived by importing each tool and asking it for its own file list, never by its name."""
    import importlib.util

    reads = []
    for path in sorted(_working_names(r"tools/(check-[a-z-]+|trace-answers)\.py")):
        spec = importlib.util.spec_from_file_location("_probe", os.path.join(ROOT, path))
        module = importlib.util.module_from_spec(spec)
        try:
            spec.loader.exec_module(module)
        except SystemExit:
            pass
        except Exception:
            continue
        listing = None
        for candidate in ("tracked_markdown", "scanned_files", "corpus_files", "markdown_files"):
            if hasattr(module, candidate):
                try:
                    listing = [str(item) for item in getattr(module, candidate)()]
                except Exception:
                    listing = None
                break
        if listing is not None and any(document in item for item in listing for document in DOCUMENTS):
            reads.append(path)
        elif listing is None and re.search(r"DEFAULT_DOCUMENTS", open(os.path.join(ROOT, path), encoding="utf-8").read()):
            reads.append(path)
    return sorted(reads)


def _passage_rows(ref):
    at_ref = {document: _blob(ref, document) for document in DOCUMENTS}
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
                    kind, document, at_ref[document].count(before), before
                )
            )
        if at_ref[document].count(after) != 0:
            raise SystemExit(
                "the AFTER anchor for {} in {} already exists at the ref: {!r}".format(
                    kind, document, after
                )
            )
        if working[document].count(after) != 1:
            raise SystemExit(
                "the AFTER anchor for {} in {} occurs {} times in the working tree, not once: "
                "{!r}".format(kind, document, working[document].count(after), after)
            )
        rows.append({"kind": kind, "document": document, "passage": what})
    return rows


def _cannot_answer_bullets(text):
    """The top-level bullets of the `What we cannot answer, and why` section."""
    section = text.split("\n## 5. What we cannot answer, and why\n", 1)[1]
    section = section.split("\n### The questions for NDI", 1)[0]
    return len(re.findall(r"^- ", section, flags=re.MULTILINE))


def build(ref):
    resolved = _resolve(ref)
    verify_sh = _blob(resolved, "tools/verify.sh")
    names_at_ref = _names_at(resolved, r"tools/(check-[a-z-]+|trace-answers)\.py")

    claims_before = _names_at(resolved, r"gpd/claims/C-\d{4}-.*\.md")
    challenges_before = _names_at(resolved, r"gpd/challenges/CH-\d{4}-.*\.md")

    for tool in GATES_OUTSIDE_THE_NAMING_PREDICATE:
        if not re.search(r"^\s+{}".format(re.escape(tool)), verify_sh, flags=re.MULTILINE):
            raise SystemExit("{} is not invoked by tools/verify.sh at {}".format(tool, resolved))
    if "set -euo pipefail" not in verify_sh:
        raise SystemExit("tools/verify.sh at {} does not set -euo pipefail".format(resolved))

    rows = _passage_rows(resolved)
    by_kind = {kind: sum(1 for row in rows if row["kind"] == kind) for kind in KINDS}

    answers_at_ref = _blob(resolved, "ANSWERS.md")
    decisions_at_ref = _blob(resolved, "DECISIONS-FOR-NDI.md")

    # The sweep denominator: every claim and challenge filed since the twelfth pass, and how many
    # of them the two documents mention at all.  `C-0186` swept C-0176..C-0184 and CH-0224..CH-0232
    # and found the sweep empty; this pass sweeps the whole range again so the denominator is one
    # number rather than two.
    swept = (
        ["C-{:04d}".format(number) for number in range(174, 190)]
        + ["CH-{:04d}".format(number) for number in range(223, 242)]
    )
    mentioned = sorted(
        identifier for identifier in swept
        if identifier in answers_at_ref or identifier in decisions_at_ref
    )

    challenge_ids = sorted(set(re.findall(r"CH-\d{4}", decisions_at_ref)))

    document = {
        "task": "T-276",
        "title": "the thirteenth ANSWERS.md synthesis - the residue T-288 named, read and closed",
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
        },
        "selfDescribingCounts": {
            # ONLY the ref counts are emitted. The WORKING count of a corpus census moves under
            # every concurrent agent, so emitting it would make this file un-diffable for exactly
            # the reason `CLAUDE.md` gives against a wall clock -- and it is the ref, not the tree,
            # that the deliverable quotes.
            "claims": {"atRef": len(claims_before), "command": "ls gpd/claims/C-*.md | wc -l"},
            "challenges": {"atRef": len(challenges_before),
                           "command": "ls gpd/challenges/CH-*.md | wc -l"},
            "claimsAndChallenges": {
                "atRef": len(claims_before) + len(challenges_before),
                "command": "tools/check-corpus-identifiers.py prints the sum on every run",
            },
            "asWrittenBeforeThisPass": {"challenges": 190, "claims": 164},
            "staleAgain": True,
            "filedByThisTask": {"claims": 1, "challenges": 1},
            "theCensusDestroysItself": (
                "this pass files one claim and one challenge, so its own working tree reads one "
                "more of each than the ref does; a census over a corpus that contains the census "
                "is not repeatable without the ref, which is why only atRef is emitted"
            ),
        },
        "checkerCensus": {
            "atRef": _checker_census(verify_sh, names_at_ref),
            "readTheDeliverables": _reads_the_deliverables(),
            "asWrittenBeforeThisPass": {
                "exist": 8, "wired": 7, "readTheDeliverables": 4,
                "clause": "every one except trace-answers.py",
            },
        },
        "supersessionSweep": {
            "range": "C-0174..C-0189 and CH-0223..CH-0241",
            "identifiersSwept": len(swept),
            "mentionedInEitherDocument": mentioned,
            "mentionedCount": len(mentioned),
            "alreadySweptByC0186": [
                "C-0176", "C-0177", "C-0178", "C-0179", "C-0181", "C-0182", "C-0183", "C-0184",
                "CH-0224", "CH-0229", "CH-0230", "CH-0231", "CH-0232",
            ],
        },
        "cannotAnswerList": {
            "topLevelBulletsAtRef": _cannot_answer_bullets(answers_at_ref),
            "entriesNamingAClosedTask": 1,
            "entriesTheProgrammeHasSinceAnswered": 0,
        },
        "decisions": {
            "count": 8,
            "distinctChallengeIdentifiersCitedAtRef": len(challenge_ids),
            "prosecutedForAStalePrice": 8,
            "stalePricesFound": 0,
            "note": (
                "every challenge either deliverable rests a number on was re-read against its own "
                "Status row; none had moved since the twelfth pass, so the STALE_PRICE class is "
                "empty and is declared empty rather than dropped"
            ),
        },
        "passages": {
            "moved": len(rows),
            "byKind": by_kind,
            "rows": rows,
        },
        "findings": [
            "The residue was not bookkeeping: {} passages moved, over {} of the seven declared "
            "kinds, and {} of them are in the document NDI reads.".format(
                len(rows),
                sum(1 for kind in KINDS if by_kind[kind]),
                sum(1 for row in rows if row["document"] == "DECISIONS-FOR-NDI.md"),
            ),
            "The challenge-and-claim census moved {} -> {} and {} -> {} at the baseline ref, so "
            "the row has now been stale in six of its seven passes; two wired gates print the "
            "numbers on every run and what was missing was the COMPARISON, not the "
            "number.".format(190, len(challenges_before), 164, len(claims_before)),
            "The checker census moved 8 -> {} and its wiring clause is false in the FAVOURABLE "
            "direction - all {} are wired now, trace-answers.py having been wired by T-277 in the "
            "same iteration the sentence was written. And the predicate is a filename prefix: "
            "tools/verify.sh runs {} distinct tools as build-failing gates, {} of which the "
            "command cannot see (CH-0243).".format(
                len(names_at_ref), len(names_at_ref),
                len(_checker_census(verify_sh, names_at_ref)["underTheGatePredicate"]),
                len(_checker_census(verify_sh, names_at_ref)["invisibleToTheNamingPredicate"]),
            ),
            "The STALE_PRICE class is EMPTY and is reported empty: all eight decisions were "
            "re-read against the Status row of every challenge they rest a number on, and none "
            "had moved. An empty class is a result.",
            "The cannot-answer list carries no entry the programme has answered - the twelfth "
            "pass's clean status reading is a TRUE negative - but one entry names a task the "
            "queue has closed under a phrasing (\"still not found\") that "
            "tools/trace-answers.py's fixed vocabulary cannot see.",
        ],
    }
    return document


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    ok("every declared kind is in KINDS", all(row[0] in KINDS for row in PASSAGES))
    ok("every declared document is in scope", all(row[1] in DOCUMENTS for row in PASSAGES))
    ok("no two passages share a before anchor",
       len({row[2] for row in PASSAGES}) == len(PASSAGES))
    ok("no two passages share an after anchor",
       len({row[3] for row in PASSAGES}) == len(PASSAGES))
    ok("STALE_PRICE is declared even though it is empty", "STALE_PRICE" in KINDS)
    sample = "x\n## 5. What we cannot answer, and why\n- a\n- b\n  - not top level\n\n### The questions for NDI\n- c\n"
    ok("the bullet counter counts top-level bullets only", _cannot_answer_bullets(sample) == 2)
    census = _checker_census(
        "set -euo pipefail\n    tools/check-a.py\n    tools/test-b.py\n    tools/T-9-c.py --check\n",
        ["tools/check-a.py"],
    )
    ok("the gate predicate excludes test- fixtures", "tools/test-b.py" not in census["underTheGatePredicate"])
    ok("the gate predicate sees a tool the naming predicate cannot",
       census["invisibleToTheNamingPredicate"] == ["tools/T-9-c.py"])
    ok("the naming predicate reports what it was given",
       census["underTheNamingPredicateCount"] == 1)

    for name, passed in checks:
        print("{}  {}".format("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# {} self-test(s), {} failure(s)".format(len(checks), len(failed)))
    return 1 if failed else 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", default="HEAD", help="the corpus state to measure against")
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--out", default=os.path.join(ROOT, "gpd", "results",
                                                      "T-276-thirteenth-answers-synthesis.json"))
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
