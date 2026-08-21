# Architecture, and the order the rest of the restructure goes in

This file records the **target layering** of this repository, what of it exists, and what each
remaining step costs.
It exists because the restructure is deliberately staged:
the steps that move no committed number were taken first,
and the ones that re-emit the corpus were not taken at all yet.

It is a design record, not a claim.
Nothing here is evidence about the device.

## Why the layering changed

Two things came out of reading this codebase against the 2024–2026 tooling literature
(`TOOLING-NOVELTY.md`, and the sources listed there).

**The ecosystem owns a boundary this repository did not touch.**
caDNAno, scadnano, ENSnano, Adenita, MagicDNA and DNAforge all read and write a design file,
and export to oxDNA/oxView.
This corpus's tile was a set of Kotlin constants in `Gen1Tile`,
so its buildability proofs could not be run against anybody else's design
and its own recommended tile could not be handed to anybody without a human redrawing it.

**The moat is the regime, not the ingredients.**
oxDNA2 carries salt-dependent electrostatics but is parameterised at or above 0.1 M *monovalent*
and excludes Mg²⁺;
mrDNA applies external fields but solves no electrode boundary.
So `brush/` and `electrostatics/` at 0.5–10 mM MgCl₂ are the parts of this tree with no counterpart —
and they were reachable only *through* the tile.

## The target layers

Innermost first. A layer may depend only on the ones above it.

| layer | package | what it holds | state |
|---|---|---|---|
| 1. quantities | `quantities/` | a number **and the state it was read at**; comparisons that refuse two states | **exists** |
| 2. lattice | `lattice/` | crossover lattices behind one interface: azimuths, step, period, register departure, station ladder | **exists** |
| 3. design | `design/` | the interchange boundary — read **and write** a scadnano `.sc`, derive the lattice facts, check buildability | **exists** (`T-266`/`C-0160` added the writer and the committed designs) |
| 4. environment | `brush/`, `electrostatics/`, `material/`, `poroelastic/` | the layer, the electrolyte, the field — validatable without a tile | exists, **not yet behind an interface** |
| 5. mechanics | `structure/`, `crossover/`, `coupling/` | grillage, influence banks, prestrain-as-load | exists, **consumes `Gen1Tile` directly** |
| 6. device | `actuator/`, `stability/`, `anchoring/`, `tile/` | the force balance, the folds, the joints | exists |
| 7. emission | `structure/ResultRounding.kt` ×6 | one rounding rule, typed records, declared inputs | **six implementations; not started** |
| 8. corpus tools | `tools/*.py` | provenance, drift, transfer detection | exists, **belongs outside this repository** |

## What layers 1–3 buy, concretely

**1. `quantities/`** — the corpus's dominant error class, made unrepresentable.
`CLAUDE.md` records eleven instances of *"a quantity is not well posed without the state it is read
at"*, every one caught by a person reading prose.
The state is now a constructor parameter, `quote()` cannot omit it,
and `ratioOf`/`differenceOf` **refuse** two quantities read at different states —
which is exactly the operation that made `C-0012`'s buffer advantage 3.16–3.35× too large.

**2. `lattice/`** — the asymmetry that cost this programme a corpus.
The placement machinery here is lattice-generic and took the honeycomb unmodified;
the *site generators* are hard square-lattice, and nothing in the type system said so.
`CrossoverLattice` holds what is shared and makes each lattice **declare** what is not
(`hasCentroSymmetricPhaseCongruence`).
Two laws only become visible from the interface, and both are now tests rather than prose:
`samePairPeriod = azimuths × step` (4 × 8 = 32, 3 × 7 = 21),
and the step is the step **because** it lands on the neighbouring azimuth (270°, 240°) —
from which the register departure falls out,
`−17.14°` per period on the square sheet and **exactly zero** on the honeycomb.

**3. `design/`** — the door, and it now opens both ways.
`ScadnanoDesign.fromResource` reads the `.sc` file the oxDNA run of `C-0157` actually simulated,
and derives 15 duplexes, 112 bp, phase 8, seven columns, the 4/3 parity split and 49 crossovers
**from the file** — a reproduction of the corpus's own counts across two implementations in two
languages, not a restatement of them.
`checkBuildability()` runs this repository's rules against an imported design,
which is the capability nothing in the field has:
caDNAno will happily let you draw a row width a boustrophedon cannot turn at.

`T-266` added the **writer**,
so `C-0151`'s recommended block is a committed file ([`gpd/designs/`](gpd/designs/README.md))
rather than a pair of integers in a study literal,
and `read → write → read` reproduces every lattice fact of the simulated sheet at integer equality.
Its first use found two things nothing else could.
`checkBuildability()` was applying the **square** sheet's width rule to a honeycomb design —
in the one function whose sibling `lattice()` refuses to guess —
which is filed rather than repaired, with the lattice-aware check added beside it.
And a `require` that had been written **tautologically** refused the writer's own first reading of
*"every x-raster row spans exactly the larger of the two lengths"*,
which is a statement about a row's union **window** and not about its helices:
read the other way it puts 7 nt of phantom scaffold in the block
and would have raised a challenge against a correct standing number.
**A quantity that nothing draws is a quantity nothing checks.**

## The order the rest goes in

**Step 4 — an `environment` interface.**
`pressure(h)`, `force(h, bias)`, `decayLength`, behind which the SCF brush and the 2-D PB edge sit.
Additive; moves no number.
What it buys is that the two packages with no counterpart in the field can be validated,
and cited, without the tile.

**Step 5 — `mechanics` on an imported design.**
`OrigamiGrillage` currently takes its lattice from `Gen1Tile`'s constants;
it should take a `ScadnanoDesign` (or a lattice plus a cross-section).
Additive if the existing constructor stays.
This is what makes every placement, phase and plan result re-runnable on a different design
instead of being a property of one.

**Step 6 — one emission layer. This one moves numbers.**
Six rounding implementations, a departure rule repaired five times per call site,
and a dependency graph that has to be *derived* by `tools/result-reader-census.py`
because studies read result files by path.
Studies should declare typed input handles;
then the census, `tools/reemission-order.py`'s topological sort and staleness detection are free.
Two schema fields pay for themselves outright:

- a **lattice tag** on every result record — the honeycomb correction of iterations 33–34 would
  have been a query rather than an audit;
- a **regime block** (buffer, valency, gap, bandwidth) — so consuming a result outside the range it
  was solved in is a gate rather than a reading.

**Cost**: every affected result file is re-emitted, in the topological order the census gives,
and any claim that *quotes* a moved number is amended.
`C-0101` established the discipline and got the order wrong inside its own sweep,
which left `T-157` stale for six iterations — so the sort is not optional.

**Step 7 — `tools/` leaves.**
`result-transfers.py`, `reemission-order.py`, `trace-answers.py`, `check-*.py`
have nothing to do with DNA.
Their relatives are statcheck and GRIM, not ReproZip.

## What was deliberately not done in this pass

- **No committed result file moved**, and no study was re-run.
  Layers 1–3 are new code with new tests; nothing existing calls them yet.
- **Nothing was deleted or renamed.**
  `tile/HoneycombLattice`, `structure/CrossoverLayout` and `Gen1Tile` are untouched, and the new
  lattice objects *take their constants from* the old ones rather than restating them, so the two
  cannot drift while both exist.
- **No claim was amended**, because no claim's number changed.

## The rule the layering exists to enforce

A number carries the state it was read at;
a lattice declares what it does not support;
a design is a file, not a constant.
Everything else here is scheduling.
