# C-0152 — **A FORCED honeycomb scaffold crossover costs `0.350894669 k_BT` at the ceiling — SUB-THERMAL — and the whole of `CH-0188`'s forcing costs `0.438634952` of ONE crossover column of the host sheet.** The cheap bound decided it and it turned on an arithmetic nobody had run: the departure is an **AZIMUTH**, and because one turn is **10.5** bp the smallest one the 21-residue lattice offers is **half** a base-pair step — `17.1428571°`, at a displacement of **ten or eleven** base pairs, not one (and this raster takes the **ten**). That span is `0.962320075 nm`, `8.24 σ` of `T-71`'s **measured** phosphodiester step, so a forced crossover does **not** close as a bond at rigid geometry and its price is elastic; the rigid-duplex limit of `C-0104`'s own roll mapping is then a strict **ceiling** and it is one multiplication. **No published price exists** — 68 queries in 7 families, and the source that *defines* the operation says in its own discussion that its structural consequences are **not predicted** — so the answer is delivered as `P-6`'s ceiling and threshold. The threshold is **quantised by the lattice**: the next rung up, a 1 bp displacement, costs `10.5268401 k_BT` for the block, **1.31590485×** the calibration, so the departure this raster needs sits a **factor of two** below the first rung that would exceed it. And the lattice supplies its own empirical calibration for free — under the exact 10.5 bp/turn geometry an **ALLOWED** scaffold crossover already carries `8.57142857°`, which every honeycomb origami ever folded absorbs, and a forced one adds exactly **twice** that

