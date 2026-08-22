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
# T-272 -- `structure/ResultEmission.kt`'s emission header, for the emitters written in Python.
#
# WHY IT IS MIRRORED AND NOT IMPORTED. `C-0162` §5(c) is the finding this exists for: **sixteen
# committed result files are written by a Python tool in `tools/` and by no Kotlin study, and no
# rule in the Kotlin emission layer reaches any of them.** An emission rule reaches only the
# emitters written in its own language, so the rule has to exist twice -- and the two copies are
# held together by a test that reads the Kotlin constants, in the same shape
# `tools/T-250-movement.py` mirrors `PARAMETER_RECORDS`.
#
# The tag vocabulary is `lattice/LatticeTag.kt`'s and the regime shape is
# `environment/RegimeSet.kt`'s -- a LIST of `environment/Regime.kt` blocks, one per environment
# state some record of the file was solved at. `T-286` / `CH-0224`: a `Regime` describes a SOLVE
# and a result file is a BAG of solves, and 17 of the 22 studies naming `MagnesiumChlorideBuffer`
# sweep the molarity, so a block that can hold one was `null` on 136 of 136 headed files.
#
# The three values, and they are three because `CLAUDE.md` requires it -- *a `null` that means "no
# requirement" and a `null` that means "not stated" are different values*:
#
#   None   the emitter has not stated what it was solved at -- the residue, to be COUNTED
#   []     a claim that no environment coordinate enters this result at all
#   [...]  the states it was solved at, one dict per state
LATTICE_TAGS = ("square", "honeycomb", "both", "none")


def with_emission_header(document, lattice, regime=None):
    """`document` with an `emission` block, carrying `lattice` and `regime`, as its first key.

    Namespaced under one key because the corpus already owns both sub-key names -- `lattice` is a
    top-level list in `T-152` and 101 numeric result leaves elsewhere, `regime` a string leaf in
    five files. Refuses to overwrite, exactly as the Kotlin does: a silent shadow is how a query
    reads a wrong lattice as an authoritative one.
    """
    if not isinstance(document, dict):
        raise TypeError("a result record must be a mapping, was: %s" % type(document).__name__)
    if lattice not in LATTICE_TAGS:
        raise ValueError(
            'no lattice tag is spelled "%s"; this project knows %s'
            % (lattice, ", ".join(LATTICE_TAGS))
        )
    if "emission" in document:
        raise ValueError(
            'this record already carries an "emission" key; an emission header may not overwrite '
            "what the emitter emitted"
        )
    if regime is not None and not isinstance(regime, list):
        raise TypeError(
            "a regime block is a LIST of solved environment states, or None where the emitter has "
            "not stated one; an empty list is the claim that no environment coordinate enters "
            "this result, and it is not the same value as None (T-286, CH-0224). Was: %s"
            % type(regime).__name__
        )
    headed = {"emission": {"lattice": lattice, "regime": regime}}
    headed.update(document)
    return headed


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append("%s: expected %r, got %r" % (name, expected, actual))

    out = with_emission_header({"answer": 1}, "square")
    check("the header is first", list(out), ["emission", "answer"])
    check("the tag is the word", out["emission"]["lattice"], "square")
    check("an unstated regime is an explicit null", out["emission"]["regime"], None)
    empty = with_emission_header({"answer": 1}, "none", regime=[])
    check("no environment coordinate is an empty list", empty["emission"]["regime"], [])
    check(
        "and an empty list is not the same value as None",
        empty["emission"]["regime"] is None,
        False,
    )
    solved = with_emission_header({"answer": 1}, "none", regime=[{"bufferMillimolar": 2.0}])
    check("a solved state is a member", solved["emission"]["regime"][0]["bufferMillimolar"], 2.0)
    try:
        with_emission_header({}, "none", regime={"bufferMillimolar": 2.0})
        failures.append("a bare regime dict: expected a refusal")
    except TypeError:
        pass
    check("the body survives", out["answer"], 1)
    kept = with_emission_header({"lattice": ["T-152 tabulates its own"]}, "square")
    check("a body's own top-level lattice survives", kept["lattice"], ["T-152 tabulates its own"])
    for bad, message in ((["a"], TypeError), ({"emission": "x"}, ValueError)):
        try:
            with_emission_header(bad, "none")
            failures.append("%r: expected a refusal" % (bad,))
        except message:
            pass
    try:
        with_emission_header({}, "cubic")
        failures.append("an unknown tag: expected a refusal")
    except ValueError:
        pass
    # The vocabulary must be the Kotlin's, or the two emission layers disagree about a word.
    import os
    import re

    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    kotlin = open(os.path.join(root, "src", "main", "kotlin", "lattice", "LatticeTag.kt")).read()
    declared = tuple(re.findall(r'^\s+[A-Z]+\("([a-z]+)"\)[,;]$', kotlin, re.MULTILINE))
    check("the tag vocabulary matches lattice/LatticeTag.kt", declared, LATTICE_TAGS)
    for failure in failures:
        print("FAIL " + failure)
    print("%d self-test(s) failed" % len(failures) if failures else "self-tests pass")
    return 1 if failures else 0


if __name__ == "__main__":
    import sys

    sys.exit(_selftest())
