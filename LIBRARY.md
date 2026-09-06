# `origami-engine` — the library extracted from the corpus

This repository is two things that a single Gradle module could not tell apart.

One is a **corpus**: a research programme's studies, its claims and challenges under `gpd/`, and the gates in `tools/` that keep them honest.
It is a record, it is specific to this programme, and its value is the record.

The other is a body of **models** — a solved grafted layer, a solved 2:1 electrolyte gap with an electrode boundary, the crossover lattices and their design rules, a grillage on a Winkler foundation with crossover prestrain as a load, a coupling influence bank under measured fabrication dropout, and a device force balance with its pull-in fold.
None of that has a counterpart in the DNA-nanotechnology tooling, and none of it is about this programme.
That is `origami-engine`.

## The two invariants

**The module must not depend on the root.**
Gradle enforces it: the root project declares `api(project(":origami-engine"))` and there is no edge back.
Everything else in this document is a consequence of that one sentence.

**The module reads no result file and emits none.**
All 148 `fun main` declarations in this repository are in the root project; the module has none.
The five occurrences of `gpd/results` under `origami-engine/src` are prose inside KDoc, not file opens.

## What is in it

| source set | files |
| --- | --- |
| `origami-engine/src/main/kotlin` | 74 |
| `origami-engine/src/test/kotlin` | 62 |
| `origami-engine/src/testFixtures/kotlin` | 1 |
| `src/main/kotlin` (corpus) | 272 |
| `src/test/kotlin` (corpus) | 149 |

By package, the module holds `structure` (14), `electrostatics` (9), `coupling` (8), `actuator` (8), `environment` (6), `brush` (6), `tile` (5), `material` (5), `poroelastic` (4), `quantities` (3), `lattice` (2), `crossover` (1), `anchoring` (1), and `Physics.kt` and `Vectors.kt` at the package root.

## The rule that decided each file

A file is in `origami-engine` when it is a model **and** its whole transitive dependency set is too.
Both halves are load-bearing, and the second is what actually moved files back out.

`structure/LatticePhaseCensus.kt` is the worked example.
It is a lattice design rule, so by the first half it belongs in the module; it was moved in, and it does not compile there, because it reads `B_DNA_TWIST_PER_BASE`, `OUT_OF_PLANE_OFFSET_BASE_PAIRS`, `rasterColumnPositions` and `outOfPlaneOffsets` from `structure/TwistCorrectedRaster.kt`, which reads `anchoring`.
It was moved back, and so was its test, and so was `structure/HoneycombRasterTurnSenseTest.kt`, which needs it.
The alternative was to drag `anchoring` — 84 main sources of placement search fitted to this programme's own claims — into a library, which is the thing the split exists to prevent.

Note the trap that makes this harder than it reads: both modules keep the same package names, so a same-package reference needs no `import` and a scan of a candidate file's import list **understates** what moving it costs.
Only the compiler knows.

## What the split forced

### `internal` is module-scoped, so eleven declarations became public

Inside one module the corpus could reach an `internal` declaration; across a module boundary it cannot, and 39 of the 97 compile errors this split first produced were exactly that.
Widening is the honest resolution rather than a workaround: the corpus was already using these, so the extraction did not make them API, it made it visible that they always were.

| declaration | file |
| --- | --- |
| `bracketedRoot`, `halfCircleMoment` | `brush/GraftedLayer.kt` |
| `contourSteps` | `brush/SelfConsistentField.kt` |
| `SEARCH_DECISION_DIGITS`, `searchDecision` | `coupling/RobustDistribution.kt` |
| `MultiStateSurrogate` constructor | `coupling/RobustDistribution.kt` |
| `InfluenceSurrogate` constructor | `coupling/NonUniformCoupling.kt` |
| `SharedBody` constructor | `coupling/SharedBodyCoupling.kt` |
| `GrillageDeflection` constructor | `structure/OrigamiGrillage.kt` |
| `OrigamiGrillage.basisAt`, `OrigamiGrillage.assembleLoad` | `structure/OrigamiGrillage.kt` |

The `internal constructor` on classes such as `SharedBodyModes`, `NonUniformDeflection`, `PlateDeflection`, `MeasuredDepthTable` and `IncorporationField` was **not** widened, because nothing in the corpus constructs them directly.
That asymmetry is the point: the boundary is now a measurement of what is used, not a guess.

### A smart cast stopped compiling

`structure/HoneycombRasterTurnSenseStudy.kt` read `turn.cell!!.x` on one line and `turn.cell.y` on the next.
A `!!` smart-casts a property only where the compiler can prove the property is stable, and it cannot prove that of a public property declared in another module — the other module is free to recompile it as a `get()`.
The fix is a local `val cell = turn.cell!!`, which is what the reader meant anyway.

### Four test files stayed behind

The module's own tests must not reach into the corpus either, and ten test files did.

Six of the ten were resolved by moving the declaration they needed into the module, because it turned out to be a model that had simply been left outside.
Eight files moved in that way, each with its own test: `electrostatics/ChargedSurface.kt`, `electrostatics/PolymerLayerPartitioning.kt`, `material/GraftedChi.kt`, `coupling/UniformityBudget.kt`, `structure/JointAllowable.kt`, `tile/HoneycombFaceLattice.kt`, `tile/HoneycombTwoLengthRaster.kt` and `tile/HoneycombCoupledTile.kt`.
A seventh, `structure/HoneycombRasterTurnSenseTest.kt`, went back to the root project with `structure/LatticePhaseCensus.kt`, the file it reads.

