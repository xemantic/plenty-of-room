# CH-0073 — `C-0026`'s build rule is a statement about EQUAL springs: on `C-0058`'s non-uniform coupling the along-helix direction is no longer a symmetry but the ratio's own axis, and the pattern that restores exactly zero crossover force is the one the flatness verdict tolerates least — 31.6 % against 69.8 %, a factor of 2.21, with `C-0017`'s total held

| | |
|---|---|
| **Against** | [`C-0026`](../claims/C-0026-one-row-per-duplex.md)'s **build rule** — *"make the attachment paths on one duplex differ from each other rather than from their neighbours' — and **if they must differ, let the error be along the helix**"* — and the reading of its scatter table that treats *"alternating columns … `3e−11 pN`"* as the harmless pattern **simpliciter** |
| **Raised by** | [`C-0060`](../claims/C-0060-buildable-stiffness-ratio.md) (`T-122`) |
| **Date** | 2026-08-14 |
| **Grounds** | methodological — a **direction shown to be a symmetry of one design** carried as a build rule for a design that does not have it. `C-0026`'s exact zero is a consequence of every attachment on a duplex carrying the **same** stiffness; `C-0058`'s coupling deliberately does not, and on its 3 × 15 grid the along-helix index **is** the rim/interior index |
| **Direction** | **neutral on every number `C-0026` reports** — its 0.883 pN per unit amplitude, its `3e−11 pN` exact zero and its 17 % break-even are all reproduced or cited unchanged, and the rule remains correct for the **crossover-force** channel on both designs. What changes is that a second channel now ranks the two patterns **oppositely**, so the rule is no longer free |
| **Status** | raised. **No count, table, number or verdict of `C-0026` moves.** What moves is the scope of one design sentence |

---

## What is challenged

`C-0026`'s scatter table is the source of the only build rule in this corpus about assembly tolerance:

| pattern (`C-0026`) | sensitivity [pN per unit amplitude] | restored at `ε = 0.1` |
|---|---|---|
| alternating **rows** (`±ε` duplex by duplex, i.e. **across** the helices) | **0.883** | 0.0878 pN |
| alternating **columns** (`±ε` station by station, i.e. **along** the helices) | **0.000** | **`3e−11 pN`** |

and its rule: *"if they must differ, let the error be along the helix."*

**The exact zero is real and is not challenged.** It is a symmetry statement, and `C-0026` says so: a scatter along the helices *"does not break the across-helix symmetry"*, so no interface transmits anything. But the symmetry it does not break is the symmetry of **one row per duplex carrying identical springs** — `C-0026`'s own `m = 1`, equal-stiffness scheme.

**`C-0058`'s flat design is built on the opposite premise.** On its 3 × 15 grid the two outer columns are the rim stations and the middle column is (all but four of) the interior ones, so **the column index is the design variable**. A scatter alternating along the helices is then not a perturbation orthogonal to the design; it is a perturbation *of the design*.

---

## Ground 1 — the flatness channel ranks the two patterns the other way, by 2.21×

`C-0060` bisects, on `C-0058`'s own surrogate under `C-0022`'s solved load, the relative amplitude at which the peak dishing first reaches `T-5b`'s 10 % of the free-tile stroke. Built design (`C-0030`'s coupled flexure at 87 and 154 bp), both readings:

| pattern | threshold, **as built** | threshold, **`C-0017`'s total held** |
|---|---|---|
| alternating **rows** (across the helices) — `C-0026`'s *worst* | 69.1 % | **69.8 %** |
| alternating **columns** (along the helices) — `C-0026`'s *recommended* | 34.6 % | **31.6 %** |
| one whole duplex row off | 73.8 % | 74.5 % |
| one attachment off | never below 95 % | never below 95 % |

&nbsp;&nbsp;&nbsp;&nbsp;**The recommended direction is the binding one, by a factor of 2.21 with the mandate held.**

The *as built* reading (no renormalisation) is the honest tolerance and it confounds two effects: the along-helix pattern is collinear with the rim/interior split, so at its own threshold it has also drifted `C-0017`'s mandated total by **21 %**. **Holding the total fixed does not rescue it — the threshold falls further, to 31.6 %** — so the sensitivity belongs to the distribution and not to the total. Both readings are in the result file and neither is quoted alone.

---

## Ground 2 — the crossover channel still prefers it, which is what makes this a trade rather than a correction

At a common 10 % amplitude, on the same built design, solved on `C-0009`'s lattice:

| pattern | peak crossover force at `ε = 0` | at `ε = 0.1` | change |
|---|---|---|---|
| alternating rows (across) | 0.7737 pN | 0.7246 pN | **−0.0514 pN** |
| alternating columns (along) | 0.7737 pN | 0.6836 pN | **−0.1129 pN** |

**The along-helix pattern is still the better one for the crossovers** — `C-0026`'s finding survives in sign and in ranking. So the two channels do not disagree about the physics; they **rank the same two build rules oppositely**, and a designer now has to choose which channel to spend the tolerance on. On the numbers, the crossover channel has 12.9× of margin against the 10 pN unzip allowable (0.774 pN) and the flatness channel has 1.30× against `T-5b`'s convention — so the choice is not close, and the rule should be **inverted** for a non-uniform coupling.

---

## Ground 3 — `C-0026`'s own case cannot see this, which is why the scope was not stated

Run on the **uniform** coupling — `C-0026`'s own design — neither alternation has a flatness threshold at all: the uniform 3 × 15 coupling dishes **0.2182** of the stroke at zero scatter, already 2.2× outside `T-5b`'s convention, so there is nothing for a tolerance to lose. **The flatness channel is only sensitive to a scatter pattern once the design is flat**, and nothing in this programme was flat before `C-0058`.

That is the whole reason the rule was stated without a scope, and it is a methodological point rather than an error: **a build rule derived on a design that fails a requirement cannot be tested against that requirement.**

---

## What this challenge does NOT claim

- **It does not claim `C-0026`'s exact zero is wrong.** It is a symmetry of equal springs on one row per duplex, it is asserted there to `3e−11 pN`, and `C-0060` reproduces the *ranking* it implies on the crossover channel.
- **It does not claim the 0.883 pN per unit amplitude moves.** That number is **CITED** unchanged in `C-0060` and carried into its own threshold table.
- **It does not claim any tolerance has been measured.** `T-45` is still open; both claims deliver thresholds.
- **It does not claim the design is intolerant.** 31.6 % is 1.86× `C-0026`'s own 17 % break-even and 6× a 5 % staple tolerance. The design is comfortable on **both** patterns; what is challenged is the *rule*, not the margin.

---

## What would settle it

1. **A measured assembly tolerance** (`T-45`). If it lands below ~5 %, neither pattern binds and the rule is free again — for this design, at this ratio.
2. **A non-uniform design whose distribution is NOT collinear with a lattice direction** — for example `C-0058`'s 45-parameter optimum, or a rim rule on a grid with more than three columns. The along-helix direction would then stop being the ratio's axis, and `C-0026`'s rule should return. This is the direct test and it has not been run.
3. **A flat design at `C-0041`'s buildable 15 paths**, where the grid is a single column and the along-helix alternation is the *only* alternation available. `C-0058` shows no distribution is flat there, so the question may not arise.
