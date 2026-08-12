# CH-0002 — The corrections do not all run the same way, so `C-0001`'s numbers are not lower bounds

| | |
|---|---|
| **Challenges** | [`C-0001`](../claims/C-0001-layer-stiffness.md), the banner added to it on 2026-08-12, and the *"What follows, and what does not"* section of [`CH-0001`](CH-0001-semidilute-premise.md) that the banner quotes |
| **Raised by** | [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md), task [`T-1c`](../tasks/T-1c-crossover-valid-layer-response.md) |
| **Raised** | 2026-08-12, iteration 3 |
| **Status** | **UPHELD.** `C-0001` is not withdrawn and `CH-0001` is not withdrawn; what is corrected is the *direction* both attach to the correction, and the exponent range one of them asserts. |

---

## The standing statements being challenged

`CH-0001`, under *"Does not follow"*:

> **Does not follow.** That `C-0001` is wrong about direction. Every correction found here makes the
> layer **softer**, not stiffer:
>
> - the local exponent is 1.66–1.92 against the 9/4 used, and `P(h) = (k_BT/s³)[(L₀/h)^m − (h/L₀)^(3/4)]`
>   is monotonically increasing in `m` for `h < L₀`, so a smaller exponent means less pressure at
>   every compression;
> - the measured des Cloizeaux prefactor is **0.751×** the `k_BT/s³` de Gennes convention at the design point.
>
> So **`C-0001`'s strokes are lower bounds and its design window is a lower bound on the window's width.**

and, under *"Does follow"*, item 2:

> **`C-0001`'s stiffness at first contact is ~19% high.** `k/A = (k_BT/s³)(m + 3/4)/L₀`, so replacing
> `m = 9/4` by `m_eff = 1.672` multiplies it by `2.422/3 = 0.807`.

and the banner these produced on `C-0001`:

> Every correction runs the same way — softer — so **the strokes below are lower bounds and the design
> window is a lower bound on its own width**.

## The contradicting result

Re-deriving the layer response from an interaction free energy valid across the crossover (`C-0003`):

| quantity, at `L₀ = 10 nm`, `σ = 0.024 nm⁻²` | `C-0001` | `C-0003` bracket |
|---|---|---|
| osmotic exponent of the **layer's** restoring pressure | 9/4 used; 1.66–1.92 asserted by `CH-0001` | **2.00 – 2.37** at rest, up to **2.49** under load |
| chain length `N` for a 10 nm layer | 199.4 | **224.8 – 374.3** |
| `φ/φ#` | 1.13 (`C-0002`) | **1.40 – 3.51** |
| stiffness at first contact | 7.39 pN/nm | **0 (diffuse edge) or 9.84 – 13.83** |
| stroke at 100 pN | 4.95 nm | **3.83 – 6.01 nm** |
| secant stiffness | 20.2 pN/nm | **16.6 – 26.1 pN/nm** |

and at the other two heights the stroke brackets are **0.47–1.53 nm** (5 nm, against 0.73)
and **1.55–3.21 nm** (7 nm, against 2.20) — straddling in both cases.

Provenance: `gpd/results/T-1c-crossover-valid-layer-response.json`, `brush.CrossoverLayerStudyKt`.

## Methodological grounds

Two, and the first is the one that matters.

### 1. The exponent `CH-0001` corrected with is a bulk quantity, and a grafted layer does not have it

`CH-0001` took `m_eff = d lnΠ/d lnφ` from the measured **bulk** equation of state
`Π(φ) = (k_BT/v₀)[φ/N + αφ^(9/4)]` and substituted it into the brush pressure law.
Integrating that same equation of state through `f(φ) = φ ∫ Π(φ')/φ'² dφ'` gives

&nbsp;&nbsp;&nbsp;&nbsp;`f(φ) = (k_BT/v₀)[ (φ lnφ)/N + (4α/5) φ^(9/4) ]`

and the first term is the **translational entropy of whole chains** — an ideal-gas entropy of *chain
centres of mass*. It is the only term that bends the exponent below 9/4: remove it and `Π_int = αφ^(9/4)`
exactly, at every density. Grafting removes it. The chains are tethered; they do not explore the volume.

So `m_eff = 1.66–1.92` is a correct statement about a **beaker of PEG solution** and not a statement about a
**grafted layer** at all. What the layer's own interaction has is `2.00–2.56` across every model and every
design point in the sweep — the two-body limb contributing exactly 2, the third virial term pushing above
9/4, and nothing anywhere below 2.

`CH-0001` names this as the way it could fail, in its own closing paragraph:

> **If this challenge is itself wrong**, the way it fails is that a grafted layer, having no chain
> translational entropy, is semidilute at a lower density than a bulk solution of the same chains

That is the right mechanism, and it turns out to bite harder than the phrasing suggests: it does not merely
move `φ#` down for a brush, it removes the entire van't Hoff limb from the brush's free energy, and with it
the whole basis for an exponent below 2.

**Therefore `CH-0001`'s first "softer" correction does not exist**, and its item 2 — *"`C-0001`'s stiffness at
first contact is ~19 % high"* — has the wrong sign. On the box profile the crossover-valid stiffness at first
contact is **9.84–13.83 pN/nm** against `C-0001`'s 7.39, i.e. **33–87 % higher**.

### 2. The direction claim was made without the height relation being corrected

`CH-0001`'s own item 3 says the chain length under every `C-0001` number rests on the failed premise and
*"cannot be repaired by changing an exponent"* — and then the *"Does not follow"* section nonetheless
concludes a direction, from the two corrections that **were** available (exponent and prefactor), while the
larger and unavailable one was still outstanding.

It is the larger one. Correcting the height relation raises `N(L₀)` by **5–88 %**, which raises `φ` by the same
factor, which raises `Π_int` by `φ^m` — and it runs **stiffer**, opposite to both corrections `CH-0001` had.
The three effects are comparable in size and opposite in sign, which is exactly why the resulting bracket
**straddles** `C-0001` rather than lying to one side of it.

## What follows, and what does not

**Does not follow.** That `CH-0001` is wrong about the *premise*. It is not. Its central finding — that the
semidilute premise was checked against the wrong boundary in the wrong units, and fails — stands, and is
reinforced here by two further routes: the chains contain **0.06 thermal blobs** and so are not swollen at all,
and `CH-0003`'s blob stack is 1.47 deep. `CH-0001`'s item 3 is upheld and quantified. `CH-0001` is **not**
withdrawn.

**Does not follow.** That `C-0001`'s numbers are wrong. Its arithmetic is correct and reproducible, and its
verdict of PASS against the `T-1` predicate stands. What changes is what may be *inferred* from them.

**Does follow.**

1. **`C-0001`'s strokes are not lower bounds.** They sit inside the crossover-valid bracket at all three
   heights. A downstream task must carry the bracket, not the single number, and not a one-sided inequality.
2. **`C-0001`'s design window is not a lower bound on its own width either.** At 10 nm it is *empty* under
   both box models and `[0.018, 0.061] nm⁻²` under the strong-stretching ones — wider than `C-0001`'s
   `[0.024, 0.045]` under one profile model and non-existent under the other. The window's existence is
   decided by the **profile**, which neither claim has established.
3. **`m = 3` is not excluded by measurement.** `CH-0001` excluded it from the bulk `m_eff` range. The layer's
   own interaction reaches **2.56** at rest and **2.59** under load once the measured third virial coefficient
   is included, so the accessible range is `[2, 2.6]`, not `[1.66, 1.92]`. What is excluded is `m < 2`.
4. **`C-0001`'s one surviving headline is the one it stated most cautiously.** *"The ~10 nm desired stroke is
   unreachable at 100 pN anywhere in the brush regime, at any of the three heights"* is confirmed under every
   model, every interaction and every regime criterion tried here.

## Resolution

`C-0001` is **not withdrawn and not overwritten**; nor is `CH-0001`. The banner on `C-0001` is annotated in
place with a pointer here, and the phrase *"the strokes below are lower bounds"* is struck through, because it
is the one assertion that a downstream task could act on and be wrong.

**Outstanding, and queued:**

- **`P-9`** — the bulk-vs-brush interaction parameter (`C-0007`). Every osmotic input to `C-0003` is a bulk
  measurement applied to a brush. `C-0003` quantifies its own exposure (`k ∝ K^(4/13)`), so this is a bounded
  risk rather than an open one, but it is the largest remaining premise.
- **`T-2`** — must treat the 10 nm window as *possible and not established*, and the thing that would settle
  it is a numerical SCF density profile, not a better interaction law.

**If this challenge is itself wrong**, the way it fails is that a grafted layer is *not* free of translational
entropy after all — that the grafting points are mobile enough, or the layer laterally inhomogeneous enough,
that some chain-scale entropy survives and drags the exponent back below 2. Nothing in `C-0002` or `C-0003`
excludes that; what it would take to settle it is a lateral-inhomogeneity model, and no task is queued for one
because nothing yet depends on it.
