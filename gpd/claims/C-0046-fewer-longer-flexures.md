# C-0046 — The trade is DEGENERATE on its own axis and runs the OPPOSITE way to its premise: the arm the crossover inventory can place is a function of the PRODUCT `n·h` alone — 5.387 nm at every point of the curve — while the far anchorage is per-FLEXURE, so *fewer, longer* flexures are strictly worse than *more, shorter* ones, and **no point of the `(path count, hinge count, hinge-line length)` space reaches §3's desired 10 nm stroke at either reading of the compliance ceiling**

| | |
|---|---|
| **Task** | [`T-99`](../tasks/T-99-fewer-longer-flexures.md), raised by [`C-0040`](C-0040-hinge-line-census.md)'s open item 2 |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Verification type** | **logical** (two exact ledgers on a lattice whose pitch is cited and whose inventory `C-0015` counts, plus a closed-form rigid-arm placement that needs no elastica) **+ in-silico** (`C-0039`'s two-spring **elastica** re-run as a library at every admitted point of the `(n, h)` grid, at **two** placements and **two** readings of the ceiling) |
| **Verdict** | **PASS on the method, and the escape route FAILS — `T-99` is settled and the answer is negative.** **`0` of 31 points at the standing placement and `0` of 29 at §3's own desired-clause placement reach the desired 10 nm stroke, at BOTH readings of `C-0023`'s ceiling** — so `T-107`'s question, running concurrently, **cannot move this verdict**: the desired stroke fails on the *geometry* and the *allowable* before the ceiling is consulted. The reason the trade cannot work is structural and was not anticipated: the three variables are **one budget spent twice** (`n·h ≤ 56`, `n(h−1)p ≤ 640 nm`, `h ≤ 4`), and on that budget the arm a rigid-armed array can place is `δ/sin θ` with `θ tan θ = k_target δ²/(N k_θ)` — **a function of the PRODUCT `n·h` and of nothing else, hence 5.387 nm at every one of the eight splits of `n·h = 56`**. What breaks the degeneracy is `C-0034`'s far anchorage, whose couple is **per-flexure**: the array's restraint is `N k_θ + n k_far`, so the placed arm **grows with the path count** — 6.903 nm at `(14, 4)`, 8.231 at `(28, 2)`, **9.973 at `(56, 1)`** — and *"fewer, longer"* is the wrong direction at every point. **The best point of the whole space is `(56, 1)`: an arm of 9.973 nm = 29.3 bp, a tangent of 38.17 pN/nm at §3's acceptable stroke, and a usable stroke of 3.312 nm** — it clears §3's **acceptable** 3 nm clause and misses its **desired** one by **3.02×**, and it spends **100 %** of the tile's crossover inventory to do it. **Only 3 of the 31 swept points clear even the acceptable stroke, and all three are at `h = 1`: 45, 50 and 56 paths.** §3's desired-clause placement `P10` (`k_c = 10 pN/nm` at 10 nm) *does* place arms of 11.4–18.1 nm that reach 10 nm inside the ceiling — and **every one of them is refused by `C-0017`'s stability floor**, which the placement misses by **2.34–2.79×**. That yields a bound owing nothing to the flexure branch at all: **placement `k_c = F/δ` and stability `k_c > |k_eff|` together cap the stroke at 3.58–4.27 nm at §3's 100 pN.** Raises [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number. |
| **Provenance** | `gpd/results/T-99-flexure-count-hinge-trade.json`, produced by `anchoring.FlexureCountHingeTradeStudyKt`; **7 cheap bounds, 65 ledger records, 60 trade records over 2 placements, 4 region records, 14 convergence records, 14 upstream reproductions, 5 verdict statements**; **26 gate-named tests in `FlexureCountHingeTradeTest`**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with one concurrent agent's mid-TDD file dropped by `--drop-file` (`src/test/kotlin/synthesis/DesiredStrokeReachTest.kt`, `T-108`); the result file re-run through `tools/study.sh` on a second isolated tree and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS-measured 2.69 nm; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; the element is `C-0034`'s `E5a` on `C-0039`'s inextensible two-spring elastica, **free to draw in** (`H = 0`), far anchorage `A2` = the arm's own duplex end at 78.2353 pN·nm/rad |
| **Consumes** | [`C-0040`](C-0040-hinge-line-census.md) (`perInterfacePitch`, `maximumHingeCount`, `hingeLineCensus` — **re-run as a library**; the 32 bp interface pitch, the four-crossover census, the 163.2 nm demand), [`C-0039`](C-0039-two-spring-elastica.md) (`TwoSpringElastica`, `elasticaArmForStiffness`, `elasticaArmCeiling`, `illinoisRoot` — **re-run as a library**, every design number reproduced), [`C-0034`](C-0034-guided-arm-anchorage.md) (`ArmAnchorage.twoTerminus`, the `A2` anchorage and its counting theorem), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 49–56 inventory, the 45-path 3 × 15 grid), [`C-0023`](C-0023-two-sided-coupling.md) (the **declared** 40 pN/nm ceiling, `E5`, the series composition), [`C-0017`](C-0017-output-coupling-stiffness.md) (placement on the **secant**, stability on the **tangent**, `|k_eff| = 23.41–27.91 pN/nm` at the 10 nm layer in 2 mM), [`C-0032`](C-0032-softening-coupling-stability.md) (the stability margin already at 1.0000–1.0019), [`C-0041`](C-0041-flexure-array-packing.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the 34-path floor, the 10 pN unzip allowable), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md), against the programme's habit of reading §3's **desired** stroke on a coupling **placed for its acceptable one** |

