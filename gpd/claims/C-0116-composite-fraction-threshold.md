# C-0116 — **`C-0109`'s overturning of the flatness negative SURVIVES, with 3.30× of margin on the adverse end of the measured band.** The uncoupled four-layer free-tile dishing crosses `T-5b`'s 0.10 at `f = 0.0788618807`, **monotonically** (one sign change over the whole unit interval), against a measured interlayer coupling of **0.26–0.33** — so the measured band's *low* end clears the threshold by **3.29690337×** and its centre by **3.80411927×**. Declared falsifier **`F2` did not fire**: the crossing is nowhere near the measurement's own uncertainty. **The cheap bound removed the sweep before it ran**: `f` enters only through `1 + f(factor − 1)`, and that one number multiplies `D_∥` and `D_⊥` alike, so the threshold is a **scalar inversion** and not a two-dimensional search. And `CH-0124`'s true honeycomb spacing has its **own** threshold, `f = 0.105149174` — still cleared, by 2.47267753×

> **Annotated, iteration 34 ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **THE HEADLINE MARGIN IS WITHDRAWN FOR `15 × 4`.**
> The cross-section this threshold is read on is not a honeycomb — every `edgeY` in the four-layer line is 1.5× too small —
> and re-solved at the corrected geometry the crossing moves from `f = 0.0788618807` to **`0.276970522`**,
> which is **inside** the measured 0.26–0.33 band rather than 3.29690337× below it:
> at the band's low end the `15 × 4` four-layer tile dishes **0.101759944** and **fails `T-5b`**.
> `10 × 6`'s crossing moves to **0.012737738** and stays 20× below the band, so *that* cross-section still has no threshold to clear.
> **The verdict *"the four-layer tile is flat"* survives on a different cross-section from the one this claim reads it on.**

| | |
|---|---|
| **Task** | [`T-196`](../tasks/T-196-composite-fraction-threshold.md) — where the four-layer tile stops being flat |
| **Leaf** | `A8.2` |
| **Verification type** | **in-silico** (beam-and-hinge grillage, `C-0022`'s solved collar) **+ logical** (the scale inversion, asserted as a test at `1e-12`) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Verdict** | **PASS on all four predicates. Of three declared falsifiers, NONE fired.** `F1`: the dishing is monotone — **one** sign change over `[0, 1]`, counted rather than assumed. `F2`: the crossing is at **0.0788618807**, a factor of **3.2969** below the measured band's *low* end, so the verdict does not sit inside the measurement's uncertainty. `F3`: `C-0109`'s two numbers reproduce at **1.1e−10** and **8.6e−10**, so the comparison is licensed. |
| **Provenance** | [`gpd/results/T-196-composite-fraction-threshold.json`](../results/T-196-composite-fraction-threshold.json), produced by `tile.CompositeFractionThresholdStudyKt`; model [`src/main/kotlin/tile/CompositeFractionThreshold.kt`](../../src/main/kotlin/tile/CompositeFractionThreshold.kt), tests [`src/test/kotlin/tile/CompositeFractionThresholdTest.kt`](../../src/test/kotlin/tile/CompositeFractionThresholdTest.kt) (10, written first and watched to fail). **Result file byte-identical across two independent JVM runs.** |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Four honeycomb layers, 15 rows, `C-0086`'s buildable 38.08 nm width; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation; 81 × 81 dishing grid; flat means below `T-5b`'s 0.10. **Uncoupled throughout** — no attachment coupling anywhere in this study. |
| **Consumes** | [`C-0109`](C-0109-four-layer-tile.md) (the tile, its calibration and the threshold it left open), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the buildable width), [`C-0001`](C-0001-layer-stiffness.md) (the foundation secant), [`CH-0124`](../challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md) (the true honeycomb spacing, carried beside the default) |
| **Constrains** | `ANSWERS.md` §2 row 5b and §3 row (g), both of which state `C-0109`'s verdict as conditional on this threshold and **say they are owed a re-read when it lands**. **No claim is contradicted and no challenge is raised** — `C-0109`'s stated interval `(0.00, 0.26)` is confirmed and narrowed, not overturned. |

