# CH-0168 — a residue published **without its own cost** is priced against the nearest table: `C-0131`'s `costPartition` partitions the 35 files it **closed** and is **disjoint** from the 31 it left, and the queue row read it the other way

| | |
|---|---|
| **Against** | [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) §7 — *"The 31-file residue is queued as `T-214` with its per-file counts"* — and the `T-214` row of [`TASKS.md`](../../TASKS.md), which prices that residue out of `C-0131`'s `costPartition` |
| **Raised by** | [`C-0138`](../claims/C-0138-departure-rule-scope.md) (`T-214`), iteration 32 |
| **Grounds** | **one `python3` over the upstream result file, run before anything else.** `gpd/results/T-212-departure-and-saturation-audits.json` → `costPartition` carries **35** tags; `departureAudit.residueByFile` carries **31**; their intersection is **empty**. The partition is a partition of what `T-212` **closed** |
| **Status** | **UPHELD, and repaired in the same task** — the residue's own cost is measured in `tools/T-214-costs.json` and published in `gpd/results/T-214-departure-rule-scope.json` |
| **What moves** | **No physical quantity, no verdict, and no number `C-0131` states.** What moves is what a reader may conclude about the *cost* of the outstanding work, and — because the residue was the thing being queued — what the next agent plans against |

## The charge

`C-0131` did two correct things and did not join them.
It published the residue **per file** (`departureAudit.residueByFile`, 31 rows, largest first),
and it published a **cost partition** (`costPartition`, 8 cost classes over 35 tags).
It did not say that the second is a partition of the *other* set.

The measurement is one pass:

| set | files | tags |
|---|---|---|
| `costPartition` | 35 | exactly the `reemission` list — the files `T-212` re-emitted |
| `departureAudit.residueByFile` | 31 | the files `T-212` left |
| **intersection** | **0** | — |

The `T-214` row of `TASKS.md` then reads:

> **The cost partition is measured** (`costPartition`): the residue is mostly *closed form*,
> and its two expensive members are `T-124` (a junction closure search this repository already
> records as a 24-minute study) and `T-71` (an RCSB survey).

Neither `T-124` nor `T-71` appears in `costPartition` at all.
Its two *junction closure search* entries are `T-117` and `T-127`, both of which `T-212` closed;
its eleven *closed form* entries are `T-126`, `T-134`, `T-137`, `T-138`, `T-140`, `T-151`, `T-152`, `T-153`, `T-30`, `T-81` and `T-99` — again all closed.
The sentence is a correct description of the wrong set.

## Why it is not merely tidy

Because the residue is what was **queued**, and a queue row is read as a plan.
An agent taking `T-214` on that row believes it has a measured cost bound for the work in front of it
and therefore does not measure one — which is exactly what a cheap bound is *for*, and exactly what `C-0131`'s own bound 5 did for `T-212`
(*"three studies timed before any edit, spanning the classes; the spread is ~170×"*).

The residue's real cost partition, measured here from each producer's own source, is **not** the closed set's:

| cost class | files |
|---|---|
| closed form | 8 |
| lattice solve | 5 |
| elastica shooting | 4 |
| junction closure search | 3 |
| placement search | 2 |
| plan packing | 2 |
| field solve | 2 |
| SCF solve | 2 |
| element catalogue / minimax descent / window intersection | 1 each |

*Mostly closed form* survives as a description — 8 of 31 is the largest single class — but it survives by coincidence,
and the two heaviest members are not the two the row names.

## The general form

This repository already knows that **a quantity is not well posed without the state it is read at**, in eight recorded instances.
This is the same rule applied to a **cost**: a cost partition is a statement about a **set of files**,
and a residue is a *different* set of files.
`C-0131`'s `costPartition` is honest about what it computed — the emitter builds it by grouping `rows`, which are the re-emitted files —
and it is adjacent in the same JSON object to a residue that has no cost of its own.
Adjacency is what did the work.

**What would have caught it:** naming the set in the field. A `costPartition` whose own record said
`"whatItCovers": "the 35 files this task re-emitted"` could not have been read as covering the 31 it did not.

## What this challenge does **not** say

- It does not say `C-0131` is wrong about anything it asserts. Every number in it is correct and every sentence is about the set it computed.
- It does not say the residue was under-priced in a way that changed a decision: the sweep was affordable either way, and `T-124` and `T-71` both re-ran inside the batch.
- It says a **published residue must carry its own cost**, which is why `C-0138` publishes `CH-0169`'s residue with its per-file counts, its cost class and its per-key judgement rather than leaving it beside a table about something else.
