# C-0133 — **A SEAMLESS RASTER ROW CANNOT BE TWIST-CORRECTED EXACTLY, EVER, AND THE PROOF IS ONE LINE.** A boustrophedon needs an **odd** number of half turns across its row and a twist correction needs those half turns to be B-DNA's, so `N = 180q/34.2857 = 21q/4` with `q` odd — and `21q` is odd, so **`N` is never an integer**. **But the residual is an INVARIANT: exactly a quarter of a base pair, `8.5714°` across the whole tile, at every admissible width** — against `C-0086`'s 112 bp row's **1.75 bp = 60.0°**, exactly **7×**, a ratio of two integers. **The construction near §3's 40 nm exists and it is 110 bp = 37.40 nm**, seven domains of `16+15+16+16+16+15+16`, whose per-interface spacings are `31,31,32,32,31,31` — **Snodin's measured mix, arrived at from arithmetic**. It takes `C-0107`'s row-end prestrain from **+16.68…+24.79°** to **−3.56…−2.19°**, **12 of 12 cells below** `C-0104`'s 15.4497275° where 12 of 12 were above, and it **flips the sign**. **AND THE FLATNESS BARELY MOVES, WHICH IS THE RESULT**: at `C-0090`'s own placement shape the prestrain's contribution is `+0.0298` of the stroke at 112 bp and `+0.0279` at 110 — **1.07×** — because the correction **relocates** the strain into the interior (peak **12.37–12.84°** at the columns flanking the short domains) rather than removing it. Re-optimised, the corrected tile is genuinely better: **0.0580196384** against **0.0817325013**, and the winning **design moves**. It costs **one base pair of arm** (24 rises → 23) and the station lattice's **centro-symmetry** unless the out-of-plane offset is mirrored, which costs **30.0°** of azimuth at 8 of 52 stations

