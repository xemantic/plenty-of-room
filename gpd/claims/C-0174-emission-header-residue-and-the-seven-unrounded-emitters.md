# C-0174 — **`CH-0223`'s seven emitters are repaired at a precision ARGUED rather than guessed, and the argument moves the judgement off the axis the challenge put it on: all seven are `P-18`'s nine-digit site, because NEITHER of the two studies the challenge names as "downstream of a solved SCF height" reaches that solver at all — zero occurrences of `SelfConsistentField` in five sources — while the per-study decision that IS owed is the absolute FLOOR, and exactly one of the seven needs it.** The whole change was **simulated offline over the committed corpus before a JVM started** and the prediction is exact — **62 result files re-emitted, 61 of them Kotlin studies in one 43-constraint topological order, 0 failures, 3 h 44 m** — and `CH-0223`'s own 41 369 was reproduced by that mirror, **50 of which are false positives of its flat nine-digit predicate** (`CH-0226`), 45 of them one study rounding to three digits with a reason

| | |
|---|---|
| **Task** | [`T-278`](../tasks/T-278-emission-header-residue.md) — the emission header's residue, taken with [`CH-0223`](../challenges/CH-0223-seven-emitters-call-no-rounding-function.md) |
| **Leaf** | `A8.2` — the remainder of step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Verification type** | **logical** (a call-graph reading, two censuses over the committed corpus, three tools with named self-tests) **+ in-silico** (studies re-emitted through one snapshot in one topological order, movement classified by kind against `git show HEAD:`) |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** No model, solver, mesh, tolerance or convergence parameter was touched. Every field this task moves is a **precision** or a **schema** field |
| **Verdict** | **PASS.** `tools/T-272-header-census.py` reads **`BOTH 130`, `DECLARED-NOT-EMITTED 0`, `EMITTED-NOT-DECLARED 0`, `NEITHER 0`** where it read 72 / 55 / 0 / 1. All seven of `CH-0223`'s emitters round, at a precision argued per study, and `tools/T-278-emitter-rounding-census.py` reads **0 of 130** emitting studies writing through no rounding function. The sweep's signature is the one declared before it ran — `added` **114 = 2 × 57**, `wording` **0**, `boolean` **0**, `removed` **0** — and `numeric` is **41 319**, of which **41 297 were predicted offline field for field** and 22 are the rounding repair propagating along one live reader edge. The four movers outside that signature are each controlled against `HEAD` (§4) and **none is this sweep's defect**; one of them is a reproduction residual **closing to exactly `0.0`**. `A5` answered with a measurement: **2 of 24** Python emitters reached, 22 refused with a stated reason. `A6`: **one** claim quotes a moved number and it is amended |
| **Conditions** | Units unchanged and locked; 300 K; `k_BT = 4.142 pN·nm`. Nothing physical is computed here |
| **Consumes** | [`C-0172`](C-0172-typed-handles-and-the-emission-header.md) (the header, the residue named by tool, the two-graph finding), [`CH-0223`](../challenges/CH-0223-seven-emitters-call-no-rounding-function.md) (the seven and their count), [`P-18`](../tasks/P-18.md)'s provenance rule and its `roundingSites` table, [`C-0162`](C-0162-round-outputs-never-inputs.md) (round outputs never inputs; the `parameter` movement kind), [`C-0138`](C-0138-departure-rule-scope.md)/[`C-0153`](C-0153-unrounded-prose-interpolations.md) (simulate a rounding change offline before running it), [`C-0117`](C-0117-reemission-order.md)/`CH-0131` (a sweep is a topological sort), [`C-0129`](C-0129-result-file-hygiene.md)/[`C-0131`](C-0131-departure-and-saturation-audits.md) (the `--committed` control), [`C-0092`](C-0092-large-rotation-arm-branch.md) (*a repair must leave the defect measurable*), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*) |
| **Raises** | [`CH-0225`](../challenges/CH-0225-the-scf-hazard-does-not-reach-the-two-studies-it-names.md), [`CH-0226`](../challenges/CH-0226-a-flat-nine-digit-corpus-predicate-has-false-positives.md) |

