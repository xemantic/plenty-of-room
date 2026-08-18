# C-0108 — **`C-0103`'s count effect does not hold at all 32 phases, and its decomposition is not one.** At fixed station geometry on a **search-free** nested family run at every crossover phase, the 34 → 30 reduction is adverse at **27 of 32** phases and **favourable at 5** (25 – 29, by up to **−4.59519576 %**), spanning **−4.59519576 to +12.2058991 %** against `C-0103`'s **+12.8596328 %** at phase 24. Over the whole 32 × 6 grid the balanced two-way additive fit in `log p90` leaves a worst residual of **7.72508874 %** of a level — **1.53234725×** its own 34 → 30 count main effect, and the **interaction carries more of the variation (9.79218189 %) than the phase main effect does (7.83610301 %)**. So *"the count term is +12.86 % and the phase term is −19.0 %"* is **one of two readings of the same journey**: on the published designs the other reading is **−11.4787354 %** of phase and **+2.9293362 %** of count, and on the search-free 2 × 2 the phase term **changes sign** between the two orderings. **The TOTAL is path-independent to `0.0` and the recommendation rests on the total, so it stands** — and at `C-0102`'s recommended phase **8** the count term is **+2.9293362 to +4.47532136 %**, 2.9 – 4.4× smaller than the figure the trade was priced at

