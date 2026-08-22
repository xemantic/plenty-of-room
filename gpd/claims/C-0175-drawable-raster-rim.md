# C-0175 — **THE RAGGEDNESS IS STILL ON THE WRONG AXIS AT `7 / 14 bp`, AND A PRESTRAIN NEVER WAS — ONE LEVER ARM DECIDES BOTH, AND IT SAYS THE OPPOSITE THING IN EACH CASE.** `T-258`: the drawable `102 / 109` raster's relief is `7 / 14 bp = 2.38 / 4.76 nm`, present at **every** column of the cross-section, so the coefficient on §3's gap-facing flatness is **exactly zero at any relief** — a statement carrying no magnitude, which is why `1.75×` cannot move it. The residual rim bound moves `1.79816514×` and that is **two** factors (`7/4` in the relief, `112/109` in the row span), landing at **`9.96901722e−05`** of the stroke — but the **comparand `C-0147` quoted its `496×` against has been withdrawn** by `C-0167`, and against what still exists the margin is **`262.520141×`**. What did move is the edge field: the relief is now **`0.962489509`** of the slit's transverse decay length at 2 mM and a 5 nm gap, a reserve of `1.04×` where `C-0147` read 1.8–3.6×. `T-254`: a prestrain reaches `w` only through the covalent link, whose in-plane arm is `(d/2)û_y`, so the coefficient is zero **iff `û_y = 0`** — and the honeycomb's three azimuths give `|û_y| = 1` in plane and `1/2` through the thickness, **never zero**. So every raster turn is on the flatness axis, and the load is not the one the question was raised about: the drawable raster forces **nothing**, while **all 59** turns carry `C-0152`'s own allowed `8.57142857°`. Ceiling over every sign and every subset: **`0.0764244991`** against a free tile of `0.0446459684`, inside `T-5b`, at a departure margin of **`1.74×`**. And the 59 turn ties are covalent elements `C-0154`'s lattice does not carry: adding them stiffens the recommended block by **`1.12×`**

