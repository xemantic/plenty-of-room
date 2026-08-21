# C-0160 — **THIS PROGRAMME'S RECOMMENDED TILE IS NOW A FILE, AND DRAWING IT IS A CHECK ON EVERY GEOMETRIC QUANTITY AT ONCE.** A scadnano **writer** closes `ARCHITECTURE.md`'s layer 3: `read → write → read` reproduces every lattice fact of the `.sc` `C-0157` simulated — **15 duplexes, 112 bp, phase 8, seven columns, 49 crossovers, 14 raster turns, the 4/3 split** — at integer equality with **no tolerance**, and the second write is byte-identical, so the writer is the reader's **inverse** rather than a plausible second implementation. `C-0151`'s recommended block is emitted as a committed artifact: **60 helices, ten corrugated x-raster rows of six, 102 / 109 bp, closing on caDNAno's `±5 bp` rule at ZERO forced crossovers**, and its 55 stations, phase 16, 14 bp offset, `b₀ = 5`, 7 bp stagger, 102 bp interface window and `116 bp = 39.44 nm` extent are re-derived on the emitted design. The **reference** scadnano implementation (0.21.1) loads both with **zero warnings** and independently counts **49 staple crossings and 14 scaffold crossings** on the sheet — a third implementation agreeing with this corpus's own census. **The declared falsifier `F2` FIRED and is the finding: `checkBuildability()` is LATTICE-BLIND**, applying the square sheet's odd-multiple-of-16-bp width rule to a honeycomb design, in the one function whose sibling `lattice()` *refuses* to guess. **And a non-tautological `require` refused this claim's own first construction** — reading *"every x-raster row spans exactly the larger of the two lengths"* as a statement about a row's **helices** rather than about its union **window** puts 7 nt of phantom scaffold in the block and would have raised a challenge against a correct standing number. **A row of a two-length honeycomb raster is NOT uniform** — 102, 109, 109, 109, 109, 102 on one parity — and `C-0140`'s *"the two path ends are charged one of each length"* is **exact**, drawn: 6 330 nt, 919 spare on M13

