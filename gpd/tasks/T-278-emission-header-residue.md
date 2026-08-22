# T-278 — the emission header's residue, and the seven emitters that call no rounding function

| | |
|---|---|
| **Leaf** | `A8.2` — the remainder of step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | [`C-0172`](../claims/C-0172-typed-handles-and-the-emission-header.md) (`T-272`), which discharged `P2` whole and `P3`/`P4` on 72 of 128 emitting studies and **named** its 55-study residue rather than estimating it; and [`CH-0223`](../challenges/CH-0223-seven-emitters-call-no-rounding-function.md), filed measured-and-unrepaired in the same iteration |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### What is left

Two things, and they are one task because they are one sweep.

| | |
|---|---|
| **the residue** | 55 studies declare an emission header their committed result file does not carry, and one (`design.SimulatedTileCensusStudy`, landed by a concurrent agent while `T-272` was running) declares none at all. `tools/T-272-header-census.py --verbose` prints all 56 by name |
| **`CH-0223`** | seven studies write their result file through **no rounding function at all** — `output.writeText(json.encodeToString(result) + "\n")`, identical to the character in all seven — and they carry **41 297 of the corpus's 41 369** over-precise numeric leaves, 99.83 % |

They overlap: `T-6` and `T-7` are in both sets, so a sweep that took them separately would emit those two files twice.
Five of `CH-0223`'s seven (`T-1`, `T-1c`, `P-3`, `P-6`, `P-9`) already carry a header and are re-emitted only because their **rounding** changes.

### The judgement `CH-0223` declined to make, and where it actually lives

`CH-0223` §4 declines the repair because *"the digit count is a **judgement per study**"*, and it asserts one:

> `T-1c` and `T-1` are downstream of a solved SCF height and are determined to
> `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` or fewer (`P-18`, `CH-0043`), not to nine.

That is a claim about a **call graph**, so it is checkable rather than arguable, and this task checks it before it obeys it.
`P-18` states the rule in its own conventions block — *"PROVENANCE of an emitted number is the loosest solver tolerance on any path from a model input to it. Nine digits is defensible only where that is ≤ 1e−9"* — and applies it to six rounding **sites** by hand.
Applying it to these seven **studies** is the whole of the judgement.

### Acceptance predicates

| | predicate |
|---|---|
| **A1** | `tools/T-272-header-census.py` reads **`DECLARED-NOT-EMITTED 0`** and **`EMITTED-NOT-DECLARED 0`**, in one `tools/reemission-order.py` topological order whose dependency-constraint count is asserted **non-zero** and whose census is **derived fresh** and compared against the committed one before it is trusted (`C-0172` §2: the two graphs diverged from position 50 of 151) |
| **A2** | each of `CH-0223`'s seven emitters given a rounding call at a precision **argued per study from its own solver provenance**, with the argument in the claim; and where the precision cannot be defended, that study left **measurable** rather than guessed (`C-0092`) |
| **A3** | the change **simulated offline over the committed files before any JVM starts**, so the re-run is a *confirmation*: every field that moves is on the predicted list, and any field that moves and is not on it is a finding about the study rather than about the rounding (`C-0138`, `C-0150`) |
| **A4** | movement reported **by kind** (`numeric`, `prose`, `wording`, `departure`, `parameter`, `boolean`, `added`, `removed`) against `git show HEAD:<path>`, with a signature declared **before** the sweep and every `numeric` mover outside it controlled by a `--committed` re-run before it is called staleness |
| **A5** | the 24 Python emitters either reached by the header rule or **refused with a stated reason** |
| **A7** | the result emitted as JSON into `gpd/results/`, per step 4 of [`SESSION-PROMPT.md`](../../SESSION-PROMPT.md), **naming the corpus state it measured** — a file whose subject is the corpus is a function of a mutable tree, so a `baselineRef` is what makes it re-runnable (`CH-0212`, `CLAUDE.md`) |
| **A6** | every claim that **quotes** a moved number amended; never a stale file kept as *"the record of what the claim was written on"* |

