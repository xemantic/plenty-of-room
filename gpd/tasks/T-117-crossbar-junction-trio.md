# T-117 — Do THREE 90° junctions close on ONE crossbar duplex?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Raised by** | [`C-0048`](../claims/C-0048-truss-cap.md), open item 1 — *"whether three 90° junctions close on one 13 bp crossbar is open exactly as the pair was before `C-0042`. It is that claim's question at the other end of the same legs, on a **lone** seat duplex with no lattice neighbours"* |
| **Verification type** | **in-silico** (a deterministic closure search over **three** junctions on one **finite, lone** seat duplex, on `C-0029`'s own backbone geometry and its own admissibility test) **+ logical** (four cheap bounds, all closed form: a truncated seat contact, a solid–solid clearance between two convex bodies, a **chord-twist quantisation**, and a duplex free energy) **+ literature** (the nearest-neighbour thermodynamics fetched and read for this task, plus recorded negatives) |
| **Numbering** | claim **`C-0052`**; the challenge number assigned by the coordinator (`CH-0064`) was **taken by `T-118` in the same iteration**, so this task takes **`CH-0067`**, the next above the highest visible (`CH-0066`) |

---

## Formulate

### The question, and why it is not `C-0042`'s with one more body

`C-0042` seated **two** 90° junctions on **one sheet duplex** and found the pair *loose*: it closes at every separation from the 6 bp steric floor up, with both chords exactly on the flexure axis and zero unpaired nucleotides. `C-0048` then showed that the truss's **cap** cannot be the flexure and cannot be a spring — a steric count forces it to be a **separate crossbar duplex**, `w + 2R` long, hosting **three** 90° junctions and **six** covalent links, its axis one duplex radius above the leg heads — and did not route it.

Three things make this harder than `C-0042`'s search, and each is handled rather than assumed:

1. **The seat is LONE and FINITE.** `C-0042`'s seat was a sheet duplex with neighbours at ±2.69 nm, a 32 bp crossover phase lattice and a shared lateral seat. A 13 bp crossbar has **none** of that. What replaces them: the neighbour terms of `seatFaceHeight` vanish (a lone seat's face height is `R` for every offset inside the rim, exactly); the crossover-occupancy count is **vacuous** (a lone crossbar carries no crossovers, so `crossoverFreePhaseCount` has nothing to count); and the shared lateral seat becomes the statement that **one straight crossbar gives both legs the same lateral offset by construction**. In exchange the crossbar brings **two freedoms the sheet duplex does not have** — its own **helical phase** `Φ` and its own **axial phase** `t` are continuous, because it is a free body (`CH-0056`) — and **one constraint the sheet duplex does not have**: an **axial rim** at `±L/2`, past which there is neither seat nor phosphate.
2. **The three junctions arrive in different directions.** Two leg heads from **below** (their terminal base-pair planes horizontal, chords in the `x–y` plane) and the flexure's own end from the **side** (its terminal plane vertical, chord in the `x–z` plane). They are not three instances of one problem: the legs need phosphates on the crossbar's **underside** and the flexure needs them on its **flank**, and one helical phase must serve all three.
3. **13 bp is short.** `C-0042` introduced `seatContactLength` because an unbounded search parks its optimum on the **rim** of the seat, where the flat end face's line contact `2√(R² − y_c²)` has collapsed to a point — legal by face height, a mechanism in fact. On a 4.42 nm crossbar the axial rim is 1.02 nm from each leg's axis, so the exclusion is **load-bearing** and is written here as a **truncated** contact, `|[x_c − a, x_c + a] ∩ [−L/2, L/2]|` with `a = √(R² − y_c²)`.

### Geometry and sign conventions (restated, not inherited)

The sheet is the `x–y` plane and `z` its normal. **The crossbar runs along `x̂`**, its axis at `y = 0`, `z = 0` of the cap's local frame; **the flexure's own axis is `ŷ`**. This is `C-0042`'s frame unchanged — its seat duplex also runs along `x̂` with the flexure along `ŷ` — and it maps onto `C-0048`'s by `x̂_here = ŷ_C-0048`, `ŷ_here = x̂_C-0048`.

- The **legs** stand along `ẑ`, their axes at `(x = ±w/2, y = y_c)`, their terminal base-pair planes at `z = −h_f` where `h_f` is the lone seat's face height. Their termini lie on a circle of radius `r_P` in that horizontal plane, at azimuths `ψ` and `ψ + Δ` measured in `x–y` from `x̂`.
- The **flexure** arrives along `−ŷ`, its axis at `(x = 0, z = z_c)`, its terminal plane at `y = −h_f`, termini at azimuths `ψ_f`, `ψ_f + Δ` measured in `x–z` from `x̂`.
- A **chord** is a **line**: every azimuth is folded modulo `π` and every misalignment lies in `[0, π/2]`.
- **The design's demands**, from `C-0048`'s recommended design: the leg-head chords lie **along `x̂`** (i.e. *across* the flexure axis — the azimuth that puts the strong constant in the **free** plane, worth 1.95/1.46 against 1.48/1.12); the flexure's own end chord lies **along `ẑ`** (vertical — the only orientation that restrains the flexure's own bending, which is in the `y–z` plane about `x̂`); and, from `C-0042`, each leg's **base** chord lies along **`ŷ`**, the flexure axis.
- **Units**: nm, pN, pN·nm, pN·nm/rad, pN/nm; `k_BT = 4.141947 pN·nm` at **T = 300 K**; aqueous **2 mM MgCl₂**; free energies in kcal/mol **and** `k_BT`, with `1 kcal/mol = 1.6776 k_BT` at 300 K.

### Acceptance predicates