---

## 1. The judgement `CH-0223` declined, made — and it is not on the axis the challenge put it on

`CH-0223` refuses its own repair on the ground that *"the digit count is a **judgement per
study**"*, and offers one: *"`T-1c` and `T-1` are downstream of a solved SCF height and are
determined to `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` or fewer."*

That is a claim about a **call graph**, so it was checked before it was obeyed. At `b853b85`, the
commit the challenge was filed on, `SelfConsistentField` and `heightAtPressure` occur **zero**
times in `brush/BrushStiffnessStudy.kt`, `brush/CrossoverLayerStudy.kt`, `brush/LayerDesignPoint.kt`,
`brush/BrushCompression.kt` and `brush/PolymerBrush.kt` — every source on either study's path.

| study | what it actually solves with | loosest tolerance | digits |
|---|---|---|---|
| `T-1` | `DeGennesScaling(9/4)`, `DeGennesScaling(3)`, `MilnerWittenCates` — closed-form equilibria; one iteration, `heightUnderLoad`, **100 bisection halvings** of a bracket `[L₀·1e−12, L₀]` | machine precision | **9** |
| `T-1c` | `AlexanderBoxLayer`, `StrongStretchingLayer` — `bracketedRoot` at its default `1e-15`, `solveLambda` at `CONVERGENCE = 1e-15`, `InteractionFreeEnergy` at the same | `1e-15` | **9** |
| `T-6` | closed forms in `electrostatics/ChargedSurface.kt`; two boundary searches, **300 geometric halvings** over eleven decades | machine precision | **9** |
| `T-7` | Brinkman transmissivity and closed-form drags; one search, the 1 kHz contour, **200 halvings** | machine precision | **9** |
| `P-3` | arithmetic on measured constants; `heightUnderLoad` and a 200-halving des Cloizeaux reach | machine precision | **9** |
| `P-6` | **no solver at all** — the provenance closure over its source finds zero named convergence criteria | — | **9** |
| `P-9` | **no solver at all** — Alexander-de Gennes compression fits inverted in closed form | — | **9** |

`P-18`'s own conventions block states the rule — *"PROVENANCE of an emitted number is the loosest
solver tolerance on any path from a model input to it. Nine digits is defensible only where that is
≤ 1e−9"* — and its `roundingSites` table's first row is exactly where these seven belong: *"analytic
models and closed-form geometry, looseTolerance 1e−15, determinedDigits 9, overPrintedBy 0."*

**So the digit count is the same for all seven, and the per-study judgement is real and lives on
the other axis.** `RESULT_ABSOLUTE_FLOOR = 1e-9` is documented as a magnitude **in the locked
units** — *"no force below a nanopiconewton is of interest"* — and `P-18`'s companion rule is that
*an absolute floor is a claim about units, and it does not travel*. Simulated over the committed
files, the default floor reaches **370 of the 41 297 fields**, in two studies and for opposite
reasons:

| | fields | verdict |
|---|---|---|
| `T-1c` `equilibriumStiffness`, `1e−13` to `1e−16 pN/nm` | **274** | **keep the default.** `CLAUDE.md`: the Milner-Witten-Cates form has *exactly zero* stiffness at `L₀`, "because the brush's outer edge is diffuse". The floor states the physics, in the locked units — `RESULT_ABSOLUTE_FLOOR`'s own documented case (`T-5`'s zero internal shear) |
| `T-7` `inertialTime`, smallest `6.96645e−14` **seconds** | **96** | **lower it to zero** (`POROELASTIC_RESULT_FLOOR`, declared with its reason). Seconds are not piconewtons; the same study's `verticalDrainageTime` clears `1e−9` by half a unit in the first digit at `1.53e−09 s`, and the ratio of the two **is** its own overdamping verdict |

One of the seven needed a decision, and it was not a digit count. That is `CH-0225`.

## 2. The cheap bound ran first, offline, and it is exact

A rounding change is a **pure function of the committed document**, so its blast radius is
derivable rather than discoverable. `tools/T-278-rounding-simulation.py` mirrors
`structure/ResultRounding.kt` to the branch — the parameter-record exemption, the `record/spelling`
departure map, the integral-number rendering, the absolute floor — and predicts every moved leaf in
seconds.

