# `T-336` — a self-describing count the deliverable **prints** against the one a result file **pins**

**Leaf** `A8.2`.
**Raised by** [`C-0222`](../claims/C-0222-the-gate-census-by-reachability.md) (`T-334`) §8, whose
last validity-range bullet reads *"the deliverable's own sentence is still a typed numeral … nothing
yet checks that what `DECISIONS-FOR-NDI.md` prints agrees with what the tool derives. That is
`T-336`, and until it lands the recurrence is prevented only for the **wiring** half of the defect,
not for the **quoting** half."*
Predecessors on the same count:
[`CH-0222`](../challenges/CH-0222-a-self-describing-count-can-be-right-and-its-predicate-wrong.md),
[`CH-0243`](../challenges/CH-0243-a-checker-census-keyed-on-a-filename-prefix.md),
[`CH-0286`](../challenges/CH-0286-a-gate-wired-through-a-helper-is-invisible-to-the-census.md).

---

## Formulate

### The question, and why the row's own framing turns out to be aimed at the wrong document

The row asks for *"a gate that reads the checker-census figures out of
[`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) and fails when they disagree with
`tools/T-334-gate-census.py`"*, **or** a recorded refusal.

**The cheap bound (Plan, below) says both branches are aimed slightly off, and it says so with a
measurement rather than with a preference.** Every T-334 figure now standing in
`DECISIONS-FOR-NDI.md` is **pinned to a sha and re-derives exactly** — `44` at `d9a3522`, `42` at
`71d126e`, `44` at `d7b7074`, `46` at `bb678d2`, and the whole `18 / 34 / 8` and `−12 / +13 / −8`
decomposition with them. The pass that quoted a **command** and pinned every figure to a **state**
did not drift. So a gate pointed at that passage would come clean and would be measuring the one
half of the corpus that is already right.

**The half that is wrong is a different self-describing count, in the other document, and it is
wrong against its own pass's machine record.** [`ANSWERS.md`](../../ANSWERS.md) line 1385 carries
the challenge-and-claim census, which its own text says has been stale at **eight of nine** passes,
and which twice states — in print — what is missing:

> *"So what is missing has never been the number and is still not: it is the **COMPARISON**, and the
> two gates that could make it are already wired."*

At `52a7bf3` that passage's live figures are **`247` challenges, `214` claims, `461` together**, and
its forward reading **`248 / 215`**. Its own pass's result file,
[`gpd/results/T-332-fifteenth-answers-synthesis.json`](../results/T-332-fifteenth-answers-synthesis.json),
records **two** readings: `selfDescribingCounts.*.atRef` = **`246 / 213 / 459`** at a **resolved**
`baselineRef` of `d7b7074`, and `workingTreeBeforeThisClaimsOwnFiles` = **`247 / 214 / 461`** at no
state at all. **The deliverable quotes the one of the pair that nothing can pin** — and neither
`(247, 214)` nor `(248, 215)` occurs at **any** of the repository's **296** commits.

The predecessor pass had already written the rule into its own emitted record:
[`gpd/results/T-319-fourteenth-answers-synthesis.json`](../results/T-319-fourteenth-answers-synthesis.json)
says *"only `atRef` is emitted, and the deliverable quotes the ref rather than the tree"*. Nothing
gated it, and the next pass emitted a tree reading **and quoted it**.

### The question, stated exactly

> Given that a self-describing count is a fact about the corpus and therefore belongs to no claim —
> `CLAUDE.md`'s *the one number a numeric tracer cannot own* — **what object can a deliverable's
> figure be checked against, such that the check is permanent rather than momentary?**

Not against `HEAD`. [`CH-0182`](../challenges/CH-0182-a-census-is-dated-by-its-premise-set.md) — *a
census over a corpus that contains the census destroys itself* — makes a gate on agreement-with-`HEAD`
**unsatisfiable by construction**, and the passage says so itself (*"its own finished tree reads
248 / 215"*). A gate that can never come clean is not a gate
([`C-0083`](../claims/C-0083-markdown-tables-that-do-not-render.md)).

**Against a `(quantity, value, resolvedRef)` triple a committed result file already writes.** A sha
does not move when the corpus grows, so the equality is permanent; and the comparison
*prose ↔ committed JSON* needs **no `git` at all**, which matters because
[`tools/snapshot.sh`](../../tools/snapshot.sh) excludes `./.git` and `tools/verify.sh` runs every
check inside that snapshot — **a git-dependent gate is unwirable here, and would skip silently**
where it is wired ([`C-0195`](../claims/C-0195-the-discriminating-input.md);
[`C-0177`](../claims/C-0177-queue-status-vocabulary.md)'s *a gate that cannot fail*).

### How this differs from the gate-on-a-numeral `C-0222` refused

`C-0222` §4 refused *"a gate parsing `18 + 21 + 12` = FIFTY-ONE out of prose"* as *"a gate on a
**numeral**, which is the class of predicate this task exists to retire"*. The refusal is right, and
it is right about **two** properties of that gate, not one.

| | the refused gate | this one |
|---|---|---|
| **anchor** | a numeral pattern — unbounded surface: spelled forms, three counts in one sentence, sums, struck history | a **declared registry entry**, `(quantity, deriving tool, subject phrase)`, refusing an undeclared quantity rather than defaulting it (`C-0182`'s third state). Prose is read only to **locate** a figure |
| **comparand** | a **live derivation at `HEAD`** | a **committed JSON leaf** at that file's own resolved `baselineRef` |
| **can it come clean?** | **no, by construction** — `CH-0182` forbids `prose == HEAD` for a census of the corpus that contains the census | **yes, permanently** — a sha-pinned value never moves |
| **predicate applied to the figure** | *does this numeral equal today's value* | *is this figure a value some machine record **pins*** — a **membership** test, not a value test |
| **object class** | prose against a running program | **two committed artifacts in the same tree**, sharing no code — which is exactly the class `C-0222` itself shipped as its arm 1 (`dependsOn` list against `HARNESSES` table) |
| **`git` needed** | yes, to derive | **no** |

The distinction is not a rephrasing. `C-0222`'s refusal turns on the comparand being **momentary**;
the numeral-parsing is the *symptom* of having no stable thing to compare against. This task supplies
the stable thing — it was already being written, by three emitters, and nobody had ever read it back.

### Numeric targets

Derived in **Plan** as the cheap bound, at `52a7bf3`. Integers; no tolerance.

| | target | value |
|---|---|---|
| `P1` | live **quoted tool reports** across both deliverables — a figure attributed to a named `tools/` script | **9** (6 in `ANSWERS.md`, 3 in `DECISIONS-FOR-NDI.md`), **0** false positives |
| `P2` | pinned `(quantity, value, resolvedRef)` records in committed result files | **20**, in **4** files |
| `P3` | of `P2`, how many re-derive at their own recorded ref | **20**; **0** mismatches |
| `P4` | distinct corpus **quantity families** the deliverables print that a committed tool derives on every default `tools/verify.sh` run | **4** — challenge files, claims, claims-and-challenges, and the gate census with its four sub-predicates |
| `P5` | live prose figures of a registry quantity that **no** committed record pins | **5 distinct values** (`247`, `214`, `461`, `248`, `215`) over **11 tokens**, all on `ANSWERS.md` line 1385 |
| `P6` | commits, of the repository's **296**, at which `(challenges, claims)` is `(247, 214)` or `(248, 215)` | **0** and **0** |
| `P7` | numeric leaves of `T-334`'s **unpinned** `atThisPassesTree` block that disagree with **every** committed state from `bb678d2` onward | **4** of **13** |
| `P8` | the shipped `--check`'s defect count at `52a7bf3` | **0** |

**Acceptance.** `P1`–`P7` are reproduced by the shipped tool `tools/T-336-pinned-count-census.py`
(a code span and not a link, because a task file is committed **before** the artifact it names and
[`tools/check-corpus-links.py`](../../tools/check-corpus-links.py) is right to refuse a link to
something that does not exist yet). `P8` is `--check` exiting 0. Every figure is emitted beside the
**state it was read at**, and the emitter's `--ref` **defaults to a pinned sha and never to a moving
`HEAD`** ([`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md)).

