# CH-0285 — **`areaInnerProduct` lays a 6-point Gauss rule across a strip that `evaluate` is DISCONTINUOUS inside, so every fit taken through it is short by a constant `0.819694` — which makes `CH-0284`'s own published channel sizes `1.22×` low and would silently bias any convention adopted to close it**

**Against** [`HoneycombGrillage.areaInnerProduct`](../../src/main/kotlin/tile/HoneycombGrillage.kt) and everything downstream of it — `faceRigidGram`, `worstFaceNonOrthogonality`, `unconditionalFaceRigidCoefficients`, and therefore `HoneycombDeflection.dishingCoefficients` **at every odd raster-row count**, which is `C-0219`'s own corrected `15 × 4` triple.
**From** [`C-0221`](../claims/C-0221-the-fit-and-the-sample-in-one-reconstruction.md) (`T-326`) §2d.
**Kind** — a **numerical defect**, not a convention: the rule is exact for the integrand it was written for and the integrand it is handed is a different one.

---

## 1. The statement

`areaInnerProduct(a, b)` is `integrateOverFace { evaluate(a, s, y) * evaluate(b, s, y) } / area`, and `integrateOverFace` lays **one 6-point Gauss-Legendre rule** (`HoneycombGrillage.QUADRATURE_POINTS = 6`) across each whole tributary strip in `y`.

`evaluate` is the **nearest-beam** reconstruction. Its cell boundary is the midpoint between two consecutive face axes, and a honeycomb face's gap sequence is `d, 2d, d, 2d, …` against a strip of `3d/2` — so a boundary falls **`d/4` inside each strip's end, at every strip, by construction**.

A smooth rule is therefore being applied to a jump. That is not a rounding: Gauss-Legendre has no order at all across a discontinuity inside its own interval.

## 2. The size, and why it is a constant

Measured against exact piecewise integration, on the piston projection's fit/sample gap:

```
gauss6 / exact  =  0.819694
```

at all **12** readings over `m = 4, 6, 10, 14, 15, 16` and both face columns in Kotlin, and at **20 of 20** over `m = 4, 6, 10, 14, 15` in the independent Python prototype ([`gpd/data/T-326-cheap-bound/`](../data/T-326-cheap-bound/README.md)).

It **must** be a pure number: both readings are linear functionals of `(W, Φ)`, and `C-0221`'s bond pairing reduces each to a multiple of the same scalar — the summed relative roll across the face's own vertical bonds — so their ratio carries no field, no row length and no thickness.

## 3. What it costs

| | |
|---|---|
| `CH-0284`'s published collar channel, `4.3E-4`–`5.0E-4` | is `1.2200×` low; exactly integrated it is `5.2E-4`–`6.1E-4` |
| `CH-0284`'s published prestrain channel, `0.0067` | is `1.2200×` low; exactly integrated it is `8.1E-3` |
| `C-0219`'s committed `15 × 4` triple `0.242196276 / 0.157167743 / 0.150056485` | was fitted through the unsplit rule; split, it moves by `5.3E-5`–`6.3E-5` relative, and **no verdict moves** |

So the defect **moves no verdict anywhere it has been measured**, and it is raised because a fit adopted to close `CH-0284` would inherit an 18 % error in **exactly the term that question is about**.

## 4. What would settle it

Split the `y` quadrature at the nearest-beam boundaries wherever the integrand goes through `evaluate`.
`C-0221` adds `integrateOverFaceSplit` and `splitFaceInnerProduct`, which do that and are asserted equal to the unsplit rule on a field that carries no jump — so the split is demonstrably about the **reconstruction** and not about the rule.
Repointing `areaInnerProduct` at it is **not** taken there, because `areaInnerProduct` is on the path of `C-0219`'s committed odd-`m` readings and the re-emission is `T-335`'s.

The **load** quadrature must **not** be split: `assembleLoad`'s integrand is a pressure field, which carries no jump, and its centred tributary is what makes `CLAUDE.md`'s uniform-load falsifier exact.

| | |
|---|---|
| **Status** | **RAISED**, iteration 53 |
| **Raised by** | [`C-0221`](../claims/C-0221-the-fit-and-the-sample-in-one-reconstruction.md) (`T-326`) |
| **Severity** | **measured and bounded** — a constant `1.22×` on one term, moving no committed verdict, and carried in the queue with the adoption it would otherwise bias (`T-335`) |
