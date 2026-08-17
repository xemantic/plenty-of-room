# C-0087 — Under the measured staple dropout **every flat Gen-1 design stops being flat**, and the position dependence is not what does it: **one** missing staple takes `C-0063`'s optimised 34 roots from **0.0706** to **0.5010** of the stroke, so the flatness verdict is lost in **89.6 – 100 %** of realisations at *every* one of five incorporation conventions, *every* one of three mandate readings and *all four* standing placements — **not one of the sixty cells is flat at the median, let alone at the 90th percentile.** `CH-0084`'s indicative half is settled and it is settled the hard way. **And the dropout REVERSES the ranking of the two flat designs** — `C-0063`'s equal springs beat `C-0058`'s two-level grid at zero dropout and lose to it under fabrication. Along the way the per-staple map itself was read: **`CH-0084`'s 48 % is one corner of 168** and the perimeter *mean* is **77.5 %**, which changes one published row and no verdict

| | |
|---|---|
| **Task** | [`T-148`](../tasks/T-148.md), raised by [`CH-0084`](../challenges/CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md) (out of [`C-0072`](C-0072-plan-tolerance-model.md)) and named in [`C-0080`](C-0080-third-answers-synthesis.md) |
| **Leaf** | **`A1.2`** (the anchoring scheme the coupling belongs to), with **`A8.2`** for the flatness of the tile it holds |
| **Verification type** | **logical** (the mandate is an equality on a **sum**, so the mean dropout settles that half in one line; and a *single*-path removal settles the flatness half in `n` solves) **+ in-silico** (`C-0058`/`C-0060`/`C-0063`/`C-0068`/`C-0074`'s own exact Woodbury surrogate on `C-0009`'s grillage under `C-0022`'s **solved** load, re-run under a seeded Bernoulli dropout by **Monte Carlo**, 10 000 realisations per cell) **+ literature** (Strauss et al. 2018 **read directly**, including the 168-value per-staple map of its Supplementary Fig. 14) |
| **Verdict** | **PASS on the predicate, and the answer is NEGATIVE and UNANIMOUS.** Under the measured staple dropout **no standing Gen-1 design is flat**: over the twenty `placement × convention` cells read **as built** the *lowest* exceedance of `T-5b`'s 0.10 anywhere is **90.6 %** of realisations and seventeen of the twenty exceed it in more than 98 %; over all **sixty** cells — the three mandate readings included — the lowest is **89.6 %**, the lowest 90th percentile anywhere is **0.3954**, and **none of the sixty is flat even at the median**. **The cheap bound is what explains it, and it needed no Monte Carlo at all**: removing **exactly one** of `C-0063`'s 34 paths — 34 solves — takes its dishing from **0.0706** to **0.5010** of the stroke at the worst station, and one of `C-0058`'s 45 takes **0.0753** to **0.3060**. An exhaustively optimised placement is a delicately tuned cancellation, and *any* missing path destroys it; the mean station dropout is 8–35 % and the mean survivor count 20.0–41.3. **`CH-0084`'s flatness half is therefore settled in the direction it feared, but not by the mechanism it named**: the position dependence is worth **1.2 – 1.7×** on the 90th percentile, real and adverse, and the verdict is already lost at `CH-0084`'s own *position-independent* 0.84. **The declared falsifier `F1` did not fire** — all four position-dependent conventions agree. **What does change the design conversation is a reversal**: at zero dropout `C-0063`'s 34 **equal** springs (0.0706) beat `C-0058`'s two-level 45 (0.0753), and under the measured dropout the order inverts to **0.6391 against 0.4893** at the 90th percentile and **99.8 % against 97.0 %** exceedance — because the denser, more regular 45-path array loses less per missing path (0.3060 against 0.5010). **The equal-spring advantage `C-0063` and `CH-0080` read as a property of the placement is, under fabrication, a liability.** **Neither mandate reading repairs it**: holding `C-0017`'s total on the survivors is *worse* at the 90th percentile (0.6719) and pre-stiffening every path by its own inverse incorporation is better at the median (0.4392 against 0.4482) and **still worse** at the 90th (0.6454) — the dropout costs the **shape** of the coupling, not only its level. **The mandate half needs no pipeline and is carried forward**: `E[K] = Σ kᵢpᵢ` is a **16.0 %** shortfall at the measured mean and **19.4 – 21.7 %** under the measured position-dependent field, against `C-0060`'s worst rounding error of 5.44 %. Raises [`CH-0102`](../challenges/CH-0102-the-forty-eight-per-cent-is-one-corner-of-one-hundred-and-sixty-eight.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED.** The **input** is unusually strong for this repository — Strauss et al. (2018) is a measurement, read directly, including its per-staple map — but it is a measurement of *staple* incorporation on a *plain Rothemund rectangle* at one folding protocol, and the out-of-plane motif every placement here stands on is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |
| **Provenance** | `gpd/results/T-148-staple-dropout.json`, produced by `coupling.StapleDropoutStudyKt`; model in `src/main/kotlin/coupling/StapleDropout.kt`, with one additive method (`InfluenceSurrogate.solveWithDropout`) in `src/main/kotlin/coupling/NonUniformCoupling.kt`; the literature transcription, its three validation checks and its URLs in [`gpd/data/T-148-strauss-incorporation-map.md`](../data/T-148-strauss-incorporation-map.md); **9 cheap bounds, 5 incorporation fields, 20 station-field records, 60 Monte Carlo cells at 10 000 realisations each, 4 convergence axes, 17 upstream reproductions, 6 predicates**; **37 gate-named tests in `src/test/kotlin/coupling/StapleDropoutTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 17 m 6 s** — the whole suite on its own isolated tree, with a concurrent agent's mid-TDD `src/main/kotlin/stability/RecommendedElementFoldStudy.kt` dropped by `--drop-file`; the result file **re-run through `tools/study.sh` twice and diffed**: three independent runs agree on every one of 2 037 lines and differed only on a `runtimeSeconds` wall clock, which has been **removed** (`CLAUDE.md`: *emit nothing that counts steps*) — the emitted file therefore carries no non-reproducible field, and the removal touched only the study's own `main`, which compiled and ran clean afterwards |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** sheet, 15 duplexes at the SAXS-measured **2.69 nm**, each placement on **its own** crossover host (`C-0058`/`C-0060`: 8 symmetrically centred columns; `C-0063`: phase 24; `C-0074`: phase 8); `C-0022`'s **solved** edge profiles at **2 mM, 10 nm, 0.192 V** (design state) and **2 mM, 7 nm, 0.192 V** (the held end of `C-0068`'s range); `C-0017`'s **33.3333 pN/nm** as a **SUM** at §3's **acceptable 3 nm**; free-tile stroke **4.90731 nm**; dishing on an **81 × 81** grid; flat means below **`T-5b`'s 0.10 CONVENTION**; **seed 20260817, 10 000 realisations per cell**, decisions at 6 significant digits and emission at 9 |
| **Consumes** | [`CH-0084`](../challenges/CH-0084-the-measured-staple-incorporation-is-past-the-flatness-threshold.md)/[`C-0072`](C-0072-plan-tolerance-model.md) (the question, and the Bernoulli composition **re-derived** rather than cited), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`, `rimStiffenedWeights`, `normalisedStiffnesses`, `rimMask` — the two levels **re-derived from its own rim × 5 rule**, its 0.2182 and 0.0753 reproduced), [`C-0060`](C-0060-buildable-stiffness-ratio.md) (the 34.6 %/31.6 % thresholds, **CITED**, and its as-built/mandate-held discipline), [`C-0063`](C-0063-upward-root-placement.md) (**the placement itself**, read from `gpd/results/T-125-*.json`; its 0.0706 reproduced), [`C-0068`](C-0068-range-robust-placement.md) (the two-state range; its 0.0789 reproduced), [`C-0074`](C-0074-two-per-row-placement.md) (**its recommended placement**, read from `gpd/results/T-136-*.json`, and its 30-parameter minimax **re-run**), [`C-0064`](C-0064-robust-distribution.md) (`MultiStateSurrogate`, `minimaxStiffnessDistribution`), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, keyed on concentration, gap **and bias**), [`C-0026`](C-0026-one-row-per-duplex.md) (`ScatterPattern`, the free-tile stroke, the 17 % break-even — **CITED**), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0049`](C-0049-compliance-ceiling-stroke.md), [`C-0009`](C-0009-discrete-lattice-tile.md)/[`C-0015`](C-0015-crossover-phase-and-registration.md)/`Gen1Tile` |
| **Raises** | [`CH-0102`](../challenges/CH-0102-the-forty-eight-per-cent-is-one-corner-of-one-hundred-and-sixty-eight.md), against `C-0072`/`CH-0084`'s **edge** reading of the measurement |

---

## The claim, in one line

**A flat Gen-1 tile is a cancellation between thirty-odd attachments, and the only measured statement anybody has about whether those attachments form says that about one in six of them will not — so the question `CH-0084` framed as *how strongly is the dropout correlated with the rim* turns out to be the second-order one: a single absent path already costs seven times the whole tolerance, and after that it hardly matters where the absences are.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward**;
  `w` is positive **downward**; the origin is the tile centre.
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit **plane**,
  on the same **81 × 81** grid as every flatness claim upstream. **Flat** means below **10 %** of
  the free-tile stroke — `T-5b`'s **convention**, not a physical threshold.
- **A dropout is a REMOVAL, not a perturbation.** A path whose staple is absent has stiffness
  exactly zero and is solved as an **absent station**: the Woodbury system is assembled over the
  surviving stations alone, which is exact superposition and not a limit.
- **Realisations are INDEPENDENT across stations.** Strauss reports no correlation length; this is
  stated as the convention it is.
- **The verdict statistic is the 90th percentile** of the dishing distribution, with the median and
  the exceedance probability beside it. **That is a choice**: nothing upstream says what fraction
  of built tiles a design is allowed to lose, and it is named as open.
- **An operating state** is a `(concentration, gap, bias)` triple of `C-0022`'s solved profiles; an
  **operating range** is the set of states one device traverses (`CH-0077`, `C-0068`).
- **The random stream is a SplitMix64 seeded at 20260817** and the seed and sample count travel in
  the result file. Percentiles are **nearest-rank order statistics**, so no interpolation can move
  a reported value off the sample.

---

## The two cheap bounds, which ran first — and the second one settles the question

### Bound 1 — the mandate, in one line and with no spatial model

`C-0017`'s mandate is an equality on a **SUM** (`C-0058`, `C-0060`), so the dropout's cost to it is
`E[K] = Σ kᵢ pᵢ` in closed form, with `Var(K) = Σ kᵢ² pᵢ(1 − pᵢ)`.

| reading | `E[K]` [pN/nm] | shortfall | σ(K) [pN/nm] |
|---|---|---|---|
| `CH-0084`'s position-independent 0.84 | **28.00** | **16.0 %** | **1.822** |
| the fitted flat band, on the 3 × 15 grid's own stations | — | 17.5 % | — |
| the fitted exponential, same stations | — | 19.9 % | — |
| the **measured** depth field, same stations | — | **19.4 %** | — |
| the measured depth field, on `C-0063`'s 34 roots | — | **21.7 %** | — |

**Against `C-0060`'s worst level-rounding error of 5.44 %** — which that claim calls *"a placement
error, not a rounding nuisance"* and spends a one-base-pair trim to take to `1.3e−4` — this is
**2.9–4.0×** larger and **a trim cannot touch it**, because the missing paths are not there to trim.
`CH-0084`'s statement 2 is reproduced exactly and is carried forward unchanged.

### Bound 2 — ONE missing staple, in `n` solves, and it is the whole answer

| placement | at zero dropout | with **exactly one** path absent, worst station | ratio |
|---|---|---|---|
| **`C-0063`'s 34 upward roots, equal springs** | **0.0706** | **0.5010** | **7.1×** |
| **`C-0058`'s 45-path two-level rim × 5** | **0.0753** | **0.3060** | **4.1×** |

> **Both are past `T-5b`'s 0.10 by 3–5×, and neither needed a Monte Carlo.** This is the finding:
> a flatness optimum found by an exhaustive placement sweep (`C-0063`: 1 144 858 placements) or by
> a 45-parameter minimax (`C-0058`) is a **cancellation**, and a cancellation has no tolerance to a
> term going missing. Everything below is that fact seen through a distribution.
>
> The mechanism is already in `CLAUDE.md`: *"an attachment coupling can be a NET DISHING SOURCE,
> and the sign flips at an attachment pitch of one Winkler bending length"* — `ℓ = 12.83 nm` along
> the helices. A dropout **is** an increase in the attachment pitch, at random places.

### The bound that did NOT settle it, and is reported for that reason

`InfluenceSurrogate.reachableDishingFloor` is the least-squares-optimal dishing over **all** force
vectors at a station set, so — since removing stations can only raise it — the value over the
**full** set is a rigorous lower bound on **every** realisation. Over all four station sets it is
**0.00548** of the stroke, 18× below `T-5b`'s convention. **Falsifier `F4` did not fire**: the cheap
bound could not have decided the question the other way, so the Monte Carlo was necessary.

---

## Deliverable 1 — the incorporation field, fitted to the measurement rather than assumed

Strauss et al. (2018) is **read directly**, and so — new here — is the per-staple map of its
Supplementary Fig. 14. The transcription, its three independent validation checks and its URLs are
in [`gpd/data/T-148-strauss-incorporation-map.md`](../data/T-148-strauss-incorporation-map.md).
**Five conventions are carried, not one**, because the mapping of a 87 × 65 nm rectangle's
measurement onto a 40 nm tile is a convention and must be quoted as one:

| convention | what it is | free parameter | fitted to | reference mean | **Gen-1 tile area mean** |
|---|---|---|---|---|---|
| `UNIFORM` | `p ≡ 0.84` everywhere — **`CH-0084`'s own reading** | none | — | 0.8400 | 0.8400 |
| `FLAT_BAND` | 0.48 inside a **4.619 nm** band of the rim, 0.95 inside it | band width | the reference **area** mean = 0.84 | 0.8400 | **0.7587** |
| `EXPONENTIAL` | 0.95 relaxed toward 0.48 over a **4.996 nm** decay length | decay length | the same | 0.8400 | **0.7733** |
| `LATTICE_RING` | the stated mechanism at **one** lattice cell | **none** | nothing — a **check** | **0.9010** | 0.8547 |
| **`MEASURED_DEPTH`** | **the 168 measured values**, by along- and across-helix depth in nm | **none** | nothing — **no fit** | 0.8350 | **0.7904** |

**Three results, and the first two are about the measurement rather than about the tile.**

1. **The measured boundary layer is WIDER than one lattice cell, and the measurement says so
   itself.** The mechanism Strauss names — *"staples at the edges and corners are missing
   neighboring helices and/or lack stacking interactions"* — read literally at one lattice cell
   predicts a reference mean of **0.9010** against the measured **0.8400**. The `LATTICE_RING`
   convention is therefore **rejected by the measured mean**, and it is carried only because a
   convention that fails a check is worth more than one that was never checked.
2. **A 40 nm tile is not a Rothemund rectangle, and the transfer is PESSIMISTIC.** The same fitted
   boundary layer over the Gen-1 tile's own area gives **0.759–0.773** against the measured 0.840,
   because a 40 × 40.35 nm tile has **1.85×** the perimeter per unit area of the reference. So
   **`CH-0084`'s 0.84 is the optimistic reading of its own measurement on this object**, and the
   measured-depth transfer agrees: **0.790**.
3. **The 48 % is one corner of 168.** Read off the map instead of the abstract, the **perimeter
   mean** incorporation is **0.775** and the interior mean **0.862**; the single worst cell (0.479)
   is a corner and the best corner is **0.871**, *above* the interior mean. That is
   [`CH-0102`](../challenges/CH-0102-the-forty-eight-per-cent-is-one-corner-of-one-hundred-and-sixty-eight.md),
   it moves one published row in the **favourable** direction, and it changes no verdict — because
   `C-0060`'s 34.6 % threshold needs an incorporation of **89.31 %** and **only 30 of the 168
   measured positions** reach it, while `C-0026`'s 17 % break-even needs **97.19 %** and **none of
   the 168** does — arithmetic on `StrausIncorporationMap`, asserted as a gate-5 test.

---

## Deliverable 2 — the distribution, on the four placements that carry a standing verdict

10 000 seeded realisations per cell, **as built** (the surviving paths keep their design stiffness
and the total falls — `C-0060`'s own honest convention). `p90` is the 90th percentile of
peak dishing over the free-tile stroke at the design state; `exceedance` is the fraction of
realisations above `T-5b`'s 0.10, with its binomial standard error.

| placement | zero dropout | convention | median | **p90** | **exceedance** | mean survivors |
|---|---|---|---|---|---|---|
| `C-0017`'s 45 equal springs, 3 × 15 | 0.2182 | `UNIFORM` | 0.3319 | 0.5259 | **100.0 %** | 37.8 |
| the same | 0.2182 | **`MEASURED_DEPTH`** | 0.4154 | **0.6142** | **100.0 %** | 36.3 |
| **`C-0058`'s two-level rim × 5, 3 × 15** | **0.0753** | `UNIFORM` | 0.2152 | 0.3954 | **90.9 % ± 0.3** | 37.8 |
| the same | 0.0753 | `LATTICE_RING` | 0.3068 | 0.4159 | **90.6 % ± 0.3** | 41.3 |
| the same | 0.0753 | **`MEASURED_DEPTH`** | 0.2768 | **0.4893** | **97.0 % ± 0.2** | 36.3 |
| **`C-0063`'s 34 equal roots, phase 24** | **0.0706** | `UNIFORM` | 0.2832 | 0.5346 | **98.4 % ± 0.1** | 28.6 |
| the same | 0.0706 | **`MEASURED_DEPTH`** | 0.4482 | **0.6391** | **99.8 % ± 0.04** | 26.6 |
| **`C-0074`'s recommended 30 roots, minimax** | **0.0682** | `UNIFORM` | 0.2359 | 0.4867 | **98.9 % ± 0.1** | 25.2 |
| the same | 0.0682 | **`MEASURED_DEPTH`** | 0.3246 | **0.5733** | **99.8 % ± 0.04** | 23.3 |

- **The lowest exceedance in the whole twenty-cell as-built sweep is 90.6 %**, and it belongs to the
  convention the measured mean **rejects** on the design the measurement is **least** unkind to.
- **`MEASURED_DEPTH` is the mildest position-dependent reading** — it uses the perimeter's measured
  *mean* rather than its worst cell — **and it still fails at 97.0–100 %.** `CLAUDE.md`: *a negative
  result is strongest when the failing element is the one that passes.*
- **Every one of the twenty cells is also worse than no coupling at all** at the 90th percentile
  (`C-0058`'s and `C-0063`'s own uncoupled figure is ~0.308).

### The spatial structure is worth 1.2 – 1.7×, which is real and is not the story

At the same measured mean, `CH-0084`'s position-independent field puts `C-0063`'s 90th percentile at
**0.5346** where the measured depth field puts it at **0.6391** (**1.20×**) and the fitted flat band
higher still; on `C-0058`'s two-level design the same pair is **0.3954** against **0.4893**
(**1.24×**). **The direction is adverse, exactly as `CH-0084` predicted** — the position dependence
does make it worse — and it is a factor of 1.2–1.7 on a quantity that is already 4–9× past the
tolerance. `CH-0084`'s honest qualification is hereby quantified, and quantifying it does not
change the verdict.

---

## Deliverable 3 — the ranking of the two flat designs REVERSES

| | at zero dropout | under the measured dropout, p90 | exceedance | cost of ONE missing path |
|---|---|---|---|---|
| **`C-0063`'s 34 EQUAL springs** | **0.0706** — the better | 0.6391 — **the worse** | 99.8 % | **0.5010** |
| **`C-0058`'s 45-path two-level** | 0.0753 | **0.4893** — the better | **97.0 %** | **0.3060** |

**`C-0063`'s headline is that a placement is flat with *no distribution at all*, and `CH-0080` reads
the equal-spring advantage as belonging to the 10 nm layer. Under fabrication it belongs to
nobody**: what the dropout rewards is a **denser, more regular** array — 45 paths on a 3 × 15 grid
against 34 on an upward lattice — because the per-path loss is what the distribution of outcomes is
made of. **A design rule read off a zero-defect solve can invert under the only defect statistics
anybody has measured**, and that is a new instance of `CLAUDE.md`'s *"quote it with the state it is
read at"*, where the state is **the fabrication**.

---

## Deliverable 4 — neither mandate reading repairs it

On `C-0063`'s placement under the measured depth field:

| mandate reading | what it is | median | p90 | mean realised total [pN/nm] |
|---|---|---|---|---|
| **as built** | the survivors keep their design stiffness | 0.4482 | **0.6391** | 26.11 |
| **mandate held** | the survivors rescaled to `C-0017`'s 33.3333 — a **diagnostic** | 0.4626 | **0.6719** | 26.11 |
| **compensated** | every path pre-stiffened by `1/pᵢ`, so `E[K]` **is** the mandate | **0.4392** | 0.6454 | **33.3221** |

- **Rescaling the survivors makes it slightly WORSE**, which is the cleanest evidence that the
  dropout costs the coupling's **shape** and not merely its level: restoring the level on a
  mis-shaped support set amplifies the mis-shape.
- **Compensation restores the mandate exactly in expectation** (33.3221 against 33.3333) and moves
  the dishing by **2.0 % better at the median and 1.0 % worse at the 90th percentile** — i.e. it
  narrows nothing and repairs nothing. It is also not free: it stiffens hardest exactly where the
  measurement says the paths are least likely to form.
- **The per-path allowable is not what fails.** At the mean 26.6 survivors of 34, holding the
  mandate puts 1.253 pN/nm on a path, i.e. **3.76 pN** at §3's acceptable stroke against the 10 pN
  unzip allowable — still 2.7× clear. **Nothing here is a force problem.**

---

## The five verification gates

Executed as **37 gate-named tests** in `src/test/kotlin/coupling/StapleDropoutTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a probability is dimensionless and every field returns one inside `[0, 1]` at every point of the tile; the Bernoulli relative scatter `√(f/(1−f))` reproduces `CH-0084`'s 43.6 / 104.1 / 22.9 %; the detection-to-incorporation offset is **additive in percentage points** (41 + 7 = 48, 88 + 7 = 95, 77 + 7 = 84, all three exactly); the fitted band and decay length are positive lengths bounded by the reference geometry; fourteen classes of unphysical argument throw | **PASS** |
| **2 — limiting cases** | a realisation that keeps **every** path reproduces the standing full-coupling solve (to 1 ulp — see below); one that keeps **none** returns the free tile exactly and reports zero support forces; a dropout equals an independently assembled surrogate over the surviving stations alone (`1e−9`); a zero band and a vanishing decay give the constant centre field; a band wider than the half tile depresses every station; both fitted fields take the measured edge value **exactly** at the rim; presence at `p = 1` keeps every path and at `p = 0` none; the area mean of a constant field is the constant | **PASS** |
| **3 — symmetry and conservation** | **the uncoupled tile under a UNIFORM load dishes exactly zero** (falsifier `F3`, `< 1e−9`); the flat-band area fraction is the exact rectangle complement and the exponential area mean matches an independent 800 × 800 midpoint quadrature to `5e−4`; both fitted parameters reproduce the mean they were fitted to (`1e−9`); the expected total and its deviation are the closed forms and a 40 000-draw sample reproduces them inside four standard errors; the SplitMix64 stream is bit-reproducible from its seed and differs between seeds; every field is invariant under a point reflection; compensation makes `E[K]` exactly the nominal total; renormalising the survivors restores the mandate exactly | **PASS** |
| **4 — numerical convergence, and statistical power** | the 90th percentile and the exceedance probability at **1 250 / 2 500 / 5 000 / 10 000 / 20 000** realisations, with the **binomial standard error** quoted beside every probability in the result file; the **mean over 200 realisations** on the 41 / 81 / 161 dishing grid (a single realisation is not a convergence test — three nested grids share their nodes and agree to the last digit); the fitted band over the reference-geometry bracket; the order statistic asserted not to mutate its sample and the binomial error to fall as `1/√n` | **PASS** |
| **5 — literature and upstream cross-check** | **the map's own three checks** — the paper's printed 41 / 88 / 77, its own `+7` panel offset, and the `k/186` quantisation that identifies `n = 186` at a **3.5×** discrimination over `n = 185` or `187`; the 168-of-192 probed census and the 52/116 perimeter split; `C-0058`'s **0.2182** and **0.0753**, `C-0063`'s **0.0706**, `C-0068`'s **0.0789**, `C-0074`'s **0.2424** equal-spring and **0.0682** minimax, `C-0026`'s **4.90731 nm** free-tile stroke, `C-0017`'s **33.3333 pN/nm**, `C-0058`'s **34** rim stations and `CH-0084`'s three scatter figures. **Worst strict departure over seventeen reproductions: `6.3e−4`**, which is the four-significant-figure rounding of `C-0068`'s published 0.0789 | **PASS** |

