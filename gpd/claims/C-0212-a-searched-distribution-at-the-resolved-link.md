# C-0212 — **`C-0208`'s `0 of 64` REVERSES THE MOMENT THE DISTRIBUTION IS SEARCHED — `22 of 32` CELLS ARE FLAT AT THE 90TH PERCENTILE, TIGHTEST `0.0647024141` AGAINST A TRANSFERRED `0.100198485` — AND A BARE READING OF THAT COUNT WOULD BE THE MOST MISLEADING SENTENCE THIS CORPUS COULD PUBLISH.** Every coupled census here grades on `C-0058`'s **equal springs** and its **rim-graded 5:1**, both *rules transferred onto* the lattice and neither an *optimum of* it; composing `C-0135`'s smoothed minimax with a multi-start descent on the **true** training percentile, graded **out of sample** on `C-0208`'s own 4 000-realisation stream, closes the `0.198 %` its tightest cell missed — **`1.45251772×`** at that very cell, paired — and clears `T-5b` at 22 of the same 32 cells — `0 of 32` on either transferred rule, at a worst reproduction departure of **`4.0E-9`**. **Three things must travel with that count and none of them is in the study's own verdict block.** **(1) `NOT ONE` of the 32 cells is flat and inside BOTH of the study's two per-path thresholds** — `ratioInsideBuildableWindow` `12 of 32`, `peakInsideUnzipCeiling` `3 of 32`, both together **`0 of 32`** — **but one of those two flags is not the threshold it is named after**: `C-0060`'s `3.5 ≤ R ≤ 20` is its **Deliverable 1**, *"the flat ratio window, MEASURED"*, a sweep of the one-parameter rim rule's ratio on `C-0058`'s **square-lattice 45-station** design reporting where **that design stays FLAT** — it is not a buildability constraint, `C-0060` puts **no ceiling on `R` at all**, and this study measures flatness **directly** (`CH-0273`). Read on the one physical per-path threshold, `C-0023`'s 10 pN unzip allowable over §3's acceptable 3 nm stroke, **`3 of 32` cells are flat AND admissible, all three on the rooting helices**, and at two of them the **two-level projection `C-0060` actually measures** is flat, inside `[3.5, 20]` and inside the allowable too. **(2) `0 of 32` searched cells beat the UNCOUPLED tile at the 90th percentile** — `0.0448134881` / `0.0469005226` against a best searched `0.0647024141` — while **`16 of 32` beat it at ZERO defects** (best `0.0298112409`), so what the coupling costs is `C-0087`'s measured dropout and not the coupling; and the coupling is not there for flatness (`CH-0272`). **(3) The ratio and the peak are linked in ONE direction only, by a theorem and not by a correlation**: the mandate gives `peak ≤ R·S/n` exactly, so `R ≤ n/10` is **sufficient** for the allowable and **nothing** is necessary — measured, the correlation of `log R` against `log(peak)` over the 32 cells is **`−0.0949781`**. **`F1`, `F2`, `F11` and `F14` were declared OPEN and all four FIRED**; `F3`, `F13` and every closed falsifier did not

