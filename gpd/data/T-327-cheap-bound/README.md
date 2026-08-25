# `T-327` — the cheap bound, retained

[`resolution-bound.py`](resolution-bound.py) is the prototype every number in
[`gpd/tasks/T-327-the-resolution-of-the-flatness-census.md`](../../tasks/T-327-the-resolution-of-the-flatness-census.md) §2 came from,
retained so the Plan stays checkable without a JVM.
[`resolution-bound.out`](resolution-bound.out) is its output at the commit this task was formulated at.

It reads the eighteen committed result files and **writes nothing**.
It takes **no arguments** and refuses any (`CH-0268`).
It needs no third-party package: the binomial tail is the regularised incomplete beta by continued fraction, so the exact Clopper-Pearson interval is available with the standard library alone.
It runs in about a second:

```
python3 gpd/data/T-327-cheap-bound/resolution-bound.py
```

Four readings, all before any code was written and none of them a solve:

| | what | reading |
|---|---|---|
| **2a** | `C-0221` §5's transferred threshold, entered on the census's own `rel` axis | **`4.57e−3` should be `4.57e−4`**, and the recount is **`2`** where §5 published **`99`**. Visible with no code at all: as published, the census places the flip margin at `0.93` of the convergence departure where `C-0180`'s own sentence places it at **`9.3`** |
| **2b** | is the `1 146` a count of verdicts or of leaves? | **leaves.** The boolean test is on the parent record, so `366` of them are `medianOverStroke`, `worstSingleRemovalOverStroke`, `uncoupledDishingOverStroke`, `p95OverStroke` and `worstSinglePathRemovalOverStroke`, on none of which any boolean is written |
| **2c** | what is `flatAtP90` a function of? | **the exceedance, exactly.** `flatAt*P90 ⟺ exceedance ≤ 0.10` at **`1 440` of `1 440`** records that carry both, `0` disagreeing. So the verdict is a binomial statement at `n = 4 000` and its resolution is `√(0.1 × 0.9/4000) = 4.743416e−3`, i.e. `18.97` realisations. At the exact two-sided `95 %` Clopper-Pearson interval, **`7` of the `19`** booleans reading *flat* are `UNDETERMINED` and **`1` of the `1 421`** reading *not flat* is |
| **2d** | does the **ordering** survive where the **level** does not? | **yes.** `C-0180`'s two recovering comparisons are `3 854 / 4 000` and `3 478 / 4 000` paired realisations, sign-test `p` below the double-precision floor, where the levels are `392 / 400` and `398 / 400` at one-sided binomial `p = 0.349` and `p = 0.471` |

**A note on why the density route is not the headline.**
`SE(p90) = √(p(1−p)/n)/f` needs the tail density, estimable from the emitted `p90` and `p95` as `0.05/(p95 − p90)`.
It gives `1.577e−3` of the stroke at `C-0180`'s tighter cell, `3.7×` its own margin — the same conclusion.
But that is a **secant on a right-skewed tail**, so it under-estimates `f` and over-states `SE`: conservative in the *alarming* direction, which is the wrong direction for an alarm.
The binomial route above needs no density at all, and it is what §2c and the claim rest on.

**A note on the two normalisations.**
The census's axis is `|v − 0.10| / 0.10`; every `convergence` record in the corpus emits `|fine − coarse| / coarse`.
For a reading in `[0.09, 0.11]` these agree to within `v/0.10`, and mixing them with a *stroke-fraction* distance costs exactly the factor of ten reading 2a reports.
