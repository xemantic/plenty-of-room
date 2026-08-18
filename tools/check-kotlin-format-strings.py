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
"""Counts `%` conversions against arguments in every Kotlin `String.format` call.

`CLAUDE.md` records this failure family five times and it has cost this repository two
long runs:

- **`+` binds tighter than `.format()`**, so `"a %s " + "b %f".format(x, y)` formats only the
  *second* literal.  It can throw hundreds of characters downstream
  (`IllegalFormatConversionException: d != java.lang.String`), or **not throw at all** and emit
  a grammatical sentence with the wrong numbers in it.
- A **last-line** prose defect is the worst kind: `T-176` lost 49 minutes of completed
  computation, `T-133` seventeen.

`CLAUDE.md`'s own remedy is *"check the calls mechanically — count `%` conversions over the
**whole parenthesised concatenation** against the top-level commas of the `format(...)`
argument list, stripping `%%` first"*.  That is exactly this script, and nothing was running it.

The receiver is recovered by walking backwards from `.format(`: over whitespace, then either a
balanced `(...)` group or a single `"..."` literal.  **A single literal immediately preceded by
a `+` is itself a finding** — that is the binding trap, and it is reported even when the
conversion count happens to match, because a matching count there is a coincidence.

Usage:

    tools/check-kotlin-format-strings.py                 # the whole tree
    tools/check-kotlin-format-strings.py src/main/kotlin/electrostatics
    tools/check-kotlin-format-strings.py --self-test

Exit status is 1 if any defect is found, 0 otherwise.
"""

import os
import re
import sys

# No space flag: a bare percent sign in prose ("at the mean 84 %, and ...") would otherwise read
# as a conversion, and prose percent signs are common in this repository's findings.
CONVERSION = re.compile(r"%[-#+0,(]*[0-9]*(?:\.[0-9]+)?[a-zA-Z]")


def _strip_templates(source):
    r"""Removes every `${...}` template body, matching braces rather than trusting a regex.

    The naive `\$\{[^{}]*\}` form cannot see a template whose body contains braces of its own,
    and a **nested** `"%.0f".format(it)` inside a `joinToString { ... }` inside a template is
    exactly that shape.  That nested conversion is consumed by its own call long before the
    outer one runs, so counting it against the outer argument list is a false positive — it was
    one, on `StandoffBaseJointStudy.kt:875`, and it is the pattern `T-207` found by hand.
    """
    output = []
    index = 0
    length = len(source)
    while index < length:
        if source.startswith("${", index):
            depth = 1
            index += 2
            while index < length and depth > 0:
                character = source[index]
                if character == "{":
                    depth += 1
                elif character == "}":
                    depth -= 1
                index += 1
            output.append("1")
            continue
        output.append(source[index])
        index += 1
    return "".join(output)


def _conversions(source):
    """Counts `%` conversions, with `%%` escapes and `${...}` string templates removed first.

    A Kotlin template inside a format string is common and legal — `"%.${digits}f"` is one
    conversion whose precision is computed — and reading the `${` as ordinary text loses it.
    """
    return len(CONVERSION.findall(_strip_templates(source).replace("%%", "")))


class FormatDefect:
    """One suspicious `String.format` call: where it is, and what is wrong with it."""

    def __init__(self, path, line, kind, conversions, arguments, excerpt):
        self.path = path
        self.line = line
        self.kind = kind
        self.conversions = conversions
        self.arguments = arguments
        self.excerpt = excerpt

    def __str__(self):
        return (
            f"{self.path}:{self.line}: {self.kind} "
            f"({self.conversions} conversions, {self.arguments} arguments)\n"
            f"    {self.excerpt}"
        )


