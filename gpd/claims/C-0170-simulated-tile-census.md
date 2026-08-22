# C-0170 — **THE TILE oxDNA SIMULATED AND THE TILE EVERY PLACEMENT RESULT IS GRADED ON SHARE NOT ONE STAPLE CROSSOVER COLUMN.** The design file's phase datum is the **row start** and the mechanics' is the **tile centre**, and on a 112 bp row those differ by exactly **8 bp** — because `112 = 7 × 16` and seven is **odd** — so the same integer `8` names two lattices: the simulated tile is tile-centre phase **16**, 7 columns, **49** staple crossovers, **63** inter-duplex ties, and the graded tile is phase **8**, 8 columns, **42** staple crossovers plus **14** raster turns, **56** ties. **`C-0157`'s `k_θ` does not move** — `k_BT/Var(roll)` is a per-site equipartition and reads no count — but the object it was measured on is the **stiffer** of the two by `1.125×` in ties, so the bracket's upper end is its biased-high end, and its **downstream** `D_⊥ = k_θ d/p` is a linear density worth exactly **49** crossovers on this row, i.e. the SIMULATED count and `1.14285714×` less than the graded lattice carries. **The row-end admission binary is not a column count at all** — at phase 8 the two end columns *are* the scaffold's raster turns, so `C-0099`'s 56 is `42 + 14` and its 42 is the same object with the turns unmodelled, which the census re-derives from the lattice alone. And **the seven-column reading is the GENERIC one**: **30 of 32** phases give 7 and 49, and only 8 and 24 do not

| | |
|---|---|
| **Task** | [`T-275`](../tasks/T-275-simulated-tile-census.md) — which crossover census a measured constant is read against |
| **Leaf** | `A1.2`, with `A8.2` |
| **Verification type** | **logical** (an integer census over an emitted design against `anchoring.rasterColumnLayout`, the lattice every placement study in this corpus calls) **+ in-silico** (three graded solves at `T-267`'s own load case, which price the difference the census finds) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING IS FOLDED AND NOTHING IS MEASURED HERE.** What is measured is which **object** a measurement made elsewhere was made on. No oxDNA is re-run, no trajectory is re-analysed, and `C-0157`'s bracket is consumed unchanged |
| **Verdict** | **PASS on all six predicates, and the answer to `D3` is NO.** `P1`–`P6` hold; **none of `F1`–`F6` fired**. `F2` — *the design matches tile-centre phase 8*, which would have made `T-275`'s premise wrong — did not fire: the design matches phase **16**, at both row-end settings and at a worst column departure of `1.8e−15 nm`. **`CH-0219` and `CH-0220` are RAISED**; both are about a **ground** and neither disputes a number |
| **Provenance** | [`gpd/results/T-275-simulated-tile-census.json`](../results/T-275-simulated-tile-census.json), produced by `design.SimulatedTileCensusStudyKt`; model in `src/main/kotlin/design/SimulatedTileCensus.kt` (**new**). `ScadnanoDesign.kt`, `DesignedGrillage.kt`, `CrossoverLayout.kt`, `BuildableRasterWidth.kt`, `CrossoverPhaseSelection.kt`, `OrigamiSheet.kt` and `tools/oxdna/gen1_tile_design.py` were **read, not edited** — no existing source is touched. **28 gate-named tests**, written first and watched fail, in `src/test/kotlin/design/SimulatedTileCensusTest.kt`; **mutation-tested**, six mutations of the new source failing **3, 5, 1, 3, 3 and 2** named tests with the restored source passing 28 of 28. Two independent runs of the study are **byte-identical**. The three `gpd/results/` hygiene gates read clean (raw conversions **0 of 152**, prose precision **0 tokens in 0 files of 152**, the `T-225` departure census **0 unclassified**) |
| **Conditions** | Lattice and structure only. Rise `0.34 nm/bp`; single-layer **square-lattice** sheet, **15 duplexes** at the SAXS `d = 2.69 nm`, `p = 32 bp` per interface, column pitch 16 bp, junction planes 8 bp; `C-0086`'s buildable seamless row **112 bp = 38.08 nm**. The three solves are at `T-267`'s own state and no other: `C-0001`'s foundation secant `0.012625625 pN/nm³`, `T-10`'s interior pressure `0.0619578686 pN/nm²`, a 50 % taper over one 4 nm Debye length, two subdivisions — held identical across the three so the comparison is of **lattices** and not of device states |
| **Consumes** | [`C-0157`](C-0157-crossover-hinge-constant-from-oxdna.md) (the simulated design and its `k_θ` bracket), [`C-0169`](C-0169-crossover-vertical-compliance.md) (§8, which raised this and states the 56/42/49 three ways), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (`grillageImport`, and its committed peak dishing, reproduced at `3e−10`), [`C-0160`](C-0160-scadnano-writer.md) (the reader, the role filter and the committed `.sc`), [`C-0090`](C-0090-buildable-raster-width.md) (`rasterColumnLayout`, the 38.08 nm width, *"a column lands on the row end at `b ≡ 8 (mod 16)`"* and *"in a seamless boustrophedon that column IS the scaffold crossover"*), [`C-0099`](C-0099-row-end-crossover-stiffness.md) / [`C-0095`](C-0095-row-end-crossover.md) (the 14/42 scaffold/staple split, **re-derived here from the lattice alone**), [`C-0063`](C-0063-upward-root-placement.md) (the centro-symmetric pair 8 and 24), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 32-phase period and the *"49–56"* inventory), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage) |
| **Constrains** | **Nothing is overturned and no committed result file moves.** `C-0157`'s bracket, `C-0169`'s thresholds, `C-0099`'s decomposition, `C-0090`'s recommended placement and every placement result downstream of `D_⊥` keep their values. What changes is that the *object* each is read on is now named, in a **datum-carrying** phase, and that `C-0169` §9 item 4 is **CLOSED** |

