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
# T-278 -- the SOLVER PROVENANCE of a study: every iterative tolerance reachable from its entry
# point, so a rounding precision can be argued rather than guessed.
#
#     tools/T-278-solver-provenance.py <study source>...   the closure and its tolerances
#     tools/T-278-solver-provenance.py --selftest
#
# WHY THIS EXISTS. `P-18` states the rule this tool mechanises, in its own conventions block:
# "PROVENANCE of an emitted number is the loosest solver tolerance on any path from a model input
# to it. Nine digits is defensible only where that is <= 1e-9." `P-18` then applies it to six
# rounding SITES by hand. `CH-0223` needs it applied to seven STUDIES, and asserts an answer for
# two of them -- that `T-1` and `T-1c` are downstream of a solved SCF height -- which is a claim
# about a call graph and is therefore checkable rather than arguable.
#
# WHAT IT IS NOT. It does not decide the precision. It ENUMERATES the candidates, because the one
# thing a per-study judgement must not do is miss a solver; which of the enumerated constants is a
# solver tolerance and which is a verdict threshold (`FLATNESS_TOLERANCE = 0.10` is the latter, and
# the corpus carries eleven of them) is a judgement per name, in `T-225`'s shape, and it is made in
# the claim rather than here.
#
# THE CLOSURE IS OVER DECLARATIONS, NOT FILES. `CLAUDE.md` records why: package `window` declares
# `ledger`, `array`, `reader` and `scalar` privately in several files at once, so a file-granular
# call graph reported one study as reading thirteen result files where it reads three. A name is
# resolved in its own file first, and a `private` top-level declaration is invisible outside it.
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = os.path.join(ROOT, "src", "main", "kotlin")

# A top-level or member declaration this closure can follow to.
DECLARATION = re.compile(
    r"^(?P<indent>[ \t]*)(?P<modifiers>(?:public |internal |private |protected |"
    r"open |abstract |sealed |data |value |enum |inline |override |suspend |operator |"
    r"external |annotation |companion |const |lateinit |tailrec |infix )*)"
    r"(?P<kind>fun|class|object|interface|val|var)\s+"
    r"(?:<[^>]*>\s*)?(?:[A-Za-z_][\w.]*\.)?(?P<name>[A-Za-z_]\w*)",
    re.MULTILINE,
)

IDENTIFIER = re.compile(r"[A-Za-z_]\w*")

# A floating-point literal small enough to be a convergence criterion rather than a coefficient.
# The upper bound is deliberately loose: `FLATNESS_TOLERANCE = 0.10` must be ENUMERATED and then
# judged, not filtered out by a threshold nobody argued for.
TOLERANCE_SITE = re.compile(
    r"(?P<name>[A-Za-z_]\w*)\s*(?::\s*Double\s*)?=\s*(?P<value>-?\d+(?:\.\d+)?[eE]-?\d+|0\.\d+)"
)

# The names whose value is a convergence criterion. A judgement per name, extended by census.
TOLERANCE_NAMES = re.compile(r"tolerance|convergence|epsilon|residual", re.IGNORECASE)

# The serialisation boundary, CUT rather than followed. `P-18` states the rule as "the loosest
# solver tolerance on any path from a MODEL INPUT to it", and these three files sit on the path
# from a number to a FILE: they round, they tag and they namespace, and they compute no physics.
# They are also what makes the same-package rule explode -- `structure/ResultEmission.kt` is a
# sibling of thirty studies and of `OrigamiGrillage`, so following it puts the whole lattice
# corpus in a polymer study's closure. Cutting here is the difference between 158 sources and a
# readable number, and it is a statement about the DIRECTION of the graph rather than a filter.
EMISSION_LAYER = frozenset(
    os.path.join(SOURCES, name)
    for name in (
        os.path.join("structure", "ResultEmission.kt"),
        os.path.join("structure", "ResultRounding.kt"),
        os.path.join("lattice", "LatticeTag.kt"),
    )
)


def blank_comments(text):
    """`text` with every comment replaced by spaces, so a KDoc cannot contribute an identifier.

    Length-preserving, because every offset reported downstream is an offset into the original.
    """
    out = list(text)
    i, n = 0, len(text)
    in_string = in_char = False
    while i < n:
        two = text[i:i + 2]
        if not in_string and not in_char and two == "//":
            while i < n and text[i] != "\n":
                out[i] = " "
                i += 1
            continue
        if not in_string and not in_char and two == "/*":
            depth = 0
            while i < n:
                if text[i:i + 2] == "/*":
                    depth += 1
                    out[i] = out[i + 1] = " "
                    i += 2
                    continue
                if text[i:i + 2] == "*/":
                    depth -= 1
                    out[i] = out[i + 1] = " "
                    i += 2
                    if depth == 0:
                        break
                    continue
                if text[i] != "\n":
                    out[i] = " "
                i += 1
            continue
        if not in_char and text[i] == '"':
            in_string = not in_string
        elif not in_string and text[i] == "'":
            in_char = not in_char
        elif text[i] == "\\" and (in_string or in_char):
            i += 2
            continue
        i += 1
    return "".join(out)


