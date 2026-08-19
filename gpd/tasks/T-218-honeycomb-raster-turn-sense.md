# `T-218` — which turn sense `Δ` does a caDNAno `15 × 4` honeycomb x-raster carry?

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0136`](../claims/C-0136-mixed-domain-phase-and-honeycomb-twist.md) *Still open* item 1, and [`CH-0165`](../challenges/CH-0165-an-integral-scaffold-lattice-is-necessary-not-sufficient.md) *What would settle it* |
| **Verification type** | **logical** (exact integer lattice geometry and residue arithmetic, asserted over whole periods) **+ literature** (the caDNAno cross-section sentence and the per-helix scaffold allotment, read directly out of `gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`, already in the repository — **zero fetches**) |
| **Units** | lengths **nm**, angles **degrees**, rise **0.34 nm/bp**, `k_BT = 4.141947 pN·nm` at 300 K |

---

## Formulate

`C-0136`/`CH-0165` derive the honeycomb's admissible raster row lengths as

&nbsp;&nbsp;&nbsp;&nbsp;`N ≡ 7Δ + {0, 10, 11} (mod 21)`, `Δ = (b − a) mod 3 ≠ 0`,

with the two turn senses **disjoint** — `{7, 17, 18}` at `Δ = 1` against `{3, 4, 14}` at `Δ = 2`.
`C-0119`'s own 112 bp row has residue **7**.
So the question *"which `Δ` does the built cross-section carry"* decides
whether 112 bp is admissible at all, and whether **119 bp = 40.46 nm** — the closest any lattice in
this corpus gets to §3's nominal 40.0 nm, `+1.15 %` — is available.

**Numeric target.** The sequence `Δ_k` over the 60 helices of the `15 × 4` x-raster, and the
resulting admissible row-length residue set.

**Acceptance predicates.**

- `P1` — the cross-section geometry is **read**, not assumed: the sublattice sequence along an
  x-raster row follows from a sentence of the primary source rather than from a choice made here.
- `P2` — the turn-sense sequence is emitted per helix for all 60 helices of design (i).
- `P3` — the answer is invariant under the one convention that is free (which face the
  cross-section is viewed from), or the dependence is stated.
- `P4` — the same machinery, run on the **square** sheet, reproduces `C-0086`'s unconditional rule;
  a construction that cannot do that is not evidence about the honeycomb.
- `P5` — the consequence for `C-0119`'s 112 bp and for 119 bp is stated as **admissible /
  inadmissible / admissible on a stated fraction of the helices**, not as a verdict on the lattice.
- `P6` — if no uniform row length exists, the **cheapest departure** from uniformity that restores
  admissibility is quoted, in base pairs and nm.

**Geometry and sign conventions, fixed before deriving.**

- All helices are parallel to a **global** `z`, positions in base pairs from one common origin
  plane. B-DNA is **right-handed**: viewed from `+z`, the backbone azimuth **increases**
  counter-clockwise with increasing `z`, at the lattice's own `720/21 = 34.2857 °/bp`.
- Consequently one azimuth step, `+7 bp`, advances the azimuth by `+240° ≡ −120°`, so **neighbour
  class `j` increases as the neighbour azimuth decreases by 120°**.
- `Δ_geom` at a helix is `(j(β) − j(α)) mod 3` for the neighbour `α` the scaffold **arrives from**
  and the neighbour `β` it **leaves to** — a property of the cross-section alone.
- `s_k = +1` if the scaffold traverses helix `k` in `+z` and `−1` otherwise. A raster runs the full
  length of every helix, so `s` **alternates** along the path.
- `Δ_eff = (s_k · Δ_geom,k) mod 3` is the sense that enters the residue formula, because the row
  **length** is `|z_out − z_in|` and is positive by construction.

---

## Plan

**The cheap bound runs first and it is pure arithmetic — no solve is justified at all.**
This repository's lattice machinery is single-layer square-lattice throughout
(`CLAUDE.md`: *"`OrigamiGrillage` never reads `layers`"*), so a solved number here would be a
square-lattice number wearing a honeycomb label. Three steps, each `O(60)`:

1. **Read the cross-section.** The caDNAno paper states the x-raster row geometry in one sentence.
   Whatever it says about *corrugation* fixes the sublattice sequence and therefore `Δ_geom`.
2. **Compose the two alternations.** `Δ_geom` (a cross-section property) and `s` (a path property)
   are independent; `CLAUDE.md` records that two independent sign alternations can **cancel**, and
   that composing them before choosing is the discipline. Compose, do not assume.
3. **Intersect with `CH-0165`'s residue triples** and read off what survives.

**Cost.** Minutes. The alternative — building a honeycomb grillage — is weeks and would answer a
different question.

**What would falsify this approach.** If the built design's raster turns are **not** antiparallel
scaffold crossovers at all, the residue condition does not bind and the whole construction is
answering a question the object does not ask. That is checkable in the same source, from the
per-helix scaffold allotment, and it is checked here.

### Declared falsifiers

| # | statement | must |
|---|---|---|
| `F1` | consecutive helices of an x-raster row are on the **same** honeycomb sublattice | not fire |
| `F2` | `Δ_geom` is **constant** along the raster | not fire |
| `F3` | the two alternations **cancel**, so `Δ_eff` is constant and a uniform row length exists | *open* |
| `F4` | the same machinery on the **square** sheet gives a non-constant `Δ_eff`, i.e. fails to reproduce `C-0086` | not fire |
| `F5` | the alternation verdict **changes** under the free viewing convention (mirroring the cross-section) | not fire |
| `F6` | the two honeycomb residue triples are **not** disjoint, contradicting `C-0136` | not fire |
| `F7` | the paper's own per-helix scaffold allotment does not reproduce its scaffold lengths | not fire |
| `F8` | some uniform residue satisfies **both** turn senses | not fire |
| `F9` | the minimum admissible row-length stagger is not the value derived here | not fire |

---

## Execute

`src/main/kotlin/structure/HoneycombRasterTurnSense.kt` (model, exact integer lattice) and
`structure/HoneycombRasterTurnSenseStudy.kt` (the study), tests first in
`src/test/kotlin/structure/HoneycombRasterTurnSenseTest.kt`.
Result: `gpd/results/T-218-honeycomb-raster-turn-sense.json`.
