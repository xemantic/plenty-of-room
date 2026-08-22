# C-0189 — a filename cannot supply a premise family's line CONTEXT either: **10 occurrences leave the census, 0 enter, and not one of them is debt** — and the delta of the opposite sign that `C-0184` split this task off to keep separate is measured at **0 in-scope occurrences** and left standing

| | |
|---|---|
| **Task** | [`T-287`](../tasks/T-287-a-filename-cannot-supply-a-context.md), raised by [`C-0184`](C-0184-a-slug-is-not-a-statement.md) (`T-285`) while bounding its own scope |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests, two mutation rows on the existing harness, and a before-and-after reading taken by running the **committed** predicate against the repaired one |
| **Verdict** | **PASS on `F1`–`F6`.** **387 → 377** occurrences in **41 → 40** files; **10** removed, **0** added; **0** of the removed carry a class this census gates, which is the acceptance predicate; the gate reads **0** defects before and **0** after the regeneration; the debt line is **unmoved** |
| **Maturity** | TRL 1–3 process artifact. Nothing here is a measurement of the physics; it is a measurement of what a supersession census is entitled to count |
| **Provenance** | `tools/T-234-census.py` (the predicate and its named tests), `tools/T-234-mutation-test.py` (two new rows, one retired), `tools/T-234-emit-classification.py` (the regeneration), `tools/T-287-emit-result.py`; result `gpd/results/T-287-a-filename-cannot-supply-a-context.json`, `baselineRef` `2c043809540a68c1e4e8be356b5b736ea75a6ce9` |
| **Conditions** | Documents and tools only. No Kotlin source is touched, no result file other than this task's own is emitted, and **no physical number in the corpus moves**. The classification table is regenerated, which moves indices and nothing else |
| **Consumes** | [`C-0184`](C-0184-a-slug-is-not-a-statement.md) (`T-285`, *a slug is not a statement*, and the blanking rule this reuses), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (the family split, the override mechanism, and *measure the cure before writing it*), [`C-0182`](C-0182-name-the-discharge.md) (`T-281`, the discharge registry), [`C-0158`](C-0158-prose-gate-red.md) (record the gate's own reading) |
| **Constrains** | every future reading of `tools/T-234-census.py`'s families, and therefore the debt line both outward-facing documents are measured against |

---

## The claim, in one line

`T-285` stopped a family token **matching** inside a link target on the ground that *a slug is a name and not a statement*;
the same tool then asked whether the token's **line** carried its family's context word,
and asked it of the **unblanked** line — so a name could still admit a match that no word of the line asserts.

## 1. The defect, and why `T-285` left it

`occurrences()` matched against the blanked text and tested the context against `text.split("\n")`.
`context_distance()`, which exists to say *the line context said nothing about this token*, read the original text for the same reason.

`C-0184` left it deliberately and pinned the behaviour with a named test, because the two repairs run in **opposite** directions:
`T-285` **removes** matches and this **admits** them,
so taking both in one pass would put two deltas of opposite sign into one before-and-after list and neither could be audited against the other.

That pinning test is **inverted** here rather than struck.
*Strike, never delete* is a rule about a **statement**; a test that asserts a repaired defect is not a record of anything, it is a false assertion.
The sentence it was written under is preserved above the inverted test, which is what a record of the old scope actually needs.

## 2. The cheap bound decided the whole task, before anything was written

The change is one expression and the measurement is a diff of two runs over the corpus.
Run first, with each removed occurrence's class read out of the **committed** classification:

| class at the baseline ref | occurrences removed |
|---|---|
| `CORRECT` | 6 |
| `RECORD` | 2 |
| `RESTATED` | 1 |
| `OUT_OF_SCOPE` (a hand override) | 1 |
| **a class this census GATES** | **0** |

`F2` is exactly that zero.
Had any removal carried a gated class, a live debt statement would have been admitted only by a filename,
the repair would have **hidden** debt, and the honest answer would have been *state the decision and keep the original line*.
The bound costs one pass and it is what says the expensive half is worth buying.

**The before reading is run, not remembered.**
`tools/T-287-emit-result.py` reads `tools/T-234-census.py` and `tools/T-234-classification.json` out of `git show <ref>:` and executes them there,
so each removed occurrence's class is the class the **committed** table really gave it.
Reading the classes out of today's table after the change would compare old occurrences against a re-indexed one,
and `C-0184` recorded that intermediate reading as measuring nothing.

## 3. The ten, one at a time

Every one of them is in a metadata row that is a **list of links** — a `Consumes`, a `Raises`, a `Provenance` — or in a queue row whose only context word is inside a link target.

| file | line | class at the baseline ref |
|---|---|---|
| `TASKS.md` | 688 | `RECORD` |
| `TASKS.md` | 688 | `RECORD` |
| `C-0140-…` | 8 | `CORRECT` |
| `C-0140-…` | 13 | `CORRECT` |
| `C-0140-…` | 13 | `CORRECT` |
| `C-0175-…` | 12 | `RESTATED` |
| `C-0175-…` | 13 | `OUT_OF_SCOPE`, by hand |
| `C-0176-…` | 12 | `CORRECT` |
| `C-0176-…` | 12 | `CORRECT` |
| `C-0182-…` | 69 | `CORRECT` |

The tokens are not spelled here, for the reason the `T-285` and `T-287` queue rows do not spell them:
this claim is in the census's own scope and a claim about a token would add occurrences of it.
They are in the result file, which is not.

**A key collision was found while measuring this and it under-reported by one.**
Keying an occurrence on `(file, line, token, family)` merges the two on `TASKS.md`'s own row — a queue row is a paragraph on one physical line — and the first reading was **9**.
The census's own `snippet` is centred on the token and tells them apart; the reading is **10**.

## 4. What moved and what did not

| | before | after |
|---|---|---|
| occurrences in scope | **387** | **377** |
| files | **41** | **40** |
| added | — | **0** |
| `tools/T-234-census.py --check` | **0** defects | **0** defects |
| remote-context advisory | **8** | **8** |

The **debt line is unmoved**, and that is a check rather than a coincidence:
not one of the ten is in either outward-facing document, so neither the numerator nor either denominator can move.

The remote-context list changes composition without changing its count:
two entries go with the occurrences themselves, and one enters because its nearest context word was inside a filename —
the diagnostic doing its job rather than a regression, and precisely why the distance had to travel with the admission test.
A distance measured on the original text can only **under**-report, because a filename is a context word it counts and the admission rule does not;
the two must come from one text or the diagnostic contradicts the rule it diagnoses.

## 5. The regeneration, and the one hand override it drops

Run **last**, after every predicate change was settled, with `git status` read first for a foreign in-flight file (`C-0176` §1b).
A family change moves a class and the table is keyed on the index, so this is not optional:
with the predicate in and the table still keyed on the old indices the gate reads **10** stale entries, and that number measures nothing.

**7** hand overrides at the baseline ref, **6** carried, **1** dropped and named: the `OUT_OF_SCOPE` reading on `C-0175`'s line 13.
It is not lost to a key collision — the occurrence it was written about **no longer exists**,
because the statement it qualified was admitted by a filename and by nothing else.
That is the override mechanism behaving as `C-0176` designed it: **dropped loudly, never silently moved**.

## 6. The other delta, measured and NOT taken

`REFINE_WINDOW` and `STRUCTURAL_WINDOW` read the original text too,
so a filename can supply a **governing** word in the same way.
Measured over the whole corpus it moves **one** occurrence's family, in a challenge file, which this census does not read:
**0 in-scope occurrences**.

It is not taken here, on `C-0184`'s own ground one level down — one delta at a time, or neither can be audited —
and on a second ground that is specific to it: with zero in-scope effect,
**no named test over any file this census reads could hold the change open**, so it would land untested.
It is queued as a row of its own.

## 7. Acceptance predicates

| | |
|---|---|
| **F1** — one rule, one text; blanking length-preserving so offsets and line numbers are unmoved | **PASS**, §1, and a named test asserts the offset |
| **F2** — every reclassification read one at a time, and **no** gated class removed | **PASS**, §2 and §3: 0 of 10 |
| **F3** — the gate does not move; the debt line reported before and after | **PASS**, §4: 0 and 0, debt unmoved |
| **F4** — the two deltas stay separable, and the refinement window measured separately | **PASS**, §6 |
| **F5** — the classification regenerated last, `git status` read first, dropped overrides named | **PASS**, §5 |
| **F6** — the mutation test | **PASS**: two new rows on `tools/T-234-mutation-test.py`, both killing named tests; **0 mutations failing nothing** |

**And a mutation had to be RETIRED, which is the finding beside the repair.**
`T-285` carried this very change as a **widening** mutation, to hold its own stated scope open.
`T-287` takes that widening, so the mutation is now a no-op — and the harness said so, reporting it as the run's only row failing nothing.
It is replaced by two **narrowings** that restore the original-line reading of the context and of the distance.
**A mutation table is dated by the predicate it mutates**, exactly as a census is dated by its premise set.

## 8. Validity range

This is a statement about `tools/T-234-census.py`'s **line context** test and its distance diagnostic, over the in-scope corpus at the recorded ref.
It says nothing about the refinement window (§6), nothing about any other tool that reads a line of Markdown,
and nothing physical.
The occurrence counts are a reading of a **moving** corpus and the result file records the ref they were taken at:
a concurrent claim's queue row landed mid-iteration carrying one family token, which is `CH-0182` for the tenth consecutive iteration,
and it moved the absolute counts by one at both ends while moving the **delta** — 10 removed, 0 added, 0 of a gated class — not at all.
