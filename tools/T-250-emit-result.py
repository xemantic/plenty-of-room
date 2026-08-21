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
"""Emits `gpd/results/T-250-prose-interpolation-sweep.json`.

Everything numeric is DERIVED here and nothing is typed: the census before (from `git show
HEAD:`), the census after (from the working tree), the movement by kind and the staleness
identity (`tools/T-250-movement.py`), the re-emission order and its dependency-constraint count
(`tools/reemission-order.py`), and the mutation coverage of the predicate and of the promotion
(`tools/T-249-mutation-test.py`, `tools/T-250-mutation-test.py`).

`tools/T-250-body.json` carries the prose, and a handful of cheap-bound values by hand which are
ASSERTED against what is derived -- the emitter refuses to write a stale file.

    tools/T-250-emit-result.py
    tools/T-250-emit-result.py --check     # derive and assert, write nothing
"""

import importlib.util
import json
import os
import re
import shutil
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "gpd", "results", "T-250-prose-interpolation-sweep.json")
BODY = os.path.join(ROOT, "tools", "T-250-body.json")
CLAIM_TEMPLATE = os.path.join(ROOT, "tools", "C-0156-claim-template.md")
CLAIM = os.path.join(ROOT, "gpd", "claims", "C-0156-prose-interpolation-sweep.md")


def _module(name, filename):
    spec = importlib.util.spec_from_file_location(
        name, os.path.join(ROOT, "tools", filename))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


hygiene = _module("hygiene", "check-result-file-hygiene.py")
movement = _module("movement", "T-250-movement.py")
order_tool = _module("order", "reemission-order.py")
predicate_mutations = _module("m249", "T-249-mutation-test.py")
gate_mutations = _module("m250", "T-250-mutation-test.py")

RESULTS = os.path.join(ROOT, "gpd", "results")


PLACEHOLDER = re.compile(r"\{\{([^{}]+)\}\}")


def render_claim(document, template=CLAIM_TEMPLATE, out=CLAIM):
    """Write the claim by substituting `{{json/pointer}}` slots out of the emitted result.

    **Every number in the claim is therefore grepped out of the artifact rather than typed**, which
    is `SESSION-PROMPT.md`'s rule executed by the machine instead of by hand.  And the step
    ASSERTS its own completion: an unsubstituted `{{…}}` reaching `gpd/claims/` would be exactly
    the defect this task is about — a rendering step that fails silently and is visible only to
    somebody who reads the emitted artifact (`C-0127`'s `String.format` family, `C-0153`'s prose
    family).  A missing pointer raises before anything is written.
    """
    text = open(template, encoding="utf-8").read()
    missing = []

    def value_at(pointer):
        node = document
        for part in pointer.strip("/").split("/"):
            node = node[int(part)] if part.isdigit() else node[part]
        return node

    def substitute(match):
        try:
            value = value_at(match.group(1))
        except (KeyError, IndexError, TypeError, ValueError):
            missing.append(match.group(1))
            return match.group(0)
        if isinstance(value, float) and value == int(value):
            value = int(value)
        if isinstance(value, int) and not isinstance(value, bool) and value >= 10000:
            return f"{value:,}".replace(",", "\u202f")
        return str(value)

    rendered = PLACEHOLDER.sub(substitute, text)
    if missing:
        raise SystemExit(f"CLAIM TEMPLATE — no such pointer: {sorted(set(missing))}")
    left = PLACEHOLDER.findall(rendered)
    if left:
        raise SystemExit(f"CLAIM TEMPLATE — {len(left)} placeholder(s) survived: {left[:5]}")
    with open(out, "w", encoding="utf-8") as handle:
        handle.write(rendered)
    return out


def resolve(ref):
    return subprocess.run(["git", "-C", ROOT, "rev-parse", ref],
                          capture_output=True, text=True, check=True).stdout.strip()


