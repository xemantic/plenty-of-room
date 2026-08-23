# CH-0003 — The Gen-1 layer is one and a half blobs tall, so it is not a blob stack

| | |
|---|---|
| **Challenges** | [`C-0001`](../claims/C-0001-layer-stiffness.md), its use of the Alexander-de Gennes height relation and the `Σ ≥ 5` brush-onset criterion that sets its window's lower edge |
| **Raised by** | [`C-0004`](../claims/C-0004-poroelastic-drainage.md), task [`T-7`](../tasks/T-7-poroelastic-drainage.md) |
| **Raised** | 2026-08-12, iteration 3 |
| **Status** | ~~**OPEN** — for `T-1c` to resolve.~~ **RESOLVED** by [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) (`T-1c`), which is what the index has recorded since: the height relation is replaced, and the chains carry 0.06 thermal blobs — there is no blob structure to stack. `C-0001` is not withdrawn and its numbers are not disputed here |

---

## The standing statement being challenged

`C-0001` derives every one of its numbers from the Alexander-de Gennes picture, in which a grafted chain
is subdivided into blobs of the grafting spacing `s` and the layer height is the blob diameter times the
number of blobs per chain:

> `L₀ ≃ N a (a/s)^(2/3)`

and locates the lower edge of its design window at the conventional brush onset `Σ ≥ 5`,
which `C-0002` then shows is *"not arbitrary"* — it corresponds to `φ = 1.085 φ#` for PEG, exactly.

## The contradicting result

For the Alexander-de Gennes layer, the number of blobs stacked across the layer height is

&nbsp;&nbsp;&nbsp;&nbsp;**`L₀ / s = (Σ / π)^(5/6)`**

**identically** — the monomer size, the chain length and the grafting density all cancel. At the
conventional onset `Σ = 5` this is

&nbsp;&nbsp;&nbsp;&nbsp;**`L₀ / s = (5/π)^(5/6) = 1.4729`, for every polymer, every chain length, every thickness.**

At the four surviving `C-0001` design points — 5 nm, 7 nm, and the two edges at 10 nm — it is
**1.517, 1.485, 1.549 and 1.732**.

Proved as an exact identity in `BlobStackHeightTest`, not observed numerically:
`L₀/s = N a^(5/3) σ^(5/6)` and `Σ = π a² N^(6/5) σ`, so `(Σ/π)^(5/6) = a^(5/3) N σ^(5/6)`, term by term.

Provenance: `gpd/results/T-7-poroelastic-drainage.json`, `poroelastic.PoroelasticDrainageStudyKt`,
49 `poroelastic` tests green.

## Methodological grounds

### 1. The premise being violated is the picture's own construction, not one of its consequences

The Alexander-de Gennes brush is *defined* as a stack of space-filling blobs of size `s`: the free energy
is `k_BT` per blob, the osmotic pressure is `k_BT/s³` — one thermal energy per blob volume — and the
height is the blob diameter times the blob count. All of that presumes the blob count across the layer is
large enough for "stack" to mean something. It is 1.47.

A layer 1.5 blobs tall has a chain that is essentially **one blob plus a bit**, which is a *mushroom*
described in brush language. The `k_BT/s³` pressure scale, which `C-0001` uses directly and `C-0002`
corrects by a measured factor of 0.751, is a bulk-blob-density statement being applied to something that
does not have a bulk.

### 2. It is structural, not marginal — and it has the same shape as `CH-0001`'s finding

`CH-0001` showed that at fixed `Σ` the ratio `φ/φ#` is independent of layer height and chain length, so
`Σ = 5` lands the layer in the crossover *every time*, for PEG. The identity above is the same kind of
statement about a different quantity: at fixed `Σ` the blob-stack height is independent of *everything*,
for *every* material. The conventional brush criterion does not merely fail to deliver semidilute
thermodynamics — it does not deliver a stack of blobs either, and it never can, because the two are
inverse powers of the same `Σ`.

Inverting the identity, a genuine ten-blob stack needs

