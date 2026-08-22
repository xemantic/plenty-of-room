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
# T-281 -- what the `name the discharge` rule would have REFUSED, over the census's own history.
#
#     tools/T-281-history.py [--json]
#
# `P-29`'s standard, `P-30`'s and `CLAUDE.md`'s: a rule's firing rate is MEASURED over the corpus's
# own past, not argued -- *a drift checker's false positives cost more than its true ones* is a
# RATE, and a gate that fires on correct work is switched off within an iteration.
#
# Each revision of `tools/T-234-census.py` is extracted with `git archive` (never `git checkout`,
# which on this shared checkout reverts another agent's work) and run against the corpus AT THAT
# SAME REVISION.  Two sets come out: the family names the census EMITTED, and the family names it
# DECLARED.  The rule refuses their difference.
#
# A refusal is classified, and the classification is the part with content:
#
#   GENUINE    the default the tool applied was later CONTRADICTED -- occurrences of that family
#              belong to a discharge other than the census's own subject.  Answering the question
#              the rule forces would have changed the census.
#   PROMPT     the default was right, and the answer is a single claim already in the corpus.  The
#              refusal costs the author one line, once, forever.
#
# There is no third class, and that is a property of the rule rather than a convenience: it reads a
# DECLARATION, so it cannot mistake a declared family for an undeclared one.  Its false-positive
# rate against a declared registry is ZERO by construction, and what the history measures is the
# firing count and how much of it was load-bearing.
import argparse
import json
import os
import shutil
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
CENSUS = "tools/T-234-census.py"

sys.path.insert(0, HERE)
_census = __import__("T-234-census")

#: Which base family each of today's refined names was split OUT of.  `refine_placement` renames
#: `PLACEMENT` to `GRILLAGE` or `SQUARE`; `refine_width` renames `WIDTH` to `ROW_SPAN`.  The map is
#: a hand reading of those two functions and it is what lets a refusal at an OLD revision be
#: classified against today's answer: if a base family's occurrences are split across discharges
#: today, then asking its discharge back then had a real answer that the default got wrong.
SPLIT_OF = {
    "GRILLAGE": "PLACEMENT",
    "SQUARE": "PLACEMENT",
    "ROW_SPAN": "WIDTH",
}

GENUINE = "GENUINE"
PROMPT = "PROMPT"


def _revisions(ref="HEAD"):
    return subprocess.check_output(
        ["git", "log", "--format=%H %ad %s", "--date=short", ref, "--", CENSUS],
        cwd=ROOT, text=True,
    ).splitlines()


def _extract(sha, directory):
    tar = subprocess.check_output(["git", "archive", sha], cwd=ROOT)
    subprocess.run(["tar", "-x", "-C", directory], input=tar, check=True)


def _emitted(directory, tool=None):
    """{family: count} as the census AT `directory` reads the corpus AT `directory`."""
    tool = tool or os.path.join(directory, "tools", "T-234-census.py")
    result = subprocess.run(
        [sys.executable, tool, "--census", "--root", directory],
        capture_output=True, text=True,
    )
    if result.returncode:
        raise RuntimeError(result.stderr.strip().splitlines()[-1][:200])
    counts = {}
    for record in json.loads(result.stdout):
        counts[record["family"]] = counts.get(record["family"], 0) + 1
    return counts


def _declared(directory):
    """The family names that revision of the census DECLARES, read out of its own source."""
    source = os.path.join(directory, "tools", "T-234-census.py")
    result = subprocess.run(
        [
            sys.executable, "-c",
            "import importlib.util,sys,json;"
            "spec=importlib.util.spec_from_file_location('c', sys.argv[1]);"
            "m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);"
            "print(json.dumps(sorted(getattr(m, 'FAMILY_DISCHARGE', {}))))",
            source,
        ],
        cwd=os.path.join(directory, "tools"), capture_output=True, text=True,
    )
    if result.returncode:
        raise RuntimeError(result.stderr.strip().splitlines()[-1][:200])
    return set(json.loads(result.stdout))


def _today(directory):
    """{family: count} as TODAY's predicate reads the corpus at `directory`.

    This is what classifies an old refusal: it is today's answer to the question the rule would
    have forced back then.
    """
    counts = {}
    for path in _census.corpus_files(directory):
        if not _census.in_scope(path):
            continue
        try:
            with open(os.path.join(directory, path), encoding="utf-8") as handle:
                text = handle.read()
        except OSError:
            continue
        for family, _line, _offset, _token, _distance in _census.occurrences(text):
            counts[family] = counts.get(family, 0) + 1
    return counts


