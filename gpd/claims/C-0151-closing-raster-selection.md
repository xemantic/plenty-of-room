# C-0151 — **`C-0140`'s SELECTION FILTER AND SCAFFOLD CLOSURE ARE EXACTLY DISJOINT, and the drawable raster is `102 / 109 bp` — at the same 39.44 nm width, for the price of ONE crossover column.** Closure depends on the two row lengths only through their residues modulo 21, so **441** cases settle the family with no solve: **three** residue pairs close, they are the same on both 60-helix cross-sections, and **every one of them has `L₁ − L₂ ≡ 14 (mod 21)`** — so the minimum stagger a **drawable** two-length honeycomb raster can carry is **7 bp = 2.38 nm**, and `C-0140`'s *"at most 4 bp"* filter could not have returned a buildable design at any length. Selected inside the closing family, `102 / 109` is jointly best on axial extent (**116 bp = 39.44 nm, `−1.40 %`** — the *same* extent as the pair it replaces) and strictly best among the three pairs that tie there: **10** crossover columns against 9 and 6, **55** stations against 50 and 40, a 7 bp stagger against 14 and 28. The two rasters give the **same tile**, so the whole cost of closure on the flatness axis is the **interface window**, 102 bp against 108, and with it **one** crossover column: **2 flat cells of 8** against 3, and **the recommended cell survives at both ends of the measured band** — `0.0773373597` at `f = 0.30` and `0.0821458169` at `f = 0.26`, against `T-5b`'s 0.10. The ladder phase is **determined at 16** with the 14 bp offset, and closure **buys** 270 nt of scaffold and takes the blunt-end stacking clearance from `0.18` rises — unquotable — to **3.18**

