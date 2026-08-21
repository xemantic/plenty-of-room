# C-0161 — **THE MECHANICS NOW TAKES A DESIGN, SO A PLACEMENT RESULT STOPS BEING A PROPERTY OF A SET OF CONSTANTS.** `OrigamiGrillage` and `HoneycombGrillage` are constructible from a scadnano `.sc` file, or from a lattice plus a cross-section, and the `Gen1Tile` constructors are **untouched**: `T-10`'s own tile rebuilt through the new path has a load vector **bit-identical** over 855 degrees of freedom, a solved field agreeing at `0.0`, and it reproduces `T-10`'s committed edge-taper peak dishing `1.28014255 nm` at `2.1e−9` — which is that file's own nine-digit emission precision. `C-0151`'s recommended `10 × 6` block, until now *"a pair of integers in a study literal"*, imports its cross-section out of its own helix grid positions and reproduces `HoneycombBlock(10, 6)` bit-identically over **4 080** degrees of freedom. **Exactly one of the five scalars `OrigamiGrillage` reads is a lattice number** and it was a constant with a lattice in its name: `samePairPeriod = azimuths × step`, so a specification on a lattice whose single-layer interfaces are **not a path graph** is now REFUSED (`C-0154`) rather than silently reshaped. **`P4` graded a design this corpus did not draw — `scadnano.origami_rectangle`, the reference implementation's own Rothemund rectangle — and it reports three things nothing here had: the rectangle is representable EXACTLY (90 lattice sites against 90 drawn, 0 absent) at a column parity sequence `[0, 1, 0, 1, 1, 0]` that NO phase sweep in this corpus can generate, because a SEAM breaks the alternation `CrossoverLayout.centred` and `.phased` impose by construction; the reference implementation draws every crossover as TWO strand crossings at adjacent offsets, where this corpus draws one, which is worth a factor of two in the census and `1.087×` in the flatness ([`CH-0209`](../challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md)); and `rowBasePairs` is not a span.** And a phase integer does not determine a sheet: at the buildable 38.08 nm the corpus's phases **16 and 0** put the columns in identical positions with every parity exchanged, and the **file** says which one was drawn

