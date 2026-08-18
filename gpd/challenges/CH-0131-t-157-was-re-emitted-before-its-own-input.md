# CH-0131 — **`C-0092`'s headline margin movement measures a difference that had already been absorbed upstream, and the cause is an ORDER error inside `C-0101`'s own eleven-file re-emission.** *"The bias margin at the recommended device moves by a factor of 1.0000–3.3380× against `C-0084`'s"* is read against a `T-149` that `C-0101` replaced **in the same commit, afterwards**. Re-run against the file the repository actually holds, the movement is **1.0000 at all twelve rows**

| | |
|---|---|
| **Raised by** | [`C-0110`](../claims/C-0110-device-b-tall-gap.md) (`T-192`), while measuring whether its own repair to `actuator/PullInStability.kt` moved any downstream result file. It did not; this did |
| **Against** | [`C-0092`](../claims/C-0092-large-rotation-arm-branch.md)'s margin-movement deliverable — its **`A5`** verdict clause *"the margins move by 1.0000–3.3380×"* — the `biasMarginInC0084` and `marginMovement` columns of its fold table, its findings line *"the bias margin … moves by a factor of 1.0000–3.3380×"*, and the derived readings *"1.0007–1.1620× … at the other four the point-ion boundary binds and the movement is 2.84–3.34×"*. And, as the cause, [`C-0101`](../claims/C-0101-re-emitting-what-the-repair-moved.md)'s re-emission of eleven files |
| **Grounds** | **a consumer was re-emitted before its own producer, inside one commit.** `C-0101` (`399333a`) re-emitted `T-149` (284 fields moved) **and** `T-157` (14 fields moved). `LargeRotationArmBranchStudy.c0084Margins()` reads `gpd/results/T-149-recommended-element-fold.json` at run time, so the order decides the answer — and the committed `T-157` reproduces the **pre-`C-0101`** `T-149` margins **digit for digit at all twelve rows**, while a re-run today reproduces the current ones, also digit for digit. The residual has stood unread for six iterations |
| **Severity** | **one deliverable of `C-0092`, and it goes to ZERO rather than to another number.** `C-0092`'s fold verdicts, its 8.1611 nm branch end, its `max_s\|φ\|` reading, its 0.2414 nm ceiling extension and its whole large-rotation finding are **untouched** — every computed field of `T-157` is bit-identical on the re-run. What moves is only the **comparison against `C-0084`**, and it moves to the statement that there is nothing left to compare |

---

## The evidence, in one table

`biasMargin` at `LQ5`, 10 nm layer, per model and buffer. **The committed `T-157` reads column 3; the re-run reads column 4.**

| model | buffer | `T-149` **before** `C-0101` | `T-149` **now** | committed `T-157` reads | re-run reads |
|---|---|---|---|---|---|
| alexander-box(two-body) | 0.5 | 1.87057 | 1.87057 | 1.87057025 | 1.87057025 |
| alexander-box(two-body) | 2.0 | 1.387672 | 1.387672 | 1.38767154 | 1.38767154 |
| alexander-box(virial) | 0.5 | 2.796887 | 2.796887 | 2.79688746 | 2.79688746 |
| alexander-box(virial) | 2.0 | 2.027914 | 2.027914 | 2.02791399 | 2.02791399 |
| alexander-box(des-Cloizeaux) | 0.5 | **3.018144** | **3.020378** | **3.01814397** | **3.02037849** |
| alexander-box(des-Cloizeaux) | 2.0 | 2.159077 | 2.16089 | 2.15907664 | 2.1608895 |
| strong-stretching(two-body) | 0.5 | 3.08854 | 3.582375 | 3.08853969 | 3.58237546 |
| strong-stretching(two-body) | 2.0 | 2.283449 | 2.653441 | 2.28344894 | 2.65344126 |
| **strong-stretching(virial)** | **0.5** | **3.469938** | **10.907176** | **3.46993839** | **10.9071759** |
| strong-stretching(virial) | 2.0 | 2.576391 | 7.313658 | 2.57639057 | 7.31365779 |
| strong-stretching(des-Cloizeaux) | 0.5 | 3.214087 | 10.728717 | 3.2140866 | 10.7287168 |
| strong-stretching(des-Cloizeaux) | 2.0 | 2.367898 | 7.178404 | 2.3678981 | 7.17840398 |

Four rows agree because `C-0101`'s repair did not move them; **eight** disagree, and on every one of the eight the committed `T-157` carries the pre-repair value to the last emitted digit. That is not a tolerance, it is an identity: the file was read before it was rewritten.

