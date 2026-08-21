# C-0158 — `tools/verify.sh` has been RED since the commit that made the prose census a gate, and the residue is **two different defects in one number**: **62 of 69 tokens were artifacts a source repair never re-emitted, and 7 were source-side call sites at four SHAPES a mechanical sweep cannot match**. Re-emitted and repaired, the gate reads **0 tokens in 0 string fields in 0 files, of 147 scanned**, and the movement is **45 prose fields and 0 numeric**

| | |
|---|---|
| **Task** | [`P-27`](../tasks/P-27-prose-gate-red.md), raised by the coordinator, iteration 39 |
| **Leaf** | none — a **process** claim protecting the machine-readable artifact of every leaf |
| **Verification type** | **logical** (a gate re-run against the corpus of the commit that promoted it, by `git archive`; a channel census over the eight files' string leaves against every numeric read site in the tree; a by-kind movement classification against `git show HEAD:<path>`) **+ in-silico** (eight studies re-emitted through one `tools/study-batch.sh` snapshot, then four repaired emitters re-emitted through a second) |
| **Verdict** | **PASS on all six predicates.** `P1` **0 tokens in 0 files** (§4); `P2` **45 prose, 0 numeric, 0 boolean, 0 wording, 0 departure, 0 added, 0 removed**, with the one unexplained token and the twelve below the census both named (§4); `P3` **0 live channels** (§3); `P4` the three-file exemption carried (§6); `P5` §7; `P6` §8 |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every number this task moved is a rendering precision, and the one that moved the other way is a parsed-back input |
| **Provenance** | the gate and its 136 self-tests in [`tools/check-result-file-hygiene.py`](../../tools/check-result-file-hygiene.py); the re-emissions through [`tools/study-batch.sh`](../../tools/study-batch.sh) in `tools/reemission-order.py` order, both its `--selftest` (13 checks) and a `T-157`/`T-149` control run before the order was trusted; the movement classification by [`tools/T-250-movement.py`](../../tools/T-250-movement.py) (18 self-tests) against `HEAD`; the channel census scripted per §3; nine result files re-emitted |
| **Conditions** | The tree at `5443b52` plus this task's edits; the eight files' committed versions are unchanged between `a895871` — where this task's opening measurement was taken — and `5443b52`, and the movement of §4 is identical against both refs. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. **Nothing physical is computed** |
| **Consumes** | [`C-0156`](C-0156-prose-interpolation-sweep.md) (the gate, the sweep, the movement tool, the allowlist mechanism), [`C-0153`](C-0153-unrounded-prose-interpolations.md) (`roundedForProse`, the predicate), [`C-0117`](C-0117-reemission-order.md) (*a sweep is a topological sort*), [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (re-emit what a repair moved), [`C-0110`](C-0110-device-b-tall-gap.md) (run the consumers even when the change is provably invisible), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*), [`C-0131`](C-0131-departure-and-saturation-audits.md) (a control re-run can convict the baseline) |
| **Raises** | [`CH-0206`](../challenges/CH-0206-the-gate-was-red-at-the-commit-that-promoted-it.md) — against `C-0156`'s *"this task makes the corpus clean"* and the `verify.sh` comment *"so the line reads 0 in 0 and is a GATE"* |

---

## The claim, in one line

The gate was red at the commit that promoted it — measured on that commit's own tree —
because a claim that **wires** a gate is not obliged to **run** it,
and `./gradlew test` runs only the checker's self-tests.
The 69 tokens are two defects wearing one number, and only one of them is staleness.

---

## 1. The measurement that starts the task

```
$ git archive 49b1a01 | tar -x -C <scratch>
$ cd <scratch> && tools/check-result-file-hygiene.py --prose
  69 token(s) in 44 string field(s) in 8 file(s), of 146 scanned
EXIT=1
```

`tools/verify.sh` runs that line last under `set -euo pipefail`,
so the script has exited 1 since `49b1a01`.
At `a895871` the same reading is **69 tokens in 44 string fields in 8 files, of 147 scanned** —
one more file scanned (`T-9`, which passes) and not one token different,
which is the first evidence that nothing regressed in between.

| file | tokens at `HEAD` |
|---|---|
| `T-134-plan-tolerance.json` | 33 |
| `T-12-lateral-confinement.json` | 17 |
| `T-139-duplex-pair-separation.json` | 9 |
| `T-108-desired-stroke-reach.json` | 4 |
| `T-126-arm-slab-clearance.json` | 2 |
| `T-152-collinear-clearance.json` | 2 |
| `T-159-doubling-ladder-repair.json` | 1 |
| `T-50-beyond-mean-field-gap.json` | 1 |
| **total** | **69** |

## 2. The cheap bounds, both run before any study

`tools/reemission-order.py --selftest` passes 13 checks, and the `T-157`/`T-149` control returns the
known edge (*"`T-149` must be re-emitted BEFORE `T-157`"*) — `C-0153` found the same tool silently
reporting `0` on a path argument, so an unchecked zero is exactly what this task must not inherit.
Over the eight it reports **0 dependency constraints inside the set**, so any order is safe.

`tools/result-reader-census.py` bounds the blast radius: **`T-108`** has two readers
(`anchoring/RangeRobustPlacementStudy.kt`, `window/SecondResynthesisStudy.kt`, plus
`window/SecondResynthesisTest.kt`), **`T-139`** one (`tile/ForcedCrossoverPriceStudy.kt`),
**`T-50`** one (`electrostatics/PlanarCouplingWallStudy.kt`),
and **`T-12`, `T-126`, `T-134`, `T-152`, `T-159` have none**.

**And a third bound nobody asked for settles the whole ordering question.**
`T-159` reads `T-149`'s result file, and `T-149` is modified in this same change (`P-4`, §6) —
so `reemission-order.py` puts `T-149` first, which the working tree already satisfies.
Grepped rather than assumed: the only site in the tree that reads `armLength` back is
`src/test/kotlin/stability/DoublingLadderRepairTest.kt:272`, a **test**, and
`stability/DoublingLadderRepairStudy.kt` reads only `folds` and `runParameters.elementCeilingSafety`.
Measured: `T-159` re-emitted against the modified `T-149` reproduced its committed file
**byte for byte**.

## 3. `P3` — `CH-0205`'s channel, checked BEFORE re-emitting, and it is empty

`CH-0205` reports **7** keys the corpus reads back with a literal-key `.toDouble()` that are a string
leaf somewhere, and calls the channel **latent**. The question this task had to answer first is
whether it is latent *in these eight*, because if a consumer parses a string leaf of one of them then
the rendering decision is a decision about an **input** and a rounding sweep is not a safe repair.

Scripted over the eight, in two halves so neither idiom is missed
(the script is in the task's scratch and its two halves are stated here in full):

* the **key** half — every literal key the tree reads numerically, matching all three idioms this
  repository uses (`"k"` … `.toDouble()`, `.toInt()`, `.toLong()`, with or without `OrNull`,
  through `jsonPrimitive.content` or through `.toString().trim('"')`), intersected with each file's
  string-leaf key set;
* the **serializer** half — every `@Serializable` numeric field name in the tree, since kotlinx
  decodes by name and a lenient reader would take a quoted number.

| file | string-leaf keys | keys the sweep MOVED | intersect a numeric read site |
|---|---|---|---|
| `T-108` | 53 | `note` | **none** |
| `T-12` | 69 | `bindingConstraint` | **none** |
| `T-126` | 25 | `findings[*]` | **none** |
| `T-134` | 41 | `findings[*]`, `predicates[*].evidence` | **none** |
| `T-139` | 59 | `findings[*]`, `falsifiers[*].outcome` | **none** |
| `T-152` | 26 | `findings[*]`, `predicates[*].evidence` | **none** |
| `T-159` | 57 | `citedInputs[*]` | **none** |
| `T-50` | 66 | `literatureCriteria[*].reading` | **none** |

**0 live channels.** Every token the sweep moved lives in a sentence — `note`, `bindingConstraint`,
`evidence`, `outcome`, `reading`, or a bare index of `findings`/`citedInputs` — and none of those
keys is parsed numerically anywhere in the tree.

The two nearest misses were checked by hand and both resolve to **numeric** leaves at `HEAD`, so the
channel is latent here exactly as `CH-0205` says: `tile/ForcedCrossoverPriceStudy.kt` reads
`T-139`'s `calibration[4].value`, which is a `float`; `electrostatics/PlanarCouplingWallStudy.kt`
reads `T-50`'s `memberEffects[*].marginRatio` through `.toString().trim('"').toDouble()` — the
shape that would take a string just as happily — and all eight values are `float`.

## 4. `P1` and `P2` — what was re-emitted and what moved

Two `tools/study-batch.sh` snapshots, runs strictly sequential, copy-back scoped per run:
the first over all eight, the second over the four whose emitters §5 had to repair.

| | gate reading |
|---|---|
| at `HEAD` | **69** tokens, 44 fields, **8** files |
| after re-emitting all eight, source untouched | **7** tokens, 6 fields, **4** files |
| after repairing the four emitters and re-emitting them | **0** tokens, **0** fields, **0** files, of 147 scanned |

So **62 of the 69 were staleness** and **7 were not**, and the second batch is what separates them.
`T-126`, `T-159` and `T-50` reported *"no result file changed"* in the first batch —
they reproduce their committed files **byte for byte** —
and `T-134` went 33 → 3.

**By kind, over all nine moved files, against `git show HEAD:<path>`** (`tools/T-250-movement.py --ref HEAD`):

| kind | count |
|---|---|
| **prose** (a string leaf whose digits moved and whose skeleton did not) | **45** |
| wording (a verdict change) | **0** |
| departure | **0** |
| **numeric** | **0** |
| boolean | **0** |
| added | **0** |
| removed | **0** |
| moved tokens | 70 |
| tokens explained by the rounding its call site declares | 69 |
| **tokens unexplained** | **1** |
| tokens the census's own predicate cannot see | 12 |

**Zero numeric movement, which is `P2`'s target, and the two non-zero residual columns are named rather than swallowed:**

* **the one unexplained token is the `P-4` exemption, and it moved the other way.**
  `T-149`'s `/runParameters/armLength`, `8.16439083` → `8.164390826631301`.
  `explains()` tests whether the new literal is a **rounding** of the old; this is a **de**-rounding,
  deliberate, and it is `CH-0205`'s one live instance (§6). It is the only unexplained token in the
  change and it is expected.
* **the 12 below the census** — 8 in `T-134`, 3 in `T-139`, 1 in `T-149` — are tokens that moved and
  that the census's own predicate cannot see: `C-0153`'s short-`toString` class, whose rate
  `CH-0204` records as unmeasured. They are reported by the classifier for exactly this reason and
  they carry no verdict.

## 5. The half the task did not predict: **7 tokens in 4 files are SOURCE-side, at four shapes**

`P-27` was formulated on *"the source is clean and the artifacts are stale"*.
Measured, the eight partition three ways and only half of them fit that sentence:
**four are purely stale** (`T-108`, `T-12`, `T-139`, `T-152`),
**three are purely source-side** (`T-126`, `T-159`, `T-50` — each reproducing its committed file
byte for byte), and **one is both** (`T-134`, 30 stale tokens and 3 source-side).
`C-0156` §1 states its mechanical rule as `x.toString()` → `x.roundedForProse().toString()`,
*"keyed on exactly the census's own defect keys"*, reaching **86 of the 98 bare-number sites**,
with the **80 sentences** *"read"*.
Every survivor is a sentence, and every one is an expression that a reader scanning for a bare
`${identifier}` skips:

| shape | file | tokens | the site, verbatim |
|---|---|---|---|
| a **lambda** inside a template | `T-134` | 3 | `${counts.first { it.paths == 30 }.ceiling}`, `${counts.first { it.paths == 30 }.margin}` ×2 |
| a **call** inside a template | `T-126` | 2 | `${displacements.max()}`, `${worstTipClearance(rowRoots, arm, edgeX, width)}` |
| a **`+` concatenation**, not a template at all | `T-50` | 1 | `"… mu_GC = " + saturatedGouyChapman + " nm, 7 nm gap"` |
| a hardcoded **decimal literal**, no expression | `T-159` | 1 | `"C-0084's ladder refusal 7.91968584 nm and path ceiling 7.909685836937754 nm — CITED."` |

The four repairs are the sweep's own rule applied at those sites, plus, for `T-159`, the only repair
available to a literal: **round the literal**. Its ground is measured rather than chosen —
`7.90968584` is what its two siblings in the same list carry (`7.91968584`, `8.16439083`), what the
**same result file** already renders at `/findings/theCorrectedDomain`
(*"The path ceiling moves 7.90968584 -> 8.13040721 nm"*), and what
`gpd/data/T-159-downstream-diff.json` carries as a numeric `published` field in **30** records.

**And the last shape is the one that matters for the next sweep**: it has *no rounding call site to
repair*, so a mechanical pass over call sites is structurally blind to it, and no amount of widening
`roundedForProse`'s reach would have found it.

## 6. `P4` — the `T-149` exemption, carried

`T-250`'s sweep rounded `T-149`'s `runParameters.armLength` to nine digits.
`src/test/kotlin/stability/DoublingLadderRepairTest.kt` reads it back with `.toDouble()` and asserts
the re-derived contour against it at **`1e-14`**, so the rendering decision had become a decision
about how many digits an **input** carries — `CH-0205`'s latent channel, made live.
Three files carry the repair and all three are in this change:
the emitter writes full precision again with the reason at the call site,
`PROSE_ALLOWLIST` carries the one token with **two named mutation rows**
(the exemption is admitted in its own file; a *different* full-precision parameter of the same file
is still a defect), and `gpd/results/T-149-recommended-element-fold.json` carries the re-emission.
`tools/check-result-file-hygiene.py --self-test`: **136 of 136 pass**.

## 7. `P5` — why the sweep's own verification passed, and what closes it

Not *"the working tree was clean at verify time"*.
Three of the eight reproduce byte for byte, so **a working-tree run would have been red too**:
under either mode, at `49b1a01`, `tools/verify.sh` exits 1.
The reason is simpler and is three facts, none of which is a defect in the checker:

1. **`./gradlew test` does not run the gate.** `build.gradle.kts` wires `testResultFileHygiene`,
   which runs `--self-test` and nothing else. By the convention that file states six times, a
   checker's self-tests hang off `test` and the check that **reads the corpus** lives in
   `tools/verify.sh`. So the gate is reachable by exactly one command.
2. **`C-0156` records no run of that command.** Its Provenance names the result file, the emitter,
   the movement tool and its 18 self-tests, the checker and its 134 self-tests, and both mutation
   measurements — and no suite run. `T-250`'s own `gate` record carries `isAGate`,
   `allowlistEntries`, `allowlistIsTokenLevel` and `selfTests`, and **no reading**.
3. **The claim's own `F3` row records the residue**, at *"615 tokens in 37 files after the sweep"* —
   measured by `census()` in `tools/T-250-emit-result.py`, which **is** `check_prose_precision`,
   i.e. the gate. `census(RESULTS)` reads the live working tree at emit time, a state the result
   file does not name; `C-0156` §6 requires exactly that state to be named, and fixed it for the
   *before* census (`--baseline <ref>`) and not for the *after* one.

**The closure is the control, and it is the one `P-10` already built.**
`tools/verify.sh --committed` snapshots `HEAD` and is the only run that reads what the commit
carries; this claim records it in §8. The comment beside the gate line in `tools/verify.sh` is
repaired to state the **rule** and let the line state the **reading** —
`CLAUDE.md`'s own *"a numerical guard's own justification is a statement about a STATE, and it
expires when the state moves"*, met on a shell comment — and its label no longer says *"committed"*
where the default mode reads the working tree.

**Not closed, and named with its cost** (`C-0083`'s discipline): wiring the gate itself into
`./gradlew test` would make it unmissable and would overturn a convention argued six times in
`build.gradle.kts`. That is a design decision this task does not take unilaterally with sibling
agents in the tree, and it is `CH-0206`'s open half.

## 8. `P6` — the suite

See the acceptance verdict row. `tools/verify.sh --committed` is the authoritative reading and is
run on the commit this claim is filed with; the working-tree run additionally carries two sibling
agents' in-flight `design/`, `quantities/` and `environment/` packages, which is why the
`--committed` run is the one quoted.

## 9. Validity range

* **Nothing physical moved, and that is measured rather than argued**: 0 numeric, 0 boolean and
  0 wording fields over nine files. The strongest single piece of that evidence is the first batch:
  `T-126`, `T-159` and `T-50` were re-run against **unrepaired** sources and reproduced their
  committed files **byte for byte**, so every solve those three studies contain is unmoved by
  everything upstream of them that this iteration touched.
* **The gate's predicate is unchanged.** No line of `unrounded_numbers_in`, `PROSE_NUMBER` or
  `prose_exit_code` moved; the only checker change is the one `PROSE_ALLOWLIST` entry of `P-4` and
  its two named tests.
* **`0 in 0` is a statement about a corpus at a moment**, which is this claim's own subject. It is
  true of the tree at the commit named in §8 and of nothing else; the next study to interpolate a
  number into a sentence without `roundedForProse` makes it false again, and that is what the gate
  is for.
* **The four shapes of §5 are a lower bound, not a census.** They are the shapes that survived one
  sweep in eight files. A source-side count needs Kotlin's own types, which is `CH-0204`'s standing
  residue and is not closed here.
* `P3`'s emptiness is a statement about `HEAD`'s consumers. It is re-checked whenever a consumer
  changes which key it parses, which nothing in this repository enforces (`CH-0205`).
