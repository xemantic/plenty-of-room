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
# T-327 -- the resolution of the flatness census.
#
#     tools/T-327-flatness-resolution.py [--self-test] [--report] [--confidence 0.95]
#
# WHAT THIS IS FOR.  `C-0221` section 5 censused the eighteen committed result files carrying a
# `HoneycombDeflection` dishing and asked whether a verdict inside the model's own convergence
# departure is a verdict.  Two things about that question turn out to be wrong, and one thing
# about it turns out to be much more serious than it looks.
#
#   1  THE TRANSFERRED THRESHOLD IS A FACTOR OF TEN OUT.  `C-0180`'s `4.57e-4` is a departure
#      RELATIVE TO THE VALUE -- its own sentence divides a margin of `0.00426` by it and gets
#      `9.3` -- and the census enters it on a `|v - 0.10| / 0.10` axis as `4.57e-3`.  Read
#      commensurately the count is 2, not 99.
#
#   2  THE `1 146` COUNTS LEAVES.  The predicate's boolean test is on the PARENT record, so a
#      median or a p95 sitting beside a verdict is counted as one.
#
#   3  AND THE RESOLUTION IS THE WRONG QUANTITY.  `coupling/DropoutRobustPlacement.kt` computes
#      `flatAtP90 = orderStatistic(sample, 0.90) < tolerance` with `orderStatistic` at
#      `sorted[ceil(0.9 n) - 1]`, so at n = 4 000 the verdict holds iff at most 400 realisations
#      exceed the tolerance -- that is, iff `exceedance <= 0.10`, a field the corpus ALREADY
#      EMITS, with its own `exceedanceStandardError` beside it.  So the verdict is a binomial
#      statement and its resolution is the sampling error of a proportion, which is 30 to 100
#      times the discretisation departure the corpus quotes.
#
# WHAT IT REFUSES TO DO.  It classifies a convergence axis by KIND and lets only a DISCRETISATION
# axis into a resolution: a training-realisation axis is the SEARCH's variance, a
# composite-fraction axis is a physical BRACKET, a link-penalty axis is `C-0100`'s binary, and an
# axis the rules do not recognise is UNCLASSIFIED rather than guessed.
#
# IT WRITES NOTHING and it re-emits nothing.  `tools/T-327-emit-result.py` is the emitter.
"""The resolution of the flatness census: what a `flatAtP90` verdict can and cannot resolve."""
import argparse
import collections
import glob
import json
import math
import os
import sys
from fractions import Fraction

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

#: `T-5b`'s flatness convention, as a fraction of the tile's own free stroke.
TOLERANCE = 0.10

#: The eighteen committed files carrying a `HoneycombDeflection` dishing -- `C-0221` section 5's set.
FILES = ("T-253", "T-254", "T-263", "T-267", "T-279", "T-284", "T-291", "T-294",
         "T-297", "T-299", "T-303", "T-304", "T-307", "T-310", "T-315", "T-316",
         "T-322", "T-323")

#: `C-0221` section 5's own published channels, kept so the reproduction is exact before the
#: recount is offered.  The fourth is the one reading 2a corrects.
PUBLISHED_CHANNELS = (
    (1.0e-5, "the tightest reading itself"),
    (5.0e-4, "CH-0284's collar channel"),
    (4.2724e-3, "the movement that would flip C-0180's tightest recovered cell"),
    (4.57e-3, "C-0180's beam-subdivision departure, AS PUBLISHED -- 10x too generous"),
    (6.7e-3, "CH-0284's bond-prestrain channel"),
    (4.02e-2, "the prestrain channel at convention C's 6x"),
)

#: The same threshold, commensurate with the axis it is entered on.
COMMENSURATE_CHANNEL = (4.57e-4, "C-0180's beam-subdivision departure, commensurate")

#: Leaf keys the census admits that no boolean of their own record is written on.
DIAGNOSTIC_LEAVES = ("medianOverStroke", "worstSingleRemovalOverStroke",
                     "uncoupledDishingOverStroke", "p95OverStroke",
                     "worstSinglePathRemovalOverStroke")

Row = collections.namedtuple("Row", "relative value tag path leaf")

#: A convergence axis's KIND, and whether it may enter a numerical resolution.  Ordered: the
#: SEARCH tests run first, because a search's own grid axis carries a grid word too and must not
#: be caught by a bare "search".
_AXIS_RULES = (
    ("SEARCH", ("realisation", "descent", "sweeps", "screening")),
    ("PENALTY", ("penalty",)),
    ("PARAMETER", ("composite fraction", "stagger")),
    ("DISCRETISATION", ("subdivision", "sample", "grid", "samples per", "node spacing")),
)


# --------------------------------------------------------------------------------------------
# The binomial arithmetic.  No third-party package: the tail is the regularised incomplete beta
# by continued fraction, so the EXACT Clopper-Pearson interval is available from the standard
# library alone and the census runs anywhere.
# --------------------------------------------------------------------------------------------

def _betacf(a, b, x, iterations=500, tiny=1e-300):
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
        if abs(delta - 1.0) < 1e-15:
            break
    return h


