# C-0202 — **a scaffold LENGTH is not a scaffold PROVENANCE: the third census family is split, the two readings are about two different OBJECTS, and the rule has ZERO false positives at every one of twelve radii — while the radius it takes is not fitted but the SMALLEST sufficient one, which a monotonicity theorem makes the safest**

| | |
|---|---|
| **Task** | [`T-300`](../tasks/T-300-a-length-is-not-a-provenance.md), raised by [`C-0193`](C-0193-the-built-turn-is-a-tether.md) (`T-296`) after its own **seven** occurrences forced a hand override each — the row says *eight*; the census counts seven |
| **Leaf** | none — a **process** claim protecting the census that measures the two customer-facing documents' debt |
| **Verification type** | **logical** — a predicate over the corpus, measured against a hand reading of **all 70** occurrences recorded **before** the rule was written |
| **Verdict** | **PASS on all five predicates.** `F1` **0** false positives at 12 radii from 60 to 1200; `F2` the answer plateaus and the plateau is a theorem; `F3` hand overrides on the family **16 → 3**; `F4` the gate reads `GATE 0 defect(s)`; `F5` **71** mutations, **0** failing nothing and **0** killed by an exception |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Not one number any claim quotes moves. What moves is a classification and the advisory debt line the census prints |
| **Provenance** | `gpd/results/T-300-a-length-is-not-a-provenance.json`, emitted by [`tools/T-300-emit-result.py`](../../tools/T-300-emit-result.py) at `baselineRef` `b04a675`; the predicate and its 26 named tests in [`tools/T-234-census.py`](../../tools/T-234-census.py); the coercion and its 6 in [`tools/T-234-emit-classification.py`](../../tools/T-234-emit-classification.py); the 14 mutation rows in [`tools/T-234-mutation-test.py`](../../tools/T-234-mutation-test.py) |
| **Conditions** | The tree at `b04a675` plus this iteration's edits, recorded as `baselineRef` in the result file. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (the split discipline: sweep to a plateau, read every reclassification, measure a widening before writing it, replace a rule wholesale), [`C-0179`](C-0179-the-debt-line-as-a-ratio.md) (the two denominators and which one falls), [`C-0182`](C-0182-name-the-discharge.md) (a family must NAME its discharge), [`C-0184`](C-0184-a-slug-is-not-a-statement.md) (emit the intermediate reading rather than describing it), [`C-0196`](C-0196-a-name-cannot-govern-a-token.md) (the refinement window reads blanked text), [`C-0193`](C-0193-the-built-turn-is-a-tether.md) (the two readings, stated in its own prose) |
| **Raises** | [`CH-0252`](../challenges/CH-0252-two-rows-one-identifier.md) — two queue rows carried one identifier, so a closing verdict on either was invisible on the other. **RAISED and repaired in the same commit** |

---

## The claim, in one line

The third of the census's five families was a bare token with **no line context and no refinement**,
so every occurrence of it read as the withdrawn premise
and a reader had to hand-override **sixteen** of seventy;
split by **what governs the token** it is `50` provenance and `20` forward budget
at the baseline ref, with **zero** disagreements against a hand reading taken beforehand,
and the hand-override count falls to **three** — none of which is a family call.

---

## 1. The cheap bound, which ran before any predicate was touched

Two counts over `census(root)`, neither needing a rule:

| | |
|---|---|
| occurrences of the family at `b04a675` | **70** |
| of those, settled by a **hand override** | **16** |

If the second were small relative to the first, a *name* could govern the family
and `T-300`'s second deliverable — *the stated decision that a name may govern it* — would be the answer.
It is not: **16 of 70** — and **all sixteen were typed in a single iteration**, by three hands.
That is measured rather than recalled: the classification committed at the end of iteration 45
carries **zero** hand overrides on this family, of six in the whole table.
The queue row raising this task says *twelve*, counting the two passes it could see;
the census counts **sixteen**, and two of the four it does not name are the row's **own**,
typed by its author to stop the row asking for the split from being counted as a debt.
`CLAUDE.md` calls a hand override a **dated object**; sixteen of them are a rule nobody wrote down.

## 2. The two readings are about two different OBJECTS, which is why a name could not settle it

