# C-0162 — **ROUND OUTPUTS, NEVER INPUTS.** A result file's parameter block was rounded like its results, so `gpd/README.md`'s *"reproducible from it alone"* was false for **50 of 148** files — and the rule that repairs it is one sticky exemption in the one layer every study already goes through, so all **six** rounding entry points obey it with no edit of their own. The blast radius is **1 086 numeric parameter leaves in 50 files** and nothing outside a parameter subtree is reachable by the new branch at all — and the cheaper offline bound this claim tried to draw inside it, *"a value already at fewer than nine significant digits cannot move"*, is **WRONG and was falsified by its own sweep**: **66 of the 333 fields that moved** were shorter than nine digits at `HEAD`, because an **absolute floor is part of the rounding**, so the values that move furthest are the ones that were **below** it — `T-121` emitted three duplex masses as `0.0` and they are `3.2e−21`, `8.8e−19` and `1.9e−18` (§3). `CH-0207`'s reader-graph closure of 83 files — **~5 hours** — collapses to those same 42 by reading one table: six of its seven parameter-reading call sites read **strings**, a seventh reads `1614.0`, and the single live edge is `T-136 → T-138`, both already in the sweep and in the right order. **`P1` was found already discharged** — the six implementations delegated in iterations 36–38 — and the count is wrong in the other direction too: **sixteen Python emitters** in `tools/` write committed result files and no rule in the Kotlin layer reaches any of them

| | |
|---|---|
| **Task** | [`T-268`](../tasks/T-268-emission-layer.md) — one emission layer, typed input handles, and a lattice tag and regime block on every record |
| **Leaf** | none — step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Verification type** | **logical** (a rule about what a result file promises) **+ in-silico** (the rule is applied and the corpus re-emitted, with every movement classified by kind against `git show HEAD:`) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** This claim is about **emission, provenance and reproducibility**, not about the device. **No physics is asserted, no model changes, and no solver is touched** |
| **Verdict** | **PASS on `P1` (found already discharged — §5a), `P6` and `P7` over the swept set; PARTIAL on `P5`; OPEN on `P2`, `P3`, `P4` with measured costs (§7).** **46 studies re-run (42 in a 38-constraint topological order, then 4 the withdrawn bound had wrongly excluded), 0 failures, 40 files changed**: `parameter` **333**, `numeric` **14**, `prose` 3, and `wording` / `departure` / `boolean` / `added` / `removed` all **0**. All 14 `numeric` were controlled against `HEAD` and **none is this sweep's** — two descent manifolds (`C-0135`, `C-0131`) and one nine-digit **rounding tie**; all three files already failed to reproduce from `HEAD`'s own code. `F2` discharged exactly: `T-3a` now carries `−0.3986652379247042`. **No claim quotes a moved number, so none is amended** |
| **Conditions** | Units unchanged and locked; 300 K; `k_BT = 4.142 pN·nm`. No model, solver, mesh, tolerance or convergence parameter was altered |
| **Consumes** | [`CH-0207`](../challenges/CH-0207-a-parameter-block-cannot-re-run-its-own-study.md) (the defect, and its seven-reader table), [`C-0159`](C-0159-environment-interface.md) (which raised it), [`C-0138`](C-0138-departure-rule-scope.md) / `T-214` and `C-0129` (put the rule in the layer, not the call sites), `T-214`/[`C-0138`](C-0138-departure-rule-scope.md) again (the integral-number rendering carried as a parameter — `P1` cites this as `CH-0133`, which does not exist; see §5), [`C-0117`](C-0117-reemission-order.md) / `CH-0131` (a re-emission sweep is a topological sort), [`C-0135`](C-0135-descent-manifold-width.md) (descent manifolds), [`C-0153`](C-0153-unrounded-prose-interpolations.md) / `T-250` (the by-kind movement classifier), `P-28` (the run times the cheap bound is built on) |
| **Constrains** | Restores `gpd/README.md`'s `results/` contract for the files it swept. Leaves `P2`, `P3`, `P4` and the rest of `P5` open with a measured cost (§7) |

---

## 1. The rule

> **An input is not a result.** A number anywhere below a `parameters`, `runParameters` or
> `citedInputs` key is emitted exactly as the study was handed it — at `Double.toString()`'s
> shortest round-trip decimal — so the file can re-run its own study.

`CH-0207` offered two repairs and this is its first: the alternative was to weaken
`gpd/README.md` instead, which cannot be taken while seven call sites read a parameter block back
**as an input** and at least one of them feeds a rounded number into a solve.

Three properties make it a rule of the **layer** rather than a habit of the call sites:

- **It is a default.** `roundedForResult`'s new `parameterRecords` parameter defaults to
  `PARAMETER_RECORDS`, so `actuator/`, `coupling/`, `window/`, `brush/ScfDensityProfileStudy` and
  `brush/FluctuationCorrectionStudy` inherit it with no edit. That is `C-0138`'s lesson applied
  before it had to be learned a sixth time — the departure rule was repaired **five** times per
  call site before it moved here.
- **It is sticky.** A parameter block is a record *type*, so everything below it is input. The
  existing `record` qualifier carries only the **nearest** enclosing key, which under
  `parameters/buffer/debyeLength` is `buffer`; the exemption is threaded down the whole subtree
  instead.
- **It beats every precision rule beneath it**, `DEPARTURE_DIGITS_BY_KEY` included. Those say how
  well a quantity this study *computed* is determined; a number the study was *handed* is not that
  quantity.

## 2. The scope is a census, and it is closed in both directions

Over the 148 committed result files:

| spelling | occurrences | where |
|---|---|---|
| `parameters` | 95 | all top level |
| `citedInputs` | 41 | all top level |
| `runParameters` | 19 | all top level |

The singular **`parameter` occurs 180 times and is deliberately excluded**: it is a *swept axis
coordinate* (152 strings, 28 `Double`s), so widening the set to every key whose name contains
*parameter* would silently stop rounding 28 **outputs**. `CLAUDE.md`'s *"every spelling the corpus
uses is a census that stops"* read the other way — **a named set may be extended by census and
never by pattern** — and both directions are named tests, per `C-0127`'s mutation-test standard.

## 3. The blast radius, proved offline before a JVM started

`C-0138`'s standard: the reach of a one-line change in a shared layer is provable in Python.

| | |
|---|---|
| files carrying at least one **numeric** parameter leaf | **50** of 148 |
| numeric parameter leaves in them — **the true ceiling** | **1 086** |
| fields outside a parameter subtree that can move | **0**, structurally — the new branch is reachable only under the sticky flag |
| …carrying **9 or more** significant digits — a *heuristic* narrowing, and **wrong** | 312, in 42 files |
| measured movers | **333**, in **40** files: **267** predicted by the heuristic and **66** not |

**The narrowing is withdrawn, and its failure is the more useful half.** The reasoning was that a
value already at fewer than nine significant digits is its own nine-digit rounding, so the rule is a
no-op on it. That is true of the **precision** and false of the **floor**: `roundForResult` also
floors any magnitude below `RESULT_ABSOLUTE_FLOOR = 1e−9` to exactly zero, and it drops trailing
digits that round to a shorter decimal. So the leaves that move **furthest** are precisely the ones
the heuristic called immovable —

| file | leaf | committed | after |
|---|---|---|---|
| `T-121` | `parameters/duplexMassPerLength` | `0.0` | `3.1745599867647062e−21` |
| `T-121` | `parameters/armArrayMass` | `0.0` | `8.812238471377075e−19` |
| `T-121` | `parameters/sheetMass` | `0.0` | `1.9047359920588236e−18` |
| `P-14` | `runParameters/medialCollar` | `1.222623` | `1.222623003199281` |

`T-121`'s parameter block was reporting a duplex as **massless**. `RESULT_ABSOLUTE_FLOOR` is a claim
*in the locked units* — `P-18`'s *"an absolute floor is a claim about units, and it does not travel"*
— and a mass in kg per nm is not in them; the floor reached an **input** in the wrong units, which
is `C-0031`'s floored `layerStiffness` beside an unfloored `√(k_BT/k)` met in a parameter block.

**And the wrong bound cost a re-run.** The sweep was scoped by the heuristic to 42 studies, so
**four Kotlin studies with numeric parameter blocks were excluded** — `P-18`, `T-1f`, `T-5`, `T-5b`
— and had to be swept afterwards (0 dependency constraints among them, and `T-1f`'s two readers
consume its `propagation` records rather than its parameters, so no downstream re-run was owed).
The lesson is the one `CLAUDE.md` already states about ceilings: **a cheap bound is only cheap if it
is a bound.** The sound one here is the whole `1 086`, and it was affordable.

The other **64** files interpolate their parameters into **strings** and were never exposed, because
`roundedForResult` dispatches on the JSON type. Which side a study landed on was a per-study
rendering convention with no rule behind it, and that is the half of `CH-0207` this repair removes.

## 4. The closure question, answered by a table rather than by five hours of compute

A downstream study moves only if it reads a parameter leaf **that moved**. The naive reader-graph
closure of the 50 seed files is **83 files, ~222 measured minutes plus 33 untimed studies — about
five hours a pass.** `CH-0207` had already published the seven call sites that read a parameter
block as an input, so the question is one lookup per row:

| reader | producer | what it reads | can it move? |
|---|---|---|---|
| `window/UpstreamResults.readScfResults` | `T-1d` | `restingLoad`, `monomerVolume`, `tileArea` | **no** — strings |
| `window/UpstreamResults.readActuatorResults` | `T-3` | `trustedBiasCeiling`, `biasCeiling` | **no** — strings |
| `coupling/BuildableWidthCountPhaseStudy` | `T-188`, `T-3b` | `freeStrokeBuildable`, the collar | **no** — strings |
| `structure/RaggedFaceCostStudy` | `T-218` | `recommendedAxialExtentBasePairs` | **no** — string |
| `stability/DoublingLadderRepairStudy` | `T-149` | `elementCeilingSafety` | **no** — string |
| `window/UpstreamResults.readLayoutResults` | `T-14` | `tileArea` = `1614.0` | **no** — four digits |
| `anchoring/PathCountConsistencyStudy` | `T-136` | `winnerMargin` = `0.419511928`, `recommendedMinimaxOverStroke` = `0.0682200897` | **YES** — nine digits each |

**One live edge, `T-136 → T-138`.** Both are in the swept set and the topological order puts `T-136`
at position 29 and `T-138` at 37, so the set is closed and the order is right. Five hours of compute
were spent by reading a table a challenge had already written.

## 5. Three findings about the step's own premise, which was written from a stale description

**(a) `P1` was already discharged.** All six Kotlin rounding entry points already delegated to
`structure/ResultRounding.kt` before this task opened — `actuator/` by `T-212`/`CH-0154`,
`coupling/` and `window/` by `T-214`/`C-0138`, `brush/FluctuationCorrectionStudy` by `T-214`,
`brush/ScfDensityProfileStudy` by `T-225` — the three public ones assert both constants equal to the
tree's as a test, and the integral-number rendering is already the `roundIntegralNumbers` parameter.
[`ARCHITECTURE.md`](../../ARCHITECTURE.md)'s layer-7 row, *"six implementations; not started"*,
describes the tree as it was before iteration 36. `T-268`'s own defect statement inherited it.
The two `brush/` constants that *"disagree on the absolute floor"* are not a disagreement either:
they are **declared per-study precisions** with stated reasons (`SOLVED_HEIGHT_SIGNIFICANT_DIGITS`
= 6 for anything downstream of a solved height, `1e−12` for `T-1f`'s dimensionless fields), which is
exactly the shape `P-18` requires.

**(b) There is no `CH-0133`.** `P1` cites it for the integral-number rendering. The corpus's highest
challenge is `CH-0209`; no challenge file, no index row in
[`gpd/challenges/README.md`](../challenges/README.md) and no source in the tree carries that ID —
the only two occurrences anywhere are `T-268`'s own predicate table and, until this claim was
checked, the sources written from it. The **finding** is real and is `T-214`/`C-0138`'s, restated in
`CLAUDE.md` as *"the integral-number rendering is a per-package convention frozen by the committed
files"*. `T-268`'s `P1` row is annotated in place rather than rewritten (`C-0071`: **strike, never
delete**). This is `C-0083`'s class exactly — *a cross-reference is a filename, and a filename is a
number like any other* — reaching an **identifier** rather than a slug, where no numeric tracer and
no link checker can see it, because a bare `CH-0133` in prose is not a link.

**(c) The implementation count is wrong in the other direction, and by more than the six.**
**Sixteen committed result files are written by a Python tool in `tools/` and by no Kotlin study** —
`T-9`, `T-119`, `T-183`, `T-184`, `T-194`, `T-200`, `T-201`, `T-202`, `T-211`, `T-212`, `T-214`,
`T-225`, `T-234`, `T-249`, `T-250` and `P-22` — and **no rule in the Kotlin emission layer reaches
any of them.** Seven more (`T-129`, `T-136`, `T-148`, `T-156`, `T-157`, `T-172`, `T-192`) are written
by a Kotlin study and then **patched** by one, which is a hazard this sweep ran straight through: a
re-run rewrites the file from the study and would silently drop whatever the patcher added. It did
not — **`removed` is `0` across all 40 files** — and the only reason that is *known* rather than
hoped is that the by-kind classifier counts a vanished pointer. Four of them
(`T-9`, `T-183`, `T-225`, `T-249`) carry numeric parameter blocks and are therefore inside this
rule's *scope* and outside its *reach*; they are excluded from the sweep only because none of their
parameter leaves carries nine significant digits, which is luck rather than design. They are step
7's material (`ARCHITECTURE.md`: *"`tools/` leaves"*), and the honest statement of layer 7 is
**six implementations in Kotlin and sixteen in Python**, not six.