Two things make the mirror credible rather than merely plausible. Its self-tests are taken from
`ResultRounding.kt`'s own KDoc examples; and one of them **reproduces `CH-0223`'s `P-9` count of 70
against the committed file**. Over `git archive HEAD` the whole corpus reads **41 369 in 17 files**
and the seven's share **41 297 / 99.83 %**, to the leaf — the challenge's own numbers, derived
independently.

**And reproducing them is what exposed two defects in the predicate itself, one of them in this
mirror.** A mutation test over the mirror's own classifications (`tools/T-278-mutation-test.py`,
**8 mutations, 0 survivors**) found that `roundToLong` rounds ties towards **positive infinity** —
`−2.5 → −2`, neither half-even nor half-away-from-zero — and a `T-190` reading found that Kotlin's
`10.0.pow(23)` and Python's `10.0 ** 23` differ by **one unit in the last place**, which is enough
to decide the verdict at `1e−15`. Corrected, the corpus figure is **41 361**, and read at each
study's own declared precision **41 312 in 12 files**. The seven's **41 297 is invariant under
every one of these readings** — which is why `CH-0223`'s headline stands and its denominator does
not (`CH-0226`).

It also settles a thing the sweep could not: **none of the seven carries a `reproductions` or
`convergence` record**, so `DEPARTURE_DIGITS_BY_KEY`, which `roundedForResult` applies as a
baseline, is provably inert for all seven. That is a proof rather than a re-run.

## 3. The order was derived fresh and checked against the committed census

`C-0172` §2 found that `tools/reemission-order.py` sorts from the **committed**
`P-22-result-reader-census.json`, which was then 16 edges stale, and that the two orders *"diverge
from position 50 of 151"*. It re-emitted `P-22` in the same commit; whether that closed is a
question nobody had asked.

Asked here: over this task's 61-file set the committed census and a census derived fresh from the
tree give **43 dependency constraints each, the same 43 pairs, and an identical order** — checked
pair by pair and position by position, not merely by count. `A1`'s *"assert the constraint count
non-zero"* is met at **43**, and `C-0172`'s *"necessary and not sufficient"* is met by the
comparison rather than by trust.

## 4. The sweep

### The signature, declared before it ran

`F1`, in the task file: **a header-only file moves `added = 2` and nothing else; a rounded file
moves `added = 2` plus exactly the `numeric` fields the offline simulation named.** Anything else —
a `prose`, `wording`, `boolean`, `departure`, `parameter` or `removed` movement, or a `numeric`
movement not on the predicted list — is either a defect of this change or a pre-existing
irreproducibility, and must be controlled against `HEAD` before it is called either.

### What it moved

| kind | count | |
|---|---|---|
| `added` | **114** | `emission/lattice` and `emission/regime`, `2 × 57` — the 56 residue studies that gained a header plus `T-198`. The five of `CH-0223`'s seven that already carried one add nothing |
| `numeric` | **41 319** | **41 297** are the seven, predicted offline before any JVM started and matched field for field (below); **22** are `T-121`, and they are not noise — see §4a |
| `parameter` | **4** | 3 in `T-121` (the same propagation) and 1 in `T-125` — §4a |
| `prose` | **1** | one token of one `findings` sentence in `T-123` — §4a |
| `departure` | **2** | `T-188`'s two `reproductions[*].departure`, `2.4e−09 → 0.0` — a residual **closing**, §4a |
| `wording`, `boolean`, `removed` | **0** | |

**61 studies, 0 failures, 3 h 44 m** measured from the batch log — against `C-0162`'s 1 h 50 m for
46, on a box shared with two other agents throughout. `T-155` alone is 32.8 min and `T-127` 21.6, and **31 of the 61 had no recorded run time before this** (the longest, `T-3` at 11.7 min, `T-188` at 10.8 and `T-165` at 10.7).


### 4a. The four movers outside the signature, each controlled

