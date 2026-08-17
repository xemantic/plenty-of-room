# C-0090 — **The Gen-1 tile survives the buildable seamless raster width, and the width SELECTS the design the programme already recommends.** `112 bp = 38.08 nm` is **exactly seven column pitches** where §3's 40.0 nm is 7.35, so the row-end scaffold crossover — the one that turns the raster — is a lattice point only at the phases `b ≡ 8 (mod 16)`, i.e. **8 and 24**, which are exactly `C-0063`'s two centro-symmetric phases; `C-0015`'s **ten** eight-column phases collapse to those **two**. At them the upward station lattice is **bit-identical** to the 40 nm one (departure `0.0`), and the best 34-root placement dishes **0.0621469105** of the stroke against `T-5b`'s 0.10 — **12.0 % BETTER** than `C-0063`'s 0.0706145537. **The price is that the arm must be quantised**: the binding ceiling switches from `C-0069`'s inboard `pitch − d = 8.19 nm` to the outboard `edgeX/2 − pitch = 8.16 nm`, the two crossing at `edgeX = 2(2p − d) = 38.14 nm` with 38.08 falling **0.176 base pairs** below — so `C-0039`'s elastica arm overhangs by **0.00439083 nm** (0.0129 of one rise) and `C-0085`'s **24-rise 8.16 nm** arm is exactly tangent, restoring the capacity 38 → 45 and the whole 198 288-member family