## 6. The sweep, and what moved — `P5` in part, `P6` and `P7` in full over it

**46 studies re-run**, in `tools/reemission-order.py`'s topological order over the whole set at once,
its dependency-constraint count asserted **non-zero (38)** before the order was trusted (`C-0153`
found that tool silently reporting `0` on a path argument). **Zero failures. 40 files changed; 6 were
re-run and did not change**, because their nine-digit parameters were already exact.

| kind | count |
|---|---|
| **`parameter`** — a numeric leaf below a parameter block | **333** |
| `numeric` — **any other** numeric leaf, and a finding | **14** |
| `prose` — a string leaf whose digits moved | 3 |
| …of which tokens the staleness identity **cannot** explain as a rounding | 4 |
| `wording`, `departure`, `boolean`, `added`, `removed` | **0** |

The four unexplained tokens are `T-136`'s drawn numbers rendered into its two verdict sentences —
correctly unexplained, because the staleness identity asks whether a moved token is the **rounding**
of its predecessor and a manifold draw is not.

`tools/T-250-movement.py` gained a `parameter` kind for this, mirroring `PARAMETER_RECORDS` as a
literal rather than importing it — *a census that reads its own subject's declaration cannot report a
drift between them.* Without the kind, all 333 deliberate movements would have reported as `numeric`,
which that tool defines as **a finding**; naming the kind is what lets `numeric` keep meaning what it
meant, and it is why the 14 stood out.

### `F2` — the artifact now satisfies the contract

`T-3a`'s `runParameters.nominalTileChargeDensity`, the number `CH-0207` was filed on:

| | |
|---|---|
| committed at `HEAD` | `−0.398665238` |
| after the sweep | **`−0.3986652379247042`** |
| what `CH-0207` says the study solved with | `−0.3986652379247042` |

and that file moved **4 parameter fields and nothing else**.

### `F1`/`P7` — all 14 `numeric` movements controlled against `HEAD`

Every one was put through `C-0129`'s control: the study run **twice** in a `--committed` snapshot —
`HEAD`'s code, `HEAD`'s inputs, none of this change.

| file | fields | control verdict |
|---|---|---|
| `T-129` | **7**, all `subsets[*].minimaxWorstOverStroke` | two `HEAD` runs differ in **10** fields — `ranges[1]`'s six and two subset minimaxes. `C-0135`'s **descent manifold**, and `C-0131`'s two-valued `ranges[1]`; `TASKS.md`'s own entry-point row already records *"a re-run differs in 28 lines of 1 423, all inside the 31 subset minimaxes"*. The sweep's seven are a strict **subset** of that block |
| `T-136` | **6**, all `distributions[11]/*`, plus the 3 `prose` | two `HEAD` runs differ in the **same nine** fields, and the two readings are exactly the pair the sweep swapped between (`peakRatio` `2.45258143` ↔ `2.31362128`). **Two-valued**, like `C-0131`'s `ranges[1]` |
| `T-14` | **1**, `cornerStates[0]/phases[30]/…StiffAnchor` | two `HEAD` runs differ in **two** fields, `12.0023304` ↔ `12.0023305` and `7.13662295` ↔ `7.13662294` — one unit in the **ninth** digit each way |

**Not one movement outside a parameter block is this sweep's**, and all three files failed the
weaker test too: **`run A` does not reproduce the committed file at `HEAD`'s own code** in any of the
three. They were already irreproducible, and `T-136`'s entry-point row says it was *"re-run through
`tools/study.sh` and diffed **byte-for-byte identical**"* — which the control measures as a **draw**
rather than a property, `C-0131`'s rule verbatim.

`T-14`'s is a mechanism this corpus had not recorded. It is not a descent: the underlying value sits
within half a unit in the last place of the **nine-digit rounding tie** at `12.00233045`, so the JIT's
summation-order noise — which nine-digit rounding exists to absorb — is **amplified by the rounding
boundary into a visible last-digit flip**. Rounding makes a file reproducible everywhere except
within half an ulp of a tie, and there it makes it worse. No claim quotes either value (checked).

### The one live propagation edge, end to end

`T-136`'s two parameters are the only ones in the corpus that a downstream study reads **as inputs**
and that could move (§4). They did:

| | `HEAD` | after |
|---|---|---|
| `winnerMargin` | `0.419511928` | `0.4195119276710484` |
| `recommendedMinimaxOverStroke` | `0.0682200897` | `0.06822008970269246` |

