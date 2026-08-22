# C-0181 — **A REGIME DESCRIBES A SOLVE AND A RESULT FILE IS A BAG OF SOLVES, SO THE BLOCK'S DEFECT IS ITS ARITY AND NOT ITS FIELDS** — `emission.regime` was `null` on **135 of 135** headed result files, and **17 of the 22** studies naming `MagnesiumChlorideBuffer` sweep the molarity. The repair is a `RegimeSet` and **three** emitted values where there was one: JSON `null` = *not stated* (a residue to be counted), `[]` = *no environment coordinate at all* (a claim), `[…]` = the states solved — with `Regime.neutralLayer`'s documented `null` **buffer** surviving as a third, distinct fact inside a stated member. Widening `Regime.bufferMillimolar` to a set is **falsified by the corpus**: `TallGapDeviceBStudy` solves `{0.5, 1.0, 2.0} mM` over its tall heights and `{0.5, 2.0} mM` over its fold heights, so one widened regime would admit `1.0 mM` at a fold height that no record carries. **And the consumer-side answer is honest rather than flattering: a file-granular gate is addressable on 71 of the corpus's 170 study read edges, 69 of them onto a swept file and 50 onto `T-3b` alone — and it CANNOT refuse the one mistake `CLAUDE.md` has actually recorded**, a consumer picking the wrong record inside a file whose set contains its state

