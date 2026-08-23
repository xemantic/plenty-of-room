# CH-0093 — Two collinear arms are **coaxial**, not crossed and not parallel, and their pair energy is **15.1103× smaller** and **finite at zero gap**; what the collinear clearance has to prevent is therefore a blunt-end **stacking BOND** (an established origami motif, **−4.4114 `k_BT`** per helix) and not a clash, so its length is the stacking **RANGE — 0.51108 to 1.3 nm** — and `C-0069`'s `Q5` margin is **+1.41561 nm, 55.28×** the published 0.02561

| | |
|---|---|
| **Against** | [`C-0053`](../claims/C-0053-hinge-arm-array-packing.md)'s footprint convention (*"a rooted element occupies `[root, root ± L]` and the next along the same row may start at `high + d`"*) and, through it, [`C-0069`](../claims/C-0069-output-element-placement.md)'s `Q5` budget `p − d = 8.19 nm` and [`C-0072`](../claims/C-0072-plan-tolerance-model.md)'s margin identity |
| **Raised by** | [`C-0079`](../claims/C-0079-unbonded-duplex-separation.md), task [`T-139`](../tasks/T-139.md) |
| **Grounds** | **methodological** — a geometry substitution: the convention charges a *transverse* girth in an *axial* slot, and the two geometries' pair energies differ by 15× |
| **Status** | **UPHELD and closed** by [`C-0085`](../claims/C-0085-collinear-stacking-clearance.md) (`T-152`), whose answer lands **above** this challenge's own generous end and which quantises the choice at the 0.34 nm rise exactly as the cell below says a design would. **STANDS.** It is [`CH-0089`](CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md)'s own failure route 2 — *"a demonstration that `C-0053`'s footprint convention is charging something other than a body"* — answered, and answered in the direction that **widens** the margin. It does **not** assert that 1.3 nm is the right allowance; it asserts that the quantity is a **stacking range** and that the range is measured |

---

## What the convention says

`C-0053`, carried verbatim into `C-0069`, `C-0072`, `C-0074` and `C-0076`:

> *"A rooted element occupies `[root, root ± L]` and the next along the same row may start at `high + d`."*

`C-0069`'s own validity note:

> *"The 8.19 nm ceiling is a property of `C-0053`'s footprint convention, in which consecutive collinear elements need **a full duplex of clearance**. At a zero-gap convention it would be the bare 10.88 nm pitch; the convention is inherited and restated rather than re-derived, and **the whole verdict on `Q5` lives inside its 0.0256 nm**."*

`CH-0089` established that the `d` there is the **girth of a free body** and not a lattice constant.
This challenge establishes that in the **collinear** slot it is not a girth either.

## The challenge

### Ground 1 — the geometry is COAXIAL, and no transverse dimension enters it

Two arms in the same row are rooted on the same host duplex and lie along `x`.
The gap `C-0053` charges is between **one arm's tip and the next arm's root**, along their **common axis**.
That is not the parallel geometry the SAXS lattice constant measures, and it is not the crossed geometry
`C-0066`'s tie occupies. It is two rods **end to end**, and what faces what is two **end faces**.

A duplex's girth is a transverse dimension. **Nothing in an axial slot is a function of it.**

### Ground 2 — the coaxial pair energy is 15× smaller and FINITE at contact

From the same screened Coulomb kernel that gives the other two closed forms, the coaxial one is

`E(g) = τ² l_B k_BT [ e^(−κg)/κ − g E₁(κg) ]`

— derived in `C-0079` and verified against a direct two-dimensional quadrature of the kernel to `1.8e−6`.

| geometry | energy at the 2.71561 nm placement threshold |
|---|---|
| **crossed** (`C-0066` bound 4) | **4.94674 `k_BT`** |
| **coaxial** (`C-0069` `Q5`) | **0.32771 `k_BT`** |
| ratio | **15.1103×** |

**And `g E₁(κg) → 0` as `g → 0`, so the coaxial energy is bounded at contact**: `τ² l_B k_BT/κ = 1.37475 k_BT`.
Two duplexes brought blunt end to blunt end pay a **finite** electrostatic price.
The measured short-range repulsion, whose decay length is **0.24 nm**, is eleven decay lengths away at 2.7 nm and contributes `1.4e−3 k_BT`.

**There is no steric exclusion in this slot to charge.**

### Ground 3 — what IS there is an attraction, and it is a design hazard rather than a clearance

Two blunt DNA ends **stack**. It is not a speculation: it is a load-bearing DNA-origami motif with four primary sources, and it is the mechanism by which Rothemund's own 2006 rectangles assembled into unwanted ribbons.

