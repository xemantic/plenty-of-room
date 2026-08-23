# C-0195 — a mutation's discriminating input can be the COMMITTED CORPUS, and repairing the corpus expires it silently: **276 mutations over 13 harnesses, every one held open by a fixture, 0 corpus-dependent** — and the state `C-0192` §8 describes **existed at no committed commit of this repository**, because at `7f7957d` that mutation had **two** killers and `T-292` removed both

| | |
|---|---|
| **Task** | [`T-295`](../tasks/T-295-the-discriminating-input.md), opened by [`C-0192`](C-0192-the-column-repair.md) (`T-292`) §8 |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as a paired experiment over every mutation of every harness in `tools/`, 53 named self-tests, a 33-row mutation table at 0 survivors, and a three-state reconstruction of the instance the class is known to have |
| **Verdict** | **PASS on `F1`–`F8`, and the census is WIRED.** `tools/T-295-mutation-input-census.py --check` reads **276 mutations, 276 fixture-backed, 0 corpus-dependent, 0 survivors, 0 revived, 0 defects** and exits 0; it runs from `tools/verify.sh` |
| **Maturity** | TRL 1–3 process artifact. Nothing here is a measurement of the physics; it is a measurement of what makes this repository's mutation tables mean anything |
| **Provenance** | `tools/T-295-mutation-input-census.py` (the census, **53** named self-tests), `tools/T-295-mutation-test.py` (**33** mutations, **0** survivors, over a measured and subtracted baseline), `tools/T-295-emit-result.py`; one row added to `tools/P-31-harness-census.py`'s `HARNESSES` and one block to `tools/verify.sh`; result `gpd/results/T-295-mutation-input-census.json` |
| **Conditions** | Tools and documents only. No Kotlin source is touched, no result file other than this task's own is emitted, and **no physical number in the corpus moves** |
| **Consumes** | [`C-0192`](C-0192-the-column-repair.md) (`T-292` §8, which raised this), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (`P-31`, whose declared harness table this imports rather than copies), [`C-0183`](C-0183-residue-as-a-gate.md) (`T-283`, whose twelfth mutation is the instance), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (*construct the state*), [`C-0177`](C-0177-queue-status-vocabulary.md) (*read the return the process exits on*), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (*a mutation must replace a rule wholesale*), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*) |
| **Constrains** | every future mutation harness in `tools/`: a mutation whose only discriminator is committed corpus state now fails the build unless it is declared by name |
| **Raises** | [`CH-0250`](../challenges/CH-0250-the-survivor-had-two-causes.md), against [`C-0192`](C-0192-the-column-repair.md) §8 — the committed queue was not the only discriminator, and `T-292` removed the second one itself |

---

## The claim, in one line

`P-31` asks whether a harness's reference **into its subject** resolves;
this asks what input makes each mutation **observable**, because a mutation whose only discriminator is committed corpus state expires the moment that state is repaired —
and over all thirteen harnesses in `tools/`, all **276** mutations, the answer today is that **every one of them is held open by a fixture** and none by the corpus.

## 1. What `P-31` cannot see, and why the class is silent

`C-0192` §8 is the instance and it is worth restating precisely.
`tools/T-283-mutation-test.py`'s twelfth mutation flips the residue arm from a row's **leftmost** verdict to its **last**.
Repairing 21 queue rows took that harness from **0 survivors to 1**, on a predicate nobody had touched.

`tools/P-31-harness-census.py --check` reports **0 unresolved anchors** for that harness throughout, and it is right:
the anchor never moved.
What moved was the **input** that made the two readings different objects.

So the class is invisible to anchor resolution **by construction**, and the failure was loud in iteration 45 only because a harness happened to be re-run in the same hour.
`CLAUDE.md` records *a convention is not a mechanism* six times.

## 2. The method, and the cheap bound that chose it

The obvious route is **static** — parse each self-test suite, find its `check(name, expression)` calls, and ask whether each expression reaches a read of `TASKS.md`, `ANSWERS.md` or `gpd/`.
It is refused on three measurements, each of which cost one look and all three of which ran before any code was written:

- **the complete killer set is not in any harness's output.** Several harnesses print only the first **two** killers per row and truncate each to 110 characters. *Every killer is corpus-backed* is a statement about the whole set.
- **`CLAUDE.md` already prices the closure**: *a static call graph over FILES is not a conservative approximation, it is noise*. The reach here crosses eight tools and two import styles.
- **it answers a different question.** *Does this test read `TASKS.md`* is not *does this test NEED `TASKS.md`*, and only the second one expires.

What replaces it is a **paired experiment**.
Two copies of the tree: a **control**, faithful, and a **treatment** in which every committed artifact outside `tools/` and the build infrastructure is emptied — **1620** files at the ref this was emitted at, a count that moves with the corpus and is dated by `baselineRef`.
Every harness runs in both, **unmodified**, and its own printed per-mutation row is read in each.

