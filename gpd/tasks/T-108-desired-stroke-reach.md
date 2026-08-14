# T-108 — Is §3's DESIRED ~10 nm stroke reachable by any coupling this programme has?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A2.2`** for the stroke the actuator itself can deliver |
| **Raised by** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md)'s own sentence — *"the honest answer to `A8.2` is that 10 nm is out of reach and 3 nm is not"* — and by [`C-0039`](../claims/C-0039-two-spring-elastica.md)'s open item 2 |
| **Verification type** | **logical** (a kinematic identity: the stroke is `L₀ − h`, so it is bounded by the layer's own resting height, which §3 fixes at ≤ 10 nm) **+ in-silico** (`C-0003`'s six layer models re-run for the three stroke ceilings, and every element of the coupling catalogue re-run from its owning claim's library against eight predicates at both of §3's strokes) |
| **Units** | nm, pN, pN/nm, pN·nm, nm²; `k_BT = 4.141947 pN·nm` at 300 K; aqueous 2 mM MgCl₂, and 0.5 mM where a stability floor is read |

---

## Why this task exists

Three independent routes now say the desired stroke fails, and **none of them was looking for it**:

- [`CH-0040`](../challenges/CH-0040-e5-is-a-small-rotation-law-at-47-degrees.md)'s cube-root arm cap;
- [`C-0040`](../claims/C-0040-hinge-line-census.md)'s hinge-line inventory — the 16-crossover line does not exist, and the tile carries four;
- [`C-0039`](../claims/C-0039-two-spring-elastica.md)'s geometry — a stroke that is ≥ 73 % of the arm's own contour stiffens whatever the arm is made of; 0 of 34 placements and 0 of 25 sensitivity points reach 10 nm.

[`C-0041`](../claims/C-0041-flexure-array-packing.md) adds a fourth on the plan view, and [`C-0046`](../claims/C-0046-fewer-longer-flexures.md) a fifth on the `(path count, hinge count)` trade.
**§7 of the problem definition says a question that cannot be answered with the available methods must be stated plainly rather than answered anyway, and NDI wants emptiness reported early.** Nothing has yet said it as a claim.

---

## Geometry and sign conventions, restated

- The **stroke** `s = L₀ − h` is positive **downward**; `L₀` is a **force-onset** height (`C-0011`, `CH-0010`).
- **`h > Nσv₀`** always: the layer's dry thickness is the height at which its volume fraction reaches one, and `C-0003` enforces it in code.
- A coupling **reaction** `R(s)` is positive **upward**; the operating point is the **root** of `W(s) = R(s)`, never a force over a stiffness.
- §3 names **three layer heights, 5 / 7 / 10 nm**, and its target stroke row reads *"≥3 nm acceptable, ~10 nm desired"*.
  §6 task 3's acceptance predicate reads *"stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration that it is unreachable"* — **the desired figure is a §3 target row, not a §6 predicate**, and the distinction is carried throughout.

---

## The acceptance predicates, declared before the run

| | predicate | falsifiable by |
|---|---|---|
| **`P1`** | **`s < L₀ ≤ 10 nm` identically**, so §3's desired stroke asks the 10 nm layer for `h = 0` | a §3 layer height above 10 nm |
| **`P2`** | the three stroke ceilings — kinematic (`L₀ − Nσv₀`), validity (`L₀ − Nσv₀/0.2`, `C-0002`'s crossover, which is `C-0018`'s own binding bias ceiling at 10 nm) and dead-load (the stroke at which the layer alone carries §3's 100 pN) — are each strictly below 10 nm at **every** point of the sweep | any state reaching 10 nm on any of the three |
| **`P3`** | **the free stroke bounds every coupled stroke**, because the delivered stroke is monotone decreasing in the coupling stiffness (`C-0017`'s own gate-2 theorem, re-derived here) — which is what lets one bound cover the whole catalogue | a coupling that increases the delivered stroke |
| **`P4`** | a synthesis table over **every** element in the catalogue, each row naming its binding constraint, at both of §3's strokes: `E1`, `E3a`, `E3b`, `E4`, `E5` (`C-0023`), `E5a` at every hinge count the lattice supplies (`C-0039`/`C-0040`), `C-0030`'s coupled flexure in both mountings and at both path counts, `C-0037`'s truss, and the hypothetical ideal coupling placed at §3's own desired clause | an element in the corpus with no row |
| **`P5`** | **"unreachable in physics" is NOT established; "unreachable on §3's own stack" is** — and the escape is priced as a layer height | a route to 10 nm inside §3's stated parameters |

---

## Plan, and the cost justification

**The cheap bound is an identity and it decides the answer**: `s = L₀ − h < L₀ ≤ 10 nm`.
It costs nothing, no coupling appears in it, and it cannot be moved by any model — so the expensive half is not run to *decide* the question but to say by **how much** and **where** the failure lives, which is what a design programme needs.

The next cheapest bound is the dry-thickness floor, then `C-0002`'s `φ = 0.2` crossover — both algebraic in quantities `C-0003` already computes.
Only the dead-load stroke needs a root solve, and it is one bisection per `(height, model, σ)` on the layer alone, with **no field solve at all** — because §3's force target is a load the layer has to carry whatever supplies it.
That is the whole reason this task is seconds rather than the ~7 minutes `T-4` costs.

The catalogue half re-runs each element from its **owning claim's library** rather than tabulating it, because a tabulated stiffness does not carry the stroke it was read at — which is [`T-107`](T-107-compliance-ceiling-stroke.md)'s finding, applied to this task's own inputs.

**What would falsify this approach:**

1. a §3 layer height above 10 nm, which would make `P1` vacuous (**did not fire** — §3 names 5, 7 and 10);
2. the dead-load stroke exceeding the kinematic ceiling anywhere, which would mean the bisection had left the layer's validity range (**did not fire**);
3. any element reaching 10 nm while clearing every other predicate, which would relocate the failure back into the coupling (**did not fire** — 0 of 14 rows);
4. the delivered stroke rising with the coupling stiffness on any characteristic, which would break `P3` and make the free-stroke bound useless (**did not fire**);
5. a specification-neutral escape — a way to 10 nm inside §3's own parameters — which would make `P5` false (**did not fire**; the escapes found are all specification changes and are named as such).

---

## Verify

All five gates as executable tests in `src/test/kotlin/synthesis/DesiredStrokeReachTest.kt`, shared with [`T-107`](T-107-compliance-ceiling-stroke.md); **27 gate-named tests**, `tools/verify.sh` **BUILD SUCCESSFUL** with one concurrent agent's mid-TDD main source dropped by `--drop-file` (`src/main/kotlin/coupling/SingleColumnFlatnessStudy.kt`, `T-101`).
`gpd/results/T-108-desired-stroke-reach.json` is re-run through `tools/study.sh` and reported *"no result file changed"*.

The answer is filed as [`C-0050`](../claims/C-0050-desired-stroke-reach.md), with [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md) against `C-0040`.
