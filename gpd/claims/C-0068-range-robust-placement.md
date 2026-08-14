# C-0068 — `C-0063`'s flat placement **is** flat over the range its own device traverses, with **equal springs** and no distribution at all — 0.0789 at 2 mM, 0.0853 at 0.5 mM and 0.0896 at 10 mM, every one inside `T-5b`'s 0.10 — and it is the **range-optimal** member of its own family, with **0 of 198 288** centro-symmetric placements beating it; but the equal-spring advantage belongs to the **10 nm layer**, because at the 5 nm layer's two states the same 34 roots dish **0.2000** and are a net dishing *source*

| | |
|---|---|
| **Task** | [`T-129`](../tasks/T-129-range-robust-placement.md), which is [`C-0064`](C-0064-robust-distribution.md)'s *Still open* item 1, [`C-0063`](C-0063-upward-root-placement.md)'s own item 1, and the single-state exposure [`C-0065`](C-0065-crossbar-array-placement.md) and [`C-0066`](C-0066-arm-slab-tie-clearance.md) both name |
| **Leaf** | **`A8.2`** (structural rigidity and joint stiffness), with **`A1.2`** for the anchoring scheme the placement belongs to |
| **Verification type** | **in-silico** (`C-0009`'s beam-and-hinge grillage at **phase 24 with its own eight crossover columns**, `C-0006`/`C-0047`'s flatness pipeline, `C-0022`'s **solved** loads read at run time and keyed on `(concentration, gap, bias)`, `C-0064`'s multi-state Woodbury surrogate and its smoothed minimax with analytic gradients — all re-run as libraries — through a new **multi-state influence bank** over every candidate root, asserted against a surrogate built over the subset alone and against an assembled solve) **+ logical** (the per-state least-squares floor, which bounds every distribution whatever; and `s = L₀ − h`, which decides what a device can occupy) |
| **Verdict** | **PASS, and the answer is YES for the device the programme placed — and only for a 10 nm layer.** With **34 equal springs** and `C-0017`'s unchanged total, `C-0063`'s placement dishes **0.0789** over the whole range `C-0018`'s placed 2 mM device traverses (gaps 10 → 7 nm at its own 0.192 V, both ends solved by `C-0022`), **0.0853** over `C-0032`'s 0.5 mM recommendation and **0.0896** over the 10 mM device — **all three inside `T-5b`'s 0.10**, against **0.0706** at the single state `C-0063` reported. **So the single-state verdict travels, and the cost of the range is the margin**: 1.42× at the design state becomes **1.12×** at the tightest range, and it is the *compressed* end of the stroke that spends it. **The exception is the FIVE nanometre device**, whose range contains `C-0022`'s 2 nm state: equal springs dish **0.2000** there, 2.0× outside the convention, and are **worse than no coupling at all** at *both* of its states (0.1104 against a free 0.0638 at 5 nm; 0.2000 against 0.1648 at 2 nm) — where the same equal springs on `C-0058`'s 3 × 15 grid dish 0.0796 and are inside it. **A distribution recovers all four** — 0.0291 / 0.0365 / **0.0565** / 0.0382 at peak ratios of only **1.72–2.32**, well below the 5:1 `C-0060` prices as buildable — so nothing here is infeasible; what falls is *"equal springs are enough"*, and only at the 5 nm layer (`CH-0080`). **The placement itself is vindicated twice over, and the phase turns out to be what the layer selects.** Re-enumerating the centro-symmetric family **exhaustively under a range objective** — 361 584 placements at the two phases the congruence admits, each priced under two objectives — finds **0 of 198 288 at phase 24 better than `C-0063`'s own**, which *is* the range argmin, and phase 8's best at 0.0910; under the **5 nm** device's range instead, **no** placement at phase 24 is flat (best 0.1169) while one at **phase 8 is** (**0.0895**) — and that one dishes **0.2416** at the 10 nm design state, so the two layers' *argmins* are mutually poor (whether any single member serves both was not searched for); and adding two interpolated intermediate gaps moves the reading by **exactly zero at nine significant digits**, so two solved endpoints *are* the range. **The cheap bound did not fire** (per-state floor 0.0056, 18× below the tolerance), and `C-0064`'s sign instrument explains everything: within a 10 nm device the two ends' free-tile dishing fields run **+0.9969 to +0.9998**, while the 5 nm device's own two states run **−0.9427** — *a device whose own range is anti-parallel to itself*. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — `C-0055`'s free lever on one crossover is this programme's own construct and `C-0029`'s literature finding is unchanged and upstream of every number here. |
| **Provenance** | `gpd/results/T-129-range-robust-placement.json`, produced by `anchoring.RangeRobustPlacementStudyKt`; model in `src/main/kotlin/anchoring/RangeRobustPlacement.kt`; **6 cheap bounds, 13 state records, 45 pairwise cosines, 4 operating ranges, 31 subset minimaxes, 361 584 centro-symmetric placements enumerated exhaustively and each priced under two range objectives (723 168 evaluations, 4 records), 5 distribution records, 4 convergence records, 14 upstream reproductions, 5 predicates**; **13 gate-named tests in `src/test/kotlin/anchoring/RangeRobustPlacementTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 12 m 19 s** — the whole suite, on its own isolated tree, with **nothing dropped**; the result file re-run through `tools/study.sh` and diffed: **28 lines of 1 423 differ, all of them inside the 31 subset minimaxes, and every other section is byte-identical** — see *Determinism* below |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **MgCl₂ at 0.5 / 2 / 10 mM**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured **2.69 nm**, **phase 24 carrying its own eight crossover columns**; `C-0055`'s **34** upward roots at `C-0063`'s swept placement; `C-0017`'s **33.3333 pN/nm** mandate as a **sum**; `C-0022`'s **solved** edge profiles; `C-0001`'s foundation secant; free-tile stroke **4.90731 nm** |
| **Consumes** | [`C-0063`](C-0063-upward-root-placement.md) (the placement, read from its own result file and re-checked against the phase-24 upward lattice, its centro-symmetry and its 0.0706), [`C-0064`](C-0064-robust-distribution.md) (`MultiStateSurrogate` and `minimaxStiffnessDistribution` re-run as libraries — with their `searchDecision` rounding inherited unchanged — the operating-range definition, re-declared here in code, and its 0.0373 / 0.0753 / 0.0796 reproduced), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collars, keyed on concentration, gap **and bias**), [`C-0050`](C-0050-desired-stroke-reach.md) (`s = L₀ − h` and the dead-load stroke, read from its own result file), [`C-0018`](C-0018-maximum-usable-bias.md)/[`C-0032`](C-0032-softening-coupling-stability.md) (which device is placed, and in which buffer), [`C-0058`](C-0058-non-uniform-coupling.md) (`rimStiffenedWeights`, `normalisedStiffnesses`, `perPathThermalForces`, `attachmentGrid` and its 0.2182), [`C-0055`](C-0055-unused-junction-site.md)/[`C-0053`](C-0053-hinge-arm-array-packing.md) (the upward lattice, the arm, the row scheduler), [`C-0009`](C-0009-discrete-lattice-tile.md)/[`C-0015`](C-0015-crossover-phase-and-registration.md)/[`C-0047`](C-0047-single-column-flatness.md)/[`C-0026`](C-0026-one-row-per-duplex.md) (the grillage, the phase lattice, the flatness pipeline, the normaliser), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (the per-path ceiling), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate) |
| **Raises** | [`CH-0080`](../challenges/CH-0080-the-equal-spring-advantage-belongs-to-the-ten-nanometre-layer.md), against `C-0063`'s Deliverable 2 |

---

## The claim, in one line

**`C-0064` found that a flatness verdict read at one state need not travel, and named `C-0063`'s single-state 0.0706 as the largest open item it left; the answer is that it does travel — over the whole stroke of every 10 nm device `C-0022` solved, with equal springs, and on a placement that turns out to be the argmin of the range objective as well as of the single-state one — but that it travels no further than the layer it was designed against, because on a 5 nm layer the same 34 roots dish 2.0× the convention and are worse than no coupling at all.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly);
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous buffer with **Mg²⁺**.
- `x` **along** the helices, `y` **across** them, `z` **normal** and positive **upward** — away from the
  grafted layer, which lies below the tile; `w` positive **downward**.
- **Dishing** is the peak absolute departure from the area-weighted best-fit **plane** — piston and both
  tilts removed — on the same **81 × 81** grid as `C-0026`, `C-0047`, `C-0058`, `C-0063` and `C-0064`,
  normalised by the **free-tile stroke 4.90731 nm**.
- **Flat** means below **0.10** of that stroke — `T-5b`'s convention via `C-0015`, **a convention and not
  a physical threshold**.
- A **state** is a `(concentration, gap, bias)` triple of `C-0022`'s solved profiles.
- An **operating range** is the set of states **one device** traverses: one buffer, one layer height, one
  bias, from `gap = L₀` down to `gap = L₀ − s` — `C-0064`'s definition, re-declared here in code because
  it is a *premise* of this task and not a number to be inherited.
- The coupling is **34 linear springs to ground** whose stiffnesses **sum** to `C-0017`'s mandate. The
  headline is at **equal** springs; the distribution is swept separately.

### The upstream gotcha, avoided by construction

`gpd/results/T-3b-*.json` carries **more than one** profile per `(concentration, gap)` — one per
operating bias. Every lookup here is keyed on **all three** and errors if the triple is absent.

---

## The cheap bounds, which ran before any optimisation

| | bound | value | what it settles | falsifier fired? |
|---|---|---|---|---|
| **1** | the per-state least-squares floor, worst over the four device ranges | **0.0056** of the stroke | no distribution whatever on these 34 stations beats this, so a floor above 0.10 would have proved the placement a single-state result outright | **no**, 18× below the tolerance — so every negative below is a *"not found"*, not a theorem |
| **2** | the worst free-field cosine against the design state | **−0.9842** | `C-0064`'s instrument, one pass over two precomputed fields: which states can share one correction at all | — |
| **3** | the stroke `C-0022`'s 2 nm state demands of a 10 nm device | **8.000 nm** | read against bound 4, it is what makes the exclusion of that state **physical** | **no** |
| **4** | `C-0050`'s dead-load stroke at a 10 nm layer, the largest anywhere it solved | **7.4235 nm** | 8 nm is outside it at **every** layer model and **every** grafting density in `C-0027`'s 10 nm window (6.0135 nm at §3's own `σ = 0.024 nm⁻²`), and `C-0017`'s theorem says a coupling only reduces it | — |
| **5** | `C-0050`'s dead-load stroke at §3's own 5 nm layer | **1.5299 nm** | the same test applied to the device that *owns* the 2 nm state — and it does **not** clear the 3 nm that device needs either, so `C-0022`'s 2 nm state is held by its solved 0.368 V bias and not by a 100 pN dead load | **no** — *and that is the surprise*, recorded rather than smoothed |
| **6** | 34 equal springs over the design device's range | **0.0789** of the stroke | the headline, at one Cholesky per state | **no** |

---

## Deliverable 1 — the ranges, which is the answer

| device | states traversed | **equal springs** | flat? | **34-parameter minimax** | flat? | peak ratio |
|---|---|---|---|---|---|---|
| **2 mM, `L₀` = 10 nm, 0.192 V** (`C-0018`'s placed device) | 10 nm, 7 nm | **0.0789** | **YES** | **0.0291** | YES | 1.83 |
| **0.5 mM, `L₀` = 10 nm, 0.134 V** (`C-0032`'s recommendation) | 10 nm, 7 nm × 2 biases | **0.0853** | **YES** | 0.0365 | YES | 1.72 |
| **10 mM, `L₀` = 10 nm, 0.192 V** | 10 nm, 7 nm × 2 biases | **0.0896** | **YES** | 0.0382 | YES | 2.21 |
| **2 mM, `L₀` = 5 nm, 0.368 V** | 5 nm, 2 nm | **0.2000** | **NO** | **0.0565** | YES | 2.32 |

- **Both endpoints are active** at the design device's optimum, which is what an equalised minimax looks
  like; at 0.5 mM and 10 mM the compressed end alone binds.
- **The single state is not the range**: 0.0706 at the design state against 0.0789 over the range, and
  the tightest 10 nm reading is 0.0896 — so the margin against `T-5b`'s 0.10 falls from **1.42× to
  1.12×**. A design quoted at the single state is quoted 12 % optimistically.
- **The 5 nm device fails at its own rest state alone** (0.1104), so this is not a range effect there at
  all: the placement is simply not flat against a 5 nm layer's load field.

---

## Deliverable 2 — the distribution, and `C-0058`'s rim rule over a range

| rule | ratio | design state | **over the design device's range** | flat over the range? |
|---|---|---|---|---|
| **uniform — 34 EQUAL springs** | 1.00 | **0.0706** | **0.0789** | **YES** |
| `C-0058`'s rim rule over 6.70 nm | 2.00 | 0.1410 | **0.1599** | no |
| the same | 3.00 | 0.1802 | 0.2086 | no |
| **the same — `C-0058`'s own ×5** | 5.00 | 0.2214 | **0.2566** | no |
| **the 34-parameter minimax over the range** | **1.83** | — | **0.0291** | **YES** |

`C-0063`'s finding that the rim rule **reverses sign** on this station set is reproduced over a *range*,
not only at a state: every ratio above 1 makes it worse, monotonically.

**What the range costs in force**, at the minimax rather than at equal springs: the peak path stiffness
goes to 1.83× the uniform share, i.e. **5.38 pN** per path at §3's acceptable 3 nm on the mandate secant
(3.64 pN solved), against the **10 pN** unzip allowable — 1.9× clear, and inside `C-0049`'s per-path
ceiling of 3.3333 pN/nm. `C-0014`'s thermal force per path goes from 0.346 pN to **0.632 pN**. At the
5 nm device the same numbers are 6.83 pN and 0.803 pN.

---

## Deliverable 3 — which states one device co-occupies, and why the 2 nm exclusion is physical

A device is a `(buffer, layer height, bias)`; under bias it descends from `L₀` to `L₀ − s`, and
`C-0050`'s identity `s = L₀ − h` is what turns a state into a stroke.

| claim | value | source |
|---|---|---|
| stroke a 10 nm device needs to occupy a 2 nm gap | **8.000 nm** | arithmetic, in code |
| largest dead-load stroke `C-0050` finds at a 10 nm layer, over six models and `C-0027`'s whole window | **7.4235 nm** | read from `gpd/results/T-108-*.json` |
| the same at §3's own `σ = 0.024 nm⁻²` | **6.0135 nm** | idem |
| stroke the **5 nm** device needs to occupy the same 2 nm gap | **3.000 nm** — §3's acceptable stroke | arithmetic |

**So the 2 nm state is not a state of any 10 nm device**, and the exclusion costs this claim nothing,
because the device that *does* own it is evaluated in its own right — and is the one that fails.
**That is the test a reader should apply to any convenient exclusion: does removing the state remove the
problem? Here it does not.**

> **The honest caveat, reported rather than smoothed.** The same dead-load test does not clear the
> **5 nm** device's own 3 nm either (1.5299 nm), so `C-0022`'s 2 nm state is held by its solved
> **0.368 V** bias and not by a 100 pN dead load. That is a fact about `C-0022`'s state set, inherited
> here as given; it does not move any number above, because the 5 nm device is evaluated at exactly the
> states `C-0022` solved for it.

---

## Deliverable 4 — the placement, re-swept under the RANGE objective

`C-0063` found its placement by minimising the dishing at **one** state. The whole centro-symmetric
family — the two phases the congruence `2c ≡ 0 (mod 10.88 nm)` admits — was re-enumerated
**exhaustively** here under a *range* objective, in one pass over two objectives.

| phase | objective | enumerated | best | flat? | better than `C-0063`'s | the winner at the 10 nm design state |
|---|---|---|---|---|---|---|
| **24** | the 2 mM **10 nm** device's range | **198 288** | **0.0789** | **YES** | **0 of 198 288** | 0.0706 — it **is** `C-0063`'s placement |
| 8 | the same | 163 296 | 0.0910 | YES | 0 of 163 296 | 0.0910 |
| 24 | the 2 mM **5 nm** device's range | 198 288 | **0.1169** | **NO** | 27 355 of 198 288 | 0.2986 |
| **8** | the same | 163 296 | **0.0895** | **YES** | 30 465 of 163 296 | **0.2416** |

**Three readings, and the third was not anticipated.**

1. **`C-0063`'s placement is the argmin of the range objective as well as of the single-state one**, over
   the family it was found in — the single-state search cost nothing in range performance, which is not
   something the single-state claim could have known.
2. **The 5 nm device is not without an equal-spring placement**: 30 465 members of the phase-8 family
   beat `C-0063`'s 0.2000 there and the best is **0.0895**, inside the convention. What it lacks is a
   placement at **phase 24**, where the best of 198 288 is 0.1169 and **nothing** clears.
3. **So the layer selects the phase, and the two layers' argmins are mutually poor.** The phase-8 winner
   for the 5 nm device dishes **0.2416** at the 10 nm design state, and `C-0063`'s winner dishes 0.2000
   over the 5 nm range. **That is a statement about the two argmins and not a proof that no member serves
   both** — a compromise placement was not searched for, and it is *Still open* item 1.
   `C-0063` found the eight-column phases to be *the flat ones* under a 10 nm layer's load;
   which **of** those ten a design should take is decided by the layer, and that is a variable no claim
   had carried.

---

## Determinism — measured, and the same manifold `C-0064` met

`gpd/README.md` requires that a re-run which changes nothing produces no diff. Two independent runs of
the finished code differ in **28 lines of 1 423**, and they are all in one place.

| section | re-run |
|---|---|
| bounds, states, cosines, **ranges**, **placements**, distributions, convergence, reproductions, predicates, findings, parameters | **byte-identical** |
| the 31 subset minimaxes | **7 of 31 differ**, by `8.9e−9` to `2.3e−3` relative; **0 verdict flips** |

The cause is `C-0064`'s: a 34-parameter minimax has an optimal **manifold**, so a descent returns *a*
member of it and a last-ulp difference in one accepted step lands in a neighbouring basin of equal
objective. What is new here is *where* it did **not** happen: the four **operating ranges** — this
claim's whole answer — reproduce to the last emitted digit, as do all four exhaustive placement
enumerations over 723 168 evaluations, because a placement sweep's decisions are rounded comparisons
between *distinct* designs rather than steps along a flat set. **The subsets are a diagnostic, not a
verdict**, and the dichotomy they report (12 of 14 against 17 of 17) is unchanged between runs.

---

## The five verification gates

Executed as **13 gate-named tests** in `src/test/kotlin/anchoring/RangeRobustPlacementTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a bank refuses a host that carries supports, empty stations, an empty state list, a repeated or out-of-range index and an unknown station; `s = L₀ − h` is refused for a gap above the resting height and for a negative ceiling; **the worst dishing over a range is exactly linear in the applied pressure** (`1e−10`) | **PASS** |
| **2 — limiting cases** | **a uniform load on a uniform Winkler foundation dishes exactly zero on the free tile** (`< 1e−9` nm) — the free strong falsifier; the worst over one state is that state's own peak; the worst over a subset never exceeds the worst over a superset; the minimax is a **descent** that never returns worse than its equal-spring start and conserves the mandate to `1e−12` with every stiffness positive; the per-state floor never exceeds what any distribution reaches | **PASS** |
| **3 — symmetry and conservation** | **Maxwell-Betti reciprocity of the sliced station influence matrix**, measured between two different quadratures (`< 1e−9`); a **point-reflected** station set dishes identically at every state on a lattice that is centro-symmetric and not mirror-symmetric | **PASS** |
| **4 — numerical convergence** | **a sliced bank equals a surrogate built over that subset alone** (`1e−12` on the peaks, the forces, the floor and the cosine) — the check that licenses 361 584 placements through one factorisation; **the equal-spring range reading equals an assembled `OrigamiGrillage` solve at the same stations** (`1e−9`); and in the study, nested subdivisions 1 ⊂ 2 ⊂ 4, the sample grid 41/81/161 at **two** ranges, and the range's own discretisation | **PASS** |
| **5 — literature and upstream cross-check** | **`C-0063`'s 0.0706145537 reproduced to the last emitted digit** (departure `0.0` at the file's nine significant digits, `2.9e−10` before rounding) on its own host at its own state — the only published number on these stations — with its 34 roots asserted onto the phase-24 upward lattice and its centro-symmetry re-checked; `C-0022`'s free tile 0.3079 (`7.7e−6`); `C-0026`'s stroke 4.90731 nm; `C-0058`'s 0.2182 (`6.1e−5`); **`C-0064`'s 0.0753 (`5.7e−4`), 0.0796 and its 45-path range minimax 0.0373 (`1.7e−3`)**; `C-0055`'s 10.88 nm pitch, 34 roots and 8.164 nm arm; `C-0049`'s 3.3333 pN/nm; `C-0050`'s 6.0135 nm and 7.4235 nm | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a range floor above 0.10, proving no distribution on this placement is flat | **no** | 0.0056, 18× below |
| 2 | the sliced bank disagreeing with a surrogate over the same subset, or with an assembled solve | **no** | `1e−12` and `1e−9`, both as tests |
| 3 | a uniform load dishing anything on the free tile | **no** | `< 1e−9` nm |
| 4 | `C-0063`'s 0.0706 failing to reproduce | **no** | departure `0.0` at nine significant digits |
| 5 | the minimax returning worse than its equal-spring start | **no** | it is a test |

