# T-10 — Discrete-lattice check of the tile: a beam-and-hinge grillage instead of a continuum plate

| | |
|---|---|
| **Leaves** | `A8.2` (structural rigidity / mode analysis), `A1.2` (`../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 tasks 5 and 5b; questions §4(f) and §4(g); parameters §3; geometry §1 |
| **Verification type** | in-silico (a beam-and-hinge grillage finite-element model written for this task, calibrated against `C-0006`'s orthotropic Kirchhoff plate on a shared set of physical ingredients) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0009`](../claims/C-0009-discrete-lattice-tile.md), raises [`CH-0008`](../challenges/CH-0008-plate-conservative-about-flatness.md) |
| **Raised by** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md), whose own validity section names both of the reasons this task exists |

---

## Formulate

### Why this task exists

`C-0006` reached its conclusions with a continuum Kirchhoff plate and then reported two facts that undercut that choice.
Both are quoted from `C-0006` rather than paraphrased:

> **`ℓ_⊥/p < 1` across the whole sweep**, which is a validity failure of the model itself:
> the across-helix bending length is *shorter than the crossover spacing*, so the continuum plate
> reduction is marginal and the tile is closer to **~15 quasi-independent duplex beams sharing a
> polymer cushion** than to a plate.

> The internal transfer is milder still: the anchor force spread over the 9.3 load paths on an
> `ℓ`-sized contour around it gives **1.9 pN per crossover or duplex**.

The first says the model is out of its own validity range.
The second is an *equal-sharing assumption*, not a peak — and §4(f) asks whether the field pulls the origami apart, which is a question about the peak.
A continuum plate cannot answer it, because the anchor reaction enters the plate as a delta function and leaves it smeared over a contour.

### The question, as a numeric target

Build the same sheet as a **discrete grillage** — duplex beams, crossover hinges, Winkler springs on the lattice's own tributary areas — and produce:

1. the discrepancy, as a number on each quantity `C-0006` reports, between the lattice and the plate, **with the direction of the plate's error stated**;
2. the **peak force on one crossover** at and around a discrete anchor, against the per-path allowables;
3. the number of attachment points flatness demands, counted the way a lattice counts.

### Acceptance predicate

Discharged when all seven hold.

1. **Gate 2 passes before any result is computed**: the lattice's long-wavelength limit reproduces `C-0006`'s `D_∥`, `D_⊥` **and** `D_k`, each separately, to a stated and explained residual. If it does not, the model is wrong and every discrepancy it reports is a change of parameters rather than of functional form.
2. Both principal rigidities, the four dishing amplitudes `C-0006` reports (uniform, edge taper, 4 anchors, single lever) and the thermal RMS are computed on **both** models over the same `k_f` sweep, and the ratio is reported for each.
3. The verdict on `C-0006`'s own prediction — *"a discrete lattice has more shape freedom than the plate that approximates it, not less"* — is stated as **confirmed or refuted**, per load case, and not inherited.
4. The **peak per-crossover force** is reported against the per-path allowables `C-0006` established: single-duplex shear ~48–65 pN, unzip 10–15 pN, and the 65 pN hard ceiling because every origami helix is nicked. The §4(f) 35–60 pN band is **not** used as a per-path allowable, per `C-0006`'s own literature trace.
5. The peak is reported **against `C-0006`'s 1.9 pN equal-sharing figure**, as a concentration factor, so that the thing `C-0006` declined to estimate is delivered as a ratio to what it did estimate.
6. Every conclusion is stated as a function of the crossover hinge constant `k_θ`, swept over Chen et al.'s admissible `α ∈ [0.6, 1.2]` **and** probed at the out-of-range stiffness `CH-0005` names as the way `C-0006` could be wrong.
7. The `no discrete attachment scheme is flat` conclusion is confirmed, refuted or sharpened, with the lattice's own patch count, and with the attachment threshold **solved** on both models rather than taken from the continuum heuristic that produced `C-0006`'s 55.

### Units, locked