| quantity | value | read flag |
|---|---|---|
| free energy of one blunt-end stack **between two separate origami bodies** | **−2.63 kcal/mol per helix = −4.4114 `k_BT`** (1×TAE + 12.5 mM Mg²⁺, 22 °C) | **READ DIRECTLY** — Woo & Rothemund, *Nature Chem.* **3**:620 (2011), SI Table S4 |
| the same, single-molecule | −0.8 to −3.4 kcal/mol per stack, 20 mM MgCl₂ | **ABSTRACT ONLY (verbatim)** — Kilchherr et al., *Science* **353**:aaf5508 (2016) |
| **the RANGE**, oxDNA2's coaxial-stacking radial term | minimum **3.4072 Å**, **hard cutoff 5.1108 Å** | **READ DIRECTLY** — LAMMPS `pair_oxdna2` / Henrich et al., *EPJE* **41**:57 (2018) |
| **the RANGE**, all-atom PMF | force falls past **6.5 Å**, *"becomes slightly repulsive after ∼13 Å"* | **READ DIRECTLY** — Maffeo, Luan & Aksimentiev, *NAR* **40**:3812 (2012) |

> **So the collinear clearance's job is to stop consecutive arms from bonding to each other**, which would couple two independent levers into one body and defeat the output stage. That is a real requirement and it has a length — but the length is the **stacking range**, and the whole attractive interaction is inside **two base-pair rises**.

### Ground 4 — what it is worth

| collinear clearance | reading | plan budget `p − d` [nm] | `Q5` margin `p − d − L` [nm] | over the rise | × the published 0.02561 |
|---|---|---|---|---|---|
| **2.69** | the standing duplex girth | **8.19000** | **+0.02561** | 0.075 | **1×** |
| 1.81728 | `CH-0089`'s measured girth | 9.06272 | +0.89833 | 2.64 | 35.1× |
| **1.30000** | **the all-atom PMF's repulsive onset** — the generous end of the stacking range | **9.58000** | **+1.41561** | **4.16** | **55.28×** |
| 0.51108 | oxDNA2's coaxial-stacking cutoff | 10.36892 | **+2.20453** | 6.48 | 86.1× |

**34 of 34 place at every one of them**, and at the generous end the margin is **4.16 base-pair rises** — i.e. it clears `C-0072`'s floor 1 (the design quantum), floor 2 (the SAXS spread) and floor 3 (the thermal breathing), and comes within **1.28×** of floor 4, which `C-0072` itself classifies as *"a floor of resolution, not of failure"*.

---

## What this challenge does NOT assert

**It does not assert that 1.3 nm is the right allowance.**
The stacking range is bracketed by a simulation potential's cutoff (0.51108 nm) and an all-atom PMF's sign change (1.3 nm), and no measurement of the *range* exists.
A design would in any case quantise the choice at the 0.34 nm rise.

**It does not withdraw `C-0053`'s convention as a convention.**
`C-0053`'s own 43-of-45 verdict is unchanged at every width in the bracket (`C-0076`, reproduced), and `C-0041`'s Fact B contains no width worth moving.
What is challenged is the convention's *interpretation* — *"a full duplex of clearance"* — and therefore the **number**.

**It does not by itself make `Q5` quotable.**
`C-0079`'s own finding is that a hard-body width is a threshold on an unstated energy budget ([`CH-0094`](CH-0094-a-hard-body-width-is-a-threshold-and-no-plan-claim-states-one.md)), and `C-0072`'s conclusion that the margin is not quotable survives — on a **new** ground, that 0.0256 nm is **1.2373 %** of the pair energy at 2.7 nm.

## What survives in each claim

- **`C-0053`.** Its 43 of 45 is unchanged at every exclusion width tested; only its own sentence *"consecutive collinear elements need a full duplex of clearance"* is challenged.
- **`C-0069`.** Its whole element-space argument is untouched — the `(48 EI/k)^(1/3)` = 22.41 nm refusal, the `S/k` = 1122 nm axial refusal and the `c ≤ 2.3416` joint window contain no `d` worth moving, and the **placed count stays 34**. What moves is `Q5`'s margin, and with it the joint window, which should be re-read at whatever allowance a design adopts.
- **`C-0072`.** Its floors are correctly computed against the margin it was given, and its *conclusion* is reinforced rather than removed: `C-0079` supplies an independent reason the nominal verdict cannot be quoted to 0.03 nm.
- **`C-0066`.** **Untouched by this challenge** — its bound 4 really does have a body in the gap, and it is the **crossed** geometry. `CH-0089` is the challenge that applies there, and `C-0079` upholds it.

## How this challenge would fail

1. **A blunt-end stacking interaction with a range beyond ~2.7 nm.** Two independent sources put the whole attractive limb inside 1.3 nm; a third disagreeing would remove the widening.
2. **A demonstration that `C-0053`'s footprint convention is charging a fabrication or staple-routing allowance** rather than a body or a bond. It would then be a *design* number, not a physical one, and neither `CH-0089` nor this challenge would apply.
3. **A geometry in which consecutive arms are not coaxial** — a design in which the next element's root is offset across the row. `C-0026`'s one-row-per-duplex registration forbids that, and `C-0066` measured the escape as *along*-helix only.
4. **A demonstration that consecutive arms stacking is harmless**, in which case the gap has no requirement at all and the budget is the bare 10.88 nm pitch — which would widen the margin further, not narrow it.
