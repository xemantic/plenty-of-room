# ANSWERS — the Gen-1 questions, as posed

This file answers [the problem definition](third-party/2026-08-ndi-gen1-problem-definition.md) in its own terms:
the eight tasks of §6, the open questions of §4, and §7's test of whether the loop worked.

It is a **synthesis, not a source**.
Every number here belongs to a claim in [`gpd/claims/`](gpd/claims/), and the claim carries the provenance,
the validity range and the verdict.
Where a claim has been challenged, the challenge is named — this project does not overwrite results.

**Maturity: TRL 1–3 throughout.**
`PASS` means model-consistent and traceable.
**Nothing in this repository is measured.**
Conditions are 300 K, aqueous buffer with stated Mg²⁺, `k_BT = 4.142 pN·nm`, unless a row says otherwise.

---

## 1. The short version

Three findings dominate, and none of them is a stiffness number.

1. **The tile is not a rigid plate, and the picture has to go.** It is rigid *exactly* under a uniform load —
   at any flexural rigidity — and dishes under every departure from uniformity, including the unavoidable one
   at 300 K. A point-coupled lever and an area-averaging charge sensor **do not measure the same displacement**;
   they differ by **32 %** of the stroke, and that part is **irreducible** — it is forced by the tile's own
   electrostatic edge, which no coupling choice can remove. §4(g)'s own test for abandoning the rigid-plate
   assumption is met, and met by a load nobody chose.
   (`C-0006`, `C-0009`, `C-0022`, `CH-0005`, `CH-0025`.)
2. **The polymer layer confines the tile in one direction only.** Its lateral restoring stiffness is *exactly*
   zero by symmetry — not small — so an untethered tile diffuses 63 nm in one 1 kHz period, 21× the positional
   predicate. It also exerts no upward force above `L₀`, so at zero bias the tile is unconfined in **both**
   directions. Nothing in the §3 stack holds it — **and `T-13` now says what does, and what does not.**
   What is unavoidably present, van der Waals across the gap, gives a **stable but not confining**
   equilibrium: its `1/h³` force has a bounded potential, so the well is only **0.2–5.7 `k_BT`** deep and the
   tile escapes it. **Stability and confinement are different properties.** A **one-sided** coupling — the
   ssDNA spacer that made `C-0017`'s lever compliant enough — supplies **exactly zero** downward preload,
   because a single strand carries no compression, and closing the hold-down then costs eight substrate
   tethers for 0.07–0.38 nm of stroke.
   **The requirement dissolves once the coupling is two-sided.** The potential above `L₀` turns from linear
   to quadratic, so the requirement stops being a *force* (`k_BT/σ` = 1.38 pN) and becomes a *stiffness*
   (`k_BT/σ²` = 0.4602 pN/nm) — one power of the position bound apart — and §3's own mandated 33.333 pN/nm
   exceeds it **72×** unpreloaded. The tetherless device goes from 1.4–5.4 `k_BT` and 0 of 18 confining to
   **959–7582 `k_BT` and 18 of 18**, and `C-0014`'s eight substrate tethers **leave the design**.
   (`C-0010`, `C-0021`, `C-0023`, `CH-0027`.)
3. **The output coupling decides the programme, and it is fixed by §3 rather than by the layer.**
   100 pN at ≤ 2 V is reachable with room to spare and drainage clears 1 kHz by 22× — and at 10 nm the
   operating point the device reaches them at is not one it holds by itself.
   But the number a lever must bring is **not** the 5–277 pN/nm the stability table suggested: the force
   delivered to a load over a stroke is `k_c·Δs`, so §3's own 100 pN and 3 nm fix it at **33.333 pN/nm** by
   arithmetic, and read at the bias the device actually operates at the stability floor is **0 at 5 and 7 nm
   and 23.4–27.9 pN/nm at 10 nm**.
   A coupling of **45 attachments — the same grid flatness already needs, and that grid is one attachment row per
duplex** — each a duplex standoff in series
   with a **13-nucleotide tuned ssDNA spacer**, supplies it: it *places* the operating point on its secant and
   *stabilises* it on its tangent, at 2.2 pN per load path against a 10 pN allowable.
   **The design window is not empty.**
   The margin at §3's own 2 mM is 1.19–1.42×, inside its own mean-field error, so the verdict is
   *not excluded*, never established — and dropping to 0.5 mM buys six times more.
   Carrying **both** iteration-4 corrections it is **1.23–1.53×** — that figure being the **affine
   mandate's**, not the coupling the programme actually has — the polymer fluctuation bound *degrades*
   it to 1.11–1.25× and the finite-tile enhancement *restores* it to 1.34–1.67×, and the two are of the same
   size and opposite sign, so `C-0019`'s ≥ 1.07× was one half of a two-sided correction (`C-0027`).
   The verdict is unchanged in kind — *not excluded*, never established, because the **electrostatic**
   one-loop error is 123–214 % and nothing in this programme narrows it (`CH-0019`).
   **And that reserve has since been spent.** Every margin above was banked on the coupling being
   strain-**stiffening**, which makes its tangent exceed its secant and hands stability a free reserve at zero
   placement cost. The coupling the programme actually arrived at, once its joint was solved rather than
   bracketed, strain-**softens**: `t/s` falls 1.095 → 0.757, and the assembled tangent has an *interior*
   minimum of 22.88 pN/nm at a 4.55 nm stroke, inside the operating range. Re-running the pull-in analysis on
   that law over 216 states, the 10 nm / 2 mM bias margin collapses to **1.0000–1.0019** — the device is
   placed *on* its own fold — and the fold's stroke walks back from 3.41–4.13 nm to **2.80–3.17 nm, through
   §3's own 3 nm target at two of six layer models**. Both escapes are priced and both fail: the adverse
   mounting is 42.4–61.0 pN/nm, past the 40 pN/nm compliance ceiling at **0 of 8** lengths, and a shorter
   standoff lands 2.2 % short. At **0.5 mM every predicate clears** (1.44–5.93× on stiffness, 1.038–2.327× on
   bias).
   **So 0.5 mM is no longer the comfortable choice with 2 mM still defensible — it is a requirement of the
   only coupling that survives.** That is a *specification* decision for NDI, and it is handed back as one
   rather than adopted here.
   (`C-0012`, `C-0016`, `C-0017`, `C-0030`, `C-0032`, `CH-0016`, `CH-0042`.)

