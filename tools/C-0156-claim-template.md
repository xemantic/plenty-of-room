# C-0156 — the census `C-0153` could only publish as an **audit** is now a **gate**: **{{census/beforeAtHead/tokens}} tokens in {{census/beforeAtHead/files}} of {{census/beforeAtHead/scanned}} committed result files**, swept in one topological sort of {{reemission/files}} studies, reading **{{census/afterInTheWorkingTree/tokens}} tokens in {{census/afterInTheWorkingTree/files}} files** — and the guard the claim that raised it spent a whole section repairing was wrong in the **other** direction too, worth **{{census/widenedGuardExtraTokens}} further tokens** that nothing could see

| | |
|---|---|
| **Task** | [`T-250`](../tasks/T-250-prose-interpolation-sweep.md), raised by [`C-0153`](C-0153-unrounded-prose-interpolations.md) (`T-249`) |
| **Leaf** | none — a **process** claim protecting the machine-readable artifact of every leaf |
| **Verification type** | **logical** (a shape census over the committed corpus at a named ref, an offline movement classification against `git`, a staleness identity measured token by token, mutation coverage of the predicate **and** of the promotion) **+ in-silico** ({{reemission/files}} studies re-emitted through one snapshot in one `tools/reemission-order.py` topological sort) |
| **Verdict** | **PASS on all seven predicates.** `F1`–`F6` are reported in §7 with what each did |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every number this task moved is a rendering precision |
| **Provenance** | `gpd/results/T-250-prose-interpolation-sweep.json`, emitted by [`tools/T-250-emit-result.py`](../../tools/T-250-emit-result.py) from [`tools/T-250-body.json`](../../tools/T-250-body.json); the movement classification and the staleness identity in [`tools/T-250-movement.py`](../../tools/T-250-movement.py) (18 self-tests); the gate and its named tests in [`tools/check-result-file-hygiene.py`](../../tools/check-result-file-hygiene.py) (`--prose`, {{gate/selfTests}} self-tests); the promotion's mutation measurement in [`tools/T-250-mutation-test.py`](../../tools/T-250-mutation-test.py) and the predicate's in [`tools/T-249-mutation-test.py`](../../tools/T-249-mutation-test.py) |
| **Conditions** | The tree at `{{census/baselineRef}}` plus this iteration's edits. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0153`](C-0153-unrounded-prose-interpolations.md) (the census, the shape, `roundedForProse`), [`C-0138`](C-0138-departure-rule-scope.md) (*the cure is a property of a call site*), [`C-0129`](C-0129-result-file-hygiene.md) (the tool and its audit/gate policy), [`C-0127`](C-0127-format-string-repair.md) (mutation-test a predicate; the digits-stripped prose classifier), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*), [`C-0117`](C-0117-reemission-order.md) (*a sweep is a topological sort*), [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (re-emit and amend), [`C-0110`](C-0110-device-b-tall-gap.md) (run the consumers even when the change is provably invisible), [`C-0092`](C-0092-large-rotation-arm-branch.md) (a repair must leave the defect measurable) |
| **Raises** | [`CH-0204`](../challenges/CH-0204-the-guard-was-wrong-in-the-other-direction-too.md) — the census's **false-negative** rate was never measured, and the trailing guard was wrong in the other direction — and [`CH-0205`](../challenges/CH-0205-a-number-typed-as-a-string-is-untyped-as-well-as-unrounded.md) — a number typed as a string is **untyped** as well as unrounded, **{{residue/bareNumberStringLeaves/leaves}}** such leaves in **{{residue/bareNumberStringLeaves/files}}** of {{residue/bareNumberStringLeaves/scanned}} files |

---

## The claim, in one line

`C-0153` proved the shape and could not gate it, because the corpus was not clean and
**a gate that cannot come clean is not a gate**.
This task makes the corpus clean — {{reemission/files}} studies, one topological sort,
**{{movementByKind/totals/prose}} prose fields moved and {{movementByKind/totals/numeric}} numeric,
{{movementByKind/totals/boolean}} boolean, {{movementByKind/totals/wording}} wording,
{{movementByKind/totals/added}} added, {{movementByKind/totals/removed}} removed** — and turns the
line into a build-failing gate.

