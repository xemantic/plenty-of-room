# C-0178 — the register reads a row's LEADING verdict, and it now sees every row

| | |
|---|---|
| **Task** | [`P-30`](../tasks/P-30-leading-verdict-and-row-coverage.md) |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests and a mutation test |
| **Verdict** | **PASS.** Four open rows are back in the register and a fifth row is visible to it for the first time; the gate reads **0 defects** on the queue it lands on; **24 mutations, 0 survivors**; and the false-positive rate over the queue's own history is **0 of 138 revisions**, the 9 firings being genuine undeclared coinages |
| **Raises** | [`CH-0232`](../challenges/CH-0232-the-agreement-check-agreed-about-words.md), against [`C-0177`](C-0177-queue-status-vocabulary.md) — its agreement check couples the reader to a list of WORDS and never to the reader's own PREDICATE |
| **Maturity** | TRL 1–3 process artifact. Nothing here is a measurement of the physics; it is a measurement of the register the loop picks its work from |
| **Provenance** | `tools/queue_verdicts.py` (new), `tools/trace-answers.py`, `tools/check-queue-vocabulary.py`, `tools/test-trace-answers.py` (132 named tests, 0 failures), `tools/P-30-mutation-test.py`, `tools/P-30-history.py`, `tools/P-30-emit-result.py`; result `gpd/results/P-30-queue-row-coverage.json`, `baselineRef` `3e71284d5fe2bd05bf3b96ccb32cc20d6ba79ddd` |
| **Conditions** | Documents only — `TASKS.md`, `ANSWERS.md`, `DECISIONS-FOR-NDI.md` and the four Python tools named above. No Kotlin source is touched, no result file other than this task's own is emitted, and no physical number in the corpus moves |
| **Consumes** | [`C-0177`](C-0177-queue-status-vocabulary.md) (`P-29`, the vocabulary gate and its measured predicate), [`C-0173`](C-0173-trace-answers-wired.md) (the reader wired into `tools/verify.sh`), [`C-0088`](C-0088-does-the-deliverable-agree-with-itself.md) (the status check the reader carries), [`C-0071`](C-0071-output-element-recommendation.md) (*strike, never delete*) |
| **Constrains** | every future reading of `TASKS.md` by `tools/trace-answers.py`, and therefore every *"is this task open"* statement in `ANSWERS.md` and `DECISIONS-FOR-NDI.md` |

---

## 1. What was wrong — two defects, and only the first was suspected

**(a) Two predicates about one thing.** `P-29` left the tree with a reader and a gate that disagree by construction.
`queue_status()` decided a row OPEN or CLOSED by scanning the **whole row after the identifier** for a closing word;
`tools/check-queue-vocabulary.py` gated the vocabulary on a **leading, short, bold** run only, a restriction it justifies by measurement.
**The residue between them was unguarded**, and it held **four open rows the register read CLOSED** — each by a closing word that is not about the task:

| row | the closing word the reader saw | what it is about |
|---|---|---|
| `T-261` | *"a challenge the corpus has since ANSWERED"*, and a quoted criterion `` `ANSWERED` ``, `` `UPHELD` ``, `` `RESOLVED` `` | the row's **own title**, and a **criterion quoted as data** |
| `T-268` | *"`CH-0207` **CLOSED and REPAIRED**"*, *"`P1` was found ALREADY DISCHARGED"* | a **challenge**, and a **deliverable** |
| `T-272` | *"`P2` is DISCHARGED over the whole corpus"* | a **deliverable** |
| `T-280` | *"Candidate 1 … is **DONE**"* | a **candidate of the remedy** |

`T-268` is a **HIGH VALUE** row and `SESSION-PROMPT.md` says the loop takes its next task from this register, so an open task outside it is a process blocker.

**(b) A row the reader could not see at all, and this one is much older.**
`_QUEUE_ROW` required a **trailing pipe**. GFM does not, so `tools/check-markdown-tables.py` is clean on a row that omits one and **the reader's format assumption was asserted nowhere**.
At the baseline the file holds **273** task rows and the reader saw **271**; at the commit this lands on it holds **275** and the reader sees **275**.
Over the queue's own history the defect is not one row: **81 of 138 revisions** carried at least one invisible row, **116 row-instances**, **5 distinct rows** — `T-66`, `T-72`, `T-97`, `T-98`, `T-182` — and **two of them, `T-97` and `T-98`, were OPEN**, across **17** revisions.
So the coverage defect has already cost the loop, and nothing in the tree could see it.

## 2. The rule, and why it is a reading of the file rather than a preference

The reader now takes the row's **first leading verdict**, and falls back to the whole-row scan only where there is none.
A verdict is a run that **opens a cell**: either a bold run of at most `MAX_WORDS = 6` words carrying a closing word, or a `TODO`.

