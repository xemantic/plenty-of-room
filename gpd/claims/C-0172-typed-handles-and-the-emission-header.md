# C-0172 — **`P2` is discharged over the whole corpus and it moved nothing: 166 result-file reads in 75 sources became typed handles, 0 literal reads remain, and the derived reader graph is edge-for-edge IDENTICAL** — the declaration and the derivation agree. `P3` and `P4` are landed as a **required, additive emission header** on 127 of 128 emitting studies and emitted on **72** of them so far, with a signature declared before the sweep and met at every file: **`added = 2` and every other kind `0`**. Five findings produced rather than assumed: **`tools/reemission-order.py` sorts from a census 16 edges stale, and the order diverges from position 50**; **seven studies write a committed result file through no rounding function at all**, carrying **41 297 of the corpus's 41 369** over-precise numeric leaves (`CH-0223`); and **`environment/Regime` cannot name a swept buffer**, which 14 of the 21 electrolyte studies are (`CH-0224`), so `P4`'s block is `null` on exactly the results the gate exists to refuse. **A shared Kotlin SOURCE is a dependency edge no result-file reader graph can see**: `T-267`'s committed `degreesOfFreedom` is `4080` where `CH-0214`'s repair makes it **4320**, because it constructs the same `HoneycombGrillage` that `C-0167` repaired without reading its result file. And the header's own key name had to be a **census**: `lattice` is already a top-level key in one committed file and 101 numeric result leaves, `regime` a leaf in five, so the block is namespaced under `emission` — which occurs **0** times in 152 files

| | |
|---|---|
| **Task** | [`T-272`](../tasks/T-272-emission-layer-remainder.md) — `P2`, `P3`, `P4` of step 6, raised by [`C-0162`](C-0162-round-outputs-never-inputs.md) (`T-268`) |
| **Leaf** | none — the remainder of step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Verification type** | **logical** (a census over the committed corpus; a graph identity before and after a refactor; four tools with named self-tests) **+ in-silico** (72 studies re-emitted through one snapshot in one topological order, movement classified by kind against `git show HEAD:`) |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** No model, solver, mesh, tolerance or convergence parameter was touched, and every field this task moved is a **schema** field |
| **Verdict** | **PASS on `P2` over the whole corpus. PARTIAL on `P3`/`P4`: declared on 127 of 128 emitting studies, emitted on 72.** `A1` met on the order (asserted **164** constraints, non-zero, from a freshly derived census rather than the stale committed one); `A2` met (`added` only, every file); `A3` answered with a measurement (§7); `A4` vacuous — **no number moved anywhere, so no claim is amended** |
| **Conditions** | Units unchanged and locked; 300 K; `k_BT = 4.142 pN·nm`. Nothing physical is computed here |
| **Consumes** | [`C-0162`](C-0162-round-outputs-never-inputs.md) (the rule, the `parameter` movement kind, the measured cost), [`C-0159`](C-0159-environment-interface.md) (`Regime`), `T-266`/[`C-0160`](C-0160-scadnano-writer.md) and the layer-2 work (`CrossoverLattice`), [`C-0082`](C-0082-result-reader-census.md)/`P-22` (the derived reader graph), [`C-0117`](C-0117-reemission-order.md)/`CH-0131` (a sweep is a topological sort), [`C-0153`](C-0153-unrounded-prose-interpolations.md)/[`C-0156`](C-0156-prose-interpolation-sweep.md) (the by-kind classifier), [`C-0163`](C-0163-cold-start-entry-points.md)/`P-28` (the entry points, and the `./--check/` shadow directory), [`C-0127`](C-0127-format-string-repair.md) (mutation-test a predicate), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*) |
| **Raises** | [`CH-0223`](../challenges/CH-0223-seven-emitters-call-no-rounding-function.md) and [`CH-0224`](../challenges/CH-0224-a-regime-cannot-name-a-swept-buffer.md) |

---

## 1. What was built, and why each piece is where it is