---

## 1. The census, before anything was repaired

Taken over the corpus at `{{census/baselineRef}}`, which the result file **records**, because a
census is a function of a mutable object and cannot otherwise be re-run (§6).

| | tokens | string fields | pointer sites | files |
|---|---|---|---|---|
| at the baseline, under this task's predicate | **{{census/beforeAtHead/tokens}}** | {{census/beforeAtHead/stringFields}} | {{census/beforeAtHead/pointerSites}} | **{{census/beforeAtHead/files}}** of {{census/beforeAtHead/scanned}} |
| at the baseline, under `C-0153`'s own predicate | {{census/beforeAtHeadUnderTheT249Guard/tokens}} | {{census/beforeAtHeadUnderTheT249Guard/stringFields}} | {{census/beforeAtHeadUnderTheT249Guard/pointerSites}} | {{census/beforeAtHeadUnderTheT249Guard/files}} |
| after the sweep | **{{census/afterInTheWorkingTree/tokens}}** | {{census/afterInTheWorkingTree/stringFields}} | {{census/afterInTheWorkingTree/pointerSites}} | **{{census/afterInTheWorkingTree/files}}** |

### The cheap bound resized the work by a factor of four, before a single study ran

`C-0153` priced the residue at 741 tokens and warned that the pointer count is a **lower** bound on
the source-side call sites. Both halves are worth restating with what was measured.

**The token count is not the work.** Collapsing array indices out of the JSON pointers gives
{{census/beforeAtHead/pointerSites}} distinct sites for {{census/beforeAtHead/tokens}} tokens, and
the distribution is extremely skewed: `T-21`'s 351 tokens are **340** copies of one
`bindingCeilingName` expression plus 11 `runParameters` entries, and `T-192`'s 49 are **42** copies
of one `verdict` sentence plus 7. Classified before any edit, the
{{residue/siteClassesAtTheBaseline/sites}} sites split
**{{residue/siteClassesAtTheBaseline/bareNumberStrings}} bare-number strings** (a
`Map<String, String>` entry whose whole value is a number) against
**{{residue/siteClassesAtTheBaseline/sentences}} sentences** — and only the second half needs
judgement.

**The source-side count is larger than the pointer count and smaller than the token count.**
**86** of the bare-number sites were repaired by one mechanical rule
(`x.toString()` → `x.roundedForProse().toString()`, keyed on exactly the census's own defect
keys); every sentence, and the bare sites the rule could not match, were read.

---

## 2. The repair, and the three classes it turned out to have

| class | what it is | rounding | where |
|---|---|---|---|
| **1 — a bare number rendered as a string** | `"kuhnSegmentVolume" to peg.kuhnSegmentVolume.toString()` | `roundedForProse()`, nine digits, the locked-units floor | {{residue/siteClassesAtTheBaseline/bareNumberStrings}} sites, 86 of them mechanical |
| **2 — a number inside a sentence** | `"… phi = $phi)"`, `"REFUSED — the path folds at $x nm"` | the same | {{residue/siteClassesAtTheBaseline/sentences}} sites, by hand |
| **3 — a departure inside a sentence** | `"the two groupings agree to 4.440892098500626E-16 nm"` | `DEPARTURE_SIGNIFICANT_DIGITS = 2`, `floor = 0.0` | 3 of those, and they decide the class |

Class 3 is why this cannot be a mechanism: `RESULT_ABSOLUTE_FLOOR` is a claim in the **locked
units** (`P-18`), so the default floor renders a dimensionless residual as `0.0` and deletes the
sentence's whole content. It has to be read.

### Two sub-classes the residue did not predict, and both are findings