SI, scaled, per `P-2`: lengths nm, forces pN, moments and energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly),
stiffness pN/nm (`= 1 mN/m` exactly), **flexural rigidity pN·nm**, **foundation stiffness per unit area pN/nm³**,
hinge stiffness pN·nm/rad, **hinge stiffness per unit interface length pN·nm/rad per nm**.
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer with Mg²⁺**.

### Geometry and sign conventions, fixed before deriving

Restated rather than inherited, per §5 of the problem definition.

- `x` runs **along** the DNA helices, `y` **across** them, `z` normal to the electrode and positive away from it; the origin of `(x, y)` is at the centre of the footprint.
- The plate/lattice deflection `w(x, y)` is measured **downward**, positive when the polymer layer is compressed. It is the opposite sense to `z`, deliberately, and the two are never added.
- The applied pressure `q(x, y) > 0` acts downward; the foundation reaction is `k_f w` upward.
- A load path carries **tension** as positive. A crossover's transmitted force is signed so that positive is transmitted from the far side of its interface toward the near one, which is the sign convention of the cut-equilibrium shear it must equal.
- **Lattice kinematics.** Duplex `i` has axis `y_i`, deflection `w_i(x)`, slope `θ_i(x) = dw_i/dx` and **roll** `φ_i(x)`. The deflection field on its tributary strip is `w(x, y) = w_i(x) + φ_i(x)(y − y_i)` for `|y − y_i| ≤ d/2`. Three degrees of freedom per node.
- **Footprint.** A lattice can only be an integer number of duplexes wide, so the tile is `40 × (15 × 2.69) = 40 × 40.35 nm`. **The plate it is compared against uses the same footprint**, so the comparison isolates the functional form; the effect of the 0.87 % area difference against `C-0006`'s 40 × 40 nm is reported separately and is below 0.8 % on every quantity.

### What a crossover is, mechanically

Named in advance, because the whole task turns on it.

| element | what it represents | constant |
|---|---|---|
| duplex beam, bending | the duplex as an Euler-Bernoulli rod | `EI = L_p k_BT` |
| duplex beam, torsion | the duplex rolling about its own axis between crossovers | `GJ` |
| **crossover hinge** | the relative **roll** of the two duplexes a crossover joins | `k_θ` |
| **crossover link** | the two helix surfaces staying connected in `z` | a **constraint**; its stiffness is a penalty and the answer must not depend on it |
| foundation | the polymer layer under a duplex's tributary strip of width `d` | `k_f d` on `w` **and** `k_f d³/12` on `φ` |

The roll foundation term is not decoration: `½ k_f ∬ (w + φΔy)² dΔy dx` is what the continuum `½ k_f ∫ w² dA` becomes on a strip, and a node-lumped foundation would lose it and let each duplex spin against nothing.

### Crossover topology, from the primary source

Crossovers recur every **16 bp** along a helix but **alternate between its two neighbours**, so one interface is linked every **32 bp = 10.88 nm** (`C-0006`, from Rothemund 2006).
The lattice therefore has columns at `p/2 = 5.44 nm`, and interface `i` takes the columns of one parity while interface `i+1` takes the other.
Eight columns fit strictly inside 40 nm, giving **4 crossovers per interface, 14 interfaces, 56 crossovers**.

This topology has a consequence a plate cannot have: **the lattice is not mirror-symmetric.**
A mirror in `x` maps one interface's columns onto its neighbour's; a mirror in `y` swaps interface parities too.
Only their product, the point inversion `(x, y) → (−x, −y)`, is an exact symmetry. It is asserted as a test rather than assumed.

### What is deliberately excluded

- **No electrostatics is solved.** The load enters as a 100 pN total and a bounded edge taper; `T-3` owns the load model, and the lattice response is shown to be linear in the taper depth exactly as the plate's is, so `T-3`'s answer can be substituted without a re-run.
- **No oxDNA.** `k_θ` is swept, not derived. Deriving it is `T-9`, costed at days, and it needs the coordinator's go-ahead.
- **No poroelasticity**, with a citation rather than an assumption: [`C-0004`](../claims/C-0004-poroelastic-drainage.md) puts the layer's drainage corner at 91 kHz, so at ≥ 1 kHz the foundation is **drained** and a quasi-static elastic Winkler foundation is licensed.
- **No in-plane membrane stiffening**, which is conservative — it can only stiffen the sheet.

