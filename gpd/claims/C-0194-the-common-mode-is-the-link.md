# C-0194 — **THE CROSSOVER'S COMMON AZIMUTHAL MODE IS THE VERTICAL LINK, AND `d/2` IS A THEOREM RATHER THAN A FITTED ARM — SO `HoneycombGrillage` DOES NOT MISS THE LARGER OF THE TWO SPRINGS, IT CARRIES IT AS A CONSTRAINT AT `336.800449×` THE PHYSICAL VALUE.** The link's own gradient is `(1, armY, −1, armY)` over `(W_a, Φ_a, W_b, Φ_b)`, `armY = (d/2)·unitY`, so its residual is a function of the **sum** of the two rolls; probed on the recommended `10 × 6` lattice a common roll of 1 mrad at every beam stores **exactly `0.0` pN·nm** in the hinges and **`6.7528608`** in the links, and a rigid roll stores **`0.0` in all three element families**. `d/2` is the **only** arm annihilating the linearised rigid roll `Φ ≡ α`, `W = α y` — the residual an arm `a` leaves is `α·unitY·(2a − d)` — so the span's own `r_P` is not admissible in a linear element, and the difference is an **exact identity**: `d²/(2 g r_P) − d/(2 r_P) = d/g`, i.e. `4.92396953 − 1.39549545 = 3.52847408`. **What IS wrong is a MAGNITUDE, and it is the penalty's**: `CH-0242`'s own premise gives a bond tension `T = 2k_θ/r_P = 29.7795467 pN` and a link stiffness `k_R = T/g = 41.4338953 pN/nm` against `RIGID_LINK_STIFFNESS = 10000`, **`241.348295×`** — and the tension cross-checks, implying an effective phosphodiester-step stiffness of `548.995 pN/nm` over the built span's `0.0542437 nm` excess above `T-71`'s measured C2′-endo step. **The error that carries is MEASURED, not bracketed, because `linkStiffness` is an existing constructor argument**: over six decades **`0 of 6`** (cross-section, coupling) pairs move a free-tile flatness verdict and the worst relative spread is **`0.0380542`**. **But `F10` FIRED — the free tile's insensitivity does NOT transfer to the coupled cells**: `C-0180`'s two recovered cells are flat at `k_link ≥ 1000` and **NOT flat at `100` or at `k_R`**, so the corpus's coupled recovery rests on a link stiffness the span law alone does not supply. **And `C-0190` §6's threshold becomes a VALUE**: because the common mode is the link, the departure is an offset in the link's own residual, `R₀ = d·unitY·ρ` — a load, projection **exactly `0.0`** on the relative roll at all 59 ties — and the free tile reads **`0.0931890716`** at the penalty and **`0.0813628214`** at `k_R`, **`3.18422026×`** and `3.64706455×` **below** `C-0190`'s per-beam twist `0.296735462`, and **inside** `T-5b` where the twist was outside. **`0 of 24` loaded coupled readings are flat**, so `C-0190`'s coupled conclusion is upheld on the corrected channel and the derived magnitude. **And the ratio the corpus quotes is wrong**: `1 + 2r_P/(d − 2r_P) = d/(d − 2r_P) = 3.52847408`, not `3.52810239` — 0.0105 % low in four documents, while `T-291`'s own `openQuestions` block emitted the right one from the same expression

