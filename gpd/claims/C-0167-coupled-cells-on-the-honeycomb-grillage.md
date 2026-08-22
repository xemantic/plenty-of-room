# C-0167 — **Every coupled cell in this corpus is a smeared single-layer square-lattice solve, and re-graded on the honeycomb lattice the recommended coupled design FAILS: `C-0151`'s `0.0773373597` is `0.145086839`, its *"2 flat cells of 8"* is `0` of 8, and `0` of `64` cells clear `T-5b` where the smeared sheet gives `15`.** The correction is **1.876×** at the recommended cell and it is **not a multiplier** — the per-realisation median ratio runs **1.064 to 2.475** and one cell reads **0.966** — so no table can be rescaled. **What fails is the COUPLING, not the block**: the uncoupled four-layer honeycomb tile is flat at **0.0501417315** and **0.0522223659** at the two ends of the measured band, and the recommended cell is flat at **0.0626407003** with no defects at all — it is `C-0087`'s measured staple dropout, acting on a tile whose across-helix rigidity is `24/7` smaller than the corpus believed, that takes it past the tolerance at **4 000 of 4 000** realisations. And the port cost one function: `influenceSurrogate` was already written against a model-agnostic interface, so the whole re-grade is a **point-load dual** and a fifteen-line adapter, with the smeared half reproducing `C-0151`'s four published percentiles at **`≤ 6.3e−10`** in the same process

