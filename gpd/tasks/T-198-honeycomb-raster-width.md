# T-198 — Can four honeycomb layers be rastered from one circular M13 at a buildable width?

**Leaf:** `A8.2`
**Raised by:** [`C-0109`](../claims/C-0109-four-layer-tile.md) §11 item 3
**Verification type:** logical (integer-lattice arithmetic and a brute-forced parity) + literature
**Units:** base pairs and nm for length, dimensionless for counts

---

## Formulate

`C-0109` established that the tile §3 actually specifies is **four honeycomb layers**, that it is flat with no
coupling at all, and that one circular M13 pays for it — 15 × 112 bp × 4 = **6 720** of **7 249** nt.
It answered the **count** and said so:

> Whether four honeycomb layers can be **routed** from one circular M13 at a buildable width. The count fits;
> `C-0086`'s odd-half-turn rule is a square-lattice statement and admits no integer base-pair honeycomb row at
> all. The honeycomb raster width is unanswered.

### Acceptance predicate

1. The admissible seamless row lengths of a **honeycomb multilayer** raster are stated, from the **primary**
   design rules, read directly — or the absence of such a rule is established with its query record.
2. Whether a **seam** is forced is **derived**, not asserted, over the adjacency graph the scaffold may
   actually use; and if it is forced, its cost is stated in this programme's own units.
3. The scaffold budget is re-checked against the scaffolds this cross-section is **actually folded from**,
   not only against the figure `C-0109` used.
4. `C-0109`'s own budget arithmetic reproduces.

**Falsifier.** If the honeycomb's scaffold-crossover lattice is genuinely non-integral, the four-layer tile is
not buildable as a seamless raster at **any** width and the recommendation must change — not the tile's
rigidity, which `C-0116` has already settled, but whether it can be drawn at all.

---

## Plan

**The cheap bound runs first and it is one division.** `C-0086` adopted Rothemund's *"the distance between
successive scaffold crossovers must be an odd number of half turns"*, which on the square lattice's 16 bp half
turn gives 16, 48, 80, 112, 144 and selects 112 bp = 38.08 nm. A honeycomb half turn is **5.25 bp**, and an
odd multiple of 5.25 is never an integer — so either the rule is prohibitive on the honeycomb, or it is
outside its own domain. **Establish which before anything else**, because the two have opposite consequences.

**Then read the primary source rather than recalling it.** `CLAUDE.md`: *a design RULE is a citable primary
number and should be read, not recalled.* And **check `gpd/data/` first** — three previous tasks found their
answer already in the corpus.

**Then derive the seam.** `CLAUDE.md` records that a seam is *a parity on a tree*: a single-layer sheet's
row-adjacency graph is a path, a closed walk on a tree traverses every edge an even number of times, so a
circular scaffold gives every row two segments. A honeycomb helix has **three** neighbours, so that graph has
cycles and the argument does not obviously survive. Brute-force the parity over the graph the scaffold may
actually use, at every tractable order, and assert the theorem beyond them.

**Method choice, justified against cost.** Python rather than Kotlin, retained in `tools/`.
`SESSION-PROMPT.md` permits it — *"use the best tool for the problem … retain the driver scripts inside this
repository"* — and this task is graph combinatorics and integer arithmetic with no floating-point result a
`Double` could round. A sibling agent held the Gradle daemons for `T-197`'s plate solves throughout, and
`CLAUDE.md` measures that contention as what OOMs this box; avoiding it is part of the justification.

**What would falsify the approach.** That the scaffold may use the honeycomb's full three-regular adjacency,
in which case the graph is not a tree, the parity argument collapses, and a seam-free circular route may
exist. That is decided by what the design rules permit, not by geometry — so the literature read has to come
before the derivation is trusted.