**A fixed-decimal format cannot express a significant-digit rule.** Two studies carry
`private fun nine(value: Double) = "%.9f".format(value)`, and `"%.9f"` is nine **decimal places**:
for any value above one it emits ten or more **significant** digits while looking exactly like
compliance — `33.333333333`, `-6.707070569`, `8.164390827`. The gate sees it only where the value
happens to exceed one, which is why three of `T-188`'s tokens were caught and the rest of the same
helper's output was not.

**A `require` message that a caller is expected to CATCH and RECORD is a result, not a
diagnostic.** `C-0153` §5 deliberately exempts `require`/`check`/`println` messages, on the correct
ground that full precision is what a failed reproduction needs. `anchoring/TwoSpringElastica.kt`
has two `require` messages that exist **in order to be caught** — `C-0092`'s branch-refusal
taxonomy — and `synthesis/DesiredStrokeReachStudy.kt` stores `failure.message` straight into
`catalogue[*].note`. So the exemption is right at the **throw** site and wrong at the **catch**
site, and the number is already inside a string by the time the catcher sees it. The two messages
are rounded, with a comment saying why. **The blast radius is one file and it is a count, not a
guess:** 12 studies wrap an elastica call in `runCatching`, exactly **one** stores `.message` into
a result field, and `.message` appears in only four `main` sources in the tree.

---

## 3. The sweep: one topological sort, {{reemission/files}} studies, one snapshot

`CH-0131`'s rule is that a re-emission sweep is a **topological sort of the reader census**, not a
list, and `C-0153` found `tools/reemission-order.py` silently reporting **0** constraints when
handed a path. Handed basenames it reports **{{reemission/constraintsFromTheCensus}} dependency
constraints inside the set**, which is asserted non-zero before the order is used (`F4`).

`tools/study-batch.sh` is the vehicle: one snapshot, one cold Gradle build,
{{reemission/files}} runs, with the copy-back **re-baselined immediately before each run** so that
each study writes back exactly what it changed.

**One constraint the census cannot see was added by hand.** `tile/ForcedCrossoverPriceStudy.kt`
(`T-246`) is a sibling's study from the previous iteration and is not yet in
`P-22-result-reader-census.json`; it reads `T-139` at run time, so `T-139` must precede it. The
order satisfies it already, and the point is that a census one iteration stale is a **silent**
omission from a sort — the same failure mode `C-0153` found in the tool's argument handling.

---

## 4. What moved, by kind

Against each file's own committed version at `{{census/baselineRef}}`, read out of `git`,
flattened to JSON pointers and classified leaf by leaf.

| kind | fields |
|---|---|
| **prose** — a string whose digits moved and whose non-numeric skeleton did not | **{{movementByKind/totals/prose}}** |
| **wording** — a string whose skeleton moved, i.e. a verdict change | **{{movementByKind/totals/wording}}** |
| **departure** — a numeric leaf under a departure spelling | **{{movementByKind/totals/departure}}** |
| **numeric** — any other numeric leaf | **{{movementByKind/totals/numeric}}** |
| **boolean** | **{{movementByKind/totals/boolean}}** |
| **added / removed** | **{{movementByKind/totals/added}} / {{movementByKind/totals/removed}}** |

### Nothing is stale, as an identity

Every moved prose **token** is checked against the rounding its own call site declares, on the
**value** and not on the text — Kotlin's `Double.toString` and Python's `repr` disagree about
exponent spelling (`8.755985E-4` against `0.0008755985`), and that is a rendering.

**{{movementByKind/totals/tokensExplained}} of {{movementByKind/totals/movedTokens}} moved tokens
are exactly `roundForResult(old value, d)` for one of the two declared precisions;
{{movementByKind/totals/tokensUnexplained}} are unexplained.**

---

## 5. The guard was wrong in the other direction too, and the pairing tool is what found it

`C-0153` §3 records that the predicate's first draft carried the symmetric trailing guard
`(?![\w.])`, that this refused every number at the **end of a sentence**, and that
*"a checker's blind spot is invisible in exactly the cases it misses"*. It repaired the guard to
`(?!\w)(?!\.\d)` and then measured the **false-positive** rate exhaustively, resting the census's
credibility on that number.

