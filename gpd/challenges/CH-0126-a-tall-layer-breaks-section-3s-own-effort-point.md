# CH-0126 — **§3's effort-point row does not permit a 17–26 nm layer, it forbids one, and the arithmetic is one addition.** `C-0050` reads *"the effort point may sit ~20–25 nm above the electrode"* as evidence that a 17–26 nm layer's geometry *"is not absurd"*. On this programme's own `ActuatorGeometry` the effort point of a 17–26 nm layer is **32–41 nm**, and **27–36 nm** with the lever bonded straight onto the tile — **1.08× to 1.64× past the top of the band**, at every one of the six heights `C-0050` names

| | |
|---|---|
| **Raised by** | [`C-0110`](../claims/C-0110-device-b-tall-gap.md) (`T-192`) |
| **Against** | [`C-0050`](../claims/C-0050-desired-stroke-reach.md)'s escape section — the sentence *"§3's own tile-thickness row notes the effort point 'may sit ~20–25 nm above the electrode', so the geometry is not absurd"* — and the same sentence as it has been carried into `TASKS.md`, `ANSWERS.md` §6 row 2 and `DECISIONS-FOR-NDI.md` decision 2 |
| **Grounds** | `ActuatorGeometry.effortPointHeight(L₀) = L₀ + tileThickness + leverAttachmentHeight`, with §3's own 10 nm tile. At §3's three layer heights and a 5 nm attachment that is **20 / 22 / 25 nm** — the §3 band reproduced at both ends, which is exactly why `CLAUDE.md` records the row as *forcing a constant attachment height and fixing it at 5 nm*. At 17–26 nm the same expression gives **32–41 nm**, and 27–36 nm at a zero-length attachment. The row `C-0050` cites as permission is the row that refuses |
| **Severity** | **one sentence and its transfer into three documents, not a verdict.** `C-0050`'s escape table (16.63–26.12 nm, reproduced by `C-0110` to ≤ 2.0e−4), its five routes to *"unreachable on §3's own stack"*, its `s < L₀` identity and its whole catalogue are untouched. What moves is (a) the clause *"so the geometry is not absurd"*, (b) the reading of §3's effort-point row as permissive, and (c) the standing of decision 2 as a question about the **layer** alone — it is a question about the **stack** |

---

## What is claimed upstream

`C-0050`, in its escape section:

> **1.7–2.6× §3's tallest layer**, and `C-0001` already recorded the direction — *"the reason to go outside the
> 5–10 nm range is upward"* — without pricing it.
> **§3's own tile-thickness row notes the effort point *"may sit ~20–25 nm above the electrode"*, so the geometry
> is not absurd**; but a 17–26 nm layer is a different device …

and the same sentence in `ANSWERS.md` §6 row 2 (*"§3's own tile row already allows the effort point at '~20–25 nm above the electrode', so the geometry is not absurd"*), in the `T-115` row of `TASKS.md`, and in `DECISIONS-FOR-NDI.md`'s decision-2 block (*"§3's own tile row already allows the effort point to sit ~20–25 nm above the electrode, so the geometry is not absurd on its face"*).

## What the arithmetic says

`ActuatorGeometry`'s own KDoc states the construction and its own calibration:

> *"how far above the tile's **top** face the output coupling takes its purchase, in nm. §3 says the effort
> point 'may sit ~20–25 nm above the electrode'; at 5 nm this places the three §3 layer heights at exactly
> 20 / 22 / 25 nm, i.e. it reproduces the §3 band at both ends."*

| `L₀` | effort point at a 5 nm attachment | at a 0 nm attachment | §3's band |
|---|---|---|---|
| 5 nm | 20 nm | 15 | 20–25 |
| 7 nm | 22 nm | 17 | 20–25 |
| 10 nm | **25 nm** | 20 | 20–25 |
| **17 nm** | **32 nm** | **27** | 20–25 |
| 20 nm | 35 nm | 30 | 20–25 |
| 23 nm | 38 nm | 33 | 20–25 |
| **26 nm** | **41 nm** | **36** | 20–25 |

**The band is exhausted at exactly `L₀ = 10 nm`.** That is not a coincidence and it is `CLAUDE.md`'s own recorded finding: *"§3's `~20–25 nm` band is exactly as wide as its 5–10 nm layer-height range, which forces a **constant** attachment height and fixes it at **5 nm**."* A row that is saturated at the top of the layer range cannot also be evidence that a layer 2.6× taller is admissible.

Emitted in `gpd/results/T-192-device-b-tall-gap.json`, `validityDepartures[]`, quantity *"§3's own effort-point row"*: `valueHere` = **31.9999 to 41.0 nm** at a 10 nm tile and a 5 nm attachment, **26.9999 to 36.0 nm** with the lever bonded straight onto the tile; `severity` = **1.08× to 1.64×** the top of §3's band.

## Why this is a challenge and not a note

Three reasons.

1. **It is load-bearing in a decision NDI is being asked to take.** `DECISIONS-FOR-NDI.md`'s decision 2 offers a 17–26 nm layer as a yes/no about the **polymer**, and cites the effort-point row as evidence that the rest of the stack accommodates it. It does not. The decision as posed is therefore incomplete: a *yes* to a 17–26 nm layer is simultaneously a *yes* to moving §3's effort point by 1.08–1.64×, and NDI has not been told so.
2. **It cost nothing to check and nobody checked it.** The constant is in `ActuatorGeometry`, the arithmetic is one addition, and `CLAUDE.md` already carried the sentence that makes it obvious. `C-0110` found it while writing a validity table, not while looking for it — which is the argument for writing the validity table.
3. **The claim's own conclusion is unchanged and its ground is not.** `C-0050`'s *"a 17–26 nm layer is a different device"* survives and is strengthened; what fails is the reason offered for thinking the geometry was already accommodated.

## What would settle it

A statement from NDI that the effort point may sit above 25 nm — which is a **specification** answer, not a calculation, and belongs beside decisions 2 and 4 rather than in a claim. Until then the row stands as a constraint, and `C-0110` records it as such.

## What this challenge does NOT say

It does not say a 17–26 nm layer is geometrically impossible. It says §3, as written, does not accommodate one, and that the sentence offered as evidence that it does is reading the row backwards. `C-0110`'s own refusal of the tall corner rests on the **field**, not on this — the two are independent, and this one is the cheaper.
