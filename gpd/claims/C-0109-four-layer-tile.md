# C-0109 — the tile §3 actually specifies: a four-layer, ~10 nm body

> **Annotated, iteration 34 ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **The cross-section this claim solves is not a honeycomb.**
> A honeycomb of bond length `d` has an in-plane row pitch `3d/2` and a layer pitch `d√3/2`,
> so `edgeY = rasterRows × d` is exactly **1.5×** too small at every `m`.
> Re-solved on the corrected geometry the `15 × 4` free tile dishes **0.0978155002** where this claim reads 0.0577199433,
> and its `f*` moves **inside** the measured 0.26–0.33 band.
> **The four-layer verdict survives on `10 × 6` and not on `15 × 4`.**
> This claim's numbers reproduce at departure `0.0` **at the geometry they were solved on**; what moved is the geometry.
> `T-232` re-grades the coupled cells and [`C-0140`](C-0140-honeycomb-raster-turn-sense.md) moves the row length as well.

| | |
|---|---|
| **Task** | [`T-191`](../tasks/T-191-four-layer-tile.md) |
| **Leaf** | `A8.2` |
| **Verification type** | in-silico (beam-and-hinge grillage + orthotropic plate) + logical (lattice and scaffold arithmetic) + literature |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-191-four-layer-tile.json`, produced by `tile.FourLayerTileStudyKt`; model `src/main/kotlin/tile/FourLayerTile.kt`, tests `src/test/kotlin/tile/FourLayerTileTest.kt` |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation `k_f` = 0.012625625 pN/nm³; 15 duplex rows; `C-0017`'s 33.3333 pN/nm as a **sum** at the acceptable 3 nm stroke; dishing peak on an 81 × 81 grid over the free-tile stroke; flat means below `T-5b`'s **0.10 convention** |
| **Consumes** | `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`), and quotes `gpd/results/T-155-dropout-robust-placement.json` (`C-0089`) and `gpd/results/T-151-scaffold-routing.json` (`C-0086`) for reproduction only |
| **Raises** | [`CH-0124`](../challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md) against `C-0006`, [`CH-0125`](../challenges/CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md) against `C-0093` |

> **Verdict** — **PASS on the acceptance predicate, and the flatness negative does NOT survive the tile §3 specifies.** The reason is not that a coupling works on the thicker tile: it is that **the thicker tile does not need one to stay flat.** At the interlayer coupling four measured origami bundles support (`f = 0.30`), the four-layer honeycomb tile at `C-0086`'s buildable 38.08 nm dishes **0.0577199433** of the stroke under `C-0022`'s solved collar **with no attachment coupling at all**, inside `T-5b`'s 0.10, against the single-layer tile's **0.307902368**. The cheap bound predicted it before any lattice was assembled and needs only a fourth root: `D_∥` goes 85.502 → **4 547.17603** pN·nm and `D_⊥` 3.34504758 → **240.931249**, so `C-0058`'s reach goes 12.8290845 → **34.6447329** nm along the helices and 5.70561353 → **16.6216854** across, and `C-0089`'s run-robustness demand falls from **13 columns / 195 paths to 5 / 75** — **4 to 9 columns over the entire `INDEPENDENT`…`COMPOSITE` bracket**, so the direction commits to nothing about the coupling. **One circular M13 pays for exactly four layers and not five**: 15 × 112 bp × 4 = **6 720** of **7 249** nt, 92.7 %, where five layers are 1 151 nt over — `C-0086`'s 4.31× scaffold excess is spent almost exactly by the thickness §3 states, which is NDI's own arithmetic. **What survives, narrowed 4.6×, is a statement about the COUPLING, not about the tile**: adding an equal-spring array still helps at zero defects (0.0401618784 at 90 paths, every graded cell flat) and **costs** under `C-0087`'s measured dropout, whose best four-layer 90th percentile is **0.116465044** — **1.16×** the convention against the single layer's **0.532748246**, at an exceedance of **23.07 %** against **100.00 %**. That is `CLAUDE.md`'s own *"an attachment coupling can be a NET DISHING SOURCE"* read on a tile that no longer needs the correction — and it is inside what `C-0089`'s distribution axis (1.30–1.61×) and `C-0093`'s topology axis (2.22×) are already known to buy, neither of which is searched here. At the bracket's `COMPOSITE` end the coupled cells clear outright (**0.0853679905** at 15 paths, **0.0920173317** at 90). **Eight upstream numbers reproduce**, worst departure **7.2e−5**, including `C-0089`'s own 0.6142 and 0.532748246 and `C-0063`'s 0.3079 — so the harness is the one that produced the negative and the comparison is licensed. Raises [`CH-0124`](../challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md) and [`CH-0125`](../challenges/CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md).

## 1. What §3 says, and what this repository actually models

§3's parameter row reads **"Tile thickness ~10 nm (single-layer honeycomb)"**.
Those two clauses cannot both hold — one layer of 2 nm duplexes at the honeycomb 2.536 nm spacing is 2 nm thick — and **this repository carries both readings, in different packages, and has never reconciled them**:

| where | what it takes the tile to be | flag |
|---|---|---|
| `actuator/ActuatorGeometry.kt:66` | `val tileThickness: Double = 10.0`, KDoc *"§3 says ~10 nm for a single-layer honeycomb"* | **thick** |
| `electrostatics/DnaOrigamiTile.kt:65` | `val thickness: Double = 10.0`; its KDoc states the contradiction verbatim and resolves it toward the thick tile — `helixCount` = 45.6 duplexes in the `edge × thickness` cross-section | **thick** |
| every structural study | `origamiSheet(INTERHELICAL_SHEET, CROSSOVER_SPACING_SHEET_BP)` — one layer, 2 nm | **thin** |

So `C-0022`'s electrostatic load and `ActuatorGeometry`'s stack were computed for a ~10 nm tile,
and the **flatness** — `C-0006`, `C-0009`, `C-0058`, `C-0063`, `C-0087`, `C-0089`, `C-0093`, `C-0098`, `C-0102` — was computed for a 2 nm one.

Two more statements point at the thick tile.
`C-0086` measures the single-layer sheet at **1 680** of M13's **7 249** nt, a **4.31×** excess.
And NDI's answer to decision 5 (2026-08-18) is *"M13, circular ~7-8K nucleotides … to use exess scaffold, just make the tile thicker. The 1700 nucleotide structure the agent is proposing seems… thin and low stiffness."*

**And the four-layer variant has never been solved anywhere.**
`Gen1Tile.gen1SheetVariants()` builds `four-layer-honeycomb-rigid` and documents it as *"the ~10 nm thickness §3 also states"*;
`structure/TileFlatnessStudy.kt:173` then does `val (_, sheet) = variants.first()` and solves the **single-layer** sheet only.
The four-layer row exists in `T-5b`'s result file as three rigidity numbers and nothing else.

## 2. The cheap bound, which ran before any lattice was assembled

`ℓ = (4D/k_f)^(1/4)` at `k_f` = 0.012625625 pN/nm³ — `C-0058`'s reach, re-derived here and reproducing `C-0089`'s emitted 12.82908 / 5.70561 nm.

| tile | `D_∥` | `D_⊥` | `D_∥/D_⊥` | `ℓ_∥` | `ℓ_⊥` | thickness | columns a run of 3 demands |
|---|---|---|---|---|---|---|---|
| single-layer square lattice (**the flatness negative's own tile**) | 85.5018587 | 3.34504758 | **25.5608** | 12.8290845 | 5.70561353 | 2.000 | **13** at 40.0 nm, 12 at 38.08 |
| four layers, INDEPENDENT (bracket floor) | 362.776025 | 19.2216181 | 18.8735 | 18.4124432 | 8.83383457 | 9.608 | **9** |
| four layers, **CALIBRATED at the measured `f` = 0.30** | **4 547.17603** | **240.931249** | 18.8735 | **34.6447329** | **16.6216854** | 9.608 | **5** |
| four layers, COMPOSITE (bracket ceiling) | 14 310.776 | 758.253721 | 18.8735 | 46.144248 | 22.1388682 | 9.608 | **4** |
| four layers, `C-0006`'s standing `ALONG_HELICES_ONLY` variant | 14 310.776 | 19.2216181 | **744.5** | 46.144248 | 8.83383457 | 9.608 | 4 |

Read the last row against the three above it: it is [`CH-0124`](../challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md).

**The column demand is 5 across the whole measured band 0.26–0.33, and 4–9 across the entire `INDEPENDENT`…`COMPOSITE` bracket, against `C-0089`'s 13.**
So the *direction* of the answer needs no commitment about the interlayer coupling at all.

## 3. The interlayer coupling is the whole width of the answer, and it is MEASURED

`INDEPENDENT` and `COMPOSITE` differ by `1 + S Σy²/(nB)` = **39.448** for four honeycomb layers. No solve narrows that.
Four measured origami bundles do — **two lattices, three laboratories, three techniques** — and they agree:

| bundle | lattice | measured `L_p` | `f` | read |
|---|---|---|---|---|
| Kauert, Kurth, Liedl & Seidel, *Nano Lett.* **11**:5558 (2011), 6-helix | honeycomb | 1 880 nm | **0.302** | abstract only (ACS 403s the article, PDF and SI; Unpaywall `closed`); the persistence lengths quoted from Chhabra et al., arXiv:2006.15029 Table 1, **read directly**, and corroborated to 1 % by Zhang et al. 2022 (PMC9494703), **read directly** |
| Kauert et al., 4-helix | square | 740 nm | **0.288** | same |
| Pfitzner et al., *Angew. Chem.* **52**:7766 (2013), 6-helix | honeycomb | 2 µm | **0.325** | PMC3749440, **read directly** |
| Wang et al., *JACS* **134**:1606 (2012), 6-helix tile | tile-based | 1.0 µm | **0.292** | PMC3267479, **read directly** |

`f = (EI_measured − EI_independent)/(EI_composite − EI_independent)`.
**The fraction is arithmetic performed here; no source reports it** — but each row's ingredients are published measurements, and the reproduction of two of them is an executable test.

Three independent corroborations that the `RIGID` limit is a ~3× over-prediction rather than a bound to be preferred:

- **Wang et al. publish the rigid-composite formula themselves**, name it a *"naïve model"* in which *"the 6HB nanotube is represented by a ring of rigidly linked rods"* with `p_tube/p_helix = N[1 + 2(R/r)²]`, and measure it to over-predict: **2.7 / 4.4 / 5.25 µm estimated against 1.0 / 3.6 / 5.0 measured**.
- **Kauert et al.'s own model reached the same conclusion.** As reported by Li et al. (*Nanoscale* 2023, PMC10395309), they *"studied four different types of boundary conditions between the bundles (fully disconnected …, fully attached …, and two partially attached …). They concluded that the most reasonable conditions were the two partial attachments."* That is exactly this bracket, run by the people who measured it, resolved **in between**.
- **`InterlayerCoupling.RIGID` is CanDo's assumption and CanDo says so**, in the same sentence both of this repository's duplex elastic constants come from: *"bundles of isotropic elastic rods that are **rigidly constrained to their nearest neighbors at specific crossover positions** … stretching (1100 pN), bending (230 pN nm²), and torsional (460 pN nm²)"* (Kim, Kilchherr, Dietz & Bathe, *NAR* **40**:2862, 2012, **read directly**).

Two caveats travel with `f = 0.30`, and they run in **opposite** directions:

- every measured bundle is a **rod whose helices are mutually crossovered around a closed ring**; a 15-wide × 4-deep slab has a different crossover topology and a far larger `Σy²`, and shear lag grows with the lever arm — so `f = 0.30` is plausibly an **upper** bound here;
- every measured `f` is depressed by **fabrication defects** as well as by crossover compliance, and no source separates the two: Chhabra et al. say *"the experimentally measured values should be seen as lower bounds to the persistence lengths of ideal structures"*, and their own defect-free oxDNA simulations land at `f = 0.74`.

Everything fetched is retained in `gpd/data/T-191-sources/` with a manifest and a re-runnable `composite-fraction.py`.
**A citation correction**: Kauert et al. is *Kauert, Kurth, Liedl & Seidel* — Ralf Seidel's group at TU Dresden — not Dietz's; `Gen1Tile.kt`'s KDoc for `DUPLEX_TORSIONAL_PERSISTENCE` attributes 97 ± 4 nm to *"Kauert et al., Nano Lett. 11:5558, 2011, SI"*, and **that SI could not be obtained here**, so the attribution is carried unverified rather than challenged.

## 4. The identity that makes `C-0006`'s variant a mixed state

Chen et al.'s softened-bond construction gives the crossover a hinge spring `k_θ = 2αB/(100a)` and, by
`Gen1Tile.crossoverInPlaneStiffness`, an in-plane spring `k_s = 2αS/(100a)`. **Their ratio is `S/B`** and `α` cancels.
The parallel-axis excess is therefore

- along the helices: `(S/d)Σy² ÷ (nB/d) = S Σy²/(nB)`
- across the helices: `(k_s d/p)Σy² ÷ (n(k_θ/p)d) = k_s Σy²/(n k_θ) = S Σy²/(nB)`

— **the same number**, so a multi-layer sheet's **anisotropy is invariant along the whole coupling axis**: 18.873 at `INDEPENDENT`, at `COMPOSITE` and at every fraction between. Asserted at `1e-12` as gate 3.

`C-0006`'s standing `four-layer-honeycomb-rigid` variant takes the parallel axis **along the helices only** and reads an anisotropy of **744.5**, which is 39.45× a value the model cannot take at any coupling. It is a **mixed state**, not a bracket end — [`CH-0124`](../challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md) — and its one downstream consumer is `C-0093`'s brick — [`CH-0125`](../challenges/CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md).

A second, independent defect of the same variant: `OrigamiSheet.layerSpacing` defaults to `interhelicalDistance`, so it stacks its rows **2.536 nm** apart where a honeycomb array stacks them at `d√3/2` = **2.196 nm** — overstating `Σy²` by exactly `4/3`, the parallel-axis factor **39.448 against 29.836** and the thickness **9.608 against 8.589 nm**. At the measured `f` the corrected reading is `D_∥` = 3 501.08 against 4 547.18 and `ℓ_∥` = 32.45 against 34.64 nm, and **the column demand is 5 either way**.
## 5. The solve: the free tile, and what a coupling does to it

`C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V, `C-0001`'s secant foundation, 15 duplex rows,
`C-0017`'s 33.3333 pN/nm shared **equally** over the paths, dishing as a peak on an 81 × 81 grid over the free-tile stroke.
Five tiles graded, six cells each (no coupling plus five column counts), 10 000 dropout realisations per cell at `C-0087`'s own seed.

