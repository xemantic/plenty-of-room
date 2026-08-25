# C-0221 — **THE FIT/SAMPLE GAP IS A CLOSED FORM IN THE FACE'S OWN VERTICAL BONDS AND IT IS EXACT: `(d²/16)·Σ(Φ_upper − Φ_lower)` OVER THE `d`-GAPS, VERIFIED BELOW A DECLARED `1e-12` AT `28 OF 28` ROWS AND `252` READINGS — SO THE DISCREPANCY IS A BOND-HINGE COORDINATE, WHICH IS WHY `CH-0284`'s ALARM WAS PRICED ON A BASIS ELEMENT AND NOT ON A STATE.** A bare bond prestrain moves **`0.0501`** under the consistent convention and the two coupled cells `C-0180`'s verdict rests on — whose banks are built out of exactly such responses — move **`0.00143`**, a factor of **`35.0`**, reproducing `C-0180` at **`3.5E-10`** with the verdict moving at **`0 of 2`**. **`CH-0284` §4 OFFERS TWO REMEDIES AND NEITHER IS THE CONSISTENT ONE**: the first is **not well posed** (the owning strips overlap by `d/2` and gap by `d/2`, so they are not a partition), and the second fixes the reconstruction while leaving the measure the overlapping, gapping tributary sum. The consistent one is a **third convention nobody named** — nearest-beam over the face **rectangle** — whose Gram is diagonal at **`28 of 28`** readings against the standing one's **`14`**, so it **DISSOLVES `CH-0282`'s parity rather than repairing it** and would make `C-0219`'s integer branch dead; on the piston projection the three are collinear in one scalar at exactly **`0 : 1 : 6`**, `5 of 5` at even `m`. **AND THE SHIPPED `areaInnerProduct` LAYS A 6-POINT GAUSS RULE ACROSS A STRIP `evaluate` IS DISCONTINUOUS INSIDE**, under-reporting the gap by a constant **`0.819693683`** at all `12` readings — so `CH-0284`'s own published channel sizes are **`1.21997×` low** (`CH-0285`). **AND THE MARGIN THE REFUSAL WAS PRICED AGAINST IS NOT THE CORPUS'S TIGHTEST**: over the eighteen files there are **`1 146`** verdict-bearing readings and the tightest is **`0.10000102`** at `T-294/cells/92`, **`417×`** tighter in stroke units than `C-0180`'s `0.426 %`, with **`99`** of them sitting closer to `T-5b` than the convergence departure `C-0180` measured on this very lattice. **THE CONVENTION IS RECOMMENDED AND NOT ADOPTED**: the code is **additive only**, `224` insertions and `0` deletions, `3 of 3` byte-identity controls, because an adoption without the eighteen-file sweep would leave every committed file unreproducible from its own code (`T-335`)

