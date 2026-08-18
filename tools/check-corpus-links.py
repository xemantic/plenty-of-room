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


def broken_links_in(text, directory, root=ROOT):
    """[(link, resolved path)] for every relative Markdown link in `text` that does not resolve.

    `directory` is the file's own directory relative to the repository root, and every link is
    resolved against it — the same slug is valid in one directory and not in another, which is how
    the live defects got in. Absolute URLs and anchors-only links are not this checker's business.
    """
    missing = []
    for match in _LINK.finditer(text):
        link = match.group(1)
        if "://" in link or link.startswith("/"):
            continue
        resolved = os.path.normpath(os.path.join(directory, link))
        if not os.path.exists(os.path.join(root, resolved)):
            missing.append((link, resolved))
    return missing


def tracked_markdown(root=ROOT):
    """The tracked Markdown under `gpd/`. Falls back to a walk outside a git checkout.

    `C-0083` records why the fallback matters: a verification SNAPSHOT has no `.git`, so a checker
    that only knows `git ls-files` silently checks nothing there.
    """
    try:
        listed = subprocess.run(
            ["git", "ls-files", "gpd/**/*.md", "gpd/*.md"],
            cwd=root, capture_output=True, text=True, check=True,
        ).stdout.split()
        if listed:
            return sorted(listed)
    except (OSError, subprocess.CalledProcessError):
        pass
    found = []
    for base, _dirs, files in os.walk(os.path.join(root, "gpd")):
        for name in files:
            if name.endswith(".md"):
                found.append(os.path.relpath(os.path.join(base, name), root))
    return sorted(found)


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
