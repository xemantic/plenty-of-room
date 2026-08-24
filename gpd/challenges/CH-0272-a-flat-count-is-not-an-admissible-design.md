# CH-0272 — A FLAT COUNT IS NOT A COUNT OF ADMISSIBLE DESIGNS, AND A VERDICT BLOCK THAT REPORTS THREE THRESHOLDS SEPARATELY REPORTS NO CONJUNCTION AT ALL

| | |
|---|---|
| **Against** | the `verdict` and `findings` blocks of [`gpd/results/T-316-a-searched-distribution-at-the-resolved-link.json`](../results/T-316-a-searched-distribution-at-the-resolved-link.json), and any downstream reading of its headline *"does a SEARCHED distribution clear `T-5b` at the 90th percentile = YES at 22 of 32 cells"* |
| **Raised by** | [`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md) / [`T-316`](../tasks/T-316-a-searched-distribution-at-the-resolved-link.md) §3 and §5 |
| **Grounds** | methodological — `CLAUDE.md`'s *count the INDEPENDENT margins, not the rows* and *always run the uncoupled tile as the reference*, on a **verdict block** rather than on a design |
| **Status** | **RAISED, and REPAIRED IN THE CLAIM RATHER THAN IN THE FILE.** No number of the result file is disputed; every count below is derived from that same file's own `cells[*]` records |

---

## The defect

The study emits **three** per-cell booleans and its `verdict` block reports each of the underlying
quantities on its own, in three separate rows:

> *"does a SEARCHED distribution clear `T-5b` at the 90th percentile = YES at 22 of 32 cells"*
> *"the stiffness ratio the argmin demands at the tightest cell = 191.010656 … OUTSIDE it"*

and `F14`'s note carries *"19 of 22 flat cells demand more than 3.33333333 pN/nm on one path"*.

**Not one sentence of the file states a conjunction, and the file emits the flags to compute
them.** Over the 32 `cells[*]` records:

| | count |
|---|---|
| `flatAtP90` | **22** |
| `ratioInsideBuildableWindow` | **12** |
| `peakInsideUnzipCeiling` | **3** |
| `ratioInsideBuildableWindow` **and** `peakInsideUnzipCeiling` | **0** |
| all three | **0** |
| `beatsUncoupledAtP90` | **0** |

A reader of the verdict block takes away *22 flat designs*. The number of cells the file's own
flags call flat **and** admissible on both per-path axes is **zero**, and the number that beat the
uncoupled tile is **zero** — and the second of those is emitted **per cell** and appears in no
summary of any kind.

## Why it matters, and what it is NOT

**It is not an argument for removing the coupling.** `C-0017`'s mandate is a **placement and
stability** requirement; the stroke and the lateral confinement want ties for reasons that are not
flatness. What the `0 of 32` establishes is narrower and exact: **on this lattice, at these
placements, under `C-0087`'s measured dropout, flatness is not what the coupling buys.** And the
scoping runs the other way at zero defects, where **16 of 32** searched cells *do* beat the
uncoupled tile (best `0.0298112409` against `0.0448134881`), so the cost is the **dropout** and not
the coupling — which is `C-0167`'s own finding reproduced with the distribution freed.

**And the empty conjunction must not be told as an anti-correlation.** The Pearson correlation of
`log(searchedRatio)` against `log(searchedPeakStiffness)` over the same 32 records is
**`−0.0949781`**. What links the two is one-sided and it is a theorem: with `n` paths summing to
`S`, `peak ≤ R·S/n` exactly, so `R ≤ n·A/S = n/10` is **sufficient** for a per-path allowance `A`
and nothing whatever is necessary.

## What the challenge asks

That the conjunction be stated wherever the count is, in either direction — because with
[`CH-0273`](CH-0273-the-buildable-ratio-window-is-a-flatness-window.md) applied the conjunction
that matters is **not** empty: read on the one physical per-path threshold, **3 of 32** cells are
flat and admissible. A verdict block that reports thresholds separately hides an empty conjunction
*and* a non-empty one with equal efficiency.

## What does NOT move

Every number of `T-316`'s result file stands, `C-0208`'s 64 published readings are reproduced at
`4.0E-9`, and `C-0212`'s own headline carries all three readings. This challenge is against the
**shape of a verdict**, and it is filed so that a downstream synthesis quoting the JSON does not
inherit the omission.