def regularised_beta(a, b, x):
    """`I_x(a, b)`, the regularised incomplete beta."""
    if x <= 0.0:
        return 0.0
    if x >= 1.0:
        return 1.0
    logfront = (math.lgamma(a + b) - math.lgamma(a) - math.lgamma(b)
                + a * math.log(x) + b * math.log(1.0 - x))
    if x < (a + 1.0) / (a + b + 2.0):
        return math.exp(logfront) * _betacf(a, b, x) / a
    return 1.0 - math.exp(logfront) * _betacf(b, a, 1.0 - x) / b


def binom_cdf(k, n, p):
    """`P(X <= k)` for `X ~ Binomial(n, p)`, exactly, via the incomplete beta."""
    if k < 0:
        return 0.0
    if k >= n:
        return 1.0
    return regularised_beta(n - k, k + 1, 1.0 - p)


def _bisect(f, lo, hi, iterations=200):
    flo = f(lo)
    for _ in range(iterations):
        mid = 0.5 * (lo + hi)
        if flo * f(mid) <= 0.0:
            hi = mid
        else:
            lo = mid
            flo = f(lo)
    return 0.5 * (lo + hi)


def clopper_pearson(x, n, confidence=0.95):
    """The EXACT two-sided Clopper-Pearson interval on `x` successes in `n` draws.

    `(0.0, ...)` at `x = 0` and `(..., 1.0)` at `x = n` -- exactly, not to a tolerance, because
    those two limits are the boundary of the parameter space and not a numerical result.
    """
    alpha = (1.0 - confidence) / 2.0
    low = 0.0 if x == 0 else _bisect(lambda p: (1.0 - binom_cdf(x - 1, n, p)) - alpha, 0.0, 1.0)
    high = 1.0 if x == n else _bisect(lambda p: binom_cdf(x, n, p) - alpha, 0.0, 1.0)
    return low, high


def one_sided_binomial_p(x, n, p0, flat):
    """The one-sided exact p-value for the verdict actually being claimed.

    A FLAT verdict claims `p <= p0`, so its evidence is the LOWER tail; a NOT-FLAT verdict claims
    `p > p0` and its evidence is the upper one.  Reading one for the other is the commonest way
    to make a marginal reading look decisive.
    """
    return binom_cdf(x, n, p0) if flat else 1.0 - binom_cdf(x - 1, n, p0)


def two_sided_binomial_p(x, n, p0):
    """Twice the smaller tail, capped at one."""
    return min(1.0, 2.0 * min(binom_cdf(x, n, p0), 1.0 - binom_cdf(x - 1, n, p0)))


# --------------------------------------------------------------------------------------------
# The corpus.
# --------------------------------------------------------------------------------------------

def result_documents(root=ROOT):
    """`{tag: document}` over the eighteen files, refusing rather than skipping a missing one."""
    documents = {}
    for tag in FILES:
        matches = sorted(glob.glob(os.path.join(root, "gpd/results/%s-*.json" % tag)))
        if not matches:
            raise SystemExit("no committed result file for %s under %s" % (tag, root))
        with open(matches[0]) as handle:
            documents[tag] = json.load(handle)
    return documents


def _leaves(node, path, parent, out):
    if isinstance(node, dict):
        for key, value in node.items():
            _leaves(value, path + "/" + key, node, out)
    elif isinstance(node, list):
        for index, value in enumerate(node):
            _leaves(value, path + "/%d" % index, parent, out)
    elif isinstance(node, float):
        out.append((path, node, parent))


def _records(node, path, out):
    if isinstance(node, dict):
        out.append((path, node))
        for key, value in node.items():
            _records(value, path + "/" + key, out)
    elif isinstance(node, list):
        for index, value in enumerate(node):
            _records(value, path + "/%d" % index, out)


def margin_census_of(documents):
    """`C-0221` section 5's predicate, verbatim, so a recount is against the same population.

    Every numeric leaf whose key ends `OverStroke` or contains `ishing`, in a JSON object that
    also carries at least one boolean, valued in `[0.09, 0.11]`.  The boolean test is on the
    PARENT, which is reading 2b: a diagnostic sitting beside a verdict is counted as one.
    """
    rows = []
    for tag in sorted(documents):
        found = []
        _leaves(documents[tag], "", None, found)
        for path, value, parent in found:
            leaf = path.rsplit("/", 1)[-1]
            if not 0.09 <= value <= 0.11:
                continue
            if not isinstance(parent, dict):
                continue
            if not any(isinstance(x, bool) for x in parent.values()):
                continue
            if not (leaf.endswith("OverStroke") or "ishing" in leaf):
                continue
            rows.append(Row(abs(value - TOLERANCE) / TOLERANCE, value, tag, path, leaf))
    rows.sort(key=lambda row: (row.relative, row.tag, row.path))
    return rows


def leaf_key_partition(rows):
    """`{leaf key: count}` over a census, so *"1 146 verdicts"* can be checked against *"leaves"*."""
    counted = collections.Counter(row.leaf for row in rows)
    return dict(counted)


