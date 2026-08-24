# T-316 — A distribution SEARCHED at the resolved per-bond link, against the `0.198 %` the tightest cell misses by

**Leaf:** `A8.2`
**Raised by:** [`C-0208`](../claims/C-0208-a-bond-link-is-two-mechanisms.md) (`T-310`) §2 and §11,
which sharpens [`C-0205`](../claims/C-0205-what-link-stiffness-the-recovery-needs.md)'s own last open question with a number.
**Reserved claim:** `C-0212`. **Reserved challenges:** `CH-0272`, `CH-0273`. **Reserved queue rows:** `T-324`, `T-325`.

---

## Formulate

### The standing state

`C-0167` re-graded every coupled cell in this corpus onto the honeycomb grillage and read **`0 of 64`**.
`C-0205` bisected the link stiffness the recovery needs and found the crossover connector cannot supply it.
`C-0208` resolved the link by the bond's own direction and read `0 of 64` at every one of five radial rungs —
and its tightest cell, at the radial bracket **floor** `754.005141 pN/nm`,
misses `T-5b`'s `0.10` by **`0.198 %`**: `0.100198485` of the free-tile stroke,
at `f = 0.30`, the abstract grid on the rooting helices, `5 × 10 = 50` paths, rim-graded 5:1.

Every one of those 64 cells — and every cell of every coupled census in this repository —
is graded on exactly **two** distributions:
`C-0058`'s **equal springs** and its **rim-graded 5:1**.
Both are *rules transferred onto* the lattice.
Neither is an *optimum of* it.

`CLAUDE.md` records the size of that difference where it has been measured:
*"projection onto a constrained family is not optimisation within it, and here the gap is 24.9 %"*.
The gap this task has to close is **`0.198 %`**.

### The question

1. Does a distribution **searched** at the resolved per-bond link put any coupled cell inside `T-5b`'s `0.10`
   at the 90th percentile of `C-0087`'s measured staple dropout — the statistic every census here is graded on?
2. If one does, what does it cost in **buildability** — `C-0060`'s measured `3.5 ≤ R ≤ 20` window on the
   max/min per-path stiffness ratio — and in **fragility**, which is the price `CLAUDE.md` attaches to
   an optimised cancellation?
3. Is the searched answer **out of sample**? A percentile optimised on the ensemble it is graded on is
   not a result; the search must see a different stream from the one the verdict is read on.

### What is fixed and what moves

**Fixed**, and inherited unchanged from `C-0167`/`C-0180`/`C-0205`/`C-0208`:
the `10 × 6` cross-section, the `116 bp` block extent, the drawable `102 / 109` raster,
the 435 staple bonds and 59 raster turn ties, `C-0022`'s solved collar,
`C-0017`'s mandate at the acceptable clause, `C-0087`'s measured depth incorporation,
seed `197197` and 4 000 realisations for the **grading** ensemble, the `81 × 81` dishing grid, `T-5b`'s `0.10`.

**Moves**: the per-path stiffness vector, subject to `C-0017`'s mandate on the **sum**.
That is the only design variable this task opens.

### Numeric targets

| # | target |
|---|---|
| `P1` | the **cheap bound**, before any search: the oracle `p90` dishing floor at every cell — a rigorous per-realisation lower bound over *every* distribution whatever — with the statement of what it can and cannot decide |
| `P2` | a distribution **searched** at every one of the 32 cells of the radial bracket **floor** rung (4 placements × 4 column counts × 2 composite fractions), by a search that composes `C-0135`'s smoothed-minimax cure with a true-percentile polish, graded **out of sample** on the grading ensemble |
| `P3` | the **in-sample / out-of-sample gap** at every searched cell, emitted, so that an over-fit is seen rather than inferred |
| `P4` | the **max/min stiffness ratio** the argmin demands at every searched cell, against `C-0060`'s measured `3.5 ≤ R ≤ 20` — the threshold the moving quantity feeds that a flatness falsifier alone cannot see |
| `P5` | the **fragility** of any cell that clears: its worst single-path removal, and its `p90` when quantised onto `C-0060`'s two levels |
| `P6` | the tightest cell re-searched at **all five** of `C-0208`'s radial rungs, so the answer is a property of the question and not of one rung |