---

## 1. The cheap bound, which ran first and cost one division

`C-0086`'s buildable seamless row is `112 bp = 7 × 16 bp`. **Seven is odd**, so the tile centre sits
half a column pitch — 8 bp — away from every lattice point of a row-start-phased column lattice:

```
centre-datum phase  ≡  row-start-datum phase + (rowBp/2) mod 16   (mod 16)
                    ≡  row-start-datum phase + 8
```

That predicted, before any file was opened, that the generator's *"phase 8"* is the corpus's phase
**0 or 16**; that at the corpus's phase 8 a column lands **exactly** on each row end, which is why
`CrossoverLayout.EDGE_MARGIN` and not the physics decides 8 columns against 6 there; and that at
0 or 16 no column touches the edge, the guard is inert, and the count is **7**.

**The cheap bound corrected its own predicate.** `P3` was first written as *"exactly one
`(phase, admitRowEnd)` pair"*; the arithmetic returns **two** pairs at one phase, because where no
column touches the edge the row-end setting cannot change anything — the guard's inertness, stated
as an observable, and the control for `P4` where the same setting is worth two columns.

**What the arithmetic cannot decide is the parity.** The pitch is 16 bp and the *period* is 32, so
it reaches a **pair** — 0 and 16 — whose column positions are identical and whose parities are
exchanged. That is `C-0090`'s pair and `C-0161`'s `parityPairs` row. Which member is drawn is a
fact about the **file**, and the file says **16**.

The census then costs milliseconds and buys the difference between a docstring and an artifact —
which matters precisely because the docstring and the code disagree about which claim they
implement (`CH-0220`).

## 2. Deliverable 1 — which census `k_θ` is read against

**The seven-column, 49-staple-crossover lattice this corpus calls tile-centre phase 16.**

| | the design file | the graded lattice |
|---|---|---|
| tile-centre phase | **16** | **8** |
| row-start phase | 8 | 0 |
| columns | 7 | 8 admitted, 6 refused |
| staple columns, bp | `8, 24, 40, 56, 72, 88, 104` | `16, 32, 48, 64, 80, 96` |
| row-end columns, bp | none | `0.147, 111.853`, inset by `EDGE_MARGIN` |
| **staple crossovers** | **49** | **42** |
| raster turns | **14**, at offsets 0 and 111 | **14**, and they *are* the two end columns |
| all strand crossings | **63** | — |
| inter-duplex ties | **63** | **56** |
| per-interface split | 4/3 | 4/4 |

Matched against all 32 phases at both row-end settings, the design's columns reproduce
`rasterColumnLayout` at phase **16** on positions **and** parities, at a worst departure of
`1.8e−15 nm`, and at phase **0** on positions with every parity **inverted**. At no other phase do
the positions match at all.

**A bare *"crossovers"* count on this design is ambiguous by `0.285714286`** — `63/49 − 1` — which
is why the census is taken with the strand-role filter `C-0160`'s reader supplies.

## 3. Deliverable 2 — what it does to the numbers

### `k_θ` itself does not move, and the census names the direction of what is left

`tools/T-9-emit-result.py` computes `k = k_BT/Var(roll)` at a crossover site. That is a **per-site
equipartition** and it reads no count: `F5` is declared on exactly that and does not fire. So
`C-0157`'s `5.62052112 – 25.9227606 pN·nm/rad` is untouched and the fitted `13.5294118` is still
inside it.

What the census moves is the **surroundings** the variance was measured in, and the direction is
the *opposite* of the intuition:

- the simulated tile carries **63** inter-duplex ties against the graded tile's **56**, `1.125×` —
  **more** restraint, not less, because at phase 16 all seven columns are interior where at phase 8
  two of the eight are spent on the row ends the scaffold already ties;
- so a roll measured on it is held by more neighbouring material, and **the upper end of the
  bracket is its biased-high end**;
- and that direction is **already inside** the bracket, whose two readings are exactly *"the hinge
  is the only constraint"* against *"the hinge is what a crossover adds over the mid-span roll"*.

### The continuum runs the other way, and it lands on the simulated count exactly

`D_⊥ = k_θ · d / p` is built on `OrigamiSheet.crossoverLinearDensity = layers/p`, a density **per
unit length along the helices**. Over fourteen 112 bp interfaces that is

```
14 × 38.08 nm / 10.88 nm = 49.0 crossovers
```

— **the simulated tile's count, to the last digit**, and `1.14285714×` less than the graded
lattice's 56. So the smeared rigidity every placement result is written on assumes the density of
the tile oxDNA simulated, while the discrete grillage it is graded on carries `8/7` of it.

### `C-0169`'s thresholds are internally consistent and are read at phase 8

`C-0169` swept the vertical link at all **56** crossovers of phase 8 under `C-0090`'s recommended
placement, which is a phase-8 object throughout; nothing in it is read against the simulated tile,
and none of its numbers moves. What this census supplies is the sentence its §9 item 4 asked for:
**a measured `k_z` or `k_s` from `C-0157`'s protocol would be a phase-16 measurement read against a
phase-8 threshold**, and the run priced in its §1 should be re-specified before it is bought.

### Priced in the corpus's own currency

Same footprint, same foundation, same taper, same subdivision:

| lattice | peak dishing, nm | of the stroke | against the graded tile |
|---|---|---|---|
| the simulated tile, imported from its own file (phase 16) | **1.273683** | 0.259548049 | **0.023** |
| the graded tile, phase 8, row end admitted | **1.24539212** | 0.253783003 | — |
| the same, row end refused | **1.36325824** | 0.277801475 | **0.095** |

**2.3 %.** The mismatch is real, and it is affordable — which is the honest reading, and it is
worth stating in that order: the objects differ, the difference is small, and *"they are the same
object"* was never true.

## 4. Deliverable 3 — is the simulated tile the tile the corpus grades on?

**No.** The two share **0** crossover columns of 8 and 7, and **not one staple crossover column**:
the graded lattice's columns sit at `0.147, 16, 32, 48, 64, 80, 96, 111.853 bp` and the design's at
`8, 24, 40, 56, 72, 88, 104`. They **interleave at 8 bp** and are disjoint — `CLAUDE.md`'s own
*"a shift by one column pitch … hands every interface the OTHER parity's columns — a physically
different sheet"*, at **half** that shift.

What they *do* share is their **turns**: the graded lattice's two end columns are the row ends, and
that is exactly where the file draws its 14 scaffold crossings.

## 5. The finding the census was not sent for: the row-end binary is not a column count

At tile-centre phase 8 the two end columns land on the row ends, and `C-0090` already says what
sits there — *"in a seamless boustrophedon that column **is** the scaffold crossover"*. Derived
from the lattice alone, with no file read:

| | phase 8, admitted | phase 8, refused | phase 16 |
|---|---|---|---|
| lattice ties the grillage builds | **56** | **42** | **49** |
| of which modelled raster turns | **14** | **0** | **0** |
| staple crossovers | **42** | **42** | **49** |
| the object's own ties (`staples + D − 1`) | **56** | **56** | **63** |

So `C-0099`'s two readings are **one object**, and the binary between them is *"does the grillage
model the raster turns"* rather than *"how many crossover columns are there"*. The staple count is
**42 in both**. This re-derives `C-0095`'s 14/42 split from the column lattice — two independent
routes to one partition, one from strand roles in a file and one from `x = ±edgeX/2` — and it is
recorded as a **reproduction**, not a challenge: `C-0099` already treats those fourteen as raster
turns and already prices their `k_θ` at 2.85 % of `CH-0111`'s interval.

## 6. And the seven-column reading is the generic one

Over all 32 tile-centre phases at 38.08 nm:

| phases | columns | staple crossovers | ties |
|---|---|---|---|
| **30 of 32** | 7 | **49** | 63 |
| **8 and 24** | 8 admitted / 6 refused | **42** | 56 |

