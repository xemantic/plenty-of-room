# CH-0088 — `C-0072`'s weave bracket substitutes an ACROSS-helix separation of two CROSSOVER-BONDED duplexes into an ALONG-helix clearance between two UNBONDED bodies, so its `+0.866 / −0.884 nm` is not a bracket on the plan margin at all; the coefficient is **exactly zero**, and had the axis been right the reported ends would still have been wrong, because the *count* is a step function of the margin and `C-0072` propagated only onto the margin

| | |
|---|---|
| **Against** | [`C-0072`](../claims/C-0072-plan-tolerance-model.md), Deliverable 7, *"The framing changes: 2.69 nm is a Bragg lattice constant, not a local distance"* — the table headed *"read through the plan model"* |
| **Raised by** | [`C-0076`](../claims/C-0076-weave-exclusion-width.md), task [`T-137`](../tasks/T-137.md), which `C-0072` itself opened for exactly this purpose |
| **Grounds** | **methodological** — a measured quantity carried into a slot that names it but does not mean it |
| **Status** | **STANDS.** `C-0072`'s arithmetic is reproduced to `3.9e−4` (its own rounding) and its *conclusion from that arithmetic* is withdrawn |

---

## What `C-0072` says

> | read through the plan model | `d` used | margin `p − d − L` | verdict |
> |---|---|---|---|
> | the weave **minimum**, at a crossover | 1.85 nm | **+0.866 nm** | places comfortably — 34× the nominal margin |
> | the **lattice constant** this programme uses | 2.69 nm | **+0.0256 nm** | the knife edge |
> | the weave **maximum**, midway between crossovers | 3.60 nm | **−0.884 nm** | **does not place at all** |
>
> *"The measured weave brackets the verdict from comfortable to impossible, and the plan model samples neither end."*

and, in its headline: *"the 2.69 nm itself turns out to be a **Bragg lattice constant** over a measured **1.85 → 3.60 nm sawtooth** that brackets the verdict from comfortable to impossible."*

## The challenge

### Ground 1 — the axis

`C-0072`'s own identity is `M = p − d − L`, and it defines every term **along the helices**: `p` is the 32 bp root pitch, `L` is the element's plan length, and `d` is the clearance `C-0053`'s footprint convention charges between one element's **tip** and the next element's **root**, both on the same row.

The weave is not a quantity on that axis. Both primary sources define it identically, and both were re-fetched and read directly for `T-137`:

- **Bai et al., *PNAS* **109**:20012 (2012)**, Fig. 3 E/F caption: *"The pattern was computed using **the coordinates of base pair midpoints** … The **midpoints of neighboring dsDNA helices** move on average from a minimum distance ⟨d min⟩ = 18.5 Å at the cross-over to a maximum distance of ⟨d max⟩ = 36 Å."*
- **Snodin et al., *NAR* **47**:1585 (2019)**: *"We quantify the weave pattern of the 2D tile by measuring the **distance between the helix axes** (defined as the midpoint between the bases for each base pair) **for adjacent double helices**."*

A distance between two parallel helix **axes** is measured perpendicular to them. Its variation *along* `x` is a variation in a *transverse* separation; it has no component along `x`, and it is not a clearance between two bodies laid end to end.

### Ground 2 — the bonding

The weave's **minimum is at the crossover** — both sources say so verbatim — i.e. at the one point along the interface where the two duplexes are held to each other by a covalent Holliday junction. A hard-body exclusion width is a statement about bodies that are **not** held to each other. `C-0053`'s convention applies `d` between an arm's tip and the next arm's root, which share no crossover, no staple and no scaffold.

**Substituting 1.85 nm into that slot asserts that two unbonded bodies may approach as closely as two bodies a covalent junction is pulling together. Substituting 3.60 nm asserts that they must stand as far apart as two bodies a junction is splaying.** Neither is a statement the measurement makes.

### Ground 3 — even on the right axis, the ends are wrong

