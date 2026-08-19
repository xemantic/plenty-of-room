# CH-0193 — the departure checker's `wide` line is documented as *"a ceiling on the class"* and the class **exceeds it**: it is keyed on the substring `departure`, so **47 of the 54** fields `T-225` found are outside it, and so are all **21** it classified out

| | |
|---|---|
| **Against** | [`C-0129`](../claims/C-0129-result-file-hygiene.md)'s `wide` census and its restatement in [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) and [`C-0138`](../claims/C-0138-departure-rule-scope.md) §6 — *"`wide` (any leaf key containing 'departure'; a ceiling on the class, NOT gated)"*, and the docstring of `check_departures` in [`tools/check-result-file-hygiene.py`](../../tools/check-result-file-hygiene.py): *"It is a ceiling on the class, not a defect count"* |
| **Raised by** | [`C-0150`](../claims/C-0150-departure-spelling-set-and-the-wall-clock.md) (`T-225`), iteration 36 |
| **Grounds** | **arithmetic over the committed corpus, no solve.** The class is *"a residual between two refinements of one solve"*; the `wide` predicate is *"a leaf key whose name contains the substring `departure`"*. Of the **54** fields `T-225` gates, **7** contain that substring and **47** do not — `firstIntegralRelativeSpread`, `firstIntegralCoreSpread`, `centrelineRouteSpread`, `relativeError`, `relativeSpread`, `relativeMovement`. Of the **21** fields `T-225` classifies **out**, **0** contain it. A ceiling the class exceeds by **68 of 75** is not a ceiling |
| **Status** | **UPHELD, and repaired by construction rather than by widening `wide`** — the ceiling is now [`tools/T-225-census.py`](../../tools/T-225-census.py), which searches for the **shape** and requires every candidate name to be classified |
| **What moves** | **No physical quantity, no verdict, and no number any of the three claims states.** What moves is what a reader may conclude from a clean `scope` line — three claims describe `wide` as the bound that makes the gate's cleanliness meaningful, and it bounds a *substring*, not a class |

## The charge

`C-0083`'s rule, quoted in all three claims, is that **a gate which cannot come clean is not a gate**,
and that publishing the residue is what stops a narrowed predicate becoming a claim of cleanliness.
The residue here is the `wide` line. It is the only thing standing between

```
scope (the same predicate — since T-214 the gate IS the rule): 0 field(s) in 0 file(s)
```

and a reader concluding that the corpus is clean under the rule.

But `wide` is not a superset of the rule. It is a superset of **one spelling family**.
It over-counts in one direction — deliberately, and the claims say so: it includes `departureRatio` and
`plateDeparture`, which are ratios between two *models*. What no claim says is that it **under-counts in the
other**, and by far more:

| set | fields | contain the substring `departure` |
|---|---|---|
| gated by `T-225`'s widened predicate | 54 | **7** (`T-60`'s `multiplierDeparture`, `gradientDeparture`) |
| classified **out** by `T-225` (a `log₁₀`, an order, a length, an absolute residual) | 21 | **0** |
| **the class, as `CH-0169` states it** | **75** | **7** |

So at the moment `C-0138` published `scope: 0 field(s) in 0 file(s)` beside
`wide: 945 field(s) in 28 file(s)`, the honest count outside the gate was **75 fields in 8 files**,
and **68 of them were in neither number**.

## Why this matters more than the count

Because the `wide` line is the one place a defect of this class is **supposed** to become visible before
anybody goes looking, and it made two of `T-225`'s eight names visible while hiding six.
It is also the reason [`CH-0192`](CH-0192-the-census-that-measured-a-stopped-census-stopped-too.md) is a *near miss* rather than a discovery:
`T-60`'s two fields were inside the printed `945` on every run for four iterations, in a line whose whole
purpose is to be differenced against the gate, and the difference was never taken.

**A ceiling is only a ceiling with respect to a stated class**, and a substring is not a class.
The repair is not to widen the substring — that would have found the two `*Departure` names and none of the
other six — but to search for the **shape** and demand a **classification**: `tools/T-225-census.py --check`
exits 1 on any candidate name the corpus contains that is in neither the gated set nor the excluded set.
The `wide` line is retained unchanged, and re-described as what it is: a bound on **one spelling family**,
kept because it is the census `C-0129` published and a reader who watched it is entitled to keep watching it.
