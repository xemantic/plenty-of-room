# CH-0180 — the scaffold this programme's cross-section is folded from is **not established**: the caDNAno paper's Methods list says design (i) is `p8064`, its main text's own rule says **p7560**, and the Methods list agrees with that rule at **6 of its 7 designs and disagrees only at ours**

| | |
|---|---|
| **Against** | [`C-0125`](../claims/C-0125-scaffold-remainder.md) — its scaffold table row *"p8064 \| 8 064 \| **READ DIRECTLY**; designs (i), (iii), (v)"* and the **1 344 nt** remainder its whole bound is written on; and [`C-0119`](../claims/C-0119-honeycomb-raster-width.md) §5 — *"p8064 — designs i, iii, v, **including ours**"* and *"design (i) used p8064"* |
| **Raised by** | [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) / [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md), while sweeping what [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) moved |
| **Kind** | **substantive — and it is a SOURCE contradiction, not a reading error.** Both claims read the source correctly. The source disagrees with itself, in two passages of one paper, and neither claim knows the other passage exists |
| **Status** | **raised.** It sharpens [`CH-0173`](CH-0173-the-built-block-turns-on-loops-not-crossovers.md) item 2, which asserted the correction without knowing what it was correcting |

---

## 1. The two passages, both in `gpd/data/T-151-sources/PMC2731887-fullTextXML.xml`

**The main text states a rule:**

> *"The shapes were folded either from a **7560-base scaffold into 60 parallel helices** or from an
> **8064-base scaffold into 64 parallel helices** to create number-of-rows versus
> number-of-helices-per-x-raster-row combinations of 15 × 4, 10 × 6 …, 8 × 8, 6 × 10, 4 × 16,
> 3 × 20, 2 × 30."*

**The Methods section states a list:**

> *"The design: scaffold pairings are as follows: **i: p8064**, ii: p7560, iii: p8064, iv: p7560,
> v: p8064, vi: p7560, vii: p7560."*

Design (i) is `15 × 4` = **60** helices. The rule sends 60 helices to **p7560**. The list sends
design (i) to **p8064**. They cannot both be right.

## 2. The discriminator, and it is one column of arithmetic

Apply the main text's rule to all seven designs and compare against the Methods list:

| design | `m × n` | helices | rule says | Methods list says | agree? |
|---|---|---|---|---|---|
| **(i)** | **15 × 4** | **60** | **p7560** | **p8064** | **NO** |
| (ii) | 10 × 6 | 60 | p7560 | p7560 | yes |
| (iii) | 8 × 8 | 64 | p8064 | p8064 | yes |
| (iv) | 6 × 10 | 60 | p7560 | p7560 | yes |
| (v) | 4 × 16 | 64 | p8064 | p8064 | yes |
| (vi) | 3 × 20 | 60 | p7560 | p7560 | yes |
| (vii) | 2 × 30 | 60 | p7560 | p7560 | yes |

**The list agrees with the rule at 6 of 7 and disagrees at exactly one — the one this programme uses.**

And the paper's own per-helix accounting closes it:

> *"Each helix was allotted **126 bases** of scaffold."*

`60 × 126 = 7 560` and `64 × 126 = 8 064`, **exactly**. Under the Methods list, design (i) would
fold 60 helices at 126 bases from an 8 064-base scaffold and leave **504 nucleotides** the paper's
own accounting has no line for. Under the rule it spends its scaffold to the last nucleotide.

**The balance of evidence is that the Methods list carries a typographical error at (i)**, and this
challenge does not assert it as a fact: what it asserts is that **the scaffold is not established**,
and that two standing claims state it as though it were.

## 3. What moves if the main text governs

| | `C-0125` / `C-0119` as written | if design (i) is p7560 |
|---|---|---|
| scaffold for the four-layer tile | p8064, 8 064 nt | **p7560, 7 560 nt** |
| unpaired remainder, four layer `10 × 6` | **1 344 nt** | **0 nt** |
| `C-0119`'s budget row | *"p8064 — designs i, iii, v, including ours"* | one design out |
| `C-0125`'s headline bound | written on 1 344 nt | written on a remainder that does not exist |

**`C-0125`'s bound survives in form and not in value.** It is a per-nucleotide bound with the
remainder as an input, so a smaller remainder makes it *smaller* — the direction is favourable and
the number is not the published one. `C-0119`'s routing conclusion, its integrality result, its seam
and its yield reading are untouched: none of them reads the scaffold identity.

**And `C-0140`'s own p7560 derivation is upheld and its ground is widened.** `C-0140` derived the
identity from the helix count alone and did not know the Methods list contradicts it; that the
contradiction exists is *stronger* evidence for its reading than the arithmetic alone, because the
disagreement is confined to one row of a list that is otherwise consistent with the rule.

## What would settle it

1. **Supplementary Notes 2 and 3** — the paper's own *"detailed schematics and staple lists"*. A
   staple list for design (i) has a scaffold length in it, and that is decisive.
2. A **published erratum** or a later paper citing design (i)'s scaffold.
3. Failing both, the claims must say **p7560 or p8064, not established**, and carry the remainder as
   a bracket `0–504 nt` rather than as 1 344.
