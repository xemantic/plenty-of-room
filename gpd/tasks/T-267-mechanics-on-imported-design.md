# T-267 — mechanics on an **imported** design, so a placement result stops being a property of one tile

| | |
|---|---|
| **Leaf** | none — step 5 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | the iteration-39 restructure |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### What it costs not to have done this

`OrigamiGrillage` takes its lattice from `Gen1Tile`'s constants.
So every placement, phase, plan-ceiling and flatness number in this corpus is a property of **a set of
constants**, not of a design — and when iterations 33–34 found that the four-layer cross-section every one of
those numbers was solved on **is not a honeycomb** (every `edgeY` exactly 1.5× too small), the results were
**invalid rather than re-runnable**.

That is the whole cost of the missing seam, and it has already been paid once.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | `OrigamiGrillage` and `HoneycombGrillage` constructible from a `ScadnanoDesign`, or from a lattice plus a cross-section |
| **P2** | the existing `Gen1Tile` constructor **retained**, so the step is additive |
| **P3** | at least one existing placement or flatness study re-run through the new path reproducing its committed result **bit for bit** — and note `CLAUDE.md`'s own caveat that bit-identity is assertable on `assembleLoad` and not on a solved field, so the load vector is the object to compare exactly and the solved field at `1e-10` |
| **P4** | the imported path exercised on a design the corpus did **not** produce — the `.sc` of `C-0157`'s oxDNA run — and whatever it reports recorded, whether or not it is flattering |

### Units and conventions

Unchanged. **No committed number may move**; `P3` is how that is checked.

---

## 2. Plan

Additive if `P2` holds. The work is a constructor and a mapping, not a model change.

`P4` is the one that turns this from plumbing into a result: it is the first time this repository grades a
design it did not itself parameterise, which is the capability the tooling survey says nothing in the field has.

### Cost

Depends almost entirely on how much of the lattice `OrigamiGrillage` reads implicitly.
`CLAUDE.md` already records the answer for one direction — it takes **exactly five scalars** from its sheet and
**never reads `layers` or `interlayerCoupling`** — so the surface is smaller than it looks, and that note is the
cheap bound to re-read before starting.

### What would falsify this approach

- **`P3` does not reproduce.** Then the new path is not the same object; find out which of the five scalars
  moved before adopting anything.
- **The imported design cannot be expressed.** Then the grillage's lattice assumptions are narrower than the
  designs the field draws, which is worth stating explicitly — it is the same class of finding as
  *`OrigamiGrillage`'s interfaces are a path graph and a honeycomb block's are not*.

---

## 3. Execute

`structure/DesignedGrillage.kt` (new) carries three types and one extension:

- **`DuplexMechanics`** — the elasticity, which is the part **no design file carries**. A `.sc`
  states a lattice, a cross-section and a routing; a persistence length is a measurement.
  `gen1()` is `Gen1Tile`'s cited constants, unchanged.
- **`SheetCrossSection`** — duplexes, `d`, layers. `layers > 1` is **refused** here rather than
  ignored in `OrigamiGrillage`, which never reads it.
- **`GrillageSpecification`** — lattice, cross-section, mechanics, footprint, column layout,
  absent sites, provenance. Its `init` refuses a lattice whose `singleLayerInterfacesFormAPath` is
  false (`C-0154`), and `reasonToRefuse` is `environment/Regime`'s discipline one layer down:
  a **reason**, not a boolean, in the order this corpus has been bitten in — lattice, cross-section,
  footprint, columns.
- **`ScadnanoDesign.grillageImport`** — the design *is* the constructor. Duplexes from the helix
  count, footprint from the **axial window of every strand**, columns from the **staple** crossings'
  offsets, parities from the lower helices those crossings join, `d` and the rise from the file's
  own geometry block. The parity lattice's surplus over the file is carried as `T-110`'s
  `consumedCrossovers`, which is what makes the import **exact** rather than approximate.

`tile/DesignedHoneycombGrillage.kt` (new) does the same for `HoneycombGrillage`, reading the
cross-section out of the helices' own **grid positions** — the one place a scadnano file states it.

`lattice/CrossoverLattice.kt` gains one declared property, `singleLayerInterfacesFormAPath`, beside
`hasCentroSymmetricPhaseCongruence`. `design/ScadnanoDesign.kt` gains four accessors —
`axialWindowBasePairs`, `axialSpanBasePairs`, `risePerBasePairOrNull`, `interhelicalDistanceNm`.
Both edits are **additive**; no existing method is touched.

`tools/scadnano/emit-reference-rectangle.py` emits `P4`'s subject with the **reference**
implementation's own generator, and `--check` asserts the committed artifact is still what it
emits.

## 4. Verify

| gate | how | outcome |
|---|---|---|
| 1 dimensional | the crossover spacing is `azimuths × step × rise`, asserted equal to `Gen1Tile`'s constant on both lattices; the specification's `OrigamiSheet` is asserted **equal** to `origamiSheet`'s | pass |
| 2 limiting | a honeycomb specification is refused; a honeycomb design is refused by the sheet importer and accepted by the block importer; a square design is refused by the block importer | pass |
| 3 symmetry / conservation | the imported grillage's crossover **site set** equals the design's own `(lowerHelix, offset)` set, not merely its count | pass |
| 4 numerical | load vectors compared with `==`, solved fields at `1e−10`, per `CLAUDE.md`; the load case is `T-10`'s edge taper and **not** a uniform pressure | pass |
| 5 cross-check | `T-10`'s committed edge-taper peak dishing, **read out of its result file**, reproduced at `2.1e−9` — that file's own emission precision | pass |

23 tests, written first and watched fail, in `structure/DesignedGrillageTest.kt` (20) and
`tile/DesignedHoneycombGrillageTest.kt` (3). Whole Kotlin suite: **3 115 tests in 177 classes, 0
failures**. Six mutations of the new sources fail **1, 1, 5, 1, 3 and 1** named tests — and the
sixth failed **zero** until the gap it exposed was closed with a constructed test, which is
`C-0161` §9.

## 5. File

Claim [`C-0161`](../claims/C-0161-mechanics-on-an-imported-design.md);
challenge [`CH-0209`](../challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md);
result [`gpd/results/T-267-mechanics-on-imported-design.json`](../results/T-267-mechanics-on-imported-design.json).

All four predicates met; none of the five declared falsifiers fired. **`F4` not firing is the
task's most valuable outcome** — the reference implementation's own Rothemund rectangle is
representable **exactly**, and what it exposed instead is that its column parities do not
alternate (a seam), that it draws every crossover as **two** strand crossings, and that
`rowBasePairs` is not a span.

### What this task deliberately did not do

**Re-run anything.** No placement, phase, plan-ceiling or flatness study has been re-run through
the imported path. This task supplies the constructor those re-runs need; performing them is
separate and larger, and doing it inside a task whose `P2` is *"no committed number moves"* would
have been incoherent.
