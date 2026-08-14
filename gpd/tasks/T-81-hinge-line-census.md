# T-81 — Does a 16-crossover hinge line exist on a 40 nm tile?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Raised by** | [`C-0034`](../claims/C-0034-guided-arm-anchorage.md), open item 5: *"Whether 16 crossovers can be assembled into one hinge line on a 40 nm tile at all. This claim takes `C-0029`'s hinge count as given and only prices what is at the other end."* |
| **Verification type** | **logical** (a count on a lattice whose pitch is cited and measured — no simulation can move a count) **+ in-silico** (`C-0015`'s own `CrossoverLayout` re-run as a library over the complete 32 bp phase space, and `C-0034`'s placement pipeline re-run at the counts that exist) |
| **Units** | nm, pN, pN·nm, pN·nm/rad, pN/nm; `k_BT = 4.141947 pN·nm` at 300 K; aqueous 2 mM MgCl₂ |

---

## Why this task exists

Three standing claims price a design on a hinge of **16 antiparallel crossovers** and none of them counts:

- [`C-0023`](../claims/C-0023-two-sided-coupling.md) files `E5` with `hingeCount` a free integer;
- [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md) adopts **`E5g16`** — *"16 antiparallel crossovers, `k_θ` = 13.53 pN·nm/rad each"*;
- [`C-0034`](../claims/C-0034-guided-arm-anchorage.md) adopts **`E5a16`** and reports its own predicate `P4` as *"PASS at 16 and 32 crossovers; **FAILS at 8**"*.

So the surviving output-coupling branch — the only element in this programme that reaches §3's **desired** 10 nm stroke — rests on an integer that has never been checked against the sheet it has to be built on.
`C-0015` has the lattice, in code, complete over its own 32 bp phase space.
This task puts the two together.

---

## Geometry and sign conventions, restated

- **`x`** runs **along** the helices, **`y`** across them, **`z`** normal to the sheet.
  Duplex `j` has its axis at `y = j d` with `d = 2.69 nm` (SAXS, single layer).
- **Interface `b`** is the line between duplexes `b` and `b+1`, running along `x` at `y = (b + ½) d`.
  A tile of `D` duplexes has `D − 1` interfaces.
- **A crossover** is a point on an interface.
  Crossovers recur every **16 bp** along a *helix* but **alternate between its two neighbours**,
  so a given *interface* is linked every **`p = 32 bp = 10.88 nm`** —
  and interface `b` carries the columns of parity `b mod 2` of the `p/2 = 16 bp` column lattice.
  This is `C-0015`'s construction, used here unchanged.
- **A hinge line** is a maximal set of crossovers that share **one interface** and **one pair of bodies**:
  they are collinear along `x`, they turn through the same angle, and their `k_θ` therefore add in **parallel**, giving `n k_θ`.
  Its torsional axis is the interface line itself.
- **Positive rotation** lifts the outboard body in `+z`.
- The **phase** is quantised to base pairs and has period **32 bp, not 16** — a half-period shift leaves every column position unchanged and hands every interface **the other parity's** columns.
  The sweep here is therefore over all **32** phases and is complete, not sampled.

### The one modelling premise, stated where it can be attacked

`k_θ = 13.53 pN·nm/rad` (Chen et al., **cited and fitted**, via `C-0009`) is the **interhelical dihedral** spring:
it resists rotation of duplex `b+1` relative to duplex `b` **about their common interface line**, which runs along `x`.
That is how `C-0009`'s grillage uses it and how `C-0015` recovers `D_⊥` from it.
**So `n k_θ` is the right spring for a hinge whose axis runs along `x`, and for no other axis.**
Everything below is counted on that premise; where a reading violates it, the task says so rather than counting anyway.

---

## The acceptance predicates, declared before the run

| | predicate | falsifiable by |
|---|---|---|
| **`P1`** | the per-interface pitch is `p = 32 bp` and a hinge line of `n` crossovers needs exactly `(n − 1) p` of collinear interface | a lattice on which a hinge line's stations are not at pitch `p` |
| **`P2`** | **16 crossovers are reachable in one hinge line on a 40 × 40 nm tile at some phase** | the census; this is the predicate the task exists to test |
| **`P3`** | the census reproduces `C-0015`'s tile inventory exactly — **56 crossovers at 10 of 32 phases, 49 at the other 22** — and its centro-symmetry rule `(columns + duplexes)` odd | any departure |
| **`P4`** | the hinge count `C-0034`'s pipeline needs to place an arm reaching §3's desired 10 nm stroke is **at most** the count that exists | the placement solve |
| **`P5`** | the hinge demand and `C-0015`'s 45 attachments on a 3 × 15 grid are simultaneously satisfiable on one sheet | the inventory arithmetic |
| **`P6`** | the **lattice** (phase) effect on the count is small beside whatever shortfall there is — i.e. the verdict is a continuum fact, not a quantisation artefact | the continuum control |

