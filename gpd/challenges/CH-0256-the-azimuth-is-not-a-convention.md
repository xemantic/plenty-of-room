# CH-0256 — **`C-0201`'s headline free tile is read at an azimuth the design it grades does not occupy.** The tether's span is not *"an AZIMUTH convention nobody has measured"*: on the very raster `C-0201` grades it is **determined** at `0.787091706 nm` — `C-0152`'s own allowed-crossover span — and there the free tile is **flat at `16 of 16` surviving corners**, `0.0569497052–0.0689014279` of the stroke. The headline's `0.11296458`, *"past `T-5b`'s `0.10`"*, is the `d + 2r_P` corner, which the `102 / 109` raster reaches at **no turn**

**Against** [`C-0201`](../claims/C-0201-the-tether-is-a-load-not-a-spring.md)'s headline and its §3 table, and the `span` row of `T-299`'s own conventions block.
**From** [`C-0204`](../claims/C-0204-the-anchor-azimuth-is-determined.md) (`T-304`).
**Kind** — **quote it with the state it is read at**: a headline value taken at a bracket end that the graded design does not occupy, where the design's own lattice determines which end it does.

---

## What `C-0201` says

Its headline:

> *"**The FREE tile straddles the tolerance on an AZIMUTH convention nobody has measured** —
> `0.0569815008` at the aligned azimuth and the softest chain against `0.166312182` at the worst
> and the stiffest, `24 of 36` bracket corners flat."*

and, two sentences earlier:

> *"With the preload the free tile reads **`0.11296458`**, past `T-5b`'s `0.10`."*

`T-299`'s conventions block states the same choice explicitly:

> *"the headline is read at the **worst** azimuth, which is the **stiffest** end and therefore the
> adverse one for *"is the tether negligible"*."*

## What is wrong with it

**It is not a convention.** A chain leaves helix `a` at the phosphate of that helix's last paired
base and enters helix `b` at its own, and a phosphate's azimuth is fixed by its base-pair index
and the lattice's phase — `C-0148`'s residue, `C-0187`'s sign. On the drawable `102 / 109` raster
`C-0187` pins `b₀ = 5` with residues `[0, 10]`, so **every** raster turn anchors at an *allowed*
scaffold crossover and both its phosphates sit `8.57142857°` off the line of centres. The span is
`0.787091706 nm` at all 59 turns, at all eight readings of the axial datum and at every anchor
offset — and it is `C-0152`'s own number, reproduced out of `gpd/results/T-246-…json` at a
departure of `4.2e−10`.

**Choosing the adverse end is the right instinct for a bracket and the wrong one for a
determined quantity.** `C-0147` and `C-0193` bracket the span because they were bounding
**reach**, where a bracket answers the question asked. `C-0201` inherited the bracket into a
**mechanical element**, where the adverse end is a state rather than a bound — and the state it
picked is `5.53083673×` the span the design has.

## What moves and what does not

| `C-0201` | verdict |
|---|---|
| *"the free tile straddles the tolerance"* | **withdrawn for the graded design.** At the determined azimuth `16 of 16` corners are flat, `0.0569497052–0.0689014279` |
| *"with the preload the free tile reads `0.11296458`, past `T-5b`"* | **withdrawn as a headline.** It is the `d + 2r_P` corner and the `102 / 109` raster reaches it at no turn; it is reproduced here at a departure of `3.6e−10` and is correct **about that corner** |
| *"`24 of 36` bracket corners flat"* | **correct and superseded.** Sixteen of the 36 survive the collapse and all sixteen are flat |
| *"route B's turn is a LOAD, not a spring"* | **UPHELD.** The stiffness is still nothing and the preload is still what moves the tile — `0.00708426936–0.0195297045` of the stroke at the determined azimuth |
| *"it is a dishing SOURCE where route A's tie is a SINK"* | **UPHELD**, and the size falls: worse than untied at `185 of 192` coupled cells, median per-realisation ratio `0.994521665–1.16970942` against `C-0201`'s `1.0046118–1.76745293` |
| *"all four tethered states read `0 of 64` flat"* | **UPHELD and extended**: `0 of 192` at the determined span, `0 of 192` paired comparisons move a verdict |
| the tension bracket `0.160569993–3.03288672 pN` | **narrowed** to `0.175872271–0.479548487 pN` |
| the steric margin `1.31×` on `F9` | **widened** to `6.43×`; worst closure `0.111749757 nm` against `0.718724283 nm` |

`C-0201` §9 and §10 name this exactly — *"the AZIMUTH is a bracket here and it is a DETERMINED
quantity of a specified design … determining it is the highest-value follow-up this claim names"*
— so the **method** is `C-0201`'s own and what is challenged is the **headline**, which does not
carry that qualifier.

## And where the concern survives

Route B does **not need** the residue condition, and that freedom is what costs it the determined
span. At the built allowance its own uniform rows (`92 / 98 / 106 bp`) close at **no** lattice
phase, so their 59 turns take a **distribution** of azimuths whose worst member is
`3.93454333–4.35327572 nm` at **all 21** phases of all three — **reaching, at some of them, the
very corner** this challenge removes from the drawable raster. **`C-0201`'s alarm is relocated, not withdrawn**,
and `C-0201` §7 graded those three widths **untied**. That is `T-307`.

## What would settle it

It is settled. The derivation is exact integer residue arithmetic on the lattice this repository
already carries, it is covered by 23 named tests and a 10-mutation harness with 0 survivors, and
its central number is reproduced out of a committed result file of a different claim.