`A2` and `F1` require that anything beyond the declared signature be controlled against `HEAD`
before it is called staleness. All four are, and they are four different things:

| file | movement | control | verdict |
|---|---|---|---|
| `T-121-stacked-arm-sheet.json` | `numeric = 22`, `parameter = 3`, all in the **ninth** digit of `cornerFrequency`, `inBandFraction`, `inBandRms` | `structure/StackedArmSheetStudy` reads `ResultInputs.T_7.file()`, and **240** of the fields it reads out of `T-7` moved in this sweep | **PROPAGATION, not noise.** The rounding repair travelling one live reader edge, with the topological order putting `T-7` at position 25 and `T-121` at 41 — `C-0162`'s `T-136 → T-138` edge doing exactly what it was built to do |
| `T-125-upward-root-placement.json` | `parameter = 1`, the **16th** digit of `parameters/optimisedDishingOverStroke` | two runs at `HEAD`'s own code in a `git archive HEAD` tree: **run A and run B disagree with each other** on `parameters/bestReachableFloor`, and both disagree with the committed file on three leaves | **PRE-EXISTING.** Three unrounded `parameters` leaves carry this study's own **search outputs**, so the input exemption is protecting the one field that most needs rounding — `C-0172`'s `T-129`/`T-138` case, third instance |
| `T-123-robust-distribution.json` | `prose = 1`: *"going from 16 starts to 42 moves the answer by `1.1e−15`"* → `6.7e−16` | one run at `HEAD`: the committed file **does not reproduce from its own code** — 15 fields of `subsets/17/*` differ, and this sweep landed back on the committed values for all 15 | **`C-0135`'s DESCENT MANIFOLD**, and the only thing this sweep moved is a **search-path diagnostic in prose**, which `CLAUDE.md` says should not be emitted at all |
| `T-188-buildable-width-count-phase.json` | `departure = 2`, `2.4e−09 → 0.0` | none needed — the direction is the diagnosis | **A RESIDUAL CLOSING.** `CLAUDE.md`: *"a reproduction residual is a staleness detector"*; `T-188` reproduces `C-0090`'s free-tile stroke `5.15473846 nm` at **exactly zero** now, where it carried `2.4e−09` of its input's staleness before |

`T-253` and `T-263` are quoted apart because a sibling agent was rewriting
`tile/HoneycombGrillage.kt`'s prestrain assembly while this sweep ran. This snapshot was taken
**before** that edit and its copy of the file is **byte-identical to `HEAD`** (one `diff`), so both
studies were graded on the committed model; both moved `added = 2` and nothing else, and the
sibling's own re-run reports `T-267` byte-identical and no numeric field of any of the three moving.

### `F2` — the offline prediction, checked field by field

The strong form, and it is met at every one of the seven:

| file | predicted | actual | missing | unpredicted |
|---|---|---|---|---|
| `T-1c-crossover-valid-layer-response.json` | 25 774 | **25 774** | 0 | 0 |
| `T-7-poroelastic-drainage.json` | 7 914 | **7 914** | 0 | 0 |
| `T-1-layer-stiffness.json` | 7 049 | **7 049** | 0 | 0 |
| `T-6-mean-field-screening-validity.json` | 330 | **330** | 0 | 0 |
| `P-6-solvent-quality-vs-salt.json` | 81 | **81** | 0 | 0 |
| `P-3-peg-material-parameters.json` | 79 | **79** | 0 | 0 |
| `P-9-grafted-chi.json` | 70 | **70** | 0 | 0 |
| | **41 297** | **41 297** | **0** | **0** |

So the re-run is a **confirmation** and not a discovery, which is what `A3` asked for. Two
consequences are worth naming because neither could have been asserted without it:

- **`parameter = 0` over the whole sweep.** `T-1` carries 13 `Double` leaves in its `parameters`
  block and `P-3` carries 18, and adding a rounding call to a study that never had one is the first
  time `C-0162`'s exemption has had to protect an input in these seven. It held: not one parameter
  leaf moved.
