# C-0037 — A truss discharges the stability problem and does NOT cost the draw-in, because the axis the standoff buckles about is orthogonal to the axis it draws in on: the frame couple is a rank-one tensor on the leg offsets, so its azimuth is a pure trade, and the answer is a PARTIALLY triangulated head — two legs laid ACROSS the flexure axis, which is the leg count the literature's only rigid out-of-plane mounting actually has

| | |
|---|---|
| **Task** | [`T-72`](../tasks/T-72-triangulated-standoff.md), **covering [`T-66`](../tasks/T-72-triangulated-standoff.md)**, which it re-scopes |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (a multi-leg frame assembled at the head and solved into `C-0030`'s coupled beam, with Maxwell-Betti asserted on the *assembled* object between two independently integrated off-diagonals) **+ logical** (a frame couple is a rank-one tensor on the leg offsets, so `Σx_i² + Σy_i²` is conserved and the azimuth spends it) **+ literature** (the triangulated motif this programme has been citing, re-fetched, re-read and its strand table counted by hand) |
| **Verdict** | **PASS, and the trade the task was sent to price does not exist in the form it was posed.** `C-0028` and `C-0029` priced a truss as *"a triangulated head cannot sway, and sway is the draw-in"* — but the draw-in lives on `(u_x, φ_y)` and the buckling that closed the branch lives on `(u_y, φ_x)`, and **for a two-leg row of separation `w` at azimuth `θ`, `Σx_i² = (w²/2)cos²θ` and `Σy_i² = (w²/2)sin²θ`, so at `θ = 90°` the loaded plane inherits EXACTLY ZERO frame stiffness.** Laid **across** the flexure axis, two legs take the adopted critical load from **1.46 pN to 9.77 pN (6.71×)**, move the governing plane from the free one to the loaded one — i.e. **restore `C-0029`'s restrained-axis reading, which is precisely what `C-0029` said a truss would have to do** — and cost **23 % of the draw-in supply** (2.90× the demand, against 3.75×) and **3.5 % of the tangent** (26.09 against 25.20 pN/nm, ceiling 40). **The window is `ℓ = 5–10 nm`, all nine predicates, on CanDo's rigidity AND on Fields et al.'s measured one**, where the single standoff fails `P6` at every length (margin 0.40–0.57). Laid **along** the flexure axis the same two legs cost 63 % of the supply, add 31 % to the tangent **and still fail `P6`** — they spend the whole frame budget on the plane that was not failing. **And the cost is not the triangulation at all: `L2a6`, `L2a8` and `L2a12` have identical span, tangent, `Φ` and supply ratio, so the draw-in cost of a cross row is the leg COUNT and not the leg SPACING.** Two results were not anticipated: **`P9`, that an off-square row reacts part of the head moment as an axial couple and overloads its outermost leg — it fails at 12 of the 12 azimuths below 90°, so the exact cross row is uniquely optimal and not merely best**; and, in the literature, that Pumm et al.'s *"set of double-helical spacers"* is **exactly two**, each **39 bp**, each attached by **one covalent link per end** — `C-0029`'s `R3` ball joint — so **the rigidity reported in print belongs to the PAIR and not to either joint. That is a frame couple, and it is this claim's mechanism, already built.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED EITHER** — `C-0028`'s and `C-0029`'s literature finding is unchanged and this claim's own search adds to it: no duplex has been built standing normal to a single-layer sheet, so two of them sharing a cap is a model of a model. |
| **Provenance** | `gpd/results/T-72-triangulated-standoff.json`, produced by `anchoring.TriangulatedStandoffStudyKt`; **9 cheap-bound quantities, 8 layouts, 32 design records, 13 azimuth records, 25 sensitivity records, 10 convergence records, 14 upstream reproductions, 7 literature records**; **26 gate-named tests in `TriangulatedStandoffTest`, 310 in `anchoring`, 1208 in the suite, 0 failures**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with a concurrent agent's half-written `actuator/CollarEquilibriumPathStudy.kt` removed by `--drop-file`; the result file re-run through `tools/study.sh` and reported *"no result file changed"* |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet at the SAXS 2.69 nm; `C-0029`'s realisable two-link base on the **hard 180° chord** (78.24 restrained / 13.53 free); `EI = 230 pN·nm²` (CanDo model input) with **every critical load also on Fields et al.'s implied 172.9** |
| **Consumes** | [`C-0029`](C-0029-perpendicular-junction-routing.md) (`realisablePerpendicularBase`, `maximumBaseRotationalStiffness`, `bondHingeStiffness`, `bondSlideStiffness`, `DuplexBackbone`, the counting theorem), [`C-0030`](C-0030-coupled-standoff-joint.md) (`StandoffTipFlexibility`, `standoffTipFlexibility`, `standoffTipFlexibilityByIntegration`, `CoupledJointFlexure`, `coupledFlexureSpan`, `coupledBucklingStroke`, `peakFlexureCompression`, `bracedColumnBucklingLoad`, `favourableStrokeClearance`, `FIELDS_BENDING_RIGIDITY`, `FlexureOrientation`, `DrawInModel` — **re-run as a library**), [`C-0028`](C-0028-standoff-base-joint.md) (`swayColumnDeterminant`/`standoffBucklingLoad`, `baseRestraintParameter`, `seriesStiffness`, `StandoffBase`), [`C-0025`](C-0025-flexure-end-joint.md) (`c(ρ)`, `g(β)`), [`C-0023`](C-0023-two-sided-coupling.md) (the 40 pN/nm ceiling, the 45 paths), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate and the envelope), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile`, [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (10 / 65 pN), [`CH-0037`](../challenges/CH-0037-the-buckling-duty-is-the-mandate-not-the-element.md) (the duty is the element's own) |
| **Raises** | [`CH-0050`](../challenges/CH-0050-the-truss-cost-was-priced-on-the-wrong-axis.md) against `C-0028` and `C-0029` |

---

## The claim, in one line

**The opposition `C-0028` named and `C-0029` inherited — *"the standoff's sway IS the flexure's draw-in, so a head that cannot sway cannot draw in"* — is an identity about ONE coordinate applied to a failure on ANOTHER: a two-link base leaves the axis orthogonal to the loaded plane free, and that is the axis the column buckles about, so a truss that restrains it need not touch the sway at all; the frame couple `k_a Σd_i²` is a rank-one tensor on the leg offsets, `Σx_i² + Σy_i² = w²/2` identically, and laying two legs ACROSS the flexure axis puts the entire budget on the failing plane and exactly none on the working one — 6.71× in critical load for 23 % of a draw-in supply that had 3.75× of margin — which reopens §3's desired 10 nm stroke on the standoff branch and is, to the leg count, what the only rigid out-of-plane mounting in the published literature already is.**

---

## The two cheap bounds, which ran first and decided the shape of the answer

| | bound | value | what it settled |
|---|---|---|---|
| **1** | the **frame** couple against the **bond** couple the free axis has | `k_a Σd²` = **96.88** pN·nm/rad on the nominal two-link cap (162.76 rigid), against **13.53** — **7.16×** | a truss can restore the free axis at all. Below ~2× it could not, and the task would have closed in a paragraph |
| **2** | the **conservation identity** `Σx_i² + Σy_i² = w²/2` | residual **0.0**, exactly, at every azimuth | the truss has **one** budget of frame couple and the azimuth spends it — so a *partial* triangulation exists, and the two requirements are not on one degree of freedom |

Only because bound 2 says the two planes are **complementary** rather than coupled was the full solve worth running; and only because bound 1 clears the free axis by 7× is there anything to solve. Both are asserted as tests.

---

## The model, which is `C-0030`'s pipeline with one object substituted

`n` legs sharing a rigid cap act **in parallel** in bending, and their **axial** stiffnesses at their offsets add a frame couple to the head's rotation *and to nothing else* — a rigid-body rotation `φ` about the centroid stretches leg `i` by `∓d_i φ` and translates none of them. So, in either plane,

&nbsp;&nbsp;&nbsp;&nbsp;**`K_truss = n·K_leg + [[0, 0], [0, k_frame]]`**, &nbsp;
`k_frame = series(k_a Σd_i², k_tie)`, &nbsp; `k_a = series(S/ℓ, k_z,base)` = **44.0 pN/nm** at 8 nm,

and the inverse is a `StandoffTipFlexibility` — exactly what `CoupledJointFlexure`, `coupledFlexureSpan`, `coupledBucklingStroke` and `peakFlexureCompression` already consume. **Every number below is `C-0030`'s pipeline with the head object replaced**, which is why the reductions are exact and not approximate:

- `n = 1`, `Σd² = 0` returns `standoffTipFlexibility` **entry by entry**;
- `Σd² = 0` at any `n` returns it **divided by `n`**, correlation untouched;
- `k_frame → ∞` sends `C12 → 0` and `C11` to the **rotation-fixed** sway, exactly 4× stiffer at a clamp — *a fully triangulated head supplies no draw-in at all*, which is `C-0028`'s intuition, correct in the limit it applies to.

Buckling is `C-0028`'s own sway determinant one level up. The cap enforces one common head rotation, so per column the external head spring is `k_frame/n` and

&nbsp;&nbsp;&nbsp;&nbsp;**`P_c,plane = n·u²EI/ℓ²`**, &nbsp; `ρ_b = k_θb ℓ/EI`, &nbsp; `ρ_h = (k_frame/n)ℓ/EI`, &nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;**`P_c,truss = min(P_c,loaded, P_c,free)` — a column buckles about its softest axis.**

The head is free to translate in both planes, which is not conservatism but a requirement: the head's translation in the loaded plane **is** the draw-in, and `C-0028` establishes that holding it is `C-0023`'s *ends held axially* reading.

---

## The layout catalogue, at `ℓ = 8 nm` on the nominal two-link cap

45 paths, secant placed at 33.3333 pN/nm at 3 nm by construction, favourable mounting, duty and margin at §3's **desired** 10 nm on the element's own end shear (`CH-0037`).

| id | legs | `Σx²` | `Σy²` | span [nm] | tangent | supply/demand | duty(10) | `P_c` loaded | `P_c` free | **`P_c`** | plane | **margin** | verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `L1` **single** | 1 | 0 | 0 | 31.85 | 25.20 | 3.75 | 2.91 | 4.89 | **1.46** | **1.46** | free | **0.50** | **FAIL `P6`** |
| `L2a6` across, 6 bp | 2 | **0** | 2.081 | 33.43 | 26.09 | 2.90 | 3.50 | 9.77 | 8.84 | 8.84 | free | 2.52 | **PASS** |
| **`L2a8` across, 8 bp** | **2** | **0** | **3.699** | **33.43** | **26.09** | **2.90** | **3.50** | **9.77** | **11.70** | **9.77** | **loaded** | **2.79** | **PASS** |
| `L2a12` across, 12 bp | 2 | **0** | 8.323 | 33.43 | 26.09 | 2.90 | 3.50 | 9.77 | 16.14 | 9.77 | loaded | 2.79 | **PASS** |
| `L2l` **along**, 2.69 nm | 2 | 3.618 | 0 | 36.05 | 33.02 | **1.39** | 5.42 | 22.09 | **2.91** | **2.91** | free | **0.54** | **FAIL `P6`** |
| `L3a` three across | 3 | 0 | 14.80 | 34.26 | 27.54 | 2.35 | 4.17 | 14.66 | 25.50 | 14.66 | loaded | **3.52** | **PASS** |
| `L3t` **triangle** | 3 | 4.824 | 3.699 | 37.21 | 36.20 | **0.95** | 6.78 | 34.06 | 16.39 | 16.39 | free | 2.42 | **FAIL `P8`** |
| `L4` rectangle | 4 | 7.236 | 7.398 | 38.31 | **39.61** | **0.66** | 8.21 | 48.01 | 25.86 | 25.86 | free | 3.15 | **FAIL `P4`** |

Four things fall out and none was assumed.

1. **The azimuth is the whole design, and it is `C-0028`'s "orientation is worth 9.65× for free" one level up.** The same two duplexes, the same bases, the same cap: across the flexure axis they pass with 2.79× of buckling margin; along it they fail `P6` at 0.54×, *and* lose 63 % of the draw-in supply, *and* add 31 % to the tangent. The along row is worse on every axis at once, because a frame couple has an axis and it spent it on the plane that was already strong.
2. **The separation is FREE in the loaded plane.** `L2a6`, `L2a8` and `L2a12` have identical span, tangent, `Φ` and supply ratio — to the last digit, because `Σx_i² = 0` exactly. **The draw-in cost of a cross row is the leg COUNT, not the leg SPACING**, so the separation can be spent entirely on the free plane, up to the point (between 6 and 8 bp) where the loaded plane becomes the minimum and further spending buys nothing.
3. **Full triangulation is the failure `C-0028` predicted — for the layouts it predicted it of.** `L3t` and `L4` put frame couple in the loaded plane, `Φ` collapses to 0.076 and 0.052, the supply falls below the demand, the beam reverts to tension and the membrane term takes the tangent to 36.2 and 39.6 pN/nm and the tension past the 10 pN unzip allowable. **`C-0028`'s intuition was right about the geometry it had in mind and wrong about the geometry the problem needs.**
4. **More legs across is monotonically better and is not needed.** `L3a` reaches 3.52× at the cost of 19 % more supply; two legs already clear every predicate on both rigidities.

### The azimuth sweep, which is the trade drawn out

| θ [deg] | 0 | 22.5 | 45 | 67.5 | 82.5 | **90** |
|---|---|---|---|---|---|---|
| `Σx²` | 3.699 | 3.158 | 1.850 | 0.542 | 0.063 | **0** |
| `Φ` | 0.113 | 0.120 | 0.145 | 0.204 | 0.250 | **0.258** |
| supply/demand | 1.37 | 1.44 | 1.72 | 2.34 | 2.82 | **2.90** |
| tangent [pN/nm] | 33.12 | 32.72 | 31.28 | 28.38 | 26.41 | **26.09** |
| `P_c` [pN] | 2.91 | 5.67 | 9.33 | 11.18 | 10.29 | **9.77** |
| **peak leg / per-leg `P_c`** | 15.00 / 1.46 | 15.13 / 2.84 | 14.98 / 4.66 | 11.91 / 5.59 | 5.82 / 5.14 | **1.75 / 4.89** |
| verdict | FAIL `P6` | FAIL `P9` | FAIL `P9` | FAIL `P9` | FAIL `P9` | **PASS** |

> **A result that was not anticipated, and it is what makes the answer unique rather than merely best.** A row with `Σx_i² > 0` reacts part of the head **moment** as an axial **couple**, so its outermost leg carries more than its share — and `P9` fails at **12 of the 12** azimuths below 90°, including ones with ample *total* margin. **The cost of an off-square row is not only the draw-in it spends but an eccentricity in the very load `P6` is written on.** Only the exact cross row escapes both, and it escapes the second one *identically*: `Σx_i² = 0` means a head moment loads no leg axially at all.
>
> This is `C-0020`'s lesson in a new place — *"a moment applied to a duplex is reacted by the crossovers as an axial couple, so equilibrium bounds the SUM of the member forces on a cut and never the per-member peak"* — now one level up, and with the same consequence: the mean is not the design number.

---

## The window, and the design that results

`L2a8` over `C-0017`'s envelope, nominal two-link cap, favourable mounting:

| `ℓ` [nm] | 5 | 6 | **7** | **8** | **9** | 10 |
|---|---|---|---|---|---|---|
| span [nm] / bp | 33.26 / 98 | 33.26 / 98 | 33.34 / 98 | **33.43 / 98** | 33.51 / 99 | 33.56 / 99 |
| `c₀` | 121.7 | 117.4 | 113.7 | **110.4** | 107.4 | 104.6 |
| tangent [pN/nm] | 28.89 | 26.85 | 26.16 | **26.09** | 26.27 | 26.57 |
| `Φ` | 0.151 | 0.186 | 0.222 | **0.258** | 0.295 | 0.333 |
| supply/demand at 3 nm | 1.69 | 2.08 | 2.49 | **2.90** | 3.32 | 3.75 |
| `T(10)` [pN] | +7.60 | +3.77 | +1.64 | **+0.41** | −0.33 | −0.78 |
| duty(10) [pN] | 6.57 | 4.91 | 4.01 | **3.50** | 3.21 | 3.05 |
| `P_c` [pN] | 19.42 | 14.99 | 11.95 | **9.77** | 8.15 | 6.90 |
| **margin, CanDo** | 2.96 | 3.05 | 2.98 | **2.79** | 2.54 | 2.27 |
| **margin, Fields** | 2.22 | 2.29 | 2.24 | **2.10** | 1.91 | 1.70 |
| buckling stroke [nm] | 16.08 | 17.02 | 17.77 | **18.27** | 18.46 | 18.30 |
| clearance `ℓ − 2.69` | 2.31 | 3.31 | 4.31 | **5.31** | 6.31 | 7.31 |
| verdict | PASS | PASS | PASS | **PASS** | PASS | PASS |

> **The window is `ℓ = 5–10 nm` on CanDo's rigidity and on Fields et al.'s measured one — every length, all nine predicates** — against **no length at all** for the single standoff, whose margin runs **0.40–0.57** and which buckles at a stroke of **3.05–7.02 nm**, i.e. inside §3's desired one. `C-0030`'s clearance ceiling, reported and not adopted, trims the practical window to `ℓ ≥ 6 nm` for §3's *acceptable* 3 nm exactly as it did there.

| the recommended design | |
|---|---|
| **element** | transverse duplex flexure, tile tied at midspan, 45 on `C-0015`'s 3 × 15 grid |
| **span** | **33.43 nm = 98 bp** (was 31.82 = 94 with one leg) |
| **end joint** | **TWO duplexes standing normal to the sheet, 8.0 nm = 24 bp each, laid in a row ACROSS the flexure's axis at 8 bp = 2.72 nm along one sheet duplex** |
| **each base** | `C-0029`'s two-terminus junction, chord along the flexure axis: **78.24 pN·nm/rad restrained, 13.53 free, 64.71 pN/nm axial** |
| **cap** | the two leg heads tied to the flexure end; **nominal `k_link` = `2 k_bond,s` = 64.71 pN/nm per leg**, forced by `C-0029`'s counting theorem applied at the *other* end of each leg |
| **frame couple** | **96.88 pN·nm/rad** in the free plane, **0 exactly** in the loaded one |
| **compliance** | tangent **26.09 pN/nm** at 3 nm (`t/s` = 0.783, minimum **24.40** over 0–10 nm) — **35 % below** `C-0023`'s ceiling, and strain-**softening**, so `CH-0042`/`C-0032` applies unchanged |
| **draw-in** | **0.774 nm supplied per end against 0.267 demanded — 2.90×**; the beam is in **compression** (`−1.13 pN`) at the placement point and crosses to `+0.41 pN` at 10 nm |
| **duty** | 1.111 pN at the held point (exact by placement), **3.499 pN at 10 nm** |
| **buckling** | **`P_c` = 9.77 pN, in the LOADED plane** (free plane 11.70) — **2.79× on CanDo's rigidity, 2.10× on Fields et al.'s**; the truss buckles at an **18.27 nm** stroke |
| **per-leg check (`P9`)** | peak leg compression **1.750 pN** against a per-leg critical load of **4.89** — **2.79×**, identical to the total margin because `Σx_i² = 0` |
| **transverse support** | **88.0 pN/nm** against the beam's 0.741 — **119×**, no dead band |
| **clearance** | **5.31 nm** — covers §3's acceptable 3 nm, not its desired 10; reported, not adopted |
| **plan cost** | **180 standoffs** (2 legs × 2 ends × 45 paths) = **720 nm² of duplex cross-section, 45 % of a 40 × 40 nm footprint** — a *scale*, not a layout; `T-96` owns the plan view |

---

## The literature, re-fetched and counted by hand

Every load-bearing quote below was fetched and re-verified **in this task**, per `CLAUDE.md`'s research practice; the delegated search was treated as a summary and its four load-bearing statements re-checked against the primary sources.

| question | answer | flag |
|---|---|---|
| **Is the only rigid out-of-plane mounting in print triangulated?** | Yes: *"The obstacles consist of 18-nm-long rectangular plates that protrude with an inclination of about 50° from the surface of the triangular platform. **The plates were held rigidly at this angle with a set of double-helical spacers.**"* | **read directly, re-verified verbatim** (Pumm et al., *Nature* **607**:492, EuropePMC `PMC9300469` full text) |
| **How many is "a set"?** | **EXACTLY TWO.** Methods: *"**a set of two spacer oligonucleotide strands** was added in an approximately 100× excess to the sample to mount the obstacles on the triangular platform."* The SI strand table lists `spacer1_01/02`, `spacer2_01/02`, `spacer3_01/02` — three obstacles, six strands — plus two universal complements. **The literature's only rigid out-of-plane mounting has the leg count this claim recommends.** | **read directly, re-verified verbatim** (Methods and SI pp. 22–23, `41586_2022_4910_MOESM1_ESM.pdf`, fetched and read here) |
| **How is each spacer attached?** | Each universal complement is **exactly 39 nt** (`spacer_complement_01 = ATAGTCAGGTGGCATTCTAGTTTCAGGCAAGTGGATTCG`), i.e. exactly the duplex region, so it **terminates at both ends** of the spacer duplex and the spacer strand's own backbone is the single continuation into the plate at one end and into the platform at the other. **So each 39 bp = 13.3 nm spacer is attached by ONE covalent link per end — `C-0029`'s `R3` ball joint, and Rothemund's own observed failure — and the rigidity Pumm et al. report belongs to the PAIR, not to either joint. That is a frame couple, and it is this claim's mechanism, already built.** | sequences **read directly**; the one-link-per-end reading is **DERIVED HERE** from the published strand table and is **not stated in the paper** — flagged as such |
| **Does the paper say how the two spacers are ARRANGED?** | **No.** The word *"spacer"* occurs exactly twice in the whole article — the two sentences above — and there is no caDNAno figure of the obstacle. Spacing, splay and parallelism are all unstated. **This claim's azimuth finding has no published precedent to agree or disagree with.** | **not found** |
| **Is there a published statement that triangulation rigidifies a DNA structure?** | Yes, **qualitatively and with no number**: *"When triangulation was applied to any non-triangular faces, the corresponding structures … showcased significant rigidification"*, and *"We have shown that triangulation is an effective approach to rigidify the corresponding structures."* It is a **wireframe** result — polyhedral faces — not an out-of-plane mounting, so it is used only as a direction check. | **read directly, re-verified verbatim** (Wang et al., *Nat. Commun.* **10**:1067, `PMC6403373`) |
| **A measured stiffness for a multi-duplex bundle standing off a plate?** | **NOT FOUND**, and the nearest source is **paywalled**: Kauert, Kurth, Liedl & Seidel, *Nano Lett.* **11**:5558 (2011) measure **free-standing** 4- and 6-helix bundles and report only qualitatively in the abstract. **No number from it is used here.** | **abstract only** |
| **A published spacing rule for two duplexes protruding from one face?** | **NOT FOUND.** The SAXS interhelical distance — *"for the one layer sheet, we obtain an inter-helical distance of 26.9 ± 0.2 Å"* — is a measurement of **in-lattice packing**, and the same paper's 10 % expansion tolerance is a statement about a packed lattice, not about two free-standing legs. **This claim's 2.04–4.08 nm leg separations are a modelling choice bounded only by sterics.** | **not found** for the protruding case; the SAXS number **read directly** |
| **Two or more duplexes normal to a single-layer sheet under a shared cap?** | **NOT FOUND**, over ~72 further queries in this task on top of `C-0028`'s and `C-0029`'s ~110. Closest in print: a DX-tile array with a **single 5 bp** vertical duplex per tile, and Pumm's obstacle, which is **inclined 50°** on a **multilayer** platform. | **not found** |

---

## The five verification gates

Executed as **26 gate-named tests** in `src/test/kotlin/anchoring/TriangulatedStandoffTest.kt`; **310 `anchoring` tests, 1208 in the suite, 0 failures**, `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a frame couple is a stiffness times a **squared** offset, so doubling every offset quadruples it exactly and a zero offset gives exactly zero at any leg stiffness; a leg's axial stiffness is a modulus over a length **in series with its base**, halving with the length and strictly softer than either member; a truss critical load is a rigidity over a squared length at **fixed** dimensionless restraints (halving the length quadruples it) and scales **exactly** with the leg count; unphysical arguments throw at eight entry points, including an **off-centroid layout** | **PASS** |
| **2 — limiting cases** | **one leg with no offset IS `C-0030`'s single standoff, entry by entry**, at four lengths × two bases, to 1e−12; **legs collapsed onto one axis are `n` springs in parallel exactly** — every entry `C/n`, correlation untouched — at `n` = 1…4; **a cross row adds nothing to the loaded plane, exactly** (`Σx² = 0`, `k_frame,loaded == 0.0`); **a rigid fully triangulated head supplies NO draw-in** (`C12 → 0`, `C11 →` the rotation-fixed sway, **exactly 4×** at a clamp); a **pinned cap** reduces the truss to independent legs at any offsets; the base's two readings reproduce `C-0029`'s ceiling and `C-0028`'s `B1`, and the single standoff buckles about the **free** axis at all six lengths | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the truss buckling load is **exactly** scan-step independent (64 → 8192, departure `0.00e+00`); the placed span is scan-step independent to `2.1e−16`; the assembled flexibility from quadrature matches the closed form to `< 1e−12` at three meshes — and it is **stated in the test** that Simpson is *exact* for this integrand, so a convergence *rate* here would be a property of the round-off; **the result file was re-emitted through `tools/study.sh` and reported *"no result file changed"*** | **PASS** |
| **5 — literature and upstream** | `C-0029`'s hard ceiling **78.24**, nominal **62.06**, free axis **13.53**, and its `T1` weak-axis critical loads at 5 / 7 / 8 nm (**2.46 / 1.69 / 1.46**); `C-0030`'s `B2` coupled design — span **31.82**, tangent **25.23**, duty **3.313**, `P_c` **7.21**; `C-0028`'s `B2` **261.17**; Fields et al.'s **172.906**; the SAXS **2.69**; `C-0025`'s `S/ℓ` = **137.5**; and **Pumm et al.'s spacer count (2) asserted as the recommended leg count, with its 39 bp = 13.26 nm length asserted to be OUTSIDE `C-0017`'s 10 nm envelope** — the precedent is for the mechanism, not the geometry. Worst departure over 14 reproductions: **2.6e−3**, which is the upstream published rounding | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **Maxwell-Betti on the ASSEMBLED truss, between two different quadratures.** Each leg's `C12` reaches the assembly through a **double** cumulative-Simpson integration and its `C21` through a **single** one; the legs are then inverted, summed with the frame couple and inverted back. Over 3 lengths × 3 frame couples × 3 leg counts the assembled off-diagonals agree to **< 1e−12**. Nothing in that route forces it.
2. **The conservation identity, asserted rather than constructed.** `Σx_i² + Σy_i² = w²/2` at every azimuth, and each half equal to its own `cos²`/`sin²` — the statement that makes a partial triangulation possible.
3. **The two planes are exchanged exactly by exchanging the layout's two axes**, and a layout is invariant under leg permutation and under reflection through its centroid.
4. **The axial share of a head moment is `k_frame·C22` of it** — zero for a pinned cap, zero for a cross row *at any cap stiffness*, and strictly positive otherwise. Asserted at three layouts; it is `P9`'s whole content.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the cheap bound failing — the frame couple not clearing the bond couple by enough to move `P6` | **no** | **7.16×** on the nominal cap, 12.0× on a rigid one |
| 2 | `Σx_i²` not vanishing for a cross row, i.e. the conflict being real after all | **no, and it vanishes IDENTICALLY** | `k_frame,loaded == 0.0`, and the three cross-row separations give bit-identical loaded-plane designs |
| 3 | `P3` failing for the cross row — the doubled sway stiffness alone taking the tangent past 40 pN/nm | **no** | 26.09 against 40, and 35 % of headroom |

