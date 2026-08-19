# C-0123 — **The collar transfer cannot move the `10 × 6` verdict, and the bound needed no field solve.** The collar is a **local** rim effect, so its share of the load scales as **perimeter over area**: `15 × 4` carries **1.05555×** what `C-0022`'s solved tile does and `10 × 6` carries **1.31958×**, a **1.25013×** difference between them. Re-graded over a range containing those factors, **`10 × 6` is flat at the 90th percentile at every scale tested up to 3.0×** — against the **1.32×** it actually needs — so the margin is at least **2.27×** and the true break is above the range. `F1` did not fire and neither did `F2`: the response is monotone in the scale, so a single-factor bound is the right instrument

> **Annotated, iteration 34 ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **BOTH aspect ratios this claim is written on have moved by 1.5×**, in the same direction and by the same factor:
> the honeycomb `15 × 4` is **38.08 × 56.524 nm** and `10 × 6` is **38.08 × 37.504 nm**, so the two perimeter-to-area ratios swap places.
> `C-0141` records that the collar is read unchanged at both, which **reopens this claim's question rather than answering it**.

| | |
|---|---|
| **Task** | [`T-204`](../tasks/T-204-collar-aspect-ratio.md) — does `C-0022`'s collar transfer to the `10 × 6` aspect ratio? |
| **Leaf** | `A7.4`, with `A8.2` |
| **Verification type** | **logical** (a perimeter-over-area bound, asserted as a test) **+ in-silico** (the sensitivity, on the influence surrogate under the measured dropout) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Verdict** | **PASS on all four predicates. Neither falsifier fired.** `10 × 6` stays flat at **every** collar scale from 1.0 to 3.0 while needing 1.32×; the first scale at which any of its cells fails is **above the range tested**. `15 × 4` is flat at no scale under equal springs, which is `C-0118`'s own reading and not a new result. |
| **Provenance** | [`gpd/results/T-204-collar-aspect-ratio.json`](../results/T-204-collar-aspect-ratio.json), produced by `tile.CollarAspectRatioStudyKt`; `collarShareRatio` in [`tile/HoneycombStationLattice.kt`](../../src/main/kotlin/tile/HoneycombStationLattice.kt), tests [`tile/CollarAspectRatioTest.kt`](../../src/test/kotlin/tile/CollarAspectRatioTest.kt) (3, written first and watched to fail). |
| **Conditions** | `C-0022`'s solved profile at 2 mM / 10 nm / 0.192 V, taken from its 40 × 40.35 nm tile; honeycomb at 112 bp span; `C-0017`'s mandate with equal springs; `C-0087`'s measured dropout, 4 000 realisations, seed 204204. |
| **Consumes** | [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved profile and the tile it was solved on), [`C-0118`](C-0118-coupled-four-layer.md) (the cells re-graded), [`C-0120`](C-0120-cross-section-comparison.md) and [`C-0122`](C-0122-honeycomb-station-lattice.md) (which both name this as owed) |
| **Constrains** | Nothing numerically. **No claim is contradicted and no challenge is raised** — an open question of two claims is **discharged**, which strengthens them. |

---

## 1. The bound, which is arithmetic

The collar's **depth and width are set by screening** — `CLAUDE.md` records a sub-Debye 1.65 nm band scaling
as `1/L` — and its **share of the load** by the tile's perimeter over area:

| tile | | collar share, relative to `C-0022`'s |
|---|---|---|
| `C-0022` as solved | 40 × 40.35 nm | 1 |
| `15 × 4` | 38.08 × 38.04 nm | **1.05555×** |
| `10 × 6` | 38.08 × 25.36 nm | **1.31958×** |
| `10 × 6` against `15 × 4` | | **1.25013×** |

**No field is solved to get any of that**, and it is asserted as a test — a square of half the side has twice
the share, and a tile compared with itself transfers exactly.

---

## 2. The sensitivity, measured rather than argued

Both collar terms scaled together over `1.0 … 3.0`, a range that **contains** each cross-section's own factor:

| collar scale | `10 × 6`, 1 column | `10 × 6`, 5 columns |
|---|---|---|
| 1.0 (as solved) | 0.0278431488 | 0.0395297268 |
| **1.31957669** (its own factor) | **0.0314919626** | **0.0438546296** |
| 1.5 | 0.0335519722 | 0.0462697939 |
| 2.0 | 0.0392607957 | 0.0529966179 |
| 3.0 | **0.0506784425** | **0.0664028863** |

**Flat at every one.** The tile needs **1.32×** and is still flat at **3.0×**, so the margin is at least
**2.27×** and the true break lies above the range. `F1` did not fire.

**And the response is monotone in the scale** at both column counts, so a single-factor bound is the right
instrument — `F2` did not fire either, and that mattered: `CLAUDE.md` records that a verdict which is not
monotone in a swept variable has no threshold at all.

`15 × 4` is flat at no scale, including 1.0. That is `C-0118`'s own reading under equal springs — its single
flat cell used the rim grading, which is not re-graded here — and it is reported so the table is not mistaken
for a new negative.

---

## 3. Validity range, and what this deliberately does NOT cover

- **It scales the SHARE and holds the SHAPE.** `C-0022`'s taper width and rim standoff are unchanged, which is
  the whole premise: they are set by screening, not by the tile. **A genuine 2-D re-solve at 38.08 × 25.36 nm
  could move the shape too, and that is not bounded here.** It remains the thorough answer and it is now known
  to be worth less than it looked, because the share it would correct is already covered 2.27× over.
- **A rectangular tile's collar is not uniform along its perimeter** — the short and long sides see different
  fringing, and `edgeCollarPressure` applies one profile to all four. `C-0022`'s convention, inherited.
- **Scaling the collar RAISES the total force** slightly rather than redistributing it, because the interior
  pressure is held at `TARGET_FORCE` over the footprint. That is the conservative direction for a flatness
  question and it is why the margin above is a lower bound.
- **Equal springs only**, as `C-0118`'s best cells use.
