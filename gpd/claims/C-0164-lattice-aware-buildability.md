# C-0164 — **THE FUNCTION THAT HANDS A VERDICT TO SOMEBODY ELSE'S DESIGN NO LONGER GUESSES WHICH LATTICE IT IS ON.** `checkBuildability()` is lattice-aware **by default**, and the honeycomb branch that `C-0160` had to leave *withheld* now **answers**: `C-0148`'s `±5 bp` closure derived from the **file** — 59 raster crossovers reduced by their own bond class to `{4, 14}`, one `b₀`, **zero forced** — and `C-0136`'s per-helix residues beside it. The two rules are one statement on two lattices, `N ≡ step·Δ + {0, ±2·offset} (mod period)`, which is `C-0086`'s odd multiples of 16 bp at `offset = 0` and `C-0136`'s `7Δ + {0, 10, 11}` at `offset = 5` — and the reason the square rule is **unconditional** is one line of arithmetic the code now runs: a raster's axial sign alternates, so the rule survives it exactly when `−Δ = Δ`, and `2` **is** self-inverse modulo 4. **The predicate refuses what it should refuse**: `C-0140`'s withdrawn `112 / 108` fails closure at exactly the **10** forced crossovers `C-0148` found, while passing the per-element rule — `CLAUDE.md`'s *a per-element rule that is NECESSARY is not SUFFICIENT once the elements share a boundary*, executable. **And grading the one design in this tree that nobody here drew found the lattice-blind rule passing it for the wrong reason**: `scadnano.origami_rectangle` was admitted on a 144 bp *row width* that is neither its 128 bp span nor any of its 48 / 80 / 128 bp scaffold runs, and its 128 bp run — the one that enters **and leaves by the same neighbour** — is admissible only under the general rule, `≡ 0 (mod 32)`, which the row reading cannot express

