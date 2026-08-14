# T-106 — The truss cap as a solved body, not a series spring

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Raised by** | [`C-0037`](../claims/C-0037-triangulated-standoff.md) open item 2, restated by [`C-0042`](../claims/C-0042-paired-perpendicular-junction.md) open item 2 |
| **Verification type** | **logical** (the counting theorem and a steric exclusion, both counts, both cheap) **+ in-silico** (a solved cap: its bending, its torsion, its rigid height, and a beam-column finite element with per-leg head springs) **+ literature** (the torsional constant read directly, and a recorded negative for the motif) |
| **Priority** | **high** — the largest open item under `C-0037`'s recommended design once `C-0042` retired the paired-junction risk |

---

## Formulate

`C-0037` recommends a **partially triangulated standoff**: two duplexes standing normal to the
sheet, laid in a row **across** the flexure's axis, sharing a **cap** that ties their heads to the
flexure's end. Its whole mechanism is the **frame couple**

&nbsp;&nbsp;&nbsp;&nbsp;`k_frame = series(k_a Σd_i², k_tie)`, &nbsp;&nbsp;
`k_a = series(S/ℓ, k_z,base)`, &nbsp;&nbsp; `k_tie = k_link Σd_i²`,

with `k_link = 2 k_bond,s = 64.71 pN/nm` *"forced by `C-0029`'s counting theorem applied at the
other end of each leg"*. `C-0037`'s own validity range says the rest of the cap is **asserted**:

> *"The cap is one rigid body of finite rotational stiffness in series with the legs' axial couple.
> Its nominal value is forced by the counting theorem … Its **geometry** — what physically joins two
> leg heads 2.72 nm apart to one flexure duplex — is asserted, not designed."*

`C-0042` narrowed the row to **7 bp = 2.38 nm** and left the cap untouched: *"this claim places two
**bases**, not a cap."*

**The question.** What body joins the two leg heads to the flexure's end; what rotational compliance
that body has; and what `C-0037`'s frame couple, `C-0042`'s 7 bp recommendation and the buckling
margin become once the cap is solved instead of asserted.

### Acceptance predicates

| | predicate |
|---|---|
| **`Q1`** | The cap's **geometry** is derived, not asserted: what body it is, how long it is in base pairs, where its axis sits, and which alternatives are excluded — each by a stated count or length, not by preference. |
| **`Q2`** | The counting theorem is applied at **every** junction the cap creates, on **both** axes of each chord, and the resulting stiffnesses are named. A junction credited with more than two covalent links fails this predicate. |
| **`Q3`** | `C-0037`'s frame couple is **recomputed** on the solved cap and the departure from its series-spring value reported at every separation from the 6 bp steric floor to 12 bp. |
| **`Q4`** | The head's assembled 2 × 2 flexibility is recomputed and `C-0030`'s coupled-flexure pipeline **re-run** on it — span, tangent, coupling factor, supply/demand, duty. |
| **`Q5`** | Both critical loads are recomputed and the **governing plane** reported at every separation; `C-0042`'s *"seven base pairs"* is either upheld or challenged with a number. |
| **`Q6`** | Every reduction is exact: with the cap rigid, its junctions rigid and its height zero, every quantity reproduces `C-0037` and `C-0042` **entry by entry**. |
| **`Q7`** | Maxwell-Betti asserted on the assembled object between two **independently integrated** off-diagonals, as `C-0030` and `C-0042` both do. |
| **`Q8`** | `P1`–`P9` of `C-0037` re-evaluated on the solved cap over `C-0017`'s envelope, with a verdict per length and per separation, on **both** rigidities. |

### Units, geometry and sign conventions — restated

- **Units locked.** nm, pN, pN·nm, pN·nm/rad, pN/nm; `k_BT = 4.141947 pN·nm` at **T = 300 K**;
  aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at
  the **acceptable** 3 nm and the **desired** 10 nm.
