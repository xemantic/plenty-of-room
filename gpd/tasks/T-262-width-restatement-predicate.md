# T-262 — `T-234`'s `WIDTH` family reads a RESTATEMENT as debt, and the debt line is now mostly the corrections themselves

| | |
|---|---|
| **Leaf** | none — a process task, protecting the census that protects every honeycomb leaf |
| **Raised by** | [`C-0155`](../claims/C-0155-tenth-answers-synthesis.md) (`T-257`), with a second instance found in iteration 39 |
| **Claim** | [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) |
| **Result** | `gpd/results/T-262-width-restatement-predicate.json` |

---

## Formulate

[`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) withdrew *"a honeycomb row length asserted as a uniform tile width"*.
[`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) then **restored** `112 bp` as a **row span** — a different functional of the same block, and *"the width that threatened it is not a width"* —
and [`C-0151`](../claims/C-0151-closing-raster-selection.md) restored `drawable` as the name of the **drawable `102 / 109` raster**.
The `WIDTH` family cannot tell the withdrawn reading from either restored one,
so **a correct restatement reads as debt**:
the advisory `T-233 debt` line went **23 → 32** in one document pass and all nine new occurrences were that pass's own correcting sentences.

There is a second, sharper instance.
The family reads `TASKS.md`'s `T-9` row — a **square-lattice single-layer** statement about the design [`C-0157`](../claims/C-0157-crossover-hinge-constant-from-oxdna.md)'s oxDNA run simulated — as a withdrawn honeycomb width,
because the honeycomb context is tested on the **line**, and a `TASKS.md` row is a paragraph on one physical line.

And a third thing this row was to decide:
`tools/T-234-emit-classification.py`'s docstring has promised since iteration 34 that a per-occurrence hand override *"survives"*.
It did not — the emitter built its table from scratch and never read the file it overwrote.
Either the docstring or the emitter is wrong.

### Acceptance predicate

**Either** a `WIDTH` predicate that distinguishes a withdrawn *uniform tile width* from a restored *row span* or a corrected raster width, with self-tests,
**or** a defended decision that the advisory debt line is not a measure of debt and should say so in the tool's own output.
Either is acceptable; the decision must be argued, not asserted.

In addition:

1. Every rule fails a **named** test when narrowed *and* when widened, measured over both tools' self-tests.
2. The false-positive rate is measured — and, per the coordinator's prior art, measured over **history** and not only over the current tree.
3. The emitter's docstring and the emitter agree.
4. `python3 tools/T-234-census.py --check` exits 0.

### Units and conventions

No physics. Counts are integers.
The historical series is taken out of `git`, never out of the working tree,
and the result file records the `baselineRef` it was taken at.

## Plan

**Cheap bound 1 — count the token before writing a rule.**
Of 24 `drawable` occurrences, 20 name `C-0151`'s drawable `102 / 109` raster and 4 name `C-0119`'s *"drawable at a uniform width"*.
One word, two statements, and the ratio says which way the family is now wrong.

**Cheap bound 2 — measure the obvious cure before writing it.**
Sentence-scoping the honeycomb context is the natural fix for the `T-9` row.
Measured, it drops **56 of 103** `WIDTH` occurrences, most of them genuine —
so it is rejected on `CLAUDE.md`'s own ground that *a predicate can always be narrowed until the tree is clean*.
What replaces it is a **measurement**: the distance from the token to the nearest honeycomb word on its own line,
reported when it exceeds a stated threshold, with the tool refusing to guess and a hand override settling each one.

**Cheap bound 3 — measure the false-positive rate over history.**
One loop over `git show <commit>:<file>` for the last 40 revisions of the two deliverables,
counting occurrences that would need a pointer under the old predicate and under the new one.
No solve, and it is the number that makes a split believable.

**Method.** TDD: named tests first, watched to fail.
Then a governing-noun refinement on `WIDTH` (`span`/`row` against `width`/`extent`/`footprint`/`edgeX`, nearest wins, because the restoring sentences name both),
a two-way test for `drawable`,
a `contextDistance` diagnostic,
and a hand-override mechanism keyed on the occurrence's **neighbourhood** rather than its index — because `TASKS.md` gains rows every iteration and an index is a dated object.

### What would falsify this approach

- **The governing-noun rule disagrees with a hand reading of the 103 occurrences on more than a handful.** Then the two readings are not grammatically separable and the honest deliverable is the second branch alone: say in the tool's output that the debt line is not a measure of debt.
- **The historical series shows no difference.** If the old and the new predicate grow alike over the deliverables' revisions, the split has removed nothing that matters and the finding is only the statement about the line.
- **The split changes the SIGN of the debt line's growth.** It does not, and that is a result rather than a failure: a correcting sentence must NAME a withdrawn premise in order to withdraw it, so a document pass will always add occurrences. Any claim that the split makes the line a debt measure would be false.
- **A mutation fails nothing**, or a rule can only be widened — then it has become a pattern rather than a judgement.