4. **The design window survived nine claims and ten challenges by moving one edge, outward.**
   Of the four iteration-4 results aimed at it, three live on axes an intersection cannot see and the fourth
   is nearly cancelled by the part of the design that produced it.
   **Ten of twelve discovered axes do not resolve in grafting density**, and axes now *leave* the stack as
   well as arriving: the lateral-confinement footprint left when the in-plane tethers left the design, and
   `C-0049` withdrew `C-0023`'s 40 pN/nm tangent ceiling — it is exactly `1.2 ×` a **placement** mandate and
   carries that stroke inside it — replaced by a path count, `n·a/s`.
   **A second re-synthesis against three further iterations moves nothing at all**: `C-0031`–`C-0050` and
   `CH-0043`–`CH-0062` leave **0 of 6 window edges** moved, 0 grid steps, worst departure exactly `0.0`, no
   owner changed. That null is the finding, and its cause is `C-0016`'s own lesson turned on itself — of
   those twenty claims **exactly one** carries a quantity that is a function of `σ` at all. The window was
   not *survived*; it was never addressed. `C-0050` did produce the first genuinely `σ`-resolved constraints
   since iteration 4 (a kinematic ceiling and a validity ceiling), and at all 61 grid points **neither
   binds**, by 1.71–3.11×.
   **So a `(σ, L₀)` window is the wrong object for the Gen-1 decision, and the deliverable is now a height
   plus five specification questions** — of which only a taller layer can buy the desired stroke.
   (`C-0027`, `C-0051`.)

And the shape of the whole problem, which no single number shows: **static stability wants the thin layer,
whose window is empty by 13.3×; the window, the stroke and the force-versus-height trade all want the thick
one, whose operating point is unstable everywhere.** All three pull the same way and stability pulls against
all three.

**Whether the tile is held at zero bias is a question about the coupling's *topology*, not about any force in
the stack.**
DNA's compliance is either entropic — which only pulls — or bending, which is signed, and the programme had
committed to the first.
A flexure or a crossover hinge at exactly the stiffness §3 already mandates turns a 1.4–5.4 `k_BT` trap into a
959–7582 `k_BT` confinement, with no preload and no extra part.
The element is settled; the **joint** was not, and it is `C-0025`: no covalent origami joint reaches the
free-to-draw-in reading, and the single-stranded hinge that would cannot *support* the beam, because a
flexible link has no direction and pays its axial slack back as transverse dead band.
The flexure is buildable on **normal duplex standoffs** — span **31.82 nm = 94 bp on 8 nm standoffs with a
two-crossover, favourable-orientation base**, tangent **25.23 pN/nm**, window **`ℓ = 5–10 nm`** — and `T-13`
still closes, because the joint changes the element's geometry and not its sidedness.
The **joint** is now solved rather than bracketed: the two standoff springs are the diagonal of one 2 × 2 read
with the other load zero, and the off-diagonal they dropped is not a compliance but a **draw-in the standoff
supplies** — 3.09× the demand it was set against, so the flexure runs in **compression** rather than tension
and the buckling margin rises **1.41 → 2.18×** (1.64× on the measured rigidity).
**But that supply is odd where the demand is even, so the coupling's law is no longer odd and its sign is
decided by which body carries the standoffs** — worth the difference between a window and none, free to a
builder, and §3 does not say which.
The base is now specified rather than assumed: a **single** crossover buckles at every length, and the base's
**orientation** is worth 9.65×, because two crossovers react a moment as a couple and a couple has an axis.
(`C-0023`, `C-0025`, `C-0028`, `CH-0027`, `CH-0031`, `CH-0037`, `CH-0038`.)

**And the branch it depends on has since closed at §3's *desired* stroke.**
`T-67` found that the 90° routing **does** exist — both links close covalently with zero unpaired
nucleotides, and the optimum is a scaffold excursion — but that a duplex **end** has only **two strand
termini**, so its base is a hinge with a lever arm bounded by the duplex's own radius, 1.0 nm, against the
1.345 nm the design needs. A couple goes as the square, so the base is **3.34× short of a hard ceiling**, and
the two links lie on a chord, leaving one axis free — **which is the axis the column buckles about**.
**What survived at the time was a crossover-hinge flexure instead: `E5g16`, a 12.24 nm = 36 bp guided arm on
16 antiparallel crossovers**, 2.04 pN per crossover against a 10 pN allowable, **with no member in axial
compression and no 90° junction anywhere in the design**. (`C-0029`, `CH-0039`, `CH-0040`.)

**Its `16` has since been withdrawn, and with it the `g16`/`a16` designs.**
A crossover serves one *interface* every 32 bp = 10.88 nm, so a hinge line of `n` needs `(n−1)×10.88 nm` of
collinear interface — **sixteen needs 163.2 nm, i.e. 4.08 tiles** — and the complete 32-phase census of a
40 nm tile gives **four**, at every phase. Sixteen can be assembled from four interfaces of four, but
interfaces compose in **series** and are worth 14.6 % of their count. The surviving hinge design is
**`E5a1`, one crossover per flexure** (`C-0040`).
Its arm was then solved as the elastica it is rather than composed: `C-0034`'s bracket premise — *"two errors
run opposite ways and very nearly cancel"* — is **false**, both readings correct the same linear
boundary-value problem and both stiffen it, so the exact composition lands **outside** their span
(`C-0039`, `CH-0053`).
And the tangent figures above were read against a ceiling owed at the **placement** stroke: `C-0023`'s
40 pN/nm is exactly `1.2 × (100 pN / 3 nm)`, a declared linearity tolerance carrying the placement stroke
inside it, so the same construction at §3's desired clause is **12 pN/nm, not 40** (`C-0049`). There is no
upper bound on a coupling tangent anywhere in the acceptance stack; what binds beyond the working point is the
per-path unzip allowable, `n·allowable/s`, which tightens as `1/s`.

