# `T-137` — the measured weave, read directly

Every number below carries a **read flag**, per `CLAUDE.md`'s research practice.
Nothing here is taken from a search summary, and nothing is inherited from
[`gpd/data/T-134-tolerance-literature.md`](T-134-tolerance-literature.md) without being
re-fetched — except where that is stated in the row.

## Access notes

| source | route | outcome |
|---|---|---|
| Bai et al., *PNAS* **109**:20012 (2012) | `https://pmc.ncbi.nlm.nih.gov/articles/PMC3523823/` | **200, full text**, re-fetched by this task |
| Snodin et al., *NAR* **47**:1585 (2019) | `https://pmc.ncbi.nlm.nih.gov/articles/PMC6379721/` | **200, full text**, re-fetched by this task |
| Yoo & Aksimentiev, *PNAS* **110**:20099 (2013) | not re-fetched | **carried from `T-134`'s survey and flagged as such** |
| Fischer et al., *Nano Lett.* **16**:4282 (2016) | not re-fetched | **carried from `T-134`'s survey**; only the 2.73 nm square-lattice constant is used, as a cross-check target |

## Snodin, Romano, Rovigatti, Ouldridge, Louis, Doye, *NAR* **47**:1585 (2019)

**READ DIRECTLY**, `PMC6379721`.
oxDNA, **2D tile** — *this programme's own object* — at **300 K** and **`[Na⁺] = 0.5 M`**,
which is **not** this project's 2 mM MgCl₂ and is carried as a validity note.

### The definition of the quantity

> *"We quantify the weave pattern of the 2D tile by measuring the distance between the **helix
> axes (defined as the midpoint between the bases for each base pair)** for adjacent double
> helices."*

### The phase rule — the load-bearing passage for `T-137`

> *"Each group exhibits a wave-like pattern with **minima at the crossovers**, where the double
> helices are brought closest together, and **maxima away from the crossovers, normally at a
> position which is both midway between the junctions and where the adjacent pair of helices have
> a crossover**. This pattern has a **periodicity of about 32 base-pair steps**, corresponding to
> the periodic junction placement in the origami."*

This is the whole geometric input to `C-0076`'s phase model, and it is stated rather than assumed:
minimum at the interface's *own* crossovers, maximum at the *adjacent* interfaces' crossovers,
period 32 bp.

### The shape

> *"It is also interesting to note the **'triangular wave' character** of the weave plots. The
> bending that creates the weave pattern is mostly localized at the junctions with the
> **intervening sections basically straight**. This is in part because the junctions are
> relatively flexible compared to the duplex sections between the junctions, which being only a
> small fraction of the persistence length (typically only 16 bp long compared to the 125 bp for
> the duplex persistence length for the model) are very stiff."*

### The amplitude

> *"Assuming a perfectly triangular wave form and taking **1.5 nm as a typical value of the
> difference in the interhelix distance between the maxima and minima** of the weave pattern
> (Figure 3(a)) together with their **16 base-pair separation** gives θ = 7.85°."*

### The mean — and it disagrees with the SAXS lattice constant

> *"Taking the average between the maxima and minima of the triangular wave form gives an
> interhelix distance of about **3.25 Å** at `[Na⁺] = 0.5 M` (Figure 3A) and **3.1 Å** at the high
> ionic strength limit (Figure 4B), both at 300 K. … these authors were able to provide more
> quantitative data for a 10-helix bundle tube … implied an interhelix separation of ∼**2.95 Å**.
> … Rothemund also provided an estimate of **3 Å** for the interhelix separation based on AFM
> images."*

