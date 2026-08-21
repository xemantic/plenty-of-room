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
# T-203 / C-0122 -- every relative Markdown link in gpd/ resolves to a file that exists.
#
#     tools/check-corpus-links.py            checks the corpus, exit 1 on any defect
#     tools/check-corpus-links.py --selftest  runs the self-tests
#
# WHY THIS EXISTS. A claim cites its neighbours by relative link, and a claim FILENAME carries a
# slug that the writer reconstructs from memory -- `C-0006-tile-flatness.md` for what is actually
# `C-0006-tile-load-distribution-and-flatness.md`, `C-0034-far-anchorage.md` for
# `C-0034-guided-arm-anchorage.md`. Nothing in the tree checked it, and a sweep found **15** broken
# references accumulated across many iterations, in claims, challenges and task files alike.
#
# It is the same class as `C-0083`'s Markdown tables: invisible to every numeric, status and
# self-consistency check, silent at the point of writing, and cheap to mechanise. `CLAUDE.md`
# already records the general form -- *"a defect's LOCATION is a number like any other, and two
# records can agree with each other and both be wrong about it ... grep the string out of the tree
# before accepting a filename from a claim."* This is that rule, made a gate.
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# ANY relative Markdown link. Resolved against the FILE'S OWN DIRECTORY with `normpath`, which is
# the only correct rule and the one the first version of this checker got wrong: it special-cased
# `../../` as "the repository root", which is true for `gpd/claims/x.md` and false for
# `gpd/data/T-161-sources/MANIFEST.md`, where the same prefix means `gpd/`. The checker reported 15
# false positives on its first real run, all of them manifests two levels deep. One resolver, no
# depth assumptions.
_LINK = re.compile(r"\]\(([^)\s]+?\.md)(?:#[^)]*)?\)")

# A link inside CODE is not a link, it is a description of one -- and the one place in this
# repository that describes link SHAPES is `CLAUDE.md`'s own entry about this checker's first
# blind spot, which quotes `](C-0006-....md)` verbatim. Scanning the root documents without
# stripping code first would therefore report the sentence that records the defect as a defect.
# Fences first, then inline spans; both are replaced by same-length blanks so that nothing below
# moves and a reported position stays true.
_FENCE = re.compile(r"```.*?```", re.DOTALL)
_CODE_SPAN = re.compile(r"`[^`\n]*`")


def _without_code(text):
    """`text` with fenced blocks and inline code spans blanked, length-preserving."""
    def blank(match):
        return "".join(" " if character != "\n" else "\n" for character in match.group(0))
    return _CODE_SPAN.sub(blank, _FENCE.sub(blank, text))


def broken_links_in(text, directory, root=ROOT):
    """[(link, resolved path)] for every relative Markdown link in `text` that does not resolve.

    `directory` is the file's own directory relative to the repository root, and every link is
    resolved against it — the same slug is valid in one directory and not in another, which is how
    the live defects got in. Absolute URLs and anchors-only links are not this checker's business.
    """
    missing = []
    for match in _LINK.finditer(_without_code(text)):
        link = match.group(1)
        if "://" in link or link.startswith("/"):
            continue
        resolved = os.path.normpath(os.path.join(directory, link))
        if not os.path.exists(os.path.join(root, resolved)):
            missing.append((link, resolved))
    return missing


# The two OUTWARD-FACING documents. `T-184` found `DECISIONS-FOR-NDI.md` -- the one NDI actually
# reads -- checked by nothing at all, this checker included: it scanned `gpd/` and stopped there,
# so a mistyped claim slug in either deliverable was invisible. They carry more claim links than
# most claims do, and a broken one in them is read by the customer rather than by an agent.
def root_documents(root=ROOT):
    """Every Markdown file at the repository root, DERIVED rather than listed.

    It was a hand-written list of two, then three, and `C-0160` found the fourth and fifth —
    `ARCHITECTURE.md` and `CLAUDE.md`, both carrying relative links and neither scanned. A list
    that has to be extended by hand every time somebody adds a root document is the same decay
    this checker exists to stop, one level up: *a checker's DEFAULT is part of its logic*. There
    is no root Markdown file that should be exempt from having working links, so the set is the
    directory listing.
    """
    return sorted(name for name in os.listdir(root)
                  if name.endswith(".md") and os.path.isfile(os.path.join(root, name)))