| | |
|---|---|
| **Tasks** | [`T-246`](../tasks/T-246-forced-scaffold-crossover-price.md) — price a forced scaffold crossover, or record that this repository cannot |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (closed-form rigid-body geometry on the honeycomb crossover-residue lattice, against `T-71`'s measured backbone survey) **+ literature** (a recorded negative existence result with its query strings, and one published sweep converted onto the same axis) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Nothing here is measured on a folded object. What *is* measured and consumed: the phosphodiester step and phosphate radius (`T-71`, 13 084 crystallographic linkages), the honeycomb SAXS lattice constant, and the host sheet's own pair interaction (`C-0079`). **An elastic energy is not a folding yield** — see §7. |
| **Verdict** | **PASS on all seven predicates. None of `F1`–`F5` fired.** `F3` and `F5` are written the favourable way round — *"a forced crossover does NOT close as a bond"* and *"no published price exists"* — so their **not** firing is the finding in both cases. |
| **Provenance** | [`gpd/results/T-246-forced-scaffold-crossover-price.json`](../results/T-246-forced-scaffold-crossover-price.json) (`tile.ForcedCrossoverPriceStudyKt`); model [`tile/ForcedCrossoverPrice.kt`](../../src/main/kotlin/tile/ForcedCrossoverPrice.kt), tests [`tile/ForcedCrossoverPriceTest.kt`](../../src/test/kotlin/tile/ForcedCrossoverPriceTest.kt) (**25**, written first and watched fail). Literature corpus and every raw response retained in [`gpd/data/T-246-sources/`](../data/T-246-sources/), with `queries.md` and `MANIFEST.md`. Geometry consumed **unmodified** from [`structure/HoneycombTurnLoop.kt`](../../src/main/kotlin/structure/HoneycombTurnLoop.kt) (`C-0147`) and [`tile/HoneycombBondClassResidues.kt`](../../src/main/kotlin/tile/HoneycombBondClassResidues.kt) (`C-0148`). |
| **Conditions** | Honeycomb interhelical distance `d` = 2.536 nm; **measured** phosphate radius `r_P` = 0.9086378584708424 nm (`T-71`), steric floor `2r_P` = 1.8172757169416849 nm; rise 0.34 nm/bp; caDNAno honeycomb twist 10.5 bp/turn, so `240/7` = 34.2857143° per base pair and 21 bp = **720° exactly**; scaffold offset ±5 bp; measured C2′-endo step 0.664480580 nm, SD 0.036162985, P99 0.756744753; `C` = 460 pN·nm²; `k_θ` = `Gen1Tile.crossoverHingeStiffness(α)` over its own `α` = 0.6–1.2 bracket (8.11764706–16.2352941 pN·nm/rad); crossover spacing swept at 7 bp and 21 bp; `k_BT` = 4.141947 pN·nm at 300 K, aqueous 2 mM MgCl₂ (the buffer `C-0079`'s calibration is read at). Rasters 112/108, 101/109, 102/109, 112/109, 122/119; cross-sections `10 × 6` and `15 × 4`; first axial sign ±1, mirrored-and-reversed composed as a proper rotation. |
| **Consumes** | [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md), [`CH-0188`](../challenges/CH-0188-the-recommended-raster-does-not-close.md), [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md), [`C-0140`](C-0140-honeycomb-raster-turn-sense.md), [`C-0104`](C-0104-row-end-prestrain.md), [`C-0079`](C-0079-unbonded-duplex-separation.md), `T-71` |
| **Constrains** | **`CH-0188`'s severity is now STATED rather than assumed**, and its geometric verdict is upheld unchanged. **Two challenges are raised.** [`CH-0196`](../challenges/CH-0196-the-eight-base-pair-yield-cost-is-an-elided-attribution.md) against `C-0055`'s Deliverable 6 and `CLAUDE.md`'s entry built on it; [`CH-0197`](../challenges/CH-0197-an-allowed-scaffold-crossover-is-not-aligned.md) against `C-0147`'s `F1` reading. |

---

## 1. The cheap bound, and it decided the task

A forced crossover is one placed at a residue caDNAno's default rule does not allow. What that
*costs* is not a count of base pairs — it is an **azimuth**, because displacing a crossover rotates
both backbones (the two helices are parallel and same-handed, so both rotate the same way by the
same angle).

caDNAno fixes the honeycomb twist at 10.5 bp per turn, so one base pair is `240/7 = 34.2857143°`
and the 21 bp residue period is **720° exactly**. The map from a residue departure `k` to an
azimuth is therefore `fold(k · 240/7)`, and it has a consequence nobody had run:

| `k` (bp) | 0 | **1** | 2 | 3 | 4 | 5 | … | 9 | **10** | **11** | 12 | … | 20 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| azimuth | 0° | **34.29°** | 68.57° | 102.86° | 137.14° | 171.43° | | −51.43° | **−17.14°** | **+17.14°** | 51.43° | | −34.29° |

**The smallest nonzero azimuthal departure the lattice offers is `17.1428571°` — HALF a base-pair
step — and it is reached at TEN and ELEVEN base pairs of displacement, not at one.** Because one
turn is 10.5 bp, a displacement of 10 or 11 is one whole turn either side of exact. So *the
cheapest forcing is the one displaced furthest in base pairs*, and no count of base pairs can see
it. `F2` did not fire: the minimum is reached at 10 and 11 and nowhere else, and the map is
injective on the 21 residues.

**And that is exactly what `CH-0188`'s raster needs.** At 112/108 the reduced residues are
`{0, 10, 11}` and the best `b₀` admits `{0, 10}`, so the stray residue 11 is **1 bp** from allowed
residue 10 and **10 bp** the other way round the ring from allowed residue 0 — `34.29°` against
`17.14°`. The census takes the ten, at every one of the four conventions and both cross-sections
(`classZeroResidue` 5 or 12, departures `±10` depending on the axial datum).

## 2. It does not close as a bond, so there is something to price

A scaffold crossover carries **zero** unpaired nucleotides, so its span must fall inside **one**
phosphodiester step. The span is `C-0147`'s own model at `(θ, 180° + θ)`, consumed unmodified, and
it reproduces that claim's two endpoints to the last ulp — `d − 2r_P` at 0° and `d + 2r_P` at 180°.
`F1` did not fire.

| | azimuth | span [nm] | σ of the measured C2′-endo step | span / P99 | closes? |
|---|---|---|---|---|---|
| an **allowed** crossover, caDNAno's idealisation | 0° | **0.718724283** | **+1.49997857** | 0.949757868 | **yes** |
| an **allowed** crossover, exact 10.5 bp/turn | 8.57142857° | 0.787091706 | +3.39051453 | 1.04010197 | **no** |
| a **forced** crossover, the minimal rung | **17.1428571°** | **0.962320075** | **+8.2360318** | **1.27165741** | **no** |
| a forced crossover displaced 1 bp | 34.2857143° | 1.45539014 | +21.8706935 | 1.92322462 | no |

`F3` did not fire, and its not firing is the finding.

> The `8.57142857°` row's `span / P99` is **constructed here**, `0.787091706 / 0.756744753`, from
> two fields the file emits (`allowedCrossoverReadings[1].spanNm` and `parameters`); the file emits
> the ratio only on the integer-base-pair ladder. Every other cell of the table is an emitted field.

**Nor can the axes pay it alone, and that is a closed form rather than a search.** The span is
minimised over the interhelical distance at `d = 2r_P cos θ`, giving `2r_P |sin θ|` — so *"can an
approach close this bond at all"* is a comparison. At the minimal rung it can: the axes would have
to close from 2.536 nm to **2.12974658 nm** (a **0.406253424 nm** approach, 16.0 % of the lattice
constant) to reach the measured mean step, which is above the 1.81727572 nm steric floor. At a 1 bp
displacement **no separation whatever** reaches it — the smallest reachable span is 1.02370786 nm
against a P99 of 0.756744753 — and the root-finder returns `null`, which is a **verdict**.

## 3. The ceiling, and it needs no solve

The departure must be absorbed by deformation, and **any admissible channel's cost is an upper
bound on the true price, because the structure minimises over channels.** `C-0104`/`T-182` already
map an azimuthal register error at a crossover onto a relative **roll**, penalised by `k_θ` and
relieved by the duplex's own torsion over `λ = √(Cp/k_θ)`. A localised defect is those two springs
in **series**, so the **rigid-duplex limit `½ k_θ θ²` is a strict ceiling** over the whole series
and it is one multiplication.

| | per forced crossover | all 10 | over one host-sheet crossover column |
|---|---|---|---|
| **ceiling** — rigid duplex, `α` = 1.2, exact-geometry baseline | **0.350894669 `k_BT`** | **3.50894669 `k_BT`** | **0.438634952** |
| relieved by duplex torsion at 7 bp | 0.306481308 | 3.06481308 | 0.383116147 |
| relieved at 21 bp | 0.280491789 | 2.80491789 | 0.350628018 |

**The ceiling is SUB-THERMAL** — `2.84985806×` below one `k_BT` — and the whole block's forcing costs
**0.438634952** of what **one** crossover column of the demonstrated host sheet already pays to hold
its own two duplexes at the SAXS 2.69 nm (`C-0079`, `7.99969697 k_BT`), and origami folds. `F4` did
not fire.

**The two readings of the `±5 bp` rule differ by EXACTLY a factor of two, and that is arithmetic
rather than a bracket.** caDNAno writes *"five base pairs, or half a turn"*, but the exact half turn
at 10.5 bp/turn is **5.25** bp — so an allowed scaffold crossover is `x = 8.57142857°` off the line
of centres and a forced one is `3x`. The excess is `8x²` against the idealised reading's `(2x)² = 4x²`,
at every stiffness and every `α`. The expensive reading is the one quoted throughout.

## 4. The threshold, and the LATTICE quantises it

| axis | here | what would change the answer | factor | licensed |
|---|---|---|---|---|
| energy, per crossover | 0.350894669 `k_BT` | 1 `k_BT`, the fold's own currency | 2.84985806× | **yes** |
| energy, whole block | 3.50894669 `k_BT` | 7.99969697 `k_BT` (`C-0079`) | 2.27980009× | **yes** |
| `k_θ` | 16.2352941 pN·nm/rad | 37.0132249 | 2.27980009× | **yes** |
| **azimuthal departure** | **17.1428571°** | 25.8840118° — which falls **between two rungs** | — | **yes** |
| published folding outcome | 17.14° at 10 of 59 | 34.2857143° at **every** interface, and it folds | ½ | no |
| flatness prestrain | 17.14° | `C-0104`'s 15.4497275° | 1.10958961× | **no** |

**The departure axis is discrete, so the threshold is bracketed rather than crossed.** The energy is
quadratic, so 2.27980009× of energy is 1.50990069× of departure — and the lattice has no rung
there. The **next** rung is a 1 bp displacement, `34.2857143°`, which supplies `2×` the departure and
`4×` the energy: **10.5268401 `k_BT`** for the block, **1.31590485×** the calibration. So the
departure `CH-0188`'s raster actually needs sits a **factor of two below the first rung that would
exceed the fold's own demonstrated currency**, and the rung that would exceed it is the one the
azimuth arithmetic of §1 says a designer never has to take.

## 5. The lattice supplies its own empirical calibration, for free

Read at the exact 10.5 bp/turn geometry, **an ALLOWED scaffold crossover already carries
`8.57142857°`** — 0.25 bp off the exact half turn, on either side, because caDNAno's `±5` is an
integer approximation to 5.25. Its span, 0.787091706 nm, is `+3.39051453 σ` and **outside** the
measured 99th percentile.

So the rigid model is already at its limit for a crossover that demonstrably folds, and the
structure **absorbs `8.57142857°` at every scaffold crossover of every honeycomb origami ever
built**. A forced crossover of the minimal rung adds exactly **twice** that. This is a calibration
the lattice hands over for nothing, and it needs no measurement that does not already exist —
which is also why `CH-0197` is raised against `C-0147`'s aligned reading.

## 6. The literature — a recorded negative, and one ladder the answer sits under

**No published price for a forced crossover exists.** 68 queries in 7 declared families —
54 EuropePMC (8 s spacing, retried), 10 arXiv, 4 OpenAlex, 8 direct fetches — 14 candidate papers
examined, **13 read directly**, 1 abstract only. Every raw response is retained in
`gpd/data/T-246-sources/`, with `queries.md` recording each query verbatim with its endpoint, hit
count and raw-file name. Representative hit counts: `"forced crossover" AND "DNA origami"` **0**;
`"DNA origami" AND "off-register"` **0**; `"irregular crossover"` / `"unconventional crossover"` /
`"arbitrary crossover"` **0** each; `"design rule" AND violation` **0**;
`"canonical DNA geometry"` **1**, and it is the caDNAno paper itself.

**And it is an EXPLICIT EXCLUSION rather than a null search**, which `CLAUDE.md` records as far
stronger evidence. Douglas et al. (`PMC2731887`, **read directly**), in their own Discussion:

> *"caDNAno provides tools to introduce deviations from the basic honeycomb architecture, such as
> **forced crossovers**, to create very complicated designs. **Additional software development will
> be required** to make designs of these non-standard motifs more natural, for example for caDNAno
> **to predict the structural consequences of these changes**. **More work is also needed to see
> what design rules lead to stable structures**."*

Seventeen years old and unanswered. The cadnano.org gallery the same sentence points to for
*"designs that folded successfully, although with varying yields"* was checked live **and** at its
2012 Wayback capture: three citations and three `.zip` files, **no yields on the page at all**.

**What does exist is one systematic sweep on the same axis**, and it is the only one. Ke, Bellot,
Voigt, Fradkov & Shih (*Chem. Sci.* **3**:2587, `PMC3957201`, **read directly**) underwind a
honeycomb 24-helix bundle by inserting base pairs into its 21 bp crossover period, so each rung is
a whole number of base pairs of azimuthal register per interface:

| bp/turn | bp per interface | inserted | azimuth per interface | × this study's forced crossover | outcome |
|---|---|---|---|---|---|
| 10.5 | 21 | 0 | 0° | — | the control |
| 11.0 | 22 | 1 | **34.2857143°** | **2.0** | *"appeared to decrease the efficiency of folding"* — **and the sign reverses between two architectures of the same laboratory**: *"the previously reported 60hb folded better at 11.0 bp/turn"* |
| 11.5 | 23 | 2 | 68.5714286° | 4.0 | *"folding performance on par with 24hb underwound to 11.0 bp/turn"* |
| 12.0 | 24 | 3 | 102.857143° | 6.0 | *"abolished productive folding completely"* — and the authors name the mechanism: *"the penalty from torsional strain energy"* |

**A forced crossover here is HALF the bottom rung of the only published ladder, and it is applied to
10 of 59 interfaces rather than to all of them.** It is **not a transfer** — their departure is
global and relieved by the duplex actually adopting the non-canonical twist, this one is local and
unrelieved — and what it licenses is a **scale**, not a verdict.

**Two related published results were checked and are about something else**, which is why both are
raised as challenges rather than used:

- **Ke et al. 2009** (`PMC2821935`, **read directly**) observe the lower yield and then decline the
  attribution in the very next sentence — *"Alternatively, simply having a large number of layers
  with our default crossover pattern may be destabilizing, **irrespective of the position of the
  breaks**"*, and *"**Future systematic studies will be required** to determine the relative
  importance of these staple breaks"*. See `CH-0196`.
- **Ke et al. 2012 hexagonal/hybrid** (`PMC3336742`, **read directly**) is the closest thing to a
  demonstration that caDNAno's **forcing tool** builds folding structures — *"all crossovers need to
  be manually implemented in caDNAno for hexagonal-lattice or hybrid origami"*, folding *"with high
  yields"* — but it is a **lattice** change, not an off-register position, and **no percentage is
  stated anywhere**.

## 7. What is still not priced, and it is named rather than assumed

- **An elastic energy is not a folding yield.** Folding is kinetic and cooperative. The ceiling says
  a forced crossover of the minimal rung **cannot be argued out of a fold on elastic grounds**; it
  says nothing about a kinetic trap, and no published measurement of one was found.
- **The ceiling is a ceiling WITHIN `C-0104`'s mapping.** A mechanism outside it — base-pair
  unstacking, backbone strain, a local melt — is not bounded by it.
- **The FLATNESS channel cannot be evaluated here at all.** The departure is `1.10958961×`
  `C-0104`'s 15.4497275° threshold, and that threshold is a **uniform** prestrain on **all 56**
  crossovers of a **single-layer square-lattice** tile at one placement. `CLAUDE.md` records that
  `OrigamiGrillage` never reads `layers` or `interlayerCoupling` and that `CrossoverLayout`'s
  two-parity alternation makes the crossover combinatorics square-lattice, so **a honeycomb
  prestrain solve does not exist in this repository**. The number is quoted to say *which*
  measurement is owed, never to transfer a verdict.
- **Every raster crossover sits at a row TURN**, which is the block's axial **rim** rather than its
  gap-facing face. That is an observation and not a result: `C-0147` proved the coefficient is zero
  for the **raggedness**, and nothing here proves it for a prestrain.

## 8. What it means for `CH-0188`

**`CH-0188`'s geometric verdict is untouched and is consumed unmodified**: the 112/108 raster does
not close, 102/109 does, and this study reproduces 10 of 59, 8 of 59 and 0 at departure `0.0` before
reading anything new off the lattice.

**What changes is the SEVERITY.** On the one axis this repository can price, forcing ten crossovers
is sub-thermal and cheap against the fold's own demonstrated currency. So scaffold closure is a
**reason to prefer 102/109** — a free improvement on a hard rule — and **not a proof that 112/108 is
unbuildable**. `C-0148`'s conservative reading (*"buildable and off-rule"*) is upheld, and it is now
a measurement rather than an assertion. The unpriced risk is **kinetic** and **flatness-side**, and
both are now named, bounded where they can be bounded, and refused where they cannot.

## 9. Verification gates

| gate | what was checked | verdict |
|---|---|---|
| **1 — dimensional** | angles folded to `(−180, 180]` and converted once at the API; `½ k_θ θ²` in pN·nm/rad × rad² = pN·nm; `2C/λ` with `λ = √(Cp/k_θ)` in pN·nm²/nm = pN·nm/rad; `k_BT` = 4.141947 pN·nm named at every conversion | **PASS** |
| **2 — limiting cases** | the span reproduces `d − 2r_P` at 0° and `d + 2r_P` at 180° to the last ulp; it is **even** in the azimuth and **monotone** on `[0, 180]`; `2r_P\|sin θ\|` asserted against a 40 001-point scan of the separation at four azimuths; the series stiffness tends to `k_θ` as `C → ∞` and to zero as `C → 0`; the energy is quadratic and vanishes at zero departure | **PASS** |
| **3 — symmetry and conservation** | 21 bp × `240/7` = **720° exactly**; the residue→azimuth map is **injective** on all 21 residues; the minimum is at 10 **and** 11 with opposite signs; the closure census is invariant across both cross-sections, both axial signs and the proper mirror-plus-reversal | **PASS** |
| **4 — numerical convergence** | **there is no convergence axis**: every quantity is a closed form in exact integer or elementary arithmetic — no mesh, no timestep, no sampling, no minimiser. The one inversion (the interhelical root) is asserted back through the span it was solved from at **8.4e−16**. Two independent runs of the study are **byte-identical** | **PASS** |
| **5 — literature cross-check** | caDNAno's rule and both of its scope statements read directly from `gpd/data/T-151-sources/`; the twist ladder read directly from `PMC3957201`; `C-0079`'s calibration read at run time out of `gpd/results/T-139-duplex-pair-separation.json` and asserted equal to the constant at `0.0`; every quotation verified character-for-character against its retained source file | **PASS** |

| | task | statement | fired | note |
|---|---|---|---|---|
| **`F1`** | `T-246` | the span identity reproduces `C-0147`'s two endpoints | **no** | both to the last ulp, so no new convention enters |
| **`F2`** | `T-246` | `17.142857°` is the smallest nonzero departure the lattice offers | **no** | reached at 10 and 11 bp and nowhere else |
| **`F3`** | `T-246` | a forced crossover does **not** close as a bond at rigid geometry | **no** | written the favourable way round; 0.962320075 nm against a P99 of 0.756744753 — **its not firing is the finding** |
| **`F4`** | `T-246` | all ten forced crossovers cost less than one host-sheet crossover column | **no** | 0.438634952 of it |
| **`F5`** | `T-246` | no published yield or stability cost for a forced crossover exists | **no** | 68 queries in 7 families; **one paper falsifies it**, and that would be the better outcome |

## 10. Validity range

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- The ceiling holds **within** `C-0104`'s roll mapping and **within** `Gen1Tile`'s square-lattice
  `k_θ`, swept over its own `α` = 0.6–1.2 bracket. No honeycomb measurement of `k_θ` exists here.
- The departure is derived at caDNAno's **honeycomb** 10.5 bp/turn. The square lattice's `10.67`
  and its 8 bp / 32 bp planes are a different arithmetic and none of §1 transfers to it.
- Both readings of the `±5 bp` rounding are carried and the expensive one is quoted.
- The literature verdict is falsifiable by **one** measurement on a design differing from a control
  only in the lattice position of a crossover.

## 11. Open questions

- What does a four-layer **honeycomb** tile do under a prestrain on 10 of 59 of its **scaffold**
  crossovers? The lattice machinery is single-layer square-lattice, so the question needs a
  honeycomb grillage before it can even be asked.
- Is a forced crossover a **kinetic** cost rather than an elastic one? Nothing here can see a
  folding pathway, and the elastic ceiling explicitly does not bound one.
- Does the forced crossover's position at the raster **turn** — the block's axial rim — give it the
  zero coefficient on flatness that `C-0147` proved for the raggedness?
- Does any cadnano.org gallery design carry a forced crossover, and with what yield? The paper names
  the gallery as the record; the gallery, live and at its 2012 capture, carries no yields.