| | |
|---|---|
| **Task** | [`T-263`](../tasks/T-263-honeycomb-grillage-regrade.md) — re-grade the coupled honeycomb cells on the honeycomb GRILLAGE, not on a smeared sheet |
| **Leaf** | `A8.2` |
| **Verification type** | **in-silico** (a three-dimensional beam-and-bond lattice solve, an exact Woodbury coupling surrogate, and `C-0087`'s measured incorporation as a Bernoulli dropout over 4 000 realisations on **one common stream restricted per cell**) **+ logical** (an exact algebraic identity between the surrogate and the assembled solve, and a crossover census that costs no solve) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The *folding* statistics graded against are measured; the flatness is not. The cross-section, the raster and the crossover lattice are **lattice** statements this repository derives, not measurements of a folded object. |
| **Verdict** | **PASS on all five predicates. `F1`, `F2`, `F3`, `F4` and `F6` did not fire; `F5` FIRED, and it was declared expected to.** `P1` all eleven reproductions close at `≤ 1.7e−9`; `P2` all 64 recommended cells are re-graded at both ends of the band against `C-0151`'s own count on the same cells; `P3` the uniform-load falsifier holds at `< 1e−9` on the coupled lattice with the tributaries centred on their own beams; `P4` the comparison is paired, one stream, read per realisation; `P5` two convergence axes are emitted and the identity fields are emitted as thresholds. |
| **Provenance** | [`gpd/results/T-263-honeycomb-grillage-regrade.json`](../results/T-263-honeycomb-grillage-regrade.json) (`tile.HoneycombGrillageRegradeStudyKt`, **new**); model [`tile/HoneycombCoupledLattice.kt`](../../src/main/kotlin/tile/HoneycombCoupledLattice.kt) (**new file**) and one added function plus one repaired node list on [`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt). **`coupling/NonUniformCoupling.kt`, `coupling/DropoutRobustPlacement.kt`, `tile/HoneycombCoupledTile.kt`, `tile/HoneycombTwoLengthRaster.kt`, `tile/FourLayerTile.kt` and `structure/OrigamiGrillage.kt` were READ, NOT EDITED** — which is why nothing `C-0142`, `C-0146` or `C-0151` published can move. **17 gate-named tests written first and watched fail** — [`tile/HoneycombCoupledLatticeTest.kt`](../../src/test/kotlin/tile/HoneycombCoupledLatticeTest.kt) — and **mutation-tested afterwards**: zeroing the dual's roll entries, restoring the pre-repair node list, and reversing the influence load's sign each fail **2, 2 and 2** named tests, and the restored sources pass 17 of 17. Result file **BYTE-IDENTICAL across two independent JVM runs**, after one same-quantity identity field was re-emitted as a **threshold and a boolean** because two runs disagreed in exactly that field and nowhere else. A full `./gradlew test` on the final sources gives **3 158 tests, 0 failures, 0 errors**; `check-markdown-tables.py` (0 defects over 555 files), `check-corpus-links.py` (0 over 552), `check-kotlin-format-strings.py`, `check-challenge-index.py` (190 of 190 indexed), `check-corpus-identifiers.py` (0 dangling over 532), `check-result-file-hygiene.py` with `--departures`, `--saturated` and the `--prose` gate, and `result-reader-census.py --check` are all clean; `check-entry-points.py` is red on this study's missing `TASKS.md` row, which the coordinator owns. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp; crossover planes every **7 bp**, one pair per class every **21 bp**. Cross-section `10 × 6` (60 helices), block extent **116 bp = 39.44 nm** at `C-0151`'s recommended `102 / 109` raster, `edgeY` = 38.04 nm. `k_θ` = 13.5294118 pN·nm/rad, `k_s` = 64.7058824 pN/nm, link penalty `1e4` pN/nm; 4 320 unknowns, 435 bonds, half-bandwidth 243. `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the **gap-facing face only**; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Composite fractions **0.30** and **0.26** (`C-0116`'s measured band, entering as `hingeStiffnessEnhancement` 21.1851817 and 18.4938242) plus the lattice's own **1.0** lower bound. Honeycomb subdivisions 1 (headline), 2 (convergence); the smeared half at `C-0151`'s own 2. |
| **Consumes** | [`C-0154`](C-0154-honeycomb-grillage.md) (`HoneycombGrillage`, the `24/7` overstatement, the dimer census, the three free-tile readings reproduced here), [`C-0151`](C-0151-closing-raster-selection.md) (the recommended `102 / 109` raster and the cells re-graded, **read from its result file**), [`C-0146`](C-0146-coupled-cells-at-the-two-length-raster.md) (the 116 bp extent and the two-length station lattice), [`C-0142`](C-0142-coupled-cells-at-the-honeycomb-cross-section.md) (the surrogate and the dropout grading, untouched), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0103`](C-0103-path-count-at-fixed-geometry.md) (common random numbers) |
| **Constrains** | **Two challenges are raised.** [`CH-0213`](../challenges/CH-0213-the-recommended-coupled-design-does-not-survive-the-honeycomb-grillage.md) against `C-0151`'s flatness headline and, through the same mechanism, `C-0146`'s, `C-0142`'s and `C-0118`'s; [`CH-0214`](../challenges/CH-0214-the-honeycomb-lattice-stopped-at-its-last-crossover-plane.md) against `HoneycombGrillage`'s unstated row-length precondition, **raised and repaired in the same iteration**. `C-0154` §10's second open item — *"whether a COUPLED honeycomb block moves any flatness verdict"* — is **ANSWERED**. |

---

## 1. The cheap bound, and it decided what the run could and could not be about

Four questions, all answered before a Monte Carlo and three of them before a solve.

| question | smeared equivalent sheet | honeycomb grillage | consequence |
|---|---|---|---|
| how many crossover columns does **one interface** carry? | **5** — 10 columns at the 3.57 nm half-pitch, alternating between the two parities | **5 to 6** — the planes of one bond class, recurring every 21 bp | **they agree**, so the whole of the re-grade is the across-helix rigidity and the dimer topology and **not** a second column-count error |
| where are the **rooting helices** of the gap-facing face? | one beam per raster row at a uniform 3.804 nm | gaps alternate **2.536 and 5.072 nm** — `d` and `2d` about `3d/2` | an abstract-grid station sits **0.634 nm = `d/4`** off its own helix, **alternating in sign** across the rows |
| does the **normalising stroke** move between the models? | `p/k_f` = 5.27921926 nm | `p/k_f` = 5.27921926 nm | a free body on a uniform Winkler foundation translates rigidly whatever its rigidities, so the re-grade is **controlled**: same extent, same grid, same stroke, same mandate, same stream |
| which way does the coupled margin move? | free tile 0.0240648102 (`C-0141`) | free tile 0.0449400126 (`C-0154`) | 1.868× before any coupling, so the run's job is **how many** cells survive, not **whether** the number moves |

The second row is the one nobody had. `CLAUDE.md` records that *"alternating an attachment's
STATION across the helices is the same symmetry break as alternating its STIFFNESS, and it is
FIRST order"*; on a **corrugated** honeycomb face that alternation is not a design choice, it is
what the lattice hands you, and a smeared plate has no coordinate for it. So every placement is
graded twice — at the abstract grid's uniform `y`, and with each station moved onto its own
rooting helix.

## 2. The port, which is one function and an adapter

`coupling/NonUniformCoupling.kt`'s `influenceSurrogate` is already written against a model-agnostic
`DishingSolution`; `latticeInfluenceSurrogate` and `plateInfluenceSurrogate` are its two existing
adapters. The whole port is therefore

- **`HoneycombGrillage.pointLoadDual(s, y, magnitude)`** — the exact gradient of the same
  `evaluate` the sampling uses, so a station off its beam's axis is carried through that beam's
  **roll** with `y − beamY` as the arm, and a station on the axis puts nothing into the roll; and
- **`honeycombInfluenceSurrogate`**, fifteen lines.

Because the dual is the gradient of the evaluation, `M = eᵀK⁻¹e` is symmetric **by construction**
and a reciprocity residual measures nothing — so it is **not** offered as a gate. What is offered
instead is Betti between the point dual and the **pressure load vector**, two different load cases
on one factorisation, and the exact identity that the surrogate at full presence reproduces an
**assembled** solve carrying the Woodbury support force as a point load. Both hold below `1e−9`.

## 3. The re-grade

`C-0151`'s recommended state, `10 × 6` at the 116 bp block extent, the abstract grid, `1 × 10 = 10`
paths, equal springs:

| | `f = 0.30` | `f = 0.26` |
|---|---|---|
| `C-0151`, smeared sheet — **reproduced here at `6.3e−10` / `4.4e−11`** | 0.0773373597 | 0.0821458169 |
| **the honeycomb grillage** | **0.145086839** | **0.149852804** |
| ratio of the 90th percentiles | **1.87602525** | **1.82422928** |
| **median of the per-realisation ratios** | **1.85737817** | **1.7942757** |
| realisations at which the honeycomb reads worse | **4 000 of 4 000** | **4 000 of 4 000** |
| the honeycomb cell's **nominal**, no defects | **0.0626407003 — flat** | 0.0634254657 — flat |

Over the whole set:

| | honeycomb grillage | smeared equivalent sheet |
|---|---|---|
| cells clearing `T-5b`'s 0.10 at the 90th percentile | **0 of 64** | **15 of 64** |
| `C-0151`'s own eight `f = 0.30` abstract-grid cells | **0 of 8** | **2 of 8** (its published count) |
| paired cells changing their verdict | **15 of 64**, every one a loss | — |
| the **uncoupled** block at `f = 0.30` / `0.26` | **0.0501417315 / 0.0522223659 — FLAT** | 0.0281953496 / 0.0299114053 — flat |
| the **uncoupled** block with no enhancement at all | 0.132443428 — not flat | — |

**And it is not a multiplier.** The per-realisation median ratio runs **1.06375481 to
2.47485493** across the 64 paired cells, and it is **not monotone in the path count**: by column
count it is 1.380–2.475 at one, 1.166–1.555 at two, 1.064–1.208 at three and 1.190–1.462 at five.
So *"multiply `C-0151`'s table by 1.87"* is not a repair, and no design can be recovered by
re-reading an existing sweep at a rescaled tolerance.

**And the paired reading and the unpaired one disagree in SIGN at six of the 64 cells.** The ratio
of the two 90th percentiles falls **below one** — 0.946828178 to 0.968492853, *"the honeycomb is
better"* — at six cells, all of them `3 × 10` rim-graded; on the **same** realisations, read one by
one, the median of the ratios is **1.063755 to 1.137788** and the honeycomb is worse at **58.1–64.8
%** of them. Over all 64 cells the median of the per-realisation ratios is **above one everywhere**
and the honeycomb is worse at **58.1 % to 100 %** of realisations, at 8 cells all 4 000 of them.
`CLAUDE.md`'s *"a ratio of two ORDER STATISTICS is not the order statistic of the ratio"*, met on a
sign rather than on a magnitude.

**Moving a station onto its own rooting helix is worth 0.961 to 1.083×**, and its sign is set by
the **distribution**: it is adverse at every one of the 16 equal-spring pairs (1.0413–1.0826) and
runs both ways at the rim-graded ones (0.9606–1.0739), because the outermost rooting helix sits
0.634 nm **inboard** of where the abstract grid puts its outermost station. It is first order and
it is small; it is reported because a smeared plate cannot express it at all.

## 4. What fails is the coupling, and the block is flat without it

Three readings say this and they are not the same reading:

- the **uncoupled** four-layer honeycomb block is inside `T-5b` at both ends of the measured band;
- the recommended coupled cell is inside `T-5b` at **zero defects** (0.0626407003); and
- it is outside it at the 90th percentile of `C-0087`'s **measured** staple incorporation.

That is `CLAUDE.md`'s *"an attachment coupling can be a NET DISHING SOURCE"* and *"always run the
uncoupled tile as the reference"*, met at a corrected geometry — and `C-0109`'s *"every coupled
cell is worse than the uncoupled tile"* reproduces at **160 of 160** cells graded here, both
models included. The design
question the re-grade poses is therefore not *"which coupling"* but **whether the recommended
`10 × 6` block needs one at all**, which is a question this repository has never asked of a body
that was flat on its own.

## 5. The five gates

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | a unit downward point load asserted to do **unit work** under a unit piston and work equal to its own `s` and `y` under the two rigid tilts; its roll entries asserted to vanish exactly on the beam axis and to carry exactly `magnitude × offset` off it; the face gaps asserted to be `d` and `2d` alternating and the abstract-grid offset exactly `d/4` with alternating sign |
| **2 — limiting cases** | an empty point-load list asserted **bit-identical** to the pressure-only solve; a zero-magnitude load asserted inert; a station off the face **refused** rather than snapped; the beams asserted to reach `lengthS/2` at 42, 56 and 112 bp with **no node added**, and at 116 bp with a 4 bp overhang |
| **3 — symmetry / conservation** | the standing uniform-load falsifier asserted at `< 1e−9` on the 116 bp block, whose free stroke is `p/k_f` to `1e−9`; Betti between the point dual and the pressure load vector; the surrogate at full presence asserted equal to the **assembled** solve with the Woodbury support force applied as a point load, and `f = k w` asserted on the same solve |
| **4 — numerical convergence** | beam subdivisions 1 → 2, departure **`1.1e−4`** on the recommended cell's nominal; the dishing sample grid 41 / 81 / 161, departure **`0.0`** at both steps; the identity fields emitted as a **threshold and a boolean** rather than as values, after two runs disagreed in exactly one such field |
| **5 — literature cross-check** | **eleven** reproductions, worst `1.7e−9`: `C-0154`'s three free `10 × 6` readings at 112 bp (`1.1e−9`, `2.3e−10`, `1.2e−10`), `C-0151`'s two uncoupled references and its four published cell percentiles (`≤ 6.3e−10`), all in the same process; the honeycomb lattice's per-interface crossover count checked against the published 21 bp rule and against the smeared model's own parity alternation |

## 6. Falsifiers

| | statement | fired | note |
|---|---|---|---|
| **`F1`** | a uniform pressure on the coupled honeycomb lattice dishes exactly zero | **no** (below `1e−9`) | **it fired on the first smoke run**, and the cause was `CH-0214`: the beams stopped at the last crossover plane, so a 116 bp row carried an unsupported, unloaded 1.36 nm strip and dished **0.15** of the stroke under a load whose exact answer is zero |
| **`F2`** | the free-tile reproduction of `C-0154` closes at `1e−8` | **no** | worst `1.1e−9` over the three enhancements |
| **`F3`** | the surrogate at full presence equals the assembled solve | **no** | below `1e−9` on a one-path coupling |
| **`F4`** | Betti holds between the point dual and the pressure load vector | **no** | below `1e−9` |
| **`F5`** | **the honeycomb re-grade changes NO flatness verdict** | **FIRED** | **declared expected to fire, and its firing is the finding**: 15 of 64 paired cells move, 0 honeycomb cells flat against 15 smeared |
| **`F6`** | the honeycomb lattice's sparsest interface carries as many crossover columns as the smeared model gives every interface | **no** | 5 against 5, so there is no second overstatement |

## 7. What the port cost, and why it was cheap

`C-0154`'s node-major banded ordering carries straight over: 4 320 unknowns at half-bandwidth 243,
one factorisation per `(cross-section, enhancement)` and one **back-substitution** per influence
function. The whole study — 160 graded cells, 64 paired comparisons, 4 000 realisations each, two
convergence axes and eleven reproductions — runs in **2 m 24 s**. The alternative, a full solve per
realisation, is four orders dearer for an answer superposition gives exactly.

## 8. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **The lattice carries ONE row length.** `C-0151`'s `102 / 109` raster has a 7 bp stagger and a
  **102 bp interface window**; the block here is built at the **116 bp extent**, which is the width
  `C-0151` grades at, so the comparison is controlled and the window is not modelled. What the
  window costs is measured on the smeared model in `C-0151` (one crossover column) and is not
  re-measured.
- **The lattice carries NO across-helix parallel-axis term.** The layers' membrane action across
  the helices needs an in-plane transverse coordinate this model does not have, so its `D_⊥` is
  the **independent** one and a **lower** bound, and the composite fraction enters as a smeared
  multiplier on `k_θ`. The bracket is enclosed: the smeared sheet is its upper end and the
  enhancement-1.0 lattice its lower, and at the lower end the **free** tile already exceeds the
  tolerance.
- `k_θ` is `Gen1Tile`'s **square-lattice-fitted** constant; no honeycomb measurement of it exists
  in this repository, and `k_s` is a construction rather than a measurement.
- Kirchhoff is not safe at these thicknesses (`C-0109`, `C-0120`): transverse shear is not carried,
  so every `D_∥` here is an upper bound.
- The dropout statistics are measured on a **single-layer Rothemund rectangle** and only the
  *profile* transfers, in nm (`C-0087`, `C-0109`); the ensemble perturbs the **coupling** and never
  the block's own crossovers.
- `assembleLoad` and `integrateOverFace` are adjoint only up to a **corrugation bookkeeping term**
  — the face strips are one row pitch centred on their own axis, which is what keeps the
  uniform-load falsifier exact, while `evaluate` reads the *nearest* face helix. Asserted in a test to lie
  between machine precision and 1 %, and measured at **0.0012** relative on a `4 × 2` probe under a
  tilted pressure. **No number here is exposed to it**, because the surrogate never
  calls `integrateOverFace`; it is recorded because it is a property of the shared class.
- **Nothing here re-opens the placement search.** The stations are `C-0151`'s, and the
  distributions are `C-0058`'s two.

## 9. Open questions

- **Whether the recommended block needs an attachment coupling at all.** The uncoupled four-layer
  honeycomb block is flat at both ends of the measured band and every one of 64 coupled cells is
  worse than it. No claim in this repository has asked what a *tie-less* Gen-1 tile costs
  elsewhere in the stack — the stroke, the lateral confinement and `C-0017`'s stability mandate all
  want ties for reasons that are not flatness.
- What the **across-helix parallel-axis term** is worth once an in-plane transverse coordinate is
  carried. It removes the only bracket in this answer, and it is `C-0154` §10's first open item.
- Whether `C-0089`'s percentile descent, or a distribution searched on the honeycomb lattice rather
  than transferred onto it, recovers any of the 15 lost cells. Every distribution graded here is a
  rule written on a smeared model's geometry.
- What a **per-layer** defect does. This lattice can remove one crossover of one interface of one
  layer, which a smeared equivalent sheet cannot express at all.
- Whether the **102 bp interface window**, modelled as a restricted bond set rather than as a
  column count, moves any cell graded here at 116 bp.
