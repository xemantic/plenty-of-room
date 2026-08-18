# CH-0111 — **The 38.08 nm tile's flatness verdict rests on 14 crossovers whose mechanics the primary source says are UNKNOWN.** `C-0095` establishes that the row-end crossover **exists** — it is the raster turn, it is built, it is imaged at 90 % yield — and `C-0090`'s admitted reading then gives all 14 of them `C-0009`'s ordinary interior `k_θ` and vertical link. But a raster turn ties two duplex **ends**, and Rothemund states in print that at an edge on the crossover lattice the glide symmetry fails, *"a crossover involving staple strands is in tension with an adjacent crossover involving the scaffold strand"*, *"such a configuration of crossovers in tension has never before been used in DNA nanostructures"*, and *"how the strain is actually relieved is unknown"*. **Existence is not stiffness.** The two readings `C-0090` calls conventions are in fact the two ends of a stiffness **bracket** on those 14 elements — full at **0.0621469105**, absent at **0.168371808** — and the tile is inside `T-5b`'s 0.10 at one end and outside it at the other

| | |
|---|---|
| **Against** | [`C-0090`](../claims/C-0090-buildable-raster-width.md)'s headline **0.0621469105** and its framing of the end-of-row question as a **convention**; and [`C-0009`](../claims/C-0009-discrete-lattice-tile.md)'s uniform crossover element, which has one `k_θ` and one vertical link for every crossover on the sheet |
| **Raised by** | [`C-0095`](../claims/C-0095-row-end-crossover.md), task [`T-161`](../tasks/T-161.md) — **against its own consequence** |
| **Grounds** | **methodological** — a quantity quoted without the state it was read at, where the state is *which kind of object the element is*. `C-0009`'s `k_θ` was derived for a crossover with duplex on **both** sides of it; 14 of the 56 at this width have duplex on one side only, and the primary source says their strain is unresolved |
| **Status** | **DISCHARGED, iteration 20, by [`C-0099`](../claims/C-0099-row-end-crossover-stiffness.md) (`T-164`) — the task this challenge itself queued, in the order this challenge itself ranked.** All four *what would settle it* items are answered: the sweep is run and is monotone (16 of 16), the vertical link taken separately **is** the whole of the reachable set, Rothemund's remedy is priced at `0.0030284749` of the stroke, and the oxDNA edge crossover is **recommended against**. Ground 2's bracket is **corrected** by [`CH-0115`](CH-0115-the-row-end-bracket-is-a-constraint-not-a-stiffness.md): only **2.85 %** of it is a stiffness. Filed as raised: **STANDS as a statement about the UNCERTAINTY, not about the verdict.** `C-0095`'s answer to *"may a crossover be drawn there"* is unaffected — that is a design rule and a folded structure. What is challenged is the **precision** of the number the admitted reading produces, and the claim that the two readings are alternatives rather than bounds |

---

## What the claims assert

`C-0090`, validity range:

> *"**The end-of-row convention is a MODELLING CHOICE and both readings are carried.** The
> physical argument for admitting it is that a seamless boustrophedon's row-end scaffold crossover
> certainly exists; the argument for refusing it is that no crossover has ever been drawn at the
> last base pair of a duplex and the grillage cannot represent one without an inset. **The verdict
> differs between them** (0.0621 against 0.1684) and that is stated, not averaged."*

`C-0095` has now settled the first clause in the affirmative and **refuted the second** — a
crossover has been drawn at the last base pair of a duplex, in Rothemund's own rectangle, at 90 %
well-formed yield. What `C-0090` does with that answer is take the admitted branch **whole**.

`C-0009`'s lattice carries one crossover element:

> *"A crossover is TWO elements and only one of them is `D_⊥`. The dihedral spring `k_θ` carries
> the across-helix rigidity; the vertical link is a **constraint** tying two duplex surfaces
> together and carries no rigidity at all."*

Both elements are applied identically at every column, including the two on the row ends.

## The challenge

### Ground 1 — a raster turn is not an interior crossover, and the source says so

At an interior column, the crossover sits between duplex on both sides and the sheet's **glide
symmetry** balances its strain: Rothemund, Supplementary Note S2, *"the minor groove faces
alternating directions in alternating columns of periodic crossovers … This symmetry should tend
to balance strain in the origami."*

