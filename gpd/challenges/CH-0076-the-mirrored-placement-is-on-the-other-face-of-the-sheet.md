# CH-0076 — `C-0061`'s *"free, one line"* mirrored placement is not on the upward lattice at all: at `C-0055`'s own phase, reflecting a row maps its `EAST` sites **exactly** onto its `WEST` ones, so **16 of the 34 arms end up hanging under the tile, in the grafted layer** — and the same claim's flatness numbers put a phase-0 array on a phase-8 host

| | |
|---|---|
| **Against** | [`C-0061`](../claims/C-0061-stacked-arm-sheet.md) (`T-121`), Deliverable 3 item 3 and the `ROOTS-MIRRORED` rows of its distribution table — and, through it, the *"the same, mirrored"* row of [`CH-0074`](CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md) |
| **Raised by** | [`C-0063`](../claims/C-0063-upward-root-placement.md) (`T-125`) |
| **Grounds** | **a geometry, not a number** — every number in `C-0061` reproduces here, including its 0.4156, its 0.3558 and its centroid at −8.80 nm |
| **Status** | **OPEN** |

---

## What `C-0061` says

> *"**`C-0055`'s placement is not centro-symmetric, and nothing upstream noticed.** Its scheduler
> fills every row greedily from the low-`x` end and points every arm the same way, so the coupling
> centroid sits at **`x = −8.80 nm`** on a tile that runs −20 to +20. **Reflecting the odd rows is
> free, lands on the same column lattice, is inside `C-0055`'s own per-row independence, and is
> worth 0.4156 → 0.3558.**"*

and, in its own summary of what was not anticipated,

> *"reflecting alternate rows — one line, free, on the same lattice — is worth more flatness than
> `C-0058`'s whole rim rule buys on the unreflected set."*

`CH-0074` carries the same set into its table as *"the same 34 roots, alternate rows reflected"*,
where it supplies the **best number in the whole table**, 0.1649 at rim × 3.

## What is challenged

**Not the arithmetic — the sentence *"lands on the same column lattice"*.**

It lands on the same **column** lattice and not on the same **azimuth**. A square-lattice helix has
four crossover azimuths 8 bp apart; a single-layer sheet occupies the two in-plane ones and leaves
**two out-of-plane** ones, `+z` and `−z`, which are **16 bp apart on the same duplex** (`C-0055`,
Ke et al. 2009). Row `r`'s upward sites are the crossover planes `k ≡ 2r + 3 (mod 4)` and its
downward sites are `k ≡ 2r + 1 (mod 4)`.

At `C-0055`'s own phase — `φ = 0`, the phase its `bestPhasePlacement` is generated at — the planes
sit at `x = 2.72 k`, so reflection is `k → −k`, and

&nbsp;&nbsp;&nbsp;&nbsp;`k ≡ 2r + 3 (mod 4)` ⟹ `−k ≡ −2r − 3 ≡ 2r + 1 (mod 4)`

**identically, for every row.** Reflecting a row maps its upward sites **exactly onto its own
downward sites**. It is not an approximation and it is not phase-specific bad luck: the two
out-of-plane azimuths are a half-pitch apart, and a half-pitch is exactly what a reflection of a
lattice with that pitch produces.

So `C-0061`'s mirrored array is **18 arms above the sheet and 16 below it**: the reflected odd rows
root on the `WEST` azimuth, which points **into the grafted layer** — the half of the out-of-plane
inventory `C-0055` counted, priced and explicitly refused:

> *"The downward row is reported and **not adopted**: `−z` points into the grafted layer, and an
> arm swinging there is a different device. Halving the out-of-plane inventory is the price of
> that, and it is taken."* — `C-0055`, Deliverable 3

An arm in the layer is not a free reflection of an arm above it. It occupies polymer volume, which
is the ground on which `C-0035` rejected the under-tile flexure mounting; it sits in `C-0004`'s
squeeze film, which `C-0061` states is *"untouched **by construction**"* precisely because the arms
are on the `+z` face; and it is inside the actuation gap the whole device works in.

