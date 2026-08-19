# C-0141 — **The honeycomb face supplies the stations `C-0122` counted, for none of the reasons `C-0122`, `CH-0151` or `C-0128` give — and the cross-section every four-layer claim here is written on is not a honeycomb at all.** A honeycomb spends `3√3/4 · d² = 8.35449857 nm²` per helix where the standing tile spends `d² = 6.431296`, **1.29903811×**, because the in-plane row pitch is `3d/2` and the layer pitch `d√3/2` and only their **product** is the cell. So every `edgeY` in the four-layer line is **exactly 1.5× too small**: `15 × 4` is **56.524 nm** across, **1.40084263** of §3's 40.35, and `10 × 6` is **37.504 nm**, **0.929467162** of it — **the footprint ordering `C-0120` publishes REVERSES**. The census survives at **90** and **60**; the azimuth is **30°, not 60°**, there is **no perpendicular root anywhere on the face**, and a **7 bp stagger between adjacent station rows is FORCED** — which is what makes the **rooting-helix parity** decide everything: `15 × 4` admits **no** centro-symmetric station lattice at **any** of the 21 phases, at **either** admissible offset, at **either** row length, and `10 × 6` admits one **at the full station count**

| | |
|---|---|
| **Task** | [`T-219`](../tasks/T-219-honeycomb-station-lattice-and-placement.md) — the honeycomb's own station lattice, plan ceiling and placement family |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (an integer lattice census and two exhaustive enumerations, derived from the primary rules) **+ in-silico** (eight plate/grillage solves at two geometries, plus two threshold bisections and two convergence axes) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The cross-section is a lattice statement read from the primary source; no folded object is measured here. |
| **Verdict** | **PASS on all four predicates. `F1`, `F2`, `F4` and `F5` did not fire; `F3` FIRED and the direction is favourable.** `P1` the census is derived; `P2` the plan ceiling is delivered as an exact bisection at ten counts; `P3` the centro-symmetric family exists on one cross-section and provably not on the other; `P4` the square-lattice-specific machinery is named function by function — **and the useful half of `P4` is that the placement machinery is NOT square-lattice-specific and took the honeycomb lattice unmodified.** |
| **Provenance** | [`gpd/results/T-219-honeycomb-station-lattice-and-placement.json`](../results/T-219-honeycomb-station-lattice-and-placement.json), produced by `tile.HoneycombPlacementStudyKt`; model [`tile/HoneycombFaceLattice.kt`](../../src/main/kotlin/tile/HoneycombFaceLattice.kt), tests [`tile/HoneycombFaceLatticeTest.kt`](../../src/test/kotlin/tile/HoneycombFaceLatticeTest.kt) (**23**, written first and watched to fail). |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.141947 pN·nm. Honeycomb at 10.5 bp/turn, bond length `d` = 2.536 nm; rows of 112 bp (`C-0119`) and 119 bp (`C-0136`); `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V, read from `gpd/results/T-3b-tile-edge-load-profile.json`; `C-0001`'s secant foundation; the measured composite fraction 0.30, band 0.26–0.33; `T-5b`'s 0.10; 81 × 81 dishing grid, 2 subdivisions. |
| **Consumes** | [`C-0119`](C-0119-honeycomb-raster-width.md) (the primary honeycomb rules and the seven published cross-sections), [`C-0122`](C-0122-honeycomb-station-lattice.md) / [`CH-0151`](../challenges/CH-0151-an-oblique-helix-has-two-free-azimuths-not-one.md) (the census this derives), [`C-0128`](C-0128-oblique-attachment-root.md) (the oblique anisotropy), [`C-0118`](C-0118-coupled-four-layer.md) (the caveat this closes), [`C-0120`](C-0120-cross-section-comparison.md) (the cross-section comparison), [`C-0116`](C-0116-composite-fraction-threshold.md) (the threshold), [`C-0109`](C-0109-four-layer-tile.md), [`C-0072`](C-0072-plan-tolerance-model.md) (the plan-ceiling bisection, reproduced), [`C-0063`](C-0063-upward-root-placement.md) (the centro-symmetric family), [`C-0136`](C-0136-mixed-domain-phase-and-honeycomb-twist.md) (the 119 bp buildable width), [`C-0022`](C-0022-tile-edge-load-profile.md) |
| **Constrains** | **Two challenges are raised.** [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md) against `C-0120`'s footprint ordering and `C-0109`'s cross-section; [`CH-0175`](../challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md) against `C-0128`'s 60° and `CH-0151`'s two-azimuth correction. `C-0122`'s **numbers** are upheld and its **reason** is withdrawn. |

---

## 1. The cheap bound, and it settled the geometry before the census

Two multiplications, run before any code:

| | honeycomb | the standing tile | ratio |
|---|---|---|---|
| in-plane row pitch | `3d/2` = **3.804 nm** | `d` = 2.536 | 1.5 |
| layer pitch | `d√3/2` = **2.19624042 nm** | `d` = 2.536 | 0.866 |
| **plan area per helix** | **`3√3/4 · d²` = 8.35449857 nm²** | `d²` = 6.431296 | **1.29903811** |

**The standing cross-section is 1.299× denser than any honeycomb of that bond length can be**, and
the two pitches are wrong in **opposite** directions — which is why `CLAUDE.md`'s standing note
(*"a honeycomb array stacks its rows at `d√3/2`, not `d`"*) fixes one factor and makes the **density**
error worse: `2.536 × 2.196 = 5.569 nm²` is 1.50× out where `2.536 × 2.536` is 1.30× out. Only the
**product** `3d/2 · d√3/2` is the cell, and asserting that product is one test.

The other cheap bound was the along-helix ladder — 21 bp = **7.14 nm** against the square lattice's
32 bp = 10.88 nm, so the collinear inboard budget is `7.14 − 2.536` = **4.604 nm** against
`10.88 − 2.69` = **8.19 nm**, **1.77888792×** less. That one **predicted the wrong sign**; see §6.

## 2. The cross-section, derived from the source's own two sentences

Douglas et al., *NAR* **37**:5001 (PMC2731887), **already in `gpd/data/T-151-sources/`**:

> *"The **x-raster rows** within the honeycomb framework are **corrugated**; they stagger up and down
> and encompass helices that are actually at **two different y-positions**. Similarly, virtual
> y-oriented layers can be defined that stagger left and right and encompass helices that are at
> **two different x-positions**."* … *"the nomenclature of the designs is `m × n`, where `m` is the
> number of x-raster rows, and `n` is the number of helices per x-raster row."*

So a block is `m` corrugated rows of `n` helices; site `(r, c)`; neighbours `(r, c ± 1)` and
`(r + 1, c)` if `r + c` is even, else `(r − 1, c)`. Every bond is asserted to be exactly `d` long in
the emitted positions rather than argued.

| design | helices | **in-plane width** | **thickness** | standing `edgeY` | honeycomb `edgeY` | of §3's 40.35 |
|---|---|---|---|---|---|---|
| **`15 × 4`** | 60 | **56.524 nm** | **8.58872127 nm** | 38.04 | **57.06** | **1.40084263** |
| **`10 × 6`** | 60 | **37.504 nm** | **12.9812021 nm** | 25.36 | **38.04** | **0.929467162** |
| `8 × 8` | 64 | 29.896 | 17.373683 | 20.288 | 30.432 | 0.740916976 |
| `6 × 10` | 60 | 22.288 | 21.7661638 | 15.216 | 22.824 | 0.552366791 |
| `3 × 20` | 60 | 10.876 | 43.7285681 | 7.608 | 11.412 | 0.269541512 |

**The footprint ordering reverses.** `C-0120` reports `15 × 4` at `38.08 × 38.04` nm — *"essentially
§3's"* — and charges `10 × 6` *"a third of the footprint"* at `38.08 × 25.36`. On the honeycomb the
in-plane pitch is 1.5× larger at **every** `m`, and `10 × 1.5 = 15` exactly, so **the corrected
`10 × 6` tile has bit-for-bit the `edgeY` the corpus attributed to `15 × 4`**: `38.04 nm`. The
cross-section this programme charges a footprint for **is** §3's footprint; the one it calls
essentially §3's is half again too wide. That is [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md).

## 3. The census — the numbers survive and every reason for them is withdrawn

The tile's face is the one normal to the **thin** cross-section direction, which for both 60-helix
candidates is the face of `n` columns. A face helix `(r, n − 1)` has its `(r, n)` neighbour absent
and its other two present, so:

| | face helices | rooting azimuths **each** | angle from the normal | perpendicular roots | across-helix pitch | stations at 112 bp |
|---|---|---|---|---|---|---|
| **`15 × 4`** | **15** | **1** | **30.0°**, sign alternating | **0** | **3.804 nm** | **90** |
| **`10 × 6`** | **10** | **1** | **30.0°**, sign alternating | **0** | **3.804 nm** | **60** |

- **`C-0122`'s 90 and 60 reproduce at departure `0.0`** — and its perpendicular/oblique split
  (8/7 and 5/5) is withdrawn: **there is no perpendicular root anywhere on the face.**
- **`CH-0151`'s 132 and 90 do not hold.** Its `±60°` pair belongs to a helix whose two *up-oblique*
  neighbours are absent; on a full `m × n` block those neighbours are the **other sublattice's
  helices in its own row**, which are present. The two-azimuth helix exists only in a **half-row
  termination** the published designs do not have.
- **`C-0128`'s 60° is the wrong angle.** On the real face `κ(30°) = 0.75 + 0.25 A` against
  `κ(60°) = 0.25 + 0.75 A`, so its rigid-body oblique cost of **6.017×** becomes **2.67233333×** at the
  same `A` — the correction is **favourable** and its verdict (`C-0118`'s flatness is not spent by
  the azimuth) is strengthened, not overturned. That is
  [`CH-0175`](../challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md).

**And the stagger is FORCED.** The two face sublattices carry their free azimuth on two **different
bond classes**, whose crossover residues differ by 7 or 14 bp mod 21 — never 0. So adjacent station
rows are offset along the helices by **2.38 nm**, and **no honeycomb face has its station rows in
register.** `CLAUDE.md` records that an 8 bp connectivity stagger is a **first-order** symmetry break
on the square lattice and has to be designed in; here it cannot be designed out.

## 4. The plan ceiling

`maximumPlanCeilingForCount`'s exact bisection, on the honeycomb lattice, at 112 bp, offset 7,
phase 0. It reproduces `C-0072`/`T-136`'s **9.535 nm** at 30 roots on the square phase-24 lattice at
departure **`0.0`**, through the same function.

| paths | `15 × 4` (of 90) | `10 × 6` (of 60) |
|---|---|---|
| 10 | 38.08 | 38.08 |
| 15 | 38.08 | 16.66 |
| 20 | 16.66 | 16.66 |
| 30 | 16.66 | **9.52** |
| 34 | **9.52** | 5.872 |
| 45 | **9.52** | 4.604 |
| 50 | 5.872 | 4.604 |
| 60 | 5.872 | **2.38** |
| 75 | 4.604 | — |
| 90 | **2.38** | — |

`C-0069`'s recommended output element is **8.16439083 nm** long, so the honeycomb affords it at up to
**45 paths on `15 × 4`** and **30 on `10 × 6`**, and refuses it above.

## 5. The centro-symmetric placement family, and the parity that decides it

Rows `r` and `m − 1 − r` of a face have the **same** parity when `m` is odd, so the reflection maps
a row onto one carrying the **same** ladder phase — which the forced stagger cannot satisfy. When
`m` is **even** the reflection **swaps** the two phases and the stagger is exactly what makes the
symmetry available.

| | rooting helices | offset 7 | offset 14 | offset 0 (the counterfactual) |
|---|---|---|---|---|
| **`15 × 4`, 112 bp** | 15 | **none** | **none** | phase 14, **75** of 90 stations |
| **`15 × 4`, 119 bp** | 15 | **none** | **none** | phase 7, **90** of 90 |
| **`10 × 6`, 112 bp** | 10 | phase 0, **60 of 60** | phase 7, **60 of 60** | phase 14, 50 of 60 |
| **`10 × 6`, 119 bp** | 10 | phase 14, **60 of 60** | phase 0, **60 of 60** | phase 7, 60 of 60 |

Every phase of every case is enumerated — 21 phases × 3 offsets × 2 row lengths × 2 cross-sections
— and where a symmetric lattice exists, `centroSymmetricPlacementsOn` streams **more than 20 000**
members at two roots per row and a 3 nm arm, so the family is not merely non-empty.

**`C-0063`'s whole placement family exists on one of the two cross-sections and provably not on the
other**, and that is a third independent reason to prefer `10 × 6`, after `C-0120`'s rigidity and
§2's footprint. It is also the answer to the question the corpus has been asking of the row length:
the counterfactual column shows that **119 bp keeps all six stations per row where 112 bp costs
one** — a `C-0136` result reaching a `C-0063` question.

## 6. `F3` fired, and it is `CLAUDE.md`'s own lesson from the other side

The cheap bound predicted a **tighter** plan budget on the honeycomb, and it is right only at
**saturation**. A placement below the station count **skips** stations, so the binding pitch is a
multiple of 21 bp: at 45 of 90 paths the honeycomb affords **9.52 nm** against the square lattice's
8.19 nm inboard budget, and only at 90 of 90 does it fall to **2.38 nm**, below the 4.604 nm the
inboard bound alone predicts (the tile **edge** binds there, not the neighbour).

**A denser ladder is a larger choice set, not a tighter one** — it costs only where every station is
spent. The inboard bound is a bound on a *saturated* row and was quoted as a bound on a *lattice*;
the eleventh instance in this repository of **quote it with the state it is read at**.

## 7. What the geometry costs the standing four-layer verdict

Re-solved at the corrected cross-section, with **only** `edgeY`, the in-plane pitch and the layer
spacing moved and the rest of `C-0120`'s construction bit-identical:

| | `edgeY` | `D_∥` | `D_⊥` | free-tile dishing | `f*` | at the band low `f = 0.26` |
|---|---|---|---|---|---|---|
| `15 × 4` standing | 38.04 | 4547.17603 | 240.931249 | **0.0577199433** | 0.0788618807 | 0.0612595739 flat |
| **`15 × 4` honeycomb** | **57.06** | 2334.05068 | 278.255762 | **0.0978155002** | **0.276970522** | **0.101759944 — NOT FLAT** |
| `10 × 6` standing | 25.36 | 15189.564 | 804.816135 | **0.00874363524** | none | 0.00927188486 flat |
| **`10 × 6` honeycomb** | **38.04** | 7685.47603 | 916.230312 | **0.0240648102** | **0.012737738** | **0.0255589305 flat** |

`C-0120`'s `0.0577199433` and `0.00874363524` reproduce at departure `0.0` at the geometry they were
solved on, so the movement is the geometry and nothing else.

**The consequence is a verdict, not a number.** `C-0116`'s composite-fraction threshold for
`15 × 4` moves from `0.0788618807` to **`0.276970522`**, which is **inside** the measured band
0.26–0.33 — so at the low end of the only interlayer-coupling calibration anybody has measured the
`15 × 4` four-layer tile dishes **0.101759944** and **fails `T-5b`**. `10 × 6`'s threshold moves to
`0.012737738`, still **20×** below the band, and it stays flat across the whole of it. **`C-0120`'s
central finding — that the second cross-section removes the dependency on the interlayer-coupling
calibration — survives the correction and `15 × 4`'s margin does not.**

## 8. The five gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | lengths nm, areas nm², rigidities pN·nm, angles degrees, dishing dimensionless; the cell area is asserted equal to the product of the two pitches |
| **limiting cases** | a `1 × 1` block is free on all three azimuths and carries exactly one rooting azimuth; `layers = 1` leaves the parallel-axis factor at 1; a block refuses a non-positive size |
| **symmetry / conservation** | every bond is asserted to be exactly `d` long in the emitted coordinates and the neighbour relation is asserted symmetric at every site of a 6 × 6 block; `F4`, the standing falsifier, holds at `4.1e−11 … 1.2e−10` over four solves |
| **numerical convergence** | nested beam subdivisions 1/2/4 (departure `8.3e−5`, `6e−6`, 0) and dishing samples 41/81/161 (departure 0 throughout) on the corrected `15 × 4`; the plan ceiling and the family enumeration are exact rather than convergent |
| **literature cross-check** | the cross-section is read directly from the caDNAno paper's own corrugation sentence and Figure 2 caption, already in the repository; four reproductions at departure **`0.0`** (`C-0122` ×2, `C-0072`, `C-0120`) |

## 9. Validity range, and what this does NOT establish

- **The cross-section is a LATTICE statement.** No folded object is measured; what is read from the
  source is the nomenclature and the corrugation, and the geometry follows from the honeycomb.
- **The census counts ONE face.** The opposite face carries the same inventory pointing **into** the
  grafted layer and is unusable — `C-0055`'s `WEST` azimuth, on a slab.
- **The inter-row ladder offset is 7 or 14 bp and this repository cannot yet say which.** Both are
  carried and **no answer here depends on the choice**. It is **not** the scaffold turn sense
  `T-218` settles, which is a different variable; if `T-218` also fixes the staple bond-class
  residues, the two readings collapse to one and every number in §5 stands as published.
- **The dishing rows re-grade the SMEARED equivalent sheet.** The grillage is still single-layer and
  still square-lattice in its crossover combinatorics; only `edgeY`, the in-plane pitch and the layer
  spacing move. **`C-0118`'s 16-cell dropout grading is NOT re-run**, so no 90th-percentile number is
  produced here and its verdict is neither reproduced nor overturned at the corrected geometry.
- **The row length is a live variable and `T-218` is moving it.** §5's station counts and §4's
  ceilings are read at 112 bp (`C-0119`) and 119 bp (`C-0136`), and `CH-0172`/`CH-0173` are
  challenging both. **Nothing structural here depends on it** — the azimuth, the 30°, the forced
  stagger, the parity rule and the cross-section are row-length-free — but the **numbers** in §4 and
  the station counts in §3 and §5 are not, and they must be re-read at whatever width `T-218`
  settles on.
- **`C-0022`'s collar is read unchanged at both aspect ratios**, and both `edgeY` values have now
  moved by 1.5×, which reopens `C-0123`'s question rather than answering it.
- **Is `C-0118`'s path count a demonstration now?** **On the count and the positions, yes** — the
  stations exist, they are on a derived lattice, the plan ceiling admits `C-0069`'s element at the
  relevant counts, and a centro-symmetric family exists on `10 × 6`. **On the tile, not yet**: the
  cells were graded at a cross-section that is not a honeycomb, and re-grading them is the one thing
  that would close it end to end.