**READ DIRECTLY, meaning INFERRED**: the paper prints `Å` where it means `nm` throughout that
paragraph — 3.25 Å is a third of a bond length, and the comparison targets (Rothemund's 3 nm AFM
estimate, the 10-helix bundle's 2.95 nm) are unambiguous. Carried as **3.25 nm**, and flagged.

### The fluctuation about the deterministic pattern

> *"the fluctuations, which are smallest at the junctions and largest at the midpoints between the
> junctions, are **significantly smaller in magnitude than the variation in the interhelical
> distance due to the weave pattern itself**."*

### The edge duplexes, which the paper excludes

> *"we omit some inter-helix distances. Namely, those involving the double helices at the top and
> bottom edges of the origami, as these are **only constrained on one side and so exhibit slightly
> different behaviour**."*

This is why `WeaveProfile` carries an `edgeDuplexesStraight` reading, and why the claim reports the
node congruence under both.

### Three causes, none of them steric

> *"firstly, electrostatic repulsion between the negatively charged helices; and secondly, that
> the detailed local structure around the junctions favours the helical arms to bend slightly away
> from each other. A third possible contribution is an entropic effect … all three of these
> effects play a role"*,

and, on removing the electrostatics: *"the weave pattern remained, albeit with a **reduction in the
magnitude of the oscillations by about 20 %**."*

## Bai, Martin, Scheres, Dietz, *PNAS* **109**:20012 (2012)

**READ DIRECTLY**, `PMC3523823`. Cryo-EM of a **multilayer** square-lattice brick, 11.5 Å overall.

### The sawtooth, Fig. 3 E/F caption, verbatim

> *"(E and F) Schematic representation of the 3D chickenwire-like pattern found in the structure,
> depicting dsDNA helical stretches in gray and cross-overs in red. **The pattern was computed
> using the coordinates of base pair midpoints in the pseudoatomic model. The midpoints of
> neighboring dsDNA helices move on average from a minimum distance ⟨d min⟩ = 18.5 Å at the
> cross-over to a maximum distance of ⟨d max⟩ = 36 Å away from each other.**"*

### The shape, main text, verbatim

> *"we find that the helices are **not parallel**: on either side of the majority of cross-overs
> they enclose a nonzero angle within, but also out of the plane normal to the cross-over
> direction. Globally, this results in a **3D chickenwire-like pattern whereby individual dsDNA
> helices form diamond-shaped cavities in between cross-overs**."*

A diamond is two straight edges — the same triangular wave Snodin measures, from a different
method on a different object.

### Two further constants, in the same paper, unused by this programme until now

> *"The overall dimensions of the object and the extent of global twist … also agree with a
> prediction when using **2.6-nm effective helix diameter** and **10.44 bp/turn** reciprocal twist
> density."*

> *"in agreement with default B-form DNA geometry, we find that the **average distance from base
> pair to base pair midpoints is 3.35 Å** as derived from the helical distance between consecutive
> cross-overs that are resolved in the cryo-EM density."*

**READ DIRECTLY.** The rise is **1.5 % below** this project's 0.34 nm and the effective helix
diameter is a **sixth** reading of the lattice constant.

## Yoo & Aksimentiev, *PNAS* **110**:20099 (2013)

**CITED via `gpd/data/T-134-tolerance-literature.md`, NOT re-fetched by this task.**
All-atom MD: *"the DNA–DNA distance was found to range between 18 and 30 Å"*,
*"The mean interhelical distance at the junction (⟨d⟩) is 18.6 Å"*,
*"the rmsf values were less than 4 Å"*.

## The spread this leaves

| quantity | readings | spread |
|---|---|---|
| **peak-to-peak** | 1.2 nm (Yoo, all-atom) · **1.5 nm (Snodin, 2D tile)** · 1.75 nm (Bai, multilayer) | **1.46×** |
| **mean** | 2.6 nm (Bai's own design assumption) · 2.69 nm (SAXS sheet) · 2.725 nm (Bai's midpoint) · 2.73 nm (SAXS square) · 2.741 nm (Fischer SI `a_mean`) · 2.95–3.0 nm (10HB SAXS, Rothemund AFM) · 3.1–3.25 nm (oxDNA) | **1.25×** |
| **minimum** | 1.80 nm (Yoo) · 1.85 nm (Bai) · 1.86 nm (Yoo, at the junction) | **1.03×** |

**The minimum is the best-determined of the three**, and it is the one that lands on this
repository's own measured phosphate-backbone contact.

## `T-71`, this repository — the constant that adjudicates 1.85 against 2.0

**MEASURED**, not cited: `MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS` =
**0.9086378584708424 nm**, SD **0.0664994523721773 nm**, over the B-form C2′-endo population of a
13 084-linkage survey of 876 X-ray DNA-only RCSB entries (`tools/T-71-emit-kotlin-constants.py`).

Phosphate-backbone contact is twice that, **1.8172757 nm** — **1.8 % inside** Bai's 18.5 Å and
**0.35 population standard deviations** below it.