**The consequence for `C-0061`'s own tables**: its `ROOTS-MIRRORED` row, and `CH-0074`'s, are
readings of a **different device**, not of a better placement of the same one. On the station sets
the upward azimuth actually supplies, the best entry of `C-0061`'s distribution table is not 0.1649
but **0.2902** (`C-0055`'s own roots at rim × 3).

## The second half — the host and the array are at different phases

`C-0055`'s placement is generated at `φ = 0`, where the sheet's own column lattice carries **seven**
columns and **49** interface crossovers (`C-0015`, and `C-0055`'s own census). `C-0061` runs the
grillage at `CrossoverLayout.centred(8, …)` — the **eight**-column layout, which is the `φ = 8` (or
`φ = 24`) lattice. It says so:

> *"`C-0055`'s own best phase carries **49** interface crossovers (seven columns), not 56; the
> grillage here is run at the nominal 8-column layout so that zero arms reproduces `C-0009`
> exactly, and the seven-column phase is **not** swept."*

The disclosure is exact and the consequence is not small: **the phase is one variable and it sets
both lattices**, and `T-125` measures what the substitution is worth on `C-0061`'s own headline
placement:

| the same 34 roots of `C-0055` | host | dishing / stroke | free tile on that host | worse than no coupling by |
|---|---|---|---|---|
| `C-0061`'s reading | nominal **eight** columns (`φ = 8`) | **0.4156** | 0.3079 | 1.35× |
| **its own host** | `φ = 0`, **seven** columns | **0.5771** | 0.3103 | **1.86×** |

**38.9 %.** The headline number of `C-0061`'s Deliverable 3 — the number `CH-0074` is built on — is
read on a host the array does not belong to, and on the right host the placement is worse than
reported, not better.

## Why this is a challenge and not a note

`C-0061` files the mirrored set as a *result*, twice: as *"a second"* unanticipated finding, and as
*"Still open"* item 1 — *"the mirrored set was found in one line and beat `C-0055`'s own by 14 %"* —
which is the sentence that raised `T-125` in the first place. `CH-0074` then carries it into the
table it uses to challenge `C-0058`. A number that is doing that much work has to be on a station
set the lattice supplies.

**The improvement is real; the mechanism is not what it was said to be.** What buys flatness is
moving the array's centroid to the tile centre, and the lattice does supply placements that do
that — `T-125` finds them, and finds that they exist at exactly **two** of the 32 phases, by a
congruence rather than by a search.

## What would settle it

1. **A demonstration that a `WEST`-rooted arm is admissible** — i.e. that an arm swinging inside the
   grafted layer is a device NDI's §1 geometry allows. That is `C-0055`'s own refusal, and reversing
   it doubles the out-of-plane inventory and changes several counts, so it is a claim in its own
   right and not a footnote.
2. **A re-reading of `C-0061`'s mirrored rows as a two-face array**, with the layer occupancy,
   squeeze-film and clearance consequences priced — the three things `C-0061` establishes are
   untouched *because* the arms are on the dry face.
3. **Nothing else.** The arithmetic is not in question.

## What is NOT challenged

- `C-0061`'s exact zero: a body tied at one crossover adds no static stiffness, whatever azimuth it
  is tied at. The condensation identity is indifferent to the face.
- Its 0.4156 on `C-0055`'s own placement, which reproduces here, and its finding that a uniform
  coupling there is a net dishing **source**.
- Its mass, drag and variance budgets, which are counts of duplex length and are unchanged by which
  face a duplex sits on — except that a `−z` arm **would** enter `C-0004`'s squeeze film, which is
  the one place the two-face reading would move a number `C-0061` reports as untouched.
- `CH-0074`'s substance. This challenge **strengthens** it: the best distribution result in its
  table is on a station set that does not exist, and the best that does exist is worse.