---

## Plan

### The cheap bound, run first

Three closed forms, before any assembly:

1. **The direction-matched discreteness ratios.** `C-0006` compared `ℓ_⊥` against `p`. That pairs the **across**-helix bending length with the **along**-helix hinge spacing, which is a comparison between two different directions. Matched, the two criteria are `ℓ_∥/p` (bending along `x` against the hinge spacing along `x`) and `ℓ_⊥/d` (bending along `y` against the duplex spacing along `y`). Computing both costs nothing and it decides how bad the discreteness is expected to be *before* any lattice is built.
2. **The number of crossovers inside an anchor's influence patch**, `π ℓ_∥ ℓ_⊥ /(d p)`. If this is large the continuum is fine and the task is cheap; if it is of order unity the anchor is talking to a handful of discrete elements and the peak force cannot be a contour average.
3. **The long-wavelength energy identities**, by hand: imposing `w = ½κy²` on the lattice costs `½ k_θ (κd)²` per crossover, and the crossover areal density is `1/(dp)`, so `D_⊥ = k_θ d/p` — `C-0006`'s closed form, recovered rather than assumed. The same for `w = ½κx²` → `D_∥ = EI/d` and `w = τxy` → `D_k = GJ/(4d)`. These make gate 2 an **exact** test rather than a tolerance.

### The expensive calculation, and why it is this one and not a bigger one

**Chosen: a 855-degree-of-freedom beam-and-hinge grillage finite-element model, dense Cholesky, written for this task. Cost: seconds per configuration, ~45 s for the whole study.**

| method | cost | why not |
|---|---|---|
| closed forms alone | seconds | run first, and they set up gate 2 — but they cannot resolve a discrete anchor, which is the number this task exists to produce |
| **beam-and-hinge grillage** | seconds | **chosen** |
| refine `C-0006`'s Ritz plate | seconds | buys nothing: refining a continuum does not make it discrete, and the quantity in question (force on *one* crossover) does not exist in it |
| CanDo-style base-pair beam FEM | days, and needs a design file we do not have | and **its crossover model is rigid, by its authors' own statement** — which is exactly the compliance a single-layer sheet has nowhere else. It would answer the question by assuming it away |
| oxDNA coarse-grained MD | days on this box | the only route to `k_θ` from first principles, and that is `T-9`, a separately costed queue item |

The grillage buys three things nothing else does: the rigid-body modes are exact nodal vectors, so "dishing" needs no plane fitting; the coefficient covariance under equipartition is `k_BT K⁻¹`, so the thermal amplitude comes from the same matrix as the loaded one **with no sampling**; and every crossover is an addressable element whose transmitted force is read off directly.

Prefer **published measurement on the actual material** over simulating it, per the research practice — and here it is the *reverse* of `P-3`: no measurement of a single crossover's force in a loaded sheet exists, the only accessible constant (`k_θ`) is already cited from Chen et al., and the thing being computed is a *load path topology* rather than a material constant. A lattice solve is therefore the cheap route and MD is the expensive one, not the other way round.

### The thermal amplitude comes from the same matrix, exactly

Equipartition on the assembled stiffness: the coefficient covariance is `k_BT K⁻¹`, so
`⟨w_dish²⟩ = k_BT tr(M K⁻¹)` with `M = G − Σ_k (G u_k)(G u_k)ᵀ/(u_kᵀ G u_k)` over the three rigid modes,
and `G` the area Gram matrix — which is the foundation matrix at unit `k_f` over the area, so it is assembled once and reused.
`tr(G K⁻¹)` is evaluated by factorising `G = C Cᵀ` and summing `‖L⁻¹C_j‖²`, i.e. one **forward** substitution per degree of freedom rather than a full solve.
No Monte Carlo, no statistical-power question: the fluctuation is exact for a harmonic functional.

### What would falsify this approach

Stated in advance, per §5. The outcome of each is in Verify.

