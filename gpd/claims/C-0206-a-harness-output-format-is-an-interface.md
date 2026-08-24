# C-0206 — a mutation harness's PRINTED OUTPUT is an interface, and it is now DECLARED per harness: **six shapes over fourteen running harnesses, three of which stated no row count at all** — and the third repair of this class had already made the fourth collision

| | |
|---|---|
| **Task** | [`T-306`](../tasks/T-306-a-harness-output-format-is-an-interface.md), raised by the coordinator at iteration 47's assembled `HEAD`; carries the open half of `T-301` and all of `T-305` |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as 54 + 78 named self-tests over the two censuses, a 27-row mutation table at **0 survivors** over a measured and subtracted baseline, one **pinned historical** assertion against the committed past, and the full paired census re-run |
| **Verdict** | **PASS on `F1`–`F8`.** `tools/P-31-harness-census.py --check` reads **17 harnesses, 308 anchors, 33 symbols, 0 unresolved, wired 17 of 17**; `tools/T-295-mutation-input-census.py --check` reads **330 mutations over 15 harnesses, 330 fixture-backed, 0 corpus-dependent, 0 survivors, 0 revived, 0 defects** |
| **Maturity** | TRL 1–3 process artifact. It measures what makes this repository's mutation tables mean anything, not the physics |
| **Provenance** | `tools/P-31-harness-census.py` (the declaration, **54** named self-tests), `tools/T-295-mutation-input-census.py` (the reading, **78**), `tools/T-306-mutation-test.py` (**new**, 27 mutations, 0 survivors), `tools/T-299-mutation-test.py` (**moved** from `gpd/data/T-299-mutation/mutate.py`), row counts added to `tools/T-225-`, `tools/T-249-` and `tools/T-250-mutation-test.py`, baseline locals named in `tools/T-298-` and `tools/T-299-mutation-test.py`, two re-anchored rows in `tools/T-295-mutation-test.py`, row formats repaired in `tools/T-298-` and `tools/T-306-mutation-test.py`, three Gradle tasks |
| **Conditions** | `tools/` and `build.gradle.kts` only. No Kotlin source is touched, **no result file is emitted or re-emitted**, and **no physical number in the corpus moves** |
| **Consumes** | [`C-0185`](C-0185-orphaned-mutation-anchors.md) (`P-31`, whose table now carries the declaration), [`C-0195`](C-0195-the-discriminating-input.md) (`T-295`, the census that reads it), [`C-0182`](C-0182-name-the-discharge.md) (*a registry checked in both directions, and a report needs a third state*), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (*a declared list is a dated object*; *a mutation must replace a rule wholesale*), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (*a mutation that fails nothing is the finding — construct the state*), [`C-0177`](C-0177-queue-status-vocabulary.md) (*read the return the process exits on*), [`C-0179`](C-0179-the-debt-line-as-a-ratio.md) (*two guards for one constraint make a mutation of either a no-op*) |
| **Constrains** | every mutation harness in `tools/`: it must declare the row shapes it prints, print a row count of its own, and — where it takes an argument — be declared `BY-HAND` and print a lower-case `usage:` line |
| **Raises** | [`CH-0260`](../challenges/CH-0260-the-third-repair-made-the-fourth-collision.md), against [`C-0203`](C-0203-a-challenges-status-row-is-the-authority.md) / `T-298` — its repair of the third collision **made** the fourth, and the census's own report hid it. `CH-0261` is reserved and **released unused** |

---

## The claim, in one line

The eight regular expressions that read a harness's rows were a **guess at a format nobody declared**, and the guess collided three times in two iterations;
the shapes are now **declared per harness in `P-31`'s own table** and each harness is parsed with **its own alone**,
the row count is **mandatory** so that no harness can under-count silently,
and the `BY HAND` state is **declared as well as derived** with a disagreement in either direction a defect.

## 1. The measurement that chose the deliverable

The queue offered two shapes and asked for a measurement first.
Taken at `342d7ad` — iteration 47's assembled `HEAD` — by running the fifteen declared harnesses once each and tallying which pattern matched each printed line:

| | |
|---|---|
| harnesses declared | 15 |
| harnesses that run bare | 14; `T-297-mutation-test.py` prints its usage |
| **distinct printed row shapes** | **6** |
| `killed-by` | 4 harnesses, 71 rows |
| `killed-pair` | 4 harnesses, 76 rows |
| `kind-row` | 2 harnesses, 94 rows |
| `arrow` | 2 harnesses, 19 rows |
| `of-row` | 1 harness, 22 rows |
| `killed-n` | 1 harness, 21 rows |
| declared row patterns | 8; the other two (`survived`, `survives`) fire on **0** rows, because the corpus has 0 survivors |
| declared summary patterns | 3, **all three load-bearing** |
| **harnesses stating no row count** | **3** — `T-225` (22 rows), `T-249` (11), `T-250` (8) |

**The choice was a declared shape per harness, and the measurement is what decided it.**

- The six shapes are **not gratuitous**: `kind-row` carries `C-0176`'s `NARROW`/`WIDEN` direction and `killed-pair` carries two suites separately, so a single required shape is a **ten-harness** output rewrite that loses two fields or prints every row twice.
- `C-0176`'s *a declared list is a dated object* **is already discharged for this table**: `P-31.discovers_harnesses` fails the build on an undeclared `tools/` harness, so a harness written tomorrow cannot exist without a row, and the row must now name its shape. A declaration inside a gated registry is not a census that stopped.
- What was missing was **teeth**, and the measurement found two live holes a declaration alone does not close.

## 2. The two holes, both live at `342d7ad`

**Cross-shape acceptance.** `parse_rows` tried all eight patterns against every line, first match wins, so a harness that changed to *another* harness's shape was read silently under different semantics. Now each harness is parsed with its declared shapes alone, and a line readable by some *other* shape is **counted** and named in the refusal.

**A silent under-count.** At the three harnesses stating no count, `reconcile` cannot see a partial shape change: both arms drop the same rows, the lengths agree, `stated is None`, and no refusal is raised. Run at `342d7ad`, `reconcile(rows[:-1], rows[:-1], None)` returns `[]` — no refusal. The count is now **mandatory**, and the three harnesses gained the `# N mutation(s), M survivor(s)` line eleven others already print. Their counts are **22, 11 and 8**, which are the row counts measured above.

## 3. `T-301`, the open half: `wired_in` is a USE and not a MENTION

`wired_in` was `basename in build_text or basename in verify_text`.
Measured at the pinned `342d7ad`, that was **wrong about the corpus**: `tools/T-283-mutation-test.py` was reported *wired in `tools/verify.sh`* off **one sentence of one comment** there, and `tools/verify.sh` runs no mutation harness at all.
`build.gradle.kts` had accreted the defensive form the predicate forces — a comment saying, in as many words, that a basename was being **withheld** so that the explanation would not read as the wiring.

The predicate is now three derivations, none of them a substring of the whole file:

- Kotlin comments (`//` and `/* */`) are blanked **length-preservingly**, then the basename must fall inside a **balanced `commandLine(...)` span**. Comment-stripping alone is not enough: `description = "Runs tools/h.py, …"` is prose on an executable line.
- Shell `#` comments are stripped outside quotes, then the basename must be the line's **command word**. `echo "skip with: tools/h.py"` names a harness in an argument and runs nothing.
- Both are asserted **against the committed past**: a pinned named test reads `git show 342d7ad:tools/verify.sh`, asserts the basename **is** in it, and asserts the repaired predicate returns `[]`.

**The residue the queue flagged — `T-298-mutation-test.py` reading `base: NO` — is a false reading of a derivation, not a gap.**
That harness measures a baseline and **refuses outright** on a red one, which is stronger than subtracting; it simply named no identifier carrying the word, and `measures_baseline` is deliberately blind to comments and strings.
Repaired by naming the local `baseline_code` / `baseline_failures` — **the harness moved, not the derivation**, which is `T-306`'s own third collision met one file across. `T-299` had the same defect (`base_failures`) and took the same repair.

## 4. `T-305`: the Kotlin harness, and whether the third state composes

`gpd/data/T-299-mutation/mutate.py` is now `tools/T-299-mutation-test.py`, declared, wired, and **executable**.

**It did not compose, and the reason is worth the sentence.** `T-295`'s derived third state keys on `^usage:`, **case-sensitively**; that harness refused with `sys.exit(__doc__)` and its docstring says `Usage:`. Dropped into `tools/` unchanged it would have read as a `REFUSED` — a defect — for a reason that is a capital letter.
Repaired in the **harness**, which prints a lower-case `usage:` line to stderr before exiting: widening the census's pattern to `(?i)` would have made any harness whose prose says *usage:* excuse itself from being censused, and that is the direction that flatters.

