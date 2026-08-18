# T-183 — Teach the deliverable's self-consistency check to read CHALLENGE statuses

**Leaf:** — (process; guards the `A8.2`/`A2.1` deliverable rather than computing anything)
**Raised by:** [`C-0106`](../claims/C-0106-fourth-answers-synthesis.md), the fourth `ANSWERS.md` synthesis
**Verification type:** logical, with executable self-tests
**Units:** none — this task manipulates document text, not physical quantities

---

## Formulate

`C-0088` mechanised *"a task the deliverable gives two statuses"* and scoped it to **task** identifiers
explicitly.
`T-175` then found by hand that **2 of its 12 third-class instances are a CHALLENGE with two statuses** —
`CH-0083` read *open* in `ANSWERS.md`'s §2 verdict table and in its §Task-4 header while the same paragraph
said `RESOLVED` twelve lines below,
and **both halves passed every existing check**,
because the checker's reference pattern matches `T-\d+` and `P-\d+` and nothing else.

The corpus carries **111** challenge files
and their statuses are as load-bearing as a task's:
a challenge that is `UPHELD` has withdrawn something a claim asserts,
and a deliverable that still reports the withdrawn reading is wrong in exactly the way this checker exists to catch.

### Numeric target and acceptance predicate

**Acceptance predicate.**
All four hold, as executable self-tests in `tools/test-trace-answers.py`, written before the implementation:

1. `status_words` recognises the **challenge** status vocabulary — `UPHELD`, `WITHDRAWN`, `STANDS`, `RAISED`,
   beside the `OPEN`/`RESOLVED`/`DISCHARGED` it already carries — and maps each onto a verdict that a
   contradiction can be read on.
2. `self_contradictions` fires on a `CH-` identifier given two verdicts in one document,
   and is silent on a `CH-` identifier merely cited.
3. A **corpus** comparison exists for challenges as it does for tasks: a challenge the deliverable asserts is
   open, that the corpus records as closed, is reported — with the challenge's own file as the authority.
4. **The false-positive count on the real `ANSWERS.md` is zero for every guard that the existing three
   checks already run clean on.** The false-positive budget is the design constraint, not the coverage:
   `C-0088`'s guard 2 (a duration — *"open since"* — is not a status) must carry over, and the standing
   `_ANSWERING` cancellation with it.

**Falsifier.**
If the extension reports a single false positive against the committed `ANSWERS.md`,
the extension is wrong and is not shipped —
`C-0080`'s finding is that *a drift checker's false positives cost more than its true ones, because the tool
exists in order to be believed*.
A second falsifier: if the challenge status vocabulary turns out not to be a **controlled** vocabulary in the
corpus, a corpus comparison cannot be written honestly, and this task must say so rather than guess.

---

## Plan

**Cheap bound first, and it runs before any code.**
Measure the corpus's own status vocabulary before writing a matcher for it:
count the `**Status**` rows across all `gpd/challenges/CH-*.md`, and the rows of the `README.md` index.
If the vocabulary is prose rather than controlled, deliverable 3 changes shape from *"compare"* to
*"compare where a status is declared, and report the coverage"*.
Cost: one `python3` one-liner, against the hours a matcher tuned on a vocabulary that does not exist would waste.

**Method.** Extend `tools/trace-answers.py` in three places, tests first:

- `_TASK_REFERENCE` → a reference pattern that also matches `CH-\d+`, used by `self_contradictions`
  (and **not** by `open_assertions`, whose corpus authority is `TASKS.md` and which has no challenge rows to read).
- `status_words` → the challenge vocabulary, mapped onto the existing three verdicts.
- A new `challenge_status(directory)` reading each challenge file's own `**Status**` row,
  plus `stale_challenge_statuses` beside `stale_statuses`, reported in `main()` unconditionally.

**Justification against cost.**
The alternative is to keep finding this class by hand, which is what `T-175` did — 12 instances, one synthesis,
and 2 of them invisible to all three checks.
`C-0088`'s own lesson is that *a one-off reconciliation cannot converge on this class and a standing check can*.

**What would falsify the approach.**
That a challenge's status is not assertable in a synthesis at all —
i.e. that `ANSWERS.md` never states one — in which case the check is dead code and should not be written.
Checked by counting `CH-` references in `ANSWERS.md` before implementing.