- **the debt** — *which scaffold a 2009 caDNAno block was folded from*.
  That is the premise [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) /
  [`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md) withdrew,
  settling design (i) at `60 × 126 = 7 560`.
- **not the debt** — *a scaffold **length** in a forward budget for a Gen-1 tile nobody has folded*,
  on [`C-0151`](C-0151-closing-raster-selection.md)'s drawable raster.
  That object **did not exist in 2009**, so no reading of it can assert the withdrawn premise, at all.

[`C-0193`](C-0193-the-built-turn-is-a-tether.md) states the split in its own prose —
*"a statement about a Gen-1 tile nobody has folded, and not about which scaffold any 2009 block was folded from"* —
and then hand-overrode its way past it seven times, because the tool had nowhere to put the distinction.
This is [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md)'s shape for the **third** time,
after the placement and width families:
**before widening a pattern, ask whether the thing it cannot see is a SECOND PREMISE,
in which case the pattern was never the right instrument.**

The new family is declared **no discharge at all** — a **token collision**, like the attributive one,
and not a **restatement**, like the restored row span.
It is not the withdrawn premise read correctly; it is a different object wearing the same string.
That is why its coercion is the collision's class and not the restatement's, and a named test says so.

## 3. The rule, and the direction that makes it safe

Nearest wins between two word classes, **defaulting to the debt**.

The asymmetry is the whole safety argument and it is not a preference:

- reading a **budget as a debt** costs a hand override — which is the state this task replaces, and is visible;
- reading a **debt as a budget** removes an occurrence from the gate **silently**.

So a budget word must be strictly nearer than any provenance word,
and a token with **no** governing word at all stays a debt.
Measured, that direction never fires wrongly: **0 false positives at every one of twelve radii.**

## 4. The window is swept to a plateau, and the plateau is a THEOREM

`C-0176`'s standard is *the middle of a flat region rather than a fitted number*.
Here the sweep has more structure than that, and it is derivable before it is run:

> A match found at radius `R` sits at distance at most `R`,
> so enlarging `R` can only add candidates **further away**,
> and can only change an occurrence for which **neither** class matched.

So a nearest-wins refinement is **monotone**: a wider window moves a token **off** the default
and never back onto it. That makes *"the plateau"* well defined —
and it makes the **smallest sufficient radius the safest**, because every extra character
can only convert a debt-by-default into a budget, which is the unsafe direction.
The monotonicity is asserted as a named test over five fixtures at three radius pairs, not argued.

| radius | forward budget | provenance | false positives | false negatives | what flipped |
|---|---|---|---|---|---|
| 60 | 15 | 55 | **0** | 5 | — |
| 80 | 17 | 53 | **0** | 3 | two occurrences off the default |
| 100 | 19 | 51 | **0** | 1 | two more |
| 120 | 19 | 51 | **0** | 1 | — |
| 150 | 19 | 51 | **0** | 1 | — |
| 200 | 19 | 51 | **0** | 1 | — |
| 250 | 19 | 51 | **0** | 1 | — |
| **300 — adopted** | **20** | **50** | **0** | **0** | one more, and the split is complete |
| 400 | 20 | 50 | **0** | 0 | — |
| 500 | 21 | 49 | **0** | 0 | a **borderline**, reached across a queue-row boundary |
| 800 | 21 | 49 | **0** | 0 | — |
| 1200 | 21 | 49 | **0** | 0 | — |

Two flat regions, and the adopted one is the first at which the split is complete.
At **500** the window reaches into a **neighbouring queue row** — `TASKS.md` is a paragraph per physical line —
and the occurrence it flips there is the row that *asked for this split*,
which the hand reading declined to label at all.
That is `STRUCTURAL_WINDOW`'s own recorded failure mode, met on a second predicate.

**And the radius is not a new constant.** It is `REFINE_WINDOW = 300`,
the corpus's own refinement radius, already used by the width refinement,
and a named test holds it to that rather than to a literal.

## 5. Every reclassification, read individually

The result file carries all 70, one row each, with the hand reading, the rule's verdict,
and the distance to the nearest word of each class.
**At the adopted radius there are zero disagreements** over the 66 occurrences the hand reading labelled.

The other **four** are labelled *borderline* in the hand reading, they are excluded from
the false-positive and false-negative counts, and **all four take the safe default**:

| occurrence | why it is borderline | rule |
|---|---|---|
| the queue row asking for this split | a statement about the **tool**, not a design premise | default |
| the same row's explanation of the two readings | as above | default |
| a remainder-table cell pairing a Gen-1 **single-layer** tile with a 2009 scaffold | a forward budget on a premise the same table also uses as provenance; **no governing word of either class is in range** | default |
| a reproduction row citing an upstream claim's row-width **ceilings** | a forward budget quoted as a check, inside a correcting claim, so the class is `CORRECT` either way | default |

The first two no longer exist: this task rewrote the queue row **without spelling the token** (§8).

## 6. The widenings were measured before they were written

`C-0176` rejected two natural repairs on measurement rather than taste,
and its rule is that a **widening**'s cost is its false positives.
Every candidate here was scored against the same hand reading before one was adopted
(all at the baseline ref; `at 300` is the adopted radius):

| budget words | forward budget at 120 | at 300 | at 1200 | false positives, any radius |
|---|---|---|---|---|
| the verbs alone — the starter set the queue row named | 13 | 16 | 19 | **0** |
| + a bare *short* | 14 | 17 | 19 | **0** |
| + *built allowance* | 18 | 19 | 21 | **0** |
| + *recommended raster* | 14 | 17 | 20 | **0** |
| + the Gen-1 tile's own coordinates | 18 | 19 | 21 | **0** |
| **+ the coordinates AND a bare *short* — adopted** | **19** | **20** | **21** | **0** |
| + the word *budget* — **REJECTED** | 13 | 16 | 19 | **0** |

Two of these rows are findings rather than arithmetic:

- **The verbs alone are not enough.** The starter set the queue row named leaves **four**
  occurrences on the default at the adopted radius — every one of them a
  *width against §3's nominal* statement, where the governing word is a **dimension** and not a verb.
  What reaches them is the second class: the **Gen-1 tile's own coordinates**,
  which is `C-0193`'s own discriminator (*which object is this about*) written as a pattern.
- **The word *budget* is inert, not merely dangerous.** It was the obvious candidate and it adds
  **nothing at any radius** — because the 2009 block has a scaffold budget too,
  and every sentence carrying the word already carries a nearer provenance word.
  A widening that changes no verdict is a widening that was measuring the wrong thing.

## 7. The debt line, before and after, in both denominators

The census prints an advisory line the gate does not fail on,
because `T-233` owns the two deliverables and this task does not edit them.

| | numerator | all families | ratio | `CH-0230`'s own denominator | ratio |
|---|---|---|---|---|---|
| **before**, as committed | 24 | 93 | `0.258064516` | 68 | `0.352941176` |
| **before**, unmanaged | 30 | 93 | `0.322580645` | 68 | `0.441176471` |
| **after**, as committed | 23 | 93 | `0.247311828` | 61 | `0.37704918` |
| **after**, unmanaged | 25 | 93 | `0.268817204` | 61 | `0.409836066` |

*Unmanaged* is the gate's verdict with **no hand override at all** — every entry from the emitter's stated rules.

Three readings of one table:

1. **The managed all-family ratio falls**, `0.258064516 → 0.247311828`, on exactly **one** occurrence:
   a deliverable sentence saying what a longer scaffold **affords**, which nobody had overridden
   and which the gate had been listing as debt. That is a **correction**, not a discount.
2. **`CH-0230`'s own narrow denominator RISES**, `0.352941176 → 0.37704918`,
   because a correct restatement leaves the numerator **and** the narrow denominator together.
   That is [`C-0179`](C-0179-the-debt-line-as-a-ratio.md)'s measurement, met on a third family
   and by a different mechanism: there a *sentence* was written, here a *reading* was split off.
   Both readings are printed by the tool and neither is quoted alone.
3. **What the split really buys is the unmanaged reading.** The gap a reader's typing was carrying
   falls from **6** to **2**, i.e. the rule now carries two thirds of what sixteen hand overrides did.

## 8. `CH-0182`, and the one place it does NOT bite

A claim explaining a census family has to quote that family's own worked examples,
so writing it moves the number it reports — eight consecutive iterations of it.

**This claim does not enter the census at all.** It never spells the family's token:
the split is explained by naming the two **governing** word classes,
which is what the refinement reads, and the token itself is never needed.
Measured, the family census with and without this file is **identical, key for key**,
and the debt line's two readings **coincide** because the line counts only the two deliverables.
So the honest report is not *"both readings, and here is the difference"* but
*"both readings were taken and they are the same object"* — and that is a property of
**how the claim is written**, not a property of the census.

The queue row was rewritten on the same principle and it is why it is **two occurrences shorter**
than the one it replaces: a row asking for a token to be classified should not add two of it.

## 9. The intermediate reading, emitted rather than described

[`C-0184`](C-0184-a-slug-is-not-a-statement.md)'s trap: the classification is keyed on an
occurrence **index**, so a reading taken between a predicate change and its regeneration
measures nothing — and *"this measures nothing"* is a sentence a reader has to take on trust
unless the reading is published.

With the refinement in and the classification not yet regenerated the gate read:

```
GATE 3 defect(s)
  wrong discharge: ANSWERS.md#47 is MOVED on family FORWARD_BUDGET, which belongs to no discharge at all
  wrong discharge: DECISIONS-FOR-NDI.md#19 is MOVED on family FORWARD_BUDGET, ...
  wrong discharge: DECISIONS-FOR-NDI.md#29 is MOVED on family FORWARD_BUDGET, ...
  T-233 debt 23 of 93 occurrence(s) = 0.247311828
```

The three defects are the **two-layer agreement** doing its job:
a family this census does not gate may not carry a class it does,
and the coercion that fixes it lives in the emitter.
The debt figure in that intermediate state happens to equal the final one,
which is precisely why it must be shown rather than asserted — it is right for the wrong reason
until the table is regenerated.

## 10. What the regeneration found, which is a live instance of a recorded trap

Regenerating lost **sixteen** hand overrides — every one the family had — and printed **fourteen**
`DROPPED` lines, because two of the sixteen shared a key with a third. **Thirteen are the point of
the task.** The other three were not:

> three `RECORD` calls on one claim's verbatim quotation of the paper's own scaffold-pairing
> sentence were stored with **`snippet: null`**.

`override_key` is `(file, family, token, snippet[:100])`,
so a stored entry with no snippet keys as `(file, family, token, "")`
and the live occurrence keys as `(file, family, token, "…the sentence…")`.
They could never match. **Any** regeneration would have dropped those three,
for the stated reason *"no longer matches any occurrence"* — which is false about a line nobody touched.
`CLAUDE.md` already records the trap; this is it, live, in a claim filed the previous day.

And it disabled the **second** guard as well, which is the part the recorded entry does not carry.
An override with no snippet keys as `(file, family, token, "")`,
so all three collapsed onto **one** key — which is exactly what
`hand_overrides`'s collision report exists to catch, *"a reader's call landing on the wrong
occurrence is the one failure this mechanism must not have"*.
It reported **nothing**, because it fires only where two colliding entries **disagree** about the class
and these three agreed. A missing snippet therefore does not merely break an override;
it makes three overrides indistinguishable **and** silences the check that would say so.
They are restored here **with** their snippets, and the emitter is now idempotent:
a second run writes a byte-identical table.

Those three are also the whole of the residue: **hand overrides on the family, 16 → 3**,
and the three that remain are a **class** call about a quotation, not a **family** call —
they survive because the emitter's quotation rule reads the line's **opening**
and that sentence is quoted mid-line. That is a different rule's gap and it is not repaired here.

## 11. Verification

| gate | how | verdict |
|---|---|---|
| 1 — dimensional | no quantity is computed; every ratio is an exact quotient of two integers | **N/A, stated** |
| 2 — limiting cases | radius `0` and radius `10⁶` are both mutation rows, and both fail named tests; a token with no governing word of either class is a named test | **PASS** |
| 3 — symmetry / conservation | the family census is a **partition**: `50 + 20 = 70` at the baseline ref, and no occurrence leaves or enters. The two-layer agreement (`classify`) is asserted in **both** directions for the new family | **PASS** |
| 4 — convergence | the radius sweep, 12 points from 60 to 1200, with the monotonicity theorem asserted as a named test | **PASS** |
| 5 — cross-check | the hand reading of all 70 occurrences, recorded **before** the rule and retained as data in the emitter; **0** disagreements at the adopted radius over the 66 labelled ones | **PASS** |

**Mutation coverage: 71 rows, 0 failing nothing, 0 killed by an exception**, over 225 named tests.
The baseline is subtracted (`CH-0237`) — the unmutated copies fail nothing.
Two rows deserve naming:

- *the DEFAULT flipped: a token with no governing word leaves the gate* — the one direction a
  split of a **gated** family may not have. It fails five named tests.
- *the split taken too far: the whole family declared not a debt* — which would remove the
  withdrawn premise from the gate along with the budget. It fails three.

Two **pre-existing** anchors moved because this family is the ninth,
and both are repaired rather than deleted: the discharge map and the coercion map are now
quoted from the live source, so a mutation of either still replaces the rule **wholesale**
and cannot go stale on the next family. That is `CLAUDE.md`'s own
*a mutation anchor is a reference into somebody else's source, and a refactor orphans it*,
met inside one file rather than across two.

**And one pre-existing crash-kill was found by the same run and repaired.**
A mutation that empties the census's own result list was killing a named test by `IndexError`
rather than by name — `CH-0237`'s *a mutation killed by an exception is a fixture defect, not a
measurement*, present at `b04a675` and invisible in the headline, because the harness's exit code
turns on **survivors** and a crash reads as a kill. The run now reports **zero** of them.

## 12. Validity range

- The split is measured over the **in-scope** corpus only —
  `gpd/claims/*.md`, `TASKS.md` and the two deliverables. A task file, a challenge or a result
  file is not read by this census and is not covered by any number here.
- The **0 false positives** is a measurement over 66 labelled occurrences at twelve radii,
  not a proof. A sentence putting a provenance statement nearer a budget word than to any
  provenance word would be misread, and the failure would be **silent**. What bounds it is the
  default: an occurrence with no governing word at all stays a debt, so the rule can only be
  wrong where a budget word is genuinely present and genuinely nearer.
- The hand reading is **one reader's**. It is retained as data in the emitter, keyed on
  `(file, index)` at a named ref, precisely so that a second reader can disagree one occurrence
  at a time rather than with a count.
- Nothing here touches the two deliverables, and nothing here is evidence that their debt
  is smaller. It is evidence that **seven** of the occurrences the gate was counting are about
  a different object.