| | |
|---|---|
| **Task** | [`T-266`](../tasks/T-266-scadnano-writer.md) — a scadnano writer, so the recommended tile is a file somebody can open |
| **Leaf** | none — completes layer 3 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Verification type** | **logical** (exact integer round trip; every lattice derivation is integer arithmetic) **+ in-silico** (the emitted files are loaded and re-derived by the **reference** scadnano implementation, an independent parser in another language) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** `PASS` here means the emitted file is a scadnano file that this repository's own rules accept and that the reference implementation reads without complaint. **No object is folded and none is measured.** The honeycomb artifact is explicitly *not foldable* — see §6 |
| **Verdict** | **PASS on `P1`, `P2`, `P3`, `P5`; `P4` PASSES on the square sheet and FAILS on the honeycomb block, which is the task's own most-valuable outcome and is filed rather than repaired quietly.** `F1` (the round trip is not exact) did **not** fire. `F3` (the reference implementation refuses a file) did **not** fire. `F4` (scadnano's honeycomb grid disagrees with this corpus's cross-section) did **not** fire, at departure `0.0`. `F5` (the emitted block's interior spans disagree with `HoneycombRasterResidues`) did **not** fire. `F6` (the emitted scaffold's base count disagrees with `HoneycombRasterProfile.scaffoldNucleotides`) **fired against an intermediate construction and does not fire against the emitted design** — see §5, and the builder's own `require` is what refused the construction. **`F2` FIRED**: `checkBuildability()` reports a violation on the recommended block, because its width rule is a square-lattice statement (§4). **`CH-0208` was reserved for this claim and is RELEASED UNUSED** — no standing claim is contradicted |
| **Provenance** | Artifacts [`gpd/designs/gen1-block-honeycomb-10x6-102-109.sc`](../designs/gen1-block-honeycomb-10x6-102-109.sc) and [`gpd/designs/gen1-sheet-square-15x112.sc`](../designs/gen1-sheet-square-15x112.sc), with [`gpd/designs/README.md`](../designs/README.md); emitter [`design/DesignEmission.kt`](../../src/main/kotlin/design/DesignEmission.kt) (**new**), writer [`design/ScadnanoWriter.kt`](../../src/main/kotlin/design/ScadnanoWriter.kt) (**new**), block builder and lattice-aware check [`design/HoneycombBlockDesign.kt`](../../src/main/kotlin/design/HoneycombBlockDesign.kt) (**new**). [`design/ScadnanoDesign.kt`](../../src/main/kotlin/design/ScadnanoDesign.kt) is edited **additively only** — three defaulted constructor parameters (`helices`, `geometry`, `version`), two record types widened from `private` to public and one added (`ScadnanoGeometry`), and one helper widened from `private` to `internal`; **no existing behaviour changes and its twelve standing tests pass unmodified**. **29 new gate-named tests**, written first and watched fail, in [`design/ScadnanoWriterTest.kt`](../../src/test/kotlin/design/ScadnanoWriterTest.kt) (11), [`design/HoneycombBlockDesignTest.kt`](../../src/test/kotlin/design/HoneycombBlockDesignTest.kt) (12) and [`design/CommittedDesignsTest.kt`](../../src/test/kotlin/design/CommittedDesignsTest.kt) (6), **41 in the package**. **Mutation-tested**: six mutations of the new sources fail **2, 5, 2, 2, 1 and 1** named tests (§7), and the restored source passes **41 of 41**. Reference-implementation validation by the retained driver [`tools/scadnano/validate-sc.py`](../../tools/scadnano/validate-sc.py) (8 self-tests, environment notes in its header). **No committed result file moves, no study is re-run, and no claim's number changes** |
| **Conditions** | Lattice statements only — no temperature, buffer or load enters this claim. Rise `0.34 nm/bp`; single-layer square-lattice `d = 2.69 nm` (SAXS), honeycomb `d = 2.536 nm` (SAXS), in-plane row pitch `3d/2 = 3.804 nm`. Square lattice at caDNAno's `32/3 = 10.67 bp/turn`, honeycomb at B-DNA's `10.5`. scadnano format version **0.21.1**, reference implementation **0.21.1** |
| **Consumes** | [`C-0151`](C-0151-closing-raster-selection.md) (the recommended pair, cross-section, stagger, interface window, station census, phase and `b₀`), [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) (the `±5 bp` closure predicate and the 14 bp offset), [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) (the x-raster path, turn senses, level walk and the two-end scaffold charge — **upheld exactly**), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the `10 × 6` cross-section), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the buildable seamless width and the crossover census), [`C-0157`](C-0157-crossover-hinge-constant-from-oxdna.md) (the `.sc` fixture and its single-coverage finding), [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md) (the turn-loop allowance, cited as the reason a loopout is **not** emitted), [`C-0126`](C-0126-four-layer-supersession.md) (which body is the device tile) |
| **Constrains** | **Raises no challenge.** Supplies the design object [`T-267`](../tasks/T-267-mechanics-on-imported-design.md) needs, and closes [`ARCHITECTURE.md`](../../ARCHITECTURE.md)'s layer-3 row |

---

## 1. The round trip, and why it is the gate

A writer cannot be checked by reading its output, because the thing that would read it is the same
codebase. The gate with content is the **fixed point**:

| what | assertion | result |
|---|---|---|
| grid, helix count, strand count, staple count | integer equality | identical |
| every domain of every strand (`helix`, `forward`, `start`, `end`) | list equality, **no tolerance** | identical |
| crossover count, columns, phase, per-interface split, every `DesignCrossover` | list equality | identical |
| scaffold turns | integer equality | 14, identical |
| edge along the helices, accumulated register departure | `isCloseTo` | 38.08 nm, −60.0° |
| the second write against the first | **string equality** | byte-identical |