| | |
|---|---|
| `lattice/LatticeTag.kt` | `SQUARE` / `HONEYCOMB` / `BOTH` / `NONE`. Four states and not a boolean, because the query the tag exists for — *"which results are single-layer square-lattice"* — must **admit** a comparison file: `includes(SquareCrossoverLattice)` is true of `BOTH` and false of `HONEYCOMB`. `NONE` is a **claim** in `Regime`'s own sense, and `ofTag` **refuses** an unknown word rather than defaulting to it, because a default there would make a misspelling read as *"no lattice enters this result"* — the one answer that silently exempts a file from the query |
| `structure/ResultEmission.kt` | `JsonElement.withEmissionHeader(lattice, regime)` — **one namespaced `emission` block** in front of the record carrying `lattice` and `regime`, refusing a non-object, refusing to overwrite, and refusing to be applied twice. The namespace is a census result and not a preference (§1b) |
| `structure/ResultRounding.kt` | `emission` added to `PARAMETER_RECORDS` **by census**: the spelling occurs nowhere in the 152 committed files at any depth, so the widening can move nothing that exists — a proof rather than a re-run. It makes the order of the two calls at an emission site irrelevant, which is a **test** rather than a convention |
| `structure/ResultInputs.kt` | **generated** — one `ResultInput` handle per committed result file, with `file()` and `file(directory)`. The generator is also a `--check`, so a new result file with no handle fails the build rather than being noticed |
| `tools/result-reader-census.py` | resolves `ResultInputs.T_3B` as a read of `T-3b-…json`, reading the registry's own declaration rather than recomputing the name; the registry file is **excluded** from resolution, or every study mentioning `ResultInputs` would inherit all 152 reads |
| `tools/T-272-header-census.py` | the four states — `BOTH`, `DECLARED-NOT-EMITTED`, `EMITTED-NOT-DECLARED`, `NEITHER` — reading the **source** for the declaration and the **artifact** for the emission, which is the whole point: a census that asked one of them twice could not see a disagreement between them |
| `tools/emission_header.py` | the same header for the emitters written in Python, mirrored rather than imported, with a self-test that reads `lattice/LatticeTag.kt`'s own vocabulary so the two copies cannot drift |

### 1b. The header's own key name had to be a census, and the first draft failed it

The header was first written as two **top-level** keys, `lattice` and `regime`. A census of the
committed corpus refuses that, and the refusal is sharp:

| | |
|---|---|
| files carrying a **top-level** `lattice` already | **1** — `gpd/results/T-152-collinear-clearance.json`, where it is a **list** of the lattice quantities that study tabulates |
| numeric **result** leaves named `lattice` anywhere | **101** — `T-10`'s `againstC0006[*].lattice`, `T-15`'s `continuumStations[*].lattice` |
| files with a leaf named `regime` | **5** — `GraftingRegime.MUSHROOM`, `SolutionRegime.CROSSOVER`, a shear-lag regime, a coupling regime |
| files carrying `emission` at any depth | **0** of 152 |

So `withEmissionHeader` would have **thrown** on `T-152` — the one study whose subject is closest
to the tag's own — and `regime` in `PARAMETER_RECORDS` would have been a sticky exemption on a
name the corpus uses for a *result*. That is `C-0162`'s own trap met from the other side: it
excluded the singular `parameter` from `PARAMETER_RECORDS` because *"it is a swept axis
coordinate, so widening the set would silently stop rounding 28 outputs"*; here the name a **new**
schema field wants is already a result, and the cure is the same — do not share a name with the
corpus.

**It cost a sweep.** Twenty-three files had already been emitted with the top-level form when the
census was run; they were reverted and the sweep restarted. The census is four lines of Python and
it should have run before the first `withEmissionHeader` was written, which is `CLAUDE.md`'s own
*the cheap bound runs before the expensive calculation* applied to a **key name**.

## 2. `A1` — the order, and the finding that the tool sorts from a stale graph

The constraint count was asserted non-zero before the order was trusted, as `A1` requires and as
`C-0153` made necessary. Asserting it is not sufficient, because **there are two graphs**:

| census | tags | dependency constraints | |
|---|---|---|---|
| `gpd/results/P-22-result-reader-census.json` as committed — **what `tools/reemission-order.py` reads** | 151 | **148** | |
| derived from the tree at this commit | 151 | **164** | **16 edges the committed census does not carry** |

Both are non-zero, both give an acyclic order, and **the two orders diverge from position 50 of
151.** `tools/reemission-order.py` opens `CENSUS` — the committed file — so every sweep ordered by
it since `P-22` last went stale has been ordered from a graph missing a tenth of its edges. This
task used the **derived** order and re-emits `P-22` in the same commit.

