# C-0103 — **`CH-0103`'s missing column is real, it is +12.86 %, and the recommendation it challenges does not pay it.** At fixed station geometry on `C-0063`'s own phase-24 upward lattice — `C-0072`'s own interior-root rule applied to `C-0063`'s own 34 roots, so the 34- and 30-root members **are** the published designs — the 34 → 30 reduction moves the 90th-percentile dishing under `C-0087`'s measured dropout from **0.638498565** to **0.720607136** of the free-tile stroke, **+12.86 %**, against a plan margin that improves **68.9×**. But the move the programme *actually* recommends carries a **phase** change with it, and on its own stations it is **8.68 % BETTER** (0.639129638 → 0.583664426), **10.30 %** better under its own minimax. The count axis **survives** the transfer to the real lattice and is **3.07× steeper** than the abstract grid's, −0.740086889 against −0.241246026 — while the *same* lattice's **placement-searched** subsets fit **+0.0610348337**, a slope of the wrong sign, which is [`CH-0119`](../challenges/CH-0119-a-redundancy-slope-measured-over-searched-subsets-is-not-a-count-slope.md)

| | |
|---|---|
| **Task** | [`T-163`](../tasks/T-163.md), raised by [`CH-0103`](../challenges/CH-0103-the-path-count-recommendation-runs-against-fabrication.md) (out of [`C-0089`](C-0089-dropout-robust-placement.md), `T-155`), *"What would settle it"* item 1 |
| **Leaf** | **`A8.2`** (the flatness of the tile), with **`A1.2`** for the anchoring scheme the coupling belongs to |
| **Verification type** | **logical** (three closed-form bounds — a rank-transfer test, a redundancy division read off `C-0089`'s own published curve, and the pitch/run-length arithmetic — all before the sampler) **+ in-silico** (`C-0058`/`C-0063`/`C-0087`/`C-0089`'s own exact Woodbury surrogate on `C-0009`'s grillage under `C-0022`'s **solved** load, with `C-0087`'s seeded Bernoulli dropout unchanged, 10 000 realisations per cell) |
| **Verdict** | **PASS on every predicate, and `CH-0103` is UPHELD as a bookkeeping correction while the recommendation it challenges STANDS.** Over **21 graded cells** — two **nested** chains and one placement-searched family at 22/25/28/30/34/45 paths on one crossover phase, plus three reference cells — the second term of `CH-0103`'s trade is measured for the first time: at **fixed station geometry** the 34 → 30 reduction costs **+12.8596328 %** of the 90th-percentile dishing (0.638498565 → 0.720607136 of the free-tile stroke), where the plan margin it buys improves by **68.8998088×** (0.0256098233 → 1.76451193 nm). Expressed as relative moves that is **528 : 1** in the recommendation's favour. **And the recommendation does not even pay the 12.86 %**: the move the programme took is `C-0063`'s 34 at phase **24** to `C-0074`'s 30 at phase **8**, and graded cell for cell it is **8.67824131 % BETTER** under the dropout (0.639129638 → 0.583664426, both reproduced to `2e−10`) and **10.30 %** better under `C-0074`'s own minimax (0.573317978). **The confound `CH-0103` itself flagged is now a number**: the count term is **+12.86 %** and the phase term is **−19.0 %**, and the phase is the larger of the two. **The declared falsifier `F1` did NOT fire** — at fixed geometry the percentile falls at **every** step of **both** nested chains — and the count axis on the real lattice is **3.07×** and **2.92× steeper** than `C-0089`'s abstract grid (−0.740086889 and −0.704431429 against −0.241246026), which runs **opposite** to `C-0098`'s shared-body finding of a 2.08× *shallower* lattice slope. **The cheap bound settled the direction for nothing and under-predicted the size by 4.2×**: one log-log division over `C-0089`'s six published points gives **+3.07 %** before a realisation is drawn. **`C-0089`'s cheap ranking instrument transfers across COUNTS** — Spearman ρ = **0.942857143**, **1.000000000** and 0.942857143 within the three families and **0.985522234** over all 18 — so the axis it fails on is `C-0098`'s **phase**, not the count. **Nothing here is flat**: all 21 cells exceed `T-5b`'s 0.10 at the 90th percentile, in **99.53–100 %** of realisations, and **21 of 21 are worse than no coupling at all** (the uncoupled tile dishes 0.307902368), the worst by **3.35976695×**. Raises [`CH-0119`](../challenges/CH-0119-a-redundancy-slope-measured-over-searched-subsets-is-not-a-count-slope.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED.** The dropout **input** is `C-0087`'s, which is Strauss et al. (2018) read directly — a measurement of *staple* incorporation on a plain Rothemund rectangle at one folding protocol — and the out-of-plane motif every placement stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |
| **Provenance** | `gpd/results/T-163-path-count-fixed-geometry.json`, produced by `coupling.PathCountFixedGeometryStudyKt`; model in `src/main/kotlin/coupling/PathCountAtFixedGeometry.kt`, which **adds no method to any shared source** — it composes `C-0089`'s `DropoutRobustPlacement`, `C-0087`'s `StapleDropout`, `C-0058`'s `NonUniformCoupling`, `C-0098`'s `SharedBodyPlacement` (`descendTieSubset`, `redundancyFit`, `longestAbsenceRunByRow`) and `C-0055`/`C-0063`'s `anchoring` lattice and influence bank as libraries; **3 cheap bounds, a 6-row pitch ledger, 21 graded dropout cells at 10 000 seeded realisations each, 10 rank-agreement scopes, 4 redundancy fits, a 16-row trade table, 3 convergence axes, 15 reproductions, 7 predicates, 5 falsifiers**; **20 gate-named tests in `src/test/kotlin/coupling/PathCountAtFixedGeometryTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 21 m 41 s — the whole suite, on its own isolated tree, with NOTHING dropped**, the four post-Gradle gates (`testHarness`, `testMarkdownTables`, `testDeliverableTracer`, the census self-checks) included and clean; the result file **produced twice on separate snapshots and diffed field by field — 1 113 of 1 117 fields identical**, the four that differ being deliberately edited `findings` strings and no number anywhere |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** sheet, 15 duplexes at the SAXS-measured **2.69 nm**; **one** crossover phase, **24** — `C-0063`'s own, centro-symmetric and an eight-column host, carrying **53** upward `EAST` sites in rows of 4, 3, 4, 3, … — with phase **8** used only by the three reference cells; `C-0022`'s **solved** edge profile at **2 mM, 10 nm, 0.192 V** for every headline, with 2 mM / 7 nm / 0.192 V used only by `C-0074`'s own minimax; `C-0017`'s **33.3333333 pN/nm** as a **SUM** at §3's **acceptable 3 nm**, shared **EQUALLY** at every count; free-tile stroke **4.90731102 nm**; dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; `C-0087`'s **`MEASURED_DEPTH`** incorporation field; **ONE** Bernoulli stream over all 53 sites at seed **20260817**, **10 000** realisations, restricted per subset (**common random numbers**); decisions at 6 significant digits, emission at 9, difference-of-nearly-equal fields at **2** |
| **Consumes** | [`C-0089`](C-0089-dropout-robust-placement.md) (`DropoutEnsemble`, `dropoutDishingSample`, `summariseDropoutDishing`, `worstSinglePathRemoval`, `spearmanRankCorrelation`, `columnsForRunRobustness`; its abstract-grid density curve **read at run time** from `gpd/results/T-155-*.json` and **refitted**; its 0.583664426 reproduced), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (**the whole dropout model** — `measuredDepthIncorporation`, `DropoutRandom`, `bernoulliPresence`, `orderStatistic`, `solveWithDropout`; its 0.501011167, 0.639129638 and 0.5733 all reproduced), [`C-0098`](C-0098-shared-body-placement-and-distribution.md) (`descendTieSubset`, `redundancyFit`, `longestAbsenceRunByRow`, and its slope **challenged**), [`C-0072`](C-0072-plan-tolerance-model.md) (**its own `rowsWithoutInteriorRoots` reduction rule, restated and extended into a nested chain**; its 0.260281397 reproduced), [`C-0063`](C-0063-upward-root-placement.md) (**the 34-root anchor placement**, read from `gpd/results/T-125-*.json`, and `UpwardRootInfluenceBank`; its 0.0706145537 and 0.307902368 reproduced), [`C-0074`](C-0074-two-per-row-placement.md) (**the recommended 30-root placement**, read from `gpd/results/T-136-*.json`, and its 30-parameter minimax **re-run**; its 0.242359741 and 0.0682200897 reproduced), [`C-0075`](C-0075-path-count-consistency.md) (**the self-consistent count table**, read at run time from `gpd/results/T-138-*.json` — every arm length and plan margin here is its), [`C-0055`](C-0055-unused-junction-site.md) (`upwardRootLattice`, `armDirections`), [`C-0064`](C-0064-robust-distribution.md) (`multiStateSurrogate`, `minimaxStiffnessDistribution`), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`, `normalisedStiffnesses`), [`C-0047`](C-0047-single-column-flatness.md) (the 12.8290845 nm bending length), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (the 53-site inventory at phase 24), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0026`](C-0026-one-row-per-duplex.md) (the free-tile stroke), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0009`](C-0009-discrete-lattice-tile.md)/[`C-0015`](C-0015-crossover-phase-and-registration.md)/`Gen1Tile`, [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the 10 pN unzip allowable and `T-5b`'s 0.10) |
| **Raises** | [`CH-0119`](../challenges/CH-0119-a-redundancy-slope-measured-over-searched-subsets-is-not-a-count-slope.md) against `C-0098`'s Deliverable 3 |

---

## The claim, in one line

**A count reduction on this lattice really does spend dropout robustness — and the programme's own 34 → 30 move buys a crossover phase at the same time, which is worth more than the count costs, so `CH-0103` was right about the missing column and wrong about which way it points.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward** — away
  from the grafted layer, which is **below** the tile. `w` is positive **downward**; the origin
  is the tile centre.
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit
  **plane**, on the same **81 × 81** grid as every flatness claim upstream. **Flat** means below
  **0.10** of the free-tile stroke — `T-5b`'s **convention**, not a physical threshold.
- **A dropout is a REMOVAL, not a perturbation** (`C-0087`): an absent path is solved as an
  absent station, which is exact superposition.
- **The operating state is named**: `C-0022`'s **solved 2 mM / 10 nm / 0.192 V** profile. The
  **eleventh** time this programme has had to say which state a flatness verdict is read at.
- **The verdict statistic is the 90th percentile**, a nearest-rank order statistic, and it is
  `C-0087`'s, `C-0089`'s and `C-0098`'s, so every cell here is comparable with theirs.
- **The distribution is EQUAL springs at `C-0017`'s unchanged SUM**, at every count. That is
  `C-0089`'s Deliverable 1 convention and it is the one that isolates the count: a distribution
  is a *redistribution of the same budget*, and `C-0089` (1.30–1.61×) and `C-0098` (1.026×)
  have already priced that axis on their own topologies.

### The one convention that is NOT inherited, and it is what makes the answer readable

`C-0089` draws a **separate** Bernoulli ensemble per station set, so two counts differ both in
their design and in their fabrication outcomes. Here **one** stream is drawn over the whole
53-site inventory and **restricted** to each subset, so two nested designs see the same staple
present or absent at every station they share — **common random numbers**. The convergence axis
shows what it buys: the 34-path percentile itself moves **0.0029** between 5 000 and 10 000
realisations while the 34 → 30 **difference** moves **0.0026** — on a difference of **0.0821085708**,
i.e. the cost term is **32×** its own sampling floor where the two levels it is a difference of
are only 220× theirs. The published per-set convention is carried beside it at the 34-root cell
and reproduces `C-0087`'s **0.639129638** exactly.

---

## The three cheap bounds, which ran first

### Bound 1 — does `C-0089`'s single-removal bound transfer ACROSS COUNTS?

`C-0089` measures Spearman ρ = 0.972896669 between the worst single-path removal (`n` solves, no
sampling) and the 90th percentile, over 22 *designs at mixed counts*. `C-0098` measures
**0.468487481** over six *phases*. Neither is a statement about a count axis at fixed geometry.

| scope | pairs | **Spearman ρ** | transfers? |
|---|---|---|---|
| all 18 fixed-geometry cells, counts and placements mixed | 18 | **0.985522234** | yes |
| **across counts**, chain A | 6 | **0.942857143** | yes |
| **across counts**, chain B | 6 | **1.000000000** | yes |
| **across counts**, the searched family | 6 | 0.942857143 | yes |
| across placements, at 22 / 25 / 28 / 30 / 34 paths | 3 each | **1.000000000** | yes |
| across placements, at 45 paths | 3 | **0.500000000** | **no** |

**`F2` did not fire.** The bound tracks a monotone density axis essentially perfectly and tracks
three placements at one count perfectly at five of six counts. **So the axis `C-0098` found it
failing on is the PHASE and not the count**, and that is a sharper statement than either claim
could make alone: a bound that removes one path predicts what a *density* change does and does
not predict what a *lattice reshuffle at fixed density* does.

### Bound 2 — the redundancy division, read off `C-0089`'s own published curve

`C-0089`'s six abstract-grid points, read at run time from `gpd/results/T-155-*.json` and
refitted, give a slope of **−0.241246026** and therefore predict **+3.07 %** for 34 → 30 —
**before a single realisation is drawn**.

> Measured at fixed geometry: **+12.8596328 %**. The cheap bound gets the **sign** and the
> **order of magnitude** for one division and under-predicts the size by **4.2×**. That is the
> honest reading of a cheap bound run before an expensive calculation, and it is also the first
> evidence for the steeper lattice slope below.

### Bound 3 — the pitch and run-length arithmetic at the counts this task sweeps

| paths | widest row | pitch [nm] | pitch / `ℓ` | p90 longest run | surviving pitch [nm] | **columns demanded** | shortfall |
|---|---|---|---|---|---|---|---|
| 22 / 25 / 28 / 30 | 2 | 20.000 | 1.559 | 2 | 60.000 | **10** | **5.00×** |
| 34 | 3 | 13.333 | 1.039 | 2 | 40.000 | **10** | **3.33×** |
| 45 | 3 | 13.333 | 1.039 | 3 | 53.333 | **13** | **4.33×** |

`ℓ = 12.8290845 nm`, reproducing `C-0047`. **No count in the swept range comes within 3.33× of
the density the measured dropout demands**, and the shortfall is a *length*, so no count the
40 nm tile can carry closes it. This is `C-0089`'s Bound 3, unchanged in kind and read on the
real lattice.

---

## Deliverable 1 — the count axis at FIXED station geometry

Two **nested** chains anchored on `C-0063`'s own 34 roots at phase 24. **Chain A** is `C-0072`'s
own `rowsWithoutInteriorRoots` rule and its exact inverse, so its 30-root member **is** `C-0072`'s
own published reduction (0.260281397 at zero defects, reproduced to `2.4e−10`). **Chain B** runs
the same two rules on **mirror row pairs**, so it is centro-symmetric wherever the parity admits
it. Nestedness is asserted as a gate at every pair of counts.

| paths | arm [nm] | plan margin [nm] | places? | **chain A p90** | **chain B p90** | searched p90 |
|---|---|---|---|---|---|---|
| 22 | 6.88710679 | 8.08789321 | yes | **1.0344802** | **0.973099528** | 0.464620176 |
| 25 | 7.2357405 | 2.2992595 | yes | 0.935111943 | 0.954231448 | 0.546875266 |
| 28 | 7.56280606 | 1.97219394 | yes | 0.798304658 | 0.862986882 | 0.450748151 |
| **30** | **7.77048807** | **1.76451193** | **yes** | **0.720607136** | **0.720607136** | 0.4187767 |
| **34** | **8.16439018** | **0.0256098233** | **yes** | **0.638498565** | **0.638498565** | 0.410573715 |
| 45 | 9.13115573 | **−0.941155731** | **no** | 0.632244272 | 0.633945457 | 0.541356909 |

- **`F1` did not fire.** The percentile falls at **every** step of **both** nested chains. The
  count axis `C-0089` measured on the abstract grid survives the transfer to the lattice the arms
  actually root on.
- **And it is STEEPER, not shallower.** The fitted slopes are **−0.740086889** (chain A) and
  **−0.704431429** (chain B) against `C-0089`'s abstract-grid **−0.241246026** — **3.07×** and
  **2.92×**. `C-0098` measured the *shared body's* real-lattice slope 2.08× **shallower** than its
  abstract one; **the array's runs the other way**, and `CH-0119` is why the two are not
  comparable as measured.
- **The two chains agree exactly at 30 and 34** — the interior rule and the mirror-pair rule
  remove the same four roots — which is what makes the headline a property of the *count* and not
  of the rule that produced the chain.
- **The searched family is NOT fixed geometry** and is not monotone (0.464620176, 0.546875266,
  0.450748151, 0.4187767, 0.410573715, 0.541356909). It is reported for the placement axis and for `CH-0119`, and **it is
  not a set of designs**: **5 of its 6 cells cannot be given arm directions at their own
  self-consistent arm length**, because the descent optimises a dishing objective that knows
  nothing about a footprint.

## Deliverable 2 — **`CH-0103`'s question, in one table**

| | what 34 → 30 buys | what 34 → 30 costs |
|---|---|---|
| **plan margin** (`C-0072`/`C-0074`/`C-0075`, read at run time from `T-138`) | **0.0256098233 → 1.76451193 nm**, **68.8998088×** | — |
| **90th-percentile dishing under the measured dropout, at FIXED geometry** | — | **0.638498565 → 0.720607136**, **+12.8596328 %** (factor 1.12859633) |
| **the two as relative moves** | +6790 % | +12.86 % |
| **ratio** | **528 : 1 in the recommendation's favour** | |
| **and the move the programme ACTUALLY recommends** | the same 68.9× | **−8.67824131 %** — the recommended design is *better* under fabrication |

**The reduction stands.** Three independent grounds:

1. **The trade is 528 : 1.** The plan margin improves by 68.9× where the 90th percentile worsens
   by 1.129×, and the plan margin is the axis on which `C-0072`'s four floors — the base-pair
   rise at 13.28×, the 2.73/2.69 nm SAXS disagreement at 1.56×, the thermal breathing at 10.46×
   and the tip's own bending at 70.6× — all exceed the 34-path margin. A 12.86 % move on a
   quantity already 6.4× past its convention does not weigh against a margin that is 0.075 of a
   base-pair rise.
2. **The recommended move does not pay the 12.86 % at all.** It is `C-0063`'s 34 at phase **24**
   → `C-0074`'s 30 at phase **8**, and graded cell for cell it reads **0.639129638 →
   0.583664426**, i.e. **8.68 % better**, and **0.573317978** (10.30 % better) under `C-0074`'s
   own 30-parameter minimax. The count term is **+12.86 %** and the **phase** term is **−19.0 %**.
3. **Neither design is flat.** All 21 cells exceed `T-5b`'s 0.10 at the 90th percentile, at
   99.53–100 % of realisations, and **21 of 21 are worse than no coupling at all** (0.307902368),
   the worst by **3.35976695×**. The count axis therefore decides no acceptance verdict on this branch:
   it moves a number that is 4.1× past a convention to a number that is 4.6× past it, inside a family that spans 4.1–10.3×.

## Deliverable 3 — the placement axis at fixed count is the LARGER of the two

| at 34 paths | p90 |
|---|---|
| chain A / chain B (`C-0063`'s own placement) | **0.638498565** |
| the searched subset at the same count | **0.410573715** |
| **spread** | **1.56×** |
| the whole 34 → 30 count move, same lattice | **1.13×** |

**The variable `CH-0103` says the programme spent is the smaller of the two on this lattice** —
and the larger one is the variable `C-0063` spent 1 144 858 placements on. The searched rows are
**upper bounds** (a first-improvement descent) and, as noted above, **not buildable**; what they
establish is that the count is not the dominant axis at fixed phase.

## Deliverable 4 — under the dropout this family is a NET DISHING SOURCE, and the sparse end is the worst of it

The uncoupled tile on this host dishes **0.307902368** of the stroke (reproducing `C-0074`'s
parameter block exactly). **All 21 graded cells exceed it at the 90th percentile**, the worst —
chain A at 22 paths — by **3.35976695×**, at 1.0344802.

`CLAUDE.md`: *"an attachment coupling can be a NET DISHING SOURCE, and the sign flips at an
attachment pitch of one Winkler bending length"*. A dropout **is** an increase in that pitch, and
a count reduction starts the design closer to the crossing: at 22–30 paths the widest row carries
two roots at a 20 nm pitch, **1.56 `ℓ`**, before any staple is missing at all.

---

## The five verification gates

Executed as **20 gate-named tests** in `src/test/kotlin/coupling/PathCountAtFixedGeometryTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a restricted ensemble carries one probability per retained path and refuses an empty, a repeated or an out-of-range index list; a nested chain refuses a count outside `[15, 45]`, an anchor root that is not a site of its own row and a row carrying more than the maximum; the station index map is a strictly increasing permutation into the flattened lattice and refuses an off-lattice root; an arm admissibility test refuses a non-positive arm or edge, admits `C-0075`'s 8.16439018 nm arm on `C-0063`'s own placement and refuses a 20 nm one | **PASS** |
| **2 — limiting cases** | a chain at its own anchor count returns the anchor exactly, on both rules; a restriction to every index reproduces the parent stream and its probabilities exactly; **the swept family is NESTED at every one of the 15 ordered pairs of the six counts, on both chains, and every member carries exactly its own count**; the chain at 30 is two roots in every row, i.e. `C-0072`'s own reduction; the sliced bank reproduces an independently assembled `latticeInfluenceSurrogate` at `< 1e−9`; a restricted ensemble at unit incorporation keeps every retained path and reproduces the nominal solve | **PASS** |
| **3 — symmetry and conservation** | **`F3`** — the uncoupled tile under a **uniform** load dishes exactly zero, on the dishing and on the oracle floor (`< 1e−9`); **the restriction is common random numbers realisation for realisation**, asserted both against the parent's own flags and against a nested pair's shared stations; the phase-24 upward lattice is centro-symmetric, computed independently of `centroSymmetricUpwardPhases`; the symmetric chain is centro-symmetric at 22, 28, 30, 34 and 45 | **PASS** |
| **4 — numerical convergence and statistical power** | the 90th percentile of chain A at 34 paths at **1 250 / 2 500 / 5 000 / 10 000** realisations (0.636375317, 0.638198529, 0.641371302, 0.638498565; departure **0.0029**); **the 34 → 30 DIFFERENCE at the same four levels under common random numbers** (0.0901029114, 0.0783285022, 0.079498062, 0.0821085708; departure **0.0026**), which is the axis `F5` is read on; the **mean over 200 realisations** on the **41 / 81 / 161** dishing grid (0.462279657, 0.46250114, 0.462582655; departure **8.2e−05**) — `C-0087`'s cure for the degenerate nested-grid percentile; a binomial standard error beside every exceedance; a redundancy fit recovers an exact power law to `1e−9`; departures emitted at **two** significant digits and **no step counter anywhere** | **PASS** |
| **5 — literature and upstream cross-check** | **fifteen reproductions, worst strict departure `9.7e−09`**: `C-0063`'s **0.0706145537**, `C-0072`'s **0.260281397**, `C-0074`'s **0.242359741** and **0.0682200897** (its minimax **re-run**, not transcribed), the uncoupled tile's **0.307902368**, `C-0087`'s **0.501011167**, **0.639129638** and **0.5733**, `C-0089`'s **0.583664426**, `C-0026`'s **4.90731102 nm**, `C-0047`'s **12.8290845 nm**, `C-0066`'s **53** sites, and `C-0075`'s **8.16439018 nm** arm with its **0.0256098233** and **1.76451193 nm** margins | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **`F1`** | **the declared one** — at fixed station geometry the 90th percentile is **not** monotone decreasing in the path count over 22 → 45 | **NO** | the percentile falls at every step of **both** nested chains; the placement-searched family is not monotone and is not fixed geometry |
| **`F2`** | `C-0089`'s single-removal bound does not transfer **across counts** (ρ < 0.8) | **NO** | ρ = 0.942857143, 1.000000000, 0.942857143 within the three families and 0.985522234 over all 18 |
| **`F3`** | the uncoupled tile dishes non-zero under a **uniform** load | **NO** | `< 1e−9`, wired as a test |
| **`F4`** | a standing figure fails to reproduce | **NO** | worst strict departure **`9.7e−09`** over 15 reproductions |
| **`F5`** | the cost of 34 → 30 is below the ensemble's own convergence departure, so `CH-0103`'s cost term is not measurable | **NO** | the cost is **0.0821085708** of the stroke against a sampling departure of **`2.6e−03`** — **32×** clear, and it is clear *because* of the common random numbers |

