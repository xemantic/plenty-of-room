# T-21 — The semidilute→concentrated crossover for *this* layer

Replaces the cited `0.2–0.3` band carried by [`C-0002`](../claims/C-0002-peg-material-parameters.md)
and made load-bearing by [`C-0018`](../claims/C-0018-maximum-usable-bias.md),
where `φ = 0.2` is the binding bias ceiling at **121 of 162** states.

| | |
|---|---|
| **Leaf** | `A2.1` (premise), consumed by `A2.2` |
| **Verification type** | **logical + in-silico** — a closed-form crossover family derived from `C-0002`'s own measured parameters, plus a re-ranking of `C-0018`'s 162 bias ceilings on the real Poisson-Boltzmann path |
| **Maturity target** | TRL 1–3. PASS means model-consistent and traceable; **nothing here is measured** |

---

## Formulate

### The acceptance predicate

> The `φ` at which the des Cloizeaux exponent stops being the one the layer is entitled to,
> **derived rather than read off a band.**

Split into four falsifiable parts:

- **`P1`** — a **derived** upper crossover for PEG in water at 300 K,
  computed from `C-0002`'s measured `v₀`, `b`, `n_K` and the measured excluded volume `v`,
  with **its definition named** and the other members of the family quoted beside it.
  Falsified if the number can only be produced by choosing a convention that is not stated.
- **`P2`** — the **premises checked against the actual material**.
  `CLAUDE.md`: PEG/water at 300 K is a *marginal* solvent and "blob arguments do not apply to Gen-1 chains at all".
  A blob-derived upper crossover must therefore be shown to be *applicable* before it is used.
  Falsified if the derivation silently assumes a swollen chain.
- **`P3`** — the **layer's** value, not the bulk solution's:
  a statement about whether a grafted layer inherits this crossover at all,
  argued on the same grounds `CH-0001`/`CH-0002` used to strip the *lower* crossover out of the brush pressure law.
- **`P4`** — the **propagation**: `C-0018`'s 162 ceilings re-read at the derived value,
  with the binding-ceiling census, the margins, and an explicit statement of whether
  `C-0018`'s window, `C-0016`/`C-0027`'s window edges or `C-0017`'s margin move.
  **If the derived crossover is lower than 0.2, that is worse for the device and is reported as such.**

### Two roles, and they are different questions

`C-0002` quotes `0.2–0.3` as a statement about **which scaling regime a solution is in**.
`C-0018` consumes it as a statement about **where the layer's constitutive law stops being supported**.
Those are not the same axis, and the task brief is explicit that the second may not be what the first intended.
Both are answered separately below, and the claim says which one each consumer needs.

### Units and conventions — restated, per `SESSION-PROMPT.md`

- Lengths **nm**, volumes **nm³**, forces **pN**, pressure **pN/nm² = 1 MPa exactly**, bias **V**, `k_BT = 4.142 pN·nm` at **300 K**.
- **A volume fraction in this project is always the physical one**, `φ = c v₀ = c_K v_K = N σ v₀ / h`.
  Sources writing `φ = n a³` quote a *reduced density*, **1.408×** smaller for PEG (`C-0002`).
  Sources writing `φ = n b³` quote a reduced density on the **Kuhn** segment,
  **`b³/v_K` = 7.09×** *larger* than the physical one — the aspect ratio `C-0002` derived and forbade substituting.
  **Every crossover below is quoted in the physical convention and the conversion is asserted as a test.**
- Excluded volume is a **pair** quantity: `v_K = n_K² v_m` (`CH-0020`), never `n_K v_m`.
- `σ` grafting density in nm⁻²; `s = L₀ − h` the stroke, positive downward; `L₀` a **force-onset** height (`CH-0010`).
- A crossover is a **convention until its definition is named** (`CLAUDE.md`, four times over: `Σ ≥ 5`, `L₀/R₀ ≥ 1`, the blob stack, the thermal-blob prefactor).
  Every number below is therefore emitted with the criterion that produced it.

---

## Plan

### The cheap bound, first — and it very nearly settles the task on its own

Before any correlation length is evaluated:

> **The des Cloizeaux regime requires a chain longer than a thermal blob.**
> `C-0002`'s Gen-1 chains are `N_K = 32–72` Kuhn segments;
> the measured excluded volume puts the thermal blob at `g_T = 126` (scaling) to `1160` (Yamakawa exact).
> `N_K < g_T` at every design point, in **both** conventions.

