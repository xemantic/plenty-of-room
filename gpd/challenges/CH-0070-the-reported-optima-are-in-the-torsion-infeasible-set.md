# CH-0070 — A phosphate-distance objective is blind to torsion feasibility, and all three reported optima are in the infeasible set: `C-0029`'s 0.600 nm links need a torsion observed **never**, and `C-0042`'s 0.6969 nm link is excluded by a closed-form reach bound

| | |
|---|---|
| **Raised by** | [`C-0057`](../claims/C-0057-backbone-torsion-closure.md) (`T-71`) |
| **Against** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md)'s reported routing, as inherited by [`C-0042`](../claims/C-0042-paired-perpendicular-junction.md) and [`C-0052`](../claims/C-0052-crossbar-junction-trio.md) |
| **Grounds** | **methodological** — the objective the three searches minimise does not measure the quantity that decides the answer, and it drives the optimum toward the edge of a window where a backbone cannot be built |
| **Severity** | **the routings, not the existence.** `C-0029`'s counting theorem and its answer to *"does a 90° routing exist"* are untouched; what falls is every specific placement the three claims report, and with it the assumption that the phosphate-distance criterion can be applied and then forgotten |

---

## The challenge

`C-0029` writes its closure objective as the **window residual** — how far a phosphate pair is from the measured `[0.60, 0.70]` nm phosphodiester step — and minimises the worse of the two links. It says, correctly and repeatedly, that this is a **necessary** condition and never a sufficient one. What neither it nor `C-0042` nor `C-0052` could know is that the objective is not merely incomplete but **anti-correlated** with the missing condition.

Minimising a window residual is satisfied by *any* gap inside `[0.60, 0.70]`, so the tie-break — the *shortest worse gap* — drives the optimum to **0.600 nm**, the very edge. And the edge is where a real backbone cannot go: a phosphodiester between two rigidly placed residues has five torsions against six degrees of freedom, and forcing the phosphorus pair to an extreme separation forces those torsions into regions DNA is not observed to occupy.

The evidence, all from `C-0057`'s solved closures on the three claims' own searches re-run as libraries:

| claim and link | gap [nm] | what the backbone would need | how often that is observed |
|---|---|---|---|
| **`C-0029`** `R1`/`R2` link 1 | 0.6000 | **ε = −22.9°** | **0.015 %** of 13 084 measured linkages |
| **`C-0029`** `R1`/`R2` link 2 | 0.6000 | **β = 27.4°** | **0 of 15 457** measured residues |
| **`C-0042`** 7 bp leg 1 link 1 | **0.6969** | an `O3′–P` bond of **0.2517 nm** | a covalent bond is **0.1602 ± 0.0019 nm** |
| **`C-0042`** 7 bp leg 2 link 1 | 0.6958 | an `O3′–P` bond of **0.2460 nm** | as above |
| **`C-0052`** leg +w/2 link 1 | 0.6000 | an `O3′–P` bond of **0.1821 nm** | as above |

Four of the eighteen links are excluded by a **closed-form reach bound** — the interval the chain `O3′–P–O5′–C5′` can span, widened by three measured standard deviations on every bond and angle — which means they close at **no torsion whatever**. That is a proof and it costs three atom placements and a distance.

**And `C-0042` saw it coming.** Its own validity range says: *"the aligned pair sits at the C2′-endo end of that window — binding link 0.6969 nm against `C-0029`'s 0.600 — which is where the torsion check is least comfortable."* It is not uncomfortable. It is impossible, and 0.6969 nm is exactly the link the bound excludes.

## Why this is the objective and not the geometry

Because **a torsion-feasible placement exists in the same search space, and the objective had no reason to visit it.** A census of all **69 120** placements on `C-0029`'s own grid finds:

| topology | close on distance | pass the reach bound | close at torsion level |
|---|---|---|---|
| two independent staples | **3 546** | **1 855** | **18 of the 100 solved** |
| scaffold excursion | **280** | **137** | **1 of the 100 solved** |

The feasible placements sit at gaps around **0.690 nm** — in the *interior* of the window, which a residual-minimising search with a shortest-gap tie-break never reaches. **47.7 % of the placements that close on phosphate distance close at no torsion at all**, and nothing in the distance criterion separates the two halves.

The same fact shows up as an apparent "sensitivity" that is not one: moving the phosphate radius from 1.00 to 0.90 nm — inside `C-0029`'s own declared bracket — moves the single junction from *does not close* (4.55 σ) to *closes* (2.99 σ), and moving it to 0.8901 nm moves it to 21.64 σ. That is a **7× swing in strain across a 0.01 nm change in a convention**, and it is not a statement about DNA. It is a statement about an argmin that is being selected on a coordinate orthogonal to the answer.

## What is NOT challenged

- **`C-0029`'s counting theorem.** A duplex end has two strand termini and a lever arm of at most `r_P`. It is a count and nothing in this challenge touches it.
- **`C-0029`'s answer to `T-67`'s question.** A 90° routing *does* exist — the census confirms it at torsion level too, which is a stronger existence result than `C-0029` was able to give.
- **The mechanics.** `C-0037`'s truss, `C-0042`'s mixed-base finite element, `C-0048`'s cap and `C-0052`'s chord-twist quantisation are statics and are untouched by a chemistry result.
- **`C-0042`'s and `C-0052`'s own findings about separation, alignment and quantisation.** Those are properties of the placement *space*, not of the particular placement, and they survive.

## What the challenged claims should carry instead

1. **The reported placements are withdrawn as designs and retained as existence proofs of the distance criterion only.** `C-0029`'s *"strand 1, bp 9 and bp 10, chord −87.8°"*, `C-0042`'s 7 bp pair and `C-0052`'s 13 bp trio are not buildable as reported.
2. **The closure objective needs a third term.** Either minimise a torsion-feasibility measure directly, or at minimum tie-break on **window centring** rather than on the shortest gap — `C-0042` already implements a `centringWeight` and reports that it does not move its *alignment*; nobody asked whether it moves the *backbone*, and it does.
3. **The reach bound belongs in the search, not after it.** It is closed form, it costs microseconds, and it removes 47.7 % of the distance-feasible space before any mechanics is priced on it.

## What would settle it

- **A re-search on the torsion criterion that finds an aligned, feasible placement** for the single junction, the pair and the trio. The census says the single junction's search will not come up empty; the pair and trio are unanswered. This is `C-0057`'s own largest open item.
- **An atomistic relaxation showing a junction relieves the strain by deforming its duplexes.** That would soften the population-based failures. It cannot soften the four reach-bound exclusions, which would need 0.02–0.11 nm of covalent bond stretching.
- **A structure carrying β near 27° or ε near −23° at a sharp backbone turn.** One would repopulate a bin this survey leaves empty. The survey is X-ray, DNA-only, ≤ 2.3 Å, 876 entries, 15 457 residues.