**Three results that were not anticipated.**

1. **That the real lattice's count axis is STEEPER than the abstract grid's, where `C-0098` found
   the opposite.** −0.740 against −0.241 here; −0.377 against −0.784 there. Chasing the
   difference is what produced `CH-0119`: on **one** lattice, **one** phase and **one** topology,
   a nested chain fits −0.740 and a placement-searched family fits **+0.061** — a slope of the
   wrong sign, from the same data, differing only in whether the placement was allowed to move.
2. **That the count term and the phase term have opposite signs on the very move being
   challenged**, and the phase is the larger. `CH-0103`'s own *"What is NOT claimed"* section
   suspected this and could not price it; it is +12.86 % against −19.0 %.
3. **That the searched placements are unbuildable.** Five of six cannot be given arm directions
   at their own arm length. A subset descent on this lattice optimises a dishing objective that
   has never heard of `C-0053`'s footprint, and the rows it returns are bounds and not designs.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. The dropout input is measured; nothing derived
  here is.
- **The incorporation field, its conventions and the independence of realisations are `C-0087`'s
  UNCHANGED**, and this claim inherits its whole validity range: a plain Rothemund rectangle at
  one folding protocol, a coupling path whose own incorporation nobody has measured, and
  independence as a convention rather than a measurement.
