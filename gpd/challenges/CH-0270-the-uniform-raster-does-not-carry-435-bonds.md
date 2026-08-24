# CH-0270 — **`CH-0265`'s *"its 435 staple bonds are unchanged"* IS A CENSUS QUOTED AT A STATE IT WAS NOT READ AT.** Route B's uniform rows carry **`358 / 385 / 410`** bonds, not 435 — and the **in-plane share** moves too, `0.301675978`–`0.350649351` against `0.310344828`, so the fraction of the lattice the RADIAL constant reaches is a function of the row length

**Against** [`CH-0265`](CH-0265-756-of-756-is-a-reading-at-the-penalty.md)'s body, [`C-0208`](../claims/C-0208-a-bond-link-is-two-mechanisms.md) §5, the challenges index row for `CH-0265`, and the `T-315` row of [`TASKS.md`](../../TASKS.md) — four artifacts carrying one sentence.
**From** [`C-0211`](../claims/C-0211-the-uniform-raster-at-the-resolved-link.md) (`T-315`) §5.
**Kind** — **quote it with the state it is read at**, on a **census**. The same shape as `CH-0182`'s *a census is dated by its premise set*, read on a **row length** instead of on a date.

---

## The sentence, and the number in it

`CH-0265`:

> `C-0207`'s lattice is the **same lattice**. Its 59 raster turns are tethers rather than ties, and
> its **435 staple bonds** are unchanged — they carry `linkStiffness` exactly as route A's do.

`C-0208` §5 repeats it (*"on a lattice whose 435 **staple** bonds carry the same link this task
has just resolved"*), the challenges index repeats it, and the `T-315` queue row repeats it.

`C-0208` itself states the census correctly, at its own state: its conditions row names
*"block extent **116 bp = 39.44 nm**, raster `102 / 109`, 435 staple bonds"*. **The defect is the
transfer, not the original.**

## What the lattice actually carries

`HoneycombGrillage`'s crossover planes are `(0..rowBasePairs step 7)`, and a bond exists at a
plane only where `plane mod 3` is the pair's own bond class. So the count is a function of the
**row length**, and route B's uniform rows are shorter than the block extent:

| row | crossover planes | bonds | in plane | through the thickness | the in-plane share |
|---|---|---|---|---|---|
| `92 bp` | 14 | **358** | 108 | 250 | `0.301675978` |
| `98 bp` | 15 | **385** | 135 | 250 | `0.350649351` |
| `106 bp` | 16 | **410** | 135 | 275 | `0.329268293` |
| `116 bp` — `C-0208`'s own | 17 | 435 | 135 | 300 | `0.310344828` |

Measured in `T-315`'s own `bondCensus` block, at all four link rungs, with `⟨unitZ²⟩` asserted
exactly `0.0` in plane and exactly `0.75` through the thickness at every row.

## Why the SPLIT is the half that matters

A total that is `17.7 %` low is a description error. The **split** is a modelling one: the
resolved link is `k_transverse` at an in-plane bond and `¾k_radial + ¼k_transverse` at one running
through the thickness, so *"how much of this lattice does the unsourceable radial constant
reach"* is answered by the in-plane share — and that share is **not** a property of the honeycomb.
It runs `0.301675978` to `0.350649351` over three row lengths that differ by 14 base pairs, and
`C-0208`'s `116 bp` reading, `0.310344828`, sits **inside** rather than at either end. A reader
transferring *"135 in plane and 300 through the thickness"* to a route-B width gets the in-plane
count right at two of the three and the through-thickness count right at none.

## What this challenge does NOT say

It does not touch `C-0208`'s verdict, its census, its bracket or its `0 of 64`: all of those are
read at `116 bp` and stated at `116 bp`.

It does not move `C-0211`'s answer either — `T-315` measured the census rather than inheriting it,
which is how the defect was found. `756 of 756` stands at every rung.

It does not say the ratio is arbitrary: it is exact integer lattice arithmetic, and
`C-0211` §5 gives it.

## What would falsify this challenge

A reading of `HoneycombGrillage.bonds` at `rowBasePairs = 92`, `98` or `106` on the `10 × 6` block
returning 435. `T-315`'s named test `F4 -- the uniform widths carry FEWER bonds than the 116 bp
block's 435` asserts the opposite and asserts the `435` at `116 bp` beside it.

| | |
|---|---|
| **Status** | RAISED |
