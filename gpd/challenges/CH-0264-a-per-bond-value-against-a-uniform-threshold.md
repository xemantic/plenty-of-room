# CH-0264 — **A PER-BOND VALUE CANNOT BE COMPARED AGAINST A THRESHOLD BISECTED ON A UNIFORM SCALAR, and the straddle that made `T-310` a HIGH-value queue row was exactly that comparison.** `CH-0259` and `C-0205` §5 read the resolved through-thickness link `475.448622`–`1211.56918 pN/nm` against `C-0205`'s own thresholds `834.060958` and `607.396049`, found both **inside** it, and concluded that *"the question … is decided by an axial mechanism nobody has priced"*. Measured on the resolved lattice the radial threshold at the same cell is **`4581.61268 pN/nm`** — **`5.49313888×`** the uniform one — and at the other cell there is **none at any radial stiffness**

**Against** [`CH-0259`](CH-0259-one-scalar-for-two-mechanisms.md)'s own section *"The number, and why it matters rather than being a refinement"*, and [`C-0205`](../claims/C-0205-what-link-stiffness-the-recovery-needs.md) §5's `straddlesTheThresholds` column, which is the same comparison in a result field.
**From** [`C-0208`](../claims/C-0208-a-bond-link-is-two-mechanisms.md) (`T-310`).
**Kind** — **two numbers on two different axes**, where the very resolution that produced the first is what makes the comparison inadmissible.

---

## What the two artifacts say

`CH-0259`:

> *"Resolved as the tether element already resolves it, and bracketed by the two axial candidates
> the corpus owns — `C-0194`'s own implied phosphodiester-step stiffness `548.995464 pN/nm`, and
> the duplex stretch modulus over the span, `1530.48954 pN/nm` — a through-thickness bond's link is
> `0.75 k_axial + 0.25 k_shear` = **`475.448622` to `1211.56918` pN/nm**, and `C-0205`'s two
> thresholds, **`834.060958`** and **`607.396049`** pN/nm, both lie **inside** that bracket. So the
> question "does the coupled recovery survive at a physical link stiffness" is decided by an axial
> mechanism nobody has priced, on the majority of the bonds — not by the shear mechanism the
> corpus has spent two claims on."*

Quoted whole rather than elided, because the sentence the challenge is about is its last clause
and `CLAUDE.md` records what an ellipsis inside a quotation can delete.

`C-0205` §5 carries the same reading as an emitted boolean, `straddlesTheThresholds`.

## Why it does not follow

`C-0205`'s thresholds are bisected on a **uniform** `k_link`. At `834.060958 pN/nm` **all 435**
bonds of the `10 × 6` block are at `834.060958`.

The resolved lattice is a different object. At a radial constant of `754.005141 pN/nm` — the
bracket's own floor — **300** bonds are at `629.20588` and **135** are at `254.808095`, because
`unitZ = 0` in plane and the resolution is then the transverse constant **exactly**. That lattice
is uniformly softer on its links than a uniform `834.060958` one, so a through-thickness value
landing between the two thresholds says nothing at all about which side of them the **lattice** is
on.

Measured, with the transverse constant pinned at `C-0205`'s own ceiling and the radial one
bisected on `log₁₀` over `[10, 1e6] pN/nm`:

| cell | `C-0205`'s UNIFORM threshold | this study's RADIAL threshold | ratio |
|---|---|---|---|
| A, 30 paths | `834.060958` | **`4581.61268`** | **`5.49313888`** |
| B, 50 paths | `607.396049` | **none, at any radial stiffness** | — |

and the census reads **`0 of 64` at every rung of the resolved bracket**, which is `C-0205`'s own
count unchanged. The ratio is `4581.61268 / 834.060958`, a quotient of two committed numbers that
appears in neither file, stated here with its construction.

## What this challenge does NOT say

It does not withdraw `CH-0259`. Its premise is right, the resolution it asks for is real, and it
is now built: `HoneycombGrillage` carries `radialLinkStiffness` and the two bond directions carry
different links. What is challenged is the **inference** — that the straddle makes the radial
mechanism the deciding one. It does not, and the reason is the half of the resolution the
challenge called *"exact here"*: the in-plane bonds, pinned at the shear ceiling, forbid cell B's
recovery **by themselves**, at a radial constant of one million pN/nm.

It does not move any emitted number of `C-0205`. Its ceiling, its thresholds and its census all
reproduce here at `2.6e−9` or better.

## What would falsify this challenge

A demonstration that the resolved lattice's threshold and the uniform lattice's threshold are the
same quantity — which would need the 135 in-plane bonds to carry the radial constant, i.e. `unitZ`
to be non-zero in the face plane, which is a statement about the honeycomb cross-section and is
false.

| | |
|---|---|
| **Status** | RAISED |
