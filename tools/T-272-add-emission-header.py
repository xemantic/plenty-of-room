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
# T-272 -- put `structure/ResultEmission.kt`'s emission header on a study's own write.
#
#     tools/T-272-add-emission-header.py <source> <LatticeTag> <regime expression>
#     tools/T-272-add-emission-header.py --selftest
#
# WHY A TOOL AND NOT AN EDIT. The header goes on 127 emitting studies, whose write blocks are
# NOT uniform -- six rounding entry points, `roundedForResult` / `roundedForActuatorResult` /
# `roundedForCouplingResult` / `roundedForWindowResult` / `roundedForScfResult` /
# `roundedForFluctuationResult`, some with a `digitsByKey`, some cast to `JsonObject` first. What
# IS uniform is the shape: exactly one `writeText(` on the study's own output, and exactly one
# rounding call inside it. So the transformation is "append `.withEmissionHeader(...)` after the
# rounding call's matching close paren", which needs a paren walk rather than a regex -- the
# argument lists contain parentheses and `String.format` calls.
#
# The lattice tag and the regime are ARGUMENTS, never derived: `lattice/LatticeTag.kt` records the
# measurement that a derivation is 23 % noise, and a regime is by definition the tuple the study
# was handed. This tool is the mechanical half; the judgement is the caller's.
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

ROUNDING_CALL = re.compile(r"\.roundedFor(?:[A-Za-z]*)Result\(")

LATTICE_IMPORT = "import com.xemantic.nano.plentyofroom.lattice.LatticeTag"
HEADER_IMPORT = "import com.xemantic.nano.plentyofroom.structure.withEmissionHeader"
REGIME_IMPORT = "import com.xemantic.nano.plentyofroom.environment.Regime"


def blank_comments(text):
    """`text` with every comment replaced by spaces, LENGTH-PRESERVING so indices still line up.

    A comment is not a lexical curiosity here: an apostrophe in `// the tile's own row` opens a
    character literal for any naive scanner, which then swallows every parenthesis until the next
    apostrophe in the file. Two studies refused with `unbalanced parentheses` for exactly that.
    """
    out = []
    i = 0
    n = len(text)
    while i < n:
        if text.startswith("//", i):
            end = text.find("\n", i)
            end = n if end < 0 else end
            out.append(" " * (end - i))
            i = end
        elif text.startswith("/*", i):
            end = text.find("*/", i + 2)
            end = n if end < 0 else end + 2
            out.append("".join(c if c == "\n" else " " for c in text[i:end]))
            i = end
        elif text[i] == '"':
            if text.startswith('"""', i):
                end = text.find('"""', i + 3)
                end = n if end < 0 else end + 3
            else:
                end = i + 1
                while end < n and text[end] != '"':
                    end += 2 if text[end] == "\\" else 1
                end = min(end + 1, n)
            out.append(text[i:end])
            i = end
        else:
            out.append(text[i])
            i += 1
    blanked = "".join(out)
    assert len(blanked) == len(text)
    return blanked


def matching_paren(text, open_index):
    """The index of the `)` closing the `(` at `open_index`, skipping string literals."""
    depth = 0
    i = open_index
    n = len(text)
    while i < n:
        c = text[i]
        if c == '"':
            # A raw string is `"""`; a plain one honours backslash escapes.
            if text.startswith('"""', i):
                end = text.find('"""', i + 3)
                i = n if end < 0 else end + 3
                continue
            i += 1
            while i < n and text[i] != '"':
                i += 2 if text[i] == "\\" else 1
            i += 1
            continue
        if c == "'":
            i += 2 if text[i + 1 : i + 2] == "\\" else 1
            while i < n and text[i] != "'":
                i += 1
            i += 1
            continue
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError("unbalanced parentheses from index %d" % open_index)


def write_block(text):
    """The `(start, end)` span of the argument list of the study's own `writeText(`.

    Located on the comment-blanked text, which is index-identical to `text`.
    """
    blanked = blank_comments(text)
    calls = [m for m in re.finditer(r"\.writeText\(", blanked)]
    if len(calls) != 1:
        raise ValueError("expected exactly one `.writeText(`, found %d" % len(calls))
    open_index = calls[0].end() - 1
    return open_index, matching_paren(blanked, open_index)


