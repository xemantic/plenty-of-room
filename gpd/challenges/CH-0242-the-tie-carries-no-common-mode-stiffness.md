# CH-0242 — **The lattice's crossover carries only the SMALLER of a bond's two azimuthal springs. Expanded on the corpus's OWN phosphate span, the common-mode stiffness is `3.52810239×` the relative one — so the element set is missing the larger of the two at every one of the 435 bonds and all 59 ties, and it is the one a scaffold crossover's departure loads.**

| | |
|---|---|
| **Against** | [`C-0154`](../claims/C-0154-honeycomb-grillage.md) (`T-253`) — `HoneycombGrillage`'s element set, whose bond carries *"a dihedral spring `k_θ` on the **relative** roll"* and nothing else on the azimuthal coordinates; and, through it, [`C-0175`](../claims/C-0175-drawable-raster-rim.md) §8, [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) §4, [`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md) and [`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md) itself |
| **Raised by** | [`T-291`](../tasks/T-291-common-mode-departure-and-beam-twist.md) / [`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md) |
| **Grounds** | **logical** — a second-order expansion of `turnPhosphateSpan`, which is `C-0147`'s model and this corpus's own definition of what a crossover's geometry costs, about the line of centres. The resulting quadratic form in the two backbone azimuths is **not** rank one, and the mode the model keeps is the **cheaper** of its two |
| **Status** | **RAISED.** No number of `C-0154`, `C-0175`, `C-0180`, `C-0187` or `C-0190` is disputed. What is disputed is the **element set**: a spring the lattice does not have, which is where the magnitude of `C-0190`'s answer lives |

---

## 1. The corpus's own span, expanded

`turnPhosphateSpan(d, r_P, θ_u, 180° + θ_l)` puts the two phosphates

&nbsp;&nbsp;&nbsp;&nbsp;`span²(θ_u, θ_l) = (d − r_P(cos θ_u + cos θ_l))² + r_P²(sin θ_u + sin θ_l)²`

apart, where `θ_u` and `θ_l` are each duplex's own backbone azimuth measured off the line of
centres, in the **same** rotational sense (`ForcedCrossoverPrice`'s own construction, and
`CH-0240` §1). To second order about `θ_u = θ_l = 0`,

&nbsp;&nbsp;&nbsp;&nbsp;`span ≈ (d − 2r_P) + (r_P/2)(θ_u² + θ_l²) + [r_P²/(2(d − 2r_P))](θ_u + θ_l)²`.

Both terms are positive, and the second is a **pure common mode**. So the span excess a crossover
pays is a quadratic form of rank **two**, not one, and its two eigenmodes cost differently:

| mode | `θ_u`, `θ_l` | span excess |
|---|---|---|
| **relative** | `+θ`, `−θ` | `r_P θ²` |
| **common** | `+θ`, `+θ` | `r_P θ² · [1 + 2r_P/(d − 2r_P)]` |

At the honeycomb's own `d = 2.536 nm` and `T-71`'s measured `r_P = 0.908637858 nm`, `d − 2r_P` is
`0.718724283 nm` and the ratio is

&nbsp;&nbsp;&nbsp;&nbsp;`1 + 2r_P/(d − 2r_P) = 3.52810239`.

**The common mode is the stiffer of the two, by three and a half times**, and it is the one the
model does not have at all.

## 2. The exact statement, which needs no expansion

The same fact appears exactly rather than to second order. At a common-mode departure `δ` a pure
relative roll `r` puts the pair at `(δ + r, 180° + δ − r)` and

&nbsp;&nbsp;&nbsp;&nbsp;`span²(u) = (d − 2 r_P cos δ · u)² + 4 r_P² sin²δ · u²`, `u = cos r`,

whose stationary point is `u* = d cos δ / (2 r_P) = 1.37990892` — **outside** the reachable
`[−1, 1]`. So the minimum over the *whole* relative channel is at `u = 1`, i.e. `r = 0`, the built
state itself: **a relative roll of any amplitude makes a honeycomb crossover's span worse.**
`C-0190` measures the shortfall it leaves at `0.0683674233 nm`, which is the whole of the
departure's own span excess.

## 3. What the model carries instead

`HoneycombGrillage`'s bond and tie carry, on the roll coordinates, exactly

&nbsp;&nbsp;&nbsp;&nbsp;`½ k_θ (Φ_upper − Φ_lower)²`

and nothing else. That is the rank-one **relative** form — the cheaper eigenmode — and its
prestrain is a couple pair on the same coordinate. So:

- a **common-mode** azimuthal demand has no work conjugate at the bond at all, and reaches the
  structure only through the duplex's own `GJ` (`CH-0240`, `C-0190`);
- the tie **stiffness** deliverables of `C-0175` and `C-0180` — the `1.12×` the 59 ties buy — are
  **lower bounds**, because the missing spring is a stiffness the built object has;
- and `C-0190`'s answer has a derived **shape** and an unpinned **magnitude**, because the
  magnitude is `k_common · δ` and `k_common` is the number this challenge says is absent.

## 4. What this challenge does NOT claim

- It does not claim a **value** for `k_common`. The ratio above is exact given that the crossover
  is loaded in span at all — i.e. that its `g′(span) > 0` — and `T-71`'s measurement says the
  honeycomb's `d − 2r_P = 0.7187 nm` sits at `+1.50 σ` of the measured C2′-endo phosphodiester
  step, so the bond is stretched and `g′ > 0`. The **absolute** stiffness needs a force-extension
  law for a phosphodiester step, which this repository does not carry.
- It does not dispute `k_θ`. `Gen1Tile.crossoverHingeStiffness()` is a square-lattice-fitted
  constant for the relative coordinate and is inherited unchanged.
- It does not move any emitted number. Every study named above is internally consistent; what is
  challenged is what the lattice is a model **of**.

## 5. What would falsify this challenge

One of:

- a demonstration that a honeycomb crossover is **not** loaded in span at its built geometry —
  i.e. that the covalent step is relaxed at `d − 2r_P` — which would put `g′ = 0` and make the
  leading term quartic in the azimuths rather than quadratic. `T-71`'s `+1.50 σ` says otherwise,
  and `C-0175`'s own `n = 0` reach check is what established that the crossover closes *tightly*;
- a demonstration that `k_θ` as fitted already **contains** the common-mode stiffness — which it
  cannot, being a coefficient on a difference of two rolls;
- a measurement, or a published all-atom or oxDNA number, for a crossover's resistance to a
  **common** roll of its two duplexes. None was sought here, and none is known to this repository.
