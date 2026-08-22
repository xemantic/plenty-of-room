# T-270 — `design.checkBuildability()` is LATTICE-BLIND, and it says so twelve lines below the function that refuses to guess

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0160`](../claims/C-0160-scadnano-writer.md)'s declared falsifier **`F2`**, which **fired** |
| **Claim** | `C-0164` |
| **Challenge reserved** | `CH-0211` — reserved by `C-0163` and **released unused**, so it is free |
| **Verification type** | **logical** — every quantity here is integer lattice arithmetic, and no tolerance is admissible |

## 1. Formulate

### The defect, exactly

`ScadnanoDesign.lattice()` **refuses** a grid this project has no lattice for,
and its message says why:
*"guessing between them silently transfers a phase congruence, a station ladder and a register departure that do not hold"*.

Twelve lines below it, `checkBuildability()` applies `seamlessRowWidthIsAdmissible` —
`C-0086`'s rule, an **odd** number of half turns across the row, i.e. the odd multiples of **16 bp** on the **square** sheet —
to whatever design it is handed.
Run on this programme's own recommended honeycomb block it reports one violation,
and the sentence it prints names a 16 bp ladder that a 21 bp period has nothing to do with.

So the function that hands a verdict to **somebody else's** design —
the capability [`ARCHITECTURE.md`](../../ARCHITECTURE.md) says nothing in the field has —
is the one that guesses.

It is `CLAUDE.md`'s own *a lattice rule transfers between lattices only if its class difference is self-inverse in the new modulus*, met in code:
modulo 4 the square sheet's two in-plane neighbours are 180° apart and the width rule is unconditional;
modulo 3 neither 1 nor 2 is self-inverse,
which is exactly why the honeycomb's admissible widths need a **turn sense** and the square sheet's do not.

### What is already done, and must not be redone

`C-0160` left the behaviour **byte-identical**, pinned the failure with a named test,
and added an **additive** `checkBuildabilityOnItsOwnLattice()` that
reproduces the original field-for-field on a square design and **withholds** the width rule with its reason otherwise.
This task finishes that migration.
It is not a new check; it is (a) making the lattice-aware form the **default**,
(b) supplying the honeycomb rule the withheld branch is missing,
and (c) deciding, in the code, what a design on an **underivable** lattice gets.

### What the honeycomb rule is, and on whose authority

Two rules, and they are not the same rule read twice.

**Per element, and NECESSARY.** `C-0136`: a helix's admissible lengths are
`N ≡ 7Δ + {0, 10, 11} (mod 21)` with `Δ = (b−a) mod 3` the **effective** neighbour-class difference
(`(axial sign) × (geometric sense)`, `HoneycombRasterTurnSense`).
Both turn senses are disjoint (`{7,17,18}` against `{3,4,14}`),
so a raster carrying both has no admissible **uniform** width at all,
which is why a honeycomb block is two-length in the first place.

**Globally, and SUFFICIENT for drawability on caDNAno's default rule.** `C-0148`:
a scaffold crossover sits `±5 bp` from its pair's staple position,
and **one** lattice constant `b₀` serves the whole design —
so reducing every raster crossover by its own bond class, `(level − 7·class) mod 21`,
must leave at most **two** values, exactly **10** apart.
`C-0151` selected `102 / 109` on exactly that test, and `C-0140`'s `112 / 108` fails it at 10 forced crossovers.

`CLAUDE.md` states the relation in as many words:
**a per-element rule that is NECESSARY is not SUFFICIENT once the elements share a boundary.**
Closure implies per-helix admissibility (each helix's two ends are then each `b₀ ± 5`) and the converse is false,
so both are reported and the pair says whether a failure is **local** or **global**.

### The one thing that makes this derivable from a FILE rather than from a construction

`HoneycombRasterResidues` answers closure from a *construction* — two row lengths and a turn-sense walk.
An imported design carries neither. It carries two things that are enough:

- **the level**, from the domain boundaries. A raster crossover sits on the **edge of the axial window the helix turns at**:
  `level = end` for a forward domain and `level = start` for a reverse one.
  The two sides of one crossover give the same number, which is asserted rather than assumed.
  (The **offset** the file records is `level − 1` for a forward domain and `level` for a reverse one,
  so reading offsets instead of edges perturbs half the residues by one and is not a datum at all.)
- **the neighbour class**, from `grid_position`. scadnano's honeycomb grid map
  `(h, v) → (h·d√3/2, …)` lands on this corpus's own integer cross-section cell `(x, y)`,
  which `C-0160`'s `F4` already checked at departure `0.0` in physical coordinates;
  inverted it gives the cell, hence the bond azimuth, hence `honeycombBondClass`.

A global datum shift moves every reduced residue alike, so closure is **convention-free** in the file's own origin —
which is what lets a design nobody here drew be graded at all.

### Units, geometry, conventions — locked

Base pairs are integers; **no tolerance is admissible anywhere in this task**, and every acceptance predicate below is `==`.
Rise `0.34 nm/bp`. Honeycomb `d = 2.536 nm` (SAXS), period `21 bp`, step `7 bp`, scaffold offset `±5 bp`,
azimuth increases counter-clockwise with `z` and neighbour class therefore **increases as the azimuth decreases**
(`HoneycombRasterTurnSense`'s convention, unchanged).
Axial positions are integer base pairs on the **file's** own offset axis;
the corpus's `z` differs from it by a translation and by nothing else.

## 2. Acceptance predicates

| | predicate |
|---|---|
| **P1** | The width rule is applied **per lattice**, with a named test in **both** directions: a square design still gets `C-0086`'s odd-multiple-of-16-bp rule at the same verdict as before, and a honeycomb design gets `C-0136`/`C-0148` and **not** the square rule — no report of a honeycomb design mentions a 16 bp ladder |
| **P2** | The honeycomb closure is derived **from the imported file** (levels from domain edges, classes from `grid_position`) and reproduces `HoneycombRasterResidues` on the recommended block at **integer equality**: the same `closes`, the same `offRuleCrossovers`, and the same distinct reduced residues up to the one global shift the emission applied |
| **P3** | The check **refuses the pair the corpus withdrew**: a `10 × 6` block at `C-0140`'s `112 / 108` reports a closure violation at **10** forced crossovers, from the file, where `102 / 109` reports none. A predicate that passes everything it is shown is not a predicate |
| **P4** | A design whose lattice cannot be derived gets a **stated verdict** recorded in the code and in the claim — not a default, and not an empty violation list a caller can read as a pass |
| **P5** | `C-0160`'s pinning test is **kept with its reason or retired explicitly**, never deleted quietly; and `C-0160`'s field-for-field reproduction on the square sheet still holds |
| **P6** | The recommended honeycomb block in `gpd/designs/` is graded and what it reports is recorded, **flattering or not**; and the one genuinely foreign design in the tree (`gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc`) is graded too |
| **P7** | No committed result file moves and no study is re-run. `gpd/designs/*.sc` stay **byte-identical** — this task changes what is *checked*, not what is *drawn* |

## 3. Falsifiers — what result would say this approach is wrong

| | falsifier |
|---|---|
| **F1** | The file-derived closure **disagrees** with `HoneycombRasterResidues` on the emitted block. Then the level convention or the class map is wrong, and the honeycomb branch must go back to withholding rather than answer wrongly — which is the failure this whole task is about, committed one level up |
| **F2** | The file-derived check **passes** `112 / 108`. Then it is not testing closure at all, and P3 is the test that says so |
| **F3** | scadnano's honeycomb grid position maps to a point that is **not** a site of this corpus's cross-section, at any `(h, v)`. Then the class cannot be derived from the file and the honeycomb branch is not implementable from an import at all |
| **F4** | Making the lattice-aware form the default moves any verdict on the committed **square sheet**. The square path must be unchanged; a repair that moves a square answer is a second defect, not a repair. **AMENDED before any run** — as first written this row also covered the foreign rectangle. `C-0086`'s rule is quantified over **seamless** rasters and the rectangle has a seam, so reading Rothemund's *"the distance between successive scaffold crossovers must be an odd number of half turns"* per **run** rather than per **row** was adopted on that premise argument, before the rectangle was graded; a moved reading there is therefore a **finding to be reported**, not a falsifier. The amendment is recorded here and in `C-0164` §7 rather than left to be inferred |
| **F5** | A `gpd/designs/*.sc` file changes by one byte |
| **F6** | The generalised square run rule reports a violation on `scadnano.origami_rectangle` — the field's own generator's own canonical Rothemund rectangle. Added with the `F4` amendment: a generalisation that refuses the reference implementation's output is wrong about Rothemund's rule, not about the rectangle |

## 4. Plan, and the cheap bound first

**The cheap bound is arithmetic and it runs before any file is parsed.**
Closure depends on the two row lengths only through their residues **modulo 21**,
so `HoneycombClosingFamily.closingResiduePairs` is already an exhaustive 441-case answer;
the file-derived predicate therefore has a known answer to reproduce on every candidate,
and the *only* thing it can get wrong is the datum and the class map.
Those are the two things P2 and F3 test directly, and each is a handful of integer comparisons.
Nothing here needs a solve, a sweep or a result file — the whole task is `O(crossovers)` integer arithmetic,
which is why it is filed as cheap and high priority.

**Method.**

1. Invert scadnano's published honeycomb `grid_position → position` map to this corpus's integer cell,
   and assert on the whole `10 × 6` path that it reproduces `honeycombXRasterPath` **exactly** (F3).
2. Derive the raster crossovers of an imported design: level from the window edge, class from the two cells.
   Assert the two sides of every crossover agree on the level.
3. Reduce, and answer `closes`, `classZeroResidueCandidates`, `offRuleCrossovers` — the same three quantities
   `HoneycombRasterResidues` answers from the construction, asserted equal on the emitted block (P2)
   and **unequal in the right direction** on `112 / 108` (P3).
4. Per-helix `C-0136` admissibility from the same derived data, reported beside closure so a failure says *local* or *global*.
5. Make `checkBuildability()` the lattice-aware one; give the report a **three-state verdict**
   so an empty violation list on an unanswerable design cannot be read as a pass;
   retire `C-0160`'s pinning test explicitly, and keep its square field-for-field reproduction as a test.
6. Grade the two committed designs and the foreign rectangle; record what they say.

**Rejected, and why.**

- *Deriving the turn senses from the file and re-running `HoneycombRasterResidues`.*
  It needs the raster path, which needs the cross-section, which needs the grid map anyway —
  and it would answer a **construction's** question with a construction, which is what an import exists to avoid.
- *Implementing the honeycomb **staple**-crossover residue rule.*
  A staple crossover must sit at `b₀ + 7·class` exactly, and the datum relating a staple crossing's *offset*
  to the scaffold's *level* is fixed by nothing in this corpus — which has never determined a honeycomb staple routing.
  Guessing it is the defect this task is repairing. It is **withheld with its reason** wherever a honeycomb design
  carries staple crossings, and it is **vacuous** (not withheld) where it carries none: a rule with an empty domain
  has nothing to say, and calling that *withheld* would make the recommended block inconclusive for a reason that is not about it.
- *A result file.* Nothing here is a computation over a parameter; it is a predicate over two committed artifacts.
  `CommittedDesignsTest` and the named tests are the record, exactly as `C-0160` argued for `gpd/designs/`.

## 5. Cost

One package, no solve, no sweep, no result file, no study re-run.
The expensive half of this task was already paid by `C-0160`'s writer:
the block exists as a file, so the honeycomb branch has something to be tested **on**,
and `CLAUDE.md`'s *a quantity that nothing draws is a quantity nothing checks* is why this repair was invisible until then.