**The literature finding that started it stands.**
No publication was found in which a duplex stands normal to a single-layer origami sheet as a stand-off:
out-of-plane duplexes there are hairpin or staple-extension *overhangs*, perpendicular helices in origami are
perpendicular *within* the plane, and **every body standing on an origami plate that has actually been built
is held by a pin** — which this programme has just shown to be a *mechanism*, `P_c = 0` exactly.
The only rigid out-of-plane mounting in print is **triangulated**, not clamped.
**This is the largest single buildability risk in the coupling branch**, it is stated rather than assumed
away, and it is `T-66` and `T-67`. (`C-0028`.)

**But the sheet's own crossovers are not the inventory a hinge must come out of.**
A square-lattice helix has **four** crossover azimuths at 8 bp intervals and a single-layer sheet occupies
**two** — `8 bp × 33.75°/bp = 270°` exactly, so the unoccupied pair points **out of the sheet plane**, and at
B-DNA's preferred twist it is off-register by **half** what the sheet's own next in-plane crossover is
(4.286° against 8.571°, the departure being linear in the offset). A 40 nm tile offers **161–176** junction
sites and builds **49–56**: it occupies **under a third of its own lattice at every phase**. So a hinge
rooted upward consumes **no** interface crossover, the sheet stays in one piece at every count, and the
hinge ceiling is **52–60** rather than the 42 a pigeonhole on the built crossovers gives. What it does *not*
buy is §3's 45 on a 40 nm tile: an upward line belongs to one duplex, so its roots sit at 10.88 nm against an
arm demanding 11.82, and the buildable count is **34** — against 25 in plane, and 45 only on a 49.25 nm tile.
**The site is published geometry and the free lever on it is not** — 62 recorded queries find no
crossover-rooted flexure hinge in print, and every published origami hinge is an ssDNA connection.
(`C-0055`, `CH-0068`; the geometry read directly from Ke et al., *JACS* **131**:15903.)

**Two of this programme's structural results are the same theorem at two scales**: an anchor's orientation
decides everything and its material almost nothing (`C-0014`), and a **joint** cannot be stiff in one
direction and soft in another unless it bends (`C-0025`).
Both are convexity, and both are escaped by the same thing — bending is signed, and has a direction.

**Orientation decides the anchoring problem twice over.**
`C-0014` found that an anchor's orientation *relative to the layer* decides everything and its material almost
nothing; `C-0020` finds the same for its orientation *within the plane of the sheet, relative to the helices* —
worth 11.75× in the effective allowable, at no cost in material, count or stroke.

Methodologically, the finding worth most is that **this project repeatedly caught itself** — concluding a
direction from the corrections it happened to have (`CH-0002`), quoting a `χ` that was a units error assembled
from an abstract (`CH-0012`), sampling a layout space it believed it had swept (`CH-0014`), trusting two
models that agreed with each other because they shared a defect (`C-0011`), quoting a loop expansion of one
field as the uncertainty on another (`CH-0019`), and carrying a number that was right only because two
convention errors cancelled (`CH-0020`).

---

## 2. The eight tasks of §6

| # | Task | Leaf | Verdict | Claim |
|---|---|---|---|---|
| 1 | Stiffness of the polymer layer | `A2.1` | **PASS**, then **superseded twice** | `C-0001` → `C-0003` → `C-0011` |
| 2 | Feasible design window | `A2.1` | **PASS** — non-empty at 7 and 10 nm, empty at 5 nm; P2 closed by `C-0017` | `C-0016`, `C-0017` |
| 3 | Stroke and blocking force vs bias | `A2.2` | **PASS** — reachable, but the operating point is not holdable | `C-0008`, `C-0012` |
| 4 | Electrostatic softening and pull-in | new | **PASS** — both branches answered, each for a different load line | `C-0018`, `CH-0017` |
| 5 | Load distribution across the origami | `A1.2` | **PASS** | `C-0006`, `C-0009` |
| 5b | Deflected shape of the tile | `A8.2` | **PASS**, verdict *rigid plate rejected* | `C-0006`, `C-0009` |
| 6 | Validity boundary of mean-field screening | `A7.4` | **PASS** | `C-0005`, `C-0008` |
| 7 | Poroelastic drainage time | new | **PASS** — not binding, boundary named | `C-0004` |
| 8 | Tile positional variance | `A1.2` | **PASS** at the operating point, **partial** against the leaf | `C-0010` |

### Task 1 — stiffness

`L₀ = N a^(5/3) σ^(1/3)` is **replaced**, not merely re-parameterised.
It is a *two-body* result; the des Cloizeaux interaction gives `σ^(5/13)`, and the blob construction gives
`σ^(1/3)` again only because it minimises against blob elasticity rather than Gaussian.
So "which height law" reduces to "which elasticity", which is a checkable material question — and the check
says PEG in water is a **marginal** solvent whose Gen-1 chains carry 0.02–0.10 of one thermal blob.
The chains are not swollen, there are no blobs, and every blob-based statement made about this layer across
three iterations was about a structure it does not have.

Stiffness is **not a single number at the resting height** and never was: the strong-stretching pressure
vanishes quadratically at `L₀`, so three of six models give exactly zero there.
Quote it at a stated compression. At the working point, 47.7–64.1 pN/nm over the 40 × 40 nm tile.

### Task 2 — the design window

