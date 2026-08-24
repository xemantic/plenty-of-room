# C-0208 — **`C-0205`'s `0 of 64` STANDS AT THE RESOLVED PER-BOND LINK, AND THE REASON IS THE MECHANISM `CH-0259` SAID WAS NOT THE DECIDING ONE: THE 135 IN-PLANE BONDS ALONE FORBID THE RECOVERY.** `HoneycombGrillage` now resolves a bond's normal link by the bond's own direction, `k_radial·unitZ² + k_transverse·unitY²` — `HoneycombTetherElement.normalStiffness`'s expression on a **bond** — and at the `135` in-plane bonds `unitZ = 0` so the link is `C-0205`'s shear ceiling `254.808095 pN/nm` **exactly**, while at the `300` that run through the thickness it is **`629.20588`–`1365.32644`**. Graded there the census reads **`0 of 64` at every one of five radial rungs**, the tightest cell in the whole corpus misses `T-5b` by **`0.198 %`** (`0.100198485`), and with the transverse constant pinned at its own ceiling the two cells `C-0180` recovered need a **radial** constant of **`4581.61268 pN/nm`** and *no radial constant at all*: at `1e6 pN/nm` cell B's residual is still **`+2.2e−4`**. **`CH-0259`'s straddle was a comparison between two different objects** — a per-bond through-thickness link against a threshold bisected on a **uniform** scalar, where a uniform `834.060958` puts all 435 bonds above the shear ceiling and the resolved lattice puts 135 exactly on it (`CH-0264`). **The radial axis carries the first MEASURED term either axis of this problem has had**: the duplex pair's own `V″(d)` over the `21 bp` of interface one crossover owns is **`205.009678 pN/nm`** from `MengMagnesium`'s osmotic-stress law at a separation **above** that fit's own data floor — and `C-0205` §1b carried the *same tensor's other eigenvalue* for its sign and quoted it **per unit of force**, so it never evaluated it; evaluated it is **`−21.429583 pN/nm`** and would lower `C-0205`'s own ceiling by **`1.09182329×`**. **And the pair supplies a cross-check nobody asked for**: it pushes **`54.3454226 pN`** apart per crossover against `C-0194`'s implied inward bond tension **`29.7795467 pN`**, a ratio of **`1.82492444`** — a 2020 osmotic-stress measurement and a 2014 fitted dihedral spring, sharing no fitted constant, agreeing within a factor of two about what holds a honeycomb crossover at its built separation. **The radial constant is still unsourceable**: eight further EuropePMC queries on the **separation** coordinate, one paper read directly, and the two published distributions of it are unusable for equipartition for stated reasons. **All ten declared falsifiers did NOT fire, and `F5`, `F6`, `F8` and `F9` were declared OPEN**