| | |
|---|---|
| **Task** | [`T-153`](../tasks/T-153.md), raised by [`C-0086`](C-0086-seamless-scaffold-routing.md)'s *Still open* item 3 and [`CH-0101`](../challenges/CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md) |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** |
| **Verification type** | **logical** (three closed-form lattice congruences — the axis, the end-of-row phase, and the two-sided arm ceiling — which settle most of the branch before any solve, and eight invariants asserted **by construction** at both widths) **+ in-silico** (`C-0063`'s placement pipeline, `C-0009`'s grillage, `C-0058`'s exact Woodbury surrogate, `C-0069`'s `rootedLengthCeiling`, `C-0074`'s `maximumPlanCeilingForCount` and `C-0053`'s packer **re-run as libraries** at the changed width; 1 224 720 exhaustive centro-symmetric placements and 616 140 descent evaluations) |
| **Verdict** | **PASS, and the acceptance is met in full: every one of `C-0063`'s placement, `C-0015`'s phase census and the branch's plan margins is re-read at 112 bp, and none of `CH-0101`'s four escapes is needed.** **The axis settles half the branch in one line.** Rothemund's odd-half-turn rule binds the distance between successive *scaffold* crossovers, which in a boustrophedon are the **two ends of one row** — an **along-helix** length. So `Gen1Tile.EDGE_X` moves 40.0 → 38.08 nm and the across-helix geometry is a **count of duplexes the scaffold never rasters along**: 15 duplexes at the SAXS 2.69 nm, 40.35 nm, untouched. **Eight quantities are therefore invariant BY CONSTRUCTION** — the root pitch, `C-0069`'s `pitch − d`, `C-0072`'s `M = p − d − L`, `C-0063`'s `3a + 2(15−a) = 34`, `C-0049`'s per-path ceiling, `C-0017`'s mandate, the duplex count and the across-helix span — asserted at both widths at departure `0.0` and not argued. **The width then selects the phase.** 38.08 nm is `7 × 5.44` exactly, so a column lands on the row end at `b ≡ 8 (mod 16)` — phases **8 and 24**, `C-0063`'s centro-symmetric pair — and in a seamless boustrophedon that column **is** the scaffold crossover. `C-0015`'s ten eight-column phases become **two** (or **none** if the row-end crossover is refused, which is `CrossoverLayout.EDGE_MARGIN`'s numerical guard making a physical assertion). At those two phases the upward station set is **identical** to the 40 nm one, so the whole comparison is of **hosts and loads**, not of stations. **The tile is still flat, and flatter**: 0.0621469105 at phase 8 against the free tile's 0.299034765 and `C-0063`'s 0.0706145537 — the winning phase moves 24 → 8. **What it costs is an arm that must be an integer number of base pairs.** The plan budget is a **minimum of two bounds** and `C-0069` reported only the inboard one; the outboard one, `edgeX/2 − outermost root`, owns the budget below `edgeX = 38.14 nm`. `C-0039`'s 8.16439083 nm root overhangs a three-arm three-site row by **0.00439083 nm**, takes the phase-24 capacity 45 → 38 and halves the symmetric family (198 288 → 93 312), and dishes 0.1427 at phase 8 — **outside** `T-5b`. `C-0085`'s quantisation to **24 rises** makes the clearance exactly zero and restores every one of those numbers bit for bit. Raises [`CH-0105`](../challenges/CH-0105-the-plan-budget-is-two-bounds-and-only-one-carries-the-tile-width.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The motif (`C-0055`'s free lever on one upward crossover) remains **undemonstrated**, and `C-0022`'s collar terms are **carried unchanged** to the narrower tile rather than re-solved |
| **Provenance** | `gpd/results/T-153-buildable-raster-width.json`, produced by `anchoring.BuildableRasterWidthStudyKt`; model in `src/main/kotlin/anchoring/BuildableRasterWidth.kt` (**new file** — `UpwardRootPlacement.kt`, `UnusedJunctionSite.kt`, `ScaffoldRouting.kt`, `SeamWeave.kt`, `WeaveExclusionWidth.kt`, `OutputElementPlacement.kt`, `TwoPerRowPlacement.kt`, `HingeArmArrayPacking.kt` and `Gen1Tile.kt` were **read, not edited**); **6 admissible-width records, 10 invariants, 96 phase-census records, 30 station-lattice records, 3 arm-ceiling records, 8 exhaustive placement enumerations, 32 descent records, 14 plan margins, 2 force records, 7 convergence records, 16 upstream reproductions, 5 predicates, 6 falsifiers, 7 findings**; **16 gate-named tests in `src/test/kotlin/anchoring/BuildableRasterWidthTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 18 m 03 s** — the whole suite on its own isolated tree, with two concurrent agents' half-written files dropped by `--drop-file` (`src/main/kotlin/stability/LargeRotationArmBranchStudy.kt`, `src/test/kotlin/synthesis/BufferRouteCensusTest.kt`) and nothing else; the result file **re-run through `tools/study.sh` and all 1 428 numeric fields diffed IDENTICAL** across two independent JVM runs; `tools/result-reader-census.py --emit` re-run and `--check` clean; `tools/check-markdown-tables.py` clean over 276 files |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes at the SAXS 2.69 nm** (40.35 nm across the helices, **UNCHANGED**), 0.34 nm rise, 32/3 bp per turn, 16 bp crossover spacing; along-helix width **40.00 nm** (§3) against **38.08 nm** (`C-0086`'s nearest admissible seamless raster row, 112 bp); `C-0039`'s arm at `C-0055`'s 34 paths, **8.16439083 nm**, and its buildable quantisation **8.16 nm** (24 rises); `C-0017`'s **33.3333 pN/nm** mandate; `C-0022`'s solved profile at 2 mM, a 10 nm gap and **0.192 V**, its collar terms **carried unchanged**; `C-0001`'s foundation secant |
| **Consumes** | [`C-0086`](C-0086-seamless-scaffold-routing.md) (`isOddHalfTurnSeparation`, the 112 bp row length — **re-derived, not transcribed**), [`C-0063`](C-0063-upward-root-placement.md) (`upwardRootLattice`, `centroSymmetricPlacements`, `descendPlacement`, `UpwardRootInfluenceBank`, and its two published optima **read from its result file as the gate**), [`C-0055`](C-0055-unused-junction-site.md) (the 8 bp plane lattice, the upward azimuth, the 34, the arm), [`C-0069`](C-0069-output-element-placement.md) (`rootedLengthCeiling`, `stationRowsOf` — **re-run as libraries**), [`C-0074`](C-0074-two-per-row-placement.md) (`maximumPlanCeilingForCount`), [`C-0085`](C-0085-collinear-stacking-clearance.md) (the rise quantisation and the 6-rise collinear clearance), [`C-0053`](C-0053-hinge-arm-array-packing.md) (`placeHingeArms`, `maximumArmsInRow`), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved edge profile, **read from its result file**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (`CrossoverLayout`, the 32-phase period, the 56/49 inventory), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0049`](C-0049-compliance-ceiling-stroke.md), [`C-0014`](C-0014-lateral-confinement.md), `Gen1Tile` |
| **Raises** | [`CH-0105`](../challenges/CH-0105-the-plan-budget-is-two-bounds-and-only-one-carries-the-tile-width.md), against `C-0069`'s *"the plan budget on **every** 34-root placement is exactly `pitch − d`"* and, through it, `C-0085`'s widening |

