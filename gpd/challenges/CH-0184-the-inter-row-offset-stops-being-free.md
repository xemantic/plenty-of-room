# CH-0184 — **`C-0141`'s *"no answer here depends on the choice"* of the 7-or-14 bp inter-row ladder offset is true at a UNIFORM row length and false at `C-0140`'s two-length raster, where the choice decides a station COUNT.** At the recommended 112 / 108 bp raster the `10 × 6` face carries **55 of 60** stations at the 7 bp offset and **50 of 60** at the 14 bp one at the same phase, and over all 42 `(phase, offset)` pairs exactly **one** keeps the full sixty — **phase 11 at the 14 bp offset**, on `15 × 4` as well. A convention the corpus records as free is the difference between a six-column placement existing and not existing

| | |
|---|---|
| **Against** | [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) §9: *"The inter-row ladder offset is 7 or 14 bp and this repository cannot yet say which. Both are carried and **no answer here depends on the choice**."* |
| **Raised by** | [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) / [`T-235`](../tasks/T-235-coupled-cells-at-the-two-length-raster.md), result [`gpd/results/T-235-coupled-cells-at-the-two-length-raster.json`](../results/T-235-coupled-cells-at-the-two-length-raster.json), section `stationCensus` |
| **Grounds** | **logical.** An exact integer census over 21 ladder phases × 2 offsets × 2 cross-sections, on the row windows `C-0140`'s own level walk produces. No solve |
| **Status** | **raised.** `C-0141`'s own numbers are upheld at the uniform row length it was read at; what is withdrawn is the **scope** of the sentence, which quantifies over answers the claim could not yet see |

---

## 1. Why the sentence was true and has stopped being true

`C-0141` derives the station ladder on a **single** row length: every rooting helix carries the
same window `[0, rowBasePairs]`, so the inter-row offset is a rigid translation of alternate rows
along an axis on which every row is identical. Shifting alternate rows by 7 bp or by 14 bp then
changes **where** a station sits and never **how many** there are, and the claim's own results —
the census, the plan ceiling and the centro-symmetric family — are all invariant under it.

`C-0140` removes the premise. A honeycomb x-raster carries **both** turn senses, so no uniform row
length exists; at its recommended 112 / 108 bp assignment the `10 × 6` face's two parities carry
**different windows** — even rows `[−112, 0]`, odd rows `[−112, −4]`. The rows are no longer
identical, and a shift along the ladder now moves a station **out of** one parity's window.

## 2. The census

Stations on the `10 × 6` face, of the 60 the uniform raster carries, at the two-length raster:

| ladder phase | 7 bp offset | 14 bp offset |
|---|---|---|
| 0 (`C-0142`'s own) | **55** | 50 |
| 4 – 10 | 55 | 55 |
| **11** | 55 | **60** |
| 12 – 14 | 50 | 55 |
| 1 – 3, 15 – 17 | 50 | 50 |
| 18 – 20 | 55 | 50 |

- **50 to 60 over the sweep**, against a flat 60 at the uniform row length.
- **Exactly one pair of 42 saturates** — phase 11 at the 14 bp offset — and `15 × 4` saturates at
  **90 of 90** at the *same* pair and runs down to 75 elsewhere.
- At `C-0142`'s own convention (phase 0, 7 bp) the two-length raster carries `5, 6, 5, 6, …`, so
  **half the rows carry five stations**.

## 3. What it decides

Every column count `C-0118`, `C-0142` and `C-0146` grade — 1, 2, 3 and 5 — is inside the sparsest
row's ladder at every pair, so **no graded placement is refused** and the flatness verdicts do not
turn on this. What turns on it is the next count up: a **six**-column placement stands at the one
saturating pair and is refused at the other 41, and refusing it is a change of the path **count**,
not of the position.

## 4. What would settle it

The offset is the residue difference between the two face sublattices' crossover **bond classes**,
which caDNAno fixes and this repository has not read out. `C-0141` names the same open item; what
this challenge adds is that reading it is no longer optional, because the answer is now a count
rather than a placement. `C-0140`'s scaffold turn sense is a **different** variable and does not
supply it.

## 5. Scope

`C-0141`'s §3 census (90 and 60 stations at 112 bp), its §4 plan ceilings, its §5
centro-symmetric family and its 30° azimuth are all read at a **uniform** row length and are
untouched. What is withdrawn is one sentence of §9 — and only in the reading *"at any row
length"*, which is not the reading it was written under.