PACKAGE = re.compile(r"^package\s+([\w.]+)", re.MULTILINE)
IMPORT = re.compile(r"^import\s+([\w.]+)(?:\s+as\s+(\w+))?", re.MULTILINE)


def survey(root=SOURCES):
    """The tree as {file: (package, {imported FQN}, {declared name})} plus a package index.

    KOTLIN'S OWN VISIBILITY RULE, not a file-granular approximation. `CLAUDE.md` records what the
    approximation costs: package `window` declares `ledger`, `array`, `reader` and `scalar`
    privately in several files at once, so a file-granular graph reported one study as reading
    thirteen result files where it reads three. Measured here the same way -- following EVERY
    identifier to EVERY file declaring it puts 311 of the tree's 340 sources in
    `BrushStiffnessStudy`'s closure, because `main`, `report` and `output` are declared
    everywhere. A name is visible only if it is declared in the same FILE, in the same PACKAGE,
    or explicitly IMPORTED, and that cuts the same closure to a readable one.
    """
    files = {}
    by_package = {}
    by_fqn = {}
    for base, _, names in os.walk(root):
        for name in sorted(names):
            if not name.endswith(".kt"):
                continue
            path = os.path.join(base, name)
            text = blank_comments(open(path, encoding="utf-8").read())
            package = (PACKAGE.search(text) or [None, ""])[1] if PACKAGE.search(text) else ""
            imports = set()
            for match in IMPORT.finditer(text):
                imports.add(match.group(2) or match.group(1).rsplit(".", 1)[-1])
                imports.add(match.group(1))
            declared = set()
            for match in DECLARATION.finditer(text):
                declared.add(match.group("name"))
                by_fqn.setdefault(package + "." + match.group("name"), set()).add(path)
            files[path] = (package, imports, declared)
            by_package.setdefault(package, set()).add(path)
    return files, by_package, by_fqn


def closure(entry, files, by_package, by_fqn):
    """Every source file reachable from `entry` under Kotlin's own name resolution."""
    seen, frontier = {entry}, [entry]
    while frontier:
        path = frontier.pop()
        package, imports, _ = files[path]
        text = blank_comments(open(path, encoding="utf-8").read())
        used = set(IDENTIFIER.findall(text))
        targets = set()
        # Same package: every sibling file declaring a name this file uses.
        #
        # A `*Study.kt` sibling is NOT followed this way. A study is an entry point, and the
        # same-package rule is what makes this closure explode through one: `structure/ResultEmission.kt`
        # is a sibling of thirty studies, so following it by shared identifier put 289 of the tree's 311
        # sources in `T-1`'s closure. Measured, exactly EIGHT non-study files reference a declaration
        # that lives in a study file (`thermalVoltage`, `bjerrumLength`, `waterViscosity`,
        # `thermalBlobKuhnSegments`), and all eight do it by an EXPLICIT import, which the rule below
        # still follows. `unimported_study_siblings` reports what this rule could be losing, so the
        # under-inclusion is a printed number rather than a silence.
        for sibling in by_package.get(package, ()):
            if sibling == path or sibling.endswith("Study.kt") or sibling in EMISSION_LAYER:
                continue
            if files[sibling][2] & used:
                targets.add(sibling)
        # Explicit imports, resolved as fully qualified declarations.
        for imported in imports:
            targets |= {t for t in by_fqn.get(imported, set()) if t not in EMISSION_LAYER}
        for target in targets:
            if target not in seen:
                seen.add(target)
                frontier.append(target)
    return seen


def unimported_study_siblings(files, by_package):
    """[(file, study sibling, {shared name})] the same-package rule declines to follow.

    The closure follows a `*Study.kt` only through an explicit import. This is what that costs,
    printed rather than assumed: a non-study file naming a declaration that lives in a study file
    of its own package without importing it.
    """
    losses = []
    for path, (package, imports, _) in sorted(files.items()):
        if path.endswith("Study.kt"):
            continue
        used = set(IDENTIFIER.findall(blank_comments(open(path, encoding="utf-8").read())))
        for sibling in sorted(by_package.get(package, ())):
            if not sibling.endswith("Study.kt"):
                continue
            shared = files[sibling][2] & used
            # An explicitly imported name is followed anyway.
            shared = {name for name in shared if name not in imports}
            if shared:
                losses.append((path, sibling, shared))
    return losses


