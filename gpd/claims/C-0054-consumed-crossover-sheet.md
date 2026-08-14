# C-0054 — A crossover cannot be a hinge and an interface at once, so the question is not how much rigidity the sheet loses but whether it is still one body: a **pigeonhole** caps a connected sheet's hinge budget at **42 of 56 (75.0 %)** and `C-0046`'s admissible region is **80–100 %**, so every design in it severs the tile — into 4, 9 and **fifteen** pieces — while the buildable ceiling costs only **11 %** of the flatness and still clears §3's acceptable stroke at **39–42 paths**

| | |
|---|---|
| **Task** | [`T-110`](../tasks/T-110-consumed-crossover-sheet.md), raised by [`C-0046`](C-0046-fewer-longer-flexures.md)'s *"Still open"* item 1, which it names *"the largest open item this claim leaves and it is new"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the count belongs to |
| **Verification type** | **logical** (a pigeonhole on a lattice whose pitch is cited and whose inventory `C-0015` counts — no simulation can move a count) **+ in-silico** (`C-0009`'s beam-and-hinge grillage re-run with the spent crossovers **removed from the assembly**, `C-0006`'s continuum plate beside it with `D_⊥` smeared, under `C-0022`'s **solved** electrostatic profile read from its own result file, and `C-0046`'s two-spring elastica re-run as a library at every path count from 34 to 56) |
| **Verdict** | **PASS on the predicate, and the answer is NEGATIVE for the region `C-0046` reports and POSITIVE for a region three grid steps below it.** **The geometric question settles first and it settles everything: hinge use and interface use are EXCLUSIVE at the site.** `C-0040` defines a hinge line as crossovers sharing *"one interface and one pair of bodies"* and `k_θ` as the *interhelical dihedral* spring, so a crossover that hinges an arm is a crossover that no longer joins its two sheet duplexes — it supplies neither the spring nor the vertical link. From that, one division: **a connected sheet needs one retained crossover on each of its 14 interfaces, so at most `56 − 14 = 42` (75.0 %) can be spent**, and `C-0046`'s three surviving designs spend **45, 50 and 56** (80.4 %, 89.3 %, 100 %). **Every one of them severs the tile — into at least 4, 9 and exactly 15 pieces — before any matrix is assembled.** **`D_⊥` does not degrade linearly, it collapses**: the interfaces bend in **series**, so the honest rigidity is a harmonic mean and is **exactly zero** the moment one interface empties, where the smeared continuum still reports 0.667 pN·nm (20 % of 3.397) and an anisotropy of 128 instead of unbounded. Under `C-0022`'s solved load on `C-0015`'s 3 × 15 grid the tile dishes **0.218** of the stroke intact, **0.242 at the 42-crossover ceiling (+11 %)** and **0.465 at 45 (2.13×)** — **the step is at severance, not at consumption**. The load distribution *improves* (peak crossover force 0.150 → 0.105 → **exactly 0** pN) for the trivial reason that a deleted load path cannot be overloaded. **`C-0010`'s insensitivity is correct and is not the relevant channel**: the smeared plate moves 31 % over the whole range while the lattice moves **117 %**, because a crossover is **two** elements and only one of them is `D_⊥`. **The generous reading — "a hinge IS an attachment" — is the worse design, not the better one**: at the same 45 paths, attaching at the hinge sites dishes **1.535** against 0.465, because a 32 bp × 2.69 nm crossover lattice with a parity is not a three-column flatness grid. And the branch is not dead: **`C-0046`'s unresolved `34 < n ≤ 45` threshold is 39**, so the window on a sheet in **one piece** is **39 ≤ n ≤ 42**, delivering 3.005–3.063 nm — and **none of `C-0046`'s three reported designs is in it.** Raises [`CH-0066`](../challenges/CH-0066-the-surviving-designs-consume-the-sheets-own-connectivity.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number: no duplex has been built standing normal to a single-layer sheet, and a duplex END has at most two covalent links. |
| **Provenance** | `gpd/results/T-110-consumed-crossover-sheet.json`, produced by `structure.ConsumedCrossoverSheetStudyKt`; model in `src/main/kotlin/structure/ConsumedCrossoverSheet.kt` (+ one defaulted constructor parameter on `OrigamiGrillage`); **7 cheap bounds, 21 rigidity/connectivity records, 120 solved flatness states, 14 variance records, 15 ceiling records, 10 convergence records, 14 upstream reproductions, 4 runtime falsifiers**; **23 gate-named tests in `src/test/kotlin/structure/ConsumedCrossoverSheetTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with three concurrent agents' mid-TDD test files dropped by `--drop-file` (`window/SecondResynthesisTest.kt`, `anchoring/HingeArmArrayPackingTest.kt`, `anchoring/CrossbarJunctionTrioTest.kt`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured **2.69 nm**, **8 symmetrically centred crossover columns** (`T-10`'s nominal layout, 56 crossovers); `C-0022`'s **solved** edge profile at 2 mM, a 10 nm gap and 0.192 V; `C-0001`'s foundation secant; `C-0017`'s 33.3333 pN/nm mandate as `n` equal springs |
| **Consumes** | [`C-0009`](C-0009-discrete-lattice-tile.md)/`OrigamiGrillage` (the grillage, `D_⊥ = k_θ d/p`, the 25.6× anisotropy, the cut identity — **re-run as a library**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 49–56 inventory, the 3 × 15 grid, *"shapes, not counts"*), [`C-0040`](C-0040-hinge-line-census.md) (**the definition of a hinge line, which is what settles the geometric question**, the 32 bp per-interface pitch, the four-crossover census), [`C-0046`](C-0046-fewer-longer-flexures.md) (`tradePoint`, the three surviving designs, the unresolved path-count bracket — **re-run as a library**), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved profile, read from `gpd/results/T-3b-tile-edge-load-profile.json`, keyed on concentration, gap **and bias**), [`C-0047`](C-0047-single-column-flatness.md)/[`C-0026`](C-0026-one-row-per-duplex.md)/[`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md) (the flatness pipeline and its saturation), [`C-0010`](C-0010-tile-positional-variance.md) (`positionalVarianceBudget`, the `D_⊥` insensitivity), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, the flatness convention), [`C-0034`](C-0034-guided-arm-anchorage.md)/[`C-0029`](C-0029-perpendicular-junction-routing.md) (the `A2` far anchorage), [`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the 10 pN unzip allowable) |
| **Raises** | [`CH-0066`](../challenges/CH-0066-the-surviving-designs-consume-the-sheets-own-connectivity.md), against `C-0046`'s best-point verdict and its 45–56 window |
| **Challenged by** | [`CH-0068`](../challenges/CH-0068-the-hinge-inventory-is-not-the-sheets-own.md), from [`C-0055`](C-0055-unused-junction-site.md) (`T-119`, the falsifier this claim names under *Challenges* item 1 and files as a task) — against the **cheap bound** and the **42-crossover ceiling**, **not** against the exclusivity argument, which `C-0055` upholds and uses. A square-lattice helix has **four** crossover azimuths at 8 bp and a single-layer sheet occupies **two** (Ke et al., *JACS* **131**:15903, read directly): `8 bp × 33.75°/bp = 270°` exactly, so the empty pair points **out of the sheet plane** and is **less** strained than the used one (4.286° against 8.571°). The tile offers **161–176** junction sites and builds **49–56**, so the hinge ceiling is **52–60 with every interface crossover retained and the sheet in one piece at every count**. **`CH-0066`'s conclusion survives** — §3's 45 still does not place on a 40 nm tile — but for the upward **root pitch** (10.88 nm against an 11.82 nm demand, giving **34**), not for severance. **Everything computed here on a CONSUMED sheet is untouched**, because an upward hinge consumes nothing |

---

> ℹ️ **Convergence with [`C-0053`](C-0053-hinge-arm-array-packing.md) (`T-116`), filed in the same iteration from the opposite direction, and the two compose.**
>
> This claim's ceiling is a **pigeonhole** and it charges an arm **exactly one crossover and nothing else**, so
> **42 is an upper bound on the hinge budget of a connected sheet, not a placement.** `C-0053` solves the plan
> view of the same `E5a1` array and finds that a real arm is a length of the host's own duplex cut free at both
> ends: it *also* buries crossovers beneath itself and removes 65.4 % of the host's duplex length, so at its
> self-consistent **43** arms the host has **no bonded component at all**, and **the count that leaves all
> fifteen duplexes bonded is 25** — 1.68× below this claim's ceiling and 1.80× below §3's 45.
>
> **Neither number contradicts the other and both refuse `C-0046`'s designs.** Where they differ is what an arm
> costs: 42 is what the *counting* permits, 25 is what the *geometry* delivers. **Quote 25 as the design number
> and 42 as the bound**, and read this claim's `39 ≤ n ≤ 42` window as *necessary* rather than sufficient —
> `C-0053` closes it from the other side.

---

## The claim, in one line

**The programme has been spending a resource without asking what it was for: a crossover is the sheet's only across-helix load path *and* the only spring a flexure hinge can be made of, and it cannot be both, so the binding constraint on a hinge array is not stiffness at all but the connectivity of the body it is bolted to — 14 interfaces need 14 crossovers, that leaves 42, and every design this programme reports needs more.**

---

## The geometric decision, taken explicitly and first

`T-110` was formulated around a question nobody upstream asks: **is a crossover used as a hinge still an interface crossover for the sheet?** Three answers were possible — exclusive, additive, partially shared — and the whole task turns on which.

**It is exclusive at the site**, and the argument is `C-0040`'s own, used rather than restated:

1. A **hinge line** is *"a maximal set of crossovers that share **one interface** and **one pair of
   bodies**"*. A hinge that turns puts those two bodies at an angle to each other, so whatever is
   outboard of the line is **not** the sheet.
2. `k_θ` is the **interhelical dihedral** constant — it resists rotation of duplex `b+1` relative to
   duplex `b` *about their common interface line* — which is why `n k_θ` is the right spring for a
   hinge whose axis runs along `x` **and for no other axis** (`C-0040`), and why `E5`'s arm can be
   priced with it at all.
3. A reciprocal strand exchange has **two** strands and **two** partners. A junction site that
   exchanges with a flexure arm is not also exchanging with the neighbouring sheet duplex — the
   same counting discipline `C-0029` applied at a duplex **end** applies at a junction **site**,
   and no force field can add a third partner.

So a consumed crossover supplies **neither** the dihedral spring **nor** the vertical link, and that is what `OrigamiGrillage.consumedCrossovers` removes. **This is the load-bearing premise of the claim and it is stated rather than assumed** — the way it would fail is set out under *Challenges* below.

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rigidities and moments **pN·nm**, pressure
  **pN/nm²** (= 1 MPa exactly); `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- **Plan view.** `x` **along** the helices, `y` **across** them, origin at the tile centre; `w`
  positive **downward**, compressing the polymer layer.
- **An interface** is an adjacent duplex pair `(b, b+1)`; a 15-duplex sheet has **14**, each
  carrying the columns of one parity, so the per-interface pitch is **32 bp = 10.88 nm** and the
  nominal eight-column layout carries **56** crossovers, four per interface.
- **The consumed fraction** `f = spent/inventory`. `C-0046`'s three surviving designs are
  `45/56 = 0.804`, `50/56 = 0.893` and `56/56 = 1.000`.
- **A component** is a connected set of duplexes under the retained crossovers.

---

## The cheap bound, which ran first and decided the verdict

&nbsp;&nbsp;&nbsp;&nbsp;**`f_max = 1 − (D − 1)/N = 1 − 14/56 = 0.750`**

The interfaces of a single-layer sheet form a **path** graph on its duplexes, so a connected sheet needs at least one retained crossover on **every** interface. Four arithmetic operations, no mesh, no fitted constant, and no simulation can move it.

| | value | what it settles |
|---|---|---|
| the ceiling at the ten **56**-crossover phases | **42 crossovers, 75.0 %** | `C-0046`'s region is 80–100 %; **every point of it is above the ceiling** |
| the ceiling at the twenty-two **49**-crossover phases | **35 crossovers, 71.4 %** | the other 22 phases are **worse**, not a way out |
| pieces at `C-0046`'s best point (45 spent) | **≥ 4** | 11 retained over 14 interfaces leaves ≥ 3 empty |
| pieces at 50 spent | **≥ 9** | |
| pieces at 56 spent | **15** | fifteen separate duplexes — *the sheet is gone*, not softened |

> **The falsifier this bound was declared against did not fire.** Had it come out at or above 1.0,
> consumption could never disconnect the sheet and the task would have closed in a paragraph.

---

## Deliverable 1 — `D_⊥(f)`, on the two conventions, and the anisotropy

Consumption spread as evenly over the interfaces as the count allows — the **connectivity-optimal** design, asserted as a gate-3 test to leave no more pieces than any other pattern:

| spent | `f` | retained | empty interfaces | **`D_⊥` uniform curvature** | **`D_⊥` uniform moment** | `D_∥/D_⊥` (curv.) | **pieces** |
|---|---|---|---|---|---|---|---|
| **0** | 0.000 | 56 | 0 | **3.3968** | 3.8994 | 25.2 | **1** |
| 14 | 0.250 | 42 | 0 | 2.5476 | 2.9245 | 33.6 | 1 |
| 28 | 0.500 | 28 | 0 | 1.6984 | 1.9497 | 50.3 | 1 |
| **42** | **0.750** | **14** | **0** | **0.8492** | **0.9748** | **100.7** | **1** |
| **45** | **0.804** | 11 | **3** | 0.6672 | **0.0000** | 128.2 | **4** |
| **50** | **0.893** | 6 | **8** | 0.3639 | **0.0000** | 234.9 | **9** |
| **56** | **1.000** | 0 | **14** | **0.0000** | **0.0000** | **unbounded** | **15** |

**The two columns are the finding.** Bending a sheet across the helices is fourteen hinge lines in **series**, so the effective rigidity is a **harmonic** mean:

&nbsp;&nbsp;&nbsp;&nbsp;`D_⊥ = L_y k_θ /(L_x Σ_i 1/n_i)` &nbsp;against&nbsp; `D_⊥ = k_θ d² N_ret/A`,

and the two agree on a uniform lattice up to exactly `(D/(D−1))² = (15/14)² = 1.1480` — `C-0009`'s own `(n−1)/n` duplex-count residual, squared, asserted as an identity rather than tolerated. On a **depleted** lattice they part company completely: the arithmetic mean degrades linearly and the harmonic one is **annihilated by a single empty interface**, because an empty interface is a free hinge.

&nbsp;&nbsp;&nbsp;&nbsp;**A continuum plate can only express the first. That is the whole excess, and it is a change of kind, not of number.**

---

## Deliverable 2 — flatness under `C-0022`'s SOLVED load, lattice and plate

`C-0015`'s 3 × 15 = 45 attachments at `C-0017`'s mandate, `C-0022`'s solved profile at 2 mM / 10 nm / 0.192 V. **A flatness count is meaningless without its load case** (`CH-0034`), and a uniform load dishes a free tile **exactly zero** at every consumption level — asserted as a runtime falsifier in the study and as a test.

| spent | pieces | **no coupling** | **`GRID` — `C-0015`'s 3 × 15** | **`AT_HINGE` — attach where you consumed** | lattice/plate (`GRID`) |
|---|---|---|---|---|---|
| **0** | 1 | 0.308 | **0.218** | — | 0.919 |
| 14 | 1 | 0.337 | 0.224 | 0.568 | 0.914 |
| 28 | 1 | 0.362 | 0.228 | 0.998 | 0.891 |
| **42** | **1** | 0.362 | **0.242** | 1.524 | 0.870 |
| **45** | **4** | 0.564 | **0.465** | **1.535** | **1.624** |
| 50 | 9 | 0.564 | 0.465 | 1.579 | 1.492 |
| **56** | **15** | 0.564 | 0.466 | 1.375 | 0.224 |

*(peak dishing as a fraction of the free-tile stroke; `T-5b`'s convention is 0.10)*

Three things, none of them a restatement:

1. **The step is at severance, not at consumption.** Spending 42 of 56 crossovers costs **11 %** of
   the dishing; spending three more costs **92 %**. The design variable that matters is a
   **topological** one.
2. **The lattice/plate ratio flips exactly there** — 0.87 → **1.62**. Below the ceiling the lattice
   is the *stiffer* model, which is `C-0009`'s rule for a smooth load and a point reaction; above
   it the lattice is **62 % softer** than the smeared plate, because the plate cannot represent a
   severed interface at all. `CLAUDE.md`'s *"a discretisation is not automatically a relaxation"*
   holds on both sides and **changes sign** at the connectivity ceiling.
3. **Nothing here reaches `T-5b`'s 10 % tolerance**, at any consumption level — which is
   `CH-0034`'s saturation and not a new failure.

---

## Deliverable 3 — `C-0006`'s load distribution, which improves as the sheet fails

| spent | peak crossover force [pN] (`GRID`) | peak duplex shear [pN] | unzip margin | (`AT_HINGE`) crossover [pN] |
|---|---|---|---|---|
| 0 | **0.1504** | 0.793 | 66× | — |
| 28 | 0.1350 | 0.818 | 74× | 1.809 |
| 42 | 0.1089 | 0.822 | 92× | **2.188** |
| 45 | 0.1054 | 0.822 | 95× | 1.790 |
| 56 | **0.0000** | 0.817 | — | 0.000 |

&nbsp;&nbsp;&nbsp;&nbsp;**The peak per-load-path force falls monotonically to exactly zero, and it is not a design success: a crossover that has been removed cannot be overloaded.** `C-0026`'s exact zero is reached here by **deleting** the load path rather than by balancing it, and the duplex's own transverse shear runs the *other* way and is unmoved (0.79 → 0.82 pN against a 65 pN nicked ceiling). **Every per-load-path allowable in the programme is discharged with two orders of margin at every consumption level, and that is exactly why the allowables are not the constraint here.**

The `AT_HINGE` column is 12–20× the `GRID` one and still 4.6× inside the 10 pN unzip allowable.

---

## Deliverable 4 — `C-0010`'s positional variance, and the channel it does not travel on

Free tile, no coupling, `C-0001`'s foundation at the **working point**, `C-0010`'s own budget:

| spent | **lattice dishing RMS [nm]** | / intact | lattice centre RMS [nm] | plate `D_⊥` | **plate area RMS [nm]** |
|---|---|---|---|---|---|
| 0 | **0.9664** | 1.000 | 0.821 | 3.345 | **0.9247** |
| 14 | 1.2391 | 1.282 | 0.861 | 2.509 | 0.9389 |
| 28 | 1.4385 | 1.489 | 1.087 | 1.673 | 0.9589 |
| 42 | 1.6515 | **1.709** | 1.557 | 0.836 | 0.9925 |
| 45 | 1.7552 | **1.816** | 1.557 | 0.656 | 1.0039 |
| 56 | **2.0995** | **2.173** | 1.575 | ~0 | **1.2127** |

&nbsp;&nbsp;&nbsp;&nbsp;**`C-0010`'s *"a 2× change in `D_⊥` moves the answer by 2.5 %"* is CORRECT, is reproduced here, and is not the relevant channel.** The smeared plate moves **31 %** across the *whole* range — consistent with 2.5 % per factor of two — while the lattice moves **117 %**, a factor of **3.8** more.

**The reason is that a crossover is two elements and only one of them is `D_⊥`.** The dihedral spring carries the across-helix bending rigidity; the **vertical link** is a *constraint* tying two duplex surfaces together, it carries no bending rigidity at all, and a continuum plate has **no parameter for it**. Scaling `D_⊥` is therefore not a model of crossover consumption, and any downstream argument that reads `C-0010`'s insensitivity as insensitivity *to the sheet's connectivity* is reading a bound on the wrong body — the same class of error as `CH-0021` and `CH-0056`.

---

## Deliverable 5 — the connected ceiling on `C-0046`'s own elastica, and a bracket it left open

`C-0046`'s `tradePoint` re-run as a library at `h = 1`, `C-0034`'s `A2` anchorage, the standing placement (secant 33.3333 pN/nm at 3 nm), `C-0023`'s 40 pN/nm ceiling:

| paths | keeps the sheet in one piece | arm [nm] | bp | tangent(3) | **usable stroke [nm]** | clears 3 nm |
|---|---|---|---|---|---|---|
| 34 | yes | 8.164 | 24.0 | 40.81 | 2.905 | no |
| 38 | yes | 8.534 | 25.1 | 40.11 | 2.986 | no |
| **39** | **yes** | **8.623** | **25.4** | **39.96** | **3.005** | **yes — the threshold** |
| 40 | yes | 8.710 | 25.6 | 39.82 | 3.025 | yes |
| 41 | yes | 8.797 | 25.9 | 39.68 | 3.044 | yes |
| **42** | **yes — the ceiling** | **8.882** | **26.1** | **39.54** | **3.063** | **yes** |
| 45 | **no (4 pieces)** | 9.131 | 26.9 | 39.18 | 3.119 | yes |
| 50 | **no (9 pieces)** | 9.527 | 28.0 | 38.67 | 3.209 | yes |
| 56 | **no (15 pieces)** | 9.973 | 29.3 | 38.18 | 3.312 | yes |

&nbsp;&nbsp;&nbsp;&nbsp;**`C-0046` bracketed its path-count threshold at `34 < n ≤ 45` and did not resolve it. It is 39.** So the window on a sheet **in one piece** is **`39 ≤ n ≤ 42`** — four path counts wide, 3.005–3.063 nm of usable stroke — and **none of `C-0046`'s three reported designs is in it.** The branch survives; the designs do not.

The cost of moving from `C-0046`'s best point to the best buildable one is **7.4 %** of the usable stroke (3.312 → 3.063 nm) and it buys a sheet that is a single body.

---

## The five verification gates

Executed as **23 gate-named tests** in `src/test/kotlin/structure/ConsumedCrossoverSheetTest.kt`; `tools/verify.sh` **BUILD SUCCESSFUL**, with three concurrent agents' mid-TDD test files dropped by `--drop-file`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `D_⊥` is `k_θ` times a length — doubling `d` quadruples it and halving the retained count halves it, both to `1e−14`; the consumed fraction is a count over a count and the ceiling is a pure ratio at **both** phase inventories; the anisotropy is a pure ratio and reports a **sentinel** where it is unbounded (`kotlinx.serialization` refuses `Infinity`); unphysical arguments throw at **six** entry points | **PASS** |
| **2 — limiting cases** | **a uniform load dishes a free tile exactly zero at 0, 14, 28, 42, 45 and 56 spent** — the strongest falsifier available, and it is also a runtime check in the study, in the lattice **and** the plate; **zero consumption reproduces `C-0009`'s lattice identically** (same 56 crossovers, same dishing and same peak force to `1e−15`, not to a tolerance); full consumption leaves no crossover, zero rigidity on **both** conventions and fifteen pieces; the two conventions agree at `(15/14)²` on a uniform lattice and **one empty interface annihilates the series one** | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | nested mesh 1 ⊂ 2 ⊂ 4 (`9.8e−4` → `2.2e−5`); the crossover link penalty over `1e3 … 1e6` (`2.0e−7`); the plate's Ritz basis degree 8/10/12 (`5.1e−5`); the result file re-emitted through `tools/study.sh` and diffed **byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **14 reproductions, worst departure `9.8e−4`** — `C-0009`'s `D_⊥` on both readings (3.345 / 3.397), its `1.015467` lattice/continuum ratio, its `D_∥ = 85.50` and its `25.56` anisotropy; `C-0015`'s 56 inventory; `C-0040`'s four-per-interface census; `C-0047`'s **0.218** (3 × 15) and **0.695** (1 × 15) under the same solved profile; `C-0022`'s **0.308** free uncoupled tile; `C-0046`'s `(45, 1)` arm, tangent and usable stroke and its `(56, 1)` usable stroke | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **Union-find against the closed form.** The component count is computed by a union-find over the
   retained crossovers and asserted equal to `1 + (empty interfaces)` at **every** consumption
   level of **every** pattern. The closed form is a theorem about the interfaces forming a *path*
   graph; nothing in the union-find knows about the path.
2. **The pigeonhole ceiling is achieved, and only by the spreading pattern.** 42 leaves one piece,
   43 does not, and the two structured patterns cannot reach it — so the bound is tight and is a
   property of the arithmetic rather than of one arrangement. Asserted alongside a sweep showing
   **no pattern leaves fewer pieces than the spreading one**, at any level.
3. **Global force balance at every consumption level and pattern** — the applied load equals what
   the foundation and the anchors carry, to `1e−9`, including on a sheet in fifteen pieces.
4. **An emptied interface transmits nothing**, and a retained one still carries the shear crossing
   it from cut equilibrium — `C-0009`'s own identity, reproduced to `1e−6` where it is valid.
5. **The imposed-curvature hinge energy is exactly linear in the retained count**, asserted as a
   ratio of exactly 2 between 56 and 28 retained, and asserted equal to the closed form at all 21
   states as a runtime check in the study.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the pigeonhole bound at or above 1.0 | **no** | **0.750**, and `C-0046`'s whole region is above it |
| 2 | `f = 0` failing to reproduce `C-0009`/`C-0047` | **no** | 14 reproductions, worst `9.8e−4` |
| 3 | a uniform load dishing anything but zero | **no** | `< 1e−9` at every level, lattice and plate |
| 4 | the two `D_⊥` conventions disagreeing at `f = 0` | **no** | exactly `(15/14)²` |
| 5 | the connectivity ceiling clearing the acceptable stroke comfortably | **partly, and it is the finding** | it clears it, but by **2.1 %** (3.063 against 3.000 nm), so the surviving window is four path counts wide |

**A result that was not anticipated:** the task was formulated as *"how much does the sheet lose?"* and the answer is that **the sheet does not lose a quantity, it loses a property**. Up to 42 crossovers spent, everything degrades gently and predictably; past 42 the object stops being a sheet and every quantity steps at once. The binding constraint on a hinge array is **topological**, and no stiffness argument could have found it.

**A second one:** the *generous* reading of the geometry — that a hinge is also an attachment, so consumption pays for itself — is **3.3× worse** at the same path count, because a crossover lattice and a flatness grid are different **shapes**. `C-0015`'s *"shapes, not counts"* in a new place, for the fourth time in this programme.

---

## Does `C-0046`'s verdict survive?

**Its model, its ledgers, its elastica and its degeneracy result survive untouched and are re-run rather than restated. Its three reported designs do not.**

| `C-0046` said | this claim finds |
|---|---|
| the arm is a function of the product `n·h` alone; *fewer, longer* is the wrong direction | **untouched**, and not re-examined here |
| *"the best point of the whole space is `(56, 1)`"*, usable stroke 3.312 nm | **it consumes 100 % of the sheet's crossovers and leaves fifteen separate duplexes.** The best *buildable* point is `(42, 1)` at 3.063 nm — **`CH-0066`** |
| *"only 3 of the 31 swept points clear even the acceptable stroke: 45, 50 and 56"* | **all three sever the sheet.** On the swept grid the clearing points that do **not** are 39–42, which its grid did not contain |
| *"the window's width is 1.24× in path count and zero in hinge count"* | the **buildable** window is `39 ≤ n ≤ 42`, a width of 1.08× |
| *"the path-count threshold lies between 34 and 45 and is not resolved further"* | **resolved: 39** |
| *"what it buys in coupling it takes out of `D_⊥` … that is not priced here"* | **priced, and `D_⊥` is not the currency.** The currency is connectivity, and `D_⊥` reaches zero at the same moment for the same reason |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** `C-0028`'s and
  `C-0029`'s literature findings are unchanged and upstream of every number.
- **The exclusivity decision is a GEOMETRIC PREMISE, not a computed result**, and it is the one
  thing on which the whole claim rests. It is `C-0040`'s own definition of a hinge line applied
  consistently; the way it would fail is set out under *Challenges*.
- **The lattice is `T-10`'s nominal layout — eight symmetrically centred columns, 56 crossovers.**
  `C-0015` shows the phase is a design variable worth 19 % in the peak force and that 22 of the 32
  phases carry **49** crossovers instead. The pigeonhole ceiling is computed at both (42 and 35)
  but the flatness, load and variance sweeps are run at the nominal phase only.
- **Three consumption patterns are swept and the spreading one is shown optimal for connectivity**,
  but *which* crossovers a real `E5a` array would take is a **plan-view** question this claim does
  not answer — that is `T-111`/`T-116`. Where the empty interfaces fall is arbitrary within the
  spreading pattern at counts below 14 retained, and the flatness numbers at 45–56 spent inherit
  that arbitrariness; the **connectivity** verdict does not, because it is a pigeonhole.
- **The per-interface cut identity is DEGRADED by severance and the global one is not.**
  `shearAcrossInterface` integrates `k_f w` over panels that straddle the tributary strips, and
  across a severed interface the reconstructed deflection field is genuinely discontinuous, so a
  Gauss-Legendre panel spanning the jump misreads the foundation reaction by ~0.06 pN. The
  conservation gate is therefore taken on the **global** balance, which is exact, and the cut
  identity is asserted only where it is valid. **This is a diagnostic limitation, not a solver
  error**, and it is recorded in `CLAUDE.md`.
- **The `AT_HINGE` placement is a bound, not a design.** It puts one attachment at every consumed
  site at `C-0017`'s mandate divided equally; a real array would place fewer, stiffer paths.
  It is carried because it is the *generous* reading of the geometry and it still loses.
- **The plate's `D_⊥` is floored at `1e−9 pN·nm`** at full consumption, because `OrthotropicPlate`
  refuses a zero rigidity — which is itself the point: a continuum plate cannot have no
  across-helix rigidity and still be a plate.
- **`k_θ` is `C-0009`'s CITED, FITTED constant** (Chen et al., `α ∈ [0.6, 1.2]`). **It does not
  enter the connectivity verdict at all** — that is pure counting — and it enters `D_⊥` linearly,
  so the whole rigidity table moves ±20 % with it and no verdict moves.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT**, not a measurement.
- **The foundation is `C-0001`'s secant, un-swept here.** `C-0009` sweeps it ×[0.25, 4] and finds
  the lattice/plate ratios move by less than 1 %; the variance table uses the **working-point**
  stiffness, which is `C-0010`'s.
- **Static and single-layer**, exactly as `C-0009`.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| crossover spacing per interface | **32 bp** | **CITED**, Rothemund, *Nature* **440**:297 (2006), via `C-0015`/`C-0040` |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al., *Nature* **459**:414 (2009) |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| duplex `EI`, `GJ` | 230, 460 pN·nm² | **CITED, CanDo MODEL INPUTS** (Kim et al., *NAR* **40**:2862, 2012) |
| tile crossover inventory | **56 / 49** | **`C-0015`**, re-derived here through the lattice itself |
| the solved edge profile | depth −0.3029 over 8.939 nm, rim −0.5939 over 1.0 nm | **`C-0022`**, read from its own result file |
| per-path unzip allowable | 10 pN | **CITED, MEASURED** via `C-0006`/`CH-0029` |
| far anchorage `k_far` | 78.2353 pN·nm/rad | **`C-0034`**'s `A2`, at the **cited** 1.0 nm phosphate radius |
| `C-0009`/`C-0047`/`C-0046`'s published numbers | 3.345, 3.397, 1.015467, 85.50, 25.56, 0.218, 0.695, 0.308, 9.131, 39.18, 3.119, 3.312 | **CITED**, and every one reproduced here as a gate-5 test |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the pigeonhole and its two phase readings, the three consumption patterns and their optimality ordering, both rigidity conventions and their `(15/14)²` identity, every component count, every dishing, crossover force, duplex shear, fluctuation amplitude and placed arm, and the resolved path-count threshold — is **derived here in code**, with `C-0009`'s, `C-0022`'s, `C-0046`'s and `C-0047`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **Where a real `E5a` array's hinges would actually fall.** The consumption patterns here are
   designed extremes; the plan view is `T-116`'s question, **answered concurrently by `C-0053`**
   (see the banner above), and the *connectivity* verdict does not need it while the *flatness*
   numbers at 45–56 spent do.
2. **The flatness and fluctuation of `C-0053`'s own 25-arm design**, which is not one of the
   consumption levels swept here and whose hinges fall where its placement puts them rather than
   where these three patterns do. Between 14 and 28 spent this claim's `GRID` dishing moves 0.224 →
   0.228, so the answer is very likely *"indistinguishable from intact"* — but it is not computed.
3. **The crossover phase.** Every solve here is at `T-10`'s nominal eight-column layout; the 22
   seven-column phases have a **lower** ceiling (35 of 49) and are not swept.
4. **Whether a hinge could be built on a junction site the single-layer sheet does not use.** That
   is the one way the exclusivity premise fails, and it is a chemistry question, not a mechanics
   one — see *Challenges*.
5. **What the superstructure has to be** once the sheet is in one piece only by virtue of 14
   crossovers. A sheet at the ceiling has **one** crossover per interface, and `C-0009`'s own
   localisation result (an anchor is carried by its two nearest crossovers) says such a sheet is
   not a plate in any useful sense — its `ℓ_⊥/d` is not re-derived here.

## Challenges

**Raises [`CH-0066`](../challenges/CH-0066-the-surviving-designs-consume-the-sheets-own-connectivity.md)** against `C-0046`'s best-point verdict and its 45–56 window. **No number in any consumed claim fails to reproduce** — 14 reproductions, worst departure `9.8e−4` against a value `C-0047` quotes to three digits.

**None stands against this claim.** The four ways it would fail:

1. **A junction site the single-layer sheet does not use.** A square-lattice helix has four
   neighbour directions and a Rothemund sheet uses only two; if a flexure arm could be crossed over
   to at an azimuth the sheet never occupies, the two uses would be **additive** and this claim's
   central premise would fail. `C-0029`'s literature survey is the standing evidence against it —
   *"a duplex standing normal to a single-layer sheet is NOT an established motif"*, and every
   published out-of-plane body is held by a **pin** — but that is a statement about what has been
   built, not about what is impossible, and it is the most valuable single falsifier of this claim.
2. **A hinge line drawn from outside the tile.** `C-0040`'s `L5`, refused on plan area by `C-0041`
   at 12.3× the footprint.
3. **A superstructure that carries the across-helix load path instead of the sheet.** A severed
   tile bolted to a rigid frame at every fragment is a device; it is not the 40 × 40 nm origami
   sheet §3 specifies, and `C-0041` already finds the superstructure severs itself.
4. **A measurement of the crossover's own vertical compliance.** Modelled here as a constraint, as
   in `C-0009`; a soft link would add a load path and soften the *intact* sheet toward the depleted
   one, which narrows the gap this claim reports rather than closing it.
