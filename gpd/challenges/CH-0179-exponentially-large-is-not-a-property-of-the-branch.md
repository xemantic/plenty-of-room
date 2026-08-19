# CH-0179 — *"Exponentially large"* is a statement at fixed asymmetry, not a property of the attractive branch, and the conservatism it is offered as is 6–15 % rather than orders of magnitude

| | |
|---|---|
| **Challenges** | [`C-0137`](../claims/C-0137-beyond-mean-field-gap.md) §`P4`'s use of Kanduč Eq. (65), and `electrostatics/BeyondMeanFieldGap.kt`'s KDoc on `weakCouplingValidityCoupling`: *"Their Eq. (65) is the **attractive** branch … and its right-hand side is 'exponentially large', so it is not implemented as a number: what it says is that the criterion below is the **conservative** one here."* |
| **Raised by** | [`C-0143`](../claims/C-0143-planar-coupling-wall.md), task [`T-221`](../tasks/T-221-planar-coupling-wall.md) |
| **Raised** | 2026-08-19, iteration 34 |
| **Status** | **Open. The conclusion survives at the wall `C-0143` selects and REVERSES at the wall `C-0137` floats.** No verdict of `C-0137` moves; one clause of its reasoning does, and the KDoc's second sentence is false at one of the eighteen readings, and it is the one `C-0137` would have quoted. |

---

## The statement being challenged

Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik (arXiv:0905.3851) give the weak-coupling validity criterion for an **attractive** mean-field pressure as Eq. (65),

&nbsp;&nbsp;&nbsp;&nbsp;`Ξ < (ζ²/|f(ζ)|) e^{−2ζD̃}`, &nbsp; `ζ < 0`,

and say in the next sentence: *"The right hand side here is exponentially large."* `C-0137` and the KDoc both take that as licence not to evaluate it, and conclude that Eq. (64) — the repulsive branch — is the conservative one to quote instead.

## The premise that is not in the quoted sentence

**The exponential is at fixed `ζ`.** Eq. (65)'s right-hand side is not large uniformly: it diverges at **both** ends of the admissible range — as `ζ → 0⁻` through the prefactor `ζ²/|f(ζ)|`, and as `ζ → −1⁻` because `f(ζ) → 0` there — and it therefore has an **infimum** in between.

And the range is not `(−1, 0)`. Eq. (65) is derived *"for `p₀ < 0`"*, and `p₀ < 0` does not hold at every `ζ` on that interval: the paper states that oppositely charged walls *"attract at large separations and a repulsion emerges only at sufficiently small separations"*, and prints the locus only as a figure. Derived from its own Eq. (18) in the `α → 0` limit — where `p̃₀ = α̃² → 0`, both sides are linear in `α`, and the equality fixes `2a/μ`:

&nbsp;&nbsp;&nbsp;&nbsp;**`D̃* = (1+ζ)/|ζ|`**, i.e. attraction ⟺ `|ζ|(1 + D̃) > 1`.

**The infimum of Eq. (65) over that domain is attained exactly at its boundary**, and it is a closed form, not a scan.

## Measured, over all six candidate walls and three gaps

| wall | `D̃` (7 nm) | `Ξ` | `inf` Eq. (65) | Eq. (64) | **ratio** |
|---|---|---|---|---|---|
| duplex cylinder, bare | 58.809 | 23.998 | 16.284 | 14.434 | **1.128** |
| single-helix layer, bare | 142.118 | 57.993 | 32.646 | 28.672 | **1.139** |
| Gauss-partitioned face, bare | 210.389 | 85.852 | 44.963 | 39.333 | **1.143** |
| Manning cylinder | 7.000 | 2.856 | 3.938 | 3.597 | **1.095** |
| **saturated face** | 3.565 | 1.455 | 2.890 | 2.805 | **1.030** |
| projected, bare | 420.778 | 171.703 | 80.136 | 69.641 | **1.151** |

Over all **18** readings (six walls × 5 / 7 / 10 nm) the ratio runs **`0.9447` to `1.1543`**.

## What is wrong, and what survives

1. **The exponential is not the operative quantity.** The two criteria agree to within a sixth at every reading — which is what continuity of the physics across `p₀ = 0` demands, and which `C-0137` could have had for the price of one closed form. *"Exponentially large"* describes the interior of the branch and not its boundary, and it is the boundary that governs a bound.
2. **The conservatism claim reverses.** *"The criterion below is the conservative one here"* is true at the bare readings, where Eq. (64) is `1.13–1.15×` tighter — and **false** at the small-`D̃` readings, where Eq. (65)'s infimum falls **below** Eq. (64)'s bound: `0.9447` at the saturated wall at the 5 nm gap, which is the only one of the 18 readings below unity — and it is the wall `C-0137` floats, at the shallowest gap §3 specifies. The KDoc's word *"here"* is doing work it cannot do, because *"here"* is a wall convention `C-0137` explicitly leaves unsettled.
3. **`C-0137`'s §`P4` conclusion survives.** Its verbatim quotation of the paper — *"for charged surfaces of opposite sign, the weak-coupling analysis performs far better at finite coupling parameters and smaller inter-surface separations"* — is upheld, and so is the Monte Carlo it rests on. What does not survive is the inference that the attractive branch removes the question: **the wall verdict is the same on both branches** when Eq. (65) is read at its own worst case (bare fails, renormalised passes), and it is only away from the branch boundary that the branch buys anything.
4. **What the branch does buy is measured rather than asserted.** Eq. (65) is satisfied for every `ζ` more negative than a threshold barely past the branch boundary; the excluded sliver is **0.4981 %** of the branch at the bare duplex wall, **0.2309 %** at the Gauss-partitioned face, and **exactly zero** at both renormalised readings, with a worst case of **1.1025 %** over all 18 readings. At `ζ = −0.5` every reading passes. That is the honest version of *"the attractive branch is the easy case"*, and it is a number.

## What it costs

No verdict. `C-0137`'s `P4` heading — *"does the `Mg²⁺`-does-not-condense bound transfer? **No.** And what replaces it is stronger"* — stands, and so does its closure of `C-0005`'s open item 4.

One sentence of `BeyondMeanFieldGap.kt`'s KDoc needs the qualifier, and `C-0143` supplies the numbers it should carry. **And one thing that was open is now closed**: `C-0137` says *"it is not implemented as a number"*; it is now, as `attractiveBranchValidityCoupling`, `attractiveBranchInfimumCoupling`, `meanFieldPressureSignChangeReducedGap` and `attractiveBranchAsymmetryThreshold`, with the `f(ζ)` branch continuity and the Eq. (18) boundary asserted as gates.

## What it leaves open

**The device's own `ζ` has never been computed** — the ratio of the electrode's charge to the tile's, at the operating bias. It decides which branch the gap is on, and at zero applied bias, where `C-0021`'s contact potential and gold's PZC set the electrode's sign and magnitude, it may put the gap back on the **repulsive** branch, where the bare reading fails. One Stern-series evaluation per state would settle it.
