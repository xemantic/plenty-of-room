# T-273 — a bare `CH-0133` in a sentence is neither a filename nor a link, and no gate here could see it

| | |
|---|---|
| **Leaf** | none — a **process** task protecting the corpus's cross-references |
| **Raised by** | [`C-0162`](../claims/C-0162-round-outputs-never-inputs.md) (`T-268`), which found `T-268`'s own `P1` citing a challenge that does not exist |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The defect

`T-268`'s `P1` and `P2` cite **`CH-0132`** and **`CH-0133`**.
Neither exists.
Both were **reserved** by `T-201` in iteration 24 and never filed — released by name in `TASKS.md` in iteration 39 — and nine occurrences accumulated —
one of them in [`CLAUDE.md`](../../CLAUDE.md) — before a claim re-derived the census and found nothing behind
the ID.

This repository already gates the two neighbouring classes and neither reaches this one:

- `C-0083` gates a claim's **filename**, because a writer reconstructs a slug from the claim's *subject*;
- [`tools/check-corpus-links.py`](../../tools/check-corpus-links.py) gates a relative **link**.

A bare `` `CH-0133` `` in prose is neither, so it is invisible to every gate in the tree.
It is the same failure one level down: a writer reconstructs an identifier from the finding's subject, and the
number that comes to mind is the one that was **reserved beside it**.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | every `C-XXXX` and `CH-XXXX` cited in `gpd/claims/`, `gpd/tasks/`, `gpd/challenges/` and the root documents resolves to a file that exists, or is a statement **about** the absence |
| **P2** | the discriminator **measured**, not asserted — the false-positive rate over the corpus's own legitimate mentions, and a named test for each context class in **both** directions |
| **P3** | the live defects repaired **by striking, never deleting**, and the correct owner named where one exists |
| **P4** | wired into [`tools/verify.sh`](../../tools/verify.sh), reading **0**, because a gate that cannot come clean is not a gate |

### Units and conventions

Nothing physical is computed. No result file moves and no study is re-run.

---

## 2. Plan

### The cheap bound runs first, and it decided the whole design

The census over the whole tree costs one pass and it is what says whether this can be a gate at all:
**28 challenge numbers and 5 claim numbers have no file**, and the corpus mentions many of them **correctly**.
A naive gate fires on **21 correct sentences**, which is the rate at which a gate gets switched off (`C-0127`).

The corpus's legitimate mentions turn out to be exactly two kinds, and both are statements *about* the
non-existence rather than citations *of* the thing:

- **RELEASED** — *"`CH-0208` was reserved for this claim and is RELEASED UNUSED"*;
- **ABSENT** — *"there is no `CH-0133`; the corpus's highest challenge is `CH-0209`"*.

The exemption is per **(document, identifier)** and not per occurrence, because `CLAUDE.md`'s entry about this
very defect names `CH-0133` three times and only the first is beside the words that declare it absent.
Per-occurrence, the gate fires on the sentence that records the defect — which is `T-249`'s failure met from
the other side.

### Scope, and one deliberate exclusion

`JOURNAL.md` is **out of scope**. It is a dated history, and an entry naming a number that was later
renumbered is a correct record of what happened; rewriting it is the one thing this repository forbids.
`tools/` is out of scope too — its checkers carry deliberately impossible fixtures.

### What would falsify this approach

- **The two context classes do not account for the legitimate mentions.** Then the gate cannot come clean and
  the honest form is `C-0129`'s: print the residue ungated, with the count and the per-file list.
- **A release note that names a RANGE does not release the numbers inside it.** Predicted and then observed:
  *"`CH-0137` through `CH-0142`"* left `CH-0138`, `CH-0139` and `CH-0140` still firing, correctly — none
  of those three had been **released by name**, and a range is not a name. The register credits a number
  nobody wrote down to nobody.
