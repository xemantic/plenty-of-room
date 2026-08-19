# C-0136 — **A SEAMLESS RASTER ROW HAS NO PHASE VARIABLE AT ALL, AND THE HONEYCOMB HAS NO TWIST TO CORRECT.** `C-0090`'s *"ten eight-column phases collapse to two"* is a collapse from a **translation** to a **parity**: the admissible rigid translation group of a seamless row is `{0}` at 112 bp and at `C-0133`'s twist-corrected 110 bp alike, and phases 8 and 24 give **identical column positions** with **inverted parities**. What a mixed-domain row loses is nothing; what it **gains** is an **arrangement** axis of **21** members that the uniform row does not have, every one carrying **eight columns as an identity**. And the honeycomb's design twist is `720/21 = 34.2857 °/bp`, which **is** B-DNA's `360/10.5` — the same number — so its `Δω` is **exactly zero** and every register number in `C-0104`, `C-0107` and `C-0133` is a **square-lattice** number here. One fact unifies both: `10.5 = 21/2`, so an **odd** multiple of a half turn is a quarter base pair off an integer and a **quadruple** one is exact; the square lattice's raster demands an odd count and the honeycomb's azimuth period is **four**

| | |
|---|---|
| **Task** | [`T-216`](../tasks/T-216-mixed-domain-phase-lattice.md) and [`T-217`](../tasks/T-217-honeycomb-twist-correction.md), both raised by [`C-0133`](C-0133-twist-corrected-raster-row.md) *Still open* items 1 and 6 |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on) |
| **Verification type** | **logical** (exact integer and residue arithmetic, asserted over whole periods and whole families rather than at a point) **+ in-silico** (`C-0009`'s grillage under `C-0022`'s carried collar on every lattice the census produces, exhaustive centro-symmetric enumerations) **+ literature** (the two caDNAno design-rule sentences grepped directly out of `gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`, already in the repository — **zero fetches**) |
| **Verdict** | **PASS on all NINE predicates — `T-216`'s four and `T-217`'s five — and one declared falsifier fired, as declared.** `T-216`: a translational phase variable **does not exist** on a seamless row, uniform or mixed — proved by pinning, enumerated over every base-pair translation — and the census of what replaces it is delivered in full. `T-217`: the honeycomb needs **no** twist correction, by one division, and the connectivity condition that *does* bind it is **derived** rather than transferred, reproducing `C-0086`'s *"odd multiples of 16 bp"* exactly when run on the square sheet's own azimuths. Raises [`CH-0164`](../challenges/CH-0164-a-seamless-row-has-a-parity-not-a-phase.md) and [`CH-0165`](../challenges/CH-0165-an-integral-scaffold-lattice-is-necessary-not-sufficient.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The two theorems and the whole census are **arithmetic**; the flatness numbers are `C-0009`'s lattice on `C-0022`'s carried collar, and the motif they place (`C-0055`'s free lever on one upward crossover) remains **undemonstrated**. The honeycomb result rests on **one constant**, `10.5 bp/turn`, which is a convention shared between the lattice and the material and is not measured here |
| **Provenance** | `gpd/results/T-216-mixed-domain-phase-lattice.json` (`structure.MixedDomainPhaseLatticeStudyKt`, **new**) and `gpd/results/T-217-honeycomb-twist-correction.json` (`structure.HoneycombTwistCorrectionStudyKt`, **new**); model in `src/main/kotlin/structure/LatticePhaseCensus.kt` (**new file** — `TwistCorrectedRaster.kt`, `TwistCorrectedRasterStudy.kt`, `BuildableRasterWidth.kt`, `OrigamiGrillage.kt` and `Gen1Tile.kt` were **read, not edited**); **20 gate-named tests in `src/test/kotlin/structure/LatticePhaseCensusTest.kt`**, written **before** the model; **4 translation records, 64 phase records, 21 arrangement records, 12 census records, 16 solved enumerations, 2 lattice records, 6 half-turn records, 19 width records, 6 sensitivity records, 9 convergence records, 10 upstream reproductions, 9 predicates, 14 falsifiers, 17 findings**; **both result files re-run and BYTE-IDENTICAL across two independent JVM runs**; the two caDNAno design-rule sentences grepped **directly** out of `gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`, which was already in the repository — **zero fetches**; `tools/verify.sh` **BUILD SUCCESSFUL in 22 m 02 s** on its own isolated tree — the whole suite, **no `--drop-file` needed** and no failure anywhere, with two concurrent agents' new sources in the tree; `tools/check-markdown-tables.py`, `tools/check-challenge-index.py`, `tools/check-result-file-hygiene.py` (`--conversions` clean over 127 files, `--departures` exit 0 — its 37 remaining fields in `T-79`, `T-96`, `T-97` and `T-118` are a concurrent agent's in-progress `T-214` scope widening and none of them is in either file emitted here), `tools/check-kotlin-format-strings.py` and `tools/result-reader-census.py --check` all clean, and this claim's, both challenges' and both task files' links checked by an explicit walk |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes** at the SAXS 2.69 nm, **0.34 nm** rise; B-DNA at **10.5 bp/turn**, `34.2857 °/bp`; `C-0133`'s **110 bp = 37.40 nm** twist-corrected row, seven domains from `{15, 16}`; `C-0022`'s solved collar at 2 mM, a 10 nm gap and **0.192 V**, **carried unchanged**; `C-0017`'s **33.3333 pN/nm** mandate shared equally over **34** roots; honeycomb rules from Douglas et al., *NAR* **37**:5001 (PMC2731887) |
| **Consumes** | [`C-0133`](C-0133-twist-corrected-raster-row.md) (`RasterRow`, `columnRegisterField`, `twistCorrectedColumnLayout`, `twistCorrectedUpwardSites`, `centroSymmetricPlacementsOn`, and its two 110 bp enumerations **read from its result file and reproduced**), [`C-0119`](C-0119-honeycomb-raster-width.md) (the honeycomb design rules, **read from its result file**), [`C-0090`](C-0090-buildable-raster-width.md) (`rasterColumnLayout`, and its two phase optima **read from its result file**), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the odd-half-turn rule — **re-derived from the azimuths, not transcribed**), [`C-0063`](C-0063-upward-root-placement.md) (`rowRootOptions`, `armDirections`, the centro-symmetric family), [`C-0055`](C-0055-unused-junction-site.md) (the 8 bp plane lattice, the `EAST` azimuth, the 34 arms), [`C-0022`](C-0022-tile-edge-load-profile.md) (**read from its result file**), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the phase and parity conventions), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage), `Gen1Tile` |
| **Raises** | [`CH-0164`](../challenges/CH-0164-a-seamless-row-has-a-parity-not-a-phase.md) against `C-0133`'s *Still open* item 1 and `C-0090`'s phase framing, [`CH-0165`](../challenges/CH-0165-an-integral-scaffold-lattice-is-necessary-not-sufficient.md) against `C-0119`'s *"integral, so drawable"* |

