# C-0089 — **No Gen-1 coupling is flat under the measured staple dropout, and what refuses it is a COUNT.** `C-0087`'s recovery route is the right one — the 90th percentile falls monotonically from **0.8522 to 0.5327** of the stroke as the path count goes 15 → 90, and moving the objective from the zero-defect value to the **percentile** is worth a further **1.30 – 1.61×** — but the best of **22** searched cells is **0.2845**, still **2.85×** `T-5b`'s convention, and the density the dropout actually demands is **13 attachment columns, 195 paths**, against the **34** `C-0075`'s plan table admits: **5.7× short, in a division that needs no solve**. And the reversal `C-0087` reads as *regularity* is a count effect — at matched count the *irregular* upward roots beat the regular grid, 0.5837 against 0.6690

| | |
|---|---|
| **Task** | [`T-155`](../tasks/T-155.md), raised by [`C-0087`](C-0087-position-dependent-staple-dropout.md) (`T-148`), open item 1 |
| **Leaf** | **`A8.2`** (the flatness of the tile), with **`A1.2`** for the anchoring scheme the coupling belongs to |
| **Verification type** | **logical** (three closed-form bounds — a rigorous per-realisation lower bound over *all* distributions, a run-length pitch arithmetic with no solve at all, and `C-0075`'s plan ceiling read as a path-count cap) **+ in-silico** (`C-0058`/`C-0063`/`C-0074`/`C-0087`'s own exact Woodbury surrogate on `C-0009`'s grillage under `C-0022`'s **solved** load, re-run under `C-0087`'s seeded Bernoulli dropout with the objective moved from a value to a **percentile**, 10 000 realisations per cell) |
| **Verdict** | **PASS on the predicate, and the answer is NO.** Over **22 graded `placement × distribution` cells** — six attachment densities from 15 to 90 paths, `C-0063`'s 34 upward roots, `C-0074`'s recommended 30, sixteen buildable one-parameter distributions and six per-path descents — the **lowest 90th-percentile dishing anywhere is 0.284537599** of the free-tile stroke, on a **6 × 15 grid of 90 paths** under a **90th-percentile descent**, against `T-5b`'s 0.10. Every one of the 22 cells exceeds the convention in **97.2 – 100 %** of realisations. **`C-0087`'s two named directions are both confirmed and both insufficient.** *Denser*: with equal springs the 90th percentile falls **monotonically** at every step of the sweep, `0.8522 → 0.6690 → 0.6142 → 0.6123 → 0.5723 → 0.5327` over 15/30/45/60/75/90 paths — the declared falsifier **`F1` did not fire**, and the direction `C-0087` read off two points holds on six. *The percentile objective*: on all three descent sets the design optimised on the 90th percentile beats the design optimised at zero defects, **0.3635 against 0.4734** at 45 paths, **0.2845 against 0.4578** at 90 and **0.4496 against 0.6358** at 34 — **1.30, 1.61 and 1.41×** — and it buys that by *giving up* zero-defect flatness, 0.0536 → 0.2047 at 45 paths, which is the trade in one line. **The third direction, REDUNDANCY, is the one that decides it, and it decides it against the tile.** A dropout **is** an increase in the attachment pitch, so `CLAUDE.md`'s net-dishing-source rule inverts into a density requirement with no solve at all: the along-helix Winkler bending length is **12.8290845 nm**, the 90th-percentile longest run of consecutive absences within a row is **3**, and the column count that keeps the surviving pitch inside one bending length is therefore **13 — 195 paths**. `C-0075`'s self-consistent count table caps the recommended rooted arm at **34** (at 45 the arm is 9.131 nm against an 8.19 nm lattice ceiling and only 30 place), so the demand exceeds the buildable count by **5.7×**. **The negative belongs to the searched family and not to all distributions, and this claim says so**: falsifier **`F5` did not fire** — the oracle floor over the survivors reaches only **0.00111 – 0.01988** at the 90th percentile, so a builder who *knew* which staples were missing could be flat by **255×**, and the whole failure is that the distribution must be fixed before the defect is known. **One instrument is delivered**: `C-0087`'s single-removal bound ranks the 22 designs against the percentile at **Spearman ρ = 0.9729**, so it is not only an explanation but a search objective, `n + 1` solves against 10 000. Raises [`CH-0103`](../challenges/CH-0103-the-path-count-recommendation-runs-against-fabrication.md) and [`CH-0104`](../challenges/CH-0104-a-reachable-floor-is-an-oracle-and-cannot-license-a-design.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED.** The **input** is `C-0087`'s, which is Strauss et al. (2018) read directly — a measurement of *staple* incorporation on a plain Rothemund rectangle at one folding protocol — and the out-of-plane motif every placement stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |
| **Provenance** | `gpd/results/T-155-dropout-robust-placement.json`, produced by `coupling.DropoutRobustPlacementStudyKt`; model in `src/main/kotlin/coupling/DropoutRobustPlacement.kt`, with one additive method (`InfluenceSurrogate.reachableDishingFloorAt`) in `src/main/kotlin/coupling/NonUniformCoupling.kt`; **6 cheap bounds, 6 redundancy ledgers, 24 graded design cells at 10 000 seeded realisations each, 10 oracle-floor records, 11 plan rows read from `C-0075`, 5 convergence axes, 16 upstream reproductions, 8 predicates**; **23 gate-named tests in `src/test/kotlin/coupling/DropoutRobustPlacementTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 18 m 9 s** — the whole suite on its own isolated tree, with two concurrent agents' mid-TDD test files dropped by `--drop-file` (`src/test/kotlin/anchoring/BuildableRasterWidthTest.kt`, `src/test/kotlin/synthesis/BufferRouteCensusTest.kt`), and the four post-Gradle gates run separately and clean (45 census self-checks, the census itself, 0 table defects in 276 files, the deliverable tracer at 0 absent tokens); the result file **re-run on a second snapshot and diffed BYTE-FOR-BYTE IDENTICAL** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** sheet, 15 duplexes at the SAXS-measured **2.69 nm**; `C-0022`'s **solved** edge profile at **2 mM, 10 nm, 0.192 V** for every headline, with **2 mM, 7 nm, 0.192 V** (the held end of `C-0068`'s range) carried beside it; `C-0017`'s **33.3333 pN/nm** as a **SUM** at §3's **acceptable 3 nm**; free-tile stroke **4.90731102 nm**; dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; grading **seed 20260817** (`C-0087`'s own) at **10 000** realisations, training **seed 20260819** at **200**; decisions at 6 significant digits, emission at 9 |
| **Consumes** | [`C-0087`](C-0087-position-dependent-staple-dropout.md) (**the whole dropout model** — `IncorporationField`, `MEASURED_DEPTH`, `DropoutRandom`, `bernoulliPresence`, `orderStatistic`, `solveWithDropout`; its 0.5010, 0.3060, 0.6391, 0.5346 and 0.998 all **reproduced**), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`, `rimStiffenedWeights`, `optimiseStiffnessDistribution`; its 0.2182 and 0.0753 reproduced), [`C-0063`](C-0063-upward-root-placement.md) (**the 34-root placement**, read from `gpd/results/T-125-*.json`; its 0.0706 reproduced), [`C-0068`](C-0068-range-robust-placement.md) (the two-state range; its 0.0789 reproduced), [`C-0074`](C-0074-two-per-row-placement.md) (**the recommended 30-root placement**, read from `gpd/results/T-136-*.json`; its 0.2424 equal-spring reading reproduced), [`C-0075`](C-0075-path-count-consistency.md) (**the self-consistent count table**, read from `gpd/results/T-138-*.json`), [`C-0069`](C-0069-output-element-placement.md) (the 8.19 nm plan budget), [`C-0047`](C-0047-single-column-flatness.md) (`winklerBendingLength`; its 12.83 and 5.71 nm reproduced), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (the per-path ceiling `a/s`), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0026`](C-0026-one-row-per-duplex.md) (the free-tile stroke, the one-row-per-duplex grid), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0015`](C-0015-crossover-phase-and-registration.md)/[`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0103`](../challenges/CH-0103-the-path-count-recommendation-runs-against-fabrication.md) against `C-0072`/`C-0074`/`C-0075`'s 34 → 30 recommendation, and [`CH-0104`](../challenges/CH-0104-a-reachable-floor-is-an-oracle-and-cannot-license-a-design.md) against `C-0074`'s use of the reachable floor as a licence |

