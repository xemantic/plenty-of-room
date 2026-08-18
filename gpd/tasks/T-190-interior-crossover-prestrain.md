# T-190 — what do the 42 INTERIOR crossovers carry, and does their cancellation hold?

| | |
|---|---|
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** |
| **Raised by** | [`C-0107`](../claims/C-0107-row-end-prestrain-value.md) *Still open* item 1 |
| **Reserved** | claim `C-0112`, challenges `CH-0129`, `CH-0130` |
| **Verification type** | **logical** (a prestrain is a load, so the response superposes exactly and the decomposition is an identity rather than an approximation) **+ in-silico** (one bank of solves on the same explicit-elastic-support grillage `C-0107` used) |

---

## Formulate

### The question

`C-0104` put a prestrain on the **14** row-end crossovers and found `T-5b`'s 0.10 crossed at
15.4497275°. `C-0107` derived a *value* for that prestrain — 17.15–24.98°, nominal 22.6184533° —
and then observed that the boundary layer it derived is a **field** `u(x)`, not a value at one
station, so every one of the sheet's **56** crossovers is built at `(−1)^b u(x)` and not only the
14 at the row ends. Its Deliverable 4 reads the two:

| reading | dishing / free stroke | flat at 0.10? |
|---|---|---|
| the 14 row-end sites alone, at `+22.6184533°` | **0.1022820** | **no** |
| the graded corrugated field `(−1)^b u(x)` over all 56 | **0.0922622** | **yes** |

and attributes the difference to the 42 interior sites, which nobody has modelled. **A verdict this
programme publishes is therefore decided by a cancellation between two site sets, one of which was
never posed as a question.**

### What is being asked, precisely

1. **Is the field separable?** Read the graded field over the 42 interior sites **separately** from
   the 14 row-end ones, and decompose the difference — or state that it is not separable, with the
   reason.
2. **Does the cancellation hold?** Over the overall sign, over the boundary layer's own
   12-cell parameter bracket, and over the lattice's own column structure.

### Locked units, geometry and sign conventions

Restated rather than inherited, per the standing invariant.

- Angles **rad** (degrees where quoted), rotational stiffness **pN·nm/rad**, couples **pN·nm**,
  lengths **nm**, forces **pN**; `k_BT = 4.141947 pN·nm` at 300 K, aqueous **2 mM MgCl₂**.
- `x` along the helices measured from the **tile centre**, `y` across them, `z` normal and positive
  **upward**; `w` positive **downward**.
- A **prestrain** `θ₀` at a crossover is `C-0104`'s: the relative roll the crossover is *built* at,
  so its hinge stores `½ k_θ (Δφ − θ₀)²`.
- `u(x)` is `C-0107`'s azimuthal register error, **odd about the row centre**,
  `u(x) = Δω λ sinh(x/λ)/cosh(L/2λ)`, `λ = √(C p/k_θ)`.
- The **graded corrugated field** is `θ₀(b, x) = s · (−1)^b u(x)`, `b` the interface index, `s = ±1`
  an **overall sign** which no source in this repository fixes.
- **Dishing** is `C-0063`'s, unchanged: the peak of the deflection with its best-fit plane removed,
  over the free plate's mean descent under the same uniform load. **Flat** means `≤ 0.10`.