**Two predictions of this task's own failed, in code, and are reported rather than repaired.**

1. The Plan expected the 2 nm state to be the antagonist and the exclusion argument to carry the answer.
   **The antagonist is the 5 nm LAYER**: the two states of that device are anti-parallel *to each other*
   (cosine −0.9427), so excluding the 2 nm state from the 10 nm devices does not rescue the device that
   owns it.
2. The Plan expected `C-0064`'s subset dichotomy to transfer exactly. **It transfers in direction and
   not in exactness**: of the 14 subsets mixing the 2 nm state with a 10 nm state, 12 fail and **2 are
   flat** (the whole group runs 0.0985–0.1128), against 0 of 14 flat on the 3 × 15 grid; the other 17
   run 0.0191–0.0672 and all 17 are flat. **Both exceptions pair the 2 nm state with the 0.5 mM 10 nm
   state** — 0.0985 and 0.0986 — which is the mildest of the three 10 nm loads. The five-state
   portfolio duty reaches **0.1124** here against `C-0064`'s 0.1254 on the grid: better, and still
   outside.

---

## Validity range

- **TRL 1–3. Nothing is measured, and the motif is not demonstrated.** `C-0055`'s free lever on one
  crossover remains this programme's own construct (62 recorded queries), and Ke et al.'s 8 bp staple
  break yield cost is unpriced at 34 arms.
