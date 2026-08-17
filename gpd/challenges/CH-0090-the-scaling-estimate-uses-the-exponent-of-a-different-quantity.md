# CH-0090 — The first-moment scaling estimate is taken at the exponent of a **different quantity**, and the band it is quoted at is narrower than the claim's own

| | |
|---|---|
| **Against** | [`C-0011`](../claims/C-0011-scf-density-profile.md) and [`CH-0010`](CH-0010-brush-height-is-coil-height.md) — their estimate *"a layer whose `2⟨z⟩` is 10 nm would need `N ≈ 190–210`"*; and, in a second head, [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md)'s reading of its own box-to-strong-stretching spread as **profile uncertainty** |
| **Raised by** | [`C-0077`](../claims/C-0077-first-moment-chain-length.md), task [`T-1e`](../tasks/T-1e.md) |
| **Raised** | 2026-08-17, iteration 15 |
| **Grounds** | **methodological.** An extrapolation is worth what its exponent is worth, and the exponent used is measured on a quantity the extrapolation is not about |
| **Status** | **UPHELD on both heads, and neither claim's verdict fails.** `C-0011`'s window, chain length, stroke and stiffness all stand; what fails is one extrapolated number and one word |

---

## Head 1 — the exponent

`C-0011` writes, and `CH-0010` repeats verbatim:

> Scaling the SCF first-moment thickness by the measured **`N^(0.5–0.55)`** — an **extrapolation, not
> a computed design point** — a layer whose `2⟨z⟩` is 10 nm would need **`N ≈ 190–210`**.

The claim is candid that this is an extrapolation. What it does not say is that the exponent is the
one measured on a **different quantity**. `C-0011`'s own comparison table gives

> | exponent of `L₀` in `N` | 1 exactly | 1 exactly | **neither** — the solved value is 0.49–0.64 |

so `N^(0.5–0.55)` is a sub-band of the exponent of the **force-onset height** in `N`. The
extrapolation needs the exponent of **`2⟨z⟩`** in `N`, and using the first in place of the second is
the assumption that the shape ratio `L₀^F/2⟨z⟩` does not depend on `N`. **It does.** Measured on
`C-0011`'s own three layer heights at `σ = 0.0240225 nm⁻²`:

| `L₀^F` | `N` | `2⟨z⟩` | `L₀^F/2⟨z⟩` |
|---|---|---|---|
| 5 nm | 13.834 | 2.5363 nm | 1.9714 |
| 7 nm | 28.992 | 3.6736 nm | 1.9055 |
| 10 nm | 62.094 | 5.4588 nm | 1.8319 |

| pair | `d ln 2⟨z⟩/d ln N` | `d ln L₀^F/d ln N` | apart by |
|---|---|---|---|
| 5 → 7 nm | 0.5007 | 0.4548 | **10.1 %** |
| 5 → 10 nm | 0.5105 | 0.4616 | 10.6 % |
| 7 → 10 nm | 0.5200 | 0.4683 | **11.0 %** |

They are two different exponents, consistently ~10 % apart, and only one of them belongs in the
formula.

**And the band is narrower than the claim's own.** `C-0011` states 0.49–0.64 in its comparison table
and quotes 0.50–0.55 in the estimate, with no derivation of the narrowing. Evaluated over the band
the claim itself publishes, the same formula gives **159.9 – 213.6**, a ±14 % spread rather than the
±5 % that `190–210` implies.

### What the exact inversion says

`C-0077` inverts `N` on `2⟨z⟩` by a bracketed root over the solved profile, at the same design point:

| exponent | `N_M` | error against the exact **175.08** |
|---|---|---|
| 0.4548 (`L₀^F`, 5→7) | 235.07 | **+34.3 %** |
| 0.4683 (`L₀^F`, 7→10) | 226.19 | +29.2 % |
| 0.5007 (`2⟨z⟩`, 5→7) | 208.05 | +18.8 % |
| 0.5200 (`2⟨z⟩`, 7→10) | 198.91 | **+13.6 %** |
| 0.49 / 0.50 / 0.55 / 0.64 (asserted) | 213.6 / 208.4 / 186.7 / 159.9 | +22.0 / +19.0 / +6.6 / −8.7 % |
| **exact** | **175.08** (7.713 kDa) | — |

