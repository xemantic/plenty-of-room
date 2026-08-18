# C-0113 — **The self-consistency check now reads CHALLENGE statuses, it fired on the real deliverable at once, and what it fired on was a FALSE POSITIVE — which is the result.** `C-0088` scoped the check to task identifiers explicitly; extended to the corpus's **111** challenge files and the deliverable's **123** references to **78** of them, it reported `CH-0083` carrying three verdicts. The corpus and the deliverable both say `RESOLVED`, and the contradiction was manufactured by **whole-sentence attribution inside a Markdown table row** — a verdict on §6 task 4 read onto a challenge named 180 characters later in the same cell. Two guards, both measured rather than chosen: a **80-character** verdict window and `C-0088`'s own duration guard in the phrasing the queue uses only for challenges. After them: **0 false positives, 0 stale challenge statuses, 0 self-contradictions.** And the extension found a **shadowed regex** that had been silently overriding `open_assertions`'s word list since `C-0080` wrote it

| | |
|---|---|
| **Task** | [`T-183`](../tasks/T-183-challenge-status-self-consistency.md) — teach the deliverable's self-consistency check to read challenge statuses, not only task statuses |
| **Leaf** | — (a process claim; it guards the deliverable that reports every leaf) |
| **Verification type** | **logical**, with **83** executable self-tests in [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py), every new one written before its implementation and watched to fail |
| **Verdict** | **PASS on all four predicates.** (1) `status_words` reads the challenge vocabulary — `UPHELD`, `WITHDRAWN`, `STANDS`, `OVERTURNED` as settled, `RAISED` as open — case-sensitively, because *"the recommendation stands"* and *"raised by `C-0107`"* are the commonest verbs in this repository's prose and neither is a verdict. (2) `self_contradictions` fires on a `CH-` identifier given two verdicts and is silent on one merely cited. (3) A corpus comparison exists, with **each challenge file's own `**Status**` row** as the authority. (4) **The false-positive count against the committed `ANSWERS.md` is zero**, which is the predicate the task declared as its own falsifier and the one that cost the work. |
| **Maturity** | **Below TRL 1–3: nothing here is physics.** No number is derived, no solver runs, no unit is engaged. The claim is about a document checker. |
| **Provenance** | [`gpd/results/T-183-challenge-status-self-consistency.json`](../results/T-183-challenge-status-self-consistency.json), emitted by the retained [`tools/T-183-emit-result.py`](../../tools/T-183-emit-result.py). Code: [`tools/trace-answers.py`](../../tools/trace-answers.py), tests [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py), wired into `./gradlew test` through the standing `testDeliverableTracer` task. |
| **Conditions** | The corpus at iteration 23: **111** challenge files, **81** with a declared `**Status**` row, **65** indexed in `gpd/challenges/README.md`. `ANSWERS.md` as committed at `bb4ec0c`. Four sibling agents were working this checkout while this ran and none of their output is anticipated here. |
| **Consumes** | [`C-0106`](C-0106-fourth-answers-synthesis.md) (which raised it, and found the two live instances by hand), [`C-0088`](C-0088-does-the-deliverable-agree-with-itself.md) (the check being extended, and its two guards), [`C-0080`](C-0080-third-answers-synthesis.md) (the third drift class and the false-positive doctrine), [`C-0078`](C-0078-status-drift-in-the-deliverable.md) and [`C-0067`](C-0067-answers-reconciliation.md) |
| **Constrains** | `tools/trace-answers.py` only. **No claim, challenge, number or verdict anywhere in the corpus is contradicted, and none is examined.** No challenge is raised. |

---

## 1. The cheap bound, and it changed the shape of the deliverable

Before any matcher was written, one `python3` one-liner measured the vocabulary the matcher would have to
match.
The task file declares that measurement as its first step and states what it would change.
It changed something:

| | count |
|---|---|
| challenge files in `gpd/challenges/` | **111** |
| of those, carrying a `**Status**` table row | **81** |
| of those, absent from the `README.md` index altogether | **46** |
| rows in the `README.md` index | **65** |