---

## The claim, in one line

**Seamlessness spends the phase variable before a twist correction can, so a mixed-domain row
inherits a parity binary and gains an arrangement axis; and the honeycomb is laid out at B-DNA's own
twist, so the correction this programme spent an iteration constructing has nothing to do there.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, angles **degrees**; rise **0.34 nm/bp**; `k_BT = 4.141947 pN·nm` at 300 K.
- **Natural twist** `ω_n = 360/10.5 = 34.2857 °/bp` — B-DNA's, `C-0015`'s, `C-0107`'s and `C-0133`'s.
- A **domain** is one inter-column stretch of a row; a **column** is a crossover plane spanning the
  tile across the helices; a **row** is one duplex whose two ends carry a scaffold crossover
  (`C-0095`) and **are** the tile edges along the helix axis.
- A **phase** is `C-0015`'s: a rigid **translation** of the plane lattice relative to the tile,
  quantised at the rise, of period 32 bp.
  A **parity** is `C-0015`'s column/interface assignment — one column pitch of shift, which leaves
  every column position unchanged.
- An **arrangement** is one ordering of a mixed row's domain lengths. The design family is
  Rothemund's own remedy — *"helical domain lengths … by single bases"* — i.e. `{15, 16}` at a
  nominal 16; the wider shell is a stated sensitivity and not adopted.
