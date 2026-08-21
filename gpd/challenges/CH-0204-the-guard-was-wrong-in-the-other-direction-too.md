# CH-0204 — the prose census's trailing guard has now been wrong **twice, in opposite directions**, and its FALSE-NEGATIVE rate was never measured: `(?!\w)` refuses a number abutting a **unit letter**, which is **31 tokens in 4 of the 47 files** the claim certified — and the instrument that found it was built to PAIR the census's output, not to produce it

| | |
|---|---|
| **Against** | [`C-0153`](../claims/C-0153-unrounded-prose-interpolations.md) (`T-249`) — §2, *"the false-positive rate is a PROOF over the population, not a sample"*, and §3, *"the checker's own first draft was wrong, in the direction the corpus is made of"* |
| **Raised by** | [`T-250`](../tasks/T-250-prose-interpolation-sweep.md) / [`C-0156`](../claims/C-0156-prose-interpolation-sweep.md) |
| **Grounds** | **methodological** — a measured false-**positive** rate standing in for a completeness argument, with the false-**negative** rate unstated |
| **Status** | **PARTLY DISCHARGED** by `T-250` (the guard is repaired and the 31 tokens are swept); **OPEN** on the general form |

---

## The observation

`C-0153` §3 is an unusually honest section. It records that the predicate's first draft carried the
symmetric trailing guard `(?![\w.])`, that this refused **every number at the end of a sentence**,
that the blind spot *"is invisible in exactly the cases it misses"*, and that the measured cost in
this corpus was **one token**.

It then ships `(?!\w)(?!\.\d)` and measures the **false-positive** rate exhaustively — 0 of 757, by
the shortest-round-trip test — and the claim's credibility rests on that number, explicitly:
*"the census's own credibility rests entirely on its measured false-positive rate"*.

**A false-positive rate is not a completeness argument, and the guard was wrong in the other
direction at the same time.** `(?!\w)` refuses a decimal followed by any word character — and this
corpus writes a ratio as `26.381529916714886x`, with the multiplication sign abutting the number.
Measured over the same committed corpus the claim certified:

| | tokens | files |
|---|---|---|
| `C-0153`'s shipped guard, `(?!\w)(?!\.\d)` | 747 | 47 |
| the same predicate with `(?!\d)(?!\.\d)` | **778** | 47 |
| the difference | **+31** (+4.1 %) | in **4** files |

Every one of the 31 is a genuine over-precise ratio — `1.70878479537323x A2's 78.235`,
`9.762128175180635x the margin`, `15.110341201220582x CHEAPER` — and **not one is a false
positive**, so the widening is strictly better rather than merely wider.

## Why the guard was there, and why the replacement keeps what it was for

`(?!\w)` was not arbitrary. `C-0153` states its purpose: it *"refuses a **prefix** of a longer
token, which is what makes the captured literal the whole `2.314028420585025E-7` rather than its
mantissa"*. That property is worth keeping — a mantissa without its exponent is a different number
by seven orders of magnitude.

It is kept, and by the regular expression's own greediness rather than by the guard:
`(?:[eE][+-]?\d+)?` is greedy, so the exponent is taken whenever it is there, and `(?!\d)` refuses
a prefix of a longer digit run. `PROSE_TOKEN_TESTS` now carries
`2.314028420585025E-7x the bound` and asserts the whole token, so the property is held open by a
named test rather than by a guard that also refuses a unit.

## What found it, and why that is the finding

Not the census, and not a reading of it. `tools/T-250-movement.py` exists to **pair** the tokens of
two renderings of one sentence so that the staleness identity can be checked token by token — and a
pairing tool cannot use a detector's guard, because a detector may prefer a false negative and a
pairing tool may not. Running it on the first re-emitted file reported four sentences whose digits
had visibly moved and in which it could find no moved token. That is the whole trace.

**A checker's blind spot is invisible in exactly the cases it misses, so it is not found by looking
at the checker — it is found by an instrument that must agree with it and cannot.** `C-0153` found
the first blind spot by *writing the tests*; this one was found by *consuming the output*. The
repository has now recorded three members of this family (`tools/check-corpus-links.py`'s 30 broken
links, `tools/check-kotlin-format-strings.py`'s nested-template strip, and this guard twice), and
the general rule they support is the one this challenge is about.

## The residue, which is why this stays OPEN

`C-0153` states the direction of its bound correctly — *"a strict LOWER bound … an unrounded number
whose `Double.toString()` happens to be short (`33.5`, `0.125`) is indistinguishable from a rounded
one and is not counted"* — and **does not measure it**, on the ground that the source-side census
*"needs a type Python does not have"*.

`T-250` measures the class the short ones are drawn from and it is large: **2 096 string leaves of
the committed corpus are a bare number at any precision, in 76 of 144 files.** The prose gate
reaches the 778 above nine digits and cannot reach the rest — which is
[`CH-0205`](CH-0205-a-number-typed-as-a-string-is-untyped-as-well-as-unrounded.md).

## What would settle it

A **source-side** census, which is the complement and which nobody has run: a `${…}` interpolation
of a `Double` inside a string literal that reaches a result field. It needs Kotlin's own type
information rather than a regular expression, so the cheap instrument is the compiler — a lint rule
or a small compiler plugin — and it is the only thing that can turn a lower bound into a count.