---

## The claim, in one line

**`C-0040` left one escape open — trade path count against hinge count — and the trade turns out not to be a trade at all: the crossover budget enters the arm only through the product `n·h`, which is capped at the tile's own inventory, so every split of it places the same arm; the only thing that does break the tie is a *per-flexure* anchorage couple, and it rewards the direction opposite to the one the escape proposed; and when §3's desired clause is finally placed on its own arithmetic rather than on the acceptable clause's, the arms it produces DO reach 10 nm and are refused by the actuator's own negative stiffness instead.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, moments **pN·nm**; `k_BT = 4.141947 pN·nm` at
  **300 K** in aqueous **2 mM MgCl₂**.
- **Plan view.** `x` runs **along** the tile's helices, `y` **across** them, origin at the tile
  centre; `z` positive **upward**, away from the electrode. §1's bias pulls the tile **down**.
- **A hinge line** is a maximal set of crossovers sharing **one interface** and **one pair of
  bodies**. They are collinear along `x`, turn through the same angle, and their `k_θ` add in
  **parallel** — the only reading under which `h k_θ` is the right torsional spring (`C-0040`).
- **The per-interface crossover pitch is `p = 32 bp = 10.88 nm`**, not 16 bp.
- **The interface-line supply is `(D + 1)·edge`** — 14 interior interfaces of a 15-duplex sheet
  plus its 2 free edges: **640 nm**.
- **Positive `δ`** is the stroke the coupling delivers; the arm's tip rises `δ = z(L)` and its ends
  approach by the draw-in `e = L − x(L)`.

---

## The cheap bounds, which ran first and settled the shape of the answer

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **`n·h ≤ 56` intersected with `CH-0029`/`C-0041`'s path-count floor of 34 at the desired stroke** | **`h = 1`** | the *whole premise* of the task, in one division: at §3's desired stroke the allowable forbids **fewer** paths and the inventory forbids **longer** hinge lines, so there is exactly **one** admissible hinge count and there is nothing to trade. Declared as falsifier 1, which did **not** fire |
| **2a** | **the hinge-supplied arm ceiling**, `θ tan θ = k_target δ²/(N k_θ)`, `r = δ/sin θ` | **5.387 nm** (4.768 at small rotation) | the arm the **whole** crossover inventory can place if the far anchorage carries nothing — **a function of the PRODUCT and of nothing else**, hence identical at all eight splits of `n·h = 56`, and **below §3's desired stroke**, so the hinge budget alone can never lift 10 nm. Declared as falsifier 2, which did **not** fire |
| **2b** | **the combined rigid-arm ceiling**, `θ tan θ = k_target δ²/(N k_θ + n k_far)` | **11.593 nm at 45 paths, 12.657 at 56** | the same with `C-0034`'s `A2` restored. It **is** a function of `n`, it exceeds 10 nm, and it therefore does **not** close the question — which is exactly why the elastica had to be run |
| **3** | the collinear-interface ledger, `n(h−1)p ≤ 640 nm` | — | the second currency; it binds nowhere the crossover ledger does not, because `h ≤ 4` |
| **4** | **the stroke `C-0017`'s stability floor alone permits**, `δ ≤ F/|k_eff|` | **3.58–4.27 nm** | a bound on §3's desired stroke owing **nothing** to the coupling's construction, discovered while pricing the `P10` placement |

