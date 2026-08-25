# `T-334` — the checker census as a **reachability** question, derived rather than typed

**Leaf** `A8.2`.
**Raised by** [`CH-0286`](../challenges/CH-0286-a-gate-wired-through-a-helper-is-invisible-to-the-census.md),
filed by [`C-0220`](../claims/C-0220-fifteenth-answers-synthesis.md) (`T-332`),
against the self-describing checker census in [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md)
and [`C-0210`](../claims/C-0210-fourteenth-answers-synthesis.md) §4(b)'s **`16 + 1 + 20` = 37**.
Predecessors in the same family:
[`CH-0222`](../challenges/CH-0222-a-self-describing-count-can-be-right-and-its-predicate-wrong.md)
(the number was right and the predicate wrong) and
[`CH-0243`](../challenges/CH-0243-a-checker-census-keyed-on-a-filename-prefix.md)
(the predicate was a filename prefix).

---

## Formulate

### The question

*How many distinct tools can make a `tools/verify.sh` run fail?*

The number has now been answered four times by four predicates — a **filename prefix** (`CH-0222`,
`CH-0243`), *invocations in one file* (`C-0210`), and *invocations in two files under two literal
shapes* (`C-0220`, `CH-0286`) — and each replacement was named as a predicate one level closer to
the question than the last. `CH-0286` diagnoses the third as *a predicate about a **literal** where
the question is about an **invocation***, and publishes **`51`** as the honest figure.

**That diagnosis is right about the literal and stops one level short of the question, which is
about a RUN.** An invocation of a tool inside an `Exec` task that `:test` does not depend on is not
something that can fail a `tools/verify.sh` run; Gradle never executes it. So the deliverable is
not a fifth number typed into a document. It is:

1. a predicate stated in terms of **reachability** rather than of textual shape, with its residue named;
2. the number **at three refs**, so the drift is visible rather than asserted;
3. a **tool** the next synthesis runs instead of typing three greps, wired so it cannot rot; and
4. a **gate** with content — an invariant that ties the reachability set to a declaration the
   corpus already maintains, so a new unreachable task cannot be silently counted as build-failing
   again.

### The predicate, stated exactly

> A tool `T` under `tools/` **can fail a default `tools/verify.sh` run** — that is, `tools/verify.sh`
> invoked with **no arguments**, under its `set -euo pipefail` at line 133 — if and only if at
> least one of:
>
> - **route A** — `tools/verify.sh` executes `tools/T` as the **command word** of a
>   comment-stripped line of its own body. *Command word*, not *mention*: this is
>   [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py)'s `shell_command_words`,
>   which `C-0206` made a **use** and not a mention, so `echo "skip with: tools/x.py"` names a tool
>   and runs nothing.
> - **route B** — `tools/verify.sh` runs `./gradlew test` (its line 175) and `tools/T` is named
>   inside the **balanced `commandLine(...)` span** of an `Exec` task of
>   [`build.gradle.kts`](../../build.gradle.kts) — `P-31`'s `command_line_spans`, with Kotlin
>   comments blanked, so a commented-out wiring and a `description = "Runs tools/T, …"` are both
>   excluded — **and that task is reachable from `:test`** through
>   `tasks.named("test") { dependsOn(…) }`.
>
> The answer is the **union of the two routes, de-duplicated by basename**.

Two properties of this predicate are deliberate and are what separate it from all four
predecessors.

- **It carries no `--self-test` filter.** A failing self-test fails the same run. The
  `--self-test` filter separates *gates over the corpus* from *self-tests over fixtures*, which is
  a different and also interesting question — reported as its own row, never as the answer.
- **It reads `dependsOn`, not `commandLine`.** Both literal and helper wirings are seen, because
  `P-31`'s span predicate sees both; whether the task **runs** is then a separate fact, read from
  the one place that decides it.

### What the predicate deliberately EXCLUDES, and which is printed beside every count

`C-0209`: a gate that can come clean must say what it does not reach, or a clean run is read as a
statement about the whole corpus.

| excluded | why | count at `HEAD` |
|---|---|---|
| `Exec` tasks registered but **not** reachable from `:test` | Gradle does not execute them; `build.gradle.kts`'s own comment says *"NOT reachable from `:test`"* | **12** |
| library modules reached only by `import` / `spec_from_file_location` from a tool on either route | a defect in one fails the run, and **no invocation names it** | **3** |
| `tools/snapshot.sh` | `tools/verify.sh` **sources** it; the command word is `source` | **1** |
| everything `./gradlew test` runs that is not a `tools/` script | the Kotlin suite is most of what can fail the run and is not a tool | not counted |
| a **non-default** invocation | `--no-checks` and any `--drop` set `checks="no"` and delete route A entirely; trailing `$@` reaches `./gradlew test`, so `-x <task>` can delete a route-B member | out of scope, stated |