| | |
|---|---|
| **Task** | [`T-326`](../tasks/T-326-the-fit-and-the-sample-in-one-reconstruction.md) — the row [`CH-0284`](../challenges/CH-0284-a-fit-and-a-sample-in-two-reconstructions.md) §5 and [`C-0219`](C-0219-a-dishing-fit-and-the-parity-of-its-basis.md) (`T-330`) opened, which **priced it rather than took it** |
| **Leaf** | **`A8.2`** |
| **Verification type** | **logical** (a closed form in the face's own vertical bonds, derived before any code and checked against an exactly piecewise integration of both reconstructions at every raster-row count from 3 to 16 and both face columns, in two languages) **+ in-silico** (the three conventions measured at every channel `CH-0284` prices and at the two coupled cells `C-0180`'s verdict rests on, with a rigorous affine ceiling beside each, plus a margin census over the whole eighteen-file corpus) |
| **Verdict** | **PASS on all twelve predicates.** `P1`–`P12` all met. `F1`, `F2`, `F3`, `F4`, `F6`, `F8`, `F12`, `F13` did not fire; `F7` and `F11` were declared closed and hold. **`F5` was declared OPEN *and declared expected to fire*, and it DID NOT** — that is the finding of §4. **`F9` FIRED TWICE** and both firings are retained (§7). **`F14` FIRED**, at `6.33E-5`, moving no verdict. **`CH-0284` is ANSWERED and its own channel sizes are corrected**; **`CH-0285` is raised**; **`T-327` and `T-335` are opened** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Nothing here changes a **solve**: every field is the one the shipped lattice already produces and what differs is which rigid plane is removed from it. The recommendation to adopt convention C is a **modelling** argument plus a measurement of its cost; it is not taken in code |
| **Provenance** | [`gpd/results/T-326-the-fit-and-the-sample-in-one-reconstruction.json`](../results/T-326-the-fit-and-the-sample-in-one-reconstruction.json), written by [`tile/FaceReconstructionStudy.kt`](../../src/main/kotlin/tile/FaceReconstructionStudy.kt) (**new**) on [`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt), which this task **owns** this iteration. Shared sources edited: `tile/HoneycombGrillage.kt` (**224 insertions, 0 deletions** — every accessor is new and nothing existing is repointed, which `git diff --numstat` states and three byte-identity controls confirm), `structure/ResultInputs.kt` (a `T_326` handle **by hand**, never through the generator; `ResultInputs.all` is read at 8 sites, every one in `structure/ResultInputsTest.kt`), and `tools/P-31-harness-census.py` (one `HARNESSES` row, inserted after `T-330`'s and textually disjoint from the concurrent agent's own addition at the end of the tuple). **16 gate-named tests written first and watched fail** — [`tile/FaceReconstructionTest.kt`](../../src/test/kotlin/tile/FaceReconstructionTest.kt) (**new**) — all 16 passing on their first real run, which is unusual here and is explained by the Python prototype having predicted every one of their constants before a line of Kotlin existed (§1). **Mutation-tested** by [`tools/T-326-mutation-test.py`](../../tools/T-326-mutation-test.py) (**new**, registered in `tools/P-31-harness-census.py`): **12 mutations, 0 survivors, 0 broken** over a subtracted baseline of 16 executed tests and 0 named failures (`CH-0237`) — after a first run whose one *survivor* was a **syntax error** the harness could not tell from a survivor, which is §7's second half. Result file **byte-identical across two independent emissions** after the two repairs `F9` forced; both fired pairs are retained in [`gpd/data/T-326-reproducibility/`](../data/T-326-reproducibility/README.md) with an account, so the firings stay checkable (`C-0092`). The cheap bound is retained and runnable with no JVM in [`gpd/data/T-326-cheap-bound/`](../data/T-326-cheap-bound/README.md). Byte-identity controls: `T-253`, `T-267`, `T-263`, **3 of 3 identical**. `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-result-path-references.py`, `check-cold-start-note.py`, `check-kotlin-format-strings.py`, `cli_guard.py --check`, `T-334-gate-census.py --check`, `P-31-harness-census.py --check` (`wired: 31 of 31`), `trace-answers.py` and `check-result-file-hygiene.py` (base, `--prose`, `--departures`, `--saturated`) are all clean. **`build.gradle.kts` carries one additive registration** for the harness, in a hunk disjoint from the concurrent agent's: `T-334`'s new reachability arm requires `P-31`'s BY-HAND set to **equal** the set of `Exec` tasks unreachable from `:test`, in both directions, and a BY-HAND declaration with no task at all fails it — which the first full run duly caught. A full `tools/verify.sh` on the final tree is recorded in the journal |
| **Conditions** | T = 300 K, aqueous **2 mM MgCl₂**, `k_BT = 4.142 pN·nm`. Honeycomb `d` = 2.536 nm (SAXS); in-plane raster-row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp. Face = the gap-facing column, `faceColumn = 0`; the owning tributary is one row pitch centred on each face beam's own axis. Parity and closed-form probes on `m × 2` blocks at 42 bp; the channels on `C-0154`'s own **112 bp** row at both cross-sections and its three enhancements each; the deciding cells on the tied `10 × 6` block at **116 bp**, enhancement `21.1851817`, seed `197197`, **4 000** realisations, `C-0087`'s measured incorporation, `C-0017`'s mandate rim-graded 5:1, `81 × 81` dishing grid, `T-5b`'s `0.10`. `HoneycombGrillage.QUADRATURE_POINTS = 6` |
| **Consumes** | [`CH-0284`](../challenges/CH-0284-a-fit-and-a-sample-in-two-reconstructions.md) — the question, its four channels and its two proposed remedies, all three re-measured; [`C-0219`](C-0219-a-dishing-fit-and-the-parity-of-its-basis.md) (`T-330`) — the parity branch, the retained accessor and the `15 × 4` triple, **reproduced**; [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) — the two recovered coupled cells and the `0.426 %` margin, **reproduced at `3.5E-10`**; [`C-0154`](C-0154-honeycomb-grillage.md) — the six free tiles, **reproduced**; [`C-0218`](C-0218-the-tied-regrade-at-the-other-cross-section.md); [`C-0022`](C-0022-tile-edge-load-profile.md); [`C-0087`](C-0087-position-dependent-staple-dropout.md); [`C-0092`](C-0092-large-rotation-arm-branch.md) — *a repair must leave the defect measurable* |
| **Constrains** | **`CH-0284` is ANSWERED**: its question is settled on the argument, its first remedy is refused as not well posed, its second is shown not to be the consistent one, and **its own published channel sizes are corrected by `1.21997×`**. **`CH-0285` is raised** against `areaInnerProduct`'s quadrature. **No committed number moves** — `C-0154`, `C-0167`, `C-0180`, `C-0208`, `C-0211`, `C-0212`, `C-0215`, `C-0216`, `C-0218` and `C-0219` are all untouched, by construction and by control. Two queue rows open: **`T-335`** (adopt convention C and sweep the eighteen files) and **`T-327`** (§5: the corpus's flatness census cannot resolve a verdict inside its own convergence departure, and `99` of `1 146` are inside it) |

---

## 1. THE CHEAP BOUND — a closed form in the face's own vertical bonds, derived before any code and exact

A honeycomb face's rooting helices sit at `y_r = r·p + ½d·[(r + faceColumn) even]`, so the gap sequence is `d, 2d, d, 2d, …` and the `d`-gaps are exactly the pairs a **vertical bond** joins (`verticalBondUp` is true on the lower member of each).

The owning strip of a face beam is `[y_r − p/2, y_r + p/2]` with `p = 3d/2`; the nearest-beam cell of the same beam is that strip **translated by `δ_r = ±d/4`**, the sign alternating with the corrugation. So each owning strip is split `5d/4` to its own beam and `d/4` to the partner across its own vertical bond. Summing a bond's **two** members, the `W` differences cancel identically and what survives is the bond's **relative roll**:

```
piston   <1, B(u)> - <1, A(u)>  =  (d^2/16) * SUM_bonds INT (phi_u - phi_l) ds
tiltS    <s, B(u)> - <s, A(u)>  =  (d^2/16) * SUM_bonds INT s*(phi_u - phi_l) ds
tiltY    <y, B(u)> - <y, A(u)>  =  SUM_bonds INT [ (d^2/16)((w_u - w_l) + ybar*(phi_u - phi_l))
                                                   - (d^3/32)(phi_u + phi_l) ] ds
```

**Verified in Python before a line of Kotlin was written**, against a direct exactly-piecewise integration of both reconstructions; **reproduced independently in Kotlin** at `28 of 28` rows and **252** readings, below the declared `1e-12` floor, and exactly zero on the two limiting cases (a pure piston and a pure `y`).

Three consequences, all free:

1. **The discrepancy is a BOND-HINGE COORDINATE.** The `d`-gap pairs *are* the face's covalent vertical bonds, whose hinge is the only thing resisting a relative roll — so the gap is small under any load the bonds resist and large under a load applied **to** a bond. That is a **mechanism** for `CH-0284`'s own channel split, and §4 is what it predicts.
2. **The leading order cancels.** For a smooth field `Φ ≈ ∂w/∂y` and the `O(d²·∂w/∂y)` term vanishes identically; what survives is `O(d³·∂²w/∂y²)`, so the relative gap is `(π²/12)(d/λ_y)²` in the dishing field's own across-face wavelength — `9.1E-4` at a face-scale half-cosine against a measured `6.1E-4` on the collar. `F4` did not fire.
3. **The movement is AFFINE, so a rigorous ceiling costs no solve.** A convention change moves only three coefficients, so the dishing moves by `−(Δc₀ + Δc₁s + Δc₂y)` and the peak cannot move further than that plane's supremum: `|Δc₀| + |Δc₁|L_s/2 + |Δc₂|L_y/2`. It holds at `8 of 8` channels and it is **tight** — `0.00365` against a measured `0.0036` at the collar.

## 2. `CH-0284` §4's FIRST REMEDY IS NOT WELL POSED, AND ITS SECOND IS NOT THE CONSISTENT ONE

The owning strips are `1.5d` wide on axes `d` and `2d` apart, so they **overlap by `d/2`** across every vertical bond and **gap by `d/2`** between them; their total measure is exactly the face width, which is what makes `CLAUDE.md`'s uniform-load falsifier exact. **They are therefore not a partition**, and `evaluate` cannot be made to use them: at a point in an overlap two beams own the field and at a point in a gap none does. The overlap-and-gap is deliberate and is not a defect to be repaired.

`CH-0284`'s second remedy — adopt `areaInnerProduct` — fixes the **reconstruction** and leaves the **measure** the overlapping, gapping tributary sum. The reported quantity is a supremum over the face **rectangle** of the **nearest**-beam reconstruction, so the fit consistent with it is that reconstruction integrated over that rectangle. Call it **C**.

## 3. CONVENTION C DISSOLVES `CH-0282` RATHER THAN REPAIRING IT

`∫s dA`, `∫y dA` and `∫sy dA` over a rectangle symmetric about its own centre are zero **whatever the corrugated ladder does**, so C's Gram is `diag(A, A·L_s²/12, A·L_y²/12)` identically:

| | diagonal at |
|---|---|
| the standing convention | **14** of 28 readings — the even `m`, which is `CH-0282`'s parity |
| convention C | **28** of 28 |

So under C the three independent projections are the least-squares fit again, **with no branch**, and `C-0219`'s integer parity statement becomes a fact about a convention nobody would then be using. For the piston projection the three conventions are collinear in one scalar — the summed bond relative roll — at exactly

```
A : B : C  =  0 : 1 : 6
```

at **5 of 5** even `m` and `faceColumn = 0`, the geometry every committed reading is taken at. At odd `m` and at `faceColumn = 1` the end beams break it and the ratio is field-dependent (`2.04`–`6.11`); that is emitted rather than asserted.

## 4. THE FINDING: `F5` WAS DECLARED EXPECTED TO FIRE AND DID NOT, BECAUSE AN INFLUENCE FUNCTION IS NOT A STATE

`CH-0284` priced its refusal on a **bare bond prestrain response**, `0.0067`, against `C-0180`'s `0.426 %` margin, and concluded that *"the prestrain reading is not [inside it], and a coupled surrogate's influence bank is built out of point-load and prestrain responses"*.

Measured at the two cells `C-0180`'s verdict actually rests on:

| | cell 69, the tightest recovered | cell 109 |
|---|---|---|
| `C-0180`'s committed `p90` | `0.0995744767` | `0.0998791032` |
| reproduced here, standing | `0.0995744767` | `0.0998791032` |
| convention B, split | `0.0995669036` | `0.0998479806` |
| convention C, sampled | `0.0994327658` | `0.0997358124` |
| relative movement under C | **`0.00143`** | **`0.0014`** |
| that cell's own margin | `0.00425523271` | `0.00120896835` |
| **verdict moves** | **no** | **no** |

Reproduction departure **`3.5E-10`** at both, so this is the same object `C-0180` measured and not a rebuild that resembles it.

**A bare bond prestrain moves `0.0501` under C and the coupled cells move `0.00143` — a factor of `35.0`.** The influence function is a **basis element**; a coupled state is a weighted sum in which the smooth free field dominates and the bonds sit near their unstrained relative roll, which is exactly what the closed form of §1 predicts. `CLAUDE.md`'s *an energy ceiling is not a field* and *a level is not a field*, met on a **basis element against a state**.

**One honest qualification, and it is the sharper reading.** At cell 109 the movement `0.0014` **exceeds** that cell's own margin `0.00121` — by `1.16×`. It does not move the verdict only because it is one-signed **downward**: both cells get *flatter*. Had the sign run the other way that verdict would have moved.

## 5. AND THE MARGIN THE REFUSAL WAS PRICED AGAINST IS NOT THE CORPUS'S TIGHTEST — `T-327`

Censused over the eighteen committed files carrying a `HoneycombDeflection` dishing, with the predicate emitted beside the count — every numeric leaf whose key ends `OverStroke` or contains `ishing`, in a JSON object that also carries at least one boolean, valued in `[0.09, 0.11]`:

| | |
|---|---|
| verdict-bearing readings | **1 146** |
| tightest | **`0.10000102`**, `T-294/cells/92/nominalCorrectedOverStroke`, `1.02E-5` relative |
| within `5.0E-4` — `CH-0284`'s collar channel | **2** |
| within `4.2724E-3` — the movement that would flip `C-0180`'s tightest cell | **96** |
| within `4.57E-3` — `C-0180`'s own beam-subdivision convergence departure | **99** |
| within `6.7E-3` — `CH-0284`'s prestrain channel | **126** |
| within `4.02E-2` — that channel at C's `6×` | **484** |

The corpus's tightest verdict-bearing reading is **`417×`** tighter in stroke units than the margin the challenge priced against, so **no channel is safe** and the decision could never have rested on the movement being small. `F6` did not fire: `8 of 8` channels move by more than `1.02E-5`.

**Its twin runs the other way and is independent of which convention wins**: `99` of the `1 146` sit **closer to `T-5b` than the convergence departure `C-0180` measured on this very lattice**. Those verdicts are not determined by the model at all, and the fit convention is one more term in the same bucket. That is `T-327`, and it is not a convention question.

## 6. `CH-0285` — a smooth rule across a discontinuous reconstruction

`integrateOverFace` lays one 6-point Gauss-Legendre rule across each whole strip, and `evaluate` jumps `d/4` inside each strip's end at every strip by construction. Measured against exact piecewise integration on the piston gap:

```
gauss6 / exact  =  0.819693683       at all 12 readings, six raster-row counts, both face columns
```

Constant, as it must be: both readings are linear functionals the bond pairing reduces to multiples of the same scalar. So `CH-0284`'s published `4.3E-4`–`5.0E-4`, `4.7E-4` and `0.0067` are **`1.21997×` low**. `F14` fired: the split quadrature moves `C-0219`'s committed `15 × 4` triple by `5.3E-5`–`6.33E-5` relative, and **no verdict moves**.

## 7. `F9` FIRED TWICE, AND THE SECOND FIRING IS A REFINEMENT OF `CLAUDE.md`'s OWN RULE

Two independent emissions differed, twice, at exactly one field each time — the worst departure between two quantities that are **exactly equal by construction**, i.e. pure ulp noise.

1. Emitted as a `Double` at two significant digits it read `4.2E-13` against `3.2E-13`.
2. **Emitted as an integer ORDER — `floor(log10(worst))` — it still moved**, `−18` against `−17`, because the noise crosses a decade.

So the standing rule *a quantity that is nothing but ulp noise must be emitted as a THRESHOLD, never as a value* needs one clause: **an ORDER is not a threshold either.** Only a **declared** constant and a boolean are stable. Both fired pairs are retained in `gpd/data/T-326-reproducibility/`; after the repair two emissions are byte-identical, diffed outside the study.

**And the mutation harness found the same blindness from the other side, in the same run.** `M06`'s first draft inserted `true || ` after a lambda arrow whose body opens with a `val` — a **syntax error** — and a harness reading only the exit code and the failure set recorded a build that ran **no test at all** as `SURVIVED`, with a plausible explanatory sentence beside it. `CLAUDE.md` already records the inverse (*a crash is not a named test, and an exit code cannot tell them apart*, on a crashed suite reading as a **kill**); this is the same fact on a build failure reading as a **survivor**. The cure is the same and it is two lines: count the testcases the run executed and report a row that executed zero as **`BROKEN`** rather than as a measurement. Repaired, the table is **12 mutations, 0 survivors, 0 broken**.

## 8. The decision, and why the code is left additive only

**Convention C is the right fit** — it reconstructs the field the way the answer is sampled, it integrates over the domain the answer's supremum is taken over, and it removes `CH-0282`'s parity as a fact about the geometry rather than as a branch. **It is not adopted here.**

An adoption without the sweep would leave eighteen committed files that their own code no longer reproduces, which is the defect `gpd/README.md`'s re-run rule exists to prevent, and `C-0219`'s partition proof does not transfer: its branch is exactly what made fifteen of them inert. The addition is therefore **224 insertions and 0 deletions**, `3 of 3` byte-identity controls, and the sweep is **`T-335`** — a far better-specified row than `T-326` was, because it now carries the convention, the closed form, the affine ceiling, the measured deciding cells and the census of what could move.

## 9. Validity range

- The closed form assumes the face gap sequence is `d, 2d, d, 2d, …`, which `HoneycombBlock.position` guarantees and a `require` in `faceVerticalBondPairs` asserts. A face off that ladder **refuses** rather than guesses.
- The `0 : 1 : 6` collinearity is exact for the **piston** projection at even `m` and `faceColumn = 0`. The `tiltY` gap is not proportional at all.
- The margin census is over the eighteen files' **committed state** and is dated by it. A reading whose verdict is written outside its own record is invisible to it.
- The deciding cells are **rebuilt**, because `T-279`'s placement and distribution helpers are private to its study. What says the rebuild is the same object is the `3.5E-10` reproduction, which is `F12`.
- The affine ceiling is rigorous for the peak dishing of a single field and superposes over a linear bank. **It is not a bound on a `p90` over a dropout ensemble**, which is why the deciding cells are measured rather than bounded — and why the corpus's other `1 144` verdict-bearing readings are **not** covered by anything here.
- `OrigamiGrillage`'s own decomposition is not examined. Its tributaries are uniform so the same defect cannot arise the same way, but that is an argument and not a measurement.

## 10. Open questions

- **`T-335`** — adopt convention C and sweep the eighteen files in `tools/reemission-order.py`'s topological order, with every marginal verdict re-read. The argument is closed; the cost is not.
- **`T-327`** — `99` of `1 146` verdict-bearing readings are inside the model's own convergence departure. What a flatness census can resolve is a question about the census, not about the convention.
- **`CH-0285`** — the quadrature, which would silently bias whichever convention is adopted.
- Whether the **load** quadrature should be split. It must not: `assembleLoad`'s integrand is a pressure field, which carries no jump, and its centred tributary is what makes the uniform-load falsifier exact.
