# P-3 — PEG material parameter sheet

| | |
|---|---|
| **Task** | `P-3` (process blocker, raised by us) |
| **Leaf** | none — this is a premise task under `A2.1`, not one of NDI's leaves |
| **Verification type** | logical + in-silico, closed against published measurement |
| **Blocks** | `T-2` (design window), `T-1b`, `P-4` (volume-fraction bookkeeping), `P-5` (brush criterion) |
| **Raised by** | `C-0001`, whose four flagged `cited`-not-derived numbers all live here |

---

## Formulate

### The question

`C-0001` produced a stiffness for the grafted layer, and flagged four numbers it had to inherit:

1. `a = 0.35 nm`, the PEG monomer size — cited, and noted to imply a monomer volume ~35% below PEG's bulk-density value;
2. the 10–16 nm height range for dense PEG 5 kDa brushes — cited and not traced to a source;
3. `χ ≈ 0.45` for PEG/water — cited, used only in a sizing remark;
4. the SCF excluded volume `w = π²a³/4`, which is a height-matching calibration and not a property of PEG.

It also left the osmotic exponent `m` undecided, carrying `9/4`, `2` and `3` side by side,
and rested its semidilute justification on a **conventional** crossover value `φ ≈ 0.2–0.3`
that was never checked against PEG in water.

The question this task must answer is therefore not "what is `a`" but:

> **For PEG in water at 300 K, at the volume fraction and chain length the surviving design window actually sits at,
> which osmotic pressure law applies, with what prefactor, and how far is the layer from the boundary of that law's validity?**

Everything else in the sheet — monomer volume, contour length, Kuhn length, excluded volume, χ, salt —
is instrumental to that.

### Numeric targets and acceptance predicate

**Acceptance (falsifiable):**

- **(a)** Every number in the sheet carries a provenance flag from
  `DERIVED` / `MEASURED` / `CITED` / `CONVENTION` / `BOUNDED`, and a source.
  A number flagged `DERIVED` must be recomputed in code from more primitive inputs, not transcribed.
- **(b)** The monomer size `a = 0.35 nm` is either **derived** from molecular geometry
  and **independently corroborated** by a published fit of the same functional form we use,
  or it is replaced. Agreement is accepted at ≤ 10%.
- **(c)** The osmotic pressure law for PEG/water is stated as a *measured* equation of state
  with a fitted prefactor, valid across the dilute→semidilute crossover,
  and is shown to reproduce an **independent** published fit of the same material to ≤ 10%.
- **(d)** The crossover volume fraction `φ#` separating van't Hoff from des Cloizeaux behaviour
  is computed for **our** chain length, and the surviving `T-1` design window is placed relative to it
  with a stated ratio `φ/φ#`. This is the answer `P-4` is waiting for.
- **(e)** The effective osmotic exponent `m_eff = d lnΠ/d lnφ` at our operating point is reported
  as a number with a bracket, replacing the three-way `{9/4, 2, 3}` spread `C-0001` carried.
- **(f)** The chain-tension premise §2 of the problem definition raises
  ("chain tension degrades solvent quality above ~30 pN per chain")
  is checked against the actual per-chain tension at the design point, and discharged or not.
- **(g)** Anything that **cannot** be pinned with the available evidence is named as such
  and left open, rather than given a plausible number. Per §7 of the problem definition.

**Falsification of the task itself:** if the published PEG/water equation of state cannot be reproduced
from its stated parameters, or if two independent fits disagree by more than 10%, the sheet is not usable
and `T-2` must proceed on the three-model spread instead, saying so.

### Units, temperature, medium

Locked, per `Physics.kt`: lengths nm, forces pN, energies pN·nm, pressures pN/nm² (= MPa exactly),
`k_BT = 4.142 pN·nm` at **300 K**, aqueous.

**Stated deviation:** the equation of state we adopt was fitted to data at **20 °C (293 K)**,
and the brush fit it is cross-checked against is at room temperature.
We evaluate at 300 K throughout, which is a ≤ 2.4% shift in the `k_BT` prefactor
and is smaller than the fit uncertainty. It is a deviation, so it is stated here and travels with the claim.

