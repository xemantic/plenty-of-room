# C-0099 — **IT DOES NOT MATTER, AND THAT IS A MEASUREMENT RATHER THAN A HOPE.** Destroying the dihedral spring of **all 14** row-end crossovers of the buildable 38.08 nm tile — from `C-0009`'s interior `13.5294118 pN·nm/rad` to **exactly zero** — moves the best 34-root dishing from **0.0621469105** to **0.0651753854** of the stroke — a ratio of **1.0487309**, from those two emitted numbers — against `T-5b`'s 0.10 which is never approached: the tile still has **34.8 %** of the convention unused at zero row-end stiffness. `CH-0111`'s bracket is **2.70925468×** wide and only **2.85 %** of it is reachable, because `C-0090`'s two readings differ in **three** things and only one of them is a stiffness — decomposed here as **2.85 % the dihedral spring, 97.40 % the vertical link, −0.25 % the mesh node**. The vertical link is a *constraint* expressing that the backbone is covalently continuous across the interface, which `C-0095` has already settled that it is, and Rothemund's own remedy for the edge strain (*"one or two scaffold bases could be left unpaired"*) adds slack to the **torsion**, not to the connectivity. **The response is monotone** — 16 of 16 consecutive pairs over two channels — so the threshold exists and the threshold is that **there is no crossing**. No accessible source gives a row-end crossover's `k_θ` and none is needed: the **ceiling is a count** (`k_θ = 2αB/(100a)` is two softened phosphate bonds and `C-0029` caps a duplex end at two strand termini, so `s ≤ 1` exactly, with `α` and `B` cancelling) and the **threshold is empty**