def channel_counts(rows, channels):
    """`[(threshold, label, count)]` -- how many readings sit within each channel."""
    return [(threshold, label, sum(1 for row in rows if row.relative <= threshold))
            for threshold, label in channels]


def _flat_p90_booleans(record):
    return {key: value for key, value in record.items()
            if isinstance(value, bool) and key.lower().startswith("flat") and "p90" in key.lower()}


def identity_disagreements_of(documents):
    """Where a `flatAt*P90` boolean disagrees with `exceedance <= TOLERANCE`.

    The identity is `<=`, not `<`: `orderStatistic(sample, 0.90)` is `sorted[ceil(0.9 n) - 1]`, so
    at `n = 4 000` exactly 400 realisations above the tolerance still leave the 3 600th below it.
    """
    disagreements = []
    for tag in sorted(documents):
        found = []
        _records(documents[tag], "", found)
        for path, record in found:
            exceedance = record.get("exceedance")
            if not isinstance(exceedance, (int, float)) or isinstance(exceedance, bool):
                continue
            for key, value in sorted(_flat_p90_booleans(record).items()):
                if (exceedance <= TOLERANCE + 1e-12) != value:
                    disagreements.append((tag, path, key, value, exceedance))
    return disagreements


def realisations_of(exceedance, standard_error):
    """The realisation count BACKED OUT of `p̂` and `√(p̂(1 − p̂)/n)`, or `None`.

    A count is never assumed.  A record that does not state its standard error yields `None`, and
    the caller must resolve it against the study's own parameter block or refuse it.
    """
    if not isinstance(standard_error, (int, float)) or standard_error <= 0.0:
        return None
    if not 0.0 < exceedance < 1.0:
        return None
    return int(round(exceedance * (1.0 - exceedance) / standard_error ** 2))


def realisation_census(documents):
    """Every realisation count the corpus states, backed out record by record."""
    counts = []
    for tag in sorted(documents):
        found = []
        _records(documents[tag], "", found)
        for _, record in found:
            exceedance = record.get("exceedance")
            if not isinstance(exceedance, (int, float)) or isinstance(exceedance, bool):
                continue
            count = realisations_of(exceedance, record.get("exceedanceStandardError"))
            if count is not None:
                counts.append(count)
    return counts


def ensemble_records(documents, default_realisations=4000):
    """Every record carrying an `exceedance` and at least one `flatAt*P90` boolean."""
    out = []
    for tag in sorted(documents):
        found = []
        _records(documents[tag], "", found)
        for path, record in found:
            exceedance = record.get("exceedance")
            if not isinstance(exceedance, (int, float)) or isinstance(exceedance, bool):
                continue
            booleans = _flat_p90_booleans(record)
            if not booleans:
                continue
            stated = realisations_of(exceedance, record.get("exceedanceStandardError"))
            out.append({
                "tag": tag,
                "path": path,
                "exceedance": exceedance,
                "booleans": booleans,
                "realisationsStated": stated,
                "realisations": stated if stated is not None else default_realisations,
                "realisationsAssumed": stated is None,
            })
    return out


def unresolvable_verdicts(documents):
    """`flatAt*P90` booleans whose record carries NO exceedance -- population C.

    No resolution is derivable from the file for these.  They are a recorded refusal, never a
    withdrawal, and the `p90` beside each is carried so the refusal can be partitioned by how
    close it sits to the tolerance instead of being left as a bare count.
    """
    out = []
    for tag in sorted(documents):
        found = []
        _records(documents[tag], "", found)
        for path, record in found:
            if isinstance(record.get("exceedance"), (int, float)):
                continue
            booleans = _flat_p90_booleans(record)
            if not booleans:
                continue
            p90s = {key: value for key, value in record.items()
                    if isinstance(value, float) and "p90" in key.lower()}
            out.append({"tag": tag, "path": path, "booleans": booleans, "p90": p90s})
    return out


# --------------------------------------------------------------------------------------------
# The resolution.
# --------------------------------------------------------------------------------------------

def determinacy(x, n, confidence=0.95, p0=None):
    """`DETERMINED` iff the exact Clopper-Pearson interval on the exceedance EXCLUDES `p0`.

    Evaluated WITHOUT inverting the interval, which is exact and about four hundred times
    cheaper: `p0 >= low` iff `P(X >= x | p0) >= alpha` and `p0 <= high` iff
    `P(X <= x | p0) >= alpha`, both tails being monotone in `p`.  A census over twelve hundred
    records at three confidence levels is then two binomial tails per record rather than eight
    hundred bisection steps.
    """
    p0 = TOLERANCE if p0 is None else p0
    alpha = (1.0 - confidence) / 2.0
    lower_tail = binom_cdf(x, n, p0)
    upper_tail = 1.0 - binom_cdf(x - 1, n, p0)
    inside = lower_tail >= alpha and upper_tail >= alpha
    return "UNDETERMINED" if inside else "DETERMINED"