**Answered, and the answer changes shape halfway through.**
In the axes §4(a)–(d) names the window is **not empty**: `σ ∈ [0.0116, 0.2885] nm⁻²` at 10 nm — 24.8× wide —
and `[0.0296, 0.0496]` at 7 nm.
**5 nm is empty**, and the proof names two constraints: the layer must be at least `σ = 0.0751 nm⁻²` for its
coils to overlap at all and at most `σ = 0.00563 nm⁻²` to deliver 3 nm of stroke, **missing each other by
13.3×**.
At both surviving heights the lower edge is coil overlap and the upper edge is the 3 nm stroke — §4(a)'s own
tension, quantified. **§4(c) and §4(d) bind nothing anywhere**, at any of 183 grid points.

**The window must be read in the FORCE-ONSET convention** (`L₀` is where the layer carries 1 pN over the tile).
It says: order **PEG of 1.6–3.3 kDa at 10 nm**, or 1.1–1.2 kDa at 7 nm, at a grafting spacing of 2–9 nm.
In the first-moment convention the same layer is ~8–9 kDa.
**That factor of four is the single most likely way this window gets misread at a bench**, and `T-1e` has not
yet separated the definitional part from the physical.

Then the shape changes.
**Seven of the eleven axes this programme discovered are not functions of grafting density at all** — flatness
(45 attachments as 3 × 15, against 56 crossovers), the usable bias window, and the output-coupling stiffness.
They cannot narrow a window; they can only close a height.
So a `(σ, L₀)` window is the wrong object for the Gen-1 decision, and the two axes that *do* resolve in `σ`
both survive: the peak per-load-path force is 3.9–8.9 pN against a 10 pN unzip allowable everywhere inside the
window — **the exceedance `C-0015` found is unreachable, because the solved layer is never as soft as the soft
end of its sweep** — and **lateral confinement is no longer a footprint cost**.
With the in-plane load path solved rather than stood in for, a tether **aligned with the helices** carries a
concentration factor of exactly **1**, so it needs 33.5 nm at §3's *desired* 10 nm stroke — a ~107 nm assembly,
which is what the *acceptable* 3 nm stroke already cost.
`C-0024` sharpens both halves: the 33.5 nm rests on a 48 pN allowable that presumes a 30 bp joint, so a
realistic 16 bp staple extension makes it **39.4 nm**, while the **same staple split across two duplexes** —
costing nothing in material, layout or stroke, and *relieving* the crossovers — makes it **27.7 nm**.
And the 54.9 pN preload is **25–186× what `T-13` actually needs**, so it is a tax rather than a price:
`L_min` is a corner of the design space, and the length delivering exactly `C-0021`'s hold-down at the 10 nm
stroke is 116.6 nm.
**The entry topology's value is that it widens the admissible length axis, not that the shortest tether is
the one to build.**
The 93–227 nm figures rested on `C-0009`'s out-of-plane factor applied to an in-plane load (`CH-0021`).
**The cost moves into the normal direction instead**: at the minimum tether length the tension *is* the
allowable, so the preload is `n_t A √(2A/S)` = **54.9 pN for four tethers, independent of the stroke** and 55 %
of §3's own 100 pN target — a `T-13` problem that did not exist before.
And the whole gain is conditional on alignment: misaligned, `L_min` is 115.9 nm, *worse* than the figure it
replaces. (`C-0020`.)

**What decides the programme is the output coupling, and `T-16` has now evaluated it.**
At 10 nm the §6 operating point is statically unstable at §3's own 2 mM buffer, so it exists only against a
lever supplying its own stiffness — but that stiffness is **33.333 pN/nm, fixed by §3's 100 pN and 3 nm
alone**, and it clears the stability floor at every height, buffer and layer model in the box.
`C-0016`'s `P2` therefore closes **non-empty at 7 and 10 nm**.
What a DNA lever cannot easily be is *compliant* enough: forty-five duplexes in tension are 4950 pN/nm,
148× too stiff, and the element that closes the task is a 10–19 nt ssDNA spacer carrying **99.6 %** of each
path's compliance.
The margin at 2 mM is 1.19–1.42× against a 123–214 % mean-field error, so this is **not excluded rather than
established**, and `T-1f` — bounding that error — is now the binding uncertainty in the programme.

Two further findings travel with it.
**The bias ceiling has to be quoted with the load it was evaluated at**: `C-0012`'s 0.02–0.1 V is a property of
the *unloaded* actuator, which snaps to near-contact, while the tile held at the §6 target sits at a 2–7 nm gap
and `φ ≤ 0.09`, inside every upstream validity range (`CH-0015`) —
and solved on its own load line rather than read off a grid, the *unloaded* ceiling is itself **0.085–0.595 V**
(`C-0018`).
And **static stability wants the thin layer, whose window is empty, while the window, the stroke and the
force-versus-height trade all want the thick one** — all three pull the same way and stability pulls against
all three.
That inversion, not any single number, is the Gen-1 design problem.

**§3's *desired* ~10 nm stroke remains unreachable** at every height and every grafting density — `C-0001`'s
one surviving headline, now confirmed against a third layer model and a fourth constraint set. (`C-0016`.)

**And it is now settled, with a mechanism, by a bound that contains no coupling at all.**
The stroke *is* the layer's compression — `s = L₀ − h`, so `s < L₀` identically — and §3 names no layer
taller than 10 nm, which makes a 10 nm stroke on a 10 nm layer the statement `h = 0`.
Over 66 states the best any device can do is a **kinematic ceiling of 9.790 nm** (1.02× short), a
`C-0002` **validity ceiling of 8.959 nm** (1.12× — and it is `C-0018`'s own binding bias ceiling at
10 nm), and a **dead-load stroke at 100 pN of 7.424 nm** (1.35×).
A coupling can only *reduce* the last of these, so the free stroke is the supremum over **every**
coupling — which is why this is a claim rather than a search.
Across the catalogue **0 of 14 elements clear the desired stroke and 3 of 14 clear the acceptable one**,
and the telling row is `C-0023`'s `E5`, which clears every coupling-side predicate at 10 nm and fails
only on the **reach**.
The established statement is therefore **unreachable on §3's own stack** — stronger than "with this
catalogue", weaker than "in physics" — and the only escape is a **taller layer, 16.63–26.12 nm**, which
is a specification question (`T-115`). Tile size, superstructure perforation and buffer cannot
substitute, because none of them is a layer height. (`C-0050`; `C-0046`, `C-0039`, `C-0040`, `C-0041`
agree from four independent directions.)