def corpus_at(ref, into):
    """The committed corpus AT A NAMED REF, materialised so the census can be run over it.

    **A result file whose subject is the corpus must name the corpus state it measured, or it can
    never be re-run.**  `T-249`'s emitter hardwires `HEAD`, so the moment this task repaired the
    corpus its committed file stopped being reproducible by its own code — the emitter now exits
    with `BODY IS STALE — … says 757.0, derived 778.0`, which is correct arithmetic about a
    different corpus.  Taking the ref as an argument, defaulting to `HEAD`, and RECORDING the
    resolved SHA in the emitted file is the whole repair, and it costs four lines.
    """
    shutil.rmtree(into, ignore_errors=True)
    os.makedirs(into)
    listing = subprocess.run(
        ["git", "-C", ROOT, "ls-tree", "--name-only", "-r", ref, "gpd/results/"],
        capture_output=True, text=True, check=True).stdout.split()
    for name in listing:
        if not name.endswith(".json"):
            continue
        blob = subprocess.run(["git", "-C", ROOT, "show", f"{ref}:{name}"],
                              capture_output=True, text=True, check=True).stdout
        with open(os.path.join(into, os.path.basename(name)), "w") as handle:
            handle.write(blob)
    return into


def census(root):
    found = hygiene.check_prose_precision(root)
    fields = {(path, pointer) for path, pointer, _, _ in found}
    files = {path for path, _, _, _ in found}
    sites = {(os.path.basename(path), re.sub(r"/\d+", "/*", pointer))
             for path, pointer, _, _ in found}
    return dict(tokens=len(found), stringFields=len(fields), files=len(files),
                pointerSites=len(sites), scanned=len(hygiene.result_files(root)))


def narrow_census(root):
    """The same census under `T-249`'s own trailing guard, so the widening is measured."""
    shipped = hygiene.PROSE_NUMBER
    hygiene.PROSE_NUMBER = re.compile(
        r"(?<![\w.])(\d+\.\d+(?:[eE][+-]?\d+)?)(?!\w)(?!\.\d)")
    try:
        return census(root)
    finally:
        hygiene.PROSE_NUMBER = shipped


def bare_number_string_leaves(root):
    """`CH-0205`: string leaves whose WHOLE value is a number, at any precision.

    The prose gate catches the ones above nine significant digits; this is the class they are
    drawn from, and it is a statement about TYPE rather than about precision.
    """
    count, files = 0, set()
    for path in hygiene.result_files(root):
        for _, text in hygiene._strings(hygiene._load(path)):
            try:
                float(str(text).strip())
            except ValueError:
                continue
            count += 1
            files.add(os.path.basename(path))
    return dict(leaves=count, files=len(files), scanned=len(hygiene.result_files(root)))


def string_typed_numeric_channels(root):
    """`CH-0205`: keys a study reads with `.toDouble()` that are a STRING leaf somewhere."""
    string_keys = set()
    for path in hygiene.result_files(root):
        def walk(node):
            if isinstance(node, dict):
                for key, value in node.items():
                    if isinstance(value, (dict, list)):
                        walk(value)
                    elif isinstance(value, str):
                        try:
                            float(value.strip())
                        except ValueError:
                            continue
                        string_keys.add(key)
            elif isinstance(node, list):
                for value in node:
                    walk(value)
        walk(hygiene._load(path))
    read = set()
    total = 0
    for dirpath, _, names in os.walk(os.path.join(ROOT, "src", "main", "kotlin")):
        for name in names:
            text = open(os.path.join(dirpath, name), encoding="utf-8").read()
            for match in re.finditer(
                    r'getValue\("([^"]+)"\)\.jsonPrimitive\.content\.toDouble\(\)', text):
                total += 1
                if match.group(1) in string_keys:
                    read.add(match.group(1))
    return dict(literalKeyReads=total, keysAlsoEmittedAsAString=len(read),
                keys=sorted(read))


