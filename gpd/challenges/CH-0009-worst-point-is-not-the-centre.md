# CH-0009 — The tile's point fluctuation is quoted at its *stiffest* point, and the worst point decides the verdict

| | |
|---|---|
| **Challenges** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md), the `at a point [nm]` column of its thermal-flatness table and the §4(g)(ii) consequence built on it |
| **Raised by** | [`C-0010`](../claims/C-0010-tile-positional-variance.md), task [`T-8`](../tasks/T-8-tile-positional-variance.md) |
| **Raised** | 2026-08-12, iteration 4 |
| **Status** | **Raised.** `C-0006` is not withdrawn and none of its arithmetic is disputed. What is disputed is the **scope** of one column: it is the best case over the footprint, presented without saying so, and it is handed to a task whose predicate the worst case fails. |

---

## The standing statement being challenged

`C-0006`, "Thermal flatness — `A8.2`'s 'no floppy modes in the workspace'":

| `k_f` × | piston [nm] | tilt [nm] | dishing [nm] | **at a point [nm]** | … |
|---|---|---|---|---|---|
| 0.25 | 1.496 | 2.116 | 1.690 | **2.238** | |
| **1.00** | **0.748** | **1.058** | **1.272** | **1.365** | |
| 4.00 | 0.374 | 0.529 | 0.919 | **0.870** | |

and, downstream of it, `T-5b`'s feedback note:

> **`T-8` is re-scoped, not merely informed.** The tile's total point fluctuation at rest is
> 1.37 nm nominal and 2.24 nm at the soft end of the `k_f` sweep, against `C-0001`'s 0.28 nm
> piston-only figure and `T-8`'s 3.0 nm predicate. Still inside, but at 46–75% of the threshold
> rather than 9%.

## The contradicting result

The `at a point` column is the fluctuation at the **centre** of the footprint,
and the centre is the **stiffest** point the tile has.
At the same foundation stiffness and with the same sheet, solver and basis:

| point | fluctuation [nm] at `C-0001` at rest | ratio to centre |
|---|---|---|
| **centre** — what `C-0006` reports | **1.365** | 1.00 |
| edge midpoint | 2.139 | 1.57 |
| **corner** | **3.405** | **2.49** |

**3.405 nm is over `T-8`'s 3.0 nm predicate**, where 1.365 nm is 46 % of it.
The column that was handed forward as "the total point fluctuation" is 2.49× below
the largest point fluctuation on the same tile in the same state.

Provenance: `gpd/results/T-8-tile-positional-variance.json`,
`structure.TilePositionalVarianceStudyKt`; asserted as a test in
`src/test/kotlin/structure/TilePositionalVarianceTest.kt`
(`the worst point of the tile should exceed the predicate where the area average does not`).
`C-0006`'s own three numbers are reproduced to within 0.4 % by the same code, so this is not a
disagreement about the solve.

## Methodological grounds

Three.

### 1. The factor is structural, not incidental, and it is `√7`

It is not a numerical accident of this tile or this foundation.
For a **perfectly rigid** plate on a Winkler foundation the three surviving modes are the piston,
of stiffness `k_f A`, and the two tilts, of stiffness `k_f A/3` each.
At a point `(x, y)` the variance is therefore

&nbsp;&nbsp;&nbsp;&nbsp;`σ²(x, y) = σ₀² [ 1 + 3(2x/L_x)² + 3(2y/L_y)² ]`,

which is `σ₀²` at the centre, `4σ₀²` at an edge midpoint and **`7σ₀²` at a corner**.
The centre is the *only* point of the footprint where the tilts contribute nothing at all.
So quoting the centre is quoting the one place where two of the tile's three rigid modes are invisible —
and it is invisible **exactly because** it is the fixed point of both of them.
The free-edge dishing modes then add to this in the same direction, because Legendre modes peak at the boundary,
taking the measured ratio from `√7 = 2.646` to 2.49 (slightly below, because the dishing modes are
comparatively less peaked than the tilts).

Asserted as a closed-form gate-2 test: a rigid plate must give **exactly** 1, 2 and `√7` pistons
at centre, edge and corner.

### 2. The consequence `C-0006` draws from the column is about a *point-coupled lever*, which is where the difference bites

`C-0006` §4(g)(i) establishes that a lever with fewer than ~55 attachments
"samples the tile at *one point* and therefore over-travels relative to the tile's mean position",
and §4(g)(ii) contrasts a point-coupled lever against an area-averaging sensor.
Both arguments are about a **point**, and neither is about the centre in particular.
A tether at a tile corner — which is where a fabricator would naturally put one,
because corners are where an origami sheet has its addressable edge staples —
sees 2.5× the fluctuation the column reports.

The column is therefore not merely conservative-in-the-wrong-direction; it is being used to
support a conclusion about an arbitrary attachment point while being computed at the single most
favourable one.

### 3. It changes a verdict, and it changes it in the unsafe direction

`T-5b`'s feedback note told `T-8` the answer was "still inside, but at 46–75 % of the threshold".
On the worst point it is **114 % of the threshold** at `C-0001`'s at-rest stiffness,
and 146 % at the soft end of `C-0010`'s own bracket.
Had `T-8` consumed the column as handed over, it would have reported a comfortable pass
on a quantity that fails.

That is the difference between a number being imprecise and a number being **mis-scoped**,
and §7 asks that validity ranges travel with results and be respected downstream.
The scope "at the centre of the footprint" did not travel.

## What is *not* challenged

- None of `C-0006`'s arithmetic. Its three thermal amplitudes are reproduced to 0.4 %.
- Its headline finding, that the tile's **shape** modes carry more thermal amplitude than its
  piston mode and that the ratio grows with the foundation stiffness. `C-0010` confirms it and
  strengthens it: the ratio reaches 2.76–2.96 at the working point, against `C-0006`'s 1.70 at rest.
- Its rejection of the rigid-plate assumption, which this challenge only deepens.
- The charge-sensor offset `δ²/(2λ_D)`, which is correctly built on the **area** RMS `δ`
  and not on a point value.

## Proposed resolution

`C-0006`'s table gains a scope note rather than a correction:
the `at a point` column is the **centre** point, and it should be read as the *lower* bound of the
point fluctuation over the footprint, whose upper bound is the corner at `≈ 2.5×`.
Any downstream consumer that asks "what does a lever attached *here* see" must use the point,
not the column, and `C-0010` supplies centre, edge and corner for exactly that reason.

The programme-level lesson is narrower and sharper than "report more numbers":
**a spatially varying fluctuation has no single "at a point" value**, and naming one without
naming the point is the same class of error as `C-0001` quoting "the layer stiffness"
without naming a compression — surprise `S-1`, which the loop has now met twice in two different variables.
