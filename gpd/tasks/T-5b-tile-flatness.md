# T-5b — Deflected shape of the tile under the actuation load: does it stay flat?

| | |
|---|---|
| **Leaf** | `A8.2` (`../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 task 5b; question §4(g); parameters §3; geometry §1 |
| **Verification type** | in-silico (analytic plate mechanics + a Rayleigh-Ritz plate solve written for this task) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) |
| **Paired with** | [`T-5`](T-5-load-distribution.md) — same structural model, one iteration |

---

## Formulate

### The question, as a numeric target

Given the §3 parameters, produce the **deformation amplitude** of the tile in nm under the
actuation load, compare it against the stroke (≥ 3 nm acceptable, ~10 nm desired),
and either uphold or reject the rigid-plate assumption that `C-0001` makes and names this task
as the test of. Then state the consequences for (i) force transfer to the lever and (ii) what an
adjacent charge sensor would see.

### Acceptance predicate

Leaf `A8.2`, as specialised by §6 task 5b:

> Deformation amplitude reported against the stroke; rigid-plate assumption upheld or rejected,
> with consequences for force transfer to the lever and for what an adjacent charge sensor would see.

Leaf `A8.2`'s own acceptance string adds two requirements this task adopts verbatim:

> Lowest relevant mode stiffness consistent with A1 stiffness bound; no floppy modes in workspace.
> **Identify the dominant compliance term (joint compliance, not lever length) and budget
> stiffness at the joints.**

Discharged when all six hold:

1. both principal flexural rigidities of the tile are **derived** from duplex elasticity and
   sheet geometry, not asserted, with the areal duplex density and crossover spacing stated
   and sourced;
2. the dominant compliance term is identified, with the competing terms quantified against it;
3. the deformation amplitude is reported against the stroke, for each named source of
   non-uniformity separately, so that the *source* of any dishing is named and not just its size;
4. the verdict on the rigid-plate assumption is stated as a function of `k_f` — including
   **the value of `k_f` at which it flips** — rather than at one stiffness;
5. the thermal (unloaded, 300 K) dishing amplitude is reported alongside the loaded one,
   because a mode that is soft under load is soft under `k_BT` as well, and `A8.2` asks about
   floppy modes in the workspace;
6. the two §4(g) consequences are quantified, or stated plainly to be unanswerable here.

### Units, locked

As in [`T-5`](T-5-load-distribution.md#units-locked). Unchanged.

### Geometry and sign conventions, fixed before deriving

As in [`T-5`](T-5-load-distribution.md#geometry-and-sign-conventions-fixed-before-deriving),
plus the decomposition this task turns on:

- The deflection is split as `w = w_rigid + w_dish`, where `w_rigid = a₀ + a₁x + a₂y` is the
  **area-averaged least-squares best-fit plane** — exactly what a rigid tile is allowed to do —
  and `w_dish` is everything else. The split is exact and needs no fitting, because the Ritz
  basis is a tensor product of Legendre polynomials, orthogonal in the area inner product, so
  `w_rigid` is precisely its first three terms.
- **Dishing amplitude** means `‖w_dish‖`, reported both as an area root-mean-square and as a
  peak over the footprint. Both are given because they differ by a factor of ~2–3 and are
  routinely conflated.
- The **stroke** it is compared against is the mean deflection `⟨w⟩ = a₀`, which is what a
  rigid-plate model reports as *the* displacement.

### The three candidate sources of dishing, named in advance

A uniform load on a uniform foundation produces **no dishing at all** (see the shared Plan), so
dishing has to come from somewhere specific, and naming the source *is* the task:

| source | mechanism | treated as |
|---|---|---|
| **S-a discrete anchoring** | §4(g): the tile is "anchored at discrete points", so a distributed load is reacted in part at points | **primary** |
| **S-b load non-uniformity** | a finite charged tile at a gap comparable to `λ_D ≈ 4 nm` loses field off its rim, so `q` is not uniform | **bounded perturbation** |
| **S-c thermal internal modes** | the tile's own bending modes carry `k_BT` each at 300 K | reported separately; it is not a *deflection* but it is a *flatness* |

Which of S-a and S-b dominates is a deliverable, not an assumption.

### What is deliberately excluded

The electrostatic load is not solved here — only its total (100 pN, §3) and a bounded
non-uniformity. Solving it is `T-3`. In-plane (membrane) stiffening of the tile is neglected,
which is conservative — it can only stiffen the sheet.

**Poroelasticity is excluded with a citation rather than an assumption.**
[`C-0004`](../claims/C-0004-poroelastic-drainage.md) (`T-7`, closed concurrently) puts the
layer's drainage corner frequency at 91 kHz at this design point, so at the ≥ 1 kHz operating
point the foundation is **drained** and an elastic Winkler foundation is the right reduction.
An undrained layer would have been stiffer, shortening `ℓ` and making every verdict here worse.

---

## Plan

**Shared with [`T-5`](T-5-load-distribution.md#plan)** — same structural model, same cost
justification, same falsifiers. Two additions specific to this task:

### The load-non-uniformity bound is analytic, not numerical

For a sinusoidal load ripple of wavelength `λ` on a plate on a Winkler foundation,
substituting `w = ŵ sin(2πy/λ)` into `D w'''' + k_f w = q̂ sin(2πy/λ)` gives the exact
transfer function

&nbsp;&nbsp;&nbsp;&nbsp;`ŵ/(q̂/k_f) = 1 / [1 + (2πℓ/λ)⁴]`.

This turns "the electrostatic load is not perfectly uniform" into a bounded number without any
electrostatics at all, and it is what decides S-b: a non-uniformity confined to within a Debye
length of the rim is a **short**-wavelength feature and the plate low-passes it by `(2πℓ/λ)⁴`.
Running this before any solve is the cheap bound for `T-5b` specifically.

### The thermal amplitude comes from the same matrix as the loaded one

Equipartition on the Ritz functional: the coefficient covariance is `k_BT K⁻¹`, so
`⟨w_dish²⟩ = k_BT Σ_{i ∉ rigid} (K⁻¹)_ii / ((2m+1)(2n+1))`, and the piston mode reduces to
`k_BT/(k_f A)` exactly when the plate is rigid — which is `T-8`'s number and is wired in as a
gate-3 test. No sampling, no MD, no statistical power question: the fluctuation is exact for a
harmonic functional.

### What would falsify this approach, in addition to the shared list

6. **The dishing amplitude exceeding the layer height.** Then the tile has contacted the
   electrode somewhere and the linear Winkler foundation is gone; the answer would be a
   contact problem, not a plate problem, and would have to be reported as "outside the model".
7. **The thermal dishing exceeding the loaded dishing by more than an order of magnitude.**
   That would mean the deterministic question §4(g) asks is not the binding one, and the
   deliverable should be reframed as a noise budget — a `T-8` question, not a `T-5b` one.

---

## Execute

Code as in [`T-5`](T-5-load-distribution.md#execute).

```shell
./gradlew test
./gradlew study -Pstudy=structure.TileFlatnessStudyKt
```

Result: [`../results/T-5b-tile-flatness.json`](../results/T-5b-tile-flatness.json),
deterministic, every run parameter in the file.
5 foundation stiffnesses × 5 dishing sources, plus the thermal spectrum at three basis degrees.

## Verify

All five gates are carried in
[`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md#the-five-verification-gates).
The outcome of the two falsifiers specific to this task:

| falsifier | fired? | outcome |
|---|---|---|
| 6. dishing exceeding the layer height | **yes, for the concentrated case** | a single lever attachment dishes the tile by 18.3 nm against a 10 nm layer, so the tile contacts the electrode and the linear Winkler model has left its own domain. Reported as **outside the model**, and the case is dead on strength grounds anyway |
| 7. thermal dishing exceeding loaded dishing by > 10× | no | 1.27 nm thermal against 1.33–2.48 nm loaded — the same order, so both belong in the answer and neither reframes it |

### The predicate, item by item

1. both principal rigidities derived, with sourced areal duplex density and crossover spacing — **yes**;
2. dominant compliance term identified as **joint compliance**, 84% of the across-helix
   compliance in the crossovers, quantified against the 16% in duplex twist — **yes**;
3. deformation amplitude reported per source, so the source is named and not just the size — **yes**;
4. verdict stated as a function of `k_f`, with the flip point (`0.30 × C-0001`) — **yes**;
5. thermal amplitude reported alongside the loaded one, converged in the basis — **yes**;
6. both §4(g) consequences quantified — **yes**.

## Result

Filed as [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md).
**The rigid-plate assumption is REJECTED**, and
[`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md) is raised against `C-0001`
accordingly, as `C-0001`'s own validity bullet anticipated.

## Feedback into Formulate

- **`T-8` is re-scoped, not merely informed.** The tile's total point fluctuation at rest is
  1.37 nm nominal and 2.24 nm at the soft end of the `k_f` sweep, against `C-0001`'s 0.28 nm
  piston-only figure and `T-8`'s 3.0 nm predicate. Still inside, but at 46–75% of the threshold
  rather than 9%. `T-8` should consume `C-0006`, not `C-0001`.
- **`T-1c` should emit the stiffness at first contact as well as the secant**, because the
  thermal verdict here flips on the at-rest number and not on the secant.
- **A new predicate for the design, not for a task:** the output coupling to the lever must be
  distributed over ≳ 55 attachment points, which exceeds the 43.7 independent patches the tile
  contains. In other words, *there is no discrete attachment scheme that is flat*. That belongs
  in `T-2`'s design window as a constraint on topology rather than on any continuous variable.
