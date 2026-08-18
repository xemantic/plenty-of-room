# C-0117 — **`C-0092`'s `A5` margins do not move at all — 1.0000 at every one of 12 folds, worst departure `3.0e−09`, against a published 1.0000–3.3380× that was measuring a stale input.** And the defect was **one edge of two, not systematic**: `C-0101`'s eleven re-emissions contain exactly two dependency edges, `T-157`←`T-149` (violated) and `T-138`←`T-136` (**clean**, established with no solve at all). **The instrument that prevents the class was already in the tree** — `tools/result-reader-census.py` derives the graph a topological sort needs, including the transitive edges a grep cannot see — so a re-emission sweep is a **sorted order**, not a list, and that is 20 lines and 11 self-tests. The tempting general gate is **measured and declined**: 499 nonzero reproduction departures across 64 of 104 result files, most of them legitimate literature cross-checks

| | |
|---|---|
| **Task** | [`T-200`](../tasks/T-200-reemission-order.md) — amend `C-0092`'s margin-movement deliverable, and prevent the class |
| **Leaf** | — (a process claim; it guards the reproducibility every claim rests on) |
| **Verification type** | **logical**, with **11** executable self-tests on [`tools/reemission-order.py`](../../tools/reemission-order.py) and every number derived from a committed artifact at run time |
| **Verdict** | **PASS on all four predicates, and the declared falsifier did NOT fire** — the second edge is clean, so the defect is an erratum plus a missing tool rather than a systematically wrong sweep. `A5`'s **range** is corrected and everything else in `A5` stands: the candidate, the arithmetic, and the *"element model branch end"* ceiling binding at **0 of 12** states. |
| **Maturity** | **Below TRL 1–3: nothing here is physics.** No solver runs and no physical quantity is derived; the claim is about result-file provenance. |
| **Provenance** | [`gpd/results/T-200-reemission-order.json`](../results/T-200-reemission-order.json), emitted by the retained [`tools/T-200-emit-result.py`](../../tools/T-200-emit-result.py), which reads `T-157`, `T-136`, `T-138`, the reader census and every result file in the tree at run time. Amendment block added to [`C-0092`](C-0092-large-rotation-arm-branch.md); its `A5` clause struck in place. |
| **Conditions** | The corpus at iteration 24. `T-196` was commissioned in the same iteration and had not filed when this was written; nothing here depends on it. |
| **Consumes** | [`CH-0131`](../challenges/CH-0131-t-157-was-re-emitted-before-its-own-input.md) (which raised it), [`C-0092`](C-0092-large-rotation-arm-branch.md) (amended), [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (the re-emission discipline, and the sweep whose order was wrong), [`C-0082`](C-0082-result-reader-census.md) (the graph), [`C-0080`](C-0080-third-answers-synthesis.md) (the false-positive doctrine that decides the gate question) |
| **Constrains** | `C-0092`'s `A5` clause. **No other claim, number or verdict moves**, and no new challenge is raised — `CH-0131` already carries the contradiction. |

---

## 1. The amendment, verified rather than inherited

Read off the re-emitted `T-157`'s own `marginMovement` field, over all 12 folds:

| | |
|---|---|
| published in `C-0092` `A5` | **1.0000–3.3380×** |
| measured now | **1.0000 at every fold** |
| worst \|1 − movement\| | **3.0e−09** — the solver's own noise, not a movement |
| `A5`'s ceiling verdict | **binds at 0 of 12** — unchanged |

So the correction is to a **range**, and the clause's finding survives. `CLAUDE.md`'s rule applies in the
favourable direction for once: *a verdict that survives can survive on a different reason* — here it survives
on the same reason and loses only a number that was never real.

---

## 2. Why it was wrong, and why nothing was looking

`T-157` reads `T-149` at run time. `C-0101` re-emitted **`T-157` before `T-149`** in one commit, so the
committed `T-157` reproduces the *pre-`C-0101`* `T-149` digit for digit and the 3.3380× is a difference
`C-0101` had already absorbed.

**The irony is exact and worth stating.** `C-0101` is the claim that *established* the re-emission
discipline — *"re-emit it and amend the claim, never keep the stale file"* — and it is the claim that broke
it, inside its own sweep, in the same commit.

**And it was not found by looking.** An unrelated repair to a shared main source prompted a coordinator to
ask an agent for the **measurement** rather than its proof that the change was invisible. Four of five
consumers came back byte-identical; `T-157` moved 17 fields; a controlled A/B with the source restored to
`HEAD` returned a `T-157` **byte-identical to the repaired run**, proving the movement belonged to the input.
**A proof that a change is invisible is not a substitute for running the consumers, because the run also
checks everything the proof was not about.**

---

## 3. The other edge, and it is clean

`C-0101`'s eleven files contain **exactly two** dependency edges, and only one had been examined:

| edge | verdict | how |
|---|---|---|
| `T-157` reads `T-149` | **VIOLATED** | `CH-0131`, by re-run and controlled A/B |
| `T-138` reads `T-136` | **NOT STALE** | this claim, with **no solve at all** |

The second needs no solve because `anchoring/PathCountConsistencyStudy.kt` reads **eight named values** out of
`T-136`'s parameter block: compare them against `T-136`'s current file. Six of the eight are echoed in
`T-138` and **all six match exactly**; the other two are read but not echoed, and are reported as
unverifiable here rather than counted as agreeing.

**So the defect is one edge of two.** The falsifier — that the sweep was systematically wrong and the whole
set needs re-emitting — did not fire, and establishing that cost one comparison.

---

## 4. The instrument, which was already in the tree

[`tools/result-reader-census.py`](../../tools/result-reader-census.py) (`P-22`, `C-0082`) already derives the
read graph, **including the transitive edges a grep cannot see** — the class `C-0073`'s grep-based audit
missed and `CH-0092` corrected. Turning that graph into an order is twenty lines:

```
tools/reemission-order.py T-149 T-79 T-136 T-99 T-157 T-108 T-134 T-152 T-116 T-135 T-138
# 11 file(s), 2 dependency constraint(s) inside the set
#   T-136 must be re-emitted BEFORE T-138
#   T-149 must be re-emitted BEFORE T-157
```

**So the class was preventable with what the repository already had**, which is the sharper version of the
lesson than *"remember the rule"*. Kahn's algorithm, ties broken by name so two runs agree, cycles
**reported** rather than silently dropped, and an edge to a file outside the re-emission set correctly does
not constrain it. **11 self-tests**, run with `--selftest`.

It is deliberately **not** wired as a gate. It is a question you ask before a sweep, not a test that runs
after one — and `C-0082` records why the census check itself sits in `tools/verify.sh` rather than in Gradle.

---

## 5. The general gate, measured and declined

The tempting alternative is a **staleness gate on reproduction residuals** — `CLAUDE.md` records that such a
residual *is* a staleness detector and that `T-118`'s sat at `8.79e−7` for an iteration, unread.

Measured across the tree: **499 nonzero departures in 64 of 104 result files.**

The great majority are legitimate. A reproduction against a **literature** value is *expected* to differ —
Fields et al. at 15.3 %, Bosco's C2′-endo step at 5.1 %, Marras et al. at 19.7 % — and a reproduction against
a different **model** is expected to differ too. A gate on this signal would fire constantly on correct files,
which is the one failure `C-0080` says a checker cannot afford: *a drift checker's false positives cost more
than its true ones, because the tool exists in order to be believed.*

**Declined, with the number rather than by assertion** — the same discipline `C-0067` applied when it refused
to ship an approximation whose false-positive rate was unmeasured.

---

## 6. Validity range, and what this does NOT do

- **It does not re-emit anything.** The one stale file was already re-emitted in iteration 23, and the second
  edge is clean, so there is nothing to re-run.
- **It does not audit the whole corpus for staleness.** It checks the eleven files `C-0101` touched. A
  tree-wide staleness audit is what §5 prices and declines; the order tool makes the *next* sweep safe
  rather than certifying every past one.
- **The two values `T-138` reads but does not echo are unverified**, and are named as such.
- **The sorter is only as good as the census.** `C-0082` asserts the derived census is a **superset** of a
  naive grep and wires that assertion as a test, so it is the strongest graph available — but a read by a
  route outside it would leave the order incomplete.