The last three could not be resolved either way, and neither could one of the tests that arrived with the files above.
Those tests are **cross-checks**: they assert that a library quantity agrees with a number a claim published, so they read both sides and belong on the side that can see both.
Each was extracted into the root project, whole, with its name unchanged, leaving the rest of its file in the module.

| root test file | what it reads from the corpus | tests |
| --- | --- | --- |
| `coupling/NonUniformCouplingCorpusCrossCheckTest.kt` | `synthesis.perPathSecantCeiling` | 1 |
| `coupling/StapleDropoutCorpusCrossCheckTest.kt` | `rimMask`, which reads `anchoring` | 1 |
| `structure/ConsumedCrossoverSheetCorpusCrossCheckTest.kt` | `anchoring.ArmAnchorage`, `anchoring.BForm`, `anchoring.tradePoint` | 1 |
| `tile/HoneycombFaceLatticeCorpusCrossCheckTest.kt` | `anchoring.maximumPlanCeilingForCount`, `structure.centroSymmetricPlacementsOn` | 3 |

Six tests of 3 656, and every name is preserved verbatim, which matters because this project's mutation harnesses match on test **names** and not on file paths.

## The tooling the split moved

A gate scoped to `src` does not fail after an extraction.
It comes back **clean**, over a corpus 137 files smaller than the one it was written for — the failure direction `CLAUDE.md` records under *a gate that cannot come clean is not a gate*, reached from the other side: this gate comes clean by not looking.

`tools/kotlin_sources.py` holds the source roots once, with seven self-tests, and **twelve** tools were wired to it.
Three of them were already broken and the other nine were quietly reporting a smaller number.

| tool | what was wrong |
| --- | --- |
| `tools/emission_header.py` | **crashed.** Opened `lattice/LatticeTag.kt` by an assembled path; the file had moved, so `--self-test` died with `FileNotFoundError` |
| `tools/T-214-emit-result.py` | **crashed.** Two of the six rounding entry points it opens, `structure/ResultRounding.kt` and `actuator/ActuatorResultRounding.kt`, had moved |
| `tools/T-296-emit-result.py` | **crashed.** Parsed constants out of `structure/Gen1Tile.kt`, which had moved, beside `anchoring/MeasuredBackbone.kt`, which had not |
| `tools/T-278-solver-provenance.py` | its `EMISSION_LAYER` — the cut at the serialisation boundary that takes a study's closure from 158 sources to 15 — named `structure/ResultRounding.kt` and `lattice/LatticeTag.kt`, both moved, so the cut would have stopped cutting |
| `tools/check-kotlin-format-strings.py` | defaulted to `roots = ["src"]` and reported `0 defect(s) over src` |
| `tools/result-reader-census.py` | censused two source roots of four |
| `tools/T-249-emit-result.py`, `tools/T-250-emit-result.py` | counted call sites over one root; six `roundedForResult` sites are in the module |
| `tools/T-225-emit-result.py` | censused wall clocks over one root |
| `tools/T-272-header-census.py`, `tools/T-272-typed-input-handles.py`, `tools/T-278-emitter-rounding-census.py` | resolved a declaration by name, or walked "every source", over one root |

The reader census adds no read edges, because the module reads no result file — but a census that cannot see a module cannot say so, which is the whole reason to widen it.
The property being restored in the nine is sharper than *it works*: **a pure file move must not move a census's answer**, and before this it moved several.

## What was deliberately not done

**The package root was not renamed.**
Both modules are still `com.xemantic.nano.plentyofroom.*`.
That is what lets a root study reference a module declaration with no import and no edit, so the split cost the corpus almost no churn — and it is also why moving a file between modules is not a local decision (see *the rule that decided each file*).
Renaming would touch every source in the repository and every claim that cites a class name, and it is a separate step.

**Nothing is published.**
`group` is set and there is no `maven-publish`, no version and no POM.
The module is consumed by project path.
Publishing it means choosing an artifact id, a versioning policy and a compatibility promise for the eleven declarations widened above, and none of those decisions has been taken.

**`anchoring` and most of `tile` were not split.**
They hold 84 and 57 main sources, and some are models rather than studies — `tile/HoneycombGrillage.kt` most obviously, which is a lattice on a foundation exactly as `structure/OrigamiGrillage.kt` is.
They stayed because their dependency sets reach into placement searches fitted to this programme's claims, and untangling that is a body of work rather than a move.
The boundary is therefore **sound but not complete**: everything in `origami-engine` belongs there, and not everything that belongs there is in it yet.

## Checking that the boundary still holds

```bash
./gradlew :origami-engine:test          # the library alone -- 961 tests
./gradlew test                          # the corpus on top of it -- 2 695 more, plus the gates
tools/verify.sh --committed             # the authoritative run, at HEAD
python3 tools/kotlin_sources.py --self-test
```

Two of the three claims in this document are enforced rather than asserted.
A dependency from the module back to the root fails `:origami-engine:compileKotlin`.
A `fun main` in the module fails `tools/check-entry-points.py`, which is wired into `:test` and reports it as `LIBRARY-ENTRY-POINT` — the invariant used to be prose, and this repository has recorded *a convention is not a mechanism* six times.

The third — that the module opens no result file — is still prose.
Its five occurrences of `gpd/results` are all KDoc, and nothing stops a sixth from being a file open.
