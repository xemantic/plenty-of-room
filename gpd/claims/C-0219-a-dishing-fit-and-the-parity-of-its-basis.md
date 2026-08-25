# C-0219 — **THE DISHING DECOMPOSITION IS NOW A GENUINE LEAST-SQUARES FIT, AND THE PARITY THAT MADE THREE INDEPENDENT PROJECTIONS WRONG IS A CLOSED-FORM INTEGER STATEMENT ABOUT THE FACE'S OWN HALF-BOND LADDER: `Σ beamY` IS EXACTLY `0` AT EVEN `m` AND `∓(m − 1)d/4` AT ODD, WHICH REPRODUCES `CH-0282`'s `0.0358744468` AND — THROUGH `(∫y dA/∫y² dA)(L_y/2)`, A PURE GEOMETRIC RATIO CARRYING NO ELEMENT, NO ROW LENGTH, NO THICKNESS AND NO TIE — `C-0218`'s `0.0620506254`, WITH NO SOLVE AT ALL.** The repair branches on that exact statement, so **15 of the 18** result files carrying a grillage dishing are unmoved **by construction** and **4 of 4** byte-identity controls confirm it; **3** are re-emitted (`T-253`, `T-254`, `T-297`) and **0** fourth-level consumers move, because every downstream reader selects the `10 × 6` half explicitly. `C-0154`'s three `15 × 4` free tiles become **`0.242196276 / 0.157167743 / 0.150056485`**, reproducing `C-0218` §7 exactly, and **no verdict moves at either cross-section**. **`F2` FIRED and its firing is what makes the branch load-bearing**: the quadrature Gram is not reliably the exact `0.0` that `T-294`'s own `10 × 6` at 116 bp returns — an assertion of exact equality failed on this study's first run — so a branch taken on the float would not have been inert, and the one taken on the **integer ladder** is. **AND THE HALF THIS TASK REFUSES IS PRICED RATHER THAN CAUTIONED** (`CH-0284`): the class **fits** with `faceFunctional`'s owning-beam reconstruction and **samples** with `evaluate`'s nearest-beam one, and that gap is ``4.3E-4`–`5.0E-4`` on the collar load every free-tile reading is taken under but **``0.0067``** on a bond prestrain influence function — against `C-0180`'s tightest recovered cell, which clears `T-5b` by `0.426 %`

