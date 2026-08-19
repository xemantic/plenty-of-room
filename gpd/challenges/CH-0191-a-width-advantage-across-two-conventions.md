# CH-0191 — `C-0140`'s *"`−1.40 %` beats the square lattice's `−4.80 %`"* reads a **bounding box** against a **row length**. A uniform square-lattice raster has no stagger, so its two readings coincide and the honeycomb's do not: on the **row length** both tiles are **112 bp = 38.08 nm** and the advantage is **exactly zero**, and on the **bounding box** the honeycomb wins by **1.36 nm — which IS the stagger**, i.e. it wins by being ragged

| | |
|---|---|
| **Against** | [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) §4 — *"the recommendation is **112 / 108 bp**, and its `−1.40 %` **beats the square lattice's 38.08 nm at `−4.80 %`** and `C-0133`'s twist-corrected 37.40 nm at `−6.50 %`"* — and both outward-facing documents, which carried the comparison unqualified |
| **Raised by** | [`C-0149`](../claims/C-0149-ninth-answers-synthesis.md) (`T-240`, `T-242`) |
| **Kind** | **methodological** — a comparison between two objects taken on two different functionals of them, which is `CLAUDE.md`'s own *quote it with the state it is read at* applied to a **width convention** |
| **Status** | **OPEN. No verdict moves and no number of `C-0140`'s is wrong**; what moves is what the sentence means. Both deliverables now carry the qualification |

---

## The ground

[`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) §1 establishes that a
two-length honeycomb raster has **two** widths and that they differ by a **stagger**:

| | honeycomb two-length raster | square-lattice seamless raster |
|---|---|---|
| **row length** — the span every x-raster row has | **112 bp = 38.08 nm** (`−4.80 %` of 40.0) | **112 bp = 38.08 nm** (`−4.80 %`) |
| **bounding box** — the extent the folded object occupies | **116 bp = 39.44 nm** (`−1.40 %`) | **112 bp = 38.08 nm** (`−4.80 %`) |
| stagger between the two | **4 bp = 1.36 nm** | **0** — every row is the same length |

`C-0140` compares its **bounding box** against the square lattice's **row length**.
The square lattice's two readings are the same number, which is exactly why the mismatch is
invisible: there is no second square-lattice figure to notice is missing.

> **On either single convention the honeycomb's width advantage over the square lattice is
> `0.00 %` or is the stagger.** It is never `−1.40 %` against `−4.80 %`.

## Why this is not a quibble

The two conventions are not interchangeable **downstream**, and the corpus already picks one:

- **Every four-layer plan ceiling, packing verdict and coupled flatness cell outside `C-0146` is
  computed at the row-length reading, 38.08 nm** — `C-0141`'s bisection (whose 10-path ceiling
  *is* 38.08 nm), `C-0142`'s sixteen cells, and `C-0147`'s plan-budget table, which consumes
  `C-0141`'s ceilings unchanged.
- **Both deliverables reported the footprint at the bounding-box reading, 39.44 nm**, in the same
  paragraph as those margins.

So the documents were quoting the tile's **size** on one functional and its **margins** on another,
with nothing saying so. That is the substance; the `−4.80 %` sentence is how it got there.

## What would close it

- **A stated convention.** Which reading §3's *"40 × 40 nm"* names is a specification question and
  is now decision **8** of [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) / `T-242`. Once it
  is answered the comparison can be re-taken on the answer and will be a real number.
- **Or a both-conventions table** wherever a width is compared, which is what this challenge
  installed in the two deliverables as an interim.

## What it does NOT establish

- **`C-0140`'s recommendation does not move.** Its selection rule minimises `|extent − 40 nm|`
  within a family of honeycomb rasters, all of which are read on the **same** convention as each
  other; the cross-convention step is only in the sentence comparing the winner to a *square*
  lattice. (The recommendation is under a separate and unrelated challenge,
  [`CH-0187`](CH-0187-the-two-length-recommendation-rests-on-an-unstated-filter.md).)
- **Nothing here says the square lattice is competitive.** It is refused on `C-0136`'s twist and on
  `C-0141`'s cross-section long before a width is compared; this challenge is about a **sentence's
  meaning**, not about a lattice choice.
- **`C-0133`'s twist-corrected 37.40 nm at `−6.50 %`** is quoted by `C-0140` in the same breath and
  is **also** a single-layer square-lattice row length, so it carries the identical mismatch.