- **Dishing** is `C-0063`'s peak-dishing-over-free-stroke; **flat** means `≤ 0.10` (`T-5b`).
- On the honeycomb, a helix's three neighbour directions are indexed `0, 1, 2` at 120°, and the
  **turn sense** of a raster helix is `Δ = (b − a) mod 3` for the neighbour it arrives from and the
  one it leaves to.

---

## `T-216`, Deliverable 1 — the cheap bound, and it is the whole existence answer

A seamless raster row's two ends **are** the tile edges and both carry a scaffold crossover, so both
end columns are pinned. A rigid translation by `t` requires `0` and `N` to remain columns, i.e. `−t`
and `N − t` to have *been* columns; `0` is the smallest column and `N` the largest, so `t = 0`.

> **The admissible rigid translation group of a seamless row is `{0}` — at `C-0133`'s twist-corrected
> 110 bp, at `C-0086`'s uniform 112 bp, and at a single 16 bp domain.** Enumerated over **every**
> base-pair translation of the period (219, 223 and 31 respectively) rather than argued.
> **`F1` did not fire.**

**So `C-0015`'s phase is a freedom of a tile whose row ends are *not* crossovers.** Reproduced on the
same machinery: at the nominal 40.00 nm **10 of 32** phases carry eight columns — `C-0015`'s own ten
— and at the buildable 38.08 nm **2 of 32** put both row ends on a column, `C-0090`'s own 8 and 24.
Seamlessness spends the other thirty.

**And the two survivors are one lattice.** At 38.08 nm phases 8 and 24 give

- **identical column positions**, to `1e−12` nm at all eight columns, and
- **inverted parities**, at all eight. **`F2` did not fire.**

That is [`CH-0164`](../challenges/CH-0164-a-seamless-row-has-a-parity-not-a-phase.md): the collapse
from ten to two is a collapse from a **translation** to a **parity**, and it does **not** rest on
`38.08 = 7 × 5.44`. That identity is what lets a *uniform* pitch be seamless at all; it is not what
fixes the phase.

**The binary is not cosmetic.** One column pitch is two 8 bp planes, so shifting by it exchanges every
duplex's `EAST` and `WEST` azimuths — `CLAUDE.md`'s *"reflecting an out-of-plane array moves it to the
other face of the sheet"*. The two parities **partition** the plane lattice, their station counts
differ (**52** against **53** on the 110 bp row), and `C-0090` measures the cost without naming it:
**0.0621469105** of the stroke at phase 8 against **0.070693794** at phase 24, a factor of
**1.13752709**.

---

## `T-216`, Deliverable 2 — the census

| what | count | note |
|---|---|---|
| arrangements of the 110 bp row over domains in `{15, 16}` | **21** | `C(7,2)` placements of the two 15 bp domains |
| …carrying **eight** columns | **21** | an **identity**: `columns = domains + 1`, where the uniform lattice's count is a function of the phase |
| …with a centro-symmetric **column set** | **3** | exactly the palindromic domain sequences |
| …distinct up to reflection | **12** | 3 self-mirror plus 9 mirror pairs |
| column/interface parities | **2** | `C-0015`'s binary — all that survives of the phase |
| **distinct column lattices of the 110 bp row** | **42** | against the 112 bp row's **2** |
| arrangements with a centro-symmetric **station lattice**, either offset convention | **6** | **not the same condition** as a centro-symmetric column set |
| arrangement/convention pairs with a centro-symmetric station lattice | **7** | every one of them enumerated here |
| …needing **no** mirrored offset, so no 30° station | **3** | `15+16+16+16+16+15+16`, `16+15+16+16+15+16+16`, `16+16+15+15+16+16+16` — **none** of which has centro-symmetric columns |
| arrangements over the wider shell, domains in `{14…17}` | **1 918** | the stated sensitivity; **not adopted** |

**`F3` and `F4` did not fire.**

Two things in that table are the result.

