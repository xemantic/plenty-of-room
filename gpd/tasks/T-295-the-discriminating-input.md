# T-295 — a mutation's discriminating input can be the COMMITTED CORPUS, and repairing the corpus expires it silently

| | |
|---|---|
| **Raised by** | [`C-0192`](../claims/C-0192-the-column-repair.md) / [`T-292`](T-292-the-column-repair.md) §8, where it is not hypothetical: repairing 21 queue rows took [`tools/T-283-mutation-test.py`](../../tools/T-283-mutation-test.py) from 0 survivors to 1, on a correct predicate |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests, a mutation table over the new predicate, and a paired experiment over every mutation of every harness in `tools/` |
| **Units** | none; every value below is an integer count or a verdict name |

## Formulate

[`P-31`](../claims/C-0185-orphaned-mutation-anchors.md) asks whether a mutation harness's reference **into its subject** still resolves —
a literal anchor of text, or the name of an attribute.
It cannot ask the other half:
**what input makes the mutation observable at all.**

`C-0192` §8 is the instance.
`tools/T-283-mutation-test.py`'s twelfth mutation flips the residue arm from a row's **leftmost** verdict to its **last**.
Before `T-292`, twenty-one rows of `TASKS.md` carried a second, unstruck verdict in another cell,
so `verdicts[0]` and `verdicts[-1]` were different objects on the real file;
after the repair every row of a gate-clean queue carries exactly one live verdict, the two are the same object, and the mutation became **unobservable**.
The rule is unchanged and still binds on any non-conforming input.
What vanished was the **discriminator**.

`P-31` resolves every anchor of that harness and reports **0 unresolved**, correctly:
the anchor was never the problem.
And the failure was loud only because a harness happened to be re-run in the same hour —
which `CLAUDE.md` has recorded six times as *a convention is not a mechanism*.

**Numeric target.**
A census over **every** mutation harness in `tools/`, one row per mutation,
saying whether the input that kills that mutation is a **fixture** the test constructs or a **committed artifact** it reads;
the fixture-less rows either given a constructed fixture (`C-0161`) or recorded **by name** so their expiry is loud;
and the census **wired** the way `tools/P-31-harness-census.py --check` is, reading 0 on the tree it lands on.

**Acceptance predicates, falsifiable.**

- **F1 — every mutation is classified, and the classification is MEASURED.** Each mutation of each harness gets exactly one of `FIXTURE`, `CORPUS`, `SURVIVOR`, `REVIVED`, derived from running the harness rather than from reading its source. A harness whose rows cannot be read is a **refusal**, never a clean table.
- **F2 — the harness list is `P-31`'s, not a second copy of it.** The census imports `P-31`'s declared table, so a harness written tomorrow fails `P-31`'s own discovery gate rather than being invisible here. A duplicated rule is invisible to a mutation test of either copy.
- **F3 — the census REFUSES rather than under-counting.** The number of rows parsed is cross-checked against each harness's own summary line, the two arms are checked to have the same row count, and the row labels are checked to agree position by position. Any of the three failing is a reported defect.
- **F4 — the instrument is checked against the instance it was written for.** The census must return `CORPUS` on `C-0192` §8's own mutation when the two named tests `C-0192` added are excised and a **pre-repair** queue is put back, `SURVIVOR` when only the tests are excised, and `FIXTURE` as the tree stands. Three verdicts, three states, all three run.
- **F5 — the gate reads 0, or the residue is named.** `--check` exits 1 on any undeclared `CORPUS` row, any `SURVIVOR`, any `REVIVED`, any refusal, and on any **declared** exemption that has stopped being true. It must read 0 on the tree this lands on, or the residue is reported honestly with the reason it cannot come clean.
- **F6 — the mutation table, and every row fails a NAMED test.** Every rule of the census is mutated **wholesale** (never widened to `original|mutant`), the harness measures and subtracts an unmutated baseline, and the count of mutations failing **nothing** is reported.
- **F7 — the new harness is declared and wired.** It is a row of `P-31`'s `HARNESSES`, `tools/P-31-harness-census.py --check` still reads 0 unresolved, and the census is run from `tools/verify.sh` so that it can fail a run.
- **F8 — the result file is DATED.** Its subject is a mutable corpus, so it records the resolved SHA of the ref it measured (`CH-0210`, `CH-0182`).

**What would falsify this approach.**

The measurement is a **paired experiment**: each harness runs unmodified in a faithful copy of the tree and in a copy whose committed artifacts are emptied.
It rests on two premises, and either failing would falsify it.

1. **That emptying the corpus removes discriminating power rather than breaking the suite.** Every harness here subtracts an unmutated baseline, so a named test that cannot run against an emptied corpus is subtracted rather than counted — but a harness whose *whole* suite dies in the treatment arm would report every one of its mutations as `CORPUS`, and the census would be measuring its own neutralisation. The signature is a harness reading 100 % corpus-dependent; if that is what the run shows, the treatment is wrong and the honest outcome is a static reading instead.
2. **That the two arms differ in nothing but the corpus.** If a harness behaved differently in a copy for a reason unrelated to the corpus — a missing `.git`, a missing build directory — the control arm would report survivors that the in-place run does not. The check is free and it is a predicate: **the control arm must reproduce 0 survivors for every harness**, which is what the build already asserts in place.

A residual **false negative** remains and is stated rather than argued away: a corpus-reading test that still discriminates on an *emptied* corpus would be classified `FIXTURE`. Emptying is the maximal perturbation, so this is the conservative direction, and `F4` bounds it on the one instance the class is known to have.

## Plan

**The cheap bound runs first, and it decides the whole method.**

The obvious route is **static**: parse each self-test suite, find the `check(name, expression)` calls, and ask of each whether its expression reaches a read of `TASKS.md`, `ANSWERS.md` or `gpd/`.
It is refused on three measurements, each of which costs one look:

- **the killer sets are truncated.** Several harnesses print only the first two killers of each row and truncate each to 110 characters, so the *complete* killer set — which is what *every killer is corpus-backed* needs — is not in any harness's output.
- **`CLAUDE.md` already prices the closure.** *A static call graph over FILES is not a conservative approximation, it is noise*; the reach here would cross eight tools and two import styles.
- **it answers the wrong question.** *Does this test read `TASKS.md`* is not *does this test NEED `TASKS.md`*, and the second is what expires.

So the method is the experiment, and its cost was measured before anything was written: twelve harnesses at 0.1–30 s each, run twice, is **about two minutes** — cheaper than the static closure and it answers the question directly.

**The reconstruction is built out of CONSTRUCTED queues and not the live file**, for the reason the whole task is about: a reconstruction that read `TASKS.md` would give a different answer inside this census's own treatment arm. The first run of the new harness *inside* `--check` reported exactly that, on two of its own rows, and the repair is `C-0161`'s.

**One historical check is kept and it is PINNED** — the third state re-run against `git show 7f7957d:TASKS.md`, the real pre-repair queue — because `CLAUDE.md`'s cure for a self-test that reads a mutable artifact is to pin the ref. It needs a git repository, so it prints a visible skip where there is none (a `verify.sh` snapshot carries no `.git`), and every mutation of that block is held open by the constructed states instead.