1. **The long-wavelength limit failing to reproduce `D_∥`, `D_⊥` or `D_k`.** Then the lattice is not a discretisation of `C-0006`'s plate and the comparison is meaningless. This is the strongest falsifier and it is wired in as three separate tests.
2. **A uniform load dishing the lattice.** A uniform load on a uniform foundation translates a free structure exactly, whatever its rigidity *and whatever its connectivity*. If the lattice dishes, the assembly is broken. Inherited from `T-5` and wired in as both a test and a runtime `check` in the study.
3. **The crossover force depending on the link penalty.** The vertical link is a constraint; if the transmitted force still moves when the penalty is stiffened, it is not a constraint force and the number is an artefact of the penalty.
4. **The answer depending on the element subdivision.** A displacement finite element is too stiff; refining must soften it monotonically on nested meshes, and the change between the last two must be small.
5. **The lattice agreeing with the plate nowhere, including where the plate's own criterion is satisfied.** Softening the foundation by 200× lifts `ℓ_∥/p` above 3 and `ℓ_⊥/d` above 5, i.e. into the regime the plate assumes. If the two models still disagree there, the disagreement at the working stiffness cannot be attributed to discreteness and the model has a bug instead.
6. **Both models agreeing everywhere at the working stiffness.** Then `C-0006`'s reduction is fine, the validity breach it recorded is harmless, and this task's deliverable is a licence rather than a correction. (Would make the answer cheap, not wrong.)

---

## Execute

Code: `src/main/kotlin/structure/` — `OrigamiGrillage.kt` (the lattice), `DiscreteLatticeTileStudy.kt` (the study),
reusing `Cholesky.kt` (extended with `forwardSolve` and `lowerColumn`, both additive), `OrigamiSheet.kt`, `PlateOnFoundation.kt`,
`LoadPaths.kt`, `Gen1Tile.kt` and `ResultRounding.kt` unchanged.
Tests, written first: `src/test/kotlin/structure/OrigamiGrillageTest.kt`, 21 tests named for the gate they discharge.

```shell
./gradlew test -PbuildDirectory=build-t10
./gradlew study -PbuildDirectory=build-t10 -Pstudy=structure.DiscreteLatticeTileStudyKt
```

Result: [`../results/T-10-discrete-lattice-tile.json`](../results/T-10-discrete-lattice-tile.json), deterministic in filename **and content** —
re-run and diffed to confirm, with `ResultRounding.kt` applied at the serialisation boundary.

5 foundation stiffnesses × (5 dishing sources × 2 models + thermal × 2 models + 12 anchored cases + 6 concentrated cases),
plus a 4-point `k_θ` sweep, a 10-point convergence sweep, a 4-point anchor-phase sweep and a 14-point flatness scan on both models.
Whole study: ~45 s.

---

## Verify

### The five gates

#### Gate 1 — dimensional consistency

- A rigid translation stores exactly `½ k_f A` per unit deflection and **nothing** in the structure: bending, torsion, hinges and links all vanish identically. Asserted to `1e−9` of the foundation energy.
- The area Gram form returns `1` for a unit piston, `L_x²/12` and `L_y²/12` for the two unit tilts, and zero for every cross term — i.e. the mesh reproduces a linear field exactly and the three rigid modes are area-orthogonal. Asserted to `1e−12`.
- Column spacing is exactly `p/2`; one interface sees every other column; 14 × 4 = 56 crossovers. Asserted, not assumed.

#### Gate 2 — limiting cases

| imposed field | lattice cost | plate cost | ratio |
|---|---|---|---|
| `w = ½κx²` (along helices) | `½ EI κ² L_x` per beam | `½ D_∥ κ² A` | **1.000000, exactly** |
| `w = ½κy²` (across helices) | `½ k_θ (κd)²` per crossover | `½ D_⊥ κ² A` | **1.015467** |
| `w = τxy` (twist) | `½ GJ τ² L_x` per beam | `2 D_k τ² A` | **1.000000, exactly** |

The one non-unit ratio is **exactly** `56/55.147`, the integer crossover count over the continuum areal density — asserted as an identity to `1e−9`, not as a tolerance. Two compensating discretisation effects make it: four crossovers per interface against the continuum's 3.676 (`+8.8 %`) and fourteen interfaces for fifteen duplexes (`−6.7 %`). On a lattice sized so the along-`x` count is exact, the residual is **exactly `(n−1)/n` in the duplex count** — asserted at `n` = 15, 24 and 36 and shown monotone.

