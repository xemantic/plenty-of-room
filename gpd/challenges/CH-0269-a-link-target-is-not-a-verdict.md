# CH-0269 — **`tools/trace-answers.py` READS A VERDICT WORD INSIDE A LINK TARGET, SO CITING A SYNTHESIS CLAIM BESIDE AN OPEN TASK IS A SELF-CONTRADICTION — AND A SYNTHESIS PASS IS THE ONE PASSAGE THAT WRITES EXACTLY THAT.** `C-0196`'s rule is already in the corpus — *a name cannot govern a token* — and `T-285`/`T-287` blanked filenames for the `T-234` census. Nothing did it here. **33 filenames of this corpus carry a settled word and 2 an open one**, and **twelve of the thirty-three are the `*-answers-synthesis.md` family** — so a sentence that links `C-0191` to the file `C-0191-thirteenth-answers-synthesis.md`, names `T-999` beside it and says its price line is left **open** is reported as a task the document calls both settled and unsettled, on the strength of the word **`answers`** in a path

| | |
|---|---|
| **Status** | **RAISED and REPAIRED in the same iteration** ([`C-0210`](../claims/C-0210-fourteenth-answers-synthesis.md), `T-319`). `blank_link_targets` is length- and newline-preserving, applied in **both** verdict arms (`self_contradictions` and `open_assertions`) and in neither of the numeric ones; **7 named tests** pin both directions, and reverting the repair fails a named test |
| **Against** | [`tools/trace-answers.py`](../../tools/trace-answers.py)'s `self_contradictions` and `open_assertions`, and the standing reading that its two verdict arms scan prose |
| **From** | [`T-319`](../tasks/T-319-fourteenth-answers-synthesis.md), which hit it on its own first draft: the fourteenth synthesis cites `C-0210-fourteenth-answers-synthesis.md` beside `T-320`, whose price line it deliberately leaves **open**, and the gate reported a self-contradiction about a task that had not been written yet |
| **Kind** | **a gate that cannot see the difference between a NAME and an ASSERTION** — `C-0161`'s *a mutation that fails nothing is the finding* read from the checker's side, and `C-0196`'s rule met on a second tool |

---

## 1. The reproduction, three lines

```
See ([`C-0191`](gpd/claims/C-0191-thirteenth-answers-synthesis.md), `T-999`) and the price line is left open.
```

`self_contradictions` returns `T-999` with verdicts `{OPEN, SETTLED}`. Remove the **link** and keep
every word of prose —

```
See (`C-0191`, `T-999`) and the price line is left open.
```

— and it returns nothing. **The whole verdict is the word `answers` inside a path.**

## 2. Why it had not bitten before, and why it was certain to

`_SETTLED_WORD` is `answered|answers|resolved|settled|closed|measured|established|demonstrated` and
`_OPEN_WORD_ASSERTION` is `open|unmeasured|unanswered|…`. Measured over `gpd/claims`,
`gpd/challenges`, `gpd/tasks` and `gpd/results`:

| | count | examples |
|---|---|---|
| filenames carrying a **settled** word | **33** | `C-0191-thirteenth-answers-synthesis.md`, `C-0067-answers-reconciliation.md`, `C-0173-trace-answers-wired.md`, `CH-0084-the-measured-staple-incorporation-…md` |
| filenames carrying an **open** word | **2** | `C-0197-the-challenge-halfs-own-open-word.md`, `CH-0130-the-overall-sign-of-the-corrugation-is-undetermined.md` |

Twelve of the thirty-three are one family, `*-answers-synthesis.md`, and **the passage that cites a
synthesis claim is a synthesis pass** — which is also the passage that states which tasks are still
open. The two populations were always going to meet; they had simply never been written into one
sentence before.

## 3. The repair, and what it deliberately does NOT touch

`blank_link_targets` blanks the `(target)` half of an inline link and keeps the `[label]`, because a
**label is prose and does assert**: a sentence whose link **label** is the word *answered*, beside `T-999` and an open word, is still a
contradiction, and a named test pins that. It is applied in the two **verdict** arms only. The
numeric arm is untouched — a number in a path is `C-0198`'s question, not this one — and so is the
challenge-status arm, which reads a known cell of a known table.

Blanking is **length- and newline-preserving**, per this corpus's own rule that a blanking pass must
not move a reported line number; two named tests pin it.

## 4. What this challenge does NOT claim

**No standing verdict moves.** At the baseline ref `71d126e` the tracer reported **0**
self-contradictions on both deliverables, so the repair silences nothing that was firing; its whole
effect on the corpus today is to stop reporting one defect that is not one, and its effect tomorrow
is that a synthesis may cite its predecessors. The counts either side of it are identical:
**1 open assertion, 0 contradicted** on `ANSWERS.md` before and after.