1. **The column count stops being a variable.** On the uniform lattice a phase decides whether the
   tile carries seven columns or eight (`C-0015`: 8 at ten phases, 7 at twenty-two). On a seamless
   mixed row it is `domains + 1` identically, at every arrangement.
2. **A centro-symmetric column set and a centro-symmetric station lattice are different conditions,
   and `C-0063`'s exhaustive family needs the second.** Every column-symmetric arrangement is
   station-symmetric under the **mirrored** convention and **none** of them is under the plain one;
   **three further** arrangements, whose columns are *not* centro-symmetric, are station-symmetric at
   the **plain 8 bp offset**, where the whole azimuth departure is **4.2857°** — so `C-0133`'s
   **30.0°** station is a cost of its **arrangement**, not of the twist correction. Selecting on
   column symmetry finds three enumerable lattices where there are **seven**. **`F7` did not fire.**

---

## `T-216`, Deliverable 3 — the census given a consequence, and it buys back `C-0133`'s 30° station

Every one of the **seven** enumerable `(arrangement, convention)` lattices, at parity 0, plus
`C-0133`'s recommended one at parity 1 — **sixteen exhaustive centro-symmetric enumerations**, each
of 163 296, 198 288 or 11 664 placements, under `C-0022`'s carried collar and `C-0017`'s mandate
shared over 34 roots.

| arrangement | offset convention | worst station azimuth | arm | enumerated | zero prestrain | **corrected graded field** | flat? |
|---|---|---|---|---|---|---|---|
| `16+15+16+16+16+15+16` | mirrored | **30.0°** | 23 rises | 163 296 | 0.0602892387 | **0.0580196384** | yes |
| `15+16+16+16+16+15+16` | **plain** | **4.2857°** | 23 rises | 163 296 | **0.0552787638** | **0.0629599351** | yes |
| `16+15+16+16+15+16+16` | **plain** | **4.2857°** | 23 rises | 163 296 | 0.0603128259 | 0.0659104296 | yes |
| `15+16+16+16+16+16+15` | mirrored | 30.0° | 23 rises | 163 296 | 0.0553958981 | 0.0825537347 | yes |
| `16+16+15+15+16+16+16` | plain **=** mirrored | 4.2857° | 24 rises | 11 664 | 0.14493967 | 0.135564386 | **no** |
| `16+16+15+16+15+16+16` | mirrored | 30.0° | 24 rises | 11 664 | 0.1449728 | 0.184339439 | **no** |
| `16+15+16+16+16+15+16` | mirrored, **parity 1** | 30.0° | 23 rises | 198 288 | 0.0704758494 | 0.0751454494 | yes |

Four readings from that table.

1. **`C-0133`'s recommended lattice is confirmed as the flattest**, `0.0580196384`, reproduced from
   its result file at relative departure `8.5e−10` on an independently constructed host — and its
   selection rule (the peak register angle) picks the same arrangement the flatness does.
   **`F5` fired, as declared, and the agreement is the finding.**
2. **But only under the field it was selected for.** At **zero** prestrain the winner is a
   *different* arrangement, `15+16+16+16+16+15+16` at `0.0552787638`, and `C-0133`'s is fourth of seven.
   The sixth *"quote it with the state it is read at"* in this corpus, read on a **lattice**.
3. **The 30° station is buyable back for 8.5 % of the flatness.** The best lattice carrying **no**
   mirrored offset — `15+16+16+16+16+15+16`, whose whole azimuth departure is `4.2857°` — dishes
   **0.0629599351** against the recommended `0.0580196384`, **1.085×**, and both are well inside
   `T-5b`'s 0.10. `C-0133` reports the 30° as a cost of the twist correction; it is a cost of the
   **arrangement**, and it has a price.
4. **The parity binary transfers with almost the same cost.** On the recommended lattice the other
   parity dishes `0.0751454494` against `0.0580196384` under the graded field (**1.29517266×**)
   and `0.0704758494` against `0.0602892387` at zero prestrain (**1.169×**), where `C-0090`'s two phases at
   the uniform 38.08 nm differ by **1.13752709**. Same object, same size, one lattice non-uniform and
   one uniform.

