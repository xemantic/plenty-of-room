# CH-0031 — A single-stranded hinge cannot absorb the flexure's draw-in, because a flexible link has no direction: it releases the beam along its axis and drops it across

| | |
|---|---|
| **Against** | [`C-0023`](../claims/C-0023-two-sided-coupling.md)'s remedy for the axial-restraint bracket — *"**A two-nucleotide single-stranded hinge at each end absorbs it**, and it is loaded along the beam's axis, not in the normal load path"* — and, through it, its `E3a` *ends free to draw in* column as a **buildable** reading rather than an idealisation |
| **Raised by** | [`C-0025`](../claims/C-0025-flexure-end-joint.md) (`T-30`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a joint sized against **one** of the three load components it has to carry, with the other two not written down |
| **Direction** | **unfavourable, and it removes an escape rather than a number.** The free-to-draw-in column is not reachable by the remedy `C-0023` proposed; it is reachable by a different joint, which `C-0025` supplies |
| **Status** | raised. **No number in `C-0023` moves** — its four spans (24.61, 39.07, 49.41, 54.91 nm), its two tangents (33.333, 91.13 pN/nm), its 8.08/86.7 pN tensions and its 0.88 nm draw-in demand are all reproduced here to ≤ 5.9e−4 — and the design that replaces the hinge lands **inside** its `E3a`/`E3b` bracket, not outside it |

---

## What is challenged

`C-0023` carries the flexure's axial restraint as a bracket and is explicit that the bracket is the binding
design choice:

> **Axial restraint is the binding choice.** If the ends are held, the beam cannot deflect without stretching
> … the tangent at the working point becomes **91.13 pN/nm** … **2.3× past this task's declared 40 pN/nm
> compliance ceiling** … and the beam's own axial tension is **86.7 pN at §3's desired 10 nm**, **past the
> 65 pN nicked-duplex ceiling**.

and then closes the bracket in one sentence:

> If instead the ends can draw in, the element is exactly linear and the demand it places on its end joints is
> **0.88 nm = 2.6 base pairs of in-plane draw-in** … **A two-nucleotide single-stranded hinge at each end
> absorbs it**, and it is loaded along the beam's axis, not in the normal load path.

**The arithmetic is right. The joint is not a joint.**

---

## Ground 1 — a beam end carries three things, and the sentence sizes one

The end of a simply supported or elastically restrained beam transmits **three** components: a transverse
shear `R/2` (in **both** directions, because the coupling is two-sided — that is the whole of `C-0023`), an
axial force, and a moment. `C-0023`'s sentence sizes the **axial** component only, and it is correct there: a
two-nucleotide chain at ~1 pN extends by well under a nanometre and the membrane term collapses to 5.5 % of
`S`.

But the same element has to react the transverse shear, and

> **a flexible link has no direction of its own.** Its transverse secant and its axial secant are both `f/x`,
> so its **anisotropy is exactly 1**.

`C-0025` evaluates it rather than arguing it. At `C-0023`'s own 45 paths and §3's working point the beam's own
per-path stiffness is `33.3333/45 = 0.7407 pN/nm`, and a 2 nt hinge on the **zero-force** end of the Kuhn
bracket supplies **4.55 pN/nm** across the beam — 6.1× the beam, against the 10× a support needs — while a
10 nt hinge, which is what the *compliance* ceiling would ask for, supplies **0.91 pN/nm**, i.e. **1.23× the
beam it is supposed to support.** The support and the beam become the same spring.

## Ground 2 — and the slack is isotropic, so it is a transverse **dead band**

The axial release the hinge supplies is its contour. The same contour is free play in **every** direction:
the beam's end can move transversely by up to `0.65 n` nm before the hinge is taut and reacts at all.

| hinge | axial release bought | transverse **dead band** | of §3's 3 nm stroke |
|---|---|---|---|
| **2 nt** (`C-0023`'s own) | 1.30 nm | **1.30 nm** | **43 %** |
| 5 nt | 3.25 nm | 3.25 nm | 108 % |
| 10 nt | 6.50 nm | 6.50 nm | 217 % |

**The hinge trades a membrane term for a dead band of exactly the same size**, and the dead band is measured
against the stroke while the membrane term is measured against a stiffness ceiling. At two nucleotides the
trade is 0.88 nm of draw-in for 1.30 nm of lost stroke — it is not a small correction, it is worse than the
thing it fixes.

## Ground 3 — this is `C-0014`'s own theorem, in a new place

`C-0014` proved, and `CLAUDE.md` records:

> For any flexible link crossing the layer, `k_lat/k_norm` is the **secant over the tangent** of its
> force-extension law, which convexity bounds at **1** — so a through-layer path costs at least as much normal
> stiffness as it buys laterally.

The flexure's end joint is the same statement rotated: a link cannot be stiff in the direction it is asked to
support and soft in the direction it is asked to release. `C-0023` escaped this once already, at the level of
the *element* — *"an element loaded transverse to its axis, or through a hinge, does not have to choose,
because its compliance is bending and a bending moment is signed"* — and the escape was not carried down to
the **joint**, where the same trap sits one level lower.

---

## What this does *not* challenge

- **The two brackets themselves.** They are exactly right, and `C-0025` reproduces all four of their spans and
  both of their tangents to ≤ 1.3e−4 as the two limits of one partial-restraint model.
- **The direction of the finding.** Axial restraint *is* the binding choice, and the restrained reading *does*
  fail the compliance ceiling and the 65 pN ceiling. `C-0025` confirms both and sharpens the second: at the
  desired stroke the restrained beam's 86.7 pN is past the **68.1 pN loading-rate-free saturation** of the
  shear allowable, so **no bonded length of any size** carries it (`CH-0029`).
- **`E3`'s two-sidedness, or `T-13`.** The flexure's law is odd at every restraint, evaluated at negative
  argument in `C-0025` as in `C-0023`, and the placement condition is met by construction at every joint. The
  zero-bias verdict — `k ≥ k_BT/σ² = 0.4602 pN/nm`, supplied 72.4× over, tetherless — is untouched.
- **`E5`, the crossover-hinge flexure.** `C-0023` names it as the fallback precisely because it accommodates
  its rotation in a hinge rather than in a span, and nothing here touches it.

## The remedy proposed

**Replace the sentence, not the bracket.** A joint has three stiffnesses and a dead band, not one, and the
free-to-draw-in column is reachable only by an **anisotropic** joint:

> The demand a free flexure places on its end joints is **0.88 nm** of in-plane draw-in, and it must be
> absorbed by a joint that is simultaneously **stiff across the beam**. A flexible link cannot do both. A
> **duplex standing normal to the sheet** can: it carries the end shear along its own axis (`S/ℓ`) and
> releases the draw-in by bending (`3EI/ℓ³`), an anisotropy of `S ℓ²/(3EI)` — **102× at 8 nm** — that the
> designer sets with a length.

`C-0025` delivers the resulting design: 8 nm (24 bp) standoffs, a 31.64 nm (93 bp) span at 45 paths, `c = 95.6`,
tangent **37.39 pN/nm** inside the 40 pN/nm ceiling, and 0.37 / 3.83 pN of beam tension at §3's 3 and 10 nm
strokes — both under the 10 pN unzip allowable.

`C-0023` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