- **The floor decision is visible in the artifact.** `T-1c` now emits **388 of its 1 098**
  `equilibriumStiffness` values as exactly `0.0` (274 newly, 114 already), every one a
  strong-stretching layer at its own resting height; and **all 96** of `T-7`'s `inertialTime`
  values survive, the smallest at `6.96645e−14 s`, where the default floor would have flattened
  every one of them.

### `A6` — the claims that quote a moved number

A scan of every `.md` in `gpd/` plus the six root documents for numeric tokens of ten or more
decimal places, intersected with the 41 297 moved values, returns **exactly two**:

| where | value | |
|---|---|---|
| [`C-0143`](C-0143-planar-coupling-wall.md)'s provenance table | `13.517697558570946` → **`13.5176976`** | **AMENDED here.** The claim's own §2 has printed the nine-digit form since it was written, and `T-221`'s result file has carried it since it was emitted, so the seventeen-digit literal was already a number **no file stated** — `C-0150`'s *"one quantity at two precisions"*, resolved rather than created by this sweep. `T-221` carries the literal in its **own source**, so its reproduction to `0.0` is untouched |
| `JOURNAL.md` | `7.000000000000001` → `7.0` | a 7 nm layer height printed with float noise, in the history rather than in a claim. Reported to the coordinator rather than edited |

## 5. The Python emitters, measured rather than asserted — and the measurement is DATED

`A5` asks that the 24 committed result files written by no Kotlin study be reached or refused with
a reason. Each was run at `HEAD` in a fresh `git` checkout reset to `HEAD`, with the **script's own
exit code** captured rather than a pipeline's:

| | count | |
|---|---|---|
| **reproduces its committed file byte for byte** — reachable | **2** | `T-194` (reached by `C-0172`), `T-198` (**reached here**: `tools/emission_header.py`, `added = 2` and nothing else) |
| produces a **different** file | **9** | `P-22`, `T-183`, `T-184`, `T-200`, `T-201`, `T-202`, `T-205`, `T-207`, `T-211` |
| **fails outright** | **8** | `T-208` (a real defect: `check_departures` returns three values where it unpacks two), `T-212`, `T-214`, `T-225` (usage), `T-234`, `T-249` and `T-250` (their own body assertions fire), `T-9` (needs an oxDNA run's output) |
| has **no emitter at all** | **5** | `T-119`, `T-147`, `T-175`, `T-220`, `T-226` |

`C-0172` measured the same population one iteration earlier and got **1 / 10 / 5 / 2**. Both
measurements are right, and the difference is the finding: **these emitters are functions of the
mutable corpus**, so `T-249` and `T-250` — which census the corpus and assert their own body
against it — moved from *differs* to *fails* the moment `HEAD` moved. A reproduction status for a
corpus census is dated the way `CH-0212` says a corpus census itself is. Adding a header to the
nine that differ would land their unrelated drift in the same commit; that is why they are refused
and not reached.

## 6. Provenance

| | |
|---|---|
| **result file** | [`gpd/results/T-278-emission-header-residue.json`](../results/T-278-emission-header-residue.json), emitted by `tools/T-278-emit-result.py` (17 self-tests). It names **two** corpus states rather than one — `baselineRef`, the commit `gpd/results/` stood at before the sweep, against which every *before* census and the whole movement table is taken out of `git`; and `challengeRef`, the commit `CH-0223` was filed on, whose 41 369 it reproduces. It carries the emission header this task put on everything else, is rounded through the same rule **at a floor of zero** (every number in it is a count or a dimensionless ratio, and a floor is a claim about units), and emits **no wall-clock timing and no step count** — the 61 per-study run times are in `gpd/data/T-278-study-run-times.txt`. **Two runs are byte-identical** |
| **the sweep** | one persistent snapshot at `68d9a6c` + this task's source edits, `HoneycombRasterTurnTies.kt` dropped (a sibling's three unresolved references would have failed `compileKotlin` for all 61 studies); `tools/T-278-batch.sh`, 61 studies, **0 failures**, copy-back re-checksummed immediately before each run and scoped to what that run changed — asserted afterwards: **0 files copied back that are not in the declared set** |
| **the order** | `tools/reemission-order.py` over the 61-file set: **43 dependency constraints**, asserted non-zero, and the committed census compared pair-for-pair and position-for-position against a census derived fresh from the tree — **identical** |
| **the controls** | a second `git archive HEAD` snapshot; `T-121` twice, `T-125` twice, `T-123` once |
| **tools built** | `tools/T-278-rounding-simulation.py` (23 self-tests, a mirror of `structure/ResultRounding.kt` whose baseline assertion is pinned to `b853b85`, the commit `CH-0223` was filed on), `tools/T-278-emitter-rounding-census.py` (13 self-tests, source and artifact halves, only the source half gated), `tools/T-278-solver-provenance.py` (12 self-tests), `tools/T-278-mutation-test.py` — **8 mutations, 0 survivors**, and it found a real defect in the mirror (`roundToLong` rounds ties towards **positive infinity**, so `−2.5 → −2`, which is neither half-even nor half-away-from-zero) and a second one at the scale factor (`CH-0226` §3) |
| **the test** | `src/test/kotlin/structure/EmitterPrecisionTest.kt`, written first and **watched fail**: with `HEAD`'s `poroelastic/PoroelasticDrainageStudy.kt` restored into the snapshot it does not compile — *"Unresolved reference `POROELASTIC_RESULT_FLOOR`"* — at three sites |
| **suite** | **3 289 tests in 189 classes, 0 failures**, `./gradlew test` on a snapshot of the working tree at the end of this task (21 m 28 s). An earlier run failed **one** test — `ResultInputsTest > every result path still spelled in a main source has a handle` — and it is not this task's: `tools/T-272-emit-result-inputs.py` keys the registry on **`git ls-files`**, so while a concurrent agent's two result files were untracked, any run of that emitter deleted their hand-added handles, and the failure surfaced in a test about something else. Isolated by running the suite on `HEAD`'s tree plus this task's changes alone — **3 259 tests in 187 classes, 0 failures** |
| **document gates** | `check-result-file-hygiene` (bare, `--departures`, `--saturated`, `--prose`), `check-markdown-tables`, `check-corpus-links`, `check-corpus-identifiers`, `check-challenge-index`, `check-entry-points` — **all eleven clean**, plus `check-queue-vocabulary` and `check-kotlin-format-strings` |

