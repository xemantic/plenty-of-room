# CH-0005 — The tile is not a rigid plate, and `C-0001` is a spatial average

| | |
|---|---|
| **Challenges** | [`C-0001`](../claims/C-0001-layer-stiffness.md), its `Rigid tile` validity bullet and the `σ_RMS` column of its headline table |
| **Raised by** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md), tasks [`T-5`](../tasks/T-5-load-distribution.md) and [`T-5b`](../tasks/T-5b-tile-flatness.md) |
| **Raised** | 2026-08-12, iteration 3 |
| **Status** | **UPHELD.** `C-0001` is not withdrawn; its arithmetic stands, one of its validity bullets is now answered rather than open, and the answer is *no*. |

---

## The standing statement being challenged

`C-0001`, validity range:

> **Rigid tile.** Assumed, not shown. `T-5b` is what tests it, and if the tile dishes, the tile
> no longer samples a single `h` and this whole claim is a spatial average.

`C-0001` names the test and names the consequence. This challenge is that test returning, and
returning against the assumption. It is filed as a challenge rather than an annotation because
`C-0001` also *uses* the assumption quantitatively — its `σ_RMS` column is a single-degree-of-freedom
equipartition result that only means what it says if the tile has one degree of freedom.

## The contradicting result

For the §3 tile on `C-0001`'s own foundation stiffness:

- the Winkler length is **`ℓ_∥ = 9.07 nm` and `ℓ_⊥ = 4.03 nm` against a 20 nm half-width**,
  so `ℓ/L = 0.45` and `0.20`. The tile cannot bridge its own width in either direction, and it
  contains **43.7 mechanically independent patches**;
- the tile's **thermal shape fluctuation is 1.272 nm RMS at 300 K**, which is **1.70×** its
  rigid-body piston fluctuation of 0.748 nm and **26% of the 4.95 nm stroke**;
- under a discretely anchored load it dishes by **50% of the stroke**; under a single
  concentrated lever attachment, by **369%**, which exceeds the layer height.

Provenance: `gpd/results/T-5-load-distribution.json`, `gpd/results/T-5b-tile-flatness.json`,
`structure.TileLoadDistributionStudyKt`, `structure.TileFlatnessStudyKt`, tests green.

## Methodological grounds

Three, in increasing order of seriousness.

### 1. The rigid-plate assumption survives exactly one load case, and it is the idealised one

A uniform load on a uniform Winkler foundation makes a free plate translate **exactly**,
whatever its rigidity. So `C-0001`'s rigid-tile assumption is *harmless* for the load case
`C-0001` actually models — a uniform osmotic pressure against a uniform tile.

But it is harmless **for a reason `C-0001` does not state and cannot rely on**: not because
the tile is stiff, but because a uniform load needs no stiffness. Every departure from perfect
uniformity — discrete anchoring (§4(g) says the tile *is* anchored at discrete points), a
finite-tile electrostatic edge effect, or a concentrated output coupling — dishes the tile by
27% to 369% of the stroke. `C-0001` got the right answer for the wrong reason, and the reason
does not generalise to anything downstream of it.

### 2. The `σ_RMS` column is a one-degree-of-freedom result for a structure that has many

`C-0001` reports `σ_RMS` = 0.16 / 0.19 / 0.28 nm from `√(k_BT/k)` on the layer stiffness, and
hands it to `T-8`. That is the **piston** mode alone. On the same foundation the free tile also has

- two rigid **tilts**, each *softer* than the piston (stiffness `k_f A/3`), contributing
  **1.058 nm** RMS area-averaged, and
- a spectrum of **shape** modes contributing **1.272 nm** RMS,

giving a total point fluctuation of **1.365 nm** at rest — nearly **five times** the number
`C-0001` reports, and 46% of `T-8`'s 3.0 nm acceptance threshold rather than 9%. At the softer
end of the `k_f` sweep it reaches **2.24 nm**, i.e. 75% of the threshold.

