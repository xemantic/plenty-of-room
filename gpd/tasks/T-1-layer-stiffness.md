# T-1 — Stiffness of the polymer layer under the tile

| | |
|---|---|
| **Leaf** | `A2.1` (`../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 task 1; premises in §2; parameters in §3; questions §4(a), §4(b) |
| **Verification type** | in-silico (analytic derivation + numeric evaluation) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0001`](../claims/C-0001-layer-stiffness.md) |

---

## Formulate

### The question, as a numeric target

Given the §3 parameters, produce the stiffness `k` in pN/nm that the DNA-origami tile sees from the grafted polymer layer,
as a function of grafting density `σ` and layer height `L₀`,
with the model named, the parameters logged, and the validity range attached.

### Acceptance predicate

> Number with stated model, parameters, and validity range; sensitivity to grafting density reported.

Discharged when all four hold:

1. `k(σ, L₀)` is emitted for `L₀ ∈ {5, 7, 10} nm` across a grafting-density sweep spanning the mushroom boundary to antifouling grade;
2. each number carries the compression law that produced it, and the law's premises are stated;
3. `d ln k / d ln σ` is reported at every point;
4. the range over which the number is meaningful is stated **and enforced**, not merely documented.

### Units, locked

SI, scaled: lengths nm, forces pN, energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly), stiffness pN/nm (`= 1 mN/m` exactly).
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer**.
Energies also reported in eV via `1 eV = 160.2177 pN·nm`; `k_BT(300 K) = 25.85 meV`.

### Geometry and sign conventions, fixed before deriving

- `z` normal to the electrode, positive away from it, origin at the electrode surface (top of the dielectric if one is present).
- Chains grafted at `z = 0`; the layer occupies `0 < z < L`.
- The tile is a **rigid, non-adsorbing wall** at height `h`. Compression means `h < L₀`.
- The disjoining pressure `P` is positive when the layer pushes the tile along `+z`.
- Stiffness `k = −∂F/∂h = −A ∂P/∂h`, positive for a restoring layer.
- The layer height is the **independent** variable and the chain length `N` follows from it,
  because §3 specifies heights (5 / 7 / 10 nm) and leaves `σ` open.

### What is deliberately excluded

No electrostatics, no ion partitioning, no poroelasticity, no tile compliance.
This is the purely mechanical restoring term. Everything else is T-3, T-4, T-6, T-7, T-5b.

---

## Plan

### Method, and the justification against cost

**Chosen: closed-form scaling and SCF compression laws, evaluated numerically. Cost: seconds.**

The problem definition asks for the cheap bound before the expensive calculation, and here the cheap bound
is the whole of what is currently justified. The candidates were:

| Method | Cost | Why not first |
|---|---|---|
| **Analytic brush compression laws** | seconds | **chosen** |
| SCF numerics (Scheutjens–Fleer lattice) | hours | Buys a real density profile, but its answer is bounded by the same premises (mean field, Flory χ) whose values we do not yet have — task `P-3`. Calibrating it now would be calibrating to a guess. |
| Coarse-grained MD (Martini PEG + explicit Mg²⁺) | days–weeks of CPU | The only route that could settle ion partitioning (§4(c)) and the χ-under-tension question (§2) at once, but it cannot be interpreted before the analytic bound says where the interesting region is. |

The analytic route also does something the expensive routes cannot: it makes the *functional-form* ambiguity
that §2 complains about explicit and comparable, instead of burying it in one code's assumptions.

### Handling the two caveats §2 asks not to inherit

**Caveat 1 — brush against a rigid wall, not two opposing brushes.**
Resolved by the mirror-plane argument rather than by picking a prefactor from the literature.
An impenetrable wall at height `h` imposes exactly the boundary condition that the midplane between two
identical non-interpenetrating brushes imposes, so in

&nbsp;&nbsp;&nbsp;&nbsp;`P(D) = (k_BT/s³)[(2L₀/D)^(9/4) − (D/2L₀)^(3/4)]`

the substitution is `D → 2h`, and the factor of two **cancels out of both ratios**:

&nbsp;&nbsp;&nbsp;&nbsp;`P(h) = (k_BT/s³)[(L₀/h)^(9/4) − (h/L₀)^(3/4)]`.