> **Bound 2a is the finding, not a screening step.** It is the exact statement that *"fewer,
> longer"* buys nothing: a flexure array cannot convert crossovers between hinges into arm length,
> because the placement condition sees only how many crossovers the array has in total.

---

## Deliverable 1 — the trade curve, and it runs the wrong way

The eight splits of the tile's whole inventory, `n·h = 56`, placed on `C-0039`'s elastica at the
standing placement (secant 33.3333 pN/nm at 3 nm):

| `n` | `h` | hinge line [nm] | **arm [nm]** | bp | tangent(3) | **usable stroke [nm]** |
|---|---|---|---|---|---|---|
| 8 | 4 | 32.64 | 5.587 | 16.4 | 51.58 | 2.417 |
| **14** | **4** | **32.64** | **6.903** | 20.3 | 44.00 | **2.662** |
| 19 | 2 | 10.88 | 7.052 | 20.7 | 43.57 | 2.686 |
| 25 | 2 | 10.88 | 7.864 | 23.1 | 41.29 | 2.858 |
| **28** | **2** | **10.88** | **8.231** | 24.2 | 40.51 | **2.938** |
| 45 | 1 | 0 | 9.131 | 26.9 | 39.18 | **3.119** |
| 50 | 1 | 0 | 9.527 | 28.0 | 38.67 | **3.209** |
| **56** | **1** | **0** | **9.973** | **29.3** | **38.17** | **3.312** |

&nbsp;&nbsp;&nbsp;&nbsp;**The arm is strictly increasing in the path count along the curve, and the
usable stroke with it. Fewer, longer flexures are worse at every step, by 1.45× in arm and 1.36×
in stroke between the ends of the curve.**

**Why, and it is a sentence not a simulation.** The hinge inventory is a property of the **sheet**
and enters the array's restraint as `N k_θ`, which the split cannot change. `C-0034`'s far
anchorage is a property of **each flexure's own duplex end** and enters as `n k_far`, which the
split *can* change — and `k_far = 78.235 pN·nm/rad` is **5.78 crossovers' worth**, so at every
point of the curve the anchorage carries more of the restraint than the hinge does. At `(56, 1)`
it carries `4381/(4381 + 758) = 85 %`. **The element the branch is named after contributes the
minority of its own stiffness.**

> This is the third appearance in this programme of `C-0034`'s own lesson that a restraint
> parameter carries something the designer thought was fixed — and the first where what it carries
> is the **path count**.

---

## Deliverable 2 — the desired stroke, at both readings of the ceiling

`C-0023`'s 40 pN/nm ceiling is **declared at no stroke**, which is `T-107`'s live question. Both
readings are carried, and **the verdict is the same at both**:

| placement | ceiling reading | points | **reaching 10 nm** | best usable stroke | binding at the best point |
|---|---|---|---|---|---|
| **`P3`** secant 33.3333 at 3 nm | **`W`** working point | 31 | **0** | 3.877 nm at `(45, 16)` | inventory, census, line |
| **`P3`** | **`S`** whole stroke | 31 | **0** | 3.877 nm at `(45, 16)` | inventory, census, line |
| **`P10`** secant 10.0 at 10 nm | **`W`** | 29 | **0** | 10.000 nm | **`C-0017`'s stability floor** |
| **`P10`** | **`S`** | 29 | **0** | 10.000 nm | **`C-0017`'s stability floor** |

&nbsp;&nbsp;&nbsp;&nbsp;**`T-107`'s answer cannot move this claim's verdict, and the reason is that
the ceiling is never the binding constraint at the desired stroke.** Under `P3` the binding
constraint at every ledger-admitted point is **geometric reach** — the arm is 4.8–10.0 nm and a tip
cannot rise past its own arm — with the unzip allowable binding as well at the smallest path
counts. Under `P10` the binding constraint is **stability**, and the ceiling is *cleared* at 12 of
the 29 points.

**The best ledger-admitted point of the whole space** — `(56, 1)`, an arm of **9.973 nm = 29.3 bp**
— is **0.027 nm short of even touching** §3's desired stroke with an infinite force, and its usable
stroke inside the ceiling is **3.312 nm, 3.02× short**.

---

## Deliverable 3 — `P10`, and the bound it uncovered

