# C-0083 — `C-0067` found the longest answer in the primary deliverable had never rendered as a table cell and recorded the lesson in `CLAUDE.md`; the defect kept happening, in the **claims**, which are the artifacts NDI reads. **38 defects in 22 of 245 files, in three kinds, one of them a broken HEADER in the claim that owns `C-0017`'s mandate.** Now 0 in 243, machine-checked

| | |
|---|---|
| **Task** | [`P-23`](../../TASKS.md) — raised and closed inside iteration 16 by the coordinator, while `T-139`, `T-147` and `T-140`/`P-22` ran in parallel; prompted by [`C-0080`](C-0080-third-answers-synthesis.md), which reported one malformed `TASKS.md` row |
| **Verification type** | **logical** (a mechanical check of every tracked Markdown table against its own header, with the checker itself verified by 26 in-memory fixtures) |
| **Verdict** | **PASS. It was not one row.** A scan of **245** tracked Markdown files found **38** table defects in **22** of them, in **three distinct kinds**, none of which is visible to a writer reading their own source. All are fixed and every word is preserved; the repository now reports **0 defects in 243 files** and the check exits non-zero if that changes. |
| **Maturity** | **TRL 1–3**, and below it: nothing here is physics. It is a property of this repository's documents. **No number, verdict, premise or acceptance predicate of any claim is touched** — the edits are cell boundaries and pipe escaping. |
| **Provenance** | [`tools/check-markdown-tables.py`](../../tools/check-markdown-tables.py); **26 checks** in [`tools/test-check-markdown-tables.py`](../../tools/test-check-markdown-tables.py), written test-first; 24 Markdown files edited |
| **Conditions** | GitHub-flavoured Markdown as GitHub renders it. No Kotlin, no solver, no units — the locked-unit invariant is not engaged. |
| **Consumes** | [`C-0067`](C-0067-answers-reconciliation.md) (the original finding and the `CLAUDE.md` rule), [`C-0080`](C-0080-third-answers-synthesis.md) (the report that one row was malformed) |
| **Constrains** | Nothing. **No claim is contradicted**; this is a rendering repair. |

---

## 1. Why a documentation defect is worth a claim

`C-0067` found that `ANSWERS.md`'s §3 row (f) — *the longest answer in the primary deliverable* — had
been written across six physical lines and **had never rendered as a table cell at all**. It recorded
the lesson in `CLAUDE.md`:

> **A Markdown table row must be ONE physical line, which is the single place semantic line breaks do
> not apply.** Never break a row at a clause boundary, and check `|`-counts after editing a table.

That was three iterations ago. The rule was recorded and **the defect kept happening** — not in the
deliverable, which had just been audited, but in the **claims and challenges**, which are the
artifacts `SESSION-PROMPT.md` says are the record. A mangled table in a claim is a mangled piece of
*evidence*: the reader sees a row with its columns shifted, or a header with phantom columns, and has
no way to know which.

It is entirely machine-checkable, so it should be machined. This is the same argument
[`C-0078`](C-0078-status-drift-in-the-deliverable.md) made two iterations earlier about status drift
and [`P-16`](../tasks/P-16.md) made about the snapshot helpers: *a check nobody remembers to perform is
not a check.*

## 2. The three kinds, all invisible in the source

| kind | what it looks like in the source | what the reader gets |
|---|---|---|
| **bare `\|` in a body cell** | `` `\|F_es\| d ln μ/dh` `` written with **unescaped** pipes | GFM splits table cells **before** it parses backticks, so inline code does not protect a pipe. Two extra columns, silently. |
| **bare `\|` in a HEADER** | `` \| 10 nm \| `V*` \| `ℓ = \|F_es\|/\|k_es\|` \| … \| `` | The **whole table** widens. Every correct body row is then the odd one out — so a naive checker blames the wrong lines, and a writer scanning the rows finds nothing wrong with any of them. |
| **a missing cell** | a row genuinely one cell short of its header | The columns shift for that row only, so a value appears under the wrong heading — the most dangerous kind, because it is still legible. |

The only correct way to write a literal pipe in a cell is `\|`, and that works inside code spans too.
Most of this repository's well-formed rows already do it; the defects are where a writer reached for
absolute-value notation and the backticks looked like enough protection.

## 3. What was found

**38 defects across 22 of 245 tracked files.** The ones worth naming:

- **`C-0017`**, whose stability-floor table carries `k_c*/floor` margins quoted in four other claims,
  `TASKS.md` and `ANSWERS.md` — a **HEADER** defect from `` `ℓ = |F_es|/|k_es|` ``, widening a
  five-column table to nine. The claim that owns the programme's coupling mandate had a table nobody
  could read correctly.
- **`CH-0070`**, five consecutive evidence rows each **missing a cell** — the challenge whose whole
  content is a table of measured backbone geometry against what a design would need.
- **`C-0076`**, filed in iteration 15, three of five falsifier rows with the verdict merged into the
  outcome cell — so `F1` and `F2` reported *"fired?"* in its own column and `F3`–`F5` did not.
- **`C-0067`** itself, the answers-reconciliation claim, with a blank three-column header over
  two-column data.
- **`TASKS.md`**'s `T-60` and `T-132` rows (bare pipes), and **two rows I had written myself earlier
  in this session** with a spurious fifth cell copied from the science table's shape into the
  four-column process table. The check found its author's own mistakes, which is the point of it.

**Every fix preserves every word.** 20 rows were repaired mechanically, by escaping only the pipes
inside inline-code spans and accepting the edit **only where it strictly reduced the defect set** —
so the repair is self-verifying rather than trusted. The remaining 18 needed a judgement about where a
merged cell should split, and each was split at an existing em-dash or sentence boundary. Word counts
before and after agree to within two words per file, the differences being the em-dashes that became
cell boundaries and one `RAISED.` added to a status cell that had none.

## 4. `third-party/` is excluded, and that is what makes the check usable

`third-party/` holds the problem definition **as received, unmodified** — a standing invariant of this
repository, not a preference. Its §6 task table has a row that does not render, and **it must keep
it.**

A checker that reports a defect nobody is permitted to fix can never come back clean, and a check that
never comes back clean cannot be a gate — it becomes a number people learn to ignore. So the exclusion
is what makes the tool usable rather than a weakening of it, it is asserted as a test, and an explicit
path argument still overrides it so the file can be inspected on purpose.

> **An invariant that forbids fixing something must be taught to the checker, or the checker decays
> into a warning.** This is the second instance of the shape in three iterations, after `C-0078`'s
> *"run the check unconditionally"*: a check's **output discipline** is as much a design decision as
> its logic.

## 5. What this does not do

- It does **not** check that a table's *content* is right, only that its shape is. A value under the
  wrong heading in a row of the correct width is invisible to it.
- It does **not** check the other rendering hazards `CLAUDE.md` records — a KDoc comment terminated by
  a starred symbol followed by a slash, or a `String.format` placeholder miscount. Those are Kotlin
  and are caught by the compiler and the suite respectively.
- It is **not yet wired into anything that runs.** That is `P-21`, in flight this iteration with
  `P-22`, and this script is now the **third** retained harness test in the same position — real
  checks that nothing invokes. The agent holding `P-21` has been told.
