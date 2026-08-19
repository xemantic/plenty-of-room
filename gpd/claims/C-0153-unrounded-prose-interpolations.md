# C-0153 — a number emitted as a **string** is not rounded, and this is the one member of that family the **serialisation boundary cannot cure**: it sees a sentence, not a number. The shape reaches **757 tokens in 706 string fields at 175 emission sites in 47 of 141 committed result files** where the claim that raised it saw **three**, and every one of the 757 is an interpolated `Double` by an **exhaustive** test rather than a sampled one

| | |
|---|---|
| **Task** | [`T-249`](../tasks/T-249-unrounded-prose-interpolations.md), raised by [`C-0150`](C-0150-departure-spelling-set-and-the-wall-clock.md) (`T-225`) |
| **Leaf** | none — a **process** claim protecting the machine-readable artifact of every leaf |
| **Verification type** | **logical** (a shape census over the committed corpus, an exhaustive shortest-round-trip test of every hit, an offline prediction of the repair, a mutation measurement of the predicate in **both** directions over 10 mutations and 29 named tests, 120 checker + 15 emitter self-tests) **+ in-silico** (one study re-run through one snapshot and diffed field by field against its **committed** version read out of `git`) |
| **Verdict** | **PASS on all seven predicates.** `F1`–`F7` are reported in §7 with what each did |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every number this task moved is a rendering precision |
| **Provenance** | `gpd/results/T-249-unrounded-prose-interpolations.json`, emitted by [`tools/T-249-emit-result.py`](../../tools/T-249-emit-result.py) (17 self-tests) from [`tools/T-249-body.json`](../../tools/T-249-body.json); the predicate and its 29 named tests in [`tools/check-result-file-hygiene.py`](../../tools/check-result-file-hygiene.py) (`--prose`, **120** self-tests); the mutation measurement in [`tools/T-249-mutation-test.py`](../../tools/T-249-mutation-test.py); the mechanism in `src/main/kotlin/structure/ResultRounding.kt` (`Double.roundedForProse`) with the eleven call sites in `src/main/kotlin/anchoring/RowEndCrossoverStiffnessStudy.kt`; four gate-named tests in `src/test/kotlin/structure/ResultRoundingTest.kt`. The emitter's own body carries five cheap-bound values by hand and **asserts every one against what it derived**, refusing to write a stale file — mutation-tested by moving 757 to 756, which exits 1. Two independent runs of the emitter are **byte-identical**. `tools/verify.sh` run twice on its own isolated tree — **`BUILD SUCCESSFUL in 21m 53s` and again in 22m 11s** after the final edits — with all eight post-Gradle gates clean: 47 census checks, 0 table defects in 498 files, 0 broken links in 491, 0 `String.format` defects, 172 of 172 challenges indexed, 0 raw conversions, and the departure gate at `0 field(s) in 0 file(s)` on all three lines. `result-reader-census.py --emit` was **not** run (two concurrent agents' new studies are outstanding in it, and the shared file is theirs) |
| **Conditions** | The tree at `HEAD` of iteration 36 plus this iteration's edits. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0150`](C-0150-departure-spelling-set-and-the-wall-clock.md) (the three sentences and the shape-not-a-list method), [`C-0138`](C-0138-departure-rule-scope.md) (the rule in the layer, and the *cure is a property of a call site*), [`C-0129`](C-0129-result-file-hygiene.md) (the tool and its audit/gate policy), [`C-0127`](C-0127-format-string-repair.md) (mutation-test a predicate; the digits-stripped prose classifier), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate), [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (re-emit and amend the claim), [`C-0117`](C-0117-reemission-order.md) (a sweep is a topological sort), [`C-0110`](C-0110-device-b-tall-gap.md) (run the consumers even when the change is provably invisible), [`C-0092`](C-0092-large-rotation-arm-branch.md) (a repair must leave the defect measurable), [`C-0031`](C-0031-bracketed-root-repair.md)/`P-18` (an absolute floor is a claim about units) |
| **Raises** | [`CH-0198`](../challenges/CH-0198-the-floor-half-of-the-rule-never-travelled.md) — the departure rule's **digit** half is a baseline in the layer and its **floor** half is still a default, at **32 of 49** call sites — and [`CH-0199`](../challenges/CH-0199-a-quoted-number-has-no-link-back-to-its-file.md) — a number quoted in a claim has no mechanical link back to the result file it came from, and **19 of 43** detectable ones are already orphaned |
| **Amends** | [`C-0099`](C-0099-row-end-crossover-stiffness.md), which quotes one number this re-emission moved |