`CLAUDE.md` already carries the general form (*a reproduction residual is a staleness detector, and
reading it costs nothing*); what is new is that the detector is the **sorter's own input**.

## 3. `P2` — discharged over the whole corpus, and it is a graph identity

| | |
|---|---|
| result-file **reads** rewritten as `ResultInputs.T_X.file()` or `.file(directory)` | **166**, in **75** sources |
| distinct result files those reads name | **47** — `T-3b` alone is read 48 times |
| sources reading a result file by path, after | **0** — gated by `tools/T-272-header-census.py --reads`, wired into `tools/verify.sh` |
| handles in the registry | **152**, one per committed result file, generated |

**The falsifier, and it is the strong form.** `P2`'s risk is not that a path is mistyped — the
Kotlin would not compile — but that the *derived* graph and the *declared* handles disagree. So the
census was re-derived before and after:

| | read edges |
|---|---|
| before the conversion | 164 |
| after | 165 |
| lost | **0** |
| gained | **1**, and it is `design/SimulatedTileCensusStudy.kt` → `T-267`, a **concurrent agent's own in-flight study** and not a file this task touched |

Over the 75 files converted the graph is **identical, edge for edge**. That is stronger than
`P2`'s own predicate asks for — the predicate is that the census be a *superset* of the grep, which
it already was (`C-0082`: 41 direct and 20 transitive edges against a grep's 41) — because after
the conversion **the grep finds nothing at all** and the superset relation is vacuous. What
replaces it is the identity above.

**Two shapes were converted and the second is the one that mattered.** `CH-0092`'s twenty invisible
edges are assembled from a directory in the caller and a name in a helper; `ResultInput.file(directory)`
puts the name back in the declaration, and `window/ResynthesisInputs.kt`, `window/SecondResynthesis.kt`
and `synthesis/BufferRouteCensusInputs.kt` — the three files that class lives in — are converted.

**One collision, and it is a finding about the sorter.** `T-119` writes two committed result files
(`T-119-literature-queries.json` from `tools/`, `T-119-unused-junction-site.json` from a Kotlin
study), so a task id does **not** identify a result file. `ResultInputs.ofTag("T-119")` returns
`null` rather than picking one — and `tools/reemission-order.py`'s entire topological sort is keyed
on exactly that identifier, so it cannot tell a read of one from a read of the other.

## 4. `P3` — the lattice tag, and why it is a DECLARATION

The tag was measured before it was written. Two derivations were tried over the **128** studies
that write a committed result file (one of them a concurrent agent's, excluded from the sweep):

| derivation | SQUARE | HONEYCOMB | BOTH | NONE |
|---|---|---|---|---|
| **words** — `Gen1Tile\|CrossoverLayout\|OrigamiGrillage\|OrigamiSheet` against `[Hh]oneycomb`, anywhere in the source | 67 | 6 | **29** | 26 |
| **constructed objects** — the same names as identifiers, with comments and string literals removed | 83 | 1 | **13** | 31 |
| …then **propagated along the reader graph**: a study carries the lattices of every result file it reads | **90** | 1 | **14** | 23 |

The first is **23 % `BOTH`**, and those 29 are not a lattice fact — a honeycomb study imports a
square-lattice constant to compare against, and a square-lattice study names the honeycomb in a
`findings` sentence. A tag derived that way would be noise where a wrong lattice tag is exactly the
error the tag exists to prevent, which is why the tag is an **argument** to the emitter and
`tools/T-272-add-emission-header.py` refuses to guess it.

**The propagation is the part worth keeping.** A tag is closed under the reader graph — a synthesis
that consumes a square-lattice result carries square-lattice content — and it moves exactly **nine**
studies, every one of them defensible on inspection: the two window resyntheses and
`T-2`, the two `stability/` studies that read `T-149`, `T-156`'s buffer route census, `T-169`'s
withdrawn-ceiling note, `T-195`'s scaffold remainder (which reads `T-3b`, the square-lattice tile's
own edge load), and `T-230` (honeycomb turn loop, reading `T-218`) which goes `SQUARE → BOTH`.

`BOTH` is the **conservative** tag: `includes()` answers yes to both queries, so a comparison file
surfaces in an audit rather than hiding from one.

## 5. `P4` — the regime, and the finding is that the block is on the wrong object

The key is emitted on every record, as an explicit `null`, and it is `null` on **every one** of
them. That is not a shortfall of effort; it is `CH-0224`:

- `environment/Regime.bufferMillimolar` is a scalar `Double?` with exactly two states — **one**
  molarity, or **none at all**, the second being a claim about a neutral layer;
- of the **21** studies naming `MagnesiumChlorideBuffer`, **14** declare a list of **2–5**
  molarities and solve every state at each of them — `listOf(0.5, 2.0, 10.0)` seven times,
  `listOf(0.5, 1.0, 2.0, 5.0, 10.0)` once;
- so at **file** granularity the block is `null` on exactly the studies whose results the gate
  exists to refuse, and the studies it *can* describe are the geometry, lattice and placement
  corpus, where `null` is correct and there is nothing to gate.

**And a study at a single state cannot state one either.** `Regime`'s constructor carries
`require(highestHeightNm > lowestHeightNm)`, a **strict** inequality, so a range of one point is
refused — and `tile/HoneycombGrillageStudy` grades under *"`C-0022`'s solved edge collar at 2 mM,
10 nm, 0.192 V"*, which is a point. The type admits exactly one shape, **one buffer over a range of
heights**, and this corpus is made of the two it does not admit: a sweep over buffers, and a point
in `(buffer, gap, bias)` read out of one upstream solve.

`T-272`'s plan declared the falsifier as *"a `regime` block cannot be stated for some record — a
finding about the study and not about the schema"*. Measured, it is a finding about **neither**:
the studies name their range perfectly well, in a list, and the schema has no place to put a list.
Three repairs are priced in `CH-0224`; the cheapest is a per-record block, which is this sweep
again with a wider edit.

## 6. The sweep, and the signature declared before it ran

`F1` was declared in the task file before any study ran: **the header is additive and nothing else
in this change can reach a study's own numbers, so the sweep's signature is `added = 2` per changed
file — the two leaves of the `emission` block — and every other kind `0`.** Anything else is either a defect of this change or a pre-existing
irreproducibility, and must be controlled against `HEAD` before it is called either.

Measured over the 72 files this sweep re-emitted, with
[`tools/T-250-movement.py`](../../tools/T-250-movement.py) against `git show HEAD:<path>`:

| kind | count |
|---|---|
| `added` — `emission/lattice` and `emission/regime` | **146** = 2 × 72 |
| `numeric` — controlled below | **16** |
| `prose`, `wording`, `departure`, `parameter`, `boolean`, `removed` | **0** |

`numeric` is **16** over four files, and `A2` requires every one controlled against `HEAD`
before it is called staleness. The first is
`T-199-cross-section-comparison.json`'s `crossSections[3].freeTileDishingOverStroke` — the free-tile
dishing of a `3 × 20` cross-section, `2.6e−05` of the stroke, four orders of magnitude below
`T-5b`'s 0.10 and quoted by no claim (checked). `C-0129`'s control, run in a `git archive HEAD`
tree with `HEAD`'s code and `HEAD`'s inputs and none of this change:

| | |
|---|---|
| committed at `HEAD` | `2.62401211e−05` |
| control run A | `2.62401216e−05` |
| control run B | `2.62401216e−05` — **identical to run A**, byte for byte over the whole file |
| this sweep | `2.62401210e−05` |

**The committed file does not reproduce from its own code**, and the two control runs agree with
each other and not with it. So the movement is not this sweep's: it is `C-0162`'s `T-14` mechanism
— a value sitting within half a unit in the last place of the **nine-digit rounding tie**, where
the JIT's summation-order noise that rounding exists to absorb is amplified by the rounding
boundary into a visible last-digit flip. Three readings of one quantity, all in the ninth digit.

### The other three `numeric` files, and one of them is a finding

| file | fields | verdict |
|---|---|---|
| `T-129-range-robust-placement.json` | 8 `subsets[*].minimaxWorstOverStroke`, and 2 unrounded `parameters` leaves in their **sixteenth** digit | `C-0135`'s **descent manifold**, the same block `C-0162` controlled at `HEAD` and `C-0131` measured as two-valued. The two parameter leaves carry the descent's own output, so they inherit it |
| `T-136-two-per-row-placement.json` | 6 `distributions/11/*`, 1 parameter, 3 prose | the **same** manifold, and the same two readings: `peakRatio` swaps `2.45258143 ↔ 2.31362128`, which is `C-0162`'s recorded pair verbatim |
| `T-267-mechanics-on-imported-design.json` | 1 — `identities[2].degreesOfFreedom`, **4080 → 4320** | **not noise, and not this task's**: `CH-0214`'s repair reaching a consumer nobody re-emitted (below) |
| `T-138-path-count-consistency.json` | 1 `parameter`, `t136RecommendedMinimax` in its **sixteenth** digit | `C-0162`'s *one live propagation edge* doing exactly what it was built to. `T-138` reads `T-136`'s parameter block **as an input**; the topological order put `T-136` at position **87** and `T-138` at **103**, so the consumer ran after the producer and picked the manifold's draw up. `0.06822008970269246 → …42`, `6e−17` relative, and it reaches nothing |

**`T-267` is a dependency edge no reader graph can see.** `C-0167` (`T-263`, iteration 40) repaired
`tile/HoneycombGrillage.nodeS` to add a trailing node where a row length is not a multiple of 7 —
which at `C-0151`'s **116 bp** block is exactly one node per beam, `60 × 4 × 17 = 4080` becoming
`60 × 4 × 18 = 4320`. It re-emitted `T-253`, the study that *owns* the grillage. `T-267` was
committed an iteration earlier (`faa32d7`, before `0b77ef0`) and **constructs the same
`HoneycombGrillage`** — but it does not *read* `T-253`'s result file, so it is not an edge of
`tools/result-reader-census.py`'s graph and not a constraint of
`tools/reemission-order.py`'s sort. **A shared Kotlin source is a dependency and a shared result
file is a different one, and this repository derives only the second.** The census already builds
the declaration reference graph the first would need.

The staleness is **repaired by this sweep** — `T-267` is re-emitted here — and the study's own
identities survive it: `bitIdentical` is still `true` and `solvedFieldRelativeDeparture` still
`0.0`, so what moved is the count and not the mechanics.

### `P-22` is re-emitted, and reported apart from the sweep

The census file is the sorter's input (§2) and it was stale, so it is re-emitted here. Its movement
is large and it is **not** a sweep movement, so it is quoted separately: `wording = 1294`,
`added = 920`, `numeric = 3`. The wording and the additions are per-study read lists growing by
eight new studies and being re-attributed to the declaration that now names the handle; the three
numeric fields are the census's own counts, and they are the whole finding:

| | at `HEAD` | now |
|---|---|---|
| `studyCount` | 121 | **130** |
| `resultFileCount` | 141 | **152** |
| `directEdgeCount` | 121 | **138** |

**So no number this change made moved, `A4` is vacuous and no claim is amended.** That is the
expected result for a change that adds a schema field and touches no arithmetic — and it is also,
this time, the non-trivial part: **166 result-file reads were re-pointed through a new type in the
same sweep**, and a handle resolving to the wrong file would have shown up as `numeric` on the
consumer, in bulk rather than in a ninth digit.

### The precondition an additive schema field needs, checked rather than hoped

A key added at the top of every result file breaks every consumer that deserialises one
**strictly**: `kotlinx.serialization`'s default is `ignoreUnknownKeys = false`, which throws on an
unknown key rather than skipping it. Measured over `src/main/kotlin`: there is exactly **one**
`decodeFromString` in the whole tree (`design/ScadnanoDesign.kt`, on a `.sc` file, not a result
file), and **all nine** `Json` readers that consume a result file are declared
`Json { ignoreUnknownKeys = true }`. Every other reader navigates a `JsonElement` by key, which
cannot see an added sibling at all. The sweep is the second check and the stronger one: a consumer
that broke would fail its own study, and `72` studies emitted with **0** failures.

### The lattice tags were reviewed where the derivation is ambiguous

All **14** `BOTH` and the **1** `HONEYCOMB` were read individually. Every one of the 14 genuinely
constructs objects from both families — a honeycomb study that also builds an `OrigamiGrillage`
over `Gen1Tile` for the collar or for the comparison — and `tile/HoneycombBondClassStudy` builds
only `HoneycombCrossoverRule`, which is why it is the corpus's single pure-honeycomb emitter.

## 7. `A3` — the Python emitters, and the count is wrong in the same direction twice

`C-0162` §5(c) reports *"sixteen committed result files are written by a Python tool in `tools/`
and by no Kotlin study"*. Derived rather than listed, it is **24** —
`T-147`, `T-175`, `T-198`, `T-205`, `T-207`, `T-208`, `T-220` and `T-226` are the eight it misses.
**And the corpus had already derived it**: `gpd/results/P-22-result-reader-census.json` carries an
`unwrittenResultFiles` field, listing **21** at the commit `C-0162` was written on, in a field built
to answer exactly that question. A hand-written census stood beside a machine-readable one and
nobody diffed them — `CLAUDE.md`'s *"a reproduction residual is a staleness detector, and reading it
costs nothing"*, on a different field.

**Two of the 24 are written by nothing at all.** `T-147-third-answers-synthesis.json` and
`T-175-fourth-answers-synthesis.json` have no Kotlin study, no `tools/*.py` and no shell script;
a `grep` of the whole tree finds them named only in prose and in this task's own registry. For
those two, `gpd/README.md`'s *"a re-run that changes nothing produces no diff"* is not violated —
there is no run.

**Why the other 22 are out of scope, measured rather than asserted.** Each emitter was run at
`HEAD` and its output diffed:

| | |
|---|---|
| emitters that reproduce their committed file byte for byte | **1** — `T-194` |
| emitters that run and produce a **different** file | **10** — `T-183`, `T-184`, `T-200`, `T-201`, `T-202`, `T-205`, `T-207`, `T-211`, `T-234`, `T-250`, plus `P-22` itself |
| emitters that **fail outright** | **5** — `T-208`, `T-212`, `T-214`, `T-225` (usage), `T-249` (its own body assertion fires: *"BODY IS STALE"*) |
| not attempted | `T-9` (an oxDNA run) and `T-119` (a live EuropePMC survey) |

Adding a header to the ten and re-emitting would land **611 insertions and 715 deletions** of
corpus drift that has nothing to do with this task, in files whose staleness is a separate finding;
five would not run at all. So **two are reached** — `T-194`, patched through
`tools/emission_header.py` and re-emitted with `added = 2` and nothing else, and **`P-22` itself**,
which had to be re-emitted anyway because the sorter reads it (§2) — and the other 22 are reported
with the measurement above. That is the honest form of *an emission rule reaches only the emitters written
in its own language*: the language is not the binding constraint here, **reproducibility is**.

## 8. Two defects found by using the tools, and repaired

**A shadow corpus, again.** `P-28` found `./--check/` — 144 result JSONs built by a mis-parsed
argument. `tools/T-249-emit-result.py --help` builds `./--help/`, 151 files, reproducibly, because
its baseline directory is a bare positional that accepts anything; and
`tools/T-250-emit-result.py --help` **re-emits this repository's own audit file**, 241 pointers
removed and 34 numeric fields moved, because every flag was matched with `in argv` and anything
else fell through to a full run. Both now refuse an unrecognised argument. `--help` is the first
thing a cold session types.

## 9. What is NOT discharged, with its measured cost

Reported rather than shrunk, per `SESSION-PROMPT.md` and `C-0162`'s own §7.

| | state | cost |
|---|---|---|
| **P2** typed input handles | **discharged over the whole corpus**, with the graph identity of §3 and a gate | — |
| **P3** lattice tag | **declared on 127 of 128** emitting studies, **emitted on 72** | the residue is **55** studies, printed by name by `tools/T-272-header-census.py --verbose`. Two batches over one topological order at ~1 study/min each; the residue is dominated by the tail `C-0162` measured — `T-1d` alone is 33 min |
| **P4** regime block | **declared and emitted on the same set, `null` everywhere**, for the reason in §5 | a **per-record** block is this sweep again with a wider edit (`CH-0224`) |
| the 24 non-Kotlin emitters | **2 reached, 22 measured out of scope** (§7) | 10 would land unrelated corpus drift, 5 do not run, 2 have no emitter at all |

The one state that must never be silent is a study declaring a header its committed file does not
carry, and it is now a **count** rather than a hope:
`tools/T-272-header-census.py` prints all four states, and `--check` fails the build on the
*regression* one — a file carrying a header its study no longer declares, which is `C-0101`'s
`T-157` staleness with the arrow reversed.

### The suite

`tools/verify.sh` on this tree: **`BUILD SUCCESSFUL in 22m 27s`**, and a confirmatory
`./gradlew test` against the committed tree afterwards: **3 254 tests in 186 classes, 0 failures,
`BUILD SUCCESSFUL in 20m 19s`**. Every gate in `tools/verify.sh` comes back clean, including the
three this task adds.

## 10. Validity range

- **This claim is about emission, provenance and the dependency graph.** No physics, no model, no
  solver, no mesh, no tolerance and no convergence parameter was touched, and no verdict of any
  upstream claim is re-derived.
- **`P2`'s identity is over the 75 files this task converted.** The one gained edge is a concurrent
  agent's own study and is excluded by name, not by threshold.
- **The lattice tags are a derivation plus a review**, not a per-study judgement: the rule is
  `tools/T-272-add-emission-header.py`'s argument and the classification is reproducible from the
  sources, the 14 `BOTH` and the 1 `HONEYCOMB` were read individually, and `BOTH` is the
  conservative answer wherever the two disagree. A tag that is wrong is a defect of the same kind
  the tag exists to prevent, and the honest statement is that the corpus's tags are **as good as a
  static reading of each study's constructed objects plus the reader graph**.
- **`regime` is `null` on every record** and that is a measurement (§5), not a default.
- **One file this task rewrote is not this task's.** `tools/T-272-typed-input-handles.py` walks
  `src/main/kotlin` and converted a concurrent agent's **untracked**
  `design/SimulatedTileCensusStudy.kt` along with everything else — a behaviour-preserving
  substitution of `ResultInputs.T_267.file()` for the path literal, which compiles and which this
  commit does **not** include. It is reported rather than reverted: reverting it would leave
  `tools/T-272-header-census.py --reads` red on a file nobody committed, which is a gate punishing
  a sibling for existing.
- **`PARAMETER_RECORDS` gains a fourth spelling by census**, which is the extension rule its own
  KDoc states; `regime` occurs nowhere in the committed corpus at this commit, so the widening
  provably moves nothing that exists.

## 11. Falsifiers

| | |
|---|---|
| **F1** — the sweep's signature | **Declared before the sweep, and it fired.** Once on `T-250-prose-interpolation-sweep.json`, where the cause was this task's own probe of `tools/T-250-emit-result.py --help` re-emitting it (§8) — reverted. Then on four files' `numeric` fields, of which two are `C-0135`'s descent manifold, one is a nine-digit rounding tie controlled at `HEAD`, and one — `T-267` — is a real staleness this sweep repairs (§6). Everything else: `added = 2` per file, every other kind `0` |
| **F2** — handles against the census | **Held**: 0 edges lost, 0 gained over the converted files |
| **F3** — the order's constraint count | **Fired usefully**: non-zero on both censuses and **different between them**, which is §2 |
| **F4** — a declared-but-not-emitted study must be countable | **Held**: `EMITTED-NOT-DECLARED = 0`, and the residue is a printed list |
| **F5** — `parameterRecords = emptySet()` reproduces the pre-repair behaviour | inherited from `C-0162`, unchanged and still a named test |
| **F6** — the header cannot collide with a key the corpus already emits | **Fired, after 23 files had been emitted.** `T-152` carries a top-level `lattice` of its own; the header is namespaced under `emission`, those 23 were reverted and the sweep restarted (§1b) |

## 12. Still open

- **`P3`/`P4`'s residue is a re-run**, and nothing here makes it cheaper. What it makes cheaper is
  the *next* one: the declaration is landed, so the residue is a list of studies rather than a list
  of judgements.
- **`CH-0223`** — seven emitters with no rounding call, 41 297 over-precise leaves. Needs a
  determined-precision measurement per study before a digit count can be chosen.
- **`CH-0224`** — the regime block is on the wrong object for a corpus of sweeps.
- **`tools/reemission-order.py` reads a committed census** (§2). Re-emitting `P-22` in this commit
  makes it current; it will go stale again the next time a study gains a read, and the tool has no
  check that says so. The census's own `--check` **does** compare against the committed baseline
  and print a note; nothing fails on it.