> **One gate-2 assertion had to be loosened and the reason is in `CLAUDE.md`.** `solve` **is**
> `solveWithDropout` at full presence — the same arithmetic, by construction — and the two still
> land **one unit in the last place** apart, because the JIT recompiles the dishing reduction
> between the two invocations and changes its summation order. Asserted at `1e−14` relative with
> the reason recorded at the assertion.

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **`F1`** | **the fitted conventions disagree about the verdict**, which would mean the position dependence is not resolvable from what is published and the deliverable is a *statement* rather than a number | **NO** | all four position-dependent conventions agree on all four placements: not flat, at every percentile reported |
| **`F2`** | the zero-dropout limit fails to reproduce a standing figure | **NO** | worst strict departure `6.3e−4`, which is a published rounding |
| **`F3`** | the uncoupled tile dishes non-zero under a uniform load | **NO** | `< 1e−9`, wired as a test |
| **`F4`** | the reachable floor over the full station sets already exceeds 0.10, making the Monte Carlo unnecessary | **NO** | 0.00548, 18× below the convention — the Monte Carlo was necessary |

**Two results that were not anticipated.**

1. **That a single missing path decides it.** The task was formulated around the *correlation* of
   the dropout — `CLAUDE.md`'s own *"which way a tolerance is correlated matters more than how big
   it is"* — and the correlation turns out to be a 1.2–1.7× correction on a quantity that a
   **count** of one has already taken 4–7× past the tolerance. The cheap bound that settles it
   (`n` solves, no sampling) was added *after* the Monte Carlo made its shape obvious, which is the
   wrong order and is recorded as such.