| | |
|---|---|
| **Task** | [`T-164`](../tasks/T-164.md), raised by [`C-0095`](C-0095-row-end-crossover.md) / [`CH-0111`](../challenges/CH-0111-the-row-end-crossover-is-admitted-with-an-interior-crossover-s-stiffness.md) |
| **Leaf** | **`A8.2`** (the plan and lattice model the anchoring array is written on), with **`A1.2`** |
| **Verification type** | **logical** (a bond count and a constraint/elasticity distinction, both closed form, which bound the reachable set before any solve) **+ in-silico** (`C-0090`'s exhaustive centro-symmetric placement pipeline re-run at 38.08 nm / phase 8 on **21** softened lattices, **163 296** placements each — **3 429 216** in all — through `C-0058`'s Woodbury bank) |
| **Verdict** | **PASS, and the acceptance is met in the strongest of its two branches: the sweep exists, it is monotone, and it never crosses.** **The cheap bound was half the answer and it cost no solve.** `C-0090`'s *refused* reading deletes the dihedral spring, the vertical link **and** the mesh node; `C-0095` has settled that the crossover **exists**, so its link — which `C-0009` itself calls *"a **constraint** … carries no rigidity at all"* — is present at full value whatever the strain relief does to the torsional register. That puts the refused reading **outside** the reachable set before anything is computed, and the sweep then measures how far outside: **97.40 %** of `CH-0111`'s interval is the link, **−0.25 %** is the mesh node, and **2.85 %** is the only elastic element of the three. **The sweep is the other half.** Nine rungs on each of two channels: channel A (the spring alone, the link retained) runs **0.0651753854 → 0.0621469105** and is inside `T-5b`'s 0.10 at **9 of 9**; channel B (both elements) is a **step and not a ramp**, reading 0.0651072886 at `s = 0.125` and **0.168640591** at `s = 0` exactly, because the link is a penalty enforcing a constraint and an eighth of a penalty still enforces it. **The counting ceiling and floor are then decoration** — the answer does not need to know whether a raster turn carries one bond or two. Raises [`CH-0115`](../challenges/CH-0115-the-row-end-bracket-is-a-constraint-not-a-stiffness.md) and **discharges `CH-0111`**. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The row-end crossover's `k_θ` is still unknown and is still not given by any accessible source; what is established is that the answer does not depend on it. The decomposition is **linear** and carries no initial stress, so Rothemund's *"crossovers in tension"* — a **prestrain** — is outside this model and is `T-172` |
| **Provenance** | `gpd/results/T-164-row-end-crossover-stiffness.json`, produced by `anchoring.RowEndCrossoverStiffnessStudyKt`; model in `src/main/kotlin/structure/CrossoverSoftening.kt` and `src/main/kotlin/anchoring/RowEndCrossoverStiffness.kt` (**both new**) plus **one defaulted parameter** added to `structure/OrigamiGrillage.kt` (`softenedCrossovers`, empty by default, asserted bit-identical to the unmodified lattice); `BuildableRasterWidth.kt`, `UpwardRootPlacement.kt`, `CrossoverLayout.kt`, `Gen1Tile.kt` and `RowEndCrossover.kt` were **read, not edited**; **7 cheap-bound records, 18 sweep records, 16 monotonicity records, 2 crossing records, 4 references, 4 convergence records, 6 upstream reproductions, 5 predicates, 6 falsifiers, 8 findings**; **13 gate-named tests in `src/test/kotlin/anchoring/RowEndCrossoverStiffnessTest.kt`**, red-checked (with the hinge softening deliberately ignored in a snapshot, **2 of 13 fail**, so the tests are not vacuous); `tools/verify.sh` run on its own isolated tree with **no `--drop-file` at all** (so its four post-Gradle gates ran): **2 365 tests, 2 failures, both in `stability/DoublingLadderRepairTest`** and both reading `gpd/results/T-149-*.json`, which the concurrent `T-167`/`C-0101` re-emitted in this same iteration — **no failure in any `anchoring` or `structure` class**; the result file **re-run through the same isolated snapshot and diffed BYTE-FOR-BYTE IDENTICAL** across two independent JVM runs; `tools/check-markdown-tables.py` clean over 304 files; `tools/result-reader-census.py --emit` re-run |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; single-layer **square-lattice** Rothemund sheet, **15 duplexes** at the SAXS 2.69 nm, 0.34 nm rise, 32/3 bp per turn, 16 bp column pitch, 32 bp per-interface spacing; along-helix width **38.08 nm** (112 bp, `C-0086`) at crossover **phase 8**; `C-0090`'s buildable 24-rise **8.16 nm** arm at `C-0055`'s **34** roots; `C-0017`'s **33.3333 pN/nm** mandate shared equally; `C-0022`'s solved collar at 2 mM, a 10 nm gap and 0.192 V, **as `C-0090` carried it**; `C-0001`'s foundation secant; free stroke **5.15473846 nm** |
| **Consumes** | [`C-0090`](C-0090-buildable-raster-width.md) (`rasterColumnLayout`, `rasterUpwardSites`, the 38.08 nm width, the 24-rise arm, phase 8 — **re-run as libraries**; its two readings **and its optimum placement key** read from its result file), [`C-0095`](C-0095-row-end-crossover.md) (the row-end crossover exists; the 14/42 scaffold/staple split), [`C-0009`](C-0009-discrete-lattice-tile.md) (`OrigamiGrillage`, `k_θ`, the vertical link as a constraint), [`C-0029`](C-0029-perpendicular-junction-routing.md) (the two-termini counting theorem), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`), [`C-0063`](C-0063-upward-root-placement.md) (`centroSymmetricPlacements`, `UpwardRootInfluenceBank`), [`C-0055`](C-0055-unused-junction-site.md) (the 34, the arm), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved collar, **read from its result file**), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 56), [`C-0017`](C-0017-output-coupling-stiffness.md), `Gen1Tile` |
| **Raises** | [`CH-0115`](../challenges/CH-0115-the-row-end-bracket-is-a-constraint-not-a-stiffness.md), against `CH-0111`'s Ground 2 and `C-0090`'s *"the end-of-row convention is a MODELLING CHOICE"* |

---

## The claim, in one line

**The question was asked as *"how stiff"* and the answer is *"stiff enough at zero"* — because the thing that separates `C-0090`'s two readings by 2.7× is not a spring at all but a covalent tie, and the spring is worth 2.85 % of it.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**,
  pressure **pN/nm²** (= 1 MPa exactly); `k_BT = 4.141947 pN·nm` at 300 K, aqueous 2 mM MgCl₂.
- `x` runs **along** the helices, `y` **across** them, `z` normal and positive **upward**; `w` positive **downward**.
- **A crossover is TWO elements** (`C-0009`): a dihedral spring `k_θ` resisting the relative roll of
  the two duplexes it joins, and a **vertical link** tying their surfaces together, which is a
  *constraint* carried by a penalty and contributes no rigidity of its own.
- **`s`** is the dimensionless **row-end softening**, `k_θ,row-end / k_θ,interior`, applied to the
  crossovers of the two end columns only — **14** of the lattice's **56**.
  **Channel A** scales the dihedral spring and retains the link; **channel B** scales both.
- **Dishing** is the peak of the deflection field with its best-fit plane removed, over the free
  plate's mean descent under the same uniform load — `C-0063`'s normalisation, unchanged.

---

## Deliverable 1 — the cheap bound, which is a count and a distinction, and ran before any solve

### (a) A ceiling that is a ratio of two counts

`Gen1Tile.crossoverHingeStiffness(α) = 2 α B/(100 a)` is Chen et al.'s **two** softened crossover
phosphate bonds in parallel. `CLAUDE.md`, from `C-0029`: *"A duplex END has exactly two strand
termini … It is a count, and no force field can add a third."*

> **A row-end crossover cannot carry more bonds than an interior one, so `s ≤ 1` — exactly.**
> `α`, `B` and the rise all cancel: it is a ratio of two integers.

| bonds | `k_θ` | `s` |
|---|---|---|
| 0 | 0 pN·nm/rad | 0 |
| 1 — a single-strand turn | **6.76470588** | **0.5** |
| 2 — an interior antiparallel crossover | **13.5294118** | **1.0** |

### (b) A floor on the OTHER element, which is a distinction rather than a number

`C-0009`: the vertical link *"is a **constraint** tying two duplex surfaces together and carries no
rigidity at all"*, and `CLAUDE.md` records that its penalty value *"the answer must not depend on"*.
`C-0095` has settled that the row-end crossover **exists** — it is the raster turn, a strand
passing from one helix to the next — so the two duplexes **are** tied at that base pair.
Rothemund's own remedy, *"one or two scaffold bases could be left unpaired and allowed to form a
hairpin that should relax the crossover"*, adds slack to the **torsion**; it does not cut the
backbone.

> **So the physically reachable set is channel A over `s ∈ [0, 1]`, and `C-0090`'s refused reading
> is OUTSIDE it.** That is `CH-0111`'s bracket refuted before a single placement is enumerated.

### (c) An unrelieved strain is a PRESTRAIN, not a compliance

Rothemund's *"crossovers in tension"* and *"how the strain is actually relieved is unknown"*
describe a **static configuration**. A linear elastic constant is a different object, and nothing
in the passage moves one. Which is why the deliverable is `P-6`'s **ceiling plus threshold** rather
than a value — and why the prestrain itself is named as an open item rather than smuggled in.

**Cost justification.** (a)–(c) are three paragraphs and no solve, and they decide the shape of the
answer. `CH-0111` ranks an oxDNA or all-atom edge crossover **third** behind this sweep, and after
the sweep it should not be spent at all: it would refine a quantity worth 2.85 % of an interval the
verdict does not cross.

---

## Deliverable 2 — the sweep, and monotonicity MEASURED rather than assumed

Exhaustive over the centro-symmetric 34-root family at 38.08 nm / phase 8 — **163 296 placements
at every rung**, `C-0090`'s own enumeration with `C-0090`'s own tie-break.

| `s` | `k_θ` (pN·nm/rad) | **channel A** best dishing/stroke | inside 0.10? | **channel B** | inside 0.10? |
|---|---|---|---|---|---|
| 0.000 | 0.000 | **0.0651753854** | **yes** | **0.168640591** | **no** |
| 0.125 | 1.691 | 0.065113784 | yes | 0.0651072886 | yes |
| 0.250 | 3.382 | 0.0644132595 | yes | 0.0644107011 | yes |
| 0.375 | 5.074 | 0.0638503242 | yes | 0.0638489184 | yes |
| 0.500 | 6.765 | 0.0633880137 | yes | 0.0633871783 | yes |
| 0.625 | 8.456 | 0.063001473 | yes | 0.0630009819 | yes |
| 0.750 | 10.147 | 0.0626735024 | yes | 0.0626732229 | yes |
| 0.875 | 11.838 | 0.0623916969 | yes | 0.0623915726 | yes |
| 1.000 | 13.529 | **0.0621469105** | **yes** | **0.0621469105** | **yes** |

> **`decreasingInStiffness` at 16 of 16 consecutive pairs, both channels.**
> The declared falsifier `F1` did **not** fire.

**And no variational argument would have settled that.** Adding stiffness lowers an *energy*; the
objective here is a **peak dishing** — the maximum of a residual field after a best-fit plane is
removed — which is not an energy, and it is then **minimised over 163 296 placements**. Monotonicity
had to be measured, and `CLAUDE.md`'s standing warning (*"a verdict that is not monotone in a swept
variable has no threshold"*, `C-0070`) is exactly why.

**Channel B is a STEP, not a ramp.** 0.0651072886 at `s = 0.125` against 0.168640591 at `s = 0`
exactly: an eighth of a penalty still enforces its constraint. The bisection asked for the crossing
returns the bracket `[0, 0.015625]`, which locates a **discontinuity** and not a threshold — and
that is the cleanest statement of why the 2.7× is binary.

---

## Deliverable 3 — the crossing, which does not exist on the channel that matters

| channel | crosses `T-5b`'s 0.10 inside `[0, 1]`? | bracket | against the counting floor `s = 1/2` |
|---|---|---|---|
| **A — the dihedral spring alone** | **NO** | — | **not reached at any `s`, including 0** |
| B — both elements | yes, at `[0, 0.015625]` | 0.015625 | below it, and it is the penalty step, not a stiffness |

> **The threshold is that there is no threshold.** A row-end crossover would have to lose its
> *vertical link* — i.e. not exist — for the 38.08 nm tile to leave `T-5b`'s convention, and
> `C-0095` has settled that it exists.

At `s = 0` the tile retains `(0.10 − 0.0651753854)/0.10 = **34.8 %** of the convention unused;
at `s = 1` it retains **37.9 %**. The whole unknown is worth **3.0 percentage points of margin**.