# `TOOLING-NOVELTY.md` was added by the repository owner in iteration 37 and was checked by
# NOTHING, exactly as `T-184`/`C-0124` found for `DECISIONS-FOR-NDI.md`: *a checker's DEFAULT
# is part of its logic, and the document nobody checks is the one the customer reads*. It is a
# third outward-facing deliverable in the same house style (SURVIVES / CORRECTED / WITHDRAWN,
# a read-flag per source), so it belongs in the same set. It was clean when added — the point
# is to keep it so, not to repair it.


def tracked_markdown(root=ROOT):
    """The Markdown under `gpd/` — tracked AND untracked — plus the outward-facing root documents.

    `C-0083` records why the fallback matters: a verification SNAPSHOT has no `.git`, so a checker
    that only knows `git ls-files` silently checks nothing there.
    """
    extra = root_documents(root)
    try:
        listed = subprocess.run(
            ["git", "ls-files", "gpd/**/*.md", "gpd/*.md"],
            cwd=root, capture_output=True, text=True, check=True,
        ).stdout.split()
        # `git ls-files` lists TRACKED files only, so a claim an agent has just written and not
        # yet committed is invisible to it. `C-0127` found exactly that: two broken links in its
        # own `Consumes` row passed a run reporting *"0 broken link(s) in 376 file(s)"*, and were
        # caught only by `verify.sh`, which runs in a `.git`-less snapshot where the walk below
        # sees everything. **The blind instrument was the one an agent uses on its OWN work
        # mid-iteration** — the moment the check is worth most. Untracked Markdown is added here so
        # a checkout run and the gate see the same set.
        untracked = subprocess.run(
            ["git", "ls-files", "--others", "--exclude-standard", "gpd/**/*.md", "gpd/*.md"],
            cwd=root, capture_output=True, text=True, check=True,
        ).stdout.split()
        if listed or untracked:
            return sorted(set(listed) | set(untracked) | set(extra))
    except (OSError, subprocess.CalledProcessError):
        pass
    found = list(extra)
    for base, _dirs, files in os.walk(os.path.join(root, "gpd")):
        for name in files:
            if name.endswith(".md"):
                found.append(os.path.relpath(os.path.join(base, name), root))
    return sorted(set(found))


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append(name)
            print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
        else:
            print("ok   {}".format(name))

    # A sibling link resolves against the file's OWN directory -- the distinction the live defects
    # turned on, since the same slug can be valid in claims/ and absent from challenges/.
    check("a sibling link that exists is not reported",
          broken_links_in("see [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)",
                          "gpd/claims"), [])
    check("a sibling link that does not exist is reported",
          [l for l, _ in broken_links_in("[`C-0006`](C-0006-tile-flatness.md)", "gpd/claims")],
          ["C-0006-tile-flatness.md"])
    check("the SAME slug is judged against the directory it is written in",
          [l for l, _ in broken_links_in(
              "[`C-0006`](C-0006-tile-load-distribution-and-flatness.md)", "gpd/challenges")],
          ["C-0006-tile-load-distribution-and-flatness.md"])
    check("a sideways link that exists is not reported",
          broken_links_in("[`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md)",
                          "gpd/challenges"), [])
    check("a sideways link that does not exist is reported",
          [l for l, _ in broken_links_in("[`x`](../claims/C-9999-nope.md)", "gpd/challenges")],
          ["../claims/C-9999-nope.md"])
    check("a rootward link that exists is not reported",
          broken_links_in("[`TASKS.md`](../../TASKS.md)", "gpd/claims"), [])
    check("a rootward link that does not exist is reported",
          [l for l, _ in broken_links_in("[`x`](../../NOPE.md)", "gpd/claims")], ["../../NOPE.md"])
    # An anchor must not defeat the resolution.
    check("a link with an anchor still resolves",
          broken_links_in("[`x`](../../TASKS.md#start-here)", "gpd/claims"), [])
    # Absolute and external links are not this checker's business.
    check("an http link is ignored",
          broken_links_in("[`x`](https://example.org/C-0001-nope.md)", "gpd/claims"), [])
    check("prose naming a claim without linking it is ignored",
          broken_links_in("see C-0006-tile-flatness.md for the detail", "gpd/claims"), [])
    check("the corpus listing finds files", len(tracked_markdown()) > 0, True)
    # `T-184`: the two outward-facing documents are IN the listing. They were not, and a broken
    # claim slug in the file NDI reads is worse than one in a claim, not better.
    check("the outward-facing set is DERIVED, and it carries the three deliverables",
          [name for name in ["ANSWERS.md", "DECISIONS-FOR-NDI.md", "TOOLING-NOVELTY.md"]
           if name in root_documents()],
          ["ANSWERS.md", "DECISIONS-FOR-NDI.md", "TOOLING-NOVELTY.md"])
    # `C-0160` found these two scanned by nothing; a derived set cannot lose them again.
    check("and the two documents a hand-written list had missed",
          [name for name in ["ARCHITECTURE.md", "CLAUDE.md"] if name in root_documents()],
          ["ARCHITECTURE.md", "CLAUDE.md"])
    # A link inside code is a DESCRIPTION of a link. `CLAUDE.md` quotes one verbatim, in the very
    # entry that records this checker's first blind spot.
    check("a link inside an inline code span is not a link",
          broken_links_in("a check that matched `](C-0006-nope.md)` and not ...", ""), [])
    check("a link inside a fenced block is not a link",
          broken_links_in("```\n[`x`](gpd/claims/C-0001-nope.md)\n```", ""), [])
    check("a real link whose TEXT is a code span is still a link",
          [l for l, _ in broken_links_in("[`C-0006`](gpd/claims/C-0006-nope.md)", "")],
          ["gpd/claims/C-0006-nope.md"])
    # On a SYNTHETIC root, so the check does not depend on this checkout: `tools/verify.sh` runs
    # these in a snapshot, and a self-test that reads the real tree is not a fixture test.
    import tempfile
    with tempfile.TemporaryDirectory() as fake:
        os.makedirs(os.path.join(fake, "gpd", "claims"))
        for name in ["ANSWERS.md", "DECISIONS-FOR-NDI.md", "TOOLING-NOVELTY.md",
                     os.path.join("gpd", "claims", "C-0001-x.md")]:
            open(os.path.join(fake, name), "w").close()
        listed = tracked_markdown(root=fake)
        check("a root document is in the listing", "ANSWERS.md" in listed, True)
        check("and so is the decision file", "DECISIONS-FOR-NDI.md" in listed, True)
        check("the corpus is still listed beside them",
              os.path.join("gpd", "claims", "C-0001-x.md") in listed, True)
        check("the listing has no duplicates", len(listed), len(set(listed)))
        check("a root document that does not exist is not listed",
              tracked_markdown(root=os.path.join(fake, "gpd")), [])
    # A root document's links resolve against the ROOT, not against `gpd/` -- the same one-resolver
    # rule the manifest defects taught, at the other end of the tree.
    check("a root-relative link from a root document resolves",
          broken_links_in("[`C-0006`](gpd/claims/C-0006-tile-load-distribution-and-flatness.md)", ""), [])
    check("and a mistyped one from a root document is reported",
          [l for l, _ in broken_links_in("[`C-0006`](gpd/claims/C-0006-tile-flatness.md)", "")],
          ["gpd/claims/C-0006-tile-flatness.md"])

    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    defects = 0
    files = tracked_markdown()
    for relative in files:
        path = os.path.join(ROOT, relative)
        try:
            with open(path, encoding="utf-8") as handle:
                text = handle.read()
        except OSError:
            continue
        for link, resolved in broken_links_in(text, os.path.dirname(relative)):
            print("{}\tBROKEN-LINK\t{}\t-> {}".format(relative, link, resolved))
            defects += 1
    sys.stdout.flush()
    print("# {} broken link(s) in {} file(s)".format(defects, len(files)), file=sys.stderr)
    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
