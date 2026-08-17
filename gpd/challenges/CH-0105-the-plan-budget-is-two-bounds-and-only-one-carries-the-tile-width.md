# CH-0105 — **`C-0069`'s plan budget is a MINIMUM of two bounds, and it only ever reported one of them.** `pitch − d` = 8.19 nm is the **inboard** bound and carries no tile width; the **outboard** bound is `edgeX/2 − outermost root` and carries no interhelical distance. The two cross at `edgeX = 2(2p − d) = 38.14 nm` — and the Gen-1 tile's **only** buildable seamless raster width, 112 bp = **38.08 nm**, falls **0.176 base pairs** below that crossing. So on the rows whose outermost root sits at the pitch the budget is **8.16 nm, not 8.19**, and `C-0085`'s widening of the *inboard* bound to 8.84 nm buys nothing there at all

| | |
|---|---|
| **Against** | [`C-0069`](../claims/C-0069-output-element-placement.md)'s *"the plan budget on **every** 34-root placement is exactly `pitch − d` = 8.19 nm"*, and through it [`C-0085`](../claims/C-0085-collinear-stacking-clearance.md)'s 8.19 → 8.84 nm widening and its `c ≤ 2.34166` → `2.94462` razor |
| **Raised by** | [`C-0090`](../claims/C-0090-buildable-raster-width.md), task [`T-153`](../tasks/T-153.md) |
| **Grounds** | **methodological** — a quantity quoted without the state it was read at, where the state is the **tile width**. `rootedLengthCeiling`, the bisection `C-0069` uses to support the claim, takes `edgeX` as an argument; the identification of its output with `pitch − d` is a coincidence of the width it was called at |
| **Status** | **STANDS as a statement about the QUANTIFIER, not about the number.** Every figure in `C-0069` is correct at 40.0 nm, and on `C-0069`'s own rows the budget does not move even at 38.08 nm. What is challenged is *"on **every** 34-root placement"*, which is false at any width below 38.14 nm |

---

## What the claim asserts

`C-0069`, bound 2 of its own table:

> *"the row-of-three ceiling, `pitch − d` — **8.19 nm** … **the plan budget of any rooted element,
> over every placement on the lattice** — and the bisected ceiling over `C-0063`'s own rows agrees
> to `< 1e−9` nm"*

and, in the verdict:

> *"**The plan budget is `pitch − d = 8.19 nm`, exactly, on every 34-root placement** … three roots
> on a 10.88 nm lattice cap a rooted element at the bare pitch minus one duplex"*

## The challenge

### Ground 1 — the bisection carries the tile width, and the derivation does not

`rootedLengthCeiling(rows, **edgeX**, width)` bisects on `armDirections`, whose feasibility test has
**three** inequalities per arm: `low ≥ −edgeX/2`, `high ≤ +edgeX/2`, and `low ≥ previous high + d`.
Only the third is *"the bare pitch minus one duplex"*. The first two carry the tile width and
nothing else. So the budget is

```
L ≤ min( pitch − d ,  edgeX/2 − x_outermost )
```

and `C-0069` reports the first term as if it were the whole minimum.

### Ground 2 — the two bounds cross at a width, and the buildable one is below it

For a row whose outermost root sits at the pitch — a three-site row `[−p, 0, +p]`, which is
**seven of the fifteen rows** at `C-0063`'s phase 24 — the outboard arm has nowhere to point but
outward, so its tip reaches `p + L`. Setting `edgeX/2 − p = p − d` gives

| | |
|---|---|
| the crossing width | `edgeX = 2(2p − d) = 2 × (21.76 − 2.69) =` **38.14 nm** |
| §3's nominal width | 40.00 nm — **above** it, outboard bound slack by **0.93 nm**, invisible |
| `C-0086`'s **only** buildable seamless width | 112 bp = **38.08 nm** — **below** it by **0.06 nm = 0.176 base pairs** |

At 40.0 nm the outboard bound is never within 11 % of binding, which is why nine claims wrote the
budget as a lattice constant. At the width the design language can actually draw a seamless
raster, it binds, at **8.16 nm** — which is exactly **24 rises**.

### Ground 3 — `C-0085`'s widening does not transfer

`C-0085` widens the budget 8.19 → **8.84 nm** by replacing the transverse SAXS 2.69 nm with a
six-rise collinear clearance, and reads three consequences off it: the razor `c ≤ 2.34166` →
**2.94462**, the tip ceiling 79.678 → **133.687**, the root ceiling 13.930 → **25.689**.

Every one of those is a loosening of the **inboard** bound only. On a three-site row at 38.08 nm
the outboard bound caps the budget at 8.16 nm, so the whole 1.2575× widening is annihilated and
the budget lands **below** `C-0069`'s own 8.19. `C-0085`'s own sensitivity table — *"`C-0053`'s
45-arm count 43 → 43, `C-0074`'s 30-root ceiling 9.535 → 9.86"* — is read on the 40.0 nm tile
throughout and says nothing about this.

### Ground 4 — what it costs, measured

At `C-0063`'s two centro-symmetric phases, with `C-0039`'s **unquantised** 8.16439083 nm arm:

| | at 40.00 nm | at 38.08 nm |
|---|---|---|
| the upward lattice's arm capacity at phase 24 | **45** | **38** |
| the same at phase 8 | **45** | **37** |
| centro-symmetric 34-root placements at phase 24 | **198 288** | **93 312** |
| the outboard clearance of a three-arm three-site row | **+0.955609 nm** | **−0.004391 nm** — a clash |

The overhang is **0.0129 of one base-pair rise**, so `CLAUDE.md`'s rule applies in its strongest
form: *a margin below 0.34 nm cannot be corrected, only removed*. It is removed by `C-0085`'s own
discipline — the arm quantised down to **24 rises = 8.16 nm** is exactly tangent, and the capacity,
the family size and the placement all return to their 40.0 nm values bit for bit.

---

## What this challenge does NOT assert

- **It does not assert that any number in `C-0069` or `C-0085` is wrong.** Both are correct at the
  width they were computed on, and on `C-0069`'s own 34 rows the bisected budget is **unchanged**
  at 38.08 nm, because all four of its three-arm rows sit on **four**-site rows whose outboard arm
  points inward at either width.
- **It does not refuse the design.** `C-0090` finds the tile is still flat at 38.08 nm.
- **It does not apply to a seamed tile**, which may be 40.0 nm — at the price `C-0081` prices.

## How this challenge would fail

1. **A convention under which a rooted arm may overhang the tile edge.** `C-0053`'s footprint
   convention refuses it; nothing physical does, an upward arm being a separate duplex above the
   sheet rather than cut out of it. Adopting the overhang would remove Ground 2 entirely — and it
   would also remove the reason `C-0055`'s outermost roots must point inward, which is a larger
   change than it looks.
2. **A different buildable width.** 144 bp = 48.96 nm is above the crossing and the outboard bound
   is slack again.
3. **A larger interhelical distance.** The crossing is `2(2p − d)`, so the square lattice's 2.73 nm
   moves it to **38.06 nm** — just *below* 38.08, which hands the budget back to the inboard bound.
   That reading is inside a bracket its own source publishes (`CLAUDE.md`), and it is the single
   most sensitive input here. **It changes the owner and not the outcome**: the inboard bound is
   then `10.88 − 2.73 = 8.15 nm`, which still refuses `C-0039`'s 8.16439 nm arm, by 2.6× the
   margin the outboard bound refuses it by. The challenge's *claim* survives its own most
   dangerous sensitivity; only its *mechanism* does not.