This is arithmetic on numbers the project already has, and it costs nothing.
If it holds, the semidilute *good-solvent* window is empty and the answer to `P1` is
"the exponent never starts", not "it stops at 0.2".
It is made exact in `P1` as the identity `φ*/φ** = √(g_T/N_K)`.

**What would falsify it:** an `N_K` above `g_T` at any design point, or a thermal-blob convention
in print that puts `g_T` below 72. Both are checked.

### The derivation (`P1`, `P2`)

The correlation length of a semidilute solution at physical volume fraction `φ`,
written on **Kuhn** segments because Gaussian statistics are not defined below that scale:

- blob is space filling: `n = φ ξ³ / v_K` segments per blob;
- **unswollen** blob (`n < g_T`): `ξ = b √n` ⟹ **`ξ_θ(φ) = v_K/(b² φ)`**;
- **swollen** blob (`n > g_T`): `ξ = b n^(3/5)(v/b³)^(1/5)` ⟹ `ξ_EV(φ) ∝ φ^(−3/4)`, the des Cloizeaux law.

Eliminating `ξ` gives the segments per blob in closed form,

&nbsp;&nbsp;&nbsp;&nbsp;**`n(φ) = (v_K / (b³ φ))²`**, &nbsp; and therefore &nbsp; **`φ_c(n) = (v_K/b³) · n^(−1/2)`**

— **one formula, and the entire family of upper crossovers is the choice of `n`**:
`n = g_T` is the excluded-volume→mean-field crossover, `n = 1` is the blob-picture→melt one.
That makes "a crossover is a convention until its definition is named" *executable* rather than hortatory.

Quoted beside them: `ξ = v₀^(1/3)` (correlation length at the monomer scale),
the **mis-coarse-grained** monomer-level `v_m/v₀` that `C-0007` reports and that the cited band coincides with,
and the fitted range of the measured equation of state.

**Why this method and not a fit.** The alternative — locating the crossover by where the
measured `φ^(9/4)` limb ceases to fit osmometry — cannot be done: the adopted equation of state
*is* a two-limb interpolation with 9/4 imposed, so it can only report where its own limbs cross,
which is the **lower** crossover `φ#` and is already `C-0002`'s. Re-fitting Rand's raw osmometry
with a free exponent is a data-acquisition task, not a modelling one, and the raw data are not in
this repository. The blob route uses only parameters this project has already measured and closed.

**What would falsify the blob route:** `P2`. If PEG/water were a *good* solvent at 300 K the
crossover would be at `φ ≈ v/b³` and the des Cloizeaux window would be wide; the marginal-solvent
check is what decides which. It is run first and its answer is allowed to invalidate the rest.

### The layer, not the solution (`P3`)

Argued, not computed: `φ*` carries `N` and is a *solution* property that grafting removes
(`CH-0001`/`CH-0002`); `φ_c(n)` contains **no `N` at all** — it is purely local, exactly as
`C-0019`'s Ginzburg parameter is — so the grafted layer inherits the upper crossover and does not
inherit the lower one. The one thing that could break the inheritance is chain stretching inside
the blob, so the stretch ratio `h/(N_K b)` is computed and reported.

### The propagation (`P4`)

`C-0018`'s ceiling is `min(pull-in, C-0005's 1.46 nm gap, the crossover gap, CH-0007's 1.0 V)`
read on the equilibrium path of a stated load line.
The crossover enters **only** through the gap `h_c = (N σ v₀)/φ_c` at which the layer reaches `φ_c`.

