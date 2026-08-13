# T-65 — Does the standoff's off-diagonal compliance take the buckling margin below one?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Raised by** | [`C-0028`](../claims/C-0028-standoff-base-joint.md), open question 1; also `C-0025`'s open question 1 and `T-41` |
| **Priority** | **high** — `C-0028` bounds the term and *argues* its sign; the recommended design's buckling margin is **1.41×**, so a ~1.4× softening in sway alone closes the window with nothing else moving |
| **Verification type** | **in-silico** (a 2 × 2 tip flexibility solved into `C-0025`'s beam, with Maxwell-Betti asserted rather than constructed) **+ logical** (the sign of the coupling is fixed by which body carries the standoffs, which is a mounting choice and not a physical constant) |

---

## Formulate

### The question, stated so it can fail

`C-0025` gives the normal duplex standoff three constants — `k_θ = EI/ℓ`, `k_a = 3EI/ℓ³`, `k_⊥ = S/ℓ` —
and `C-0028` puts each in series with a base spring.
**Both claims then use the first two as two INDEPENDENT scalar springs at the flexure's end.**
They are not independent.
A cantilever's tip obeys

&nbsp;&nbsp;&nbsp;&nbsp;`δ = Fℓ³/3EI + Mℓ²/2EI`, &nbsp;&nbsp; `θ = Fℓ²/2EI + Mℓ/EI`,

so the correct object is a **2 × 2 tip flexibility matrix** whose off-diagonal `C-0028` bounds
(correlation exactly `√3/2` at a clamped base, 0.947 at a crossover base; the other-DOF-fixed
reading exceeds the other-load-zero one by exactly 4, and by 9.70) and whose *effect* it argues
rather than solves.

### Units, geometry and sign conventions, locked before deriving

- SI as always: lengths **nm**, forces **pN**, moments **pN·nm**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**; `k_BT = 4.141947 pN·nm` at **300 K**; aqueous **2 mM MgCl₂**.
- The standoff runs from a base on the sheet at `z = 0` to a head at `z = ℓ`, and **bends in the plane containing the flexure's axis and the sheet normal** — `C-0028`'s own statement, and the reason its sway and the flexure's draw-in are one coordinate.
- The head's two in-plane degrees of freedom are `(u, φ)`: `u` the head's translation along the beam axis, counted **positive inward** (toward the flexure's midspan), and `φ = du/dz` the head's tangent rotation, counted positive in the sense a positive `u` produces. `(u, φ)` are work-conjugate to `(H, M)` — an inward force and the moment the beam applies to the head.
- **`δ` is the signed midspan deflection of the flexure, positive when the midspan moves TOWARD the plane the standoff bases stand on** — i.e. positive `δ` is the beam **sagging over its own supports' ground**, which is the sense in which a sagging beam pulls its supports together, exactly as a cable does. In that sense the beam's end rotation tilts each standoff **inward**.
- A **stroke** `s > 0` is unsigned. The device deflects the flexure in one sense only, and which sense is a **mounting choice**: `orientation = +1` (**favourable**) if the stroke drives `δ = +s`, i.e. if the actuation **closes** the gap between the two bodies the coupling joins, `orientation = −1` (**adverse**) if it opens it. Both are reported; neither is assumed.
- `EI = 230 pN·nm²` is CanDo's **model input**; `EI = 172.9 pN·nm²` is the rigidity implied by Fields et al.'s **measured** duplex buckling. **Every buckling number is reported on both**, and which one is being quoted is stated.

### The acceptance predicates, declared before the run

Carried from `C-0028` unchanged so that the comparison means something, plus one the coupled
model makes visible for the first time.

| | predicate | threshold |
|---|---|---|
| **`P1`** | the base supports the standoff and the standoff supports the beam | `k_⊥ ≥ 10 ×` the beam's per-path stiffness, dead band `≤ 0.1 nm` |
| **`P2`** | the coupling is **placed** | assembled secant `= 33.3333 pN/nm` at the 3 nm acceptable stroke, to `1e−6` |
| **`P3`** | the coupling is **compliant** | assembled tangent at the working point `≤ 40 pN/nm` (`C-0023`'s ceiling) |
| **`P4`** | per-path forces are safe | beam axial tension `≤ 10 pN` unzip at the 10 nm desired stroke |
| **`P5`** | buildable | `ℓ ≤ 10 nm` (`C-0017`'s envelope), span `≤ 60 nm` |
| **`P6`** | the standoff does not buckle | `P_c ≥ ` the **element's own** end shear at the desired stroke (`CH-0037`), free-head reading |
| **`P7`** | **new** — the FLEXURE does not buckle | the beam's own axial **compression**, which only the coupled model produces, `≤` its braced Euler load at its realised end restraint |

### The numeric target

The deliverable is not a pass/fail but four numbers with validity ranges:
**by how much the coupled joint moves `c`, `S_eff`, the duty and the buckling margin**, at
`C-0028`'s recommended design (`ℓ = 8 nm`, base `B2`, 45 paths) and across its window `ℓ = 7–9 nm`,
on both rigidities and in both mounting orientations.

---

## Plan

### The cheap bound, which runs first

One division, before any root find and before any matrix.

The head's inward translation under the beam's end moment is `C12·M`, and `M` is **first order in
`δ`** while the draw-in demand `e(δ) ≈ δ²/L` is **second order**. Define the supply per unit
deflection

&nbsp;&nbsp;&nbsp;&nbsp;`Φ ≡ 24 EI C12/(L² A)`, &nbsp;&nbsp; `A ≡ 1 + 8 EI C22/L`,

and compare `Φ δ` against `e(δ)` at the 3 nm placement point of `C-0028`'s own design.

> **If `Φδ ≪ e(δ)` — say below 10 % — the off-diagonal is a correction, `C-0028`'s bound stands, and this task closes on a division.**
> If it is comparable or larger, the term the two upstream claims dropped is not a correction to the term they kept but **larger than it**, and the full solve is justified.

### The method, and its justification against cost

The full solve is **three linear equations and one root find per design point** — the same cost as
`C-0028`'s, because it re-uses `C-0025`'s pipeline with two coefficients replaced. Nothing more
expensive is warranted: the standoff is a slender uniform beam, its tip flexibility is exact in
closed form, and the uncertainty in the answer is dominated by `EI` (25 %), `k_s` (four decades)
and the large-deflection limit (the head moves 36 % of its own length at the desired stroke) — none
of which a finer beam model would touch. A finite-element standoff would be **less** trustworthy
than the closed form and no more informative, exactly as `P-3` argued for osmometry over MD.

The beam is solved from its two exact kinematic relations

&nbsp;&nbsp;&nbsp;&nbsp;`M = 24EIδ/L² − 8EIθ/L`, &nbsp;&nbsp; `P_b = 192EIδ/L³ − 48EIθ/L²`,

closed with the joint's `θ = C12 T + C22 M`, `u = C11 T + C12 M` and the axial compatibility
`e(δ) = T a/S + u`. Setting `C12 = 0` must return `c(ρ) = 192(ρ+2)/(ρ+8)` and
`S_eff = S/(1 + 2S/(k_a L))` **identically**, and that is the first gate.

### What would falsify this approach

1. **The coupled model failing to reproduce `C-0025` and `C-0028` at `C12 → 0`.** Then no comparison means anything and nothing else in the task may be quoted.
2. **`Φδ/e(δ) ≪ 1`.** Then the answer is `C-0028`'s bound and the iteration should be spent elsewhere.
3. **Maxwell-Betti failing** on the two independently integrated off-diagonals. The flexibility would not be a flexibility.
4. **The coupling being sign-indifferent.** If the two mounting orientations give the same verdict, the orientation is not a design variable and should not be reported as one.
5. **The coupled flexure's tangent going negative somewhere inside `0–10 nm`.** The element would not be a spring at all and the design would fail before any margin is quoted.
6. **The coupled joint softening sway by ≥ 1.4× at the recommended design.** That is `C-0028`'s own stated failure mode and it closes the window with no other number moving.

### The declared prediction

`C-0028` argues the coupled joint is **softer** and therefore that `P3` is conservative and `P6` is
not. The cheap bound says something stronger and different: because the supply is first order in
`δ` and the demand second order, **the sign of the whole effect must depend on the sense of `δ`**,
so a single "softer" cannot be right — one mounting must gain what the other loses. This is written
down before the code runs.