def site_classes(root):
    """The pointer sites of the baseline census, split into the two repair classes.

    A **bare-number string** is a leaf whose WHOLE value is the number (a `Map<String, String>`
    parameter entry); a **sentence** is everything else.  The split is what makes the source-side
    work predictable before any edit: the first class takes one mechanical rewrite, the second
    has to be read.
    """
    bare, sentence = set(), set()
    for path, pointer, literal, text in hygiene.check_prose_precision(root):
        site = (os.path.basename(path), re.sub(r"/\d+", "/*", pointer))
        (bare if str(text).strip().lstrip("-") == literal else sentence).add(site)
    sentence -= bare
    return dict(sites=len(bare | sentence), bareNumberStrings=len(bare),
                sentences=len(sentence))


def reemission(files):
    tags = [order_tool.tag_of(name) for name in files]
    document = json.load(open(order_tool.CENSUS))
    reads = order_tool.edges_from_census(document)
    pairs = order_tool.dependency_pairs(tags, reads)
    return dict(files=len(tags), constraintsFromTheCensus=len(pairs),
                order=order_tool.order(tags, reads))


def predicate_coverage():
    """Mutation coverage of the `--prose` PREDICATE (`T-249`'s table, plus `T-250`'s rows)."""
    rows = predicate_mutations.mutations(hygiene)
    tests = ([(text, count, description) for text, count, description in hygiene.PROSE_TESTS]
             + [(text, tokens, description)
                for text, tokens, description in hygiene.PROSE_TOKEN_TESTS])
    per_mutation, reached = [], set()
    for name, predicate in rows:
        failed = 0
        for index, (text, expected, _) in enumerate(tests):
            found = predicate(text)
            actual = found if isinstance(expected, list) else len(found)
            if actual != expected:
                failed += 1
                reached.add(index)
        per_mutation.append(dict(mutation=name, namedTestsFailed=failed))
    return dict(mutations=len(rows), namedTests=len(tests),
                rowsReachedBySomeMutation=len(reached),
                mutationsPassingEveryTest=sum(
                    1 for row in per_mutation if row["namedTestsFailed"] == 0),
                perMutation=per_mutation)


def promotion_coverage():
    """Mutation coverage of the PROMOTION -- the exit policy and the token-level allowlist."""
    exit_tests = hygiene.PROSE_EXIT_TESTS
    allow_tests = hygiene.PROSE_ALLOWLIST_TESTS
    per_mutation, reached = [], set()
    for name, direction, policy in gate_mutations.exit_mutations(hygiene):
        failed = 0
        for index, (found, cens, expected, _) in enumerate(exit_tests):
            if policy(found, cens) != expected:
                failed += 1
                reached.add(("exit", index))
        per_mutation.append(dict(mutation=name, direction=direction, namedTestsFailed=failed))
    for name, direction, predicate in gate_mutations.allowlist_mutations(hygiene):
        failed = 0
        for index, (basename, literal, admitted, _) in enumerate(allow_tests):
            if predicate(os.path.join(hygiene.RESULTS, basename), literal) != admitted:
                failed += 1
                reached.add(("allow", index))
        per_mutation.append(dict(mutation=name, direction=direction, namedTestsFailed=failed))
    return dict(mutations=len(per_mutation),
                namedTests=len(exit_tests) + len(allow_tests),
                rowsReachedBySomeMutation=len(reached),
                mutationsPassingEveryTest=sum(
                    1 for row in per_mutation if row["namedTestsFailed"] == 0),
                perMutation=per_mutation)


RENDER_TESTS = [
    ("a pointer that resolves", "x is {{a/b}}", {"a": {"b": 3}}, "x is 3", None),
    ("an integer above ten thousand is spaced", "{{n}}", {"n": 12345}, "12\u202f345", None),
    ("a float that is a whole number renders as an integer", "{{n}}", {"n": 47.0}, "47", None),
    ("an array index", "{{a/1/b}}", {"a": [{"b": 1}, {"b": 2}]}, "2", None),
    ("a MISSING pointer refuses to write", "{{a/z}}", {"a": {"b": 3}}, None, "no such pointer"),
    ("a pointer into a non-object refuses to write", "{{a/b/c}}", {"a": {"b": 3}}, None,
     "no such pointer"),
]


