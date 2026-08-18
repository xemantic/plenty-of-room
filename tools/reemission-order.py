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
# T-200 / C-0117 -- the order a set of result files must be RE-EMITTED in.
#
#     tools/reemission-order.py T-149 T-79 T-136 T-99 T-157 T-108 T-134 T-152 T-116 T-135 T-138
#     tools/reemission-order.py --selftest
#
# WHY THIS EXISTS. `CLAUDE.md`'s rule is that when a repair moves a downstream result file you
# RE-EMIT it and amend the claim, because git already holds the history and a file the code cannot
# reproduce destroys the byte-for-byte re-run diff half this repository's claims rest on.
# `C-0101` wrote that rule, re-emitted eleven files -- and ran a CONSUMER before its own PRODUCER,
# so the committed `T-157` reproduces the *pre-repair* `T-149` and `C-0092`'s `A5` clause measures a
# difference `C-0101` had already absorbed (`CH-0131`).
#
# A re-emission sweep is therefore not a LIST, it is a TOPOLOGICAL SORT of the reader census -- and
# `tools/result-reader-census.py` already derives exactly the graph the sort needs. This is the
# twenty lines that turn that graph into an order.
#
# It deliberately does NOT gate anything. A residual scan over the corpus finds 499 nonzero
# reproduction departures in 64 of 104 result files, and the great majority are legitimate
# literature cross-checks (Fields et al. at 15 %, Bosco at 5 %) -- so a staleness gate built on
# residuals would fire constantly on correct files, which is the one failure `C-0080` says a
# checker cannot afford. The order is a tool you ASK, not a test that runs.
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CENSUS = os.path.join(ROOT, "gpd", "results", "P-22-result-reader-census.json")


def tag_of(result_file_name):
    """`T-149-recommended-element-fold.json` -> `T-149`. The task ID is the stable handle."""
    parts = result_file_name.split("-")
    if len(parts) < 2:
        return result_file_name
    return "{}-{}".format(parts[0], parts[1])


def edges_from_census(census):
    """{producer tag: set of tags it reads}, over every study that writes a result file.

    Uses `reads`, which the census documents as the union of direct and transitive reads -- a
    transitive read (a path assembled from a directory in the caller and a name in a helper) is
    exactly the kind `C-0073`'s grep-based audit missed, and it constrains the order just as much.
    """
    reads = {}
    for info in census["studies"].values():
        written = info.get("writes")
        if not written:
            continue
        producer = tag_of(written)
        consumed = {tag_of(name) for name in info.get("reads", [])}
        reads.setdefault(producer, set()).update(consumed - {producer})
    return reads


def order(tags, reads):
    """The order `tags` must be re-emitted in: every producer before every consumer of it.

    Kahn's algorithm, with ties broken by the tag's own name so two runs agree -- the same
    determinism discipline the result files are held to. Returns (order, cycles): `cycles` is the
    tags that could not be placed, which would be a genuine circular dependency and is reported
    rather than silently dropped.
    """
    wanted = list(dict.fromkeys(tags))
    within = {t: {d for d in reads.get(t, set()) if d in wanted} for t in wanted}
    placed = []
    remaining = set(wanted)
    while remaining:
        ready = sorted(t for t in remaining if not (within[t] - set(placed)))
        if not ready:
            break
        placed.extend(ready)
        remaining -= set(ready)
    return placed, sorted(remaining)


def dependency_pairs(tags, reads):
    """The (producer, consumer) pairs inside `tags` -- the constraints the order exists for."""
    wanted = set(tags)
    pairs = []
    for consumer in sorted(wanted):
        for producer in sorted(reads.get(consumer, set()) & wanted):
            pairs.append((producer, consumer))
    return pairs


def _selftest():
    failures = []

    def check(name, actual, expected):
        if actual != expected:
            failures.append(name)
            print("FAIL {}: expected {!r}, got {!r}".format(name, expected, actual))
        else:
            print("ok   {}".format(name))

    check("a tag is the first two dash-separated fields",
          tag_of("T-149-recommended-element-fold.json"), "T-149")
    check("a lettered task keeps its letter", tag_of("T-1d-layer-response.json"), "T-1d")
    # A producer must precede its consumer, whichever order it is asked in.
    reads = {"T-157": {"T-149"}, "T-149": set(), "T-138": {"T-136"}, "T-136": set()}
    check("the consumer follows the producer",
          order(["T-157", "T-149"], reads)[0], ["T-149", "T-157"])
    check("and does so when already in order",
          order(["T-149", "T-157"], reads)[0], ["T-149", "T-157"])
    check("two independent chains interleave deterministically",
          order(["T-157", "T-138", "T-149", "T-136"], reads)[0],
          ["T-136", "T-149", "T-138", "T-157"])
    check("an edge to a file NOT being re-emitted does not constrain",
          order(["T-157"], reads)[0], ["T-157"])
    check("the pairs report only the constraints inside the set",
          dependency_pairs(["T-157", "T-149"], reads), [("T-149", "T-157")])
    check("and none when the producer is outside it",
          dependency_pairs(["T-157"], reads), [])
    # A cycle is reported, never silently dropped or partially emitted.
    cyclic = {"A": {"B"}, "B": {"A"}}
    check("a cycle is reported as unplaced", order(["A", "B"], cyclic), ([], ["A", "B"]))
    check("a tag with no census entry is placed rather than lost",
          order(["T-999"], reads)[0], ["T-999"])
    check("duplicates collapse", order(["T-149", "T-149"], reads)[0], ["T-149"])
    if failures:
        print("\n{} check(s) FAILED".format(len(failures)))
        return 1
    print("\nall checks passed")
    return 0


def main(argv):
    if "--selftest" in argv:
        return _selftest()
    if not argv:
        print(__doc__)
        print("usage: tools/reemission-order.py <tag> [<tag> ...]   (e.g. T-149 T-157)")
        return 2
    with open(CENSUS, encoding="utf-8") as handle:
        census = json.load(handle)
    reads = edges_from_census(census)
    tags = [tag_of(a) if a.endswith(".json") else a for a in argv]
    placed, cycles = order(tags, reads)
    pairs = dependency_pairs(tags, reads)
    print("# {} file(s), {} dependency constraint(s) inside the set".format(len(placed), len(pairs)))
    for producer, consumer in pairs:
        print("#   {} must be re-emitted BEFORE {}".format(producer, consumer))
    if not pairs:
        print("#   none -- any order is safe")
    for position, tag in enumerate(placed, start=1):
        print("{}\t{}".format(position, tag))
    if cycles:
        print("# CIRCULAR, could not be ordered: {}".format(", ".join(cycles)), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
