# C-0142 — **A coupled four-layer tile is STILL flat at the 90th percentile under the measured folding statistics, and the corrected honeycomb cross-section halves the evidence for it: `C-0118`'s nine flat cells of sixteen are FOUR, all four are on `10 × 6`, and `15 × 4` is 0 of 8 at BOTH ends of the measured coupling band.** The best cell moves 0.0278431488 → **0.0680677948** and stays the sparsest one tested; `15 × 4`'s only flat cell moves 0.0882933461 → **0.145354102**; the cross-section's worth falls 3.17109774× → **2.13543134×**. **And the path count stops being a request**: snapped onto `C-0141`'s honeycomb station lattice all sixteen placements are realisable, moving a station by at most **3.332 nm** inside a 3.57 nm ceiling, and **three** of them are flat — the first end-to-end coupled flatness result in this programme that stands on stations a derived lattice supplies

| | |
|---|---|
| **Task** | [`T-232`](../tasks/T-232-coupled-cells-at-the-honeycomb-cross-section.md) — re-grade `C-0118`'s sixteen coupled cells at the corrected honeycomb cross-section |
| **Leaf** | `A8.2` |
| **Verification type** | **in-silico** (an influence surrogate over the four-layer grillage, `C-0087`'s measured per-site incorporation as a Bernoulli dropout over 4 000 realisations on **one common stream**) **+ logical** (the station lattice each placement stands on, and the cheap geometric bound that ran before it) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The *folding* statistics graded against are measured; the flatness is not. The cross-section is a **lattice** statement (`C-0141`), not a measurement of a folded object. |
| **Verdict** | **PASS on all four predicates. `F1`, `F2`, `F3` and `F4` did not fire; `F5` FIRED, and it was declared for exactly this.** `P1` all sixteen cells are re-graded with the exceedance's one-sided bound where saturated; `P2` the standing geometry reproduces `C-0118` at `1.4e−10 … 4.4e−9` **in the same process**, and the two are compared per realisation; `P3` the uncoupled reference is reported at both ends of the measured band; `P4` every placement is available on the honeycomb station lattice and is graded there. |
| **Provenance** | [`gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json`](../results/T-232-coupled-cells-at-the-honeycomb-cross-section.json), produced by `tile.HoneycombCoupledStudyKt`; model [`tile/HoneycombCoupledTile.kt`](../../src/main/kotlin/tile/HoneycombCoupledTile.kt), tests [`tile/HoneycombCoupledTileTest.kt`](../../src/test/kotlin/tile/HoneycombCoupledTileTest.kt) (**10**, written first, and **mutation-tested**: un-snapping the grid and substituting a ratio of medians for a median of ratios fails 3 of them). Geometry consumed unmodified from [`tile/HoneycombFaceLattice.kt`](../../src/main/kotlin/tile/HoneycombFaceLattice.kt). |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb bond length `d` = 2.536 nm; in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; 21 bp per interface; 112 bp span; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, 2 beam subdivisions, `T-5b`'s 0.10, ladder phase 0 with the forced 7 bp inter-row stagger. |
| **Consumes** | [`C-0118`](C-0118-coupled-four-layer.md) (the sixteen cells re-graded), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) / [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md) (the corrected cross-section and the station lattice), [`C-0120`](C-0120-cross-section-comparison.md), [`C-0116`](C-0116-composite-fraction-threshold.md) (the measured 0.26–0.33 band), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0129`](C-0129-result-file-hygiene.md) (the one-sided bound), [`C-0103`](C-0103-path-count-at-fixed-geometry.md) (common random numbers) |
| **Constrains** | **Two challenges are raised.** [`CH-0176`](../challenges/CH-0176-the-first-flat-coupled-tile-is-flat-at-half-its-cells.md) against `C-0118`'s title, §2, §3 and `Verdict` row; [`CH-0177`](../challenges/CH-0177-the-path-count-axis-is-not-monotone.md) against its §2 monotonicity, which its own four printed numbers refute at **both** geometries. `C-0141`'s §9 open item — *"`C-0118`'s 16-cell dropout grading is NOT re-run"* — is **DISCHARGED**. `C-0109`'s *"every coupled cell is worse than the uncoupled tile"* reproduces at 16 of 16 corrected cells. |

---

## 1. The cheap bound, and it said the run was necessary rather than a formality

Three multiplications and a division, before any Monte Carlo:

| | |
|---|---|
| honeycomb plan per helix, `3√3/4 · d²` | **8.35449857 nm²** |
| the standing cross-section's, `d²` | 6.431296 nm² |
| ratio | **1.29903811** |
| every four-layer `edgeY` | **exactly 1.5×** too small |
| stations per rooting helix at 112 bp on the 21 bp ladder | **6** — so 90 on `15 × 4`, 60 on `10 × 6` |

**The across-helix ATTACHMENT pitch is `edgeY / rasterRows` identically**, so it *is* the in-plane row
pitch, and it moves by that same 1.5×. That single quotient is how the geometry reaches the coupling,
and it is why a correction to a *cross-section* moves a *dropout percentile* at all.

Every requested path count — 15/30/45/75 on `15 × 4`, 10/20/30/50 on `10 × 6` — is inside its own
station inventory, so **no cell fails on COUNT**, and the placement question is one of position.

**What the bound could not settle.** The free-tile reference moves 1.69× on `15 × 4` and 2.75× on
`10 × 6`, so a proportional transfer of `C-0118`'s own cells lands `10 × 6` at 0.076–0.171 —
**straddling `T-5b`'s 0.10**. That is the reading that bought the run.

---

## 2. The geometry, consumed rather than re-derived

`C-0141` lifted `edgeY`, the in-plane pitch and the layer spacing out of `C-0120`'s construction as
parameters; this study passes them and changes nothing else.

| | `edgeY` | of §3's 40.35 | `D_∥` | `D_⊥` | reach along / across | stations |
|---|---|---|---|---|---|---|
| `15 × 4` standing | 38.04 | 0.942750929 | 4547.17603 | 240.931249 | 34.64 / 16.62 | 90 |
| **`15 × 4` honeycomb** | **57.06** | **1.41412639** | 2334.05068 | 278.255762 | 29.32 / 17.23 | 90 |
| `10 × 6` standing | 25.36 | 0.62850062 | 15189.564 | 804.816135 | 46.84 / 22.47 | 60 |
| **`10 × 6` honeycomb** | **38.04** | **0.942750929** | 7685.47603 | 916.230312 | 39.50 / 23.21 | 60 |

`edgeY` here is the **plate** convention, `rasterRows × the in-plane pitch`, which is what the
grillage's own `lengthY = beamCount × interhelicalDistance` uses and what `C-0141` §7 quotes.
`C-0141` §2's 56.524 / 37.504 nm are the block **envelope**, one duplex diameter smaller; both are
correct and they are 2.00 nm apart, which is why a test pins all four.

**The uncoupled reference at both ends of the measured band** (`C-0116`'s threshold for `15 × 4` now
sits *inside* 0.26–0.33, so a single-`f` verdict is not a verdict):

| | `f = 0.26` | `f = 0.30` |
|---|---|---|
| `15 × 4` standing | 0.0612595739 flat | 0.0577199433 flat |
| **`15 × 4` honeycomb** | **0.101759944 NOT flat** | 0.0978155002 flat |
| `10 × 6` standing | 0.00927188486 flat | 0.00874363524 flat |
| **`10 × 6` honeycomb** | **0.0255589305 flat** | **0.0240648102 flat** |

All six of `C-0120`'s and `C-0141`'s published references reproduce at `1.8e−10 … 3.5e−9`.

---

## 3. The sixteen cells, re-graded

At the measured `f = 0.30`, on the abstract grid `C-0118` grades — so that the *only* difference is
the geometry:

| cross-section | columns | paths | distribution | `C-0118` p90 | **corrected p90** | corrected verdict |
|---|---|---|---|---|---|---|
| `15 × 4` | 1 | 15 | equal springs | 0.131685589 | **0.213735801** | not flat |
| `15 × 4` | 1 | 15 | rim-graded 5:1 | 0.186011867 | **0.304635002** | not flat |
| `15 × 4` | 2 | 30 | equal springs | 0.155081687 | **0.250904784** | not flat |
| `15 × 4` | 2 | 30 | rim-graded 5:1 | 0.206737248 | **0.336722611** | not flat |
| `15 × 4` | 3 | 45 | equal springs | 0.133271547 | **0.219381554** | not flat |
| `15 × 4` | 3 | 45 | rim-graded 5:1 | 0.104871904 | **0.178613247** | not flat |
| `15 × 4` | 5 | 75 | equal springs | 0.124585773 | **0.198234404** | not flat |
| **`15 × 4`** | **5** | **75** | **rim-graded 5:1** | **0.0882933461 flat** | **0.145354102** | **NOT FLAT** |
| **`10 × 6`** | **1** | **10** | **equal springs** | **0.0278431488** | **0.0680677948** | **FLAT** |
| `10 × 6` | 1 | 10 | rim-graded 5:1 | 0.0306268096 | **0.102582764** | not flat |
| `10 × 6` | 2 | 20 | equal springs | 0.0541089284 | **0.119502047** | not flat |
| `10 × 6` | 2 | 20 | rim-graded 5:1 | 0.0623145994 | **0.168817101** | not flat |
| `10 × 6` | 3 | 30 | equal springs | 0.0461988976 | **0.101905503** | not flat |
| **`10 × 6`** | **3** | **30** | **rim-graded 5:1** | **0.0441544716** | **0.0954158305** | **FLAT** |
| **`10 × 6`** | **5** | **50** | **equal springs** | **0.0408747025** | **0.0900369** | **FLAT** |
| **`10 × 6`** | **5** | **50** | **rim-graded 5:1** | **0.0366559399** | **0.0822611821** | **FLAT** |

**Four of sixteen, all on `10 × 6`.** At the band's adverse low end `f = 0.26` it is the **same four
cells** — 0.072431426, 0.0968178426, 0.0923901454, 0.0832291872 — and `15 × 4` is again 0 of 8, so
neither verdict is an artefact of reading the band at one point.

**None of the sixteen `exceedance` proportions is saturated at the corrected geometry**, so
`exceedanceOneSidedBound` is `null` throughout and the symmetric `√(p̂(1−p̂)/n)` is the right
instrument — 0.00108713356 at the best cell's 0.00475, 0.00308909119 at 0.03975. At `C-0118`'s
geometry **six** of its `10 × 6` cells sat at exactly `p̂ = 0`, where the symmetric error is
identically zero and only `C-0129`'s one-sided bound (0.000748652688 at 4 000 draws) says anything.
**The correction moved the statistic off its own degeneracy**, which is `C-0129` read from the
other side.

---

## 4. The paired reading, and it is 14 % away from the unpaired one

`CLAUDE.md`: *"a ratio of two ORDER STATISTICS is not the order statistic of the ratio."* The two
geometries are graded on **one** common stream — same seed, same path count, same deviate per
station per realisation, only the incorporation thresholds moving — so the cost of the geometry can
be read per realisation:

| | median of the per-realisation ratios | ratio of the two 90th percentiles | fraction of realisations worse |
|---|---|---|---|
| `15 × 4`, over its eight cells | **1.62966102 – 1.7893938** | 1.59114801 – 1.70315632 | 0.933 – 0.9975 |
| `10 × 6`, over its eight cells | **2.14530087 – 3.823843** | 2.1609551 – 3.34944336 | 0.958 – 0.99875 |

The largest disagreement is the `10 × 6` single-column rim-graded cell: **3.823843** paired against
**3.34944336** unpaired, **14.2 %**. And the direction of the cost is itself a finding: **the sparser,
stiffer tile pays MORE**, because the correction widens the attachment pitch by 1.5× and a wider
pitch costs a sparse coupling more than a dense one. `F4` was declared against the two readings
disagreeing in *sign*; they do not, at 0 of 16 rows.

---

## 5. The path count stops being a REQUEST — `C-0118`'s largest open item, discharged

`C-0118` §5 states plainly that *"a path count here is a REQUEST, not a demonstration that the
stations exist, and that is the largest open question this claim leaves."* `C-0141` supplies the
stations. This is the one step between them.

Each cell's abstract grid is snapped along the helices onto its own row's 21 bp ladder, at phase 0
with `C-0141`'s **forced** 7 bp inter-row stagger. **All sixteen are realisable** — no cell collides
two of a row's stations, and the largest a station moves is **3.332 nm**, inside the 3.57 nm
half-ladder-pitch ceiling a nearest-station snap cannot exceed:

| | snap departure | p90 on the lattice | |
|---|---|---|---|
| **`10 × 6`, 1 column, 10 paths, equal** | 2.38 nm | **0.0863028445** | **FLAT** |
| **`10 × 6`, 3 columns, 30 paths, equal** | 3.17333333 nm | **0.0973238201** | **FLAT** |
| **`10 × 6`, 5 columns, 50 paths, equal** | 3.332 nm | **0.0868937148** | **FLAT** |
| `10 × 6`, 1 column, rim-graded | 2.38 nm | 0.111376749 | not flat |
| `10 × 6`, 2 columns, equal / rim | 2.38 nm | 0.125476912 / 0.183045719 | not flat |
| `10 × 6`, 3 columns, rim-graded | 3.17333333 nm | 0.14299002 | not flat |
| `10 × 6`, 5 columns, rim-graded | 3.332 nm | 0.108415983 | not flat |
| `15 × 4`, all eight | 2.38 – 3.332 nm | 0.190458402 – 0.35283273 | none flat |

**Three cells are flat on stations a derived lattice supplies**, and that is the first coupled
flatness result in this programme that is not standing on an abstract grid.

**Every one of the three is EQUAL SPRINGS.** On the lattice the rim grading is worse at all four
counts, where on the abstract grid it wins at two of four — the snap breaks the rim/interior
partition the grading was written against, because a station's snapped position depends on its row's
parity and the rim rule reads its unsnapped one. **A distribution rule is a property of a station
set, never of a tile** (`CLAUDE.md`), and here that is measured rather than asserted.

---

## 6. The five gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | lengths nm, stiffness pN/nm, rigidity pN·nm, dishing and composite fraction dimensionless; the plate `edgeY` is asserted equal to `rasterRows × the in-plane pitch` in the tile's own `init`, which is the identity the grillage's `lengthY` assumes |
| **limiting cases** | a placement wider than its ladder is **refused** rather than snapped (it would be a change of the path COUNT wearing a change of position); a sample compared against itself is exactly 1.0 at every percentile; the mandate still refuses a zero total and zero paths |
| **symmetry / conservation** | every distribution sums to the mandated total exactly; the influence bank is load-independent, so one bank serves every distribution of a cell; the two geometries share **one** dropout stream, which is what makes the difference a design difference |
| **numerical convergence** | dropout realisations 1000 / 2000 / 4000 on the common stream, departure **3e−11** between the last two — a convergence rather than a variance, because the stream is restricted and not redrawn. The whole result file is **byte-identical across two independent JVM runs** (the second differing only by the `sources` declaration added between them) |
| **literature cross-check** | the dropout is `C-0087`'s measured per-site map; the cross-sections are `C-0119`'s published designs; the honeycomb geometry is `C-0141`'s reading of the caDNAno paper. **Twenty-two reproductions**: `C-0118`'s sixteen cells at `1.4e−10 … 4.4e−9`, and six uncoupled references from `C-0120` and `C-0141` at `1.8e−10 … 3.5e−9`. Nothing new is cited |

**The falsifiers.** `F1` (the standing cells do not reproduce) did not fire — worst 4.4e−9. `F2` (the
corrected references do not reproduce) did not fire — worst 3.5e−9. `F3` (a p90 below its own
nominal) did not fire — 0 of 64 cells. `F4` (the paired and unpaired readings disagree in sign) did
not fire — 0 of 16 rows. **`F5` FIRED**: `10 × 6` loses four of its eight cells, and it was declared
as a falsifier rather than as an expectation precisely because the cheap bound's proportional
transfer straddled the tolerance.

**The smoke run is not the result.** At 300 realisations `F1` fired (worst departure 0.066) and `F5`
fired on a *different* cell set; nothing but the sample count changed. `CLAUDE.md`'s rule was
followed and the smoke pass was read only for the prose paths, the argmins and the serialisation.

---

## 7. What this means for `15 × 4` against `10 × 6`

`C-0141` gave three independent reasons to prefer `10 × 6` — rigidity, a footprint that is **0.93** of
§3's against `15 × 4`'s **1.41**, and a centro-symmetric placement family that exists on one and
provably not on the other. This adds the fourth and it is the strongest, because it is a **verdict**
rather than a margin:

- **`15 × 4` has no coupled design.** 0 of 8 at `f = 0.30`, 0 of 8 at `f = 0.26`, 0 of 8 on the
  lattice. Its *uncoupled* tile fails `T-5b` at the band's low end, and `C-0017`'s mandate makes the
  uncoupled tile unavailable anyway — so there is nothing left to recommend.
- **`10 × 6` has four**, at both ends of the band, three of them on stations the lattice supplies,
  and the best is the **sparsest coupling tested** — one column, ten paths, equal springs.
- **The cross-section still beats the distribution**, 2.13543134× against what either distribution
  buys inside either tile. `C-0118`'s design order survives; only its magnitude moves.

**And the recommendation is now unambiguous where it was previously a trade.** `C-0120` charged
`10 × 6` two-thirds of the footprint; `CH-0174` removed that charge; this removes the last reason to
keep `15 × 4` in the design space at all.

---

## 8. Validity range, and what this does NOT establish

- **The cross-section is a LATTICE statement** (`C-0141`): no folded object is measured.
- **The tile is a SMEARED equivalent sheet.** `OrigamiGrillage` never reads `layers` and its
  crossover combinatorics are square-lattice; only `edgeY`, the in-plane pitch and the layer spacing
  carry the honeycomb into the solve. Every number here inherits that.
- **The dropout is measured on a single-layer Rothemund rectangle**, and only the *profile* transfers,
  in nm. `C-0109`'s assumption, inherited — and the corrected `edgeY` changes the tile's
  perimeter-to-area ratio again, which `C-0087`'s own *"a boundary-layer measurement does not
  transfer between two tile sizes"* says is not free. **This is the largest inherited limitation and
  it is now larger than it was.**
- **The ROW LENGTH is carried unchanged at 112 bp, and that is a fourth moved input this claim does
  NOT move.** `C-0140` (iteration 34) shows a `15 × 4` honeycomb x-raster carries **both** turn
  senses, so no uniform row length exists at all, and its recommendation is a two-length raster at
  **112 / 108 bp** whose axial extent is 116 bp = **39.44 nm** against the 38.08 nm solved here —
  **+3.57 %** in `edgeX`. It is carried unchanged deliberately: `C-0118` grades at 112 bp, and
  holding `edgeX` fixed is what makes the movement reported here attributable to the **cross-section**
  and nothing else. A re-grade at 39.44 nm is the successor, and `C-0141`'s station counts and plan
  ceilings move with it.
- **The lattice-snapped placement is read at ONE ladder phase and ONE of the two admissible inter-row
  offsets.** `C-0141` carries 21 phases and both offsets; a phase sweep could only improve the
  snapped cells, so the three flat ones are a **lower** bound on what the lattice admits.
- **Two distributions only.** `C-0089`'s percentile descent (worth 1.30–1.61× on an array) and
  `C-0093`'s shared body are not run, and both could move cells that currently fail.
- **The mandate is read at §3's ACCEPTABLE clause.** The desired clause gives 10 pN/nm and a
  different device.
- **`C-0022`'s collar is read unchanged at both aspect ratios**, and both `edgeY` values have now
  moved by 1.5×. `C-0123`'s question is reopened, not answered.
- **Kirchhoff is not safe at these thicknesses**, so every `D_∥` is an upper bound and more so on
  `10 × 6`.
- **`15 × 4` at 57.06 nm across is 1.41× §3's footprint**, which is a specification question this
  claim does not settle and which runs the same way as its flatness verdict.