- **ONE crossover phase.** `C-0098` measures the phase axis at **1.95×** over all 32 phases on
  the shared-body topology, and this claim measures a **−19.0 %** phase term between two phases
  on the array. A count effect measured at phase 24 is not a statement about the lattice's whole
  phase family, and nothing here bounds the interaction of the two axes.
- **EQUAL springs at every count**, deliberately. The distribution axis is `C-0089`'s
  (1.30–1.61× on an array) and `C-0098`'s (1.026× on a shared body); freeing it at every count is
  a different experiment and could reorder the cells.
- **The nested chains are TWO rules of many.** They agree exactly at 30 and 34 and differ below
  30, where chain A becomes strongly asymmetric — chain A at 22 reads 1.0344802 against chain B's
  0.973099528 — so the *values* below 30 carry a rule dependence of up to 6 % and the *monotonicity*
  does not.
- **The subset search is a first-improvement descent** on an objective whose rank agreement is
  measured here rather than assumed, so the searched rows are **upper bounds**; and they are
  **not placements**, because the arm footprint is reported rather than imposed.
- **The arm footprint is REPORTED and never imposed.** An arm's length is a function of the count
  (`C-0075`), so buildability is a plan axis `C-0069`/`C-0075` own, and the 45-path row is graded
  as a station set although its own arm cannot be placed (margin **−0.941155731 nm**).
