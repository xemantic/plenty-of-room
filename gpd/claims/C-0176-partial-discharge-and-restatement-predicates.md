# C-0176 — **A DISCHARGE HAS A DATE AND A POINTER SET, AND A PARTIAL DISCHARGE IS TWO OF EACH UNDER ONE TOKEN — so what a census cannot represent is not a pattern, it is a DATA STRUCTURE.** `C-0141` supplied the honeycomb station lattice, plan ceiling and placement family and did **not** supply a grillage, and `tools/T-234-census.py` had one global pointer set for both halves; giving each family its own splits `PLACEMENT` **13 / 17 / 8** and `WIDTH` **46 / 57** by tested predicates (both counted over the corpus **without this claim's own worked examples**, which is `CH-0182` on the claim that reports it), empties two file-set patches, and takes the gate from **5 defects to 0** — **3** by predicate and **2** by a hand override the emitter had promised since iteration 34 and did not honour — with **42** mutations failing nothing in **both** directions. The advisory debt line falls **41 → 24**, and over the last **40** revisions of the two deliverables the old predicate grows **0 → 25** against the new one's **0 → 10** — at the largest single document pass, **+9 against +1**

| | |
|---|---|
| **Task** | [`T-260`](../tasks/T-260-partial-discharge-predicate.md) and [`T-262`](../tasks/T-262-width-restatement-predicate.md) |
| **Leaf** | none — a **process** claim protecting the census that protects every honeycomb leaf |
| **Verification type** | **logical** — a token census over the in-scope corpus; a predicate measured against a **hand reading of all 38** occurrences of its ambiguous family; a **historical** false-positive series taken out of `git` over 40 revisions; a mutation measurement in **both** directions over **42** mutations and **126** named self-tests (95 census + 31 emitter) |
| **Verdict** | **PASS on all four `T-260` predicates and all four `T-262` predicates.** `F1`–`F4` are reported in §7; **`F3` FIRED and its result is the claim's second half** |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Not one physical number moves. What moves is which sentences a checker calls debt |
| **Provenance** | `gpd/results/T-260-partial-discharge-predicate.json` and `gpd/results/T-262-width-restatement-predicate.json`, emitted by [`tools/T-260-emit-result.py`](../../tools/T-260-emit-result.py), which **derives** its mutation numbers by running the mutation test rather than typing them; the predicates and their 95 named tests in [`tools/T-234-census.py`](../../tools/T-234-census.py) (`--self-test`); the class coercion, the hand-override mechanism and 30 named tests in [`tools/T-234-emit-classification.py`](../../tools/T-234-emit-classification.py) (`--self-test`, new); the mutation measurement in [`tools/T-234-mutation-test.py`](../../tools/T-234-mutation-test.py) (new). `python3 tools/T-234-census.py --check` exits **0**; `--self-test` and the emitter's `--self-test` exit **0**; the mutation test exits **0** with **0** mutations failing every named test. Document gates run: `check-markdown-tables.py` **0**, `check-corpus-links.py` **0**, `check-corpus-identifiers.py` **0**, `check-challenge-index.py` **0**, `check-kotlin-format-strings.py` **0**, `check-result-file-hygiene.py --departures/--saturated/--prose` **0**. `check-entry-points.py` reports **2** `MISSING-ROW` defects, both on **another agent's uncommitted** result files (`T-254`, `T-258`) and neither reachable from anything this claim touched. **No Gradle suite run**: this task compiles no Kotlin and two concurrent agents held the box |
| **Conditions** | The corpus at the `baselineRef` each result file records, plus this iteration's edits. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed. Counts are integers |
| **Consumes** | [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) and [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) (the two discharges this census is about), [`C-0146`](C-0146-coupled-cells-at-the-two-length-raster.md) (`112 bp` restored as a row span), [`C-0151`](C-0151-closing-raster-selection.md) (the drawable `102 / 109` raster), [`C-0154`](C-0154-honeycomb-grillage.md) and [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (the half `C-0141` did not supply, and its own discharge), [`C-0144`](C-0144-honeycomb-correction-supersession.md) (the census and its five families), [`C-0127`](C-0127-format-string-repair.md) and [`C-0150`](C-0150-departure-spelling-set-and-the-wall-clock.md) (mutation-test in both directions), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate) |
| **Raises** | [`CH-0229`](../challenges/CH-0229-a-census-assumes-a-premise-is-withdrawn-once.md) — `C-0144`'s census is built on the assumption that a premise is withdrawn **once**, and the corpus has now withdrawn one in halves two iterations apart — and [`CH-0230`](../challenges/CH-0230-the-debt-line-grows-when-the-documents-are-corrected.md) — the advisory `T-233 debt` line is a count over a **moving** corpus whose every increase over 40 revisions is a synthesis pass, so it is not a measure of debt and the split does not change that sign |

