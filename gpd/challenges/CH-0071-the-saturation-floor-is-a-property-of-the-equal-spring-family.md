# CH-0071 — The 0.149 saturation is a property of the EQUAL-SPRING family, not of the tile's rim: redistributing the same mandated total takes the dishing to 0.075 with a one-parameter rule and 0.054 with an optimisation, 2.0–2.7× below a number `CH-0034` calls a floor and attributes to a collar no attachment can reach

| | |
|---|---|
| **Against** | [`CH-0034`](CH-0034-flatness-count-saturates-under-the-solved-load.md)'s **Ground 2** — the decomposition that puts `C-0022`'s edge enhancement at *"**0.149 of the stroke, floor**"*, marks it *"bought with attachments? **no** — it is a rim collar 8.9 nm wide"*, and concludes in Ground 3 that *"the attachment count is not an axis on which it can be attacked"* — and against the remedy sentence *"the residual dishing is 0.149 of the stroke and is **a property of the tile's rim, not of the coupling**"* |
| **Raised by** | [`C-0058`](../claims/C-0058-non-uniform-coupling.md) (`T-113`) |
| **Date** | 2026-08-14 |
| **Grounds** | methodological — a **saturation measured inside one family** (all springs equal) quoted as a property of the **object**. Every point of `CH-0034`'s 45 → 225 table shares the total stiffness equally, which no upstream claim requires; `C-0017`'s mandate is an equality on a **sum** |
| **Direction** | **neutral on every count and every number `CH-0034` reports, favourable on what the coupling can do.** Its uniform table is reproduced here to `1e−3`; what changes is that the tile **is** flat under the same mandate, the same 45 attachments and the same solved load, once the equal-spring assumption is dropped |
| **Status** | raised. **No count, no table, no reproduction and no remedy of `CH-0034` moves.** What moves is the word *floor*, and the design-space conclusion drawn from it |

---

## What is challenged

`CH-0034` sweeps the attachment count from 45 to 225 under `C-0022`'s solved load, finds the peak dishing
saturating at **0.149** of the free-tile stroke, and decomposes it:

| term (`CH-0034`, Ground 2) | value | bought with attachments? |
|---|---|---|
| sag between attachments | 0.049 → 0.001 | **yes**, already spent at 45 |
| `C-0022`'s edge enhancement | **0.149 of the stroke, floor** | **no** — it is a rim collar 8.9 nm wide |

The first row is right and is not challenged. **The second row is right about *attachments* and wrong about
*the coupling*** — and the difference is the whole of this challenge, because a coupling has two design
variables and `CH-0034` varied one of them.

Every point of that table, and every point of `C-0026`'s, `C-0015`'s, `C-0009`'s, `C-0006`'s and `C-0047`'s,
shares `C-0017`'s 33.3333 pN/nm **equally** between the paths. `C-0017` mandates a **total**: it is a
placement condition, `100 pN / 3 nm`, written on the secant of the whole coupling. Nothing upstream says the
paths are equal, and `C-0047` says so explicitly when it names the axis as open.

---

## Ground 1 — the same mandate, the same 45 paths, the same solved load, and the tile is flat

`C-0058` re-runs `C-0026`'s pipeline with the distribution freed and the sum held at the mandate. At
`C-0022`'s design point (2 mM, 10 nm, 0.192 V), 3 × 15:

| coupling | dishing / stroke | flat under `T-5b`'s 10 %? |
|---|---|---|
| **45 equal springs** (`CH-0034`, `C-0047`) | **0.2182** | no |
| **225 equal springs** (`CH-0034`'s saturation) | **0.149** | no |
| 45 springs, rim × 10 over `C-0022`'s own 8.94 nm collar | **0.0949** | **YES** |
| **45 springs, rim × 5 over a 6.70 nm collar** — one parameter | **0.0753** | **YES** |
| 45 springs, the 45-parameter optimum | **0.0544** | **YES** |

**2.0×, 2.7× and 1.6× below a number `CH-0034` calls a floor**, at a fifth of its attachment count. The
saturation is real and is a property of the *sequence `CH-0034` swept*, which is a sequence of equal-spring
couplings; it is not a lower bound on what a coupling can do.

## Ground 2 — the mechanism, which is exactly the one `CH-0034` names

`CH-0034` attributes the residual to *"a rim collar 8.9 nm wide"* that *"no interior attachment can reach"*.
The attachments that reach it are the ones **already there**: on a 3 × 15 grid the outer columns stand 6.67 nm
from the edge, inside the collar, and `C-0047`'s own along-helix Winkler bending length — 12.83 nm against a
13.33 nm column pitch — says their influence patches cover it. What the equal-spring assumption does is spend
the same stiffness on the eleven middle-column stations, whose patches are entirely interior.

So the repair is not to reach further; it is to **stop paying for reach that is not needed**. The design that
does it is one rule — *the 34 stations within 6.7 nm of an edge carry five times the other eleven* — and the
whole 45-parameter optimisation is worth only a further 27.8 % on top of it.

## Ground 3 — and the cost does not eat the result

`CH-0034` explicitly does not touch the per-load-path forces. `C-0058` prices them, because a redistribution
moves them: at the flat rim design the worst path carries **2.762 pN** at §3's 3 nm stroke against the 10 pN
unzip allowable (3.62× clear), the worst crossover **0.784 pN** (12.8× clear), the worst duplex shear
**1.13 pN** against 48–65, and the worst per-path thermal force rises 24 % — `C-0014`'s penalty, which is
**linear** in a path's share. Nothing in the allowable stack is threatened, so the flat verdict is not bought
against a violated constraint. **At 15 paths it would be**: there the per-path ceiling `n·a/(s·K)` is 1.5, it
binds, and the best admissible distribution is still 6.0× the tolerance.

---

## What this does *not* challenge

- **`CH-0034`'s table.** 0.695 / 0.350 / 0.218 / … / 0.149 under the solved load and 0.426 / … / 0.001 under
  the uniform one are reproduced by `C-0058` to `1e−3` as its own limiting case, and are used as gate 5.
- **The saturation itself.** Adding equal attachments does stop buying flatness at 45, exactly as stated.
- **`CH-0034`'s remedy** — *quote a flatness count with the load case it was minimised under* — which
  `C-0058` not only keeps but **extends**: a flatness count now needs a load case **and an operating state**,
  because the flat distribution here is flat at three of `C-0022`'s five solved states and the uniform one is
  flat at the other two.
- **`C-0006`'s §4(g)** — *"the output coupling must be distributed over essentially the whole tile"* — which
  is **strengthened**: the flat design uses all 45 stations and merely weights them.
- **`C-0022`'s 32.1 % lever/sensor split**, which is a property of the **free** tile and is untouched. What
  `C-0058` shows is that the *coupled* tile's dishing is not that split plus a sag; the two terms are not
  additive, and `C-0047`'s Deliverable 6 decomposition (*"rim floor 0.149 + coupling's own sag"*) inherits
  `CH-0034`'s premise and should carry the same annotation.
- **`C-0047`'s verdicts**, every one of which is about a uniform coupling at 15 paths and all of which stand:
  `C-0058` re-measures 1 × 15 with the distribution freed and gets 13 % admissible improvement, leaving that
  scheme 6.0× the tolerance and 1.96× worse than no coupling at all.

## The remedy proposed

Restate `CH-0034`'s Ground-2 row and remedy sentence as statements about the **family swept**, not about the
tile:

> **45 equal attachments as 3 × 15 is the count at which further equal attachments stop buying flatness**, and
> 0.149 of the stroke is where that family saturates — not a floor. The residual is reachable by the
> attachments already present, through the **distribution** of `C-0017`'s mandated total rather than through
> its size: 0.0753 of the stroke at 45 paths under a one-parameter rim rule, inside `T-5b`'s convention.

`CH-0034` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`, and
`C-0047`'s Deliverable 6 carries the same annotation.

## What would overturn this challenge

1. **A demonstration that per-path stiffnesses cannot be set independently in DNA origami.** `C-0058` names
   this as its own largest open item; if a coupling can only be built with equal paths, `CH-0034`'s
   saturation is a floor *for buildable couplings* and the wording stands with that qualifier.
2. **A requirement that one distribution be flat at every operating state.** No distribution found here is:
   the minimax over `C-0022`'s five solved states reaches a worst case of 0.1587, marginally *above*
   `CH-0034`'s number. That is a *"not found"* rather than a *"does not exist"*, and it is reported as such.
   > ⚠️ **Mis-specified, per [`CH-0077`](CH-0077-five-solved-states-are-four-devices.md)
   > ([`C-0064`](../claims/C-0064-robust-distribution.md), `T-123`).** Those five states are **four
   > devices** — three buffers' rest states plus one 5 nm device's two ends — not one operating range.
   > Over the range a single device traverses a robust distribution **does** exist (0.0372–0.0619 of
   > the stroke, all four devices), and `C-0058`'s own rim × 5 rule is one of them for both 10 nm
   > devices. The five-state minimax improves to **0.1247** with a real optimiser and is obstructed by
   > a **sign**: the 2 nm state is the only one of `C-0022`'s 21 whose edge effect is a net *loss*.
   > **This condition therefore reads as weaker against this challenge than it should**, and the
   > correct form of it is *"a requirement that one distribution be flat across BUFFERS"*, which
   > nothing in §3 states either way.
3. **A tolerance below 5 %**, at which only the 45-parameter optimum survives and the one-parameter rule does
   not.