### Units, locked

None. Every quantity is a **count of files, tokens or JSON leaves**, or a signed difference of two
such counts. No physics. Stated because `P-18`'s rule — *a floor is a claim about units and it does
not travel* — is why a dimensionless corpus census must not inherit a physics emitter's `1e-9` floor.

### Conventions, fixed before deriving

- **A record is PINNED iff its file carries a resolved 40-hex `baselineRef` and the value sits under
  a key the registry classifies as pinned.** `atRef` is pinned; `workingTreeBeforeThisClaimsOwnFiles`,
  `atThisPassesTree` and `asWrittenBeforeThisPass` are **not**. An **undeclared** key shape is a
  **REFUSAL**, never a pass.
- **Struck text is not live**, blanked length- and newline-preservingly, so reported line numbers
  survive. `C-0071`'s *strike, never delete* is what makes historical readings safe to leave in place.
- **Distinctness of a prose figure is by `(document, line, value)`**, so one value quoted twice on
  one line is two tokens and one figure; both are reported.
- **The prose arm reads no `git`.** The re-derivation arm does, and prints a **visible `stderr` skip**
  where `.git` is absent.

### Verification type

**Logical**, over the repository's own committed artifacts. Nothing measured, nothing simulated.

---

## Plan

### The cheap bound, run in Formulate, and what it decides