### Numeric targets and falsifiable acceptance predicates

Derived in **Plan** below as the cheap bound; the run must reproduce each of them from the shipped
tool and emit them at a recorded `baselineRef`.

| | target | value |
|---|---|---|
| `P1` | the union at `HEAD` = `d9a3522` | **44** |
| `P2` | the union at `71d126e`, `C-0210`'s own baseline | **42** |
| `P3` | the union at `d7b7074`, `C-0220`'s own baseline | **44** |
| `P4` | the decomposition of `CH-0286`'s `51 − 44` into three signed terms | `−12`, `+13`, `−8` |
| `P5` | `Exec` tasks registered and not reachable from `:test`, at `HEAD` | **12** |
| `P6` | tools reached only by import — the residue closure's surplus over `P1` | **3** |
| `P7` | the shipped gate's defect count at `HEAD` | **0** |

**Acceptance.** `P1`–`P6` are reproduced by
[`tools/T-334-gate-census.py`](../../tools/T-334-gate-census.py) — at Formulate this was a code
span and not a link, because a task file is committed **before** the artifact it names and
`tools/check-corpus-links.py` is right to refuse a link to something that does not exist yet; the
link was added in Execute, once it did — from the tree and from a `git archive` of each ref, agreeing with the Formulate-stage derivation **exactly** (these are
integers; there is no tolerance). `P7` is `--check` exiting 0 at `HEAD`. Every count is emitted
beside the **predicate that produced it** and the **state it was read at**, which is `C-0210`'s own
discipline and the only form in which a self-describing number can be checked.

### Units, locked

None. Every quantity in this task is a **count of distinct basenames under `tools/`**, or a signed
difference of two such counts. No physics, no `nm`, no `pN`. Stated because
[`C-0073`](../claims/C-0073-determined-precision-of-a-result-file.md) (`P-18`)'s rule — *a floor is a claim about
units and it does not travel* — is why a dimensionless corpus census must not inherit a `1e-9`
absolute floor from a physics emitter.

### Conventions, fixed before deriving

- **Distinctness is by `os.path.basename`.** `P-31-harness-census.py` invoked twice, at
  `--self-test` and at `--check`, is **one** tool. `C-0210`'s `20` and `C-0220`'s `21` already
  de-duplicate this way; the reachability count must too, or the sum is not comparable.
