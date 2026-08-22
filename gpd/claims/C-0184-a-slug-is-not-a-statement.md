# C-0184 — **A CENSUS TOKEN THAT FIRES INSIDE A FILENAME IS A LINK TARGET, AND A LINK TARGET ASSERTS NOTHING — SO CITING A CLAIM COST AN UNCLASSIFIABLE OCCURRENCE, AND THE REPAIR HAD TO LAND *WITH* THE REGENERATION BECAUSE BLANKING A SPAN MOVES EVERY INDEX BELOW IT.** `T-285`: [`tools/T-234-census.py`](../../tools/T-234-census.py) blanked an identifier and not the file the identifier names, so one premise family's own token fired inside the slug of a claim's filename — **8 of 394** in-scope occurrences, in **4** files. `C-0182`'s **5** is the count of the *claim* slug alone; the other three are a task file and a result file named inside [`C-0175`](C-0175-drawable-raster-rim.md) itself, so the shape is an identifier, a hyphenated slug and an **extension**, not one claim's filename. The rule is one pattern, ordered **before** the bare-identifier rule, and its false-positive count is **0 of 4 149** spans — exhaustively, not sampled: **4 146** resolve to a basename that exists or has ever existed, and the **3** that do not are two claims quoting a *broken* filename in order to say it is broken. Nothing else moves: **0** surviving occurrences change family, token or neighbourhood. `T-282`: the gate then regenerates from **21** defects to **0**, of which **5** are the predicate's and **16** the regeneration's, with **7** hand overrides carried and **0** dropped. **The intermediate reading is the finding**: with the rule in and the table still keyed on the old indices the gate reads **9** on the working tree and **8** on the ref's own corpus, and neither number measures anything at all.

