# T-1d — A numerical self-consistent-field density profile for the Gen-1 grafted layer

| | |
|---|---|
| **Leaf** | `A2.1` (`../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 task 1 and task 2; premises in §2; parameters in §3; questions §4(a), §4(b) |
| **Raised by** | [`T-1c`](T-1c-crossover-valid-layer-response.md), falsifier (3), which **fired**; and [`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md), outstanding item 2 |
| **Verification type** | in-silico (numerical SCF, Edwards propagator), closed against the analytic ideal-chain solution and against `T-1c`'s two profile models in the limits where each is exact |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0011`](../claims/C-0011-scf-density-profile.md), raising [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) |

---

## Formulate

### The question, as a numeric target

`T-1c` produced the layer response from a measurement-anchored interaction free energy, and carried
**two profile models** — an Alexander box and Milner-Witten-Cates strong stretching — crossed with
**three interaction free energies** — two-body from the measured `A₂`, the measured virial expansion,
and the des Cloizeaux limb of the measured crossover equation of state. It found:

- the three interaction laws differ by only **1.45×** in `Π_int` at the layer's own volume fraction;
- but **the profile model decides whether the 10 nm design window exists at all** — empty under both
  box models, `[0.018, 0.061] nm⁻²` under strong stretching;
- and **neither profile model's premise is met**: the box is a restricted trial function, and strong
  stretching wants `L₀ ≫ R₀` where the Gen-1 layer has `L₀/R₀ = 0.83–1.07`.

So the single largest remaining uncertainty in the whole mechanical branch is the **shape of the
density profile**, and `T-2`'s deliverable — a non-empty window *or* a proof of emptiness — is
unavailable at 10 nm until it is settled.

Produce `φ(z)`, `L₀(N, σ)`, `N(L₀, σ)`, `P(h)`, `k(h)` and the stroke at the §3 target force from a
density profile that is **solved** rather than assumed, against the same interaction bracket, and
say whether the 10 nm window exists.

### Acceptance predicate

`TASKS.md` states it as:

> Whether the 10 nm window exists, decided by a profile whose premise is met.

Tightened here, and discharged only when **all six** hold:

1. The density profile is obtained from a **numerical** self-consistent field — a Scheutjens-Fleer
   lattice or an equivalent continuous-chain propagator — with no profile ansatz anywhere, and the
   choice between the two is justified against cost and against what each would cost in *fidelity to
   the measurement* already in hand.
2. The interaction free energy is **the same bracket `T-1c` used**, entering as the self-consistent
   field `w(z) = μ(φ(z))/k_BT` evaluated on the local volume fraction — so that the difference
   between this answer and `T-1c`'s is the profile and nothing else.
3. The chain model is justified by measurement rather than assumed: the Gaussian propagator on the
   measured Kuhn parameters is licensed by `C-0003`'s thermal-blob count, stated as a number.
4. The boundary condition at the wall is **stated and defended**, and the alternative is run as a
   sensitivity rather than dismissed.
5. `P(h)` is obtained by **two independent routes** — the thermodynamic `−∂F/∂h` and a
   contact-value theorem — and the two are checked against each other.
6. Gate 4 is discharged as a **demonstrated order** in each of the node spacing, the contour step
   and the self-consistency tolerance, not as an assertion that a solver converged.

### Units, locked

SI, scaled: lengths nm, forces pN, energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly),
stiffness pN/nm (`= 1 mN/m` exactly).
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer** (2–10 mM MgCl₂, not entering this task).
Energies also reported in eV via `1 eV = 160.2177 pN·nm`; `k_BT(300 K) = 25.85 meV`.

A **volume fraction** is always the physical one. In `T-1c` that was `φ = N σ v₀ / h`, a single
number; here it is a **field** `φ(z)`, and `N σ v₀ / h` is its mean. Both are reported and they are
not interchangeable — the peak of the SCF profile exceeds the mean by a factor that is itself a
result.

Osmotic virial coefficients keep `T-1c`'s convention: `Π/(RT) = c/M + A₂c² + A₃c³`, `c` in `g/cm³`,
**no factor of two**.