- **The flatness is a dishing convention at one grid resolution**, and the margin at the tightest 10 nm
  range is 1.12×. The sampling sensitivity was therefore measured **at that range too**: 41 / 81 / 161
  samples give 0.08959 / 0.08962 / **0.08969**, a departure of **0.08 %**, against 1.1 % at the design
  device's range (0.0788 / 0.0789 / 0.0797). Neither consumes the margin, but a placement quoted at
  0.098 would not be safe against the second.
- **Nested subdivisions 1 ⊂ 2 ⊂ 4** move the design range's reading 0.07875 → 0.07885 → 0.07885
  (`3.1e−5`), and adding two interpolated intermediate gaps moves it by **exactly zero at nine
  significant digits**.
- **`T-5b`'s 10 % is a CONVENTION, and this result is sensitive to it.** At **8 %** the 2 mM and 0.5 mM
  ranges still clear with equal springs (0.0789, 0.0853) and the **10 mM one does not** (0.0896); at
  **5 %** none of the four clears with equal springs, and three of the four clear with a distribution
  (0.0291, 0.0365, 0.0382) while the 5 nm device's 0.0565 does not.
- **The load profiles are `C-0022`'s** and inherit its whole validity range: mean field, point ions, a
  2-D solve with the corner bracketed rather than solved, an **unsourced rim charge** worth 1.85 × on
  the collar, and a gap filled with free buffer.