| | |
|---|---|
| **Task** | [`T-258`](../tasks/T-258-drawable-ragged-face.md) (raised by [`C-0151`](C-0151-closing-raster-selection.md) §9 item 2) and [`T-254`](../tasks/T-254-raster-turn-prestrain.md) (raised by [`C-0152`](C-0152-forced-scaffold-crossover-price.md) §7 and [`C-0154`](C-0154-honeycomb-grillage.md) §10) |
| **Leaf** | **`A8.2`** |
| **Verification type** | `T-258` **logical** — exact integer lattice arithmetic on the rise and on the honeycomb cross-section, plus three closed forms already in the corpus (the plate ripple transfer, the square wave's fundamental, the slit's transverse eigenvalue). **No solve, and that is a stated refusal**: no model here carries a per-helix row length. `T-254` **logical** (a lever-arm argument and an exact turn census, both before any matrix) **+ in-silico** (`C-0154`'s three-dimensional beam-and-bond lattice, a linear prestrain influence bank over all 59 turns, and a triangle-inequality ceiling) |
| **Verdict** | **PASS on all thirteen predicates.** `T-258`: `F1`, `F2`, `F5`, `F7` did not fire; `F3`, `F4`, `F6` were **declared open** and did not fire. `T-254`: `F1`, `F2`, `F3`, `F4`, `F7`, `F8` did not fire; **`F5` and `F6` were declared open and BOTH FIRED** — `F5` at 3 of 4 coupled `15 × 4` states, where the **free** tile already exceeds the tolerance, so the turns never *decide* it (`C-0154`'s own `F5`, read at the turns); `F6` because the 59 ties are worth `0.890395426`–`0.983417274` of the free-tile dishing against a convergence departure of `3.1e−8`, which is [`CH-0227`](../challenges/CH-0227-the-honeycomb-lattice-omits-the-rasters-own-turn-ties.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED on a folded object.** `k_θ` is `Gen1Tile`'s square-lattice-fitted constant; the departure is a lattice statement read through `C-0104`'s **rigid-duplex** roll mapping and is therefore a **ceiling**; the raster, the cross-section and the crossover lattice are derivations, not measurements |
| **Provenance** | [`gpd/results/T-258-drawable-ragged-face.json`](../results/T-258-drawable-ragged-face.json) (`structure.DrawableRaggedFaceStudyKt`, **new**) and [`gpd/results/T-254-raster-turn-prestrain.json`](../results/T-254-raster-turn-prestrain.json) (`tile.RasterTurnPrestrainStudyKt`, **new**). Models [`structure/DrawableRaggedFace.kt`](../../src/main/kotlin/structure/DrawableRaggedFace.kt) and [`tile/HoneycombRasterTurnTies.kt`](../../src/main/kotlin/tile/HoneycombRasterTurnTies.kt) (**both new files**), plus a **strictly additive** scaffold-tie extension to [`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt) whose default is an empty list — asserted bit-identical as a named test. `structure/HoneycombTurnLoop.kt`, `structure/HoneycombRasterTurnSense.kt`, `tile/HoneycombFaceLattice.kt`, `tile/FourLayerTile.kt` and `structure/RaggedFaceCostStudy.kt` were **read, not edited**. **30 gate-named tests written first and watched fail** — [`structure/DrawableRaggedFaceTest.kt`](../../src/test/kotlin/structure/DrawableRaggedFaceTest.kt) (**13**) and [`tile/HoneycombRasterTurnTiesTest.kt`](../../src/test/kotlin/tile/HoneycombRasterTurnTiesTest.kt) (**17**); three of the seventeen failed on the **first real run** and caught a live defect (§11). **The shared-source change is proved not to move its consumers by RE-RUNNING them, not by argument** (`CLAUDE.md`: a shared Kotlin source is a dependency edge and only the result-file one is derived): `T-267` reproduces its committed file **byte for byte**; `T-253` and `T-263` each differ in **exactly one field**, `/emission/lattice`, which is **absent** from both committed files and is the emission-header residue a concurrent agent is sweeping — **no numeric field of either moves**. The gate is asserted the way `CLAUDE.md` prescribes: the crossover **site set** and `assembleLoad` **bit-identical**, the solved field at `1e−10`. Both result files **BYTE-IDENTICAL across two independent JVM runs**. `check-result-file-hygiene.py` clean on `--prose`, `--departures` and `--saturated`; `check-kotlin-format-strings.py`, `check-markdown-tables.py`, `check-corpus-links.py`, `check-corpus-identifiers.py`, `check-challenge-index.py`, `check-queue-vocabulary.py` and `trace-answers.py` clean; `result-reader-census.py --check` clean with a note that `--emit` is owed once the iteration's studies are committed. A full `./gradlew test` on the final sources: **3 289 tests in 189 classes, 0 failures, 0 errors**. `check-entry-points.py` is red on this claim's two missing `TASKS.md` rows, which the coordinator owns |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.141947 pN·nm. Honeycomb `d` = **2.536 nm** (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp; crossover planes every **7 bp**, one pair per class every **21 bp**. Cross-sections **`10 × 6`** (recommended) and **`15 × 4`** (control), 60 helices each. Rasters **`102 / 109`** (drawable, `C-0151`) and **`112 / 108`** (`C-0140`, does not close); block extent **116 bp = 39.44 nm** at both. `k_θ` = 13.5294118 pN·nm/rad, `k_s` = 64.7058824 pN/nm, link penalty `1e4` pN/nm; hinge enhancements 1.0 / 18.4938242 / 21.1851817 on `10 × 6` and 1.0 / 9.65079217 / 12.7228458 on `15 × 4` (`C-0116`'s measured band plus the lattice's own lower bound). `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the **gap-facing face only**; §3's 100 pN over the face; `T-5b`'s **0.10**; 81 × 81 dishing grid; free-edge penalty **50×**; buffers 0.5 / 1.0 / 2.0 mM and gaps 5 / 7 / 10 nm for the edge field |
| **Consumes** | [`C-0147`](C-0147-honeycomb-turn-slack-and-ragged-face.md) (both channels, its 4 / 8 bp reading and its published bound, **reproduced from its result file**), [`C-0151`](C-0151-closing-raster-selection.md) (the drawable pair), [`C-0152`](C-0152-forced-scaffold-crossover-price.md) (the departures and the closure census, **read at run time**), [`C-0154`](C-0154-honeycomb-grillage.md) (the grillage, its free tile and its bond census, **reproduced**), [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (the re-grade that withdrew `T-258`'s comparand, and its uncoupled references, **reproduced**), [`C-0104`](C-0104-row-end-prestrain.md) (a prestrain is a load, and the influence-function trap), [`C-0140`](C-0140-honeycomb-raster-turn-sense.md), [`C-0142`](C-0142-coupled-cells-at-the-honeycomb-cross-section.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md), [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) |
| **Constrains** | **`C-0152`'s open question 3 and `C-0154`'s open question 3 are ANSWERED** — the raster turn does **not** inherit `C-0147`'s zero, and the coefficient is a distribution with a ceiling. **`C-0147`'s verdict is upheld at the drawable relief** and its margin restated at the current comparand. **Two challenges are raised.** [`CH-0227`](../challenges/CH-0227-the-honeycomb-lattice-omits-the-rasters-own-turn-ties.md) against `C-0154`'s bond list and, through it, `C-0167`'s uncoupled references; [`CH-0228`](../challenges/CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md) against `C-0152` §7 and `C-0154` §5's ten-site scope |

---

## The claim, in two lines

**`T-258`.** The relief grew by `1.75×` and the axis did not move, because an axis has no
magnitude; the *bound* grew by `1.79816514×`, and the thing it is compared against moved further
than the bound did.

**`T-254`.** No honeycomb bond has a zero in-plane lever arm, so a prestrain at a raster turn
reaches the flatness at every turn — and the prestrain the recommended design carries is
fifty-nine allowed crossovers, not ten forced ones.

---

## 1. `T-258` Deliverable 1 — the axis, re-taken rather than inherited

`C-0147` proved the coefficient of the raggedness on §3's gap-facing flatness is **exactly zero**,
on an argument about coordinates: the gap-facing surface is one **column** of the cross-section and
the relief changes where a helix **ends**, an `x` coordinate *in* that plane.

That argument carries no magnitude, so `1.75×` of relief cannot move it — **provided the premise
still holds**, and the premise is that no relief moves material off the gap-facing column. Read per
column at the drawable pair:

| raster | cross-section | front | rear | extent | front spread **by column** |
|---|---|---|---|---|---|
| **`102 / 109`** | `15 × 4` | **7 bp = 2.38 nm** | **14 bp = 4.76 nm** | 116 bp | `7, 7, 7, 7` |
| **`102 / 109`** | `10 × 6` | **7 bp = 2.38 nm** | **14 bp = 4.76 nm** | 116 bp | `7, 7, 7, 7, 7, 7` |
| `112 / 108` | `15 × 4` | 4 bp = 1.36 nm | 8 bp = 2.72 nm | 116 bp | `4, 4, 4, 4` |
| `112 / 108` | `10 × 6` | 4 bp = 1.36 nm | 8 bp = 2.72 nm | 116 bp | `4, 4, 4, 4, 4, 4` |

**Every column of the cross-section carries the identical relief.** The raggedness is the tile's
**rim** at the gap-facing column, at the buried columns and at the far one alike; no column loses
or gains a helix at any relief a two-length raster can carry. **So the coefficient is exactly zero
at 4 bp, at 7 bp and at every relief, and `F1` did not fire.** `C-0147`'s 4 / 8 reproduce at
departure `0.0` at both cross-sections (`F2` did not fire), and the 7 / 14 is invariant under
swapping which row length takes which turn sense (`F7` did not fire).

## 2. `T-258` Deliverable 2 — the bound, and it is TWO factors

| raster | cross-section | `ℓ_across` | `λ` | transfer | × 50 | rim lever | **bound** | ÷ the 4 bp reading |
|---|---|---|---|---|---|---|---|---|
| **`102 / 109`** | `15 × 4` | 17.2310927 | 7.608 | 2.43837603e−05 | 1.219e−03 | **0.0817676772** | **9.96901722e−05** | **1.79816514** |
| **`102 / 109`** | `10 × 6` | 23.2114857 | 7.608 | 7.40538369e−06 | 3.703e−04 | 0.0817676772 | **3.02760512e−05** | 1.79816514 |
| `112 / 108` | `15 × 4` | 17.2310927 | 7.608 | 2.43837603e−05 | 1.219e−03 | 0.0454728409 | 5.54399427e−05 | 1.0 |
| `112 / 108` | `10 × 6` | 23.2114857 | 7.608 | 7.40538369e−06 | 3.703e−04 | 0.0454728409 | 1.68371917e−05 | 1.0 |

`C-0147`'s two published bounds reproduce **exactly** in the bottom two rows, and its across-helix
bending lengths with them.

**The move is `1.79816514×` and not `1.75×`, and the difference is the finding in miniature**: `7/4`
comes from the relief and `112/109` from the **row span**, because the drawable raster's rows span
109 bp where `C-0140`'s span 112. Only the **rim lever** contains the relief; the ripple transfer,
the 50× free-edge penalty and the modulation wavelength contain none of it — and the wavelength is
**2 raster rows = 7.608 nm at both rasters** (**`F3` did not fire**), because a period is a property
of the turn-sense alternation and not of a length.

## 3. `T-258` Deliverable 3 — the comparand `C-0147` quoted `496×` against has been WITHDRAWN

`C-0147` divided its headroom by its bound against `C-0142`'s tightest coupled cell still flat at
the 90th percentile, `0.0973238201`. [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md)
has since re-graded **every** cell of that family on the honeycomb grillage and finds **`0` of
`64`** inside `T-5b`. **That state is no longer a state of this design**, and saying so is what this
deliverable is for.

| comparand | model | value | relative headroom | **margin over the bound** | |
|---|---|---|---|---|---|
| `C-0142`'s tightest cell flat at `p90` | smeared equivalent sheet | 0.0973238201 | 0.0274976866 | **275.831468×** | **WITHDRAWN** |
| `C-0167`'s tightest cell flat with **no defects** | honeycomb grillage | 0.0974496759 | 0.0261706781 | **262.520141×** | live |
| `C-0167`'s **uncoupled** four-layer tile | honeycomb grillage | 0.0522223659 | 0.914888349 | **9177.31736×** | live |

**`F4` was declared open and did not fire, at a worst standing margin of `262.520141×`.** The
raggedness is not a flatness term at the drawable relief either, and the reason it is not is still
the axis and not the size.

## 4. `T-258` Deliverable 4 — what DID move, and it is the edge field

| buffer | gap 5 nm | gap 7 nm | gap 10 nm |
|---|---|---|---|
| 0.5 mM | 0.806776 | 0.614056 | 0.481245 |
| 1.0 mM | 0.861812 | 0.684761 | 0.568709 |
| **2.0 mM** | **0.962489509** | 0.807814 | 0.712106 |

*(the relief ÷ the slit's transverse decay length `1/q₀`; `resolvable` is false at all nine.)*

**`F6` did not fire** — the rim still wanders by less than the distance over which its own
perturbation dies, so *a ragged rim is a straight rim at its mean* as far as `C-0022`'s collar is
concerned. But the reserve is **`1.04×`** at 2 mM and a 5 nm gap where `C-0147` read **1.8–3.6×**,
and the relief has crossed `C-0005`'s **1.46 nm** gap resolution, which `C-0147` could quote as a
second, independent reason to call the relief invisible and this claim cannot. **Neither changes a
verdict; both remove a reserve.**

---

## 5. `T-254` Deliverable 1 — the cheap bound is a LEVER ARM, and it needs no solve

A prestrain's work conjugate is a **roll**. A roll reaches the deflection field only through the
covalent **link**, which constrains `w_a + a_y φ_a − w_b + a_y φ_b` with `a_y = (d/2)û_y` the
in-plane component of the bond's own half-vector. **So the coefficient is exactly zero if and only
if `û_y = 0`** — a tie stacked purely through the thickness would roll its two duplexes against
each other and lift neither.

The three bonds of a honeycomb site are at `90/210/330°` (sublattice A) or `270/30/150°` (B), so

> `|û_y| = 1` for the in-plane bond and `1/2` for each of the two through-thickness ones.
> **No bond and no turn tie has a zero in-plane arm; the least over both cross-sections is `0.5`.**

**`F1` did not fire, and the answer to *"does a raster turn sit on the flatness axis at all"* is
YES, at every turn, before a matrix is assembled.** `C-0147`'s exact zero does **not** transfer, and
the reason is a difference of *kind*: a raggedness is a **geometry** on an orthogonal coordinate,
and a prestrain is a **load** — a load is not confined to the coordinate it is applied on.
`C-0104`'s own subject is a row-end prestrain, and it moves a flatness verdict across `T-5b`.

## 6. `T-254` Deliverable 2 — the census, and it names which interfaces the scaffold loads

| cross-section | turns | through the thickness | in plane | at the two rims | lattice bonds | in-plane interfaces | interlayer interfaces | turns joining two gap-facing helices |
|---|---|---|---|---|---|---|---|---|
| **`10 × 6`** | **59** | **50** | **9** | 30 / 29 | **435** | 27 | **50** | **5** |
| `15 × 4` | 59 | 45 | 14 | 30 / 29 | 410 | 28 | 45 | 7 |

Within a raster row the path steps through the block's **thickness**; at a row transition it steps
**in plane**. So on `10 × 6` the raster's within-row turns are **every one** of the 50 interlayer
interfaces `C-0154` counts, and its row transitions are 9 of the 27 in-plane ones. `C-0154`'s bond
and interface counts reproduce at departure `0.0`, and the turn kinds sum to `H − 1` at both
cross-sections (**`F2` did not fire**).

## 7. `T-254` Deliverable 3 — the load, and it is NOT the one the question was raised about

`C-0151` shows `112 / 108` does not close on caDNAno's `±5 bp` rule and `102 / 109` does, with
**zero** forced crossovers — read here out of `C-0152`'s own closure census, `0` at `102 / 109`
against `10` at `112 / 108` (**`F7` did not fire**). **So on the recommended design `C-0152`'s
forcing load does not exist.**

What does exist is `C-0152` §5's own calibration, read as a load instead of as a calibration:
caDNAno's `±5 bp` is an integer approximation to a `5.25 bp` half turn, so an **allowed** scaffold
crossover already sits **`8.57142857°`** off the line of centres — at **every** raster turn, on
**every** raster, forced or not. That is [`CH-0228`](../challenges/CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md).

## 8. `T-254` Deliverable 4 — the coefficient, as a distribution and as a ceiling

At `10 × 6` and the calibrated coupling, unit responses over all 59 turns, taken on
`withoutPrestrain` (**`F4` did not fire**):

| | peak dishing per radian |
|---|---|
| **largest single turn** | **0.012534868** |
| median | 0.00242282813 |
| smallest | 1.4132248e−04 |
| largest among the **15** turns touching the gap-facing face | **0.012534868** |
| largest among the 44 that do not | 0.00755291522 |

**A turn that touches the gap-facing face is worth `1.66×` the best buried one**, and the single
largest is an **in-plane** turn. At the allowed departure the largest single turn is worth
`0.00187521186` of the stroke.

| cross-section | coupling | free tile | Σ unit | **ceiling, all 59 at 8.57142857°** | inside `T-5b` | departure that would reach 0.10 |
|---|---|---|---|---|---|---|
| **`10 × 6`** | **`f = 0.30`** | **0.0446459684** | 0.21242383 | **0.0764244991** | **YES** | **14.9303041°** (`1.74×`) |
| `10 × 6` | `f = 0.26` | 0.0467367262 | 0.231892761 | 0.0814278021 | **YES** | 13.1602245° (`1.54×`) |
| `10 × 6` | none | 0.12738041 | 1.78362279 | 0.394209756 | no | none — the free tile already exceeds it |
| `15 × 4` | `f = 0.30` | 0.220086801 | 0.618565219 | 0.312623942 | no | none — the free tile already exceeds it |
| `15 × 4` | `f = 0.26` | 0.227094793 | 0.747795142 | 0.338964685 | no | none — the free tile already exceeds it |
| `15 × 4` | none | 0.31116115 | 3.76536474 | 0.874458398 | no | none — the free tile already exceeds it |

The ceiling is a **triangle inequality** on a convex seminorm, so it bounds every sign assignment
and every subset. **`F5` was declared open and FIRED, at 3 of 4 coupled `15 × 4` states — and at
every one of them the FREE tile already exceeds the tolerance**, so the turns never *decide* the
verdict. That is `C-0154`'s own `F5` reached at a different site set, and it is the favourable
reading.

Adding the ten forced crossovers' **excess** on top of the 59 allowed ones — the `112 / 108` case —
takes the `10 × 6` ceiling to **`0.090670608`**, still inside `T-5b`.

**The realised field is `0.60` of the ceiling.** The sign of each turn's departure is fixed by no
source in this repository, so it is swept:

| sign assignment | `10 × 6`, `f = 0.30` | as a fraction of the ceiling |
|---|---|---|
| every turn the same way | **0.0460995878** | 0.603204318 |
| alternating along the raster path | 0.0457993778 | 0.599276126 |
| by the rim the turn sits at | 0.0457993778 | 0.599276126 |

A **0.7 %** spread: no assignment in that family cancels, and all three are inside `T-5b`.

## 9. `T-254` Deliverable 5 — the 59 ties are elements the corpus's lattice does not have

`C-0154`'s bonds are the **staple** ladder; a raster turn sits at `s = ±L/2`, past the last of them,
and it is a covalent crossover like any other. Adding them:

| cross-section | coupling | no ties | 59 ties | ratio |
|---|---|---|---|---|
| **`10 × 6`** | `f = 0.30` | **0.0501417316** | **0.0446459684** | **0.890395426** |
| `10 × 6` | `f = 0.26` | 0.0522223659 | 0.0467367262 | 0.894956124 |
| `10 × 6` | none | 0.132443428 | 0.12738041 | 0.961772226 |
| `15 × 4` | `f = 0.30` | 0.22389874 | 0.220086801 | 0.982974723 |
| `15 × 4` | `f = 0.26` | 0.23097815 | 0.227094793 | 0.983187342 |
| `15 × 4` | none | 0.316408058 | 0.31116115 | 0.983417274 |

The untied column reproduces `C-0167`'s **own** uncoupled references, 0.0501417315 and
0.0522223659, at `1e−9`. **`F6` was declared open and FIRED**, at `1.12×` on the recommended
cross-section against a convergence departure of `3.1e−8`. That is
[`CH-0227`](../challenges/CH-0227-the-honeycomb-lattice-omits-the-rasters-own-turn-ties.md), and it
is `C-0099`'s square-lattice `56 = 42 + 14` read on the honeycomb, where the split is `435 + 59`.

---

## 10. The five verification gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a relief is a base-pair **count** and is quoted in rises and nm; a square wave enters a sinusoidal transfer through its fundamental `2A/π`; every tie is asserted **exactly** one lattice constant long in the emitted `(y, z)`; angles folded once at the API and converted to radians at the load; `½k_θθ²` in pN·nm/rad × rad² = pN·nm; both models **refuse** a non-positive relief, a non-adjacent beam pair and a node outside the beam | **PASS** |
| **2 — limiting cases** | a **uniform** row length leaves both faces flat (0 bp, extent 112); a zero relief bounds the flatness move at **exactly** `0.0`; an **empty** tie list leaves the lattice bit-identical in its bond count and in a solved field to `1e−14`; a unit tie response equals the field of one radian at that tie alone to `1e−9` | **PASS** |
| **3 — symmetry, conservation and the standing falsifier** | **a uniform pressure on the TIED lattice dishes `0.0`** — `CLAUDE.md`'s sharpest falsifier, re-run with 59 covalent ties added (**`F3` did not fire**); a tie prestrain changes **no** entry of the stiffness matrix, sampled band-entry by band-entry; the field is exactly **linear** in the prestrain to `1e−9`; adding the ties cannot **soften** the block under a fixed load, which is a Loewner statement and not a measurement; both length-to-sense assignments give the same spreads; the turns alternate rims and the first axial sign swaps them at all 59 | **PASS** |
| **4 — numerical convergence** | `T-258` has **no** mesh and no sampling — every quantity is a closed form and exact integer arithmetic, and the convergence gate is discharged as exhaustion over the family. `T-254`: nested beam subdivisions 1 / 2, departure **`3.1e−8`** on the largest unit response; the dishing sample grid 41 / 81 / 161, departure **`0.0`**. Both result files **byte-identical across two independent JVM runs** | **PASS** |
| **5 — literature and upstream** | **eighteen reproductions.** `T-258`: `C-0147`'s two published bounds and its two across-helix bending lengths (`< 1e−6`), its 4 / 8 bp at both cross-sections, `C-0151`'s 7 bp relief and 116 bp extent, `C-0167`'s `0 of 64`. `T-254`: `C-0154`'s free tile **0.0449400126** (`2.4e−10`), its 435 / 410 bonds and 27 + 50 / 28 + 45 interfaces, `C-0152`'s **17.1428571°** and **8.57142857°** and its 10-vs-0 forced census, `C-0167`'s two uncoupled references (`1e−9`), and the uniform-load falsifier at `3e−12`. Every closed form — the ripple transfer, the transverse eigenvalue, the collar, the roll mapping — is the corpus's **own** function, called rather than re-implemented | **PASS** |

### The thirteen declared falsifiers

| # | task | falsifier | fired? | outcome |
|---|---|---|---|---|
| `F1` | `T-258` | the drawable relief moves material off the gap-facing column | **no** | present at 6 of 6 columns; the relief is the rim at every column |
| `F2` | `T-258` | the model fails `C-0147`'s 4 / 8, or fails to return 7 / 14 | **no** | both, at both cross-sections, bound reproduced below `1e−6` |
| `F3` | `T-258` | the modulation wavelength moves with the relief — **declared open** | **no** | 2 raster rows at both rasters; a period carries no length |
| `F4` | `T-258` | the bounded move exceeds the headroom of any standing flat state — **declared open** | **no** | worst margin `262.520141×` |
| `F5` | `T-258` | the relief falls below the 0.34 nm quantum | **no** | 7 and 14 whole rises |
| `F6` | `T-258` | the relief is resolvable by the slit's transverse decay | **no** | worst `0.962489509`, and the reserve has fallen to `1.04×` |
| `F7` | `T-258` | the two length-to-sense assignments give different spreads | **no** | invariant at both cross-sections |
| `F1` | `T-254` | some honeycomb bond has `û_y = 0` | **no** | least `\|û_y\|` is `0.5`; **its not firing IS the finding** |
| `F2` | `T-254` | the turn census fails the path, the sum or the bond test | **no** | 59 = 50 + 9 and 45 + 14; the order reproduces the path under the **column mirror** |
| `F3` | `T-254` | the ties move the uniform-load dishing off zero | **no** | `0.0` at 81 × 81 with 59 ties |
| `F4` | `T-254` | the field is not linear, or an influence is contaminated by the prestrain | **no** | two named tests; every influence on `withoutPrestrain` |
| `F5` | `T-254` | the turn-set ceiling exceeds `T-5b` — **declared open** | **FIRED** | 3 of 4 coupled `15 × 4` states, and at every one the **free** tile already exceeds it |
| `F6` | `T-254` | the 59 ties move the free tile by more than the convergence departure — **declared open** | **FIRED** | `1.12×` against `3.1e−8`; that is `CH-0227` |
| `F7` | `T-254` | the drawable raster forces crossovers, or `112 / 108` forces none | **no** | 0 and 10, from `C-0152`'s own census |
| `F8` | `T-254` | the largest single-turn coefficient is below the solve residual | **no** | `0.012534868` per radian, four orders above it |

---

## 11. What the failing tests found, and it is a live defect

Three of `T-254`'s sixteen tests failed on their first real run, and one of them was not a test bug.

`honeycombXRasterPath` and `HoneycombBlock.position` use **opposite vertical-bond parities** —
`CLAUDE.md` records it — so the natural identification of a path cell's `x` with a block site's
`column` is a **mirror image** of the truth. Under it the raster's row transition joins two helices
**`5.072 nm = 2d`** apart, which the honeycomb does not bond at all; the model refused it with
*"a tie must join two beams exactly one lattice constant apart"*. The correct order traverses row
`r` as `n − 1 … 0` when `r` is even, which is the path under `c = n − 1 − x`.

**A census asserted against the bond graph catches it and a census asserted against the path does
not**, because in the path's own coordinates the step is a perfectly good bond. That is why the
`gate 3` test is written against `honeycombBondPairs` and not against `honeycombXRasterPath`.

## 12. Validity range, and what this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **A per-helix row length is not a parameter of any lattice model in this repository.** `T-258`'s
  flatness cost is therefore **bounded**, not measured, and the bound carries the 50× free-edge
  penalty for exactly that reason. `T-254`'s grillage carries one row length, so the two-length
  raster enters as its 116 bp block extent and a turn's axial position is right to within the
  stagger — which `T-258` shows has coefficient zero on the flatness.
- **A scaffold crossover sits 5 bp from a staple position**, so a turn tie's true axial station is
  within **1.7 nm** of the rim node it is modelled at. The influence bank is emitted **per turn**,
  so the sensitivity to that placement is readable rather than assumed.
- **The departure is read through `C-0104`'s rigid-duplex roll mapping**, which is what this corpus
  already uses for the same quantity. It is a **ceiling** on the true relative roll: a real
  crossover absorbs part of its azimuth in backbone strain, and nothing here bounds that from
  below.
- **The sign of each turn's prestrain is undetermined.** Three assignments are swept; the ceiling
  bounds all of them and every subset, which is why the **ceiling** and not the field is the
  quotable number.
- **The lattice carries no across-helix parallel-axis term** (`C-0154`), so its `D_⊥` is the
  independent one and a **lower** bound; the bracket is run at three ends and the two carrying any
  interlayer coupling agree on every verdict.
- **The block is FREE.** No attachment coupling is applied, so every dishing number here is
  `C-0109`'s uncoupled reference and not a design.
- `k_θ` is `Gen1Tile`'s square-lattice-fitted constant, at a **scaffold** turn as at a staple
  crossover; no honeycomb measurement of either exists here.
- **`T-258`'s threshold is quoted at the state it is read at, and that state moved.** `C-0147`'s
  comparand was withdrawn by `C-0167` between the two claims; both readings are emitted and the
  withdrawn one is flagged in the result file.

## 13. Still open — named, not answered

1. **The coupled re-grade is owed on the tied lattice.** `C-0167` graded 64 cells on a block
   missing 59 covalent ties. A coupling changes the load path, so the `1.12×` measured on a free
   tile does not transfer — `C-0154`'s own composite fraction reads 0.2468 on the rigidity and
   0.9405 on the dishing.
2. **Whether the allowed `8.57142857°` is carried or relaxed.** The rigid-duplex reading is a
   ceiling; a folded scaffold crossover may absorb it in backbone strain or a local unstacking, and
   no measurement bounds it from below.
3. **What fixes the sign of each turn's prestrain.** It is a property of the raster's own turn
   senses and of which way each crossover is displaced, and no source in this repository states it.
4. **What a ragged rim does to `C-0022`'s collar at a `1.04×` reserve.** The comparison of lengths
   still passes; a 2-D solve on a **stepped** rim has never been run, and this is the first state
   where the two lengths are within four per cent of each other.
5. **Whether the same lever-arm census applies to the SQUARE lattice's row-end crossovers**, where
   `C-0104` measured the flatness effect but the arm was never counted.
6. **Which face §3's effort point sits nearer.** `C-0147` named it; the rear relief is now
   **4.76 nm**, twice the front's and `3.26×` `C-0005`'s gap resolution.
