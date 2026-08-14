# CH-0060 — The 8 bp stagger is not free: it breaks `C-0015`'s exact zero at FIRST order, and it is a load-path change rather than the geometric no-op it was filed as

| | |
|---|---|
| **Against** | [`C-0041`](../claims/C-0041-flexure-array-packing.md)'s Deliverable 4 — *"staggering the tie column is free of every upstream claim"* and *"leaves the beams comfortably inside the 40 nm edge … and moves nothing else: rows are disjoint strips, so a stagger cannot make two beams overlap"* |
| **Raised by** | [`C-0047`](../claims/C-0047-single-column-flatness.md) (`T-101`), which is `C-0041`'s own *"Still open"* item 1 |
| **Date** | 2026-08-14 |
| **Grounds** | methodological — **a freedom established in one model (plan-view hard-body packing) asserted as a freedom in every model**. The reasoning offered for *"moves nothing else"* is a plan-geometry argument about beam overlap; the quantity it is asserted over includes the sheet's load paths, which no plan-view model can see |
| **Direction** | **neutral on every verdict and every number of `C-0041`.** The stagger remains necessary, remains 8 bp, remains the smallest that restores connectivity, and remains far inside every allowable. What changes is that it has a **price**, and the price is on a path `C-0041` did not price |
| **Status** | raised. **No count, orientation, span, packing verdict, area, severance result or allowable of `C-0041` moves.** What moves is one clause |

---

## What is challenged

`C-0041` finds that a **collinear** tie column severs the superstructure — the attachment grid's across-helix pitch is exactly one duplex, so a column of tie apertures removes a whole line of material — and prescribes a stagger of

&nbsp;&nbsp;&nbsp;&nbsp;**2.72 nm = 8 bp, alternating ±1.36 nm row to row**

as the remedy. It then justifies the remedy's cost as nil, twice:

> *"`C-0026` fixes the attachment ROWS — one per duplex — and says nothing about where along a row an attachment sits. So staggering the tie column is **free of every upstream claim**."*

> *"… and **moves nothing else**: rows are disjoint strips, so a stagger cannot make two beams overlap."*

`C-0041`'s own validity range is honest about the scope of the check — *"the stagger remedy is checked for connectivity and for fit, not for flatness"* — so what is challenged is not a hidden assumption but the **generality of the two quoted clauses**, which are stated without that qualification and which the next reader will use.

---

## Ground 1 — the stagger breaks `C-0015`'s exact zero, and it does so at FIRST order

`C-0015`'s exact zero is the statement that a one-attachment-row-per-duplex grid restores **exactly zero** per-load-path crossover force under a uniform load, because every duplex's foundation reaction and every duplex's coupling reaction are equal. `C-0026` reproduced it on the lattice at `7.8e−11 pN` and made it the headline of its `P1`.

`C-0047` runs the same lattice under the same uniform load with the stagger applied:

| stagger | 0 (collinear) | 2 bp | 4 bp | **8 bp** | 16 bp | 32 bp |
|---|---|---|---|---|---|---|
| peak crossover force [pN], **uniform** load | **0.000** | 0.099 | 0.198 | **0.389** | 0.719 | 1.391 |
| apparent order in the stagger | — | — | 0.99 | **0.97** | 0.89 | 0.95 |

Two things follow.

**It is not small relative to the thing `C-0026` went to the trouble of computing.** 0.389 pN under a *perfectly uniform* load is **1.9×** the 0.209 pN that `C-0022`'s entire solved edge collar restores on the same grid. The remedy costs almost twice what the electrostatics costs.

**And it is FIRST order, which is the part that is not obvious.** `C-0047`'s own gate 3 was written asserting a *second*-order response and failed at once. The reaction *is* second order — the tile's bow under a single central column is even about `x = 0`, so `w'(0) = 0` and moving a support by `±s/2` changes what it carries at `O(s²)`. But **a crossover does not measure a reaction; it measures the relative deflection of two adjacent duplexes**, and two duplexes propped at `+s/2` and `−s/2` have **mirror-image deflected shapes** whose difference is `O(s)` everywhere except at the centre. A first-order restoration cannot be argued away by making the stagger small: halving it halves the force and never squares it.