**And the 24-rise arm is a trap.** The two arrangements that admit `C-0085`'s full 24-rise arm — so
that `CH-0159`'s *"the twist correction costs one base pair of arm"* would not apply — are exactly
the two whose 34-root family collapses from 163 296 to **11 664** and whose best placement is
**outside** `T-5b` at both prestrain states. The arm is not free; it is paid for in placement family.

---

## `T-217`, Deliverable 1 — the cheap bound, and it closed the task in one division

caDNAno's honeycomb lays its azimuth period out as **two turns in 21 bp** (Douglas et al., quoted
verbatim in `C-0119`). So

&nbsp;&nbsp;&nbsp;&nbsp;`ω_d = 720/21 = 34.2857… °/bp`, **which is `360/10.5`, the same number.**

| | square sheet | honeycomb |
|---|---|---|
| azimuths per helix | 4 | **3** |
| base pairs per azimuth step | 8 | **7** |
| azimuth period | 32 bp | **21 bp** |
| half turns in that period | 6 | **4** |
| integral at 10.5 bp/turn? | **no** | **yes** |
| design twist | 33.75 °/bp | **34.2857 °/bp** |
| mismatch against B-DNA | `+0.535714286 °/bp` | **`0.0`** |
| accumulated over a 112 bp row | **`+60.0°`** | **`0.0°`** |

> **The honeycomb has nothing to correct.** `C-0107`'s boundary layer has driver **exactly zero**
> there, and `C-0104`'s 15.4497275° threshold, `C-0107`'s 17.15–24.98° and every register number in
> `C-0133` are **square-lattice** numbers that do not transfer. **`F1` did not fire.**

**And the reason unifies both tasks in one line.** `10.5 = 21/2`, so a half turn is `5.25` base
pairs: `h` half turns is an integer number of base pairs **iff `h ≡ 0 (mod 4)`**, and the distance to
the nearest integer is **exactly 0.25** for odd `h` and **exactly 0.5** for `h ≡ 2 (mod 4)`.
`C-0133`'s theorem is the **odd** case — a square-lattice boustrophedon needs an odd number of half
turns across its row. The honeycomb is the `h = 4` case. **Asserted over 2001 half-turn counts;
`F2` did not fire.**

---

## `T-217`, Deliverable 2 — the connectivity half, derived rather than transferred

Scaffold crossovers to neighbour class `j` sit at `7j ± 5 (mod 21)` — caDNAno's *"five base pairs, or
half a turn, upstream or downstream"*, which is `C-0119`'s `7k ± 5`. A raster row is the stretch
between the crossover the scaffold arrives on and the one it leaves on, and those go to two
**different** neighbours, so

&nbsp;&nbsp;&nbsp;&nbsp;`N ≡ 7Δ + {0, 10, 11} (mod 21)`, `Δ = (b − a) mod 3 ≠ 0`.

| `Δ` | admissible `N (mod 21)` |
|---|---|
| 1 | `{7, 17, 18}` |
| 2 | `{3, 4, 14}` |
| 0 — *back to the same neighbour*, which a progressive raster may not do | `{0, 10, 11}` |

**The construction is gated by reproducing `C-0086`.** Run on the square sheet's own azimuths — four
classes 8 bp apart, the two in-plane neighbours two classes apart, no scaffold offset — the *same*
expression returns `N ≡ 16 (mod 32)`, i.e. **16, 48, 80, 112, 144, 176**, which is `C-0086`'s *"odd
multiples of 16 bp"* exactly. **`F3` did not fire.**

| lattice / turn sense | nearest admissible width to §3's 40.0 nm | departure |
|---|---|---|
| square sheet (`C-0086`) | 112 bp = **38.08 nm** | **−4.80 %** |
| square sheet, twist-corrected (`C-0133`) | 110 bp = **37.40 nm** | **−6.50 %** |
| honeycomb, `Δ = 1` | 112 bp = **38.08 nm** | **−4.80 %** |
| honeycomb, `Δ = 2` | **119 bp = 40.46 nm** | **+1.15 %** |