- **Where `C-0022` did not solve a device's compressed end at its own bias**, the range brackets it with
  the two neighbouring solved biases — a **wider** requirement than the device faces, not a narrower one.
- **The interpolated intermediate gaps are linear interpolations** of `C-0022`'s solved
  `(depth, width, rim)` triples. They are a discretisation check, not new solves, and no verdict rests on
  them.
- **The exhaustive enumeration covers the centro-symmetric family only**, at 2 or 3 arms per row, at the
  two phases the congruence admits. `C-0063`'s own open item — that the 22 asymmetric phases are searched
  and not enumerated — is untouched here.
- **The distribution results are a DESCENT** reporting the best point found, never a global optimum; the
  per-state floor is rigorous and the optimum is not. And per `C-0064`, a 34-parameter optimum is a member
  of a *manifold*: its objective is stable, its per-path spread is not, and **no two-level quantisation of
  it was searched here** — `C-0064`'s rule is *quantise to price, never to find*.
- **Linear Winkler foundation at `C-0001`'s secant, ×1 only**; static, single layer, 300 K.
- **One arm count and one arm** — `C-0055`'s 34 and `C-0039`'s 8.16439 nm. A different count re-solves
  the arm and re-opens the placement.
- **`C-0055`'s own greedy placement is not re-run over ranges here.** It is the same 34 roots differently
  placed, and `C-0061`/`C-0063` already convict it at the design state (0.4156, 1.35× *worse* than no
  coupling); a range verdict on a design that fails at a state it contains would add nothing. Everything
  here is `C-0063`'s swept placement, and the enumerations sweep the family that contains both.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `C-0063`'s placement | 34 roots at phase 24 | **CITED**, read at run time from `gpd/results/T-125-*.json`, and re-checked against the phase-24 upward lattice and its centro-symmetry |
