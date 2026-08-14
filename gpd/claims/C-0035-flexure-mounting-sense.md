# C-0035 — "Which body carries the standoffs" is not a free choice and is not the variable: the midspan deflection IS the change in the two bodies' separation, so the sign is a kinematic identity, and exactly one of the four mountings is buildable

| | |
|---|---|
| **Task** | [`T-75`](../tasks/T-75-flexure-mounting-sense.md) (primary) and [`T-78`](../tasks/T-75-flexure-mounting-sense.md) (its immediate consequence — one task file covers both) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme |
| **Verification type** | **logical** (an exact kinematic identity on the §1 stack, with no free parameter, cross-checked against an independent coordinate construction) **+ in-silico** (the buildability filters and both escapes priced on `C-0030`'s own solved flexure) |
| **Verdict** | **PASS, and `T-75` is SETTLED — as a *determination*, not as a specification gap.** The flexure's midspan is tied to one body and its ends stand on the other, so its deflection **is** the change in the two bodies' separation and `dδ/ds = (v_base − v_driven)/n` is exactly `±1` with **no length in it**. The sign is therefore a **product of two binaries** — base body **and** standoff normal — of which `C-0030` named one: **of the four mountings exactly two are favourable, one with each base body and one with each standoff normal, so "which body carries the standoffs" decides nothing on its own.** Of the two favourable mountings, one puts the flexure **under the tile, inside the actuation gap**, where the 45-flexure array alone occupies **37–85 %** of the polymer layer's own volume and, at §3's 5 and 7 nm layers, sits **at or below the electrode surface**. **The survivor is unique: the standoff bases stand on the OUTPUT SUPERSTRUCTURE, the standoffs point AWAY from the tile, the flexure is OUTBOARD of its own ground, and each midspan is tied back DOWN through that ground to the tile.** Both adverse mountings fail `C-0023`'s 40 pN/nm ceiling at every length inside `C-0017`'s envelope **and** cannot place §3's own effort point — inboard, the effort point cannot come closer to the tile than the standoff length, and §3 allows **5 nm** at the 10 nm layer on **both** readings of its band. The pre-bow escape is real, priced and **rejected 12 of 12**: it needs a built rise of **4.08–16.66 nm (12–49 bp)** costing a preload of **150–225 pN**, i.e. **1.5–2.25× §3's entire target force** and **109–163×** `C-0021`'s hold-down requirement, and at the desired 10 nm stroke **no rise up to 30 nm** brings the tangent under the ceiling. **`T-78` is settled too, and the ceiling is REAL rather than a design choice**: the body under the midspan is the standoff-carrying body *by construction*, because the favourable sense is defined by the driven body lying on its far side. At §3's **acceptable 3 nm** stroke it costs **nothing** for `ℓ ≥ 6 nm` — zero penetration, no slot. At §3's **desired 10 nm** stroke and `ℓ = 8 nm` the midspan goes **4.69 nm** past the contact plane and the beam demands a slot **18.37 nm long — 57.7 % of the span, 54 bp** — which over 45 paths is **2223 nm², 1.39× the tile's own footprint**. **The escape from the ceiling is a hole bigger than the device.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED EITHER** — `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number: no duplex has been built standing normal to a single-layer sheet, every published out-of-plane base is a **pin**, and a duplex END has at most **two** covalent links. |
| **Provenance** | `gpd/results/T-75-flexure-mounting-sense.json`, produced by `anchoring.FlexureMountingSenseStudyKt`; **4 mounting records, 44 design records, 33 aperture records, 3 effort-band records, 9 occupancy records, 12 pre-bow records, 7 convergence records, 10 upstream reproductions**; **30 gate-named tests in `FlexureMountingSenseTest`, 0 failures**, and the whole suite green on `tools/verify.sh`; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** on two independent runs |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile, ~10 nm thick (§3); 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; `EI = 230 pN·nm²`; `C-0028`'s `B2` base at 261.2 pN·nm/rad |
| **Consumes** | [`C-0030`](C-0030-coupled-standoff-joint.md) (`CoupledJointFlexure`, `FlexureOrientation`, `favourableStrokeClearance`, the whole `B2` design table — **re-run as a library**, its favourable and adverse designs at `ℓ = 8 nm` reproduced to `3e−5`–`1e−4`), [`C-0028`](C-0028-standoff-base-joint.md) (`StandoffBase.crossovers(2, favourable)`), [`C-0025`](C-0025-flexure-end-joint.md) (`c(ρ)`, reproduced to `1.5e−16`; **its beam integrated once more here to give the deflected shape**), [`C-0023`](C-0023-two-sided-coupling.md) (the 40 pN/nm ceiling, the 45 paths, the mounting offset as a **length** quantised at 0.34 nm), [`C-0017`](C-0017-output-coupling-stiffness.md) (the 33.333 pN/nm mandate, the 10 nm envelope, the superstructure), [`C-0021`](C-0021-zero-bias-resting-position.md) (the 1.3806 pN hold-down requirement), [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md)/`ActuatorGeometry` (§3's effort-point band, reproduced at both ends), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`EI`, `S`, the SAXS interhelical distance, the rise) |
| **Raises** | [`CH-0045`](../challenges/CH-0045-the-mounting-sense-is-not-a-free-binary.md) and [`CH-0046`](../challenges/CH-0046-a-standoff-in-tension-does-not-buckle.md), both against `C-0030` |
| **Challenged by** | [`CH-0055`](../challenges/CH-0055-the-forty-five-path-array-is-not-a-placement.md) (from [`C-0041`](C-0041-flexure-array-packing.md), `T-96`) — against the `Conditions` premise *"45 load paths on `C-0015`'s 3 × 15 grid"*, which has **no plan view at any level count or body size**; the tile carries **15**. **No number here fails to reproduce**, and its 326 nm² tie-aperture floor is an **area** where the question is **connectivity** — the holes are collinear and sever the superstructure into **18 components** |

---

## The claim, in one line

**The flexure's midspan deflection is the change in the two bodies' separation, so its sign is fixed by a kinematic identity that contains no length and is a PRODUCT of two binaries — of which `C-0030` named one; and once the other is added, exactly one of the four mountings survives §1 and §3: the standoffs stand on the OUTPUT SUPERSTRUCTURE, pointing away from the tile, with the flexure outboard of its own ground and each midspan tied back down through that ground — which also settles `T-78`, because the body under the midspan is then not an accident of specification but the definition of the favourable sense, and buying a stroke past its clearance costs an aperture 1.39× the tile's own footprint.**

---

## The cheap bound, which ran first and settled the question

Nine numbers, before any root find, any span and any matrix.

| mounting | base body | standoff normal | **`dδ/ds`** | sense |
|---|---|---|---|---|
| `Tu` | tile | upward | **−1** | adverse |
| `Td` | tile | downward | **+1** | **favourable** |
| `Sd` | superstructure | downward | **−1** | adverse |
| `Su` | superstructure | **upward** | **+1** | **favourable** |

> **Both bodies appear on both sides of the split, and so do both normals.** `favourableCountWithTileBase = 1` and `favourableCountWithUpwardNormal = 1` — so neither variable, alone, carries any information about the sign at all.
>
> **Falsifier 2 (all four the same sign) did not fire; falsifier 1 (the rate depending on a length) did not fire either** — asserted at 4 mountings × 4 standoff lengths × 3 tie lengths. The expensive part was therefore warranted, and it went where the cheap bound pointed: not at the sign, at **buildability**.

---

## Deliverable 1 — the identity, and the three things it is equivalent to

The midspan sits at `z_base + n(ℓ − δ)` and the driven body's attachment plane is a fixed tie from it, so

&nbsp;&nbsp;&nbsp;&nbsp;**`dδ/ds = (v_base − v_driven)/n`**, &nbsp; `v_TILE = −1` (§1: the bias pulls the tile down), &nbsp; `v_SUPERSTRUCTURE = 0`, &nbsp; `n = ±1`.

The favourable sense is the same statement three ways over, and all three are **asserted** rather than assumed:

| statement | where it is checked |
|---|---|
| `dδ/ds > 0` — the midspan sags **toward** its own base plane | `FlexureMounting.deflectionRate`, by differentiating the chain |
| the **tie crosses the base plane**, i.e. the driven body is on the far side of it | `MountingStack.tieCrossesBasePlane`, by comparing three `z` coordinates — **a different construction**, agreeing at 4 mountings × 3 layer heights × 3 standoff lengths × 3 effort heights |
| the flexure is **outboard** of its ground rather than **inboard** between the two bodies | `FlexureMounting.inboard` |
| the standoff's axial duty is **compression** | `FlexureMounting.standoffAxialSense` |

> **The last equivalence is not bookkeeping.** The beam's end shear acts **along the standoff's own axis**, so it pushes the head toward its base exactly when the midspan moves that way. **A standoff in tension does not buckle** — `C-0028`'s and `C-0030`'s `P6` is a predicate of the *favourable* mounting alone, and `C-0030`'s adverse buckling margins (0.99 / 0.74 at `ℓ = 8 nm`) are charged against a member its own kinematics puts in **tension**. That is [`CH-0046`](../challenges/CH-0046-a-standoff-in-tension-does-not-buckle.md); it does **not** move the adverse verdict, which is owned by `P3`.

---

## Deliverable 2 — the buildability filter, and why the survivor is unique

Four mountings, three filters, each of them arithmetic on §1, §3 and existing claims.

| | `Tu` | `Td` | `Sd` | **`Su`** |
|---|---|---|---|---|
| base body | tile | tile | superstructure | **superstructure** |
| standoff normal | up | down | down | **up** |
| sense | adverse | favourable | adverse | **favourable** |
| topology | inboard | outboard | inboard | **outboard** |
| standoff axial duty | tension | compression | tension | **compression** |
| assembled tangent at `ℓ = 8 nm` [pN/nm] | 44.82 | 25.23 | 44.82 | **25.23** |
| passes `C-0023`'s 40 pN/nm ceiling? | **no** | yes | **no** | **yes** |
| **minimum** effort height above the tile's top face | **`ℓ`** | 0 | **`ℓ`** | **0** |
| places §3's effort point at `ℓ = 8 nm`? | **no** | yes | **no** | **yes** |
| flexure inside the actuation gap? | no | **yes** | no | **no** |
| verdict | **FAIL** | **FAIL** | **FAIL** | **PASS** |

### Filter 1 — §3's own effort point, on **both** of its readings

§3: *"Tile thickness ~10 nm …; effort point may sit ~20–25 nm above the electrode"*, with layer heights 5 / 7 / 10 nm.

- **Constant reading** (`C-0012`'s, and the only one that reproduces both ends of the band): the band is 5 nm wide and the layer-height range is 5 nm wide, which **forces** a constant attachment height, and it is **5 nm** — giving 20 / 22 / 25 nm exactly. Reproduced here to `0.0`.
- **Loose reading**: the effort point merely lies in `[20, 25]` nm at every layer height, so the attachment height may be **10 / 8 / 5 nm** at the 5 / 7 / 10 nm layers.

**The inboard topologies stack the standoff and the tie in series between the two bodies, so their effort point cannot come closer to the tile than `ℓ`.** The outboard ones fold the tie back past their own base plane and have no floor at all.

| layer height | 5 nm | 7 nm | **10 nm** |
|---|---|---|---|
| longest inboard standoff, loose reading | 10.0 | 8.0 | **5.0** |
| longest inboard standoff, constant reading | 5.0 | 5.0 | **5.0** |
| admits `C-0030`'s `ℓ = 5–10 nm` window? | partly / no | partly / no | **no / no** |

> **The two readings AGREE at the 10 nm layer — which is where `C-0016` and `C-0027` put the entire design window.** So the filter is not an artefact of a reading: at the design height §3 permits an inboard standoff of at most 5 nm, and `C-0030`'s adverse mounting is past `C-0023`'s ceiling at every length up to 13.16 nm.

### Filter 2 — the polymer layer

`Td` is favourable, and it puts the flexure **below** the tile.

| layer height | `ℓ` | beam plane above the electrode [nm] | clears the electrode? | array volume / layer volume | implied `Π` ratio |
|---|---|---|---|---|---|
| 5 | 5 / 6 / 8 | **0.00 / −1.00 / −3.00** | **no** | 0.738 / 0.773 / 0.845 | 20.4 / 28.0 / 66.4 |
| 7 | 5 / 6 / 8 | 2.00 / **1.00** / **−1.00** | yes / no / no | 0.527 / 0.552 / 0.604 | 5.4 / 6.1 / 8.0 |
| **10** | 5 / 6 / 8 | 5.00 / 4.00 / 2.00 | yes | **0.369 / 0.386 / 0.423** | 2.8 / 3.0 / 3.4 |

> **0 of 9 admissible.** At §3's 5 and 7 nm layers the beam is at or below the electrode surface at the standoff lengths the clearance needs. At the 10 nm layer it clears — and the 45-beam, 90-standoff array then occupies **37–42 % of the polymer layer's own volume**, which at fixed chain content raises `φ` by 1.6× and its des Cloizeaux pressure by **2.8–3.4×**. **This is a cheap bound and it is deliberately crude: it is 7–8× past any threshold that would justify a solve.** It is also not the only objection — the tie must then perforate the tile itself, 45 times, in a 15-duplex single-layer sheet.

### Filter 3 — what the tie must pass through

The favourable sense **requires** the tie to cross the standoff base plane. On `Td` that plane is the tile; on `Su` it is the superstructure. **`C-0009`, `C-0015` and `C-0026` build the whole tile load-path argument on an unbroken 15-duplex sheet, and the superstructure is `C-0017`'s and is unspecified.** So the perforation is a design choice on `Su` and a contradiction on `Td`.

---

## Deliverable 3 — `T-78`, the clearance ceiling as an **aperture**

`C-0030` reports the ceiling `ℓ − 2.69 nm` and files it as *"real if the body is the solid tile, a design choice if it is `C-0017`'s unspecified superstructure"*.

**Both halves are superseded.** The body under the midspan is the standoff-carrying body **by construction** — the favourable sense is *defined* by the driven body lying on its far side — so the ceiling is real whichever body it is. And it cannot be an imperforate plane either, because the tie has to cross it at exactly the place the midspan descends toward. **The aperture the tie needs and the clearance the midspan needs are the same feature**, so the right question is not *"is there a ceiling"* but *"how big is the hole"*.

It is answered by integrating `C-0025`'s beam once more:

&nbsp;&nbsp;&nbsp;&nbsp;**`w(u)/δ = (24u + 12ρu² − 16(2+ρ)u³)/(8+ρ)`**, &nbsp; `u = x/L ∈ [0, ½]`, symmetric about `½`,

which is `3u − 4u³` at `ρ = 0`, `12u² − 16u³` at `ρ → ∞`, has end slope `24/(8+ρ)` (the beam's own `Lθ₀/δ`), and returns `c(ρ) = 192(ρ+2)/(ρ+8)` at midspan by construction. **Three checks it was not built from.**

### The aperture, at `ℓ = 5–12.7 nm`

| `ℓ` [nm] | penetration at 3 nm | slot at 3 nm | penetration at 10 nm | **slot at 10 nm** | slot / span | slot [bp] | 45 slots / footprint |
|---|---|---|---|---|---|---|---|
| 5 | **0.69** | **11.69 nm** | 7.69 | 25.11 | 0.790 | 74 | 1.90 |
| **6** | **0** | **none** | 6.69 | 22.80 | 0.719 | 67 | 1.73 |
| 7 | 0 | none | 5.69 | 20.60 | 0.649 | 61 | 1.56 |
| **8** | **0** | **none** | **4.69** | **18.37 nm** | **0.577** | **54** | **1.39** |
| 9 | 0 | none | 3.69 | 16.00 | 0.502 | 47 | 1.21 |
| 10 | 0 | none | 2.69 | 13.39 | 0.420 | 39 | 1.01 |
| 12.7 | 0 | none | **0** | **none** | 0 | 0 | 0 |

> **§3's ACCEPTABLE clause is free.** At `ℓ ≥ 6 nm` the midspan never reaches the base body: zero penetration, no slot, and `T-78`'s ceiling is discharged outright.
>
> **§3's DESIRED clause is not.** At the recommended `ℓ = 8 nm` the beam needs a slot **57.7 % of its own span** — 54 base pairs — under **every one of the 45 flexures**, totalling **2223 nm², 1.39× the whole tile footprint**. Removing the slot entirely needs `ℓ ≥ 12.7 nm`, which is outside `C-0017`'s 10 nm envelope (`C-0030`'s `P5`) — exactly as `C-0030` reports, and now with the price of *not* doing so.

### And an aperture **floor** that is present at every stroke

`C-0023`'s two-sidedness makes the midspan tie a **duplex**, not a strand, and the favourable topology makes it cross the base plane by construction. So the base body needs **45 duplex-omission holes** whatever the stroke: `45 × 2.69² = 326 nm²`, **20.4 % of the tile footprint** — the *"tie only"* column of the result file, and the irreducible part of `T-78`'s answer.

---

## Deliverable 4 — the two escapes, priced

### The pre-bow (`T-42`) — real, and rejected 12 of 12

Building the flexure already sagging toward its base plane by `δ₀` puts the first `δ₀` of stroke on the favourable limb **inside an adverse mounting**, with the span re-placed on the **incremental** secant `[F(0) − F(s)]/s`. It genuinely recovers the compliance. It costs:

| `ℓ` | stroke | minimum rise | in bp | span | peak assembled tangent | **preload** | / `C-0021`'s 1.38 pN | / §3's 100 pN | verdict |
|---|---|---|---|---|---|---|---|---|---|
| 3 | 3 nm | **16.66 nm** | 49 | 158.07 | 39.91 | **225.1 pN** | 163× | **2.25×** | rejected |
| 5 | 3 nm | **4.08 nm** | 12 | 30.31 | 39.18 | **150.3 pN** | 109× | **1.50×** | rejected |
| 8 | 3 nm | **5.10 nm** | 15 | 28.45 | 39.60 | **203.5 pN** | 147× | **2.04×** | rejected — *and the topology still cannot place §3's effort point* |
| 3 / 5 / 8 | **10 nm** | **none up to 30 nm** | — | — | 62.5–86.4 | — | — | — | **CLOSED** |

> **The rise required is larger than the stroke it protects** — 1.4–5.6× — because the span has to be re-placed at every trial rise and the favourable limb's own tangent is not flat. The preload it costs is **1.5–2.25× §3's entire target force**, pressed onto the polymer layer before any bias is applied, which is `CH-0024`'s *"a stroke measured from a height the tile never occupies"* at full scale. And at §3's **desired** stroke **no rise closes it at all**: the operating range leaves the favourable limb before the ceiling is met.
>
> **`C-0023`'s discipline is upheld in a new place: a preload is a mounting offset, i.e. a LENGTH — and here the length that is wanted is 12–49 base pairs, which is entirely buildable and entirely unaffordable.**

### Reversing the stroke — recorded as an assumption, with its falsifier

§1 fixes it: *"positive bias on the electrode applies a downward force on the tile"*, and `C-0017`'s superstructure is the coupling's far ground. **The identity is robust to lever compliance**: if the superstructure's own attachment moves *with* the tile by a fraction `f`, every rate is scaled by `(1 − f)` and **no sign changes**. It fails only for `f > 1`, i.e. if the lever descends faster than the tile drives it — which no passive load does. `T-33` is the task that would establish `f`.

---

## The nominal design that results

| | |
|---|---|
| **mounting** | **`Su` — standoff bases on the OUTPUT SUPERSTRUCTURE, standoffs pointing AWAY from the tile, flexure OUTBOARD above the superstructure's plane, each midspan tied back DOWN through that plane to the tile** |
| **why not the tile** | the two tile-based mountings are the adverse `Tu` (past the compliance ceiling at every admissible length, and unable to place §3's effort point) and the favourable `Td` (flexure inside the actuation gap: 37–85 % of the layer's volume, and below the electrode at two of §3's three layer heights) |
| **standoff** | 8.0 nm = 24 bp on `C-0028`'s `B2` two-crossover base — unchanged, but now **rooted in the superstructure** |
| **span** | 31.82 nm = 94 bp, tangent 25.23 pN/nm — `C-0030`'s numbers, reproduced to `1e−4` |
| **what the tile carries** | **only the 45 tie attachments of `C-0015`'s 3 × 15 grid** — the scheme `C-0026` already validated. No standoff, no base couple, no 90° junction anywhere on the tile |
| **what the superstructure must now do** | host 45 normal-standoff bases **and** be perforated by 45 duplex ties — 326 nm², 20.4 % of the tile footprint, at every stroke |
| **stroke delivered without an aperture for the beam** | **`ℓ − 2.69 nm`: 5.31 nm at `ℓ = 8`** — covers §3's acceptable 3 nm at `ℓ ≥ 6 nm` |
| **cost of §3's desired 10 nm stroke** | a **18.37 nm × 2.69 nm** slot under every midspan — **2223 nm², 1.39× the tile footprint** — or `ℓ ≥ 12.7 nm`, outside `C-0017`'s envelope |
| **pre-bow** | not available: 4.08–16.66 nm of rise for a 150–225 pN preload, and closed outright at the desired stroke |

---

## The five verification gates

Executed as **30 gate-named tests** in `src/test/kotlin/anchoring/FlexureMountingSenseTest.kt`, 0 failures, with the whole suite green on `tools/verify.sh`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `dδ/ds` is dimensionless and exactly `±1` at all four mountings, and **identical over 4 × 4 × 3 combinations of standoff and tie length** — it contains no length; the beam shape is dimensionless, exactly 1 at midspan and 0 at both ends for five restraints; the aperture length is a length and **doubles exactly with the span**; the array volume goes as the **square** of the duplex radius and equals `nπr²(L + 2ℓ)`; unphysical arguments throw, including a negative tie and a position outside `[0, 1]` | **PASS** |
| **2 — limiting cases** | the shape reduces to `3u − 4u³` (pinned) and `12u² − 16u³` (clamped) at four positions each; its **end slope reproduces `24/(8+ρ)`**, which is `Lθ₀/δ` read off the beam's own solution and not off the polynomial; a **zero pre-bow reproduces `C-0030`'s signed stroke laws exactly** — reaction and tangent, at 4 mountings × 4 strokes; a zero pre-bow costs exactly zero preload; a standoff longer than the contact distance leaves clearance and one shorter leaves none; **the tie's aperture is present at the stroke where the beam's is zero** | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the aperture solve is **exactly scan-step independent** over 64 → 4096 (`0.0`); the level solve **inverts the shape** to `1e−9` at 3 restraints × 4 levels; the maximum assembled tangent is sample-count independent over 200 → 20000 (`0.0`); **the pre-bowed span returns its own target secant** to `1e−8` at 4 mountings × 3 rises; **the result file is byte-identical on two independent `tools/study.sh` runs** | **PASS** |
| **5 — literature and upstream cross-check** | **`C-0030`'s favourable and adverse designs at `ℓ = 8 nm` reproduced** — span 31.82 / 40.14 nm to `2.9e−5` / `7.0e−5`, tangent 25.23 / 44.82 pN/nm to `1.1e−4` / `6.0e−5`; its clearance table (5.31 / 3.31 / 7.31 nm) to `1.7e−16` and its 12.69 nm desired-stroke length to `7.9e−4` (the grid step); **`C-0025`'s `c(ρ)` recovered from the new shape function to `1.5e−16`**; **§3's effort band reproduced at both ends** (20.0 / 25.0 nm, `0.0`); the SAXS interhelical distance 2.69 nm | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The tie-crossing test agrees with the kinematic rate at every realisable stack** — 4 mountings × 3 layer heights × 3 standoff lengths × 3 effort heights. One differentiates a chain, the other compares three `z` coordinates; nothing forces them to agree.
2. **The sign is a product**: flipping the base body alone flips it at every mounting, and flipping the standoff normal alone flips it at every mounting — asserted separately, which is the executable form of *"`C-0030` named half the variable"*.
3. **Compression ⟺ favourable**, asserted at all four mountings.
4. **`min(stroke, clearance) + penetration = stroke` identically**, at 4 strokes × 3 standoff lengths — the midspan's travel is conserved between what fits and what does not.
5. **The shape is symmetric about the midspan**, at 4 restraints × 4 positions, and the inboard effort-height floor is `ℓ` for exactly the inboard mountings and 0 for exactly the outboard ones.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | `dδ/ds` depending on a length | **no** | identical over 48 combinations |
| 2 | all four mountings giving the same sign | **no** | 2–2, and split by the **product** |
| 3 | both favourable mountings surviving — i.e. the answer really being a specification gap | **no** | `Td` fails on the layer at 9 of 9 states; the survivor is unique |
| 4 | the pre-bow escape being free | **no** | 150–225 pN, 109–163× `C-0021`'s requirement, and closed at the desired stroke |
| 5 | the aperture at §3's **acceptable** stroke being non-zero | **no** | zero for `ℓ ≥ 6 nm` — the clause the programme actually delivers is free |
| 6 | §3's effort band admitting the inboard topology at `ℓ ≥ 5 nm` | **no at the design height** | the loose reading admits `ℓ ≤ 10 / 8 / 5 nm` at the 5 / 7 / 10 nm layers, and **the whole window lives at 10 nm** |

**A result that was not anticipated:** the adverse mounting is not short of a *mechanism*, it is short of a **length**. Its tangent falls monotonically with the standoff and meets `C-0023`'s ceiling at **13.16 nm**, against 3.48 nm favourable — 3.78× longer, outside `C-0017`'s 10 nm envelope, and at that length the inboard topology puts §3's effort point 13.2 nm above the tile against §3's own 5 nm. **`C-0030`'s "no window at any length" is a statement about the interval it swept**, and the honest form is the length the window would need. That is the second half of [`CH-0045`](../challenges/CH-0045-the-mounting-sense-is-not-a-free-binary.md).

---

## What this does to the queue

| task | before | after |
|---|---|---|
| **`T-75`** | *"a specification gap, the fourth in this programme"* | **CLOSED as a determination.** The mounting is `Su`; the deciding variable was never *which body* alone |
| **`T-78`** | *"real if the body is the solid tile, a design choice if it is the superstructure"* | **CLOSED.** The body under the midspan is the standoff-carrying body **by construction**, so the ceiling is real; the escape is an aperture, priced at 1.39× the footprint at the desired stroke and **zero at the acceptable one** |
| `T-68` (can the sheet react the base moment?) | asked of the **tile** | now asked of the **superstructure**, which need not be a single layer — the question is unchanged in physics and softer in practice |
| `T-66` / `T-72` (a triangulated standoff) | a remedy on the tile | a remedy on the **superstructure**, where a multilayer host makes a truss easier. **`C-0029`'s two-covalent-link ceiling is NOT relieved** — it is a property of the standoff duplex's own end, not of the body it lands on |
| `T-67` / `C-0029` (the 90° routing) | between a **sheet** duplex and a normal standoff | **unchanged in kind, moved in place.** The tile now carries no standoff at all |
| `T-31` (does a flexure array stay compliant?) | medium | **promoted in consequence, not in priority**: every aperture area here is quoted against the tile footprint as a *scale*, and 45 flexures of ~32 nm span do not lie side by side in 40 × 40 nm |
| `T-33` (is the lever's load path two-sided?) | medium | **the body that now carries the standoffs is the one `T-33` asks about** |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number.
- **The kinematic identity assumes the superstructure is the coupling's fixed ground.** A superstructure attachment that moves *with* the tile by a fraction `f` scales every rate by `(1 − f)` and changes **no sign**; only `f > 1` would, and no passive load does that. `T-33`.
- **§3's effort band is an indication, not a constraint** — *"may sit ~20–25 nm"*. Both readings are carried and they **agree at the 10 nm layer**, which is where the design window is; at 5 and 7 nm the loose reading is 2× weaker and the filter there rests on the compliance ceiling alone.
- **The superstructure is UNSPECIFIED beyond `C-0017`'s assumption that it exists and is grounded.** Whether it may be perforated is a design choice on an undesigned body — which is exactly why the aperture is priced as an **area** rather than asserted, and why one question goes back to Kazik.
- **The aperture uses the LINEAR deflected shape.** At the desired 10 nm stroke the head rotation is 0.63–0.68 rad (`C-0030`), so the 10 nm aperture column is a linear-theory extrapolation, exactly as `C-0030`'s 10 nm columns are. The 3 nm columns — where the answer is *zero* — are inside small deflection.
- **The array packing is not solved.** 45 flexures of ~32 nm span do not lie side by side in a 40 × 40 nm footprint at a 2.69 nm pitch, so every aperture-area fraction is a **scale** against the tile footprint and not a layout. `T-31` owns the array, and it owns this too.
- **The layer-occupancy kill is a cheap bound by design** — the array's own excluded volume and the des Cloizeaux exponent 9/4 — because it is 7–8× past any threshold that would justify a solve. The two independent objections beside it (the beam at or below the electrode; 45 perforations of the tile) do not depend on it.
- **The contact distance is the SAXS single-layer interhelical distance, 2.69 nm.** A multilayer superstructure would give 2.536 (honeycomb) or 2.73 (square) — worth ±6 % of the clearance and nothing of the verdict.
- **`C-0023`'s chord draw-in is used throughout**, so `T-43`'s 1.13–1.20× inconsistency travels with the pre-bow numbers unchanged.
- **The pre-bow's rejection threshold is `10× C-0021`'s hold-down requirement.** It is a declared convention; the numbers are 109–163×, so no reasonable threshold changes the verdict.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement** |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| B-DNA steric diameter | 2.0 nm | **CITED**; the phosphate backbone *is* the surface (`CLAUDE.md`) |
| §3 parameters | 100 pN, 3 nm, 10 nm, 40 × 40 nm, ~10 nm tile, effort point ~20–25 nm | **CITED** |
| `C-0030`'s `B2` designs, `C-0021`'s 1.3806 pN, `C-0023`'s 40 pN/nm | — | **CITED**, and reproduced here as gate-5 tests |

Everything else — the kinematic identity, the four-mounting enumeration, the deflected shape, the penetration, the aperture length and area, the layer occupancy, the effort-band filters, the pre-bowed span, preload and minimum rise, and the adverse mounting's required length — is **derived here in code**, with `C-0030`'s pipeline re-run rather than tabulated.

## Still open — named, not answered

1. **May the output superstructure be perforated under each flexure midspan?** It is the one input `T-78` cannot supply, it is worth §3's **desired** stroke, and it is a question about a body nothing in §1 or §3 describes. **Carried to `TASKS.md`'s open questions as one sentence, with the bracket and the threshold.** `T-95`.
2. **`T-31`'s array packing.** Every area fraction here inherits it.
3. **`T-33`** — the superstructure is now the body that carries the standoffs, so its own load path is load-bearing in a third direction.
4. **`C-0029`'s two-covalent-link ceiling on the standoff's base couple is untouched** by moving the base to another body, and it remains the binding constraint on the whole standoff branch at §3's desired stroke.

## Challenges

**Raises [`CH-0045`](../challenges/CH-0045-the-mounting-sense-is-not-a-free-binary.md)** (the mounting sense is not a free binary and *"which body carries the standoffs"* is half the variable; and *"no window at any length"* is a statement about a swept interval) and **[`CH-0046`](../challenges/CH-0046-a-standoff-in-tension-does-not-buckle.md)** (the adverse mounting's buckling margins are charged against a member its own kinematics puts in tension), both against `C-0030`.

**No number in `C-0030` fails to reproduce.** Its favourable and adverse designs, its clearance table and its 12.69 nm figure are all recovered to `1e−4` or better. What is challenged is the **framing**, in both cases.

**None stands against this claim.** The four ways it would fail:

1. **A specification or a build placing the output superstructure BELOW the tile.** Then `v_SUPERSTRUCTURE` and `v_TILE` exchange roles and the surviving mounting is `Td` rather than `Su`. §3's effort point forbids it, but §3's effort point is an indication.
2. **A superstructure that cannot be perforated at all.** Then even the tie's 326 nm² floor is unavailable, the outboard topology is unbuildable, and the branch closes at every stroke — not merely at the desired one.
3. **A demonstration that the flexure's midspan and the driven body are not rigidly co-moving** — a slack tie, which is `C-0023`'s one-sided element returning. Then `|dδ/ds| < 1` over part of the stroke and the placement condition moves, though **no sign changes**.
4. **A large-deflection solve showing the beam's deflected shape is materially flatter near the ends than `C-0025`'s.** The aperture length would shrink; the 3 nm answer (zero) is unaffected.
