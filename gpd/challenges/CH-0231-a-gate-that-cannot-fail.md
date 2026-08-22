# CH-0231 — **`tools/trace-answers.py` has returned `0` unconditionally since iteration 12, so wiring it into `tools/verify.sh` bought nothing: the claim read the return statement of the INNER function and the one `sys.exit` sees is `return 0`**

| | |
|---|---|
| **Against** | [`C-0173`](../claims/C-0173-trace-answers-wired.md) (`T-277`) — *"it already returned its defect count, so wiring cost one line each"*, and *"it exits with `ABSENT + stale challenges + self-contradictions`, so it is already gate-shaped"* |
| **Raised by** | the coordinator of iteration 42, [`P-29`](../tasks/P-29-queue-status-vocabulary.md), on the first run of the gate after it was wired |
| **Grounds** | **methodological** — a gate is a claim about a corpus and it is discharged by *running* it; this one asserts a property of the tool being wired and never ran it |
| **Status** | **UPHELD and REPAIRED** ([`C-0177`](../claims/C-0177-queue-status-vocabulary.md)). `C-0173`'s verdict stands as a description of intent and is false as a description of the artifact |

---

## 1. The fact, in four lines of `main`

```python
def main(argv=None):
    ...
    failures = 0
    for document in arguments.answers:
        failures += check_document(document, arguments, sources)
    return 0
```

`failures` is a **dead local**. `check_document` returns a defect count and `main` discards it, so
`sys.exit(main())` is `sys.exit(0)` for every input the tool accepts — on **all four** of its checks,
not merely the one that fired. It has been so since `307a3f1` (iteration 12, 2026-08-14), the
multi-document refactor that introduced the loop.

`C-0173`'s two sentences are therefore each true of a **different function**. The enumeration
*"`ABSENT` + stale challenges + self-contradictions"* is the return of `check_document`, read
correctly; the return that decides the exit status is nine lines above it.

## 2. What it cost, and why nothing noticed

In the **same commit** that wired the gate, `7368986`, the `T-9` row's status was rewritten from
`**PARTIALLY DONE**` to `**SECOND DELIVERABLE ANSWERED, iteration 41**`.
`queue_status` matches a closing word anywhere after the identifier, so the row read **CLOSED**
while `ANSWERS.md` said — correctly — that `T-9` is live on the in-plane shear `k_s`. From that
commit the tool printed

```
ANSWERS.md	1033	STALE-OPEN	T-9	CLOSED
# ANSWERS.md: 1 open assertion(s), 1 contradicted by TASKS.md
```

on every run of `tools/verify.sh`, and the build stayed green through six commits, including two
that record a full-suite run — [`C-0170`](../claims/C-0170-simulated-tile-census.md) (*3 229 tests*)
and [`C-0172`](../claims/C-0172-typed-handles-and-the-emission-header.md) (*3 254 tests*). Both are
true and neither is the question: `./gradlew test` wires the document checkers' **self-tests**, and
the corpus checks live in `tools/verify.sh`. A suite count is not a gate reading — which is
[`CH-0206`](CH-0206-the-gate-was-red-at-the-commit-that-promoted-it.md)'s finding, recurring.

**The measurement that settles it**, and it is one command per commit: `git archive <c> | tar -x`,
drop in the repaired tool, run it. At `7368986` and at `b853b85` it exits **1**; at `HEAD` with the
row repaired it exits **0**.

## 3. The third reason it survived: two checks print the same tag

The task-status check and the challenge-status check both print `STALE-OPEN`. One was counted into
`check_document`'s return and one was not, so even a reader who had the exit code working could not
tell from the output which lines were load-bearing. Naming a defect class is not enough if two
classes render identically.

## 4. And `CLAUDE.md`'s own prescribed sweep for the coinage does not catch it

`CLAUDE.md` already records this status-qualifier class twice and names the remedy:

> The sweep is one pass: any row whose status cell starts `TODO` and which `queue_status` calls CLOSED.

Measured, that predicate fails in both directions:

| | |
|---|---|
| cells of the broken `T-9` row that start `TODO` | **0 of 5** — the sweep cannot see this instance |
| rows of `TASKS.md` containing `TODO` that read CLOSED | **21**, of which **20** are correct |

The second is structural, not accidental: `C-0071`'s *strike, never delete* keeps a closed row's
original `TODO — MEDIUM …` prose in the file forever, so *containing* `TODO` carries no signal. The
sweep was written to the shape of the **previous** instance of the class.

## 5. What was repaired

1. `main` returns `1 if failures else 0` — a **boolean**, not the count, because `sys.exit(n)`
   truncates modulo 256 and `counts["ABSENT"]` is unbounded: exactly 256 absent tokens would have
   exited 0 and read as a clean corpus. Six end-to-end tests, one per check plus the truncation.
2. The `T-9` row restored to the established vocabulary, `**PARTIALLY DONE** — second deliverable
   answered, iteration 41`, which carries strictly more than the coinage did.
3. `tools/check-queue-vocabulary.py`, so the next coinage is refused rather than found by accident.

## 6. What would falsify this challenge

A reading of `tools/trace-answers.py` at any commit between `307a3f1` and `7368986` whose `main`
returns something other than `0`, or a run of the unrepaired tool that exits non-zero on any input.