| | |
|---|---|
| **Task** | [`T-286`](../tasks/T-286-a-regime-is-a-set.md) — the design question inside step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md), raised by [`CH-0224`](../challenges/CH-0224-a-regime-cannot-name-a-swept-buffer.md) |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (a design claim about this repository's schema) **+ in-silico** (the census is derived from the committed tree at a named ref, and the demonstration is three studies re-run and diffed) |
| **Verdict** | **PASS on `F1`, `F2`, `F3`, `F4`, `F5`, `F6`.** Two of the task's three declared falsifiers **fired in part and neither reversed the design**: `CH-0224`'s §5 falsifier is **upheld for five studies** — `GoldElectrodePzc`, `TwoSidedCoupling`, `ZeroBiasRestingPosition`, `PlanarCouplingWall` and `EdgeWidthDependence` fix one molarity, all at 2.0 mM, and each is a **row** rather than a type — and the third falsifier (*"`null` is already unambiguous"*) is refuted, because a stated regime with a `null` buffer is a value the corpus's own `Regime.neutralLayer` documents and `T-1` now emits |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** This claim is about **software structure and provenance**, not about the device. **No physics is asserted, no object is measured, and no physical number moves** |
| **Provenance** | New source [`environment/RegimeSet.kt`](../../src/main/kotlin/environment/RegimeSet.kt) (`RegimeSet`, `RegimeVerdict`, `RegimeReading`, `RegimeSet?.readFor`); [`structure/ResultEmission.kt`](../../src/main/kotlin/structure/ResultEmission.kt)'s `withEmissionHeader` retyped from `Regime?` to `RegimeSet?` with a non-null `Regime` convenience overload; the Python mirror [`tools/emission_header.py`](../../tools/emission_header.py) given the same shape and a refusal, with 4 new self-tests; the emitter [`tools/T-286-emit-result.py`](../../tools/T-286-emit-result.py), **14 self-tests**. **Tests first, watched to fail** on `Unresolved reference 'RegimeSet'` before the type existed: [`environment/RegimeSetTest.kt`](../../src/test/kotlin/environment/RegimeSetTest.kt), **12 new**, and [`structure/ResultEmissionTest.kt`](../../src/test/kotlin/structure/ResultEmissionTest.kt) **11 → 14**. Three studies adopt the declaration and are re-run: `brush/BrushStiffnessStudy`, `electrostatics/MeanFieldValidityStudy`, `anchoring/TwoSidedCouplingStudy`. Result [`gpd/results/T-286-a-regime-is-a-set.json`](../results/T-286-a-regime-is-a-set.json), taken at `baselineRef = 9620d3ef3f21aa4038055a5752cd637f49e62954`. **Suite: 3 315 tests in 191 classes, 1 failure, 0 errors, and the failure is a concurrent agent's** — `ResultInputsTest > every result path still spelled in a main source has a handle` names `T-279-tied-honeycomb-regrade.json`, a sibling's **untracked** result file whose typed handle its own commit will add; removing that sibling's three in-flight files from the snapshot makes the same test pass. `testQueueVocabularyMutations` was **excluded**: it is red at `HEAD` on its own (`6 mutation(s), 6 survivor(s)`, verified in a `git archive HEAD` tree) because a sibling is mid-edit in `tools/check-queue-vocabulary.py`. Neither is touched here |
| **Conditions** | 300 K; `k_BT = 4.142 pN·nm`; molarities mM, lengths nm, bias V, band Hz. A **read edge** is one `(study, result file)` pair of [`tools/result-reader-census.py`](../../tools/result-reader-census.py), derived at `baselineRef` and never from the working tree. An **electrolyte study** is one whose source names `MagnesiumChlorideBuffer` |
| **Consumes** | [`C-0159`](C-0159-environment-interface.md) / `T-265` (`Regime`, whose refusal predicate is **not** touched here), [`C-0172`](C-0172-typed-handles-and-the-emission-header.md) / `T-272` (the emission header and its census), [`C-0162`](C-0162-round-outputs-never-inputs.md) (*round outputs, never inputs* — which is why a regime bound is emitted unrounded), [`C-0174`](C-0174-emission-header-residue-and-the-seven-unrounded-emitters.md) (the Python-emitter precedent and the `baselineRef` obligation), [`CH-0224`](../challenges/CH-0224-a-regime-cannot-name-a-swept-buffer.md) |
| **Constrains** | [`T-272`](../tasks/T-272-emission-layer-remainder.md)'s `P4`: the type it has to put on 132 call sites now exists and has three values, so the sweep is a **declaration** pass rather than a design pass. It also **prices** `P4` honestly — the sweep buys a gate over at most **71 of 170** study read edges, and buys nothing at all against the per-record defect |
| **Raises** | Nothing new is filed. `CH-0224` moves to **RESOLVED — the arity is repaired; the per-record residue is named and is `T-272`'s.** One measured corpus fact is reported without a challenge in §7 (`gpd/results/T-199-cross-section-comparison.json` does not reproduce from `HEAD`'s own code in one field's ninth digit, a mechanism `CLAUDE.md` already records) |

---

## 1. What `CH-0224` said, re-derived

The challenge's headline is a count, so it is checkable.
At `baselineRef` the emission header is carried by **135 of 159** committed result files,
and its `regime` is `null` on **135 of 135** of them and stated on **0**
(`committedCorpus` in the result file).
The key exists everywhere and refuses nothing anywhere — exactly as filed.

The source side is what makes that inevitable rather than accidental.
Of the **22** studies naming `MagnesiumChlorideBuffer`,
**17 sweep** the molarity and **5 fix one**:

| | studies |
|---|---|
| **sweeps** (17) | `CollarEquilibriumPath`, `MaximumUsableBias`, `StrokeAndBlockingForce`, `TallGapDeviceB`, `OutputCoupling`, `ConcentratedCrossover`, `BeyondMeanFieldGap`, `CutRimCharge`, `MeanFieldValidity`, `NonlinearPbProfile`, `ScaffoldRemainder`, `TileEdgeLoadProfile`, `LargeRotationArmBranch`, `RecommendedElementFold`, `SofteningCouplingStability`, `DrawableRaggedFace`, `RaggedFaceCost` |
| **fixes one, all at 2.0 mM** (5) | `GoldElectrodePzc`, `TwoSidedCoupling`, `ZeroBiasRestingPosition`, `PlanarCouplingWall`, `EdgeWidthDependence` |

`CH-0224`'s §2 table lists 14 and its §5 names seven studies as unclassified.
Read, **two** of those seven sweep (`RaggedFaceCost`, `CollarEquilibriumPath`) and **five** fix
one — `EdgeWidthDependence` selects `concentration == 2.0` out of `T-3b` — and a twenty-second
study the challenge lists nowhere, `structure/DrawableRaggedFaceStudy`, sweeps `{0.5, 1.0, 2.0}`.
**So the challenge's own falsifier fires in part** — five single-buffer studies exist —
and it does not reverse anything: five rows against seventeen.

### The classification is read, not regexed, and that is a measured trap

A scan for `listOf(0.5, 2.0, …)` inside an electrolyte study infers a **role** from a **type**.
`anchoring/GoldElectrodePzcStudy` carries `listOf(2.0, 10.0)`
and those are `C-0021`'s two readings of §3's tile **thickness in nm**, not molarities.
So the table in `tools/T-286-emit-result.py` records an **evidence line** per study,
and its *membership* is asserted against the tree at `baselineRef` —
a study that starts or stops naming the buffer fails the emitter
rather than ageing quietly out of the census.

## 2. The design decision, and why it is the arity

**A `Regime` describes a solve. A result file is a bag of solves.**

The asymmetry already inside `Regime` is the argument, and it is not an oversight.
`Environment.pressure(heightNm)` and `force(heightNm, biasVolts)` take the gap and the bias as
**arguments**, so a solve is a function over them and an **interval** with containment is the
honest statement — which is exactly what `Regime` already carries for both, and it carries the
band as a value with `null` meaning *quasi-static*.
The molarity is a **constructor** argument: `MagnesiumChlorideBuffer(c)` is built before anything
is solved, and it identifies **which** environment.

A study that sweeps the buffer therefore does not widen one regime.
It instantiates **several environments**, each keeping its own correct interval.
So the honest object at file granularity is a **set of regimes**, and the block's defect is its
**arity**, not its fields.

### `CH-0224`'s repair 2 is falsified by the corpus, at the cost of one `grep`

`actuator/TallGapDeviceBStudy` declares
`TALL_GAP_REACH_BUFFERS = listOf(0.5, 1.0, 2.0)` over `TALL_GAP_HEIGHTS = listOf(17.0, …, 26.0)`
and `TALL_GAP_BUFFERS = listOf(0.5, 2.0)` over its fold heights.
A single `Regime` with a set-valued buffer would have to carry the **union** of both height ranges,
and would then admit `1.0 mM` at a fold height — a pair no record of that file was solved at.
A set of two regimes does not.
This is asserted, on those numbers, in `RegimeSetTest`'s
*"a set separates two sub-sweeps that a widened single regime would merge"*.

Repair 3 (a `RegimeFamily` beside `Regime`) is what `RegimeSet` is, minus the second vocabulary:
it holds `Regime`s and adds no field of its own.
Repair 1 (a per-record regime) is not withdrawn — it is **necessary and it is `T-272`'s**, see §5.

## 3. `F2` — three values where there was one

`CLAUDE.md`: *a `null` that means "no requirement" and a `null` that means "not stated" are
different values.* There are **three** facts here and the emitted JSON now separates all three:

| emitted | meaning | who says it |
|---|---|---|
| `"regime": null` | **the study has not stated** what it was solved at — a residue to be **counted**, read as `RegimeVerdict.NOT_STATED`, never a silent admission | all 132 existing call sites, unchanged |
| `"regime": []` | **no environment coordinate enters this result at all** — a lattice census, a junction closure search, a plan packing. A claim, and it admits every consumer | `RegimeSet.noEnvironment`; `T-286`'s own file is the corpus's only one, **1 of 163** in the working tree |
| `"regime": [ … ]` | the states solved, one member per environment | `T-1`, `T-6`, `T-23` |

And the physical claim `Regime`'s KDoc already documents survives as a **fourth** distinguishable
fact **inside** the third: a stated member whose `bufferMillimolar` is `null`, which is *"ideal
mobile salt cancels out of a neutral grafted layer exactly"*.
That is **not** the empty set: a layer solved in an electrolyte-free environment is a different
statement from a result that is a function of no environment coordinate, and the first refuses an
electrolyte consumer where the second admits it.

`RegimeSet?.readFor(consumer)` is an extension on a **nullable** receiver precisely so a gate
cannot fail to see the difference — which is the reading `CH-0224` measured across all 135 files.

**A note on the working-tree column, which is `CH-0182` in miniature.** `T-286`'s own file carries
`"regime": []` and its census walks `gpd/results/` **before** it writes itself, so run 1 reads
`regimeEmptySet = 0` and run 2 reads `1`. Run 3 is byte-identical to run 2: the census is a
**fixed point** after one iteration, and the committed file is the second run. A census over a
corpus that contains the census is not repeatable in one pass, and saying which pass it is, is
part of the result.

## 4. `F3`/`F4` — the demonstration, and what moved

The signature was declared before the runs: **`regime: null` → an array, and nothing else.**
Measured by `diff` against `git show HEAD:<path>`, each file moves in exactly **one hunk**:

| study | file | value emitted | hunks moved |
|---|---|---|---|
| `brush/BrushStiffnessStudy` | `T-1-layer-stiffness.json` | one member, `bufferMillimolar: null` — the neutral-layer claim, over `[0.38478379807447505, 10.0]` nm derived from the study's own most-compressed wall position | **1** |
| `electrostatics/MeanFieldValidityStudy` | `T-6-mean-field-screening-validity.json` | **three** members, `{2.0, 5.0, 10.0}` mM over `[5, 10]` nm at zero bias | **1** |
| `anchoring/TwoSidedCouplingStudy` | `T-23-two-sided-coupling.json` | one member, 2.0 mM over `[3, 10]` nm, bias `[0, 1]` V, band **1000 Hz** | **1** |

All three reproduce `HEAD` **byte for byte** before the change, and all three have **zero** study
read edges and **zero** test read edges, which is why they were chosen: the demonstration cannot
propagate.

`T-1`'s `lowestHeightNm` is emitted as `0.38478379807447505` — **unrounded**, because
`EMISSION_KEY` is a parameter record. That is `C-0162`'s *round outputs, never inputs* working on
the block, visible.

### The 132 call sites that do not adopt it

They keep passing `null`, they keep serialising `null`, and **no committed byte moves**.
Asserted of the type (`an empty set and a null regime are different JSON`) and then measured:
`material/PegMaterialStudy` re-run against the changed emission layer is **byte-identical** to the
checkout.
What changes for those 132 is the **meaning** — from *"no solved range"* to *"not stated"* — and
that is a KDoc and a census, not a re-run.
The control is byte-identical to **`HEAD`** as well as to the checkout, so it is a statement about
the committed file and not about a staged one.
Closing the residue is `T-272`'s sweep and is priced there at ≥ 7 h.

## 5. `F5` — what a gate can reach, honestly

A gate's value is a property of its **consumers**, and this is the measurement nobody had taken.

| | |
|---|---|
| study read edges in the corpus | **170** |
| landing on an electrolyte study's result file | **71** |
| …of which land on a file whose buffer is a **set** | **69** |
| …of which land on a single-state file | **2** |
| `T-3b-tile-edge-load-profile.json` alone | **50** |

So **97 % of the addressable read edges land on a file the old block could not describe**, which
is the strongest form of `CH-0224`'s point: the coordinate the block could not express is the
coordinate almost every gateable read is in.

**And the honest half.** A file-granular set is a **necessary** condition and not a sufficient one.
It refuses a consumer that asks a file for a state no record of it carries.
It **cannot** refuse a consumer that picks the **wrong record** inside a file whose set contains
the state — and that is not hypothetical, it is the defect `CLAUDE.md` already records against the
busiest file in this corpus:

> An upstream result file may hold more than one record per state. `gpd/results/T-3b-*.json`
> carries two solved profiles per `(concentration, gap)` — one per operating bias — so
> `firstOrNull { c && h }` silently takes whichever is listed first.

**Fifty read edges land on that file and a file-level regime gate would admit every one of them.**
Only a regime on the **record** closes it, and that is `CH-0224`'s repair 1: `T-272`'s sweep with a
wider edit. This claim does not do it and does not pretend to.

So the plain answer to *"can this ever be a gate"* is:
**yes, over 71 of 170 read edges, and it would refuse none of them today** — every consumer in the
corpus selects a state its file carries. What the block buys is a **future** refusal and a
**machine-readable statement of what a file was solved at**; what it does not buy is protection
against the mistake that has actually been made here.

## 6. `F6` — the key census, before the emitter

No new schema key is introduced. `emission.regime` keeps its name; only its **shape** changes,
from `object | null` to `array | null`, and **no committed file carries an object there** —
0 of 135 — so the widening can move nothing that exists.
The record keys the new study emits were censused over all committed result files before the
emitter was written: `electrolyteStudies` 0, `consumerSide` 0, `demonstration` 0,
`regimeCensus` 0. The `RegimeReading` leaf names `verdict` (3 595 existing occurrences) and
`reason` (281) are established corpus leaf names and are used nested, not at top level.

## 7. What this task measured and is reporting without a challenge

`gpd/results/T-199-cross-section-comparison.json` **does not reproduce from `HEAD`'s own code**.
Its committed `crossSections[…].freeTileDishingOverStroke` is `2.6240121E-5`;
two runs under this task's sources and one `--committed` control run at `HEAD`'s code and `HEAD`'s
inputs all give `2.62401216E-5` — the same value, three times, from two different source trees.
It is one field, in its ninth significant digit, and it is the **nine-digit rounding tie**
`CLAUDE.md` already records for `T-14`: a value within half an ulp of the rounding boundary flips
when last-ulp JIT noise moves it.
**No verdict moves and no claim quotes it.**
It is not filed as a challenge because the mechanism is documented, the file is not this task's,
and a sibling agent is working in `tile/`; it is recorded here so the next re-emission sweep has
it by name.

## 8. Validity range

- **This is a design claim about this repository.** It is not evidence about the device. No number
  in §1–§6 is physical and none of the physics numbers in the three re-emitted files moved.
- **`Regime.reasonToRefuse` is untouched.** It compares buffers by **equality**, so a
  neutral-layer source is refused against an electrolyte consumer. That is `C-0159`'s claim and
  this task deliberately does not relitigate it; whether a layer pressure and a gap force are
  *consumed* in one another's regime, or merely **summed** in a coupled model, is a separate
  question and it is left open.
- **The demonstration is three studies of 22.** The other nineteen electrolyte studies, and every
  one of the ~110 studies with no environment coordinate, still emit `null` — which under this
  claim's own vocabulary now reads *not stated*, correctly, and is countable by
  `tools/T-286-emit-result.py`. **Naming which studies were demonstrated on and which were not is
  part of the claim**, and the not-demonstrated set is every study except `T-1`, `T-6` and `T-23`.
- **The read-edge census is derived at one ref.** `tools/result-reader-census.py` at
  `9620d3ef` gives 170 study edges; the working tree gives 172 because two studies have landed
  since. The committed reading is the citable one and the file records both.
- **The Python mirror's self-tests are still invoked by nothing.** `tools/emission_header.py`
  gains a shape refusal and 4 self-tests here and they pass, but the file is imported by three
  emitters and wired into neither [`build.gradle.kts`](../../build.gradle.kts) nor
  [`tools/verify.sh`](../../tools/verify.sh) — which is the state it was already in. Wiring it is
  a **new gate** and would owe `C-0158`'s reading at the commit that lands it; it is named here
  rather than taken, because `build.gradle.kts` is being edited by a concurrent agent.
- **`[]` admits every consumer**, and that is a modelling choice with a stated ground: a number
  that is a function of no buffer, no gap, no bias and no band may be read in any of them. A
  study that emits `[]` while secretly depending on an environment coordinate is a *study* defect
  the schema cannot see.