**The honeycomb's width list is 4.571× denser** — one admissible width every **7.00 bp** at a fixed
turn sense against the square sheet's every **32** — and it reaches §3's nominal width far closer
than anything on the square lattice. **`F4` and `F5` did not fire.**

**But the two turn senses are DISJOINT**, `{7,17,18} ∩ {3,4,14} = ∅`, and `C-0119`'s own 112 bp row
has residue **7**: admissible at `Δ = 1`, inadmissible at `Δ = 2`. That is
[`CH-0165`](../challenges/CH-0165-an-integral-scaffold-lattice-is-necessary-not-sufficient.md) —
integrality is **necessary** and `C-0119` reads it as **sufficient**. **`F7` did not fire**: the row
*is* admissible, at one sense of two.

---

## `T-217`, Deliverable 3 — the quarter base pair relocates, and stops accumulating

caDNAno quantises its scaffold half-turn offset to **5 bp** against the exact **5.25**, so every
*scaffold* crossover carries **0.25 bp = 8.5714°** of azimuth departure — `C-0133`'s invariant, to the
last digit. **`F6` did not fire.**

But on the square sheet that quarter base pair is a property of the **whole row** and drives
`C-0107`'s boundary layer; here it is a **fixed, local, non-accumulating** departure at each scaffold
crossover, and the **staple** lattice — which carries every crossover column, every register field
and every attachment station — is at B-DNA's own twist **exactly**. A local `8.57°` against a global
`60.0°`.

## `T-217`, Deliverable 4 — the one constant the whole favourable result rests on

| B-DNA bp/turn | source | mismatch [°/bp] | accumulated over 112 bp [°] | fraction of the square sheet's 60.0 |
|---|---|---|---|---|
| 10.34 | the twist at which the honeycomb's own driver equals the square sheet's | `+0.530533296` | `+59.4197292` | **0.990** |
| 10.44 | the low end of the solution B-DNA band in circulation | `+0.197044335` | `+22.0689655` | **0.368** |
| **10.5** | **the locked value — the honeycomb's design twist and B-DNA's, one number** | **`0.0`** | **`0.0`** | **0.000** |
| 10.55 | a 0.5 % departure the other way | `−0.162491537` | `−18.1990521` | 0.303 |
| 10.6667 | the **square** lattice's design twist, for scale | `−0.535819754` | `−60.0118125` | 1.000 |

**A 0.57 % error in the assumed twist costs 36.8 % of `C-0086`'s uncorrected driver.** The honeycomb's
advantage is exactly as good as the constant, and nothing in this repository measures it.

**No solve is justified on the honeycomb and that is a result, not an omission.** `CLAUDE.md`:
*"`OrigamiGrillage` never reads `layers` or `interlayerCoupling`"*, and `CrossoverLayout`'s two-parity
alternation **is** the square lattice's combinatorics — so a dishing number computed here would be a
square-lattice number wearing a honeycomb label.

---

## The five verification gates

Executed as **20 gate-named tests** in `src/test/kotlin/structure/LatticePhaseCensusTest.kt`,
written before the model.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a lattice carries degrees per base and no length; the azimuth period is a base-pair count and the step degrees are `270`/`240`; unphysical lattices, arrangements and half-turn counts throw | **PASS** |
| **2 — limiting cases** | **the half-turn integrality theorem** over 2001 counts, at all three residue classes mod 4; the honeycomb period is exactly four half turns; a lattice read at its own design twist has zero mismatch at every row length; **the translation group is `{0}`** on every seamless row; the 21-member family, its 3 palindromes and its 12 reflection classes; a uniform row has exactly **one** arrangement | **PASS** |
| **3 — symmetry and reproduction** | the neighbour-azimuth construction returns `C-0086`'s list exactly; the honeycomb residues and their disjointness; the residue set depends only on `Δ`; `C-0119`'s 112 bp row; **`C-0090`'s phases 8 and 24 are one lattice and two parities**; parity 0 reproduces `twistCorrectedUpwardSites` to `1e−12` nm; the two parities **partition** the plane lattice; a palindromic domain sequence gives a centro-symmetric column set and conversely | **PASS** |
| **4 — exactness and convergence** | the honeycomb scaffold offset is a quarter base pair and `8.5714°`; the width-list density ratio is exactly `32 × 3/21`. Convergence: the dishing sample grid 41 → 81 → 161 moves `0.0`; the beam subdivision 1 → 2 → 4 moves `6.2e−3` then `7.0e−4`; the register field's hinge-smearing convention 1 → 2 → 4 moves `1.0e−2` then `2.6e−3` | **PASS** |
| **5 — literature and upstream** | **ten reproductions at departure `0.0` or `1e−9`**: `C-0119`'s four honeycomb constants and all 32 of its scaffold offsets on a 112 bp row; `C-0086`'s width list; `C-0133`'s residual invariant; `C-0133`'s two 110 bp enumerated optima (`0.0602892387` and `0.0580196384`); `C-0090`'s two phase optima. The honeycomb rules read directly from PMC2731887 in `gpd/data/T-151-sources/` — **zero fetches** | **PASS** |

