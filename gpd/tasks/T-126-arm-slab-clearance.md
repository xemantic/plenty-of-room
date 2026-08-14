# T-126 — Does the arm slab clear `C-0035`'s tie-down path?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring scheme |
| **Raised by** | [`C-0061`](../claims/C-0061-stacked-arm-sheet.md)'s *Still open* item 4 — *"the clearance between the arm slab and `C-0035`'s tie-down path. Stated with a geometry (1.69–3.69 nm above the sheet, 46.3 % of the plan) and not solved"* — and by [`C-0063`](../claims/C-0063-upward-root-placement.md)'s own item 4, which says the arm **directions** are chosen greedily and *"`C-0035`'s clearance question is a plan-view one"* |
| **Verification type** | **logical** (exact plane and section geometry on measured lattice constants — no mesh, no fitted parameter) **+ in-silico** (`C-0063`'s placement, `C-0009`'s grillage and `C-0022`'s solved load re-run as libraries for the one number the geometry cannot supply) |
| **Acceptance predicate** | a **plan and section** answer — for each of §3's tie counts, the number of tie-downs that clash with the arm slab, at the best arm-direction assignment, together with the section statement that decides whether a clash is level-independent — **or** the statement that the only buildable mounting and the only workable arm array cannot share one face |
| **TRL** | **1–3.** Nothing here is measured, and the motif is not demonstrated: `C-0029`'s and `C-0055`'s findings are upstream of every number |

---

## Conventions, restated rather than inherited

- Lengths **nm**, areas **nm²**, forces **pN**, stiffness **pN/nm**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- **Plan.** `x` runs **along** the host sheet's helices, `y` **across** them, origin at the tile
  centre. **Section.** `z` is normal and positive **upward**, away from the grafted layer, with
  `z = 0` on the sheet's own **mid-plane**; §1's bias pulls the tile **down**.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`** — the SAXS single-layer interhelical
  distance — so two parallel duplexes at exactly `d` are **tangent and admissible**. `C-0041`'s
  and `C-0053`'s convention verbatim; the 2.0 nm steric and 2.73 nm square-lattice readings are
  swept.
- **In section a duplex is a cylinder of radius 1.0 nm** — the B-DNA phosphate radius, which **is**
  the steric surface (`CLAUDE.md`). So the sheet occupies `z ∈ [−1.0, +1.0]` and an arm laid one
  interhelical distance above it occupies `z ∈ [1.69, 3.69]` — `C-0061`'s slab, re-derived.
- **An arm** is `C-0055`'s: a duplex lying **parallel** to its host, one interhelical distance
  above it, held by **one** antiparallel crossover at an `EAST` (upward, `+z`) site. It is rooted
  at its hinge and **not** centred on it, so `+x` and `−x` are different designs (`C-0053`).
- **A tie-down** is `C-0035`'s: a **duplex standing normal to the sheet**, descending from a
  flexure midspan through the output superstructure onto the tile. `C-0023` makes it two-sided and
  therefore a duplex rather than a strand; `C-0029` makes its landing a two-link joint at a duplex
  end, quantised at the **0.34 nm** rise along `x` and **not** on a crossover lattice.
- **The arm's rotation axis is taken ACROSS its own length at the root**, so a stroke lifts the tip
  out of the sheet plane. This is `C-0055`'s deliberately unadjudicated open item 2, and it is
  stated here as a convention: under the other reading (`C-0040`'s hinge line, which runs **along**
  `x`) the arm spins about its own axis and delivers **no** stroke at all, so the element would not
  exist to have a clearance.
- **The stroke is §3's ACCEPTABLE 3 nm.** The desired 10 nm is evaluated and reported.

## The question, made precise

`C-0035` settled the flexure mounting and found exactly one buildable survivor, `Su`: standoff
bases on the **output superstructure**, standoffs pointing **away** from the tile, the flexure
**outboard** of its own ground, and each midspan tied back **down** through that ground to the
tile. Its ledger records *"the tile now carries no out-of-plane element at all"*.

`C-0055` then bought the programme's escape by rooting the flexure hinges on the **unused** `EAST`
azimuth — an out-of-plane element on the tile, on the `+z` face, exactly the face `C-0035`'s ties
cross. `C-0061` priced the array at zero in rigidity and named this clearance as open;
`C-0063` moved the placement to phase 24 and made the tile flat at 0.0706 with equal springs.

**Two bodies now want the same face:** 34 arms occupying a slab above the sheet over 46.3 % of the
plan, and `N` tie-downs descending through it. The predicate is a plan and a section, not an area.

## Plan — the cheap bounds first, and what would falsify the approach

| | bound | why it is cheap | what it settles |
|---|---|---|---|
| **1** | the **plan area** of 34 arms plus 45 ties against the 1614 nm² footprint | one multiplication | whether an area budget decides it. `C-0041` established that an area bound is exactly what invites *"stack it in three levels"*, so this one is run **in order to be refuted** |
| **2** | the **section**: does the tie's clear column contain the arm slab's band? | three comparisons | whether a plan overlap is **level-independent**. If it is, the section reduces the whole question to plan and no stacking can help — `C-0041`'s Fact A in a new place |
| **3** | the **swept** plan envelope against the rest footprint | `√(L² − s²)` | whether the sweep is adverse. A rotating arm's plan projection is a **cosine**, so the swept envelope may be the rest footprint identically |
| **4** | the **root pitch minus the arm**, against a tie's own width | one subtraction | the width of the only gap the lattice offers between two consecutive same-sense arms — `10.88 − 8.164` against `2.69` |
| **5** | the `EAST` **inventory** at phase 24 against 34 arms plus `N` ties | a census `C-0055` already has | whether a tie rooted out of plane could be placed at all. Reported **conditionally**, because `C-0029`'s two-link junction is *not* on the crossover lattice |

**The expensive part is warranted only if bound 2 fires** — if a plan overlap could be relieved by
stacking, the question would be a level count and not a clearance.

**What would falsify this approach:**

1. **the section bound not firing** — a tie that can pass over an arm. Then the question is
   `C-0041`'s level assignment and this task is formulated on the wrong quantity;
2. **the swept envelope exceeding the rest footprint** — then a static plan view is not
   conservative and the whole geometry has to be re-done as a swept-volume problem;
3. **the interleave being decided by room rather than by registration** — if the free tie capacity
   of a row were below the demand, the answer would be *"the tile is too small"* (`T-102`) and not
   a placement statement;
4. **zero arms failing to reproduce `C-0035`'s clearance ledger** — the strong free limiting case,
   and the one that says this model is the same model.

## Method

1. The section: the arm slab's band at stroke `s`, the tie's clear column, and the
   level-independence theorem that follows.
2. The plan, at **`C-0063`'s** placement (phase 24, centro-symmetric, 34 roots) — **not**
   `C-0055`'s, and not `C-0061`'s mirrored alternative, which `CH-0076` showed is on the `WEST`
   azimuth and hangs 16 of 34 arms into the grafted layer.
3. The **arm directions** as the free variable `C-0063` left open: every feasible assignment per
   row, exhaustively, because the rows are independent (`C-0055`'s gate 3).
4. The tie array at 1, 2 and 3 columns of `C-0015`'s `m × 15` grid; then the same grid rigidly
   translated; then the ties freed in `x` within their own row.
5. The one number the geometry cannot supply: what freeing the registration costs in **flatness**,
   on `C-0009`'s grillage under `C-0022`'s solved load, against `C-0047`'s 0.218 and `C-0063`'s
   0.0706.

## Units, locked

nm, pN, pN·nm, pN/nm, pN/nm² (= 1 MPa exactly); `k_BT = 4.141947 pN·nm` at 300 K.
Interhelical distance **2.69 nm measured**; crossover interface spacing **32 bp**; the phase
quantised to base pairs with period **32**; rise **0.34 nm/bp**.
