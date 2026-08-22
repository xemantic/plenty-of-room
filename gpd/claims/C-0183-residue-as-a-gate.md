# C-0183 — **THE QUEUE'S RESIDUE LINE IS A GATE, AND *"IT CANNOT BE MADE CLEAN"* WAS A STATEMENT ABOUT THE PREDICATE AND NOT ABOUT THE QUESTION.** `P-30` left a row whose PROSE carries a closing word printed and ungated, on the ground that `T-261`'s criterion quotes three status words as data — and all three are **backticked** in the row as committed, so blanking inline code spans before the whole-row scan clears it in one line. Measured over **139** revisions of `TASKS.md` the blanked predicate fires **115** row-instances on **7** distinct rows against the unblanked **118** on **8**; every one of the seven is a genuine idiom violation whose repair falsifies nothing, so the **false-positive rate is 0**, and the gate reads **0 rows** on the queue it lands on. **12 mutations, 0 survivors — and 2 survived the first run**, one of them in the *unsafe* direction: blanking a **leading** code span lets the bold run behind it become the cell's leading run and MANUFACTURES a closing verdict in a cell that has none

| | |
|---|---|
| **Task** | [`T-283`](../tasks/T-283-residue-as-a-gate.md) |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests, a mutation test in both directions, and a false-positive measurement over every revision of `TASKS.md` |
| **Verdict** | **PASS on all seven predicates, and the conclusion is GATE rather than *stays advisory*.** `F1`–`F7` are reported in §5 |
| **Raises** | [`CH-0236`](../challenges/CH-0236-a-line-left-advisory-on-a-ground-about-its-own-predicate.md) and [`CH-0237`](../challenges/CH-0237-a-mutation-harness-layout-is-a-premise-of-its-own-measurement.md), both against [`C-0178`](C-0178-leading-verdict-and-row-coverage.md); both **RAISED and REPAIRED in this iteration**, and no number of `C-0178` is disputed |
| **Maturity** | TRL 1–3 process artifact. **No physics changed.** What moves is one line of a gate's output from advisory to blocking |
| **Provenance** | [`tools/queue_verdicts.py`](../../tools/queue_verdicts.py) (`blank_code_spans`), [`tools/check-queue-vocabulary.py`](../../tools/check-queue-vocabulary.py) (**55** named tests, up from 46), [`tools/T-283-residue-history.py`](../../tools/T-283-residue-history.py) (new, the measurement and its hand classification), [`tools/T-283-mutation-test.py`](../../tools/T-283-mutation-test.py) (new, 12 mutations), [`tools/T-283-emit-result.py`](../../tools/T-283-emit-result.py) (new), [`tools/P-30-mutation-test.py`](../../tools/P-30-mutation-test.py) (repaired, `CH-0237`); result `gpd/results/T-283-residue-as-a-gate.json`, `baselineRef` `9620d3ef3f21aa4038055a5752cd637f49e62954`. Gate readings in §6 |
| **Conditions** | Documents and Python tools only. No Kotlin source is touched, no Gradle task is added, **nothing new is wired into `tools/verify.sh`** — `tools/check-queue-vocabulary.py` was wired there by `C-0173`/`C-0177` and what grew is the coverage of what is wired. Units unchanged and untouched. Every number is an integer count except the false-positive rate, an exact quotient |
| **Consumes** | [`C-0178`](C-0178-leading-verdict-and-row-coverage.md) (`P-30`, the residue and the leading-verdict predicate), [`C-0177`](C-0177-queue-status-vocabulary.md) (`P-29`, the vocabulary gate and the measured-predicate standard), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate), [`C-0071`](C-0071-output-element-recommendation.md) (*strike, never delete*), [`C-0158`](C-0158-prose-gate-red.md) (a claim that wires a gate records the gate's reading), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (a mutation that fails nothing is the finding) |
| **Constrains** | every future edit to `TASKS.md`: an upper-case closing word in a row's prose is now a build failure unless it is backticked or lower-cased |

---

## The claim, in one line

`C-0178` §5 says a row whose prose carries a closing word *"cannot be made clean"* and gives `T-261` as the counter-example;
`T-261`'s three words are already inside backticks, so the sentence is true of the **predicate `P-30` had** and not of the question —
and a claim about a checker is dated by the checker's own predicate, which is `CH-0182` and `CH-0229` on a third axis.

## 1. The cheap bounds

| bound | cost | measured | what it decided |
|---|---|---|---|
| **1** — list the **distinct rows** that fire, over all history, before writing any gate | one walk | **8** unblanked, **7** blanked | the classification is a handful of readings rather than a morning's, so the question is decidable and the answer is not *stays advisory* by default |
| **2** — read the row the claim names as its counter-example | one `grep` | its three words are **backticked** | the discriminator between *quoted as data* and *asserted about this row* was already in the corpus's own idiom; the candidate predicate is one line |
| **3** — ask whether the blanking can reach the VERDICT | seconds | `row_verdicts` reads the **unblanked** body | the blanking is confined to the scan, which is what keeps the change from moving the register — and the mutation test later found this is not automatic (§4) |

## 2. The measurement

Every revision of `TASKS.md` reachable from the baseline, scanned in **both** readings:

| | row-instances | distinct rows |
|---|---|---|
| the predicate `C-0178` §5 left advisory | **118** | **8** |
| with inline code spans blanked | **115** | **7** |
| on the queue this lands on, unblanked / blanked | **1** / **0** | — |

The seven that still fire, each with its own reason recorded in `tools/T-283-residue-history.py`:

| row | the closing word in prose | why it is a TRUE positive |
|---|---|---|
| `T-111` | `**ANSWERED by C-0053**` mid-cell, row leading `TODO` | an assertion about the row's own progress written as prose; the row was later rewritten |
| `T-183` | *RESOLVED* in **italics**, quoting another document | backticking it is the corpus's own idiom for a quoted token and falsifies nothing |
| `T-231` | *the honeycomb station lattice was ANSWERED by C-0141* | the shape `CLAUDE.md` records verbatim, whose prescribed repair is lower-casing |
| `T-261` | its own **title** read *a challenge the corpus has since ANSWERED* | lower-cased in the `P-30` document repair |
| `T-268` | *`CH-0207` **CLOSED and REPAIRED*** and *`P1` was found ALREADY DISCHARGED* | a challenge and a deliverable; both lower-cased in the `P-30` repair |
| `T-272` | *`P2` is DISCHARGED over the whole corpus* | a deliverable; lower-cased in the `P-30` repair |
| `T-280` | *Candidate 1 … is **DONE*** | a candidate of a remedy; lower-cased in the `P-30` repair |

**0 of 7 are false positives**, so the measured rate is 0 and the gate is bought.

The two rows the blanking **removes** are both verbatim quotations inside backticks, which is `F4`:
`T-256`'s whole firing is a tool's own output line, `` `line 965 STALE-OPEN CH-0187 CLOSED` ``;
`T-261`'s is the acceptance criterion `C-0178` §5 named.
Both removals are **true negatives**, and no removal hides a live inconsistency.

## 3. What the escape costs, and why it is not an exemption

`C-0176` records that *a set membership silences a symptom; it does not repair a predicate*, and two narrowings were rejected before code on that ground:
exempting a row by identifier is that failure exactly, and scoping the scan to the status **cell** would drop the residue's whole subject, since the closing words live in the goal and subject cells.
Blanking a code span is neither.
It is a statement about **Markdown**, it is what the same function already does one rule earlier for struck spans, and it gives every future row an escape that costs two characters.

The gate's refusal names **both** repairs, because this predicate has two and only the second serves `T-261`:

```
RESIDUE     T-1  leads with 'TODO' and its PROSE carries a closing word: a whole-row
            scan reads CLOSED. The queue writes verdicts in bold UPPER CASE and prose in
            lower, so either lower-case the word, or — if it is a status token quoted
            as DATA — put it in `backticks`, which this scan blanks
```

## 4. The mutation test found two things, and one is a hazard nobody had named

**12 mutations, 0 survivors** — after **2 survived the first run**, which is the finding rather than a gap in the list.

- **The unsafe direction.** *"The blanking touches the scan and never the verdict"* was asserted nowhere in the direction that matters, because no fixture had a code span in **front** of a verdict. Blanking a leading code span does not merely hide a word: it lets the bold run **behind** it become the cell's leading run. Measured, `cell_verdict(" \`note\` **DONE** (iteration 3) ")` is `None` unblanked and `("DONE", "CLOSED")` blanked — so a blanked reader would **manufacture** a closing verdict in a cell that has none, and an open row would read closed. That is the failure direction `CLAUDE.md` records four times as the costly one, and it is now two named tests.
- **The refusal's own words** were asserted nowhere, so a message stripped to `RESIDUE T-1 'TODO' CLOSED` failed nothing. Three named tests now assert that both repairs are named.

**And a third defect, in the harness rather than in the subject** — [`CH-0237`](../challenges/CH-0237-a-mutation-harness-layout-is-a-premise-of-its-own-measurement.md).
The first run of this table read **12 mutations, 12 survivors**, because it copied `tools/` **flat** and the gate resolves its queue as `dirname(dirname(__file__))/TASKS.md`.
The same layout is in `tools/P-30-mutation-test.py`, where it was harmless until this task added self-tests that read the queue — after which **every one of its 24 rows** was `killed` by one and the same `FileNotFoundError`, with no test failing and the headline unchanged.
Both harnesses now build `<tmp>/tools/*.py` beside `<tmp>/TASKS.md` and **measure and subtract** the failures of an unmutated copy; `P-30`'s reading is restored to **24 mutations, 0 survivors** at a baseline of **0**.

## 5. Acceptance predicates

| | predicate | reading | verdict |
|---|---|---|---|
| **F1** | the candidate clears the standing counter-example and reads 0 at `HEAD` | `T-261` cleared; residue **0** rows at the baseline ref and **0** on the working tree | **PASS** |
| **F2** | the blanking touches the SCAN and never the VERDICT | asserted in both directions, including the manufacture hazard §4 names | **PASS** |
| **F3** | the false-positive rate is measured over the queue's own history and every distinct row classified | **139** revisions, 118/8 unblanked, 115/7 blanked, **0 of 7** false | **PASS** |
| **F4** | every row the blanking removes is inspected and is a true negative | `T-256` and `T-261`, both verbatim quotations inside backticks | **PASS** |
| **F5** | each rule fails a named test when mutated, wholesale, with the count that fails nothing reported | **12 mutations, 0 survivors, 0 failing nothing** — 2 survived the first run and both were test gaps | **PASS** |
| **F6** | the gate's own reading is recorded, and nothing new is wired | §6; `tools/check-queue-vocabulary.py` was already in `tools/verify.sh` | **PASS** |
| **F7** | the measurement reports both readings, because this row is inside its own scope | residue at the baseline ref and on the working tree both **0**; the `T-283` row is written in the queue's own idiom and does not fire on itself | **PASS** |

## 6. Verification, and the gates' own readings

```
python3 tools/check-queue-vocabulary.py --selftest  ->  # 55 self-test(s), 0 failure(s)
python3 tools/check-queue-vocabulary.py            ->  # 0 defect(s); 277 leading verdict(s) over 275 row(s) in TASKS.md
                                                       # residue (GATED since T-283 ...): 0 row(s)
python3 tools/T-283-mutation-test.py               ->  # 12 mutation(s), 0 survivor(s)
python3 tools/T-283-residue-history.py             ->  # false positives: 0 of 7 distinct rows
python3 tools/P-30-mutation-test.py                ->  # 24 mutation(s), 0 survivor(s), baseline 0
python3 tools/test-trace-answers.py                ->  all checks passed
```

## 7. Validity range

- The classification of the seven firing rows is a **reading**, held as data in `tools/T-283-residue-history.py` so that a reader can disagree one row at a time; a row that fires and is in neither hand table makes that tool exit 1, so the measurement cannot grow an unexamined firing.
- **0 false positives is a rate over a corpus of one file and 139 revisions**, and it rests on the escape being available. A future row that must carry an upper-case closing word in prose **outside** a code span would make this a permanent one-defect gate, and the honest response then is to demote it again rather than to exempt the row.
- The blanking is deliberately **not** applied to the reader's own fallback scan for a row carrying no leading verdict. Measured over the committed queue that would move **nothing**, and it is asserted as a named test; changing it is `P-30`'s territory, not this task's.
