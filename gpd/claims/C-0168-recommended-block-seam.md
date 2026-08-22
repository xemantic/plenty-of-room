# C-0168 — **NO, THE SEAM IS NOT FORCED, AND THE COMMITTED DESIGN IS RIGHT AS DRAWN — BUT NOT FOR THE REASON EITHER SIDE OF THE FORK EXPECTED.** `C-0119` §4's *"theorem"* has **two** premises and the block fails the **second**: 919 spare nucleotides close the circle at **1.03–2.69 `k_BT`** across the whole 2× ssDNA Kuhn bracket, against the **8.0 `k_BT`** the host sheet pays per crossover column, over a separation of **35.504 nm = 14 d exactly**, both termini at offset 7 and on the same face. The **first** premise is false too and it does not matter: the block's own cross-section adjacency carries **77 edges on 60 helices, 18 independent cycles**, not the 59 of a path. What decides it in microseconds is a **degree census** — the two raster termini are **degree one** at every one of 27 blocks swept, because two of a corner helix's three honeycomb bonds point **out** of the block, and a degree-one vertex lies on **no** cycle. So no honeycomb block of this family admits a Hamiltonian cycle at all, and `C-0119`'s factorial guard is never reached. And **even where the second premise holds the honeycomb's seam is nothing like Rothemund's**: the lower bound is **62 domains** on the lattice adjacency against **118** on the path, **1.903×**, and the corpus has been quoting the expensive one. **The induced scaffold graph is a path if and only if the row carries two helices**, which is *exactly* `C-0154`'s boundary for the **mechanical** interfaces: two independent questions, one integer. `CH-0212`'s second proposed reading **cannot be taken** — the block carries **0 staple crossings and 0 columns**, so it has no column parity to read — which makes the fork settleable only on the argument, and the argument turns out to be the cheap half. **No number moves**: both studies are inside `C-0161`'s alternating family, and graded anyway the rectangle's own seam shape costs **1.157×** (`0.0240648102 → 0.0278434397`), both flat

