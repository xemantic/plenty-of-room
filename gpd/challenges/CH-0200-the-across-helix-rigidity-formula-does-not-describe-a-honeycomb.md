# CH-0200 — **`D_⊥ = layers × k_θ d / p` is a statement about a stack of independent single-layer sheets, and a honeycomb block contains none.** Only half the in-plane adjacent pairs of a honeycomb are bonded at all, so one layer of a `10 × 6` block is **5 dimer components** and of a `15 × 4` block **8** — the across-helix load path *necessarily* traverses the thickness. Measured on a honeycomb lattice at the **same** `k_θ`, the **same** 21 bp per interface and `C-0141`'s **corrected** `3d/2` in-plane pitch, `D_⊥` is **`7/24 = 0.291666667`** of the formula on `10 × 6` and **0.288194444** on `15 × 4` — **`24/7 = 3.42857×`** overstated, in the **optimistic** direction for every flatness verdict built on it. **What makes this a challenge against one term and not against the construction is that the same formula reproduces `D_∥` EXACTLY**, at `2.8e−15`

| | |
|---|---|
| **Against** | `OrigamiSheet.acrossHelixRigidity` = `layers × interfaceHingeStiffness × interhelicalDistance` and `multiLayerRigidities`'s `acrossIndependent`, **as applied to a honeycomb block** — i.e. [`C-0109`](../claims/C-0109-four-layer-tile.md), [`C-0120`](../claims/C-0120-cross-section-comparison.md), [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) §7, [`C-0142`](../claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md), [`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) and [`C-0151`](../claims/C-0151-closing-raster-selection.md), every one of which grades a four-layer honeycomb cell through the smeared equivalent sheet |
| **Raised by** | [`C-0154`](../claims/C-0154-honeycomb-grillage.md) / [`T-253`](../tasks/T-253-honeycomb-grillage.md) §3, result [`gpd/results/T-253-honeycomb-grillage.json`](../results/T-253-honeycomb-grillage.json), sections `census` and `longWavelength` |
| **Grounds** | **logical + in-silico.** The component count is an exact union-find over the published neighbour rule; the rigidity is an imposed-field energy on a lattice built from the same rule, compared against the corpus's own function at the corpus's own parameters |
| **Kind** | **a scope correction, not an arithmetic error.** `OrigamiSheet`'s KDoc is explicit that the across-helix multi-layer coupling *"is deliberately **not** applied here"* and that the value is *"a lower bound for `layers > 1`"*. What is challenged is the **independent** term underneath that caveat — the `layers ×` factor itself — which no caveat in the tree qualifies |
| **Status** | **raised.** No published number is recomputed here. `C-0141`'s `10 × 6` honeycomb free-tile dishing of `0.0240648102` becomes **`0.0449400126`** on the lattice at the same calibrated coupling — **1.87×** — and the cell is **still flat**, so the verdict survives and the margin does not |

---

## 1. The census, which is the whole argument

A honeycomb site `(r, c)` bonds to `(r ± 1, c)` — up if `r + c` is even, down if it is odd — and to
`(r, c ± 1)` always. So within one layer (fixed `c`) the bond `(r, c) → (r+1, c)` exists for
**exactly half** the values of `r`:

| | helices | face helices | in-plane interfaces | interlayer interfaces | **components of ONE layer** |
|---|---|---|---|---|---|
| `10 × 6` | 60 | 10 | 27 | 50 | **5** |
| `15 × 4` | 60 | 15 | 28 | 45 | **8** |

They are **dimers**, and `honeycombBondGraphComponents(HoneycombBlock(m, 1)) == ⌈m/2⌉` is asserted
at every `m` from 2 to 12. A stack of `layers` single-layer sheets is therefore not a decomposition
a honeycomb block admits, and `layers × (one sheet's `D_⊥`)` is a sum over bodies that do not exist.

## 2. What the lattice measures

Imposing `W = ½κy²` on the honeycomb lattice makes every beam's bending and torsion vanish, every
normal link's extension vanish identically, and the whole energy sit in the dihedral springs — so
`D_⊥` is read exactly, with no solve:

| | measured | `multiLayerRigidities` at `d` = 2.536 | at `C-0141`'s `3d/2` = 3.804 | **measured / corrected** |
|---|---|---|---|---|
| `10 × 6` | **12.6141869** | 28.8324271 | 43.2486406 | **0.291666667** |
| `15 × 4` | **8.30934531** | 19.2216181 | 28.8324271 | **0.288194444** |

Two facts account for all of it, and neither is expressible in a smeared sheet: an **in-plane** bond
carries a full `d` of lever arm across the row while an **interlayer** bond carries only `d/2`
(its direction is 30° off the in-plane axis), and **only half the in-plane pairs are bonded**.

## 3. Why this is not a challenge to the construction

On the **same** lattice, the **same** imposed-field method and the **same** function, `D_∥` at rigid
composite action reproduces `multiLayerRigidities` at `C-0141`'s corrected pitch to **`2.8e−15`** —
24771.776 pN·nm at `10 × 6` and 7215.85068 at `15 × 4`, both exact. The parallel-axis half of the
corpus's four-layer model is therefore **verified** by this task, and the challenge is confined to
the across-helix independent term.

It is also worth naming what is *not* claimed: the lattice carries no across-helix **parallel-axis**
term at all (it needs an in-plane transverse coordinate the model does not have), so its `D_⊥` is
the independent one and the comparison above is like for like only because the corpus's
`acrossIndependent` is too.

## 4. What it moves, and what would settle it

- The direction is **optimistic**: a `D_⊥` `3.43×` too stiff dishes less. Measured end to end at the
  design point, `C-0141`'s `10 × 6` honeycomb free-tile dishing goes `0.0240648102 → 0.0449400126`,
  **1.87×**, and remains inside `T-5b`'s 0.10. **That reading carries `C-0141`'s own calibrated
  across-helix enhancement of 21.1851817 unchanged** and only replaces the smeared sheet by the
  lattice; run self-consistently at the fraction the lattice itself measures (`CH-0201`) it is
  `0.0477844467`, **1.99×**, and still flat. **`C-0120`'s central finding — that the second
  cross-section removes the dependency on the interlayer-coupling calibration — is untouched.**
- `C-0142`, `C-0146` and `C-0151`'s **coupled** cells are graded through the same smeared sheet and
  are not re-run here; whether any of them crosses 0.10 at the corrected `D_⊥` is the open half.
- **What would settle it** is re-grading those cells on `HoneycombGrillage`, which now exists and
  costs half a minute per cross-section. That is queued rather than done, because a coupled grading
  needs `C-0142`'s influence surrogate ported onto the new lattice.
