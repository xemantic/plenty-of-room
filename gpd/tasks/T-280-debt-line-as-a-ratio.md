# T-280 — the advisory `T-233 debt` line published as a RATIO, and the denominator `CH-0230` named is the one that does not work

| | |
|---|---|
| **Leaf** | none — a process task, protecting the census that protects every honeycomb leaf |
| **Raised by** | [`CH-0230`](../challenges/CH-0230-the-debt-line-grows-when-the-documents-are-corrected.md) candidate 2, filed by [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) (`T-262`) |
| **Claim** | [`C-0179`](../claims/C-0179-the-debt-line-as-a-ratio.md) |
| **Result** | `gpd/results/T-280-debt-line-as-a-ratio.json` |

---

## Formulate

`tools/T-234-census.py --check` prints an advisory line —
*"`T-233` debt N occurrence(s) in the two deliverables"* —
and every synthesis pass since [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) has read it as a work list.

[`CH-0230`](../challenges/CH-0230-the-debt-line-grows-when-the-documents-are-corrected.md) measured it over the last **40** revisions of `ANSWERS.md` and `DECISIONS-FOR-NDI.md` and found that **every single increase is a synthesis pass**,
under the old predicate (`0 → 25`) and under `C-0176`'s split (`0 → 10`) alike,
because a correcting sentence has to **name** the withdrawn premise in order to withdraw it.
The count is not challenged; the word *debt* is.

The challenge names three candidate remedies.
Candidate 1 — say so in the output — is **DONE**.
This task is candidate 2: publish the number as a **ratio**, so that the line falls when a pass adds pointed sentences,
which is the behaviour the word *debt* implies.
It also **prices** candidate 3 — counting **lines** rather than occurrences — rather than assuming it is dearer.

The question the row calls *"needs no new predicate, only the number published as a fraction"* is to be **verified, not inherited**:
a fraction has a **denominator**, and `CH-0230` states one — *"unpointed occurrences over all occurrences of **the same families**"*.
Whether that denominator delivers the promised behaviour is a measurement and not a matter of taste.

### Acceptance predicate

`tools/T-234-census.py --check` prints, beside the count, a **ratio whose denominator is named in the output**,
and the ratio's behaviour over the same 40-revision range is **measured** and published —
not asserted, and not assumed to fall.

### Declared falsifiers

| | falsifier |
|---|---|
| **`F1`** | **the ratio does not fall at any pass where the count rose.** Then candidate 2 delivers nothing the count does not, and the honest deliverable is to say so and leave the line a count. This is a real possibility: the numerator and the denominator both grow on a correcting pass, and a ratio below one that gains equal amounts top and bottom **rises** |
| **`F2`** | **a mutation of any new rule fails no named test** — a rule nothing asserts (`C-0127`, raised by `C-0150` to both directions, and `C-0161`'s reading that a silent mutation is the finding) |
| **`F3`** | **the two readings `C-0176` publishes — with and without that claim's own worked examples — differ for this line.** Then the ratio must say which reading it is, per `CLAUDE.md`'s *name the set inside the field* |
| **`F4`** | **candidate 3 changes the SIGN of the growth where candidate 2 does not.** Then counting lines is the remedy and this task has built the wrong one, and must say so plainly |

### Units and conventions

No physics. Every quantity is a **count** of occurrences or of physical lines, or a dimensionless ratio of two counts.
Counts are integers; ratios are exact rationals emitted at the corpus's nine significant digits with a floor of **zero**,
because `RESULT_ABSOLUTE_FLOOR` is a claim in the locked units and does not travel (`P-18`).
The historical series is taken out of `git` — `git show <sha>:<path>` — never out of the working tree,
and the result file records the **resolved** `baselineRef`, per `CLAUDE.md`'s `T-249` entry,
taking the ref as an argument defaulting to `HEAD`.

## Plan

**Cheap bound 1 — measure every candidate denominator over history BEFORE writing one.**
One loop over `git show`, forty revisions, no solve.
Three denominators are available without a new predicate:
every occurrence of the **subject** families (which is the one `CH-0230` names),
every occurrence of **any** family the census finds in the two deliverables,
and pointed-plus-unpointed with the struck excluded.
The loop says which of them falls where the count rose, and it runs before a line of the tool is edited.

**Cheap bound 2 — price candidate 3 with one `Counter`.**
A line-keyed census collapses every occurrence on one physical line onto one key.
The number that decides it is not the compression factor but **how many lines carry occurrences of more than one class, or of more than one discharge** —
because `C-0176`'s whole architecture is that a family carries its own pointer set and a class is a reading *per occurrence*.
If that number is not zero, a line-keyed census cannot represent `T-260`'s partial discharge at all,
and the compression it buys is irrelevant.

**Then, and only then, the tool change**, test-first:
a named function with named self-tests, the ratio printed beside the count with the denominator named in the same output,
a `tools/T-280-emit-result.py` recording the resolved ref and both series,
and a `tools/T-280-mutation-test.py` in which every new rule is mutated **wholesale** — never widened to `original|mutant`, which is a no-op and which `C-0176`'s own first table did on 9 of 22 rows.

**What would falsify the approach.**
If cheap bound 1 finds no denominator that falls where the count rose, candidate 2 is dead and the deliverable is that finding.
If cheap bound 2 finds no mixed-class line, candidate 3 is cheap and must be weighed on its merits rather than dismissed.

## Cost

Both cheap bounds are seconds of `git show` and one pass over the census's own predicates.
No Kotlin compiles, no study runs, no Gradle.
