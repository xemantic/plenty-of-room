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
# `T-337`'s CHEAP BOUND -- every number of the task file's section 2, over the committed corpus,
# with no JVM, no solve and no third-party package.  It answers the scoping question the row
# turns on: how large is population C really, and how much of it is recoverable WITHOUT a
# re-emission.
#
#     gpd/data/T-337-cheap-bound/cheap-bound.py
#
# It reuses `tools/T-327-flatness-resolution.py`'s own predicates by import -- `_records`,
# `_flat_p90_booleans`, `clopper_pearson`, `determinacy`, `resolution_band`, `FILES` -- so the
# population is `C-0223`'s and not a new one.  Reimplementing them would have been the way to
# reproduce `C-0223`'s counts by accident rather than by construction.

import collections
import glob
import importlib.util
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(
    os.path.dirname(os.path.abspath(__file__)))))
TOLERANCE = 0.10
REALISATIONS = 4000

#: The field a record's `flatAtP90` boolean is read on, tried in order.  The searched-distribution
#: studies write their verdict on `searchedP90`, not on `p90OverStroke`.
P90_KEYS = ("p90OverStroke", "searchedP90", "p90")


def _census_module():
    path = os.path.join(ROOT, "tools", "T-327-flatness-resolution.py")
    spec = importlib.util.spec_from_file_location("t327_resolution", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def population_c(resolution, documents):
    """Every `flatAt*P90` boolean in a record carrying NO exceedance, with the p90 it is read on."""
    rows = []
    for tag in resolution.FILES:
        found = []
        resolution._records(documents[tag], "", found)
        for path, record in found:
            booleans = resolution._flat_p90_booleans(record)
            if not booleans:
                continue
            exceedance = record.get("exceedance")
            if isinstance(exceedance, (int, float)) and not isinstance(exceedance, bool):
                continue
            p90 = None
            for key in P90_KEYS:
                value = record.get(key)
                if isinstance(value, float):
                    p90 = (key, value)
                    break
            rows.append((tag, path, booleans, p90, record))
    return rows


def donors(resolution):
    """`{p90: [(file, path, exceedance, nominal)]}` over EVERY committed result file."""
    index = collections.defaultdict(list)
    count = 0
    for path in sorted(glob.glob(os.path.join(ROOT, "gpd/results/*.json"))):
        with open(path) as handle:
            document = json.load(handle)
        found = []
        resolution._records(document, "", found)
        for record_path, record in found:
            exceedance = record.get("exceedance")
            if not isinstance(exceedance, (int, float)) or isinstance(exceedance, bool):
                continue
            for key in P90_KEYS:
                value = record.get(key)
                if isinstance(value, float):
                    index[round(value, 12)].append(
                        (os.path.basename(path), record_path, exceedance,
                         record.get("nominalOverStroke")))
                    count += 1
                    break
    return index, count


def main():
    resolution = _census_module()
    documents = resolution.result_documents(ROOT)
    rows = population_c(resolution, documents)

    # -- 2a, the population, against C-0223 section 4b ----------------------------------------
    booleans = sum(len(r[2]) for r in rows)
    positive = [r for r in rows if any(r[2].values())]
    print("== 2a  population C over C-0223's eighteen files")
    total_booleans = 0
    total_positive = 0
    for tag in resolution.FILES:
        found = []
        resolution._records(documents[tag], "", found)
        for _path, record in found:
            total_booleans += len(resolution._flat_p90_booleans(record))
            total_positive += sum(1 for v in resolution._flat_p90_booleans(record).values() if v)
    print("   flatAt*P90 booleans corpus-wide over the eighteen : %d" % total_booleans)
    print("   ... in a record carrying NO exceedance            : %d" % booleans)
    print("   positive verdicts                                 : %d" % total_positive)
    print("   ... carrying no exceedance (population C)         : %d" % len(positive))
    per_file = collections.Counter(r[0] for r in positive)
    print("   per file: %s" % ", ".join("%s %d" % kv for kv in sorted(per_file.items())))

    # -- 2b, where they live -------------------------------------------------------------------
    print()
    print("== 2b  the blocks, and the records with no exceedance")
    blocks = collections.Counter()
    records = collections.Counter()
    for tag, path, booleans_here, _p90, _record in rows:
        import re
        blocks[(tag, re.sub(r"/\d+", "/*", path))] += sum(1 for v in booleans_here.values() if v)
        records[tag] += 1
    for tag in sorted(records):
        parts = ["%s %d" % (b[1], n) for b, n in sorted(blocks.items()) if b[0] == tag and n]
        print("   %-8s %4d records, positive: %s" % (tag, records[tag], ", ".join(parts) or "none"))

    # -- 2c, recoverability without a re-emission ----------------------------------------------
    print()
    print("== 2c  recoverable with NO re-emission")
    index, donor_count = donors(resolution)
    print("   donor records carrying an exceedance and a p90 : %d over %d distinct p90"
          % (donor_count, len(index)))
    identified = 0
    recovered = []
    for tag, path, booleans_here, p90, record in positive:
        if p90 is None:
            continue
        identified += 1
        match = index.get(round(p90[1], 12))
        if not match:
            continue
        exceedances = set(round(m[2], 12) for m in match)
        # A donor that carries NO `nominalOverStroke` cannot corroborate one; `T-327`'s own
        # result file echoes two of these cells and is such a donor, so it is counted as a
        # match and excluded from the corroboration rather than silently read as a mismatch.
        with_nominal = [m for m in match if isinstance(m[3], float)]
        nominal_agrees = (bool(with_nominal)
                          and all(m[3] == record.get("nominalOverStroke") for m in with_nominal))
        recovered.append((tag, path, p90[1], match, exceedances,
                          "%s (%d of %d donors state one)"
                          % (nominal_agrees, len(with_nominal), len(match))))
    print("   positives whose own p90 field was identified   : %d of %d"
          % (identified, len(positive)))
    print("   RECOVERED by a whole-corpus p90 join           : %d" % len(recovered))
    for tag, path, value, match, exceedances, nominal_agrees in recovered:
        x = int(round(list(exceedances)[0] * REALISATIONS))
        print("     %-8s %-16s p90=%.9f x=%d of %d -> %-12s donors=%s nominal agrees=%s"
              % (tag, path, value, x, REALISATIONS,
                 resolution.determinacy(x, REALISATIONS, 0.95, TOLERANCE),
                 [m[0].split("-")[0] + "-" + m[0].split("-")[1] + m[1] for m in match],
                 nominal_agrees))
    print("   NEEDING a re-emission                          : %d"
          % (len(positive) - len(recovered)))

    # -- 2d, the expected yield ----------------------------------------------------------------
    print()
    print("== 2d  expected yield: the p90 band each positive sits in")
    bands = ((0.0975, 1.0, ">= 0.0975"), (0.0950, 0.0975, "[0.0950, 0.0975)"),
             (0.0900, 0.0950, "[0.0900, 0.0950)"), (0.0, 0.0900, "< 0.0900"))
    by_band = collections.defaultdict(collections.Counter)
    failures = 0
    for tag, _path, _booleans, p90, _record in positive:
        if p90 is None:
            failures += 1
            continue
        for low, high, name in bands:
            if low <= p90[1] < high:
                by_band[name][tag] += 1
                break
    print("   p90-field identification failures: %d" % failures)
    for _low, _high, name in bands:
        counter = by_band[name]
        print("   %-18s %3d   %s" % (name, sum(counter.values()),
                                     ", ".join("%s %d" % kv for kv in sorted(counter.items()))))

    print()
    print("== 2d  the donor calibration, on the 928 records carrying both")
    pairs = []
    for tag in resolution.FILES:
        found = []
        resolution._records(documents[tag], "", found)
        for _path, record in found:
            exceedance = record.get("exceedance")
            p90 = record.get("p90OverStroke")
            if isinstance(exceedance, (int, float)) and not isinstance(exceedance, bool) \
                    and isinstance(p90, float):
                pairs.append((p90, exceedance))
    low_band, high_band = resolution.resolution_band(REALISATIONS, 0.95)
    band_low = low_band / float(REALISATIONS)
    band_high = high_band / float(REALISATIONS)
    print("   pairs: %d ; undetermined band at n=%d, 95%%: [%d, %d] realisations"
          % (len(pairs), REALISATIONS, low_band, high_band))
    for low, high, name in ((0.0975, 0.10, "p90 in [0.0975, 0.10)"),
                            (0.0900, 0.0975, "p90 in [0.0900, 0.0975)"),
                            (0.0, 0.0900, "p90 < 0.0900")):
        subset = [q for q in pairs if low <= q[0] < high]
        inside = [q for q in subset if band_low <= q[1] <= band_high]
        largest = max((q[1] for q in subset), default=None)
        print("   %-26s %3d donors, %3d inside the band, max exceedance %s"
              % (name, len(subset), len(inside),
                 ("%.4f" % largest) if largest is not None else "-"))

    # -- the tolerance and the realisation count, asserted rather than assumed -----------------
    print()
    print("== 0  the seven studies' own constants, read out of the sources")
    sources = {
        "T-279": "tile/HoneycombTiedRegradeStudy.kt", "T-284": "tile/RasterTurnPrestrainSignStudy.kt",
        "T-291": "tile/RasterTurnTwistPriceStudy.kt", "T-297": "tile/CommonModeLinkStudy.kt",
        "T-299": "tile/HoneycombTetheredRegradeStudy.kt", "T-303": "tile/LinkStiffnessThresholdStudy.kt",
        "T-310": "tile/RadialLinkResolutionStudy.kt", "T-316": "tile/SearchedDistributionStudy.kt",
        "T-322": "tile/RouteBCoupledStudy.kt", "T-323": "tile/JointPlacementDistributionStudy.kt",
    }
    import re as _re
    for tag in sorted(sources):
        text = open(os.path.join(ROOT, "src/main/kotlin", sources[tag])).read()
        tolerance = _re.search(r"_TOLERANCE:\s*Double\s*=\s*([0-9.]+)", text)
        realisations = _re.search(
            r"Realisations(?::\s*Int)?\s*=(?:.|\n)*?else\s+(\d+)", text)
        summaries = text.count("summariseDropoutDishing")
        print("   %-8s tolerance=%-6s realisations=%-6s summariseDropoutDishing call sites=%d"
              % (tag, tolerance.group(1) if tolerance else "?",
                 realisations.group(1) if realisations else "?", summaries))


if __name__ == "__main__":
    main()
