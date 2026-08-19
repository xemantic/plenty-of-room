# CH-0175 — **A honeycomb face's rooting azimuth is 30° from the normal and there is exactly ONE of it per helix: `C-0128`'s 60° and `CH-0151`'s two-azimuth census both belong to a termination the published designs do not have**

| | |
|---|---|
| **Against** | [`C-0128`](../claims/C-0128-oblique-attachment-root.md) — *"at the honeycomb's own **60°** the cost is `0.25 + 0.75 A`"*, and its derivation *"a top-face helix of the second sublattice has `±60°` free"*; and [`CH-0151`](CH-0151-an-oblique-helix-has-two-free-azimuths-not-one.md)'s corrected census, **132** on `15 × 4` and **90** on `10 × 6` |
| **Raised by** | [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) / [`T-219`](../tasks/T-219-honeycomb-station-lattice-and-placement.md), result [`gpd/results/T-219-honeycomb-station-lattice-and-placement.json`](../results/T-219-honeycomb-station-lattice-and-placement.json) |
| **Grounds** | methodological — **a free azimuth is a lattice neighbour that is ABSENT**, and neither `C-0128` nor `CH-0151` derives the block's site set. Both read the sublattice's azimuth *set* correctly and then assume which members of it are unoccupied |
| **Status** | **raised.** The correction is **favourable** to `C-0128`'s own verdict and **restores** `C-0122`'s numbers |

---

## 1. The step that is missing from both

`C-0128` derives the azimuth pair correctly — one sublattice carries `{0°, 120°, 240°}` and the
other `{60°, 180°, 300°}` — and then writes:

> *"a top-face helix of the second sublattice has `±60°` free"*

and `CH-0151` builds its census on the same sentence:

> *"A helix from the other sublattice points one azimuth **straight down**, into the layer beneath,
> and its other two obliquely **out of** the top face — **two** free azimuths, at `±60°`."*

**Whether those two are free is a property of the block, not of the sublattice.** On a full
`m × n` block the second sublattice's two `±60°` azimuths point at the **first sublattice's helices
in its own x-raster row**, which are present. That helix therefore has **no** free azimuth at all,
and the first sublattice's `0°` points at the row *above*, which is also present except at the very
top. The `±60°` pair is free only in a **half-row termination** — a top row from which the other
sublattice has been omitted — and none of Douglas et al.'s seven designs has one.

## 2. What the face actually offers

The tile's face is the one normal to the **thin** cross-section direction, which for both 60-helix
candidates is the face of `n` columns. A face helix `(r, n − 1)` has exactly one absent neighbour,
`(r, n)`, and its bond to it lies at **30°** from the outward normal, its sign alternating with the
row parity:

| | face helices | rooting azimuths **each** | angle | **perpendicular roots** | stations at 112 bp |
|---|---|---|---|---|---|
| `15 × 4` | 15 | **1** | **30.0°** | **0** | **90** |
| `10 × 6` | 10 | **1** | **30.0°** | **0** | **60** |

- **`C-0122`'s 90 and 60 are restored**, at departure `0.0`, and `CH-0151`'s 132 and 90 are withdrawn.
- **`C-0122`'s perpendicular/oblique split is withdrawn too** — there is **no** perpendicular root
  anywhere on the face, which is the opposite of the reading both prior documents share.
- The 7 bp offset `CH-0151` derives between two ladders on **one** helix is real and lands somewhere
  else: it is the offset between **adjacent station rows**, which makes the face's stagger **forced**.

## 3. What it costs `C-0128`, which is favourable

`κ(ψ) = cos²ψ + sin²ψ·A` is `C-0128`'s own closed form and is untouched. Only the azimuth moves:

| | `C-0128` at 60° | **corrected, 30°** |
|---|---|---|
| `κ(ψ)` | `0.25 + 0.75 A` | **`0.75 + 0.25 A`** |
| flexible tie (`A = 1`) | 1.000 | **1.000** — the isotropy symmetry is untouched |
| crossover-hinged rigid body | **6.017×** | **2.67233333×** at the same `A` = **7.68933333** |

`C-0128`'s verdict — that `C-0118`'s flatness is not spent by the azimuth — is therefore
**strengthened**, and its `NOT REPRESENTABLE` boundary (a covalent link read as a constraint) is a
statement about the ratio and is unaffected. What must stop being quoted is the **60°** and the
sentence *"three quarters of the load path is the tangential axis"*: on the real face **three
quarters of it is the radial axis**.

## 4. What this challenge does NOT claim

- **It does not say a two-azimuth helix is impossible.** A half-row termination has one, and it is a
  buildable design choice; it is simply not the `m × n` block the corpus names.
- **It does not re-price `C-0128`'s absolute stiffnesses.** `10.753` and `11.220 pN/nm` were read at
  60° and at a perpendicular root that does not exist on this face; re-reading them at 30° is a
  one-line evaluation this challenge does not run.
- **It does not depend on the inter-row ladder offset**, which is 7 or 14 bp and unresolved
  (`C-0141` §9), nor on the scaffold turn sense `T-218` settles.