| | |
|---|---|
| **Task** | [`T-330`](../tasks/T-330-a-dishing-fit-and-the-parity-of-its-basis.md) — the row [`C-0218`](C-0218-the-tied-regrade-at-the-other-cross-section.md) (`T-294`) §7 and [`CH-0282`](../challenges/CH-0282-a-dishing-fit-assumes-an-even-raster-row-count.md) opened |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (a closed-form, integer cheap bound over the face's own half-bond ladder, which reproduces both of the challenge's published numbers with no solve) **+ in-silico** (the repaired decomposition, its reproduction of `C-0154`'s and `C-0218`'s committed readings, the measured cost of the branch, and four byte-identity controls) |
| **Verdict** | **PASS on all ten predicates.** `P1`–`P10` all met. `F1`, `F3`, `F4`, `F5`, `F6`, `F7`, `F8`, `F10`, `F11` did not fire. **`F2` was declared OPEN and FIRED** — its firing is what makes the branch load-bearing (§2). **`F9` was declared OPEN and FIRED on its first run**, at three fields, every one of them a comparison of two quantities meant to be zero; repaired, two independent emissions are now **byte-identical**. **`CH-0282` is RESOLVED**; **`CH-0284` is raised** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Nothing here changes a **solve**: at `15 × 4` the solved field under a uniform pressure is uniform to `1e−10` relative, which `C-0218` asserts as a named test. What moved is the **fit** |
| **Provenance** | [`gpd/results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json`](../results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json), written by [`tile/FaceRigidBasisStudy.kt`](../../src/main/kotlin/tile/FaceRigidBasisStudy.kt) (**new**) on [`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt), which this task **owns** this iteration. Shared sources edited: `tile/HoneycombGrillage.kt` (the repair), `tile/CrossSectionTiedRegrade.kt` and `tile/CrossSectionTiedRegradeStudy.kt` (`T-294`'s own two files, five one-line repointings at the retained accessor plus the delegation), `structure/ResultInputs.kt` (a `T_330` handle **by hand**, never through the generator), `build.gradle.kts` and `tools/P-31-harness-census.py` (the harness wiring and registration), `tools/T-294-mutation-test.py` (six anchors this refactor orphaned, which `P-31` caught, and one mutation that had become a no-op). **19 gate-named tests written first and watched fail** — [`tile/FaceRigidBasisTest.kt`](../../src/test/kotlin/tile/FaceRigidBasisTest.kt) (**new**), of which **six failed on their first real run and every one was informative** (§2, §7) — plus two in `HoneycombGrillageTest` and one in `CrossSectionTiedRegradeTest`. **Mutation-tested** by [`tools/T-330-mutation-test.py`](../../tools/T-330-mutation-test.py) (**new**, registered in `tools/P-31-harness-census.py` and wired in `build.gradle.kts`): **13 mutations, 0 survivors** over a subtracted baseline (`CH-0237`), after two first-run survivors that were both findings; and `tools/T-294-mutation-test.py` re-run at **19 mutations, 0 survivors**. A full `./gradlew test` on the final sources gives **3 635 tests in 205 classes, 0 failures, 0 errors** — `BUILD SUCCESSFUL`, which carries every wired document gate with it. Result file **byte-identical across two independent emissions** after the three repairs `F9` forced; the two emissions it **fired** on are retained in [`gpd/data/T-330-reproducibility/`](../data/T-330-reproducibility/README.md) with a field-by-field account, so the firing stays checkable (`C-0092`). Re-emitted: `T-253`, `T-254`, `T-297`. Controls re-run: `T-267`, `T-304`, `T-307`, `T-263` (byte-identical) and `T-294` (§3a). `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-result-path-references.py`, `check-cold-start-note.py`, `check-queue-vocabulary.py`, `cli_guard.py --check`, `P-31-harness-census.py --check` and `check-result-file-hygiene.py` (base, `--prose`, `--departures`, `--saturated`) are all clean |
| **Conditions** | T = 300 K, aqueous **2 mM MgCl₂**, `k_BT = 4.142 pN·nm`. Honeycomb `d` = 2.536 nm (SAXS); in-plane raster-row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp. Face = the gap-facing column, `faceColumn = 0`; tributary = one row pitch centred on each face beam's own axis. `C-0154`'s own `112 bp` row and its own three enhancements at each cross-section; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation; `C-0017`'s mandate; `T-5b`'s `0.10` of the free stroke; an `81 × 81` dishing grid. The parity probe is an `m × 2` block at 42 bp, because the worst off-diagonal is a ratio of three integrals that all carry the axial span as a factor |
| **Consumes** | [`C-0218`](C-0218-the-tied-regrade-at-the-other-cross-section.md) (`T-294`) — §7's corrected triple and its Gram, **reproduced**; [`C-0154`](C-0154-honeycomb-grillage.md) (`T-253`) — the six free tiles, **reproduced in the retained convention and corrected**; [`C-0022`](C-0022-tile-edge-load-profile.md); [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) — the `0.426 %` margin the refusal is priced against; [`C-0092`](C-0092-large-rotation-arm-branch.md) — *a repair must leave the defect measurable* |
| **Constrains** | **`CH-0282` is RESOLVED.** `C-0154`'s three `15 × 4` readings are corrected and its `10 × 6` readings are untouched; `C-0167`, `C-0180`, `C-0208`, `C-0211`, `C-0212`, `C-0215` and `C-0216` are **provably unmoved**, all being `m = 10`. **One challenge is raised**, [`CH-0284`](../challenges/CH-0284-a-fit-and-a-sample-in-two-reconstructions.md), against the reconstruction the class fits in against the one it samples in. Four passages of the two deliverables carry the corrected triple and are handed to the synthesis rather than edited here |

---

## 1. THE CHEAP BOUND — closed form, integer, and it reproduces BOTH of the challenge's published numbers with no solve

Each face beam's tributary is one row pitch `p = 3d/2` **centred on its own axis**, so

```
<piston, tiltY> = int y dA = L_s * p * SUM_r (y_r - yDatum)
```

and the face's rooting helices lie on an **integer ladder in units of `d/2`** — the row pitch is `3d/2` and the corrugation is half a bond, so every face `y` is a whole number of half bonds. Antisymmetry about the datum is therefore an exact statement in integers.

| `m` | `Σ (y_r − yDatum)`, faceColumn 0 | orthogonal | worst relative Gram off-diagonal |
|---|---|---|---|
| 10 | `0` | yes | below `1.0E-9` |
| 11 | `−6.34 nm` | no | `0.0475958489` |
| 14 | `0` | yes | below `1.0E-9` |
| 15 | **`−8.876 nm`** | no | **`0.0358744468`** |

`Σ = 0` at even `m` and `∓(m − 1)d/4` at odd, the sign set by the face column's own parity — **28 of 28** readings over `m = 3…16` and both face columns, and `faceRigidModesAreOrthogonal == (m even)` at **28 of 28**.

**And the defect's own MAGNITUDE is a pure geometric ratio.** For a uniform field `u = c·piston` the three projections leave `−c(∫y dA/∫y² dA)y`, so the standing convention's uniform-load dishing is

```
(int y dA / int y^2 dA) * (L_y / 2)
```

— carrying no element, no row length, no thickness and no tie. Evaluated at `m = 15` it is **`0.0620506254`**, which is `C-0218` §7's own number to every digit it publishes, **reproduced on a `15 × 2` probe at 42 bp with no ties** where `C-0218` measured it on a tied `15 × 4` block at 116 bp. Four multiplications; no solve.

## 2. The repair, and why it branches

`HoneycombGrillage` gains, all lazy and all built once per lattice: `faceRigidModes`, `faceRigidModesAreOrthogonal` (the **exact** integer antisymmetry), `faceRigidGram`, `worstFaceNonOrthogonality`, `faceRigidCoefficients` and `unconditionalFaceRigidCoefficients`. `HoneycombDeflection.dishingCoefficients` becomes the least-squares fit; the three-projection reading is retained as `independentProjectionDishingCoefficients` / `independentProjectionDishing` / `independentProjectionPeakDishing` (`C-0092`), and `T-294`'s `solveSymmetricThreeByThree` and `FaceRigidBasis` are **delegated rather than duplicated**.

Where the basis is orthogonal the three projections **are** the solve — a diagonal Gram makes the `3 × 3` elimination return exactly those three quotients — so the class takes them unchanged, **bit for bit**, asserted over four load cases at `m = 4`, `6` and `10`.

**`F2` fired, and its firing is what makes the branch load-bearing.** The quadrature Gram's off-diagonal at an even `m` is *not* reliably the exact `0.0` that `T-294`'s own `10 × 6` at 116 bp returns; a first draft of the predicate compared the `Double` positions and failed at `m = 4`, because `1.268 − 6.34` and `11.412 − 6.34` are not exact negatives. Only the integer statement is inert.

## 3. The blast radius, derived and then confirmed

Eighteen committed result files carry a number through `HoneycombDeflection.dishingCoefficients`.

| | files |
|---|---|
| `m = 10`, **provably unmoved by construction** — 15 | `T-263`, `T-267`, `T-279`, `T-284`, `T-291`, `T-294`, `T-299`, `T-303`, `T-304`, `T-307`, `T-310`, `T-315`, `T-316`, `T-322`, `T-323` |
| carrying `15 × 4` records, **re-emitted** — 3 | `T-253`, `T-254`, `T-297` |

Re-emitted in the order `tools/reemission-order.py` gives, `T-253 → T-254 → T-297`, and every diff is confined to the `15 × 4` half:

| file | leaves moved | of | what moved |
|---|---|---|---|
| `T-253` | **27** | 514 | six `flatness` and three `prestrain` records, all `15 × 4` |
| `T-254` | **51** | 1 058 | nine `fields`, three `ceilings`, three `stiffness`, all `15 × 4`; **no `10 × 6` field moved** |
| `T-297` | **74** | 1 151 | eighteen `sweep` rows, all `15 × 4`, six `reproductions` reading them, and two prose *"worst departure"* strings |

**No fourth-level consumer moves, and that is derived**: `T-263` and `T-315` select `crossSection == "10 x 6"`, `T-284` and `T-291` the `10 × 6` enhancement `21.1851817`, and `T-303` reads `cells`, which carries no `15 × 4` record.

### 3a. `F6`'s controls — and the one that differs does not differ because of this

Four `m = 10` files were re-run as byte-identity controls and **4 of 4 are byte-identical**: `T-267`, `T-304`, `T-307`, `T-263`.

`T-294` — which grades **both** cross-sections and whose five standing-convention call sites were repointed at the retained accessor — comes back differing at **5 of 10 649 leaves, every one of them `upstream/*/reproductionDeparture`**. That is the field `C-0218` §9 already declares irreproducible, at 3 of 10 649 between two runs of *unchanged* code; `DEPARTURE_DIGITS_BY_KEY` is keyed on `reproductions` and `convergence` and cannot see an `upstream` record. Every other leaf — every census, every cell, every `flatAtP90Standing`, every verdict — is identical. **The committed `T-294` is therefore left alone rather than re-emitted with five leaves of noise**, and the cure is filed as a queue row: `T-330`'s own `T330_DEPARTURE_DIGITS` is the one-line form of it.

## 4. `C-0154`'s three readings, and the ordering

| enhancement | `C-0154`, published | reproduced here, retained convention | **corrected** | flat either way |
|---|---|---|---|---|
| `1.0` | `0.312237799` | `0.312237799` | **`0.242196276`** | no / no |
| `9.65079217` | `0.227177955` | `0.227177955` | **`0.157167743`** | no / no |
| `12.7228458` | `0.220064299` | `0.220064299` | **`0.150056485`** | no / no |

**`11 of 11` reproductions close**, including `C-0218` §7's corrected triple to `< 1e−8` and `CH-0282`'s Gram off-diagonal to `0.0`. **No verdict moves at either cross-section, at any enhancement** — the corrected readings are smaller and all three remain outside `T-5b`, so the cross-section ordering `DECISIONS-FOR-NDI.md`'s decision 7 now rests on is **strengthened**.

## 5. The half this task REFUSES, priced rather than cautioned — `CH-0284`

The class **fits** its rigid plane with `faceFunctional`'s owning-beam reconstruction and **samples** the residual with `evaluate`'s nearest-beam one. The three modes reconstruct identically under both, so the Gram is one object and the whole disagreement is in the right-hand side. Measured at an orthogonal basis:

| load case | relative gap |
|---|---|
| `C-0022`'s solved collar, three enhancements | `4.3E-4`–`5.0E-4` |
| a point load at the face centre | `4.7E-4` |
| **a unit bond prestrain** | **`0.0067`** |
| a uniform pressure | not well posed — both readings are the solver's own noise |

`C-0180`'s tightest recovered coupled cell clears `T-5b` by **`0.426 %`**. The collar reading is inside that margin by a factor of **8.5** (`0.00426 / 5.0E-4`, both emitted) (`0.00426 / 5.0E-4`, both emitted); **the prestrain reading is not**, and a coupled surrogate's influence bank is built out of point-load and prestrain responses. So adopting the unconditional fit is not free: it moves all 18 files and it could cross `T-5b` at a recovered cell. It is a **convention** and not the defect `CH-0282` raises, so it is measured and filed (`CH-0284`, `T-326`) rather than taken as a side effect.

## 6. The standing falsifier slept for eleven iterations, and the fix costs no solve

`CLAUDE.md`'s *a uniform load on a uniform Winkler foundation produces no dishing at all* is written on a **solve**, so it can only test the lattice it is handed. `HoneycombGrillageTest`'s own `F1` hands it `rows = 6`; the fixture default is `rows = 4`. Both even. **No grillage dishing test in this repository had ever built an odd-`m` lattice** before `T-294`.

Two strengthenings, both wired here and both microseconds:

1. **Take the falsifier on the PROJECTOR**: the decomposition must annihilate each of its **own three basis modes** exactly. No load, no foundation, no factorisation — a property of the geometry alone, and it fires at an odd `m` instantly.
2. **Run the solve falsifier at both parities** — `rows = 3, 4, 5, 6, 7` in `HoneycombGrillageTest`.

The general form, offered for `CLAUDE.md`: *a falsifier written on a SOLVE tests the lattice you hand it; the same statement written on the DECOMPOSITION tests the geometry, costs no solve, and cannot be handed a favourable case by accident.*

## 7. What the run measured, and what it refused

- **Convergence.** The `81 × 81` dishing grid: `41 → 81 → 161` moves the corrected `15 × 4` free tile by at most `1.1E-16`. Beam subdivision `1 → 2` moves it `5.2E-4` — the same order `C-0180`'s own convergence cell reports, and it leaves every verdict standing.
- **Three degenerate comparisons were found and removed from the emitted file**, all of them `CLAUDE.md`'s own *comparing two quantities that are both meant to be zero relatively compares their noise*: the corrected uniform-load dishing (now a **threshold and a boolean**), the fit-versus-sample gap under a uniform load (now `0.0` with a `wellPosed` flag), and the geometric prediction's own tolerance. `F9` fired on the first two and passes after; two independent emissions are **byte-identical**.
- **The repair exposed the same trap in an existing test**: `HoneycombGrillageTest`'s axial-pin test compares two peak dishings on an **odd** `rows = 5` lattice, and once corrected both are the solver's noise, so a *relative* `isCloseTo` compares noise. It now compares absolutely, against the one scale in the problem.

## 8. Validity range

- The branch is on the face's own geometry and is exact for any block whose face lies on the half-bond ladder, which `HoneycombBlock.position` guarantees and a `require` asserts. A face that left that ladder would **refuse** rather than guess.
- The odd-`m` right-hand side is taken in `evaluate`'s inner product, the one the reported peak dishing is sampled through. The `faceFunctional` pairing the load is assembled with is a **different** fit; `CH-0284`.
- Nothing here changes a **solve**.
- The parity probe uses an `m × 2` block at 42 bp, justified by a named test: the worst off-diagonal is independent of the row length and the thickness. A one-column block is a mechanism under the banded solve, so every probe that solves takes two columns.
- `OrigamiGrillage`'s own decomposition is **not** examined. Its tributaries are uniform so the same defect cannot arise the same way, but that is an argument and not a measurement.

## 9. Open questions

- `CH-0284` — whether the class should fit and sample in the same reconstruction (`T-326`).
- `T-294`'s `upstream/*/reproductionDeparture` needs the two-digit departure rule; `T-330` carries the one-line form (`T-327`).
- Whether every geometric falsifier in this tree should be swept over both parities of every discrete count it depends on, or whether the projector-level test generalises more cheaply.