| | |
|---|---|
| **Task** | [`T-285`](../tasks/T-285-a-slug-is-not-a-statement.md) (raised by [`C-0182`](C-0182-name-the-discharge.md), `T-281`) and [`T-282`](../tasks/T-282-classification-regeneration.md) (raised by [`C-0179`](C-0179-the-debt-line-as-a-ratio.md), `T-280`) |
| **Leaf** | — (process; the corpus's own integrity machinery) |
| **Verification type** | **logical**, as executable named self-tests, a mutation table in both directions, and two exhaustive censuses over the corpus. No solve, no Kotlin, no Gradle |
| **Verdict** | **PASS on all fifteen predicates.** `T-285`: `F1`–`F8` all discharged, none fired. `T-282`: `F1`–`F7` all discharged; **`F4` FIRED, twice, and that is the result** — two readings the emitter's rules got wrong were found and repaired rather than regenerated over (§5) |
| **Maturity** | **TRL 1–3.** These are statements about documents, not about a folded object. Nothing here is measured on DNA |
| **Provenance** | [`gpd/results/T-285-a-slug-is-not-a-statement.json`](../results/T-285-a-slug-is-not-a-statement.json) and [`gpd/results/T-282-classification-regeneration.json`](../results/T-282-classification-regeneration.json), both from [`tools/T-285-emit-result.py`](../../tools/T-285-emit-result.py) (**new**), which takes the ref as an argument, defaults it to `HEAD` and records the **resolved** SHA (`CH-0210`). Every count is derived at emit time: the occurrence census by running it, the *before* gate by running today's `classify` against the ref's own committed table under the ref's own blanking, the mutation numbers by running the mutation test. **No wall-clock timing and no step counter is emitted.** Edited: [`tools/T-234-census.py`](../../tools/T-234-census.py) (one pattern, one ordering, **18** named tests), [`tools/T-234-emit-classification.py`](../../tools/T-234-emit-classification.py) (one registration, §5), [`tools/T-234-mutation-test.py`](../../tools/T-234-mutation-test.py) (**13** mutations and a repair to its own name extractor, §6), and [`tools/T-234-classification.json`](../../tools/T-234-classification.json) (regenerated). **Tests first, watched to fail**: the first batch of thirteen was written before the pattern and **six** of them failed, the other seven being over-blanking guards that hold vacuously until a widening exists to be taken too far; five more were added as the mutation table demanded them. `T-234-census.py --self-test` **0 failures**, `T-234-emit-classification.py --self-test` **0 failures**, `T-234-mutation-test.py` **55 mutations, 0 survivors**. Clean: `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-markdown-tables.py`, `check-cold-start-note.py`, `check-entry-points.py`, `check-queue-vocabulary.py` (and `--selftest`), `check-result-file-hygiene.py` on `--prose`, `--departures` and `--saturated`, `trace-answers.py` on both deliverables, `test-trace-answers.py`, and the `T-281`, `T-283` and `P-30` mutation tests. **No Gradle run**: nothing in `src/` is touched, and the claim says so rather than quoting a suite count it did not take |
| **Conditions** | The corpus as it stood on the working tree of iteration 43, baseline ref `9620d3e`. Scope: `gpd/claims/*.md`, `TASKS.md`, `ANSWERS.md`, `DECISIONS-FOR-NDI.md` — **182** files scanned, **41** carrying at least one occurrence. `git status` was read before the regeneration and is recorded in §4 |
| **Consumes** | [`C-0182`](C-0182-name-the-discharge.md) (the defect, and its own measurement of it), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (the family split, the hand-override mechanism, and §1b's inherited-stale-regeneration trap), [`C-0179`](C-0179-the-debt-line-as-a-ratio.md) (the debt line and its two denominators), [`C-0177`](C-0177-queue-status-vocabulary.md) (a mutation replaces a rule wholesale), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (a mutation that fails nothing is the finding) |
| **Constrains** | `T-285` and `T-282` are **DONE**. The gate [`tools/T-234-census.py --check`](../../tools/T-234-census.py) exits **0** for the first time since iteration 42. A new row, `T-287`, is opened for the effect this task deliberately did **not** repair (§7) |
| **Raises** | [`CH-0238`](../challenges/CH-0238-a-tools-own-instrument-was-wrong-about-which-of-its-tests-are-load-bearing.md) against `tools/T-234-mutation-test.py`'s reach report (§6) |

---

## 1. The defect, and why the raising claim's number is a lower bound on its own scope

`blank_identifiers` exists so that a task identifier cannot be read as a quantity —
so that a queue row's number is not mistaken for the count its digits happen to spell.
It blanked the **identifier** and left the **file the identifier names**:
in a link written `[C-0175](gpd/claims/C-0175-drawable-raster-rim.md)`
the identifier goes and the rest of the filename stays,
and one premise family's own adjectival token duly fired inside the link target.

A link target is a **name**.
It asserts nothing about a row length, a tile dimension or anything else,
so it is neither debt nor a restatement — it is not a statement at all,
and it reached the gate as an occurrence with nothing in it to read.

`C-0182` measured **5 of 40** occurrences of that token corpus-wide as the slug.
That number is right and it is the count of the **claim** slug alone.
Derived from the shape — an identifier, a hyphenated slug, and an extension — the count is **8**,
because a **task** file and a **result** file are named the same way,
and three such occurrences sit inside `C-0175` itself.
The general form is what the rule is written on;
the particular form is what the raising claim happened to have in front of it.

## 2. The rule, and the two things that make it testable rather than commented

One pattern, `SLUG_FILENAME`, prepended to the blanking rules.
Two properties carry the whole repair and each is a named test rather than a comment:

**Order.** The filename rule must run **before** the bare-identifier rule.
Run second it never matches, because the bare rule has already eaten `C-0175` out of the front of the filename and left the slug behind.
A test that asserts only *"the slug is blanked"* fails when the order is wrong,
which is what makes the order testable at all.

**Length preservation.** Every offset the census reports, every snippet, every strike span and every hand-override key indexes the file as it is on disk.
A blanking that shortens the text moves all of them.

The extension is **generic** rather than enumerated.
Behind an identifier this corpus uses `md` (3 264), `json` (594), `py` (273), `txt` (13) and `sh` (5), and **nothing else**,
so a list costs the same today and silently reintroduces the defect on the first `.csv`.

## 3. The measurements, and what a false-positive rate is not

| | |
|---|---|
| in-scope occurrences before | **394** |
| in-scope occurrences after | **386** |
| removed | **8** — 5 the claim slug, 3 a task or result filename |
| surviving occurrences that changed family, token or neighbourhood | **0** |
| spans the rule blanks over the in-scope corpus | **4 149** |
| resolving to a basename that exists, is untracked, or has ever existed | **4 146** |
| not resolving | **3** — two claims quoting a *broken* filename in order to say it is broken |
| **false positives** | **0** |

Those two totals are a reading of a **moving** corpus and they moved while this claim was being written —
**4 102** before it existed, **4 149** with it and this iteration's queue rows in —
which is `CH-0182` once more.
What does **not** move is the quantity the predicate is judged on: **0** false positives, at every reading taken.

The census is **exhaustive, not sampled**.
`CH-0204` records that a false-positive **rate** is not a completeness argument,
and this population is small enough to enumerate,
so every span is resolved against `git ls-files`, the untracked listing, **and** every basename in the history —
a link to a file that has since been renamed still resolves.

**No legitimate statement is lost.**
Every removed occurrence is a token inside a filename inside a link;
the sentence around each is untouched,
and where a file is both linked **and** discussed the statement keeps its own occurrence.
That last case is a named test, because it is the one an over-blanking would take.

**What requiring an extension costs.**
Nineteen distinct identifier phrases with no extension stand in the corpus, over **59** occurrences —
every one a source **directory** or a result-file **stem** used as a table key —
and **none** of them carries a family token.
So the extension is what distinguishes a file reference from prose, and requiring it costs nothing measurable.

## 4. The regeneration, and the intermediate reading that measures nothing

**The tree was read, not assumed.** `git status` before the regeneration showed
four uncommitted claims — `C-0180`, `C-0181`, `C-0182`, `C-0183` — each complete
(a headline, a metadata table, eight or nine numbered sections),
three uncommitted challenges, this iteration's Kotlin sources, and no half-written markdown anywhere in the census's scope.
`C-0176` §1b is the ground for looking: the regeneration it inherited had itself gone stale inside one task.

| state | gate defects |
|---|---|
| the baseline ref `9620d3e`, its own tools against its own corpus | **18** |
| the working tree, before anything in this task | **21** |
| the working tree, `T-285`'s rule in and the classification still keyed on the old indices | **9** |
| the ref's corpus, `T-285`'s rule dropped in over the ref's own classification | **8** |
| after the regeneration | **0** |

**Neither the 9 nor the 8 is a reading of anything, and that is the point of the ordering.**
Removing an occurrence moves every index below it,
so a stale index table lines up against *different* occurrences and its defect count falls for a reason that is not a repair.
It is why `T-285` is a rider on `T-282` and could not have been filed alone —
and both numbers are emitted, so that *"this reading measures nothing"* is something a reader can reproduce rather than a sentence to be taken on trust.

The 21 partition exactly:
**5** were removed by the predicate — five unclassified occurrences that are no longer occurrences —
and **16** by the regeneration:
six *wrong discharge* entries on queue rows written before `C-0176` split the families,
four unclassified queue rows added since the last regeneration,
five unclassified statements in `C-0175`,
and one in `C-0182`.
Every one of the four newly classified queue rows is **struck or pointed**,
which is what makes `MOVED` the right class there rather than a live assertion.

**Hand overrides: 6 before, 7 after, 0 dropped, 0 key collisions.**
The seventh is new and is §5's first item.
The table went from 39 files and 379 entries to **41** and **386**,
and only **three** files changed at all.
Re-running the emitter on its own output is **byte-identical**, so the table is a fixed point.

The debt line is unmoved — **24 of 88 = `0.272727273`**, and `CH-0230`'s own denominator **24 of 63 = `0.380952381`** —
because this task edits neither deliverable and none of the eight removed occurrences is in one.
That is the check, not a coincidence.

## 5. `F4` fired twice: two readings the rules got wrong, and neither was regenerated over

**(a) `C-0175`'s Constrains row.** The rules read it `MOVED` — *asserts a premise `C-0140`/`C-0141` withdrew*.
The sentence upholds `C-0147`'s verdict at the **relief** of `C-0151`'s selected raster;
it asserts no uniform tile dimension at all.
The governing-noun refinement put it on the debt family because no row noun and no `raster` stands within its window.
**Repaired by a hand override to `OUT_OF_SCOPE`**, keyed on the occurrence's own neighbourhood and dropped loudly if that text is rewritten.

The alternative cure was **measured and refused**, which is `C-0176`'s own standard:
adding the phrase to the restored-reading test reclassifies **exactly one** occurrence in the whole corpus — this one —
which is a hand override wearing a predicate's clothes.
And the class is not free either: the **family** cannot be overridden, only the class,
so this cannot read `RESTATED` — that class lives on the restored family and this occurrence is on the gated one.

**It was found by the gate itself**, as its only remaining `UNPOINTED` report after the first regeneration.

**(b) `C-0182`'s account of the gate.** The rules read it `MOVED` too.
`C-0182`'s subject **is** this census, and the token is quoted as **data** in a sentence reporting an unclassified occurrence —
which is `C-0144`'s own #20 exactly, *a document about the tool, not a design premise*.
**Repaired by registering the file in the emitter's `CORRECTING` set**, on the ground `C-0176` is registered on, and stated in the comment beside it.

**It was not found by the gate**, and could not have been:
the claim carries both a forward pointer and a headline pointer, so a wrong `MOVED` there is silent.
It was found by reading every entry the regeneration added — which is what `F4` asks for,
and the reason `F4` is a predicate rather than a sentiment.

This is `CH-0182` for the **eighth** consecutive iteration, and it is the version of it that runs *toward* the tool:
a claim whose subject is a census is inside that census's scope, and writing it moves the number it reports.
**This claim is written the other way**: it does not spell the family token in prose at all,
exactly as the `T-285` queue row does not, so it contributes **zero** occurrences and the census's reading of the corpus is unmoved by the document that reports it.

## 6. The mutation table, and a defect in the harness that measures it

**55 mutations, 0 survivors** over **184** named tests.
Thirteen of the 55 mutate this task's own rule — **7 narrowing, 6 widening** —
and every one of the **18** named tests `T-285` adds is killed by at least one mutation.

| direction | mutation | what it takes away or too far |
|---|---|---|
| NARROW | the filename rule removed | the pre-`T-285` predicate — the defect back |
| NARROW | the filename rule runs **after** the bare identifier | it eats its own prefix and leaves the slug |
| NARROW | the extension is `.md` alone | a result filename's token survives |
| NARROW | the slug charset drops the hyphen | a multi-word slug is not a filename |
| NARROW | the sub-letter is dropped | a `T-1d` file is not a filename |
| NARROW | the blanking stops preserving length | every offset below a span is wrong |
| NARROW | the **bare** identifier rule removed | `T-132` is a station census again |
| WIDEN | the extension is not required | every hyphenated identifier phrase is blanked |
| WIDEN | the slug admits whitespace | it reaches a later full stop in the same sentence |
| WIDEN | the slug admits whitespace **and** the full stop | one filename reaches the next decimal point behind it |
| WIDEN | the identifier prefix is not required | any dotted token is a filename, a decimal number included |
| WIDEN | the rule decays into a catch-all | `C-0150`'s judgement-becomes-a-pattern |
| WIDEN | the blanking is applied to the line **context** as well | §7's scope decision, reversed |

Every mutation **replaces** a rule wholesale.
`C-0177` measured the alternation trap at 9 of 22 rows of `C-0176`'s first table and `C-0179`'s at 2 of 6;
none is present here, and the count of mutations failing nothing is **0**.

**And the harness was wrong about which of its own tests are load-bearing.**
`tools/T-234-mutation-test.py` recovered a named test with a regular expression that captures only the **first** string literal of the name,
while the self-test reports the whole, concatenated name.
The two never compare equal.
Measured: the extractor missed **two** names outright (182 against 184 over an unchanged test set),
and this task's first reach measurement listed **seven** of its tests as unreached, of which **three** had demonstrably been killed by a mutation in the same run.
The four genuine gaps are what the last two mutations of the table above were written for.
It is parsed with `ast` now — which is what the interpreter does at the call — and the extractor carries its own self-check.
[`CH-0238`](../challenges/CH-0238-a-tools-own-instrument-was-wrong-about-which-of-its-tests-are-load-bearing.md).

## 7. What this task deliberately did not repair, and why it is a row rather than a footnote

The **line context** test reads the **original** line, so a filename can still supply its family's context:
a line carrying no context word of its own can be admitted by one inside a neighbouring link target.

That is real, it is measurable, and it runs the **opposite** way —
it **admits** matches where this task **removes** them —
so repairing it in the same pass would move classifications this task cannot audit against the before-list it published.
It is **`T-287`**, and the behaviour is pinned by a named test in the meantime, so a silent change to it fails something.

`CLAUDE.md`'s standing rule for this shape is the one applied:
wire what can be made clean, print what cannot, and never let one pass carry two deltas that cannot be told apart.

## 8. What would falsify this

- **A span the rule blanks that is not a filename.** One would make the rule a silencer rather than a blanking, and `CLAUDE.md` records that a drift checker's false positives cost more than its true ones. Measured: **0 of 4 102**.
- **A removed occurrence whose token was doing the work of a statement.** Measured one at a time: **0 of 8**.
- **A surviving occurrence that changed family, token or neighbourhood.** The whole effect is meant to be a removal and the index shift behind it. Measured: **0**.
- **A hand override silently re-pointed.** The mechanism exists to prevent exactly that. Measured: **7 carried, 0 dropped, 0 key collisions**.
- **An entry the regeneration added whose class no stated rule reaches, and which was accepted anyway.** Two were found and neither was accepted (§5).
- **A mutation of this rule that fails no named test.** Measured: **0 of 13**, and 0 of 55 over the whole table.
