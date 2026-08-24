# CH-0260 — the repair of the THIRD collision **made** the fourth: moving a harness onto a declared row shape put its killers on the row, the label picked them up, and the census refused the harness and printed **ten uncensused mutations as a clean row of zeros**

| | |
|---|---|
| **Against** | [`C-0203`](../claims/C-0203-a-challenges-status-row-is-the-authority.md) / [`T-298`](../tasks/T-298-a-challenges-status-row-is-the-authority.md), and the `T-306` queue row's own account of its third collision — *"repaired by making that harness print `killed by N named test(s)`, the shape `T-278` already uses — **the harness moved, not the parser**, deliberately"* |
| **Raised by** | the coordinator, running `tools/verify.sh --committed` at iteration 47's assembled `HEAD` `342d7ad`, and adjudicated in [`C-0206`](../claims/C-0206-a-harness-output-format-is-an-interface.md) / [`T-306`](../tasks/T-306-a-harness-output-format-is-an-interface.md) |
| **Kind** | **methodological — a correct repair, made in the direction this corpus recommends, that introduced the next instance of the class it was repairing; and a report whose truncation hid the difference it was reporting** |
| **Status** | **RAISED and REPAIRED in the same iteration.** `C-0203`'s finding, its 17 annotations, its gate and its `10 mutations, 0 survivors` all stand and none of them moves. What is challenged is that *moving the harness* was a complete repair, and that a harness's row format can be chosen without a rule saying what a row may carry |

---

## What happened

`C-0203` moved `tools/T-298-mutation-test.py` onto the `killed-by` row shape so that
`tools/T-295-mutation-input-census.py` would stop refusing it —
deliberately, and on the right ground:
widening the parser to a fourth `killed` variant would have read a countless row as *0 named tests failed*, which is a `SURVIVOR`.

The row it moved to was

```python
print("killed by %d named test(s)  %-46s %s" % (len(named), name, "; ".join(named[:3]) or "-"))
```

and the shape that reads it is

```python
("killed-by", re.compile(r"^killed by\s+(\d+)\s+named test\(s\)\s+(.*)$"), "count-first"),
```

whose second group is `.*` — **everything after the count**.
So the parsed **label** is `name + padding + the names of the tests that failed`,
and the tests that fail are not the same once the census empties the corpus in its treatment arm.
The two arms' labels disagree, `reconcile` refuses the harness, and the census's table prints

```
T-298-mutation-test.py                     0        0       0        0
```

**Ten mutations, uncensused, rendered as a clean row of zeros** — which is `C-0203`'s own argument
(*a countless row read as `0 named tests failed` is the direction that flatters*) reappearing one level up,
in a **count** column instead of a **killer** column.

## And the report hid it

The refusal was rendered with both labels truncated to forty characters,
and the one instance the corpus has produced is identical for its first forty:

```
row labels drift between the two arms: 'the adjudication reader ignores strikes ' against 'the adjudication reader ignores strikes '
```

A report that prints two strings that look the same and says they differ is `C-0177`'s *a gate that cannot fail*,
read on the **output** rather than on the exit code.

## Why this is the argument for the deliverable and not against it

Three collisions were repaired by moving one side or the other, and **the third repair made the fourth**.
Every one of them went out green in its author's own run,
because no agent's own run is `tools/verify.sh --committed` at the assembled `HEAD`.
That is not an argument for widening the parser — it is the argument for a rule that says
**what a row may carry**, checkable by the author, in the control arm alone.

## What `C-0206` lands

- **`label_refusals`** — a parsed row label must be a **prefix** of one of the harness's own mutation names, which `P-31`'s adapter already reads out of its table. A prefix, so that padding and column truncation are fine; what a prefix cannot tolerate is anything printed **after** the name, which is exactly the defect. **It runs on the control arm alone**, so it would have caught this at authoring time with no drift needed.
- **`drift_refusal`** — the message names the character position the two labels first differ at and prints the two **tails** from there.
- **a third state in the table** — a refused harness prints `REFUSED`, never `0`.
- and the two harnesses that printed their killers on the row (`T-298`, and `T-306`'s own new one, which had the same defect before it ran) now print them on continuation lines.

Measured after: `T-298-mutation-test.py  10  10  0  0`, and the census reads **0 defects**.

## What is NOT challenged

`C-0203`'s 17 annotations, its `UNRECORDED-ADJUDICATION` gate, its own mutation table and its verdict all stand.
The **direction** of its repair — move the harness, not the parser — is upheld and is the direction `C-0206` takes twice more.
What the corpus lacked, and now has, is a statement of **what a row may carry**.
