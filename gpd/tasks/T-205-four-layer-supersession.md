# T-205 — What did the four-layer line supersede in the queue?

**Leaf:** — (process; it prevents wasted work rather than producing a number)
**Raised by:** the coordinator, reading the open queue after `C-0123`
**Verification type:** logical
**Units:** none

---

## Formulate

`C-0109`–`C-0123` moved the tile: §3's own thickness row says *"~10 nm"*, every structural claim before
iteration 23 modelled a **2 nm** sheet, and the four-layer tile is flat **with no attachment coupling at all**.

**Eight open tasks optimise a coupling placement, phase or distribution on the single-layer tile.** If the
tile is four-layer, that is work on a body §3's own row contradicts — which is exactly what `C-0109` found the
*flatness negative itself* to be.

### Acceptance predicate

1. Every open item is read against `C-0109`–`C-0123` and classified, with a reason per item.
2. The open set is **derived from `TASKS.md`** by the same `queue_status` the deliverable's checker uses, so
   the denominator cannot drift from the register.
3. Items the line makes **more** load-bearing are found as well as items it makes contingent — a sweep that
   only looks for work to cancel is not a sweep.
4. Nothing is struck. `CLAUDE.md`: *strike a discharged item, never delete it.*

**Falsifier.** If no open item is contingent on the tile's layer count, the queue is independent of the tile
and this sweep is unnecessary — a null worth recording, because it would mean the four-layer line changed a
verdict without changing any planned work.

---

## Plan

**It is a read, and that is the justification.** The alternative is an agent picking up `T-176` — an
exhaustive enumeration over 163 296 placements — for a tile that may not be the one built.

**Method.** Read each open row; classify as **CONTINGENT** (well posed only if the tile stays single-layer),
**STRENGTHENED** (the line makes it more load-bearing) or **UNAFFECTED**; mark the first two **in the queue
itself**, because a classification that lives only in a claim is one an agent picking up a task will not see.

**What would falsify the approach.** That *"contingent"* is the wrong word — that these tasks are simply dead.
They are not: NDI has not ruled, decision 7 is unanswered, and §3's row is genuinely ambiguous. **Contingent
is a third state**, and inventing the word is the move `C-0071` made for `DISCHARGED` when the queue needed a
status it did not have.