| | |
|---|---|
| **Task** | [`T-267`](../tasks/T-267-mechanics-on-imported-design.md) — mechanics on an imported design, so a placement result stops being a property of one tile |
| **Leaf** | none — step 5 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Verification type** | **in-silico** (the existing grillage finite-element models, rebuilt through a new constructor and solved) **+ logical** (every lattice derivation is integer arithmetic on the design file, and the reproduction is an exact identity rather than a tolerance) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** This claim is about **software structure and provenance**. `PASS` means a design file and a set of constants produce the same object, and that what an imported design reports is recorded. **Nothing is folded and nothing is measured** |
| **Verdict** | **PASS on `P1`, `P2`, `P3`, `P4`.** None of the five declared falsifiers fired. `F1` (the specification path does not reproduce `T-10`'s load vector bit for bit) did **not** fire, taken where the two objects are built by the same arithmetic. `F2` (the imported grillage builds crossovers the design does not draw) did **not** fire on any representable design. `F3` (a honeycomb design is accepted by the sheet grillage) did **not** fire — it is refused and routed to `HoneycombGrillage`. `F4` (the reference implementation's rectangle cannot be expressed) did **not** fire, and its *not* firing is this task's most valuable outcome. `F5` (a design stating no geometry is given a default) did **not** fire. **`CH-0209` is RAISED** |
| **Provenance** | Result [`gpd/results/T-267-mechanics-on-imported-design.json`](../results/T-267-mechanics-on-imported-design.json), study [`structure/ImportedDesignMechanicsStudy.kt`](../../src/main/kotlin/structure/ImportedDesignMechanicsStudy.kt) (**new**). New sources [`structure/DesignedGrillage.kt`](../../src/main/kotlin/structure/DesignedGrillage.kt) and [`tile/DesignedHoneycombGrillage.kt`](../../src/main/kotlin/tile/DesignedHoneycombGrillage.kt). Two existing sources edited **additively only**: [`lattice/CrossoverLattice.kt`](../../src/main/kotlin/lattice/CrossoverLattice.kt) gains one declared property (`singleLayerInterfacesFormAPath`) beside `hasCentroSymmetricPhaseCongruence`, and [`design/ScadnanoDesign.kt`](../../src/main/kotlin/design/ScadnanoDesign.kt) gains four accessors (`axialWindowBasePairs`, `axialSpanBasePairs`, `risePerBasePairOrNull`, `interhelicalDistanceNm`); **no existing behaviour changes and no existing method is touched**. Third-party artifact [`gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc`](../designs/third-party/scadnano-origami-rectangle-16x8.sc) with its retained generator [`tools/scadnano/emit-reference-rectangle.py`](../../tools/scadnano/emit-reference-rectangle.py). **23 new gate-named tests, written first and watched fail**, in [`structure/DesignedGrillageTest.kt`](../../src/test/kotlin/structure/DesignedGrillageTest.kt) (20) and [`tile/DesignedHoneycombGrillageTest.kt`](../../src/test/kotlin/tile/DesignedHoneycombGrillageTest.kt) (3). The whole Kotlin suite is green with them in: **3 115 tests in 177 classes, 0 failures, 0 errors**. **Mutation-tested**, six mutations of the new sources failing **1, 1, 5, 1, 3 and 1** named tests with the restored source passing 23 of 23 — and the sixth failed **nothing** on the first pass, which is §9. Two study runs are **byte-identical**, and the three `gpd/results/` hygiene gates read clean. **No committed result file moves, no study is re-run, and no claim's number changes**: `T-10`'s file is *read*, not re-emitted |
| **Conditions** | Lattice and structure only. 300 K, `k_BT = 4.141947 pN·nm`; lengths nm, forces pN, pressures `pN/nm²` = MPa, foundation `pN/nm³`. Every graded design is solved at the **same** foundation (`C-0001`'s secant, `0.012625625 pN/nm³`), the **same** interior pressure (`0.0619578686 pN/nm²`) and the **same** 50 % edge taper over one 4 nm Debye length, so a peak dishing over stroke compares lattices and not device states. `x` along the helices, `y` across, `w` positive downward |
| **Consumes** | [`C-0160`](C-0160-scadnano-writer.md) (the reader, the writer and the two committed designs), [`C-0157`](C-0157-crossover-hinge-constant-from-oxdna.md) (the simulated `.sc` and its single-coverage ground), [`C-0159`](C-0159-environment-interface.md) (the *declare the state you were read at* discipline `GrillageSpecification` carries), [`C-0154`](C-0154-honeycomb-grillage.md) (a honeycomb block's interfaces are not a path graph), [`C-0009`](C-0009-discrete-lattice-tile.md) / `T-10` (the grillage and the number reproduced), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the buildable 112 bp row and its seven columns), [`C-0090`](C-0090-buildable-raster-width.md) (two phases, identical positions, inverted parities), [`C-0151`](C-0151-closing-raster-selection.md) / [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the `10 × 6` block), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the phase is a design variable), `T-110` (`consumedCrossovers`, which is what makes an import exact) |
| **Constrains** | Closes [`ARCHITECTURE.md`](../../ARCHITECTURE.md)'s step 5. Raises [`CH-0209`](../challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md) against `C-0157`'s stated ground and `C-0160`'s `noSiteIsCrossedTwice` predicate. **Does not re-run any placement, phase or plan-ceiling study**: it supplies the constructor those re-runs need, and says so |

---

## 1. The cheap bound, which is why `P2` was achievable at all

`CLAUDE.md` already records the surface: *"`OrigamiGrillage` NEVER READS `layers` OR
`interlayerCoupling` — it takes exactly five scalars from its sheet"*. Re-read before anything was
written, that says the constructor is a **mapping** and not a model change. One further reading
turns it into the design:

| scalar | what it is |
|---|---|
| `interhelicalDistance` | a **cross-section** number — and a `.sc` file states it, as `2r + gap` |
| `crossoverSpacing` | a **lattice** number — `azimuths × step`, 4 × 8 = 32 bp square, 3 × 7 = 21 honeycomb |
| `crossoverHingeStiffness` | a **material** number — no design file carries it and none ever will |
| `duplex.bendingRigidity` | a material number |
| `duplex.torsionalRigidity` | a material number |

So exactly **one of five** is a lattice number, and in this repository it was
`Gen1Tile.CROSSOVER_SPACING_SHEET_BP` — a constant with a lattice baked into its name.
`latticeCrossoverSpacing(lattice, mechanics)` derives it, and `GrillageSpecification`'s `init`
refuses a lattice whose single-layer interfaces are not a path graph, which is `C-0154`'s finding
made unrepresentable instead of remembered.

## 2. `P3` — the reproduction, and the discipline it is taken under

`CLAUDE.md` fixes the form: **bit-identity is assertable on `assembleLoad`**, a fixed-order
scatter-add, and **not on a solved field**, where two identically constructed grillages differ by
~4 ulp inside one JVM. It also fixes the load case: a bare uniform pressure on a free tile dishes
exactly zero, so that comparison tests nothing. The load case is `T-10`'s own electrostatic edge
taper, plus one off-centre point load.

| the two objects | load vector | solved field | column positions |
|---|---|---|---|
| `T-10`'s tile from `Gen1Tile`'s constants **against** the lattice-plus-cross-section specification | **bit-identical**, 855 dof | `0.0` | `0.0` nm |
| `C-0151`'s block from `HoneycombBlock(10, 6)` **against** the imported `.sc` | **bit-identical**, 4 080 dof | `0.0` | `0.0` nm |
| `C-0086`'s 38.08 nm tile from the `.sc` **against** `anchoring.rasterColumnLayout(16, …)` | **not** bit-identical | `4.7e−12` | `1.8e−15` nm |

And the third row is a result rather than a failure, which is why it is reported beside the other
two instead of being tuned away. `(offset − 56) × 0.34` and `5.44 + k × 2.72` are the same seven
numbers and **not the same seven `Double`s**; `CLAUDE.md`'s rule one level further out is that
bit-identity is assertable only where the two objects are built by the **same arithmetic**. The
falsifier `F1` is therefore taken on the rows that are owed exactness, and the cross-construction
row is asserted at `1e−10` — where it sits two decades inside.

The number that ties this to the corpus rather than to itself is `T-10`'s own committed one, read
out of `gpd/results/T-10-discrete-lattice-tile.json` rather than transcribed:

| | |
|---|---|
| peak dishing, 50 % edge taper, `C-0001` secant, through the **specification** path | `1.28014255 nm` |
| the committed `T-10` value | `1.28014255 nm` |
| relative departure | **`2.1e−9`** — which is that file's own nine-significant-digit emission |

## 3. `P1` — the design is the constructor, and the difference is expressible

Everything geometric comes out of the file: the duplex count is the helix count, the footprint is
the **axial window of every strand in it**, the crossover columns are the offsets its **staple**
crossings occupy, the column parities are the parities of the lower helices those crossings join,
and the interhelical distance and rise are the design's own geometry block where it has one.

The part that makes the import **exact** rather than approximate is `T-110`'s own design variable.
The grillage's parity lattice offers a site at every `(beam, column)` with
`beam ≡ parity (mod 2)`; a design need not build all of them, and the difference is carried as
`consumedCrossovers`. So the imported grillage builds **exactly the crossovers the file draws**,
asserted site by site against `(lowerHelix, offset)` rather than by count.

On the two designs this corpus owns the difference is empty — 49 of 49 on the sheet, 0 absent —
which is itself a check on `C-0086`'s lattice: the file's seven columns at `8 + 16k` split 4/3
between the parities and produce 49, and the parity lattice built from the file produces the same
49.

## 4. `P4` — a design this corpus did not draw, and what it reported

`gpd/designs/*.sc` are this corpus's own, so grading either tests the plumbing and not the claim.
`scadnano.origami_rectangle.create` is the **reference** implementation's own canonical Rothemund
rectangle — its authors' code, its conventions — and the artifact is its `to_json()` verbatim,
with a retained generator and a `--check` mode.

| | corpus sheet | reference rectangle | recommended block |
|---|---|---|---|
| drawn by | this corpus | **scadnano 0.21.1** | this corpus |
| grid | square | square | honeycomb |
| duplexes / helices | 15 | 16 | 60 |
| axial span | 112 bp = 38.08 nm | **128 bp** = 43.52 nm | 116 bp = 39.44 nm |
| geometry block | rise, gap, twist | **none at all** | rise, radius, gap, twist |
| columns, as drawn | 7 | **12** | — |
| columns, as junctions | 7 | **6** | — |
| column parities | `[0,1,0,1,0,1,0]` | **`[0,1,0,1,1,0]`** | — |
| parities alternate | yes | **no** | — |
| staple crossings | 49 | **90** | 0 (no staple set) |
| lattice sites built | 49 | 90 (or 45) | — |
| absent sites | **0** | **0** | — |
| representable as `OrigamiGrillage` | yes | **yes** | **refused** |
| peak dishing / stroke | `0.259548049` | `0.258057772` / `0.28058418` | — |

The census is confirmed from **outside** this repository. `tools/scadnano/validate-sc.py` loads
all three files in the reference implementation with **zero warnings** and derives the counts with
its own parser:

```
python3 tools/scadnano/validate-sc.py gpd/designs/*.sc gpd/designs/third-party/*.sc
# 3 of 3 file(s) load in the reference implementation without warnings
```

| file | helices | scaffold domains | scaffold crossings | staple crossings |
|---|---|---|---|---|
| `gen1-sheet-square-15x112.sc` | 15 | 15 | 14 | **49** |
| `third-party/scadnano-origami-rectangle-16x8.sc` | 16 | **31** | **30** | **90** |

So the **90** is not this reader's arithmetic: the generator's own library counts it on the
generator's own output, and the import agrees. The 31 scaffold domains against 16 helices are the
seam — a boustrophedon on 16 helices has 16.

Four things came back, and three of them are things no design in this repository has.

**(a) It is representable, and exactly.** `F4` was declared because the honest failure mode of
this task is *"the grillage's lattice assumptions are narrower than the designs the field draws"*.
It did not fire: 90 lattice sites against 90 drawn, zero absent.

**(b) A seam breaks the parity alternation, and no sweep here can generate one.**
`CrossoverLayout.centred` and `CrossoverLayout.phased` alternate the parity **by construction**;
the rectangle's seam doubles a column pitch — its junction columns sit at `31.5, 47.5, 63.5, 79.5,
111.5, 127.5` with a 32 bp gap where the others are 16 — so two consecutive columns serve the
**same** interface parity. `CrossoverLayout` carries the parities explicitly and represents it
without complaint. **Every phase-swept placement, count and flatness result in this corpus is over
the alternating family, and a seamed sheet is outside it.** That is a scope statement about a
large body of standing work, and it is stated rather than acted on: nothing here re-runs those
studies.

**(c) The reference implementation draws a crossover as two strand crossings.** Every one of the
rectangle's fifteen interfaces carries its crossings in **adjacent pairs** at `o` and `o+1` —
`31/32, 63/64, 127/128` on the even-lower interfaces, `47/48, 79/80, 111/112` on the odd ones.
This corpus's own `.sc` carries **one**. The two readings give 90 lattice sites or 45 on the same
file, and reading it as junctions softens the tile by `1.087×` in peak dishing over stroke. The
reading is therefore a **named parameter** of every import (`AdjacentCrossingReading`) and not a
default, and the disagreement is filed as [`CH-0209`](../challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md).

**(d) It states no geometry at all, and is refused.** With nothing supplied, the import returns no
specification and names the reason — *"a design that does not say what gap it was drawn at has not
said 2.69 nm, and guessing one is the same class of error as guessing a grid"*. That is
`ScadnanoDesign.lattice`'s own refusal, one field down, and it is `F5`.

## 5. A phase integer does not determine a sheet, and the file does

`C-0090` records that two phases *"give identical column positions (to `1e−12` nm) with inverted
parities"* and that the result is *"a physically different sheet"*. Met from the file side at the
buildable 38.08 nm with the row-end plane excluded, the pair is **16 and 0**:

| | phase 16 | phase 0 |
|---|---|---|
| column positions | identical, departure **`0.0` nm** | |
| parities | `[0,1,0,1,0,1,0]` | `[1,0,1,0,1,0,1]` — **every one exchanged** |
| peak dishing under the edge taper | `1.273683 nm` | `1.273683 nm`, departure `6.7e−12` |

The **drawn** design occupies phase 16, and no integer in this corpus recorded that: the oxDNA
driver calls the same tile *"phase 8"* because it counts its phase from the **row start** where
this corpus counts from the **tile centre**, and `112/2 = 56 ≡ 8 (mod 16)` is the whole of the
difference. Two conventions, one lattice, and the file is what settles which.

The two peaks agreeing is **a symmetry of this load case, not an insensitivity to the parity**:
the exchange is the reflection `b → D−2−b` across the helices and the edge taper is symmetric in
`y`. Read at an asymmetric load the two would part.

## 6. `rowBasePairs` is not a span, and only a foreign design could show it

`C-0160`'s reader takes the row length as *"the longest offset any scaffold domain reaches"*.
Both artifacts this repository writes start at offset zero — the honeycomb block is deliberately
shifted there by its own builder — so the reading is exact on everything committed. scadnano's
rectangle places its flanking columns first and starts its scaffold at **offset 16**, where
`rowBasePairs` reads 144 bp against a true span of **128**: 16 bp of empty lattice counted as tile,
5.44 nm on a 43.52 nm footprint.

`axialSpanBasePairs()` is added **beside** it and `C-0160`'s method is left exactly as published,
because it is correct for what it says it does and every committed number rests on it.

## 7. Validity range

- This is a **constructor**, not a model. Every number it produces is the number the existing
  grillage produces on the same lattice; no physics is added and none is re-derived.
- The elasticity is never read from a design file. `DuplexMechanics` is what the caller supplies,
  by name, and its `gen1()` factory is `Gen1Tile`'s cited constants unchanged.
- `OrigamiGrillage` remains a **single-layer, path-graph** object. A cross-section of more than
  one layer is refused *here* rather than ignored *there*, and a honeycomb design is routed to
  `HoneycombGrillage`.
- The gradings hold the foundation, the pressure and the taper fixed across designs of different
  footprint. A peak dishing over stroke in §4 compares the lattices' own responses and is **not**
  an operating point of the Gen-1 actuator.
- The reference rectangle is graded at this corpus's own single-layer `d = 2.69 nm` because its
  file states none. That number is supplied and declared, and every length derived from it is
  nominal in it.
- **No placement, phase or plan-ceiling study has been re-run through the imported path.** This
  claim supplies the constructor; re-running them is separate, and larger.

## 8. What would falsify this

1. A load vector from the specification path that is not bit-identical to the constants path where
   the arithmetic is the same — the new path would then not be the same object, and which of the
   five scalars moved would have to be found before anything is adopted.
2. An imported design whose grillage builds a crossover the file does not draw, or misses one it
   does.
3. A honeycomb design accepted by the single-layer sheet importer.
4. A published origami design this importer cannot express **and** that `HoneycombGrillage` cannot
   either — the rectangle was the test and it passed, but one design is not a survey.
5. An oxDNA relaxation showing that the adjacent-offset crossing pair does not fold, or folds to a
   materially different hinge constant. That is `CH-0209`, and it would move a reading this task
   deliberately leaves named rather than chosen.

## 9. The mutation test found a gap, and the gap is the mechanism this claim is about

Six mutations, each run against the 23 named tests:

| mutation | named tests it fails |
|---|---|
| M1 — the sheet importer accepts a honeycomb design | **1** |
| M2 — a specification no longer refuses a non-path lattice | **1** |
| M3 — the crossover spacing is the any-azimuth step, not the same-pair period | **5** |
| M4 — the footprint is `rowBasePairs`, not the axial span | **1** |
| M5 — every adjacent offset merges, whatever it joins | **3** |
| M6 — the parity lattice's surplus is **not** carried as `consumedCrossovers` | **0**, then **1** |

**M6 failed nothing.** On all three designs available to this task the surplus is empty — the file
and the parity lattice agree site for site — so replacing `absent.toSet()` with `emptySet()` is
indistinguishable, and the mechanism §3 calls *"what makes the import exact rather than
approximate"* had no test at all.

It is closed by **constructing** the surplus rather than by asserting it: the committed sheet with
one staple dropped — chosen as one carrying exactly one crossing, so all seven columns stand — is
48 crossings against a 49-site parity lattice, and the imported grillage builds **48**, at the
sites the file draws. M6 then fails that test and nothing else.

`C-0083`'s rule read on a test rather than on a gate: *a predicate can always be narrowed until
the tree is clean*, and a test suite that only ever meets designs where a mechanism is inert says
nothing about the mechanism.
