# T-200 — Amend `C-0092`'s margin-movement deliverable, and prevent the class

**Leaf:** — (process; it guards the reproducibility every claim rests on)
**Raised by:** [`CH-0131`](../challenges/CH-0131-t-157-was-re-emitted-before-its-own-input.md)
**Verification type:** logical, with executable self-tests on the retained sorter
**Units:** none

---

## Formulate

`CLAUDE.md`'s rule is that when a repair moves a downstream result file you **re-emit** it and amend the
claim — never keep the stale file — because git already holds the history and a file the code cannot
reproduce destroys the byte-for-byte re-run diff half this repository's claims rest on.

`C-0101` wrote that rule, re-emitted eleven files, and **ran a consumer before its own producer**.
So the committed `T-157` reproduces the *pre-`C-0101`* `T-149`, and `C-0092`'s `A5` clause — *"the margins
move by 1.0000–3.3380×"* — measures a difference `C-0101` had already absorbed.

### Acceptance predicate

1. `C-0092`'s `A5` clause is amended to what it measures, with the corrected reading **verified off the
   re-emitted file** rather than inherited from the challenge.
2. The amendment is added, not substituted: the original reading stays visible and struck, and the
   contradiction stays with `CH-0131`.
3. **Every** dependency edge among `C-0101`'s eleven is checked, not only the one that was found.
4. Whatever prevents the class is either shipped with tests, or priced and declined **with a measurement**.

**Falsifier.**
If the other edges among the eleven were violated too, then the defect is systematic and the deliverable is a
re-emission of the whole set rather than an erratum plus a tool.

---

## Plan

**Cheap bound first, and it settles predicate 3 with no solve.**
A consumer reads *named values* out of its producer's file, so staleness can be tested by comparing those
values against the producer's current content — no re-run, no compile.
Run that before considering any re-emission.

**Method.**

1. Derive the dependency edges among the eleven from `tools/result-reader-census.py`'s graph, which already
   includes the **transitive** edges a grep cannot see (`CH-0092`).
2. Verify the corrected `A5` reading from `T-157`'s own `marginMovement` field.
3. Test the second edge by the cheap bound above.
4. Ship the ~20 lines that turn the census graph into a **topological order**, with self-tests — the class is
   preventable with what is already in the tree.
5. Price the tempting general gate — a scan of every reproduction residual — and decline it if the
   false-positive rate is unacceptable, **measured rather than asserted**.

**Justification against cost.** The alternative to a tool is to remember the rule, and `C-0101` is the proof
that remembering it is not enough: the claim that *wrote* the discipline is the one that broke it.

**What would falsify the approach.**
That the census graph is not the right graph — that a consumer can read a producer by a route the census
cannot see. `C-0082` asserts the derived census is a **superset** of what a naive grep finds, and that
assertion is a wired test, so the graph is the strongest available. If a route existed outside it, the order
would be incomplete and predicate 4 would fail.
