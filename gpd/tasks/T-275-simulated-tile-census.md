# T-275 — which crossover census a measured constant is read against

**Leaf:** `A1.2`, with `A8.2` — the same leaf `T-9` sits on.
**Raised by:** [`C-0169`](../claims/C-0169-crossover-vertical-compliance.md) §8 and §9 item 4.
**Reserved:** claim `C-0170`, challenges `CH-0219`, `CH-0220`.

---

## 1. Formulate

### 1.1 The question

[`C-0157`](../claims/C-0157-crossover-hinge-constant-from-oxdna.md) measured `k_θ` on an oxDNA tile
its own generator describes as *"crossover phase 8 … columns at `x = 8 + 16k`, `k = 0…6`"* —
**seven** columns and **49** crossovers.
Every placement, flatness, prestrain and plan result in this corpus is graded on
`rasterColumnLayout(8, sheet, 38.08 nm, admitRowEnd = true)`, which [`C-0169`](../claims/C-0169-crossover-vertical-compliance.md)
§8 reports as **eight** columns and **56** crossovers (4/4 over fourteen interfaces),
falling to **six** and **42** (3/3) when the row-end column is refused.

**Neither grillage reading is seven.**
So the tile oxDNA simulated and the tile the corpus grades on are not obviously the same object,
and the difference is the width of one 8 bp junction plane.

`CLAUDE.md` already records the mechanism:
*a crossover **plane** lands on the row end modulo 8 bp and is a **column** only modulo 16 bp*,
and `CrossoverLayout.EDGE_MARGIN` decides the row-end column at 38.08 nm where it is inert at 40.0.
What is new is that the mechanism has landed on a **measured** object for the first time:
until `C-0157` every quantity in this dispute was a model input.

### 1.2 The deliverables

| | deliverable |
|---|---|
| **D1** | **Which census** `C-0157`'s `k_θ` bracket is read against — the simulated tile's own lattice, named in the corpus's own phase coordinate, or a statement that it is outside the corpus's family. |
| **D2** | **What, if anything, that does** to `C-0157`'s bracket and to the thresholds `C-0169` measured. |
| **D3** | **Whether the simulated tile is the tile the corpus grades on**, answered yes or no with the census that decides it. |

### 1.3 Units, geometry and sign conventions

Locked, and restated because the whole question is a coordinate:

- Lengths **nm**, base pairs **integer**, rise `0.34 nm/bp`, `k_BT = 4.141947 pN·nm` at 300 K.
  Hinge stiffness `pN·nm/rad`, flexural rigidity `pN·nm`.
- `x` runs **along** the helices, `y` **across** them, `z` normal to the sheet; `w` positive downward.
- **Two phase data are in circulation and this task exists because they collide on the integer 8.**
  - The **row-start** datum: `ScadnanoDesign.crossoverPhase()` returns the *first* crossover offset,
    counted from the scaffold's own offset 0. The oxDNA generator and `C-0160`'s headline use it.
  - The **tile-centre** datum: `CrossoverLayout.atBasePairPhase` / `rasterColumnLayout` lay the
    lattice down as `x = phase·rise + k·(p/2)` with `x` measured from the **centre** of the
    footprint. `C-0063`, `C-0090`, `C-0134` and every placement study use it.
  Every phase integer in this task carries its datum explicitly.
- A **junction plane** is an 8 bp azimuth plane; a **column** is a plane of even index, i.e. the
  16 bp sub-lattice a single-layer sheet's two in-plane azimuths occupy. Interface `b` carries the
  columns whose parity matches `b mod 2`, so any one adjacent **pair** is linked every 32 bp.
- A **crossover** is a **staple** crossing. A scaffold crossing on a seamless raster is a **turn**
  and is not a lattice site (`C-0157`, `CLAUDE.md`). A bare *"crossovers"* count on this design is
  ambiguous by `63/49 − 1`.

### 1.4 Acceptance predicates

Falsifiable, and each one is decided by an integer or by a ratio of integers.

