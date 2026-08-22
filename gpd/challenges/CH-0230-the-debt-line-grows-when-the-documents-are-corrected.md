# CH-0230 — the advisory `T-233 debt` line is a count over a MOVING corpus and every one of its increases over 40 revisions is a synthesis pass, so it is not a measure of debt: a correcting sentence has to NAME a withdrawn premise in order to withdraw it

| | |
|---|---|
| **Against** | the `T-233 debt` line of [`tools/T-234-census.py`](../../tools/T-234-census.py) `--check`, and the reading every synthesis claim since [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) has put on it — a work list of what the two deliverables still owe |
| **Raised by** | [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) / [`T-262`](../tasks/T-262-width-restatement-predicate.md), whose own `F3` fired on it |
| **Kind** | **methodological — a monotone counter read as a debt.** The count is correct; what is wrong is the sentence attached to it |
| **Status** | **raised, and ANSWERED in candidates 1, 2 and 3 — but not in its mechanism.** Candidate 1 is done (the tool says so in its own output); candidate 2 is done by [`C-0179`](../claims/C-0179-the-debt-line-as-a-ratio.md) (`T-280`), **which refutes this challenge's own statement of it** — the denominator named below, *all occurrences of the same families*, fell at **0** of the 4 non-saturated passes and rose at **4**, while a denominator over **every** occurrence the census finds fell at 3 and rose at 1; candidate 3 is **priced and declined** there. ~~What is not repaired is the metric: no predicate can make this line fall when the documents are corrected~~ — the clause about a **predicate** stands and the clause about the **metric** does not: no predicate can, and a **ratio** can, because a correct restatement lands in a family this census does not gate (`C-0176`'s split, without which candidate 2 would carry nothing). The **count's** growth is unchanged, which is this challenge's real content |

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
   **TAKEN, and half of this sentence is wrong** ([`C-0179`](../claims/C-0179-the-debt-line-as-a-ratio.md), `T-280`): *"needs no new predicate"* is right, and *"the same families"* is the denominator that does **not** fall. A correcting sentence lands in the numerator **and** the denominator at once, and a ratio below one that gains equally top and bottom goes **up** — measured, that reading rose at **4 of 4** non-saturated passes. What falls is the ratio over **every** occurrence the census finds in the two deliverables, because a correct restatement reads as a family this census does not gate: **3 fell, 1 rose**, and the rise is a pass that added two unpointed assertions and no repair.
3. **Count LINES rather than occurrences.** `CH-0182` already measures the difference for a related census (21 named lines against 27 true ones). A physical line is closer to a unit of editing work than a token is, and a correcting sentence that names a premise three times is one edit.
   **PRICED and DECLINED** ([`C-0179`](../claims/C-0179-the-debt-line-as-a-ratio.md), `T-280`), and not on cost: over the same 40 revisions the **line** count rose at **11 of the 11** passes at which the occurrence count rose, so it does not change the sign either; and **7 of 51** deliverable lines carry occurrences of more than one **class** and **6 of 51** of more than one **discharge**, so a line-keyed census cannot represent `T-260`'s partial discharge at all. It buys a compression of **1.7254902×** and would undo the data structure `C-0176` built.

~~Until one of those is taken, quote the line **with what it counts**~~ — all three are now taken (two adopted, one declined), and the line prints its count, its ratio and the **name of its denominator** together. Quote it with **all three**, which is `CLAUDE.md`'s own *quote it with the state it is read at*, applied to a checker's own output.
