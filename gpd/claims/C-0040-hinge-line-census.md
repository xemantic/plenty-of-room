# C-0040 — A hinge line on a 40 nm tile carries FOUR crossovers, at every one of the 32 phases; sixteen needs 163 nm of collinear interface or 33 duplexes, so `E5g16`/`E5a16` rest on a count the lattice does not supply — and the count that does exist reaches §3's acceptable stroke and not its desired one

| | |
|---|---|
| **Task** | [`T-81`](../tasks/T-81-hinge-line-census.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Verification type** | **logical** (a count on a lattice whose pitch is cited and measured — no simulation can move a count) **+ in-silico** (`C-0015`'s own `CrossoverLayout` re-run as a library over the complete 32 bp phase space, and `C-0034`'s placement pipeline re-run at every hinge count from 1 to 32) |
| **Verdict** | **PASS on the method, FAIL on the assertion, and the branch survives at §3's ACCEPTABLE stroke only.** **Sixteen crossovers in one hinge line do not exist on a 40 nm tile, at any phase, in either direction of the sheet.** The largest hinge line the tile carries is **four**, at **every one of the 32 phases**; the other parity carries **three** at the 22 seven-column phases and **four** at the 10 eight-column ones — so **the phases that maximise a hinge line are exactly `C-0015`'s ten centro-symmetric ones**, which nothing in either construction forced. Sixteen demands **163.2 nm** of collinear interface (**4.08 tiles**) along the helices, or **33 duplexes = 88.8 nm** (**2.22 tiles**) across them — and the transverse line restrains the **wrong axis** in any case. The absolute geometric ceiling, a line spanning the tile dilated by its own arm, is **six**; at 45 paths each flexure can own **one or two**. **Sixteen crossovers can be *assembled* — four interfaces of four — but interfaces compose in SERIES: `n_eff = n_i·3(2m−1)/(m(2m+1))`, so sixteen are worth 2.333 of hinge, 14.6 % of their own count, and 45 such flexures assemble to 16.03 pN/nm, 2.08× too soft.** Re-priced: `C-0034`'s pipeline needs **10** crossovers to lift 10 nm by rotation and **12** to do it inside `C-0023`'s ceiling, against **3** for the ceiling at the acceptable stroke. **At four crossovers the arm places at 7.748 nm: §3's acceptable 3 nm clears at 36.58 pN/nm, and the desired 10 nm is out of geometric reach. At the one or two a flexure can actually own, the tangent is 42.0–54.1 pN/nm and even the acceptable stroke fails `C-0023`'s own ceiling.** Raises [`CH-0054`](../challenges/CH-0054-the-sixteen-crossover-hinge-line-does-not-exist.md). |
| **Maturity** | **TRL 1–3. A count on a lattice. NOTHING HERE IS MEASURED**, no sheet has been built, and no routing here is a sequence design. |
| **Provenance** | `gpd/results/T-81-hinge-line-census.json`, produced by `anchoring.HingeLineCensusStudyKt`; **6 cheap bounds, 32 phase records, 6 topologies, 32 fan records, 13 designs, 6 demand records, 4 sensitivities, 16 upstream reproductions, 21 convergence records**; **27 gate-named tests in `HingeLineCensusTest`**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree, with two concurrent agents' mid-TDD test files dropped by `--drop-file` (`anchoring/TwoSpringElasticaTest.kt`, `anchoring/PairedPerpendicularJunctionTest.kt`); the result file re-run through `tools/study.sh` and reported *"no result file changed"* |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile, 15 duplexes; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet at the SAXS-measured 2.69 nm interhelical distance |
| **Consumes** | [`C-0015`](C-0015-crossover-phase-and-registration.md) (`CrossoverLayout`, the 32 bp phase period, the parity rule, the 49/56 inventory, the 3 × 15 grid — **re-run as a library, not tabulated**), [`C-0034`](C-0034-guided-arm-anchorage.md) (`anchoredArmForStiffness`, `guidedArmFactor`, `armRestraintParameter`, the `A2` anchorage — **re-run as a library**), [`C-0029`](C-0029-perpendicular-junction-routing.md) (`RotatingHingeArm`, `rotatingArmForStiffness`, `hingeArmCeiling`, `BForm`), [`C-0023`](C-0023-two-sided-coupling.md) (the 40 pN/nm ceiling, the 45 paths, the mandate), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`k_θ`, `EI`, `d`, the rise, the across-helix rigidity, the localisation result) |
| **Raises** | [`CH-0054`](../challenges/CH-0054-the-sixteen-crossover-hinge-line-does-not-exist.md) against `C-0023`, `C-0029` and `C-0034` |
| **Challenged by** | [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md), from [`C-0049`](C-0049-compliance-ceiling-stroke.md)/[`C-0050`](C-0050-desired-stroke-reach.md) (`T-107`/`T-108`), against the verdict clause *"at the one or two a flexure can actually own, the tangent is 42.0–54.1 pN/nm and even the acceptable stroke fails `C-0023`'s own ceiling"* and the design rows behind it — those are `C-0034`'s **series composition**, which `CH-0053` supersedes; on `C-0039`'s exact elastica the same design places at a **9.131 nm arm and 39.18 pN/nm, INSIDE the ceiling**. **The census, the ledgers, the fan law and the `n_eff` result are untouched and re-run**; the desired-stroke verdict is untouched. See also [`C-0046`](C-0046-fewer-longer-flexures.md), which reaches the same conclusion independently |
| **Extended by** | [`C-0046`](C-0046-fewer-longer-flexures.md) (`T-99`), which sweeps **this claim's own open item 2** — the path-count against hinge-count trade. Every number here reproduces. The trade is **degenerate**: the arm the inventory can place is a function of the **product** `n·h` alone, 5.387 nm at all eight splits of `n·h = 56`, and what breaks the tie is `C-0034`'s **per-flexure** anchorage, so the placed arm grows with the **path** count and *fewer, longer* is the wrong direction. **0 of 31 points reach §3's desired stroke.** This claim's hinge-count window `3 ≤ n ≤ 6` becomes **`h = 1`** once the inventory ledger is enforced on the exact elastica, and the surviving design spends **80–100 %** of the sheet's crossovers. Also raises [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md) |