---

## The claim, in one line

A census is defined by **the discharge it is about**, and a token that spans two discharges belongs to two censuses —
so the thing `tools/T-234-census.py` could not represent was never a regular expression,
it was the single global `POINTERS` tuple that made every family belong to one correction.

---

## 1. The cheap bounds, and the second one is why this is not a keyword arms race

| bound | cost | measured | what it decided |
|---|---|---|---|
| **1** — dump every occurrence of the ambiguous family with a window and **read all 38** | minutes | **17** structural-model, **14** absence-claim, **7** attributive | the census picks the predicate, not taste; and it is the ground truth any false-positive rate needs |
| **2** — ask whether a pattern *can* be right | seconds | the `PLACEMENT` pattern is **one string** and `C-0141` discharged **half of what it matches** | no regular expression over that string can be right. What has to change is that a **family carries its own pointer set** — one field and one map |
| **3** — price the gate before widening it | seconds | gating the grillage half on `C-0154`/`C-0167` would need pointers written into claims this task does not own | report that half on its own line, ungated, naming the census it belongs to (`CLAUDE.md`'s own residue-beside-the-gate rule) |
| **4** — count the `drawable` token before writing a rule for it | seconds | **20 of 24** name `C-0151`'s drawable `102 / 109` raster; **4** name `C-0119`'s *"drawable at a uniform width"* | one word, two statements, and the ratio says which way the family is now wrong |
| **5** — measure the OBVIOUS cure before writing it | seconds | sentence-scoping the honeycomb context drops **56 of 103** `WIDTH` occurrences, most of them genuine | rejected, on `CLAUDE.md`'s own ground that *a predicate can always be narrowed until the tree is clean* |
| **6** — measure the false-positive rate over **history** | seconds | one loop over `git show <commit>:<file>`, 40 revisions | the number that makes a split believable, and no solve |

---

## 1b. The baseline, which moved twice while it was being measured

A census whose scope includes `TASKS.md` has a reading, not a value.
All four of these are the **same tool** against the corpus as it stood when each was taken,
and the chain is stated because no one of them is *"the"* before-number:

| state | gate defects | `T-233 debt` |
|---|---|---|
| the standing emitter's **committed** classification, against the corpus as it now stands | **41** | 37 |
| the **inherited** uncommitted regeneration found in the working tree at the start of this task | **18** | 41 |
| a **fresh** regeneration by the standing emitter, against the corpus as it now stands | **5** | 41 |
| after `T-260`/`T-262` | **0** | **24** |

The inherited file was a legitimate regeneration that had itself gone stale — it was taken before three
further claims entered the corpus — which is `CH-0182` happening while the task that reads it is running.
It is **not** kept: the shipped classification is a fresh regeneration by the repaired emitter,
carrying six hand overrides that now survive it.

**And it happened a third time, to this claim.**
`C-0176` is in scope, and its §2 and §3 tables quote the families' own example sentences in order to say which reading each is —
so writing it added **13** occurrences to the census it is about, every one `CORRECT`.
The claim is therefore registered in the emitter's `CORRECTING` set on the same ground `C-0144` is,
and **every family split in this claim is quoted over the corpus WITHOUT those 13**,
which is the number that does not move when the claim is edited.
The result files carry both readings.
The general rule was measured and rejected: exempting every occurrence inside a quoted span covers 8 of the 13
and reclassifies **9 existing** occurrences across **6 other files** (5 `MOVED`, 3 `DISCHARGED`, 1 `SURVIVING`),
which would excuse a deliverable that quotes a withdrawn sentence as its own assertion.

Of the **5**, three are resolved by the predicates —
`C-0167`'s headline and `C-0172`'s `LatticeTag` row are `GRILLAGE` (`T-260`),
`C-0171`'s coupling-threshold row is `ROW_SPAN` (`T-262`) —
and two by a hand override with a stated reason,
`TASKS.md`'s `T-9` row (a square-lattice oxDNA design) and `C-0167`'s five-gates reproduction row.

---

## 2. `T-260` — the partial discharge, and what it is a data structure for

`C-0141` supplied the honeycomb **station lattice**, the **plan ceiling**, the **placement family** and the price of an **oblique root**.
It did **not** supply a **grillage**: `OrigamiGrillage` never reads `layers`,
so every coupled cell in this corpus stayed a smeared single-layer square-lattice solve
until `C-0154` built one (`T-253`) and `C-0167` re-graded onto it — **two iterations later, by different claims**.

One token, two statements, two correcting claims, two dates.
The census had **one** `POINTERS` tuple, so a sentence about the grillage was read as a `C-0141` debt and flagged.

The repair is three things and none of them is a new pattern:

1. **`DISCHARGES`** — a map from a discharge name to its pointer set, with `SUBJECT = "C-0140/C-0141"` naming the one this census is *about*.
2. **A family carries its own discharge.** `FAMILY_DISCHARGE` sends `GRILLAGE` to `C-0154/C-0167`, and `SQUARE`/`ROW_SPAN` to **no discharge at all**.
3. **The gate reads only its own subject**, and prints the other families beside it with the census they belong to.

The refinement that assigns the family is a **positive** test in each direction, so it can be wrong in a way a test can catch:

| rule | reading | fires on | does not fire on |
|---|---|---|---|
| the token's neighbourhood names the **structural model** (`grillage`, `OrigamiGrillage`, `CrossoverLayout`, `smeared`, `coupled cell`, `lattice machinery`, `crossover combinatorics`, `equivalent sheet`) | `GRILLAGE` — the half `C-0141` did not supply | *"every coupled cell in this corpus is a smeared single-layer square-lattice solve"* | *"every plan ceiling, phase result and placement in this corpus is single-layer square-lattice"* |
| the token is **attributive** of an object that genuinely is one (`sheet`, `tile`, `number`, `design`, `question`, `d =`), emphasis stripped first | `SQUARE` — not an assertion about the corpus's inventory | *"40.0 × 40.35 nm single-layer square-lattice sheet"* | *"…placement in this corpus is single-layer square-lattice"* |
| otherwise | `PLACEMENT` — the absence `C-0141` supplies | *"the honeycomb has no station lattice, no plan ceiling and no placement family"* | — |

**The structural-model test needed its own, tighter radius, and the sweep is what found it.**
It is a **proximity** test, not a phrase test.
At the refinement window of 300 characters it reached a *"coupled cell"* **253 characters away, in a different sentence**,
and read `ANSWERS.md`'s own *"every plan ceiling, station lattice, crossover phase and placement in this corpus is single-layer square-lattice"* as a grillage statement.
Swept, the split is a **plateau**:

| radius (characters) | `GRILLAGE` | `PLACEMENT` | `SQUARE` |
|---|---|---|---|
| 80 | 17 | 13 | 8 |
| 100 | 17 | 13 | 8 |
| **120** | **17** | **13** | **8** |
| 150 | 17 | 13 | 8 |
| 200 | 18 | 12 | 8 |
| 300 | 19 | 11 | 8 |

`STRUCTURAL_WINDOW = 120` is the middle of a flat region, not a fitted number,
and the plateau's value **reproduces the hand reading's 17 exactly**.

**And the two file-set patches are gone.**
`C-0152` and `C-0154` were registered in the emitter's `CORRECTING` set *by comments that name them as false positives*;
`OUT_OF_SCOPE_FILES` named the two files that happened to carry an attributive use.
Both are now carried by the predicate: **4 file-set entries replaced, gate still 0.**

---

## 3. `T-262` — the restatement, and the answer is BOTH branches

`C-0140` withdrew *"a honeycomb row length asserted as a uniform tile width"*;
`C-0146` restored `112 bp` as a **row span** (*"the width that threatened it is not a width"*)
and `C-0151` restored `drawable` as the name of the **drawable `102 / 109` raster**.

The `WIDTH` refinement is a **governing-noun** test, nearest wins, because the restoring sentences name **both**:

| sentence | nearest governing word | reading |
|---|---|---|
| *"honeycomb at 10.5 bp/turn, 112 bp span"* | `span`, +7 | `ROW_SPAN` |
| *"every x-raster row spans 112 bp = 38.08 nm and the 116 bp = 39.44 nm extent is a stagger"* | `spans`, −6 (against `extent`, +45) | `ROW_SPAN` |
| *"the honeycomb tile is 15 rows × 4 layers × 112 bp"* | `× 4`, −12 (against `rows`, −22) | `WIDTH` |
| *"so neither 112 bp nor 119 bp is a uniform width"* | `width` | `WIDTH` |
| *"at the drawable 102 / 109 raster the count is 10"* | the drawable-raster phrase | `ROW_SPAN` |
| *"overturned in the reading `drawable at a uniform width`"* | no raster | `WIDTH` |

`WIDTH` **46** against `ROW_SPAN` **57**: the family the census gates is now **44.7 %** of what it was.

**The second instance is a LATTICE question and the tool refuses to guess it.**
`TASKS.md`'s `T-9` row is a **square-lattice** oxDNA statement (15 duplexes, 49 crossovers, the 4/3 parity split — `C-0157`/`C-0160`),
and its line's nearest honeycomb word is **3 425 characters away**, in a different sentence of the same queue row.
Sentence-scoping the context would have dropped 56 of 103 occurrences, so instead the census **measures** the distance,
reports every occurrence beyond `CONTEXT_REMOTE = 1000` characters as `REMOTE-CONTEXT`,
and a **hand override** settles it.
**7** occurrences are reported; **6** are settled by hand with a stated reason;
**5** of the 6 are the square-lattice collision and **1** is a five-gates reproduction row naming the state a reading was taken at.

**And the emitter's docstring is now true.**
It has promised since iteration 34 that a hand override *"survives"*; it did not — the emitter built its table from scratch and never read the file it overwrote.
It reads it now, and it keys the override on the occurrence's **neighbourhood** — file, family, token and a 40-character window centred on the token — **not** on its index,
because `TASKS.md` gains rows every iteration and an index is a dated object.
Two independent regenerations carry all **6** overrides over; one whose neighbourhood has been rewritten is **dropped and reported**, never silently moved;
and two overrides sharing one key are reported as **ambiguous** rather than resolved.

---

## 4. The false-positive rate, measured over HISTORY as well as over the tree

**Over the tree**, against the hand reading of all 38 `PLACEMENT`-family occurrences: the predicate agrees on **36**, disagrees on **2**, and **0** of the disagreements change a class that matters —
one is `TASKS.md`'s own `T-260` row, which reads `GRILLAGE` because it quotes the word *grillage* while describing the split (either reading is defensible, and the row is a queue `RECORD`);
the other is a synthesis claim's row, whose class is `RECORD` on the file rule either way.

**Over history** — the measurement the coordinator's prior art on `tools/check-queue-vocabulary.py` prescribes, and the one that makes the split believable.
For each of the last **40** revisions of `ANSWERS.md` and `DECISIONS-FOR-NDI.md`, count the token-family occurrences that are neither struck nor already pointed:

| commit | pass | old predicate | new predicate |
|---|---|---|---|
| `89fd099` | the fifth `ANSWERS.md` synthesis | **+2** | +2 |
| `47ef394` | the sixth synthesis | **+1** | +1 |
| `7b6e465` | iteration 35 | **+1** | **+0** |
| `413659f` | iteration 36 | **+3** | **+1** |
| `49b1a01` | iteration 38 | **+9** | **+1** |
| `d077d55` | the eleventh synthesis | **+3** | +2 |
| `cfbe0cc` | the twelfth synthesis | **+6** | **+3** |
| **total** | | **0 → 25** | **0 → 10** |

The split removes **15 of 25**, and at the largest single document pass **8 of 9**.
The advisory `T-233 debt` line over the whole census falls **41 → 24**.

---

## 5. What this does NOT fix, and it is the sharper half

**Every one of those increases is a synthesis pass — under the new predicate too.**
A correcting sentence has to **name** a withdrawn premise in order to withdraw it,
so a document pass will always add occurrences to a token census of that premise.
The split cuts the rate by about three fifths and **does not change the sign**.

That is `CH-0230`, and it is why the tool now says so in its own output rather than leaving the number to be read as a work list.
It is also the honest answer to `T-262`'s either/or: **both** branches were deliverable, and neither alone would have been true.

**And the predicate still cannot tell which LATTICE a token belongs to.**
`15 × 112 bp` and `15 duplexes, 112 bp` are this corpus's **square-lattice** sheet, on lines that discuss the honeycomb block beside them:
**5** such occurrences, **2** caught by the remote-context advisory and **3** not.
All 5 are hand-settled. The residue is a lattice question, not a width one, and no widening of a governing-noun rule reaches it.

---

## 6. The mutation measurement, in both directions

`C-0127`'s standard is that restoring the old narrow predicate must fail a **named** test;
`C-0150` raised it, because a predicate that can only ever be widened has become a pattern.
`tools/T-234-mutation-test.py` applies **42** mutations — **26** narrowing, **16** widening — to the source of the two tools,
runs the mutated module's own `--self-test` in process, and collects the **named** tests it fails.

| | |
|---|---|
| mutations | **42** (26 `NARROW`, 16 `WIDEN`) |
| mutations failing **no** named test | **0** |
| named self-tests | **126** (95 census, 31 emitter) |
| named tests added by these two tasks | **83** |
| of those, reached by at least one mutation | **77** |

**The first draft of this table indicted itself before it indicted anything else**, exactly as the coordinator predicted:
**nine of 22 rows** "failed nothing", and eight of the nine were the table rather than the tool:
each mutation had been written as an **alternation with the original** (`NEVER + "|" + original`), which is a no-op.
Rewritten to replace each rule **wholesale**, every row bites; the ninth was a genuine gap and needed a new named test.

The six unreached rows are limiting cases and set-membership assertions — *"an empty table carries nothing"*, *"coerce leaves `FOOTPRINT` alone"* —
reached by no plausible mutation of the shipped logic; inventing one would be writing a test for a test.
That is reported and **not** gated; the exit code turns on **silent mutations**, which is the measurement that has content.

---

## 7. The four declared falsifiers

| | falsifier | outcome |
|---|---|---|
| **F1** | the hand reading and the predicate disagree materially, so the distinction is not mechanical | **DID NOT FIRE** — 36 of 38, 0 material |
| **F2** | a mutation fails nothing — a rule no named test asserts | **FIRED on the FIRST DRAFT, on 9 of 22 rows.** Eight were the mutation TABLE — each mutation had been written as an alternation with the original, which is a no-op — and the ninth was a genuine gap in the tests. Repaired both ways; **0 of 42** now |
| **F3** | the split changes the SIGN of the debt line's growth, i.e. the line becomes a debt measure | **FIRED — and its result is §5.** It does not change the sign, it cannot, and the claim that it did would have been false. `CH-0230` |
| **F4** | closing the gate needs a new `CORRECTING` entry, or an edit to a claim this task does not own | **DID NOT FIRE** — 4 file-set entries **removed**, 0 added, and no claim body edited |

---

## 8. Validity range

- **This is a claim about a checker, not about the honeycomb.** No physical number moves, and no claim's verdict moves.
- **The classification is still dated by the corpus** (`CH-0182`, now for the seventh consecutive iteration, and three times inside this one task — the inherited regeneration had gone stale, a sibling's claim arrived mid-run, and this claim entered its own census). What changes is the failure **direction**: a new occurrence in a non-subject family arrives as `SURVIVING` or `RESTATED` rather than as `MOVED`, so a correcting claim no longer arrives as debt.
- **Every family split here is quoted over the corpus excluding this claim's own 13 worked examples.** Over the whole census, including them, the same split is `GRILLAGE` 21 / `PLACEMENT` 13 / `SQUARE` 11 and `WIDTH` 49 / `ROW_SPAN` 60. Both readings are in the result files; the excluding one is the one that does not move when this claim is edited.
- **The census's `--check` is deliberately NOT wired into `tools/verify.sh`.** Its scope includes `TASKS.md`, which every agent edits every iteration, so an occurrence arrives unclassified through no fault of the tree and the gate would go red for a reason nobody caused — `C-0083`'s rule read forward. What is worth wiring is the two `--self-test` invocations, which are in-memory apart from three corpus assertions; that wiring is **requested of the coordinator** and is not done here, because `build.gradle.kts` is not this task's to edit.
- **The hand overrides are a reading**, six of them, each with its reason in `tools/T-234-classification.json`. A reader may disagree one occurrence at a time, which is what that file is for.
- **`SNIPPET_CHARS = 40` cannot separate two identical tokens closer together than that.** The case is rare, it is not silently resolved, and the emitter reports it as `AMBIGUOUS`.
- **No Gradle suite was run.** This task compiles no Kotlin and touches nothing under `src/`; two concurrent agents held the box.