---

## The claim, in one line

**Fabrication does not ask a coupling to be well placed, it asks it to be redundant — and redundancy on this tile is a column count, a column count is a length, and the length the measured dropout demands is three and a half times the 40 nm the tile has.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward**;
  `w` is positive **downward**; the origin is the tile centre.
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit
  **plane**, on the same **81 × 81** grid as every flatness claim upstream. **Flat** means below
  **0.10** of the free-tile stroke — `T-5b`'s **convention**, not a physical threshold.
- **A dropout is a REMOVAL, not a perturbation.** An absent path is solved as an absent station,
  which is exact superposition (`C-0087`).
- **The operating state is named**: `C-0022`'s **solved 2 mM / 10 nm / 0.192 V** profile — the
  design state of `C-0063`, `C-0068` and `C-0087` — with the held end of `C-0068`'s range carried
  beside it. This is the eighth time this programme has had to say which state a flatness verdict
  is read at.
- **The verdict statistic is the 90th percentile**, taken as a **nearest-rank order statistic**,
  and it is `C-0087`'s so the two claims are comparable cell for cell. It remains a **choice**
  (`C-0087` open item 4): the median, the 95th percentile, the worst realisation and the
  exceedance probability are all emitted beside it.
- **Every optimised percentile in this claim is OUT OF SAMPLE.** A descent sees a
  200-realisation ensemble at seed **20260819**; every quoted number is read on the independent
  10 000-realisation ensemble at `C-0087`'s seed **20260817**. An in-sample percentile optimum is
  not a result.

