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
- **Semidilute.** Working volume fraction is φ ≈ 0.03–0.044, comfortably below the ~0.2–0.3 crossover,
  so the `m = 9/4` exponent is justified **for this layer at this operating point** — checked, not inherited.
  Correcting `a` to match PEG's true monomer volume raises φ by ~1.5×; the conclusion survives.
- **Mechanical only.** No electrostatics, no ion partitioning, no poroelasticity, no tile compliance.
- **Rigid tile.** Assumed, not shown. `T-5b` is what tests it, and if the tile dishes, the tile
  no longer samples a single `h` and this whole claim is a spatial average.
- **The SCF excluded volume is height-matched, not measured.** `w = π²a³/4 ≈ 2.47a³` is a calibration
  device that makes the two models comparable. A real PEG excluded volume `w = a³(1 − 2χ)` with `χ ≈ 0.45`
  is roughly 25× smaller. Task `P-3`.

## Numbers that are cited rather than derived

Flagged, per §7 of the problem definition:

- `a = 0.35 nm` (PEG monomer size) — **cited**. Implies a monomer volume ~35% below PEG's bulk-density value. Task `P-3`.
- The 10–16 nm height range for dense PEG 5 kDa brushes — **cited, and not yet traced to a specific source**. Task `P-3`.
- `Σ = 5` as the brush onset — **convention**.
- `χ ≈ 0.45` for PEG/water, used only in the sizing remark above — **cited**. Task `P-3`.

Everything else in this claim is derived from the §3 parameters.

## Challenges

None standing. A result contradicting this claim should be raised in `gpd/challenges/` with methodological
grounds rather than overwriting it.
