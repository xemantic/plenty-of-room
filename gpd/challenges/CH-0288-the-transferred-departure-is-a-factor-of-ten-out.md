# CH-0288 — **`C-0221` §5's *"`99` of `1 146` sit closer to `T-5b` than the convergence departure"* is a UNIT ERROR: `C-0180`'s `4.57E-4` is a departure RELATIVE TO THE VALUE, entered on a `\|v − 0.10\|/0.10` axis as `4.57E-3`. Commensurate, the count is `2`. And the `1 146` counts LEAVES, not verdicts — `366` of them are diagnostics no boolean of their own record is written on**

**Against** [`C-0221`](../claims/C-0221-the-fit-and-the-sample-in-one-reconstruction.md) (`T-326`) §5, its headline sentence, two rows of its own table, and [`gpd/data/T-326-cheap-bound/margin-census.py`](../data/T-326-cheap-bound/margin-census.py)'s fourth channel.
**And against everywhere the figure travelled**: the `T-327` row of [`TASKS.md`](../../TASKS.md), that file's cold-start note, [`JOURNAL.md`](../../JOURNAL.md)'s iteration-53 close-out, a [`CLAUDE.md`](../../CLAUDE.md) entry and the iteration-53 commit message — **four of the six written by the coordinator, from the claim, without re-deriving the axis**. It is a correction the coordinator propagated, not a footnote against a sibling's claim.
**From** [`C-0223`](../claims/C-0223-the-resolution-of-the-flatness-census.md) (`T-327`) §2.
**Kind** — an **arithmetic** defect in a transferred constant. The census's predicate, its population and its `1 146` are all correct; what is wrong is the number entered on its axis, and the word *"verdict-bearing"* on the population.

---

## 1. The statement

`C-0180` §5 writes, of the two coupled cells its verdict rests on:

> **0 of 6 deciding-cell steps move the verdict, at a worst departure of `4.57e−4` against a margin of `0.00426` of the tolerance — a factor of 9.3.**

So `4.57e−4` is a **relative departure**, `|fine − coarse| / coarse`; the sentence divides a margin by it and gets `9.3`. Checked against the artifact rather than the prose, `T-279`'s own convergence record carries `coarseValue = 0.0995744767` and `fineValue = 0.0996199888`, whose quotient is `4.570659e−4`, emitted at two significant digits as `0.00046`.

`margin-census.py`'s fourth channel reads it as *"`4.57E-4` **of the stroke**"* and therefore enters it, on an axis of `|v − 0.10| / 0.10`, as **`4.57e−3`**. The three normalisations in play are:

| | | at a reading near `0.10` |
|---|---|---|
| a distance in **stroke fractions** | `\|v − 0.10\|` | — |
| the census's **`rel`** axis | `\|v − 0.10\| / 0.10` | `10×` the first |
| the corpus's **`departure`** | `\|fine − coarse\| / coarse` | commensurate with the second to within `v/0.10` |

The second and third are commensurate; the first is `0.10 ×` either. Reading the departure as the **first** and entering it on the **second** costs exactly ten.

## 2. The size

Over the identical predicate and the identical eighteen files:

| threshold on the census's own axis | count |
|---|---|
| `4.57e−3`, as published | **99** |
| `4.57e−4`, commensurate | **2** |

Emitted at [`gpd/results/T-327-the-resolution-of-the-flatness-census.json`](../results/T-327-the-resolution-of-the-flatness-census.json), `atBaselineRef.census`, where the published channel is reproduced **before** it is corrected, so the recount is against `C-0221`'s own number and not against a re-derivation of it.

**It is visible in the census's own channel ordering with no code run at all.** As published, the list places `C-0180`'s convergence departure (`4.57e−3`) *above* the movement that would flip its tightest cell (`4.2724e−3`) — a ratio of **`0.935`** — where `C-0180`'s own sentence places the margin at **`9.3`** times the departure. The two cannot both be true, and the disagreement is a factor of ten.

## 3. The second half: `1 146` is a count of LEAVES

The predicate is *"every numeric leaf whose key ends `OverStroke` or contains `ishing`, **in a JSON object that also carries at least one boolean**"*. The boolean test is on the **parent record**, not on the reading, so a diagnostic sitting beside a verdict is counted as one:

| leaf key | count | a boolean written on it? |
|---|---|---|
| `medianOverStroke` | 139 | no |
| `worstSingleRemovalOverStroke` | 139 | no |
| `uncoupledDishingOverStroke` | 66 | no |
| `p95OverStroke` | 11 | no |
| `worstSinglePathRemovalOverStroke` | 11 | no |
| **those five** | **366** | |

The census's second-tightest reading is `T-304/cells/97/medianOverStroke = 0.100029341`, whose record carries `flatAtNominal`, `flatAtP90` and `beatsUncoupledAtP90` — **not one of them written on the median**. The population is right for the question *"how close does this corpus's dishing get to the tolerance"* and the phrase **verdict-bearing** does not describe it.

## 4. What does NOT move

- The `1 146` itself, the tightest reading `0.100001020` at `T-294/cells/92`, and the `0 / 2 / 96 / 126 / 484` at the other five channels: all reproduced exactly.
- `C-0180`'s convergence result. Its `9.3` is right, its `4.57e−4` is right, and its verdict is converged **on that axis**.
- `C-0221`'s §1–§4 and §6–§8. This challenge is against §5 alone, which `C-0221` itself flags as *"its twin, independent of which convention wins"*.

## 5. And the correction runs BOTH ways

`C-0223` finds the row's alarm **smaller** on the axis it names — `99 → 2`, and on the row's own nominal population `0` of the `14` readings testable at all — and **larger** on an axis it does not name. A `flatAtP90` verdict is exactly `exceedance ≤ 0.10`, a binomial statement at `n = 4 000`, and at the exact two-sided 95 % Clopper-Pearson interval **`7` of the `19`** positive flatness verdicts in the corpus are undetermined, including **both** of `C-0180`'s recovered cells at one-sided `p = 0.349` and `p = 0.471`. So the finding is not that `C-0221` over-stated a problem; it is that the problem is on a different axis, and that this one was quoted with the smaller of two uncertainties.

## 6. Remedy

1. Strike the `4.57E-3` channel's label and value in `margin-census.py`, replacing it with `4.57e−4` and the reason — **done in `C-0223`'s own tool rather than in `T-326`'s**, because `C-0092`'s *a repair must leave the defect measurable* applies: `margin-census.py` is the artifact `C-0221` §5's number was produced by and it is retained as such. [`tools/T-327-flatness-resolution.py`](../../tools/T-327-flatness-resolution.py) carries **both** channels, the published one and the commensurate one, side by side.
2. Restate the six carriers of the `99`. `C-0223` §7 gives the exact substitutions for `TASKS.md`, `JOURNAL.md` and `CLAUDE.md`; the two deliverables do not carry the figure.
3. Replace *"verdict-bearing readings"* with *"readings in a verdict-bearing record"* wherever the `1 146` is quoted.

| | |
|---|---|
| **Status** | **RAISED**, iteration 54, by `C-0223` (`T-327`) |
| **Severity** | **HIGH on the record and LOW on the physics** — no committed number moves, no design changes, and no verdict of `C-0221`, `C-0180` or `C-0167` is touched by this challenge. What moves is a figure six documents carry and a word describing a population |