Two further things the row needed and the table did not have:

- **a fifth adapter shape**, `id_file_old_new_what` — a Kotlin harness names what each mutation *breaks* as a fifth field, where the Python harnesses have three or four;
- **the `BY-HAND` sentinel**, because one of its mutations is one Gradle `test` run.

**The wiring is real and is out of `:test`.** `testRasterTurnTetherMutations` and `testCommonModeMutations` are registered `Exec` tasks taking `-PmutationSnapshot=<dir>`; `./gradlew test --dry-run` lists neither and lists `testHarnessOutputContractMutations`. `wired: 17 of 17`, with the two by-hand harnesses reported as a **third state** in `P-31`'s own output rather than as unwired.

## 5. The mutation table — 19 rows, 0 survivors, and the first run found five

`tools/T-306-mutation-test.py` mutates **both** halves of the contract and runs **both** suites for every row, so a rule only the other tool notices is still held open. Baseline measured and subtracted (`CH-0237`).

Two things had to be subtracted or the table would have been a table of nothing:

- **A collateral killer.** Both subjects are themselves the subjects of mutation harnesses whose anchors are literal spans of their source, so **any** text change to either orphans an anchor and `P-31`'s own resolution test fails. Subtracted **by name** (`EXPECTED_COLLATERAL`) and documented. Unsubtracted, the first run read 19 killed and **5 of those rows were killed by that and by nothing else** — `C-0177`'s *full and empty* reached from the subject's side.
- **A crash is not a named test.** A mutation that makes a suite stop exits 1 with a traceback, which is indistinguishable from a clean run by exit code. The harness now requires each suite's own `# N self-test(s)` completion line and reports an unfinished suite as a **survivor**.

With both in place the first honest run read **5 survivors**, every one a finding:

| survivor | why it survived | the constructed fixture (`C-0161`) |
|---|---|---|
| the `//` comment is no longer blanked | the existing fixture's comment held no `commandLine(`, so there was no span to find either way | a **commented-out** `commandLine("$projectDir/tools/h.py")` |
| the `/* */` block comment is no longer blanked | the same | a whole wiring block left in the file |
| the shell `#` comment is no longer stripped | the fixture wrote `# tools/h.py`, whose first word is `#` at any stripping | `#tools/h.py --check`, where the `#` abuts the path |
| `declared_row_shapes` ignores which harness is asked | the only test compared the census against the **lookup**, and both moved together | the lookup asserted against the **table**, row by row |
| the stated count is admitted again | the mutation made the census **crash**, and a crash is not a named test | the mutation rewritten as the pre-repair rule, **wholesale** |

**And one more, in a sibling harness.** Making the count mandatory gave `reconcile` two guards that both refuse an empty reading, so `tools/T-295-mutation-test.py`'s row *"an empty control reading is not a refusal"* went from killed to **SURVIVED** — `C-0179`'s *two guards for one constraint make a mutation of either a no-op*, introduced by this task and caught by an existing harness within the hour. Repaired by reading that named test with a **stated count of zero**, so only the empty-rows guard can be what refuses.

## 5b. THE FOURTH COLLISION, found by the coordinator at the assembled `HEAD` — and it is the strongest argument for this deliverable

`tools/verify.sh --committed` at `342d7ad` was **RED, with exactly one defect**, and the defect was in the census:

```
REFUSED   T-298-mutation-test.py
          row labels drift between the two arms: 'the adjudication reader ignores strikes ' against 'the adjudication reader ignores strikes '
# 293 mutation(s) over 14 harness(es); … 1 defect(s)
```

Iteration 47 repaired collision (3) by **moving the harness** onto the `killed-by` shape —
the right direction, on the right ground.
The row it moved to prints its killers **after the name on the same line**,
and `killed-by`'s second group is `.*`,
so the parsed **label** is `name + padding + killers`;
the killers differ once the census empties the corpus, the arms disagree, and the harness is refused.
**The third repair made the fourth collision, in the same act.**

Two consequences, both landed as findings rather than as a bug fix:

- **Ten mutations, uncensused, printed as a clean row of zeros.** `0` in a *count* column reads exactly like a harness with nothing to measure — `C-0203`'s own *a countless row read as `0 named tests failed`* argument, one level up. A refused harness now prints **`REFUSED`** and `-`, which is `C-0182`'s third state met in a table.
- **The report hid the difference it was reporting.** Both labels were truncated to forty characters and are identical for their first forty. `drift_refusal` now names the character position they first differ at and prints the two **tails**.

**And the repair had to catch it at AUTHORING time, not fix it.**
`label_refusals` requires a parsed label to be a **prefix** of one of the harness's own mutation names —
which `P-31`'s adapter already reads out of that harness's table, so nothing new is declared.
A prefix, because a harness pads its name into a column and may truncate it there;
what a prefix cannot tolerate is anything printed **after** the name, which is exactly the defect.
**It runs on the control arm alone**, so it fires without the two arms ever having to disagree.

Measured over the whole roster before the repair: 4 of the 11 harnesses whose adapter supplies names carried labels that are not names —
`T-234` (28 of 71) and `T-280` (10 of 23) by **truncation**, which a prefix admits,
and `T-298` (10 of 10) and this task's own new harness (19 of 19) by **appending the killers**, which it does not.
Both of the latter now print their killers on continuation lines,
and `T-298-mutation-test.py` reads `10  10  0  0`.

**Why the class survives at all**: every one of the four collisions went out **green in its author's own run**,
because no agent's own run is `tools/verify.sh --committed` at the assembled `HEAD`.
That is not an argument for a more forgiving parser. It is the argument for a rule about what a row may carry,
checkable by the author, in one arm, before the commit.

## 6. Falsifiers

| | verdict |
|---|---|
| **F1** every harness declares its shapes and is parsed with those alone | **PASS** — `rowShapes` on every census row; a foreign shape is a named refusal |
| **F2** the declaration is checked in both directions | **PASS** — an undeclared printed shape and a declared shape that never prints are both refusals; a **survivor** shape is exempt because it is contingent, and the exemption is **derived** from `ROW_SHAPES`'s own `zero` kind |
| **F3** a row count is mandatory | **PASS** — `reconcile` refuses on `stated is None`; the three harnesses that stated none now state 22, 11 and 8 |
| **F4** `BY HAND` declared as well as derived, both directions | **PASS** — `by_hand_refusals` and `treat_as_by_hand`, four named tests, three mutations |
| **F5** `wired_in` distinguishes a use from a mention | **PASS**, and the live reading moved: `T-283-mutation-test.py` no longer reads as wired in `tools/verify.sh`, asserted against the pinned `342d7ad` |
| **F6** the Kotlin harness moved, declared, wired, and the composition **stated** | **PASS** — and it did **not** compose: `Usage:` against `^usage:`, repaired in the harness |
| **F7** a mutation table over every predicate changed, both directions, subtracted baseline | **PASS** — 27 rows, 0 survivors, 5 real findings on the first honest run |
| **F8** both gates read 0 | **PASS** — `P-31 --check` 0 unresolved; `T-295 --check` 0 defects over 330 mutations |

## 7. Validity range

- **The declaration is a declaration, and it is only as good as its gate.** What stops it becoming a census that stopped is `P-31.discovers_harnesses`, which fails the build on an undeclared `tools/*mutation-test.py` — *not* anything in this task. A harness added outside `tools/` is invisible to both censuses, exactly as `T-299` was for an iteration.
- **`survived`/`survives` are declared and unexercised.** The corpus has 0 survivors, so those two shapes are asserted by the census's own fixtures and by nothing in the tree. The first harness to survive a mutation is the first real test of them.
- **The collateral subtraction is a judgement.** `EXPECTED_COLLATERAL` names one test that fires for any text change to either subject. It is correct today because both subjects carry other harnesses' anchors; if that stops being true the subtraction hides a real killer, and nothing checks it.
- **No result file.** This census's output is `tools/P-31-harness-census.py --json`, live. A static JSON of it would be a second copy of a declared table, and `CH-0246` records what running the consumers of a corpus-subject result file does: it **overwrites the record instead of checking it**. The *before* measurement in §1 is reproducible from `git archive 342d7ad` and the harness runs, which is stated here rather than frozen.
- **Nothing physical moves.** No `src/`, no `gpd/results/`, no claim's number.