def resolution_band(n, confidence=0.95):
    """The inclusive band of exceedance COUNTS at which a verdict may not be quoted."""
    centre = int(round(TOLERANCE * n))
    low = centre
    while low > 0 and determinacy(low - 1, n, confidence) == "UNDETERMINED":
        low -= 1
    high = centre
    while high < n and determinacy(high + 1, n, confidence) == "UNDETERMINED":
        high += 1
    return low, high


def determinacy_census(records, confidence=0.95):
    """Positive and negative verdicts, split by whether their own ensemble can resolve them."""
    census = {"positive": 0, "positiveUndetermined": 0,
              "negative": 0, "negativeUndetermined": 0, "undetermined": []}
    for record in records:
        n = record["realisations"]
        x = int(round(record["exceedance"] * n))
        verdict = determinacy(x, n, confidence)
        for key, value in sorted(record["booleans"].items()):
            if value:
                census["positive"] += 1
                if verdict == "UNDETERMINED":
                    census["positiveUndetermined"] += 1
            else:
                census["negative"] += 1
                if verdict == "UNDETERMINED":
                    census["negativeUndetermined"] += 1
        if verdict == "UNDETERMINED":
            low, high = clopper_pearson(x, n, confidence)
            flat = any(record["booleans"].values())
            census["undetermined"].append({
                "tag": record["tag"], "path": record["path"],
                "exceedanceCount": x, "realisations": n,
                "clopperPearsonLow": low, "clopperPearsonHigh": high,
                "oneSidedBinomialP": one_sided_binomial_p(x, n, TOLERANCE, flat),
                "twoSidedBinomialP": two_sided_binomial_p(x, n, TOLERANCE),
                "readsFlat": flat,
            })
    census["undetermined"].sort(key=lambda row: (row["tag"], row["path"]))
    return census


def axis_kind(text):
    """The KIND of a convergence axis, so only the right kind enters a resolution."""
    lowered = (text or "").lower()
    for kind, needles in _AXIS_RULES:
        if any(needle in lowered for needle in needles):
            return kind
    return "UNCLASSIFIED"


def enters_a_resolution(kind):
    """Only a discretisation axis is a statement about the numerical model's own resolution."""
    return kind == "DISCRETISATION"


def convergence_axes(documents):
    """Every convergence record of the eighteen files, with its axis classified."""
    out = []
    for tag in sorted(documents):
        for record in documents[tag].get("convergence") or []:
            axis = record.get("axis") or ""
            departure = record.get("departure")
            if departure is None:
                departure = record.get("relativeDeparture")
            out.append({
                "tag": tag,
                "axis": axis,
                "kind": axis_kind(axis),
                "quantity": record.get("quantity") or "",
                "cell": str(record.get("cell") or record.get("setting") or ""),
                "departure": departure,
            })
    return out


def paired_wins(fraction_worse, realisations):
    """How many of the SAME realisations the subject wins -- a paired sign test's input."""
    return int(round((1.0 - fraction_worse) * realisations))


def paired_orderings(documents):
    """Every paired comparison the corpus emits, as an exact sign test on its own stream."""
    out = []
    for tag in sorted(documents):
        for record in documents[tag].get("paired") or []:
            worse = None
            for key in ("fractionTiedIsWorse", "fractionSubjectIsWorse", "fractionAbove"):
                if isinstance(record.get(key), (int, float)):
                    worse = record[key]
                    break
            realisations = record.get("realisations")
            if worse is None or not isinstance(realisations, int):
                continue
            wins = paired_wins(worse, realisations)
            out.append({
                "tag": tag,
                "comparison": record.get("comparison") or "",
                "pathCount": record.get("pathCount"),
                "distribution": record.get("distribution"),
                "realisations": realisations,
                "wins": wins,
                "signTestTwoSidedP": two_sided_binomial_p(min(wins, realisations - wins),
                                                          realisations, 0.5),
                "verdictMoved": record.get("verdictMoved"),
            })
    return out


def _worst_nominal_discretisation(document):
    """The worst DISCRETISATION departure a file emits on a NOMINAL quantity, or `None`.

    A per-`(file, quantity)` match.  It is deliberately NOT a per-file maximum over every axis:
    `T-279`'s worst is `0.018` on `C-0167`'s own convergence cell, which `C-0180` states in as many
    words is not the quantity its verdict rests on, and transferring it would be exactly the
    mistake reading 2a caught one scope up.  Nor may a SEARCH, PARAMETER or PENALTY axis stand in
    for a missing one -- there the honest answer is that the file cannot say.
    """
    worst = None
    for record in document.get("convergence") or []:
        if not enters_a_resolution(axis_kind(record.get("axis") or "")):
            continue
        quantity = (record.get("quantity") or "").lower()
        if "nominal" not in quantity and "no defect" not in quantity:
            continue
        departure = record.get("departure")
        if departure is None:
            departure = record.get("relativeDeparture")
        if isinstance(departure, float):
            worst = departure if worst is None else max(worst, departure)
    return worst


