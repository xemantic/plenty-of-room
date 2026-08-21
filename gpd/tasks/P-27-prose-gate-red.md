# P-27 — the prose gate is RED, and it reported clean at the very commit that wired it

| | |
|---|---|
| **Leaf** | none — a **process** task protecting the machine-readable artifact of every leaf |
| **Raised by** | the coordinator, iteration 39, while establishing whether the tree was fit to restructure on |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The defect, measured

`tools/check-result-file-hygiene.py --prose` at `HEAD` exits **1** and reports

```
69 token(s) in 44 string field(s) in 8 file(s), of 147 scanned
    33  T-134-plan-tolerance.json          9  T-139-duplex-pair-separation.json
    17  T-12-lateral-confinement.json      4  T-108-desired-stroke-reach.json
     2  T-126-arm-slab-clearance.json      2  T-152-collinear-clearance.json
     1  T-159-doubling-ladder-repair.json  1  T-50-beyond-mean-field-gap.json
```

That line is a **gate**, not an audit — `tools/verify.sh` runs it last and `T-250`/`C-0156` promoted it there
on the strength of the sweep reading *"0 in 0"*.
So `tools/verify.sh` has been red since the commit that made it a gate.

### Why it is red, and it is not a regression

Three facts, each one `git` command and no solve:

- **the checker has not changed since `49b1a01`** — `git diff 49b1a01 HEAD -- tools/check-result-file-hygiene.py`
  is empty, so this is not `CH-0204`'s widened guard arriving late;
- **the result corpus has not changed since `49b1a01`** either, apart from `T-9` being **added**, and `T-9` passes;
- **all eight of those studies had their emitters swept in `49b1a01` itself** — `ArmSlabClearanceStudy`,
  `CollinearClearanceStudy`, `LateralConfinementStudy`, `PlanToleranceStudy`, `BeyondMeanFieldGapStudy`,
  `DuplexPairSeparationStudy`, `DoublingLadderRepairStudy`, `DesiredStrokeReachStudy`.

**The source is clean and the artifacts are stale.**
The sweep repaired the call sites of forty-seven studies and re-emitted thirty-nine of them.
It is `CLAUDE.md`'s standing rule — *when a repair moves a downstream result file, re-emit it* — failing on the
producer instead of the consumer, and it is the reason a gate must be run against the **committed corpus** and
never against the sweep that produced it.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | the eight files re-emitted, and `tools/check-result-file-hygiene.py --prose` reading **0 tokens in 0 files** against the committed corpus |
| **P2** | what moved reported **by kind** against each file's committed version (`git show HEAD:<path>`) — prose digits / numeric / verdicts and wording / booleans / added / removed — with **zero** numeric movement expected and any exception named |
| **P3** | `CH-0205`'s channel checked **before** re-emitting, not after: for each of the eight, whether any consumer parses one of its **string** leaves back with `.toDouble()`. `T-108`, `T-139` and `T-50` have readers; the other five have none |
| **P4** | the uncommitted `T-149` `armLength` exemption (emitter + `PROSE_ALLOWLIST` + its two named tests) committed in the same change, since it is the one live instance of `P3`'s channel. **Already done in the working tree and not committed**: the emitter writes full precision again, the allowlist and its two mutation rows are in place, and the re-emission has run — `gpd/results/T-149-recommended-element-fold.json` carries `8.164390826631301` against the committed `8.16439083`. It is a **fourth** modified file to carry, not a task |
| **P5** | a stated reason the sweep's own verification passed — a `--committed` control, or the snapshot copy-back it implies — so the failure mode is closed and not merely cleared |
| **P6** | `tools/verify.sh` green end to end, and the `## Number reservations — iteration 39` block updated with the claim actually filed |

### Units and conventions

Nothing physical is computed.
Every re-emission re-runs its study's own solves; **no physics may move**, and `P2` is how that is checked.

---

## 2. Plan

### The cheap bounds run first, and both are already done

[`tools/reemission-order.py`](../../tools/reemission-order.py) over the eight reports **0 dependency
constraints inside the set** — any order is safe, and they can run in parallel.
That output is trusted only because the tool's `--selftest` passes **and** a `T-157`/`T-149` control returns
the known edge; `C-0153` found this same tool silently reporting `0` on a path argument, so an unchecked zero
is exactly the failure this task must not inherit.

The reader census bounds the blast radius: **T-108** has two readers
(`anchoring/RangeRobustPlacementStudy.kt`, `window/SecondResynthesisStudy.kt`), **T-139** one
(`tile/ForcedCrossoverPriceStudy.kt`), **T-50** one (`electrostatics/PlanarCouplingWallStudy.kt`), and
**T-12, T-126, T-134, T-152, T-159 have none**.
A prose-only change cannot move a consumer that parses **numeric** leaves, so those four consumers need
re-running only if `P3` finds a string leaf being parsed.

### Cost

Read out of the runbook rather than guessed: `T-108` ~2 min, `T-152` ~2 min, `T-159` ~3 min, `T-126` ~5 s;
`T-12`, `T-134`, `T-139` and `T-50` had **no `Entry points` row at all** until iteration 39 added three of them,
which is `P-28`'s finding and the reason this task could not be started from `TASKS.md` alone.
One `tools/study-batch.sh` snapshot, eight sequential runs, copy back **only** the eight files each run wrote.

### What would falsify this approach

- **A numeric field moves.** Then the eight files were stale in more than their prose, the sweep is not the
  whole story, and the movement is a finding rather than a repair — report it before clearing the gate.
- **A consumer parses a string leaf of one of the eight.** Then `CH-0205` is live in more than `T-149`, the
  rendering decision is a decision about an **input**, and the exemption mechanism needs to generalise.
- **`--prose` does not reach 0.** Then the gate's predicate is wider than the sweep's repair, `C-0083`'s
  *a gate that cannot come clean is not a gate* applies, and the honest move is to print the residue ungated.