**And the mean field the window is computed in is itself broken there.**
The polymer Ginzburg parameter is 0.30–1.71 across the window and 1.30 at the design point, so the one-loop
correction exceeds the term it corrects and the expansion supplies no bound at all.
The window survives anyway, because the disjoining pressure is *conformational*: destroying the interaction
entirely costs 9.4 % of the layer stiffness, and both windows in fact **widen**, by 13.4 % at 10 nm and 1.8 %
at 7 nm. (`C-0019`.)

### Task 3 — stroke and blocking force

**Reachable, and the operating point it is reachable at is not one the device can be held at.**
100 pN of blocking force needs 0.065–0.699 V and 100 pN *at* a 3 nm stroke needs 0.082–0.368 V, all inside the
~1 V point-ion boundary with 5–12× of margin.
The actuator is **voltage-saturated above ~0.5 V** — a factor of 8 in bias buys 1.9× in force — so §3's 2 V
ceiling is nearly irrelevant to what the device can do.

But **`k_eff < 0` at the loaded operating point at 7 and 10 nm**, and the *free* operating point leaves an
upstream validity range above ~0.1 V — **one** range, `C-0002`'s `φ = 0.2`, and not the three reported here
until `C-0018` checked them: `C-0005`'s 1.46 nm correlation band and `CH-0007`'s 1 V point-ion boundary are
never reached at all.
Two consequences that a single "bias needed" figure hides: **the blocking force understates the peak output
force by up to 20×**, because `dW/dh = k_eff` exactly and the characteristic *rises* with stroke wherever the
field softens the layer; and **the two halves of this task run in opposite directions with layer height** —
blocking force 10× harder from 5 to 10 nm, stroke 10× easier. (`C-0012`.)

### Task 4 — electrostatic softening and pull-in

**Both branches answered, each for a different load line — because a ceiling belongs to a `(bias, load line)`
pair and not to a device.**

For the **coupled** device the usable bias is **0.097–0.425 V**, and the ceiling is `C-0002`'s `φ = 0.2`
crossover at 43 of 54 states.
**Pull-in binds at only 11 of 54**, all of them 10 nm in 2 mM, where it is **0.130–0.184 V against an operating
bias of 0.128–0.180 V** — a margin of **1.007–1.032**, the thinnest anywhere in the programme.
`C-0017`'s comfortable-sounding 1.19–1.42× is a *stiffness* margin, and a stiffness margin is not a bias
margin: `V(s)` is flat at a fold, so 19–42 % of stiffness buys 0.7–3.2 % of bias.
**0.5 mM removes the fold entirely** (1.29–2.36×), which is leaf `A2.2`'s low-screening condition arriving a
fourth time.

The **unloaded** tile has **no pull-in at 49 of 54 states** — so §6's own second branch, *"the osmotic
divergence removes the instability"*, is true, of the free tile and of nothing else — and its ceiling is
**0.085–0.595 V, not the 0.02–0.1 V** carried until now.
A **dead load** has no stable compressed equilibrium at any bias wherever it folds, its ceiling degenerating
to `C-0008`'s blocking bias, reproduced to 2.3e−3 by an independent construction.

**And the arrest is osmotic after all.** `CH-0011`'s feature is real and is now four executable tests —
`|F_es|` is non-monotone, its peak lies below 3 nm, `k_es` changes sign there — but two counterfactuals at 324
states put the osmotic stopper at the larger gap **everywhere**, by 1.9–5×.
Passing the point where a force stops growing is not being stopped by it.
(`C-0018`, `CH-0017`, correcting `C-0012` and `CH-0011`.)

**And every stroke in this programme is measured from `L₀`, which is a height the tile never occupies.**
On the device the programme actually committed to — two-sided, tetherless — the delivered stroke is
**2.973–2.982 nm** at the 10 nm design point, a **0.6–0.9 %** shortfall.
The 2–13 % that `CH-0024` reported is a property of `C-0014`'s eight substrate tethers, which supply 92 % of
that stack's hold-down and which `CH-0027` removed the same day (`CH-0036`) — and those tethers are worth
**four grid steps** of the 10 nm window and three of the 7 nm one, where the tethered reading leaves it
1.230× wide, one grid step from empty.
No verdict moves; the *statement* does. (`C-0021`, `C-0023`, `C-0027`, `CH-0024`, `CH-0036`.)

**Every electrostatic force in this programme is a one-dimensional pressure multiplied by 1600 nm², and that
understates the force on the finite tile by 5–19 %** (`C-0022`, `CH-0026`) — 14.7 % at the design point,
25.8 % on a 20 nm tile, and equivalent to a sub-Debye 1.65 nm collar on every side.
It is favourable to every force clause and — at the **force-pinned** operating point the device is actually
held at — favourable to the stability clauses too: the level term is absorbed entirely into the bias and only
the collar's *gradient* survives, which lengthens the decay and takes the 10 nm / 2 mM margin from
1.19–1.42× to 1.34–1.67×.
The unfavourable direction `CH-0026` asserted holds only at fixed **bias**, i.e. for the free tile (`CH-0035`).
Either way it is an order of magnitude inside the standing 123–214 % mean-field uncertainty.

### Task 6 — mean-field screening

The answer is **yes and no, and the two halves have different reasons.**
Mean field is **uncontrolled** across the whole 5–10 nm working range (the one-loop correction is 123–214 % of
leading for Mg²⁺) yet **qualitatively safe** there, because correlation attraction needs a gap under 1.46 nm
and the layer never allows it. Controlled PB begins only above 12.9 nm.
`Ξ ∝ q³`: the divalence does this, not the surface charge — Na⁺ at the same surface gives 3.0 against 24.

### Task 7 — poroelasticity