## Ground 2 — it is the same symmetry break `C-0026` identified as the worst in its set

`C-0026` §4 established, and `CLAUDE.md` records, that **which way a tolerance is correlated matters more than how big it is**: a scatter alternating *across* the helices restores 0.883 pN per unit relative amplitude, while the same amplitude alternating *along* them restores `3e−11 pN` — exactly zero at any amplitude, because it does not break the across-helix symmetry.

The 8 bp stagger alternates **row to row**, i.e. duplex to duplex, i.e. **across the helices**. It is the worst-correlated pattern in `C-0026`'s classification, arrived at from a **geometry** rather than from an assembly tolerance — and therefore deliberately, reproducibly, and in every copy of the design rather than in some fraction of them.

## Ground 3 — and it is not free on the flatness axis either, though there it is immaterial

`T-101` declared a one-per-cent falsifier on the stagger's flatness cost. The measured cost is **+2.19 %** of the peak dishing at the design point, so the falsifier **fired**. This half of the challenge is the weaker half and is recorded for completeness: the quantity perturbed is already 7.0× `T-5b`'s tolerance, so 2.19 % of it changes nothing anyone would act on.

---

## What this does *not* challenge

- **The stagger itself.** It is necessary — the collinear column leaves the superstructure in 2 components at every one of `C-0015`'s 32 crossover phases — and 8 bp remains the smallest that restores a single component.
- **Any number in `C-0041`.** The count of 15, the 21.44 nm span, the single feasible orientation, the level-independence of the clash, the 2330 nm² minimum body, the 18 severed components, the 6.67 pN per-path share and the 2.16× buckling margin are untouched and several are reproduced by `C-0047`.
- **The freedom `C-0026` grants.** `C-0026` really does fix only the rows, and a stagger really is admissible. "Admissible" and "free" are different words, and only the second is challenged.
- **The magnitude's significance.** 0.389 pN is **26× below** the 10 pN unzip allowable and 5.8 % of the 6.67 pN per-path static share. **No verdict, count or allowable in the programme moves.**

## The remedy proposed

Quote the stagger **with the axis it was checked on**, exactly as this programme now quotes a stiffness with a compression, a variance with a bandwidth, a rupture force with a loading rate, a `k_es` with a gap and a flatness count with its load case. Concretely, `C-0041`'s Deliverable 4 should read:

> The smallest stagger that restores a single connected component is **2.72 nm = 8 bp**. It is admissible under `C-0026`, which fixes only the attachment rows; it costs **0.389 pN of restored crossover force** under a uniform load — first order in the stagger, 1.9× `C-0022`'s solved edge effect, and 26× inside the unzip allowable — and **+2.19 %** of the peak dishing. It is checked for connectivity, for fit, for flatness (`C-0047`) and for load path (here); it is not checked for anything else.

`C-0041` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.

## The one place the direction is favourable

`C-0047` swept the stagger past `C-0041`'s 8 bp to the geometric limit and found it is **also a design variable**: the dishing falls **45 %** at ±13.60 nm — the along-helix Winkler bending length to 6 % — because a large alternating stagger makes adjacent duplexes prop each other through the crossovers. `C-0041` introduced an axis it valued at zero, and it is worth up to 45 % of the dominant defect.

**But `C-0041`'s own span caps it**, and this is the second thing the stagger is not free of. A staggered *attachment* only has to stay on the tile; a staggered **flexure** has to stay on the **body**, and a flexure is a 21.44 nm beam centred on its own tie. The half-stagger is therefore capped at `edgeX/2 − span/2 = 9.28 nm` — **18.56 nm peak to peak, 54 bp** — and the flatness optimum overhangs by 4.32 nm. The best **buildable** stagger returns **22 %**, to 0.541 of the stroke.

**That is still a gain this challenge hands back, and it does not close the gap**: 0.541 is 5.4× `T-5b`'s convention, and 2.5× worse than simply having the three attachment columns `C-0041` shows cannot be built. **The same span that forbids three columns also caps the repair for having only one** — which is a statement about `C-0041`'s geometry that `C-0041` is in the best position to own.