- **The ref for a corpus-subject measurement is an argument, never a moving `HEAD` default that
  overwrites the record** ([`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md)).
  The emitter takes `--ref`, resolves it, and records the resolved SHA.
- **`tools/verify.sh` line 133 (`set -euo pipefail`) and line 175 (`./gradlew test "$@"`) are
  asserted, not assumed** — the emitter refuses a `tools/verify.sh` at any ref that does not carry
  both, exactly as `T-319`'s emitter already does.

### Verification type

**Logical**, over the repository's own build wiring. Nothing here is measured and nothing is
simulated. The one empirical statement — *Gradle does not execute an `Exec` task that `:test` does
not depend on* — is settled by three independent readings and is **not** settled by a build run in
this task, because `./gradlew` must not run while a sibling agent holds studies in flight; the
empirical confirmation is named as a hand-off in `F5`.

---

## Plan

### The cheap bound runs first, and it settles the whole answer before any tool is written

Four commands over two files, run in Formulate, no build and no solve:

1. `tasks.register<Exec>(…)` occurrences of `build.gradle.kts`, comments blanked — **47** at `HEAD`;
2. the `dependsOn(…)` argument list of `tasks.named("test")` — **35** entries, every one an `Exec`
   task, so the reachable set is exactly those 35 and the unreachable set is **12**;
3. `P-31.shell_command_words` over `tools/verify.sh` — **18** distinct `tools/` basenames;
4. the union — `18 + 34 − 8` = **44**, the `34` being the distinct tools of 35 reachable tasks
   (`P-31-harness-census.py` twice) and the `8` the overlap between the two routes.

**The bound decides three things and leaves the tool with nothing to discover.**

- **The twelve helper-wired harnesses cannot fail the run.** They are exactly the 12 unreachable
  tasks, and `build.gradle.kts`'s own comment above `mutationSnapshotArguments` says
  *"registered here … so they are runnable by name and **NOT reachable from `:test`**"*. So
  `CH-0286`'s *"twelve build-failing tools"* is wrong about the twelve, and `51` is an **overcount**
  where `37` was an undercount.
- **`51 − 44` is three errors of opposite sign, and at `C-0210`'s ref they nearly cancelled.**
  `−12` for the unreachable, `+13` for the Gradle tools the `--self-test` filter and the
  de-duplication removed, `−8` for the overlap the *"the two sets are disjoint"* clause asserts and
  the honest predicate does not have. At `71d126e` the same three are `−6 / +12 / −7` = `−1`, so
  `43` was within one of `42` **by coincidence**, and the coincidence has since expired.
  `CLAUDE.md`'s *decompose a ratio before predicting it*, on a difference of two censuses.
- **The corpus already knew.** `P-31`'s `HARNESSES` table declares exactly those twelve `BY-HAND`,
  and the sets are **equal in both directions**. `CH-0286` names `P-31` as the instrument to derive
  the census from and did not read the column that answers it.

Because the bound is complete, the tool's job is **not** to find the number. It is to make the
number **re-derivable** and to make the mistake **unrepeatable** — which is what the task row asks
for and what four typed re-derivations have failed to deliver.

### The method, and its cost

**A new tool, `tools/T-334-gate-census.py`, that IMPORTS `tools/P-31-harness-census.py` rather than
copying it.**

Extending `P-31` in place was considered and is refused, on two measured grounds. `P-31`'s subject
is *mutation harnesses*, not *gates*, and widening it would make its `--check` a census of two
different populations; and `P-31-harness-census.py` is itself a **declared mutation subject** of
`tools/T-306-mutation-test.py`, so an edit to it risks orphaning transcribed anchors — the exact
failure `P-31` exists to catch (`C-0185`). Importing is the corpus's own idiom (`T-332` imports
`T-319` imports `T-276`) and keeps *use-not-mention* resolution as **one** implementation.

Modes:

- **default / `--census [--ref REF]`** — the whole table: the four historical predicates with their
  counts, the reachability union, the twelve, the residue, and the three-term decomposition of any
  published number handed to it. Exit 0 always. *This is what the next synthesis runs.*
- **`--check`** — three arms, every one clean at `HEAD` and every one able to fail:
  1. the set of `Exec` tasks unreachable from `:test` **equals** the set of harnesses `P-31`
     declares `BY-HAND`, in **both** directions — 12 = 12 today, derived from a Kotlin
     `dependsOn` list and a Python declaration table that share nothing;
  2. every tool named in an `Exec` task's `description` is a tool that task's `commandLine` runs —
     `C-0206`'s use/mention consistency, 0 defects today;
  3. every tool on either route exists under `tools/` and is executable — 0 defects today.
- **`--self-test`** — named self-tests over in-memory fixtures, no repository read.

The gate is deliberately **not** a check on the wording of `DECISIONS-FOR-NDI.md`. Arm 1 is the
invariant that actually prevents recurrence: it is impossible to add a helper-wired harness and
have the tree stay green without declaring it `BY-HAND`, and once it is declared `BY-HAND` the
census subtracts it by construction. A gate parsing *"`18 + 21 + 12` = FIFTY-ONE"* out of prose
would be a gate on a numeral, which is the class of predicate this task exists to retire.

**Cost.** ~450–550 lines of Python with ~25 named self-tests, an emitter for
`gpd/results/T-334-*.json` carrying `baselineRef`, a mutation harness declared in `P-31`'s table
and wired, two `Exec` tasks and two `dependsOn` entries in `build.gradle.kts`, the claim this task will
file, and the hand-off text. Against it: the alternative is a fifth typed number, and the four typed numbers cost
four passes and produced three wrong answers.

### What would falsify this approach

- If an `Exec` task not in `dependsOn` **does** run under `./gradlew test` — then reachability is
  the wrong discriminator and the union is 56, not 44. Three independent readings say otherwise;
  the empirical run is `F5`.
- If `tools/verify.sh` reached a tool by a shape `shell_command_words` cannot see — a pipeline, an
  `if`, a command substitution. Measured in Formulate: the only two `tools/…` strings in
  `tools/verify.sh` that are not command words are the `source` of `snapshot.sh` and one `echo`.
- If a second file wired tools into the same run. Measured: `.github/workflows/` runs no `tools/`
  script, and no other `tools/*.sh` is invoked by `tools/verify.sh`.
- If arm 1's agreement were a tautology — if `P-31`'s `BY-HAND` were itself derived from
  `dependsOn`. It is not: `BY_HAND` is a hand-written literal in `HARNESSES`, cross-checked by
  `tools/T-295-mutation-input-census.py` against each harness's own printed **usage** line.

---

## The declared falsifiers

Declared **before** the tool is written, and before this file is committed.

| | fires if | status |
|---|---|---|
| `F1` | the shipped tool's union at `HEAD` is not **44**, or at `71d126e` not **42**, or at `d7b7074` not **44** — i.e. the tool disagrees with the Formulate cheap bound at any of the three refs | **OPEN** |
| `F2` | the unreachable set and `P-31`'s `BY-HAND` set are **not** equal at all three refs. Equality is asserted at `HEAD`; at `71d126e` and `d7b7074` it is **untested and expected to hold at 6 and 11**. A failure at a historical ref is a finding about when the invariant became true, not a defect in the gate | **OPEN** |
| `F3` | `51 − 44` does not decompose into exactly `−12 / +13 / −8`, or the same decomposition at `71d126e` does not sum to `−1` | **OPEN** |
| `F4` | the residue closure is not **3** import-only modules, or any of the 44 is missing or non-executable | **OPEN** |
| `F5` | a `./gradlew test` run executes any of the twelve unreachable tasks. **Cannot be run in this task** — a build must not run while a sibling holds studies in flight. Declared as a hand-off: the coordinator's next `tools/verify.sh --committed` settles it, and the log will name every task it ran | **OPEN — HAND-OFF** |
| `F6` | any mutation of the shipped gate's three arms fails **no** named test, or the harness's unmutated baseline is not empty (`CH-0237`'s subtracted baseline) | **OPEN** |
| `F7` | `tools/P-31-harness-census.py --check` moves off 0 defects, or any harness's row count changes, as a result of this task's edits | **OPEN** |
| `F8` | wiring the new tool changes the answer it reports — the census destroying itself (`CH-0182`). **Expected to fire**: two new `Exec` tasks in `dependsOn` take the working-tree union from 44 to **45**. It is declared as a falsifier because a pass that did not notice would publish a number its own commit had already moved; the repair is to emit **both** readings and name the state of each | **OPEN — expected to fire** |
| `F9` | the result file is not byte-identical across two independent runs at a fixed `--ref` | **OPEN** |

---

## What this task does not do

- **It does not edit `DECISIONS-FOR-NDI.md`, `ANSWERS.md`, `TASKS.md`, `JOURNAL.md` or
  `CLAUDE.md`.** The substitution the corrected census implies is reported as a hand-off with the
  exact before and after text; applying it belongs to whoever owns those documents this iteration.
- **It does not touch any physics, any flatness verdict, any coupled census or any result file
  other than its own.** `CH-0286` says as much and it is restated here.
- **It does not repair `P-31`'s `wired_in`.** That function reports *"build.gradle.kts"* for the
  twelve `BY-HAND` harnesses, which is true of the **file** and not of the **run**; whether it
  should carry the reachability fact is a question about `P-31`'s own subject, and it is filed
  rather than answered.
- **It does not run `./gradlew` or `tools/verify.sh`.** Every gate is run directly as Python.

---

## Execute and Verify — the run

Filed as [`C-0222`](../claims/C-0222-the-gate-census-by-reachability.md).

### What was built

| | |
|---|---|
| [`tools/T-334-gate-census.py`](../../tools/T-334-gate-census.py) | the predicate, the four historical predicates kept beside it, the residue, the decomposition, the three-arm `--check`; **42** named self-tests over in-memory fixtures and a `_StubTree`, reading no repository state |
| [`tools/T-334-mutation-test.py`](../../tools/T-334-mutation-test.py) | **23** mutations, **0** survivors, over a measured and subtracted green baseline |
| [`tools/T-334-emit-result.py`](../../tools/T-334-emit-result.py) | **10** self-tests; emits [`gpd/results/T-334-the-gate-census-by-reachability.json`](../results/T-334-the-gate-census-by-reachability.json) with `baselineRef` `d9a3522`, byte-identical across two runs |
| [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py) | one row added to `HARNESSES`, declaring the harness and its `killed-by` / `survives` shape |
| [`build.gradle.kts`](../../build.gradle.kts) | three `Exec` tasks and three `dependsOn` entries |

### The targets, all reproduced

| | target | derived | reproduced by the tool |
|---|---|---|---|
| `P1` | union at `d9a3522` | **44** | yes |
| `P2` | union at `71d126e` | **42** | yes |
| `P3` | union at `d7b7074` | **44** | yes |
| `P4` | decomposition of `51 − 44` | `−12 / +13 / −8` | yes |
| `P5` | `Exec` tasks unreachable from `:test` | **12** | yes |
| `P6` | import-only residue | **3** | yes |
| `P7` | `--check` defects at `d9a3522` | **0** | yes |

### The falsifiers

`F8` **fired, as declared, and by more than declared** — `44 → 46`, where the task file predicted
45, because the prediction counted the `Exec` **tasks** the wiring adds and the answer counts
**tools**. `F5` is a **hand-off** to the coordinator's `tools/verify.sh --committed`. Every other
falsifier did not fire, and `F2` is **tested at all three refs** rather than asserted at one:
`6 = 6`, `11 = 11`, `12 = 12`. `F3` did not fire and needed a parameter it was not declared with —
`37` counted the literal shape only where `43 / 50 / 51` counted both — so the tool now takes it
and asserts that each published figure **reconstructs** from its own two parts. The full table is
`C-0222` §7.