**`F4` did not fire**, and it could not have: there is no crossing to lie above the counting floor.

---

## Deliverable 4 — the decomposition, which is what makes the negative result readable

| what is removed from the 14 row-end crossovers | dishing/stroke | share of `CH-0111`'s interval |
|---|---|---|
| nothing — `C-0090`'s admitted reading | **0.0621469105** | — |
| the dihedral spring | **0.0651753854** | **2.85 %** |
| the dihedral spring **and** the vertical link | **0.168640591** | **97.40 %** |
| the whole column, on its own six-column host — `C-0090`'s refused reading | **0.168371808** | **−0.25 %** |

**The vertical link is 34× the dihedral spring, and the mesh node is worth −0.25 %** — the sign
being negative because two extra nodes near the rim refine the discretisation slightly. `F3` did
not fire: channel B's `s = 0` and the refused reading agree to **0.16 %**, which is exactly the
statement that they are the same mechanics on two meshes.

The free (uncoupled) tile moves the same way and by the same amount: 0.299034765 at `s = 1` against
**0.314026489** at `s = 0`, **+5.0 %**, against the coupled optimum's +4.9 %. The coupling is neither
amplifying nor suppressing the row-end softening.

---

## Deliverable 5 — the DESIGN does not move, which is the exposure that would have been worse

