# CH-0277 — A RATIO MEASURED ON ONE LATTICE AND APPLIED TO ANOTHER GOT THE VERDICT RIGHT AND THE LEVEL WRONG, AND ITS MISSES ARE ONE-SIGNED

| | |
|---|---|
| **Against** | [`T-322`](../tasks/T-322-route-b-coupled-on-its-own-stations.md)'s own cheap bound 2 — the `searchedP90 / uncoupled` ratio band taken from [`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md)'s 32 cells and applied to [`C-0211`](../claims/C-0211-the-uniform-raster-at-the-resolved-link.md)'s committed uncoupled readings — and against any future use of a `116 bp` ratio as a route-B estimate |
| **Not against** | `C-0212` or `C-0211`, whose numbers are exact on their own tiles; nor the bound's **verdict**, which was right on both rules |
| **Raised by** | [`C-0215`](../claims/C-0215-route-b-coupled-on-its-own-stations.md) / [`T-322`](../tasks/T-322-route-b-coupled-on-its-own-stations.md) §4, by its own declared falsifier `F20`, which FIRED |
| **Grounds** | `CLAUDE.md`'s *decompose a ratio before predicting it*, and its own *a boundary-layer measurement does not transfer between two tile sizes* — reached through a **cheap bound** rather than through a claim |
| **Status** | **RAISED, and it is a measurement rather than a repair.** The bound was declared a PREDICTION and not a theorem before the run, with `F20` attached to it; this is what the falsifier bought |

---

## What was predicted, and what was measured

`C-0212`'s own 32 cells at the headline composite fraction carry
`searchedP90 / uncoupledDishing` over **`1.4438156`–`2.7106587×`** and
`bestTransferredP90 / uncoupledDishing` over **`2.2359`–`3.5094×`**.
Applied to `C-0211`'s committed uncoupled route-B readings:

| row | predicted **transferred** | predicted **searched** | measured tightest searched |
|---|---|---|---|
| `92 bp` | `0.116616839`–`0.183039806` | `0.075304442`–`0.141378609` | **`0.068793971`** |
| `98 bp` | `0.129006232`–`0.202485986` | `0.083304799`–`0.156398696` | **`0.069137604`** |
| `106 bp` | `0.117133656`–`0.183850993` | `0.075638173`–`0.142005164` | **`0.075715489`** |

**The verdict transferred.** The band said *excludes flat* on a transferred rule and the measured
census is `0 of 48`; it said *straddles* on a searched rule and `27 of 48` clear.

**The level did not, and the misses are one-signed.** Of the 48 searched readings, **8 fall below
the band and 0 above**; of the 48 transferred readings, **14 fall below and 1 above**.

## Why that is a finding and not a defect

A ratio carried between two lattices differing in bond census, station ladder, tile width, interior
pressure, dropout field and turn topology has no reason to be tight, and the bound said so in the
task file before the run. What the miss **measures** is the direction: **route B's own tile does
better than the `116 bp` block predicts**, at every one of the 23 misses and at none of the
reverse. That is the quantitative form of *the stations belong to a different tile*, and it is
the second half of `CH-0276`.

## What is asked

That a `116 bp` ratio not be used as a route-B **level** — only as a **verdict** indicator, where
it is measured to work — and that any future transfer of this kind carry a falsifier on its own
band, as `F20` did here. A cheap bound whose miss is unmeasured is a bound whose reliability is
unmeasured.
