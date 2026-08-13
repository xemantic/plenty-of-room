# CH-0029 — The 48 pN per-load-path allowable is a *30 base-pair* number, and the entry topology is exactly what fixes the length

| | |
|---|---|
| **Against** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md)'s per-path allowable table **as consumed** by [`C-0009`](../claims/C-0009-discrete-lattice-tile.md), [`C-0014`](../claims/C-0014-lateral-confinement.md) and [`C-0020`](../claims/C-0020-in-plane-shear-lag.md) — specifically `C-0020`'s `A_eff = 48.00 pN`, its `L_min` table and the 54.9 pN preload that follows from it |
| **Raised by** | [`C-0024`](../claims/C-0024-attachment-entry-topology.md) (`T-19`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a **length-dependent measured quantity used as a length-free material constant**, at a length no claim in the programme has ever specified |
| **Direction** | **unfavourable for a realistic staple extension** (a 16 bp bond gives 34.8 pN, an 8 bp bond 18.8 pN, against the 48.0 assumed), **favourable if the joint is designed at 30 bp or more**, and the remedy is a *build rule* rather than a new number |
| **Status** | raised. **No verdict of `C-0006`, `C-0009`, `C-0014` or `C-0020` moves** — the correction is 1.35× on one path class and is dominated by the ×2 the same claim's own entry-topology result buys back. What moves is that `A_eff` acquires a **sequence-design argument** |

---

## What is challenged

`C-0006` traces the per-load-path shear allowable to its primary source and records it as

> | hybridised staple domain, **shear** | 48 ± 2 pN (30 bp) … 65 pN | ~50 nm/s … 2697 pN/s | Strunz et al. *PNAS* **96**:11277 (1999) … |

with the domain length **written into the table**, and immediately below it records the consequence:

> 1. **Shear rupture saturates with domain length** (~70 pN asymptote, Strunz et al.). A longer
>    staple domain does not buy proportional capacity.

Downstream, the parenthesis is dropped. `C-0009` judges its crossover path against it, `C-0014` divides it by
a concentration factor, and `C-0020` writes

> `A_eff = min over path classes of (allowable ÷ η)`, with the duplex axial force judged against the **48 pN**
> single-duplex shear allowable (a nick in a loaded duplex is a staple domain in shear geometry)

and reaches **`A_eff = 48.00 pN`** for an aligned tether, from which `L_min(10 nm) = 33.5 nm`, the withdrawal
of the ~227 nm assembly, and the stroke-independent **54.9 pN** normal preload all follow.

**The number is the capacity of a 30 base-pair bond.** Nothing in `C-0009`, `C-0014` or `C-0020` says how long
the tether's own hybridised domain is — and `T-19` exists precisely because that length is an *entry-topology*
variable, i.e. a sequence-design choice.

---

## Ground 1 — the paper's own model makes the length a first-order variable

Strunz et al. publish all three constants of their single-barrier fit: the barrier separation linear in the
number of base pairs (0.7 Å per bp with a 7 Å offset, their Eq. 3), the thermal off-rate
`ν = 10^(α − β n) s⁻¹` with `α = 3 ± 1` and `β = 0.5 ± 0.1` (Eq. 2), and `F = (k_BT/x) ln(r x/(ν k_BT))`
(Eq. 1). Assembled from those constants alone the model reproduces **both** of the paper's headline numbers —
**47.1 pN against the measured 48 ± 2 pN at 30 bp and 100 pN/s**, and a saturation of **68.1 pN** against their
own *"≈70 pN"*. It is therefore usable at the lengths *between* the three they measured, and it says:

| bonded length | 4 bp | 8 bp | 12 bp | **16 bp** | 20 bp | **30 bp** | 32 bp | ∞ |
|---|---|---|---|---|---|---|---|---|
| shear allowable at 100 pN/s | 3.6 | **18.8** | 28.3 | **34.8** | 39.5 | **47.1** | 48.2 | **68.1 pN** |

A Rothemund staple binds in 8- or 16-nucleotide domains. **At 16 bp the allowable is 34.8 pN, not 48 — the
figure in use is optimistic by ×1.35, and by ×2.6 if the extension is 8 bp.**

## Ground 2 — the load path it is applied to may not be a staple domain at all

`C-0020` justifies the 48 pN on the duplex axial path with *"a nick in a loaded duplex is a staple domain in
shear geometry"*. In a Rothemund sheet the **scaffold strand is continuous** and only the staples are nicked,
so axial tension along a helix runs down an unbroken backbone; the limit there is the **65 pN nicked
overstretching ceiling** (van Mameren et al.), not a domain rupture. The 48 pN belongs to the **tether's own
joint**, where the load genuinely has to cross from an extension into the sheet in shear.

Splitting the one allowable into the two it actually is moves the answer **both ways**: the duplex path gains
(48 → 65 pN) and the joint path loses (48 → 34.8 pN at a realistic 16 bp), and which binds depends on the
entry topology. That is the substance of the challenge: `A_eff` is not a property of the sheet.

## Ground 3 — and the correction is not monotone in the design, which is why it must be raised rather than patched

Because the allowable is **concave** in the bonded length, splitting one bond into several *shorter* ones on
adjacent duplexes divides the load faster than it weakens each domain above a break-even total length of
**14.3 bp** at the reference rate
(19.1 bp at Strunz's slowest measured rate, 3.0 bp at his fastest).
So the same physics that makes the 48 pN optimistic for a 16 bp bond
makes a **two-duplex** bond worth ×1.44 on the joint *and* ×2.00 on the sheet. A blanket downward correction of the allowable would be as wrong as the blanket 48.

---

## What this does *not* challenge

- **`C-0006`'s table.** It states the length. The error is in the transfer, not the source.
- **`C-0020`'s transfer ratios.** `η = 1.0000` aligned is reproduced here exactly, at every placement, and
  every entry topology obeys the cut-equilibrium floor. The *sheet* half of `C-0020` is untouched.
- **`C-0009`'s crossover path**, which is judged against the 10–15 pN **unzip** allowable — and unzipping is
  **length-independent**, so nothing there moves.
- **The 65 pN ceiling**, which is rate-independent and applies to the nicked duplex whatever the staple does.
- **Any verdict.** `C-0020`'s aligned design still passes; `L_min(10 nm)` moves from 33.5 nm to 39.4 nm at a
  16 bp joint and back to **27.7 nm** as soon as the same staple is split across two duplexes.

## The remedy proposed

Quote the shear allowable **with the bonded length it belongs to**, as `C-0006` already does, and carry the
length as a design variable rather than dropping it:

| design | bonded length per bond | allowable per bond | `A_eff` | `L_min(10 nm)` | 4-tether preload |
|---|---|---|---|---|---|
| ~~one point, 48 pN assumed~~ | ~~unstated~~ | ~~48.0 pN~~ | ~~48.0 pN~~ | ~~33.5 nm~~ | ~~54.9 pN~~ |
| one point, realistic extension | 16 bp | 34.8 pN | 34.8 pN | 39.4 nm | 34.2 pN |
| one point, long extension | 30 bp | 47.1 pN | 47.1 pN | 33.8 nm | 53.4 pN |
| **two duplexes, the same 32 bp staple split** | **16 bp each** | **34.8 pN** | **69.6 pN** | **27.7 nm** | 94.6 pN |
| two duplexes, 30 bp each | 30 bp | 47.1 pN | 94.2 pN | 23.7 nm | 146.7 pN |

and add the build rule beside the existing *"present every joint in shear geometry"* and *"align the tether
with the helices"*: **split the bond across two duplexes whenever the total bonded length exceeds ~14 bp**.

`C-0006`, `C-0014` and `C-0020` are annotated in place with a banner pointing here rather than edited, per
`gpd/README.md`.
