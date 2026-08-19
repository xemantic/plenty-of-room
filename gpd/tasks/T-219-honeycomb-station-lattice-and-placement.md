# T-219 — The honeycomb's own station lattice, plan ceiling and placement family

**Leaf:** `A8.2` (the plan and lattice model the anchoring array is written on).
**Claim reserved:** `C-0141`.
**Challenges reserved:** `CH-0174`, `CH-0175`.
**Raised by:** [`C-0136`](../claims/C-0136-mixed-domain-phase-and-honeycomb-twist.md),
and named as its own largest open item by [`C-0118`](../claims/C-0118-coupled-four-layer.md) §5.

---

## Formulate

### The question, exactly

`C-0118` produced the first coupled tile in this programme's history that is flat at the 90th
percentile under the only measured folding statistics, and it named its own largest caveat in one
sentence: *"The attachment grid is the abstract one, not a lattice census … **a path count here is a
REQUEST, not a demonstration that the stations exist**."*

`CLAUDE.md` states the underlying problem twice:

> *"`OrigamiGrillage` **never reads `layers` or `interlayerCoupling`** … the lattice machinery of
> this repository is **single-layer**, and `CrossoverLayout`'s two-parity alternation makes its
> crossover combinatorics **square-lattice**. Every placement, phase and plan ceiling in the corpus
> is a single-layer square-lattice result and does not transfer to a honeycomb face."*

So: **what attachment lattice does a honeycomb block's face actually offer, what plan length can an
element rooted on it keep, and does a centro-symmetric placement family exist there** — or, failing
that, precisely which machinery is square-lattice-specific and what a honeycomb answer would need.

### What is already on the table, and why it is not yet an answer

- `C-0122` censuses the stations by **multiplying** top-face helices by stations per 21 bp ladder,
  giving **90** (`15 × 4`) and **60** (`10 × 6`).
- `CH-0151` corrects that to **132** and **90**, on the ground that a top-face helix of the oblique
  sublattice carries **two** free azimuths at `±60°` rather than one.
- `C-0128` prices an oblique root at the honeycomb's *"own **60°**"*.
- **None of the three derives the block's cross-section.** A census of free azimuths is a statement
  about which lattice neighbours are **absent**, and that cannot be settled without the site set.

### Locked units and conventions

- Lengths **nm**, angles **degrees**, forces **pN**, stiffness **pN/nm**;
  rise **0.34 nm/bp**; `k_BT = 4.141947 pN·nm` at 300 K in aqueous 2 mM MgCl₂.
- **Honeycomb bond length** `d = 2.536 nm` (`Gen1Tile.INTERHELICAL_HONEYCOMB`, Fischer SAXS).
- **Cross-section indexing**, from the caDNAno paper (Douglas et al., *NAR* **37**:5001, PMC2731887,
  already in `gpd/data/T-151-sources/`), read directly:
  > *"as viewed down the helical axes, close-packing rows of helices were arrayed within the
  > honeycomb framework in an **x-raster pattern** (i.e. left to right, then down, then right to
  > left, then down…). **The x-raster rows within the honeycomb framework are corrugated; they
  > stagger up and down and encompass helices that are actually at two different y-positions.**
  > Similarly, **virtual y-oriented layers can be defined that stagger left and right and encompass
  > helices that are at two different x-positions**."*
  > *"The nomenclature of the designs is `m × n`, where `m` is the number of x-raster rows, and `n`
  > is the number of helices per x-raster row."*

  So a block is `m` corrugated rows of `n` helices; site `(r, c)` with `0 ≤ r < m`, `0 ≤ c < n`;
  neighbours `(r, c ± 1)` and `(r + 1, c)` if `r + c` is even, else `(r − 1, c)`.
