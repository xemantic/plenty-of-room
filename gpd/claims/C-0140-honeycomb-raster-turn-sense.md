# C-0140 — **A `15 × 4` HONEYCOMB X-RASTER CARRIES BOTH TURN SENSES, SO THERE IS NO UNIFORM HONEYCOMB ROW LENGTH AT ALL.** The cheap bound is a theorem — a honeycomb path can never continue in the same direction, so an x-raster row is **corrugated** and its neighbour-class difference **alternates** — and the scaffold's axial direction alternates with it. **The two alternations cancel WITHIN a row and the `m − 1` row turns break them**: every row interior carries one sense, consecutive rows carry opposite senses, and design (i) ends at **sense 2 on 30 helices and sense 1 on 28**. `C-0119`'s **112 bp** is admissible on 28 of them and `C-0136`'s **119 bp = 40.46 nm** on the other 30. The remedy costs **3 base pairs = 1.02 nm**: at **112 / 108 bp** the tile's axial extent is **116 bp = 39.44 nm**, `−1.40 %` of §3's nominal — **better than the square lattice's `−4.80 %`** — and the reason the built blocks need no such stagger is that **their raster turns are 28 unpaired nucleotides, not crossovers**

| | |
|---|---|
| **Task** | [`T-218`](../tasks/T-218-honeycomb-raster-turn-sense.md), raised by [`C-0136`](C-0136-mixed-domain-phase-and-honeycomb-twist.md) *Still open* item 1 and [`CH-0165`](../challenges/CH-0165-an-integral-scaffold-lattice-is-necessary-not-sufficient.md) *What would settle it* |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (exact integer cross-section geometry and residue arithmetic, asserted over whole periods, whole designs and whole residue families) **+ literature** (five passages of the caDNAno paper grepped directly out of `gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`, already in the repository — **zero fetches**). **No solve, and that is a result rather than an omission** |
| **Verdict** | **PASS on all SIX predicates; NONE of the nine declared falsifiers fired, including `F3`, which was declared open.** The turn sense is **not** constant, `CH-0165`'s second branch obtains, and `C-0119` is **overturned in the reading *"drawable at a uniform width"*** while every other result of `C-0119` is reproduced here at departure `0.0`. Raises [`CH-0172`](../challenges/CH-0172-a-honeycomb-x-raster-carries-both-turn-senses.md) and [`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** Every number is integer lattice arithmetic on caDNAno's published design rules; the cross-section itself is above that — designed, folded, gel-analysed and imaged by others — and this claim reports their accounting rather than demonstrating anything |
| **Provenance** | [`gpd/results/T-218-honeycomb-raster-turn-sense.json`](../results/T-218-honeycomb-raster-turn-sense.json) (`structure.HoneycombRasterTurnSenseStudyKt`, **new**); model in `src/main/kotlin/structure/HoneycombRasterTurnSense.kt` (**new file** — `LatticePhaseCensus.kt` was **read, not edited**, and its `HelixCrossoverLattice` supplies every residue used here); **22 gate-named tests** in `src/test/kotlin/structure/HoneycombRasterTurnSenseTest.kt`, **written first and watched fail** (the whole suite failed to compile against the absent model, then failed on two real defects: an azimuth guard written on multiples of 60° where the honeycomb's bonds are the **odd** multiples of 30°, and a `single()` on a two-element list); **58 turn records, 8 design records, 33 width records, 3 loop records, 7 reproductions at departure `0.0`, 6 predicates, 9 falsifiers, 10 findings**; result file **BYTE-IDENTICAL across two independent JVM runs**; `tools/verify.sh` **BUILD SUCCESSFUL in 22 m 22 s** on its own isolated tree — the whole suite, **no `--drop-file` needed** with two concurrent agents' new sources in it — and `tools/check-kotlin-format-strings.py`, `tools/check-result-file-hygiene.py`, `tools/check-markdown-tables.py` (0 defects over 441 files), `tools/check-challenge-index.py`, `tools/check-corpus-links.py` and `tools/result-reader-census.py --check` (114 studies, 112 direct + 27 transitive edges) all clean. The one broken link `verify.sh` reports is a concurrent agent's in-progress challenge pointing at a claim not yet written, and is in neither of this claim's files |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; rise **0.34 nm/bp**; caDNAno **honeycomb** — three azimuths at **7 bp**, azimuth period **21 bp = 2 turns**, scaffold crossovers **5 bp** upstream or downstream of the staple lattice; B-DNA at **10.5 bp/turn**, which is the honeycomb's own design twist (`C-0136`); cross-section **design (i)** of Douglas et al., **15 x-raster rows of 4 helices, 60 duplexes**; §3's nominal tile width **40.0 nm** |
| **Consumes** | [`C-0136`](C-0136-mixed-domain-phase-and-honeycomb-twist.md) (`HelixCrossoverLattice`, `turnPairResidues`, and both residue triples **read from its result file and reproduced**), [`C-0119`](C-0119-honeycomb-raster-width.md) (the honeycomb design rules, **read from its result file and reproduced**), [`C-0109`](C-0109-four-layer-tile.md) (the 15 × 112 × 4 tile and its scaffold budget), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the square-lattice control, **re-derived on this task's own machinery, not transcribed**), [`C-0133`](C-0133-twist-corrected-raster-row.md) (the 37.40 nm twist-corrected width, for scale), `Gen1Tile` (the rise) |
| **Raises** | [`CH-0172`](../challenges/CH-0172-a-honeycomb-x-raster-carries-both-turn-senses.md) against `C-0119`'s *"drawable"* and `C-0136`'s 40.46 nm and `Δ = 1` width cell, [`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md) against `C-0119`'s scaffold budget and its scaffold identification |

---

## The claim, in one line

**The honeycomb's admissible-width rule needs a turn sense, an x-raster does not have one, and what
it costs is three base pairs — not the tile.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, angles **degrees**; rise **0.34 nm/bp**; `k_BT = 4.141947 pN·nm` at 300 K.
- The cross-section is an **exact integer** lattice: a cell `(x, y)` sits at `(x·d√3/2, y·d/2)`, so
  every honeycomb site is an integer pair and every bond one of six integer offsets. Nothing here
  rounds a lattice position.
- Sublattice **A** is `y ≡ 0 (mod 3)` with `x − y/3` even, bonds at `90° / 210° / 330°`;
  sublattice **B** is `y ≡ 2 (mod 3)` with `x − (y−2)/3` even, bonds at `270° / 30° / 150°`.
- All helices are parallel to a **global** `z`, positions in base pairs from one common origin
  plane. **B-DNA is right-handed**, so viewed from `+z` the backbone azimuth increases
  counter-clockwise with `z`; one azimuth step (`+7 bp`) advances it by `+240° ≡ −120°`, and
  **neighbour class increases as the neighbour azimuth decreases by 120°**.
- `Δ_geom` is `(j(β) − j(α)) mod 3` for the neighbour `α` the scaffold **arrives from** and the
  neighbour `β` it **leaves to** — a property of the cross-section alone.
- `s = +1` where the scaffold traverses a helix in `+z`. A raster runs the full length of every
  helix, so `s` **alternates** along the path.
- `Δ_eff = (s·Δ_geom) mod 3` is the sense that enters `N ≡ 7Δ + {0, 10, 11} (mod 21)`, because a
  row **length** is `|z_out − z_in|` and is positive by construction. `C-0136` writes the formula
  without this sign; the sign is what this task supplies.

---

## Deliverable 1 — the cheap bound, and it is a theorem rather than a reading

**A honeycomb path can never continue in the same direction.** From an A site the three bonds are
`(0,+2)`, `(−1,−1)`, `(+1,−1)`; from a B site `(0,−2)`, `(+1,+1)`, `(−1,+1)`. No offset appears in
both lists, so no chain of three helices is straight and **every** x-raster row is corrugated —
at every lattice, every width and every design. The caDNAno paper states the same fact of its own
blocks:

> *"The x-raster rows within the honeycomb framework are **corrugated**; they **stagger up and
> down** and encompass helices that are actually at **two different y-positions**."*

Consecutive helices of a row are therefore on **opposite sublattices**, and `Δ_geom` alternates.
**`F1` did not fire** — all 59 consecutive pairs of design (i) alternate A/B — and **`F2` did not
fire**: the geometric sequence is

```
1222211112222111122221111222211112222111122221111222211112
```

blocks of four, period 8.

**And one lattice fact falls out for free: every x-raster row must have an EVEN number of helices.**
A row's two ends must both carry the **downward** vertical bond, and that bond points up on one
sublattice and down on the other. The paper folded seven designs with `n = 4, 6, 8, 10, 16, 20, 30`
— every one even.

---

## Deliverable 2 — the two alternations, composed rather than assumed

`CLAUDE.md` records that *"two independent sign alternations can CANCEL"*, and that the discipline
is to **compose them before choosing**. Composed:

| helix | `Δ_geom` | `s` | `Δ_eff` |
|---|---|---|---|
| row interior | alternates 1, 2, 1, 2 … | alternates `+, −` | **constant** |
| row turn (3 helices) | three equal values | still alternates | **breaks** |

> **`Δ_eff` is constant along a row interior, consecutive rows carry opposite senses, and each of
> the `m − 1` row turns contaminates three helices.** On design (i):
>
> ```
> 2212112122121121221211212212112122121121221211212212112122
> ```
>
> **sense 2 on 30 helices and sense 1 on 28**, of the 58 with a defined sense — the two path ends
> carry one raster crossover each and have none. **`F3` did not fire**, and it was the falsifier
> declared open.

**The answer carries no convention.** Mirroring the cross-section (`x → −x`) and flipping which
face the scaffold starts at each swap the two **labels** one for one — `1 ↔ 2` at every helix — and
leave both senses present. **`F5` did not fire.**

**The square-lattice control is what makes this a derivation.** Run on the same code, Rothemund's
single-layer sheet — whose two in-plane neighbours are 180° apart, i.e. **two** azimuth classes —
returns a **constant** sense and `N ≡ 16 (mod 32)`, `C-0086`'s rule exactly. **`F4` did not fire.**
And the reason is arithmetic:

> **`2` is its own negative modulo 4, and neither `1` nor `2` is self-inverse modulo 3.** The
> square lattice's unconditionality is an accident of `4 = 2 × 2`; the honeycomb's three azimuths
> cannot have it.

---

## Deliverable 3 — the census over every cross-section the paper folded

| design | helices | sense 1 | sense 2 | minority | constant? |
|---|---|---|---|---|---|
| **15 × 4 — design (i), this programme's tile** | 60 | **28** | **30** | 0.483 | **no** |
| 10 × 6 — design (ii), the paper's own recommendation | 60 | 29 | 29 | 0.500 | no |
| 8 × 8 | 64 | 31 | 31 | 0.500 | no |
| 6 × 10 | 60 | 29 | 29 | 0.500 | no |
| 4 × 16 | 64 | 31 | 31 | 0.500 | no |
| 3 × 20 | 60 | 20 | 38 | 0.345 | no |
| 2 × 30 | 60 | 29 | 29 | 0.500 | no |
| **1 × 60 — one row, no row turn** | 60 | 0 | **58** | 0.000 | **YES** |

**The only constant-sense raster has one row**, and one corrugated row spans two `y` positions —
**two layers, not four**. So the escape exists and is not available to the tile `C-0119`
recommends. `C-0136`'s *"a raster carrying both senses has no admissible row length at any width"*
is upheld at **7 of the 7 built cross-sections**.

---

## Deliverable 4 — what survives of 112 bp and of 119 bp

The two residue triples are disjoint (**`F6` did not fire**) and **0 of 2 100** candidate widths
serve both senses (**`F8` did not fire**). So:

| width | residue mod 21 | admissible at | on design (i) |
|---|---|---|---|
| `C-0119`'s **112 bp** = 38.08 nm | **7** | sense 1 only | **28 of 58 helices** |
| `C-0136`'s **119 bp** = 40.46 nm | **14** | sense 2 only | **30 of 58 helices** |

**Neither is a uniform tile width, and `C-0136`'s `+1.15 %` is withdrawn as one.**

**The remedy is a two-length raster and the minimum stagger is exactly 3 bp = 1.02 nm** — residue 7
against 4, and 17 against 14 (**`F9` did not fire**). Assigning the two lengths over the real
60-helix path and solving for the crossover levels:

| sense 1 / sense 2 | stagger | axial extent | departure from 40.0 nm | scaffold | fits M13 | faces ragged |
|---|---|---|---|---|---|---|
| 101 / 109 bp | 8 bp | 117 bp = 39.78 nm | **`−0.55 %`** | 6 308 nt | yes | 8 / 16 bp |
| 102 / 109 bp | 7 bp | 116 bp = 39.44 nm | `−1.40 %` | 6 337 nt | yes | 7 / 14 bp |
| **112 / 108 bp** | **4 bp** | **116 bp = 39.44 nm** | **`−1.40 %`** | **6 596 nt** | **yes** | **4 / 8 bp** |
| **112 / 109 bp** | **3 bp** | 115 bp = 39.10 nm | `−2.25 %` | 6 627 nt | yes | **3 / 6 bp** |
| 122 / 119 bp | 3 bp | 125 bp = 42.50 nm | `+6.25 %` | 7 227 nt | yes | 3 / 6 bp |

Read on a stated rule — **minimum `|extent − 40 nm|` among pairs with a stagger of at most 4 bp
that fit M13** — the recommendation is **112 / 108 bp**, and its `−1.40 %` **beats the square
lattice's 38.08 nm at `−4.80 %` and `C-0133`'s twist-corrected 37.40 nm at `−6.50 %`.** The
honeycomb's width advantage survives; what it costs is a **4 bp ragged front face and an 8 bp
ragged rear one**, 1.36 and 2.72 nm.

**The tile's extent is not the mean of the two row lengths.** The crossover levels drift and
return — the net displacement over one 8-helix period is **exactly zero**, which is why the block
stays bounded — but the excursion is real, and it is the extent that a §3 dimension is owed.

---

## Deliverable 5 — why the built blocks need none of this

The paper's own accounting:

> *"Each helix was allotted **126 bases** of scaffold. Of those 126 bases, **98 were paired** with
> complementary staples, and the remaining **28 bases** were divided into **front and rear unpaired
> loop fragments at the ends of each helix**."*

**So every raster turn of the built blocks passes through `14 + 14 = 28` unpaired nucleotides**,
which is not an antiparallel crossover and which the residue condition does not bind at all. That
is how a block whose turn sense alternates is folded with all sixty helices at one length, and the
paper's TEM criterion names the loops as a visible feature — *"more than 3 nm away from the
**unpaired scaffold loops at the front and rear interfaces**"*.

**The arithmetic is exact and it identifies the scaffold** (**`F7` did not fire**):

&nbsp;&nbsp;&nbsp;&nbsp;`60 × 126 = 7 560` and `64 × 126 = 8 064`.

`15 × 4` is **60** helices, so design (i) is a **p7560** design that spends its scaffold **to the
last nucleotide**; the 64-helix designs are (iii) `8 × 8` and (v) `4 × 16`. That is
[`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md) item 2.

**And the loops cost more than this programme's tile has** — item 1:

| scaffold | 60 helices at the built 28 nt allowance | widest paired row |
|---|---|---|
| M13mp18, 7 249 nt | needs 8 400 nt at 112 bp — **1 151 short** | **92 bp = 31.28 nm** (`−21.80 %`) |
| p7560, 7 560 nt | — | 98 bp = 33.32 nm (`−16.70 %`), **the paper's own 98** |
| p8064, 8 064 nt | **336 short** | 106 bp = 36.04 nm (`−9.90 %`) |

That p7560's ceiling is **exactly the 98 bp the paper paired** is the check that the accounting is
being read correctly.

**The two routes are complementary and the ragged one is cheaper.** Route A (all crossovers, two
lengths) fits M13 with **653 nt spare** at 112 / 108 bp; route B (uniform, loops) does not fit M13
at 112 bp at all. Nothing here measures the **minimum** loop a turn needs, and that is the open
question this claim hands on.

---

## The five verification gates

Executed as **22 gate-named tests** in `src/test/kotlin/structure/HoneycombRasterTurnSenseTest.kt`,
written before the model and watched fail.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a cross-section carries azimuths and no length; the six bonds land on the **odd** multiples of 30°; an off-lattice site, an off-bond offset, an odd row, a zero axial sign and a non-lattice azimuth pair all throw | **PASS** |
| **2 — limiting cases** | **the cheap bound**: no honeycomb bond can be followed by itself, at both sublattices and all six bonds; the 15 × 4 path is 60 distinct cells and a valid honeycomb path; every row spans exactly **two** `y` positions and four `x` positions; consecutive helices are on opposite sublattices; every folded design has an **even** row | **PASS** |
| **3 — symmetry and reproduction** | `Δ_geom` alternates in blocks of four, with period 8 asserted over the whole path; `Δ_eff` is **not** constant, both senses occur, and the counts are 30 and 28; a **row interior** carries one sense and consecutive rows carry opposite ones, at all 15 rows; a **one-row** raster is constant; mirroring the cross-section and flipping the first axial sign each map `Δ_eff → 3 − Δ_eff` at **every** helix; a two-length raster's crossover levels drift **exactly zero** over one 8-helix period, at four candidate pairs; the square-sheet control returns a constant sense and `C-0086`'s `N ≡ 16 (mod 32)`; the two honeycomb triples are `{7,17,18}` and `{3,4,14}` and disjoint | **PASS** |
| **4 — exactness over families** | no uniform row length serves both senses, over 2 100 widths; the minimum stagger is **3**, and the square lattice's self-inverse control is **0**; the alternation holds at all seven folded cross-sections; `126 ≡ 0 (mod 21)`, `60 × 126 = 7 560`, `64 × 126 = 8 064`, `98 ≡ 14 (mod 21)`. **There is no mesh and no sampling: the task is exact integer arithmetic and the convergence gate is discharged as exhaustion over whole periods** | **PASS** |
| **5 — literature and upstream** | **seven reproductions at departure `0.0`**: `C-0136`'s two residue triples from its result file; `C-0119`'s azimuth step, azimuth period and scaffold offset from its result file; the paper's own per-helix allotment against both of its scaffolds. `C-0119`'s 112 bp and `C-0136`'s 119 bp checked against both triples; **the `Δ = 1` nearest width re-derived as 122 bp, against the 112 bp `C-0136`'s table carries and the 122 its own result file carries**. Five passages read directly from PMC2731887 in `gpd/data/T-151-sources/` — **zero fetches** | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | consecutive helices of an x-raster row are on the **same** sublattice | **no** | all 59 pairs alternate; it is forced by the lattice |
| **F2** | `Δ_geom` is constant along the raster | **no** | blocks of four, period 8 |
| **F3** | the two alternations **cancel**, so a uniform row length exists — *declared open* | **no** | 30 against 28; they cancel **within** a row and the `m − 1` row turns break them |
| **F4** | the square-sheet control fails to reproduce `C-0086` | **no** | constant at 2, `N ≡ 16 (mod 32)` |
| **F5** | the alternation verdict changes under the free viewing convention | **no** | mirroring swaps the labels one for one |
| **F6** | the two residue triples are not disjoint | **no** | `{7,17,18} ∩ {3,4,14} = ∅` |
| **F7** | the per-helix allotment does not reproduce the paper's two scaffold lengths | **no** | `60 × 126 = 7 560`, `64 × 126 = 8 064`, both exact |
| **F8** | some uniform row length serves both senses | **no** | 0 of 2 100 |
| **F9** | the minimum stagger is not 3 bp | **no** | 3 bp = 1.02 nm |

---

## Still open — named, not answered

1. **The minimum unpaired slack a honeycomb raster turn needs.** The built blocks use 28 nt per
   turn; this claim shows a **zero**-slack route exists at a 3 bp stagger, and nothing here says
   what lies between, or what a short loop costs in yield.
2. **What a 4 bp ragged face costs the tile.** §3 asks for a flat tile and a stated width; a
   4/8 bp raggedness is 1.36/2.72 nm of front and rear surface relief, and no flatness model in
   this repository reads a honeycomb lattice (`C-0136`'s own open item 2, unchanged).
3. **Whether some other scaffold route on the honeycomb has a constant sense.** Only the
   **x-raster** is examined here, and only under caDNAno's *"the path of the scaffold stays within
   a 2D surface"* default. `C-0119` records that caDNAno permits forced crossovers off that
   surface, and that route is not explored.
4. **The `10 × 6` cross-section is the paper's own recommendation and carries the same alternation
   (29 / 29).** Whether the tile should move to it is `T-199`'s question and is untouched here.
5. **Nothing here re-reads `C-0126`'s four-layer flatness**, `C-0109`'s rigidity or `C-0116`'s
   threshold. The tile remains buildable; what changes is that it is buildable at **two** row
   lengths.
