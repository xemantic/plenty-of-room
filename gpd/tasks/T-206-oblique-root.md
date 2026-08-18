# T-206 — what does an OBLIQUE attachment root cost against a perpendicular one?

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0122`](../claims/C-0122-honeycomb-station-lattice.md) §5, which censused the honeycomb's attachment lattice and could not price it |
| **Claim** | `C-0128` |
| **Challenges reserved** | `CH-0151`, `CH-0152` |
| **Result** | `gpd/results/T-206-oblique-root.json` |
| **Verification type** | **logical** (a closed-form decomposition on the corpus's own joint constants) **+ in-silico** (the re-grading of `C-0118`'s cells under the alternation the lattice imposes) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **NOT empirically demonstrated.** |

---

## 1. Formulate

### The question

A honeycomb helix has **three** crossover azimuths, 120° apart.
caDNAno alternates the helix orientation between the lattice's two sublattices,
so along a slab's **top face** the free direction alternates:
half the helices carry one azimuth pointing **straight out** of the slab,
and half carry **two oblique** ones.
`C-0122` counted them — 8 perpendicular / 7 oblique on `15 × 4`, 5 / 5 on `10 × 6` —
and treated every station as equivalent, because the *count* is unaffected.

**This corpus has never priced an oblique root.**
`C-0118`'s flat coupled cells — the first in this programme flat at the 90th percentile under
the measured folding statistics — stand on stations roughly half of which are not perpendicular.

### Geometry and sign conventions, fixed before deriving

- Helices run along `x`. The slab's outward normal is `+z`. The cross-section is the `(y, z)` plane.
- A root's **azimuth** `ψ` is the angle between its lattice direction and `+z`, in the `(y, z)` plane.
- Sublattice A carries azimuths `{0°, 120°, 240°}`; sublattice B carries those rotated by
  `azimuthSeparation/2 = 60°`, i.e. `{60°, 180°, 300°}`.
  So on the top face the free azimuths are `ψ = 0°` (A, one of them) and `ψ = ±60°` (B, two of them).
  **`ψ_oblique = 60°` exactly**, and it is derived from the lattice rather than asserted.
- The load a coupling path carries is **normal**, along `z`.
- A root has a **radial** axis (along its own azimuth — the direction a crossover's covalent link acts in)
  and a **tangential** axis (perpendicular to it in the `(y, z)` plane — the direction the crossover's
  dihedral hinge rotates the attached body in).

### Locked units

SI. Lengths nm, forces pN, stiffness pN/nm, rotational stiffness pN·nm/rad, `k_BT` = 4.142 pN·nm at 300 K,
aqueous buffer with stated Mg²⁺. Dimensionless ratios carried as ratios.

### Numeric target and acceptance predicates

- **`P1`** — the cost of an oblique root against a perpendicular one is delivered as a **number with the
  root model it was read on**, or the branch is declared `NOT REPRESENTABLE` with what it would take.
- **`P2`** — the limiting cases are named and asserted: `ψ = 0°` reproduces the perpendicular root
  **exactly**, and `ψ = 90°` is the **pure in-plane** case (the crossover's dihedral hinge alone).
- **`P3`** — the consequence for `C-0118`'s flat cells is stated: whether they survive the alternation
  the lattice imposes, graded at the 90th percentile under `C-0087`'s measured dropout.
- **`P4`** — the cheap bound is stated **before** any solve, and what it settles is recorded.

### Falsifiers

- **`F1`** — if the cost factor is **not** monotone in the root's own radial-to-tangential anisotropy,
  the closed form is the wrong decomposition and a solve is needed.
- **`F2`** — if the oblique root's **absolute** normal stiffness depends strongly on the covalent
  link's stiffness — which this corpus holds to be a *constraint*, i.e. a binary — then the branch
  that matters is unbounded and only a bracket can be quoted.

---

## 2. Plan

### The cheap bound, stated before any solve

A root whose radial axis lies at `ψ` from the surface normal, loaded along the normal, has

&nbsp;&nbsp;&nbsp;&nbsp;`1/k_z(ψ) = cos²ψ / k_radial + sin²ψ / k_tangential`,

because the root's stiffness is a symmetric tensor diagonal in its own two axes and `k_z` is the
inverse of the normal-normal entry of its **compliance**. Therefore

&nbsp;&nbsp;&nbsp;&nbsp;**`κ(ψ) ≡ k_z(0) / k_z(ψ) = cos²ψ + sin²ψ · A`,&nbsp;&nbsp; `A ≡ k_radial / k_tangential`.**

Three things follow with no computation at all:

1. **The whole question is ONE anisotropy.** Everything about the member, the ground and the
   material enters only through `A`.
2. **`κ ≥ 1` whenever `A ≥ 1`**, with equality **iff `A = 1`** — so an oblique root can never be
   *stiffer*, and it is free exactly when the root is **isotropic**.
3. At the honeycomb's own `ψ = 60°`, `κ = 0.25 + 0.75 A` — so **three quarters of the load path is
   the tangential axis**, whatever the radial one is.

That is the bound this task runs first, and it decides where the effort goes: measure `A` for each
root model the corpus already has, and do not solve anything until one of them is ambiguous.

### Method

1. Derive `ψ_oblique` from `HoneycombLattice` rather than asserting 60°.
2. Read `A` off each root model the corpus supplies, and report all of them:
   - **R1, the flexible tie** — a strand rooted at the station. `FlexureEndJoint`'s own invariant
     (*`k_⊥/k_axial ≡ 1` for any isotropic element, and for any covalent tie on a softened bond*)
     gives `A = 1` **exactly**, so `κ = 1` at **every** azimuth.
   - **R2, the crossover-hinged rigid body** — radial is the crossover's link, `2 k_bond,s`;
     tangential is `C-0009`'s dihedral spring on the frame-indifferent lever `d/2`, `k_θ/(d/2)²`.
   - **R3, the link as a CONSTRAINT** — `CLAUDE.md`'s own reading (*a covalent tie is a BINARY, and
     asking how stiff it is is asking the wrong question*). Then `A = ∞`, `κ = ∞`, and the **ratio**
     is not representable — but the oblique root's **absolute** stiffness still is.
3. Price the consequence for a coupling **path**, which is the root in series with whatever supplies
   the compliance `C-0017`'s mandate demands, at each of `C-0118`'s path counts.
4. Re-grade `C-0118`'s `10 × 6` cells with the **alternation** the lattice imposes, at the mandated
   total, under `C-0087`'s measured dropout, at the 90th percentile.
5. Check the model boundaries explicitly: `C-0037`'s `TwoLinkBase` refuses a misalignment past 45°,
   and `ψ = 60°` is past it — say whether that guard applies to this angle or to a different one.

### Justification against cost

The decomposition is closed form and the constants are already in the tree, so steps 1–3 cost no
solve at all. Step 4 reuses `C-0118`'s own influence surrogate and dropout ensemble, which is a
bank problem rather than a solve problem — the tile is factorised once per cell.

### What would falsify this approach

If the root's two axes are **not** the eigenvectors of its stiffness tensor — if, say, the covalent
links' geometry couples radial and tangential — then `κ` is not `cos² + A sin²` and a lattice solve
is required. The test is that the tensor built from the two axes must be diagonal in them, which is
asserted rather than assumed.