Every standing claim in this programme reads §3's **desired** stroke on a coupling **placed for its
acceptable one**: `k_c = 100 pN/3 nm = 33.3333 pN/nm`, and then asks what happens at 10 nm — where
that coupling would have to deliver **333 pN**, 3.3× what §3 asks for. `C-0017`'s own placement
arithmetic says the desired clause's coupling is `k_c = 100 pN/10 nm = **10 pN/nm**`, which is a
**different device**. Placed that way, the flexure suddenly looks fine:

| `n` | `h` | arm [nm] | bp | tangent(10) | usable stroke | per path at 10 nm | inside the 40 pN/nm ceiling |
|---|---|---|---|---|---|---|---|
| 10 | 4 | 12.732 | 37.4 | 33.4 | 10.000 | 10.00 | yes |
| **14** | **4** | **13.778** | **40.5** | **26.3** | **10.000** | **7.14** | **yes** |
| 28 | 2 | 15.635 | 46.0 | 20.2 | 10.000 | 3.57 | yes |
| 56 | 1 | 18.096 | 53.2 | 16.7 | 10.000 | 1.79 | yes |

&nbsp;&nbsp;&nbsp;&nbsp;**And every one of them is refused, by a constraint that has nothing to do
with the flexure.** `C-0017`'s stability clause is `k_c > |k_eff|`, and `|k_eff| = 23.41–27.91
pN/nm` at the 10 nm layer in 2 mM — so a 10 pN/nm coupling is **2.34–2.79× below the floor**, and
`C-0032` has already shown that the *existing* 33.3333 pN/nm coupling has a margin of only
1.0000–1.0019 there.

Composing the two clauses gives a bound that mentions no element at all:

&nbsp;&nbsp;&nbsp;&nbsp;**`δ ≤ F/|k_eff|` &nbsp;→&nbsp; 3.58–4.27 nm at §3's 100 pN.**

> **§3's desired 10 nm stroke and §3's 100 pN cannot both be delivered by any stable coupling at
> the 10 nm layer in 2 mM, whatever it is made of.** That is [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md),
> and it is filed against the programme's reading convention rather than against any one claim's
> numbers.

---

## Deliverable 4 — what the branch DOES deliver, and how narrow it is

Of the 31 swept points at the standing placement, **3 clear §3's acceptable 3 nm stroke** inside
the compliance ceiling and inside the 10 pN unzip allowable — and **all three are at `h = 1`**:

| `n` | `h` | arm [nm] | bp | tangent(3) | usable [nm] | per path at 3 nm | crossovers spent |
|---|---|---|---|---|---|---|---|
| 45 | 1 | 9.131 | 26.9 | 39.18 | 3.119 | 2.22 | **45 of 56 — 80 %** |
| 50 | 1 | 9.527 | 28.0 | 38.67 | 3.209 | 2.00 | 89 % |
| **56** | **1** | **9.973** | **29.3** | **38.17** | **3.312** | **1.79** | **100 %** |

**The window's width is 1.24× in path count and zero in hinge count**, and what changes across it
is only how completely the sheet is consumed. Below 45 paths the element fails §3's *acceptable*
stroke as well: `(34, 1)` reaches 2.905 nm and `(15, 1)` only 2.469 nm. **The threshold in path
count lies between 34 and 45 on the swept grid** and is not resolved further here.

> **This is a much narrower reading of the branch than `C-0040` left standing.** `C-0040` reported
> a hinge-count window `3 ≤ n ≤ 6` clearing the acceptable stroke at 45 paths; on `C-0039`'s exact
> elastica and with the inventory ledger enforced, the surviving design is the **opposite** corner —
> `h = 1`, the largest affordable path count, and a hinge line of zero length.
>
> **And it costs the sheet.** `C-0040` records that every crossover in the 49–56 inventory *is
> already a structural load path* in `C-0009`'s grillage. The surviving design spends 80–100 % of
> them, so what it buys in coupling it takes out of `D_⊥`. **That is not priced here**, and it is
> the largest open item this claim leaves.

---

## The five verification gates

Executed as **26 gate-named tests** in `src/test/kotlin/anchoring/FlexureCountHingeTradeTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL**, with `T-108`'s mid-TDD `synthesis/DesiredStrokeReachTest.kt`
dropped by `--drop-file`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the line demand is a length, linear in the pitch and in `(h − 1)` and **exactly zero at `h = 1`**; the crossover demand and the line supply are pure counts and lengths; the small-rotation arm ceiling is `√(restraint/stiffness)` — four times the restraint doubles it and four times the target halves it; unphysical arguments throw at **eight** entry points | **PASS** |
| **2 — limiting cases** | the two ledgers are **exact inverses** over `n = 1…56`; the inventory admits exactly **1** crossover per flexure at 45 paths, **4** at fourteen, **0** at 57, and **3** at the 49-crossover phases; the exact-rotation ceiling **strictly exceeds** the small-rotation one and converges to it as `δ → 0`; **the hinge-supplied ceiling is identical at all eight splits of `n·h = 56`**; a flexure whose arm is shorter than the target stroke is reported as geometrically unreachable, not approximated; the `P10` placement lands **between the stroke and 1.5× it**, which is exactly why the search floor had to be widened; the two ceiling readings agree on an element that clears both | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the placed arm and the usable stroke are **independent of the RK4 step count** over 200 → 400 → 800 (`1e−5` / `1e−4`); the usable stroke is **scan-sample independent** over 30 → 240 (`< 1e−5`); the rigid-arm ceiling bisection satisfies **its own defining equation** `θ tan θ = k δ²/Σk` to `1e−10` at four restraints, and independently reproduces the mandate through `rigidArmSecant` to `1e−9`; the result file was re-emitted through `tools/study.sh` and diffed | **PASS** |
| **5 — literature and upstream** | **`C-0039` reproduced** — the `E5a16` arm 12.7198 nm (`2.7e−6`), its tangent at the acceptable stroke 36.44 (`1.2e−7`), its secant 69.94 (`1.2e−7`) and tangent 264.24 (`6.1e−7`) at the desired one, its usable stroke 3.877 nm (`5.5e−5`), its arm cap 13.648 nm (`4.3e−6`) and its 15-path arm 8.40 nm (`5.9e−4`, against a value its own claim quotes to three digits); **`C-0040` reproduced** — the four-crossover census on a 40 nm line and the 163.2 nm demand for sixteen (`1.7e−16`); **`C-0041`/`CH-0029`'s 34-path floor reproduced exactly**; `C-0009`'s hinge constant and `C-0029`'s two-terminus couple to `2.6e−9`; the mandate to `0.0`; and **the widened placement solver reproduces `C-0039`'s own to `1e−9`** at four designs, above the floor where both are defined | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The placed arm is strictly increasing in the path count along `n·h = 56`**, asserted over six
   splits. Nothing in the placement solve knows about the ledger; the monotonicity is the
   *consequence* of the anchorage being per-flexure and it is the whole direction of the answer.