So the simulated tile is the **ordinary** member of the family and the graded tile is the
**outlier** — the one whose count is decided by `CrossoverLayout.EDGE_MARGIN`, and the one
`C-0063` and `C-0090` select for reasons that are about **centro-symmetry** and about the
**row-end scaffold crossover**, not about the crossover count. Neither selection argument is
weakened by this; what changes is that *"seven columns and 49"* is not evidence that a design was
drawn at any particular phase, and `C-0015`'s *"49–56"* band admits **32 of 32** phases of this
tile (`CH-0220`).

## 7. Gates

| gate | how it was taken | result |
|---|---|---|
| **1 — dimensional** | every quantity is an integer count, a base-pair offset, a position in nm or a dimensionless fraction; the three solves carry `T-267`'s own units | clean |
| **2 — limiting cases** | a lattice with equal per-interface counts must have smeared and series fractions **exactly equal** (asserted `== 1.0` for both at a uniform 4); an interface with zero crossovers must annihilate the **series** fraction and not the smeared one | both hold |
| **3 — symmetry** | the design's column set is invariant under reflection about the row centre (`bp → 112 − bp`); phase 0 and phase 16 have identical positions with every parity exchanged, which is `C-0161`'s own `parityPairs` control | both hold |
| **4 — convergence** | not applicable to an integer census. The three solves are at `C-0161`'s own subdivision and the imported one reproduces its committed peak dishing at **`3e−10`**, which is that file's nine-digit emission precision | clean |
| **5 — literature / corpus cross-check** | the reference scadnano implementation's independent count (49 staple crossings, 14 scaffold crossings, `C-0160` §1) is a third implementation; `C-0169`'s 56 and 42, `C-0157`'s 49 and 14, and `C-0090`'s `38.08 = 7 × 5.44` are reproduced at departure **`0.0`** | 6 of 6 |

## 8. Validity range

- **THIS IS A CENSUS OF ONE ROW LENGTH, and it generalises only as arithmetic.** The 8 bp datum
  offset is `(rowBp/2) mod 16`; on a row spanning an **even** number of column pitches it is
  **zero**, the two phase data coincide, and this whole census would have been silent. It is not a
  general theorem that a design file and this corpus disagree — it is a theorem about odd pitch
  counts, and `C-0086`'s buildable width has one.
- **THE LATTICE IS SINGLE-LAYER SQUARE.** Every count here is a property of `CrossoverLayout`'s
  two-parity alternation and of a path-graph interface set; none of it transfers to a honeycomb
  block (`C-0154`), whose face carries its own 21 bp ladder and whose interfaces are not a path.
- **NOTHING HERE MEASURES `k_θ`, `k_z` OR `k_s`.** The direction reported in §3 is a statement about
  which of two objects carries more restraint, not a correction to a bracket. Correcting the
  bracket needs the re-run `C-0169` §1 prices, on a **named** lattice.
- **THE THREE SOLVES ARE ONE OPERATING STATE**, `T-267`'s. A flatness verdict needs an operating
  state as well as a load case, and 2.3 % is read at that one. The census's integers do not depend
  on it; the 2.3 % does.
- **THE ROLE FILTER IS `C-0160`'s AND INHERITS `CH-0209`.** A crossover is counted as one strand
  crossing here, as the file draws it; the reference implementation draws a Rothemund crossover as
  **two** crossings at adjacent offsets, and read that way every count in this claim would double
  on a design drawn its way. This design is not drawn its way, and the census is of this design.
- **THE `physicalTies` COLUMN ASSUMES A SEAMLESS RASTER** — that the object carries `D − 1 = 14`
  raster turns whether or not a lattice models them. That is `C-0086`'s routing and `C-0095`'s
  settled fact for this tile; on a seamed sheet the count is different and `C-0161` already records
  that a seam's parity sequence is one no phase sweep here can generate.

## 9. Still open

1. **Which lattice a re-run should simulate.** The census says the two objects differ by seven
   inter-duplex ties out of 56 and share no staple column; `C-0169` §1 prices the re-run at about
   one day and 649 MB. That decision should be recorded before it is bought.
2. **Whether a phase integer should ever be quoted without its datum.** `CH-0219` argues it should
   not, and `C-0161`'s `parityPairs` row shows that even the datum is insufficient — 0 and 16 have
   identical positions and exchanged parities, so only the **file** settles it.
3. **Whether `k_θ` is the right constant for the fourteen row-end sites**, which are scaffold
   crossings and are given the staple hinge constant. `C-0099` shows the answer does not move the
   flatness verdict (2.85 % of `CH-0111`'s interval), which is why this is listed and not
   challenged — but nothing has asked the question directly.
4. **Which phase the programme should recommend**, now that 30 of 32 give the same census.
   `C-0090` selects 8 and 24 for the row-end scaffold crossover and `C-0063` for centro-symmetry;
   neither argument is about the crossover count, and both stand.