---

## The claim, in one line

**Three claims priced a design on sixteen crossovers in one hinge line and none of them counted; the count is four, it is four at every phase of the complete design space, and it is four for a reason that is not a quantisation but a density — a crossover serves one *interface* every 32 bp, so sixteen of them collinear need 163 nm of a sheet that is 40 nm long, and the only way to assemble sixteen at all puts them in SERIES, where they are worth 2.3.**

---

## The three cheap bounds, which ran first and decided the verdict

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **the collinear interface a 16-crossover hinge line demands**, `(16 − 1) × 32 bp` | **163.2 nm = 4.08 tiles** | the longitudinal reading, in one division. Had it come out under 40 nm the task would have closed in a paragraph — declared in the task file as falsifier 2 |
| **2** | **the duplexes a 16-crossover TRANSVERSE line demands**, `2n + 1` | **33 duplexes = 88.8 nm = 2.22 tiles** | the other direction of the sheet, in a second division — and the transverse line's crossovers are dihedral springs about a line running the *other* way, so it does not supply `n k_θ` at all |
| **3** | **the inventory**, 45 paths × 16 against `C-0015`'s whole tile | **720 against 49–56, i.e. 12.86×** | the demand side. Even **one** flexure at sixteen takes **28.6 %** of every crossover the sheet has |

Only because all three fail in the same direction was the full sweep worth running at all —
and what the sweep adds is not the verdict but **the count that does exist and what the design does at it**.

---

## The lattice, and why the pitch is 32 bp and not 16