- **Axes.** The sheet is the `x–y` plane and `z` its normal. **`x` is the flexure's own axis**
  (`C-0037`'s convention), so the **loaded** plane is `x–z`, head coordinates `(u_x, φ_y)`, and the
  **free** plane is `y–z`, coordinates `(u_y, φ_x)`. The two legs are offset along **`ŷ`** at
  `±w/2`, which is `C-0037`'s cross row and `C-0042`'s *"along one sheet helix"*.
- **The cap's own axis.** A duplex of radius `R = 1.00 nm` running along **`ŷ`**, its axis at
  `z = ℓ + R` where `ℓ` is the leg length; the flexure's end butts its side, so the flexure's axis
  sits at the same `z = ℓ + R`. **The rigid height `e = R` between the leg heads and the flexure's
  axis is part of the cap's geometry and is carried through every transformation.**
- **Sign conventions** for `(u, φ)` are `C-0030`'s [StandoffTipFlexibility] unchanged: `u` positive
  **inward** along the beam's axis, `φ = du/dz` positive in the sense a positive `u` produces.
- **A chord is a line**, so every junction misalignment folds into `[0, π/2]`, as `C-0042`.
- `EI = 230 pN·nm²` is CanDo's **model input**; every critical load is also reported on Fields et
  al.'s implied **172.906**.

---

## Plan

### The cheap bounds, which run first

| | bound | why it is cheap | what it would settle |
|---|---|---|---|
| **1** | **The seat exclusion.** A leg is seated on a duplex only if its axis lies within `R` of that duplex's axis (`C-0042`'s `seatContactLength = 2√(R² − y_c²)`, zero beyond the rim). Two legs must be at least `2R` apart (`C-0042`'s steric floor). **So no single duplex perpendicular to the leg row can seat both**, and in particular the flexure cannot. | two evaluations of a function that already exists | whether the cap is a **separate body**. If it is not, the task is a paragraph |
| **2** | **The counting theorem at the flexure's end.** A duplex end has exactly **two** termini; a cap that is the flexure's own end has two links to share between **two** legs — one each, `C-0037`'s `H1`, not its nominal `H2`. | a count | the same conclusion by an independent route |
| **3** | **The cap's own bending.** The frame-couple path is statically determinate, so the compliances add: `1/k_frame = 1/(k_a,series Σd²) + w/(12 EI_cap)`, i.e. **`k_cap,bend = 12 EI/w`** for free overhangs and moment-free attachments (`16 EI/w` clamped). Compare against `k_a,series Σd² = k_a,series w²/2`. | closed form | whether a finite-element cap is needed at all. **If the ratio is below ~2 the series form is dead and only a solve will do; above ~5 the cap's bending is a correction and the expensive part is elsewhere** |
| **4** | **The cap's torsion.** In the **loaded** plane the head moment reaches the legs through the cap's **torsion** over `w/2` each side: `k_cap,tors = 4C/w`, `C = 460 pN·nm²` (CanDo) or `103 k_BT` (measured). Compare against the head's own assembled `1/C22`. | closed form | whether the loaded plane's compliance moves at all |
| **5** | **The counting theorem at the leg heads, on the ROTATION.** `C-0037` applies it to the **axial** path only. The same two links also carry the head's **rotation**, at most `2k_bond,θ + 2k_bond,s r_P²` = **78.24** on one axis and **13.53** on the other, and the two are simultaneous — `C-0042`'s conserved budget `loaded + free = 91.76` one level up. Compare against the head's assembled rotational stiffness. | one evaluation of `chordBaseAxes` | **whether the term `C-0037` did not carry is bigger than the one it did** |

**The declared falsifiers.**

1. **Bound 1 failing** — a duplex that seats both legs. Then the cap is the flexure itself, its
   rotational stiffness is that of a rigid cross-section, and `C-0037`'s assertion is right for a
   reason it did not give.
2. **Bound 3 landing below ~2** — the cap's bending comparable to the couple it carries. Then the
   series form is not a small correction and every number of `C-0037`'s must be re-derived, not
   corrected.
3. **Bound 5 landing above ~10×** — the head junctions' rotation negligible against the assembled
   head. Then the cap is a geometry question only and the mechanics stands.
4. **The reductions failing** — the solved cap not returning `C-0037` in its rigid limit. Then the
   model is a different model and nothing may be compared.

**Pre-registered prediction.** Bound 1 excludes the flexure, so the cap is a **crossbar duplex**
running along the leg row, ~13 bp, hosting **three** 90° junctions. Bound 3 clears by ~9×, so the
cap's *bending* is a few per cent. **The term that moves the answer is bound 5** — the head
junctions' rotational compliance, which `C-0037` takes as infinite — together with the cap's rigid
height `e = R`. The expected consequence is that the **free** plane's critical load falls, so
`C-0042`'s crossing separation moves **above** 7 bp.

### Method, and its cost justification

The expensive alternative is a full 3-D frame eigen-solve of legs + cap + flexure. It is not
needed, and the reason is structural rather than budgetary:

- the **free** plane is a planar frame whose frame-couple path is **statically determinate**, so its
  compliances add in closed form and the only coupled quantity is the buckling eigenvalue;
- the **loaded** plane has `Σx_i² = 0` by construction (`C-0037`'s finding 2, `C-0042`'s `Q4`), so
  the cap contributes **no** frame couple there and only a series torsion on the rotation
  coordinate;
- the buckling eigenvalue is taken on `C-0042`'s **mixed-base beam-column finite element**, extended
  by one degree of freedom per leg — its own head rotation, spring-coupled to the cap's — and by the
  cap's rigid height in the geometric stiffness. **Reusing it means the extension is verified by the
  reduction**: with the junction springs infinite and the height zero it must return
  `mixedBaseTrussBucklingLoad`, hence `C-0037`'s `trussBucklingLoad`, hence `C-0028`'s sway
  determinant — three claims deep, and asserted as tests.

`C-0030`'s `CoupledJointFlexure`, `coupledFlexureSpan`, `coupledBucklingStroke`,
`peakFlexureCompression` and `C-0037`'s `TriangulatedStandoff` are **re-run as libraries**, never
tabulated.

The one number fetched for this task is the duplex's **torsional** constant, which no upstream claim
needed. It is read directly and carried on both readings (CanDo's model input and the measured
persistence length), and the study reports what it is worth.

### What would falsify the *approach*

If the solved cap's assembled flexibility could not be written as `C-0037`'s assembly plus diagonal
series terms and one congruence for the height — i.e. if the cap coupled the two planes — the planar
decomposition would be wrong and a 3-D frame would be mandatory. The test is `Q7`: the assembled
off-diagonals must remain equal under two independent quadratures, which they cannot if a plane has
been dropped.
