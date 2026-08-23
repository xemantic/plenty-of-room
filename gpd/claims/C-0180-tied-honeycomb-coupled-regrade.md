# C-0180 — **`C-0167`'s *"`0` of `64`"* IS `2` OF `64` ON THE TIED LATTICE, SO `CH-0227`'s *"no verdict of `C-0154` or `C-0167` reverses"* IS FALSE AT THE COUPLED LEVEL — AND ONCE THE TIES CARRY THE LOAD THEY DEMONSTRABLY CARRY IT IS `1` OF `64`, WITH WHICH ONE SET BY A SIGN NOTHING FIXES.** The 59 raster turn ties recover **two** cells — `0.106041029 → 0.0995744767` and `0.101931622 → 0.0998791032` — and the tightest clears `T-5b` by **0.426 %**, converged: 0 of 6 deciding-cell convergence steps move it at a worst departure of `4.57e−4`, a factor of **9.3**. **It is not a multiplier, and the free tile is a CEILING the coupled cells never reach**: the per-realisation median ratio runs **0.902845544 to 0.988116016** against the free tile's **0.890395426**, so `CH-0227`'s `1.12×` **over-states the coupled benefit at every one of the 64 cells** (`1.012–1.108×`). **And the tail runs the other way**: at **27 of 64** cells the 90th percentile of the per-realisation ratio is **above one** while the median is below it at **64 of 64**, single realisations reach **1.15725406**, and the ties are adverse at up to **27.45 %** of the ensemble. `CH-0228`'s load then decides the recovery: at the allowed **8.57142857°** every cell moves at most **0.00242194151** of the stroke — **5.7×** the winning margin — and exactly **1 of 64** is flat at each sign, a *different* one. The untied half reproduces all **128** of `C-0167`'s committed values at **`4.2e−9`** and the tied free tiles reproduce `C-0175`'s three at **`≤ 1.5e−9`**, so this is one object measured in two states and not two objects

