# C-0182 — **A CENSUS NOW REFUSES A FAMILY WHOSE DISCHARGE IT CANNOT NAME, AND AN ABSENCE HAD BEEN READ AS AN ANSWER.** `tools/T-234-census.py` emitted **8** family names and declared **3**; the other five were answered by `FAMILY_DISCHARGE.get(family, SUBJECT)` — a default nobody wrote down — and the two that later needed a split, `PLACEMENT` and `WIDTH`, were among them for **eight iterations**. The rule is a data structure that cannot be constructed with a discharge naming no claim and cannot be asked about a family naming no discharge; the report carries a **third** verdict state, and a declared family with no occurrence reads `VACUOUS` rather than unanswerable. Retrospectively over the census's own **3** revisions it fires **15** family-revision refusals — **2 GENUINE**, **13** one-line prompts, **0 false positives**, the rule reading a **declaration** and therefore unable to mistake one for the other — and at the first revision **25 of 264** occurrences were already misdated. **24 mutations, 0 survivors.** Not one census record moves: **391** records byte-identical, and the gate's defect count is **unchanged — 21 before and 21 after** on one corpus read by two tools, of which **18** are `T-282`'s at the baseline ref's own committed corpus, one is a concurrent agent's claim and **two are this claim's own**, one of them a **link target** rather than a statement