**A false-positive rate is not a completeness argument.** `(?!\w)` refuses a decimal abutting a
**unit letter**, and this corpus writes a ratio as `26.381529916714886x`. Measured over the same
baseline: **{{census/widenedGuardExtraTokens}} further tokens**, in
{{residue/widenedGuardExtraFiles}} files, **every one a genuine over-precise ratio and not one a
false positive** — `1.70878479537323x A2's 78.235`, `9.762128175180635x the margin`,
`15.110341201220582x CHEAPER`.

The property `(?!\w)` was there for — that the captured literal is the whole
`2.314028420585025E-7` and not its mantissa — survives on the pattern's own greediness plus
`(?!\d)`, and is now held open by a named token test rather than by a guard that also refuses a
unit.

**What found it was `tools/T-250-movement.py`.** It has to **pair** the tokens of two renderings of
one sentence, and a pairing tool cannot use a detector's guard: a detector may prefer a false
negative and a pairing tool may not. On the first re-emitted file it reported four sentences whose
digits had visibly moved and in which it could find no moved token. **A checker's blind spot is
found by the tool that must agree with it, never by the tool that produces it** — and the widening
cost **no extra files at all**, because all {{residue/widenedGuardExtraFiles}} were already in the
sweep, which is luck rather than method (`CH-0204`).

---

## 6. A result file whose subject is the CORPUS must name the corpus state it measured

Every other result file in this repository is a function of code plus committed inputs.
A **census** is a function of the whole mutable corpus, and `gpd/README.md`'s re-run rule is
simply false for it unless the corpus state is named.

`tools/T-249-emit-result.py` hardwires `HEAD`. The moment this task repaired the corpus it stopped
being able to reproduce its own committed file:

```
$ tools/T-249-emit-result.py --check
17 of 17 emitter self-tests pass
BODY IS STALE — '1 — the artifact-side census, run before any repair' says 757.0, derived 778.0
```

That is **correct arithmetic about a different object**, and it is not a defect in `C-0153`'s
numbers — 757 was true of the corpus it measured. It is a defect in the emitter's ability to say
*which* corpus. `tools/T-250-emit-result.py` therefore takes `--baseline <ref>`, defaults it to
`HEAD`, and **records the resolved SHA** (`census.baselineRef = {{census/baselineRef}}`);
`tools/T-250-movement.py` takes the same ref, because the moment this sweep is committed `HEAD` is
the post-repair corpus and every movement it reports would collapse to zero. Four lines, and
without them this claim's own file could never be re-run either.

---

## 7. Falsifiers

| | statement | fired | outcome |
|---|---|---|---|
| **F1** | a re-emission moves a numeric field, a boolean or a verdict | **{{falsifiers/0/fired}}** | {{falsifiers/0/outcome}} |
| **F2** | a moved prose token is not the rounding its call site declares | **{{falsifiers/1/fired}}** | {{falsifiers/1/outcome}} |
| **F3** | a repaired file still carries a token | **{{falsifiers/2/fired}}** | {{falsifiers/2/outcome}} |
| **F4** | `reemission-order.py` reports zero dependency constraints | **{{falsifiers/3/fired}}** | {{falsifiers/3/outcome}} |
| **F5** | a mutation of the promoted gate passes every named test | **{{falsifiers/4/fired}}** | {{falsifiers/4/outcome}} |
| **F6** | the widened trailing guard costs a file the sweep did not already own | **{{falsifiers/5/fired}}** | {{falsifiers/5/outcome}} |

---

## 8. The promotion, and how it is mutation-tested

`--prose` moves from the audit list to the gate list in `tools/verify.sh`, beside `--conversions`
and `--departures`, and `main` returns 1 on any defect. Two objects had to be added, and **neither
is reachable by the predicate's own tests**:

* an **exit policy**, written as a function `prose_exit_code(found, census)` precisely so that it
  can be mutated and named-tested — "promoted from an audit to a gate" is exactly one `return`;
