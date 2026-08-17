# T-70 — What holds `E5g16`'s guided arm, and does `c = 12` survive its own anchorage?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme |
| **Raised by** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md), *"Still open"* item 1 — **the largest open item under the only surviving design** |
| **Verification type** | **logical** (the same counting theorem `C-0029` proved, applied at the arm's *far* end instead of the standoff's base) **+ in-silico** (the arm's own two-spring boundary-value problem solved exactly, `C-0029`'s `RotatingHingeArm` pipeline re-run as a library on the realised end factor) |
| **Status** | Formulate + Plan below; executed by `anchoring.GuidedArmAnchorageStudyKt` |

---

## Formulate

### What `C-0029` left standing on an assertion

`C-0029` closes the perpendicular-standoff branch and hands the Gen-1 output coupling to
**`E5g16`** — *"a 12.24 nm = 36 bp **guided** arm on 16 crossovers"* — and the whole design turns on
one letter in a cube root:

&nbsp;&nbsp;&nbsp;&nbsp;`r ≤ (c·n·EI/k_target)^(1/3)`, &nbsp; `c = 3` → **9.767 nm**, `c = 12` → **15.50 nm**.

§3's **desired** stroke is 10 nm and `C-0029`'s own reach criterion is `r > 10 nm`, so the cantilever
cap fails it and the guided cap clears it. `C-0029`'s validity range says the quiet part out loud:

> *"A guided arm (`c = 12`) is **asserted, not designed**. What holds `E5g16`'s far end against
> rotation is a second anchorage on the lever, and its own compliance is not modelled here."*

`c = 12` is the **fixed-guided** end condition — the far end may translate but may not rotate. It is
an idealisation exactly as `c = 192` was in `C-0025`, and `C-0025`'s lesson was that no real joint
sits at a textbook limit.

### The question, stated numerically

1. What **rotational stiffness** `k_far` can a realisable origami motif put at the arm's far end,
   given `C-0029`'s counting theorem (a duplex **end** has exactly **two** strand termini, lever arm
   `≤ r_P = 1.0 nm`, and two links on a chord restrain **one** axis)?
2. What is the arm's own end-condition factor as a **continuum** `c(ρ_far)` — not a choice between
   3 and 12 — for the arm's own boundary-value problem, with `ρ_far ≡ k_far r/EI`?
3. What is the arm cap `(c·n·EI/k_target)^(1/3)` **recomputed on the realised `c`**, and does §3's
   **desired** 10 nm stroke survive it? Does §3's **acceptable** 3 nm?
4. Which **axis** does the arm bend about, and does a two-link anchorage restrain that one?
5. Is the anchorage's compliance in **series** with the hinge and the arm — and what is the sign and
   size of what that does?

### Acceptance predicates, declared before the run

| | predicate | falsifiable statement |
|---|---|---|
| **`P1`** | `c(ρ)` is a continuum with the two textbook values as its **exact** limits | `c(0) = 3` and `c(∞) = 12` to machine precision, monotone in between |
| **`P2`** | the realised `c` at the design point is **strictly inside** `(3, 12)` | if it lands at either limit the "continuum" framing is empty and the task closes on a division |
| **`P3`** | the cap recomputed on the realised `c` **exceeds §3's desired 10 nm stroke** | `r_cap > 10.0 nm` for the adopted anchorage |
| **`P4`** | a placed design exists: an arm solving the 33.3333 pN/nm mandate at 3 nm, with `r > 10 nm` and tangent inside `C-0023`'s 40 pN/nm ceiling at both §3 strokes | any of the three failing kills `E5` outright |
| **`P5`** | the anchorage restrains the axis the arm's bending needs, at a phase cost bounded by the base-pair quantum | if the needed axis is the free one, `c` collapses to the chord reading |
| **`P6`** | the far anchorage's own load is carriable: the **couple** it reacts, resolved onto its two links as `M/(2a)`, demands a bonded length inside `CH-0029`'s ladder — i.e. below the 68.1 pN loading-rate-free saturation — at §3's **desired** stroke | a guide is not free: `M_far = FLρ/(2(1+ρ))`, and it is the guide's moment that relieves the hinge. If no bonded length carries it, the guide is unbuildable for the load it guides |

### Units, geometry and sign conventions — restated

- **Units locked**: nm, pN, pN·nm, pN/nm, pressure pN/nm² = 1 MPa. `k_BT = 4.141947 pN·nm` at
  **T = 300 K**, aqueous **2 mM MgCl₂**.
- **Geometry.** The arm is a single B-form duplex of bending rigidity `EI = 230 pN·nm²`, length `r`,
  running between two bodies — the anchoring sheet (near end) and the 40 × 40 nm tile (far end) —
  which **translate** relative to each other by the stroke `δ` and do **not** rotate. The near end is
  `C-0023`'s crossover hinge, `k_near = n_hinge · k_θ`. The far end is the **second anchorage**, the
  subject of this task, `k_far`.
- **Sign conventions.** `δ > 0` is the tile moving **away** from the substrate (the stroke direction).
  A rotational stiffness is positive and **restoring**. End rotations `θ_A` (near) and `θ_B` (far) are
  the beam's tangent slopes relative to the bodies' common plane, positive in the sense of `δ`.