---

## The claim, in one line

**Every earlier member of this family was cured at the serialisation boundary, and this one cannot be** —
the boundary dispatches on the JSON *type*, and by the time it sees `"channel B at s = 0 is $x"` the
number is gone. So the cure is necessarily per **call site**, the only thing that can keep it closed
is a **census**, and the census is therefore the deliverable rather than the three fields.

---

## 1. The cheap bounds, and the first of them resized the task by 250×

| bound | cost | measured | what it decided |
|---|---|---|---|
| **1** — the artifact-side census over the committed corpus | seconds | **757** tokens in **706** string fields at **175** emission sites in **47** of **141** files | the class is 250× the instance that raised it, and it is 47 re-emissions rather than one |
| **2** — the shortest-round-trip test over every hit | seconds | **0 of 757** are not the shortest decimal that round-trips their own double | the false-positive rate is **exhaustive, not sampled** |
| **3** — the offline prediction over the one file being repaired | seconds | **16** tokens in **10** string fields, with their new values | what the seventeen-minute solve must move, before it is spent |
| **4** — the reader census against the target | seconds | **0** readers of `T-164` | one file, no topological sort |
| **5** — the runbook cost of the residue | seconds | **47** files, **39** dependency constraints, **8** with a stated runtime summing to **161 min** | the sweep is a multi-iteration topological sort, so the census ships as an **audit** and the sweep is queued with its own cost |

**A caveat on the site count, stated because it runs the costly way.** The 175 sites are JSON
pointers with array indices collapsed, so one pointer can be several source expressions: `T-164`'s
16 tokens sit at **3** such pointers (`/decision`, `/falsifiers/*/outcome`, `/findings/*`) and were
repaired at **11** distinct call sites in the Kotlin. So 175 is a **lower** bound on the source-side
work by roughly the same factor, and `T-250` should be priced on the call sites, not on the
pointers.

### Why the census is run over the ARTIFACT and not over the source

The obvious census is a static one: find a `${…}` interpolation of a `Double` inside a string literal
in `src/main/kotlin`. That needs a type Python does not have, and `C-0127`'s checker had to learn
four false-positive classes before it was believable.

The artifact-side predicate is **exact** and needs no parsing at all. A number that went through
`roundedForResult` cannot carry more than `RESULT_SIGNIFICANT_DIGITS = 9` significant digits, by
construction — so **every** decimal token above nine inside a *string* value is a number that did
not go through it. The predicate is one regular expression and a digit count, it runs over the
committed corpus in seconds, and it bounds the blast radius before a single study is re-run.

It is a strict **lower** bound and the direction is stated: an unrounded number whose
`Double.toString()` happens to be short — `33.5`, `0.125` — is indistinguishable from a rounded one
and is not counted. The source-side census is the complement and it is not run here.

---

## 2. The false-positive rate is a PROOF over the population, not a sample

`CLAUDE.md`'s standing rule is that a drift checker's false positives cost more than its true ones,
because the tool exists in order to be believed, and that an **unmeasured** false-positive rate is
what makes a checker stop being believed. The usual measurement is to read a stratified sample.

There is an exact instrument here, and it is one line.
**`Double.toString()` emits the shortest decimal that round-trips**, and so does Python's `repr`.
A token carrying *more* digits than the shortest form of its own value is therefore **not** a
`toString` output — and a number transcribed from a paper, a version string, a date or an identifier
has no reason whatever to satisfy the shortest-round-trip property.

Applied to all **757** hits: **0** fail it. Every hit in the corpus is an interpolated `Double`.
That is a statement about the whole population and it cost one pass, where a sample would have
supported an estimate.

The predicate's own guards are held open by 29 named tests, and the classes they refuse are the ones
a corpus actually contains: a decimal inside an identifier (`v3.14159265358979`), the two halves of a
dotted date (`2026.08.19`), a DOI prefix, a result-file path, a bare integer of any length (exact,
and its rendering deterministic), and a number already at or below nine digits.

---

## 3. The checker's own first draft was wrong, in the direction the corpus is made of

Written with the **symmetric** trailing guard `(?![\w.])` — the obvious mirror of the leading
`(?<![\w.])`, and the form that correctly refuses `2026.08.19` — the predicate refuses a number
followed by a **full stop**, i.e. **every number at the end of a sentence**. In a corpus whose
defects live in `findings` and `outcome` strings that is not a corner case; it is the shape of the
data.

