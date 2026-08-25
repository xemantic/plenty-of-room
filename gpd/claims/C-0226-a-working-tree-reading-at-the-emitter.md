# C-0226 — a working-tree reading **does** have a legitimate use and **seven** of the corpus's thirteen files make it, so the fix is not the refusal; what is illegitimate is a reading of a count the deliverable prints **about itself**, because **a quantity that cannot be true at the moment it is written is not a quantity** — and the state it was reaching for becomes nameable **one commit later**, which is why the replacement is a different pinned measurement and not a renamed key

| | |
|---|---|
| **Task** | [`T-340`](../tasks/T-340-a-working-tree-reading-at-the-emitter.md) and [`T-339`](../tasks/T-339-the-prose-arm-promoted.md), both opened by [`C-0224`](C-0224-a-quoted-count-against-a-pinned-record.md) (`T-336`) |
| **Leaf** | `A8.2` (process) |
| **Verification type** | **logical** — **83** named self-tests in the census (was 66), **13** in the new emitter, **11** in `T-334`'s and **15** in `T-332`'s, a **43**-row mutation table at **0** survivors over a measured and subtracted green baseline (`CH-0237`), every arm run in a copy of the tree carrying **no `./.git`**, and every count derived from the committed artifacts rather than quoted |
| **Verdict** | **PASS on `P1`–`P11` and `Q1`–`Q7`.** Of `T-340`'s fourteen declared falsifiers **two fired and both on a count rather than on a finding**: `F1` on the leaf total, because a hand census over the working tree counted **floats** where the arms walk integers — **155** integers against **199** including floats, both now emitted; and `F1` again on the kind split, **7 / 2 / 4** against the Formulate's own corrected reading, which is the same number it declared. `F2`–`F14` did not fire. Of `T-339`'s nine, **`G1` fired as declared** — the prose arm was already green at `HEAD` — and `G2`–`G9` did not |
| **Maturity** | TRL 1–3 process artifact. **No physics changed, and no Kotlin under `src/` is touched.** Two committed result files are re-emitted, each at its own recorded `baselineRef`; **0** leaves move outside a working-tree block in either |
| **Provenance** | [`tools/T-340-emit-result.py`](../../tools/T-340-emit-result.py) (new; the population census, the declared kind table with a per-file evidence line, the declared reader-role table with a refusal, **13** self-tests), arm C and the per-block ref attribution in [`tools/T-336-pinned-count-census.py`](../../tools/T-336-pinned-count-census.py), ten new rows and two re-anchored ones in [`tools/T-336-mutation-test.py`](../../tools/T-336-mutation-test.py), the repairs in [`tools/T-334-emit-result.py`](../../tools/T-334-emit-result.py) and [`tools/T-332-emit-result.py`](../../tools/T-332-emit-result.py), one `Exec` task and one `dependsOn` entry in [`build.gradle.kts`](../../build.gradle.kts), result [`gpd/results/T-340-a-working-tree-reading-at-the-emitter.json`](../results/T-340-a-working-tree-reading-at-the-emitter.json) (`baselineRef` `91f9a48`, byte-identical across two runs), and the two re-emitted files [`gpd/results/T-332-fifteenth-answers-synthesis.json`](../results/T-332-fifteenth-answers-synthesis.json) and [`gpd/results/T-334-the-gate-census-by-reachability.json`](../results/T-334-the-gate-census-by-reachability.json) |
| **Conditions** | The corpus at **`91f9a48`** — the commit carrying this task's Formulate and Plan, **pinned rather than defaulted**, because a corpus-subject emitter defaulting to `HEAD` re-bases its own measurement between the draft and the emission (`CH-0246`). `tools/`, `build.gradle.kts`, two committed result files and this task's own artifacts only. **No `./gradlew` and no `tools/verify.sh` run**: a sibling agent held Kotlin studies in flight for the whole iteration, so every gate was run directly as Python and the build is the coordinator's |
| **Consumes** | [`C-0224`](C-0224-a-quoted-count-against-a-pinned-record.md) (the registry, both rows, and the tool this extends), [`C-0222`](C-0222-the-gate-census-by-reachability.md) (`T-334`'s reachability census and its repaired file), [`C-0220`](C-0220-fifteenth-answers-synthesis.md) (`T-332`'s repaired file), [`CH-0292`](../challenges/CH-0292-the-deliverable-quotes-the-unpinnable-half.md) and [`CH-0293`](../challenges/CH-0293-an-unpinned-block-inside-the-file-that-cites-the-rule.md) (the two records), [`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md) (a corpus-subject file must name its state), [`CH-0182`](../challenges/CH-0182-a-census-is-dated-by-its-premise-set.md) (a census destroys itself), [`C-0092`](C-0092-large-rotation-arm-branch.md) (a repair must leave the defect measurable), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate), [`C-0209`](C-0209-a-link-target-is-a-filename-whatever-it-names.md) (a gate that comes clean must say what it does not reach), [`C-0182`](C-0182-name-the-discharge.md) (declare, and refuse the undeclared), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (a mutation that fails nothing is the finding — construct the state), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (a mutation must replace a rule wholesale), [`C-0177`](C-0177-queue-status-vocabulary.md) (a gate that cannot fail), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (a mutation anchor is a reference into somebody else's source), [`C-0195`](C-0195-the-discriminating-input.md) (a fixture layout is a dependency declaration), [`C-0196`](C-0196-a-name-cannot-govern-a-token.md), [`C-0071`](C-0071-output-element-recommendation.md) (strike, never delete) |
| **Constrains** | every future result file recording a count this corpus derives on every run, and every future emitter that wants to say what its own pass will look like |
| **Raises** | [`CH-0295`](../challenges/CH-0295-a-task-declared-by-registering-is-unreachable.md), [`CH-0296`](../challenges/CH-0296-a-quantity-under-a-synonym-is-invisible.md). Opens `T-343` and `T-344` |

---

## The claim, in five lines

**The row demanded the crux be answered before any code, and it is answered BOTH ways, by a
measurement.** Over every committed result file, **13** carry a reading of an uncommitted working
tree, under **23** distinct key names and **155** integer leaves (**199** counting floats). **Seven
of the thirteen are legitimate**: the *after* half of a before/after measurement of a repair the
pass itself performs — `T-285`'s **21 → 0**, `T-281`'s **21 → 21**, `T-282`'s **9** against **8** at
its ref. The *before* is pinned; the *after* **cannot** be, because an emitter runs before its own
commit exists, and deleting it would delete the corpus's standard evidence that a repair worked.

**So the refusal cannot be on the key. It is on the QUANTITY.** The two offending records carry a
**declared registry quantity** — a count the deliverable prints *about itself* — so the same count
exists in two publishable readings and a prose writer must choose. `CH-0292` is what choosing wrong
looks like.

**A mechanical test settles it with no taste at all.** `gpd/README.md` requires a result file to be
reproducible from itself; re-run `tools/T-334-emit-result.py` at its own `baselineRef` and **55**
leaves move, **55 of 55** inside a working-tree block, its pinned half at **0**. A working-tree
block is exactly the part of a corpus-subject file that makes it irreproducible from itself.

**And the repair is available now and was not at emit time.** A pass's tree becomes a **commit** one
commit later, so the cure is not a renamed key a gate must refuse for ever but a **different, pinned
measurement**: the same census at `bee6b06` for `T-332` and at `bb678d2` for `T-334`. After it, arm
C's population is **empty by construction rather than by exemption**, which is what makes `T-339`
one constant.

**A quantity that cannot be true at the moment it is written is not a quantity.**

---

## 1. The measurement, at `91f9a48`

| | |
|---|---|
| result files recording a reading of an uncommitted working tree | **13** |
| distinct working-tree key names | **23** (of which **3** are PROSE: `workingTreeCaveat`, `whyTheWorkingTreeReadingDiffers`, `quietTree`) |
| numeric leaves under one | **155** integers, **199** including floats |
| of those, a **declared registry quantity** | **4**, in **2** files |
| kind **A** — the *after* half of a repair the pass performs | **7** files |
| kind **B** — a rival absolute reading of a registry quantity | **2** files |
| kind **C** — a corpus the pass does not change, pinnable at its own commit | **4** files |
| **non-prose consumers of any working-tree leaf** | **0**, out of **98** occurrences: **53** EMITTER-WRITES, **43** CENSUS-CLASSIFIES, **2** PROSE |
| arm C's population, before the repair and after | **4** leaves → **0** |
| the population itself, before and after | **13** files → **11** |

Every classification carries an **evidence line** in the result file, and an occurrence matching no
declared reader role **refuses** rather than defaulting to `CONSUMER` — because a consumer would
invert this claim's recommendation from *remove* to *rename*, and that is not a thing to infer from
a filename. A first draft's filename regular expression read **three** occurrences wrong:
`tools/T-289-column-history.py` **writes** its key at line 173, and a `.md` template under `tools/`
is prose.

## 2. The reproducibility test, which is the half that needs no judgement

| file | re-run at | leaves moved | inside a working-tree block | outside |
|---|---|---|---|---|
| `T-334-the-gate-census-by-reachability.json` | `d9a3522` | **55** | **55** | **0** |
| `T-327-the-resolution-of-the-flatness-census.json` | `86b3bbd` | **0** | — | — |
| `T-332-fifteenth-answers-synthesis.json` | `d7b7074` | **the emitter refused to run at all** | — | — |

`T-332`'s refusal is its own finding and it is structural: **1 of its 26 declared AFTER anchors no
longer occurs**, and it is the passage `CH-0292`'s own repair struck. A synthesis emitter that
asserts its own prose is still **live** in a deliverable is hostage to `C-0071`'s *strike, never
delete*, which guarantees a later pass will amend it. The anchor is **widened** to the bold run that
survives the strike — what it asserts is unchanged, only where it looks.

`T-327` is identical, and that is kind **C**: stable **by luck**, because nobody has re-emitted a
flatness result since. It is pinnable at its own commit and nothing here changes it.

## 3. The two records were NOT alike, and that is the argument

| | `T-332` | `T-334` |
|---|---|---|
| what the removed value was | a **hardcoded literal**, `{"challenges": 247, "claims": 214, "sum": 461}`, typed into the emitter | a **live derivation** at `Tree(None)` |
| does any `--ref` reproduce it? | **no** — and none refutes it either | yes, and differently every day |
| was it right? | its census reading occurs at **0** of 298 commits; its **checker** census, `18 / 21 / 12 / 51`, was **right** and is the same at `bee6b06` | **four of thirteen leaves were wrong at the moment they were committed** (`CH-0293`); its headline, **46**, was right at `bb678d2` |

**One was right and one was wrong and no reader could tell which, because neither named a state.**
Unpinnable is a defect independently of whether the value happens to be correct — which is the
sharpest form of the rule and it is measured rather than asserted.

## 4. What the repair produced

| file | removed | added | changed | removed outside a working-tree block |
|---|---|---|---|---|
| `T-334-the-gate-census-by-reachability.json` | **155** | 157 | **4** | **0** |
| `T-332-fifteenth-answers-synthesis.json` | **9** | 11 | **2** | **0** |

`T-334`'s four changed leaves are `armOneAtThreeRefs[2]`, whose `ref` was `null` and is now
`bb678d2`, and whose two counts go **12 → 13** — exactly what `CH-0293` says every committed state
from `bb678d2` onward reads. `theCensusMovesItsOwnAnswer` keeps its `CH-0182` finding with **both
terms pinned**: `44` at `d9a3522` against `46` at `bb678d2`. `T-332`'s two changed leaves are prose
findings that quoted the tree reading and now quote the commit; its counts there are
**249 / 215 / 464**.

Two named self-tests that **asserted the defect** — `T-334`'s *"the baseline reading is the ref's
and not the working tree's"*, whose second clause was `document["atThisPassesTree"]["ref"] is None`,
and `T-336`'s *"the prose arm is not gated until `T-339` flips one constant"* — are **inverted, not
struck**: a test asserting a repaired defect is not a record but a false assertion.

## 5. Arm C, and why it is scoped by the key

*No result file records a declared registry quantity under a working-tree key.* It is scoped by the
**key** and not by `CENSUS_MARKERS`, because
`gpd/results/T-327-the-resolution-of-the-flatness-census.json` carries a **173**-entry
`atThisPassesTree` block and **none** of the three markers — a family-scoped arm would have had a
measured hole on its first day. It needs **no `git`**, which is the only reason it can be wired at
all (`tools/snapshot.sh` excludes `./.git`).

The arm exposed two limitations of the registry it extends, both repaired here and both with named
tests: a pinned block may now name **its own `ref`**, and the re-derivation uses it rather than the
file's `baselineRef`; and a registry leaf is matched with the **state key stripped**, so one
quantity recorded at two pinned states is **two records**. Together those take the re-derivation
from **11** records to **15**, at **0** mismatches, and the pinned population from **44** to **65**.

## 6. `T-339`: the promotion, and the premise that had already been discharged

`PROSE_ARM_IS_GATED` is **one constant** and it is now `True`. The row said the arm was red at
`HEAD`; measured, it has been **green since `7ff9d07`**, where the coordinator applied `C-0224` §8's
four substitutions — so `G1` fired as declared, and its firing is a finding about the row.

`C-0177`'s hazard is the one that mattered: **a promoted gate that cannot fail is worse than a
printed one**, and this corpus shipped exactly that for thirty iterations. It is refused here by
**construction rather than by argument** — two named tests drive a **constructed red fixture**
through the gated path, one a deliverable quoting a figure no record pins and one a result file
recording a registry quantity at a tree, and `_gated_rows` reproduces `main`'s own composition
rather than restating it.

The other hazard is that a flip can make a mutation a **no-op**. Measured by comparing the harness's
killer sets before and after: **43 rows both sides, 0 survivors both sides, 0 rows whose killer
count shrank, 0 rows that disappeared.**

## 7. Falsifier verdicts

| | declared | verdict |
|---|---|---|
| `F1` | the population is not **13** files / **23** keys / **199** numeric leaves, or the kind split is not **7 / 2 / 4** | **FIRED on the leaf count, and on nothing else.** 13 files and 23 keys exactly; **155** integer leaves against **199** including floats — a hand census over the working tree counted floats where the arms walk integers, and **both are now emitted**. The split is **7 / 2 / 4** |
| `F2` | any working-tree leaf has a **non-prose** consumer | **did not fire** — **0** of **98** occurrences, from a declared table that **refuses** an unclassified path |
| `F3` | kind **A** is empty; the crux answer inverts | **did not fire** — **7** files |
| `F4` | a kind-**A** reading is pinnable after all | **did not fire** — the *after* half is the pass's own uncommitted repair, which no sha names until the pass commits |
| `F5` | re-emitting either record moves a leaf **outside** a working-tree block | **did not fire** — **0** and **0** |
| `F6` | the re-keyed `T-334` block does not read **13** at `CH-0293`'s four leaves, or `theCensusMovesItsOwnAnswer` loses its finding | **did not fire** — **13**, and the finding survives with both terms pinned |
| `F7` | the derived `T-332` counts at `bee6b06` are not **249 / 215 / 464** | **did not fire** |
| `F8` | arm C is not red on exactly **2** blocks before and **0** after, or fires on a kind-**A** record | **did not fire** — 4 leaves in 2 blocks, then 0; and a named test holds kind A open |
| `F9` | the arm needs `git`, or `--check` fails in a tree with no `./.git` | **did not fire** — `--check`, `--self-test` and `--prose --strict` all exit 0 in a `./.git`-free copy |
| `F10` | a new mutation fails no **named** test, or the baseline is not green | **did not fire** — 43 / 0 over a green subtracted baseline. **Two rows were ANCHOR failures on the first run**, because the repair of `pinned_values` orphaned two of the harness's own anchors — `C-0185`, and the harness's `count == 1` assertion is what said so |
| `F11` | `T-336 --check` / `--self-test`, `P-31 --check`, `T-334 --check` or `check-corpus-links.py` moves off 0 | **did not fire** — all 0. `P-31` reads **1** defect and it is a sibling's undeclared `T-337-mutation-test.py`, not this task's |
| `F12` | scoping the arm by `CENSUS_MARKERS` would have reached `T-327`'s block | **did not fire** — it carries none of the three markers and **173** entries |
| `F13` | a re-emission moves a number quoted in either deliverable, in a claim or in a challenge | **did not fire** — the moved values are the removed tree readings, whose only prose carriers are `CH-0292`, `CH-0293` and this claim, all of which quote them **in order to correct them** |
| `F14` | the `T-332` anchor repair trims what the emitter asserts rather than widening where it looks | **did not fire** — `"**Two hundred and forty-seven** challenges in"` becomes `"**Two hundred and forty-seven**"`, which occurs **once** in the working tree and **zero** times at the ref |
| `G1` | the prose arm is not already green at `HEAD` | **FIRED, and its firing is a finding about the row** — green at `HEAD`, `dfce9c1` and `7ff9d07` |
| `G2` | `--check` is not 0 at `HEAD` after the flip | **did not fire** |
| `G3` | `--check` needs `git` after the flip | **did not fire** |
| `G4` | the flip turns any mutation into a no-op | **did not fire** — 43 rows both sides, no killer count shrank |
| `G5` | the test pinning the constant is struck rather than inverted | **did not fire** |
| `G6` | a number of `T-336`'s result file moves other than the gating flag | **did not fire** — that file is not re-emitted; its `baselineRef` is `52a7bf3` and its counts are pinned there |
| `G7` | `tools/verify.sh --committed` is red for a reason this task introduced | **NOT RUN** — a sibling held studies in flight all iteration; the build is the coordinator's, and it is stated here rather than assumed |
| `G8` | the arm `T-340` ships is not red on exactly the two records before the repair | **did not fire** |
| `G9` | more than one constant has to change for the promotion | **did not fire** — one constant, plus the inversion of the test that pinned it |

## 8. Validity range, and what this does not settle

- **`G7` is not discharged.** No `./gradlew` and no `tools/verify.sh` ran. Every gate this task
  touches was run directly as Python, in the checkout **and** in a `./.git`-free copy, and the
  authoritative run is the coordinator's.
- **Arm C's population is a LOWER BOUND**, and `CH-0296` says by how much: the arm matches a
  registry quantity by its record leaf's **name**, so `sum` (which is `claimsAndChallenges`) and
  `distinct` (which is `gateCensusUnion`) are invisible to it. Both were removed anyway, because
  they sat in blocks it did see — by **adjacency**, which is exactly the reasoning this corpus keeps
  recording as unwanted.
- **Kinds A and C are legal and NOT reached**, printed on every run (`C-0209`). Kind C is pinnable
  at its own commit and nobody has done it; that is `T-343`.
- **The reader census excludes prose by DECLARATION.** A sentence about the defect is not a reader
  of it, and counting one would make every repair look load-bearing. The exclusion is stated in the
  result file rather than assumed.
- **This file is excluded from its own population by name.** Its own `armC/atTheWorkingTree` key
  matches the working-tree expression, so it enters the census the moment it exists and the count
  would differ between a first emission and a second — `CH-0182` for the tenth time, discharged by
  **naming** the exclusion rather than by renaming the key.
- **`T-334`'s and `T-332`'s emitters are not wired** and need `git` for every path; both now
  **refuse visibly on stderr** with a reduced arm count instead of crashing with a traceback
  (`C-0195`).
- **One wiring form is supported and nothing says so** — `CH-0295`. Writing the new `Exec` task as
  `val x by tasks.registering(Exec::class)` took `T-334`'s `armOne` invariant to `14` against `13`;
  `tasks.register<Exec>("x")`, the form all **61** other tasks use, is clean.
