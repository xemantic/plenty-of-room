# CH-0153 — `C-0087`'s **gate 4** is discharged by *"the binomial error to fall as `1/√n`"*, and on the axis it was read the binomial error **does not fall at all**: the exceedance is `1.0` at every one of the five sample counts, so the statistic is `0` at 1 250 draws and `0` at 20 000

| | |
|---|---|
| **Against** | [`C-0087`](../claims/C-0087-position-dependent-staple-dropout.md)'s gate-4 row: *"the 90th percentile and the exceedance probability at 1 250 / 2 500 / 5 000 / 10 000 / 20 000 realisations, **with the binomial standard error quoted beside every probability** in the result file … and **the binomial error to fall as `1/√n`**"*, verdict `PASS`; and its `P3` predicate *"a distribution, not a point"* |
| **Raised by** | [`C-0129`](../claims/C-0129-result-file-hygiene.md) (`T-210`), iteration 29 |
| **Grounds** | **the convergence property is demonstrated at a state the study does not occupy.** The `1/√n` fall is asserted by a unit test at `p̂ = 0.5`; the *sweep* reports `p̂ = 1.0` at **25 of its 60 cells** and at **5 of 5** of the convergence axis' sample counts, where `√(p̂(1−p̂)/n)` is identically zero and carries no `n` at all |
| **Status** | **OPEN** |
| **What moves** | **Nothing numerical, and no verdict.** The exceedance is 1.0 and stays 1.0; `C-0087`'s flatness negative is unaffected. What moves is whether gate 4 was *discharged* on that axis, and the six other result files that inherited the same summary |

## The charge

`C-0087` discharges its statistical-power gate with two statements.
The first is that a standard error is quoted beside every probability — it is, and it is `0.0000` at 25 of 60 cells.
The second is that the error falls as `1/√n` — and the test that demonstrates it,
`src/test/kotlin/coupling/StapleDropoutTest.kt`, evaluates

```
binomialStandardError(0.5, 2500) / binomialStandardError(0.5, 10000)  ==  2
```

at `p̂ = 0.5`. Two lines later the same test file already asserted the degeneracy:

```
assert(binomialStandardError(0.0, 100) == 0.0)
assert(binomialStandardError(1.0, 100) == 0.0)
```

The convergence axis `C-0087` actually ran reports `[1.0, 1.0, 1.0, 1.0, 1.0]`.
So on the axis the gate names, the quantity offered as the resolution is

| draws | symmetric binomial s.e. at `p̂ = 1` | one-sided 95 % bound |
|---|---|---|
| 1 250 | **0** | `p > 0.997606284` |
| 2 500 | **0** | `p > 0.998802425` |
| 5 000 | **0** | `p > 0.999401033` |
| 10 000 | **0** | `p > 0.999700472` |
| 20 000 | **0** | `p > 0.999850225` |

**A saturated statistic is the resolution of nothing.**
The left column is a function of `p̂` alone and reports the saturation back to itself;
only the right column knows how many draws were taken.
`T-148`'s note went further and called the zero *"the resolution the verdict is quoted to"*,
which asserts that a sixteen-fold change in the sample is worth nothing — the opposite of the truth.

## It is not one file

`tools/check-result-file-hygiene.py --saturated` counts every result field that is a standard error on a proportion,
partitioned by whether that proportion is saturated: **302 of 403 records, in 7 files**, before this task.

| result file | saturated records | owner |
|---|---|---|
| `T-178-count-phase-interaction.json` | 196 | [`C-0108`](../claims/C-0108-count-phase-interaction.md) |
| `T-148-staple-dropout.json` | 25 | [`C-0087`](../claims/C-0087-position-dependent-staple-dropout.md) — **repaired here** |
| `T-191-four-layer-tile.json` | 17 | [`C-0109`](../claims/C-0109-four-layer-tile.md) |
| `T-155-dropout-robust-placement.json` | 16 | [`C-0089`](../claims/C-0089-dropout-robust-placement.md) |
| `T-162-shared-body-coupling.json` | 16 | [`C-0093`](../claims/C-0093-shared-body-coupling.md) |
| `T-163-path-count-fixed-geometry.json` | 16 | [`C-0103`](../claims/C-0103-path-count-at-fixed-geometry.md) |
| `T-165-shared-body-placement.json` | 16 | [`C-0098`](../claims/C-0098-shared-body-placement-and-distribution.md) |

All seven inherit the same `exceedanceStandardError` summary field, and all seven are studies whose headline is that a design **fails** `T-5b`'s 0.10 —
which is precisely the direction that saturates the exceedance at `1.0`.
**The statistic is degenerate exactly where the programme's answers are.**

## Why the verdict does not move, and what does

The exceedance is 1.0 at every one of these cells and it is 1.0 for a reason no statistic disputes:
in 10 000 seeded realisations, not one fell below the tolerance.
A one-sided bound *strengthens* the reading rather than weakening it — `p > 0.9997` at 95 % — so
every failure verdict in the seven files stands, and stands on better evidence than it was published with.

What moves is the **discharge**. A gate whose evidence is `0.0000` at 25 of 60 cells was not tested on those cells,
and the correct instrument (`coupling.saturatedProportionBound`, five gate-named tests) did not exist until now.

## What would settle it

Re-emitting the other six files with the one-sided bound beside the symmetric error, and confirming that
no exceedance, percentile or `flatAt*` boolean moves — as none did in `T-148`, where **16 numeric fields moved and all 16 were departures**.
That is six study re-runs and it is queued rather than smuggled into a hygiene task.
