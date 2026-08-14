# T-116 — Does a 45-arm hinge-line array have a plan view?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme |
| **Raised by** | [`C-0050`](../claims/C-0050-desired-stroke-reach.md), open item 1 — *"`E5a1` clears every predicate this claim can evaluate at §3's acceptable stroke, and its plan view has never been drawn"* |
| **Verification type** | **logical** (exact plane geometry and an exact count on measured lattice constants — no mesh, no free parameter) **+ in-silico** (`C-0039`'s elastica placement re-run as a library, `C-0015`'s `CrossoverLayout` re-run over the complete 32 bp phase space, `C-0041`'s packer reproduced as a limiting case) |
| **TRL** | **1–3.** Nothing here is measured. A plan view is not a demonstration. |

---

## Formulate

### The question, exactly

`C-0050`'s catalogue has **three** rows of fourteen that clear every predicate it can evaluate at
§3's **acceptable** 3 nm stroke, and only one of them is computed on the exact element:
**`E5a1`** — `C-0039`'s two-spring elastica on **one** crossover per flexure, 45 flexures,
arm **9.131 nm**, assembled tangent **39.18 pN/nm**.
Its rows carry `packingAssessed = false`.

`C-0041` asked the same question of a **different** element and the answer cost 45 paths → 15.
**That result does not transfer** and the task is to say what does.

### The numeric target and the acceptance predicate

**`P1`** — the **cheap bound**: the plan area 45 arms occupy, against the 40 × 40 nm footprint.
**`P2`** — a **single-level layout** of 45 arms on `C-0015`'s 3 × 15 grid, or the statement that
none exists, swept over 720 orientations in `[0, π)` exactly as `C-0041` swept them.
**`P3`** — the **packing-limited path count**, solved at every column count, not asserted.
**`P4`** — **the 45 crossovers**: an injective assignment of one crossover of the host sheet's own
lattice to each arm, over **all 32** phases — *placement*, which is a different question from the
*inventory* `C-0040` counted.
**`P5`** — what the array does to the **host sheet**: duplex length consumed, crossovers deleted,
connected components left.
**`P6`** — the **flatness consequence** of the column count the packing admits, against `C-0047`'s
break-even at three columns.
**`P7`** — if it does not pack: **which variable must give, and by how much, as a threshold.**

The task is **discharged** when `P1`–`P7` are answered with numbers, or when a packing verdict is
stated in the negative with its threshold.

### Units, locked

Lengths **nm**, forces **pN**, energies **pN·nm**, stiffness **pN/nm**, areas **nm²**;
`k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
Base pairs are quoted at the **0.34 nm** rise; a crossover phase is an **integer** number of them.

### Geometry and sign conventions, restated rather than inherited

- **Plan.** `x` runs **along** the host sheet's helices, `y` **across** them, origin at the tile
  centre. `z` is positive **upward**, away from the electrode; §1's bias pulls the tile **down**.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`** — the SAXS-measured single-layer
  interhelical distance — so two parallel duplexes at exactly `d` are **tangent and admissible**.
  Identical to `C-0041`'s convention and deliberately the loosest defensible one.
- **A hinge-line arm occupies ONE rectangle `arm × d`.** Its **root** is the end carrying the
  hinge; its **tip** is the end carrying `C-0034`'s `A2` anchorage. The rectangle is **rooted**, not
  centred: the anchor point of the array's grid is the **hinge**, because that is where the load
  path enters the host sheet.
- **`E5a1` owns NO vertical member.** Its near end is a crossover **in the host sheet's own plane**
  and its far end is a two-link `A2` joint, both point features. This is the premise `C-0041`'s
  Fact A rests on, and **it is the premise that does not transfer** — `C-0041`'s obstruction is a
  clash between *standoffs and ties*, and `E5a1` has neither.
- **The hinge axis runs along `x`.** `C-0040` establishes that `n k_θ` is the interhelical dihedral
  spring about the interface line and *"is the right spring for a hinge whose axis runs along `x`,
  and for no other axis"*. At `n = 1` the same 13.53 pN·nm/rad is also `C-0029`'s `B1` — two
  softened bonds about their own chord — so a **single** crossover is a point joint and the arm may
  extend along `x` from it with the full lever arm. **`E5a1` is the one member of the family for
  which the hinge-line orientation question does not arise**, and that is why it, and not `E5a16`,
  is the row `C-0050` left open.
- **A crossover serves one interface every 32 bp = 10.88 nm**, and adjacent interfaces are offset by
  16 bp = 5.44 nm because crossovers alternate between a helix's two neighbours (`C-0015`,
  `C-0040`). Interface `b` carries the columns of parity `b mod 2`.