**`F5` FIRED, at 3 of 18 rungs, and it is bounded.** The exhaustive optimum returns a placement
other than `C-0090`'s at channel A `s = 0` and channel B `s = 0` and `s = 0.125`.

| rung | family best | `C-0090`'s own 34 roots | penalty | published placement flat? |
|---|---|---|---|---|
| A, `s = 0` | 0.0651753854 | **0.0660509839** | **`8.755985E-4`** | **yes** |
| A, every other rung | — | identical to the best | **0.0** | yes |
| B, `s = 0.125` | 0.0651072886 | 0.0651243992 | `1.71105888E-5` | yes |
| B, `s = 0` | 0.168640591 | 0.256628835 | 0.0879882439 | **no** |

**Keeping `C-0090`'s published placement at every rung of channel A costs at most `8.755985E-4` of the
stroke and stays inside `T-5b`'s 0.10 throughout**, so the recommended *design* is not a function of
the unmeasured parameter — only the *value* of its flatness is. The two rungs where the optimum
genuinely moves are the two where the row-end crossover is absent, not softened.

---

## The five verification gates

Executed as **13 gate-named tests** in `src/test/kotlin/anchoring/RowEndCrossoverStiffnessTest.kt`,
plus four in-study `check`s that abort the run and five strict reproduction gates.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a softening factor is dimensionless and its two elements are separate; the bond ladder carries `pN·nm/rad` and reproduces `Gen1Tile.crossoverHingeStiffness` at two bonds and half of it at one; unphysical arguments throw at **eight** entry points | **PASS** |
| **2 — limiting cases** | an **empty** softening map and an **all-`FULL`** one are bit-identical to the unmodified lattice; **`ABSENT` at a site set is bit-identical to `consumedCrossovers` at the same sites**, which is an independent code path — one deletes the element, the other multiplies it by zero; the row-end columns are `[0, 7]` when admitted and **empty** when refused; they carry **14** crossovers, one per interface, out of **56** | **PASS** |
| **3 — symmetry and conservation** | **a uniform load on a uniform foundation dishes `2.314e−7` of the free stroke, at every rung** — `C-0090`'s own `2.13e−7` at the same 0.05 nm inset, and the same conditioning statement; the row-end crossover set is invariant under the sheet's centro-symmetry | **PASS** |
| **4 — numerical convergence** | nested subdivisions 1 ⊂ 2 ⊂ 4 at `s = 1` (**0.63 %**) and at `s = 0` (**2.91 %**); the dishing sample grid 41/81/161 at `s = 1` (**0.42 %**) and `s = 0` (**0.91 %**) — all far below the 4.9 % the whole sweep spans, and all far below the 38 % of margin | **PASS** |
| **5 — literature and upstream** | `C-0090`'s **0.0621469105** at `s = 1` (departure **3.39e−11**) and its **0.168371808** on the refused host (**3.00e−10**) — both **checked at `1e−8` BEFORE serialisation**, because `roundedForResult`'s absolute floor emits a dimensionless departure as `0.0` (`CLAUDE.md`, and `C-0090` records the same); `Gen1Tile`'s **13.5294118 pN·nm/rad** from the bond count (**0.0**); `C-0095`'s **14** and `C-0015`'s **56** (**0.0**); `CH-0111`'s **0.356** recomputed as **0.356348561** | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** *(the declared one)* | the best 34-root dishing is **not monotone** in the row-end softening | **NO** | 16 of 16 consecutive pairs over both channels decrease in stiffness |
| **F2** | at `s = 1` the pipeline does not reproduce `C-0090`'s 0.0621469105 | **NO** | departure **`3.3864695769825204E-11`**, carried verbatim in `F2`'s own string |
| **F3** | channel B's `s = 0` limit differs materially from the refused reading | **NO** | 0.168640591 against 0.168371808, **0.16 %** — same mechanics, two meshes |
| **F4** | channel A's crossing lies **above** the counting floor `s = 1/2` | **NO** | there is no crossing at all |
| **F5** | the best placement **key** moves across the sweep | **YES** | **fired at 3 of 18 rungs and bounded**: keeping `C-0090`'s own placement costs at most `8.755985E-4` of the stroke on channel A and is flat at 17 of 18 rungs. Deliverable 5 |
| **F6** | a uniform load on a uniform foundation dishes more than `1e−6` of the free stroke | **NO** | worst over 18 rungs **2.314e−7** |