**The pull-in bias does not depend on `φ_c` at all**, so it is read from
`gpd/results/T-4-maximum-usable-bias.json` keyed on **every** dimension that sweep varied
(model, height, grafting density, buffer, load line — `CLAUDE.md`'s "more than one record per state"),
and flagged **CITED FROM `C-0018`**. Only the validity biases are recomputed, on the same
`EquilibriumPath` + `PoissonBoltzmannGap` pipeline C-0018 used — 162 states × the candidate
crossovers, ~1100 path evaluations, against ~8500 if the folds were re-located for nothing.
Gate 5 re-derives one fold and the `φ = 0.2` ceiling itself, so the cited half is checked rather than trusted.

### Verification gates

| gate | what is asserted |
|---|---|
| 1 dimensional | `ξ` in nm; `φ_c` dimensionless; `n(φ)` dimensionless; every conversion inverts |
| 2 limiting | the two correlation-length branches cross exactly where `n(φ) = g_T`; `n(φ_c(n)) = n` for all `n`; **a space-filling athermal segment (`v = v_K = b³`) returns `φ** = 1`, the textbook result** |
| 3 identity | **`φ*/φ** = √(g_T/N_K)` exactly** — the des Cloizeaux window is non-empty *iff* the chain exceeds a thermal blob; asserted to machine precision, not observed numerically |
| 4 convergence | the crossing located by bisection reproduces the closed form; the log-slope of `ξ` recovered by Richardson extrapolation returns −1 and −3/4 on the two branches |
| 5 literature | the physical↔reduced conversion reproduces `GraftedChi.correlationBlobSize` (written independently, in the **reduced** convention) exactly; the textbook athermal limit is recovered; the fit range of the adopted equation of state is read from the source, not remembered |

### Cost

The derivation is closed form (milliseconds). The propagation is the only expensive part and is
bounded to ~1100 Poisson-Boltzmann path evaluations by not re-locating folds that cannot move.

---

## Execute

`src/main/kotlin/crossover/ConcentratedCrossover.kt` — the physics.
`src/main/kotlin/crossover/ConcentratedCrossoverStudy.kt` — entry point `crossover.ConcentratedCrossoverStudyKt`,
emitting `gpd/results/T-21-concentrated-crossover.json`.
`src/test/kotlin/crossover/ConcentratedCrossoverTest.kt` — gate-named tests, written first.

---

## Verify

Filed as [`C-0036`](../claims/C-0036-concentrated-crossover.md). **PASS on `P1`–`P4`.**
Raises [`CH-0048`](../challenges/CH-0048-the-good-solvent-premise-was-checked-on-monomers.md) against `C-0007`
and [`CH-0049`](../challenges/CH-0049-the-cited-band-is-a-reduced-density-and-the-fit-range-is-wrong.md) against `C-0002`.

| gate | asserted | result |
|---|---|---|
| 1 dimensional | `ξ·φ` a material constant on the ideal branch; `n(φ)` the exact inverse of `φ_c(n)`; weight↔volume inverts; the two constructors agree | to **1e−12** |
| 2 limiting | the branches cross exactly where the blob holds `g_T` segments, and the crossing length **is** `ξ_T`; `φ_c(1) = 1/7.0918` with `ξ = b`; a space-filling athermal segment returns `φ** = 1` | exact; the athermal limit **0.0** |
| 3 identity | `φ**/φ* = √(N_K/g_T)`; `isEmpty ⟺ N_K < g_T`; the thermal blob equals `CH-0020`'s corrected count | **1.5e−16**, 1e−12 |
| 4 convergence | the crossing located by bisection on a **logarithmic** bracket (`P-15`) reproduces the closed form; both branches are exact power laws so a central difference in `ln φ` returns −1 and −3/4 at every step | **1e−9**, 1e−12 |
| 5 literature | R&C eq (5.36) `φ** = v/b³` reproduced in the **reduced** convention; R&C's printed athermal `φ** ≈ 1`; `C-0018`'s own `φ = 0.2` ceiling; `C-0002`'s aspect ratio; Hansen et al. (2003)'s measured onset converted out of its own convention | **0.0**, 0.0, **4.5e−9**, 2.5e−4 |

**The cheap bound was decisive, as planned**: `N_K = 21.6–120.4` against `g_T = 126–1160` settles
`P1` and `P2` before any correlation length is evaluated. What the expensive half added was the
*direction* of the propagation and the discovery that the ceiling **saturates** above `φ ≈ 0.4`.

**What did NOT go as planned.** The falsifier named in the Plan — *"an `N_K` above `g_T` at any
design point"* — nearly fired: at the 10 nm alexander-box(two-body) point `N_K/g_T = 0.953` in the
scaling normalisation, and with `C-0007`'s Flory-Huggins excluded volume instead of `C-0003`'s
`A₂` one it fires outright (`g_T = 19.5 < N_K`). The `2 × 2` corner sweep was added in response,
and the emptiness verdict is reported as **3 of 4 corners**, with the fourth defeated by a
different argument (the layer sits above the window's upper edge for its whole stroke) rather than
averaged away.

Test suite: `tools/verify.sh`. Study: `tools/study.sh crossover.ConcentratedCrossoverStudyKt` (~70 s).