---

## Plan — the cheap bounds first, and what would make an expensive calculation necessary

**Bound 1 — the line length a count demands.** `(16 − 1) × 10.88 nm = 163.2 nm` of collinear interface.
The tile is 40 nm along the helices. One division.

**Bound 2 — the transverse reading.** If instead the 16 are taken one per interface across a transverse line
(`TASKS.md`'s own guess — *"16 duplexes sharing a transverse fold line"*),
a transverse line serves only the interfaces of **one parity**, so it needs `2 × 16 + 1 = 33` duplexes = **88.8 nm**.
Two divisions, and the answer is bounded in **both** directions of the sheet before any code runs.

**Bound 3 — the inventory.** `C-0015` already reports the whole tile as **49–56** crossovers.
45 load paths × 16 = **720**. One division.

These three decide the verdict. What they do **not** decide is *what the design does at the count that exists*,
and that needs `C-0034`'s placement pipeline re-run at `n = 1 … 16` — cheap, because the pipeline is a library.

**The expensive calculation that is therefore NOT run.** If a hinge line could be made long enough,
the next question would be how many of its crossovers actually *participate* under a point load —
a shear-lag/effective-width solve on the raft, which is `C-0020`'s problem in a new place.
It is not run because it can only **lower** the count: the rigid-raft census is an **upper bound**,
and the upper bound already fails. `C-0009`'s own result — *"a rigid anchor is carried by its two nearest
crossovers and essentially nothing else"* — says which way the correction runs.

### What would falsify this approach

1. **A per-interface pitch other than 32 bp.** If crossovers could serve one interface every 16 bp, every count here doubles.
   Carried explicitly as a sensitivity, because it is the exact error `CLAUDE.md` warns about — and the answer does not change.
2. **The census reaching 16 at some phase**, which would make `P2` pass and this whole task a footnote.
3. **`C-0015`'s inventory failing to reproduce**, which would mean the census is not counting the same lattice.
4. **`C-0034`'s `E5a16` arm, tangents and its 8-crossover failure not reproducing** from the re-run pipeline,
   which would mean the design being re-priced is not the filed one.
5. **The continuum control showing the shortfall is a quantisation artefact** — i.e. a continuum hinge line
   of the same length delivering 16 where the lattice delivers 4.

---

## Method

1. **The census.** `CrossoverLayout.atBasePairPhase` re-run at all 32 phases on the 40 nm footprint;
   per-parity counts, column count, tile inventory, centro-symmetry — reported **per phase**, best and worst named.
2. **The topology ladder.** Five readings of *"one hinge line"* along the helices, each with the line length
   it implies and the count the lattice gives it: the flexure's own plan share at 45 paths, the arm's own
   length, one full-length tile interface, the tile dilated by the arm, and an unbounded superstructure —
   plus the **transverse** reading, counted for completeness and reported as the wrong axis.
3. **The fan.** The only way 16 crossovers can be *assembled* into one flexure is across several interfaces,
   where they compose in **series**, not in parallel. Derived, not asserted:
   with `m` interfaces of `n_i` crossovers each, the lines at `0, d, 2d, …` from the **root** line and the
   load at the outermost duplex axis, i.e. at `(m − ½)d`,
   `δ/F = d² Σ_{i=1}^{m}(i − ½)²/(n_i k_θ)`, so the equivalent single hinge read at that lever has
   **`n_eff = n_i · 3(2m − 1)/(m(2m + 1))`**, exactly `n_i` at `m = 1`.
4. **The continuum control**, per `CLAUDE.md`: the same fan as a cantilever strip of `C-0009`'s
   across-helix rigidity `D_⊥ = k_θ d/p` per unit width, and the excess quoted.
5. **The re-pricing.** `C-0034`'s `anchoredArmForStiffness` re-run at `n = 1 … 32` on the adopted `A2`
   anchorage, giving the arm, the realised `c`, the tangent at both §3 strokes, the per-crossover bond
   force, and the two thresholds: the count `C-0023`'s 40 pN/nm ceiling needs, and the count §3's desired
   stroke needs.
