#!/usr/bin/env python3
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
"""Where this repository's Kotlin sources live, now that there are two modules.

`origami-engine` holds the library and the root project holds the corpus (`LIBRARY.md`), so a
tool that resolves a Kotlin declaration by name, or censuses "every source", has to look in
BOTH -- and a tool that looks in one of them reports a clean run over a corpus smaller than the
one it was written for, which is the failure direction `CLAUDE.md` warns about under *a gate
that cannot come clean is not a gate*: here the gate comes clean by not looking.

Import this rather than assembling `src/main/kotlin` by hand.
"""

import os

#: main-source roots, corpus first, each relative to the repository root
MAIN_ROOTS = (
    os.path.join("src", "main", "kotlin"),
    os.path.join("origami-engine", "src", "main", "kotlin"),
)

#: test-source roots, corpus first (the module also has `src/testFixtures/kotlin`)
TEST_ROOTS = (
    os.path.join("src", "test", "kotlin"),
    os.path.join("origami-engine", "src", "test", "kotlin"),
    os.path.join("origami-engine", "src", "testFixtures", "kotlin"),
)


def main_roots(root):
    """The absolute main-source roots under repository [root] that exist."""
    return [os.path.join(root, r) for r in MAIN_ROOTS if os.path.isdir(os.path.join(root, r))]


def test_roots(root):
    """The absolute test-source roots under repository [root] that exist."""
    return [os.path.join(root, r) for r in TEST_ROOTS if os.path.isdir(os.path.join(root, r))]


def resolve_main(root, relative):
    """The first existing `<main root>/<relative>` under [root], or None.

    `relative` is a path below the source root, e.g. `structure/Gen1Tile.kt`.  Returning None
    rather than a non-existent path is deliberate: a caller that opens it gets its own error at
    its own call site, and a caller that checks gets to say which module it expected.
    """
    for base in main_roots(root):
        candidate = os.path.join(base, relative)
        if os.path.isfile(candidate):
            return candidate
    return None


def walk_main(root):
    """Every `.kt` file under every main-source root of [root], as absolute paths."""
    for base in main_roots(root):
        for dirpath, _, names in os.walk(base):
            for name in sorted(names):
                if name.endswith(".kt"):
                    yield os.path.join(dirpath, name)


def _selftest():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    failures = []

    def check(what, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, was %r" % (what, expected, actual))

    check("both main roots exist", len(main_roots(root)), 2)
    check("the corpus root is first", main_roots(root)[0].endswith(os.path.join("src", "main", "kotlin")), True)
    # a declaration in each module resolves, and one in neither does not
    check("a corpus source resolves",
          resolve_main(root, os.path.join("structure", "ResultInputs.kt")) is not None, True)
    check("a library source resolves",
          resolve_main(root, os.path.join("lattice", "LatticeTag.kt")) is not None, True)
    check("a missing source is None",
          resolve_main(root, os.path.join("structure", "NoSuchFile.kt")), None)
    # the walk is the union and it is not the corpus alone
    every = list(walk_main(root))
    corpus_only = [p for p in every if os.path.join("origami-engine", "src") not in p]
    check("the walk reaches the library", len(every) > len(corpus_only), True)
    check("the walk has no duplicates", len(every), len(set(every)))
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    print("# %d self-test(s)" % 7)
    return 1 if failures else 0


if __name__ == "__main__":
    import sys
    if len(sys.argv) == 2 and sys.argv[1] == "--self-test":
        sys.exit(_selftest())
    print("usage: kotlin_sources.py --self-test")
    print("  (a library module: import it, do not run it)")
    sys.exit(2)
