# `T-339` — the prose arm promoted, and **the row's own premise has already been discharged by the substitution it was waiting for**

**Leaf** `A8.2`.
**Raised by** [`C-0224`](../claims/C-0224-a-quoted-count-against-a-pinned-record.md) (`T-336`), whose
§7 reads *"`T-339` flips **one constant**, `PROSE_ARM_IS_GATED`, once the substitution lands; a
promotion that needs a rewrite is a promotion nobody performs."*
**Depends on** `T-340`, which repairs the two records; and on
[`CH-0292`](../challenges/CH-0292-the-deliverable-quotes-the-unpinnable-half.md)'s substitution,
which the coordinator landed in `7ff9d07`.

---

## Formulate

### The row's premise, and where it stands

The row says the arm *"is red at `HEAD` on exactly the two records it was opened about"*. **Measured,
it is green, and has been since `7ff9d07`** — the iteration-54 commit in which the coordinator
applied `C-0224` §8's four verified substitutions. `tools/T-336-pinned-count-census.py --prose
--strict` exits **0** at `HEAD`, at `dfce9c1` and at `7ff9d07`, reporting *0 prose figures pinned by
nothing, 0 unreached numerals on flagged lines*. So the promotion is available today and the
sequencing behind `T-340` is a **choice**, not a blocker.

**The parenthetical the row was briefed with — *"no result file emits an unpinned reading of a
registry quantity"* — is not the prose arm and is not an arm that exists.** It is a good description
of the arm `T-340` ships, which **is** red at `HEAD` on exactly the two records
([`CH-0292`](../challenges/CH-0292-the-deliverable-quotes-the-unpinnable-half.md)'s
`workingTreeBeforeThisClaimsOwnFiles` and
[`CH-0293`](../challenges/CH-0293-an-unpinned-block-inside-the-file-that-cites-the-rule.md)'s
`atThisPassesTree`). Both readings are carried, and both arms end green.

### Numeric targets

| | target | value |
|---|---|---|
| `Q1` | `tools/T-336-pinned-count-census.py --prose --strict` at `HEAD` **before** the flip | exit **0**, **0** membership failures, **0** unreached numerals |
| `Q2` | `--check` after the flip, at `HEAD` | exit **0**; the prose arm reported **GATED** rather than *PRINTED, NOT GATED* |
| `Q3` | `--check` after the flip, in a copy of the tree carrying **no `./.git`** | exit **0**, and no arm silently degrades |
| `Q4` | lines of `tools/T-336-pinned-count-census.py` changed | **1** constant, plus the **inversion** of the one named test that pins it, plus the header comment that names `T-339` |
| `Q5` | mutations of `tools/T-336-mutation-test.py` that become no-ops under the flip | **0**; survivors stay at **0** over a green subtracted baseline |
| `Q6` | numbers of `gpd/results/T-336-a-quoted-count-against-a-pinned-record.json` that move | **0** other than the gating flag and the counts the flip is about |
| `Q7` | the arm `T-340` ships, before and after `T-340`'s repair | **2** blocks red, then **0** |

**Acceptance.** `Q1`–`Q7` demonstrated, and `tools/verify.sh --committed` green at the assembled
`HEAD` for reasons this task owns.

### Units, locked

None — counts of defects, lines and mutations. No physics, **no Kotlin**.

### Conventions, fixed before deriving

- **A gate must be able to come clean at `HEAD`** ([`C-0083`](../claims/C-0083-markdown-tables-that-do-not-render.md)), and *coming clean* is demonstrated by **running it**, not by reading it — [`C-0224`](../claims/C-0224-a-quoted-count-against-a-pinned-record.md)'s own §8 hazard, `CH-0206`'s *a claim that wires a gate is not obliged to run one*.
- **A named test that pins a deliberately-left defect is INVERTED, never struck** — `ok("T-336 the prose arm is not gated until T-339 flips one constant", PROSE_ARM_IS_GATED is False)` becomes its own converse and keeps its subject.
- **Git-free.** `tools/snapshot.sh` excludes `./.git`, so a gated arm that needs `git` could only skip; the re-derivation arm keeps its **visible `stderr`** refusal and stays out of `--check`.

### Verification type

**Logical**, by running the gate at the states named above.

---

## Plan

### The cheap bound, and what it decides

**One command, run before anything was written.** `--prose --strict` at `HEAD`, at `dfce9c1` and at
`7ff9d07` all exit **0**. **Decides** that the row's blocker is discharged, that the flip is a
one-constant change today, and that the *interesting* half of the row is the arm `T-340` ships —
which is the arm the brief actually describes and which is genuinely red on the two records.

**A second command decides the risk.** `CLAUDE.md` records that *a mutation killed only by a test
that reads a mutable artifact is dated by that artifact*, and a gating **constant** is exactly such
an artifact: a mutation whose only killer asserts `PROSE_ARM_IS_GATED is False` becomes a no-op the
moment it is True. `tools/T-336-mutation-test.py` is run before and after and its **killer sets are
compared row by row**, not just its headline.

### Method, and its justification against cost

Flip the constant; invert the one named test; run `--check`, `--prose --strict`, `--self-test`, the
mutation harness, `tools/P-31-harness-census.py --check`,
`tools/T-295-mutation-input-census.py --check` and `tools/T-334-gate-census.py --check`, each at
`HEAD` **and** in a `./.git`-free copy of the tree. Then re-emit
`gpd/results/T-336-a-quoted-count-against-a-pinned-record.json` at its own recorded `baselineRef`
only if a number it states has moved, and say which.

Cost is minutes, and it is spent almost entirely on the two arms' *runs* rather than on the edit.
The whole justification for doing it at all is `CLAUDE.md`'s own note that the residue line is the
shape that decays: *a `note:` that exits 0 is read as a clean corpus*, and this one has a measured
population.

### What would falsify this approach

If the flip makes any mutation a no-op (`G4`), the promotion has bought a gate and sold a
measurement, and the harness needs a constructed fixture before the flip may land — `C-0161`'s
*construct the state*, met on a promotion rather than on a test gap.

If `--check` cannot come clean in a git-free copy (`G3`), the arm is not wireable where
`tools/verify.sh` runs it and the promotion is unsound however green it is in the checkout.

### Falsifiers, declared before the run

| | declared | status |
|---|---|---|
| `G1` | the prose arm is **not** already green at `HEAD` before the flip — i.e. the row's premise still holds | **DECLARED TO FIRE**: measured green at `HEAD`, `dfce9c1` and `7ff9d07`. Its firing is a finding about the row, not about the work |
| `G2` | after the flip, `--check` is not **0** at `HEAD` | OPEN |
| `G3` | after the flip, `--check` needs `git`, or fails in a copy of the tree with no `./.git` | OPEN |
| `G4` | the flip turns any mutation of `tools/T-336-mutation-test.py` into a no-op — a survivor, or a row whose **killer set** empties or shrinks | OPEN |
| `G5` | the test pinning `PROSE_ARM_IS_GATED is False` is **struck or deleted** rather than inverted | OPEN |
| `G6` | any number of `gpd/results/T-336-a-quoted-count-against-a-pinned-record.json` moves other than the gating flag and the counts the flip is about | OPEN |
| `G7` | `tools/verify.sh --committed` is red at the assembled `HEAD` for a reason this task introduced | OPEN |
| `G8` | the arm `T-340` ships is **not** red on exactly the two records before `T-340`'s repair, or not green after — i.e. the brief's *"arm C"* description matches nothing that can be built | OPEN |
| `G9` | more than **one** constant has to change for the promotion, contra `C-0224` §7 | OPEN |

---

## Execute

`PROSE_ARM_IS_GATED = False → True`, one constant, exactly as `C-0224` §7 promised; the named test
that pinned it is **inverted, not struck**; and the header comment that named `T-339` now records
what the flip waited for. Nothing else in the tool changed for this row.

Two named tests were added, because `C-0177`'s hazard is the one that matters here — **a promoted
gate that cannot fail is worse than a printed one**, and this corpus shipped exactly that for thirty
iterations. They drive a **constructed red fixture** through the gated path: a deliverable quoting a
figure no record pins, and a result file recording a registry quantity at a tree. `_gated_rows`
reproduces `main`'s own composition rather than restating it, so a change to what `--check` gates
cannot pass the test while failing the tool.

## Verify

| | reading |
|---|---|
| `--prose --strict` at `HEAD`, `dfce9c1`, `7ff9d07`, **before** the flip | exit **0** at all three — `G1` fired as declared |
| `--check` at `HEAD` after the flip | exit **0**, reported **GATED** |
| `--check`, `--self-test`, `--prose --strict` in a copy with **no `./.git`** | exit **0**, **0**, **0** |
| the mutation harness, before against after | **43** rows both sides, **0** survivors both sides, **0** rows whose killer count shrank, **0** rows that disappeared |
| `gpd/results/T-336-*.json` | not re-emitted: its `baselineRef` is `52a7bf3` and its counts are pinned there |

Filed as [`C-0226`](../claims/C-0226-a-working-tree-reading-at-the-emitter.md), with `T-340`.