| | |
|---|---|
| **Task** | [`T-297`](../tasks/T-297-the-common-mode-is-the-link.md) — resolving [`CH-0242`](../challenges/CH-0242-the-tie-carries-no-common-mode-stiffness.md) |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (the element decomposition, the frame-indifference theorem that fixes the connector arm, and the exact identity `d²/(2 g r_P) − d/(2 r_P) = d/g` — all algebra on the corpus's own `turnPhosphateSpan` and on four committed lines of `HoneycombGrillage.assemble`, and the whole cheap bound runs with **no solver at all**) **+ in-silico** (the assembled stiffness matrix probed through the lattice's own energy accessors, the free tile swept over `linkStiffness` at both 60-helix cross-sections and all three couplings, and the departure carried as a link eigenstrain and graded through `C-0058`'s exact Woodbury surrogate on `C-0167`'s stations, `C-0087`'s measured incorporation and 4 000 realisations of one common stream) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The bond tension `T = 2k_θ/r_P` is an **attribution**, not a measurement: it assumes, with `CH-0242`, that both eigenmodes of the span form are one mechanism, and `k_θ` is Chen et al.'s fitted **square-lattice** dihedral constant with nothing in it resolved into stacking, backbone and junction geometry. Every number is conditional on the raster's turns carrying **zero** unpaired nucleotides — `C-0175`'s modelling choice, and `C-0193`/[`CH-0247`](../challenges/CH-0247-the-tie-set-is-a-route-not-a-lattice.md) establish that the only folded block of this cross-section does otherwise. |
| **Verdict** | **PASS on all six predicates. `F1`–`F9` did not fire; `F10` was declared OPEN and FIRED, and its firing is the second half of the finding.** `P1` the link residual is a function of `Φ_u + Φ_l`, asserted on the assembled matrix against a closed form at departure `0.0`; `P2` `d/2` is the only frame-indifferent arm, asserted against three others; `P3` the model's common-mode stiffness exceeds `CH-0242`'s physical one by `336.800449×` and `84.2001122×`; `P4` the whole sweep is emitted with its spread and `C-0175` §9's six readings reproduce at the standing stiffness to **`0.0`**; `P5` the link eigenstrain's projection on the relative roll is exactly `0.0` at every tie; `P6` the field converges as the penalty stiffens. |
| **Provenance** | [`gpd/results/T-297-the-common-mode-is-the-link.json`](../results/T-297-the-common-mode-is-the-link.json) (`tile.CommonModeLinkStudyKt`, **new**); model [`tile/CrossoverCommonMode.kt`](../../src/main/kotlin/tile/CrossoverCommonMode.kt) (**new file**); harness [`tools/T-297-mutation-test.py`](../../tools/T-297-mutation-test.py) (**new file**, declared in `tools/P-31-harness-census.py`, `13/13` anchors resolved). **Two shared sources are edited and BOTH edits are pure ADDITIONS**: `tile/HoneycombGrillage.kt` gains **three new public methods** — `turnLinkExtension`, `turnLinkOffsetLoad`, `turnLinkOffsetResponse` — in the shape of `beamTwistResponse` beside them, with **no existing member touched** and none of the three names occurring anywhere else in the tree, so no call site can have moved; and `structure/ResultInputs.kt` gains a `T_297` handle, because the tree's invariant is *every result path spelled in a main source has a handle*. **The proof is not offered instead of the run**: the four nearest consumers of the edited grillage were re-run in `tools/reemission-order.py`'s own topological order — `T-253`, `T-254`, `T-267`, `T-279` — and all four are **BYTE-IDENTICAL**; the study's own 14 reproductions close at **`2.5e−9`**, including `C-0175` §9's six free-tile readings at **exactly `0.0`** and `C-0190` §6's two zero-eigenstrain coupled `p90`s. **Twenty gate-named tests written first and watched fail** — [`tile/CrossoverCommonModeTest.kt`](../../src/test/kotlin/tile/CrossoverCommonModeTest.kt), which did not compile against a model that did not yet exist — and **mutation-tested afterwards**, `tools/T-297-mutation-test.py`, **13 mutations, 0 survivors over a subtracted baseline of 0**, with the subject sources asserted to occur at exactly one path each (`C-0190` §8b). A full `./gradlew test` on the final sources gives **3 367 tests in 194 classes, 0 failures, 0 errors, 0 skipped**, with no task excluded, and `tools/verify.sh` reports **BUILD SUCCESSFUL in 25m 10s**. `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-entry-points.py`, `check-queue-vocabulary.py`, `check-kotlin-format-strings.py`, `check-cold-start-note.py`, `P-31-harness-census.py --check`, `T-278-emitter-rounding-census.py --check` and `result-reader-census.py --check` all exit 0; `gpd/results/P-22-result-reader-census.json` is re-emitted, **additively**. Result file **byte-identical across two independent JVM runs**. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.141947 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); `r_P` = 0.908637858 nm (`T-71`, measured on 13 084 crystallographic linkages); `g = d − 2r_P` = 0.718724283 nm; in-plane row pitch `3d/2`, layer pitch `d√3/2`, rise 0.34 nm/bp. `k_θ` = 13.5294118 pN·nm/rad at `α` = 1; `k_s` = 64.7058824 pN/nm; link ladder **41.4338953, 100, 1 000, 10 000, 100 000, 1 000 000 pN/nm**. Cross-sections `10 × 6` and `15 × 4`, block extent **116 bp = 39.44 nm**, raster `102 / 109` (`C-0151`, drawable), 435 staple bonds and 59 raster turn ties at `s = ±L/2`. `C-0001`'s secant foundation on the gap-facing face only; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; §3's 100 pN over the face; `C-0017`'s mandate at the acceptable clause; `C-0058`'s rim-graded 5:1 at a 6.7 nm band; `C-0087`'s measured depth incorporation; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Allowed departure **8.57142857°**. |
| **Consumes** | [`CH-0242`](../challenges/CH-0242-the-tie-carries-no-common-mode-stiffness.md) — the challenge settled here; [`C-0190`](C-0190-the-departure-is-common-mode-and-what-replaces-it.md) (`T-291`) — the derived assignment, the two deciding cells and the twist reading, **reproduced**; [`C-0175`](C-0175-drawable-raster-rim.md) (`T-254`) §9 — the six free-tile readings, **reproduced at exactly `0.0`**; [`C-0154`](C-0154-honeycomb-grillage.md) (`T-253`) — the lattice, unmodified except by addition; [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md) — `turnPhosphateSpan`; [`C-0187`](C-0187-the-turn-prestrain-sign-is-derived.md), [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md), [`C-0151`](C-0151-closing-raster-selection.md), [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0104`](C-0104-row-end-prestrain.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0001`](C-0001-layer-stiffness.md) |
| **Constrains** | **[`CH-0242`](../challenges/CH-0242-the-tie-carries-no-common-mode-stiffness.md) is UPHELD IN PART and REFUTED in its direction.** Its §1 expansion and its ratio's *form* stand; its §3 premise — *"and nothing else on the azimuthal coordinates"* — is false, and its conclusion that the element set is *missing* the larger spring is reversed: the lattice **over-carries** it by `336.800449×`. **One challenge is raised**, [`CH-0248`](../challenges/CH-0248-the-common-mode-is-the-link.md), against `C-0190` §6's premise and its headline `3.52810239`. **No number of `C-0154`, `C-0175`, `C-0180`, `C-0187` or `C-0190` moves**, and `C-0175` §9's tie deliverable is annotated rather than withdrawn: it is not a lower bound *for the reason `CH-0242` gives*, and at the span law's own link stiffness the ties buy slightly **more** (`0.883711189` against `0.890395426`). **One task is opened**, `T-300`: what the coupled recovery's link-stiffness threshold is, since it lies between 100 and 1 000 pN/nm and the corpus has never swept the penalty at the coupled level. |

---

## 1. The cheap bound is the whole of the first deliverable, and it needs no solver

Three readings, all closed form, all before any lattice is assembled.

| # | question | answer | consequence |
|---|---|---|---|
| 1 | which coordinate of the lattice does a crossover's **common** azimuthal mode live on? | the vertical **link**. `assemble` builds it with the gradient `(1, armY, −1, armY)` over `(W_a, Φ_a, W_b, Φ_b)`, `armY = (d/2)·unitY`, so its residual `R = ΔW + (d/2)·unitY·(Φ_a + Φ_b)` is a function of the **sum** | `CH-0242` §3's premise is false, and the question changes from *add a spring* to *measure what the penalty costs* |
| 2 | is the lattice above or below the physical common-mode stiffness? | **above**, by `336.800449×` in plane and `84.2001122×` through the thickness | the lattice sits at the **rigid** end of the mode, not at the free one |
| 3 | what does the expensive half then cost? | a sweep of an **existing** constructor argument — `linkStiffness` is already a `HoneycombGrillage` parameter | no new element, no new stiffness matrix, and every influence bank in the corpus stands |

## 2. The probe — measured on the assembled matrix, not argued from the source

On the recommended `10 × 6` block with all 59 ties, at the standing penalty:

| state | hinge [pN·nm] | link [pN·nm] | slip [pN·nm] |
|---|---|---|---|
| a **common** roll of 1 mrad at every beam, axes at nominal | **`0.0`** | **`6.7528608`** | `0.0` |
| a **rigid** roll, `Φ ≡ α` and `W = α y` | `0.0` | `0.0` | `0.0` |
| consecutive x-raster rows counter-rolled, so every **in-plane** joint carries the pure relative eigenmode | `0.00389647059` | `2.411736` | `0.0` |

The first row matches the closed form `½ k_link (d·unitY·θ)²` summed over the bonds at a departure
of **`0.0`**; the third's hinge energy is `½ k_θ (2θ)²` over the **144** bonds and ties that join
two adjacent rows, and the file's `hingeEnergy` and `closedFormLinkEnergy` agree to the last
emitted digit; and the worst turn tie's own link extension under the rigid roll rounds to
`0.0` nm at the emitted precision. **Both eigenmodes are present and neither is where `CH-0242`
looked for it.**

## 3. `d/2` is a theorem, and the identity that prices what it costs

The linearised rigid roll of the whole block is `Φ ≡ α`, `W = α y`. The residual an arm `a` leaves
under it is

&nbsp;&nbsp;&nbsp;&nbsp;`α · unitY · (2a − d)`,

zero at **every** bond direction if and only if `a = d/2`. The span's own arm — the phosphate
radius `r_P` — leaves `7.18724283e−4` nm at 1 mrad, so a **linear** element may not use it. That is
`CLAUDE.md`'s standing frame-indifference entry, met on the honeycomb and asserted rather than
recalled.

The price of the theorem is one term, and it is an exact identity:

&nbsp;&nbsp;&nbsp;&nbsp;`d²/(2 g r_P) − d/(2 r_P) = d/g`, i.e. `4.92396953 − 1.39549545 = 3.52847408`.

| reading | value, as a multiple of the relative mode |
|---|---|
| `CH-0242`'s raw expansion at fixed axes, `d/g` | **`3.52847408`** |
| the frame-indifferent linear element at fixed axes, `d²/(2 g r_P)` | **`4.92396953`** |
| the prestress **geometric** term a linear analysis excludes, `d/(2 r_P)` | **`1.39549545`** |

The excluded term is the geometric stiffness of a taut connector under a rotation the reduced
kinematics cannot follow — the model has no in-plane transverse coordinate, so its *"rigid roll"*
keeps the axes' `y` separation and shears the pair. It is **`1.39549545 k_θ`**, against the
link's own `336.800449 k_θ`: smaller by 241×, and therefore not what decides anything.

## 4. What IS wrong, and it is the penalty's magnitude

`CH-0242`'s own premise is that both eigenmodes of the span form are one mechanism. Carried one
step, that fixes the bond tension from `k_θ`:

&nbsp;&nbsp;&nbsp;&nbsp;`E = T[(r_P/4)Δφ² + R²/(2g)]`, so `k_θ = T r_P/2` and `k_R = T/g`.

| quantity | value |
|---|---|
| implied bond tension `T = 2k_θ/r_P` | **`29.7795467 pN`** |
| span-derived link stiffness `k_R = T/g` | **`41.4338953 pN/nm`** |
| `RIGID_LINK_STIFFNESS` | `10000.0 pN/nm` |
| **the ratio** | **`241.348295×`** |

**The tension cross-checks against a measurement nothing here fitted.** The built span
`g = 0.718724283 nm` stands `0.0542437 nm` above `T-71`'s measured C2′-endo phosphodiester step,
and `T` over that extension implies an effective step stiffness of **`548.995 pN/nm`** — half
`Gen1Tile.DUPLEX_STRETCH_MODULUS`, and the right order for a short covalent chain whose
compliance is torsional rather than bond-stretching.

`CLAUDE.md` already records that *a penalty is justified against the bodies it joins and never
against the element it stands in for*. `RIGID_LINK_STIFFNESS`'s own KDoc prices `1e4 pN/nm`
against the duplex **stretch** modulus; a relative transverse displacement of two duplexes changes
the connector's span only at **second** order, so what resists it is `T/g`, not an axial modulus.
The entry is met from the other side.

## 5. The error the approximation carries, measured over six decades

`linkStiffness` is a constructor argument, so the error is six solves of a lattice that exists.
Re-taking `C-0175` §9's own table at `10 × 6` and `f = 0.30`:

| `k_link` [pN/nm] | free tile, no ties | free tile, 59 ties | ratio | inside `T-5b` |
|---|---|---|---|---|
| **`41.4338953`** (`k_R`) | `0.0524339812` | **`0.0463364958`** | `0.883711189` | **yes** |
| 100 | `0.0511598261` | `0.0453880363` | `0.887181207` | yes |
| 1 000 | `0.0502404971` | `0.0447188595` | `0.89009588` | yes |
| **10 000** (standing) | **`0.0501417316`** | **`0.0446459684`** | **`0.890395426`** | **yes** |
| 100 000 | `0.0501317698` | `0.044638578` | `0.890424937` | yes |
| 1 000 000 | `0.0501307726` | `0.0446378379` | `0.890427885` | yes |

**`F5` was declared open and did not fire**: over the whole ladder **`0 of 6`** (cross-section,
coupling) pairs move a flatness verdict, and the worst relative spread in the tied free tile is
**`0.0380542`**. The `241×` error in the link stiffness is worth **3.8 %** of the free tile's
dishing and **no verdict at all**.

The row at 10 000 reproduces `C-0175` §9 at a departure of **exactly `0.0`** — all twelve
readings, both cross-sections, all three couplings.

## 6. But `F10` FIRED, and the coupled cells are not insensitive at all

The free tile's insensitivity does **not** transfer. Graded on the two cells `C-0180` recovered
and `C-0190` §6 quotes its threshold on, at **zero** eigenstrain:

| `k_link` [pN/nm] | `abstract grid`, 3 col | `abstract grid on the rooting helices`, 5 col |
|---|---|---|
| **`41.4338953`** (`k_R`) | **`0.107701645` — NOT flat** | **`0.103971919` — NOT flat** |
| 100 | **`0.10306316` — NOT flat** | **`0.101678429` — NOT flat** |
| 1 000 | `0.0999026162` — flat | `0.0999353736` — flat |
| **10 000** (standing) | **`0.0995744768`** — flat | **`0.0998791032`** — flat |
| 100 000 | `0.0995426168` — flat | `0.0998756402` — flat |
| 1 000 000 | `0.0995369555` — flat | `0.0998752932` — flat |

**The corpus's coupled recovery rests on a link stiffness the span law alone does not supply**,
and the threshold sits between 100 and 1 000 pN/nm — 24× to 241× above `k_R`. Two things bound
which way that goes and neither is settled here: `k_R` is a **pure-tension lower bound** on the
link (the connector's own bending and the junction's stacking are not in it), and the two cells
sit `0.10 − 0.0995744768` and `0.10 − 0.0998791032` inside the tolerance at the standing value,
which is the thinnest margin in the corpus. It is `T-300`.

The 10 000 column reproduces `C-0190` §6's own zero-eigenstrain readings — `0.0995744767` and
`0.0998791032` — at a departure of `2.5e−9`, which is the reproduced file's own emission
precision.

## 7. The departure on the coordinate it lives on — and `C-0190`'s threshold becomes a value

Because the common mode is the link, a crossover built with **both** backbones rolled by `ρ` off
the line of centres is relaxed at the link residual `R₀ = d·unitY·ρ`, not at zero. That is an
**offset**, i.e. a load: no entry of the stiffness matrix moves, the field is exactly linear in
it, and `C-0104`'s influence-bank trap does not arise. Its projection on the relative roll is
**exactly `0.0` pN·nm at all 59 ties** on a load that is not itself zero — the exact mirror of
`C-0190`'s `F2`, which found the *relative* prestrain's projection on the *common* roll to be
exactly zero.

Free tile at `10 × 6`, both phases, both ends of the ladder:

| coupling | no eigenstrain | link offset, phase `+1` | phase `−1` | `C-0190`'s per-beam twist, `+1` |
|---|---|---|---|---|
| `f = 0.30`, `k_link = 10 000` | `0.0446459684` | **`0.0931890716` — inside** | `0.0667312375` — inside | `0.296735462` — **outside** |
| `f = 0.30`, `k_link = k_R` | `0.0463364958` | **`0.0813628214` — inside** | `0.0661826442` — inside | — |
| `f = 0.26`, `k_link = 10 000` | `0.0467367262` | `0.0952881886` — inside | `0.0687126148` — inside | `0.298908715` — outside |
| none, `k_link = 10 000` | `0.12738041` — outside | `0.175404773` — outside | `0.148248798` — outside | `0.380784182` — outside |

**The correct channel is `3.18422026×` cheaper than the proxy at the same state** *(one division of
`C-0190`'s emitted `0.296735462` by this study's emitted `0.0931890716`; the ratio is not itself
emitted, and `3.64706455×` at `k_R` is the same division)* **, and it reverses the free-tile
verdict**: `0.0931890716` is inside `T-5b`'s 0.10 where `0.296735462` is outside.
Both ratios are one division from emitted numbers and neither is itself emitted.

**At the coupled level `C-0190`'s conclusion stands.** `0 of 24` loaded readings are flat at the
90th percentile — 2 cells × 2 phases × 6 link stiffnesses — so the recovered cells do not survive
the departure on either channel. `F9` was declared open and did not fire.

## 8. The arithmetic, and where the right value already was

&nbsp;&nbsp;&nbsp;&nbsp;`1 + 2 r_P/(d − 2 r_P) = d/(d − 2 r_P) = 3.52847408`

at `d = 2.536 nm` and `T-71`'s `r_P = 0.9086378584708424 nm`. The value in circulation is
**`3.52810239`**, 0.0105 % low, and it stands in `CH-0242`'s headline and §1, `C-0190`'s headline
and §6, the challenges index row for `CH-0242`, and two prose strings of
`gpd/results/T-291-common-mode-departure-and-beam-twist.json`.

**`T-291`'s own `openQuestions` block computes the same expression and emitted `3.52847408`.** The
artifact carried the correction the prose did not — `CLAUDE.md`'s *grep every headline number out
of the result file* met from the other side, and the reason a hand-computed number in five places
is worth a challenge (`CH-0248` §4). No verdict turns on it: `C-0190`'s `409×` is `409×` either
way.

## 9. The five verification gates

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | all three common-mode span ratios asserted invariant under a common rescaling of `d` and `r_P` at three scale factors; `T` asserted equal to `2k_θ/r_P` and `k_R` to `T/g` in closed form, and both asserted linear in `k_θ`, so neither carries a hidden angle; every stiffness pN·nm/rad or pN/nm, angles degrees at the API and radians only where a lattice is loaded |
| **2 — limiting cases** | the identity `d²/(2gr_P) − d/(2r_P) = d/g` asserted at four geometries; `r_P → 0` makes the two eigenmodes cost the same; the raw ratio asserted against the **exact** `turnPhosphateSpan` at a 1 mrad common roll to `1e−5` relative; a zero roll leaves no offset and an empty map returns the zero field **exactly**; a turn index outside the tie list and a non-finite offset are refused; and the eigenstrain field asserted to **converge** as the penalty stiffens |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the free tied lattice dishes exactly zero at BOTH ends of the link ladder** — re-taken because a link stiffness moves every entry of the matrix; a rigid roll stores zero hinge, link and slip energy in the assembled lattice and zero extension at every turn tie; the link offset load asserted **orthogonal to the relative roll** at every tie and **not** orthogonal to the common one; the field asserted exactly linear in the offsets; and the load asserted to touch **only** the `W` and roll coordinates, which is what makes `load[pinnedDof] = 0.0` provably inert for it |
| **4 — numerical convergence** | beam subdivisions 1 → 2 → 4 in the test and 1 → 2 in the study, **taken at the deciding cell** (the loaded reading nearest the tolerance), departure `2.1e−6`; the dishing sample grid 81 → 161 at the same cell, departure `4.1e−4`; and the link penalty 1e4 → 1e5 → 1e6 on the eigenstrain field alone, departure `2.0e−5`. **3 of 3 axes leave the verdict standing.** Result file byte-identical across two independent JVM runs |
| **5 — literature and upstream** | `r_P` and the C2′-endo step from `T-71`'s own 13 084-linkage measurement; `d` from Fischer et al.'s SAXS; the `±5 bp` rule through `C-0148`; **14 reproductions**, twelve of `C-0175` §9 at exactly `0.0` and two of `C-0190` §6 at `2.5e−9`; and the implied step stiffness cross-checked against `Gen1Tile.DUPLEX_STRETCH_MODULUS` |

## 10. The mutation table

Thirteen mutations over the twenty tests, applied after they were green, against a **subtracted
baseline of 0**, with the three subject sources asserted to occur at **exactly one path each**
(`C-0190` §8b's stray-copy detector, wired as a precondition rather than remembered).

| # | mutation | killed by |
|---|---|---|
| `M1` | `CH-0242`'s ratio drops the factor two in `1 + 2r_P/g` | the gate 5 re-derivation, the gate 2 identity and the exact-span check |
| `M2` | the frame-indifferent ratio drops its one-half | the identity and `P3` |
| `M3` | the geometric term is inverted | the identity |
| `M4` | the implied bond tension drops its factor two | the gate 1 force check |
| `M5` | the span-derived link stiffness divides by `d` instead of by `g` | the gate 1 force check |
| `M6` | the lattice common-mode stiffness drops its quarter | `P3`'s exact match against the linearised ratio |
| `M7` | the frame-indifferent connector arm becomes `d/3` | `P2` |
| `M8` | the rigid-roll residual drops the factor two on the arm | `P2` |
| `M9` | the link offset uses the arm rather than twice it | `P1`'s closed form |
| `M10` | the link offset load is applied with **opposite** signs at the two rolls, i.e. on the relative coordinate | `P5`, both directions |
| `M11` | the link offset load drops the penalty, so the field cannot converge as it stiffens | the penalty-convergence test |
| `M12` | the turn link extension reads the **relative** roll instead of the common one | the rigid-roll extension assertion |
| `M13` | the link offset accepts a turn index outside the tie list | the refusal test |

**13 of 13 killed, 0 survivors.**

The harness is **declared** in `tools/P-31-harness-census.py` and its thirteen anchors resolve
`13/13` — and it is the corpus's **first** whose subjects are **Kotlin** rather than Python
modules in `tools/`, which is why the census learned to resolve a declared subject path from the
tree root (two lines, plus the row). It is deliberately **not wired into the build**: it drives
Gradle, one incremental Kotlin compile and one test run per mutation, so wiring it would make the
build recursive and turn a ten-second task into a ten-minute one. `--check` reports it as
*"NOWHERE — runs only when somebody remembers"*, which is correct, and the reason is in the
harness's own docstring.

**And its first run was worthless in the loud direction, which is what the baseline check is
for.** `tasks.named("test") { dependsOn(…) }` hangs **29** Python document gates off the Kotlin
test task, so a snapshot whose own `P-31-harness-census.py` had not yet been told about this
harness failed the build *before* `:test` ran, and the baseline read
*"the test class did not run at all"* — after which every row would have been `killed` by that
one sentinel. `CH-0237`'s subtracted baseline is what said so on the first line of output. The
repair is to **derive** the `-x` flags from `build.gradle.kts`'s own `dependsOn` block rather than
list them, so a gate added tomorrow is excluded by construction; it also takes a mutation from
about 45 s to about 25 s. A Kotlin mutation harness is coupled to the whole corpus census unless
it says otherwise.

## 11. The ten declared falsifiers

| # | falsifier | fired | outcome |
|---|---|---|---|
| `F1` | the link's residual does not carry the sum of the two rolls, so the common mode really is absent | **no** | hinge `0.0`, link `6.7528608` pN·nm, matching the closed form at departure `0.0` |
| `F2` | the lattice's own common-mode stiffness is **below** `CH-0242`'s physical one | **no** | `336.800449×` above it in plane, `84.2001122×` through the thickness |
| `F3` | some arm other than `d/2` also annihilates the linearised rigid roll | **no** | `r_P` leaves `7.18724283e−4` nm at 1 mrad; the assembled lattice stores `0.0` under a rigid roll |
| `F4` | a uniform pressure on the free tied lattice dishes anything but zero, at either end of the ladder | **no** | worst reading `0.0` nm |
| `F5` | **declared OPEN** — the free tile's flatness verdict moves across `T-5b` between the two ends | **no** | `0 of 6` pairs move; worst relative spread `0.0380542` |
| `F6` | the link eigenstrain's field does not converge as the penalty stiffens | **no** | `0.0595221732`, `0.0597196721`, `0.0597395661` at 1e4, 1e5, 1e6 |
| `F7` | the link eigenstrain has a nonzero projection on the relative roll | **no** | exactly `0.0` pN·nm at all 59 ties, both ends, on a load that is not itself zero |
| `F8` | `C-0175` §9's six free-tile readings are not reproduced at the standing link stiffness | **no** | 14 reproductions, worst departure `2.5e−9`; the twelve `T-254` ones at exactly `0.0` |
| `F9` | **declared OPEN** — the two cells `C-0180` recovered survive the derived departure on the corrected channel | **no** | `0 of 24` loaded readings flat at the 90th percentile |
| `F10` | **declared OPEN** — the coupled cells' verdict is as insensitive to the link stiffness as the free tile's | **FIRED** | flat at `k_link ≥ 1000` and **not** flat at 100 or at `k_R`, at **both** cells |

## 12. Validity range, and what this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **`T = 2k_θ/r_P` is an ATTRIBUTION of the whole of `k_θ` to the span mechanism.** It is
  `CH-0242`'s own premise — without it the challenge's own ratio does not follow either — but if
  `k_θ` is largely stacking or backbone bending rather than connector tension, `T` is smaller and
  `k_R` smaller with it. The direction of that error is known: it makes the penalty **more**
  wrong, never less, and it moves `F10`'s threshold further from `k_R`.
- **The link's arm `d/2` is forced by frame indifference WITHIN this kinematics.** A model
  carrying an in-plane transverse coordinate could use the geometry's own `r_P` and would then
  also have to carry the prestress geometric term this one excludes. `C-0154` §9 records that the
  lattice has no such coordinate.
- **The model's common-mode stiffness is `unitY`-dependent** — four times larger at an in-plane
  bond than at an interlayer one — where the span form has no such anisotropy. That is the same
  missing coordinate seen from the side, and it is why §2 quotes two numbers.
- **Every number is conditional on the raster's turns carrying ZERO unpaired nucleotides.**
  `C-0193` (`T-296`) and [`CH-0247`](../challenges/CH-0247-the-tie-set-is-a-route-not-a-lattice.md)
  establish that the only folded `10 × 6` block joins its rim duplex ends through **28 nt** of
  ssDNA, which is a tether and not a tie. On that design there is no link at the **turn** at all,
  so §7's eigenstrain does not exist and §6's tied cells are not its cells. §1–§5 are untouched:
  the 435 **staple** bonds carry the link on either route, and §5's own `no ties` column is route
  B's mechanical lattice for them.
- The lattice carries no across-helix parallel-axis term, so its `D_⊥` is the independent one and
  a lower bound; Kirchhoff is not safe at these thicknesses, so every `D_∥` is an upper bound.
- The dropout statistics are measured on a single-layer Rothemund rectangle and only the
  **profile** transfers, in nm; the ensemble perturbs the **coupling** and never the block's own
  crossovers or its ties.
- **Only two coupled cells are graded**, the two `C-0190` §6 quotes its threshold on. A full
  re-grade of `C-0167`'s 64 cells at a moved link stiffness is not run here.
- Nothing here re-opens the placement search, the distribution rule, the raster, the
  cross-section or the departure's magnitude in degrees.

## 13. Open questions

- **Where the coupled recovery's link-stiffness threshold actually is**, and what the connector's
  real force-extension law makes of it. `T-300`. The ratio between the two azimuthal springs is
  convention-free; the absolute link stiffness is `T/g` and `T` is attributed rather than
  measured.
- **Whether `C-0167`'s whole 64-cell census moves at a link stiffness below 1 000 pN/nm.** Only
  the two recovered cells are graded here, and they are the two with the thinnest margins.
- **What an in-plane transverse coordinate would buy.** It would make the link's arm the
  geometry's `r_P` rather than `d/2`, remove the `unitY` anisotropy, and let the prestress
  geometric term be carried honestly rather than excluded.
- **Whether `C-0190`'s per-beam twist and this study's link eigenstrain are the same load in the
  rigid-link limit.** They load different coordinates of the same demand, the corpus now carries
  both, and they differ by `3.18422026×` on the free tile at the same state.