### The declared falsifiers, and what happened

| task | # | falsifier | fired? | outcome |
|---|---|---|---|---|
| `T-216` | **F1** | a non-zero rigid translation leaves both end columns on the tile edges | **no** | the group is `{0}` on every row checked |
| `T-216` | **F2** | phases 8 and 24 do not give identical column positions at 38.08 nm | **no** | identical to `1e−12` nm, parities inverted at all eight |
| `T-216` | **F3** | some arrangement carries other than eight columns | **no** | 8 at all 21 — the identity `domains + 1` |
| `T-216` | **F4** | the centro-symmetric arrangements are not three, or the reflection classes not twelve | **no** | 3 and 12 |
| `T-216` | **F5** | the flatness ranking of the centro-symmetric arrangements **agrees** with their register ranking | **YES — as declared** | see below |
| `T-216` | **F7** | no arrangement is centro-symmetric at the plain 8 bp offset, so the 30° station is unavoidable | **no** | **3 of 21 are**, at 4.2857° everywhere |
| `T-216` | **F6** | a free tile under a uniform load on a uniform foundation dishes something | **no** | largest `1.7e−07` |
| `T-217` | **F1** | the honeycomb's design twist differs from `360/10.5` | **no** | mismatch `0.0` |
| `T-217` | **F2** | an odd half turn is integral, or a quadruple one is not | **no** | 0 of 1001 and 0 of 500 |
| `T-217` | **F3** | the construction does not reproduce `C-0086` | **no** | 16, 48, 80, 112, 144, 176 |
| `T-217` | **F4** | no admissible honeycomb width within 5 % of 40.0 nm | **no** | 119 bp, `+1.15 %` |
| `T-217` | **F5** | the honeycomb list is not denser | **no** | **4.571×** |
| `T-217` | **F6** | the scaffold half-turn residual is not exactly a quarter base pair | **no** | `0.2500 bp = 8.5714°` |
| `T-217` | **F7** | `C-0119`'s 112 bp row is inadmissible at **every** turn sense | **no** | admissible at `Δ = 1`, not at `Δ = 2` |

---

## Still open — named, not answered

1. **Which turn sense `Δ` a caDNAno `15 × 4` honeycomb x-raster carries.** It decides whether
   `C-0119`'s 112 bp row is buildable and whether the 40.46 nm width is available, and a raster that
   carries **both** senses has no admissible row length at all. It is a lattice reading of the
   caDNAno cross-section, not a solve — `CH-0165`.
2. **The honeycomb has no station lattice, no plan ceiling and no placement family in this
   repository**, so nothing here re-reads `C-0126`'s four-layer flatness on its own lattice.
3. **`C-0022`'s collar is carried, not re-solved**, at 37.40 nm — `C-0133`'s open item, unchanged.
4. **The measured staple dropout is not applied**; every flatness here is a zero-defect optimum,
   which `C-0092`/`C-0103` establish is a cancellation a missing path destroys.
5. **The parity binary is measured on one arrangement only.** It is a design variable at every one of
   the 21, and only `C-0133`'s recommended lattice is read at both values here.