- The lattice is the one `C-0090`, `C-0099`, `C-0104` and `C-0107` all carry: 15 duplexes at the
  SAXS 2.69 nm, **38.08 nm** along the helices, crossover **phase 8** (8 columns), `C-0090`'s
  published 34-root key, `C-0017`'s 33.3333 pN/nm mandate as 34 explicit elastic supports,
  `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V, `C-0001`'s foundation secant, free stroke
  5.15473846 nm.

### Acceptance predicates

- **`P1`** — the decomposition is delivered as an **identity**: the solved field under the graded
  prestrain equals the load-only field plus the row-end-only prestrain response plus the
  interior-only prestrain response, to a relative `1e−10` in the coefficient vector. (If it does
  not, the whole method is wrong and `C-0104`'s linearity claim with it.)
- **`P2`** — the interior contribution is **quantified** against the row-end one on a
  convention-free measure — the cosine of the two dishing fields under the lattice's own area inner
  product — and the peak-dishing readings of both site sets are reported separately.
- **`P3`** — the cancellation is tested over the axes that could break it: the **overall sign**
  (which `C-0107` did not sweep), the **decay length** over `C-0107`'s own 12-cell bracket, and the
  lattice's **column** structure (a cumulative ladder from the row ends inward).
- **`P4`** — every number `C-0107` and `C-0104` published that this task re-reads is reproduced
  from **their result files**, not transcribed, and any disagreement is raised as a challenge and
  never overwritten.

### Falsifiers, declared before the run

| # | falsifier | what it would mean |
|---|---|---|
| **F1** | the three solved fields do not superpose to `1e−10` | a prestrain is not a pure load in this lattice; `C-0104` Deliverable 1 falls |
| **F2** | the graded field's restriction to the 14 row-end sites is `C-0107`'s **nominal** `+22.6184533°` map | `C-0107`'s comparison is between a consistent pair and this task is a confirmation |
| **F3** | the interior contribution is a small correction — `‖d_I‖ < 0.2 ‖d_R‖` | the row-end-only idealisation is good and `C-0104`'s three distributions are adequate |
| **F4** | the graded field at the **other** overall sign is also flat | the flat verdict is stronger than published and does not rest on an unstated sign |
| **F5** | every cell of `C-0107`'s 12-cell boundary-layer bracket keeps the graded field flat | the cancellation is robust to the one parameter that is bracketed rather than known |
| **F6** | *(standing)* a uniform load on a uniform foundation dishes more than `1e−6` of the free stroke, read on the **support-free, prestrain-free** lattice | the solver is broken |

**`CLAUDE.md`'s best falsifier is deliberately NOT declared here.** *"A uniform load on a uniform
Winkler foundation produces no dishing at all"* is a statement about a **load**; a uniform prestrain
is an **eigenstrain**, and the state that relaxes every hinge at once is a **cylinder** of curvature
`θ₀/d`. `C-0104` Deliverable 5 measured it (0.299 → 0.638 of the stroke under a uniform 17.143° on
all 56) and asserting the zero would report a correct solver as broken. `F6` is the load-only form,
read on `withoutPrestrain` and on the support-free lattice, which is where it is true.

---

## Plan

### Method, and why it is the cheap one

**A prestrain is a load, not a stiffness** (`C-0104` Deliverable 1). `½k_θ(Δφ − θ₀)²` leaves the
quadratic term untouched, so no entry of the stiffness matrix moves and the solved field is
**exactly linear** in the prestrain map. Three consequences, all free:

1. `θ_graded = θ_rowEnd + θ_interior` as maps ⟹ `w_graded = w_load + w_rowEnd + w_interior` as
   fields. **The decomposition is an identity, not a model.**
2. Peak dishing is a **peak of a plane-removed field**, i.e. a seminorm, so it is **not** additive.
   The cancellation is therefore a **cross term**, and the right instrument for it is the inner
   product `⟨d_R, d_I⟩` under the lattice's own area measure — which `OrigamiGrillage` already
   exposes — and not a difference of two peaks.
3. The whole sign and bracket sweep costs one solve per state on an already-factorised host.

**The trap, paid for upstream and re-paid here.** A term that lives on the **structure** contaminates
every **influence** function computed from it (`CH-0120`). This study builds **no** influence bank:
it uses `C-0107`'s explicit-elastic-support host, so there is nothing to contaminate. The standing
uniform-load falsifier is read on `withoutPrestrain` **and** on the support-free lattice, which is
the fourth and fifth places this trap has bitten.

### Cheap bound first, before any solve

The graded field's values at every site are **arithmetic**: `(−1)^b u(x_c)` on 8 known column
positions. Two numbers come out of that with no solve at all — which of `C-0107`'s two row-end-only
rows the graded field's own row-end restriction actually is, and the ratio of the first interior
column's amplitude to the row end's. If those two say the cancellation is structural, the solves
price it; if they say the interior is negligible, the task closes on arithmetic.

### What result would falsify this approach

If `F1` fires — the three fields do not superpose — then the prestrain is not entering as a pure
load, every number `C-0104` and `C-0107` published on this axis is suspect, and the correct response
is a challenge against `C-0104` Deliverable 1 rather than a claim here.

### Cost

One bank of solves on a lattice that already factorises in under a second, plus one closed-form
field evaluation per site. No new solver, no literature spend (`C-0107` has already established
that the coordinate is unmeasured and that the one published study of it excludes exactly these
sites), no oxDNA. The expensive alternative — re-running `C-0104`'s 163 296-placement enumeration
under the graded field — is **not** taken here: it answers a different question (`T-185`).
