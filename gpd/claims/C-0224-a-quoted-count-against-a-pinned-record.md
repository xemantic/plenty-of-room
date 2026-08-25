# C-0224 — a gate on a self-describing count cannot compare against a **moving** thing, and **having nothing stable to compare against is why such a gate degenerates into parsing numerals**. Three emitters already write `(quantity, value, resolvedRef)` triples nobody had ever read back; **11 of 11 re-derive at their own ref, 0 mismatches**, and against them `ANSWERS.md`'s live `247 / 214 / 461` is pinned by **nothing** and occurs at **0 of 298** commits — while every figure of the block `C-0222` wrote is pinned to a sha and correct

| | |
|---|---|
| **Task** | [`T-336`](../tasks/T-336-a-quoted-count-against-a-pinned-record.md), opened by [`C-0222`](C-0222-the-gate-census-by-reachability.md) (`T-334`) §8 |
| **Leaf** | `A8.2` (process) |
| **Verification type** | **logical** — 66 named self-tests in the census, 10 in its emitter, a 33-row mutation table at **0 survivors** over a measured and subtracted green baseline (`CH-0237`), and a false-positive rate measured over the corpus's **own history** rather than at a `HEAD` somebody has just repaired. `tools/T-295-mutation-input-census.py --check` reads **448 mutations over 19 harnesses, 448 fixture-backed, 0 corpus-dependent, 0 survivors, 0 defects**, this task's 33 among them and all fixture-backed |
| **Verdict** | **PASS on `P4`–`P8`.** Of the eleven declared falsifiers **five fired**, and only one of the five is a defect in the work: `F9` and `F11` were **declared to fire** and did, exactly as declared; `F1` and `F2` fired **on a count and not on a finding**, both in the favourable direction (the shipped predicate reaches **11** anchored figures where the Formulate probe reached 9, and the pinned-record population is **44** where the probe hand-enumerated 20); `F3` fired **on its form** and is satisfied on its substance — all five declared values are reported, three as membership failures and two in the printed residue, and *"11 tokens"* is not a quantity the tool emits. `F4`, `F5`, `F6`, `F7`, `F8` and `F10` did not fire |
| **Maturity** | TRL 1–3 process artifact. **No physics changed.** No Kotlin under `src/` is touched except one `ResultInputs` handle; no existing result file is emitted or re-emitted; no physical number in the corpus moves |
| **Provenance** | [`tools/T-336-pinned-count-census.py`](../../tools/T-336-pinned-count-census.py) (new; the registry, the two gated arms, the printed prose residue, the git-dependent re-derivation arm, **66** named self-tests), [`tools/T-336-mutation-test.py`](../../tools/T-336-mutation-test.py) (**33** mutations, **0** survivors), [`tools/T-336-emit-result.py`](../../tools/T-336-emit-result.py) (**10** self-tests), result [`gpd/results/T-336-a-quoted-count-against-a-pinned-record.json`](../results/T-336-a-quoted-count-against-a-pinned-record.json) (`baselineRef` `52a7bf3`, byte-identical across two runs), one row in [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py)'s `HARNESSES` table, three `Exec` tasks plus three `dependsOn` entries in [`build.gradle.kts`](../../build.gradle.kts), one handle in [`src/main/kotlin/structure/ResultInputs.kt`](../../src/main/kotlin/structure/ResultInputs.kt) |
| **Conditions** | The corpus at **`52a7bf3`** — `HEAD` as this task's Formulate began, **pinned rather than defaulted**, because a corpus-subject emitter defaulting to `HEAD` re-bases its own measurement between the draft and the emission (`CH-0246`, which `C-0222` hit within one task). `tools/`, `build.gradle.kts`, one `ResultInputs` handle and this task's own artifacts only. **No `./gradlew` and no `tools/verify.sh` run**: a sibling agent held studies in flight, so every gate was run directly as Python and the build is the coordinator's |
| **Consumes** | [`C-0222`](C-0222-the-gate-census-by-reachability.md) (the row, the refusal, and `T-334`'s reachability census, which this tool imports), [`C-0220`](C-0220-fifteenth-answers-synthesis.md) and [`C-0210`](C-0210-fourteenth-answers-synthesis.md) (the two passes whose prose and records disagree), [`CH-0182`](../challenges/CH-0182-a-census-is-dated-by-its-premise-set.md) (a census destroys itself), [`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md) (a corpus-subject file must record its state), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate), [`C-0209`](C-0209-a-link-target-is-a-filename-whatever-it-names.md) (a gate that comes clean must say what it does not reach), [`C-0129`](C-0129-result-file-hygiene.md) (wire what can be clean, print the residue), [`C-0177`](C-0177-queue-status-vocabulary.md) (a gate that cannot fail), [`C-0182`](C-0182-name-the-discharge.md) (declare, and refuse the undeclared), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (a mutation that fails nothing is the finding — construct the state), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (a mutation must replace a rule wholesale), [`C-0196`](C-0196-a-name-cannot-govern-a-token.md) (a name cannot govern a token), [`C-0195`](C-0195-the-discriminating-input.md) (a fixture layout is a dependency declaration), [`C-0071`](C-0071-output-element-recommendation.md) (strike, never delete), [`C-0202`](C-0202-a-length-is-not-a-provenance.md) (sweep a window to a plateau) |
| **Constrains** | every future statement, in either deliverable, of a count this corpus derives on every run; and every future emitter that records one |
| **Raises** | [`CH-0292`](../challenges/CH-0292-the-deliverable-quotes-the-unpinnable-half.md), [`CH-0293`](../challenges/CH-0293-an-unpinned-block-inside-the-file-that-cites-the-rule.md). Opens `T-339` and `T-340` |

---

## The claim, in four lines

**`C-0222` refused a gate on a numeral, and the refusal turns on the COMPARAND rather than on the
numeral.** A gate comparing prose against a live derivation at `HEAD` is unsatisfiable by
construction for a census of the corpus that contains the census (`CH-0182`) — the deliverable
says so itself, *"its own finished tree reads 248 / 215"* — and a gate that can never come clean
is not a gate. **Having nothing stable to compare against is why such a gate degenerates into
parsing numerals: the pattern-matching is the symptom and the missing fixed point is the disease.**

**The stable thing was already being written and nobody had read it back.** Four committed result
files carry **44** pinned records; **11** of them map to a declared quantity, and **11 of 11
re-derive at their own recorded ref, 0 mismatches**. A sha does not move when the corpus grows, so
the equality is *permanent*; and the comparison *prose against committed JSON* needs **no `git`**,
which is the only reason it can be wired — `tools/snapshot.sh` excludes `./.git`.

**Measured against it, the defect is a MEMBERSHIP failure and not a staleness.** `ANSWERS.md` line
1385 prints `247 / 214 / 461`; its own pass's file records `246 / 213 / 459` at `d7b7074` **and**
`247 / 214 / 461` under `workingTreeBeforeThisClaimsOwnFiles`, at no state. **The deliverable quotes
the one of the pair nothing can pin**, and `(247, 214)` and `(248, 215)` occur at **0** and **0** of
the repository's **298** commits.

**And the block `C-0222` itself wrote is the control that proves the discipline works**: every one
of its figures is pinned to a sha, and every one re-derives.

---

## 1. Why this is not the gate that was refused

| axis | the gate `C-0222` refused | this one |
|---|---|---|
| **anchor** | a numeral pattern — unbounded surface: spelled forms, three counts in a sentence, sums, struck history | a **declared registry entry**, `(quantity, deriving tool, derivation anchors, unit)`, which **refuses** an undeclared name rather than defaulting it. Prose is read only to **locate** |
| **comparand** | a live derivation at `HEAD` | a **committed JSON leaf** at that file's own resolved `baselineRef` |
| **can it come clean?** | **no, by construction** (`CH-0182`) | **yes, permanently** — a sha-pinned value never moves |
| **predicate on the figure** | *does this numeral equal today's value* | *is this a value some machine record **pins*** — a **membership** test |
| **object class** | prose against a running program | **two committed artifacts in the same tree, sharing no code** — the class `C-0222` itself shipped as its arm 1 |
| **`git`** | required | **not required**, so it can be wired at all |

The distinction is not a rephrasing, and it is the reusable half of this task: **before building a
gate on a self-describing number, find the fixed point it is allowed to be compared against. If
there is none, the gate you will end up writing is a numeral parser, whatever you intended.**

## 2. The registry, and its third state

Five quantities are declared, each with the tool that derives it **on every default `tools/verify.sh`
run** — checked against `T-334`'s own union, so a quantity cannot be declared against a tool nothing
runs — the derivations a prose figure must stand beside, and the unit phrase it must be followed by.

**A tool anchor without a unit is a declaration defect**, and that rule was found by a fixture rather
than reasoned: one tool prints two of the registry's counts, so an anchor on the tool alone attributes
every nearby number to both. `tools/check-corpus-identifiers.py` is exactly that case in the corpus.

An **undeclared quantity refuses** (`C-0182`), and so does a **state-shaped record key** in neither
vocabulary. The refusal is deliberately narrow: a key naming no state at all is pinned by its file's
own `baselineRef`, which is what `gpd/README.md` means by a result file being reproducible from
itself. Where the refusal bites is the one place `C-0182`'s *absence read as an answer* actually
does — a **new** unpinned key invented next pass will be state-shaped, because that is what such a
key is for.

**`UNPINNED_KEYS` is a census of what the corpus already writes**, and it has three kinds: a reading
of the pass's own tree; a reading attributed to another pass **in words rather than in a sha**
(`T-332` records four counts `atC0210sOwnRef` and carries no sha for it); and a figure **quoted from
a predecessor in order to correct it**, retained because `C-0092` requires a repair to leave the
defect measurable.

## 3. The numbers, at `52a7bf3`

| | |
|---|---|
| declared quantities | **5** |
| census-family result files | **4** |
| pinned records | **44** |
| unpinned records | **37** |
| pinned records mapping to a declared quantity, re-derived at their own ref | **11**, **0** mismatches |
| gated-arm defects (`--check`) | **0** |
| anchored live prose figures | **11** (8 in `ANSWERS.md`, 3 in `DECISIONS-FOR-NDI.md`) |
| of those, pinned by nothing | **4** |
| numerals on a flagged line carrying no anchor (printed, not gated) | **4** |
| commits carrying `(247, 214)` / `(248, 215)`, of **298** | **0** / **0** |

## 4. The false-positive rate, measured over history rather than at `HEAD`

`C-0209`'s standard. Run over **103** revisions of the two deliverables, the predicate produces
**85** hits over **22** distinct `(document, quantity, value, sense)` triples, audited exhaustively
by hand: **0 false positives**. Every one is a genuine self-describing count of its own quantity,
including the eleven successive spelled headlines the row has carried since iteration 4 — *"Nine
challenges in `gpd/challenges/`"* through *"Twenty-nine"*.

Four narrowings were **measured and kept** rather than preferred, each having produced a false
positive first: a digit inside an identifier is not a figure (`C-0196`; `295` was being read out of
`tools/T-295-mutation-input-census.py`); a link **target** is blanked and its label kept; a
**directory** anchors only the reverse sense, because a directory reports nothing; and the figure
must sit within **15** characters of the report verb, not 40.

**The prose window was swept to a plateau** (`C-0202`): 40 / 60 / 80 / 100 / 120 / 150 / 200 / 300
gives 3 / 3 / 4 / 4 / 4 / 4 / 4 / 4, flat from **80**, and **120** is inside it.

## 5. The mutation table, and fifteen survivors that were the finding

**33 mutations, 0 survivors** over a green subtracted baseline. Eleven rows restore a defect an
earlier draft of this very tool carried, which is the point of keeping them.

**The first run read 15 survivors and every one was a fixture that could not discriminate**
(`C-0161`) — not a missing test but a fixture with nowhere for the rule to matter. Constructing
the states cost twelve new named tests and **found three real defects in the subject**:

| found by | what it was |
|---|---|
| *a unit discriminates two quantities one tool prints* | a **tool anchor with no unit** attributes every nearby number to its quantity. Repaired in the subject **and** promoted to a rule: arm 2 now refuses such a declaration |
| *a weak verb does not attribute a nearby number* | the figure was searched **40** characters past the verb, so `reports T-295 widgets, and is a gate where 606` attributed the `606`. Tightened to 15 — and the **unit** search had to be separated from it, or every `reports **461** claims and challenges exist` is silently dropped |
| *an `atRef` nested inside a working-tree block* | `classify` scanned for `PINNED_KEYS` over the **whole** path first, so an `atRef` inside an `atThisPassesTree` block read as **pinned** — the one direction this gate must not fail in. Now the **first** state key on the path wins |

Two further defects were caught by named tests on their first run, and both are `CLAUDE.md`
entries met rather than avoided: **`_NUMBER` is an alternation, so `A|B` + `X` binds as `A | BX`**
and silently dropped the digit branch; and the **trailing guard** was written as the symmetric
mirror `(?![\w.\-])`, which **refuses every number at the end of a sentence** — the third recorded
instance of that exact trap, and this corpus's figures sit at the ends of sentences.

## 5a. Two ways to obey the rule, and why the second had to exist

The first draft's predicate was *membership in the set of values some record pins*, and applied to
`DECISIONS-FOR-NDI.md` it produced a substitution that would have **struck a historical figure out
of the paragraph whose whole subject is that figure's drift** — `C-0071`'s *strike, never delete*
turned against the record it protects.

`CLAUDE.md`'s rule is *quote it with the state it was read at*, and there are **two** ways to obey
it: point at a record that pins the value, or **name the state in the sentence**. So a figure is
also clean when its own sentence carries a resolvable-shaped sha. The split between the arms falls
out of the same constraint as everything else here: the **gated** arm checks that a sha is present
and well shaped, because resolving it needs `git`; whether it re-derives there is `--rederive`.

Three mutations hold it open, and the sentence boundary is load-bearing — a sha *elsewhere on the
line* does not pin a figure, and a six-character hex word is not a sha.

## 6. Falsifier verdicts

| | declared | verdict |
|---|---|---|
| `F1` | the anchored population is not **9**, or any hit is a false positive, or the historical rate is not 0 | **FIRED on the count, favourably** — **11**, because the shipped anchor set is a **superset** of the Formulate probe's (it carries `ls gpd/claims/C-*.md`, which the probe did not). **0** false positives at `HEAD` and **0** over 103 revisions |
| `F2` | the pinned records are not **20** in **4** files, or any fails to re-derive | **FIRED on the count only** — **44** records in **4** files, of which **11** map to a declared quantity; the probe's 20 was a broader hand enumeration of `T-334` leaves. **0 mismatches**, which is the half that carries the finding |
| `F3` | the unpinned prose figures are not exactly `247`, `214`, `461`, `248`, `215` over **11 tokens** | **FIRED on its form.** All five values are reported — `247 / 214 / 461` as membership failures and `248 / 215` in the printed residue — and *"tokens"* is not a quantity the tool emits; it reports per `(line, value)`. The substitution covers all five |
| `F4` | `(247, 214)` or `(248, 215)` occurs at any commit | **did not fire** — **0** and **0** of **298**, exhaustive |
| `F5` | `T-334`'s `atThisPassesTree` does not disagree with every committed state from `bb678d2`, or not at **4** of **13** leaves | **did not fire** — **4** of **13**, identically at `bb678d2`, `f52416c` and `52a7bf3` |
| `F6` | `--check` is not 0, or an arm cannot be made to fail by a named test, or an arm needs `git` | **did not fire** — 0 defects, three arms each held open by named tests, and no `git` on any gated path |
| `F7` | a mutation fails no named test, the baseline is not empty, or a mutation is corpus-dependent | **did not fire** — 33 / 0 over a green baseline, and `tools/T-295-mutation-input-census.py --check` reads **448 fixture-backed, 0 corpus-dependent, 0 defects** |
| `F8` | `P-31 --check` or `T-334 --check` moves off 0, or a harness's row count changes | **did not fire** — `P-31`: **0 unresolved, wired 33 of 33**; `T-334`: **0 defects, 13 unreachable = 13 BY-HAND** |
| `F9` | wiring does not move `T-334`'s union from 46 | **FIRED, as declared and to the declared value** — **46 → 48**, attributed exactly: the union at `52a7bf3` is 46 and this task's two tools are the whole of the difference. The working tree at the end of the iteration reads **51**, the other three being a sibling's `T-327` — which is the same `CH-0246` hazard one level out, and the reason the emitted figure is taken at a pinned ref and not at the tree |
| `F10` | the result file is not byte-identical across two runs at a fixed ref | **did not fire** |
| `F11` | the prose arm, run `--strict`, exits 0 | **FIRED, as declared, and its firing is the deliverable** — it exits 1 on four membership failures |

## 7. Validity range, and what this does not settle

- **The prose arm is NOT wired build-failing.** It is red at `52a7bf3` and this task may not edit
  `ANSWERS.md`. It is printed on every `--check` run and gates only under `--prose --strict` —
  `C-0129`'s idiom. `T-339` flips **one constant**, `PROSE_ARM_IS_GATED`, once the substitution
  lands; a promotion that needs a rewrite is a promotion nobody performs.
- **The registry is a statement about the tree it is read at.** Run at `7f7957d`, `71d126e`,
  `d7b7074` or `d9a3522` the gate reports **2** arm-2 defects, because `tools/T-334-gate-census.py`
  did not exist there and two quantities declare it as their deriver. That is correct behaviour and
  not a historical failure: a quantity cannot have been *derived on every run* by a tool nothing ran.
  It is clean at `bb678d2` and at `52a7bf3`.
- **The re-derivation arm cannot run under `tools/verify.sh`.** `tools/snapshot.sh` excludes
  `./.git`; the arm prints a **visible `stderr`** skip and is run directly in the checkout.
- **Four numerals on the flagged line carry no declared anchor** and are printed, not gated. Two of
  them (`248`, `215`) are part of the defect; two (`184`, `212`) are correct historical readings.
  A predicate that reached them would have to anchor on a subject word, which was measured and
  refused.
- **`gateCensusUnion` reaches nothing in today's prose**, because `DECISIONS-FOR-NDI.md`'s figures
  for it are followed by a **sha** rather than by a unit phrase — which is the *correct* way to
  write them and is why they have not drifted. The quantity is declared, its deriver is checked,
  and the prose arm reports it as reaching nothing rather than as clean.
- **Nothing here re-derives a physical number**, and `T-334`'s own conclusions are untouched:
  `CH-0293` is against one unpinned block of its result file and upholds the claim entire.

## 8. The hand-off

`ANSWERS.md` line 1385 carries a figure this claim's author may not edit. The substitution is
reported to the coordinator with its exact text, and it is a **sentence** change and not a number
change: **the corrected figure is itself pinned**, to `T-332`'s own recorded `baselineRef`
`d7b7074`, so it re-derives forever and the next pass has a fixed point to quote rather than a tree
to read. A substitution that swapped one unpinnable number for another would repeat the defect.

**It is verified rather than predicted.** All four substitutions — three on `ANSWERS.md` line 1385
and one clause on `DECISIONS-FOR-NDI.md` line 1748 — were applied to a **scratch copy** of the two
deliverables, each `FIND` string matching exactly once, and the gate re-run there reports
**`0` prose figures pinned by nothing, `0` unreached, exit `0`**. So `T-339` is exactly what it is
described as: one flip of `PROSE_ARM_IS_GATED`, with nothing else owed.

The fourth is the instructive one. Under the first draft's predicate its only repair would have
been to **strike** a historical figure out of the paragraph whose whole subject is that figure's
drift — `C-0071` turned against the record it protects. It is a one-clause pin instead, *"returns
**ten** at `05562ea`"*, and `05562ea` is checked: `tools/T-334-gate-census.py --ref 05562ea` reads
a naming predicate of 10 there.