The pressure is the same function *of the compression ratio*.
The circulating error consists in keeping the 2 while reinterpreting `D` as the wall distance,
which understates the pressure at a given compression by `2^(9/4) ≈ 4.76`.
Note the mapping is *cleaner* in our geometry than in the one it was derived for:
a rigid wall enforces zero interpenetration exactly, whereas real opposing brushes interdigitate.

**Caveat 2 — which osmotic exponent we are entitled to.**
Not resolved by choosing; resolved by carrying all of them.
`Π ∝ φ^m` is evaluated at `m = 9/4` (good-solvent semidilute, des Cloizeaux), `m = 2` (mean-field)
and `m = 3` (concentrated / theta), and the spread between them is reported as the uncertainty.
The mean volume fraction at the working point is emitted at every design point so that
*where the crossover sits for our layer* becomes a checkable number rather than an assumption.

**The third form.** The Milner–Witten–Cates SCF result is implemented alongside, not cited.
Derived here from the parabolic self-consistent potential `U(z) = A(L²−z²)`, `A = 3π²/(8N²a²)`,
truncated at a finite wall concentration, with the wall pressure from the mean-field contact-value
theorem `P = ½ w k_BT n(h)²`:

&nbsp;&nbsp;&nbsp;&nbsp;`n(z) = Γ[1/h + (h² − 3z²)/(2L₀³)]`, `Γ = Nσ` &nbsp;→&nbsp; `P(h) = ½ w k_BT (Γ/L₀)²(L₀/h − h²/L₀²)²`.

To make it comparable rather than merely different, its excluded volume is calibrated so that the two models
agree on the one thing both predict — the unperturbed height. That happens at `w = π²a³/4 ≈ 2.47 a³`,
**independent of `N` and `σ`**, so any residual difference in the compression curves is functional form,
not calibration.

### What would falsify this approach

Stated in advance:

1. **The working volume fraction landing above ~0.2–0.3.** Then the semidilute premise is gone,
   `m → 3` regardless of solvent quality, and the whole scaling family is being used outside its domain.
2. **The window landing in the mushroom or weak-crossover regime.** The Alexander–de Gennes picture assumes
   stretched, overlapping chains; if the answer only works at `Σ ≲ 5` it is an answer the method cannot support.
3. **The four models disagreeing qualitatively** — e.g. one saying a window exists and another saying it does not.
   Order-of-magnitude agreement with a factor-of-two spread is what this method is worth; more than that
   and the analytic route must yield to SCF numerics.

Outcome: (1) did not fire — see Verify. (2) fired at the *lower* edge of the window and is reported as such.
(3) did not fire.

---

## Execute

Code: `src/main/kotlin/brush/` — `PolymerBrush.kt`, `BrushCompression.kt`, `LayerDesignPoint.kt`, `BrushStiffnessStudy.kt`.
Tests, written first: `src/test/kotlin/` — 75 tests, all green.

```shell
./gradlew test
./gradlew study -Pstudy=brush.BrushStiffnessStudyKt
```

Result: [`../results/T-1-layer-stiffness.json`](../results/T-1-layer-stiffness.json) — 183 design points
(3 layer heights × 61 log-spaced grafting densities from 0.002 to 1.0 nm⁻²), 4 models each,
every parameter of the run logged in the file. Deterministic: no timestamp, so a re-run that changes nothing produces no diff.

---

## Verify

All five gates, executed as tests rather than asserted in prose.
Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- The scaling stiffness at equilibrium reduces to the closed form `(m + n)k_BT/(s³L₀) = 3 k_BT σ^(3/2)/L₀`,
  asserted against a formula arrived at differently from the implementation's derivative.
- Pressure × area reduces to a load in pN; stiffness per area × area reduces to pN/nm.
- The composite scaling `k/A ∝ σ^(7/6)` — *not* `σ^(3/2)`, because `L₀` itself carries `σ^(1/3)` —
  is confirmed by doubling the grafting density.

### Gate 2 — limiting cases

- Every model exerts exactly zero pressure at its own equilibrium height.
- Every model pushes the tile away, and stiffens monotonically, all the way down to `h/L₀ = 0.1`.
- **A qualitative divergence, and a finding rather than a check:** the scaling form opens with *finite*
  stiffness at first contact, the SCF form with *none*. The SCF pressure vanishes quadratically at `L₀`
  because the brush's outer edge is diffuse. So "the stiffness of the polymer layer" is not a well-posed
  single number at the resting height — it is only well-posed at a stated compression. Reported accordingly.

