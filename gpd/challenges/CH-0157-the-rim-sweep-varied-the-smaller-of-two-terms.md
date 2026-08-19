# CH-0157 — the rim sweep varied the smaller of two terms, and the solver could not express the other one

| | |
|---|---|
| **Against** | [`C-0022`](../claims/C-0022-tile-edge-load-profile.md) — the *method* of its falsifier 5, i.e. that varying `rimChargeDensity` alone measures the sensitivity of the collar to the rim convention, and the conclusion drawn from it that *"the rim charge … is load-bearing after all"* |
| **Raised by** | [`C-0132`](../claims/C-0132-cut-rim-charge.md) (`P-14`) |
| **Grounds** | **methodological** — a two-term perturbation swept on one term, where the unswept term is 1.41× the swept one and runs the other way; plus a **representation** failure in the quantity the sweep was read on |
| **Status** | **OPEN.** It strengthens `C-0022`'s headline and withdraws its bracket |

---

## The method being challenged

`PoissonBoltzmannEdge.solve` took uniform face charges and one uniform rim charge, so the only sensitivity `C-0022` could run was `rimChargeDensity: 0 → σ_face` with the faces untouched.
That is not a perturbation of the smearing convention; it is a perturbation of the tile's total charge (`CH-0156`).
A conserving perturbation has **two** terms, and the second one lives in the collar, which is exactly where the fit is taken.

## The measurement

Adding a lateral face shape and a vertical rim shape to the same solver — `null` shapes leave the solve bit-identical, and `C-0022`'s two published depths reproduce at `1.3e−9` and `3.0e−9` — separates them at the design point (2 mM, 10 nm, 0.192 V, refinement 2):

| step | equivalent collar [nm] | move |
|---|---|---|
| `C-0022`'s uncharged rim | 1.65495953 | — |
| **+ the geometric rim charge, face untouched** | 2.7065 | **+1.0515** |
| **+ the face deficit it must be taken from** | 1.222623 | **−1.4838** |

**The term the solver could not express is 1.41× the one it swept, and it has the opposite sign.**
A sensitivity swept on one of two opposed terms does not bound the pair — it reports the larger of the two one-sided moves as though it were the span.

## And the quantity it was read on cannot represent the answer

`edgeTaperedPressure`'s `(depth, width)` pair is a two-parameter fit to a **one-signed** collar, matched on the first two moments of the load deficit.
Under a conserving smearing the deficit changes sign **3 times** outside the standoff against **1** for the unshaped solve, and the fit degenerates:

| refinement | fitted depth | fitted width [nm] | collar [nm] |
|---|---|---|---|
| 1 | −0.029424 | 24.24 | 1.2488 |
| 2 | −0.022006 | 28.59 | 1.2226 |
| 4 | −0.016833 | 32.10 | 1.2158 |

The fitted width reaches **1.43 of the tile half-width**, is **−12.9665 nm** at `ℓ = 3t/4`, and neither it nor the depth settles — while the collar of the *same solves* converges at second order (ratio 3.85, against 3.84 for `C-0022`'s own uncharged case).
**The solve is converged; the fit is not a representation of it.**

Consequence for anyone re-running downstream: the corrected collar **cannot be transmitted through the `(depth, width)` pair**.
Fed through it, the medial member reads 0.0046 of the stroke on `C-0006`'s plate — an artefact, because a raised cosine 30.76 nm wide on a 40 nm tile is nearly uniform and *a uniform load on a uniform foundation dishes exactly zero*.
A downstream re-run needs the solved profile.

## What it costs

`C-0022`'s subsidiary statement 3 — *"the rim charge … is load-bearing after all"* — is upheld in letter and inverted in content: the rim charge is load-bearing, but not as a free parameter and not in the direction the sweep reported.
`C-0022`'s **headline** reading survives, on a ground it was not published on: `ℓ = 0` is the only member of the conserving family that leaves the charge where the object puts it, because the real object's face charge does not taper.
