# CH-0125 — `C-0093`'s four-layer brick is mis-specified in three ways, and the net is not signed

| | |
|---|---|
| **Against** | [`C-0093`](../claims/C-0093-shared-body-coupling.md), the `four-layer honeycomb brick` body of its tie ladder, and the statement *"a buildable body loses where the rigid limit wins"* |
| **Raised by** | [`C-0109`](../claims/C-0109-four-layer-tile.md) (`T-191`), iteration 23 |
| **Grounds** | the body's three plate rigidities are each taken at a reading `T-191` shows to be wrong, and the three corrections do not share a sign |
| **Status** | **OPEN** — the re-solve is owed and is **not** performed here |
| **Also carried by** | `ANSWERS.md` §6 and row (g), `DECISIONS-FOR-NDI.md` (twice), `TASKS.md`'s `T-162` and `T-191` rows |

## What `C-0093` builds

`coupling/SharedBodyCouplingStudy.kt` builds the buildable shared body as

```
origamiSheet(INTERHELICAL_HONEYCOMB, CROSSOVER_SPACING_HONEYCOMB_BP,
             layers = 4, interlayerCoupling = InterlayerCoupling.RIGID)
```

and hands its plate to `SharedBodyModes.bendingStiffness`, which reads all three rigidities.
The body enters the result file as
`alongHelixRigidity 14310.776`, `acrossHelixRigidity 19.2216181`, `twistingRigidity 181.388013`, `thickness 9.608`,
and it is **1.564×** the rigid limit's condensed station compliance, reading **0.100166871** of the stroke at `C-0063`'s 34 stations.

## The three corrections

| | what `C-0093` uses | what `T-191` finds | direction |
|---|---|---|---|
| **`D_⊥`** | 19.2216 — the **uncoupled** value, because `OrigamiSheet` applies the parallel axis along the helices only ([`CH-0124`](CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md)) | **240.93** at the measured coupling, **758.25** at full composite action — **12.5× to 39.4×** higher | the brick is **stiffer** than priced, so `C-0093`'s verdict is **pessimistic** |
| **`D_∥`** | 14 310.78 — the **`RIGID`** limit | **4 547.18** at the measured `f = 0.30` — **3.15×** lower | the brick is **softer** than priced, so `C-0093`'s verdict is **optimistic** |
| **layer spacing** | 2.536 nm = `d` | a honeycomb array stacks rows at `d√3/2` = **2.196 nm**, so `Σy²` is over by `4/3` | the brick is **softer** again |

The first correction is far the largest **as a ratio**, and it acts on the **soft** axis of a body whose anisotropy is 18.87 —
which is the axis a plate's bending energy is most sensitive to.
But the second and third act on the axis that carries the load along the helices.
**Neither this challenge nor `T-191` claims to know the net**, and `T-191` does not re-solve the shared-body condensation.

## Why the `D_∥` correction is a measurement and not an opinion

`InterlayerCoupling.RIGID` is CanDo's assumption, and CanDo says so in the sentence both of this repository's duplex constants come from:

> "DNA origami nanostructures are modeled as bundles of isotropic elastic rods that are **rigidly constrained to their nearest neighbors at specific crossover positions**." — Kim, Kilchherr, Dietz & Bathe, *NAR* **40**:2862 (2012)

Four measured origami bundles put a real crossover-linked body at `f = 0.26–0.33` of the way from independent to composite —
Kauert, Kurth, Liedl & Seidel (*Nano Lett.* **11**:5558, 2011: 4HB square 740 nm, 6HB honeycomb 1880 nm),
Pfitzner et al. (*Angew. Chem.* **52**:7766, 2013: 6HB 2 µm),
Wang et al. (*JACS* **134**:1606, 2012: 6HB tile 1.0 µm) — and the last of these
**publishes the rigid-composite formula, names it a *"naïve model"* of *"rigidly linked rods"*, and measures it to over-predict by 2.7×**.
Kauert et al.'s own model swept four boundary conditions between the two limits and, as reported by
Li et al. (*Nanoscale* 2023, PMC10395309), *"concluded that the most reasonable conditions were the two partial attachments."*
Sources and the arithmetic are in `gpd/data/T-191-sources/`.

## A fourth, smaller point: the two numbers `C-0093`'s verdict compares are read at different rungs

`C-0093`'s verdict row, `ANSWERS.md` and `DECISIONS-FOR-NDI.md` all read
*"a four-layer honeycomb brick reads 0.100166871 where the rigid limit reads 0.0344013403"*.
`C-0093`'s **own** Deliverable 1 table shows those are different rows of the tie ladder:
0.100166871 is the brick at **100 pN/nm** ties and 0.0344013403 the rigid body at **1000**.
Matched at 100, the pair is **0.100166871 against 0.0354644034**, a ratio of **2.825** rather than 2.912.
The conclusion — body rigidity is first order — is unaffected; the number quoted for it is not the matched one.

## What would settle it

Re-running `coupling.SharedBodyCouplingStudyKt`'s tie ladder with the brick's plate built at
`D_∥ = 4547.18`, `D_⊥ = 240.93`, `D_k = 181.388` and, if the geometry is corrected too,
at the honeycomb layer spacing. It is **one body in an existing loop** and no new machinery.
Until then, *"a buildable body loses where the rigid limit wins"* rests on a body specified in three wrong places.
