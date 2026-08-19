# C-0147 — **THE 28 nt TURN LOOP IS A CHOICE — 4.67× ITS OWN REACH BOUND — AND THE 4 bp RAGGED FACE IS ON THE WRONG AXIS TO THREATEN FLATNESS AT ALL.** Two cheap bounds, run before anything else, decided both tasks. `T-230`: a turn's two anchoring phosphates are at most `d + 2r_P = 4.35327572 nm` apart and **6** unpaired nucleotides reach that on `T-71`'s MEASURED phosphodiester step, against the built **28** — but the minimum is a **criterion**, not a number, and the built allowance is exactly what makes a worst-azimuth turn **sub-thermal (0.52–0.76 `k_BT`) and sub-piconewton (1.00–1.47 pN)**. M13 affords **8 nt** at a 112 bp row, so route B fits — and at 8 nt the turn carries **6.54–12.11 pN**, past the unzip allowable. `T-231`: the ragged faces are the tile's **RIM**, not its gap-facing surface, so the coefficient on §3's flatness is **exactly zero**; the residual channel, a 2-row rim modulation at **7.608 nm** against bending lengths of **17.23–23.21 nm**, is bounded at **5.5e−5** of the stroke against a **0.0275** threshold — **496×**. What the raggedness does cost is **plan budget**, and what it buys is a **published anti-stacking geometry**