**What was not anticipated.** The task was formulated as *"locate the crossing"* and the crossing
does not exist, which was a possible outcome. What was **not** anticipated is *why*: the sweep's own
first rung — the softest state a crossover that exists can be in — lands 2.85 % of the way across an
interval the challenge had read as a stiffness bracket, and the remaining 97 % turns out to be a
covalent binary that `C-0095` had already decided. The second surprise is `F5`: the *design* moves
at exactly the rungs where the crossover is **absent** rather than soft, which is the one place the
argmin is genuinely near-degenerate.

---

## Validity range

- **TRL 1–3. Nothing here is measured**, and the row-end crossover's `k_θ` remains unknown. What is
  established is that the flatness verdict is insensitive to it over its entire admissible range.
- **THE DECOMPOSITION IS LINEAR AND CARRIES NO PRESTRAIN.** Rothemund's *"a crossover involving
  staple strands is in tension with an adjacent crossover involving the scaffold strand"* describes
  an **initial stress**, and `C-0009`'s lattice has no term for one — no initial strain, no
  geometric stiffness, no residual moment. This claim therefore answers *"what if the row-end
  crossover is softer"* and **not** *"what if it is prestrained"*. That is **`T-172`**, and it is
  the one route by which this verdict could still move.
- **The whole argument for channel A rests on one sentence of physics**: that a crossover which
  exists ties its two duplexes vertically. A frayed or melted terminal region would break it, and
  the evidence against that is Rothemund's *"the last base pair does form and assumes a planar
  configuration"* — read directly by `C-0095`, and a single sentence.
