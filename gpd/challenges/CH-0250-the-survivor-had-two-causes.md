# CH-0250 — the committed queue was not the ONLY discriminator: at the last pre-repair commit that mutation had **two** killers, and `T-292` removed the second one itself by **inverting** a gate assertion from *exits 0* to *exits 1*

| | |
|---|---|
| **Against** | [`C-0192`](../claims/C-0192-the-column-repair.md) §8 — *"the committed queue **was itself the fixture** that made `verdicts[0]` and `verdicts[-1]` different objects … what vanished was **the** discriminating input"* — and the same account in `TASKS.md`'s `T-292` and `T-295` rows |
| **Raised by** | [`C-0195`](../claims/C-0195-the-discriminating-input.md) / [`T-295`](../tasks/T-295-the-discriminating-input.md), by running the census it delivers against the pre-repair commit |
| **Kind** | **methodological — a correct observation with an incomplete causal account, where the missing half is a second, self-inflicted cause and a rule nothing in the corpus carries** |
| **Status** | **RAISED.** `C-0192`'s finding, its 21 rows, its token-preservation proof, its `12 mutations, 0 survivors` re-reading and its repair are all **UPHELD** and none of them moves. What is challenged is the sentence saying *why* the mutation survived |

---

## What `C-0192` §8 says

> Before the repair, twenty-one rows carried a **second, unstruck** verdict in another cell,
> and the committed queue was itself the fixture that made `verdicts[0]` and `verdicts[-1]` different objects.
> After it, every row of a gate-clean queue carries **exactly one** live verdict …
> The rule is unchanged and still binds on any non-conforming input, of which the file's own history is full;
> what vanished was the discriminating input.

Read as written, that is one cause: the queue was the discriminator and the repair removed it.

## What the pre-repair commit says

`T-295`'s census, run against `git archive 7f7957d` — the last commit before the repair — classifies every mutation of `tools/T-283-mutation-test.py` by running the harness twice: once in a faithful copy, once in a copy whose committed artifacts are emptied.

| the mutation | killers, faithful copy | killers, corpus emptied | verdict |
|---|---|---|---|
| *the residue reads EVERY verdict of a row rather than the leftmost* | **2** | **1** | **FIXTURE** |

At `7f7957d` that mutation had **two** killers, and one of them survives the committed queue being emptied to nothing.
The survivor is named, and it is a **constructed** fixture, not a committed file:

```
SELFTEST FAIL: T-289 a miscolumned verdict does NOT fail the gate — the arm is advisory, because the predicate
```

Its fixture is `check-queue-vocabulary.py`'s own `_leaf_row`, at `7f7957d`:

```
| ID | Task | Acceptance | Leaf | Status |
| T-1 | t | a | **DONE** (iteration 3) | **TODO — HIGH** |
```

— a two-verdict row, built in the file, fed through `_gate_on`, and asserted to exit **0**.
Under *leftmost → last* the residue arm fires on it, the gate exits **1**, and the assertion fails.
**That is a fixture kill, and it is the one `C-0192` §8's account has no room for.**

## So the survivor has a second cause, and `T-292` is it

`T-292` promoted the miscolumned arm to a gate, which required **inverting** that very assertion.
At `HEAD` the same test reads:

```
T-292 a miscolumned verdict FAILS the gate — the arm was promoted when the queue repair …
```

`_gate_on` returns an **exit code**, and an exit code **saturates**:
a run that already exits 1 for the miscolumned verdict exits 1 whether or not the residue arm fires as well.
So `== 0` is sensitive to an **additional** defect and `== 1` is blind to one.
Inverting the assertion did not weaken the gate — it strengthened it — and it removed that test's power to kill this mutation.

`C-0192`'s own Provenance line names the inversion — *"`tools/T-289-mutation-test.py` (16 mutations, 0 survivors, **with its gate row inverted**)"* — and §8 does not connect it to the survivor.

## The measured decomposition

Three states, each one run rather than argued, and each giving a different verdict:

| state | control killers | killers with the corpus emptied | verdict |
|---|---|---|---|
| `7f7957d` tools, `7f7957d` queue | 2 | 1 | **FIXTURE** |
| `HEAD` tools **minus** `C-0192`'s constructed fixture, a **pre-repair** queue | 1 | 0 | **CORPUS** |
| `HEAD` tools minus that fixture, a **repaired** queue | 0 | 0 | **SURVIVOR** |

The corpus-only state — the one `C-0192` §8 describes — **existed at no committed state of this repository**.
It was created inside `T-292`'s own working tree, by the two halves of that task acting together:
the queue repair removed one killer and the gate promotion removed the other.

## What is asked

1. `C-0192` §8's *"what vanished was the discriminating input"* annotated in place (`C-0071`: strike, never delete) to *"what vanished were **both** of the two discriminating inputs, and this task removed each of them"*, with the second named.
2. `TASKS.md`'s `T-292` and `T-295` rows carry the same singular account and are owed the same annotation.
3. The general rule recorded, because nothing in the corpus carries it: **an assertion that a gate EXITS 1 cannot detect an additional defect, because exit codes saturate — so inverting a gate test from *exits 0* to *exits 1* silently drops every mutation that test used to kill through the other arm.** That is a mutation-coverage loss caused by a correct promotion, and it is invisible to `P-31` (the anchor still resolves) and to the promoting task's own mutation table (which tests the arm being promoted, not the arms that shared its exit code).

## What is NOT asked

Nothing about the repair itself, which is right and is upheld;
nothing about `C-0192`'s own remedy, which is `C-0161`'s *construct the state* and is the correct one;
and nothing about `T-283`'s predicate, which never moved.
The survivor was real, the repair was right, and the account of the cause names one of two.
