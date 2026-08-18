# C-0107 — **THE CHEAP BOUND DOES NOT CLOSE IT, AND THE DERIVED VALUE IS PAST THE THRESHOLD.** All **8** ceilings on a row-end crossover's prestrain lie **above** `C-0104`'s 15.4497275° — rupture by 5.5–36×, because the couple at the threshold is 3.65 pN·nm. And `C-0104`'s ladder is the wrong ladder: its rungs are **per-domain** register offsets, but every domain's error carries the **same sign**, so it *accumulates* along a duplex and what limits it is the duplex's own torsion. The boundary layer `u(±L/2) = Δω λ tanh(L/2λ)`, `λ = √(Cp/k_θ) = 11.8–24.0 nm` against a 19.04 nm half-row, leaves **17.15–24.98°** at a free row end over a 12-cell parameter bracket — **12 of 12 above the threshold**, against an un-relieved 30.00°. **The sign composes to `C-0104`'s ADVERSE distribution**: Rothemund's glide symmetry flips the crossover type with the interface parity and a boustrophedon's raster turns alternate ends, and the two `(−1)^b` cancel. Read as `C-0104` reads it — the 14 row-end sites alone — the published placement **loses `T-5b`'s 0.10 in both signs** at the nominal value (0.1023 / 0.1193); read as the **field** it actually is — `(−1)^b u(x)` at all 56 crossovers — it **keeps** it at **0.0922622**, with **7.7 %** of the convention unused where zero prestrain leaves 37.9 %. **The one published measurement of this exact coordinate excludes exactly these sites**: Snodin et al. (2019) measure the interduplex corrugation of a 2D origami by oxDNA at ≈ 8–9° average and ≈ ±16° per junction, and write that they *"exclude the outermost junctions on the tile"*. **So `C-0099`'s recommendation is REVERSED and the oxDNA run is warranted** — one row end and its two neighbours, ~256 nt, an order of magnitude inside `T-9`'s own cost model

