# T-96 — Does the surviving mounting survive `T-31`'s array packing?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme |
| **Raised by** | [`C-0035`](../claims/C-0035-flexure-mounting-sense.md), which quotes **every** aperture area as a fraction of the tile footprint *"explicitly as a scale rather than a placement"* and records the packing as unsolved |
| **Verification type** | **logical** (exact plan-view lattice geometry, no free parameter) **+ in-silico** (`C-0030`'s span placement re-run at every candidate path count, and the layouts solved as a blocking digraph) |
| **Maturity target** | TRL 1–3 |

---

## Formulate

### The question, stated numerically

`C-0035` settled the mounting: **`Su`** — standoff bases on the output superstructure, standoffs
pointing away from the tile, the flexure **outboard** above the superstructure's plane, each midspan
tied back **down** through that plane to the tile. Its costs are quoted as areas:

| | |
|---|---|
| flexure span (`C-0030`, `ℓ = 8 nm`, 45 paths) | **31.82 nm = 94 bp** |
| tie apertures, **every** stroke | 45 × 2.69² = **326 nm², 20.4 %** of 1600 nm² |
| beam slot at §3's **desired** 10 nm stroke | 45 × 18.37 × 2.69 = **2223 nm², 1.39×** the footprint |
| beam slot at §3's **acceptable** 3 nm stroke, `ℓ ≥ 6 nm` | **zero** |

**None of those is a placement.** The acceptance predicate is a plan view: *45 flexures of ~32 nm
span placed in plan, with their 45 apertures, on a body the size of the tile.*

### The acceptance predicate

| # | predicate | threshold |
|---|---|---|
| **P1** | the **cheap bound**: the total plan area the 45 beams occupy, against the tile footprint | reported as a ratio; if **< 1** the task closes on a division |
| **P2** | a **single-level** layout of 45 beams exists, or does not, on the 3 × 15 grid — swept over the beam orientation, not asserted at one | a pair count of overlaps, and the orientation window if any |
| **P3** | a **multi-level** layout exists, or does not, at **any** level count and **any** body size — the standoffs and the ties are vertical members and must reach through every plane below them | a count of **mutually blocking** pairs; one such pair closes every level budget at once |
| **P4** | the **packing-limited path count** on the Gen-1 40 × 40 nm tile | an integer `n_pack` |
| **P5** | `n_pack` against the per-path allowable at **both** of §3's strokes — 10 pN unzip (`C-0006`/`CH-0029`), 100 pN at 3 nm and the coupling's own delivered force at 10 nm | pass/fail per stroke, and they may differ in kind |
| **P6** | if the array does not pack: the **threshold** on the variable that must give, as a number, not a preference | a minimum tile area, or a maximum span, or a path count |
| **P7** | what the 45 tie apertures do to the superstructure **as a sheet**, not as an area | a connected-component count |

### Units, and the conventions restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, energies **pN·nm**; `k_BT = 4.141947 pN·nm`
  at **T = 300 K** in aqueous **2 mM MgCl₂**.
- **Plan view.** `x` runs **along** the tile's helices, `y` **across** them, origin at the tile
  centre — `C-0015`'s and `C-0026`'s convention. `z` is positive **upward**, away from the
  electrode. §1's bias pulls the tile **down**.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`** — the SAXS-measured single-layer
  interhelical distance (Fischer et al. 2016), so its exclusion half-width is `d/2 = 1.345 nm` and
  two parallel duplexes at exactly `d` are **tangent and admissible**. That is the lattice
  condition, and it is deliberately the *loosest* defensible one: the steric diameter is 2.0 nm.
- **A vertical member — a standoff or a tie — is a disc of radius `d/2` in plan.** It is a duplex
  standing normal to the sheet (`C-0028`), and a plane it must pass through needs a
  duplex-omission hole of one pitch.
- **A flexure occupies, in plan, one rectangle `span × d` centred on its midspan**, plus **three**
  vertical members: two standoffs at its ends and one tie at its midspan (`C-0035`'s `Su`).
- **A level** is a beam plane at height `ℓ` above the superstructure. Levels are quantised to the
  **0.34 nm** rise, separated by at least the **2.0 nm** steric diameter, bounded above by
  `C-0017`'s **10 nm** envelope and below by `C-0030`'s clearance `ℓ ≥ stroke + 2.69 nm`.
- **The midspans are not free.** The tie is vertical, so a flexure's midspan sits over its tile
  attachment: `C-0015`'s `m × 15` grid, `x_i = 40(i + ½)/m − 20`, `y_j = (j − 7)·2.69` — and
  `C-0026` has already shown that **fifteen rows is one attachment row per duplex** for every `m`.
- **Blocking, defined once.** Flexure `Y` **blocks** flexure `X` when `Y`'s plan rectangle covers
  any of `X`'s three vertical members. Then `level(Y) > level(X)` is required. Two flexures that
  block each other are **infeasible at any level count and on any body**.

### Inherited numbers, and what is cited rather than derived

| number | value | flag |
|---|---|---|
| interhelical distance | 2.69 nm | **CITED, MEASURED** (SAXS, Fischer et al. 2016) |
| B-DNA steric diameter | 2.0 nm | **CITED** — the phosphate backbone *is* the surface |
| rise per base pair | 0.34 nm | **CITED** |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT**, not a measurement |
| duplex `S` | 1100 pN | **CITED, MEASURED** (Wang et al. 1997) |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006` |
| §3 | 100 pN, 3 nm, 10 nm, 40 × 40 nm | **CITED** |
| `C-0030`'s `B2` base, span and tangent | 261.2 pN·nm/rad, 31.82 nm, 25.23 pN/nm | **CITED, and re-derived here** as gate-5 reproductions |
| `C-0035`'s aperture areas | 326 / 2223 nm² | **CITED, and re-derived here** |

Every span, every layout, every blocking relation, every level count, every packing-limited path
count and every threshold is **derived in code**.

---

## Plan

### The cheap bound, and why it does not close the task

One multiplication: `45 × 31.82 × 2.69 = 3852 nm²` against a 1600 nm² footprint — **2.41×**. It
settles `P1` and it settles nothing else, because a factor of 2.41 is exactly the size that invites
*"stack it in three levels"*, and `C-0017`'s envelope has room for three. **The cheap bound tells us
where the expensive part has to go: not at the area, at the topology.** That is the justification
for going further, and it is the same shape as `C-0035`'s own (the cheap bound settled the sign and
the expensive part went to buildability).

### The method

1. **Exact plan geometry**, no mesh. Oriented rectangles, separating-axis overlap, point-to-rectangle
   clearance. `openrndr-math`'s `Vector2` for the small fixed-size geometry, per `CLAUDE.md`.
2. **Sweep the orientation** rather than assume one. `C-0015`'s discipline — *"sampling a continuous
   angle on a discrete lattice is not a sweep"* — applies in a new place: the beam is a free duplex
   between two standoff heads, so its plan angle is continuous, but the two conditions that bound it
   (the across-row pitch and the along-row pitch) are lattice quantities. Sweep the angle finely and
   report the **window**, then check whether the window is empty.
3. **The blocking digraph**, which is what makes stacking a *decidable* question rather than an
   area budget. A mutually blocking pair kills every level count at once; an acyclic digraph is
   layered by longest path and then repaired against same-level overlaps.
4. **Re-place the span at every candidate path count** with `C-0030`'s own `coupledFlexureSpan`,
   never with a scaling law — the `n^(1/3)` is then available as a *check*, not as an input.
5. **The superstructure as a sheet**: omit the pierced base pairs from a Rothemund lattice at the
   32 bp crossover interface spacing and count connected components by union-find.

### What would falsify this approach

| # | falsifier | consequence if it fires |
|---|---|---|
| **F1** | the total beam area coming out **below** the footprint | the task closes on a division and the expensive part was unwarranted |
| **F2** | some beam orientation packing 45 in **one** level | the answer is a layout, and `C-0035`'s scales become placements |
| **F3** | a level assignment existing at 2 or 3 levels inside `C-0017`'s envelope | stacking is the answer and the cost is a height budget |
| **F4** | the packing-limited count reaching **34** | §3's desired stroke packs on the specified tile and the two clauses do **not** differ in kind |
| **F5** | the regular tie-hole grid leaving the superstructure **connected** | the severance is not a finding, and only the 326 nm² area stands |
| **F6** | the re-placed spans not reproducing `C-0030` at 45 paths | the pipeline is wrong and nothing downstream is usable |

### Cost

Milliseconds. Every ingredient is closed form or a bisection `C-0030` already owns; the only loops
are over 45 flexures, a few hundred orientations and a few dozen path counts. **The expensive
alternative — a general 2-D packing optimiser over free midspan positions — is not merely more
expensive, it is the wrong object**: the midspans are pinned by the ties, so there is nothing to
optimise over except the orientation and the level, both of which are enumerable.
