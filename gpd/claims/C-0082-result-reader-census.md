# C-0082 — **A grep finds 43 of the 63 read edges over `gpd/results/`, and the 20 it misses are a third of the graph.** The census is now derived from the sources and checked on every verification run. **The cheap form the task was queued with barely exists: 3 of 74 studies declare a `sources` parameter**, so *"declared equals read"* covers 4 % of the repository and the other 96 % needs the derivation. **`P-19`'s ranking inverts on it** — `window/` is the cheapest of the three sites to re-emit, not the most exposed, and its risk is an ORDER (`CH-0097`). **And `P-21`'s premise was half wrong**: `tools/test-snapshot.sh` has hung off `./gradlew test` since `P-16`; only the deliverable tracer was orphaned, and it is wired now, with the table checker

| | |
|---|---|
| **Task** | [`P-22`](../tasks/P-22.md), raised by [`CH-0092`](../challenges/CH-0092-the-propagation-did-not-close.md); **[`P-21`](../tasks/P-22.md) is folded into this claim** and is closed by the same edit |
| **Leaf** | none — this is a process blocker on the loop's own instrumentation, not a physical result. It governs what every *"re-emitting this file moves nothing else"* statement in the repository means. |
| **Verification type** | **logical** (the reader graph is a property of the source text and is derived from it) **+ in-silico** (executable, 45 self-tests, cross-checked against the two independent statements the repository already carries — a hand grep, and the studies' own `sources` declarations) |
| **Verdict** | **PASS.** The census is derived, emitted, self-tested and invoked. **Its headline is the size of what a grep cannot see**: 43 direct read edges and **20 transitive** ones, so `C-0073`'s instrument was missing **a third** of the graph, not one edge. `CH-0092`'s three published counts are reproduced exactly and are asserted as tests (`T-1d` **3** readers, `T-1f` **2**, `T-2` **0**), and the derivation is asserted to be a **superset** of the grep — the grep is a strict lower bound, and a literal it finds that the census does not attribute is a derivation bug rather than a discovery. **The cheap form the task was queued with turns out to be nearly empty**: `WindowResynthesisStudy` and `DesignWindowStudy` were the *only* two studies of 72 declaring a `sources` parameter when the task started, so the test the acceptance predicate asks for — *declared equals read* — is real, is executable, passes, and covered **2 of 72**. A sibling then filed a third mid-iteration (`electrostatics/DuplexPairSeparationStudy`, `T-139`) and **the gate passed on it at first contact**, which is the convention working rather than a coincidence — 3 of 74 at emission. What covers the rest is the derived census plus a drift gate. **A second, unqueued deliverable came out of the same graph**: `P-19`'s ranking of the rounding sites was written on `C-0073`'s `roundingSites` table, which names the wrong studies — `coupling/` does **not** round `T-129` and **does** round `T-16` and `T-17`, `window/` rounds three files rather than two, `actuator/` five rather than two with two of them in other packages — and read on the closure the ranking **inverts** (`CH-0097`). **Nothing is re-emitted**: `P-19`'s standing reason holds and this was a ranking job. **`P-21` closes with a correction to its own premise**: `tools/test-snapshot.sh` has been a `dependsOn("testHarness")` of `./gradlew test` since `P-16` (commit `ef0740f`, iteration 5), so of the two scripts it names only `tools/test-trace-answers.py` was orphaned. It is wired now, together with the coordinator's `tools/test-check-markdown-tables.py` (`P-23`) — whose **gate** half is deliberately left unwired, because a verification snapshot has no `.git` and the checker's fallback then defeats its own `third-party/` exclusion. |
| **Maturity** | **TRL 1–3.** This is an instrument, not a measurement. It is a **static** analysis: it says what the sources *say*, never what a run *does*, and the two limits of that are named in the validity range. |
| **Provenance** | `gpd/results/P-22-result-reader-census.json`, produced by [`tools/result-reader-census.py`](../../tools/result-reader-census.py); **45 self-tests** in [`tools/test-result-reader-census.py`](../../tools/test-result-reader-census.py); wired into [`tools/verify.sh`](../../tools/verify.sh) (the census, which reads the tree) and [`build.gradle.kts`](../../build.gradle.kts) (the three self-tests, which read only fixtures); **the edited script run end to end on its own snapshot** — the three Gradle harness tasks all ran, `BUILD SUCCESSFUL`, then `45 checks passed` and `result-reader census ok: 74 studies, 3 declaring, 43 direct + 20 transitive read edges`, which is the wiring proved on a real snapshot rather than in the checkout; and the **whole suite** `BUILD SUCCESSFUL in 13 m 53 s`, `--drop-file src/test/kotlin/electrostatics/DuplexPairSeparationTest.kt` for a sibling's in-progress `T-139` (which also auto-skips the census check, by the design above) |
| **Conditions** | Not a physical result: **no temperature, medium or unit applies**. Every quantity here is a count of files, edges or declarations, and the locked units are untouched. Derived over `src/main/kotlin` and `src/test/kotlin`, against the tree at emission: **74 studies, 76 result files**. Two studies and three result files were added by siblings *during* this iteration, so the counts below are quoted at emission and the census is re-emitted with them. |
| **Consumes** | [`C-0073`](C-0073-determined-precision-of-a-result-file.md) (its `roundingSites` measurement — the `determinedDigits` are consumed, the study sets are challenged), [`CH-0092`](../challenges/CH-0092-the-propagation-did-not-close.md) (its three corrected counts, as the fixed point the derivation had to hit), [`C-0078`](C-0078-status-drift-in-the-deliverable.md) (*"a check nobody remembers to ask for is not a check"*, applied to the harness's own tests) |
| **Raises** | [`CH-0097`](../challenges/CH-0097-the-rounding-site-table-names-the-wrong-studies.md) against `C-0073`'s `roundingSites` table |

---

## The claim, in one line

**The audit instrument `C-0073` used could see two thirds of the graph, the convention that would
have made the graph self-maintaining is used by three studies out of seventy-four, and the only
thing that closes the gap is a derivation — which is now written, tested against the grep as a lower
bound, and run on every verification.**

---

## Deliverable 1 — the cheap bound, which reshaped the task before any code was written

`CH-0092` publishes three counts (`T-1d`: 3 readers, `T-1f`: 2, `T-2`: 0). Those are the fixed
point: a derivation that reproduces `C-0073`'s 1 and 0 is a grep in different clothing. They are
now three assertions in `tools/test-result-reader-census.py`.

Two more free arithmetic checks came before any parsing: the repository has one study per result
file to within the handful nothing emits, so a census in which two studies write the same file is
wrong on its own count — that is a gate. And **the `sources` convention had to be measured before
the test could rest on it.** It was:

| | count |
|---|---|
| studies (a main source with a top-level `fun main`) | **74** (72 when the task started) |
| studies declaring a `sources` parameter | **3** — `window/WindowResynthesisStudy.kt`, `window/DesignWindowStudy.kt`, and `electrostatics/DuplexPairSeparationStudy.kt`, filed mid-iteration and passing the gate at first contact |
| result files in `gpd/results/` | 76 |
| result files no study writes | **3** — `T-119-literature-queries.json` (a Python survey), `T-147-third-answers-synthesis.json`, and **this claim's own census**, which a Python tool emits |

> **The acceptance predicate's cheap form is real and it is 4 % of the repository.** It is
> implemented, it is a hard gate, and both declarations pass exactly — `DesignWindowStudy` declares
> `T-1d, T-3, T-14` and reads exactly those; `WindowResynthesisStudy` declares
> `T-1d, T-14, T-1f, T-3b, T-13, T-16, T-4, T-17` and reads exactly those eight, **through a helper
> that names no directory called by a caller that names no file**. The declaration `CH-0092` said
> *"was in the repository, in a field written to be read"* is therefore correct — and it was, until
> this iteration, one of exactly two.

---

## Deliverable 2 — the derivation, and the falsifier that fired on the first version

A read is a result-file string literal **in the argument list of a `File(...)` construction** —
syntactic position, never substring — propagated along a reference graph, with comments stripped
first. Comments and prose matter: **7 main sources name a result file, in a KDoc line or in a
`findings`/`source` string, that they never open** (*"read from gpd/results/T-130-\*.json"*), and a
substring census counts every one of them as a reader — it over-reports and is then useless for certifying that a propagation closes.

**The declared falsifier of the first implementation fired, and it is the interesting result.** The
graph was built over **files**, on the stated argument that over-inclusion is the safe direction.
It is not safe enough to be usable: package `window` declares `ledger`, `array`, `reader` and
`scalar` **privately in several files at once**, so `DesignWindowStudy` — which reads three result
files — came out reading **thirteen**, and the declaration gate failed on the only two studies it
covers. The unit is therefore a **top-level declaration**, and two Kotlin scoping rules are applied:
a name resolves in its **own file** first, and a `private` top-level declaration is **invisible
outside it**. With those, the census reproduces `CH-0092` exactly.

| | value |
|---|---|
| direct read edges — what a grep for the basename finds | **43** |
| transitive read edges — what it cannot find | **20** |
| total | **63** |
| the grep's coverage | **68 %** |

The 20 all run through `window/ResynthesisInputs.kt` (8, into `WindowResynthesisStudy`; 8 more into
`SecondResynthesisStudy`) and `window/SecondResynthesis.kt` (4). **`C-0073` did not miss an edge, it
missed a third of the graph** — and the fraction is what makes *"the propagation closes"*
unsupportable as a hand judgement rather than merely unlucky.

### What the census also shows, that nobody had asked

| | |
|---|---|
| most-read result file | **`T-3b-tile-edge-load-profile.json`, 16 studies** — `C-0022`'s solved edge load is the repository's most reused input by a factor of two |
| second | `T-125-upward-root-placement.json`, **11** — `C-0063`'s placement |
| result files read by **tests** | **13**, by 6 test classes; `T-1d` is read by 4 of them |

The test readers are the ones `CLAUDE.md` already warns about (*"an assertion tighter than a result
file's EMISSION precision is not a stronger test"*) and no audit in this repository had listed them.

---

## Deliverable 3 — the check, and where it is wired

| check | what fails it | where it runs |
|---|---|---|
| **declared == read** | a study declares a file it does not read, or reads one it does not declare | `tools/verify.sh` |
| **no file has two writers** | two studies emit the same result file | `tools/verify.sh` |
| **census drift** | a study **already in the census** changes what it reads or writes | `tools/verify.sh` |
| 45 self-tests, on fixtures | the derivation itself | `tools/verify.sh` |

**Two design decisions, both taken because two other agents are running this checkout.**

1. **A study that is new to the census is a NOTE, not a failure.** A sibling adding a study must not
   have their verification run broken by a stale baseline; and a *new* study is work its own author
   can see, whereas an *existing* study silently gaining a reader is precisely the drift that fooled
   `C-0073`. A study that has left the tree is a note too, because `--drop-file` produces exactly
   that.
2. **The tree-reading checks hang off `tools/verify.sh`, not off Gradle** — because
   `build.gradle.kts` states the rule already, for `testHarness`: a task attached to `test` must
   touch nothing under `src/`, so that a `--drop`/`--drop-file` cannot make it fail. The census
   reads `src/` by construction, so it goes where the drop is known. `tools/verify.sh` skips it
   automatically when a drop was requested, and `--no-checks` skips it by hand.

### `P-21`, folded in — and its premise was half wrong

`P-21` records that `tools/test-snapshot.sh` (19 checks) and `tools/test-trace-answers.py` (42) are
*"invoked by nothing … run when an agent remembers"*. **`tools/test-snapshot.sh` has been a
`dependsOn` of `./gradlew test` since `P-16` itself** — `build.gradle.kts` registers `testHarness`
and the commit that added it is `ef0740f`, iteration 5, the same commit that wrote the tests. So
`P-16` did the wiring and `P-21` was queued believing it had not.

That leaves one orphan, and by the time this task reached it, two: the coordinator filed
`tools/check-markdown-tables.py` and its 26 self-tests (`P-23`) mid-iteration. Both self-tests read
only fixtures, so both are now Gradle tasks beside `testHarness`.

**The table GATE is not wired, and the reason is a property of `tools/verify.sh` rather than of the
checker.** It was wired, and the full run caught it: **a snapshot has no `.git`**, so the checker's
`git ls-files` fails, its fallback walks the tree emitting `./`-prefixed paths, and that prefix
defeats its own `third-party/` exclusion — the one directory whose table defect must be preserved,
because the problem definition as received may not be modified. Inside a snapshot it therefore
reports a defect it can never be rid of, and *a gate that can never come clean is not a gate* is
the checker's own stated design rule. It belongs in the checkout, where `git ls-files` works.
(The same run also found a real defect in a sibling's untracked `C-0079`, which is reported to its
author rather than edited here.)

| script | checks | invoked by, before | invoked by, now |
|---|---|---|---|
| `tools/test-snapshot.sh` | 19 | **`./gradlew test`, since `P-16`** | unchanged |
| `tools/test-trace-answers.py` | 42 | **nothing** | `./gradlew test` |
| `tools/test-check-markdown-tables.py` | 26 | **nothing** | `./gradlew test` |
| `tools/check-markdown-tables.py` (the gate) | 243 files | nothing | **left where it is, with a reason** — see below |
| `tools/test-result-reader-census.py` | 45 | — | `tools/verify.sh` |
| `tools/result-reader-census.py --check` | — | — | `tools/verify.sh` |

**`tools/trace-answers.py` itself is deliberately NOT wired.** It reads `ANSWERS.md`, which an agent
edits during an iteration; a gate on a document under revision fails for the wrong reason. Its
self-test is wired, which is what `P-21` asked for.

---

## Deliverable 4 — `P-19` re-adjudicated, and nothing re-emitted

`CH-0092` says `P-19`'s ranking *"was written on `C-0073`'s reader census"* and that *"`window/` has
two more consumers than that ranking assumed"*. That is true of `window/` as a **reader** — `T-1d`
and `T-1f` are its inputs. **The cost of re-emitting a rounding site is a statement about its
outputs**, and on the derived closure the ranking **inverts**:

| site | printed / determined | studies | emits | downstream | tests | ranking |
|---|---|---|---|---|---|---|
| `actuator/ActuatorResultRounding.kt` | 9 / **4** | 5 | 5 files | 4 | 4 | **most exposed, and the largest over-print** |
| `coupling/CouplingResultRounding.kt` | 9 / **5** | 6 | 6 files | 2 | 2 | middle — and its whole exposure is through `T-16` and `T-17`, which `C-0073`'s row omits |
| `window/WindowResultRounding.kt` | 9 / **6** | 3 | `T-2`, `T-25`, `T-118` | **none** | 1 | **cheapest — nothing outside `window/` reads any of its outputs** |
| `structure/ResultRounding.kt` | 9 / **9** | 50 | 50 files | 9 | 4 | not over-printed; nothing to do |

**But `window/` carries the one hazard the other two do not, and it is not size.** `T-118` reads
`T-25`, so a re-emission has an **order**, and running the second against a stale first is exactly
`CH-0092`'s documented failure — a reproduction residual sitting at `8.79e−7` for an iteration.
**The risk in `window/` is a sequence, and a per-site digit count cannot show it.**

While deriving that table, three of `C-0073`'s four site rows turned out to name the wrong studies
— including one member that is simply not a member (`T-129` rounds through `structure/`). That is
`CH-0097`, and it is `CH-0092`'s defect on a second table of the same claim.

**Nothing is re-emitted here.** `P-19`'s standing reason — changing code that produces published
results costs a re-run and a diff of everything downstream — is unchanged, and this task was asked
for a ranking.

---

## The five verification gates

Executed as **45 checks** in `tools/test-result-reader-census.py`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | not applicable in the physical sense, and stated rather than skipped: every quantity here is a **count**, and the two type-level analogues are checked — a *task id* is derived from a basename and round-trips on four shapes (`T-1d`, `T-125`, `P-18`, `T-5b`), and a *declared source* parses to task ids from both the abbreviated and the concatenated multi-line form | **PASS** |
| **2 — limiting cases** | a comment is not a read; a prose string is not a read; a wildcard (`T-130-*.json`) is not a file; a non-result `File` is ignored; a `File` bound to a `val` that is never written is a **read** and one that is written is a **write**; a study's own output never appears among its reads; a `//` inside a string is not a comment and a nested block comment is removed whole | **PASS** |
| **3 — symmetry and conservation** | `readersOf` is the exact inverse of the per-study `reads`; **the derived census is a strict SUPERSET of what a naive grep finds** over all 72 studies (the grep is a lower bound by construction — it finds every edge whose path is a single literal) and strictly larger (20 transitive edges); no result file has two writers | **PASS** |
| **4 — numerical convergence** | not applicable — there is no mesh, no tolerance and no iteration. The analogue that **is** checkable is **stability under the analysis's own parameters**, and it is the one that fired: at file granularity the census gives `DesignWindowStudy` 13 reads and at declaration granularity 3, so the granularity is not a refinement but the answer. Both are exercised; only the second reproduces `CH-0092` | **PASS, with the coarse variant refuted** |
| **5 — literature and upstream** | **`CH-0092`'s three published counts asserted by name** — `T-1d` read by `DesignWindowStudy`, `SecondResynthesisStudy`, `WindowResynthesisStudy`; `T-1f` by the latter two; `T-2` by nothing; the real tree passes its own declaration and write gates; the `File(directory, name)` shape of `CH-0092` is a fixture in three forms, including split across lines | **PASS** |

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the derivation reproduces `C-0073`'s 1 and 0, i.e. it is a grep in different clothing | **no** | 3 and 2, matching `CH-0092`, asserted as tests |
| **F2** | the closure is so over-inclusive that a file-level graph is the wrong granularity | **YES** | `DesignWindowStudy` read 13 of 20 result files at file granularity. Fixed by moving to declarations and applying Kotlin's own scoping; the failure is recorded in the tool's header so the next agent does not undo it |
| **F3** | many studies build their paths in a way no syntactic rule can follow | **no** | every result-file path in the tree is a literal in a `File(...)` argument list; **zero** unresolvable call sites |
| **F4** | `sources` is declared by nearly every study, so the census is redundant | **no**, emphatically | **3 of 74**, and the third arrived mid-iteration and passed |
| **F5** | the edit breaks `tools/verify.sh` for the two agents using it | **no** | full suite run through the edited script after the edit; the checks are additive, run *after* Gradle so a failure never hides a build result, skipped automatically on any `--drop`, and skippable by hand with `--no-checks` |

---

## Validity range

- **This is a STATIC analysis and it says what the sources say.** It cannot see a path assembled at
  run time, read from a configuration, or produced by reflection. There are none today (F3), and a
  new one would be invisible — which is why the drift gate is on the *derived* graph and not on a
  hand list.
- **The propagation is over declarations, and a declaration is coarser than a call.** A study that
  reaches a large class inherits every result file that class opens, even if it calls one method.
  `ResynthesisInputs.read` opens all eight in one function, so the distinction does not arise
  today; it would if a reader class were ever split by concern.
- **Visibility is approximated, not resolved.** Same package, a wildcard import, or a
  fully-qualified single import make a declaration visible; `internal`, `protected` and nested
  visibility are not modelled, and neither are overloads. The direction of the error is
  **over**-inclusion, which is the safe one for an audit that certifies closure.
- **A `sources` declaration is compared at TASK-ID granularity**, because `WindowResynthesisStudy`
  declares `"gpd/results/T-1d, T-14, T-1f, …"` and names no slug. Two result files of one task
  would be indistinguishable to the gate; there are none.
- **The census is a snapshot of a moving tree.** `resultFiles` and `unwrittenResultFiles` change
  when any agent adds a file, and they are informational for that reason; the gate is on `studies`.
- **`P-19` is re-ranked, not discharged.** No rounding site is changed and no result file is
  re-emitted by this claim.
- **The `roundingSites` correction (`CH-0097`) does not touch any `determinedDigits`.** A determined
  precision is a property of a solver's tolerance, which `P-18` measured directly and which no
  census can move.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `T-1d` has 3 readers, `T-1f` 2, `T-2` 0 | — | **CITED** (`CH-0092`) — and **re-derived here**, which is the point |
| the determined digit counts, 9 / 6 / 4 / 5 per rounding site | — | **CITED** (`C-0073` / `gpd/results/P-18-determined-precision.json`), not re-measured |
| `T-25` moved 325 fields and `T-118` 2 751 on re-emission | — | **CITED** (`CH-0092`), not reproduced here |
| `testHarness` was added in commit `ef0740f` | — | **DERIVED**, `git log -S` on `build.gradle.kts` |

Everything else — the 74 studies, the 43 + 20 edges, the 3 declarations, the 16 readers of `T-3b`,
the 13 test-read files, the four rounding sites' study sets and their closures — is **derived here
by the tool**, and re-derived on every `tools/verify.sh`.

## Still open — named, not answered

1. **A study's `sources` is a free-text string and the gate had to parse it.** Two studies write it
   two different ways. If the convention is ever to be relied on, it should be a `List<String>`
   emitted from the same constant the reader uses — at which point the declaration and the read
   cannot disagree at all. That is a change to published-result code and is **not** made here.
2. **`T-147-third-answers-synthesis.json` and `T-119-literature-queries.json` have no writer in the
   source tree.** Both are legitimate — one is a Python survey, one is a synthesis — but a result
   file with no emitter cannot be re-run, and nothing in the repository records that. Worth a
   `provenance` field rather than a rule.
3. **The 16 readers of `T-3b`.** No claim has ever asked what re-emitting `C-0022`'s solved edge
   load would move; it is by a factor of two the most reused input in the repository, and its
   emitter's rounding (`structure/ResultRounding.kt`) is the one `P-18` found is **not**
   over-printed. So the exposure is large and the risk is currently zero — which is worth recording
   before anyone changes that site.

## Challenges

**Raises [`CH-0097`](../challenges/CH-0097-the-rounding-site-table-names-the-wrong-studies.md)**
against `C-0073`'s `roundingSites` table.

**None stands against this claim.** The three ways it would fail:

1. **A result-file path assembled at run time.** The census would miss it silently, and the drift
   gate would not notice, because the gate compares derived to derived.
2. **A Kotlin visibility rule modelled wrongly in the direction of exclusion.** Over-inclusion is
   safe here; a missed edge is not, and the only defence is the grep-superset gate, which catches
   exclusion of *direct* edges but not of transitive ones.
3. **A study whose `sources` is right and whose reads are wrong.** The gate asserts they agree and
   cannot say which is correct.
