# CH-0199 — a number **quoted** in a claim has no mechanical link back to the result file it came from, so a re-emission orphans it silently: **19 of 43** over-precise tokens in the corpus's own documents are already unfindable in any committed result file, and nothing can tell a deliberate historical quotation from a stale one

| | |
|---|---|
| **Against** | [`C-0101`](../claims/C-0101-re-emitting-what-the-repair-moved.md) (`T-167`) — *"re-emit the downstream file and amend the claim … amend the claim only where it **quotes** a moved number"* — and every claim that has since followed it |
| **Raised by** | [`T-249`](../tasks/T-249-unrounded-prose-interpolations.md) / [`C-0153`](../claims/C-0153-unrounded-prose-interpolations.md) |
| **Grounds** | **methodological** — a discipline stated as a rule, applied by hand, with no instrument, measured over the corpus |
| **Status** | **ANSWERED, iteration 46** ([`C-0198`](../claims/C-0198-a-quoted-number-has-no-link-back.md), [`T-252`](../tasks/T-252-a-quoted-number-has-no-link-back.md)), **on the second of the two branches the task offered and on a measurement**. The finding stands: `C-0101`'s rule is executed by hand and has no instrument. What is decided is that the instrument cannot be built on this population — the detectable class (a decimal above nine significant digits, which a rounded result file cannot contain) is **69 of 69 deliberate and 0 stale**, in three sub-classes that are all legitimate, so the convention would be written onto 69 correct quotations. And the corpus **manufactures** the class, by `C-0092`'s own rule that *a repair must leave the defect measurable*. **What is gated instead is the neighbouring question**: a `Provenance` row names its result file as a **bare path**, which `tools/check-corpus-links.py` cannot see — `tools/check-result-path-references.py`, 8 self-tests, wired, **192 claims, 175 naming a file, 0 broken** |

---

## The observation

`C-0101` established the rule and this repository has followed it correctly several times.
It is executed **by hand**, and its failure mode is invisible in both directions:

- `tools/trace-answers.py` traces a **synthesis** document's numbers against the **claim** corpus;
- `tools/result-transfers.py` compares one **result file** against another;
- `tools/result-reader-census.py` derives which **code** reads which file.

None of them checks a **claim's** number against the **result file the claim names**.
So when a repair re-emits a file, the claims that quote its numbers keep quoting the old ones, and
the only thing that finds it is somebody remembering.

## The measurement

`T-249`'s `--prose` predicate — a decimal token above nine significant digits — is a **detector for
exactly the tokens this can be measured on**, because those are the ones a rounded result file
cannot contain by construction and a claim can only have got from a file's own prose or from a run.

Over `gpd/claims/`, `gpd/challenges/`, `ANSWERS.md`, `DECISIONS-FOR-NDI.md`, `TASKS.md`,
`CLAUDE.md` and `JOURNAL.md`: **43** such tokens in **17** documents, of which **25** are still
present in some committed result file and **19 are not**.

| orphaned token | document | what it looks like |
|---|---|---|
| `1.109130975` | [`CH-0092`](CH-0092-the-propagation-did-not-close.md), [`C-0073`](../claims/C-0073-determined-precision-of-a-result-file.md) | a quoted result whose file has since moved |
| `0.9240787673730241` (×5) | [`CH-0178`](CH-0178-a-criterion-with-no-root-returned-its-own-bracket-floor.md), `gpd/challenges/README.md` | a bracket floor a repair removed |
| `0.6756091733686969`, `7.919686749317395` | [`C-0096`](../claims/C-0096-doubling-ladder-repair.md) | superseded by the continuation repair |
| `25.144662445344164`, `…167` | [`C-0110`](../claims/C-0110-device-b-tall-gap.md), `CLAUDE.md`, `JOURNAL.md` | the clamp's own two values, quoted as **evidence of a bug** |
| `3.033032179558636E121`, `108.37760001695746` | [`C-0101`](../claims/C-0101-re-emitting-what-the-repair-moved.md) | a diverged solver parameter, quoted as evidence |
| `0.5000000000000001` | `CLAUDE.md` | `0.125.pow(1.0/3.0)`, quoted as a floating-point fact |
| `0.1686405908358076` | [`C-0150`](../claims/C-0150-departure-spelling-set-and-the-wall-clock.md), `TASKS.md` | the **pre**-repair reading, quoted as evidence of `T-249` |

**The census is taken entirely at `HEAD`, documents and result files both, and it has to be.** Writing this challenge quotes fourteen of the nineteen orphans, so a census run over the working tree counts its own evidence and returns a larger number every time it is run — `CH-0190` records exactly that self-destruction for the token partition in `ANSWERS.md`. Measured over the working tree as this file was drafted the same predicate reads **66 tokens in 20 documents**, of which **38** are unfindable.

**And that is the challenge, not the list.** Several of those nineteen are *deliberate* — a claim
quoting the value a defect produced is doing exactly what `C-0092`'s rule (*a repair must leave the
defect measurable*) requires, and `CLAUDE.md`'s `0.5000000000000001` is a statement about IEEE 754
rather than a citation. Others are plainly stale. **No instrument in this repository can tell them
apart**, because the corpus carries no convention that marks a number as historical, and the
distinction lives in the sentence around it.

## Why this task raises it

`T-249` re-emitted `gpd/results/T-164-row-end-crossover-stiffness.json`, whose `F2` sentence carried
`3.3864695769825204E-11`. [`C-0099`](../claims/C-0099-row-end-crossover-stiffness.md) **quotes that
token verbatim**, once. `C-0101`'s rule says to amend it, and `C-0153` does — by hand, after a
`grep` that had to be thought of. Had it not been, the token would have become the twentieth row of
the table above, indistinguishable from the deliberate ones.

## What would settle it

Either instrument would do, and they differ in cost by an order of magnitude:

1. **A convention.** Mark a historical number where it is written — a backtick-plus-strike, or a
   `(pre-repair)` marker the checker knows — and then a token that is *unmarked* and *unfindable* is
   a defect by construction. `C-0071`'s *strike, never delete* is the same idea one level down, and
   `C-0109` already had to teach `trace-answers.py` to blank struck spans.
2. **A checker.** For every over-precise token in a document, look for it in `gpd/results/` and
   report the misses. Twenty lines, and it would have to be run as an **audit** at first, because
   19 of 43 fire today and a gate that cannot come clean is not a gate (`C-0083`).

Neither is built here. What is built is the detector the measurement rests on.

## What it does not challenge

`C-0101`'s rule, which is right, and which `C-0153` follows. This is a statement that a rule with no
instrument is a rule executed as well as its author's memory, and that the corpus can now measure
how well that has gone.