| | predicate | how it is decided |
|---|---|---|
| **P1** | On a 112 bp row the row-start and tile-centre phase data differ by **exactly 8 bp**, and that offset is `(rowBp/2) mod 16` — so the *same integer 8* names two different lattices. | arithmetic, no file |
| **P2** | The committed `.sc` carries **49** staple crossovers, **14** scaffold turns and **63** strand crossings; the bare count is ambiguous by **28.57 %**. | census over the file, filtered by strand role |
| **P3** | The design's column set is reproduced by `rasterColumnLayout` at **exactly one** tile-centre **phase** with positions **and** parities matching, at **both** row-end settings — the row-end setting being immaterial there is itself the guard's inertness — and that phase is **not 8**. | exhaustive sweep of all 32 phases × {admit, refuse} |
| **P4** | At tile-centre phase 8 the lattice carries 8 columns / **56** crossovers (4/4) admitted and 6 / **42** (3/3) refused, and **neither is 49**. | census |
| **P5** | The whole 32-phase census is emitted, and the phases that yield 7 columns / 49 crossovers are enumerated rather than asserted. | census |
| **P6** | The price of the mismatch is quoted in the corpus's own currency: the smeared and series `D_⊥` fractions of the two lattices, and the peak dishing of the same load case on each. | two solves through `C-0161`'s importer |

### 1.5 Declared falsifiers

| | fires if | what it would mean |
|---|---|---|
| **F1** | the design's columns match **no** `(phase, admitRowEnd)` at all | the simulated tile is outside the corpus's 32-phase family, and `C-0161`'s import is grading an object no phase sweep can generate — strictly worse than the framing above |
| **F2** | the design matches tile-centre **phase 8** | `T-275`'s premise is wrong: there is no mismatch and `C-0169` §8 is over-read |
| **F3** | the staple-crossover count is not 49, or the turn count not 14 | `C-0157`'s and `C-0160`'s censuses are both wrong and the `k_θ` reading moves with them |
| **F4** | the cheap bound's predicted phase disagrees with the measured match | the arithmetic in P1 is not the mechanism, and the coincidence needs another explanation |
| **F5** | `k_θ`'s estimator reads a lattice **count** | the bracket itself moves, and this is not a census question at all — it is a re-analysis, and the frames are pruned |
| **F6** | the two lattices' `D_⊥` fractions or dishings are **identical** | the mismatch is free, and the finding is that the corpus may keep both objects |

### 1.6 What would falsify the approach itself

- **If the row had an even number of column pitches**, the two phase data would coincide, this
  census would be silent, and the mismatch — if any — would have to be found another way.
  The approach is therefore a census *of this row length* and generalises only as arithmetic.
- **If the roll estimator were non-local** — if `k_θ` were extracted from a plate mode or from a
  whole-interface fit rather than from a per-site variance — no census could bound the transfer
  between the two lattices, because the count would be inside the measurement. `F5` is that test.
- **If `C-0161`'s importer did not reproduce the file's own columns**, every number below is a
  property of the importer. Its published `columnPositionDepartureNm = 1.8e−15` is the guard.

---

## 2. Plan

### 2.1 The cheap bound, which runs first and costs one division

`112 bp = 7 × 16 bp`. Seven is **odd**, so the tile centre sits half a column pitch —
**8 bp** — away from any lattice point of a row-start-phased column lattice.
Therefore on this row, and on every row whose column-pitch count is odd:

```
centre-datum phase  =  row-start-datum phase  +  (rowBp/2) mod 16   (mod 16)
                    =  row-start-datum phase  +  8
```

That predicts, before any file is opened:

1. the generator's *"phase 8"* (row-start) is the corpus's **phase 16** (tile centre),
   whose positions are those of phase 0 with every parity exchanged;
2. at tile-centre phase 8 a column lands **exactly** on each row end
   (`x = ±19.04 = ±edgeX/2`), which is why `EDGE_MARGIN` — and not the physics — decides
   8 columns against 6 there, and why phase 8 is one of `C-0134`'s two row-end phases;
3. at tile-centre phase 0/16 no column touches the edge, the guard is inert, and the count is **7**.