**Medium:** the equation of state was fitted to PEG in **pure water**, not in 2–10 mM MgCl₂.
That is a premise gap, not a detail; it is discharged by bound or left open, per (g).

### Conventions fixed before deriving

The single most dangerous thing in this task is that **`a` is three different quantities in the literature**,
all written with the same letter:

| symbol | meaning | where it appears |
|---|---|---|
| `a_ADG` | *effective monomer length* of the Alexander-de Gennes one-parameter picture | `L₀ = N a^(5/3) σ^(1/3)`, `φ̄₀ = (a/D)^(4/3)`, `Π = α(k_BT/a³)φ^(9/4)` |
| `v₀^(1/3)` | *volumetric* monomer size, from the monomer's actual volume | the physical volume fraction `φ = N σ v₀ / h` |
| `b` | *Kuhn length*, the statistical segment | Gaussian elasticity, `R² = N_K b²` |

They are **not** interchangeable and the ratios between them are not 1.
Convention adopted here and enforced downstream:

- `φ` **always** means the **physical** polymer volume fraction, computed with the monomer volume `v₀`.
  Where a source defines `φ` as a reduced density `n a³`, we convert on the way in and say so.
- `a_ADG` is used **only** inside Alexander-de Gennes expressions, never as a volume.
- Any prefactor fitted against a particular `φ` convention travels with that convention.

---

## Plan

### Method, and why this one

**Cheap bound first, per §5 and §7.** The whole task is arithmetic on published measurements
plus a closed-form equation of state — there is no simulation here, and there should not be.
An MD or SCF calculation of PEG's excluded volume would cost days and would be *less* trustworthy
than osmometry on the actual polymer in the actual solvent, which already exists and spans our concentration range.
The expensive method is not merely unnecessary, it is worse. That is the justification.

Order of work, cheapest first:

1. **Molecular arithmetic** — monomer molar mass from atomic masses, monomer volume from the
   partial specific volume, all-trans contour length from bond geometry. Pure derivation, no fit.
   Validated on polyethylene, whose crystallographic repeat is known independently.
2. **Adopt the measured equation of state** — Cohen, Podgornik, Hansen & Parsegian (2009),
   a one-parameter non-virial interpolation between van't Hoff and des Cloizeaux limbs,
   fitted to Rand's osmometry on **12 PEG molecular weights** in water, `r² = 0.9926`.
   Implement it, and re-derive its crossover concentration numerically instead of quoting it.
3. **Cross-check against a second, independent fit** — Hansen, Cohen, Podgornik & Parsegian (2003),
   which fits the des Cloizeaux prefactor with a *different* `φ` convention and a *different* dataset reduction,
   and additionally fits `a_ADG` to PEG-brush compression. If the two disagree by > 10%, acceptance (c) fails.
4. **Place our layer** — evaluate `φ`, `φ#`, `φ/φ#` and `m_eff` at the `T-1` design points,
   both unperturbed and at the working height under the §3 target force.
5. **Discharge or open** the remaining premises: chain tension, Mg²⁺, χ.

### What would falsify this approach

Stated in advance:

- **The equation of state does not reproduce the independent fit.** If step 3 disagrees by more than 10%,
  the measured prefactor is not established and the whole sheet degrades to the inherited spread.
- **Our layer sits in the des Cloizeaux domain after all** (`φ ≥ 5φ#`). Then `C-0001` was right for the
  right reason, this task confirms rather than corrects it, and `m = 9/4` stands.
- **Our layer sits in the van't Hoff domain** (`φ ≤ 0.2φ#`). Then the brush picture collapses entirely —
  no semidilute solution, no blobs, no Alexander-de Gennes — and `T-1` must be withdrawn rather than adjusted.
- **The per-chain tension exceeds ~30 pN.** Then solvent quality is load-dependent, the equation of state
  is being applied outside the state it was measured in, and the layer's stiffness becomes a function of its own history.

The middle case — the layer *inside* the crossover — is the one this task expects,
and it is the one that requires the most care, because neither limiting law applies
and quoting either one is the error.

### Cost

Minutes of compute. The expense is in the reading, which is where it belongs for a premise task.
