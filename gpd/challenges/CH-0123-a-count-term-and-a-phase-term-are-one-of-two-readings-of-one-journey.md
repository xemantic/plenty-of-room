# CH-0123 — **A count term and a phase term are not two things a move is made of; they are two ways of cutting one journey in half, and here the two cuts disagree by more than the phase is worth.** `C-0103` defends the programme's 34 → 30 recommendation by splitting the move it takes — `C-0063`'s 34 roots at phase **24** to `C-0074`'s 30 at phase **8** — into *"a count term of **+12.86 %** and a phase term of **−19.0 %**"*, and concludes that *"the phase is the larger of the two"*. Both numbers are correct and both are **one ordering** of a 2 × 2. Taken the other way round — phase first, then count — the same journey between the same two designs reads **−11.4787354 %** of phase and **+2.9293362 %** of count. **What is challenged is not a number but the existence of the decomposition**: the interaction is **−8.79880284 %**, which is **0.761334254** of the count term it splits, and on a search-free family at the same two phases the **phase term changes sign** between the two orderings

| | |
|---|---|
| **Raised by** | [`C-0108`](../claims/C-0108-count-phase-interaction.md) (`T-178`) |
| **Against** | [`C-0103`](../claims/C-0103-path-count-at-fixed-geometry.md)'s **Deliverable 2** and the third row of its verdict — *"the count term is +12.86 % and the phase term is −19.0 %, and the phase is the larger of the two"* — and the same sentence where it appears in its *"What this does to the standing claims"* row for `C-0074` |
| **Grounds** | **a two-factor move has two splits and only one total, and `C-0103` reports one split as if it were the decomposition.** `C-0108` grades all four corners of the 2 × 2 under one ensemble per phase: count first gives **+12.8596328 %** then **−19.267547 %** (reproducing `C-0103` exactly), phase first gives **−11.4787354 %** then **+2.9293362 %**. The two orderings share a total of **−8.88564999 %** at a path disagreement of **0.0**, and differ by an interaction of **−8.79880284 %**. On the **search-free** family run at both phases the same construction gives **+10.8397678 %** / **−3.92538749 %** against **+1.92730307 %** / **+4.47532136 %** — the phase term is **adverse** in one reading and **favourable** in the other. And over the whole 32-phase × 6-count grid the **interaction carries more of the variation (9.79218189 %) than the phase main effect does (7.83610301 %)**, with a worst additive residual of **1.53234725×** the count main effect it splits |
| **Severity** | **an attribution, not a verdict, and `C-0103`'s verdict survives it in full.** The 34 → 30 recommendation is defended by `C-0103` on three grounds and only the second is touched. Ground 1 (the plan-margin trade is 528 : 1) contains no phase term. Ground 3 (neither design is flat) is confirmed at all 32 phases — 192 of 192 cells past `T-5b`'s 0.10 at 100 % exceedance. Ground 2 is *"the move the programme actually recommends reads 8.68 % better under fabrication"*, and **that reading is a TOTAL and is path-independent**, so it stands exactly as published. What must be withdrawn is the sentence that follows it: the *split* of that total into a count term and a phase term, and the comparison *"the phase is the larger of the two"* |

---

## What is claimed upstream

`C-0103` Deliverable 2, final row, and its verdict:

> *"**The confound `CH-0103` itself flagged is now a number**: the count term is **+12.86 %** and
> the phase term is **−19.0 %**, and the phase is the larger of the two."*

and, in its *"What this does to the standing claims"* row for `C-0074`:

> *"`CH-0103`'s reading that this is 'a property of the placement, not of the count' is confirmed
> and now carries a decomposition: **+12.86 %** of count against **−19.0 %** of phase."*

The two numbers are obtained by measuring the count term at fixed geometry at phase 24
(0.638498565 → 0.720607136) and then **subtracting** it from the measured total of the recommended
move (0.639129638 → 0.583664426, −8.67824131 %). `C-0103`'s own validity range says the two axes
are not swept together and that *"nothing here bounds the interaction of the two axes"*.

## Why this is a challenge and not a note

**Because a subtraction is a decomposition only when the two factors do not interact, and here
they do.** `C-0108` grades the fourth corner — 34 roots at phase 8, built from `C-0074`'s own 30
by `C-0103`'s own addition rule so that the count axis at phase 8 is nested exactly as it is at
phase 24 — and the 2 × 2 closes:

| the recommended move, 34 @ 24 → 30 @ 8 | count first, then phase | phase first, then count |
|---|---|---|
| first term | **+12.8596328 %** (count, at phase 24) | **−11.4787354 %** (phase, at 34 paths) |
| second term | **−19.267547 %** (phase, at 30 paths) | **+2.9293362 %** (count, at phase 8) |
| **total** | **−8.88564999 %** | **−8.88564999 %** |

The corners are 0.638498565, 0.720607136, 0.565207004 and 0.581763818 of the free-tile stroke; the
path disagreement is **0.0**. The count term is **4.4×** larger in one reading than in the other
and the phase term **1.68×**.

On the **search-free** family — one deterministic construction at both phases, which removes the
objection that the four corners are designs of unequal search quality — the disagreement is
qualitative rather than merely quantitative:

| the same move, search-free family | count first, then phase | phase first, then count |
|---|---|---|
| first term | **+10.8397678 %** | **+1.92730307 %** |
| second term | **−3.92538749 %** | **+4.47532136 %** |
| **total** | **+6.48887743 %** | **+6.48887743 %** |

Here the phase is **favourable** if taken second and **adverse** if taken first. *"The phase is the
larger of the two"* has no truth value under this reading.

**And the interaction is not small on the scale the claim is arguing at.** Over `C-0108`'s
32 × 6 grid the balanced two-way additive fit in `log p90` splits the variation
**82.3717151 %** count, **9.79218189 %** interaction and **7.83610301 %** phase — the interaction
is **larger than the phase main effect** — and its worst residual, **0.0744123213** log units, is
**1.53234725×** the fit's own 34 → 30 count main effect of **0.0485610042**.

## What is NOT claimed

- **`C-0103`'s numbers are not wrong.** Its 0.638498565, 0.720607136, +12.8596328 % and
  −8.67824131 % all reproduce, the first two to `4.6e−10` and `2.3e−10` on `C-0108`'s own
  pipeline, and its −19.0 % is recovered exactly as the second term of the first ordering.
- **Its verdict is not challenged.** `CH-0103` remains upheld as a bookkeeping correction and the
  34 → 30 reduction remains recommended. `C-0108` in fact **strengthens** the recommendation at
  the phase the programme now builds at: at `C-0102`'s phase 8 the count term is **+2.9293362 %**
  on the published designs and **+4.47532136 %** on the search-free family, 2.9 – 4.4× cheaper
  than the figure the trade was priced at.
- **The direction of neither axis is challenged on the published designs.** In both orderings of
  the published 2 × 2 the count is adverse and the phase favourable; it is the *magnitudes* that
  are path-dependent there. The sign reversal is on the search-free family.
- **No claim is made that one ordering is the right one.** There is no privileged ordering; that
  is the point. What a two-factor move has is a **total** and an **interaction**.

## How to settle it

1. **`C-0103` restates the sentence** — queued as **`T-186`**. Something of the form *"the total of the recommended move
   is −8.68 % and it is path-independent; split count-first it is +12.86 % and −19.0 %, split
   phase-first it is −11.48 % and +2.93 %, and the interaction between the two axes is −8.80 %"*
   discharges this challenge with no re-computation, because every number in it is already
   published or is in `gpd/results/T-178-count-phase-interaction.json`.
2. **`ANSWERS.md` carries the total and not the split**, wherever §4(g) quotes the count/phase
   trade.
3. **A third corner measured on an optimised 34-root placement at phase 8.** `C-0108`'s
   published-adjacent 2 × 2 builds that corner by an addition rule rather than by a search, and
   its own validity range says so; an exhaustive or descent-optimised 34 at phase 8 would tighten
   the published reading, though not the search-free one, which is already matched by
   construction.

## What would make this challenge wrong

- **A demonstration that the fourth corner is mis-built.** If 34 roots at phase 8 grown from
  `C-0074`'s 30 is not a fair partner for `C-0063`'s exhaustively optimised 34 at phase 24, the
  *published* 2 × 2's interaction is contaminated. It would not touch the **search-free** 2 × 2,
  whose four corners come from one rule, nor the 32 × 6 grid.
- **A different verdict statistic.** The interaction is measured at the 90th percentile, which is
  a tail statement; at the median the two orderings could agree more closely.
- **A distribution freed at every corner.** `C-0089`'s 1.30–1.61× on this topology exceeds every
  term in the table, and `T-179` owns that axis.