- **`C-0022`'s collar is `C-0090`'s, carried and not re-solved** at 38.08 nm. Unchanged from
  `C-0090`'s own open item 1, and it is `T-160`'s subject in this iteration.
- **Phase 8 only.** `C-0090`'s two admissible phases are 8 and 24; the sweep is run at the winner.
  Phase 24's admitted reading is 0.070693794, 13.8 % worse and still flat, so the same conclusion
  has 29 % of margin there rather than 38 % — but it is **not** re-swept here.
- **The `α = 1` reading of `k_θ`.** Chen et al. bracket `α` at 0.6–1.2 and this programme carries
  `α = 1`. That bracket is **inside** the sweep: `α = 0.6` on the row-end crossovers alone is
  `s = 0.6`, which the sweep brackets at 0.0633880137 (`s = 0.5`) and 0.063001473 (`s = 0.625`),
  and no value of `α` in any bracket reaches the convention.
- **The vertical link's penalty value is not a physical stiffness** and the study does not treat it
  as one; channel B is carried as the mechanical content of the refused reading, not as a sweep of
  a material parameter. Its `s = 0` rung is the only defensible point on it.
- **`OrigamiGrillage` gained one defaulted parameter.** `softenedCrossovers`, empty by default, and
  the empty map is asserted bit-identical to the previous behaviour. Nothing published moves.
- **The exhaustive family is the centro-symmetric one**, `C-0063`'s, at `minimumPerRow = 2`,
  `maximumPerRow = 3`. A non-symmetric placement could be better at some rung and is not searched;
  the comparison across rungs is on one family throughout, which is what a sweep needs.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `k_θ = 2αB/(100a)` and the `α = 0.6–1.2` bracket | 13.5294118 pN·nm/rad | **CITED, FITTED TO MEASUREMENT** — Chen et al., *JACS* **136**:6995, SI §S2; **re-derived here from the bond count** and asserted equal |