| | |
|---|---|
| **Task** | [`T-245`](../tasks/T-245-closing-raster-selection.md) — re-select the honeycomb two-length row pair on scaffold closure, and re-grade the coupled cells there |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (exact integer arithmetic on the crossover-residue lattice, **exhaustive** over residue pairs modulo 21) **+ in-silico** (`C-0142`'s influence surrogate and Monte Carlo dropout grading, 4 000 realisations on **one** common stream restricted per cell) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The closure rule is caDNAno's published **default**; the raster is a lattice statement; the folding statistics graded against are measured and the flatness is not. No folded object is measured here. |
| **Verdict** | **PASS on all five predicates. `F1`, `F2`, `F5`, `F6` and `F8` did not fire; `F3` and `F4` were declared OPEN and did not fire; `F7` FIRED, at 2 of 24 paired rows, both at cells where the effect is inside 1.3 %.** `P1` the closing family is enumerated exhaustively over the 441 residue pairs at both cross-sections and reproduces `C-0148`'s five-pair verdict and its three closing classes; `P2` every scoring axis is emitted for **every** family member (144 records), so the recommendation is a selection; `P3` every re-graded cell carries its uncoupled reference, its `f`, its column count and its path count, and the column reading is swept 10 / 11 / 12; `P4` the comparison is paired, one stream, read per realisation; `P5` the scaffold and unpaired-nucleotide budgets are re-derived at the recommended lengths against `C-0147`'s 8 nt. |
| **Provenance** | [`gpd/results/T-245-closing-raster-selection.json`](../results/T-245-closing-raster-selection.json) (`tile.HoneycombClosingRasterStudyKt`, **new**); model [`tile/HoneycombClosingFamily.kt`](../../src/main/kotlin/tile/HoneycombClosingFamily.kt) (**new file** — `tile/HoneycombBondClassResidues.kt` was **read, not edited**, and supplies every residue used here); **23 gate-named tests** in [`tile/HoneycombClosingFamilyTest.kt`](../../src/test/kotlin/tile/HoneycombClosingFamilyTest.kt), **written first and watched fail**, and **mutation-tested afterwards**: collapsing the minimum-stagger fold, reading the column window off the row span instead of the interface, dropping the two path ends from the scaffold, and swapping the two face parities each fail **1, 3, 1 and 1** named tests, and the restored source passes 23 of 23. Result file **BYTE-IDENTICAL across two independent JVM runs**. `tools/verify.sh` **BUILD SUCCESSFUL in 22 m 25 s** on its own isolated tree with two concurrent agents' new sources in it — **no `--drop-file` needed** — and its post-build gates all clean: `result-reader-census.py --check` (123 studies, 126 direct + 27 transitive edges), `check-markdown-tables.py` (0 defects over 498 files), `check-corpus-links.py` (0 over 491), `check-kotlin-format-strings.py` (0 over `src`), `check-challenge-index.py` (172 of 172 indexed), and `check-result-file-hygiene.py` with `--departures` and `--saturated`. `tools/T-234-census.py --check` goes **10 defects → 3**, the three remaining being a sibling's. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2`; rise 0.34 nm/bp; 21 bp per interface; crossover-column pitch `21 × 0.34 / 2` = 3.57 nm. Cross-section `10 × 6` (design (ii), 60 helices), four-layer plate rigidities, `15 × 4` carried for the closure sweep and the selection cross-check. Closing family enumerated over row lengths **60–200 bp** at a stagger of at most **42 bp**. `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, 2 beam subdivisions, `T-5b`'s 0.10; `f` = 0.26 and 0.30 (`C-0116`'s measured band). **64 cells graded, 46 reproductions, worst departure `4.1e−9`.** |
| **Consumes** | [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) (`HoneycombRasterResidues`, the closure predicate, the 14 bp offset and the row-span column reading), [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) (the path, the turn senses, the level walk and the width family), [`C-0146`](C-0146-coupled-cells-at-the-two-length-raster.md) (the eight cells re-graded here, **read from its result file** and reproduced at `≤ 4.1e−9`), [`C-0142`](C-0142-coupled-cells-at-the-honeycomb-cross-section.md) (the grading machinery, untouched), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the cross-section, the plan-ceiling bisection), [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md) (the 8 nt allowance, the blunt-end onset, `CH-0187`'s four axes), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0069`](C-0069-output-element-placement.md), [`C-0129`](C-0129-result-file-hygiene.md) |
| **Constrains** | **`CH-0188` is ANSWERED** — the drawable pair is selected inside the closing family rather than inherited from `C-0140`'s five, and it is `102 / 109`. **`CH-0189` is ANSWERED** — the phase is determined at 16 and the design is graded there. **`CH-0187` is ANSWERED in its own terms** — its four axes are re-scored on the closing family and the axis it could not have had (closure) settles the choice. **Two challenges are raised.** [`CH-0194`](../challenges/CH-0194-the-filter-and-closure-are-disjoint.md) against `C-0140`'s *"the remedy costs 3 base pairs"* and against the status of its stagger filter; [`CH-0195`](../challenges/CH-0195-both-graded-column-counts-belong-to-an-undrawable-raster.md) against `C-0146` §3's guard finding and `C-0148` §4b's selection of the 11-column reading. |

---

## 1. The cheap bound, and it settled the family before any solve

**Closure depends on the two row lengths only through their residues modulo 21.** A raster
crossover's reduced residue is `(level − 7·class) mod 21`; every level is an integer combination
of the two lengths on a **fixed** class sequence; and `C-0148`'s condition is a statement about
that residue set. So the whole family lives in **441** cases per cross-section, and enumerating
them is exact rather than a search. That is asserted rather than assumed — the test walks all 441
at two different length representatives of each residue and requires the verdicts to agree.

**Three residue pairs close, and they are the same on both 60-helix cross-sections:**

| `L₁ mod 21` | `L₂ mod 21` | `L₁ − L₂ (mod 21)` | least `\|L₁ − L₂\|` |
|---|---|---|---|
| 7 | 14 | **14** | **7** |
| 17 | 3 | **14** | **7** |
| 18 | 4 | **14** | **7** |

**Every closing pair carries the same length difference modulo 21.** So `|L₁ − L₂| ∈ {7, 14, 28,
35, …}` and the **minimum stagger a drawable two-length honeycomb raster can carry is 7 bp =
2.38 nm** — where `C-0140`'s `F9` reports a minimum of **3 bp**, correctly, as a **per-helix**
minimum (`C-0136`'s row-length rule applied one helix at a time).

**`C-0140` selected its recommendation under a filter of *"a stagger of at most 4 bp"*.** The
filter and the closing family are therefore **disjoint**: that rule could not have returned a
drawable raster at any pair of lengths whatever. `CH-0188` found four of `C-0140`'s five
candidates undrawable and the fifth — `102 / 109`, at a 7 bp stagger — is precisely the one the
filter had already excluded. **That is `CH-0194`**, and it is one line of modular arithmetic.

The verdict is convention-free: identical over **128** readings (8 length pairs × 2 cross-sections
× first axial sign × mirror × axial datum) and at both cross-sections. `F5` did not fire.

## 2. The selection, inside the closing family

Enumerated over row lengths 60–200 bp at a stagger of at most 42 bp, the `10 × 6` path carries
**68** closing length pairs, of which **29** fit M13. The selection rule is stated once and
applied at both cross-sections: **among the pairs that close and fit M13, take the best
`|extent − 40 nm|`; break the tie on crossover columns, then on the station census, then on the
stagger.** Every axis is emitted for every one of the 144 family records, so this is a selection
and not an assertion.

The best `|extent − 40 nm|` in the closing family is **1.40 %**, and **three** pairs reach it:

| pair | stagger | extent | departure | interface window | **columns** | stations | scaffold on `10 × 6` | M13 spare | front relief |
|---|---|---|---|---|---|---|---|---|---|
| 60 / 88 | 28 bp | 116 bp | `−1.40 %` | 60 bp | **6** | 40 | 4 440 nt | 2 809 | 28 bp |
| 102 / 88 | 14 bp | 116 bp | `−1.40 %` | 88 bp | **9** | 50 | 5 700 nt | 1 549 | 14 bp |
| **102 / 109** | **7 bp** | **116 bp** | **`−1.40 %`** | **102 bp** | **10** | **55** | **6 330 nt** | **919** | **7 bp** |

**The tie is broken three times in the same direction.** A wider stagger at the same extent buys
scaffold and spends **interface window** — the window is `rowSpan − stagger`, exactly — so it
spends crossover columns and stations together. `102 / 109` is strictly best on both.

The next-best extent is 115 bp (`101 / 108`, `−2.25 %`, 45 stations) and the next after that
126 bp (`112 / 119`, `+7.10 %`, 11 columns, 55 stations). **`F1` did not fire**: nothing in the
closing family inside M13 beats 116 bp.

**The same rule on `15 × 4` returns the same pair**, 82 of 90 stations — `selectionIsCrossSectionFree`
is `true`.

### 2a. `CH-0187`'s four axes, re-scored, plus the two it could not have

| axis | `102 / 109` | `112 / 108` | winner |
|---|---|---|---|
| **scaffold closure**, forced raster crossovers of 59 | **0** | 10 | **`102 / 109`** — a **rule**, not a preference |
| axial extent against §3's nominal 40.0 nm | `−1.40 %` | `−1.40 %` | **TIE** — closure costs nothing here |
| scaffold on M13's 7 249 nt (`10 × 6`) | **6 330 nt** | 6 600 nt | **`102 / 109`** |
| front-face relief past the blunt-end stacking onset | **3.176 rises** | 0.176 rises | **`102 / 109`** |
| front-face relief against `C-0141`'s **saturated** plan ceiling | 2.38 nm | **1.36 nm** | `112 / 108` |
| **row-derived crossover columns** | 10 | **11** | `112 / 108` |
| stations at the **determined** phase | **55 of 60** | *none — no phase is determined* | **`102 / 109`** |

Five axes to two, and the two it loses are both bounded. The **plan-ceiling** axis is
`CH-0187`'s own, and it binds only at **saturation** — which the determined phase makes
unreachable: the sparsest row carries **5** stations, so a placement is capped at five columns
(50 paths) and the plan ceiling there is **4.604 nm**, comfortably above the 2.38 nm relief. The
**column** axis is the whole cost of closure and §4 measures it.

`C-0069`'s recommended 8.16439083 nm output element is afforded at **10 and 20 paths** on the
determined lattice and refused at 30 — the same verdict boundary `C-0141` reports at `112 / 108`,
which affords 20 and refuses 30.

## 3. The stacking margin becomes quotable, and the loop route gains headroom

`CH-0187` records that `112 / 108` clears the blunt-end stacking onset by **0.06 nm = 0.18 of a
rise**, i.e. below the design language's own quantum, so by `CLAUDE.md`'s rule **not a quotable
margin at all**. At `102 / 109` the front-face relief is 7 bp = **2.38 nm** and the clearance is
**1.08 nm = 3.176 rises**.

Route A spends **6 330 nt** of M13's 7 249 on `10 × 6`, **919** spare against `112 / 108`'s 649 —
so **closure buys 270 nt**. Read as `C-0147`'s own unpaired-nucleotide allowance the recommended
lengths afford **15 nt per helix** against **10** at `112 / 108` and **8** at its uniform 112 bp
row: the loop route, which `C-0147` finds strained at 8 nt (past the 10 pN unzip allowable), gains
headroom rather than losing it. `F8` did not fire.

## 4. What closure costs the flatness: exactly one crossover column

**`C-0140`'s pair and the recommended one have the same block extent, 116 bp = 39.44 nm.** So at
the width §3 is owed they are the **same tile** — graded at the same column count all eight cells
agree below `1e−10`, which is what a **solved** field of this lattice can be asserted to (two
identically constructed grillages differ by a few ulp; `CLAUDE.md`). What differs is the
**interface window**, 102 bp against 108, and with it the row-derived column count, **10 against
11**.

`10 × 6`, abstract grid, at the measured `f = 0.30` (`T-5b`'s 0.10; **bold** is flat at `p90`):

| columns | paths | distribution | **`102 / 109`, 10 col** | `112 / 108`, 11 col | `112 / 108`, 12 col (the box) |
|---|---|---|---|---|---|
| 1 | 10 | equal springs | **0.0773373597** | **0.0708759349** | **0.0662801686** |
| 1 | 10 | rim-graded 5:1 | 0.11075597 | 0.104654401 | **0.0998334915** |
| 2 | 20 | equal springs | 0.137611877 | 0.125509341 | 0.116688801 |
| 2 | 20 | rim-graded 5:1 | 0.188690218 | 0.174594445 | 0.16373126 |
| 3 | 30 | equal springs | 0.117473795 | 0.107278473 | **0.0997830457** |
| 3 | 30 | rim-graded 5:1 | 0.109744899 | 0.100357905 | **0.0938556471** |
| 5 | 50 | equal springs | 0.103404517 | **0.0946671181** | **0.0880177483** |
| 5 | 50 | rim-graded 5:1 | **0.0921821694** | **0.0855380627** | **0.0805842317** |
| | | **flat of 8** | **2** | **3** | **6** |

At the band's adverse low end `f = 0.26` the recommendation is **2 of 8** against `112 / 108`'s
3 of 8, and the recommended cell is **0.0821458169** against 0.0754995025. The uncoupled
references are **0.0281953496** (`f = 0.30`) and **0.0299114053** (`f = 0.26`) at 10 columns,
against 0.0252615047 and 0.0268332278 at 11 — both flat, and **no graded cell beats its own
uncoupled tile**, which is unchanged from `C-0142` and `C-0146`.

> **The recommended design — one column, ten paths, equal springs — survives at the drawable
> raster, at both ends of the measured band.** `F3` was declared open and did not fire.

**What is lost is `C-0142`'s tightest cell, for the third time and now for a structural reason.**
The 3-column rim-graded cell is `0.0938556471` at twelve columns, `0.100357905` at eleven and
**0.109744899** at ten: `C-0146` found it decided by a **numerical guard**, and at the drawable
pair the guard decides nothing at all — the interface window gives **10** columns at all three
`EDGE_MARGIN` conventions (0.05, 0.17 and 0.34 nm), slack 2.45 / 2.21 / 1.87 nm. **That is `CH-0195`.**

### 4a. The determined station lattice

`CH-0189`'s point is that the ladder phase is not a menu. At `102 / 109` it is **determined at 16**
with the **14 bp** inter-row offset, carrying `5, 6, 5, 6, …` — **55 of 60** stations. Graded
there, at the same 10 columns and `f = 0.30`:

| columns | paths | equal springs | rim-graded 5:1 |
|---|---|---|---|
| 1 | 10 | **0.0868025325** | 0.110684428 |
| 2 | 20 | 0.133831373 | 0.191098254 |
| 3 | 30 | 0.121463683 | 0.11965968 |
| 5 | 50 | 0.102307224 | **0.0943125609** |
| | **flat of 8** | | **2** |

**No graded column count is refused** — `F4` was declared open and did not fire — and five
columns is exactly the sparsest row's capacity, so a **six**-column placement does not stand at
the recommended pair. Snapping the recommended cell onto the determined lattice costs **12 %** at
`p90` (0.0773373597 → 0.0868025325) and it stays flat.

### 4b. `CH-0189`'s open item, and the answer is a PRICE rather than a negative

`CH-0189` asks whether *some* closing pair's determined phase saturates the station census, which
would restore the six-column placement `CH-0184` reported and `CH-0189` withdrew. **Exactly one
does inside M13 on `10 × 6`**: `123 / 109 bp`, 60 of 60 stations at 6 per row, determined phase 2,
11 crossover columns — and its axial extent is **137 bp = 46.58 nm, `+16.45 %`** on §3's 40.0 nm.
**No saturating closing pair inside M13 comes within 5 % of the nominal width.** So a six-column
honeycomb placement exists and is drawable; it costs a tile sixteen per cent too wide.

## 5. The paired readings

The three comparisons are read on **one** common stream — same seed, same deviate per station per
realisation — so each cost is readable per realisation:

| comparison | median of the per-realisation ratios | ratio of the two 90th percentiles |
|---|---|---|
| what **closure** costs (10 columns over 11) | **1.0567397 – 1.09611647** | 1.05830207 – 1.09642737 |
| what the **determined lattice** costs (over the abstract grid) | **0.989077433 – 1.2668452** | 0.972527776 – 1.1223881 |
| the **bounding-box** column reading over the row-derived one | **0.846385737 – 0.904393789** | 0.847955883 – 0.901382489 |

**`F7` FIRED, at 2 of 24 rows**, both in the lattice comparison and both at cells where the effect
is inside 1.3 % — a median of `1.00080489` against a percentile ratio of `0.99935406`, and
`0.989077433` against `1.01276185` — so the two statistics straddle unity from opposite sides. Same shape and same direction as `C-0146`'s own `F7`, and
neither row is a flatness verdict.

**Closure costs a uniform `1.0567397`–`1.09611647`× of the dishing** and the box reading buys a
uniform `0.846385737`–`0.904393789`×.
`CLAUDE.md`'s *"a ratio of two order statistics is not the order statistic of the ratio"* is
respected throughout; the largest disagreement between the paired and unpaired readings here is
the 1-column lattice cell, **1.2668452** paired against **1.1223881** unpaired.

## 6. The five gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | residues are integers modulo 21 base pairs; axial positions integer base pairs on one global `z`; windows converted at the rise and asserted equal to their own base-pair count; columns dimensionless; dishing a fraction of the free stroke; a non-positive row length is **refused** |
| **limiting cases** | a **uniform** row length closes at no residue (21 of 21), which is `C-0140`'s negative from the residue side; the block extent is asserted `2·max − min` and the interface window `rowSpan − stagger` at five pairs; the determined station census from `HoneycombRasterResidues` and from `TwoLengthRaster.stationLattice` are **two independent constructions** and agree row for row |
| **symmetry and conservation** | the closure verdict is invariant over 128 readings — 8 pairs × 2 cross-sections × first axial sign × mirror × axial datum — and the closing residue set is identical at both cross-sections; every distribution sums to the mandated total exactly; the two column readings share **one** dropout stream, which is what makes the difference a design difference |
| **numerical convergence** | dropout realisations 1000 / 2000 / 4000 on the common stream, departure **`0.0`** between the last two — a convergence rather than a variance, the stream being restricted and not redrawn. `EDGE_MARGIN` swept 0.05 / 0.17 / 0.34 nm as a **guard**, and it is **inert** at the recommended pair's interface window (10 columns at all three). The residue enumeration is exact integer arithmetic and has no convergence axis. The whole file is **byte-identical across two independent JVM runs** |
| **literature cross-check** | the `±5 bp` rule is quoted from the primary source (Douglas et al., *NAR* **37**:5001, `PMC2731887`, in `gpd/data/T-151-sources/`, **read directly**) and consumed through `C-0148`'s model rather than re-implemented. **46 reproductions at `≤ 4.1e−9`**: `C-0148`'s `b₀` = 5, phase 16, offset 14, 55 stations and 10 forced crossovers at `112 / 108`; `C-0140`'s 116 bp extent, 6 337 nt scaffold and 7 / 14 bp faces at the recommended pair, and its 6 596 nt at `112 / 108`; **32 of `C-0146`'s own graded cells** at three readings plus its two-length lattice column and three uncoupled references; and `C-0147`'s 8 nt at a uniform 112 bp row |

**The statistic.** None of the 64 cells' `exceedance` proportions is saturated, so `C-0129`'s
one-sided bound is `null` throughout and the symmetric `√(p̂(1−p̂)/n)` is the right instrument —
checked rather than assumed.

**The smoke run is not the result.** At 150 realisations the reproductions ran to a worst
departure of **0.19** and three flat verdicts differed; nothing but the sample count changed, and
the smoke pass was read only for the prose paths, the argmins and the serialisation.

## 7. What this means

- **The recommended honeycomb raster is `102 / 109 bp`**, at a determined ladder phase of **16**
  and the 14 bp inter-row offset, on a `10 × 6` block of extent **116 bp = 39.44 nm** carrying
  **10** crossover columns and **55** stations. It is drawable on caDNAno's default rules with
  **zero** forced crossovers.
- **The width finding does not move.** `C-0140`'s `−1.40 %` is the *closing* family's best too,
  and it still beats the square lattice's `−4.80 %` and `C-0133`'s `−6.50 %`. Closure is free on
  the one axis `C-0140` scored.
- **The whole cost of closure, on the flatness axis, is one crossover column**, because the two
  pairs give the same tile. One flat cell of eight, and the recommended coupled design is not it.
- **`C-0140`'s minimum stagger of 3 bp is not the minimum a design can build.** That is 7 bp, and
  the filter that selected `112 / 108` excluded every pair that could have been drawn.
- **Two of `C-0146`'s standing puzzles dissolve rather than resolve.** Its guard finding and its
  open question about the twelfth column are both read at a raster that cannot be drawn; at the
  one that can, the guard is inert and the count is 10.

### 7a. What `C-0152` says about the other side of the trade, filed in the same iteration

[`C-0152`](C-0152-forced-scaffold-crossover-price.md) (`T-246`, concurrent) prices a **forced**
scaffold crossover for the first time: `0.350894669 k_BT` at its ceiling — **sub-thermal** — and
the whole of `CH-0188`'s ten-crossover forcing at `0.438634952` of **one crossover column** of the
host sheet's own demonstrated currency. Its own conclusion is that *"scaffold closure is a **reason
to prefer 102/109** — a free improvement on a hard rule — and **not a proof that 112/108 is
unbuildable**"*, and that the unpriced risk is **kinetic**.

**That is the same recommendation reached from the opposite side, and the two results compose
into one trade rather than cancelling.** `112 / 108` buys one crossover column — one flat cell of
eight, and a uniform `1.0567397`–`1.09611647`× of the dishing — for ten forced crossovers, an
elastic price that is sub-thermal and a kinetic price nobody can quote. `102 / 109` pays that
column and is on-rule. **What the closure axis is entitled to is the word *free*, not the word
*impossible*** — and this claim's `axes` table calls closure *"a rule, not a preference"*, which
`C-0152` narrows to a rule whose **elastic** violation is cheap. Both claims agree on the
recommendation and neither, on its own, can close the kinetic question.

## 8. Validity range, and what this does NOT establish

- **The `±5 bp` rule is caDNAno's DEFAULT, not a law.** The same paper permits forcing a
  crossover between any two scaffold bases and warns that departure from the default rules *"may
  lead to folding failure if too much deviation from canonical DNA geometry is implied"*. A
  non-closing raster is **off-rule and buildable**, not impossible. `C-0152` (`T-246`, filed in
  the same iteration) prices the **elastic** half at `0.350894669 k_BT` per forced crossover and
  finds it sub-thermal; the **kinetic** half — folding yield — is still unpriced by anybody, and
  it is the half caDNAno's own warning is about.
- **The half turn is 5.25 bp and caDNAno writes 5.** Every residue inherits that rounding, and it
  is the source's own.
- **The residue enumeration is exhaustive; the LENGTH enumeration is not.** The family is swept
  over 60–200 bp at a stagger of at most 42 bp; a pair outside that box is not excluded, though
  the extent identity `2·max − min` makes anything near 40 nm reachable inside it.
- **The tile is a SMEARED equivalent sheet with one `lengthX`**, so the 7 bp inter-row axial
  stagger — *larger* than `C-0140`'s 4 — is not representable at all. Only the interface
  **window** carries it, through the column count.
- **The grillage is single-layer and square-lattice in its crossover combinatorics** (`C-0141`'s
  standing caveat, unchanged); a multi-layer honeycomb face enters only as a smeared equivalent
  sheet, and Kirchhoff is not safe at these thicknesses (`C-0109`, `C-0120`).
- **The dropout statistics are measured on a single-layer Rothemund rectangle** and only the
  *profile* transfers, in nm (`C-0087`, `C-0109`).
- **The station census counts ONE face**, the one pointing away from the grafted layer.
- **The 7 bp stagger changes the ragged-face geometry `C-0147` priced at 4 bp.** Its coefficient
  on §3's flatness is exactly zero (the raggedness is on the **rim**), and the residual rim
  modulation bound scales with the relief — but it has not been re-run at 7 bp here.
- **Nothing here measures a folded object.** The whole selection is lattice arithmetic on a
  published design rule.
- **`tools/result-reader-census.py --emit` was NOT run** (a shared file, with two siblings' studies
  in flight); `--check` is clean and reports this study and `tile/ForcedCrossoverPriceStudy.kt` as
  the two not yet in the emitted census. `T-241` owns that file.

## 9. Still open — named, not answered

1. **What a FORCED scaffold crossover costs in folding YIELD.** `C-0152` closes the elastic axis
   (sub-thermal) and records a negative existence result for the published literature over 68
   queries in 7 families; the kinetic axis is the one `112 / 108` would have to be defended on and
   it remains open. If forcing is kinetically free, the trade is one crossover column against
   nothing and `112 / 108` returns.
2. **Whether the ragged-face verdict survives the 7 bp relief.** `C-0147` priced 4 bp and found
   a coefficient of exactly zero on §3's flatness; the rim modulation bound is proportional to
   the relief and was 496× clear, so the margin is ample, but it is not re-run.
3. **Whether `C-0089`'s percentile descent recovers the flat cell the tenth column loses.**
4. **Which width reading a folded block is owed against §3** — unchanged, because both pairs give
   the same 116 bp box; but the row span moves, 112 → 109 bp.