## 7. Validity range

- **Nothing physical is asserted.** No model, solver, mesh, tolerance or convergence parameter was
  altered. A `numeric` movement in this sweep is a **precision** change or a pre-existing
  irreproducibility, and every one is classified in §4.
- **The precision argument is a PROVENANCE argument, not a perturbation measurement.** `P-18`'s
  shape is to perturb the solve path and take `determinedDigits` of the movement; what is done here
  is `P-18`'s *other* instrument, the loosest tolerance on the path, read from the sources. It is
  the conservative one — a solver tighter than `1e-9` cannot make a quantity *less* determined —
  and it is not a measurement of the quantity's own conditioning. A study whose answer amplifies its
  solver's tolerance would need the perturbation run.
- **The solver-provenance closure over-includes and says so.** `tools/T-278-solver-provenance.py`
  enumerates candidates; it does not decide. Cut at the serialisation boundary it reads 15–19
  sources for six of the seven and **140 for `T-6`**, whose `electrostatics` package siblings pull
  in the whole tree — `CLAUDE.md`'s *"a static call graph over FILES is not a conservative
  approximation, it is noise"*, met again. The seven judgements in §1 rest on **reading the study's
  own constructed models**, with the closure as the enumeration that says nothing was missed.
- **The corpus over-precision figure is not a defect count.** `CH-0226`: read at each study's own
  declared per-key precision, and with the scale factor the Kotlin actually uses, it is **41 312 in
  12 files** against the published 41 369 in 17 — **49** leaves are a study's own declared
  precision and **8** are the predicate's own arithmetic (`CH-0226`). After this sweep the residue
  is **15 leaves in 5 files and every one is written by a Python emitter**; not one Kotlin-written
  result file in the corpus is over-precise at its own declared precision.
- **`P4`'s regime block is still `null` everywhere**, for `CH-0224`'s reason, which this task does
  not touch.