Two things about that had to be measured before they could be written.

**An unbolded leading `TODO` is a verdict.** `T-261`'s status cell is `TODO — **MEDIUM**`, where the **bold carries the priority** and the verdict is bare — which is why `P-29`'s census, keyed on a bold run containing a closing word, could not see it at all. Refusing it leaves `T-261` unrepaired.

**The LEFTMOST verdict is the live one.** **12** rows carry a leading `TODO` and are read CLOSED by the old reader, and only **3** of them are the defect; the other **9** (`T-263`, `T-265`, `T-266`, `T-267`, `T-270`, `T-271`, `T-274`, `T-275`, `T-276`) carry a live `**DONE**` in an **earlier** cell and preserve the original `TODO — **PRIORITY**` note in a **later** one — `C-0071`'s *strike, never delete* applied to a whole column. So *"any leading `TODO` opens the row"* and *"the last verdict wins"* are both **9 false positives**; taking the **first** reproduces the old reader on **262 of 262** rows that carry a verdict, and the remaining **10** carry none and agree with the fallback at **10 of 10**.

## 3. What moved, and nothing else did

Both readers on the **same** queue — the baseline one, so the figure contains neither this task's document edits nor any concurrent agent's:

| row | before | after |
|---|---|---|
| `T-182` | **UNSEEN** | CLOSED |
| `T-261` | CLOSED | **OPEN** |
| `T-268` | CLOSED | **OPEN** |
| `T-272` | CLOSED | **OPEN** |
| `T-280` | CLOSED | **OPEN** |

Rows seen **271 → 273** on the baseline queue; open rows **62 → 66**. **No other row of 271 changes its reading** (`F3`).

**The repair is contained** (`F4`): `stale_statuses` is **0 before and 0 after** for `ANSWERS.md` **and** for `DECISIONS-FOR-NDI.md`, so no published sentence rested on those four rows having read closed.

**And the two halves of the repair are separable, which is what says the reader change is necessary rather than convenient.** Measured with the baseline reader against the repaired queue: lower-casing the prose alone fixes `T-268` and `T-272`; the trailing pipe alone fixes `T-182`; and **`T-261` is fixed only by the reader change**, because its acceptance criterion quotes `` `ANSWERED` ``, `` `UPHELD` `` and `` `RESOLVED` `` **as data** and lower-casing them would falsify the quotation. (`T-280` was rewritten to `**DONE**` by a sibling agent in this same iteration, so its row is closed today for a reason that is not this repair; the baseline measurement above is the one that is re-derivable.)

## 4. Where the shared predicate lives, and why not in either of the two files

`tools/check-queue-vocabulary.py` already imports `tools/trace-answers.py`.
Putting the shared predicate in the **gate** would make the **reader** depend on the thing that checks it — a cycle, and a gate that defines its own subject.
Putting it in the reader would work and would leave the predicate unnamed, in a 900-line file about something else.
It is therefore a third module, `tools/queue_verdicts.py`, and the dependency runs one way:

    queue_verdicts  <--  trace-answers  <--  check-queue-vocabulary

That also **de-duplicated a rule that was written out three times**: the closing-word set and the `PARTIALLY|PARTLY` qualifier existed once in the reader and once in the gate, and now exist once. `CLAUDE.md`'s *"the rule now lives once"* is usually written after a repair and before the next divergence; here the divergence is the defect being repaired, so the two files can no longer disagree about what a verdict is.

## 5. The gate, and what is deliberately not gated

`tools/check-queue-vocabulary.py` gained two arms, so **nothing new is wired into `tools/verify.sh`** — it was wired there by `C-0173`/`C-0177` already, and what grew is the coverage of what is wired.

- **`F1` coverage.** Every table row whose first cell is a task identifier must be a row the reader sees. The scanner (`queue_verdicts.task_rows`) is written **without** the reader's own pattern, and that independence is asserted **structurally** by a named test — a second opinion that consults the first is not one, and sharing the pattern is exactly what hid `T-182`.
- **`F5` per-row agreement.** `P-29` put each declared phrase through the reader in a **synthetic** one-row queue; this asks the same question of the **real** rows. It is not tautological: the declared sense comes from the hand-maintained sets in the gate and the read sense from the reader's own regular expression.
- **The residue is printed and NOT gated.** A row whose prose carries a closing word that is not its verdict cannot be made clean — `T-261`'s quoted criterion is the standing counter-example — so `CLAUDE.md`'s rule for exactly this shape applies: *wire the gate on what can be made clean and print the residue beside it, ungated*, with the count and the per-row list. It stood at **4 rows** before the document repair and stands at **1** after.