### Acceptance predicate

The task passes when `P1`–`P6` are discharged and the claim states plainly
whether a **searched** distribution clears `T-5b` at the 90th percentile at any cell,
at what stiffness ratio, at what in-sample/out-of-sample gap,
and — if one does — what it costs in buildability and in fragility.

A negative is as much a result as a positive: it closes the last live route to a flat coupled cell
that does not change the body, the link or the specification.

### Units and conventions — locked before deriving

- nm, pN, pN/nm, pN·nm, pN·nm/rad, pN/nm² (= 1 MPa). `k_BT = 4.141947 pN·nm` at `T = 300 K`, aqueous 2 mM MgCl₂.
- `W` positive **downward**, toward the electrode (`C-0006`); a coupling's support force is upward and enters as its negative.
- `s` along the helices, `y` across them in the face plane (row pitch `3d/2`), `z` through the thickness (layer pitch `d√3/2`).
- Honeycomb `d = 2.536 nm`; rise `0.34 nm/bp`; `k_θ = 13.5294118 pN·nm/rad` at `α = 1`.
- The link is resolved per bond: `k_link = k_radial·unitZ² + k_transverse·unitY²` (`C-0208`), with
  `k_transverse` pinned at `C-0205`'s ceiling `254.80809548301096 pN/nm` throughout, and `k_radial`
  at `C-0208`'s five rungs — `254.808095` (the control), `548.995464`, **`754.005141`** (the bracket floor,
  the headline rung), `1530.48954`, `1735.49922`.
- `C-0017`'s mandate is an **equality on the sum**, `MANDATED_TOTAL_STIFFNESS = 100/3 pN/nm`;
  every distribution here sums to it exactly, so the search is a **redistribution of a fixed budget**.
- **Dishing** is `|w − affine fit|` peaked over the `81 × 81` face grid and divided by the free-tile stroke,
  exactly as `C-0167` and `C-0208` read it.
- The **grading** ensemble is seed `197197`, 4 000 realisations — `C-0208`'s own, so its published cells reproduce.
  The **training** ensemble the search sees is seed `316316`, 200 realisations, and is **disjoint in seed**
  from the grading one, so every quoted percentile is out of sample.
- A stiffness **ratio** is `max/min` over the per-path vector.

---

## Plan

### The cheap bound runs first, and it is two things, neither of which needs a search

**Bound 1 — the oracle `p90` floor.**
`InfluenceSurrogate.reachableDishingFloorAt` optimises over attachment **force** vectors, which is a
relaxation of optimising over **stiffness** vectors: dishing is affine in the forces, every distribution
produces some force vector, and the peak of a sampled field is never below its own root mean square.
Restricted to a realisation's survivors it is therefore a **pointwise** lower bound on the peak dishing
of every distribution whatever, so the `p90` of the floor sample is a lower bound on the `p90` of any
distribution's own sample — order statistics being monotone under a pointwise inequality.
It costs `O(n³)` per realisation from one precomputed Gram matrix and **no search at all**.
`CLAUDE.md` states its limitation in advance: *the floor can **exclude** and can never **admit**,*
and the gap to the best fixed distribution was 34–255× the one time it was measured.
It is run first because a cell it excludes needs no search, and because a bound that turns out to be
slack is a measurement of how much room the search has.

**Bound 2 — the spread the two transferred distributions already show.**
At the bracket floor rung, at `f = 0.30`, the abstract grid at `5 × 10 = 50` paths reads
`0.103489604` rim-graded against `0.114289438` equal — **10.4 %** apart on the deciding statistic.
So the one-dimensional family the corpus has always graded on is not flat, and a target of `0.198 %`
is inside the range that family's own two members already span. That is a *cost* bound, not an exclusion:
it says the search is worth running, and it is one subtraction on a committed result file.

