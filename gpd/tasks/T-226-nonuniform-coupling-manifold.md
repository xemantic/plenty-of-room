# T-226 — `T-113` is irreproducible run to run, and `C-0058`'s multi-state minimax numbers quote one member of a manifold

| | |
|---|---|
| **Leaf** | none — a **process** task about the reproducibility of an emitted artifact and the numbers a claim quotes from it |
| **Raised by** | [`C-0138`](../claims/C-0138-departure-rule-scope.md) §8 (`T-214`), whose `F1` fired on this file |
| **Verification type** | **logical** (a structural classification of every moving field) **+ in-silico** (repeated emissions of `coupling.NonUniformCouplingStudyKt` at `HEAD` and under `C-0135`'s cure, diffed field by field) |
| **Units** | dishing is a **fraction of the stroke**, dimensionless; stiffnesses pN/nm; forces pN; `k_BT = 4.141947 pN·nm` at 300 K |

## Formulate

`C-0138` §8 measured that two runs of **identical code** on **identical inputs** in **one** `--committed` snapshot disagree in **217** fields of
`gpd/results/T-113-non-uniform-coupling.json`, and that three emissions give three different points
(committed↔A **223**, committed↔B **6**, A↔B **217**).
The moving block is never scattered — it is **one descent record and its transfers**, and a *different* one each run.
No verdict, boolean or prose wording moves in any comparison.

`C-0058`'s headline **0.0753** — the 3 × 15 single-state optimum the design window quotes — is identical in every emission.
What moves is `C-0058`'s **multi-state minimax** row: `0.1247`, `0.1286`, `0.1195`, `0.1307`, `0.6118`, `3.115`, `9.346`, `1.082`.
**`0.1247` has reached `CLAUDE.md` and `ANSWERS.md`.**

`C-0135` measured the same object on `T-129` and supplies a cure — periodic restarts, a small-difference restart,
a lattice snap on the iterate, decisions at six significant digits with the earlier candidate winning ties —
and also supplies the legitimate alternative outcome: *measure the width instead*.

### Acceptance predicate

Either

- **(a)** `C-0135`'s cure applied to `coupling`'s descent so that two fresh emissions of `T-113` agree on the previously-moving fields; or
- **(b)** a **measured manifold width per moving record**, with `C-0058` re-quoted against it and every downstream carrier of `0.1247` restated to carry the width.

`P1` The irreproducibility reproduces: two fresh runs of `HEAD` disagree, and the moving block is one descent record and its transfers.
`P2` The moving fields are classified VALUE / POINT / OTHER, per `C-0135`'s own classification, and `OTHER` is **0**.
`P3` No verdict, boolean or `bindingStates` list moves in any comparison.
`P4` The width of `C-0058`'s multi-state minimax numbers is **measured** over an ensemble of at least three emissions, at both the VALUE and the POINT.
`P5` Every corpus carrier of a moved number (`C-0058`, `CLAUDE.md`, `ANSWERS.md`, `TASKS.md`) carries the width or a pointer to it.
`P6` If a repair moves the emitted file, every consumer is re-emitted in `tools/reemission-order.py`'s order and `C-0058` amended where it **quotes** a moved number.

## Plan

**Cheap bound first.** Before touching a descent:

1. `tools/result-reader-census.py` for the consumers of `T-113` — the re-emission cost is a **fact about the graph**, and it decides whether a repair is affordable at all.
2. Read `coupling/RobustDistribution.kt`'s terminal-selection line — `C-0135` names it exactly — and check whether `C-0135`'s cure is even applicable to `NonUniformCouplingStudy`'s descent, which may be a different optimiser.
3. Grep the corpus for every carrier of the eight moving numbers, **before** deciding between (a) and (b): if the carriers are few, (b) is cheap and honest; if a repair moves the file, (a) costs a full re-emission sweep.
4. Only then run the ensemble.

`C-0135`'s own §7 is the standing warning: *removing the manifold would mean changing the descent's terminal selection — which moves published numbers, for a movement that changes no verdict.*
A legitimate outcome of this task is therefore **(b)**, and it is a full answer.

### What would falsify this approach

- `F1` — two fresh runs agree, i.e. the irreproducibility does not reproduce and `C-0138` §8 measured something else (a snapshot artefact).
- `F2` — a moving field is neither a VALUE nor a POINT of a descent, i.e. `OTHER > 0`, which would make it a defect and not a manifold.
- `F3` — a verdict moves between two emissions. Then it is not cosmetic and (b) is not available.
- `F4` — the measured width crosses `T-5b`'s 0.10 flatness convention, which would make the choice of manifold member a **design** decision.
- `F5` — `C-0135`'s cure applies cleanly and collapses the manifold, in which case (b) was the wrong branch and (a) is owed with its re-emission sweep.