| `C-0090`'s two readings and its optimum placement key | 0.0621469105 / 0.168371808 | **`C-0090`, READ FROM ITS RESULT FILE, keyed on case and phase, and REPRODUCED** to 3.4e−11 / 3.0e−10 |
| `C-0022`'s solved collar terms | — | **`C-0022`, READ FROM ITS RESULT FILE**, keyed on concentration, gap and bias |
| the edge-strain passage and *"the last base pair does form"* | verbatim | **CITED, READ DIRECTLY** by `C-0095` — Rothemund 2006, Suppl. Note S2, `gpd/data/T-151-sources/` |
| the two strand termini at a duplex end | 2 | **`C-0029`, CITED** as a count |
| interhelical distance, rise, bp/turn, crossover spacing | 2.69 nm, 0.34 nm, 32/3, 16 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| `CH-0111`'s 0.356 | 0.356348561 | **`CH-0111`, RECOMPUTED** |

Everything else — the bond ladder and its ceiling, the constraint/elasticity distinction and the
reachable set it defines, all 18 exhaustive rungs, the monotonicity census, the two crossing
searches, the three-way decomposition, the published-placement penalties, the four convergence
axes and the uniform-load residual — is **derived here in code**.

## Still open — named, not answered

1. **The PRESTRAIN.** `T-172`. Rothemund's *"crossovers in tension"* is an initial stress and
   `C-0009`'s lattice has no term for one. It is the only route left by which the row-end crossover
   could move a verdict, and it is a different modelling question from this one.
2. **Phase 24 is not re-swept.** Its admitted reading has 29 % of margin rather than 38 %, so the
   conclusion is expected to hold with less room; it is not demonstrated.
3. **What a row-end crossover's `k_θ` actually is.** Unknown, unsourced, and — on this evidence —
   not worth an oxDNA campaign. If one is ever run for another reason, this claim predicts the
   flatness to within 4.9 % whatever it returns.
4. **`C-0022`'s collar at 38.08 nm**, `C-0090`'s open item 1, still carried.
5. **Whether the *coupling force* per path moves with the row-end stiffness.** Only the flatness is
   swept here; `C-0090`'s 1.42774664 pN peak crossover force is not re-read at `s = 0`.

## Challenges

**Raises [`CH-0115`](../challenges/CH-0115-the-row-end-bracket-is-a-constraint-not-a-stiffness.md)**
against `CH-0111`'s Ground 2 and `C-0090`'s framing of its two readings as a convention.

**[`CH-0111`](../challenges/CH-0111-the-row-end-crossover-is-admitted-with-an-interior-crossover-s-stiffness.md)
is DISCHARGED, in all four of its *"what would settle it"* items:**

1. *The sweep* — run, monotone, no crossing.
2. *The vertical link separately* — that is channel A, and it is the whole of the reachable set.
3. *An oxDNA or all-atom edge crossover* — **recommended against**: it would resolve a quantity
   worth 2.85 % of an interval the verdict does not cross.
4. *Rothemund's remedy priced as mechanics* — a relaxed crossover is a softer one, which is channel
   A moving toward `s = 0`, which is worth `0.0651753854 − 0.0621469105 = 0.0030284749` of the stroke and cannot lose the
   verdict.

**`C-0095`'s *Still open* items 1 and 4 are CLOSED** — the stiffness question is answered in its
consequence, and the response **is** monotone in it.

**`C-0090`'s *Still open* item 2 is closed for the second time**, now on the mechanics rather than
on the permission.

**None stands against this claim.** The five ways it would fail:

1. **A row-end crossover that does not tie its two duplexes vertically.** Then the state is channel
   B's `s = 0` and `C-0090`'s refused reading is right. One sentence of Rothemund is the evidence
   against it.
2. **A prestrain that stiffens or softens the lattice materially.** `T-172`.
3. **A non-symmetric placement family** in which the ranking across rungs differs.
4. **A different phase.** Phase 24 has less margin and is not re-swept.
5. **An interhelical distance of 2.73 nm**, which would move the whole lattice; it moves `C-0090`
   first and this claim only through it.