---

## 1. The cheap bound, which removed the sweep before it ran

`multiLayerRigidities` admits the interlayer coupling only through

```
realised = 1 + f (factor − 1),      factor = 39.4479652  (four honeycomb layers, spacing d)
```

and that **one** number multiplies `D_∥` **and** `D_⊥` — the identity `k_s/k_θ = S/B` that `C-0109` asserts
as its own gate 3. So **`f` is a pure scale on the plate**: the free-tile dishing is a function of that single
scalar, and a threshold in `f` follows from a threshold in the scale by one division.

Asserted as a test rather than argued: over `f ∈ {0.1, 0.26, 0.30, 0.5, 1.0}` the two rigidity ratios agree to
`1e-12`, and each equals the affine enhancement.

**That is what makes this a one-dimensional inversion instead of a two-dimensional search**, and it is the
whole reason the task was cheap enough to be worth doing before anything else in the queue.

---

## 2. The sweep, and it is monotone

| `f` | enhancement | `D_∥` | `D_⊥` | dishing | |
|---|---|---|---|---|---|
| 0.0000 | 1.0000 | 362.78 | 19.222 | **0.18273857** | not flat |
| 0.0300 | 2.1534 | 781.22 | 41.393 | 0.134081210 | not flat |
| 0.0500 | 2.9224 | 1060.18 | 56.173 | 0.116692022 | not flat |
| 0.0750 | 3.8836 | 1408.88 | 74.649 | 0.101848791 | not flat |
| **0.0788618807** | **4.03207885** | — | — | **0.1** | **the crossing** |
| 0.1000 | 4.8448 | 1757.58 | 93.125 | 0.091349054 | flat |
| **0.2600** | 10.9965 | 3989.26 | 211.370 | 0.061259574 | **flat — the measured band's low end** |
| **0.3000** | 12.5344 | 4547.18 | 240.931 | **0.057719943** | **flat — `C-0109`'s reading** |
| 0.3300 | 13.6878 | 4965.62 | 263.102 | 0.055518829 | flat |
| 1.0000 | 39.4480 | 14310.78 | 758.254 | 0.038201271 | flat |

**Strictly decreasing over the whole interval, and `F1` did not fire**: the scan counts **one** sign change
across `[0, 1]`. `CLAUDE.md`'s warning is that a non-monotone verdict has no threshold and gets *more*
alternating under refinement; here monotonicity is a measured property, not an assumption, and the search
would have reported the alternation had there been any.

Note the `f = 0` row: four **independent** layers still dish only 0.1827 against the single layer's 0.3079,
because four layers add rigidity linearly even with no coupling at all. That is why `C-0109`'s interval was
non-trivial rather than vacuous.

---

## 3. The threshold, and the margin

| | |
|---|---|
| crossing | **`f` = 0.0788618807** |
| bracket | 0.0788618803 … 0.078861881 |
| dishing there | 0.1 — the crossing, to the bisection's own width |
| enhancement there | 4.03207885 |
| sign changes over `[0, 1]` | **1** |
| **margin at the measured band's LOW end (0.26)** | **3.29690337×** |
| **margin at the band's centre (0.30)** | **3.80411927×** |

**So `F2` did not fire.** The threshold is not merely below the measured band — it is below the *adverse* end
of it by a factor of 3.3, which is the reading that matters, because `C-0109` warns that a slab's `f` is
plausibly *lower* than a measured rod's.

**How far the calibration would have to be wrong.** For the verdict to fail, a 15-wide × 4-deep slab would
have to realise **less than 30 %** of what the least-coupled measured bundle realises. That is the number a
successor should attack, and it is stated as a threshold rather than as a confidence.