---

## The claim, in one line

**A 4.8 % narrower tile is not a 4.8 % perturbation of anything — it is a different integer, and the integer it is turns out to be the one the placement already wanted; what the width actually costs is that the arm can no longer be a solved real number.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly); `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward**, away from the grafted layer; `w` positive **downward**.
- **A row** is one duplex; **a plane** is `C-0055`'s 8 bp crossover plane; **a column** is the sheet's own 16 bp column lattice, of which the planes are every other member; **a root** is the crossover tying one upward arm to its host duplex.
- **`edgeX` is the along-helix width** — `Gen1Tile`'s own words, *"the §3 tile footprint **along the helices**"*.
- **Interior** means `CrossoverLayout.phased`'s truncation, which keeps only what lies strictly inside the footprint by `EDGE_MARGIN = 0.05 nm`. **End-of-row admitted** means a column or plane lying *on* the row end is kept, inset by that same 0.05 nm.

---

## Deliverable 1 — the axis, and the eight invariants that follow from it

`C-0086`'s rule binds *"the distance between successive scaffold crossovers"*, and a boustrophedon's
successive scaffold crossovers are the two ends of **one row**. That is an **along-helix** length.
The across-helix span is `15 × 2.69 = 40.35 nm` and is a **count of duplexes**; the scaffold does
not raster along it, and no odd-half-turn condition applies to it.

**So the duplex count does not change, and neither does anything written on the along-helix lattice.**

| quantity | owner | at 40.00 nm | at 38.08 nm | invariant **by construction**? |
|---|---|---|---|---|
| the across-helix span | §3 / Fischer et al. | 40.35 nm | 40.35 nm | **yes** |
| the duplex count | §3 | 15 | 15 | **yes** |
| the upward root pitch | `C-0055` | 10.88 nm | 10.88 nm | **yes** |
| `C-0069`'s inboard budget `pitch − d` | `C-0069` | 8.19 nm | 8.19 nm | **yes** |
| `C-0072`'s `M = p − d − L` | `C-0072`/`C-0066` | 0.02560917 nm | 0.02560917 nm | **yes** |
| `C-0063`'s count vector, `3a + 2(15−a) = 34` | `C-0063` | 4 rows of three | 4 rows of three | **yes** |
| `C-0049`'s per-path stiffness ceiling | `C-0049` | 3.33333 pN/nm | 3.33333 pN/nm | **yes** |
| `C-0017`'s mandate | `C-0017` | 33.3333 pN/nm | 33.3333 pN/nm | **yes** |
| the centro-symmetric phases | `C-0063` | {8, 24} | {8, 24} | **computed, not asserted** |
| the footprint | §3 | 1614 nm² | **1536.528 nm²** | **no — −4.8 %** |

The eight *"yes"* rows are asserted in code at both widths, and the study **aborts** if one moves.
**Departure: `0.0`, all eight.** That is falsifier **F4** and it did not fire.

**The footprint is the only thing the level of the load cares about, and it cancels.** §3's 100 pN
over 1536.528 nm² is `+5.04 %` of pressure — and the free stroke it is normalised by is
`q/k_f`, which carries the **same** factor (4.90731102 → 5.15473846 nm). Dishing over stroke is a
ratio of two linear responses to the same field, so **the level of the load moves it not at all**;
what moves it is the *geometry* — the host's columns and the collar's position.

---

## Deliverable 2 — the buildable width SELECTS `C-0063`'s own phase

`38.08 = 7 × 5.44` **exactly**, where `40.0 = 7.35 × 5.44`. So the column census stops being a
truncation with a remainder and becomes a **tangency**: a column sits at `x = ±L/2` exactly when
`phase ≡ −L/2 (mod 16 bp)`, i.e. `b ≡ −56 ≡ 8 (mod 16)`.

> **`endOfRowColumnPhases(112) = [8, 24]`** — a closed-form congruence, not a search, and **empty**
> for any row length that is not a whole number of pitches (in particular for §3's 118 bp).

**And in a seamless boustrophedon that column is the scaffold crossover that turns the raster.**
The routing and the crossover phase have stopped being independent choices.

| | 40.00 nm | 38.08 nm, row-end **refused** | 38.08 nm, row-end **admitted** |
|---|---|---|---|
| eight-column phases | **10** — 6…10 and 22…26 | **0** | **2** — 8 and 24 |
| seven-column phases | 22 | 30 | 30 |
| six-column phases | 0 | **2** — 8 and 24 | 0 |
| interface crossovers at the eight-column phases | **56** (`C-0015`) | — | **56** |
| interface crossovers at the seven-column phases | **49** (`C-0015`) | 49 | 49 |
| interface crossovers at the six-column phases | — | **42** | — |

**`C-0015`'s ten eight-column phases collapse to two, and the two are exactly `C-0063`'s
centro-symmetric pair** — a *sixth* independent construction landing on that set, and the first in
which the tile's own width does the landing. Falsifier **F5** (no eight-column phase survives) did
not fire under the admitted reading, and **would** have fired under the refused one.

---

## Deliverable 3 — the station lattice is IDENTICAL, so this is a comparison of hosts

At phases 8 and 24 the upward (`EAST`) sites of every row sit at `0, ±10.88` (three-site rows) and
`±5.44, ±16.32` (four-site rows). The outermost is 16.32 nm, inside **both** half-widths.

> **Worst departure over 15 rows at 2 phases: `0.0` nm.** Falsifier **F2** did not fire.

That is what makes the whole rest of this claim a two-variable comparison: the **host** (which
columns the grillage carries) and the **load field** (where the collar sits). It is also why the
end-of-row planes matter only for the *columns*: an end plane has an **even** index, and the
upward azimuth needs `k ≡ 2b + 3 (mod 4)`, which is odd. **The row-end crossover can never be an
upward site**, at any phase, so admitting it adds in-plane inventory and no stations.

---

## Deliverable 4 — the plan budget is TWO bounds, and the width owns one of them

A three-arm row is bounded twice:

| bound | expression | carries | at 40.00 nm | at 38.08 nm |
|---|---|---|---|---|
| **inboard** — the pair one pitch apart | `pitch − d` | no tile width | **8.19 nm** | **8.19 nm** |
| **outboard** — the arm that must stay over the tile | `edgeX/2 − x_outermost` | no interhelical distance | **9.12 nm** | **8.16 nm** |
| **the binding one** | the minimum | — | **8.19** (inboard) | **8.16** (outboard) |

They cross at `edgeX = 2(2p − d) = **38.14 nm**`, and **38.08 nm falls 0.176 base pairs below it.**
At 40.0 nm the outboard bound is slack by 0.93 nm and invisible, which is why nine claims wrote the
budget as a lattice constant. That is [`CH-0105`](../challenges/CH-0105-the-plan-budget-is-two-bounds-and-only-one-carries-the-tile-width.md).

**Consequences, all bisected with `C-0069`'s own `rootedLengthCeiling`:**

| row set | clearance | at 40.00 nm | at 38.08 nm |
|---|---|---|---|
| `C-0063`'s **own** 34 rows | SAXS 2.69 nm | 8.19 nm | **8.19 nm — unchanged** |
| a **three-site** row carrying three arms | SAXS 2.69 nm | 8.19 nm | **8.16 nm** |
| `C-0063`'s own 34 rows | `C-0085`'s 6 rises | **8.84 nm** | 8.84 nm |
| a three-site row carrying three arms | `C-0085`'s 6 rises | **8.84 nm** | **8.16 nm — the widening is annihilated** |
| the razor `c = k L³/EI` on a three-site row | SAXS | **2.34166** | **2.31602** |

`C-0063`'s own placement is untouched because all four of its three-arm rows sit on **four**-site
rows, whose outermost root is at 16.32 nm and whose outboard arm points inward at either width.
The lattice's *other* three-arm configuration is the one the tile edge cuts — and it is **seven of
the fifteen rows** at each of phases 8 and 24.

**On a three-site row every collinear clearance from 4 to 8 rises now gives the same 8.16 nm**
(9.12 / 9.12 / 8.84 / 8.50 / 8.16 at 40.00 nm), so `C-0085`'s design variable has no purchase there
at all.

---

## Deliverable 5 — the arm must be an integer number of base pairs, and then everything returns

`C-0039`'s elastica root is **8.16439083 nm = 24.0129 rises**, which no duplex can be.

| | elastica 8.16439083 nm | buildable 24 rises = 8.16 nm |
|---|---|---|
| outboard clearance of a three-arm three-site row at 38.08 nm | **−0.00439083 nm** — a clash | **3.55e−15 nm** — exactly tangent |
| the overhang as a fraction of one rise | **0.0129** | — |
| arm capacity of the phase-24 upward lattice at 38.08 nm | **38** (45 at 40.00) | **45** |
| the same at phase 8 | **37** (45 at 40.00) | **45** |
| exhaustive centro-symmetric 34-root placements at phase 24 | **93 312** (198 288 at 40.00) | **198 288 — the same set** |
| best dishing at phase 8 | **0.142709615 — OUTSIDE `T-5b`** | **0.0621469105** |
| best dishing at phase 24 | 0.0776435346 | 0.070693794 |

**`32 bp of pitch + 24 bp of arm = 56 bp = exactly half of 112 bp.`** `CLAUDE.md` already records
that *a lattice can hold a tolerance of zero*; here the tolerance is zero **because** the width is
buildable. The overhang the unquantised arm asks for is **77× below the rise**, so `CLAUDE.md`'s
*a margin below 0.34 nm cannot be corrected, only removed* applies in its strongest form — and
`C-0085`'s own discipline removes it. **The buildable width does not merely tolerate the
quantisation; it requires it.**

---

## Deliverable 6 — the flatness, and the phase that wins

Exhaustive over the centro-symmetric family at phases 8 and 24; a 32-phase four-start descent
beside it, on the recommended geometry.

| case | phase | columns | enumerated | best dishing/stroke | free tile | inside `T-5b`'s 0.10? |
|---|---|---|---|---|---|---|
| **GATE** — 40.00 nm, elastica arm | 24 | 8 | 198 288 | **0.0706145537** | 0.307902368 | **yes** |
| **GATE** — 40.00 nm, elastica arm | 8 | 8 | 163 296 | 0.0873905056 | 0.307902368 | yes |
| **RECOMMENDED** — 38.08 nm, 24 bp arm, row-end admitted | **8** | 8 | 163 296 | **0.0621469105** | **0.299034765** | **yes** |
| **RECOMMENDED** — the same at phase 24 | 24 | 8 | 198 288 | 0.070693794 | 0.299034733 | yes |
| **BRACKET** — 38.08 nm, 24 bp arm, row-end **refused** | 8 | 6 | 163 296 | **0.168371808** | 0.307355642 | **no** |
| **BRACKET** — the same at phase 24 | 24 | 6 | 198 288 | 0.156510532 | 0.307355642 | **no** |
| **UNBUILDABLE ARM** — 38.08 nm, elastica arm | 8 | 8 | 46 656 | **0.142709615** | 0.299034765 | **no** |
| **UNBUILDABLE ARM** — the same at phase 24 | 24 | 8 | 93 312 | 0.0776435346 | 0.299034733 | yes |

**The winning phase moves 24 → 8 and the flatness improves 12.0 %.** The 32-phase descent agrees on
the ranking (phase 8 at 0.0658484805, phase 24 at 0.0777862581, every other phase 0.0923–0.1377,
**9 of 32 inside the convention**) and, as at 40 nm, is beaten by the exhaustive symmetric
enumeration at its own phase.

**The cost stays in kind.** Per path 2.94117647 pN against the 10 pN unzip allowable; `C-0014`'s
thermal force 0.345591239 pN; the peak crossover force 1.24568444 → **1.42774664 pN** and the peak
duplex shear 1.40922674 → **1.72247432 pN**, both still an order of magnitude clear of their
allowables.

---

## Deliverable 7 — every plan margin in the branch, re-read

| owner | quantity | 40.00 nm | 38.08 nm | moves? |
|---|---|---|---|---|
| `C-0069` | the plan budget `pitch − d` | 8.19 nm | 8.19 nm | **no** — invariant by construction |
| `C-0069`/`C-0072`/`C-0066` | `M = p − d − L`, elastica arm | 0.02560917 nm | 0.02560917 nm | **no** |
| `C-0085` | `M` at the buildable 24 bp arm | 0.03 nm | 0.03 nm | **no** |
| **`T-153`** | the **outboard** margin, elastica arm | **+0.955609 nm** | **−0.004391 nm** | **YES — sign change** |
| **`T-153`** | the **outboard** margin, 24 bp arm | +0.96 nm | **0.000000 nm** | **YES — to exactly zero** |
| `C-0074` | the 30-root plan ceiling at phase 24 | 9.535 nm | 9.535 nm | **no** |
| `C-0074`/`C-0063` | the 34-root plan ceiling at phase 24 | 8.19 nm | 8.19 nm | **no** |
| `C-0053` | the in-plane hinge-arm packer at 9.13 nm, phase 24 | **43 arms** | **29 arms** | **YES — −33 %** |
| `C-0069` | the bisected budget on `C-0063`'s own rows | 8.19 nm | 8.19 nm | **no** |
| `C-0069` | the same on a three-site row | 8.19 nm | **8.16 nm** | **YES** |
| `C-0085` | the widened budget, `C-0063`'s own rows | 8.84 nm | 8.84 nm | **no** |
| `C-0085` | the same on a three-site row | 8.84 nm | **8.16 nm** | **YES — annihilated** |
| `C-0069`/`C-0085` | the razor `c`, `C-0063`'s own rows | 2.34166 (2.94462) | 2.34166 (2.94462) | **no** |
| `C-0085` | the clearance sweep, 4–8 rises, three-site row | 9.12 … 8.16 | **8.16 throughout** | **YES — saturated** |

**Fourteen margins; six move, and every mover is an OUTBOARD quantity nobody had written down.**
`C-0053`'s 43 → 29 is the largest single movement in the branch — a 4.8 % narrower tile costs a
third of the *in-plane* arm count, because the in-plane root pitch is 5.44 nm and a 9.13 nm arm
needs three of them — and it is harmless only because `C-0053`'s branch is already refused.

---

## The five verification gates

Executed as **16 gate-named tests** in `src/test/kotlin/anchoring/BuildableRasterWidthTest.kt`,
plus five in-study `check`s that abort the run.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a width is `bp × rise`; `quantisedToRise` is a floor and is idempotent; the overhang is a length; unphysical arguments throw at **seven** entry points | **PASS** |
| **2 — limiting cases** | **with the row end refused, `rasterColumnLayout`, `rasterSiteInventory` and `rasterUpwardSites` reproduce `CrossoverLayout.atBasePairPhase`, `junctionSiteInventory` and `upwardRootLattice` to the last bit at BOTH widths and all 32 phases**; `endOfRowColumnPhases` is empty for 118 bp and `[8, 24]` for 112 and 144; a zero-length arm has clearance and a 40 nm arm never places | **PASS** |
| **3 — symmetry and conservation** | the station lattice at phases 8 and 24 identical between widths (`0.0`); `centroSymmetricUpwardPhases` re-derived at the new width and still `[8, 24]`; the end-of-row plane lattice symmetric about the tile centre; **the Woodbury surrogate against the assembled 855-DOF solve at `2.9e−8`**; **a uniform load on a uniform Winkler foundation dishing exactly zero** — see below | **PASS** |
| **4 — numerical convergence** | nested subdivisions 1 ⊂ 2 ⊂ 4: 0.0617554318 / 0.0621469087 / 0.0618197415, **0.53 %**; the dishing grid 41/81/161: 0.0621469087 / 0.0621469087 / 0.0624086433, **0.42 %**; the descent repeated returns an **identical placement key**; and the **inset sweep** below | **PASS** |
| **5 — literature and upstream** | **16 reproductions, 9 of them strict**: `C-0063`'s **0.0706145537** (departure **2.88e−10**) and **0.0873905056** (**4.45e−10**), its enumerated **198 288** and **163 296** (**exactly**), `C-0069`'s **8.19** bisected on its own rows (**1.1e−10**) and its razor **2.34166**, `C-0085`'s **8.84** and **2.94462**, `C-0072`'s **0.02561**, `C-0055`'s **10.88** and **34**, `C-0015`'s **56/49**, `C-0086`'s **112 bp** and `CH-0101`'s **38.08 nm** | **PASS** |

### Gate 3/4 — the one place the guard is not inert, measured

`CrossoverLayout.EDGE_MARGIN = 0.05 nm` exists so a column cannot seed a zero-length beam element.
At 40.0 nm it is inert — no base-pair phase brings a column within 0.28 nm of the edge. At 38.08 nm
it **deletes** two of eight columns at exactly the two phases the design wants, and the reading it
produces (the BRACKET rows above) is **outside `T-5b`** where the admitted reading is comfortably
inside. So the guard had to be swept:

| inset | 0.05 nm (the guard) | 0.17 nm (half a rise) | 0.34 nm (one rise) |
|---|---|---|---|
| dishing/stroke of the recommended placement | 0.0621469087 | 0.0623464033 | 0.0622667411 |
| the uniform-load falsifier's residual, over the free stroke | 2.13e−7 | 3.48e−9 | **0.0** |

**The answer is stable to 0.32 % across the guard**, and the exact-zero falsifier's residual is
pure conditioning of the short end element the inset creates — it falls by two decades as the inset
grows and reaches machine zero at one rise. The falsifier is therefore asserted **relative to the
free stroke** at `1e−6`, with the residual carried, rather than at an absolute `1e−9` the geometry
cannot support.

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the best 34-root placement at 38.08 nm is outside `T-5b`'s 0.10 | **NO** | **0.0621469105** against 0.10 — and 12.0 % better than at 40.00 nm |
| **F2** | the upward station lattice at phase 24 differs between the widths | **NO** | departure **`0.0`** over 15 rows at 2 phases |
| **F3** | the self-consistent upward arm count is no longer 34 | **NO** | the phase-24 lattice carries **45** with the buildable arm and **38** with the elastica one, against 34 demanded |
| **F4** | a quantity declared invariant by construction moves | **NO** | 8 of 8 at departure `0.0`; the study aborts if one does |
| **F5** | no phase at 38.08 nm carries eight columns under **either** convention | **NO** | phases 8 and 24 do, with the row-end column admitted; **none** does with it refused, which is why both readings are carried |
| **F6** | the pipeline fails to reproduce `C-0063`'s 0.0706145537 at 40.00 nm | **NO** | reproduced at **2.88e−10** |

**What was not anticipated.** The task was formulated as a damage assessment — *what does a 4.8 %
narrower tile break* — and three of its four substantive results run the other way. The width
**selects** the phase the placement already wanted, the placement gets **flatter**, and the arm
quantisation `C-0085` argued for on stacking-energy grounds turns out to be **geometrically
required**. What it does break is a claim nobody was looking at: `C-0069`'s budget is two bounds
and the second one had never been written down.

---

## Validity range

- **TRL 1–3, and the motif is not demonstrated.** `C-0055`'s free lever on one upward crossover is unchanged and remains this programme's own construct.
- **`C-0022`'s collar terms are CARRIED, not re-solved.** The interior pressure is rescaled to the new footprint, but the taper depth, taper width and rim residual are the 40 nm tile's. The collar is a sub-Debye rim feature and `C-0022`'s own total fringing enhancement scales as `1/L`, so the error is of order 5 % **in the collar term**, not in the interior — and the dishing is a ratio in which the level cancels. A re-solve of the 2-D Poisson-Boltzmann edge at 38.08 nm is the honest fix and is **not** done here.
- **The end-of-row convention is a MODELLING CHOICE and both readings are carried.** The physical argument for admitting it is that a seamless boustrophedon's row-end scaffold crossover certainly exists; the argument for refusing it is that no crossover has ever been drawn at the last base pair of a duplex and the grillage cannot represent one without an inset. **The verdict differs between them** (0.0621 against 0.1684) and that is stated, not averaged.
- **The arm is quantised DOWN**, which over-places `C-0017`'s equality by 0.16 % of stiffness. `C-0075`'s one-base-pair trim recovers it; that trim is not re-run here.
- **`C-0081`'s seam cost is CITED, not recomputed** at the new width.
- **144 bp = 48.96 nm is not evaluated.** It is admissible, 22 % *larger* than §3's nominal, and above the 38.14 nm crossing so its outboard bound is slack again.
- **The emitted `departure` fields read `0.0` where the true departure is `2.9e−10`.** `structure.roundedForResult` floors at an absolute `1e-9`, which is a magnitude *in the locked units* and does not travel to a **dimensionless** ratio (`CLAUDE.md`). The un-floored values are carried verbatim in the falsifier strings (`F6`: *"departure 2.88e-10"*) and in the console log; the strict gate is taken **before** serialisation, at `1e-8`.
- **No stiffness, stroke, bias, force-balance or layer number is touched.** This claim moves a width, a phase and a plan.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the odd-half-turn rule and the 112 bp row length | — | **`C-0086`, CONSUMED AND RE-DERIVED** (`isOddHalfTurnSeparation` re-run; 112 admissible, 118 not) |
| `C-0063`'s two centro-symmetric optima | 0.0706145537, 0.0873905056 | **`C-0063`, READ FROM ITS RESULT FILE and REPRODUCED to 2.9e−10 / 4.5e−10** |
| `C-0022`'s solved collar terms | — | **`C-0022`, READ FROM ITS RESULT FILE, keyed on concentration, gap and bias** |
| `C-0039`'s arm at 34 paths | 8.16439083 nm | **`C-0055`/`C-0039`, CITED** as `C0055_ARM_LENGTH` |
| interhelical distance, rise, bp/turn, crossover spacing | 2.69 nm, 0.34 nm, 32/3, 16 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| `C-0085`'s 6-rise collinear clearance and its 8.84 / 2.94462 | — | **`C-0085`, CONSUMED and REPRODUCED at 40.00 nm** |
| `C-0053`'s 9.13 nm in-plane arm | 9.13 nm | **`C-0053`, CITED** |
| `C-0081`'s seam cost, 6–12 of 34 and 0.122724 / −0.002276 nm | — | **`C-0081`, CITED, not recomputed** |

Everything else — the axis argument, the eight invariants, the end-of-row congruence and its two
phases, the whole three-convention phase census, the station-lattice identity, the two-sided arm
ceiling and its crossing width, the overhang and its quantised repair, all eight exhaustive
placement enumerations, the 32-phase descent, the fourteen plan margins and the inset sweep — is
**derived here in code**.

## Still open — named, not answered

1. **`C-0022`'s edge load at 38.08 nm.** The collar is carried, not re-solved. It is the one input this claim rescales rather than recomputes, and it is the only route by which the flatness verdict could move materially.
2. **Which end-of-row convention is right.** It is a question about whether a crossover can be drawn at the last base pair of a duplex, and it is worth the whole verdict (0.0621 against 0.1684). **This is a design-language question, not a modelling one, and it should be put to NDI beside the scaffold question `C-0086` already carries.**
3. **The seamed tile at 40.0 nm has not been re-costed against this result.** `C-0081` prices the seam in stations; what a seam does to the *phase census* at 40.0 nm is not asked here.
4. **144 bp = 48.96 nm.** The other neighbour of §3's nominal, and 22 % larger. Whether §3 would rather have a bigger tile than a narrower one is a specification question.
5. **`C-0087`'s measured staple dropout is not applied here.** Every dishing in this claim is a zero-defect optimum, and `T-155` is showing that such optima are cancellations a missing path destroys.

## Challenges

**Raises [`CH-0105`](../challenges/CH-0105-the-plan-budget-is-two-bounds-and-only-one-carries-the-tile-width.md)** against `C-0069`'s quantifier and `C-0085`'s widening.

**[`CH-0101`](../challenges/CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md) is DISCHARGED in its open item** — *"the number of upward stations, the crossover-phase census, every plan margin: unevaluated"* — and its own sign guess is corrected: it calls the effect *"unfavourable but small"*, and the flatness moves **favourably** by 12.0 % while the thing that is unfavourable is a margin the challenge did not name.

**[`C-0086`](C-0086-seamless-scaffold-routing.md)'s *Still open* item 3 is CLOSED.**

**None stands against this claim.** The five ways it would fail:

1. **A row-end crossover that cannot be drawn.** Then the eight-column phases are gone at 38.08 nm and the best placement dishes 0.1568–0.1684, outside `T-5b` — the branch's only flat design would need re-searching at seven columns.
2. **A re-solved `C-0022` collar at 38.08 nm that differs materially in shape**, not just in level.
3. **An interhelical distance of 2.73 nm** (the square-lattice SAXS value, inside its own source's bracket): the crossing width moves to 38.06 nm, below 38.08, and the *inboard* bound owns the budget again — at 8.15 nm, which still refuses the elastica arm. The mechanism changes and the verdict does not.
4. **A design language that absorbs the raster phase with an unpaired base** (`CH-0101`'s escape 1), which restores 40.0 nm and makes this whole claim unnecessary at the price of a defect Rothemund introduced the trick to fix.
5. **A demonstration that an upward arm may overhang the tile edge.** `C-0053`'s footprint convention refuses it and nothing physical does; adopting it would remove Deliverable 4 and 5 entirely.