Written as `(?!\w)(?!\.\d)` instead, it finds them. The measured cost in *this* corpus is **one
token**, which is honest and small — and the point is not the size, it is that a checker's blind
spot is invisible in exactly the cases it misses, so the size was unknowable until the guard was
changed. `CLAUDE.md` already records this for `tools/check-corpus-links.py` (30 broken links) and
for `tools/check-kotlin-format-strings.py` (a nested-template strip that needed a balanced-brace
walk). This is the third instance, and the first where the wrong guard was the *symmetric* one.

The retained named test is `a defect at the END of a sentence — the first draft's blind spot`, and
restoring the first draft fails it and one other.

---

## 4. Why the serialisation boundary cannot be the cure, and what follows from that

`C-0138`'s central move was to stop repairing the departure rule per file and put it in
`roundedForResult`, *"one line in the layer every study already goes through"*. That worked because
the boundary sees a **number**: a JSON numeric leaf, with a key, in a record.

Here it sees a **sentence**. `roundedForResult` dispatches on the JSON type and passes strings
through — correctly, because a string is not a number — and by the time it is called the `Double`
that produced `"channel B at s = 0 is 0.1686405908358075"` no longer exists. The only way the layer
could reach it is to re-parse decimals **out of a study's own prose** and rewrite them, which would
rewrite a cited literature value (`"13.529411764705882 pN·nm/rad (Chen 2014, FITTED)"`) as readily
as a computed one, and would silently edit sentences whose whole point is a digit.

So the cure is **necessarily** per call site. What this task ships is:

- `fun Double.roundedForProse(digits, floor)` in `structure/ResultRounding.kt` — not a mechanism, a
  **name**, with the KDoc that says why the boundary is not available and what digits and floor a
  departure in prose takes;
- the **census**, which is the only thing that can keep the class closed, because nothing in the
  type system will.

That asymmetry is the finding. Five members of this family were closed by a mechanism; this one
cannot be, and a task that assumed otherwise would have spent its iteration writing the wrong repair.

---

## 5. The repair, per call site

Eleven interpolations in `anchoring/RowEndCrossoverStiffnessStudy.kt`, each given the digits and the
floor its own quantity is entitled to — which is `C-0138`'s *"the cure is a property of a call
site"* in the only place it can be:

| site | quantity | digits | floor |
|---|---|---|---|
| `F2` outcome | a **departure** | `DEPARTURE_SIGNIFICANT_DIGITS` = 2 | `0.0` — `RESULT_ABSOLUTE_FLOOR` is a claim in the locked units (`P-18`) and would render the whole sentence as `0.0` |
| `F3`, `F4`, `F5`, `F6` outcomes; `findings` 0, 1, 3, 5, 6; `decision` | dishing fractions, a softening ratio, a placement penalty | `RESULT_SIGNIFICANT_DIGITS` = 9 | default |

Two kinds of interpolation are **deliberately left raw**, and both are stated exceptions rather
than omissions: the `check{}` failure message at the reproduction loop, which carries three numeric
interpolations and never reaches a file — full precision is exactly what a failed reproduction needs
— and the `println` lines, which go to the console. `require` messages are in the same class.

**Every other interpolation in the file is an `Int`** — `sweep.size`, `monotonicity.count { … }`,
`C0055_ARM_COUNT` — which the predicate correctly refuses and a named test says so.

---

## 6. What the re-emission moved

One study, one snapshot, one file, diffed field by field against its **committed** version read
out of `git`. `T-164` has **0 readers**, so there is no topological sort and no consumer to re-run.

| file | prose digits only | verdicts / wording | numeric | booleans | added | removed |
|---|---|---|---|---|---|---|
| `T-164` | **10** | 0 | **0** | 0 | 0 | 0 |

**Ten lines of an 823-line file, every one a string, every one a digit change inside a sentence.**
`diff` reports exactly `5c5`, `787c787`, `793c793`, `805c805`, `811c811`, `815,816c815,816`,
`818c818`, `820,821c820,821` — and nothing else, in a file whose emission re-enumerates 163 296
placements at each of 18 rungs through its own Woodbury bank.

**The offline prediction was exact.** Sixteen tokens in ten string fields were predicted, with their
new values, before the solve was launched; sixteen moved, in those ten fields. `F2` did not fire.

