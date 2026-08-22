# T-260 — `tools/T-234-census.py` cannot represent a PARTIAL discharge, and it costs three false positives

| | |
|---|---|
| **Leaf** | none — a process task, protecting the census that protects every honeycomb leaf |
| **Raised by** | the coordinator, iteration 37, while clearing the gate |
| **Claim** | [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) |
| **Result** | `gpd/results/T-260-partial-discharge-predicate.json` |

---

## Formulate

`tools/T-234-census.py` sweeps the corpus for premises [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) and [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) withdrew.
Its `PLACEMENT` family matches the string `single-layer square-lattice`,
and it treats every match as an assertion of an absence `C-0141` now supplies.

**`C-0141` supplied only half of what that string says.**
It supplied the honeycomb station lattice, the plan ceiling, the placement family and the price of an oblique root.
It did **not** supply a *grillage*: `OrigamiGrillage` never reads `layers`,
so every coupled cell in this corpus stayed a smeared single-layer square-lattice solve
until [`C-0154`](../claims/C-0154-honeycomb-grillage.md) built one (`T-253`) and [`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md) re-graded onto it.

One token, two statements, two correcting claims, two dates.
A sentence about the grillage is **live and correct** and needs no `C-0141` pointer,
and the census flagged three of them.
The standing workaround — registering the whole file in the emitter's `CORRECTING` set —
silences the symptom and leaves the predicate wrong,
which is exactly what `CLAUDE.md` warns about under *a drift checker's FALSE positives cost more than its true ones*.
A first attempt at a `GRILLAGE_HALF` pattern reclassified two of three and was reverted unshipped, because it added semantics with no self-test.

### Acceptance predicate

1. A **tested** predicate separates the half `C-0141` discharged from the half it did not,
   with named self-tests in `--self-test`, and the `CORRECTING` entries that exist only to hide these occurrences are **removed**.
2. The class and the family cannot disagree: a family this census does not gate may not carry a class it gates,
   and that is asserted mechanically rather than remembered.
3. Every rule fails a **named** test when narrowed *and* when widened (`C-0127`/`C-0150`'s standard), measured and not asserted.
4. `python3 tools/T-234-census.py --check` exits 0, and the exit code is read from the tool and not from a pipeline.

### Units and conventions

No physics. Counts are integers; no length, force or energy enters this task.
The corpus is the in-scope Markdown — `gpd/claims/`, `TASKS.md`, `ANSWERS.md`, `DECISIONS-FOR-NDI.md` — read at the `baselineRef` the result file records.

## Plan

**Cheap bound first, and it is a reading rather than a calculation.**
Dump every occurrence of the family with a window around the token and read all 38 by hand.
That costs minutes, it produces the ground truth any false-positive rate has to be measured against,
and — following the coordinator's prior art on `tools/check-queue-vocabulary.py` — **the census picks the predicate, not taste**.

**The second cheap bound is a data-structure question, not a pattern question.**
The `PLACEMENT` pattern is one string and `C-0141` discharged half of what it matches,
so *no* regular expression over that string can be right.
What has to change is that a **family carries its own pointer set**:
a census is defined by the discharge it is about,
and a token that spans two discharges belongs to two censuses.
That is one field on `FAMILIES` and one map, and it is why this is not a keyword arms race.

**The third bound is the cost of the gate.**
Gating the grillage half on `C-0154`/`C-0167` would demand annotating claims this task does not own.
So the honest form is `CLAUDE.md`'s own: wire the gate on what can be made clean,
and print the other half beside it, ungated, naming the census it belongs to.

**Method.** TDD throughout: the named tests go in first and are watched to fail.
Then `refine_placement`, a per-family `discharge`, a class coercion in the emitter,
and `tools/T-234-mutation-test.py` measuring both directions over the two tools' self-tests.

### What would falsify this approach

- **The hand reading and the predicate disagree materially.** If more than a handful of the 38 occurrences need a call the predicate cannot make, then the distinction is not mechanical and the right answer is to say so and leave the family alone (which is what [`C-0149`](../claims/C-0149-ninth-answers-synthesis.md) correctly did once).
- **A mutation fails nothing.** A rule no named test asserts is a rule that is not tested, and shipping it would be exactly the unshipped `GRILLAGE_HALF` attempt again.
- **The split needs a new `CORRECTING` entry to come clean.** Trading one set membership for another is not a repair.
- **Closing the gate requires editing a claim this task does not own** — annotating somebody else's argument to satisfy a checker is the same error as registering their file.
