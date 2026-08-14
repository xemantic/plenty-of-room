# CH-0048 — The good-solvent premise was checked on monomers, and on Kuhn segments it fails

| | |
|---|---|
| **Challenges** | [`C-0007`](../claims/C-0007-solvent-quality-vs-salt.md) — its parameter-sheet row *"thermal-blob volume fraction `φ** = v/v₀` = 0.516"* and its gate-5 statement *"the good-solvent premise checked against the actual material — `φ/φ** = 0.06–0.14`, a factor of 7 of margin"* |
| **Raised by** | [`C-0036`](../claims/C-0036-concentrated-crossover.md), task [`T-21`](../tasks/T-21-concentrated-crossover.md) |
| **Raised** | 2026-08-14, iteration 5 |
| **Status** | **Raised.** The value is wrong by a fixed factor of **16.17**; the "factor of 7 of margin" becomes a factor of **0.9–2.2**, i.e. the layer *straddles* the crossover rather than sitting comfortably below it. Nothing else in `C-0007` moves, and the correction picks the **tighter** end of `C-0007`'s own transfer bracket. |

---

## The standing statement being challenged

`C-0007`'s parameter sheet:

> | thermal-blob volume fraction `φ** = v/v₀` | **0.516** | **DERIVED** |

and its gate 5:

> the good-solvent premise checked against the actual material — `φ/φ** = 0.06–0.14`,
> a factor of 7 of margin.

The same row is emitted by `material.SolventQualitySaltStudy` with the comment
*"the layer sits at 0.029–0.071, i.e. 0.06–0.14 of it — good-solvent premise holds"*.

## The methodological ground

**`φ** = v/b³` is a statement about a STATISTICAL SEGMENT, and `v/v₀` reads it on a monomer.**

The thermal-blob crossover is the volume fraction at which the correlation blob stops being
swollen. It is derived by writing the blob as space filling, `n = φ ξ³/v_seg`, its internal
statistics as Gaussian, `ξ = b √n`, and setting `n = g_T = (b³/v)²`. Both steps need a segment
whose *length* and *excluded volume* are its own: `b` is the Kuhn length and `v` the Kuhn **pair**
excluded volume, `n_K² v_m` (`CH-0020`).

`v/v₀` is that expression with the monomer substituted for the statistical segment — which means
identifying the segment length's cube with the monomer *volume*. For PEG those are 1.331 nm³ and
0.0604 nm³, a factor of 22, and this is `C-0002`'s own `a`-trap in a third costume: it already
forbids writing `a` for a volume, and `C-0007`'s row writes `b` for one.

Done on Kuhn segments the crossover is

&nbsp;&nbsp;&nbsp;&nbsp;`φ** = v_K v / b⁶ = (v_K/b³) g_T^(−1/2)`

and the ratio between the two readings is

&nbsp;&nbsp;&nbsp;&nbsp;**`φ**(monomer) / φ**(Kuhn) = b⁶ / (v₀ v_K n_K²) = 16.17`**

which **does not contain the excluded volume at all**. It is therefore the same factor for
`C-0007`'s Flory-Huggins route (`v_m = 0.03114 nm³`) and for `C-0003`'s osmometry route
(`v_m = 0.01225 nm³`), and it is asserted as a test in `ConcentratedCrossoverTest`.

## What changes

| quantity | `C-0007` | corrected | factor |
|---|---|---|---|
| `φ**`, Flory-Huggins `v`, scaling `g_T` | 0.516 | **0.0319** | 16.17 |
| `φ**`, Flory-Huggins `v`, Yamakawa `g_T` | — | **0.0105** | — |
| the layer's `φ/φ**` at rest | 0.06–0.14 | **0.9–2.2** | 16.17 |
| the layer's `φ/φ**` at the §3 3 nm stroke | — | **1.6–6.0** | — |

So the premise **fails** rather than holding with margin: at its own resting height the Gen-1
layer is already at or above the thermal-blob crossover, and the whole of the §3 stroke is spent
above it. The correlation blob is not swollen anywhere in the design space.

## What does NOT change, and why this is a bounded challenge

- **`C-0007`'s headline result stands entirely.** The buffer does not reach the layer's mechanics;
  the `χ(T)` fit, the water-lattice convention, the excluded volumes, the theta band and the
  salting-in/salting-out bound are untouched. This challenge is against one diagnostic row and
  the gate-5 sentence built on it.
- **`C-0007` already brackets the consequence.** Its transfer function from a change in solvent
  quality to a change in layer response is carried between
  `DES_CLOIZEAUX_TRANSFER_EXPONENT = 3/4` (which is what the *swollen*-blob premise licenses) and
  `MEAN_FIELD_TRANSFER_EXPONENT = 1` (which is what the corrected premise says). The correction
  therefore selects the mean-field end of a bracket `C-0007` already carries — and by `C-0007`'s
  own note that is *"the more forgiving of the two"* to quote and the **tighter** on `χ`.
  The correction moves a verdict inside a stated bracket; it does not leave one.
- **`C-0003`'s conclusion is reinforced, not contradicted.** `C-0003` already says the Gen-1
  chains are unswollen and that blob arguments do not apply to them. This challenge says the same
  thing about the *correlation blob* rather than the whole chain, and with `C-0007`'s own
  excluded volume rather than `C-0003`'s.

## What would refute this challenge

A defensible construction in which `φ** = v/v₀` is the right expression — i.e. a derivation of the
thermal-blob crossover that uses the monomer as the statistical segment *and* is consistent with
Gaussian statistics below the Kuhn length. `T-21` records that it found none, and that the
invariance test which settles it (the crossover must be the same number whichever segment it is
computed on, once `v` and `b` are coarse-grained consistently) is passed only by the Kuhn reading.

## Provenance

`gpd/results/T-21-concentrated-crossover.json`, `crossover.ConcentratedCrossoverStudyKt`,
`src/test/kotlin/crossover/ConcentratedCrossoverTest.kt`.