def skip_string(text, position):
    """Returns the index just past the Kotlin string literal starting at `position`.

    Handles the raw `\"\"\"` form, backslash escapes, and — the case that matters — a `${...}`
    template whose own body may contain further string literals with commas and brackets in
    them.  `joinToString(",")` inside a template is exactly that, and reading its comma as an
    argument separator inflated one call's argument count by two.
    """
    length = len(text)
    if text.startswith('\"\"\"', position):
        end = text.find('\"\"\"', position + 3)
        return length if end < 0 else end + 3
    index = position + 1
    while index < length:
        character = text[index]
        if character == "\\":
            index += 2
            continue
        if character == '"':
            return index + 1
        if text.startswith("${", index):
            depth = 1
            index += 2
            while index < length and depth > 0:
                if text[index] == '"':
                    index = skip_string(text, index)
                    continue
                if text[index] == "{":
                    depth += 1
                elif text[index] == "}":
                    depth -= 1
                index += 1
            continue
        index += 1
    return length


def blank_comments(text):
    """Returns `text` with every comment blanked to spaces, preserving every offset.

    A comment that talks *about* the trap — and this repository has one that does, verbatim —
    is otherwise parsed as code and reported as an instance of it.
    """
    out = list(text)
    index = 0
    length = len(text)
    while index < length:
        character = text[index]
        if character == '"':
            index = skip_string(text, index)
            continue
        if text.startswith("//", index):
            while index < length and text[index] != "\n":
                out[index] = " "
                index += 1
            continue
        if text.startswith("/*", index):
            end = text.find("*/", index + 2)
            end = length if end < 0 else end + 2
            for position in range(index, end):
                if out[position] != "\n":
                    out[position] = " "
            index = end
            continue
        index += 1
    return "".join(out)


def _skip_back_whitespace(text, index):
    while index >= 0 and text[index] in " \t\r\n":
        index -= 1
    return index


def _receiver_span(text, call):
    """Returns `(start, unparenthesised_literal)` for the receiver of a `.format(` at `call`."""
    index = _skip_back_whitespace(text, call - 1)
    if index < 0:
        return None, False
    if text[index] == ")":
        depth = 1
        index -= 1
        while depth > 0 and index >= 0:
            if text[index] == ")":
                depth += 1
            elif text[index] == "(":
                depth -= 1
            index -= 1
        return index + 1, False
    if text[index] == '"':
        index -= 1
        while index >= 0 and not (text[index] == '"' and text[index - 1] != "\\"):
            index -= 1
        return index, True
    return None, False


def _concatenated_format_literal(text, literal_start):
    """Is the bare literal at `literal_start` the tail of a `"..." + "..."` chain?

    Only then is the `+` binding trap live.  `"x %s".format(a) + "y %s".format(b)` is two
    perfectly good calls, and its left operand ends in `)` rather than `"` — which is the whole
    discriminator.  The left literal must itself carry a conversion, or the concatenation was
    never meant to be one format string.
    """
    plus = _skip_back_whitespace(text, literal_start - 1)
    if plus < 0 or text[plus] != "+":
        return False
    end = _skip_back_whitespace(text, plus - 1)
    if end < 0 or text[end] != '"':
        return False
    index = end - 1
    while index >= 0 and not (text[index] == '"' and text[index - 1] != "\\"):
        index -= 1
    if index < 0:
        return False
    left = text[index : end + 1]
    return _conversions(left) > 0


def _argument_count(text, call):
    cursor = call + len(".format(")
    depth = 1
    commas = 0
    position = cursor
    while depth > 0 and position < len(text):
        character = text[position]
        if character == '"':
            # Skip the whole literal: a comma or a bracket inside it is text, not syntax.
            position = skip_string(text, position)
            continue
        if character in "([{":
            depth += 1
        elif character in ")]}":
            depth -= 1
        elif character == "," and depth == 1:
            commas += 1
        position += 1
    body = text[cursor : position - 1]
    return (commas + 1) if body.strip() else 0