| | predicate |
|---|---|
| **`Q1`** | **all six links close covalently** — every terminus within the **measured** `[0.60, 0.70] nm` phosphodiester step of a crossbar phosphate, **zero** unpaired nucleotides |
| **`Q2`** | the **six targets are distinct** phosphates |
| **`Q3`** | no phosphate pair closer than the van der Waals separation, 0.35 nm, anywhere in the assembly |
| **`Q4`** | every junction's **truncated** seat contact is above the floor `C-0042` adopted, so no junction is balanced on the crossbar's rim |
| **`Q5`** | the three **bodies** clear each other: leg–leg by the steric floor, and **leg–flexure** by a solid-to-solid separation that is reported against the 0.54–0.69 nm surface gap packed origami duplexes actually keep |
| **`Q6`** | the closure is achieved at the **azimuths the design demands** — or, if it is not, the misalignment is priced through `C-0048`'s own solver and the verdict is re-read |
| **`Q7`** | the crossbar is a **structural member**: its duplex free energy is stated, with the length that would be needed if it is not, and what that length costs |

**A negative on `Q1`–`Q3` closes `C-0037`'s branch on chemistry rather than mechanics**, and is as admissible an answer as a positive.

---

## Plan

### The four cheap bounds, which run before any search

| | bound | why it is cheap | what would falsify the approach |
|---|---|---|---|
| **1** | **the truncated seat contact.** `2√(R² − y_c²)` intersected with the crossbar's own extent | closed form, one line | it never truncates — i.e. the rim is not near any junction — in which case `C-0042`'s unbounded exclusion transfers unchanged and bound 1 was not worth writing |
| **2** | **the leg–flexure solid clearance.** Both bodies are a cylinder ∩ a half-space, hence **convex**, so alternating projection converges to the exact closest pair | two closed-form projections and a fixed point | the clearance is comfortably above one interhelical surface gap at every separation, in which case the flexure's entry between the legs is free and only the phosphates bind |
| **3** | **the chord-twist quantisation.** A leg is **one body with two junctions**, so the azimuths of its base chord and its cap chord are **not independent**: they differ by `m × 33.74°` for a leg of `m` base-pair steps, folded modulo `π`. The same statement applies to the **flexure**, whose two ends sit on two crossbars | integer arithmetic; no solve | the residual `‖m·twist − 90°‖` is small at every `m` in the envelope, i.e. the constraint costs nothing and `C-0048`'s two azimuths were independently choosable after all |
| **4** | **the crossbar's duplex free energy**, from published nearest-neighbour parameters | a sum of 12 table entries | the spread over sequence is smaller than the distance to marginality, i.e. the answer is a property of length alone |

**Bound 3 is the one written to bind, and the cost justification is that it can settle a design variable before any geometry is solved.** `CH-0056` established that the azimuthal quantum belongs to the **sheet** and not to a free standoff's chord — because a free-standing duplex with **one** junction has no inherited phase. A duplex with **two** junctions has no inherited phase either, but its two chords are rigidly related, and that relation *is* quantised. The bound is arithmetic and costs nothing; if it binds, it moves `C-0048`'s recommended leg length before the expensive search is run.

### The search

`C-0042`'s search, extended in the three ways the geometry demands and **not** in any other:

- the seat is a **finite lone duplex** of `L` base pairs at continuous helical phase `Φ` and continuous axial phase `t`;
- **three** junction bodies, two horizontal-chorded and one vertical-chorded, each with its own azimuth;
- the six links are checked for **mutual** distinctness and the whole assembly for van der Waals contact, not each junction alone.

Deterministic by construction, exactly as `C-0042`: fixed grids, fixed local refinement, strict comparisons so the lowest index wins every tie, no floating-point tolerance in the control flow. Two azimuth modes are run and reported separately:

- **`FREE`** — every junction's azimuth free. This answers the acceptance question as literally posed: *do three 90° junctions close on one crossbar duplex?*
- **`LOCKED`** — the two legs' azimuths fixed by `C-0042`'s solved base placement carried up the leg through bound 3's twist. This answers the **design** question, and it is strictly harder: the two legs then have **no** azimuthal freedom at all and the six links must be closed by the crossbar's own phase, its axial offset, the shared lateral seat and the flexure's own azimuth alone.

### Why not something more expensive

An oxDNA or atomistic study cannot answer this. `C-0029`'s reasoning applies unchanged: the closure test is a **necessary** condition — a phosphate pair inside the measured step with no van der Waals overlap — and a coarse-grained model could only find the junction *additionally* frustrated. The one thing a finer model would add is the backbone **torsion**, which is `T-71` and is an unchanged ceiling on everything here, and `C-0042` already records that its aligned pair's binding link sits at the **C2′-endo** end of the window, where that check is least comfortable.

### Declared falsifiers

| # | falsifier |
|---|---|
| **1** | **the trio not closing at any crossbar length** — then `C-0048`'s cap does not exist and `C-0037`'s branch closes on chemistry |
| **2** | **the trio closing only at the free azimuths** and not at the design's — then the cap's azimuth is not a free design variable and `C-0048`'s sensitivity table has to be re-read at the azimuth the routing permits |
| **3** | **bound 3 not binding** — the leg's two chords independently choosable, so the recommended design stands unchanged |
| **4** | **the reductions failing** — the search not reproducing `C-0042` when the crossbar is made long and the flexure removed |
| **5** | **the crossbar not being a duplex at 300 K** at any length that fits the plan |

### Pre-registered prediction

The trio closes; the binding constraint is not the phosphate reach but the **axial rim**, so the crossbar has to be longer than `C-0048`'s minimum; bound 3 **binds**, and it binds hardest at exactly `C-0048`'s recommended leg length; and the crossbar's stability is bought by **overhang**, which is mechanically free because `12EI/w` and `4C/w` both carry the leg **separation** and not the crossbar's length.
