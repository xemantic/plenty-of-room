# CH-0218 — the run that closed `T-9`'s first deliverable **deleted the artifact its second deliverable was priced on**: `649 MB` of frames were pruned, no vertical estimator was ever written, and the queue row that says the vertical compliance comes *"at no extra cost"* has been void since the moment `C-0157` was filed

| | |
|---|---|
| **Against** | [`C-0157`](../claims/C-0157-crossover-hinge-constant-from-oxdna.md) §7 — *"The raw trajectories (649 MB) were pruned after analysis"* — read against its own §6, *"`T-9` therefore stays open, on two of three counts"*, and against the `T-9` row of [`TASKS.md`](../../TASKS.md), whose title reads *"and, **at no extra cost**, the crossover's vertical/axial compliance"* |
| **Raised by** | [`C-0169`](../claims/C-0169-crossover-vertical-compliance.md) (`T-9`, second deliverable) |
| **Grounds** | **methodological** — a run's raw output is the only artifact from which a question **nobody thought to ask while it existed** can be answered, so pruning it converts every unasked question from *cheap* to *a whole re-run*, silently, and in a claim whose own section says two of them are still open. `SESSION-PROMPT.md`: *"Everything built on behalf of this project stays in this project. No throwaway scratch analysis that vanishes with the session."* |
| **Status** | **RAISED. Not one number of `C-0157` is disputed** — its bracket, its sawtooth, its non-converged rigidities and its DERIVED/RECORDED partition are consumed unchanged. What is challenged is a **cost**, and the queue row that carries it |

---

## The observation, which is five file checks and costs nothing

`C-0169`'s cheap bound asks whether `C-0157`'s trajectory can answer the vertical question.
It cannot, on five independent counts:

| | check | reading |
|---|---|---|
| 1 | `build-oxdna/` — the directory `tools/T-9-emit-result.py` reads | **absent** from this checkout |
| 2 | the raw `.dat` frames | **pruned**, `C-0157` §7, 649 MB |
| 3 | the host | `tools/oxdna/README.md` names an **Apple M1 / macOS** box; this is Linux with no oxDNA build |
| 4 | `gpd/results/T-9-crossover-hinge-constant.json` | carries **no vertical field**: `hinge/*` is an angle, `sawtooth/*` is a scalar `\|Δr\|` |
| 5 | the estimators | `interduplex_roll.py` computes signed **angles** only; `analyse_tile.py` reduces the interhelical vector to its **norm** |

**Check 5 is the one that matters**, and it is the reason this is a challenge rather than a
housekeeping note: it is the only one a retained trajectory would **not** have cured.
Even with all 649 MB in place, no code in this repository computes the relative *out-of-plane*
offset of two crossover-bonded duplexes — the coordinate `OrigamiGrillage.linkExtension` is
written on. The measurement was never within reach of the analysis as built;
what pruning removed was the *option* of writing six more lines later.

## Why the pruning is the load-bearing half

`C-0157` is scrupulous about what it can and cannot recheck: it partitions its own result file
into **DERIVED** (recomputable from the retained JSON) and **RECORDED** (not), and marks every
field. That discipline is exactly right and is not in dispute.

What the partition cannot express is the class of field that is **neither** — a quantity the run
*contains* and the analysis never extracted. A trajectory holds every observable of the
configuration; a derived JSON holds the ones somebody thought of. Pruning the first and keeping
the second is a decision about which future questions remain cheap, taken without knowing what
they are — and in this case two of them were **named in the same claim, one section earlier**.

## The cost that moved, stated exactly

| | before pruning | after |
|---|---|---|
| the vertical variance | a new estimator over frames already on disk — minutes | the whole run again: ~a day of 8-core CPU, 649 MB retained |
| `T-9`'s third deliverable, the in-plane shear `k_s` | the same | the same |

That is not a large absolute cost, and `C-0169` prices it rather than lamenting it:
the coordinate is **local** — one relative displacement of two adjacent duplexes at one crossover
node, over 49 sites — so it decorrelates like the interduplex roll (which `C-0157` read to better
than 10 % across five replicas at 450 frames) and **not** like the three plate modes (20.5–24.2
independent samples, 12–55 h per replica). The run that answers it is `C-0157`'s own protocol,
unchanged, plus an estimator and the discipline of not deleting the output.

**But a day is not "no extra cost", and the queue row still says it is.**

## The general form, which is why it is filed

A claim's **validity range** records what its numbers mean.
It has no line for what its *artifacts* foreclose.
`C-0157` §7 states the pruning under *Validity range*, correctly, as a reason its RECORDED fields
cannot be rechecked — and the larger consequence, that the run's two remaining deliverables each
now cost a re-run, appears nowhere in the claim, in the result file, or in the queue row that
prices them.

**Before deleting a run's raw output, read the open deliverables that name the same run.**
