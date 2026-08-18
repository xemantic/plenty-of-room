# C-0112 — **THE FIELD IS EXACTLY SEPARABLE AND THE VERDICT IS NOT, AND THE 42 INTERIOR CROSSOVERS ARE NOT A CORRECTION — THEY CARRY MORE OF THE EIGENSTRAIN THAN THE 14 ROW ENDS DO.** A prestrain is a load, so the graded field's response splits into its 14-site and 42-site parts as an **identity**, to **2.1e−15** in the coefficient vector. Peak dishing is a **seminorm** of that field and does not add — the graded peak is **0.294** of the sum of its two parts' peaks — so the cancellation is a **cross term**, `cos = −0.579495374`, on an interior field carrying **0.688** of the row-end field's area norm and **53.65 %** of the assembled absolute couple. The cancellation is **structural and needs no solve**: the column next inboard of the row end carries **0.661** of the row end's amplitude, on the same 7 sites per column, **at the opposite sign**, and the ladder shows the whole move recovered — **106.2 %** of it — by that one column pair. **`C-0107`'s 0.0922622 SURVIVES**, reproduced here at **1.1e−10** on the same host, and it is flat at **both** overall signs (0.0922622 / **0.0910197**) and at **40 of 40** cells of `C-0107`'s own 12-cell bracket, where the row-end-only idealisation is flat at **14 of 40**. What does **not** survive is the pairing that explains it: the graded field's own row-end restriction is uniform at **−22.5397532°**, the sign `C-0107` calls *adverse*, so the consistent counterpart of 0.0922622 is **0.1190748** and not 0.1022820 — the published comparison differences two states that differ in **three** factors, and the true interior term is **0.0268125** of the stroke against the published 0.0100