**The cheap bound was run before any Kotlin was written, and it corrected its own predicate.**
`P3` was first written as *"exactly one `(phase, admitRowEnd)` pair"*; the arithmetic returns
**two** pairs at one phase, because where no column touches the edge the row-end setting cannot
change anything. That is not a weaker predicate — it is the guard's inertness, stated as an
observable, and it is the control for `P4`, where the same setting is worth two columns.

**The cheap bound therefore predicts the entire answer.** What the census buys is that it is
*measured on the emitted artifact* rather than derived from a docstring — which matters precisely
because the docstring and the code disagree about which claim they implement.

### 2.2 Method, and its justification against cost

**A census over the emitted `.sc` against the lattice. Not a solve.**

| step | cost | why not the alternative |
|---|---|---|
| the phase-datum arithmetic (§2.1) | one division | — |
| `ScadnanoDesign.fromFile` on `gpd/designs/gen1-sheet-square-15x112.sc`, split by strand role | milliseconds | `C-0160`'s reader exists and `C-0161`'s importer already derives the grillage from it; re-deriving either would be a second implementation of a settled thing |
| the 32-phase × 2 lattice census through `rasterColumnLayout` | milliseconds | the function that every placement study calls, so the census is of the corpus's own object and not of a re-statement of it |
| the `D_⊥` ledger (smeared and series) over each phase's per-interface count vector | arithmetic | `CLAUDE.md` already prices a seven-column sheet at `49/56` and `42/49`; the ledger is what attaches those to a **phase** |
| two graded solves, same load case, phase 16 against phase 8 | seconds | this is the one place a solve is bought, and it is bought because a census cannot say what the mismatch is *worth*; `C-0161` already publishes the phase-16 half |

**The expensive alternative is explicitly declined**: re-running oxDNA on the phase-8 tile is about
one day of wall clock plus 649 MB, it needs a machine this checkout does not have
(`C-0169` §1, checks 1–3), and it cannot be justified until the census says whether the two
lattices differ by enough to matter.

### 2.3 What the deliverables will be read against

- **D1** is answered by P3 and P5: a phase integer in a **named datum**, with the census row beside it.
- **D2** is answered by asking what `k_θ`'s estimator reads (F5) and what the corpus does with it.
  `k_θ = k_BT/Var(roll at a crossover)` is a **per-site equipartition** and carries no count;
  the count enters one step downstream, in `D_⊥ = k_θ·d/p`, which is a **linear density**
  `1/p = 1/10.88 nm⁻¹` — so the deliverable is the comparison of that density against **both**
  lattices' actual crossover counts, and it is a division.
- **D3** is a yes or a no, and the census decides it.

### 2.4 Verification gates

1. **Dimensional** — every quantity here is an integer count, a base-pair offset, a nm position or
   a dimensionless fraction; the `D_⊥` ledger is asserted dimensionless by construction.
2. **Limiting cases** — a lattice whose per-interface counts are all equal must have smeared and
   series fractions **exactly equal**; a design censused against its own emitting phase must match
   at departure `0.0`.
3. **Symmetry** — reflecting the design about the row centre (`bp → rowBp − bp`) must leave the
   column set invariant, and exchanging every parity must map phase 0 onto phase 16 with identical
   positions (`C-0161`'s own `parityPairs` row is the control).
4. **Convergence** — not applicable to an integer census; the two solves are taken at `C-0161`'s
   own subdivision and their departure from its published value is asserted.
5. **Literature / corpus cross-check** — the reference scadnano implementation's independent count
   (49 staple crossings, 14 scaffold crossings, `C-0160` §1) is the third implementation, and
   `C-0157`'s own generator test is the second.

### 2.5 Reserved artifacts

- Claim [`C-0170`](../claims/), challenges `CH-0219` and `CH-0220` — used only if a standing claim
  is contradicted; released explicitly in the claim if not.
- Study `design.SimulatedTileCensusStudyKt`, model `design/SimulatedTileCensus.kt`,
  result `gpd/results/T-275-simulated-tile-census.json`. Tests first.
