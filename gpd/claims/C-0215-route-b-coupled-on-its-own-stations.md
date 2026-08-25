# C-0215 — **GRADED ON ROUTE B's OWN TILE, AT STATIONS DERIVED AT EACH ROW LENGTH, A SEARCHED COUPLING IS FLAT AT `27 OF 48` CELLS AND — FOR THE FIRST TIME IN THIS CORPUS — FLAT **AND** ADMISSIBLE AT `7`.** `0 of 48` on either transferred rule (tightest `0.100227515`), `27 of 48` searched (tightest **`0.0687939715`**), and `7` of those also inside `C-0023`'s per-path allowable where `C-0212` had `3 of 32` on the `116 bp` block and `0` on its own two-flag conjunction. **And the count nobody should quote without the next one: `0 of 48` beat the UNCOUPLED route-B tile**, which `C-0211` shows is already flat at `756 of 756` (`0.0521565503`–`0.0576976711`) — **which is NOT an argument for removing the coupling**: `C-0017`'s mandate is a **placement and stability** requirement and an uncoupled tile delivers no output at all, so on this tile flatness is simply not what the coupling buys, and the searched coupling is a **mandate the flatness survives** — at 27 cells, and admissibly at 7. **The station set could not be inherited and the arithmetic says so before any solve: route B's rows carry `5` station columns against the block extent's `6`, at every one of the 21 phases, and `T-316`'s inherited ladder phase `16` refuses a 5-column placement at `2 of 3` widths** — the column count that carries 12 of the 27 flat cells. **The tile substitution moves the flat verdict at `10 of 48` paired cells** (route B over the block, `0.839625798`–`1.34239093`), **the three widths rank differently coupled than free** (`92 < 106 < 98` uncoupled, `92 < 98 < 106` coupled), and **the transferred ratio band misses one-signed and favourably** — `8 of 48` below it, `0` above. `F11`, `F12`, `F14`, `F18`, `F19` and `F20` FIRED, all six declared OPEN; `F13`, `F15`, `F16`, `F17` were declared open and did not

