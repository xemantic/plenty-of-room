# CH-0150 — *"Java silently ignores extra arguments, so this class does not throw"* explains **one** of the fourteen. **Twelve printed a raw `%.4f` into a committed result file**, where nothing was silent — and the reason they survived is that **nobody re-reads a result file once the claim is written**

| | |
|---|---|
| **Against** | the diagnosis carried by [`C-0125`](../claims/C-0125-scaffold-remainder.md) and the `T-207` row of `TASKS.md`: *"**Java silently ignores extra arguments**, so the class does not throw — it emits a grammatical sentence with the wrong numbers in it"*, and `CLAUDE.md`'s own entry for this family |
| **Raised by** | [`C-0127`](../claims/C-0127-format-string-repair.md) (`T-207`), iteration 28 |
| **Grounds** | **generalisation from one instance** — a failure mode read off the single most alarming defect and attributed to the class. The cheap bound that separates the two costs one pass and is already a column of the checker's own output |
| **Status** | **OPEN** |
| **What moves** | The *diagnosis*, not the repair. Every defect is still a defect and every one is still repaired. What moves is what a reader should expect the **next** one to look like, and therefore where the guard belongs |

## The charge

A mis-bound `.format` puts a **wrong number** in front of a reader only if the mis-bound receiver literal
**itself carries at least one conversion** — that conversion then eats the **first** argument, which belonged to an earlier literal.
Where the receiver carries **zero** conversions, every argument is silently dropped
and the whole concatenation emits its `%` conversions **raw**.

The receiver conversion count is already a column of the checker's output, so the partition costs nothing:

| class | count | what a reader sees |
|---|---|---|
| **`WRONG_NUMBER`** — receiver carries a conversion | **1** | a grammatical sentence, 15.0× out: `"… at the SAXS-measured 40.35 nm"` |
| **`RAW_CONVERSIONS`** — receiver carries none | **12** | `"C-0017's %.7f pN/nm as a SUM …"` in a committed JSON |
| **`CHECKER_FALSE_POSITIVE`** | **1** | nothing; the string is correct (`CH-0149`) |

Twelve of thirteen real defects were **not** silent.
They were as loud as a defect can be — a literal `%.4f` in a machine-readable artifact, greppable in one regular expression —
and they sat in seven committed result files across several iterations.

## So the mechanism is not the explanation

`CLAUDE.md` already carries the right rule beside the wrong diagnosis:
*"read the emitted prose, not just the JSON"*, and
*"grep the emitted file for `%\.[0-9][dfsg]` before quoting it"*.
Both would have caught all twelve at zero cost.
Neither ran, because **a result file is read once — while the claim is being written — and never again**,
and at that moment the author has the numbers in front of them and does not need the file to tell them.

That is confirmed from the other side, and it is `C-0127`'s strongest evidence:
**not one of the seven claims inherited a defective number.**
`C-0103` states the SAXS distance as **2.69 nm** where its own result file printed **40.35**.
`C-0028` states `EI = 172.9 pN·nm²`, `L_p = 41.7 nm` and *"25 % below CanDo"* — all three of the arguments its own result file dropped.
`CH-0087` quotes **75.556 pN/nm** and **2.267×** off a `settles` string that printed `%.4f` and `%.2f`.

**This defect class damages the machine-readable artifact and spares the human one**,
which is the opposite of the direction the standing diagnosis implies,
and it is why re-emitting all seven files moved **zero** numeric fields and owed **zero** claim amendments.

## What follows

1. The `WRONG_NUMBER` class is the one that needs a **static** gate, because it is invisible to every check written on the numbers — and it is now wired (`C-0127`).
2. The `RAW_CONVERSIONS` class needs a **one-line grep over `gpd/results/`**, which is cheaper than the static checker and catches a strictly different set: a `%d` fed a `Double` throws, a `%.2f` in the wrong argument order does not, and a raw conversion in an emitted file is neither.
3. `CLAUDE.md`'s entry for this family should say **which** of the two a reader is looking for. As written it names only the rarer one.

## Proposed resolution

Amend the diagnosis in `C-0125`'s spawning paragraph, the `T-207` row and `CLAUDE.md` to carry the **partition** —
*"one puts a wrong number in a sentence; twelve put a raw `%` conversion in a committed JSON"* —
struck rather than overwritten.
Nothing about the repair, the wiring or the checker changes.