**Nothing is stale, as an identity.** Every over-precise token in the committed prose is replaced by
**exactly** the rounding its own call site declares: **16 of 16, 0 unexplained.** The comparison is
taken on the **value** and not on the text, because Kotlin's `Double.toString` and Python's `repr`
disagree about exponent spelling — `8.755985E-4` against `0.0008755985` — and that difference is a
rendering, not a number.

### The file stopped contradicting itself

| quantity | numeric field | the sentence, before | the sentence, after |
|---|---|---|---|
| best 34-root dishing at `s = 0` (channel A) | `sweep[0].bestDishingOverStroke` = `0.0651753854` | `0.06517538540278571` | `0.0651753854` |
| `C-0090`'s admitted optimum | `reproductions[0].published` = `0.0621469105` | `0.062146910466135304` | `0.0621469105` |
| `C-0090`'s refused reading | `reproductions[1].published` = `0.168371808` | `0.1683718082999668` | `0.168371808` |
| channel B at `s = 0` | `sweep[9].bestDishingOverStroke` = `0.168640591` | `0.1686405908358075` | `0.168640591` |
| worst published-placement penalty | `sweep[0].publishedPlacementPenalty` = `8.755985E-4` | `8.755984995810762E-4` | `8.755985E-4` |

Five of the ten fields now quote the file's own numeric field **exactly**; `C-0099`'s own falsifier
table had already rounded two of them by hand to `0.168640591` and `0.168371808`, which is what the
file now says.

### `P7`'s one named exception

`F2`'s sentence reads `reproduced at a departure of 3.4E-11`, and the numeric
`reproductions[0].departure` reads `0.0`. They do not disagree about the quantity: the record is
**floored** by `RESULT_ABSOLUTE_FLOOR`, a claim in the locked units, applied to a dimensionless
ratio. The prose is deliberately given `floor = 0.0`, so it carries information the record has lost.
That is [`CH-0198`](../challenges/CH-0198-the-floor-half-of-the-rule-never-travelled.md), it predates
this task, and repairing it would move a **numeric** field.

---

## 7. Falsifiers

| | statement | fired | outcome |
|---|---|---|---|
| **F1** | the re-emitted `T-164` moves a numeric field, a boolean or a verdict | **no** | 10 prose-digit fields, **0** numeric, **0** boolean, **0** verdict/wording, 0 added, 0 removed |
| **F2** | the offline prediction differs from what the re-emission moved | **no** | 16 tokens in 10 fields predicted; 16 in 10 moved, with the predicted values |
| **F3** | a prose number disagrees with the file's own numeric field for the same quantity | **no**, with one **named** exception | five of ten fields now quote the numeric field exactly; `F2`'s departure is floored in the record and not in the prose — `CH-0198` |
| **F4** | the census still finds a hit in the repaired file | **no** | `0` tokens remain in `T-164` |
| **F5** | a mutation of the predicate passes every named test | **no** | **10** mutations over **29** named tests; **0** pass every one, and **28 of 29** rows are reached by some mutation. The one unreached row is the empty string, a degenerate-input guard |
| **F6** | `T-164` has a reader, so this is a sweep rather than one file | **no** | 0 readers, by `tools/result-reader-census.py` and by a `grep` of the basename; the only other mention is `T-225`'s record of what it re-emitted |
| **F7** | the shipped predicate is the first draft, i.e. it was never wrong and therefore never tested | **no — the first draft WAS wrong** | the symmetric trailing guard missed every number at the end of a sentence; **1** token of the committed corpus, and restoring it fails **2** named tests |

### The mutation table, in both directions

`C-0127`'s standard is that restoring the narrow predicate must fail a **named** test; `C-0150`'s
addition is that a predicate which can only ever be **widened** has become a pattern, so the tests
must bite both ways. Five of the ten mutations here are widenings.

| mutation | direction | named tests failed |
|---|---|---|
| a naive digit-run pattern, no guards and no threshold | widening | **21** |
| threshold narrowed to seventeen digits | narrowing | **15** |
| no digit threshold at all | widening | **6** |
| character count instead of significant digits | widening | **3** |
| threshold widened to six digits | widening | **2** |
| the leading lookaround dropped | widening | **2** |
| the symmetric trailing guard of the FIRST DRAFT restored | narrowing | **2** |
| the trailing word guard dropped | widening | **1** |
| the dotted-token guard dropped | widening | **1** |
| bare integers matched as well | widening | **1** |

**Measured false-positive rate: 0 of 757**, exhaustively, by the shortest-round-trip test of §2 —
not a sample, and not an assertion.

