# C-0190 — **`CH-0240` IS UPHELD ON THREE CHECKS AND NO SOLVE, AND THE RELATIVE CHANNEL IS WORSE THAN "LESS EFFICIENT": IT CANNOT RELIEVE THE DEPARTURE AT ALL, BECAUSE `u* = d cos δ / (2 r_P) = 1.37990892` LIES OUTSIDE THE REACHABLE `cos r`.** The two backbones of a honeycomb scaffold crossover are antipodal at every one of the 43 level displacements of the period (worst departure from `180°`: **`0.0`**), and the tie prestrain load's projection on the demanded common-mode roll is **exactly `0.0` at all 59 ties** on a load that is not itself zero. **What replaces it is `17.1428571°` of TWIST on each of 58 interior helices — UNIFORM in sign where the roll assignment ALTERNATES — invariant at all eight readings of the free conventions, with the two raster termini carrying none because a single-ended roll demand is a rigid roll.** Its rigid-duplex ceiling is **`8.31368089 k_BT`** over the block at the 102 bp row, **`1.03924948`** of ONE crossover column of the host sheet `C-0079` measures — and the host sheet folds. **`GJ` over one row is `13.2641292 pN·nm/rad` against `k_θ = 13.5294118`, 1.96 % apart**, so the two channels are priced alike *per site* and the whole factor of **`3.85510136×`** between the block totals is that the twist is `2δ` and the energy is quadratic. **BUT THE FIELD IS NOT ALIKE AT ALL.** The state that relaxes every hinge, every link and every beam torsion at once is a twisted **ribbon** `W = y θ₀ s / L`, which costs nothing except against the **foundation** — so the twist term's dishing is `0.253422732` of the stroke at `f = 0.30` and **`0.253403772` at no enhancement at all**, a **21.19×** change in `k_θ` moving it by **0.0075 %** *(one division of two emitted numbers, `0.253422732` against `0.253403772`; neither the percentage nor the `2.85×` is itself emitted)* where it moves the free tile 2.85×. On the coupled cells: **`0 of 64` flat at either sign, `0` at both, `0` sign-contingent**, against `C-0187`'s `1 / 1 / 0 / 2` on the relative channel; the twist moves a cell **`226.780027×`** what the relative roll moves it at the median and **`1440.62139×`** at the worst. **The load's SHAPE is derived and its MAGNITUDE is not** — it scales with the tie's own **common-mode** stiffness, which the lattice does not carry at all (`CH-0242`) — so the answer is also quoted as a threshold: the tighter recovered cell survives **`0.005`** of the derived eigenstrain and the other is out at **`0.002`**, i.e. a common-mode tie stiffness under **`0.11663286 pN·nm/rad`**, **0.862 %** of `k_θ`, where `CH-0242` puts the physical one at **`3.52810239×`** `k_θ`

