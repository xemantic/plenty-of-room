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
# T-327 cheap bound, prototype -- the resolution of the flatness census, before any code is written.
#
# It answers four questions over the committed corpus, with no solve, no JVM and no re-emission:
#
#   2a  is `C-0221` section 5's transferred threshold commensurate with the axis it is entered on?
#   2b  is the `1 146` a count of VERDICTS or of LEAVES?
#   2c  what is `flatAtP90` a function of, and what is THAT quantity's resolution?
#   2d  does the ORDERING survive where the LEVEL does not?
#
# Run:  python3 gpd/data/T-327-cheap-bound/resolution-bound.py
#
# It takes NO arguments and refuses any (`CH-0268`).  It writes nothing.
import glob
import json
import math
import os
import sys

TOLERANCE = 0.10

#: The eighteen committed files carrying a `HoneycombDeflection` dishing --- `C-0221` section 5's own set.
FILES = ["T-253", "T-254", "T-263", "T-267", "T-279", "T-284", "T-291", "T-294",
         "T-297", "T-299", "T-303", "T-304", "T-307", "T-310", "T-315", "T-316",
         "T-322", "T-323"]

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..")


def _load(tag):
    matches = glob.glob(os.path.join(ROOT, "gpd/results/%s-*.json" % tag))
    if not matches:
        raise SystemExit("no committed result file for %s" % tag)
    return json.load(open(matches[0]))


def _leaves(node, path, parent, out):
    if isinstance(node, dict):
        for key, value in node.items():
            _leaves(value, path + "/" + key, node, out)
    elif isinstance(node, list):
        for index, value in enumerate(node):
            _leaves(value, path + "/%d" % index, parent, out)
    elif isinstance(node, float):
        out.append((path, node, parent))


def census():
    """`C-0221` section 5's predicate, verbatim, so the recount is against the same population."""
    rows = []
    for tag in FILES:
        out = []
        _leaves(_load(tag), "", None, out)
        for path, value, parent in out:
            key = path.rsplit("/", 1)[-1]
            if not (0.09 <= value <= 0.11):
                continue
            if not isinstance(parent, dict):
                continue
            if not any(isinstance(x, bool) for x in parent.values()):
                continue
            if not (key.endswith("OverStroke") or "ishing" in key):
                continue
            rows.append((abs(value - TOLERANCE) / TOLERANCE, value, tag, path, parent))
    rows.sort(key=lambda r: r[0])
    return rows


def records_with_exceedance():
    """Every record carrying an `exceedance` and at least one `flatAt*P90*` boolean."""
    found = []

    def walk(tag, node, path):
        if isinstance(node, dict):
            exceedance = node.get("exceedance")
            if isinstance(exceedance, (int, float)):
                booleans = {
                    k: v for k, v in node.items()
                    if isinstance(v, bool) and k.lower().startswith("flat") and "p90" in k.lower()
                }
                if booleans:
                    found.append((tag, path, exceedance, booleans,
                                  node.get("exceedanceStandardError")))
            for k, v in node.items():
                walk(tag, v, path + "/" + k)
        elif isinstance(node, list):
            for i, v in enumerate(node):
                walk(tag, v, path + "/%d" % i)

    for tag in FILES:
        walk(tag, _load(tag), "")
    return found


def clopper_pearson(x, n, confidence=0.95):
    """The exact two-sided interval, by bisection on the binomial tail --- no scipy needed."""
    alpha = (1.0 - confidence) / 2.0

    def upper_tail(p):                      # P(X >= x)
        return 1.0 - _binom_cdf(x - 1, n, p)

    def lower_tail(p):                      # P(X <= x)
        return _binom_cdf(x, n, p)

    low = 0.0 if x == 0 else _bisect(lambda p: upper_tail(p) - alpha, 0.0, 1.0)
    high = 1.0 if x == n else _bisect(lambda p: lower_tail(p) - alpha, 0.0, 1.0)
    return low, high


def _bisect(f, lo, hi, iterations=200):
    for _ in range(iterations):
        mid = 0.5 * (lo + hi)
        if f(lo) * f(mid) <= 0.0:
            hi = mid
        else:
            lo = mid
    return 0.5 * (lo + hi)


def _binom_cdf(k, n, p):
    """P(X <= k) by the regularised incomplete beta, via a continued fraction."""
    if k < 0:
        return 0.0
    if k >= n:
        return 1.0
    return _betainc(n - k, k + 1, 1.0 - p)