| | |
|---|---|
| **Task** | [`T-230`](../tasks/T-230-honeycomb-turn-loop-slack.md) and [`T-231`](../tasks/T-231-ragged-face-cost.md), both raised by [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) *Still open* items 1 and 2 |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (a covalent **reach** bound on the measured backbone, an exact freely-jointed-chain force and free-energy law, and exact integer lattice arithmetic on the rise — all closed forms) **+ literature** (the caDNAno per-helix allotment and the blunt-end stacking range, both consumed from `gpd/data/`, already in the repository — **zero fetches**). **No solve, and for `T-231` that is a stated refusal rather than an omission** |
| **Verdict** | **PASS on all twelve predicates. Of the twelve declared falsifiers eleven did not fire and `F6` of `T-231` FIRED, favourably and as declared open** — the 112 / 108 recommendation is **conditional** on a path count, not absolute. Raises [`CH-0186`](../challenges/CH-0186-the-twenty-eight-nucleotide-turn-is-a-choice.md) against `CH-0173`'s 92 bp ceiling and [`CH-0187`](../challenges/CH-0187-the-two-length-recommendation-rests-on-an-unstated-filter.md) against `C-0140`'s selection rule |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The constants are: `T-71`'s backbone from 13 084 crystallographic linkages, the honeycomb's SAXS lattice constant, the caDNAno blocks' own published scaffold accounting, and the blunt-end stacking corpus — all measured by others on other objects and transferred |
| **Provenance** | [`gpd/results/T-230-honeycomb-turn-loop-slack.json`](../results/T-230-honeycomb-turn-loop-slack.json) (`structure.HoneycombTurnLoopStudyKt`, **new**) and [`gpd/results/T-231-ragged-face-cost.json`](../results/T-231-ragged-face-cost.json) (`structure.RaggedFaceCostStudyKt`, **new**); model in `src/main/kotlin/structure/HoneycombTurnLoop.kt` (**new file** — `HoneycombRasterTurnSense.kt` was **read, not edited**, and supplies every lattice residue used here); **25 gate-named tests** in `src/test/kotlin/structure/HoneycombTurnLoopTest.kt`, written before the model and **mutation-tested afterwards** (dropping the `+ 1` from the reach, swapping the two face parities and crippling the inverse Langevin each fail named tests, and the restored source passes 25 of 25). Both result files **BYTE-IDENTICAL across two independent JVM runs** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; rise **0.34 nm/bp**; honeycomb interhelical distance **2.536 nm** (`Gen1Tile.INTERHELICAL_HONEYCOMB`, SAXS), in-plane row pitch `3d/2 = 3.804 nm` and layer pitch `d√3/2 = 2.196 nm` (`C-0141`'s corrected cross-section); phosphate radius **0.9086378584708424 nm** (`T-71`, MEASURED); intrastrand P···P step **0.664481 ± 0.036163 nm** C2′-endo, **0.607234** C3′-endo (`T-71`); ssDNA Kuhn **2.10–2.84 nm** (zero-force scattering) with the **inextensible** contour **0.65–0.70 nm/nt** that travels with it; cross-sections **design (i) `15 × 4`** and **design (ii) `10 × 6`**, 60 helices each; composite fraction **0.30**, `Gen1Tile.FOUNDATION_SECANT` |
| **Consumes** | [`C-0140`](C-0140-honeycomb-raster-turn-sense.md)/[`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md) (the turn-sense machinery re-run as a library, and its 92 / 98 / 106 bp, its 4 / 8 bp faces and its 116 bp extent **read from its result file and reproduced at departure `0.0`**), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the cross-section pitches and the plan ceilings, **read from its result file**), [`C-0142`](C-0142-coupled-cells-at-the-honeycomb-cross-section.md) (the tightest coupled cell that is still flat, **read from its result file**), [`C-0079`](C-0079-unbonded-duplex-separation.md)/[`C-0085`](C-0085-collinear-stacking-clearance.md) (the blunt-end stacking range, through `gpd/data/T-139-blunt-end-stacking-literature.md`), [`C-0022`](C-0022-tile-edge-load-profile.md)/[`C-0110`](C-0110-device-b-tall-gap.md) (`transverseDecayRateBound`, the transverse eigenproblem), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (`loadRippleTransmission`), `Gen1Tile`, `MeasuredBackbone` |
| **Raises** | [`CH-0186`](../challenges/CH-0186-the-twenty-eight-nucleotide-turn-is-a-choice.md), [`CH-0187`](../challenges/CH-0187-the-two-length-recommendation-rests-on-an-unstated-filter.md) |

---

## The claim, in two lines

**`T-230`.** Six nucleotides reach; twenty-eight is what a *slack* turn costs; and M13 affords
eight — so route B is possible, strained, and decided by an unresolved polymer convention.

**`T-231`.** The relief is on the rim, not on the gap, so §3's flatness cannot read it; what it
costs is plan budget and what it buys is an anti-stacking geometry.

---

## `T-230` Deliverable 1 — the cheap bound, and it is one division

`n` unpaired nucleotides between two anchoring phosphates make **`n + 1`** phosphodiester steps.
So the greatest span the chain can reach is `(n + 1) × step`, and below `span/step − 1` the turn
closes **at no conformation whatever** — the same discipline `CLAUDE.md` records for the
`O3′–P–O5′–C5′` reach interval, one scale up, and it needs no polymer model at all.

| azimuth case | span [nm] | at C2′-endo 0.664481 | at C3′-endo 0.607234 | at C2′-endo P99 0.756745 |
|---|---|---|---|---|
| aligned — **this IS a scaffold crossover** | **0.718724283** | 1 | 1 | **0** |
| azimuth-averaged over both backbones | 2.70321445 | **4** | 4 | 3 |
| centre to centre | 2.536 | 3 | 4 | 3 |
| **worst — both backbones pointing away** | **4.35327572** | **6** | 7 | 5 |

> **The built blocks spend 28 nt per turn. The bound is 6. That is `4.66666667×`, so the
> allowance is a CHOICE.**

**And the `n = 0` row is the check that the geometry is being read right, on measured constants
alone.** A scaffold crossover is a turn with no unpaired nucleotides, so its span must be one
phosphodiester step — and `d − 2r_P = 0.718724283 nm` sits at **`+1.49997857 σ`** of the measured
C2′-endo step and inside its 99th percentile. Nothing was fitted: the honeycomb's SAXS lattice
constant and a 13 084-linkage crystallographic survey agree, unprompted, that a crossover closes
and that it closes **tightly** — which is *why* the `7k ± 5` residue condition binds it at all.
**`F1` did not fire.**

## `T-230` Deliverable 2 — but the minimum is a CRITERION, and the spread is a factor of seven

A turn loop held at a fraction `x = R/L_c` of its contour carries `f = (k_BT/b)L⁻¹(x)` and stores
`G = (k_BT L_c/b)[xu − ln(sinh u/u)]`. Read at the **worst** azimuth, which is the reading a
**free** row length is owed (a free width leaves both backbone azimuths free):

| criterion | ground | nt per turn | widest uniform row on M13 |
|---|---|---|---|
| **reach** | `T-71`'s measured step; below it, no conformation | **6** | **114 bp = 38.76 nm** |
| the 10 pN unzip allowable | `Gen1Tile.DUPLEX_UNZIP_ALLOWABLE` | 8–9 | 111–112 bp |
| **one `k_BT` of stored free energy** | the fold's own currency | **16–22** | 98–104 bp |
| one pN of turn tension | below every per-path allowable here | 29–41 | 79–91 bp |

**And the built 28 nt lands inside that bracket, where a slack turn is.** Held at 4.35327572 nm it
sits at **0.222–0.239** of its own contour, carries **1.00195245–1.46667915 pN** and stores
**0.518481856–0.7570064 `k_BT`** — sub-thermal, about one piconewton, **1.27–1.75×** the one-`k_BT`
bound and **0.68–0.97×** the one-pN bound. **`F2` did not fire** (28 is not required) and
**`F3` did not fire** (41/6 = 6.83, inside a decade, so the criteria bracket rather than
contradict).

## `T-230` Deliverable 3 — and that decides the route, because M13 affords exactly EIGHT

`60 × (112 + L) ≤ 7 249` gives **`L ≤ 8`**.

| | route A — all crossovers, two lengths | route B — uniform, loops |
|---|---|---|
| slack per turn | **0** | 8 nt, M13's whole affordance |
| row length | 112 / 108 bp, extent **116 bp = 39.44 nm** | uniform 112 bp = 38.08 nm |
| scaffold on M13 | 6 596 nt, **653 spare** | 7 200 nt, 49 spare |
| turn tension at the worst azimuth | — | **6.54349121–12.112167 pN** |
| turn free energy | — | 2.36–3.74 `k_BT`, **139–220 `k_BT`** over 59 turns |

**`F5` did not fire**: route B *does* reach 112 bp on M13, by two nucleotides. What it costs is the
whole difference — an 8 nt turn sits at **0.777–0.837 of its contour** and is at or past the 10 pN
unzip allowable at the tight end of the Kuhn bracket. **`C-0140`'s recommendation of route A
stands, on a better reason than it had**: not that route B does not fit, but that it fits only
strained. That is [`CH-0186`](../challenges/CH-0186-the-twenty-eight-nucleotide-turn-is-a-choice.md).

**And the budget is decided by an unresolved convention.** Over `CLAUDE.md`'s 2× method-systematic
ssDNA Kuhn bracket the one-`k_BT` turn asks **16 nt** at the loose end and **22** at the tight one
— a 1.4× spread with no measurement between them — against an affordance of 8. **p8064 removes the
question**, affording **22 nt**, which is inside the one-`k_BT` band at both ends of the bracket;
that is the cheapest thing anybody could do to route B, and `CH-0180` records that the paper's own
Methods put design (i) on p8064.

## `T-230` Deliverable 4 — the yield half CANNOT be priced, and the threshold is quoted instead

No published measurement relates a **scaffold turn-loop length** to origami folding yield. The
three nearest are on different axes and are named as such: Ke et al.'s is an **8 bp staple domain
between two crossovers**; Rothemund's 63 % → 11 % is a scaffold **linearisation**; Strauss et
al.'s 48–95 % is per-**staple** incorporation. The only measured point on the loop-length axis is
the built blocks themselves — 28 nt per turn, folded, gel-purified and imaged — and this claim
shows that point to be 4.67× the reach bound.

> **The threshold that decides the route is 8 nt.** At or below it a uniform 112 bp row fits M13;
> above it it does not. That is the number a folding experiment would have to bracket.

---

## `T-231` Deliverable 1 — the cheap bound is one QUESTION, and the answer is an AXIS

A four-layer honeycomb block's **gap-facing surface** is the outermost **layer**'s sidewalls — one
*column* of the cross-section, every helix of it lying in the tile plane. A **row length** changes
where a helix *ends*, which is a coordinate in that same plane, at right angles to the gap.

> **So the two ragged faces are the tile's RIM at `x = 0` and `x = L`, the coefficient of the
> raggedness on §3's normal-direction flatness field is EXACTLY ZERO, and `T-5b`'s dishing
> convention cannot read it at all.** **`F1` did not fire.**

It is `CLAUDE.md`'s own *"before substituting a measurement into an exclusion width, ask which
**axis** it is on"*, asked of a **specification** instead of a measurement.

## `T-231` Deliverable 2 — what it actually is: a COMB, and the same comb on both cross-sections

| cross-section | front | rear | extent | rim period | modulation `λ` | front-face levels | rear-face levels |
|---|---|---|---|---|---|---|---|
| **`15 × 4`** | **4 bp = 1.36 nm** | **8 bp = 2.72 nm** | 116 bp = 39.44 nm | **2 rows** | **7.608 nm** | 28 at `−4`, 30 at `0` | 14 at `−116`, 28 at `−112`, 16 at `−108` |
| **`10 × 6`** | **4 bp = 1.36 nm** | **8 bp = 2.72 nm** | 116 bp | **2 rows** | 7.608 nm | 29 at `−4`, 29 at `0` | 20 at `−116`, 18 at `−112`, 20 at `−108` |

`C-0140`'s 4 and 8 bp and its 116 bp extent reproduce at departure **`0.0`** (**`F2` did not
fire**), and the **identical** 4 / 8 appears on `10 × 6` — the raggedness is a property of the
honeycomb's turn-sense alternation, not of the block's shape. The front face's ends sit at exactly
**two** levels, so over the outermost 4 bp only about half the helices are present: the rim is a
comb and its cross-sectional area is halved over 1.36 nm.

**The two faces are not mirror images, and that asymmetry reconciles this reading with
[`C-0146`](C-0146-coupled-cells-at-the-two-length-raster.md)'s.** The front carries **two** levels
and the rear **three** — because a 108 bp helix is **recessed inside its own row's window** rather
than shortening it. So `C-0146`'s *"every x-raster row of the block is still 112 bp and consecutive
rows are staggered by 4 bp"* and this claim's *"every helix carries 112 or 108 bp and the faces are
ragged by 4 and 8"* are the **same object measured per row and per helix**, independently derived
in the same iteration and agreeing on the block extent, the stagger and the front spread. **`F5` did not fire** — the relief is
4 and 8 whole **rises**, above the design language's own quantum by construction, so everything
priced here is a design variable.

## `T-231` Deliverable 3 — the flatness cost, bounded at 496× the threshold that would move a verdict

| cross-section | `D_⊥` [pN·nm] | `ℓ_across` [nm] | `λ` [nm] | ripple transfer | × 50 free-edge penalty | rim lever | **bounded move** |
|---|---|---|---|---|---|---|---|
| `15 × 4` | 278.255762 | **17.2310927** | 7.608 | 2.43837603e−05 | 1.21918802e−03 | 0.0454728409 | **5.54399427e−05** |
| `10 × 6` | 916.230312 | **23.2114857** | 7.608 | 7.40538369e−06 | 3.70269185e−04 | 0.0454728409 | 1.68371917e−05 |

The gap-facing rim alternates with period **exactly 2 raster rows** — a square wave, entered
through its fundamental — against across-helix bending lengths **2.3–3.1×** longer than its whole
wavelength, so the plate cannot follow it (**`F3` did not fire**). `CLAUDE.md` records that this
transfer function **over**-attenuates a *rim* perturbation by 50× against a finite-plate solve, and
that penalty is applied, so the bound is conservative by construction.

> **`C-0142`'s tightest coupled cell that is still flat at the 90th percentile sits at
> `0.0973238201`, so it has `0.0274976866` — 2.75 % — of headroom, and the raggedness can consume
> at most `0.2016 %` of it. `F4` was DECLARED OPEN and did not fire, at a margin of 496×.**

**No coupled cell in `C-0142` or `C-0146` is at risk from the RAGGEDNESS** — the bound is
`1.68e−05` of the stroke on `10 × 6`, where the tightest surviving cell has `0.0275` of headroom.
What those cells *are* exposed to is the row geometry itself, which is `T-235`'s question and not
this one's; `C-0146` answers it, and its answer is that the row-faithful reading keeps every row at
112 bp and the movement is a **station** count and a **numerical guard**, not a width.

## `T-231` Deliverable 4 — the edge field cannot resolve it either, in closed form

A lateral feature of the rim enters the slit as a transverse mode decaying at `q₀` with
`q₀² ≥ κ² + (π/2h)²` (`C-0022`'s transverse eigenproblem, re-read at `C-0110`'s tall gaps). Over §3's three gaps and the three buffers `1/q₀` is
**2.4728–4.9455 nm**, i.e. **1.8–3.6×** the 1.36 nm relief:

| buffer | gap 5 nm | gap 7 nm | gap 10 nm |
|---|---|---|---|
| 0.5 mM | 0.4610 | 0.3509 | 0.2750 |
| 1.0 mM | 0.4925 | 0.3913 | 0.3250 |
| 2.0 mM | 0.5500 | 0.4616 | 0.4069 |

*(relief ÷ transverse decay length; `resolvable` is false at all nine.)* The rim wanders by less
than the distance over which its own perturbation dies, so **a ragged rim is a straight rim at its
mean** as far as `C-0022`'s collar is concerned. It had to be checked rather than assumed, because
1.36 nm is the same size as `C-0005`'s 1.46 nm gap resolution.

## `T-231` Deliverable 5 — what it DOES cost: the plan budget, and there it is not small

A row 4 bp short has 4 bp = **1.36 nm** less of its own axial extent outboard of any fixed root
plane. Against `C-0141`'s honeycomb plan ceilings:

| demanded paths (`15 × 4`) | 10–15 | 20–30 | 34–45 | 50–60 | 75 | 90 |
|---|---|---|---|---|---|---|
| ceiling [nm] | 38.08 | 16.66 | 9.52 | 5.872 | 4.604 | **2.380** |
| relief as a fraction | 0.036 | 0.082 | 0.143 | 0.232 | 0.295 | **0.571** |

This is the one channel on which the raggedness is a real design variable, and it is one because a
row length is a base-pair **count**: the relief is 4 whole rises, not a residue.

## `T-231` Deliverable 6 — and what it BUYS, on an axis nothing upstream priced

All three of Rothemund's measured anti-stacking remedies work by denying a **terminus** a coaxial
partner; a staggered face denies it geometrically. The range is measured and already in the corpus
(`gpd/data/T-139-blunt-end-stacking-literature.md`, `read directly`): the all-atom PMF's
**attractive limb ends at 0.65 nm** and its force **turns repulsive past 1.30 nm**.

| pair | stagger | extent | front relief | clears 0.65 nm | clears 1.30 nm | margin |
|---|---|---|---|---|---|---|
| 101 / 109 | 8 bp | **`−0.55 %`** | 2.72 nm = 8 rises | yes | **yes** | **`+4.18` rises** |
| 102 / 109 | 7 bp | `−1.40 %` | 2.38 nm = 7 rises | yes | yes | `+3.18` rises |
| **112 / 108** | **4 bp** | `−1.40 %` | **1.36 nm = 4 rises** | yes | **yes** | **`+0.18` rises** |
| 112 / 109 | 3 bp | `−2.25 %` | 1.02 nm = 3 rises | yes | **no** | `−0.82` rises |

**`F6` FIRED, favourably, and it was declared open.** `C-0140`'s 112 / 108 clears the conservative
onset where its own *"tightest stagger"* 112 / 109 does not — but it clears it by **0.06 nm**,
which is **0.18 of a rise** and therefore *not a quotable margin* by this project's own rule; and
among **all** pairs that fit M13 the width optimum is **101 / 109 at `−0.55 %`**, which `C-0140`
excluded through a self-imposed *"stagger of at most 4 bp"* filter it gives no ground for.

> **Priced on four axes the two candidates rank 3–1.** `101 / 109` wins the width, the scaffold
> (6 308 nt against 6 596) and the anti-stacking clearance; `112 / 108` wins **only** the plan
> budget — and wins it decisively, because 2.72 nm **exceeds** the 2.380 nm saturated ceiling and
> leaves a short row no outboard budget at all. **So the recommendation stands only for a design
> that needs a saturated path count**; at `C-0142`'s flat cells on `10 × 6` (10 to 50 paths,
> ceilings 38.08 down to 4.604 nm) both pairs are affordable and the wider one is better on three
> axes of four. That is
> [`CH-0187`](../challenges/CH-0187-the-two-length-recommendation-rests-on-an-unstated-filter.md).

---

## The five verification gates

Executed as **25 gate-named tests** in `src/test/kotlin/structure/HoneycombTurnLoopTest.kt`.
The tests were written before the model file existed; because they were not run against an absent
model, they were **mutation-tested** afterwards instead, and that is recorded here rather than
claimed as a watched failure.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a span is a length and its two extremes are the line of centres; an axial offset enters in quadrature; `n` unpaired nucleotides make `n + 1` steps; a square wave enters a sinusoidal transfer through its fundamental; a departure degrades to absolute at a zero reference; seven guards refuse a non-physical geometry | **PASS** |
| **2 — limiting cases** | **the cheap bound**: the zero-slack crossover span lies inside the MEASURED phosphodiester step and closes at `n = 0`; the reach bound is the exact inverse of the reach at 41 consecutive counts and at ±1e−9 either side; the Langevin function has both limits and is finite at `u = 1e12` (`CLAUDE.md`'s `cosh/sinh` trap); the FJC tension vanishes at zero extension, exceeds 5 pN at 0.78 of contour, and **throws** below the reach | **PASS** |
| **3 — symmetry and reproduction** | the inverse Langevin inverts the Langevin at nine extensions to `1e−9`; the FJC tension and free energy reduce to the Gaussian spring at `x ≈ 1e−3`; the free energy equals a **20 000-point quadrature of its own tension**; `C-0140`'s 92 / 98 / 106 bp and `60 × (98 + 28) = 7 560`; its 4 / 8 bp faces, its 116 bp extent and its 112 / 109 pair; a **uniform** row length leaves both faces flat; the front-face raggedness **IS** the stagger at five staggers; every helix carries one of exactly two lengths | **PASS** |
| **4 — exactness and periods** | the period of a constant sequence is 1 and of an alternation 2; the gap-facing rim alternates with period **2** and spread **4 bp** on `15 × 4` **and** on `10 × 6`; the reach bound is monotone in the span and in the step. **There is no mesh and no sampling** — both tasks are exact arithmetic and one bisection, and the convergence gate is discharged as exhaustion over whole families plus the inverse Langevin settling to `1e−12` at 200 iterations against `1e−6` at 40 | **PASS** |
| **5 — literature and upstream** | **ten reproductions at departure `0.0`**: `C-0140`'s M13 and p8064 ceilings, its loop nucleotides, its front and rear spreads and its axial extent, all from its result file; the paper's own `98 + 28 = 126` and `60 × 126 = 7 560`; `C-0141`'s `3d/2` row pitch; `C-0142`'s tightest flat cell `0.0973238201`. The ripple transfer and the slit decay are the corpus's **own** functions, called rather than re-implemented; the stacking range is read from `gpd/data/T-139-blunt-end-stacking-literature.md` with its own flags | **PASS** |

### The declared falsifiers, and what happened

| # | task | falsifier | fired? | outcome |
|---|---|---|---|---|
| **F1** | `T-230` | the crossover span falls outside the measured step at 3σ, so a honeycomb scaffold crossover is impossible | **no** | `+1.49997857 σ`, inside the 99th percentile |
| **F2** | `T-230` | the reach bound is at or above the built 28 nt, so 28 is a REQUIREMENT | **no** | 6 against 28, a factor of `4.66666667` |
| **F3** | `T-230` | the criteria disagree by more than a decade — **declared open** | **no** | 41/6 = 6.83; they bracket, and 28 lies inside the bracket |
| **F4** | `T-230` | the model fails to reproduce `C-0140`'s 92 / 98 / 106 bp | **no** | all three at departure `0.0` |
| **F5** | `T-230` | route B still fails to fit 112 bp on M13, so the routes never compete | **no** | M13 affords 8 nt against a reach bound of 6 |
| **F6** | `T-230` | the FJC law fails its own limits | **no** | asserted as three tests, including a 20 000-point quadrature |
| **F1** | `T-231` | the ragged face is a GAP-FACING face | **no** | it is the rim; the two axes are orthogonal |
| **F2** | `T-231` | the spreads fail to reproduce `C-0140`'s 4 and 8 bp | **no** | departure `0.0`, and the identical 4 / 8 on `10 × 6` |
| **F3** | `T-231` | the modulation is longer than the bending length, so nothing attenuates | **no** | 7.608 nm against 17.23–23.21 nm, 2.3–3.1 wavelengths per bending length |
| **F4** | `T-231` | the bounded flatness cost exceeds the threshold — **declared open** | **no** | 5.5e−05 against 0.0275, a margin of **496×** |
| **F5** | `T-231` | the relief is below the 0.34 nm quantum | **no** | 4 and 8 whole rises, by construction |
| **F6** | `T-231` | the recommendation changes under the second axis — **declared open** | **FIRED** | 101 / 109 wins three axes of four; 112 / 108 survives on the plan budget alone, and only at a saturated path count |

---

## What this hands the rest of the programme

- **`T-235` (`C-0146`) needs the raggedness numbers and they are here**: 4 bp front and 8 bp rear
  on **both** cross-sections, a 2-row modulation at 7.608 nm, and a bound of `1.68e−05` of the
  stroke on `10 × 6` — so the raggedness is **not** a term in that re-grade. The two claims were
  written independently in the same iteration and **agree**: `C-0146`'s per-**row** reading (every
  raster row spans 112 bp, consecutive rows staggered by 4) and this claim's per-**helix** one
  (112 or 108 per helix, faces ragged 4 and 8) are the same object, because a short helix is
  recessed inside its own row's window.
- **`T-233` / `ANSWERS.md` and `DECISIONS-FOR-NDI.md`** (owned by another agent this iteration) are
  owed two sentences: that the honeycomb turn slack is **6 nt by reach and 8 nt by M13's budget**,
  against the 28 nt the built blocks spend; and that the 4 bp ragged face costs §3's flatness
  **nothing** because it is on the rim.
- **`tools/result-reader-census.py --check` passes but reports three studies not in the emitted
  census** — the two filed here and one a concurrent agent is still writing. `--emit` was **not**
  run, because it would freeze a sibling's in-flight study into a shared file; it should be run
  once the iteration's studies are all committed.

## Still open — named, not answered

1. **A per-row row length is not a parameter of any lattice model in this repository.**
   `OrigamiGrillage` and `HoneycombCoupledTile` both take a single `edgeX`. The flatness cost is
   therefore **bounded**, not measured, and the bound carries `CLAUDE.md`'s own 50× free-edge
   penalty because the transfer function is an infinite-plate result.
2. **What a turn loop between 6 and 28 nt does to folding yield is unmeasured**, and no published
   measurement is on that axis. The threshold that would decide it is **8 nt**.
3. **The anti-stacking benefit is a FABRICATION consideration and this programme cannot price
   fabrication.** It is reported as a mechanism with a measured range, never as a yield —
   `CLAUDE.md`'s *"rank what two options BUY, never what they cost"*.
4. **Which face §3's effort point sits nearer is not asked here**, and the rear relief is twice the
   front's at every stagger.
5. **The turn's steric clearance past the neighbouring duplex is not modelled** — the span is a
   straight line between two phosphates, which is a lower bound on the path an actual loop takes,
   so the reach bound is a lower bound in that direction too.