2. **That the ranking of the two flat designs inverts.** Nothing upstream suggested that the
   equal-spring 34-root placement — flatter, cheaper in distribution, and the programme's
   recommended flat design — would be the *more* fragile of the two.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. The **input** is measured and read directly; nothing
  derived here is.
- **Strauss measures STAPLE incorporation on a plain Rothemund rectangle at one folding protocol.**
  A Gen-1 coupling path is a designed element (an arm on a crossover, a flexure) whose own
  incorporation nobody has measured. The claim is that 84 % is the **right order** and the **only
  measured** figure, not that it is the coupling's own — `CH-0084`'s qualification 2, carried.
- **The MAPPING onto a 40 nm tile is a CONVENTION** and is carried as five of them, with the
  reference geometry's staple and helix counts **read directly** and only its interhelical distance
  bracketed (2.69 / 2.73 / 3.00 nm).
- **Realisations are INDEPENDENT across stations.** A folding-run-to-folding-run common mode, or a
  correlation between neighbouring staples, would change the distribution's shape and is not
  modelled. Strauss's own instrument could measure it.
- **The 90th percentile is this claim's choice of verdict statistic.** No upstream clause says what
  fraction of built tiles a design may lose. The median and the full exceedance probability are
  reported beside it so that another choice can be read off the same file.
