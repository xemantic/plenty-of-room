# C-0105 — The note is re-worded off the ceiling `C-0049` withdrew, and **no verdict moves, because the clause-correct reading is 3.33× STRICTER, not looser**: 40 pN/nm is `1.2 × (100 pN / 3 nm)` and the same construction at §3's desired clause is **12 pN/nm**, against which the softest of `T-79`'s 26 placing rows is **13.3×** past. And the miss does not rest on a declared number at all — **26 of 26** are also past `C-0006`'s per-path secant ceiling of **45 pN/nm**, which is cited rather than declared. **The defect was in a file neither `C-0101` nor the `T-169` row names**: the string was in `anchoring/TwoSpringElasticaStudy.kt` / `T-79`, not in `synthesis/DesiredStrokeReachStudy.kt` / `T-108` — which is where `C-0049` was *derived* and is the one place in the tree that already read the ceiling with its stroke

| | |
|---|---|
| **Task** | [`T-169`](../tasks/T-169.md), raised by [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) §4 |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (which clause a declared number belongs to — one division) **+ in-silico** (the elastica study re-emitted and all 34 rows re-read under both readings *and* under the derived ceiling) |
| **Verdict** | **PASS on the first branch of the acceptance: re-word, do not restore.** `C-0049`'s finding is upheld and is arithmetic, not judgement — `40/(100/3) = 1.2` exactly, so the ceiling is a multiple of the *mandate* and carries the mandate's stroke inside it. The re-worded note names the tolerance read at the row's own clause (**12.00 pN/nm**) and, separately, `C-0006`'s per-path secant ceiling (**45.00 pN/nm**), so the two independent reasons are no longer conflated into one withdrawn number. **`P1` 0 of 34 rows quote the withdrawn phrase; `P2` 26 of 26 placing rows carry the same verdict under both readings; `P3` 26 of 26 are also past the CITED ceiling.** |
| **Maturity** | **TRL 1–3.** No physics is derived here. One emitted string, one library call, and an audit of 34 rows. |
| **Provenance** | `gpd/results/T-169-withdrawn-ceiling-note.json`, produced by `synthesis.WithdrawnCeilingNoteStudyKt` (**new**), which **reads** the re-emitted `gpd/results/T-79-two-spring-elastica.json` rather than recomputing it; `synthesis/CouplingCeiling.kt` gained `ClauseCeilingReading`, `clauseCeilingReading` and `pastClauseCeilingNote` (**new**, `C-0049`'s own file); `anchoring/TwoSpringElasticaStudy.kt` edited in **six** places (the verdict string, three findings, one sensitivity predicate and the `conditions` block, plus a stroke-aware overload of its usable-stroke search) and re-emitted; **10 gate-named tests in `src/test/kotlin/synthesis/WithdrawnCeilingNoteTest.kt`**, red-checked before implementation (7 of 9 unresolved-reference failures); `tools/verify.sh` run on an isolated tree with a sibling agent's mid-TDD `src/test/kotlin/anchoring/CrossoverPhaseSelectionTest.kt` dropped by `--drop-file` |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; 45 load paths (`C-0015`) and 15 (`C-0041`); `C-0006`/`CH-0029`'s 10 pN unzip allowable |
| **Consumes** | [`C-0049`](C-0049-compliance-ceiling-stroke.md) (`declaredComplianceCeiling`, `perPathSecantCeiling`, `DECLARED_CEILING_FACTOR` — **re-run as a library, not quoted**), [`C-0039`](C-0039-two-spring-elastica.md) (the catalogue whose prose is corrected), [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (which flagged it), [`C-0023`](C-0023-two-sided-coupling.md) (the declared tolerance), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) |
| **Constrains** | `gpd/results/T-79-two-spring-elastica.json`, re-emitted. **No number in it moves** — the diff is confined to the 26 verdict strings, three findings, the `conditions` keys and three new fields. |

---

## The claim, in one line

**The reason printed beside a verdict is part of the verdict, and this one was 3.33× too generous — in the direction that would have made the element look better than it is.**

---

## 1. The cheap bound, which is one division and is the whole answer

`C-0023` declares its ceiling; nothing derives it. Divide it by the thing it was declared against:

&nbsp;&nbsp;&nbsp;&nbsp;**`40 / (100 pN / 3 nm) = 1.2`, exactly.**

| §3 clause | mandate | declared tolerance `1.2 ×` | per-path secant ceiling, 45 paths |
|---|---|---|---|
| **acceptable — 100 pN at 3 nm** | 33.3333 pN/nm | **40.0000** | 150.00 |
| **desired — 100 pN at ~10 nm** | 10.0000 pN/nm | **12.0000** | **45.00** |

Both readings are re-derived here from `synthesis/CouplingCeiling.kt` — `C-0049`'s own library, whose signature already takes the stroke as an argument — rather than transcribed. They differ by **exactly 10/3**, and the declared one **falls** with the stroke because the mandate does.

> **So the correction is a TIGHTENING.** `F3` was declared for exactly this: if the clause-correct reading had been looser, re-wording would have been a relaxation and would have needed an argument. It is 3.33× stricter, and it did not fire.

## 2. The audit, row by row

`T-79`'s catalogue is 34 placements; **26** reach the desired stroke and are therefore judged there.

| | withdrawn reading, 40 pN/nm | clause-correct reading, 12 pN/nm | per-path secant ceiling, 45 pN/nm |
|---|---|---|---|
| rows past it | **26 of 26** | **26 of 26** | **26 of 26** |
| softest placing row | 159.16 pN/nm tangent, **3.98×** | 159.16 pN/nm tangent, **13.3×** | 57.98 pN/nm secant, **1.29×** |

> **`P2` is met at 26 of 26 and `F1` did not fire.** The verdict cannot move: the softest row in the whole catalogue is an order of magnitude past the *stricter* of the two declared readings.

**`P3` is the part that matters more than either.** Every placing row is *also* past `C-0006`/`CH-0029`'s per-path allowable expressed as a secant ceiling, `n·allowable/s = 45 pN/nm` — which is **cited**, not declared, and which **tightens as the stroke grows** where the declared tolerance merely moves. So the catalogue's refusal of the desired stroke never depended on `C-0023`'s tolerance at all, and the re-worded note now says so, in the two clauses it actually rests on. This is `C-0049`'s own *"dropping the declared ceiling moves the miss from 6.6× on a declared tolerance to 1.55× and 4.66× on a CITED allowable"*, reproduced at the row level: **1.29×** at 45 paths on the softest row.

## 3. What changed in the tree

| file | change |
|---|---|
| `synthesis/CouplingCeiling.kt` | `ClauseCeilingReading`, `clauseCeilingReading(...)`, `pastClauseCeilingNote(...)` — new, in `C-0049`'s own file, so the correction lives where the finding does |
| `anchoring/TwoSpringElasticaStudy.kt` | the placement verdict; `theCeiling`, `theUsableStroke` and `whatSurvives`; the sensitivity sweep's `clearsDesiredStrokeInsideCeiling` predicate, which read 40 at 10 nm too; the `conditions` block, now three keyed entries instead of one |
| `gpd/results/T-79-two-spring-elastica.json` | re-emitted. **No existing number moves**; four fields are added |
| `synthesis/WithdrawnCeilingNoteStudy.kt` | new, emits `gpd/results/T-169-withdrawn-ceiling-note.json` |

Two additions are worth naming because they are *numbers* and not prose:

- **`usableStrokeInsideClauseCeiling` = 3.224 nm** against the published `usableStrokeInsideCeiling` = **3.877 nm**. The first reads the tolerance at each stroke's own clause (`1.2 × 100/s`, falling); the second holds it at the placement clause's 40 pN/nm. **Both clear §3's acceptable 3 nm and neither clears its desired 10 nm**, so `C-0039`'s verdict is untouched and its published 3.877 is deliberately left where it is: it is quoted by `C-0039`, `C-0046`, `CH-0053` and `T-99`, and it is a well-defined quantity — the stroke inside the tolerance **read at the clause it is owed at**. The clause-by-clause reading is emitted *beside* it rather than replacing it.
- **`insideDeclaredCeilingAtDesiredClause`** and **`insidePerPathSecantCeilingAtDesired`**, both `false` at all 26 placing rows, so a reader can check `P2` and `P3` from the file instead of trusting this claim.

## 4. What surprised us, and it is a bookkeeping finding

**The defect was not in the file either record names.** `C-0101` §4 says *"the new text of row 1's sibling"* and files `T-169` against the note; the `T-169` row's acceptance predicate names **`synthesis/DesiredStrokeReachStudy.kt`**. The string *"places, but past the 40 pN/nm ceiling at the desired stroke"* is in **`anchoring/TwoSpringElasticaStudy.kt`**, and it is emitted into **`gpd/results/T-79-two-spring-elastica.json`**, 26 times.

`T-108` never carried it. `T-108` is `T-107`'s study — it is where `C-0049` was **derived**, and its own verdict string already reads *"The same construction at §3's desired clause is 12 pN/nm, not 40"*. So the one study in the tree that had the ceiling right was the one the task pointed the correction at, and the study that had inherited the number was named nowhere.

> **A defect's *location* is a number like any other, and `CLAUDE.md`'s rule — grep it out of the file before quoting it — applies to filenames.** Two records agreed with each other and both were wrong, which is exactly the failure mode `CLAUDE.md` records for a subagent report and a claim: agreement between two documents is not verification against the tree.

## 5. The five verification gates

Executed as **10 gate-named tests** in `src/test/kotlin/synthesis/WithdrawnCeilingNoteTest.kt`, plus three in-study falsifiers and one in-study `check`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | all three readings carry `pN/nm` and the stroke is carried beside them; a zero stroke, a zero placement stroke and a zero path count each throw | **PASS** |
| **2 — limiting cases** | at the placement stroke the two declared readings are **the same number**, 40.0000, by construction; a row inside every ceiling is described as *inside* and not as *past*; the note contains no *"40 pN/nm ceiling at the desired stroke"* | **PASS** |
| **3 — symmetry and conservation** | the declared ceiling is exactly `1.2 F/s` at five strokes; the declared and the per-path ceilings are **both** `1/s`, so their ratio is stroke-free — the identity `C-0049`'s argument rests on | **PASS** |
| **4 — numerical convergence** | not a discretised quantity; instead, the two readings are asserted to **agree on the verdict** at all 26 emitted tangents, and to differ by exactly 10/3 | **PASS** |
| **5 — literature and upstream** | `C-0049`'s own six rows re-derived from the library — 40 / 12 declared, 150 / 45 per-path at 45 paths, 50 / 15 at 15 | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | re-wording the note moves a verdict | **NO** | the softest placing row is **13.3×** past the stricter reading; 26 of 26 agree |
| **F2** | the withdrawn phrase survives anywhere in the re-emitted file | **NO** | **0** occurrences |
| **F3** | the clause-correct reading is *looser*, so the correction is a relaxation | **NO** | it is **3.33× stricter** |

## Validity range

- **TRL 1–3.** Nothing here is measured, and the number being corrected was never a measurement either: `C-0023`'s 40 pN/nm is a **declared** design tolerance, which is most of why `C-0049` could withdraw it by inspection.
- **`usableStrokeInsideCeiling` is deliberately unchanged at 3.877 nm.** It is a well-posed quantity read at the clause the tolerance is owed at, it is quoted by four records, and a test (`FlexureCountHingeTradeTest`) asserts it. The clause-by-clause companion, 3.224 nm, is emitted beside it and is the honest reading of *"the stroke this element delivers inside a tolerance that falls with the stroke"*.
- **The per-path secant ceiling is read at 45 paths.** At `C-0041`'s buildable **15** it is 15 pN/nm at the desired stroke, and the same rows are further past it; that is not swept here because `T-79`'s catalogue is a 45-path catalogue.
- **This claim corrects a reason, not a result.** `C-0039`'s and `C-0050`'s verdicts stand, on the ground `C-0049` gives them.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `C-0023`'s declared ceiling and its factor | 40 pN/nm, 1.2 | **CITED, DECLARED** (`T-23` `P2`) — **re-derived here** as `1.2 × mandate` and asserted at four `(force, stroke)` pairs |
| `C-0006`/`CH-0029`'s per-path unzip allowable | 10 pN | **CITED** |
| §3's 100 pN, 3 nm and ~10 nm | — | **CITED**, the problem definition |
| `T-79`'s 34 placement rows | — | **READ FROM ITS RESULT FILE**, after re-emission, not recomputed |

Everything else — the two clause readings, the three-way row audit, the 10/3 identity, the two new emitted booleans and `usableStrokeInsideClauseCeiling` — is **derived here in code**.

## Still open — named, not answered

1. **`C-0101` §4 and the `T-169` row both misname the file.** Corrected in `TASKS.md`'s row; `C-0101` is a coordinator's claim from the previous iteration and is **not** edited here, because its verdict and its physics are unaffected and the correction belongs in the record that found it.
2. **Nothing sweeps whether other studies read 40 pN/nm at a stroke it is not owed at.** `grep -rn "40" over the tree is not a search; a search would be for `COMPLIANT_CEILING`-like constants compared against a quantity evaluated at a stroke other than 3 nm. Two were found and fixed in `T-79`; there may be more.
3. **`C-0041`'s 15-path reading** of the same catalogue is not emitted.