`C-0072` propagates onto the **margin** and never re-runs the **count**, and the count is a *step function* of the margin. `C-0076` performs `C-0072`'s substitution properly, with the weave as a genuine function of position on each row's own interface, through a generalised packer that reproduces `C-0063`'s `armDirections` bit for bit at a constant clearance:

| substitution | placed of 34 |
|---|---|
| the weave at Snodin's amplitude on the SAXS mean | **28** |
| the weave at Bai's amplitude and mean | **22** |
| `C-0072`'s reading of the same operation | *"places comfortably"* / *"does not place at all"* |

Resolution independent over four decades of `x`-snapping (a gate-4 test in `WeaveExclusionWidthTest`). **28 of 34 is neither end of the bracket, and no single-width reading produces it.**

### Ground 4 — on the axis where the weave DOES live, its coefficient at this design is exactly zero

`C-0055`'s upward roots are the crossover planes `k ≡ 2b+3 (mod 4)`, which is **odd** for every duplex `b`. The weave's extrema are the crossover planes `k ≡ 2b` and `k ≡ 2b+2 (mod 4)`, which are **even**. Snodin measures the profile as a **triangular** wave — *"the bending … is mostly localized at the junctions with the intervening sections basically straight"* — and a triangular wave takes its mean midway between consecutive extrema.

**So every one of `C-0063`'s 34 stations sits on a node of the weave**, on both bounding interfaces, at **all 32 crossover phases**, with a worst departure of `4.4e−16 nm` and a worst host-duplex axis offset of `0.0` — and **independently of the amplitude**, swept `0 → 2.5 nm` at `≤ 1e−14 nm`.

The whole **1.2–1.75 nm** amplitude spread the three sources disagree over therefore has **coefficient exactly zero** at this design.

---

## What survives in `C-0072`

**Everything except this table's conclusion.**

- The identity `M = p − d − L` and the observation that `C-0069` and `C-0066` published the same subtraction twice: **upheld**, reproduced here to `9.2e−6` and `8.3e−7` nm.
- The four floors, the correlation structure and its exact 7×, the twist's zero coefficient, the seat's non-monotonicity, the 30-path escape, Fischer's measured lattice width, Strauss's incorporation map and `CH-0084`: **all untouched** — none of them passes through the weave.
- The framing statement itself — *"2.69 nm is a Bragg lattice constant and not a local centre-to-centre distance"* — is **correct, important, and reinforced**. It is the reading *through the plan margin* that does not follow from it.

## What replaces it

The measurement's real consequence is not a bracket on the margin but a **question about the width's value in a role the weave never measured**. `C-0076` reports the placement threshold as the lattice quantity `pitch − arm = 2.715609 nm` and the defensible readings as straddling it — measured phosphate contact 1.8173, asserted steric 2.0 and the SAXS 2.69 all place 34 of 34; Bai's midpoint 2.725, the square lattice 2.73 and oxDNA's 3.25 all place 22.

## The precedent

This is the same family as `CH-0021` (*"a concentration factor and a per-path share live on different cuts, and multiplying one by the other is a category error"*) and `CH-0004` (*"the Debye length is three different numbers in this project, and all three are correct in their own place"*). `CLAUDE.md` records both. The new instance is worth its own line because the substituted quantity here carries the *same units and the same nominal value* as the slot it was put into, which is what made it invisible: 2.69 nm is genuinely the mean of the sawtooth **and** genuinely the number in the plan model, and they are still not the same quantity.

## How this challenge would fail

1. **A mechanism that holds two collinear, unbonded elements at a lattice constant along `x`.** None is in the plan model, and none is in the sources.
2. **A weave measurement whose extrema do not sit on the crossover planes** — the node congruence is what makes ground 4 exact. Snodin's Rothemund **seam**, where one group of helix pairs has an unusually long junction-free run, is the candidate, and `C-0076` records it as open.
3. **A demonstration that `C-0063`'s stations are not all one phase class.** `C-0065` established it and `C-0076` re-derives it from `C-0055`'s azimuth rule at all 32 phases.
