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
# `CH-0268` -- an emitter that treats an unrecognised argument as no argument at all will EMIT.
#
# `CLAUDE.md` records this trap twice already (`P-28`'s `./--check/`, and a `./--help/` before it)
# and states the remedy in five words: *"Parse the flag or refuse the argument"*.  It was still
# live in FIVE tools of this repository, measured by running every `tools/*.py` with `--help`
# inside a `git archive HEAD` tree and diffing:
#
#   tools/T-161-fetch-sources.py        -> built a 3-file shadow corpus in ./--help/
#   tools/T-183-emit-result.py          -> overwrote gpd/results/T-183-...json
#   tools/T-200-emit-result.py          -> overwrote gpd/results/T-200-...json
#   tools/T-205-emit-result.py          -> overwrote gpd/results/T-205-...json
#   tools/T-234-emit-classification.py  -> overwrote tools/T-234-classification.json
#
# Four of those five are TRACKED artifacts, and one of them is the classification registry a wired
# gate reads.  This is `CH-0246`'s hazard reached through the argument parser: a corpus-subject
# artifact overwritten by a command whose whole purpose was to ask what the command does.
#
# `refuse_unknown_arguments` is for a tool that takes NO arguments (or only the ones it names).
# A tool that takes real options should use `argparse`, which already refuses correctly.
"""Refuse an argument a tool does not recognise, instead of ignoring it and writing."""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

#: A tool that WRITES must either parse its arguments or refuse them.  Scoped to the two families
#: that write: `*emit*` and `*fetch*`.  The scope is stated rather than inferred, and the census
#: below asserts it against the tree so a new emitter fails the check rather than ageing in.
WRITER_PATTERN = r"(emit|fetch)"


def refuse_unknown_arguments(usage, recognised=(), allow_positional=False):
    """Exit non-zero on any argument outside `recognised`, printing `usage` to stderr.

    A tool whose `__main__` block ignores `sys.argv` will happily EMIT when handed `--help`,
    which is what a cold session types first.  Call this before doing any work.

    `allow_positional` is for a tool that genuinely takes one -- a baseline directory, say.  It
    still refuses every unrecognised OPTION, which is the half that bites: an option beginning
    with `-` is never a path, so accepting it as one is what built `./--help/`.
    """
    unknown = [
        argument for argument in sys.argv[1:]
        if argument not in recognised
        and not (allow_positional and not argument.startswith("-"))
    ]
    if unknown:
        sys.stderr.write("usage: {}\n".format(usage))
        sys.stderr.write("unrecognised argument(s): {}\n".format(" ".join(unknown)))
        sys.stderr.write("refusing to run: this tool WRITES, and an unparsed argument is not data\n")
        raise SystemExit(2)


def writers(root=ROOT):
    """Every `tools/*.py` in a family that writes a committed artifact."""
    directory = os.path.join(root, "tools")
    return sorted(
        name for name in os.listdir(directory)
        if name.endswith(".py") and re.search(WRITER_PATTERN, name)
        and not name.startswith("test-")
    )


#: A refusal, statically.  Three shapes, and the third is the one already in the tree: `T-249` and
#: `T-250` were repaired by hand when `T-272` found this trap, so a predicate that recognised only
#: `argparse` and this module would call the two tools that ALREADY carry the cure defective.
#: This is a PROXY for the dynamic probe (`CH-0268` runs every tool with `--help` inside a
#: `git archive` tree and diffs); the proxy is what a build can afford, and the probe is what says
#: the proxy is right.  The probe UNDER-reports, because a tool that re-emits its own file
#: byte-for-byte changes nothing -- which is why the static predicate is the gated one.
_REFUSES = (
    lambda text: "argparse" in text,
    lambda text: "refuse_unknown_arguments" in text,
    lambda text: bool(re.search(r"\bunknown\b", text))
    and bool(re.search(r"return 2\b|SystemExit\(2\)", text)),
)


def guarded(name, root=ROOT):
    """Does this writer refuse an argument it does not recognise?"""
    text = open(os.path.join(root, "tools", name), encoding="utf-8").read()
    return any(predicate(text) for predicate in _REFUSES)


def census(root=ROOT):
    return {name: guarded(name, root) for name in writers(root)}


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    saved = sys.argv
    try:
        sys.argv = ["tool"]
        refuse_unknown_arguments("tool")
        ok("no arguments is accepted", True)
    except SystemExit:
        ok("no arguments is accepted", False)
    finally:
        sys.argv = saved
    for probe, allowed, expected in (
        (["tool", "--help"], (), True),
        (["tool", "--check"], (), True),
        (["tool", "--check"], ("--check",), False),
        (["tool", "out.json"], (), True),
        (["tool", "--self-test", "--nope"], ("--self-test",), True),
    ):
        saved = sys.argv
        refused = False
        try:
            sys.argv = probe
            refuse_unknown_arguments("tool", allowed)
        except SystemExit:
            refused = True
        finally:
            sys.argv = saved
        ok("{} with allowed {} refuses={}".format(probe[1:], allowed, expected),
           refused == expected)

    for probe, allowed, positional, expected in (
        (["tool", "baseline/"], (), True, False),
        (["tool", "--help"], (), True, True),
        (["tool", "--self-test", "baseline/"], ("--self-test",), True, False),
        (["tool", "-x", "baseline/"], (), True, True),
    ):
        saved = sys.argv
        refused = False
        try:
            sys.argv = probe
            refuse_unknown_arguments("tool", allowed, allow_positional=positional)
        except SystemExit:
            refused = True
        finally:
            sys.argv = saved
        ok("positional-allowing {} refuses={}".format(probe[1:], expected), refused == expected)

    ok("the writer family is non-empty on this tree", len(writers()) > 0)
    ok("a test- fixture is not counted as a writer",
       not any(name.startswith("test-") for name in writers()))
    ok("this guard's own module is not a writer", "cli_guard.py" not in writers())
    ok("argparse counts as parsing", guarded("T-319-emit-result.py"))
    ok("a hand-rolled refusal counts too, which is what T-249 and T-250 carry",
       guarded("T-249-emit-result.py") and guarded("T-250-emit-result.py"))
    ok("this module's own call counts", guarded("T-183-emit-result.py"))
    ok("a writer that names `unknown` but never exits 2 does NOT count",
       not any(predicate("unknown = []\nprint(unknown)\n") for predicate in _REFUSES))
    ok("a writer that exits 2 but never names `unknown` does NOT count",
       not any(predicate("if bad:\n    return 2\n") for predicate in _REFUSES))

    for name, passed in checks:
        print("{}  {}".format("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# {} self-test(s), {} failure(s)".format(len(checks), len(failed)))
    return 1 if failed else 0


def _check():
    reading = census()
    unguarded = sorted(name for name, is_guarded in reading.items() if not is_guarded)
    for name in unguarded:
        print("UNPARSED-ARGUMENTS  tools/{}  -- it WRITES and it ignores sys.argv".format(name))
    print("# {} writer(s) in tools/; {} parse or refuse their arguments; {} do not".format(
        len(reading), len(reading) - len(unguarded), len(unguarded)))
    return 1 if unguarded else 0


if __name__ == "__main__":
    if "--self-test" in sys.argv[1:]:
        raise SystemExit(_self_test())
    if "--check" in sys.argv[1:]:
        raise SystemExit(_check())
    sys.stderr.write("usage: tools/cli_guard.py [--self-test | --check]\n")
    raise SystemExit(2)
