# CH-0248 — **`HoneycombGrillage` DOES carry a common-mode azimuthal stiffness. It is the vertical LINK's own residual — `ΔW + (d/2)·unitY·(Φ_u + Φ_l)`, a function of the SUM — carried at a penalty, so the lattice sits at the RIGID end of that mode at `336.800449×` the physical value rather than missing it. `C-0190` §6's magnitude is therefore derivable and not a threshold. And the ratio the corpus quotes, `3.52810239`, is `3.52847408`.**

| | |
|---|---|
| **Against** | [`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md) (`T-291`) §6 — *"what scales it is the tie's own **common-mode** stiffness, and `HoneycombGrillage` does not carry one: its bond and tie have `½ k_θ(Φ_u − Φ_l)²` and **nothing else on the azimuthal coordinates**"* — and its headline `3.52810239`; and, inherited from it, [`CH-0242`](CH-0242-the-tie-carries-no-common-mode-stiffness.md) §3, the challenges index row for `CH-0242`, and two prose strings of `gpd/results/T-291-common-mode-departure-and-beam-twist.json` |
| **Raised by** | [`T-297`](../tasks/T-297-the-common-mode-is-the-link.md) / [`C-0194`](../claims/C-0194-the-common-mode-is-the-link.md) |
| **Grounds** | **logical, and measured on the assembled matrix.** Four lines of `HoneycombGrillage.assemble` build the normal link with the gradient `(1, armY, −1, armY)` over `(W_a, Φ_a, W_b, Φ_b)`, `armY = (d/2)·unitY`. Its residual is a function of `Φ_a + Φ_b`, so a common-mode roll has a work conjugate at the bond. Probed on the recommended `10 × 6` lattice, a common roll of 1 mrad at every beam stores **exactly `0.0` pN·nm** in the hinges and **`6.7528608` pN·nm** in the links |
| **Status** | **RAISED.** **No number of `C-0190` is disputed and no verdict of it moves**: its 58 twisted beams, its `17.1428571°`, its `8.31368089 k_BT`, its `0.296735462` free tile and its `0 of 64` are all right about the load it assembles. What is disputed is the **premise the threshold rests on** — and, separately, one arithmetic slip that `T-291`'s own artifact does not share |

---

## 1. The premise, read off the file all of them consume

`HoneycombGrillage.assemble`, verbatim, at both the bond and the tie:

```kotlin
val armY = half * bond.unitY
val linkGradient = doubleArrayOf(1.0, armY, -1.0, armY)
```

over `(W_a, Φ_a, W_b, Φ_b)`, `half = d/2`. The residual is therefore

&nbsp;&nbsp;&nbsp;&nbsp;`R = (W_a − W_b) + (d/2)·unitY·(Φ_a + Φ_b)`

— **the sum, not the difference**. `CH-0242` §3's *"and nothing else on the azimuthal
coordinates"* is false as written, and `C-0190` §6 inherits it.

The lattice's own energy accessors say so without any argument. On the recommended `10 × 6`
block, at `Φ = 1 mrad` on **every** beam with the axes at nominal:

| state | hinge | link | slip |
|---|---|---|---|
| a **common** roll of 1 mrad | `0.0` | **`6.7528608`** | `0.0` |
| a **rigid** roll (`Φ = α`, `W = α y`) | `0.0` | `0.0` | `0.0` |

## 2. The direction reverses

Read as `½ k (Φ_a + Φ_b)²`, the link gives the model a common-mode azimuthal stiffness of
`k_link·(d·unitY)²/4` — **`16078.24 pN·nm/rad`** at an in-plane bond and **`4019.56`** at an
interlayer one, against `CH-0242`'s physical `3.52847408 × k_θ` = **`47.7381787 pN·nm/rad`**.

**The lattice is `336.800449×` and `84.2001122×` too STIFF.** It is not missing the larger of the
two springs; it is carrying it as a **constraint**.

## 3. What that does to `C-0190` §6

Three consequences, and none of them moves a number `C-0190` emitted:

- the departure's magnitude is **not** unpinned for the reason given. Because the common mode is
  the link, the departure is an **offset in the link's own residual**, `R₀ = d·unitY·ρ`, which is
  a **load** — so it costs no entry of the stiffness matrix and its size is fixed by `k_link`,
  not by a spring the model lacks;
- the threshold `0.11663286 pN·nm/rad` is therefore a threshold on a stiffness the model
  **has**, and the *"409× on the wrong side"* is a comparison against a spring the lattice
  already over-carries by 337×;
- and *"a common-mode azimuthal demand … reaches the structure only through the duplex's own
  `GJ`"* (`CH-0242` §3, first bullet) is false: it reaches it through the tie.

## 4. And one arithmetic slip, which its own artifact does not share

&nbsp;&nbsp;&nbsp;&nbsp;`1 + 2 r_P/(d − 2 r_P) = d/(d − 2 r_P)`

at `d = 2.536 nm` and `T-71`'s measured `r_P = 0.9086378584708424 nm` is **`3.52847408`**, not
`3.52810239` — **0.0105 % low**. The wrong value stands in `C-0190`'s headline and §6,
`CH-0242`'s headline and §1, the challenges index row for `CH-0242`, and two prose strings of
`T-291`'s own result file. **`T-291`'s `openQuestions` block computes the same expression and
emitted `3.52847408`**, so the artifact carried the correction the prose did not — `CLAUDE.md`'s
own *grep every headline number out of the result file*, met from the other side.

No verdict turns on it: the `409×` is `409×` either way.

## 5. What this challenge does NOT claim

- It does **not** claim that `C-0190`'s per-beam twist is wrong about the object it loads. It is
  the model's natural choice given the premise, and `C-0190` says so.
- It does **not** dispute `k_θ`, the departure's magnitude in degrees, the derived alternation,
  the uniformity of the twist assignment, or any coupled verdict.
- It does **not** claim the model's link stiffness is right. `C-0194` measures it to be
  **`241.348295×`** the span law's own `T/g`, and that is a separate finding with a separate
  consequence (`C-0194` §5–§6).

## 6. What would falsify this challenge

One of:

- a demonstration that the link's gradient does not carry `Φ_a + Φ_b` — which is four lines of
  the committed source and one probe of the assembled matrix;
- a demonstration that the frame-indifferent common-mode coordinate is something other than the
  link residual — which requires an arm other than `d/2` to annihilate the linearised rigid roll
  `Φ ≡ α`, `W = α y`, and the residual an arm `a` leaves under it is `α·unitY·(2a − d)`;
- a demonstration that `1 + 2 r_P/(d − 2 r_P)` at the stated constants is `3.52810239`.
