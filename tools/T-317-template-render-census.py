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
# T-317 / C-0213 -- IS THE CLAIM TEMPLATE'S LINK SET ALREADY CHECKED, IN ITS RENDER?
#
#     tools/T-317-template-render-census.py            the census, over the whole history
#     tools/T-317-template-render-census.py --selftest  runs the self-tests
#
# WHY THIS EXISTS. `C-0213` §4 states honestly that widening the link gate's file set catches
# nothing that has ever existed, and the reason is that `tools/C-0156-claim-template.md`'s links
# are ALREADY resolved -- in scope, at the right directory -- inside the claim
# `tools/T-250-emit-result.py` renders it into. That is a claim about the corpus's history and
# `CH-0266`'s whole lesson is that such a number must have an artifact behind it: *there was no
# method statement, no scanner and no artifact to re-run, which is why the only way to check it
# was to measure the whole thing again*. So the measurement is a retained tool, not a paragraph.
#
# Measured at `646b29e` plus this iteration's edits: **130** commits carry both, the link multiset
# is IDENTICAL at **130 of 130**, and there is **no** commit at which the template exists and its
# render does not. So the residue the widened gate closes is the window between a template edit
# and its render, and that window has never been open at a commit.
import importlib.util
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

#: The one template/render pair this corpus has. NOT derived: which file is rendered into which
#: is a fact about `tools/T-250-emit-result.py`'s two module constants, and reading a Python
#: source to find it would be a static call graph over files, which `CLAUDE.md` refuses.
PAIRS = (("tools/C-0156-claim-template.md",
          "gpd/claims/C-0156-prose-interpolation-sweep.md"),)

_FLAGS = ("--selftest",)


def _links():
    """`relative_links_in`, borrowed from the checker so the two cannot disagree about a link."""
    path = os.path.join(ROOT, "tools", "check-corpus-links.py")
    spec = importlib.util.spec_from_file_location("_check_corpus_links", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module.relative_links_in


def compare(template_text, render_text, links):
    """`(template links, render links, identical as a MULTISET)`.

    A multiset and not a set: a claim cites the same neighbour in its `Task` row and again in
    `Consumes`, and a render that dropped one of the two would still match as a set.
    """
    left = sorted(links(template_text))
    right = sorted(links(render_text))
    return len(left), len(right), left == right


def history(root=ROOT, revision="HEAD", pairs=PAIRS):
    """`(both, identical, template only)` over every commit reachable from `revision`."""
    links = _links()

    def git(*arguments):
        return subprocess.run(["git"] + list(arguments), cwd=root,
                              capture_output=True, text=True, check=True).stdout

    blobs = {}
    both = identical = template_only = 0
    for commit in git("rev-list", "--reverse", revision).split():
        entries = {}
        for line in git("ls-tree", "-r", commit).splitlines():
            meta, path = line.split("\t", 1)
            _mode, kind, sha = meta.split()
            if kind == "blob":
                entries[path] = sha
        for template, render in pairs:
            if template not in entries:
                continue
            if render not in entries:
                template_only += 1
                continue
            both += 1
            texts = []
            for path in (template, render):
                sha = entries[path]
                if sha not in blobs:
                    blobs[sha] = subprocess.run(["git", "cat-file", "blob", sha], cwd=root,
                                                capture_output=True, check=True
                                                ).stdout.decode("utf-8", "replace")
                texts.append(blobs[sha])
            if compare(texts[0], texts[1], links)[2]:
                identical += 1
    return both, identical, template_only


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append(name)
            print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
        else:
            print("ok   {}".format(name))

    links = _links()
    check("a render carrying the template's links is identical",
          compare("[`a`](x.md) [`b`](../y.py)", "[`a`](x.md) and [`b`](../y.py)", links),
          (2, 2, True))
    check("a render that has LOST a link is not",
          compare("[`a`](x.md) [`b`](../y.py)", "[`a`](x.md)", links), (2, 1, False))
    check("a render that has GAINED one is not either",
          compare("[`a`](x.md)", "[`a`](x.md) [`b`](../y.py)", links), (1, 2, False))
    # A MULTISET and not a set: a claim cites the same neighbour twice and a render that dropped
    # one of the two occurrences would pass a set comparison.
    check("a duplicated citation is compared as a multiset, not as a set",
          compare("[`a`](x.md) [`a`](x.md)", "[`a`](x.md)", links), (2, 1, False))
    check("and a substituted placeholder does not change the link set",
          compare("{{n}} see [`a`](x.md)", "42 see [`a`](x.md)", links), (1, 1, True))
    # The pair is a declaration and the file it names must exist, or the census measures nothing.
    check("both members of every declared pair exist in this checkout",
          [pair for pair in PAIRS
           if not all(os.path.exists(os.path.join(ROOT, path)) for path in pair)], [])
    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv):
    unrecognised = [argument for argument in argv if argument not in _FLAGS]
    if unrecognised:
        print("unrecognised argument(s): {}\nusage: T-317-template-render-census.py [{}]".format(
            " ".join(unrecognised), " | ".join(_FLAGS)), file=sys.stderr)
        return 2
    if "--selftest" in argv:
        return _selftest()
    both, identical, template_only = history()
    print("# {} commit(s) carry both the template and its render; the link multiset is identical "
          "at {} of them; {} commit(s) carry the template ALONE".format(
              both, identical, template_only), file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
