# C-0205 — **`C-0180`'s COUPLED RECOVERY NEEDS `607.396049`–`834.060958 pN/nm` OF LINK STIFFNESS AND THE CROSSOVER CONNECTOR'S SHEAR MECHANISM SUPPLIES AT MOST `254.808095` — SO `2 of 64` IS A READING AT A NUMERICAL PENALTY AND THE CENSUS IS `0 of 64` AT EVERY ROUTE THE CONNECTOR CAN SUPPLY.** Three closed-form routes and no solver: `C-0194`'s span-law tension `41.4338953`, Chen et al.'s own softened bond read on the **displacement** axis `64.7058824` — which carries **no `k_θ` at all** and agrees with the first within **`1.56166544×`** — and the connector's own bending `89.6961146`–`190.102213` at **clamped** ends and **exactly zero** at pinned ones, on a continuum `c(ρ) = 12ρ/(6+ρ)` derived here for a *relative end displacement* and borrowed from neither `C-0025` nor `C-0034`. **Bisected on `log₁₀ k_link` to a bracket `3.05175781E-5` decades wide**, the two cells `C-0180` recovered cross `T-5b` at **`834.060958`** and **`607.396049 pN/nm`**, so the ceiling is short by **`3.27329066×`** and **`2.38373921×`**, and the whole 64-cell census reads **`0 / 0 / 0 / 2 / 2`** at `41.4338953 / 64.7058824 / 254.808095 / 1 000 / 10 000` — `C-0167`'s own `0 of 64` given back by the shear mechanism. **Route B agrees by another road and needs no threshold at all**: `0 of 16` of `C-0201`'s tethered readings are flat over four decades, so the link stiffness decides everything on the design nobody has folded and nothing on the one the 2009 staple order buys. **And the ceiling is exact for the IN-PLANE bonds ONLY** — `HoneycombTetherElement.normalStiffness` is `tangent·unitZ² + secant·unitY²`, so the same source file already resolves a chain's two mechanisms by the bond's own direction while a **bond**'s link is one scalar; through the thickness `unitZ² = 0.75` at **300** of the block's **435** bonds, most of a relative `W` displacement is a change of the interhelical **separation**, and resolved that link is `475.448622`–`1211.56918 pN/nm`, which **straddles both thresholds**. **`F5` was declared OPEN and did NOT fire; `F4` and `F8` were declared open and BOTH fired**, and the second is the useful one: beam subdivision moves the *threshold* by **21 %** and the *verdict* by nothing at either refinement. **No published number for a crossover's stiffness against a relative normal displacement exists** — eight recorded queries, two papers read directly, none on the coordinate