---

## 8. The residue, published with its own cost

`CH-0168`'s rule is that a residue published without its own cost is priced against the nearest table.

| residue | size | why it stays | cost to close |
|---|---|---|---|
| the **46 other affected result files** | **741 tokens in 696 string fields at 172 JSON sites** | out of scope for a task whose claim is that it moved one file's prose and nothing else; and it is a **judgement per call site** (digits and floor), not a mechanism | **38 of 47 are in the runbook**, 8 of those carry a stated runtime summing to **161 min**, and the set carries **39 dependency constraints** — a topological sort over several iterations, queued as `T-250`. The largest single file is `T-21` at 351 tokens from 12 sites |
| a **sibling's brand-new result file**, written in this same iteration | **6 tokens** in `T-246-forced-scaffold-crossover-price.json` | it is another agent's file and another agent's task | it is also the strongest evidence in this claim that the class is **live** rather than historical: it reproduces in work written while the census was being taken |
| **this claim's own result file** | **1 token** — `0.06517538540278571`, quoted as the *before* half of §6's self-contradiction table | `C-0092`: a repair must leave the defect **measurable**, and quoting the two precisions side by side IS the finding | it is exactly the class [`CH-0199`](../challenges/CH-0199-a-quoted-number-has-no-link-back-to-its-file.md) says no instrument can distinguish from a stale quotation. When `T-250` closes and the line becomes a gate it needs either an allowlist entry — `C-0129`'s precedent, `T-207`'s own record, the conversions gate's only one — or a rewrite in words |
| the **audit not being a gate** | `748` tokens in `48` files as this claim is filed | `C-0083`: a gate that cannot come clean is not a gate | it becomes a gate the moment `T-250` closes, at which point the `--prose` line moves from the audit list to the gate list — one line |
| the **floored departure** ([`CH-0198`](../challenges/CH-0198-the-floor-half-of-the-rule-never-travelled.md)) | 32 of 49 call sites | it moves a **numeric** field, which this task's claim forbids | queued as `T-251` |
| the **orphaned quotations** ([`CH-0199`](../challenges/CH-0199-a-quoted-number-has-no-link-back-to-its-file.md)) | 19 of 43 | it needs a corpus **convention** (mark a historical number) before a checker can tell a deliberate quotation from a stale one | queued as `T-252` |

---

## 9. What the outward-facing documents owe

`ANSWERS.md` and `DECISIONS-FOR-NDI.md` were **not** edited here — no agent edits them this
iteration by agreement. Nothing is owed to either: neither quotes a `T-164` number, neither carries
an over-precise token, and this task moves no physical quantity, no verdict and no window edge.
The one document that owes something is `CLAUDE.md`, whose *"a number emitted as a STRING is not
rounded"* entry is rewritten with the measured census and the structural reason the boundary cannot
reach it, and which gains the `reemission-order.py` path trap and the wait-loop self-match.

`C-0099` is amended where it quotes the one number this re-emission moved, per `C-0101`.

**One thing IS owed to `DECISIONS-FOR-NDI.md`, and it is not this task's.** Running
`tools/trace-answers.py --answers DECISIONS-FOR-NDI.md` over the working tree at the end of this
iteration reports `line 965 STALE-OPEN CH-0187 CLOSED` — the document says *"`CH-0187` is open on
exactly that"* and a concurrent task's `C-0151` **answered** it hours earlier. `ANSWERS.md` is clean
on the same pass, and `DECISIONS-FOR-NDI.md` read `0 open assertion(s) contradicted` before that
closure landed. It is recorded as `T-253` rather than repaired here, because the file belongs to
another agent this iteration and because `C-0071`'s rule is that a discharge must not be invisible
to the queue. It is also the third consecutive iteration in which a **standing** checker caught a
drift within the hour it was created.

---

## 10. What this is an instance of

`C-0150` names the general form of its own family: *a rule stated as "every X the corpus uses" is
enforced against a list, and a list is checkable only against the search that produced it.*

This is the level above. **A rule can be stated correctly, recorded in `CLAUDE.md`, and still have
no enforcement point at all** — not because nobody wrote the checker, but because the mechanism the
other members of the family were cured by is *structurally unavailable* here. When that happens the
only remaining instrument is a census over the artifact, and the census's own credibility rests
entirely on its measured false-positive rate. Which is why the shortest-round-trip test, one line
long, is the most load-bearing thing in this claim.