**Not binding, by 22× at the §3 worst case and 5.6× under a composite worst case.**
Drainage is a *footprint* problem, not a thickness problem — the thickness cancels and `τ ∝ L²` in the tile
edge — and a **denser** layer drains *faster*, so the binding direction is dilution.
The design would have to leave the poroelastic model's own domain of validity before poroelasticity could bind.

### Task 8 — positional variance

**PASS at the operating point**: 0.87–0.96 nm broadband, 0.069–0.110 nm in band below 1 kHz, against 3.0 nm.
Two qualifications travel with it. The tile's **worst point** (a corner — the centre is the fixed point of both
tilts and therefore the *quietest* place on it) exceeds 3.0 nm in every state softer than the working point.
And the **lateral coordinate is not part of the PASS at all.**

Leaf `A1.2` asks for a *simulated* σ_RMS with a **95 % CI**, and that half is **not discharged**.
The reason is not cost: oxDNA models the origami and **not the polymer layer that sets the answer**, so run as
specified it returns a confidence interval on a different quantity.
A CI on an exact analytic result is a category error, and the model bracket is not one.

---

**And the unbiased state now has an answer of its own.**
`C-0021` computes it by exact Boltzmann quadrature rather than equipartition — the zero-bias potential is
harmonic below the rest height and **linear** above it — giving **0.360–0.501 nm broadband and
0.019–0.041 nm in band** against the same 3.0 nm predicate, with the tile spending up to **53 % of its time
above `L₀`**, where the layer holds it with nothing at all.
The harmonic reading understates that amplitude by up to **2.6×**.
And the requirement there is a **force** or a **stiffness** depending on the **topology of the coupling**,
the two being exactly one power of the bound apart (`F_req = k_req·σ`).
With the *one-sided* coupling the programme had committed to it is a force, `k_BT/3 nm = 1.381 pN`, and
nothing in the §3 stack supplies it: the tetherless device is a 1.4–5.4 `k_BT` trap and its RMS is
**2.56–12.98 nm**, failing leaf `A1.1` at 15 of 18 states.
With a **two-sided** coupling it is a stiffness, `k_BT/σ² = 0.4602 pN/nm`, which §3's own mandated
33.333 pN/nm exceeds **72.4× unpreloaded** — and the same device becomes **959–7582 `k_BT`, 18/18 confining,
0.217–0.352 nm broadband and 0.012–0.035 nm in band**.
Three DNA elements deliver it, and the eight substrate tethers `C-0021` needed leave the design.
(`C-0021`, `C-0023`, `CH-0027`.)

## 3. The open questions of §4