2. **The assembled secant equals the placement target at every point**, to `1e−7`, at four
   `(n, h)` pairs — the condition the arm was solved for, read back through an independently
   constructed beam.
3. **The rigid-arm ceiling strictly bounds the placed arm from above** at four points. Two
   independent objects — a closed-form spring balance and a shooting elastica — and the inequality
   is a theorem (adding a compliance can only shorten the arm) that neither computes.
4. **A placement below the stability floor is refused at a point that clears everything else**:
   `(14, 4)` at `P10` reaches the stroke, holds the ceiling and stays inside the allowable, and is
   still refused. Feasibility is not a property of the element alone, and the test asserts it.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the inventory admitting `h ≥ 3` at `CH-0029`'s path-count floor | **no** | it admits **`h = 1`**, so the trade has one admissible hinge count at the desired stroke and there is nothing to trade |
| 2 | the hinge-supplied ceiling exceeding 10 nm under `P3` | **no** | **5.387 nm**, and it is the same number at every split of the inventory |
| 3 | the sweep failing to reproduce `C-0039`'s `(45, 16)` design | **no** | arm, both tangents, the secant and the usable stroke all at ≤ `5.5e−5` |
| 4 | the `P10` placement clearing `C-0017`'s stability floor | **no, and it is the finding** | 10 pN/nm against 23.41–27.91, i.e. 2.34–2.79× short — `CH-0059` |
| 5 | any point of the swept region reaching the desired stroke | **no** | 0 of 31 and 0 of 29, at both ceiling readings, asserted as a runtime check in the study |