def check_source(text, path="<memory>"):
    """Returns the list of [FormatDefect] in one Kotlin source."""
    text = blank_comments(text)
    defects = []
    cursor = 0
    while True:
        call = text.find(".format(", cursor)
        if call < 0:
            break
        cursor = call + 1
        start, bare_literal = _receiver_span(text, call)
        if start is None:
            continue
        receiver = text[start:call]
        conversions = _conversions(receiver)
        arguments = _argument_count(text, call)
        line = text.count("\n", 0, call) + 1
        excerpt = " ".join(receiver.split())[:100]
        if bare_literal and _concatenated_format_literal(text, start):
            if True:
                defects.append(
                    FormatDefect(
                        path, line,
                        "format applied to the LAST literal of a concatenation: "
                        "`+` binds tighter than `.format()` — parenthesise the whole expression",
                        conversions, arguments, excerpt,
                    )
                )
                continue
        if conversions != arguments:
            defects.append(
                FormatDefect(
                    path, line, "conversion/argument count mismatch",
                    conversions, arguments, excerpt,
                )
            )
    return defects


def check_tree(roots):
    defects = []
    for root in roots:
        if os.path.isfile(root):
            with open(root, encoding="utf-8") as handle:
                defects += check_source(handle.read(), root)
            continue
        for directory, _, names in os.walk(root):
            for name in sorted(names):
                if not name.endswith(".kt"):
                    continue
                path = os.path.join(directory, name)
                with open(path, encoding="utf-8") as handle:
                    defects += check_source(handle.read(), path)
    return defects


SELF_TESTS = [
    ('println("a %d".format(x))', 0, "matched, one conversion one argument"),
    ('println("a %d %s".format(x))', 1, "one conversion too many"),
    ('println("a".format(x))', 1, "an argument with no conversion"),
    ('println(("a %d " + "b %.2f").format(x, y))', 0, "parenthesised concatenation, matched"),
    ('println("a %d " + "b %.2f".format(x, y))', 1, "the + binding trap"),
    ('println("100%% sure %d".format(x))', 0, "%% is an escape, not a conversion"),
    ('println("a %d " + "b %.2f %s".format(x, y))', 1, "the binding trap, counts also wrong"),
    ('val s = "x %s".format(a) + "y %s".format(b)', 0, "two separate matched calls"),
    ('println(buildString { }.format())', 0, "no conversions, no arguments"),
    ('println("%.${digits}f".format(x))', 0, "a Kotlin template inside the conversion"),
    ('println("a %s, b".format(listOf(1, 2)))', 0, "a comma inside a nested call"),
    ('println("a %s".format(xs.map { it, }))', 0, "a comma inside a lambda"),
    ('println("a %s %d".format(f(", "), y))', 0, "a comma inside a string argument"),
    ('println("at 84 %, and " + "%.3g".format(x))', 0, "a bare percent sign in prose"),
    ('// literal + "%s".format(x) is the trap\nval a = 1', 0, "a comment describing the trap"),
    ('/* "%s %d".format(x) */\nval a = 1', 0, "a block comment describing the trap"),
    ('println("%s %s".format(a, "${xs.joinToString(\",\")}"))', 0,
     "a comma inside a string template inside a string argument"),
    ('println(("a ${xs.joinToString { "%.0f".format(it) }} b %d").format(y))', 0,
     "a nested format inside a template whose body carries braces"),
    ('println("a ${b { "%d" }} %s %d".format(x))', 1,
     "a template with braces still leaves the outer conversions countable"),
]


def self_test():
    failures = 0
    for source, expected, description in SELF_TESTS:
        found = len(check_source(source))
        if found != expected:
            failures += 1
            print(f"SELF-TEST FAILED — {description}: expected {expected}, found {found}")
            for defect in check_source(source):
                print(f"    {defect}")
    print(f"{len(SELF_TESTS) - failures} of {len(SELF_TESTS)} self-tests pass")
    return failures


def main(argv):
    if "--self-test" in argv:
        return 1 if self_test() else 0
    roots = [argument for argument in argv[1:] if not argument.startswith("-")]
    if not roots:
        roots = ["src"]
    defects = check_tree(roots)
    for defect in defects:
        print(defect)
    print(f"{len(defects)} defect(s) over {', '.join(roots)}")
    return 1 if defects else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
