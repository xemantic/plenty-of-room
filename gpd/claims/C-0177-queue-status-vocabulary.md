# C-0177 — the queue's status vocabulary, mechanised; and the checker that guards it could not fail

| | |
|---|---|
| **Task** | [`P-29`](../tasks/P-29-queue-status-vocabulary.md) |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests and a mutation test |
| **Verdict** | **PASS.** `tools/trace-answers.py` exits non-zero on all four of its checks for the first time; the `T-9` coinage is repaired; and `tools/check-queue-vocabulary.py` refuses the next one, at a false-positive rate **measured at 0 over 37 clean revisions of `TASKS.md`** |
| **Raises** | [`CH-0231`](../challenges/CH-0231-a-gate-that-cannot-fail.md), against [`C-0173`](C-0173-trace-answers-wired.md) |

---

## 1. What was wrong

Three things, found in this order and each cheaper than the last to state.

**(a) A coinage.** Iteration 41 rewrote the `T-9` row's status from `**PARTIALLY DONE**` to
`**SECOND DELIVERABLE ANSWERED, iteration 41**`. `queue_status` matches a closing word anywhere
after the identifier, so the row read **CLOSED** — while `ANSWERS.md` said, correctly, that `T-9`
is live on the in-plane shear `k_s`. `T-9`'s three deliverables are done ([`C-0157`](C-0157-crossover-hinge-constant-from-oxdna.md)),
done ([`C-0169`](C-0169-crossover-vertical-compliance.md)) and open. This is the **third** instance
of a class `CLAUDE.md` records twice, and its failure direction is the unsafe one: an open row
silently leaves the register.

**(b) The gate that caught it could not fail.** `tools/trace-answers.py`'s `main` accumulates
`failures` into a dead local and ends `return 0` — since iteration 12. `C-0173` wired it into
`tools/verify.sh` under `set -euo pipefail` on the stated ground that *"it already returned its
defect count"*, which is a true statement about the **inner** function `check_document` and false
about the one `sys.exit` reads. So the wiring was inert for **all four** checks, and `(a)` printed
on stdout through six commits — two of which record a full-suite run — with the build green.
Filed as [`CH-0231`](../challenges/CH-0231-a-gate-that-cannot-fail.md).

**(c) `CLAUDE.md`'s own prescribed sweep for `(a)` does not catch `(a)`.** The entry names it:
*"any row whose status cell starts `TODO` and which `queue_status` calls CLOSED"*. **0 of 5** cells
of the broken `T-9` row start `TODO`, and over the whole queue the predicate is **21** hits of which
**20** are correct — because `C-0071`'s *strike, never delete* keeps a closed row's original
`TODO — MEDIUM …` prose forever. The sweep was written to the shape of the previous instance.

## 2. The repairs

| | what | evidence |
|---|---|---|
| (a) | `T-9`'s status restored to `**PARTIALLY DONE** — second deliverable answered, iteration 41` | the established vocabulary, and it carries strictly more than the coinage: the row is *not closed* **and** which deliverable closed |
| (b) | `main` returns `1 if failures else 0` | six end-to-end tests in `tools/test-trace-answers.py`, one per check plus the truncation |
| (c) | `tools/check-queue-vocabulary.py`, gated, with `tools/test-check-queue-vocabulary.py` | 16 self-tests; **6 mutations, 0 survivors** |

**The exit code is a boolean and not the count, deliberately.** `sys.exit(n)` truncates modulo 256
and `counts["ABSENT"]` is unbounded, so exactly 256 absent tokens would have exited 0 and read as a
clean corpus — a second latent way for this gate to be inert. Named test:
*"300 defects still exit non-zero (256 would truncate to 0)"*.

## 3. The new gate's predicate, which is a measurement rather than a taste

A verdict is a bold run that **opens a cell** and is at most **six** words. The alternative — any
bold run containing a closing word — was measured first:

| predicate over `TASKS.md` | distinct phrases |
|---|---|
| bold run **anywhere** in a row containing a closing word | **29** |
| bold run that **opens a cell**, ≤ 6 words | **8** |