### Units and conventions

Unchanged and locked. 300 K, `k_BT = 4.142 pN·nm`.
**No physics changes here**: every field this task moves is a **precision** or a **schema** field, and any field that moves for another reason is the finding.

---

## 2. Plan

### The cheap bound runs first, and it is offline

A rounding change is a **pure function of the committed document**, so the blast radius is derivable rather than discoverable.
`tools/T-278-rounding-simulation.py` mirrors `structure/ResultRounding.kt` — the parameter-record exemption, the `record/spelling` departure map, the integral-number rendering and the absolute floor — and predicts every moved leaf, in seconds, with no JVM.
Its self-tests are taken from that file's own KDoc examples, and one of them reproduces **`CH-0223`'s own 41 297** on the artifact it was measured on, which is what makes the mirror credible rather than merely plausible.

`tools/T-278-solver-provenance.py` is the other half: the closure of Kotlin sources reachable from a study's entry point under Kotlin's own visibility rule, and every named convergence criterion in it.
It **enumerates**, it does not decide — which of the enumerated constants is a solver tolerance and which is a verdict threshold (`FLATNESS_TOLERANCE = 0.10` is the latter and the corpus carries eleven) is a judgement per name in `T-225`'s shape, made in the claim.

### Sequencing

**One pass, one snapshot, one sorted order.** The set is the 56-study residue plus the five already-emitted studies whose rounding changes, 61 result files.
`tools/reemission-order.py` sorts it; the order is trusted only after (i) its constraint count is asserted non-zero and (ii) the committed census is checked against a fresh derivation.
`tools/study-batch.sh` is the vehicle — one snapshot, many runs, copy-back re-checksummed per run.

If the whole set does not fit the box, a **sorted prefix** is delivered and the residue **named** — never an unsorted subset, which is `C-0101`'s consumer-before-producer error.

### The falsifiers, declared before the sweep

| | |
|---|---|
| **F1** | **The signature of a header-only file is `added = 2` and every other kind `0`**, and of a rounded file `added = 2` plus exactly the `numeric` fields the offline simulation named. A `prose`, `wording`, `boolean`, `departure`, `parameter` or `removed` movement, or a `numeric` movement **not on the predicted list**, is either a defect of this change or a pre-existing irreproducibility, and must be controlled against `HEAD` before it is called either |
| **F2** | **The offline prediction is exact.** The set of `numeric` movers in the seven rounded files must equal the simulation's prediction, field for field. A prediction that is a strict superset means the study is not deterministic; a strict subset means the mirror is wrong |
| **F3** | **The committed reader census and a fresh derivation give the same order over this set.** They did not when `C-0172` looked, and it re-emitted `P-22` in the same commit; a disagreement now means that re-emission did not close |
| **F4** | **`CH-0223`'s SCF assertion is checkable and may be false.** If neither `T-1` nor `T-1c` names `SelfConsistentField` or `heightAtPressure` anywhere on its path, the six-digit hazard does not apply to them and the challenge's stated ground is wrong even where its count is right |
| **F5** | **A floor is a claim about units and it does not travel** (`P-18`). Every value the simulation reports as **flattened to exactly 0.0** must be either a quantity the physics says is exactly zero, or a quantity in the locked force units — and any that is neither means the study needs a lowered `floor`, not a different digit count |

### What would falsify this approach

- **A movement cannot be attributed to the emission rule or to the rounding.** Then something else changed; stop, and run the third control at `HEAD`'s own code that says whose defect it is (`C-0129`'s `T-136` control).
- **The offline mirror and the Kotlin disagree.** Then the prediction is worthless and the sweep is a discovery rather than a confirmation, and the mirror is the thing to fix.
- **A study will not run.** Then the residue was never a re-emission problem and the study is the finding.
