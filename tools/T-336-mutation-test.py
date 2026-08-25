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
# T-336 -- MUTATION COVERAGE FOR `tools/T-336-pinned-count-census.py`.
#
#     tools/T-336-mutation-test.py
#
# `C-0176`'s standard in BOTH directions: every rule must fail a NAMED test when it is reverted
# and when it is over-widened, and a mutation must replace a rule WHOLESALE rather than widening
# it to `original|mutant`, which leaves the original matching everything it used to.
#
# `C-0185`/`CH-0237`'s subtracted baseline runs FIRST and its named failures are printed: without
# it a fixture defect reads as `0 survivors` in the quiet direction, or as every row killed by one
# and the same error in the loud one, and the headline means nothing either way.  The anchor count
# is asserted at exactly 1 per row -- a harness that does not assert its anchors reports `killed`
# off a mutation that never applied.
#
# Several rows restore a defect this tool's OWN first drafts carried, which is the point of
# keeping them: the symmetric trailing guard `(?![\w.])` that refuses every number at the end of
# a sentence (`CLAUDE.md` records it twice and it bit a third time here), the missing
# identifier guard that read `295` out of `T-295-mutation-input-census.py`, the missing unit that
# attributed one tool's two printed counts to both quantities, and the forward sense on a
# directory anchor.
#
# The row shape is `killed-by`, declared in `tools/P-31-harness-census.py`'s `HARNESSES` table
# (`C-0206`: a harness's output is an INTERFACE and it is declared, never inferred).  The killers
# go on their OWN lines, because printed after the name they are captured as part of the LABEL.
"""Mutation coverage for tools/T-336-pinned-count-census.py."""

import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SUBJECT = "T-336-pinned-count-census.py"