- **The dishing pipeline, the lattice, the hosts, the load and the free-tile stroke are
  `C-0058`'s, `C-0063`'s and `C-0074`'s unchanged**, and inherit their whole validity range:
  `C-0022`'s **unsourced rim charge**, `C-0001`'s single foundation secant, one crossover layout
  per placement, and the fact that each standing verdict was read at one state or one range.
- **`T-5b`'s 0.10 is a CONVENTION**, not a physical threshold. At a 5 % convention nothing here
  changes; at a 50 % one the two-level design's median (0.277) would pass and its 90th percentile
  (0.489) would not.
- **`C-0074`'s recommended placement is its largest-plan-ceiling one, not its flattest** — 0.2424
  with equal springs and 0.0682 under the minimax, against 0.1726 and 0.0648 for the flattest
  phase-8 placement. The recommendation trades flatness for plan margin, and the numbers here are
  the recommendation's own.
- **Single layer, static, 300 K, aqueous 2 mM MgCl₂.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| staple **detection** efficiency, min / max / mean | 41 / 88 / 77 % | **CITED, MEASURED**, Strauss et al. (2018), **read directly** |
| the detection-to-incorporation offset | +7 percentage points | **CITED, MEASURED**, same; the additivity is **re-derived** |
| the 168-value per-staple map | Suppl. Fig. 14 | **READ DIRECTLY, TRANSCRIBED FROM A FIGURE**, validated three ways |
| structures imaged | `n = 186` | **CITED**, Methods; **confirmed** by the map's own quantisation |
| the reference rectangle's helix and staple counts | 24 helices, 16 × 16 bp core | **CITED, READ DIRECTLY** (Rothemund 2006 Suppl. Note S3; Strauss Suppl. Tables 6/7) |
| Rothemund's pixel yield | 94 % | **CITED, MEASURED**, read directly |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| rise per base pair | 0.34 nm | **CITED, MEASURED** |
| `C-0060`'s flatness thresholds | 34.6 % / 31.6 % | **CITED** from `C-0060` |
| `C-0026`'s break-even | 17 % | **CITED** from `C-0026` |
| `C-0022`'s solved collars | 2 mM / 10 nm / 0.192 V and 2 mM / 7 nm / 0.192 V | **CITED**, read at run time from `gpd/results/T-3b-*.json` |
| `C-0063`'s and `C-0074`'s placements | 34 and 30 roots | **CITED**, read at run time from their own result files |
| `RIGID_PLATE_TOLERANCE` | 0.10 | **CITED CONVENTION** from `T-5b` |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |

Everything else — every field, fit, station statistic, realisation, percentile, exceedance,
convergence axis and reproduction — is **derived here in code**, with `C-0058`'s, `C-0063`'s,
`C-0068`'s and `C-0074`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether a placement exists that is flat UNDER the dropout.** This claim grades the standing
   designs; it does not search. The direction the reversal points is *denser and more regular*, and
   the objective would be a percentile rather than a value — a different optimisation from any run
   so far. This is the successor task and it is queued.
2. **Whether the coupling element's own incorporation is the staple's.** Everything here is a
   transfer from a plain rectangle. `CH-0084` asked for the measurement and so does this claim.
3. **Whether the dropout is correlated within a folding run.** Independence is a convention here.
   A common mode would compress the distribution and a neighbour correlation would widen it, and
   Strauss's own instrument can measure both.
4. **What fraction of built tiles a flatness verdict is owed over.** The 90th percentile is this
   claim's choice; §3 and `T-5b` say nothing about yield.
5. **Whether the per-path allowable survives compensation at a lower survivor count.** At the mean
   it does, 2.7× clear; the tail was not swept.
6. **Whether `C-0060`'s and `C-0026`'s thresholds should now be retired as instruments.** Both are
   amplitude thresholds on a *multiplicative* scatter, and the measurement is a *removal*. They
   graded it correctly, and this claim shows the grading was not the binding statement.

## Challenges

**Raises [`CH-0102`](../challenges/CH-0102-the-forty-eight-per-cent-is-one-corner-of-one-hundred-and-sixty-eight.md)**
against `C-0072`/`CH-0084`'s **edge** reading — the 48 % is the single worst of 168 cells and a
corner, the perimeter mean is 77.5 %, and the correction runs in the favourable direction and
changes no verdict.

**None stands against this claim.** The four ways it would fail:

1. **A per-site incorporation measurement on a coupling-bearing tile showing an incorporation above
   89 %.** That is the number `C-0060`'s threshold needs, and the whole verdict is graded against
   the transfer of a plain rectangle's map. It is the measurement `T-45` has waited four iterations
   for and it is routine.
2. **A placement that is flat under the dropout.** Open item 1; it would remove the negative
   without touching anything else here.
3. **A demonstration that the dropout is strongly correlated within a folding run.** A common mode
   that removes *whole* couplings rather than individual paths would leave the shape intact and
   only move the level, which the compensated reading already prices.
4. **A different verdict statistic.** At a median rather than a 90th percentile, `C-0058`'s
   two-level design under the mildest convention reads 0.2768 — still 2.8× past `T-5b`, so the
   verdict is not statistic-limited, but the *margin* quoted is.
