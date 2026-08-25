# T-330 — the dishing decomposition's basis, and the parity that decides whether it is orthogonal

**Leaf** `A8.2`.
**Raised by** [`C-0218`](../claims/C-0218-the-tied-regrade-at-the-other-cross-section.md) (`T-294`) §7 and [`CH-0282`](../challenges/CH-0282-a-dishing-fit-assumes-an-even-raster-row-count.md).
**Claim to be filed** `C-0219`. **Challenge numbers reserved** `CH-0284`, `CH-0285`. **Queue rows reserved** `T-326`, `T-327`.

---

## 0. Locked units, geometry and sign conventions

SI throughout, `k_BT = 4.142 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
Lengths nm, forces pN, stiffness pN/nm, pressure pN/nm² (= 1 MPa exactly).

Honeycomb `d = 2.536 nm` (SAXS).
In-plane raster-row pitch `p = 3d/2 = 3.804 nm`; layer pitch `d√3/2 = 2.19624042 nm`; rise `0.34 nm/bp`.
A block is `m` corrugated x-raster rows of `n` helices; the **face** is the column `faceColumn` (default `0`) the polymer layer confronts.

`s` runs along the helices, `y` across them in the plane of the face, `z` through the thickness; `W` is positive **downward** (into the foundation), `Φ` is the beam roll about its own axis, and the face field off a beam axis is `W + Φ·(y − y_beam)`.
The face `y` datum is `HoneycombGrillage`'s own: `yDatum = (min faceY + max faceY)/2`, so `beamY` is centred on the face's **envelope**, not on its **centroid** — which is the whole of what follows.

Dishing is reported as a **fraction of the free stroke**, `peakDishing / (F/(edgeX·edgeY·k_f))`, on an `81 × 81` face grid, against `T-5b`'s `0.10`.

---

## 1. The statement

`HoneycombDeflection` removes its best-fit rigid plane by **three independent projections**:

```
meanDeflection = <piston, u> / A
tiltAlong      = <tiltS,  u> / ||tiltS||^2
tiltAcross     = <tiltY,  u> / ||tiltY||^2
dishing        = u - meanDeflection*piston - tiltAlong*tiltS - tiltAcross*tiltY
```

That is the least-squares fit **iff the three modes are mutually orthogonal** in the face inner product.
`⟨piston, tiltS⟩ = ∫s dA = 0` and `⟨tiltS, tiltY⟩ = ∫sy dA = 0` because the axial range is symmetric.
`⟨piston, tiltY⟩ = ∫y dA` is not automatically zero, and on a **corrugated** honeycomb face it is a **parity in `m`**.

Every block this corpus had graded before `T-294` has `m = 10`.
At `15 × 4` a solved field that is uniform to `1e−10` — the state `CLAUDE.md`'s standing falsifier is written on — is reported as **`0.0620506254`** of the stroke of dishing, `62 %` of `T-5b`'s whole tolerance on a field with no curvature at all.

---

## 2. THE CHEAP BOUND — it is closed form, integer, and it runs with no JVM and no solve

The face's rooting helices sit at `y_r = r·p + ½d·[(r + faceColumn) even]`, so the gap sequence along the face is `d, 2d, d, 2d, …`, and the tributary of each face beam is one row pitch `p` **centred on that beam's own axis**.
Therefore

```
<piston, tiltY> = L_s * p * SUM_r (y_r - yDatum)
```

and the whole question is the sign-free integer `SUM_r (y_r − yDatum)`.
Evaluated in closed form:

| `m` | `Σ (y_r − yDatum)` | `ȳ = Σ/m` | worst relative Gram off-diagonal |
|---|---|---|---|
| 10 | `0` | `0` | `0.0` |
| 11 | `−6.34 nm` | `−0.576363636 nm` | `0.0475958489` |
| 14 | `0` | `0` | `0.0` |
| 15 | `−8.876 nm` | `−0.591733333 nm` | **`0.0358744468`** |

`Σ = 0` at even `m` and `Σ = −(m − 1)d/4` at odd `m`, exactly — `−3.5 d = −8.876 nm` at `m = 15`, `−2.5 d = −6.34 nm` at `m = 11`.
The `m = 15` off-diagonal **reproduces `C-0218`'s and `CH-0282`'s `0.0358744468` to every digit they publish, from four lines of arithmetic and no solve.**

**So the condition under which a committed reading cannot move is a PARITY, and the corpus is partitioned by it before any code is written.**
Equivalently and more usefully as a predicate on the object rather than on `m`: the face's beam positions are **exactly antisymmetric** about their own datum, `beamY[i] == −beamY[m−1−i]` for every `i`.
That is an exact floating-point statement, not a tolerance, and it is what the repair will branch on.

### 2b. What the bound decides about the METHOD

- The Gram is a property of the **geometry**, so it is built **once per lattice**, lazily, and an influence bank of several hundred fields pays for it once. There is no solve in the repair at all.
- The correction is a `3 × 3` symmetric elimination. `T-294` already wrote one (`solveSymmetricThreeByThree`), tested it and mutation-tested it; this task **moves it into the shared class and deletes the duplicate**, because `CLAUDE.md` records that *a duplicated rule is invisible to a mutation test of either copy*.
- **Where the basis is orthogonal the standing three projections ARE the solve**, so the repair takes them **unchanged, bit for bit**, and 15 of the corpus's 18 grillage-emitting result files are unmoved by a *proof about the code* rather than by a re-run. That is the difference between a three-file sweep and an eighteen-file one.

---

## 3. The corpus, partitioned by the bound

Eighteen committed result files carry a number that passes through `HoneycombDeflection.dishingCoefficients`.
Derived by enumerating every main source that constructs a `HoneycombGrillage`, then reading its raster-row count:

| `m` | files | why they cannot move |
|---|---|---|
| **10** | `T-263`, `T-267`, `T-279`, `T-284`, `T-291`, `T-294`, `T-299`, `T-303`, `T-304`, `T-307`, `T-310`, `T-315`, `T-316`, `T-322`, `T-323` — **15** | the face basis is orthogonal, so the corrected fit **is** the standing one and the class returns the identical coefficients |
| **10 and 15** | `T-253`, `T-254`, `T-297` — **3** | their `15 × 4` records move; their `10 × 6` records do not |

`T-294` is in the first row and needs **five one-line edits in its own two files** to stay there — `CrossSectionTiedRegrade.kt:405` and `CrossSectionTiedRegradeStudy.kt:389, 886, 1175, 1177`, every one of them a `peakDishing` / `dishing` call standing for the *standing* convention, repointed at the retained accessor.
After them its `standing` column and its `corrected` column are both unchanged and the file is byte-identical — which is a **source-level proof**, confirmed by a re-run rather than replaced by one.
Those five sites are also what discharges `F11`: the retained reading is not an accessor nothing calls.

**No fourth-level consumer moves, and that is derived rather than asserted.**
Every reader of the three affected files selects the `10 × 6` half explicitly:

| reader | reads | selector |
|---|---|---|
| `T-263` | `T-253` `flatness` | `crossSection == "10 x 6"` |
| `T-284`, `T-291` | `T-254` `fields` | the `10 × 6` enhancement `21.1851817` |
| `T-303` | `T-297` `cells` | the `cells` block carries no `15 × 4` record |
| `T-315` | `T-297` `sweep` | `crossSection == "10 x 6"` |

**Re-emission order** (`tools/reemission-order.py`, and the two edges it knows plus the two the stale committed census does not): `T-253 → T-254 → T-297`, with `T-294` re-run after `T-253` as a byte-identity control.
`CLAUDE.md` records a claim that re-emitted eleven files and ran a consumer before its own producer; the order is asked, not guessed.

---

## 4. Numeric targets

| | target |
|---|---|
| **P1** | `Σ_r (y_r − yDatum)` over the face beams is exactly `0` at even `m` and `−(m − 1)d/4` at odd `m`, at every `m` in `3…16` and at both `faceColumn` values — `−8.876 nm` at `m = 15`, `−6.34 nm` at `m = 11` |
| **P2** | the worst relative Gram off-diagonal is `0.0` at `m = 10` and `m = 14`, **`0.0358744468`** at `m = 15` and `0.0475958489` at `m = 11` — the first two reproducing `C-0218` |
| **P3** | a uniform pressure gives a **corrected** peak dishing below `1e−9` of the stroke at **every** `m` in `3…16`; the **retained** decomposition gives **`0.0620506254`** of the stroke on the tied `15 × 4` block, reproducing `C-0218` §7 |
| **P4** | wherever the face basis is orthogonal, the corrected and retained coefficient vectors are **bit-identical**, over at least four load cases (uniform, `C-0022`'s solved collar, a face point load, a bond prestrain response) |
| **P5** | the corrected `15 × 4` free tiles at `C-0154`'s own `112 bp` row and its three enhancements `1.0 / 9.65079217 / 12.7228458` reproduce `C-0218`'s **`0.242196276 / 0.157167743 / 0.150056485`** to `< 1e−9` relative |
| **P6** | the retained readings at the same three cells reproduce `C-0154`'s published `0.312237799 / 0.227177955 / 0.220064299` to `< 1e−9` relative, so the defect stays measurable in the shipped class (`C-0092`) |
| **P7** | all three corrected readings remain **outside** `T-5b`'s `0.10`, so **no verdict moves** at either cross-section and at any enhancement |
| **P8** | the blast radius: **18** grillage-emitting result files, **15** provably unmoved, **3** re-emitted, **0** fourth-level consumers moved |
| **P9** | the `CH-0282` §5 residue — the class **fits** with `faceFunctional`'s owning-beam reconstruction and **samples** with `evaluate`'s nearest-beam one — measured, as the relative difference between the dual-pairing and the `areaInnerProduct` right-hand sides, at **both** cross-sections. `CH-0282` records it as *"under `1e−3`"* at `10 × 6` and gives it no number at `15 × 4` |
| **P10** | the cost of **not** branching: the largest relative movement an unbranched adoption would inflict on an `m = 10` reading, over the same four load cases — the number that says whether the branch is load-bearing or cosmetic |

---

## 5. Falsifiers, declared before the run

| | falsifier | state |
|---|---|---|
| **F1** | the corrected and retained coefficients differ at **any** orthogonal-basis lattice — then the branch is wrong and the 15-file partition is void | **declared CLOSED**; a departure is a defect in the repair |
| **F2** | the quadrature Gram's off-diagonals are **not** exactly `0.0` at even `m` — then an unbranched adoption would perturb every even-`m` reading at the last ulp and `P10` is the price of the branch | **OPEN** |
| **F3** | the dual-pairing correction and the `areaInnerProduct` correction differ by more than `1e−2` relative at `15 × 4` — then `CH-0282` §5 is larger than recorded and is challenged as `CH-0284` | **OPEN** |
| **F4** | any corrected `15 × 4` reading crosses `T-5b` — then a **verdict** moves, and the two deliverables need more than a number swap | **OPEN** |
| **F5** | `P5` fails — then this implementation and `T-294`'s `FaceRigidBasis` disagree, and one of the two is wrong | **declared CLOSED** |
| **F6** | any `m = 10` result file re-run as a control is **not** byte-identical — then *"provably unmoved"* is false. At least three controls, chosen for cost, plus `T-294` | **OPEN** |
| **F7** | `faceRigidModesAreOrthogonal` is not exactly `m % 2 == 0` at every `m` in `3…16` and both `faceColumn` values — then the parity statement is wrong | **OPEN** |
| **F8** | **the strengthened falsifier**: the decomposition fails to annihilate each of its **own three basis modes** exactly, at any `m`. This needs **no solve at all** and is the test that would have caught `CH-0282` eleven iterations ago | **OPEN** |
| **F9** | two independent emissions of `T-330` are not byte-identical (`CH-0281`: a departure this study emits is a `reproductionDeparture`, and `DEPARTURE_DIGITS_BY_KEY` must reach it) | **OPEN** |
| **F10** | any mutation of the new code survives every named test, over a subtracted baseline (`CH-0237`), with the harness registered in `tools/P-31-harness-census.py` and wired in `build.gradle.kts` | **OPEN** |
| **F11** | the retained accessor is **unreachable** from any consumer — a defect retained behind an API nothing calls is not measurable, and `C-0092` asks for measurable | **OPEN** |

---

## 6. Method, and its justification against cost

### 6a. The repair

`HoneycombGrillage` gains, all lazy and all built once per lattice:

- `faceRigidModesAreOrthogonal: Boolean` — the **exact** antisymmetry of `beamY` over `faceBeams`. Not a tolerance: an exact float comparison of the geometric premise.
- `faceRigidGram: List<List<Double>>` — `G[0][0] = area`, `G[1][1] = tiltSNorm`, `G[2][2] = tiltYNorm`, off-diagonals from the existing duals, so that at a diagonal Gram the elimination reproduces the three standing quotients **exactly**.
- `worstFaceNonOrthogonality: Double` — the diagnostic `CH-0282` tabulates.

`HoneycombDeflection.dishingCoefficients` becomes: the standing three projections where the basis is orthogonal, and a `3 × 3` least-squares solve where it is not.
The three-projection reading is **retained** as `independentProjectionDishingCoefficients` / `independentProjectionDishing(s, y)` / `independentProjectionPeakDishing(samples)` — `C-0092`, and `F11` requires that a consumer actually calls it.

**Which inner product the odd-`m` right-hand side is taken in is a decision and it is made deliberately.**
The three modes reconstruct **identically** under both conventions — `evaluate` and the owning-beam reconstruction both return exactly `1`, `s` and `y` — so the **Gram is the same object either way** and only the right-hand side differs.
`dishing()` and `peakDishing()` both sample through `evaluate`, so the fit that minimises the quantity the class **reports** is the `areaInnerProduct` one, which is also `T-294`'s and therefore reproduces `C-0218` exactly (`P5`).
The dual-pairing fit is the one **consistent with `assembleLoad`'s own quadrature**.
`T-330` adopts the `areaInnerProduct` right-hand side at odd `m`, emits the dual reading beside it, measures the gap at both cross-sections (`P9`), and **refuses** to change the even-`m` convention — that is `CH-0282` §5's question, it moves all eighteen files, and it is filed rather than taken.

### 6b. Why not simply adopt the least-squares fit everywhere

Because it is not free and the price is measurable before it is paid.
Adopting the `areaInnerProduct` fit unconditionally changes every `m = 10` reading by the `CH-0282` §5 residue — recorded there as under `1e−3` relative — and `C-0180`'s tightest recovered cell clears `T-5b` by **`0.426 %`**.
A `0.1 %` perturbation does not obviously cross that, and *"does not obviously cross"* is not a verdict: it would put eighteen result files, six claims and two deliverable passages in play for a change that is a **convention** rather than a defect.
The branch confines this task to the defect `CH-0282` actually raises, and `P9`/`P10` publish what the deferred half would cost.

### 6c. The study

A new `tile/FaceRigidBasisStudy.kt` emitting `gpd/results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json`, with a hand-added `ResultInputs.T_330` handle (**never** through `tools/T-272-emit-result-inputs.py`, which reads the git index).
Its blocks: the closed-form parity census over `m = 3…16` and both `faceColumn` values; the Gram and its worst off-diagonal at each; the uniform-pressure falsifier in both conventions at each; the three `C-0154` cells in all three readings with their departures against `C-0218` and against `C-0154`; the `§5` residue at both cross-sections; and the unbranched cost `P10`.

Cost: the `m = 3…16` lattices are one helix per row and solve in milliseconds; the three `15 × 4` cells at `C-0154`'s `112 bp` row are the only real solves, and `T-253` already runs them.

### 6d. The re-emission

`T-253`, `T-254` and `T-297` in that order, then `T-294` as a byte-identity control, each through `tools/study.sh` so the copy-back is scoped to the file the run wrote.
Each is preceded by the corresponding retained-accessor edit where a study wants the standing reading kept beside the corrected one, and the three re-emitted files carry **both** readings at every affected record so the defect stays measurable in the artifact as well as in the class (`C-0092`).
`C-0154` is annotated in place — strike, never delete — with the corrected triple and a pointer to `C-0219`.

### 6e. What would falsify this approach as a whole

If `F2` shows the quadrature Gram is exactly diagonal at even `m` **and** `P10` shows the unbranched movement is below the ninth significant digit at every one of the four load cases, then the branch buys nothing and the honest repair is the unconditional solve — the class would then be simpler and the corpus still unmoved.
`P10` is what decides it, and it costs four solves on a lattice the study builds anyway.

Conversely, if `F4` fires — if a corrected `15 × 4` reading falls inside `T-5b` — then `CH-0282`'s *"no verdict moves"* is wrong, the cross-section ordering that `DECISIONS-FOR-NDI.md`'s decision 7 now stands on is in question, and the deliverable hand-off in §7 becomes a re-argument rather than a number swap.

---

## 7. The deliverable hand-off — reported, not edited

`ANSWERS.md` and `DECISIONS-FOR-NDI.md` are owned by the concurrent synthesis this iteration.
Exactly **four** passages of the two deliverables carry the affected triple `0.312237799 / 0.227177955 / 0.220064299`, and one claim does:

| file | line at `HEAD` `7145aba` | what it says |
|---|---|---|
| `ANSWERS.md` | 289 | *"on the honeycomb **grillage** the free tiles are **0.312237799 / 0.227177955 / 0.220064299** on `15 × 4`"* |
| `ANSWERS.md` | 1760 | *"`15 × 4`'s **free** tile is outside `T-5b` at **every** enhancement read — 0.312237799, 0.227177955 and 0.220064299"* |
| `DECISIONS-FOR-NDI.md` | 243 | decision 7, *"`15 × 4`'s FREE tile is outside `T-5b` at every enhancement read on that lattice (0.312237799 / 0.227177955 / 0.220064299)"* |
| `DECISIONS-FOR-NDI.md` | 1330 | *"on the honeycomb **grillage** `15 × 4` reads **0.312237799 / 0.227177955 / 0.220064299**"* |
| `C-0191` | 70 | the same triple, inside the thirteenth synthesis's own correction table |

In all five the **sentence survives and the numbers move**, because all three corrected readings are smaller and all three remain outside `T-5b` — the ordering the passages rest on is **strengthened**, not disturbed.
The hand-off is deliberate: `CLAUDE.md` records that *a discharge is invisible to whoever files the removal*.

---

## 8. Whether the standing falsifier should be strengthened, and what it costs

It should, and the strengthening costs no solve.

`CLAUDE.md`'s falsifier — *a uniform load on a uniform Winkler foundation produces no dishing at all* — is written on a **solve**, so it can only ever test the lattice it is handed, and `HoneycombGrillageTest`'s `F1` hands it `rows = 6` while the fixture default is `rows = 4`.
Both are even.
Eleven iterations of a correct falsifier ran on the favourable parity of the one thing it could not see.

Two strengthenings, both microseconds:

1. **Take the falsifier on the PROJECTOR, not on the solve** (`F8`): the decomposition must annihilate each of its own three basis modes **exactly**. That needs no load, no foundation and no factorisation — it is a property of the geometry alone, and it fires at odd `m` instantly.
2. **Run the solve falsifier at both parities**: a one-helix-per-row block at `m = 3` and `m = 11` costs milliseconds.

The general form, for `CLAUDE.md`: *a falsifier written on a SOLVE tests the lattice you hand it; the same statement written on the DECOMPOSITION tests the geometry, costs no solve, and cannot be handed a favourable case by accident.*
Whether to add a **generic** parity sweep to every geometric falsifier in the tree is `T-326`'s question, not this one's; the two named tests above are wired here.

---

## 9. Execute

Filed as [`C-0219`](../claims/C-0219-a-dishing-fit-and-the-parity-of-its-basis.md), which **RESOLVES** [`CH-0282`](../challenges/CH-0282-a-dishing-fit-assumes-an-even-raster-row-count.md) and raises [`CH-0284`](../challenges/CH-0284-a-fit-and-a-sample-in-two-reconstructions.md).
Result: [`gpd/results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json`](../results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json), written by `tile/FaceRigidBasisStudy.kt` on `tile/HoneycombGrillage.kt`.

**19 gate-named tests written first and watched fail** (`src/test/kotlin/tile/FaceRigidBasisTest.kt`), plus two in `HoneycombGrillageTest` and one in `CrossSectionTiedRegradeTest`.
**Six failed on their first real run and every one was informative** — see §10.

## 10. Verify — the ten targets and the eleven falsifiers

| | result |
|---|---|
| **P1** | met, **28 of 28** — and the sweep **generalised the closed form**: the sign is set by the **face column's own parity**, `−(m−1)d/4` at an even column and `+` at an odd one. A first pass carried the `faceColumn = 0` sign only and the two-column sweep found it |
| **P2** | met — `0.0358744468` at `m = 15` and `0.0475958489` at `m = 11`, reproducing `CH-0282`; below `1.0E-9` at every even `m` |
| **P3** | met — corrected uniform-load dishing below `1.0E-9` at all 28 readings; the retained convention reads `0.211764706` at its worst (`m = 3`) and `0.0620506254` at `m = 15` |
| **P4** | met — bit-identical over four load cases at `m = 4`, `6`, `10` |
| **P5** | met — `0.242196276 / 0.157167743 / 0.150056485`, reproducing `C-0218` §7 to `< 1e−8` |
| **P6** | met — `0.312237799 / 0.227177955 / 0.220064299` reproduced from the shipped class through the retained accessor |
| **P7** | met — all three outside `T-5b`; `anyVerdictMoves = false` |
| **P8** | met — **18 / 15 / 3 / 0** |
| **P9** | met — `4.3E-4`–`5.0E-4` on the collar, `4.7E-4` on a point load, **`0.0067`** on a bond prestrain |
| **P10** | met — and it is `P9`'s largest well-posed reading, `0.0067`, which is **outside** `C-0180`'s `0.426 %` margin |

| | falsifier | outcome |
|---|---|---|
| **F1** | corrected ≠ retained at an orthogonal basis | did not fire |
| **F2** | the quadrature Gram is not exactly diagonal at even `m` | **FIRED**, and its firing is the finding: the branch is taken on the **integer ladder** and is load-bearing |
| **F3** | the two right-hand-side conventions differ by more than `1e−2` at `15 × 4` | did not fire — at a non-orthogonal basis they are the same call |
| **F4** | a corrected `15 × 4` reading crosses `T-5b` | did not fire |
| **F5** | disagreement with `T-294`'s `FaceRigidBasis` | did not fire |
| **F6** | an `m = 10` control is not byte-identical | did not fire — **4 of 4** identical; `T-294` differs at 5 of 10 649, all of them `C-0218` §9's own declared-irreproducible field |
| **F7** | the orthogonality flag is not exactly `m` even | did not fire, **28 of 28** |
| **F8** | the projector fails to annihilate its own basis | did not fire |
| **F9** | two emissions not byte-identical | **FIRED on the first run**, at three fields, every one a comparison of two quantities meant to be zero; repaired and now byte-identical |
| **F10** | a mutation survives | did not fire — **13 mutations, 0 survivors** after two first-run survivors that were both findings |
| **F11** | the retained accessor is unreachable | did not fire — five call sites |

**The six first-run test failures, and what each was.**
Two were the author's assertion that a quadrature returns an exact zero (`F2`);
one was the `Double` antisymmetry predicate failing at `m = 4`, which forced the integer ladder;
one was a one-column probe being a mechanism under the banded solve;
and two were the branch not yet existing.
A seventh and an eighth appeared in **existing** tests: `T-294`'s uniform-field test had to be repointed at the retained accessor (`C-0092`), and `HoneycombGrillageTest`'s axial-pin test — at `rows = 5`, **odd** — turned out to have been comparing two noises relatively and passing for the wrong reason.

## 11. File — what this leaves for others

- **Four passages of the two deliverables** carry the corrected triple (§7); they are reported to the coordinator and handed to the synthesis, not edited here.
- `C-0154` is annotated in place, strike-never-delete, and its `10 × 6` readings are untouched.
- `CH-0284` and the `T-294` departure-key repair are filed as queue rows.
- **`P-31` caught six of `T-294`'s mutation anchors orphaned by this refactor**, and one of its mutations had become a **no-op** because the class default changed under the row; both repaired, and that harness re-runs at **19 mutations, 0 survivors**.