Crossovers recur every **16 bp** along a *helix* — 1.5 turns of the square lattice, `16/10.67 = 1.500` —
but they **alternate between a helix's two neighbours**, because 1.5 turns advances the backbone azimuth by 540° ≡ 180°,
i.e. to the *other* side of the duplex.
So a given **interface** is linked every **32 bp = 3.000 turns = 10.88 nm**,
and interface `b` carries the columns of parity `b mod 2` of the 16 bp column lattice.

> **`k_θ` = 13.53 pN·nm/rad is the *interhelical dihedral* spring** — it resists rotation of duplex `b+1` relative to duplex `b`
> **about their common interface line, which runs along the helices**.
> That is how `C-0009`'s grillage uses it and how `C-0015` recovers `D_⊥` from it.
> **`n k_θ` is therefore the right spring for a hinge whose axis runs along `x`, and for no other axis** —
> which is what makes the transverse reading a category error rather than merely a short one.

---

## 1. The census — complete over all 32 phases, not sampled

| | |
|---|---|
| **largest hinge line, at every one of the 32 phases** | **4** |
| smallest (the other parity) | **3** at the 22 seven-column phases, **4** at the 10 eight-column ones |
| **the ten best phases** | **6, 7, 8, 9, 10, 22, 23, 24, 25, 26 bp** — four on **both** parities |
| the cost of the wrong parity elsewhere | **25 %** (three against four) |
| tile inventory | **56** at those ten phases, **49** at the other 22 — `C-0015` reproduced exactly |
| **phases reaching 16** | **none** |

### The result that was not anticipated

&nbsp;&nbsp;&nbsp;&nbsp;**The phases that maximise a hinge line are exactly `C-0015`'s ten centro-symmetric phases.**

Both are the eight-column phases: `C-0015`'s parity rule says centro-symmetry holds when `(columns + duplexes)` is odd,
and with 15 duplexes that is `columns` even, i.e. eight.
The two statements were derived for different purposes — one about a symmetry group, one about a count on one interface —
and nothing in either construction forced them to coincide.
It is asserted as a gate-3 test rather than observed in a table.

---

## 2. The topology ladder — five readings of *"one hinge line"*, and one of the wrong axis

| id | reading | line [nm] | best | worst | reaches 16 |
|---|---|---|---|---|---|
| **`L1`** | **the flexure's own plan share at 45 paths** (3 attachment columns along `x`) | 13.333 | **2** | 1 | no |
| **`L2`** | **the adopted arm's own length**, `C-0034`'s `E5a16` | 11.028 | **2** | 1 | no |
| **`L3`** | **one full-length interface of the tile** — one flexure owning a whole edge | 40.000 | **4** | 3 | no |
| **`L4`** | **the tile dilated by its own arm** — the longest line any flexure attached to the tile can reach | 62.057 | **6** | 5 | no |
| `L5` | an unbounded superstructure: whatever sixteen demands | 163.300 | 16 | 15 | **priced and refused** |
| **`L6`** | **a transverse fold line** — `TASKS.md`'s own guess | 40.35 | **7** | 7 | no, **and the wrong axis** |

**`L5` is the only escape and it costs a device.**
Forty-five hinge lines of 163.2 nm at a 2.69 nm duplex pitch is **19,755 nm², 12.3× the tile's whole footprint** —
which is [`T-96`](../../TASKS.md)'s plan-view question with a factor attached, and it is answered here in the negative.

**`L6` fails twice.** Fifteen duplexes give fourteen interfaces and a transverse line serves **seven** of them,
not sixteen; and each of those seven is a dihedral spring about a line running along the helices,
so a fold *across* the helices is resisted by the duplexes' own `EI` and not by any crossover.
`TASKS.md`'s guess — *"the 16 must come from 16 duplexes sharing a transverse fold line"* — is short by 2.3× **and** on the wrong axis.

---

## 3. Where sixteen crossovers CAN be found, and what they are worth

