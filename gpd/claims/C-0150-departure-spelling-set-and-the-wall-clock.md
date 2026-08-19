# C-0150 — the departure rule's spelling set is a **shape**, not a list, and the shape finds **fourteen** candidates where the challenge that diagnosed the list found nine: **eight are the quantity, six are not, and the six are excluded on measured grounds** — one of them a `log₁₀` whose two-digit rounding moves the residual it stands for by **24 %**. And the corpus's one wall clock is gone

| | |
|---|---|
| **Task** | [`T-225`](../tasks/T-225-departure-spelling-set.md) and [`T-227`](../tasks/T-227-wall-clock-in-a-result-file.md), raised by [`C-0138`](C-0138-departure-rule-scope.md)/[`CH-0169`](../challenges/CH-0169-four-spellings-of-eleven-and-four-implementations.md) (`T-214`) |
| **Leaf** | none — a **process** claim protecting the machine-readable artifact of every leaf |
| **Verification type** | **logical** (a shape census over the committed corpus, a per-key classification against each emission site, an offline simulation of the widening over every committed result file, a mechanical wall-clock census over the *source* rather than over field names, and 91 checker + 28 census + 15 emitter self-tests, with the gate's mutation coverage measured over 31 named tests) **+ in-silico** (ten studies re-run in **one** topological order through **one** snapshot and diffed field by field against their **committed** version read out of `git`) |
| **Verdict** | **PASS on all nine predicates.** `F1`–`F7` are reported below with what each did |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every number this task moved is a diagnostic precision, and one field is deleted |
| **Provenance** | `gpd/results/T-225-departure-spelling-set.json`, emitted by [`tools/T-225-emit-result.py`](../../tools/T-225-emit-result.py) from [`tools/T-225-body.json`](../../tools/T-225-body.json); the shape census in [`tools/T-225-census.py`](../../tools/T-225-census.py) (28 self-tests, `--check`); the mutation measurement in [`tools/T-225-mutation-test.py`](../../tools/T-225-mutation-test.py); the retained order in [`tools/T-225-reemission-order.txt`](../../tools/T-225-reemission-order.txt); the mechanism in `src/main/kotlin/structure/ResultRounding.kt` with the last delegation in `brush/ScfDensityProfileStudy.kt`; the gate in [`tools/check-result-file-hygiene.py`](../../tools/check-result-file-hygiene.py) (**91** self-tests) |
| **Conditions** | The tree at `HEAD` of iteration 35 plus this iteration's edits. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0138`](C-0138-departure-rule-scope.md)/[`CH-0169`](../challenges/CH-0169-four-spellings-of-eleven-and-four-implementations.md) (the residue, the per-key judgement and the reach), [`C-0131`](C-0131-departure-and-saturation-audits.md)/[`CH-0154`](../challenges/CH-0154-the-rule-lives-once-was-true-of-one-package.md) (the `record/spelling` mechanism), [`C-0129`](C-0129-result-file-hygiene.md) (the gate and the `wide` line), [`C-0093`](C-0093-shared-body-coupling.md)/[`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (the two-digit rule and the re-emission discipline), [`C-0117`](C-0117-reemission-order.md)/`CH-0131` (a sweep is a topological sort), [`C-0127`](C-0127-format-string-repair.md) (mutation-test a widened gate), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate), [`C-0110`](C-0110-device-b-tall-gap.md) (run the consumers even when the change is provably invisible), `P-18` (`RESULT_ABSOLUTE_FLOOR` is a claim in the locked units) |
| **Raises** | [`CH-0192`](../challenges/CH-0192-the-census-that-measured-a-stopped-census-stopped-too.md) — the census that measured *"a list is a census that stopped"* stopped too, at the rule's own word — and [`CH-0193`](../challenges/CH-0193-a-ceiling-the-class-exceeds.md) — the `wide` line is documented as a ceiling on the class and the class exceeds it by 68 of 75 |

---

## The claim, in one line

**A rule stated as a list of names is enforced against a *shape*, and the exclusions are the expensive half** —
here the shape found five candidates two prior censuses did not, and six of the fourteen must be left alone,
for four different reasons, only one of which was correctly diagnosed before the emission sites were read.

---

## 1. The cheap bounds, and every one of them shrank the job

| bound | cost | measured | what it decided |
|---|---|---|---|
| **1** — the **shape** census over all committed result files | seconds | **14** candidate names, **75 fields in 8 files** above two digits | the job is 5 names larger than the queue row says, and 2 files wider |
| **2** — the per-key classification, read at the emission site | minutes | **8 in, 6 out**; 54 fields in 6 files in, 21 in 3 out | **the exclusions removed no file from the sweep and the additions added one** (`T-60`); the classification is what makes the widening safe rather than what makes it small |
| **3** — the offline simulation of the widened rule, in Python, over all 138 committed files | seconds | 54 fields in 6 files, per file identical to the checker's independent count | the blast radius is bounded before an SCF solve is spent |
| **4** — the wall-clock census over the **source**, not over field names | seconds | **31** studies measure elapsed time; **1** emits it | `C-0138`'s *"exactly one"* is confirmed, by a stronger instrument |
| **5** — the reader census against the moved keys | seconds | **0** of 62 reader/producer pairs read a moved field | the sweep is the seven producers plus `T-227`'s three consumers, not `T-3b`'s 43 readers |

Bound 4 is worth stating as a method. A wall clock cannot be found reliably by field **name** —
`T-7` emits `waterViscosityPascalSeconds` and five model times, `T-119` a configured
`pauseSecondsBetweenQueries` — so the census is run over `System.nanoTime()`/`currentTimeMillis()`
in `src/main/kotlin` and the variable is followed to its use. Thirty-one studies time themselves and
thirty of them `println`; `brush/DeterminedPrecisionStudy` says so in its own KDoc
(*"the seconds go to stdout only"*, *"cost is counted in SCF SOLVES, not seconds"*).

---

## 2. The classification, and why it is not a pattern

`CH-0169`'s central point is that past the mechanical part of the rule the residue *"needs a judgement per key"*.
It is right, and a judgement per key is only as good as the **key list** it is made over — which is what
[`CH-0192`](../challenges/CH-0192-the-census-that-measured-a-stopped-census-stopped-too.md) is about.
The list here is derived by **shape**: a leaf key inside a `reproductions`/`convergence` record whose *name
denotes a discrepancy*. Deliberately over-inclusive — a false positive costs one line of classification and a
false negative is the whole defect.

### IN — the rule's quantity: a RELATIVE comparison of two computations of one quantity

| spelling | fields > 2 digits | file | what it is |
|---|---|---|---|
| `firstIntegralRelativeSpread` | 12 | `T-3a` | the first integral of the PB solve is **constant in exact arithmetic**; the spread over nodes is zero physics |
| `firstIntegralCoreSpread` | 12 | `T-3a` | the same, over the core of the gap |
| `centrelineRouteSpread` | 11 | `T-3b` | two evaluation **routes** to one solved load |
| `relativeError` | 6 | `T-1d` | `abs(pressure − reference)/reference` over a mesh/contour refinement — `departureFromFinest` under a fifth name |
| `gradientDeparture` | 5 | `T-60` | the 2-D edge mesh's refinement residual on `d ln μ/dh` |
| `relativeSpread` | 4 | `T-164` | the spread over a **nested** 1/2/4 subdivision |
| `multiplierDeparture` | 2 | `T-60` | the same on `μ` |
| `relativeMovement` | 2 | `T-108` | `abs(coarse − fine)/coarse`, with `coarse` and `fine` emitted beside it |

**54 fields in 6 files.**

### OUT — and each exclusion is a measurement, not a preference

| spelling | fields | file | the ground |
|---|---|---|---|
| `residualExponent` | 6 | `T-1d` | **a `log₁₀`.** `T-1d` emits `residual` as exactly `0.0` — `RESULT_ABSOLUTE_FLOOR` reaches it — and carries the information in the exponent instead. Two digits on `−11.0931` is `−11`: a residual of `1e−11` where the solve produced `8.070e−12`, **+23.9 %**, and the three-row node-spacing axis (`−11.0931`, `−11.0912`, `−11.0906`) collapses to one constant |
| `coverageErrorExponent` | 6 | `T-1d` | the same. `−14.1669` → `−14` is `6.81e−15` → `1e−14`, **+46.9 %**, and `−14.5744` rounds the *other* way to `−15`, **−62.5 %** — so two rows a factor of **2.56** apart in the quantity render a factor of **ten** apart |
| `observedOrder` | 6 | `T-1d` (4), `T-1e` (2) | **a logarithm of a RATIO** of two residuals, and the *answer* of the convergence axis. `CLAUDE.md` quotes exactly these four values — `2.08–2.32` for the disjoining pressure and `1.59`/`1.11` for the first moment; at two digits the file would say `2.1`, `2.3`, `1.6`, `1.1`, i.e. **the corpus's own prose would sit outside the file it is read from** |
| `worstResidual` | 3 | `T-117` | **a length in nm** — *"the binding link's distance from the measured `[0.60, 0.70]` nm step"* — in the locked units, carrying the decision `covalent = worstResidual <= 0.0`, and sitting beside the record's **own** dimensionless `departure`, already at two digits. A **level**, in the class of `published`, `reproduced`, `coarse`, `fine` |
| `residual` | 6 | `T-1d` | an **absolute** residual in the solved quantity's own scale (0 fields move either way; both are emitted as exactly `0.0`) |
| `coverageError` | 6 | `T-1d` | the same |

**21 fields in 3 files, left where they are.**

`CH-0169` published twelve as the count that must not be swept. It is **21**, because `observedOrder` is
a third logarithm the exact-match census did not find, and `worstResidual`'s *"needs reading"* resolves to a
length rather than to a departure. And it published `relativeMovement` as an exclusion, which is the one
judgement that runs the wrong way — see §3.

---

## 3. The two judgements that were wrong, one in each direction

**`relativeMovement` was excluded as *"`P-18`'s own determined-precision measurement"*, and the record
qualifier already protects that.** `P-18` is `brush/DeterminedPrecisionStudy`; it declares
`"relativeMovement" to 3` at its own emission site and emits the field **outside** both departure records,
where nothing in this rule can reach it — the same protection `CH-0154` established for `T-193`'s volts and
`T-160`'s own answer. The fields the census counts belong to `synthesis/DesiredStrokeReachStudy`, and they are
`abs(coarse − fine)/coarse` over a scan-step and an RK4-step refinement. **Two of the three files that emit
`convergence[*].relativeMovement` — `T-182` and `T-189` — already emit it at two significant digits**, so the
corpus had classified it correctly twice before the challenge classified it wrongly once. A **spelling** was
read as a **study**.

**`worstResidual` was left open as *"needs reading"* and the reading is one KDoc line.** It is a length.

---

## 4. The reach: the sixth rounding implementation, and it is the last one

`CH-0154` measured that `roundedForActuatorResult()` takes no arguments, so six files could not obey the rule
*by any edit at their own emission sites*, and repaired `actuator/`.
`CH-0169` found the same defect in **four** more entry points and `T-214` delegated three, leaving
`brush/ScfDensityProfileStudy.kt`'s private traversal **and measuring it clean** — correctly, against a rule
that then had four spellings and no field of `T-1d` under any of them.

Under the widened rule `T-1d` carries six, so the fourth is delegated here and the census closes at **6 of 6**.
The delegation had to carry two things the file owns and the shared function defaults differently:

| observable | this file | the shared default | carried as |
|---|---|---|---|
| the digit ceiling | **6** (`SOLVED_HEIGHT_SIGNIFICANT_DIGITS`, `P-18`) | 9 | `digits = RESULT_SIGNIFICANT_DIGITS` |
| the integral-number rendering | pass through | pass through | the default, `roundIntegralNumbers = false` |

and one scalar call site — `insideStandingBracket`, which compares three **rounded** values and is therefore a
**decision** — kept its own six-digit wrapper. Delegating that without carrying the default would have moved a
boolean with nothing visible beside it, which is `C-0138`'s `roundIntegralNumbers` finding in a second place.

---

## 5. The sweep

Ten files, **one** `tools/reemission-order.py` order over the whole set
([`tools/T-225-reemission-order.txt`](../../tools/T-225-reemission-order.txt)), through **one**
`tools/study-batch.sh` snapshot, each diffed field by field against its **committed** version read
out of `git`. Six are `T-225`, one is `T-227`, three are `T-227`'s readers.

| # | file | departure fields | other numeric | verdicts / wording | prose digits only | booleans | added | removed |
|---|---|---|---|---|---|---|---|---|
| 1 | `T-108` | 2 | 0 | 0 | 0 | 0 | 0 | 0 |
| 2 | `T-1d` | 6 | 0 | 0 | 0 | 0 | 0 | 0 |
| 3 | `T-3a` | 24 | 0 | 0 | 0 | 0 | 0 | 0 |
| 4 | `T-3b` | 11 | 0 | 0 | 0 | 0 | 0 | 0 |
| 5 | `T-60` | 7 | 0 | 0 | 0 | 0 | 0 | 0 |
| 6 | `T-164` | 4 | 0 | 0 | **3** | 0 | 0 | 0 |
| 7 | `T-172` | 0 | 0 | 0 | 0 | 0 | 0 | **1** |
| 8 | `T-182` | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| 9 | `T-189` | 0 | 0 | **1** | 0 | 0 | 0 | 0 |
| 10 | `T-190` | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| | **total** | **54** | **0** | **1** | **3** | **0** | **0** | **1** |

**54 departure fields, exactly what the offline simulation predicted, file by file** — `F2` did not fire.
**Zero** other numeric fields, in ten files including a 158 497-node field solve and an SCF sweep.
The one removal is `T-172`'s `parameters/elapsedSeconds`.

**Nothing is stale, as an identity.** Every departure field in every re-emitted file is either
unchanged or **exactly** the two-significant-digit rounding of its own committed value:
**104 already at two digits, 54 exactly the two-digit rounding, 0 unexplained**, over 158 residuals in ten files.

### The three prose-digit movements in `T-164`, and the one wording movement in `T-189`

`T-164`'s three are `0.1686405908358076` → `0.1686405908358075` inside three sentences —
a `Double.toString()` drifting in its **sixteenth** digit, which `C-0127`'s digits-stripped
classifier separates from a verdict change by construction. They are also a live instance of
`CLAUDE.md`'s *"a number emitted as a STRING is not rounded"*: the serialisation boundary rounds a
JSON number and cannot reach one interpolated into prose, so `T-164` is permanently un-diffable for
the same reason `T-172` was. **It is queued (`T-249`) rather than repaired here**, because repairing
it is another re-emission and this sweep's whole claim is that it moved departures and nothing else.

`T-189`'s one is its `parameters/sources` string gaining
`gpd/results/T-182-row-end-prestrain-value.json`. It is **deterministic staleness in `HEAD`**, not a
movement this sweep caused: `HEAD`'s `TwistCorrectedRasterStudy.kt` reads that file at line 459 and
lists it at line 1161, and the committed result — emitted in the *same commit*, `3f15f8a` — lists
three sources. The working tree's copy of that source is identical to `HEAD`'s.

---

## 6. `T-227` — the wall clock, and why the census had to be run over the source

`CLAUDE.md` states the rule verbatim and it was still emitted, which is the same shape as every
other entry in this family. `C-0138` found it by **re-emitting the file and watching the field move
1.1 %**, not by anyone reading it.

**The removal is one line, and proving it safe is the work.** Removing a field is a schema change,
and `T-172` has three readers. All three take the same route:

```kotlin
Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("cheapBounds").jsonArray.map { it.jsonObject }
    .firstOrNull { it.getValue("name")... .startsWith(namePrefix) }
    ... .getValue("value")
```

— a **named lookup in `cheapBounds`**, never a deserialisation of `parameters`. So the removal is
invisible to all three by construction, and `C-0110`'s rule then says to run them anyway, because
the run also checks everything the proof was not about. It did: `T-182` and `T-190` came back
**byte-identical**, and `T-189` came back with a staleness this task did not cause.

**The corpus census is run over the source, because a field name cannot answer the question.**
`T-7` emits `waterViscosityPascalSeconds` and five model times (`relaxationTime`,
`lateralDrainageTime`, `verticalDrainageTime`, `zimmRelaxationTime`, `inertialTime`); `T-119` emits
a configured `pauseSecondsBetweenQueries = 8`. A name-shaped census returns all of them and cannot
rank them. Grepping `System.nanoTime()`/`System.currentTimeMillis()` in `src/main/kotlin` and
following the variable to its use is exact: **31 studies measure their own elapsed time and exactly
one put it in a result file.** Thirty `println` it — `brush/DeterminedPrecisionStudy` says why in
its own KDoc, *"the seconds go to stdout only"* and *"cost is counted in SCF SOLVES, not seconds"*,
which is the discipline the other thirty follow without stating it.

`C-0138`'s *"exactly one"* is therefore **confirmed by a stronger instrument**, and `F7` did not fire.

---

## 7. The gate, and its mutation coverage measured in both directions

```
GATE  (12 classified spellings inside a reproductions/convergence record): 0 field(s) in 0 file(s)
scope (the same predicate — since T-214 the gate IS the rule):            0 field(s) in 0 file(s)
strict (C-0129's leaf-name predicate, now a proper subset of the gate):   0 field(s) in 0 file(s)
```

`C-0127`'s standard is that restoring the old narrow predicate must **fail a named test**, and
`C-0138` met it by measuring rather than asserting. Measured here, by
[`tools/T-225-mutation-test.py`](../../tools/T-225-mutation-test.py), over **31** `GATE_TESTS`:

| mutation | named tests that fail |
|---|---|
| narrowed back to `T-214`'s four spellings | **9** |
| narrowed back to `C-0129`'s leaf name | **15** |
| the six **excluded** names swept in by pattern | **6** |
| the record qualifier dropped | **7** |

and, one name at a time, **every one of the 12 `IN` and 6 `OUT` classifications fails at least one
named test when it is changed** — which is the property that matters, because a whole-predicate
count can be carried by two popular rows. The exclusions are held open in the direction nothing else
in this repository tests: **sweeping a name in by pattern is a failure**, and that is exactly what
`CH-0169` refused and had no instrument for.

`tools/T-225-census.py --check` closes the other half: it exits 1 on a candidate name the corpus
contains that is in **neither** set, which mechanises the *"standing obligation to add a name when a
study coins one"* `CH-0169` states in prose. It reads
`18 candidate name(s), 12 in, 6 out, 0 unclassified`.

---

## 8. Falsifiers

| | statement | fired | outcome |
|---|---|---|---|
| **F1** | a re-emitted file moves a non-departure numeric field, a verdict, a boolean or a word | **FIRED on 2 of 10** | `T-164`'s three prose-digit movements are `Double.toString()` in its sixteenth digit inside a sentence, separated from a verdict by `C-0127`'s classifier and queued as `T-249`; `T-189`'s one wording movement is deterministic staleness in `HEAD` from iteration 31, confirmed by a control re-run of `HEAD`'s own code. **0 numeric, 0 boolean, 0 verdict** |
| **F2** | the offline prediction differs from what the sweep moved | **no** | 54 fields in 6 files predicted, 54 in 6 moved, per file identical |
| **F3** | a residual is not the two-digit rounding of its committed value | **no** | 54 of 54 are exactly it; **0** unexplained over 158 residuals |
| **F4** | an excluded key is a residual after all, or an included key is a level | **no, against this classification — FIRED TWICE against the inherited one** | `relativeMovement` was excluded as `P-18`'s answer and is a mesh-refinement residual that two other files already emit at two digits; `worstResidual` was left *"needs reading"* and is a length in nm. That is [`CH-0192`](../challenges/CH-0192-the-census-that-measured-a-stopped-census-stopped-too.md) |
| **F5** | the gate does not come clean, or a mutation passes | **no** | `0 field(s) in 0 file(s)`; 18 of 18 classifications individually protected |
| **F6** | a reader of `T-172` fails or moves a number after the removal | **no** | `T-182` and `T-190` byte-identical; `T-189`'s only movement predates this task |
| **F7** | a second wall clock exists | **no** | 31 studies measure elapsed time, 1 emitted it, 0 do now |

---

## 9. The residue, published with its cost

`CH-0168`'s rule is that a residue published without its own cost is priced against the nearest table.

| residue | size | why it stays | cost to close |
|---|---|---|---|
| the **six excluded spellings** | 21 fields in 3 files | a `log₁₀` (×2), a convergence order, a length in nm, two absolute residuals — each with a stated ground and a named mutation test | **not work; a decision.** Closing it would be a defect |
| **`T-164`'s three unrounded numbers inside prose** (`T-249`) | 3 fields in 1 file | out of scope for a sweep whose claim is that it moved departures only | one `String.format` fix, one lattice-solve re-emission, no readers |
| **`T-189`'s stale `sources` string** | 1 field | repaired **here**, by the re-emission | done |
| the **`wide` line's under-count** ([`CH-0193`](../challenges/CH-0193-a-ceiling-the-class-exceeds.md)) | 68 of 75 | retained deliberately as a bound on one spelling family; the ceiling on the class is now `tools/T-225-census.py` | done |

---

## 10. What the outward-facing documents owe: nothing, and it is measured

`ANSWERS.md` and `DECISIONS-FOR-NDI.md` were **not** edited here (they are another agent's this
iteration). Nothing is owed to either: `tools/trace-answers.py` reads
`0 open assertion(s), 0 contradicted by TASKS.md, 0 ABSENT tokens` on both after this task's
`TASKS.md` rows, and neither document quotes a departure precision, a spelling set or a wall clock.
The one place this work is visible outside `gpd/` is `CLAUDE.md`, whose *"every spelling the corpus
uses"* entry is rewritten with the corrected census and whose wall-clock entry now records the
removal and the source-side census method.

---

## 11. What this is a fourth instance of

`CLAUDE.md` already carries *"each named one instance, and each instance was a population"* for this rule
three times over: `C-0093` cured it on one axis, `C-0101` in eleven files, `C-0127` found a twelfth,
`C-0129` named it, `C-0131` re-keyed it, `C-0138` moved it into the layer. The fourth level is the one
above the mechanism: **the predicate itself was enumerated, twice, by the method it exists to catch.**

The general form is worth stating because it is not about departures.
A rule that is stated as *"every X the corpus uses"* is enforced against a **list**, and a list is
checkable only against the search that produced it. The cure has three parts and this task needed all three:

1. search for the **shape** rather than the name, and be over-inclusive, because a false positive costs one
   line of classification and a false negative is the defect;
2. require a **classification** of every candidate the shape returns, so the judgement is made rather than
   omitted — and make *that* the gate (`tools/T-225-census.py --check`);
3. mutation-test the classification **in both directions**, because a predicate that can only ever be
   widened has become a pattern, which is exactly what the judgement refused.

The one that was missing everywhere before this task is the third.
`C-0127`'s standard — *restoring the narrow predicate must fail a named test* — is one-sided, and it is the
side a widening satisfies for free.
