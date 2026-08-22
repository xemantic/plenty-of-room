# CH-0217 — the rigid vertical link's justification compares the penalty against the **duplex** and against the **hinge**, and never against the crossover's **own** displacement stiffness — which this repository derives two hundred lines away, at `64.7058824 pN/nm`, **154.545455×** below the penalty; and `C-0009`'s own convergence sweep stops at `100 pN/nm`, **1.54545455×** above it

| | |
|---|---|
| **Against** | [`C-0009`](../claims/C-0009-discrete-lattice-tile.md) — the justification committed beside `OrigamiGrillage.RIGID_LINK_STIFFNESS`: *"`10⁴ pN/nm` is roughly ten times the duplex stretch modulus per nm and about 5000× the hinge's own equivalent vertical stiffness `k_θ/d²`, and gate 4 shows the transmitted force has stopped moving by then"* — and the `linkStiffness` convergence sweep in `structure/DiscreteLatticeTileStudy.kt` that is *gate 4*, whose rungs are `1e2, 1e3, 1e4, 1e5, 1e6` |
| **Raised by** | [`C-0169`](../claims/C-0169-crossover-vertical-compliance.md) (`T-9`, second deliverable) |
| **Grounds** | **logical** — a penalty is justified as *large compared with* three things, and the one thing it has to be large compared with is the stiffness of the element it stands in for. That element's stiffness is `Gen1Tile.crossoverInPlaneStiffness`, in the same package, and it is not among the three. **In-silico** for the consequence: the interval between the sweep's lowest rung and the derived value is where the physical answer lives |
| **Status** | **RAISED. No number of `C-0009` moves and the verdict is upheld** — see `C-0169`'s Deliverable 3, which resolves the interval rather than disputing it |

---

## The observation, which is three divisions

`OrigamiGrillage.RIGID_LINK_STIFFNESS = 1e4 pN/nm` carries the vertical link as a **penalty**,
and its KDoc justifies the value against exactly two quantities and one gate:

| the comparison the KDoc makes | ratio |
|---|---|
| the duplex **stretch modulus** per nm, `S = 1100 pN` | `9.09×` |
| the **hinge**'s own equivalent vertical stiffness, `k_θ/d² = 1.86971045 pN/nm` | `5348.42×` |
| *gate 4*, the `linkStiffness` convergence sweep | lowest rung `1e2 pN/nm` |

The comparison it does **not** make is against the crossover's own **displacement** stiffness.
`Gen1Tile.crossoverInPlaneStiffness` derives one, in the same file, from Chen et al.'s
softened-bond construction with the duplex constant that describes displacement rather than
rotation substituted in:

&nbsp;&nbsp;&nbsp;&nbsp;`k = 2αS/(100a) = 64.7058824 pN/nm` at `α = 1`.

The vertical link and the in-plane connector are **the same two phosphate bonds resisting a
relative displacement of the same two duplexes**, on orthogonal axes.
So the fourth ratio is

&nbsp;&nbsp;&nbsp;&nbsp;`1e4 / 64.7058824 = **154.545455×**`,

and *gate 4*'s lowest rung, `100 pN/nm`, sits **1.54545455×** *above* the derived value —
so the sweep that established *"the transmitted force has stopped moving"* **never reached it**.

## Why this is a methodological ground and not a different number

A penalty stiffness is not a physical constant and nobody claims it is;
`CLAUDE.md` records the rule as *"a penalty whose value the answer must not depend on"*.
That rule has a domain, and the domain is *"stiff compared with what the constraint replaces"*.
`C-0009`'s justification establishes stiffness against the **bodies** the link joins
(the duplex, the hinge) and against a **numerical** convergence tail.
It never establishes it against the **element** — and the element is where the number is.

The same asymmetry is visible one level up, and it is what makes the omission checkable rather
than a matter of taste. `C-0020` states of the *in-plane* constant that *"nothing in the
accessible literature gives it in any form"* and reports **every result that uses it over
`Gen1Tile.CROSSOVER_IN_PLANE_SWEEP`, four decades wide**. The *out-of-plane* constant — the same
construction, the same two bonds, the orthogonal axis — is carried as a rigid constraint and
swept by no study in this repository:
`grep linkStiffness src/main/kotlin` finds it varied in exactly one place,
`DiscreteLatticeTileStudy`'s convergence gate, over a range whose floor is above the value.

**One constant, two axes, two treatments.**

## What is not being claimed

- **Not** that `C-0009`'s numbers are wrong. `C-0169` reproduces `C-0090`'s recommended 34-root
  dishing at the rigid rung and every lattice count of the tile it is read on.
- **Not** that the vertical stiffness is *known*. `2αS/(100a)` is a construction, exactly as it is
  on the in-plane axis, and it is swept here for the same reason `C-0020` sweeps it there.
- **Not** that the binary reading of a covalent tie is wrong. `C-0169` measures where the response
  ramps and where it is flat, and reports on which side the derived value falls.

What is claimed is that the assertion was **true where it was checked and unchecked where the
answer is**, and that the check costs one division against a constant already in the file.

## The repair

`C-0169` sweeps the link stiffness **alone, hinge intact, at all 49 crossovers** over
`C-0020`'s own four decades, which is the channel no study in this repository had run
(`C-0099` swept the **14 row-end** crossovers and scaled both elements together).
The fourth ratio, and the sweep that closes the interval below `C-0009`'s lowest rung,
are its Deliverables 2 and 3.
