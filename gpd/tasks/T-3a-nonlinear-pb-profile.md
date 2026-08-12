# T-3a — The 1-D nonlinear Poisson-Boltzmann profile of the Gen-1 stack in the actual 2:1 buffer, tile and electrode as one system

| | |
|---|---|
| **Leaf** | `A7.4` (`../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) — *"Solve the coupled Poisson-Boltzmann problem for the gated lever … as ONE system vs ionic strength"* |
| **Problem definition** | §6 tasks 3, 4 and 6; mechanism and sign conventions §1; parameters §3; questions §4(c) and §4(e); process §5, §7 |
| **Parent claim** | [`C-0005`](../claims/C-0005-mean-field-screening-validity.md), which closed `T-6` but had to leave three numbers that only a real solve can supply |
| **Verification type** | in-silico (closed-form 2:1 Gouy-Chapman + graded finite-volume Newton solve of the nonlinear two-point boundary-value problem) + logical |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** And *inside* mean field: `C-0005` puts the one-loop correction at 123–214 % of the leading term across this whole gap range. |
| **Status** | Executed, verified, filed as claim [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md); raises [`CH-0007`](../challenges/CH-0007-point-ion-boundary-in-applied-bias.md); **resolves** [`CH-0004`](../challenges/CH-0004-screening-decay-length.md) |

---

## Formulate

### The question, as a numeric target

For the Gen-1 stack — a Manning-renormalised DNA-origami tile held at a gap `h` above an electrode biased to `V`, under 2 / 5 / 10 mM `MgCl₂` — produce, **from an actual solve of the asymmetric nonlinear Poisson-Boltzmann equation** rather than from any symmetric closed form or superposition formula:

1. **`σ_eff`, the tile's own far-field effective charge density**, replacing `C-0005`'s `σ_eff = κ/(π l_B q) = 0.0568 e/nm²`, which that claim flagged explicitly as an *order-of-magnitude ceiling only* because it is read from the **symmetric `z:z`** closed form while `MgCl₂` is **2:1 and asymmetric**;
2. **`F_es(h, V)`** across the §3 ranges, from the **osmotic + Maxwell first integral**;
3. **`k_es = −∂F_es/∂z` differentiated from the solve**, with the error in §1's `|k_es| ≈ F_es/λ_D` quantified — because `CH-0004` shows that reading is wrong here in the **non-conservative** direction for pull-in;
4. **the decay length the force actually has**, `ℓ = −1/(d ln|F_es|/dh)`, against **all three** of the Debye lengths `CH-0004` distinguishes.

### Acceptance predicate

> An effective charge density from an **asymmetric**-electrolyte solve replacing `C-0005`'s symmetric `z:z` ceiling; `F_es(h, V)` over the §3 ranges from the osmotic + Maxwell first integral rather than a superposition formula; `k_es` differentiated from the solve with the error in §1's `|k_es| ≈ F_es/λ_D` quantified; and the force's own decay length against all three Debye lengths `CH-0004` distinguishes.

Discharged when all six hold:

1. the 2:1 first integral is **derived**, not adapted from the symmetric `sinh` form, and the symmetric form is reproduced as a *limiting case* of the same machinery;
2. `σ_eff` is emitted at every §3 buffer, for **both signs of surface charge**, and the sensitivity to the (three-fold ambiguous) reading of the tile's gap-facing charge is reported;
3. `F_es` is emitted at every §3 gap and buffer and at biases up to 2 V, from a route that is **exact given the profile** — no linear superposition anywhere in the force path;
4. `k_es` is a **numerical derivative of the solve**, shown to be step-independent, and the ratio to §1's estimate is tabulated;
5. `ℓ` is tabulated against the bulk `λ_D` (3.93 nm), the gap counterion length (0.84–1.18 nm) and the in-layer length (4.5–5.5 nm);
6. every state point carries a flag saying whether it is **numerically resolved**, and the mesh-convergence table that justifies the flag is emitted.

### Units, locked

SI, scaled. Lengths nm; charge in elementary charges `e`; areal charge density `e/nm²`; number density `nm⁻³`; concentration mM; potential V; force **pN**; pressure **pN/nm²**, which is exactly 1 MPa; stiffness **pN/nm**, exactly 1 mN/m; energy pN·nm.
`k_BT = 4.142 pN·nm` at **T = 300 K**; `k_BT/e = 25.852 mV`; `l_B = 0.7141 nm` at `ε_r = 78`.
Medium **aqueous MgCl₂ buffer**, 2 / 5 / 10 mM.

### Geometry and sign conventions, fixed before deriving

Restated in full, per §5, and enforced as tests rather than as comments:

- `z` is normal to the electrode, **positive away from it**, origin at the electrode surface.
- The **electrode** is at `z = 0`. The **tile** is at `z = h`; **"gap" always means the tile-electrode separation `h`**, never the polymer layer height, which happens to be numerically equal at rest.
- The tile carries **net negative** charge (phosphate backbone). **Positive bias on the electrode pulls the tile down**, toward `−z`. So `F_es,z < 0` under positive bias.
- Therefore `k_es = −∂F_es,z/∂z` is **negative**, per §1, and `k_eff(z, V) = k_brush(z) + k_es(z, V)`.
- `y = eψ/k_BT` is the **valency-free** reduced potential. Valencies live in the Boltzmann factors — `e^{−2y}` for `Mg²⁺` and `e^{+y}` for `Cl⁻` — never in `y`.
- **`σ` is signed here**, unlike `T-6` where `σ_s` was always a magnitude. In a two-*dissimilar*-surface problem the relative sign is the physics, so it cannot live in the geometry.
- `ℓ`, the decay length of the force, is `−1/(d ln|F_es|/dh) = F_es/k_es`, positive for an attraction that grows as the gap closes.

### The four premises that are checked rather than inherited

Each was named in the task brief as an error the previous iteration had already made or nearly made.

| premise | how it is handled |
|---|---|
| **2:1 asymmetry** | The first integral is **re-derived** for 2:1. `I = 3c`, `κ² = 24π l_B c`, and `(y'/κ)² = (e^{−2y} + 2e^{y} − 3)/3` — not the symmetric `sinh` form, and **not even in `y`**. The symmetric `z:z` amplitude is carried alongside *only* as a cross-check. |
| **two dissimilar surfaces** | Constant **charge** at the tile (phosphate `pKa ≈ 1`, no regulation), constant **potential** at the electrode, in series with a compact Stern layer. This is the **mixed** problem and it equals neither canonical case. The `V = 0` limit is where the difference is qualitative. |
| **Manning-renormalised charge** | 11.90 %, 1276 e over the tile — **cited from `C-0005`**, which derived it. The bare charge is carried only to show what it would have given. |
| **the polymer layer is in the gap** | The gap is a **medium with position-dependent salt partitioning and permittivity**, from `C-0005`'s §4(c) numbers. The Donnan potential of a neutral partitioning layer is reproduced by the solve as an independent check of `C-0005`'s geometric-mean combination rule. |

### What is deliberately excluded

No stroke and no layer response — that is `T-3` with `T-1c`'s stiffness. No pull-in verdict — that is `T-4`; the numbers it needs are handed over, not consumed. No lateral structure: see below, and it is stated as a limitation rather than estimated. No explicit-ion simulation: `C-0005` prices it at 1–3 weeks and it needs the coordinator's go-ahead.

---

## Plan

### Method, and the justification against cost

**Chosen: an exact 2:1 closed form for the single plate, plus a graded finite-volume Newton solve of the two-point boundary-value problem for the gap. Cost: three minutes of wall clock for the whole sweep.**

The candidates, in cost order:

| Method | Cost | Verdict |
|---|---|---|
| Reuse `C-0005`'s symmetric `σ_eff` ceiling | free | **Rejected by `C-0005` itself.** It is a ceiling, and the task exists because a force cannot be computed from it. |
| **Linearised (Debye-Hückel) mixed BVP, closed form** | µs | **Run first, as the cheap bound.** Two-line closed form; the disjoining pressure collapses to `k_BT κ²(y_d² − B²)/(8π l_B)`. Not adequate — see below — but it *brackets*. |
| **Linear superposition of the two saturated far fields** | µs | **Also run first**, as the *other* cheap bound. It brackets from the opposite side. |
| **Nonlinear 2:1 PB, 1-D, graded FV + Newton** | ~1 ms per state point | **Chosen.** |
| Size-modified (Bikerman) PB | same solver, one extra term | **Folded in** as a bracket — this is `T-6b`'s cheap step, and folding it in cost one function. |
| 2-D/3-D PB with the real origami charge pattern (FEniCS) | hours | Would answer the lateral-load question this task cannot. **Not run**; named as the follow-on. |
| Explicit-ion Monte Carlo | 1–3 weeks | **Not run**, per `C-0005`'s costing and the standing instruction. |

**The decisive argument is not cost, it is that the two cheap bounds disagree by a factor of fifteen.** At the working point the linearised solve at the same boundary data *overstates* `|F_es|` by ~3.7×, because a linear theory has no charge saturation and takes a 9.1 `k_BT/e` electrode potential at face value; linear superposition of the *saturated* far fields *understates* by ~4.0×, because at `κh ≈ 1.3` the two double layers overlap and superposition is outside its own premise. The true answer lies between them and nothing short of the nonlinear solve locates it. That is reported in the result file as `the_two_cheap_bounds_bracket_the_answer`, so the justification is auditable after the fact as well as asserted before it.

### Why a graded mesh, decided in advance

The Gouy-Chapman length at the tile's renormalised charge is `μ ≈ 0.09 nm`; at a biased electrode it is shorter; the gap runs to 30 nm and the far field decays on `λ_D ≈ 3.9 nm`. A uniform mesh resolving `μ` across 30 nm would need millions of nodes, and one that does not resolve it leaves an **absolute** error in the tile surface potential which enters the contact-value pressure as a constant offset — and a constant offset destroys the decay-length measurement at large gaps, which is the whole of `CH-0004`. Nodes are therefore placed at `z_i = h[1 + tanh(β(ξ_i − ½))/tanh(β/2)]/2`, clustered at **both** walls, with `β = 10` and `N ∝ h`. The grading is analytic, so the scheme stays second order, and gate 4 checks that as an *order* rather than assuming it.

### What would falsify this approach

Stated in advance.

1. **The first integral failing to be constant across the gap.** It is the exact conservation law of the model; if the numerical solution does not respect it, the discretisation is wrong. *(Did not fire — but it forced a change of evaluation point, see below.)*
2. **The nonlinear solve failing to reproduce the linear closed form as the charges go to zero.** *(Did not fire — agreement to 2 % at amplitude 1e−3, and the departure is first order in the amplitude.)*
3. **The 2:1 machinery failing to reproduce `κ/(π l_B q)` when the electrolyte is made symmetric.** That expression is `C-0005`'s, and if the new code could not recover it, the disagreement would be a bug and not a finding. *(Did not fire — exact.)*
4. **`σ_eff` from the asymmetric solve coming out *above* the symmetric ceiling.** `C-0005` claims the saturation is generic and only the prefactor is in doubt; a value above the ceiling would mean the generic claim was wrong too. *(Did not fire — exactly `6 − 3√3 = 0.804` of it, for the tile.)*
5. **The force decay length coming out at the 0.84–1.18 nm counterion length.** That is `CH-0004`'s own proposal, and had it been right the challenge would have been upheld in full. *(Fired against `CH-0004`: `ℓ = 1.5–3.9 nm`, two to three times longer.)*
6. **100 pN turning out to need a bias where nothing is trustworthy.** If the force target sat only above the point-ion boundary, the answer would have had to be "unknown", not "reachable". *(Did not fire — and this is the finding that matters most.)*

### One thing the plan got wrong, recorded rather than quietly fixed

The pressure was to be read from the **contact-value theorem** at the tile, where the Maxwell term is exact from the Neumann condition and no derivative is taken. That is the best-conditioned route at small gaps and it is still emitted. At 30 nm it is the *worst*: the pressure is four orders of magnitude below the osmotic and Maxwell terms it is the difference of, so a 1e−6 relative error in the profile became a 4 % error in `F_es`, which is exactly the regime the decay-length measurement lives in. The fix is to evaluate the (constant) first integral at the node **minimising `|Π_osm| + |Maxwell|`**. Both routes are emitted and gate 3 asserts they agree; the convergence table shows the change took the 30 nm error from 3.9e−2 to 2.7e−5 at the same mesh.

---

## Execute

Code: `src/main/kotlin/electrostatics/` — new `AsymmetricGouyChapman.kt` (the 2:1 closed forms and the two cheap bounds), new `PoissonBoltzmannGap.kt` (medium profile, ion model incl. Bikerman, graded FV Newton solver, Stern series), new `NonlinearPbProfileStudy.kt` (the study). Reuses `Electrolyte.kt`, `ChargedSurface.kt`, `DnaOrigamiTile.kt`, `PolymerLayerPartitioning.kt`, `ConfinedGap.kt` unchanged.

Tests, written first and watched fail: `src/test/kotlin/electrostatics/AsymmetricGouyChapmanTest.kt` (18) and `PoissonBoltzmannGapTest.kt` (19). **The 73 existing electrostatics tests are untouched and still green.**

```shell
./gradlew test -PbuildDirectory=build-t3a
./gradlew study -Pstudy=electrostatics.NonlinearPbProfileStudyKt -PbuildDirectory=build-t3a
```

Result: [`../results/T-3a-nonlinear-pb-profile.json`](../results/T-3a-nonlinear-pb-profile.json) — 24 effective-charge points, 42 electrode points, 216 force state points (3 buffers × 12 gaps × 6 biases), 12 force thresholds, 54 Bikerman brackets, 81 layer points, 12 convergence points, 9 pull-in hand-offs. Every run parameter and every cited input logged in the file. Deterministic: no timestamp, and the whole tree rounded at the serialisation boundary per `structure/ResultRounding.kt`. **Verified by re-running and diffing: byte-identical.**

---

## Verify

All five gates, executed as tests. Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- The 2:1 first integral **vanishes at `y = 0` and equals `y²` to leading order** — which is what pins `κ² = 24π l_B c` rather than a monovalent `κ`. And it is asserted to be **not** even in `y`, so the asymmetry is visible rather than latent.
- The 2:1 Grahame relation and its inverse round-trip to 1e−9 over five decades of charge.
- `σ_eff` is asserted through the **identity** `κ A/(4π l_B)` rather than through its formula, so a transposed factor cannot hide.
- The **contact-value theorem** is reproduced from the two-plate solve at a large gap: `Σn_i(y_wall) − Σn_i(0) = 2π l_B σ²` to 1e−5. This ties `nm⁻³` of ion density to `(e/nm²)² × nm` of charge density and is the sharpest dimensional check available here.
- `k_BT/nm³ → pN/nm² → pN` over the footprint, asserted as exact algebra.

### Gate 2 — limiting cases

- **Debye-Hückel limit**: `σ_eff → σ` as `σ → 0`, and the approach is **first order in the charge** (error ratio 2.00 on halving), not merely "close".
- **Linear limit of the nonlinear solve**: the full BVP reproduces the closed-form linearised mixed-BC pressure to 2 % at amplitude 1e−3, and the departure is first order in the amplitude.
- **Bikerman → point ion** as the site density grows: agreement to `2e4/n_max`, i.e. the size-modified model is *exactly* the `n_max → ∞` limit of the same code rather than a second implementation.
- **Saturation**: the far-field amplitude saturates at `12 − 6√3` (negative wall) and exactly `6` (positive wall), approached monotonically from below, and the two differ by exactly `2 + √3`.
- **The symmetric `z:z` form is recovered** by the same `σ_eff` definition: `4/z·tanh(z y₀/4)` and hence `κ/(π l_B z)`, which is `C-0005`'s expression.
- **Zero bias is not a neutral wall**: a grounded conductor acquires induced countercharge and the interaction is non-zero. A constant-charge model gives exactly zero here, which is why the mixed problem had to be solved.
- **Both decay-length limits**: `λ_D/2` at zero bias (image interaction, `e^{−2κh}`) and `λ_D` under bias (`e^{−κh}`) — asserted against the *nonlinear* solve, not only against the linear closed form.
- Both surfaces uncharged ⟹ pressure and electrode charge vanish to 1e−14.

### Gate 3 — symmetry and conservation

- **The first integral is constant across the gap.** Three independent evaluations — the contact-value theorem at the tile, the midplane, and the best-conditioned interior node — agree to 1e−3, and the discrepancy falls by **exactly 4×** on mesh doubling, so it is discretisation and not model error. The full spread is reported alongside the *core* spread over the middle half of the gap, and the difference between them is itself the cancellation diagnostic.
- **Electroneutrality of the plate**: the closed-form profile's space charge, by Simpson plus the analytic tail, cancels the wall charge to 1e−6.
- **Electroneutrality of the gap**: `σ_electrode + σ_tile + ∫ρ dz = 0` to 1e−8, with `σ_electrode` from the half-cell flux balance and `∫ρ` from an independent quadrature.
- **Sign conservation**: `k_es < 0` at every working gap, asserted directly.
- **Charge inversion is impossible in mean field**: `sign(σ_eff) = sign(σ)` at every charge, and `|σ_eff| < |σ|` for every *negative* surface. (For a *positive* surface in 2:1 it is not — see below; that is physics, not a violation.)
- **The Donnan potential** of a thick uncharged partitioning layer comes out at `(1/3)ln(K₊/K₋)` to 1e−5, independently reproducing the stoichiometric geometric mean `C-0005` uses to combine partition coefficients.

### Gate 4 — numerical convergence

- **Second order in the mesh**, checked as an order across three meshes (ratios 4.0 and 4.0), not against a tolerance.
- The Newton correction is driven below 1e−11 in under 60 iterations at every bias, including `ψ_d = 0.235 V` (`y = 9.1`).
- **`k_es` is step-independent**: two differencing steps a factor of four apart agree to 1e−3, and the residual difference falls by the factor of 5 that `O(δ²)` predicts.
- The closed-form profile is verified **by substitution** into the 2:1 PB equation, with the residual checked as a **convergence order** (ratio 4.0), which would catch a formula that is numerically close but structurally wrong.
- The far-field amplitude is approached **exponentially**, at the rate `e^{−κΔz}` — asserted as a rate, not a tolerance.
- A full **mesh-convergence table** is emitted (6 meshes × 2 gaps), and every state point carries a `numericallyResolved` flag from its own core spread. **10 of 216 force points are flagged unresolved**; all ten are the `V = 0` column at gaps ≥ 12 nm, where `|F_es| < 4e−4 pN`.

### Gate 5 — literature and upstream cross-check

- **`C-0005`'s symmetric ceiling is reproduced exactly** — 0.0567557 e/nm² at 2 mM — through the same `σ_eff` definition applied to the symmetric amplitude. The disagreement with the asymmetric value is therefore a *finding*, not a discrepancy between two codes.
- **The solver and the analytics are independent implementations** of the same 2:1 problem — one a graded finite-volume Newton solve, the other a quadrature done by hand — and they agree node by node to 1e−3 in a gap wide enough that the electrode is out of range, and to 1e−6 on the tile's surface potential.
- **The 2:1 Debye length is the one `T-6` derived**, 3.92688 nm at 2 mM, asserted against `√(24π l_B c)`.
- **`C-0005`'s point-ion boundary is reproduced** — 0.19657 V of diffuse-layer drop for `Cl⁻` at 2 mM — and then *read correctly*, which is `CH-0007`.
- **The premise `C-0005` could not check is checked**: the `Mg²⁺` contact density at the tile, at the Manning-renormalised charge, is **below** close packing — so the point-ion assumption survives at the tile even though `C-0005` found it 1.75× past close packing at the *bare* duplex charge.
- **`C-0002`'s and `C-0005`'s layer numbers enter as inputs and their Donnan consistency is re-derived**, as above.

### Not verified, and stated as such

- **The direction of the correlation correction for oppositely charged walls is unknown.** Every published `Ξ` criterion is a like-charge result; `C-0005` says so and this task inherits it whole. This is the largest uncertainty on `F_es` and no closed form repairs it.
- **The lateral load profile is not computed and cannot be** by a 1-D treatment. See the hand-off below.
- **2 V is outside the aqueous electrochemical window.** Faradaic current, gas evolution and electrode corrosion are outside every model here. The 2 V column is reported because §3 asks for it, not because it is an operable point.
- **The Stern capacitance is cited, not derived**, and it is now *load-bearing*: it decides how much of an applied bias reaches the diffuse layer.
- **Nothing here is measured.** `PASS` means model-consistent and traceable.

---

## Result

Filed as [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md).
Raises [`CH-0007`](../challenges/CH-0007-point-ion-boundary-in-applied-bias.md) against `C-0005`'s comparison of its 0.197 V diffuse-layer boundary with §3's 2 V applied bias.
**Resolves** [`CH-0004`](../challenges/CH-0004-screening-decay-length.md): its ground 3 (direction and consequence) is upheld, its ground 1 (magnitude) is not, and its own "if this challenge is itself wrong" clause is the one that fires.

## Feedback into Formulate

- **`T-3` may now compute stroke.** It has `F_es(h, V)` and it must pair it with `T-1c`'s layer response, not `C-0001`'s.
- **`T-4` must not inherit `|k_es| ≈ F_es/λ_D`** — it is up to 2.6× low — and it now has `k_es` directly. It also inherits a warning: against `C-0001`'s stiffness, `|k_es|` reaches `k_brush` at 0.077–0.26 V. **`C-0003` landed mid-iteration and supersedes `C-0001`**; `T-4` should redo the comparison against its 0 – 13.8 pN/nm bracket, which moves the 10 nm cancellation bias to ≈ 0.13 V and changes no conclusion.
- **`T-5b` cannot be closed from here.** A 2-D solve of the tile edge is the task that would close it; the parameter this task *can* hand over is `d ln|P|/dh = −1/ℓ`.
- **`T-6b` is partly discharged**: the size-modified bracket is computed and it moves `|F_es|` by +0.8 % to +32 %. What remains of `T-6b` is the Stern capacitance itself.
- **A new task is warranted** on the electrochemical window: whether 1–2 V is applicable at all in aqueous `MgCl₂`.