* a **token-level allowlist**, `{(file, literal)}` rather than the per-**file** kind
  `--conversions` uses. `C-0153` §6's self-contradiction table quotes the two precisions side by
  side, which **is** the finding (`C-0092`), and a per-file entry would exempt that whole result
  file for the sake of one sentence. There is **{{gate/allowlistEntries}}** entry.

| | mutations | named tests | passing every test | rows reached |
|---|---|---|---|---|
| the **predicate** (`T-249`'s table, plus this task's rows) | {{mutationCoverage/predicate/mutations}} | {{mutationCoverage/predicate/namedTests}} | **{{mutationCoverage/predicate/mutationsPassingEveryTest}}** | {{mutationCoverage/predicate/rowsReachedBySomeMutation}} |
| the **promotion** (the exit policy and the allowlist) | {{mutationCoverage/promotion/mutations}} | {{mutationCoverage/promotion/namedTests}} | **{{mutationCoverage/promotion/mutationsPassingEveryTest}}** | {{mutationCoverage/promotion/rowsReachedBySomeMutation}} |

Half the promotion's mutations **widen** the gate and half **narrow** it, per `C-0150`'s standard:
restoring `T-249`'s audit-only `return 0` fails *"a prose defect FAILS the build"*; making the gate
fire under `--census` fails *"--census reports and never gates"*; a **per-file** allowlist fails
*"a DIFFERENT over-precise token in the allowlisted file is still a defect"*, which is the row that
exists because a per-file allowlist passes every test written about the file it exempts. Restoring
`T-249`'s own `(?!\w)` trailing guard fails **{{residue/t249GuardNamedTestsFailed}}** named tests of
the predicate table.

---

## 9. The residue, published with its own cost

| residue | size | why it stays | cost to close |
|---|---|---|---|
| the census's **false-negative** rate ([`CH-0204`](../challenges/CH-0204-the-guard-was-wrong-in-the-other-direction-too.md)) | unmeasured; the guard cost {{census/widenedGuardExtraTokens}} tokens and the short-`toString` class is unbounded | a source-side census needs Kotlin's own types, which a regular expression does not have | a lint rule or a compiler plugin — the only instrument that can turn a lower bound into a count |
| a number typed as a **string** ([`CH-0205`](../challenges/CH-0205-a-number-typed-as-a-string-is-untyped-as-well-as-unrounded.md)) | **{{residue/bareNumberStringLeaves/leaves}}** bare-number string leaves in **{{residue/bareNumberStringLeaves/files}}** of {{residue/bareNumberStringLeaves/scanned}} files; the gate reaches {{census/beforeAtHead/tokens}} of them | the repair is a **schema partition**, and it moves a **numeric** field in {{residue/bareNumberStringLeaves/files}} files — the one thing this claim forbids | one pass to partition bare from annotated, then a sweep larger than this one |
| the **floored departure** ([`CH-0198`](../challenges/CH-0198-the-floor-half-of-the-rule-never-travelled.md)) | 32 of 49 call sites | unchanged by this task; it moves a numeric field | `T-251` |
| the **orphaned quotations** ([`CH-0199`](../challenges/CH-0199-a-quoted-number-has-no-link-back-to-its-file.md)) | 19 of 43 | needs a corpus convention before a checker | `T-252` |
| `T-249`'s own emitter | cannot reproduce its committed file | its `HEAD` is hardwired; the file itself is correct about the corpus it measured | one `--baseline <ref>` argument, the way `T-250`'s emitter now takes one |

---

## 10. What this is an instance of

`C-0153` closed with *a rule can be stated correctly, recorded in `CLAUDE.md`, and still have no
enforcement point at all*. This is the step after: **an enforcement point that cannot come clean is
not an enforcement point either**, and the distance between an audit and a gate is not the
predicate — it is the corpus.

And the thing the sweep found on the way is the sharper half. A checker's credibility was
established by an exhaustive **false-positive** measurement, which is the measurement that can be
made cheaply; its **false-negative** rate was never measured, could not have been found by reading
it, and was found instead by the first tool obliged to agree with it.