| | |
|---|---|
| **Task** | [`T-310`](../tasks/T-310-a-bond-link-is-two-mechanisms.md) — raised by [`C-0205`](C-0205-what-link-stiffness-the-recovery-needs.md) (`T-303`) §5 and [`CH-0259`](../challenges/CH-0259-one-scalar-for-two-mechanisms.md) |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (the central-force decomposition, the closed-form `V″(d)` of the corpus's own measured equation of state, and the force cross-check — the whole cheap bound runs with no solver) **+ in-silico** (the same three-dimensional beam-and-bond lattice, now carrying a per-bond link, the same exact Woodbury coupling surrogate and the same `C-0087`-measured incorporation dropout over 4 000 realisations of one common stream as `C-0167`, `C-0180` and `C-0205`) **+ literature** (eight recorded EuropePMC queries on the **separation** coordinate, one paper read directly, `MengMagnesium` and Snodin et al. re-read out of `gpd/data/`) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The radial constant's two connector candidates are `C-0194`'s own attribution and an explicit upper bound; only the **pair** term is measured, and it measures the duplex **pair** rather than the crossover. Nothing here measures a crossover's stiffness on either coordinate, and the recorded search found nothing that does. |
| **Verdict** | **PASS on all six predicates. Of the ten declared falsifiers NONE fired, and `F5`, `F6`, `F8` and `F9` were declared OPEN.** `P1` the resolution reproduces `CH-0259`'s own `475.448622` and `1211.56918` at `6.5e−10` and `1.3e−9`, on the lattice's own `135 / 300` bond census with `⟨unitZ²⟩` asserted at `0.0` and `0.75`; `P2` a radial bracket from two connector routes and one **measured** pair term, every sign stated; `P3` a per-bond link whose `null` default is **bit-identical** to the standing object over every band entry of the recommended block's **4 320**-degree-of-freedom lattice, with all **435** crossover sites asserted beside it; `P4` all 64 cells re-graded at five radial rungs including a control that reproduces `C-0205`'s own census row at `2.6e−9`; `P5` the radial threshold bisected with monotonicity measured first — and reported **per deciding cell**, because cell B's is not monotone and its bisection correctly returns `null`; `P6` route B carried, read out of `C-0201`'s own artifact, with the direction of the unmeasured half stated and challenged (`CH-0265`). |
| **Provenance** | [`gpd/results/T-310-a-bond-link-is-two-mechanisms.json`](../results/T-310-a-bond-link-is-two-mechanisms.json) (`tile.RadialLinkResolutionStudyKt`, **new**); model [`tile/CrossoverLinkResolution.kt`](../../src/main/kotlin/tile/CrossoverLinkResolution.kt) (**new file**); harness [`tools/T-310-mutation-test.py`](../../tools/T-310-mutation-test.py) (**new file**, declared in `tools/P-31-harness-census.py` and wired as `testRadialLinkResolutionMutations`). **One shared source is edited and the edit is a PARAMETER plus two accessors**: `tile/HoneycombGrillage.kt` gains `radialLinkStiffness: Double? = null`, `linkStiffnessAt`, two `linkStiffnessOf` overloads, and four call sites that read them (the bond link, the tie link, `linkEnergy`, `turnLinkOffsetLoad`); `withoutPrestrain`'s constructor call is rewritten from **positional to named** arguments because it gained a parameter and `CLAUDE.md` records what a positional list of same-typed arguments costs. **The default is bit-identical rather than nearly so, by construction**: `linkStiffnessAt` returns `linkStiffness` by **identity** when the new parameter is unset, because `unitY² + unitZ²` is not exactly one in floating point, and `linkEnergy` is branched for the same reason — `0.5·k·Σx²` and `0.5·Σ k x²` are not the same `Double` and `C-0194` §2 quotes `6.7528608` out of that accessor. **Thirty-one named tests written first and watched fail** — [`tile/CrossoverLinkResolutionTest.kt`](../../src/test/kotlin/tile/CrossoverLinkResolutionTest.kt), which did not compile against a model that did not exist — of which **one failed on its first real run and it was the author's mistake, not the code's** (§9) — and **mutation-tested afterwards**, `tools/T-310-mutation-test.py`, **16 mutations, 0 survivors over a subtracted baseline of 0**, after a first run whose **three** survivors were three real test gaps (§10a). Two anchors of **other tasks' mutation harnesses** were orphaned by the edit and repaired in the same commit (`tools/T-297-mutation-test.py` `M11`, `tools/T-299-mutation-test.py` `M12`); `tools/P-31-harness-census.py --check` reports **0 unresolved of 368 anchors and 33 symbols over 21 harnesses, 21 of 21 wired**. Result file **byte-identical across two independent JVM runs**. `tools/verify.sh` on the final sources: **BUILD SUCCESSFUL in 25m 4s, exit 0**, and separately a full Kotlin suite in an owned snapshot, **BUILD SUCCESSFUL in 22m 29s** (`verify.sh` deletes its snapshot on exit, so no test **count** travels with it — `CLAUDE.md`). Every document gate clean — `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-entry-points.py`, `check-queue-vocabulary.py` (all three arms), `check-kotlin-format-strings.py`, `check-cold-start-note.py`, `check-result-path-references.py`, `check-result-file-hygiene.py` (all four arms), `trace-answers.py` on **both** deliverables (its own adjudicated-challenge arm included), `P-31-harness-census.py --check`, `T-295-mutation-input-census.py --check`, `T-272-emit-result-inputs.py --check`, `T-278-emitter-rounding-census.py --check` and `result-reader-census.py --check`. **`gpd/results/P-22-result-reader-census.json` is re-emitted, additively** (140 → 141 studies, 164 → 167 direct edges) and `structure/ResultInputs.kt` is regenerated — the hand-added handle was correct and differed only in **line wrapping**, all 187 handles identical, which is `CLAUDE.md`'s own recorded shape. Sources and recorded queries in [`gpd/data/T-310-sources/`](../data/T-310-sources/README.md). |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.141947 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); `r_P` = 0.908637858 nm (`T-71`, measured on 13 084 crystallographic linkages); `g = d − 2r_P` = 0.718724283 nm; relaxed C2′-endo step 0.66448058 nm (`T-71`); in-plane row pitch `3d/2`, layer pitch `d√3/2`, rise 0.34 nm/bp. `k_θ` = 13.5294118 pN·nm/rad and `k_s` = 64.7058824 pN/nm at `α = 1`; `S` = 1100 pN. **Transverse constant pinned at `C-0205`'s ceiling, 254.808095 pN/nm, throughout.** Pair law `Π_R` = 201.8e3 pN/nm², `λ` = 0.24 nm, data floor 2.45 nm (Meng et al. 2020, 20 mM MgCl₂), over `21 bp` = 7.14 nm of interface per crossover. Cross-section `10 × 6`, block extent **116 bp = 39.44 nm**, raster `102 / 109` (`C-0151`, drawable), 435 staple bonds and **59 raster turn ties at `s = ±L/2`** with zero prestrain. `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing face only; §3's 100 pN over the face; `C-0017`'s mandate at the **acceptable** clause; `C-0058`'s rim-graded 5:1 at a 6.7 nm band and its equal-spring twin; `C-0087`'s measured depth incorporation; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Radial ladder **254.808095, 548.995464, 754.005141, 1530.48954, 1735.49922 pN/nm**; bisection bracket **10 to 1 000 000 pN/nm of RADIAL stiffness**, 16 iterations. |
| **Consumes** | [`C-0205`](C-0205-what-link-stiffness-the-recovery-needs.md) (`T-303`) — the shear ceiling, the two uniform thresholds and the census row at the ceiling, **all reproduced**, worst `2.6e−9`; [`C-0194`](C-0194-the-common-mode-is-the-link.md) (`T-297`) — the link's coordinate, the implied bond tension and the implied step stiffness, **reproduced**; [`C-0201`](C-0201-the-tether-is-a-load-not-a-spring.md) (`T-299`) — route B's own link sweep, **read and cited**; [`C-0207`](C-0207-the-uniform-raster-is-flat-with-its-tethers.md) — route B's uniform raster, read; [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md), [`C-0175`](C-0175-drawable-raster-rim.md), [`C-0154`](C-0154-honeycomb-grillage.md), [`C-0151`](C-0151-closing-raster-selection.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0104`](C-0104-row-end-prestrain.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0001`](C-0001-layer-stiffness.md); `T-71`'s measured backbone; `T-139`'s measured Mg²⁺ equation of state |
| **Constrains** | **[`CH-0259`](../challenges/CH-0259-one-scalar-for-two-mechanisms.md) is UPHELD IN ITS PREMISE and REFUTED IN ITS CONSEQUENCE.** Its premise — that the file resolves a tether's two mechanisms by direction and a bond's by nothing, and that the ceiling is a *shear* ceiling exact only in plane — is upheld and is now built. Its consequence — *"the question is decided by an axial mechanism nobody has priced"* — is refuted: **the census does not move**, and the reason it does not is the mechanism the challenge set aside. **Two challenges are raised.** [`CH-0264`](../challenges/CH-0264-a-per-bond-value-against-a-uniform-threshold.md) against `CH-0259`'s and `C-0205` §5's straddle argument, which compares a per-bond value against a threshold bisected on a uniform scalar. [`CH-0265`](../challenges/CH-0265-756-of-756-is-a-reading-at-the-penalty.md) against `C-0207`'s `756 of 756`, which is a reading at `k_link = 1e4 pN/nm`, `39.2452209×` (`10000 / 254.808095`) above every rung a crossover connector can supply. **No number of `C-0154`, `C-0167`, `C-0175`, `C-0180`, `C-0187`, `C-0194`, `C-0201`, `C-0205` or `C-0207` moves** — the default lattice is bit-identical over every band entry, and every reproduction closes at `2.6e−9` or better. **Two tasks are opened**: `T-315`, route B graded at the resolved per-bond link; `T-316`, a distribution **searched** at the resolved link against the `0.198 %` the tightest cell now misses by. |

---

## 1. The cheap bound is the whole of the first deliverable, and it needs no solver

`W` is the deflection **normal to the face**, so a relative `W` displacement of two crossover-bonded
duplexes decomposes on their own line of centres. That is the central-force decomposition
`K = V″(r) n̂n̂ᵀ + (V′(r)/r)(I − n̂n̂ᵀ)`, projected onto the link's own gradient direction `z`:

&nbsp;&nbsp;&nbsp;&nbsp;`k_link(bond) = k_radial·unitZ² + k_transverse·unitY²`,

which is the expression `HoneycombTetherElement.normalStiffness` has carried for a **chain** since
`T-299`, on a **bond**.

| bond direction | bonds | `⟨unitZ²⟩` | the resolved link |
|---|---|---|---|
| in plane | **135** | `0.0` | `C-0205`'s shear ceiling **`254.808095`**, at every radial constant, **exactly** |
| through the thickness | **300** | **`0.75`** | **`629.20588`** to **`1365.32644`** |

### 1a. The radial axis has a MEASURED term, and the transverse one has none

| route | premise | measured? | pN/nm |
|---|---|---|---|
| the connector at `C-0194`'s implied phosphodiester-step stiffness | `T = 2k_θ/r_P` over the `0.0542437 nm` the built span stands above `T-71`'s measured C2′-endo step (`C-0194` §4's own figure, and `T-297`'s) | constructed | **`548.995464`** |
| the connector at the duplex stretch modulus over the span | `RIGID_LINK_STIFFNESS`'s own KDoc prices `1e4` against `S`; a phosphodiester connector is not a duplex | constructed, an **upper bound** | **`1530.48954`** |
| the duplex **pair**'s own repulsion, `V″(d)`, over one crossover's `21 bp` | `Π(d) = Π_R e^(−d/λ)`, `f_∥ = Π d/√3` exact for a hexagonal array, `d = 2.536 nm` **above** the fit's own `2.45 nm` floor | **MEASURED** | **`205.009678`** |
| **the bracket**, the connector candidates in parallel with the pair | | | **`754.005141`** to **`1735.49922`** |

`C-0205` §1b carried the **other** eigenvalue of that same tensor — the transverse one, `V′(d)/d` —
for its **sign**, and quoted it *per unit of repulsive force per unit length*: its `−2.81545741` is
`−L/d` and carries no force at all. **Evaluated at the measured law it is `−21.429583 pN/nm`**,
smaller in magnitude than the radial term by `9.56666667×` and of the opposite sign, and adding it would lower
`C-0205`'s own shear ceiling to `233.378512 pN/nm`, `1.09182329×`. The ceiling stands here as the
**generous** reading and is not moved.

### 1b. And the pair supplies a cross-check nobody asked for

The pair pushes **`54.3454226 pN`** apart per crossover; `C-0194`'s implied bond tension pulls
**`29.7795467 pN`** together. The ratio is **`1.82492444`**.

A 2020 osmotic-stress measurement and a 2014 fitted dihedral spring read through `CH-0242`'s
attribution share **no fitted constant**, and they agree within a factor of two about the force
that holds a honeycomb crossover at its built separation. **`F8` was declared OPEN — that they
disagree by more than an order of magnitude — and it did not fire.**

## 2. The census, at the resolved per-bond link

All 64 of `C-0167`/`C-0180`'s coupled cells — 4 placements × 4 column counts × 2 distributions ×
2 composite fractions — at 4 000 realisations of the same common stream:

| `k_radial` [pN/nm] | what it is | through-thickness link | flat at `p90` | tightest `p90` |
|---|---|---|---|---|
| **`254.808095`** | the **control**: radial = transverse = `C-0205`'s ceiling | `254.808095` | **0 of 64** | **`0.100581834`** |
| `548.995464` | `CH-0259`'s own low candidate, connector alone | `475.448622` | **0 of 64** | `0.100428875` |
| **`754.005141`** | the bracket **floor** — that connector plus the measured pair term | **`629.20588`** | **0 of 64** | **`0.100198485`** |
| `1530.48954` | `CH-0259`'s own high candidate, connector alone | `1211.56918` | **0 of 64** | `0.100209344` |
| **`1735.49922`** | the bracket **ceiling** | **`1365.32644`** | **0 of 64** | `0.100210806` |

The top row is a **control**, not a rung: at radial = transverse the resolution is the uniform
lattice, and it returns `C-0205`'s own census row — `0 of 64` and a tightest `p90` of
`0.100581834` — at a departure of **`2.6e−9`**, through a code path `C-0205` did not have.

**`C-0205`'s `0 of 64` stands.** `F5` was declared OPEN — that the resolved census recovers cells
the uniform one refuses — and it **did not fire**.

The tightest cell in the whole table is at the bracket **floor** and misses `T-5b` by
**`0.198 %`** — `(0.100198485 − 0.10)/0.10`, a quotient of the file's own tightest `p90` and `T-5b`'s tolerance. That is the closest this programme's coupled recovery has ever come at a link
stiffness anything can supply, and it is the number `T-316` is written on.

## 3. The quantity the corpus needs is the RADIAL constant, and the answer is outside the bracket

With the transverse constant pinned at `C-0205`'s own ceiling and the radial one bisected on
`log₁₀` over `[10, 1e6] pN/nm`, 16 iterations:

| cell | radial threshold `k*` | bracket ceiling / `k*` | verdict |
|---|---|---|---|
| A, 30 paths, abstract grid | **`4581.61268 pN/nm`** | `0.378796582` | the bracket is short by **`2.63993935×`** |
| B, 50 paths, on the rooting helices | **none, at any radial stiffness** | — | residual still **`+2.2e−4`** at `1e6 pN/nm` |

The two ratios in that table are **derived here** and their construction travels with them:
`0.378796582` is the file's own `thresholds/bracketCeilingOverThreshold`, i.e.
`1735.49922 / 4581.61268`, and `2.63993935` is its **reciprocal**, `4581.61268 / 1735.49922` —
both arguments being numbers the file states.

Cell B is the finding. Its residual is positive at **both** ends of a five-decade bracket and its
`p90` has an interior **minimum** — `0.100198485` at `754.005141` — so there is no radial constant,
physical or not, that recovers it. **The 135 in-plane bonds, pinned at the shear ceiling because
`unitZ = 0` there, forbid the recovery by themselves.**

That is exactly the mechanism `CH-0259` set aside as *"exact here"* and therefore not the deciding
one. It is the deciding one.

## 4. Why `CH-0259`'s straddle was never evidence — `CH-0264`

`CH-0259` observes that the resolved through-thickness link, `475.448622`–`1211.56918`, contains
`C-0205`'s two thresholds `834.060958` and `607.396049`, and concludes that the question *"is
decided by an axial mechanism nobody has priced"*.

Those two numbers are not on the same axis. `C-0205`'s thresholds are bisected on a **uniform**
`k_link`: at `834.060958` **all 435** bonds are at `834.060958`. The resolved lattice at a radial
constant of `754.005141` puts **300** bonds at `629.20588` and **135** at `254.808095`, which is a
much softer object — and the measured radial threshold, taken on the resolved lattice, is
`4581.61268`, **`5.49313888×`** cell A's uniform one — a quotient of two numbers the two files
state, `4581.61268 / 834.060958`, and stated here as a quotient because it appears in neither.

**A per-bond value cannot be compared against a threshold bisected on a uniform scalar.** The
resolution that exposes the comparison is the same resolution that makes the comparison
inadmissible. `CH-0264`.

## 5. Route B is carried, not resolved

`C-0201`'s committed result file carries its own `linkStiffness` block: **`0 of 16`** tethered
readings are flat at the 90th percentile, over four decades and four cells, and the `p90` **rises**
as the link softens at every one. So the link stiffness decides everything on route A and nothing
on route B, and both routes agree on the count.

`C-0207` then found route B's own **uniform** raster flat at `756 of 756` — and that is a reading
at `k_link = 1e4 pN/nm`, `39.2452209×` (`10000 / 254.808095`) above every rung a crossover
connector can supply, on a lattice whose 435 **staple** bonds carry the same link this task has
just resolved. Its direction
is known and adverse. It is **not** re-graded here, because that is a study and not a footnote:
`CH-0265` and `T-315`.

## 6. The literature: the number does not exist on this coordinate either

`gpd/` was checked first and it paid twice — `T-137`'s Snodin and Bai passages, and `T-139`'s
measured Mg²⁺ equation of state, which is where the one measured term comes from.

Eight further EuropePMC queries, recorded in
[`gpd/data/T-310-sources/`](../data/T-310-sources/README.md) with their hit counts, name the
**separation** rather than the shear. Yoo & Aksimentiev (*PNAS* **110**:20101, `PMC3864285`) was
**read directly**: it plots the distribution of local inter-DNA distances and of the interhelical
distances at the junction, and the elastic constants it **fits** are the **bundle**'s bending and
twist moduli. No `σ` in the text and no spring constant anywhere — and the distribution it does
plot runs `18–30 Å`, which is the **deterministic weave**, so `k_BT/σ²` taken on it would measure
the sawtooth rather than the junction (`CLAUDE.md`: *a deterministic pattern whose phase you know
is not a tolerance*). Snodin et al. measure the right coordinate at the right place and report it
in **words**: *"the fluctuations, which are smallest at the junctions … are significantly smaller
in magnitude than the variation in the interhelical distance due to the weave pattern itself"*.

**So `T-310` closes the way `T-303` did — a bracket and a threshold instead of a value — with one
term of the bracket now measured.**

## 7. The five verification gates

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | the resolution asserted homogeneous of degree one in both constants at three scale factors; the pair radial term asserted linear in the amplitude and in the contact length; every stiffness pN/nm, the pair law pN/nm² |
| **2 — limiting cases** | the resolution is **exactly** the transverse constant at `unitZ = 0` and **exactly** the radial one at `unitY = 0`; equal constants return that constant on a unit vector; a non-positive constant is refused; the pair radial term is **exactly zero** at `d = λ` and positive above it; a non-positive `radialLinkStiffness` is refused by the lattice |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the free per-bond lattice dishes exactly zero**, re-taken because a per-bond link moves every entry of the matrix; the pair radial term asserted equal to a central difference of the pair's **own** force law and the transverse one equal to `C-0205`'s own `centralPairForceTransverseStiffness` at the measured force; the default lattice asserted **bit-identical** over every band entry of a **4 320**-degree-of-freedom lattice **and** on all **435** of its crossover sites, because a load vector cannot show a lattice difference (`CLAUDE.md`) |
| **4 — numerical convergence** | the deciding quantity is the bisected **radial** threshold and the deciding cell is the one that has one; beam subdivisions `1 → 2` move it **`0.023`** relative (`4557.12988 → 4660.76313`) against `T-303`'s 21 % on the uniform axis, and the **verdict does not move** at either refinement because both readings are `2.6×` above the bracket ceiling; the same axis on the `p90` at the radial floor moves `9.5e−5`, and the dishing grid `81 → 161` moves **`0.0`**. Monotonicity is measured **before** the bisection and reported **per deciding cell**: 10 of 256 consecutive pairs of the census rise, worst `3.4e-05`; cell A is monotone and cell B is **not**, which is why cell B's bisection returns `null` rather than a number |
| **5 — literature and upstream** | `r_P` and the phosphodiester step from `T-71`'s own 13 084-linkage measurement; `d` from Fischer et al.'s SAXS; `k_θ`, `k_s` and `S` from Chen et al. and CanDo; the pair law from Meng et al.'s osmotic stress; **eleven reproductions, worst `2.6e−9`**, including `CH-0259`'s own two published readings, `C-0205`'s ceiling and both of its uniform thresholds read back out of its committed file at exactly `0.0`, and its census row at its own ceiling at `2.6e−9`; and the recorded negative existence result of §6 |

## 8. What the per-bond link cost the shared lattice, and why the default is bit-identical

`radialLinkStiffness` is a **nullable** constructor argument and `linkStiffnessAt` returns
`linkStiffness` by **identity** when it is unset. That is not fastidiousness: `unitY² + unitZ²`
is not exactly one at an in-plane bond of this block, so a lattice that always evaluated the
resolution would differ from the standing object in the last ulp of every link entry, and no
comparison against `C-0167`, `C-0180`, `C-0194`, `C-0201`, `C-0205` or `C-0207` would be
admissible. `linkEnergy` is branched for the same reason — `0.5·k·Σx²` and `0.5·Σ k x²` are not
the same `Double`, and `C-0194` §2 quotes `6.7528608` out of it.

The identity is **asserted**, over every band entry of the recommended block's **4 320**-degree-of-freedom lattice with `==`,
and the crossover **site set** is asserted beside it, because `CLAUDE.md` records that a
load-vector identity is not a lattice identity.

## 9. What failed on its first run, and it was the author

One of the thirty-one tests failed the first time it was run for real, and the code was right.
`F4`'s bond census was written against the recommended block at `rowBasePairs = 102` — the
**raster's** own row length — where every study of this block builds it at the **block extent**,
`116 bp`. At 102 the lattice carries **385** bonds and not 435, because `nodeS` steps the crossover
planes with the row. The test asserts a **census**, so it was asserting one of a different tile.
`CLAUDE.md`'s own *quote it with the state it is read at*, met on a constructor argument, and the
declared falsifier is what caught it.

## 10. The mutation table

Sixteen mutations over the thirty-one tests, applied after they were green, against a
**subtracted baseline** (`CH-0237`), each replacing a rule **wholesale** (`C-0176`), with each
subject asserted to occur at exactly one path (`C-0190`) and every anchor exactly once (`C-0185`).

**Seven of the sixteen mutate `HoneycombGrillage` itself**, which is the point: a mutation of the
new per-bond branch is the only evidence that the branch is load-bearing and that the `null`
default really is the object four claims measured.

| # | what it breaks |
|---|---|
| `M01` | the resolution exchanges its two directions, so an in-plane bond takes the radial constant |
| `M02` | a non-positive radial constant is admitted by the resolution |
| `M03` | a negative transverse constant is admitted |
| `M04` | the pair's radial term takes the sign of the force derivative rather than minus it |
| `M05` | the hexagonal array-to-pair conversion `1/√3` is dropped |
| `M06` | the contact length is dropped, so the term is a stiffness per unit length wearing a stiffness's units |
| `M07` | the implied step stiffness is read over the whole span instead of over the extension |
| `M08` | the measured pair term stops entering the radial bracket |
| `M09` | the resolved builder ignores its own radial argument |
| `M10` | the **lattice** resolves the link on the wrong axis |
| `M11` | the **default** stops returning the scalar by identity, so a default lattice is no longer bit-identical |
| `M12` | the assembled **bond** link ignores the resolution |
| `M13` | the assembled **tie** link ignores the resolution |
| `M14` | the link energy stops being the per-bond sum |
| `M15` | the lattice admits a non-positive radial constant |
| `M16` | `withoutPrestrain` drops the radial constant, so every influence bank is taken on a different lattice from the free field (`C-0104`) |

**Second run: 16 mutations, 0 survivors, over a subtracted baseline of 0.**

### 10a. Three survived the FIRST run, and all three were real gaps

`C-0161`'s rule is that such a mutation is **the finding**, and here it was three times over.

- **`M08`** — the bracket's own composition, `floor = connectorAtImpliedStep + pairRadial`, was
  asserted **nowhere**: every test read the terms and none read the sum.
- **`M09`** — `honeycombTiedLatticeAtResolvedLink` was asserted only at its `null` default, which
  is the case that must be bit-identical and not the case the study runs on.
- **`M13`** — the assembled **tie** link was asserted by **nothing at all**. This is the one that
  needed a **constructed** state rather than a sharper assertion: every point-load test runs on a
  lattice whose ties share a node with a bond, so no solved field can separate the two elements.
  The discriminating test picks a tie whose `(node, beam pair)` **no bond shares** and reads the
  `(W_lower, W_upper)` entry of the assembled matrix, which the link gradient
  `(1, armY, −1, armY)` makes exactly minus that tie's own link stiffness.

`M13`'s first attempt then failed for a second reason worth recording: `stiffnessEntry(i, j)`
reads the **lower triangle only** and returns `0.0` above the diagonal, so an entry read at
`(lower, upper)` is silently zero and asserts nothing (`CLAUDE.md`).

## 11. Validity range

- **Every route to the radial constant is a bound or a construction except one.** The connector
  candidates are `C-0194`'s own attribution and an explicit upper bound; only the pair term is
  measured, and it is a **term** of the constant rather than the constant.
- **The pair term is attributed over `21 bp` of interface per crossover**, which is the honeycomb's
  own crossover period. The repulsion is continuous along the interface and the lattice puts it at
  one node; that is the convention `C-0205` §1b used for the transverse half and it is a
  convention.
- **`MengMagnesium`'s fit is at 20 mM MgCl₂** and this device's buffer is 2 mM. The short-range
  repulsion its `0.24 nm` decay length describes is hydration rather than electrostatic, which is
  why the corpus uses it across buffers, but the amplitude is not measured at 2 mM.
- **The transverse constant is pinned at `C-0205`'s ceiling throughout**, which is its *generous*
  reading — the measured pair term would lower it by `1.09182329×`, and the in-plane bonds are what
  decide cell B.
- **The census is taken on route A**, whose raster turns carry **zero** unpaired nucleotides
  (`C-0175`'s modelling choice). `C-0193` and `C-0200` establish that the only folded block of this
  cross-section does otherwise, so the whole threshold is a statement about a design nobody has
  folded.
- **The threshold is a property of one distribution rule, one placement family, one cross-section,
  one raster and one load case**, exactly as `C-0205`'s is. A distribution **searched** at the
  resolved link is not graded (`T-316`).
- **`0 of 64` is a count at `T-5b`'s `0.10`**, and the tightest cell misses it by `0.198 %`.

## 12. Open questions

- What an oxDNA or all-atom measurement of a crossover's stiffness against a change of the
  interhelical **separation** would give. §6: the two published distributions of that coordinate
  are unusable for equipartition, one because it is dominated by a deterministic pattern and the
  other because it is quoted in words.
- What a resolved per-bond link does to `C-0207`'s `756 of 756`. `CH-0265`, `T-315`.
- What **transverse** constant the recovery would need, with the radial one at its own ceiling —
  the inverse bisection, which §3 says is where the answer now lives.
- Whether the pair term belongs on the **link** at all or as its own distributed element. It acts
  continuously along the interface and the link acts at a node; a distributed separation spring is
  a different element with a different influence function.
- Whether a distribution **searched** at the resolved per-bond link closes the `0.198 %`. `T-316`.
