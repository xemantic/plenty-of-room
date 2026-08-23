# T-293 — the refinement window reads a name too

| | |
|---|---|
| **Leaf** | none — a **process** task, protecting the census that measures the two customer-facing documents' debt |
| **Raised by** | [`C-0189`](../claims/C-0189-a-filename-cannot-supply-a-context.md) (`T-287`) §4, *"the other delta not taken here"* |
| **Verification type** | **logical** — a census over the corpus's own filenames, and a before/after delta run against the predicate committed at a named ref |
| **Units** | none; every value is an integer count or a name. Nothing physical is computed |

---

## Formulate

`tools/T-234-census.py` reads a family token out of `blank_identifiers(text)` — identifiers and
`<ID>-<slug>.<ext>` filenames replaced by spaces of the same length — and, since `T-287`, tests the
**line context** against that same blanked text.
It then calls `refine(text, ...)` with the **original** text,
so a governing word sitting inside a neighbouring **filename** can still decide
which of two discharges a token takes.

That is the third and last of three signs of one rule:
`T-285` repaired the **match**, `T-287` the **line context**, and this row is the **refinement window**.

### Why it was not simply landed with `T-287`

Two reasons, and the second is the awkward one.

1. `C-0184`'s: **one delta at a time, or none can be audited against the others.**
2. Measured over the whole corpus the change moves **one** occurrence's family,
   in a challenge file, which this census does not read — **zero** in-scope occurrences.
   So no named test over any file the census reads could hold the change open,
   and a synthetic fixture is exactly what `C-0176` §1b warns against:
   a test written to the shape of the change rather than to the corpus.

### The acceptance predicate

**PASS** iff all of:

- **`F1`** the change moves **0** occurrences into or out of the census
  (a refinement runs *after* the match and the context test, so it may rename and may neither create nor destroy);
- **`F2`** **0** in-scope occurrences change family, so `tools/T-234-classification.json` needs no regeneration
  and `C-0184`'s intermediate-reading trap does not arise;
- **`F3`** `tools/T-234-census.py --check` reads **0** before **and after**, recorded rather than asserted (`C-0158`);
- **`F4`** the change is held open by **named tests built from the corpus's own instance**, not from an invented shape,
  and a mutation restoring the original-text reading fails **those** tests and nothing else;
- **`F5`** the row's own cheap-bound question is **answered with a number** rather than inherited.

**What would falsify this approach**: a non-zero `F1`, which would mean the edit reached a rule it
is not about; or a slug census reading **zero**, which would make the in-scope zero *structural* and
the change a statement about nothing — in which case the honest deliverable is the row's other
branch, *the stated decision that a name may govern a token, and why*.

---

## Plan

**The cheap bound runs first, and it is the row's own.**
One pass over every tracked **basename** against the five governing-word patterns a refinement
consults (`_STRUCTURAL_MODEL`, `_ATTRIBUTIVE`, `_ROW_WORDS`, `_WIDTH_WORDS`, `_DRAWABLE_RASTER`)
settles whether the in-scope zero is *rare* or *structural*.
It costs one `git ls-files` and five regular expressions, and it runs **before** the predicate is touched —
because if it reads zero the change should not be landed at all.

**Then TDD.**
The tests come first and must fail: one fixture per refinement function, each built from the
corpus's own single instance rather than invented.
Then the one-token change `refine(text, …)` → `refine(hunted, …)`.

**Then the delta**, measured against the census executed out of `git show <ref>:` rather than
remembered — `C-0184`'s rule, and `T-287`'s emitter already carries the measurement function this
row was deferred from.

**Cost**: minutes. No solve, no Gradle, no re-emission of any physical study.
The census is a `tools/`-only object and this task moves no number any claim quotes.
