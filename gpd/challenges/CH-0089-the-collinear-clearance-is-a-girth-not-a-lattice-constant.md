# CH-0089 — The gap in `C-0066`'s bound 4 and `C-0069`'s `Q5` is occupied by ONE free duplex, so what it must clear is that duplex's **girth**, not the lattice constant of a crossover-bonded pair; at this repository's own measured phosphate contact, **1.817276 nm**, the clearance is **0.898333 nm — 2.64 base-pair rises and 35× the published 0.0256** — which clears three of `C-0072`'s four floors and leaves the branch's knife edge a property of an **unmeasured convention** rather than of the lattice

| | |
|---|---|
| **Against** | [`C-0066`](../claims/C-0066-arm-slab-tie-clearance.md) bound 4 (*"the only gap the lattice offers clears a duplex by 0.0256 nm"*) and [`C-0069`](../claims/C-0069-output-element-placement.md) `Q5` (*"the plan budget on every 34-root placement is `pitch − d` = 8.19 nm exactly"*), and through them [`C-0072`](../claims/C-0072-plan-tolerance-model.md)'s *"neither margin is quotable"* |
| **Raised by** | [`C-0076`](../claims/C-0076-weave-exclusion-width.md), task [`T-137`](../tasks/T-137.md) |
| **Grounds** | **methodological** — one symbol used for three quantities, and the one with a measurement is not the one the number came from |
| **Status** | **UPHELD** by [`C-0079`](../claims/C-0079-unbonded-duplex-separation.md) (`T-139`), which also **closes this challenge's own failure route 1**: two unbonded parallel duplexes in 2 mM MgCl₂ have no equilibrium separation at all, on four independent measured methods, so no separation above 2.7156 nm exists to be found. **STANDS as a statement about what the 0.0256 nm is a margin AGAINST.** It does **not** assert that 0.898 nm is the true clearance: the honest outcome is that the exclusion width in this role is **unmeasured**, with a measured **floor** of 1.8173 nm and a counter-argument pointing the other way |

---

## What the two claims say

`C-0069`, conventions: *"**A duplex in plan is a rectangle of width `d = 2.69 nm`** (SAXS) … **A rooted element occupies `[root, root ± L]` and the next along the same row may start at `high + d`** — `C-0053`'s footprint convention … It is the convention that turns the 10.88 nm root pitch into an 8.19 nm ceiling."*

`C-0066`, bound 4: *"the root pitch minus the arm, against a tie's width — **2.71561 nm** against 2.69 nm … the only gap the lattice offers clears a **duplex** by **0.0256 nm**."*

`C-0041`, which both inherit: *"The exclusion width is the SAXS interhelical distance, 2.69 nm, not the 2.0 nm steric diameter. That is the **loosest** defensible choice and it is deliberate."*

## The challenge

### Ground 1 — the gap contains ONE body, and a lattice constant is not a body's width

`C-0066` is explicit about what stands in the gap: a **tie**, a duplex standing normal to the sheet, whose plan footprint is a disc of its own diameter. `C-0069`'s collinear clearance is the same thing generalised — the room the next element's root needs past the previous element's tip.

**2.69 nm is not the width of a duplex.** It is the centre-to-centre spacing at which a square-lattice origami holds two duplexes that are **bonded to each other by crossovers** — and the same measurement that establishes it (`C-0076`, Deliverable 4) shows that at their closest approach those bonded duplexes sit at **1.85 nm**, which is where their **backbones touch**. The gap between the lattice constant and the body is 0.87 nm of *space*, and charging it to the body is charging a design 32 % more girth than the body has.

### Ground 2 — the measurement exists and this repository made it

`T-71` surveyed 876 X-ray DNA-only RCSB entries and **13 084 crystallographic linkages** and emitted the constants into source. The B-form C2′-endo population phosphate radius is **0.908638 nm**, SD **0.066499 nm**, so phosphate-backbone contact is **1.817276 nm** — and the phosphate backbone **is** the duplex's surface, which is `CLAUDE.md`'s own entry.

`CLAUDE.md`'s asserted 2.0 nm is a round number from one 2024 secondary reading (`a_DNA ≈ 10 Å`). It is **9.1 %** above what 13 084 measured linkages give, and `CLAUDE.md` already carries the 8.9–9.4 Å fibre bracket that contains the measurement.

### Ground 3 — what it is worth

| reading of the exclusion width | value [nm] | `C-0069`'s budget `p − d` [nm] | `Q5`'s margin `p − d − L` [nm] | over the 0.34 nm rise | placed of 34 |
|---|---|---|---|---|---|
| **the standing SAXS lattice constant** | 2.690000 | **8.19** | **+0.025609** | 0.075 | 34 |
| this project's asserted steric diameter | 2.000000 | 8.88 | +0.715609 | 2.10 | 34 |
| **`T-71`'s MEASURED phosphate contact** | **1.817276** | **9.062724** | **+0.898333** | **2.64** | 34 |