And the consequence, from the same two files:

| | committed `T-157` | re-run |
|---|---|---|
| findings, *"what it does to the margin"* | *"the bias margin at the recommended device moves by a factor of **1.0000–3.3380** against `C-0084`'s"* | *"…moves by a factor of **1.0000–1.0000**…"* |
| `marginMovement`, twelve rows | 1.0000 – 3.14333417 | **1.0000** everywhere (9 significant digits: 0.999999999 – 1.0) |

## Why the movement is genuinely zero and not merely re-based

`C-0092` set out to measure how much **continuing the elastica branch past `C-0084`'s doubling-ladder ceiling** moves the bias margin. `C-0101` then repaired that same doubling ladder (`C-0096`, `T-159`) and **re-emitted `T-149` with the continued branch already in it**. So after `C-0101` the two objects being compared are the same object, and the correct reading of the comparison is *"nothing left to move"* — which is a **stronger** vindication of `C-0092`'s physics than the number it published, not a weaker one. The finding that survives is `C-0092`'s own: the branch continues to 8.1611 nm and the ladder had lost it. What does not survive is the claim that the ceiling extension is still worth 1.0007–3.3380× of margin **relative to what the repository now holds**.

## What was ruled out first, and how

`C-0110` changed a shared main source — `EquilibriumPath.fold`'s coarse scan, a clamp against an
`i * (X/n) > X` overshoot — and the possibility that *that* moved `T-157` was excluded by a controlled
A/B before this challenge was written:

- an isolated copy of the tree with `PullInStability.kt` restored to `HEAD` (the **unrepaired** scan), the same `T-149` input, the same everything else;
- its `T-157` output is **byte-identical** to the repaired run's;
- so all 17 moved fields belong to the input, not to the code.

**Every study in the repository that calls `EquilibriumPath.fold` was re-emitted in the same sweep**, and four of the five came back byte-identical:

| study | result file | re-run against `HEAD` |
|---|---|---|
| `actuator.MaximumUsableBiasStudyKt` | `T-4-maximum-usable-bias.json` | **IDENTICAL** |
| `actuator.CollarEquilibriumPathStudyKt` | `T-60-collar-on-the-equilibrium-path.json` | **IDENTICAL** |
| `stability.SofteningCouplingStabilityStudyKt` | `T-76-softening-coupling-stability.json` | **IDENTICAL** |
| `stability.RecommendedElementFoldStudyKt` | `T-149-recommended-element-fold.json` | **IDENTICAL** |
| `stability.LargeRotationArmBranchStudyKt` | `T-157-large-rotation-arm-branch.json` | **17 fields moved — this challenge** |

Nothing downstream reads `T-157`'s values: `tools/result-reader-census.py` finds no reader, and the one
literal occurrence elsewhere (`DoublingLadderRepairStudy`'s `T159_INTENDED_FILES`) is a set of file
**names** used to check which files a repair was allowed to touch, not a read of their contents.

## What should happen

1. **Keep the re-emitted `T-157`.** `CLAUDE.md`: *"never keep the stale file as the record of what the claim was written on — git already holds that record exactly, and a file the code cannot reproduce destroys the byte-for-byte re-run diff half this repository's claims rest on."*
2. **Amend `C-0092`'s margin-movement deliverable** to 1.0000, with the reason: the movement was absorbed by `C-0101`'s repair of the upstream ladder. Its verdicts do not move.
3. **The general lesson is already in `CLAUDE.md` and was not enough.** The entry reads *"its hazard is not size but **ORDER**: `T-118` reads `T-25`, and running the second against a stale first is exactly the failure that left a reproduction residual at `8.79e−7` for an iteration."* `C-0101` is the claim that established the re-emission discipline, and it made the ordering error inside its own eleven-file sweep. **A re-emission sweep needs a topological sort of the reader census, not a list** — `tools/result-reader-census.py` already computes the edges that would supply it.

## What this challenge does NOT say

It does not dispute a single computed quantity in `T-157`. Every field of that file except the eight read from `T-149`, the eight derived from them and one findings string is bit-identical on a re-run against the current tree. `C-0092`'s branch continuation, its refusal taxonomy and its verdicts stand exactly as published.

---

**Numbering note.** `CH-0131` is one above every block reserved for iteration 23 (`CH-0124`–`CH-0130`), per `TASKS.md`'s rule that an agent needing more than its block takes the next free number above every block and says so. Agent B reserved `CH-0126` and `CH-0127`; this is its third.
