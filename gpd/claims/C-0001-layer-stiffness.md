# C-0001 — Stiffness of the grafted polymer layer under the Gen-1 tile

| | |
|---|---|
| **Task** | [`T-1`](../tasks/T-1-layer-stiffness.md) |
| **Leaf** | `A2.1` |
| **Verification type** | in-silico |
| **Verdict** | **PASS** — acceptance predicate discharged |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-1-layer-stiffness.json`, produced by `brush.BrushStiffnessStudyKt`, 75 tests green |
| **Conditions** | T = 300 K, aqueous buffer, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile (A = 1600 nm²); PEG, `a = 0.35 nm` |
| **Challenged** | **[`CH-0001`](../challenges/CH-0001-semidilute-premise.md) — UPHELD in part**, **[`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md) — UPHELD**, **[`CH-0003`](../challenges/CH-0003-blob-stack-height.md) — resolved by [`C-0003`](C-0003-crossover-valid-layer-response.md)**. See the banners below. |

> ⚠️ **This claim's validity range is corrected by [`CH-0001`](../challenges/CH-0001-semidilute-premise.md) (2026-08-12).**
> The numbers below are unchanged and reproducible, but the layer is **not** in the semidilute regime
> the `Semidilute` bullet claims: on the measured PEG/water equation of state it sits *inside* the
> dilute→semidilute crossover, ~~with a local osmotic exponent of 1.66–1.92 rather than 9/4.
> Every correction runs the same way — softer — so **the strokes below are lower bounds and the design
> window is a lower bound on its own width**. The `m = 3` model is excluded by measurement.~~
> Do not consume this claim without reading `CH-0001`.