Sixteen crossovers **can** be assembled into one flexure: four interfaces of four, each line 32.64 nm long, which fits the tile.
But a raft hinged on several parallel lines is a **fan**, and its interfaces are in **series**, not in parallel:
each carries only the moment of what is outboard of it and turns through its own angle.
Derived, not asserted, with the lines at `0, d, 2d, …` from the root and the load at `(m − ½)d`:

&nbsp;&nbsp;&nbsp;&nbsp;`δ/F = d² Σ_{i=1}^{m}(i − ½)²/(n_i k_θ)` &nbsp;→&nbsp; **`n_eff = n_i · 3(2m − 1)/(m(2m + 1))`**, exactly `n_i` at `m = 1`.

| `m` | `n_i` | **total crossovers** | lever [nm] | **`n_eff`** | `n_eff`/total | assembled [pN/nm] | of the mandate | lattice/continuum |
|---|---|---|---|---|---|---|---|---|
| 1 | 4 | 4 | 1.345 | **4.000** | 1.000 | 1346.2 | 40.39 | 6.000 |
| 2 | 4 | 8 | 4.035 | 3.600 | 0.450 | 134.6 | 4.04 | 2.222 |
| **4** | **4** | **16** | **9.415** | **2.333** | **0.146** | **16.03** | **0.481** | **1.469** |
| 5 | 4 | 20 | 12.105 | 1.964 | 0.098 | 8.16 | 0.245 | 1.358 |
| 8 | 4 | 32 | 20.175 | 1.324 | 0.041 | 1.98 | 0.059 | 1.209 |

&nbsp;&nbsp;&nbsp;&nbsp;**Sixteen crossovers arranged as a fan are worth 2.333 of hinge — 14.6 % of their own count — and 45 such flexures assemble to 16.03 pN/nm, 2.08× too soft for §3's mandate.**

The fan that *would* place needs **nine crossovers per interface** — eight reaches 32.05 pN/nm and nine 36.06 — i.e. **87.0 nm** of collinear line, **2.18× the tile**. Swept in the result file at `n_i` = 3, 4, 8 and 9.
So the fan does not rescue the count either, and it fails on the same currency: collinear interface.

> **This is the honest answer to *"where else could sixteen come from"*, and it is a mechanism, not a shortage.**
> The fan is *buildable* — four interfaces of four crossovers each fits inside 40 nm at every phase —
> and it is the arrangement that puts the outboard crossovers on short lever arms, which is why it loses 85 % of its count.

---

## 4. `C-0034`'s design, re-priced at every hinge count

