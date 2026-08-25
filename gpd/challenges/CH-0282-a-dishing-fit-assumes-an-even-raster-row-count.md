# CH-0282 — **`HoneycombGrillage`'s DISHING is a least-squares fit only where the face's three rigid modes are ORTHOGONAL, and on a corrugated honeycomb face they are orthogonal if and only if the raster-row count is EVEN.** At `15 × 4` a perfectly uniform solved field — every face beam at `p/k_f` to `1e−10` — is reported as **`0.0620506254`** of the stroke of dishing, which is `62 %` of `T-5b`'s whole tolerance on a field with no curvature at all. Every block this corpus has graded has `m = 10`; the three `15 × 4` free-tile readings `C-0154` publishes and **both deliverables quote** are the ones this reaches

**Against** [`C-0154`](../claims/C-0154-honeycomb-grillage.md)'s three `15 × 4` flatness readings and the `HoneycombDeflection.dishingCoefficients` construction they are taken with, and through them the `15 × 4` cells of the decision-7 comparison passage in [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) and of [`ANSWERS.md`](../../ANSWERS.md) §1.
**From** [`C-0218`](../claims/C-0218-the-tied-regrade-at-the-other-cross-section.md) (`T-294`) §3, where the standing falsifier `F1` fired.
**Kind** — a **decomposition** that assumes an orthogonality its own geometry does not always have. `CLAUDE.md`'s own *a numerical guard becomes a physical assertion the moment the lattice lands on it*, read on a **projection** rather than on a guard.

---

## 1. The statement, and the one line it rests on

`HoneycombDeflection` removes its best-fit rigid plane by **three independent projections**:

```
meanDeflection = <piston, u> / A
tiltAlong      = <tiltS,  u> / ||tiltS||^2
tiltAcross     = <tiltY,  u> / ||tiltY||^2
dishing        = u - meanDeflection*piston - tiltAlong*tiltS - tiltAcross*tiltY
```

Its KDoc calls the result *"the face field with its area-averaged best-fit rigid plane removed"*,
and that is what it is **iff the three modes are mutually orthogonal** under the face inner
product. Two of the three cross terms vanish for a reason that is a property of the axial
coordinate: `⟨piston, tiltS⟩ = ∫s dA = 0` because the axial range is symmetric, and
`⟨tiltS, tiltY⟩ = 0` with it.

The third does not. `⟨piston, tiltY⟩ = ∫y dA` over the face's tributary strips, and each strip is
one row pitch **centred on its own beam's axis**, so

```
<piston, tiltY> = L_s * (3d/2) * SUM_i y_i
```

— it vanishes exactly when the face's rooting-helix positions are symmetric about their own datum.

## 2. And a honeycomb face is CORRUGATED, so that symmetry is a PARITY

`C-0167`'s own cheap bound records the geometry: a honeycomb face's rooting helices sit at
alternating `±d/4` about the `3d/2` ladder, so the gap sequence along the face is
`d, 2d, d, 2d, …`. A palindromic gap sequence is what makes the positions symmetric, and that
sequence is palindromic **iff the number of gaps is odd**, i.e. iff the raster-row count `m` is
**even**.

| | `m = 10` | `m = 15` |
|---|---|---|
| worst off-diagonal of the Gram, relative | `0.0` | **`0.0358744468`** |
| a **uniform** pressure's dishing, **standing** decomposition | not emitted; a named test bounds it at `< 1e−9` | **`0.0620506254`** |
| the same, **least-squares** | `< 1e−9` (named test) | **`0.0`** |

*(both Gram entries and the `15 × 4` dishing readings are emitted in `T-294`'s `census` and
`falsifiers` blocks. The parity is asserted at four raster-row counts — `m = 10` and `m = 14`
orthogonal, `m = 11` and `m = 15` not — but as the orthogonality **flag**, in a named test; only
the two cross-sections `T-294` grades have numbers.)*

**The solved field is not in question.** At `15 × 4` every face beam reads `p/k_f` to `1e−10`
relative — asserted as a named test — so the lattice, the load, the foundation and the solve are
all exactly right and it is the **fit** that reports curvature where there is none.