&nbsp;&nbsp;&nbsp;&nbsp;`Σ = π (L₀/s)^(6/5) = 49.8`,

**ten times the conventional onset.** `C-0002` already established that the grafting densities which reach
the des Cloizeaux regime are ruled out by §4(a) as far too stiff, and are physically unrealisable at 5 and
7 nm. This is the same tension arriving from the layer's **geometry** rather than from the solution's
**thermodynamics** — two independent routes to *"a compliant brush and a real brush may be mutually
exclusive for PEG"*.

### 3. It is corroborated by an entirely unrelated calculation

`T-7` needed the layer's hydrodynamic screening length. On the measurement-anchored correlation-length
model (`k = ξ²`, validated on PEG hydrogels by Offeddu et al. 2018), the 10 nm design point has
`ξ = 5.598 nm`, i.e. the layer is **1.79 screening lengths tall**. The blob identity gives **1.55**.
De Gennes argues that hydrodynamic and excluded-volume screening share one length, and here two routes
that share no inputs beyond the material sheet agree to 15 % on "this layer is under two screening
lengths thick".

## What follows, and what does not

**Does not follow.** That `C-0001`'s arithmetic is wrong, or that its direction is wrong. Nothing here
touches its numbers. Nor does it follow that the layer is *stiffer* or *softer* than `C-0001` says: unlike
`CH-0001`, this challenge does not carry a signed correction. A one-blob-thick layer's compression law is
simply **not derivable from the blob picture at all** — the pressure is a surface-interaction problem, not
a bulk-osmotic one, and which way it moves is exactly what `T-1c` has to find out.

**Does follow.**

1. **`Σ ≥ 5` is not defensible as a brush criterion on geometric grounds either.** `P-5` was resolved in
   substance by `CH-0001` in favour of stating the criterion on `φ/φ#`. This adds a second falsifiable
   statement it should carry: the blob-stack height `(Σ/π)^(5/6)`. A criterion that admits 1.5-blob layers
   is a criterion for coil overlap, not for brush behaviour — which is precisely what Hansen et al. say
   about the thermodynamic version.
2. **`T-1c`'s crossover-valid free energy must also be valid at low blob count**, not only at
   `φ/φ# ≈ 1`. Swapping the osmotic exponent does not fix a layer that has no bulk.
3. **`T-2` must not treat the `Σ = 5` edge as a physical boundary.** It is the lower edge of `C-0001`'s
   window and therefore sets the window's width, and it is now challenged on two independent grounds.
4. **The Milner-Witten-Cates SCF model is challenged by the same observation**, and harder: strong
   stretching assumes `L₀ ≫ R_F`. At the design points `L₀/R_F` = 1.18, 1.17, 1.19, 1.25 — the layer is
   barely stretched at all. `C-0001` carries SCF as one of four models; this says it is outside its own
   premise too.

## If this challenge is itself wrong

The way it fails is that the blob-stack count is not the right diagnostic: the Alexander-de Gennes result
is known to survive well below its nominal domain because the two errors it makes — treating a
non-uniform density profile as a box, and a marginally-stretched chain as strongly stretched — partly
cancel, and the height relation is experimentally robust (`C-0002` cites two independent unconstrained
fits returning `a = 0.356 ± 0.07` and `0.330 ± 0.15` nm on real PEG brushes). If so, the identity above is
a curiosity about the convention rather than a defect of the model, and the right response is to state the
blob-stack height alongside `φ/φ#` as a validity diagnostic rather than to change anything.

That would still be an improvement on the current situation, in which neither is stated.
`T-1c` is where it gets decided.

## Effect on `C-0004`

None. `T-7`'s own result does not use the Alexander-de Gennes free energy: it takes the layer stiffness as
a *parameter* and reports `τ ∝ 1/k_layer`, precisely so that this kind of finding moves the answer by a
stated factor rather than invalidating it. If `T-1c` softens the layer fourfold, the worst-case corner
frequency falls from 22.6 kHz to 5.6 kHz and poroelasticity is still not binding.