Base-pair counts and helix indices are integers, so *"no tolerance is admissible"* is a predicate and
not a preference — `T-266`'s own units section says so, and every row above is `==`.

The facts that come back are the corpus's own: **15 duplexes, 112 bp = 38.08 nm, phase 8, seven
columns at `8 + 16k`, 49 crossovers, 14 raster turns, seven interfaces carrying four and seven
carrying three**. `C-0086` derived them from a lattice argument, `tools/oxdna/gen1_tile_design.py`
from a Python construction, `ScadnanoDesign` from the file — and they now survive an emission and a
re-read.

## 2. Somebody else's parser, which is the only honest test of a compatibility claim

`P1` is a claim about **scadnano's** format, and no test written here can settle it: a shared
misreading would pass a round trip silently. The reference implementation can settle it, and it is
a `pip install`:

```
python3 tools/scadnano/validate-sc.py gpd/designs/*.sc
# 2 of 2 file(s) load in the reference implementation without warnings
```

It runs with `warnings.simplefilter('always')`, so an advisory counts as a defect — *"loads without
warnings"* is the predicate `T-266` was written to. What it derives, independently:

| file | helices | strands | scaffold domains | scaffold crossings | staple crossings |
|---|---|---|---|---|---|
| `gen1-sheet-square-15x112.sc` | 15 | 65 | 15 | **14** | **49** |
| `gen1-block-honeycomb-10x6-102-109.sc` | 60 | 1 | 60 | **59** | 0 |

**49 and 14, from a third implementation in another language.** `CLAUDE.md`'s warning that *"a bare
'crossovers' count is 28.6 % ambiguous"* is met here from the outside: the validator filters by
strand role before counting, and the two numbers it reports are the two this corpus distinguishes.

## 3. The recommended block, drawn

`C-0151` selects `102 / 109 bp` on the `10 × 6` cross-section. Until this claim that recommendation
was **a pair of integers in a study literal** — `tile/ForcedCrossoverPriceStudy.kt` and
`tile/HoneycombBondClassStudy.kt` each carry it in a candidate list, and no object in the tree was
that design. It is now a file, and every quantity below is re-derived on it:

| | value | owner |
|---|---|---|
| helices | **60**, ten corrugated x-raster rows of six | `C-0141` |
| row lengths | **102 / 109 bp**, stagger **7 bp** | `C-0151` |
| closure | **closes**, `b₀ = 5`, **0** forced crossovers | `C-0148` |
| raster turns | **59**, every one a direct scaffold crossover | `C-0140` |
| axial extent (bounding box) | **116 bp = 39.44 nm** | `C-0151` |
| row span | **109 bp = 37.06 nm** | `C-0151` |
| interface window | **102 bp** | `C-0151` |
| stations on the face | **55** of 60, ladder phase **16**, inter-row offset **14 bp** | `C-0148`, `C-0151` |
| across the helices | `10 × 3d/2` = **38.04 nm** | `C-0141` |
| scaffold | **6 330 nt**, 919 spare on one M13 | `C-0140` |

**Nothing here is a second implementation of the lattice.** The path, the turn senses and the level
walk are read off `HoneycombRasterResidues` — the object `C-0148` and `C-0151` graded on — so the
emitted design and the graded design are the same object, and the tests assert equality rather than
agreement. What the writer adds is the two things a lattice object does not carry: a grid
**position** for every helix, and a start and an end offset for **every** helix, including the two
path ends the corpus's own windows do not cover.

### The cross-section is scadnano's own, checked rather than assumed

scadnano's honeycomb grid takes `(h, v)`, and `grid_position_to_position` is a published formula.
Evaluated at the `(column, raster row)` this writer emits, it lands on this corpus's own honeycomb
cell — `(x·d√3/2, y·d/2)` — at **departure `0.0`**, up to the sign of the `y` axis, which is a
global reflection of the picture and preserves every distance and every adjacency. That is `F4`, and
it did not fire. Two lattices from two projects agreeing exactly is what makes the emitted block a
**honeycomb** block rather than a picture of one.