`C-0034`'s `anchoredArmForStiffness` re-run unchanged on its adopted `A2` anchorage
(78.235 pN·nm/rad, the arm's own duplex end), 45 paths, secant placed at 33.3333 pN/nm at §3's acceptable 3 nm.

| `n` | arm [nm] | bp | realised `c` | tangent(3) | tangent(10) | pN per bond at 3 nm | verdict |
|---|---|---|---|---|---|---|---|
| **1** | 4.765 | 14.0 | 5.595 | **54.11** | 533.2 | 4.27 | **FAILS `C-0023`'s ceiling at the ACCEPTABLE stroke** |
| **2** | 6.079 | 17.9 | 6.067 | **42.01** | 261.0 | 3.04 | **FAILS the ceiling at the acceptable stroke** |
| **3** | 7.024 | 20.7 | 6.366 | 38.32 | 149.4 | 2.44 | acceptable stroke clears; **cannot reach the desired one** |
| **4** | **7.748** | **22.8** | **6.575** | **36.58** | 98.16 | **2.05** | **acceptable stroke clears; cannot reach the desired one** |
| **6** | **8.795** | 25.9 | 6.851 | 35.01 | 61.07 | 1.58 | **acceptable stroke clears; cannot reach the desired one** |
| 8 | 9.517 | 28.0 | 7.026 | 34.33 | 48.60 | 1.30 | `C-0034`'s own failing row, reproduced |
| 9 | 9.801 | 28.8 | 7.091 | 34.12 | 45.28 | 1.19 | still short of the stroke |
| **10** | **10.047** | 29.6 | 7.147 | 33.97 | **42.91** | 1.10 | **reaches the stroke, past the ceiling there** |
| **12** | **10.452** | 30.7 | 7.235 | 33.77 | **39.83** | 0.96 | **the first count that passes everything** |
| 16 | 11.028 | 32.4 | 7.356 | 33.57 | 36.78 | 0.76 | `C-0034`'s `E5a16`, reproduced exactly |
| 32 | 12.080 | 35.5 | 7.561 | 33.38 | 33.99 | 0.42 | — |

### The three thresholds, and the three supplies

| | | |
|---|---|---|
| **needed** for `C-0023`'s 40 pN/nm ceiling at the **acceptable** stroke | **3** | |
| **needed** to lift §3's **desired** 10 nm by rotation (`δ = r sin θ < r`) | **10** | |
| **needed** to do that **inside** the ceiling | **12** | |
| **supplied** by one full-length interface of the tile (`L3`) | **4** | one flexure only |
| **supplied** at the absolute geometric ceiling (`L4`) | **6** | one flexure only |
| **supplied** per flexure at 45 paths (`L1`) | **1–2** | the realistic design |

&nbsp;&nbsp;&nbsp;&nbsp;**So the design window in hinge count is `3 ≤ n ≤ 6` — it clears §3's ACCEPTABLE stroke and cannot reach the DESIRED one — and at the 1–2 crossovers forty-five independent flexures can each own, even the acceptable stroke fails `C-0023`'s own compliance ceiling.**

The arm at four crossovers is **7.748 nm**, and `δ = r sin θ < r` is `C-0029`'s own geometric statement, needing no constitutive law:
**a 7.75 nm arm cannot lift 10 nm.**
It can be *pushed* to 10 nm by bending the arm as well as rotating the hinge, and the tangent there is **98.2 pN/nm**, 2.45× the ceiling.

---

## 5. The conflict with `C-0015`'s 45 attachments

Both consume the same lattice, and the arithmetic does not close.

| currency | supply | demand at `n = 16` | demand at `n = 1` |
|---|---|---|---|
| **crossovers** | **49–56** on the whole tile | **720** — **12.86×** | **45** — 80–92 % of the inventory |
| **collinear interface line** | 14 interior interfaces + 2 free edges × 40 nm = **640 nm** | **7344 nm** — **11.5×** | 0 nm (one crossover needs no line) |

**And every crossover in that inventory is already a structural load path** in `C-0009`'s and `C-0015`'s grillage —
it is what carries the tile's across-helix rigidity `D_⊥` and what the 3 × 15 flatness grid was minimised against.
Converting an interface into a free hinge line removes it from the sheet.

&nbsp;&nbsp;&nbsp;&nbsp;**`C-0015`'s 45 attachments and a 16-crossover hinge cannot be built on the same sheet. They cannot be built at `n = 2` either without double-booking four fifths of the tile's crossovers.**

---

## 6. The continuum control, and what kind of fact this is

`CLAUDE.md` requires a continuum comparison beside every lattice claim, and here it changes the *category* of the answer.

| | |
|---|---|
| **continuum reading** of a 40 nm hinge line — crossover **line density** `1/p` times the length | **3.676** |
| lattice, best phase | 4 — **+9 %** |
| lattice, worst parity | 3 — **−18 %** |
| **the assertion**, against the continuum | **16/3.676 = 4.35×** |

&nbsp;&nbsp;&nbsp;&nbsp;**The quantisation is worth −18 % to +9 % and the assertion is out by 4.35×, so the verdict is a CONTINUUM fact about crossover density, not a lattice artefact.** A continuum sheet of the same size and the same crossover density does not deliver sixteen either.

The fan's own discreteness is bounded the same way: `2m(2m+1)/(2m−1)²` = **6.00** at one interface, **2.22** at two, **1.47** at four, `1 + 3/(2m)` asymptotically — **and always above one**, so the lattice fan is the **softer** of the two. That is the direction `CLAUDE.md` warns is not automatic.

---

## The five verification gates

Executed as **27 gate-named tests** in `src/test/kotlin/anchoring/HingeLineCensusTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the pitch is a base-pair count times a rise and is linear in both; a hinge line of `n` demands `(n − 1)p`, and the demand and the census are **exact inverses** over `n = 1…24`; the count is a length over a pitch, so doubling the pitch halves it; unphysical arguments throw at nine entry points | **PASS** |
| **2 — limiting cases** | a line shorter than one pitch holds exactly one crossover, and one pitch exactly two; the fan reduces to `n_i` **exactly** at one interface, over eight counts; a transverse line on `D` duplexes serves `⌈(D−1)/2⌉` of one parity, and sixteen needs 33 duplexes; the fan's effective count is strictly decreasing in the interface count | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the base-pair sweep is **complete** — refining the phase grid 100× produces no count the 32 base-pair phases do not already contain; the lattice fan converges to the continuum strip **monotonically from above**, exactly 6 at `m = 1`, 20/9 at 2, 72/49 at 4, and `1 + 3/(2m)` at 64 and 256; the re-priced arm reproduces its own target secant to `1e−7` at every hinge count from 1 to 16 | **PASS** |
| **5 — literature and upstream** | `C-0015`'s 56/49 inventory, its ten eight-column phases and its ten centro-symmetric phases, and **this census against `CrossoverLayout` itself at every one of the 32 phases, departure `0`**; `C-0009`'s across-helix rigidity `k_θ d/p` to `1.3e−16`; `C-0029`'s `E5g8`, `E5g16`, `E5g32` and the cantilever ceiling to ≤ `3.8e−9`; `C-0034`'s `E5a16` arm, both tangents, its realised `c` and its failing 8-crossover row; `32 bp = 3.000 turns` of the square lattice and `16 bp = 1.500`. **Worst departure over 16 reproductions, excluding the five upstream values their own claims quote rounded (11.028, 33.56, 36.78, 9.52, 7.356) and the deliberate literature comparison: `3.8e−9`** | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The two parities' counts sum to the column count at every phase** — a conservation law the census never imposed,
   because the two parities are counted by two independent calls on two different lattice offsets.
2. **A 16 bp shift swaps the two parities exactly, and a 32 bp shift is the identity** — asserted at all 32 phases.
   This is `C-0015`'s *"the period is `p`, not `p/2`"* recovered from a completely different construction,
   and it is what makes the sweep complete rather than half of one.
3. **Centro-symmetry holds exactly when `(columns + duplexes)` is odd**, at all 32 phases, and at exactly ten of them —
   `C-0015`'s parity rule, and the coincidence with the maximum hinge line falls out of it rather than being asserted.
4. **The general fan reproduces the uniform one at six interface counts and is exactly `n` for a single line at the root** —
   two independently written expressions, a compliance sum and a closed form, agreeing to the last digit with nothing forcing it.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a per-interface pitch other than 32 bp | **no, and it is taken deliberately anyway** | at the 16 bp per-helix mis-reading the count is **8**; at honeycomb's 21 bp it is **6**; at both together **12**. **The most optimistic lattice reading in circulation still misses sixteen** |
| 2 | the census reaching sixteen at some phase | **no** | four, at every phase, and it is asserted as a runtime check in the study |
| 3 | `C-0015`'s inventory failing to reproduce | **no** | 56/49 at 10/22 phases, and agreement with `CrossoverLayout` itself at departure `0` |
| 4 | `C-0034`'s design not reproducing | **no** | arm, both tangents, realised `c` and the failing 8-crossover row all inside the rounding its own claim quotes |
| 5 | the shortfall being a quantisation artefact | **no** | the continuum reading is 3.676 and the assertion is 4.35× above it |

---

## Does `C-0034`'s verdict survive?

**Its model survives in full and reproduces to nine digits. Its *design* does not, and the failure is upstream of everything it priced.**

| `C-0034` said | this claim finds |
|---|---|
| `E5a16` — an 11.03–12.50 nm arm on **16 crossovers** | **the 16 do not exist.** Four is the most a hinge line on this tile carries, at any phase |
| *"`P4` PASS at 16 and 32 crossovers; **FAILS at 8**"* | **reproduced exactly** — and the supply is 4, which is below 8, so `P4` fails at the count that exists |
| *"Whether 16 crossovers can be assembled into one hinge line on a 40 nm tile at all"* — its own open item 5 | **answered: no.** In one line, four; in a fan, sixteen crossovers are worth 2.333 |
| the cap is 13.43 nm and the arm 11.03–12.50 | **untouched.** The cap is a property of the *anchorage* and is independent of the hinge count; what the count moves is the **placement**, and at four crossovers it places at 7.75 nm — 1.73× below the cap, which therefore stops binding altogether |
| *"the dominant compliance term has changed sides — the ARM, 58.5 %"* | **it changes back.** At four crossovers the **hinge** carries 78.3 % of the path compliance, and at one, 95.2 % — `C-0023`'s original reading. `A8.2`'s named quantity is a function of a count nobody had checked |
| tangent 33.56 / 36.78 pN/nm inside the 40 pN/nm ceiling | **reproduced exactly**, and at the realisable counts it is 36.58 (`n = 4`) and 42.01–54.11 (`n = 1–2`) |
| the counting theorem at the arm's far end, `c(ρ)`, the fixed-point cap, `CH-0044` | **untouched and used** rather than restated |

---

## Validity range

- **TRL 1–3. Nothing here is measured.** A count on a lattice whose pitch is **cited** (Rothemund 2006) and whose
  interhelical distance is **measured** (SAXS). No sheet has been built and no routing here is a sequence design.
- **The count is an UPPER bound on what participates.** The census assumes the raft is rigid along the hinge line,
  so every crossover on it turns through the same angle. `C-0009`'s own result is that **a rigid anchor is carried by
  its two nearest crossovers and essentially nothing else** — 2.3–7.6× concentration — so the realised count runs
  **lower**, never higher. The shear-lag solve that would quantify it is **not run**, and the reason is stated: the
  upper bound already fails.
- **`n k_θ` is the right spring only for a hinge line running ALONG the helices.** Every count reported for a
  transverse line is reported as a count and explicitly **not** as a stiffness.
- **The 32 bp per-interface pitch is the load-bearing premise**, and it is swept: at the 16 bp per-helix reading the
  count is 8, at honeycomb's 21 bp it is 6, at both together 12. **No reading reaches sixteen**, so no verdict here
  moves across the premise — which is unusual in this programme and is stated for that reason.
- **The `L4` reading (six) assumes the flexure's arm still reaches the tile.** A hinge line longer than that belongs
  to `L5`, which is refused on plan area, not on lattice geometry — and that refusal is `T-96`'s to confirm.
- **The fan is a rigid-body model of a raft**: the duplexes are rigid and the interfaces carry all the compliance.
  Its continuum control is quoted beside it and runs 6.00 → 1.47 over the range used.
- **`k_θ` is `C-0009`'s CITED, FITTED constant** (Chen et al., `α ∈ [0.6, 1.2]`). **It does not enter the count at
  all** — the census is pure geometry — and it enters the re-pricing exactly as it enters `C-0034`, where the arm
  goes as `√(n k_θ)`, so an `α` at the bottom of the bracket is equivalent to a hinge count 1.67× lower and moves the
  thresholds **the wrong way**.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT**, not a measurement; Fields et al.'s measured buckling implies 25 % less,
  which lengthens every arm here by 8 % and does not close a 2.5× gap.
- **One flexure per load path and 45 attachments**, exactly as `C-0023`, `C-0025`, `C-0029` and `C-0034` assume.
- **The `L1` reading assigns each flexure an equal share of the tile's plan.** A design that concentrates the
  flexures on fewer, longer hinge lines trades path count against hinge count, and `CH-0029`'s unzip allowable
  fixes the path count from below at 34 — so that trade is bounded and is named as an open item, not explored here.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| crossover spacing per interface | **32 bp** (16 bp per helix) | **CITED**, Rothemund, *Nature* **440**:297 (2006), via `C-0015`. **This is the whole load-bearing premise and it is swept** |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al., *Nature* **459**:414 (2009) |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| base pairs per turn | 10.67 square, 10.5 honeycomb | **CITED** |
| honeycomb crossover spacing | 21 bp | **CITED**, carried only as a sensitivity |
| crossover hinge `k_θ = 2αB/(100a)` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009`. **Does not enter the count** |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement** |
| far anchorage `k_far` | 78.235 pN·nm/rad | `C-0034`'s `A2`, from `C-0029`'s counting theorem at the **cited** 1.0 nm phosphate radius |
| the 45 paths, the 3 × 15 grid, the 49–56 inventory | — | **`C-0015`**, and re-run here rather than tabulated |
| the 40 pN/nm compliance ceiling | — | **`C-0023`** |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |
| `C-0034`'s `E5a16` and its 8-crossover row | 11.028 nm, 33.56/36.78 pN/nm, 9.52 nm | **CITED**, and reproduced here as gate-5 tests |

Everything else — the census and its 32 phases, the topology ladder, the fan composition law and its continuum
control, every re-priced arm, tangent, bond force and threshold, and the two supply-against-demand ledgers — is
**derived here in code**, with `C-0015`'s and `C-0034`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **What the programme's answer to `A8.2` now is at §3's DESIRED stroke.** `C-0029` closed the standoff branch there
   and opened `E5g16`; `C-0037` reopened the standoff branch with a truss; this claim closes `E5g16`/`E5a16` there.
   **`T-98` — which branch Gen-1 should take — is no longer a comparison of two live options.**
2. **Whether fewer, longer flexures beat 45 short ones.** The path count is bounded below at 34 by `CH-0029`'s unzip
   allowable and above by the tile's crossover inventory; a design at 34 paths on longer hinge lines is the one
   trade this claim does not sweep.
3. **The participation solve.** How many crossovers of a long hinge line actually turn with the arm, which
   `C-0009`'s 2.3–7.6× localisation says is fewer than the census counts. Only needed if `L5` is ever admitted.
4. **Whether the fan is the right element after all.** It is buildable, it is 2.08× too soft at 45 paths, and its
   `n_eff` law is derived here for the first time in this programme. At §3's **acceptable** stroke it has not been
   priced at all.
5. **`k_s` and `α`**, unchanged from `C-0034` — they do not touch the count and they do move the re-pricing.

## Challenges

**Raises [`CH-0054`](../challenges/CH-0054-the-sixteen-crossover-hinge-line-does-not-exist.md)** against `C-0023`'s
free `hingeCount`, `C-0029`'s `E5g16` and `C-0034`'s `E5a16`.
**No number in any of the three fails to reproduce** — sixteen reproductions at ≤ `3.8e−9` outside their own rounding.

**None stands against this claim.** The three ways it would fail:

1. **A published single-layer Rothemund sheet with a per-interface crossover spacing below 32 bp.** That is the one
   premise the count rests on; at 16 bp the count is 8, which is still not 16, so this would have to be a *quarter*
   of the cited pitch.
2. **A demonstration that a hinge line may run outside the tile at no plan-area cost** — `L5` — which needs the
   superstructure to be 12.3× the tile's footprint and is `T-96`'s to refuse or to admit.
3. **A hinge motif whose axis runs ACROSS the helices with a characterised stiffness.** Then the transverse line's
   seven crossovers become relevant and the count question is a different one. Nothing in the literature `C-0029`
   surveyed has such a motif, and seven is still not sixteen.