| | |
|---|---|
| **Task** | [`T-274`](../tasks/T-274-recommended-block-seam.md) — does the recommended `10 × 6` honeycomb block need a scaffold seam, and is the committed design drawn without one? |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (integer graph theory on the block's own cross-section lattice; a degree census, a bridge census and two handshake bounds) **+ in-silico** (a census of three committed artifacts through the reader, one counterfactual and one extent sweep on the grillage) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable, NOT empirically demonstrated.** Nothing here derives a folding **yield**; an energy is not a yield, and `CLAUDE.md` already records that residue as kinetic. No object is folded. |
| **Verdict** | **PASS on all seven predicates. Not one of the five declared falsifiers fired.** `F1` (the induced graph is a tree) did not fire — 77 edges on 60 helices. `F2` (a Hamiltonian cycle exists) did not fire — both termini are degree one. `F3` (`CH-0212`'s census of the artifact is wrong) did not fire — 60 domains on 60 helices, exactly as filed. `F4` (the remainder cannot reach or cannot afford) did not fire — it reaches on all four ssDNA conventions and is affordable on all four. `F5` (the seamed counterfactual crosses `T-5b`'s 0.10) did not fire — 0.0240648 and 0.0278434, both flat. **`CH-0212` is CLOSED on its favourable resolution.** |
| **Provenance** | [`gpd/results/T-274-recommended-block-seam.json`](../results/T-274-recommended-block-seam.json), emitted by [`design/ScaffoldSeamStudy.kt`](../../src/main/kotlin/design/ScaffoldSeamStudy.kt) (**new**) on [`design/ScaffoldSeamParity.kt`](../../src/main/kotlin/design/ScaffoldSeamParity.kt) (**new**). **24 gate-named tests**, written first and watched fail, in [`design/ScaffoldSeamParityTest.kt`](../../src/test/kotlin/design/ScaffoldSeamParityTest.kt) — and one of them **fired on its author**, refuting the degree bound's first draft on the three-vertex path (§2). Full suite green: **3 201** tests, 0 failures. **No existing source is edited at all** — the two new files are additive, and the three committed `.sc` artifacts are read, never rewritten, so `CommittedDesignsTest`'s byte-identity gate is untouched. **No committed result file moves and no study is re-run.** |
| **Conditions** | Lattice and topology only. Honeycomb bond length `d = 2.536 nm` (SAXS); rise `0.34 nm/bp`; row pitch `3d/2`, column pitch `d√3/2`. `k_BT = 4.142 pN·nm` at 300 K. The counterfactual and the extent sweep are at `C-0022`'s solved collar, 2 mM MgCl₂, 10 nm gap, 0.192 V — the state both studies grade at. |
| **Consumes** | [`C-0119`](C-0119-honeycomb-raster-width.md) §4 (the statement under test), [`C-0160`](C-0160-scadnano-writer.md) (the committed artifact and its 6 330 / 919 nt budget), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) §4 (the seam discriminator, the parity-alternation scope statement, and the reference rectangle as the seamed control), [`C-0154`](C-0154-honeycomb-grillage.md) (a honeycomb block's interfaces are not a path graph), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the cross-section and the `10 × 6` free-tile dishing this study reproduces at departure `0.0`), [`C-0151`](C-0151-closing-raster-selection.md) (the drawable `102 / 109` raster), `SsDnaTether` (the Kuhn and contour brackets, **cited, measured**) |
| **Constrains** | **Annotates [`C-0119`](C-0119-honeycomb-raster-width.md) §4** — one word, *forced*, struck and replaced; its brute force, its reading of Figure 2b and its `7k ± 5` integrality result are untouched. Sweeps the two carriers in the deliverables. **Raises [`CH-0215`](../challenges/CH-0215-the-artifact-does-not-state-its-scaffold-topology.md)** (against `C-0160`) and **[`CH-0216`](../challenges/CH-0216-the-recommended-block-is-graded-at-neither-of-its-widths.md)** (against `C-0141` / `C-0142`, priced at ≤ 3.9 %). |

---

## 1. The cheap bound, and it settles the question

A vertex of degree one lies on **no cycle**. So if either end of the raster has one honeycomb
neighbour inside the block, no closed walk can cover the block without revisiting, and no scaffold
routing gives one domain per helix *while closing inside the design*.

It is 60 lookups of a three-element neighbour list, against `C-0119`'s own 59! it refuses to
attempt — and it returns:

| | `10 × 6` |
|---|---|
| helices | **60** |
| degree-one helices | **`[0, 59]`** — the two raster termini, and nothing else |
| why | two of a corner helix's **three** honeycomb bonds point **out of the block** |
| Hamiltonian cycle | **none**, decided rather than searched |

Swept over 27 blocks (`m = 2…10`, `n = 2, 4, 6`) the termini are the only degree-one helices at
**every** one.

---

## 2. The two premises, separated

`CLAUDE.md` states the theorem with two:

> **(P1)** the graph the scaffold may use is a **tree** — every edge a bridge, so a closed walk
> crosses each an even number of times; **(P2)** the scaffold is a **fully folded circular**
> strand, so its closure is itself an edge of that graph. **A seam needs BOTH premises and dropping
> either removes it.**

### (P1) holds only under a restriction, and the restriction is a reading of the source

| reading of *"the graph the scaffold may use"* | edges on 60 helices | tree | cycles | bridges |
|---|---|---|---|---|
| Douglas et al., *"the path of the scaffold stays within a 2D surface"* — `C-0119` §4's graph | **59** | **yes** | 0 | 59 |
| the honeycomb lattice's own adjacency, **induced** on the block | **77** | **no** | **18** | **2** |

`C-0154` is what makes the second reading obligatory rather than pedantic: a honeycomb site has
three lattice neighbours, and the block's mechanical interfaces are not a path either. **The verdict
is the same on both readings, which is what makes it robust** — and it does not come from (P1) at all.

### (P2) is where the block leaves the theorem

A **lower bound** on the **domains** a fully folded circular scaffold needs, from the handshake
identity on an Eulerian covering multigraph `H` — every covered helix has even degree ≥ 2; a leaf's
single edge must carry an even multiplicity, so ≥ 2; its neighbour then needs 2 more to reach the
rest of a connected `H`; and every bridge is in `H` at multiplicity ≥ 2 while `H`'s underlying simple
graph spans:

| reading | lower bound | which bound binds |
|---|---|---|
| the 2-D surface (a path) | **118** | `2(\|V\| − 1)` — the **bridge** bound, every edge a bridge; Rothemund's two segments per helix |
| the block's own adjacency | **62** | `\|V\| + leaves` — the **leaf** bound, the block having only two bridges |
| **the committed design** | **60** | a **Hamiltonian path**, which is not a closed walk at all |

**Nothing here needs either bound to be attained**: both exceed 60, which is the whole argument.

The degree bound's *`+2` for the rest of the graph* is unsound where a helix carries **both**
leaves and has no rest to reach — on the three-vertex path it returns 5 against a true minimum of 4 —
and a declared test on exactly that path caught it. Repairing it left the emitted file **byte-identical**:
the block's two leaves have distinct neighbours and 56 other helices behind them, so the qualifier is
satisfied and 62 does not move. So the drawn routing is a statement that the scaffold is **not fully folded**
— a linear strand, or a circular one closing through its own unpaired remainder.

---

## 3. The remainder closes, and it costs less than one crossover column

The block's two termini are `HoneycombCell(0, 0)` and `HoneycombCell(0, −28)` — the same `x`, so the
same face, no detour around the block — and the committed design's 5′ and 3′ ends both sit at
**offset 7**, so the separation is purely lateral:

**`14 d = 35.504 nm`, exactly.**

Against `C-0160`'s **919 spare nucleotides** of M13's 7 249, over `CLAUDE.md`'s 2× method-systematic
ssDNA bracket — the Kuhn length and the contour per nucleotide travelling as a **pair**, never mixed:

| convention | `b` (nm) | nm/nt | reach (nt) | `r/L_c` | `ΔF` (`k_BT`) | `ΔF` (eV) | nt at 8 `k_BT` |
|---|---|---|---|---|---|---|---|
| zero-force scattering, 2 mM | 2.84 | 0.70 | **51** | 0.0551904244 | **1.03493706** | 0.0267551926 | 119 |
| zero-force scattering, 10 mM | 2.10 | 0.65 | 55 | 0.0594358416 | 1.50729294 | 0.0389665369 | 174 |
| force spectroscopy, 2 mM | 1.41 | 0.57 | 63 | 0.0677777141 | 2.55997868 | 0.0661805684 | 295 |
| force spectroscopy, 10 mM | 1.34 | 0.57 | **63** | 0.0677777141 | **2.69370891** | 0.0696377623 | 310 |

The reach bound runs first and asks **51–63 nt**; the Gaussian stretch costs **1.03–2.69 `k_BT`**
against the **8.0 `k_BT`** `CLAUDE.md` records the host sheet paying per crossover column, and the
extension ratio is **0.055–0.068**, so the Gaussian sits well inside its own small-extension validity.
The last column is the falsifier read as a **threshold**: the seam would be forced below **119–310 nt**
of remainder, and the block has 919.

### And the cross-section this programme recommends closes cheapest, which nothing selected for

| block | separation | spare | `ΔF` over the bracket | affordable at 8 `k_BT` |
|---|---|---|---|---|
| `10 × 6` | **35.504 nm** | 919 nt | **1.03–2.69 `k_BT`** | **4 of 4 conventions** |
| `15 × 4` | 54.9206503 nm | 529 nt | 4.30219953–11.1976599 `k_BT` | 2 of 4 |

**4.16×** worse at the same convention (`4.30219953 / 1.03493706`).
`15 × 4` still closes at the zero-force end — which is the end `CLAUDE.md` says a ~1 pN chain
needs — and not at the force-spectroscopy end. Folding yield,
flatness, footprint and now scaffold closure all rank the two the same way.

---

## 4. The artifacts, read back through the reader

| | committed block | corpus square sheet | reference rectangle |
|---|---|---|---|
| grid | honeycomb | square | square |
| helices | **60** | 15 | 16 |
| strands | **1** | 65 | 65 |
| scaffold domains | **60** | 15 | **31** |
| domains per helix | **1** | 1 | **1, 2** |
| scaffold bases | **6 330** | 1 680 | 2 048 |
| scaffold turns | 59 | 14 | 30 |
| staple crossings | **0** | 49 | 90 |
| crossover columns | **0** | 7 | 12 |
| 5′ / 3′ offsets | **7 / 7** | 0 / 111 | 95 / 96 |
| `C-0161`'s discriminator | **no seam** | no seam | **31 against 16 — the seam** |

`CH-0212`'s census is confirmed exactly, and the rectangle is the control that makes it a
discriminator rather than a coincidence.

### `CH-0212`'s second free reading is not available, and that is itself an answer

It proposed running the importer on the block and reading its column parity sequence. **A column
parity is a property of the STAPLE ladder**, and the block carries no staple set at all
(`C-0160` §6 says so and gives the reason) — **0 staple crossings, 0 columns**, so `crossoverPhase()`
throws rather than returning a phase. The rectangle returns one, so the absence is a fact about the
design and not about the reader. It is also why `C-0161` refused the block as an `OrigamiGrillage`.

**So the fork is settleable only on the argument — the half `CH-0212` called the more expensive
one — and the argument is a degree census.**

---

## 5. What happens to the two studies: nothing, and it is measured anyway

`C-0161` §4(b) restricts every phase-swept result in this corpus to the **alternating** family. The
block is seamless, so `HoneycombCoupledStudy` and `HoneycombPlacementStudy` are inside it and **no
number moves**. Graded anyway, on the smeared equivalent sheet both studies use today, at the state
they use, with the **shape** a seam takes in the one seamed design this corpus has imported — one
column deleted, a doubled pitch, two consecutive columns of the same parity:

| layout | columns | parities | equal-adjacent | collar dishing | uniform load | flat |
|---|---|---|---|---|---|---|
| alternating (`CrossoverLayout.centred`) | 11 | `01010101010` | 0 | **0.0240648102** | `0.0` | yes |
| seamed (the rectangle's own shape) | 10 | `0101001010` | **1** | **0.0278434397** | `0.0` | yes |

**1.15701888×**, and both flat at `T-5b`'s 0.10. What that challenge's last sentence left open —
*"Under the solved edge collar the two would part, and by how much is unmeasured"* — is now measured,
on the branch that is not taken. The alternating reading reproduces `C-0141`/`T-219`'s
committed `10 × 6` free-tile dishing at departure **`0.0`**, which is what makes it the same object.

### A second scope item, found while looking, and priced rather than left open

Both studies grade the recommended block at **112 bp = 38.08 nm** — the **square** sheet's own row
length, and the **withdrawn** `112 / 108` honeycomb pair's. `C-0151`'s drawable `102 / 109` raster's
rows span **109 bp = 37.06 nm** and its box is **116 bp = 39.44 nm**; 38.08 is **neither**.

| reading | bp | `edgeX` | columns | collar dishing | of as-graded | flat |
|---|---|---|---|---|---|---|
| drawable row span (`C-0151`) | 109 | 37.06 nm | 11 | 0.0231880196 | 0.963565448 | yes |
| **as graded by both studies** | 112 | 38.08 nm | 11 | **0.0240648102** | 1.0 | yes |
| drawable bounding box (`C-0146`) | 116 | 39.44 nm | 12 | 0.0231299291 | 0.96115153 | yes |

Worth at most **3.9 %**, both alternatives *better* than the as-graded value, all three flat.
**No verdict moves**, and the repair is a re-emission that belongs with `T-263`. Filed as
[`CH-0216`](../challenges/CH-0216-the-recommended-block-is-graded-at-neither-of-its-widths.md).

---

## 6. Reproductions

| of | quantity | published | here | departure |
|---|---|---|---|---|
| `C-0141` / `T-219` | `10 × 6` free-tile collar dishing over stroke | 0.0240648102 | 0.0240648102 | **`0.0`** |
| `C-0141` / `T-219` | `10 × 6` plate `edgeY` in nm | 38.04 | 38.04 | **`0.0`** |
| `C-0119` / `CLAUDE.md` | domains a fully folded circular scaffold needs on a 60-helix **path** | 118 | 118 | **`0.0`** |
| `C-0160` | scaffold bases in the committed block | 6 330 | 6 330 | **`0.0`** |
| `C-0161` | scaffold domains of the reference rectangle | 31 | 31 | **`0.0`** |

The third row is the one that matters for the annotation: `C-0119` §4's arithmetic is **reproduced**,
not overturned. What is withdrawn is the word *forced*, which is a statement about the premise it
never examined.

---

## 7. What is *not* claimed

- **Not** that `C-0119` §4 is wrong. Its brute force at orders 3–7, its reading of caDNAno Figure 2b,
  its `7k ± 5` integrality result and its scaffold budget are untouched and reproduce.
- **Not** a folding yield. An elastic or entropic price is not a yield, and the residue is kinetic.
- **Not** that the committed artifact is complete. It states no scaffold topology at all — see
  [`CH-0215`](../challenges/CH-0215-the-artifact-does-not-state-its-scaffold-topology.md).
- **Not** that 62 is *attained*, and not that it is drawable. It is a **lower bound**, which is all
  the argument uses; whether a 62-domain routing exists, and whether it would close under caDNAno's
  `±5 bp` scaffold rule, are separate questions and both are moot because the design does not need one.
- **Not** a re-grade of the coupled cells. The counterfactual prices a branch that is not taken, on
  the generator both studies call today.

---

## 8. Validity range

- **LATTICE AND TOPOLOGY ONLY.** No folding yield is derived.
- **THE CLOSURE IS AN IDEAL CHAIN.** The Gaussian stretch ignores ssDNA's excluded volume, its
  electrostatic stiffening in Mg²⁺, and exclusion by the block itself. All three **raise** the price;
  the margin against one crossover column is `8.0 / 2.69370891 = 3.0` at the tightest convention and
  `8.0 / 1.03493706 = 7.7` at the loosest, so the sign is known and the conclusion is not close.
- **THE INDUCED READING IS PERMISSIVE.** It allows scaffold crossovers Douglas et al. say do not
  occur. It is carried so that the verdict cannot depend on which reading is preferred, and it does not.
- **THE COUNTERFACTUAL IS A SHAPE, NOT A DESIGN.** The block has no staple routing, so a *"seamed
  honeycomb column layout"* is the rectangle's own signature applied to the block's ladder.
- **THE COUNTERFACTUAL AND THE EXTENT SWEEP ARE ON THE SMEARED EQUIVALENT SHEET** both studies use
  today. A re-grade on the honeycomb grillage (`T-263`) moves the cells together; the **ratio** is
  what this claim quotes.
- **THE ARTIFACT DOES NOT STATE ITS SCAFFOLD TOPOLOGY**, so *"the drawn routing asserts a scaffold
  that is not fully folded"* is an inference from the domain count, not a field of the file.

## 9. Open questions

- Should the emitted block carry the 919 nt remainder as an explicit loopout, or the scaffold's
  circularity as a flag? Both are **schema** steps in `ScadnanoWriter`; neither is a lattice question
  (`CH-0215`).
- Does a honeycomb raster with a **shorter** remainder — a bigger block on the same scaffold — reach
  the closure threshold this claim names? The threshold is emitted per convention and the answer is
  one comparison.
- Whether a 62-domain routing exists at all, and whether it is drawable under caDNAno's `±5 bp`
  scaffold rule. Neither is needed here, and the bound is the cheap half.