This is not a correction to `C-0001`'s arithmetic. It is a statement that the quantity
`C-0001` computed is not the quantity `T-8` needs, and `T-8` must not consume it as though
it were.

### 3. The layer no longer sees one height, and `Π(h)` is convex

With a thermal shape fluctuation `δ = 1.272 nm` about a mean height `h = 10 nm`, and the local
osmotic exponent `m_eff = 1.672` from `C-0002`,

&nbsp;&nbsp;&nbsp;&nbsp;`⟨Π⟩ / Π(⟨h⟩) ≈ 1 + m(m+1)/2 · (δ/h)² = 1.036`.

The layer pushes back **3.6% harder** than a flat-tile model says, so `C-0001`'s strokes are
**3.6% optimistic** from this effect. Small — and worth stating precisely because it runs
**opposite** to `CH-0001`'s correction, which makes the layer softer. The two do not cancel;
`CH-0001`'s is the far larger of the two. But it means the rigid-tile assumption is not merely
unproven, it is quantitatively wrong in a stated direction and by a stated amount, which is
what a validity bullet is for.

## What follows, and what does not

**Does not follow.** That `C-0001`'s numbers are wrong. Its stiffnesses, strokes and design
window are computed for a uniform load on a uniform layer, where the rigid-plate assumption is
*exact*, and they stand as computed. The `T-1` acceptance predicate — a number with a stated
model, parameters and validity range — remains discharged.

**Does follow.**

1. **The `Rigid tile` validity bullet is answered, and the answer is no.** It should be
   rewritten from "assumed, not shown" to "shown false for every non-uniform load case;
   exact only for the uniform one".
2. **`C-0001`'s `σ_RMS` column must not be consumed by `T-8` as the tile's positional variance.**
   It is the piston mode alone. The tilts and shape modes together dominate it by 4×.
   `T-8`'s preliminary entry in `TASKS.md` already says "this is the layer-normal DOF only —
   tilt, lateral and internal modes are not in it"; this challenge supplies those modes and
   they are not small.
3. **`C-0001`'s strokes carry a +3.6% stiffening correction** from sampling a convex `Π(h)` at a
   distribution of heights, opposite in sign to `CH-0001`'s softening.
4. **Any downstream task that couples the tile to a *point*** — a lever, a tether, a charge
   sensor at one location — **is outside `C-0001`'s domain entirely** and must use `C-0006`.
   `T-3` and `T-4` in particular must not treat "the tile displacement" as a single number if
   the force is delivered anywhere other than uniformly.

## Resolution

`C-0001` is **not withdrawn and not overwritten**. Its `Rigid tile` bullet is annotated in place
with a pointer here, per the no-overwrite rule. The numbers stay; what changes is the scope over
which they mean anything.

**Outstanding, and queued:**

- `T-8` must be re-scoped to include tilt and shape modes, which `C-0006` supplies directly —
  the total point RMS at rest is 1.37 nm nominal and 2.24 nm at the soft end of the `k_f` sweep,
  both inside 3.0 nm but not by the margin `C-0001` suggested.
- `T-1c`, which is re-deriving `k_f`, should emit the stiffness **at first contact** as well as
  the secant, because the thermal verdict here flips at `k_f ≈ 0.30 × C-0001` and the flip is
  decided by the at-rest number, not the secant.

## If this challenge is itself wrong

The way it fails is through the crossover hinge constant `k_θ`, which is the single largest open
premise under `C-0006`. It rests on Chen et al.'s `k_θ = 2αB/(100a)`, in which only `α` was
fitted and the factor `1/100` is carried over from CanDo's *nick* softening rather than measured
for a crossover. If the true crossover is ~30× stiffer than that — enough to bring `D_⊥` up to
`D_∥` — then `ℓ_⊥` rises by 2.3× to 9.3 nm, still below the half-width, and the thermal dishing
falls but does not vanish. **Grounds 1 and 2 survive that; ground 3 weakens.** Nothing in the
accessible literature measures a crossover hinge constant directly, and an oxDNA calculation of
one is the queue item that would settle it.