**The pre-registered prediction held**, and in the form it was written: the answer is a **partially** triangulated head, and the azimuth is worth a whole window at no cost in material.

**Two results that were not anticipated.** `P9` — the eccentric leg loading of an off-square row — was not in the task file, and it is what makes 90° *uniquely* optimal rather than merely best. And the literature count: *"a set"* is **two**, with **one covalent link per end**, so the published rigid mounting is a frame couple made of ball joints — the mechanism of this claim, and the opposite of the joint stiffness `C-0028` was looking for.

---

## Sensitivities — what moves a verdict and what does not

| axis | range | `P_c` [pN] | margin CanDo | margin Fields | verdict moves? |
|---|---|---|---|---|---|
| **head tie** at 8 bp | rigid → two-link → one-link | 9.77 / 9.77 / 9.77 | 2.79 | 2.10 | **no — absorbed** (the free plane runs 14.57 → 11.70 → 9.94 but the **loaded** plane is the minimum) |
| **head tie** at the 6 bp steric floor | rigid → two-link → one-link | **11.40 / 8.84 / 7.45** | 2.79 / 2.52 / 2.13 | 2.10 / 1.90 / 1.60 | **no, but it stops being absorbed** — 1.53× across the sweep |
| **`k_s`** (`C-0020`'s four decades, unmeasured) | ×1/32 → ×8 | 3.28 → 14.12 | **1.29** → 3.14 | **0.97** → 2.36 | **yes, at ×1/32 on the measured rigidity only** — the same crossing `C-0030` reports |
| **`α`** (Chen et al.'s own bracket) | 0.6 → 1.2 | 7.40 → 10.60 | 2.36 → 2.92 | 1.78 → 2.19 | **no** |
| **chord convention** | hard 180° → nominal 120° | 9.77 → 8.69 | 2.79 → 2.61 | 2.10 → 1.96 | **no** |
| **`EI` everywhere** | 230 → 172.9 | 9.77 → 8.32 | 2.79 → 2.35 | 2.10 → 2.35 | **no** |
| **draw-in model** (`T-43`) | chord → shape | 9.77 | 2.79 → 2.53 | 2.10 → 1.90 | **no** |
| **mounting** (`T-75`) | favourable → adverse | 9.77 | 2.79 → 1.34 | 2.10 → **1.01** | **YES — tangent 44.56 pN/nm, `P3` fails** |
| **layout** | the eight of the catalogue | 1.46 → 25.86 | 0.50 → 3.52 | — | **YES, and it is the finding** |

> **`k_s` remains verdict-critical and the truss does not remove that**, exactly as in `C-0028` and `C-0030`: at `k_s/32` the margin is 1.29 on CanDo's rigidity and **0.97 on Fields et al.'s**. `T-9` is still the task that settles it.
>
> **The adverse mounting still has no window**, and for the same reason as in `C-0030` — the tangent, not the buckling. `C-0035`'s determination (`Su`, standoff bases on the output superstructure) is what supplies the favourable sense, and this claim inherits it unchanged.

---

## Does `C-0029`'s verdict survive?

**Its theorem does, entirely. Its branch closure does not — and `C-0029` names the exact condition under which it would not.**

| `C-0029` said | this claim finds |
|---|---|
| a duplex end has exactly two termini, so a base has at most two links on a chord of half-width ≤ 1.0 nm | **the whole method rests on it**, and its three constants are reproduced to ≤ 6e−5. It also applies at the **other** end of the leg, which is what fixes the cap's nominal stiffness |
| two links on a chord restrain **one** axis; the other keeps `2 k_bond,θ` = 13.53 | **reproduced**, and this claim's free plane *is* that axis |
| **a column buckles about its softest axis, so `P6` fails at every length** | **upheld for a SINGLE standoff** — margin 0.40–0.57 over `ℓ = 5–10 nm`, buckling stroke 3.05–7.02 nm — and **reversed for a two-leg cross row**, whose softest axis is now the *loaded* one at 9.77 pN |
| **the standoff branch closes at §3's DESIRED 10 nm stroke** | **reopened.** `ℓ = 5–10 nm`, all nine predicates, on both rigidities |
| *"the restrained-axis reading is available only if a **second element** restrains the free axis"* | **exactly right, and it is available**: the cross row restores precisely that reading, at 2 × `C-0029`'s own restrained-axis critical load |
| *"`C-0028` already priced what it costs: a triangulated head cannot sway"* | **[`CH-0050`](../challenges/CH-0050-the-truss-cost-was-priced-on-the-wrong-axis.md).** True of a head triangulated *along* the flexure axis, false of one triangulated *across* it, and only the second restrains the failing axis |
| the one-sided blunt-end contact is **3.7× short** of carrying the free axis | **untouched and not needed** — the frame couple clears it by 7.16× and no contact is credited anywhere here |
| `E5g16`/`E5a16` is what survives, with **no 90° junction anywhere** | **still the alternative, and still the one with fewer open premises.** This claim reopens a branch; it does not argue the branch should be preferred — see below |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated either.** Two duplexes standing normal to a single-layer sheet under a shared cap is not in the literature; the closest thing in print is inclined 50° on a multilayer platform.
- **The legs are tied ONLY at the head.** They are not crossovered to each other along their length, which would make them a 2-helix bundle and a different object. For a **cross** row that omission is conservative: a bundle would stiffen the free plane further and leave the loaded plane's bending unchanged, because the offset is in `y`.
- **The cap is one rigid body of finite rotational stiffness in series with the legs' axial couple.** Its nominal value is *forced* by the counting theorem; the rigid reading is reported beside it and **not adopted**. Its **geometry** — what physically joins two leg heads 2.72 nm apart to one flexure duplex — is asserted, not designed.
- **The axial load is shared equally under a centroidal shear** (exact for a symmetric layout); the head **moment**'s axial share is carried separately as `P9` and is exactly zero for a cross row.
- **The frame couple is taken to be unaffected by the axial preload**, and the frame's coupling to the sway eigenvalue is modelled as a head spring rather than solved as a two-degree-of-freedom frame eigenproblem. A **torsional** (cap yaw) mode is not modelled; nothing loads it.
- **SMALL DEFLECTION**, exactly as `C-0025`, `C-0028` and `C-0030` flag. The 10 nm columns are linear-theory extrapolations; the 3 nm placement point is inside it.
- **The base chord is assumed laid ALONG the flexure axis** — the orientation that puts the strong axis in the loaded plane. `C-0029` shows the chord azimuth is quantised at 33.74°/bp and that the worst misalignment costs `cos²(16.87°)` = **8.4 %** of the couple; that projection is **not** applied here, so the restrained-axis numbers are the best-phase ones.
- **`k_s` is `C-0020`'s DERIVED, unmeasured construction** and *both* the base couple and the cap rest on it. Swept four decades; the verdict moves at `k_s/32` on the measured rigidity.
- **`EI = 230 pN·nm²` is a CanDo model input**; every critical load is also reported on Fields et al.'s implied 172.9, which is the measured end and 25 % lower.
- **One flexure per load path and 45 attachments**, exactly as upstream. **Two legs per end doubles the standoff count to 180** and 45 % of a tile footprint in duplex cross-section — a *scale*, and whether it fits in plan is `T-96`.
- **The favourable mounting's clearance is reported beside the predicates and not adopted as one**, exactly as `C-0030` does.
- **The `L1` rows here are `C-0030`'s coupled beam on `C-0029`'s realisable base** — a combination neither claim published — which is why the single standoff's margin reads 0.50 here and 0.31 in `C-0029`'s decoupled table. Both upstream pipelines are reproduced separately as gate-5 tests.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012); Fields et al.'s measured buckling implies **172.9** |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| phosphate radius | 1.00 nm | **CITED, READ DIRECTLY** (Hedley et al., *Phys. Rev. X* **14**:031042, 2024), via `C-0029` |
| `k_bond,θ` / `k_bond,s` | 6.765 pN·nm/rad / 32.35 pN/nm | **CITED+FITTED** (Chen et al., *JACS* **136**:6995, 2014) and **DERIVED** (`C-0020`), the second **NOT measured** |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016), re-verified verbatim here |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al. (2009) |
| duplex buckling at 40–41 bp under 9 pN | — | **CITED, MEASURED**, Fields, Meyer & Cohen, *NAR* **41**:9881 (2013), used **only** to produce the second rigidity |
| Pumm et al.'s spacer count and length | **2 per plate, 39 bp** | **CITED, READ DIRECTLY** and counted by hand from the SI strand table; used **only** as a cross-check, never as an input |
| `C-0029`'s ceiling and weak-axis loads, `C-0030`'s `B2` design | — | **CITED**, and reproduced here as gate-5 tests |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the layout algebra and its conservation identity, the assembled 2 × 2 and its quadrature, the frame couple and its series with the cap, the two-plane critical loads, every span, tangent, `Φ`, supply ratio, axial force, duty, margin, per-leg peak, buckling stroke and verdict — is **derived here in code**, with `C-0029`'s and `C-0030`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether TWO 90° junctions can close on one sheet duplex 6–8 bp apart.** `C-0029`'s closure search places **one**; two of them share a seat duplex and their scaffold excursions must not collide. **This is the largest open item under the recommended design**, and it is `T-71`'s question with a second junction attached — `T-97`.
2. **The cap as a solved body rather than a series spring.** Its nominal stiffness is forced by the counting theorem; its geometry is asserted.
3. **`k_s`**, on which the base couple and the cap both rest, and which moves a verdict here as in `C-0028` and `C-0030`. `T-9`.
4. **Whether the plan view admits 180 standoffs and 45 flexures on a 40 × 40 nm footprint.** `T-96`.
5. **Whether this branch should be preferred to `E5a16` at all.** `C-0034`'s guided-arm flexure needs no 90° junction, no standoff, no truss and no cap, and clears §3's desired stroke on a joint the literature has built. **This claim reopens a branch; it does not argue that the branch should be taken.** `T-98`.

## Challenges

**Raises [`CH-0050`](../challenges/CH-0050-the-truss-cost-was-priced-on-the-wrong-axis.md)** against `C-0028` and `C-0029` — the truss's cost was priced on the sway coordinate when the failure was on the orthogonal one. **No number in either claim fails to reproduce**: `C-0029`'s ceiling and weak-axis critical loads and `C-0030`'s whole `B2` design are recovered to ≤ 2.6e−3, which is their own published rounding.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that two 90° junctions cannot close 6–8 bp apart on one sheet duplex.** The recommended design needs two, and `C-0029` searched for one. This is the single largest risk and it is a *chemistry* question, not a mechanics one.
2. **A demonstration that the two leg heads cannot be tied.** At the recommended separation the cap's compliance is absorbed by the loaded plane, but the cap must exist; a pinned cap reduces the truss to two independent legs and restores `C-0029`'s failure exactly.
3. **A measurement of `k_s` more than ~30× below `C-0020`'s construction**, which takes the margin to 0.97 on the measured rigidity — the same crossing `C-0030` reports.
4. **A specification or a build showing the flexure is mounted adversely.** Then the tangent is 44.56 pN/nm, `P3` fails and the window is empty — unchanged from `C-0030`, and settled the favourable way by `C-0035`.