def add_header(text, lattice, regime):
    """`text` with `.withEmissionHeader(LatticeTag.X, <regime>)` after its rounding call."""
    start, end = write_block(text)
    inside = text[start:end]
    rounding = ROUNDING_CALL.search(blank_comments(inside))
    if not rounding:
        raise ValueError("no rounding call inside the write block")
    close = matching_paren(blank_comments(inside), rounding.end() - 1)
    suffix = ".withEmissionHeader(LatticeTag.%s, %s)" % (lattice, regime)
    patched = inside[: close + 1] + suffix + inside[close + 1 :]
    return text[:start] + patched + text[end:]


def add_imports(text, needs_regime):
    """`text` with the header's imports inserted IN PLACE, never re-sorting what is there.

    Re-sorting the whole block is what the first draft did, and over 127 studies it moves
    `java.io.File` and `kotlin.math.abs` above `kotlinx.*` in every one of them -- several
    hundred lines of diff that no reader can distinguish from the change under test. A sweep
    whose diff cannot be read is a sweep whose movement cannot be classified by kind, which is
    this task's own `A2`.
    """
    wanted = [w for w in ([LATTICE_IMPORT, HEADER_IMPORT] +
                          ([REGIME_IMPORT] if needs_regime else [])) if w not in text]
    lines = text.split("\n")
    for line in sorted(wanted):
        indices = [i for i, l in enumerate(lines) if l.startswith("import ")]
        before = [i for i in indices if lines[i] < line]
        at = (max(before) + 1) if before else min(indices)
        lines.insert(at, line)
    return "\n".join(lines)


def patch(path, lattice, regime):
    text = open(path).read()
    if ".withEmissionHeader(" in text:
        return False
    text = add_header(text, lattice, regime)
    text = add_imports(text, regime != "null" and "Regime." in regime)
    open(path, "w").write(text)
    return True


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    check("paren simple", matching_paren("f(a, b)", 1), 6)
    check("paren nested", matching_paren("f(g(x), h(y))", 1), 12)
    check("paren string", matching_paren('f("a)b")', 1), 7)
    check("paren raw string", matching_paren('f("""a)b""")', 1), 11)
    check("comments are blanked length-preservingly",
          blank_comments("a // the tile's own\nb"), "a                  \nb")
    check("a block comment keeps its newlines",
          blank_comments("a /* x\ny */ b"), "a     \n     b")
    check("a string survives blanking", blank_comments('f("// not a comment")'),
          'f("// not a comment")')
    check("an apostrophe in a comment cannot swallow a paren",
          matching_paren(blank_comments("f(x) // the tile's own"), 1), 3)

    body = (
        "package p\n\nimport a.B\nimport java.io.File\n\nfun main() {\n"
        '    output.writeText(json.encodeToString(e.roundedForResult(digits = 6)) + "\\n")\n}\n'
    )
    out = add_header(body, "SQUARE", "null")
    check(
        "header appended after the rounding call",
        "roundedForResult(digits = 6).withEmissionHeader(LatticeTag.SQUARE, null)" in out,
        True,
    )
    # The `+ "\n"` outside the rounding call must survive, i.e. the append is INSIDE writeText.
    check("newline survives", out.count('+ "\\n"'), 1)

    imported = add_imports(out, False)
    check("lattice import added", LATTICE_IMPORT in imported, True)
    check("header import added", HEADER_IMPORT in imported, True)
    check("regime import withheld", REGIME_IMPORT in imported, False)
    # The existing block is NOT re-sorted -- an unsorted neighbour must stay where it is.
    unsorted = "package p\n\nimport zz.Z\nimport aa.A\n\nfun main() {\n" + \
        '    o.writeText(json.encodeToString(e.roundedForResult()) + "\\n")\n}\n'
    kept = add_imports(unsorted, False)
    check("existing order preserved", kept.index("import zz.Z") < kept.index("import aa.A"), True)
    check("new import lands among the imports", "\nimport com.xemantic" in kept, True)

    # Two write calls is a shape this tool must refuse rather than guess at.
    try:
        write_block(body.replace("fun main", "fun x() { f.writeText(y) }\nfun main"))
        failures.append("two writeText calls: expected a refusal")
    except ValueError:
        pass

    # A study already carrying a header is left alone, so the tool is idempotent.
    check("idempotent", ".withEmissionHeader(" in out, True)

    for failure in failures:
        print("FAIL " + failure)
    print("%d self-tests failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


def main(argv):
    if len(argv) == 2 and argv[1] == "--selftest":
        return _selftest()
    if len(argv) != 4:
        print(__doc__ or "usage: T-272-add-emission-header.py <source> <TAG> <regime>", file=sys.stderr)
        return 2
    changed = patch(argv[1], argv[2], argv[3])
    print(("patched " if changed else "already carries a header: ") + argv[1])
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