The row's cheap bound is *"one grep: how many self-describing counts does either deliverable print
that a committed tool already derives on every run"*. Run, it is five commands and it settled the
shape of the answer before a line of the tool existed.

1. **The population is 9 and it is tight.** A *quoted tool report* — a live passage naming a
   `tools/` script and attributing a figure to it — occurs **9** times: six in `ANSWERS.md`
   (`check-challenge-index.py` ×3, `check-corpus-identifiers.py` ×3) and three in
   `DECISIONS-FOR-NDI.md` (the naming predicate, `ten / eleven / eleven`). **Zero false positives**,
   because the anchor is the **tool**, not a numeral.
2. **Four quantity families, and the records to check them against already exist.** Four committed
   result files carry **20** `(quantity, value, resolvedRef)` triples: `T-276` at `7f7957d`, `T-319`
   at `71d126e`, `T-332` at `d7b7074`, `T-334` at `d9a3522`.
3. **All 20 re-derive exactly, 0 mismatches** — `211 / 184 / 395`, `231 / 204 / 435`, `246 / 213 / 459`,
   and T-334's `18 / 34 / 8 / 44 / 12 / 3 / 11 / 21 / 12`. *So the machinery is correct and has simply
   never been read back.*
4. **The defect is a membership failure, not a staleness.** `ANSWERS.md`'s live `247 / 214 / 461` and
   `248 / 215` are pinned by **no** record and occur at **no** commit of **296**. Their own pass's
   file records `246 / 213 / 459` at `d7b7074`, unquoted.
