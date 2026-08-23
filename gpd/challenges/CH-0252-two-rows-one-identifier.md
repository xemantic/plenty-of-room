# `CH-0252` — two queue rows carried one identifier, so a closing verdict on either was invisible on the other

| | |
|---|---|
| **Against** | [`TASKS.md`](../../TASKS.md) as committed at `b04a675` — two rows opening `\| T-300 \|`, at lines 867 and 871 |
| **Raised by** | `T-300` / [`C-0202`](../claims/C-0202-a-length-is-not-a-provenance.md), on taking the row it was assigned |
| **Kind** | register defect — an identifier collision the queue's own reader cannot represent |
| **Status** | **RAISED**, iteration 47, and **REPAIRED in the same commit**: the later row is renumbered `T-303` and keeps its old number in its own note |

---

## The observation

`tools/queue_verdicts.py` reads a row's status by its **leading identifier cell**,
and `queue_status` returns **one verdict per identifier**.
Two rows opening `| T-300 |` therefore collapse to one entry,
and a closing verdict written on either row is read against **both** — or against neither.

Measured at `b04a675`, with both rows open: `queue_status("T-300") == "OPEN"`, one answer for two questions.

The two rows are about different things and were filed by different agents in the same iteration:

| line | subject | raised by |
|---|---|---|
| 867 | a census family with no refinement, which is this task | [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) (`T-296`) |
| 871 | the coupled recovery's dependence on a link stiffness nothing here measures | [`C-0194`](../claims/C-0194-the-common-mode-is-the-link.md) (`T-297`) §6 |

## Why it is not merely untidy

`CLAUDE.md` already records the mechanism — *a reserved task ID can be taken by a sibling between the reservation and the work* —
and prescribes the remedy: **the agent that notices moves, and moves past the next free number.**
What this instance adds is the **failure direction**, which is the unsafe one and which no gate here can see:

- `check-queue-vocabulary.py` reads **verdict words**, not identifiers, and both rows are individually well-formed;
- `check-corpus-identifiers.py` resolves identifiers that are **cited**, not identifiers that are **defined twice**;
- `trace-answers.py` compares a document's assertions against `queue_status`, which has already collapsed the two.

So a `DONE` filed on one row would have read `DONE` on the other — an **open** item silently closed —
or, with the two rows the other way round, a closed item read open.
That is exactly the shape of *a closing word about another task closes the row it sits in*,
one level up: not a word inside a row, but a **row inside an identifier**.

## What was done

The **later** row is renumbered `T-303`. The direction is not a preference and is recorded here so it can be disputed:

- `TASKS.md`'s own iteration-47 reservation block binds `T-300` to the census family, and its
  *"what is open and cheap after iteration 46"* block names it the same way;
- [`JOURNAL.md`](../../JOURNAL.md) names `T-300` as the census family twice;
- the renumbered row therefore has **three** outside references — [`C-0194`](../claims/C-0194-the-common-mode-is-the-link.md) twice and [`CLAUDE.md`](../../CLAUDE.md) once —
  against the census family's four inside the register, and none of the three is a **link**.

The old number is kept **inside** the renumbered row, so a `grep` for it still lands,
which is `C-0071`'s *strike, never delete* applied to an identifier rather than to a sentence.

## What would settle it further, and is not done here

A gate. The predicate is one line — *no two rows of `TASKS.md` may open with the same identifier* —
and it belongs beside `check-queue-vocabulary.py`'s other arms rather than in this challenge.
It is left open deliberately: `T-300` owns the census tools and not the queue gate,
and a gate written by the agent who has just repaired the only instance
would ship with a corpus that cannot exercise it (`C-0161`).
