# T-246 — price a FORCED scaffold crossover, or record that this repository cannot

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`CH-0188`](../challenges/CH-0188-the-recommended-raster-does-not-close.md), from [`C-0148`](../claims/C-0148-face-bond-class-residues-and-row-span-columns.md) (`T-244`) |
| **Claim number reserved** | `C-0152` |
| **Challenge numbers reserved** | `CH-0196`, `CH-0197` |
| **Verification type** | **logical** — closed-form rigid-body geometry on the honeycomb crossover-residue lattice against `T-71`'s measured backbone survey, plus a **literature reading** and, failing that, a recorded negative existence result |

## Formulate

`CH-0188` shows that `C-0140`'s recommended `112 / 108 bp` two-length raster does not close on
caDNAno's own `±5 bp` scaffold-crossover rule, and that **10 of its 59** raster crossovers on the
`10 × 6` block would have to be **forced**.
The challenge's severity therefore rests entirely on an unpriced assumption — that a forced
crossover is bad — and it says so:
*"that is a **yield** cost of the same family as Ke et al.'s 8 bp staple domains, and this
repository has no way to price it."*

Until it is priced, `CH-0188` is a **binary** where it should be a **cost**, and `C-0148`'s
conservative reading (*"buildable and off-rule"*) is an assertion rather than a measurement.

### Numeric target

The price of **one** forced honeycomb scaffold crossover, in whatever currency this repository can
supply — and, failing a published number, a **ceiling** (the largest the price can be) and a
**threshold** (the value the unknown would need for `CH-0188`'s verdict to change), per `P-6`.

### Acceptance predicates

- **`P1`** — the **geometric departure** a forced crossover implies is derived, not assumed:
  the azimuthal offset between where the raster puts the crossover and where caDNAno's rule allows
  it, as an exact function of the base-pair residue departure, minimised over the allowed set.
- **`P2`** — the derivation **reproduces `C-0147`'s two endpoints exactly** — `d − 2r_P` at zero
  azimuthal departure and `d + 2r_P` at 180° — so that the geometry is being read the same way the
  standing turn-slack claim reads it, and no new convention enters.
- **`P3`** — the forced crossover's phosphate span is compared against `T-71`'s **measured**
  phosphodiester step distribution, in σ and against its 99th percentile, so that *"does it close
  as a bond at all"* is answered on measured constants before any elastic model runs.
- **`P4`** — where it does not close, the **channel** that must pay is identified and the price is
  delivered as a **rigorous upper bound**: any single admissible deformation channel's cost is a
  ceiling on the true price, because the structure minimises over channels.
- **`P5`** — the ceiling is quoted **against a calibration this repository already carries** —
  the host sheet's own standing cost, `0.80 k_BT/nm` and `8.0 k_BT` per crossover column, which a
  fold pays and folds — and the transfer is licensed or refused explicitly.
- **`P6`** — a literature search for a published **yield or stability** cost of a forced crossover
  is run and **its query strings are recorded**, with a read-directly / abstract-only / not-found
  flag on every source, so that a negative existence result is falsifiable by one paper.
- **`P7`** — the **threshold** is stated: what the unknown would have to be for `CH-0188`'s
  severity to change, on each axis the price is delivered on.

## Plan

**Cheap bound first, and it decides the task.** The departure a forced crossover implies is a
rotation of both backbones by `Δ · 240/7°` (honeycomb, 10.5 bp/turn, 21 bp per interface), so the
two anchoring phosphates separate as

&nbsp;&nbsp;&nbsp;&nbsp;`span(θ) = √(d² − 4 d r_P cos θ + 4 r_P²)`,

which is `HoneycombTurnLoop.turnPhosphateSpan(d, r_P, θ, 180° + θ)` — the model `C-0147` already
published, **consumed unmodified**. Its two endpoints are `|d − 2r_P|` and `d + 2r_P`, which is
`P2`. A scaffold crossover carries **zero** unpaired nucleotides, so its span must fall inside one
**measured** phosphodiester step or it does not close at all. One closed form, no solve, no mesh.

**Then the elastic ceiling, and it is two springs in series.** If the span does not close, the
departure must be absorbed. `C-0104`/`T-182`'s `EdgeTwistRelief` already maps an azimuthal register
error at a crossover onto a **relative roll**, penalised by the crossover dihedral spring `k_θ` and
relieved by the duplex's own torsion over `λ = √(C p / k_θ)`. A localised defect `θ_f` on that
field costs `½ · series(k_θ, 2C/λ) · θ_f²` exactly, and the **rigid-duplex limit `½ k_θ θ_f²` is a
strict upper bound over the whole series** — so the ceiling needs no boundary-layer solve at all,
only the limit. Because the structure minimises over channels, **any** admissible channel's cost is
a ceiling; torsion is the one this repository has a calibration for.

**Justification against cost.** The alternative — an oxDNA or all-atom relaxation of a forced
junction — is exactly the spend `CLAUDE.md` warns against: *"a minimiser reports a local minimum
where a reach bound reports an impossibility"*. The reach bound here runs in microseconds and the
elastic ceiling in one multiplication, and both are falsifiable by a single published measurement.

**What would falsify this approach.** If the span identity did not reproduce `C-0147`'s two
endpoints, the geometry would be read differently from the standing claim and nothing derived from
it could be carried. If the forced span fell **inside** the measured phosphodiester step, there
would be nothing to price and the whole elastic branch would be vacuous — which is `F3` below, and
its firing would be the finding. If the elastic ceiling exceeded the host sheet's own standing cost
per crossover column, the ceiling would be useless as a bound and a real solve would be owed.

### Falsifiers

| | statement | fires if |
|---|---|---|
| **`F1`** | the span identity reproduces `C-0147`'s `d − 2r_P` and `d + 2r_P` | it departs by more than `1e−12` relative |
| **`F2`** | `17.142857°` is the **smallest** nonzero azimuthal departure the 21-residue lattice offers | any residue departure `k ∈ 1…20` folds below it |
| **`F3`** | a forced crossover does **not** close as a bond at rigid ideal geometry | its span falls inside the measured step at the 99th percentile — then there is nothing to price |
| **`F4`** | the elastic ceiling for all ten forced crossovers is **below** the host sheet's `8.0 k_BT` per crossover column | it is not |
| **`F5`** | no published yield or stability cost for a forced crossover exists | one is found — which would be the better outcome |
