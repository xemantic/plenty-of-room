# T-321 — a gate on the argument guard reads a SOURCE where the defect is a RUN

| | |
|---|---|
| **Raised by** | [`C-0210`](../claims/C-0210-fourteenth-answers-synthesis.md) (`T-319`), out of [`CH-0268`](../challenges/CH-0268-an-emitter-that-ignores-its-arguments-emits.md), which shipped a **static** gate and recorded that its own **dynamic** probe reads differently |
| **Leaf** | A8.2 (process) |
| **Verification type** | **logical and in-silico**: named self-tests in the probe, a mutation table over its predicates in both directions, and two full probe runs over `git archive` trees at two named refs |
| **Units** | none; every value below is an integer count of tools, a path, an exit code, a wall-clock second or a nanosecond of filesystem timestamp resolution |

## Formulate

`tools/cli_guard.py --check` asks a **source** whether it *could* refuse an argument it does not recognise.
`CH-0268`'s probe asks a **run** whether it *did* write.
Those are different questions about the same population, the challenge records both readings, and the queue row asks for either the dynamic arm wired or a **recorded refusal** saying why the static proxy is the one to gate on.

The row states the tension as *"`5` tools against the static predicate's `11`"* — a gap of six — and prices the probe at *"about ten minutes over a `git archive` tree"*.

**Every one of those three numbers is inherited, and `CLAUDE.md`'s rule is that a census is dated by its predicate and by its premise set.**
So the task is not *decide*; it is *re-derive, then decide*.

### What a decision has to be measured against

A recorded refusal is only a result if it names, with numbers:

1. what the dynamic arm **costs** per run;
2. what it **catches** that the static one does not;
3. what it **misses** that the static one catches.

(3) is the half a challenge that ships the static gate cannot supply, because it is a statement about the dynamic arm's blind spots, and `CH-0268` names only one of them.

### The cheap bound, run first, and it moves the answer

The readings the row quotes are readings at the **pre-repair** ref, because at `HEAD` the defect is repaired and both arms must read zero.
So the comparison has to be taken twice: at `HEAD`, where agreement is the expected result, and at `441270c8` — the parent of the repair commit `cfbeff3` — where the defect exists.

**One `git show --stat cfbeff3 -- tools/` settles the static half before any probe runs**: the repair commit guards **sixteen** writers, not eleven.
The published `eleven` is refuted by the artifact the same commit produced ([`CH-0275`](../challenges/CH-0275-the-repair-commit-guards-sixteen.md)).

## Plan

**Build the dynamic arm as a retained instrument, run it at both refs, and then decide.**
Building it is the only way to price it, and an instrument that has been run is worth more than a decision that has not.

1. **`tools/T-321-dynamic-guard-probe.py`**, TDD, with named self-tests.
   It exports a ref with `git archive` into a throwaway tree, hands each writer an argument it does not recognise, and observes the tree.
   It never touches the checkout.
2. **Delegate the population** to `cli_guard.writers()` rather than re-deriving it (`C-0195`), so the two arms are censuses of **one** population and their readings are comparable.
   The predicate is today's and the premise set is the ref's.
3. **Three observations, not one.**
   `CH-0268` §3 records that its checksum probe cannot see a byte-identical re-emission.
   A checksum is the wrong instrument for *did this tool write*; the cheap one is `st_mtime_ns`, which moves whether or not the bytes do.
   Classify `CREATED` / `DELETED` / `CONTENT` (all a checksum can see) / `TOUCHED` (the blind spot).
4. **Cheap bound inside the instrument too**: the detector is a `stat` walk and a `sha256` is taken only of the paths that walk flags — so the probe hashes a handful of files per writer rather than the tree.
5. **Measure the instrument's own floor.**
   `st_mtime_ns` is not nanosecond-resolved in practice; the granularity is a property of the box and must be measured, then compared against the smallest interval the probe has to resolve (one interpreter startup).
6. **Price the exact instrument beside the cheap one**: spot-check `strace -f -e trace=openat,…` on the byte-identical re-emitter, and time it against an untraced run, so *"a write-syscall observation would close it"* is a measurement rather than a suggestion.
7. **Account for every writer the static arm flags** — a dynamic reading that is merely smaller says nothing; a partition of the difference into named causes is what decides the question.
8. **Mutation-test the probe's predicates in both directions**, with a subtracted green baseline (`CH-0237`), declared in `tools/P-31-harness-census.py` in the same commit and wired.

### What would falsify this approach

- **The dynamic arm finds a writer the static arm does not.** Then the static predicate is not conservative, gating on it is unsound, and the dynamic arm has to be wired whatever it costs.
- **The mtime observation finds nothing the checksum found.** Then `CH-0268` §3's blind spot is notional, the third observation is dead weight, and the recorded refusal loses its most interesting half.
- **The probe's reading is not reproducible across two runs at one ref.** Then it is not an instrument and cannot be evidence either way.
- **The probe costs what the row says.** Ten minutes per run makes wiring it a real cost and the refusal easy; anything much cheaper means the refusal has to rest on soundness rather than on price.

## Conventions fixed before deriving

- **The population** is `cli_guard.writers()` — `tools/*.py` whose basename matches `(emit|fetch)`, excluding `test-` fixtures — evaluated on the tree under test.
- **The argument** is `--help`, which is what `CH-0268` used and what a cold session types first.
- **A write** is any change to a path in the tree that is not the observer's own footprint.
  CPython byte-compiles every module it imports, so `__pycache__/*.pyc` is excluded **by name** as an instrument artifact; unfiltered it is not a small correction (see `C-0214` §2).
- **A refusal** is exit code `2` (`cli_guard.refuse_unknown_arguments`) or `0` (`argparse`, which prints help and exits clean).
  Any other exit, or a timeout, is a **failure**, and a failure that wrote nothing is not a refusal.
- **The refs** are `HEAD` = `646b29e` and the pre-repair `441270c8`, resolved and recorded in the claim.
