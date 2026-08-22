# CH-0238 — a mutation table's `UNREACHED` list is produced by a SECOND instrument, and that one was matching where it should have been parsing — so it reported as unreached tests a mutation had demonstrably killed

| | |
|---|---|
| **Against** | [`tools/T-234-mutation-test.py`](../../tools/T-234-mutation-test.py)'s `_named_tests`, and through it every `UNREACHED` list [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) and [`C-0182`](../claims/C-0182-name-the-discharge.md) quote from that tool |
| **Raised by** | [`C-0184`](../claims/C-0184-a-slug-is-not-a-statement.md) / [`T-285`](../tasks/T-285-a-slug-is-not-a-statement.md), which needed the reach measurement for its own rule and could not reconcile it |
| **Kind** | **methodological — a measurement taken with the wrong instrument, in the direction that reports work as undone** |
| **Status** | **RAISED and REPAIRED in the same iteration.** `_named_tests` parses with `ast`, which is what the interpreter does at the call, and carries its own self-check. The corrected reading is **184** named tests where the same test set read **182**, and all **18** of `T-285`'s own tests are reached |

---

## The measurement

`tools/T-234-mutation-test.py` reports two things.
The first — *does every mutation fail a named test?* — comes from running the mutated self-test and reading its `FAIL` lines.
The second — *is every named test load-bearing?* — is `C-0150`'s question,
and it needs a **list of the names that exist**, which is a different instrument:

```python
return re.findall(r'ok\(\s*\n?\s*"((?:[^"\\]|\\.)*)"', source)
```

That captures the **first** string literal after `ok(`.
This corpus writes long test names as adjacent literals across source lines,
and Python concatenates them at the call — so the self-test reports

```
FAIL  T-285 the filename rule runs BEFORE the bare-identifier rule, which would otherwise eat its own prefix and leave the slug
```

while the name list holds only `T-285 the filename rule runs BEFORE the bare-identifier rule, which would otherwise eat`.
The two never compare equal, so the test is recorded **unreached** however many mutations kill it.
The optional single `\n` in the pattern also drops a name outright whenever the first literal begins on the third source line.

Measured over an unchanged test set: **182 names against 184**, i.e. two missed entirely and an unknown number truncated.
On this task's own rule the reach report listed **7** of its tests as unreached;
**3** of those had been killed by a mutation **in the same run**, and only **4** were genuine gaps.

## Why this is not a nitpick

The two instruments fail in opposite directions and only one of them is loud.
A mutation that fails nothing prints `SURVIVED` and the tool exits non-zero — it cannot be missed.
A test wrongly listed `UNREACHED` prints as a line of advice,
and the advice is *"write another mutation for a test that is already covered"*.

It is therefore the failure direction that **manufactures work and hides the real gap in the same list**:
here, four genuine gaps sat in a list of seven, and nothing distinguished them.
`C-0161`'s standard — *a mutation that fails nothing is the finding, not a gap in the test list* — has an inverse,
and this is it: **a test that appears in no killer's list may be a defect of the list**.

`CLAUDE.md` records the general shape twice already —
*a checker's blind spot is invisible in exactly the cases it misses*, and
*a checker's blind spot is found by the tool that must AGREE with it, never by the tool that produces it*.
Here the two tools that must agree are inside **one file**, twenty lines apart,
and the disagreement had been standing since `T-260` wrote them.

## The repair

`_named_tests` parses the source with `ast` and takes `ok(...)`'s first argument where it is a string constant.
`ast` folds implicit concatenation into one constant, which is exactly what the interpreter does at the call site,
so the list and the `FAIL` lines are now the **same strings by construction** rather than by a pattern that has to keep up.

The extractor is itself a predicate, so it carries a self-check that runs before the table:
a name split across two source lines must come back whole, and a single-literal name must survive.
The check runs at the top of `main` and refuses the run rather than reporting a table taken with a broken instrument.

## What would falsify this

A named test that `ast` cannot recover and the regular expression could —
a name built by a call, an f-string, or a variable rather than a literal.
None exists in either self-test today,
and such a name would be a defect in its own right: a test name that is not a constant cannot be quoted in a claim.