| | |
|---|---|
| **Task** | [`T-182`](../tasks/T-182.md), raised by [`C-0104`](C-0104-row-end-prestrain.md) *Still open* item 1 |
| **Leaf** | **`A1.2`** (the molecular model the quantity lives in), with **`A8.2`** (the plan and lattice model it enters) |
| **Verification type** | **logical** (four families of closed-form ceiling, one of them a boundary-value problem with an exact solution) **+ literature** (20 EuropePMC queries over 156 unique records plus 10 targeted title queries, five full texts fetched, one figure digitised) **+ in-silico** (`C-0090`'s 34-root placement re-solved at 11 prestrain states on an **explicit elastic-support** grillage, an independent code path from `C-0058`'s Woodbury surrogate) |
| **Verdict** | **PASS on all four predicates, and the declared falsifier `F2` FIRED IN THE OTHER DIRECTION.** `P1`: 8 ceilings, every one compared against 15.4497275°, and **every one lies above it** — so the task does **not** close on the cheap bound and `C-0099`'s recommendation does not stand on arithmetic. `P2`: the modalities `C-0104` did not search are searched, every load-bearing source carries a read status, every query string is recorded. `P3`: a **value with a bracket** is delivered — 17.15–24.98°, from a mechanism that is named in print — and the **sign** is decided by a two-flip lattice congruence rather than assumed. `P4`: the run is **warranted** and costed. Raises [`CH-0122`](../challenges/CH-0122-the-prestrain-threshold-is-a-secant-not-a-triangle-inequality.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The boundary layer is a **derivation**, new in this claim, from constants this project already carries; the closest published *measurement* is an oxDNA study of the interior of a **twist-corrected** tile, and it excludes the row ends. That gap is the claim. |
| **Provenance** | `gpd/results/T-182-row-end-prestrain-value.json`, produced by `structure.EdgeTwistReliefStudyKt` (**new**); model in `src/main/kotlin/structure/EdgeTwistRelief.kt` (**new**). **No existing source file was edited** — `OrigamiGrillage`, `CrossoverPrestrain.kt`, `RowEndPrestrainStudy.kt`, `anchoring/` and `coupling/` were **read, not modified**. **8 ceilings, 12 boundary-layer cells, 11 solved dishing states, 8 literature records, 5 convergence records, 7 upstream reproductions, 6 oxDNA cost records, 4 predicates, 6 falsifiers, 8 findings**; **16 gate-named tests in `src/test/kotlin/structure/EdgeTwistReliefTest.kt`**, red-checked (7 unresolved references, then the whole suite green); literature, drivers and query logs in `gpd/data/T-182-sources/` (`MANIFEST.md`, `query.py`, `targeted.py`, `fetch.py`, `fetches.json`, and the rendered figure page); `tools/result-reader-census.py --emit` re-run; `tools/check-markdown-tables.py` clean over 335 files; `tools/verify.sh` **BUILD SUCCESSFUL in 21 m 34 s** on its own isolated tree with **one** concurrent agent's mid-TDD file dropped by `--drop-file` (`src/main/kotlin/coupling/CountPhaseInteractionStudy.kt`, which fails `compileKotlin` with a syntax error and which nothing in `structure` imports) and nothing else — **no test failure anywhere**, and the four post-Gradle gates are skipped by that flag, so they were run by hand instead; the result file **re-run through `tools/study.sh` on an isolated tree and BYTE-IDENTICAL** across two independent JVM runs (`tools/study.sh` reports *"no result file changed"*), after every dimensionless departure and convergence measure was rounded to **two significant digits** (`CLAUDE.md`'s `RESULT_ABSOLUTE_FLOOR` trap and `C-0102`'s cure) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes** at the SAXS 2.69 nm, 0.34 nm rise, **32/3 bp per turn design against B-DNA's 10.5**, 16 bp column pitch, 32 bp per-interface spacing; along-helix width **38.08 nm** at crossover **phase 8**; `C-0090`'s published 34-root key; `C-0017`'s **33.3333 pN/nm** shared equally as **explicit elastic supports**; `C-0022`'s solved collar at 2 mM, a 10 nm gap and 0.192 V; `C-0001`'s foundation secant; free stroke **5.15473846 nm** |
| **Consumes** | [`C-0104`](C-0104-row-end-prestrain.md) (the term, the threshold and the three distributions — **read from its result file**), [`C-0099`](C-0099-row-end-crossover-stiffness.md) (the recommendation this claim reverses), [`C-0090`](C-0090-buildable-raster-width.md) (the 38.08 nm width, phase 8, the placement key — **read from its result file and reproduced**), [`C-0095`](C-0095-row-end-crossover.md) (the row-end crossover is a scaffold raster turn; the 14), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the 112 bp row and the boustrophedon), [`C-0009`](C-0009-discrete-lattice-tile.md) (`OrigamiGrillage`, `k_θ`), [`C-0029`](C-0029-perpendicular-junction-routing.md) (the two-termini count, for the rupture ceiling), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 56, the column/interface parity rule), [`C-0022`](C-0022-tile-edge-load-profile.md) (**read from its result file**), `Gen1Tile` |
| **Raises** | [`CH-0122`](../challenges/CH-0122-the-prestrain-threshold-is-a-secant-not-a-triangle-inequality.md), against `C-0104` |

---

## The claim, in one line

**`C-0104` asked how much prestrain the flatness can survive; this asks how much there is, and the answer is more — because the register error does not sit in one domain, it walks to the end of the row and stops only where the duplex's own torsion stops it.**

---

## The conventions, restated rather than inherited

