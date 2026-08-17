# T-75 — which body carries the standoffs, and T-78 — what sits under the flexure's midspan

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme |
| **Raised by** | [`C-0030`](../claims/C-0030-coupled-standoff-joint.md), open questions 1 and 5 |
| **Covers** | **`T-75`** (primary) and **`T-78`** (its immediate consequence — they are two halves of one question and are taken together) |
| **Verification type** | **logical** (a kinematic identity on the stack, exact, with no free parameter) **+ in-silico** (the three escapes priced on `C-0030`'s own solved flexure) |
| **Status** | formulated → planned → executed |

---

## Why this task exists

`C-0030` establishes that the coupled flexure's force law is **signed but not odd**:
the standoff head's tilt supplies `Φδ`, which is **odd**, against an arc-length demand `e(δ)`, which is **even**.
So one sense of the deflection relieves the beam and the other loads it,
and the two differ by a whole design window —
`ℓ = 5–10 nm` with a 2.18× buckling margin in one, and 42–61 pN/nm with **no** admissible length in the other.

`C-0030` names the deciding variable **"which body carries the standoffs"**,
declares it free to a builder, and files it as *a specification gap, the fourth in this programme*.

**A specification gap is only a gap if the choice is genuinely not decidable from what is specified plus what is buildable.**
This task's job is to find out.

---

## Units, geometry and sign conventions

Locked, and stated with unusual care, because this task is **entirely** about a sign convention.

- Lengths **nm**, forces **pN**, moments **pN·nm**, stiffness **pN/nm**, `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
- **`z` is normal to the electrode, positive away from it, origin at the electrode surface** — `ActuatorGeometry`'s convention, inherited unchanged.
- The tile's bottom face is at `z = h` (the layer height), its top face at `z = h + t` with `t = 10 nm` (§3).
- **The stroke `s` is positive downward**: the tile descends by `s`, the superstructure does not move.
  This is §1's mechanism — *"positive bias on the electrode applies a downward force on the tile"* — and it is the only thing in this task that is not a definition.
- A **mounting** is the ordered pair (**base body**, **standoff normal**):
  which of the two bodies the standoff *bases* stand on, and which way along `z` the standoffs point out of that body's plane.
  The flexure's ends sit on the standoff heads at `z_base + n·ℓ`, `n = ±1`.
- **`δ` is the flexure's midspan deflection relative to its own ends, counted positive TOWARD the standoff base plane.**
  This is `C-0030`'s `FlexureOrientation.FAVOURABLE` direction: sagging toward the plane the bases stand on.
  So the midspan sits at `z_base + n(ℓ − δ)`.
- The midspan is tied to the **other** body — the **driven** body — by a tie of length `m ≥ 0`.
- **Favourable** means `δ > 0` over the stroke; **adverse** means `δ < 0`.
- A standoff's axial duty is **compression** when the beam's end shear pushes its head toward its base, **tension** otherwise.

## The numeric targets

| | |
|---|---|
| **`T-75`** | the mounting sense fixed by a **buildable argument**, or a bracket **and** a threshold and one sharply-posed question |
| **`T-78`** | the favourable mounting's clearance ceiling **settled**, or shown to be a design choice — with its price |

---

## The acceptance predicates, declared before the run

| | predicate | test |
|---|---|---|
| **`Q1`** | the sign is decided by a **kinematic identity**, not by a preference | `dδ/ds` is derived from the stack's own geometry and is exactly `±1` at every mounting |
| **`Q2`** | **"which body carries the standoffs" is either sufficient or it is not**, said in code | the sign is computed for all four (body × normal) combinations and the marginal effect of each variable reported |
| **`Q3`** | every mounting that survives is **buildable against §1/§3**, not merely elastic | each configuration filtered on: §3's effort-point band, whether the beam enters the polymer layer, and what the tie must pass through |
| **`Q4`** | the escapes are **priced**, not dismissed | the pre-bow escape carried to a threshold (the minimum built rise, and the preload it costs) |
| **`Q5`** | **`T-78`** — the clearance ceiling is quantified **as an aperture**, at §3's acceptable *and* desired strokes | penetration depth, aperture length from the solved beam shape, aperture area against the footprint |

**A `PASS` on `Q1`–`Q3` settles `T-75`. A `PASS` on `Q5` settles `T-78`.**
Failure of `Q1` or `Q2` — i.e. finding the sign genuinely under-determined — is a legitimate outcome and produces the bracket-and-threshold form instead.

---

## The cheap bound, which runs first

**One kinematic identity, before any matrix and any root find.**

The midspan sits at `z_base + n(ℓ − δ)` and the driven body's attachment plane is a fixed tie length from it,
so differentiating the chain with respect to the stroke gives

&nbsp;&nbsp;&nbsp;&nbsp;**`dδ/ds = (v_base − v_driven)/n`**, &nbsp; `v_TILE = −1`, &nbsp; `v_SUPERSTRUCTURE = 0`.

Three things fall straight out of it, and each is a test rather than a remark:

1. **It does not contain `m` or `ℓ`.** The tie length and the standoff length cannot move the sign.
2. **It is a product of two binaries.** Swapping the base body flips it; flipping the standoff normal flips it.
   So **"which body carries the standoffs" decides nothing on its own** — `C-0030` named half of the variable.
3. `|dδ/ds| = 1` exactly, so the flexure's deflection **is** the stroke, and the favourable/adverse split is the sign of a `±1`.

**If the cheap bound shows the sign is a free binary after all, the expensive part is not warranted and the task closes as a gap.**
It does not: it shows the sign is fixed once the *topology* is fixed, which turns the question into a buildability question — and that is where the cost goes.

---

## Plan — method, and its justification against cost

### Step 1 — the kinematic identity (minutes)

Implement the mounting as a data type whose sign is **derived** from the stack, not asserted.
Cross-check it against an independent geometric statement — *"the driven body lies on the far side of the base plane from the beam"*, i.e. **the tie crosses the base plane** — computed from actual `z` coordinates.
Two constructions, one answer; the agreement is the test.

### Step 2 — the standoff's axial sense (minutes)

The beam's end shear acts along the standoff's own axis.
Deriving its direction per mounting costs nothing and answers a question `C-0028` and `C-0030` both charged Euler buckling against without asking:
**a standoff in tension does not buckle.**

### Step 3 — the buildability filter (hours)

Four configurations, three filters, each of which is arithmetic on §1/§3 and existing claims:

- **§3's effort point.** `ActuatorGeometry` already records that a 10 nm tile on §3's three layer heights with a **5 nm** attachment height reproduces §3's *"~20–25 nm above the electrode"* band **at both ends** (`C-0012`). §3's band is 5 nm wide and its layer-height range is 5 nm wide, so a *constant* attachment height is forced and it is 5 nm. Each configuration then implies a **minimum** effort height, and it is compared against that 5 nm.
- **The polymer layer.** A configuration that puts the flexure *below* the tile puts it inside the actuation gap. Priced by the array's own **volume** against the layer's, which is a division, and by `ℓ < h`.
- **What the tie passes through.** A tie crossing a body needs an aperture in it. The tile is a 15-duplex single-layer sheet (`C-0009`, `C-0015`); the superstructure is unspecified (`C-0017`).

### Step 4 — the escapes (hours)

Two, and they must be closed or the answer is not settled:

- **A pre-bow (`T-42`).** Building the flexure already sagging toward the base plane by `δ₀` puts the first `δ₀` of stroke on the favourable limb *in an adverse mounting*. Priced on `C-0030`'s own solved flexure: the span re-placed on the **incremental** secant, the tangent maximised over the stroke, and the **preload** the built rise costs read off at zero stroke. Base-pair quantised at 0.34 nm (`C-0023`).
- **Reversing the stroke.** §1 fixes the direction; recorded as an assumption with its falsifier rather than tested.

### Step 5 — `T-78`, the aperture (hours)

The clearance ceiling `ℓ − d` is `C-0030`'s, and it assumes an **imperforate** body under the midspan.
But the favourable topology *requires* the tie to cross that body — so the body cannot be imperforate at exactly the place the midspan descends toward.
The right question is therefore not "is there a ceiling" but **"how big is the hole"**, and that is answered by the solved beam shape:

&nbsp;&nbsp;&nbsp;&nbsp;`w(u)/δ = (24u + 12ρu² − 16(2+ρ)u³)/(8+ρ)`, &nbsp; `u = x/L ∈ [0, ½]`, symmetric,

which is `C-0025`'s own beam integrated once more — it returns `c(ρ) = 192(ρ+2)/(ρ+8)` at `u = ½` by construction, and reduces to `3u − 4u³` (pinned) and `12u² − 16u³` (clamped) at its limits, so it is verifiable at three independent points.

### Why not something more expensive

A finite-element or elastica treatment of the array would add nothing: **the sign is a `±1` from a kinematic chain**, and no refinement of the beam changes a `±1`.
The parts that *could* move — the preload the pre-bow costs, the aperture the beam demands — are evaluated on `C-0030`'s already-verified flexure rather than on a new model, so the whole task is a re-reading plus a shape function.
Spending a solve here would be `P-3`'s lesson in a new place: the serious method would be *less* trustworthy than the arithmetic, not more.

### What would falsify this approach

| # | falsifier | what it would mean |
|---|---|---|
| 1 | `dδ/ds` depending on the tie length, the standoff length or the span | the sign is not kinematic and the whole framing is wrong |
| 2 | all four configurations giving the same sign | the variable does not exist and `C-0030`'s window difference is an artefact |
| 3 | the two favourable configurations both surviving the buildability filter | the choice is genuinely free and the answer **is** a specification gap — the bracket-and-threshold form is delivered instead |
| 4 | the pre-bow escape being **free** (a preload inside `C-0021`'s 1.38 pN requirement) | the mounting question dissolves: an adverse mounting could simply be pre-bowed into the favourable limb |
| 5 | the aperture demanded at §3's **acceptable** 3 nm stroke being non-zero | `T-78`'s ceiling binds at the clause the programme actually delivers, not only at the desired one |
| 6 | §3's effort-point band admitting the inboard topology at `ℓ ≥ 5 nm` | the sharpest specification filter disappears and only the layer and the perforation filters remain |

---

## What is inherited, and from where

| from | what | re-derived here? |
|---|---|---|
| `C-0030` | `CoupledJointFlexure`, `coupledFlexureSpan`, `FlexureOrientation`, `favourableStrokeClearance`, the `B2` design at `ℓ = 8 nm` | **consumed as a library and re-run**; its favourable/adverse table reproduced as a gate-5 test |
| `C-0028` | `StandoffBase.crossovers(2, favourable)`, `k_θb = 261.2 pN·nm/rad` | consumed |
| `C-0025` | `c(ρ) = 192(ρ+2)/(ρ+8)`, the partially restrained beam | **the shape function is derived here** and asserted to return `c(ρ)` at midspan |
| `C-0017` | the 33.333 pN/nm mandate, the 10 nm envelope, the output superstructure | consumed |
| `C-0023` | the 40 pN/nm compliance ceiling, the 45 paths, the mounting offset as a **length** quantised at 0.34 nm | consumed |
| `C-0012` / `ActuatorGeometry` | the 5 nm lever attachment height that reproduces §3's 20–25 nm band at both ends | **re-derived** as a gate-5 test |
| `C-0009` / `Gen1Tile` | `EI`, `S`, the 2.69 nm interhelical distance, the 2.0 nm duplex diameter, the 0.34 nm rise | consumed |
| §1 | the tile descends under bias — **the one physical input** | stated as an assumption with its falsifier |

---

## Execute — the run

| | |
|---|---|
| **entry point** | `anchoring.FlexureMountingSenseStudyKt`, via `tools/study.sh anchoring.FlexureMountingSenseStudyKt` (~3 s) |
| **emits** | `gpd/results/T-75-flexure-mounting-sense.json` — **byte-identical on two independent runs** |
| **model** | `src/main/kotlin/anchoring/FlexureMountingSense.kt` |
| **tests** | `src/test/kotlin/anchoring/FlexureMountingSenseTest.kt` — **30 gate-named tests, 0 failures** |
| **records** | 4 mountings, 44 designs, 33 apertures, 3 effort-band rows, 9 occupancy rows, 12 pre-bow rows, 7 convergence rows, 10 upstream reproductions |

### The predicates, as they came out

| | predicate | outcome |
|---|---|---|
| **`Q1`** | the sign is a kinematic identity | **PASS** — `\|dδ/ds\| = 1` at all four mountings, and identical over 48 combinations of standoff and tie length |
| **`Q2`** | neither variable alone decides the sign | **PASS** — 1 of 2 favourable mountings has each base body, and 1 of 2 has each standoff normal |
| **`Q3`** | exactly one mounting survives the buildability filter | **PASS** — `Su`: bases on the superstructure, standoffs pointing away from the tile, flexure outboard |
| **`Q4`** | the pre-bow escape is priced to a threshold | **PASS** — 0 of 12 admissible; 4.08–16.66 nm of rise for a 150–225 pN preload, closed outright at the desired stroke |
| **`Q5`** | the clearance ceiling is quantified as an aperture at both strokes | **PASS** — zero at 3 nm for `ℓ ≥ 6 nm`; 18.37 nm × 2.69 nm × 45 = 2223 nm² = 1.39× the footprint at 10 nm |

### The declared falsifiers, and what happened

| # | falsifier | fired? |
|---|---|---|
| 1 | `dδ/ds` depending on a length | **no** |
| 2 | all four mountings giving the same sign | **no** — 2–2, split by the product |
| 3 | both favourable mountings surviving, i.e. a genuine specification gap | **no** — `Td` fails on the layer at 9 of 9 states |
| 4 | the pre-bow escape being free | **no** — 109–163× `C-0021`'s requirement |
| 5 | the aperture at §3's **acceptable** stroke being non-zero | **no** — the clause the programme delivers is free |
| 6 | §3's effort band admitting the inboard topology at `ℓ ≥ 5 nm` | **no at the design height** — both readings give 5 nm at the 10 nm layer |

**Falsifier 3 is the one that decided the shape of the answer.** Had it fired, this task would have delivered the bracket-and-threshold form and handed one question back. It did not, so the question that goes back is the *narrower* one — whether the surviving mounting's own body may be perforated (`T-95`, open question 6) — and `T-75` closes as a determination.

---

## Verify — the five gates

Full table in [`C-0035`](../claims/C-0035-flexure-mounting-sense.md). In brief:

| gate | outcome |
|---|---|
| **1 — dimensional** | **PASS** — the rate is dimensionless and length-free; the shape is dimensionless and 1 at midspan; the aperture doubles exactly with the span; the array volume goes as `r²` |
| **2 — limiting cases** | **PASS** — the shape reduces to both textbook forms; its end slope reproduces `24/(8+ρ)` from the beam's own solution; a zero pre-bow reproduces `C-0030`'s signed stroke laws exactly |
| **3 — symmetry and conservation** | **PASS** — the tie-crossing test agrees with the differentiated chain at every realisable stack; the sign is a product; compression ⟺ favourable; `min(stroke, clearance) + penetration = stroke` identically |
| **4 — numerical convergence** | **PASS** — the aperture solve is exactly scan-independent; the level solve inverts the shape to `1e−9`; the pre-bowed span returns its own target secant to `1e−8`; the result file is byte-identical on two runs |
| **5 — literature and upstream** | **PASS** — `C-0030`'s favourable and adverse designs to `≤ 1.1e−4`, its clearance table to `1.7e−16`; `C-0025`'s `c(ρ)` recovered from the new shape function to `1.5e−16`; §3's effort band at both ends to `0.0` |

## File

Claim [`C-0035`](../claims/C-0035-flexure-mounting-sense.md), raising [`CH-0045`](../challenges/CH-0045-the-mounting-sense-is-not-a-free-binary.md) and [`CH-0046`](../challenges/CH-0046-a-standoff-in-tension-does-not-buckle.md), both against `C-0030`, whose every number reproduces.
Queued: `T-95` (may the superstructure be perforated — a **specification** question, carried to the open questions as item 6) and `T-96` (`T-31`'s array packing with a plan-view constraint attached).
