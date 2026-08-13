# T-3b — The 2-D nonlinear Poisson-Boltzmann problem at the tile edge, and the lateral load profile it delivers

| | |
|---|---|
| **Leaf** | `A7.4` (`../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) — *"Solve the coupled Poisson-Boltzmann problem for the gated lever … as ONE system vs ionic strength"*, extended to the dimension `T-3a` declined |
| **Problem definition** | §6 tasks 3 and 5b; §4(g), which is the last §4 question still open; parameters §3; process §5, §7 |
| **Parent claims** | [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md), whose open question 2 is *"the lateral load profile is not computed and a 1-D treatment cannot compute it"*; [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md), which makes the dishing **exactly linear** in the taper depth and therefore consumes a profile without re-fitting; [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md), whose open question 3 names this task by ID |
| **Verification type** | in-silico (graded finite-volume Newton solve of the **2-D** asymmetric nonlinear Poisson-Boltzmann problem around a charged obstacle, plus a closed-form transverse-eigenvalue cheap bound) + logical |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** And *inside* mean field: `C-0005` puts the one-loop correction at 123–214 % of the leading term across this whole gap range, and this task inherits that whole. |
| **Status** | Executed, verified, filed as claim [`C-0022`](../claims/C-0022-tile-edge-load-profile.md); raises [`CH-0025`](../challenges/CH-0025-edge-taper-is-an-edge-enhancement.md) and [`CH-0026`](../challenges/CH-0026-forces-are-footprint-integrated-one-dimensional-pressures.md) |

---

## Formulate

### The question, as a numeric target

For the Gen-1 stack — a Manning-renormalised DNA-origami tile of finite lateral extent, held at a gap `h` above a biased electrode in 2:1 `MgCl₂` — produce, **from an actual 2-D solve** rather than from any 1-D treatment or lateral superposition formula:

1. **`Π(x)`, the lateral profile of the vertical electrostatic traction on the tile**, from the tile centre-line to beyond its rim, at the §3 working gaps and buffers, and at the biases `C-0012`/`C-0017` locate as the **operating point** — not at arbitrary grid biases, an error this project has made twice (`CH-0007`, `CH-0016`);
2. **the edge-taper parameter pair `(depth, width)` in exactly the form [`edgeTaperedPressure`](../../src/main/kotlin/structure/PlateOnFoundation.kt) consumes it**, so that `C-0006`'s and `C-0009`'s dishing follows without re-fitting anything;
3. **the dishing that pair produces, as a fraction of the stroke**, hence the lever-versus-sensor displacement split as **one number** rather than the 11 %–369 % band `C-0012` had to quote;
4. **the total-force correction** the edge costs, because a load that tapers at the rim is a load whose integral is smaller than `1-D pressure × footprint` — which is what every force in `C-0008` and `C-0012` is;
5. **the consequence for §4(g)**, and whether the taper moves a [`C-0016`](../claims/C-0016-design-window.md) window edge or a [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) per-load-path force into an allowable it was previously clear of.

### Acceptance predicate

> A lateral load profile `Π(x)` from a **2-D** nonlinear 2:1 Poisson-Boltzmann solve of the tile edge, reduced to the `(depth, width)` pair `C-0006`/`C-0009` consume, with the dishing and the lever/sensor split that follow, the total-force correction the edge costs, and a statement of whether §4(g)'s rigid-plate rejection is confirmed.

Discharged when all six hold:

1. the 2-D solve **reproduces the `T-3a` 1-D pressure deep under the tile**, at every state point, to better than the 1-D solve's own mesh convergence — the strongest available falsifier, because the two codes share only the ion model;
2. the lateral domain size and the far-field boundary condition are **stated, justified, and shown not to matter** — Dirichlet against Neumann at the outer edge, and two domain widths;
3. convergence is shown on **nested** refinements in both directions, as an order and not against a tolerance;
4. a **cheap bound runs first** and its expected error is stated in advance, and the solve is compared against it rather than merely reported after it;
5. the taper is emitted as `(depth, width)` **and** as the raw profile, so a downstream re-fit is possible without re-running the solve;
6. every state point carries a `numericallyResolved` flag, and the mesh-convergence table that justifies it is emitted.

### Units, locked

SI, scaled. Lengths nm; charge in elementary charges `e`; areal charge density `e/nm²`; number density `nm⁻³`; concentration mM; potential V; force **pN**; pressure **pN/nm²**, which is exactly 1 MPa; stiffness **pN/nm**, exactly 1 mN/m; energy pN·nm.
`k_BT = 4.142 pN·nm` at **T = 300 K**; `k_BT/e = 25.852 mV`; `l_B = 0.7141 nm` at `ε_r = 78`.
Medium **aqueous MgCl₂ buffer**, 0.5 / 2 / 10 mM.

### Geometry and sign conventions, fixed before deriving

Inherited from `T-3a` unchanged wherever they overlap, and extended by the one new axis.

- `z` is normal to the electrode, **positive away from it**, origin at the electrode surface.
- `x` is the new **lateral** coordinate. `x = 0` is the tile **centre-line**, which is a symmetry plane; the tile rim is at `x = a = 20 nm`, half of the 40 nm §3 footprint; the domain runs out to `x = X > a`.
- The **electrode** is the plane `z = 0` and extends laterally under the whole domain — it is a macroscopic electrode, not a counter-pad the size of the tile.
- The **tile** is an impermeable obstacle occupying `0 ≤ x ≤ a`, `h ≤ z ≤ h + t`, with `t = 10 nm` per §3. **"Gap" always means `h`, the tile-electrode separation**, never the polymer layer height, which happens to be numerically equal at rest.
- Its **bottom** face and its **top** face each carry the nominal `T-3a` charge density `σ_b = σ_t = −0.3987 e/nm²` (the "half the tile facing the gap" reading, applied to both faces, so the whole tile charge is present). Its **rim** carries `σ_rim`, nominally 0 — swept.
- `y = eψ/k_BT` is the **valency-free** reduced potential.
- **`σ` is signed**, as in `T-3a`.
- The vertical traction on the tile is written `Π(x)`, **positive upward** (away from the electrode), so it is the 2-D generalisation of `T-3a`'s disjoining pressure and equals it deep under the tile. The **load** `C-0006` consumes is `−Π`, positive downward.

### The premises that are checked rather than inherited

| premise | how it is handled |
|---|---|
| **2:1 asymmetry** | The ion model, the first integral and the saturation constants are `T-3a`'s, reused verbatim through `IonModel`. Nothing is re-derived and nothing is re-litigated. |
| **the Stern series** | The applied bias is mapped to a diffuse-layer potential through `T-3a`'s `diffusePotentialOfAppliedBias`, at the *1-D* gap, and that `ψ_d` is the Dirichlet datum of the 2-D solve. Stated as an approximation: the electrode's compact layer is not re-solved laterally. |
| **charge saturation** | Checked before any charge model is chosen, per `C-0008`: the tile is at 93 % of its 2:1 saturation at 2 mM, so the three-fold reading ambiguity is 7 % in `σ_eff`. The **rim** charge is swept in the expectation that it would prove equally irrelevant — and it did not: see the falsifier table below. |
| **point ions are a lower bound on `\|F_es\|`** | Inherited whole from `C-0008`'s Bikerman bracket, `+0.8 %` to `+56 %`, one-sided and upward. It is a *scale* correction and the taper is a *ratio*, which is the argument for **not** repeating it here. **That argument is asserted, not tested** — the solver carries the size-modified ion model through `IonModel` and no state point was run with it. |
| **mean field is uncontrolled here** | `C-0005`: 123–214 % at these gaps, and for the oppositely charged pair not even the direction is published. Inherited, stated, and **not** improved by adding a dimension. |

### What is deliberately excluded

No 3-D corner solve — the corner is treated by two mappings that bracket it, and the bracket is reported rather than resolved. No lateral variation of the Stern layer. No explicit-ion simulation (`C-0005` prices it at 1–3 weeks). No re-derivation of `C-0006`'s plate or `C-0009`'s lattice: their solvers are *consumed*, read-only, exactly as they were built to be.

---

## Plan

### Method, and the justification against cost

**Chosen: a closed-form transverse-eigenvalue cheap bound first, then a graded finite-volume Newton solve of the 2-D nonlinear problem on a tensor mesh with the tile as a masked obstacle, the linear system taken by conjugate gradients preconditioned with symmetric line Gauss-Seidel. Cost: minutes.**

| Method | Cost | Verdict |
|---|---|---|
| Quote `C-0006`'s assumed 50 % over 4 nm | free | **Rejected by `C-0006` itself**, which calls it *"a bounded perturbation"* and says `T-3` owns the load model. |
| `C-0008`'s hand-off `d ln\|P\|/dh = −1/ℓ` | free | Converts a **geometric** perturbation into a load one. The edge is not a geometric perturbation, so this is the wrong lever. Reported as the reason a 1-D task could not close this. |
| **Half-plane superposition depth + slit transverse-eigenvalue width** | µs | **Run first, as the cheap bound.** Both halves are closed form given a 1-D solve, and the width half is a *rigorous upper bound* within linear theory. |
| **2-D nonlinear PB, graded FV + Newton + PCG** | ~2 s per state point | **Chosen.** |
| FEniCS / a third-party PDE stack | hours of setup | Rejected: it would put the load-bearing numerics outside this repository's test suite, and the geometry is a rectangle with a rectangular hole. |
| 3-D solve of the whole tile | hours | **Not run.** The edge is straight over 40 nm against a 3–7 nm taper, so the 2-D reduction is good everywhere except within a taper width of a corner — 4 % of the footprint. Bracketed instead, see below. |
| Explicit-ion Monte Carlo | 1–3 weeks | **Not run**, per `C-0005`'s costing. It is the only thing that would repair the mean-field error, which dominates every number here. |

**The decisive argument is not cost, it is that the cheap bound cannot supply the width and the depth from the same premise.** The half-plane depth argument is an *unconfined* superposition result and the tile sits in a slit; the transverse-eigenvalue width is a *linearised* result and the tile is charge-saturated. They are each one-sided and they are one-sided in opposite directions, so a design number cannot be read off their combination. The 2-D solve is what closes the interval — and the cheap bound is what makes its answer auditable rather than merely asserted.

### Why the cheap bound is the transverse eigenvalue, and what it predicts

Linearise about the 1-D solution and look for a lateral mode `δy = φ(z) e^{−q x}`:

&nbsp;&nbsp;&nbsp;&nbsp;`−φ'' + κ_loc²(z) φ = q² φ`, &nbsp; `φ(0) = 0` (the electrode is held at a potential), &nbsp; `φ'(h) = 0` (the tile carries a fixed charge),

with `κ_loc²(z) = −4π l_B (dρ/dy)` at the 1-D profile, which is `≥ κ²` everywhere because counterion accumulation only strengthens the screening. The lowest eigenvalue therefore obeys

&nbsp;&nbsp;&nbsp;&nbsp;**`q₀² ≥ κ² + (π/2h)²`**,

so the taper's decay length is **at most** `1/√(κ² + (π/2h)²)` — 3.34 nm at 2 mM and a 10 nm gap, 2.47 nm at 5 nm. **The prediction is therefore that the taper is narrower than the Debye length, and narrower than the 4 nm `C-0006` assumed**, and that it narrows as the gap closes, which is the opposite of the intuition that a wider gap leaks less.

The depth half comes from the exact linear-superposition anchor: a semi-infinite uniformly charged plane produces, on the plane and at its own edge, exactly **half** the potential of a complete plane, so the rim behaves as if the tile carried `σ/2`, and `T-3a`'s closed-form linear mixed-boundary pressure evaluated at `σ/2` gives a depth. Its expected error is stated in advance: **a factor of about two**, because the superposition ignores the electrode's image and the linear theory ignores the tile's charge saturation, and those two run in opposite directions.

### Why the mesh is graded, and where

Exactly `T-3a`'s argument, now in two directions. The Gouy-Chapman length at the tile is `μ ≈ 0.09 nm` against a 40 nm tile and a 60 nm domain, and a uniform mesh that does not resolve `μ` leaves an **absolute** error in the surface potential which enters the traction as a constant offset — and a constant offset is precisely what a *taper* measurement cannot survive, because the taper is a ratio of two tractions. Nodes are placed by `tanh` grading:

- in `z`: clustered at the electrode, at the tile's bottom face, at the tile's top face, and stretched into the bulk above;
- in `x`: clustered at the rim from both sides, where the re-entrant geometry puts the strongest lateral gradient, and stretched toward the centre-line and toward the far field.

The grading is analytic and the mesh lines fall exactly on `z = h`, `z = h + t` and `x = a`, so the obstacle is resolved with **no staircase error at all** and the scheme stays second order — which gate 4 checks as an order.

### Why conjugate gradients with a line preconditioner

The finite-volume Jacobian on a tensor mesh is symmetric (the face coefficient `ε A/d` is the same seen from either side) and negative definite (because `dρ/dy < 0` everywhere, which is the same fact that makes Newton globally convergent in `T-3a`), so the negated system is **SPD** and conjugate gradients is the right method rather than a lucky one. The conditioning is dominated by the `z` grading — five orders of magnitude between the finest and coarsest cell — so the preconditioner solves each `x`-column's tridiagonal exactly, in a forward-then-backward symmetric sweep, which removes that direction from the condition number and leaves only the far milder `x` one. A dense or banded factorisation was rejected on cost: at 80 000 unknowns and a bandwidth of 300 it is 3e9 flops **per Newton step**.

### What would falsify this approach

Stated in advance, per §5.

1. **The 2-D solve failing to reproduce `T-3a`'s 1-D pressure deep under the tile.** The two codes share only `IonModel`; the mesh, the discretisation, the linear solver and the traction evaluation are all new. If they disagree at `x = 0`, the 2-D code is wrong and nothing downstream is worth reading.
2. **The far-field boundary condition mattering.** If Dirichlet and Neumann at `x = X` give different tapers, the domain is too small and the answer is a boundary artefact.
3. **The taper coming out wider than the tile half-width.** Then it is not an edge effect, `C-0006`'s raised-cosine perturbation cannot represent it, and the whole "taper depth × width" parameterisation the downstream claims consume would have to be abandoned rather than filled in.
4. **The depth exceeding 1.** A load that reverses sign at the rim is outside the `0..1` domain of `edgeTaperedPressure`, and `C-0006`'s exact linearity in depth would then be being used outside its own range.
5. **The rim charge mattering.** If `σ_rim` swept from 0 to the face density moves the depth materially, the tile's rim charge becomes a load-bearing unknown that no source in this project supplies.
6. **The dishing exceeding the layer height.** Then the plate has left its own linear domain and the number cannot be quoted.

### Which of them fired — recorded after the fact

| falsifier | outcome |
|---|---|
| 1. centre-line ≠ `T-3a` | **Did not fire.** 0.03–0.14 % at every one of 21 state points. |
| 2. far-field BC mattering | **Did not fire.** Dirichlet against Neumann moves nothing beyond the sixth digit — once the far-field datum was made the *isolated electrode's own profile* rather than zero, which was itself a bug this falsifier caught. |
| 3. taper wider than the half-width | **Did not fire**, but it is the closest call: 8.94 nm against a 20 nm half-width, i.e. 45 %. |
| 4. depth exceeding 1 | **Did not fire** for the fitted depth (`\|d\| ≤ 0.52`). The *pointwise* load at the 1 nm standoff does exceed the interior by up to 2.7×, which is why the two are reported separately. |
| 5. rim charge mattering | **FIRED.** 1.85× on the depth. Reported as a bracket; the rim charge joins the programme's unsourced numbers. |
| 6. dishing exceeding the layer height | **Did not fire.** 0.54–4.18 nm against a 10 nm layer, across the whole foundation sweep. |

And one that was not declared fired as well: **the corner traction is mesh-divergent**, not merely mesh-dependent. That is why the fit carries a 1 nm standoff and the residual is recovered from a global momentum balance.

---

## Execute

Code, all new, all in `src/main/kotlin/electrostatics/`:

- `TileEdgeFringing.kt` — the cheap bound: the half-plane superposition depth, the transverse eigenvalue and its closed-form lower bound, and the taper-fit algebra (depth, equivalent width, load deficit) that turns a solved profile into the pair `edgeTaperedPressure` consumes;
- `PoissonBoltzmannEdge.kt` — the 2-D graded finite-volume Newton solve around the masked obstacle, the PCG/line-Gauss-Seidel linear solver, and the traction profile read off the converged field;
- `TileEdgeLoadProfileStudy.kt` — the study and its `main`.

Nothing in `actuator`, `brush`, `structure`, `anchoring` or `coupling` is modified. `structure`'s `PlateOnFoundation`, `edgeTaperedPressure`, `Gen1Tile` and `origamiSheet` are **consumed read-only**, which is what makes the dishing number `C-0006`'s and not a second opinion.

Tests, written first and watched fail: `src/test/kotlin/electrostatics/TileEdgeFringingTest.kt` (16) and `PoissonBoltzmannEdgeTest.kt` (22). Every existing electrostatics test is untouched, and the authoritative full-suite run through `tools/verify.sh` is green.

```shell
./gradlew test -PbuildDirectory=build-t3b
tools/study.sh electrostatics.TileEdgeLoadProfileStudyKt
```

The study is ~5 minutes of wall clock for 21 solved state points at 89 000 nodes each plus an 11-point convergence and domain sweep that reaches 158 000.

Result: [`../results/T-3b-tile-edge-load-profile.json`](../results/T-3b-tile-edge-load-profile.json).
Deterministic: no timestamp, and the whole tree rounded at the serialisation boundary per `structure/ResultRounding.kt`. Verified by re-running: `tools/study.sh` reports *no result file changed*.

---

## Verify

All five gates, executed as tests. Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- The 2-D traction is assembled from the **same** `k_BT/nm³ → pN/nm² → pN` chain as `T-3a`, asserted against it.
- The Maxwell term of the 2-D traction is asserted to reduce to the contact-value `2π l_B σ²` exactly when the lateral field vanishes — i.e. the 2-D stress tensor is checked to contain the 1-D one as a term, not to resemble it.
- The transverse eigenvalue is asserted to have dimensions of inverse length by returning `κ` exactly for a free bulk mode.
- The equivalent taper width is asserted to be a length by the identity `∫deficit = depth × Π∞ × W/2`, which is the definition it is fitted from.

### Gate 2 — limiting cases

- **A tile with no edge**: as `a → ∞` the profile at the centre-line reproduces the 1-D pressure.
- **An uncharged tile at zero bias**: traction identically zero, laterally as well as in the mean.
- **The transverse eigenvalue reduces to `κ` when `h → ∞`** and to `π/2h` when `κ → 0`, both asserted as limits and not tolerances.
- **The taper fit is exact on a synthetic raised cosine**: fed the very profile `edgeTaperedPressure` generates, the fit must return its own `(depth, width)` — a round-trip, not a resemblance.
- **The linear-superposition depth is bounded** by the depth of a fully uncharged rim, which is the `σ → 0` limit.

### Gate 3 — symmetry and conservation

- **Global electroneutrality of the 2-D domain**: tile charge + electrode charge + `∫∫ρ dA` sums to zero.
- **The centre-line is a symmetry plane**: the lateral flux across `x = 0` vanishes identically.
- **The first integral is recovered laterally**: deep under the tile, where `∂/∂x → 0`, the vertical first integral is constant in `z` again, and the departure from constancy is the taper's own signature rather than a discretisation error.
- **The total force from the traction profile equals the total force from the domain's momentum balance**, by two independent routes.

### Gate 4 — numerical convergence

- **Nested** refinement, `1 → 2 → 4` in each direction separately and then jointly, with the order reported rather than a tolerance asserted (`CLAUDE.md`: a subdivision of 3 moves a feature off a node and breaks monotonicity, so 1/2/4 and never 1/2/3/4).
- **Domain independence**: the outer boundary at `X − a` = 20 nm and 40 nm, and Dirichlet against Neumann there.
- **Newton and PCG both driven to a residual that is reported**, not assumed.
- Every state point carries `numericallyResolved`, read from its own centre-line agreement with `T-3a`.

### Gate 5 — literature and upstream cross-check

- **`T-3a`'s 1-D pressure is reproduced at the centre-line** at every state point, through a code that shares only the ion model.
- **`C-0008`'s force table is reproduced** at the corresponding gaps and biases, as the `x → 0` limit of this solve times the footprint.
- **`C-0006`'s exact linearity in the taper depth is re-demonstrated** on the fitted pair, and its `dishing ≈ 0.54 × depth × stroke` calibration is reproduced at its own 4 nm width before being replaced at the solved width.
- **`C-0009`'s lattice-versus-plate ratio for a smooth edge taper** (the plate overstates by 1–16 %) is carried through as a stated correction rather than silently ignored.

### Not verified, and stated as such

- **Mean field.** Inherited from `C-0005` and `C-0008` whole and not improved: 123–214 % at these gaps, direction unknown for the oppositely charged pair. **Adding a dimension does not reduce this and no claim here suggests it does.**
- **Point ions.** `C-0008`'s Bikerman bracket is one-sided and upward, `+0.8 %` to `+56 %` on the force. **It is not run here at all**, on the argument that a scale correction cannot move a ratio — an argument that is stated rather than demonstrated.
- **The corner is bracketed, not solved.** A 3-D solve is the only thing that resolves it.
- **The tile is an impermeable obstacle with face charges**, as in `C-0008`. A real origami sheet has electrolyte in its interstices.
- **Nothing here is measured.**

---

## Result

Filed as [`C-0022`](../claims/C-0022-tile-edge-load-profile.md).
Raises [`CH-0025`](../challenges/CH-0025-edge-taper-is-an-edge-enhancement.md) against `C-0006`'s and `C-0009`'s edge-taper load case — its **sign** and its width, not its verdict.
Raises [`CH-0026`](../challenges/CH-0026-forces-are-footprint-integrated-one-dimensional-pressures.md) against `C-0008` and `C-0012`, whose every force is a 1-D pressure multiplied by 1600 nm².

## Feedback into Formulate

- **§4(g) closes.** The rigid-plate rejection is confirmed and the irreducible part of it is now a number rather than an assumption.
- **`C-0008`'s and `C-0012`'s forces are footprint-integrated 1-D pressures** and therefore overstate the total by the edge deficit. The correction is a multiplier and it is emitted so that any of them can be re-read without a re-run.
- **`T-4` and `T-16` inherit the same multiplier**, since a blocking force and a coupling requirement are both forces over the same footprint.
- **A 3-D corner solve is the follow-on**, and it is worth what the bracket is wide, not more.
