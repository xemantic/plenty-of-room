# T-196 — Where the four-layer tile stops being flat

**Leaf:** `A8.2`
**Raised by:** [`C-0109`](../claims/C-0109-four-layer-tile.md) §11 item 1
**Verification type:** in-silico (beam-and-hinge grillage) + logical (the scale inversion)
**Units:** rigidity in pN·nm, length in nm, dishing dimensionless as a fraction of the free-tile stroke

**Geometry and sign conventions**, restated rather than inherited: four honeycomb duplex layers, 15 rows,
`C-0086`'s buildable **38.08 nm** width; load is `C-0022`'s solved edge collar at 2 mM / 10 nm / 0.192 V;
foundation is `Gen1Tile.FOUNDATION_SECANT`; dishing is the peak deflection with its area-averaged
least-squares best-fit plane removed, on an 81 × 81 grid, over the free-tile stroke; **flat** means below
`T-5b`'s 0.10 convention. **No coupling anywhere** — this is the uncoupled free tile throughout.

---

## Formulate

`C-0109` found the four-layer tile flat **with no attachment coupling at all** (0.0577199433 of the stroke,
against the single layer's 0.307902368), which overturns this programme's flatness negative — and said
plainly that the verdict turns on one number, the interlayer coupling fraction `f`:

> **The threshold is `f` such that the free-tile dishing crosses 0.10, and it lies between 0.00 and 0.26.**
> Locating it is one sweep of the same study.

### Numeric target and acceptance predicate

**Target:** the value of `f` at which the uncoupled four-layer free-tile dishing crosses **0.10**, with its
bracket and its convergence — or the demonstration that the crossing is not a well-posed single number.

**Acceptance predicate.** All four hold:

1. The dishing is shown **monotone** in `f` — by counting *every* sign change over the whole interval, not by
   bisecting the first one found — or the alternation is reported instead of a threshold.
2. The crossing is located with a bracket, and its position relative to the **measured** band `f = 0.26–0.33`
   is stated as a ratio.
3. `C-0109`'s own two numbers reproduce, so that any comparison against them is licensed.
4. Convergence is declared on the mesh, the dishing grid, the scan resolution **and** the bisection width —
   and refining the *scan* must not move the root, because the bisection owns the precision.

**Falsifiers**, declared before the run:

- **`F1`** — the dishing is **not monotone** in `f`, so no threshold exists (`CLAUDE.md`: *a verdict that is
  not monotone in a swept variable has no threshold, and sweeping it finer finds more alternation rather than
  less* — `C-0070`'s lateral seat).
- **`F2`** — the crossing lies **above** 0.26, which would put `C-0109`'s verdict inside the measurement's
  own uncertainty and make it undecidable on published evidence.
- **`F3`** — `C-0109`'s numbers do not reproduce, in which case no comparison is licensed.

---

## Plan

**The cheap bound runs first and it removes most of the sweep.**
`multiLayerRigidities` admits `f` only through `realised = 1 + f(factor − 1)`, and that **one** number
multiplies `D_∥` **and** `D_⊥` alike — the identity `k_s/k_θ = S/B` that `C-0109` asserts as a test.
So `f` is a pure **scale** on the plate, the dishing is a function of that single scale, and the threshold in
`f` follows from the threshold in the scale by one division.
That is checked as a test (both rigidities scale by the same factor to `1e-12`) before any plate is solved,
and it is why this is a one-dimensional inversion rather than a two-dimensional search.

**Method.** Sweep `f` over the whole unit interval including the measured band's ends, then locate the
crossing with a scan-then-bisect that counts every sign change. `CLAUDE.md` forbids two shortcuts here and
both are avoided by construction: differentiating at `f = 0` (a tolerance threshold is not a slope at the
origin, and the two can have opposite signs) and bisecting a quantity whose monotonicity has not been shown.

**Justification against cost.** One plate solve per sample; the whole study is minutes. The alternative —
declaring the four-layer verdict without its threshold — leaves this programme's largest recent result resting
on a number nobody has bounded, which is the position `C-0109` explicitly declined to leave it in.

**What would falsify this approach.** That `f` is *not* a pure scale — that the two rigidities move
differently with the coupling — in which case the threshold is a surface rather than a point and the whole
inversion is invalid. Tested directly, and it is exact.