| | |
|---|---|
| **Task** | [`T-303`](../tasks/T-303-what-link-stiffness-the-recovery-needs.md) — raised by [`C-0194`](C-0194-the-common-mode-is-the-link.md) (`T-297`) §6, where `F10` was declared open and **FIRED** |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (three closed-form routes to the connector's own transverse stiffness, two of which carry no `k_θ`; a slope-deflection derivation of the relative-end-displacement end-condition continuum; and the central-force sign argument) **+ in-silico** (the same three-dimensional beam-and-bond lattice, the same exact Woodbury coupling surrogate and the same `C-0087`-measured incorporation dropout over 4 000 realisations of one common stream as `C-0167` and `C-0180`, swept over an **existing** constructor argument) **+ literature** (eight recorded EuropePMC queries, two papers read directly, and `C-0201`'s own committed route-B sweep read and cited) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Every route to the link stiffness is a **construction**: route 1 is `CH-0242`'s attribution of the whole of `k_θ` to the span mechanism, route 2 is Chen et al.'s own softened-bond construction — which `Gen1Tile`'s KDoc already flags as *"a construction, not a measurement"* — read on a second axis, and route 3 is a worm-like chain over one phosphodiester step. Nothing here measures a crossover's normal-link stiffness and the recorded search found nothing that does. |
| **Verdict** | **PASS on all five predicates. `F1`, `F2`, `F3`, `F6`, `F7`, `F9` and `F10` did not fire; `F4`, `F5` and `F8` were declared OPEN, and `F4` and `F8` FIRED.** `P1` three independent routes and an explicit ceiling, all closed form; `P2` both thresholds bisected on `log₁₀ k_link` with monotonicity asserted over the whole ladder first; `P3` all 64 cells re-graded at five rungs; `P4` route B answered from its own artifact; `P5` convergence taken at the deciding cell on the deciding quantity, and all **eleven** reproductions closed — `C-0194` §6's eight shared rungs at a worst **`4.4e-10`** and `C-0180` §2's two recovered cells at **`3.5e-11`**. |
| **Provenance** | [`gpd/results/T-303-what-link-stiffness-the-recovery-needs.json`](../results/T-303-what-link-stiffness-the-recovery-needs.json) (`tile.LinkStiffnessThresholdStudyKt`, **new**); model [`tile/CrossoverLinkStiffness.kt`](../../src/main/kotlin/tile/CrossoverLinkStiffness.kt) (**new file**). **NO SHARED SOURCE ON ANY NUMBER'S PATH IS EDITED** — `tile/HoneycombGrillage.kt`, `tile/HoneycombTiedRegrade.kt`, `tile/CrossoverCommonMode.kt`, `coupling/NonUniformCoupling.kt` and `coupling/DropoutRobustPlacement.kt` were **read, not edited**, so nothing `C-0154`, `C-0167`, `C-0175`, `C-0180`, `C-0194` or `C-0201` publishes can move and no consumer re-run is owed. The one shared source touched is `structure/ResultInputs.kt`, which gains a `T_303` handle because the tree's invariant is *every result path spelled in a main source has a handle*, and that edit is **provably inert**: `ResultInputs.all` is read at 8 sites, every one of them in `structure/ResultInputsTest.kt`. **Eighteen gate-named tests written first and watched fail** — [`tile/CrossoverLinkStiffnessTest.kt`](../../src/test/kotlin/tile/CrossoverLinkStiffnessTest.kt), which did not compile against a model that did not exist — of which **one failed on its first real run and it was the author's mistake, not the code's** (§8) — and **mutation-tested afterwards**, [`tools/T-303-mutation-test.py`](../../tools/T-303-mutation-test.py), **15 mutations over a subtracted baseline of 0, `0` survivors** — after the eighteenth test was written to kill the one that survived the first run (§10a) — with the subject asserted to occur at exactly one path and every anchor exactly once. Result file **byte-identical across two independent JVM runs**, after one `thresholds/residualAtThreshold` field was re-emitted at two significant digits because it is a difference of two nearly equal numbers and the shared departure baseline is keyed on `reproductions`/`convergence` records (§8b). A full `./gradlew test` on the final sources gives **3 459 tests, 1 failure**, and the one failure is a sibling agent's in-flight window rather than this work: `ResultInputsTest > every handle resolves to a committed result file` names `T-304` and `T-307`, whose result files were untracked when the snapshot was taken; **`T_303` resolves**. `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-entry-points.py`, `check-queue-vocabulary.py`, `check-kotlin-format-strings.py`, `check-cold-start-note.py`, `check-result-path-references.py`, `check-result-file-hygiene.py` (all four arms), `trace-answers.py` and `result-reader-census.py --check` all exit 0. Sources and recorded queries in [`gpd/data/T-303-sources/`](../data/T-303-sources/README.md). |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.141947 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); `r_P` = 0.908637858 nm (`T-71`, measured on 13 084 crystallographic linkages); `g = d − 2r_P` = 0.718724283 nm; in-plane row pitch `3d/2`, layer pitch `d√3/2`, rise 0.34 nm/bp. `k_θ` = 13.5294118 pN·nm/rad and `k_s` = 64.7058824 pN/nm at `α = 1`. ssDNA Kuhn 1.34 nm (10–40 pN force spectroscopy) and 2.84 nm (zero force at 2 mM), so `L_p = b/2`. Cross-section `10 × 6`, block extent **116 bp = 39.44 nm**, raster `102 / 109` (`C-0151`, drawable), 435 staple bonds and **59 raster turn ties at `s = ±L/2`** with **zero** prestrain. `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing face only; §3's 100 pN over the face; `C-0017`'s mandate at the **acceptable** clause; `C-0058`'s rim-graded 5:1 at a 6.7 nm band and its equal-spring twin; `C-0087`'s measured depth incorporation; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Link ladder **41.4338953, 64.7058824, 100, 254.808095, 1 000, 10 000 pN/nm**; bisection bracket **30 to 3 000 pN/nm**, 16 iterations. |
| **Consumes** | [`C-0194`](C-0194-the-common-mode-is-the-link.md) (`T-297`) — the span law, the `d/2` theorem, the implied bond tension and the six-rung coupled ladder, **reproduced at a worst `4.4e-10`**; [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) — the two recovered cells, **reproduced**; [`C-0201`](C-0201-the-tether-is-a-load-not-a-spring.md) (`T-299`) — route B's own link sweep, **read and cited**; [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md), [`C-0175`](C-0175-drawable-raster-rim.md), [`C-0154`](C-0154-honeycomb-grillage.md), [`C-0151`](C-0151-closing-raster-selection.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0104`](C-0104-row-end-prestrain.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0001`](C-0001-layer-stiffness.md); `T-71`'s measured backbone; `CH-0242`'s premise |
| **Constrains** | **Two challenges are raised.** [`CH-0258`](../challenges/CH-0258-two-of-sixty-four-is-a-reading-at-a-penalty.md) against `C-0180`'s headline `2 of 64` and the passages carrying it — the count is a reading at `k_link = 1e4 pN/nm` and is `0 of 64` at every rung the shear mechanism supplies. [`CH-0259`](../challenges/CH-0259-one-scalar-for-two-mechanisms.md) against `C-0194` §4's transfer of a **shear** stiffness to every bond direction, and against this claim's own ceiling, which inherits it. **`C-0194`'s `F10` is DISCHARGED**: the threshold it left open is bisected and the census across it is taken. **No number of `C-0180`, `C-0187`, `C-0194` or `C-0201` moves** — every one that could be re-taken here reproduces at `0.0`. **Two tasks are opened**: `T-309`, move the mutation harness into `tools/` and declare it in `P-31`'s registry; `T-310`, a per-bond link stiffness in `HoneycombGrillage` and the census re-taken on it, which is what `CH-0259` needs. |