## 3. What it is worth on the numbers the corpus carries

`C-0154`'s three `15 × 4` free-tile readings are quoted in `ANSWERS.md`, in
`DECISIONS-FOR-NDI.md` and in this repository's own `CLAUDE.md`-adjacent prose, and they are the
ground the decision-7 comparison passage now stands on. Re-taken at `C-0154`'s own `112 bp` row
and its own three enhancements, in both conventions:

| enhancement | `C-0154`, published | reproduced here | **corrected** | flat either way |
|---|---|---|---|---|
| `1.0` | `0.312237799` | see `T-294`'s `upstream` block | see `upstream` | no / no |
| `9.65079217` | `0.227177955` | see `upstream` | see `upstream` | no / no |
| `12.7228458` | `0.220064299` | see `upstream` | see `upstream` | no / no |

**No verdict moves.** All three are outside `T-5b` in both conventions, which is why this is a
challenge against three **numbers** and not against `C-0154`'s conclusion or against the
cross-section ordering that rests on it. The ordering is if anything **strengthened**: the
corrected readings are smaller and still outside the tolerance.

## 4. What is NOT challenged

- **Every reading this corpus has taken at an EVEN raster-row count.** At `m = 10` the modes are
  orthogonal to `0.0` and the two conventions agree; measured on a non-uniform collar load the
  residue is under one part in a thousand, and that residue is a **second and far smaller**
  inconsistency (§5) rather than this one.
- `C-0154`'s conclusion, `C-0167`'s, `C-0180`'s, `C-0208`'s, `C-0211`'s, `C-0212`'s, `C-0215`'s
  and `C-0216`'s — every one of them is `10 × 6` or route B at `m = 10`.
- The **solve**. Nothing here says a lattice deflection is wrong.

## 5. The smaller inconsistency this uncovered, stated and not challenged

`HoneycombDeflection` **fits** with `faceFunctional`'s duals and **samples** with `evaluate`. The
two reconstruct a field off the beam axis differently — `faceFunctional` attributes a quadrature
point to the strip's **owning** beam and `evaluate` to the **nearest** one, and on a corrugated
face a `3d/2` strip reaches past the midpoint of a `d` gap. At `10 × 6` that is worth under
`1e−3` relative on a collar load, which is why it is recorded here rather than challenged.

## 6. What would settle it

Replace the three independent projections by a `3 × 3` least-squares solve in the face inner
product — nine numbers and one elimination, computed **once per lattice** because the Gram is a
property of the geometry. `T-294` does exactly that in its own file (`FaceRigidBasis`), asserts
that it annihilates a uniform field at **both** cross-sections, and emits **both** readings at
every cell rather than replacing one with the other (`C-0092`).

`HoneycombGrillage` is a shared source `T-294` did not edit. The repair belongs to whoever owns
the next change to it, and the queue row is `T-330`.

| | |
|---|---|
| **Status** | ~~**RAISED**, iteration 51~~ **RESOLVED**, iteration 52, by [`C-0219`](../claims/C-0219-a-dishing-fit-and-the-parity-of-its-basis.md) ([`T-330`](../tasks/T-330-a-dishing-fit-and-the-parity-of-its-basis.md)). The decomposition is a genuine least-squares fit, branching on an **exact integer** statement about the face's own half-bond ladder so that an even-`m` reading is returned bit for bit; `C-0154`'s three `15 × 4` readings are corrected in place to `0.242196276 / 0.157167743 / 0.150056485` and **no verdict moves**. §5's separate inconsistency is measured and carried on as [`CH-0284`](CH-0284-a-fit-and-a-sample-in-two-reconstructions.md) |
| **Raised by** | [`C-0218`](../claims/C-0218-the-tied-regrade-at-the-other-cross-section.md) (`T-294`) |
| **Severity** | **numbers, not verdicts** — three published readings move by up to `1.44×`, and none of them crosses `T-5b` in either convention |