**And the bank is free.** The influence bank is a property of the **structure** — the lattice, the station
set and the load — and a distribution is a *diagonal* the Woodbury system adds. So one `InfluenceSurrogate`
per `(placement, columns, composite fraction, radial rung)` serves **every** distribution ever tried at
that cell, at one `n × n` Cholesky per candidate and no lattice factorisation whatever. The search is
therefore affordable *because* it is a search over an existing bank, which is what makes this task
a study rather than a proposal.

### The search, and where `C-0135`'s cure does and does not reach

`C-0135` records that cyclic coordinate descent **on a max** stalls on the kink, and that the loss is a
factor of 2.5. Its cure — log-sum-exp smoothing, continuation over the smoothing scale, an exact adjoint
gradient, Polak-Ribière with restarts, a lattice snap on the iterate, and every decision taken at six
significant digits — is implemented in `coupling/RobustDistribution.kt` as `minimaxStiffnessDistribution`,
and it applies to a **maximum of smooth functions**.

The zero-defect peak dishing **is** such a maximum: the peak over `81 × 81` samples of a field that is
smooth in the stiffnesses. So the cure applies to it verbatim, and this task uses it, unchanged, over a
one-state `MultiStateSurrogate` built on the honeycomb lattice.

The **90th percentile of a dropout ensemble is not** such a maximum. It is an *order statistic*, which
selects a realisation rather than maximising over a smooth family, so neither the log-sum-exp nor its
adjoint gradient transfers. This task does **not** invent a smoothing for it. What it does instead is
what `C-0089` did and what the corpus has an instrument for: a multi-start coordinate descent on the
**true** training percentile, with every acceptance and every tie-break taken through `searchDecision`
at six significant digits and the earlier candidate winning ties, so the search path is a function of
its inputs and not of the JIT's compilation schedule.

The two are **composed, not compared**: the smoothed nominal minimax runs first and its answer becomes
one of the starts of the percentile descent, beside equal springs and rim-graded 5:1. A descent seeded
from its own comparands cannot report a worse in-sample objective than the best of them, which is
`F6` below and is a property of the composition rather than a hope about it.

The smooth surrogate the percentile *does* admit — a CVaR of a log-sum-exp, whose adjoint would need a
factorisation per realisation — is **named as an open question and priced**, not attempted here. That is
the cheap-bound discipline applied to the method: the composition above reuses two tested instruments and
adds one forty-line adapter, and a new smoothing is a study of its own.

### Cost

Per cell: one lattice factorisation (shared across every cell of one `(fraction, rung)`), `n` back-substitutions
for the bank, one smoothed-minimax homotopy (~450 evaluations at `n × 81²` each), one percentile descent
(2 sweeps × `n` coordinates × 16 evaluations × 200 training realisations), and four gradings at
4 000 realisations. The percentile descent dominates and scales as `n²`; summed over column counts
`{1, 2, 3, 5}` it is 1.56× the `n = 50` cell alone, and over 8 `(placement, fraction)` pairs about
12.5× that cell. Measured on a smoke pass before the full run, as `CLAUDE.md` requires.

### What would falsify the approach

If the one-state `MultiStateSurrogate` built here does not reproduce, to the last few ulp, the peak
dishing of the `InfluenceSurrogate` the grading uses, the two searches are not searching the same object
and nothing below is admissible. If the surrogate at full presence does not reproduce the **assembled**
lattice solve with its own Woodbury support forces applied as point loads, the whole sweep is on the
wrong object — `C-0058` asserts exactly this and it is re-asserted here at the resolved link.

### What this task does NOT do

It does not re-open the placement search, the cross-section, the raster, the load case, the link
resolution or the radial bracket. It does not touch `tile/HoneycombGrillage.kt`. It does not re-grade
route B (`T-315`). It does not withdraw `C-0208`'s `0 of 64`, which is a reading on the two transferred
distributions and is exact on them.

---

## Falsifiers, declared before the run

