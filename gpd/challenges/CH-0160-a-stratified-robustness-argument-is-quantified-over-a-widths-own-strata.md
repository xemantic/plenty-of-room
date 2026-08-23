# CH-0160 — **A stratified robustness argument is quantified over the strata of the width it was run at, and three of `C-0108`'s four do not exist at the width the design can build.** `C-0108` defends *"the interaction is not a host effect"* by showing it survives inside each of `C-0102`'s four phase strata at §3's nominal 40.00 nm. At `C-0086`'s buildable **38.08 nm** the eight-column stratum has **two** members instead of ten, the *"eight-column but not centro-symmetric"* stratum is **EMPTY**, and the richest-inventory stratum is **{0, 16}** instead of ten disjoint phases. **The conclusion survives and its ground does not**: re-stratified at the buildable width the interaction inside the 28-phase remainder is **2.14777246×** its own 34 → 30 count main effect, against `C-0108`'s largest stratum ratio at 40.00 nm — so the argument is stronger on the new partition and is **not the argument that was published**

| | |
|---|---|
| **Raised by** | [`C-0134`](../claims/C-0134-buildable-width-count-phase.md) (`T-188`) |
| **Against** | [`C-0108`](../claims/C-0108-count-phase-interaction.md)'s per-stratum deliverable and the finding *"THE INTERACTION IS NOT A HOST EFFECT, BECAUSE IT SURVIVES INSIDE EVERY STRATUM"*, together with the `C-0102` row of its *"What this does to the standing claims"* table — *"its three demands cut the 32 phases into sets inside which the interaction survives (2.47 – 6.95 % of a level), so the interaction is not a host effect"* |
| **Grounds** | **a stratum is a property of a WIDTH, and the width `C-0108` is read at is not one the design language can draw.** `C-0086` shows a seamless raster quantises the along-helix width at odd multiples of 16 bp, so 40.00 nm (117.6 bp) is unbuildable and 112 bp = 38.08 nm is the nearest width that is. At 38.08 nm, `endOfRowColumnPhases(112) = [8, 24]` and the census is: **2** phases at the richest column count (8, and only with the row-end column admitted; **6** and no phase above 7 without it), **2** at the richest inventory (60 stations, phases **0** and **16**), and **28** in neither. `C-0015`'s ten eight-column phases and `C-0098`'s ten richest-inventory phases — the two sets `C-0108` stratifies on — have **no counterpart** at the buildable width. Measured on the buildable width's own strata the residual ratios are **0.29785241** (phases 8, 24), **0.305403211** (phases 0, 16) and **2.14777246** (the 28 others), so the interaction survives — but on a partition of a different lattice |
| **Status** | **OPEN, and the conclusion is UPHELD on the new ground** — the wording the index carries. No `C-0108` number moves; what moves is which lattice *"every stratum"* ranges over |
| **Severity** | **a GROUND, not a verdict, and `C-0108`'s conclusion is upheld on the new ground.** Nothing `C-0108` reports moves: its worst additive residual (0.0744123213), its count main effect (0.0485610042), its interaction share (0.0979218189), its 2 × 2 (−5.74202435 %, +6.48887743 %) and its 2 × 2's four corners all reproduce here to `8.7e−08` or better, read from its own result file at run time. What is challenged is the *quantifier*: *"every stratum"* ranges over a set that is a function of the tile width, and the standing recommendation is built at the other width. The remedy is one sentence — say **which** width the strata belong to — and it costs no solve, because `C-0134` has already measured the replacement |

---

## What is claimed upstream

`C-0108`'s finding, in full:

> *"**THE INTERACTION IS NOT A HOST EFFECT, BECAUSE IT SURVIVES INSIDE EVERY STRATUM.**
> `C-0102`'s three demands cut the 32 phases into structurally comparable sets, and the worst
> additive residual inside them is …"*

and its `C-0102` row:

> *"Its three demands cut the 32 phases into sets inside which the interaction survives
> (2.47 – 6.95 % of a level), so the interaction is not a host effect."*

The four strata are `C-0108`'s own labels:
*eight-column **and** centro-symmetric* (`C-0063`'s two),
*eight-column* (`C-0015`'s ten),
*richest inventory, seven-column* (`C-0098`'s ten),
and *seven-column, neither richest nor symmetric*.

## Why this is a challenge and not a note

**Because the partition is not a convention the study chose — it is a census of a lattice, and
the lattice moves with the width.**

| stratum | at 40.00 nm (`C-0108`) | at 38.08 nm, row-end **admitted** | at 38.08 nm, row-end **refused** |
|---|---|---|---|
| richest column count | **10** phases (6 – 10, 22 – 26), at 8 columns | **2** phases (8, 24), at 8 columns | **2** phases (8, 24), at **6** columns; nothing above **7** |
| of which centro-symmetric | 2 (8, 24) | **2 (8, 24) — the whole stratum** | 2 (8, 24) |
| richest column count **but not** symmetric | **8** phases | **EMPTY** | **EMPTY** |
| richest inventory (60 stations) | **10** phases, disjoint from the above | **2** phases (**0**, **16**) | none — the richest inventory is 53 |
| the remainder | 12 phases | **28** phases | 28 phases |

Three of the four strata therefore have no counterpart at the buildable width, and one of them is
empty. **A verdict quantified over *"every stratum"* is a verdict about the 40.00 nm partition.**

## What the replacement says

Re-stratified at 38.08 nm with the row-end column admitted — the convention `C-0095` and `C-0099`
closed the programme onto — the same balanced two-way additive fit gives:

| stratum at 38.08 nm | phases | worst additive residual, log units | as % of a level | ÷ its own 34 → 30 count main effect | interaction share |
|---|---|---|---|---|---|
| richest-column **and** centro-symmetric | **2** (8, 24) | 0.0236154869 | 2.38965406 % | **0.29785241** | 1.43926915 % |
| richest inventory | **2** (0, 16) | 0.026264077 | 2.66120173 % | **0.305403211** | 1.30396593 % |
| neither | **28** | 0.0733348956 | 7.60908547 % | **2.14777246** | 9.19982122 % |

So the interaction **does** survive inside the buildable width's own strata, and inside the largest
of them it is **2.14777246×** the count main effect it splits — a stronger statement than the one
published. `C-0108`'s conclusion is upheld; the sentence that carries it has to name its width.

## What would settle it

One editorial correction in `C-0108`: qualify *"every stratum"* as *"every stratum of the 40.00 nm
lattice"*, and cite `C-0134` for the buildable width's own partition. Every number the replacement
needs is already emitted in `gpd/results/T-188-buildable-width-count-phase.json`
(`interactions[]`, the three `within this width's own stratum` scopes) and in
`gpd/results/T-178-count-phase-interaction.json` (the four 40.00 nm ones).

## What this challenge does NOT say

It does **not** say the interaction is a host effect. It does not move any number `C-0108`
publishes, and it does not touch its verdict, its falsifiers or `CH-0123`. It says that a
robustness argument built on a census inherits the census's own validity range, and that this
one's validity range is a tile width the design cannot fold.
