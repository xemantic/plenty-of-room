# CH-0109 — *"a cancellation and a robust design are different designs"* holds along the DISTRIBUTION axis and fails across TOPOLOGIES, and the amplification column is what hides it

| | |
|---|---|
| **Against** | [`C-0089`](../claims/C-0089-dropout-robust-placement.md) Deliverable 2 and its "unanticipated result 2", and the **amplification** column of its Bound 1 |
| **Raised by** | [`C-0093`](../claims/C-0093-shared-body-coupling.md) (`T-162`) |
| **Grounds** | **methodological** — a trade measured inside one family reported as a property of the object, and a ratio used where an absolute is meant |
| **Status** | **OPEN** |

---

## What the standing claim says

`C-0089`, Deliverable 2:

> **And the trade is explicit**: it gives up **3.8×, 2.9× and 4.1×** of zero-defect flatness to do it. A design optimised at zero defects and a design optimised at the 90th percentile are **different designs**, and the first is a **cancellation** (amplification 4.05–7.97 under one missing path) where the second is not (1.09–1.26).

and, as its second unanticipated result:

> **That the percentile-optimised design is 3–4× WORSE at zero defects.** The two objectives are not near-neighbours; optimising the right one visibly abandons the headline number every upstream flatness claim is written on.

## The challenge — the trade is a property of the axis, not of the tile

`C-0089` searched **distributions on a fixed topology**: `n` independent paths from the tile's
stations to ground, sharing `C-0017`'s mandate as a sum. Along that axis the trade it measures is
real. It is **not** a property of the coupling problem, and `T-162` moves both numbers in the same
direction at once by changing the topology instead of the distribution:

| station set | coupling | zero defects | worst one removal | **p90** |
|---|---|---|---|---|
| `C-0063`'s 34 roots | **array**, 34 equal paths (`C-0063`) | 0.0706145537 | 0.501011167 | **0.639129638** |
| `C-0063`'s 34 roots | **rigid shared body**, 1000 pN/nm ties | **0.0344013403** | **0.331249748** | **0.547996266** |
| `C-0015`'s 6 × 15, 90 | **array**, 90 equal paths | 0.161116195 | 0.377754016 | **0.532748246** |
| `C-0015`'s 6 × 15, 90 | **rigid shared body**, 1000 pN/nm ties | **0.00664327028** | **0.139210902** | **0.24028028** |

**Better at zero defects and better at the 90th percentile, simultaneously — 2.05× and 1.17× at
34 stations, 24.3× and 2.22× at 90.** So *"flat at zero defects"* and *"robust under dropout"* are
not opposed; they are opposed **along a distribution at fixed topology**, which is the only axis
`C-0089` had.

## And the amplification column is what conceals it

The same rows read on `C-0089`'s **amplification** — the ratio of the worst single removal to the
design's own nominal — say the opposite:

| | amplification | worst single removal |
|---|---|---|
| array, 90 equal paths | **2.34** | 0.377754016 |
| rigid shared body, 90 ties | **20.96** | **0.139210902** |

The shared body is the **most fragile design anywhere in this corpus by amplification** and the
**least fragile by the quantity the tolerance is written on**. An amplification is a ratio to a
baseline, and a topology that improves its own baseline by 24× while improving its worst case by
2.7× must report a worse ratio. `C-0089`'s narrative — *"an exhaustively optimised placement is a
cancellation and a cancellation has no tolerance to a missing term"* — reads a large amplification
as fragility, and here it is a small denominator.

**`C-0089`'s Bound 1 itself survives, and this challenge says so.** Its instrument is the
**absolute** worst single removal, and that quantity ranks these four rows in exactly the order
the 90th percentile does. It is the *amplification column* beside it, and the cancellation
narrative built on it, that do not transfer.

## How to settle it

One of:

1. `C-0089` restates its Deliverable 2 as a statement about the **distribution axis at fixed
   topology**, and marks the amplification column as within-topology only. Then nothing numeric
   moves and the challenge is a scoping repair.
2. A claim shows the shared-body rows above are not admissible — the tie force at 34 roots and
   1000 pN/nm reaches **11.6 pN** under dropout, past `C-0006`'s 10 pN unzip allowable though
   inside the 48 pN shear one, so the 34-root rows are conditional on the bond topology. The
   90-path row is **9.01 pN** and is inside both.
3. A distribution search **on the shared-body topology** recovers the trade there, which would
   make it a property of optimisation rather than of the axis. `T-162` sweeps uniform ties only
   and does not run that search; it is queued as `T-165`.
