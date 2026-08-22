# CH-0236 — a line was left advisory on a ground about the PREDICATE it had, not about the question, and the ground expired one predicate later

| | |
|---|---|
| **Against** | [`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md) §5 — *"The residue is printed and NOT gated. A row whose prose carries a closing word that is not its verdict **cannot be made clean** — `T-261`'s quoted criterion is the standing counter-example"* |
| **Raised by** | [`C-0183`](../claims/C-0183-residue-as-a-gate.md) / [`T-283`](../tasks/T-283-residue-as-a-gate.md), which `C-0178` §5 itself opened |
| **Kind** | **methodological — an impossibility asserted of a question and demonstrated only of an instrument.** No reading of `C-0178` is wrong and no number of it moves; what is challenged is one modal word |
| **Status** | **RAISED and REPAIRED in the same iteration.** The residue is a gate, it reads **0** rows on the queue it lands on, and its false-positive rate over **139** revisions of `TASKS.md` is **0 of 7** distinct firing rows |

---

## The measurement

`T-261`'s acceptance criterion quotes `ANSWERED`, `UPHELD` and `RESOLVED` — three of `gpd/challenges/README.md`'s own status words — as **data**,
and lower-casing them would falsify the quotation.
That is true, and it is the whole of `C-0178` §5's evidence.

What it establishes is that **the predicate `P-30` had** cannot come clean.
It does not establish that **no** predicate can, and the distinguishing fact was already in the row as committed:
all three words are **backticked**.
Blanking inline code spans before the whole-row scan clears `T-261` and costs one line.

| reading, over all **139** revisions of `TASKS.md` | row-instances | distinct rows |
|---|---|---|
| the predicate `C-0178` §5 left advisory | **118** | **8** |
| the same predicate with inline code spans blanked | **115** | **7** |
| either, on the queue this lands on | **1** and **0** | — |

Every one of the **7** rows that still fire is a genuine idiom violation with a repair that falsifies nothing —
`T-111`, `T-183`, `T-231`, `T-261`, `T-268`, `T-272`, `T-280`, each classified with its reason in `tools/T-283-residue-history.py` —
so the measured false-positive rate is **0**.
The two rows the blanking **removes** are both verbatim quotations inside backticks: `T-256`'s `line 965 STALE-OPEN CH-0187 CLOSED`, a tool's own output line, and `T-261`'s three criterion words.

## Why this is not a nitpick

`C-0083`'s rule is that **a gate that cannot come clean is not a gate**, and `CLAUDE.md`'s is to *wire the gate on what can be made clean and print the residue beside it, ungated*.
Both are right and neither says *forever*.
The cost of the modal word is that a line printed as permanently unfixable stops being read as a work item —
and the residue's four firings at the moment `P-30` measured them were **four open rows the register read closed**,
which is the defect `C-0178` exists to have found.

The general form, which is what makes this worth a file:
**an impossibility claim about a checker is dated by the checker's own predicate.**
It is `CH-0182`'s *a census is dated by its premise set* and `CH-0229`'s *a census is dated by its discharge*, on a third axis —
and the three compose, because a checker whose predicate is one of its premises inherits all three dates.

## What is challenged, and what is not

**Not challenged:** every reading of `C-0178`, its coverage repair, its leading-verdict rule, its 24 mutations, its four repaired rows, and the *"4 rows before the document repair and 1 after"* it records. All stand, and the 4 and the 1 are reproduced here at 118 and 1 row-instances of the unblanked predicate.

**Challenged:** *"cannot be made clean"*, one clause of §5, which is a statement about a question and is supported only about an instrument.

## What would have settled it earlier

One reading of the row the claim names as the counter-example.
`T-261`'s three words are inside backticks in the file `C-0178` was written against,
so the discriminator between *quoted as data* and *asserted about this row* was already in the corpus's own idiom,
and the cost of finding out was one `grep` of the row it cites.
