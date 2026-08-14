# C-0048 — The cap cannot be the flexure and cannot be a spring: a steric count forces it to be a THIRD duplex, and once it is a body the counting theorem applies at its junctions too — where `C-0037` carries the axial path and takes the ROTATION as infinite, which is the term that costs 30 % of the buckling margin and makes `C-0042`'s seven base pairs a property of the CAP's azimuth rather than of the row's

| | |
|---|---|
| **Task** | [`T-106`](../tasks/T-106-truss-cap.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **logical** (a steric exclusion and a terminus count, both cheap, both closed form, and the first of them decides the geometry) **+ in-silico** (a solved cap: its bending by Castigliano on a statically determinate path, its torsion, its rigid height as a unit-determinant congruence, and `C-0042`'s beam-column element extended by one degree of freedom per leg) **+ literature** (the torsional constant fetched and read for this task, and 10 further recorded negatives) |
| **Verdict** | **PASS, and the cap survives the counting theorem only because it has a second azimuth to spend.** Two counts fix the geometry before any solve. `C-0042`'s **steric floor** puts two legs at least `2R = 2.00 nm` apart, and a leg is seated on a duplex only within `R` of its axis — so a duplex laid **across** the row seats **neither** leg (line contact **0.000 nm** at every separation from 6 to 16 bp, against **2.000 nm** for one laid **along** it), and **the flexure cannot be the cap**. The counting theorem says it again independently: the flexure's own end has two termini and there are two legs, so such a cap is **one** link per leg — `C-0037`'s `H1`, not its nominal `H2`. **The cap is therefore a separate crossbar duplex, 13 bp = 4.38 nm at the recommended pitch, hosting THREE 90° junctions and six covalent links, with its axis one duplex radius above the leg heads.** Solved, it costs four things and `C-0037` has none of them: the crossbar's **bending** (`12EI/w` exactly, from a statically determinate path — **8.93×** the couple it carries at 7 bp, so the frame couple falls only **3.9 %**, from 74.18 to 71.31 pN·nm/rad); its **torsion** (`4C/w`, 16.7× the head's own, worth 0.1 %); its **height** (`e = R = 1.00 nm`, which *raises* `C12` but shortens the legs by 1 nm at a given flexure height and softens the buckling geometrically); and — **the term that moves the answer** — the **rotational** path of the same two links `C-0037` credits only axially. A chord's two axes are simultaneous and sum to `C-0042`'s conserved **91.76 pN·nm/rad**, so the cap junction is a **third** instance of one rank-one budget, and unlike the leg row's it has **no free corner**: laid **along** the flexure axis the free plane is capped at **6.20 pN** and **no** separation up to 16 bp hands the governing plane back; laid **across** it the free plane reaches **9.24 pN** and **seven base pairs** is again the smallest that crosses. **So `C-0042`'s separation stands and its reason does not — the crossing is bought at the cap, not at the row** ([`CH-0061`](../challenges/CH-0061-the-cap-is-a-body-and-its-junctions-are-counted-too.md)). The recommended design passes all nine predicates over the whole `h = 5–10 nm` envelope, on **both** rigidities, at a buckling margin of **1.95 / 1.46** against `C-0037`'s 2.79 / 2.10 — **the cap costs 30 % of the margin, 38 % of the draw-in supply and 19 % of the tangent headroom, and it does not close the branch.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the motif is *less* demonstrated than before** — this claim adds a **third body** to a motif the literature does not contain, and its own cap junctions are not routed. `C-0042` retired a chemistry risk; this claim opens a smaller one. |
| **Provenance** | `gpd/results/T-106-truss-cap.json`, produced by `anchoring.TrussCapStudyKt`; **8 cheap-bound quantities, 11 geometry records, 11 frame-couple records, 45 design records, 18 sensitivity records, 12 convergence records, 16 upstream reproductions, 5 literature records**; **27 gate-named tests in `TrussCapTest`**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree; the result file re-run through `tools/study.sh` and reported *"no result file changed"* |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet at the SAXS 2.69 nm, 0.34 nm rise, phosphate radius **1.00 nm**; base couple on the **hard, convention-free 180° chord**, as `C-0037` and `C-0042` both adopt; `EI = 230 pN·nm²` (CanDo model input) with every critical load also on Fields et al.'s implied **172.906**; `GJ = 460 pN·nm²` (CanDo) with the measured 103 nm carried beside it |
| **Consumes** | [`C-0037`](C-0037-triangulated-standoff.md) (`TrussLayout`, `TwoLinkBase`, `trussFrameCouple`, `trussTipFlexibility`, `trussBucklingLoad`, `legAxialStiffness`, `TriangulatedStandoff` — **re-run as a library**), [`C-0042`](C-0042-paired-perpendicular-junction.md) (`seatContactLength`, `pairStericFloorBasePairs`, `chordBaseAxes`, `mixedBaseTrussBucklingLoad` — reproduced by the extended element), [`C-0029`](C-0029-perpendicular-junction-routing.md) (the counting theorem, `maximumBaseRotationalStiffness`, `bondHingeStiffness`, `bondSlideStiffness`, `BForm`, `DuplexBackbone`), [`C-0030`](C-0030-coupled-standoff-joint.md) (`StandoffTipFlexibility`, `standoffTipFlexibility`, `standoffTipFlexibilityByIntegration`, `CoupledJointFlexure`, `coupledFlexureSpan`, `coupledBucklingStroke`, `peakFlexureCompression`, `bracedColumnBucklingLoad`, `favourableStrokeClearance`, `FIELDS_BENDING_RIGIDITY`, `FlexureOrientation`, `DrawInModel`), [`C-0028`](C-0028-standoff-base-joint.md) (`standoffBucklingLoad`, `baseRestraintParameter`, `seriesStiffness`), [`C-0035`](C-0035-flexure-mounting-sense.md) (the favourable mounting), [`C-0023`](C-0023-two-sided-coupling.md) (the 40 pN/nm ceiling, the 45 paths), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile`, [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) |
| **Raises** | [`CH-0061`](../challenges/CH-0061-the-cap-is-a-body-and-its-junctions-are-counted-too.md) against `C-0037`, and against `C-0042`'s inheritance of it |

---

## The claim, in one line

**`C-0037` gave the cap a stiffness and no geometry, and the geometry is what decides it: a leg is seated only within one radius of a seat's axis while two legs must stand at least one diameter apart, so no duplex across the row — the flexure included — can seat both, and the cap has to be a third duplex laid *along* the row; once it is a body it brings its own bending (small), its own torsion (smaller), a height of exactly one radius (which helps the draw-in and hurts the stability), and three more two-terminus junctions whose ROTATIONAL stiffness `C-0037` takes as infinite while carefully carrying their axial one — and because a chord's two axes are one conserved budget, that omission is not a correction but a *second azimuth*, on which the whole crossing `C-0042` attributed to a seven-base-pair row actually depends.**

---

## The six cheap bounds, which ran first — and the two that decided it

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **the seat exclusion** — `C-0042`'s `seatContactLength` at half the row pitch | **0.000 nm**, at every separation from 6 to 16 bp, against **2.000 nm** along the row | **the cap is a separate body**, and the flexure cannot be it. A count and a length, before anything is solved |
| **2** | **the counting theorem at the flexure's end** | **1 link per leg** | the same conclusion by an independent route: a cap that *is* the flexure's end is `C-0037`'s `H1`, and `C-0037` reports `H1` as worth 1.53× of critical load at the steric floor |
| **3** | the crossbar's **bending** against the couple it carries | **8.93×** at 7 bp (1159.7 against 129.8 pN·nm/rad) | above the ~5 the task's falsifier 2 names, so **the cap's bending is a correction, not the answer** — 3.9 % of the frame couple |
| **4** | the crossbar's **torsion** against the head's own rotational stiffness | **16.7×** (773.1 against 46.28) | the loaded plane's cap term is 0.1 % and does not move a verdict on any reading of `C` |
| **5** | **the head junction's ROTATION** against the head's own | **1.69×** (78.24 against 46.28) — and **0.29×** on the other axis (13.53) | **below the falsifier's 10×, and this is the term `C-0037` does not carry.** It is not a correction to the frame couple; it is a **ceiling** on the head restraint the frame couple can ever deliver |
| **6** | the cap's **rigid height** | **1.00 nm** = one duplex radius | the flexure's axis is not at the leg heads. `C12` gains `e·C22`; the legs lose 1 nm at a given flexure height |

> **Bound 1 is the whole geometry and it is free.** `C-0042` introduced `seatContactLength` to stop *its* search parking a standoff on a seat's rim; evaluated at half a leg row's own pitch, the same function says a seat perpendicular to the row is **always** a rim seat, because the steric floor and the seat condition are the same length read twice.
>
> **Bound 5 is the whole mechanics.** `C-0037` writes *"`k_link` = 2 `k_bond,s` = 64.71 pN/nm per leg head, forced by `C-0029`'s counting theorem applied at the other end of each leg"* — and stops. The same two links, on the same chord, also carry the head's **rotation**, and the counting theorem bounds that at `2k_bond,θ + 2k_bond,s r_P²` = 78.24 on one axis and leaves 13.53 on the other. **A ceiling on the head restraint is not a series correction to the couple: it caps what any couple can deliver, which is why widening the row stops working.**

---

## The cap, as a body

| | |
|---|---|
| **what it is** | a **crossbar duplex** laid **along** the leg row, i.e. **across** the flexure's own axis |
| **why it must be** | bound 1: the steric floor puts each leg `≥ R` from the axis of any seat perpendicular to the row, and a flat end face makes contact `2√(R² − y_c²)` — **zero** there. Bound 2 says it again by counting termini |
| **how long** | `w + 2R` = **4.38 nm = 13 bp** at the 7 bp row; 12–20 bp over the swept band |
| **where its axis sits** | one cap radius above the leg heads, `z = ℓ + R`; the flexure butts its **side**, so the flexure's axis is at the same height and **`e = 1.00 nm` above the leg heads** |
| **what it hosts** | **three** 90° junctions — one per leg from below, the flexure's own end from the side — and therefore **six** covalent links, at `C-0029`'s two per duplex end |
| **its bending** | `k_cap,bend = 12 EI/w` **exactly** for free overhangs and moment-free attachments (`16 EI/w` clamped; the bracket is worth **1.6 %**). The frame-couple path is statically determinate — the leg forces are `±M/w` whatever the stiffnesses — so the compliances add and Castigliano gives it in one line |
| **its torsion** | `k_cap,tors = 4C/w` = **773.1 pN·nm/rad** at 7 bp on CanDo's `GJ`, 714.5 on the measured 103 nm. It is the **loaded** plane's only cap term, because a cross row has `Σx_i² = 0` and therefore no frame couple at all there |
| **its in-plane bending** | `3EI/(w/2)³` a side — two orders above the legs' own sway — and taken as rigid |
| **the frame couple it leaves** | `series(k_a Σd², k_link Σd², 12EI/w)` = **71.31** against `C-0037`'s **74.18** pN·nm/rad at 7 bp, on the same legs |

### What the crossbar's bending costs, across the band

At the 8 nm flexure height, both readings on the same 7 nm legs:

| `w` [bp] | 6 | **7** | 8 | 10 | 12 | 14 | 16 |
|---|---|---|---|---|---|---|---|
| `Σd²` [nm²] | 2.081 | **2.832** | 3.699 | 5.780 | 8.323 | 11.329 | 14.797 |
| `k_a Σd²` | 95.4 | **129.8** | 169.5 | 264.9 | 381.4 | 519.2 | 678.1 |
| `12EI/w` | 1352.9 | **1159.7** | 1014.7 | 811.8 | 676.5 | 579.8 | 507.4 |
| asserted `k_frame` | 54.50 | **74.18** | 96.88 | 151.38 | 217.99 | 296.71 | 387.54 |
| **solved `k_frame`** | 53.61 | **71.31** | 90.40 | 130.20 | 167.89 | 199.41 | 222.72 |
| ratio | 0.984 | **0.961** | 0.933 | 0.860 | 0.770 | 0.672 | **0.575** |

> **The cap's bending goes as `1/w` where the couple goes as `w²`, so it is the *wide* rows the crossbar cannot carry** — 1.6 % at the steric floor, 42.5 % at 16 bp. That asymmetry is why the cap cannot be folded into an effective link stiffness, which is what `C-0037`'s `k_tie = k_link Σd²` is: every term in that expression carries `Σd²` and this one does not.

---

## The finding: the cap junction's azimuth is a third conserved budget, and it has no free corner

`C-0042` established that a two-link chord's couple is a rank-one tensor: `loaded + free = 4k_bond,θ + 2k_bond,s r_P²` = **91.76 pN·nm/rad**, invariant under the chord's azimuth. That identity was found on the **base**. It holds at the **cap** too — the same two termini on the same terminal chord — and there the two planes are in direct opposition:

| at `h = 8 nm`, legs 7.0 nm, `C-0029`'s hard chord | cap chord **along** the flexure axis (`Sx`) | cap chord **across** it (`Sy`) |
|---|---|---|
| head junction, loaded plane | **78.24** | 13.53 |
| head junction, free plane | 13.53 | **78.24** |
| span [nm] | 30.30 | **28.25** |
| tangent at 3 nm [pN/nm] | 29.40 | **30.93** |
| supply / demand at 3 nm | 2.20 | **1.81** |
| duty at 10 nm [pN] | 4.18 | **4.60** |
| `P_c` loaded [pN] | 9.67 | 8.95 |
| `P_c` free [pN] | **6.20** | **9.24** |
| governing plane | **free** | **loaded** |
| margin, CanDo / Fields | 1.48 / 1.12 | **1.95 / 1.46** |
| smallest `w` handing the plane back to the loaded one | **none up to 16 bp** | **7 bp** |

> **This is `C-0037`'s azimuth trade one level up, and it is the harder one.** At the *base* and at the *row* the trade had a free corner: a cross row puts `Σx_i² = 0` and costs the loaded plane exactly nothing. At the *cap* there is no such corner — the loaded plane wants the chord's strong axis for the draw-in supply and the tangent, the free plane wants it for stability, and 91.76 is all there is. The design goes to the **free** plane because that is where `P6` binds, and pays 18 % of the supply ratio and 5 % of the tangent for it.
>
> **And it is the reason `C-0042`'s seven base pairs survives.** `C-0042` reports 7 bp as *"the smallest separation whose free plane has crossed above its loaded one"*, 10.30 against 9.77 pN — reproduced here to **1.9e−4**. On a solved cap that crossing exists **only** at the across-axis cap chord: along it, the free plane is capped at 6.20 pN by the junction's own 13.53 and **no** row width up to 16 bp buys it back. The separation is right; the mechanism is not the row's.

---

## The design that results, over `C-0017`'s envelope

Seven base pairs, cap chord **across** the flexure axis, 45 paths, secant placed at 33.3333 pN/nm at 3 nm by construction, favourable mounting (`C-0035`), duty at §3's **desired** 10 nm on the element's own end shear (`CH-0037`). **`h` is the flexure's height above the sheet; the legs are `h − 1.00 nm`, because the cap has a radius.**

| `h` [nm] | 5 | 6 | **7** | **8** | 9 | 10 |
|---|---|---|---|---|---|---|
| legs [nm] | 4.00 | 5.00 | 6.00 | **7.00** | 8.00 | 9.00 |
| span [nm] | 28.39 | 28.28 | 28.25 | **28.25** | 28.27 | 28.29 |
| tangent at 3 nm | 35.21 | 32.84 | 31.58 | **30.93** | 30.61 | 30.48 |
| supply / demand | 1.19 | 1.39 | 1.59 | **1.81** | 2.05 | 2.30 |
| duty(10) [pN] | 7.90 | 6.25 | 5.23 | **4.60** | 4.19 | 3.93 |
| `P_c` loaded | 14.68 | 12.47 | 10.55 | **8.95** | 7.64 | 6.58 |
| `P_c` free | 16.62 | 13.24 | 10.92 | **9.24** | 7.96 | 6.96 |
| **margin, CanDo** | 1.86 | 2.00 | **2.02** | **1.95** | 1.82 | 1.68 |
| **margin, Fields** | 1.40 | 1.50 | **1.52** | **1.46** | 1.37 | 1.26 |
| clearance `h − 2.69` | 2.31 | 3.31 | 4.31 | **5.31** | 6.31 | 7.31 |
| verdict | PASS | PASS | PASS | **PASS** | PASS | PASS |

| the recommended design | |
|---|---|
| **element** | transverse duplex flexure, tile tied at midspan, 45 on `C-0015`'s 3 × 15 grid |
| **span** | **28.25 nm = 83 bp** (was 33.43 = 98 with `C-0037`'s asserted cap) |
| **legs** | **two duplexes standing normal to the sheet, 7.00 nm = 21 bp**, in a row **across** the flexure's axis at **7 bp = 2.38 nm** along one sheet duplex (`C-0042`'s placement, unchanged) |
| **each base** | `C-0029`'s two-terminus junction, chord **along** the flexure axis: 78.24 restrained / 13.53 free, 64.71 axial |
| **the cap** | a **crossbar duplex, 13 bp = 4.38 nm**, laid **along** the leg row, axis **1.00 nm** above the leg heads, hosting **three** 90° junctions and **six** covalent links |
| **each cap junction** | the same two-terminus chord, laid **ACROSS** the flexure axis — the opposite azimuth to the base's: **13.53** loaded / **78.24** free, 64.71 axial and shear |
| **the flexure's own end** | a fourth two-terminus junction onto the crossbar, chord on the loaded plane: **78.24** pN·nm/rad and **64.71** pN/nm |
| **frame couple** | **71.31 pN·nm/rad** in the free plane (asserted 74.18), **0 exactly** in the loaded one |
| **compliance** | tangent **30.93 pN/nm** at 3 nm (`t/s` = **0.928**, minimum **30.83** over 0–10 nm) — **23 % below** `C-0023`'s ceiling, and strain-**softening**, so `CH-0042`/`C-0032` applies unchanged |
| **draw-in** | **0.571 nm supplied per end against 0.315 demanded — 1.81×** (was 2.90×) |
| **duty** | 1.111 pN at the held point (exact by placement), **4.599 pN at 10 nm** |
| **buckling** | **`P_c` = 8.95 pN, in the LOADED plane** (free 9.24) — **1.95× on CanDo's rigidity, 1.46× on Fields et al.'s**; the truss buckles at a **14.94 nm** stroke |
| **per-leg check (`P9`)** | 2.298 pN against a per-leg critical load of 4.474 — **1.95×**, identical to the total because `Σx_i² = 0` |
| **transverse support** | **53.66 pN/nm** against the beam's 0.741 — **72×**, no dead band |
| **clearance** | **5.31 nm** — covers §3's acceptable 3 nm, not its desired 10; reported, not adopted |
| **plan cost** | **180 legs + 90 crossbars** (2 legs and 1 cap × 2 ends × 45 paths) — `T-96` owns the plan view, and this claim adds 90 bodies to it |

> **What the solved cap costs, at the same design point:** buckling margin **2.79 → 1.95** (CanDo) and **2.10 → 1.46** (Fields et al.), i.e. **−30 %**; draw-in supply **2.90 → 1.81**, i.e. **−38 %**; tangent headroom **13.9 → 9.1 pN/nm**, i.e. **−35 %**; and the bending coefficient `c₀` **110.4 → 73.1**, which is what shortens the span from 33.43 to 28.25 nm. **Nothing fails.** The branch `C-0037` reopened stays open, with less of everything.

---

## The five verification gates

Executed as **27 gate-named tests** in `src/test/kotlin/anchoring/TrussCapTest.kt`; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree, and the result file re-emitted through `tools/study.sh` reported *"no result file changed"*.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the cap's bending is a rigidity over a length (doubling `EI` doubles it, doubling `w` halves it) and its end factor is a pure multiplier; its torsion is `4C/w` with the same two scalings; the rigid height enters as a congruence whose **determinant is invariant** and whose three entries gain exactly `e·C22`, `2e·C12 + e²C22` and nothing; a cap length is a width over a rise, so halving the rise doubles the base-pair count; unphysical arguments throw at **nine** entry points, including a mismatched junction list | **PASS** |
| **2 — limiting cases** | **no duplex across the row seats either leg at any separation 6–16 bp**, and one along it seats each on its full diameter; an infinitely stiff cap returns `C-0037`'s `trussFrameCouple` **exactly**, and a rigid link on top of that the bare `k_a Σd²`; **a rigid cap of zero height returns `trussTipFlexibility` entry by entry** at 3 lengths × 3 frame couples × 3 leg counts to 1e−12; **rigid junctions and zero height return `C-0042`'s `mixedBaseTrussBucklingLoad`** at 3 frame couples × 3 base sets to 1e−9 — which is what verifies the element matrices written out again rather than shared out of a filed claim's source; a **pinned** cap junction reduces the truss to independent free-headed legs; a pinned base with no frame couple is **exactly zero**, `C-0028`'s mechanism; a finite junction is strictly softer than a rigid one and a weak one softer than a strong one; the rigid height strictly lowers the critical load; **the along-axis cap chord never hands over the governing plane at any separation to 16 bp, and the across-axis one does so at exactly 7** | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the capped element falls monotonically on nested meshes 8 → 16 → 32 and moves **1.3e−10** between 32 and 64, which is the bisection's own floor; the assembled `C12` by quadrature matches the closed form to **1.7e−15** at four Simpson levels; the placed span is **bit-identical** at four scan resolutions (departure `0.00e+00`); the cap's end-condition bracket (12 → 16) is **1.6 %** of the frame couple; the result file re-emitted through `tools/study.sh` reported *"no result file changed"* | **PASS** |
| **5 — literature and upstream** | `C-0037`'s `L2a8` frame couple **96.88**, loaded **9.77** and free **11.70** critical loads, span **33.43**, tangent **26.09**, supply **2.90** and leg axial **44.0**; `C-0042`'s 7 bp free **10.30** and loaded **9.77**; `C-0029`'s ceiling **78.24**, free axis **13.53** and link **64.71**; `C-0042`'s steric floor **6 bp** and chord budget **91.76**; Fields et al.'s **172.906**; the SAXS **2.69**; and the duplex torsional constant on both readings. Worst departure over **16 reproductions: 1.95e−4**, which is the upstream published rounding | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **Maxwell-Betti on the assembled *and capped* head, between two quadratures.** Each leg's `C12` reaches the assembly through a **double** cumulative-Simpson integration and its `C21` through a **single** one; they then pass through two junction springs, a parallel sum, a frame couple, a torsion, a unit-determinant congruence and a fourth junction. Over 3 lengths × 3 leg counts × 2 frame couples the assembled off-diagonals agree to **< 1e−12**, and the two quadratures agree with the closed form to 1e−9. **Nothing in that route forces it.**
2. **The rigid height is a congruence of unit determinant**, so `det C` is invariant under it — asserted to 1e−12. A height that changed the determinant would be adding energy to a rigid-body motion.
3. **The chord budget is conserved at the cap**, `loaded + free = 91.76` at seven azimuths, with each half equal to its own `cos²`/`sin²` and the two ends equal to `C-0029`'s two published constants. `C-0042`'s identity, on a body it was not derived for.
4. **The capped element does not care in which order its legs are listed**, at *mismatched* bases and *mismatched* junctions — exact as physics, limited only by the `LDL` elimination order (1e−9).

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | **bound 1 failing** — a duplex that seats both legs | **no**, and it fails identically | line contact `0.000` at 6–16 bp, because the steric floor *is* the seat condition read twice |
| 2 | **bound 3 landing below ~2** — the cap's bending comparable to the couple | **no** | 8.93× at the recommended pitch. It does fall to **0.75×** at 16 bp, which is why the *wide* rows are the ones the crossbar cannot carry |
| 3 | **bound 5 landing above ~10×** — the head junctions negligible | **no, and it is the finding** | **1.69×** on one axis and **0.29×** on the other |
| 4 | **the reductions failing** | **no** | three claims deep: `trussTipFlexibility` entry by entry, `mixedBaseTrussBucklingLoad` to 1e−9, `C-0028`'s mechanism exactly zero |

**The pre-registered prediction held in three of its four parts and was wrong in the fourth, which is the interesting one.** It predicted a crossbar of ~13 bp (13), a cap bending clearance of ~9× (8.93), and that the head junctions' rotation would be the term that moves the answer (it is). It predicted that *"`C-0042`'s crossing separation moves above 7 bp"* — and it does not: **the crossing does not move, it becomes conditional on a design variable neither upstream claim has.**

---

## Sensitivities — what moves a verdict and what does not

| axis | range | `k_frame` | `P_c` [pN] | margin CanDo / Fields | verdict moves? |
|---|---|---|---|---|---|
| **cap model** | `C-0037`'s series spring → solved crossbar | 74.18 → 71.31 | 9.77 → 8.95 | 2.79/2.10 → 1.95/1.46 | **no — but it is 30 % of the margin** |
| **cap junction azimuth** | across the flexure axis → along it | 71.31 | 8.95 → **6.20** | 1.95/1.46 → 1.48/1.12 | **no, and it is the finding** — both pass, but only one crosses the plane |
| **flexure end junction** | rigid → two-link → **one-link** | 71.31 | 8.95 | 2.07/1.56 → 1.95/1.46 → 1.64/1.23 | **YES at one link** — supply 0.72, `P8` fails. **And one link per end is exactly what the literature's built precedent has** |
| **cap end condition** | pinned (12) → clamped (16) | 71.31 → 72.43 | 8.95 | 1.95/1.46 | **no** — 1.6 % |
| **cap torsional rigidity** | CanDo 460 → 100 nm → measured 103 nm | 71.31 | 8.95 | 1.95/1.46 | **no** — the tangent moves 0.01 pN/nm |
| **`k_s`** (`C-0020`'s four decades, unmeasured) | ×1/32 → ×8 | 2.84 → 223.5 | 3.22 → 13.11 | **0.93/0.70** → 2.21/1.66 | **YES at ×1/32, and now on BOTH rigidities** — `C-0037` had 1.29/0.97 there |
| **`EI` everywhere** | 230 → 172.9 | 71.31 → 69.89 | 8.95 → 7.72 | 1.95 → 1.72 | **no** |
| **draw-in model** (`T-43`) | chord → shape | 71.31 | 8.95 | 1.95/1.46 → 1.72/1.29 | **no** |
| **mounting** (`T-75`) | favourable → adverse | 71.31 | 8.95 | 1.24/0.93 | **YES — tangent 44.18 pN/nm, `P3` fails**, unchanged from `C-0037` |

> **`k_s` remains verdict-critical and the solved cap makes it worse, not better.** The base couple, the head links **and** all four cap junctions rest on `C-0020`'s derived, unmeasured `k_s`; at `k_s/32` the margin is **0.93 on CanDo's rigidity**, where `C-0037` had 1.29. `T-9` is still the task that settles it, and it now settles more of the design than it did.

---

## Do `C-0037`'s and `C-0042`'s verdicts survive?

**Both do. One assertion in each does not.**

| upstream said | this claim finds |
|---|---|
| `C-0037`: *"the cap is one rigid body of finite rotational stiffness in series with the legs' axial couple … its geometry is asserted, not designed"* | **the geometry is now derived and it is not free**: a crossbar duplex, 13 bp, three junctions, six links, one radius of height — [`CH-0061`](../challenges/CH-0061-the-cap-is-a-body-and-its-junctions-are-counted-too.md) |
| `C-0037`: `k_link` = `2 k_bond,s`, *"forced by `C-0029`'s counting theorem applied at the other end of each leg"* | **right, and incomplete.** The same two links carry the head's rotation, at 78.24 / 13.53, and `C-0037` takes both as infinite. That is what costs 30 % of the margin |
| `C-0037`: the frame couple is `series(k_a Σd², k_tie)` | **upheld in form**, with one more series member that does **not** carry `Σd²` — 3.9 % at 7 bp, 42.5 % at 16 |
| `C-0037`: `L2a6`/`L2a8`/`L2a12` are bit-identical in the loaded plane | **upheld** — 9.77 pN, span 33.43, tangent 26.09, supply 2.90 at all three, reproduced to ≤ 1.9e−4 |
| `C-0037`: the window is `ℓ = 5–10 nm`, all nine predicates, on both rigidities | **upheld on a solved cap**, at `h = 5–10 nm` and margins 1.68–2.02 / 1.26–1.52 |
| `C-0037`: *"a pinned cap reduces the truss to two independent legs and restores `C-0029`'s failure exactly"* | **reproduced as a gate-2 test**, and the solved cap is nowhere near that corner |
| `C-0042`: **seven** base pairs, because 7 bp is the smallest row whose free plane has crossed | **the separation stands; the reason does not.** With the cap chord along the flexure axis no separation to 16 bp crosses at all. The crossing is bought at the **cap's** azimuth |
| `C-0042`: *"this claim places two bases, not a cap"* | **the cap is now placed as a body — and not routed.** Whether three 90° junctions close on one 13 bp crossbar is `C-0042`'s own question at the other end of the same legs, and is open |
| `C-0042`: `Σ(Δy_i)² = 0` enforced, so `C-0037`'s `P9` stays vacuous | **untouched**; the per-leg margin equals the total to the last digit |

---

## The literature

| question | answer | flag |
|---|---|---|
| **What torsional constant does a B-form duplex have?** | *"the measured high-force (6.5 pN) torsional stiffness values of `C = 103 ± 4 nm` are identical, within experimental errors, for all tested salt concentration, suggesting that the intrinsic torsional stiffness of DNA does not depend on salt"* — including **10 mM MgCl₂**. That is **426.6 pN·nm²**, against CanDo's model input of 460 | **read directly** (abstract, verbatim, EuropePMC `PMC5449586`; Kriegel, Ermann, Forbes, Dulin, Dekker & Lipfert, *Nucleic Acids Res.* **45**:5920, 2017) |
| **Does the literature's only rigid out-of-plane mounting describe its CAP?** | **Its cap is an entire 18-nm multilayer PLATE, not a duplex** — and each of its two spacers is attached to it by **one covalent bond per end**. So the one built precedent caps a two-leg frame with a body far stiffer than a crossbar **and** attaches to it with **half** the links `C-0037` assumes. This claim's `F1` sensitivity is that precedent, and it **fails `P8`** | **read directly** by `C-0037`, re-read here (Pumm et al., *Nature* **607**:492, Methods and SI pp. 22–23) |
| **What is the nearest published relative of the cap junction — a duplex END meeting a duplex SIDE at 90°?** | The **T junction**, used exactly as this claim's cap has to be used: *"we present one-, two-, and three-layer T-shaped crossover tiles, by integrating T junction with antiparallel crossover tiles. These tiles **carry over the orthogonal binding directions from T junction and retain the rigidity from antiparallel crossover tiles**"*. **In print the orthogonal joint supplies DIRECTION and the crossovers supply RIGIDITY** — this claim's finding one level up. It is an **in-plane**, **base-pairing** motif, so it adds no covalent link and leaves the counting theorem untouched | **read directly** (abstract, verbatim, EuropePMC `PMC10667507`, 2023) |
| **Is there a published crossbar tying two duplexes that stand off an origami sheet?** | **NOT FOUND**, over 10 further EuropePMC queries here, on top of `C-0042`'s 11, `C-0037`'s ~72 and `C-0029`'s ~110 | **not found** |
| **A measured rotational stiffness for a perpendicular duplex-to-duplex joint?** | **NOT FOUND** — unchanged from `C-0029`, whose only nearby number is Pan et al.'s four-way junction scissor stiffness of 135 pN·nm/rad, fitted to MD | **not found** |

**Query strings recorded, so the negative is falsifiable by one paper** (EuropePMC REST search, ~9 s apart):
`ABSTRACT:"DNA origami" AND ABSTRACT:"crossbar"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"cap" AND ABSTRACT:"duplex"` (0);
`ABSTRACT:"DNA nanostructure" AND ABSTRACT:"perpendicular" AND ABSTRACT:"crossbar"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"portal frame"` (0);
`ABSTRACT:"DNA" AND ABSTRACT:"three-way" AND ABSTRACT:"perpendicular" AND ABSTRACT:"duplex"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"standoff" AND ABSTRACT:"cap"` (0);
`ABSTRACT:"origami" AND ABSTRACT:"two legs" AND ABSTRACT:"rigid"` (0);
`ABSTRACT:"DNA" AND ABSTRACT:"T-junction" AND ABSTRACT:"rigidity"` (**1** — the T-shaped crossover tile paper above, read);
`ABSTRACT:"DNA origami" AND ABSTRACT:"torsional stiffness" AND ABSTRACT:"junction"` (0);
`ABSTRACT:"DNA origami" AND ABSTRACT:"tripod" AND ABSTRACT:"platform"` (1 — gold-nanorod plasmonics on an origami tripod, a 3-D nanorod scaffold, not an out-of-plane duplex on a single-layer sheet).

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is *less* demonstrated than it was.** Two duplexes standing normal to a single-layer sheet is not in the literature; a crossbar tying them and carrying a flexure is a third body on top of that.
- **The cap's geometry is DERIVED but not ROUTED.** This claim shows the cap must be a separate crossbar and how long it is; it does **not** run `C-0042`'s closure search on it, so whether **three** 90° junctions close on one 13 bp crossbar is open exactly as the pair was before `C-0042`. It is that claim's question at the other end of the same legs, on a **lone** seat duplex with no lattice neighbours.
- **A 13 bp crossbar is a short duplex and its own thermodynamic stability is not modelled.** It is held by six covalent links and nothing else.
- **The cap's bending is exact on the frame-couple path and bracketed off it.** `12EI/w` assumes moment-free attachments and free overhangs; `16EI/w` assumes clamped ones; the true value lies between and the bracket is worth 1.6 %.
- **The cap's TORSION is the loaded plane's only cap term because the cross row has `Σx_i² = 0` exactly.** A row that is not straight would couple the two planes and this decomposition would not hold — which is `C-0037`'s `P9` and `C-0042`'s `Q4` in a new place.
- **The crossbar's bending about the sheet normal is taken as rigid** — `3EI/(w/2)³` a side, two orders above the legs' own sway.
- **The rigid height is the crossbar's radius**, i.e. the flexure butts the crossbar's *side*. A design that let the flexure sit **on** the crossbar would double it.
- **Cap yaw is not modelled and nothing loads it**; the frame couple is taken to be unaffected by the axial preload, as `C-0037` and `C-0042` both assume.
- **`k_s` is `C-0020`'s DERIVED, unmeasured construction**, and the base couple, the head links and all four cap junctions rest on it. Swept four decades; the verdict moves at ×1/32 on **both** rigidities.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT** and every critical load is also given on Fields et al.'s implied 172.9. The torsional constant is carried on CanDo's 460 and on the measured 103 nm, and is worth 0.1 % either way.
- **SMALL DEFLECTION**, exactly as `C-0025`, `C-0028`, `C-0030`, `C-0037` and `C-0042` flag.
- **The favourable mounting's clearance is reported beside the predicates and not adopted as one**, exactly as `C-0030`, `C-0037` and `C-0042` do.
- **The plan cost rises by 90 crossbars.** `T-96` owns the plan view and this claim makes its problem larger, not smaller.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| phosphate radius | 1.00 nm | **CITED, READ DIRECTLY** (Hedley et al., *Phys. Rev. X* **14**:031042, 2024), via `C-0029` |
| duplex steric radius | 1.00 nm | **CITED**, the standard 2 nm diameter |
| rise per base pair | 0.34 nm | **CITED** (Douglas et al., 2009) |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS (Fischer et al., 2016) |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012) |
| duplex `GJ` | 460 pN·nm² | **CITED, a CanDo MODEL INPUT**; the measured torsional persistence **103 ± 4 nm** (Kriegel et al., *NAR* **45**:5920, 2017) gives **426.6**, **READ DIRECTLY** here |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED** (Wang et al., *Biophys. J.* **72**:1335, 1997) |
| `k_bond,θ` | 6.765 pN·nm/rad | **CITED+FITTED** (Chen et al., *JACS* **136**:6995, 2014) via `C-0009` |
| `k_bond,s` | 32.35 pN/nm | **DERIVED** (`C-0020`), **NOT measured**; swept four decades, and a verdict moves |
| Fields et al.'s implied rigidity | 172.9 pN·nm² | **CITED, MEASURED** (*NAR* **41**:9881, 2013) |
| Pumm et al.'s spacer count and attachment | 2 per plate, one link per end | **CITED, READ DIRECTLY** by `C-0037`; used here as the `F1` sensitivity |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the seat exclusion and the geometry that follows, the crossbar's length, bending, torsion and height, the solved frame couple, the assembled and capped 2 × 2 and its quadrature, the extended beam-column element and every critical load, every span, tangent, coupling factor, supply ratio, duty, margin, per-leg peak, buckling stroke and verdict — is **derived here in code**, with `C-0028`'s, `C-0029`'s, `C-0030`'s, `C-0037`'s and `C-0042`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether three 90° junctions close on one crossbar duplex.** `C-0042`'s search at the other end of the same legs, with a third junction, a lone seat and no lattice neighbours. **This is now the largest open item under the recommended design**, and it is the risk `C-0042` retired, moved up one storey.
2. **Whether a 13 bp crossbar is a structural member.** Its own melting is not modelled, and lengthening it costs plan area.
3. **`k_s`.** `T-9`, and it now moves a verdict on **both** rigidities rather than one.
4. **Whether the plan view admits 180 legs, 90 crossbars and 45 flexures on a 40 × 40 nm footprint.** `T-96`, with 90 more bodies than it had.
5. **Whether this branch should be preferred to `E5a16` at all.** `T-98`. **This claim ADDS an open premise where `C-0042` removed one**, and it costs 30 % of the buckling margin — both belong in that comparison.

## Challenges

**Raises [`CH-0061`](../challenges/CH-0061-the-cap-is-a-body-and-its-junctions-are-counted-too.md)** against `C-0037`'s asserted cap, and against `C-0042`'s inheritance of it. **No number in either claim fails to reproduce** — 16 reproductions at ≤ 1.95e−4, including `C-0037`'s whole `L2a8` design and `C-0042`'s 7 bp critical loads through an independently extended solver.

**None stands against this claim.** The four ways it would fail:

1. **A closure showing three 90° junctions cannot be placed on one crossbar.** Then the cap does not exist and `C-0037`'s branch closes on chemistry, not mechanics.
2. **A cap geometry that avoids the height or the junctions** — for instance a multilayer cap, which is what the one published precedent uses. That would recover most of the 30 %, at a plan cost this claim does not price.
3. **A measurement of `k_s` more than ~30× below `C-0020`'s construction**, which takes the margin to 0.93 on CanDo's rigidity and 0.70 on Fields et al.'s.
4. **A specification or a build showing the flexure is mounted adversely.** Then the tangent is 44.18 pN/nm, `P3` fails and the window is empty — unchanged from `C-0030` and `C-0037`, and settled the favourable way by `C-0035`.
