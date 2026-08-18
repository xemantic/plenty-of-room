# CH-0122 — `C-0104`'s 15.4497275° is a **secant of the peak**, not the triangle inequality it is named as, and the bound it claims is **tighter** than the bound it quotes: 11.5188°

| | |
|---|---|
| **Against** | [`C-0104`](../claims/C-0104-row-end-prestrain.md), Deliverable 1 and its `cheapBounds` record *"the prestrain at which C-0090's placement reaches T-5b's 0.10"* |
| **Raised by** | [`C-0107`](../claims/C-0107-row-end-prestrain-value.md) (`T-182`) |
| **Kind** | **methodological** — a bound derived by one argument and justified by another |
| **Status** | **OPEN, and it does not overturn a verdict.** `C-0104`'s number remains a valid upper bound on the crossing, but by **convexity** and not by the inequality it names; the inequality it names gives **11.5188°**, which is 1.34× tighter and moves the threshold *away* from every candidate value |

---

## The ground

`C-0104`'s Deliverable 1 states its second consequence as

> *"**Peak dishing is an absolute value**, so `D(θ₀) ≤ D_load + |θ₀|·D_unit`: one unit-prestrain
> solve gives a rigorous ceiling over the whole axis."*

and its result file records the ceiling's ingredient as

> `"name": "dishing per radian of uniform row-end prestrain, at C-0090's placement"`,
> `"statement": "one solve, exact for every theta_0 by linearity"`, `"value": 0.140379322`.

`D_unit` in that inequality is the **peak of the prestrain-only field**, `max|B|`. What
`C-0104` computes is

```kotlin
val unitSlope = abs(unitDishing - zeroDishing) / unitAngle
```

— the **secant of the peak** between `θ₀ = 0` and `θ₀ = 1 rad`, `|D(1) − D(0)|`, with both terms
read on the **loaded** tile. Those are two different numbers, because peak dishing is
`max_{x,y} |A(x, y) + θ₀ B(x, y)|` and a maximum of absolute values is **not** linear in `θ₀`; the
two fields do not peak in the same place.

`T-182` computes both on an independent code path (an explicit elastic-support grillage rather
than `C-0058`'s Woodbury surrogate) and reproduces `C-0104`'s own definition exactly:

| quantity | value | departure from `C-0104` |
|---|---|---|
| `C-0104`'s secant `\|D(1 rad) − D(0)\|`, reproduced | **0.140379315** | **5.1e−08** |
| the peak of the prestrain-**only** field, `max\|B\|` — the `D_unit` the inequality needs | **0.188285084** | — |
| the crossing from the **secant** — `C-0104`'s published threshold | **15.4497275°** | — |
| the crossing from the **triangle inequality** — `(0.10 − 0.0621469105)/max\|B\|` | **11.5188°** | **1.341×** tighter |

## Why the verdict does not move

Peak dishing is a maximum of affine functions of `θ₀` and is therefore **convex** in `θ₀`. A chord
from `θ₀ = 0` to `θ₀ = 1 rad` lies **above** the function on `[0, 1 rad]`, so the secant crossing is
an upper bound on the true crossing wherever the true crossing lies inside that interval — and it
does (`C-0104` measures the first crossing at its 16.875° rung). **`C-0104`'s 15.4497275° is a
correct upper bound reached by a correct argument that the claim does not state, and an incorrect
argument that it does.**

## Why it is worth raising anyway

1. **The two bounds are not interchangeable and only one is cheap.** The triangle bound needs a
   *load-free* prestrain solve; the secant needs a *loaded* one. `C-0104`'s prose tells the next
   agent to compute the first and its code computes the second.
2. **It moves the threshold in the direction that matters.** `T-182` derives a row-end prestrain of
   **21.03–24.98°** from the duplex's own twist boundary layer. Against 15.4497° that is a
   1.36–1.62× exceedance; against the true triangle ceiling of 11.5188° it is **1.83–2.17×**. The
   correction makes `C-0104`'s *"the threshold is reachable"* stronger, not weaker.
3. **`C-0104`'s companion field carries the same defect.** `"dishing per radian, UNCOUPLED tile"`
   (0.265219996) is the same construction on the free tile and is quoted the same way.

## What would settle it

1. **The cheap one, and it is a doc fix.** Rename the field to what it is — a secant of the peak
   over `[0, 1 rad]` — and record the convexity argument that makes it a bound, so the next reader
   does not re-derive the triangle inequality and get a different number.
2. **Better: emit both.** One extra load-free solve gives `max|B|` and the genuine triangle
   ceiling; `T-182` emits them as `unitPrestrainPeakHere` and `trueTriangleCeilingDegrees`.
3. **A convexity assertion.** `D(θ₀)` convex in `θ₀` is a two-line test on three solves and is what
   licenses the chord; nothing in `C-0104` asserts it.

## What this challenge does **not** say

It does not say `C-0104`'s 15.4497275° is wrong, and it does not move any published verdict:
`C-0104`'s sweep reports **measured** dishings at every rung, and those are unaffected. The
threshold is the only derived quantity involved, and it is conservative in the direction the claim
needed it to be.
