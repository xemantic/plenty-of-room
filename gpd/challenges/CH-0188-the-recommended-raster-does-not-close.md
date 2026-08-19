# CH-0188 — **`C-0140`'s recommended `112 / 108 bp` two-length raster does not close on caDNAno's own scaffold-crossover rule, and exactly one of its five candidate pairs does.** A scaffold crossover sits `±5 bp` from its pair's staple position, so one lattice constant `b₀` must serve every raster crossover; at 112 / 108 no `b₀` does, and **10 of 59** raster crossovers on the `10 × 6` block would have to be **forced** — which caDNAno permits and warns *"may lead to folding failure"*. **102 / 109** closes at zero forced crossovers, at both cross-sections and all four sign/mirror/datum conventions

| | |
|---|---|
| **Against** | [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) §4's recommendation — *"Read on a stated rule … the recommendation is **112 / 108 bp**"* — and, through it, every downstream reading of the two-length raster's **absolute axial windows** ([`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md), [`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md), [`CH-0187`](CH-0187-the-two-length-recommendation-rests-on-an-unstated-filter.md)) |
| **Raised by** | [`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md) / [`T-244`](../tasks/T-244-face-bond-class-residues.md), result [`gpd/results/T-244-face-bond-class-residues.json`](../results/T-244-face-bond-class-residues.json), section `closure` |
| **Grounds** | **logical.** One pass over `C-0140`'s own level walk, reducing every raster crossover by its own bond class. Exact integer arithmetic; no solve |
| **Kind** | **a missing constraint, not an error.** `C-0136`'s row-length rule is **per helix** and is satisfied everywhere; what it does not enforce is that the two helices sharing a crossover agree about **which** of the two `±5` positions that crossover occupies. The recommendation is a local optimum of a rule that is necessary and not sufficient |
| **Status** | **ANSWERED** by [`C-0151`](../claims/C-0151-closing-raster-selection.md) (`T-245`), which re-selects **inside the closing family** rather than among `C-0140`'s five and returns **`102 / 109`** — at the *same* 116 bp = 39.44 nm extent, for the price of **one** crossover column (10 against 11), and it is `−1.40 %` on §3's nominal exactly as `112 / 108` was. `C-0140`'s turn-sense derivation, its class algebra, its level walk and its *"no uniform row length exists"* negative are all upheld — and the last is **corroborated** from a new direction, no uniform length closing either |

---

## 1. The constraint `C-0136` does not carry

Douglas et al. (caDNAno, `PMC2731887`, in `gpd/data/T-151-sources/`, **read directly**):

> *"Our default rules allow antiparallel crossovers between adjacent scaffold helices to occur
> **five base pairs, or half a turn, upstream or downstream** of allowed crossover positions for
> the associated staple helices."*

So a scaffold crossover of a pair whose staple lattice is `b₀ + 7c (mod 21)` sits at
`b₀ + 7c ± 5`. `b₀` is a property of the **lattice** — one constant for the whole design.

`C-0136`'s admissible row lengths `N ≡ 7Δ + {0, 10, 11} (mod 21)` are exactly this rule applied to
**one helix**: `{0, +10, −10}` is the three ways the two ends' `±5` choices can differ. `C-0148`
re-derives that set from the residue map and it agrees term for term.

**But a helix's length fixes its own two sign choices, and each of its crossovers is shared with a
neighbour that has fixed the same choice from its own side.** Nothing in the per-helix rule makes
those agree, and on the real `10 × 6` raster they do not.

## 2. The test, and it is convention-free

Reduce every raster crossover by its own bond class: `(level − 7·class) mod 21`. Every value must
equal `b₀ + 5` or `b₀ − 5`, so **the set has at most two members and they are 10 apart**. Shifting
the axial datum shifts every member alike, so the verdict carries no convention.

| pair (`C-0140`'s five) | reduced residues | closes | forced crossovers, `10 × 6` | `15 × 4` |
|---|---|---|---|---|
| **112 / 108** — the recommendation | `0, 10, 11` | **no** | **10 of 59** | 8 of 59 |
| 101 / 109 — `CH-0187`'s alternative | `0, 1, 10, 11, 12` | **no** | 34 of 59 | 29 of 59 |
| **102 / 109** | `0, 10` | **YES** | **0** | **0** |
| 112 / 109 | `0, 10, 11` | **no** | 10 of 59 | 8 of 59 |
| 122 / 119 | `0, 1, 11` | **no** | 10 of 59 | 7 of 59 |

The verdict is identical at both 60-helix cross-sections and at all four
`(first axial sign, mirror, axial datum)` conventions — **80** graded cells, and **no uniform row
length closes at any of five lengths tested**, which is `C-0140`'s own negative reached from the
residue side rather than from the disjointness of two length sets.

**Exhaustively over residue pairs modulo 21 there are three closing classes on the `10 × 6` path** —
`(7, 14)`, `(17, 3)` and `(18, 4)` — of which only the last is among `C-0140`'s five candidates.

## 3. What it costs, and what it does not

**It is not a refutation of the geometry.** The same paper says caDNAno *"permits the user to force
crossovers between any two staple bases or between any two scaffold bases"*, adding that *"users
should take care … as departure from the default rules may lead to folding failure if too much
deviation from canonical DNA geometry is implied"*. A 112 / 108 block is buildable with **10 forced
scaffold crossovers**; that is a **yield** cost of the same family as Ke et al.'s 8 bp staple
domains, and this repository has no way to price it.

**What it does move is a selection.** `C-0140` chose 112 / 108 on `|extent − 40 nm|` under a stagger
filter; `CH-0187` re-opened the choice on four further axes and preferred 101 / 109 on three of
them. Scaffold closure is a **fifth axis**, it is a hard rule rather than a preference, and it
excludes **both** — leaving 102 / 109, which `C-0146`'s own Deliverable-4 table already carries
(rows 109 bp, block 116 bp, stagger 7 bp).

**And it is what makes the station ladder's phase determinable at all** — see
[`CH-0189`](CH-0189-the-ladder-phase-is-not-a-sweep.md).

## 4. Scope

- `C-0140`'s **turn senses**, its neighbour-class algebra, its level walk and its *"no uniform row
  length exists"* result are untouched and are consumed unmodified here.
- Every **relative** quantity read off the two-length raster survives: `C-0146`'s row spans, its
  4 bp stagger, its 116 bp block extent, the family statement that no two-length raster lengthens a
  row, and `C-0148`'s inter-row ladder **offset** of 14 bp, which is a difference of two classes and
  contains no `b₀`.
- What is challenged is the **recommendation** and, with it, any reading that depends on the
  raster's **absolute** axial registration.
- Nothing graded moves: `C-0142`'s and `C-0146`'s cells are all read at 112 / 108 and a re-grade at
  102 / 109 is a new study, whose interface window is 102 bp and whose row-derived crossover-column
  count is **10** rather than 11.