### Gate 3 — symmetry and conservation

- The SCF density profile conserves the grafted coverage `Γ = Nσ` at every compression,
  by Simpson quadrature (exact for the quadratic profile), to 1e-10 relative.
- Equipartition `σ = sqrt(k_BT/k)` is reachable from the same stiffness the actuation calculation uses,
  which is the hand-off to `T-8`.

### Gate 4 — numerical convergence

- Every model's analytic stiffness matches a central difference of its own pressure to 1e-7 relative,
  at three compressions.
- The central-difference error falls **quadratically** with the step, confirming the derivative is the
  right one and not merely a plausible one.
- The working-point solver is bisection, chosen because the pressure is strictly decreasing in `h` for
  every model here, so it is unconditionally convergent and needs no derivative; 100 halvings drive the
  bracket to machine precision. Newton would be faster and would risk stepping outside the validity range
  near `L₀`, where the pressure is flat.

### Gate 5 — literature cross-check, with premises checked against the material

- **The exponent is inherited by the code, not asserted:** halving the height multiplies the pressure by
  `2^m` for each `m`, because `φ ∝ 1/h` at fixed coverage. Confirmed for `m = 9/4, 2, 3`.
- **The SCF form is mean-field by construction** and approaches `m = 2` under strong compression regardless
  of what the scaling form is asked to use. Confirmed.
- **Height matching** at `w = π²a³/4` is confirmed independent of `N` and `σ`.
- **The `A1.1` bounds are reproduced** from our own equipartition helper:
  `σ = 3 nm → k ≥ 0.46 pN/nm`, `σ = 0.1 nm → k ≥ 414 pN/nm`.
  This is a cross-check against NDI's own V&V matrix, not against a textbook.
- **PEG height anchor, PROVISIONAL:** a dense PEG 5 kDa brush (`N ≈ 113`, `σ = 0.3 nm⁻²`) comes out at
  13.1 nm, inside the 10–16 nm range reported for antifouling-grade PEG layers. Flagged provisional:
  the anchor is a range recalled from the brush literature and not yet traced to a specific source,
  and the monomer size it is evaluated at is itself unsourced. That is task **`P-3`**, and it is the
  single largest open premise under this result.
- **The semidilute premise, checked against our own layer rather than assumed:** the mean volume fraction
  across the surviving window is **φ ≈ 0.02 at rest and 0.03–0.044 at the working point**. The
  semidilute→concentrated crossover is conventionally placed near φ ≈ 0.2–0.3, so at the operating point
  we **are** entitled to the semidilute exponent, with roughly a factor of five in reserve.
  The falsifier (1) did not fire.
  Caveat carried forward: `a = 0.35 nm` implies a monomer volume of 0.043 nm³, about 35% below the
  ~0.065 nm³ that PEG's bulk density gives for an ethylene-oxide unit. Correcting it raises every φ by
  roughly 1.5×, to ~0.05–0.07 — still semidilute, so the conclusion is robust, but the number is not final
  until `P-3`.

### Not verified, and stated as such

- The free-energy functional whose integral gives the Alexander–de Gennes blob count `(48/35)N/g`
  is an analytic consistency argument, **not an executed check**. Queued as `T-1b`; `T-4` needs it anyway.
- The `Σ ≥ 5` brush boundary is a **convention**, not a derivation. It sets the lower edge of the window,
  so the window's width is convention-dependent in a way the upper edge is not. Stated in the claim.
- Nothing here is measured. `PASS` means model-consistent and traceable.

---

## Result

Filed as [`C-0001`](../claims/C-0001-layer-stiffness.md).

## Feedback into Formulate

Two tightened predicates and two new blockers came out of this task:

- **`T-2`** inherits a concrete, sharp starting point: the window is empty at 5 and 7 nm and narrow at 10 nm,
  under the mechanical constraint alone, before §4(c) and §4(d) have had a chance to shrink it further.
- **`T-3`** must not assume 100 pN is available; T-1 only says what the layer does *if* it is.
- **`P-3`** (PEG material sheet) is now the binding premise, not a nicety — it moves φ by ~1.5× and `w` by ~25×.
- **`T-1b`** (free-energy functional) is promoted because `T-4` cannot start without it.