**The gate's own reading at the commit this lands on** (`C-0158`: a suite count is not a gate reading):

```
# 0 defect(s); 277 leading verdict(s) over 275 row(s) in TASKS.md
# residue (reported, NOT gated): 1 row(s) whose prose carries a closing word that is not their verdict
#   T-261  verdict 'TODO', whole-row scan reads CLOSED
```

## 6. Verification

| gate | reading |
|---|---|
| `tools/check-queue-vocabulary.py --selftest` | `# 36 self-test(s), 0 failure(s)` |
| `tools/check-queue-vocabulary.py` | `# 0 defect(s); 277 leading verdict(s) over 275 row(s)` |
| `tools/test-trace-answers.py` | 132 named tests, **all checks passed** |
| `tools/P-30-mutation-test.py` | `# 24 mutation(s), 0 survivor(s)` |
| `tools/P-30-history.py` | `# 138 revision(s) of TASKS.md; 9 fire the gate` — `UNDECLARED 9, UNSEEN 0, ROW 0, DISAGREES 0` |
| `tools/trace-answers.py` | exit 0, both documents |
| `tools/trace-answers.py --answers DECISIONS-FOR-NDI.md` | exit 0 |
| `tools/check-corpus-links.py` | `# 0 broken link(s) in 587 file(s)` |
| `tools/check-corpus-identifiers.py` | `# 0 dangling identifier(s) in 567 file(s)` |
| `tools/check-challenge-index.py` | `# 204 challenge file(s), 204 indexed, 0 unindexed` |
| `tools/check-markdown-tables.py` | `# 0 table defect(s) in 590 file(s)` |
| `tools/check-cold-start-note.py` | `# 0 defect(s); cold-start heading at iteration 42, journal at 42` |
| `tools/check-entry-points.py` | `# 0 defect(s); 131 of 133 studies emit a result file` |

**The false-positive rate is measured, not argued** (`F7`). Over **138** revisions of `TASKS.md` the gate fires on **9**, and every one is a genuine **undeclared coinage** the queue made and did not declare — `SECOND DELIVERABLE ANSWERED, iteration 41` (the `P-29` defect, 3 revisions) and `RESOLVED in substance, REOPENED in scope` / `RESOLVED in substance` (a `P-5` coinage, 6 revisions). **0 false positives**, and **0 firings of either new arm**, because the repaired reader sees the historically invisible rows: the coverage arm's demonstrated failure is in its named tests and in the mutation that restores the trailing-pipe requirement, and its value is prospective.

**The mutation test** (`F6`) — 24 mutations, each a **wholesale text replacement** in a throwaway copy of `tools/`, which cannot widen a rule to `original|mutant` by construction. That trap is `C-0177`'s own measured one: **9 of 22 rows of its first table failed nothing**, eight of them for exactly that reason. Killers are counted separately per suite, because a rule only one suite reaches is worth knowing about — eight mutations are killed by the reader's tests alone, twelve by the gate's alone, and four by both.

**Seven mutations survived the first run, and the survivors were the finding** (`C-0161`: a mutation that fails nothing is a measurement of the corpus, not a gap in the list). Five were real gaps and are now closed by five new named tests — the closing word's word boundaries, a leading bold run carrying no closing word, a bold closing word later in a cell, `TODO` inside a longer word, and the structural independence of the coverage scanner. **One survivor was a defect in the code rather than in the tests**: `TASK_ROW` carried both a `^` **and** a `.match()` call at every site, so a mutation of either was a no-op — which is `P-29`'s own recorded trap on `_LEADING_BOLD`, reproduced verbatim one file away, and the `^` is now gone. **One was a duplicated rule**: the closing-word set existed in the reader as well as in `queue_verdicts`, so mutating one left the other standing; de-duplicating it is §4 above.

## 7. Validity range, and what would falsify this

- **The rule is a reading of `TASKS.md`'s current practice, not a theorem.** If the queue ever begins **appending** a new verdict to the right of an old one instead of replacing it, "the first leading verdict wins" closes nothing and opens everything, which is the safe direction but is wrong. The falsifier is one pass: a row whose leftmost verdict is `TODO` or a qualifier and whose rightmost is a live closing verdict. There are **0** today.
- **`MAX_WORDS = 6` is a measured bound and not a principle.** A legitimate verdict longer than six words is refused, reads OPEN, and stays in the register — safe, and visible as an `UNDECLARED`-free row that nevertheless reads open.
- **The residue is not zero and is not meant to be.** One row stands, and it stands because a task file may quote a status word as data. A future gate on the residue would have to blank inline code spans first, which is a further predicate with its own false-positive rate and is **not** measured here.
- **This claim moves no physical number.** Its whole consequence is which rows the loop can see.
