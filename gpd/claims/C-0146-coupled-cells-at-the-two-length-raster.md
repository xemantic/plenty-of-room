# C-0146 — **`C-0142`'s recommended coupled design SURVIVES `C-0140`'s two-length raster, and the width that threatened it is not a width.** Every x-raster row of the `10 × 6` block is **still 112 bp**; the 116 bp = **39.44 nm** block extent is a **4 bp = 1.36 nm inter-row STAGGER**, which a single-`lengthX` plate cannot represent at all. What the raster actually costs is **stations** — the 21 bp ladder becomes row-dependent, `C-0142`'s own phase carries **55 of 60**, and exactly **one** of 42 `(phase, offset)` pairs keeps all sixty, at the **14 bp offset `C-0141` says nothing depends on** — and a **numerical guard**: the bounding box clears a **twelfth** crossover column by **0.07 nm**, one fifth of a base-pair rise, and that column alone is the difference between **6 flat cells of 8** and **3**

| | |
|---|---|
| **Task** | [`T-235`](../tasks/T-235-coupled-cells-at-the-two-length-raster.md) — re-grade the corrected `10 × 6` coupled cells at `C-0140`'s two-length raster |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (an exact integer construction of the two-length raster's axial levels, face windows and station census, derived from `C-0140`'s own path and turn machinery) **+ in-silico** (`C-0142`'s influence surrogate and Monte Carlo dropout grading, 4 000 realisations on **one common stream**) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The *folding* statistics graded against are measured; the flatness is not, and the two-length raster is a **lattice** statement (`C-0140`) — a design this repository derives, not one anybody has built. |
| **Verdict** | **PASS on all five predicates. `F1`, `F2`, `F3` and `F4` did not fire; `F5`, `F6` and `F7` FIRED, and all three were declared for exactly this.** `P1` the axial geometry is derived from `C-0140` rather than re-derived; `P2` every `10 × 6` cell is re-graded at both ends of the band with the uncoupled reference beside it; `P3` the 112 bp reading reproduces `C-0142` **in the same process** at `≤ 3.9e−9` and the comparison is per realisation; `P4` the census is delivered at both offsets and all 21 phases and no graded placement is refused; `P5` the column count is swept over `EDGE_MARGIN`. |
| **Provenance** | [`gpd/results/T-235-coupled-cells-at-the-two-length-raster.json`](../results/T-235-coupled-cells-at-the-two-length-raster.json), produced by `tile.HoneycombTwoLengthStudyKt`; model [`tile/HoneycombTwoLengthRaster.kt`](../../src/main/kotlin/tile/HoneycombTwoLengthRaster.kt), tests [`tile/HoneycombTwoLengthRasterTest.kt`](../../src/test/kotlin/tile/HoneycombTwoLengthRasterTest.kt) (**14**, written first and **mutation-tested**: moving the lattice datum off the block centre, taking the face helix from the wrong end of the row, widening the station window from the row's own to the block's, and rounding instead of flooring the column count each fail **4, 2, 2 and 1** of them). Geometry consumed unmodified from [`structure/HoneycombRasterTurnSense.kt`](../../src/main/kotlin/structure/HoneycombRasterTurnSense.kt) (`C-0140`) and [`tile/HoneycombFaceLattice.kt`](../../src/main/kotlin/tile/HoneycombFaceLattice.kt) (`C-0141`); the grading is [`tile/HoneycombCoupledTile.kt`](../../src/main/kotlin/tile/HoneycombCoupledTile.kt) (`C-0142`), untouched. `tools/verify.sh` **BUILD SUCCESSFUL in 21 m 57 s** on its own isolated tree with two concurrent agents' new sources in it — **no `--drop-file` needed** — and a full `./gradlew test` on the final sources gives **2 879 tests, 0 failures, 0 errors**. `tools/check-markdown-tables.py` (0 defects over 469 files), `check-corpus-links.py` (0 over 462), `check-kotlin-format-strings.py`, `check-challenge-index.py` (160 of 160 indexed), `check-result-file-hygiene.py` and its `--departures` and `--saturated` passes, and `result-reader-census.py --check` all clean; the census's only note is the advisory that this study and two siblings' are not yet in the emitted `P-22` file, which `T-241` owns. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb bond length `d` = 2.536 nm; in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; 21 bp per interface; rise 0.34 nm/bp. Two-length raster **112 bp at effective sense 1, 108 bp at effective sense 2**, first axial sign `+1`, unmirrored. `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, 2 beam subdivisions, `T-5b`'s 0.10. **96 cells graded**; the result file is **byte-identical across two independent JVM runs**. |
| **Consumes** | [`C-0142`](C-0142-coupled-cells-at-the-honeycomb-cross-section.md) (the eight `10 × 6` cells this re-reads, its `15 × 4` controls and its grading machinery), [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) (the two-length raster, its turn senses and its 116 bp extent), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the cross-section and the single-length station lattice), [`C-0116`](C-0116-composite-fraction-threshold.md) (the measured 0.26–0.33 band), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0129`](C-0129-result-file-hygiene.md), [`C-0103`](C-0103-path-count-at-fixed-geometry.md) |
| **Constrains** | **Two challenges are raised.** [`CH-0184`](../challenges/CH-0184-the-inter-row-offset-stops-being-free.md) against `C-0141` §9's *"no answer here depends on the choice"* of the 7-or-14 bp inter-row offset; [`CH-0185`](../challenges/CH-0185-a-bounding-box-crossover-column.md) against `CrossoverLayout.EDGE_MARGIN`'s standing inertness sentence and against reading a four-layer flatness at a **bounding-box** `edgeX`. `C-0142` §8's *"the ROW LENGTH is carried unchanged at 112 bp … a re-grade at 39.44 nm is the successor"* is **DISCHARGED**. |

---

## 1. The cheap bound, and it changed the question before any Monte Carlo

`C-0140`'s path, turn senses and level walk are exact integer arithmetic and already in the tree.
Walked over the real `10 × 6` raster at 112 / 108 bp — microseconds — they say four things:

| | |
|---|---|
| the face's rooting helices | alternate sense **exactly** with the raster-row parity: even rows sense 1, **112 bp**, window `[−112, 0]`; odd rows sense 2, **108 bp**, window `[−112, −4]` |
| **every x-raster row** | spans **112 bp**, at all ten rows — the row unions are `[−112, 0]` and `[−116, −4]` |
| the block's extent | **116 bp = 39.44 nm**, and it is 116 **only because the rows are staggered by 4 bp = 1.36 nm** |
| the across-helix attachment pitch | `edgeY / rasterRows`, **untouched** by a row-length move |

**So the `+3.57 %` is not a row length, and the width question splits into two readings that are
both defensible and are not the same tile:**

- the **row-faithful** one — `edgeX` = 112 bp = 38.08 nm, which is `C-0142`'s own tile, re-run in
  the same process and reproducing all 28 of its published cells at `≤ 3.9e−9`;
- the **bounding-box** one — `edgeX` = 116 bp = 39.44 nm, the dimension §3 is owed for an object.

A smeared plate has **one** `lengthX`, so the 4 bp inter-row stagger is not representable in
either. That is why both are carried and neither is preferred.

**And this is not a property of the pair `C-0140` selects.** Over all five pairs its own
Deliverable 4 table carries, at **both** 60-helix cross-sections:

| pair | every raster row | block extent | stagger |
|---|---|---|---|
| **112 / 108 bp** (`C-0140`'s recommendation) | **112 bp** | 116 bp | 4 bp |
| 101 / 109 bp | 109 bp | 117 bp | 8 bp |
| 102 / 109 bp | 109 bp | 116 bp | 7 bp |
| 112 / 109 bp | 112 bp | 115 bp | 3 bp |
| 122 / 119 bp | 122 bp | 125 bp | 3 bp |

Every row spans the **larger** of the two lengths, exactly, and the block extent exceeds it by
exactly the stagger. **No two-length raster lengthens a row at all**, so `CH-0187`'s re-selection
of the pair cannot overturn the reading — only change the stagger.

## 2. What the raster does cost: the station ladder becomes row-dependent

Because the two face parities carry different **windows**, the 21 bp ladder is no longer one
object. Swept over all 21 phases and both of `C-0141`'s admissible inter-row offsets:

| | stations on the face, of 60 | per row |
|---|---|---|
| the uniform 112 bp raster at `C-0142`'s phase 0 / 7 bp | **60** | 6 everywhere |
| the two-length raster at phase 0 / **7 bp** | **55** | `5, 6, 5, 6, …` |
| the two-length raster at phase 0 / **14 bp** | **50** | 5 everywhere |
| the two-length raster, best of 42 pairs | **60**, at **phase 11 / 14 bp**, and at that pair alone | 6 everywhere |
| the two-length raster, worst of 42 pairs | 50 | |

**Every graded column count (1, 2, 3, 5) is still realisable at every pair** — `F4` did not fire,
0 of 48 `(configuration, column count)` pairs refused — so the raster costs **stations**, not
**paths**. A **six**-column placement is a different matter: it is refused at 41 of the 42 pairs
and admitted at one.

`15 × 4` behaves the same way, saturating at **90 of 90** at the *same* unique pair (11 / 14 bp)
and running down to 75 elsewhere.

**And that is `CH-0184`.** `C-0141` §9 records the 7-or-14 bp offset as a convention *"this
repository cannot yet say which"*, adding that *"no answer here depends on the choice"*. At a
two-length raster it does: the choice moves the station count by **55 against 50** at one phase,
and the only phase that keeps the full inventory lives at the **14 bp** offset.

## 3. The numerical guard, and it decides three cells of eight

A 116 bp extent clears **eleven** honeycomb crossover pitches by **0.07 nm** — one fifth of a
base-pair rise. Swept:

| width reading | `edgeX` | `EDGE_MARGIN` | columns | slack past the last pitch |
|---|---|---|---|---|
| row length 112 bp | 38.08 nm | 0.05 / 0.17 / 0.34 nm | **11 / 11 / 11** | 2.28 / 2.04 / 1.70 nm |
| block extent 116 bp | 39.44 nm | **0.05 nm** (standing) | **12** | **0.07 nm** |
| block extent 116 bp | 39.44 nm | 0.17 nm (half a rise) | **11** | 3.40 nm |
| block extent 116 bp | 39.44 nm | 0.34 nm (one rise) | **11** | 3.06 nm |

`CrossoverLayout.EDGE_MARGIN`'s KDoc still certifies itself inert — *"far below the 0.28 nm
closest approach any base-pair phase makes on a 40 nm tile, so it never decides a column count
that the physics does not already decide"*. It decides one here, and **that is `CH-0185`**;
`C-0134` found the same sentence false at 38.08 nm on the square lattice, and this is the second
geometry.

**The two effects of the wider box are OPPOSED, and the guard picks which wins.** On the
uncoupled tile at `f = 0.30`:

| | uncoupled dishing, fraction of the free stroke |
|---|---|
| 112 bp, 11 columns (`C-0142`) | **0.0240648102** |
| 116 bp, **11** columns — the width alone | **0.0252615047** (`+4.97 %`, adverse) |
| 116 bp, **12** columns — the guard's extra column | **0.0231299291** (`−8.44 %` on the line above, favourable, and **below** the 112 bp reading) |

**Every raster row is 112 bp**, so eleven is the column count a *row* can carry and twelve is the
count the *bounding box* admits. The twelfth column is a property of the box.

## 4. The eight cells, re-read

`10 × 6`, abstract grid, at the measured `f = 0.30` (`T-5b`'s 0.10; **bold** is flat):

| columns | paths | distribution | 112 bp / 11 col (`C-0142`) | 116 bp / **11** col | 116 bp / **12** col |
|---|---|---|---|---|---|
| 1 | 10 | equal springs | **0.0680677948** | **0.0708759349** | **0.0662801686** |
| 1 | 10 | rim-graded 5:1 | 0.102582764 | 0.104654401 | **0.0998334915** |
| 2 | 20 | equal springs | 0.119502047 | 0.125509341 | 0.116688801 |
| 2 | 20 | rim-graded 5:1 | 0.168817101 | 0.174594445 | 0.16373126 |
| 3 | 30 | equal springs | 0.101905503 | 0.107278473 | **0.0997830457** |
| 3 | 30 | rim-graded 5:1 | **0.0954158305** | 0.100357905 | **0.0938556471** |
| 5 | 50 | equal springs | **0.0900369** | **0.0946671181** | **0.0880177483** |
| 5 | 50 | rim-graded 5:1 | **0.0822611821** | **0.0855380627** | **0.0805842317** |
| | | **flat of 8** | **4** | **3** | **6** |

At the band's adverse low end `f = 0.26` the same three readings give **4 / 3 / 4**, and the
recommended cell is **0.072431426 / 0.0754995025 / 0.0708859619** — flat at all three.

> **The recommended design — one column, ten paths, equal springs — survives its own buildable
> width at both ends of the measured band and at both column-count readings.** `F3` was declared
> open and did not fire; it is written on the twelve-column reading alone, and the result file's
> `recommendationSurvivesItsBuildableWidth` is the conjunction over all four states — `true`.

**What does not survive is `C-0142`'s tightest cell**, and it is exactly the one `C-0142` named:
the 3-column rim-graded cell, `0.0954158305` at 112 bp, goes to **0.100357905** at 116 bp on
eleven columns — across the tolerance — and to `0.0938556471` on twelve. At `f = 0.26` it goes
`0.0968178426 → 0.102199442 / 0.0954321322`. **One flat cell of four is decided by a 0.07 nm
slack against a numerical guard.**

`15 × 4` is **0 of 8** at both widths, as it was at `C-0142`'s: its best cell moves
`0.145354102 → 0.141713508`. The direction the task asked for does not move.

## 5. On the station lattice, and the equal-spring monopoly breaks

Snapped onto the two-length station lattice (largest snap **3.332 nm**, inside the 3.57 nm
half-ladder ceiling), at `f = 0.30`. `C-0142`'s column is its own — 112 bp on eleven crossover
columns, reproduced here at `≤ 3.9e−9` — and the two two-length columns are 116 bp on twelve:

| columns | distribution | `C-0142`, single-length lattice | two-length, phase 0 / 7 bp | two-length, phase 11 / 14 bp |
|---|---|---|---|---|
| 1 | equal springs | **0.0863028445** | **0.0835390491** | **0.0831416174** |
| 1 | rim-graded 5:1 | 0.111376749 | 0.105363807 | 0.10496691 |
| 2 | equal springs | 0.125476912 | 0.121589624 | 0.122256183 |
| 2 | rim-graded 5:1 | 0.183045719 | 0.175216395 | 0.176293602 |
| 3 | equal springs | **0.0973238201** | 0.100550512 | 0.100937943 |
| 3 | rim-graded 5:1 | 0.14299002 | 0.129314053 | 0.129665937 |
| 5 | equal springs | **0.0868937148** | **0.0874939277** | **0.0865671607** |
| 5 | rim-graded 5:1 | 0.108415983 | **0.0968448829** | **0.0971571422** |
| | **flat of 8** | **3, all equal springs** | **3** | **3** |

**`F6` fired, and the direction is informative.** `C-0142`'s finding that *"every one of the three
is EQUAL SPRINGS"* does not survive: the **best** cell is still equal springs at every reading,
but the 5-column rim-graded cell becomes flat (`0.108415983 → 0.0968448829`) while the 3-column
equal-spring cell is lost (`0.0973238201 → 0.100550512`). The two-length lattice moves stations
**across** `C-0058`'s rim band by a different amount than the single-length one did, and a
distribution rule remains **a property of a station set, never of a tile**. On **eleven** columns
the same lattice is 2 of 8, both equal springs.

## 6. The paired reading, and it disagrees in sign three times

The two widths are graded on **one** common stream — same seed, same path count, same deviate per
station per realisation — so the width's cost is readable per realisation:

| | median of the per-realisation ratios | ratio of the two 90th percentiles |
|---|---|---|
| `10 × 6`, abstract grid, eight cells | **0.956433922 – 1.01639081** | 0.969873658 – 0.983648589 |
| `10 × 6`, station lattice, eight cells | **0.873617528 – 1.0238607** | 0.893271268 – 1.03315418 |

**`F7` fired at 3 of 24 rows** — and it was declared as a falsifier because a *sign* disagreement
means the summary does not describe its sample. All three are cells at which the width is worth
essentially nothing and the two statistics therefore straddle unity from opposite sides, at a
spread of **0.043, 0.034 and 0.048**:

| | median of ratios | ratio of percentiles |
|---|---|---|
| `10 × 6`, abstract grid, 1 column, equal springs | 1.01639081 | 0.973737563 |
| `15 × 4`, abstract grid, 1 column, equal springs | 1.02566918 | 0.991605148 |
| `10 × 6`, two-length lattice, 5 columns, equal springs | 0.958704735 | 1.00690744 |

**All three are one-column-per-row or five-column equal-spring cells, and none is a flatness
verdict that turns on the width.** The largest genuine effect anywhere is the 5-column rim-graded
lattice cell, **0.873617528** paired against **0.893271268** unpaired — `CLAUDE.md`'s *"a ratio of
two ORDER STATISTICS is not the order statistic of the ratio"*, again, and again in the direction
that matters.

## 7. The five gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | axial positions integer base pairs on one global `z`, lengths nm, stiffness pN/nm, dishing dimensionless; the lattice's extreme stations are asserted to straddle zero and to fit inside the block extent; the plate `edgeY` is asserted equal to `rasterRows ×` the in-plane pitch in the tile's own `init` |
| **limiting cases** | **equal lengths return `C-0141`'s `honeycombStationLattice` position for position**, over 21 phases × 2 offsets, worst departure `3.6e−15` nm; a one-row raster carries one sense and its only face helix is a path end, which is **refused** rather than extrapolated; a placement wider than the sparsest row's ladder is refused; an odd raster-row count puts a path end on the face and is flagged |
| **symmetry / conservation** | every distribution sums to the mandated total exactly; the influence bank is load-independent so one bank serves both distributions of a cell; the two widths share **one** dropout stream, which is what makes the difference a design difference; the face's sense alternation with the row parity is asserted at all ten rows |
| **numerical convergence** | dropout realisations 1000 / 2000 / 4000 on the common stream, departure **0.0** between the last two — a convergence rather than a variance, the stream being restricted and not redrawn. **`EDGE_MARGIN` swept 0.05 / 0.17 / 0.34 nm**, which is a *guard* and not a convergence parameter and is carried on the convergence axis for exactly that reason. The whole file is **byte-identical across two independent JVM runs** |
| **literature cross-check** | **35 reproductions**: `C-0142`'s 28 published cells at `≤ 3.9e−9` (16 abstract-grid at `f = 0.30`, 4 at `f = 0.26`, 8 lattice-snapped), three uncoupled references at `≤ 7.5e−10`, and four of `C-0140`'s integers — the 116 bp extent and the 28 / 30 / 29 sense census — at departure **`0.0`**. Nothing new is cited |

**The statistic.** None of the 96 cells' `exceedance` proportions is saturated, so `C-0129`'s
one-sided bound is `null` throughout and the symmetric `√(p̂(1−p̂)/n)` is the right instrument —
`0.0005586675` to `0.00790403279` over the whole set.

**The falsifiers.** `F1` (the 112 bp reading does not reproduce `C-0142`) did not fire — worst
`3.9e−9`. `F2` (the generalisation is not one) did not fire — `3.6e−15` nm. **`F3`** (the
recommendation loses `T-5b`), declared **open**, did not fire. `F4` (a column count is refused)
did not fire — 0 of 48. **`F5` FIRED**, declared open: the guard moves the column count and with
it three cells of eight. **`F6` FIRED**, declared open: the equal-spring monopoly on the lattice
breaks. **`F7` FIRED**: 3 of 24 paired rows disagree in sign, all three at cells where the effect
is inside 3 %.

**The smoke run is not the result.** At 200 realisations `F1` fired (worst departure 0.19) and the
flat sets differed; nothing but the sample count changed. `CLAUDE.md`'s rule was followed and the
smoke pass was read only for the prose paths, the argmins and the serialisation.

## 8. What this means

- **`C-0142`'s recommendation stands at its own buildable width.** One column, ten paths, equal
  springs on `10 × 6`: `0.0662801686` at 12 columns, `0.0708759349` at 11, and `0.0708859619` /
  `0.0754995025` at the band's low end. Flat at every reading.
- **The `+3.57 %` was never a row length.** It is a 4 bp inter-row stagger, and the honest
  statement of what a two-length raster does to a *plate* is that the plate cannot represent it.
- **What it does cost is inventory**: five of sixty stations at `C-0142`'s own ladder phase, and
  a **unique** phase/offset pair if the full sixty are wanted.
- **`15 × 4` is unchanged and still has no coupled design**, 0 of 8 at both widths. Every reason
  `C-0141` and `C-0142` give for `10 × 6` survives.

## 9. Validity range, and what this does NOT establish

- **The raster is a LATTICE statement** (`C-0140`): no folded object is measured, and the
  two-length assignment is a design this repository derives rather than one anybody has built.
- **The two ENDS of the scaffold path carry no defined turn sense** (`C-0140`), so their length is
  free. `10 × 6`'s face is clear of both; `15 × 4`'s row 14 face helix **is** the path terminus and
  its window is extrapolated from the defined face rows of its own parity.
- **The block extent is taken over the INTERIOR helices**, which is `C-0140`'s own convention; the
  two terminal helices could extend it further.
- **The tile is a SMEARED equivalent sheet with ONE `lengthX`**, so the 4 bp axial stagger is not
  representable at all, and neither width reading is preferred here. **Which one §3 is owed is a
  specification question this claim does not settle**, and the two differ by 3.57 %.
- **The crossover-column count is read as an arithmetic on `edgeX`**, which every four-layer study
  in this corpus does; nothing here re-derives what a *staggered* row lattice actually carries.
- **The dropout statistics are measured on a single-layer Rothemund rectangle** and only the
  *profile* transfers, in nm (`C-0087`, `C-0109`). The wider tile changes the perimeter-to-area
  ratio again.
- **The mandate is read at §3's ACCEPTABLE clause** (100 pN / 3 nm).
- **Two distributions only** — equal and `C-0058`'s rim rule. `C-0089`'s percentile descent and
  `C-0093`'s shared body are not run.
- **`C-0022`'s collar is read unchanged** at the wider aspect ratio; `C-0123`'s question is
  reopened again rather than answered.
- **The length PAIR is `C-0140`'s recommendation and it is under challenge.** `CH-0187` (`T-231`,
  in flight in the same iteration) argues that `C-0140`'s *"stagger of at most 4 bp"* filter is
  unstated and that 101 / 109 bp wins on three axes of four. §1's family table is why nothing
  here turns on it: every pair in that family gives every row the larger length and every block
  the larger length plus the stagger. Only the **stagger** and the block extent move — 3 to 8 bp —
  and the graded cells are read at 112 / 108.
- **Kirchhoff is not safe at these thicknesses** (`C-0109`, `C-0120`): every `D_∥` is an upper
  bound.

## 10. Still open — named, not answered

1. **Which width reading a folded block is owed against §3** — the bounding box the object
   occupies, or the row length its beams carry. They differ by 3.57 % and by three flat cells of
   eight.
2. **What the 4 bp inter-row axial stagger costs a flatness model that can represent it.** No
   model in this repository can.
3. **Which ladder phase and which inter-row offset a caDNAno honeycomb carries.** It now decides a
   station **count**, not only a position (`CH-0184`).
4. **Whether the twelfth crossover column exists on a staggered 112 bp row lattice.** The plate
   says yes at a 0.07 nm slack; the rows say no (`CH-0185`).