def nominal_population(documents):
    """Population B -- zero-defect readings, on the discretisation axis the row names.

    A reading is `UNDETERMINED` when its own distance from the tolerance, relative to its own
    value so it is commensurate with a `departure`, is inside its file's worst nominal
    discretisation departure; `NO-AXIS` where the file emits none.
    """
    out = []
    for tag in sorted(documents):
        worst = _worst_nominal_discretisation(documents[tag])
        found = []
        _records(documents[tag], "", found)
        for path, record in found:
            verdicts = {key: value for key, value in record.items()
                        if isinstance(value, bool) and key.lower().startswith("flat")
                        and "nominal" in key.lower()}
            if not verdicts:
                continue
            for leaf, value in sorted(record.items()):
                if not isinstance(value, float) or isinstance(value, bool):
                    continue
                if "ominal" not in leaf:
                    continue
                if not (leaf.endswith("OverStroke") or "ishing" in leaf):
                    continue
                if not 0.09 <= value <= 0.11:
                    continue
                relative = abs(value - TOLERANCE) / value
                out.append({
                    "tag": tag, "path": path, "leaf": leaf, "value": value,
                    "relativeToItsOwnValue": relative,
                    "worstNominalDiscretisationDeparture": worst,
                    "determinacy": ("NO-AXIS" if worst is None else
                                    "UNDETERMINED" if relative <= worst else "DETERMINED"),
                    "readsFlat": any(verdicts.values()),
                })
    return out