---

## 4. `CH-0124`'s geometry has its own threshold, and it also clears

The true honeycomb array stacks its rows at `d√3/2`, which scales `Σy²` by 3/4 and therefore moves `factor`
itself — so the threshold there is a **different number**, not the same one read at a different dishing:

| | default spacing `d` | true honeycomb `d√3/2` |
|---|---|---|
| parallel-axis factor | 39.4479652 | **29.8359739** |
| threshold `f` | 0.0788618807 | **0.105149174** |
| dishing at the measured `f = 0.30` | 0.057719943 | 0.06512036 |
| margin at the band's low end | 3.29690337× | **2.47267753×** |

Both are carried, because `CH-0124` is **open**. The verdict is the same on either geometry and the margin is
1.33× smaller on the corrected one — which is the direction `CH-0124` predicted.

---

## 5. The five verification gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | the enhancement is dimensionless and affine in `f`; rigidities in pN·nm, reach in nm, dishing dimensionless |
| **limiting cases** | `f = 0` is `INDEPENDENT` (layers add linearly, enhancement 1) and `f = 1` is `COMPOSITE` (full parallel axis, enhancement = factor); both named and both read |
| **symmetry / conservation** | the two rigidities scale by the *same* factor to `1e-12` — the identity the inversion rests on. `CLAUDE.md`'s uniform-load falsifier is valid here (this is a **load**, not an eigenstrain, so the cylinder caveat does not apply) and is inherited from `C-0109`'s own gates on the same lattice |
| **numerical convergence** | mesh subdivisions 1/2/4 (nested only, per `CLAUDE.md`): departure **1.5e-05**. Dishing grid 41/81/161: **0.0**. Bisection width 1e-6/1e-9/1e-12: **0.0**. Scan resolution 20/40/80: **0.0** — refining the *scan* does not move the root, which is the check that the bisection owns the precision and the scan missed no feature |
| **literature cross-check** | the calibration is `C-0109`'s, from four measured bundles across two lattices and three laboratories, with its own *"published as a naïve model and measured to over-predict by 2.7×"* provenance; nothing new is cited here |

**Reproductions:** `C-0109`'s four-layer 0.0577199433 at **1.1e−10**, and the single-layer 0.307902368 at
**8.6e−10**. `F3` did not fire, so the comparison is licensed — and the harness that produced the negative is
the harness that now bounds its reversal.

---

## 6. Validity range, and what this does NOT establish

- **The body is a SMEARED equivalent sheet.** `OrigamiGrillage` never reads `layers`, so the four-layer tile
  enters as one orthotropic sheet carrying the multi-layer rigidities — `C-0109`'s reduction, asserted there
  at `1e-12` and inherited here.
- **This is the RIGID-limit family, and body rigidity is first order.** `C-0093` found a *buildable*
  four-layer body reads 0.100166871 where its rigid limit reads 0.0344013403. This study sweeps the
  interlayer coupling of a smeared sheet; it does **not** model a buildable body's own compliance, and the
  threshold on such a body is a separate question.
- **The measured `f` is calibrated on RODS.** Bundles crossovered around a closed ring, not a 15-wide ×
  4-deep slab, whose crossover topology differs and whose second moment is far larger. `f = 0.30` is
  plausibly an **upper** bound there — which is why §3 states how far it would have to fall.
- **The dropout statistics do not enter**, and that is deliberate: they are measured on a single-layer
  rectangle and this study is of the **uncoupled** tile. The *coupled* four-layer question is `T-197`.
- **Kirchhoff is not safe at this thickness.** Thickness over span is 0.252, so `D_∥` is an upper bound
  again — `C-0109`'s own caveat, unrelieved here.
- **`T-5b`'s 0.10 is a convention, not a measurement**, and the threshold is a function of it: a tolerance of
  0.15 or 0.05 would move `f*` correspondingly, monotonically, by §2's own table.