**A result that was not anticipated:** the task was formulated around *"does the trade escape?"* and
the answer is that **the trade is not a trade** — the crossover budget enters the placement only
through its total, so the entire one-parameter family it describes places the *same* arm. The only
thing that moves the arm along that family is `C-0034`'s far anchorage, and it rewards the
direction the escape was proposed *against*.

**A second one:** the two readings of `C-0023`'s ceiling — the concurrent `T-107`'s whole subject —
turn out to be **irrelevant to this branch's verdict**, because at every point of the space the
ceiling is either cleared or dominated by a constraint that binds first.

---

## Does `C-0040`'s verdict survive?

**Its census, its ledgers and its fan law survive untouched and are re-run rather than restated.
Its design window in hinge count does not, and the direction reverses.**

| `C-0040` said | this claim finds |
|---|---|
| a 40 nm hinge line carries **four** crossovers at every phase | **reproduced**, and used as the census ceiling `h ≤ 4` |
| sixteen demands 163.2 nm of collinear interface | **reproduced to `1.7e−16`** |
| *"the design window in hinge count is `3 ≤ n ≤ 6`"* (at 45 paths, on the small-deflection composition) | **it is `h = 1`** once the inventory ledger is enforced and the arm is placed on `C-0039`'s exact elastica. At 45 paths `h = 3` demands 135 crossovers against a 56-crossover sheet |
| *"the path count is bounded below at 34 and above by the inventory; that trade is not swept"* — its open item 2 | **swept, and settled.** The interval is not empty, it is **degenerate**: at 34 paths the inventory admits `h = 1` and nothing else |
| ten crossovers are needed to lift 10 nm by rotation, twelve to do it inside the ceiling | **untouched as arithmetic on its own composition**, and moot: the supply at any affordable path count is one |
| its `L5` reading — a hinge line running outside the tile | **not admitted here either**; `C-0041` refused it on plan area |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** `C-0028`'s and
  `C-0029`'s literature findings are unchanged and upstream of every number: no duplex has been
  built standing normal to a single-layer sheet, and a duplex END has at most **two** covalent
  links.
- **The crossover inventory is used as a BUDGET and the placement of the individual hinge lines is
  not solved.** `n·h ≤ 56` is necessary, not sufficient: a design also has to put `n` disjoint
  hinge lines on 16 interfaces at the right phases, and `C-0040`'s own localisation caveat
  (a rigid anchor is carried by its two nearest crossovers, 2.3–7.6×) says the *realised* count
  runs lower, never higher. **Both omissions run against the design**, so the verdict is safe in
  the direction it is taken.
- **Converting the inventory into hinges removes it from the sheet.** The surviving design spends
  80–100 % of the tile's crossovers, and what that does to `C-0009`'s `D_⊥`, to `C-0015`'s
  flatness grid and to `C-0006`'s load distribution is **not computed here**. It is the largest
  open item.
- **The plan view of the array is not solved.** `C-0041` shows the *standoff* flexure packs at 15;
  `E5a` has no standoff and no tie, so that count does not transfer, and no packing verdict for
  `E5a` exists at all. If it did transfer, §3's acceptable stroke would fail too (2.469 nm at 15
  paths) — which is stated as an exposure, not as a result.
- **`C-0017`'s stability floor is CITED**, at the **10 nm layer height in 2 mM MgCl₂** where it is
  23.41–27.91 pN/nm. At the 5 nm and 7 nm layer heights `C-0017` reports **zero** floor, so the
  `P10` refusal is a statement about the 10 nm design point and not about the actuator in general.
  It is nevertheless the design point `C-0016`, `C-0018` and `C-0032` all read, and `C-0016`'s own
  verdict — the desired stroke unreachable at every height and grafting density — is untouched here.
- **`C-0023`'s 40 pN/nm ceiling is a DECLARED design ceiling**, 1.2× the mandate, not a measured
  limit; **both** readings of it are carried and neither decides the verdict.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT**, not a measurement; Fields et al.'s measured
  buckling implies 25 % less, which `C-0039` shows lengthens the arm 8 % and moves no verdict —
  and an 8 % longer arm at `(56, 1)` is 10.77 nm, which touches the desired stroke geometrically
  and still fails everything else.
- **`k_θ` is `C-0009`'s CITED, FITTED constant** (Chen et al., `α ∈ [0.6, 1.2]`). It enters the
  hinge-supplied ceiling as `√α`, so the whole bracket moves 5.387 nm by ±1.22×, i.e. 4.4–6.6 nm —
  **still below the desired stroke at both ends**. It enters the placed arm more weakly still,
  because the anchorage carries 85 % of the restraint.
