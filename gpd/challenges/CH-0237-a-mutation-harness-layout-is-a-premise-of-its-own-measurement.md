# CH-0237 — a mutation harness's FIXTURE LAYOUT is a premise of its own measurement, and a change in the SUBJECT can make `0 survivors` vacuous without failing anything

| | |
|---|---|
| **Against** | [`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md)'s Verdict and §6 — *"**24 mutations, 0 survivors**"* — and the harness that produces it, [`tools/P-30-mutation-test.py`](../../tools/P-30-mutation-test.py) |
| **Raised by** | [`C-0183`](../claims/C-0183-residue-as-a-gate.md) / [`T-283`](../tasks/T-283-residue-as-a-gate.md), which broke it by adding self-tests that read the corpus |
| **Kind** | **methodological — a measurement whose fixture is an unstated premise.** The table was correct when it was taken and stopped being a measurement when the subject changed, with no test anywhere failing |
| **Status** | **RAISED and REPAIRED in the same iteration.** The harness now builds `<tmp>/tools/*.py` beside `<tmp>/TASKS.md` and **measures and subtracts** the failures of an unmutated copy; the reading is **24 mutations, 0 survivors** again, at a baseline of **0** |

---

## The measurement

`tools/P-30-mutation-test.py` copies `tools/` **flat** into a scratch directory and runs the gate there.
The gate resolves its queue as `dirname(dirname(__file__))/TASKS.md`,
so in a flat copy that is `/tmp/TASKS.md`, which does not exist.

While no self-test read the queue this was harmless: nothing touched the missing file.
`T-283` added self-tests that do — among them *"the residue is now a GATE and the real queue reads zero"* —
and the table immediately read:

```
killed  reader  0  gate  1   the per-row agreement check compares against the wrong sense
                          FAIL (raised) FileNotFoundError: [Errno 2] No such file or directory: '/tmp/TASKS.md'
...
# 24 mutation(s), 0 survivor(s)
```

**Every one of the 24 rows is killed, and every one is killed by the same error, which is about none of them.**
The headline is unchanged, no test fails, and the table has stopped being a measurement.

## Why this is not a nitpick

`C-0177` measured the trap that makes a mutation table *look full and be empty* — 9 of 22 rows of `C-0176`'s first table failed nothing because the mutation widened a rule to `original|mutant` instead of replacing it.
This is the same failure reached from the other side: not from the **table**, which is correct here, but from the **subject**, which moved underneath it.
A widened mutation is visible in the table's own source; a harness whose fixture no longer matches the subject is visible nowhere.

The general form: **a mutation table's killer counts are only evidence if an UNMUTATED copy passes**, and nothing was asserting that.
The repair is two lines and it is the same one in both harnesses —
run the unmutated copy first, record its failures, and subtract them —
after which a `killed` row means *this mutation broke something*, which is what the column says.

`T-283`'s own first run is the corroboration: with a flat layout it read **12 mutations, 12 survivors**, which is the same defect in the loud direction.

## What is challenged, and what is not

**Not challenged:** any of `C-0178`'s 24 mutations, its predicate, its repairs, its coverage finding, or its false-positive measurement over 138 revisions. The table is **restored to the same reading** — 24 mutations, 0 survivors — under a harness that can now fail.

**Challenged:** the *falsifiability* of that reading after the subject changed, and the unstated premise that made it so.

## What would settle it in general

A one-line assertion in every mutation harness in this repository: **the unmutated copy must produce zero named failures**, printed, before any mutation runs.

Censused, all five carry it now, and two of them carried it already —
`tools/T-234-mutation-test.py` (`BASELINE FAILS on …`) and `tools/T-280-mutation-test.py` (`BASELINE FAILS: …`),
which is why the first is what **caught** this iteration's own collateral:
completing the census's family map falsified a self-test in `tools/T-234-emit-classification.py` whose *name* stayed true —
*"every family the census does not gate has a coercion"*, written as `set(FAMILY_DISCHARGE) == set(FAMILY_CLASS)`,
a proxy that held only while the map was **partial**.
The three subprocess harnesses — `tools/P-30-mutation-test.py`, `tools/T-281-mutation-test.py`, `tools/T-283-mutation-test.py` — carry it as of this iteration.

**RESIDUE DISCHARGED, iteration 44** ([`C-0185`](../claims/C-0185-orphaned-mutation-anchors.md), [`P-31`](../tasks/P-31-orphaned-mutation-anchors.md)). All **ten** mutation harnesses in `tools/` now measure and report a baseline, against **4 of 8** at `9620d3e`; and the same task found this defect's twin in the QUIET direction — `tools/test-check-queue-vocabulary.py` copied `tools/` flat and copied one of the two modules its subject imports, so a mutant that never started was reported as a **survivor** rather than as a kill. [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py) reports each harness's baseline beside its anchor resolution, so the omission is now visible without waiting for a table to go strange.

**The residue this challenge leaves open** is the other half of the same premise: an in-process harness (`T-234`, `T-280`) execs the mutated source and cannot be wrong about *where* the file lives, while a subprocess harness must reproduce the tree's own **layout** — and nothing asserts that it does. The baseline check is what makes the omission visible rather than silent, which is why it is the repair and not a `require` on the path.
