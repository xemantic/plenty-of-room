# CH-0287 — **A SYNTHESIS CLAIM'S OWN CORRECTION TABLE IS THE DENSEST CARRIER OF SUPERSEDED VALUES IN THIS CORPUS, AND IT IS OUTSIDE EVERY PASS AND EVERY GATE — SO A CLAIM CAN CONTRADICT THE CLAIM IT CITES, INDEFINITELY, WITH EVERY CHECKER GREEN.** `C-0219` moved `C-0154`'s three `15 × 4` free tiles; of the **27** line-occurrences of that triple in the corpus — excluding this challenge's own, which `CH-0182` makes unavoidable — **19** belong to the claims and tasks that own the correction, **3** are `C-0154`'s own rows, struck in place, **4** are the two deliverables' (repaired here) and **exactly one** is a non-owner that still states the withdrawn value live: [`C-0191`](../claims/C-0191-thirteenth-answers-synthesis.md) §2(b), the **thirteenth synthesis's** own *"what moved"* table, which now quotes `C-0154` as reading `0.312237799 / 0.227177955 / 0.220064299` where `C-0154` reads `0.242196276 / 0.157167743 / 0.150056485`. The numeric tracer **cannot** see it — pointed at that file it reports `0 ABSENT`, because the withdrawn value is still `CITED`, by the very claims that withdrew it — which is `C-0080`'s third drift class, unmechanisable by construction, met on the one document class that manufactures it

**Against** no number and no verdict. Against the **scope** of this programme's reconciliation practice: fifteen synthesis passes have reconciled [`ANSWERS.md`](../../ANSWERS.md) and [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md), and **none** has reconciled the claim corpus against itself.
**Not against** [`C-0191`](../claims/C-0191-thirteenth-answers-synthesis.md), which was correct when written, nor against [`C-0219`](../claims/C-0219-a-dishing-fit-and-the-parity-of-its-basis.md) or [`C-0218`](../claims/C-0218-the-tied-regrade-at-the-other-cross-section.md), which annotated every artifact they own.
**From** [`C-0220`](../claims/C-0220-fifteenth-answers-synthesis.md) (`T-332`).
**Kind** — a **scope** statement about a retained practice, with one live instance and its exhaustive census. `C-0088`'s *a checker's DEFAULT is part of its logic* read on the **pass** rather than on the tool.

---

## 1. The census, exhaustive

Every line carrying `0.312237799`, `0.227177955` or `0.220064299` under `gpd/`,
plus the two deliverables, classified by whether the file **owns** the correction.
**This challenge itself carries three more**, because reporting the class requires quoting it —
`CH-0182`, and both readings are emitted.

| where | occurrences | state |
|---|---|---|
| `C-0218`, `C-0219`, `CH-0282`, `T-294`, `T-330` — the owners | **19** | correct: the withdrawn value is the *before* column of their own tables |
| `C-0154` — the claim corrected | **3** | struck and replaced in place, by `T-330` |
| `ANSWERS.md`, `DECISIONS-FOR-NDI.md` | **4** | struck and replaced by `C-0220` (`T-332`), this pass |
| **`C-0191` §2(b)** | **1** | **live, unannotated, and contradicting the claim it cites** |
| **total, excluding this challenge's own 3** | **27** | derived in `gpd/results/T-332-fifteenth-answers-synthesis.json`, field `supersededTripleCensus` |

The one live instance was **known**: `T-330`'s hand-off names it, and assigns it to its own author.
It was not annotated, and **nothing in this repository can say so** —
which is the finding, not the omission.

## 2. Why no gate reaches it

[`tools/trace-answers.py`](../../tools/trace-answers.py) has four arms and none of them fires here.

- **The numeric arm** asks whether a token appears in *some* claim.
  `0.312237799` appears in three, so it reads `CITED`.
  This is `C-0080`'s third drift class stated in as many words:
  *"a SUPERSEDED number reads `CITED`, because it has an owner"*,
  and `C-0080` records that no corpus comparison can see it.
- **The status arm** needs a status word; §2(b) is a table cell of numbers.
- **The self-consistency arm** compares a document against itself; §2(b) is internally consistent.
- **The priced-on-adjudicated arm** needs a challenge identifier; §2(b) cites none.

Run against the file directly the tool is clean:
`111 tokens: 40 CITED, 71 ELSEWHERE, 0 ABSENT; 0 self-contradictions`.
**The instrument works and the question is not one it asks.**

## 3. Why the synthesis claim is the worst case rather than an arbitrary one

A synthesis claim's `§` tables exist **in order** to quote other claims' numbers beside a correction.
Every such row is a **deliberate** copy of a value into a second file,
and the corpus's own rule — *strike, never delete* — guarantees the copy is never removed.
So the class does not merely occur in synthesis claims; **they are what generate it.**
`CH-0230`'s mechanism (*a correcting sentence has to NAME the thing it corrects, so a census of that
name GROWS when the documents are corrected*) reaches the same conclusion from the other side.

## 4. What would settle it, and what it costs

Two candidates, in ascending cost.

1. **A pass-level rule, free.** A synthesis's own correction table is re-read at the next pass —
   i.e. the fifteenth pass re-reads the thirteenth's and fourteenth's tables.
   This is the cheap half and it catches exactly the instance above.
   It is a **convention**, and `CLAUDE.md` records six times that a convention is not a mechanism.
2. **A gate, unpriced.** *A claim that names another claim beside a number must quote the value
   that claim states.* Mechanisable in principle — the citation is a link and the value is a token —
   and **its false-positive rate is unmeasured**, which `CLAUDE.md` records as the thing that makes
   a gate get switched off. A *before* column of a correction table is the obvious false positive,
   and it is the commonest shape in the population.

This challenge asks for the first and **prices rather than proposes** the second.
`T-333` carries it.

## 5. What does not move

**No number, no verdict, no result file, and no flatness reading.**
The corrected triple is smaller at all three enhancements and outside `T-5b` at all three,
so `C-0191` §2(b)'s conclusion — *the cross-section ordering survives, and no single multiple is
restated* — is true of the corrected values as well as of the withdrawn ones.
The defect is that a reader of `C-0191` is handed a number the corpus no longer states.

| | |
|---|---|
| **Status** | **RAISED**, iteration 52 |
| **Raised by** | [`C-0220`](../claims/C-0220-fifteenth-answers-synthesis.md) (`T-332`) |
| **Moves** | nothing yet. `T-332` is forbidden by its own brief from editing `C-0191`, so the instance is **reported and left**, which is itself the evidence for arm 1 above |