def _betainc(a, b, x):
    if x <= 0.0:
        return 0.0
    if x >= 1.0:
        return 1.0
    front = math.exp(math.lgamma(a + b) - math.lgamma(a) - math.lgamma(b)
                     + a * math.log(x) + b * math.log(1.0 - x))
    if x < (a + 1.0) / (a + b + 2.0):
        return front * _betacf(a, b, x) / a
    return 1.0 - math.exp(math.lgamma(a + b) - math.lgamma(a) - math.lgamma(b)
                          + b * math.log(1.0 - x) + a * math.log(x)) * _betacf(b, a, 1.0 - x) / b


def _betacf(a, b, x, iterations=400, tiny=1e-300):
    qab, qap, qam = a + b, a + 1.0, a - 1.0
    c, d = 1.0, 1.0 - qab * x / qap
    if abs(d) < tiny:
        d = tiny
    d = 1.0 / d
    h = d
    for m in range(1, iterations + 1):
        m2 = 2 * m
        aa = m * (b - m) * x / ((qam + m2) * (a + m2))
        d = 1.0 + aa * d
        if abs(d) < tiny:
            d = tiny
        c = 1.0 + aa / c
        if abs(c) < tiny:
            c = tiny
        d = 1.0 / d
        h *= d * c
        aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2))
        d = 1.0 + aa * d
        if abs(d) < tiny:
            d = tiny
        c = 1.0 + aa / c
        if abs(c) < tiny:
            c = tiny
        d = 1.0 / d
        delta = d * c
        h *= delta
        if abs(delta - 1.0) < 1e-14:
            break
    return h


def two_sided_binomial_p(x, n, p0):
    lower = _binom_cdf(x, n, p0)
    upper = 1.0 - _binom_cdf(x - 1, n, p0)
    return min(1.0, 2.0 * min(lower, upper))


def one_sided_binomial_p(x, n, p0, flat):
    return _binom_cdf(x, n, p0) if flat else 1.0 - _binom_cdf(x - 1, n, p0)