- **`T-5b`'s 0.10 is a CONVENTION.** At a 50 % convention nothing here changes: the best cell in
  the sweep is 0.410573715 and the best *buildable* one 0.464620176.
- The dishing pipeline, the lattice, the host, the load and the free-tile stroke are `C-0058`'s,
  `C-0063`'s, `C-0087`'s and `C-0089`'s unchanged, and inherit `C-0022`'s unsourced rim charge
  and `C-0001`'s single foundation secant.
- **Single layer, static, 300 K, aqueous 2 mM MgCl₂.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| staple incorporation map | 168 cells, 48–95 %, mean 84 % | **CITED, MEASURED**, Strauss et al. (2018) **through `C-0087`** |
| `C-0089`'s abstract-grid density curve | 6 rows, read at run time from `gpd/results/T-155-*.json` | **CITED**, and **refitted** rather than transcribed |
| `C-0075`'s self-consistent count table | arm, ceiling, margin and placed count at every swept count, read at run time from `gpd/results/T-138-*.json` | **CITED** |
| `C-0063`'s and `C-0074`'s placements | 34 and 30 roots, read at run time from their own result files | **CITED** |
| `C-0022`'s solved collars | 2 mM / 10 nm / 0.192 V and 2 mM / 7 nm / 0.192 V, read at run time | **CITED** |
| `C-0098`'s slopes and rank correlation | −0.784357442, −0.376769756, 0.468487481 | **CITED** from `C-0098` |
| `C-0072`'s four tolerance floors | 1.56× to 70.6× | **CITED** from `C-0072` |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| `T-5b`'s tolerance | 0.10 | **CITED CONVENTION** |

