# CH-0240 — **A level displacement rotates BOTH backbones the same way, so the allowed `8.57142857°` departure has coefficient EXACTLY ZERO on a relative roll — and that is the coordinate the corpus loads it through.**

| | |
|---|---|
| **Against** | [`CH-0228`](CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md), whose load is *"a built-in relative roll"* at every one of a raster's 59 turns; [`C-0175`](../claims/C-0175-drawable-raster-rim.md) (`T-254`) §8, which builds the unit-response bank and the triangle-inequality ceiling on `HoneycombGrillage`'s dihedral spring; and [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) §4, whose coupled prestrain deliverable carries the same term into a flatness verdict |
| **Raised by** | [`T-284`](../tasks/T-284-turn-prestrain-sign.md) / [`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md) |
| **Grounds** | **logical** — one line of algebra on the challenged claims' own azimuth convention, and a verbatim sentence from the model file `C-0152` and `C-0175` both consume |
| **Status** | **RAISED.** No number of `C-0175` or `C-0180` is disputed: every one of them is right about the object it was taken on. What is disputed is that the object is the load |

---

## 1. The corpus already states the premise, in the file that supplies the magnitude

[`tile/ForcedCrossoverPrice.kt`](../../src/main/kotlin/tile/ForcedCrossoverPrice.kt)'s own header,
verbatim:

> Displacing a crossover by `k` base pairs rotates **both** backbones by `k · 240/7°` in the
> **same** sense — the two helices are parallel and same-handed — so the departure folds to
> `(−180°, +180°]`.

*Both*, and *in the same sense*. That is not an aside: it is what makes
`span(θ) = √(d² − 4 d r_P cos θ + 4 r_P²)` the right span, because it puts the two phosphates at
azimuths `θ` and `180° + θ` rather than at `θ` and `180° − θ`.

## 2. So the relative azimuth is level-independent, exactly

Write `ψ_P(ζ)` for the backbone azimuth of helix `P` at axial level `ζ`, in the convention
`HoneycombRasterTurnSense` fixes — increasing counter-clockwise with `z`, `240/7°` per base pair.
Both helices of a bond are parallel, same-handed and at the same design twist, so

&nbsp;&nbsp;&nbsp;&nbsp;`ψ_P(ζ) = a_P + (240/7)·ζ` and `ψ_Q(ζ) = a_Q + (240/7)·ζ`,

and caDNAno's *"points of closest proximity"* fixes `a_P − a_Q = −180°` at the staple level. Then

&nbsp;&nbsp;&nbsp;&nbsp;`∂(ψ_P − ψ_Q)/∂ζ = 0`, identically, at every level and every displacement.

**A level displacement produces no relative azimuth at all.** The `8.57142857°` an allowed
scaffold crossover carries is a **common-mode** demand: both duplexes must roll the same way, by
the same amount, to bring their backbones back onto the line of centres.

## 3. And the model's tie prestrain is the relative coordinate

`HoneycombScaffoldTurnTie.prestrainRadians` is documented as *"the relative roll the tie is built
at"*, and `HoneycombGrillage.addPrestrainCouples` applies it as

&nbsp;&nbsp;&nbsp;&nbsp;`load[Φ_upper] += k_θ θ₀` and `load[Φ_lower] −= k_θ θ₀`,

an equal-and-opposite couple pair — the work conjugate of `Φ_upper − Φ_lower`, which is exactly the
quantity §2 shows the departure does not move. **A common-mode azimuth has coefficient zero on
it.** So the field `C-0175` §8 measures and `C-0180` §4 grades is the response to a load the
lattice does not demand.

## 4. What survives, and it is most of it

- **`C-0152` §5 is untouched.** Its `8.57142857°` is a statement about a **built** structure's
  geometry and its use is a **folding** calibration; nothing here disputes the angle or the
  calibration.
- **`C-0152`'s energy ceiling `½ k_θ θ²` survives as a ceiling.** It is a minimum over admissible
  deformation channels, and a relative roll *does* reduce the phosphate span, just less efficiently
  than a common one — so charging the departure to the dihedral spring over-prices it, which is the
  safe direction for a ceiling.
- **`C-0175`'s tie STIFFNESS deliverable is untouched.** The `1.12×` the 59 ties buy is a property
  of the elements' presence, not of any prestrain, and `C-0180`'s coupled re-grade at **zero**
  prestrain — its own headline `2 of 64` — carries no prestrain at all.
- **`T-284`'s derivation is untouched and is strengthened.** Which of caDNAno's two `±5 bp`
  positions each turn occupies is modular arithmetic on the closure condition and does not depend
  on how the departure is loaded. What this challenge adds is *why* the one residual global binary
  is not derivable: it is a sign convention of a coordinate whose true eigenstrain is zero.

## 5. What replaces it, and it is not zero

The demand is common-mode **at a site**, and `T-284` derives that the sites **alternate**: every
interior helix of the recommended raster carries `−5 bp` at one end and `+5 bp` at the other. So a
helix is asked to roll `+8.57142857°` at one end and `−8.57142857°` at the other — a demanded
**twist of `17.1428571°` over its own row**, which is to the digit the departure `C-0152` prices a
**forced** crossover at, arrived at from the other side.

That is a **per-beam torsional eigenstrain**, and `HoneycombGrillage` has no term for it: its
eigenstrains live on bonds, not on beams. Its coefficient is not zero — it reaches `W` through the
duplex's own `GJ` and the link's `d/2` arm — and **its sign relative to the term currently applied
is unknown**, so this challenge claims no direction, only that the two are different loads.
`T-291` is opened to price it.

## 6. What would falsify this challenge

One of:

- a demonstration that the two helices of a honeycomb bond are **not** at antipodal backbone
  azimuths at their crossover levels — which would make part of the departure differential. The
  lattice's own rule puts them antipodal by construction, and `forcedCrossoverSpan`'s
  `(θ, 180° + θ)` is that construction;
- a statement that `HoneycombGrillage`'s `Φ` is not a roll about the beam's own axis, or that the
  tie's couple pair is not the work conjugate of `Φ_upper − Φ_lower` — both are readable in the
  source and neither is;
- a physical argument that the common-mode demand at a **rim** tie is taken up by a relative roll
  in practice, because the neighbouring bonds hold one of the two duplexes and not the other. That
  is a real argument and it is a **quantitative** one — it is `T-291`, not a refutation, and it
  would make the currently applied term an approximation with a measurable error rather than a
  term with coefficient zero.