- **Cross-section coordinates:** `x` across the row (the `n` direction, the tile's **thickness**),
  `y` along the stack of rows (the `m` direction, the tile's **in-plane width**).
  The helix axis is the third direction and carries the 112 bp span.
- **`z` is the device normal, positive upward, away from the grafted layer.**
  The tile's **top face** is the face normal to the thinner cross-section dimension.
- A **station** is a crossover position on a **free** azimuth — one whose lattice neighbour is
  absent — with a positive component along the outward face normal.
- An attachment roots on **one** azimuth, so its ladder is the **21 bp** period and never the 7 bp
  step (`C-0119`, `C-0122`).

### Acceptance predicates

- **`P1`** — a **station census** derived from the site set rather than multiplied: for each
  published cross-section, the face helices, their free azimuths, the angle of each from the face
  normal, and the resulting station count, at a stated row length and ladder phase.
- **`P2`** — an **arm / plan ceiling** on that lattice: the largest rooted element any placement of
  a demanded count can keep, by `maximumPlanCeilingForCount`'s exact bisection, with the cheap
  inboard bound `ladder − girth` quoted first.
- **`P3`** — the **centro-symmetric placement family**: whether one exists at all on the honeycomb
  station lattice, at which counts and phases, and how large it is.
- **`P4`** — a named list of **what is square-lattice-specific**, file by file and function by
  function, with what a honeycomb result would require of each.

### Falsifiers, declared before the run

- **`F1`** — if the honeycomb's per-site area `3√3/4 · d²` equals the area per helix of the
  cross-section the corpus assumes (`interhelicalDistance × layerSpacing`), then the assumed
  geometry **is** a honeycomb and there is nothing to correct.
- **`F2`** — if any top-face helix of a full `m × n` block carries **two** rooting azimuths, then
  `CH-0151`'s correction stands and this task's census is wrong.
- **`F3`** — if the honeycomb plan ceiling at `C-0118`'s largest demand is **not** below the square
  lattice's, the cheap bound `21 bp − d < 32 bp − d` was the wrong bound.
- **`F4`** (standing) — a **uniform** load on a uniform Winkler foundation must dish exactly zero on
  every plate solved here.
- **`F5`** — if `centroSymmetricPlacementsOn` and `maximumPlanCeilingForCount` cannot take the
  honeycomb lattice at all, then the combinatorial machinery is square-lattice-specific too, and
  `P2`/`P3` are answerable only as a statement of what is missing.

---

## Plan

### The cheap bound, run before any code

Two divisions, and they decide the shape of the whole answer:

1. **The ladder.** The honeycomb's own azimuth period is **21 bp = 7.140 nm** against the square
   lattice's 32 bp = 10.880 nm. So the *inboard* plan budget for two collinear elements — the
   binding half of `C-0069`'s ceiling — is `7.140 − 2.536 = 4.604 nm` against `10.880 − 2.690 =
   8.190 nm`: **1.78× less**, before any placement is enumerated.
2. **The packing.** A honeycomb lattice of bond length `d` has **one site per `3√3/4 · d² = 8.3545
   nm²`**. The cross-section every four-layer claim in this corpus is written on has
   `interhelicalDistance × layerSpacing = d² = 6.4313 nm²` per helix. The ratio is
   `3√3/4 = 1.299038`, exactly — **the assumed cross-section is 1.30× denser than any honeycomb
   lattice can be**, and its two pitches are wrong in opposite directions: the in-plane row pitch is
   `3d/2` and the layer pitch is `d√3/2`, whose product is exactly the honeycomb's cell area.

The second of these is one multiplication and it says the geometry has to be re-derived before a
census means anything. `CLAUDE.md` already records half of it (*"a honeycomb array stacks its rows
at `d√3/2`"*); the other half — that the **in-plane** pitch is then `3d/2` and not `d` — is what
makes the pair conserve the cell area.

### Method, and its justification against cost

- **Everything here is integer lattice arithmetic and closed-form geometry.** No field solve is
  needed for `P1`–`P4`, and none is justified: the census is a neighbour count, the plan ceiling is
  a monotone bisection on a feasibility predicate, and the centro-symmetric family is an exhaustive
  enumeration of a partition.
- **The combinatorial machinery is re-used, not rewritten.** `maximumPlanCeilingForCount`,
  `latticeRootCapacity`, `rootedLengthCeiling`, `rowRootOptions`, `armDirections` and
  `centroSymmetricPlacementsOn` all take an **explicit** `List<List<Double>>` lattice. Feeding them
  the honeycomb lattice is the test of `F5`, and it costs nothing.
- **One plate solve is justified and only one**: the corrected cross-section's free-tile dishing,
  because `C-0120`'s footprint ordering and `C-0116`'s threshold both rest on `edgeY`, and a
  1.5× change in the in-plane pitch is a change in the plate's **span**. It also wires `F4`.
- **The row length is swept over both readings**: `C-0119`'s standing 112 bp and `C-0136`'s
  honeycomb-buildable 119 bp = 40.46 nm.
- **The inter-row ladder offset is a convention and is swept**: the two face sublattices' free
  azimuths belong to two different bond classes, whose crossover residues differ by 7 bp mod 21, so
  the offset is `7` or `14` bp and the study carries both. Sibling task `T-218` settles the
  *scaffold* turn sense, which is a different variable; nothing here depends on it.

### What would falsify the approach

If the census turns out **not** to be a property of the site set — if two different terminations of
the same `m × n` block gave different face inventories — then a census is not well posed without a
termination convention, and the deliverable becomes the convention rather than the number.

### Reproductions

- `C-0122`'s **90** and **60** at 112 bp, phase 0.
- `C-0072`/`T-136`'s **9.535 nm** maximum plan ceiling at 30 roots on the square phase-24 lattice,
  through the same `maximumPlanCeilingForCount` this task feeds the honeycomb lattice to.
- `C-0120`'s `15 × 4` free-tile dishing **0.0577199433** at the geometry it was solved on.