| | |
|---|---|
| **Task** | [`T-190`](../tasks/T-190-interior-crossover-prestrain.md), raised by [`C-0107`](C-0107-row-end-prestrain-value.md) *Still open* item 1 |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** |
| **Verification type** | **logical** (a prestrain is a load, so the decomposition is an identity and the per-column census is arithmetic that runs before any solve) **+ in-silico** (one bank of **73** grillage solves on `C-0107`'s own explicit elastic-support host: 9 states, 6 prestrain-only decomposition solves, an `2 × 2 × 2` factorial, an 8-rung column ladder and a 40-cell bracket at both overall signs) |
| **Verdict** | **PASS on all four predicates.** `P1`: the split is an identity — worst superposition departure **2.1e−15** over both signs, and the quadratic norm identity `‖G‖² = ‖R‖² + 2⟨R, I⟩ + ‖I‖²` closes at the same order. `P2`: the interior contribution is quantified convention-free — **cosine −0.579495374**, `‖I‖/‖R‖ = 0.688`. `P3`: the cancellation is tested over the **overall sign** (which `C-0107` did not sweep), over its **12-cell bracket** (40 cells with both signs, plus the self-consistent `α` rows `C-0107` did not run) and over the lattice's **column** structure. `P4`: **10 reproductions**, worst **3.0e−08**. Raises [`CH-0129`](../challenges/CH-0129-the-graded-field-is-compared-against-the-other-overall-sign.md) and [`CH-0130`](../challenges/CH-0130-the-overall-sign-of-the-corrugation-is-undetermined.md), both against `C-0107`. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The prestrain itself remains unknown and unsourced — `C-0107` established that the one published measurement of the coordinate (Snodin et al. 2019) excludes exactly these sites, and this claim adds no source. What is established is that the **interior** field is not an extra assumption: it is `C-0107`'s own boundary layer read at the stations it already covers, and it is the **14-site idealisation** that has nothing behind it. |
| **Provenance** | `gpd/results/T-190-interior-crossover-prestrain.json`, produced by `structure.InteriorCrossoverPrestrainStudyKt` (**new**); model in `src/main/kotlin/structure/InteriorCrossoverPrestrain.kt` (**new**). **No existing source file was edited and no existing result file was re-emitted** — `OrigamiGrillage`, `CrossoverPrestrain.kt`, `EdgeTwistRelief.kt`, `EdgeTwistReliefStudy.kt`, `RowEndPrestrainStudy.kt`, `anchoring/`, `coupling/` and `window/` were **read, not modified**. **8 column records, 9 solved states, 2 decomposition records, 8 factorial cells, 8 ladder rungs, 40 bracket cells, 5 convergence records, 10 reproductions, 4 predicates, 6 falsifiers, 8 findings**; **13 gate-named tests in `src/test/kotlin/structure/InteriorCrossoverPrestrainTest.kt`**, red-checked (25 unresolved references, then green); `tools/study.sh` and `tools/verify.sh` run on isolated trees; `tools/verify.sh --tests` over `InteriorCrossoverPrestrainTest`, `ResultRoundingTest`, `EdgeTwistReliefTest` and `CrossoverPrestrainTest` is **BUILD SUCCESSFUL**, with one concurrent agent's mid-TDD `src/test/kotlin/actuator/TallGapDeviceBTest.kt` dropped by `--drop-file` (its three failures are that agent's and touch nothing here); the result file **re-run through `tools/study.sh` on an isolated tree and BYTE-IDENTICAL** across two independent JVM runs. Emitted with **`floor = 0.0`**, because `RESULT_ABSOLUTE_FLOOR` is a claim in the **locked units** and the load-bearing numbers here are dimensionless — the first run emitted the `2.1e−15` superposition departure and four `~1e−10` reproductions as exactly `0.0`. Lowering the floor then hit the second half of the same `CLAUDE.md` entry: `roundForResult` throws `Cannot round NaN value` on an **exact zero** at `floor = 0.0`, a latent defect no caller had reached because every existing caller passes a positive floor that catches the zero first. **Repaired in `structure/ResultRounding.kt` with a red-checked test**, and it is a strict no-op for all 40-odd existing callers (the smallest floor in use elsewhere is `1e-18`); `tools/result-reader-census.py --emit` re-run; `tools/check-markdown-tables.py` clean over 354 files; `tools/trace-answers.py` clean |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes** at the SAXS 2.69 nm, 0.34 nm rise, **32/3 bp per turn design against B-DNA's 10.5**, 16 bp column pitch, 32 bp per-interface spacing; along-helix width **38.08 nm** at crossover **phase 8** (8 columns, 56 crossovers, 14 of them at a row end); `C-0090`'s published 34-root key; `C-0017`'s **33.3333333 pN/nm** shared equally as 34 **explicit elastic supports**; `C-0022`'s solved collar at 2 mM, a 10 nm gap and 0.192 V; `C-0001`'s foundation secant; free stroke **5.15473846 nm**; `k_θ` = 13.5294118 pN·nm/rad |
| **Consumes** | [`C-0107`](C-0107-row-end-prestrain-value.md) (the boundary layer, the graded field, `EdgeTwistRelief`, `corrugatedPrestrain`, and its eleven solved states — **read from its result file**), [`C-0104`](C-0104-row-end-prestrain.md) (a prestrain is a load and the response is linear in it; the threshold — **read from its result file**), [`C-0090`](C-0090-buildable-raster-width.md) (the 38.08 nm width, phase 8, the placement key — **read from its result file and reproduced**), [`C-0099`](C-0099-row-end-crossover-stiffness.md) (`rowEndCrossoverSites`, the 14), [`C-0095`](C-0095-row-end-crossover.md), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 56, the column/interface parity rule), [`C-0009`](C-0009-discrete-lattice-tile.md) (`OrigamiGrillage`, `k_θ`), [`C-0022`](C-0022-tile-edge-load-profile.md) (**read from its result file**), [`C-0063`](C-0063-upward-root-placement.md) (the dishing convention), `Gen1Tile` |
| **Raises** | [`CH-0129`](../challenges/CH-0129-the-graded-field-is-compared-against-the-other-overall-sign.md), [`CH-0130`](../challenges/CH-0130-the-overall-sign-of-the-corrugation-is-undetermined.md), both against `C-0107` |

---

## The claim, in one line

**`C-0107` asked whether the complete field keeps the verdict and found that it does; this asks what the 42 sites nobody modelled are actually carrying, and the answer is that they carry more of it than the 14 that were — at the opposite sign, by a lattice congruence that needs no solve.**

---

## The conventions, restated rather than inherited

