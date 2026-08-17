# CH-0035 — The edge correction cannot reach the window edge, and at a pinned operating point it helps stability rather than hurting it

| | |
|---|---|
| **Against** | [`CH-0026`](CH-0026-forces-are-footprint-integrated-one-dimensional-pressures.md) — its two *directional* statements, not its measurement |
| **Raised by** | [`C-0027`](../claims/C-0027-window-resynthesis.md) (`T-25`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a one-signed correction propagated to two clauses by the sign of the force rather than by which quantity each clause is a function of |
| **Direction** | **favourable, twice.** One propagation does not exist; the other has the opposite sign |
| **Status** | raised. **No number in `C-0022` or `CH-0026` moves**, and the +14.7 % is reproduced from `T-3b`'s own file. What is challenged is the two-line consequence table |

---

## What is challenged

`CH-0026` measures the finite-tile edge enhancement — the tile behaves electrostatically as one 1.65 nm larger
on every side, worth **+4.9 % to +19.2 %** of total force across the §3 box — and then states its consequences:

> - **`C-0016`'s upper window edge — the 3 nm stroke at 100 pN — moves outward**, because more force at the
>   same bias is more stroke.
> - **`C-0012`'s `k_eff < 0` problem gets worse**, because `k_es` carries the same multiplier and it is the
>   negative term. So does `C-0017`'s stability floor, and `C-0018`'s usable bias ceiling comes down.

and concludes that the direction is *"the **favourable** one for every force clause and the **unfavourable**
one for every stability clause — which is exactly the combination in which an error survives longest."*

**The measurement is not disputed. Both consequences are.**

---

## Ground 1 — `C-0016`'s upper edge is a polymer clause and there is no field in it

`C-0016`'s upper window edge is `a-compliance-stroke`, and its own provenance line reads:

> `C-0011` — **stroke ≥ 3.0 nm under a 100 pN dead load over the tile**

A **dead load** is a specified force. It is not produced by a bias, it does not depend on the tile's
capacitance, and it does not change when the tile turns out to be electrostatically 1.65 nm larger on every
side. The quantity the edge is read on is `strokeUnderTargetForce`, which `T-1d` computes from the layer's own
disjoining pressure against 100 pN.

**`CH-0026`'s multiplier is not an argument of that function.** `C-0027` asserts this as a test: running the
whole intersection with the edge enhancement switched on and everything else at identity moves **no index at
any of the three heights**.

The step `CH-0026` skipped is *"more force at the same bias is more stroke"* → *"so the upper edge moves"*.
The first half is true and is a statement about §6 task 3's *bias* clause, which `C-0012` owns. The second
half requires the window's stroke clause to be **a function of the bias**, and it is not — `C-0016` fixes the
force at §3's 100 pN precisely so that the edge is a property of the layer.

---

## Ground 2 — at a pinned operating point the multiplier cancels, and the surviving term runs the other way

`C-0017` defines `k_es = |F_es| d ln|F_es|/dh` and `ℓ = −1/(d ln|F_es|/dh)`, so

&nbsp;&nbsp;&nbsp;&nbsp;**`k_es = −|F_es|/ℓ`, identically.**

And the operating point is defined by a **force balance**: `|F_es| = 100 pN + P(g)·A`. That is mechanics.
`T-16`'s own file confirms it — `electrostaticForceAtTarget` is *identical across 0.5, 1 and 2 mM* at every
`(model, height)`, to the file's 9-digit rounding, because the buffer changes only the bias needed to produce
it. `C-0027` asserts this as a gate-3 test.

> **So a multiplier `μ(h)` on the LEVEL of `|F_es|` is absorbed entirely into the bias and reaches `k_es` not
> at all.** It is not small; it is exactly zero at the operating point.

What survives is the *gradient* of the collar:

&nbsp;&nbsp;&nbsp;&nbsp;`1/ℓ_2D = 1/ℓ_1D − d ln μ/dh`

and `μ` **rises with the gap** — `T-3b`'s own numbers at 2 mM give 0.961 at 2 nm, 1.036 at 4, 1.056 at 5,
1.105 at 7 and 1.150 at 10, so `d ln μ/dh = 0.0133 – 0.0226 nm⁻¹` at the 7 nm held gap over three difference
schemes. A more slowly decaying attraction has a **smaller** `|k_es|`. The finite-tile correction therefore
**stiffens** the actuator at its operating point:

| 10 nm, 2 mM, six models | stability margin `33.333/\|k_eff\|` |
|---|---|
| `C-0017` as published | 1.194 – 1.424× |
| **with `CH-0026` carried at the operating point** | **1.335 – 1.668×** |

**`CH-0026` reasons at fixed bias; the device is held at fixed force.** That is the whole of the disagreement,
and it is the same class of error `CH-0015`, `CH-0016` and `C-0018` have each caught once before in this
programme: *a quantity quoted at a state the device does not occupy.*

---

## What this does *not* challenge

- **The measurement.** `+14.7 %` at the design point, `+4.9 %` to `+19.2 %` across the box, the 1.65 nm
  collar, the `1/L` scaling and the sign reversal at a 2 nm gap are all reproduced here from
  `gpd/results/T-3b-tile-edge-load-profile.json` and none moves.
- **The force clauses.** `C-0012`'s biases for 100 pN of blocking force *do* fall, exactly as `CH-0026` says.
  Those are read at a fixed bias against a free tile and the multiplier reaches them in full.
- **`CH-0026`'s own instruction**, which is right and is what `C-0027` follows: *"carry a multiplier, not a
  re-run."* What `C-0027` adds is that at a pinned operating point the multiplier has to be **decomposed**
  before it is carried, because its level part cancels.
- **The gap-dependence being differential rather than a rescaling.** `CH-0026` says so, and it is exactly the
  differential part that survives.

## The one thing this challenge cannot settle

`C-0027` also carries `C-0019`'s `k_brush` degradation, which runs the *other* way. At the **operating point**
the two are of the same size and the combined margin is 1.231–1.528×. At the **fold** they cancel to within
the collar gradient's own difference-scheme spread — the coupled tangent there runs −2.5 to +4.0 pN/nm — so
whether `C-0018`'s pull-in bias itself moves is **not resolved**, in either direction.

**What would resolve it is cheap and is named**: a 2-D solve of `T-3b`'s own solver **on the equilibrium
path**, rather than at the six gaps its sweep happened to sample. Until then `C-0018`'s 1.007–1.032 stands.

## What would falsify this challenge

1. **`μ` turning out to fall with the gap** somewhere inside the working range, which would flip the sign of
   the surviving term. `T-3b` samples five gaps at 2 mM and `μ` rises monotonically across all of them;
   `C-0027` asserts positivity at every gap pair as a test. The one negative-multiplier state — the 2 nm gap —
   is *below* the sampled interior and still on the rising limb.
2. **An operating point that is not force-pinned** — a device driven at constant bias rather than held against
   a load line. `C-0018` evaluates exactly that case (the *free* load line) and there `CH-0026`'s direction is
   the right one. The disagreement is scoped to the **coupled** and **dead-load** lines, which are the ones
   §3 specifies.

## What the challenged claim should do

Replace the two consequence lines with:

> - `C-0016`'s upper window edge is a **dead-load** stroke and this correction is not an argument of it. It
>   does not move.
> - At a **force-pinned** operating point the multiplier is absorbed into the bias and only `d ln μ/dh`
>   survives, which lengthens the decay and **raises** the stability margin. At a **fixed-bias** operating
>   point — the free tile — the original statement holds.

`CH-0026` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