def main():
    if len(sys.argv) > 1:
        sys.stderr.write("usage: resolution-bound.py   (takes no arguments)\n")
        raise SystemExit(2)

    rows = census()
    print("=== 2a  the transferred threshold, on the axis it is entered on ===")
    print("`C-0221` section 5's rel axis is |v - 0.10| / 0.10; the corpus's `departure` is")
    print("|fine - coarse| / coarse.  For v near 0.10 the two are commensurate, so `C-0180`'s")
    print("4.57e-4 belongs on that axis as 4.57e-4 and NOT as 4.57e-3.")
    for threshold, label in ((4.57e-3, "as published in C-0221 section 5"),
                             (4.57e-4, "commensurate")):
        print("  within %-9.4g  %-34s %5d" % (
            threshold, label, sum(1 for r in rows if r[0] <= threshold)))
    coarse, fine = 0.0995744767, 0.0996199888
    departure = (fine - coarse) / coarse
    margin = (TOLERANCE - coarse) / coarse
    print("  C-0180 cell 69: departure %.6e, margin/value %.6e, ratio %.4f  (its own '9.3')"
          % (departure, margin, margin / departure))
    print("  read as published the census puts the flip margin 4.2724e-3 at only %.2f of the"
          % (4.2724e-3 / 4.57e-3))
    print("  departure, where C-0180's own sentence puts it at 9.3 --- a factor of ten, visible")
    print("  in the channel ORDERING with no code run at all.")

    print()
    print("=== 2b  1 146 counts LEAVES, not verdicts ===")
    diagnostics = ("medianOverStroke", "worstSingleRemovalOverStroke",
                   "uncoupledDishingOverStroke", "p95OverStroke",
                   "worstSinglePathRemovalOverStroke")
    counted = {}
    for _, _, _, path, _ in rows:
        key = path.rsplit("/", 1)[-1]
        counted[key] = counted.get(key, 0) + 1
    print("  total %d over %d distinct leaf keys" % (len(rows), len(counted)))
    total = 0
    for key in diagnostics:
        print("    %-34s %5d   no boolean is written on it" % (key, counted.get(key, 0)))
        total += counted.get(key, 0)
    print("    %-34s %5d" % ("(those five)", total))
    print("  tightest two:")
    for rel, value, tag, path, parent in rows[:2]:
        booleans = [k for k, v in parent.items() if isinstance(v, bool)]
        print("    rel=%.4e  %.9f  %s%s" % (rel, value, tag, path))
        print("      booleans in that record: %s" % ", ".join(booleans))

    print()
    print("=== 2c  what `flatAtP90` is a function of, and that quantity's resolution ===")
    records = records_with_exceedance()
    booleans = sum(len(b) for _, _, _, b, _ in records)
    disagree = [(t, p, k) for t, p, e, b, _ in records for k, v in b.items()
                if (e <= TOLERANCE + 1e-12) != v]
    print("  records carrying both: %d, booleans: %d" % (len(records), booleans))
    print("  `flatAt*P90` <=> exceedance <= 0.10 disagrees at %d of %d" % (len(disagree), booleans))
    counts = {}
    for _, _, exceedance, _, standard_error in records:
        if isinstance(standard_error, (int, float)) and standard_error > 0.0:
            n = round(exceedance * (1.0 - exceedance) / standard_error ** 2)
            counts[n] = counts.get(n, 0) + 1
    print("  realisation counts backed out of the emitted standard error: %s" % counts)

    n = 4000
    sigma = math.sqrt(TOLERANCE * (1.0 - TOLERANCE) / n)
    print("  at n = %d the binomial sigma at p0 = 0.10 is %.6e, i.e. %.2f realisations of %d;"
          % (n, sigma, sigma * n, n))
    print("  a two-sided 95%% interval is %d +/- %.1f counts" % (TOLERANCE * n, 1.959964 * sigma * n))
    positive = negative = positive_undetermined = negative_undetermined = 0
    undetermined = []
    for tag, path, exceedance, bools, _ in records:
        x = round(exceedance * n)
        low, high = clopper_pearson(x, n)
        contains = low <= TOLERANCE <= high
        for key, value in bools.items():
            if value:
                positive += 1
            else:
                negative += 1
            if contains:
                if value:
                    positive_undetermined += 1
                else:
                    negative_undetermined += 1
        if contains:
            undetermined.append((tag, path, x, low, high, any(bools.values())))
    print("  booleans reading TRUE  %4d, of which UNDETERMINED at 95%% Clopper-Pearson: %d"
          % (positive, positive_undetermined))
    print("  booleans reading FALSE %4d, of which UNDETERMINED: %d"
          % (negative, negative_undetermined))
    print("  the undetermined records:")
    for tag, path, x, low, high, flat in sorted(undetermined):
        print("    %-7s %-24s X=%4d/400  CP=[%.5f, %.5f]  two-sided p=%.3f  flat=%s"
              % (tag, path, x, low, high, two_sided_binomial_p(x, n, TOLERANCE), flat))
    print("  the declared sweep, undetermined booleans by confidence level:")
    for confidence in (0.90, 0.95, 0.99):
        total = 0
        for _, _, exceedance, bools, _ in records:
            low, high = clopper_pearson(round(exceedance * n), n, confidence)
            if low <= TOLERANCE <= high:
                total += len(bools)
        print("    %.0f%%  %d" % (confidence * 100, total))

    print()
    print("  C-0180's two recovered cells, density-free:")
    for label, coarse, fine, exceedance in (
            ("cell 69,  30 paths", 0.0995744767, 0.0996199888, 0.098),
            ("cell 109, 50 paths", 0.0998791032, 0.0998892051, 0.0995)):
        x = round(exceedance * n)
        departure = (fine - coarse) / coarse
        margin = (TOLERANCE - coarse) / coarse
        print("    %s: margin/discretisation departure = %.4f ; margin/binomial sigma = %.3f ;"
              % (label, margin / departure, abs(exceedance - TOLERANCE) / sigma))
        print("      X = %d of %d against %d, one-sided binomial p = %.3f, two-sided %.3f"
              % (x, n, int(TOLERANCE * n), one_sided_binomial_p(x, n, TOLERANCE, True),
                 two_sided_binomial_p(x, n, TOLERANCE)))

    print()
    print("=== 2d  the ORDERING, from the corpus's own paired block ===")
    paired = _load("T-279").get("paired") or []
    for record in paired:
        if record.get("verdictMoved"):
            worse = record.get("fractionTiedIsWorse")
            wins = round((1.0 - worse) * record["realisations"])
            print("    %d paths, %s: the tie is flatter at %d of %d paired realisations"
                  % (record["pathCount"], record["distribution"], wins, record["realisations"]))
            print("      sign-test two-sided p = %.3e ; tied p90 %.9f, untied %.9f"
                  % (two_sided_binomial_p(min(wins, record["realisations"] - wins),
                                          record["realisations"], 0.5),
                     record["tiedP90OverStroke"], record["untiedP90OverStroke"]))
    print()
    print("  ORDERING resolved; LEVEL not.  That is the whole of the answer's shape.")


if __name__ == "__main__":
    main()
