# CH-0091 — A first-moment 10 nm layer is not a 10 nm layer: it puts the tile at 16–18 nm, and §3 admits it at 0 of 61 grid points

| | |
|---|---|
| **Against** | [`C-0016`](../claims/C-0016-design-window.md) — its **height-convention banner**, and the same sentence as carried by [`C-0027`](../claims/C-0027-window-resynthesis.md), [`C-0051`](../claims/C-0051-second-window-resynthesis.md) and `ANSWERS.md` |
| **Raised by** | [`C-0077`](../claims/C-0077-first-moment-chain-length.md), task [`T-1e`](../tasks/T-1e.md) |
| **Raised** | 2026-08-17, iteration 15 |
| **Grounds** | **methodological**, with two arithmetic corrections attached. The banner presents two conventions as two readings of **one** layer; they are two different layers, and only one of them is inside §3 |
| **Status** | **UPHELD, and `C-0016`'s window does not move — not one edge, not one owner, not one grid step.** What moves is the banner's two numbers and its framing |

---

## What the banner says

`C-0016` opens with it, before any result, in a box of its own:

> ## THE HEIGHT CONVENTION — read this before any number below
>
> > **Every layer height here is a FORCE-ONSET height: `L₀` is the height at which the layer carries
> > 1.0 pN over the 40 × 40 nm tile** (`C-0011`).
>
> The **first-moment** thickness `2⟨z⟩` of the same layer is **1.71–2.16× smaller** across the
> surviving windows. The polymer to order differs by about **four times** between the two
> conventions, and `T-1e` — which would separate the definitional part of that gap from the physical
> part exactly — **has not run**.
>
> **A bench reading this window in the wrong convention would order 8–9 kDa PEG where it needs
> 1.1–3.3 kDa.**

`ANSWERS.md` carries the same, and calls it *"the single most likely way this window gets misread at
a bench"*.

**`T-1e` has now run.** The banner's instinct was right and three of its numbers are not.

---

## The corrections

### 1. The framing: these are two devices, not two readings

The banner reads as *"here is a 10 nm layer; one convention calls its polymer 1.6–3.3 kDa and the
other calls it 8–9 kDa"*. But a layer whose **first moment** is 10 nm is a **different, taller layer
holding a longer chain**, and `C-0077` measures where its tile sits:

| | force-onset convention | first-moment convention |
|---|---|---|
| the specification `L₀ = 10 nm` means | the tile sits at 10 nm | `2⟨z⟩ = 10 nm` |
| where the tile actually sits | 10 nm, by construction | **16.08 – 18.05 nm** across `C-0016`'s own window; **13.20 – 18.05 nm** across the whole 61-point grid |
| inside §3's stated 5–10 nm band? | yes, by construction | **0 of 61 grid points** |
| `N` at the design point | 62.108 | 175.080 |

**§3 specifies where the tile is.** *"Polymer layer thickness 5–10 nm"* sits in the same table as
*"effort point ~20–25 nm above the electrode"*, i.e. it is a statement about the stack, about a
distance between two bodies. That is a force-onset statement, and it is why **`C-0016`'s window is in
the right convention**.

The risk the banner warns about is therefore real but mis-described. It is not *"a bench might order
the wrong polymer for this device"*; it is *"a bench that specifies the layer by a **measured**
thickness — which is the only thickness a bench can measure — is specifying a **different device**,
and one §3 does not currently admit."* That is a specification question, and it belongs beside
`T-115`'s *"may the layer be taller than 10 nm?"* rather than in a modelling footnote.

### 2. The molar mass: 7.71 kDa at the design point, 4.17–8.73 kDa across the window

`C-0077` inverts `N` on the first moment exactly rather than by scaling:

| | 7 nm window | 10 nm window |
|---|---|---|
| `N`, force-onset (`C-0016`'s row) | 25.3 – 28.0 | 36.6 – 74.0 |
| PEG, force-onset (`C-0016`'s row) | 1.11 – 1.23 kDa | 1.61 – 3.26 kDa |
| **`N`, first-moment** | — | **94.7 – 198.1** |
| **PEG, first-moment** | — | **4.17 – 8.73 kDa** |
| at the design point `σ = 0.024` | 2.736 kDa | **7.713 kDa** |

**8–9 kDa is the value at the window's *lower edge only*.** Across the window the first-moment
reading is 4.17–8.73 kDa, and at the `C-0001`/`C-0003` design point it is **7.71 kDa** — below the
banner's band. The 8–9 kDa came from `C-0011`'s `N ≈ 190–210` scaling estimate, which `CH-0090`
shows is 12–19 % high.

### 3. The factor: 2.59–2.84×, not "about four"

| | value |
|---|---|
| `N_M/N_F` at the 10 nm design point | **2.819** |
| `N_M/N_F` across `C-0016`'s 10 nm window | **2.585 – 2.843** |
| `N_M/N_F` across the whole 61-point grid | 1.947 – 2.843 |
| across `C-0003`'s three interaction laws at the design point | 2.819 – 3.040 |

*"About four times"* is the ratio of the **top** of one range (8–9 kDa) to the **bottom** of the other
(1.6–3.3 kDa) — two ends of two different intervals. Read point by point, as a ratio must be, the
conventions differ by **2.6–2.8×**.

The banner's *other* number is correct: the first-moment thickness is **1.71–2.07× smaller** across
the 10 nm window (`C-0077` measures the shape ratio at 1.710–2.066 there, 1.372–2.069 over the whole
grid), against the banner's 1.71–2.16.

---

## What follows, and what does not

**Does not follow.** That any window edge moves. `C-0016`'s `σ ∈ [0.0116, 0.2601]` at 10 nm and
`[0.0296, 0.0496]` at 7 nm are in the force-onset convention, the lower edges are still owned by coil
overlap `Σ ≥ 1` and the upper by the 3 nm stroke, and 5 nm is still empty. **Zero of six edges, zero
grid steps, no owner change.** `C-0027`'s and `C-0051`'s re-runs are equally untouched.

**Does not follow.** That the banner should be deleted. Its instinct — that a convention mismatch is
the most likely way this window gets misread — is upheld, and it is the reason `T-1e` was queued.

**Does follow.**

1. **The banner's arithmetic is replaced**: 7.71 kDa at the design point and 4.17–8.73 kDa across the
   window, at a convention factor of **2.59–2.84×**, not 8–9 kDa at "about four times".
2. **The banner's framing is replaced**: the first-moment reading is a *different device*, not a
   second reading of this one, and its tile sits 16.1–18.0 nm above the electrode.
3. **Both numbers must travel together in the bench order.** A bench does not buy a force onset; it
   buys a molecular weight, and the molecular weight that produces a given *measured* thickness is
   the first-moment one. `C-0077` is the conversion, and `C-0016`'s bench-order table should carry
   both rows.
4. **A fifth specification question is available and is deliberately NOT raised as one.** *"Is §3's
   5–10 nm a tile height or a measured layer thickness?"* would be the natural question to send NDI —
   but §3's own effort-point row already answers it (a distance between two bodies), so this is
   settled by reading rather than by asking, and the programme's four open questions stay four.

## If this challenge is itself wrong

Two ways.

**The specification reading.** If NDI intends *"polymer layer thickness 5–10 nm"* as a **measured**
thickness — an ellipsometric or reflectivity number — then the first-moment convention is the
specified one, `C-0016`'s window is in the wrong convention throughout, and the whole intersection
would have to be re-run on layers whose tiles sit at 13–18 nm. `C-0077` does not run that, and says
so. The §3 effort-point row is the evidence against it and it is not a proof.

**The layer model.** The 16.1–18.0 nm force-onset heights are `C-0011`'s solved SCF layer at the 1 pN
threshold, and they inherit its unbounded mean-field correction at `φ ≈ 0.01` whole. A layer model
that put the force onset of a `2⟨z⟩ = 10 nm` layer below 10 nm would defeat point 1 of this
challenge — but no model in this programme does: the box's force onset **is** its first moment, and
strong stretching's is 1.276× larger.