### Geometry and sign conventions, fixed before deriving

Restated verbatim from `T-1` and `T-1c`, because a downstream task must not have to look them up:

- `z` normal to the electrode, positive away from it, origin at the electrode surface.
- Chains grafted at `z = 0`, **one end fixed there and the other free**; the layer occupies `0 < z < h`.
- The tile is a **rigid, non-adsorbing wall** at height `h`. Compression means `h < L₀`.
- The disjoining pressure `P` is positive when the layer pushes the tile along `+z`.
- Stiffness `k = −∂F/∂h = −A ∂P/∂h`, positive for a restoring layer.
- The layer height is the **independent** variable and the chain length `N` follows from it.
- The layer is **grafted**, so it has no chain translational entropy, and the van't Hoff limb of the
  bulk equation of state is removed before that equation of state is used.

Two conventions are **added** here, and both are substantive.

- **The propagator is absorbing (Dirichlet) at both surfaces.** A rigid impenetrable tile removes
  every chain conformation that would cross it, which is what an absorbing condition is. The
  reflecting alternative is the *mid-plane between two identical brushes* — it costs the chains no
  conformational entropy — and it is carried as a sensitivity because it is exactly the assumption
  under which `T-1c`'s contact-value theorem `P = Π_int(φ(h))` is literally true.
- **`L₀` is defined by a stated resting load.** `T-1c`'s two models both have a sharp `L₀` because
  both profiles are trial functions that terminate. **An SCF layer has none**: its outer edge is a
  real decaying end distribution and `P = 0` is reached only asymptotically. `L₀` is therefore
  defined as the height at which the layer carries **1 pN over the 40 × 40 nm tile** — one percent of
  the §3 target force — and that threshold travels with every number derived from it. A decade
  either side is carried as a sensitivity.

### What is deliberately excluded

No electrostatics, no ion partitioning, no poroelasticity, no tile compliance. This is the purely
mechanical restoring term. Everything else is `T-3`, `T-4`, `T-6`, `T-7`, `T-5b`.

---

## Plan

### The method, and why this one

**A continuous-chain (Edwards) propagator solved numerically, not a Scheutjens-Fleer lattice.**
Both are defensible; the choice rests on two things, and the second is the load-bearing one.

&nbsp;&nbsp;&nbsp;&nbsp;`∂q/∂n = (b²/6n_K) ∂²q/∂z² − w(z) q`, &nbsp; `w(z) = μ(φ(z))/k_BT`, &nbsp; `μ = v₀ ∂f_int/∂φ`

1. **The chains are nearly ideal, and this is measured, not assumed.** `C-0003` establishes that PEG
   in water is a *marginal* solvent: excluded volume 12.25 Å³ against a 60.4 Å³ monomer, thermal blob
   1222 Kuhn segments (167 kDa), and the Gen-1 chains carry **0.02–0.10 of one thermal blob**. So a
   Gaussian propagator with the measured Kuhn parameters (`b = 1.1 nm`, `n_K = 3.11`, `C-0002`) is
   the *right* chain model here — a licence SCF usually does not get, and one worth stating rather
   than assuming silently.
2. **A lattice would have to throw away the measurement.** Scheutjens-Fleer expresses the
   interaction as a Flory `χ` on lattice sites, and `C-0007` shows that `χ` carries a lattice-site
   convention worth a factor of **2.010** — the exact analogue of `C-0002`'s three meanings of `a`.
   Re-expressing `f_int(φ)` as a `χ` and back would put a factor-of-two convention between this
   answer and the osmometry that `T-1c` spent an iteration anchoring it to. The continuum propagator
   consumes `InteractionFreeEnergy` **unchanged**, which is what makes this profile and `T-1c`'s
   differ in the profile and in nothing else.

### The cheap bound before the expensive calculation, per §5

