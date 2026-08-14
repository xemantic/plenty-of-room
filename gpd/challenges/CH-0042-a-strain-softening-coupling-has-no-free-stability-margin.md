# CH-0042 — The coupled flexure is strain-SOFTENING and its law is no longer odd, so `C-0017`'s free stability margin becomes a 24 % debt and the stability condition is written on a number nobody has computed

| | |
|---|---|
| **Against** | [`C-0017`](../claims/C-0017-output-coupling-stiffness.md)'s tangent/secant theorem **as consumed by** [`C-0023`](../claims/C-0023-two-sided-coupling.md), [`C-0025`](../claims/C-0025-flexure-end-joint.md) and [`C-0028`](../claims/C-0028-standoff-base-joint.md) — and against the transfer of `C-0023`'s **oddness** through all three |
| **Raised by** | [`C-0030`](../claims/C-0030-coupled-standoff-joint.md) / [`T-65`](../tasks/T-65-coupled-standoff-joint.md) |
| **Grounds** | **methodological.** A theorem stated for a strain-*stiffening* element is being applied to one that is now strain-*softening*, and a two-sidedness argument written on an odd law is being inherited by a law that is not odd |
| **Status** | **UPHELD and RESOLVED** by [`C-0032`](../claims/C-0032-softening-coupling-stability.md) / [`T-76`](../tasks/T-76-softening-coupling-stability.md) (2026-08-14). **Both horns hold.** At 10 nm / 2 mM the softening line's bias margin collapses from `C-0018`'s 1.007–1.032 to **1.000–1.002** and the fold's own stroke walks back from 3.41–4.13 nm to **2.80–3.17 nm**, crossing §3's 3 nm target at two of six models; `min_s k_tangent` = 22.875 is below the 23.41–27.91 floor at 6 of 6. The **adverse mounting is 1.06–1.53× past `C-0023`'s ceiling at 0 of 8 standoff lengths.** What the two-horn framing did not contain: **0.5 mM clears every predicate at 1.44–5.93×**, and `ℓ = 5 nm` at 2 mM clears five of six. `C-0032` raises [`CH-0047`](CH-0047-a-tangent-minimum-over-zero-stroke-is-not-a-requirement.md) against this challenge's own `[0, 10]` reading of `min_s k_tangent` — **no verdict here moves** |

---

## What is being challenged

`C-0017`'s theorem, recorded in `CLAUDE.md` and consumed verbatim downstream:

> *"**Placement is written on the coupling's SECANT and stability on its TANGENT**, so a strain-stiffening element discharges both with one part and the whole `tangent/secant` ratio is free stability margin at zero placement cost."*

`C-0025` reads its own design through it — *"the 12 % of `t/s` above unity is free stability margin by `C-0017`'s theorem"* — and `C-0028` inherits the same reading at `t/s` = 1.095.

**The theorem is correct and its premise no longer holds.** Once the standoff's tip is one 2 × 2 rather than two springs, the flexure is **strain-softening** over §3's whole stroke:

| at `ℓ = 8 nm`, base `B2`, 45 paths | decoupled (`C-0028`) | **coupled** |
|---|---|---|
| assembled secant at the 3 nm placement point | 33.333 | 33.333 (by construction) |
| assembled **tangent** at 3 nm | **36.51** | **25.23** |
| `t/s` | **1.095** | **0.757** |
| assembled tangent **minimum** over 0–10 nm | 36.51 (monotone rising) | **22.88 pN/nm at a 4.55 nm stroke** |
| assembled secant at the 10 nm desired stroke | 46.01 | **29.81** |
| force delivered at the desired stroke | 460 pN | **298 pN** |

Across the recommended `ℓ = 5–10 nm` window the minimum assembled tangent is **21.4–27.3 pN/nm**, and it is below the placed secant at every length above 4 nm.

## Why it matters, in one line

`C-0017`'s stability condition is `k_c > |k_eff|`, written on the **tangent**. `C-0018` reports its fold margins as **19–42 % in `k_c/|k_eff|`** at the 33.333 pN/nm mandate — which places `|k_eff|` at roughly **23.5–28.0 pN/nm**. **The coupled element's tangent minimum, 22.88 pN/nm, sits inside that band.** So a design that discharges §3's placement clause exactly may no longer discharge its stability clause at all — and the quantity to compare is not the tangent at the working point but its **minimum over the stroke**, which is a number no claim in this programme has computed.

Two further consequences neither `C-0017` nor its consumers cover:

1. **`t/s < 1` is not merely "no free margin", it is a debt.** The excess of the secant over the tangent, **24.3 %** here, is stability margin the placement clause *consumes*. `C-0017`'s theorem has a sign and it has now flipped.
2. **The tangent is not monotone in the stroke**, so "the tangent at the working point" is not even the right reading. It has an interior minimum, at 4.55 nm — between §3's acceptable 3 nm and its desired 10 nm, i.e. **inside the operating range**.

## The second half — the law is no longer odd

`C-0023`'s zero-bias verdict, inherited unchanged by `C-0025` and `C-0028`, rests on the coupling being **two-sided** *and* on `F_req = k_req·σ` for a **quadratic** (symmetric) potential. Under coupling the supplied draw-in `Φδ` is **odd** while the demand `e(δ)` is **even**, so:

- the element is still **signed** — `carriesCompression` passes at every probe, asserted as a gate-3 test — so `T-13`'s topological argument survives;
- but the two limbs are **not mirror images**: `|R(−s)/R(s)|` = **1.88 at 3 nm and 3.9 at 10 nm**. The well is asymmetric.

**`T-13` still closes**, because the softer limb — the stroke limb — supplies **21.4–27.3 pN/nm assembled** against `C-0023`'s requirement of **0.4602 pN/nm**, i.e. **47–59× over** where `C-0023` had 72×. But it closes on an asymmetric well, and the sentence in `C-0023`, `C-0025` and `C-0028` that transfers the verdict says "quadratic".

## What is NOT challenged

- `C-0017`'s theorem itself, which is right for the element it was stated for.
- `C-0023`'s **sidedness** requirement or its currency identity `F_req = k_req·σ`. Both survive.
- `C-0023`'s 40 pN/nm compliance **ceiling**, which the coupled element clears with **31 %** to spare rather than 9 %.
- `T-13`'s closure, which is re-checked in `C-0030` rather than inherited, and which holds at 47–59×.

## What would resolve it

`T-76`: re-run `C-0018`'s pull-in and fold analysis with a coupling whose tangent is a **non-monotone function of the stroke with a minimum below its secant**, and report the fold margin on the minimum rather than on the working-point tangent. Two outcomes are possible and the difference is a design decision, not a modelling one:

- `|k_eff|` below 21.4 pN/nm everywhere in `C-0018`'s bias window → the design stands and the debt is affordable;
- `|k_eff|` above it anywhere → the placement stiffness must be raised above 33.333 pN/nm, which §3's own clause forbids, or the flexure must be given back some strain-stiffening — which, per `C-0030`, means mounting it the **adverse** way, and that fails `C-0023`'s compliance ceiling at every standoff length.

**If both horns hold, the branch is closed by a constraint pair that only exists once the joint is coupled** — and that, not the numbers above, is why this is filed as a challenge rather than a footnote.
