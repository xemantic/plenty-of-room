# T-216 — What is the crossover PHASE LATTICE of a mixed-domain row?

**Leaf:** `A8.2` (the plan and lattice model the anchoring array is written on).
**Claim reserved:** `C-0136`. **Challenges reserved:** `CH-0164`, `CH-0165` (shared with `T-217`).
**Raised by:** [`C-0133`](../claims/C-0133-twist-corrected-raster-row.md) *Still open* item 1 —
*"the largest single gap this claim leaves"*.

---

## Formulate

### The question, exactly

`C-0015` establishes that a Rothemund sheet's crossover **column phase** has period 32 bp, is
quantised to base pairs, and that *"a 40 nm tile fits 8 columns at 10 of the 32 phases and 7 at the
other 22"*.
`C-0090` then collapses those ten eight-column phases to **two** — 8 and 24 — and the collapse rests on
`38.08 = 7 × 5.44` **exactly**, an identity of a **uniform** 16 bp pitch:
the row-end scaffold crossover, the one that turns the raster, is a lattice point only at
`phase ≡ −L/2 (mod 16 bp)`.

`C-0133`'s twist-corrected row is **110 bp** with domains `16+15+16+16+16+15+16`.
Its column pitch is **not uniform**, so the identity `38.08 = 7 × 5.44` has no counterpart,
and *"the phase"* may not be a single integer at all.

**So: does a mixed-domain seamless raster row have a phase variable, and if it does, what is its
census?**

### The tension the task exists to resolve

Every phase, placement, station-count and centro-symmetry result in this corpus
(`C-0015`, `C-0055`, `C-0063`, `C-0090`, `C-0104`, `C-0107`, `C-0133`)
is written on the **uniform** 16 bp column lattice.
`C-0133` solves at the one phase `C-0090` recommends and does not ask what the phase lattice of a
mixed-domain row even is.
If the answer is *"a non-uniform pitch has no phase variable"*, that is a legitimate and valuable
answer and the corpus's phase sweeps do not transfer to the corrected row at all.
If it is *"here is the census"*, then `C-0133`'s recommended design is one member of a family
nobody has enumerated.

### Locked conventions

- Lengths **nm**, angles **degrees** where quoted and **radians** in code,
  `k_BT = 4.141947 pN·nm` at 300 K, aqueous **2 mM MgCl₂**; rise **0.34 nm/bp**.
- **Natural twist** `ω_n = 360/10.5 = 34.2857 °/bp` — B-DNA's, `C-0015`'s and `C-0107`'s value.
- A **domain** is one inter-column stretch of a row, nominally 1.5 turns (3 half turns).
- A **row** is one duplex of `N` base pairs, both of whose ends carry a scaffold crossover
  (`C-0095`); **the row ends are the tile edges** along the helix axis.
- A **column** is a crossover plane spanning the tile across the helices —
  the even-index members of `C-0055`'s 8 bp plane lattice on a uniform row.
- A **phase** is `C-0015`'s: a rigid **translation** of the column lattice relative to the tile,
  quantised at the rise, of period 32 bp.
- A **parity** is `C-0015`'s column/interface assignment — a shift by one column pitch leaves every
  column position unchanged and hands every interface the other parity's columns.
- **Centro-symmetric** means invariant under `x → −x` about the row centre.
- **Dishing** is `C-0063`'s peak-dishing-over-free-stroke; **flat** means `≤ 0.10` (`T-5b`).
- The **design family** of a twist-corrected row is Rothemund's own remedy — domain lengths adjusted
  *"by single bases"*, i.e. domains drawn from `{15, 16}` at a nominal 16.
  The wider shell `{14…17}` is reported as a sensitivity, not adopted.

### Acceptance predicates

- **P1** — the **existence** question is answered before any solve, by exact integer arithmetic:
  either a translational phase variable exists for a mixed-domain seamless row, with its period,
  or it does not, with the reason.
