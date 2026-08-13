# CH-0020 — The thermal-blob count is a convention, its excluded volume is coarse-grained linearly, and two errors nearly cancel

| | |
|---|---|
| **Challenges** | [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) — its *"the thermal blob is 1222 Kuhn segments, 3799 monomers, 167 kDa"* and the inference *"the whole Gen-1 design space is 0.02–0.10 of one thermal blob … blob arguments do not apply to Gen-1 chains at all"*; and `PegWater.thermalBlobKuhnSegments`, the code that produces it |
| **Raised by** | [`C-0019`](../claims/C-0019-mean-field-fluctuation-corrections.md), task [`T-1f`](../tasks/T-1f-mean-field-fluctuation-corrections.md) |
| **Raised** | 2026-08-13, iteration 4 |
| **Status** | **Upheld in part, and it is the *inference* rather than the number that fails.** `C-0003`'s conclusion — the chains are unswollen, so Gaussian Kuhn elasticity and not blob elasticity — **stands**. What does not stand is the margin, the code that computes it, and the reading of a blob count as a measurement. |

---

## The standing statements being challenged

`C-0003`:

> **PEG in water at 300 K is a marginal solvent** — `v = 12.25 Å³` against a monomer volume of
> 60.4 Å³ — so the thermal blob is **1222 Kuhn segments, 3799 monomers, 167 kDa**. The whole Gen-1
> design space is 60–375 monomers, i.e. **0.02–0.10 of one thermal blob.**

and `CLAUDE.md`, which carries it as a standing finding:

> the thermal blob is 1222 Kuhn segments (167 kDa) and any chain below ~40 kDa is unswollen. Blob
> arguments — Alexander-de Gennes heights, `k_BT` per blob, `ξ = s` — **do not apply to Gen-1
> chains at all**.

## Ground 1 — the excluded volume is coarse-grained linearly, and it must be quadratic

`PegWater.thermalBlobKuhnSegments` computes `g_T = (b³/v_K)²` with

&nbsp;&nbsp;&nbsp;&nbsp;`v_K = B · (n_K v₀) = n_K v_m`

i.e. the excluded volume of a Kuhn segment is taken to be `n_K` times a monomer's. It is not.
The interaction is `(v/2)∫c²` and the segment density coarse-grains as `c_K = c_m/n_K`, so

&nbsp;&nbsp;&nbsp;&nbsp;**`v_K = n_K² v_m`** — a **pair** quantity coarse-grains as the *square*.

This is not an opinion, and `C-0019` asserts it as a test rather than arguing it: the Ginzburg
parameter of a polymer solution, `Gz = √(v/(c b⁶))`, is **invariant under the choice of segment**,
and it is invariant only with `v_K = n_K² v_m` and `b_m = b/√n_K`. `C-0019`'s gate 1 evaluates it
in both conventions and gets agreement to 1e-12; with the incumbent coarse-graining the two
disagree by `n_K² = 9.67`.

The same statement, checked a second independent way: the thermal blob in *monomer* units,
`(b_m³/v_m)²`, is **392.8 monomers**, and `n_K × (b³/v_K)²` with the quadratic `v_K` is **392.8**.
With the linear one it is 3799.

**In the scaling normalisation the corrected blob is 126.3 Kuhn segments, 392.8 monomers, 17.3 kDa**
— not 1222 / 3799 / 167 kDa.

## Ground 2 — but the blob's prefactor is itself a published convention bracket, and the two errors nearly cancel

`g_T = (b³/v)²` is the *scaling* normalisation: it sets `z ≈ (v/b³)√n = 1`. Yamakawa's exact
two-parameter form (*Modern Theory of Polymer Solutions*, 1971, Eq. 13.32) is

&nbsp;&nbsp;&nbsp;&nbsp;`z = (3/2πb²)^{3/2} v √n`, &nbsp;&nbsp;prefactor `(3/2π)^{3/2} = 0.32992`

so `z = 1` gives `g_T = [b³/(0.32992 v)]²`, which is `1/0.32992² = **9.187×**` the scaling value.
And `n_K² = **9.671**`.

> **The incumbent number is within 5.3 % of the right answer in the wrong convention.** Correct
> `v_K`, keep Yamakawa's prefactor, and the blob is **1160 Kuhn segments** against `C-0003`'s 1222.
> Keep `C-0003`'s coarse-graining, drop the prefactor, and it is 1222. The two conventions are
> nearly reciprocal for *this* material and they cancel to within a twentieth.