| | |
|---|---|
| **Task** | [`T-322`](../tasks/T-322-route-b-coupled-on-its-own-stations.md) — raised by [`C-0207`](C-0207-the-uniform-raster-is-flat-with-its-tethers.md) §7 and [`C-0211`](C-0211-the-uniform-raster-at-the-resolved-link.md) §10 and §11 |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (the station census is exact integer lattice geometry and the oracle floor is a pointwise theorem) **+ in-silico** (the same honeycomb grillage, the same `C-0201` tether element, the same `C-0208` per-bond link, the same `C-0058` Woodbury bank and the same `C-0087` dropout stream — only the **tile** and the **distribution** move) **+ literature** (`C-0087`'s measured depth incorporation, re-derived at each width's own edges) |
| **Verdict** | **PASS on all seven predicates.** Of the twenty declared falsifiers, **`F11`, `F12`, `F14`, `F18`, `F19` and `F20` FIRED — every one of them declared OPEN**, so *"either answer is the result"*; `F13`, `F15`, `F16` and `F17` were declared open and did not fire; the ten closed falsifiers did not fire. **Nothing standing is withdrawn**: `C-0212`'s `22 of 32` and `C-0208`'s `0 of 64` are exact readings on the `116 bp` block, and this study reproduces `C-0212`'s own paired cells rather than disputing them. Raises [`CH-0276`](../challenges/CH-0276-a-coupled-census-inherited-a-station-set.md) and [`CH-0277`](../challenges/CH-0277-a-ratio-transferred-between-two-lattices.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Every number is a property of one lattice, one placement family, one raster, one load case and one dropout model, read at a **radial** link constant `C-0208` records as unsourceable and carries as a bracket. No route-B coupling has been drawn, let alone folded |
| **Provenance** | [`gpd/results/T-322-route-b-coupled-on-its-own-stations.json`](../results/T-322-route-b-coupled-on-its-own-stations.json), written by [`tile/RouteBCoupledStudy.kt`](../../src/main/kotlin/tile/RouteBCoupledStudy.kt) (**new**) on [`tile/RouteBCoupled.kt`](../../src/main/kotlin/tile/RouteBCoupled.kt) (**new**). **19 named tests** written first and watched fail — [`tile/RouteBCoupledTest.kt`](../../src/test/kotlin/tile/RouteBCoupledTest.kt), which did not compile against a model that did not exist — and a **20-mutation** harness at [`tools/T-322-mutation-test.py`](../../tools/T-322-mutation-test.py): **0 survivors over a subtracted baseline of 0**, after a first run whose **2 survivors were one real gap** (§10), declared in `tools/P-31-harness-census.py` and wired as `testRouteBCoupledMutations` in the same commit. **No existing Kotlin main source was modified** except one hand-added line of `structure/ResultInputs.kt`, the generated handle registry — provably inert, `ResultInputs.all` being read at 9 sites all inside `structure/ResultInputsTest.kt`. `tile/HoneycombGrillage.kt`, `tile/SearchedDistribution.kt` and `coupling/RobustDistribution.kt` were **not** touched. **`F4` is discharged by a second full emission in a separate snapshot, diffed against the artifact outside the study** (§11) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, `C-0022`'s design state — 10 nm gap, `0.192 V`, its solved collar read from `T-3b`; cross-section **`10 × 6`**; **route B's** uniform raster with 59 per-turn **tethers**, paired rows **`92 / 98 / 106 bp`** (`scaffoldNucleotides / 60 − 28`, M13mp18 / p7560 / p8064); `edgeX` = `31.28 / 33.32 / 36.04 nm`, `edgeY` = `38.04 nm`; `d` = 2.536 nm (SAXS), rise 0.34 nm/bp, phosphate radius `0.908637858 nm` (`T-71`); `b₀` = `5 / 16 / 9` and the **worst** of `C-0211`'s twelve chain corners at each, both read out of its committed cells; ladder phase **derived** at `7 / 0 / 0` with `C-0141`'s forced `14 bp` stagger; transverse link pinned at `C-0205`'s ceiling `254.808095 pN/nm`, headline radial rung `C-0208`'s bracket **floor** `754.005141` (through-thickness `629.20588`); composite fraction **0.30** with 0.26 at the deciding cell; `C-0017`'s mandate **`33.3333333 pN/nm` on the SUM**; grading seed `197197` at **4 000** realisations, training seed `316316` at **120**, disjoint; `81 × 81` dishing grid, search grid 41; `subdivisions = 1`; rim band 6.7 nm; `T-5b` = 0.10 |
| **Consumes** | [`C-0211`](C-0211-the-uniform-raster-at-the-resolved-link.md)/`T-315` (the uncoupled reference at every cell — the recommended `b₀`, the worst corner and the dishing, **reproduced to `8.1e−10`**), [`C-0212`](C-0212-a-searched-distribution-at-the-resolved-link.md)/`T-316` (the search, and its own 32 cells as the transferred ratio band **and** the paired comparands), [`C-0208`](C-0208-a-bond-link-is-two-mechanisms.md)/`T-310` (the per-bond link and the radial bracket, **recomputed**), [`C-0205`](C-0205-what-link-stiffness-the-recovery-needs.md)/`T-303` (the shear ceiling, **recomputed**), [`C-0207`](C-0207-the-uniform-raster-is-flat-with-its-tethers.md), [`C-0201`](C-0201-the-tether-is-a-load-not-a-spring.md)/`T-299` (the element unchanged), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (the four placements and the grading construction), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the station ladder and its forced stagger), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (the measured depth incorporation, **re-derived at each width's own edges**), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0135`](C-0135-descent-manifold-width.md), [`C-0089`](C-0089-dropout-robust-placement.md), [`C-0104`](C-0104-row-end-prestrain.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0023`](C-0023-two-sided-coupling.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0193`](C-0193-the-built-turn-is-a-tether.md), [`C-0200`](C-0200-the-file-draws-and-the-table-orders.md), [`C-0022`](C-0022-tile-edge-load-profile.md)/`T-3b`, [`CH-0270`](../challenges/CH-0270-the-uniform-raster-does-not-carry-435-bonds.md), [`CH-0272`](../challenges/CH-0272-a-flat-count-is-not-an-admissible-design.md), [`CH-0273`](../challenges/CH-0273-the-buildable-ratio-window-is-a-flatness-window.md) |
| **Raises** | [`CH-0276`](../challenges/CH-0276-a-coupled-census-inherited-a-station-set.md), [`CH-0277`](../challenges/CH-0277-a-ratio-transferred-between-two-lattices.md) |

---

## The claim, in five lines

Every coupled number in this repository is read on the **`116 bp` block extent** of route A's
drawable raster. The tile the programme recommends is **route B's**, and `C-0211` shows its
**free** tile is flat at `756 of 756`.

**Graded coupled on route B's own tile, at stations derived at each row length**, a *transferred*
rule is flat at **`0 of 48`** cells and a *searched* one at **`27 of 48`**, tightest
**`0.0687939715`** of the stroke against the transferred **`0.100227515`**.

**`7 of 48` are flat AND inside `C-0023`'s per-path allowable** — the conjunction `CH-0272` says a
verdict block must state, and the first non-empty one this corpus has had.

**`0 of 48` beat the uncoupled tile.** On route B the coupling is not a flatness remedy; it is a
mandate the flatness has to survive, and 27 cells survive it.

**The station set could not be inherited, and one page of integer arithmetic says so before any
solve**: 5 station columns against the block's 6, and the inherited phase refuses the column count
that carries 12 of the 27 flat cells.

## 1. The cheap bound that is pure lattice arithmetic, and it decided two things

`honeycombStationsOnHelix(L, p) = (L − p)/21 + 1`, minimised over the two staggered row parities.
No solve, no ensemble, no lattice assembled:

| row | at `T-316`'s inherited phase `16` | derived phase | best `min_row(stations)` |
|---|---|---|---|
| `92 bp` | **4** | `7` | **5** |
| `98 bp` | **4** | `0` | **5** |
| `106 bp` | 5 | `0` | **5** |
| `116 bp` (route A's block extent) | 5 | `7` | **6** |

Two readings fall out and both were declared as falsifiers before the run.

**Route B's rows carry five station columns where the block extent carries six, at every one of
the 21 phases.** A shorter row is not merely a smaller tile; it is a tile with one fewer place to
stand — and the fifth column is not decoration: **12 of the 27 flat cells are 5-column
placements** (1 / 3 / 11 / 12 flat at 1 / 2 / 3 / 5 columns).

**And at the inherited phase the fifth column does not exist at `92` and `98 bp`** —
`honeycombSnappedGrid` **refuses** it, because a placement wider than a row's own ladder is a
change of the path **count** wearing a change of position. `F12` fired at `2 of 3` widths. The
phase is free on route B precisely because `C-0141`'s `±5 bp` rule fixes it only where the raster
**closes**, and route B's uniform rows close at no phase; `16` is a **route-A** number.

## 2. The answer, and the conjunction beside it

| over the 48 cells | count |
|---|---|
| flat at the 90th percentile on a **transferred** rule | **0** |
| flat on the **searched** rule | **27** |
| `peakInsideUnzipCeiling` (`C-0023`, `3.33333333 pN/nm`) | 7 |
| **flat AND admissible** | **7** |
| `beatsUncoupledAtP90` | **0** |
| all three | **0** |
| inside `C-0060`'s `[3.5, 20]` — a **FLATNESS** window (`CH-0273`), emitted and not gated on | 9 |
| flat, admissible **and** inside that window | 2 |

*(counts over this study's own `cells[*]` records)*

| | |
|---|---|
| tightest transferred | **`0.100227515`** |
| tightest searched | **`0.0687939715`** — 92 bp, abstract grid, `5 × 10 = 50` paths |
| the uncoupled route-B tile at `92 / 98 / 106 bp` | `0.0521565503` / `0.0576976711` / `0.0523876952` |
| cells where the search wins **out of sample** | **48 of 48**, gain `1.08747732`–`1.67588391×` |
| searched `p90` over the sweep | `0.068793971`–`0.124434728` |
| transferred `p90` over the sweep | `0.100227515`–`0.188211567` |

**The seven flat, admissible cells are six 5-column placements and one 3-column**, spread over all
three widths — `92 bp` twice, `98 bp` three times, `106 bp` twice — at peak per-path stiffnesses
`2.64052496`–`3.24270994 pN/nm`, all under the `3.33333333` allowable.

## 3. `0 of 48` beat the uncoupled tile, and that is the sentence that must travel

`C-0212` read `0 of 32` on the `116 bp` block and `CH-0272` named it; the direction **transfers**
and the level does not. What is different on route B is that the uncoupled tile is *already flat*
— `C-0211`'s `756 of 756` — so the comparison stops being a disappointment and becomes the frame:

> On route B the mandated coupling is not there for flatness. It is `C-0017`'s output requirement,
> and the question is whether the flatness survives it. At 27 of 48 cells it does, and at 7 of
> those the per-path force is admissible too.

The best searched cell is `1.31909477×` the uncoupled reading at its own width; the sweep's best
is `0.0687939715` against an uncoupled `0.0521565503`. **No coupling in this corpus, on either
tile, has ever improved on doing nothing** — and `allThreeThresholds` is `0 of 48` for exactly that
reason.

### And `0 of 48` is NOT an argument for removing the coupling

This is the sentence a reader will take away, and it is the one most easily got wrong, so it is
stated here rather than left to be inferred from a count.

`C-0017`'s `33.3333333 pN/nm` is a **placement and stability** requirement, not a flatness one: it
is the equality that puts §3's 100 pN at its acceptable 3 nm stroke, and `C-0018`'s fold needs
`k_c > |k_eff|` on top of it. **A tile with no coupling delivers no output at all**, so the
uncoupled reading is not a rival design — it is the *reference* `CLAUDE.md` requires be run beside
every coupled one, and nothing more.

What `0 of 48` says is therefore narrow and exact: **on route B's tile, flatness is not what the
coupling buys.** `C-0211` has already bought it, free, at `756 of 756`. The coupled question is
whether the flatness *survives* the mandate, and the measured answer is that it does at
**27 of 48** cells and survives it *admissibly* — inside `C-0023`'s per-path allowable — at **7**.

Read the other way round, the same three numbers say what a designer should do: **take the
coupling, spend the mandate, and choose the cell** — not drop the coupling to save a dishing the
device cannot use without one.

## 4. The transferred ratio band was a prediction, it missed, and it missed ONE-SIGNED

`C-0212`'s own `searchedP90 / uncoupled` over its 32 cells is `1.4438156`–`2.7106587×`, and its
best-transferred ratio `2.2359`–`3.5094×`. Applied to `C-0211`'s committed uncoupled readings:

| row | predicted **transferred** | predicted **searched** | measured tightest searched |
|---|---|---|---|
| `92 bp` | `0.116616839`–`0.183039806` | `0.075304442`–`0.141378609` | **`0.068793971`** |
| `98 bp` | `0.129006232`–`0.202485986` | `0.083304799`–`0.156398696` | **`0.069137604`** |
| `106 bp` | `0.117133656`–`0.183850993` | `0.075638173`–`0.142005164` | **`0.075715489`** |

**The VERDICT it predicted is exactly right**: it said *excludes flat* on a transferred rule and
the measured census is `0 of 48`; it said *straddles* on a searched rule and 27 of 48 clear.

**The LEVEL is not, and the misses are one-signed.** `8 of 48` searched cells fall **below** the
band and **0** above; on the transferred rule `14` fall below and `1` above. So a ratio measured on
the `116 bp` block is **conservative** on route B's own tile — route B does better than the
transfer predicts, at every miss — which is `CH-0277`, and it is the measurement that turns
*"the stations belong to a different tile"* from an objection into a number.

## 5. The tile substitution is worth `10 of 48` verdicts and `0.839625798`–`1.34239093×`

Paired against `C-0212`'s own cells at the same `(placement, columns, fraction)`:

| | |
|---|---|
| paired cells | 48 (each route-B cell against the `116 bp` block's) |
| the flat verdict **moves** | **10** |
| route B's searched `p90` over the block's | **`0.839625798`–`1.34239093×`** |
| flat on the block | 33 of 48 |
| flat on route B | 27 of 48 |

So the tile is worth up to **`1.34×`** on the deciding statistic and it moves the verdict at a
fifth of the cells — in **both** directions. A coupled census read on the block extent is not a
census of route B's tile, and this is `CH-0276`.

## 6. The three widths rank differently coupled than free

| | ordering |
|---|---|
| `C-0211`'s **uncoupled** free tile | `92 < 106 < 98` (`0.0521565503 < 0.0523876952 < 0.0576976711`) |
| this study's **coupled** best per width | `92 < 98 < 106` (`0.068793971 < 0.069137604 < 0.075715489`) |

`98` and `106` exchange. `F19` was declared OPEN and fired: **a free-tile ranking does not predict
the coupled one**, which is `CLAUDE.md`'s own *a placement is tuned to a LOAD FIELD, not to a tile*
read on the row length. All three widths are flat at `9 of 16` cells each, so the *count* is
identical and only the *level* moves.

## 7. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a dishing dimensionless as a fraction of the free stroke of the **same** lattice at the **same** rung; a per-path stiffness in pN/nm against an allowable in pN/nm; a ratio dimensionless; a station count an integer; the mandate an equality on a **sum** in pN/nm | **PASS** |
| **2 — limiting cases** | the ladder refuses a non-positive row, a face with no rooting helix, a negative stagger, a zero period and a phase outside it; the band refuses a non-positive or inverted pair and a non-positive uncoupled reading or threshold; an unknown rung is refused rather than returning an empty reference list; the conjunction is a conjunction | **PASS**, 19 named tests |
| **3 — symmetry and the standing falsifiers** | a uniform pressure on the **free** route-B tethered lattice at the resolved link, **per width**, because two of the three rows carry a `nodeS` overhang and one does not (`F1`); the null-radial rung **bit-identical** to `UniformRasterTethers.lattice` on `assembleLoad` over every degree of freedom and on the crossover **site set**, at all three widths (`F2`); the surrogate at full presence against the **assembled** solve with its own Woodbury support forces as point loads (`F3`); the two surrogates against each other (`F5`); the tethered and untied free strokes (`F8`) | **PASS** |
| **4 — numerical convergence** | nine axes, all at the **deciding cell**: the search's own dishing grid (41 → 81, `0.017`), the verdict's grid (81 → 41 and 161, `0.0` at both), the **training** realisations (120 → 60, `0.088`; → 240, `0.037`), beam subdivisions (1 → 2, `0.013`), the descent's sweeps (2 → 3, `0.056`), the composite fraction (0.30 → 0.26, `0.029`) and `C-0141`'s stagger (14 → 7 bp, `0.0`). **`0 of 9` move the verdict** | **PASS** |
| **5 — literature and upstream** | **fourteen reproductions, worst departure `1.9e−9`**: `C-0211`'s uncoupled free tile at all three widths (`1.4e−10`, `2.3e−10`, `8.1e−10`) and its own published `bestWorstCornerDishing` at `0.0`; `CH-0270`'s bond census `358 / 385 / 410` exactly; `C-0205`'s shear ceiling; `C-0208`'s radial floor and its through-thickness link; `C-0017`'s mandate; `C-0023`'s allowable. Every closed form is the corpus's own function, **called** rather than re-implemented | **PASS** |

### The twenty declared falsifiers

| # | falsifier | fired |
|---|---|---|
| `F1` | a uniform pressure on the free route-B tethered lattice dishes more than `1e−9` of the stroke at any width | **no** |
| `F2` | the null-radial lattice is not bit-identical to the standing object | **no** — load vector and site set identical at all three widths |
| `F3` | the surrogate does not reproduce the assembled solve at `1e−9` | **no** |
| `F4` | two independent runs do not produce a byte-identical result file | **no** — §11 |
| `F5` | the two surrogates disagree by more than `1e−10` | **no** |
| `F6` | the uncoupled reading does not reproduce `C-0211`'s committed cell to `1e−8` | **no** — worst `8.1e−10` |
| `F7` | the bond census is not `358 / 385 / 410`, or `⟨unitZ²⟩` is not `0` / `0.75` | **no** |
| `F8` | the tethered and untied free strokes differ by more than `1e−9` | **no** |
| `F9` | a searched `p90` falls below the oracle floor | **no** — `0 of 48`; it is a theorem |
| `F10` | the in-sample objective is worse than the best of its own starts | **no** — `0 of 48` |
| `F11` *(open)* | **the headline.** A searched distribution puts some route-B cell inside `T-5b` | **FIRED** — `27 of 48` |
| `F12` *(open)* | the inherited ladder phase cannot carry a 5-column placement at some width | **FIRED** — `2 of 3` |
| `F13` *(open)* | **no** cell is flat AND inside `C-0023`'s allowable | **did not fire** — `7 of 48` are both |
| `F14` *(open)* | **no** coupled cell beats the uncoupled route-B tile | **FIRED** — `0 of 48` beat it |
| `F15` *(open)* | a flat cell loses the verdict to one missing path | **no** — `0 of 27`, amplification `1.0695`–`1.6773` |
| `F16` *(open)* | the verdict at the deciding cell moves across the rung ladder | **no** — FLAT at all six rungs, `0.0630983759`–`0.0687939715` |
| `F17` *(open)* | the out-of-sample `p90` is worse than the best transferred at any cell | **no** — the search wins at `48 of 48` |
| `F18` *(open)* | the flat verdict differs from `C-0212`'s paired verdict | **FIRED** — `10 of 48` |
| `F19` *(open)* | the widths rank differently coupled than uncoupled | **FIRED** — `98` and `106` exchange |
| `F20` *(open)* | a searched `p90` falls outside the cheap bound's predicted band | **FIRED** — `8 of 48`, all **below** |

## 8. The oracle floor excluded nothing, and that is a measurement of the room the search had

`InfluenceSurrogate.reachableDishingFloorAt` over every realisation is a **pointwise** lower bound
on the peak dishing of every distribution whatever: `0.00128199096`–`0.00391494906` of the stroke
over the 48 cells, excluding **`0`** outright, with the best transferred distribution sitting
**`37.4404449`–`97.7322378×`** above it. `CLAUDE.md`'s statement that such a floor *can EXCLUDE and
can never ADMIT* is reproduced here on a third lattice — and the two orders of magnitude of slack
are why the search was worth running.

## 9. Fragility, and the two-level projection

At all **27** flat cells, the worst **single-path removal** keeps the verdict — `F15` did not fire —
with amplification over the zero-defect reading of `1.0695`–`1.6773×`. Quantised onto `C-0060`'s own
**two levels**, **16 of the 27** stay flat: the searched distribution is not a knife edge, and the
two-level projection costs about two fifths of the flat cells rather than all of them.

The ratio the argmin demands runs `6.51322935`–`780.707822` and the peak per-path stiffness
`2.64052496`–`25.6278197 pN/nm`. `C-0060`'s `[3.5, 20]` is emitted at every cell and **named a
flatness window** (`CH-0273`); the threshold gated on is `C-0023`'s.

## 10. The mutation test, and its two survivors were one real gap

Twenty mutations of `tile/RouteBCoupled.kt`, every one required to fail a **named** test, with the
unmutated copy run first and its failures subtracted (`CH-0237`), `find src -name` asserted to
return exactly one path (`C-0190`), every anchor asserted to occur exactly once (`C-0185`), and the
`-x` flags **derived** from `build.gradle.kts`'s own `dependsOn` block (`C-0194`).

**First run: 18 killed, 2 SURVIVED** — `M08` and `M09`, the two constructor `require`s of
`RouteBStationLadder`. Both for one reason: **`honeycombStationLattice` carries the same two
requirements verbatim and `derivedPhase`'s own initialiser reaches it**, so a widened guard here
still throws — from downstream, with the **same message**, at the same construction call.
`C-0207` §8's *a guard whose only observable behaviour is duplicated downstream is a guard no
mutation of it can reach*, met on a **third** object after `C-0211` §9's second.

The repair is in the guard's **wording** and in the fixture: the messages now name this class
(*"a route-B station ladder needs a positive rowBasePairs"*), and the test asserts the message and
not merely the type. **After it: 20 mutations, `0` survivors over a subtracted baseline of `0`.**

## 11. `F4` — two runs, byte-identical, and it also prices the repair

The guard-message repair of §10 landed **after** the first full emission, so the second run
discharges two things at once: `F4`'s byte-identity, and the proof that changing a string inside a
`require` lambda that never fires cannot move an emitted number.

Run A and run B were emitted in two **separate** `tools/study.sh` snapshots — 70 minutes apart, on
a box carrying a concurrent agent's own study — and diffed **outside** the study, against a copy of
run A saved before run B was launched:

| | |
|---|---|
| `cmp` | exit **0**, no differing byte |
| SHA-256, both files | `051afbd8e7fc00dd97670687a578ff70d1fec929671ab7e333609d5ecc904e4a` |
| size, both files | `169573` bytes |
| `tools/study.sh`'s own copy-back | *"no result file changed"* |

**The verdict is taken from the diff and not from the copy-back.** *"Nothing was copied back"* is a
statement about `cmp` inside the script and is only as good as the snapshot's baseline; the
sibling reading in the same iteration went the other way (a concurrent study's file moved by two
bytes and duly copied back), so the signal is not vacuous — but the artifact-level `cmp` and the
hash are what discharge `F4`, and they are stated here rather than inferred.

**Run B is the run at the COMMITTED source**, the guard-message repair included, so the committed
result file is reproducible from the committed code and the repair is measured inert rather than
argued inert.

## 12. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **No cell is flat, admissible AND better than the uncoupled tile** — `allThreeThresholds` is `0 of 48`. The 7 admissible cells are a coupling the flatness *survives*, not one it *wants*. **That is not a case for removing the coupling** (§3): `C-0017`'s mandate is a placement and stability requirement and an uncoupled tile delivers no output, so the uncoupled reading is a **reference** and never a candidate design.
- The **placement** is not searched. `C-0063` records that which stations a coupling enters at is worth more than how its stiffness is distributed, and route B's ladder is one column narrower than the block's, so the placement axis is *smaller* here and the question is sharper, not settled.
- The **ladder phase** is derived by one stated rule and not swept. A different rule gives a different placement; the stagger row of the convergence block (`0.0`) is what that is worth at the deciding cell and at no other.
- `T-315`'s 21 lattice phases and 12 chain corners are **not** re-swept: each width is graded at `C-0211`'s own recommended `b₀` and at the **worst** of its twelve corners there.
- The composite fraction `0.26` and `C-0141`'s `7 bp` stagger are carried at the **deciding cell only**, named in `T-322`'s Plan before the run and not silent caps.
- The **count** of flat cells is out of sample; **which** cell is tightest is an order statistic over 48 read on the grading stream and carries a selection the count does not.
- The radial constant is **unsourceable** (`C-0208` §6); the transverse one is pinned at `C-0205`'s ceiling, its generous reading.
- Every bond is still missing `CH-0242`'s common-mode spring; the lattice carries no steric floor; `k_θ` is `Gen1Tile`'s square-lattice-fitted constant; and the nine in-plane raster turns contribute exactly zero preload because this model has no in-plane transverse coordinate — a property of the **model**.

## 13. Still open — named, not answered

- **A joint search over the placement and the distribution on route B's tile.** `T-323` opens it on the block; route B's narrower ladder is a different question.
- **What the station ladder phase is worth as a design variable on a coupled cell.** It is derived here by one rule and swept nowhere.
- **Whether a shared-body topology** — `C-0017`'s mandate spent once in a rigid-body mode rather than at every station — changes the answer. It is a change of TOPOLOGY, and the dishing projector annihilates a rigid-body mode by construction.
- **Whether route B should trade paired row length against tether span at all.** These three widths are the maximum each scaffold affords, and a shorter row buys a smaller tile and spends a station column.