---

## The three cheap bounds, which ran first — and the third one is the answer

### Bound 1 — the worst single-path removal, `n` solves, and it turns out to be an INSTRUMENT

`C-0087` used one missing path to *explain* its distribution. Run over all 22 designs, the same
`n`-solve bound **ranks** them:

| | |
|---|---|
| **Spearman ρ between the worst single removal and the 90th percentile** | **0.972896669** |

So a placement search may be run on the cheap objective — `n + 1` surrogate solves against 10 000
— and `C-0087`'s bound is an instrument as well as an explanation. It also carries the whole story
of the percentile objective in one column: the amplification `worst-single-removal / nominal` is
**6.43, 4.05 and 7.97** for the three zero-defect-optimised designs and **1.25, 1.09 and 1.26**
for the three percentile-optimised ones on the same station sets.

### Bound 2 — the ORACLE floor under dropout, which is rigorous and did NOT fire

`InfluenceSurrogate.reachableDishingFloorAt(present)` is the least-squares-optimal RMS dishing over
**all** force vectors at a realisation's surviving stations. A peak is never below its own RMS and
every distribution produces *some* force vector, so it is a pointwise lower bound on that
realisation's peak dishing **for every distribution whatever** — indeed for an oracle allowed a
different distribution per tile. Percentiles are monotone under a pointwise bound.

| station set | paths | floor at full presence | median floor | **p90 floor** | excludes? |
|---|---|---|---|---|---|
| 1 × 15 grid | 15 | 0.01688 | 0.01710 | **0.01988** | no |
| 3 × 15 grid | 45 | 0.00268 | 0.00387 | **0.00486** | no |
| **6 × 15 grid** | **90** | **0.00028** | 0.00077 | **0.00111** | no |
| `C-0063`'s roots | 34 | 0.00312 | 0.00631 | **0.00917** | no |
| `C-0074`'s roots | 30 | 0.00548 | 0.01001 | **0.01434** | no |

**Falsifier `F5` did not fire, and that is a result rather than a disappointment.** The bound tops
out at **0.0199**, 5× below the convention, so *nothing here is excluded by geometry*: an oracle
could hold the 90-path tile flat by **255×** (0.00111 against the 0.2845 the best fixed
distribution reaches). **The entire failure is that a coupling is specified before it is folded.**
That is `CH-0104`.

### Bound 3 — the run-length pitch arithmetic, with no solve at all, and it settles the question

