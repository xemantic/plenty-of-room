# CH-0259 — **UPHELD IN ITS PREMISE and REFUTED IN ITS CONSEQUENCE** ([`C-0208`](../claims/C-0208-a-bond-link-is-two-mechanisms.md), [`T-310`](../tasks/T-310-a-bond-link-is-two-mechanisms.md)): the per-bond link is **built**, the resolution is real — in plane the link is `C-0205`'s shear ceiling `254.808095 pN/nm` **exactly** and through the thickness `629.20588`–`1365.32644` — and the census is **`0 of 64` at every rung of the resolved bracket**, so the answer does **not** move. What is refuted is *"the question … is decided by an axial mechanism nobody has priced"*: the **radial** threshold measured on the resolved lattice is **`4581.61268 pN/nm`** at cell A, `2.63993935×` the bracket ceiling, and at cell B there is **none at any radial stiffness** — the 135 IN-PLANE bonds, the ones this challenge calls *"exact here"*, forbid the recovery by themselves. The straddle below compares a per-bond value against a threshold bisected on a **uniform** scalar ([`CH-0264`](CH-0264-a-per-bond-value-against-a-uniform-threshold.md)). The original headline follows. **`HoneycombGrillage` resolves a TETHER's two mechanisms onto the link residual by the bond's own direction and applies ONE scalar to a BOND's, so the span-shear link stiffness is exact for the in-plane bonds and is not the whole story for the two thirds that run through the thickness**

**Against** [`C-0194`](../claims/C-0194-the-common-mode-is-the-link.md) §4, which derives `k_R = T/g` from a *shear* of a taut connector and applies it as **the** link stiffness; and against [`C-0205`](../claims/C-0205-what-link-stiffness-the-recovery-needs.md)'s own ceiling, which inherits that reading.
**From** [`C-0205`](../claims/C-0205-what-link-stiffness-the-recovery-needs.md) (`T-303`).
**Kind** — **an element that carries one constant for two mechanisms**, where the same source file already carries the resolution for a neighbouring element.

---

## The two elements, twelve hundred lines apart in one file

`HoneycombTetherElement` (`tile/HoneycombGrillage.kt`) resolves a chain's two mechanisms
onto the link residual by the bond's own direction, and its KDoc says why:

> *"The chain's own decomposition, `K = (df/dx)n̂n̂ᵀ + (f/x)(I − n̂n̂ᵀ)` with
> `n̂ = (unitY, unitZ, 0)` … gives exactly two scalars on the grillage's two existing gradients"*,

so that

> *"The stiffness in pN/nm on the **link** residual: `(df/dx)·unitZ² + (f/x)·unitY²`"*.

A **bond**'s link carries the constructor's single `linkStiffness`, at every bond direction.

## Why the distinction is load-bearing here

`W` is the deflection **normal to the face**, i.e. along the block's thickness `z`.
For an **in-plane** bond the line of centres lies in the face plane, `unitZ = 0`, and a relative
`W` displacement is a pure transverse **shear** of the connector — exactly the mechanism
`CH-0242`'s expansion and `C-0194` §4's `k_R = T/g` describe, and there
`C-0205`'s ceiling of `254.808095 pN/nm` is the right ceiling.

For a **through-thickness** bond `unitZ² = 0.75`, so three quarters of a relative `W` displacement
is a change of the interhelical **separation**. What resists that is the connector's own **axial**
stiffness and the duplex pair's own force — neither of which is `T/g`, and neither of which
`CH-0242`, `C-0194` or `C-0205` prices. Two of every three bonds of a honeycomb block are of this
kind.

## The number, and why it matters rather than being a refinement

> **THE STRADDLE BELOW IS A COMPARISON BETWEEN TWO DIFFERENT OBJECTS** ([`CH-0264`](CH-0264-a-per-bond-value-against-a-uniform-threshold.md)).
> `C-0205`'s thresholds are bisected on a **uniform** `k_link`, where all 435 bonds sit at the
> threshold; the resolved lattice puts **135** of them at `254.808095` because `unitZ = 0` in
> plane. Measured on the resolved lattice the **radial** threshold is `4581.61268 pN/nm` at cell A
> — `5.49313888×` the uniform one — and cell B has **none at any radial stiffness**. The paragraph
> below is retained because `C-0208` §4 is written against it.

Resolved as the tether element already resolves it, and bracketed by the two axial candidates the
corpus owns — `C-0194`'s own implied phosphodiester-step stiffness `548.995464 pN/nm`, and the
duplex stretch modulus over the span, `1530.48954 pN/nm` — a through-thickness bond's link is

&nbsp;&nbsp;&nbsp;&nbsp;`0.75 k_axial + 0.25 k_shear` = **`475.448622` to `1211.56918` pN/nm**,

and `C-0205`'s two thresholds, **`834.060958`** and **`607.396049`** pN/nm, both lie **inside** that
bracket. So the question *"does the coupled recovery survive at a physical link stiffness"* is
decided by an axial mechanism nobody has priced, on the majority of the bonds — not by the shear
mechanism the corpus has spent two claims on.

## What would settle it

A per-bond `linkStiffness` in `HoneycombGrillage`, populated the way `HoneycombTetherElement`
already populates its own, and `C-0205`'s census re-taken on it. That is a change to a shared
source and it is a queue item (`T-310`), not a footnote — and it needs the axial constant, which
this repository does not have either: `C-0205`'s recorded literature search found **no** published
number for a crossover's stiffness against a relative normal displacement, in all-atom MD, oxDNA,
metadynamics or experiment.

## What this challenge does NOT say

It does not withdraw `C-0194` §4. Its derivation of `k_R` is correct for the coordinate it is
written on, and the frame-indifference theorem, the identity `d²/(2gr_P) − d/(2r_P) = d/g` and the
`336.800449×` reading are untouched. What is challenged is the **transfer** of one scalar to every
bond direction, and the transfer is the file's own — `C-0194` inherited it.

| | |
|---|---|
| **Status** | ~~RAISED~~ **UPHELD IN PART and REFUTED IN ITS DIRECTION** ([`C-0208`](../claims/C-0208-a-bond-link-is-two-mechanisms.md), [`T-310`](../tasks/T-310-a-bond-link-is-two-mechanisms.md)) |