| | |
|---|---|
| **Task** | [`T-270`](../tasks/T-270-lattice-blind-buildability.md) — the width rule, per lattice, derived from an imported file |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** — every quantity is integer lattice arithmetic and every acceptance predicate is `==`; **no tolerance is used anywhere in this claim** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** `ADMISSIBLE` here means *"every rule this repository has for this design's lattice applies, and passes"*. **No object is folded and none is measured**, and the honeycomb block remains explicitly **not foldable** — it carries no staple set (`C-0160` §6) |
| **Verdict** | **PASS on `P1`–`P7`.** `F1` (the file-derived closure disagrees with `HoneycombRasterResidues`) did **not** fire — same `closes`, same `forcedCrossovers`, same residues up to the one global shift. `F2` (the check passes `112 / 108`) did **not** fire — it refuses it at 10. `F3` (a scadnano honeycomb grid position off this corpus's lattice) did **not** fire, at 144 positions. `F4` (a moved verdict on the square sheet) did **not** fire; **`F4` was amended before any run** — see §7 — and the foreign rectangle's *ground* moves while its verdict does not. `F5` (a design artifact changes) did **not** fire: `gpd/designs/*.sc` are byte-identical, asserted by `CommittedDesignsTest`. `F6` (the generalised square rule refuses the field's own generator's output) did **not** fire |
| **Provenance** | New source [`design/DesignLatticeRules.kt`](../../src/main/kotlin/design/DesignLatticeRules.kt); `checkBuildability()` **removed** from [`design/ScadnanoDesign.kt`](../../src/main/kotlin/design/ScadnanoDesign.kt) and `checkBuildabilityOnItsOwnLattice()` from [`design/HoneycombBlockDesign.kt`](../../src/main/kotlin/design/HoneycombBlockDesign.kt), both becoming the one lattice-aware function; one declared constant added to [`lattice/CrossoverLattice.kt`](../../src/main/kotlin/lattice/CrossoverLattice.kt) (`scaffoldCrossoverOffsetBasePairs`). **14 new gate-named tests** in [`design/DesignLatticeRulesTest.kt`](../../src/test/kotlin/design/DesignLatticeRulesTest.kt), written first and watched fail, and 3 rewritten in [`design/HoneycombBlockDesignTest.kt`](../../src/test/kotlin/design/HoneycombBlockDesignTest.kt) — **55 in the package**, against `C-0160`'s 41. Independent second implementation of the closure, retained: [`tools/T-270-closure-from-sc.py`](../../tools/T-270-closure-from-sc.py) (9 self-tests), which shares no code with the Kotlin and reads only the committed `.sc`. Mutation-tested (§8). The whole suite is **3 138 tests, 0 failures**, and `tools/verify.sh` runs clean on the working tree. **No committed result file moves, no study is re-run, and no committed design changes by one byte** |
| **Conditions** | Lattice statements only — no temperature, buffer or load enters this claim. Square lattice: 4 azimuths, 8 bp step, 32 bp period, **no** scaffold offset, drawn at `32/3` bp/turn. Honeycomb: 3 azimuths, 7 bp step, 21 bp period, `±5 bp` scaffold offset, drawn at B-DNA's 10.5. scadnano format 0.21.1 |
| **Consumes** | [`C-0160`](C-0160-scadnano-writer.md) (the writer, the emitted block, the fired falsifier `F2`, and the additive check this completes), [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) (the `±5 bp` closure predicate and its 10-forced-crossover reading of `112 / 108`), [`C-0136`](C-0136-mixed-domain-phase-and-honeycomb-twist.md) (the admissible row-length residues and the two disjoint turn senses), [`C-0151`](C-0151-closing-raster-selection.md) (the recommended `102 / 109` pair), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the seamless square width rule), [`C-0154`](C-0154-honeycomb-grillage.md) (a honeycomb block's interfaces are not a path graph) |
| **Constrains** | **Raises [`CH-0211`](../challenges/CH-0211-the-closure-does-not-need-the-turn-senses.md)** against `C-0160`'s validity range — one stated *reason*, not a number and not a verdict. `CH-0211` was reserved by `C-0163` and **released unused**; it is taken here |

---

## 1. The defect, and where it was

`ScadnanoDesign.lattice()` refuses a grid this project has no lattice for and says why:
*"guessing between them silently transfers a phase congruence, a station ladder and a register
departure that do not hold"*.
Twelve lines below it, `checkBuildability()` applied `C-0086`'s **square** width rule to whatever
design it was handed. `C-0160` declared that as `F2`, it fired on this programme's own recommended
honeycomb block, and it was filed rather than repaired quietly so the fired falsifier stayed
measurable.

It was invisible for a reason worth keeping: **until `C-0160` there was no honeycomb design to run
it on.** `CLAUDE.md`'s *a quantity that nothing draws is a quantity nothing checks*, met on a
predicate instead of a quantity.

## 2. One rule, two lattices

`CLAUDE.md` states the general law once — *an integral crossover lattice is necessary and not
sufficient: the crossover must also point at the neighbour the raster needs next* — so an
admissible run length is `N ≡ (exit residue) − (entry residue) (mod period)`. Written that way the
two lattices are **one predicate with three declared constants**, and
`CrossoverLattice.scaffoldCrossoverOffsetBasePairs` is the third:

| | azimuths | step | period | scaffold offset | a boustrophedon's run | source |
|---|---|---|---|---|---|---|
| square | 4 | 8 bp | 32 bp | 0 | `N ≡ 16 (mod 32)` | `C-0086`, Ke et al. |
| honeycomb | 3 | 7 bp | 21 bp | `±5 bp` | `N ≡ 7Δ + {0, 10, 11} (mod 21)` | `C-0136`, Douglas et al. |

`admissibleRunResidues(honeycomb, 1)` returns `{7, 17, 18}` and `admissibleRunResidues(honeycomb, 2)`
returns `{3, 4, 14}` — `C-0136`'s two **disjoint** turn senses, reproduced by a function that was
written for both lattices and told about neither.

And the asymmetry the whole task turns on is now executable rather than prose. A raster's axial sign
alternates helix to helix, so a width rule survives it exactly when `−Δ = Δ` in the lattice's own
modulus. `floorMod(−2, 4) = 2`: the square sheet's two in-plane neighbours are two classes apart and
**2 is self-inverse modulo 4**, so `C-0086`'s rule needs no turn sense. Modulo 3 neither 1 nor 2 is,
which is exactly why the honeycomb's admissible widths need one — and why the two senses being
disjoint is what makes a honeycomb block **two-length** in the first place.

## 3. What makes the honeycomb rule answerable about a FILE

`HoneycombRasterResidues` answers closure from a **construction** — two row lengths walked along a
turn-sense path. An imported design has neither, and it has two things that are enough:

**The level, from the domain boundaries.** A raster crossover sits on the **edge of the axial window
the helix turns at**: `end` for a forward domain, `start` for a reverse one. The two sides of one
crossover give the same number, and `importedRasterCrossovers` asserts that rather than assuming it.
The **offset** the file records is `level − 1` for a forward domain and `level` for a reverse one —
so reading offsets instead of edges perturbs *half* the residues by one and is not a datum at all.
That is a named test: on the emitted block the two readings differ on some crossovers and agree on
others, which is the signature of a convention rather than a shift.

**The neighbour class, from `grid_position`.** `honeycombCellOfGridPosition` inverts scadnano's own
published `grid_position → position` map onto this corpus's integer cross-section cell.
`C-0160`'s `F4` checked the forward composition at departure `0.0`; the inverse reproduces
`honeycombXRasterPath` at all 60 helices of the block, and every one of 144 swept `(h, v)` lands on
a **site** of the lattice — which `HoneycombCell` itself refuses otherwise.

A global datum shift moves every reduced residue alike, so the closure condition is **convention-free
in the file's own origin**. That is what lets a design nobody here drew be graded at all, and it is
visible in the numbers: the block's `b₀` is **9** read on the file's datum and **5** on the corpus's
own `z`, the emission's 4 bp shift moving both alike.

## 4. The recommended block, graded — and the pair the corpus withdrew, refused

| design | lattice | verdict | width rule | closure | forced |
|---|---|---|---|---|---|
| `gen1-block-honeycomb-10x6-102-109.sc` (`C-0151`) | honeycomb | **ADMISSIBLE** | `C-0136` per run, all admissible | **closes**, `b₀ = 9` (file datum) | **0** |
| the same builder at `112 / 108` (`C-0140`, withdrawn) | honeycomb | **VIOLATIONS** | per run, **all admissible** | **does not close** | **10** |
| `gen1-sheet-square-15x112.sc` (`C-0086`) | square | **ADMISSIBLE** | 112 bp, odd multiple of 16 | — | — |
| `third-party/scadnano-origami-rectangle-16x8.sc` (reference impl.) | square | **ADMISSIBLE** | runs 48 / 80 / 128, all admissible | — | — |

The block's 59 raster crossovers reduce to exactly **two** residues, `{4, 14}` — ten apart, which is
the closure condition — and one `b₀` admits them. Derived a third time, in Python, straight out of
the committed `.sc` and independently of the Kotlin, the same three numbers come back.

**The second row is the one that makes the predicate a predicate.** `C-0140` recommended `112 / 108`;
`C-0148` showed no lattice constant serves it and that **10 of its 59** crossovers would have to be
forced. Read out of a *drawn* `112 / 108` block, the file-derived check returns the same 10 — and
the **per-element** rule passes every run of the same design. A predicate that passes everything it
is shown is not a predicate, and this one refuses the design this corpus itself withdrew, for the
reason it withdrew it.

## 5. The foreign design, and what grading it found

The one design in this tree that nobody here drew is `scadnano.origami_rectangle`'s own canonical
Rothemund rectangle. The lattice-blind rule **passed** it — and passed it on a number that is not a
row of that design:

- `rowBasePairs()` reads **144**, the largest offset a scaffold domain reaches;
- the design's axial **span** is **128** (it starts at offset 16);
- and it is **not seamless** — its scaffold runs are **48, 80 and 128 bp**, two per helix on fifteen
  of sixteen helices.

144 happens to be an odd multiple of 16, so `C-0086`'s rule admitted it. `C-0086`'s rule is a
statement about a **seamless** raster's row, and this design has a seam, so the report now carries
`seamlessRowWidthIsAdmissible = null` beside `isSeamlessRaster = false` and applies the general rule
instead — Rothemund's own sentence, *the distance between successive scaffold crossovers must be an
odd number of half turns*, read per **run** rather than per row.

And the general rule is not merely wider, it is the one that has anything to say about the **128 bp**
run: that helix (helix 0) is where the two halves of the raster join, so the scaffold **enters from
helix 1 and leaves to helix 1** — the *same* neighbour, `Δ = 0` — which demands `N ≡ 0 (mod 32)`,
and `128 = 4 × 32`. The 48 and 80 bp runs enter and leave by **opposite** neighbours, `Δ = 2`,
demanding `N ≡ 16 (mod 32)`, and both are. The field's own generator satisfies the generalised rule
at every one of its 29 interior runs; the row reading could not have expressed the 128 bp one at all.

## 6. A design whose lattice cannot be derived — a decision, not a default

`lattice()` **refuses**, and must: it returns a `CrossoverLattice` and there is none.
`checkBuildability()` **reports**, and this is the decision recorded here and in the code:

- every rule that needs an azimuth, a period or a neighbour is **withheld with its reason**;
- the two rules that are statements about **strands** rather than about a lattice — a site
  registered twice, insertions or deletions — are still **answered**, because they are what the
  person who handed the file over can act on;
- and the verdict is **`INCONCLUSIVE`**, which exists precisely so that an empty violation list
  cannot be read as a pass. That is the same failure shape as answering a rule that does not hold,
  seen from the other side.

Both directions are named tests: an unknown grid with a clean design reports `INCONCLUSIVE`, and an
unknown grid with a doubly-registered crossover site reports **`VIOLATIONS`** — a violation is a
violation whatever else was withheld.

One rule is withheld on a honeycomb design that carries **staple** crossings: a staple crossover must
sit at `b₀ + 7·class` exactly, and nothing in this corpus fixes the datum relating a staple
crossing's *offset* to the scaffold's *level*, because this corpus has never determined a honeycomb
staple routing. It is **not** withheld where a design carries no staple crossings — a rule with an
empty domain has nothing to say, and calling that *withheld* would make the recommended block
inconclusive for a reason that is not about it.

## 7. `C-0160`'s pinning test, retired explicitly — and an amended falsifier

`C-0160` pinned the lattice-blind behaviour with a named test, so that `F2` stayed measurable until
it was repaired. It is **retired, not deleted**: the test method survives under the name *"the
SQUARE-lattice width rule is no longer applied to this honeycomb design"*, carrying the retired
assertions verbatim in a comment and asserting their **negation** — no sentence of a honeycomb
report may name the square sheet's 16 bp ladder.

`C-0160` also asserted its lattice-aware check against the lattice-blind one *at run time*. The
lattice-blind one is gone, so those four fields are pinned here as **literals**, which is the
stronger pin: it survives the function it was taken from.

**`F4` was amended before any run, and the amendment is recorded rather than inferred.** As written
it said *"a moved verdict on the square sheet **or on the foreign rectangle** falsifies the repair"*.
Reading `C-0086`'s rule as a per-run rule is a **premise** argument — the rule is quantified over
seamless rasters — and it was adopted on that argument before the rectangle was graded, so `F4` was
narrowed to the square **sheet** and the rectangle's reading became a reported finding.
As it happens the rectangle's *verdict* does not move either; what moves is the ground it stands on.

## 8. Mutation table

Each row is one mutation of the new source, run against the package's 55 tests; the restored source
passes all 55.

| mutation | named tests that fail |
|---|---|
| `honeycombCellOfGridPosition` drops its `h`-parity branch | **10** |
| the crossover level is the file's **offset** instead of the window edge | **4** |
| `admissibleRunResidues` drops the `±2·offset` members | **5** |
| the square width rule is applied on every lattice (the defect, restored) | **5** |
| the verdict ignores `notApplicable` and returns `ADMISSIBLE` | **1** |
| a square run's two neighbours are read as always the same | **7** |

The fourth row is the defect itself, put back, and it fails five named tests — including *"an
underivable lattice is INCONCLUSIVE"*, because a rule applied on every lattice is applied on a
design that has **none**. That is the blindness in its purest form and it is what the third state
of the verdict was added to catch.

## 9. Validity range

- **Every number here is a lattice statement.** No temperature, buffer, load or stiffness enters,
  and nothing in this claim is evidence about the device.
- **A foreign honeycomb file is graded on scadnano's own axis conventions**, and the composition of
  its grid map with this corpus's cell handedness is calibrated on the one design where both are
  known. A file drawn with a reversed axial datum would need the reversal to travel with the residue
  map, which is `CLAUDE.md`'s *a residue map is a handedness*; nothing here detects that.
- **The honeycomb STAPLE-crossover residue rule is not implemented**, and is withheld with its reason
  wherever it would apply. It is the one rule a *foldable* honeycomb design would need.
- **`ADMISSIBLE` is a statement about the rules this repository has**, not about foldability. The
  recommended block carries no staple set and no sequence, and is not foldable.
- **The square branch reads a sheet's neighbours from the helix ordering**, which is exact while the
  adjacency predicate holds and is withheld with it. A multi-layer square design, whose crossovers
  reach out of the plane, is not covered.
- **`C-0160`'s round trip, artifacts and counts are consumed unmodified** and none of them moves.

## 10. Still open

- **A honeycomb staple router**, and with it the staple-residue rule above. It is still the largest
  single thing between this corpus and a bench order.
- **The adjacent-offset crossing motif** ([`CH-0209`](../challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md)):
  `noSiteIsCrossedTwice` still keys on the exact offset and cannot see a pair at `o` and `o+1`. This
  claim does not touch it.
- **A width rule for a design that is neither a raster nor a sheet.** Everything here is quantified
  over designs whose scaffold runs between crossovers; a multi-scaffold or origami-free design gets
  `INCONCLUSIVE` and deserves better.