| `C-0022`'s solved collars | 13 states | **CITED**, read at run time, keyed on `(concentration, gap, bias)` |
| `C-0050`'s dead-load strokes | 7.4235 / 6.0135 / 1.5299 nm | **CITED**, read at run time from `gpd/results/T-108-*.json` |
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| duplex `EI`, `GJ`, `S` | 230, 460 pN·nm²; 1100 pN | **CITED, CanDo MODEL INPUTS** |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad | **CITED, FITTED**, Chen et al. (2014) SI, via `C-0009` |
| crossover spacing, rise per base pair | 32 bp, 0.34 nm | **CITED**, Rothemund (2006) / Ke et al. (2009) |
| the 10 pN unzip allowable, the 48–65 pN shear band | | **CITED** via `C-0006`/`CH-0029` |
| `C-0017`'s mandate | 33.3333 pN/nm | **CITED**, itself §3 arithmetic |
| `T-5b`'s tolerance | 0.10 | **CITED CONVENTION** |
| §3 parameters | 100 pN, 3 nm, 40 × 40 nm | **CITED** |

Everything else — the multi-state root bank and its reciprocity, the per-state floors, the cosine matrix,
the four operating ranges, the 31 subset minimaxes, the two exhaustive range-objective enumerations, the
distribution family, the force and thermal budgets and every convergence reading — is **derived here in
code**, with `C-0009`'s, `C-0022`'s, `C-0055`'s, `C-0058`'s, `C-0063`'s and `C-0064`'s pipelines **re-run
rather than tabulated**.

