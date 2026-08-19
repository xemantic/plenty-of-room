# CH-0158 — `C-0086`'s *"the buildable widths are the odd multiples of 16 bp"* is a statement about a sheet built at **33.75 °/bp**, and the design twist is a **design variable**, not a constant

| | |
|---|---|
| **Against** | [`C-0086`](../claims/C-0086-seamless-scaffold-routing.md), Deliverable 4, and every claim written on the 112 bp row it selects — [`C-0090`](../claims/C-0090-buildable-raster-width.md), [`C-0104`](../claims/C-0104-row-end-prestrain.md), [`C-0107`](../claims/C-0107-row-end-prestrain-value.md), [`CH-0101`](CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md) |
| **Raised by** | [`C-0133`](../claims/C-0133-twist-corrected-raster-row.md) (`T-189`) |
| **Kind** | **methodological** — a quantisation derived with one of its two inputs held at a value the same programme records as a defect |
| **Status** | **OPEN, and it does not overturn `C-0086`'s theorem or its arithmetic.** Both are correct *given* a 33.75 °/bp lattice. What is challenged is that the list is presented as a property of the **sheet** when it is a property of the **(sheet, design twist)** pair, and the second entry is exactly what `C-0107`'s row-end prestrain is a complaint about |

---

## The ground

`C-0086` Deliverable 4, read directly:

> *"A boustrophedon has **only** progressive crossovers, and its successive scaffold crossovers are
> the two ends of one row — so the constraint binds the **row length**. At the 1.5-turn (16 bp)
> spacing this sheet is built on, three half-turns are 16 bp, so an odd number of half-turns is an
> **odd multiple of 16 bp**"*

The step *"three half-turns are 16 bp"* is true **iff** a half turn is 16/3 bp, i.e. iff the design
twist is `540/16 = 33.75 °/bp`. That is the square lattice's nominal value, and it is the very
quantity `C-0107` measures a **17.15–24.98°** row-end prestrain from, because B-DNA prefers
`34.2857 °/bp`. `C-0086` itself flags the gap in its validity range — *"the twist correction
Rothemund's program applies [is] not [checked]"* — and then the list is carried forward by four
claims as a lattice constant.

## What changes if the premise is dropped

At B-DNA's own twist a half turn is `5.25 bp` and the admissible row lengths are the odd multiples of
it: `5.25, 15.75, 26.25, …` — **none of them an integer**, which is `C-0133`'s incompatibility
theorem. The *approximable* list, taking the nearest integer at each odd half-turn count that a
1.5-turn column pitch can carry, is

| domains `D` (odd) | half turns `q = 3D` | row length | width |
|---|---|---|---|
| 1 | 3 | 16 bp | 5.44 nm |
| 3 | 9 | **47 bp** | 15.98 nm |
| 5 | 15 | **79 bp** | 26.86 nm |
| 7 | 21 | **110 bp** | **37.40 nm** |
| 9 | 27 | **142 bp** | 48.28 nm |

**112 bp is not on it.** Three of the five entries are not on `C-0086`'s list either, and the two
lists intersect only at 16 bp. So *"the buildable widths"* is not one set: it is one set per design
twist, and the programme has never chosen which twist the Gen-1 tile is built at.

## Why this matters rather than being a quibble

1. `C-0090`'s central result — that 38.08 nm is `7 × 5.44` **exactly**, which is what collapses
   `C-0015`'s ten eight-column phases to two — is an identity in the 16 bp pitch. On a twist-corrected
   row the pitch is not uniform (five 16 bp domains and two 15 bp), so the phase argument has to be
   re-made, not inherited.
2. `C-0107`'s 17.15–24.98° prestrain is the **cost of not correcting**, and `C-0133` shows the
   correction takes the row end to `−3.56…−2.19°`. A claim that prices a defect and a claim that
   quantises the width on the assumption of that defect cannot both be carried unqualified.
3. The correction is not exotic: it is what Snodin et al.'s measured tile does (*"a suitable number
   of sections with 31 base pairs between equivalent junctions"*) and what Rothemund's own design
   program does (*"helical domain lengths … by single bases"*). The uncorrected sheet is the
   unusual choice.

## What would settle it

A specification decision: **is the Gen-1 sheet twist-corrected?** If yes, the row is 110 bp and every
plan, phase and placement result written on 38.08 nm is re-read (`C-0133` re-reads the flatness and
the plan; the phase census is not re-read here). If no, `C-0086`'s list stands and `C-0107`'s
prestrain travels with it as a standing exposure. What is **not** available is to carry the width
from one answer and the prestrain from the other.
