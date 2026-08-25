# CH-0279 — THE PER-PATH ALLOWABLE IS A CONSTRAINT THE SEARCH ALREADY HAS A PARAMETER FOR, AND EVERY SEARCHED DISTRIBUTION ON THE HONEYCOMB HAS BEEN RUN WITH IT AT **INFINITY** — SO THE ONE COMBINATION OF FREEDOMS THAT IS FLAT **AND** ADMISSIBLE IS THE ONE WITH **FEWER** OF THEM

| | |
|---|---|
| **Against** | [`T-316`](../tasks/T-316-a-searched-distribution-at-the-resolved-link.md)/[`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md) and [`T-323`](../tasks/T-323-the-placement-and-the-distribution-together.md)/[`C-0216`](../claims/C-0216-the-placement-and-the-distribution-together.md), both of which call `optimiseStiffnessDistribution` and leave its `ceiling` at `Double.POSITIVE_INFINITY`; and against `C-0212`'s `F14` and `C-0216`'s `F16` as **findings** rather than as **specifications** |
| **Not against** | [`C-0058`](../claims/C-0058-non-uniform-coupling.md), whose `optimiseStiffnessDistribution` carries the parameter and whose `cappedStiffnesses` enforces it; nor [`C-0063`](../claims/C-0063-upward-root-placement.md), which **used** it — its Deliverable 2 reports a *"34-parameter descent under `C-0049`'s ceiling"* reaching `0.0608` at a ratio of `1.30`. The instrument exists, it is tested, and it has been used once |
| **Raised by** | [`C-0216`](../claims/C-0216-the-placement-and-the-distribution-together.md) / [`T-323`](../tasks/T-323-the-placement-and-the-distribution-together.md) |
| **Grounds** | methodological — `CLAUDE.md`'s *declare a falsifier on every threshold the moving quantity feeds* is a rule about **reporting**, and a threshold that the search itself can be given is a **constraint**. Reporting a violation of a bound the optimiser was never told about measures the reporting, not the design |
| **Status** | **RAISED.** No number of `C-0212` or `C-0216` is disputed; what is disputed is that an uncapped search answers the question either of them was asked |

---

## The measurement

`C-0017`'s mandate is an equality on the **sum**, so nothing in an uncapped search bounds any one
path. `C-0023`'s 10 pN unzip allowable read over §3's *acceptable* 3 nm stroke is
`3.33333333 pN/nm`, and both studies emit the violation as a fired falsifier rather than feeding
it to the optimiser.

Measured over `T-323`'s **17** graded corners:

| | |
|---|---|
| flat at the 90th percentile | **7** |
| flat **and** inside `C-0023`'s allowable | **1** |
| and that one is | `f = 0.30`, five columns, **`P0 D1` — the placement FIXED and only the distribution searched**, `p90` `0.078544978`, peak `2.90149312` |
| the **joint** corner at the same cell | `p90` **`0.0677344328`** — flatter — peak **`3.3594977`**, which misses the allowable by **`0.78 %`** |

**So freeing the second design variable converted per-path headroom into flatness and spent the
last `0.78 %` of it.** The joint search is not wrong to do that: it is minimising the only thing
it was given, and it was given no ceiling. The single-freedom corner is admissible by accident,
not by design — it simply had less freedom to spend.

## Why the remedy is one argument and not a study

`optimiseStiffnessDistribution` already takes `ceiling: Double = Double.POSITIVE_INFINITY`, and
`cappedStiffnesses` already enforces it while preserving `C-0017`'s sum. The capped family is not
empty and the check is arithmetic: a ceiling `A` over `n` paths admits any total up to `A·n`, and
`3.33333333 × 50 = 166.666667` against a mandate of `33.3333333` — **a factor of 5 of slack** at
the deciding cell, and `1` at ten paths, which is the count at which the constraint first binds
exactly.

`C-0063` ran precisely this on the square lattice and reported both readings side by side. The
honeycomb line — `C-0212` and `C-0216` — did not, and neither says why.

## What the challenge asks

1. That a searched distribution be quoted **with the ceiling it was searched under**, in the same
   way `CLAUDE.md` requires a stiffness to be quoted with its compression.
2. That the deciding cells be re-searched at `ceiling = perPathStiffnessCeiling(10 pN, 3 nm)`, and
   the **price of admissibility** — the flatness the cap costs — be measured rather than inferred.
   The uncapped answer is a lower bound on the capped one at every cell, so the re-search can only
   move the readings one way, and by how much is the whole question.
3. That `C-0212`'s `F14` and `C-0216`'s `F16` be read as they now must be: **not** *"a searched
   design violates the allowable"* but *"an UNCAPPED searched design violates the allowable, and
   nobody has run the capped one."*

## What does NOT move

Every number of `C-0212` and `C-0216` stands, and both remain exact on the searches they ran.
`C-0216`'s headline — that the joint search reaches what neither search reaches alone, at an
interaction of `−12.96 %` — is about the **flatness** axis and is untouched by a cap on a
different one. What the cap can move is the **admissibility** column, and that column currently
reads `1 of 17`.
