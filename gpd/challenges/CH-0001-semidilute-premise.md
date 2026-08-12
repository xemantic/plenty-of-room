# CH-0001 — The layer is not in the semidilute regime `C-0001` assumed

| | |
|---|---|
| **Challenges** | [`C-0001`](../claims/C-0001-layer-stiffness.md), its `Semidilute` validity bullet and journal surprise `S-5` |
| **Raised by** | [`C-0002`](../claims/C-0002-peg-material-parameters.md), task [`P-3`](../tasks/P-3-peg-material-parameters.md) |
| **Raised** | 2026-08-12, iteration 2 |
| **Status** | **UPHELD in part** — see Resolution. `C-0001` is not withdrawn; its validity range is wrong and its numbers become bounds. |

---

## The standing statement being challenged

`C-0001`, validity range:

> **Semidilute.** Working volume fraction is φ ≈ 0.03–0.044, comfortably below the ~0.2–0.3 crossover,
> so the `m = 9/4` exponent is justified **for this layer at this operating point** — checked, not inherited.

and the corresponding entry in `JOURNAL.md`:

> **S-5. The semidilute premise survives contact with our own layer.** […] which is a checked premise
> rather than an inherited one, and it is the first of §2's caveats to actually close.

## The contradicting result

For PEG in water at the surviving `T-1` design points, the measured osmotic equation of state
puts the layer at **φ/φ# = 1.08–1.23**, inside the dilute→semidilute **crossover**,
with a local exponent of **1.66–1.71** rather than 9/4.
Under the §3 target force it compresses only to φ/φ# = 1.3–2.2 and `m_eff` = 1.73–1.92.
The des Cloizeaux domain begins at φ/φ# = 5 and is not reached at any point of the design space.

Provenance: `gpd/results/P-3-peg-material-parameters.json`, `material.PegMaterialStudyKt`, 119 tests green.

## Methodological grounds

Three, in increasing order of seriousness. The first two are errors of identification; the third is the one that matters.

### 1. The wrong crossover was checked

`C-0001` compared the layer's volume fraction against the **semidilute→concentrated** crossover
(the one at φ ≈ 0.2–0.3, where the exponent moves toward 3) and found it far below.
That comparison is correct and irrelevant. A solution of *finite* chains has a **second** boundary,
below it — the **dilute→semidilute** crossover, where the pressure stops being van't Hoff and
starts being des Cloizeaux — and the semidilute window is bounded by *both*.
Being far below the upper boundary says nothing about being above the lower one.

For our chain length the lower boundary sits at φ# ≈ 0.026, and the layer sits at 1.08–1.23 φ#.
`C-0001` was measuring its distance from the far wall.

### 2. The volume fractions compared were in different units

`C-0001`'s φ was computed as `N σ a³ / h` with the Alexander-de Gennes effective monomer length
`a = 0.35 nm`. That is a **reduced density** `n a³`, not a physical volume fraction, because
`a` is a *contour* length and `a³ = 0.0429 nm³` is not the volume of an ethylene oxide monomer,
which is `v₀ = 0.0604 nm³` (`C-0002`). The physical volume fractions are **1.408×** larger.
Comparing a reduced density against a literature volume fraction is a units error of 41%,
and it happened to run in the direction that made the premise look safer.

### 3. The chain-overlap criterion is not sufficient, and this is known for this material

This is the substantive ground. `C-0001` located its lower boundary with the reduced grafting
density `Σ ≥ 5` — coil overlap. Hansen, Cohen, Podgornik and Parsegian, fitting the *same*
Alexander-de Gennes form to PEG-brush compression data, state the point explicitly:

> the chain overlap condition φ ≃ φ\* = N^(−4/5) […] does not provide a sufficient criterion
> for the attainment of des Cloizeaux behaviour

and measure the actual onset at **φ# ≃ 0.15 for PEG-2000** and **0.07–0.09 for PEG-5000**,
against overlap concentrations of 0.05 and 0.02 — a factor of 3–4 higher than overlap.
They then require, as a precondition for applying the brush law at all:

> In order to invoke [the compressed-brush pressure], it must be established that the polymer size
> and density in the compressed brush are in a regime where bulk des Cloizeaux scaling applies.

That precondition was not established for our layer. It has now been checked, and it fails.