`CLAUDE.md`: *"an attachment coupling can be a NET DISHING SOURCE, and the sign flips at an
attachment pitch of one Winkler bending length"*. **A dropout is an increase in that pitch**, so
the rule inverts into a density requirement: surviving `j` consecutive absences in a row needs
`columns ≥ (j + 1)·edgeX/ℓ`.

| paths | pitch [nm] | pitch / `ℓ` | p90 longest run | surviving pitch [nm] | inside `ℓ`? | **columns demanded** | **paths demanded** |
|---|---|---|---|---|---|---|---|
| 15 | 40.000 | 3.118 | 1 | 80.000 | no | 7 | 105 |
| 30 | 20.000 | 1.559 | 2 | 60.000 | no | 10 | 150 |
| 45 | 13.333 | 1.039 | 3 | 53.333 | no | **13** | **195** |
| 60 | 10.000 | 0.779 | 3 | 40.000 | no | **13** | **195** |
| 75 | 8.000 | 0.624 | 3 | 32.000 | no | **13** | **195** |
| 90 | 6.667 | 0.520 | 3 | 26.667 | no | **13** | **195** |

`ℓ = 12.8290845 nm`, reproducing `C-0047`'s 12.83. **The demand saturates at 13 columns — 195
paths — and it is a floor**, because a denser array has *more* places for a long run, so the fixed
point of the requirement is above 13 and not below it.

`C-0075`'s self-consistent count table, read at run time from `gpd/results/T-138-*.json`, caps the
programme's recommended rooted arm at **34** paths:

| paths | arm [nm] | lattice ceiling [nm] | plan margin [nm] | placed | self-consistent |
|---|---|---|---|---|---|
| **45** | **9.13115573** | **8.19** | **−0.941155731** | **30** | **NO** |
| **34** | 8.16439018 | 8.19 | **0.0256098233** | 34 | **YES** |
| 30 | 7.77048807 | 9.535 | 1.76451193 | 30 | yes |

> **195 demanded against 34 buildable is 5.735×, and neither number needed a solve.**

---

## Deliverable 1 — the density axis, priced

Equal springs, `C-0017`'s unchanged total, `MEASURED_DEPTH` incorporation, 10 000 realisations:

| station set | paths | zero defects | median | **p90** | exceedance | mean survivors |
|---|---|---|---|---|---|---|
| 1 × 15 | 15 | 0.6952 | 0.7019 | **0.8522** | 100.0 % | 12.6 |
| 2 × 15 | 30 | 0.3504 | 0.4894 | **0.6690** | 100.0 % | 23.7 |
| 3 × 15 | 45 | 0.2182 | 0.4154 | **0.6142** | 100.0 % | 36.3 |
| 4 × 15 | 60 | 0.1823 | 0.4356 | **0.6123** | 99.99 % | 46.5 |
| 5 × 15 | 75 | 0.1681 | 0.4194 | **0.5723** | 99.99 % | 59.0 |
| 6 × 15 | 90 | 0.1611 | 0.3966 | **0.5327** | 100.0 % | 71.7 |

**`F1` did not fire**: the percentile falls at every step, a factor of **1.60** from 15 to 90
paths. The direction is right and its slope is the problem — six times the paths buys 1.6× where
the verdict needs 5.3×.

## Deliverable 2 — the objective, priced separately from the design

| station set | distribution | zero defects | worst one removal | **p90** | peak ratio | per-path force [pN] |
|---|---|---|---|---|---|---|
| 3 × 15, 45 | zero-defect descent | **0.0536** | 0.3446 | 0.4734 | 1.48 | 3.30 |
| 3 × 15, 45 | **percentile descent** | 0.2047 | **0.2551** | **0.3635** | 4.50 | **10.00** |
| 6 × 15, 90 | zero-defect descent | **0.0635** | 0.2569 | 0.4578 | 6.85 | 7.61 |
| 6 × 15, 90 | **percentile descent** | 0.1870 | **0.2046** | **0.2845** | 4.77 | 5.30 |
| 34 roots | zero-defect descent | **0.0640** | 0.5096 | 0.6358 | 1.27 | 3.74 |
| 34 roots | **percentile descent** | 0.2652 | **0.3353** | **0.4496** | 3.40 | **10.00** |

