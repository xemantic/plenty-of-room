# T-125 — Sweep the row phases of `C-0055`'s upward arm array

| | |
|---|---|
| **Raised by** | [`C-0061`](../claims/C-0061-stacked-arm-sheet.md) (`T-121`), *Still open* item 1 — *"the row phases are a free variable nobody has swept"* — and [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md), which is the charge this task has to answer |
| **Leaf** | **`A8.2`** (structural rigidity and joint stiffness), with **`A1.2`** for the anchoring scheme the placement belongs to |
| **Verification type** | **logical** (a lattice-arithmetic census of the placements the upward azimuth can supply — a count and a congruence, no mesh) **+ in-silico** (`C-0009`'s grillage and `C-0006`/`C-0047`'s flatness pipeline re-run at every phase under `C-0022`'s **solved** load, `C-0058`'s distribution family and its Woodbury surrogate **re-run as libraries**) |

---

## Formulate

### The question, stated so that it can fail

`C-0061` re-ran the flatness pipeline with the coupling entering at `C-0055`'s **own** 34 upward arm
roots and found the worst placement result in the programme: a **uniform** coupling there dishes
**0.4156** of the free-tile stroke against **0.3079** free — **1.35× worse than no coupling at all** —
and `C-0058`'s flat rim rule reaches only **0.1649** against its published **0.0753**. That is
`CH-0074`: *the flat distribution lives on stations no placement supplies.*

But `C-0055`'s placement is **one** placement, produced by a scheduler that fills every row greedily
from the low-`x` end, and `C-0061` observed in a single line that its coupling centroid sits at
`x = −8.80 nm` on a tile running −20 to +20. **The row phases — which roots of its own 10.88 nm
lattice each row uses, and which way each row is filled — are a free design variable, and nobody has
swept them.**

The question is therefore: **over every placement the upward lattice can supply at `C-0055`'s
self-consistent 34 roots, what is the flattest, and does it clear anything?**

Three bars, in the order they must be cleared:

1. **No coupling at all** — the free tile's **0.3079**. `C-0047` established that a coupling can be
   worse than none, and `C-0061` found exactly that at these stations. A placement that does not beat
   0.3079 is not a design.
2. **`T-5b`'s convention** — **0.10** of the stroke.
3. **`C-0058`'s published 0.0753** on `C-0015`'s 3 × 15 grid, which is the number `CH-0074` says no
   placement supplies.

### The acceptance predicate

**The flatness of the best 34-root placement under `C-0022`'s solved load, against `T-5b`'s 10 %
convention**, delivered with:

- the best placement **exhibited** (its phase, its roots row by row, its symmetry), not just its number;
- all three reference points quoted — free tile **0.3079**, `C-0055`'s own placement **0.4156**,
  `C-0058`'s 3 × 15 flat design **0.0753**;
- a plain statement of whether it beats **no coupling at all**, and whether it is inside **0.10**;
- whether `C-0058`'s **flat distribution** becomes available on the swept placement, which is what
  settles `CH-0074` either way — resolved if it does, **hardened** if it does not;
- the **cost**: peak per-path force against the 10 pN unzip allowable and `C-0049`'s `n·a/s` ceiling,
  and `C-0014`'s per-path thermal force `√(k_BT k)/N`, because over-stiffening is not free.

### Units and conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly),
  rigidities **pN·nm²**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them in the sheet plane, `z` **normal** and positive
  **upward** — away from the grafted layer, which lies below the tile (`C-0055`). `w` is positive
  **downward**, compressing the layer (`C-0006`, `C-0009`).
- **A placement is a set of upward roots**, one per arm: row `r`, position `x` on that row's own
  upward site lattice. An **upward site** belongs to **one** duplex, so its pitch is the bare
  **32 bp = 10.88 nm**, and adjacent rows' lattices are offset by **16 bp = 5.44 nm** (`C-0055`).
- **The phase is one variable, and it sets both lattices.** The crossover planes are at
  `x = φ·0.34 + 2.72 k` nm; the sheet's own columns are the planes with `k` even, and row `r`'s
  upward (`EAST`) sites are the planes with `k ≡ 2r + 3 (mod 4)`. **A design chooses `φ`, and the
  host's column count and the arm roots follow together.** `φ` is quantised to base pairs and its
  period is **32**, not 16 (`C-0015`).
- **Dishing** is the deflection with its area-averaged best-fit rigid plane removed, peak over an
  81 × 81 grid, normalised by the **free tile's own stroke** — `C-0006`/`C-0047`'s convention exactly,
  and the one `C-0061`'s 0.4156 and `C-0058`'s 0.0753 are quoted in.
- **The load is `C-0022`'s SOLVED edge profile** at 2 mM, a 10 nm gap and 0.192 V, read from its own
  result file. **A uniform load is vacuous here** — a free plate on a uniform foundation dishes
  *exactly zero* whatever its rigidity — and that is wired in as a runtime falsifier rather than
  stated.