# The self-test block for tools/T-327-flatness-resolution.py, written FIRST.
def self_test(root=ROOT):
    failures = []
    executed = [0]

    def ok(name, condition):
        executed[0] += 1
        print(("ok   " if condition else "FAIL ") + name)
        if not condition:
            failures.append(name)

    # --- the binomial arithmetic, against values that can be checked by hand -----------------
    ok("the binomial cdf is exactly 1 at k = n", binom_cdf(10, 10, 0.3) == 1.0)
    ok("the binomial cdf is exactly 0 below k = 0", binom_cdf(-1, 10, 0.3) == 0.0)
    ok("the binomial cdf of a fair coin is 1/2 at the median of an odd count",
       abs(binom_cdf(2, 5, 0.5) - 0.5) < 1e-12)
    ok("the binomial cdf matches an exact sum on a small case",
       abs(binom_cdf(3, 7, 0.25) - sum(
           math.comb(7, i) * 0.25 ** i * 0.75 ** (7 - i) for i in range(4))) < 1e-12)
    ok("the binomial cdf matches an EXACT RATIONAL sum, so the continued fraction is not a "
       "third-party package's answer taken on trust",
       all(abs(binom_cdf(k, 200, 0.10)
               - float(sum(Fraction(math.comb(200, i)) * Fraction(1, 10) ** i
                           * Fraction(9, 10) ** (200 - i) for i in range(k + 1)))) < 1e-12
           for k in (0, 5, 15, 20, 25, 40, 200)))
    ok("the Clopper-Pearson upper limit at zero successes is 1 - (alpha)^(1/n)",
       abs(clopper_pearson(0, 10, 0.95)[1] - (1.0 - 0.025 ** (1.0 / 10))) < 1e-9)
    ok("the Clopper-Pearson lower limit at zero successes is exactly 0",
       clopper_pearson(0, 10, 0.95)[0] == 0.0)
    ok("the Clopper-Pearson upper limit at n successes is exactly 1",
       clopper_pearson(10, 10, 0.95)[1] == 1.0)
    ok("a Clopper-Pearson interval brackets its own point estimate",
       clopper_pearson(392, 4000, 0.95)[0] < 0.098 < clopper_pearson(392, 4000, 0.95)[1])
    ok("a Clopper-Pearson interval WIDENS with the confidence level",
       (clopper_pearson(392, 4000, 0.99)[1] - clopper_pearson(392, 4000, 0.99)[0])
       > (clopper_pearson(392, 4000, 0.90)[1] - clopper_pearson(392, 4000, 0.90)[0]))
    ok("the one-sided p for a FLAT reading is the LOWER tail",
       abs(one_sided_binomial_p(392, 4000, 0.10, True) - binom_cdf(392, 4000, 0.10)) < 1e-15)
    ok("the one-sided p for a NOT-FLAT reading is the UPPER tail",
       abs(one_sided_binomial_p(411, 4000, 0.10, False)
           - (1.0 - binom_cdf(410, 4000, 0.10))) < 1e-15)
    ok("a two-sided p is capped at one", two_sided_binomial_p(400, 4000, 0.10) <= 1.0)

    # --- the census predicate, on fixtures rather than on the live corpus ---------------------
    counted = {"cells": [{"p90OverStroke": 0.0995, "flatAtP90": True}]}
    ok("a reading in range, matching the key, in a record carrying a boolean, is counted",
       [r.path for r in margin_census_of({"X": counted})] == ["/cells/0/p90OverStroke"])
    ok("a reading OUTSIDE [0.09, 0.11] is not counted",
       margin_census_of({"X": {"cells": [{"p90OverStroke": 0.5, "flatAtP90": True}]}}) == [])
    ok("a record carrying NO boolean is not counted",
       margin_census_of({"X": {"cells": [{"p90OverStroke": 0.0995}]}}) == [])
    ok("a key matching neither OverStroke nor ishing is not counted",
       margin_census_of({"X": {"cells": [{"p90Nm": 0.0995, "flatAtP90": True}]}}) == [])
    ok("a key CONTAINING ishing is counted, as C-0221's predicate says",
       len(margin_census_of({"X": {"c": [{"worstCornerDishing": 0.0995, "f": True}]}})) == 1)
    ok("the boolean test is on the PARENT record, so a diagnostic beside a verdict is counted",
       sorted(r.leaf for r in margin_census_of(
           {"X": {"c": [{"medianOverStroke": 0.0995, "p90OverStroke": 0.1005,
                         "flatAtP90": False}]}}))
       == ["medianOverStroke", "p90OverStroke"])
    ok("the census is sorted by distance from the tolerance, tightest first",
       [round(r.relative, 4) for r in margin_census_of(
           {"X": {"c": [{"aOverStroke": 0.105, "bOverStroke": 0.101, "f": True}]}})]
       == [0.01, 0.05])
    ok("the leaf-key partition sums to the census total",
       sum(leaf_key_partition(margin_census_of(
           {"X": {"c": [{"aOverStroke": 0.105, "bOverStroke": 0.101, "f": True}]}})).values()) == 2)

    # --- the identity the whole instrument rests on -------------------------------------------
    agreeing = {"X": {"c": [{"exceedance": 0.098, "flatAtP90": True}]}}
    disagreeing = {"X": {"c": [{"exceedance": 0.101, "flatAtP90": True}]}}
    ok("flatAt*P90 <=> exceedance <= 0.10 agrees where the corpus is consistent",
       identity_disagreements_of(agreeing) == [])
    ok("a boolean disagreeing with its own exceedance is reported, with its path",
       [d[1] for d in identity_disagreements_of(disagreeing)] == ["/c/0"])
    ok("the identity is <=, not <: an exceedance of exactly 0.10 reads FLAT",
       identity_disagreements_of({"X": {"c": [{"exceedance": 0.10, "flatAtP90": True}]}}) == [])
    ok("the identity is <=, not >=: an exceedance of exactly 0.10 with flat FALSE is a defect",
       len(identity_disagreements_of(
           {"X": {"c": [{"exceedance": 0.10, "flatAtP90": False}]}})) == 1)
    ok("a NON-VERDICT boolean carrying p90 in its name is not tested either -- constructed, "
       "because the live corpus alone held this rule open (C-0195)",
       identity_disagreements_of(
           {"X": {"c": [{"exceedance": 0.098, "flatAtP90": True,
                         "beatsUncoupledAtP90": False}]}}) == [])
    ok("a boolean that is not a flat-at-p90 verdict is not tested by the identity",
       identity_disagreements_of(
           {"X": {"c": [{"exceedance": 0.101, "flatAtNominal": True}]}}) == [])

    # --- the realisation count is BACKED OUT, never assumed -----------------------------------
    ok("the realisation count is recovered from the exceedance and its standard error",
       realisations_of(0.098, 0.00470095735) == 4000)
    ok("a record with no standard error yields no count rather than a guessed one",
       realisations_of(0.098, None) is None)
    ok("a zero standard error yields no count rather than a division by zero",
       realisations_of(0.098, 0.0) is None)

    # --- the resolution itself -----------------------------------------------------------------
    ok("C-0180's tighter recovered cell is UNDETERMINED at 95 per cent",
       determinacy(392, 4000, 0.95) == "UNDETERMINED")
    ok("C-0180's other recovered cell is UNDETERMINED at 95 per cent",
       determinacy(398, 4000, 0.95) == "UNDETERMINED")
    ok("a reading far from the tolerance is DETERMINED",
       determinacy(100, 4000, 0.95) == "DETERMINED")
    ok("a reading far ABOVE the tolerance is DETERMINED too",
       determinacy(3000, 4000, 0.95) == "DETERMINED")
    ok("determinacy is non-increasing in the confidence level",
       not (determinacy(360, 4000, 0.90) == "UNDETERMINED"
            and determinacy(360, 4000, 0.99) == "DETERMINED"))
    ok("the interval is TWO-sided: alpha is half the complement, which one reading pins exactly",
       determinacy(365, 4000, 0.95) == "UNDETERMINED"
       and abs(binom_cdf(365, 4000, 0.10) - 0.033233) < 1e-5)
    ok("and at 90 per cent that same reading is DETERMINED, so the level is load-bearing",
       determinacy(365, 4000, 0.90) == "DETERMINED")
    ok("determinacy agrees with the inverted Clopper-Pearson interval it is a shortcut for",
       all((determinacy(x, 400, 0.95) == "UNDETERMINED")
           == (clopper_pearson(x, 400, 0.95)[0] <= TOLERANCE <= clopper_pearson(x, 400, 0.95)[1])
           for x in (0, 20, 34, 40, 46, 60, 400)))
    ok("the resolution in COUNTS is symmetric about the tolerance's own count",
       resolution_band(4000, 0.95)[0] < 400 < resolution_band(4000, 0.95)[1])
    ok("the resolution band widens with the confidence level",
       (resolution_band(4000, 0.99)[1] - resolution_band(4000, 0.99)[0])
       > (resolution_band(4000, 0.90)[1] - resolution_band(4000, 0.90)[0]))

    # --- which convergence axes may enter a RESOLUTION, and which may not ---------------------
    ok("a beam-subdivision axis is a discretisation axis",
       axis_kind("beam subdivisions 1 -> 2") == "DISCRETISATION")
    ok("a dishing sample grid is a discretisation axis",
       axis_kind("the dishing sample grid") == "DISCRETISATION")
    ok("a dishing grid the SEARCH runs on is still a DISCRETISATION axis",
       axis_kind("the dishing sample grid the SEARCH runs on, 41 against 81")
       == "DISCRETISATION")
    ok("a training-realisation axis is the SEARCH's variance, not the verdict's",
       axis_kind("the TRAINING realisations the search sees, 120 against 60") == "SEARCH")
    ok("a screening-realisation axis is a SEARCH axis too",
       axis_kind("the SCREENING realisations the exhaustive census ranks on") == "SEARCH")
    ok("a percentile-descent sweep count is a SEARCH axis",
       axis_kind("the percentile descent's sweeps, 2 against 4") == "SEARCH")
    ok("a composite-fraction axis is a physical BRACKET, not a departure",
       axis_kind("the composite fraction, 0.3 against 0.26") == "PARAMETER")
    ok("a forced-stagger axis is a PARAMETER axis",
       axis_kind("C-0141's forced inter-row stagger, 14 bp against 7") == "PARAMETER")
    ok("a link-penalty axis is a PENALTY, which is C-0100's binary",
       axis_kind("the link PENALTY, 1e4 -> 1e5 -> 1e6, on the eigenstrain field alone")
       == "PENALTY")
    ok("an axis the rules do not recognise is UNCLASSIFIED rather than guessed",
       axis_kind("the phase of the moon") == "UNCLASSIFIED")
    ok("only a DISCRETISATION axis may enter a resolution",
       [k for k in ("DISCRETISATION", "SEARCH", "PARAMETER", "PENALTY", "UNCLASSIFIED")
        if enters_a_resolution(k)] == ["DISCRETISATION"])

    # --- population B: the NOMINAL readings, on the row's own axis --------------------------
    nominal_doc = {"X": {"c": [{"nominalOverStroke": 0.09999, "flatAtNominal": True}],
                         "convergence": [{"axis": "beam subdivisions",
                                          "quantity": "the nominal dishing over stroke",
                                          "departure": 0.001}]}}
    ok("a nominal reading inside its own file's nominal discretisation departure is UNDETERMINED",
       [r["determinacy"] for r in nominal_population(nominal_doc)] == ["UNDETERMINED"])
    wide = json.loads(json.dumps(nominal_doc))
    wide["X"]["c"][0]["nominalOverStroke"] = 0.098
    ok("a nominal reading outside it is DETERMINED",
       [r["determinacy"] for r in nominal_population(wide)] == ["DETERMINED"])
    noaxis = json.loads(json.dumps(nominal_doc))
    noaxis["X"]["convergence"] = [{"axis": "beam subdivisions",
                                   "quantity": "the p90 of the dropout ensemble",
                                   "departure": 0.001}]
    ok("a nominal reading whose file has no NOMINAL discretisation axis is refused, not "
       "measured against the p90's",
       [r["determinacy"] for r in nominal_population(noaxis)] == ["NO-AXIS"])
    searchaxis = json.loads(json.dumps(nominal_doc))
    searchaxis["X"]["convergence"][0]["axis"] = "the TRAINING realisations the search sees"
    ok("a SEARCH axis may not stand in for a discretisation one",
       [r["determinacy"] for r in nominal_population(searchaxis)] == ["NO-AXIS"])

    # --- the paired ordering -------------------------------------------------------------------
    ok("a paired fraction-worse becomes a count of wins on the same stream",
       paired_wins(0.0365, 4000) == 3854)
    ok("a paired sign test on 3 854 of 4 000 is below any conventional level",
       two_sided_binomial_p(4000 - 3854, 4000, 0.5) < 1e-100)
    ok("a paired sign test at exactly half is not significant at all",
       abs(two_sided_binomial_p(2000, 4000, 0.5) - 1.0) < 1e-12)

    # --- the live corpus: an INVARIANT, not a defect count ------------------------------------
    # It is an invariant and not a defect count, so it does not expire when a defect is repaired
    # (`CLAUDE.md`: a self-test that reads a mutable artifact expires the moment the defect it
    # asserts is repaired).  The skip is VISIBLE and goes to stderr, because `--self-test` is
    # routinely redirected (`C-0195`), and it must cover an EMPTY results directory as well as a
    # missing one: `tools/T-295-mutation-input-census.py` empties the committed artifacts to ask
    # whether a mutation is held open by a fixture or by the corpus, and a crash there reads as a
    # refusal rather than as an answer.
    documents = (result_documents(root)
                 if glob.glob(os.path.join(root, "gpd", "results", "T-253-*.json")) else None)
    # The arms run only where the corpus is present IN SUBSTANCE, not merely in name.
    # `tools/T-295-mutation-input-census.py` builds its treatment tree by rewriting every
    # committed `.json` as `{}` -- deliberately, so a reader that raises does not make a whole
    # suite fail identically there -- so a file-existence test is not the right guard: what says
    # the corpus is readable is that it holds the records these arms are about.
    if documents is not None and ensemble_records(documents):
        ok("every committed record's flatAt*P90 agrees with its own exceedance",
           identity_disagreements_of(documents) == [])
        ok("every committed record that states a realisation count states 4 000",
           set(realisation_census(documents)) == {4000})
        ok("the eighteen files C-0221 section 5 censused are all present",
           len(documents) == len(FILES))
    else:
        sys.stderr.write(
            "# SKIPPED the 3 live-corpus arms: no readable gpd/results under %s\n" % root)

    print("# %d self-test(s), %d failure(s)" % (executed[0], len(failures)))
    return not failures