So **the challenge status is not a controlled vocabulary**, and there is no register for challenges
answering to what `TASKS.md` is for tasks —
the file that *looks* like one indexes 65 of 111.
Deliverable 3 therefore changed shape before it was written,
from *"compare the deliverable against the corpus"*
to *"compare it where a status is declared, report the coverage, and stay silent where it is not"*.

**An undeclared status returns `UNKNOWN` and is silent, never guessed.**

---

## 2. The check fired, and it was wrong

Wired in and run against the committed `ANSWERS.md`, the extension immediately reported one subject:

```
512	SELF-CONTRADICTION	CH-0083	DISCHARGED/OPEN/SETTLED
```

which is exactly the class `C-0106` found by hand and exactly what the task was written to catch.
It is also **false**.
The corpus records `CH-0083` as resolved and so does the deliverable, in all four of its mentions.
The three verdicts came from two places, and only one of them is a phrasing problem:

| line | what the checker read | what it is |
|---|---|---|
| 512 | `DISCHARGED` | a verdict on **§6 task 4**, 180 characters before `CH-0083` is named, **in the same table cell** |
| 512 | `RESOLVED` | correct, twice |
| 682 | `open` | *"raised **open in iteration 16** and **RESOLVED in iteration 17**"* — a duration and its own closure |
| 979 | `RESOLVED` | correct |

The first is the one that matters.
**A Markdown table row is one physical line carrying several independent statements**, and the check's
original unit was the sentence —
so a row that gives a *task* a verdict and then names a *challenge* as provenance had the task's verdict
read onto the challenge.
That is not a phrasing accident in one row; it is the attribution rule meeting the document's own format,
and it would recur on every verdict table the deliverable contains.

The second is `C-0088`'s guard 2 — *"a duration is not a status"* — in a phrasing the queue uses only for
challenges: `_HISTORICAL` matched `open since|for|from` and the deliverable writes `open in iteration 16`.

---

## 3. Two guards, and the window was measured

**Guard 1 — a verdict attaches to the identifier it is NEAR.** `_VERDICT_WINDOW = 80` characters.

The number is a measurement, not a taste, and the sweep is emitted at every rung in the result file so the
choice can be re-audited without re-running anything:

| verdict window (characters) | subjects reported | |
|---|---|---|
| 40 | **0** | |
| 60 | **0** | |
| 80 | **0** | **adopted** |
| 120 | **0** | |
| 200 | 1 | `CH-0083`, false |
| 400 | 1 | `CH-0083`, false |
| unbounded (the original rule) | 1 | `CH-0083`, false |

Every phrasing the deliverable actually uses puts the verdict within ~30 characters of its subject —
*"(`T-45` is still unmeasured)"*, *"**`CH-0083` is RESOLVED**"* —
and the misattribution that had to be excluded sits at 180.
**80 is the crossing with margin**, and the sweep shows there is a plateau to sit on rather than an edge to
balance on.

**Guard 2 — `open in iteration N` is a duration**, one alternative added to `_HISTORICAL`.

Both are pinned by tests that were **watched to fail** with the guards reverted:

```
FAIL a verdict far from its subject in one table row does not attach:
     expected [], got [SelfContradiction(task='CH-0083', verdicts={'DISCHARGED', 'SETTLED'}, …)]
FAIL 'raised open in iteration 16 and RESOLVED in 17' is history, not a contradiction:
     expected [], got [SelfContradiction(task='CH-0083', verdicts={'SETTLED', 'OPEN'}, …)]
```

**The false positive is the deliverable of this task, not an obstacle to it.**
`C-0080`'s standing finding is that *a drift checker's false positives cost more than its true ones, because
the tool exists in order to be believed*, and the task declared a single false positive as grounds for not
shipping the extension at all.
What it cost was two guards; what it bought is that the next agent can believe the number.

---

## 4. The corpus half, and why its bias is the right way round