- Angles **rad** (degrees where a source quotes them), rotational stiffness **pN·nm/rad**,
  torsional rigidity **pN·nm²**, couples **pN·nm**, lengths **nm**, forces **pN**;
  `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
- `x` along the helices measured from the **row centre**, `y` across them, `z` normal and positive
  **upward**; `w` positive **downward**.
- A **prestrain** `θ₀` is `C-0104`'s, unchanged: the relative roll a crossover is *built at*, so its
  hinge stores `½k_θ(Δφ − θ₀)²`.
- `u(x)` is the **azimuthal register error** of a duplex: the phase by which its backbone lags the
  phase the crossover lattice was laid out at. `u ≡ 0` is a duplex wound to the **design** twist;
  `u′ ≡ Δω` is a duplex wound to its **natural** twist.
- **Dishing** is `C-0063`'s, unchanged, and **flat** means `≤ T-5b`'s 0.10 of the free stroke.

---

## Deliverable 1 — four families of ceiling, and not one of them closes the question

`F2` was declared as *"every ceiling lands below 15.4497275°, so the question closes on the cheap
bound and `C-0099`'s recommendation stands"*. **It fired in the other direction: 8 of 8 lie above.**

| ceiling | degrees | closes it? | status |
|---|---|---|---|
| rupture — unzip allowable, 10 pN | **84.698** | no | **DERIVED** (allowable `CITED`, `CLAUDE.md`) |
| rupture — unzip allowable, 15 pN | 127.047 | no | DERIVED |
| rupture — duplex shear, 48 pN | 406.551 | no | DERIVED |
| rupture — the nicked ceiling, 65 pN | 550.538 | no | DERIVED |
| register quantum — half a base-pair step | **17.143** | no | DERIVED (`C-0104`'s own rung, re-derived) |
| Rothemund's remedy — one unpaired base | 34.286 | no | **CITED, READ DIRECTLY** |
| Rothemund's remedy — two unpaired bases | 68.571 | no | CITED, READ DIRECTLY |
| **the twist boundary layer at a free row end** | **24.983** | no | **DERIVED — Deliverable 2** |

**Rupture does not bind, and the reason is a number.** `k_θ = 2k_bond a²` on `C-0029`'s two strand
termini at the measured 1.00 nm phosphate radius inverts to `θ₀ ≤ 2aF/k_θ`; at the threshold the
crossover's whole couple is `k_θ θ₀ = **3.65 pN·nm**`, i.e. **1.82 pN per bond**. A crossover hinge
is soft, so a prestrain that destroys a flatness verdict is nowhere near one that breaks a bond.

**Rothemund's own remedy is a scale, and it is the right order.** *"If, in the future, strain
associated defects should be detected at edges, then one or two scaffold bases could be left
unpaired and allowed to form a hairpin that should relax the crossover."* A remedy sized at one to
two bases prices the strain it relieves at one to two bases of twist — 34.3° to 68.6°.

**Cost justification, discharged.** Four ceilings and one closed form ran before any solve and
before any fetch. Had one landed below 15.45° the branch would have closed on arithmetic. None did,
which is what licensed the eleven grillage solves and the literature spend that follow.

---

## Deliverable 2 — the mechanism `C-0104`'s ladder leaves out, and it is the one that produces a value

`C-0104`'s ladder is a ladder of **per-domain** register offsets: 4.286° over 8 bp, 8.571° over
16 bp, 17.143° over 32 bp. Rothemund, read directly, says why that is not the whole quantity:

> *"The use of 16 bases to represent 1.5 turns of DNA … means that the helical domains between
> crossovers are slightly **overtwisted or undertwisted**"*

— every domain, the same sign. **A same-signed per-domain error accumulates**, and the accumulation
is what the literature calls the global twist of a square-lattice origami. What limits it is the
duplex's own torsion. Smearing the crossovers at their mean contour spacing `p`,

&nbsp;&nbsp;&nbsp;&nbsp;`E = ∫ [½C(u′ − Δω)² + ½(k_θ/p)u²] dx`, &nbsp;&nbsp;&nbsp; `u″ = u/λ²`, &nbsp;&nbsp;&nbsp; `λ = √(Cp/k_θ)`,

with the **natural** boundary condition `u′ = Δω` at a free duplex end — which is exactly what a row
end is — giving `u(±L/2) = ± Δω λ tanh(L/2λ)`, **odd about the row centre**, with the two limits
`Δω L/2` at `k_θ → 0` and `0` at `k_θ → ∞`, both asserted as tests.

`Δω = 0.0275 rad/nm` (34.2857 − 33.75 degrees per base over a 0.34 nm rise), `L = 38.08 nm`.

| `C` (pN·nm²) | `α` | `p` (nm) | `λ` (nm) | **`u(L/2)`** | relief |
|---|---|---|---|---|---|
| 460 (CanDo `GJ`) | 0.6 | 10.20 | 24.04 | **24.983°** | 16.7 % |
| 460 | 1.0 | **10.20** | **18.62** | **22.618°** | 24.6 % |
| 460 | 1.2 | 10.20 | 17.00 | 21.631° | 27.9 % |
| 414.2 (`l_t` = 100 nm × `k_BT`) | 0.6 | 10.20 | 22.81 | 24.548° | 18.2 % |
| 414.2 | 1.0 | 10.20 | 17.67 | 22.059° | 26.5 % |
| 414.2 | 1.2 | 10.20 | 16.13 | 21.034° | 29.9 % |
| both | 0.6–1.2 | 5.44 | 11.78–17.56 | **17.153–21.989°** | 26.7–42.8 % |
| — | — | — | — | **un-relieved limit `Δω L/2` = 30.000°** | 0 |

> **12 of 12 cells lie above `C-0104`'s 15.4497275°**, and the bracket over the whole `2 × 3 × 2`
> sweep is **17.15–24.98°**. The relief a free end buys is only **17–43 %**, because `λ` is
> comparable to the half-row: the tile is *too small* for its own duplexes to untwist their way out.

**`p` is the one convention here and it is bracketed, not chosen.** The principled reading is
`p = D·L/N_c = 10.20 nm` — the mean contour of one duplex per crossover, with the hinge energy
halved because a crossover is shared by two duplexes; the other end is the bare 16 bp column pitch,
5.44 nm. They differ by 3.9° at `α = 1`, less than the `α` bracket.

---

## Deliverable 3 — the sign, which is a lattice congruence and composes to the ADVERSE case

Three facts, each read directly or already asserted in this repository:

1. **Rothemund's glide symmetry.** *"Alternating columns of staple crossovers are related by a
   glide symmetry — the local configuration of crossovers in one column is identical to that of
   crossovers in the next column over after a translation and a 'flip' (a rotation about one of the
   crossovers in-plane axes) … This symmetry should tend to balance strain in the origami and cause
   them to be, on average, flat."* A flip about an in-plane axis reverses the out-of-plane sense, so
   the fold a given azimuthal error induces carries a factor `(−1)ᶜ` in the column index.
2. **`C-0015`'s parity rule.** A column of parity `p` serves the interfaces of parity `p`, so
   `(−1)ᶜ = (−1)^b` in the interface index — the glide alternation **is** the corrugation.
3. **`C-0104`'s own lattice finding.** *"Each of the 14 row-end crossovers is the only one on its
   interface, and which end it sits at alternates with the interface"* — a boustrophedon's raster
   turns alternate ends. So the row-end crossover of interface `b` sits where `u = (−1)^b u_max`.

**The two `(−1)^b` cancel.** `θ₀(b) = (−1)^b · (−1)^b u_max = +u_max`, every interface, the **same
sign** — which is `C-0104`'s **uniform** distribution, and the uniform distribution is the only one
of its three that ever crosses 0.10. Asserted as gate 3 over all 14 interfaces.

> The favourable reading was available and is false: it is *not* the opposed-ends map, and the
> reason it is not is that the raster's own alternation is already spent cancelling the glide's.

---

## Deliverable 4 — what it does to `C-0090`'s recommended design, on an independent code path

Eleven solves at `C-0090`'s published 34-root key, 38.08 nm, phase 8, under `C-0022`'s collar.
The coupling is 34 **explicit elastic supports** at `33.3333/34 pN/nm`, not `C-0058`'s Woodbury
surrogate — which is why the first row is a **gate** and not a tautology.

| state | `θ₀` | dishing/stroke | flat? | peak hinge moment |
|---|---|---|---|---|
| **zero prestrain — the reproduction gate** | 0° | **0.0621469** | **yes** | 0.377 pN·nm |
| 14 row-end sites, low end of the derived bracket | **+21.034°** | 0.0977014 | **yes** | 3.121 |
| the same, adverse sign | −21.034° | **0.1141262** | **no** | 3.083 |
| 14 row-end sites, **nominal** | +22.618° | **0.1022820** | **no** | 3.343 |
| the same, adverse sign | −22.618° | **0.1193334** | **no** | 3.304 |
| 14 row-end sites, high end | ±24.983° | 0.1091174 / 0.1271038 | no / no | 3.674 |
| the un-relieved rigid limit | ±30° | 0.1236204 / 0.1435906 | no / no | 4.376 |
| the same nominal on the **glide-alternating** map | 22.618° | 0.0871795 | yes | 3.172 |
| **the GRADED corrugated field, `(−1)^b u(x)` at all 56 crossovers** | 22.618° peak | **0.0922622** | **yes** | 2.253 |

**Two readings, and they disagree about the verdict.**

- Read as `C-0104` reads it — a prestrain on the **14 row-end sites alone** — the published
  placement **loses `T-5b`'s 0.10 in both signs** at the nominal derived value, and survives only at
  the soft end of the bracket in the favourable sign.
- Read as the **field the boundary layer actually is** — every crossover at `x` carrying
  `(−1)^b u(x)`, the interior ones included — it **keeps** the verdict at **0.0922622**, because the
  42 interior sites partly cancel the 14 at the ends.

> **The difference between the two readings is the 42 interior crossovers, which nobody has
> measured either.** The honest statement is that at the derived value the design retains **7.7 %**
> of the convention where zero prestrain retains **37.9 %** — a 4.9× loss of margin on a quantity
> with no measurement behind it. That is the exposure, and it is a *design* exposure rather than a
> feasibility one, because `C-0104` has already shown that re-optimising the 163 296-placement
> family recovers flatness at ±17.14°.

**The free (uncoupled) tile moves the same way**: 0.299035 at zero prestrain against 0.403735 at the
nominal `+θ₀` and 0.264732 under the graded field, so the coupling is neither amplifying nor
suppressing the eigenstrain.

---

## Deliverable 5 — the literature, extended past `C-0104`'s, with a read status per source

**20 EuropePMC queries over 156 unique records**, plus **10 targeted title queries**, plus five full
texts fetched and read. `gpd/data/` was checked **first** and it paid: two of the three load-bearing
sources were already in the repository, fetched by `T-151`.

| question | source | read as | quantified? |
|---|---|---|---|
| Has an oxDNA study measured the interduplex roll at origami crossovers? | Snodin, Schreck, Romano, Louis & Doye, *Nucleic Acids Res.* **47**:1585 (2019) | **READ DIRECTLY** (text); **Figure 5 rendered and digitised against its own axis** | **YES — ≈ 8–9° in the average, ≈ ±16° per junction** |
| Does it cover the **row-end** crossover? | the same paragraph | **READ DIRECTLY** | **NO, and it says so** |
| Is the accumulating-register mechanism the published one? | the same paper | **READ DIRECTLY** | **YES — 17.14° per 32 bp domain, inferred from its own 31/32 bp cure** |
| Do the free junction's own preferred angles bound the out-of-plane term? | the same paper, Figure 1C | **READ DIRECTLY** | partly — `θ = 4.5°` for `160° ≤ φ ≤ 180°` |
| Is the accumulation seen over a whole row experimentally? | Ni et al., *iScience* **25**:104373 (2022), cryo-EM | **READ DIRECTLY** | as a **global twist**, not per crossover |
| An all-atom reading of the same junction? | Yoo & Aksimentiev, *PNAS* **110**:20099 (2013) | **READ DIRECTLY** | junction dihedral mean **−4°**; `ω₃` = −1.3° per array cell, **NOT load-bearing** (the cell length for the SQ design is not stated in the passage) |
| Is the edge strain unrelieved, and does anyone know how it relieves? | Rothemund, *Nature* **440**:297 (2006), Suppl. Note S2 | **READ DIRECTLY** | **NO — *"How the strain is actually relieved is unknown"*** |
| Is the edge domain length a design variable tuned by hand? | the same, Suppl. Note S1 §4 | **READ DIRECTLY** | **YES**, quantised at **one base**, 34.29° |
| The one direct **experimental** global-twist measurement of a single-layer origami | Baker et al., *ACS Nano* **12**:5791 (2018) | **NOT OBTAINED** — ACS `403`, Crossref carries no abstract | — |

**The load-bearing sentence of the whole search** is Snodin et al.'s own scope statement:

> *"Note, we do not include data for all junctions, but only those that have the canonical pattern
> of neighbouring junctions; thus, we **exclude the outermost junctions on the tile**, and the
> junctions next to the scaffold seam as well as the seam itself."*

The protocol for measuring exactly this quantity is published, applied, and **explicitly excludes
the sites `T-182` asks about**. And the tile they measure is **twist-corrected** — *"the current
design has included a suitable number of sections with 31 base pairs between equivalent junctions in
order to remove this net twist"* — so their 8–9° is what remains when the register mismatch has been
**designed out**, which `C-0086`'s 112 bp seamless raster has not.

---

## Deliverable 6 — the oxDNA verdict, and it is a reversal

`C-0099` recommended **against** the run: *"it would resolve a quantity worth 2.85 % of an interval
the verdict does not cross."* That was correct for a **stiffness**. It does not survive for a
**prestrain**, which is a *load*: the verdict crosses at 15.4497° (11.5188° on the true triangle
ceiling, `CH-0122`), and every route to a value lands in **8–30°**.

| item | value |
|---|---|
| minimal system | one row end plus its two neighbouring junctions — a 2-duplex, 2 × 32 bp motif, **≈ 256 nt** |
| the whole 38.08 nm × 15-duplex tile | **3 360 nt** |
| `T-9`'s own cost model | *"2–5 k nucleotides, µs-scale umbrella sampling on 8 cores, **days not weeks — it fits this box**"* |
| verdict against that model | the **tile** sits at the top of `T-9`'s range; the **motif** is an order of magnitude inside it |
| what it must resolve to be worth running | the row-end residual to better than **±3°**, against a 17.15–24.98° bracket and a 15.45° threshold |
| the protocol | **published** — Snodin et al.'s inter-helix-vector angle, applied to the junctions they excluded |
| toolchain | oxDNA needs `g++`/`cmake`, absent by default on this box and installable (`CLAUDE.md`); **no GPU is required at this size** |

> **It fits this VPS**, and it should be run at the *row end* rather than over a tile. The cheapest
> version of it answers two questions at once, because `T-9` wants `k_θ` and the crossover's vertical
> compliance from the same equilibration.

**And there is a cheaper design action that does not need the run at all.** Snodin et al.'s design
removes the mismatch by mixing 31 bp and 32 bp inter-junction domains, and Rothemund's program tunes
edge domain lengths by single bases for the same reason. `C-0086`'s seamless 112 bp raster was
quantised on connectivity alone and has never been checked for twist correction. **A twist-corrected
row length would take `Δω` toward zero and the whole boundary layer with it** — new task `T-183`.

---

## The five verification gates

**16 gate-named tests** in `src/test/kotlin/structure/EdgeTwistReliefTest.kt`, plus four in-study
`check`s and seven strict reproduction records.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `λ = √(Cp/k_θ)` is a length and halves when `k_θ` quadruples; the twist rate mismatch is rad/nm and positive; a non-positive rigidity, stiffness, spacing or length, a `NaN` mismatch and a zero rise each throw | **PASS** |
| **2 — limiting cases** | `k_θ → 0` returns the free-duplex `Δω L/2` to `1e−6`; `k_θ → ∞` returns zero; `C → ∞` returns the rigid limit; a zero mismatch is a zero residual everywhere; the relieved residual never exceeds the rigid limit | **PASS** |
| **3 — symmetry and conservation** | `u` is **exactly odd** about the row centre and zero at it; the corrugated field alternates with the interface index; **the two `(−1)^b` compose to `+1` at every one of the 14 interfaces**, which is the uniform distribution; and the standing uniform-load falsifier reads **`2.126e−7`** of the free stroke on `withoutPrestrain` at every state | **PASS** |
| **4 — numerical convergence** | the closed form against an independent **discrete tridiagonal chain**, 16 ⊂ 64 ⊂ 256 elements, monotone and `< 1e−4` relative at 256 (**1.4e−4** over 64 → 256 in the study); grillage subdivisions 1 → 2 → 4 at the adverse nominal, **0.89 %** then **0.096 %**; the dishing sample grid 41 → 81 → 161, **0.0** | **PASS** |
| **5 — literature and upstream** | `C-0090`'s **0.0621469105** reproduced at **3.0e−08** *on an independent explicit-support code path*; `C-0104`'s baseline at 3.0e−08 and its `dishingPerRadian` **on `C-0104`'s own definition** at **5.1e−08**; `Gen1Tile`'s 13.5294118 pN·nm/rad at 2.6e−09; `CLAUDE.md`'s 8.5714286° at 3.3e−09; `C-0095`'s **14** and `C-0015`'s **56**, both exactly | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the boundary-layer model fails its own limits | **NO** | both limits reproduced to `1e−6`, gate 2 |
| **F2** *(the declared one)* | **every** ceiling lands below 15.4497275°, closing the question and upholding `C-0099` | **FIRED, IN THE OTHER DIRECTION** | **8 of 8 lie above**, and the one that is a value rather than a limit is among them |
| **F3** | the independent explicit-support path does not reproduce `C-0090`'s 0.0621469105 and `C-0104`'s slope on `C-0104`'s own definition | **NO** | 3.0e−08, 3.0e−08, 5.1e−08 |
| **F4** | a source **quantifies** the residual angle at a row-end crossover | **NO** for the row end | Snodin et al. quantify the **interior** at 8–9° / ±16° and exclude the outermost junctions in as many words |
| **F5** | the sign composition does not select one of `C-0104`'s three distributions | **NO** | it selects **uniform**, the adverse one, at all 14 interfaces |
| **F6** | the standing one: a uniform load on a uniform foundation dishes more than `1e−6` of the free stroke, on `withoutPrestrain` | **NO, after firing once and being right to** | worst **2.126e−07** on the **support-free**, prestrain-free lattice. Read first on the 34-anchor **coupled** lattice it read **0.170** and fired — correctly, because a sparse coupling is itself a dishing source (`C-0060`) and that is not what this falsifier is about. `C-0104` avoids it structurally (its coupling lives in the Woodbury surrogate, not in the lattice); an explicit-support path has to read it on the free lattice. **Fourth instance of a trap around this term**, after `CH-0120`'s three |

**What was not anticipated.** Three things. First, that the *complete* field would keep the verdict
where the *row-end-only* idealisation loses it — the boundary layer is a field and `C-0104`'s three
distributions are all maps on 14 sites, so the question of what the other 42 crossovers carry was
never posed and it turns out to be worth the whole verdict. Second, that the sign would compose to
the **adverse** map: two independent alternations, each of which alone would have been favourable,
cancel. Third, that the literature would contain the exact measurement protocol, applied to the
exact structure, with the exact sites excluded in one clause of one sentence. And fourth, smaller:
that moving the coupling from a Woodbury surrogate onto the lattice as explicit supports would move
the standing uniform-load falsifier with it — the falsifier is about a **free** plate, and on a
34-anchor lattice a uniform load genuinely dishes (0.170). It fired, and it was right to.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** The boundary layer is a **new derivation** in this claim,
  one-dimensional, linear, and smeared; its inputs (`C`, `k_θ`, `Δω`, `L`) are all carried elsewhere
  in this repository and are re-derived here.
- **The model assumes the sheet stays flat while the duplex relieves.** It charges the whole
  azimuthal error to the crossover hinge wherever the duplex does not untwist. A sheet free to
  corrugate relieves more, so **`u(L/2)` is an upper bound within the model's own family** — and the
  grillage solves that follow *do* let the sheet fold, which is why the graded field reads 0.0923
  and not the row-end-only 0.1023.
- **`p` is a smearing convention** and is bracketed at 5.44 and 10.20 nm; it is worth 3.9° at
  `α = 1`, less than the `α` bracket.
- **The groove-asymmetry term is NOT included.** Rothemund names **two** causes — *"the non-integral
  number of bases in a single turn, **and** the major-minor groove angle"* — and only the first is
  modelled. The second is balanced in the bulk by the glide symmetry and, on Rothemund's own
  statement, **not** balanced at an edge, so it is an additive term of unknown sign. Yoo &
  Aksimentiev's all-atom junction dihedral mean of **−4°** and oxDNA's `θ = 4.5°` are the scale.
- **`C-0086`'s 112 bp row is NOT twist-corrected, and `C-0086` says so itself**: *"the crossover
  phase the scaffold turns land on, and **the twist correction Rothemund's program applies**, are
  not [checked]. A routing that passes here can still fail a caDNAno-level design."* If a corrected
  row length is adopted, `Δω` falls and the whole boundary layer falls with it. That is `T-183` and
  it is the cheapest way to remove this exposure.
- **Phase 8 and `C-0090`'s published key only**, as `C-0099` and `C-0104`. No re-optimisation is run
  here; `C-0104` has already shown the 163 296-placement family recovers flatness at ±17.14°, and
  whether it recovers it at 22.6° is **not** demonstrated.
- **The dishing states are read at one operating state** — `C-0022`'s solved 2 mM / 10 nm / 0.192 V
  collar — which is the eighth instance in this project of *quote it with the state it is read at*.
- **Snodin et al.'s 8–9° / ±16° is read off a figure**, at 170 dpi against its own printed axis, and
  is flagged as such. It is used as a **cross-check on the order of magnitude**, never as the value.
- **Nothing published moves.** No existing source file was edited and no existing result file was
  re-emitted by this claim.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the glide symmetry, the *"on average, flat"* balance, its failure at edges, the by-hand edge tuning and the *"one or two scaffold bases"* remedy | verbatim | **CITED, READ DIRECTLY** — Rothemund 2006 Suppl. Notes S1 §4 / S2, `gpd/data/T-151-sources/` |
| the corrugation definition, its magnitude, the 31/32 bp twist correction and the exclusion of the outermost junctions | ≈ 8–9° / ±16° | **CITED, READ DIRECTLY** — Snodin et al. 2019, `gpd/data/T-151-sources/PMC6379721-*`; the magnitude **digitised from Figure 5** |
| the all-atom junction dihedral mean | −4° | **CITED, READ DIRECTLY** — Yoo & Aksimentiev 2013 |
| the 10.7-against-10.5 accumulation seen by cryo-EM | — | **CITED, READ DIRECTLY** — Ni et al. 2022 |
| `k_θ = 2αB/(100a)` at `α = 1`, and the 0.6–1.2 bracket | 13.5294118 pN·nm/rad | **CITED, FITTED** — Chen et al., *JACS* **136**:6995, via `C-0009`; **re-derived and asserted** |
| `GJ` = 460 pN·nm² and torsional persistence 100 nm | — | **CITED** — CanDo (2012); Kriegel et al. 2017 / Kauert et al. 2011 |
| the phosphate radius | 1.00 nm | **CITED** — `C-0029`, via `BForm.PHOSPHATE_RADIUS` |
| the unzip / shear / nicked allowables | 10–15 / 48–65 / 65 pN | **CITED** — `CLAUDE.md`, via the problem definition and Strunz |
| `C-0090`'s reading and key, `C-0104`'s threshold and slope, `C-0022`'s collar | — | **READ FROM THEIR RESULT FILES and REPRODUCED** |
| interhelical distance, rise, bp/turn | 2.69 nm, 0.34 nm, 32/3 and 10.5 | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |

Everything else — the four ceiling families, the boundary-value problem and its closed form, the
12-cell bracket, the sign composition, all eleven solved dishing states, the graded field, the five
convergence records and the seven reproductions — is **derived here in code**.

## Still open — named, not answered

1. **What the 42 interior crossovers carry.** The whole difference between *"flat at 0.0923"* and
   *"not flat at 0.1023"* is the interior field, and `C-0104`'s three distributions are all maps on
   14 sites. **`T-184`.**
2. **The groove-asymmetry term.** Rothemund's second cause, additive, of unknown sign, and the one
   an oxDNA run would return *together* with the register term rather than separately.
3. **Whether `C-0086`'s row length can be twist-corrected.** Snodin et al.'s design and Rothemund's
   own edge tuning both do it, and it would remove the driver rather than absorb it. **`T-183`.**
4. **Re-optimising `C-0090`'s placement at 22.6°.** `C-0104` shows recovery at ±17.14°; recovery at
   the derived value is not demonstrated. **`T-185`.**
5. **Baker et al. (2018)** is the one direct experimental measurement of a single-layer origami's
   global twist and it is behind an ACS `403`. It would calibrate `Δω` after relief, from experiment.
6. **Phase 24 is not read**, as in `C-0099` and `C-0104`.

## Challenges

**Raises [`CH-0122`](../challenges/CH-0122-the-prestrain-threshold-is-a-secant-not-a-triangle-inequality.md)**
against `C-0104`: its `dishingPerRadian` is the **secant** `|D(1 rad) − D(0)|` and not the peak of
the unit response, so the ceiling it names as a triangle inequality is not one. Reproduced here at
`5.1e−08` on its own definition; the peak of the prestrain-only field is **0.188285084** and the
genuine triangle ceiling is **11.5188°**, 1.34× tighter. `C-0104`'s number survives as an upper
bound **by convexity** — peak dishing is a maximum of affine functions of `θ₀`, so a chord from zero
lies above it — and the correction moves the threshold **away** from every candidate value, so no
verdict moves.

**`C-0099`'s recommendation 3 is REVERSED** — *"an oxDNA or all-atom edge crossover: recommended
against"* — and its *Still open* item 3, *"what a row-end crossover's `k_θ` actually is … on this
evidence, not worth an oxDNA campaign"*, is now worth one, for a different quantity.

**`C-0104`'s *Still open* item 1 is CLOSED as far as a derivation can close it**: the physical rung
is neither the 16 bp nor the 32 bp one, it is the **accumulated** value at a free row end, and it is
above both.

**None stands against this claim.** The five ways it would fail:

1. **A twist-corrected row length.** Then `Δω → 0` and the boundary layer with it. `T-183`.
2. **A sheet free enough to corrugate away most of the azimuthal error**, which would put the
   crossover prestrain below the model's upper bound. The grillage solves bound how much of that is
   available at the Gen-1 stiffnesses; a full 3-D treatment does not exist here.
3. **A groove term of the opposite sign** large enough to cancel the register term.
4. **An interior field that does not cancel the way the graded solve says**, i.e. `T-184`.
5. **An oxDNA measurement at the row end.** Which is the point.