| method | cost | role |
|---|---|---|
| **Alexander box profile**, closed form | microseconds | `T-1c`'s cheap bound — **already run**, carried here for comparison |
| **Generalised Milner-Witten-Cates strong stretching**, numeric | seconds | `T-1c`'s calibrating calculation — **already run**, carried here |
| **This: numerical SCF, Edwards propagator** | ~20 ms per solve, ~10⁴ solves per sweep | the calculation `T-1c`'s falsifier (3) said was now worth buying |
| Coarse-grained MD (Martini PEG + explicit Mg²⁺) | days-weeks of CPU | still not run — it would buy a worse interaction, not a better profile |

`T-1`'s original cost table deferred SCF numerics on the grounds that it would be *"calibrating to a
guess"*, because the interaction parameters were unknown. **That condition no longer holds**:
`C-0002` and `C-0003` anchor the interaction free energy in measurement. This is the calculation
becoming worth buying, exactly on the terms the project set in advance.

**What the grid can afford, stated in the Plan as asked.** A single solve at the production grid
(`Δz = 0.2 nm`, `Δn = 0.5` monomer) is ~20 ms. A pressure costs two solves and a stiffness three; a
resting height costs a bracketed root over `ln h`; a chain-length inversion costs a bracketed root
over `ln N` whose every evaluation is a resting height. That is ~7 s per (height, `σ`, interaction),
so the affordable sweep is **3 layer heights × 25 log-spaced grafting densities × 3 interaction
laws**, on four of the box's eight cores, plus the box and strong-stretching models at every point
for free. Ten minutes of wall clock, not hours — the cost was profiled before the sweep, and the
grid was chosen from the profile rather than from optimism.

### Where this can go quietly wrong, and what is done about each

**The wall pressure.** With an absorbing wall the volume fraction *vanishes* at the wall, so the
naive contact-value theorem `P = Π_int(φ(h))` returns zero and the whole normal stress at the wall is
conformational. Its correct continuum form is

&nbsp;&nbsp;&nbsp;&nbsp;`P(h) = k_BT (b²/6n_K) · lim_{z→h} φ(z)/[v₀ (h−z)²] + Π_int(φ(h))`

and it is checked against the thermodynamic `−∂F/∂h` rather than trusted.

**The free energy's additive constant.** `F/A = −σ k_BT lnQ − ∫Π_int(φ)dz` carries a constant from
the normalisation of the grafted source, and that constant depends on the node spacing. A
`−∂F/∂h` taken across two different spacings measures the constant. The derivative is therefore taken
by moving the wall **exactly one node layer** either way, so both solves share one spacing, and the
source amplitude is `1/Δz²` — the normalisation for which `q` converges to the boundary Green's
function derivative with an `O(Δz²)` error and no `ln Δz` at all.

**The iteration cap.** `CLAUDE.md` records that an unreachable tolerance silently runs the cap. Every
profile therefore carries its own `converged` verdict, its iteration count and its final residual,
and every consumer in the test file asserts them.

### What would falsify this approach

Stated in advance, before the run:

1. **The propagator failing to reproduce the analytic ideal-chain profile** when the interaction is
   switched off. The Edwards equation on `[0, h]` with Dirichlet ends has an exact double-sine-series
   solution that normalises itself to `N σ v₀` with no free constant; if the solver misses it, the
   source, the boundary condition or the contour quadrature is wrong and nothing downstream is worth
   reading.
2. **The SCF profile failing to approach the truncated parabola** in a layer that *does* meet the
   strong-stretching premise. Then the disagreement found at Gen-1 conditions would be a bug rather
   than physics.
3. **The two pressure routes disagreeing by more than the discretisation error.** Then either the
   free energy or the contact theorem is wrong.
4. **The SCF answer landing inside `T-1c`'s box-to-strong-stretching bracket at every design point.**
   Then the profile uncertainty `T-1c` reported as a *lower* bound was in fact the whole of it, this
   task changes no conclusion, and `T-1c`'s falsifier (3) fired on a question that did not need
   buying after all.
5. **The window's existence still turning on something not solved here.** Then `T-2` is still
   blocked, and the report has to say so plainly instead of answering anyway.

Outcome:

- **(1) did not fire.** The interaction-free solver reproduces the analytic ideal-chain series
  pointwise over the inner layer and its first moment to 5e-3.
