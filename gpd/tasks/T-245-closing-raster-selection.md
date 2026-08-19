# T-245 — Re-select the honeycomb two-length row pair on SCAFFOLD CLOSURE, and re-grade the coupled cells there

**Leaf** `A8.2`.
**Raised by** [`CH-0188`](../challenges/CH-0188-the-recommended-raster-does-not-close.md) and
[`CH-0189`](../challenges/CH-0189-the-ladder-phase-is-not-a-sweep.md), both from
[`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md) (`T-244`).

---

## Formulate

### The question

`C-0140` recommends a **112 / 108 bp** two-length honeycomb x-raster,
and every coupled cell this repository has graded on a honeycomb block sits at that pair.
`C-0148` showed that it **cannot be drawn** on caDNAno's default rules:
a scaffold crossover sits `±5 bp` from its pair's staple position `b₀ + 7c (mod 21)`,
one lattice constant `b₀` must serve every raster crossover,
and at 112 / 108 no `b₀` does — **10 of 59** raster crossovers would have to be forced.

`C-0140`'s five candidate pairs were selected under a filter (*"a stagger of at most 4 bp"*)
that `CH-0187` shows is unstated and that has nothing to do with closure.
So the selection must be re-run **inside the closing family**, not inside `C-0140`'s five,
and the coupled cells re-graded at whatever that selection returns.

### The numeric target

1. The **closing family**: every residue pair `(L₁ mod 21, L₂ mod 21)` whose raster closes,
   and every length pair in a stated range that realises one, with its own
   stagger, row span, block extent, scaffold budget, raggedness,
   determined `b₀`, determined ladder phase, station census,
   interface window and **row-derived** crossover-column count.
2. A **recommendation** inside that family, scored on `CH-0187`'s four axes
   (axial extent against §3's nominal 40.0 nm; scaffold against M13's 7 249 nt;
   front-face relief against the blunt-end stacking onset; front-face relief against
   `C-0141`'s saturated outboard plan ceiling) **plus** closure, station census and column count.
3. `C-0146`'s eight `10 × 6` coupled cells re-graded at the recommended pair's own
   **row-derived** column count, at both ends of `C-0116`'s measured `f` band,
   with the **uncoupled tile as the reference at every cell**,
   and on the **determined** station lattice as well as on the abstract grid.

### The acceptance predicates

- `P1` the closing family is enumerated **exhaustively over residue pairs modulo 21**
  at both 60-helix cross-sections, and the enumeration reproduces `C-0148`'s
  five-pair verdict and its three closing classes at departure `0.0`.
- `P2` the recommendation is made **inside the closing family** and every axis it is
  scored on is emitted for **every** member, so the ranking is a selection and not an assertion.
- `P3` every re-graded cell carries its uncoupled reference, its `f`, its column count
  and its path count, and the width/column readings are swept rather than chosen.
- `P4` the comparison against `C-0146`'s 112 / 108 cells is **paired** — one common
  dropout stream, restricted per cell, read per realisation.
- `P5` the scaffold and turn-slack budgets are re-derived at the recommended lengths
  and compared against `C-0147`'s 8 nt at a 112 bp row.

### Units and conventions, locked

- Lengths **nm**, axial positions **integer base pairs on one global `z`** (`C-0140`'s convention),
  residues **modulo 21 bp**, stiffness **pN/nm**, dishing **dimensionless**, a fraction of the free stroke.
- Rise **0.34 nm/bp**; honeycomb `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2`;
  layer pitch `d√3/2`; 21 bp per interface; crossover-column pitch `21 × 0.34 / 2` = 3.57 nm.
- `T` = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm.
- `T-5b`'s flatness convention **0.10** of the free stroke; `C-0116`'s measured band `f` = 0.26–0.33.
- `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the **SUM**.

### Verification type

**logical** (exact integer arithmetic on the crossover-residue lattice — no mesh, no sampling)
**+ in-silico** (`C-0142`'s influence surrogate and Monte Carlo dropout grading, on one common stream).

---

## Plan

### The cheap bound runs first, and it may settle the ranking

Closure depends only on `(L₁ mod 21, L₂ mod 21)`, because the reduced residue of a raster
crossover is `(level − 7·class) mod 21` and every level is an integer combination of the two
lengths on a fixed class sequence. So the closing set is a subset of the **441** residue pairs
and can be enumerated exhaustively in microseconds — before any width, any scaffold and any solve.

If that enumeration shows the closing pairs all share one value of `L₁ − L₂ (mod 21)`,
then the **minimum closing stagger** follows by arithmetic, and whether `C-0140`'s
*"at most 4 bp"* filter is merely unstated or is **exactly disjoint** from closure
is settled with no search at all.

The extent of a two-length raster is `2·max − min` (checked, not assumed),
so the best achievable `|extent − 40 nm|` inside the closing family is a one-line search
over the same residue classes and needs no walk.

### Why the Monte Carlo is still needed

The flatness verdict is `T-5b`'s 0.10 read at the **90th percentile** of the measured staple
dropout, and `C-0146` shows three cells of eight turning on a single crossover column.
The re-grade therefore has to be run; what the cheap bound buys is that it is run **once**,
at one pair, rather than over a family.

### What would falsify this approach

- If the closing set were **empty** at both cross-sections, the whole two-length route would be
  off-rule and the deliverable would be a negative, not a recommendation.
- If closure depended on the cross-section, on the axial sign, on the mirror or on the datum,
  the condition would be a convention rather than a rule and nothing could be selected on it.
- If the recommended pair's re-graded cells were **worse than the uncoupled tile** at every
  column count, the coupling would be a net dishing source and the recommendation would be
  a cross-section, not a coupling.

### Falsifiers, declared before the run

| | |
|---|---|
| **F1** | some closing pair beats the recommended one on the axial-extent axis |
| **F2** | the closing family admits a stagger below 7 bp |
| **F3** | the recommended cell (1 column, 10 paths, equal springs) loses `T-5b`'s 0.10 at `p90` at either end of the measured band — **declared OPEN** |
| **F4** | a graded column count is refused on the **determined** station lattice — **declared OPEN** |
| **F5** | the closure verdict depends on the cross-section or on any of the four `(sign, mirror, datum)` conventions |
| **F6** | the recommended pair's row-derived crossover-column count is not what `CH-0188` predicts |
| **F7** | a paired median-of-ratios and the corresponding ratio-of-percentiles disagree in **sign** |
| **F8** | the recommended pair does not fit M13 |
