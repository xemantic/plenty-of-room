# `gpd/designs/` — the designs this programme recommends, as files somebody else can open

Every other tool in this field reads **and writes** a design.
This corpus's tile was a set of Kotlin constants,
so its recommended tile could not be handed to anybody without a human redrawing it —
and a design somebody redraws is not the design that was graded.

These files are written by [`design/DesignEmission.kt`](../../src/main/kotlin/design/DesignEmission.kt):

```
./gradlew study -Pstudy=design.DesignEmissionKt
```

They are **not** result files and no `Entry points` row re-runs them.
What keeps them from going stale is stronger:
[`CommittedDesignsTest`](../../src/test/kotlin/design/CommittedDesignsTest.kt)
asserts each committed file is **byte-identical** to what the writer emits today,
so a change to the writer, to the lattice or to the recommended raster fails the build.

Both files load in the **reference** scadnano implementation (0.21.1) with **zero warnings**:

```
python3 tools/scadnano/validate-sc.py gpd/designs/*.sc
```

Task [`T-266`](../tasks/T-266-scadnano-writer.md), claim [`C-0160`](../claims/C-0160-scadnano-writer.md).

---

## `gen1-block-honeycomb-10x6-102-109.sc` — the recommended block

**Recommended by [`C-0151`](../claims/C-0151-closing-raster-selection.md)**
(selection), on [`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md)'s
scaffold-closure rule, at [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md)'s
`10 × 6` cross-section.

| | |
|---|---|
| cross-section | ten corrugated x-raster rows of six helices, 60 helices, four layers |
| row lengths | **102 / 109 bp**, a 7 bp stagger — the least a *drawable* two-length honeycomb raster can carry |
| per-row composition | a row is **not** uniform: 102, 109, 109, 109, 109, 102 on one parity and 109, 102, 102, 102, 102, 109 on the other, both spanning a 109 bp window |
| closure | closes on caDNAno's published `±5 bp` scaffold rule at **zero** forced crossovers, `b₀ = 5` |
| raster turns | 59, every one a direct scaffold crossover |
| station ladder | phase **16**, inter-row offset **14 bp**, **55** of 60 stations on the face |
| scaffold | **6 330 nt**, 919 spare on one M13 |

### Which width reading this is quoted at

§3 specifies a `40 × 40 nm` tile footprint and a two-length raster has **two** width readings
`3.57 %` apart, which is [decision 8](../../DECISIONS-FOR-NDI.md) and task
[`T-242`](../tasks/T-242-which-width-section-three-names.md).
Both are stated here so neither is inherited silently:

| reading | along the helices | against §3's 40 nm |
|---|---|---|
| **bounding box** (the block's axial extent, 116 bp) | **39.44 nm** | `−1.40 %` |
| **row span** (what any one x-raster row spans, 109 bp) | **37.06 nm** | `−7.35 %` |

Across the helices the block is `10 × 3d/2` = **38.04 nm** at the SAXS honeycomb `d = 2.536 nm`.
The `.sc` file carries the bounding-box reading, because an offset is an offset:
the stagger is *in* the file, as the difference between two rows' domains,
and no single number in the format can express it.

### What this file deliberately does not carry, and why

- **No staple set.**
  This corpus determines the block's row lengths, turn senses, closure, crossover columns and
  station ladder. It has never determined a honeycomb **staple routing**, and inventing one here
  would put design into an artifact that nothing graded.
  The file is therefore openable, inspectable and checkable — and **not yet foldable**.
- **No turn loopouts.**
  Every raster turn is emitted as a direct scaffold crossover with no unpaired nucleotides, which
  is what the closure rule certifies at zero forced crossovers and what the corpus's own level walk
  models. [`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md) prices an unpaired
  turn allowance separately — 6 nt on reach, 8 nt affordable on M13 at a 112 bp row, 28 nt as built
  — and emitting one needs scadnano's heterogeneous `domains` array, which this reader does not parse.
- **No sequence.** The scaffold routing fixes the base count and not the M13 phase.

---

## `gen1-sheet-square-15x112.sc` — the sheet the oxDNA run simulated

Not the recommended device tile —
[`C-0126`](../claims/C-0126-four-layer-supersession.md) puts the device on the four-layer
honeycomb block above.
This is [`C-0086`](../claims/C-0086-seamless-scaffold-routing.md)'s single-layer square-lattice
Rothemund raster, the object
[`C-0157`](../claims/C-0157-crossover-hinge-constant-from-oxdna.md)'s oxDNA run actually simulated,
**round-tripped through this repository's writer** from
[`src/test/resources/gen1-tile.sc`](../../src/test/resources/gen1-tile.sc).

| | |
|---|---|
| duplexes | 15 |
| row length | **112 bp = 38.08 nm** — the only buildable *seamless* raster width near §3's 40 nm |
| across the helices | `14 × 2.69` = **37.66 nm** at the SAXS single-layer `d` |
| crossover columns | seven, at phase **8** |
| crossovers | **49**, split 4/3 between the two parities |
| raster turns | 14, one per interface |
| scaffold | 1 680 nt of M13, sequence carried |

It is here because it is the one design in this programme that has been *simulated*,
and because it is what makes the writer's round trip a proof rather than an assertion:
what is written must read back as the same 15 / 112 / 8 / 7 / 49 / 4-3 the corpus derived
independently, and does.

---

## `third-party/scadnano-origami-rectangle-16x8.sc` — somebody else's design

Not this programme's, and that is the point.
It is what
[`scadnano.origami_rectangle.create`](https://github.com/UC-Davis-molecular-computing/scadnano-python-package)
emits at `num_helices = 16, num_cols = 8` —
the **reference** implementation's own canonical Rothemund rectangle,
written by its authors and following scadnano's conventions rather than this corpus's.

```
python3 tools/scadnano/emit-reference-rectangle.py          # writes it
python3 tools/scadnano/emit-reference-rectangle.py --check  # asserts the committed copy is current
```

Nothing in it is edited: the file is the generator's `to_json()` verbatim.
It exists so that
[`T-267`](../tasks/T-267-mechanics-on-imported-design.md) can grade a design
**this corpus did not parameterise**,
which is the only honest test of an import.

| | |
|---|---|
| grid | square, **no geometry block at all** |
| helices | 16 |
| axial window | offsets `[16, 144)` — **128 bp**, and the scaffold does **not** start at zero |
| scaffold | one strand, 31 domains, a **seam** |
| staple crossings | **90**, in adjacent pairs at `o` and `o+1` on every interface |
| crossover columns | **12** offsets, **6** junctions |
| column parities | `[0, 1, 0, 1, 1, 0]` — the seam breaks the alternation |

What it reported when graded is [`C-0161`](../claims/C-0161-mechanics-on-an-imported-design.md)
and [`CH-0209`](../challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md);
three of the six rows above are things no design in this repository has.
