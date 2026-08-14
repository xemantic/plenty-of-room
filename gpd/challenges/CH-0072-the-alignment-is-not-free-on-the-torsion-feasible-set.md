# CH-0072 — The chord alignment is free on the distance criterion and NOT on the torsion-feasible one: `C-0042`'s 0.00° at every separation becomes 6°–69°, its own recommended 7 bp row is the WORST of the seven at 69° — past the half right angle where the base's two axes exchange — and `C-0029`'s recommended scaffold excursion cannot be aligned at all

| | |
|---|---|
| **Raised by** | [`C-0059`](../claims/C-0059-torsion-feasible-routing.md) (`T-124`) |
| **Against** | [`C-0042`](../claims/C-0042-paired-perpendicular-junction.md)'s *"the azimuth costs exactly nothing … both base chords come out at 90.0°, i.e. EXACTLY on the flexure axis, at every separation"* and its recommended **7 bp** row; and [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md)'s headline *"a 90° routing exists and it is a scaffold excursion"*. Through them, [`CH-0056`](CH-0056-the-azimuthal-quantum-belongs-to-the-sheet.md), whose conclusion is upheld and whose **consequence** is not |
| **Grounds** | **a design variable priced on a search space larger than the feasible one.** The alignment is free over the placements that satisfy the *distance* criterion; the placements that also satisfy the *torsion* criterion are a sparse subset of those, and the alignment is not free over the subset |
| **Severity** | **the row pitch and the topology, not the existence.** The pair still closes at every separation from 6 to 12 bp with both junctions torsion-feasible; what falls is *"the azimuth costs nothing"*, the **7 bp** recommendation that `C-0042` resolved `C-0037`'s *"between 6 and 8 bp"* to, and `C-0029`'s preference for the scaffold excursion |

---

## What is claimed upstream

`C-0042`'s headline is that the alignment is not merely cheap but **free**:

> *"the azimuth costs exactly nothing: both base chords come out at 90.0°, i.e. **EXACTLY on the flexure axis**, at every separation, on both groove conventions, under the strict 'both junctions grounded on ONE sheet duplex' reading, and with both legs seated on the duplex's own axis."*

and its reason is `CH-0056`'s, which this challenge upholds:

> *"the standoff's rotation about its own axis is a CONTINUOUS free parameter and not the base-pair lattice `C-0029` named."*

`C-0029`'s headline names the topology:

> *"A 90° routing exists **and it is a scaffold excursion** … the best independent-staple routing *is* a scaffold excursion."*

## Why the argument does not survive the torsion criterion

**`CH-0056` is right about the chord and it is not right about the placement.** The chord azimuth is `ψ₀ + Δ/2 + π/2`, a function of the standoff's own azimuth alone, and `ψ₀` is continuous — so nothing quantises the *chord*. But **torsion feasibility is a relation between two bodies**, and it depends on `ψ₀` *relative to the seat duplex's phosphate lattice*. So the standoff may take any azimuth it likes, and only a sparse subset of those azimuths puts its two termini where a backbone can be built.

The subset is sparse but not thin, and the distinction matters:

| | of `C-0029`'s 69 120 placements |
|---|---|
| close on **phosphate distance** | **3 546** |
| also pass the closed-form **reach** bound | **1 855**, spread over **118 of the 120** azimuth values |
| of those, within `C-0029`'s own **±16.87°** alignment band | **414** |
| of the **120 best-aligned**, **closing at torsion level** | **7** |

So a *single* junction can be simultaneously feasible and **exactly** aligned — chord at **90.0°**, `cos²ψ = 1.0000`, at a 0.643 nm binding link in the interior of the measured window. `C-0042`'s statement is true of one junction.

**It is a pair that breaks it**, because a pair must find *two* closing placements at a **fixed separation** on **one** seat duplex, and only **18 of 90** axial positions carry a closing placement at all. The pair then inherits whichever chords those two positions offer:

| separation [bp] | 6 | **7** | 8 | **9** | **10** | 11 | 12 |
|---|---|---|---|---|---|---|---|
| `C-0042`'s worst misalignment | 0.00° | **0.00°** | 0.00° | 0.00° | 0.00° | 0.00° | 0.00° |
| **on the torsion-feasible set** | 33.0° | **69.0°** | 57.0° | **6.0°** | **6.0°** | 33.0° | 33.0° |
| `cos²ψ` reaching the loaded plane | 0.703 | **0.128** | 0.297 | **0.989** | **0.989** | 0.703 | 0.703 |

**And the separation `C-0042` recommends is the worst of the seven.** At 7 bp the worse leg's chord lands **69.0°** off the flexure axis, which delivers **12.8 %** of the base couple to the plane the flexure loads — and, worse, **69° is past the half right angle at which `C-0037`'s `TwoLinkBase` invariant stops being able to represent the base at all**, because beyond 45° the restrained and free axes have exchanged. That is not a degraded design point; it is outside the model the three claims are written in.

**Nine and ten base pairs deliver 6.0°**, inside `C-0029`'s own ±16.87° allowance and worth 98.9 % of the couple. So the row pitch is not free either — it is a real design variable again, and its optimum has moved.

## And the topology is not the one `C-0029` recommends

`C-0057` already found the two topologies are not interchangeable on feasibility — 3 546 against 280 covalent, 18 against 1 closing. This adds the axis the design cares about:

| topology | reach-feasible | in the ±16.87° band | closing, of the 120 best-aligned | **best closing chord** | `cos²ψ` |
|---|---|---|---|---|---|
| **two independent staples** | 1 855 | 414 | **7** | **0.0°** | **1.000** |
| scaffold excursion | 137 | 59 | **1** | **39.0°** | 0.604 |

The scaffold excursion's only closing placement in the 120 best-aligned is `C-0057`'s own census optimum at a −51.0° chord. **It is feasible and it cannot be aligned**, and a 39° misalignment costs 40 % of the base couple. `C-0029`'s *"the best independent routing IS the scaffold excursion"* was a property of its argmin; on the feasible set the two topologies part company, and the one to build is the one `C-0029` reported as the runner-up.

## What is NOT challenged

- **`CH-0056` itself.** A free duplex's chord inherits no lattice phase. That is upheld here and is exactly why the *chord* can reach 0.0° at a single junction.
- **`C-0042`'s existence result.** Two 90° junctions do close on one seat duplex at every separation from the 6 bp steric floor to 12 bp — and now they close at **torsion** level too, which is a stronger existence result than `C-0042` could give.
- **`C-0042`'s steric floor, its screw-image analysis, its mixed-base finite element, or its `Σ(Δy)² = 0`.** None is touched.
- **`C-0029`'s counting theorem**, which is a count.
- **`C-0052`'s leg-is-one-body constraint**, which is arithmetic on that count and is imposed unchanged in `C-0059`'s design table.

## What the challenged claims should carry instead

1. **`C-0042`'s *"the azimuth costs exactly nothing"* is a statement about the distance criterion and must be qualified as one.** On the feasible set the pair's alignment is separation-dependent and never zero.
2. **The recommended row pitch moves from 7 bp to 9–10 bp**, and the reason is chemistry rather than the free-plane crossing `C-0042` resolved it on. Both readings want a number in the 6–12 bp band; they do not want the same number.
3. **The recommended topology is two independent staples, not the scaffold excursion.**
4. **A misalignment beyond 45° is not a design point at all** under `C-0037`'s two-axis base invariant, and a search that can return one needs a guard. `C-0059` adds it.

## What would settle it

- **A deeper solve.** `C-0059` solves the 120 best-aligned placements of 1 855, and at most four candidates per axial position for the pair. A larger budget can only *improve* the alignment reported here — never worsen it — so the 6.0° at 9 bp is an **upper** bound on the misalignment and the 69.0° at 7 bp is one too. If a deeper search finds a 0° pair at 7 bp, this challenge's *ordering* falls and its methodological point does not.
- **An atomistic relaxation** showing a junction relieves its strain by deforming its duplexes. That would enlarge the feasible set and could restore the alignment at any separation.
- **A wider backbone survey** repopulating the torsion bins the closures fail in. Same effect.
