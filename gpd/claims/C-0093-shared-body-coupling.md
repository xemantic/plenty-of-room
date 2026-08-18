# C-0093 — **A coupling that is not an array does not escape `C-0089`'s count argument; it weakens it by 2.2× in level and 3.0× in slope, and still asks for 252 ties against the 53 the lattice offers.** The escape is real and it is a **division**: under a shared body `C-0017`'s mandate lives in the body's GROUND, which is a rigid-body mode of the tile and therefore invisible to dishing, so the ties are freed from 0.98 to 3.33 pN/nm and the best 90th-percentile dishing anywhere falls to **0.24028028** — the lowest this programme has reached under the measured dropout, still **2.40×** `T-5b`'s 0.10

| | |
|---|---|
| **Task** | [`T-162`](../tasks/T-162.md), raised by [`C-0089`](C-0089-dropout-robust-placement.md) (`T-155`), open item 3 — *"the only structural escape this claim can see"* |
| **Leaf** | **`A8.2`** (the flatness of the tile), with **`A1.2`** for the anchoring scheme the coupling belongs to |
| **Verification type** | **logical** (a closed-form condensation identity, its rigid limit and its rank statement, all verified before any sampler; plus a plan-length division that needs no solve) **+ in-silico** (`C-0058`/`C-0063`/`C-0087`/`C-0089`'s own exact Woodbury surrogate on `C-0009`'s grillage under `C-0022`'s **solved** load, with the coupling's *topology* changed and `C-0087`'s seeded Bernoulli dropout unchanged, 10 000 realisations per cell) |
| **Verdict** | **PASS on the predicate, and the answer is NO — but the negative is a much narrower one than `C-0089`'s.** A shared body is **not** a rescaling of an array: it is the same Woodbury system with one term added, `(T⁻¹ + M + F_b) f = w_free`, and the array is its `F_b = 0` corner. **The declared falsifier `F1` did NOT fire**: a rigid shared body at `C-0063`'s own 34 stations dishes **0.0344013403** of the stroke at zero defects against the array's 0.0706145537 — 2.05× flatter on the identical station set, and `T-5b`-flat at every rung of the tie ladder. Under `C-0087`'s measured dropout the best of **39 graded cells** is **0.24028028** at the 90th percentile — a 6 × 15 grid of 90 rigid ties — against the array's 0.532748246 on the same stations (**2.22×**) and against `C-0089`'s best-of-22 **0.284537599** (**1.18×**), with the **exceedance falling from 100 % to 76.6 %** where `C-0089`'s lowest anywhere was 97.16 %. **The mechanism is a division and it needs no solve**: `C-0017`'s 33.3333 pN/nm is a **sum** over an array's paths, so at 34 paths each station is held at **0.980392157 pN/nm**; under a shared body the same equality is `series(Σtᵢ, g)` rather than `Σᵢ series(tᵢ, gᵢ)`, the compliance moves into the body's ground — **99.90 %** of it at the stiffest ties — and the tie is capped instead by `C-0049`'s per-path **force**, 3.33333333 pN/nm, **3.4×** more local support. **What still refuses it is the count, measured rather than asserted**: over seven grids from 15 to 180 ties the shared body's 90th percentile falls with a redundancy slope of **−0.784357442** against the array's **−0.264642174**, **2.96× steeper**, and the log-log fit crosses 0.10 at **252.126899 ties** — an extrapolation only 1.40× beyond the densest grid measured — against the **53** upward crossover sites `C-0066` counts at phase 24: **4.8× short**, where `C-0089`'s array is 5.7× short of 34. **And the body's own rigidity is a first-order variable, not an idealisation**: a four-layer honeycomb brick is 1.564× the rigid body's condensed station compliance and a single-layer sheet 2.311×, and at `C-0063`'s 34 stations the brick reads **0.100166871** at zero defects — *worse than the array*. Raises [`CH-0108`](../challenges/CH-0108-the-flexure-bound-is-a-bound-on-a-path-count-not-on-a-family.md) and [`CH-0109`](../challenges/CH-0109-a-cancellation-and-a-robust-design-are-different-designs-only-within-one-topology.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED.** The dropout **input** is `C-0087`'s, which is Strauss et al. (2018) read directly. The **motif** — a second origami layer tied to the sheet at many out-of-plane crossover sites — is the one in this neighbourhood with a **published precedent** (`C-0055`, Ke et al. 2009); what has no precedent is a Gen-1 device built that way, and **§3 describes a single-layer tile**. |
| **Provenance** | `gpd/results/T-162-shared-body-coupling.json`, produced by `coupling.SharedBodyCouplingStudyKt`; model in `src/main/kotlin/coupling/SharedBodyCoupling.kt` with one additive method (`InfluenceSurrogate.solveWithSharedBody`) in `src/main/kotlin/coupling/NonUniformCoupling.kt`, which `solveWithDropout` now delegates to; **6 cheap bounds, 5 mandate rows, 6 rank rows, 14 ground-element rows, 3 bodies, 63 zero-defect cells, 39 graded dropout cells at 10 000 seeded realisations each, 14 density rows, 7 buildability rows, 3 convergence axes, 7 reproductions, 7 predicates, 5 falsifiers**; **21 gate-named tests in `src/test/kotlin/coupling/SharedBodyCouplingTest.kt`**; the result file was **produced three times on separate snapshots** — the first two, of identical code, differed in **one field of the whole file**, the Ritz axis' convergence *departure* at `3.19469867e−11` against `3.19472365e−11`, which is the JIT-summation effect `CLAUDE.md` records and which `RESULT_ABSOLUTE_FLOOR` cannot catch because a departure between two dimensionless dishing ratios is not in the locked units (`P-18`); convergence departures are now emitted at **two significant digits** per `CLAUDE.md`'s own rule and the third run differs from the first in exactly the five fields the two repairs touch; `tools/verify.sh` **BUILD SUCCESSFUL in 20 m 58 s** on the whole suite with **no `--drop-file`**, and the three post-Gradle gates clean — 45 census self-checks, the census itself (85 studies, 60 direct + 27 transitive read edges) and **0 table defects in 303 files** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** sheet, 15 duplexes at the SAXS-measured **2.69 nm**; `C-0022`'s **solved** edge profile at **2 mM, 10 nm, 0.192 V** — `C-0063`'s, `C-0087`'s and `C-0089`'s design state; `C-0017`'s **33.3333333 pN/nm** as a **SUM** at §3's **acceptable 3 nm**; free-tile stroke **4.90731102 nm**; dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; grading **seed 20260817** (`C-0087`'s own) at **10 000** realisations; the body's ground is a **distributed** element scaled by a bracketed root so the coupling's heave secant is the mandate exactly; Ritz degree **4** for a flexible body, **1** (the three rigid modes) for the rigid limit; decisions at 6 significant digits, emission at 9 |
| **Consumes** | [`C-0089`](C-0089-dropout-robust-placement.md) (the question, the 0.284537599 comparison, the 195-path run-length demand, `DropoutEnsemble`/`summariseDropoutDishing`/`orderStatistic`), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (**the whole dropout model**; its 0.5010 and 0.6391 both **reproduced**), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`; its 0.0753 reproduced), [`C-0063`](C-0063-upward-root-placement.md) (**the 34-root placement**, read from `gpd/results/T-125-*.json`; its 0.0706145537 reproduced to `2.9e−10`), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0026`](C-0026-one-row-per-duplex.md) (the free-tile stroke, the one-row-per-duplex grid), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (the per-path ceiling `a/s`), [`C-0069`](C-0069-output-element-placement.md)/[`CH-0081`](../challenges/CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md) (the 8.19 nm plan budget and the 22.414 nm flexure floor, **reproduced to `8.6e−6`**), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (the arm slab and the 53-site `EAST` inventory), [`C-0061`](C-0061-stacked-arm-sheet.md) (the arm slab's height), [`C-0035`](C-0035-flexure-mounting-sense.md) (§3's effort point), [`C-0055`](C-0055-unused-junction-site.md) (the multilayer precedent), [`C-0025`](C-0025-flexure-end-joint.md)/[`C-0034`](C-0034-guided-arm-anchorage.md) (the end-condition coefficients), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0015`](C-0015-crossover-phase-and-registration.md)/[`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0108`](../challenges/CH-0108-the-flexure-bound-is-a-bound-on-a-path-count-not-on-a-family.md) against `C-0069`/`CH-0081`'s *"a bound on the family"*, and [`CH-0109`](../challenges/CH-0109-a-cancellation-and-a-robust-design-are-different-designs-only-within-one-topology.md) against `C-0089`'s Deliverable 2 and its amplification column |

---

## The claim, in one line

**An array spends `C-0017`'s mandate at every station; a shared body spends it once, in a mode the
tile cannot dish in — which is worth 2.2× in flatness and 3.0× in the redundancy slope, and is
still not worth the four-and-a-half-fold count the tolerance asks for.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, plate rigidity **pN·nm**, pressure
  **pN/nm² = 1 MPa exactly**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward** — away
  from the grafted layer, which is **below** the tile. `w` is positive **downward**; the origin is
  the tile centre.
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit
  **plane**, on the same **81 × 81** grid as every flatness claim upstream. **Flat** means below
  **0.10** of the free-tile stroke — `T-5b`'s **convention**, not a physical threshold.
- A **tie** runs from a tile station to the shared **body**; the body runs to **ground** through
  its own element. A tie force is positive **upward on the tile**, therefore downward on the body.
- **A dropout is a REMOVAL, not a perturbation** (`C-0087`), and it removes the tie — the body
  stays. That is precisely the distinction this task exists to price.
- **The operating state is named**: `C-0022`'s **solved 2 mM / 10 nm / 0.192 V** profile. This is
  the **ninth** time this programme has had to say which state a flatness verdict is read at.
- **The verdict statistic is the 90th percentile**, a nearest-rank order statistic, and it is
  `C-0087`'s and `C-0089`'s so all three claims are comparable cell for cell.

---

## The topology, as one added term

Write `M` for the free tile's compliance at the stations, `T = diag(t)` for the tie stiffnesses,
`Φ` for the body's mode shapes there and `H` for the body's own stiffness — bending plus ground —
in those coordinates. Then

| | |
|---|---|
| **array** | `(T⁻¹ + M) f = w_free` |
| **shared body** | `(T⁻¹ + M + F_b) f = w_free`, `F_b = Φ H⁻¹ Φᵀ` |

**The shared body is an additive compliance in series with the ties and nothing else**, so the
array is its `F_b = 0` corner and one code path carries both. `InfluenceSurrogate.solveWithDropout`
now delegates to `solveWithSharedBody`, so nothing `C-0058`, `C-0063`, `C-0087` or `C-0089`
publishes can move — and `C-0063`'s 0.0706145537 reproduces to `2.9e−10`.

Condensing the body out gives what the tile feels,

> `K_c = T − T Φ (Φᵀ T Φ + H)⁻¹ Φᵀ T`

which is symmetric, positive **semi**-definite, and **not diagonal**. That is the whole of the
difference.

---

## The three cheap bounds, which ran first

### Bound 1 — the rank statement, and it reproduces three separate `CLAUDE.md` entries at once

For a **free** rigid body (`Φ = [1, x, y]`, `H = 0`) the condensation is `T − TA(AᵀTA)⁻¹AᵀT`, of
rank `max(n − 3, 0)`. Measured, at equal ties, as the largest entry of `K_c` over the tie
stiffness:

| ties | expected rank | largest `K_c` entry / tie | adds exactly zero? |
|---|---|---|---|
| **1** | 0 | **0.0** | **YES** |
| **2** | 0 | **1.36e−12** | **YES** |
| **3** | 0 | **4.39e−12** | **YES** |
| 4 | 1 | 0.260851144 | no |
| 5 | 2 | 0.79655714 | no |
| 6 | 3 | 0.822198907 | no |

*"A body attached at ONE point and otherwise free adds EXACTLY ZERO stiffness there"*, *"two ties
on a straight host still add no bending, because two points determine a line"* and *"the lever's
own `EI` is engaged only at three ties"* are **one formula**, and the formula also says what the
fourth tie buys. Physically: a free body carries no net force and no net moment, so it supports
only the **non-affine** part of the station displacement — which is exactly what dishing measures.
Gate 3 asserts the consequence directly: **a free shared body under a uniform load applies zero
tie force**, at `1e−9` of the applied load.

### Bound 2 — the mandate arithmetic, which is the escape in one division

| paths | array per station [pN/nm] | shared-body tie ceiling from force [pN/nm] | ratio |
|---|---|---|---|
| 15 | 2.22222222 | 3.33333333 | **1.5** |
| 30 | 1.11111111 | 3.33333333 | **3.0** |
| **34** | **0.980392157** | **3.33333333** | **3.4** |
| 45 | 0.740740741 | 3.33333333 | **4.5** |
| 90 | 0.37037037 | 3.33333333 | **9.0** |

`C-0017`'s mandate is an equality on the coupling's **heave secant**. In an array that secant is
`Σ tᵢ`, so the mandate is a **per-station budget** and it gets smaller as the design gets denser —
the redundancy axis fights itself. Under a shared body the same secant is `series(Σ tᵢ, g)`: the
compliance lives in the **ground**, and the solved ground share runs **1.96 % → 99.90 %** of the
coupling's total compliance across the tie ladder. **The ties are then capped by `C-0049`'s
per-path force, not by the mandate** — and the ratio *grows* with the path count instead of
shrinking, which is consistent with the steeper redundancy slope Deliverable 3 measures and is
not offered as its proof.

### Bound 3 — `CH-0081`'s flexure floor, re-read with the mandate massed

`CH-0081`'s 22.414 nm is `(48 EI/k)^(1/3)` at `k = 33.3333/34`; reproduced here to **`8.6e−6`**
relative. A bending element's plan length goes as `n^(1/3)`, so concentrating the mandate on `n`
ground elements shortens it by `(34/n)^(1/3)`:

| ground elements | per element [pN/nm] | per element [pN] | two-support [nm] | one-support [nm] | inside 8.19 nm | inside 48 pN shear |
|---|---|---|---|---|---|---|
| 1 | 33.3333333 | 100.0 | 6.91878937 | **4.35856418** | both | **no** |
| 2 | 16.6666667 | 50.0 | 8.71712836 | **5.49144676** | one-support | **no** |
| **4** | 8.33333333 | **25.0** | 10.9828935 | **6.91878937** | one-support | **yes** |
| **6** | 5.55555556 | **16.6666667** | 12.5722746 | **7.92003673** | one-support | **yes** |
| 10 | 3.33333333 | 10.0 | 14.9060798 | 9.39024187 | neither | yes |
| 34 | **0.980392157** | 2.94117647 | **22.4141917** | 14.120056 | neither | yes |

That is `CH-0108`. It is **not** a reopening of the flexure branch — it is the observation that
the standing bound is quoted at a path count nobody names.

---

## Deliverable 1 — `F1`, the declared falsifier, at `C-0063`'s own 34 stations and zero defects

Equal ties, the ground placed on `C-0017`'s mandate at every rung:

| tie [pN/nm] | ground scale | ground's share of the compliance | **rigid body** | four-layer brick | single-layer sheet | peak tie force [pN] |
|---|---|---|---|---|---|---|
| 1.0 | 1700.0 | 1.96 % | 0.070212609 | 0.0699768208 | 0.0698761891 | 2.30 |
| 3.33333333 | 47.2222222 | 70.59 % | 0.0486364441 | 0.0930170828 | 0.0913337872 | 2.53–2.86 |
| 10.0 | 36.9565217 | 90.20 % | 0.0405588494 | 0.0992503563 | 0.114636199 | 2.82–3.24 |
| 33.3333333 | 34.3434343 | 97.06 % | 0.0371107839 | 0.100413982 | 0.12221779 | 3.06–3.50 |
| 100.0 | 33.6633663 | 99.02 % | 0.0354644034 | 0.100166871 | 0.124797325 | 3.17–3.61 |
| 333.333333 | 33.4316617 | 99.71 % | 0.0346649239 | 0.0998881029 | 0.125854026 | 3.21–3.65 |
| **1000.0** | **33.3660451** | **99.90 %** | **0.0344013403** | 0.0997786338 | 0.126179531 | 3.22–3.67 |
| — | — | — | *array: 0.0706145537* | | | 2.30 |

- **`F1` did NOT fire.** The rigid shared body is inside `T-5b`'s 0.10 at **every** rung, and at
  the stiffest ties it is **2.05×** flatter than the array on the identical stations.
- **And the body's own rigidity is not an idealisation to be assumed away.** The four-layer
  honeycomb brick — the stiffest body §3's own *"~10 nm"* thickness admits, `D_∥ = 14310.776 pN·nm`
  against the tile's 85.5018587 — reads **0.100166871** at 100 pN/nm ties, i.e. *worse than the array*, and
  a single-layer body reads 0.126179531. At 34 stations only the **rigid limit** wins.
- The 1.0 pN/nm rung is the degenerate one: `Σt = 34` barely exceeds the mandate, the ground has
  to be 1700× a unit element, and every body collapses onto the array's own number. It is the
  `F_b`-dominated corner and it is emitted to show the family is continuous.

## Deliverable 2 — the dropout, and the best cell this programme has reached

`C-0087`'s measured incorporation, seed 20260817, 10 000 realisations, 39 graded cells:

| station set | coupling | zero defects | worst one removal | median | **p90** | p95 | exceedance | peak tie force under dropout [pN] |
|---|---|---|---|---|---|---|---|---|
| 34 roots | array | 0.0706145537 | 0.501011167 | 0.448242691 | **0.639129638** | 0.687128267 | 99.81 % | 4.11 |
| 34 roots | rigid, 1000 | **0.0344013403** | **0.331249748** | 0.334286221 | **0.547996266** | 0.601805152 | 98.41 % | 11.64 |
| 3 × 15, 45 | array | 0.21821335 | 0.448598222 | 0.415440019 | **0.614243977** | 0.655728512 | 100.0 % | 3.38 |
| 3 × 15, 45 | rigid, 1000 | 0.0584137683 | 0.164563858 | 0.162201091 | **0.383198481** | 0.483433066 | 81.61 % | 12.44 |
| **6 × 15, 90** | array | 0.161116195 | 0.377754016 | 0.39663353 | **0.532748246** | 0.569031036 | 100.0 % | 1.67 |
| **6 × 15, 90** | **rigid, 1000** | **0.00664327028** | **0.139210902** | **0.148216097** | **0.24028028** | 0.286658739 | **76.60 %** | **9.01** |
| 6 × 15, 90 | brick, 1000 | 0.0762431778 | 0.196000174 | 0.203309562 | **0.288822628** | 0.32682798 | 81.63 % | 8.97 |

- **The best cell of 39 is 0.24028028**, against `C-0089`'s best of 22 at **0.284537599** —
  **1.18× better**, and it is reached with **uniform** ties and **no distribution search at all**,
  where `C-0089`'s best needed a 90-realisation percentile descent over 90 parameters.
- **The exceedance is the bigger movement**: 76.60 % ± 0.42 % against `C-0089`'s lowest anywhere
  of 97.16 %, and against 100 % for the array on the identical stations. Almost a quarter of built
  tiles would be flat.
- **The per-path force does not bind at the winner**: 9.01 pN under dropout, inside `C-0006`'s
  10 pN unzip allowable. It **does** bind at 34 roots and the stiffest ties (11.64 pN), where the
  bond must then be a shear geometry rather than an unzip one.
- **Still 2.40× the convention**, at the median as well as the 90th percentile (0.148216 against
  0.10), so the negative is not statistic-limited.

## Deliverable 3 — the count, measured on seven grids

Equal ties at 1000 pN/nm, rigid body, against the array at the same stations:

| ties | array p90 | array exceedance | **shared body p90** | shared body exceedance |
|---|---|---|---|---|
| 15 | 0.85219673 | 100.0 % | 0.820761389 | 100.0 % |
| 30 | 0.668998495 | 100.0 % | 0.526119351 | 100.0 % |
| 45 | 0.614243977 | 100.0 % | 0.383198481 | 81.61 % |
| 60 | 0.61234699 | 99.99 % | 0.399490333 | 77.79 % |
| 90 | 0.532748246 | 100.0 % | 0.24028028 | 76.60 % |
| 135 | 0.457967934 | 100.0 % | 0.144618226 | 22.12 % |
| 180 | 0.433524507 | 100.0 % | 0.12015241 | 23.05 % |

| | |
|---|---|
| **redundancy slope, array** | **−0.264642174** |
| **redundancy slope, shared body** | **−0.784357442** (2.96× steeper) |
| **ties the fit demands at `T-5b`'s 0.10** | **252.126899** — an extrapolation 1.40× beyond the densest grid measured |
| **upward crossover sites at phase 24** (`C-0066`) | **53** |
| **shortfall** | **4.76×**, against `C-0089`'s 5.7× for the array |

> **The topology changes the slope of the redundancy axis and not only its level, and the count it
> then demands is of the same order as `C-0089`'s. That is the answer: the escape is real, it is
> the largest single improvement anywhere in this branch, and it is not large enough.**

The curve is **not** monotone at one step (45 → 60 goes 0.3832 → 0.3995), which is reported rather
than smoothed: the two grids have different station geometries relative to the collar and the
90th percentile is a sample statistic with a `4e−3` settling (gate 4).

## Deliverable 4 — can it be built, and can it be placed?

| question | demanded | available | clears? | owner |
|---|---|---|---|---|
| vertical room above the tile at the 10 nm layer, at rest | 3.69 nm | 5.0 nm | **yes** | `C-0035`, `C-0061` |
| the same, at the stroke the device traverses | 6.69 nm | 5.0 nm | **NO** | `C-0066` |
| upward crossover sites for the ties, phase 24 | 34 | 53 | yes | `C-0066`, `C-0055` |
| one ground element's plan length, one-support | 4.36 nm | 8.19 nm | yes | `C-0069`/`CH-0081` |
| a published precedent for the motif | 1 | 1 | yes | `C-0055` (Ke et al. 2009) |
| **ties the density fit demands** | **252** | **53** | **NO** | `C-0066` + this study |
| **does §3 describe a two-layer tile?** | 1 | 0 | **NO** | `C-0053` |

**Three rows fail and only one of them is physics.**

1. **The height row fails only if the arm array is retained**, and it is not: a shared body
   *replaces* `C-0055`/`C-0063`'s 34 upward arms rather than standing above them, and the ties are
   the same upward crossover sites the arms were rooted on. The row is kept because a design that
   wanted both would face it.
2. **The count row is the verdict** and it is Deliverable 3.
3. **The specification row is the honest one.** A body tied to a single-layer sheet at many
   out-of-plane crossover sites **is square-lattice multilayer origami** — `C-0055`, verbatim:
   *"a crossover from a sheet duplex to a duplex added at an out-of-plane azimuth is the
   elementary step of square-lattice multilayer origami — but there the added duplex is tied at
   many sites and is rigid. The undemonstrated part is that it is free at one crossover."* So the
   motif this task priced is the one with a **precedent**, and the motif the programme has been
   building on (`C-0061`'s single-tie arm) is the one without. What §3 does not describe is a
   **two-layer tile** (`C-0053`: *"a two-layer body §3 does not describe"*), and that is a
   question for NDI, not a calculation.

`C-0041`'s severance obstruction — *"every duplex of the superstructure is cut into four pieces
by the 3 × 15 grid"* — **does not apply**: an inter-layer crossover is a tie, not a hole, and it
perforates nothing. That is why the `m × 15` station sets, which `C-0089` had to flag as abstract,
are less abstract here; the binding count is the upward-site inventory instead.

---

## The five verification gates

Executed as **21 gate-named tests** in `src/test/kotlin/coupling/SharedBodyCouplingTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a mode set carries one dimensionless shape per station per mode and refuses a non-positive footprint or a degree below one; the heave mode is unity everywhere and the tilts are the reduced coordinates; a distributed ground is `K/((2a+1)(2b+1))` and diagonal, i.e. `K` on heave and `K/3` on each tilt; a condensed compliance is symmetric and is exactly `1/K` at the footprint centre of a rigid body on a total ground `K`; the condensation refuses a mismatched tie set, presence vector or compliance; the mandate arithmetic is a stiffness over a stiffness | **PASS** |
| **2 — limiting cases** | an infinitely well grounded body converges monotonically to the **array**, `1e−6` at `10⁹×` the mandate; **`P1`** — a free rigid body adds exactly zero at 1, 2 and 3 ties (`1e−9` of the tie) and **annihilates every affine tile motion** at four; **`P1`** — the four-tie condensation reproduces `T − TA(AᵀTA)⁻¹AᵀT` assembled by hand on a 3 × 3 inverse that reuses no production code, `1e−7`; an absent tie leaves the matrix **and** the body's load path, and what survives is the three-station coupling rather than a submatrix; a shared body's coupling is never stiffer than its own ties | **PASS** |
| **3 — symmetry and conservation** | **`F3`** — a **free** shared body under a **uniform** load applies exactly zero tie force and leaves the dishing exactly zero (`1e−9`), beside the standing falsifier that the uncoupled tile does; the **two condensation routes agree** — `T − TΦ(ΦᵀTΦ+H)⁻¹ΦᵀT` against `(T⁻¹ + ΦH⁻¹Φᵀ)⁻¹`, two different inversions, `1e−8`; the modal stiffness and the condensed compliance are both reciprocal; **the mandate is placed on the body's ground** to `1e−9` and the ground then carries over 95 % of the coupling's compliance; a softer body is a larger compliance, never a smaller one | **PASS** |
| **4 — numerical convergence and statistical power** | the condensed compliance is **monotone non-decreasing** in the Ritz degree (a richer space can only soften a Rayleigh-Ritz body) with shrinking steps; the four-layer body is stiffer than the single-layer one at every non-rigid mode and the three rigid modes carry **identically zero** bending energy in both; in the study, the Ritz degree at **1/2/3/4/5** (`3.2e−11` from degree 3 on — the rigid/flexible gap is physical, not a truncation), the ensemble at **1250/2500/5000/10 000** (`3.8e−3`), and the dishing grid at **41/81/161** taken on the **mean** over 200 realisations because `C-0087` and `C-0089` both record that a percentile on three nested grids is degenerate (`2.1e−4`); a binomial standard error beside every exceedance | **PASS** |
| **5 — literature and upstream cross-check** | `C-0063`'s **0.0706145537** (`2.9e−10`), `C-0017`/`C-0058`'s **0.2182** (`6.1e−5`), `C-0058`'s **0.0753** (`5.7e−4`, its four-figure rounding), `C-0087`'s **0.5010** (`2.2e−5`) and **0.6391** (`4.6e−5`), `C-0026`'s **4.90731102 nm** (`7.7e−10`), and `CH-0081`'s **22.414 nm** (`8.6e−6`). **Worst departure over seven reproductions: `5.7e−4`.** The motif is checked against `C-0055`'s reading of Ke et al. (2009) rather than asserted | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **`F1`** | **the declared one** — a rigid shared body at `C-0063`'s 34 stations does not reach 0.10 at **zero** dropout | **NO** | 0.0344013403–0.070212609 over the tie ladder, flat at every rung, 2.05× the array |
| **`F2`** | the single-removal **amplification** is not materially below the array's | **YES** | 20.96 against the array's 2.06–7.10 — **and it fired because an amplification is a RATIO**: the absolute worst single removal is 0.139210902 against the array's 0.377754016, **2.71× better**. `CH-0109` |
| **`F3`** | a free shared body under a uniform load applies a non-zero tie force | **NO** | `< 1e−9` of the applied load, wired as a test |
| **`F4`** | the array corner fails to reproduce `C-0063`'s 0.0706145537 | **NO** | `2.9e−10` |
| **`F5`** | the shared body cannot be placed, and the count question is priced anyway | **YES, partly** | 3 of 7 buildability rows do not clear; the count question is priced regardless, as the acceptance requires |

**Four results that were not anticipated.**

1. **That the escape is a division and not a mechanics.** The task was framed as *"a missing tie is
   a stiffness perturbation rather than a removed path"*. It is not — a missing tie is still a
   missing tie, and the dishing between the survivors is still governed by the pitch. What the
   shared body actually does is move `C-0017`'s compliance out of the `n` local supports and into
   **one global rigid-body mode**, which dishing projects out by construction. The topology is
   worth 3.4× of *local support stiffness* at 34 paths and 9.0× at 90, and that is the whole
   effect.
2. **That `F2` fired and was informative rather than damaging.** The winning design is the most
   fragile in the corpus by amplification and the least fragile by the quantity the tolerance is
   written on.
3. **That a buildable body loses where the rigid limit wins.** At 34 stations the four-layer brick
   is *worse than the array*; it only overtakes at 90 stations. The body's own rigidity is not a
   second-order correction and the rigid limit is not a mild idealisation.
4. **That the specification, not the lattice, is the cleanest negative.** `C-0041`'s severance and
   `C-0069`'s plan budget both fall away under this topology; what does not is that §3 names a
   single-layer tile.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. The dropout input is measured; nothing derived here
  is.
- **The body is a Rayleigh-Ritz plate, and Ritz truncation OVERESTIMATES a body's stiffness** —
  i.e. it runs in the shared body's favour. The degree axis settles to `3.2e−11` from degree 3, so
  the truncation is not what separates the rigid limit from the brick; that gap is physical.
- **The body's ground is a DISTRIBUTED element.** A body grounded at one point has free tilts and
  is *softer*; a body grounded on a stiffer array is *stiffer* and approaches the array. Both are
  inside the family the condensation describes and neither is swept here.
- **The ties are UNIFORM.** No distribution search was run on the shared-body topology, so
  0.24028028 is an **upper bound** on what the topology reaches — `C-0089`'s distribution axis
  was worth 1.30–1.61× on the array. Queued as `T-165`.
- **`D_⊥` of a multilayer body is a LOWER bound** (`OrigamiSheet`: the across-helix axial
  stiffness of a crossover is not determined in this project), so the brick's rows are the
  pessimistic reading of that body.
- **The `m × 15` station sets above three columns are abstract as GEOMETRY** — they are `C-0015`'s
  equal-tributary grid, not a solved placement on the upward lattice — but they are **not**
  abstract as a *perforation*, because an inter-layer tie is not a hole. The 53-site inventory is
  the count that binds.
- **The 252-tie demand is a two-parameter log-log fit over seven grids**, extrapolated 1.40×
  beyond the densest one measured. Its slope, −0.784357442, is the measured quantity; the crossing
  is a reading of it.
- **No range reading.** Only the design state (`C-0022`'s 2 mM / 10 nm / 0.192 V) is emitted; at
  zero defects the range cost the array 12 % (`C-0068`), which cannot move a 2.4× exceedance.
- **`T-5b`'s 0.10 is a CONVENTION.** At a 25 % convention the winning design's *median* (0.148)
  would pass and its 90th percentile would not.
- The dishing pipeline, the lattice, the host, the load and the free-tile stroke are `C-0058`'s,
  `C-0063`'s and `C-0089`'s unchanged, and inherit `C-0022`'s unsourced rim charge and `C-0001`'s
  single foundation secant.
- **Single layer for the TILE, static, 300 K, aqueous 2 mM MgCl₂** — and a **second layer** for
  the coupling, which is the specification departure named above.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| staple incorporation map | 168 cells, 48–95 %, mean 84 % | **CITED, MEASURED**, Strauss et al. (2018) **through `C-0087`** |
| `C-0089`'s best fixed-distribution cell | 0.284537599 | **CITED** from `C-0089` |
| `C-0089`'s run-length path demand | 195 | **CITED** from `C-0089` |
| the upward (`EAST`) site inventory at phase 24 | 53 | **CITED** from `C-0066` |
| the arm slab's height, at rest and at stroke | 1.69–3.69 / 1.69–6.69 nm | **CITED** from `C-0061`/`C-0066` |
| §3's effort-point height budget | 5 nm at the 10 nm layer | **CITED** from `C-0035` (constant reading) |
| `C-0069`'s rooted plan budget | 8.19 nm | **CITED** from `C-0069` |
| `C-0025`/`C-0034`'s end-condition coefficients | 48 and 12 | **CITED** |
| the multilayer motif | Ke et al. (2009) | **CITED** from `C-0055`, which read it |
| `C-0063`'s 34-root placement | read at run time from `gpd/results/T-125-*.json` | **CITED** |
| `C-0022`'s solved collar | 2 mM / 10 nm / 0.192 V, read at run time | **CITED** |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| `T-5b`'s tolerance | 0.10 | **CITED CONVENTION** |

Everything else — every condensation, bound, ledger, graded cell, density row, convergence axis
and reproduction — is **derived here in code**.

## What this does to the standing claims

| claim | what moves |
|---|---|
| **`C-0089`** | **Its verdict stands and its GROUND narrows.** *"No Gen-1 coupling is flat under the measured staple dropout"* survives — 0.24028028 against 0.10 — and *"what refuses it is a COUNT"* survives as well, now measured on a second topology rather than derived on one. What moves is the **size** of the shortfall (5.7× → 4.8×), the **best number in the branch** (0.284537599 → 0.24028028), the **exceedance floor** (97.16 % → 76.60 %) and the reading of its Deliverable 2, which is `CH-0109`. Its open item 3 is **discharged**. |
| **`C-0087`** | **Nothing.** Its 0.5010, 0.6391 and its whole sampler reproduce. |
| **`C-0063`** | **Nothing at zero defects**; its 0.0706145537 reproduces to `2.9e−10`. What is added is that its own 34 stations carry a **2.05× flatter** coupling under a different topology, with no change of placement. |
| **`C-0069`/`CH-0081`** | **`CH-0108`.** Their *"refused at every span, every end joint and every placement — a bound on the family"* is a bound on `(family, 34 paths)`; at one to six ground elements the same family places inside the same budget. Neither the kinematic floor nor the force allowable is touched, and the branch is not reopened. |
| **`C-0061`/`C-0055`** | **A reversal of which motif is speculative.** `C-0061` prices a single-tie arm and flags it as undemonstrated; the many-tie rigid body it explicitly does **not** adopt (its 19.3× composite bracket) is the one with a published precedent. This claim does not re-price the 19.3×; it observes that the programme's precedent risk sits on the arm, not on the body. |
| **`C-0041`** | **Its severance obstruction does not transfer to this topology.** A perforating attachment grid cuts the superstructure into 18 pieces; an inter-layer crossover is a tie and cuts nothing. The obstruction is a property of the *flexure-and-standoff* array, not of a coupling at many stations. |
| **`ANSWERS.md` §1 / §4(g)** | §4(g) should now say that `C-0089`'s recovery route was searched **and a second topology was searched beside it**: the array's demand is 5.7× the buildable count, a shared-body coupling reaches **0.2403 of the stroke at the 90th percentile with 76.6 % exceedance** — the best in the programme — and still demands 4.8× the lattice's upward-site inventory. §1 should record that the flat tile is refused under measured folding **by two topologies**, and that the second one needs a two-layer object §3 does not describe. **A moved verdict is a challenge, not an overwrite**: the synthesis task owns the edit. |

## Still open — named, not answered

1. **A distribution search on the shared-body topology.** The ties here are uniform. `C-0089`'s
   distribution axis was worth 1.30–1.61× on the array, and 0.2403 × 0.62 = 0.149 would still miss
   the convention — but the margin is now inside one axis's reach. Queued as **`T-165`**.
2. **A placement search on the UPWARD lattice under this topology.** The 90-tie winner sits on
   `C-0015`'s abstract equal-tributary grid; the 53 real upward sites are irregular and `C-0074`
   showed irregular can beat regular at matched count. That is where the 4.8× would actually be
   tested.
3. **The body's ground topology.** Distributed here; one central element frees the tilts and is
   *better* for dishing but leaves the tile's tilt unconstrained, which is a gap-uniformity
   question nothing in this corpus asks.
4. **Whether §3 admits a two-layer tile.** A specification question for NDI, and the cleanest of
   the three failing buildability rows. Queued as **`T-166`**.
5. **The ties' own incorporation.** `C-0087`'s item 2 and `C-0089`'s item 1, unchanged: an
   inter-layer crossover is still a staple, and nobody has measured its incorporation.
6. **What fraction of built tiles a flatness verdict is owed over.** At 76.60 % exceedance this
   parameter now decides the verdict where at 97–100 % it did not.

## Challenges

**Raises [`CH-0108`](../challenges/CH-0108-the-flexure-bound-is-a-bound-on-a-path-count-not-on-a-family.md)
and [`CH-0109`](../challenges/CH-0109-a-cancellation-and-a-robust-design-are-different-designs-only-within-one-topology.md).**

**None stands against this claim.** The four ways it would fail:

1. **A measurement of an inter-layer crossover's incorporation materially different from a
   staple's.** The whole verdict is a transfer of a plain rectangle's map onto a motif nobody has
   mapped.
2. **A distribution or placement search on this topology reaching 0.10.** Open items 1 and 2; it
   needs a further 2.40×, and the two axes together have never been worth that in this corpus.
3. **A body model that is not Rayleigh-Ritz.** The rigid limit is exact and the flexible bodies
   are variational upper bounds on stiffness; a solved second grillage would place the brick
   somewhere between the brick row and the single-layer row, both of which are emitted.
4. **NDI ruling that a two-layer tile is out of scope**, which would remove the topology entirely
   and leave `C-0089` standing unqualified.