## 4. `F2` fired: the buildability check is lattice-blind

`ScadnanoDesign.lattice()` **refuses** a grid it has no lattice for, and says why: *"guessing between
them silently transfers a phase congruence, a station ladder and a register departure that do not
hold"*. Twelve lines below, `checkBuildability()` applies `seamlessRowWidthIsAdmissible` — which is
`C-0086`'s rule, an **odd** number of half turns across the row, i.e. the odd multiples of 16 bp on
the **square** sheet — to whatever design it is handed. On the recommended honeycomb block it duly
reports one violation, and the sentence it prints names a 16 bp ladder that has nothing to do with
this design's 21 bp period.

**This is exactly the transfer `C-0141` had to undo**, surviving in the one function whose sibling
was written to prevent it. It was invisible because the repository had no honeycomb design to run it
on — which is the argument for an interchange writer, made by the writer's first use.

It is filed rather than repaired quietly. `checkBuildability()` is **left byte-identical in
behaviour**; the failure is pinned by a named test
(*"checkBuildability applies a SQUARE-lattice width rule to a honeycomb design"*); and the repair is
**additive** — `checkBuildabilityOnItsOwnLattice()`, which reproduces the original **field for
field** on a square design (asserted as a test, because a second implementation that agreed by
construction would prove nothing) and, on any other lattice, **withholds** the width rule with its
reason instead of answering it wrongly. A boolean cannot say *not applicable*; the new report has a
third state and a `notApplicable` list.

Two rules are withheld on the block, and the second matters as much as the first: the
crossing-adjacency predicate reads the design's **helix ordering**, which on a raster is the scaffold
path — it is *not* the interface graph, and `C-0154` records that a honeycomb block's interfaces are
not a path graph at all.

## 5. What a non-tautological `require` refused, and why it matters more than what it emitted

Drawing the block means giving all 60 helices an axial span, including the two path ends whose
**turn sense** is undefined — the corpus's level walk has nothing to give them.

`CLAUDE.md` states, of the two-length raster, that *"every x-raster row spans exactly the larger of
the two lengths and the block exceeds it by exactly the stagger"*. Read as a statement about a row's
**helices**, that says each end helix spans 109, and the block then needs **6 337 nt** against the
6 330 `HoneycombRasterProfile` charges — short by exactly the **stagger**, at every candidate pair
and both cross-sections, which is the shape of a real finding. It is not one. The sentence is about
a row's **union window**, and a row is **not uniform**:

| parity | the six helices of a row, in path order | union window |
|---|---|---|
| even rows | 102, 109, 109, 109, 109, 102 | 109 |
| odd rows | 109, 102, 102, 102, 102, 109 | 109 |

What determines a path end's length is a **symmetry**: the raster repeats with row parity, so an end
helix takes the window its own **position** carries in the rows of its own parity. Drawn that way
the ends come out at **102 and 109** — *"one of each length"*, which is `C-0140`'s accounting
**exactly**, and the block needs **6 330 nt** with **919** spare on M13.

The thing that caught it was a `require` that had been **tautological** in the first draft (it
compared a value against the expression that produced it) and was rewritten to assert the premise
instead. It refused the construction, thirteen tests failed, and the challenge number reserved for
this claim was released unused. **A tautological assertion is not a weak check, it is not a check**
— and the corollary is the reason this task was worth doing: a quantity that nothing draws is a
quantity nothing checks, and an interchange writer checks every geometric quantity at once, because
a file has to say where every base is.

## 6. What the artifacts deliberately do not carry

`P5` asks the artifact to say which claim recommended it and at which width reading, because §3's
`40 × 40 nm` is still two readings apart (decision 8, `T-242`). `gpd/designs/README.md` carries both
— **39.44 nm** on the bounding box (`−1.40 %`) and **37.06 nm** on the row span (`−7.35 %`) — and
says that the `.sc` file carries the box, because the stagger *is* the difference between two rows'
domains and no single number in the format can express it.