The design rests on a fact this repository already established: every harness here **subtracts the named failures of an unmutated baseline** (`CH-0237`, `C-0185`).
So a named test that cannot run against an emptied corpus is *subtracted* in the treatment arm rather than counted — which is exactly the wanted behaviour, because a test that cannot discriminate on an emptied corpus cannot kill anything there.

| control | treatment | verdict | meaning |
|---|---|---|---|
| killed | killed | `FIXTURE` | some killer survives the corpus being emptied |
| killed | not killed | `CORPUS` | its only discriminator is committed state |
| not killed | — | `SURVIVOR` | the harness's own gate owns this one |
| not killed | killed | `REVIVED` | reported; nothing should produce it |

**Emptying is the maximal perturbation**, so a mutation still killed under it is killed by something that is not the corpus.
The residual risk is a **false negative** — a corpus-reading test that still discriminates on an empty corpus — and it is stated rather than argued away, and bounded by §4.

**Cost, measured before it was written**: thirteen harnesses at 0.1–35 s each, run twice, is about **four minutes**, which is cheaper than the static closure and answers the question directly.

## 3. The reading

`tools/T-295-mutation-input-census.py --check`, on this tree:

| harness | mutations | fixture-backed | corpus-dependent | other |
|---|---|---|---|---|
| `test-check-queue-vocabulary.py` | 17 | 17 | 0 | 0 |
| `P-30-mutation-test.py` | 24 | 24 | 0 | 0 |
| `T-281-mutation-test.py` | 24 | 24 | 0 | 0 |
| `T-283-mutation-test.py` | 12 | 12 | 0 | 0 |
| `T-289-mutation-test.py` | 16 | 16 | 0 | 0 |
| `T-292-mutation-test.py` | 21 | 21 | 0 | 0 |
| `T-295-mutation-test.py` | 33 | 33 | 0 | 0 |
| `T-234-mutation-test.py` | 57 | 57 | 0 | 0 |
| `T-280-mutation-test.py` | 23 | 23 | 0 | 0 |
| `T-278-mutation-test.py` | 8 | 8 | 0 | 0 |
| `T-225-mutation-test.py` | 22 | 22 | 0 | 0 |
| `T-249-mutation-test.py` | 11 | 11 | 0 | 0 |
| `T-250-mutation-test.py` | 8 | 8 | 0 | 0 |
| **total** | **276** | **276** | **0** | **0** |

**0 defects, exit 0.**
The registry of declared corpus dependencies, `CORPUS_DEPENDENT_BY_DESIGN`, is therefore **empty** — and it is tested in **both** directions on synthetic readings, because a declaration that has stopped being true is how a registry rots (`C-0182`).

Two properties of that table are worth separating from the zero.

**The control arm reproduces 0 survivors for every harness**, which is the second premise of the design and the check that the two arms differ in nothing but the corpus.
It is what the build already asserts in place, and it is asserted here on a copy.

**And the row counts are cross-checked rather than trusted.** The number of rows parsed is compared against each harness's own summary line, the two arms are required to print the same number of rows, and the labels are required to agree position by position. Any of the three failing is a reported **refusal**, never a clean row: a census that silently drops rows is worse than one that refuses.

## 4. The reconstruction — and the census's first run convicted its own author

`F4` demands that the instrument report the instance it was written for.
Three states, each **run**, each giving a different verdict:

| state | control killers | killers with the corpus emptied | verdict |
|---|---|---|---|
| `C-0192`'s constructed fixture kept, a repaired queue | 1 | 1 | **FIXTURE** |
| that fixture removed, a repaired queue | 0 | 0 | **SURVIVOR** |
| that fixture removed, a **pre-repair** queue | 1 | 0 | **CORPUS** |

The third is the demonstration, and the same reading is taken against the **real** pre-repair queue at `git show 7f7957d:TASKS.md` — pinned, because `CLAUDE.md`'s cure for a self-test that reads a mutable artifact is to pin the ref.

**The three queues in the constructed states are CONSTRUCTED and not read**, and that is not a stylistic choice.
The first version read the live `TASKS.md`, and the first run of the new harness *inside* `--check` reported **two of its own rows as survivors** — because inside this census's own treatment arm the live queue is empty and the scratch copy has no `.git`.
That is the defect under study, one level up, in the instrument itself, found by the baseline subtraction that exists for it.
The repair is `C-0161`'s: construct the state.

## 5. What the census says about the corpus's own history, and it is a challenge

Run against `git archive 7f7957d` — the last commit **before** `T-292` — the same mutation reads:

| the mutation | killers, faithful copy | killers, corpus emptied | verdict |
|---|---|---|---|
| *the residue reads EVERY verdict of a row rather than the leftmost* | **2** | **1** | **FIXTURE** |

