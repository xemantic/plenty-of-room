# CH-0291 — **`C-0223` §4b's *"what can be said without the datum is a BOUND in the safe direction: a `p90` far below the tolerance implies an exceedance far below it"* is not a bound. The only exact implication of `p90 < 0.10` is `x ≤ 400`, which IS the boolean — and the corpus offers `5` donor records below `p90 = 0.09` with which to calibrate the `48` population-C readings that sit there**

**Against** [`C-0223`](../claims/C-0223-the-resolution-of-the-flatness-census.md) (`T-327`) §4b, one sentence.
**From** [`C-0225`](../claims/C-0225-the-exceedance-beside-every-verdict.md) (`T-337`) §2.
**Kind** — a **modality** defect. The sentence's *conclusion* is right, its *direction* is right, and `T-337` measured both; what is wrong is the word **bound**, and the correction is a number the challenged claim could not have had.

---

## 1. The statement

`C-0223` §4b, having refused population C, offers what can still be said about it:

> **They are not withdrawn and they are not endorsed.** What can be said without the datum is a bound in the safe direction: a `p90` far below the tolerance implies an exceedance far below it, so the readings at risk are the marginal ones.

## 2. It is not an implication, by the order statistics' own definition

`exceedance = #{s > τ} / n` and `p90 = sorted[⌈0.9n⌉ − 1]`, so at `n = 4 000` a `p90` **pins the lower `3 600` order statistics and constrains the top `400` not at all**. `sorted[3599] = 0.03` is arithmetically compatible with every one of the remaining `400` realisations exceeding `0.10`, i.e. with `x = 400` and an exceedance of exactly `0.10`.

So the complete exact content of `p90 < τ` is **`x ≤ 400`**, which is `C-0223` §1c's own identity read the other way — it is the **boolean**, and it is what population C already carries. There is no residual bound to extract; a `p90` of `0.03` and a `p90` of `0.0999` are, as bounds, the same statement.

## 3. And the empirical transfer that would make it a prior cannot be calibrated at the low end

Measured over the `928` committed records that carry **both** a `p90OverStroke` and an exceedance (`gpd/data/T-337-cheap-bound/cheap-bound.py`):

| donor band | donors | inside `C-0223`'s undetermined band `[0.09075, 0.1095]` | largest exceedance |
|---|---|---|---|
| `p90 ∈ [0.0975, 0.10)` | `7` | **`7`** | `0.0995` |
| `p90 ∈ [0.0900, 0.0975)` | `7` | `0` | `0.0853` |
| `p90 < 0.0900` | **`5`** | `0` | `0.0535` |

**`48` of the `87` population-C positives sit below `p90 = 0.09`, and the corpus offers `5` donors there.** `n = 5` is not a calibration for `48` readings, and `T-337` declines to offer one. What the table *does* support, at the top of the range, is a sharp **prior** — and that is what the sentence should say.

## 4. What survives, which is most of it

- **The direction is right and is now measured.** `25` of the `87` sit at `p90 ≥ 0.0975`, and every donor in that band is `UNDETERMINED`. *"The readings at risk are the marginal ones"* is upheld.
- **The refusal is right.** `C-0223` refused population C rather than estimating it, and §9 states *"Population C is refused, not estimated"* — which is the correct modality, one sentence away from the one challenged.
- **Nothing downstream carried the word.** Grepped: the sentence is quoted in no claim, no deliverable and no queue row, so this is a correction at its source and not a sweep.

## 5. The remedy

One word and one number: *"a bound in the safe direction"* becomes *"a distributional **prior**, and not a bound — the only exact implication of `p90 < τ` is the boolean itself"*, with `T-337`'s donor table as the calibration it rests on and the `5`-donor refusal beside it.

**Status** — **RAISED**, iteration 55.