Also discharged: a uniform load dishes the lattice by nothing at all at hinge stiffnesses spanning 10³; a rigid lattice translates under a point load to `P/(k_f A)`; quadrupling `k_f` quarters the deflection; a softer hinge softens `D_⊥` proportionally and leaves `D_∥` untouched.

#### Gate 3 — symmetry and conservation

- **Force balance**: foundation reaction + anchor reactions = applied load, to `1e−8`.
- **The crossovers on one interface carry exactly the shear crossing it**, computed independently from the equilibrium of everything beyond the cut. To `1e−6`. This is what makes the per-crossover force a *load path* number and not a post-processing artefact.
- **Equipartition**: a rigid lattice's piston fluctuation is `√(k_BT/(k_f A))` and its tilt contribution exactly `√2` of it.
- **Symmetry, corrected rather than assumed.** The lattice is **centro-symmetric and not mirror-symmetric**, and both halves of that are asserted: `w(x, y) = w(−x, −y)` to `1e−8`, and `w(−x, y) ≠ w(x, y)` by more than `1e−3` relative under a centred point load. A continuum plate has the full rectangular group and cannot lose this symmetry; the lattice loses it because the crossovers alternate.

#### Gate 4 — numerical convergence

| parameter | swept | peak crossover force [pN] | 4-anchor dishing [nm] | thermal dishing [nm] |
|---|---|---|---|---|
| element subdivision | 1 / 2 / 4 | 37.151 / 37.139 / 37.139 | 2.2629 / 2.2522 / 2.2501 | 1.4643 / 1.4670 / 1.4676 |
| link penalty [pN/nm] | 10² … 10⁶ | 36.69 / 37.10 / 37.139 / 37.143 / 37.144 | 2.2436 … 2.2523 | 1.4772 … 1.4669 |
| **crossover columns** | **7 / 8** | **44.146 / 37.139** | **2.3340 / 2.2522** | **1.6275 / 1.4670** |

The mesh and the penalty are converged to 0.1 % and 0.01 %.
**The crossover count is not a convergence parameter and does not converge** — it is a physical property of the design, and moving from 7 to 8 columns moves the peak crossover force by 19 %. That is the dominant uncertainty in the peak force and it is reported as one, not averaged away.
Mesh refinement is asserted monotone only on **nested** refinements (1 ⊂ 2 ⊂ 4); a non-nested mesh moves the load point from a node to mid-element and the monotonicity theorem stops applying — found empirically, and recorded.

#### Gate 5 — literature cross-check, premises checked against the material

- **Where the plate's own criterion is satisfied, the two models agree.** At `k_f/200`, which lifts `ℓ_∥/p` above 3 and `ℓ_⊥/d` above 5, the lattice and the plate give the same point-load dishing to better than 10 %. Wired in as a test. This is what licenses attributing the disagreement at the working stiffness to discreteness.
- **The model class is the accepted one for this structure.** Li, Madhvacharyula, Du, Adepu & Choi, *Chem. Sci.* **14**:8018 (2023), doi 10.1039/d3sc01793a, reviewing mechanical models of DNA nanostructures, describe Chen et al.'s spring system as the model for "a single-layer origami rectangle", with `k_t` the torsional and `k_b` the bending spring constant of the crossovers, and state the geometry verbatim: *"The length between neighboring crossovers is 16 bp."* That is the 16 bp per-helix spacing this lattice uses for its columns, whose alternation gives 32 bp per interface.
- **CanDo is excluded on its own authors' terms**, again verbatim from the same review: it is a base-pair-resolution beam FEM whose "boundary conditions are set such that the neighboring bases can slide but not separate", and `C-0006` already records that it treats crossovers as **rigid**. For a single-layer sheet that removes the only across-helix compliance there is.
- **The per-path allowables are used as `C-0006` traced them**, not as §4(f) states them: the 35–60 pN band is a whole-cross-section number at 5.5 pN/s and is **not** used here.