def tolerances(paths):
    """[(file, name, value)] for every named numeric criterion in `paths`."""
    found = []
    for path in sorted(paths):
        text = blank_comments(open(path, encoding="utf-8").read())
        for match in TOLERANCE_SITE.finditer(text):
            name = match.group("name")
            if not TOLERANCE_NAMES.search(name):
                continue
            found.append((os.path.relpath(path, ROOT), name, float(match.group("value"))))
    return found


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    check("a line comment contributes nothing",
          blank_comments("val a = 1 // SelfConsistentFieldLayer\n").strip(), "val a = 1")
    check("a KDoc contributes nothing",
          blank_comments("/** SelfConsistentFieldLayer */\nval a = 1").strip().splitlines()[-1],
          "val a = 1")
    check("blanking preserves length",
          len(blank_comments("/* xx */val a = 1")), len("/* xx */val a = 1"))
    check("a declaration is found",
          [m.group("name") for m in DECLARATION.finditer("fun heightUnderLoad(")],
          ["heightUnderLoad"])
    check("a private declaration is recognised",
          [("private" in m.group("modifiers"), m.group("name"))
           for m in DECLARATION.finditer("private fun solveLambda(")],
          [(True, "solveLambda")])
    check("a class is found",
          [m.group("name") for m in DECLARATION.finditer("internal class AlexanderBoxLayer(")],
          ["AlexanderBoxLayer"])
    # A tolerance is recognised by NAME and its value is read, whatever its magnitude -- a
    # verdict threshold must be enumerated and then judged, never filtered out silently.
    check("a scientific literal is read",
          tolerances_in("private const val CONVERGENCE = 1e-15"), [("CONVERGENCE", 1e-15)])
    check("a decimal literal is read",
          tolerances_in("val FLATNESS_TOLERANCE = 0.10"), [("FLATNESS_TOLERANCE", 0.1)])
    check("a default parameter is read",
          tolerances_in("    tolerance: Double = 1e-15,"), [("tolerance", 1e-15)])
    check("a name that is not a criterion is not read",
          tolerances_in("private const val TILE_EDGE = 40.0"), [])
    check("a commented tolerance is not read",
          tolerances_in("// private const val CONVERGENCE = 1e-3"), [])
    check("a package is read",
          PACKAGE.search("package com.xemantic.nano.plentyofroom.brush").group(1),
          "com.xemantic.nano.plentyofroom.brush")
    check("an import contributes both its tail and its fully qualified name",
          sorted({(IMPORT.search("import a.b.C").group(2)
                   or IMPORT.search("import a.b.C").group(1).rsplit(".", 1)[-1]),
                  IMPORT.search("import a.b.C").group(1)}),
          ["C", "a.b.C"])
    check("an aliased import contributes its alias",
          IMPORT.search("import a.b.C as D").group(2), "D")
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def tolerances_in(text):
    """The criteria in a snippet, for the self-tests."""
    text = blank_comments(text)
    return [
        (m.group("name"), float(m.group("value")))
        for m in TOLERANCE_SITE.finditer(text)
        if TOLERANCE_NAMES.search(m.group("name"))
    ]


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    entries = [a for a in argv if not a.startswith("-")]
    if not entries:
        print("usage: tools/T-278-solver-provenance.py <study source>... | --selftest",
              file=sys.stderr)
        return 2
    files, by_package, by_fqn = survey()
    if "--losses" in argv:
        losses = unimported_study_siblings(files, by_package)
        for path, sibling, shared in losses:
            print("SAME-PACKAGE-STUDY %s -> %s (%s)"
                  % (os.path.relpath(path, ROOT), os.path.relpath(sibling, ROOT),
                     ", ".join(sorted(shared))))
        print("%d same-package study reference(s) the closure declines to follow" % len(losses))
        return 0
    for entry in entries:
        path = entry if os.path.isabs(entry) else os.path.join(ROOT, entry)
        if not os.path.exists(path):
            print("MISSING %s" % entry, file=sys.stderr)
            return 2
        reached = closure(path, files, by_package, by_fqn)
        found = tolerances(reached)
        print("%s -- %d source(s) in the closure, %d named criteria"
              % (os.path.relpath(path, ROOT), len(reached), len(found)))
        for source, name, value in sorted(found, key=lambda row: (-row[2], row[0], row[1])):
            print("  %-12g %-34s %s" % (value, name, source))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