Worse, `P-3` shows the failure is **structural, not marginal**: at fixed `Σ` the ratio φ/φ# is
*independent of layer height and chain length* (proved in `PegWaterTest`), and at `Σ = 5` it equals
**1.085** for PEG in water — always. The conventional brush-onset criterion does not land you in the
semidilute regime for this material at any thickness. It lands you at the crossover, every time.

## What follows, and what does not

**Does not follow.** That `C-0001` is wrong about direction. Every correction found here makes the
layer **softer**, not stiffer:

- the local exponent is 1.66–1.92 against the 9/4 used, and `P(h) = (k_BT/s³)[(L₀/h)^m − (h/L₀)^(3/4)]`
  is monotonically increasing in `m` for `h < L₀`, so a smaller exponent means less pressure at
  every compression;
- the measured des Cloizeaux prefactor is **0.751×** the `k_BT/s³` de Gennes convention at the design point.

So **`C-0001`'s strokes are lower bounds and its design window is a lower bound on the window's width.**
Its headline — empty at 5 nm, empty at 7 nm, narrow at 10 nm — may be too pessimistic, and cannot be
too optimistic, from these two corrections alone.

**Does follow.**

1. **`m = 3` is excluded by measurement.** It is above the whole accessible range of `m_eff`. One of
   `C-0001`'s four models is dead, and it was the stiffest one.
2. **`C-0001`'s stiffness at first contact is ~19% high.** `k/A = (k_BT/s³)(m + 3/4)/L₀`, so replacing
   `m = 9/4` by `m_eff = 1.672` multiplies it by `2.422/3 = 0.807`.
3. **The premise under the *height* is also unestablished, not only the one under the exponent.**
   `L₀ = N a^(5/3) σ^(1/3)` is itself a consequence of semidilute blob structure — Hansen et al. are
   explicit that the linear `L₀(N)` relation "is largely a consequence of semi-dilute solution behaviour
   in the brush". Since `T-1` *inverts* that relation to get `N` from the specified layer height,
   the chain length underlying every number in `C-0001` rests on the same unestablished premise.
   **This cannot be repaired by changing an exponent.** It needs a brush free energy that is valid
   across the crossover — queued as `T-1c`.
4. **A layer that is a genuine des Cloizeaux brush is not reachable.** To reach φ = 5φ# a 10 nm layer
   needs σ = 0.99 nm⁻² (s = 1.0 nm, φ = 0.35), 41× the design window; a 5 nm layer needs
   σ = 3.96 nm⁻², which is not physically realisable. §4(a) rules such densities out for stiffness
   reasons anyway. **The compliant-brush window §4(a) asks for and the semidilute premise the brush
   theory needs may be mutually exclusive for PEG.** That is a `T-2` finding in the making and it is
   the reason this challenge is filed rather than noted.

## Resolution

`C-0001` is **not withdrawn and not overwritten.** Its arithmetic is correct and reproducible; its
verdict of PASS against the `T-1` acceptance predicate stands, since that predicate asks for a number
with a stated model and validity range, and both were stated.

What is corrected is the **validity range itself**, which claimed a premise as checked that was checked
against the wrong boundary in the wrong units. The claim is annotated in place with a pointer here,
per the no-overwrite rule; the numbers stay as they are, reclassified from *results* to *bounds*.

**Outstanding, and queued:**

- `T-1c` — re-derive the layer response with a crossover-valid free energy, not a fixed exponent.
  Until then `T-2` must treat `C-0001`'s window as a lower bound on width and say so.
- `P-5` — resolved in substance by this challenge: the brush criterion should be `φ ≥ φ#` on the
  measured equation of state, not `Σ ≥ 5`. The two are equivalent statements for PEG (`Σ = 5 ⇔ φ = 1.085 φ#`),
  which is why the convention was not *arbitrary* — but it is not *sufficient*, which is what matters.

**If this challenge is itself wrong**, the way it fails is that a grafted layer, having no chain
translational entropy, is semidilute at a lower density than a bulk solution of the same chains —
i.e. that φ# measured in bulk is an overestimate of the brush's own onset. That is a real possibility
and it is not settled here. It would move the boundary down, not remove it, and the layer would then
sit at the bottom edge of the semidilute regime rather than inside the crossover. `T-1c` is where it
gets decided.
