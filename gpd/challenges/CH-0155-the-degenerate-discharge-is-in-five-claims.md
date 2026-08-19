# CH-0155 — `CH-0153` is a population too, and the population is **claims**: the same gate-4 discharge, *"a binomial standard error beside every exceedance"*, stands in **five** claims and is degenerate in all five — `C-0129` measured the result files and left the verification gates unmeasured

| | |
|---|---|
| **Against** | [`CH-0153`](CH-0153-a-statistical-power-gate-discharged-by-a-statistic-that-is-identically-zero.md)'s scope — it is raised against `C-0087`'s gate-4 row alone — and against the four claims that carry the identical discharge: [`C-0089`](../claims/C-0089-dropout-robust-placement.md), [`C-0093`](../claims/C-0093-shared-body-coupling.md), [`C-0098`](../claims/C-0098-shared-body-placement-and-distribution.md), [`C-0103`](../claims/C-0103-path-count-at-fixed-geometry.md) |
| **Raised by** | [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) (`T-213`), iteration 30 |
| **Grounds** | one `grep` over `gpd/claims/`. Five claims discharge their **statistical power** gate with a standard error that is **exactly `0.0` at 16 of 24, 16 of 39, 16 of 25 and 16 of 21 cells** of the four files respectively, and at 25 of 60 in `C-0087`'s. `C-0129` measured the population **of records** (302 in 7 files) and did not measure the population **of discharges** |
| **Status** | **UPHELD, and repaired for all five** |
| **What moves** | **No exceedance, no percentile, no `flatAt*` boolean and no verdict.** What moves is the evidence each gate stands on: the one-sided bound reads `p > 0.9976` at 1 250 draws and `p > 0.99985` at 20 000 where the symmetric error reads `0` at both, so **every failure verdict now rests on a statement that carries its own sample size** |

## The charge

`CH-0153` names one claim. The sentence it objects to is not in one claim.

| claim | file | `*StandardError` fields | saturated | at `p̂ = 1` |
|---|---|---|---|---|
| [`C-0087`](../claims/C-0087-position-dependent-staple-dropout.md) | `T-148-staple-dropout.json` | 60 | **25** | 25 |
| [`C-0089`](../claims/C-0089-dropout-robust-placement.md) | `T-155-dropout-robust-placement.json` | 24 | **16** | 16 |
| [`C-0093`](../claims/C-0093-shared-body-coupling.md) | `T-162-shared-body-coupling.json` | 39 | **16** | 16 |
| [`C-0098`](../claims/C-0098-shared-body-placement-and-distribution.md) | `T-165-shared-body-placement.json` | 25 | **16** | 16 |
| [`C-0103`](../claims/C-0103-path-count-at-fixed-geometry.md) | `T-163-path-count-fixed-geometry.json` | 21 | **16** | 16 |
| — (`C-0108` does not cite it in gate 4) | `T-178-count-phase-interaction.json` | 198 | **196** | 196 |
| — (`C-0109` does not cite it in gate 4) | `T-191-four-layer-tile.json` | 35 | **17** | 15 |

The four claims beyond `C-0087` all read, verbatim or nearly, *"a binomial standard error beside every exceedance"*, in a gate row headed **numerical convergence and statistical power**, with verdict `PASS`.
In every one of them the statistic is `√(p̂(1 − p̂)/n)` evaluated at `p̂ = 1` for a majority or near-majority of the cells, where it is **identically zero for every `n`** and therefore reports the saturation back to itself.

**And the degeneracy is not accidental — it sits exactly where the programme's answers are.**
All seven files are studies whose headline is that a design **fails** `T-5b`'s 0.10, and failing is the direction that drives the exceedance to `1.0`.
The better a design is, the more informative the symmetric error; the worse it is, the less.
A statistic that goes blind in the direction of the finding is not a weak instrument, it is the wrong one.

## What is not being challenged

- **No number moves and no verdict moves.** This is checked rather than inherited: `T-212`'s sweep re-emitted all six remaining files and **0 exceedance probabilities, 0 percentiles and 0 `flatAt*` booleans** moved. The exceedance was `1.0` and stays `1.0`.
- **The symmetric error is not deleted.** It is uninformative rather than wrong, and removing it would break every reader of the schema. A record is repaired by emitting the one-sided bound **beside** it — which is also the only repair the standing checker will recognise, in both directions, by self-test.
- **The gates are not re-graded to `FAIL`.** `C-0129`'s reading is right: what is in question is whether gate 4 was *discharged on that axis*, not whether the studies are sound. Each claim's other gate-4 evidence — the percentile at four or five sample counts, the dishing grid at 41/81/161, the common-random-number difference — is untouched and is what the convergence half of the gate actually rests on.

## The repair, and where it had to go

`C-0129` repaired `T-148` **at its emission site**, which is the same shape as the three per-file departure repairs its own §3 diagnoses.
All seven studies build their summary through **one** function, `coupling.summariseDropoutDishing`, returning one type, `DropoutDishing`.
So the instrument belongs on the summary:

```kotlin
val exceedanceOneSidedBound: Double?   // null where the proportion is not saturated
```

computed once, from `coupling.saturatedProportionBound` — the exact Clopper-Pearson limit at `x = n`, `p > (1 − c)^(1/n)`, whose large-`n` form is the rule of three.
Six record classes then pass `summary.exceedanceOneSidedBound` and nothing else changes.

## The second-order finding, which is the one worth keeping

`CH-0152` records that `T-148`'s statistic was *"computed correctly, emitted correctly, unit-tested, read, and misdescribed in a sentence written around it"*, and asks that a repair repair the sentence too.
That was applied here as a **measurement** rather than as an assumption: every string in all six remaining result files was scanned for *resolution*, *statistical power*, *standard error*, *binomial* and *sampling*, and **none of the six describes the symmetric error as a resolution**.
`T-148` is the only file whose prose asserted it.
The misdescription is therefore *rarer* than the degenerate statistic by a factor of seven — and the five **claims** are where it lives instead.
**A prose defect in a result file was repaired by re-reading result files; the same defect in a claim needed a different search, and nothing in the standing toolchain performs it.**
