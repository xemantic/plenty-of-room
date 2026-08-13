# CH-0027 — The hold-down requirement is a **force** only because the stack is one-sided; make the coupling two-sided and it is a **stiffness**, which §3 already mandates 72× over

| | |
|---|---|
| **Against** | [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) — *"The scale everything is judged against"* (`F ≥ k_BT/3.0 nm = 1.380649 pN`, the **declared acceptance**), and the sentence **"A coupling can decide where the tile sits; it cannot be the thing that holds it there."** |
| **Raised by** | [`C-0023`](../claims/C-0023-two-sided-coupling.md) (`T-23`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — an acceptance *currency* derived from a property of the stack that was being evaluated, and then applied as a property of the problem |
| **Direction** | **favourable to `C-0021`'s programme, and it removes a part.** The requirement it makes binding is discharged by the coupling §3 already mandates, with no preload, and `C-0014`'s eight substrate tethers leave the design |
| **Status** | raised. **No number, table or verdict in `C-0021` moves.** Every one of its 144 solved states is a one-sided stack; its two device rows are reproduced here on its own quadrature domain (1.391 against 1.40 `k_BT`, 5.371 against 5.37, 18/18 and 0/18 confining) |

---

## What is challenged

`C-0021` derives its acceptance scale, and the derivation is explicit and correct as far as it goes:

> Above `L₀` the coordinate is not harmonic. The layer contributes nothing at all there, so a hold-down of
> magnitude `F` confines the tile through a **linear** potential and the upward excursion is exponentially
> distributed: `⟨h − L₀⟩ = k_BT/F` exactly … **`F ≥ k_BT/3.0 nm = 1.380649 pN`** (DECLARED ACCEPTANCE).

and concludes, from `M2`:

> **A coupling can decide where the tile sits; it cannot be the thing that holds it there.**

The arithmetic is right. **The quantifier is not.** "The layer contributes nothing above `L₀`" is a statement
about the *layer*; "so the potential is linear" additionally requires that **nothing else** contributes there —
and that is a property of the particular coupling `C-0017` committed to, not of the problem.

---

## The methodological ground

`C-0021` writes, one section earlier and correctly:

> **`holdDownForceScale(σ)/σ = k_BT/σ² = 0.460216 pN/nm` identically** … *The force requirement and the
> stiffness requirement are the same statement one power of the bound apart, and which one applies is decided
> by whether the confining potential is linear or quadratic.*

**Whether the confining potential is linear or quadratic is decided by the coupling's topology, and the
topology is a design variable.** A coupling that carries load in both directions contributes `−k(h − h₀)` above
`L₀` as well as below it, so the potential there is quadratic, the excursion Gaussian, and the requirement is
the *stiffness*:

| | requirement | what §3's own mandated coupling supplies | margin |
|---|---|---|---|
| **one-sided stack** (`C-0021`'s, and correct for it) | `F ≥ k_BT/σ` = **1.380649 pN** | **exactly 0** — `K2`'s `R(0) = 0` | — |
| **two-sided coupling** | `k ≥ k_BT/σ²` = **0.460216 pN/nm** | **33.3333 pN/nm** | **72.43×** |

The same coupling that is *4 % short* of the force requirement — and that `C-0021` prices at 3 pN of preload
per pN/nm of excess stiffness — is **72× past** the stiffness one, unpreloaded.

`C-0023` checks this rather than arguing it, on `C-0021`'s **own** balance, its own layer models, its own van
der Waals assembly, its own residual field and its own Boltzmann quadrature domain rule, changing only the
coupling:

| scenario | well [`k_BT`] | confining | RMS [nm] |
|---|---|---|---|
| `C-0021`'s device with the tether removed (one-sided `K2`, 33.3 pN/nm) | **1.4 – 5.4** | **0/18** | 2.56 – 12.98 |
| **the same stiffness made two-sided, no tether, no preload** | **959 – 7582** | **18/18** | **0.217 – 0.352** |

No extra part, no extra stiffness, no preload — the sign of the reaction at negative displacement, and nothing
else. Asserted as a gate-3 test in the shape that makes it a statement about the potential rather than about
one number: **the escape barrier grows as the square of the quadrature domain for a two-sided element and
linearly for a one-sided one.**

---

## Why `C-0021` could not have seen it, and said so

This challenge is the answer coming back to a question `C-0021` itself asked. Its *Still open* list, item 4:

> **A two-sided compliant DNA coupling.** If one exists — an antagonistic spacer pair, a bending hinge rather
> than a stretched chain — then `T-16`'s stiffness margin and `T-13`'s hold-down become one part instead of
> two, and the exact relation `F = (k_c − k_c*)δ*` prices it.

**Three exist** (`C-0023`: a transverse duplex flexure at a 24.61 nm span, a crossover-hinge flexure on a
4.11 nm arm, an antagonistic ssDNA pair). What `C-0021` did not anticipate is that finding one **removes the
requirement it priced** rather than paying it: the relation `F = (k_c − k_c*)δ*` remains exactly true and
becomes a *choice*, and the mounting offset that would deliver the thermal-scale preload is **0.0409 nm**, an
eighth of a base-pair rise — below what any assembly can set, while the smallest offset a design *can* build
delivers 12.78 pN, 9.3× the requirement.

---

## What this does *not* challenge

- **The force scale itself.** `k_BT/σ = 1.380649 pN` is exact and is reproduced here from `k_BT` alone. It is
  the right acceptance for a one-sided stack, and a one-sided stack is what `C-0021` was asked to evaluate.
- **`M2`'s exact zero.** `K2` supplies no preload of either sign; re-evaluated here through a signed interface,
  its reaction **and its tangent** are identically zero at every negative displacement.
- **Any of `C-0021`'s 144 solved states, its van der Waals bracket, its `M4` trap-not-confinement finding, its
  contact-potential thresholds or its bridging ceiling.** All are one-sided results and all stand; the two
  device rows are reproduced here to 6.4e−3 and 0.
- **That van der Waals is unavoidable.** It is what keeps `CH-0024`'s stroke shortfall non-zero even with a
  two-sided coupling (0.6–9.4 % against `C-0021`'s 2.4–12.7 %).

## The remedy proposed

Two sentences, not a recomputation:

1. **State the acceptance scale with its topology.** *"For a **one-sided** stack the requirement is a force,
   `k_BT/σ`; for a **two-sided** coupling it is a stiffness, `k_BT/σ²`. `F_req = k_req·σ` identically, so
   two-sidedness is worth exactly one power of the position bound."*
2. **Qualify the `M2` conclusion.** *"A **one-sided** coupling can decide where the tile sits and cannot be the
   thing that holds it there."*

`C-0021` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
