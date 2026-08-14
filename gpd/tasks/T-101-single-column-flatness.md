# T-101 — Is a 15-attachment scheme flat under the SOLVED load?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the count belongs to |
| **Raised by** | [`C-0041`](../claims/C-0041-flexure-array-packing.md), whose *"Still open"* item 1 is this task and which names it *"the largest open item this claim leaves"* |
| **Verification type** | **in-silico** (`C-0009`/`C-0015`'s beam-and-hinge grillage and `C-0006`'s continuum plate, both loaded through `C-0022`'s **solved** electrostatic profile read from its own result file) **+ logical** (a closed-form Winkler bending length that settles the column count before any matrix is assembled) |
| **Units** | lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**, energy **pN·nm**; `k_BT = 4.141947 pN·nm` at **300 K**, aqueous buffer with **Mg²⁺** |
| **Maturity target** | **TRL 1–3.** Model-consistent and traceable. Nothing here is measured, and the flexure motif this count belongs to is **not demonstrated** (`C-0028`, `C-0029`). |

---

## Formulate

### The question

`C-0041` establishes that the Gen-1 tile carries **exactly fifteen** flexures, in a **1 × 15** column at
one of 720 orientations, with the ties **staggered 8 bp** row to row so the superstructure is not severed.
Fifteen is **below the range** either `C-0015` (which searched grid *shapes* from 45 upward) or `CH-0034`
(which swept 45 → 225) examined. `C-0041` says so explicitly and hands the question here.

**Is a 1 × 15 attachment scheme flat, under the load `T-3b` actually solved, against `T-5b`'s 10 % convention?**

### The numeric target and the acceptance predicate

**Acceptance.** The dishing of a **1 × 15** grid — collinear and 8 bp-staggered — under `C-0022`'s solved
electrostatic profile, expressed as a fraction of the free-tile stroke, stated with the load case and the
tolerance both named, together with:

1. the same quantity for `C-0015`'s **45 as 3 × 15** and `C-0009`'s **64 as 8 × 8** at the same load case,
   so the reader can see what dropping to one column costs;
2. the **orientation** contrast — a single attachment column laid **across** the helices (1 × 15, one
   attachment per duplex) against one laid **along** them (15 × 1, fifteen attachments on one duplex) —
   with the sheet's own 25.6× rigidity anisotropy quoted as the reason;
3. the **lattice** run beside the **continuum plate**, with the excess quoted per `CLAUDE.md`, and the sign
   of the excess reported rather than assumed (a discretisation is **not** automatically a relaxation);
4. an explicit statement of whether `C-0022`'s **32.1 % irreducible lever/sensor split** is affected.

**Falsifiable form.** `flat` ⟺ peak dishing `< 0.10 ×` the free-tile stroke — `T-5b`'s convention, cited from
`C-0015`, **a convention and not a physical threshold**.

### Geometry and sign conventions, restated rather than inherited

- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre.
- `w` is positive **downward**, compressing the polymer layer (`T-5`'s convention, unchanged).
- The tile is **40.0 nm along `x`** and 15 duplexes at the SAXS-measured `d = 2.69 nm` **across `y`**,
  i.e. 40.35 nm; footprint 1614 nm².
- An attachment grid of `columns × rows` places `x_i = 40(i + ½)/columns − 20` and
  `y_j = 40.35(j + ½)/15 − 20.175 = (j − 7)·2.69` — the duplex axes exactly (`C-0026`).
- A **stagger** of `s` nm displaces the attachments of even rows by `+s/2` and of odd rows by `−s/2`
  **along `x`**. `C-0041`'s remedy is `s = 8 bp = 2.72 nm`, alternating `±1.36 nm`. The stagger is
  quantised to the 0.34 nm rise, because a DNA design cannot place an attachment between base pairs.
- The **load** is a downward pressure of interior value `100 pN / 1614 nm²` modified by `C-0022`'s solved
  collar — smooth raised-cosine term plus rim residual, both read from
  `gpd/results/T-3b-tile-edge-load-profile.json`. A collar **depth is negative for an enhancement**, which
  is the sign `C-0022` solved. The **total** load therefore exceeds 100 pN by `C-0022`'s edge gain; that is
  `C-0022`'s own convention and `C-0026`'s.
- The **coupling** is `n` equal linear springs to ground totalling `33.3333 pN/nm` (`C-0017`'s mandate),
  one at each attachment.
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit **plane** —
  piston and both tilts removed — so a rigid translation and a rigid tilt cost nothing.
- The **free-tile stroke** is the mean deflection of the *unsupported* plate under the *uniform* load at
  the same foundation stiffness: 4.907 nm at `k_f × 1`. `C-0006`'s, `C-0015`'s and `C-0026`'s normaliser,
  unchanged, so that every number here is comparable with theirs.

### The upstream gotcha this task must not walk into

`gpd/results/T-3b-*.json` carries **two** solved profiles per `(concentration, gap)` — one per operating
bias — so a lookup keyed on `(concentration, gap)` alone silently takes whichever is listed first, at a bias
`C-0022`'s headline table does not use. Every profile lookup here is keyed on
**`(concentration, gapHeight, appliedBias)`**, and the bias travels into the result file with the record.

---

## Plan

### The cheap bound, which runs first and is a closed form

A duplex on the polymer layer is a beam of rigidity `EI` on a Winkler foundation of `k_f d` per unit length.
Its **bending length**

&nbsp;&nbsp;&nbsp;&nbsp;**`ℓ = (4 EI / k)^(1/4)`**

is the decay length of the beam's own influence patch: a point support flattens the sheet over roughly `±ℓ`
and does nothing beyond it. Two numbers follow with no matrix at all —

- **along** the helices, `EI = 230 pN·nm²` per duplex over `d = 2.69 nm`, so `ℓ_∥ ≈ 12.8 nm`;
- **across** them, `D_⊥ = k_θ d / p ≈ 3.3 pN·nm` (the crossover hinge, not the duplex), so `ℓ_⊥ ≈ 5.7 nm`.

The attachment *row* pitch is 2.69 nm — **0.47 `ℓ_⊥`**, comfortably dense. The attachment *column* pitch at
one column is the whole 40 nm edge — **3.1 `ℓ_∥`**. The ratio passes through 1 between three and four
columns, which is `C-0015`'s answer of three arriving before any solve, and it predicts that **one column
cannot flatten a 40 nm tile along the helices at any row count**.

**Cost justification.** The bound is four arithmetic operations against a 1665-degree-of-freedom Cholesky
factorisation per state, and it decides the *structure* of the answer — whether the question is a saturation
(as `CH-0034` found between 45 and 225) or a genuine loss. It cannot deliver the number `T-5b`'s predicate
asks for, because the predicate is written on a peak dishing under a specific non-uniform load, so the
expensive part still has to run; what the bound buys is knowing in advance which of the two regimes to look
for and where a wrong answer would show.

**What would falsify the cheap bound:** the solved lattice putting the 1 × 15 dishing within 2× of the
3 × 15 dishing under the same load. That would mean the influence patch is not the controlling length and
the whole bending-length argument is wrong.

### The expensive part

Re-run `C-0026`'s pipeline — `structure`'s grillage and `structure`'s plate, `C-0022`'s solved field — at
grid shapes neither `C-0015` nor `CH-0034` ran:

| what | why |
|---|---|
| 1 × 15 **collinear** | `C-0041`'s count, the baseline |
| 1 × 15 **staggered**, 0 / 4 / 8 / 16 / 32 / 64 bp | `C-0041`'s connectivity remedy; nobody has priced it in flatness *or* in crossover force |
| **15 × 1** | the other orientation of a single column — the one the lattice does **not** supply |
| 2 × 15, 3 × 15, 4 × 15, 5 × 15, 8 × 15, 15 × 15 | the column sweep, to locate the break-even against **no coupling at all** |
| 8 × 8 | `C-0009`'s count, at the same load case |
| **no coupling** | the reference `CH-0034` never quotes: `C-0022`'s free tile at 0.321 of the stroke |

Load cases: uniform (the case in which the objective is identically zero at infinite count, kept as the
falsifier), `C-0022`'s five headline solved states, and `C-0006`'s assumed taper for audit.

**Why no new model is written.** `C-0026` already reads `C-0022`'s file, already assembles the coupling as
`n` springs, and already runs the plate beside the lattice. Writing a second lattice would be a third
implementation of a solved problem and would not be comparable with `CH-0034`'s table. What is new here is
one placement function — the stagger — and the shapes.

### What would falsify the whole approach

1. **The uniform load producing non-zero dishing on the free tile.** A free plate on a uniform foundation
   translates exactly; if the solver dishes it, the solver is wrong and every number here is void. Wired in
   as a test.
2. **The 1 × 15 dishing coming out at or below `CH-0034`'s 0.149 saturation floor.** That would mean the
   floor is a property of the rim alone and the column count buys nothing in either direction — and would
   contradict `CH-0034`'s own 45 → 225 monotone table.
3. **The 8 bp stagger moving the dishing by more than a per cent.** `C-0041` asserts the stagger is *"free
   of every upstream claim"* and checks it *"for connectivity and for fit, not for flatness"*. If 2.72 nm on
   a 40 nm edge is worth more than a per cent, that assertion needs a challenge.
4. **The staggered grid restoring a crossover force comparable with the per-path static share.** A stagger
   alternating row to row is an *across-helix* symmetry break, which `C-0026` §4 shows is the worst class of
   scatter; if the positional version behaves like the stiffness version, the remedy costs a load path.

### The five verification gates

1. **Dimensional** — dishing is a length; the whole system is linear, so scaling the pressure by `λ` scales
   the dishing by `λ` exactly and leaves `dishing/stroke` invariant; unphysical arguments throw.
2. **Limiting cases** — a **uniform load on a uniform foundation with no supports dishes exactly zero**
   (the free falsifier this project already uses); a zero stagger reproduces `attachmentGrid` identically;
   a staggered grid still has one row per duplex; the column sweep tends to `CH-0034`'s floor.
3. **Symmetry and conservation** — support forces plus foundation force equal the applied force; the
   dishing field is orthogonal to piston and to both tilts; a stagger and its **mirror image** give
   identical dishing; the collinear 1 × 15 keeps `C-0015`'s exact zero crossover force under a uniform
   load, and the staggered one is second order in the stagger.
4. **Numerical convergence** — **nested** subdivisions 1 ⊂ 2 ⊂ 4 (never 1/2/3/4, which moves a point load
   off a node); the plate's basis degree; the dishing sample grid; the strip quadrature.
5. **Literature and upstream cross-check** — `C-0026`'s 1 × 15, 3 × 15, 15 × 3 and 8 × 8 dishing
   reproduced; `CH-0034`'s 0.149 saturation reproduced at 15 × 15; `C-0022`'s free-plate 0.3213 at
   `k_f × 1` reproduced; `C-0041`'s 8 bp = 2.72 nm quantisation reproduced; the 25.6× sheet anisotropy
   reproduced from the sheet's own rigidities.

Note that `1/(1 + (2πℓ/λ)⁴)` — the plate-on-foundation ripple transfer function — is **not** used anywhere
here: it does not apply at a free edge, and every load case in this task is dominated by the rim.

### Deliverables

- `src/main/kotlin/coupling/SingleColumnFlatness.kt` — the staggered placement and the bending length.
- `src/main/kotlin/coupling/SingleColumnFlatnessStudy.kt` — the study main.
- `src/test/kotlin/coupling/SingleColumnFlatnessTest.kt` — the gates, written first.
- `gpd/results/T-101-single-column-flatness.json`.
- A claim in `gpd/claims/`, and a challenge if a standing claim is contradicted.