`T-138` reads both, ran after `T-136` in the order, and moved **`parameter` = 10 and every other kind
`0`** — so `C-0008`-scale insensitivity holds: the `1.6e−10` those digits are worth does not reach
its outputs. The channel is now correct **and** measured, rather than correct and assumed.

### `P6`'s second half — claims quoting a moved number

**None.** Every one of the 333 is a parameter, and a grep of `gpd/`, `ANSWERS.md` and
`DECISIONS-FOR-NDI.md` for the three drawn values finds no claim quoting them. No claim is amended
and no verdict moves — which is the expected result for a rule that changes only how an **input** is
rendered.

## 7. What is **not** discharged, with its measured cost

Reported rather than shrunk, per `SESSION-PROMPT.md`. `P5` as written asks for the whole corpus in
one topological order; measured against `P-28`'s newly completed run times that is **411 minutes over
71 of the 124 emitting studies, with 53 studies carrying no measured time at all** — at least seven
hours for one pass, and `P6`/`P7` require a second. It does not fit a session on this box, and
`CLAUDE.md` records that three concurrent agents is this machine's ceiling for compiling work.

| predicate | state | what it would cost |
|---|---|---|
| **P1** | **discharged, and it was already discharged** — §5(a) | — |
| **P2** typed input handles | **open** | 72 studies read `gpd/results/` by path; the convention that would make the graph self-maintaining is used by **2 of 72**. The census is already asserted a superset of the grep (`P-22`/`C-0082`, 41 direct and 20 transitive edges against a grep's 1) |
| **P3** `lattice` tag on every record | **open** | the *content* exists — `lattice/CrossoverLattice` since `T-266` — what is missing is putting it on every record, which **is** the full `P5` sweep |
| **P4** `regime` block on every record | **open** | likewise: `environment/Regime` is `@Serializable` data since `T-265`/`C-0159`, built for exactly this. Again gated on the full sweep |
| **P5** whole corpus, one order | **partial** — the 46 studies whose result file this step's rule can move, 42 of them in a 38-constraint topological order | the remaining 102 files: **≥ 7 h** for one pass, **≥ 14 h** with the control |
| **P6** movement by kind | **discharged over the swept set** — §6 | — |
| **P7** manifolds not reported as staleness | **discharged over the swept set** — §6 | — |

**`P3` and `P4` are deliberately not landed as unused types.** Layers 1–4 were landed that way on
purpose and said so; here the type already exists on both axes, so writing a *third* unused
declaration would add a schema nothing emits while the thing that is actually missing — the sweep —
stays unpaid. The honest statement is that both are **one full re-emission away**, and that the
re-emission is the cost.

## 8. Validity range

- **This claim is about emission and provenance.** No physics, no model, no solver, no mesh, no
  tolerance and no convergence parameter was touched, and no verdict of any upstream claim is
  re-derived here.
- **The rule is asserted over the 40 files swept that changed**, not over the corpus. The 64 string-parameter
  files satisfied the contract already and are untouched; the four Python-emitted files inside the
  rule's scope are outside its reach (§5(c)).
- **The offline ceiling is 1 086 leaves and the narrowing to 312 was withdrawn** — §3. What *did* move is §6.
- `PARAMETER_RECORDS` is a **census of the corpus as it stands at this commit**. `CLAUDE.md`'s own
  rule applies to it: a study that coins a fourth parameter-block spelling is outside the rule until
  the set is extended, and the extension must be by census.
## 9. Still open, and what would falsify this

- **`CH-0207` is closed on its first repair, not its second.** If the seven readers were ever
  re-pointed at a study's *source* rather than at its result file, the contract could instead be
  weakened, and this rule would become unnecessary rather than wrong.
- **`P2`, `P3`, `P4` and the bulk of `P5` are open** with the costs in §7. Nothing here makes them
  cheaper; what it does is make the *next* sweep's movement classifiable, because
  `tools/T-250-movement.py` now has a `parameter` kind and `numeric` keeps meaning *a finding*.
- **Falsifier `F1` — a movement that is not a parameter and not explained.** Declared before the
  sweep, on the absolute quantity rather than on a ratio (`CLAUDE.md`: an amplification mis-ranks
  across topologies). Fired once; §6 records the control that attributes it.
- **Falsifier `F2` — a study whose parameters no longer round-trip.** The rule is only worth having
  if the artifact now satisfies it: `T-3a`'s `nominalTileChargeDensity`, the number `CH-0207` was
  filed on, must come back as the value the study solved with.
- **Falsifier `F3` — the exemption reaches a field it is not about.** `parameterRecords = emptySet()`
  must reproduce the pre-repair behaviour bit for bit, which is a named test rather than a re-run.