| | Question | Answer |
|---|---|---|
| (a) | Grafting density and regime | **Four brush criteria have failed here**, each a convention asked to do a measurement's work: `Σ ≥ 5` failed thermodynamically *and* geometrically; `L₀/R₀ ≥ 1`, adopted to replace it, turned out **exactly vacuous** — it admits all 183 points of the sweep. **Coil overlap `Σ = πR₀²σ ≥ 1` is the only criterion that bounds anything**, and it owns the lower edge of every surviving window. Window: see Task 2. |
| (b) | Layer height | **Empty at 5 nm only** — the earlier "empty at 7 nm too" was withdrawn when a solved density profile replaced two trial functions. There is a genuine **trade**, not an ordering: the window, the stroke and the force all want the thick layer, and static stability wants the thin one. |
| (c) | Porosity and ion partitioning | **The sign in the question is backwards.** The layer *excludes* 23–48 % of the salt, so it **lengthens** the local Debye length by 1.14–1.39× and **protects** the field rather than screening it away. It also *amplifies* `F_es` by 1.15–1.60×. The dielectric-decrement mechanism named in §4(c) is a 3.9 % effect — the layer is 97 % water. **The bound is one-sided** (exclusion only); cation coordination by PEG's ether oxygens could flip it, and no binding constant exists in accessible literature. |
| (d) | Poroelasticity | **Not binding**, with the boundary named. See Task 7. |
| (e) | Screening | See Task 6. The force's own decay length is **1.8–2.8 nm** at the working gap — not the 4 nm bulk Debye length, and it is bias-dependent. Leaf `A2.2`'s low-screening operating point is **vindicated twice**: at 10 mM the 100 pN target is unreachable at 7 and 10 nm, at 0.5 mM it is reached even at 10 nm. |
| (f) | Structural survival | The **35–60 pN band is not a per-load-path allowable** — it is a whole-cross-section disassembly force at a stated loading rate, and a DNA rupture force without a loading rate is not a material constant. Per path use duplex shear (~48–65 pN) or unzip (10–15 pN), 65 pN a hard ceiling. **Three load paths clear 35 pN, eleven clear 10 pN — and 45 (as 3 × 15, not 64 as 8 × 8) are needed for flatness, against 56 crossovers.** A rigid anchor is carried by its **two nearest crossovers and essentially nothing else**, so an equal-share figure understates the peak by 2.3–7.6× **out of plane** — but inside the actual design window the peak is 3.9–8.9 pN against a 10 pN unzip allowable, so **the exceedance is unreachable there**. **The exact-zero load path of a one-row-per-duplex grid survives a realistic load with 20× to spare**
(`C-0026`): under the edge profile `T-3b` actually solved it restores 0.150 pN (0.332 worst case) against a
2.222 pN static share, and what breaks it is attachment-stiffness scatter at 0.883 pN per unit relative
amplitude — even a coupling with every second path at 1 % of nominal stays 12× clear of unzip.
So the crossover path never becomes binding for a distributed coupling, out of plane as well as in.
**In plane the factor is different in kind**: a lateral tether collects nothing from the layer, so the peak is a *fraction* of its own tension — `η = 1.0000` aligned with the helices, up to 2.33 misaligned, and the staple layout is worth **exactly nothing** on the binding path (`C-0020`). ~~**And the desired stroke puts a floor under the path count**: at 10 nm the mandated coupling delivers 333 pN, so the 10 pN unzip allowable alone needs **≥ 34 load paths**, a fourth and tightest route to 45 (`C-0025`).~~ **WITHDRAWN by `CH-0059`/`C-0049`.** The 34 is not a material property but a property of the **placement convention**: it is `33.333 × 10/10`, i.e. §3's *desired* stroke read on a coupling placed for its *acceptable* one, and the same allowable gives **10** under the other reading. The per-path ceiling it derives from is `n·allowable/s`, a bound on a **force**, so it *tightens* as `1/s` — 150 → 45 pN/nm at 45 paths, and 50 → **15** at the 15 paths `C-0041` shows the tile actually carries. **And the per-path allowable is itself a function of the bonded length** (`C-0024`, `CH-0029`): 48 pN is Strunz's **30 bp** number, while a realistic 16 bp staple extension gives 34.8 pN and an 8 bp one 18.8. **What a tether bonds to is a sequence-design choice, and it is worth more than the sheet is** — the sheet's answer is pure arithmetic (a bond spanning `m` duplexes enters at exactly `1/m`, floor `1/D`, ceiling 720 pN), while the joint moves ×2.5 over the realistic 8–20 bp range, and **splitting a bond across two duplexes wins above a 14.3 bp total bonded length and loses below it**. |
| (g) | Does the tile stay flat? | **No, and the irreducible part is now a number.** Rigid *exactly* under a uniform load at any rigidity, and dishing 26–369 % of the stroke under every departure. The part that **cannot be designed away** is the electrostatic edge effect, now solved in 2-D (`C-0022`): the rim *gains* load rather than losing it, and the dishing it forces is **32 % of the stroke**, and **45 attachments is the count at which further attachments stop buying flatness, not the count at which the tile becomes flat** — under the solved load the dishing saturates at 0.149 of the stroke between 45 and 225 attachments (`CH-0034`) (21–44 % over the foundation sweep, 30–32 % on the discrete lattice). So the lever and the area-averaging sensor differ by **32 % of the stroke whatever the coupling does** — `C-0012`'s 11 %–369 % band was a statement about the number of attachments, which is a design choice; this is not. §4(g)'s own criterion for abandoning the rigid-plate picture is met, and met by a load nobody chose. **REVISED, iteration 9 (`C-0058`): it CAN be made flat, and the axis is the coupling's *distribution* rather than its size.** `C-0017`'s mandate is an equality on a **sum**, and every claim above shares it equally between the paths. Freeing that — same 33.3333 pN/nm, same 45 attachments, same solved load — gives **0.0753** of the stroke under a one-parameter rule (*the 34 stations within 6.7 nm of an edge carry 5× the other 11*) and **0.0544** under a full optimisation, both inside `T-5b`'s 10 %, at 2.762 pN per path against a 10 pN allowable. **So 0.149 is where the equal-spring family saturates, not a floor** (`CH-0071`). Three qualifications travel with it: it is owed at **one operating state** (the same design dishes 0.187 at the 2 nm gap, where the uniform coupling gives 0.071, and no distribution found is flat at all five); it needs **three attachment columns**, which `C-0041`'s packing forbids (at the buildable 1 × 15 the axis buys 13 % and the coupling is still 1.96× worse than none); and the 5:1 per-path ratio **can** be built but the array **cannot** be placed. **`C-0060` (`T-122`, iteration 10)**: all seven settings of the five catalogue elements reach both levels — one base pair is 1.0–19.1 % of a level's own stiffness against a flat ratio window measured at `3.5 ≤ R ≤ 20`, i.e. 25× finer than the requirement, where `C-0023`'s *preload* quantum was 8.3× coarser than its own, because a preload is a length and a stiffness is a **power** of a length — and all fourteen built designs are flat (0.0715–0.0815). The mandate survives only because it is a **sum**: rounding the two levels independently misses `C-0017`'s equality by up to 5.44 %, recovered to `1.3e−4` by moving individual paths one base pair, at the price of 3–4 distinct staple lengths. The tolerance is a **threshold**, 34.6 % relative scatter (`T-45` is still unmeasured), 2.04× `C-0026`'s break-even. **What fails is the placement**: `k ∝ span^(−3)` makes the soft level 1.71× the longer member, so `C-0030`'s interior span is 52.36 nm on a 40 nm tile and six of seven elements place 0–30 of the 45 stations — `C-0041`'s obstruction unchanged and made worse (`T-127`). One free improvement: the best one-parameter ratio at the same collar is **7, not 5** (0.0653 against 0.0753). |

---

## 4. §7 — what NDI would count as the loop working

| §7 criterion | Where to check it |
|---|---|
| Inherited numbers get re-derived | `a = 0.35 nm` closed two ways (`C-0002`); `λ_D ≈ 4 nm` re-derived to 1.8 % (`C-0005`); the de Gennes wall mapping derived, not looked up (`C-0001`); the MWC form rebuilt rather than cited. |
| Premises checked against the material | The semidilute premise **failed** (`CH-0001`); the Darcy premise **failed** where it did not change the verdict (`C-0004`); strong stretching is outside its own premise (`CH-0003`); `χ ≈ 0.45` turned out to have **no primary source at all** (`C-0007`). |
| Method justified against cost, cheap bound first | SCF numerics deferred twice on the stated ground that it would be *"calibrating to a guess"*, and bought only once the interaction was anchored in measurement (`T-1d`). MD declined for the Hofmeister effect because it would be *worse* than the existing measurement, not merely dearer (`C-0007`). Explicit-ion MC costed at 1–3 weeks and not run (`C-0005`). `T-15` built an in-plane *sibling* lattice rather than adding degrees of freedom to the out-of-plane one, on the ground that the two decouple exactly for a flat sheet and merging them would have forced re-verification of four published claims for a change that cannot move them. `T-19` bounded all four entry topologies by a cut-equilibrium pigeonhole before assembling a matrix, which settled two of them outright and redirected the footprint question from the lattice to the literature — where its answer was. |
| Validity ranges travel and are respected | `C-0008` is handed to `T-3` with an explicit may/may-not list; `C-0004` is parameterised by a stiffness that was being re-derived concurrently. |
| Disagreement raised as a challenge, not an overwrite | Twenty-nine challenges in [`gpd/challenges/`](gpd/challenges/). `CH-0007` challenges **our own** queue's reading, not a subordinate's, and `CH-0021` corrects a factor `C-0014` had itself flagged as a stand-in — finding it wrong in **both** directions. |
| Model-consistent vs measured maintained | Every claim header carries it; no `PASS` in this repository asserts measurement. |
| A feared effect chased down and *dissolved* rather than carried | The "grafted `χ` ≈ 0.60", once thought 239× the salt effect, turned out to be `1.2 × ½` assembled from an abstract, against a model whose own theta is 0.696, for the wrong geometry and the wrong observable (`C-0013`, `CH-0012`). |
| Unanswerable questions stated plainly | The Mg²⁺/PEG binding constant does not exist in accessible literature (`P-8`); the crossover hinge constant is a fitted model input (`T-9`); leaf `A1.2`'s CI is **not discharged** rather than approximated; the intermediate-coupling regime has no systematic theory and the sources say so themselves. |