| | |
|---|---|
| **Task** | [`T-281`](../tasks/T-281-name-the-discharge.md) |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests, a mutation test in both directions, and a retrospective measurement over the census's own revision history |
| **Verdict** | **PASS on all seven predicates.** `F1`–`F7` are reported in §5 |
| **Raises** | nothing against a standing claim. [`CH-0229`](../challenges/CH-0229-a-census-assumes-a-premise-is-withdrawn-once.md), which raised this task, is **ANSWERED** in the half it left open |
| **Maturity** | TRL 1–3 process artifact. **No physics changed** and not one physical number moves; what moves is whether a census can be written without naming its own date |
| **Provenance** | [`tools/census_discharges.py`](../../tools/census_discharges.py) (new, **45** named tests), [`tools/T-234-census.py`](../../tools/T-234-census.py) (**135** named tests, up from 123, and it runs the registry's 45 too), [`tools/T-281-mutation-test.py`](../../tools/T-281-mutation-test.py) (new, 24 mutations), [`tools/T-281-history.py`](../../tools/T-281-history.py) (new, the retrospective), [`tools/T-281-emit-result.py`](../../tools/T-281-emit-result.py) (new); result `gpd/results/T-281-name-the-discharge.json`, `baselineRef` `9620d3ef3f21aa4038055a5752cd637f49e62954`. `python3 tools/census_discharges.py --self-test` exits **0**; `python3 tools/T-234-census.py --self-test` exits **0**; `python3 tools/T-281-mutation-test.py` exits **0**; `python3 tools/T-281-history.py` exits **0**. Gate readings in §6 |
| **Conditions** | Documents and Python tools only. No Kotlin source is touched, no Gradle task is added, no result file other than this task's own is emitted, and no physical number in the corpus moves. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K. Every number here is an integer count |
| **Consumes** | [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (`T-260`/`T-262`, the representation this supplies the discovery for), [`C-0144`](C-0144-honeycomb-correction-supersession.md) (the census and its five families), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) and [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) (the subject discharge), [`C-0154`](C-0154-honeycomb-grillage.md) and [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (the second discharge), [`C-0127`](C-0127-format-string-repair.md) and [`C-0150`](C-0150-departure-spelling-set-and-the-wall-clock.md) (mutation-test in both directions), [`C-0177`](C-0177-queue-status-vocabulary.md) (a convention is not a mechanism) |
| **Constrains** | every census this repository writes hereafter, and in particular the next regeneration of `tools/T-234-classification.json` ([`T-282`](../../TASKS.md)) |

---

## The claim, in one line

`CH-0229` asked for *"a rule, one line, in whatever writes the next census: name the claim that discharges each family, one family at a time, and refuse a family whose discharge you cannot name"* —
and the line that had to go was not an addition but a **deletion**: `FAMILY_DISCHARGE.get(family, SUBJECT)`, in which the second argument answers a question nobody asked.

## 1. The cheap bounds

| bound | cost | measured | what it decided |
|---|---|---|---|
| **1** — count the family names the census EMITS against the names it DECLARES | one run | **8** emitted, **3** declared | five families were answered by a default; the defect is a **completeness** defect and its repair is five lines, not a predicate |
| **2** — ask how many revisions the census has, before designing the retrospective | one `git log` | **3** | the whole history is three `git archive` extractions and no JVM, so the measurement is affordable and there is no reason to argue instead of measure |
| **3** — ask what a false positive of this rule could even be | seconds | the rule reads a **declaration** | it cannot mistake a declared family for an undeclared one, so its false-positive rate against a declared registry is **0 by construction**, and what the history can measure is the **firing count** and how much of it was load-bearing |

Bound 3 is the one that shapes the claim: a declaration requirement is not a drift checker, so the honest number is not a rate but a **cost** — one line per family, once — beside a count of how often the default was wrong.

## 2. The rule

`tools/census_discharges.py` is a module because *"whatever writes the next census"* is the subject.
`CLAUDE.md` asks that the implementations a rule must reach be **enumerated** before *"the rule now lives once"* is written; enumerated here they are **1**, which is exactly when a shared module is cheap rather than speculative.

- **A discharge must name a claim.** `DischargeRegistry` refuses at construction a non-`None` discharge whose pointer tuple is empty, a family naming a discharge the registry does not define, and a subject that is not itself a defined discharge. The `None` discharge is the explicit *not a debt at all* and may name **no** claim — which is a declaration, not an absence.
- **The getter refuses.** `discharge_of` raises `UndeclaredFamily`, and the refusal carries `CH-0229`'s own question in its message: *name the claim that discharges it — and check that it is ONE claim: a premise can be withdrawn in HALVES, and a family that spans two discharges belongs to two censuses.* That sentence is the **discovery** half, delivered at the only moment anyone is looking.
- **The report does not.** `report` returns `DECLARED`, `VACUOUS` or `UNDECLARED` and never raises. `CLAUDE.md`: *a getter must refuse and a report must not*, and *a report then needs a third verdict state* — an empty violation list on an unanswerable family is indistinguishable from a clean one.
- **An empty domain is VACUOUS, not withheld.** A family declared and matched nowhere reads `VACUOUS` and is **clean**. The other half of the same `CLAUDE.md` entry, and it matters in the direction that keeps a gate switched on: reporting a correct declaration as unanswerable is how a gate acquires false positives it does not have.
- **An `UNDECLARED` row's discharge slot is a sentinel and not `None`**, because `None` is a legitimate declared discharge and a report in which *"nobody said"* and *"somebody said: none"* render alike is the conflation being removed.

The census consumes it: `FAMILY_DISCHARGE` is now complete at **8** entries, `discharge_of` delegates, and `check()` runs the family report **before** `census()` — because `census()` asks for a discharge and the getter refuses, and a traceback is not a report.

## 3. The retrospective

Every revision of `tools/T-234-census.py` extracted with `git archive` — never `git checkout`, which on this shared checkout reverts another agent's work — and run against the corpus **at that same revision**.

| commit | iteration | emitted | declared | refused | of those GENUINE | occurrences | misdated |
|---|---|---|---|---|---|---|---|
| `eade1a6` | 34 | 5 | **0** | 5 | **2** (`PLACEMENT`, `WIDTH`) | 264 | **25** |
| `3e71284` | 42 | 8 | 3 | 5 | 0 | 391 | 0 |
| `9620d3e` | 43 | 8 | 3 | 5 | 0 | 391 | 0 |

**15 family-revision refusals, 2 GENUINE, 13 PROMPT, 0 false positives.**
A refusal is `GENUINE` where occurrences the revision filed under that family are ones today's reading puts under **another** discharge — that is, where answering the forced question would have **changed** the census.
It is a `PROMPT` where the default was right and the answer is a single claim already in the corpus, which costs one line.

The number with content is the last column: at the census's **first** revision, **25 of its 264 occurrences** — `GRILLAGE` 1, `SQUARE` 4, `ROW_SPAN` 20 — already belonged to a discharge other than the one it applied, and the census carried the earlier date for all of them from iteration **34** to iteration **42**.

## 4. What this does NOT do

The rule forces the question and cannot answer it.
Nothing mechanical can tell an author that the single claim they are about to name is only **half** of the discharge —
that is a reading, and `C-0176` is the demonstration that it takes one.
What changes is that the question is now **asked**, at the moment the family is written, by an object that will not proceed without an answer.

The defects `python3 tools/T-234-census.py --check` reports are `T-282`'s and are **not** repaired here: four unclassified `TASKS.md` rows, eight unclassified occurrences in [`C-0175`](C-0175-drawable-raster-rim.md), six wrong-discharge reports on rows written before `C-0176` split the families — **18** at the baseline ref's own corpus — and a **nineteenth** that appeared in the working tree during this iteration, an unclassified `drawable` in a concurrent agent's [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md).
Regenerating `tools/T-234-classification.json` mid-iteration sweeps in whatever is in flight (`C-0176` §1b) and two other agents held the tree — and the nineteenth defect **is** that fact, arriving while this was being written, which is `CH-0182` for the tenth consecutive iteration.

**And the twentieth and twenty-first are this claim.** Its own text carries **2** occurrences of the census's `drawable` token, because a claim about a census has to quote the census's own tokens in order to describe them; `C-0176` had 13 and published both readings, and so does this one. The control for `F5` is therefore **one corpus and two tools**, derived rather than asserted: `HEAD`'s own census tool and this task's, both run against the same working tree, both reading **21**. A before/after taken across two *corpora* would have charged the sibling's claim and this claim's own sentences to the rule.

**One of the two is not a statement at all, and that is a finding with a mechanism.** `blank_identifiers` blanks `C-0175` and does **not** blank the file it names, so the `WIDTH` family's `drawable` token fires inside the slug `C-0175-drawable-raster-rim.md` — a **link target**. Measured, **5 of the corpus's 40** `drawable` occurrences are that shape, among them the sibling's `C-0180` and one of this claim's two. It is not repaired here: the predicate's records and indices are exactly what [`T-282`](../../TASKS.md) has to regenerate against, and moving them mid-iteration is the trap this section already declines. Filed as **`T-285`**.

## 5. Acceptance predicates

| | predicate | reading | verdict |
|---|---|---|---|
| **F1** | the getter refuses an undeclared family, and returns the discharge of a declared one — including the explicit `None` | 6 named tests, both directions | **PASS** |
| **F2** | a discharge must name a claim; an undefined discharge, an empty pointer set, a `None` that names one, and an undefined subject are all refused at construction | 9 named tests | **PASS** |
| **F3** | the report has three states and does not refuse; `VACUOUS` is clean and is never reported as `UNDECLARED` | 20 named tests, including *"a VACUOUS family is NOT undeclared"* and *"an UNDECLARED row's discharge is the SENTINEL, not None"* | **PASS** |
| **F4** | with an undeclared family in play the census refuses to be written and `--check` names it | asserted structurally (`check()` reports before `census(root)`) and by the mutation that reorders them | **PASS** |
| **F5** | nothing else moves | **391** census records byte-identical against `HEAD`'s own tool on the same corpus; gate **21** defects before and **21** after — the control is ONE corpus and TWO tools, because a sibling's claim and this one's own text both entered the tree mid-iteration and would otherwise be charged to the rule. At the baseline ref's own committed corpus the reading is **18** | **PASS** |
| **F6** | every rule fails a named test when mutated, wholesale, with the count that fails nothing reported | **24 mutations, 0 survivors, 0 failing nothing**, over a measured and subtracted baseline of 2 | **PASS** |
| **F7** | the retrospective is measured over every revision | **3** revisions, **15** refusals, **2** genuine, **0** false positives | **PASS** |

## 6. Verification, and the gates' own readings

`C-0158`'s rule — a claim that touches a gate records the gate's **reading**, the way every other claim records a suite result:

```
python3 tools/census_discharges.py --self-test   ->  self-test: 45 test(s), 0 failure(s)
python3 tools/T-234-census.py --self-test        ->  self-test: 0 failure(s)   (135 named tests + the registry's 45)
python3 tools/T-234-census.py --check            ->  GATE 21 defect(s)         (21 under HEAD's OWN tool on the same tree; 18 at HEAD's corpus)
python3 tools/T-281-mutation-test.py             ->  24 mutation(s), 0 survivor(s)
python3 tools/T-281-history.py                   ->  15 refusal(s), 2 GENUINE, 13 PROMPT, 0 false positive(s)
python3 tools/T-234-mutation-test.py             ->  42 mutations, 0 failing nothing, exit 0
python3 tools/T-234-emit-classification.py --self-test -> self-test: 0 failure(s)
```

**One piece of collateral, and its own harness caught it.**
Completing `FAMILY_DISCHARGE` falsified a self-test in `tools/T-234-emit-classification.py` whose **name** stayed true:
*"every family the census does not gate has a coercion"*, written as `set(FAMILY_DISCHARGE) == set(FAMILY_CLASS)` —
a proxy that held only while the map was **partial**.
It is `CLAUDE.md`'s *assert the premise a derivation rests on, never a proxy for it*, and it was found by `tools/T-234-mutation-test.py`'s **baseline** check rather than by reading.
The expression is now the sentence.
Two mutation anchors in the same table had to move with it, and both keep their original **meaning** rather than their original text.

## 7. Validity range

- The rule is about **declaration**, not about correctness: it cannot tell that a named discharge is the wrong one, nor that it is half of one. Its whole value is that the question is asked.
- The retrospective is over the **census tool's** revisions, of which there are three. It is a complete history and it is a small one; the `GENUINE`/`PROMPT` split rests on **one** family pair (`PLACEMENT`, `WIDTH`) and would not survive being quoted as a rate.
- `tools/census_discharges.py` has exactly **one** consumer today. *"The rule now lives once"* is true of the implementations that exist and says nothing about a census written in another language, which is `C-0162`'s standing shape.