- **`F2` did not fire**: the percentile objective wins on all three sets, by **1.30, 1.61 and
  1.41×**.
- **And the trade is explicit**: it gives up **3.8×, 2.9× and 4.1×** of zero-defect flatness to do
  it. A design optimised at zero defects and a design optimised at the 90th percentile are
  different designs, and the first is a **cancellation** (amplification 4.05–7.97 under one
  missing path) where the second is not (1.09–1.26).
- **The per-path allowable binds on two of the three**: the descent is capped at `C-0049`'s
  `a/s = 3.33333333 pN/nm` and sits **exactly on it** (10.00 pN at the 3 nm stroke) at 45 paths
  and at 34. At 90 paths it does not bind (5.30 pN) — **the redundancy axis loosens the force
  constraint exactly as `C-0087` said it would**, and it is not the force that fails.

## Deliverable 3 — the buildable one-parameter families, and the best cell in the sweep

`C-0058`'s rim × R for eleven ratios and a `1/pᵉ` compensation for five exponents, chosen **out of
sample** and graded on the independent ensemble:

| station set | paths | chosen | zero defects | median | **p90** | exceedance |
|---|---|---|---|---|---|---|
| 3 × 15 | 45 | rim × 10 | **0.0792** | 0.2515 | 0.4636 | **97.16 %** |
| 5 × 15 | 75 | rim × 7 | 0.1712 | 0.2316 | 0.3224 | 100.0 % |
| **6 × 15** | **90** | **rim × 7** | 0.1853 | 0.2335 | **0.2919** | 100.0 % |
| `C-0063`'s 34 | 34 | rim × 7 | 0.2462 | 0.3658 | 0.4819 | 100.0 % |
| `C-0074`'s 30 | 30 | rim × 5 | 0.3367 | 0.4239 | 0.5479 | 100.0 % |

**The single best cell of all 22 is the 90-path percentile descent at 0.284537599** — median
0.2232, p95 0.3133, worst realisation 0.5739, **exceedance 100.0 %**, mean 71.7 survivors of 90,
peak stiffness ratio 4.77 and 5.30 pN in the stiffest path. **The lowest exceedance anywhere is
97.16 %**, and it belongs to the *only* design in the sweep that is flat at zero defects
(0.0792) — which is the whole finding restated: flat-at-zero-defects and robust-under-dropout are
different designs, and neither is both.

## Deliverable 4 — *more regular* is the smaller half of the direction, and its sign is the other way

`C-0087` reads its ranking reversal as *denser **and more regular***. At matched count the two
separate:

| | paths | **p90** |
|---|---|---|
| `C-0074`'s 30 upward roots (irregular lattice) | 30 | **0.5837** |
| `C-0015`'s 2 × 15 grid (regular) | 30 | **0.6690** |

The **irregular** placement is **13 %** better, where the step from 30 to 90 paths on the grid is
**1.26×**. **So the reversal `C-0087` measured is a COUNT effect**, and regularity as such runs the
other way on this pair. `C-0087`'s Deliverable 3 stands as a fact about two designs and its
*explanation* is narrowed here; that is a refinement, not a challenge.

---

## The five verification gates