> ⚠️ **The struck-through direction above is withdrawn by [`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md) (2026-08-12).**
> `1.66–1.92` is the local exponent of the **bulk** equation of state, and the term that drags it below 9/4
> is the translational entropy of whole chains, which a **grafted** layer does not have. The exponent of
> *this* layer's restoring pressure is **2.00–2.56**; what is excluded is `m < 2`, not `m = 9/4` or `m = 3`.
> Correcting the height relation as well raises `N(L₀)` by 5–88 % and runs **stiffer**, so the
> crossover-valid bracket **straddles** the numbers below rather than lying above them:
> at `L₀ = 10 nm`, `σ = 0.024 nm⁻²` the stroke is **3.83–6.01 nm** against the 4.95 nm tabulated here.
> **The strokes below are neither upper nor lower bounds. Carry the bracket in [`C-0003`](C-0003-crossover-valid-layer-response.md), not these numbers.**
> The one headline that survives every model is *"the ~10 nm desired stroke is unreachable at 100 pN"*.

> ⚠️ **The height relation itself is replaced by [`C-0003`](C-0003-crossover-valid-layer-response.md) (2026-08-12),**
> which closes [`CH-0003`](../challenges/CH-0003-blob-stack-height.md). `L₀ = N a^(5/3) σ^(1/3)` is a blob-stack
> result and the Gen-1 layer is not a blob stack: 1.47 blobs tall (`CH-0003`), 0.06 thermal blobs per chain,
> and `L₀/R₀ = 0.83–1.07` at the design points below. The convention's unity prefactor is worth an excluded
> volume of 81 Å³ against a **measured** 12.25 Å³ — a factor of 6.6.

---

## Claim

For a PEG layer grafted at density `σ` and standing `L₀` tall, compressed by a rigid 40 × 40 nm tile,
the stiffness at the unperturbed height is

&nbsp;&nbsp;&nbsp;&nbsp;**`k(L₀) = 3 A k_BT σ^(3/2) / L₀`** &nbsp;(scaling form, de Gennes exponents)

which for the §3 geometry evaluates to the table below.
The stiffness that governs the **stroke** is not this one but the secant `k_sec = F/(L₀ − h)`
over the approach to the working point, which is roughly 2–3× larger because the layer stiffens as it is squeezed.
Both are reported, because they answer different questions and are routinely conflated.

### The number, at the brush-regime boundary of each layer height

Evaluated with the good-solvent semidilute exponent `m = 9/4`, at a 100 pN load (§3 target force):

| `L₀` [nm] | `σ` at `Σ = 5` [nm⁻²] | `s` [nm] | `N` | `k(L₀)` [pN/nm] | stroke @ 100 pN [nm] | `k_sec` [pN/nm] | `σ_RMS` [nm] |
|---|---|---|---|---|---|---|---|
| 5 | 0.092 | 3.29 | 64 | 111.6 | 0.73 | 137.0 | 0.16 |
| 7 | 0.045 | 4.73 | 113 | 26.9 | 2.21 | 45.3 | 0.19 |
| 10 | 0.024 | 6.45 | 199 | 7.4 | 4.95 | 20.2 | 0.28 |

Full sweep — 3 heights × 61 grafting densities × 4 models — in the JSON.

### Sensitivity to grafting density

`d ln k_sec / d ln σ` at fixed layer height, across the surviving window: **0.55 – 0.90**.

Two things are worth separating here, because they differ and the difference is easy to get wrong:

- At the **unperturbed height**, `k ∝ σ^(7/6)` in closed form — *not* `σ^(3/2)`, because `L₀` itself carries `σ^(1/3)`.
- At the **working point** the exponent falls to ~0.6–0.9, because a denser layer is also compressed less by
  the same load, which partly offsets its higher modulus.

Either way the stiffness is **sub-quadratic** in grafting density: there is no sharp knee to design against,
and the window is bounded by the regime boundary and the stroke requirement rather than by any feature of `k(σ)`.

---

## The finding that matters more than the number

Under the mechanical constraint **alone** — brush regime (`Σ ≥ 5`) and ≥ 3 nm stroke at 100 pN —

| `L₀` | window in `σ` [nm⁻²] |
|---|---|
| 5 nm | **empty** |
| 7 nm | **empty** |
| 10 nm | **[0.024, 0.045]** — narrow, less than a factor of two wide |

and **the ~10 nm desired stroke is unreachable at 100 pN anywhere in the brush regime, at any of the three heights.**

This holds across all four models. Model by model, the 10 nm window is
`[0.024, 0.045]` (m = 9/4), `[0.024, 0.050]` (m = 2), `[0.024, 0.036]` (m = 3), `[0.024, 0.030]` (SCF) —
so the **model-robust intersection is `σ ∈ [0.024, 0.030] nm⁻²`, `s ≈ 5.8–6.5 nm`, `N ≈ 186–199`
(PEG ≈ 8–9 kDa)**, and the disagreement between models is a factor of ~1.5 in stroke, never in direction.

The binding constraint at the *lower* edge is the mushroom boundary — exactly the tension §4(a) names.
The binding constraint at the *upper* edge is the stroke.
Layer thickness, not grafting density, is what opens the window: it is empty at 5 and 7 nm and opens only at 10.
That is a direct answer to §4(b), and it says the reason to go outside the 5–10 nm range is **upward**.

This is the mechanical restoring term only. §4(c) porosity, §4(d) poroelasticity and the electrostatics of
§4(e) have not yet had a chance to shrink this window further, and none of them can widen it.

---

## Validity range

Respected downstream, and enforced in code rather than documented:

- **`0 < h ≤ L₀`.** Above `L₀` a non-adsorbing brush loses contact with the tile and the pressure is zero.
  The scaling form's negative branch there is an artefact of the interpolation; evaluating it throws
  rather than returning an unphysical attraction to a downstream task.
- **Brush regime.** Points reported `MUSHROOM` or `CROSSOVER` are outside the premise of the scaling form
  and are emitted to locate the boundary, not as answers. Note the `Σ ≥ 5` boundary is a **convention**,
  not a derivation: it sets the window's lower edge, so the window's *width* is convention-dependent
  in a way its upper edge is not. On the weaker `Σ > 1` criterion the 7 nm window would open and the
  10 nm window would roughly double. Anyone using this result must state which criterion they are using.
- ~~**Semidilute.** Working volume fraction is φ ≈ 0.03–0.044, comfortably below the ~0.2–0.3 crossover,
  so the `m = 9/4` exponent is justified **for this layer at this operating point** — checked, not inherited.
  Correcting `a` to match PEG's true monomer volume raises φ by ~1.5×; the conclusion survives.~~
  **WITHDRAWN by [`CH-0001`](../challenges/CH-0001-semidilute-premise.md).** Three faults: it checked the
  *upper* (semidilute→concentrated) crossover when the binding one is the *lower* (dilute→semidilute) one
  at φ# ≈ 0.026; the φ quoted is a reduced density `n a³`, 1.408× smaller than the physical volume fraction;
  and coil overlap is not a sufficient criterion for semidilute behaviour in this material. Replaced by
  [`C-0002`](C-0002-peg-material-parameters.md): the layer sits at φ/φ# = 1.08–1.23, in the crossover,
  with `m_eff` = 1.66–1.92.
- **Mechanical only.** No electrostatics, no ion partitioning, no poroelasticity, no tile compliance.
- ~~**Rigid tile.** Assumed, not shown. `T-5b` is what tests it, and if the tile dishes, the tile
  no longer samples a single `h` and this whole claim is a spatial average.~~
  **ANSWERED, AND AGAINST THE ASSUMPTION, by [`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md).**
  `T-5b` has run. The tile is a plate on an elastic foundation with `ℓ/L = 0.20–0.45`, so it is
  rigid only for a *uniform* load — where it is rigid **exactly**, whatever its rigidity, and
  these numbers therefore stand as computed. For every non-uniform case it dishes by 27–369% of
  the stroke, and at 300 K its shape modes fluctuate by 1.27 nm RMS, 1.70× the piston mode.
  Two consequences: the `σ_RMS` column above is the piston mode alone and is **4× smaller than
  the tile's actual point fluctuation** (`T-8` must not consume it as-is), and sampling a convex
  `Π(h)` at a distribution of heights stiffens the layer by **+3.6%**, opposite in sign to
  `CH-0001`. See [`C-0006`](C-0006-tile-load-distribution-and-flatness.md).