### 5.1 The uncoupled tile — the reference `CLAUDE.md` insists on

| tile | free-tile dishing | flat at `T-5b`'s 0.10? |
|---|---|---|
| single-layer square lattice, 40.0 nm (the flatness negative's own tile) | **0.307902368** | no — 3.08× |
| single-layer square lattice, `C-0086`'s 38.08 nm | 0.299397543 | no — 2.99× |
| four layers, `INDEPENDENT` (bracket floor) | 0.18273857 | no — 1.83× |
| **four layers, CALIBRATED at the measured `f` = 0.30** | **0.0577199433** | **yes** |
| four layers, `COMPOSITE` (bracket ceiling) | 0.0382012715 | **yes** |
| four layers, `f` = 0.26 (measured band floor, not graded) | 0.0612595738 | **yes** |
| four layers, `f` = 0.33 (measured band ceiling, not graded) | 0.0555188286 | **yes** |
| four layers at the **true honeycomb layer spacing**, `f` = 0.30 (not graded) | 0.0651203605 | **yes** |
| four layers, `C-0006`'s `ALONG_HELICES_ONLY` mixed state (not graded) | 0.160153834 | no |

**The whole measured band is flat, uncoupled**, and so is the geometrically corrected reading; only the bracket *floor* — layers that do not share load at all — is not.
Note that `C-0006`'s mixed variant reads 0.160153834, i.e. it would have reported the four-layer tile as **not flat**: the defect [`CH-0124`](../challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md) names changes the verdict, not just the number.

### 5.2 The coupled tile, at zero defects and under the measured dropout

Equal springs, `MEASURED_DEPTH` incorporation, 10 000 realisations. The 90th percentile is `C-0087`/`C-0089`'s verdict statistic.

| tile | paths | nominal | **p90** | exceedance | worst single removal ÷ nominal |
|---|---|---|---|---|---|
| single-layer, 40.0 nm | 45 | 0.21821335 | **0.614243977** | 100.00 % | 2.056 |
| single-layer, 40.0 nm | 90 | 0.161116195 | **0.532748246** | 100.00 % | 2.345 |
| four layers, `INDEPENDENT` | 90 | 0.0969663002 | 0.249204267 | 96.84 % | 1.576 |
| **four layers, CALIBRATED** | 15 | 0.0607282891 | 0.128176929 | 25.37 % | 1.788 |
| **four layers, CALIBRATED** | 45 | 0.0415373893 | 0.133066222 | 32.21 % | 2.084 |
| **four layers, CALIBRATED** | 90 | **0.0401618784** | **0.116465044** | **23.07 %** | 1.647 |
| four layers, `COMPOSITE` | 15 | 0.0358187616 | **0.0853679905** | 4.97 % | 1.975 |
| four layers, `COMPOSITE` | 90 | 0.0291934034 | **0.0920173317** | 5.42 % | 1.733 |

Three things to read off it.

- **At zero defects every four-layer cell is flat** — 0.0292 to 0.0607 of the stroke, against the single layer's 0.1522–0.6952.
- **Under the measured dropout the calibrated coupled tile is 1.16–1.55× the convention**, where the single layer is 5.33–8.52×.
  The negative narrows by **4.6×** and does not vanish.
- **The coupling is the thing that fails, not the tile.** The uncoupled calibrated tile reads 0.0577199433 and every coupled cell of the same tile reads worse under dropout.
  That is `CLAUDE.md`'s *"an attachment coupling can be a NET DISHING SOURCE"*, and on a tile that no longer needs a flatness correction it is the whole residual.
  **1.16× is inside what the two axes this study does not search are already known to buy** — `C-0089`'s distribution (1.30–1.61×) and `C-0093`'s topology (2.22×) — so a flat *coupled* four-layer design is likely and is **not claimed here**.

### 5.3 The transfer assumption, made visible

`C-0087`'s incorporation field is a **single-layer** measurement. Under the `UNIFORM` 0.84 convention instead, the same 3 × 15 cells read
**0.525926694 / 0.564236509 / 0.241041944 / 0.113564326 / 0.0917118555** across the five graded tiles, against `MEASURED_DEPTH`'s 0.614243977 / 0.650145584 / 0.283580205 / 0.133066222 / 0.106795572 —
a 6–17 % spread, which is the honest size of the assumption on the axis it is made.
The **direction** of the transfer is adverse: a four-layer station has more redundancy behind it than the measurement it is graded under.
## 6. The scaffold: one circular M13 pays for exactly four layers and not five

`C-0086`'s own construction — one nucleotide of scaffold per base pair per duplex — extended by the layer count.
It is a **count**, and it is lattice-independent: whatever the crossover rule, a duplex of `L` base pairs carries `L` nucleotides of scaffold.

| layers | nt at `C-0086`'s buildable 15 × 112 bp | of M13's 7 249 | overhang |
|---|---|---|---|
| 1 (`C-0086`'s own, reproduced exactly) | 1 680 | 23.2 % | 4.3149× |
| 2 | 3 360 | 46.4 % | 2.1574× |
| 3 | 5 040 | 69.5 % | 1.4383× |
| **4** | **6 720** | **92.7 %** | **1.0787×** |
| 5 | 8 400 | **116 % — 1 151 nt over** | 0.8630× |

At the nominal 118 bp row, four layers are **7 080** of 7 249 — also inside, at 97.7 %.
So `C-0086`'s 4.31× excess is spent almost exactly by the thickness §3 states, and the ceiling is **four layers**: `⌊7249/1680⌋ = 4`.
**The declared falsifier `F4` did not fire.**

**What this does *not* say** is that four honeycomb layers can be **routed**.
`C-0086`'s admissible-width rule — Rothemund's *"the distance between successive scaffold crossovers must be an odd number of half turns"* — is a **square-lattice** statement.
Applied at the honeycomb's 10.5 bp/turn it admits **no integer base-pair row length whatever**: an odd multiple of 5.25 bp is never an integer.
That is a statement about a rule outside its own domain, not a proof of impossibility — honeycomb multilayer origami is routed with seams and a different crossover rule (Douglas et al. 2009) — but it means **the buildable honeycomb raster width is an open question, and this claim answers only the nucleotide count.**

## 7. What does not transfer, stated rather than assumed

1. **The lattice machinery of this repository is single-layer, and square-lattice in its crossover combinatorics.**
   `OrigamiGrillage` reads exactly five things from its `OrigamiSheet` — `interhelicalDistance`, `crossoverSpacing`, `crossoverHingeStiffness`, `duplex.bendingRigidity`, `duplex.torsionalRigidity` — and **never reads `layers` or `interlayerCoupling`**.
   Assembling a grillage on `Gen1Tile`'s `four-layer-honeycomb-rigid` variant would give a lattice **bit-identical** to the single-layer honeycomb one.
   The four-layer body therefore enters here as a **smeared equivalent sheet** whose three plate rigidities equal the multi-layer body's exactly (gate 2, at `1e-12`) and whose internals are a single layer's.
2. **Transverse shear is neglected and should not be.** At 9.608 nm over 38.08 nm the thickness/span ratio is **0.252**, outside Kirchhoff — `C-0006` says so in its own validity range. `D_∥` is an upper bound *again*, on top of the coupling reading.
3. **`CrossoverLayout` carries a two-parity alternation**, which is the square lattice; a honeycomb helix has **three** crossover azimuths at 7 bp. So the `T-10` centred column construction is used and **no phase is swept** — and every placement, phase and plan ceiling in this corpus (`C-0055`, `C-0063`, `C-0072`, `C-0074`, `C-0075`, `C-0090`, `C-0102`) is a **square-lattice single-layer** result that does **not** transfer to a honeycomb top face.
4. **The dropout statistic is a single-layer measurement.** Strauss et al. imaged a plain single-layer Rothemund rectangle; `C-0087`'s incorporation field is a boundary-layer profile in nm, applied here to a body with four times the staples and a different perimeter-to-area ratio. **The direction is adverse** — a four-layer station has more redundancy behind it than the measurement it is graded under — so the four-layer verdict here is a **lower bound** on the four-layer tile's robustness. Both `MEASURED_DEPTH` and `UNIFORM` conventions are carried so the size of the transfer assumption is visible.
5. **The across-helix composite term rests on a construction.** `Gen1Tile.crossoverInPlaneStiffness` is flagged in its own KDoc as *"a construction, not a measurement"* and is swept over four decades elsewhere. The `f = 0.26–0.33` calibration is measured **along** the helices (bundles bend about axes normal to their own); no measurement of a multilayer origami **slab**'s across-helix rigidity exists — 41 EuropePMC queries found none, every measured multilayer object in the literature being rod-like.
6. **A thicker tile is a different body for `C-0022`'s charge and `C-0004`'s drainage**, and neither is re-run here. `electrostatics/DnaOrigamiTile` already defaults to 10 nm, so `C-0022`'s load is arguably the one that was always computed for *this* tile; the gap electrostatics, the Stern series and the stack geometry are not re-derived.
7. **`T-5b`'s 0.10 is a CONVENTION, not a physical threshold.**
8. TRL 1–3. Nothing derived here is measured; the inputs that are measured are named as such.
## 8. The five verification gates

| gate | what was asserted | verdict |
|---|---|---|
| **1 — dimensional consistency** | `D = EI/d` is `pN·nm²/nm = pN·nm`; `ℓ = (4D/k_f)^(1/4)` is `(pN·nm ÷ pN/nm³)^(1/4) = nm`; four honeycomb layers are 9.608 nm thick and one duplex is 2.000 | **PASS** |
| **2 — limiting cases** | one layer has no parallel-axis term at **any** coupling; the `INDEPENDENT` four-layer sheet is **exactly** four single honeycomb layers in all three rigidities; the smeared equivalent sheet reproduces the multi-layer sheet's `D_∥`, `D_⊥`, `D_k` to **`1e-12`** (falsifier `F2`); the `ALONG_HELICES_ONLY` reading reproduces `C-0006`'s published row | **PASS** |
| **3 — symmetry and conservation** | a **uniform load** on the four-layer lattice dishes exactly zero (falsifier `F1`, `CLAUDE.md`'s standing falsifier); the parallel-axis excess is the **same factor** along and across the helices to `1e-12`; therefore the anisotropy is **invariant** along the whole coupling axis | **PASS** |
| **4 — numerical convergence** | beam subdivisions 1/2/4 (nested, per `CLAUDE.md`) on the free-tile dishing; dropout realisations at a quarter, a half and the full count; the reach's exact fourth-root scaling asserted to `1e-12` | **PASS** |
| **5 — literature cross-check** | Kauert et al.'s 6HB honeycomb, Kauert et al.'s 4HB square and Pfitzner et al.'s 6HB each reproduce a composite fraction inside the declared 0.26–0.33 band, as executable tests; the adopted 0.30 lies inside it; the calibrated tile lies strictly between the two limits in both directions | **PASS** |

**20 tests**, every one named for the gate it discharges. Suite: `tools/verify.sh` with two sibling files dropped —
**2 502 tests completed, 4 failed**, and all four belong to concurrent agents' unfinished work
(`TallGapDeviceBTest`, whose main source had to be dropped, and a fresh `ElectrodePotentialOfZeroChargeTest`).
No `FourLayerTileTest` failure.

**Convergence, emitted**: beam subdivisions 1/2/4 move the free-tile dishing by `1.5e−5`;
dropout realisations 2 500/5 000/10 000 move the 90th percentile by `0.0065`.

`gpd/data/T-191-sources/` retains everything fetched, with a manifest and `composite-fraction.py`.

## 9. The falsifiers, declared before the run

| | falsifier | fired? | what it read |
|---|---|---|---|
| **`F1`** | a uniform load on the four-layer lattice dishes more than solver noise | **no** | asserted as a test, `dishingRms < 1e-9 × meanDeflection` |
| **`F2`** | the smeared equivalent sheet does not reproduce the multi-layer rigidities | **no** | asserted at `1e-12` on all three rigidities, at all four couplings |
| **`F3`** | the four-layer 90th percentile stays above 0.10 at every path count | **no, but only at the bracket ceiling** — it fires at the measured `f` = 0.30 (best 0.116465044) and does **not** at `COMPOSITE` (0.0853679905 at 15 paths, 0.0920173317 at 90). And it is a falsifier on the **coupled** cells: the uncoupled calibrated tile reads 0.0577199433 | graded over the density sweep at both incorporation conventions |
| **`F4`** | four honeycomb layers need more than one M13 | **no** | 6 720 nt of 7 249 |
| **`F5`** | the single-layer baseline does not reproduce `C-0089` | **no** — 0.614243977 against its published 0.6142 at 45 paths (`7.2e−5`) and 0.532748246 against `C-0089`'s own 0.532748246 at 90 | the licence for every comparison in this claim |
## 10. What `ANSWERS.md` owes — reported, not edited

1. **Row (g), *"Does the tile stay flat?"*** — the whole row is a **2 nm-tile** result and says so nowhere. It closes with *"the tile can be made flat on paper … and cannot be made flat at the state of the art in DNA-origami folding, and the question is closed on every coupling axis this programme can reach."* That closure stands **for the tile it was derived on**. On the tile §3 specifies it does not: the axis that was never spent is the **body**, not the coupling.
2. **Row 6 of the decisions table** already records NDI's *"YES, BY IMPLICATION"* and names `T-191`; it now has an answer to point at.
3. **Row 5** quotes `C-0086`'s 1 680 of 7 249 nt and NDI's *"just make the tile thicker"*; the arithmetic that closes it — four layers, 6 720 nt, 92.7 % — belongs beside it.
4. **§1's stated open item (ii)** — *"§3 specifies a ~10 nm tile and every structural claim here modelled a 2 nm one"* — is answered.
5. **`C-0093`'s brick number, carried in row (g), row 6 and `DECISIONS-FOR-NDI.md` twice**, is under [`CH-0125`](../challenges/CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md) and should not be re-quoted without it.
6. **Nothing in §2, §5, §6 or the design-window rows moves**, because none of them is a function of the tile's thickness — and that is worth saying, because a reader who learns the tile is five times thicker will assume otherwise. The two that *would* move are `C-0022`'s charge and the stack geometry, and neither is re-derived here.
## 11. Still open

1. **Where between `INDEPENDENT` and `COMPOSITE` a four-layer *slab* sits.** The calibration is measured on **rods** — bundles whose helices are crossovered around a closed ring. A 15-wide × 4-deep slab has a different crossover topology and a far larger `Σy²`. `f = 0.30` is plausibly an upper bound there, and the *uncoupled* verdict turns on it: at `f = 0.30` the tile is flat and at `f = 0` it is not. **The threshold is `f` such that the free-tile dishing crosses 0.10, and it lies between 0.00 and 0.26.** Locating it is one sweep of the same study and is the single cheapest thing a successor can do.
2. **Whether a *coupled* four-layer tile is flat under dropout.** This study grades one distribution (equal) on one placement family (a uniform grid) at one topology (an array). `C-0089` and `C-0093` between them are worth 1.3–2.2× on exactly this statistic and the residual is 1.16×.
3. **Whether four honeycomb layers can be ROUTED from one circular M13 at a buildable width.** The count fits; `C-0086`'s odd-half-turn rule is a square-lattice statement and admits no integer base-pair honeycomb row at all. The honeycomb raster width is unanswered.
4. **What the attachment lattice of a four-layer honeycomb top face is.** Every plan ceiling, phase result and placement in this corpus is single-layer square-lattice.
5. **Transverse shear.** Thickness/span is 0.252; Kirchhoff is not safe there and `D_∥` is an upper bound again.
6. **`C-0022`'s charge and the stack geometry on a 9.6 nm tile.** `electrostatics/DnaOrigamiTile` already defaults to 10 nm — so the *load* is arguably already the thick tile's — but nothing was re-solved, and `ActuatorGeometry`'s ~20–25 nm effort-point band means a 9.608 nm tile leaves **0.4–5.4 nm** of standoff above a 10 nm layer where a 2 nm tile leaves 8–13. That is a consequence, not a result, and it belongs to a successor.
7. **`C-0004`'s drainage** is a footprint problem by its own finding, so the thickness should not touch it — but that is an argument, not a re-run.