### The falsifiers, and whether they fired

| falsifier | fired? | outcome |
|---|---|---|
| 1. long-wavelength limit failing | **no** | `D_∥` and `D_k` exact; `D_⊥` off by exactly the integer crossover count, which is explained and asserted as an identity |
| 2. a uniform load dishing the lattice | **no** | zero to `< 1e−9 nm` at every hinge stiffness and every `k_f`; also a runtime `check` in the study |
| 3. crossover force depending on the penalty | **no** | 0.01 % between `10⁵` and `10⁶ pN/nm`; the reported force is a constraint force |
| 4. answer depending on the subdivision | **no** | 0.1 % between the last two nested meshes |
| 5. disagreement even where the plate is valid | **no** | agreement to better than 10 % at `k_f/200` |
| 6. **agreement everywhere at the working stiffness** | **partially, and informatively** | the two models agree to 0.6–2.7 % on the smooth edge-taper case at every `k_f`, and disagree by −16 % to +38 % on the two point-coupled cases. The plate is fine for smooth loads and wrong for point ones — which is exactly where `C-0006` used it |

### The predicate, item by item

1. gate 2 passes on all three rigidities, with the one residual explained and asserted as an identity — **yes**;
2. both rigidities, four dishing amplitudes and the thermal RMS computed on both models across the `k_f` sweep, with ratios — **yes**;
3. `C-0006`'s prediction stated per load case: **confirmed** for the thermal and concentrated cases, **refuted** for the anchored and edge-taper ones — **yes**, and it raises [`CH-0008`](../challenges/CH-0008-plate-conservative-about-flatness.md);
4. peak per-crossover force against duplex shear / unzip / the 65 pN ceiling, with the §4(f) band excluded — **yes**;
5. reported as a concentration factor against `C-0006`'s 1.9 pN equal-sharing figure: **2.3–7.6×** — **yes**;
6. every conclusion as a function of `k_θ`, over `α ∈ [0.6, 1.2]` and at the out-of-range `α = 25.6` probe — **yes**;
7. the flatness threshold solved on both models (**64**, against `C-0006`'s heuristic 55) and set against the lattice's own count of **56 crossovers** — **yes**.

## Result

Filed as [`C-0009`](../claims/C-0009-discrete-lattice-tile.md),
which raises [`CH-0008`](../challenges/CH-0008-plate-conservative-about-flatness.md) against `C-0006`.

## Feedback into Formulate

- **`T-9` becomes less urgent for `C-0006`'s conclusions and more urgent for the *design*.** Every ratio in this task is flat over Chen et al.'s admissible `α ∈ [0.6, 1.2]` — the anchored dishing ratio moves from 0.907 to 0.914 and the thermal one from 1.132 to 1.159, i.e. `k_θ` does not decide any verdict here. It takes the out-of-range `α = 25.6` probe to flip the anchored verdict. But the peak crossover force at an anchor *is* the number a design has to respect, and it rises with `k_θ` (3.42 → 3.90 pN over the admissible range, 4.89 pN at the probe), so `T-9` should be re-scoped from "does `C-0006` survive" to "what is the per-path force budget".
- **`T-2`'s design window gains a topological constraint with a lattice-native form:** flatness needs **more attachment points than the tile has crossovers** (64 against 56). That is stronger and more physical than "55 attachments against 43.7 patches", and it does not move if `k_f` is re-derived, because both counts scale together.
- **`T-8` should consume the lattice's total point RMS, not the plate's.** At rest and nominal `k_f` the lattice gives 1.433 nm against the plate's 1.364 nm, and 2.281 nm against 2.237 nm at the soft end of the sweep — still inside the 3.0 nm predicate but 5 % worse than `C-0006` supplied.
- **A new queue item:** the crossover *phase* is a free design variable with a 19 % effect on the peak per-path force, and nothing in the programme currently owns it. Where the first crossover column sits relative to the tile edge is chosen by the staple layout, i.e. for free.
- **`T-3` can still hand its load non-uniformity straight in.** The lattice's edge-taper response is linear in the depth to the same five digits as the plate's, and the two models agree on it to 2.7 %.