- **`x`** runs along the arm's own axis, **`z`** normal to the sheet, **`y`** in-plane transverse. The
  arm's working bending is in the **`x–z`** plane, i.e. its end rotations are about the **`y`** axis.
- **45 load paths** on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm gives the
  33.3333 pN/nm placement mandate, and the **desired** stroke is 10 nm.

---

## Plan

### The cheap bounds, which run first

Three of them, all arithmetic, all before any root is found.

| | the bound, and what it would settle |
|---|---|
| **1** | **`ρ_far` carries the arm length.** `ρ = k_far r/EI`, and the cap is a *fixed point* — a longer arm makes the **same** joint relatively stiffer. `EI/r` at `r ≈ 12 nm` is **19.2 pN·nm/rad**, and `C-0029`'s own two-terminus ceiling is **78.2** — so `ρ_far ≈ 4`, i.e. `c` lands near the middle of `(3, 12)` and **neither** textbook value applies. If `ρ_far` had come out at 0.01 or 100 the continuum would have collapsed onto an end and the task would close in a paragraph. |
| **2** | **the cap at the two worst readings.** Even at `k_far = 13.53 pN·nm/rad` (the chord axis, `C-0029`'s `B1`) the cap is a fixed point above 10 nm; at `k_far = 0` (a single covalent link, `C-0029`'s `R3` ball joint) it is exactly the cantilever's 9.767. So the verdict is decided by **link count at the far end**, not by the motif's material. |
| **3** | **the series identity.** `C-0023`'s composition `1/k = r²/(nk_θ) + r³/(cEI)` is *exact* when the far end carries no moment and *wrong* when it does — because a guide carries part of the moment and **relieves the hinge**. One line of algebra says whether `c = 12` and that composition can both be true. |

Only because bound 1 puts `ρ_far` in the interior is the two-spring solve worth running at all.

### The method, and its justification against cost

1. **Derive `c(ρ_far)` in closed form** for the arm's own boundary-value problem — a beam whose near
   end is clamped in the bending sense (the hinge's rigid rotation is `C-0023`'s separate series term)
   and whose far end carries a rotational spring `k_far`, loaded by a transverse end force. Superposing
   the cantilever tip-load and tip-moment solutions and imposing `M = −k_far θ_B` gives a two-line
   result. **Cost: free. Falsified by** its limits not being exactly 3 and 12.
2. **Derive the full two-spring `c(ρ_near, ρ_far)`** from the Euler-Bernoulli element stiffness matrix
   with both end rotations condensed out, which contains *both* joints and therefore settles the series
   question of bound 3 without a separate model. **Cost: free.**
3. **Price `k_far` from motifs that exist**, re-using `C-0029`'s counting theorem and `C-0025`'s joint
   catalogue **as libraries** rather than re-deriving them: the two-terminus ceiling
   `2k_bond,θ + 2k_bond,s a²` at four groove readings, the chord axis, a single-nicked continuation
   (one backbone intact — a clamp), a doubly-nicked continuation (which **is** a crossover), a
   multi-crossover clamp at the 32 bp pitch, and a single covalent link (a ball joint).
4. **Recompute the cap as a fixed point** `r = (c(k_far r/EI)·n·EI/k_target)^(1/3)`, and **re-run
   `C-0029`'s own `RotatingHingeArm`** with `armFactor = c_realised` to get the placed design, its
   tangent at both §3 strokes, its rotation, and its per-path forces.
5. **Sweep** `k_θ`'s Chen bracket `α ∈ [0.6, 1.2]`, `k_s` over `C-0020`'s four decades, `EI` at Fields
   et al.'s measured-buckling reading, and the hinge count, because `C-0029` found verdicts that move
   across `α` and `k_s`.

**Why not a coarse-grained simulation.** The binding facts are a **count** (two strand termini at a
duplex end) and a **linear boundary-value problem**. An oxDNA study could only find the joint
*additionally* frustrated; it cannot add a third backbone, and it cannot change the algebra of a beam
with two end springs. `C-0029` made the same call for the same reason. The one thing simulation could
add — the joint's behaviour at 20–36° of rotation — is named as an open item, not simulated here.

### What result would falsify this approach

1. **`c(ρ)`'s limits not being 3 and 12.** Then the boundary-value problem is set up wrong and every
   number downstream is void.
2. **`ρ_far` landing outside `[0.1, 100]`** at the design arm. Then the continuum is decorative and one
   of the textbook values is simply right — the task closes on bound 1 and says so.
3. **The two-spring `c` failing to reproduce the series composition at `ρ_far = 0`.** The two
   derivations are independent and must agree exactly at the cantilever corner; if they do not, one is
   wrong.
4. **`C-0029`'s `E5g16` numbers failing to reproduce** when its own pipeline is re-run at `c = 12`
   (arm 12.242 nm, tangent 33.68/38.68 pN/nm, rotation 7.1°/23.2°, cap 15.5005 nm). Then this task is
   not talking about the same object.
5. **The realised cap falling below 10 nm.** That is not a failure of the *method* — it is the
   answer `C-0029` named as the third way it would fail, and it would mean the programme has **no**
   element reaching §3's desired stroke.

### What this task does *not* do

- It does not re-open whether `E5`'s hinge constant survives 23° of rotation (`C-0029`'s open item 5).
- It does not design a strand routing or a sequence; base pairs make a design statement concrete.
- It does not re-solve the layer, the field, or the coupled operating point.
