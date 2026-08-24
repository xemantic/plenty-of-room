# CH-0268 — **`CLAUDE.md` RECORDS THIS TRAP TWICE, `T-272` REPAIRED IT AT TWO CALL SITES WITH THE REASON WRITTEN BESIDE THEM, AND ELEVEN OTHER WRITERS NEVER GOT IT: HANDED `--help`, FIVE TOOLS OF THIS REPOSITORY OVERWRITE FOUR TRACKED ARTIFACTS AND BUILD A THREE-FILE SHADOW CORPUS IN `./--help/`.** Measured by running every `tools/*.py` with `--help` inside a `git archive HEAD` tree and diffing: `T-183-emit-result.py`, `T-200-emit-result.py` and `T-205-emit-result.py` rewrite their own committed result files, `T-234-emit-classification.py` rewrites `tools/T-234-classification.json` — the classification registry a wired census reads — and `T-161-fetch-sources.py` creates `./--help/` and fills it with three query JSONs. **And the probe UNDER-reports**: it compares checksums, so a tool that re-emits its own file byte-for-byte is invisible to it. Statically, **eleven** writers ignored `sys.argv` or fell through to a write, `tools/T-272-emit-result-inputs.py` among them — which on `--help` **rewrites a Kotlin main source**, `structure/ResultInputs.kt`

| | |
|---|---|
| **Status** | **RAISED and REPAIRED in the same iteration, and it found a SECOND instance in a WIRED gate (§4b)** ([`C-0210`](../claims/C-0210-fourteenth-answers-synthesis.md), `T-319`). All eleven are guarded through a new shared [`tools/cli_guard.py`](../../tools/cli_guard.py) (**18 self-tests**); the dynamic probe re-run on the repaired tree writes **0** files, and `tools/cli_guard.py --check` reads **45 writers, 45 refusing, 0 not** and is wired |
| **Against** | no standing numeric claim — this is against a **rule** `CLAUDE.md` already states (*"Parse the flag or refuse the argument"*) and against the assumption that stating it repaired it |
| **From** | [`T-319`](../tasks/T-319-fourteenth-answers-synthesis.md), which hit it live: a `--help` typed at `tools/T-234-emit-classification.py`, to find out what the tool does, regenerated the classification registry — 248 insertions and 177 deletions — inside a pass whose whole discipline is not to touch it |
| **Kind** | **a cure is a property of a CALL SITE, not of a repository**, and the second half is `C-0083`'s: a rule with no mechanism is a sentence |

---

## 1. What the rule already said, and where it already lived

`CLAUDE.md`, verbatim:

> **`--help` IS WHAT A COLD SESSION TYPES FIRST, AND AN EMITTER THAT TREATS AN UNRECOGNISED
> ARGUMENT AS DATA WILL EMIT.** One emitter built a **151-file shadow corpus in `./--help/`** —
> `P-28`'s `./--check/` reproduced exactly — and another re-emitted its own audit file.
> **Parse the flag or refuse the argument**; a positional output path is a loaded gun in a
> directory a census walks.

And the cure is **already in the tree, twice**. `tools/T-249-emit-result.py` and
`tools/T-250-emit-result.py` each carry a hand-rolled refusal with the finding written above it —
*"An OPTION IS NOT A DIRECTORY"*, *"AN UNRECOGNISED OPTION MUST NOT EMIT (`T-272`)"*.

**Neither the entry nor the two repairs reached the other eleven writers.** That is `CLAUDE.md`'s own
*a cure is a property of a call site, not of a repository — grep for the call sites, not for the fix*,
on the very entry that records the trap.

## 2. The measurement

Every `tools/*.py` run with `--help` inside a `git archive HEAD` tree, checksummed before and after:

| tool | what `--help` wrote |
|---|---|
| `tools/T-161-fetch-sources.py` | **`./--help/`**, three query JSONs — `P-28`'s `./--check/` reproduced |
| `tools/T-183-emit-result.py` | `gpd/results/T-183-challenge-status-self-consistency.json` |
| `tools/T-200-emit-result.py` | `gpd/results/T-200-reemission-order.json` |
| `tools/T-205-emit-result.py` | `gpd/results/T-205-four-layer-supersession.json` |
| `tools/T-234-emit-classification.py` | `tools/T-234-classification.json` — **read by a wired census** |

**Four tracked artifacts and one shadow directory.** Three of the four are committed *result files*,
which `gpd/README.md` says are reproducible from themselves alone — and which `CH-0246` says must not
be re-run as a control, because the re-run **overwrites the record instead of checking it**. Here the
command that does it is the one a reader types to ask what the command does.

## 3. Why the probe is a LOWER bound, and the static predicate is the gated one

The probe compares **checksums**, so a tool whose re-emission reproduces its own file byte-for-byte
changes nothing and is invisible. `tools/T-272-emit-result-inputs.py` is exactly that case: on
`--help` it matches neither `--selftest` nor `--check`, falls through, and **writes
`structure/ResultInputs.kt`** — a Kotlin main source — with content identical to what is there, so
the probe reports nothing.

Statically, a writer that cannot refuse is one that uses no `argparse`, calls no shared guard, and
carries no hand-rolled refusal. **Eleven**, against the probe's five.

## 4. What was done

- `tools/cli_guard.py`, with `refuse_unknown_arguments(usage, recognised, allow_positional)` and
  **18 self-tests**, including both directions of the classification: a writer that names `unknown`
  but never exits `2` does **not** count as refusing, and one that exits `2` but never names
  `unknown` does not either.
- All eleven writers guarded. Nine had ignored `sys.argv` entirely; two matched flags with `in argv`
  and fell through.
- `tools/cli_guard.py --check` — **45 writers in `tools/`, 45 refusing, 0 not** — wired as a gate.
- The dynamic probe re-run on the repaired tree: **0 files written**.

## 4b. And the guard found a SECOND instance of the same class, in a WIRED gate

The repair went red on the authoritative `tools/verify.sh` run, at `:testEmitterRounding`, and the
reason is the defect one level out.

`build.gradle.kts` and `tools/verify.sh` both invoke
`tools/T-278-emitter-rounding-census.py` with **`--self-test`**; the tool dispatches on
**`--selftest`**. Before the guard it matched neither branch, printed nothing and **returned 0** — so
for as long as both wirings have existed, that *"self-test"* task has run **nothing** and been green.
Its sibling `tools/T-278-rounding-simulation.py` is wired the same way and dispatches the same way,
and there the unrecognised flag was worse than inert: it fell through to the **full census**, which
also exits 0, so the task ran the wrong thing and passed.

Censused over all **69** wired invocations in the two files, a flag a wired call passes that the
tool's own source never mentions occurs **3 times, over 2 tools**, both of the `T-278` family. Both
now accept either spelling, and the wired tasks run what their names say.

**This is `CLAUDE.md`'s own *a gate can be wired and still be unable to fail*, found by a guard
written for a different reason** — and it is the argument for refusing an unrecognised argument even
where the tool does not write: an ignored flag is a silent change of what the command does.

## 5. What this challenge does NOT claim

**No number of any claim moves**, and none of the four overwritten artifacts was *found* stale — three
of them are re-emissions of themselves. The damage is a hazard rather than a recorded loss, and the
one recorded loss is this task's own: the accidental regeneration of `tools/T-234-classification.json`,
which was reverted from `HEAD` before anything was measured on it.

**And the gate is a proxy.** It reads a source for a refusal; the probe runs the tool. The proxy is
what a build can afford and the probe is what says the proxy is right — both readings are recorded
here, and they disagree by six, in the direction that says the static one is the safe one to gate on.