| | |
|---|---|
| **Task** | [`T-189`](../tasks/T-189-twist-corrected-raster.md), raised by [`C-0107`](C-0107-row-end-prestrain-value.md) *Still open* item 3 and by [`C-0086`](C-0086-seamless-scaffold-routing.md)'s own validity range |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** |
| **Verification type** | **logical** (an exact integer incompatibility and an exact residual invariant, both asserted over hundreds of half-turn counts rather than at one) **+ in-silico** (`C-0009`'s grillage and `C-0063`/`C-0090`'s exhaustive centro-symmetric enumeration re-run on a **non-uniform** column lattice constructed for this task, four enumerations of **163 296** placements each) **+ literature** (Snodin et al. and Rothemund 2006, already in `gpd/data/T-151-sources/` — `CLAUDE.md`'s *check `gpd/data/` before fetching anything*, which cost this task **zero** fetches) |
| **Verdict** | **PASS on all four predicates. The acceptance's BOTH branches are delivered, which is the shape nobody expected.** `P1`: the enumeration is closed form and ran before any solve — and it returns **an incompatibility**, exactly, for every row length at every domain mix. `P2`: and the *approximate* construction nevertheless exists, at **110 bp = 37.40 nm**, with a residual floor that is a **width-independent invariant**. `P3`: `C-0107`'s twelve-cell bracket is re-read, and the re-read is a **multiplication** because the boundary layer is exactly linear in its driver — proved, then measured at `0.0` on an independent discrete chain. `P4`: `C-0090`'s placement is re-enumerated on the corrected lattice and **the design moves, not only its value**. Raises [`CH-0158`](../challenges/CH-0158-the-admissible-width-list-assumes-an-uncorrected-twist.md) and [`CH-0159`](../challenges/CH-0159-a-zero-plan-clearance-cannot-survive-a-width-change.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The incompatibility theorem and the quarter-base-pair invariant are **arithmetic**; the register field is a derivation from constants this repository already carries; the flatness is `C-0009`'s lattice on `C-0022`'s carried collar. The motif the placement carries (`C-0055`'s free lever on one upward crossover) remains **undemonstrated** |
| **Provenance** | `gpd/results/T-189-twist-corrected-raster.json`, produced by `structure.TwistCorrectedRasterStudyKt` (**new**); model in `src/main/kotlin/structure/TwistCorrectedRaster.kt` (**new file** — `EdgeTwistRelief.kt`, `CrossoverPrestrain.kt`, `OrigamiGrillage.kt`, `anchoring/` and `coupling/` were **read, not edited**). **18 row records, 21 arrangements, 24 register cells, 3 lattice censuses, 4 solved dishing states, 4 exhaustive enumerations of 163 296 placements each, 6 convergence records, 11 upstream reproductions, 4 predicates, 7 falsifiers, 13 findings**; **19 gate-named tests in `src/test/kotlin/structure/TwistCorrectedRasterTest.kt`**, written **before** the model and run against it (one genuine failure on the first run — a `1e−9` symmetry tolerance on a chain deliberately conditioned at `1.5e8` — corrected to `1e−7` and the conditioning stated); the result file **re-run twice on an isolated tree and BYTE-IDENTICAL** across two independent JVM runs; `tools/verify.sh` **BUILD SUCCESSFUL in 22 m 06 s** on its own isolated tree — the whole suite, **no `--drop-file` needed** and no failure anywhere, with two concurrent agents' new sources in the tree; `tools/check-markdown-tables.py` clean over **413** files, `tools/check-challenge-index.py` **134 of 134 indexed**, `tools/check-result-file-hygiene.py` and `tools/check-kotlin-format-strings.py` clean, and `tools/check-corpus-links.py` clean over 406 tracked files with this claim's, both challenges' and the task file's own links checked by an explicit walk (`CLAUDE.md`: the script skips **uncommitted** files in a checkout) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes** at the SAXS 2.69 nm, **0.34 nm** rise, 32/3 bp per turn design against B-DNA's **10.5**; along-helix width **38.08 nm** (`C-0086`'s 112 bp) against **37.40 nm** (the 110 bp twist-corrected row) at crossover phase **8**; `C-0090`'s published 34-root key; `C-0017`'s **33.3333 pN/nm** mandate shared equally; `C-0022`'s solved collar at 2 mM, a 10 nm gap and **0.192 V**, **carried unchanged**; `C-0001`'s foundation secant; torsional rigidity **460 pN·nm²** (CanDo) and **414.2** (`l_t = 100 nm × k_BT`), crossover hinge `α = 0.6/1.0/1.2`, smeared spacing `p = 10.20` and **5.44 nm** — `C-0107`'s own `2 × 3 × 2` |
| **Consumes** | [`C-0107`](C-0107-row-end-prestrain-value.md) (`EdgeTwistRelief`, the boundary layer, the twelve-cell bracket, the glide sign, and its graded-field dishing **read from its result file**), [`C-0104`](C-0104-row-end-prestrain.md) (the threshold **read from its result file**; `OrigamiGrillage.crossoverPrestrains`), [`C-0090`](C-0090-buildable-raster-width.md) (the 38.08 nm width, phase 8, the 24-rise arm and the placement key **read from its result file and reproduced exactly**), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the odd-half-turn rule and the 112 bp row — **re-derived, not transcribed**), [`C-0063`](C-0063-upward-root-placement.md) (`rowRootOptions`, `armDirections`, the centro-symmetric family — **reproduced at 163 296**), [`C-0055`](C-0055-unused-junction-site.md) (the 8 bp plane lattice, the `EAST` azimuth rule, the 34 arms), [`C-0085`](C-0085-collinear-stacking-clearance.md) (the arm's quantisation to the rise), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`), [`C-0022`](C-0022-tile-edge-load-profile.md) (**read from its result file**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the column/interface parity rule), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage, `k_θ`), `Gen1Tile` |
| **Raises** | [`CH-0158`](../challenges/CH-0158-the-admissible-width-list-assumes-an-uncorrected-twist.md) against `C-0086`'s admissible-width list, [`CH-0159`](../challenges/CH-0159-a-zero-plan-clearance-cannot-survive-a-width-change.md) against `C-0090`'s zero plan clearance |

---

## The claim, in one line

**The two quantisations that a Gen-1 row has to satisfy are exactly incompatible, the incompatibility is worth exactly a quarter of a base pair whatever the tile size, and paying it converts a global one-signed twist into a local alternating one — which is a very large change in the driver and a very small one in the flatness.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, angles **degrees** at the interface and **radians** in the fields; `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂; rise **0.34 nm/bp**.
- **Design twist** `ω_d` — the azimuthal advance per base pair the *crossover lattice* imposes.
  **Natural twist** `ω_n = 360/10.5 = 34.2857 °/bp`, B-DNA's, `C-0015`'s and `C-0107`'s value.
- A **domain** is one inter-column stretch of a row, nominally 1.5 turns = 3 half turns.
- A **row** is one duplex, `N` base pairs, both ends carrying a scaffold crossover (`C-0095`).
- **Seamless-admissible** means the azimuthal advance across the whole row is an **odd** multiple of 180° — Rothemund's rule, binding the row length because a boustrophedon's successive scaffold crossovers are the two ends of one row (`C-0086`).
- **Dishing** is `C-0063`'s peak-dishing-over-free-stroke; **flat** means `≤ 0.10` (`T-5b`).

---

## Deliverable 1 — the theorem, which is the whole cheap bound and ran before any code

Two integer conditions on one row:

1. **connectivity** — `N ω_d = 180 q` with `q` **odd**;
2. **twist correction** — `ω_d = ω_n = 360/10.5`.

Together, `N = 180 q/ω_n = 5.25 q = 21q/4`. For odd `q`, `21q` is **odd**, so `21q/4` is never an
integer.

> **No integer row length is exactly an odd number of half turns at B-DNA's twist — at any width
> and at any domain mix.** The domain lengths do not enter: only their sum does.
> Asserted over the first **2001** odd half-turn counts. **`F1` did not fire.**

**And the residual is an invariant.** `21q ≡ 1` or `3 (mod 4)`, so the nearest integer to `21q/4` is
**exactly 0.25 away**, for every odd `q` — asserted over 201 counts, **`F2` did not fire**. The
accumulated twist a best-approximating row carries is therefore

&nbsp;&nbsp;&nbsp;&nbsp;`0.25 bp × 34.2857 °/bp = 8.5714°`, **independently of the row length**.

`C-0086`'s 112 bp row sits **1.75 bp** from its nearest odd half-turn count and carries **60.0°** —
`1.75/0.25 = ` **exactly 7**, a ratio of two integers, reproduced at departure `0.0`.

**The same quarter base pair is also the per-domain floor.** A 1.5-turn domain needs 540°; 16 bp of
B-DNA gives 548.571° and 15 bp gives 514.286°, so the cheapest domain any integer lattice can build
is misregistered by **8.571° = 0.25 bp**. A twist correction is never a *removal* of register error;
it is a choice of how many **25.714°** domains to buy in order to cancel the accumulation of the
cheap ones.

---

## Deliverable 2 — the construction, and the two lists that do not intersect

| domains `D` (odd) | half turns `q = 3D` | row length | width [nm] | design twist [°/bp] | total residual [°] | domain mix |
|---|---|---|---|---|---|---|
| 1 | 3 | 16 bp | 5.44 | 33.75000 | +8.5714 | `16` |
| 3 | 9 | **47 bp** | 15.98 | 34.46809 | −8.5714 | `16+15+16` |
| 5 | 15 | **79 bp** | 26.86 | 34.17722 | +8.5714 | `16+16+15+16+16` |
| **7** | **21** | **110 bp** | **37.40** | **34.36364** | **−8.5714** | **`16+15+16+16+16+15+16`** |
| 9 | 27 | **142 bp** | 48.28 | 34.22535 | +8.5714 | `16+16+16+15+16+15+16+16+16` |

Against `C-0086`'s list at the **uncorrected** 33.75 °/bp — 16, 48, 80, **112**, 144 bp, carrying
8.57, 25.71, 42.86, **60.00** and 77.14° — the two lists **intersect only at 16 bp**, and that is
[`CH-0158`](../challenges/CH-0158-the-admissible-width-list-assumes-an-uncorrected-twist.md).

**110 bp = 37.40 nm** is the entry nearest §3's 40.0 nm: 1.8 % narrower than `C-0086`'s 112 bp row
and 6.5 % below the nominal. Its per-interface spacings — two consecutive domains, i.e. the distance
between crossovers to the **same** neighbour — are **`31,31,32,32,31,31`**, which is Snodin et al.'s
measured tile in as many words:

> *"included a suitable number of sections with **31 base pairs between equivalent junctions** in
> order to remove this net twist"*

and Rothemund's own design program, *"helical domain lengths … by **single bases**"*. **Neither was
fitted to; both fall out of the arithmetic.**

**The arrangement of the two short domains is a design variable worth 2.23×, and the obvious rule is
not the best one.** Over the 21 arrangements the peak register runs **12.7746 to 28.5441°**; three
are centro-symmetric:

| short domains at | mix | row end [°] | peak [°] |
|---|---|---|---|
| 0, 6 | `15+16+16+16+16+16+15` | −8.1303 | 17.2791 |
| **1, 5** | **`16+15+16+16+16+15+16`** | **−3.1835** | **12.7746** |
| 2, 4 | `16+16+15+16+15+16+16` | −0.3955 | 18.3388 |

The innermost symmetric pair — what an even split writes down first — is the **worst** of the three.

---

## Deliverable 3 — the register re-read, and the re-read is a multiplication

`C-0107`'s boundary layer is `u(±L/2) = Δω λ tanh(L/2λ)` with `λ = √(Cp/k_θ)`, which contains no
`Δω`. **So the whole field is exactly linear in its driver**, and `C-0107`'s twelve cells re-read by
multiplication rather than by a sweep — proved by inspection, then measured on an independent
discrete chain at departure **`0.0`**. **`F4` did not fire**, and no sweep was paid for.

What the mixed-domain row needs beyond that is a **non-uniform** chain, because its per-domain
mismatch is no longer one number: `+8.571°` at a 16 bp domain and `−25.714°` at a 15 bp one. Solved
as a tridiagonal minimisation of `Σ (C/ℓᵢ)(Δu − δᵢ)²/2 + Σ (k_θ/p) wⱼ uⱼ²/2` with both ends free,
which **reproduces `EdgeTwistRelief.discreteEndResidual` to the last bit** on seven equal domains
(departure `0.0`).

| row | row-end register [°] | peak register [°], anywhere | above `C-0104`'s 15.4497°? |
|---|---|---|---|
| `C-0086`'s 112 bp | **+16.6754 … +24.7889** | **+16.6754 … +24.7889** (at the ends) | **12 of 12** |
| the 110 bp corrected | **−3.5581 … −2.1903** | **12.3708 … 12.8400** (in the interior) | **0 of 12** |

Three things in that table:

1. **The row end is removed and its sign flips.** A 110 bp row is *over*twisted by its own lattice
   where a 112 bp row is *under*twisted. **`F5` did not fire.**
2. **The strain is RELOCATED, not removed.** The corrected peak sits at the columns flanking the
   short domains, where `C-0086`'s peak is at the row ends. Peak against peak the correction is
   worth **1.93×**, against **7×** on the accumulated twist. `F5b` was declared on the peak and did
   not fire — *but only just*, 12.84° against 15.45°.
3. **The corrected field is nearly insensitive to the parameters `C-0107` had to bracket** — the
   peak spans **3.65 %** over the whole `2 × 3 × 2` `(C, α, p)` sweep against **32.7 %** for
   `C-0086`'s row, because a local per-domain strain is taken by the hinges at its own two columns
   while a global accumulation is set by a decay length. **The correction removes the parameter
   uncertainty as well as the value.**

---

## Deliverable 4 — the placement, and the answer is that the DESIGN moves

The corrected row's columns are non-uniform, so its column layout and its upward station lattice had
to be **constructed**, not inherited — and the construction is gated by reproducing
`rasterColumnLayout`, `rasterUpwardSites` and `centroSymmetricPlacements` **exactly** at a uniform
16 bp mix (departures `0.0`, `0.0`, and **163 296 = 163 296**). **`F3` did not fire.**

| lattice | stations | centro-symmetric | out-of-plane offsets | worst azimuth departure |
|---|---|---|---|---|
| 112 bp, 8 bp planes | **52** | **yes** | `8+8+8+8+8+8+8` | 4.29° |
| 110 bp, 8 bp planes | **52** | **NO** | `8+8+8+8+8+8+8` | 4.29° |
| 110 bp, **mirrored** planes | **52** | **yes** | `8+8+8+8+8+7+8` | **30.00°** |

The `EAST` site sits 8 bp past its column, which is **not** mirror-symmetric inside an **odd**
domain — so the twist correction silently breaks the centro-symmetry `C-0063`'s whole exhaustive
family assumes. Mirroring the offsets right of the row centre restores it exactly, at the price of
**8 of 52** stations sitting at a 7 bp offset whose azimuth departure is **30.0°** against 4.29°.
The station **count** is unchanged.

**And the plan costs one base pair of arm.** `C-0090` records that at 38.08 nm the outboard bound
owns the plan budget and `C-0085`'s 24-rise arm is **exactly tangent**. Moving every station 0.34 nm
inboard **empties** the 34-root centro-symmetric family at 24 rises; the largest arm that admits it
is **23 rises = 7.82 nm**. That is
[`CH-0159`](../challenges/CH-0159-a-zero-plan-clearance-cannot-survive-a-width-change.md).

| host | state | placement | dishing / stroke | flat at 0.10? |
|---|---|---|---|---|
| 112 bp | zero prestrain | `C-0090`'s published key | **0.0621469105** | yes |
| 112 bp | `C-0107`'s graded field | `C-0090`'s published key | **0.0919522067** | yes |
| 110 bp | zero prestrain | `C-0090`'s placement **shape** | **0.0669772989** | yes |
| 110 bp | the corrected graded field | `C-0090`'s placement **shape** | **0.0948701361** | yes |
| 112 bp | zero prestrain | **best of 163 296** | **0.0621469105** | yes |
| 112 bp | `C-0107`'s graded field | **best of 163 296** | **0.0817325013** | yes |
| 110 bp | zero prestrain | **best of 163 296** | **0.0602892387** | yes |
| 110 bp | the corrected graded field | **best of 163 296** | **0.0580196384** | yes |

**THE FLATNESS BARELY MOVES AT A FROZEN PLACEMENT, AND THAT IS THE RESULT THIS TASK EXISTS TO
REPORT.** Differencing the rows of that table (the subtraction is arithmetic on the result file's
own fields, not a quantity the file emits), the prestrain's **contribution** at `C-0090`'s own
placement shape is

&nbsp;&nbsp;&nbsp;&nbsp;`+0.0298052962` at 112 bp against `+0.0278928372` at 110 bp — **1.07×**,

against **7×** on the accumulated twist and 1.93× on the peak angle. Dishing responds to the whole
crossover prestrain **field**, not to its row-end value, and the corrected field is a sawtooth of
**9.11°** RMS against the uncorrected boundary layer's **14.08°**. It is `CLAUDE.md`'s own *"a
mandate written on a SUM is not a mandate on each term"*, read on a **driver** instead of on a
coupling.

**Re-optimised, the correction is worth more than that, and the design moves.** The same difference
over the exhaustive enumerations is `+0.0195855908` at 112 bp against **`−0.0022696003`** at 110 —
the corrected field's contribution changes **sign**, because a placement free to move can exploit an
alternating field where it can only fight a coherent one. The winning shape at 110 bp is **not**
`C-0090`'s, at either prestrain state; the winning shape at 112 bp is `C-0090`'s at zero prestrain
and not under the graded field. **All eight readings are inside `T-5b`'s 0.10.**

---

## The five verification gates

Executed as **19 gate-named tests** in `src/test/kotlin/structure/TwistCorrectedRasterTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a row length is a base pair count and its width a rise times it; the design twist is degrees per base and carries no length; the azimuth departure is 4.286° at 8 bp and −30.0° at 7; unphysical rows, domains, subdivisions and scales throw | **PASS** |
| **2 — limiting cases** | **THE THEOREM**, over 2001 odd half-turn counts; **the quarter-base-pair invariant**, over 201; a rigid hinge holds the register at zero and a free one lets the whole accumulation surface as `Δω L/2`, odd about the row centre; an even domain count is not seamless-admissible | **PASS** |
| **3 — symmetry and reproduction** | the non-uniform chain reproduces `EdgeTwistRelief.discreteEndResidual` at `<1e−10` relative; a centro-symmetric domain sequence gives an exactly **odd** register field; the generalised column layout and upward lattice reproduce `rasterColumnLayout` and `rasterUpwardSites` to `<1e−12` nm; mirroring the offsets restores centro-symmetry and is a **no-op** on equal domains | **PASS** |
| **4 — exactness and convergence** | the register field is **exactly linear** in the driver (halving halves every node); the domain mix is the unique even split and is centro-symmetric; the best row near 40 nm is 110 bp. Convergence: the hinge-smearing convention 1 → 2 → 4 sub-segments moves the peak register **1.0 %** then **0.26 %**; the dishing sample grid 41 → 81 → 161 moves **0.0**; the beam subdivision 1 → 2 → 4 moves **2.3 %** then **0.39 %** | **PASS** |
| **5 — literature and upstream** | **11 reproductions**: `C-0090`'s 0.0621469105 at departure `0.0` on an independently constructed lattice, `C-0104`'s 15.4497275°, `C-0107`'s 30.0° un-relieved accumulation and its graded-field dishing (`0.0922622269` against `0.0919522067`, **0.34 %** — the continuum field against this discrete chain), the 7× ratio, the 52-station census, `C-0063`'s 163 296-member family, and the two lattice constructions at machine precision. Snodin's 31 bp and Rothemund's single-base rule asserted as tests | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | an integer row length is exactly an odd number of half turns at 10.5 bp/turn | **no** | 0 of 2001 — the incompatibility is a theorem |
| **F2** | the residual is not exactly a quarter base pair at every odd half-turn count | **no** | 0.25 bp at all 201, exactly |
| **F3** | the generalised construction does not reproduce the uniform lattice | **no** | columns, stations and the 163 296-member family all exact |
| **F4** | the register field is not exactly proportional to the twist mismatch | **no** | departure `0.0` |
| **F5** | the corrected row's **row-end** register still exceeds 15.4497° | **no** | −3.56 to −2.19° |
| **F5b** | the corrected row's **peak** register, wherever it sits, exceeds 15.4497° | **no, but only just** | **12.37–12.84°** — the interior peak is 83 % of the threshold, where the row end is 23 % |
| **F6** | a free plate under a uniform load on a uniform Winkler foundation dishes something | **no** | largest over 4 states `2.13e−07` |

---

## Still open — named, not answered

1. **The phase census at 110 bp is not re-derived.** `C-0090`'s collapse of `C-0015`'s ten
   eight-column phases to two is an identity in the **uniform** 16 bp pitch, and a twist-corrected
   row has no uniform pitch. This claim solves at the one phase `C-0090` recommends and does not
   ask what the phase lattice of a mixed-domain row even is. **That is the largest single gap here.**
2. **`C-0022`'s collar is carried, not re-solved**, at 37.40 nm — the same open item `C-0090` carries
   at 38.08 nm, one base pair further from where it was solved.
3. **Whether §3 wants a 37.40 nm tile.** 110 bp is 6.5 % below the nominal 40.0 nm where 112 bp is
   4.8 % below. This is a specification question and it belongs beside `C-0086`'s scaffold question
   and `C-0090`'s *"would §3 rather have a bigger tile"*.
4. **The 30° azimuth departure at the mirrored stations is priced as an angle and not as a joint.**
   `C-0055`'s arm roots on that azimuth; what a 30° register departure does to a crossover-rooted
   lever is `T-9`'s question, not this one's.
5. **The measured staple dropout is not applied.** Every dishing here is a zero-defect optimum, and
   `C-0092`/`C-0103` establish that such optima are cancellations a missing path destroys.
6. **The honeycomb four-layer tile is not read, and the cheap bound suggests it needs nothing.**
   `CLAUDE.md` records that *"`10.67 bp/turn` is the **square** lattice and `10.5 bp/turn` the
   **honeycomb**"* — so a honeycomb lattice is laid out at B-DNA's own twist and its `Δω` is
   **zero**, which would make every number in this claim a square-lattice number and give
   `C-0126`'s four-layer line a favourable argument it has not made. What does **not** transfer is
   the connectivity half: `21 bp = 2 turns` is an **even** number of half turns, so the
   odd-half-turn rule binds a different integer there. **`T-217`.**

## Challenges

**Raises [`CH-0158`](../challenges/CH-0158-the-admissible-width-list-assumes-an-uncorrected-twist.md)**
against `C-0086`'s *"the buildable widths are the odd multiples of 16 bp"*: the list is a property of
the `(sheet, design twist)` pair, and the second entry is exactly the defect `C-0107` prices.

**Raises [`CH-0159`](../challenges/CH-0159-a-zero-plan-clearance-cannot-survive-a-width-change.md)**
against `C-0090`'s exactly-zero plan clearance: the first design change the programme contemplated
emptied the 34-root family, and the arm quantum has to be quoted with the width.

**`C-0107`'s *Still open* item 3 is CLOSED**, and it is closed with a shape the item did not
anticipate: the correction is available, it removes the driver, and it moves the flatness by 7 %.

**Nothing here overturns `C-0107`.** Its 17.15–24.98° stands as the prestrain of an **uncorrected**
row, which is the row `C-0086` selects and the row every plan claim is written on today.
