# T-285 — a census token that fires inside a FILENAME is a link target, not a statement

| | |
|---|---|
| **Raised by** | [`C-0182`](../claims/C-0182-name-the-discharge.md) (`T-281`), while measuring its own footprint |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable named self-tests, a mutation test in both directions, and an exhaustive false-positive measurement over every span the new rule blanks |
| **Units** | none; this is a document-integrity task |

## Formulate

`tools/T-234-census.py` blanks task, claim and challenge **identifiers** before it matches its
premise families, so that `T-132` cannot be read as the 132-station census.
`blank_identifiers` blanks the identifier and **not the file the identifier names**:
in a link written `[C-0175](gpd/claims/C-0175-drawable-raster-rim.md)` the identifier goes and
`-drawable-raster-rim.md` stays, so the `WIDTH` family's own token fires inside a **link target**.

A link target is a name.
It asserts nothing about a honeycomb row length, a uniform tile width, or anything else,
so it is neither debt nor a restatement — it is not a statement at all,
and it reaches the gate as an occurrence that cannot be classified because there is nothing to read.

`C-0182` measured **5 of 40** occurrences of that token corpus-wide as the claim's own slug
and left the repair for this row, on one ground:
blanking a span **moves every index below it**,
and `tools/T-234-classification.json` is keyed on the index,
so the repair is only meaningful together with `T-282`'s regeneration and must land **before** it.

**Numeric target.**
Every occurrence of a family token that falls inside a `<ID>-<slug>.<ext>` filename is removed from the census;
the measured false-positive rate of the blanking is **0**;
no surviving occurrence changes its family, its token or its neighbourhood;
and the gate's unclassified count falls by exactly the number of such occurrences that were unclassified.

**Acceptance predicates, falsifiable.**

- **F1 — the defect is reproduced and then removed.** Before the repair, a family token inside `<ID>-<slug>.<ext>` fires; after it, it does not. Both directions are named tests.
- **F2 — the rule is ORDERED, and the order is asserted behaviourally.** The filename pattern must run **before** the bare-identifier pattern: the bare pattern blanks `C-0175` and leaves the slug, after which the filename pattern can no longer match its own prefix. A test that only asserts *"the slug is blanked"* fails if the order is wrong, which is what makes the order testable rather than commented.
- **F3 — the blanking is length-preserving.** Every offset the census reports, every snippet, every strike span and every hand-override key indexes the file as it is on disk. A non-length-preserving blanking moves all of them.
- **F4 — the blanking touches the MATCH and never the CONTEXT, the REFINEMENT or the SNIPPET.** Those three read the original text, so the whole effect of the repair is the removal of the in-filename occurrences and the index shift that follows. Asserted by measurement: every surviving occurrence keeps its `(file, family, token, snippet)` identity, and the census count falls by exactly the measured number.
- **F5 — the false-positive rate is measured exhaustively, not sampled.** Every span the new pattern blanks over the in-scope corpus is enumerated and each is resolved against the repository's own file listing, current **and** historical. A span that is not a filename is a false positive and falsifies the rule.
- **F6 — no legitimate statement is lost.** Every occurrence the rule removes is read one at a time: a removal is correct only where the token is inside the filename and the sentence around it is untouched. A removal that silences a sentence genuinely discussing the family falsifies the rule. `CLAUDE.md`: *a drift checker's false positives cost more than its true ones.*
- **F7 — the mutation test.** Every rule fails at least one **named** test when mutated; a mutation **replaces** a rule wholesale and never widens it to `original|mutant` (`C-0177`'s measured trap, 9 of 22 rows); the predicate is a **widening** of an exclusion, so it is mutated in **both** directions — narrowed until the defect returns, and widened until it blanks prose it must not — and the count of mutations that fail **nothing** is reported. The harness reproduces `<tmp>/tools/*.py` beside `<tmp>/TASKS.md` and subtracts a measured baseline (`CH-0237`).
- **F8 — this task's own artifacts are inside the census's scope, and are written not to move it.** `gpd/claims/` is in scope. `CH-0182`: *a claim about a census is inside that census's own scope, and writing it moves the number it reports.* The claim this row files therefore **does not spell the family token in prose**, exactly as this queue row does not, and the census is asserted to find **zero** occurrences in it. Both readings are recorded anyway.

**What would falsify the approach.**
A single span matched by the pattern that is **not** a filename,
or a single removed occurrence whose token inside the filename was nevertheless doing the work of a statement.
Either would make the rule a silencer rather than a blanking,
and the honest answer would be the row's other branch — declare a slug in scope and classify the occurrences by hand.

## Plan

**Cheap bound first, and it is two greps.**

1. *How many occurrences does this touch?* Run the census's own `occurrences()` over the in-scope corpus and intersect the offsets with the spans of `\b(?:CH|C|P|T|S)-\d{1,4}[a-z]?-[A-Za-z0-9-]+\.[A-Za-z0-9]{1,5}\b`. **8 of 394**, in four files, one family token. `C-0182`'s **5** is the count of the **claim** slug alone; the other three are a task file and a result file named inside `C-0175` itself, so the row's number is a strict lower bound on its own scope and the general shape is `<ID>-<slug>.<ext>` rather than `C-NNNN-<slug>.md`.
2. *What extensions does the corpus actually use behind an identifier?* `md` 3245, `json` 586, `py` 253, `txt` 13, `sh` 5 — and **0** of anything else. So a generic `\.[A-Za-z0-9]{1,5}` costs nothing today and does not have to be revisited when the next artifact kind appears; an enumerated list would silently reintroduce the defect on the first `.csv`.

Both run before any edit, and together they say the repair is one pattern rather than a family of them.

**Why blank rather than classify.**
The row offers two branches.
Classifying the occurrences is a hand judgement per occurrence that has to be repeated every time a claim cites another claim,
and this corpus cites `C-0175` often — so the debt would be recurrent and the classification would be the same reading every time.
Blanking is one line, it is testable in both directions, and it is exactly what `blank_identifiers` already exists to do:
its docstring says *"replace every task/claim/challenge identifier"*, and a filename **is** that identifier with a slug and an extension after it.
The branch is chosen on cost and on the fact that the reading never varies.

**Why the false-positive measurement is exhaustive rather than sampled.**
`CLAUDE.md` records that a drift checker's false positives cost more than its true ones,
and that a false-positive **rate** is not a completeness argument (`CH-0204`).
The population here is small enough to enumerate — 4 102 spans — so the measurement is a census and not a rate,
and every span is resolved against `git ls-files`, `--others --exclude-standard`, **and** every basename that has ever existed in the history,
so a link to a file that was renamed still resolves.
The residue is read by hand.

**What this task deliberately does not do.**
It does not touch the **context** test, which reads the original line and can therefore take its honeycomb context
from the word *honeycomb* inside a neighbouring filename.
That is a real and separate effect, it runs in the **opposite** direction (it admits matches rather than removing them),
and repairing it would move classifications this task cannot audit in the same pass.
It is measured here and left as its own row.
