# C-0104 — **IT DOES MATTER, AND IT IS THE FIRST ROW-END UNKNOWN THAT DOES.** A crossover prestrain is an *initial stress*, so `½k_θ(Δφ − θ₀)²` leaves the stiffness matrix untouched and enters `C-0009`'s lattice as a **load vector** — which makes the whole `θ₀` axis one solve, and puts `T-5b`'s 0.10 at **15.45°** of uniform row-end prestrain. **The lattice's own register ladder reaches it**: `C-0090`'s recommended 34-root placement holds the convention at the 8 bp (±4.286°) and 16 bp (±8.571°) rungs and **loses it at 0.1013 at the 32 bp rung in the adverse sign**. The **design absorbs it** — re-running the 163 296-placement enumeration recovers 0.0826 and **0.0711** at ±17.14° — but `C-0090`'s published key is the optimum at only **1 of 3** states, so the recommended *design* is a function of an unmeasured parameter in a way `C-0099`'s stiffness sweep was not. **No accessible source quantifies the prestrain and Rothemund says so himself**, over 10 recorded queries and 68 records

| | |
|---|---|
| **Task** | [`T-172`](../tasks/T-172.md), raised by [`C-0099`](C-0099-row-end-crossover-stiffness.md) *Still open* item 1 / [`CH-0115`](../challenges/CH-0115-the-row-end-bracket-is-a-constraint-not-a-stiffness.md) |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** |
| **Verification type** | **logical** (that an initial stress is a load and therefore superposes — three consequences before any solve) **+ in-silico** (`C-0090`'s exhaustive centro-symmetric pipeline at 38.08 nm / phase 8: **37** ladder states at its own placement and **3** full enumerations of **163 296** placements each) **+ literature** (Rothemund's SI read directly; 10 recorded EuropePMC queries) |
| **Verdict** | **PASS on the acceptance's second branch, and the branch is the interesting one.** `P1`: the term exists — `OrigamiGrillage.crossoverPrestrains`, empty by default, and the empty and all-zero maps are asserted **bit-identical in the assembled load vector** to the unmodified lattice. `P2`: the best 34-root dishing is re-read at 37 states and 3 enumerations. `P3`: **no accessible source bounds the prestrain**, and the flatness is insensitive to any `\|θ₀\|` **below 15.45°** — which is *inside* the range the lattice's own register makes plausible, so unlike `C-0099` this is **not** a null. Raises [`CH-0120`](../challenges/CH-0120-an-influence-bank-assumes-a-lattice-with-no-self-load.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The prestrain itself is unknown and unsourced; what is established is the **threshold**, the **mechanism**, and that the threshold is reachable. |
| **Provenance** | `gpd/results/T-172-row-end-prestrain.json`, produced by `structure.RowEndPrestrainStudyKt` (**new**); model in `src/main/kotlin/structure/CrossoverPrestrain.kt` (**new**) plus **one defaulted constructor parameter and one derived property** added to `structure/OrigamiGrillage.kt` (`crossoverPrestrains`, `withoutPrestrain`), with `assembleLoad` widened from `private` to `internal` so bit-identity can be asserted on the **load vector** rather than on a solved field; `hingeEnergy` and `CrossoverForce.hingeMoment` now read `Δφ − θ₀`, which is a no-op at the default. `anchoring/`, `coupling/` and `window/` were **read, not edited**. **6 cheap bounds, 6 ladder rungs, 37 sweep states, 3 exhaustive enumerations, 18 linearity records, 3 literature records, 6 convergence records, 8 reproductions, 3 predicates, 6 falsifiers, 6 findings**; **12 gate-named tests in `src/test/kotlin/structure/CrossoverPrestrainTest.kt`**, red-checked (7 unresolved references, then 3 genuine failures that changed the design); `tools/study.sh` and `tools/verify.sh` run on isolated trees with a sibling agent's mid-TDD `src/test/kotlin/anchoring/CrossoverPhaseSelectionTest.kt` dropped by `--drop-file`; literature in `gpd/data/T-172-sources/` (`query.py`, `europepmc-queries.json`) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes** at the SAXS 2.69 nm, 0.34 nm rise, 32/3 bp per turn, 16 bp column pitch, 32 bp per-interface spacing; along-helix width **38.08 nm** at crossover **phase 8**; `C-0090`'s buildable 24-rise **8.16 nm** arm at `C-0055`'s **34** roots; `C-0017`'s **33.3333 pN/nm** mandate shared equally; `C-0022`'s solved collar at 2 mM, a 10 nm gap and 0.192 V, **as `C-0090` and `C-0099` carried it**; `C-0001`'s foundation secant; free stroke **5.15473846 nm** |
| **Consumes** | [`C-0099`](C-0099-row-end-crossover-stiffness.md) (the question, the 14 row-end sites, `rowEndCrossoverSites`, the admitted reading), [`C-0090`](C-0090-buildable-raster-width.md) (the 38.08 nm width, phase 8, the arm, and its optimum placement **key read from its result file**), [`C-0095`](C-0095-row-end-crossover.md) (the crossover exists), [`C-0009`](C-0009-discrete-lattice-tile.md) (`OrigamiGrillage`, `k_θ`), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`), [`C-0063`](C-0063-upward-root-placement.md) (`centroSymmetricPlacements`, `UpwardRootInfluenceBank` — used as the **agreement gate**), [`C-0055`](C-0055-unused-junction-site.md), [`C-0022`](C-0022-tile-edge-load-profile.md) (**read from its result file**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the register-is-linear-in-the-offset rule), `Gen1Tile` |
| **Raises** | [`CH-0120`](../challenges/CH-0120-an-influence-bank-assumes-a-lattice-with-no-self-load.md), against `C-0058` and `C-0063` |

---

## The claim, in one line

**`C-0099` asked how stiff a row-end crossover is and the answer did not matter; this asks what angle it was built at, and that one does — the threshold is 15.45° and the lattice's own arithmetic puts a candidate at 17.14°.**

---

## The conventions, restated rather than inherited

- Angles **rad** (degrees where a source quotes them), rotational stiffness **pN·nm/rad**, couples **pN·nm**, lengths **nm**; `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
- `x` along the helices, `y` across them, `z` normal and positive **upward**; `w` positive **downward**.
- A **crossover is TWO elements** (`C-0009`): a dihedral spring `k_θ` on the *relative roll*, and a vertical link, which is a constraint.
- A **prestrain** `θ₀` is the relative roll the crossover is **built at**: its hinge stores `½k_θ(Δφ − θ₀)²`. Positive `θ₀` rolls the upper duplex positively relative to the lower at zero load.
- **Dishing** is `C-0063`'s, unchanged: the peak of the deflection with its best-fit plane removed, over the free plate's mean descent under the same uniform load.

---

## Deliverable 1 — the cheap bound, which is three consequences of one expansion

&nbsp;&nbsp;&nbsp;&nbsp;`½ k_θ (Δφ − θ₀)² = ½ k_θ Δφ² − k_θ θ₀ Δφ + ½ k_θ θ₀²`

**The quadratic term is untouched.** A prestrain therefore moves **no entry of the stiffness matrix**; it contributes a fixed couple `±k_θθ₀` to two roll degrees of freedom. Three things follow, and none of them costs a solve:

1. **The deflection field is linear in `θ₀`** — asserted to `1e−9` over three decades of angle as gate 4.
2. **Peak dishing is an absolute value**, so `D(θ₀) ≤ D_load + |θ₀|·D_unit`: one unit-prestrain solve gives a rigorous ceiling over the whole axis.
3. **The host's factorisation is unchanged**, so `C-0058`'s Woodbury reduction stays exact — *provided* the influence functions are taken on the prestrain-free lattice, which is [`CH-0120`](../challenges/CH-0120-an-influence-bank-assumes-a-lattice-with-no-self-load.md).

| quantity | value |
|---|---|
| `C-0090`'s placement dishing at zero prestrain | **0.0621469105** of the free stroke |
| dishing per radian of uniform row-end prestrain, at that placement | **0.140379322** |
| the same slope, **uncoupled** tile | **0.265219996** |
| **the prestrain at which `T-5b`'s 0.10 is reached** | **15.4497275°** (0.269648613 rad) |
| the assembled couple 14 row-end crossovers carry there | **51.10 pN·nm** |

**Cost justification.** The alternative is an oxDNA or all-atom edge crossover, which `CH-0111` ranked third and `C-0099` recommended against — and which would have to be run at a prestrain nobody can specify. One extra solve gives the threshold; the ladder below then decides whether the threshold is anywhere near. That arithmetic ran before the exhaustive enumerations and is what justified paying for three of them rather than twenty-one.

---

## Deliverable 2 — the ladder, DERIVED from the lattice rather than transcribed

`CLAUDE.md`, from `C-0015`: *"the register departure from a design twist is LINEAR in the base-pair offset"*. `registerPrestrain(n, 32/3 bp per turn, 10.5 bp per turn)` reproduces the two figures that record carries and extends them.

| rung | basis | degrees | couple per crossover | assembled over 14 |
|---|---|---|---|---|
| **8 bp register** | the out-of-plane site's offset | **4.286°** | 1.012 pN·nm | 14.17 |
| **16 bp register** | the sheet's own next in-plane crossover, one column pitch away | **8.571°** | 2.024 | 28.34 |
| **32 bp register** | the **per-interface** spacing — the distance from a row-end crossover to the next one *on its own interface*, which is the one that would balance it | **17.143°** | 4.048 | 56.67 |
| half the azimuthal quantum | the worst registration a 33.75°/bp lattice can impose | **16.875°** | 3.985 | 55.79 |
| a quarter turn | assumption-free geometric ceiling | 90° | 21.25 | 297.53 |
| a half turn | the **absolute** ceiling: past it the crossover is better described in the opposite register | 180° | 42.50 | 595.05 |

> **The threshold, 15.45°, sits between the 16 bp rung and the 32 bp rung.** Which of the two is the physical one is exactly what Rothemund says is unknown — and it is the whole of the remaining exposure.

---

## Deliverable 3 — the sweep at `C-0090`'s own placement, and `F6` FIRED

Three distributions × six rungs × both signs, plus zero: **37 states, 23 of them flat**.

| rung | `+θ₀` | flat? | `−θ₀` | flat? | peak hinge moment at `−θ₀` |
|---|---|---|---|---|---|
| zero | **0.0621469** | **yes** | — | — | 0.681 pN·nm |
| 8 bp register, ±4.286° | 0.0648540 | yes | 0.0655257 | yes | 1.038 |
| 16 bp register, ±8.571° | 0.0690665 | yes | 0.0731721 | yes | 1.590 |
| half the quantum, ±16.875° | 0.0856790 | yes | **0.1004593** | **NO** | 2.659 |
| **32 bp register, ±17.143°** | 0.0864533 | yes | **0.1013395** | **NO** | 2.693 |
| a quarter turn, ±90° | 0.2970667 | no | 0.3407623 | no | 12.74 |
| a half turn, ±180° | 0.5572362 | no | 0.6365198 | no | 25.44 |

**`F6` FIRED**: the crossing is reached, by the 32 bp rung, at 17.14° — and the first measured crossing over the whole sweep is at **−16.875°**, 0.1004593.

**The uniform distribution is not even in the sign** — `−17.14°` is 17 % worse than `+17.14°` — and the asymmetry belongs to `C-0022`'s **solved collar**, not to the lattice: the collar breaks the tile's up/down symmetry, so a prestrain that bows the rim the way the collar already does costs more than one that opposes it.

**And two of the three distributions are the same map.** Each of the 14 row-end crossovers is the only one on its interface, and *which end* it sits at alternates with the interface — so *"alternating by interface"* and *"opposed ends"* are the identical assignment, to the last digit, and both are **even** in the sign. That is a lattice congruence, not a coincidence, and it halves the distribution axis before any solve. Both are **flat at every rung below a quarter turn**, so the uniform distribution is the adverse one.

---

## Deliverable 4 — the design absorbs it, and `F4` FIRED

Three exhaustive enumerations of the centro-symmetric 34-root family, **163 296 placements each**.

| state | best over the family | `C-0090`'s own placement | penalty | best key = `C-0090`'s? | flat? |
|---|---|---|---|---|---|
| zero prestrain | **0.0621469105** | 0.0621469105 | **0.0** | **yes** | yes |
| uniform, **+17.143°** | **0.0826363454** | 0.0864532913 | 0.0038169 | **no** | **yes** |
| uniform, **−17.143°** | **0.0711278815** | 0.1013395 | **0.0302116** | **no** | **yes** |

> **Re-optimising recovers the verdict in both signs**, and in the adverse one it is worth **0.0302** of the stroke — five times the entire span of `C-0099`'s stiffness sweep. So a prestrain of this size is a **placement** question, not a feasibility one.
>
> **But `F4` fired**: `C-0090`'s published key is the optimum at **1 of 3** states. `C-0099` could say *"the design is not a function of the unmeasured parameter, only the value of its flatness is"*; **that sentence does not survive here.**

---

## Deliverable 5 — the falsifier the task was PROPOSED with is false, and that is a result

`T-172` was set with a suggested falsifier: *"a uniform prestrain on a free tile must translate it and dish exactly zero, which is this project's best falsifier and the same symmetry as a uniform load."*

**It is not the same symmetry.** A uniform *load* is equilibrated by a rigid translation — `w = q/k_f` has zero fourth derivative and satisfies the free-edge conditions identically. A uniform *prestrain* is an **eigenstrain**, and the state that relaxes every hinge **and** every vertical link at once is the one in which consecutive duplexes are rolled by `θ₀` and stepped so their touching surfaces still meet: a **cylinder of curvature `θ₀/d`**. A free tile under a uniform prestrain **curls**.

Measured: a uniform 17.143° on all **56** crossovers takes the free tile from **0.299035** to **0.638118** of the stroke. And the cylinder is quantitative, not rhetorical — gate 3 asserts the sagitta `κL²/12` against the solved peak on a soft foundation and they agree to better than a factor of 1.5, on a quantity that spans decades.

> **`F3` did not fire, in the sense that matters: it was declared as *"the uniform prestrain dishes zero"* and it does not.** `CLAUDE.md`'s best falsifier is **silent** on an eigenstrain, and a study that had asserted it would have reported a correct solver as broken.

The standing falsifier is still run, on `OrigamiGrillage.withoutPrestrain`, where it is a statement about a **load**: worst over the whole sweep **2.126e−7** of the free stroke (`F5` silent). Reading it on the prestrained lattice instead gave **0.833** — the study's own third instance of `CH-0120`'s trap, caught by the falsifier firing.

---

## Deliverable 6 — the literature, read directly, and it is a NOT FOUND with a reason

| question | source | read as | quantified? |
|---|---|---|---|
| Is the row-end / seam crossover tension quantified anywhere? | Rothemund 2006, *Nature* **440**:297, Suppl. Note S2 — `gpd/data/T-151-sources/` | **READ DIRECTLY** | **no**, and the paper says so |
| What **coordinate** does the strain live in? | the same note, design-program section | **READ DIRECTLY** | **yes — an ANGLE** |
| Has anyone since put a number on it? | EuropePMC, **10 recorded queries**, **68 unique records** — `gpd/data/T-172-sources/europepmc-queries.json` | abstracts read; three full texts already manifested by `T-151` | **NOT FOUND** |

The second row is the one that made the modelling decision. Rothemund's own design program scores the strain as

> *"the sum of the squared **angular deviation** from the tangent point for the base before and the base after the crossover"*,

with the two causes named as *"the non-integral number of bases in a single turn, and the major-minor groove angle"*. **Both are angles**, and the coordinate is exactly the dihedral one `C-0009`'s crossover hinge already carries — which is why the term needed no new degree of freedom.

What the literature does quantify is the **global** twist of a whole object (underwinding, helicity mismatch, intercalator relief); the closest statement to an edge is a 2021 tutorial's *"compensation of residual strain/torque is much easier for the hexagonal lattice"*. **No per-crossover residual angle at an edge exists in print.**

---

## The five verification gates

**12 gate-named tests** in `src/test/kotlin/structure/CrossoverPrestrainTest.kt`, plus one in-study `check` and eight reproduction records.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a prestrain couple is `pN·nm/rad × rad`, odd in the angle; a base-pair offset becomes radians and the 8 bp rung is **exactly half** the 16 bp one; a negative stiffness, a `NaN` angle and an infinite prestrain each throw | **PASS** |
| **2 — limiting cases** | an **empty** map and an **all-zero** map are **bit-identical in the assembled load vector** to the unmodified lattice; a prestrain on a crossover whose hinge is dead (`k_θ = 0`) changes nothing, because the couple is `k_θθ₀` | **PASS** |
| **3 — symmetry and conservation** | the response **superposes exactly** (load-only + prestrain-only = both, at nine points, `1e−9`); it is exactly **odd** in `θ₀`, so peak dishing is even; a uniform prestrain **curls** the sheet to its predicted cylindrical sagitta; the moment a prestrained crossover carries is `k_θ(Δφ − θ₀)`, not `k_θΔφ`; and the standing uniform-load falsifier holds at **2.126e−7** | **PASS** |
| **4 — numerical convergence** | nested subdivisions 1 ⊂ 2 ⊂ 4 at the 32 bp rung: **1.11 %**, **0.115 %**, 0 — an order below the 39 % the sweep spans; the dishing sample grid 41/81/161 moves **0.0** | **PASS** |
| **5 — literature and upstream** | `C-0090`'s **0.0621469105** reproduced at **5.4e−10**; `Gen1Tile`'s 13.5294118 pN·nm/rad at 2.6e−9; `CLAUDE.md`'s 8.5714286° and 4.2857143° at 3.3e−9; `C-0095`'s 14; the free stroke 5.15473846; and **`C-0063`'s own `UpwardRootInfluenceBank` reproduced at 0.0** by the split bank, which is an independent code path | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | zero prestrain does not reproduce `C-0090`'s 0.0621469105 | **NO** | departure **5.449e−10** |
| **F2** | the coupled response is not linear in `θ₀` | **NO** | worst departure of the *peak* from the unit-slope prediction **0.208**, which is expected — a peak of `\|A + θB\|` is linear in `θ` only where the two fields peak at the same place; exact **field** linearity is asserted at `1e−9` in gate 4 |
| **F3** | a uniform prestrain over every crossover dishes zero | **NO** | 0.299035 → 0.638118: it **curls**. Deliverable 5 |
| **F4** | the exhaustive optimum placement moves under a prestrain | **YES** | `C-0090`'s key is the optimum at **1 of 3** states. Deliverable 4 |
| **F5** | a uniform load on a uniform foundation dishes more than `1e−6` | **NO** | worst **2.126e−7** — after being read on `withoutPrestrain`; on the prestrained lattice it read 0.833 and *correctly* fired |
| **F6** | `T-5b`'s 0.10 is crossed inside the derived ladder | **YES** | the crossing at **15.45°** is reached by the **32 bp register** rung at 17.14° |

**What was not anticipated.** Three things. First, that the answer would be *"yes it matters"* — every previous row-end question closed as a null, and the reason this one does not is that a prestrain is a **load** while a stiffness is a *coefficient*, and a load does not have to be small to be admissible. Second, that putting the term where it physically belongs — on the **structure** — would silently corrupt every influence function computed from that structure; the naive build died on a Cholesky pivot at 1 rad and would have returned a plausible wrong number at 17°. Third, that *"alternating by interface"* and *"opposed ends"* are the same map: two distributions chosen to be different turned out to be one lattice fact.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** The prestrain is unknown, unsourced, and — on Rothemund's own statement — was unknown in 2006 and is unknown now. What is established is the **threshold** and the **mechanism**.
- **The threshold is read at `C-0090`'s recommended placement, at phase 8, at 38.08 nm, under `C-0022`'s carried collar.** It is a *placement-specific* number: the re-optimised family clears the same prestrain with 29 % of margin. `CLAUDE.md`'s discipline applies — **quote a flatness with the state it is read at** — and here the state includes the prestrain.
- **`|θ₀| < 15.45°` is a triangle-inequality CEILING, not the measured crossing.** The measured first crossing at `C-0090`'s placement is at **16.875°**, so the bound is conservative by 9 %, as a bound should be.
- **The distribution axis is three states of which two coincide, and none of them is sourced.** Whether the 14 row-end crossovers share a sign is a question about the scaffold route, which `C-0095` describes and does not resolve into a sign.
- **Phase 8 only**, as `C-0099`. Phase 24 has 29 % of margin at zero prestrain rather than 38 %, so its threshold is lower and is not computed.
- **The exhaustive family is `C-0063`'s centro-symmetric one** at `minimumPerRow = 2`, `maximumPerRow = 3`. A non-symmetric placement could do better at some rung.
- **The term is a *linear* eigenstrain.** It carries no geometric stiffness: a prestrained crossover in this model does not stiffen or soften the lattice, it only loads it. That is exact for a small rotation and is an approximation at the 90° and 180° rungs, which are quoted as geometric ceilings and not as design states.
- **`OrigamiGrillage` gained one defaulted parameter and one derived property**, and `assembleLoad` became `internal`. The empty map is asserted bit-identical **in the load vector**; two identical *solves* of this lattice differ by ~4 ulp in one JVM, so a `==` on a solved field would have tested the tiering compiler and not the term.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the *"crossovers in tension"* and *"how the strain is actually relieved is unknown"* passages | verbatim | **CITED, READ DIRECTLY** — Rothemund 2006, Suppl. Note S2, `gpd/data/T-151-sources/` |
| the strain metric, *"the sum of the squared angular deviation"* | verbatim | **CITED, READ DIRECTLY**, same note |
| `k_θ = 2αB/(100a)` at `α = 1` | 13.5294118 pN·nm/rad | **CITED, FITTED** — Chen et al., *JACS* **136**:6995 — via `C-0009`, **re-derived and asserted** |
| `C-0090`'s reading and optimum placement key | 0.0621469105 | **`C-0090`, READ FROM ITS RESULT FILE and REPRODUCED** to 5.4e−10 |
| `C-0022`'s solved collar terms | — | **`C-0022`, READ FROM ITS RESULT FILE**, keyed on concentration, gap and bias |
| the preferred 10.5 bp/turn and the square lattice's 32/3 | — | **CITED** (Rothemund 2006, Ke et al. 2009) |
| interhelical distance, rise | 2.69 nm, 0.34 nm | **CITED, MEASURED** (SAXS, Fischer et al. 2016) |

Everything else — the load-vector derivation and its three consequences, the unit-prestrain slope, the 15.45° threshold, the six-rung ladder, all 37 sweep states, the three exhaustive enumerations, the cylinder sagitta, the distribution congruence and the six convergence records — is **derived here in code**.

## Still open — named, not answered

1. **Which rung is physical.** The threshold sits *between* the 16 bp register (8.571°, comfortably flat) and the 32 bp per-interface register (17.143°, not flat in the adverse sign), and nothing in print chooses. **`T-176`.**
2. **The sign, and whether the 14 share one.** Not sourced, and it is worth 17 % of the dishing at the 32 bp rung.
3. **Phase 24 is not swept**, and it starts with less margin, so its threshold is lower.
4. **`C-0090`'s recommended placement should be re-selected under a prestrain**, or declared robust to one. `F4` says it is not the optimum at 2 of 3 states; the penalty is bounded at 0.0302 of the stroke and the verdict survives, but the *recommendation* has acquired a dependence it did not have.
5. **The peak crossover FORCE is not re-read.** `C-0090`'s 1.42774664 pN peak is not recomputed under a prestrain, although the **hinge moment** is: it runs 0.681 → 2.693 pN·nm over the physical ladder, a factor of 4.
6. **`CH-0120` is not repaired**, only raised, because `C-0058`'s and `C-0063`'s components are consumed by eighteen studies. **`T-175`.**