- **(2) did not fire.** At `L/R₀ = 4.6` the SCF profile matches the truncated parabola to **1.4 %**
  over the inner 60 % of the layer, departing only at the outer edge where the parabola truncates and
  the real end distribution does not.
- **(3) did not fire.** The contact-value route and `−∂F/∂h` agree to 1–2 %, which is the size of the
  near-wall extrapolation error.
- **(4) FIRED, and in the direction that matters.** The SCF answer is **outside** `T-1c`'s bracket on
  the chain length by a factor of 3–4, and the disagreement is not a refinement of the profile shape
  but a **missing term**: neither of `T-1c`'s models contains the chain's entropic resistance to
  confinement, which at Gen-1 grafting densities is what holds the tile up.
- **(5) did not fire.** The 10 nm window is decided — `σ ∈ [0.0116, 0.2601] nm⁻²` — and it is decided
  the same way under all three interaction laws and over **two** decades of the resting-load
  threshold. A sixth thing did emerge that was not stated in advance: the criterion `P-5` adopted,
  `L₀/R₀ ≥ 1`, cannot bound the window from below once `L₀` is an onset height, and coil overlap
  `Σ = πR₀²σ ≥ 1` — which is also this method's own validity condition — has to be carried with it.

---

## Execute

Code, in the `brush` package, tests written first:

- `src/main/kotlin/brush/SelfConsistentField.kt` — `ScfDiscretisation`, `ScfWallCondition`,
  `ScfProfile`, and `SelfConsistentFieldLayer`, which implements the **same** `GraftedLayerModel`
  contract as `T-1c`'s `AlexanderBoxLayer` and `StrongStretchingLayer`, so the extension functions
  those models are consumed through apply here unchanged and the three answers are like for like.
- `src/main/kotlin/brush/ScfDensityProfileStudy.kt` — the study entry point.

Tests: `src/test/kotlin/brush/SelfConsistentFieldTest.kt`, 19 tests, named for the gate they discharge.

```shell
./gradlew test -PbuildDirectory=build-t1d
./gradlew study -Pstudy=brush.ScfDensityProfileStudyKt -PbuildDirectory=build-t1d
```

Result: [`../results/T-1d-scf-density-profile.json`](../results/T-1d-scf-density-profile.json) —
183 design points (3 layer heights × 61 log-spaced grafting densities from 0.002 to 1.0 nm⁻², the
same grid `T-1c` swept) × 5 models = 915 responses, **33 min wall clock on four threads, 373 MB peak**.
Deterministic: no timestamp, and every floating-point number rounded to nine significant digits at
the serialisation boundary — which matters more here than it did in `structure`, because the sweep
runs on four threads and the summation order of a reduction is not even fixed within one run.

---

## Verify

All five gates, executed as tests rather than asserted in prose. Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- The converged field **is** `μ(φ(z))/k_BT` at every node, to 1e-9 — which is the whole content of
  "self-consistent", and the one place `T-1c`'s measured interaction enters the profile.
- The layer free energy is the field-theoretic `−σ k_BT lnQ − ∫Π_int(φ)dz` to 1e-12, i.e. the
  Legendre transform doing the same work it does in `InteractionFreeEnergy`.
- Pressure × area reduces to a load in pN; stiffness per area × area to pN/nm.

### Gate 2 — limiting cases

- **The interaction-free layer reproduces the analytic ideal grafted-chain profile.** The strongest
  available check on the propagator itself: with the field off, the Edwards equation on `[0, h]` with
  Dirichlet ends has a closed-form double sine series that normalises itself to `N σ v₀`, so a wrong
  source, boundary condition, Simpson weight or contour quadrature shows up immediately.
- The pressure falls monotonically as the wall retreats, and the tile carries under 0.01 pN at 30 nm.
- **The profile approaches the strong-stretching parabola where strong stretching applies** —
  1.4 % over the inner 60 % at `L/R₀ = 4.6`, departing only at the outer edge. Falsifier 2 did not fire.

### Gate 3 — symmetry and conservation