That is a coincidence, not a justification, and it would not survive a change of polymer: the
cancellation is `n_K² ≈ (2π/3)^{3/2}`, i.e. `n_K ≈ 3.11`, which is a property of PEG's Kuhn segment
and of nothing else.

The convention spread is not this project's invention. Schroeder (*J. Rheol.* **62**, 371 (2018),
arXiv:1712.03555) states it in print for the same quantity:

> The thermal blob size is given by `ξ_T ≡ c b⁴/v(T)`, where `c` is a numerical constant of order
> unity … **estimates of the thermal blob size can vary widely depending on how the prefactor `c` is
> considered** … `c ≈ 0.1` … if the prefactor is assumed to be of order unity `c ≈ 1`, then the
> thermal blob size corresponds to 166 kbp, which illustrates that care is required in defining such
> properties.

— a ~30× bracket in `ξ_T`, i.e. ~10³ in `g_T`, for one material in one paper.

## Ground 3 — the inference does not survive either reading, and this is the part that matters

"0.02–0.10 thermal blobs per chain" was used to license two things: Gaussian Kuhn elasticity instead
of blob elasticity, and the stronger statement that blob arguments *do not apply at all*. The
convention-free quantity is not the blob count — it is the **Fixman parameter and the swelling it
implies**, both of which follow from `v` and `b` with no blob in them:

| `N` | Kuhn segments | `z` | `α² = 1 + 4z/3` | **`α = R/R₀`** |
|---|---|---|---|---|
| 27.8 (7 nm upper edge) | 8.94 | 0.0878 | 1.117 | **1.057** |
| **62.1 (10 nm design point)** | **19.97** | **0.1312** | **1.175** | **1.084** |
| 199.4 (`C-0001`'s chain) | 64.1 | 0.2352 | 1.314 | **1.146** |
| 375 (top of the design space) | 120.6 | 0.3224 | 1.430 | **1.196** |

**The Gen-1 chains are swollen by 6 % to 20 % in radius, and the longest chain in the design space
is at `z = 0.32`, at the edge of Yamakawa's own `|z| < 0.15` trust band for the first-order form.**
"0.06 of a thermal blob" reads as *"excluded volume is negligible"*; the same `z` says
*"the coil is 8 % bigger than the calculation assumes"*.

That difference is not cosmetic here, because `C-0016`'s **lower window edge at every height** is
coil overlap `Σ = πR₀²σ ≥ 1`, which scales exactly as `α²`.

## What follows, and what does not

**Does not follow.** That `C-0003`'s choice of Gaussian Kuhn elasticity over blob elasticity is
wrong. It is not: `n/g_T = 0.07–0.95` on the corrected scaling reading and `0.008–0.10` on the
Yamakawa reading, and the chain is sub-blob on both. **`C-0003`'s conclusion survives; its margin
does not.** At the design point it is 6.3× rather than 50×, and at the longest chain in the design
space it is 1.05× rather than 10×.

**Does not follow.** That any number in `C-0003`, `C-0011` or `C-0016` moves. Nothing downstream
consumes `thermalBlobKuhnSegments` numerically — it appears in a `KDoc`, in `C-0003`'s prose and in
`CLAUDE.md`. `C-0019` adds `thermalBlobKuhnSegmentsCorrected` **beside** the incumbent rather than
replacing it, per `SESSION-PROMPT.md`.

**Does follow.**

1. **The code is dimensionally wrong and should be corrected**, with the incumbent kept and
   annotated so that no result file changes silently.
2. **`C-0003`'s "0.02–0.10 of one thermal blob" and `CLAUDE.md`'s "do not apply at all" should be
   restated with a convention attached**, and with the swelling — 6–20 % — quoted beside the blob
   count, because the swelling is what a downstream task can act on.
3. **`C-0016`'s lower window edge moves**, by exactly `1/α²`. The windows get *wider*.
4. **This is the fifth convention in this project asked to do a measurement's work** — after
   `Σ ≥ 5` (twice), `L₀/R₀ ≥ 1` (twice), the three meanings of `a`, and the `χ` lattice site. It
   is the first one whose two errors nearly cancelled, which is the reason it survived three
   iterations.

## If this challenge is itself wrong

The way it fails is on ground 1, and it would fail if the `b` in `g_T = (b³/v)²` were not the
statistical segment length conjugate to the `v` beside it — if, for instance, the formula were
meant to be read with a *packing* length. It is not: `C-0019` pins the convention by an invariance
that has nothing to do with blobs, and the two independent monomer/Kuhn evaluations of the same
physical `Gz` agree only under the quadratic coarse-graining. Ground 2 is not a defence of the
incumbent — a number that is right because two conventions cancel is not a number that is right.
