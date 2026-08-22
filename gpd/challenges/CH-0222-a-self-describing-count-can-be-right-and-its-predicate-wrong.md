# CH-0222 — **`DECISIONS-FOR-NDI.md`'s checker census says *"there are now SEVEN, and every one of them reads this file … All seven are wired into `tools/verify.sh`"*, and TWO of those three clauses are false.** Derived: **eight** retained checkers exist, **four** of them read the file, and **seven** are wired into `verify.sh` — the one that is not is `trace-answers.py`, which is the only one that reads this document *by name*

| | |
|---|---|
| **Against** | [`C-0165`](../claims/C-0165-eleventh-answers-synthesis.md) §3 and the passage it wrote into [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) — *"**UPDATED, iteration 39 — there are now SEVEN, and every one of them reads this file.** … All seven are wired into `tools/verify.sh`."* |
| **Raised by** | [`C-0171`](../claims/C-0171-twelfth-answers-synthesis.md) (`T-276`), while re-deriving the pass's own instrument list |
| **Grounds** | **derivation, three commands.** `ls tools/check-*.py tools/trace-answers.py` returns **8**. `grep -nE "tools/(check\|trace)[a-z-]*\.py" tools/verify.sh` shows **7** of them invoked — every one except `trace-answers.py`, whose **self-test** is wired in `build.gradle.kts` and whose checker is not wired anywhere. Reading the scope of each: `check-corpus-links.py` and `check-corpus-identifiers.py` scan the root documents, `check-markdown-tables.py` scans every tracked `*.md`, `trace-answers.py` defaults to both — **four**; `check-entry-points.py` reads `TASKS.md` and `src/`, `check-challenge-index.py` reads `gpd/challenges/`, `check-result-file-hygiene.py` reads `gpd/results/`, and `check-kotlin-format-strings.py` reads `src/` — **four that do not**, and the eighth was omitted from the enumeration entirely |
| **Status** | **RAISED, and REPAIRED in the same pass** by striking the two false clauses and replacing them with the three derived counts and the commands that produce them. **`C-0165`'s finding is upheld** — the count *had* been stale and deriving it *is* the repair; what is added is that a count is only as true as the predicate it is a count of |

---

## 1. Why this is not the same defect `C-0165` fixed

`C-0165`'s finding is that **a self-describing count is the one number a tracer cannot own**, and its
repair was to derive the number. That is right and it worked: **seven** is a defensible reading of
*"how many checkers were wired into `verify.sh` at iteration 39"*, and it was.

What was not derived is the **predicate**. One sentence carries three of them —

1. *how many exist* → **8**,
2. *how many read this file* → **4**,
3. *how many are wired into `verify.sh`* → **7** —

and a single number was written for all three. Every reading of that sentence is therefore wrong
about two of the three questions it appears to answer, and the tracer cannot see it because **`7` is
not a quantity with an owning claim at all**.

## 2. The consequence, which is not cosmetic

The checker the sentence promises is gated is `trace-answers.py`, and it is the **only** one of the
eight that reads this document by name and the **only** one not in `verify.sh`. A reader of
`DECISIONS-FOR-NDI.md` — or an agent taking a synthesis task — is told that every checker on this
file runs in the build. In fact the numeric, task-status, challenge-status and self-consistency
checks run **only when a human remembers to run them**, which is precisely the failure mode
`C-0078` mechanised the others against.

## 3. The general form

**A self-describing count can be right in its NUMBER and wrong in its PREDICATE**, and the failure is
invisible to every check in this repository because a count of the instruments is owned by no claim.
Derive each predicate separately and print the command beside it — which is what the repaired
passage now does.

## 4. What this does NOT establish

- No claim of `C-0165` about `ANSWERS.md` or about iteration 39's three findings is disputed.
- It is **not** a recommendation to wire `trace-answers.py` into `tools/verify.sh`. That is a
  judgement with a cost — the tracer's `ELSEWHERE` rows are adjudicated by hand and a gate that
  cannot come clean is not a gate (`C-0083`) — and it belongs to whoever owns the build, not here.