- **The elastica is `C-0039`'s, on its small-rotation branch**, free to draw in, with `max_s|φ|`
  carried as a validity flag; the arm is a uniform inextensible Euler-Bernoulli rod.
- **The `(n, h)` grid is 13 path counts × 5 hinge counts**, filtered by the two ledgers to 29
  admitted points, plus two reproduction points. **The path-count threshold for §3's acceptable
  stroke is bracketed at 34 < n ≤ 45 and not resolved further.**
- **One flexure per load path**, exactly as `C-0023`, `C-0025`, `C-0029`, `C-0034` and `C-0039`
  assume.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| crossover spacing per interface | **32 bp** | **CITED**, Rothemund, *Nature* **440**:297 (2006), via `C-0015`/`C-0040` |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al., *Nature* **459**:414 (2009) |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| tile crossover inventory | **56 / 49** | **`C-0015`**, and re-derived through `C-0040`'s census |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement** |
| far anchorage `k_far` | 78.2353 pN·nm/rad | **`C-0034`**'s `A2`, from `C-0029`'s counting theorem at the **cited** 1.0 nm phosphate radius |
| per-path unzip allowable | 10 pN | **CITED, MEASURED** via `C-0006`; length-independent (`CH-0029`) |
| the compliance ceiling | 40 pN/nm | **DECLARED** by `C-0023`, at no stroke |
| **`C-0017`'s stability floor** | **23.41–27.91 pN/nm** | **CITED**, at the 10 nm layer in 2 mM. **This claim's `P10` verdict rests on it entirely** |
| `C-0039`'s design numbers | 12.7198, 36.44, 69.94, 264.24, 3.877, 13.648, 8.40 | **CITED**, and every one reproduced here as a gate-5 test |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the two ledgers and their inverses, the rigid-arm ceilings and their closed
form, the widened placement solver, every placed arm, tangent, secant, usable stroke, per-path
force and verdict, the two placements, the two ceiling readings, the four regions and the binding
constraint at every boundary — is **derived here in code**, with `C-0039`'s and `C-0040`'s
pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **What spending 80–100 % of the tile's crossovers on hinges does to the SHEET.** `C-0009`'s
   `D_⊥`, `C-0015`'s flatness and `C-0006`'s load distribution all rest on those crossovers. This
   is the largest open item and it is new.
2. **A plan view for `E5a`.** `C-0041` solved it for the standoff flexure and got 15; `E5a` is a
   different body and has no packing verdict. If it packed at 15, §3's *acceptable* stroke would
   fail too.
3. **The path-count threshold for §3's acceptable stroke**, bracketed here at `34 < n ≤ 45`.
4. **Whether §3's desired stroke may be read on its own placement at all** — `CH-0059`. It is a
   question about the acceptance clause, and it belongs with `T-107` and `T-108`.
5. **`C-0017`'s stability floor at layer heights other than 10 nm**, where it is reported as zero
   and where `P10` would therefore not be refused — against `C-0016`'s finding that the desired
   stroke is unreachable at every height anyway.

## Challenges

**Raises [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md)**
against the programme-wide convention of evaluating §3's **desired** stroke on a coupling **placed
for its acceptable one**. **No number in any consumed claim fails to reproduce** — 14 reproductions,
worst departure `5.9e−4` against a value `C-0039` quotes to three digits, and `≤ 5.5e−5` otherwise.

**None stands against this claim.** The four ways it would fail:

1. **A hinge motif whose crossovers are not drawn from the tile's own inventory** — a hinge line on
   an interface the sheet does not use. There are 16 lines and 14 of them are interior; the two
   free edges are already counted.
2. **A far anchorage substantially stiffer than `C-0034`'s `A2`.** `A4`'s singly nicked
   continuation is 683 pN·nm/rad and would lengthen every arm; `C-0039` swept it and the usable
   stroke moved to 4.00–4.14 nm, still 2.4× short.
3. **A demonstration that `|k_eff|` at the working point is far below `C-0017`'s 23.41 pN/nm**,
   which would admit the `P10` placement. `C-0032` moves it the other way.
4. **A path count above 56 at `h = 1`** — i.e. more crossovers on the sheet, which means a larger
   tile. That is `T-102`'s specification question, and `C-0041` already prices the desired stroke
   at 1.44× the footprint by a different route.