---

## 1. The cheap bound is the whole of the first deliverable, and it runs before any lattice

Three routes to one coordinate — the stiffness on the link residual
`R = ΔW + (d/2)·unitY·(Φ_a + Φ_b)`, which `C-0194` established **is** the crossover's common
azimuthal mode.

| route | premise | carries `k_θ`? | pN/nm |
|---|---|---|---|
| tension, `C-0194`'s span law `k_R = T/g`, `T = 2k_θ/r_P` | `CH-0242`'s own — both eigenmodes of the span form are one mechanism — at **full** attribution, so the largest tension it admits | yes | **`41.4338953`** |
| Chen et al.'s softened bond, read on the **displacement** axis, `2αS/(100a)` | the same two phosphate bonds the lattice already prices for **axial slip**, resisting a displacement in another direction | **no** | **`64.7058824`** |
| the connector's own bending, `c(ρ)·EI/g³`, `EI = L_p k_BT`, **clamped** ends | a beam in double curvature; `c ∈ [0, 12]` needs no `k_r`, because both limits are exact | **no** | **`89.6961146`** to **`190.102213`** |
| **the ceiling** — the larger displacement route plus the stiffest bending, every mechanism at its most favourable at once | | | **`254.808095`** |

Against `RIGID_LINK_STIFFNESS = 10 000 pN/nm` that is a factor of **`39.2452209`**.

**Two of the three routes carry no `k_θ` at all, and the two that can be compared agree within
`1.56166544×`.** That is what makes the ceiling a bracket rather than a restatement of
`CH-0242`'s premise: route 2 substitutes the **stretch** modulus `S` where the hinge substitutes
the **bending** rigidity `B`, so no part of route 1's attribution enters it, and `Gen1Tile`'s own
KDoc says the transfer in as many words — *"the same two phosphate bonds on the orthogonal axis"*
is `CLAUDE.md`'s phrasing of a comparison nobody had made.

### 1a. The end-condition continuum is derived here, and it is a third one