| | |
|---|---|
| **Task** | [`T-291`](../tasks/T-291-common-mode-departure-and-beam-twist.md) — settling `CH-0240`, and pricing the per-beam twist that replaces the withdrawn coordinate |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (two lines of algebra on the challenged claims' own azimuth convention plus a reading of the model file all of them consume — deliverable 1 is settled with **no solver at all**) **+ in-silico** (the replacement eigenstrain assembled on the same three-dimensional beam-and-bond lattice and graded through the same exact Woodbury coupling surrogate, the same `C-0087`-measured incorporation as a Bernoulli dropout over 4 000 realisations on **one common stream restricted per cell**, and the same `T-5b` convention that `C-0180` and `C-0187` used) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The departure's magnitude is `C-0152`'s rigid-duplex reading of caDNAno's own rule; `GJ` is `Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY`, which is CanDo's model **input** and not a measurement; the tie's axial station is `s = ±L/2` exactly, where a scaffold crossover sits 5 bp from a staple position; and **every number is conditional on the raster's turns carrying ZERO unpaired nucleotides**, which is `C-0175`'s modelling choice and not the built precedent (Douglas et al. allot **28 nt** per helix as front and rear loops). |
| **Verdict** | **PASS on all eleven predicates. `F1`–`F8` and `F11` did not fire; `F9` and `F10` were declared OPEN and their outcome is the second half of the finding.** `F9` — *the flat census is the same at both signs* — **did fire**, and it fired the uninteresting way: the census is the same at both signs because it is **empty** at both. `F10` — *the two channels move a cell by the same amount* — **did not fire**, decisively: `120.509903×` to `1440.62139×`. Deliverable 1 is delivered in full and with no solve; deliverable 2 is delivered to the same depth `C-0187` reached and **no further**, and the residue is named; deliverable 3 is delivered per cell and, because its magnitude rests on a spring the lattice does not have, **as a threshold as well as a value**. |
| **Provenance** | [`gpd/results/T-291-common-mode-departure-and-beam-twist.json`](../results/T-291-common-mode-departure-and-beam-twist.json) (`tile.RasterTurnTwistPriceStudyKt`, **new**); model [`tile/RasterTurnTwistEigenstrain.kt`](../../src/main/kotlin/tile/RasterTurnTwistEigenstrain.kt) (**new file**). **Two shared sources are edited and BOTH edits are pure ADDITIONS, 43 insertions against 1 deletion**: `tile/HoneycombGrillage.kt` gains **one new public method** `beamTwistResponse`, in the shape of the two unit-response methods already beside it, with **no existing member touched** — the name occurs nowhere else in the tree, so no call site can have moved; and `structure/ResultInputs.kt` gains a `T_291` handle, because the tree's invariant is *every result path spelled in a main source has a handle*, and `ResultInputs.all` is read only in `structure/ResultInputsTest.kt`. **The proof is not offered instead of the run**: the study's own six reproductions exercise the edited grillage against `C-0175`'s and `C-0187`'s **committed** files and close at `1.1e−10`, `6.4e−10`, `2.9e−10` and `3.5e−10`, and `T-254` is re-run end to end. **Eighteen gate-named tests written first and watched fail** — [`tile/RasterTurnTwistEigenstrainTest.kt`](../../src/test/kotlin/tile/RasterTurnTwistEigenstrainTest.kt), which did not compile against a model that did not yet exist — of which **one failed on its first real run** and is recorded in §8, and **mutation-tested afterwards — ten mutations, 0 survivors over a subtracted baseline of 0**, whose *first* run reported six survivors for a reason that was not the tests (§8b). A full `./gradlew test` on the final sources gives **3 347 tests in 193 classes, 0 failures, 0 errors, 0 skipped**, with no task excluded. `T-254` — the nearest consumer of the edited grillage — re-runs **byte-identical**. `check-result-file-hygiene.py` (`--prose`, `--departures`, `--saturated`), `check-kotlin-format-strings.py`, `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-entry-points.py`, `check-queue-vocabulary.py`, `trace-answers.py` (both deliverables, **0 ABSENT**), `result-reader-census.py --check`, `T-278-emitter-rounding-census.py --check` and `P-31-harness-census.py --check` are all clean; `gpd/results/P-22-result-reader-census.json` is re-emitted, **additively**. Result file **byte-identical across two independent JVM runs**. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); `r_P` = 0.908637858 nm (`T-71`, measured); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp; scaffold crossovers at the staple position **± 5 bp** against an exact half turn of **5.25 bp**. Cross-section `10 × 6` (60 helices), block extent **116 bp = 39.44 nm** at `C-0151`'s `102 / 109` raster, `edgeY` = 38.04 nm. `k_θ` = 13.5294118 pN·nm/rad, `k_s` = 64.7058824 pN/nm, `GJ` = 460 pN·nm², link penalty `1e4` pN/nm; **435 staple bonds and 59 raster turn ties** (`firstAxialSign = +1`, ties at `s = ±L/2`). `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing face only; `C-0087`'s measured depth-convention incorporation; `C-0017`'s mandate at §3's **acceptable** clause, 33.3333 pN/nm on the SUM; seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. Composite fractions **0.30** and **0.26** (`C-0116`, entering as `hingeStiffnessEnhancement` 21.1851817 and 18.4938242) plus the lattice's own **1.0**. Demanded twist **17.1428571°**, applied at **both** global signs. |
| **Consumes** | [`CH-0240`](../challenges/CH-0240-the-allowed-departure-is-common-mode.md) — the challenge settled here; [`C-0187`](C-0187-the-turn-prestrain-sign-is-derived.md) (`T-284`) — the derived alternation, the 64 coupled cells, the stations, the distributions, the dropout stream and the zero-prestrain readings, **reproduced**; [`C-0175`](C-0175-drawable-raster-rim.md) (`T-254`) — the tie set and its own relative-roll readings, **carried unchanged as the term replaced**; [`C-0152`](C-0152-forced-scaffold-crossover-price.md) §5 — the allowed departure; [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md) — `turnPhosphateSpan`; [`C-0079`](C-0079-unbonded-duplex-separation.md) — the host-sheet column energy; [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md), [`C-0151`](C-0151-closing-raster-selection.md), [`C-0154`](C-0154-honeycomb-grillage.md), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md), [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0104`](C-0104-row-end-prestrain.md), [`C-0071`](C-0071-output-element-recommendation.md) |
| **Constrains** | **[`CH-0240`](../challenges/CH-0240-the-allowed-departure-is-common-mode.md) is UPHELD**, and its own §4 parenthetical — *"a relative roll **does** reduce the phosphate span, just less efficiently"* — is **withdrawn**, in the direction that strengthens it. **[`C-0175`](C-0175-drawable-raster-rim.md) §8, [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) §4 and [`CH-0228`](../challenges/CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md) have their COORDINATE withdrawn and not one of their numbers**: each is annotated in place, *strike, never delete*. `C-0180` §3's zero-prestrain headline `2 of 64` and `C-0175`'s tie **stiffness** deliverable are untouched, both carrying no prestrain at all. `C-0152` §5's folding calibration is untouched; what is withdrawn there is the **ground** its `½ k_θ θ²` ceiling was given, and the number survives on the twist channel to 1.96 %. **One challenge is raised**: [`CH-0242`](../challenges/CH-0242-the-tie-carries-no-common-mode-stiffness.md), that the lattice's bond and tie carry only the **cheaper** of a crossover's two azimuthal springs. **One task is opened**: `T-296`, whether the raster's turns are built with zero unpaired nucleotides at all. |

---

## 1. The cheap bound is the whole of deliverable 1, and it needs no solver

Three checks, all of them microseconds, all of them on the challenged claims' own models.

| # | the statement | the quantity | reading | holds |
|---|---|---|---|---|
| 1 | the two backbones of a honeycomb scaffold crossover are **antipodal at every level displacement**, so `∂(ψ_P − ψ_Q)/∂ζ = 0` identically | the largest departure of the relative azimuth from `180°` over the whole 21 bp period, both signs | **`0.0`** | yes |
| 2 | the **relative** channel cannot relieve a common-mode departure at all | `u* = d cos δ / (2 r_P)`, the stationary point of the span in the relative roll | **`1.37990892`**, outside the reachable `cos r ∈ [−1, 1]` | yes |
| 3 | the tie prestrain load is **orthogonal to the demanded common-mode roll** at every one of the 59 raster turns | the largest `\|L · c_k\|` over the ties, in pN·nm | **`0.0`**, on a load that is not itself zero | yes |

Check 1 is `ForcedCrossoverPrice`'s own `(θ, 180° + θ)` construction read as a difference; check 3
is `assembleLoad` with and without the derived prestrain, projected onto the direction that rolls
**both** beams of a tie the same way. Neither needs a solve, and together they are `CH-0240` §2
and §3 made executable.

**Check 2 is new, and it is the one that goes further than the challenge.** At a common-mode
departure `δ` a pure relative roll `r` puts the pair at `(δ + r, 180° + δ − r)`, and the corpus's
own span becomes

&nbsp;&nbsp;&nbsp;&nbsp;`span²(u) = (d − 2 r_P cos δ · u)² + 4 r_P² sin²δ · u²`, `u = cos r`,

whose stationary point is `u* = d cos δ / (2 r_P)`. At the honeycomb's `d = 2.536 nm` and `T-71`'s
measured `r_P` that is **1.37990892**, so the minimum over the *whole* relative channel sits at
`u = 1`, which is `r = 0` — **the built state itself**. Any relative roll of any amplitude makes
the span **worse**, and the shortfall the channel leaves above the floor is **`0.0683674233 nm`**,
which is the entire span excess the departure carries (built `0.787091706 nm` against a floor of
`0.718724283 nm`). A brute sweep over 7 201 amplitudes agrees with the closed form.

`CH-0240` §4 said a relative roll reduces the span *"just less efficiently"*. It does not reduce
it. That clause is withdrawn, and the challenge is stronger without it.

## 2. Deliverable 2 — the replacement, and how far its sign is derivable

The relief is a roll of **each** duplex by `−δ` about its **own** axis — an absolute roll, and
independent of which neighbour the turn bonds to, because the departure is expressed as a rotation
of the backbone about the duplex axis. `C-0187` derives that the departures **alternate**: every
turn at the block's high axial rim carries `+8.57142857°` and every turn at the low rim the
negation, at all eight readings of `firstAxialSign`, `mirrored` and `axialReversed`. So every
interior helix is asked for `−δ` at one end and `+δ` at the other:

| raster | determined | beams carrying twist | beams free | distinct twist |
|---|---|---|---|---|
| **`102 / 109`** (`C-0151`, drawable) | **yes** | **58** | **2** | **`[−17.1428571°]`** — one value |
| `112 / 108` (`C-0140`, undrawable) | no | 0 | 0 | none; the raster does not close, so no departure is determined |

**The twist assignment is UNIFORM where the roll assignment ALTERNATES**, which is a strictly
stronger statement than `C-0187`'s: `2^58` sign assignments collapse to **one**, and the eight
readings of the free conventions all return `58` beams, `17.1428571°` and one sign.

The two raster termini carry **no** twist, because each has a turn at one rim only and a
single-ended roll demand is a rigid roll of that duplex, for which the model has no spring.

### The one binary that is left, and why it is left

`C-0187` could not orient its roll assignment because a common-mode departure has no image on the
relative coordinate at all. The twist coordinate **is** the coordinate the demand lives on, so the
map is not vacuous — and it is still not derivable **here**, for a different and stated reason:

> `HoneycombGrillage` carries **no handedness**. `EI`, `GJ`, `k_θ`, `k_s` and the link penalty are
> scalars; nothing in the class ties its `s` to the raster's own axial datum, and nothing ties
> `Φ`'s rotational sense to B-DNA's. So no constant of the model maps an azimuthal sense onto `Φ`.

That is a property of the model rather than of the lattice, and it is the honest answer: the sign
is derived **to the same depth `C-0187` reached and no further**, both signs are graded, and the
verdict is quoted at the worse. What is *not* left free is the relative assignment, and that is the
part `2^58` lives in.

## 3. Deliverable 3a — the cheap bound on the price, before any lattice is assembled

`½ (GJ/L) θ₀²` is a strict **upper** bound on what the eigenstrain stores, because the relaxed
state minimises. It is `C-0152` §5's own instrument, read on the channel the demand loads.

| over | `L` [nm] | `GJ/L` [pN·nm/rad] | per beam [`k_BT`] | × 58 [`k_BT`] | over one host-sheet column |
|---|---|---|---|---|---|
| **the 102 bp row** | 34.68 | **13.2641292** | **0.143339326** | **8.31368089** | **1.03924948** |
| the 109 bp row | 37.06 | 12.4123044 | 0.134134048 | 7.77977478 | 0.972508685 |
| the 116 bp block extent (the model's own beam) | 39.44 | 11.663286 | 0.126039752 | 7.31030561 | 0.913822816 |

Two readings follow, and neither needs a solve.

**The whole block's twist demand costs about ONE crossover column of the host sheet** — `C-0079`
measures 7.99969697 `k_BT` per column of a sheet that demonstrably folds — so the demand is
affordable as an *energy*. That is the same calibration `C-0152` §5 uses, applied to the whole
raster rather than to one site.

**And `GJ` over one row is `13.2641292` against `k_θ = 13.5294118` — 1.96 % apart.** The duplex's
own torsional compliance over one row and the crossover's dihedral spring are the same number, so
the two channels are priced **alike per site**, and the entire factor of **`3.85510136×`** between
the block totals (8.31368089 `k_BT` against the relative-roll charge's 2.15654015 over 59 ties) is
that the twist is `2δ` where the roll is `δ` and the energy is quadratic. A coincidence of two
unrelated constants, and it is what makes `C-0152`'s number survive the change of channel.

## 4. Deliverable 3b — and the FIELD is not alike at all, for a geometric reason

The energies agree to a factor of four; the fields do not agree at all, and the reason is that the
state which relaxes **every** hinge, **every** link and **every** beam torsion at once is a twisted
**ribbon**:

&nbsp;&nbsp;&nbsp;&nbsp;`Φ(s) = θ₀ s / L`, `W(s, y) = y θ₀ s / L`, `U = −z y θ₀ / L`.

Every bond's relative roll vanishes (all beams share one `Φ(s)`); every link's residual
`W_a − W_b + armY(Φ_a + Φ_b)` vanishes identically; every beam's bending vanishes (`W` is linear in
`s`); every slip vanishes at that `U`. **Only the foundation resists it** — and a saddle is not a
rigid mode, so it is pure dishing.

Three consequences, all measured:

- **The twist term's dishing is almost independent of `k_θ`.** At the enhanced `f = 0.30` it is
  `0.253422732` of the stroke, at `f = 0.26` `0.253418941`, and at the lattice's own **`1.0`**
  `0.253403772` — a **21.19×** change in the hinge stiffness moving it by **0.0075 %**, where the
  same change moves the *free tile's* own dishing 2.85× (`0.12738041 → 0.0446459684`).
  *Both ratios are one division from emitted numbers and neither is itself emitted.*
  Nothing was tuned to produce that; it is the ribbon's signature.
- **The relaxation is measurable and exact.** `E = E₀ − ½ Lᵀu` at equilibrium, so the relaxed
  energy is `12.2934434 pN·nm` against the restrained `30.2788984` — **`0.406006958`** of the
  ceiling at `f = 0.30`, and 0.392737468 at no enhancement.
- **The triangle inequality holds and is not tight.** Over the 60 unit twist responses at
  `f = 0.30` the largest single beam is worth `0.0629908781` of the stroke per radian, the median
  `0.0258974564` and the smallest `0.0131109907`; the ceiling at the demand is `0.474153608` and
  the realised field **`0.53447391`** of it.

`CLAUDE.md`'s standing warning is met from the other side: *a uniform load on a uniform foundation
dishes exactly zero* is asserted here on the tied, zero-eigenstrain lattice, and **its eigenstrain
sibling is asserted the other way** — a uniform *twist* relaxes into a saddle, not a rigid mode,
so it must **not** dish zero, and a test asserts that too.

## 5. Deliverable 3c — the free tile and the 64 coupled cells

| composite fraction | no eigenstrain | the derived twist, phase `+1` | phase `−1` |
|---|---|---|---|
| **`f = 0.30`** | **0.0446459684** — inside `T-5b` | **0.296735462** | **0.292284868** |
| `f = 0.26` | 0.0467367262 — inside | 0.298908715 | 0.294526083 |
| the lattice's own `1.0` | 0.12738041 — outside | 0.380784182 | 0.37934593 |

Against `C-0175` §8's relative-roll reading of the same tile, **0.0457993778**: the term the corpus
applies is not a small version of the right one, it is a **different** one, and the right one is
**6.48×** larger at the same state.

Graded on `C-0167`'s 64 coupled cells — the same four placements, the same two distributions, the
same two composite fractions, the same 4 000-realisation common stream:

| | this study, the twist | `C-0187`, the relative roll |
|---|---|---|
| flat at `T-5b`'s 0.10 at the 90th percentile, **phase `+1`** | **0 of 64** | 1 of 64 |
| the same, **phase `−1`** | **0 of 64** | 1 of 64 |
| flat at **BOTH** signs | **0 of 64** | 0 of 64 |
| verdict **depends on** the sign | **0 of 64** | 2 of 64 |
| worst `\|movement\|` from the zero-term cell, over 128 cells | **0.247557293** | 0.00203756217 |
| median `\|movement\|` | **0.202322435** | 0.000581022203 |

**`F9` was declared open and it fired, the uninteresting way**: the census *is* the same at both
signs, because it is **empty** at both. The two recovered cells `C-0180` and `C-0187` argue over —
`0.0995744767` and `0.0998791032` at zero prestrain, both reproduced here to `3e−10` — read
`0.302153443` and `0.294923913` at the worse sign.

Read cell by cell against `C-0187`'s own published readings of the same 64 cells, the twist moves a
cell **`226.780027×`** what the relative roll moves it at the median, **`120.509903×`** at the least
and **`1440.62139×`** at the most. `F10` did not fire.

## 6. The magnitude is not settled, so the answer is also a THRESHOLD

The load's **shape** is derived exactly — a couple pair on each interior beam's two end roll
coordinates, uniform in sign across the block. What scales it is the tie's own **common-mode**
stiffness, and `HoneycombGrillage` does not carry one: its bond and tie have `½ k_θ(Φ_u − Φ_l)²`
and nothing else on the azimuthal coordinates (`CH-0242`). Modelling the demand as a per-beam
torsional eigenstrain against the duplex's own `GJ` is the model's natural choice — it is the only
spring the model has on that coordinate — and it corresponds to a common-mode stiffness of
**`23.326572 pN·nm/rad`**.

Because the field is exactly linear in the load, the whole ladder costs **no extra solve**
*(seven of its ten rungs are shown; the full ladder is in the result file's `scale` record)*:

| fraction of the derived eigenstrain | implied `k_common` [pN·nm/rad] | cell A (abstract grid, 3 col) | cell B (rooting helices, 5 col) |
|---|---|---|---|
| **0** | 0 | **0.0995744767** — flat | **0.0998791032** — flat |
| **0.002** | 0.046653144 | **0.099584342** — flat | 0.10012094 — **not flat** |
| **0.005** | 0.11663286 | **0.0998028853** — flat | 0.100327159 — **not flat** |
| 0.01 | 0.23326572 | 0.10020506 — not flat | 0.100423045 — not flat |
| 0.05 | 1.1663286 | 0.103008232 | 0.104400529 |
| 0.25 | 5.831643 | 0.138852474 | 0.141350433 |
| **1.0** | **23.326572** | **0.302153443** | **0.294923913** |

**The coupled recovery survives only a common-mode tie stiffness below `0.11663286 pN·nm/rad`,
which is `0.862 %` of `k_θ`** — and `CH-0242` puts the physical one at **`3.52810239×`** `k_θ`, i.e.
**409×** the ceiling *(one division of `3.52810239 × 13.5294118` by `0.11663286`; the ratio is not itself emitted)*,
on the other side. **No nonzero rung of the ladder keeps both cells flat.**
That is `CLAUDE.md`'s own *deliver a ceiling and a threshold instead of a value*, and it is what
makes the missing spring's absence decidable without measuring it.

## 7. Convergence, and the five gates

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | the demanded twist asserted to be exactly twice `C-0152`'s allowed departure at every one of 58 interior beams; the restrained energy asserted against `½ (GJ/L) θ²` in closed form and asserted to fall with the beam length; the reconstructed DOF layout asserted against the lattice's **own** `pistonMode` basis vector before it is used |
| **2 — limiting cases** | a zero eigenstrain returns the zero field **exactly**; a raster that does not close **refuses** rather than guessing; a beam outside the block and a non-finite twist are refused; and the eigenstrain load asserted to **telescope** to one couple pair at the beam's two ends, reconstructed as `K u` from the public `stiffnessEntry`, at **subdivisions 1 and 2** |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the tied, zero-eigenstrain lattice dishes exactly `0.0`**; **and a uniform twist does NOT**, asserted the other way because `CLAUDE.md` records that a uniform eigenstrain does not inherit that falsifier; the field asserted **exactly linear** in the eigenstrain to `1e−9`; and the twist's own limiting case — a uniform twist over **every** beam relaxing into the ribbon `W = y θ₀ s / L` as the foundation vanishes, monotone over three decades, which is what fixes the response's **sign** against the eigenstrain's |
| **4 — numerical convergence** | beam subdivisions 1 → 2 and the dishing sample grid 81 → 161, taken on the `p90` of each deciding cell at each sign: **8 of 8** steps leave the verdict standing, worst departure `2.3e−5`; the result file **byte-identical across two independent JVM runs** |
| **5 — literature and upstream** | the `±5 bp` rule from the primary source (Douglas et al., *NAR* **37**:5001, `PMC2731887`, in `gpd/data/T-151-sources/`, **read directly**) and consumed through `C-0148`'s model; `r_P` from `T-71`'s own 13 084-linkage measurement; **six reproductions against `C-0175`'s and `C-0187`'s committed files, worst `6.4e−10`** |

## 8. The failing test, and what it found

`the eigenstrain load telescopes to one couple pair at the beam's two ends` reconstructs `K u` from
the class's public `stiffnessEntry` and asserts that every entry but the two end `Φ` coordinates of
the loaded beam is zero. It failed on its first real run at **one** further coordinate: the axial
rigid mode's pinned degree of freedom, `dof(0, axialPinBeam, U)`, whose row the factorisation
replaces by the identity — so `K u` there is *not* the applied load and never was. The repair is
one excluded index and a comment, and without the test the study would have carried an assertion
that is false about a correct solver. It is the same family as `C-0104`'s *a term that lives on the
structure contaminates every influence function*, met one level down: **a pinned coordinate is not
a coordinate of the load.**

## 8b. The mutation table, and the run that had to be thrown away

Ten mutations over the eighteen tests, applied after they were green, against a subtracted
baseline of **0**:

| # | mutation | killed |
|---|---|---|
| `M1` | the relative channel's stationary point is not clamped to the reachable `cos r` | `F3` — no relative roll reaches the span the common-mode relief reaches |
| `M2` | the two backbones are read as `(θ, 180° − θ)` rather than `(θ, 180° + θ)` | `F1` — a level displacement leaves the two backbones exactly antipodal |
| `M3` | the demanded relief roll is the departure rather than its negation | `F4` — the demanded relief roll is the negation of the departure at BOTH duplexes |
| `M4` | the twist is the **sum** of the two end demands rather than their difference | `F4` × 2 — the census, and the eight-convention invariance |
| `M5` | a beam with a demand at **one** end only carries the twist of that end | `F4` × 2 — the same two |
| `M6` | the eigenstrain load is applied with the **same** sign at both beam ends | `F6`, and the telescoping test |
| `M7` | the eigenstrain couple is divided by the **node count** rather than by the beam's span | `F6`, and the telescoping test |
| `M8` | the load is applied at the first **interior** node rather than at the beam end | `F6`, and the telescoping test |
| `M9` | `beamTwistResponse` accepts a beam outside the block and a non-finite twist | the refusal test |
| `M10` | the restrained energy drops its one-half | the rigid-duplex ceiling test |

**10 of 10 killed, 0 survivors.**

**And the first run of that table was worthless, in a way that reads exactly like a weak test
list.** It reported **six survivors — and all six were the six mutations in one file.** The cause
was not the tests: a mistyped `cp` with two sources and a directory target had left a **copy of
the model in `src/test/kotlin/tile/`** as well as `src/main/kotlin/tile/`, inside the snapshot the
harness drove. Kotlin resolves a top-level name in its **own** source set first and the test
compilation is *associated* with main, so there is **no redeclaration error**: the tests bound to
the copy, and mutating the original changed nothing they could see — while `:compileKotlin` duly
ran, which makes the mutation look landed. Two checks settle it and the second is decisive: make
one mutation a **syntax error** (the build must fail — it did) and then have a probe test **print**
the mutated function's value (it printed the pristine `0.0683674233`). The one-line detector is
`find src -name '<file>.kt'` returning two paths.

The shape is the signal rather than the count: **a green baseline plus every mutation of one file
surviving is a statement about the build, not about the tests** — `CH-0237`'s *a mutation harness
layout is a premise of its own measurement*, reached through a stray file instead of a moved
anchor.

## 9. The eleven declared falsifiers

| # | falsifier | fired | outcome |
|---|---|---|---|
| `F1` | the two backbones are not antipodal at every level displacement | **no** | worst departure from `180°` over the whole period: `0.0` |
| `F2` | the tie prestrain load has a nonzero projection on the demanded common-mode roll | **no** | exactly `0.0` at all 59 ties, on a load that is not itself zero |
| `F3` | a pure relative roll reaches the span the common-mode relief reaches | **no** | `u* = 1.37990892 > 1`, shortfall `0.0683674233 nm` |
| `F4` | the derived twist is not `17.1428571°`, or not one sign, or not convention-invariant | **no** | 58 beams at one value, 2 free, at all eight readings |
| `F5` | the response is not exactly linear in the eigenstrain | **no** | below `1e−9` over a sampled face |
| `F6` | a uniform twist does not relax into the ribbon as the foundation vanishes | **no** | monotone over three decades, residual `< 1e−4 nm` |
| `F7` | a uniform pressure on the tied, zero-eigenstrain lattice dishes anything but zero | **no** | `< 1e−9`, and the eigenstrain sibling asserted the other way |
| `F8` | the realised field exceeds the triangle-inequality ceiling on its own 60 unit responses | **no** | `0.53447391`, `0.530859998`, `0.395773967` of the ceiling |
| `F9` | **declared OPEN** — the flat census is the same at both signs | **FIRED** | and the uninteresting way: **0 of 64 at both**, so the census is the same because it is empty |
| `F10` | **declared OPEN** — the two channels move a cell by the same amount | **no** | `120.509903×` to `1440.62139×`, median `226.780027×` |
| `F11` | a convergence step moves a verdict at any deciding cell | **no** | 8 of 8 survive, worst departure `2.3e−5` |

## 10. What this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **Every number here is conditional on the raster's turns carrying ZERO unpaired nucleotides**,
  which is `C-0175`'s own modelling choice and **not** the built precedent: Douglas et al.'s
  honeycomb blocks allot **28 nt** per helix as front and rear unpaired loops, and a turn with 28
  unpaired nucleotides is a flexible tether that demands no azimuth at all. On that design there is
  no tie, no prestrain and no twist — **and `C-0175`'s tie stiffness deliverable and `C-0180`'s
  coupled recovery go with them.** That is `T-296`.
- **The load's shape is derived and its magnitude is not**, because it scales with a spring the
  lattice does not carry (`CH-0242`). The eigenstrain formulation is the model's natural choice,
  not a measurement, which is why §6 exists.
- The model applies the twist over the **model's** beam, whose length is the block's 116 bp extent,
  where the physical rows are 102 and 109 bp. The load is `GJ θ₀ / L`, so the model **under**-loads
  the demand by `116/109 = 1.06×` to `116/102 = 1.14×`; the field is exactly linear in it.
- The demand is **not accumulating** along a helix — exactly two turns, so exactly `2δ` — and it
  **is** coherent across helices, which is what makes it a whole-block twist rather than noise.
  `CLAUDE.md`'s *"local and non-accumulating"* is right about the first and says nothing about the
  second.
- The magnitude is `C-0152`'s **rigid-duplex** reading and is a **ceiling**: nothing here bounds
  from below how much of the 0.25 bp is taken up in backbone strain or local unstacking rather than
  in a roll.
- `GJ` is `Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY`, which is CanDo's model input; `CLAUDE.md` records
  that CanDo's `EI` implies a persistence length 25 % above the measured one, and no independent
  torsional measurement is carried here.
- The tie sits at `s = ±L/2` **exactly**; a scaffold crossover sits 5 bp from a staple position, so
  its true axial station is within **1.7 nm** of the rim node.
- The lattice carries **no** across-helix parallel-axis term, so its `D_⊥` is the independent one
  and a lower bound; Kirchhoff is not safe at these thicknesses, so every `D_∥` is an upper bound.
- The dropout statistics are measured on a **single-layer Rothemund rectangle** and only the
  *profile* transfers, in nm; the ensemble perturbs the **coupling** and never the block's own
  crossovers or its ties.
- **Nothing here re-opens the placement search, the distribution rule, the raster, the
  cross-section or the departure's magnitude.**

## 11. Open questions

- **What the tie's own common-mode stiffness is** — `CH-0242`. The ratio to the relative one is
  exact given that the crossover is loaded in span at all; the absolute value needs a
  force-extension law for a phosphodiester step, which this repository does not carry.
- **Whether the raster's turns are built with zero unpaired nucleotides at all** — `T-296`. Every
  number of this claim, of `C-0175` §8 and of `C-0180` §4 rests on it, and the only built honeycomb
  blocks in the literature use 28 nt.
- **Whether a distribution *searched* on the tied lattice under the twist term reaches a cell flat
  at both signs.** Every distribution graded here is a rule written on a smeared model's geometry,
  and the threshold of §6 says how far one would have to get.
- **What a FORCED crossover costs on this channel.** `C-0152`'s ceiling is taken on the relative
  roll; on the twist channel the relief must be reconciled with the neighbouring crossovers 7 or
  21 bp away rather than over a whole row, which is a much shorter lever and therefore a much
  larger number.