- Angles **rad** (degrees where quoted), rotational stiffness **pN·nm/rad**, couples **pN·nm**,
  lengths **nm**, forces **pN**; `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
- `x` along the helices measured from the **tile centre**, `y` across them, `z` normal and positive
  **upward**; `w` positive **downward**.
- A **prestrain** `θ₀` is `C-0104`'s: the relative roll a crossover is *built* at, so its hinge
  stores `½ k_θ (Δφ − θ₀)²`.
- `u(x)` is `C-0107`'s azimuthal register error, **odd about the row centre**.
- The **graded corrugated field** is `θ₀(b, x) = s·(−1)^b u(x)`, with `s = ±1` an **overall sign**
  that nothing in this repository fixes — swept here, never defaulted.
- **Dishing** is `C-0063`'s, unchanged, and **flat** means `≤ T-5b`'s 0.10 of the free stroke.

---

## Deliverable 1 — the census, which is arithmetic and runs before any solve

`C-0107`'s field is defined at every crossover station. Evaluated on the phase-8 layout the study
solves, the 56 sites fall into 8 columns of 7, and the whole answer is visible in the table:

| column | `x` (nm) | parity | at a row end? | sites | `θ₀` at overall sign `+` | of the row end |
|---|---|---|---|---|---|---|
| 0 | **−18.99** | 0 | **yes** | 7 | **−22.540°** | 1.000 |
| 1 | −13.60 | 1 | no | 7 | **+14.897°** | **0.661** |
| 2 | −8.16 | 0 | no | 7 | −8.455° | 0.375 |
| 3 | −2.72 | 1 | no | 7 | +2.740° | 0.122 |
| 4 | +2.72 | 0 | no | 7 | +2.740° | 0.122 |
| 5 | +8.16 | 1 | no | 7 | −8.455° | 0.375 |
| 6 | +13.60 | 0 | no | 7 | +14.897° | 0.661 |
| 7 | **+18.99** | 1 | **yes** | 7 | **−22.540°** | 1.000 |

Three things follow, none of which costs a solve.

1. **The field is EVEN in `x` and ALTERNATES with the column**, because `u` is odd and the glide
   factor `(−1)^b` is tied to the column parity by `C-0015`'s rule. It is a corrugation *along* the
   helices as well as across them, and that is not what a *"prestrain at the row ends"* picture
   suggests.
2. **The column next inboard carries 0.661 of the row end's amplitude at the OPPOSITE sign**, on
   the same number of sites. The cancellation is structural, and this line predicts it.
3. **The 42 interior sites carry 53.65 % of the assembled absolute couple** (86.25 pN·nm against
   the row ends' 74.51, of a whole-field 160.77) and their **net** couple, +30.35 pN·nm, opposes
   the row ends' −74.51. *"The other 42"* is not a remainder; it is the larger half.

**Cost justification, discharged.** The census ran first. Had it shown the interior amplitudes
small, the task would have closed on arithmetic and `C-0104`'s 14-site idealisation would have been
vindicated. It showed the opposite, which is what licensed the 73 solves that follow.

---

## Deliverable 2 — the decomposition is an IDENTITY, and the verdict is not

A prestrain changes no entry of the stiffness matrix (`C-0104` Deliverable 1), so for disjoint site
sets `R` (14 row-end) and `I` (42 interior) with `θ_G = θ_R + θ_I`,

&nbsp;&nbsp;&nbsp;&nbsp;`w(load, θ_G) = w(load, 0) + w(0, θ_R) + w(0, θ_I)` &nbsp;&nbsp;**exactly**.

| quantity | overall sign `+` | overall sign `−` |
|---|---|---|
| **superposition departure** in the coefficient vector | **2.1e−15** | **2.1e−15** |
| `‖G‖² − (‖R‖² + 2⟨R, I⟩ + ‖I‖²)`, relative | **2.1e−15** | 2.1e−15 |
| `‖R‖`, the row-end dishing field's area norm | 0.077353738 | 0.077353738 |
| `‖I‖`, the interior dishing field's area norm | **0.0532208269** | 0.0532208269 |
| `‖G‖` | 0.0635978729 | 0.0635978729 |
| **cosine `⟨R, I⟩/(‖R‖‖I‖)`** | **−0.579495374** | −0.579495374 |
| peak of the row-end-only prestrain response | 0.0740700165 | — |
| peak of the interior-only prestrain response | 0.0446322147 | — |
| peak of the graded prestrain response | **0.034903452** | — |
| **the graded peak over the sum of the two parts' peaks** | **0.294042089** | — |

> **The field separates exactly and the verdict does not.** Peak dishing is a seminorm, so it is
> **not** additive: the graded peak is 29.4 % of the sum of its parts'. **No part of the flatness
> verdict can be assigned to either site set** — the honest statement of the cancellation is the
> **cross term**, `2⟨R, I⟩`, and its convention-free measure is the cosine, **−0.579**.

This is the answer to the second half of the task's question — *"or the statement that the interior
field is not separable"*. It **is** separable, exactly, as a field; the *verdict* built on it is
not, and that distinction is the deliverable.

---

## Deliverable 3 — the 42 sites' own reading, and it is FLAT ON ITS OWN

Nine solved states on `C-0107`'s host (`gpd/results/T-190-interior-crossover-prestrain.json`):

| state | sites | dishing / stroke | free tile | flat? |
|---|---|---|---|---|
| zero prestrain — the reproduction gate | 0 | **0.0621469** | 0.2990348 | yes |
| the **graded** field, overall sign `+` | 56 | **0.0922622** | 0.2647317 | **yes** |
| its **14 row-end** sites alone, sign `+` | 14 | **0.1190748** | 0.2024181 | **no** |
| its **42 interior** sites alone, sign `+` | 42 | **0.0815294** | 0.3593760 | **yes** |
| the **graded** field, overall sign `−` | 56 | **0.0910197** | 0.3435120 | **yes** |
| its **14 row-end** sites alone, sign `−` | 14 | **0.1020545** | 0.4033704 | **no** |
| its **42 interior** sites alone, sign `−` | 42 | 0.0841713 | 0.2391763 | yes |
| `C-0107`'s row-end-only idealisation, `+22.6185°` | 14 | **0.1022820** | 0.4037347 | no |
| `C-0107`'s row-end-only idealisation, `−22.6185°` | 14 | **0.1193334** | 0.2020876 | no |

> **Each half alone fails or passes for the wrong reason.** The 14 row-end sites alone lose the
> convention **in both signs**; the 42 interior sites alone keep it **in both signs**; and the whole
> field keeps it more comfortably than the interior half does at one sign and less at the other.
> The **free-tile** column is the diagnostic: the interior half at sign `+` dishes 0.3594 where the
> row-end half dishes 0.2024, straddling the 0.2990 zero-prestrain value — they push the free tile
> in **opposite** directions, which is the cosine again, read without a coupling.

---

## Deliverable 4 — `C-0107`'s comparison differences three factors, and `F2` FIRED

`F2` was declared as *"the graded field's restriction to the 14 row-end sites is `C-0107`'s NOMINAL
`+22.6184533°` map"*. **It fired.** Measured — `uniformValueOrNull` returns `null` if the 14 do not
agree, and it does not — the graded field at overall sign `+` puts

&nbsp;&nbsp;&nbsp;&nbsp;**`−22.5397532°`**, at **all 14** interfaces,

because `C-0015`'s parity rule puts column 0 (`x = −18.99 nm`, parity 0) on the **even** interfaces
and column 7 (`x = +18.99`, parity 1) on the odd ones, and the glide factor then cancels the end
alternation to leave `−u(18.99)` everywhere. That is the sign `C-0107` calls **adverse**.

The `2 × 2 × 2` factorial separates the three factors, every cell a solve:

| overall sign | row-end station | interior | dishing / stroke | flat? | reproduces |
|---|---|---|---|---|---|
| `+` | row end, 19.04 nm | absent | **0.1193334** | no | `C-0107`'s `−θ₀` row |
| `+` | row end | present | 0.0923285 | yes | — |
| `+` | column, 18.99 nm | absent | **0.1190748** | no | — |
| `+` | column | present | **0.0922622** | yes | **`C-0107`'s graded field** |
| `−` | row end | absent | **0.1022820** | no | **`C-0107`'s `+θ₀` row** |
| `−` | row end | present | 0.0911034 | yes | — |
| `−` | column | absent | 0.1020545 | no | — |
| `−` | column | present | 0.0910197 | yes | — |

At a fixed overall sign the terms are clean:

| term | at sign `+` | at sign `−` |
|---|---|---|
| **station** (19.04 nm against the 18.99 nm column) | **0.0002586** | 0.0002275 |
| **interior** (42 sites on) | **0.0268125** | **0.0110348** |
| **sign** alone, at fixed station, no interior | **0.0170514** | — |

> `C-0107`'s published **0.0100** is `0.1022820 − 0.0922622`, i.e. an interior term **plus a sign
> flip running the other way**. The interior term at the consistent pairing is **0.0268125**,
> **2.68×** larger. That is [`CH-0129`](../challenges/CH-0129-the-graded-field-is-compared-against-the-other-overall-sign.md),
> and it makes `C-0107`'s own case stronger rather than weaker.

The **station** factor is worth 0.2 % and can never move a verdict, which is worth recording: the
0.05 nm `CrossoverLayout.EDGE_MARGIN` guard that `CH-0118`/`C-0090` showed can delete two columns at
this width is, *for the prestrain field's amplitude*, inert.

---

## Deliverable 5 — the ladder, and the cancellation OVERSHOOTS

Adding the column pairs inward from the row ends, one pair at a time, both signs:

| overall sign | column pairs | sites | dishing / stroke | flat? | fraction of the whole move recovered |
|---|---|---|---|---|---|
| `+` | 1 (the row ends) | 14 | 0.1190748 | no | 0.0000 |
| `+` | 2 (`± 13.60 nm` added) | 28 | **0.0906005** | **yes** | **1.0620** |
| `+` | 3 (`± 8.16 nm`) | 42 | 0.0915672 | yes | 1.0259 |
| `+` | 4 (`± 2.72 nm`, the whole field) | 56 | 0.0922622 | yes | 1.0000 |
| `−` | 1 | 14 | 0.1020545 | no | 0.0000 |
| `−` | 2 | 28 | **0.0877289** | **yes** | **1.2982** |
| `−` | 3 | 42 | 0.0912875 | yes | 0.9757 |
| `−` | 4 | 56 | 0.0910197 | yes | 1.0000 |

> **The first interior column pair does all of it and slightly more** — 106.2 % at sign `+` and
> 129.8 % at sign `−` — and the two inner pairs walk it back. Fourteen of the forty-two interior
> sites carry the cancellation; the other twenty-eight are a small correction to it. That is the
> `0.661` amplitude ratio of Deliverable 1 doing the work, and it was visible before any solve.

---

## Deliverable 6 — the cancellation over `C-0107`'s own bracket, and `F5` did NOT fail

`C-0107`'s boundary layer is bracketed, not known: two torsional rigidities × three `α` × two
smearing conventions. Graded and row-end-only re-solved at every cell and **both** overall signs —
40 cells, and 16 of them re-solved with the **lattice's own hinge moved with `α`** as well, which
`C-0107` did not do (it swept `α` inside the field only).

| | graded field | row-end-only idealisation |
|---|---|---|
| **flat at 0.10** | **40 of 40** | **14 of 40** |
| dishing range | **0.0822154 – 0.0959733** | 0.0862543 – **0.1268451** |
| cancellation `row-end − graded` | **0.00404 – 0.03087** of the stroke | — |

with `λ` running 11.781–24.042 nm (2.04×) and the end residual 17.1527–24.9830°. The
**self-consistent** `α` rows are flat at 24 of 24 and the field-only ones at 16 of 16, so moving the
lattice's hinge with the field's does not change the verdict.

> **The cancellation is a property of the field's SHAPE, and the shape is set by `λ/L`**, which is
> the one thing the bracket moves. Over its whole range the graded reading never leaves the
> convention and the row-end-only reading leaves it at 26 of 40 cells. **The row-end-only
> idealisation is the `λ → 0` limit of the graded field**, and the Gen-1 tile is nowhere near it:
> `λ = 18.62 nm` against a 19.04 nm half-row. `C-0104`'s three distributions are maps on 14 sites
> because `C-0104` had no field; `C-0107` has one, and it covers the tile.

---

## The five verification gates

**13 gate-named tests** in `src/test/kotlin/structure/InteriorCrossoverPrestrainTest.kt`, plus four
in-study `check`s, five convergence records and ten strict reproductions.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the partition counts 14 and 42, loses nothing and overlaps in nothing; a prestrain ledger is `k_θ × angle`, odd in the angle in its net and even in its absolute; an overall sign that is not `±1`, a non-positive hinge, a zero norm and a one-duplex lattice each throw | **PASS** |
| **2 — limiting cases** | `λ → 0` concentrates the field at the row ends — the interior's share of the absolute couple falls below `1e−6`, so `C-0104`'s 14-site idealisation is recovered **as a limit**; `λ → ∞` makes `u` linear in `x`, so two columns' amplitudes are in the ratio of their stations; a zero mismatch is a zero field; an empty restriction is empty | **PASS** |
| **3 — symmetry and conservation** | the graded field is **invariant under the tile's centro-symmetry** `(b, c) → (D−2−b, C−1−c)`, exactly, at every one of the 56 sites — the only reflection a Rothemund sheet has; the row-end restriction is **measured** uniform rather than asserted; the field splits into its two restrictions with no residue; the cosine expands a squared norm exactly; **superposition to 2.1e−15** in the study; and the standing uniform-load falsifier reads **2.126e−07** on the support-free, prestrain-free lattice | **PASS** |
| **4 — numerical convergence** | the closed form against an independent tridiagonal chain, 16 ⊂ 64 ⊂ 256, monotone and `< 1e−4`; beam subdivisions 1 → 2 → 4 on the graded field, **2.1 %** then **0.36 %**; the dishing sample grid 41 → 81 → 161, **0.0** twice; and **the cosine on its own axis**, subdivisions 2 → 4, **1.1e−05** — a derived quantity checked separately, per `CLAUDE.md` | **PASS** |
| **5 — literature and upstream** | `C-0090`'s 0.0621469105 at **3.0e−08**; `C-0107`'s zero-prestrain baseline, its **graded 0.0922622269**, both its row-end-only nominal states and its 22.6184533° end residual, all **read from its result file** and reproduced at **≤ 3.4e−09**; its uniform-load falsifier at 1.7e−09; `C-0104`'s threshold exactly; `C-0095`'s **14** and `C-0015`'s **56**, exactly | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the three solved fields do not superpose to `1e−10` | **NO** | **2.1e−15**, both signs. `C-0104`'s linearity holds and the decomposition is an identity |
| **F2** | the graded field's row-end restriction is `C-0107`'s **nominal** `+22.6184533°` map | **YES** | it is uniform at **−22.5397532°** — the *adverse* sign. [`CH-0129`](../challenges/CH-0129-the-graded-field-is-compared-against-the-other-overall-sign.md) |
| **F3** | the interior contribution is a small correction, `‖I‖ < 0.2‖R‖` | **NO** | `‖I‖/‖R‖ = **0.688**`, and the interior carries **53.65 %** of the absolute couple |
| **F4** | the graded field at the **other** overall sign is also flat | **YES** | **0.0910197**, flat. The verdict survives at both signs and `C-0107` read one. [`CH-0130`](../challenges/CH-0130-the-overall-sign-of-the-corrugation-is-undetermined.md) |
| **F5** | every cell of the boundary-layer bracket keeps the graded field flat | **YES** | **40 of 40**, against 14 of 40 for the row-end-only idealisation |
| **F6** | *(standing)* a uniform load on a uniform foundation dishes more than `1e−6` of the free stroke, on the support-free prestrain-free lattice | **NO** | **2.126e−07** |

**`CLAUDE.md`'s best falsifier is deliberately NOT declared against the eigenstrain.** A uniform
prestrain is an eigenstrain and the state that relaxes every hinge at once is a **cylinder** of
curvature `θ₀/d`, which `C-0104` Deliverable 5 measured (0.299 → 0.638 of the stroke). `F6` is the
**load-only** form, read on `withoutPrestrain` *and* on the support-free lattice — the two places
`CH-0120` and `C-0107` between them found this trap four times.

**What was not anticipated.** Four things. First, that the interior sites would carry **more** of
the eigenstrain than the row ends — the question was posed as *"what do the other 42 carry"* and the
answer is *"the larger half"*. Second, that the whole cancellation would be done by **one** column
pair and then slightly **over**done, so the inner 28 sites are a correction to a correction. Third,
that `C-0107`'s prose and `C-0107`'s solve would disagree about the overall sign — its gate asserts
the composition on an **assumed** end assignment and its study reads it off the **lattice**, and
they differ by exactly the global factor `CH-0130` shows is undetermined. Fourth, that the first
emission of this study's own result file would flatten its load-bearing `2.1e−15` to `0.0` through
`RESULT_ABSOLUTE_FLOOR` — the **third** recorded instance of *"an absolute floor is a claim about
units and it does not travel"*, in a study written by an agent who had read the entry.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** The prestrain is unknown and unsourced; `C-0107`'s
  literature search stands and is not repeated. This claim is about the **structure** of the field
  `C-0107` derived, not about its magnitude.
- **The model is `C-0107`'s, unchanged and unextended.** In particular the map from an azimuthal
  register error `u(x)` to a crossover **dihedral** prestrain is inherited whole, including its
  unmodelled prefactor and its overall sign; `CH-0130` is about the sign and says so.
- **Read at one operating state** — `C-0022`'s solved 2 mM / 10 nm / 0.192 V collar — and at
  **`C-0090`'s published 34-root key**, at phase 8, at 38.08 nm. The ninth instance in this project
  of *quote it with the state it is read at*. No re-optimisation is run; whether the placement
  family recovers more under the graded field is `T-185`.
- **The cosine and the norms are properties of the DISHING fields**, i.e. of the deflection with its
  best-fit plane removed, under `C-0063`'s convention. A different flatness convention would give a
  different cosine.
- **The bracket sweep moves `λ` and the field's shape; it does not move the placement, the phase,
  the collar or the width.** The self-consistent `α` rows move the lattice hinge as well and change
  no verdict, but they are 16 cells and not a study of the hinge.
- **Phase 8 only**, as `C-0099`, `C-0104` and `C-0107`. Phase 24 starts with less margin and its
  column positions differ, so both the census and the cancellation would have to be re-read there.
- **The result file is emitted with `floor = 0.0`** because its load-bearing quantities are
  dimensionless. Every departure in it is pre-rounded to **two significant digits**, which is what
  makes an unfloored emission reproducible.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the boundary layer `u(x)`, `λ`, `Δω` and the 22.6184533° nominal | — | **`C-0107`, READ FROM ITS RESULT FILE and REPRODUCED** to 3.8e−10 |
| `C-0107`'s eleven solved dishing states | — | **`C-0107`, READ FROM ITS RESULT FILE and REPRODUCED** (four of them, to ≤ 3.4e−09) |
| `C-0104`'s threshold, and that a prestrain is a load | 15.4497275° | **`C-0104`, READ FROM ITS RESULT FILE**; the linearity **re-asserted here at 2.1e−15** |
| `C-0090`'s reading and optimum placement key | 0.0621469105 | **`C-0090`, READ FROM ITS RESULT FILE and REPRODUCED** to 3.0e−08 |
| `C-0022`'s solved collar terms | — | **`C-0022`, READ FROM ITS RESULT FILE**, keyed on concentration, gap and bias |
| `k_θ = 2αB/(100a)` at `α = 1` | 13.5294118 pN·nm/rad | **CITED, FITTED** — Chen et al., *JACS* **136**:6995 — via `C-0009`, **re-derived and asserted** |
| `GJ` = 460 pN·nm², torsional persistence 100 nm | — | **CITED** — CanDo (2012); Kriegel et al. 2017 |
| interhelical distance, rise, bp/turn | 2.69 nm, 0.34 nm, 32/3 and 10.5 | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |

Everything else — the 8-column census and its three consequences, the superposition identity, the
cosine and the norms, all 9 solved states, the 8 factorial cells and their three-term separation,
the 8 ladder rungs, the 40 bracket cells, the centro-symmetry of the field and the five convergence
records — is **derived here in code**.

## Still open — named, not answered

1. **The overall sign is a design unknown with a measured cost.** 0.0012 of the stroke coupled,
   0.079 free. It is a **routing** fact — which interface parity carries which crossover type — and
   `C-0086` is the claim that would own it. `CH-0130`.
2. **The groove-asymmetry term** is still absent, as in `C-0107`: Rothemund names two causes and
   only the register one is modelled. It would enter this decomposition as a **second** field with
   its own `x`-dependence, and nothing here says the two cancel the same way.
3. **Phase 24 is not read.** Its column positions differ, so the `0.661` amplitude ratio that does
   all the cancelling is a phase-8 number.
4. **The placement is not re-optimised under the graded field.** `T-185`.
5. **The interior sites' peak crossover FORCE is not re-read.** The graded field's peak hinge
   moment is lower than either half's, which is worth checking against the per-path allowables.
6. **`CH-0129`'s repair to `EdgeTwistReliefTest`'s gate 3 is not applied here** — this claim does
   not edit `C-0107`'s files. It is one line and it belongs to whoever discharges the challenge.