def _misdated(emitted, today):
    """{split name: count} for occurrences this revision filed under the WRONG discharge.

    A split name that the revision's own predicate already emits is a family it has separated, so
    its occurrences carry their own date whether or not the family is declared.  What is misdated
    is a split name today's predicate finds and the revision's does NOT -- those occurrences were
    inside a base family and inherited the census's own subject discharge.
    """
    return {
        name: count
        for name, count in today.items()
        if count
        and name not in emitted
        and _census.REGISTRY.discharge_of(name) != _census.SUBJECT
    }


def _classify(family, misdated):
    """`GENUINE` if occurrences this revision filed under `family` are misdated.

    That is the whole content of the classification: answering the question the rule forces would
    have CHANGED the census.  Everything else is a `PROMPT` -- the default was right and the answer
    is a single claim already in the corpus, so the refusal costs one line, once.
    """
    for name in misdated:
        if SPLIT_OF.get(name, name) == family:
            return GENUINE
    return PROMPT


def walk(ref="HEAD"):
    rows = []
    for line in _revisions(ref):
        sha, date, subject = line.split(" ", 2)
        directory = tempfile.mkdtemp(prefix="T-281-history.")
        try:
            _extract(sha, directory)
            emitted = _emitted(directory)
            declared = _declared(directory)
            today = _today(directory)
            refused = sorted(set(emitted) - declared)
            misdated = _misdated(emitted, today)
            rows.append(
                {
                    "commit": sha[:7],
                    "date": date,
                    "subject": subject,
                    "emitted": sorted(emitted),
                    "declared": sorted(declared),
                    "refused": refused,
                    "verdicts": {f: _classify(f, misdated) for f in refused},
                    "occurrences": sum(emitted.values()),
                    "misdatedOccurrences": sum(misdated.values()),
                    "misdatedBySplit": misdated,
                    "occurrencesTodaysPredicateFinds": sum(today.values()),
                }
            )
        finally:
            shutil.rmtree(directory, ignore_errors=True)
    return rows


def summarise(rows):
    refusals = sum(len(row["refused"]) for row in rows)
    genuine = sum(
        1 for row in rows for verdict in row["verdicts"].values() if verdict == GENUINE
    )
    families = sorted({f for row in rows for f in row["refused"]})
    genuine_families = sorted(
        {f for row in rows for f, v in row["verdicts"].items() if v == GENUINE}
    )
    return {
        "revisions": len(rows),
        "misdatedOccurrencesAtTheFirstRevision": rows[-1]["misdatedOccurrences"] if rows else 0,
        "occurrencesAtTheFirstRevision": rows[-1]["occurrences"] if rows else 0,
        "familyRevisionRefusals": refusals,
        "genuineRefusals": genuine,
        "promptRefusals": refusals - genuine,
        "falsePositives": 0,
        "distinctFamiliesRefused": families,
        "distinctFamiliesGenuine": genuine_families,
        "whyFalsePositivesAreZero": (
            "the rule reads a DECLARATION, so it cannot mistake a declared family for an "
            "undeclared one; every refusal is on a family that really names no discharge"
        ),
    }


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--json", action="store_true", help="print the walk as JSON")
    parser.add_argument("--ref", default="HEAD", help="the corpus state to walk (default HEAD)")
    args = parser.parse_args(argv)
    rows = walk(args.ref)
    if args.json:
        print(json.dumps({"revisions": rows, "summary": summarise(rows)}, indent=2))
        return 0
    for row in rows:
        print("%s  %s  %s" % (row["commit"], row["date"], row["subject"][:70]))
        print("        emitted  %s" % ", ".join(row["emitted"]))
        print("        declared %s" % (", ".join(row["declared"]) or "(none — every family "
                                       "answered by a default nobody wrote down)"))
        for family in row["refused"]:
            print("        REFUSED  %-10s %s" % (family, row["verdicts"][family]))
        print(
            "        %d occurrence(s); %d of them MISDATED — filed under this census's own "
            "subject where today's reading puts them elsewhere%s"
            % (
                row["occurrences"],
                row["misdatedOccurrences"],
                (" (" + ", ".join(
                    "%s %d" % kv for kv in sorted(row["misdatedBySplit"].items())
                ) + ")") if row["misdatedBySplit"] else "",
            )
        )
    summary = summarise(rows)
    print(
        "# %d revision(s) of %s; %d family-revision refusal(s), %d GENUINE, %d PROMPT, "
        "%d false positive(s)"
        % (
            summary["revisions"], CENSUS, summary["familyRevisionRefusals"],
            summary["genuineRefusals"], summary["promptRefusals"], summary["falsePositives"],
        )
    )
    print("# families refused: %s" % ", ".join(summary["distinctFamiliesRefused"]))
    print("# of those, GENUINE: %s" % (", ".join(summary["distinctFamiliesGenuine"]) or "none"))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