| | |
|---|---|
| **Task** | [`T-316`](../tasks/T-316-a-searched-distribution-at-the-resolved-link.md) — raised by [`C-0208`](C-0208-a-bond-link-is-two-mechanisms.md) (`T-310`) §2 and §11, which sharpens [`C-0205`](C-0205-what-link-stiffness-the-recovery-needs.md)'s own last open question with a number |
| **Leaf** | **`A8.2`** |
| **Verification type** | **in-silico** (the same honeycomb grillage, the same `C-0208` per-bond link, the same stations, the same mandate, the same 4 000-realisation dropout stream — only the distribution moves) **+ logical** (the oracle floor is a pointwise theorem and needs no search; the `peak ≤ R·S/n` relation is one line of algebra on `C-0017`'s mandate) |
| **Verdict** | **PASS on all six predicates.** Of the fourteen declared falsifiers **`F1`, `F2`, `F11` and `F14` FIRED**, all four having been declared **OPEN**, so *"either answer is the result"*; `F3` and `F13` were declared open and did not fire; the eight closed falsifiers did not fire. **`C-0208`'s `0 of 64` is NOT withdrawn** — it is exact on the two transferred distributions, and this study reproduces all 64 of its published readings at `4.0E-9`. Raises [`CH-0272`](../challenges/CH-0272-a-flat-count-is-not-an-admissible-design.md) and [`CH-0273`](../challenges/CH-0273-the-buildable-ratio-window-is-a-flatness-window.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Every number is a property of one lattice, one placement family, one raster, one load case and one dropout model, read at a **radial** link constant `C-0208` records as unsourceable and carries as a bracket. No such coupling has been drawn, let alone folded, and the searched distribution's **placement** — which is what `C-0060` says actually fails — is not priced here at all |
| **Provenance** | [`gpd/results/T-316-a-searched-distribution-at-the-resolved-link.json`](../results/T-316-a-searched-distribution-at-the-resolved-link.json), written by [`tile/SearchedDistributionStudy.kt`](../../src/main/kotlin/tile/SearchedDistributionStudy.kt) (**new**) on [`tile/SearchedDistribution.kt`](../../src/main/kotlin/tile/SearchedDistribution.kt) (**new**). **19 named tests** written first and watched fail — [`tile/SearchedDistributionTest.kt`](../../src/test/kotlin/tile/SearchedDistributionTest.kt), which did not compile against a model that did not exist — and a **16-mutation** harness at [`tools/T-316-mutation-test.py`](../../tools/T-316-mutation-test.py) — **0 survivors over a subtracted baseline of 0** (`CH-0237`), after a first run of 15 whose **three survivors were three real guard gaps** (§10) — declared in `tools/P-31-harness-census.py` and wired as `testSearchedDistributionMutations` in the same commit. **No existing Kotlin main source was modified** except one hand-added line of `structure/ResultInputs.kt`, the generated handle registry — provably inert, `ResultInputs.all` being read at 9 sites, all inside `structure/ResultInputsTest.kt`. `tile/HoneycombGrillage.kt` was **not touched**: `C-0208` already built the per-bond link this task grades at. **`F12` — two independent runs produce a byte-identical result file — is discharged by a second emission in a separate snapshot, diffed against the artifact** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, `C-0022`'s design state — 10 nm gap, `0.192 V`, its solved collar; cross-section **`10 × 6`**, block extent **`116 bp`** (`edgeX` 39.44 nm, `edgeY` 38.04 nm), the drawable **`102 / 109`** raster, 435 staple bonds and 59 raster turn ties present; `d` = 2.536 nm (SAXS); `k_θ` = 13.5294118 pN·nm/rad; transverse link pinned at `C-0205`'s ceiling **`254.808095 pN/nm`** throughout and the **headline** radial rung at `C-0208`'s bracket floor **`754.005141`**, giving a through-thickness link of **`629.20588`**; composite fractions **0.30 and 0.26** (`C-0116`); `C-0017`'s mandate **`33.3333333 pN/nm` on the SUM**, §3's *acceptable* clause; **grading** ensemble seed `197197`, **4 000** realisations (`C-0208`'s own); **training** ensemble seed `316316`, **120** realisations, disjoint in seed — **`T-316`'s plan declared 200 and the run used 120**, a departure named and priced in §13; dishing on an **81 × 81** grid and the search's own on 41; `subdivisions = 1`; rim band 6.7 nm; `T-5b` = 0.10 |
| **Consumes** | [`C-0208`](C-0208-a-bond-link-is-two-mechanisms.md)/`T-310` (the per-bond link, the five radial rungs, and **all 64 published cells, reproduced**), [`C-0205`](C-0205-what-link-stiffness-the-recovery-needs.md)/`T-303` (the shear ceiling), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (the grillage port, the placements, the station sets, the grading construction), [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) (the tied lattice), [`C-0058`](C-0058-non-uniform-coupling.md) (the two transferred rules, the Woodbury surrogate, the coordinate descent), [`C-0135`](C-0135-descent-manifold-width.md) (the smoothed-minimax cure and `searchDecision`), [`C-0089`](C-0089-dropout-robust-placement.md) (the percentile objective, the oracle floor, the single-path removal), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (the **measured** depth incorporation), [`C-0060`](C-0060-buildable-stiffness-ratio.md) (the ratio axis and its `3.5 ≤ R ≤ 20` — **re-read here as what it is**, `CH-0273`), [`C-0023`](C-0023-two-sided-coupling.md) (the 10 pN unzip allowable), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, as a **sum**), [`C-0104`](C-0104-row-end-prestrain.md) (a prestrain is a load, which is what makes the bank a compliance), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0151`](C-0151-closing-raster-selection.md), [`C-0022`](C-0022-tile-edge-load-profile.md)/`T-3b` |
| **Raises** | [`CH-0272`](../challenges/CH-0272-a-flat-count-is-not-an-admissible-design.md), [`CH-0273`](../challenges/CH-0273-the-buildable-ratio-window-is-a-flatness-window.md) |

---

## The claim, in five lines

`C-0208` read `0 of 64` at every rung of the resolved per-bond link, and its tightest cell missed
`T-5b`'s `0.10` by **`0.198 %`**. Those 64 readings are **32 cells × 2 distributions**, and both
distributions are *rules transferred onto* the lattice.

**Searched, the same 32 cells read `22 of 32` flat**, tightest **`0.0647024141`** against the
transferred **`0.100198485`** — the study's own *"a gain of 54.9 %"* where `0.198 %` was needed.
**That headline compares two minima taken at two DIFFERENT cells** (`1.5486` on those two emitted
numbers), so the paired per-cell readings are the honest ones and the file emits them: `1.45251772`
at the tightest transferred cell and `1.70065256` at the tightest searched one, over a whole-sweep
`outOfSampleGain` of `1.10434917`–`1.70065256`.

**And a design needs more than a flat count.** On the study's own two per-path flags **no cell of
the sweep satisfies both**; one of those flags is a **flatness** window borrowed from another
lattice rather than a buildability test (`CH-0273`); and read on the one physical per-path
threshold there are **3** flat, admissible cells.

**No searched cell beats the uncoupled tile at the 90th percentile, at `0 of 32`** — and `16 of 32`
beat it at zero defects, which locates the cost in `C-0087`'s measured dropout rather than in the
coupling (`CH-0272`).

**The count is out of sample; which cell is tightest is not.** The census is read on a stream the
search never sees; *tightest of 32* is an order statistic and carries a selection the count does
not.

## 1. The two cheap bounds ran first, and neither needed a search

**Bound 1 — the oracle `p90` floor**, `InfluenceSurrogate.reachableDishingFloorAt` over every
realisation: a pointwise lower bound on the peak dishing of **every** distribution whatever,
because dishing is affine in the attachment forces and optimising over forces relaxes optimising
over stiffnesses.

| | |
|---|---|
| the floor over the 32 cells | **`5.14335957E-4`–`0.00182090471`** of the stroke |
| how far the best transferred distribution sits above it | **`76.9529259`–`209.690778×`** |
| cells it **excludes** outright | **`0` of 32** |

`CLAUDE.md` states the limitation in advance and it is reproduced here on a second lattice: **a
floor can EXCLUDE and can never ADMIT.** Excluding nothing, it is a measurement of how much room
the search had — two orders of magnitude of it — and not a licence.

**Bound 2 — the spread the transferred family already shows.** At `C-0208`'s tightest cell the
equal-spring reading is **`0.123631078`** and the rim-graded one **`0.100198485`**, a ratio of
**`1.23386175`**: the one-dimensional family the corpus has always graded on is **23.4 %** wide on
the deciding statistic, against the **`0.198 %`** to be closed. One division on a committed file,
and it says the search is worth running.

**And the bank is free.** An `InfluenceSurrogate` is a property of the **structure**; a
distribution enters the Woodbury system as a **diagonal**. One bank per
`(placement, columns, fraction, rung)` — **110** point-load lattice solves — serves every
distribution ever tried at that cell, at one `n × n` Cholesky each. That is what makes this a
study rather than a proposal.

## 2. The answer, at the bracket-floor rung

The 32 cells are **4 placements × 4 column counts × 2 composite fractions**, at 10, 20, 30 and 50
paths.

| | |
|---|---|
| flat at the 90th percentile on a **transferred** rule | **`0` of 32** |
| flat on the **searched** rule | **`22` of 32** |
| tightest transferred (`C-0208`'s own) | **`0.100198485`** |
| tightest searched | **`0.0647024141`** |
| cells where the search **wins** out of sample | **`32` of 32** |
| the worst out-of-sample reading, as a multiple of the transferred one | **`0.905510711`** |

Flat by column count — a count over this study's own `cells[*]` records — is **4 / 2 / 8 / 8** of 8
at 1 / 2 / 3 / 5 columns: **not monotone in the path count**, which is `CLAUDE.md`'s own
*an attachment coupling can be a NET DISHING SOURCE* read on a searched distribution rather than on
a transferred one.

**The tightest searched cell is not the tightest transferred one.** Searched, it is `f = 0.30`, the
**determined station lattice**, `5 × 10 = 50` paths (`0.0647024141`, from a transferred
`0.110036326`); `C-0208`'s tightest is the **abstract grid on the rooting helices** at the same
fraction and path count (`0.100198485`, searched to `0.0689826248`). *Tightest of 32* moves with
the distribution, which is exactly why it is an identification and not the result.

## 3. `CH-0272` — a flat count is not an admissible design, and the study's verdict block reports no conjunction at all

The result file emits three per-cell booleans. Its `verdict` and `findings` blocks report the
first, report the second and third separately as *values*, and never report any **conjunction**.

| over the 32 cells | count |
|---|---|
| `flatAtP90` | **22** |
| `ratioInsideBuildableWindow` (`3.5 ≤ R ≤ 20`) | **12** |
| `peakInsideUnzipCeiling` (`≤ 3.33333333 pN/nm`) | **3** |
| `ratioInsideBuildableWindow` **and** `peakInsideUnzipCeiling` | **0** |
| all three | **0** |
| `beatsUncoupledAtP90` | **0** |

*(counts over this study's own `cells[*]` records)*

**The empty conjunction is not an anti-correlation, and the corpus must not be told that it is.**
The Pearson correlation of `log(searchedRatio)` against `log(searchedPeakStiffness)` over the same
32 records is **`−0.0949781`** — essentially zero. What *does* link the two is a **one-sided**
theorem, and it is one line of algebra on `C-0017`'s mandate:

> With `n` paths summing to `S` and a max/min ratio `R`, `S ≥ n·min = n·peak/R`, so
> **`peak ≤ R·S/n`, exactly.** Hence **`R ≤ n·A/S` is SUFFICIENT** for a per-path allowable `A`;
> and since `S = 100/3 pN/nm` and `A = 10/3 pN/nm`, `S/A` is **exactly ten** and the sufficient
> region is **`R ≤ n/10`** — `1 / 2 / 3 / 5` at 10 / 20 / 30 / 50 paths.
> It is **not necessary**: `peak ≥ S/(n − 1 + 1/R)`, so a large `R` forces nothing at all.

Two consequences, both derived and both checked against the 32 records (`0` violations of the
inequality):

- **`0 of 32` searched cells sit inside the sufficient region.** Their ratios run
  **`6.87455826`–`191.010656`** against a sufficient region that never exceeds `R ≤ 5`.
- **The sufficient region meets `C-0060`'s `[3.5, 20]` only at 50 paths**, in `[3.5, 5.0]`; at 10,
  20 and 30 paths the two do not intersect at all.

## 4. `CH-0273` — and one of those two flags is not a buildability test

`C-0060`'s `3.5 ≤ R ≤ 20` is its **Deliverable 1**, headed *"the flat ratio window, MEASURED"*: a
21-point sweep of the one-parameter rim rule's ratio on `C-0058`'s **square-lattice, 45-station**
surrogate, tabulating **dishing** and reporting the range over which **that design stays flat**.
Its `R = 1` end reads `0.2182` — uniform springs, which are the easiest thing in the catalogue to
build. `C-0060`'s own buildability answer is **YES on the stiffness at every element**, on
**granularity** (1.0–19.1 % of a level, 25× finer than the window), and **NO on the placement**;
it states **no ceiling on `R` whatever**.

So `T-316`'s `buildableRatioWindow`, `ratioInsideBuildableWindow` and its verdict's *"`C-0060`'s
measured buildable window"* read a **flatness proxy from another lattice** as a **buildability
constraint** — and this study measures flatness **directly, on its own lattice, at every cell**.
The mis-reading is **not this study's invention**: `C-0135` and `CLAUDE.md`'s own
*straddling `C-0060`'s buildable `3.5 ≤ R ≤ 20`* carry it in the **name**, `TASKS.md`'s `T-226` row
repeats it, and `C-0089` carries a milder form — it names the window correctly, *"its flat-ratio
window"*, and then infers *"so buildability is not the constraint"*. `C-0060` itself, `C-0064` and
`ANSWERS.md` all say **flat**. `CH-0273`.

**Read on the one physical per-path threshold the answer is not zero.**

| the three cells flat AND inside `C-0023`'s allowable | `p90` | peak per path | `R` | transferred |
|---|---|---|---|---|
| `f = 0.26`, abstract grid on the rooting helices, `3 × 10 = 30` | **`0.0990040894`** | `2.83462695` | `73.5132043` | `0.109335084` |
| `f = 0.30`, abstract grid on the rooting helices, `5 × 10 = 50` | **`0.0689826248`** | `2.75295363` | `114.271875` | `0.100198485` |
| `f = 0.30`, determined station lattice on the rooting helices, `5 × 10 = 50` | **`0.078544978`** | `2.90149312` | `64.5836107` | `0.106508519` |

All three are **on the rooting helices**, and the middle one is `C-0208`'s own tightest cell.

**What `C-0060` really charges for a ratio of 191 is a LENGTH, and it is a placement question.**
On `C-0060`'s own exponents — `k ∝ p^(−3)` bending, `k ∝ p^(−2)` hinge — a ratio of `191.010656`
needs the soft member **`5.75907232`** to **`13.8206605×`** longer than the stiff one, against
`1.70997595`–`2.23606798×` at `R = 5`, where `C-0060` already reports that six of seven elements
cannot lay 45 stations out. **That is the real obstruction and this study does not price it.**

## 5. `CH-0272`'s second half — no searched cell beats the uncoupled tile, and the scoping matters

| | |
|---|---|
| the uncoupled `10 × 6` block at the resolved link, `f = 0.30` | **`0.0448134881`** |
| the same at `f = 0.26` | **`0.0469005226`** |
| the best **searched** `p90` over 32 cells | **`0.0647024141`** |
| cells whose searched `p90` beats the uncoupled tile | **`0` of 32** |
| cells whose searched **zero-defect** dishing beats it | **`16` of 32**, best **`0.0298112409`** |

`CLAUDE.md`'s *always run the uncoupled tile as the reference* and *an attachment coupling can be a
NET DISHING SOURCE*, met after the distribution has been **freed** — the strongest form of it this
corpus has, because the distribution is no longer a constrained rule that could be blamed.

**And the scoping is the whole of what it means.** At zero defects the coupling is a flatness
**improvement** at half the cells; what makes it a net source is `C-0087`'s **measured** staple
dropout, which is `C-0167`'s own finding reproduced. **This is not an argument for removing the
coupling**: `C-0017`'s mandate is a **placement and stability** requirement, the stroke and the
lateral confinement want ties too, and none of those is a flatness argument. The statement is
narrower and it is exact: **on this lattice, at these placements, under the measured dropout,
flatness is not what the coupling buys.**

The comparison is a percentile against a point, and it is right that way round: the uncoupled tile
has no coupling paths, so `C-0087`'s dropout does not act on it and its dishing is a single
deterministic number.

## 6. What the search is worth, in sample and out

| | over the 32 cells |
|---|---|
| in-sample gain (the training percentile, against the best transferred start) | **`1.24035436`–`1.77724701×`** |
| out-of-sample gain (the grading percentile, same comparison) | **`1.10434917`–`1.70065256×`** |
| cells that **lose** out of sample (`F3`) | **`0`** |
| the worst out-of-sample reading, as a multiple of the transferred one | **`0.905510711`** |

The in-sample side is a **property of the composition and not a hope** (`F4`): the percentile
descent is seeded from **both** transferred rules *and* from the smoothed minimax's answer, and
evaluates every start before moving from it, so it cannot report worse than the best of its own
comparands.

**And the two objectives are genuinely different objects**, which is why the composition is a
composition. `C-0135`'s smoothed minimax optimises the **zero-defect peak** and reaches
`0.0039774866` of the stroke there — and its `p90` under dropout runs
**`0.0902335479`–`0.150351378`**, worse than the percentile-searched answer at every cell. A max
of smooth functions is not an order statistic, and the study does not pretend otherwise.

## 7. Fragility, and the projection that buys back the axis it is graded on

`CLAUDE.md` prices an optimised cancellation as having *no tolerance to a missing term*. Measured
here, on the 22 flat cells (`fragility[*]`, one `SEARCHED` and one transferred row each):

| | searched | the best transferred rule |
|---|---|---|
| worst **single**-path removal, zero defects | **`≤ 0.0917307843`** — `0` of 22 lose the verdict | — |
| amplification against its own zero-defect reading | **`1.04874727`–`1.74714585×`** | `1.26640348`–`2.85699419×` |
| cells where the searched design amplifies **less** than the transferred one | **18 of 22** | — |

**`F11` fired, and it fired in the favourable direction**: not one flat cell loses `T-5b` to one
missing path. The searched design is **less** amplifying than the rule it replaces at 18 of 22
cells — which is the opposite of what a cancellation argument predicts, and it is a measurement
rather than an argument.

**Quantised onto `C-0060`'s own two levels** — `quantiseToLevels`, the optimal total-preserving
projection, graded on the **same 4 000-realisation stream**:

| | |
|---|---|
| flat cells still flat after the projection | **`10` of 22** |
| their two-level ratios | **`2.39134682`–`10.0830497`**, **20 of 22** inside `[3.5, 20]` |
| of the 10, how many are inside `[3.5, 20]` | **9** |
| of those 9, how many satisfy `R₂ ≤ n/10` and are therefore inside `C-0023`'s allowable too | **2** — `f = 0.30` abstract grid on the rooting helices at `R₂ = 4.96460798`, `p90` `0.0819984662`; and `f = 0.30` determined station lattice on the rooting helices at `R₂ = 4.99455139`, `p90` `0.0993653869`, both at 50 paths |

*(counts over this study's own `fragility[*]` records; the peak stiffness of a two-level design is
**not emitted**, so the third threshold is read there through §3's `peak ≤ R·S/n` bound and not by
measurement)*

**And `C-0060`'s window is measured on exactly this object** — a two-level design — which is the
other half of `CH-0273`: the axis is right, and the object it was read on in §3 was the 50-valued
searched vector rather than the two-level one. **The projection is a PRICE, never a design**:
`CLAUDE.md` records the projected-against-searched gap at **24.9 %**, so these ten readings are a
**lower bound** on what a search *within* the two-level family would reach, and that search has not
been run.

## 8. The tightest cell over `C-0208`'s five radial rungs — `F13` did not fire

| radial | through-thickness link | best transferred | **searched** | `R` | flat |
|---|---|---|---|---|---|
| `254.808095` (the control) | `254.808095` | `0.110597484` | **`0.0699392016`** | `104.076024` | **yes** |
| `548.995464` | `475.448622` | `0.110198041` | **`0.066782473`** | `37.1418767` | **yes** |
| `754.005141` (the bracket floor) | `629.20588` | `0.110036326` | **`0.0647024141`** | `191.010656` | **yes** |
| `1530.48954` | `1211.56918` | `0.110034471` | **`0.0606641028`** | `41.5932277` | **yes** |
| `1735.49922` (the bracket ceiling) | `1365.32644` | `0.110059628` | **`0.0705668742`** | `75.2352448` | **yes** |

**The verdict is a property of the question and not of one rung.** Over the five rungs the searched
reading spans `0.0606641028`–`0.0705668742` and the transferred one `0.110034471`–`0.110597484` —
ratios of **`1.16323939`** and **`1.0051167`**, each a quotient of two numbers of this study's own
`rungs[*]` records — and the flat verdict moves at neither. The **ratio the argmin demands**, by
contrast, is not determined across the rungs at all (`37.1418767`–`191.010656`, a factor of
**`5.14273033`** on the same construction), which is
`C-0135`'s *a descent's VALUE and its POINT are determined to different precisions* reproduced on a
new axis — and the design reads the point.

## 9. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a per-path stiffness in pN/nm summing to a mandate in pN/nm; a ratio dimensionless as max over min; a dishing dimensionless as a fraction of the free stroke of the **same** lattice; a percentile as the 90th order statistic of that ratio, asserted as such rather than as a mean | **PASS** |
| **2 — limiting cases** | a fully present ensemble makes the percentile the **nominal** dishing; an equal distribution has ratio **exactly one**; the ratio refuses an empty or non-positive vector; the percentile objective refuses a non-positive free stroke; the search refuses an empty comparand set, a start of the wrong length, a zero-sweep descent and a non-positive mandate | **PASS**, 19 named tests |
| **3 — symmetry and the standing falsifiers** | a uniform pressure on the free tethered lattice **at the resolved link** dishes exactly `0.0` of the stroke (`F6`); the default lattice bit-identical to the standing object on `assembleLoad` over **4 320** degrees of freedom and on the **435**-bond crossover site set (`F7`); a searched distribution meets `C-0017`'s mandate on the **sum** exactly; the prestrain moves the **free field** and not the smoothed bank's compliance, which is `C-0104`'s trap asserted rather than assumed | **PASS** |
| **4 — numerical convergence** | **seven** axes, all at the deciding cell: the search's own sample grid `41` against `81` (**`0.077`**), the verdict's grid `81` against `41` and `161` (`0.0019`, `0.0042`), the **training** ensemble `120` against `60` and `240` (**`0.11`**, `0.057`), beam subdivisions `1` against `2` (`0.029`), and the descent's sweeps `2` against `3` (`0.023`). **`0 of 7` move the verdict**, and the worst reading any axis produces, `0.0718612653`, is still inside `T-5b` | **PASS** |
| **5 — literature and upstream** | **64 reproductions**, worst departure **`4.0E-9`** — `C-0208`'s own published `p90` at every one of the 32 cells on **both** transferred distributions, read out of `gpd/results/T-310-a-bond-link-is-two-mechanisms.json`. The one-state `MultiStateSurrogate` and the `InfluenceSurrogate` agree about a peak dishing at `0.0` (`F9`), and the surrogate at full presence reproduces the **assembled** solve with its own Woodbury support forces at `0.0` (`F8`) | **PASS** |

### The fourteen declared falsifiers

| # | falsifier | declared | fired |
|---|---|---|---|
| `F1` | a searched distribution puts at least one coupled cell inside `T-5b` at the 90th percentile | **OPEN** | **FIRED** — `22 of 32`, tightest `0.0647024141` |
| `F2` | a cell that clears does so at a ratio outside `C-0060`'s `[3.5, 20]` | **OPEN** | **FIRED** — 16 of 22; **and its ground is challenged**, `CH-0273` |
| `F3` | the searched **out-of-sample** `p90` is worse than the best transferred one at any cell | **OPEN** | no — `0 of 32`, worst `0.905510711×` |
| `F4` | the searched **in-sample** objective is worse than the best of its own starts | closed | no — a property of the composition |
| `F5` | the searched `p90` falls **below** the oracle floor | closed | no — `0` violations of a pointwise theorem over 32 cells |
| `F6` | a uniform pressure on the free lattice at the resolved link does not dish exactly zero | closed | no — **`0.0`** |
| `F7` | the default lattice is not bit-identical to the standing object, or its site set differs | closed | no — 4 320 degrees of freedom, 435 bond sites |
| `F8` | the surrogate at full presence does not reproduce the **assembled** solve | closed | no — departure `0.0`, taken on the **searched** distribution |
| `F9` | the two surrogates disagree about one distribution's peak dishing by `> 1e−10` | closed | no — departure `0.0` |
| `F10` | the transferred rules do not reproduce `C-0208`'s published `p90` at every cell | closed | no — worst of 64 is **`4.0E-9`** |
| `F11` | a cell that clears still clears with its worst **single** path removed | **OPEN** | **FIRED** — `0 of 22` lose the verdict; worst removal `0.0917307843` |
| `F12` | two independent runs do not produce a byte-identical result file | closed | no — **discharged by a second emission in a separate snapshot, diffed against the artifact** (the run cannot assert this about itself; every search decision is taken through `searchDecision` at six significant digits and no field of the file counts a step or a second) |
| `F13` | the verdict at the tightest cell moves across the five radial rungs | **OPEN** | no — flat at all five |
| `F14` | a cell that clears does so with a single-path stiffness above `C-0023`'s `3.33333333 pN/nm` | **OPEN** | **FIRED** — 19 of 22; the tightest cell's peak is **`5.22203964`** against a uniform share of `0.666666667` |

## 10. The mutation test, and its three survivors were three real gaps in one place

`C-0161`'s standard on a Kotlin subject: **sixteen mutations, every one of which must fail a NAMED
test**, with the unmutated copy run first and its failures subtracted (`CH-0237`),
`find src -name '<file>.kt'` asserted to return **exactly one** path (`C-0190`), every anchor
asserted to occur exactly once (`C-0185`), and the `-x` flags **derived** from
`build.gradle.kts`'s own `dependsOn` block (`C-0194`).

**First run: 15 mutations, 12 killed, 3 SURVIVED** — `M13` (a start of the wrong length reaches the
descent), `M14` (a zero-sweep descent is admitted, which is a search that never searches) and `M15`
(`C-0017`'s mandate admitted as non-positive). All three are **guards on the descent's own
arguments**, and all three failed nothing for the same reason: no test constructed a malformed
call. The repair is the **fixture** — one named test, *"gate 2 — the search refuses an empty
transferred set and a mismatched start"* — plus a sixteenth mutation `M16` (a search with **no**
comparand is admitted, which removes the composition's own in-sample guarantee, i.e. `F4`).

**After them: 16 mutations, `0` survivors over a subtracted baseline of `0`.**

## 11. `P1`–`P6`, discharged by name

| # | target | where |
|---|---|---|
| `P1` | the oracle `p90` floor at every cell, with what it can and cannot decide | §1 — `5.14335957E-4`–`0.00182090471`, excluding `0 of 32`, the transferred distribution `76.9529259`–`209.690778×` above it |
| `P2` | a distribution searched at all 32 cells of the bracket-floor rung, graded out of sample | §2 — `22 of 32` flat, tightest `0.0647024141`, on the `197197` stream the search never sees |
| `P3` | the in-sample / out-of-sample gap at every cell, emitted | §6 — `1.24035436`–`1.77724701×` in sample against `1.10434917`–`1.70065256×` out, per cell in `cells[*]` |
| `P4` | the max/min ratio the argmin demands, against `C-0060`'s window | §3 and §4 — `6.87455826`–`191.010656`, `12 of 32` inside; **and the window is re-read** (`CH-0273`) |
| `P5` | the fragility of any cell that clears, and its two-level `p90` | §7 — `0 of 22` lose the verdict to one missing path; `10 of 22` survive the projection |
| `P6` | the tightest cell at **all five** radial rungs | §8 — flat at all five, `0.0606641028`–`0.0705668742` |

## 12. The pre-registration is thirteen by DIFF and fourteen by NARRATIVE, and the distinction is the point

`F1`–`F13` are committed at **`646b29e`**, one commit before the run, which is exactly `C-0092`'s
discipline: *commit the pre-registration one commit BEFORE the result, so the criterion is a diff
and not an assertion.*

**`F14` is not.** It was added after the plumbing pass and before the run — the study source knew
it, and the run's **own output** carries its statement, so the declaration certainly predates the
emission — but the task-file hunk declaring it is uncommitted and lands in the **same** commit as
the result. So `F14` is a pre-registration by narrative, and the corpus should read it as one
notch weaker than the other thirteen. It is recorded here rather than glossed, and the remedy is
one commit: land the `F14` hunk of the task file **before** the commit that carries the result.

## 13. One declared parameter moved between the plan and the run

`T-316`'s *Units and conventions* section declares a **200**-realisation training ensemble; the run
used **120** (`parameters/trainingRealisations`). The declaration is retained rather than struck,
and the departure is priced by the study's **own** convergence axis, which brackets 120 with **60**
and **240**: departures `0.11` and `0.057` on the searched `p90`, with the verdict moving at
neither and the worst reading any of the seven axes produces, `0.0718612653`, still inside `T-5b`.
So 200 lies inside a bracketed range whose two ends are both measured; what is not available is a
reading **at** 200. Nothing here rests on the training count — the census is read on the disjoint
`197197` stream at every cell.

## 14. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **`C-0208`'s `0 of 64` is not withdrawn.** It is exact on the two transferred distributions and
  all 64 of its readings are reproduced here at `4.0E-9`. What is withdrawn is the *inference* that
  no coupled cell of this lattice can be flat.
- **The search is over the STIFFNESS VECTOR ALONE.** The placement, the station set, the
  cross-section, the raster, the load case, the link resolution and the radial bracket are all
  `C-0208`'s and none of them moves. `C-0063` records that **which stations a coupling enters at is
  worth more than how its stiffness is distributed**, and the two have never been searched jointly
  on this lattice.
- **A descent reports the best point it FOUND.** The oracle floor says every cell still has two
  orders of magnitude of room, and the sweep budget is emitted as a convergence axis
  (`2` against `3` sweeps, `0.023`) so a reader can see how much the search had left.
- **The COUNT is out of sample and the TIGHTEST CELL is an order statistic over 32 of them.** Quote
  the count as the result and the tightest cell as an identification.
- **The census is on ROUTE A**, whose raster turns carry **zero** unpaired nucleotides
  (`C-0175`'s modelling choice). `C-0193` and `C-0200` establish that the only folded block of this
  cross-section does otherwise, so the whole census is a statement about a design nobody has
  folded. What a searched distribution does on **route B** is not answered (`T-315`, `C-0211`).
- **The transverse constant is pinned at `C-0205`'s ceiling throughout**, its generous reading;
  `C-0208` §1a records that the measured pair term would lower it by `1.09182329×`.
- **The radial constant is unsourceable** (`C-0208` §6) and is carried as a bracket; the answer is
  stated at all five of its rungs.
- **`CH-0242`'s common-mode spring is absent**, so every bond and every tie here is still missing
  the stiffer of the two springs, at every rung.
- **Buildability is NOT established at any cell.** What §4 establishes is that one of the two flags
  the study read it on is a flatness window from another lattice. The **placement** of a 50-path
  distribution spanning a factor of 191 in stiffness — `C-0060`'s own *"what fails is the array"* —
  is unpriced here, and the two-level projection's own peak per-path stiffness is not emitted.
- Nothing here re-opens the span census, the raster, the cross-section, the chain model or
  `C-0017`'s mandate.

## 15. Still open — named, not answered

- **The placement and the distribution searched TOGETHER.** `C-0063` says the placement is worth
  more, and no study on this lattice has moved both.
- **A smoothed CVaR of a log-sum-exp** — convex in the sampled field, an upper bound on the
  percentile, differentiable throughout, and needing one triangular solve per realisation for its
  adjoint. Affordable, and a study of its own.
- **The best design inside `C-0060`'s two-level family**, searched rather than projected into. The
  ten projected readings in §7 are a lower bound on it and `CLAUDE.md` prices the gap at 24.9 %.
- **What a searched distribution does on ROUTE B**, whose turns carry 28 unpaired nucleotides.
- **Whether the shared-body topology** — `C-0017`'s mandate spent once in a rigid-body mode rather
  than at every station — moves this census the way it moved the square-lattice one. That is a
  change of **topology**, not of distribution.
- **What it costs to PLACE a searched distribution**, which is the buildability question §4 says
  nobody has asked on this lattice.