- **A row's hinge sites are the union of its two bounding interfaces'** columns — so an interior row
  sees both parities (5.44 nm pitch) and an **edge** row sees one (10.88 nm pitch). That asymmetry
  is a lattice fact and is not imposed.
- **Blocking / clash / level assignment** are `C-0041`'s relations verbatim, so that setting an
  element's parameters to `C-0041`'s standoff flexure reproduces its verdict exactly.

---

## Plan

### The cheap bound, and what would falsify the approach

Two divisions, before any sweep:

1. **area** — `45 × 9.131 × 2.69` against `40 × 40.35`. `C-0041`'s came out at **2.59×** and
   invited *"stack it"*; if this one comes out above 1 the task closes on area alone.
2. **the two pitches `C-0041` failed on** — its Fact B was `span + d = 34.51 nm` against a
   **13.33 nm** column pitch. Here `arm + d = 11.82 nm`. **One subtraction decides whether
   `C-0041`'s obstruction transfers at all.**

**What would falsify this approach:** if the arm rectangles clash at the design pitch, the
expensive part (the crossover placement) is moot and the answer is `C-0041`'s. If they do not, the
plan-area question is settled and the binding question moves to the **lattice**, which is where the
expensive part must go. Either way the bound decides where the work happens — the same shape as
`C-0041`'s own cheap bound and `C-0035`'s before it.

### Method, and its cost justification

Exact plane geometry on cited lattice constants, plus an exact combinatorial placement:

- the packer is a **generalisation** of `C-0041`'s, parameterised by where the grid anchor sits on
  the element and which fractions of it carry vertical members, so that `C-0041`'s flexure is a
  **configuration of the same code** and its *"0 of 720"* and its fifteen are a **free limiting
  case** rather than a re-implementation;
- the hinge placement is solved **exactly**: per row, a dynamic program over the row's own sites and
  both root directions gives the maximum number of non-clashing arms; the interface sharing between
  adjacent rows is closed by **augmenting-path bipartite matching** on ≤ 56 crossovers, so a
  reported placement is a placement and not a count;
- the phase is swept **completely** — 32 base pairs, `C-0015`'s period, not 16.

No simulation can move a count, and nothing here needs one. The alternative — a coarse-grained
lattice solve of the perforated sheet — is `T-110`'s question, is far more expensive, and cannot
answer *whether the arms fit*, which is what is being asked.

### Declared falsifiers

| # | falsifier | what it would mean |
|---|---|---|
| 1 | the arm array's plan **area** exceeding the footprint | the task closes on area, as `C-0041`'s did not |
| 2 | some orientation packing 45 arms in **one level** | `C-0041`'s verdict does **not** transfer, and the answer is positive on geometry |
| 3 | the **crossover placement** reaching 45 at some phase | the lattice supplies the hinges as well as the area |
| 4 | the host sheet surviving the arms **connected** | the array is free of `C-0041`'s severance finding |
| 5 | the packing admitting **three or more** columns | `C-0047`'s flatness break-even is met and the coupling is not a net dishing source |
| 6 | the generalised packer failing to reproduce `C-0041`'s 0 of 720 and its 15 | the packer is wrong and nothing else here may be believed |

### The five gates, as executable tests

1. **dimensional** — the verdict is dimensionless (scale every length by 10, nothing moves); areas
   are lengths squared and additive; unphysical arguments throw.
2. **limiting cases** — `C-0041`'s flexure as a configuration of this packer reproduces its verdict,
   its 0 of 720 and its fifteen; one arm packs at every orientation; two collinear arms clash
   between `arm` and `arm + d` and clear above it; a row with one site holds one arm.
3. **symmetry and conservation** — the verdict is invariant under a rigid rotation of the whole
   array; the two interface parities' site counts sum to the column count at every phase
   (`C-0015`'s conservation law, recovered); a 16 bp shift swaps the parities and 32 bp is the
   identity; the matching is a matching (injective, and every arm's crossover lies on one of its own
   two interfaces).
4. **numerical convergence** — the orientation sweep is sample-count independent 180 → 2880; the
   phase sweep is complete at 32 and refining it 100× produces no count the base-pair phases do not
   contain; the placed arm reproduces its own target secant.
5. **literature and upstream** — `C-0039`'s 9.131 nm arm and 39.18 pN/nm tangent; `C-0040`'s
   four-crossover census and its 49/56 inventory; `C-0015`'s ten eight-column phases; `C-0041`'s
   span, its 15, its 0 of 720 and its 18 components; the SAXS 2.69 nm, the 0.34 nm rise, the 32 bp
   interface spacing.