#: (name, file, old, new).  `old` must occur EXACTLY once in the subject.
MUTATIONS = (
    # --- classification, and the third state (`C-0182`) ------------------------------------------
    (
        "NARROW the refusal away, so a state-shaped key nobody declared reads as pinned",
        SUBJECT,
        '    for key in path:\n        if _STATE_SHAPED.search(key):\n            return "UNDECLARED"',
        '    for key in []:\n        if _STATE_SHAPED.search(key):\n            return "UNDECLARED"',
    ),
    (
        "WIDEN the refusal to every key, so a plain subtotal is refused too",
        SUBJECT,
        '_STATE_SHAPED = re.compile(r"^(at[A-Z]|as[A-Z]|working[A-Z]|this[A-Z]|before[A-Z])|"\n'
        '                           r"([Tt]ree|Ref|Pass|PassesTree)$")',
        '_STATE_SHAPED = re.compile(r".")',
    ),
    (
        "NARROW the default from pinned-by-file to UNDECLARED, this tool's own first draft",
        SUBJECT,
        '    for key in path:\n        if _STATE_SHAPED.search(key):\n            return "UNDECLARED"\n    return "PINNED"',
        '    for key in path:\n        if _STATE_SHAPED.search(key):\n            return "UNDECLARED"\n    return "UNDECLARED"',
    ),
    (
        "NARROW the UNPINNED vocabulary away, so a working-tree reading reads as pinned",
        SUBJECT,
        '        if key in UNPINNED_KEYS:\n            return "UNPINNED"',
        '        if key in ():\n            return "UNPINNED"',
    ),
    # --- arm 1: a record's state must RESOLVE (`CH-0246`) -----------------------------------------
    (
        "WIDEN the baselineRef test to any string, so `HEAD` passes as a pinned state",
        SUBJECT,
        '        if not isinstance(base, str) or not _SHA.match(base):\n'
        '            defects.append(\n'
        '                "UNRESOLVED-BASELINE',
        '        if False:\n'
        '            defects.append(\n'
        '                "UNRESOLVED-BASELINE',
    ),
    (
        "WIDEN the resolved-sha pattern to any hex, so an abbreviated ref reads as resolved",
        SUBJECT,
        '_SHA = re.compile(r"^[0-9a-f]{40}$")',
        '_SHA = re.compile(r"^[0-9a-f]+$")',
    ),
    (
        "NARROW the undeclared-key arm away entirely",
        SUBJECT,
        '        if verdict == "UNDECLARED":\n            defects.append(',
        '        if False:\n            defects.append(',
    ),
    # --- arm 2: a quantity may not be declared against a tool nothing runs -------------------------
    (
        "NARROW arm 2's existence check away",
        SUBJECT,
        '        if q.deriver not in present:\n            defects.append("MISSING-DERIVER',
        '        if False:\n            defects.append("MISSING-DERIVER',
    ),
    (
        "NARROW arm 2's executable check away",
        SUBJECT,
        '        elif not present.get(q.deriver):',
        '        elif False:',
    ),
    (
        "NARROW arm 2's reachability check away, so a deriver nothing runs may be declared",
        SUBJECT,
        '        elif q.deriver not in union:',
        '        elif False:',
    ),
    (
        "WIDEN arm 2's union to every tool present, so an unreachable Exec task counts",
        SUBJECT,
        '    union = set(reading["union"])',
        "    union = set(present)",
    ),
    # --- what a deliverable may quote, which is the whole predicate --------------------------------
    (
        # RE-ANCHORED by `T-340`: `pinned_values` now matches through `registry_quantity_of`,
        # which orphaned this anchor.  `C-0185`: a mutation anchor is a reference into somebody
        # else's source and a refactor orphans it -- the harness's `count == 1` assertion is what
        # said so, loudly, instead of reporting `killed` off a mutation that never applied.
        "NARROW pinned_values to accept an UNPINNED record too -- the defect this tool exists for",
        SUBJECT,
        '            if q is not None and classify(path) == "PINNED":',
        "            if q is not None:",
    ),
    (
        "NARROW pinned_values' resolved-ref requirement, so a file with no baselineRef pins values",
        SUBJECT,
        '        if not (isinstance(base, str) and _SHA.match(base)):\n            continue',
        "        if False:\n            continue",
    ),
    (
        # RE-ANCHORED by `T-340`, for the same reason as the row above.
        "WIDEN the record-leaf match to a suffix of length one, so any `count` pins any quantity",
        SUBJECT,
        "        if body and len(stripped) >= len(body) and stripped[-len(body):] == body:",
        "        if body and stripped[-1:] == body[-1:]:",
    ),
    # --- struck text, and link targets: `C-0071` and `C-0196` --------------------------------------
    (
        "NARROW the strike blanking away, so a withdrawn figure reads as a live assertion",
        SUBJECT,
        '    for match in re.finditer(r"~~(.+?)~~", text, re.S):\n        for index in range(match.start(), match.end()):',
        '    for match in re.finditer(r"~~(.+?)~~", text, re.S):\n        for index in range(match.start(), match.start()):',
    ),
    (
        "NARROW the link-target blanking away, so a filename's digits become a figure",
        SUBJECT,
        '    for match in re.finditer(r"\\]\\(([^)\\s]*)\\)", text):',
        '    for match in re.finditer(r"\\](\\Z)\\(\\)", text):',
    ),
    # --- the number predicate, and the guard that bit three times ----------------------------------
    (
        "WIDEN the trailing guard back to the symmetric mirror, which refuses a number at the "
        "end of a sentence -- CLAUDE.md's own recorded trap, restored",
        SUBJECT,
        '_NUMBER = re.compile(r"(?<![\\w.\\-])\\d{1,4}(?![\\w-])(?!\\.\\d)|" + SPELLED.pattern, re.I)',
        '_NUMBER = re.compile(r"(?<![\\w.\\-])\\d{1,4}(?![\\w.\\-])|" + SPELLED.pattern, re.I)',
    ),
    (
        "NARROW the leading identifier guard away, so `T-295` yields the figure 295",
        SUBJECT,
        '_NUMBER = re.compile(r"(?<![\\w.\\-])\\d{1,4}(?![\\w-])(?!\\.\\d)|" + SPELLED.pattern, re.I)',
        '_NUMBER = re.compile(r"\\d{1,4}(?![\\w-])(?!\\.\\d)|" + SPELLED.pattern, re.I)',
    ),
    (
        "WIDEN the verb set back to `is|are|=`, which attributes any nearby number to any anchor",
        SUBJECT,
        '_VERB = r"(?:reports?|returns?|reads?|prints?|gives?|shows?|finds?)"',
        '_VERB = r"(?:reports?|returns?|reads?|prints?|gives?|shows?|finds?|is|are|=)"',
    ),
    (
        "NARROW the spelled-numeral parser away, so a spelled headline is invisible",
        SUBJECT,
        "    match = SPELLED.fullmatch(text.strip())\n    if not match:\n        return None",
        "    match = None\n    if not match:\n        return None",
    ),
    # --- the anchor, which is a DERIVATION and not a subject word ----------------------------------
    (
        "NARROW the bare-directory guard, so every [CH-0286](gpd/challenges/...) link anchors",
        SUBJECT,
        '        return re.escape(anchor) + r"(?![A-Za-z0-9])"',
        "        return re.escape(anchor)",
    ),
    (
        "NARROW the unit requirement away, so one tool's two printed counts attribute to both",
        SUBJECT,
        "                    if number and (unit is None\n"
        "                                   or re.search(unit, after[unit_at:unit_at + 40], re.I)):",
        "                    if number:",
    ),
    (
        "WIDEN the forward sense onto directory anchors, which report nothing",
        SUBJECT,
        '                verb = None if anchor.endswith("/") else re.search(_VERB, after, re.I)',
        "                verb = re.search(_VERB, after, re.I)",
    ),
    (
        "NARROW the reverse sense's noun away, so it becomes 'the last number before this anchor'",
        SUBJECT,
        '                    r"(?P<figure>" + _NUMBER.pattern + r")\\s+" + re.escape(q.noun)\n'
        '                    + r"[^.;:\\n]{0,60}$", before, re.I)',
        '                    r"(?P<figure>" + _NUMBER.pattern + r")[^.;:\\n]{0,60}$", before, re.I)',
    ),
    (
        "WIDEN the reverse sense onto command anchors as well as directories",
        SUBJECT,
        '                if not (q.noun and anchor.endswith("/")):',
        "                if not q.noun:",
    ),
    # --- the second way to obey the rule: a sha named in the figure's own sentence ---------------
    (
        "NARROW the sha escape away, so a HISTORICAL figure quoted with its state is a defect",
        SUBJECT,
        "            if _SHA_IN_PROSE.search(sentence):\n                continue",
        "            if False:\n                continue",
    ),
    (
        "WIDEN the sha escape to the whole LINE, so a sha anywhere on it pins every figure",
        SUBJECT,
        "            sentence = sentence_around(\n"
        "                live_text, offsets[line - 1] + (where if where >= 0 else 0))",
        "            sentence = lines[line - 1]",
    ),
    (
        "WIDEN the sha pattern to any hex word, so `abcdef` reads as a state",
        SUBJECT,
        '_SHA_IN_PROSE = re.compile(r"(?<![0-9a-f])[0-9a-f]{7,40}(?![0-9a-f])")',
        '_SHA_IN_PROSE = re.compile(r"(?<![0-9a-f])[0-9a-f]{4,40}(?![0-9a-f])")',
    ),
    # --- the residue, which C-0209 requires to be printed and honest -------------------------------
    (
        "NARROW the residue floor to nothing, so it lists the corpus's own numbering",
        SUBJECT,
        "            floor = min(floors) if floors else 10",
        "            floor = 0",
    ),
    (
        "WIDEN the residue to every line, not only a flagged one",
        SUBJECT,
        "        for line in sorted(flagged):\n            floors = ",
        "        for line in range(1, len(live) + 1):\n            floors = ",
    ),
    # --- census-family discovery, and the git-dependent arm's VISIBLE refusal (`C-0195`) ----------
    (
        "WIDEN census discovery to every result file, so a physics file is read for counts",
        SUBJECT,
        "        if text is None or not any(marker in text for marker in CENSUS_MARKERS):",
        "        if text is None:",
    ),
    (
        "NARROW the .git skip away, so the re-derivation arm degrades instead of refusing",
        SUBJECT,
        '    if not os.path.exists(os.path.join(tree.root, ".git")):',
        "    if False:",
    ),
    (
        "NARROW quantity()'s refusal into a default, which is C-0182's absence-read-as-an-answer",
        SUBJECT,
        '    if name not in QUANTITY_BY_NAME:\n        raise KeyError(',
        "    if False:\n        raise KeyError(",
    ),

    # --- arm C -- a registry quantity at an uncommitted tree (`T-340`) ---------------------------
    (
        "NARROW arm C away entirely, so a registry quantity at a tree is not a defect",
        SUBJECT,
        "    for name, path, value, q in working_tree_records(tree, quantities):\n"
        "        if q is None:\n            continue",
        "    for name, path, value, q in working_tree_records(tree, quantities):\n"
        "        if True:\n            continue",
    ),
    (
        "NARROW the working-tree key expression, so two of the corpus's own 23 names escape it",
        SUBJECT,
        'r"WorkingTree|workingTree|ThisPassesTree|InTheTree|quietTree"',
        'r"WorkingTree|workingTree|ThisPassesTree"',
    ),
    (
        "WIDEN the working-tree key expression onto a COMMITTED state key, so every pinned "
        "reading reads as a tree reading",
        SUBJECT,
        'r"WorkingTree|workingTree|ThisPassesTree|InTheTree|quietTree"',
        'r"WorkingTree|workingTree|ThisPassesTree|InTheTree|quietTree|Ref"',
    ),
    (
        "NARROW registry_quantity_of so it does NOT strip the state key, after which one quantity "
        "read at a ref and at a tree are two different quantities and arm C is blind",
        SUBJECT,
        "    stripped = tuple(key for key in path\n"
        "                     if key not in PINNED_KEYS and key not in UNPINNED_KEYS\n"
        "                     and not _WORKING_TREE_KEY.search(key))",
        "    stripped = tuple(path)",
    ),
    (
        "WIDEN registry_quantity_of onto every path, so arm C fires on the before/after half of a "
        "repair -- kind A, which is legal and must stay legal",
        SUBJECT,
        "        if body and len(stripped) >= len(body) and stripped[-len(body):] == body:\n"
        "            return q\n    return None",
        "        if body:\n            return q\n    return None",
    ),
    (
        "NARROW arm C's reach to the census family, restoring the hole T-327's 173-entry block "
        "sits in",
        SUBJECT,
        "    for name in _result_names(tree):\n"
        "        text = tree.read(os.path.join(RESULTS, name))\n"
        "        if text is None:\n            continue",
        "    for name, text in [(name, json.dumps(document))\n"
        "                       for name, document in census_files(tree).items()]:\n"
        "        if text is None:\n            continue",
    ),
    (
        "NARROW _ref_for onto the file's baselineRef, so a block measured at its own commit is "
        "re-derived at the wrong state",
        SUBJECT,
        "            candidate = node.get(\"ref\")\n"
        "            if isinstance(candidate, str) and _SHA.match(candidate):\n"
        "                ref = candidate",
        "            candidate = None",
    ),
    (
        "WIDEN _ref_for onto any string, so `\"ref\": \"HEAD\"` is accepted as a pinned state",
        SUBJECT,
        '            if isinstance(candidate, str) and _SHA.match(candidate):\n'
        "                ref = candidate",
        "            if isinstance(candidate, str):\n                ref = candidate",
    ),
    (
        "NARROW the working-tree residue to zero, so a gate that comes clean stops saying what it "
        "does not reach (C-0209)",
        SUBJECT,
        "    return sum(1 for row in working_tree_records(tree, quantities) if row[3] is None)",
        "    return 0",
    ),
    (
        "NARROW PINNED_KEYS back, so the state a LATER pass can name is refused as undeclared",
        SUBJECT,
        'PINNED_KEYS = ("atRef", "atBaselineRef", "atTheCommitThatCarriedThisFile")',
        'PINNED_KEYS = ("atRef", "atBaselineRef")',
    ),
)