| | |
|---|---|
| **Task** | [`T-178`](../tasks/T-178.md), raised by [`C-0103`](C-0103-path-count-at-fixed-geometry.md) (`T-163`) *Still open* item 2 and its own *"ways this claim would fail"* item 2 |
| **Leaf** | **`A8.2`** (the flatness of the tile), with **`A1.2`** for the anchoring scheme the coupling belongs to |
| **Verification type** | **logical** (a 32-row lattice census with no solve, a path-decomposition identity, and `C-0098`'s own published array grading read at run time — three cheap bounds, all before the sampler) **+ in-silico** (`C-0058`/`C-0063`/`C-0087`/`C-0089`/`C-0103`'s own exact Woodbury surrogate on `C-0009`'s grillage under `C-0022`'s **solved** load, one bank **per phase**, `C-0087`'s seeded Bernoulli dropout unchanged, 10 000 realisations per cell) |
| **Verdict** | **PASS on all five predicates, and the declared falsifier `F1` FIRED, as did `F2`.** Over **198 graded cells** — a search-free nested count chain at 22/25/28/30/34/45 paths at **every one of the 32 crossover phases**, each on **its own host**, plus six reference cells — the count axis is measured off phase 24 for the first time. **The count effect survives in DIRECTION at most phases and not at all of them**: the 34 → 30 reduction costs dropout robustness at **27 of 32** phases and **buys** it at **5** — phases 25, 26, 27, 28 and 29, by **3.84532453 to 4.59519576 %** — and its size runs **−4.59519576 to +12.2058991 %** against `C-0103`'s **+12.8596328 %**. **The two axes are not separable.** The balanced two-way additive fit in `log p90` over the 32 × 6 grid leaves a worst residual of **0.0744123213** log units, **7.72508874 %** of a level and **1.53234725×** the fit's own 34 → 30 count main effect of **4.97594096 %**; the variation splits **82.3717151 %** count, **9.79218189 %** interaction and **7.83610301 %** phase, so **the interaction is larger than the phase main effect**, and the decomposition closes to **0.0**. **On the move the programme actually recommends the split is path-dependent where the total is not.** Count first then phase reads **+12.8596328 %** and **−19.267547 %** — reproducing `C-0103`'s own two numbers — while phase first then count reads **−11.4787354 %** and **+2.9293362 %**, an interaction of **−8.79880284 %**, **0.761334254** of the count term it splits; on the search-free 2 × 2 the phase term is **+1.92730307 %** one way and **−3.92538749 %** the other, i.e. it **changes sign**. Both orderings share a total of **−8.88564999 %** (published) and **+6.48887743 %** (canonical) at a path disagreement of **0.0**. **The recommendation therefore stands on its endpoints and not on its attribution** — and at `C-0102`'s recommended phase **8** the count term is only **+4.47532136 %** (search-free) and **+2.9293362 %** (on `C-0074`'s own 30 roots grown to 34), so the trade `CH-0103` raised is **2.9 – 4.4× cheaper at the phase the programme now builds at** than at the phase it was priced at. **A cheap result nobody expected**: `C-0089`'s single-removal instrument **does** transfer across phases here — ρ = **0.882697947 to 0.978005865** over 32 phases at each of the six fixed counts, 6 of 6 scopes — which locates `C-0098`'s ρ = **0.468487481** on its own **shared-body** topology at **mixed** counts rather than on the phase axis as such. **Nothing is flat**: 192 of 192 canonical cells exceed `T-5b`'s 0.10 at **100 %** exceedance and 192 of 192 are worse than no coupling at all. Raises [`CH-0123`](../challenges/CH-0123-a-count-term-and-a-phase-term-are-one-of-two-readings-of-one-journey.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED.** The dropout **input** is `C-0087`'s, which is Strauss et al. (2018) read directly — a measurement of *staple* incorporation on a plain Rothemund rectangle at one folding protocol — and the out-of-plane motif every placement stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |
| **Provenance** | `gpd/results/T-178-count-phase-interaction.json`, produced by `coupling.CountPhaseInteractionStudyKt`; model in `src/main/kotlin/coupling/CountPhaseInteraction.kt` (**new file** — it **adds no method to any shared source** and composes `C-0103`'s `nestedRootChain`/`restrictEnsemble`/`rootStationIndices`/`rootStations`, `C-0089`'s `DropoutRobustPlacement`, `C-0087`'s `StapleDropout`, `C-0058`'s `NonUniformCoupling` and `C-0055`/`C-0063`'s `anchoring` lattice and influence bank as libraries); **3 cheap bounds, a 32-row census with no solve, 198 graded dropout cells at 10 000 seeded realisations each, 32 count-term rows, 6 two-way additive fits, 2 path splits, 39 rank-agreement scopes, 2 convergence axes, 14 reproductions, 5 predicates, 5 falsifiers, 12 findings**; **18 gate-named tests in `src/test/kotlin/coupling/CountPhaseInteractionTest.kt`**; the result file was **produced twice on separate snapshots**, run **A** and run **B** differing in **two deliberately edited `findings` strings and in no number anywhere** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** sheet, 15 duplexes at the SAXS-measured **2.69 nm**; **all 32 crossover phases, each carrying its own host** (7 or 8 columns) rather than a nominal layout; `C-0022`'s **solved** edge profile at **2 mM, 10 nm, 0.192 V**; `C-0017`'s **33.3333333 pN/nm** as a **SUM** at §3's **acceptable 3 nm**, shared **EQUALLY** at every count and every phase; free-tile stroke **4.90731102 nm**; dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; `C-0087`'s **`MEASURED_DEPTH`** incorporation field; **one** Bernoulli stream **per phase** over that phase's own inventory at seed **20260817**, **10 000** realisations, restricted per subset (**common random numbers within a phase**); decisions at 6 significant digits, emission at 9, difference-of-nearly-equal fields at **2** |
| **Consumes** | [`C-0103`](C-0103-path-count-at-fixed-geometry.md) (`nestedRootChain`, `restrictEnsemble`, `rootStationIndices`, `rootStations`, `rowsAreCentroSymmetric`; its **0.638498565** and **0.720607136** reproduced and its decomposition **challenged**), [`C-0089`](C-0089-dropout-robust-placement.md) (`DropoutEnsemble`, `dropoutDishingSample`, `summariseDropoutDishing`, `worstSinglePathRemoval`, `spearmanRankCorrelation`, `orderStatistic`; its **0.583664426** reproduced), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (**the whole dropout model** — `measuredDepthIncorporation`, `DropoutRandom`, `bernoulliPresence`, `solveWithDropout`; its **0.639129638** reproduced), [`C-0098`](C-0098-shared-body-placement-and-distribution.md) (its six graded **array** cells at full inventory, **read at run time** from `gpd/results/T-165-*.json` and used as cheap bound 2; its ρ = 0.468487481 **scoped**), [`C-0102`](C-0102-crossover-phase-selection.md) (the three phase strata and the seven-column host's price), [`C-0063`](C-0063-upward-root-placement.md) (`upwardRootLattice`, `UpwardRootInfluenceBank`, the 34-root placement read from `gpd/results/T-125-*.json`; its **0.0706145537** and **0.307902368** reproduced), [`C-0074`](C-0074-two-per-row-placement.md) (the recommended 30-root placement, read from `gpd/results/T-136-*.json`), [`C-0072`](C-0072-plan-tolerance-model.md)/[`C-0075`](C-0075-path-count-consistency.md) (the reduction rule and the count table), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`, `latticeInfluenceSurrogate`), [`C-0055`](C-0055-unused-junction-site.md), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (the **53**-site inventory at phase 24), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0026`](C-0026-one-row-per-duplex.md) (the free-tile stroke), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0015`](C-0015-crossover-phase-and-registration.md)/[`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile`, [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (`T-5b`'s 0.10) |
| **Raises** | [`CH-0123`](../challenges/CH-0123-a-count-term-and-a-phase-term-are-one-of-two-readings-of-one-journey.md) against `C-0103`'s Deliverable 2 decomposition |

---

## The claim, in one line

**A count term and a phase term are not two things a move is made of — they are two ways of cutting one journey in half, and on this lattice the two cuts disagree by more than the phase main effect is worth.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa** exactly;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward** — away
  from the grafted layer, which is **below** the tile. `w` is positive **downward**; the origin
  is the tile centre.
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit
  **plane**, on the same **81 × 81** grid as every flatness claim upstream. **Flat** means below
  **0.10** of the free-tile stroke — `T-5b`'s **convention**, not a physical threshold.
- **A dropout is a REMOVAL, not a perturbation** (`C-0087`).
- **The operating state is named**: `C-0022`'s **solved 2 mM / 10 nm / 0.192 V** profile. The
  **twelfth** time this programme has had to say which state a flatness verdict is read at.
- **The verdict statistic is the 90th percentile**, a nearest-rank order statistic, so every cell
  here is comparable with `C-0087`'s, `C-0089`'s, `C-0098`'s and `C-0103`'s.
- **Every phase carries its own host.** `C-0102` is explicit that the phases are not
  interchangeable — a seven-column sheet loses `6/7` of its series `D_⊥` where the smeared
  reading loses `7/8` — so the grillage is rebuilt at every phase and never carried.
- **A term is a LOG RATIO**, and a per cent is `100(e^term − 1)`. That is what makes the two
  orderings of a 2 × 2 add exactly, and it is why a path disagreement of `0.0` is a check and
  not a coincidence.

### The one convention that is not inherited, and it is what makes the grid readable

**The family is search-free.** `CH-0119` is this programme's own finding that a
placement-**searched** family measures the search and not the count — on one lattice a nested
chain fits **−0.740086889** and a searched one **+0.0610348337** — and `C-0102`'s is that a
descent compared against an exhaustive enumeration is not a comparison. Both are avoided by using
**one deterministic construction at every phase**: one root per row at the site nearest the tile
centre, grown by `C-0103`'s own addition rule. It contains no objective, no load and no descent,
so a difference between two of its cells is a difference of **phase** or of **count** and of
nothing else. `C-0063`'s exhaustively enumerated 34-root anchor is carried **beside** it at phase
24, both as the reproduction gate and as a measure of what the anchor rule is worth.

---

## The three cheap bounds, which ran before the grid

### Bound 1 — `C-0089`'s single-removal instrument over the same 32 × 6 grid

`n` solves per cell against 10 000, i.e. about **1/300** of a graded cell. Its own two-way
interaction is **14.4692631 %** of a level against the graded grid's **7.72508874 %**, and its
interaction *share* **11.8503753 %** against **9.79218189 %**.

> The cheap instrument **over-states the interaction by 1.87×** and gets its existence, its sign
> and its rank order right. It is a screen and not a measurement, and that is the honest reading
> of a bound run before an expensive calculation.

**One deviation from `T-178`'s Plan, stated rather than glossed.** The Plan describes the cheap
grid as a pass that runs before the grading. In the code it is computed **inside the same pass
over each phase's bank**, because a bank is what both need and rebuilding 32 of them to preserve
the ordering would have cost more than the cheap layer itself. The cost statement is unaffected —
`n` solves against 10 000 per cell — and the bound is emitted first and reported whatever it says;
what is not literally true is that every cheap cell preceded every graded one in time.

### Bound 2 — `C-0098`'s own published grading, read at run time and not retyped

`C-0098` Deliverable 1 grades the **array** at each phase's full upward inventory. Read out of
`gpd/results/T-165-shared-body-placement.json`, phase 24 at 53 ties is **0.612366061** and phase
8 at 52 ties is **0.65233453** — going from `C-0063`'s phase to `C-0074`'s is **+6.5268916 %**,
i.e. the phase runs the **opposite way** to the **−19.0 %** `C-0103`'s subtraction attributes to
it, at a nearly matched count.

> **This number was in the corpus before this task ran and it costs no solve.** It is not by
> itself a refutation — 53 against 52 ties is not a fixed count, and the placements are the full
> inventories — but it is the first evidence that the phase term is not a constant to be
> subtracted out.

### Bound 3 — the path identity, which says how expensive the headline is

In log units the two orderings of a 2 × 2 share their endpoints, so
`count(φ₀) + phase(30) ≡ phase(34) + count(φ₁)` identically, and the two splits differ by exactly
one number. **The interaction on the recommendation's own move is a function of four graded
cells**, not of the grid. The 32-phase grid buys the *generality* — whether the count term is a
constant of the coupling — and never the verdict on the recommendation.

---

## Deliverable 1 — the count term at every crossover phase

The 34 → 30 reduction at fixed station geometry, on the search-free family, graded at 10 000
realisations under common random numbers within each phase.

The table below is **assembled here from the 32 emitted `countTerms` rows**; every entry is one
of them, and no entry is an average. The four strata are made **exclusive** here: at 40.00 nm
`C-0063`'s two centro-symmetric phases are a **subset** of `C-0015`'s eight-column ten, not
disjoint from them — which is [`CH-0121`](../challenges/CH-0121-two-of-the-three-phase-demand-sets-are-identical-not-disjoint.md)'s
point read at the nominal width — so the eight-column row below carries the **other eight**.

| stratum (`C-0102`'s census) | phases | count term, min | count term, max |
|---|---|---|---|
| eight-column **and** centro-symmetric (`C-0063`'s two) | 2 | **+4.47532136 %** | **+10.8397678 %** |
| eight-column (`C-0015`'s ten) | 8 | **−4.59519576 %** | **+12.2058991 %** |
| richest inventory, seven-column (`C-0098`'s ten) | 10 | +3.79130623 % | +10.6567789 % |
| seven-column, neither richest nor symmetric | 12 | **−3.99738975 %** | +8.24447382 % |
| **all 32** | **32** | **−4.59519576 %** | **+12.2058991 %** |

- **`F1` FIRED.** The reduction is **favourable at 5 of 32 phases** — 25, 26, 27, 28 and 29, by
  **3.84532453 to 4.59519576 %** — so the count effect `C-0103` measured is **not one-signed
  across the lattice's phase family**. It is adverse at the other **27**.
- **And the size is not a constant either.** Over the 27 adverse phases it spans
  **2.18035792 to 12.2058991 %**, a factor of **5.6**, and the whole family spans **16.8**
  percentage points — where the **anchor rule** at one phase is worth **2.02** of them (the
  search-free family reads **+10.8397678 %** at phase 24 against `C-0063`'s own anchor at
  **+12.8596328 %**). **The phase moves the count term about eight times as far as the anchor
  rule does**, which is what licenses reading the grid.
- **Phase 24 is at the top of the family, not in the middle of it.** `C-0103`'s
  **+12.8596328 %** exceeds **every one** of the 32 search-free count terms — the largest is
  **+12.2058991 %** at phase 6 — and the grid's own count main effect is **+4.97594096 %**.
- **At `C-0102`'s recommended phase 8 the term is +4.47532136 %** on the search-free family and
  **+2.9293362 %** on `C-0074`'s own 30 roots grown to 34. The trade `CH-0103` raised is
  **2.9 – 4.4× cheaper at the phase the programme now builds at.**

**`C-0103`'s own `F1`, re-read.** Its falsifier asked whether the percentile is monotone
decreasing in the count over 22 → 45. On the **canonical** family it is monotone at **0 of 32**
phases, and the step census says where: **22 → 25** is non-decreasing at **30 of 32** phases,
**25 → 28** at **14**, **28 → 30** at **0**, **30 → 34** at **5** — the same five, 25 to 29 —
and **34 → 45** at **5**, which are 17 to 21 and a different five. The breaks are concentrated at
the **sparse end**, where a search-free rule with two roots in the widest row is at its worst.
This is a statement about an **anchor rule** as much as about the lattice, and it is reported as
such: what it establishes is that `C-0103`'s monotonicity is **not robust to the anchor rule**
below 30 paths, while the step its recommendation moves through has five exceptions out of 32.

## Deliverable 2 — the interaction, measured over the whole grid

A **balanced two-way additive fit** of `log p90` over 32 phases × 6 counts. On a balanced
complete design the decomposition is orthogonal, so `total = phase + count + interaction` holds
identically and is asserted as a gate rather than reported as a fit quality.

| | sum of squares | share |
|---|---|---|
| **count** (main effect) | 1.64901966 | **82.3717151 %** |
| **interaction** | 0.196032102 | **9.79218189 %** |
| **phase** (main effect) | 0.156872877 | **7.83610301 %** |
| total | 2.00192464 | 100 % |
| decomposition residual | **0.0** | — |

- **`F2` FIRED, on the limb `T-178` declared first.** The worst additive residual is
  **0.0744123213** log units, i.e. **7.72508874 %** of a level, against the fit's own 34 → 30
  count main effect of **0.0485610042** log units (**4.97594096 %**): a ratio of
  **1.53234725**. *The interaction is half again as large as the count term it splits.*
- **The interaction is larger than the phase main effect.** 9.79 % of the variation against
  7.84 %. A model that carries a phase term and no interaction term has the smaller of the two.
- **It is not a host effect.** Inside `C-0102`'s strata — sets of structurally comparable hosts —
  the worst residual is **2.46798189 %** (the two centro-symmetric phases), **6.71938892 %** (the
  eight-column ten), **4.1171798 %** (the richest ten) and **6.95114909 %** (the remaining
  twelve). It survives the stratification.

## Deliverable 3 — the recommendation's own 2 × 2, read both ways round

The move the programme takes is 34 roots at phase **24** → 30 roots at phase **8**. There are two
ways to cut it in half.

| | count first, then phase | phase first, then count |
|---|---|---|
| **on the published designs** (`C-0063`'s 34 at 24, `C-0074`'s 30 at 8) | **+12.8596328 %** then **−19.267547 %** | **−11.4787354 %** then **+2.9293362 %** |
| **on the search-free family** (one rule at both phases) | **+10.8397678 %** then **−3.92538749 %** | **+1.92730307 %** then **+4.47532136 %** |

| | published | search-free |
|---|---|---|
| the four corners | 0.638498565, 0.720607136, 0.565207004, 0.581763818 | 0.722404177, 0.800711112, 0.736327095, 0.769280099 |
| **total, either way** | **−8.88564999 %** | **+6.48887743 %** |
| **path disagreement** | **0.0** | **0.0** |
| **interaction** | **−8.79880284 %** | **−5.74202435 %** |
| interaction / count term | **0.761334254** | **0.574595429** |

- **`C-0103`'s two numbers are reproduced exactly as the FIRST ordering** — +12.8596328 % of
  count and −19.267547 % of phase — and the second ordering of the same journey between the same
  two designs gives **−11.4787354 %** and **+2.9293362 %**. The count term is **4.4×** smaller
  and the phase term **1.68×** smaller.
- **On the search-free 2 × 2 the phase term changes SIGN between the orderings**, +1.92730307 %
  against −3.92538749 %. There is no fact of the matter about "what the phase is worth" in this
  move.
- **The total does not move.** −8.88564999 % and +6.48887743 % respectively, at a path
  disagreement of exactly **0.0**. **The recommendation rests on the total.**

## Deliverable 4 — the cheap instrument does transfer across phases, and `C-0098`'s failure is narrower than the phase axis

| scope | pairs | Spearman ρ | transfers? |
|---|---|---|---|
| the whole canonical grid, phases and counts mixed | 192 | **0.986846567** | yes |
| **across phases at 34 paths** | 32 | **0.978005865** | **yes** |
| across phases at 45 paths | 32 | 0.978005865 | yes |
| across phases at 25 paths | 32 | 0.962243402 | yes |
| across phases at 30 paths | 32 | 0.926319648 | yes |
| across phases at 28 paths | 32 | 0.907991202 | yes |
| across phases at 22 paths | 32 | **0.882697947** | yes |
| **across counts**, within a phase | 6 each | **0.942857143 – 1.000000000** | yes, 32 of 32 |

**`F3` did not fire, and the result is the opposite of what `C-0098` measured.** Its
ρ = **0.468487481** is read on the **shared body**, over six phases, at each phase's **own full
inventory** — so its count moves with its phase (52, 53 and 60 ties). At **fixed count on the
array** the same instrument ranks all 32 phases at **0.88 – 0.98**. The axis the bound fails on
is therefore **narrower than "the phase"**, and a phase screen on the array can be run for `n`
solves a cell.

## Deliverable 5 — nothing on this branch is flat, at any phase

192 of 192 canonical cells exceed `T-5b`'s 0.10 at the 90th percentile, at an exceedance of
**1.0** — every realisation of every cell — and 192 of 192 exceed their own host's uncoupled
dishing. The percentile spans **0.682355843 to 1.02161009** of the free-tile stroke, and the
uncoupled tile itself spans **0.307902368 to 0.312235717** across the 32 hosts.

> So neither the count nor the phase decides an acceptance verdict on this branch. `C-0103` found
> this at one phase; it is now the whole lattice.

---

## The five verification gates

Executed as **18 gate-named tests** in `src/test/kotlin/coupling/CountPhaseInteractionTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the central root placement is one root per row and every root is a site of its own row, and it refuses an empty lattice and a row with no site; a two-way fit refuses a ragged grid, a single-level factor and a non-positive value; a 2 × 2 split refuses a non-positive percentile | **PASS** |
| **2 — limiting cases** | a **separable** grid has **exactly zero** interaction and its own row and column effects are recovered; a grid in which only one factor varies has zero interaction and zero column sum of squares; the canonical chain at its minimum **is** the central root placement; **the canonical family is NESTED at every one of the 15 ordered pairs of the six counts at every one of the 32 phases**, and every member carries exactly its own count; the construction contains no search and is a function of the lattice alone | **PASS** |
| **3 — symmetry and conservation** | **`F5`** — the uncoupled tile under a **uniform** load dishes exactly zero on `C-0063`'s eight-column host **and** on a seven-column one; the two-way sums of squares decompose exactly; **`F2`** — the two orderings of a 2 × 2 share their total to `1e−15` and their two interaction readings agree to `1e−15`; a 2 × 2 assembled from a separable grid has exactly zero interaction | **PASS** |
| **4 — numerical convergence and statistical power** | the canonical 34 → 30 **factor** at phase 24 at 1 250 / 2 500 / 5 000 / 10 000 realisations (1.10654554, 1.10968042, 1.10589174, 1.10839768; departure **0.0025**), which is the axis `F2` is decided on and is read under common random numbers; the **mean** over 200 realisations on the **41 / 81 / 161** dishing grid (0.612999584, 0.613047355, 0.61309923; departure **5.2e−05**) — `C-0087`'s cure for the degenerate nested-grid percentile; a ratio-settling test in the suite at 250 / 500 / 1 000; departures emitted at **two** significant digits and no step counter anywhere | **PASS** |
| **5 — literature and upstream cross-check** | **eight strict reproductions, worst departure `3.8e−09`**: `C-0103`'s **0.638498565** and **0.720607136** (both re-graded from its own chain A on `C-0063`'s own anchor), `C-0063`'s **0.0706145537**, the uncoupled tile's **0.307902368**, `C-0087`'s **0.639129638**, `C-0089`'s **0.583664426**, `C-0026`'s **4.90731102 nm** and `C-0066`'s **53** sites; and six of `C-0098`'s graded array cells carried through unchanged | **PASS** |

---

## The declared falsifiers, and what actually happened

Declared in [`T-178`](../tasks/T-178.md) **before** execution.

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **`F1`** | **the declared one** — the count term is not one-signed across the phase family | **YES** | favourable at **5 of 32** phases (25 – 29), adverse at 27; the term runs **−4.59519576 to +12.2058991 %** |
| **`F2`** | the two axes are not separable — the worst additive residual is at least as large as the count term it splits, **or** the 2 × 2 interaction exceeds half of `C-0103`'s +12.86 % | **YES, on limb A** | worst residual **0.0744123213** log units against a count main effect of **0.0485610042**, a ratio of **1.53234725**. Limb B did **not** fire: **−5.74202435 %** against **6.4298164 %** |
| **`F3`** | the cheap instrument fails to transfer across counts here too | **NO** | ρ = 0.942857143 – 1.000000000 across counts (32 of 32 scopes) and **0.882697947 – 0.978005865** across phases at fixed count |
| **`F4`** | a standing figure fails to reproduce | **NO** | worst strict departure **`3.8e−09`** over eight reproductions |
| **`F5`** | the uncoupled tile dishes non-zero under a **uniform** load on any of the 32 hosts | **NO** | worst **`9.25e−10`** of the free-tile stroke, wired as a test on two hosts |

**Three results that were not anticipated.**

1. **That the count term changes SIGN at five phases.** The task was formulated expecting the
   sign to hold and the size to move. Phases 25 – 29 are eight-column and seven-column hosts on
   which the search-free 30-root member is *better* than the 34-root one under fabrication, by
   up to 4.6 %.
2. **That the interaction is larger than the phase main effect.** 9.79 % of the variation against
   7.84 %. The phase is the axis two claims have spent effort on and it is the smaller of the two
   things that are not the count.
3. **That `C-0089`'s cheap instrument transfers across phases after all.** `C-0098` measured
   0.468487481 and `C-0103` located that failure "on the phase axis". It is not the phase axis:
   at fixed count on the array the same instrument ranks 32 phases at 0.88 – 0.98. What
   `C-0098` measured is a **shared body at mixed counts**.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. The dropout input is measured; nothing derived
  here is.
- **The incorporation field, its conventions and the independence of realisations are `C-0087`'s
  UNCHANGED**, and this claim inherits its whole validity range: a plain Rothemund rectangle at
  one folding protocol, a coupling path whose own incorporation nobody has measured, and
  independence as a convention rather than a measurement.
- **ONE anchor rule over the grid.** The canonical family is search-free and therefore matched
  across phases, but it is a **third** rule beside `C-0103`'s two, and `C-0103` measured up to
  6 % of rule dependence between its own chains below 30 paths. Measured here at phase 24 the
  rule is worth **2.02 percentage points** of the count term; the phase is worth **16.8**. The
  *levels* carry the rule dependence, and the *signs* and the *interaction* are what is read.
- **The published-adjacent 2 × 2 has ONE constructed corner.** 34 roots at phase 8 is `C-0074`'s
  30 grown by an addition rule, not an optimum, so its anchor quality is not matched to
  `C-0063`'s exhaustively enumerated 34 at phase 24. The **search-free** 2 × 2 carries no such
  asymmetry and is the primary reading; the two agree in every qualitative respect and differ in
  the size of the interaction (−8.80 % against −5.74 %).
- **EQUAL springs at every cell**, deliberately. The distribution axis is `C-0089`'s
  (1.30–1.61× on an array) and is `T-179`'s; nothing here bounds the three-way interaction of
  count, phase and distribution.
- **Common random numbers WITHIN a phase, not across phases.** Two phases carry different site
  inventories, so their streams cannot be shared; every quantity differenced across phases here
  is a **ratio already formed within a phase**, which is where the pairing is needed. The
  convergence axis measures the within-phase ratio's own floor at **0.0025** against terms of
  0.02 – 0.12.
- **ONE width and ONE load state.** 40.00 nm, because the reproduction gates are written there,
  and `C-0022`'s solved 2 mM / 10 nm / 0.192 V. `C-0086`/`C-0090`/`C-0102` have moved the
  buildable width to **38.08 nm**, where the census collapses to `{0, 16}` and `{8, 24}` and the
  congruences differ; and `C-0068` has shown a placement can reverse between layer heights.
- **`T-5b`'s 0.10 is a CONVENTION.** At any convention nothing here changes: the best cell in the
  grid is 0.682355843.
- The dishing pipeline, the lattice, the hosts, the load and the free-tile stroke are `C-0058`'s,
  `C-0063`'s, `C-0087`'s, `C-0089`'s and `C-0103`'s unchanged, and inherit `C-0022`'s unsourced
  rim charge and `C-0001`'s single foundation secant.
- **Single layer, static, 300 K, aqueous 2 mM MgCl₂.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| staple incorporation map | 168 cells, 48–95 %, mean 84 % | **CITED, MEASURED**, Strauss et al. (2018) **through `C-0087`** |
| `C-0098`'s six graded array cells | 0.612366061, 0.612456935, 0.63217778, 0.65233453, 0.651648203, 0.651676669 | **CITED**, read at run time from its own result file |
| `C-0098`'s rank correlation across phases | 0.468487481 | **CITED**, and **scoped** here |
| `C-0103`'s count term and phase term | +12.8596328 %, −19.0 % | **CITED**, and both **reproduced** here as one ordering of the 2 × 2 |
| `C-0063`'s and `C-0074`'s placements | 34 and 30 roots, read at run time from their own result files | **CITED** |
| `C-0022`'s solved collar | 2 mM / 10 nm / 0.192 V, read at run time | **CITED** |
| `CH-0119`'s two slopes | −0.740086889, +0.0610348337 | **CITED** from `C-0103` |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| `T-5b`'s tolerance | 0.10 | **CITED CONVENTION** |

Everything else — the 32-row census, the 198 graded cells, the 32 count terms, the six two-way
fits, the two path splits, the 39 rank scopes, the two convergence axes and the eight strict
reproductions — is **derived here in code**.

## What this does to the standing claims

| claim | what moves |
|---|---|
| **`C-0103`** | **Every number reproduces and its Deliverable 2 decomposition is CHALLENGED — [`CH-0123`](../challenges/CH-0123-a-count-term-and-a-phase-term-are-one-of-two-readings-of-one-journey.md).** Its 0.638498565 and 0.720607136 reproduce to `4.6e−10` and `2.3e−10`, and its +12.8596328 % and −19.0 % are recovered exactly as the *first* ordering of the 2 × 2. What is challenged is that they are **two terms of a decomposition**: the second ordering of the same journey gives −11.4787354 % and +2.9293362 %, the interaction is −8.79880284 %, and on a search-free family the phase term changes sign between the orderings. Its verdict — `CH-0103` upheld as a bookkeeping correction, the 34 → 30 recommendation stands — is **untouched**, because the total is path-independent. Its *Still open* item 2 is **DISCHARGED**, and its own *"ways this claim would fail"* item 2 is answered: **a phase whose count term exceeds its phase term exists**, at five phases the count term is favourable outright, and the count effect it measured is the second largest in the family. |
| **`C-0072`** | **No number moves and the trade gets CHEAPER at the phase now recommended.** Its 34 → 30 reduction costs dropout robustness at 27 of 32 phases, so the missing column `CH-0103` named is general in direction; but at `C-0102`'s recommended phase **8** it costs **+2.9293362 to +4.47532136 %** rather than +12.86 %, against a plan margin that still improves 68.8998088×. |
| **`C-0074`** | **Nothing moves.** Its 0.583664426 reproduces to `6.5e−11`. Its recommended design's advantage over `C-0063`'s is confirmed as a **joint** property of the two endpoints and cannot be attributed to the phase by subtraction: the phase's share of it is −19.267547 % or −11.4787354 % depending on the order the split is taken in. |
| **`C-0075`** | **Nothing moves.** Its count table's robustness column, which `C-0103` supplied at one phase, is now known at 32 — and it is **not a column of the table**, because it depends on the phase as much as on the count. |
| **`C-0071`** | **No headline moves and its `Q5` at 34 instances is STRENGTHENED at the phase it is built at.** At phase 8 the 34-root member is the more robust of the two under the measured dropout (+4.47532136 % to go to 30), and 34 is the robust end at 27 of 32 phases. |
| **`C-0098`** | **Its verdict stands and one of its instruments is SCOPED, in its favour.** Its six graded array cells reproduce unchanged and are used as this claim's cheap bound 2. Its ρ = 0.468487481 is not a statement about the phase axis as such: at **fixed count on the array** the same instrument ranks all 32 phases at 0.882697947 – 0.978005865. What it measured is a **shared body at mixed counts**, and that is a narrower and more useful reading of its own finding. |
| **`C-0089`** | **Nothing moves.** Its 0.583664426 reproduces and its ranking instrument is measured on a third scope. |
| **`C-0102`** | **Nothing moves and its stratification does work.** Its three demands cut the 32 phases into sets inside which the interaction survives (2.47 – 6.95 % of a level), so the interaction is not a host effect. Its recommendation of phase 8 is where the count term is smallest among the eight-column hosts. |
| **`ANSWERS.md` §4(g)** | The row should now say that the count axis is real **in direction at 27 of 32 phases and reversed at five**, that its size is a function of the phase (−4.6 to +12.2 %), and that the *"count term against phase term"* split of the recommended move is **path-dependent** while its total is not. |

## Still open — named, not answered

1. **Whether the coupling element's own incorporation is the staple's.** `C-0087`'s item 2,
   unchanged, and still the only route by which this programme keeps a flat tile.
2. **Whether a count sweep with the DISTRIBUTION freed at every count reorders the cells, and
   whether the interaction survives it.** Queued as **`T-179`**.
3. **Why the count term reverses at phases 25 – 29 and nowhere else.** Those five are contiguous
   and straddle `C-0015`'s eight-column set; nothing here explains the mechanism, and a lattice
   mechanism would be worth more than the measurement. Queued as **`T-187`**.
4. **The same grid at `C-0086`'s buildable 38.08 nm.** The congruences differ, the inventory
   collapses to two phases per demand, and the strata are not the same sets. Queued as
   **`T-188`**.
5. **What fraction of built tiles a flatness verdict is owed over** — `C-0087`'s item 4,
   unchanged, and the parameter the whole branch is most sensitive to.

## Challenges

**Raises [`CH-0123`](../challenges/CH-0123-a-count-term-and-a-phase-term-are-one-of-two-readings-of-one-journey.md)**
against `C-0103`'s Deliverable 2. Its discharge is queued as **`T-186`** and is editorial: every
number the replacement sentence needs is already emitted.

**None stands against this claim.** The four ways it would fail:

1. **A per-site incorporation measurement on a coupling-bearing tile showing a materially higher
   incorporation.** The whole grid is a transfer of a plain rectangle's map.
2. **A second anchor rule over all 32 phases whose count term is one-signed.** The rule is worth
   2.02 percentage points at phase 24 and the sign reversal at phases 25 – 29 is 3.8 – 4.6 %, so
   a rule three times as influential as the one measured could remove it.
3. **A distribution freed at every count.** It is worth 1.30–1.61× on this topology and the
   count main effect is 1.05×, so it could absorb the whole grid.
4. **A different verdict statistic.** The interaction is a tail statement; at the median the
   ordering of the count terms need not be the same.
