# C-0097 — `C-0091` found by hand that two of six "independent" routes were the other four re-read. Mechanised, the detector recovers **both** of its findings — the exact match to its own `2.66e-08` and the subset it reports as 12 of 72 at departure `0` — and the two halves cost **1.0 s** and **271 s**, which is why one is a gate and the other is not

| | |
|---|---|
| **Task** | [`T-158`](../../TASKS.md), both halves — raised by [`C-0091`](C-0091-buffer-route-census.md)/[`CH-0106`](../challenges/CH-0106-six-routes-are-three.md); taken by the coordinator in iteration 19 while `T-162`, `T-161` and `T-159` ran in parallel |
| **Verification type** | **logical** (a mechanical audit of the result corpus, with the detector verified by 24 in-memory fixtures and its **recall measured against the two instances already known**) |
| **Verdict** | **PASS on both halves.** *(a)* `C-0016`'s two clauses **cannot** be stated as one sentence about the buffer and the deliverable must name which reading it means — they are the **force-pinned** and **fixed-bias** readings of the same layer, and the acceptance's second branch is the correct one. *(b)* The route census **should** become a tool, and it is one: [`tools/result-transfers.py`](../../tools/result-transfers.py) recovers **2 of the 2** transfers `C-0091` found by hand — `T-16`↔`T-25` at **2.59e−08 / 2.66e−08** over 54 and 18 values, and `T-2` ⊂ `T-3` at **12 of 72, departure 0, under the same key name on both sides**. |
| **Maturity** | **TRL 1–3**, and below it: nothing here is physics. It is a property of this repository's own result files. **No number, verdict or premise of any claim is touched.** |
| **Provenance** | [`tools/result-transfers.py`](../../tools/result-transfers.py); **24 checks** in [`tools/test-result-transfers.py`](../../tools/test-result-transfers.py), written test-first; runtimes measured on the analysis box over 84 result files |
| **Conditions** | The corpus at commit `17ed640`: 84 result JSONs. Tolerance `5e-5`, matching `EMITTED_FIELD_SLACK`; minimum series length 5. |
| **Consumes** | [`C-0091`](C-0091-buffer-route-census.md) (the finding, and the fifteen-line hand version in `synthesis/BufferRouteCensusInputs.kt`), [`C-0082`](C-0082-result-reader-census.md) (the code-level companion), [`C-0073`](C-0073-determined-precision-of-a-result-file.md) (the emission precision the tolerance is set from) |
| **Constrains** | Nothing directly. It is an instrument, and its output is **advisory**. |

---

## 1. Half (a): the two clauses are two questions

`C-0016`'s §(e) bias window prefers **0.5 mM** — and is `C-0012`'s own number — while its §(f)
stability count, read at a **fixed applied bias**, prefers **2 mM**: 86.08–109.99 pN/nm of coupling
demanded at 0.25 V against 47.63–71.54, and 1 model unstable against 0 at 0.05 V.

**This is not a contradiction and it should not be reconciled into one sentence.** `CLAUDE.md`
already carries the mechanism: at a **held** operating point the force balance pins `|F_es|`, so a
change in the buffer is absorbed into the bias and the layer sees the same force; at a **fixed
applied bias** nothing is pinned and the buffer moves the force directly. The two clauses are the
same layer answering two different questions, and the buffer's sign genuinely differs between them.

So the acceptance's **second branch** is the right one: *they answer different questions and the
deliverable must name which.* What was wrong was never the physics — it was that **no claim states
the pair**, so a synthesis inherited whichever half its author was reading. The naming sentence is
added to `ANSWERS.md`; the claims are untouched.

## 2. Half (b): the detector, and why the unit is a SERIES

`C-0091`'s finding is invisible to every other instrument in this repository, and the reason is
structural:

> **A synthesis that reads CLAIMS cannot see a transfer.** Each of the six claims states its route
> truthfully. Only the result JSONs show that two of them are one number.

It is also invisible to `tools/result-reader-census.py`, which finds dependencies that flow through
**code**. A transfer found here flowed through a **person**.

Three design decisions, each forced by the live instance:

| decision | why |
|---|---|
| the unit is a **series**, never a single number | a lone coincidence between two solved numbers is meaningless in a corpus of 84 files; a whole array matching elementwise is not |
| the comparison is a **tolerance**, and the departure is **reported** | one file printed eight significant digits where the other printed nine, so `==` would have said *not a transfer* — this is the whole point |
| series are filtered for **distinctiveness** | 2.69, 0.34, 300, 4.141947, 45, 34 recur legitimately everywhere; a detector that reports conventions is one nobody reads. A constant series, and a series whose every value is a multiple of 0.5 (a grid, an index, a count), are dropped |

## 3. The measured recall, which is the only reason to believe it

`CLAUDE.md` requires that a negative existence result over a search carry its expected yield. The
same discipline applied forward: the detector was built after the instances were known, so the
honest test is whether it finds them **without being told where to look**.

| `C-0091`'s hand finding | what the tool reports | |
|---|---|---|
| *"`T-25` carries `T-16`'s `stabilityMargin` … at 20 of 20 at `2.66e−8`"* | `T-16/requirements[]/stabilityMargin` ↔ `T-25/couplingMargins[]/marginBaseline`, 18 values, departure **2.66e−08**; and `stabilityFloor` ↔ `floorBaseline`, 54 values, **2.59e−08** | **found** |
| *"`T-2`'s bias figure IS `T-3`'s own number at 15 of 15 … at departure `0.0`"* | `T-2/biasClauses[]/biasForHundredPiconewtonBlocking` ⊂ `T-3/thresholds[]/biasForHundredPiconewtonBlocking`, **12 of 72, departure 0** | **found, by the subset matcher** |

**Recall 2 of 2.** The second needed a second matcher and is the more important half of the design:
the equal-length matcher misses it entirely, because `T-2` **selects** the twelve states it needs
out of `T-3`'s seventy-two. **A synthesis quoting a subset of another study's output is still
quoting that study, and it is the commoner shape.**

## 4. The cost, and why only one half is a gate

| matcher | runtime over 84 files | reported |
|---|---|---|
| exact, equal length | **1.0 s** | 123 shared series over 39 file pairs |
| subset, containment | **271 s** | 873 contained series |

**260× the cost and 7× the output.** So the exact matcher is cheap enough to be a gate and the
subset matcher is not, and — more importantly — 873 hits is an **audit** result, not a gate result:
its precision is low by construction, because a short series sits inside a long one easily. It is
behind `--subsets` and is the right instrument for a specific question (*is this claim's number
someone else's?*), never for a pass/fail.

Neither is wired into `tools/verify.sh`, and that is deliberate:

> **A transfer is not a defect.** It is a defect only when someone **counts** the two files as
> independent evidence, and no tool can see that. A gate that fires on a legitimate shared input
> would be turned off within an iteration.

## 5. What it does not do

- It cannot tell a **transfer** from a **shared input**. Much of the 123 is exactly that — a
  foundation sweep or a grafting-density grid appearing in two studies that both consume it. The
  reader-census answers that question and this tool does not.
- It compares **values**, not meanings. Two studies that legitimately compute the same physical
  quantity independently will match, and that is corroboration rather than duplication — which is
  the case the whole exercise exists to distinguish, and it still needs a person.
- It sees only what is **emitted**. A number that lives in a claim's prose and in no result file is
  outside it, which is `C-0088`'s territory.