def _run(work):
    run = subprocess.run(
        [sys.executable, os.path.join(work, "tools", SUBJECT), "--self-test"],
        capture_output=True, text=True, cwd=work,
    )
    named = [line[6:].strip() for line in run.stdout.splitlines() if line.startswith("FAIL ")]
    finished = any(line.startswith("# ") and "self-test(s)" in line
                   for line in run.stdout.splitlines())
    return run.returncode, named, finished


def main():
    work = tempfile.mkdtemp(prefix="T-336-mutation.")
    try:
        shutil.copytree(os.path.join(ROOT, "tools"), os.path.join(work, "tools"))
        baseline_code, baseline_failures, baseline_finished = _run(work)
        if baseline_code != 0 or not baseline_finished:
            print("BASELINE IS NOT GREEN -- nothing below is a measurement")
            for name in baseline_failures:
                print("   baseline failure:", name)
            return 2
        print("baseline: green, 0 named failures")
        source = open(os.path.join(ROOT, "tools", SUBJECT), encoding="utf-8").read()
        survivors = 0
        for name, _path, old, _new in MUTATIONS:
            count = source.count(old)
            if count != 1:
                print("ANCHOR  %-70s occurs %d times, expected 1" % (name, count))
                survivors += 1
        target = os.path.join(work, "tools", SUBJECT)
        for name, _path, old, new in MUTATIONS:
            if source.count(old) != 1:
                continue
            open(target, "w", encoding="utf-8").write(source.replace(old, new, 1))
            code, named, finished = _run(work)
            open(target, "w", encoding="utf-8").write(source)
            if code == 0 or not finished:
                survivors += 1
                print("%-8s %-70s %s" % (
                    "SURVIVES", name,
                    "no named test failed" if finished else "the suite did not finish"))
            else:
                print("killed by %d named test(s)  %s" % (len(named), name))
                for failing in named[:3]:
                    print("        FAIL: %s" % failing)
        print("# %d mutation(s), %d survivor(s)" % (len(MUTATIONS), survivors))
        return 1 if survivors else 0
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
