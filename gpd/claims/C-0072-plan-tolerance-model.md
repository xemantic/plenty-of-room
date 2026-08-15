# C-0072 — **The two knife edges are ONE lattice quantity**, `p − d − L`, and it is not quotable: four floors — none of which needs a fabrication measurement — exceed it, the weakest by **1.56×** and the strongest by **70.6×**, and the sharpest of them is that **0.0256 nm is 0.075 of a base-pair rise**, so the margin is below the finest length any DNA design can specify. **And the measurement exists after all** — a fitted lattice-constant width of **9.1 %** for the single-layer sheet, in a supplementary table its own paper never quotes, **9.8×** the margin — while the 2.69 nm itself turns out to be a **Bragg lattice constant** over a measured **1.85 → 3.60 nm sawtooth** that brackets the verdict from comfortable to impossible. Correlation is worth exactly **7×**; the **twist propagates with coefficient exactly zero**; the design that recovers the margin — **30 paths, 53×** — loses `T-5b`'s flatness; and `T-45`, open since iteration 3, is **answerable from published measurement**: a **48–95 %, mean 84 %** staple incorporation is **43.6 %** per-path scatter, past `C-0060`'s 34.6 % threshold

| | |
|---|---|
| **Task** | [`T-134`](../tasks/T-134.md), raised jointly by [`C-0069`](C-0069-output-element-placement.md)'s *Still open* item 3 and [`C-0066`](C-0066-arm-slab-tie-clearance.md)'s *Still open* item 5, and standing behind [`T-45`](../../TASKS.md) since iteration 3 |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring scheme the array belongs to |
| **Verification type** | **logical** (exact linear propagation on a margin that is a difference of two integer base-pair counts, and four closed-form floors that need no distribution) **+ in-silico** (`C-0039`'s exact elastica, `C-0053`/`C-0069`'s packer and `C-0009`'s grillage under `C-0022`'s solved load re-run as libraries at reduced path counts; `C-0070`'s pinned register re-run over a 20-point seat sweep) |
| **Verdict** | **PASS — a tolerance model CAN be built, it settles both knife edges at once, and its answer is that neither margin is quotable.** **The two edges are one quantity**: `C-0069`'s arm margin groups it `(p − d) − L` and `C-0066`'s tie clearance groups it `(p − L) − d`, and the grouping is all that separates them — both reproduce here at **0.02560917 nm**, agreeing with each other to **`1e−12` nm** and with their own published 0.0256 to **`9.2e−6`**. **Four floors exceed it and the declared falsifier did not fire on any of them**: the **base-pair rise** (13.28×), the disagreement between the **two measured SAXS interhelical distances of the same material**, 2.73 − 2.69 nm (**1.56×**), the **thermal axial breathing** of the two segments the margin differences, from the measured stretch modulus (10.46×), and the **arm tip's own bending at a perfectly rigid root** (70.6×). The first of these is the strongest statement available: **0.0256 nm is 0.075 of a base-pair rise**, so the margin is not merely inside the scatter, it is below the quantum of the design language, and no correction can be applied to recover it even if the scatter were known. **Correlation is worth exactly 7× on the one channel that has a structure**: the rise enters twice, through the host's 32 bp crossover pitch and the element's own 24 bp length, so a **common-mode** strain carries the **difference** (8 bp, threshold **1.103 %**) and an **opposed** one the **sum** (56 bp, **0.158 %**), with the independent RMS between them at **0.221 %**; the sensitivity has an exact null at a 4:3 differential strain and **no build can reach it**, because an arm and its host are the same molecule in the same buffer. **The twist does not propagate at all** — a crossover interface spacing is an integer count and 10.5 and 10.67 bp/turn both round to 32, so its coefficient is **exactly zero**. **`T-45`'s stiffness scatter is a different axis**: a rise scatter enters the per-path stiffness with exponent exactly 3, so the amplitude that destroys the plan margin moves the stiffness by 3.3 % — **0.19×** `C-0026`'s break-even and **0.096×** `C-0060`'s flatness threshold — and the two cannot be traded; expressed as the Bernoulli **staple dropout** a builder can actually measure, those thresholds are **2.81 %** and **10.69 %**. **`C-0070`'s seat has no threshold**, because the verdict is **not monotone** in it: 4 of 11 coarse seats pass and the failures alternate, so the seat is a **register**, not a tolerance. **The design that recovers the plan margin is a reduced path count** — 30 paths dissolve `C-0063`'s four forced rows of three, take the length ceiling from 8.19 to 9.12 nm and the margin from 0.0256 to **1.3495 nm, 53×** — and it **costs `T-5b`'s flatness**, 0.0706 → 0.2603. **AND THE LITERATURE IS NOT A NEGATIVE.** 77 recorded queries found the measurement in supplementary material nobody quotes: Fischer et al. (2016) fit the **single-layer sheet's** lattice-constant width at **`w_a` = 2.5 Å on `a_mean` = 27.41 Å** — 9.1 % relative, **9.76×** the margin in absolute nm and **8.27×** this claim's loosest relative threshold — with a rigorously defined Lorentzian `B/q₀` = **15.2 %** beside it as an upper bound, and the multilayer brick **3.1× better ordered**. Bai et al. (2012) derive a **2–3 Å** atom-position rmsd at the core of a square-lattice origami from the cryo-EM B-factor. Dietz et al. (2009) supply the only measured **thermal/defect split** in the field: observed shape scatter **2.1–3.6×** the thermal prediction. **And the framing changes**: the interhelical distance is a *deterministic* sawtooth, **18.5 Å at a crossover to 36 Å midway**, so 2.69 nm is a **lattice constant** and not a local separation — read through the plan model its two ends give **+0.866 nm** and **−0.884 nm**, and its minimum is *inside* the 2.0 nm steric diameter this project asserts. **`T-45` is answered**: Strauss et al. (2018) map incorporation over all 168 staples of a Rothemund rectangle at **48–95 %, mean 84 %**, which as a per-path dropout is **43.6 %** relative stiffness scatter — **1.26×** `C-0060`'s flatness threshold, **2.57×** `C-0026`'s break-even, and a **16 %** shortfall on `C-0017`'s mandate. Raises [`CH-0084`](../challenges/CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** (`C-0055`, `C-0029`). The **inputs** to the tolerance model, unusually for this programme, now include four real measurements — and each is flagged *read directly / meaning inferred / derived* individually in `gpd/data/T-134-tolerance-literature.md`, with the one whose definition the source never gives (`w_a`) carried beside a rigorously defined companion. Every *"exceeds the margin"* is the **strong** direction: it makes a positive placement verdict less believable, never more. |
| **Provenance** | `gpd/results/T-134-plan-tolerance.json`, produced by `anchoring.PlanToleranceStudyKt`; model in `src/main/kotlin/anchoring/PlanTolerance.kt`; **5 identity records, 5 cheap bounds, 6 propagation channels, 8 thermal channels, 5 stiffness-scatter records, 10 path-count records, 5 flatness solves, 20 seat records over two grids, 4 joint-window records, 17 literature records each with its own read flag, 4 convergence records, 14 upstream reproductions, 6 predicates**; **31 gate-named tests in `src/test/kotlin/anchoring/PlanToleranceTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 13 m 17 s — the whole suite, on its own isolated tree, with NOTHING dropped**; the result file re-run through `tools/study.sh`, reported *"no result file changed"*, and diffed **byte-for-byte identical**; the literature survey, its **77 query strings**, its verbatim passages and its access notes in `gpd/data/T-134-tolerance-literature.md` |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, **32 bp** crossover interface spacing, crossover phase **24**; `C-0063`'s **34** upward roots read from `gpd/results/T-125-*.json`; `C-0022`'s solved edge profile at 2 mM, 10 nm, 0.192 V; `C-0017`'s **33.3333 pN/nm** as a **sum** at §3's **acceptable 3 nm**; `EI` = 230 pN·nm², `S` = 1100 pN, `k_θ` = 13.5294 pN·nm/rad, `C-0034`'s `A2` = 78.2353, `C-0028`'s `B2` = 261.168 |
| **Consumes** | [`C-0069`](C-0069-output-element-placement.md) (`rowOfThreeLengthCeiling`, `rootedLengthCeiling`, `armDirections`, `farRestraintCeiling`, `nearRestraintCeiling`, `StationRow` — **re-run as libraries**; its 0.0256 and 8.19 reproduced), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (its bound 4 and the 2.71561 nm tip gap, reproduced), [`C-0063`](C-0063-upward-root-placement.md) (**the placement itself**, read from `gpd/results/T-125-*.json`, and its 0.0706 re-solved), [`C-0070`](C-0070-pinned-leg-budget.md) (`PinnedBaseRegister`, `bestPinnedDesign` — **re-run** over a seat sweep), [`C-0062`](C-0062-crossbar-trio-existence.md) (its per-row floors, **consumed as data**), [`C-0060`](C-0060-buildable-stiffness-ratio.md) (the 34.6 % flatness threshold, **CITED**), [`C-0026`](C-0026-one-row-per-duplex.md) (the 17 % break-even and the correlation discipline, **CITED**), [`C-0039`](C-0039-two-spring-elastica.md) (`elasticaArmForStiffness`), [`C-0034`](C-0034-guided-arm-anchorage.md)/[`C-0028`](C-0028-standoff-base-joint.md) (the two joint couples), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0084`](../challenges/CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md), against `C-0060`'s tolerance verdict and through it `C-0058`'s flat distribution — the measured staple incorporation is **past** the threshold `C-0060` published for it to be graded against |

---

## The claim, in one line

**Two claims published the same number twice without noticing, and it is 0.075 of a base-pair rise — so the tolerance model that settles it is a list of floors rather than a distribution, every floor larger than the margin and none of them needing a measurement; and then the measurement turned up anyway, in a supplementary table its own paper never quotes, nine times wider than the margin and on the very object this programme builds on — while the constant the margin is a difference of turns out not to be a local distance at all but the mean of a measured 1.85–3.60 nm sawtooth whose two ends place the element comfortably and not at all.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**, energies **pN·nm**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the host sheet's helices, `y` **across** them, `z` normal and positive upward.
- **The plan margin is `M = p − d − L`** — root pitch minus one interhelical distance minus the element's own plan length. `M > 0` means the element fits under `C-0053`'s footprint convention, in which the next collinear element may start one full duplex past the previous one's tip.
- **A relative scatter `ε` on a quantity `q` is `δq/q`; a threshold `ε*` is the amplitude at which that channel's contribution equals the whole margin.** This is the project's *"the value the unknown would need for the answer to change"* form. It is the form the task planned to deliver in, on the expectation that no measurement existed; Deliverable 7 found four, and the thresholds are what they are graded against.
- **A built length is a whole number of base pairs; a solved length is a real number of nm.** They are different objects and are carried separately throughout — the difference is the whole of the `FIXED_ELEMENT` correlation row, and it moves the margin from 0.02561 to 0.03000 nm.
- **Correlation is named, never assumed.** `COMMON` = host and element see the same relative rise perturbation; `OPPOSED` = equal and opposite; `INDEPENDENT` = uncorrelated, quoted as an RMS; `FIXED_ELEMENT` = the element does not track the rise at all.
- **A "floor" here is a length below which no design or measurement can resolve the margin**, and it is required to rest on a *measured* constant. The one thermal channel that rests on a **construction** (`C-0009`'s in-plane crossover spring) is reported and explicitly **not** counted among the floors.

---

## Deliverable 1 — the two knife edges are one quantity, and neither claim noticed

| reading | grouping | published | through `M = p − d − L` | departure |
|---|---|---|---|---|
| **`C-0069`'s `Q5` arm margin** | `(p − d) − L` — a budget minus an arm | **0.0256** | **0.02560917** | `9.2e−6` |
| **`C-0066`'s bound 4 tie clearance** | `(p − L) − d` — a gap minus a duplex | **0.0256** | **0.02560917** | `9.2e−6` |
| `C-0069`'s plan budget | `p − d` | 8.19 | **8.19** | `0.0` |
| `C-0066`'s tip gap | `p − L` | 2.71561 | **2.71560917** | `8.3e−7` |
| the **built** arm, 24 bp | `p − d − 24r` | — | **0.03000** | +17.2 % |

**The two groupings agree to `1e−12` nm at every lattice constant tested** — four interhelical distances × two crossover spacings × three element lengths, asserted as a gate-3 test. `C-0066` and `C-0069` were written by different tasks against different questions, and both reported 0.0256 nm without either observing that it is the same subtraction.

> **Quantising the arm to a whole number of base pairs *opens* the margin by 17 %** — 24 bp is 8.16 nm against the elastica's 8.16439 — **and it changes the structure of the sensitivity**, because a built length tracks the rise and a solved one does not. Both readings are carried below.

---

## Deliverable 2 — the four floors, which ran first and settled the question without a distribution

| | floor | value [nm] | over the 0.02561 nm margin | fired? | rests on |
|---|---|---|---|---|---|
| **1** | **the DESIGN QUANTUM — the base-pair rise** | **0.34** | **13.28×** | **YES** | a count |
| **2** | **the disagreement between two MEASURED interhelical distances**, 2.73 − 2.69 | **0.04** | **1.56×** | **YES** | SAXS, Fischer et al. 2016 |
| **3** | the **thermal axial** fluctuation of the two segments the margin differences | **0.26779** | **10.46×** | **YES** | the measured stretch modulus |
| **4** | the arm tip's own **transverse** fluctuation at a **perfectly rigid** root | **1.80744** | **70.58×** | **YES** | the CanDo bending rigidity |
| 5 | the **twist** coefficient over the band this project disputes | **0.0** | **0** | **no** | an integer |

**Three readings, and the first is the one to quote.**

1. **Floor 1 is a statement about the design language, not about the physics.** DNA quantises every length at 0.34 nm. A margin of 0.0256 nm cannot be *drawn*, cannot be *corrected*, and cannot be *traded* — there is no shorter increment to trade with. This is the same class of argument as `C-0023`'s mounting-offset quantum, and here it runs 13.3× the wrong way.
2. **Floor 2 needs no model at all.** The two numbers are both SAXS measurements of an origami interhelical distance, and `C-0066` already found that the verdict flips between them. Stated as a tolerance: **the spread between two published readings of the constant the margin is a difference of is 1.56× the margin.**
3. **Floor 4 survives every joint improvement**, because it is the arm's own cantilever compliance `√(k_BT L³/3EI)`. Making the root perfectly rigid removes the hinge term and leaves 1.807 nm. So no joint stiffening escape exists, and that is asserted rather than argued.

> **Falsifier F1 — a channel landing INSIDE 0.0256 nm, which would have made the margin quotable and turned this task into a distribution fit — did not fire on any of the four.** It fired on exactly one *non*-floor channel, and that is reported in Deliverable 4.

---

## Deliverable 3 — the propagation, with the correlation structure named

**The margin is `M = N_p r_host − N_a r_arm − d` with `N_p = 32` and `N_a = 24`.** Every coefficient below is exact arithmetic on those two integers.

| channel | correlation | coefficient [nm per unit relative] | reading | threshold `ε*` |
|---|---|---|---|---|
| interhelical distance `d` | — | **2.69** | solved | **0.952 %** |
| the same | — | 2.69 | built | 1.115 % |
| base-pair rise `r` | **`FIXED_ELEMENT`** (what `C-0069`/`C-0066` assume) | **10.88** | solved | **0.235 %** |
| base-pair rise `r` | **`COMMON`** | **2.72** = `(32 − 24) r` | built | **1.103 %** |
| base-pair rise `r` | **`INDEPENDENT`** | 13.60 = `r√(32² + 24²)` | built | 0.221 % |
| base-pair rise `r` | **`OPPOSED`** | **19.04** = `(32 + 24) r` | built | **0.158 %** |

**Four things fall out, and three of them are structural.**

1. **The same amplitude is worth exactly 7× between the best and the worst correlation** — `(N_p + N_a)/(N_p − N_a) = 56/8`. `CLAUDE.md`'s *"which way a tolerance is correlated matters more than how big it is"*, in a new place and with the factor in closed form.
2. **The favourable structure is the physical one, and that is luck rather than design.** An arm and its host duplex are the same molecule in the same buffer, so a global strain arrives **common-mode**, at the 8 bp coefficient and not the 56 bp one. The design did not choose this and no claim upstream noticed it was available.
3. **The sensitivity has an exact null and no build can reach it.** `dM = 0` when `δr_arm/δr_host = N_p/N_a = 4/3` exactly — asserted to machine precision as a gate-3 test. It is the analogue of `C-0026`'s exact zero and it is the *unusable* kind: `C-0026`'s null is delivered by a symmetry, this one by a differential strain nothing supplies.
4. **`FIXED_ELEMENT` is the strictest reading and it is the one both claims implicitly took.** Treating the arm as 8.16439 nm rather than as 24 base pairs puts the whole 32 bp coefficient on the host and gives a **0.235 %** threshold — 4.7× tighter than the common-mode built reading.

**Under an independent-per-step model** the margin's variance is `(N_p + N_a) σ_step²`, so the per-base-pair rise standard deviation that puts the margin at exactly one σ is

&nbsp;&nbsp;&nbsp;&nbsp;`σ_step* = M/√56 = 0.004009 nm = 1.18 % of the rise.`

> The two counts enter as a **difference** in the correlated coefficient and as a **sum** in the independent variance. That pair is the whole correlation story, and it is why one number for "the scatter" cannot be quoted.

---

## Deliverable 4 — the thermal ledger, and the stiffness the margin demands

Equipartition throughout. **The stiffness a margin demands of any translational channel is `k ≥ k_BT/M² = 6315.6 pN/nm`; of any rotational one at the arm's lever, `k_θ ≥ k_BT L²/M² = 4.2098e5 pN·nm/rad`.**

| channel | σ [nm] | over the margin | stiffness supplied | shortfall |
|---|---|---|---|---|
| the host's 32 bp pitch, axially | 0.20240 | 7.90× | `S/p` = 101.10 pN/nm | **62.5×** |
| the arm, axially | 0.17533 | 6.85× | `S/L` = 134.73 pN/nm | **46.9×** |
| **the two in quadrature** | **0.26779** | **10.46×** | — | — |
| the interhelical distance at one crossover | 0.25301 | 9.88× | 64.71 pN/nm | **97.6×** |
| *the same at the TOP of `C-0009`'s own four-decade sweep* | *0.02236* | ***0.87×*** | *8282 pN/nm* | *0.76×* |
| the arm tip at **one crossover** root | **4.86555** | **190.0×** | 13.53 pN·nm/rad | **31 116×** |
| the arm tip at the **stiffest joint in the catalogue** (`C-0028`'s `B2`) | 2.07942 | 81.2× | 261.17 pN·nm/rad | **1 612×** |
| **the arm tip at a PERFECTLY RIGID root** | **1.80744** | **70.6×** | — | — |

**Two readings.**

1. **No joint in this programme's catalogue is within three orders of magnitude of what the margin asks.** The stiffest base is 1612× short, and going to a *perfect* root buys only 2.3× because the arm's own bending is the floor.
2. **Exactly one channel falls inside the margin, and it is the one that rests on a construction rather than a measurement.** `C-0009`'s in-plane crossover spring `k_s = 2αS/(100a)` is explicitly *"a construction, not a measurement"* and is swept over four decades; at the **top** of that sweep the interhelical fluctuation is 0.0224 nm, **0.87×** the margin. **That is why the four floors are built on measured constants instead** — a floor that a modelling choice can slide under is not a floor. It is reported because it is the one place where falsifier F1 nearly fired.

---

## Deliverable 5 — what does NOT propagate, and where the margin actually is

### The twist: coefficient exactly zero

A crossover interface spacing is an **integer** base-pair count — crossovers recur every 1.5 turns but alternate between a helix's two neighbours, so an interface is linked every three turns (`CLAUDE.md`). `round(3 × 10.5) = round(3 × 10.67) = 32`. **Over the entire band between the two readings this project disputes, the margin is constant to `1e−15` nm** — asserted at 101 sampled twists as a gate-3 test. The coefficient is a **step function**, not a slope: it moves only at 10.0 (30 bp) and 11.0 (33 bp), neither of which is a reading anyone has proposed for a square-lattice sheet.

> This is the second quantity in the programme found to have a structurally zero coefficient, after `C-0026`'s along-helix scatter — and it is zero for the same kind of reason, an integer rather than a symmetry.

### `T-45`'s stiffness scatter: a different axis, with 5–10× room against the RISE and none against the DROPOUT

A built element's length is `n·r` and a bending stiffness goes as `L^−3`, so **a relative rise scatter enters the per-path stiffness with exponent exactly 3.**

| driver | relative amplitude | → relative `k` scatter | vs `C-0026`'s 17 % | vs `C-0060`'s 34.6 % | equivalent staple dropout |
|---|---|---|---|---|---|
| a 1 % rise scatter | 1.00 % | 3.00 % | 0.176× | 0.087× | 0.09 % |
| **the amplitude that kills the PLAN margin** (common mode) | **1.103 %** | **3.31 %** | **0.195×** | **0.096×** | 0.11 % |
| ±1 base pair on every path, random sign | 4.16 % | 12.49 % | 0.735× | 0.361× | 1.54 % |
| **`C-0026`'s break-even** | — | **17.0 %** | 1.00× | 0.491× | **2.81 %** |
| **`C-0060`'s flatness threshold** | — | **34.6 %** | 2.035× | 1.00× | **10.69 %** |

**Three statements.**

1. **The knife edges and `T-45` cannot be traded against each other.** The rise amplitude that consumes the whole plan margin moves the per-path stiffness by 3.3 %, which is **5.1×** inside `C-0026`'s break-even and **10.4×** inside `C-0060`'s flatness threshold. Same physical quantity, two constraints, and the stiffness one has 5–10× the room the plan one has.
2. **A ±1 base-pair length error on every path is still inside both thresholds** — 12.5 %, 0.73× `C-0026`'s. And it is a failure mode a design does not have anyway: a staple length is *ordered*, not measured.
3. **`T-45`'s thresholds become build numbers under a dropout model.** An origami attachment is incorporated or it is not, so the honest population is two-valued and `σ_rel = √(f/(1−f))`. `C-0026`'s 17 % is a **2.81 %** dropout rate and `C-0060`'s 34.6 % a **10.69 %** one. **This is a translation of a threshold onto a build-controllable variable, not an equivalence** — a dropout has a different spatial pattern from an alternating scatter, and `C-0060` measured the pattern at 2.21×.

> **These two readings are not in tension and it is worth saying which is which.** The *rise* drives the stiffness with exponent 3 and has 5–10× room against both published thresholds; the *dropout* drives it as `√(f/(1−f))` and, at the incorporation Deliverable 7 finds measured, has **none**. Same axis, two mechanisms, and only one of them had ever been named.

### `C-0070`'s lateral seat: not a tolerance axis at all

| seat [nm] | 0.00 | 0.05 | 0.10 | 0.15 | 0.20 | 0.25 | 0.30 | 0.35 | 0.40 | 0.45 | 0.50 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| passes? | **yes** | no | no | **yes** | no | no | **yes** | no | **yes** | no | no |

**The verdict is not monotone in the seat, so no threshold exists.** Four of eleven coarse seats pass, and the failures come in two kinds — *"no closing pair centre"* and *"not representable at any leg length"* — alternating. Refining to a 0.025 nm grid over `[0, 0.2]` finds more alternation, not less, so this is a property of the register and not of the step size.

> **The seat is a REGISTRATION, and a scatter model on a registration is meaningless.** `C-0070` reported its 0.5 nm reading as *"the one axis on which this claim is fragile"*; the sweep shows it is one unlucky choice among several lucky ones, and that a design must **choose** a seat rather than tolerate one. `CLAUDE.md`'s *"sampling a continuous angle on a discrete lattice is not a sweep"*, arriving on a third quantity.

---

## Deliverable 6 — the design that has margin, and what it costs

`C-0017`'s mandate is a stiffness on a **sum**, so the path count sizes the element *and* counts the instances. Re-sizing both — which `C-0069`'s own sensitivity does **not** do, and that is recorded as open item 8 below:

| paths | arm [nm] | bp | length ceiling | **margin [nm]** | margin/rise | per-path force | clears the rise quantum? | clears the axial σ? | clears the tip σ? |
|---|---|---|---|---|---|---|---|---|---|
| **34** | **8.16439** | 24 | **8.19** | **0.0256** | 0.075 | 2.94 pN | **no** | **no** | **no** |
| 33 | 8.06840 | 24 | 8.19 | 0.1216 | 0.358 | 3.03 | no | no | no |
| 32 | 7.97080 | 23 | 8.19 | 0.2192 | 0.645 | 3.13 | no | no | no |
| 31 | 7.87152 | 23 | 8.19 | 0.3185 | 0.937 | 3.23 | no | **yes** | no |
| **30** | **7.77049** | 23 | **9.12** | **1.3495** | **3.97** | 3.33 | **yes** | **yes** | no |
| 28 | 7.56281 | 22 | 9.12 | 1.5572 | 4.58 | 3.57 | yes | yes | no |
| **25** | **7.23574** | 21 | 9.12 | **1.8843** | 5.54 | 4.00 | yes | yes | **yes** |
| 22 | 6.88711 | 20 | 9.12 | 2.2329 | 6.57 | 4.55 | yes | yes | yes |
| 15 | 5.96298 | 18 | **20.00** | **14.037** | 41.3 | 6.67 | yes | yes | yes |

**The ceiling itself moves at 30, and that is the whole finding.** `C-0063`'s bound 1 — `3a + 2(15 − a) = 34` — forces **four rows of three**, and a row of three is the only configuration in which two same-sense arms sit at the bare root pitch. Dissolve them and every row carries at most two; the ceiling is then set by the tile edge, at **9.12 nm**. **The margin goes 53× for 12 % of the path count.** Every count keeps the per-path force under the 10 pN unzip allowable.

### And it costs `T-5b`'s flatness

| coupling | stations | dishing / free stroke | flat at 0.10? | peak path force | peak crossover force |
|---|---|---|---|---|---|
| none — free tile on this phase | 0 | **0.30790** | no | — | 0.244 pN |
| **`C-0063`'s 34 roots — the knife-edge design** | 34 | **0.07061** | **YES** | 2.298 pN | 1.246 pN |
| **30 roots — the four rows of three dissolved** | 30 | **0.26028** | **no** | 3.056 pN | 1.713 pN |
| 22 roots | 22 | 0.47649 | no | 4.331 pN | 1.939 pN |
| 15 roots — one per duplex | 15 | 0.31179 | no | 5.740 pN | 2.593 pN |

> **The plan margin and the flatness are bought from the same four arms.** That is the real trade this task uncovers, and it is stated rather than resolved: **the reduction rule used here is a PLAN rule (drop the interior root of every row of three) and not a flatness optimisation**, so 0.2603 is an **upper** bound on what a re-optimised 30-root placement would dish. Re-running `C-0063`'s own search under a two-per-row constraint is the follow-on, and it is filed as `T-136`.

> **And `C-0069`'s own path-count sensitivity could not have found this**, because it re-sizes the element while holding the array at 34: its 15-path row places **34** instances of a 15-path arm, presenting **2.27×** `C-0017`'s mandate, and its note *"the placement is unchanged because the count is what sets the stations"* is what pins the ceiling at 8.19 nm in every row of its table. **No headline number in `C-0069` moves** — all are read at the self-consistent `n = 34` — and this is recorded as open item 8 and filed as `T-138` rather than challenged.

### A second escape, priced and not taken

The arm length is `(c EI/k)^(1/3)`, so a **softer** joint buys plan margin at 34 paths with no flatness cost at all. Bisecting `C-0039`'s exact elastica for the restraint that delivers a one-rise margin gives ceilings **below** what the design uses at both ends — so the escape asks for a joint *softer* than the two the design already chose (`C-0034`'s `A2` at the tip, one crossover at the root), and finding one is a joint search this task does not run. The joint-window table is in the result file.

---

## Deliverable 7 — the literature, which was expected to be a negative and is not

**77 query strings** across EuropePMC, arXiv and Crossref in nine named families, recorded in full in `gpd/data/T-134-tolerance-literature.md` so the negatives are falsifiable by one paper. Every value below carries a **read flag**. **Nothing is taken from a search summary.**

### The measurement exists, and it is in a supplementary table its own paper never quotes

**Fischer, Hartl, Frank, Rädler, Liedl, Nickel, *Nano Lett.* **16**:4282 (2016)** — the paper this programme's 2.69 / 2.73 / 2.54 nm come from — **measures the width as well as the mean**. Two independent width parameters, both **READ DIRECTLY** from the SI:

| object | Lorentzian `B/q₀` (SI Table S6) | fitted `w_a/a_mean` (SI Tables S5/S7) |
|---|---|---|
| **sheet — SINGLE-LAYER, this programme's own object** | **15.2 %** | **9.1 %** (2.5 Å on 27.41 Å) |
| brick — square lattice, multilayer | 4.4 % | 2.9 % |
| 24HB — honeycomb | 11.8 % | 6.8 % |

| against | value | ratio |
|---|---|---|
| the margin, in absolute nm | 0.25 nm vs 0.02561 nm | **9.76×** |
| this claim's **loosest** relative threshold (common mode, built arm) | 9.1 % vs 1.103 % | **8.27×** |
| this claim's **tightest** relative threshold (opposed) | 9.1 % vs 0.158 % | **57.9×** |
| **floor 2** — the difference between the two published *means* | 0.25 nm vs 0.04 nm | **6.25×** |

**Four qualifications, stated before they are asked for.**

1. **`w_a` is never defined in words.** It appears only in two table headers. It is almost certainly a Gaussian width on the lattice constant in a rigid-cylinder-on-a-lattice fit, but the paper does not say so — flagged **READ DIRECTLY (meaning INFERRED)**, and carried beside `B/q₀`, whose definition *is* verbatim (*"B is the HWHM"*), as the rigorously defined companion.
2. **`B/q₀` is an upper bound**, because a Bragg peak width contains finite-size broadening as well as disorder, and **no paracrystalline decomposition exists** — eight queries returned literally zero hits for *"paracrystalline"* and *"Debye-Waller"* alongside *"DNA origami"*.
3. **A consistency check nothing forces.** The ratio `w_a`/HWHM is 0.61, 0.67, 0.57 across three objects and two lattices — two entirely different width parameters tracking each other, which is what one underlying lattice disorder would produce.
4. **The main text quotes only the peak *position* with its fit uncertainty** (±0.2 Å on the sheet). That is the standard error on the **mean** and is ~20× smaller than the width; it is not the same quantity, and reading it as a tolerance would have been the mistake this claim exists to avoid.

> **The single-layer sheet is the worst-ordered origami anyone has measured** — 3.1× the multilayer brick's relative width, on the same beamline and the same fit model. A Scherrer estimate (**DERIVED**) puts the sheet at ~3.3 coherent lattice planes out of 24 designed helices, i.e. **disorder-limited**, against the brick's roughly size-limited ~11.4 of 14. Corroborated from an independent direction: **Kube et al., *Nat. Commun.* **11**:6229 (2020)** could not solve a single-layer Rothemund rectangle at all — verbatim, *"were unsuccessful due to excessive conformational heterogeneity"*.

### The framing changes: 2.69 nm is a Bragg lattice constant, not a local distance

**Bai, Martin, Scheres, Dietz, *PNAS* **109**:20012 (2012), READ DIRECTLY**, main text Fig. 3 caption, verbatim:

> *"The midpoints of neighboring dsDNA helices move on average from a minimum distance ⟨d min⟩ = 18.5 Å at the cross-over to a maximum distance of ⟨d max⟩ = 36 Å away from each other."*

Confirmed by all-atom MD (**Yoo & Aksimentiev, *PNAS* **110**:20099, 2013**: *"the DNA–DNA distance was found to range between 18 and 30 Å"*, rmsf *"less than 4 Å"*) and by oxDNA on a **2D tile** (**Snodin et al., *NAR* **47**:1585, 2019**: the weave's standard deviation *"significantly smaller in magnitude than the variation in the interhelical distance due to the weave pattern itself"*).

| read through the plan model | `d` used | margin `p − d − L` | verdict |
|---|---|---|---|
| the weave **minimum**, at a crossover | 1.85 nm | **+0.866 nm** | places comfortably — 34× the nominal margin |
| the **lattice constant** this programme uses | 2.69 nm | **+0.0256 nm** | the knife edge |
| the weave **maximum**, midway between crossovers | 3.60 nm | **−0.884 nm** | **does not place at all** |

**Two consequences.**

1. **The measured weave brackets the verdict from comfortable to impossible, and the plan model samples neither end.** `C-0041`'s and `C-0053`'s *"a duplex in plan is a rectangle of width `d`"* uses a lattice **mean** as a **steric** width; that is a convention, it is inherited by five claims, and the measurement says the underlying quantity varies by 1.75 nm along the helix in a pattern set by the crossover positions — which are exactly what a layout chooses.
2. **The weave minimum is *inside* the 2.0 nm steric diameter this project asserts.** `CLAUDE.md`'s *"the phosphate radius in B-DNA is 10 Å, which IS the duplex's steric radius"* and a measured 18.5 Å centre-to-centre approach cannot both be hard-body statements. The resolution is presumably that a crossover-linked pair interdigitates its grooves, but nothing in this programme has priced it.

### The only measured thermal-versus-fabrication split in the field

**Dietz, Douglas, Shih, *Science* **325**:725 (2009), READ DIRECTLY**, main text, verbatim:

> *"The distributions each have a half-width at half maximum of 5° to 9°. **Our toy model predicts thermally induced angular fluctuations with a standard deviation from the mean bend angle of about 2.5°** … **The discrepancy between expected and observed distribution widths may be due to defects.** … A future challenge will be to improve folding quality such that thermal fluctuations alone determine the angular precision of any produced shape."*

**DERIVED**: observed/thermal = **2.1× to 3.6× in amplitude** over N = 74–212 particles per point. Applied to this claim's thermal floors, that is the multiplier from a *lower bound* to an *as-built estimate* — floor 3 becomes 0.56–0.96 nm, and floor 4 becomes 3.8–6.5 nm.

> This is the number a tolerance model in this field is entitled to use, and it is one paper. **Nobody has repeated it in seventeen years.**

### `T-45`, answered from published measurement — and the answer is a failure

**Strauss, Schueder, Haas, Nickels, Jungmann, *Nat. Commun.* **9**:1600 (2018), READ DIRECTLY** — a per-site incorporation map over **all 168 staples** of a Rothemund rectangle by DNA-PAINT, corroborated by next-generation sequencing. Verbatim: *"**absolute incorporation efficiencies of 48–95 % with an average of 84 %**"*, and *"strand incorporation strongly correlates with the position in the structure."*

A missing staple does not perturb a load path's stiffness — it **removes** it, so `σ_rel = √(f/(1−f))`:

| reading | efficiency | dropout | `σ_rel` | vs `C-0026`'s 17 % | vs `C-0060`'s 34.6 % |
|---|---|---|---|---|---|
| **Strauss mean** | **0.84** | 0.16 | **43.6 %** | **2.57×** | **1.26×** |
| Rothemund's own pixel yield (1 080 observed) | 0.94 | 0.06 | 25.3 % | 1.49× | 0.73× |
| **Strauss, EDGE sites** | **0.48** | 0.52 | **104.1 %** | 6.12× | **3.01×** |
| Strauss, centre sites | 0.95 | 0.05 | 22.9 % | 1.35× | 0.66× |
| the efficiency `C-0060`'s threshold implies | **0.893** | 0.107 | 34.6 % | 2.04× | 1.00× |

**And the mean alone, with no pattern assumption, is a 16 % shortfall on `C-0017`'s mandate** — 33.3333 → 28.00 pN/nm, **2.9×** the worst rounding error `C-0060` calls *"a placement error, not a rounding nuisance"*, and a trim cannot recover it because the missing paths are not there to trim. **This is [`CH-0084`](../challenges/CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md)**, and the position dependence runs the wrong way for `C-0058`, which puts 34 of its 45 stations on the rim.

### What was NOT found, with the queries recorded

| quantity | outcome |
|---|---|
| a **paracrystalline** or **Debye-Waller** disorder analysis of an origami lattice | **NOT FOUND** — eight queries, five returning literally zero |
| an origami-specific per-step **rise** or **twist** scatter | **NOT FOUND**; generic B-DNA supplied instead (Olson et al. 1998: rise 3.32 (0.19) Å over 724 steps, an **upper** bound as it includes sequence and packing) |
| the **stiffness scatter of nominally identical staple attachments** | **NOT FOUND** — nine queries across three databases. The nearest published work (Videbæk et al., *PNAS* 2025) measures an inter-subunit ssDNA handle, not an intra-tile attachment — but it proves the instrument and method exist |
| the term *"fabrication tolerance"* applied to origami at all | **NOT FOUND** |

---

## The five verification gates

Executed as **28 gate-named tests** in `src/test/kotlin/anchoring/PlanToleranceTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a margin is a length and scales with every length; a relative threshold is **dimensionless** and invariant under rescaling; an axial fluctuation is a square root of length over a modulus; a cantilever tip fluctuation goes as the **three-halves** power of the arm; the stiffness a margin demands is an **inverse square** of it and the rotational one carries the lever squared; unphysical arguments throw at **eighteen** entry points, including a zero coefficient, a zero base-pair count, a negative scatter, a dropout rate of 1, and a reduction that would empty a row | **PASS** |
| **2 — limiting cases** | **THE FREE LIMITING CASE — zero scatter reproduces BOTH published clearances, and reproduces them as the SAME number**: `C-0069`'s 8.19 nm budget, its 0.0256 nm margin, `C-0066`'s 2.71561 nm tip gap, and the identity `(p − d) − L ≡ (p − L) − d`; zero amplitude moves no channel; an infinitely stiff channel has zero excursion; the dropout and scatter maps are exact inverses over five amplitudes; a built length is the rise times a whole count and quantising **opens** the margin; dropping zero roots returns the rows unchanged | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the row-of-three ceiling is resolution independent over `1e−6 → 1e−12` (departure `0.0`), and a bisection on the same condition lands on the closed-form threshold to `1e−9`; the 34-path elastica arm is RK4-step independent over 200 → 800; the 30-root dishing over the sample grid 41/81/161; the per-step threshold is reproduced by an explicit variance sum; **the seat sweep is reported as NOT converging and that is the finding** — refining 0.05 → 0.025 nm finds more alternation, because the verdict is not monotone; **the result file re-run through `tools/study.sh` reported *"no result file changed"* and diffed byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **14 upstream reproductions**; the two SAXS interhelical distances carried as measured constants and the square-lattice one asserted to exceed the margin; **the MEASURED sheet lattice width asserted to exceed every threshold this model computes** and its mean asserted equal to the lattice constant this project already uses, to 2 %; **the measured weave asserted to bracket the placement verdict from both sides**; **the measured staple incorporation asserted past `C-0060`'s threshold**, with the edge sites worse and the incorporation `C-0060`'s threshold implies computed; 77 query strings recorded in `gpd/data/T-134-tolerance-literature.md` | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The two knife edges are ONE quantity at every lattice constant**, not only at the Gen-1 one: four interhelical distances × two crossover spacings × three element lengths, asserted at `1e−12` nm and measuring `4.4e−16`.
2. **The rise sensitivity's null direction is exact**, and it is the ratio of the two counts: a perturbation at 4:3 leaves the margin stationary to `1e−14`.
3. **A common-mode perturbation on EQUAL counts moves the margin exactly zero** — the coefficient is a difference, so `N_p = N_a` annihilates it. The falsifier for the whole propagation model.
4. **The channels superpose**: perturbing the rise and the width together equals the sum of the two linear terms to `1e−14`, which the construction never imposes.
5. **The station reduction is monotone and nested** — the roots surviving `drop + 1` are a subset of those surviving `drop`, at every drop from 0 to 10, so the path-count table is a nested family and not eleven unrelated layouts. (`CLAUDE.md`'s nested-refinement discipline, on a lattice rather than a mesh.)

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | a channel whose own floor lands **inside** 0.0256 nm | **no on all four floors; YES on one non-floor** | the interhelical fluctuation at the **top** of `C-0009`'s constructed four-decade sweep is 0.87× the margin — which is exactly why the floors were required to rest on measured constants |
| **F2** | the two knife edges being **different** quantities | **no** | identical to `1e−12` nm at every constant tested |
| **F3** | the **twist** entering with a non-zero coefficient | **no** | exactly zero over the disputed band; a step function elsewhere |
| **F4** | no path count leaving a margin above the design quantum | **no** | 30 paths leave 1.3495 nm — but the design that does it is **not flat**, which F4 did not anticipate |
| **F5** | a measured origami lattice-scatter distribution existing and being **narrower** than every floor | **the distribution EXISTS and is far WIDER** | Fischer et al.'s single-layer sheet width is **9.76×** the margin and **6.25×** floor 2 — F5 fired in the direction that reinforces the verdict rather than the one that would have overturned it |

**A result that was not anticipated:** **the design that recovers the margin loses the flatness.** The task was formulated expecting the escape to be a cost in path count and per-path force, both of which are cheap; the binding price turns out to be `T-5b`'s flatness verdict, and the four arms that buy the knife edge are the four the flatness needs.

**A second one:** **`C-0070`'s seat is not a tolerance axis at all.** The sweep was run expecting a threshold and returned an alternating pattern — so the honest answer on that axis is not a number but a change of category.

**A third, and it is the one that changes most:** **the literature search was formulated as a negative-existence exercise and returned four measurements.** The task file's own plan says *"if it has not [been measured], record the query strings so the negative is falsifiable"*. Three of the four are in **supplementary material their own main texts never discuss**, and the fourth — Strauss's incorporation map — answers a task (`T-45`) that has been recorded as *"nothing accessible gives"* since iteration 3. **`CLAUDE.md` already warns that a closed-access verdict is not an unobtainability verdict; this adds that a quantity absent from a main text is not a quantity that was not measured.**

---

## Validity range

- **TRL 1–3. Nothing DERIVED here is measured, and the motif is not demonstrated.** `C-0055`'s and `C-0029`'s findings are unchanged and upstream of every number. The **inputs** now include four real measurements, each individually read-flagged.
- **The measured widths are read flags, not endorsements.** Fischer's `w_a` is **read directly with its meaning inferred** — the SI never defines it in words — which is why `B/q₀` is carried beside it; and `B/q₀` is an **upper** bound because a Bragg peak width contains finite-size broadening. **Neither is decomposed**, and no paracrystalline analysis of an origami lattice exists.
- **Strauss's incorporation map is a plain Rothemund rectangle at one folding protocol**, and a staple is not a coupling load path. It is quoted as the right **order** and the only measured one, not as the coupling's own number; the dropout translation carries `C-0060`'s own 2.21× pattern caveat with it.
- **Bai's weave is measured on a MULTILAYER square-lattice object**; its transfer to a single-layer sheet rests on Snodin et al.'s oxDNA simulation of a 2D tile and Yoo et al.'s all-atom MD, both of which reproduce it. The plan model has not been re-run against it — that is `T-137`.
- **The floors are floors of *resolution*, not of *failure*.** A thermal excursion 70× the margin does not mean the array cannot be assembled; it means a hard-body clearance computed at nominal positions is **not the instrument** that decides whether the assembled array is comfortable. What it does mean is that the *nominal* verdict cannot be quoted to 0.03 nm — which is what both upstream claims do.
- **The propagation is linear and first order.** The margin is exactly linear in `d` and in both rises, so there is no truncation error; but the *placement verdict* is not linear in the margin (it is a count), and this claim does not re-run the packer at perturbed constants — `C-0069` already did that at 2.73 nm and found 18 of 34.
- **The correlation structures are the four named ones**, and **no measurement selects among them** — a lattice-constant width is not a statement about whether a host duplex and an arm are correlated, so Fischer's number bounds the amplitude and leaves the 7× spread open. Cryo-EM multi-body refinement would settle it.
- **The reduced-path-count design is a PLAN result and not a placement.** The station reduction drops the interior root of every row of three; it is deterministic and nested, but it is not `C-0063`'s flatness search re-run under a new constraint. Its dishing is therefore an **upper** bound and its margin a property of that particular subset.
- **The joint-window ceilings are bisected on `C-0039`'s exact elastica** and inherit its whole validity range, including the `1.5 ×` stroke floor.
- **`C-0070`'s seat sweep is at its own reference row (9 bp) and its own floors**, read from `C-0062`'s result file. Other rows are not swept.
- **The dropout translation is a translation, not an equivalence.** It maps a relative standard deviation onto a two-valued population; it does not reproduce the spatial pattern `C-0060` shows is worth 2.21×.
- **§3's desired 10 nm stroke is out of reach of this element regardless** (`C-0050`, `C-0066`), and nothing here changes that.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| interhelical distance | 2.69 nm single-layer, **2.73 nm square** | **CITED, MEASURED** (SAXS, Fischer et al., *Nano Lett.* **16**:4282, 2016). **Floor 2 is the difference between them** |
| rise per base pair, crossover interface spacing | 0.34 nm, 32 bp | **CITED** (Rothemund 2006, Ke et al. 2009, via `C-0015`/`C-0055`) |
| base pairs per turn | 10.67 square, 10.5 honeycomb | **CITED**; used only to show the coefficient is zero |
| duplex `EI`, `S` | 230 pN·nm², 1100 pN | **CITED, CanDo MODEL INPUT** (Kim et al. 2012) / **MEASURED** (Wang et al. 1997) |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad | **CITED, FITTED** (Chen et al., *JACS* **136**:6995, 2014) via `C-0009` |
| crossover in-plane `k_s` | 64.71 pN/nm | **`C-0009`, A CONSTRUCTION AND NOT A MEASUREMENT** — and it is the one channel that can fall inside the margin, which is why it is excluded from the floors |
| `C-0034`'s `A2`, `C-0028`'s `B2` | 78.2353, 261.168 pN·nm/rad | **`C-0034`/`C-0028`**, re-run through their own libraries |
| `C-0026`'s break-even, `C-0060`'s flatness threshold | 17 %, 34.6 % | **CITED**; `T-45` is unmeasured and this claim does not measure it |
| the 34 stations, the phase, the 10.88 nm pitch | phase 24 | **`C-0063`/`C-0055`, CONSUMED AS DATA** from `gpd/results/T-125-*.json` |
| `C-0069`'s and `C-0066`'s published clearances | 0.0256, 8.19, 2.71561 nm | **`C-0069`/`C-0066`**, and every one re-derived here |
| §3 targets | 100 pN, 3 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the identity and its five readings, all four floors, every propagation coefficient and threshold, the null direction, the per-step variance threshold, the eight thermal channels and the two stiffness demands, the five stiffness-scatter records and the dropout map, the twenty seat records, the ten path-count records with their ceilings and margins, the four joint-window bisections, the five flatness solves and the four convergence records — is **derived here in code**, with `C-0009`'s, `C-0022`'s, `C-0039`'s, `C-0053`'s, `C-0063`'s, `C-0069`'s and `C-0070`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The decomposition, not the measurement.** Fischer's width exists but is **not decomposed**: nothing separates finite-size broadening from static disorder from thermal breathing, and no paracrystalline analysis of an origami lattice has ever been published. The survey names the experiment that would do it — a SAXS series at several tile widths measuring the second-order reflection, where finite size broadens order-independently and as `1/N` while cumulative disorder broadens as `n²` — and it needs no new instrument.
2. **A re-optimised 30-root placement.** `T-136`. This claim's reduction is a plan rule, and whether a flat two-per-row placement exists at 30 arms is `C-0063`'s own search under a new constraint.
3. **A joint softer than `C-0034`'s `A2` or one crossover.** The joint window says a one-rise margin is buyable at 34 paths by softening either end; whether such a joint exists in DNA is a catalogue question `C-0034` and `C-0009` have not been asked.
4. **Which correlation structure applies.** The 7× spread is exact and unresolved, and Fischer's width does not resolve it — a lattice-constant width is not a statement about whether a host and an arm are correlated. Cryo-EM multi-body refinement on a twist-corrected single-layer tile, one body per duplex, returns the covariance matrix directly and would settle it.

7. **The plan model against the measured weave.** `C-0041`'s and `C-0053`'s single-width convention is now known to average a 1.85–3.60 nm sawtooth, and the two ends of that sawtooth give opposite placement verdicts. Re-running the packer with a **position-dependent** exclusion width — which is computable, since the weave's phase is the crossover phase the layout already chooses — is a task this claim opens and does not run (`T-137`).

8. **`C-0069`'s path-count sensitivity re-sizes the element but not the array.** Its 15-path row places **34** instances of a 15-path arm, which presents `34 × 33.3333/15 = 75.6 pN/nm` — **2.27×** `C-0017`'s mandate; the 45-path row is 0.53× on its own reported count. **No headline number in `C-0069` moves** (all are read at the self-consistent `n = 34`), but the reading *"the placement is unchanged because the count is what sets the stations"* is what hid Deliverable 6's escape. Recorded here rather than challenged, and filed as `T-138`.
5. **The placement verdict at perturbed constants.** This claim propagates onto the *margin*; the *count* is a step function of it and only `C-0069`'s 2.73 nm point has been evaluated.
6. **`T-45`'s remaining half.** The *incorporation* is measured (Deliverable 7) and answers the dropout mechanism; the **stiffness spread of nominally identical hybridised staple extensions as such** is not, and nine queries across three databases found nothing. The nearest published work measures an inter-subunit ssDNA handle by cryo-EM multi-body refinement — which is the method that would supply it one level down.

## Challenges

**Raises [`CH-0084`](../challenges/CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md)** against `C-0060`'s tolerance verdict — *"the tolerance is a threshold, and it is generous"* — and through it against `C-0058`'s flat distribution. `C-0060` computed the threshold *"precisely so a measured or specified tolerance can be substituted"*; the measurement is **1.26×** past it, and the mean-only mandate shortfall (16 %, no pattern assumption) is **2.9×** the placement error `C-0060` spends a trim to remove. **`C-0060`'s stiffness and buildability results are untouched and reproduce.**

**None stands against this claim.** The five ways it would fail:

1. **A measured origami lattice-constant distribution narrower than 0.0256 nm.** That would leave floors 1, 3 and 4 standing, so it would not change the verdict — but it would change floor 2, which is the smallest.
2. **A demonstration that the interhelical distance in a single-layer sheet is *not* 2.69 or 2.73 but a single value known better than 0.026 nm.** Floor 2 would fall; the other three would not.
3. **A footprint convention in which consecutive collinear elements need no clearance** — `C-0069`'s own largest lever. The budget would rise to the bare 10.88 nm pitch and there would be no knife edge to model.
4. **A crossover in-plane stiffness measurement at the top of `C-0009`'s sweep.** That would put the interhelical thermal channel inside the margin and would be the first channel this claim reports as *not* a floor becoming one — in the favourable direction.
5. **A flat 30-root placement.** Then the escape of Deliverable 6 is free and the recommendation changes from *"the margin is unquotable"* to *"build 30"*.