def self_test():
    import tempfile
    failures = 0
    for name, template, document, expected, error in RENDER_TESTS:
        with tempfile.TemporaryDirectory() as scratch:
            source = os.path.join(scratch, "t.md")
            target = os.path.join(scratch, "o.md")
            open(source, "w", encoding="utf-8").write(template)
            try:
                render_claim(document, source, target)
                actual = open(target, encoding="utf-8").read()
                if error is not None:
                    failures += 1
                    print(f"SELF-TEST FAILED — {name}: expected a refusal, wrote {actual!r}")
                elif actual != expected:
                    failures += 1
                    print(f"SELF-TEST FAILED — {name}: expected {expected!r}, got {actual!r}")
            except SystemExit as refusal:
                if error is None or error not in str(refusal):
                    failures += 1
                    print(f"SELF-TEST FAILED — {name}: unexpected refusal {refusal}")
                elif os.path.exists(target):
                    failures += 1
                    print(f"SELF-TEST FAILED — {name}: refused AND wrote the file")
    print(f"{len(RENDER_TESTS) - failures} of {len(RENDER_TESTS)} emitter self-tests pass")
    return failures


def main(argv):
    if "--self-test" in argv:
        return 1 if self_test() else 0
    if self_test():
        return 1
    check_only = "--check" in argv
    baseline = "HEAD"
    if "--baseline" in argv:
        baseline = argv[argv.index("--baseline") + 1]
    baseline_sha = resolve(baseline)
    scratch = os.path.join(ROOT, "build-T250-head")
    before = census(corpus_at(baseline, scratch))
    before_narrow = narrow_census(scratch)
    after = census(RESULTS)
    affected = sorted({os.path.basename(path) for path, _, _, _
                       in hygiene.check_prose_precision(scratch)})
    shutil.rmtree(scratch, ignore_errors=True)

    scratch2 = corpus_at(baseline, os.path.join(ROOT, "build-T250-head2"))
    residue = dict(siteClassesAtTheBaseline=site_classes(scratch2),
                   bareNumberStringLeaves=bare_number_string_leaves(scratch2),
                   stringTypedNumericChannels=string_typed_numeric_channels(scratch2))
    wide_files = {os.path.basename(row[0]) for row in hygiene.check_prose_precision(scratch2)}
    shipped = hygiene.PROSE_NUMBER
    hygiene.PROSE_NUMBER = movement.T249_NUMBER
    narrow_files = {os.path.basename(row[0]) for row in hygiene.check_prose_precision(scratch2)}
    hygiene.PROSE_NUMBER = shipped
    residue["filesTheWidenedGuardAdded"] = sorted(wide_files - narrow_files)
    wide_tokens = {(os.path.basename(row[0]), row[1], row[2])
                   for row in hygiene.check_prose_precision(scratch2)}
    hygiene.PROSE_NUMBER = movement.T249_NUMBER
    narrow_tokens = {(os.path.basename(row[0]), row[1], row[2])
                     for row in hygiene.check_prose_precision(scratch2)}
    hygiene.PROSE_NUMBER = shipped
    residue["widenedGuardExtraFiles"] = len(
        {name for name, _, _ in wide_tokens - narrow_tokens})
    shutil.rmtree(scratch2, ignore_errors=True)

    moved = [movement.compare(os.path.join(RESULTS, name), baseline_sha)
             for name in affected]
    totals = {}
    for row in moved:
        for key, value in row.items():
            if isinstance(value, int):
                totals[key] = totals.get(key, 0) + value

    body = json.load(open(BODY, encoding="utf-8"))
    document = dict(body)
    document["census"] = dict(
        baselineRef=baseline_sha,
        beforeAtHead=before,
        beforeAtHeadUnderTheT249Guard=before_narrow,
        widenedGuardExtraTokens=before["tokens"] - before_narrow["tokens"],
        afterInTheWorkingTree=after,
    )
    document["reemission"] = reemission(affected)
    document["movementByKind"] = dict(
        files=len(moved), totals=totals,
        perFile=[row for row in moved
                 if any(isinstance(v, int) and v for k, v in row.items())],
    )
    predicate_rows = predicate_coverage()
    residue["t249GuardNamedTestsFailed"] = next(
        row["namedTestsFailed"] for row in predicate_rows["perMutation"]
        if "trailing guard restored" in row["mutation"] and "T-249" in row["mutation"])
    document["residue"] = residue
    document["gate"] = dict(
        line="tools/check-result-file-hygiene.py --prose",
        wasAnAuditIn="T-249",
        isAGate=hygiene.PROSE_IS_A_GATE,
        allowlistEntries=sum(len(v) for v in hygiene.PROSE_ALLOWLIST.values()),
        allowlistIsTokenLevel=True,
        allowlistedFiles=sorted(hygiene.PROSE_ALLOWLIST),
        selfTests=(len(hygiene.CONVERSION_TESTS) + len(hygiene.DEPARTURE_TESTS)
                   + len(hygiene.SATURATION_TESTS) + len(hygiene.SCOPE_TESTS)
                   + len(hygiene.GATE_TESTS) + len(hygiene.EXCLUDED_DEPARTURE_KEYS)
                   + len(hygiene.PROSE_TESTS) + len(hygiene.PROSE_TOKEN_TESTS)
                   + len(hygiene.PROSE_ALLOWLIST_TESTS) + len(hygiene.PROSE_EXIT_TESTS) + 2),
    )
    document["mutationCoverage"] = dict(
        predicate=predicate_coverage(),
        promotion=promotion_coverage(),
    )

    for key, expected in body.get("assertedCheapBounds", {}).items():
        actual = {
            "tokensAtHead": before["tokens"],
            "filesAtHead": before["files"],
            "pointerSitesAtHead": before["pointerSites"],
            "dependencyConstraints": document["reemission"]["constraintsFromTheCensus"],
            "tokensAfter": after["tokens"],
            "filesAfter": after["files"],
            "widenedGuardExtraTokens": document["census"]["widenedGuardExtraTokens"],
        }[key]
        if actual != expected:
            print(f"ASSERTION FAILED — {key}: body says {expected}, derived {actual}")
            return 1
    document.pop("assertedCheapBounds", None)

    census_before = document["census"]["beforeAtHead"]
    census_after = document["census"]["afterInTheWorkingTree"]
    reem = document["reemission"]
    predicate = document["mutationCoverage"]["predicate"]
    promotion = document["mutationCoverage"]["promotion"]

    document["predicates"] = [
        dict(id="P1", statement="the census is re-run over the committed corpus before any repair",
             met=True,
             evidence=(f"{census_before['tokens']} tokens in {census_before['stringFields']} "
                       f"string fields at {census_before['pointerSites']} JSON pointer sites in "
                       f"{census_before['files']} of {census_before['scanned']} committed files")),
        dict(id="P2", statement="every affected file is repaired at its source call sites and "
                                "re-emitted",
             met=census_after["files"] == 0,
             evidence=f"{reem['files']} files re-emitted; the census now reads "
                      f"{census_after['tokens']} tokens in {census_after['files']} files"),
        dict(id="P3", statement="the order is reemission-order.py's topological sort and its "
                                "constraint count is asserted non-zero",
             met=reem["constraintsFromTheCensus"] > 0,
             evidence=f"{reem['constraintsFromTheCensus']} dependency constraints inside the set, "
                      f"derived from the reader census rather than typed"),
        dict(id="P4", statement="what moved is reported by kind against each file's committed "
                                "version",
             met=True,
             evidence=", ".join(f"{k}={totals.get(k, 0)}" for k in
                                ("prose", "wording", "departure", "numeric", "boolean",
                                 "added", "removed"))),
        dict(id="P5", statement="nothing is stale, as an identity: every moved prose token is "
                                "exactly the rounding its own call site declares",
             met=totals.get("tokensUnexplained", 0) == 0,
             evidence=f"{totals.get('tokensExplained', 0)} of "
                      f"{totals.get('movedTokens', 0)} moved tokens explained, "
                      f"{totals.get('tokensUnexplained', 0)} unexplained"),
        dict(id="P6", statement="the --prose line reads 0 in 0 and is promoted to a gate",
             met=census_after["tokens"] == 0 and hygiene.PROSE_IS_A_GATE,
             evidence=f"{census_after['tokens']} tokens in {census_after['files']} files; "
                      f"PROSE_IS_A_GATE = {hygiene.PROSE_IS_A_GATE}; wired in tools/verify.sh"),
        dict(id="P7", statement="the promotion is mutation-tested in both directions",
             met=promotion["mutationsPassingEveryTest"] == 0,
             evidence=f"{promotion['mutations']} mutations over {promotion['namedTests']} named "
                      f"tests, {promotion['mutationsPassingEveryTest']} passing every one, "
                      f"{promotion['rowsReachedBySomeMutation']} of {promotion['namedTests']} "
                      f"rows reached; the predicate separately at "
                      f"{predicate['mutations']}/{predicate['namedTests']}/"
                      f"{predicate['mutationsPassingEveryTest']}"),
    ]

    document["falsifiers"] = [
        dict(id="F1", statement="a re-emission moves a numeric field, a boolean or a verdict",
             fired=bool(totals.get("numeric", 0) or totals.get("boolean", 0)
                        or totals.get("wording", 0)),
             outcome=f"{totals.get('prose', 0)} prose fields moved; "
                     f"{totals.get('numeric', 0)} numeric, {totals.get('boolean', 0)} boolean, "
                     f"{totals.get('wording', 0)} wording, {totals.get('departure', 0)} "
                     f"departure, {totals.get('added', 0)} added, "
                     f"{totals.get('removed', 0)} removed"),
        dict(id="F2", statement="a moved prose token is NOT the rounding its call site declares",
             fired=totals.get("tokensUnexplained", 0) > 0,
             outcome=f"{totals.get('tokensUnexplained', 0)} of "
                     f"{totals.get('movedTokens', 0)} unexplained"),
        dict(id="F3", statement="a repaired file still carries a token",
             fired=census_after["tokens"] > 0,
             outcome=f"{census_after['tokens']} tokens in {census_after['files']} files after "
                     f"the sweep"),
        dict(id="F4", statement="reemission-order.py reports zero dependency constraints",
             fired=reem["constraintsFromTheCensus"] == 0,
             outcome=f"{reem['constraintsFromTheCensus']} constraints over "
                     f"{reem['files']} files"),
        dict(id="F5", statement="a mutation of the promoted gate passes every named test",
             fired=promotion["mutationsPassingEveryTest"] > 0,
             outcome=f"{promotion['mutationsPassingEveryTest']} of {promotion['mutations']} "
                     f"pass every named test"),
        dict(id="F6", statement="the widened trailing guard costs a file the sweep did not "
                                "already own",
             fired=bool(residue["filesTheWidenedGuardAdded"]),
             outcome=f"{document['census']['widenedGuardExtraTokens']} extra tokens, all in "
                     f"files already in the sweep"),
    ]

    if check_only:
        print("derived and asserted; nothing written")
        print(json.dumps(document["census"], indent=2))
        print(json.dumps(document["movementByKind"]["totals"], indent=2))
        return 0

    with open(OUT, "w") as handle:
        json.dump(document, handle, indent=2)
        handle.write("\n")
    render_claim(document)
    print(f"written to {OUT}")
    print(f"written to {CLAIM}")
    print(f"  before: {before}")
    print(f"  after:  {after}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
