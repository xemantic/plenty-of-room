# T-110 — What does spending 80–100 % of the tile's crossovers on HINGES do to the sheet?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the count belongs to |
| **Raised by** | [`C-0046`](../claims/C-0046-fewer-longer-flexures.md), its *"Still open"* item 1, which it names *"the largest open item this claim leaves and it is new"* |
| **Verification type** | **logical** (a pigeonhole count on a lattice whose pitch is cited and whose inventory `C-0015` counts — no simulation can move a count) **+ in-silico** (`C-0009`'s beam-and-hinge grillage and `C-0006`'s continuum plate re-run with the consumed crossovers actually removed, under `C-0022`'s **solved** electrostatic load) |
| **Units** | lengths **nm**, forces **pN**, stiffness **pN/nm**, moments and rigidities **pN·nm**, pressure **pN/nm²** (= 1 MPa exactly); `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂** |

---

## Formulate

### The question

Every flexure design this programme has left spends the tile's own crossovers as hinges.
`C-0040` puts the whole-tile inventory at **49–56**;
`C-0046`'s three surviving designs spend **45, 50 and 56** of them — 80 %, 89 % and 100 %.
But the sheet's own mechanics are built from those same crossovers:
they are the **only** across-helix compliance in a single-layer sheet
(`CLAUDE.md`: CanDo treats crossovers as rigid *and says so*, which is fine for a multilayer bundle and wrong here),
and `C-0009`'s `D_⊥`, `C-0015`'s flatness grid and `C-0006`'s load distribution all rest on them.
**Nobody has asked what the sheet is like once the coupling has eaten them.**

### The question BEHIND the question, which has to be settled before anything is computed

A crossover used as a **hinge** for a flexure may or may not still serve as an **interface** crossover for the sheet.
`C-0040`'s series-composition result is about *hinge participation*, not about whether the sheet still has its own connectivity.
So the first deliverable is a decision, from the geometry, on whether the two uses are

- **exclusive** — the crossover leaves the sheet when it becomes a hinge;
- **additive** — the hinge is a *new* junction and the sheet keeps its own;
- **partially shared** — the site is contested but some function survives.

**The whole answer turns on this and it is stated explicitly rather than assumed.**

### The numeric target and the acceptance predicate

| id | predicate | threshold |
|---|---|---|
| **`P1`** | **`D_⊥` as a function of the consumed fraction `f`**, on **both** averaging conventions, with the anisotropy `D_∥/D_⊥` beside it | reported at `f = 0, 0.25, 0.5, 0.75, 0.804, 0.893, 1.0`; `f = 0` must reproduce `C-0009`'s **3.397 pN·nm** and its 25.6× anisotropy |
| **`P2`** | **connectivity** — the number of disconnected components of the sheet at each `f` and each consumption pattern, and the **largest `f` that keeps the sheet in one piece** | exact integer count; the bound is a pigeonhole and must be derived before any solve |
| **`P3`** | **`C-0015`'s flatness re-run at 80 % and 100 % consumption under `C-0022`'s SOLVED load**, lattice and continuum plate, with the excess quoted | peak dishing over the free-tile stroke, against `T-5b`'s 10 % convention and against `C-0047`'s 0.218 for the same grid at `f = 0` |
| **`P4`** | **`C-0006`'s load distribution re-run at the same points** — peak per-load-path crossover force and peak duplex shear, against the 10 pN unzip allowable and the 65 pN nicked ceiling | reported with the interface each peak sits on |
| **`P5`** | **`C-0010`'s positional variance** re-run, and an explicit statement of whether its *"a 2× change in `D_⊥` moves the answer by 2.5 %"* insensitivity survives at 100 % consumption | lattice dishing RMS and centre RMS, plate beside it |
| **`P6`** | **the largest hinge count a CONNECTED sheet can supply**, and whether it clears §3's acceptable stroke on `C-0046`'s own elastica | a path count and a usable stroke, resolving `C-0046`'s unresolved `34 < n ≤ 45` bracket |

### Geometry and sign conventions, restated rather than inherited

- **Plan view.** `x` runs **along** the tile's helices, `y` **across** them, origin at the tile centre;
  `z` positive **upward**, away from the electrode. §1's bias pulls the tile **down**, and `w` is positive **downward**.
- **The sheet.** 40.0 × 40.35 nm, **15 duplexes** at the SAXS-measured **2.69 nm** interhelical distance,
  **8 symmetrically centred crossover columns** at 16 bp = 5.44 nm, alternating between a helix's two neighbours —
  `C-0009`'s nominal `T-10` layout, so that `f = 0` is that claim's own lattice and nothing else.
- **An interface** is the pair `(b, b+1)` of adjacent duplexes; there are **14**, and each carries the columns of one parity,
  so the per-interface pitch is **32 bp = 10.88 nm** and the inventory is **56**.
- **A consumed crossover** is one whose junction has been re-routed to a flexure arm instead of to the neighbouring
  sheet duplex. It supplies **neither** the interface's dihedral spring `k_θ` **nor** its vertical link.
- **The consumed fraction** `f = consumed/inventory`. `C-0046`'s three surviving designs are
  `f = 45/56 = 0.8036`, `50/56 = 0.8929` and `56/56 = 1.0000`.
- **A component** is a connected set of duplexes under the retained crossovers. The interfaces form a **path graph**
  on the 15 duplexes, so the component count is exactly `1 + (empty interfaces)`.

### The upstream gotchas this task must not walk into

1. **A flatness count is meaningless without its load case** (`CLAUDE.md`, `CH-0034`):
   a uniform load on a uniform Winkler foundation dishes a free plate **exactly zero**, whatever the rigidity.
   So the flatness numbers are taken under `C-0022`'s **solved** profile, read from its own result file
   and keyed on concentration, gap **and bias** (`CLAUDE.md`: an upstream result file may hold more than one record per state).
2. **A discretisation is not automatically a relaxation** (`CLAUDE.md`, `C-0009`):
   the lattice is *softer* under a point load entering it and *stiffer* under a point reaction and a smooth load.
   The continuum plate runs beside the lattice at every point and the excess is quoted.
3. **Mesh monotonicity holds only on nested refinements** — sweep 1/2/4, never 1/2/3/4.
4. **A dense `n × n` matrix per element type is what turns a comfortable lattice solve into an OOM** —
   assemble into one stiffness matrix and expose contributions as energies. `OrigamiGrillage` already does; nothing here changes it.

---

## Plan

### The cheap bound, which runs first and settles the shape of the answer

**A connected sheet needs at least one retained crossover on every interface.**
There are `D − 1 = 14` interfaces and `N = 56` crossovers, so

&nbsp;&nbsp;&nbsp;&nbsp;**`f_max = 1 − (D − 1)/N = 1 − 14/56 = 0.750`** &nbsp;(and `1 − 14/49 = 0.714` at the 22 seven-column phases).

`C-0046`'s admissible region is **0.80–1.00**.
**Every point of it is above the bound**, so the sheet is disconnected before any matrix is assembled —
and the component count follows by pigeonhole: `11` retained over 14 interfaces leaves **≥ 3** empty,
`6` leaves **≥ 8**, and `0` leaves **14**, i.e. **15 separate duplexes**.

Four arithmetic operations, no mesh, no fitted constant, and it decides `P2` outright.
**If this bound had come out above 1.0 the task would have closed in a paragraph** — that is falsifier 1.

The second cheap bound is the rigidity itself, and it is where the two averaging conventions part:

| convention | expression | what it is |
|---|---|---|
| **uniform curvature** (Voigt) | `D_⊥ = k_θ d² N_ret/A` | the energy of an *imposed* `w = ½κy²` field — `C-0009`'s own gate-2 identity, **linear in the retained count** |
| **uniform moment** (Reuss) | `D_⊥ = L_y k_θ /(L_x Σ_i 1/n_i)` | 14 hinges in **series**, which is how a sheet actually bends across the helices — **exactly zero as soon as one interface empties** |

They agree for a uniform lattice up to `(D/(D−1))² = (15/14)²`, which is asserted rather than tolerated.

### The expensive part, and why it is worth its cost

`OrigamiGrillage` is re-run with the consumed crossovers **removed from the assembly**, not smeared.
That is the only way to see the difference between the two conventions above,
and it is the difference between "the sheet is 5× softer across the helices" and "the sheet is in four pieces".
The plate runs beside it with `D_⊥` scaled by `(1 − f)` — the smeared reading a continuum can express — and the excess is the deliverable.

Two coupling **placements** are carried, because the geometry decision has two readings and they differ:

- **`GRID`** — `C-0015`'s 3 × 15 = 45 attachments where they stand, the consumption accounted separately;
- **`AT_HINGE`** — the attachments sit **at the consumed crossover sites**, because a hinge *is* an attachment.

Three consumption **patterns** are carried, because *where* the crossovers are taken from is a design variable
and `C-0015`'s own lesson is to sweep shapes rather than counts:
**`SPREAD`** (as evenly over interfaces and columns as the count allows),
**`INTERFACE_FIRST`** (whole hinge lines of four, `C-0040`'s `L3`),
**`COLUMN_FIRST`** (whole columns).

`P6` needs no new model at all: `C-0046`'s `tradePoint` is called as a library at the connectivity ceiling.

### What would falsify this approach

| # | falsifier | what it would mean |
|---|---|---|
| **1** | the pigeonhole bound coming out at or above 1.0 | consumption could never disconnect the sheet and `P2` would be vacuous |
| **2** | `f = 0` failing to reproduce `C-0009`'s `D_⊥`, its 1.015467 lattice/plate ratio, or `C-0047`'s 0.218 dishing at 3 × 15 | the removal machinery is not a modification of the standing lattice and nothing downstream would be comparable |
| **3** | a uniform load dishing anything but exactly zero at any `f` | the solver is wrong, not the physics (`CLAUDE.md`) — a free tile on a uniform foundation translates |
| **4** | the two `D_⊥` conventions failing to agree at `f = 0` up to `(15/14)²` | the series derivation is wrong |
| **5** | the connectivity ceiling clearing §3's acceptable stroke comfortably | the branch would survive with a connected sheet and this task would be a caveat rather than a verdict |

### The five verification gates

1. **Dimensional** — `D_⊥` is `k_θ` × a length; the consumed fraction is a pure count over a count;
   the component count is an integer; unphysical arguments throw.
2. **Limiting cases** — `f = 0` is `C-0009`'s lattice **identically** (same crossovers, same matrix, same numbers);
   `f = 1` has no crossovers, `D_⊥ = 0` on both conventions, 15 components, and a peak crossover force that does not exist;
   a uniform load dishes exactly zero at **every** `f`; the two conventions agree at `(15/14)²` on any uniform lattice.
3. **Symmetry and conservation** — force balance at every `f`; the retained crossovers on one interface carry exactly the
   shear crossing it, from cut equilibrium computed independently; the component count from union-find equals
   `1 + (empty interfaces)` — two independent routes; the Voigt rigidity is exactly linear in the retained count.
4. **Numerical convergence** — nested mesh refinement 1/2/4; the link penalty swept; the load quadrature panels refined;
   the result file re-emitted and diffed.
5. **Literature and upstream** — `C-0009`'s `D_⊥`, anisotropy and lattice/plate ratio; `C-0015`'s inventory and 45-path grid;
   `C-0040`'s census and per-interface pitch; `C-0047`'s dishing at 3 × 15 and 1 × 15 under the same solved profile;
   `C-0046`'s `(45, 1)`, `(50, 1)` and `(56, 1)` designs re-placed as a library.

### Deliverables

`gpd/tasks/T-110-consumed-crossover-sheet.md` (this file),
`src/main/kotlin/structure/ConsumedCrossoverSheet.kt`,
`src/main/kotlin/structure/ConsumedCrossoverSheetStudy.kt`,
`src/test/kotlin/structure/ConsumedCrossoverSheetTest.kt`,
`gpd/results/T-110-consumed-crossover-sheet.json`,
and a claim — with a challenge against whichever standing claim the numbers move.
