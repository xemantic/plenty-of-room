# C-0118 — **THE FIRST COUPLED TILE IN THIS PROGRAMME'S HISTORY THAT IS FLAT AT THE 90th PERCENTILE UNDER THE MEASURED FOLDING STATISTICS — and what delivers it is the CROSS-SECTION, not the coupling.** Of 16 graded cells, **9 are flat**; on `10 × 6` **all eight** are, best **0.0278431488** at the *sparsest* coupling of all (one column, ten paths, equal springs), and on `15 × 4` **one of eight** is, at **0.0882933461**. The cross-section is worth **3.17109774×** on this statistic, against what the distribution axis buys inside either one. And `C-0109`'s standing comparison is re-framed rather than contradicted: **the uncoupled tile is a reference, never a design**, because §3 requires 100 pN to reach a load and `C-0017`'s mandate is an **equality on the SUM**

> **Annotated, iteration 34 ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **Two things move and they run opposite ways.**
> The sixteen cells are graded on a cross-section that is not a honeycomb (`edgeY` 1.5× too small at every `m`),
> ~~so the grading is owed a re-run — that is [`T-232`](../../TASKS.md), and **no 90th-percentile number here is re-derived**.~~
> **[`T-232`](../tasks/T-232-coupled-cells-at-the-honeycomb-cross-section.md) has now run it ([`C-0142`](C-0142-coupled-cells-at-the-honeycomb-cross-section.md), [`CH-0176`](../challenges/CH-0176-the-first-flat-coupled-tile-is-flat-at-half-its-cells.md)), and EVERY 90th-percentile number in this claim's title, §2 and §3 is superseded:**
> **4** of 16 cells are flat, not 9; `10 × 6` is **4 of 8**, not eight of eight; `15 × 4` is **0 of 8** — at `f = 0.30` **and** at the measured band's low `f = 0.26` — so its single flat cell, 0.0882933461, is **0.145354102** and fails.
> The best cell is **0.0680677948** (still `10 × 6`, one column, ten paths, equal springs) and the cross-section's worth is **2.13543134×**, not 3.17109774×.
> Every one of the sixteen numbers below reproduces at `1.4e−10 … 4.4e−9` in the process that produced the corrected ones, so **the movement is the geometry and nothing else** —
> and this claim's *conclusion*, that what delivers flatness is the cross-section rather than the coupling, is **strengthened**: `15 × 4` now has no coupled design at all.
> §2's *"More paths are monotonically **worse**"* is separately withdrawn by [`CH-0177`](../challenges/CH-0177-the-path-count-axis-is-not-monotone.md) — the four numbers printed beside it rise once and fall three times, at both geometries.
> And the *"a path count here is a REQUEST, not a demonstration"* caveat below is **DISCHARGED on the count, the positions AND now the grading**:
> `C-0141` derives the honeycomb face's own station lattice (90 on `15 × 4`, 60 on `10 × 6`, one azimuth each at 30°),
> its plan ceiling, and a centro-symmetric placement family that exists on `10 × 6` and provably not on `15 × 4`;
> `C-0142` grades all sixteen placements **on that lattice** — every one is realisable, the largest snap being 3.332 nm inside a 3.57 nm ceiling — and **three** are flat, all three **equal springs**.

