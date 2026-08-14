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
# Trace every number in ANSWERS.md back to the claim that owns it (task T-131).
#
#     tools/trace-answers.py [--answers ANSWERS.md] [--claims gpd/claims] [--challenges gpd/challenges]
#
# ANSWERS.md is the repository's primary deliverable and is explicitly "a synthesis, not a
# source": every number in it is supposed to belong to a claim.  Nothing checked that.  This
# script is the mechanical half of the check — it cannot decide whether a number MEANS what
# ANSWERS.md says it means, but it can decide whether the number appears anywhere in the claim
# the passage cites, which is where the drift shows up.
#
# Output is one record per numeric token, tab-separated, with a status:
#
#   CITED     the token appears in a claim/challenge the same block cites   (traced)
#   ELSEWHERE the token appears in some claim/challenge, but not a cited one (check by hand)
#   ABSENT    the token appears in no claim or challenge at all             (untraceable)
#
# The three statuses are deliberately coarse.  ABSENT is the interesting one and it is not
# automatically an error: arithmetic performed IN the synthesis (a ratio of two claim numbers,
# a percentage) is legitimately absent, so every ABSENT is adjudicated by hand.  What the tool
# guarantees is that none of them is adjudicated by ACCIDENT.
#
# Verified by tools/test-trace-answers.py.
import argparse
import os
import re
import sys

# ANSWERS.md is typographically rich: en dashes for ranges, U+2212 for minus, thin spaces.
# Every comparison below runs on the normalised form, on both sides.
_DASHES = {
    "–": "-",  # en dash
    "—": "-",  # em dash
    "−": "-",  # minus sign
    " ": " ",  # nbsp
    " ": " ",  # narrow nbsp
    " ": " ",  # thin space
    "×": "x",  # multiplication sign
}

# Identifiers that LOOK numeric and are not quantities.  Stripped before tokenising.
_ID_PATTERNS = [
    re.compile(r"\bCH-\d{4}\b"),
    re.compile(r"\bC-\d{4}\b"),
    re.compile(r"\bP-\d{1,4}\b"),
    re.compile(r"\bT-\d{1,4}[a-z]?\b"),
    re.compile(r"\bS-\d{1,4}\b"),
    re.compile(r"\bA\d(?:\.\d)?\b"),  # NDI leaf IDs: A2.1, A8.2
    re.compile(r"§\s*\d(?:\([a-z]\))?"),  # section references
    re.compile(r"\bTRL\s*\d(?:\s*-\s*\d)?\b"),
    re.compile(r"\b\d{4}-\d{2}-\d{2}\b"),  # dates
    re.compile(r"\(\d{4}\)"),  # citation years
    re.compile(r"\bhttps?://\S+"),
    re.compile(r"\]\([^)]*\)"),  # markdown link targets
]

_NUMBER = re.compile(r"\d+(?:\.\d+)?(?:e-?\d+)?")

_CITATION = re.compile(r"\b(CH-\d{4}|C-\d{4})\b")


def normalise(text):
    """Fold the typography ANSWERS.md and the claims use inconsistently."""
    for src, dst in _DASHES.items():
        text = text.replace(src, dst)
    return text


def strip_identifiers(text):
    """Remove claim/task/leaf/section IDs so their digits are not read as quantities."""
    for pattern in _ID_PATTERNS:
        text = pattern.sub(" ", text)
    return text


def tokens(text, min_digits=2):
    """The numeric tokens of a passage, in order, deduplicated.

    `min_digits` drops bare small integers ("two", "3 nm" is kept as 3 only if
    min_digits <= 1).  Counts like 45 and 16 have two digits and are kept; a lone
    "1" or "5" carries no tracing signal and is dropped by default.
    """
    stripped = strip_identifiers(normalise(text))
    seen = []
    for match in _NUMBER.finditer(stripped):
        token = match.group(0)
        digits = sum(1 for character in token if character.isdigit())
        if digits < min_digits:
            continue
        if token not in seen:
            seen.append(token)
    return seen


