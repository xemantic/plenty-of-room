# T-129 — Is `C-0063`'s flat placement flat over a RANGE?

**Leaf `A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*),
with **`A1.2`** for the anchoring scheme the placement belongs to.

Raised by [`C-0064`](../claims/C-0064-robust-distribution.md)'s *Still open* item 1,
and named as the same exposure by [`C-0063`](../claims/C-0063-upward-root-placement.md)'s own item 1,
[`C-0065`](../claims/C-0065-crossbar-array-placement.md) and [`C-0066`](../claims/C-0066-arm-slab-tie-clearance.md).

---

## Formulate

`C-0063` makes the Gen-1 tile flat — **0.0706** of the free-tile stroke, with **34 equal springs**,
on a placement `C-0055`'s upward lattice actually supplies —
and reports it at **one** of `C-0022`'s solved states,
the 2 mM / 10 nm / 0.192 V design point,
which is the same state `C-0058` was tuned at.

`C-0064` is the reason that is not enough,
and it is also the reason the question is sharper than *"run it at the other four"*:
of the 31 non-empty subsets of `C-0022`'s five headline states,
**every one of the 14 containing both the 2 nm state and a 10 nm state fails** and every one of the other 17 is flat,
because the 2 nm state's free-tile dishing field has a cosine of **−0.943 to −1.000** against every other state's.
`CH-0077`'s reading is that the five states are **four devices**,
so the requirement is owed over the states **one device traverses**, not over a portfolio.

### The question, in one sentence

**Over the states a single device actually traverses, is `C-0063`'s placement flat under `T-5b`'s 10 % convention, with equal springs?**

### Acceptance predicate

`T-123`'s multi-state minimax re-run on `C-0055`/`C-0063`'s **34 upward roots**,
over the states each device traverses,
against `T-5b`'s **10 %** convention:

- **P1** — the worst peak dishing of **34 equal springs** on `C-0063`'s placement,
  over each device's traversed range, against 0.10 of the free-tile stroke **4.90731 nm**.
- **P2** — the same for the best **distribution** the 34 paths admit (the multi-state minimax),
  which says whether the equal-spring advantage survives a range or a distribution is needed after all
  (if it is needed, `C-0060`'s buildability and `CH-0074`'s station question both return).
- **P3** — which states are **co-occupied by one device**, with the exclusion of the 2 nm state from a
  10 nm device's range stated as a **physical** bound (`C-0050`'s own dead-load stroke), not a convenience.
- **P4** — whether a placement swept **under the range objective** beats `C-0063`'s single-state winner,
  i.e. whether the single-state placement is the right placement for a range.

### Verification type

**in-silico** (`C-0009`'s beam-and-hinge grillage at phase 24 with **its own** crossover columns,
`C-0006`/`C-0047`'s flatness pipeline, `C-0022`'s **solved** loads keyed on `(concentration, gap, bias)`,
`C-0064`'s `MultiStateSurrogate` and `minimaxStiffnessDistribution` re-run as libraries)
**+ logical** (the per-state least-squares floor, which bounds every distribution whatever;
the reachability arithmetic that decides which states one device occupies).

### Units and conventions, locked

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa** exactly;
  `k_BT = 4.141947 pN·nm` at **300 K**, aqueous buffer with **Mg²⁺** at 0.5 / 2 / 10 mM.
- `x` **along** the helices, `y` **across** them, `z` normal and positive **upward**;
  `w` positive **downward**, compressing the layer.
- **Dishing** is the peak absolute departure from the area-weighted best-fit **plane** — piston and both
  tilts removed — on the same **81 × 81** grid as `C-0026`, `C-0047`, `C-0058`, `C-0063` and `C-0064`,
  normalised by the free-tile stroke **4.90731 nm**.
- **Flat** means below **0.10** of that stroke — `T-5b`'s convention via `C-0015`, **a convention and not
  a physical threshold**.
- A **state** is a `(concentration, gap, bias)` triple of `C-0022`'s solved profiles.
- An **operating range** is the set of states **one device** traverses: one buffer, one layer height,
  one bias, from `gap = L₀` down to `gap = L₀ − s` (`C-0064`'s definition, re-used unchanged).
- The coupling is **34 linear springs to ground** whose stiffnesses **sum** to `C-0017`'s 33.3333 pN/nm.
  The sum is the mandate; the distribution is the design variable; `C-0063`'s headline is at **equal** springs.

---

## Plan

### The cheap bounds, which run before any optimisation

1. **The per-state least-squares reachable floor** on `C-0063`'s 34 stations, at every state of every
   range. Dishing is affine in the attachment forces, so the least-squares minimum over all of `ℝ³⁴`
   bounds **every** distribution below, and `max_s` of it bounds the minimax.
   **Falsifier**: a range floor above 0.10 would prove that *no* distribution on this placement is flat
   over that range, and the flat Gen-1 tile becomes a single-state result on this axis too.
2. **The free-field cosine matrix** — `C-0064`'s instrument, and it needs no optimiser at all. A pair of
   states with a cosine near `+1` wants the same correction; a negative cosine is the obstruction
   `C-0064` located. **Prediction to be falsified**: within a device's range the cosine is positive, and
   the negative ones are exactly the cross-device pairs involving the 2 nm state.
3. **The equal-spring reading itself**, which is the headline and costs one Cholesky per state.
4. **The reachability arithmetic**: a 10 nm device reaches a 2 nm gap only with an **8 nm** stroke, and
   `C-0050`'s dead-load stroke at §3's 100 pN at the 10 nm design point is at most **6.013 nm** over its
   six layer models. Read from `C-0050`'s own result file, not quoted.

### The method, and its cost

`C-0064` built exactly the instrument this task needs and it is model-agnostic: `MultiStateSurrogate`
prices every state of one distribution on `n + S` load cases from **one** factorisation, and
`minimaxStiffnessDistribution` is a log-sum-exp smoothing with continuation, analytic gradients through
the Woodbury solve and `C-0058`'s coordinate descent as a polish. What is missing is the *bank*: a
multi-state influence bank over **every candidate root** of a phase, from which a surrogate for any
34-root subset follows by slicing — which is what makes a **placement** sweep under a multi-state
objective affordable at all (`C-0063` needed 1.1 million placements at one state; the range objective
doubles the per-placement cost and nothing else).

The expensive alternative — an assembled 855-degree-of-freedom solve per placement per state — is
~10⁴× dearer and buys nothing, because superposition is *exact* for a linear system: the bank is
asserted against the assembled solve as a gate rather than trusted.

### What would falsify this approach

- The sliced surrogate disagreeing with a surrogate built over the same subset alone, or with an
  assembled `OrigamiGrillage` solve at the same stations (gate 4).
- A uniform load producing non-zero dishing on the free tile (gate 2, the free strong falsifier).
- `C-0063`'s **0.0706** failing to reproduce at its own state on its own host (gate 5, the second free
  falsifier: it is the only published number on these stations).
- The optimiser failing to match the equal-spring reading it starts from — the minimax is a descent and
  must never return worse than its start.

### The determinism discipline

`CLAUDE.md`: an argmin makes a result file irreproducible unless the **decision** is rounded too, and
`C-0064` met the next member of that family. Every comparison in the placement sweep is taken on a
rounded objective with the placement's own canonical key as the tie-break (`C-0063`'s discipline), and
every comparison inside the distribution search is `C-0064`'s `searchDecision`. Scratch files are
prefixed `T-129-` and study records `T129…`, per `CLAUDE.md`.

---

## Execute

`src/main/kotlin/anchoring/RangeRobustPlacement.kt` — `MultiStateRootBank` (one free solve per state and
one unit-point-load solve per candidate root, sliced into `C-0064`'s `MultiStateSurrogate` for any subset)
and the two-line reachability arithmetic `strokeToOccupy` / `gapOccupiable`.
Tests first, in `src/test/kotlin/anchoring/RangeRobustPlacementTest.kt` (13, gate-named).
Study: `anchoring.RangeRobustPlacementStudyKt` → `gpd/results/T-129-range-robust-placement.json`,
**~11 min**, run through `tools/study.sh`.

## Verify — the five gates

All five are executable tests; the study adds the convergence sweeps.
See [`C-0068`](../claims/C-0068-range-robust-placement.md) for the table and the departures:
the free strong falsifiers are **a uniform load dishing exactly zero on the free tile**,
**the sliced bank against a surrogate built over the same subset alone and against an assembled solve**,
and **`C-0063`'s 0.0706145537 reproducing to the last emitted digit**.

## File

**[`C-0068`](../claims/C-0068-range-robust-placement.md)** — the four predicates answered:
**P1** 0.0789 / 0.0853 / 0.0896 flat with equal springs at 10 nm, **0.2000 not flat** at 5 nm;
**P2** all four flat with a distribution (0.0291 / 0.0365 / 0.0565 / 0.0382) at peak ratios 1.72–2.32;
**P3** the 2 nm state demands 8 nm of stroke of a 10 nm device against `C-0050`'s 7.4235 nm, and the
device that owns it is evaluated separately and is the one that fails;
**P4** `C-0063`'s placement is the range argmin (0 of 198 288 better), and the **layer selects the phase**.
Raises **[`CH-0080`](../challenges/CH-0080-the-equal-spring-advantage-belongs-to-the-ten-nanometre-layer.md)**.
