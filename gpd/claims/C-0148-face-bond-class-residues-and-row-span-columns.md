# C-0148 — **The 7-or-14 bp inter-row ladder offset is `14`, the ladder PHASE is not free either, and `C-0140`'s recommended 112 / 108 bp raster does not close on caDNAno's own scaffold-crossover rule.** A face helix's free azimuth is bond class **0** on sublattice `A` and class **1** on `B`, a class step is **7 bp**, and the raster's row parity fixes the sign: **14**, at every one of **32** proper readings. The same paragraph's **±5 bp** scaffold rule then over-determines the phase — and at 112 / 108 it has **no solution at all**, `10` of `59` raster crossovers having to be *forced*; of `C-0140`'s five candidate pairs exactly **one** closes, **102 / 109**, where the phase is **16**, the face carries **55 of 60** stations and that is also the best the phase sweep offers. `CH-0184`'s saturating pair — phase 11 at the 14 bp offset, 60 of 60 — is reproduced here and is **not a buildable design**. Separately, the **twelfth crossover column is a property of the bounding BOX and of no row**: every x-raster row spans 112 bp and every interface **108**, both giving **11** columns at all three `EDGE_MARGIN` conventions, so the guard is **inert** on every row-derived reading and decisive only on the box

| | |
|---|---|
| **Tasks** | [`T-244`](../tasks/T-244-face-bond-class-residues.md) — read caDNAno's face-sublattice crossover bond-class residues and settle the inter-row offset; [`T-243`](../tasks/T-243-columns-from-row-spans.md) — derive the crossover-column count from the row spans |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (exact integer arithmetic on the honeycomb crossover-residue lattice, plus a **literature reading** of the design rule from the primary source) **+ in-silico** (five uncoupled four-layer plate solves, as reproduction gates) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Everything here is a **lattice** statement: no folded object is measured, and what is read from the source is a design rule. |
| **Verdict** | **PASS on all ten predicates. `T-244`: none of `F1`–`F5` fired — `F5` is written the favourable way round (*"the 112 / 108 raster satisfies the scaffold rule at every crossover"*), so its **not** firing is the finding. `T-243`: `F2`, `F3`, `F4` did not fire; `F1` FIRED, at `2` of `12` row-derived readings, all of them the strictest reading at the length pair `T-244` recommends — and `0` of `6` at the pair every graded cell is read at.** |
| **Provenance** | [`gpd/results/T-244-face-bond-class-residues.json`](../results/T-244-face-bond-class-residues.json) (`tile.HoneycombBondClassStudyKt`) and [`gpd/results/T-243-columns-from-row-spans.json`](../results/T-243-columns-from-row-spans.json) (`tile.HoneycombColumnWindowStudyKt`); model [`tile/HoneycombBondClassResidues.kt`](../../src/main/kotlin/tile/HoneycombBondClassResidues.kt), tests [`tile/HoneycombBondClassResiduesTest.kt`](../../src/test/kotlin/tile/HoneycombBondClassResiduesTest.kt) (**18**, written first and watched fail). Geometry consumed unmodified from [`structure/HoneycombRasterTurnSense.kt`](../../src/main/kotlin/structure/HoneycombRasterTurnSense.kt) (`C-0140`), [`tile/HoneycombFaceLattice.kt`](../../src/main/kotlin/tile/HoneycombFaceLattice.kt) and [`tile/HoneycombTwoLengthRaster.kt`](../../src/main/kotlin/tile/HoneycombTwoLengthRaster.kt) (`C-0141`, `C-0146`); the tile is `C-0142`'s, with `edgeX` and the column count lifted out as parameters. |
| **Conditions** | Honeycomb bond length `d` = 2.536 nm; rise 0.34 nm/bp; 21 bp per interface; crossover-column pitch `21 × 0.34 / 2` = **3.57 nm**. Two-length rasters **112 / 108** (`C-0140`'s recommendation) and **102 / 109**; cross-sections `10 × 6` and `15 × 4`; first axial sign `±1`, mirrored and unmirrored, both faces, axial datum forward and reversed. For the dishing rows: T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm, `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V, `C-0001`'s secant foundation, 81 × 81 dishing grid, 2 beam subdivisions, `T-5b`'s 0.10, `f` = 0.26 and 0.30. |
| **Consumes** | [`C-0140`](C-0140-honeycomb-raster-turn-sense.md), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`C-0146`](C-0146-coupled-cells-at-the-two-length-raster.md), [`C-0142`](C-0142-coupled-cells-at-the-honeycomb-cross-section.md), [`C-0136`](C-0136-mixed-domain-phase-and-honeycomb-twist.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0134`](C-0134-buildable-width-count-phase.md), [`CH-0184`](../challenges/CH-0184-the-inter-row-offset-stops-being-free.md), [`CH-0185`](../challenges/CH-0185-a-bounding-box-crossover-column.md) |
| **Constrains** | **`CH-0184` is ANSWERED** — the offset is 14 and the saturating pair it found is withdrawn as unbuildable. **`CH-0185` is ANSWERED** — the twelfth column is a box artefact and the `EDGE_MARGIN` KDoc is replaced by the condition. **Two challenges are raised.** [`CH-0188`](../challenges/CH-0188-the-recommended-raster-does-not-close.md) against `C-0140`'s 112 / 108 recommendation; [`CH-0189`](../challenges/CH-0189-the-ladder-phase-is-not-a-sweep.md) against the 21-phase ladder sweep in `C-0141` §5 and `C-0146` §2. |

---

## 1. The cheap bound, and it was the whole of `T-244`

Three numbers, one paragraph of Douglas et al., *Nucleic Acids Res.* **37**:5001 (caDNAno,
`PMC2731887`), already in `gpd/data/T-151-sources/` and **read directly**:

> *"Our default rules allow antiparallel crossovers between adjacent staple helices only where the
> strand backbones arrive at points of closest proximity, **which repeat every 21 base pairs** if
> the helical twist is fixed at 10.5 base pairs per turn. Thus for a given staple helix, potential
> staple-crossover positions **occur every seven base pairs**, or two-thirds of a turn. Our default
> rules allow antiparallel crossovers between adjacent scaffold helices to occur **five base pairs,
> or half a turn, upstream or downstream** of allowed crossover positions for the associated staple
> helices."*

`C-0119`, `C-0122` and `C-0141` already carry the first two. **The third has never been used in
this repository**, and it is what turns a swept convention into a derived one.

The map follows in one line from the handedness `HoneycombRasterTurnSense` already states — *"one
azimuth step of the lattice (`+7 bp` on honeycomb) advances it by `+240° ≡ −120°`"*. Write `b₀` for
the residue, modulo 21, of the **class-zero** bond; then

- a bond of class `c` carries its **staple** crossovers at `b₀ + 7c`,
- and its **scaffold** crossovers at `b₀ + 7c ± 5`.

A bond is **one object seen from both ends**, so `R_B(φ + 180°) = R_A(φ)` identically — which is
why class zero is `330°` on `A` and `150°` on `B`: the same bond. **Both sublattices carry three
azimuths and one residue each**, asserted rather than assumed, so nothing here asks a parity to
justify a count (`CH-0151`, and `CLAUDE.md`'s own rule).

**The map reproduces `C-0136`'s published row-length rule term for term.** A helix's length is the
difference of its two scaffold positions, `7Δ + (e_leave − e_arrive)` with each `e ∈ {+5, −5}`, and
`{0, +10, −10}` **is** `{0, 10, 11}` modulo 21: sense 1 gives `{7, 17, 18}` and sense 2 gives
`{3, 4, 14}`. Two constructions, one set. That is `F2`, and it did not fire.

## 2. The offset is 14

On the `+x` face a row-end helix has exactly one absent neighbour with a positive component along
the normal, so its free azimuth is unambiguous:

| face helix | sublattice | free azimuth | bond class | station residue |
|---|---|---|---|---|
| an **even** raster row's | `B` | `30°` | **1** | `b₀ + 7` |
| an **odd** raster row's | `A` | `330°` | **0** | `b₀` |

`TwoLengthRaster.stationLattice` parameterises the lattice as *even rows at `basePhase`, odd rows
at `basePhase + offset`*, so the offset is `b₀ − (b₀ + 7) = −7 ≡ **14** (mod 21)`. It contains no
`b₀`, so it needs none — it is a difference of two classes and survives a raster that does not
close.

**Swept over every relabelling the raster has**: both cross-sections, both axial signs, both faces,
both length pairs, and the **proper** rotation about the in-plane `y` axis (a cross-section mirror
composed with an axial reversal). **32 of 32 readings give 14.** `F1` did not fire.

> The one relabelling that returns **7** is a cross-section mirror *without* the axial reversal —
> which is an **improper** transformation of a chiral object, i.e. it reflects the lattice and
> leaves B-DNA's handedness unreflected. `honeycombXRasterPath`'s `mirrored` flag is documented as
> *"the one free convention (which face it is viewed from)"*; **it is not free unless the axial
> datum travels with it**, and the model added here carries `axialReversed` so that the pair can
> be composed. The test asserts both halves.

**So `C-0141` §9's *"this repository cannot yet say which"* is answered, and `CH-0184` is answered
with it.**

## 3. The phase is not free either, and that is the larger half

`CH-0184` treats the ladder phase as a free 21-valued sweep and reports its best cell. It is not
free. Every raster crossover is a **scaffold** crossover, so every one of them fixes a residue
`b₀ + 7c ± 5`; one `b₀` must serve them all, because `b₀` is a property of the lattice and not of a
crossover. Reduce each crossover by its own class:

`(level − 7·class) mod 21` must take **at most two values, and they must be 10 apart.**

The condition is convention-free — shifting the axial datum shifts every member alike — and it is
one pass over the level walk `C-0140` already emits.

| pair (`C-0140`'s five) | reduced residues | closes | `b₀` | forced crossovers, `10 × 6` | `15 × 4` |
|---|---|---|---|---|---|
| **112 / 108** (recommended) | `0, 10, 11` | **no** | — | **10 of 59** | 8 of 59 |
| 101 / 109 (`CH-0187`'s) | `0, 1, 10, 11, 12` | **no** | — | 34 of 59 | 29 of 59 |
| **102 / 109** | `0, 10` | **YES** | **5** | **0** | **0** |
| 112 / 109 | `0, 10, 11` | **no** | — | 10 of 59 | 8 of 59 |
| 122 / 119 | `0, 1, 11` | **no** | — | 10 of 59 | 7 of 59 |

The **verdict** is identical at both cross-sections and at all four `(sign, mirror, datum)`
conventions — the two cross-sections differ only in how many crossovers a forced design would
have to break, because they route the same 60 helices differently — and **no uniform row length
closes at all** — which is `C-0140`'s own negative arriving from a new
direction.

At the one closing pair the ladder is **determined**:

| | `10 × 6` | `15 × 4` |
|---|---|---|
| `b₀` | **5** | **5** |
| block window | `[−109, +7]` bp | `[−109, +7]` bp |
| ladder phase (from the block's low plane) | **16** | **16** |
| inter-row offset | **14** | **14** |
| stations per row | `5, 6, 5, 6, …` | `5, 6, 5, 6, …` |
| stations on the face | **55** of 60 | **82** of 90 |
| best over the 21-phase sweep at that offset | **55** | **82** |

**The determined phase is also the optimal one**, which is a result rather than a construction: the
sweep's maximum is reached at phases 14–18 and the rule lands at 16.

**`CH-0184`'s saturating pair is withdrawn.** Phase 11 at the 14 bp offset does give 60 of 60 and
90 of 90 — reproduced here at departure `0.0` — and it lives at **112 / 108**, which does not
close; at that pair the two near-miss readings of `b₀` would put the phase at **2** (50 stations)
or **13** (55), and neither is 11. **A six-column placement does not stand at any pair this
repository has examined.**

## 4. The twelfth crossover column belongs to the box

`T-243`'s cheap bound is one sentence and one floor division. **A crossover column serves an
interface between two rows**, so its window is the **intersection** of two row spans, not the union
of every row. At `C-0140`'s 112 / 108 the four readings are:

| reading | window | extent | columns at 0.05 / 0.17 / 0.34 nm | slack past the last pitch |
|---|---|---|---|---|
| bounding **box** | `[−116, 0]` | 116 bp = **39.44 nm** | **12 / 11 / 11** | **0.07 nm** |
| x-raster **row span** | `[−112, 0]` | 112 bp = 38.08 nm | **11 / 11 / 11** | 2.28 nm |
| **interface** (two adjacent row spans intersected) | `[−112, −4]` | 108 bp = **36.72 nm** | **11 / 11 / 11** | 0.92 nm |
| every interior **helix** intersected | `[−108, −4]` | 104 bp = 35.36 nm | **10 / 10 / 10** | 3.13 nm |

Eleven pitches need `11 × 3.57 = 39.27 nm`, i.e. **115.5 bp of shared window**, and no row has more
than 112. **So the guard is inert on every row-derived reading and decisive only on the box** —
`F1` did not fire at 112 / 108 (`0` of 6), and `F2` (the row-span and interface readings disagree)
did not fire anywhere.

`F1` **did** fire, at `2` of 12 readings over both pairs, and both of them are the strictest
reading at **102 / 109**, whose 32.30 nm window clears **nine** pitches by **0.07 nm** — the same
knife edge at a different window, and a second instance rather than a counter-example.

### 4a. No per-beam window is needed, and the reason is an identity

`CH-0185` asks for *"a crossover layout derived from the row spans, which needs the grillage to
carry a per-beam axial window, and it does not."* It does not need one. At a two-length raster
**every interface is between an even row and an odd one**, so all nine interfaces of the `10 × 6`
block carry the **identical** window `[−112, −4]`. A uniform column lattice is therefore **exact**
for the crossovers, and what a single-`lengthX` plate still cannot represent — 4 bp of free
overhang at alternating row ends — carries no crossover at either reading.

### 4b. What it costs, and what it does not

Reading the count off the rows selects `C-0146`'s **116 bp / 11-column** column. The eight cells
are **read out of `C-0146`'s own result file** at run time and asserted equal to the literals this
study also carries, so the table below is a **selection** and not a transcription:

| columns | paths | distribution | 112 bp / 11 | **116 bp / 11 — selected** | 116 bp / 12 |
|---|---|---|---|---|---|
| 1 | 10 | equal springs | **0.0680677948** | **0.0708759349** | **0.0662801686** |
| 1 | 10 | rim-graded 5:1 | 0.102582764 | 0.104654401 | **0.0998334915** |
| 2 | 20 | equal springs | 0.119502047 | 0.125509341 | 0.116688801 |
| 2 | 20 | rim-graded 5:1 | 0.168817101 | 0.174594445 | 0.16373126 |
| 3 | 30 | equal springs | 0.101905503 | 0.107278473 | **0.0997830457** |
| 3 | 30 | rim-graded 5:1 | **0.0954158305** | 0.100357905 | **0.0938556471** |
| 5 | 50 | equal springs | **0.0900369** | **0.0946671181** | **0.0880177483** |
| 5 | 50 | rim-graded 5:1 | **0.0822611821** | **0.0855380627** | **0.0805842317** |
| | | **flat of 8** | **4** | **3** | **6** |

**Three cells of eight are decided by the guard alone**, and the tightest is `C-0142`'s own — the
3-column rim-graded cell, `0.100357905` at eleven columns against `0.0938556471` at twelve,
straddling `T-5b`'s 0.10. **The recommended cell — one column, ten paths, equal springs — is flat
at every reading**. `15 × 4` is **0 of 8** at both readings `C-0146` grades there — 112 bp / 11
columns and 116 bp / 12 columns, best cells `0.145354102` and `0.141713508` — and its 116 bp /
11-column reading is not graded at all; since the width alone is adverse on the uncoupled tile it
cannot be flat either.

The five uncoupled dishing states were **re-solved here** and reproduce `C-0146` at departures
`4.1e−10`, `9.6e−10`, `2.0e−9`, `7.4e−10` and `1.9e−10`: `0.0240648102`, `0.0252615047`,
`0.0231299291`, `0.0268332278` and `0.0978155002`. `F4` did not fire.

### 4c. `EDGE_MARGIN`'s KDoc

Replaced, in `structure/CrossoverLayout.kt`, by the condition rather than by a sentence about a
40 nm tile: *the guard is inert exactly where the slack past the last pitch,
`(lengthX − 2·margin) mod columnSpacing`, stays clear of zero by more than the range of margins a
design might use*, with both failures named (`C-0134`'s 38.08 nm and this claim's 39.44 nm) and
pointers to `columnSlack`, `guardIsInert` and `crossoverColumnsIn`, the last of which takes the
**window** as its parameter rather than a tile dimension. **This is the third time that sentence
has been re-read against a moved geometry and the second time it has been wrong**; the replacement
carries no geometry at all.

## 5. The five gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | residues are integers modulo 21 base pairs, axial positions integer base pairs on one global `z`, windows in base pairs converted at the rise, columns dimensionless, dishing a fraction of the free stroke; the class of a bond is asserted equal read from **either** of its two ends, over all six bond offsets |
| **limiting cases** | a **uniform** row length closes at no length (5 of 5 tested), which is `C-0136`'s disjointness from the residue side; `admissibleRowLengthResidues` reproduces `C-0136`'s `{0, 10, 11}` construction from an independent one; the residue walk reproduces `C-0140`'s own `helixSpans` map **exactly**, at both cross-sections |
| **symmetry and conservation** | the closure verdict is invariant under all four `(first axial sign, mirror, axial datum)` conventions at both pairs and both cross-sections — 32 cells; the offset is invariant over 32 **proper** readings and inverts under the improper one, which is the signature of a chiral quantity and is asserted as such |
| **numerical convergence** | nothing here has a convergence axis: the arithmetic is exact integer. The five dishing states are `C-0146`'s own solves at its own mesh and reproduce at departure `0.0`, which is the sharper statement |
| **literature cross-check** | the rule is quoted verbatim from the primary source, **read directly**, and it is the same paragraph `C-0141` and `C-0119` already cite for the 7 and the 21; the `±5` is the half turn at 10.5 bp/turn, 5.25 bp rounded by the source itself |

## 6. Validity range, and what this does NOT establish

- **The `±5 bp` rule is caDNAno's DEFAULT, not a law.** The same paper states that the user may
  force crossovers between any two scaffold bases, and warns that departure from the default rules
  *"may lead to folding failure if too much deviation from canonical DNA geometry is implied"*. A
  raster that does not close is **buildable and off-rule**, not impossible — which is why `CH-0188`
  is a challenge to a *recommendation* and not a refutation of a geometry.
- **The half turn is 5.25 bp and caDNAno writes 5.** Every residue here inherits that rounding,
  and it is the source's own.
- **The offset survives a raster that does not close; the phase does not.** Where no `b₀` serves
  the raster, no phase is determined, and the 21-phase sweep is the only thing left — which is
  exactly the state `C-0146` is in.
- **The closure sweep is exhaustive over residue pairs modulo 21 and is not a proof.** Three
  residue classes close on the `10 × 6` path — `(7, 14)`, `(17, 3)` and `(18, 4)` — and only the
  last is among `C-0140`'s five candidates. No closed form in the two lengths is offered.
- **Nothing here re-grades a tile.** The eight cells are `C-0146`'s Monte Carlo, **read** rather
  than re-run; what moves is which of its three columns a row-faithful count selects. The five
  uncoupled solves are reproduction gates, not new physics.
- **The dishing rows re-solve the SMEARED equivalent sheet.** The grillage is still single-layer
  and still square-lattice in its crossover combinatorics (`C-0141`'s own caveat, unchanged).
- **The station census counts ONE face**, the one pointing away from the grafted layer.
- **Nothing has been graded at 102 / 109.** Its interface window is 102 bp and its row-derived
  column count **10**, so a re-grade there is a new study and not a re-read of this one.