- **The coupling total is `C-0017`'s 33.3333 pN/nm**, held fixed in every row of every table; a
  distribution is a redistribution, never an addition.
- **Every duplex carries at least one arm.** `C-0055` reports all 15 duplexes bonded at 34 arms; a
  placement that abandons a duplex is a different device and is excluded by construction.

---

## Plan

### The cheap bounds, which run before any solve

| | bound | what it settles | falsifier |
|---|---|---|---|
| **1** | **the count arithmetic**: 34 arms over 15 rows with at most 3 per row is `3a + 2(15−a) = 34`, i.e. **exactly four rows of three and eleven of two** | the shape of the whole design space, in one line | a row that can carry four arms, which would make the space larger than enumerated |
| **2** | **the centro-symmetry congruence**: a row's roots can be symmetric about the tile centre only if its lattice offset satisfies `2c ≡ 0 (mod 10.88)`, which happens at **exactly two of the 32 phases** | *which* phases can supply a symmetric placement at all — and whether they are `C-0015`'s eight-column ten | the argmin sitting at a phase this bound excludes |
| **3** | **`C-0058`'s reachable dishing floor** at `C-0055`'s own stations — the least-squares minimum over **all** force vectors, hence a rigorous lower bound on the peak dishing of **every** distribution | whether `CH-0074` can be settled by a distribution at all, before any distribution is searched | the floor being below 0.10, which would mean the search, not the stations, is what is failing |
| **4** | **a set-membership check**: are `C-0061`'s *mirrored* roots on the upward lattice? | whether the 0.3558 and 0.1649 of `C-0061`/`CH-0074` are quoted on a buildable station set | — |

### The method, and its cost justification

**The expensive object is a lattice solve; the cheap one is a placement.** `C-0058`'s
`InfluenceSurrogate` is an exact Woodbury reduction: given the free solution and one unit-point-load
solution per station, the response of *any* coupling at *any* subset of those stations is a small dense
solve plus a field superposition. The upward lattice offers at most **60** roots per phase, so **one
bank of 61 solves per phase** makes every placement at that phase cost a 34 × 34 Cholesky instead of an
855-degree-of-freedom one — and the bank shares a single factorisation of the host, because the stations
enter as loads and not as stiffness.

That is what makes a genuine sweep affordable:

1. **All 32 phases**, each with its **own host** — the same `φ` sets the sheet's columns and the roots,
   so a placement is never evaluated on a sheet it does not belong to. (`C-0061` ran `C-0055`'s `φ = 0`
   roots on the nominal **eight-column** host, which is the `φ = 8` lattice; the departure is measured
   here rather than assumed small.)
2. **The centro-symmetric family swept EXHAUSTIVELY** at the phases bound 2 admits — every placement
   whose root set is invariant under `(x, y) → (−x, −y)`, which is a complete enumeration and not a
   search.
3. **The general space by deterministic coordinate descent** — row-option moves at fixed per-row count,
   plus promote/demote pairs that keep the total at 34 — from several named starts at every phase,
   reported as a **descent** and never as a global optimum, with bound 3 quoted beside it as the honest
   statement of what may be left.
4. **The distribution question only on the placements that survive**: `C-0058`'s one-parameter rim
   family, its full per-path optimiser, and the reachable floor.

**What would falsify this approach**

- **F1** — the surrogate disagreeing with an assembled `OrigamiGrillage` solve at the same stations.
  Superposition is exact for a linear system, so any departure above round-off means the bank is wrong,
  and the whole sweep with it.
- **F2** — a uniform load producing non-zero dishing on the free tile, which would mean the load case,
  not the placement, is being measured.
- **F3** — `C-0061`'s **0.4156** failing to reproduce at its own configuration. It is the only
  published number on these stations and it is the anchor of the whole task.
- **F4** — the best placement failing to beat the free tile's 0.3079. Then no station set the upward
  lattice can supply is worth coupling to at all, `CH-0074` hardens from *"this station set is not
  flat"* to *"no station set this lattice supplies is flat"*, and the `E5a` upward array stops being a
  coupling placement.
- **F5** — the argmin landing at a phase bound 2 excludes, which would kill the cheap bound as a
  predictor and make the descent the only evidence.

### What this task does NOT do

- It does not re-open `C-0061`'s rigidity result. The arms add **exactly zero** static stiffness at one
  tie, so the sweep is run on the host with the coupling at the roots; that the two agree is asserted as
  a gate at the best placement rather than assumed.
- It does not sweep `C-0022`'s **other** solved states. `C-0058` shows a rim design flat at three of
  five and dishing 0.187 at the 2 nm gap; `T-123` owns that question and this task inherits its warning.
- It does not change the arm, the count or the mandate: `C-0039`'s **8.164 nm** at `C-0055`'s
  self-consistent **34**, `C-0017`'s **33.3333 pN/nm**, all fixed.