def citations(text):
    """The claim and challenge IDs a passage names."""
    seen = []
    for match in _CITATION.finditer(text):
        if match.group(1) not in seen:
            seen.append(match.group(1))
    return seen


def load_sources(*directories):
    """{ID: normalised text} for every claim and challenge file."""
    sources = {}
    for directory in directories:
        if not os.path.isdir(directory):
            continue
        for name in sorted(os.listdir(directory)):
            if not name.endswith(".md"):
                continue
            identifier = name.split("-")
            if name.startswith("CH-"):
                key = "-".join(identifier[:2])
            elif name.startswith("C-"):
                key = "-".join(identifier[:2])
            else:
                continue
            with open(os.path.join(directory, name), encoding="utf-8") as handle:
                sources[key] = normalise(handle.read())
    return sources


def contains(source_text, token):
    """Whether a claim states this token, as a number rather than as a substring.

    "45" must not match inside "1.45" or "450"; the guard is that the character
    either side of the match is not a digit and not a decimal point.
    """
    for match in re.finditer(re.escape(token), source_text):
        start, end = match.start(), match.end()
        before = source_text[start - 1] if start > 0 else " "
        after = source_text[end] if end < len(source_text) else " "
        if before.isdigit() or before == ".":
            continue
        if after.isdigit() or after == ".":
            continue
        return True
    return False


def blocks(answers_text):
    """Split ANSWERS.md into blocks: table rows one each, paragraphs one each."""
    result = []
    paragraph = []
    for number, line in enumerate(answers_text.splitlines(), start=1):
        if line.startswith("|"):
            if paragraph:
                result.append((paragraph[0][0], "\n".join(text for _, text in paragraph)))
                paragraph = []
            result.append((number, line))
        elif line.strip() == "":
            if paragraph:
                result.append((paragraph[0][0], "\n".join(text for _, text in paragraph)))
                paragraph = []
        else:
            paragraph.append((number, line))
    if paragraph:
        result.append((paragraph[0][0], "\n".join(text for _, text in paragraph)))
    return result


def trace(answers_text, sources, min_digits=2):
    """One record per (block, token): (line, token, status, cited, owners)."""
    records = []
    for line, text in blocks(answers_text):
        cited = citations(text)
        for token in tokens(text, min_digits=min_digits):
            owners = [key for key, body in sorted(sources.items()) if contains(body, token)]
            if any(key in cited for key in owners):
                status = "CITED"
            elif owners:
                status = "ELSEWHERE"
            else:
                status = "ABSENT"
            records.append((line, token, status, cited, owners))
    return records


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--answers", default="ANSWERS.md")
    parser.add_argument("--claims", default="gpd/claims")
    parser.add_argument("--challenges", default="gpd/challenges")
    parser.add_argument("--min-digits", type=int, default=2)
    parser.add_argument("--status", default="", help="only report this status")
    arguments = parser.parse_args(argv)

    with open(arguments.answers, encoding="utf-8") as handle:
        answers_text = handle.read()
    sources = load_sources(arguments.claims, arguments.challenges)
    records = trace(answers_text, sources, min_digits=arguments.min_digits)

    counts = {"CITED": 0, "ELSEWHERE": 0, "ABSENT": 0}
    for line, token, status, cited, owners in records:
        counts[status] += 1
        if arguments.status and status != arguments.status:
            continue
        print(
            "{}\t{}\t{}\t{}\t{}".format(
                line, token, status, ",".join(cited) or "-", ",".join(owners[:6]) or "-"
            )
        )
    print(
        "# {} tokens: {} CITED, {} ELSEWHERE, {} ABSENT".format(
            len(records), counts["CITED"], counts["ELSEWHERE"], counts["ABSENT"]
        ),
        file=sys.stderr,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