Three omissions are stated in the artifact rather than implied:

- **No staple set on the honeycomb block.** This corpus determines the block's row lengths, turn
  senses, closure, crossover columns and station ladder; it has never determined a honeycomb
  **staple routing**. Inventing one would put design into an artifact that nothing graded. The file
  is openable, inspectable and checkable, and **not yet foldable**.
- **No turn loopouts.** Every raster turn is a direct scaffold crossover, which is what the closure
  rule certifies at zero forced crossovers and what the level walk models. `C-0147` prices an
  unpaired turn allowance separately — 6 nt on reach, 8 nt affordable, 28 nt as built — and emitting
  one needs scadnano's heterogeneous `domains` array, which this reader does not parse. That is a
  **schema** step, not a lattice question.
- **No sequence on the block.** The routing fixes the base count, not the M13 phase.

## 7. Mutation table

A test that passes on correct code proves nothing about what it would catch. Each row is one
mutation of the new sources, run against the package's 41 tests; the restored source passes all 41.

| mutation | named tests that fail |
|---|---|
| the two path ends take their **row's span** instead of their parity position's window | **2** |
| the scaffold direction does not alternate along the raster path | **5** |
| the lattice-aware check answers the **square** width rule on every lattice | **2** |
| grid position coordinates swapped, `(h, v)` → `(v, h)` | **2** |
| the writer no longer refuses a design with no grid positions | **1** |
| the writer no longer refuses a helix-record count that disagrees | **1** |

The first row is the one this claim's §5 is about, and it is the reason the mutation was worth
writing: the construction it restores is the plausible one.

## 8. Where the artifacts live, and why not in `gpd/results/`

A result is the output of a computation and is re-run by an `Entry points` row; a **design** is an
interchange artifact. `gpd/designs/` holds them, and what replaces the re-run guarantee is stronger:
`CommittedDesignsTest` asserts each committed file is **byte-identical** to what the writer emits
today, so a change to the writer, to the lattice or to the recommended raster fails the build rather
than leaving a stale file somebody could fold. No study of this claim writes a result file, so
`tools/check-entry-points.py` is unaffected and stays at 0 defects.

## 9. Validity range

- **Every number here is a lattice statement.** No temperature, buffer, load or stiffness enters,
  and nothing in this claim is evidence about the device.
- **`P1` is a compatibility claim, evidenced at one version** — scadnano 0.21.1, both as the declared
  format version and as the reference parser. A future format revision is not covered.
- **The reference implementation loading a file is not scadnano's web editor rendering it**, and it
  is not a folding prediction. Nothing here says the block folds.
- **The honeycomb block is a scaffold routing.** Its staple set, its turn loopouts and its sequence
  are absent by decision, and any downstream use must carry that.
- **The path ends' lengths rest on the row-parity symmetry**, which is asserted on this raster and
  this cross-section rather than proved in general; the builder refuses any raster where it fails.
- **The lattice-aware check withholds rather than decides on a honeycomb design.** It does not
  implement `C-0148`'s closure predicate from the file — that needs the turn senses, which the `.sc`
  format does not carry in a form this reader derives — so a honeycomb design's *drawability* is
  still answered by `HoneycombRasterResidues` and not by the imported file.

## 10. Still open

- **A honeycomb staple router.** It is the difference between an inspectable design and a foldable
  one, and it is the largest single thing between this corpus and a bench order.
- **Loopout support in the reader and writer**, one schema change (`domains` becomes a heterogeneous
  array), which would let `C-0147`'s turn allowance be *drawn* rather than priced.
- **`C-0148`'s closure predicate, derived from an imported file.** It would make
  `checkBuildabilityOnItsOwnLattice()` answer on the honeycomb instead of withholding, and it is the
  capability nothing in the field has: caDNAno will let you draw a raster that does not close.
- **A caDNAno v2 writer.** scadnano's own exporter exists; whether this repository needs its own is a
  question about who the recipient is.