5. **And the same defect is inside `T-334`'s own result file.** Its `atThisPassesTree` block names no
   ref; **4** of its **13** leaves (`unreachable`, `gradleHelper`, and arm 1's two) read `12` where
   every committed state from `bb678d2` on reads **13**. `CH-0246` met inside the artifact that cites
   `CH-0246` — as `C-0222`'s own Conditions row half-anticipated (*"HEAD moved twice while this claim
   was being drafted"*). **And it was wrong at the moment it was committed**: the thirteenth
   helper-wired harness, `tools/T-326-mutation-test.py`, was added and wired in **the same commit,
   `bb678d2`**, by a sibling agent. On a shared checkout the tree an emitter reads and the tree its
   commit records are different objects, and a record naming no ref can never say which one it meant.

**The bound decides four things.**

- **The instrument is not a numeral parser over `DECISIONS-FOR-NDI.md`.** That document's census
  figures are all pinned and all correct; a gate there comes clean and measures nothing.
- **The comparand is a committed JSON leaf, so the gate needs no `git`** — which is the only reason
  it can be wired at all, `tools/snapshot.sh` excluding `./.git`.
- **`CH-0182` is dissolved rather than worked around.** The answer to *a census over a corpus that
  contains the census destroys itself* is not to give up on checking; it is to **quote the `atRef`
  reading and never the tree reading**, which `T-319` wrote down and the next pass inverted.
- **The prose arm is RED at `HEAD` and the record arm is CLEAN**, so the two must ship separately —
  `C-0129`'s idiom: wire the gate on what can be made clean, and **print the residue beside it,
  ungated**, with the count and the per-file list (`C-0209`).

### The method, and its cost

**A new tool, `tools/T-336-pinned-count-census.py`, importing `tools/T-334-gate-census.py`** (which
already imports `P-31`) rather than copying any resolution. Extending `T-334` in place is refused on
`C-0222`'s own stated grounds, verbatim: `T-334-gate-census.py` is a **declared mutation subject** of
`tools/T-334-mutation-test.py`, so an edit risks orphaning transcribed anchors, which is the failure
`P-31` exists to catch.

Modes:

- **default / `--census [--ref REF]`** — the registry: each declared quantity, its deriving tool, its
  pinned records, and the prose figures found. Exit 0. *This is what the next synthesis runs before
  typing anything.*
- **`--check`** — the **git-free** arms, each clean at `52a7bf3` and each able to fail:
  1. **PINNED-RECORD SHAPE** — every result file recording a registry quantity carries a resolved
     40-hex `baselineRef` (not `HEAD`, not absent), and every count sits under a key the registry
     classifies **pinned** or **explicitly unpinned**; an undeclared shape **refuses**.
  2. **REGISTRY INTEGRITY** — every declared deriving tool exists under `tools/`, is executable, and
     is on `T-334`'s own union, so a quantity cannot be declared against a tool nothing runs.
  3. **PROSE RESIDUE, printed and not gated** — the membership failures, per document, per line, with
     the pinned value that should have been quoted.
- **`--prose --strict`** — arm 3 as a **gate**. Not wired in this task, because it is **red at
  `52a7bf3`** and this task's author must not edit `ANSWERS.md`. The substitution is handed to the
  coordinator and `T-339` promotes the arm behind a single constant once it lands.
- **`--rederive [--ref REF]`** — the **git-dependent** arm: every pinned record re-derives at its own
  recorded ref (`20 of 20` today). Prints a **visible `stderr` skip** where `.git` is absent.
- **`--self-test`** — named self-tests over in-memory fixtures, no repository read, no `git`.

Plus: an emitter for `gpd/results/T-336-*.json` with a **pinned** `baselineRef`; a mutation harness
`tools/T-336-mutation-test.py` **declared in `P-31`'s `HARNESSES` table and wired in
`build.gradle.kts` in the same act** (the omission that caused last iteration's collision); and two
`Exec` tasks plus two `dependsOn` entries.

**`F9` is declared because this task will move its own answer**: wiring adds
`T-336-pinned-count-census.py` and `T-336-mutation-test.py` to `T-334`'s union, `46 → 48`. `CH-0182`
for the tenth consecutive pass touching a census, and the second time in two tasks that the pass's
own wiring is the mover.

**Cost.** ~500–600 lines of Python with ~35 named self-tests, an emitter (~10 self-tests), a mutation
table of ~18 rows over a measured and **subtracted** green baseline (`CH-0237`), two Gradle tasks, one
claim, two challenges, two queue rows and the hand-off text. Against it: the count has been re-typed
at six passes and gone stale at five of them, and the figure standing today is one that has never
existed.

### What would falsify the approach

- **If a pinned record did not re-derive** — then the records are not a stable comparand and the whole
  shape collapses. Measured in Formulate: **20 of 20**, 0 mismatches.
- **If the prose figures could not be located without a numeral parser.** They can: the anchor is the
  deriving tool's own name or the quantity's declared subject phrase, and the measured false-positive
  rate over both documents is **0 of 9**. The rate is re-measured over the corpus's **history**
  (`C-0209`'s standard, `--history`), because a rate taken at a `HEAD` somebody has just repaired is
  not a rate.
- **If the check needed `git`** — it would skip silently inside every `tools/verify.sh` snapshot and be
  a gate that cannot fail. It does not: arms 1–3 read committed files in the tree.
- **If the registry could be satisfied vacuously** — a quantity declared against a tool that does not
  run, or an unpinned key silently defaulting to *pinned*. Arm 2 and the REFUSAL state are exactly
  these two.

---

## The declared falsifiers

Declared **before** any tool is written, and before this file is committed.

| | fires if | status |
|---|---|---|
| `F1` | the shipped tool's live quoted-tool-report population at `52a7bf3` is not **9**, or any of the 9 is a false positive, or its measured false-positive rate over the corpus's history is not **0** | **OPEN** |
| `F2` | the pinned records are not **20** in **4** files, or any of them fails to re-derive at its own recorded ref | **OPEN** |
| `F3` | the unpinned live prose figures are not exactly `247`, `214`, `461`, `248`, `215` over **11** tokens on `ANSWERS.md` line 1385 — more is a finding about the registry's reach, fewer is a defect in the predicate | **OPEN** |
| `F4` | `(247, 214)` or `(248, 215)` occurs at any of the repository's 296 commits — i.e. the figure is reproducible after all and the diagnosis is wrong | **OPEN** |
| `F5` | `T-334`'s `atThisPassesTree` block does **not** disagree with every committed state from `bb678d2` on, or the disagreeing leaf count is not **4** of **13** | **OPEN** |
| `F6` | `--check` is not **0** defects at `52a7bf3`, or any of its three arms cannot be made to fail by a named test, or any arm needs `git` | **OPEN** |
| `F7` | any mutation of the shipped rules fails **no** named test, or the harness's unmutated baseline is not empty (`CH-0237`), or `tools/T-295-mutation-input-census.py --check` reports any of this task's mutations **corpus-dependent** rather than fixture-backed (`C-0195`) | **OPEN** |
| `F8` | `tools/P-31-harness-census.py --check` or `tools/T-334-gate-census.py --check` moves off 0 defects, or any existing harness's row count changes, as a result of this task's edits | **OPEN** |
| `F9` | wiring this task's two tools does **not** move `T-334`'s union from 46 — **expected to fire**, to **48**. Declared because a pass that did not notice would publish a number its own commit had already moved; the repair is to emit both readings and name the state of each | **OPEN — expected to fire** |
| `F10` | the result file is not byte-identical across two independent runs at a fixed `--ref` | **OPEN** |
| `F11` | the prose arm, run with `--strict` at `52a7bf3`, exits **0** — i.e. the defect this task exists to catch is not caught by the instrument built to catch it | **OPEN — expected to fire, and its firing is the deliverable** |