## Still open — named, not answered

1. **A placement flat over BOTH layers.** This claim finds one for each separately — `C-0063`'s at phase
   24 for the 10 nm devices, and a phase-8 member at 0.0895 for the 5 nm one — and finds their argmins
   mutually poor (0.2416 and 0.2000 at the other's duty). Whether any single placement, or any of the 22
   **asymmetric** phases, serves both is unexamined, exactly as in `C-0063`.
2. **A two-level design over a range.** The ratios wanted here are 1.72–2.32, far inside what `C-0060`
   prices, but no two-level member was searched for — and `C-0064`'s lesson is that a constrained family
   must be searched **in**, not projected **onto**.
3. **Whether a Gen-1 device must run at more than one layer height.** If it must, the 5 nm result is the
   governing one; if it must not, the 10 nm result is. **Nothing in §3 says which**, and that is the same
   specification gap `C-0064` named for buffers.
4. **The 14× gap to the per-state floor** (0.0056 against 0.0789 with equal springs) is unexplained. It
   is a loose bound — it ignores the mandate — but no claim is made that 0.0789 is optimal over
   *distributions*.
5. **`C-0063`'s own open items are untouched**: the 22 asymmetric phases, the joint placement ×
   distribution optimisation, the arm directions and the 8 bp staple-break yield.

## Challenges

**Raises [`CH-0080`](../challenges/CH-0080-the-equal-spring-advantage-belongs-to-the-ten-nanometre-layer.md)**
against `C-0063`'s Deliverable 2 — *"it needs no distribution at all"* — which is a statement about a
**10 nm** layer's load field and not about the placement. **No number in `C-0063` moves**; its 0.0706
reproduces to the last emitted digit and its placement is strengthened, being the range argmin as well.

**None stands against this claim.** The four ways it would fail:

1. **A `C-0022` load profile materially different from the solved one** — its rim charge is unsourced and
   worth 1.85× on the collar, and the 5 nm layer's states are the ones this claim is most exposed to.
2. **A tolerance materially tighter than `T-5b`'s 10 %** — at 5 % no range clears with equal springs.
3. **A demonstration that a Gen-1 tile must be flat at more than one layer height without re-tuning**,
   which would make the 5 nm failure the governing answer rather than a scope statement.
4. **A different arm count**, which re-solves `C-0039`'s arm and re-opens the placement entirely.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological
grounds rather than overwriting it.
