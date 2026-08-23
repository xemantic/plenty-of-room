# CH-0177 — **`C-0118`'s *"more paths are monotonically worse"* is refuted by the four numbers printed in the same sentence, and the correction does not repair it.** `0.0278431488 → 0.0541089284 → 0.0461988976 → 0.0408747025` **rises once and then falls three times**: the count axis peaks at two columns and recovers, so it has no threshold and it is not a design rule. At the corrected honeycomb cross-section the same shape holds — `0.0680677948 → 0.119502047 → 0.101905503 → 0.0900369` — and the consequence is now load-bearing, because **the peak is the part that fails `T-5b`**

| | |
|---|---|
| **Against** | [`C-0118`](../claims/C-0118-coupled-four-layer.md) §2: *"More paths are monotonically **worse** there (0.0278431488 → 0.0541089284 → 0.0461988976 → 0.0408747025 as columns go 1 → 2 → 3 → 5)"* |
| **Raised by** | [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md) / [`T-232`](../tasks/T-232-coupled-cells-at-the-honeycomb-cross-section.md), result [`gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json`](../results/T-232-coupled-cells-at-the-honeycomb-cross-section.json) |
| **Grounds** | **logical, and internal.** The sequence quoted is not monotone, on the claim's own printed values and at the claim's own geometry. No new computation is needed to see it; the re-grading is what made anybody read the four numbers rather than the sentence around them |
| **Status** | ~~**raised.**~~ **UPHELD**, and recorded by its target: [`C-0118`](../claims/C-0118-coupled-four-layer.md) (`T-197`) carries a banner reading *"§2's 'More paths are monotonically worse' is separately withdrawn by `CH-0177`"*. Filed by [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md) (`T-232`). `C-0118`'s *value* — that the sparsest coupling is the best one tested — is upheld at both geometries; only the *monotonicity* is withdrawn |

---

## 1. The sequence

| columns | paths on `10 × 6` | `C-0118`'s standing p90 | step | corrected honeycomb p90 | step |
|---|---|---|---|---|---|
| 1 | 10 | **0.0278431488** | — | **0.0680677948** | — |
| 2 | 20 | 0.0541089284 | **worse** ×1.94 | 0.119502047 | **worse** ×1.76 |
| 3 | 30 | 0.0461988976 | *better* ×0.85 | 0.101905503 | *better* ×0.85 |
| 5 | 50 | 0.0408747025 | *better* ×0.88 | 0.0900369 | *better* ×0.88 |

One rise and three falls, at **both** geometries, and the step ratios agree to two figures across a
1.5× change in the attachment pitch — so the shape is a property of the coupling axis and not of the
cross-section.

## 2. Why it matters now and did not before

At `C-0118`'s geometry every one of the four cells was flat, so a mis-stated monotonicity cost
nothing: the design rule *"take the sparsest"* and the design rule *"take any"* have the same
answer when all four pass. At the corrected geometry **the peak is exactly the region that fails** —
2 columns (0.119502047) and 3 columns equal springs (0.101905503) are above `T-5b`'s 0.10 while
1 column and 5 columns are below it. A reader who inherits *"monotonically worse"* concludes that
5 columns is the worst cell available; it is the second best.

`CLAUDE.md` states the general rule and this is an instance of it:
*"a verdict that is not MONOTONE in a swept variable has no threshold, and sweeping it finer finds
more alternation rather than less."* Four points is not a sweep, so the correct statement is a
**census of four cells**, not a law — and there is no licence to interpolate between them.

## 3. The physical reading `C-0118` was reaching for, restated

The claim's own mechanism is right and is worth keeping: *a dropout **is** an increase in the
attachment pitch*, so a coupling with fewer paths has fewer things to lose. What that mechanism
predicts is a **minimum at the sparse end**, not a monotone rise — and the competing effect
(`CLAUDE.md`: *"an attachment coupling can be a NET DISHING SOURCE, and the sign flips at an
attachment pitch of one Winkler bending length"*) is what turns the curve over again. Two named
effects running opposite ways cannot produce a monotone axis, and the four measured points show both.

## 4. What the correction asks of the claim

Strike the word *"monotonically"* and the inference that follows it, keep the four numbers and keep
the mechanism. `C-0142` states the corrected sequence.