def report(root=ROOT, confidence=0.95):
    documents = result_documents(root)
    rows = margin_census_of(documents)
    print("# T-327 -- the resolution of the flatness census, at %s" % root)
    print()
    print("2a  the transferred threshold, on the axis it is entered on")
    for threshold, label, count in channel_counts(rows, PUBLISHED_CHANNELS):
        print("      within %-10.5g  %-64s %5d" % (threshold, label, count))
    threshold, label = COMMENSURATE_CHANNEL
    print("      within %-10.5g  %-64s %5d"
          % (threshold, label, sum(1 for row in rows if row.relative <= threshold)))
    print()
    print("2b  the census counts LEAVES: %d over %d distinct keys"
          % (len(rows), len(leaf_key_partition(rows))))
    partition = leaf_key_partition(rows)
    diagnostics = sum(partition.get(key, 0) for key in DIAGNOSTIC_LEAVES)
    print("      of which %d carry no boolean of their own: %s"
          % (diagnostics, ", ".join("%s %d" % (k, partition.get(k, 0))
                                    for k in DIAGNOSTIC_LEAVES)))
    print()
    records = ensemble_records(documents)
    disagreements = identity_disagreements_of(documents)
    booleans = sum(len(r["booleans"]) for r in records)
    print("2c  flatAt*P90 <=> exceedance <= %.2f: %d booleans over %d records, %d disagreeing"
          % (TOLERANCE, booleans, len(records), len(disagreements)))
    census = determinacy_census(records, confidence)
    print("      at %.0f%% Clopper-Pearson: %d of %d POSITIVE verdicts undetermined, "
          "%d of %d negative"
          % (confidence * 100, census["positiveUndetermined"], census["positive"],
             census["negativeUndetermined"], census["negative"]))
    for row in census["undetermined"]:
        print("        %-7s %-24s X=%4d/%d  one-sided p=%.3f  flat=%s"
              % (row["tag"], row["path"], row["exceedanceCount"], row["realisations"],
                 row["oneSidedBinomialP"], row["readsFlat"]))
    unresolvable = unresolvable_verdicts(documents)
    positive = sum(1 for r in unresolvable for v in r["booleans"].values() if v)
    print("      population C -- no exceedance emitted: %d booleans, %d of them positive"
          % (sum(len(r["booleans"]) for r in unresolvable), positive))
    nominal = nominal_population(documents)
    noaxis = sum(1 for r in nominal if r["determinacy"] == "NO-AXIS")
    undetermined = sum(1 for r in nominal if r["determinacy"] == "UNDETERMINED")
    print("      population B -- nominal readings on the row's OWN axis: %d in range, "
          "%d with no nominal discretisation axis in their own file, %d undetermined"
          % (len(nominal), noaxis, undetermined))
    print()
    print()
    print("2d  the paired orderings the corpus already emits")
    for row in paired_orderings(documents):
        if row["verdictMoved"]:
            print("        %-7s %s paths: %d of %d wins, sign-test p = %.3e"
                  % (row["tag"], row["pathCount"], row["wins"], row["realisations"],
                     row["signTestTwoSidedP"]))
    print()
    kinds = collections.Counter(a["kind"] for a in convergence_axes(documents))
    print("    convergence axes by kind: %s" % dict(kinds))
    return 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true",
                        help="run the self-tests and exit non-zero on a failure")
    parser.add_argument("--report", action="store_true",
                        help="print the census; writes nothing")
    parser.add_argument("--confidence", type=float, default=0.95,
                        help="the two-sided confidence level the resolution is stated at")
    parser.add_argument("--root", default=ROOT, help="the tree to read")
    args = parser.parse_args(argv[1:])
    if args.self_test:
        return 0 if self_test(args.root) else 1
    if args.report:
        return report(args.root, args.confidence)
    parser.print_help()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