`challenge_status_of` reads a challenge file's `**Status**` cell, case-**insensitively** — unlike the prose
matcher, because this is a declaration in a known cell of a known table, and the corpus writes the same
verdict as `Upheld`, `UPHELD` and `upheld in part`.

**`OPEN` wins a tie**, and that is deliberate.
A status cell routinely records both a state and an outcome —
*"raised. **No number in `C-0023` moves**"* is a challenge nobody has adjudicated whose sentence also reports
what it did not move —
so the reader classifies it `OPEN`.
The consequence is that **57** of the 81 declared statuses read `OPEN` and **24** read `CLOSED`,
which over-reports how much of the corpus is genuinely live.

That is the conservative direction and it is chosen for one reason:
**the check only ever fires when the corpus says `CLOSED`.**
A false `CLOSED` would produce a false positive — the failure mode this claim is mostly about —
and a false `OPEN` merely produces silence.
So `declaredOpen` is an **upper bound** on the genuinely open set and must not be quoted as a census of it.

---

## 5. What the extension found on the way: a shadowed regex

`_OPEN_WORD` was declared **twice** at module level in `tools/trace-answers.py` — once for
`open_assertions` and once, ninety lines later, for `status_words` —
so the second silently overrode the first and **`open_assertions` has been running on the
self-consistency check's wider word list since `C-0080` wrote it**.

Python has no compiler to say so.
It is the same family as `CLAUDE.md`'s note that a `private` top-level Kotlin declaration does not scope to
its file: *a redeclaration is silent in both languages and the compiler is the only difference.*

**What it was worth: nothing published moves.** Both word lists give **0** open assertions against the
committed `ANSWERS.md`, measured before the repair.

**And the direction it happened to run is favourable.** The shadowing list is the wider one, and it contains
`unmeasured` — which is the exact word of `C-0080`'s own live instance, *"(`T-45` is still unmeasured)"*.
So the assertion check keeps the wider list **deliberately** now, named `_OPEN_WORD_ASSERTION`, with the
verdict list named `_OPEN_WORD_VERDICT` beside it and a test on each.

---

## 6. Verdicts, all four predicates

| predicate | result |
|---|---|
| 1 — the challenge vocabulary is read, mapped onto a verdict | **PASS** — `UPHELD`/`WITHDRAWN`/`STANDS`/`OVERTURNED` → settled, `RAISED` → open, all case-sensitive, negation-guarded |
| 2 — `self_contradictions` reaches `CH-` identifiers | **PASS** — fires on two verdicts, silent on a citation |
| 3 — a corpus comparison exists for challenges | **PASS**, with **81 of 111** coverage reported rather than assumed |
| 4 — zero false positives against the real deliverable | **PASS**, after two guards it took to get there |

Final state of the three retained checkers on the committed deliverable:

```
# 1050 tokens: 937 CITED, 113 ELSEWHERE, 0 ABSENT
# 0 open assertion(s), 0 contradicted by TASKS.md
# 111 challenge(s), 81 with a declared status, 0 open assertion(s) contradicted
# 0 task(s) the deliverable contradicts itself about
```

---

## 7. Validity range, and what this does NOT do

- **It reads statuses, not substance.** A deliverable that reports a challenge's *conclusion* wrongly while
  naming its status correctly passes every check here. `C-0080`'s third drift class — a **superseded**
  standing value whose owner still states it — remains unmechanised, and `C-0067` states why: it needs a
  `superseded-by` edge at *statement* granularity that no claim carries.
- **`declaredOpen = 57` is an upper bound, not a census** (§4).
- **The 30 challenges with no declared status are invisible to the corpus half.** Making them visible is a
  corpus convention change — a `**Status**` row in every challenge file — not a checker change, and it is
  queued rather than done here, because editing 30 claim-adjacent files while four agents work the tree is
  the kind of edit that loses a sibling's work.
- **The `README.md` index is 46 rows behind the corpus.** Recorded as an observation; no check depends on it,
  precisely because it cannot be relied on.