- **The SCF excluded volume is height-matched, not measured.** `w = π²a³/4 ≈ 2.47a³` is a calibration
  device that makes the two models comparable. A real PEG excluded volume `w = a³(1 − 2χ)` with `χ ≈ 0.45`
  is roughly 25× smaller. Task `P-3`.

## Numbers that are cited rather than derived

Flagged, per §7 of the problem definition:

- ~~`a = 0.35 nm` (PEG monomer size) — **cited**.~~ **CLOSED by [`C-0002`](C-0002-peg-material-parameters.md):**
  derived as the all-trans contour length (0.3639 nm, 4% away) and independently fitted (0.330–0.356 nm).
  It is a *contour* length, not a volumetric one — the monomer volume is `v₀ = 0.0604 nm³`, not `a³`.
- The 10–16 nm height range for dense PEG 5 kDa brushes — **cited, and not yet traced to a specific source**.
  Still untraced. `C-0002` recommends deleting it: nothing depends on it.
- ~~`Σ = 5` as the brush onset — **convention**.~~ **RESOLVED in substance by `C-0002`:** for PEG in water
  `Σ = 5` is exactly equivalent to `φ = 1.085 φ#`, independent of layer height — a real material statement,
  but one that places the layer at the crossover rather than in the semidilute regime.
- ~~`χ ≈ 0.45` for PEG/water, used only in the sizing remark above — **cited**.~~ **Superseded:** the sizing
  remark it supported ("roughly 25× smaller") is wrong by 5.7×; the measurement-consistent excluded volume
  is 0.0244 nm³, 4.3× below the height-matched one (`C-0002`). `χ` itself remains undetermined — task `P-6`.

Everything else in this claim is derived from the §3 parameters.

## Challenges

**[`CH-0001`](../challenges/CH-0001-semidilute-premise.md) — UPHELD in part, 2026-08-12.**
The semidilute premise fails; the claim is not withdrawn, ~~its numbers become bounds, and the
`m = 3` model is excluded by measurement~~ (**both withdrawn by `CH-0002`** — see below). `T-1c` is queued to
re-derive the layer response with a crossover-valid free energy, which is what the challenge shows cannot be
repaired by changing an exponent.

**[`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md) — UPHELD, 2026-08-12, raised by [`C-0003`](C-0003-crossover-valid-layer-response.md).**
The corrections do **not** all run the same way. `CH-0001` carried the *bulk* local exponent into the brush
pressure law; a grafted layer has no chain translational entropy, so its own exponent is 2.00–2.56 and `m < 2`
is what is excluded. Correcting the height relation raises `N(L₀)` by 5–88 % and runs stiffer, so the
crossover-valid bracket straddles the table above. **The strokes here are not lower bounds and the window is
not a lower bound on its own width.** Superseded by `C-0003` for every consumer.

**[`CH-0003`](../challenges/CH-0003-blob-stack-height.md) — raised 2026-08-12 by [`C-0004`](C-0004-poroelastic-drainage.md).**
The geometric twin of `CH-0001`: the Alexander-de Gennes layer is `(Σ/π)^(5/6)` blobs tall, i.e. **1.47 at
the `Σ = 5` convention** and 1.48–1.73 at every surviving design point, so the blob-stack picture that
underwrites `L₀ = N a^(5/3) σ^(1/3)` is being applied to a stack of one and a half blobs. It also puts
strong-stretching theory outside its own premise here, at `L₀/R_F = 1.17–1.25`. `T-1c` is where it is settled.

**[`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md) — UPHELD, 2026-08-12.**
The `Rigid tile` bullet is answered against the assumption. The numbers are not withdrawn — they
are exact for the uniform load case they model — but the scope over which they mean anything is
narrower than stated, and the `σ_RMS` column is a one-degree-of-freedom result for a structure
with many.

A further result contradicting this claim should likewise be raised in `gpd/challenges/` with
methodological grounds rather than overwriting it.
