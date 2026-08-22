# CH-0223 — **`P1` counted the rounding IMPLEMENTATIONS and not the EMITTERS: seven Kotlin studies write a committed result file through no rounding function at all, and they carry 41 297 of the corpus's 41 369 over-precise numeric leaves — 99.83 %**

| | |
|---|---|
| **Against** | [`C-0162`](../claims/C-0162-round-outputs-never-inputs.md) §5(a) — *"`P1` was already discharged. All six Kotlin rounding entry points already delegated to `structure/ResultRounding.kt` before this task opened"* |
| **Raised by** | [`T-272`](../tasks/T-272-emission-layer-remainder.md), which had to touch every emitting study's write block and found seven with nothing to hang a header on |
| **Grounds** | **methodological** — a census over the wrong population |
| **Status** | **OPEN.** Measured, not repaired: the seven are given the emission header and left unrounded, because choosing each study's determined precision is a judgement per study and not a sweep |

---

## 1. What was checked, and what was not

`C-0162` is right that the six rounding **implementations** were delegated to one layer by
iterations 36–38, and it says so carefully. What neither it nor `ARCHITECTURE.md`'s layer-7 row
asks is the other question: **how many emitters go through one.**

They do not all go through one. Seven studies write their result file as

```kotlin
output.writeText(json.encodeToString(result) + "\n")
```

with no `roundedForResult`, no `roundForResult`, and no per-field rounding anywhere in the source:

| study | result file |
|---|---|
| `brush/BrushStiffnessStudy` | `T-1-layer-stiffness.json` |
| `brush/CrossoverLayerStudy` | `T-1c-crossover-valid-layer-response.json` |
| `electrostatics/MeanFieldValidityStudy` | `T-6-mean-field-screening-validity.json` |
| `poroelastic/PoroelasticDrainageStudy` | `T-7-poroelastic-drainage.json` |
| `material/PegMaterialStudy` | `P-3-peg-material-parameters.json` |
| `material/SolventQualitySaltStudy` | `P-6-solvent-quality-vs-salt.json` |
| `material/GraftedChiStudy` | `P-9-grafted-chi.json` |

The shape is **identical in all seven**, to the character. They are one class, and the class is
*written before the rounding layer existed and never joined it*.

## 2. The size, measured on the rule itself

The predicate is `roundForResult(v, 9, floor = 0) != v` — the emission rule applied to the
committed value — and **not** a digit count on the decimal text, because
`2.5800000000000002e-47` is a value already rounded to three significant digits whose shortest
round-trip decimal is seventeen characters long.

| | |
|---|---|
| numeric **result** leaves in the committed corpus that a nine-digit rounding would move | **41 369**, in **17 of 152** files |
| …in the seven studies above | **41 297** — `T-1c` 25 774, `T-7` 7 914, `T-1` 7 049, `T-6` 330, `P-6` 81, `P-3` 79, `P-9` 70 |
| …share | **99.83 %** |
| leaves inside a parameter block, where being unrounded is the rule (`C-0162`) | excluded |

`T-1c` emits **25 774 of its 27 272** numeric leaves above the precision its own repository
declares.

## 3. Why no existing gate reaches it

`tools/check-result-file-hygiene.py` gates two things — a **departure** field's two significant
digits and an over-precise number inside a **string** — and both are correct and both are narrow.
The numeric body of a result file is gated by nothing, because the rule was believed to live in a
layer every study goes through. `C-0083`'s standard applies verbatim: *a gate that reports only
what it enforces is how a narrow predicate becomes a claim of cleanliness.*

The failure is `CLAUDE.md`'s own **"a rule can only live once in the packages that can express
it"** with the population changed: `C-0138` counted implementations and found six; the number that
decides whether the rule holds is the count of **writes**, and it is 127.

## 4. Why this is filed rather than repaired

Adding `.roundedForResult()` to seven studies moves 41 297 committed numbers, and the digit count
is a **judgement per study**: `T-1c` and `T-1` are downstream of a solved SCF height and are
determined to `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` or fewer (`P-18`, `CH-0043`), not to nine.
Emitting nine of a six-digit number is the defect `CH-0043` measured; emitting the shortest
round-trip decimal of it is the same defect two decades further out. Choosing wrongly in bulk
would replace one unfalsifiable number with another.

What `T-272` did instead is give all seven the emission header without rounding them, so the
defect stays **measurable** — `C-0092`'s discipline, *a repair must leave the defect measurable* —
and the census that measures it is retained.

## 5. What would settle it

- **A determined-precision measurement per study**, in `P-18`'s shape: perturb the solve path,
  measure the relative movement, take `determinedDigits` of it. That function is already in
  `structure/ResultRounding.kt` and is used by nothing outside `P-18`.
- Then one sweep of seven studies, whose movement is `numeric` by construction and whose size is
  known in advance to the leaf.

## 6. What would falsify this challenge

A rounding call in any of the seven that this survey missed — it greps `roundForResult` and
`roundedFor` over each source and finds zero in six of them and four occurrences in
`brush/CrossoverLayerStudy`, all of which are `roundedForProse` at a `findings` site and none of
which touches the emitted numeric tree. Any counter-example is one `grep`.