Everything else — every chain, cell, bound, ledger, fit, rank scope, trade row, convergence axis
and reproduction — is **derived here in code**.

## What this does to the standing claims

| claim | what moves |
|---|---|
| **`C-0072`** | **No number moves and one ROW is owed.** Its 30-root reduction reproduces at 0.260281397 and its 1.34951193 nm margin is unchanged. What is added is the second term of its own trade: at fixed geometry the reduction it recommends costs **+12.86 %** of the 90th percentile under the measured dropout. Against its own four plan floors (1.56–70.6× the 34-path margin) that is not a competing constraint, and the recommendation stands. |
| **`C-0074`** | **Nothing moves and the recommendation is STRENGTHENED.** Its 0.242359741, its 0.0682200897 minimax (re-run, `2.7e−12`) and its 1.76451193 nm margin all reproduce. Its recommended design reads **0.583664426** at the 90th percentile against `C-0063`'s **0.639129638** — **8.68 % better** under fabrication, and 10.30 % better under its own distribution. `CH-0103`'s reading that this is *"a property of the placement, not of the count"* is confirmed and now carries a decomposition: **+12.86 %** of count against **−19.0 %** of phase. |
| **`C-0075`** | **Nothing moves.** Its arm lengths, ceilings, margins and placed counts are read at run time and every one of them reproduces to the last digit. What is added is that its count table's **robustness** column now exists: at 34 → 30 it is +12.86 %, at 34 → 22 it is +62.0 %, and at 34 → 45 it is −0.98 % on a row its own margin already refuses (−0.941155731 nm). **The count and the arm are one variable, so the robust end of the axis is the unbuildable end.** |
| **`C-0071`** | **No headline moves and its recommendation stands.** Its `Q5` at 34 instances is the *robust* end of the buildable range as well as the self-consistent one: of the six swept counts, 34 is the largest that places with its own arm and the flattest of those under the dropout. What this adds to its exposure ledger is a **second** reason not to descend from 34, and it is the same direction as the plan one for once. |
| **`C-0089`** | **Its verdict stands, its density axis is CONFIRMED on the real lattice and one of its instruments is scoped.** Its 0.583664426 reproduces to `6.5e−11`; its count axis survives the transfer and is 3.07× steeper here than on its own abstract grid; and its ranking instrument is measured at ρ = 0.94–1.00 **across counts**, which locates `C-0098`'s ρ = 0.47 on the **phase** axis specifically. Its Deliverable 4 (*"the reversal is a COUNT effect"*) is **corrected in scope**: at matched count on the *same* phase the count effect is +12.86 %, and the 30-vs-34 reversal it reports is a phase effect of the opposite sign. |
| **`C-0087`** | **Nothing.** Its 0.501011167, 0.639129638 and 0.5733 all reproduce. |
| **`C-0098`** | **`CH-0119`.** Its Deliverable 3 slope of **−0.376769756** is measured over **placement-searched** subsets and is offered with a lattice mechanism. On the same lattice this claim measures a nested chain at −0.740086889 and a searched family at **+0.0610348337** — the same sign flip, from the same data, produced by nothing but letting the placement move. |
| **`ANSWERS.md` §4(g)** | The row should now say that the count axis was measured **at fixed geometry on the lattice** and is real, steeper than the abstract grid's, worth +12.86 % at the programme's own 34 → 30 move, and **out-weighed by the phase change that move carries**. |