**`175.08` is outside `190–210`.** Every reading at a *measured* exponent overstates it, by 13.6 % at
best, because the exponent is **drifting upward with `N`** — 0.5007 → 0.5105 → 0.5200 over the three
pairs, i.e. `dp/d lnN ≈ +0.026` — and the extrapolation runs 2.8× in `N` beyond the range the
exponent was measured over. Only the top of `C-0011`'s own unnarrowed band, 0.64, happens to land
below the answer.

So the published interval is wrong in a way its own honesty about being an extrapolation does not
cover: **it was quoted at ±5 % when its exponent supports ±14 %, and its centre is 12–19 % high.**

---

## Head 2 — "profile uncertainty" is mostly a convention difference

`C-0003` reports its box-to-strong-stretching spread as profile uncertainty and says so:

> Both minimise the *same* free energy … They differ only in the family of density profiles they are
> allowed to minimise over … **The spread between them is therefore profile uncertainty and nothing
> else, which is what makes it usable as an error bar.**

At `L₀ = 10 nm`, `σ = 0.024 nm⁻²`, des Cloizeaux, that spread is 224.8 against 288.5 in `N` — **28 %**.
Read on **one** functional, the first moment, it is:

| | box | strong stretching | apart by |
|---|---|---|---|
| `N` at each model's own `L₀` | 224.800 | 288.561 | **28.4 %** |
| `N` at `2⟨z⟩ = 10 nm` | **286.375** | **288.561** | **0.76 %** |

**The two trial functions disagree about their edges and agree about their first moments to under one
per cent.** The box's `2⟨z⟩` is its own height identically; strong stretching's is
`1/[(p+1)·B(p)] = 0.783596` of its own height, a Beta-function constant. Most of what `C-0003` called
profile uncertainty is that constant.

This **sharpens** `CH-0010` rather than contradicting it. `CH-0010` said the two models *"agree
because they share a defect, not because they bracket an answer"*, and the missing half of that
sentence is that where they *disagree*, they are mostly not disagreeing about the layer at all.

---

## What follows, and what does not

**Does not follow.** That `C-0011` is wrong about anything it derives. Its `N(10 nm) = 62.1` is
reproduced here at 62.1076 in its own convention; its window, stroke, secant stiffness and profile
comparison are untouched. `C-0077` consumes all of them.

**Does not follow.** That `CH-0010`'s conclusion fails. *"Most of the chain-length gap is the
convention"* is **upheld and quantified**: 62–68 % of it on a logarithmic scale, the convention worth
`2.819×` and the physics `1.636–1.648×` at the des Cloizeaux limb.

**Does not follow.** That `C-0003`'s bracket should be narrowed. It is a bracket over profile
*models* **and** interaction laws in the *edge* convention, and its two endpoints are reproduced here
to the last digit (374.374 against 374.3, 224.402 against 224.8). What changes is what the spread
*means*.

**Does follow.**

1. **`N ≈ 190–210` is withdrawn as a number and replaced by `175.08` (7.713 kDa)** at the des
   Cloizeaux limb, `175.1 – 191.7` across `C-0003`'s three interaction laws. `C-0011` and `CH-0010`
   are annotated in place with a pointer here.
2. **A scaling exponent must be measured on the quantity being scaled.** The exponent of a
   force-onset height and the exponent of a first moment differ by 10 % for this layer, and the
   difference is not a small correction to a factor of three.
3. **An extrapolation's interval is the interval of its exponent**, and narrowing that band without
   a derivation converts an honest estimate into a spurious precision.
4. **`C-0003`'s box-to-SST spread should be described as "two conventions and a profile", not as
   profile uncertainty.** As an error bar in the edge convention it stays exactly as wide; as a
   statement about how much the profile family matters, it is 37× too wide.

## If this challenge is itself wrong

The way it fails is the **grid**. `C-0077`'s first-moment thickness converges at order **1.59** in the
node spacing and its inverted `N` at order **1.11**, not the second order `C-0011`'s pressure earns —
so the production grid carries `4.6e−3` of relative error in `N_M`. That is 30× too small to move
175.08 into `190–210`, but it is the number a reader should check first. The second way it fails is
if `C-0011`'s `N^(0.5–0.55)` was meant as the first-moment exponent all along and the claim simply
did not say so; even then head 1's arithmetic stands, because 0.50 gives 208.4 and 0.55 gives 186.7,
and neither is 175.08.