Executed as **23 gate-named tests** in `src/test/kotlin/coupling/DropoutRobustPlacementTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a probability is dimensionless and an ensemble refuses anything outside `[0, 1]`, an empty path list or a non-positive realisation count; the run-length arithmetic is a length over a length and refuses a zero edge, a zero bending length, a negative run, a zero row length and a presence vector that does not split into rows; a removal profile carries one dishing per path and refuses a mismatched design; a rank correlation is dimensionless, reproduces `±1` on monotone pairs and refuses unpaired or single-point samples | **PASS** |
| **2 — limiting cases** | an ensemble at unit incorporation keeps every path and reproduces the nominal solve at every realisation; one at zero incorporation returns the free tile at every realisation; the oracle floor at full presence **is** `C-0058`'s standing `reachableDishingFloor` (`1e−8`) and at no presence is the free field's own RMS (`1e−12`); removing a station can only **raise** the floor; the worst single removal is the maximum of the profile; a run robustness of zero losses is the bare pitch requirement and of one loss exactly twice it | **PASS** |
| **3 — symmetry and conservation** | **the uncoupled tile under a UNIFORM load dishes exactly zero** (falsifier `F3`, `< 1e−9`, asserted on the dishing **and** on the oracle floor); **the oracle floor bounds every realisation's peak dishing from below** (`F5`'s own premise, asserted over 40 realisations and not forced by the assembly — an RMS over all force vectors against the peak of one field); the ensemble is bit-reproducible from its seed and differs between seeds; **the ensemble draws the same stream `C-0087`'s own sampler draws**, realisation for realisation; the longest absence run is counted within a row and never across one; the percentile summary is monotone in the fraction and lands on the sample | **PASS** |
| **4 — numerical convergence and statistical power** | the 90th percentile at **1 250 / 2 500 / 5 000 / 10 000** realisations (departure `2.2e−3`, i.e. 0.35 %); the 90th percentile of the **oracle floor** at the same counts, reported separately because the rigorous bound is what a negative rests on (`4.3e−5`); the dishing grid at **41 / 81 / 161** samples per edge; the **out-of-sample** 90th percentile of a percentile descent at **100 / 200 / 400** training realisations (0.4964 / 0.4496 / 0.4623, departure 1.3e−2 = 2.8 %); the **last relative improvement of each descent** (0.0387 / 0.0419 / 0.0828), so the truncation is stated rather than hidden; a binomial standard error beside every exceedance probability | **PASS**, with one degeneracy reported below |
| **5 — literature and upstream cross-check** | `C-0017`/`C-0058`'s **0.2182**, `C-0058`'s **0.0753**, `C-0063`'s **0.0706**, `C-0068`'s **0.0789**, `C-0074`'s **0.2424**, `C-0026`'s **4.90731 nm**, `C-0017`'s **33.3333 pN/nm**, `C-0047`'s **12.83** and **5.71 nm**, `C-0075`'s **8.16439018 nm** arm and `C-0069`'s **8.19 nm** budget, `C-0049`'s **3.33333333 pN/nm**, and — the strong one — `C-0087`'s **0.5010**, **0.3060**, **0.6391**, **0.998** and **0.5346**, all reproduced from the same seed and sample count rather than transcribed. **Worst strict departure over sixteen reproductions: `6.335e−4`**, which is the four-significant-figure rounding of `C-0068`'s published 0.0789 | **PASS** |

> **The grid convergence axis is DEGENERATE here and is reported as such.** The 90th percentile on
> the 41 / 81 / 161 grids is `0.628202` at all three, departure exactly **0.0** — because the three
> nested grids share their nodes and the peak of the 90th-percentile realisation lands on a shared
> one. `C-0087` recorded exactly this trap and cured it by taking the **mean over 200 realisations**
> instead, where the same three grids give 0.604499 / 0.604681 / 0.604810, departure `1.3e−4`. That
> is the informative reading and it is `C-0087`'s; this claim's own grid axis measures nothing and
> says so.

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **`F1`** | **the declared one** — the density axis is not monotone, which would withdraw the premise that redundancy buys robustness | **NO** | the 90th percentile falls at every one of five steps, 0.8522 → 0.5327 |
| **`F2`** | the percentile objective buys nothing over the zero-defect one | **NO** | it wins on all three descent sets, by 1.30 / 1.61 / 1.41× |
| **`F3`** | the uncoupled tile dishes non-zero under a **uniform** load | **NO** | `< 1e−9`, wired as a test on the dishing and on the oracle floor |
| **`F4`** | a standing figure fails to reproduce | **NO** | worst strict departure `6.3e−4`, a published rounding; `C-0087`'s own Monte Carlo reproduces to `4.6e−5` |
| **`F5`** | the oracle floor settles it with no search | **NO** | 0.00111–0.01988, 5–90× **below** the convention: the search was necessary, and the negative is a statement about *fixed* distributions |

**Three results that were not anticipated.**

1. **That the cheap bound is a ranking instrument, not only an explanation.** ρ = 0.9729 over 22
   designs was not expected of a bound that removes *one* path where the dropout removes twenty.
2. **That the percentile-optimised design is 3–4× WORSE at zero defects.** The two objectives are
   not near-neighbours; optimising the right one visibly abandons the headline number every
   upstream flatness claim is written on.
3. **That the plan ceiling, not the physics, is what closes the branch.** The study was built to
   search distributions and placements; what settles it is a division between a bending length and
   a tile edge, and a table `C-0075` had already emitted.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. The input is measured; nothing derived here is.
- **The incorporation field, its conventions and the independence of realisations are `C-0087`'s
  UNCHANGED**, and this claim inherits its whole validity range: a plain Rothemund rectangle at one
  folding protocol, a coupling path whose own incorporation nobody has measured, and independence
  as a convention rather than a measurement.
- **The `m × 15` grids above three columns are ABSTRACT station sets.** `C-0053` places no 45-arm
  array on this tile at all, `C-0041` places no 45-flexure array at any of 720 orientations, and
  `CLAUDE.md`'s own slot finding says a column of ties severs the sheet. They are priced to say
  what redundancy **would** buy, not to propose them — and the plan bound above is precisely why.
  **`C-0090` (`T-153`, this iteration) tightens this further**: at the buildable 38.08 nm raster
  width `C-0053`'s packer drops from 43 arms to **29**, so the 5.7× shortfall is the optimistic
  reading of the geometry. Not re-run here.
- **The ORACLE floor is a bound on a ROOT MEAN SQUARE against a PEAK**, so it is loose by whatever
  the peak-to-RMS ratio of the residual field is. It can exclude and can never admit, and it did
  not exclude.
- **The descents are truncated at two sweeps** and their last relative improvements are 3.9–8.3 %.
  To reach `T-5b`'s 0.10 from the best cell needs a further **64.9 %**, so the truncation cannot
  reverse the verdict; it does mean the 0.2845 is an **upper bound** on what this family reaches.
- **No range reading is emitted for the winning design.** The range pass covers the one-parameter
  families and the winner is a per-path descent, so the headline is the **design state** alone.
  At zero defects the range costs 12 % (`C-0068`'s 0.0706 → 0.0789, both reproduced here), which
  cannot move a 2.85× exceedance.
- **`T-5b`'s 0.10 is a CONVENTION.** At a 30 % convention the 90-path percentile design's *median*
  (0.2232) would pass and its 90th percentile (0.2845) would not; the verdict is not
  statistic-limited but the margin quoted is.
- The dishing pipeline, the lattice, the hosts, the load and the free-tile stroke are `C-0058`'s,
  `C-0063`'s and `C-0074`'s unchanged, and inherit `C-0022`'s unsourced rim charge and `C-0001`'s
  single foundation secant.
- **Single layer, static, 300 K, aqueous 2 mM MgCl₂.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| staple incorporation, min / max / mean | 48 / 95 / 84 % | **CITED, MEASURED**, Strauss et al. (2018) **through `C-0087`**, which read it directly |
| the 168-value per-staple map | Suppl. Fig. 14 | **CITED from `C-0087`**, which transcribed and validated it |
| `C-0075`'s self-consistent count table | 11 rows | **CITED**, read at run time from `gpd/results/T-138-*.json` |
| `C-0063`'s and `C-0074`'s placements | 34 and 30 roots | **CITED**, read at run time from their own result files |
| `C-0022`'s solved collars | 2 mM / 10 nm / 0.192 V and 2 mM / 7 nm / 0.192 V | **CITED**, read at run time from `gpd/results/T-3b-*.json` |
| `C-0053`'s packer at 38.08 nm, 29 arms | 29 | **CITED** from `C-0090` (`T-153`), not re-run |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| `T-5b`'s tolerance | 0.10 | **CITED CONVENTION** |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |

Everything else — every ensemble, bound, ledger, graded cell, oracle floor, descent, convergence
axis and reproduction — is **derived here in code**.

## What this does to the standing claims

| claim | what moves |
|---|---|
| **`C-0063`** | **Nothing at zero defects; its 0.0706 is reproduced to `2.1e−4`.** Under fabrication its 34 equal springs are the **most fragile** design in the sweep (amplification 7.10) and its p90 is 0.6391. `C-0087` already recorded that; this claim adds that a **distribution** on the same stations recovers it only to 0.4496, so the station set cannot be rescued by the axis `C-0063` declared unnecessary. |
| **`C-0068`** | **Nothing.** Its 0.0789 reproduces to `6.3e−4`. Its range reading is untouched; no range reading is emitted for the winner here. |
| **`C-0058`** | **Its rim rule survives as the best BUILDABLE family** and its ratio moves: under the dropout the out-of-sample choice is **rim × 10** at 45 paths and **rim × 7** at 90 and at `C-0063`'s roots, against the ×5 it recommends and the ×7 `C-0060` found at zero defects. Its 0.2182 and 0.0753 reproduce. |
| **`C-0060`** | **Its flat-ratio window is confirmed to be the right axis and the wrong instrument.** The ratios that win here (7–10) are inside its measured `3.5 ≤ R ≤ 20`, so buildability is not the constraint; what fails is that no ratio reaches the convention. `C-0087`'s open item 6 — whether `C-0060`'s and `C-0026`'s amplitude thresholds should be retired — is **answered YES for the dropout**: the binding statement is a count, not an amplitude. |
| **`C-0074`/`C-0075`/`C-0072`** | **`CH-0103`.** All three recommend moving **down** from 34 paths to 30 to recover plan margin. Under the measured dropout the path count is the dominant robustness axis, and the move costs it: `C-0074`'s recommended 30 roots read 0.5837 against `C-0063`'s 34 at 0.6391 — nominally *better*, but on a station set whose zero-defect design (0.2424) was already 3.4× worse. The trade *plan margin against fabrication robustness* is priced by no claim. |
| **`C-0071`** | **No headline moves.** Its recommended element is `Q5` at 34 instances, and 34 is exactly the self-consistent cap this claim reads out of `C-0075`. What this adds is that the cap is now **binding in a second channel**: it was a plan margin of 0.0256 nm, and it is also the reason the redundancy route is closed. |
| **`ANSWERS.md` §4(g)** | The row already carries `C-0087`'s conditioning. It should now say that the recovery route was searched and closed: **the direction is right, the slope is 1.6× over six times the paths, and the density the dropout demands is 5.7× the buildable count.** |

## Still open — named, not answered

1. **Whether the coupling element's own incorporation is the staple's.** `C-0087`'s item 2,
   unchanged, and now the **only** route by which the programme keeps a flat tile: at an
   incorporation high enough that the p90 longest run falls to zero, the 90-path grid's own pitch
   (0.52 `ℓ`) is already inside the requirement.
2. **Whether a LARGER tile is the answer.** Everything here is bounded by a 40 nm plan and a
   10.88 nm root lattice, and the demand is a **column count**, which is a length. `C-0090` moves
   the buildable width the wrong way.
3. **Whether a coupling that is not an ARRAY escapes the count argument.** One stiff body tied at
   many points makes a missing tie a stiffness perturbation rather than a removed load path.
   Nothing in this corpus has priced it, and it is the only structural escape this claim can see. Queued as **`T-162`**.
4. **What fraction of built tiles a flatness verdict is owed over.** `C-0087`'s item 4, unchanged,
   and now the parameter the verdict is most sensitive to.
5. **Whether the dropout is correlated within a folding run.** `C-0087`'s item 3, unchanged.
6. **Whether the run-length demand's own fixed point is materially above 13 columns.** It is a
   floor here, computed at the run statistic of sparser arrays.

## Challenges

**Raises [`CH-0103`](../challenges/CH-0103-the-path-count-recommendation-runs-against-fabrication.md)**
and **[`CH-0104`](../challenges/CH-0104-a-reachable-floor-is-an-oracle-and-cannot-license-a-design.md)**.

**None stands against this claim.** The four ways it would fail:

1. **A per-site incorporation measurement on a coupling-bearing tile showing a materially higher
   incorporation.** The whole verdict is a transfer of a plain rectangle's map.
2. **A coupling topology that is not an array.** Open item 3; it would remove the count argument
   rather than satisfy it.
3. **A search that reaches 0.10 inside this family.** The descents here are truncated at two
   sweeps and 3.9–8.3 % per sweep; reaching the convention needs a further **64.9 %**.
4. **A different verdict statistic.** At the median the best cell reads 0.2232, still 2.2× past
   `T-5b`, so the negative is not statistic-limited.
