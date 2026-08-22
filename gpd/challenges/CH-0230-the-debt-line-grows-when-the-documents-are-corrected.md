# CH-0230 — the advisory `T-233 debt` line is a count over a MOVING corpus and every one of its increases over 40 revisions is a synthesis pass, so it is not a measure of debt: a correcting sentence has to NAME a withdrawn premise in order to withdraw it

| | |
|---|---|
| **Against** | the `T-233 debt` line of [`tools/T-234-census.py`](../../tools/T-234-census.py) `--check`, and the reading every synthesis claim since [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) has put on it — a work list of what the two deliverables still owe |
| **Raised by** | [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) / [`T-262`](../tasks/T-262-width-restatement-predicate.md), whose own `F3` fired on it |
| **Kind** | **methodological — a monotone counter read as a debt.** The count is correct; what is wrong is the sentence attached to it |
| **Status** | **raised, and the tool now says so in its own output.** What is not repaired is the metric: no predicate can make this line fall when the documents are corrected |

---

## The measurement

For each of the last **40** revisions of `ANSWERS.md` and `DECISIONS-FOR-NDI.md`,
count the `WIDTH`/`PLACEMENT` token-family occurrences that are neither struck nor already carrying a forward pointer —
the debt line's own predicate, minus the class judgement, which is what makes it computable from a historical text alone.

| commit | pass | old predicate | new predicate |
|---|---|---|---|
| `89fd099` | the fifth `ANSWERS.md` synthesis | **+2** | +2 |
| `47ef394` | the sixth synthesis | **+1** | +1 |
| `7b6e465` | iteration 35 | **+1** | +0 |
| `413659f` | iteration 36 | **+3** | +1 |
| `49b1a01` | iteration 38 | **+9** | +1 |
| `d077d55` | the eleventh synthesis | **+3** | +2 |
| `cfbe0cc` | the twelfth synthesis | **+6** | +3 |
| **cumulative** | | **0 → 25** | **0 → 10** |

**Every increase is a document pass**, under both predicates.
There is no revision at which the count falls.
`T-262`'s own row records the same thing from the other end: the line went **23 → 32** in one pass and **all nine new occurrences were that pass's own correcting sentences**.

## Why this is not a nitpick

A number a synthesis reads as *"what the deliverables still owe"* is a number that decides where the next pass spends its hours.
Read as a debt it says the documents are getting **worse** each time they are corrected.
Read correctly it says the documents are **discussing** the correction more each time — which is what a correction *is*.

The mechanism is not subtle and it is not fixable by a better predicate:
**a correcting sentence has to name the withdrawn premise in order to withdraw it.**
*"`C-0140` withdrew 112 bp as a uniform tile width and `C-0146` restores it as a row span"* contains the token twice, correctly, and a token census counts it twice.

`C-0176`'s split cuts the rate by about three fifths — 15 of 25 over the series, 8 of 9 at the largest single pass — and **does not change the sign**.
That was a declared falsifier of `T-262` and it fired.

## What is challenged, and what is not

**Not challenged:** the count. It is right, it is reproducible, and the per-occurrence list behind it is correct.

**Challenged:** the word *debt*, and every downstream reading that treats a rise in the line as work arriving.
The line measures **how much of the corpus talks about the correction**, which rises with every pass that does the work properly.

## What would settle it

Three candidates, in increasing cost, none of them taken here:

1. **Say it in the output.** Done — `--check` now prints, beside the count, that the line is a count over a moving corpus, that every increase over 40 revisions is a synthesis pass, and where the series is. It is the cheapest half and it is the half that stops the misreading.
2. **Count the DENOMINATOR too.** A debt fraction — unpointed occurrences over all occurrences of the same families — falls when a pass adds pointed sentences, which is the behaviour the word *debt* implies. It needs no new predicate; it needs the number to be published as a ratio rather than a count.
3. **Count LINES rather than occurrences.** `CH-0182` already measures the difference for a related census (21 named lines against 27 true ones). A physical line is closer to a unit of editing work than a token is, and a correcting sentence that names a premise three times is one edit.

Until one of those is taken, quote the line **with what it counts** — which is `CLAUDE.md`'s own *quote it with the state it is read at*, applied to a checker's own output.