## Still open — named, not answered

1. **Whether the coupling element's own incorporation is the staple's.** `C-0087`'s item 2,
   `C-0089`'s item 1 and `C-0098`'s item 5, unchanged, and still the only route by which this
   programme keeps a flat tile.
2. **Whether the count effect measured at phase 24 holds at the other 31 phases.** The phase term
   here is −19.0 % between two phases and `C-0098`'s phase axis is 1.95× on a different topology.
   Queued as **`T-178`**.
3. **Whether a count sweep with the DISTRIBUTION freed at every count reorders the cells.** The
   distribution is worth 1.30–1.61× on an array and no acceptance clause holds it equal across
   counts. Queued as **`T-179`**.
4. **What fraction of built tiles a flatness verdict is owed over** — `C-0087`'s item 4,
   unchanged, and the parameter the whole branch is most sensitive to.

## Challenges

**Raises [`CH-0119`](../challenges/CH-0119-a-redundancy-slope-measured-over-searched-subsets-is-not-a-count-slope.md)**
against `C-0098`'s Deliverable 3.

**None stands against this claim.** The four ways it would fail:

1. **A per-site incorporation measurement on a coupling-bearing tile showing a materially higher
   incorporation.** The whole verdict is a transfer of a plain rectangle's map.
2. **A count sweep at a different phase whose count term exceeds its phase term.** Everything
   here is phase 24 with one phase-8 comparison; the two axes are not swept together.
3. **A distribution freed at every count.** It is worth 1.30–1.61× on this topology and the
   count term is 1.13×, so it could in principle absorb the whole effect.
4. **A different verdict statistic.** At the median the ordering is unchanged, but the *size* of
   a 12.86 % term is a tail statement and the median is not the tail.
