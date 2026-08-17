# CH-0101 — **§3's 40.0 nm is not a buildable raster width.** A seamless boustrophedon has **only progressive** scaffold crossovers, so Rothemund's own constraint — *"the distance between successive scaffold crossovers must be an **odd number of half turns**"* — binds the **row length**, which must therefore be an **odd multiple of the 16 bp crossover spacing**: **16, 48, 80, 112, 144 bp** and nothing between. **40.0 nm is 117.6 bp**, and the nearest admissible width is **112 bp = 38.08 nm**, **4.8 %** narrower. The step is **32 bp = 10.88 nm**, not the rise

| | |
|---|---|
| **Against** | `Gen1Tile.EDGE_X = 40.0` and, through it, every plan, flatness and load claim written on a 40.0 nm width — [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md), [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md), [`C-0022`](../claims/C-0022-tile-edge-load-profile.md), [`C-0053`](../claims/C-0053-hinge-arm-array-packing.md), [`C-0063`](../claims/C-0063-upward-root-placement.md), [`C-0069`](../claims/C-0069-output-element-placement.md) |
| **Raised by** | [`C-0086`](../claims/C-0086-seamless-scaffold-routing.md), task [`T-151`](../tasks/T-151.md) |
| **Grounds** | **methodological, and it is a specification gap as much as a modelling one** — the width is inherited from §3 as a nominal dimension and has never been checked against the design language that has to draw it |
| **Status** | **STANDS as a statement about a SEAMLESS raster**, which is what `C-0086` recommends. It does **not** assert that 40.0 nm is unbuildable in general: the seamed case is not derived, and Rothemund himself offers an escape (*"one or two scaffold bases could be left unpaired"*) whose cost he introduced it to avoid |

---

## What the programme assumes

`Gen1Tile.EDGE_X = 40.0`, everywhere, from §3's *"40 × 40 nm tile"*. It is a **nominal** dimension: no claim in this
repository derives it from a scaffold routing, and until [`T-151`](../tasks/T-151.md) no claim specified a routing
at all.

## The challenge

### Ground 1 — a boustrophedon's scaffold crossovers are all progressive

Rothemund, **read directly** (*Nature* **440**:297, main text, `gpd/data/T-151-sources/DNAorigami-nature.txt`):

> *"for the scaffold to raster **progressively** from one helix to another and onto a third, the distance between
> successive scaffold crossovers must be an **odd number of half turns**. Conversely, where the raster **reverses
> direction vertically** and returns to a previously visited helix, the distance between scaffold crossovers must
> be an **even** number of half-turns."*

`C-0086`'s seamless routing is the plain boustrophedon: one segment per row, no vertical reversal anywhere — which
is exactly the condition Rothemund's 26-helix square meets (*"had no vertical reversals in raster direction"*). So
**every** one of its crossovers is progressive, and the "distance between successive scaffold crossovers" is the
**length of one row**.

### Ground 2 — so the row length is quantised at 32 bp

On this sheet's 1.5-turn spacing, three half-turns are **16 bp**. An odd number of half-turns is therefore an
**odd multiple of 16 bp**:

| row length [bp] | width [nm] | admissible? |
|---|---|---|
| 16 | 5.44 | **yes** |
| 48 | 16.32 | **yes** |
| 80 | 27.20 | **yes** |
| **112** | **38.08** | **yes** |
| **117.6 → 118** | **40.12** (§3's 40.0 rounds here) | **NO** |
| 144 | 48.96 | **yes** |

**The step is 32 bp = 10.88 nm.** This is not a rounding at the rise — it is a quantisation an order of magnitude
coarser, and 40.0 nm falls between two rungs.

### Ground 3 — what it moves, and what it does not

| quantity | at 40.0 nm | at 38.08 nm | moves? |
|---|---|---|---|
| footprint (× 40.35 nm) | 1 614 nm² | **1 537 nm²** | **−4.8 %** |
| §3's 100 pN as a pressure | 0.0620 pN/nm² | **0.0651** | **+5.0 %** |
| the root pitch, the plane lattice, the rise | unchanged | unchanged | **no** — they are along-helix quantities and the width is a count of them |
| `C-0063`'s outermost stations at ±16.32 nm | inside ±20.0 | inside **±19.04** | **no** — they still fit, with 2.72 nm to spare |
| the number of upward stations, the crossover-phase census, every plan margin | — | — | **unevaluated**, and that is this challenge's own open item |

**The sign is unfavourable but small**, and the one thing that could have been fatal — `C-0063`'s outermost roots
falling off a narrower tile — does not happen: they sit at ±16.32 nm against a new half-width of 19.04 nm.

---

## What this challenge does NOT assert

**It does not assert that any number in the affected claims is wrong.** Every one is correct *at the width it was
computed on*; what is challenged is that the width is drawable.

**It does not derive the SEAMED case.** A double raster's segments have different separations and its own
admissible widths are not computed. `C-0086` recommends seamlessness for `C-0081`'s reasons, and this challenge
applies to that recommendation.

**It does not close on 38.08 nm.** 144 bp = 48.96 nm is also admissible and is 22 % *larger*; which side of 40.0 a
design should take is a trade against §3's own footprint clause and is not decided here.

## How this challenge would fail

1. **A design language that absorbs the phase at a turn.** Rothemund's own escape, offered for a different problem: *"one or two scaffold bases could be left unpaired and allowed to form a hairpin that should relax the crossover."* An unpaired base at each end of a row makes any row length admissible, at the price of the strain relief he introduced it for.
2. **A different crossover spacing.** At 2.5-turn spacing (26 bp on Rothemund's own integerisation, the square's) the admissible set is different, and 40.0 nm may fall on it. The Gen-1 sheet is specified at 1.5-turn (16 bp), and `C-0040`'s 32 bp per interface is built on that.
3. **A scaffold crossover that is not at the row's end.** If a row's two crossovers sit inboard of the tile edge, the separation is shorter than the width and the constraint moves off the width entirely — at the price of unpaired scaffold at both ends of every row.
4. **A honeycomb lattice.** 21 bp per interface and 10.5 bp/turn give a different admissible set; the Gen-1 sheet is square-lattice and `CLAUDE.md` records the two are not interchangeable.
