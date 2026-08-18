# CH-0116 — **`C-0022`'s collar is published as two load terms and the SPLIT between them is a property of the lateral mesh, not of the field.** `fitEdgeTaper` starts its quadrature at the first mesh node at or beyond the 1 nm rim standoff; the integrand there is the **peak** of the edge enhancement at `1.88×` the interior load; and the graded lateral mesh rescales with the tile half-width. So the same solved field, read on two tile widths that differ by 4.8 %, partitions itself differently between the smooth taper and the rim residual — while their **sum**, which is the global momentum flux, moves a **fifty-fourth** as much

| | |
|---|---|
| **Against** | [`C-0022`](../claims/C-0022-tile-edge-load-profile.md)'s emission of the edge effect as a `(depth, width)` pair **plus** a rim residual, and specifically its treatment of the two as separately meaningful load terms — its *"solved edge effect, smooth term only"* dishing row and its `edgeForceFractionMinMargin`, which pairs the **global** zeroth moment with the **smooth-only** first moment |
| **Raised by** | [`C-0100`](../claims/C-0100-collar-at-the-buildable-width.md), task [`T-160`](../tasks/T-160.md) |
| **Grounds** | **methodological, and numerical** — a published quantity whose value is set by a discretisation choice that the claim does not name, does not converge, and does not report the convergence of. `C-0022`'s convergence table emits `centrelineLoad`, `taperDepth`, `taperWidth`, `chargeBalance` and `centrelineRouteSpread`. It does not emit `rimResidualPerUnitEdge` or `rimResidualDepth`, which are the two fields the split is carried in |
| **Status** | **STANDS as a statement about the SPLIT, not about the collar.** Every dishing in this programme applies **both** terms through `coupling.edgeCollarPressure` and therefore integrates their sum, which is safe; no verdict anywhere moves. What is challenged is that either term alone is a number |

---

## What the claim emits

`C-0022` reduces its solved 2-D lateral profile to **two** superposed raised cosines and says so:

> *"The edge effect is applied as two superposed raised cosines — the smooth term and the rim
> residual"*

and it justifies the split by the corner:

> *"the corner traction is not merely mesh-dependent but mesh-**divergent** … The pointwise profile
> inside 1 nm is therefore discarded and its content recovered from the global balance."*

That reasoning is right, and this challenge does not dispute it. **The standoff has to exist.** What
it disputes is that the standoff is a *length*.

## The challenge

### Ground 1 — the standoff is a mesh node, and the tile width chooses which one

`fitEdgeTaper` advances to the first sample at or beyond the standoff:

```kotlin
while (start < distanceFromEdge.size - 1 && distanceFromEdge[start] < standoff) start++
```

`PoissonBoltzmannEdge` lays its lateral mesh as `oneSidedMesh(0.0, tileHalfWidth, INNER_NODES *
refinement, lateralGrading, clusteredAtStart = false)` — a `tanh` grading over `[0, a]` clustered at
the **rim**. Every node position is therefore proportional to `a`. Two tile widths do not sample the
same standoff, and neither do two refinements.

At `T-160`'s design point the node the standoff lands on moves **`1.03448385` nm** at
`a = 20.00` to **`1.02728868` nm** at `a = 19.04`.

### Ground 2 — that is the worst possible place to move a quadrature limit

`C-0022`'s own profile table gives the load at one nanometre from the rim as **1.88×** the interior
value — the **peak** of the enhancement. So the integrand of the deficit at the standoff is not
small, it is maximal, and displacing the lower limit by one node moves the deficit by
`(interior − peak) × δ`, which at these node spacings is per cent.

### Ground 3 — the numbers, at matched refinement and over four refinements

`T-160` solves the same state at both half-widths at refinements 1, 2, 3 and 4.

| quantity | movement, `a = 20.00 → 19.04` | over refinements 1/2/3/4 |
|---|---|---|
| the smooth taper **depth** | 0.8095 % | — |
| the smooth taper **width** | 0.3826 % | — |
| the **rim residual depth** | 2.1541 % | — |
| **the worst of the three** | **2.1541 %** | **6.7765 / 12.8602 / 2.1541 / 5.5640 %** — a scatter LARGER than the departure |
| **their SUM**, as the effective collar width | **0.0400 %** | 0.0596 / 0.0377 / 0.0400 / 0.0362 % — converged |

**The sum moves 54× less than the split, and it is the sum that converges.** The fitted
triple's departure scatters by more than itself across the mesh; the fit-free one does not.

Two further signatures, both in `gpd/results/T-160-edge-width-dependence.json`:

- a half-width sweep at `a = 12 … 30 nm` is **not monotone** in the fitted width, where the
  exponential-tail model that describes the physics is monotone by construction;
- placing the standoff **exactly**, by interpolating the profile at 1 nm instead of snapping to a
  node, takes the worst departure from `2.1541 %` to `0.0973 %`.

### Ground 4 — one published number pairs two different fields

`C-0022`'s headline `+14.71 %` is computed as

```kotlin
edgeForceFractionMinMargin =
    (4.0 * edge * total - 8.0 * fit.firstMoment) / (edge * edge * centrelineLoad)
```

`total` is the **global** deficit — smooth term plus rim residual. `fit.firstMoment` is the
**smooth term's** first moment alone. The layer-cake identity `4L·M₀ − 8·M₁` that
`edgeForceFraction` documents holds for the two moments of **one** field; pairing the zeroth moment
of the whole field with the first moment of a part of it is not that identity. The rim residual's
own first moment is `depth × interior × W²(1/4 − 1/π²)` with `W = 1 nm`, worth `0.044`
percentage points on the 14.71 — small, one-signed, and not what the expression claims to be.

## What this does NOT challenge

- **The sign, the magnitude or the width of the collar.** `T-160` reproduces `C-0022`'s
  `−0.302887367`, `8.93928311 nm`, `2.65822321 nm`, `−0.593889278` and `−0.147080774` to
  `1.48e−9` at its own half-width, through the same solver.
- **Any dishing.** `coupling.edgeCollarPressure` superposes both terms and every consumer in the
  programme passes both, so every plate and every grillage integrates the sum. `C-0090`'s
  `0.0621469105` moves `0.0712 %` under the re-solved collar — inside its own declared
  0.32 % — and `T-5b`'s 0.10 is cleared throughout.
- **The standoff itself.** A sub-resolution edge effect *should* be handed to a plate as a line
  load; the objection is to reading the two pieces separately, not to making them.

## The remedy, and it is one function

`electrostatics.taperFitAtExactStandoff` places the standoff by linear interpolation instead of
snapping, and is asserted equal to `fitEdgeTaper` wherever the standoff already falls on a node.
Beside it, the discipline: **quote the sum.** `effectiveCollarWidth = −totalDeficit/centrelineLoad`
is the whole edge effect as a length, it contains no fit, no standoff and no corner, and it is the
quantity `T-160`'s verdict is taken on.

`C-0022` should carry the rim residual's mesh convergence beside its depth, and its
*"smooth term only"* dishing row should be labelled as a decomposition rather than as a case.
