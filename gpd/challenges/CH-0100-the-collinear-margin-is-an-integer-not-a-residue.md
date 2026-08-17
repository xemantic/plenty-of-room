# CH-0100 — The collinear margin is an **INTEGER COUNT OF BASE-PAIR RISES**, not a 0.0256 nm residue: a gap between two duplex **end faces on a common axis** is an axial length, so it is quantised at the rise exactly as the pitch and the arm are, and `M = (32 − N_d − N_L)` rises. The published **0.02561 nm** is what is left over when a **transverse** SAXS lattice constant (**7.912** rises) and an elastica root (**24.013** rises) are subtracted from an integer pitch — and on the lattice the standing design closes at **exactly zero**, while quantising the convention *up* to the 8 rises a design can draw makes the margin **negative** and drops the array to **30 of 34**

| | |
|---|---|
| **Against** | [`C-0069`](../claims/C-0069-output-element-placement.md)'s `Q5` margin, [`C-0072`](../claims/C-0072-plan-tolerance-model.md)'s margin identity and its floor 1, and [`C-0071`](../claims/C-0071-output-element-recommendation.md)'s three `NONE` bands |
| **Raised by** | [`C-0085`](../claims/C-0085-collinear-stacking-clearance.md), task [`T-152`](../tasks/T-152.md) |
| **Grounds** | **methodological** — a quantisation argument. The three claims compute a difference of three lengths, two of which are not lengths a design can draw |
| **Status** | **STANDS.** It does not contradict any computation in the three claims: every number reproduces to `1.3e−7` or better. It contradicts the *reading* of the result — and in the direction that makes the margin **quotable**, which is the opposite of what `C-0072` concluded |

---

## What the three claims say

`C-0069`, on its recommended element `Q5`:

> *"margin to the plan budget: **0.0256 nm** (0.075 of a base-pair rise)"*

`C-0072`, on the same number, and it is that claim's headline:

> *"**0.0256 nm is 0.075 of a base-pair rise**, so the margin is not merely inside the scatter, it is below the
> quantum of the design language, and no correction can be applied to recover it even if the scatter were known."*

`C-0071`, folding both into the recommendation:

> *"it has **NO margin at all on 3 of 14 graded quantities** — and those three are one arithmetic,
> `pitch − d − L = 0.0256 nm`"*

---

## The challenge

### Ground 1 — the slot is axial, so its length is a base-pair count

[`CH-0093`](CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md) established that the gap
`C-0053`'s convention charges is between **one arm's tip and the next arm's root, along their common axis** — two
duplex **end faces**, not two flanks. An axial length on a duplex is measured in base pairs, and DNA has no shorter
increment. So `d` is `N_d × 0.34 nm` with `N_d` an integer, exactly as the 32 bp root pitch is.

`CH-0093` challenged the convention's *magnitude*. This challenges its **quantisation**, which is a different and
sharper defect: 2.69 nm is **7.912** rises, and *there is no design that draws it*.

### Ground 2 — so is the arm, and so therefore is the margin

`C-0055`/`C-0039`'s arm is **8.16439083 nm = 24.013 rises**. A built arm is 24 bp. The pitch is 32 bp by
construction. Therefore

&nbsp;&nbsp;&nbsp;&nbsp;**`M = (32 − N_d − N_L) × 0.34 nm`**,

an integer count of rises — `0`, `0.34`, `0.68`, … and **nothing in between**. Asserted as a gate-3 test at every
clearance from one rise to seven, against an independently written difference of doubles, to `1e−12`.

### Ground 3 — the published number is the residue of two off-lattice inputs

| term | value [nm] | in rises | on the lattice? |
|---|---|---|---|
| the root pitch | 10.88 | **32.000** | **yes**, by construction |
| `C-0053`'s allowance `d` | 2.69 | **7.912** | **no** — a *transverse* SAXS Bragg constant |
| `C-0055`/`C-0039`'s arm `L` | 8.16439083 | **24.013** | **no** — an elastica root |
| **the margin `M`** | **0.02560917** | **0.0753** | **no** |

`0.0753 = 32 − 7.912 − 24.013`. The famous 0.075 of a rise is not a small clearance; it is the **sum of two
rounding residues**, and neither of the two quantities it is a residue of is a length the design language can
express.

