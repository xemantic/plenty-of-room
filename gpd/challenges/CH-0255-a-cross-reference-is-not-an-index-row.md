# CH-0255 — `tools/check-challenge-index.py` counts a challenge as **indexed** when any row of the index merely **links** to it, so `CH-0053` had no row of its own for **38 revisions** of `gpd/challenges/README.md` while the gate reported `0 unindexed` on every one of them

| | |
|---|---|
| **Against** | [`tools/check-challenge-index.py`](../../tools/check-challenge-index.py)'s `indexed()`, and through it the standing reading that the challenge index is complete — *"215 of 215 indexed"* in [`C-0197`](../claims/C-0197-the-challenge-halfs-own-open-word.md) §6, and `219 challenge file(s), 219 indexed, 0 unindexed` in the gate's own output |
| **Raised by** | [`C-0203`](../claims/C-0203-a-challenges-status-row-is-the-authority.md) ([`T-298`](../tasks/T-298-a-challenges-status-row-is-the-authority.md)) |
| **Grounds** | **a gate that cannot see.** ``_ROW = re.compile(r"\[`(CH-\d{1,4})`\]")`` is applied with `.finditer` over the **whole file**, so it matches a link wherever it stands — including inside another row's `Against`, `Raised by`, `Grounds` or `Status` cell. An index row begins at the start of a line with `\| \[` and nothing in the checker requires that |
| **Direction** | **favourable to the corpus and adverse to the gate.** No challenge is missing from the corpus and no verdict moves; what is wrong is the count, and the failure direction is the unsafe one — a challenge with no row of its own is reported as indexed |
| **Status** | **RAISED**, and the one instance it was hiding is **repaired** by `C-0203`: `CH-0053` now has its own row. The predicate is not repaired here, because `T-298` does not own the checker |

---

## The measurement

Read at `HEAD` before the repair: **219 challenge files, 218 rows beginning a pipe, a space and a bracketed identifier**.
The file the index does not carry a row for is
[`CH-0053`](CH-0053-both-errors-run-the-same-way-and-the-desired-stroke-does-not-survive-them.md),
and it is *"indexed"* only because
[`CH-0062`](CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md)'s row links to
it in prose.

Over the file's own history, `CH-0053` is linked-but-rowless in **38 revisions** of
`gpd/challenges/README.md`, the oldest at `5ea5c137` (2026-08-14).
The gate has reported `0 unindexed` on every one of them.

## Why this is filed rather than fixed

`C-0083`'s rule is that a gate that cannot come clean is not a gate, and `CH-0236`'s that an
impossibility claim about a checker is dated by the checker's own predicate.
Here the predicate is one anchor away from correct — ``re.compile(r"^\|\s*\[`(CH-\d{1,4})`\]", re.M)``
— and `C-0197` records the same shape twice already: **`^` plus `.match()` is a double anchor**, and
its inverse, an unanchored pattern applied to a whole document, is what this is.

The repair belongs with the checker's owner, with **two named tests**: an index whose only mention
of a challenge is a cross-reference inside another row must report that challenge **unindexed**,
and a legitimate cross-reference must not create a second row for a challenge that has one.

## What would settle it

1. An argument that the index is *supposed* to count a cross-reference — in which case the count is
   right and its **name** is wrong, and `0 unindexed` should read *"0 unmentioned"*.
2. A repaired anchor with the two tests above, after which the count and the name agree.

## What this challenge does not say

- **It does not say the corpus lost a challenge.** `CH-0053` has always existed and has always been
  reachable; what was missing was its row, and with it the four columns an index row carries.
- **It does not say the count is large.** It is **one** file. The finding is that the gate could not
  have found it, and that a second one would be equally invisible.
