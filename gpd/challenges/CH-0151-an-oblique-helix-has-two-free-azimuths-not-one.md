# CH-0151 — **An oblique top-face helix has TWO free azimuths, not one, so `C-0122`'s station census is understated by 1.47–1.50×**

| | |
|---|---|
| **Against** | [`C-0122`](../claims/C-0122-honeycomb-station-lattice.md) — its census table (`15 × 4` → **90** stations, `10 × 6` → **60**), and the sentence that licenses it: *"**It fixes which helices carry the perpendicular root and which carry two oblique ones; it does not change the COUNT**, because every top-face helix has exactly one free direction either way"* (`HoneycombLattice.pointsDirectlyOut`'s KDoc, repeated in the claim's §5) |
| **Raised by** | [`C-0128`](../claims/C-0128-oblique-attachment-root.md) / [`T-206`](../tasks/T-206-oblique-root.md), result [`gpd/results/T-206-oblique-root.json`](../results/T-206-oblique-root.json) |
| **Grounds** | methodological — a **parity** correctly identified and then used to justify a **count** it does not support. The parity says *which* azimuths are free; the count needs *how many*, and the two sublattices differ in that number as well as in the direction |
| **Status** | ~~**raised.** The correction is **FAVOURABLE** and `C-0122`'s headline findings all survive it~~ **OVERTURNED** by [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) (`T-219`), via [`CH-0175`](CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md) — *"`CH-0151`'s 132 and 90 do not hold"*: the `±60°` pair belongs to a helix whose two up-oblique neighbours are **absent**, and on a full `m × n` block those neighbours are the other sublattice's helices in its own row, which are present. `C-0122`'s **90** and **60** are restored at departure `0.0` — while every *reason* `C-0122` gave for them is withdrawn in the same claim, so the count survives and the argument under it does not. The parity this challenge reads is correct; what it assumed is which members of the azimuth set are unoccupied |

---

## 1. What `C-0122` establishes and what it then assumes

`C-0122` reads the honeycomb's rules correctly from Douglas et al. — three azimuths 120° apart,
7 bp between consecutive positions over all azimuths, **21 bp** before one azimuth comes round
again — and it correctly identifies that caDNAno alternates the helix orientation between the
lattice's two sublattices, so that along a top face the free direction alternates.

It then multiplies **top-face helices × stations per 21 bp ladder**:

> `15 × 4` → 15 × 6 = **90**;&nbsp;&nbsp; `10 × 6` → 10 × 6 = **60**

which assigns exactly **one** azimuth to every top-face helix. That is true of the perpendicular
sublattice and false of the other one.

## 2. Why an oblique helix has two

A helix whose azimuth set points one direction **straight out of the slab** spends its other two on
the neighbours below it — one free azimuth. A helix from the other sublattice points one azimuth
**straight down**, into the layer beneath, and its other two obliquely **out of** the top face —
**two** free azimuths, at `±60°` from the normal.

`C-0122` says as much in the same breath — *"which helices carry the perpendicular root and which
carry **two oblique ones**"* — and does not carry the two into the arithmetic.

**They do not collide.** Each azimuth carries its own 21 bp ladder and consecutive positions over
all azimuths are 7 bp apart, so a helix's two free ladders are offset by 7 bp and interleave.
Asserted as a test: `SAME_PAIR_PERIOD_BP / ANY_AZIMUTH_STEP_BP == AZIMUTHS`.

## 3. The corrected census

| | top-face helices | perpendicular | oblique | **free azimuths** | per ladder | `C-0122` | **corrected** | factor |
|---|---|---|---|---|---|---|---|---|
| `15 × 4` | 15 | 8 | 7 | **22** | 6 | 90 | **132** | **1.46666667×** |
| `10 × 6` | 10 | 5 | 5 | **15** | 6 | 60 | **90** | **1.5×** |

The factor differs between the two because 15 is odd: the perpendicular sublattice gets the extra
helix, so the oblique count is 7 rather than 7.5.

## 4. What survives, and what a reader should stop quoting

**Everything `C-0122` concluded survives, and two of its findings are strengthened:**

- **All 8 of `C-0118`'s demands still fit**, with more margin: 10–75 paths against 90 and 132.
- **`C-0122`'s finding that a deeper block offers FEWER stations survives**: `10 × 6` supplies
  **90** where `15 × 4` supplies **132**, so the direction and the rough size are unchanged
  (1.4667× against the claimed 1.5×). The census is still set by the **face**, not the helix count.
- The snapped re-grading is untouched — it moved stations onto the 21 bp ladder and did not depend
  on how many ladders a helix owns.

**What should stop being quoted is the sentence, not the verdict.** *"Every top-face helix has
exactly one free direction either way"* is the only thing withdrawn, and with it the specific
numbers **60** and **90** as *ceilings*. They remain correct as the count of stations reachable
**at one azimuth per helix**, which is a real and possibly preferable design restriction — see below.

## 5. Why a design might still choose `C-0122`'s number

Two roots on one helix are **7 bp apart on the same duplex**. Ke et al. record, for the square
lattice's 8 bp domains, that *"some staple breaks must be implemented between crossovers 8 bp apart
… We observed significantly lower yield"* — and raised the yield of their block by **omitting**
crossovers. The corrected census is a **lattice** fact; whether a design can spend it is a
**folding-yield** question this challenge does not answer, and `C-0128` leaves it open.

So the honest statement is that `C-0122` counted the stations a **one-azimuth-per-helix** rule
offers, without saying that was the rule — and the lattice offers half as many again.
