# CH-0039 — `C-0028`'s recommended base needs a lever arm 1.35× larger than the standoff's own backbone radius, and the couple goes as the square

| | |
|---|---|
| **Raised by** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md) (`T-67`) |
| **Against** | [`C-0028`](../claims/C-0028-standoff-base-joint.md) (`T-40`) |
| **Date** | 2026-08-13 |
| **Grounds** | **structural** — a counting theorem about strand termini, not a numerical disagreement |
| **Severity** | **high**: it removes the recommended base outright and moves the surviving window |

---

## The statement challenged

`C-0028`'s replacement design is

> **two antiparallel crossovers to the two adjacent sheet duplexes, the pair laid ACROSS the flexure axis** — `k_θ_base` = 261.2 pN·nm/rad,

built from `StandoffBase.crossovers(2, favourableOrientation = true)`, whose couple is

&nbsp;&nbsp;&nbsp;&nbsp;`2 k_s (d/2)²` with `d/2 = 1.345 nm`, the **half interhelical distance of the sheet**.

`C-0028` names its own open question 2 as *"whether a 90° scaffold or staple routing … exists at all"*
and flags it as *"upstream of every number in this claim"*.
`T-67` went and looked.

## The ground

**`d/2` is a distance in the SHEET. The two links have to start in the STANDOFF.**

A B-form duplex has two backbones,
so a duplex **end** presents exactly **two** strand termini,
at the two backbone positions of its terminal base pair.
Every covalent link grounding the standoff has to start at one of them.
Therefore:

- a base joint has **at most two links**, under any routing;
- their separation is the **terminal chord** `2 r_P sin(Δ/2)`, at most `2 r_P = 2.0 nm`;
- so the lever arm is at most **1.0 nm**, against `C-0028`'s **1.345 nm** — a factor of **1.345**;
- and a couple goes as the **square** of the lever arm, so `C-0028`'s couple is over the hard ceiling by **3.34×**.

The measured phosphate radius is `a_DNA ≈ 10 Å`
(Hedley, Coshic, Aksimentiev & Kornyshev, *Phys. Rev. X* **14**:031042, 2024, **read directly**),
which is the duplex's own radius —
B-form DNA's 2 nm diameter *is* the phosphate backbone.
So the bound is not a modelling choice; it is the material.

**No simulation can overturn a count.**
An atomistic or coarse-grained study of the junction could only find it *additionally* frustrated;
it cannot add a third backbone.

## What it costs

| | `C-0028` | `C-0029` |
|---|---|---|
| base rotational stiffness, best axis | **261.2 pN·nm/rad** | **78.24** (hard) / **62.06** (nominal 120° groove) |
| longest standoff its own `P6` threshold admits | 10 nm (`B2` meets the ladder everywhere) | **8.25 nm** (hard) / **7.50 nm** (nominal) |
| the window | `ℓ = 6–10 nm`, recommended 7–9 | `ℓ = 5–8 nm` on the restrained axis, and **empty** on the weak one |
| the second axis | *"`B2u` passes nowhere but one knife-edge"*, treated as an avoidable choice of orientation | **not a choice.** Two links lie on a chord; the couple has one axis and the orthogonal one keeps only `2 k_bond,θ` = 13.53 pN·nm/rad, which **is** `B1` |

`C-0028`'s `B3` — three crossovers, 977 pN·nm/rad — is over the ceiling by **12.5×** and falls with it.

## What does NOT move

Everything `C-0028` computes is reproduced here to ≤ 4.0e−4:
`B1` (13.529, 64.706), `B2` (261.168), `B2u` (27.059),
the `P6` threshold ladder (68.8 at 8 nm, 173.6 at 10 nm),
its `B2` design span (31.063 against 31.06) and tangent (36.508 against 36.51),
and `C-0025`'s `J5-8` clamped span and tangent to 4.7e−10.
**The model is right; the input is not available.**

`C-0028`'s three structural statements all stand and one is *strengthened*:
its *"the base's ORIENTATION is worth 9.65× and it decides the design"* becomes
**the orientation is not worth 9.65× — it is worth everything, because the unfavourable axis is not an alternative but a simultaneous fact.**

## How this challenge would itself fail

1. **A routing with more than two rigid links per standoff base.** An internal nick near the base adds termini, but they sit ≥ 1 bp above the base face and cannot reach the sheet without unpaired nucleotides — checked in code.
2. **A published measurement of a rigid perpendicular duplex-to-sheet junction above 78 pN·nm/rad.** `T-67`'s search found none, over ~110 queries; the negative is bounded by open-access indexing.
3. **Two standoffs per flexure end**, whose couple has the interhelical lever arm `C-0028` assumed. That is a **truss**, it is `T-66`, and it costs the sway the standoff exists to supply.
