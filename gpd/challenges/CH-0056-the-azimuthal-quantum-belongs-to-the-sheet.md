# CH-0056 — The 33.74°/bp azimuthal quantum is a property of the SHEET's phosphate lattice, not of the standoff's base chord: the standoff is a separate duplex and its rotation about its own axis is a continuous free parameter, so the "worst misalignment costs 8.4 % of the couple" allowance both `C-0029` and `C-0037` carry is not a cost at all

| | |
|---|---|
| **Against** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md) (cheap bound 3, and `DuplexBackbone.azimuthQuantum`'s own documentation), as inherited verbatim by [`C-0037`](../claims/C-0037-triangulated-standoff.md) (validity range, *"the base chord is assumed laid ALONG the flexure axis … `C-0029` shows the chord azimuth is quantised at 33.74°/bp and that the worst misalignment costs `cos²(16.87°)` = 8.4 % of the couple"*) |
| **Raised by** | [`C-0042`](../claims/C-0042-paired-perpendicular-junction.md), task [`T-97`](../tasks/T-97-paired-perpendicular-junction.md) |
| **Grounds** | **a bound evaluated on the wrong body** |
| **Effect on numbers** | **NONE, in either claim.** Both were conservative: `C-0029` says *"the phase is cheap; the ceiling is what binds"* and never applies the projection, and `C-0037` says explicitly *"that projection is **not** applied here, so the restrained-axis numbers are the best-phase ones"*. **What changes is not an arithmetic but a design obligation**: the 8.4 % is not a debt a designer has to check for, and the "best-phase" reading is not an optimistic assumption but the only one available. |

---

## What is claimed upstream

`C-0029`'s third cheap bound reads

> **the azimuthal quantum** — `360°/10.67 = ` **33.74°/bp**, worst misalignment ±16.87°, `cos²` = **0.9158** …
> **the phase costs at most 8.4 %** of the couple

and its `DuplexBackbone.azimuthQuantum` documents it as

> The quantum in which a junction's azimuth can be chosen … **The base chord's direction is set by which base pair of the standoff carries the junction**, and base pairs are integers, so the chord can be placed only on this lattice of directions.

`C-0037` carries it into its own validity range as the reason its restrained-axis numbers are *"the best-phase ones"*.

## Why it is on the wrong body

**The chord is a property of the standoff's own end face**, and `C-0029`'s counting theorem is what establishes that: a duplex END presents exactly two strand termini, at the two backbone positions of its **terminal** base pair. So the chord's direction is fixed by the terminal base pair's azimuth — and *which* base pair is terminal is not a design choice at all, it is simply the last one.

What sets that azimuth is the **rotation of the standoff about its own axis**, and the standoff is a **separate duplex**: nothing else in the structure fixes its helical phase. Rotating it costs nothing and is not quantised by anything. The base-pair lattice `C-0029` names would apply to a duplex whose phase is inherited by counting base pairs from a fixed junction — a **continuation**, not a free-standing standoff.

What *is* quantised at 33.74°/bp is the **sheet's** phosphate azimuth at successive base pairs, i.e. **where the link may land**, not **which way the chord points**. Those are different quantities on different bodies, and they enter the design differently: the sheet's lattice constrains *closure*, the standoff's rotation sets the *couple's axis*.

## The evidence

`C-0042`'s closure search sweeps the standoff's azimuth continuously and returns, at **every** separation from 6 to 12 bp, on **both** groove conventions, under the strict seat-duplex-only reading and at the seat duplex's own axis, a covalent pair whose **worse chord is at 90.0° — exactly on the flexure axis, misalignment 0.00°, `cos²ψ = 1.0000`**. The result is bit-identical at azimuth grids of 120, 180, 360 and 720 steps.

`C-0029`'s own single-junction optimum is consistent with this and was already evidence for it: it reports a chord azimuth of **−87.8°**, i.e. **2.2°** from the sheet-helix normal — a value that is *not* on a 33.74° lattice at all, and was reported without remark.

## What follows, and what does not

**Does not follow.** Any change to a published number. `C-0029`'s bound 3 was used only to conclude that the phase is *cheap* relative to the 3.34× the lever arm costs, and that conclusion is strengthened, not weakened. `C-0037`'s critical loads, spans, tangents, supply ratios, duties and margins are all reproduced by `C-0042` to ≤ 3.1e−4, and its `L2a8` loaded and free critical loads to 4e−10 through an independent solver.

**Does follow.**

1. **The 8.4 % is not a design allowance and should not be carried as one.** A future task that reserved margin against it would be reserving against nothing.
2. **The "best-phase" reading is the only reading.** `C-0037`'s caveat can be struck rather than merely noted, which is what `C-0042` does.
3. **The quantum still matters — one level down.** `C-0042`'s *sheet phase residual* is the correctly-attributed version: `n × 33.74° ± Δ`, folded modulo `π` because a chord is a line. It explains why the two standoffs at a 7 bp pitch come out as literal translates at one azimuth, and it is reported as an explanation and never used to decide anything.
4. **It is the same class of error this project already collects.** `CH-0021` applied an out-of-plane concentration factor to an in-plane problem; `CH-0004` substituted one Debye length for another; `C-0028`'s `B2` took a lever arm from the *sheet* when the part it belonged to was a *duplex end*. Here a lattice belonging to the sheet was applied to the standoff. **The general lesson is `CLAUDE.md`'s: quote a bound with the body it is evaluated on.**

## How this challenge would itself be defeated

By a routing in which the standoff's helical phase **is** inherited — for instance a standoff formed as a continuation of a sheet strand with no nick, so that its terminal base pair's azimuth is fixed by counting from the sheet. `C-0029` shows that motif does not exist: *"a nicked continuation cannot turn a corner"*, and the 90° routing it does find is an excursion whose standoff duplex is re-formed by hybridisation and therefore free in phase. If such a routing were found, the quantum would attach to the chord after all and the 8.4 % would be real.
