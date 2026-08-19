# C-0119 — **YES, and the cross-section is not a proposal: it is design (i) of the caDNAno paper, folded from p8064 and one of only THREE of seven to produce sharp leading monomer bands.** The tile this programme now recommends is **15 × 4** in Douglas et al.'s own nomenclature — 15 x-raster rows of 4 helices, **60** duplexes — and their conclusion is that **10 × 6 yields the greatest fraction of defect-free objects**, so the tile's aspect ratio is a design variable with **published yield evidence** that this programme has never treated as one. `C-0086`'s odd-half-turn rule genuinely does not transfer (no odd multiple of 5.25 bp is an integer) — but the honeycomb quantises its half turn to **5 bp**, so the scaffold lattice **is** integral and the rule's failure was a **domain error, not a prohibition**. A seam is still forced, and for a reason in the source rather than the geometry: *"the path of the scaffold stays within a 2D surface"*

> **Annotated, iteration 34 ([`C-0140`](C-0140-honeycomb-raster-turn-sense.md), [`CH-0172`](../challenges/CH-0172-a-honeycomb-x-raster-carries-both-turn-senses.md), [`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md), [`CH-0180`](../challenges/CH-0180-the-scaffold-pairing-contradicts-its-own-paper.md), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **THREE READINGS ARE WITHDRAWN AND THE INTEGRALITY RESULT IS NOT.**
> (1) *"Drawable"* read as *"at a uniform row length"* falls: an x-raster is corrugated, so its turn sense **alternates**,
> and design (i) ends at sense 2 on 30 helices and sense 1 on 28 — the 112 bp row is admissible on **28 of 58** of them.
> The remedy is a two-length raster at **112 / 108 bp**, 116 bp = 39.44 nm of axial extent.
> (2) The scaffold budget has **no line for the 28 nt of unpaired turn loop** the only folded instance of this cross-section uses.
> (3) *"p8064 — designs i, iii, v, including ours"* is read from a Methods sentence that **contradicts the paper's own main text**
> at exactly this design and no other; the main text's rule (60 helices → 7 560, 64 → 8 064) makes design (i) a **p7560** design.
> **The `7k ± 5` integrality result, the seam, the yield reading and the cross-section identification are untouched and reproduce at departure `0.0`.**
> The *"every plan ceiling, station lattice, crossover phase and placement in this corpus is single-layer square-lattice"* item below is **DISCHARGED** by `C-0141`.

| | |
|---|---|
| **Task** | [`T-198`](../tasks/T-198-honeycomb-raster-width.md) — can four honeycomb layers be rastered from one circular M13 at a buildable width? |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (integer-lattice arithmetic; a parity brute-forced at every order 3–7 and a theorem beyond) **+ literature** (the primary honeycomb design rules, **read directly**) |
| **Maturity** | **TRL 1–3** for everything derived here. **The cross-section itself is above that** — it has been designed, folded, gel-analysed and imaged by others, and this claim reports their result rather than demonstrating anything. |
| **Verdict** | **PASS on all four predicates; the declared falsifier did NOT fire.** The honeycomb scaffold-crossover lattice is **integral** (`7k ± 5` bp), so the four-layer tile is drawable. A **seam is forced** by the same tree-parity that forces Rothemund's, because the scaffold's usable adjacency is a **path** even though the honeycomb's is three-regular. `C-0109`'s budget reproduces exactly — 6 720 of 7 249 nt, 529 remainder, **92.7 %**. |
| **Provenance** | [`gpd/results/T-198-honeycomb-raster-width.json`](../results/T-198-honeycomb-raster-width.json), emitted by the retained [`tools/T-198-honeycomb-raster.py`](../../tools/T-198-honeycomb-raster.py) (**29** self-tests, `--selftest`). Primary source [`gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`](../data/T-151-sources/), **already in the repository**. |
| **Conditions** | The corpus at iteration 25. Honeycomb lattice at 10.5 bp/turn; the tile is 15 rows × 4 layers × 112 bp. |
| **Consumes** | [`C-0109`](C-0109-four-layer-tile.md) (the tile and the count it left open), [`C-0116`](C-0116-composite-fraction-threshold.md) (the threshold, unaffected), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the square-lattice width rule this one does **not** transfer) |
| **Constrains** | Nothing numerically. **No claim is contradicted and no challenge is raised** — `C-0086`'s rule is correct *on its own lattice* and is not being overturned. What this adds is a design variable and a published yield ordering. |

---

## 1. The cheap bound, and it was right without being the answer

| | square lattice | honeycomb |
|---|---|---|
| bp per turn | 10.67 | **10.5** |
| half turn | **16 bp** | **5.25 bp** |
| odd multiples | 16, 48, 80, 112, 144 — all integers | **none is ever an integer** |

So `C-0086`'s rule admits **no** honeycomb row length at all. That is a real result and it is **not** a
prohibition — it means the rule is **outside its own domain**, which are opposite conclusions and the cheap
bound cannot tell them apart. Reading the primary source can.

---

## 2. The primary rules, read directly — and the fourth time the corpus already had them

Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, *Nucleic Acids Research* **37**:5001 (2009),
PMC2731887 — the caDNAno paper — **already in this repository**, fetched by `T-151` two iterations ago for a
different question. The whole literature answer was four `grep`s and **zero** fetches.

> *"antiparallel crossovers between adjacent staple helices only where the strand backbones arrive at points
> of closest proximity, which repeat every **21 base pairs** if the helical twist is fixed at **10.5 base
> pairs per turn**. Thus for a given staple helix, potential staple-crossover positions occur every **seven
> base pairs**, or two-thirds of a turn. Our default rules allow antiparallel crossovers between adjacent
> **scaffold** helices to occur **five base pairs**, or half a turn, upstream or downstream of allowed
> crossover positions for the associated staple helices."*

**The honeycomb quantises its half turn to 5 bp, not 5.25.** So the scaffold-crossover lattice is `7k ± 5`,
which on a 112 bp row is `{2, 5, 9, 12, 16, 19, 23, …}` — **integral**, and the routing question is open again
rather than closed.

---

## 3. The cross-section has been built, and the paper recommends a different one

Figure 2's caption fixes the nomenclature: *"m × n, where m is the number of x-raster rows, and n is the
number of helices per x-raster row."* Seven designs, differing in cross-section:

| design | helices | | our tile |
|---|---|---|---|
| (i) **15 × 4** | 60 | folded from **p8064**, sharp leading monomer band | **this one** |
| (ii) **10 × 6** | 60 | **the greatest fraction of defect-free objects** | |
| (iii) 8 × 8 | **64** | | |
| (iv) 6 × 10 | 60 | | |
| (v) 4 × 16 | **64** | | |
| (vi) 3 × 20 | 60 | | |
| (vii) 2 × 30 | 60 | sharp band, folded at 15 mM MgCl₂ rather than 22 | |

**Five of the seven are 60 helices and two are 64**, so the family is *not* at constant scaffold length —
which is why the paper folds them from **two** scaffolds, p7560 and p8064. **The comparison this claim rests
on, 15 × 4 against 10 × 6, is at 60 helices for both**, so the scaffold budget genuinely is unchanged between
them.

> *"Only folding with **three of the seven** designs — four-helix-per-x-raster or **15 × 4** (two y-layers),
> six-helix-per-x-raster or 10 × 6 (three y-layers), thirty-helix-per-x-raster or 2 × 30 (two x-layers) —
> produced sharp leading monomer bands by agarose-gel electrophoresis."*

**So the tile this programme recommends is a published, folded, gel-verified cross-section** — and it is not
the one its own source recommends. The stated trend is mechanistic rather than incidental:

> *"designs with a smaller number of x-layers or y-layers may have a folding advantage due to fewer numbers of
> highly embedded helices … and perhaps also due to the lower crossover densities. Consistent with this trend,
> single-layer shapes fold much faster and to high[er yield]."*

**The tile's aspect ratio is therefore a design variable with published yield evidence attached, and this
programme has never treated it as one.** `T-199` is queued for it: 10 × 6 is also 60 helices, so the scaffold
budget is unchanged, but six layers of ten rows is a different plate — thicker, narrower, different second
moment — and it may be a better tile than the one now recommended.

---

## 4. The seam survives the move to three dimensions, for a reason in the source

`CLAUDE.md` records that a seam is *a parity on a tree*: crossovers join only adjacent duplexes, a single-layer
sheet's row-adjacency graph is a **path**, a closed walk on a tree traverses every edge an **even** number of
times, so a fully folded circular scaffold gives every row **two** segments.

A honeycomb helix has **three** neighbours, so that graph has cycles and the argument should not survive.
**It does**, and Figure 2b says why:

> *"Scaffold crossovers only occur between helices that are **neighbors in the partially folded models**.
> Thus, these models capture an important feature of the design: **the path of the scaffold stays within a 2D
> surface**."*

The graph the **scaffold** may use is not the honeycomb's three-regular adjacency — it is the adjacency of an
unrolled 2D surface, which for an `m × n` raster is a **path** again. The tree parity applies unchanged.

**Brute-forced rather than asserted**, at every order from 3 to 7: a covering closed walk exists, **every edge
is traversed an even number of times**, there is **no Hamiltonian cycle**, and there are **exactly two**
Hamiltonian paths. Beyond order 9 the routine **refuses** — a 60-vertex call is 59! permutations, and that
guard exists because the call was made while this task was written and a factorial does not announce itself.
The 60-helix case is a theorem, not an enumeration.

**And the published blocks show it.** The paper's own TEM criterion excludes defects *"more than 3 nm away
from the **unpaired scaffold loops at the front and rear interfaces**"* — the raster turns leave loops, and
the built objects have them.

---

## 5. The scaffold budget, re-checked against what this cross-section is actually folded from

| scaffold | nt | remainder | occupancy |
|---|---|---|---|
| M13mp18, `C-0109`'s figure | 7 249 | 529 | **92.7 %** |
| p7560 — designs ii, iv, vi, vii | 7 560 | 840 | 88.9 % |
| **p8064 — designs i, iii, v, including ours** | **8 064** | **1 344** | 83.3 % |

`C-0109`'s arithmetic reproduces exactly. **But the scaffolds this cross-section is actually folded from are
longer**: p7560 and p8064 are M13mp18 derivatives bearing inserts, and design (i) used p8064. NDI's answer —
*"M13, circular ~7–8 K nucleotides"* — already names that range, so nothing is contradicted; what changes is
that the remainder is **1 344 nt rather than 529** on the standard scaffold for this design, which is
`T-195`'s question on a body 2.5× larger than it assumed.

---

## 6. Validity range, and what this does NOT establish

- **The 2D-surface premise is the paper's statement about its OWN default rules**, and caDNAno explicitly
  permits departure: *"caDNAno permits the user to force crossovers between any two staple bases or between
  any two scaffold bases"*, with the warning that *"departure from the default rules may lead to folding
  failure"*. A forced-crossover route could use the third neighbour and break the path. **This task does not
  explore that**, and it is the one route by which the seam could be avoided.
- **The parity is exhaustive to 7 and a theorem beyond.** The 60-helix case is not enumerated and does not
  need to be.
- **The yield ordering is from the TEXT.** Figure 2d/2e give the fractions as **images**, which are not
  transcribed here — only the ordering and the named winner, both stated in prose.
- **Every plan ceiling, station lattice, crossover phase and placement in this corpus is single-layer
  square-lattice.** The honeycomb's three azimuths at 7 bp are a different inventory and nothing here
  re-derives them.
- **This settles the route and the width. It re-derives no rigidity, no threshold and no flatness number** —
  `C-0109` and `C-0116` are untouched.