**35.1× the published margin**, and against `C-0072`'s own four floors:

| `C-0072`'s floor | value [nm] | over the standing 0.0256 | over the measured-girth 0.8983 | still fires? |
|---|---|---|---|---|
| **1 — the base-pair rise** (the strongest, and the one `C-0072` says to quote) | 0.34 | **13.3×** | **0.38×** | **NO** |
| 2 — the two SAXS means, 2.73 − 2.69 | 0.04 | 1.56× | 0.045× | **NO** |
| 3 — the thermal axial breathing of the two segments | 0.26779 | 10.5× | 0.30× | **NO** |
| 4 — the arm tip's own bending at a perfectly rigid root | 1.80744 | 70.6× | **2.01×** | **yes** |

**Three of the four floors stop firing**, including the one `C-0072` singles out as *"the strongest statement available … the margin is below the quantum of the design language."* At the measured girth the margin is **2.64 base-pair rises**, i.e. **buildable and correctable**. The one that survives, floor 4, `C-0072` itself classifies as *"a floor of **resolution**, not of **failure**"*.

### Ground 4 — the conflation is demonstrable, and it is worth 12 arms or 4

`C-0069` publishes *"the 2.73 nm square-lattice interhelical distance (18 of 34)"* as a sensitivity. `C-0076` reproduces that 18 through `C-0069`'s own pipeline to `0.0` and then decomposes it. Moving one constant by **0.04 nm**:

| which of the three roles the 2.73 is applied to | placed of 34 |
|---|---|
| the **collinear clearance** alone, rows left at 2.69 | **22** |
| **`C-0069`'s own reading** — the **body width** moves too, so the arms become wider than the 2.69 nm rows they sit in and the rows stop being independent | **18** |
| the **row pitch** moved with it — the only physically consistent reading, because the rows *are* the sheet's duplexes at whatever the interhelical distance is | **30** |

`C-0069`'s 18 is arithmetically correct and it is a statement about arms overrunning their own rows, not about the interhelical distance.

---

## What this challenge does NOT assert

**It does not assert that 0.898 nm is the clearance.** A crystallographic contact distance is a **floor**, not an equilibrium separation: two unbonded duplexes in 2 mM MgCl₂ are held apart by electrostatics as well as sterics, and the very same weave measurement says that crossover-bonded duplexes **splay to 3.60 nm** wherever nothing pins them — which argues the free-body separation **up** as readily as the steric floor argues it **down**.

**The honest statement is that the exclusion width in this role is unmeasured**, with

- a **measured floor** of 1.817276 nm,
- an **empirical packing distance for bonded pairs** of 2.69 nm, whose transfer to unbonded ones nothing supports,
- and a **placement threshold** at `pitch − arm = 2.715609 nm` that the whole span straddles.

The 0.0256 nm knife edge is therefore **1 % of a number nobody has measured in the role it is used in** — which is a weaker object than either `C-0066` or `C-0069` presents, and weaker in a way that no tolerance model can repair, because a tolerance is a spread about a value and the value is what is missing.

## What survives in each claim

- **`C-0066`.** Its section argument, its 108 tie stations against 45, the registration finding, `CH-0079`, the swept-envelope result and the desired-stroke ceiling are all **untouched**. Only bound 4's *number* is challenged; its **structure** — that the only gap the lattice offers is `pitch − arm` — is upheld and reproduced to `8.3e−7` nm.
- **`C-0069`.** Its whole element-space argument is untouched: the `(48 EI/k)^(1/3)` = 22.41 nm refusal of the two-support family is 2.74× the budget at **every** reading of `d`, and the `S/k` = 1122 nm axial refusal contains no `d` at all. Its **placed count stays 34**. What moves is `Q5`'s margin, and with it the `c ≤ 2.3416` joint window, which should be re-read at the measured girth.
- **`C-0072`.** Its floors are correctly computed *against the margin it was given*. Three of them stop firing when the margin is read at the measured girth, which is a change in the **input**, not an error in the model.
- **`C-0041`** and **`C-0053`** do not move at all: `C-0053` places 43 at every width in the bracket, and `C-0041`'s Fact B is 2.59× and contains no exclusion width worth challenging.

## How this challenge would fail

1. **A measurement or calculation of the equilibrium separation of two unbonded parallel duplexes in 2 mM MgCl₂ above 2.7156 nm.** Then the standing convention is right, the count is 22 rather than 34, and `Q5` fails outright — at the *stiff* end rather than the knife edge. `C-0076` records this as open item 3, and oxDNA's 3.25 nm 2D-tile mean points that way.
2. **A demonstration that `C-0053`'s footprint convention is charging something other than a body** — a fabrication clearance, a staple routing allowance, a folding-yield margin. `C-0069` calls it *"a full duplex of clearance"* and `C-0066` calls it *"a tie's width"*, so on the record it is a body.
3. **A crystallographic phosphate radius survey disagreeing with `T-71`'s 0.9086 nm.** It is this repository's own measurement, retained with its generator script.
