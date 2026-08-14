# T-107 — Is `C-0023`'s 40 pN/nm compliance ceiling required at the DESIRED stroke, or only at the working point?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Raised by** | [`C-0039`](../claims/C-0039-two-spring-elastica.md), open item 5: *"A stroke-dependent compliance ceiling. `C-0023`'s 40 pN/nm is declared at no stroke; whether the actuator needs it at the desired stroke or only at the working point is a question for `C-0017`/`C-0018`, and it decides how badly this element misses."* |
| **Verification type** | **logical** (an audit of what `C-0017` and `C-0018` actually require of a coupling law, each requirement read with the stroke it is written at) **+ in-silico** (every requirement and every element's response re-derived from the owning claim's own library) |
| **Units** | nm, pN, pN/nm, pN·nm; `k_BT = 4.141947 pN·nm` at 300 K; aqueous 2 mM MgCl₂ unless stated |

---

## Why this task exists

`C-0023` declared a design ceiling — *"tangent at that point **≤ 40 pN/nm**"* ([`T-23`](T-23-two-sided-coupling.md), `P2`) — and three iterations have consumed it without the qualifier this project demands of everything else.
`CLAUDE.md` records seven instances of the same discipline: stiffness-with-a-compression, variance-with-a-bandwidth, rupture-force-with-a-loading-rate, `k_es`-with-a-gap, flatness-count-with-a-load-case, ceiling-with-a-load-line, `c`-with-a-stroke.
**This is the eighth, and it is the first applied to an acceptance clause rather than to a model.**

The two claims that consume it read it at two different strokes and reach two different verdicts:

- [`C-0039`](../claims/C-0039-two-spring-elastica.md) reads it at §3's **desired** 10 nm stroke and rejects `E5a16` by **6.6×** (264.2 against 40);
- [`C-0040`](../claims/C-0040-hinge-line-census.md) reads it at §3's **acceptable** 3 nm stroke and rejects the buildable hinge counts by **1.05–1.35×** (42.0–54.1 against 40).

Nothing has said which is owed.
It is the cheapest thing that could move iteration 6's verdict, and it is a question about an acceptance predicate rather than about physics.

---

## Geometry and sign conventions, restated

- The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode; `L₀` is a **force-onset** height (`C-0011`, `CH-0010`).
- A coupling **reaction** `R(s)` is positive **upward**.
  Its **secant** `R(s)/s` is what §3's *placement* clause is written on and its **tangent** `dR/ds` is what the *stability* clause is written on (`C-0017`); they are the same number only for an affine line through the origin.
- The device **traverses** `[0, s*]`, from the zero-bias rest to the operating point at `s*`.
  It does not occupy strokes beyond `s*` unless a design says it does.
- **`n` is the load-path count** and a per-path force is `R(s)/n`.

---

## The acceptance predicates, declared before the run

| | predicate | falsifiable by |
|---|---|---|
| **`P1`** | the 40 pN/nm is exactly `1.2 × mandatedCouplingStiffness(100 pN, 3 nm)`, i.e. a construction on the **placement** mandate that carries the placement stroke inside it | any other derivation of 40 in `C-0023`/`T-23` |
| **`P2`** | **neither `C-0017` nor `C-0018` contains an upper bound on a coupling tangent.** Placement is an EQUALITY on the secant, stability a FLOOR on the tangent, and `C-0032` measures a stiffer tangent *raising* `C-0018`'s pull-in margin | a clause in either claim that penalises stiffness at fixed placement |
| **`P3`** | the requirement that *does* bound a coupling from above beyond the working point is the **per-path unzip allowable**, and it is a bound on a **force**, so as a stiffness it goes as `n·allowable/s` — **tightening** with the stroke where the declared ceiling is constant | a per-path allowable that is not a force |
| **`P4`** | relaxing the declared ceiling to a working-point-only requirement does **not** move `C-0039`'s verdict on `E5a16` at the desired stroke | the element clearing every requirement at 10 nm once the ceiling is dropped |
| **`P5`** | the same answer applies to the stability **FLOOR** — `CH-0047`'s open convention — and it is worth a countable number of `C-0017`'s six 2 mM model floors on `C-0030`'s element | the traversed-range minimum and the `[0, 10]` minimum clearing the same count |

---

## Plan, and the cost justification

**The cheap bound runs first and it is arithmetic**: `40 / (100/3) = 1.2` exactly, and the *same* construction at §3's desired clause is `1.2 × (100/10) = 12 pN/nm`.
If the ceiling is a construction on the mandate, then reading 40 at a 10 nm stroke is not conservative — it is the **wrong clause's number**, and it is 3.33× too generous rather than too strict.
That costs nothing and it decides the shape of the whole answer; everything after it is an audit and a re-run.

The expensive half is only expensive because it re-runs other claims' pipelines rather than quoting them: `C-0023`'s flexure and hinge placements, `C-0030`'s coupled flexure, `C-0039`'s elastica, `C-0040`'s census, `C-0041`'s packer.
That is deliberate — the task's whole content is *which stroke a number is read at*, and a tabulated number does not carry its stroke.

**What would falsify this approach:**

1. finding an upper bound on the tangent inside `C-0017` or `C-0018` that is not the per-path allowable — then the ceiling has a physical warrant and a stroke of its own (**did not fire**);
2. the per-path secant ceiling at the desired stroke coming out *above* 40 pN/nm at every buildable path count, which would make the declared ceiling the binding one after all (**did not fire**: 45 pN/nm at 45 paths and **15** at `C-0041`'s buildable 15);
3. `C-0039`'s `E5a16` clearing every remaining requirement at 10 nm once the ceiling is dropped (**did not fire**: its secant there is 69.94 pN/nm and its per-path force 15.54 pN);
4. the traversed-range reading of the stability floor changing no verdict, which would make `CH-0047` a separate question rather than the same one (**fired the other way**: it moves `C-0030`'s element from 0 of 6 to **4 of 6**).

---

## Verify

All five gates as executable tests in `src/test/kotlin/synthesis/DesiredStrokeReachTest.kt`, shared with [`T-108`](T-108-desired-stroke-reach.md); **27 gate-named tests**, `tools/verify.sh` **BUILD SUCCESSFUL**.
The result file is `gpd/results/T-108-desired-stroke-reach.json`, re-run through `tools/study.sh` and diffed **byte-for-byte identical**.

The answer is filed as [`C-0049`](../claims/C-0049-compliance-ceiling-stroke.md).
