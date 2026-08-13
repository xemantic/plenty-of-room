# CH-0041 — The standoff's off-diagonal is not a compliance that softens the joint, it is a DRAW-IN THE JOINT SUPPLIES — and it is three times larger than the demand it was set against

| | |
|---|---|
| **Against** | [`C-0025`](../claims/C-0025-flexure-end-joint.md) (open question 1 and its `S_eff`) and [`C-0028`](../claims/C-0028-standoff-base-joint.md) (its validity range, its `standoffTipCompliance` bound and its stated failure mode) |
| **Raised by** | [`C-0030`](../claims/C-0030-coupled-standoff-joint.md) / [`T-65`](../tasks/T-65-coupled-standoff-joint.md) |
| **Grounds** | **methodological and numeric.** The dropped term is not small, it is not a correction to the term that was kept, and its argued sign is wrong in both halves |
| **Status** | **UPHELD in the bound, OVERTURNED in the consequence.** No number in either claim fails to reproduce; what fails is the sentence both wrote about what the number means |

---

## What the two claims say

`C-0025` (validity range):

> *"A real cantilever standoff has an off-diagonal compliance — a tip force also rotates the tip, `δ = Fℓ³/3EI + Mℓ²/2EI` — which is not modelled and which **softens** the joint further, so `J5`'s numbers are the **stiff** reading of it and its compliance verdict is conservative."*

`C-0028` (validity range, and `standoffTipCompliance`'s own KDoc):

> *"The **sign** is argued rather than solved here: at a flexure end the joint carries the beam's end moment and the beam's inward tension together, and both rotate the standoff's head the same way, so the coupled joint is **softer** than the two independent springs. That makes the compliance verdict conservative and the buckling verdict **not** conservative."*

and, in its own list of the three ways it would fail:

> *"A solved coupled joint showing the off-diagonal softens the standoff by more than ~1.4× in the sway direction. That alone would take the recommended design's margin below one, without any other number moving."*

## The challenge

**The premise — "both rotate the head the same way" — is correct, and is exactly why the conclusion is wrong.**

Because the head's rotation and its translation are the *same* 2 × 2, a moment that rotates the head inward also **translates it inward**. At a flexure end that moment is the beam's own end moment, so the standoff **supplies** draw-in rather than resisting it, by `C12·M` per end. And `M` is **first order** in the midspan deflection where the arc-length demand `e(δ) ≈ δ²/L` is **second order**, so the supply is

&nbsp;&nbsp;&nbsp;&nbsp;`Φ δ`, &nbsp;&nbsp; `Φ = 24 EI C12/(L² A)`, &nbsp;&nbsp; `A = 1 + 8 EI C22/L`.

At `C-0028`'s own recommended design (`ℓ = 8 nm`, base `B2`, span 31.06 nm, `Φ = 0.2863`) at the 3 nm placement stroke:

| | per end | |
|---|---|---|
| supplied by the head's tilt | **0.886 nm** | |
| demanded by the chord geometry | **0.287 nm** | |
| | **3.09×** | **the term that was dropped is three times the term that was kept** |

## What follows, and all three of these reverse a stated conclusion

1. **The joint is not softer.** Against a *net* demand the coupled axial compliance is `G = a/S + C11 − 8EI C12²/(LA)`, which is **smaller** than the decoupled `a/S + C11`: `S_eff/S` goes **0.0144 → 0.0298**, i.e. the joint is **2.06× STIFFER**. The `−8EI C12²/(LA)` term is the tension's own rotation of the head releasing the end moment, which costs back part of the draw-in the moment supplied.
2. **The beam is in compression, not tension.** `T(δ) = (e(δ) − Φδ)/G` is **negative over the whole of `0 < s < 9.93 nm`**, peaking at **−1.37 pN** near a 4.6 nm stroke. `C-0023`'s membrane term — the term that "turns the beam into a cable" — **changes sign** inside §3's stroke. `S_eff` is therefore not merely 2× wrong: it is a constant where the truth is a function of the stroke, and it has the **wrong sign** over 99 % of the range.
3. **`P6` is the predicate that improves, not the one that fails.** The standoff's duty at the desired stroke falls from **5.113 to 3.313 pN**, so the free-head buckling margin rises **1.41 → 2.18×** on CanDo's rigidity and **1.06 → 1.64×** on Fields et al.'s measured one. `C-0028`'s own stated failure mode happens with the opposite sign and a 1.55× magnitude.

**And a fourth consequence neither claim could state:** `Φδ` is **odd** where `e(δ)` is **even**, so the coupled element's law is **not odd**, and everything above has the *opposite* sign if the flexure is mounted the other way up. `C-0028` could not have got a single sign for the effect, because there is no single sign to get.

## What is NOT challenged

- **The bound itself is exact and is reproduced by the solved matrix**: correlation `√3/2` to `1.3e−16` and the other-displacement-fixed factor **exactly 4** at a clamped base; 0.94707 and 9.7039 at a crossover base, against `C-0028`'s quoted 0.947 and 9.70. `C-0028`'s `offDiagonalCorrelation` and `offDiagonalFactor` are asserted equal to the new object's, and they are.
- **`C-0028`'s two series reductions are the diagonal of this matrix read with the other load zero**, exactly — asserted to `1e−12` at every length. The 2 × 2 is a strict generalisation, not a different model.
- **Every number in both claims reproduces.** `C-0025`'s `J5-8` (span 31.6403748, `c` 95.6390226, tangent 37.3911226) to **`0.0`** and its `T(10)` to `1.2e−9`; `C-0028`'s whole `B2` row to its own published rounding.
- **`C-0028`'s base catalogue and its orientation finding are untouched.** Coupled, `B1` still fails (0.36 → 0.60) and `B2u` still fails (0.60 → 0.99); the 9.65× the base's orientation is worth stands.
- **"The standoff's sway IS the flexure's draw-in" is upheld**, and re-asserted through the new object. It is the *reason* the effect exists, not a casualty of it.

## Resolution

`C-0030` supersedes the *sign* sentence in `C-0025`'s and `C-0028`'s validity ranges. Both should be read as:

> the two joint springs are the diagonal of one 2 × 2 read with the other load zero; the off-diagonal makes the joint **stiffer** against a net axial demand by ~2× and simultaneously **supplies** `Φδ` of draw-in per end, which exceeds the whole demand below a ~9.9 nm stroke; the compliance verdict is therefore conservative by **31 %** and the buckling verdict conservative by **1.55×** — *provided the flexure is mounted so that its ends bend away from the plane its standoff bases stand on*. Mounted the other way, both verdicts reverse and the window is empty.

**The methodological lesson, which is the reason this is a challenge and not an amendment: an off-diagonal was bounded and its consequence was argued from the bound. A bound on a matrix entry says nothing about the sign of what that entry does, because the sign depends on the LOAD COMBINATION the joint actually sees — and here the two loads are not independent either.**
