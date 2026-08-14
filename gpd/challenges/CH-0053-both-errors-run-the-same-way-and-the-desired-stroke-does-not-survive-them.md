# CH-0053 — Both errors run the SAME way, so the two compositions do not bracket the arm; and the exact one puts `E5`'s tangent at the desired stroke 6.6× past its own compliance ceiling

| | |
|---|---|
| **Raised by** | [`C-0039`](../claims/C-0039-two-spring-elastica.md) ([`T-79`](../tasks/T-79-two-spring-elastica.md)) |
| **Against** | [`C-0034`](../claims/C-0034-guided-arm-anchorage.md) — its **placement bracket** and its `E5a16` design table; and by inheritance [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md)'s `E5g` row *"reaches §3's desired stroke: **yes** … **PASS**"* and [`CH-0044`](CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md)'s *"what does NOT move: the arm still exceeds §3's desired 10 nm stroke"* |
| **Date** | 2026-08-14 |
| **Grounds** | **methodological** (a bracket asserted from two corrections that are not opposed) **and geometric** (a stroke that is three quarters of the arm's own contour) |
| **Severity** | **high — the design verdict moves, not only the number.** The arm is **12.7198 nm**, outside the 11.028–12.496 nm bracket. And at §3's **desired** 10 nm stroke the assembled tangent is **264.2 pN/nm against `C-0023`'s 40 pN/nm ceiling** and the **secant** is **69.94 pN/nm against the 33.3333 pN/nm mandate**, so `E5` does not deliver the desired stroke on **any** anchorage, hinge count or path count swept — **0 of 34 placements and 0 of 25 sensitivity points**. §3's **acceptable** 3 nm stroke is untouched and clears with 8.9 % of ceiling margin |

---

## The statements being challenged

`C-0034` reports its two readings as a bracket, and explains why they should be one:

> **Two errors run opposite ways and very nearly cancel.** The realised end condition is softer
> than asserted (6.28 against 12) *and* the composition it was solved with is the soft reading
> (0.607). … **Every reading clears §3's desired 10 nm stroke.**

and its design table records *"tangent … **36.78 pN/nm** at 10 nm — inside `C-0023`'s 40 pN/nm
ceiling at both, with **8.1 %** to spare at the desired stroke."*

## Ground 1 — the two errors are not opposed, and a bracket cannot be asserted from them

Both readings are corrections to the **same** object, the *linear two-spring beam*, and each adds
exactly one:

| | correction | direction | source |
|---|---|---|---|
| series → BVP | the far end carries part of the tip moment and **relieves the hinge** | **stiffens** | `CH-0044`'s own finding: the series composition retains 0.726 |
| linear → exact rotation | the arc shortens the effective span and the restoring lever falls as `cos θ` | **stiffens** | `CH-0040`'s own finding: `t/s` = 1.549 at 47° |

Two stiffening corrections applied to one baseline do not straddle it — their **sum** lies beyond
either. What made 11.028 < 12.496 look like a bracket is that they are *different* corrections to
a **common baseline neither of them reports**: series+linear, which is shorter than both.

The exact composition — a planar inextensible **elastica** with a rotational spring at each end —
lands where that argument says it must:

| reading | exact rotation | exact end condition | arm [nm] | bp |
|---|---|---|---|---|
| `C-0029` series, exact rotation | **yes** | no | 11.028 | 32.4 |
| `C-0029`'s `E5g16` at an asserted `c = 12` | yes | no | 12.242 | 36.0 |
| `C-0034` two-spring BVP, small deflection | no | **yes** | 12.496 | 36.8 |
| **`T-79` two-spring ELASTICA** | **yes** | **yes** | **12.7198** | **37.4** |

**1.79 % outside, on the long side.** That the solver is right is not asserted: its vanishing-load
limit reproduces `C-0034`'s own `c(ρ_n, ρ_f)` at **all four textbook corners and over a 25-point
interior grid to 1.7e−14**, which pins the field equation, both boundary conditions and every sign
at once.

## Ground 2 — and the 1.8 % is not what matters

At exact rotation the characteristic is not close to linear anywhere near §3's desired stroke,
because **the stroke is three quarters of the arm's own contour** — the placement condition caps
the arm at 13.65 nm, so `δ/L ≥ 0.73` at every design point in the catalogue.

| stroke | secant [pN/nm] | **tangent [pN/nm]** | `t/s` | against the 40 pN/nm ceiling | draw-in |
|---|---|---|---|---|---|
| **3 nm** (acceptable) | **33.3333** | **36.44** | 1.093 | **inside, 8.9 % to spare** | 0.383 nm = 1.1 bp |
| **3.877 nm** | ~34.5 | **40.00** | ~1.16 | **the usable limit** | ~0.65 nm |
| 6 nm | 38.91 | 56.71 | 1.458 | 1.4× past | 1.62 nm |
| **10 nm** (desired) | **69.94** | **264.24** | **3.778** | **6.6× past** | **5.34 nm = 15.7 bp** |

Three separate clauses fail at the desired stroke, and only one of them is the compliance ceiling:

1. **the SECANT is 69.94 pN/nm, 2.10× the 33.3333 mandate** — so the failure is in the *placement*
   quantity, not only in a second-order compliance remark. Delivering the desired stroke would take
   **699 pN**, seven times §3's own 100 pN;
2. **the per-path allowable**: the element's own tension is **15.54 pN** and its anchorage link
   force **24.31 pN**, both past `C-0006`'s 10 pN unzip allowable, where at 3 nm all three load
   paths are inside it. `CLAUDE.md`'s own rule — *a per-path allowable read at the acceptable
   stroke is not read at the desired one* — with the two readings on opposite sides;
3. **the draw-in**: 5.34 nm = **15.7 bp**, 42 % of the arm. `C-0029` quotes 0.095 nm at the
   acceptable stroke because `RotatingHingeArm` charges only the hinge's rigid swing and not the
   arm's bending; the exact value there is 0.383 nm, 4.0× larger. And the demand cannot be designed
   away: an inextensible arm whose ends hold their axial separation **cannot deflect at all**, so
   the held reading costs a strain — **≥ 299 pN of axial tension at the desired stroke**, 4.6× past
   the 65 pN nicked ceiling.

## And no design in the catalogue escapes it

**34 placements** (six anchorages × four hinge counts, plus the adopted anchorage at
`C-0040`'s buildable counts) and **25 sensitivity points** across `α`, `EI`, the phosphate radius,
the groove convention, the anchorage catalogue, the hinge count (`C-0040`/`CH-0054`) and the path
count (`C-0041`/`CH-0055`):

> **`clears the desired stroke inside the ceiling` is FALSE at every one of them.** The best usable
> stroke anywhere is **4.136 nm**, at `C-0029`'s own *asserted ideal guide* with 64 crossovers — a
> design `C-0034` already showed is not a motif. The desired stroke is **2.4× away at the limit of
> the catalogue**, not 1.2× away at the design point.
>
> At `C-0041`'s **15** paths the arm places at **8.40 nm** and the desired stroke is out of
> *geometric* reach entirely: a tip cannot rise past its arm.

## What does NOT move

- **`C-0034`'s continuum is confirmed, not overturned** — `c(ρ_n, ρ_f)` to 1.7e−14 by an
  independent nonlinear solver, which is a stronger check than any this challenge could ask for.
- **The counting theorem, the anchorage catalogue and the fixed-point cap** are untouched and are
  *used* here rather than restated; the cap moves **outward**, 13.428 → 13.648 nm, for the same
  reason the arm does.
- **`CH-0044`'s diagnosis stands in full**, and its 54.61 pN/nm over-placement is reproduced twice
  — from the closed form and, independently, from the elastica at vanishing load. What does not
  stand is its *"what does NOT move"* clause about the desired stroke.
- **§3's ACCEPTABLE 3 nm stroke is untouched.** `E5a16` places at 33.3333 pN/nm on the secant,
  holds 36.44 pN/nm on the tangent inside the ceiling, and keeps every load path inside the 10 pN
  unzip allowable. This challenge is about the *desired* stroke.
- Every filed number in `C-0034` and `C-0029` reproduces: 12 reproductions, worst departure
  **4.4e−5** against values their own claims quote rounded to five digits, **2.8e−9** otherwise.

## What would settle it

1. **A demonstration that the arm's far end is not rotationally restrained against a non-rotating
   second body.** Then the element is `C-0029`'s swinging lever — but its cap is `A1`'s 10.09 nm,
   which is also below the desired stroke once the placement condition is applied. The desired
   stroke fails either way; only the number changes.
2. **A crossover hinge strongly sublinear above ~10°.** That lowers the tangent at the desired
   stroke and *also* shortens the placed arm, raising `δ/L`; the net sign has to be solved, not
   argued.
3. **A compliance ceiling above 264 pN/nm at the desired stroke** — 7.9× the mandate. That is a
   change to the acceptance clause rather than to the mechanics, and it would have to be argued
   against `C-0018`'s pull-in analysis, which the stiffening moves.
