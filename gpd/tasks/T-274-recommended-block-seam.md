# T-274 — does the recommended `10 × 6` honeycomb block need a scaffold SEAM, and is the committed design drawn without one?

| | |
|---|---|
| **Raised by** | [`CH-0212`](../challenges/CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md), filed by [`C-0165`](../claims/C-0165-eleventh-answers-synthesis.md) |
| **Leaf** | `A8.2` |
| **Claim reserved** | `C-0168`. **Challenges reserved**: `CH-0215`, `CH-0216` |
| **Verification type** | **logical** (integer graph theory on the block's own cross-section lattice, brute-forced at small orders and proved beyond) **+ in-silico** (a census of the committed artifact, and one counterfactual grillage grading) |

---

## 1. Formulate

### The contradiction, stated exactly

[`C-0119`](../claims/C-0119-honeycomb-raster-width.md) §4 says a scaffold seam is **forced** on an
`m × n` honeycomb block and calls the 60-helix case *"a theorem, not an enumeration"*.
[`C-0160`](../claims/C-0160-scadnano-writer.md)'s committed artifact
[`gpd/designs/gen1-block-honeycomb-10x6-102-109.sc`](../designs/gen1-block-honeycomb-10x6-102-109.sc)
carries **one scaffold strand, 60 domains, 60 helices** — one domain per helix, which is
[`C-0161`](../claims/C-0161-mechanics-on-an-imported-design.md) §4's own seam discriminator
returning **no seam**.

It bites because `C-0161` §4(b) established that a seam **breaks the column-parity alternation
`CrossoverLayout` imposes by construction**, and both `tile/HoneycombCoupledStudy.kt` and
`tile/HoneycombPlacementStudy.kt` grade the recommended block through `CrossoverLayout.centred`.

### What is being decided

Not a value. **Which of two standing statements about the recommended block a downstream reader
may rely on**, and on what derivation.

### The theory under test, in the corpus's own words

`CLAUDE.md`:

> A scaffold **seam** is a parity on a tree, not a fabrication convention. Crossovers join only
> *adjacent* duplexes, so a single-layer sheet's row-adjacency graph is the path `P_D`, a **tree**
> — and a closed walk on a tree traverses every edge an **even** number of times. A **fully folded
> circular** scaffold therefore gives every row **two** segments, which is exactly Rothemund's
> seam; a **linear** scaffold needs only a Hamiltonian path, and a circular scaffold left partly
> unfolded closes through its own remainder. **A seam needs BOTH premises and dropping either
> removes it.**

So the theorem has **two** premises:

- **(P1)** the graph the scaffold may use is a **tree** (every edge a bridge), and
- **(P2)** the scaffold is a **fully folded circular** strand, i.e. its closure is itself an edge
  of that graph.

`C-0119` §4 asserts (P1) for a honeycomb block by restricting the scaffold to Douglas et al.'s
*"the path of the scaffold stays within a 2D surface"*, and does not examine (P2) at all.
[`C-0154`](../claims/C-0154-honeycomb-grillage.md) has since established that a
honeycomb block's interfaces are **not** a path graph, so (P1) must be **re-derived on this
block's actual cross-section adjacency** rather than inherited.

### Locked units and conventions

- Lengths **nm**, energies **k_BT** (`k_BT = 4.142 pN·nm` at 300 K) **and** eV, counts as integers.
- Honeycomb bond length `d = 2.536 nm` (SAXS, `Gen1Tile.INTERHELICAL_HONEYCOMB`); rise
  `0.34 nm/bp`; row pitch `3d/2`, column pitch `d√3/2` (`C-0141`).
- Cross-section coordinates are `HoneycombRasterTurnSense`'s integer `HoneycombCell(x, y)`, with
  `x` in units of `d√3/2` and `y` in units of `d/2` — the corpus's own lattice, not a new one.
- The **raster path** is `honeycombXRasterPath(rows, helicesPerRow)`; the **admissible-crossover
  graph** is the subgraph of the honeycomb lattice **induced** on those cells.
- ssDNA: contour `0.57 nm/nt` with Kuhn `1.34–1.41 nm` (force spectroscopy) and `0.65–0.70 nm/nt`
  with Kuhn `2.10–2.84 nm` (zero-force scattering) — `CLAUDE.md`'s 2× method-systematic bracket,
  carried as a bracket and never mixed.

### Acceptance predicates

| | predicate | falsifiable how |
|---|---|---|
| **P1** | The induced adjacency of the `10 × 6` block is derived from the corpus's own `HoneycombCell.neighbours`, and its edge count, degree sequence and bridge set are emitted. A **tree** on 60 vertices has 59 edges; anything else refutes (P1) as a lattice statement. | count the edges |
| **P2** | Whether a **Hamiltonian cycle** exists in that graph is decided — not sampled. If the two raster termini have degree 1 the answer is `no` by a one-line argument; if they do not, the search must be exhaustive or refuse. | a degree census plus a search, with a refusal guard |
| **P3** | A **lower bound on the domain count** of a fully folded circular scaffold is derived on each of the two readings of the scaffold graph (the induced lattice, and Douglas's 2-D-surface restriction), by the handshake identity on an Eulerian covering multigraph. Both bounds are integers and both must exceed 60, or the seam question is vacuous. | arithmetic on the degree sequence |
| **P4** | The committed artifact's own census — scaffold strand count, domain count, helix count, base count, terminus offsets — is read out of the **file** and matched against the emitted design, at integer equality. | `ScadnanoDesign.fromFile` |
| **P5** | The **remainder closure** is priced: the Euclidean separation of the raster path's two termini, the minimum unpaired nucleotide count that reaches it (contour bound), and the Gaussian stretch free energy of the actual remainder, over the whole ssDNA Kuhn bracket. If the closure costs more than the fold's own currency (`8.0 k_BT` per crossover column, `CLAUDE.md`), the favourable resolution is *not* available and the seam stands. | closed form |
| **P6** | `CH-0212`'s proposed reading (2) — *"run `T-267`'s importer on the block and read its column parity sequence"* — is attempted, and either delivers the sequence or is shown to be unavailable **with the reason**. | run it |
| **P7** | The two studies that call `CrossoverLayout.centred` on this block are either re-graded or shown in-family, and the **counterfactual** — what the seamed layout would have cost — is measured rather than left as *"unmeasured"*. | one grillage grading |

### Declared falsifiers

- **F1** — the induced graph on the block's cells has exactly 59 edges (it *is* a tree). Then (P1)
  holds as a lattice statement and `C-0119` §4 needs no restriction at all; the seam then turns
  entirely on (P2).
- **F2** — a Hamiltonian cycle exists. Then even a fully folded circular scaffold needs no seam,
  and `C-0119` §4's *"theorem"* fails on its **first** premise rather than its second.
- **F3** — the committed `.sc` does **not** in fact carry 60 domains on 60 helices, i.e. `CH-0212`'s
  own census is wrong. Then there is no contradiction to resolve.
- **F4** — the remainder cannot reach: the terminus separation exceeds the contour of the spare
  scaffold, or the stretch free energy exceeds the fold's currency. Then the circle **cannot**
  close through its remainder, (P2) is restored, and the seam is forced after all — in which case
  the deliverable is the seam **restored in the design**, not an annotation.
- **F5** — the seamed counterfactual moves a coupled flatness verdict across `T-5b`'s `0.10`. Then
  *"no number moves either way"* is wrong, and the fork has a cost.

### What would falsify the whole approach

That the seam is not a property of the **graph** at all. If a honeycomb scaffold's routing is
constrained by something this task does not model — a caDNAno rule about which of the three bonds
a scaffold may take at a given residue, say, or a folding-pathway argument — then a graph-theoretic
verdict is the wrong instrument and the honest output is a refusal naming the missing constraint.
The `±5 bp` residue rule (`C-0148`) is exactly such a constraint and is checked for the drawn
routing; what this task cannot do is derive a *folding yield*, and it says so.

---

## 2. Plan

### The cheap bound runs first, and it is a degree census

Building the induced subgraph of the honeycomb lattice on 60 cells is 60 lookups of a three-element
neighbour list. Its degree sequence decides `P2` outright: **a vertex of degree 1 lies on no cycle**,
so if either raster terminus has degree 1 there is no Hamiltonian cycle and no search is needed.
That is microseconds against `C-0119`'s own factorial guard, which refuses beyond order 9.

The same degree sequence gives `P3` by the handshake identity: an Eulerian covering multigraph `H`
has `|E(H)| = ½ Σ deg_H(v)`, every `deg_H(v)` is even and at least 2, a degree-1 vertex forces its
pendant edge to multiplicity ≥ 2, and that vertex's neighbour then needs ≥ 4. Both bounds are one
sum.

### The expensive half, and why it is small

- **`P4`/`P6`** are file reads through `ScadnanoDesign`, already in the tree.
- **`P5`** is a closed form: `r` from two integer lattice coordinates, `n_min = r / (contour per nt)`,
  and `ΔF = (3/2) r² / (N_K b²) k_BT` on a Gaussian chain, with `r/L_c` reported so the reader can
  see the Gaussian is inside its own validity.
- **`P7`** is one `OrigamiGrillage` solve per layout on the smeared equivalent sheet the two
  studies use today, under `C-0022`'s solved collar. A handful of solves, seconds.

No SCF solve, no Monte Carlo, no field solve. The whole study is expected to run in under a minute,
which is the justification: the question is a **topology** question and topology is integer arithmetic.

### Why not simply re-run the two studies

Because on the seamless resolution there is nothing to re-run — the alternating family is the right
family — and on the seamed resolution the block would need a **staple routing this corpus has never
determined** (`C-0160` §6 emits none), so a "seamed honeycomb layout" would be an invention rather
than a re-grade. The defensible measurement is the **shape** a seam takes in the one seamed design
this corpus has imported (`C-0161`'s reference rectangle: one doubled column pitch, two consecutive
columns of the same parity), applied to the block's own column ladder and graded. It is labelled a
counterfactual, not a design.

### Method risk, stated

The induced-subgraph reading of *"the graph the scaffold may use"* is **more permissive** than
Douglas et al.'s surface restriction and **less** permissive than the unrestricted honeycomb lattice
(which is infinite). Both readings are carried and reported side by side, because the verdict must
not depend on which one a reader prefers — and here it does not: the seam fails on **(P2)** under
both.

### Emission

`gpd/results/T-274-recommended-block-seam.json`, rounded at the serialisation boundary through
`roundedForResult`, with the departure fields at two significant digits per `DEPARTURE_DIGITS_BY_KEY`.
No wall-clock field, no step count.