| | |
|---|---|
| **Task** | [`T-279`](../tasks/T-279-tied-honeycomb-regrade.md) — `C-0167`'s 64 coupled cells re-graded on the tied honeycomb lattice |
| **Leaf** | `A8.2` |
| **Verification type** | **in-silico** (the same three-dimensional beam-and-bond lattice, the same exact Woodbury coupling surrogate and the same `C-0087`-measured incorporation as a Bernoulli dropout over 4 000 realisations on **one common stream restricted per cell**, with 59 covalent scaffold-turn ties added) **+ logical** (an exact bit-identity between the empty-tie lattice and the object `C-0167` measured, and a cheap bound taken out of `C-0167`'s own committed result file with no solve at all) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** `k_θ` at a scaffold turn is asserted equal to `k_θ` at a staple crossover because it is the same covalent object, not because anything measured it (`CH-0227` §7, inherited verbatim); the tie's axial station is taken at `s = ±L/2` exactly, where a scaffold crossover sits 5 bp from a staple position; `k_θ` itself is `Gen1Tile`'s **square-lattice-fitted** constant and `k_s` a construction. |
| **Verdict** | **PASS on all five predicates. `F1`, `F2`, `F3`, `F4`, `F7` and `F8` did not fire; `F5` and `F6` were declared open and BOTH FIRED, and their firing is the finding.** `P1` all 128 of `C-0167`'s committed values reproduce at `4.2e−9`; `P2` all 64 cells are re-graded at both ends of `C-0116`'s band with the count reported beside `C-0167`'s; `P3` the uniform-load falsifier holds at `0.0` on the tied lattice and through the tied coupled surrogate; `P4` the comparison is paired, one stream, read per realisation; `P5` two convergence axes are emitted **at the deciding cells**, and every same-quantity identity is a threshold and a boolean. |
| **Provenance** | [`gpd/results/T-279-tied-honeycomb-regrade.json`](../results/T-279-tied-honeycomb-regrade.json) (`tile.HoneycombTiedRegradeStudyKt`, **new**); model [`tile/HoneycombTiedRegrade.kt`](../../src/main/kotlin/tile/HoneycombTiedRegrade.kt) (**new file**, 2 functions). **NO SHARED SOURCE ON ANY NUMBER'S PATH IS EDITED** — `tile/HoneycombGrillage.kt`, `tile/HoneycombRasterTurnTies.kt`, `tile/HoneycombCoupledLattice.kt`, `tile/HoneycombGrillageRegradeStudy.kt`, `coupling/NonUniformCoupling.kt`, `coupling/DropoutRobustPlacement.kt` and `tile/CoupledFourLayer.kt` were **read, not edited**, which is why nothing `C-0154`, `C-0167` or `C-0175` published can move and no consumer re-run is owed. The one shared source touched is `structure/ResultInputs.kt`, which gains a `T_279` handle because the tree's invariant is *every result path spelled in a main source has a handle* — and that edit is **provably inert**: `ResultInputs.all` is read at **8 sites, every one of them in `structure/ResultInputsTest.kt`**, so no emitted number in the corpus can see it. **11 gate-named tests written first and watched fail** — [`tile/HoneycombTiedRegradeTest.kt`](../../src/test/kotlin/tile/HoneycombTiedRegradeTest.kt) — of which **two failed on the first real run and one was the author's mistake, not the code's** (§8) — and **mutation-tested afterwards**, four mutations over the eleven: taking the influence bank on the **prestrained** lattice instead of on `withoutPrestrain` (`C-0104`'s trap) fails **2**; ignoring the `tied` flag fails **5**; dropping the ties' axial-end alternation fails **1**; and taking the **free field** on `withoutPrestrain` too — so that the prestrain enters nowhere — fails **1**. The restored sources pass **11 of 11**. Result file **BYTE-IDENTICAL across two independent JVM runs**, after one reproduction column carrying a **departure** was re-emitted at two significant digits because two runs disagreed in exactly that field and nowhere else. A full `./gradlew test` on the final sources gives **3 315 tests in 191 classes, 0 failures, 0 errors**, run as `./gradlew test -x testQueueVocabularyMutations` because a concurrent agent's in-flight `tools/check-queue-vocabulary.py` mutation harness is red in the checkout at the moment of the run (6 of 6 mutations surviving, none of them this task's files); `check-result-file-hygiene.py` (`--prose`, `--departures`, `--saturated`), `check-kotlin-format-strings.py`, `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-entry-points.py` and `result-reader-census.py --check` are all clean. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp; crossover planes every **7 bp**, one pair per class every **21 bp**. Cross-section `10 × 6` (60 helices), block extent **116 bp = 39.44 nm** at `C-0151`'s recommended `102 / 109` raster, `edgeY` = 38.04 nm. `k_θ` = 13.5294118 pN·nm/rad, `k_s` = 64.7058824 pN/nm, link penalty `1e4` pN/nm; 4 320 unknowns, half-bandwidth 243, **435 staple bonds** and **59 raster turn ties** (`firstAxialSign = +1`, ties at `s = ±L/2`, 30 at the high rim and 29 at the low). `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the **gap-facing face only**; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Composite fractions **0.30** and **0.26** (`C-0116`, entering as `hingeStiffnessEnhancement` 21.1851817 and 18.4938242) plus the lattice's own **1.0** lower bound. Tie prestrain **0** for the primary deliverable and **±8.57142857°** (`C-0152` §5 / `CH-0228`) for the second. |
| **Consumes** | [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (`T-263`) — the 64 cells, the surrogate port, the stations, the distributions and the dropout stream, **read from its result file and reproduced**; [`C-0175`](C-0175-drawable-raster-rim.md) (`T-254`) — the tie set, its census and its three free-tile readings; [`C-0154`](C-0154-honeycomb-grillage.md) — the grillage and its bond census; [`C-0151`](C-0151-closing-raster-selection.md) (the `102 / 109` raster and the 116 bp extent), [`C-0152`](C-0152-forced-scaffold-crossover-price.md) (the allowed departure), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0058`](C-0058-non-uniform-coupling.md) (the influence surrogate), [`C-0103`](C-0103-path-count-at-fixed-geometry.md) (common random numbers), [`C-0104`](C-0104-row-end-prestrain.md) (a prestrain is a load, and the influence-function trap) |
| **Constrains** | **`CH-0227` §6 is DISCHARGED** — the coupled re-grade it asks for is run. **One challenge is raised**: [`CH-0234`](../challenges/CH-0234-no-verdict-reverses-was-a-free-tile-statement.md) against `C-0175`'s `CH-0227` status line, *"no verdict of `C-0154` or `C-0167` reverses"*, which is a **free-tile** statement generalised to a coupled one. **`CH-0228` is answered at the COUPLED level** and not closed: the load is worth at most 0.00242194151 of the stroke per cell, which is **5.7×** the margin that decides the two recovered cells. `C-0167`'s headline *"`0` of `64`"* is **superseded and not disputed** — it is right on the object it was taken on. |

---

## 1. The cheap bound, and it narrowed the question by four without answering it

`C-0167`'s committed result file already carries the 64 untied `p90` values, so the most favourable
transfer any multiplier hypothesis could offer is one pass over a JSON file:

| | |
|---|---|
| the free tile's own ratio | **0.890395426** (`C-0175`) |
| the untied `p90` a cell must be below to clear `T-5b` under that hypothesis | `0.10 / 0.890395426` = **0.112309652** |
| cells of `C-0167`'s 64 that meet it | **8** |
| the tightest untied cell in the corpus | **0.101931622**, **1.93 %** over the tolerance |

**Eight candidates, two realised — the bound over-predicted by 4× and contained the answer.**
And it is not a bound: a coupling changes the load path, and `C-0154`'s own composite fraction
reads **0.2468** on the rigidity against **0.9405** on the dishing, so the same lattice change is
worth 3.8× more on one functional than on the other. `C-0167` measured the same thing from the
other side, its own model change giving per-realisation median ratios of 1.064–2.475 at a
free-tile ratio of 1.868.

What **is** bounded before any solve is one direction only: `K_tied ⪰ K_untied` is a Loewner
statement, so the deflection **at** a unit point load falls — and that bounds nothing about peak
dishing, which is a seminorm of the field. That is `F7`, declared open.

## 2. The re-grade — the ties recover two cells

| | `C-0167`, untied | **this study, tied** |
|---|---|---|
| cells clearing `T-5b`'s 0.10 at the 90th percentile | **0 of 64** | **2 of 64** |
| cells flat at the **nominal**, no defects | 48 of 64 | 48 of 64 |
| the **uncoupled** block, `f = 0.30` / `0.26` | 0.0501417315 / 0.0522223659 — flat | **0.0446459684 / 0.0467367262** — flat |
| the uncoupled block with no enhancement at all | 0.132443428 — not flat | **0.12738041** — not flat |
| coupled cells worse than the uncoupled tile | 64 of 64 | **64 of 64** (`C-0109`, reproduced) |

The two cells that move, both at `f = 0.30` and both **rim-graded 5:1**:

| placement | paths | untied `p90` | **tied `p90`** | median per-realisation ratio | tied is worse at |
|---|---|---|---|---|---|
| **abstract grid**, `3 × 10` | 30 | 0.106041029 | **0.0995744767** | 0.937029766 | 3.65 % of realisations |
| **abstract grid on the rooting helices**, `5 × 10` | 50 | 0.101931622 | **0.0998791032** | 0.976539981 | 13.05 % of realisations |

Both were among the cheap bound's eight. **The margin is 0.426 % of the tolerance** at the tighter
of the two, which is why §5 spends its convergence budget there and not anywhere else.

## 3. It is not a multiplier — and the free tile is not even an order statistic of the answer

| | |
|---|---|
| the free tile's ratio (`C-0175`, `CH-0227`) | **0.890395426**, i.e. `1.123×` (its reciprocal) |
| the **median of the per-realisation ratio**, over the 64 paired cells | **0.902845544 to 0.988116016**, i.e. `1.012–1.108×` (their reciprocals) |
| the spread | **0.0852704727** |
| the largest departure of a cell's median ratio from the free tile's | **0.0977** |

**Every one of the 64 cells moves LESS than the free tile does.** So the `1.12×` is not merely *not
a multiplier*: it is a **ceiling** the coupled cells never reach, and a table rescaled by it would
be optimistic at every entry. By column count the median ratio runs 0.954–0.988 at one column,
0.903–0.982 at two, 0.903–0.958 at three and 0.925–0.977 at five — **not monotone in the path
count**, exactly as `C-0167` found for its own model change. *(Those four ranges are min/max over
this study's own `paired[*].medianRatio` column, grouped by `columns` and quoted to three digits;
the extremes at full precision are the two in the table above.)*

**And the tail runs the other way.** The median ratio is below one at **64 of 64** cells, and the
**90th percentile of the same per-realisation ratio is above one at 27 of them** — 1.00059803 to
1.01758487 — while single realisations reach **1.15725406** and the ties are adverse at between
0.2 % and **27.45 %** of the ensemble depending on the cell. *(The `27`, the `64 of 64`, the two
`p90Ratio` extremes and the two `fractionTiedIsWorse` extremes are counts and min/max over this
study's own `paired[*]` column of 64 records; the `1.15725406` is `F7`'s own emitted note.)* `CLAUDE.md`'s *a ratio of two ORDER
STATISTICS is not the order statistic of the ratio* read one level further out: the **distribution**
of the paired ratio straddles one at 42 % of the cells, and no summary of it — median, 90th
percentile, or a ratio of two 90th percentiles — carries that on its own.

*(The unpaired reading and the paired one agree in sign at 64 of 64 here, so `C-0167`'s six
sign disagreements have no analogue on this axis; the disagreement in this study is between the
median of the ratio and the 90th percentile of the same ratio.)*

## 4. `CH-0228` at the coupled level — the ties' own load decides which cell is recovered

> **THE COORDINATE OF THIS SECTION IS WITHDRAWN AND NOT ONE OF ITS NUMBERS IS**
> ([`CH-0240`](../challenges/CH-0240-the-allowed-departure-is-common-mode.md) **UPHELD** by
> [`C-0190`](C-0190-the-departure-is-common-mode-and-what-replaces-it.md), `T-291`, iteration 45).
> The four prestrained readings below, the `0.00242194151` and the `0.000540689993` all reproduce,
> and every one of them is the response to a **relative** roll — a coordinate on which an allowed
> scaffold crossover's departure has coefficient **exactly zero**, because a level displacement
> rotates both backbones the same way.
> **§3's zero-prestrain headline `2 of 64` is untouched**: it carries no prestrain at all, and the
> ties' **stiffness** deliverable is a property of the elements' presence.
> On the channel the raster actually demands — `17.1428571°` of twist on each of 58 interior
> helices — **0 of 64** cells are flat at either sign, so this section's *"which cell is
> recovered"* is a question about a load that is not applied.

The 59 ties are not only a stiffness. `C-0152` §5 establishes that caDNAno's `±5 bp` is an integer
approximation to a `5.25 bp` half turn, so **every allowed** honeycomb scaffold crossover already
sits **8.57142857°** off the line of centres — at every raster turn, forced or not, and the
recommended `102 / 109` raster forces **none**. Graded at that departure, with the influence bank
taken on `withoutPrestrain` as `C-0104` requires:

| cell | zero prestrain | **at `+8.57142857°`** | **at `−8.57142857°`** |
|---|---|---|---|
| abstract grid, `3 × 10` = 30 paths, rim-graded, `f = 0.30` | **0.0995744767** — flat | 0.10014682 — **not flat** | **0.0993228684** — flat |
| abstract grid on the rooting helices, `5 × 10` = 50 paths, rim-graded, `f = 0.30` | **0.0998791032** — flat | **0.099361573** — flat | 0.10109276 — **not flat** |

**At each sign exactly one of the two is flat, and it is a different one.** So the count is
**1 of 64 at either sign**, and *which* cell survives is set by a sign **no source in this
repository fixes** — `C-0175` says so in as many words and sweeps it on the free tile, where it is
worth 0.7 %.

Over all 128 prestrained cells the load moves the `p90` by at most **0.00242194151** of the stroke,
and the median of `|movementFromZeroPrestrain|` over this study's own 128 emitted records is
**0.000540689993** — which is a small number *and it is **5.7×** the **0.000425523** margin* by
which the tighter recovered cell clears the tolerance (`0.10 − 0.0995744767`). `CLAUDE.md`'s *an INITIAL STRESS is a
load, not a stiffness* meeting a threshold: a load too small to matter anywhere else is exactly the
size of the thing being decided.

## 5. Convergence, taken where the verdict is thin

`C-0167`'s convergence axis is the **nominal** of a `1 × 10`, equal-spring cell. That is not the
quantity this study's verdict rests on, so the axes are re-taken on the **`p90` of the deciding
cells**:

| cell | quantity | axis | departure | verdict survives |
|---|---|---|---|---|
| abstract grid, 30 paths, rim-graded, `f = 0.30` | the ensemble's `p90` | subdivisions 1 → 2 | **4.6e−4** | **yes** |
| abstract grid, 30 paths, rim-graded, `f = 0.30` | the ensemble's `p90` | sample grid 41 → 81 and 81 → 161 | **0.0**, **0.0** | yes |
| abstract grid on the rooting helices, 50 paths, rim-graded, `f = 0.30` | the ensemble's `p90` | subdivisions 1 → 2 | **1.0e−4** | **yes** |
| abstract grid on the rooting helices, 50 paths, rim-graded, `f = 0.30` | the ensemble's `p90` | sample grid 41 → 81 and 81 → 161 | **0.0**, **0.0** | yes |

*(the `departure` column is emitted at two significant digits, which is what `CLAUDE.md` asks of a
difference of two nearly equal numbers; `F8`'s own note carries the same worst value at three,
`4.57e−4`.)*

**0 of 6 deciding-cell steps move the verdict, at a worst departure of `4.57e−4` against a margin
of `0.00426` of the tolerance — a factor of 9.3.**

And the axis nobody would have looked at is the interesting one. On `C-0167`'s **own** convergence
cell the tied lattice reads **0.018** in the beam subdivision and **0.0073 / 0.0036** on the sample
grid, where the **untied control run here reads `1.1e−4`**, which is `C-0167`'s own published departure on that cell,
and reproduces its two subdivision values at **`3e−10`** and **`6.3e−10`**, so the 160× is the
**ties** and not this study's code. Adding 59 rim ties makes that particular cell's nominal far more
subdivision-sensitive and leaves the deciding cells' `p90` two orders better resolved than it.
*Convergence is a property of the quantity* — and here of the **cell**, on one lattice, in one run.

## 6. The five verification gates

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | the tied lattice asserted to carry exactly **59** ties and **435** bonds and the untied one 435 and none; every tie asserted to sit at an axial **rim** node, 30 at the high end and 29 at the low; the tie census asserted **50** through-thickness and **9** in plane, which is `C-0175`'s own split; a non-finite prestrain **refused** |
| **2 — limiting cases** | an **empty** tie list asserted bit-identical to `C-0167`'s object in the crossover **site set**, in `assembleLoad`, in the **point-load dual** and in the solved field at `1e−10`; and one level up, the whole **surrogate** — the object a coupled cell is actually made of — asserted equal at `1e−10` |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the tied lattice dishes `0.0`** and its mean deflection is `p/k_f` to `1e−9` with 59 rim ties present; the same through the tied **coupled** surrogate at vanishing coupling; the Loewner statement asserted where it is a statement — the deflection **at** a unit point load falls; the coupled field asserted exactly **linear** in the tie prestrain; the single-path **compliance** asserted invariant under the prestrain, which is `C-0104`'s trap taken on public quantities alone |
| **4 — numerical convergence** | beam subdivisions 1 → 2 and the dishing sample grid 41 / 81 / 161, taken on the **`p90` of each deciding cell** and on `C-0167`'s own cell in **both** tie states; the result file **byte-identical across two independent JVM runs**; every same-quantity identity emitted as a threshold and a boolean |
| **5 — literature and upstream** | **seven reproductions, worst `4.2e−9`**: `C-0175`'s three tied/untied free tiles (`1.1e−10`, `6.4e−10`, `1.5e−9`), `C-0167`'s fourth (`1e−10`), `C-0167`'s recommended-cell nominal at **both** subdivisions (`3e−10`, `6.3e−10`), and **all 128** of `C-0167`'s committed `p90` and `nominal` values in one aggregate row (`4.2e−9`, 128 of 128 closing) |

### The eight declared falsifiers

| # | falsifier | fired | outcome |
|---|---|---|---|
| `F1` | a uniform pressure on the tied lattice dishes exactly zero | **no** | `0.0` at 81 × 81 with 59 rim ties, and `p/k_f` to `1e−9` |
| `F2` | the untied re-grade reproduces `C-0167`'s 64 committed cells at `1e−8` | **no** | worst `4.2e−9` over 128 values |
| `F3` | an empty tie list is bit-identical to `C-0167`'s object | **no** | site set and load vector bit-identical; dual and field in the tests |
| `F4` | the tied free tile reproduces `C-0175`'s readings at `1e−8` | **no** | worst `1.5e−9` |
| `F5` | **the ties move NO flatness verdict — declared open** | **FIRED** | **2 of 64**, and that is `CH-0234` |
| `F6` | **the per-cell movement is a MULTIPLIER — declared open** | **FIRED** | largest departure from the free tile's ratio **0.0977**, over a median-ratio range of 0.902845544–0.988116016 |
| `F7` | the tied lattice reads worse than the untied one at some cell — **declared open** | **no** | 0 of 64 in the **median**; but 27 of 64 in the ratio's own 90th percentile, and 1.15725406 at the worst single realisation |
| `F8` | a moved verdict survives its own convergence axes — **declared open** | **no** | 0 of 6 steps move it, worst `4.57e−4` against a 0.00426 margin |

## 7. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- `k_θ` at a **scaffold turn** is asserted equal to `k_θ` at a **staple crossover** because it is
  the same covalent object, and `k_θ` itself is `Gen1Tile`'s square-lattice-fitted constant.
- The tie sits at `s = ±L/2` **exactly**; a scaffold crossover sits 5 bp from a staple position, so
  its true axial station is within **1.7 nm** of the rim node, and nothing here prices that.
- The lattice carries **no** across-helix parallel-axis term, so its `D_⊥` is the independent one
  and a lower bound (`C-0167` §8, unchanged); Kirchhoff is not safe at these thicknesses, so every
  `D_∥` is an upper bound.
- The dropout statistics are measured on a **single-layer Rothemund rectangle** and only the
  *profile* transfers, in nm; the ensemble perturbs the **coupling** and never the block's own
  crossovers **or its ties**. A missing scaffold turn is not in this model at all.
- The lattice carries **one row length**, at the 116 bp extent `C-0167` grades at; the `102 bp`
  interface window is not modelled.
- The prestrain deliverable sweeps the two **uniform** sign assignments only.
- **Nothing here re-opens the placement search, the distribution rule, the raster or the
  cross-section.** The stations are `C-0151`'s and the distributions `C-0058`'s two — and the two
  recovered cells are both *rim-graded*, which is a rule written on a smeared model's geometry.

## 8. What the failing tests found, and one of them was the author's

Two of the eleven tests failed on their first real run.

The Loewner test was written on a **uniform pressure** and read `1.5840799960358565` against
`1.5840799960333825` *(a test-run observation, not a field of the result file)* — the two lattices
agree to the last ulp, because a free body on a uniform
Winkler foundation translates rigidly whatever its rigidities, so the mean deflection is `p/k_f`
either way and the test was a test of nothing. It is now taken on the deflection **at** a unit
point load, which is `fᵀK⁻¹f` and is the quantity the Loewner ordering is about.

The uniform-load falsifier was written at the **full `C-0017` mandate** and duly read **0.0132**.
That is not a defect: `C-0058`'s paths run to **ground**, so under a uniform pressure they pull the
tile out of its own rigid translation — *an attachment coupling can be a NET DISHING SOURCE*, which
is this corpus's own finding. The falsifier belongs on the **free** field, which is where `C-0167`
takes it, and the tied lattice satisfies it as exactly as the untied one does. **A falsifier that
fires because the author asserted the wrong statement is worth as much as one that fires on the
code**, and it is the reason this claim can say what the coupling does under a uniform load at all.

## 9. Open questions

- **Whether the recommended `10 × 6` block needs an attachment coupling at all.** The uncoupled
  **tied** block is flat at both ends of the measured band (0.0446459684 / 0.0467367262) and
  **64 of 64** coupled cells are worse than it. This is now the third claim to reach that
  statement and no claim has asked what a tie-less Gen-1 tile costs elsewhere in the stack.
- **What fixes the sign of the turn prestrain.** It decides which of the two recovered cells is
  flat, and no source in this repository fixes it.
- **What the tie's true axial station is worth.** A scaffold crossover sits 5 bp from a staple
  position; the ties here are at the rim node exactly, and the deciding margin is 0.43 %.
- Why the **tied** lattice's beam-subdivision sensitivity at a `1 × 10` equal-spring cell is 160×
  the untied one's, when the deciding cells are two orders better resolved.
- Whether a distribution **searched** on the tied lattice recovers more than two cells. Every
  distribution graded here is a rule written on a smeared model's geometry, and both recovered
  cells are rim-graded.
- Whether the `102 bp` interface window, modelled as a restricted bond set, moves either recovered
  cell.