| # | fires if | expected |
|---|---|---|
| `F1` | **OPEN — the headline.** A distribution searched at the resolved per-bond link puts at least one coupled cell inside `T-5b`'s `0.10` at the 90th percentile of the grading ensemble | either answer is the result |
| `F2` | **OPEN.** Any cell that clears `T-5b` does so at a max/min stiffness ratio **outside** `C-0060`'s measured `3.5 ≤ R ≤ 20` — a threshold the moving quantity feeds that the flatness falsifier cannot see | either answer is the result |
| `F3` | **OPEN.** The searched distribution's **out-of-sample** `p90` is *worse* than the best transferred distribution's at any cell — an over-fit, which a percentile search on a finite training stream can produce | either answer is the result |
| `F4` | the searched distribution's **in-sample** training objective is worse than the best of its own starts at any cell | must not fire — it is a property of the composition |
| `F5` | the searched `p90` falls **below** the oracle `p90` floor at any cell | must not fire — it is a theorem |
| `F6` | a uniform pressure on the free honeycomb lattice at the resolved link does not dish exactly zero, at `< 1e−9` of the free stroke | must not fire — `CLAUDE.md`'s standing falsifier |
| `F7` | the default (`radialLinkStiffness = null`) lattice is not bit-identical to the standing object at `assembleLoad` over every degree of freedom, or its crossover site set differs | must not fire — this task edits no shared source |
| `F8` | the surrogate at full presence does not reproduce the **assembled** solve with its own Woodbury support forces applied as point loads, at `< 1e−9` relative | must not fire |
| `F9` | the one-state `MultiStateSurrogate` and the `InfluenceSurrogate` disagree about the peak dishing of one distribution by more than `1e−10` relative | must not fire |
| `F10` | the two transferred distributions do not reproduce `C-0208`'s own published `p90` at every one of the 32 cells of the bracket-floor rung, at the emission precision | must not fire |
| `F11` | **OPEN.** A cell that clears at the 90th percentile still clears when its worst **single** path is removed — `CLAUDE.md`'s *an optimised placement is a cancellation, and a cancellation has no tolerance to a missing term* | either answer is the result |
| `F12` | two independent runs of the study do not produce a byte-identical result file | must not fire |
| `F13` | **OPEN.** The verdict at the tightest cell moves across `C-0208`'s five radial rungs | either answer is the result |
| `F14` | **OPEN.** A cell that clears `T-5b` does so with a **single-path** stiffness above `C-0023`'s 10 pN unzip allowable read over §3's acceptable 3 nm stroke — a **third** threshold the moving quantity feeds, and one a mandate on a SUM does not constrain | either answer is the result |

> **`F14` was added after the plumbing pass and before the run, and this note is the provenance.**
> `CLAUDE.md` requires a falsifier on **every** threshold the moving quantity feeds, and the smoke
> pass — 150 grading and 40 training realisations, a plumbing check whose numbers are not a result —
> made it plain that a free distribution meeting an equality on a **sum** can put almost all of it
> on a few paths. `F2` reads the max/min **ratio** and `F14` reads the **peak**, and they are not
> the same threshold: a ratio inside `C-0060`'s window says nothing about the absolute force a
> path carries. ~~The declaration is committed in the same commit as the study source and before the
> full run, which is the discipline `C-0092` asks for.~~
>
> **CORRECTED at the close of iteration 49, before the commit that carries the result.** That last
> sentence describes an intention and not a commit: the session that wrote this row was interrupted
> and committed nothing, so `646b29e` carries `F1`–`F13` only and the study source was still
> uncommitted when the full run started. **This row is therefore landed on its own, one commit
> before the result** — but that commit is made *after* the run, so the pre-registration is by
> **narrative and not by diff**, and `C-0092`'s discipline is met in spirit and not in form. The
> state is recorded here, in [`C-0212`](../claims/C-0212-a-searched-distribution-at-the-resolved-link.md) §12
> and in the post-run note below, rather than repaired into looking like something it was not.