- **The grafted coverage `∫φ dz = N σ v₀` is conserved to 1e-10 at every wall height**, for all three
  interactions. This is not a soft check: it holds only because every contour step applies the *same*
  operator, and it is what caught the Rannacher defect below.
- **`∫q(z,n)q†(z,N−n)dz = Q` at every contour split**, to 1e-9 — the propagator identity the density
  normalisation rests on.
- **The contact-value pressure agrees with `−∂F/∂h`** by two genuinely independent routes: the ratio
  runs **1.022, 1.011, 1.005, 0.997, 0.990** at `h` = 6, 8, 10, 13, 16 nm. The residual is the
  near-wall extrapolation of `φ/(h−z)²`, which is the only first-order-accurate step in the file.
- The work done compressing the layer equals the free energy it gains, on a ladder of heights that
  all share one node spacing.

### Gate 4 — numerical convergence

- **Second order in the node spacing**, exhibited rather than asserted. At the 10 nm design point,
  the relative error in `P` against a `Δz = 0.05 nm` reference falls **3.0e-3 → 7.1e-4 → 1.4e-4** for
  `Δz` = 0.4, 0.2, 0.1 nm, i.e. **observed orders 2.08 and 2.32**.
- **Second order in the contour step**: **2.5e-4 → 6.5e-5 → 1.6e-5** for `Δn` = 2, 1, 0.5 monomers,
  **observed orders 1.97 and 2.02**.
- The production grid (`Δz = 0.2 nm`, `Δn = 0.5`) therefore sits at **~7e-4 relative**, three orders
  of magnitude inside the ±15 % spread on `A₂` and inside the 1.45× spread between the interaction
  laws. The `Δn` used is not free: `r = DΔn/(2Δz²)` is capped at 0.5 and the step count raised until
  it holds, because above `r ≈ 1` Crank-Nicolson rings on the grafted delta into negative densities.
- **The self-consistency iteration reports reaching its tolerance rather than running the cap** —
  asserted as a test at three heights and three interactions, per the `CLAUDE.md` gotcha, and
  emitted with every convergence row: 32 iterations, final residual `10^−11.1` in `k_BT`, coverage
  conserved to `10^−14.2`–`10^−14.6` relative.
- The analytic stiffness matches a central difference of its own pressure to 2 %.

### Gate 5 — literature cross-check, premises checked against the material

- The Gaussian propagator is licensed by measurement: 0.02–0.10 thermal blobs per chain (`C-0003`).
- **The SCF resting height is NOT linear in `N`.** It is exactly linear for both `T-1c` profile
  models and any pure power-law interaction — proved as a test in `GraftedLayerTest` — and here the
  exponent is near **one half**, the single-chain coil exponent, with `L₀/R₀` between 1 and 3.5 over
  the whole range. That is the finding, and it is filed as `CH-0010`.
- The SCF layer is **taller and more diffuse** than either `T-1c` model at the same chain and
  grafting density, and it carries a real pressure at the strong-stretching resting height.
- A reflecting wall gives a denser contact layer and a **softer** response than an absorbing one,
  because it costs the chains no conformational entropy — which is the sensitivity that prices
  `T-1c`'s contact-value assumption.

### Not verified, and stated as such

- **Mean field.** No fluctuation corrections, no lateral inhomogeneity, no correlation hole. The
  ground-state-dominance approximation is *not* made — the full contour-resolved propagator is used —
  but the field is a mean field, and for a layer this dilute the fluctuation corrections are not
  bounded here.
- **`L₀` is threshold-defined.** The resting height does not exist without one, and a decade of
  threshold moves it. The sensitivity is reported; it is not removed.
- **Every osmotic input is still a bulk property applied to a brush** (`P-9`), and the equation of
  state is still linear PEG in pure water at 20–25 °C.
- The chain is monodisperse, the grafting points are fixed and laterally uniform, and the tile is rigid.
- Nothing here is measured *about this layer*. `PASS` means model-consistent and traceable.

---

## Result

Filed as [`C-0011`](../claims/C-0011-scf-density-profile.md), raising
[`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) against `C-0003`.
