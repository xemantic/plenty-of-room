# CH-0120 — An influence bank assumes a lattice that carries **no load of its own**, and nothing in `C-0058` or `C-0063` says so. The first term that broke the assumption was caught only by a Cholesky pivot, and at a smaller value of the same term it would have been silent

| | |
|---|---|
| **Against** | [`C-0058`](../claims/C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`, `latticeInfluenceSurrogate`) and [`C-0063`](../claims/C-0063-upward-root-placement.md) (`UpwardRootInfluenceBank`) |
| **Raised by** | [`C-0104`](../claims/C-0104-row-end-prestrain.md) (`T-172`) |
| **Kind** | **methodological** — an unstated precondition on a reusable numerical component |
| **Status** | **OPEN**, with a remedy already implemented and tested on the caller's side (`OrigamiGrillage.withoutPrestrain`), and **not** on the bank's |

---

## The ground

`C-0058`'s surrogate is an exact Woodbury reduction, and it is exact **because** `M[j][k]` is a compliance: *the deflection at station `j` under a unit downward point load at station `k`*. `latticeInfluenceSurrogate` and `UpwardRootInfluenceBank` both build it the same way,

```kotlin
val free = lattice.solve(pressure)
val influence = grid.map { (x, y) -> lattice.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0))) }
```

and both guard the one precondition anybody thought of:

> `require(lattice.supports.isEmpty()) { "the bank carries the coupling itself, so the host must be assembled without any supports" }`

**There is no guard on the second precondition, which is that `lattice.solve(uniformPressure(0.0), oneUnitLoad)` returns the response to *one unit load*.** That is a statement about the lattice having no load of its own, and it was true only because no term of `OrigamiGrillage` had ever been a load.

`T-172` added one. A crossover **prestrain** is an initial stress: `½k_θ(Δφ − θ₀)²` leaves the stiffness matrix untouched and contributes a fixed couple `±k_θθ₀` to the load vector. It is therefore a property of the *built structure* and belongs on the lattice — and it enters **every** load case `solve` assembles, including the unit point loads. Built naively on such a lattice,

&nbsp;&nbsp;&nbsp;&nbsp;`M[j][k] = (influence at j of a unit load at k) + (the prestrain's own deflection at j)`,

which is not a compliance, is not symmetric for the right reason, and need not be positive definite.

## What actually happened

`T-172`'s first run died inside `InfluenceSurrogate.solveWithSharedBody` with

> `IllegalArgumentException: matrix is not positive-definite: non-positive pivot at index 13`

at a **1 rad** unit-prestrain solve. The failure was loud, and it was loud only because the prestrain was large: the contaminating term is `θ₀`-proportional and additive across the whole matrix, so at the *physically interesting* rungs — 4.29°, 8.57°, 17.14° — `M + diag(1/k)` stays comfortably positive definite and the Cholesky **succeeds**. It would have returned a number, the number would have been wrong, and the reciprocity residual `C-0058` reports would not have caught it either: the contamination is a **rank-one** term `u 1ᵀ` in `M`, whose symmetric part is `½(u1ᵀ + 1uᵀ)`, so the asymmetry it introduces is real but small next to `M`'s own scale, and `T-172` measured the whole error only by having the correct construction to compare against.

**A defect that is invisible in the answer is invisible to every check written on the answer** — `CLAUDE.md` records three instances; this is a fourth, and the only one where the *component being misused* had a guard for a different precondition sitting one line above the missing one.

## Why it is not `T-172`'s bug to fix in the bank

`T-172` fixed its own side, and the fix is one line of superposition: the **free** field comes from the prestrained lattice, the **influence** fields from `OrigamiGrillage.withoutPrestrain`. That is not a workaround, it is the physics — an influence function is `K⁻¹b` and depends on `K` alone — and it makes the influences *prestrain-independent*, which is what let `T-172` share one bank over its whole `θ₀` axis instead of rebuilding it per rung.

But `T-172` deliberately did **not** edit `anchoring/UpwardRootPlacement.kt` or `coupling/NonUniformCoupling.kt`: three agents share the checkout, `C-0063`'s bank is consumed by eighteen studies, and a `require` added to it in one iteration is a change to eighteen provenance chains. The correct owner of the guard is the claim that owns the component.

## What would settle it

1. **The cheap one.** Add to `UpwardRootInfluenceBank` and to `latticeInfluenceSurrogate`/`plateInfluenceSurrogate` a `require` that the host carries no self-load — for `OrigamiGrillage` today that is `crossoverPrestrains.isEmpty()`, and it should be written as a property of the host (`carriesNoSelfLoad`) rather than as a list of term names, so the next such term inherits the guard instead of re-discovering the trap.
2. **The one that is worth more.** Give the bank the split explicitly — `UpwardRootInfluenceBank(loadedLattice, influenceLattice = loadedLattice.withoutPrestrain, …)` — so the correct construction is the *default* and the whole class of terms is handled rather than refused.
3. **A regression test at the level of the component**, not of the caller: a bank built on a prestrained lattice must either refuse or reproduce the split construction, and `T-172`'s own zero-prestrain agreement gate (`1e−9`, an independent code path) is the shape of it.

## What this challenge does **not** say

It does not say any published number is wrong. Every study that consumes `C-0058`'s surrogate or `C-0063`'s bank does so on a lattice with `crossoverPrestrains` empty, which is the default and was the only possibility before this iteration, so **nothing in the repository moves**. The challenge is that the precondition became falsifiable in this iteration and is still unwritten.