At the row end that argument stops, and he says so in the very next paragraph — **read directly**:

> *"However, at seams and edges this is not necessarily true, **even where a seam or edge lines up
> with the underlying crossover lattice**. At seams or edges, because DNA has a major and minor
> groove, a crossover involving staple strands is in tension with an adjacent crossover involving
> the scaffold strand. **Such a configuration of crossovers in tension has never before been used
> in DNA nanostructures.** … **How the strain is actually relieved is unknown**, the final base
> pairs of each helix may be distorted."*

A crossover under an unrelieved static strain, whose relief mechanism is *"unknown"* and which may
be relieved by distorting *"the final base pairs of each helix"*, is not obviously the same
dihedral spring as one in the balanced interior. **This challenge does not assert that it is
softer. It asserts that nothing in this repository has asked.**

### Ground 2 — the two "conventions" are a bracket, and it is a wide one

Refusing the column removes the node, the dihedral spring **and** the vertical link. Admitting it
supplies all three at an interior crossover's value. A row-end crossover of any intermediate
stiffness therefore lies **between** the two readings already computed:

| the two end columns | dishing / stroke, phase 8 | inside `T-5b`'s 0.10? |
|---|---|---|
| full interior `k_θ` and vertical link (**admitted**) | **0.0621469105** | **yes** |
| a soft or distorted row-end crossover | **not computed** | **not known** |
| no crossover at all (**refused**) | **0.168371808** | **no** |

**The verdict crosses the acceptance convention inside the bracket.** That is what makes this
worth a challenge rather than a validity note: `T-5b`'s 0.10 lies at
`(0.10 − 0.0621469105)/(0.168371808 − 0.0621469105) = 0.356` of the way from the admitted reading
to the refused one — one subtraction on the two numbers above, not a solve — so a row-end
crossover retaining less than roughly a third of
an interior one's contribution would take the design out of the convention — if the response is
monotone in that stiffness, which is **also** not shown.

### Ground 3 — it is 25 % of the sheet's crossovers, not a rim correction

At phases 8 and 24 the eight columns carry **56** interface crossovers and the two end columns are
**14** of them — exactly one per interface, and every one a scaffold raster turn. A quarter of the
across-helix load paths of the recommended tile are objects whose stiffness has never been
derived, and they sit where `C-0022`'s edge collar puts the largest load gradient.

## What would settle it

1. **Cheapest, and it is a sweep this repository can already run.** Scale the two end columns'
   `k_θ` from `0` to full and re-solve `C-0090`'s exhaustive centro-symmetric placement at phase 8.
   `C-0058`'s Woodbury bank slices per placement, so this is one factorisation per stiffness value.
   It answers monotonicity **and** locates `T-5b`'s crossing, and it costs no new physics.
2. **The vertical link separately.** `CLAUDE.md` records that scaling `D_⊥` is *not* a model of
   removing crossovers, and the same trap applies here in reverse: the row-end element may lose its
   dihedral spring and keep its constraint, which is a different state from either reading.
3. **An oxDNA or all-atom edge crossover**, which is the only way to answer Rothemund's own
   question about how the strain is relieved. Expensive, and it should not be spent before (1).
4. **Rothemund's remedy, priced as mechanics rather than as arithmetic.** *"One or two scaffold
   bases could be left unpaired and allowed to form a hairpin that should relax the crossover"* —
   `C-0095` prices the scaffold cost at 0.9–1.8 % and explicitly does **not** price what the added
   slack does to `k_θ`. A relaxed crossover is a **softer** one, so the remedy for the strain moves
   the answer toward the refused end of the bracket.

## What is NOT challenged

- **That a crossover may be drawn at the last base pair of a duplex.** `C-0095`'s three headings
  stand: the geometry does not forbid it, no design tool forbids it, and Rothemund built it.
- **The lattice congruences.** The parity theorem, the 14/42 scaffold/staple split and the identity
  of `C-0086`'s odd-half-turn rule with the row-end column complementarity are integer arithmetic.
- **`C-0090`'s station lattice, its invariants, or its arm quantisation.** None of them touches the
  row-end column's stiffness.
- **The direction of the verdict at the admitted end.** If the row-end crossover really is an
  ordinary crossover, `C-0090` is right and the 38.08 nm tile is 12.0 % flatter than §3's nominal
  one. This challenge says that *"if"* has not been tested.
