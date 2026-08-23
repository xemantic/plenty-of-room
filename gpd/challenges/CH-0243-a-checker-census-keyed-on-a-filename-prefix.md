# CH-0243 — **The checker census's PREDICATE is a filename prefix, so all three of its counts are counts of `ls tools/check-*.py` and none of them is a count of GATES.** Derived at iteration 45: the command returns **ten**, not eight; **all ten** are wired into `tools/verify.sh`, so the *"seven of the eight"* clause is now false in the other direction; and `tools/verify.sh` runs **four further tools** as build-failing gates that the command cannot see, because they are not named `check-`

| | |
|---|---|
| **Against** | [`CH-0222`](CH-0222-a-self-describing-count-can-be-right-and-its-predicate-wrong.md) and the passage [`C-0171`](../claims/C-0171-twelfth-answers-synthesis.md) wrote into [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) — *"`ls tools/check-*.py tools/trace-answers.py` returns **eight** retained checkers; `grep` of `tools/verify.sh` shows **seven** of the eight wired into it — every one **except** `trace-answers.py`"* |
| **Raised by** | [`C-0191`](../claims/C-0191-thirteenth-answers-synthesis.md) (`T-276`), re-deriving the pass's own instrument list, as `CH-0182`'s *a census is dated by its premise set* requires |
| **Grounds** | **derivation, four commands, no solve.** `ls tools/check-*.py tools/trace-answers.py` returns **10** — `check-cold-start-note.py` and `check-queue-vocabulary.py` were added by `P-29` in iteration 42. `grep -nE '^\s+tools/' tools/verify.sh` shows **all ten** invoked, `trace-answers.py` among them: it was wired by `T-277` ([`C-0173`](../claims/C-0173-trace-answers-wired.md)) **in the same iteration the challenged sentence was written**. The same grep shows four more tools invoked under `set -euo pipefail` — `result-reader-census.py --check`, `T-278-emitter-rounding-census.py --check`, `T-272-emit-result-inputs.py --check` and `T-272-header-census.py --check` — so the number of distinct tools wired as build-failing gates is **14**. And *which* of them read the two deliverables is derived by importing each tool and asking it for its own file list rather than by reading its name: **4** of the ten, unchanged |
| **Status** | **RAISED, and REPAIRED in the same pass** by striking the two stale clauses and replacing them with the re-derived counts, the commands that produce them, and the four gates the command cannot see. **`CH-0222`'s finding is upheld and generalised** — it found that a count can be right in its number and wrong in its predicate; what this adds is that the predicate here is a **naming convention**, so the sentence cannot be made true by re-deriving the number |

---

## 1. What moved, and in which direction

| clause | as written (iteration 41) | derived (iteration 45) |
|---|---|---|
| how many exist | **8** | **10** — `check-cold-start-note.py` and `check-queue-vocabulary.py` added since |
| how many are wired into `tools/verify.sh` | **7 of 8**, *"every one except `trace-answers.py`"* | **10 of 10** |
| how many read the two deliverables | **4** | **4**, unchanged |
| how many tools `tools/verify.sh` runs as build-failing gates | *not asked* | **14** |

The second row is the interesting one, because it is false in the **favourable** direction: the exception the
sentence names was removed by `T-277` in the same iteration, so a reader following the advice
*"`trace-answers.py` is the one whose absence a synthesis has to remember"* would be remembering a gate that
is already there.

## 2. Why re-deriving the number does not repair the sentence

`CH-0222` repaired *"seven"* by deriving *"eight"*. That repair is inside the same predicate, and the predicate
is `ls tools/check-*.py`.

A gate is a tool `tools/verify.sh` invokes under `set -euo pipefail`. Four of them are named for the task that
produced them rather than for what they do:

- `tools/result-reader-census.py --check` (`P-22`) — the result-file dependency graph;
- `tools/T-278-emitter-rounding-census.py --check` (`T-278`, [`C-0174`](../claims/C-0174-emission-header-residue-and-the-seven-unrounded-emitters.md)) — every emitter's rounding call;
- `tools/T-272-emit-result-inputs.py --check` (`T-272`) — the typed input-handle registry;
- `tools/T-272-header-census.py --check` (`T-272`) — the emission header.

None of them can ever be returned by a glob on `check-*`, at any point in the future, no matter how carefully
the number is re-derived. **The count is a count of a naming convention.**

## 3. What is NOT disputed

Nothing about any tool's behaviour, and no number of `C-0165`, `C-0171` or `CH-0222` about the state of the
tree at the iteration it was read. Both earlier findings stand:

- a self-describing count is the one number a numeric tracer cannot own, and deriving it is the repair
  (`C-0165`);
- a count can be right in its number and wrong in its predicate (`CH-0222`).

What is added is the third member of the family: **a predicate keyed on a filename cannot be made complete by
being re-run**, so a census of gates has to be taken over `tools/verify.sh`'s own invocation list. The repaired
passage now quotes both — the ten under the naming predicate, and the fourteen under the gate predicate — with
the command that produces each.

## 4. What would falsify this

- A reading of `tools/verify.sh` in which any of the four named tools is **not** build-failing — it is not
  invoked under `set -euo pipefail`, or its non-zero exit is swallowed. One `grep` of `set -` and of the
  invocation line settles it, and it was run: `set -euo pipefail` is at line 133 and all four are plain
  invocations.
- A demonstration that `trace-answers.py` is not in fact wired — the two invocation lines are
  `tools/trace-answers.py` and `tools/trace-answers.py --answers DECISIONS-FOR-NDI.md`.
