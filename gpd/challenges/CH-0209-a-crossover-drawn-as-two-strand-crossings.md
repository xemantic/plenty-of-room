# CH-0209 — **the field's own reference implementation draws a Rothemund crossover as TWO strand crossings, at adjacent offsets, on every interface** — so the premise this corpus's simulated `.sc` was built on, *"a Rothemund staple crossover is a SINGLE strand crossing"*, is contradicted by `scadnano.origami_rectangle`, and the two cases the corpus reasoned about (one crossing, or two at the **same** offset) do not cover the case the field actually draws. The corpus's own sheet carries **one crossing per 32 bp per interface** where the reference rectangle carries **a pair**; `checkBuildability`'s `noSiteIsCrossedTwice` predicate cannot see the difference, because it keys on the exact offset; and read the two ways the same imported design is **90 lattice sites or 45**, moving its flatness by `1.087×`

| | |
|---|---|
| **Against** | [`C-0157`](../claims/C-0157-crossover-hinge-constant-from-oxdna.md)'s simulated design and the ground stated for it in [`tools/oxdna/gen1_tile_design.py`](../../tools/oxdna/gen1_tile_design.py) — *"A Rothemund staple crossover is a SINGLE strand crossing"* — and [`C-0160`](../claims/C-0160-scadnano-writer.md)'s `noSiteIsCrossedTwice` buildability predicate, which enforces that sentence |
| **Raised by** | [`C-0161`](../claims/C-0161-mechanics-on-an-imported-design.md) (`T-267`) |
| **Grounds** | **in-silico / logical** — a census of two committed `.sc` files, one of them emitted by the reference implementation (scadnano 0.21.1) and read by this repository's own reader; plus the two grillages the two readings produce, solved under one load case |
| **Status** | **OPEN** — filed, not repaired. Nothing here says `C-0157`'s `k_θ` bracket is wrong; what it says is that the bracket was measured on **one** of two motifs and the corpus has no measurement on the other |

---

## The observation

`tools/oxdna/gen1_tile_design.py` states its own ground in a comment, and it is a claim about
Rothemund's motif rather than about this design:

> A Rothemund staple crossover is a SINGLE strand crossing. Registering the
> site from BOTH sides puts two reciprocal crossings at the same base
> offset, which is geometrically over-constrained — the two backbones would
> have to face each other simultaneously in opposite senses — and it does
> not relax: minimisation stalls with those bonds at 1.56 oxDNA units where
> the FENE is only defined out to 1.00, so the real potential then diverges.

Both halves are supported. What the sentence does not consider is a **third** arrangement, and it
is the one `scadnano.origami_rectangle.create` — the reference implementation's own canonical
Rothemund rectangle, in its own repository, written by its authors — emits at every interface:

| design | interface `(0, 1)`, crossing offsets | per 32 bp |
|---|---|---|
| `gpd/designs/gen1-sheet-square-15x112.sc` (this corpus, 112 bp) | `8, 40, 72, 104` | **one** |
| `gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc` (reference, 128 bp) | `31, 32, 63, 64, 127, 128` | **a pair, at `o` and `o+1`** |

The pattern is not an edge effect and it is not the seam. Every one of the rectangle's fifteen
interfaces carries its crossings in adjacent pairs — the even-lower interfaces at
`31/32, 63/64, 127/128` and the odd-lower ones at `47/48, 79/80, 111/112` — which is **90** strand
crossings over **45** junction positions.

The count is not this repository's arithmetic. `tools/scadnano/validate-sc.py` loads the file in
the **reference implementation** and counts with its own parser: **90 staple crossings** on the
rectangle against **49** on this corpus's sheet, both with zero warnings.

Two antiparallel crossings between one duplex pair at adjacent base positions are not two
crossings at the *same* offset, so the failure `C-0157` measured does not apply to them; and they
are not one crossing, so the corpus's own design is not what the reference generator draws.

## Why it matters, in three places

**1. The census is a factor of two.** Imported through `T-267`'s path, the same rectangle is

| reading | columns | lattice sites | peak dishing / stroke |
|---|---|---|---|
| `AS_DRAWN` — every distinct offset its own column | 12 | **90** | `0.258057772` |
| `ONE_JUNCTION` — `o` and `o+1` on one pair are one junction | 6 | **45** | `0.28058418` |

`1.087×` in the flatness, at 16 duplexes on `C-0001`'s secant foundation under `T-10`'s own edge
taper. `T-267` therefore refuses to default the reading and names it in the record; that is a
mitigation, not a resolution.

**2. The buildability predicate cannot see it.** `noSiteIsCrossedTwice` keys on
`(lowerHelix, offset, onScaffold)`, so a reciprocal pair at one offset is caught and a pair one
base pair apart is not. The predicate's **letter** is what the reference rectangle satisfies; its
**intent** — that a crossover site is registered once — is what it does not.

**3. `C-0157` measured one motif.** Its `k_θ` bracket of `5.62052112 – 25.9227606 pN·nm/rad`
relaxed a sheet carrying one strand crossing per junction. Whether a pair at `o` and `o+1` relaxes,
and what hinge constant it gives, is not measured here and is not measured anywhere in this
repository.

## What is *not* claimed

- **Not** that `C-0157`'s number is wrong. Its bracket is a property of the object it simulated,
  and that object is stated.
- **Not** that the reference implementation is wrong. It is the field's own generator and its
  output loads, and is what somebody handed this repository a design would most likely hand over.
- **Not** that the two motifs are elastically different by a factor of two. A doubled crossing
  count is not a doubled hinge constant; the ratio above is what **this lattice model** does with
  the two readings, and the lattice model takes a crossover as one dihedral spring.

## What would settle it

One oxDNA relaxation, of the shape `C-0157` already has a driver for: the same 15 × 112 bp sheet
with each crossover drawn as a pair at `o` and `o+1` instead of a single crossing at `o`. Three
outcomes are distinguishable and all three are useful — it relaxes and gives a hinge constant
(then `C-0157`'s object is the wrong one and its number is a lower bound on the count); it relaxes
and gives the same one (then the motif is a drawing convention and the reading may be defaulted);
or it does not relax (then the reference implementation's rectangle is not foldable as drawn,
which would be a finding about the field's own tooling and not about this corpus).

Until then the reading is a **declared** parameter of every import, which is what
`AdjacentCrossingReading` is.