| | |
|---|---|
| **Task** | [`T-197`](../tasks/T-197-coupled-four-layer.md) — is a coupled four-layer tile flat under the measured staple dropout? |
| **Leaf** | `A8.2` |
| **Verification type** | **in-silico** — an influence surrogate over the four-layer grillage, with `C-0087`'s measured per-site incorporation as a Bernoulli dropout over 4 000 realisations on one common stream |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** The *folding* statistics it is graded against are measured; the flatness is not. |
| **Verdict** | **PASS on all four predicates. `F1` and `F3` did not fire; `F2` reproduced at `5.9e−10` and `1.8e−10`.** A coupled four-layer tile **is** flat under the measured dropout — decisively on `10 × 6` (8 of 8 cells, at every path count and both distributions) and marginally on `15 × 4` (1 of 8, needing 75 paths *and* the rim grading). `F3` did not fire because the cross-section **out-performs** the distribution by 3.17×, which is the finding. |
| **Provenance** | [`gpd/results/T-197-coupled-four-layer.json`](../results/T-197-coupled-four-layer.json), produced by `tile.CoupledFourLayerStudyKt`; model [`tile/CoupledFourLayer.kt`](../../src/main/kotlin/tile/CoupledFourLayer.kt), tests [`tile/CoupledFourLayerTest.kt`](../../src/test/kotlin/tile/CoupledFourLayerTest.kt) (6, written first and watched to fail). |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.142 pN·nm. Honeycomb, 112 bp span, `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V, `C-0001`'s secant foundation, `C-0087`'s measured depth-convention incorporation, seed 197197, 4 000 realisations, 81 × 81 dishing grid, `T-5b`'s 0.10. |
| **Consumes** | [`C-0109`](C-0109-four-layer-tile.md) (the tile and the coupled cells this extends), [`C-0120`](C-0120-cross-section-comparison.md) (the second cross-section, and why it is graded at all), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, an equality on the sum), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (the measured dropout), [`C-0058`](C-0058-non-uniform-coupling.md) (the rim grading) |
| **Constrains** | Nothing numerically — `C-0109`'s and `C-0120`'s numbers reproduce and are unmoved. **No claim is contradicted and no challenge is raised.** `C-0109`'s *"every coupled cell is worse than the uncoupled tile"* is **re-framed**, not overturned: it remains true and is not a design verdict. |

---

## 1. The framing, which is the part that had been missing

`C-0109` reports every coupled four-layer cell as **worse than the uncoupled tile**. That is true, and on its
own it decides nothing, because **the uncoupled tile is not a design the device could have**:

- §3 requires the actuator to deliver **100 pN** to a load.
- `C-0017`'s mandate is therefore an **equality on the SUM** of the coupling stiffnesses —
  `MANDATED_TOTAL_STIFFNESS` = 100 pN / 3 nm = **33.3333** pN/nm at §3's *acceptable* clause.
- So the total is **fixed and non-zero by specification**, and what a design chooses is only its
  *distribution*.

**The uncoupled tile is a reference, never a candidate.** Asserted as a test rather than as prose:
`equalShareOfMandate` refuses a zero total and refuses zero paths, and a graded distribution is required to
sum to exactly the same budget as an equal one.

---

## 2. The result

Sixteen cells: two cross-sections × four path counts × two distributions, all at the mandated total, all
under the measured dropout, all reported at the **90th percentile** rather than the zero-defect value —
because `C-0087`'s finding is that a flat design is a cancellation with no tolerance to a missing term.

| cross-section | flat at p90 | best cell | best p90 |
|---|---|---|---|
| **`10 × 6`** | **8 of 8** | 1 column, 10 paths, **equal springs** | **0.0278431488** |
| `15 × 4` | **1 of 8** | 5 columns, 75 paths, rim-graded 5:1 | 0.0882933461 |

**Nine of sixteen cells are flat**, and this is the first time in this programme that any *coupled* tile has
been flat at the 90th percentile under the only folding statistics anybody has measured.

**The best cell on `10 × 6` is the sparsest one tested** — ten paths in a single column. More paths are
monotonically *worse* there (0.0278431488 → 0.0541089284 → 0.0461988976 → 0.0408747025 as columns go 1 → 2 → 3 → 5), which is
`CLAUDE.md`'s own reading from the other side: *a dropout is an increase in the attachment pitch*, so a
coupling with fewer paths has fewer things to lose, and a tile stiff enough not to need the support pays only
the cost of the paths it has.

---

## 3. The cross-section beats the distribution, and `F3` was declared against exactly that

| axis | worth, on the 90th percentile |
|---|---|
| **cross-section**, `15 × 4` → `10 × 6` | **3.17109774×** |
| distribution, within `15 × 4` | equal 0.124585773 → graded **0.0882933461** at 75 paths, and *worse* at every other count |
| distribution, within `10 × 6` | equal 0.0278431488 (best) → graded 0.0306268096 at 10 paths; graded wins only at 30 and 50 |

**The distribution does not have a consistent sign** — it helps at some counts and hurts at others, on both
cross-sections — while the cross-section moves the statistic by 3.17× in one direction. `F3` was declared
because the opposite outcome would have made the coupling the thing to design and the tile the thing to
accept; it did not fire, and the design order is the other way round.

---

## 4. The five gates

| gate | how it was discharged |
|---|---|
| **dimensional consistency** | stiffness pN/nm, pitch and reach nm, dishing dimensionless; the mandate is asserted equal to §3's 100 pN over 3 nm |
| **limiting cases** | the mandate cannot be met by no coupling — both a zero total and a zero path count are **refused**, which is the framing of §1 made executable |
| **symmetry / conservation** | every distribution sums to the mandated total exactly, and a graded one preserves its weight ratios to `1e-12`; the influence bank is load-independent, so one bank serves every cell |
| **numerical convergence** | dropout realisations 1000 / 2000 / 4000 on a **common stream**, departure **0.0** between the last two — a convergence rather than a variance, because the stream is restricted and not redrawn |
| **literature cross-check** | the dropout is `C-0087`'s measured per-site map; the cross-sections are `C-0119`'s published designs; nothing new is cited |

**Reproductions:** `15 × 4` uncoupled at **5.9e−10**, `10 × 6` uncoupled at **1.8e−10**.

---

## 5. Validity range, and what this does NOT establish

- **The attachment grid is the abstract one, not a lattice census.** Every plan ceiling, station lattice and
  placement in this corpus is **single-layer square-lattice**, and `C-0119` establishes that the honeycomb's
  three azimuths at 7 bp are a different inventory. **A path count here is a REQUEST, not a demonstration that
  the stations exist**, and that is the largest open question this claim leaves.
- **Two distributions only** — equal and `C-0058`'s rim rule. `C-0089`'s percentile **descent** is not run,
  and it is worth 1.30–1.61× on an array; `C-0093`'s **shared-body topology** is not run either. Both were in
  `T-197`'s original scope and both remain open. The claim's verdict does not depend on them: they could only
  improve cells that are already flat.
- **The dropout is measured on a single-layer Rothemund rectangle.** Only the *profile* transfers, in nm; a
  four-layer tile has a different staple population and a different perimeter-to-area ratio. `C-0109`'s
  assumption, inherited and named.
- **The mandate is read at §3's ACCEPTABLE clause.** NDI's answer to decision 4 is that *two* devices exist,
  and the desired clause gives 10 pN/nm — a different total and therefore a different set of cells.
- **`C-0022`'s collar is read unchanged on both aspect ratios**, and it was solved on a 40 nm square. That is
  `C-0120`'s open question too, and it bears on the `10 × 6` rows here.
- **`10 × 6` is two-thirds of §3's footprint** (`C-0120`). Nothing in this claim addresses that; it is a
  specification trade for NDI, and the flatness result does not settle it.
