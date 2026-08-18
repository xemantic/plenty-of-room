# CH-0124 — `C-0006`'s four-layer variant is a MIXED state, not a bound, and its layer spacing is not a honeycomb's

| | |
|---|---|
| **Against** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md), the `four-layer honeycomb, rigid coupling` row of its variant table, and `structure/OrigamiSheet.kt`'s `InterlayerCoupling.RIGID` |
| **Raised by** | [`C-0109`](../claims/C-0109-four-layer-tile.md) (`T-191`), iteration 23 |
| **Grounds** | methodological — a quantity presented as a bracket end is a mixture of the two ends, one taken in each direction |
| **Status** | **OPEN** |
| **What moves** | **no verdict, table or number of `C-0006` itself** — it never solved on the variant. What moves is every downstream *use* of it, and the only one is [`C-0093`](../claims/C-0093-shared-body-coupling.md)'s four-layer brick, which is [`CH-0125`](CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md) |

## The charge

`OrigamiSheet.alongHelixRigidity` applies the parallel-axis theorem to the duplex stretch modulus under `InterlayerCoupling.RIGID`;
`OrigamiSheet.acrossHelixRigidity` deliberately does **not**, and `C-0006` says why, correctly:

> "Multi-layer coupling is deliberately **not** applied here: it would need an across-helix axial stiffness per unit width, which is a crossover property nothing in this task determines. The value is therefore a lower bound for `layers > 1`."

Each half of that sentence is right. The **pair** is not a bracket end.

Two readings *are* bracket ends:

- **`INDEPENDENT`** — no parallel axis in either direction. `D_∥` = 362.776, `D_⊥` = 19.2216 pN·nm.
- **`COMPOSITE`** — the parallel axis in both. `D_∥` = 14 310.78, `D_⊥` = 758.254 pN·nm.

The standing variant takes the **upper** value along the helices and the **lower** one across them.
Its `D_∥/D_⊥` = **744.5** therefore belongs to neither end, and it is **39.45×** the anisotropy both ends share.

## And the across-helix stiffness `C-0006` says is not determined is declared in the same file

`Gen1Tile.crossoverInPlaneStiffness` — three definitions above the variant list, `k_s = 2αS/(100a) = 64.7 pN/nm` —
is exactly the crossover property `C-0006` says nothing determines.
It is flagged there as *"a construction, not a measurement"*, which is a reason to bracket it, not a reason to set it to zero:
setting it to zero **is** picking one end of that bracket while picking the other end of the *along*-helix one.

## The identity that makes the charge sharp

Because `k_s/k_θ = S/B` under Chen et al.'s construction — `α` and the `1/(100a)` cancel —
the parallel-axis excess reduces to the **same** number in both directions:

- along the helices: `(S/d)Σy² ÷ (nB/d) = S Σy²/(nB)`
- across the helices: `(k_s d/p)Σy² ÷ (n(k_θ/p)d) = k_s Σy²/(n k_θ) = S Σy²/(nB)`

So a multi-layer sheet's **anisotropy is invariant along the whole coupling axis** — 18.873 for four honeycomb layers,
at `INDEPENDENT`, at `COMPOSITE`, and at every fraction between.
`744.5` is not a value the model can take at any interlayer coupling whatever.

This is asserted at `1e-12` in `src/test/kotlin/tile/FourLayerTileTest.kt`,
gate 3, *"the parallel-axis enhancement is the SAME factor along and across the helices"*.

## A second, independent defect: the layer spacing

`OrigamiSheet.layerSpacing` defaults to `interhelicalDistance`, so the four-layer variant stacks its rows **2.536 nm** apart.
A honeycomb array stacks its rows at `d√3/2` = **2.196 nm**.
That overstates `Σy²` by exactly `4/3`, the parallel-axis factor by **39.448 against 29.836**,
and the geometric thickness by **9.608 against 8.589 nm**.

## What follows

1. `C-0006`'s row should be read as **two** rows, `INDEPENDENT` and `COMPOSITE`, with the mixed one retained only for traceability.
2. The `≤ 744.5` anisotropy in `C-0006`'s table is a bound on nothing physical and should not be quoted.
3. `C-0006`'s `D_⊥ ≥ 19.222` **stands** as a lower bound; what does not stand is reading it *beside* a composite `D_∥`.
4. Nothing `C-0006` decided moves, because `structure/TileFlatnessStudy.kt` solves only `variants.first()`
   — the four-layer variant has never been carried into any solve at all, which is what `T-191` exists to fix.

## What would settle it

A measurement, or a simulation, of the across-helix bending rigidity of a multilayer origami slab.
`T-191`'s literature survey (`gpd/data/T-191-sources/`) found **none** — 41 EuropePMC queries,
every measured multilayer object in the literature being rod-like.
Meanwhile the *along*-helix coupling **is** measured, and `C-0109` adopts it: `f = 0.26–0.33`.
