# C-0098 — **The last axis is spent, and it runs the wrong way.** On the upward crossover sites the lattice actually supplies, a shared-body coupling whose **placement** and **distribution** are both searched reads **0.375506727** of the free-tile stroke at the 90th percentile under `C-0087`'s measured dropout — **3.76×** `T-5b`'s 0.10 and **1.56× WORSE** than `C-0093`'s 0.24028028, which lives on an abstract 90-station grid the lattice does not offer. The distribution axis is not merely small, it **closes as `1/t`**: a stiff shared body pins its stations onto a plane, and the spread over five very different tie shapes falls 0.555431809 → **0.00689107707** across the tie ladder. **`C-0089`'s 1.30–1.61× was a property of a DIVIDED mandate, and the same division that makes this topology flatter is what removes the axis**

| | |
|---|---|
| **Task** | [`T-165`](../tasks/T-165.md), raised by [`C-0093`](C-0093-shared-body-coupling.md) (`T-162`) *Still open* items 1 and 2, and by [`CH-0109`](../challenges/CH-0109-a-cancellation-and-a-robust-design-are-different-designs-only-within-one-topology.md) item 3 |
| **Leaf** | **`A8.2`** (the flatness of the tile), with **`A1.2`** for the anchoring scheme the coupling belongs to |
| **Verification type** | **logical** (a 32-phase lattice census, a redundancy division, a **kinematic-limit** closed form and a pitch division — four bounds, all before any sampler) **+ in-silico** (`C-0058`/`C-0063`/`C-0087`/`C-0089`/`C-0093`'s own exact Woodbury surrogate on `C-0009`'s grillage under `C-0022`'s **solved** load, with `C-0093`'s shared-body condensation and `C-0087`'s seeded Bernoulli dropout unchanged, 10 000 realisations per cell) |
| **Verdict** | **PASS on the predicate, and the answer is NO — the last unspent axis is spent and it does not close a 3.08× gap.** Over **25 graded cells** — 6 crossover phases at full upward inventory, both topologies, five placement-searched counts, two tie-cap families with a 53-parameter out-of-sample percentile descent in each, and two buildable one-parameter families — the lowest 90th-percentile dishing anywhere is **0.375506727** of the free-tile stroke, against `T-5b`'s **0.10**, with **100 %** exceedance. **The declared falsifier `F1` did not fire; nor did any of the five.** **The cheap division said so first**: `C-0093`'s own redundancy fit, refitted here from its density table to its published slope **−0.784357442** and crossing **252.126899** exactly, predicts **0.30833421** at the **60** ties the lattice's richest phase offers, so the two axes had to be worth **3.0833421×**. Measured, the **phase** is worth **1.95365697×** across the 32-phase screen and the **distribution** **1.29×** — and the 1.29× belongs to the *soft*-cap family, which is the worse design in absolute terms; at the stiff cap where the best design lives the same descent over the same 53 parameters buys **1.026×**. **The distribution axis closes as `1/t`, and that is a theorem before it is a measurement**: `K_c(sT)/s → T − TΦ(ΦᵀTΦ)⁻¹ΦᵀT`, a *kinematic* constraint independent of the tie distribution, so the spread of the 90th percentile over five very different tie shapes runs **0.555431809 → 0.348538831 → 0.063050964 → 0.00689107707 → 0.000905978148 → 9.72244009e-05** over ties of 3.33 → 100 000 pN/nm, in lock-step with the matrix departure from that limit (`2.07e−2 → 8.09e−7`). **Two lattice facts move the standing record.** `C-0093`'s 53-site ceiling is **phase 24's** inventory: the census over all 32 phases finds **60**, at ten of them, so the shortfall is 4.20× and not 4.76× ([`CH-0113`](../challenges/CH-0113-the-fifty-three-site-ceiling-is-one-phase-s-inventory-not-the-lattice-s.md)). And **the richest phase is not the flattest** — all ten richest phases are **seven**-column hosts, disjoint from `C-0015`'s eight-column ten and from `C-0063`'s two centro-symmetric ones, and graded at 10 000 realisations the 60-tie phase 17 reads **0.487309625** against phase 24's 53-tie **0.385192562**. **The measured redundancy slope on the real lattice is −0.376769756, 2.08× SHALLOWER than `C-0093`'s −0.784357442 on the abstract grids**, because the upward line's bare 32 bp pitch fixes the column count at **four** and a further tie is added inside those columns rather than as a new one — against the **13** columns `C-0089`'s run-length arithmetic demands. Raises [`CH-0113`](../challenges/CH-0113-the-fifty-three-site-ceiling-is-one-phase-s-inventory-not-the-lattice-s.md) and [`CH-0114`](../challenges/CH-0114-the-shared-body-s-tie-ceiling-is-stated-at-one-value-and-used-at-another.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED.** The dropout **input** is `C-0087`'s, which is Strauss et al. (2018) read directly on a plain Rothemund rectangle; the **motif** is `C-0093`'s shared body, whose precedent is `C-0055`'s reading of Ke et al. (2009) and whose specification gap (§3 names a single-layer tile) is unchanged. |
| **Provenance** | `gpd/results/T-165-shared-body-placement.json`, produced by `coupling.SharedBodyPlacementStudyKt`; model in `src/main/kotlin/coupling/SharedBodyPlacement.kt`, which **adds no method to any shared source** — it composes `C-0093`'s `SharedBodyCoupling`, `C-0058`'s `InfluenceSurrogate`, `C-0089`'s `DropoutRobustPlacement` and `C-0055`'s `anchoring.upwardRootLattice` as libraries; **7 cheap bounds, a 32-row census, 2 pitch ledgers, 2 redundancy fits, a 32-phase screen, 25 graded dropout cells at 10 000 seeded realisations each, a 6-rung tie ladder at 5 shapes and 2 000 realisations, 5 buildability rows, 3 convergence axes, 10 reproductions, 7 predicates, 5 falsifiers**; **19 gate-named tests in `src/test/kotlin/coupling/SharedBodyPlacementTest.kt`**; the result file was **produced three times on separate snapshots**. Runs **A** and **B** — code differing only in prose — agreed on **1 492 of 1 492** fields except six deliberately changed strings and **two reproduction `departure` values at the tenth digit** (`1.06411397e−9` against `1.06410993e−9`), which is `CLAUDE.md`'s JIT-summation effect on the one field class `C-0093` did **not** cure when it cured its convergence departures: a departure is a difference of two nearly equal numbers and it is **dimensionless**, so `RESULT_ABSOLUTE_FLOOR`, a statement in the locked units, cannot reach it (`P-18`). Run **C** rounds every reproduction departure to **two significant digits** and differs from B in **exactly those nine fields and nothing else**, prose included; it is the published file; `tools/verify.sh` **2 352 tests in 21 m 34 s** on its own isolated tree with one concurrent agent's mid-TDD test file dropped by `--drop-file` (`src/test/kotlin/anchoring/RowEndCrossoverStiffnessTest.kt`) — **2 failures, both `stability.DoublingLadderRepairTest` and both PRE-EXISTING on `HEAD`**, reproduced identically on a pristine `git archive HEAD` checkout containing nothing of this task's: that class reads only `gpd/results/T-149-recommended-element-fold.json`, which `T-167`/`CH-0112` record as not yet re-emitted. **Every test in `coupling/` passes.** The result-reader census and the Markdown-table gate run clean |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** sheet, 15 duplexes at the SAXS-measured **2.69 nm**; `C-0022`'s **solved** edge profile at **2 mM, 10 nm, 0.192 V** — `C-0063`'s, `C-0087`'s, `C-0089`'s and `C-0093`'s design state; `C-0017`'s **33.3333333 pN/nm** placed on the **body's ground** at §3's **acceptable 3 nm**; free-tile stroke **4.90731102 nm**; dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; the body is the **RIGID** limit at Ritz degree 1 on a **distributed** ground; grading **seed 20260817** at **10 000** realisations, training **seed 20260819** at **200**; decisions at 6 significant digits, emission at 9 |
| **Consumes** | [`C-0093`](C-0093-shared-body-coupling.md) (**the topology**, `SharedBodyModes`/`sharedBody`/`sharedBodyCouplingMatrix`/`placeSharedBodyGround`/`InfluenceSurrogate.solveWithSharedBody`, and its density table read at run time; its 0.0344013403 and 0.547996266 both **reproduced**), [`C-0089`](C-0089-dropout-robust-placement.md) (`DropoutEnsemble`, `summariseDropoutDishing`, `longestAbsenceRun`, `columnsForRunRobustness`, `spearmanRankCorrelation`, `inverseIncorporationWeights`), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (**the whole dropout model**; its 0.501011167 and 0.639129638 both reproduced), [`C-0055`](C-0055-unused-junction-site.md)/[`C-0063`](C-0063-upward-root-placement.md) (**the upward lattice itself**, `upwardRootLattice` and `centroSymmetricUpwardPhases`; its 0.0706145537 reproduced to `2.9e−10` and its 34-root placement read from `gpd/results/T-125-*.json`), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (the 53-site inventory at phase 24, **reproduced and then qualified**), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`, `rimStiffenedWeights`, `normalisedStiffnesses`), [`C-0047`](C-0047-single-column-flatness.md) (the 12.8290845 nm bending length), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (the per-path ceiling `a/s`, **and see `CH-0114`**), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0026`](C-0026-one-row-per-duplex.md) (the free-tile stroke), [`C-0015`](C-0015-crossover-phase-and-registration.md)/[`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile`, [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the 10 pN unzip allowable) |
| **Raises** | [`CH-0113`](../challenges/CH-0113-the-fifty-three-site-ceiling-is-one-phase-s-inventory-not-the-lattice-s.md) against `C-0093`'s 53-site ceiling, and [`CH-0114`](../challenges/CH-0114-the-shared-body-s-tie-ceiling-is-stated-at-one-value-and-used-at-another.md) against its Bound 2 tie cap |

---

## The claim, in one line

**A shared body is flatter than an array because it stops dividing `C-0017`'s mandate between its
stations — and a distribution is the redistribution of a divided budget, so the same sentence that
explains the gain explains why the last axis has nothing left in it.**

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
- A **tie** is an inter-layer crossover from a tile station to the shared **body**; the body runs
  to ground through its own element. **A tie is not an arm**: `C-0063`'s 34 is an *arm-footprint*
  cap and does not apply here, which is why the ceiling is the site inventory itself.
- **A dropout is a REMOVAL, not a perturbation** (`C-0087`), and it removes the tie; the body
  stays (`C-0093`).
- **The operating state is named**: `C-0022`'s **solved 2 mM / 10 nm / 0.192 V** profile. The
  **tenth** time this programme has had to say which state a flatness verdict is read at.
- **The verdict statistic is the 90th percentile**, a nearest-rank order statistic, and it is
  `C-0087`'s, `C-0089`'s and `C-0093`'s, so all four claims are comparable cell for cell.
- **Every optimised percentile is read OUT OF SAMPLE.** A descent sees 200 realisations at seed
  **20260819**; every quoted number is read on the independent 10 000-realisation ensemble at
  `C-0087`'s seed **20260817**.

---

## The four cheap bounds, which ran first

### Bound 1 — the census, a lattice count with no solve at all

`upwardTieCensus` runs `C-0055`'s own construction at every one of the 32 crossover phases:

| upward `EAST` sites | sheet crossover columns | centro-symmetric | phases |
|---|---|---|---|
| 52 | 7 | no | 3, 4, 5, 11, 12, 13 |
| 52 | **8** | no | 6, 7, 9, 10 |
| 52 | **8** | **yes** | 8 |
| 53 | 7 | no | 19, 20, 21, 27, 28, 29 |
| 53 | **8** | no | 22, 23, 25, 26 |
| **53** | **8** | **yes** | **24** |
| **60** | 7 | no | **0, 1, 2, 14, 15, 16, 17, 18, 30, 31** |

- **`C-0093`'s 53 is phase 24's inventory, not the lattice's.** The maximum is **60**, at ten
  phases, so its 4.76× shortfall is **4.20×**. That is `CH-0113`.
- **And the same 8 bp plane lattice carries both demands.** All ten richest phases are **seven**-
  column hosts: the ten richest, `C-0015`'s eight-column ten (6–10, 22–26) and `C-0063`'s two
  centro-symmetric ones (8, 24) are **disjoint / nested and never coincident**. The lattice offers
  the coupling its most stations exactly where it offers the **sheet** its fewest crossovers.
- **The site pitch is 10.88 nm and the columns per row are 4, at every one of the 32 phases.**

### Bound 2 — the redundancy division, which predicted the answer

`C-0093`'s density table is read at run time and refitted, reproducing its published slope and
crossing to `1.4e−10` and `6.8e−10`:

| | |
|---|---|
| slope, `d ln p90 / d ln n` | **−0.784357442** |
| crossing at `T-5b`'s 0.10 | **252.126899** ties |
| predicted at the lattice's **60** | **0.30833421** |
| **factor the two axes must buy** | **3.0833421×** |

Against that: `C-0089` measured a distribution at **1.30–1.61×** and `C-0074`'s
irregular-beats-regular at **1.13×**, whose product is **1.82×**. **The expected answer was a
negative and the search was confirmatory** — which is what a last axis is for.

### Bound 3 — the kinematic limit, and it is the bound that prices the distribution axis

For a rigid body, scaling every tie by `s`,

> `K_c(sT) = s [T − TΦ(ΦᵀTΦ)⁻¹ΦᵀT] + T Φ S⁻¹ H S⁻¹ Φᵀ T + O(1/s)`,  `S = ΦᵀTΦ`

whose leading term is the **free** body's own projector and diverges in every non-affine
direction. The stations are driven onto a **plane**, and that limit **does not depend on the tie
distribution at all**. Measured on the 53 real stations at phase 24:

| tie [pN/nm] | zero-defect dishing | spread over 5 shapes | **p90** | **spread over 5 shapes** | matrix departure from the limit | peak tie force [pN] |
|---|---|---|---|---|---|---|
| 3.33333333 | 0.0635501261 | 3.09929481 | 0.514231368 | **0.555431809** | 0.0207488847 | 5.2790868 |
| 10 | 0.0389629738 | 2.14495475 | 0.448802296 | **0.348538831** | 0.00765784887 | 6.54654645 |
| 100 | 0.0203739659 | 0.774478432 | 0.387112253 | **0.063050964** | 0.00080469456 | 8.71740881 |
| **1000** | **0.0173449294** | 0.108132139 | **0.37535305** | **0.00689107707** | 8.08807712e-05 | 9.34022417 |
| 10 000 | 0.0170060843 | 0.0111204027 | 0.374238768 | **0.000905978148** | 8.09221379e-06 | 9.41647456 |
| 100 000 | 0.0169717281 | 0.00111517949 | 0.374136624 | **9.72244009e-05** | 8.0926277e-07 | 9.42428359 |

**The spread and the matrix departure fall together, decade for decade.** At the design tie the
whole distribution family is worth **0.69 %** of the uniform reading, and the entire remaining tie
budget from there to the limit is `0.37535305 → 0.374136624`, **0.32 %**. Falsifier `F2` did not fire.

### Bound 4 — the lattice's own pitch against `C-0089`'s run-length demand

| | phase 24 | phase 0 |
|---|---|---|
| columns available per row | **4** | **4** |
| site pitch [nm] | 10.88 | 10.88 |
| pitch / `ℓ` (`ℓ` = 12.8290845 nm) | 0.848072986 | 0.848072986 |
| p90 longest absence run (`C-0089`) | 3 | 3 |
| surviving pitch [nm] | 43.52 | 43.52 |
| **columns demanded** | **13** | **13** |
| **shortfall** | **3.25×** | **3.25×** |

`13 × 10.88 nm = 141.44 nm` — the along-helix length the run-length demand implies, **3.54×** §3's
40 nm tile. *(A construction from two emitted fields, `columnsDemanded` and `sitePitch`.)* **The
demand is not merely unmet, it is below the lattice's own resolution**: the upward line's pitch is
the bare 32 bp and no phase changes it.

---

## Deliverable 1 — the placement, searched on the phase and on the subset

**The 32-phase screen at full inventory** runs on `C-0089`'s cheap objective (`n + 1` solves),
and **six phases** were then graded on the full 10 000-realisation ensemble:

| phase | ties | sheet columns | **shared body p90** | array p90 | zero-defect (shared) | peak tie force [pN] |
|---|---|---|---|---|---|---|
| **24** | **53** | **8** | **0.385192562** | 0.612366061 | **0.0173449294** | 10.5108848 |
| 23 | 53 | 8 | 0.391871561 | 0.612456935 | 0.0199386948 | 10.4831792 |
| 8 | 52 | 8 | 0.464062844 | 0.65233453 | 0.146706806 | 10.4569832 |
| 7 | 52 | 8 | 0.466888044 | 0.651648203 | 0.162955957 | 10.4645203 |
| 9 | 52 | 8 | 0.467523806 | 0.651676669 | 0.162955957 | 10.4450362 |
| **17** | **60** | **7** | **0.487309625** | 0.63217778 | 0.0483618504 | 10.4224809 |

- **The richest phase is not the flattest.** Phase 17 carries **seven more ties** than phase 24 and
  reads **1.27× worse**. The seven extra stations do not pay for the crossover column the host
  loses — which is `CH-0113`'s point restated as a measurement.
- **The placement axis is worth 1.95365697×** over the whole 32-phase screen on the cheap
  objective, and 1.26× between the best and the worst *graded* phase.
- **The shared body beats the array at every graded phase**, by 1.30–1.59×.

**And `C-0089`'s ranking instrument does not transfer to this axis.** Its worst-single-removal
bound ranks 22 *designs* against the percentile at ρ = 0.9729; over these six *phases* it ranks
them at **ρ = 0.468487481**, putting phase 8 first where the graded percentile puts phase 24
first. The subset search below runs on the cheap objective and **every number it reports is
re-graded on the full ensemble**, so the values are sound and only the search's optimality is a
statement about a descent.

**The subset search at fixed count**, on the 53 real sites at phase 24, uniform stiff ties:

| ties | **p90** | zero defects | peak tie force [pN] |
|---|---|---|---|
| 15 | 0.6505731 | 0.199013202 | 14.9020824 |
| 20 | 0.572454661 | 0.200312564 | 14.8480588 |
| 26 | 0.4937334 | 0.161273863 | 12.7913026 |
| 34 | **0.429016162** | 0.151765707 | 11.4042042 |
| 45 | 0.460342175 | 0.129425337 | 11.5896067 |
| **53** (all) | **0.385192562** | 0.0173449294 | 10.5108848 |

- At the **matched count of 34** the placement-searched subset reads **0.429016** against
  `C-0063`'s own 34 roots at **0.547996266** — the placement axis is worth **1.28×** at fixed
  count and fixed topology.
- The curve is **not monotone** at 34 → 45 (0.429016162 → 0.460342175), which is reported rather than
  smoothed: the subsets come from a first-improvement descent on an objective that ranks at
  ρ = 0.47, so a denser subset is not guaranteed to be a better-searched one.

## Deliverable 2 — the distribution, at both readings of the tie cap

53 real ties at phase 24; the descent is 53-parameter, trained on 200 realisations at seed
20260819 and graded on `C-0087`'s independent 10 000:

| tie cap | distribution | in-sample p90 | **out-of-sample p90** | zero defects | peak tie force [pN] | inside 10 pN unzip |
|---|---|---|---|---|---|---|
| `C-0049`'s `a/s` = 3.33333333 | uniform | 0.541463289 | **0.522220166** | 0.0635501261 | 5.7708004 | **yes** |
| `C-0049`'s `a/s` = 3.33333333 | 90th-percentile descent | 0.388544 | **0.404332526** | 0.154672306 | 6.66441253 | **yes** |
| the force-solved cap, 1000 | uniform | 0.415612704 | **0.385192562** | 0.0173449294 | 10.5108848 | no (48 pN shear: yes) |
| the force-solved cap, 1000 | **90th-percentile descent** | 0.376464 | **0.375506727** | 0.152034565 | 10.3254939 | no (48 pN shear: yes) |
| the force-solved cap, 1000 | `C-0058`'s rim × 10, out of sample | — | 0.385634291 | **0.0162023449** | 10.0884689 | no |
| the force-solved cap, 1000 | `C-0087`'s `1/p` compensation | — | 0.385129619 | 0.017334356 | 10.5059186 | no |

- **The distribution axis pays only where the topology is losing.** It is worth **1.29×** at the
  soft cap — whose absolute reading is the worse of the two — and **1.026×** at the stiff cap,
  where the best design lives. Bound 3 said exactly this before the descent ran.
- **The in-sample gain overstates the out-of-sample one** by 1.08× and 1.08×; there is shrinkage,
  and it is emitted rather than inferred (`inSampleP90OverStroke`).
- **The two buildable one-parameter families are worth nothing at all**: `C-0058`'s rim × 10 reads
  0.385634291 against uniform's 0.385192562, and `C-0087`'s `1/p` compensation 0.385129619. Both are inside
  the shape spread Bound 3 measures.
- **`CH-0114`**: the conservative reading of `C-0049` costs **1.36×** of flatness (0.522220166 against
  0.385192562) and it is not conservatism — `a/s` is derived on an array path, whose extension *is*
  the stroke, and a shared body's tie extension is a dishing-scale quantity. The **solved** force
  is what binds, and it is 10.3254939 pN at the winner: past `C-0006`'s 10 pN **unzip** allowable by
  5 %, well inside the 48 pN shear one, so the winning design requires a **shear** bond geometry.

## Deliverable 3 — the count, measured on the real lattice rather than extrapolated onto it

| | `C-0093`, abstract `m × 15` grids | **this study, the real upward lattice** |
|---|---|---|
| points | 7 | 6 |
| **slope `d ln p90 / d ln n`** | **−0.784357442** | **−0.376769756** |
| predicted at the lattice's 60 sites | 0.30833421 | **0.374687365** |
| factor demanded at 60 | 3.0833421× | **3.74687365×** |
| fitted crossing at 0.10 | 252.126899 | 1998.70962 — **not quotable** |

> **The real lattice's redundancy slope is 2.08× SHALLOWER than the abstract grid's**, because the
> upward line's bare 32 bp pitch fixes the column count at **four** and every further tie is added
> *inside* those columns rather than as a new one. A dropout is an increase in the attachment
> **pitch**, and a tie that shares a column with its neighbours does not shorten that pitch.

The fitted crossing is an extrapolation 33× beyond its data and is emitted, not quoted — exactly
as `C-0093` declines to quote the array's own. **The slope is the measured quantity**, and it says
the real lattice is on the wrong side of the count question by more than the abstract grid was.

## Deliverable 4 — buildability, on the real lattice

| question | demanded | available | clears? | owner |
|---|---|---|---|---|
| ties the real-lattice density fit demands | 1998.71 (not quotable) | 60 | **NO** | this study |
| attachment columns `C-0089`'s run-length demand asks of one row | **13** | **4** | **NO** | `C-0089`, `C-0055` |
| phases that are both richest and eight-column hosts | 1 | **0** | **NO** | `C-0015` |
| phases that are both richest and centro-symmetric | 1 | **0** | **NO** | `C-0063` |
| does §3 describe a two-layer tile? | 1 | **0** | **NO** | `C-0053`, `T-166` |

**Five of five do not clear, and only the second is physics.** The third and fourth are the
lattice congruence `CH-0113` records; the fifth is unchanged from `C-0093` and belongs to NDI.

**The `CH-0104` discipline, applied.** The oracle floor over each realisation's survivors reaches
**0.00313991949** at the p90 of the best full-inventory placement, against the **0.375506727** the
best fixed design achieves — a gap of **120×**. It **did not exclude**, and it licenses nothing:
the floor is attained by a force vector chosen with knowledge of the surviving support set.

---

## The five verification gates

Executed as **19 gate-named tests** in `src/test/kotlin/coupling/SharedBodyPlacementTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | an upward lattice is a count and refuses a zero edge, a one-duplex sheet and a negative phase; its stations lie inside the tile and ascend within a row; a redundancy fit refuses a single point, a zero tolerance, a non-positive count or percentile, and a sample with no spread in the count; a tie descent refuses an empty start, inverted bounds, a non-positive floor, a start outside its own bounds and a zero sweep count; a subset descent refuses an empty, a duplicated or a non-candidate choice; a row-wise run refuses a presence vector that does not split | **PASS** |
| **2 — limiting cases** | the census reproduces `C-0066`'s **53** sites at phase 24 with row lengths 4,3,4,3,… and the bare 32 bp pitch; the row-wise run **is** `C-0089`'s `longestAbsenceRun` on a uniform split and never crosses a row; a fit recovers an exact power law and inverts it to `1e−12`; a descent on a flat objective returns its own start; a descent finds a separable optimum and respects its bounds; a subset descent at full inventory has nothing to choose, reaches a target one swap away and — stated rather than discovered later — **cannot** cross a plateau to one two swaps away; **`F5`** — a body grounded at `10⁹×` the mandate **is** the array, `1e−6` | **PASS** |
| **3 — symmetry and conservation** | **`F2`** — the kinematic departure falls with the tie scale and the product `s × departure` **settles** to 5 %, i.e. the approach is first order in `1/s`, asserted on the **real** 53 stations; the census's centro-symmetric phases are exactly `C-0063`'s `centroSymmetricUpwardPhases`, `[8, 24]`, computed two independent ways; **`F3`** — a uniform load on a uniform Winkler foundation dishes exactly zero, uncoupled **and** under a free shared body, at `1e−9` on the tie forces and on the dishing | **PASS** |
| **4 — numerical convergence and statistical power** | neither descent returns a point worse than its start; in the study, the grading ensemble at **1250 / 2500 / 5000 / 10 000** (departure `1.8e−3`), the dishing grid at **41 / 81 / 161** on the **mean** over 200 realisations (`1.4e−4`, `C-0087`'s cure for the degenerate nested-grid percentile), and the descent's own **training** ensemble at **100 / 200 / 400** (0.395029049 / 0.379984226 / 0.379505417, departure `4.8e−4`) — the axis brackets the size the descent used; a binomial standard error beside every exceedance; **departures emitted at two significant digits and no step counter anywhere** | **PASS** |
| **5 — literature and upstream cross-check** | ten reproductions, **worst departure `1.1e−9`**: `C-0063`'s **0.0706145537**, `C-0093`'s **0.0344013403**, **0.547996266**, **−0.784357442** and **252.126899**, `C-0087`'s **0.501011167** and **0.639129638**, `C-0066`'s **53**, `C-0026`'s **4.90731102 nm** and `C-0049`'s **3.33333333 pN/nm** | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **`F1`** | **the declared one** — the best searched design, placement **and** distribution, reaches `T-5b`'s 0.10 at the 90th percentile | **NO** | the best of 25 graded cells is **0.375506727**, 3.76× the convention, at 100 % exceedance |
| **`F2`** | the stiff-tie kinematic bound is wrong — the shape spread at the design tie exceeds 5 % | **NO** | **0.00689107707**, and it falls decade for decade with the matrix departure |
| **`F3`** | a uniform load on a uniform foundation dishes non-zero | **NO** | `< 1e−9`, wired as a test, uncoupled and under a free body |
| **`F4`** | the pipeline fails to reproduce `C-0063`'s 0.0706145537 and `C-0093`'s 0.0344013403 | **NO** | `2.9e−10` and `1.1e−9` |
| **`F5`** | `F_b = 0` fails to converge to the array | **NO** | `1e−6` at `10⁹×` the mandate, wired as a test |

**Four results that were not anticipated.**

1. **That the best number in the branch gets WORSE when the placement is made real.** `C-0093`'s
   0.24028028 lives on `C-0015`'s abstract 6 × 15 grid of **90** stations; the lattice offers 60,
   and the best design it supports reads **0.375506727** — **1.56× worse**. Spending the last two
   axes *on the real lattice* loses ground rather than gaining it, and the honest reading is that
   the programme's best flatness number under measured folding was never buildable.
2. **That the distribution axis is a property of the mandate's DIVISION and not of the coupling.**
   It closes as `1/t`, in lock-step with the kinematic limit, and the topology that removes the
   division is the same one that removes the axis.
3. **That the richest phase is the worst host.** All ten 60-site phases are seven-column sheets,
   and the seven extra ties do not pay for the lost column.
4. **That `C-0089`'s ranking instrument does not transfer.** ρ = 0.9729 across designs,
   **0.468487481** across phases.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. The dropout input is measured; nothing derived here
  is.
- **The body is the RIGID limit**, which `C-0093` measures to be the optimistic end of its family
  (a four-layer honeycomb brick is 1.564× its condensed station compliance and reads *worse than
  the array* at 34 stations). No flexible body is swept here, so every shared-body number is an
  **upper bound on the topology's performance** and a lower bound on its dishing.
- **The body's ground is DISTRIBUTED**, as in `C-0093`; a body grounded at one point is softer and
  a body on a stiffer array approaches the array. Neither is swept.
- **The subset search is a first-improvement descent on an objective that ranks at ρ = 0.47**, so
  the fixed-count rows are **upper bounds** on what the real lattice reaches at those counts, and
  the 34 → 45 non-monotonicity is a property of the search rather than of the lattice.
- **The descents are truncated at two sweeps.** Reaching 0.10 from 0.375506727 needs a further
  73 %, so the truncation cannot reverse the verdict.
- **The real-lattice redundancy fit is over six placement-searched counts at ONE phase**, and its
  crossing is an extrapolation 33× beyond its data. The **slope** is the measured quantity.
- **The winning design's peak tie force under dropout is 10.3254939 pN**, 3 % past `C-0006`'s unzip
  allowable and well inside the 48 pN shear one — so it is conditional on a shear bond geometry,
  exactly as `C-0093`'s 34-root rows are.
- **No range reading.** Only the design state is emitted; at zero defects the range cost the array
  12 % (`C-0068`), which cannot move a 3.76× exceedance.
- **`T-5b`'s 0.10 is a CONVENTION.** At a 30 % convention the winner's *median* (0.265799969) would
  pass and its 90th percentile would not.
- **Single layer for the TILE**, static, 300 K, aqueous 2 mM MgCl₂ — and a **second layer** for the
  coupling, which is `C-0093`'s specification departure, unchanged.
- The dishing pipeline, the lattice, the host, the load and the free-tile stroke are `C-0058`'s,
  `C-0063`'s, `C-0089`'s and `C-0093`'s unchanged, and inherit `C-0022`'s unsourced rim charge and
  `C-0001`'s single foundation secant.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| staple incorporation map | 168 cells, 48–95 %, mean 84 % | **CITED, MEASURED**, Strauss et al. (2018) **through `C-0087`** |
| `C-0093`'s density table | 7 rows, read at run time from `gpd/results/T-162-*.json` | **CITED**, and **refitted** rather than transcribed |
| `C-0093`'s best abstract-grid cell | 0.24028028 | **CITED** from `C-0093` |
| `C-0089`'s distribution axis | 1.30–1.61× | **CITED** from `C-0089` |
| `C-0074`'s irregular-beats-regular | 1.13× | **CITED** from `C-0089`'s Deliverable 4 |
| `C-0063`'s 34-root placement | read at run time from `gpd/results/T-125-*.json` | **CITED** |
| `C-0022`'s solved collar | 2 mM / 10 nm / 0.192 V, read at run time | **CITED** |
| `C-0047`'s Winkler bending length | 12.8290845 nm | **CITED** |
| `C-0089`'s p90 longest absence run | 3 | **CITED** |
| per-path unzip / shear allowable | 10 / 48 pN | **CITED** via `C-0006`/`CH-0029` |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| `T-5b`'s tolerance | 0.10 | **CITED CONVENTION** |

Everything else — the census, every bound, ledger, screen, graded cell, tie rung, fit, convergence
axis and reproduction — is **derived here in code**.

## What this does to the standing claims

| claim | what moves |
|---|---|
| **`C-0093`** | **Its verdict stands and two of its numbers are qualified.** *"A coupling that is not an array does not escape the count argument"* survives and is now measured on the lattice rather than on an abstract grid. What moves: its **53-site ceiling** is phase 24's inventory and the lattice's is **60** (`CH-0113`, shortfall 4.76× → 4.20×); its **Bound 2 tie ceiling** of 3.33333333 pN/nm is stated and not used (`CH-0114`); and its headline **0.24028028** is now known to sit on a **90-station** set the upward lattice cannot supply — on the real 60-site lattice the best is **0.375506727**. Its open items 1 and 2 are **discharged**, and its open item 3 (the ground topology) is not. |
| **`C-0089`** | **Its verdict stands and its Deliverable 2 is scoped.** Its 1.30–1.61× distribution axis is confirmed to be a property of a **divided** mandate: on a shared body at the stiff cap the same 53-parameter descent buys **1.026×**. That completes `CH-0109` item 3 in `C-0089`'s favour on the array and against transferring the number. Its ranking instrument is confirmed at ρ = 0.9729 across *designs* and measured at **0.468487481** across *phases*. |
| **`C-0087`** | **Nothing.** Its 0.501011167 and 0.639129638 both reproduce to `3e−10`. |
| **`C-0063`** | **Nothing at zero defects**; its 0.0706145537 reproduces to `2.9e−10`. What is added is that its 34 stations are **34 of 53** at its own phase, that a placement-searched 34 on the same phase reads 0.429016162 against its 0.547996266 under this topology, and that its centro-symmetry congruence is reproduced independently. |
| **`C-0066`** | **`CH-0113`.** Its 53 is correct **at phase 24** and it says so; what is challenged is the inheritance of 53 as a lattice ceiling. |
| **`C-0049`** | **`CH-0114`.** Its `a/s` is a bound on a **force** and remains one; what is challenged is reading it as a bound on a **stiffness** under a topology whose path extension is not the stroke. |
| **`ANSWERS.md` §1** | Three edits are owed and the synthesis task owns them: (a) the shared body's best number **on stations the lattice supplies** is **0.375506727**, not 0.24028028, and the latter should carry *"on `C-0015`'s abstract 90-station grid"*; (b) *"the **53** the lattice offers"* becomes *"53 at phase 24, 60 at the lattice's richest phase — 4.20× short"*; (c) *"frees each tie from 0.98 to **3.33 pN/nm**"* becomes a **force** statement. **A moved verdict is a challenge, not an overwrite.** |

## Still open — named, not answered

1. **The body's ground topology.** `C-0093`'s open item 3, untouched: distributed here, and a
   single central ground frees the tilts and is *better* for dishing while leaving the tile's tilt
   unconstrained.
2. **A flexible body on the real lattice.** Every number here is the rigid limit. `C-0093` shows
   the brick loses at 34 stations and wins at 90; at 53 real stations it is unmeasured.
3. **Which phase a whole design should take.** The richest, the eight-column and the
   centro-symmetric sets do not coincide and no claim prices the three together. Queued as
   **`T-171`**.
4. **Whether a LARGER tile closes it.** `C-0089`'s open item 2, sharpened: the demand is **13
   columns at a 10.88 nm pitch, 141.44 nm** of along-helix length, 3.54× §3's tile.
5. **The ties' own incorporation.** `C-0087`'s item 2 and `C-0089`'s item 1, unchanged: an
   inter-layer crossover is still a staple and nobody has measured its incorporation. **It is now
   the only route left by which this programme keeps a flat tile.**
6. **Whether §3 admits a two-layer tile** — `T-166`, unchanged.

## Challenges

**Raises [`CH-0113`](../challenges/CH-0113-the-fifty-three-site-ceiling-is-one-phase-s-inventory-not-the-lattice-s.md)
and [`CH-0114`](../challenges/CH-0114-the-shared-body-s-tie-ceiling-is-stated-at-one-value-and-used-at-another.md).**

**None stands against this claim.** The four ways it would fail:

1. **A measurement of an inter-layer crossover's incorporation materially above a staple's.** The
   whole verdict is a transfer of a plain rectangle's map onto a motif nobody has mapped.
2. **A body model that is not the rigid limit and is nonetheless flatter.** The rigid limit is the
   best member of `C-0093`'s family, so this would need a body outside it.
3. **A placement search that is not a descent.** The subset rows run on an objective that ranks at
   ρ = 0.47; an exhaustive enumeration at a fixed count would tighten them. It would have to find
   3.76× at 53 ties, where the whole 32-phase spread is 1.95×.
4. **NDI ruling that a two-layer tile is out of scope**, which removes the topology entirely and
   leaves `C-0089` standing unqualified.