At the last pre-repair commit the mutation had **two** killers, and the one that survives the queue being emptied is named:

```
SELFTEST FAIL: T-289 a miscolumned verdict does NOT fail the gate — the arm is advisory, because the predicate
```

— a **constructed** two-verdict row, fed through `_gate_on` and asserted to exit **0**.
`T-292` promoted that arm, which required **inverting** the assertion to *a miscolumned verdict FAILS the gate*;
and an assertion that an exit code is **1** cannot detect an *additional* defect, because exit codes saturate.

So the corpus-only state `C-0192` §8 describes **existed at no committed state of this repository**.
It was created inside `T-292`'s own working tree by the two halves of that task acting together: the queue repair removed one killer and the gate promotion removed the other.
[`CH-0250`](../challenges/CH-0250-the-survivor-had-two-causes.md) asks for the account to be annotated and for the general rule to be recorded, and upholds everything `C-0192` computed.

## 6. What is delegated and what is declared

**The harness list is `P-31`'s own table, imported.**
`P-31` already gates *every mutation harness in `tools/` is declared*, so this census inherits that guarantee instead of keeping a second list — `CLAUDE.md`'s *a duplicated rule is invisible to a mutation test of either copy*.
`harness_names` deliberately does **not** read the tree, asserted as a named test, so that a sibling's in-flight harness cannot make this census refuse a file `P-31` has already refused; the plain run prints it as **not censused** with the reason.

**Declared here** and nowhere else: the build-infrastructure carve-out (`tools/`, `gradle/`, the five wrapper files), the eight row shapes the thirteen harnesses print, and the empty exemption registry.
Everything else — which files are harnesses, how many mutations each has, and which of them are corpus-dependent — is derived by running.

## 7. Where it is wired, and why not in Gradle

`tools/verify.sh`, beside the other corpus gates, and by `P-31`'s own criterion.
`P-31`'s Gradle comment says it *"reads only `tools/`, `build.gradle.kts` and `tools/verify.sh`, so it wires in here"*;
this census makes two copies of the **whole tree** and runs every harness in both, so it belongs with the gates that read the corpus.
It costs about four minutes against a twenty-minute suite.

One consequence is stated rather than hidden: a `verify.sh` snapshot carries no `.git`, so the **pinned historical** check prints a visible skip there — to stderr, so that `--self-test > /dev/null` cannot swallow it — and the three constructed states carry the demonstration in that run.

## 8. Acceptance

| predicate | verdict | evidence |
|---|---|---|
| `F1` every mutation classified, by running | **PASS** | 276 mutations over 13 harnesses, 0 refusals |
| `F2` the harness list is `P-31`'s, imported | **PASS** | equality asserted as a named test; `harness_names` does not read the tree |
| `F3` the census refuses rather than under-counting | **PASS** | stated count, row counts and row labels all cross-checked; each a named test and each mutated |
| `F4` checked against the instance it was written for | **PASS** | three constructed states, three verdicts; and the same reading on the real pre-repair queue at the pinned `7f7957d` |
| `F5` the gate reads 0, or the residue is named | **PASS** | 0 defects; 0 declared exemptions |
| `F6` every mutation of the census fails a NAMED test | **PASS** | 33 mutations, 0 survivors, over a subtracted baseline |
| `F7` declared in `P-31` and wired | **PASS** | a row of `P-31`'s `HARNESSES`, `P-31 --check` reads 0 unresolved for it; run from `tools/verify.sh` |
| `F8` the result file is dated | **PASS** | `baselineRef` records the resolved sha |

## 9. Validity range

- **This is a statement about a WORKING TREE, not about git history.** `.git` is excluded from **both** arms, so a named test that needs git cannot discriminate in either — and a mutation killed only by such a test would read `SURVIVOR`, which the gate reports. Measured: **0** such survivors across the twelve pre-existing harnesses, so the exclusion costs nothing today.
- **The false negative is real and one-sided.** A corpus-reading test that still discriminates on an *emptied* corpus is classified `FIXTURE`. Emptying is the maximal perturbation, so this is the conservative direction, and §4 bounds it on the one instance the class is known to have.
- **`0 corpus-dependent` is a reading of this tree at this ref**, and it is dated by `baselineRef`. It is not a theorem about the harnesses; a mutation added tomorrow can be corpus-dependent, which is what makes this a gate rather than a survey.
- **The eight row shapes are a declared reading of thirteen harnesses' output.** A harness that changes its output makes the census **refuse** rather than under-count, which is `F3`; but the refusal is then a defect of this census, not of that harness, and it has to be repaired here.
- **The exemption registry is empty**, so its behaviour is exercised only on synthetic readings. Both directions are tested; neither has yet been tested on a real row.
