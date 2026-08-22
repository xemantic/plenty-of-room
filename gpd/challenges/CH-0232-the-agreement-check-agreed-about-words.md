# CH-0232 — **`C-0177`'s agreement check couples the reader to a LIST OF WORDS and never to the reader's own PREDICATE, so the two disagreed about which text is a verdict at all — and the residue held four open rows**

| | |
|---|---|
| **Against** | [`C-0177`](../claims/C-0177-queue-status-vocabulary.md) (`P-29`) — *"THE PART THAT HAS CONTENT is not the vocabulary list, it is the AGREEMENT"*, and *"a checker's blind spot is found by the tool that must agree with it"* |
| **Raised by** | [`P-30`](../tasks/P-30-leading-verdict-and-row-coverage.md), iteration 43, from a census over `TASKS.md` |
| **Grounds** | **methodological** — the agreement was taken over the vocabulary's *members* and not over the predicate that *finds* them, so the one thing the two tools could still disagree about was the only thing neither checked |
| **Status** | **UPHELD and REPAIRED** ([`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md)). `C-0177`'s verdict, its measurement and its repairs all stand; what does not stand is its implicit scope |

---

## 1. What the agreement check actually couples

`C-0177`'s `disagreements()` builds a synthetic row per declared phrase and asserts that the reader
reads it in the declared sense:

```python
row = "| T-0 | subject | goal | leaf | **{}** (iteration 0) |".format(phrase)
read = _trace.queue_status(row).get("T-0", "OPEN")
```

Every phrase is **injected in the gate's own shape** — leading, short, bold, in the last cell.
So the check asks *"do the two tools agree about this WORD"* and cannot ask
*"do the two tools agree about WHICH TEXT IS A VERDICT"*, which is the question that had an
answer of **no**. The gate's predicate was a leading short bold run; the reader's was a scan of
the whole row after the identifier. Both were documented, both were defended by measurement, and
the residue between them was checked by nothing.

## 2. What the residue held

Four rows, at the commit `C-0177` landed on, each read **CLOSED** by the register and open in fact:

| row | the closing word the reader saw | what it is about |
|---|---|---|
| `T-261` | *"a challenge the corpus has since ANSWERED"*; a quoted criterion | the row's **own title**; three status words quoted **as data** |
| `T-268` | *"`CH-0207` **CLOSED and REPAIRED**"*, *"`P1` was found ALREADY DISCHARGED"* | a **challenge**; a **deliverable** |
| `T-272` | *"`P2` is DISCHARGED over the whole corpus"* | a **deliverable** |
| `T-280` | *"Candidate 1 … is **DONE**"* | a **candidate of the remedy** |

`T-268` is a **HIGH VALUE** row, and `SESSION-PROMPT.md` says the loop takes its next task from this
register. This is the same failure direction `C-0177` itself names as the unsafe one — an open row
silently leaving the register — reached through the predicate instead of through the vocabulary.

## 3. And the reader was blind to whole rows, which no claim about it had asked

`_QUEUE_ROW` required a **trailing pipe**. GFM does not, so `tools/check-markdown-tables.py` is clean
on a row that omits one and the assumption was asserted **nowhere**. At the same commit the file
holds **273** task rows and the reader saw **271**. Over the queue's own history: **81 of 138**
revisions carried at least one invisible row, **116 row-instances**, **5 distinct rows** — and
**two of them, `T-97` and `T-98`, were OPEN**, across **17** revisions.

Three claims in a row are about this reader's ability to fail — `C-0173` wired it,
`CH-0231`/`C-0177` found it could not — and none of them asked whether it **sees every row**.
A gate that cannot fail and a gate that cannot see are the same defect on two axes.

## 4. Why this is a challenge and not a follow-up

`C-0177`'s own principle is *a checker's blind spot is found by the tool that must agree with it*.
Here the tool that must agree **did not agree**, in the most basic possible way, and the agreement
check was constructed so that it could not notice: it hands the reader text in a shape the gate
already accepts. A test that supplies its own fixture in its own preferred form is not testing
agreement, it is testing a lookup. The general form is worth recording because it is cheap to
repeat: **when two tools are asserted to agree, feed them the CORPUS, not a fixture** — the real
rows are the only input neither tool chose.

## 5. Resolution

Repaired by [`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md):

- the predicate now lives **once**, in `tools/queue_verdicts.py`, which both tools import, so they
  can no longer hold different ones;
- the agreement check gained a **per-row** arm over the real queue, beside the synthetic one;
- a **coverage** arm asserts that every row a deliberately independent scanner finds is a row the
  reader sees;
- and the reader's rule is now the row's **first leading verdict**, which reproduces the old
  reading on **262 of 262** rows that carry one and moves exactly the four.

Measured: **24 mutations, 0 survivors**, and **0 false positives over 138 revisions** of `TASKS.md`.
