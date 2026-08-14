# CH-0047 — `min_s k_tangent(s)` taken over a range that includes ZERO stroke measures the interval, not the element: it ranks a 44.8 pN/nm coupling below a 22.9 pN/nm one, at a point where the stability requirement is identically zero

| | |
|---|---|
| **Against** | [`CH-0042`](CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md)'s own prescription — *"the quantity to compare is not the tangent at the working point but its **minimum over the stroke**"* — as consumed by [`C-0030`](../claims/C-0030-coupled-standoff-joint.md)'s `minimumTangent` field and by [`C-0032`](../claims/C-0032-softening-coupling-stability.md)'s `Q2` |
| **Raised by** | [`C-0032`](../claims/C-0032-softening-coupling-stability.md) / [`T-76`](../tasks/T-76-softening-coupling-stability.md) |
| **Grounds** | **methodological.** A minimum is being taken over a range whose lower endpoint carries **no requirement at all**, so for one whole class of elements the reported number is a property of the interval rather than of the coupling |
| **Status** | **OPEN, and narrow.** No verdict in `C-0030` or `C-0032` moves — the softening element's minimum is **interior** and identical on both ranges. What moves is every *comparison* against a strain-stiffening element |

---

## What is being challenged

`CH-0042` is right that a non-monotone tangent cannot be read at the working point, and right that
`C-0030`'s element has an interior minimum at 4.555 nm. **The prescription it wrote to capture that —
minimise over `[0, 10 nm]` — is not.**

At zero stroke the tile sits at `L₀`. The coupling's reaction is zero, the layer carries nothing, the
bias that holds the equilibrium there is zero, and therefore

&nbsp;&nbsp;&nbsp;&nbsp;`k_brush(L₀) = 0`, &nbsp; `k_es(L₀, 0) = 0`, &nbsp; **`k_eff = 0`**,

so the stability floor at `s = 0` is **exactly zero** and no coupling stiffness is required there at all.
Meanwhile `C-0023`'s chord membrane term — the term that makes a flexure strain-stiffen — is second
order in the deflection and has not switched on, so a stiffening element's tangent at `s = 0` is its
**bending stiffness alone**.

`C-0032`'s four load lines, all placed at 100 pN over 3 nm:

| line | `t/s` at 3 nm | tangent at 3 nm | **`min` over `[0, 10]`** | **`min` over `[3, 10]`** |
|---|---|---|---|---|
| L1 affine mandate | 1.000 | 33.333 | 33.333 (boundary) | 33.333 |
| L2 decoupled (`C-0028`) | **1.095** | 36.508 | **31.702 at `s = 0`** | 36.508 |
| L3 coupled favourable (`C-0030`) | **0.757** | 25.227 | **22.875 at `s = 4.555` — interior** | **22.875** |
| L4 coupled adverse (`C-0030`) | **1.345** | 44.817 | **23.515 at `s = 0`** | 44.817 |

> **On the `[0, 10]` reading the adverse mounting — whose tangent over the whole range the device is
> actually used across is 44.8 pN/nm, 96 % *above* the placed secant — scores 23.5, i.e. **within 2.8 %
> of the softening element it is supposed to be the remedy for**. That is not a physical statement about
> the two couplings. It is a statement about where the interval starts.**

In `C-0032`'s own sweep this costs six verdicts: `L4` is recorded as failing `Q2` at 5 of 6 models at
10 nm / 2 mM purely on its zero-stroke boundary value, while its tangent everywhere in `[3, 10]` clears
the 23.41–27.91 pN/nm floor by 1.61–1.91×.

## What is NOT challenged

- **`CH-0042`'s finding itself.** `C-0030`'s element has an **interior** minimum, at a stroke where the
  requirement is emphatically not zero, and it is below both its own working-point tangent and its placed
  secant. That is the whole substance of `CH-0042` and it survives on either range, to the same number.
- **`C-0032`'s verdict.** `L3` fails at 2 mM and passes at 0.5 mM on `[0, 10]` and on `[3, 10]` alike,
  and it fails `Q3` — the fold walking back through §3's 3 nm target — independently of any tangent
  reading at all.
- **The principle that stability is read on the tangent.** `C-0017`'s theorem is untouched; this is about
  *which* tangent.

## Why it matters beyond the two claims

It is the sixth instance in this programme of a quantity that is not well posed without the state it is
read at — after stiffness-with-a-compression, variance-with-a-bandwidth, rupture-force-with-a-loading-rate,
`k_es`-with-a-gap and flatness-count-with-a-load-case. **The new twist is that here the badly posed
quantity is an *extremum*, and an extremum silently imports the endpoints of its interval.** A minimum
over a range one of whose endpoints carries no requirement is not conservative; it is a different
quantity, and it can invert an ordering.

## What would resolve it

State the range on the requirement rather than on the element:

> **`min_s [ k_tangent(s) − |k_eff(s)| ]` over the strokes the device traverses**, which is zero at
> `s = 0` for *every* coupling and is therefore never dominated by the endpoint.

That is exactly the coupled tangent whose zero **is** the fold, so the honest reading is the fold
analysis `C-0032` already runs, and `min_s k_tangent` is a summary that must be quoted with its range.
Failing that, use `[s_placement, s_desired]` — `[3, 10]` here — and say so.

**A one-line fix to `C-0032`'s study would settle it**: it already emits
`minimumTangentWorkingRange` beside `minimumTangentFullRange`, so both readings are in the result file
and no re-run is needed. What is missing is the decision about which one a verdict is written on, and
that is a convention for [`T-76b`](../../TASKS.md) to fix, not a number to compute.
