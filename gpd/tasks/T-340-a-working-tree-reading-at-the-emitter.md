# `T-340` — a synthesis emitter can write a reading nothing can pin, and **the corpus is thirteen files deep in them, of which only two are the defect**

**Leaf** `A8.2`.
**Raised by** [`C-0224`](../claims/C-0224-a-quoted-count-against-a-pinned-record.md) (`T-336`) as the upstream half of `T-339`.
**Challenges in scope** [`CH-0292`](../challenges/CH-0292-the-deliverable-quotes-the-unpinnable-half.md) (the deliverable quoted the unpinnable half — the *prose* is repaired, the *record* is not) and [`CH-0293`](../challenges/CH-0293-an-unpinned-block-inside-the-file-that-cites-the-rule.md) (`T-334`'s own block was wrong at the moment it was committed).
Ancestors on the same rule: [`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md), [`CH-0182`](../challenges/CH-0182-a-census-is-dated-by-its-premise-set.md).

---

## Formulate

### The question the row demands be answered before any code

The row states it, and it is the crux:

> *"The honest question this row must answer first is whether a working-tree reading has any
> legitimate use: if it does, the fix is a key name a gate can refuse; if it does not, the fix is to
> stop emitting it, and that is one line per emitter."*

It is answered below **with a measurement**, in the Plan's cheap bound, and the measurement reverses
the row's own scope in one direction and confirms its diagnosis in another.

### The answer, in four lines

**Yes, and the corpus has seven files using it that way.** The dominant legitimate use is the
**after** half of a *before/after* measurement of a repair the pass itself performs — `T-285`'s
`gateDefectsBeforeOnTheWorkingTree` **21** against `gateDefectsAfterOnTheWorkingTree` **0**,
`T-281`'s **21** against **21** (the repair moved no pre-existing defect), `T-282`'s **9** on the
tree against **8** at the ref with the reason recorded in prose. The *before* is pinned at the
file's own `baselineRef`; the *after* **cannot be pinned by construction**, because the emitter runs
before its own commit exists, and deleting it would delete the standard evidence that a repair
worked — which [`C-0092`](../claims/C-0092-large-rotation-arm-branch.md)'s *a repair must leave the
defect measurable* requires.

**So the fix is the row's FIRST branch, and the discriminator is measured rather than preferred.**
A kind-A reading carries a **gate-defect count**, which no deliverable prints about itself. The two
offending records carry a **declared registry quantity** — a count the deliverable *does* print
about itself — so the same quantity exists in two publishable readings and a prose writer must
choose. That is the key name a gate can refuse: **a registry quantity under a working-tree key**.

**And there is a mechanical test that needs no taste at all.** `gpd/README.md` requires a result
file to be *"reproducible from it alone"*. Re-run each corpus-subject emitter at the file's own
recorded `baselineRef` and diff: `T-334` moves **55 leaves, 55 of 55 inside a working-tree block**,
its pinned half reproducing at **0**. A working-tree block is exactly the part of a corpus-subject
file that makes it irreproducible from itself.

**The repair is available now and was not available at emit time.** A pass's own tree becomes a
**commit** one commit later, so the cure is not to delete the block but to **re-key it to the commit
that carried it** — `bb678d2` for `T-334`, `bee6b06` for `T-332` — after which it is pinned, it
re-derives for ever, and `CH-0293`'s four wrong leaves become right.

### Numeric targets

| | target | value |
|---|---|---|
| `P1` | committed result files recording a reading of an **uncommitted working tree** | **13**, over **23** distinct key names, **199** numeric leaves, of which **3** keys are PROSE (`workingTreeCaveat`, `whyTheWorkingTreeReadingDiffers`, `quietTree` — an emitter explaining the difference in words, which is the good practice) |
| `P2` | of `P1`, kind **A** — the *after* half of a before/after repair the pass performs, un-pinnable by construction | **7** files: `T-250`, `T-281`, `T-282`, `T-285`, `T-289`, `T-292`, `P-30` |
| `P3` | of `P1`, kind **B** — a rival absolute reading of a **declared registry quantity**, whose pinned counterpart already answers the same question | **2** files, **2** blocks: `T-332`'s `workingTreeBeforeThisClaimsOwnFiles` and `T-334`'s `atThisPassesTree` |
| `P4` | of `P1`, kind **C** — a working-tree reading of a corpus the pass does not change, stable today by luck and pinnable at the pass's own commit | **4** files: `T-207` (prose only, no numeric leaf), `T-280`, `T-286`, `T-327` |
| `P5` | **non-prose consumers** of any working-tree leaf — Kotlin, a `ResultInputs` read, another tool, a `jq` in a script | **0** |
| `P6` | leaves that move when `tools/T-334-emit-result.py` is re-run at its own recorded `baselineRef` `d9a3522`, and how many are inside a working-tree block | **55**, of which **55** |
| `P7` | leaves that move when `tools/T-327-emit-result.py` is re-run at `86b3bbd` | **0** — kind C, identical |
| `P8` | `tools/T-332-emit-result.py` re-run at its own `d7b7074` | **refuses to run at all**: 1 of its **26** declared AFTER anchors no longer occurs, and it is the passage [`CH-0292`](../challenges/CH-0292-the-deliverable-quotes-the-unpinnable-half.md) repaired |
| `P9` | commits, of the repository's **298**, at which `T-332`'s hardcoded `(247, 214)` occurs | **0** — the value is a typed literal, not a reading; its own commit `bee6b06` reads `249 / 215 / 464` |
| `P10` | the new arm's defect count at `HEAD` **before** the repair, and **after** it | **2** blocks, then **0** |
| `P11` | leaves that move outside a working-tree block when the two records are re-emitted at their own refs | **0** |

**Acceptance.** `P1`–`P9` are reproduced by a shipped tool and emitted into a result file whose
`--ref` **defaults to a pinned sha and never to a moving `HEAD`** (`CH-0246`), with the resolved sha
recorded as `baselineRef`. `P10` is the new arm red before and green after, both demonstrated.
`P11` is a byte diff. The two emitters' self-tests that **assert** the unpinned shape are
**inverted, never struck** — `CLAUDE.md`'s *a named test pinning a deliberately-left defect must be
inverted when the defect is repaired*.

### Units, locked

None. Every quantity is a count of files, JSON leaves, key names or commits, or a signed difference
of two such counts. No physics; **no Kotlin under `src/` is touched**. Stated because `P-18`'s rule
— *a floor is a claim about units and it does not travel* — is why a dimensionless corpus census
must not inherit a physics emitter's `1e-9` floor.

### Conventions, fixed before deriving

- **A WORKING-TREE key is a declared subset of `UNPINNED_KEYS`**, not a regular expression over key
  names. `C-0182`: an undeclared working-tree-shaped key is a **REFUSAL**, never a default. The
  three kinds `C-0224` §2 already distinguishes are retained; only the first is a *tree* reading.
- **A REGISTRY QUANTITY is one of `tools/T-336-pinned-count-census.py`'s five declared
  `QUANTITIES`**, matched on its `record_leaf` with the state key removed. A quantity outside the
  registry is outside this arm by declaration, and the arm says so on every run (`C-0209`).
- **Reproducible-from-itself is a byte diff against a re-run at the file's own recorded
  `baselineRef`**, and the partition *inside / outside a working-tree block* is what makes the diff
  an attribution rather than an observation.
- **A re-keyed block keeps its own name and gains a `ref`.** `CH-0293` is discharged by making the
  four leaves right, and `C-0092` by `T-340`'s own result file recording both readings.
- **Struck text is not live**, blanked length- and newline-preservingly.

### Verification type

**Logical**, over the repository's own committed artifacts and the two emitters' own re-runs.
Nothing measured, nothing simulated, no physical number moves.

---

## Plan

### The cheap bound, run before any code, and what each half of it decides

**1. The population, by a hand-vetted key-token set over all committed result files.**
**13 files, 23 key names, 199 numeric leaves** — against the row's *"the two in the corpus"* and
`C-0224`'s own 4-file census-family scope. **Decides the branch**: a blanket *stop emitting it*
would delete seven files' worth of before/after repair evidence, so the answer to the crux is the
key-name gate.

**2. The reader census.** `grep` over `src/`, `tools/`, `build.gradle.kts` and every `.sh`: the only
non-prose readers of a working-tree leaf are **the emitters' own self-tests** and
`tools/T-336-pinned-count-census.py`'s classifier. **0** Kotlin studies, **0** `ResultInputs` reads
(the three handles register a path; no study spells one), **0** other tools. **Decides that the
repair breaks nothing downstream** and that `P11` can be asserted rather than hoped for.

**3. The reproducibility test.** `T-334` at `d9a3522`: **55** moved leaves, **55** of them under
`atThisPassesTree` or `armOneAtThreeRefs[2]` (whose `ref` is `null` — *"this pass's tree"*), and
**0** outside. `T-327` at `86b3bbd`: **identical**. `T-332` at `d7b7074`: **will not run**.
**Decides that the defect is mechanically detectable, that kind C is stable only by luck, and that
the cost of the task is dominated by `T-332`'s emitter rather than by the arm.**

**4. `T-332`'s offending value is a hardcoded literal** — `tools/T-332-emit-result.py` line 392
writes `{"challenges": 247, "claims": 214, "sum": 461}` verbatim, so **no `--ref` reproduces it and
no `--ref` refutes it**, and `CH-0292`'s exhaustive *0 of 298 commits* is a statement about a number
that was never read off anything. Its own commit `bee6b06` carries **249 / 215 / 464**.
**Decides that `T-332`'s repair CHANGES the value**, which is the correct outcome and must be
declared in advance so it is not mistaken for a regression.

**5. The arm's reach.** `T-327`'s `atThisPassesTree` block carries **173** entries and is invisible
to the census today, because `T-327`'s file carries none of `CENSUS_MARKERS`. **Decides that the new
arm must be scoped by the KEY and not by the census family**, or it is a gate with a measured hole
on its first day.

### Method, and its justification against cost

**Three deliverables, in this order, because the arm may not ship red (`C-0083`).**

**D1 — the measurement**, as `tools/T-340-working-tree-reading-census.py` with named self-tests over
in-memory fixtures, plus `tools/T-340-emit-result.py` writing
`gpd/results/T-340-*.json`. The classification into kinds A/B/C
carries a per-file **evidence line**, so the reader can audit it, and the reproducibility test is a
**run**, not a description.

**D2 — the two records repaired at their emitters.** `T-334`'s `atThisPassesTree` is re-keyed to
`bb678d2`, the commit that added its file, and re-derives there; the four leaves `CH-0293` names go
`12 → 13`. `T-332`'s literal is replaced by a **derived** reading at `bee6b06`. The pinned halves
must reproduce byte-for-byte, and the emitters' `--ref` defaults must be pinned shas.
`T-332`'s broken AFTER anchor is repaired **as an anchor**, not by deleting the row — the passage
still exists, struck, and the anchor must be widened to the text that survives the strike.

**D3 — the mechanism**, as one new **gated** arm in `tools/T-336-pinned-count-census.py`:
*no result file records a declared registry quantity under a declared working-tree key*, scoped by
key rather than by census family, with `WORKING_TREE_KEYS` declared and an undeclared working-tree
shape refused. Mutations go into the **existing** `tools/T-336-mutation-test.py`, which `P-31`
already declares and `build.gradle.kts` already wires, so **no new harness and no new wiring**.

**Why the mechanism is a gate on the OUTPUT and not a helper the emitters must call.** A shared
`write_working_tree_block(...)` helper is a **convention** unless something asserts the call, and
`CLAUDE.md` records *a convention is not a mechanism* six times. Asserting the emitted **shape** is
strictly stronger: it reaches an emitter that never imports the helper, which is the emitter the
next pass will write. The emission-time half is supplied instead by each repaired emitter's own
**inverted** self-test, which runs before its file is written.

**Cost.** All Python, no Kotlin, no `./gradlew study`. The dominant cost is `T-332`'s emitter, which
does not run today. Estimated at four to six hours, of which the arm and its mutations are about one.

### What would falsify this approach

If **`P5` is not 0** — if any Kotlin study, `ResultInputs` read or tool consumes a working-tree leaf
— then re-keying the two blocks is a schema change with downstream readers and the plan is wrong
about its own blast radius; the task then becomes a reader sweep before anything is re-emitted.

If **kind A is empty** (`F3`) or a kind-A reading turns out to have been pinnable (`F4`), the crux
answer inverts, the branch is *stop emitting it*, and D3's discriminator has nothing to discriminate.

If **re-emitting either record moves a leaf outside a working-tree block** (`F5`), the emitters are
not deterministic at their own refs and the repair cannot be distinguished from a re-basing, which
is `CH-0246` and forbids the re-emission entirely.

### Falsifiers, declared before the run

| | declared | status |
|---|---|---|
| `F1` | the working-tree population is not **13** files / **23** keys / **199** numeric leaves at the pinned ref, or the shipped predicate's kind split is not **7 / 2 / 4**, or any file is misclassified against its own evidence line | OPEN |
| `F2` | any working-tree leaf has a **non-prose** consumer — a Kotlin study, a `ResultInputs` read, another tool, a `jq` in a script | OPEN |
| `F3` | kind **A** is empty, i.e. every working-tree reading in the corpus is pinnable; the crux answer then inverts | OPEN |
| `F4` | any kind-**A** reading is pinnable after all — a later commit's reading equals it *and* the emitter could have named the sha at emit time | OPEN |
| `F5` | re-emitting `T-334` at `d9a3522`, or `T-332` at `d7b7074`, moves any leaf **outside** a working-tree block | OPEN |
| `F6` | the re-keyed `T-334` block does not read **13** at the four leaves `CH-0293` names, or `theCensusMovesItsOwnAnswer` loses its finding | OPEN |
| `F7` | the derived `T-332` counts at `bee6b06` are not **249 / 215 / 464** | OPEN |
| `F8` | the new arm is not red on exactly **2** blocks at `HEAD` before the repair, or not **0** after it, or it fires on any kind-**A** record | OPEN |
| `F9` | the new arm needs `git`, or `--check` fails in a copy of the tree with no `./.git` | OPEN |
| `F10` | any new mutation fails no **named** test, or the harness's subtracted baseline is not green, or `tools/T-295-mutation-input-census.py --check` reports a corpus-dependent mutation | OPEN |
| `F11` | `tools/T-336-pinned-count-census.py --check` / `--self-test`, `tools/P-31-harness-census.py --check`, `tools/T-334-gate-census.py --check` or `tools/check-corpus-links.py` moves off 0 | OPEN |
| `F12` | scoping the arm by `CENSUS_MARKERS` would have reached `T-327`'s block — i.e. the measured hole does not exist and the wider scope is unmotivated | OPEN |
| `F13` | a re-emission moves a number quoted in either deliverable, in any claim, or in any challenge | OPEN |
| `F14` | the repair of `T-332`'s broken AFTER anchor changes what the emitter asserts rather than where it asserts it — i.e. the anchor table is trimmed instead of widened | OPEN |

---

## Execute

Three deliverables, in the order the Plan sets, because the arm may not ship red (`C-0083`).

**D1 — the measurement.** [`tools/T-340-emit-result.py`](../../tools/T-340-emit-result.py), **13**
named self-tests over in-memory fixtures, emitting
[`gpd/results/T-340-a-working-tree-reading-at-the-emitter.json`](../results/T-340-a-working-tree-reading-at-the-emitter.json)
at a pinned `--ref` of `91f9a48` and recording the resolved sha as `baselineRef`. Two runs are
byte-identical. The kind table and the reader-role table are **declared with a per-row evidence
line**, and an occurrence matching no declared role **refuses** — because a consumer would invert
this task's recommendation from *remove* to *rename*, and that is not a thing to infer from a
filename. A filename regular expression read **three** occurrences wrong on its first run.

**D2 — the two records repaired at their emitters, by REMOVAL.** The coordinator's decision, on the
ground that a renamed key is one a gate must keep refusing for ever. What replaces each removed
reading is not a rename but a **different, pinned measurement**: the same census at the commit that
carried the file, `bb678d2` for `T-334` and `bee6b06` for `T-332`, which is available one commit
later and was not available at emit time. Both emitters' `--ref` defaults are pinned shas; both now
**refuse visibly on stderr** in a tree with no `./.git` instead of crashing; and the two self-tests
that **asserted** the unpinned shape are **inverted, not struck**.

**D3 — arm C**, in [`tools/T-336-pinned-count-census.py`](../../tools/T-336-pinned-count-census.py):
*no result file records a declared registry quantity under a working-tree key*, scoped by the key
and not by `CENSUS_MARKERS`. **10** new mutations in the existing
[`tools/T-336-mutation-test.py`](../../tools/T-336-mutation-test.py), so no new harness and no
`P-31` row. The arm exposed two limitations of the registry it extends, both repaired with named
tests: a pinned block may now name **its own `ref`**, and a registry leaf is matched with the state
key **stripped**, so one quantity at two pinned states is two records.

## Verify

| gate | reading |
|---|---|
| dimensional consistency | no physics; every quantity is a count of files, keys, leaves, occurrences or commits |
| limiting cases | arm C on a file with no census marker (**fires**), on kind A (**silent**), on a repaired file (**silent**); `_ref_for` with a `ref` that is not a sha (**falls back**); `reader_role` on an unclassified path (**refuses**) |
| symmetry and conservation | the re-emission partition — **0** leaves move outside a working-tree block in either file, which is what makes the change surgical rather than a re-measurement |
| numerical convergence | not applicable; the counts are exact. Determinism is asserted instead: two runs of the new emitter are byte-identical, and the file is excluded from its own population by name so the count does not differ between a first emission and a second (`CH-0182`) |
| literature cross-check | `gpd/README.md`'s *"reproducible from it alone"*, run as a test rather than cited |

**Run directly as Python, in the checkout and in a copy of the tree carrying no `./.git`.** No
`./gradlew` and no `tools/verify.sh`: a sibling agent held Kotlin studies in flight for the whole
iteration.

| | |
|---|---|
| `tools/T-336-pinned-count-census.py --self-test` | **83**, 0 failures |
| `tools/T-336-pinned-count-census.py --check` | **0** defects (**4** before the repair) |
| `tools/T-336-pinned-count-census.py --rederive` | **15** records, **0** mismatches (was 11) |
| `tools/T-336-mutation-test.py` | **43** mutations, **0** survivors, green subtracted baseline |
| `tools/T-340-emit-result.py --self-test` | **13**, 0 failures |
| `tools/T-334-emit-result.py --self-test` | **11**, 0 failures |
| `tools/T-332-emit-result.py --self-test` | **15**, 0 failures |
| `tools/T-334-gate-census.py --check` | **0** defects |
| `tools/T-295-mutation-input-census.py --check` | this harness **43 / 43 fixture-backed / 0 corpus-dependent / 0 survivors** |
| `tools/cli_guard.py --check` | **51** writers, 51 parse or refuse |
| links, tables, identifiers, result paths | **0** attributable to this task |

Filed as [`C-0226`](../claims/C-0226-a-working-tree-reading-at-the-emitter.md), raising
[`CH-0295`](../challenges/CH-0295-a-task-declared-by-registering-is-unreachable.md) and
[`CH-0296`](../challenges/CH-0296-a-quantity-under-a-synonym-is-invisible.md).