Slope-deflection on a connector whose two ends are displaced transversely relative to each other,
each held by a rotational spring `k_r` against its own duplex, with `θ_A = θ_B = θ` by symmetry
and a chord rotation `ψ = δ/g`:

&nbsp;&nbsp;&nbsp;&nbsp;`θ = 6ψ/(6+ρ)`, &nbsp; `F = (12EI/g³)·(ρ/(6+ρ))·δ`, &nbsp; `ρ = k_r g/EI`,

so `c(ρ) = 12ρ/(6+ρ)` — **exactly 0** at a pinned end and **exactly 12** at a clamped one, and
exactly 6 at `ρ = 6`. `CLAUDE.md` records that a `c(ρ)` never transfers between boundary-value
problems and this is the third member of the family in this repository: `C-0025`'s
`192(ρ+2)/(ρ+8)` is a midspan-loaded beam and `C-0034`'s `12(1+ρ)/(4+ρ)` a cantilever/guided pair.
Because both limits are exact, `c ∈ [0, 12]` is a bracket that needs no `k_r` — which is the whole
point, since nothing measures `k_r` either.

### 1b. The fourth term is carried for its SIGN, and it lowers the ceiling

Two duplexes interact directly and that interaction is **central**. At separation `d` a relative
displacement `δ` perpendicular to the line of centres gives `r ≈ d + δ²/(2d)`, so the transverse
stiffness is `V′(d)/d` — **negative** wherever the pair repels, which `CLAUDE.md` records this
pair does at every separation on four independent measured methods. Per unit of repulsive force
per unit length, over the 21 bp of interface one crossover owns, it is
**`−2.81545741 pN/nm`**. It is reported and **not** added.

## 2. The threshold, bisected — and monotonicity asserted before it is believed

`CLAUDE.md`: *a verdict that is not monotone in a swept variable has no threshold*. So the ladder
is taken first and the monotonicity is measured, and only then is anything bisected. The two cells
`C-0180` recovered, at `f = 0.30` and `C-0058`'s rim-graded 5:1, on the same stations and the same
4 000-realisation stream:

| `k_link` [pN/nm] | cell A — abstract grid, 3 × 10 | cell B — abstract grid on the rooting helices, 5 × 10 |
|---|---|---|
| **`41.4338953`** (`C-0194`'s span law) | `0.107701645` — not flat | `0.103971919` — not flat |
| **`64.7058824`** (the softened bond) | `0.104897604` — not flat | `0.102517655` — not flat |
| `100` | `0.10306316` — not flat | `0.101678429` — not flat |
| **`254.808095`** (**this study's ceiling**) | **`0.101045111` — not flat** | **`0.100581834` — not flat** |
| `1 000` | `0.0999026162` — flat | `0.0999353736` — flat |
| **`10 000`** (the standing penalty) | **`0.0995744767`** — flat | **`0.0998791032`** — flat |

Both cells are monotone over the whole ladder. Bisected on `log₁₀ k_link` over `[30, 3 000]` to a bracket **`3.05175781E-5` decades** wide,
with a residual at the root of `-1.6e-08` and `3.6e-09`:

| cell | threshold `k*` [pN/nm] | ceiling / `k*` | the ceiling is short by |
|---|---|---|---|
| A, 30 paths | **`834.060958`** | `0.305502965` | **`3.27329066×`** |
| B, 50 paths | **`607.396049`** | `0.419508977` | **`2.38373921×`** |

**`F5` was declared OPEN — *the ceiling reaches the threshold, i.e. the recovery survives* — and it
did NOT fire.**

## 3. The whole census, quoted with the link stiffness it is read at

All 64 of `C-0167`/`C-0180`'s coupled cells — 4 placements × 4 column counts × 2 distributions ×
2 composite fractions — at every rung:

| `k_link` [pN/nm] | what it is | flat at the 90th percentile | tightest `p90` |
|---|---|---|---|
| `41.4338953` | `C-0194`'s span law | **0 of 64** | `0.103971919` |
| `64.7058824` | Chen et al.'s softened bond | **0 of 64** | `0.102517655` |
| `254.808095` | **the ceiling** | **0 of 64** | `0.100581834` |
| `1 000` | one decade below the penalty | 2 of 64 | `0.0999026162` |
| `10 000` | the standing penalty | **2 of 64** | `0.0995744767` |

The bottom row is `C-0180`'s own `2 of 64`, reproduced (`F10` did not fire), and the top three are
`C-0167`'s `0 of 64` given back. **`C-0180`'s count is correct and it is a reading at `1e4 pN/nm`**
— `CH-0258`.

## 4. Route B needs no threshold, and that is the other half of the answer

`C-0201`'s committed result file already carries its own `linkStiffness` block. Read out of it and
not re-run: **`0 of 16`** tethered readings are flat at the 90th percentile, over four decades and
four cells, and the `p90` **rises** as the link softens at every one — so extrapolating below the
swept range cannot help either. **`F9` was declared OPEN and did not fire.**

So the link-stiffness question **decides everything on route A and nothing on route B**, and
`C-0193`/`C-0200` establish that route B is the arm the built object occupies. Both routes now
agree on the count: `0 of 64`.

## 5. The ceiling is exact for the in-plane bonds, and the same file already knows why

`HoneycombTetherElement.normalStiffness` is `tangent·unitZ² + secant·unitY²` — the source file
already resolves a chain's two mechanisms onto the link residual **by the bond's own direction**.
A **bond**'s link carries one scalar at every direction.

| bond direction | bonds | `⟨unitZ²⟩` | resolved link, at the two axial candidates |
|---|---|---|---|
| in plane | **135** | `0.0` | the shear ceiling, `254.808095` — **exact here** |
| through the thickness | **300** | **`0.75`** | **`475.448622`** to **`1211.56918`** |

The two axial candidates are the corpus's own: `C-0194`'s implied phosphodiester-step stiffness
**`548.995464 pN/nm`** and the duplex stretch modulus over the span, **`1530.48954 pN/nm`**. Both
thresholds — `834.060958` and `607.396049` — lie **inside** that bracket. So what decides the
coupled recovery is an **axial** mechanism nobody has priced, on two thirds of the bonds, and not
the shear mechanism two claims have spent themselves on. `CH-0259`, and `T-310`.

## 6. The literature: the number does not exist

`gpd/` was checked first, as `CLAUDE.md` requires, and it did not pay: the corpus's own crossover
elastic constants are `k_θ` (Chen et al., fitted) and `k_s` (**a construction**, and its KDoc says
*"Nothing in the accessible literature gives it in any form"*). Eight EuropePMC queries are
recorded in [`gpd/data/T-303-sources/`](../data/T-303-sources/README.md) with their hit counts;
two of the three plausible candidates were **read directly** and neither is on the coordinate —
Kaufhold et al.'s metadynamics collective variable is a **joint angle** of a whole device, and
Sengupta et al.'s four-way-junction stiffness constants are **base-pair steps inside one duplex**.
Snodin et al. measure the interhelical **distance** and its standard deviation, which is the
line of centres — the coordinate the link is *perpendicular* to.

**So `T-303` closes the way this programme closes an unsourceable coefficient: a ceiling and a
threshold instead of a value.**

## 7. The five verification gates

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | `c(ρ)` asserted a function of `ρ` alone at three values; `k_B` asserted to scale as `1/λ²` under a common rescaling of `L_p` and `g` at three scale factors; every stiffness pN/nm, every rotational stiffness pN·nm/rad |
| **2 — limiting cases** | `c(0) = 0` and `c(∞) = 12` **exactly**; `c(6) = 6`; monotone over six rungs; a pinned connector supplies exactly zero; a non-positive span and a negative `L_p` refused; the bisector refuses a bracket that does not straddle **and** a non-positive endpoint; a `null` threshold emitted rather than an endpoint where the residual does not cross |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the free tied lattice dishes exactly zero at BOTH ends of the link ladder** — re-taken because a link stiffness moves every entry of the matrix — and the lattice identity between this study's builder and `T-279`'s asserted under a **unit point load**, because a bare uniform pressure's peak dishing is its own conditioning noise |
| **4 — numerical convergence** | the deciding quantity is the **bisected threshold** and the deciding cell is the tighter of the two; beam subdivisions `1 → 2` move it **21 %** (`606.436479 → 735.816788`) and the dishing grid `81 → 161` moves it **`0.0`**. `F8` fired on the value; the **verdict** survives at `2 of 2` refinements, because the ceiling `254.808095` is below both readings. `CLAUDE.md`'s *convergence is a property of the quantity*, met on a threshold rather than on a field |
| **5 — literature and upstream** | `r_P` and the phosphodiester step from `T-71`'s own 13 084-linkage measurement; `d` from Fischer et al.'s SAXS; `k_θ` and `k_s` from Chen et al.; the ssDNA Kuhn bracket from Bosco/Camunas-Soler/Ritort and the zero-force scattering end; **eleven reproductions, `C-0194` §6's eight shared rungs at a worst `4.4e-10` and `C-0180` §2's two recovered cells at `3.5e-11`; the worst of all eleven is `7.2e-9`, on the literal `41.4338953` `C-0194` §4 prints its own `k_R` to**; and the recorded negative existence result of §6 |

## 8. What failed on its first run, and it was the author

One of the seventeen tests failed the first time it was run for real, and the code was right.
`F2`'s lattice identity — *this study's builder at the default penalty is the object `C-0180`
measured* — was written on a **uniform pressure**, whose peak dishing on a free tile is
`1.97e−14`: two identically-constructed lattices then disagree by `1.3e−18`, which is not zero,
and the relative test compares two noises. `CLAUDE.md` records exactly this
(*"the uniform-load falsifier must be taken on a WELL-CONDITIONED load case when it is used as an
identity between two lattices"*) and it was walked into anyway. Re-taken on a unit **point load**
at the face centre — which is what the coupling surrogate asks the lattice for in any case — the
two agree to `1e−12` relative.

### 8b. And one field made a 225 kB file un-diffable

Two runs of the finished study agreed on **every** field but two: the `residualAtThreshold` of the
two bisected cells, in their ninth digit. A residual at a bisected root is `p90(k*) − 0.10` — a
difference of two nearly equal numbers — and the root itself moves by an ulp when a `Double`
comparison inside the bisection flips. `DEPARTURE_DIGITS_BY_KEY` is keyed on
`reproductions`/`convergence` records and cannot see a `thresholds` one, so the two digits `CLAUDE.md`
already asks for had to be asked for **by name**. Re-emitted, the file is byte-identical.

## 9. `F4` fired, and its size is what makes it harmless

`F4` was declared open: *the `p90` is monotone decreasing in `k_link` at every graded cell.* Over
the census it is not — **1 of 256** consecutive pairs rises, by **`2.5e−5`** of the stroke. A
`p90` is an order statistic over a 4 000-realisation sample, so exact monotonicity is not owed;
what the bisection needs is monotonicity **at the cells it is taken on**, and both of those are
monotone over the whole six-rung ladder. That is why the monotonicity block is emitted **per
deciding cell** as well as over the census.

## 10. The mutation table

Fifteen mutations over the eighteen tests, applied after they were green, against a **subtracted
baseline** (`CH-0237`), each replacing a rule **wholesale** (`C-0176`), with the subject asserted
to exist at exactly one path (`C-0190`) and every anchor exactly once (`C-0185`). **`0` survivors,
after the first run's one survivor was killed by a test written for it** (§10a).

| # | what it breaks | killed by, named tests |
|---|---|---|
| `M01` | the end-condition continuum's denominator, so `c(6)` is no longer 6 | 2 — the `c(6) = 6` limit and the rescaling invariance |
| `M02` | the end-condition factor ignores `ρ`, so a pinned end is as stiff as a clamped one | 3 — both limits, the monotonicity and the rescaling invariance |
| `M03` | the bending stiffness' power of the span, so it no longer scales as `1/λ²` | 1 — the `1/λ²` rescaling |
| `M04` | the bending stiffness drops the end-condition factor, so a pin is not free | 1 — a pinned connector supplies exactly zero |
| `M05` | the softened-bond route reads the **hinge**, so it is no longer `k_θ`-independent | 2 — the softened-bond identity and the bracket |
| `M06` | the central pair term's sign, so a repulsive pair appears to stiffen the link | 1 — the central term's sign |
| `M07` | the ceiling takes the **softest** connector instead of the stiffest | 1 — the bracket's ceiling |
| `M08` | the ceiling takes the smaller of the two displacement routes | 1 — the bracket's ceiling |
| `M09` | the floor is no longer the pure-tension route | 1 — the bracket's floor |
| `M10` | the bisector accepts a bracket that does not straddle | 1 — the bisector refuses a bracket that does not straddle |
| `M11` | the bisector keeps the wrong half | 2 — the known root and the bracket width |
| `M12` | the bisector returns a bracket endpoint instead of its midpoint | **1, and only after the eighteenth test was written** — §10a |
| `M13` | the lattice builder ignores its own link-stiffness argument | 1 — an untied lattice at every link stiffness |
| `M14` | the lattice builder ties an untied lattice | 1 — an untied lattice at every link stiffness |
| `M15` | the end-condition factor no longer refuses a negative `ρ` | 1 — a negative `ρ` is refused |

### 10a. One mutation survived the first run, and it was a fixture that could not discriminate

`M12` replaces the bisector's `10.0.pow(0.5 * (lo + hi))` with `10.0.pow(lo)` — an **endpoint**
where the midpoint belongs — and on the first run it failed **nothing**. Neither existing test can
see it: at 60 iterations the final bracket is `3/2⁶⁰` decades wide and both readings are the root
to fifteen digits, and the 12-iteration test asserts only that the answer is *inside* the bracket,
which an endpoint is.

`C-0161`'s rule is that such a mutation is **the finding**, and its cure is to **construct** the
state. One iteration over `[1, 100]` leaves the bracket `[10¹, 10²]`, whose geometric centre is
exactly `10^1.5`; putting the root there makes the midpoint **exact** and an endpoint out by half
a decade. The eighteenth test does that and kills `M12`, and the table above is the second run:
**15 mutations, 0 survivors, over a subtracted baseline of 0**.

## 11. Validity range

- **Every route is a construction and none is a measurement.** Route 2's transfer — that a softened
  covalent bond resists a normal displacement as it resists an axial one — is an **isotropy**
  argument, exact for a flexible link and a model for a phosphodiester pair with a preferred
  direction.
- **Route 3 puts a worm-like chain over ONE phosphodiester step.** A persistence length is a
  thermal average over rotameric freedom; over `0.718724283 nm` the backbone is stiffer in bond
  bending and softer in torsion than a smooth rod, and `c ∈ [0, 12]` is what absorbs the direction
  of both.
- **The ceiling is a SHEAR ceiling.** §5 and `CH-0259`: it is exact at the 135 in-plane bonds and
  it is not the whole story at the 300 that run through the thickness.
- **The threshold is a property of one distribution rule, one placement family, one cross-section,
  one raster and one load case.** Nothing here re-opens the placement search, and a distribution
  *searched* at the physical link stiffness rather than transferred onto it is not graded.
- **The census is taken on route A**, whose turns carry **zero** unpaired nucleotides — `C-0175`'s
  modelling choice. `C-0193` and `C-0200` establish that the only folded block of this
  cross-section does otherwise, so the whole threshold is a statement about a design nobody has
  folded. On the built object (route B) the question does not arise: `0 of 16` at every link
  stiffness swept.
- **`F8` fired.** The bisected threshold moves 21 % between beam subdivisions 1 and 2. The verdict
  does not, at either refinement, because the ceiling is below both readings — but the *value*
  `834.060958` / `607.396049` should be read as *"of order 600 to 900 pN/nm"*, not to nine digits.

## 12. Open questions

- What an oxDNA or all-atom measurement of a crossover's **normal** relative-displacement
  stiffness would give. Nothing published carries it (§6).
- What the **axial** half of `CH-0259` is worth once `HoneycombGrillage` carries a per-bond link.
  That is `T-310` and it is where the answer now lives.
- What Chen et al.'s own `α` bracket, 0.6 to 1.2, is worth on the census. It scales route 2
  linearly and moves the ceiling by the same factor, which does not close the gap at either end.
- Whether a distribution **searched** at the physical link stiffness recovers any cell.
