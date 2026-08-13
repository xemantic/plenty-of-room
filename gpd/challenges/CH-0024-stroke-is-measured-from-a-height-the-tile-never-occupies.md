# CH-0024 — Every stroke in this programme is measured from `L₀`, and the tile is never at `L₀`

| | |
|---|---|
| **Against** | [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md) and [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) — the stroke coordinate `s = L₀ − h`, and every stroke quoted on it; with [`C-0016`](../claims/C-0016-design-window.md) inheriting it through the window |
| **Raised by** | [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) (`T-13`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a displacement quoted from an origin the device does not occupy, in the one state (zero bias) that the origin was supposed to describe |
| **Direction** | **unfavourable, and small.** 2–13 % of §3's acceptable stroke with the committed coupling present; up to 79 % without one |
| **Status** | raised. **No verdict in `C-0012`, `C-0016` or `C-0017` moves** — the shortfall is inside every model bracket those claims already carry. What moves is the *statement* that §3's 3 nm is delivered, which becomes "2.62–2.93 nm is delivered" |

---

## What is challenged

`C-0012` fixes the coordinate and `C-0017` inherits it verbatim:

> The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode.

and on it `C-0017` reports:

> **secant at 3 nm — places the operating point at 3.000 nm exactly** … delivered force **99.9994 – 100.0001 pN** across all 54 states.

The force is not challenged. **The travel is**, because `s = 0` is `h = L₀`, and `L₀` is not where the tile sits at zero bias.

---

## The methodological ground

`C-0010` establishes that a non-adsorbing layer exerts **no upward force above `L₀`**, so at zero bias the tile is held wherever the **downward** forces balance the layer — and `C-0021` shows those forces are not zero. The tile therefore starts each actuation cycle at

&nbsp;&nbsp;&nbsp;&nbsp;**`h₀ = L₀ − d`, with `d > 0` strictly**,

and the travel available to the load between rest and the operating height `L₀ − δ*` is `δ* − d`, not `δ*`.

`d` is not a rounding error, and its size depends entirely on **what resists the descent**:

| what the preload is spent against | `d` [nm] | delivered stroke [nm] | shortfall |
|---|---|---|---|
| the layer alone (no coupling fitted) | **0.05 – 2.36** | 0.64 – 2.95 | **up to 79 %** |
| the layer **and** `C-0017`'s `K2` at 33 pN/nm | **0.07 – 0.38** | **2.62 – 2.93** | **2 – 13 %** |

(18 states each: three §3 heights × six `C-0003` layer models, 2 mM MgCl₂, hold-down = `C-0014`'s eight substrate tethers + van der Waals against a gold electrode + the residual zero-bias field + gravity.)

**The mechanism of the spread is the standing finding this programme already carries.** Three of six layer models have *exactly zero* stiffness at `L₀`, so a few piconewtons of preload push the tile a long way into a layer that resists with nothing; the coupling, at 33 pN/nm, absorbs the same preload in a tenth of the distance. `C-0017`'s coupling therefore protects its own stroke figure — but it protects it to 2–13 %, not to zero.

---

## Why `C-0012` and `C-0017` could not have seen it

Neither claim is careless; both **declared** the gap and handed it here.

- `C-0012`: *"the zero-bias force is a sign-changing near-cancellation under 4 pN for which no single number is defensible"* — true of the **electrostatic** term, and `C-0021` confirms it (0.078–0.404 pN of hold-down over 5–10 nm). But the electrostatic term is not the hold-down. **Van der Waals is 0.24–28.1 pN across the same range and no claim in this programme had evaluated it**, and `C-0014`'s tether is 4.6–9.4 pN and was reported as a by-product rather than fed back into the stroke coordinate.
- `C-0017`: *"The zero-bias state is not solved … every coupling here is taken **unpreloaded** … That is `T-13`'s question."* Exactly right, and this challenge is the answer coming back.

---

## What this does *not* challenge

- **The delivered force.** `W(s)` is evaluated at the *held gap*, which is an absolute height and does not move. 100 pN is still 100 pN.
- **`k_c* = 33.333 pN/nm`.** It is `F/δ` from §3 alone and contains no reference to where the tile starts.
- **The stability floors, the `V*` table, the scheme verdicts, or `C-0016`'s window edges.** All are read at absolute heights.
- **The 5 nm and 7 nm columns**, where the shortfall is 2–13 % and the delivered stroke stays above 2.6 nm.

## The remedy proposed

Two lines, not a recomputation:

1. **Quote strokes as `δ* − d` with `d` named**, or state explicitly that the stroke is measured from `L₀` and that the zero-bias rest sits `d` below it. `C-0021`'s `deliveredStrokeToWorkingPoint` column supplies `d` at every one of the 18 states.
2. **Read §3's "≥ 3 nm acceptable" against the delivered figure.** With the committed coupling the device delivers **2.62–2.93 nm**, i.e. it *misses* §3's acceptable stroke by 2–13 % — a shortfall that is invisible in the `L₀` coordinate and is recovered by any of: a slightly taller layer, a stiffer coupling (which reduces `d` in proportion), or a smaller hold-down (which the thermal scale bounds below at 1.381 pN).

`C-0012` and `C-0017` are annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
