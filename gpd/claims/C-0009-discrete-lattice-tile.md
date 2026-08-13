# C-0009 — The tile as a discrete beam-and-hinge lattice: what the continuum plate got right, and where it errs

| | |
|---|---|
| **Task** | [`T-10`](../tasks/T-10-discrete-lattice-tile.md) |
| **Leaves** | `A8.2` (structural rigidity / mode analysis), `A1.2` |
| **Verification type** | in-silico (a beam-and-hinge grillage finite-element model written for this task, calibrated against `C-0006`'s orthotropic Kirchhoff plate on a shared set of physical ingredients) |
| **Verdict** | **PASS** on all seven items of the acceptance predicate. The continuum plate reduction is **upheld for smooth loads and rejected for point-coupled ones**, and the direction of its error is **not** the one `C-0006` predicted for every case. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-10-discrete-lattice-tile.json`, produced by `structure.DiscreteLatticeTileStudyKt`; model in `src/main/kotlin/structure/OrigamiGrillage.kt`; 21 gate-named tests in `src/test/kotlin/structure/OrigamiGrillageTest.kt` |
| **Conditions** | T = 300 K, aqueous buffer with Mg²⁺, `k_BT = 4.142 pN·nm`; 40 × 40.35 nm tile (15 duplexes); 100 pN target force (§3) |
| **Raises** | [`CH-0008`](../challenges/CH-0008-plate-conservative-about-flatness.md) against [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) |
| **Challenged by** | [`CH-0014`](../challenges/CH-0014-layout-sampled-not-swept.md), on four numbers that are maxima or class properties over a **sample** of the staple layout. No verdict below is overturned; the annotations are inline and marked **`CH-0014`** |
| **Consumes** | [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, the sheet parameters, the per-path allowables), [`C-0001`](C-0001-layer-stiffness.md) (three foundation stiffnesses, swept), [`C-0004`](C-0004-poroelastic-drainage.md) (the foundation is drained, so quasi-static elasticity is licensed) |

---

## The lattice

Built from **the same ingredients as `C-0006`'s plate**, so that any difference is functional form and not parameterisation — the same discipline `C-0001` used when it calibrated MWC against de Gennes on a shared `L₀`.

| | |
|---|---|
| duplexes | **15**, at the measured 2.69 nm interhelical distance (SAXS, Fischer et al. 2016) |
| crossover columns | **8**, spaced 16 bp = 5.44 nm, alternating between a helix's two neighbours |
| crossovers per interface | **4** (32 bp = 10.88 nm apart), 14 interfaces → **56 crossovers** |
| elements | Euler-Bernoulli duplex beams (`EI`, `GJ`), crossover torsional hinges (`k_θ`), crossover vertical links (constraint), Winkler springs on each duplex's tributary strip (`k_f d` on `w`, `k_f d³/12` on roll) |
| degrees of freedom | 855 (`w`, `dw/dx`, roll at 19 nodes on each of 15 beams) |
| footprint | 40 × 40.35 nm — a lattice is an integer number of duplexes wide. **The plate is re-run on the same footprint**; the effect against `C-0006`'s 40 × 40 nm is below 0.8 % on every quantity and is reported separately |

### Gate 2 first, because it licenses everything else

Imposing a smooth field on the lattice must cost exactly what the plate charges for it, or the lattice is not a discretisation of `C-0006`'s plate:

| imposed field | ratio, lattice / plate |
|---|---|
| `w = ½κx²` → `D_∥ = EI/d` | **1.000000, exactly** |
| `w = τxy` → `D_k = GJ/(4d)` | **1.000000, exactly** |
| `w = ½κy²` → `D_⊥ = k_θ d/p` | **1.015467** |

The one non-unit ratio is **exactly `56/55.147`** — the integer crossover count over the continuum areal density `1/(dp)` — asserted as an identity to `1e−9`, not accepted as a tolerance. It is two compensating discretisation effects: four crossovers per interface against the continuum's 3.676 (+8.8 %) and fourteen interfaces for fifteen duplexes (−6.7 %). On a lattice sized so the along-`x` count is exact the residual is **exactly `(n−1)/n` in the duplex count**, verified at `n` = 15, 24, 36.

**So the lattice is `C-0006`'s plate at long wavelength, to 1.5 % on one rigidity and identically on the other two.**

---

## The discreteness criterion, corrected

`C-0006` reported `ℓ_⊥/p = 0.26–0.52 < 1` as its validity breach. That comparison pairs the **across**-helix bending length with the **along**-helix hinge spacing — two different directions.
Direction-matched, there are two criteria and neither is the one `C-0006` used:

| `k_f` × | `ℓ_∥` [nm] | `ℓ_⊥` [nm] | `C-0006`'s `ℓ_⊥/p` | **`ℓ_∥/p`** | **`ℓ_⊥/d`** | crossovers in an anchor patch |
|---|---|---|---|---|---|---|
| 0.25 | 12.83 | 5.71 | 0.52 | **1.18** | **2.12** | 7.9 |
| 0.50 | 10.79 | 4.80 | 0.44 | 0.99 | 1.78 | 5.6 |
| **1.00** | **9.07** | **4.03** | **0.37** | **0.83** | **1.50** | **3.9** |
| 2.00 | 7.63 | 3.39 | 0.31 | 0.70 | 1.26 | 2.8 |
| 4.00 | 6.41 | 2.85 | 0.26 | 0.59 | 1.06 | 2.0 |

**The breach is real but milder than `C-0006` reported, and it lives in the other direction.**
`ℓ_⊥/d ≥ 1.06` everywhere — the sheet is always at least one duplex-spacing's worth of continuum across the helices.
`ℓ_∥/p` is the criterion that fails, and it fails for `k_f ≥ 0.5 ×` `C-0001`: the crossover hinges are **further apart along the helices than the sheet can bridge**.
The physically transparent version of the same statement is the last column: **an anchor's influence patch contains 2–8 crossovers**, and 3.9 at the design point. Four elements is not a continuum.

---

## The discrepancy, quantity by quantity

Peak dishing, nominal `k_f`, both models on the same footprint. The last column is what `C-0006` predicted about itself.

| quantity | `C-0006` (40 × 40) | plate (40 × 40.35) | **lattice** | lattice / plate | plate conservative about flatness? |
|---|---|---|---|---|---|
| `D_∥` [pN·nm] | 85.50 | 85.50 | 85.50 | 1.0000 | — |
| `D_⊥` [pN·nm] | 3.345 | 3.345 | 3.397 | 1.0155 | — |
| dishing, uniform load [nm] | 0.000 | 0.000 | **0.000** | — | exact in both, at every `k_f` |
| dishing, 50 % edge taper [nm] | 1.326 | 1.316 | **1.280** | **0.973** | **NO** |
| dishing, 4 anchors [nm] | 2.480 | 2.472 | **2.252** | **0.911** | **NO** |
| dishing, 1 lever [nm] | 18.278 | 18.286 | **22.579** | **1.235** | **yes** |
| thermal dishing RMS [nm] | 1.272 | 1.274 | **1.467** | **1.151** | **yes** |
| thermal point RMS at centre [nm] | 1.365 | 1.364 | **1.433** | **1.049** | **yes** |

Across the whole `k_f` sweep:

| source | lattice / plate, `k_f` ×0.25 → ×4 |
|---|---|
| edge taper (smooth) | 0.994 → 0.944 |
| 4 discrete anchors | 0.984 → 1.039 (0.843 at ×2) |
| **1 concentrated lever** | **1.120 → 1.382** |
| **thermal** | **1.113 → 1.199** |

### The rule the numbers obey

**The plate errs in opposite directions depending on how the load meets the sheet, and the split is between load *entering* at a point and load *leaving* at a point.**

- **Load delivered *into* the sheet at a point** (a lever tether pulling on one duplex) — the lattice is **softer** and dishes **12–38 % more**. `C-0006`'s prediction is confirmed here: the load has to be carried by the one duplex it lands on, and the sheet cannot spread it across `y` at any scale below one duplex diameter.
- **Load *reacted* at a point** (an anchor holding a distributed load) and **smooth non-uniformity** (the electrostatic edge taper) — the lattice is **stiffer** and dishes **1–16 % less**. `C-0006`'s prediction is refuted here. The reason is the same kinematics seen from the other side: the continuum plate is free to curve on scales below the duplex diameter, and a real sheet made of 2 nm rods is not. The plate's `D_⊥` is a smeared rigidity that under-resists at exactly the wavelengths a point support excites.
- **Thermal** — the lattice fluctuates **11–20 % more**, because a lattice's acoustic branch flattens toward the zone boundary and short-wavelength modes are softer than the continuum `D q⁴` extrapolation says.

`C-0006`'s statement — *"a discrete lattice has more shape freedom than the plate that approximates it, not less"* — is **half right**, and it names the wrong half as universal. This raises [`CH-0008`](../challenges/CH-0008-plate-conservative-about-flatness.md).

**It does not change any of `C-0006`'s verdicts.** The largest discrepancy is 38 % and `C-0006`'s smallest rejection margin is a factor of 2.7.

---

## The number `T-5` declined to produce: the peak force at a discrete anchor

`C-0006` reported the exact cut-averaged shear (zero, under a uniform load) and an `ℓ`-contour estimate of **1.9 pN per path under an equal-sharing assumption**, and explicitly declined a peak. Here is the peak.

### Anchored cases, nominal `k_f`

| anchors | `k_anchor/k_f A` | peak anchor [pN] | **peak crossover [pN]** | peak duplex shear [pN] | `C-0006` equal-share [pN] | **concentration** |
|---|---|---|---|---|---|---|
| 1 | 0.1 | 6.41 | 2.38 | 3.12 | 0.69 | 3.5 |
| 1 | 1.0 | 15.15 | **5.63** | 7.38 | 1.62 | **3.5** |
| 1 | 10 | 17.54 | 6.51 | 8.55 | 1.88 | 3.5 |
| 4 | 1.0 | 8.31 | 3.77 | 3.32 | 0.89 | 4.2 |
| 9 | 1.0 | 5.06 | 2.02 | 2.38 | 0.54 | 3.7 |
| 25 | 1.0 | 2.00 | 1.15 | 1.11 | 0.21 | 5.4 |

### The worst case anywhere in the sweep

> **`CH-0014`.** This is the worst over the anchor placements **sampled** here — the anchor is at the tile centre in every case. Swept over the whole unit cell at every column phase, the worst over the same `k_f` range and Chen et al.'s `α` range is **14.65 pN**, i.e. the figure below is **27 % low**. The same correction takes the single-attachment case from 37.14 pN to **50.13 pN**, which is **above** the 48 pN quasi-static duplex-shear allowable. See [`C-0015`](C-0015-crossover-phase-and-registration.md).

**`k_f` × 0.25, one anchor at ten times the layer stiffness: 30.07 pN on the anchor, `11.54 pN` on a single crossover**, against a 2.28 pN equal-sharing estimate — a **concentration factor of 5.06**.

&nbsp;&nbsp;&nbsp;&nbsp;**The concentration factor is 2.3–7.6 across every anchored case, every anchor count and every `k_f` in the sweep.**

### Against the allowables

| allowable | value | worst anchored crossover | verdict |
|---|---|---|---|
| single-duplex **unzip** | **10–15 pN** | **11.54 pN** | **REACHED at the soft end of the `k_f` sweep** |
| single-duplex **shear**, quasi-static | 48 pN | 11.54 pN | 4.2× margin |
| nicked-duplex **overstretching ceiling** | 65 pN | 11.54 pN | 5.6× margin |
| §4(f) 35–60 pN band | — | — | **not used**: it is a whole-cross-section number (`C-0006`'s literature trace), not a per-path allowable |

**So a discretely anchored tile is not comfortably safe, as `C-0006`'s 1.9 pN implied — it is within a factor of 1 of the unzip allowable at the soft end of the foundation sweep, and within a factor of 2 at the design point.**
It remains safe against duplex shear and against the 65 pN ceiling by 4–6×.

### The concentrated lever attachment

| attachments | force each [pN] | peak crossover [pN] | peak duplex shear [pN] | verdict |
|---|---|---|---|---|
| **1** | **100.0** | **37.14** | **48.74** | above unzip; **at** the 48 pN duplex-shear allowable in the duplex itself |
| 4 | 25.0 | 11.34 | 9.99 | above unzip |
| 9 | 11.1 | 4.56 | 5.44 | below every per-path allowable |
| 25 | 4.0 | 2.29 | 2.23 | below every per-path allowable |

The single-lever case was already dead in `C-0006` on three counts. The lattice adds a fourth and a fifth: the duplex it lands on carries **48.7 pN of transverse shear**, at the quasi-static single-duplex shear allowable, and the **four**-attachment case now also exceeds the layer height (the lattice's peak deflection passes 10 nm where the plate's did not).

### How localised it is

The ten most loaded crossovers under a single central anchor, by distance:

| distance from anchor [nm] | 3.03 | 4.87 | 7.25 | 8.27 | 9.10 |
|---|---|---|---|---|---|
| crossover force [pN] | **5.63** | 2.73 | 1.76 | 1.12 | 1.29 |

**The anchor is carried by its two nearest crossovers and essentially nothing else.** That is the whole reason a contour average understates the peak: the contour has 9.3 paths on it and the load uses about two.

### The crossover *phase* is worth 19 %

> **`CH-0014`.** The two lattices below are compared with the load held at the same *absolute* point, which is a **different registration** in each — at eight columns it is 2.72 nm from the nearest column and at seven it is on one. Registration is the larger lever, so the 19 % is mostly registration attributed to the count. Controlled, the column-count effect is **0.3–3.4 %** for a concentrated attachment and **4.5–9.0 %** for an anchor, and **seven columns is the better layout in both** — the opposite sign. The registration lever, swept over the whole `p × d` cell instead of four points, is **×1.43–1.60**, not 30 %. See [`C-0015`](C-0015-crossover-phase-and-registration.md).

Where the crossover columns sit relative to the tile edge is a staple-layout choice, not a physical constant.
Seven columns instead of eight moves the peak crossover force from **37.1 pN to 44.1 pN** under a concentrated lever, and the thermal dishing from 1.467 nm to 1.628 nm.
Where the *anchor* sits within the unit cell is worth another 30 %: 5.11 pN on a crossover, 5.56 pN between duplexes, 5.76 pN mid-span on a duplex axis, **6.66 pN on a duplex axis at a crossover column**.

**This is a free design lever of the same size as the whole `k_f` uncertainty, and nothing in the programme currently owns it.**

---

## Does `no discrete attachment scheme is flat` survive?

**Confirmed, and sharpened — with a lattice-native count that replaces the continuum heuristic.**

`C-0006`'s 55 is `ceil(1.25 A/ℓ_eff²)`, a heuristic, not a solve. Both models are solved here for the smallest square attachment array that keeps peak dishing below 10 % of the stroke:

| attachments | 1 | 4 | 9 | 16 | 25 | 36 | 49 | **64** | 81 | 100 |
|---|---|---|---|---|---|---|---|---|---|---|
| lattice dishing / stroke | 4.60 | 1.39 | 0.68 | 0.36 | 0.23 | 0.19 | 0.19 | **0.096** | 0.075 | 0.063 |
| plate dishing / stroke | 3.73 | 1.42 | 0.65 | 0.35 | 0.22 | 0.15 | 0.11 | **0.080** | 0.063 | 0.051 |

**Both models need 64.** `C-0006`'s heuristic 55 was 14 % optimistic, in the unsafe direction.

> **`CH-0014`.** 64 is the smallest **square** grid. The square diagonal is a one-parameter slice of the `(columns × rows)` design space, and the sheet is 25.6× stiffer along the helices than across them, so it is not the slice a designer would choose. Searched over shapes at the same criterion and the same footprint, the lattice needs **45 (a 3 × 15 grid, 0.80 attachments per crossover)** and the plate 40. At fifteen rows every duplex carries its own attachment, the crossovers stop being load paths, and the peak per-load-path force is **exactly zero**. The statement below therefore **inverts**. See [`C-0015`](C-0015-crossover-phase-and-registration.md).

| count | value | what it is |
|---|---|---|
| `A/(ℓ_∥ℓ_⊥)` | 44.1 | the continuum patch count |
| unit cells `(L_y/d)(L_x/p)` | 55.1 | the continuum crossover density × area |
| **crossovers** | **56** | **the lattice's own count** |
| **attachments flatness needs** | **64** | solved, on both models |
| **attachments per crossover** | **1.14** | |

&nbsp;&nbsp;&nbsp;&nbsp;**Flatness needs more attachment points than the tile has crossovers.** *(`CH-0014`: on square grids only — 45 against 56 once the grid shape is free.)*

That is a stronger statement than `C-0006`'s and a more physical one, because a crossover is *both* the only across-helix load path and the only across-helix compliance. Below one attachment per crossover the load has to travel through the lattice to reach an attachment, and the travel is the dishing. It also does not move when `k_f` is re-derived under `T-1c`: both counts are lattice geometry, not foundation stiffness.

**And the lattice's curve is not monotone.** From 36 to 49 attachments the lattice barely improves (0.192 → 0.187), and at 121 and 196 it gets *worse* than at 100 and 169. Where the attachments land relative to the crossovers matters as much as how many there are — a statement the continuum cannot make at all.

---

## Everything as a function of `k_θ`, the largest open premise

`T-9` is queued to settle `k_θ` by oxDNA and **has not run**. Nothing here rests on the nominal value.

| `α` | `k_θ` [pN·nm/rad] | `D_⊥` | anisotropy | 4-anchor dishing, lattice/plate | thermal dishing, lattice/plate | peak crossover, 4 anchors [pN] |
|---|---|---|---|---|---|---|
| 0.6 | 8.12 | 2.01 | 42.6 | 0.907 | 1.132 | 3.42 |
| **1.0** | **13.53** | **3.35** | **25.6** | **0.911** | **1.151** | **3.77** |
| 1.2 | 16.24 | 4.01 | 21.3 | 0.914 | 1.159 | 3.90 |
| **25.6** (out of range) | **345.8** | **85.50** | **1.00** | **1.296** | **1.460** | **4.89** |

**Over Chen et al.'s entire admissible range the ratios move by less than 1 % and 3 %.** No verdict in this claim depends on `k_θ` within the range that measurement allows.

It takes `α = 25.6` — the stiffness at which `D_⊥` reaches `D_∥` and the sheet becomes isotropic, which is `CH-0005`'s "~30× stiffer" scenario — to flip the anchored verdict, and even there the peak crossover force only rises from 3.77 to 4.89 pN and the concentrated-lever force from 37.1 to 38.3 pN.
**The peak per-path force is nearly independent of `k_θ` altogether**, because the load path from an anchor into the sheet is dominated by duplex bending, not by hinge rotation.

---

## The five verification gates

Executed as tests: `src/test/kotlin/structure/OrigamiGrillageTest.kt`, 21 tests, each named for its gate. Full detail in [`T-10`](../tasks/T-10-discrete-lattice-tile.md#the-five-gates).

- **Gate 1** — a rigid translation stores exactly `½ k_f A` and nothing structural; the area Gram form returns 1, `L_x²/12`, `L_y²/12` and zero cross terms; the crossover count follows from the stated topology.
- **Gate 2** — the three rigidity identities above; a uniform load dishes the lattice by nothing at hinge stiffnesses spanning 10³; a rigid lattice translates under a point load; quadrupling `k_f` quarters the deflection; a softer hinge softens `D_⊥` proportionally and leaves `D_∥` untouched.
- **Gate 3** — force balance to `1e−8`; **the crossovers on one interface carry exactly the shear crossing it**, computed independently from cut equilibrium, to `1e−6`; equipartition exact for a rigid lattice; and the symmetry group **corrected**: the lattice is centro-symmetric and **not** mirror-symmetric, both asserted. *(`CH-0014`: true of **this** eight-column lattice. Centro-symmetry holds exactly when the column count plus the duplex count is odd, so 10 of the 32 base-pair phases of a 40 nm tile are centro-symmetric and **22 have no symmetry at all** — including the seven-column lattice used in gate 4 below.)*
- **Gate 4** — mesh 0.1 %, link penalty 0.01 %; the crossover *count* is not a convergence parameter and is reported as a 19 % physical uncertainty instead. Monotone refinement holds only on nested meshes.
- **Gate 5** — where the plate's own criterion is satisfied (`k_f/200`, `ℓ_∥/p > 3`) the two models agree to better than 10 %, which is what licenses attributing the working-stiffness disagreement to discreteness; and the model class is cross-checked against Li et al., *Chem. Sci.* **14**:8018 (2023).

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Not measured.** No crossover force in a loaded origami sheet has ever been measured, and this claim does not pretend otherwise.
- **`k_θ` is inherited from `C-0006`**, which inherits it from Chen et al.'s fitted spring network in which only `α` was fitted and the `1/100` is CanDo's *nick* softening factor. Swept, and every conclusion shown insensitive to it — but the sweep is over a *fitted model*, not over measurements. `T-9` is what closes this.
- **The vertical crossover link is a penalty.** The reported force is a constraint force, verified converged in the penalty to 0.01 %. Nothing here models the crossover's own axial compliance, which would only add compliance and lower the peak.
- **The duplex transverse shear is a beam internal force, not a staple-domain load.** The per-path rupture allowables were measured on hybridised duplex domains, so the *crossover* force is the number they apply to; the duplex shear is reported against the nicked-duplex ceiling only, and the mode by which a duplex fails under transverse shear (kinking, nick opening) is not modelled at all.
- **A discrete anchor attaches at one point of one duplex.** That is what a tether is, but a tether attached to a *crossover* or to two duplexes at once would spread the load, and the anchor-phase sweep bounds the difference at ~30 %.
- **Linear Winkler foundation**, drained per `C-0004`, with `C-0001`'s three stiffnesses carried separately and swept ×[0.25, 4]. `C-0001`'s numbers are **lower bounds** per `CH-0001` and are being re-derived under `T-1c`; the corrections in flight run toward *softer*, which raises the peak crossover force (it goes as `k_f^(−1/2)` like the anchor force).
- **Kirchhoff-equivalent kinematics**: the lattice cannot curve within one duplex's tributary strip. That is physically right for a 2 nm rod and is precisely the stiffening that refutes half of `CH-0008`'s target — but it is a *kinematic* statement, not a measurement, and a duplex with a compliant cross-section would soften it.
- **No electrostatics is solved**, and the response is linear in the edge-taper depth to five digits, so `T-3`'s load model substitutes without a re-run.
- **Single-layer only.** The four-layer reading of §3 is not attempted here: `C-0006` already reports it as a bound rather than an answer, and Kirchhoff is not safe for it.
- **Static.** Whether the tile can dish fast enough to matter at 1 kHz is `T-7` / `C-0004` territory.

## Numbers that are cited rather than derived

Flagged per §7 of the problem definition.

- `EI = 230 pN·nm²`, `GJ = 460 pN·nm²` — **CITED**, CanDo (Kim et al., *NAR* **40**:2862, 2012); *model inputs* in that paper, not measurements. Inherited unchanged from `C-0006` so the comparison is of form.
- `d = 2.69 nm` — **CITED, MEASURED**, Fischer et al., *Nano Lett.* **16**:4282 (2016), SAXS.
- `p = 32 bp` per interface, 16 bp per helix — **CITED**, Rothemund, *Nature* **440**:297 (2006); the 16 bp per-helix figure independently confirmed verbatim in Li et al., *Chem. Sci.* **14**:8018 (2023).
- `k_θ = 2αB/(100a)`, `α ∈ [0.6, 1.2]` — **CITED, fitted**, Chen et al., *JACS* **136**:6995 (2014) SI. **The single largest open premise, and it is swept.**
- Per-path allowables: 48 ± 2 pN shear (Strunz et al., *PNAS* **96**:11277, 1999), 10–15 pN unzip (Essevaz-Roulet et al., *PNAS* **94**:11935, 1997), 65 pN nicked ceiling (van Mameren et al., *PNAS* **106**:18231, 2009) — **CITED, MEASURED**, all loading-rate dependent, all inherited from `C-0006`'s trace.
- The 40 nm footprint, 100 pN target, 10 nm layer height, `λ_D = 4 nm` — §3.
- `k_f` — **DERIVED** from `C-0001`, itself under challenge (`CH-0001`), swept ×[0.25, 4].
- `C-0006`'s published dishing amplitudes and rigidities — **CITED** from `gpd/results/T-5-load-distribution.json` and `T-5b-tile-flatness.json`, held as constants in the study so the discrepancy is *computed* rather than asserted.

Everything else is derived from these in code.

## Challenges

**Raises [`CH-0008`](../challenges/CH-0008-plate-conservative-about-flatness.md)** against `C-0006`'s validity bullet
*"Every conclusion above is therefore conservative about flatness"*. None stands against this claim.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
The way it would fail is through the crossover's **vertical** compliance, which is modelled as a constraint here: a crossover soft enough in `z` to matter would add a load path the lattice does not have, lower the peak force and soften the sheet toward the plate. Nothing accessible measures it, and `T-9` could produce it at the same cost as `k_θ`.