The 29 are mostly prose about some *other* task — *"`CH-0185` is ANSWERED"*, *"CONTINGENT is not
KILLED"*, *"`P1` was found ALREADY DISCHARGED"* — so a gate on them is ~25 rows of noise, which is
the rate at which a gate is switched off. The 8 are six declared verdicts plus two legitimate
coinages (`ANSWERED BY IMPLICATION` on `T-166`, `ANSWERED in its specification half` on `T-154`),
both of which close their row correctly and are declared **closing**.

**The half that has content is not the list.** A vocabulary satisfiable by appending a string is
not a check. Every declared phrase is additionally put through `trace-answers.queue_status` in a
synthetic one-row queue and must be read in the sense it is declared in. Declaring
`SECOND DELIVERABLE ANSWERED` *not closing* — what an author in a hurry would do — therefore fails,
because the reader still closes it; `_NOT_CLOSED_QUALIFIER` has to be taught the word too. That is
the second sense `CLAUDE.md` asks every coined status word to be tested in, and it is the mutation
the mutation test ends on. `CLAUDE.md`: *a checker's blind spot is found by the tool that must
agree with it.*

## 4. Verification

| gate | reading |
|---|---|
| 1 dimensional consistency | n/a — no physical quantity |
| 2 limiting cases | a row with no verdict yields none; a wholly struck verdict yields none; a verdict **after** a struck `~~TODO~~` is still found (the real corpus shape, `T-256`/`T-257`) |
| 3 symmetry / conservation | the declared vocabulary and `queue_status` agree in **both** senses, asserted per phrase |
| 4 numerical convergence | n/a — exact predicate |
| 5 cross-check | **counter-factual**: the new gate reads **1 defect** on `TASKS.md` at `7368986` and **0** at `HEAD`; the repaired `trace-answers.py` exits **1** at `7368986` and `b853b85` and **0** at `HEAD` |

**False-positive rate, measured rather than argued.** Over the last **40** revisions of `TASKS.md`
the new gate fires on **3** — exactly the three commits carrying the coinage — and on **0** of the
other 37.

**Mutation test, both directions** (`tools/test-check-queue-vocabulary.py`), because a predicate
carrying exclusions has two and the widening one is never written:

| mutation | killed by |
|---|---|
| a bold run **anywhere** in the cell counts | *a bold PROSE sentence mid-cell is not a verdict* |
| no word bound | *a long leading bold sentence is not a verdict* |
| a verdict must be the **whole** cell | 6 named tests |
| strike-through no longer blanked | *a verdict AFTER a struck prefix is still found* |
| the vocabulary opened | *the iteration-41 coinage is REFUSED* |
| **the list edit**: the coinage declared NOT CLOSING | *the declared vocabulary agrees with the reader* |

The mutation test **found two defects in its own self-tests on first run** and both were real: the
strike test asserted the wrong direction (blanking makes a **live** verdict visible after a struck
`~~TODO~~`, which is the corpus's actual shape, rather than hiding a struck one), and the pattern
carried a redundant `^` beside a `.match()` call site, so a mutation of **either** anchor was a
no-op. One anchor, one place.

## 5. Provenance

Derived here; nothing inherited. `tools/check-queue-vocabulary.py` (16 self-tests),
`tools/test-check-queue-vocabulary.py` (6 mutations), and the six added end-to-end tests in
`tools/test-trace-answers.py`. Both gates are wired into `tools/verify.sh`; both self-tests into
`./gradlew test`.

## 6. Validity range

A statement about **this** repository's queue and its checkers, at the commit that lands it. The
vocabulary is a **declared** set and is expected to grow — that is the point, and the gate's whole
function is to make the growth explicit. It reads only `TASKS.md`; a status asserted anywhere else
is out of scope. And it cannot see a closing word about **another** task in lower-case prose, which
is the queue's own idiom and is by construction invisible to a bold-run predicate.