- **P2** — whichever way `P1` falls, the corpus's own uniform case is **reproduced** on the same
  machinery: `C-0090`'s phases 8 and 24 at 38.08 nm, their column positions and their parities.
- **P3** — the census is delivered: how many distinct column lattices the 110 bp row admits,
  how many carry eight columns, and which are centro-symmetric — with the station lattice
  (count, centro-symmetry, worst azimuth departure) beside each.
- **P4** — the census is given a **consequence**: `C-0133` selects its arrangement on the peak
  register angle; the same three centro-symmetric arrangements are re-read on the **flatness**,
  which is the objective the design is built to, and the ranking is reported.

### Verification type

**Logical** (exact integer arithmetic over the arrangement family and the plane lattice, asserted
rather than sampled) **+ in-silico** (`C-0009`'s grillage under `C-0022`'s carried collar on the
lattices the census produces) **+ reproduction** (`C-0090`'s and `C-0133`'s published numbers).

---

## Plan

### Method, and the cheap bound that runs first

1. **The existence question is integer arithmetic and costs nothing.**
   A seamless raster row's two ends *are* the tile edges and both carry a scaffold crossover, so both
   end columns are pinned. A rigid translation by any non-zero amount moves an end column off the
   tile edge, which is not the same row. So the admissible translation group is asked directly, over
   every base-pair translation of the period, at 112 bp **and** at 110 bp, and the answer is counted
   rather than argued.
2. **The uniform reproduction is the gate.** Phases 8 and 24 are re-derived from
   `rasterJunctionPlanes` and their column positions compared. If they are the same positions with
   opposite parities, then `C-0090`'s *"two phases"* is one lattice and a **parity binary**, and the
   same binary is what survives on a mixed-domain row.
3. **The census is an enumeration of the arrangement family**, `C(7,2) = 21` compositions of 110 into
   seven parts from `{15, 16}`, with columns, parity, centro-symmetry, station lattice and plan arm
   ceiling read off each. Closed form; no solve.
4. **Only then** the flatness: three influence banks (one per centro-symmetric arrangement) and an
   exhaustive centro-symmetric placement enumeration on each under the corrected graded register
   field, plus the frozen-shape read for comparability with `C-0133`.

The order is deliberate: steps 1–3 can close the task on their own, and step 4 is the only part that
costs a solve.

### Justification against cost

The lattice constructors (`twistCorrectedColumnLayout`, `twistCorrectedUpwardSites`,
`centroSymmetricPlacementsOn`) already exist and are gated against the uniform case by `C-0133`, so
steps 1–3 are arithmetic on existing, tested code.
Step 4 costs three influence banks and three enumerations — the same order as `C-0133`'s four — and
it is the only way to answer whether the arrangement chosen on the register peak is the arrangement
the flatness wants. `CLAUDE.md`: *"rank on the objective the design will be built to, not on the one
that is cheap to evaluate."*

### What would falsify this approach

- **F1** — some non-zero rigid translation of the 110 bp column pattern leaves both end columns on the
  tile edges. *(Would mean a translational phase variable does exist and the census is a phase census
  after all.)*
- **F2** — `C-0090`'s phases 8 and 24 do **not** give identical column positions at 38.08 nm.
  *(Would mean the two admissible phases are two lattices, not one lattice and a parity, and the
  reframing is wrong.)*
- **F3** — some arrangement of the 110 bp row carries other than eight columns.
  *(Would mean the column count is not the identity `D + 1`.)*
- **F4** — the number of centro-symmetric arrangements is not three, or the number of arrangements up
  to reflection is not twelve.
- **F5** — the flatness ranking of the three centro-symmetric arrangements agrees with their register
  peak ranking. *(Not a defect — but it would mean the census has no design consequence, and the
  claim must say so.)*
- **F6** — the standing falsifier: a free tile under a **uniform** load on a uniform Winkler
  foundation dishes something.
