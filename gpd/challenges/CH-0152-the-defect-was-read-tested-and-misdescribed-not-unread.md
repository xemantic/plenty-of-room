# CH-0152 — *"a result file is read once, while the claim is being written, and never again"* explains the twelve raw conversions and **does not** explain `T-148`'s saturated statistic. That number was **read, described in prose, and unit-tested — and the description says the opposite of what the number means**

| | |
|---|---|
| **Against** | [`CH-0150`](CH-0150-twelve-of-the-fourteen-were-visibly-broken-and-the-silent-failure-diagnosis-does-not-explain-them.md)'s diagnosis, carried into [`C-0127`](../claims/C-0127-format-string-repair.md)'s headline: *"**This defect class damages the machine-readable artifact and spares the human one**"*, and *"the reason they survived is that **nobody re-reads a result file once the claim is written**"* |
| **Raised by** | [`C-0129`](../claims/C-0129-result-file-hygiene.md) (`T-208`/`T-209`/`T-210`), iteration 29 |
| **Grounds** | **a counter-instance found by `CH-0150`'s own repair.** `C-0127` uncovered `T-148`'s saturated statistic *because* repairing a print made the note legible — so the instance is inside the population `CH-0150` generalises over, and it fails the generalisation in both halves |
| **Status** | **OPEN** |
| **What moves** | The *diagnosis*, and therefore **where the guard belongs**. `CH-0150`'s repair — grep the emitted file — is correct and cannot reach this class. No verdict of `C-0127` moves; the thirteen defects it found are still thirteen defects and are still repaired |

## The charge

`CH-0150` asks why a defect that is *"as loud as a defect can be"* survives several iterations, and answers:
because a result file is written once, read once while the claim is being drafted, and never opened again.
The evidence is strong and is not disputed: **not one of the seven claims inherited a defective number.**

`T-148`'s exceedance statistic is a defect in the same corpus, found in the same pass, and **every clause of that answer is false of it**.

| `CH-0150`'s premise | `T-148`'s saturated statistic |
|---|---|
| the artifact was not read | the number was read and **described in a sentence written around it** — `"the binomial standard error at 10 000 draws is <value>, which is the resolution the verdict is quoted to"` |
| the human artifact was spared | `C-0087`'s **`P3` predicate** — *"a distribution, not a point … with a binomial standard error on every exceedance probability"* — is discharged **by this statistic** |
| a grep of the emitted file would have caught it | a grep finds `0.0000`, which is what the sentence says it is. **There is nothing textually wrong.** |
| the degeneracy was unnoticed | `src/test/kotlin/coupling/StapleDropoutTest.kt` has asserted `binomialStandardError(1.0, 100) == 0.0` since `C-0087`. **It is tested.** |

So the number was **computed correctly, emitted correctly, unit-tested, read, and written about** —
and the sentence written about it states the opposite of what it means.
`√(p̂(1−p̂)/n)` at `p̂ = 1` is a function of `p̂` alone: it is `0` at 1 250 draws and `0` at 20 000,
so calling it *"the resolution the verdict is quoted to"* asserts that the sample size is irrelevant to the confidence,
which is exactly backwards.

## What this changes

**Two failure modes, not one, and only one of them is reachable by a text gate.**

| | `CH-0150`'s class | this class |
|---|---|---|
| what is wrong | the **rendering** | the **interpretation** |
| how it survives | the artifact is not re-read | the artifact is re-read and the reader's own model is wrong |
| the guard | grep the emitted file (`T-208`'s gate, now wired, **0 defects over 119 files**) | **none exists**, and none can be written on the text |
| the census | 13 fields, 7 files, all repaired | **302 records in 7 files**, of which this task repairs 25 in 1 |

The second class is much larger than the first in this corpus and it is invisible to every gate this repository owns.
`T-208`'s `--saturated` audit finds it only because the degeneracy is *arithmetically* detectable —
a proportion of exactly `0.0` or `1.0` beside a standard error — and that is a lucky property of this one instance, not a method.

## What would settle it

A second instance of `CH-0150`'s class in which the emitted text is **correct** and the claim inherits a wrong meaning anyway
would confirm that the two classes are distinct and that the split, not the diagnosis, is what the guard should be built on.
A demonstration that no such instance exists outside `T-148` would reduce this to a single exception and leave `CH-0150`'s generalisation standing.
