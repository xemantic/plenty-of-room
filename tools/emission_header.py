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
# `environment/Regime.kt`'s. A Python emitter that computes no lattice and solves no environment
# passes `"none"` and `None`, which is a CLAIM and not an omission -- which is why the key is
# written out rather than left off.
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
    check("an absent regime is an explicit null", out["emission"]["regime"], None)
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
