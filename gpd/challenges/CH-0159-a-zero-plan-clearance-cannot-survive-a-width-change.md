# CH-0159 — `C-0090`'s 24-rise arm is **exactly tangent** at 38.08 nm, and a design change of **one base pair** in the row length empties the whole 34-root family

| | |
|---|---|
| **Against** | [`C-0090`](../claims/C-0090-buildable-raster-width.md), Deliverable on the arm ceiling, and [`C-0085`](../claims/C-0085-collinear-stacking-clearance.md)'s quantisation of the arm to 24 rises |
| **Raised by** | [`C-0133`](../claims/C-0133-twist-corrected-raster-row.md) (`T-189`) |
| **Kind** | **methodological** — a margin reported as *exactly zero* and read as *sufficient*, with no tolerance stated on the variable it is zero in |
| **Status** | **OPEN, and it costs one base pair rather than a verdict.** `C-0090`'s arithmetic is right at 38.08 nm; what is challenged is that a zero clearance was carried as a design rather than as a knife edge, and the first design change the programme actually contemplated fell off it |

---

## The ground

`C-0090`, read directly:

> *"`C-0039`'s **8.16439083 nm** root overhangs a three-arm three-site row by **0.00439083 nm**,
> takes the phase-24 capacity 45 → 38 and halves the symmetric family (198 288 → 93 312) … `C-0085`'s
> quantisation to **24 rises** makes the clearance exactly zero and restores every one of those
> numbers bit for bit."*

`CLAUDE.md` records the same fact twice more, as *"the two crossing at `edgeX = 2(2p − d) = 38.14 nm`
with 38.08 falling **0.176 base pairs** below"* and as *"a margin below 0.34 nm cannot be corrected,
only removed"*.

## What `T-189` measured

A twist-corrected seamless row is **110 bp = 37.40 nm** (`C-0133`), two base pairs narrower. Every
upward station moves inboard by up to 0.34 nm, and at `C-0090`'s own 24-rise arm the exhaustive
centro-symmetric family of **34 roots at 2–3 per row is empty** — not degraded, empty. Walking the
arm down one quantum restores it:

| row | width | largest arm admitting 34 roots | family |
|---|---|---|---|
| 112 bp (`C-0086`, `C-0090`) | 38.08 nm | **24 rises = 8.16 nm** | 163 296 |
| 110 bp (twist-corrected) | 37.40 nm | **23 rises = 7.82 nm** | non-empty, re-enumerated in `C-0133` |

So the plan budget's sensitivity to the row length is **one base pair of arm per two base pairs of
row**, and the arm is the length `C-0034`/`C-0039`'s placement condition is written on: a 4.2 %
shorter arm is not a cosmetic change to a flexure whose stiffness goes as `r^(−3)`.

## Why the *"exactly zero"* framing is the problem

A clearance of exactly zero is reported by `C-0090` as a *restoration* — the quantisation buys back
the whole family — and it is, at that width. But zero clearance means the derivative of feasibility
with respect to every geometric input is undefined on one side: the family is full at `edgeX =
38.08` and empty at `edgeX = 38.08 − 2·rise`. Nothing in `C-0090` or `C-0085` says which side a
perturbation lands on, because there is no perturbation to land on when the margin is zero.
`CLAUDE.md`'s *"a margin below 0.34 nm cannot be corrected, only removed"* is the right reading of
it, and the design tables do not carry it.

## What would settle it

Quote the arm with the **width it was placed at**, and quote the largest arm quantum that admits the
demanded root count as a function of the width — one `.any()` per quantum, no solve. `C-0133`
computes it for two widths; it is four lines and it turns a knife edge into a table.