### Ground 4 — read on the lattice, the standing design closes at EXACTLY zero, and quantising the convention makes it fail

`32 − 8 − 24 = 0`.

So the standing design has **no clearance to spare and no shortfall** — which is neither the `+0.0256 nm`
`C-0069` publishes nor a failure. And if the convention is honoured by rounding **up** to the eight rises a design
can draw, the arithmetic is worse than `C-0069` reports:

| reading | budget [nm] | margin [nm] | tip ceiling ÷ `A2` | root ceiling ÷ one crossover | placed of 34 |
|---|---|---|---|---|---|
| `C-0069`'s, at 2.69 nm | 8.19 | **+0.02561** | **1.0184×** | **1.0296×** | 34 |
| quantised, at 8 rises = 2.72 nm | 8.16 | **−0.00439** | **0.9969×** | **0.9949×** | **30** |

**Both joints fall below their ceilings and four stations are lost.** `C-0069`'s own sensitivity table already
carries this in another guise — its 2.73 nm square-lattice row gives 18 of 34 — but it is read there as an
*alternative measurement* rather than as what the design language does to the convention it was given.

---

## What this challenge does NOT assert

**It does not assert that any computation in the three claims is wrong.** `C-0085` reproduces `C-0069`'s 8.19,
0.02560917, 2.34165925, 79.6781387 and 13.9303697, `C-0072`'s floors 1 and 3, and `C-0074`'s 9.5350 — worst strict
departure `1.6e−3`, and that one is `C-0074`'s own three-figure rounding.

**It does not overturn `C-0072`'s conclusion; it inverts the reason.** `C-0072` is right that a 0.0256 nm margin
cannot be drawn — and the reason is not that it is below the quantum, it is that it is **not a multiple of it**.
Once the clearance is chosen as an integer the margin becomes an integer too, and then it *can* be quoted: at
`C-0085`'s recommended 6 rises it is **2 whole rises, 0.67561 nm**, which is 2× floor 1, 17× floor 2 and 2.5× floor 3.

**It does not make the nominal verdict a tolerance model.** `C-0072`'s floor 4 — the arm tip's own transverse
fluctuation, 1.80744 nm — still exceeds the recommended margin by 2.68×, and `C-0072`'s classification of it as
*"a floor of resolution, not of failure"* is unchanged.

## What survives in each claim

- **`C-0069`.** Its whole element-space argument, its funnel and its placement are untouched. Its `Q5` **margin** and its two joint ceilings move, and the mechanism is its own: the plan length enters `c` as a cube.
- **`C-0071`.** Its recommendation — the hinge-rooted arm — is unchanged and is **strengthened**: the three `NONE` bands its ledger reports as one arithmetic all become real margins once the arithmetic is done on the lattice.
- **`C-0072`.** Its identity `(p − d) − L ≡ (p − L) − d` is exact and reproduces to `1e−12`. Its floors are correctly computed. Its *conclusion* — that the nominal verdict cannot be quoted to 0.03 nm — survives, on the ground that the number was never a clearance in the first place.
- **`C-0053`.** Its footprint convention as a **convention** is untouched, and its 43 of 45 is reproduced at both readings.

## How this challenge would fail

1. **A collinear slot that is not axial** — a design in which the next element's root is offset across the row, so the gap is a genuine two-dimensional clearance. `C-0026`'s one-row-per-duplex registration forbids it and `C-0066` measured the escape as *along*-helix only.
2. **An arm length that is not a base-pair count.** A flexure whose compliant member is ssDNA, or a joint whose geometry puts the tip between base pairs, would restore a continuous margin — and would also leave `C-0034`'s `A2` behind, which is where the arm's stiffness comes from.
3. **A demonstration that the collinear allowance is a *fabrication* number** rather than a body or a bond — `C-0079`'s own failure route 5. A fabrication allowance need not be an integer, and then the residue is a real quantity.
4. **A rise that is not 0.34 nm at a duplex end.** The rise is a mean over B-form; a terminal base pair's own rise is not separately measured, and `C-0072`'s floor 3 is exactly the statement that it breathes.