---

## 5. What we cannot answer, and why

- **The 95 % CI of leaf `A1.2`.** Requires an ensemble; the named tool models the wrong subsystem.
- **The Mg²⁺/PEG coordination constant.** Two independent searches; the mechanism is documented in water, the
  number is not, and the quantitative NMR work is in methanol. It needs a paywalled pull or an experiment.
- **The crossover hinge constant `k_θ`.** No accessible measurement of a single-layer origami sheet's bending
  rigidity exists in any direction. Costed as `T-9` — days of oxDNA on 8 cores.
- **The direction of the correlation correction for *oppositely* charged walls.** Every published coupling
  criterion is a like-charge result. This is the largest uncertainty on every electrostatic force here.
- **A compression measurement of a PEG brush *inside* the Gen-1 grafting window.** None exists. `P-9` bounds
  the bulk-versus-brush `χ` difference at `|Δχ| ≤ 0.053` from *denser* layers, so the bound comes from above
  and assumes monotonicity in grafting density.
- **Two paywalled papers** would close the only genuinely missing measurements. This is an access limit, not a
  compute limit, and it is the first thing this programme has needed that the machine cannot supply.

---

- **A fluctuation-corrected density profile for the layer.**
  The expansion whose saddle point is the SCF is broken at `φ ≈ 0.01`, so the correction cannot be *computed*
  — only bracketed by re-running the mean field over the range the broken series licenses. The method that
  would settle it is a field-theoretic (complex-Langevin) simulation, costed at weeks; the nearest published
  substitute is paywalled with no repository copy. **The bracket is ≤ 9.4 % on stiffness, so nothing turns on
  it.** (`C-0019`.)
- **Whether `C-0018`'s pull-in bias itself moves.**
  The polymer softening and the finite-tile enhancement cancel at the fold to within the collar gradient's own
  difference-scheme spread — the coupled tangent runs −2.5 to +4.0 pN/nm. This is not a missing measurement:
  `T-3b`'s own solver, run on the equilibrium path rather than at six sampled gaps, would settle it (`T-60`).
  It is the only unresolved margin left that a cheap calculation can close. (`C-0027`.)
- **The thermal force in a crossover.**
  It is `√(k_BT k_v)` with `k_v` the crossover's vertical stiffness, which nothing measures (`T-9`); the
  programme's lattice models it as a rigid penalty, in which the *static* force converges and the
  *fluctuating* one provably does not. Bracketed at 2.78–115.8 pN and reported as a bracket. (`C-0026`.)
- **Whether a duplex can be routed at 90° out of a single-layer sheet at all.**
  `T-40` consulted the literature `C-0025` had not, and found **no published instance** — and the two obvious
  motifs are excluded, because a nicked continuation preserves the helix axis and the antiparallel crossover
  requires parallel helices. The base condition every published out-of-plane element actually uses is a
  **pin**, which is a mechanism here. A buildability question, not a modelling one. (`C-0028`, `T-66`, `T-67`.)
- **Which body carries the standoffs, and what sits under the flexure's midspan.**
  The coupled joint's law is not odd, so one mounting gives a 5–10 nm window and the other none — and the
  favourable one costs a clearance that makes §3's *desired* 10 nm stroke unreachable at any standoff length
  inside the coupling's own envelope. A specification gap, not a modelling one. (`C-0030`, `T-75`, `T-78`.)
- **Whether a strain-softening coupling still satisfies the stability clause.**
  The assembled tangent's minimum, 22.88 pN/nm at a 4.55 nm stroke, sits inside `C-0018`'s own `|k_eff|`
  band. (`CH-0042`, `T-76`.)
- **Whether a flexure array on a shared superstructure stays as compliant as independent leaf springs.**
  `C-0023` models 45 of them as independent, which is the *compliant* reading — and the compliance ceiling is
  the binding side, so the assumption is not conservative. (`T-31`.)
- **What the electrode is made of.** §1 says *"patterned electrode"*.
  Metal against oxide is **2.6×** on the van der Waals hold-down, which is the one term no design can remove.
  A specification gap, not a modelling one. (`C-0021`, `P-13`.)
- **Where the electrode's potential of zero charge sits.** A contact potential of **0.9–5.1 mV** — below
  anything a bench would call zero — supplies the entire zero-bias hold-down, and nothing in §1 or §3 fixes it.
  A measurement, not a calculation. (`C-0021`.)

## 6. Reading order

For the process, [`JOURNAL.md`](JOURNAL.md) — decisions and surprises in order, including the ones that
reversed earlier conclusions.
For the live state, [`TASKS.md`](TASKS.md).
For any number in this file, the claim it belongs to in [`gpd/claims/`](gpd/claims/), and the challenge
standing against it in [`gpd/challenges/`](gpd/challenges/).
