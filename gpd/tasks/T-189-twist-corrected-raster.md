# T-189 — Can `C-0086`'s 112 bp raster row be TWIST-CORRECTED?

**Leaf:** `A8.2` (the plan and lattice model the anchoring array is written on), with `A1.2`.
**Claim reserved:** `C-0133`. **Challenges reserved:** `CH-0158`, `CH-0159`.
**Raised by:** [`C-0107`](../claims/C-0107-row-end-prestrain-value.md) *Still open*, and by
[`C-0086`](../claims/C-0086-seamless-scaffold-routing.md)'s own validity range.

---

## Formulate

### The question, exactly

`C-0107` derives a **17.15–24.98°** row-end prestrain from the mismatch between the square lattice's
**33.75 °/bp** design twist and B-DNA's **34.2857 °/bp**,
and `C-0104` puts `T-5b`'s 0.10 flatness convention at **15.4497275°** of uniform row-end prestrain
(`CH-0122` corrects the ceiling that follows from a triangle inequality to **11.52°**).
Both published cures act on the **response** — a stiffer crossover, a re-optimised placement.
Both published *remedies in the literature* act on the **driver**:

- Snodin et al. (2019), read directly in `gpd/data/T-151-sources/`:
  the measured tile *"included a suitable number of sections with 31 base pairs between equivalent
  junctions in order to remove this net twist"*.
- Rothemund (2006), Supplementary Notes, read directly:
  his own program changes *"helical domain lengths … by single bases until the strain energy is
  minimized"*, with the worked edge example moving **5 bp → 6 bp**.

So: **is a twist correction available at all on a seamless boustrophedon row, and if it is, what does
it leave?**

### The tension the task exists to resolve

`C-0086` quantises the row length on **connectivity alone**: Rothemund's *"the distance between
successive scaffold crossovers must be an odd number of half turns"* binds the row length of a
boustrophedon, and at the design twist that makes the admissible widths the **odd multiples of 16 bp**
— 16, 48, 80, 112, 144 — of which 112 bp = 38.08 nm is the only one near §3's 40 nm.

A twist correction wants the **mean** inter-junction domain to realise B-DNA's twist, i.e. 1.5 turns in
**15.75 bp** rather than 16, equivalently 3 turns in **31.5 bp** rather than 32 — which is exactly what
Snodin's mixed 31/32 bp sections deliver.

**Those two demands may be arithmetically incompatible**, and settling that is the deliverable.

### Locked conventions

- Lengths **nm**, angles **degrees** where quoted and **radians** in code, `k_BT = 4.141947 pN·nm` at 300 K,
  aqueous **2 mM MgCl₂**; rise **0.34 nm/bp**.
- **Design twist** `ω_d` — degrees of azimuthal advance per base pair that the *crossover lattice* imposes.
  **Natural twist** `ω_n = 360/10.5 = 34.2857 °/bp` — B-DNA's, `C-0015`'s and `C-0107`'s value.
- A **domain** is one inter-column stretch of a row, nominally 1.5 turns (16 bp on the square lattice).
- A **row** is one duplex, `N` base pairs long, both of whose ends carry a scaffold crossover
  (`C-0095`: the row-end crossover is the raster turn).
- **Seamless-admissible** means the azimuthal advance across the whole row is an **odd** multiple of 180°.
- `Δω` is `C-0107`'s **signed** twist-rate mismatch, `(ω_n − ω_d)·(π/180)/rise` in rad/nm.
- **Dishing** is `C-0063`'s peak-dishing-over-free-stroke; **flat** means `≤ 0.10` (`T-5b`).

### Acceptance predicates

- **P1** — the closed-form enumeration is delivered **before any solve**: the set of
  `(D, N, domain mix)` that are simultaneously seamless-admissible and twist-corrected, with the
  incompatibility (if any) stated as a theorem rather than as an empty search.
- **P2** — either a **construction** near §3's 40 nm, with its residual `Δω`, or the statement that
  none exists, with the reason.
- **P3** — the row-end prestrain re-read at the corrected driver over `C-0107`'s **own** 12-cell
  `(C, α, p)` bracket, with the linearity in `Δω` **proved** rather than swept.
- **P4** — `C-0090`'s recommended 34-root placement re-read under the corrected driver, saying whether
  the **design** moves or only its **value** (`C-0104` records that the argmin moves at 17.14°).

### Verification type

**logical** (the quantisation theorem and the residual invariant are exact arithmetic)
**+ in-silico** (`C-0009`'s grillage and `C-0063`/`C-0090`'s exhaustive centro-symmetric placement
enumeration, re-run on the corrected lattice)
**+ literature** (Snodin and Rothemund, both already in `gpd/data/T-151-sources/` — `CLAUDE.md`'s
*check `gpd/data/` before fetching anything*).

---

## Plan

### The cheap bound, and it runs first

Two integer conditions on one row:

1. **connectivity** — `N·ω_d ≡ 180° (mod 360°)`, i.e. `N·ω_d = 180 q` with `q` **odd**;
2. **twist correction** — `ω_d = ω_n = 360/10.5`.

Together: `N = 180 q/ω_n = 5.25 q = 21q/4` with `q` odd. `21q` is odd, so `21q/4` is **never an integer**.
**The two demands are exactly incompatible, for every row length, at every domain mix.**
This is one line and it settles the *exact* form of the question with no code at all.

What is then worth computing is the **residual**: `|N − 5.25q|` over integers `N` and odd `q`, which is a
closed form, and the mixed-domain construction that realises it. Both run before any solve.

### The expensive half, justified against cost

Only two things need a solve:

- the boundary layer of `C-0107` re-read at the corrected `Δω` — but `u(±L/2) = Δω λ tanh(L/2λ)` is
  **exactly linear** in `Δω` and `λ` does not contain it, so this is an algebraic identity to be
  *asserted as a test*, not a sweep. Cost: zero.
- `C-0090`'s placement, which is a 163 296-member exhaustive enumeration per state on `C-0063`'s
  influence bank — `C-0104` already paid this three times, so the cost is known and bounded.

The twist-corrected row has **non-uniform** column pitches, so its column layout and its upward station
lattice have to be constructed rather than inherited. That construction is gated by requiring it to
reproduce `rasterColumnLayout` and `upwardRootLattice` **bit for bit** when every domain is 16 bp.

### What would falsify this approach

- **F1** — an integer row length that is exactly an odd number of half turns at 10.5 bp/turn.
  Would destroy the incompatibility theorem outright.
- **F2** — the residual `|N − 5.25q|` at the best `q` is *not* the same at every admissible `N`.
  Would destroy the invariant the whole answer rests on.
- **F3** — the generalised column/station construction does not reproduce the uniform lattice at a
  16 bp domain mix. Would mean the corrected lattice is a different object, not a perturbation.
- **F4** — the boundary-layer end residual is not exactly proportional to `Δω` (checked against the
  independent discrete chain solve, at two mismatches differing by a decade).
- **F5** — the corrected row's prestrain still exceeds `C-0104`'s 15.4497275°.
  Would mean the twist correction does not remove `C-0107`'s exposure.
- **F6** — the standing falsifier: a free plate under a **uniform** load on a uniform Winkler
  foundation dishes exactly zero, read on the support-free, prestrain-free lattice.

---

## Execute / Verify / File

Model in `src/main/kotlin/structure/TwistCorrectedRaster.kt` (new).
Study `structure.TwistCorrectedRasterStudyKt` → `gpd/results/T-189-twist-corrected-raster.json`.
Tests first, in `src/test/kotlin/structure/TwistCorrectedRasterTest.kt`.
