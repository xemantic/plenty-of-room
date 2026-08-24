# C-0214 — **THE DYNAMIC ARM IS REFUSED AS A GATE, MEASURED RATHER THAN ASSERTED: OF THE SIXTEEN WRITERS THE STATIC PREDICATE FLAGS AT THE ONE REF WHERE THE DEFECT EXISTS, ONLY `7` EVER WRITE — AND THE SIXTEENTH IS `CH-0268`'s OWN §4b DEFECT, WHICH IS NOT A WRITE AT ALL AND IS THEREFORE OUTSIDE A WRITE-OBSERVING PROBE AT ANY COST.** Both arms re-derived: at `HEAD` = `646b29e` they **agree at zero** (`45 writers, 45 refusing, 0 not`; `0` files written), so the disagreement `T-321` inherited is a reading at the pre-repair `441270c8`, and there it is **`16` against `5`** — a gap of **nine**, not six, because the static half published as `eleven` is **sixteen** ([`CH-0275`](../challenges/CH-0275-the-repair-commit-guards-sixteen.md)). **A checksum is the wrong instrument and `st_mtime_ns` is both cheaper and stronger**: it closes `CH-0268` §3's own blind spot, takes the dynamic reading `5 → 7`, and finds a byte-identical re-emitter **no artifact names** — `tools/T-194-emit-result.py`, rewriting its own committed result file — beside the `tools/T-272-emit-result-inputs.py` that section predicted. The `9` the dynamic arm cannot reach partition exactly: **8** ran and **died before the write** (three causes, all properties of the probe's own tree or of the argument's shape) and **1** exited `0` having run the **wrong branch**. `dynamic ⊆ static` at `7` of `16` with **`0`** writers outside it, so the static predicate has no false negative where one could be found. The probe costs **7.4 s** at a clean tree and **2 m 18 s** at a dirty one — not the row's *"about ten minutes"* — and it is **not deterministic**: one of the tools it runs makes network requests, and its file count reads `10`, `11` here against `CH-0268`'s `3`

| | |
|---|---|
| **Statement** | `tools/cli_guard.py --check`'s **static** predicate is the one to gate on, and the **dynamic** probe is retained as an unwired instrument, run by hand. The refusal rests on four measured grounds, not on cost: the dynamic arm **cannot reach 9 of the 16** defects the static arm flags at the pre-repair ref, and one of those nine is the class `CH-0268` §4b itself calls the argument for the rule; it is a **strict subset** of the static reading (`0` writers outside it), so gating on the static one refuses strictly more; it is **not deterministic**; and it is **destructive by construction** if ever pointed at the checkout, because letting a writer write is its whole method. What the arm buys is not a gate but three things a gate cannot: it **proves** the static predicate conservative at the one state where that is checkable, it **found** a writer nothing had named, and it **measured** its own two instrument floors |
| **Verdict** | **PASS — a recorded refusal, with the dynamic arm built, run at two refs, and retained.** `T-321` offered either shape and the row is explicit that a refusal is a result; this one is measured. Of four declared falsifiers, **three did not fire and one did**, on `T-161`'s file count, and the one that fired is ground 3 of the refusal |
| **Provenance** | [`tools/T-321-dynamic-guard-probe.py`](../../tools/T-321-dynamic-guard-probe.py) (new, **44 self-tests**, unwired by decision) and [`tools/T-321-mutation-test.py`](../../tools/T-321-mutation-test.py) (**21 mutations, 0 survivors**, declared in [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py)'s `HARNESSES` in the same commit; **the Gradle wiring is handed to the coordinator and is NOT applied here** — see §9, where trying to wire it in the one file this task owns failed a named test of `P-31`'s own suite). Two probe runs at each of two refs. `tools/P-31-harness-census.py --check`: **25 harness(es); 458 anchor(s) and 33 symbol(s) into their subjects; 0 unresolved**, **wired: 24 of 25** (§9). `tools/T-295-mutation-input-census.py --check` reads this harness **21 / 21 fixture-backed, 0 corpus-dependent, 0 survivors**. No Kotlin, **no result file** and no re-emission — [`C-0209`](C-0209-a-link-target-is-a-filename-whatever-it-names.md)'s precedent for a tools-only process claim, and the reason is [`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md): a result file whose subject is the corpus is re-run into oblivion, and every number here is one command away from being re-derived |
| **Conditions** | The corpus at `HEAD` = `646b29e77d9d3fde6f3f0ac949e51e8f43a7453f` and at `441270c8cbd9b254d616d33ae7eacdada09921c3`, the parent of the repair commit `cfbeff3`. The predicate is **today's** `cli_guard.census()` in both readings; only the premise set moves. One box, one filesystem (`ext2/ext3`), one Python (`3.12`). `tools/`, `gpd/` and no other tree |
| **Consumes** | [`CH-0268`](../challenges/CH-0268-an-emitter-that-ignores-its-arguments-emits.md) (the static gate, its §3 blind spot and its §4b second defect), [`C-0210`](C-0210-fourteenth-answers-synthesis.md) (which opened the row), [`C-0195`](C-0195-the-discriminating-input.md) (*a census must delegate what its subject is*; *a fixture layout is a dependency declaration*), [`C-0185`](C-0185-orphaned-mutation-anchors.md)/[`CH-0237`](../challenges/CH-0237-a-mutation-harness-layout-is-a-premise-of-its-own-measurement.md) (the subtracted baseline, the asserted anchor count), [`C-0206`](C-0206-a-harness-output-format-is-an-interface.md) (the declared row shape), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (a mutation must replace a rule wholesale, in both directions), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (*a mutation that fails nothing is the finding — construct the state*), [`C-0179`](C-0179-the-debt-line-as-a-ratio.md) (*a test can be satisfied by the name of the thing it protects*), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*) |
| **Constrains** | **`T-321` is DONE.** Raises [`CH-0275`](../challenges/CH-0275-the-repair-commit-guards-sixteen.md) against `CH-0268`'s and `C-0210`'s **eleven**. Nothing physical, no window edge, no design |
| **Raises** | [`CH-0275`](../challenges/CH-0275-the-repair-commit-guards-sixteen.md) |

---

## 1. Both arms, re-derived, at both refs

Nothing below is inherited.
The predicate is `cli_guard.census()` as it stands today, applied to each ref's own tree, which is the only way to read a repaired defect at the state where it existed.

| | at `HEAD` = `646b29e` | at `441270c8` (pre-repair) |
|---|---|---|
| writers in the population | **45** | **44** |
| **static**: do not parse or refuse | **0** | **16** |
| **dynamic**, checksum observation | **0** | **5** |
| **dynamic**, any observation | **0** | **7** |
| wrote nothing because the run failed first | **0** | **8** |
| wall clock, one run | **7.4 s** | **2 m 18 s** |

**At `HEAD` the two arms agree, and that is the expected result rather than a finding** — the defect is repaired, so an instrument that reads a write has nothing to read.
The disagreement `T-321` was opened on is a property of the pre-repair ref, and re-derived there it is **16 against 5**, a gap of **nine**.
The published *"eleven against five"* is refuted by the repair commit's own `--stat`; that is `CH-0275` and it is not re-argued here.

## 2. The five reproduce by name, and the mtime observation adds two

`CH-0268`'s five are reproduced **exactly**, at the same ref, by a probe written independently of it:

| tool | what `--help` did | seen by |
|---|---|---|
| `tools/T-161-fetch-sources.py` | **created** `./--help/` and filled it with query JSONs | checksum |
| `tools/T-183-emit-result.py` | **content** — `gpd/results/T-183-challenge-status-self-consistency.json` | checksum |
| `tools/T-200-emit-result.py` | **content** — `gpd/results/T-200-reemission-order.json` | checksum |
| `tools/T-205-emit-result.py` | **content** — `gpd/results/T-205-four-layer-supersession.json` | checksum |
| `tools/T-234-emit-classification.py` | **content** — `tools/T-234-classification.json` | checksum |
| `tools/T-272-emit-result-inputs.py` | **touched** — `src/main/kotlin/structure/ResultInputs.kt`, byte for byte | **`st_mtime_ns` only** |
| `tools/T-194-emit-result.py` | **touched** — `gpd/results/T-194-one-reserve.json`, byte for byte | **`st_mtime_ns` only** |

`CH-0268` §3 predicts the sixth **by name** and says the probe cannot see it.
It does not predict the seventh, and no artifact of this repository names it: `tools/T-194-emit-result.py` rewrites its own committed result file on `--help`, identically, and every checksum comparison ever taken of that state reports nothing.

**So the blind spot was real, it was larger than the section that named it, and closing it costs less than the checksum does.**
The checksum arm needs a *second* full hash and a tree restore to recover the before-bytes; the mtime arm is the `stat` walk the probe has to make anyway in order to know what is worth hashing.

### 2b. The observer's own footprint is five times the signal, and it has to be named

CPython byte-compiles every module it imports.
Run without excluding `__pycache__`, the same probe at the same ref reads **24 of 44** writers writing — every extra one of them a tool that merely *imported a sibling*.
Filtered, it reads **5**, which is what `CH-0268` reports, so that challenge's probe evidently filtered it too and did not say so.

A dynamic probe therefore **over-reports by about 5× before it under-reports**, and the correction is an exclusion by name that is a statement about the *instrument*.
It is declared as `IGNORED` and held open by four named tests, including the one that refuses a substring match.

## 3. The nine the dynamic arm cannot reach, partitioned

Every one of the sixteen the static arm flags is accounted for, and the dynamic reading is a **strict subset**:

| | count | why the dynamic arm is silent |
|---|---|---|
| wrote, checksum-visible | **5** | — |
| wrote, `st_mtime_ns` only | **2** | invisible to a checksum; **visible here** |
| **died before the write** | **8** | exit `1`; see below |
| **exited `0` having written nothing** | **1** | `tools/T-278-emitter-rounding-census.py` — it ran the **wrong branch** |
| **total** | **16** | and **0** writers the static arm did not flag |

The eight deaths have three causes and **none of them is a property of the tool**:

- a `git archive` tree carries **no `.git`**, so `T-201-emit-result.py` dies on `git show 9ed4fdc:ANSWERS.md` returning 128;
- `T-212-emit-result.py` takes `argv[1], argv[2]` and consumed `--help` as its *first* positional, then wanted a second — with two arguments it would have written;
- `T-234-emit-result.py` had an unrelated stale API against a sibling at that ref.

Change the probe's tree, or the argument, and this number moves.
**A reading that depends on the probe's own environment is a reading, not a gate.**

**The sixteenth is the decisive one.**
`tools/T-278-emitter-rounding-census.py` handed `--help` wrote nothing and exited `0` —
and that tool is `CH-0268` §4b's own second defect, the one where a wired invocation passes `--self-test` to a tool dispatching on `--selftest`, so the task *"ran nothing and was green"*.
That defect is **not a write**, so no observation of the tree — checksum, `st_mtime_ns` or syscall — can see it at any cost.
`CH-0268` §4b calls it *"the argument for refusing an unrecognised argument even where the tool does not write"*, and it is exactly the half a write-observing probe is structurally blind to.

## 4. The instrument's own two floors, measured

**The mtime floor is real and it is 1 ms.**
Over 20 000 rewrites of one file, `/tmp` (`ext2/ext3`) yields **7 535 distinct timestamps** and a smallest positive step of **1 000 006 ns**.
So two writes inside one tick share a timestamp and the mtime observation misses them — the instrument is **not exact**, and a named test asserts that in the *failing* direction as well as the passing one.
It was found the honest way: the first version of that test passed on one run and failed on the next with nothing changed, because whether any one pair straddles a tick is a coin toss.

The floor does not bite here, and the margin is measured rather than assumed: the smallest interval this probe ever has to resolve is one interpreter startup, and the fastest probed writer measured here takes **32–36 ms** — **32×** the tick at its fastest.

**The write-syscall observation is exact, has no floor, and costs 2.05×.**
`strace -f -e trace=openat,creat,rename,renameat2,unlink,unlinkat,mkdir` on the byte-identical re-emitter shows the intent directly:

```
openat(AT_FDCWD, ".../src/main/kotlin/structure/ResultInputs.kt", O_WRONLY|O_CREAT|O_TRUNC|O_CLOEXEC, 0666) = 3
```

Timed over the six writers that matter, `2.649 s` untraced against `5.420 s` traced — **2.05×** in aggregate, `1.30–5.42×` per tool.

**So the answer to the queue row's question is: `st_mtime_ns` closes the blind spot and is CHEAPER than the checksum, and a syscall observation closes it exactly for about twice the run time and an `strace` dependency.**
The syscall arm is what to reach for the moment the population stops being pure Python or starts spawning children, because a checksum and an mtime observe the **tree** while `strace` observes the **intent**; it is not needed here, because on the one tool where the two were compared the mtime arm already found everything the syscall arm found.

## 5. The refusal, and the four grounds

**`tools/cli_guard.py --check` stays the gate.  The probe is retained, run by hand, and only its TESTS are wired.**

1. **It cannot reach 9 of 16, and one of those nine is the class the rule exists for.** §3.
2. **It is a strict subset of the static reading, with `0` writers outside it.** So at the one ref where the question is answerable the static predicate has **no false negative**, and gating on it refuses strictly more than gating on the probe would. `C-0083`'s *a gate that cannot come clean is not a gate* has a converse used here: a gate that comes clean must be the **conservative** one.
3. **It is not deterministic.** `tools/T-161-fetch-sources.py` makes network requests; its created-file count is **10** in one run and **11** in the next, against `CH-0268`'s **3**. The *classification* is reproducible — two runs at each ref agree on every tool, every class and every count except that one — and a gate cannot be built on a number a remote service moves.
4. **It is destructive by construction.** Letting a writer write is the method. It runs only inside a throwaway `git archive` tree and never touches the checkout, and a gate that must never be pointed at the tree it gates carries a foot-gun a build should not hold.

**What the arm bought, which is why it was built rather than argued:** it is the only thing that can say ground 2 at all; it found `T-194`, which nothing named; it measured the `__pycache__` noise floor and the 1 ms mtime floor; and re-running it is what showed `T-321`'s inherited premise to be dated.

## 6. Falsifiers declared in `T-321`, and their outcomes

| | declared | outcome |
|---|---|---|
| **F1** | the dynamic arm finds a writer the static arm does not — the static predicate is then not conservative and must not be the gate | **did not fire.** `dynamic ⊆ static`, `0` outside, at both refs |
| **F2** | the mtime observation finds nothing the checksum found — the third observation is then dead weight | **did not fire.** It found **2**, one of them named nowhere |
| **F3** | the probe's reading is not reproducible across two runs at one ref | **FIRED, on one field.** The classification is identical across two runs at each ref; `T-161`'s created count is `10` / `11`. Recorded as ground 3 of the refusal rather than repaired |
| **F4** | the probe costs what the row says — ten minutes makes the refusal easy and cheapness forces it onto soundness | **did not fire.** `7.4 s` / `2 m 18 s`, of which `120 s` is one network timeout. The refusal therefore rests on soundness, which is where F4 was written to push it |

## 7. Verification gates

| gate | reading |
|---|---|
| dimensional consistency | not applicable — every quantity here is a count, a path, an exit code, a second or a nanosecond, and the units travel in the names |
| limiting cases | the `HEAD` arm **is** the limiting case: a tree with the defect repaired must read `0` under every observation, and it does, twice |
| symmetry / conservation | the sixteen partition **exactly** into `5 + 2 + 8 + 1`, and the dynamic set is a subset of the static one with an empty residue |
| numerical convergence | two runs at each ref; identical on every tool, class and count but one, which is §5 ground 3 |
| literature cross-check | `CH-0268`'s own five, reproduced **by name** at the same ref by an independently written probe; and its §3 prediction that `T-272` is invisible to a checksum, confirmed and extended |

`tools/T-321-dynamic-guard-probe.py --self-test`: **44 self-test(s), 0 failure(s)**, three runs.
`tools/T-321-mutation-test.py`: **21 mutation(s), 0 survivor(s)**, on a subtracted green baseline.

**The mutation table's first run read `3 survivors`, and all three were test defects rather than missing rows** — which is what the table is for:

- a **defensive clause with no reachable state**: `failed_before_writing` began `record["timedOut"] or record["exitCode"] not in NOT_A_FAILURE`, and a timed-out run has `exitCode is None`, which is already outside `NOT_A_FAILURE`. The disjunct was **dead**, no fixture could reach it, and a duplicated rule is invisible to a mutation of either copy. It is removed and the invariant is asserted directly;
- a **crash read as a survivor**: silencing the granularity measurement made the self-test suite raise rather than fail a named test, and `C-0206`'s rule is that a suite that does not finish is a **survivor**. The fallback that converts it into a named failure is now its own function with tests in both directions;
- a **test satisfied by a mention**: *"this probe refuses an argument it does not recognise"* was written as `"refuse_unknown_arguments" in <own source>`, and deleting the **call** left the name in the comments above it. It is now behavioural — the probe is run with `--nonsense` and must exit `2`, print to stderr and emit nothing. `C-0179`'s *a test can be satisfied by the name of the thing it protects*, in the one file whose subject is that trap.

## 8. Validity range, and what this claim does not say

- **One box, one filesystem, one Python.** The 1 ms tick, the 32 ms startup and the 2.05× `strace` multiplier are properties of this machine. The *ordering* — tick ≪ startup — is what the refusal uses, and it is unlikely to invert; the numbers are not portable.
- **One argument.** Everything is measured at `--help`. A different unrecognised argument moves the *died-before-the-write* count (`T-212` is the worked example) and cannot move the static reading at all, which is part of why the static one is the gate.
- **The population is `cli_guard.writers()`** — `tools/*.py` matching `(emit\|fetch)`, excluding `test-` fixtures. A tool that writes and is named neither is outside **both** arms, and neither this claim nor the gate says anything about it.
- **This is not a claim that `CH-0268`'s repair was insufficient.** It is the opposite: the repair reached sixteen writers, the tree reads clean under an observation strictly stronger than the one that found the defect, and the only thing wrong with the challenge is a number.
- **Nothing here is empirical about the device.** No physics, no result file, no window edge, no re-emission.

## 9. The harness is declared and NOT wired, and the reason is a named test of somebody else's suite

`tools/T-321-mutation-test.py` is declared in `tools/P-31-harness-census.py`'s `HARNESSES` table in the same commit that adds it, which is `C-0185`'s rule.
It is **not** wired, so `P-31 --check` reads **`wired: 24 of 25`**, and that is a debt this claim states rather than hides.

Every Python-subject harness in this repository is wired as an `Exec` task in `build.gradle.kts` and hung off `:test`.
`T-321` does not own that file, so the wiring was attempted in the one file it does own — `tools/verify.sh`, where the corpus gates live — **and `tools/T-306-mutation-test.py` went red on its baseline**:

```
BASELINE IS NOT GREEN -- nothing below is a measurement
   baseline failure: P-31 every declared harness is executable, because the build execs its path directly
   baseline failure: P-31 in THIS tree no mutation harness is run from tools/verify.sh, though one is
                     named in a comment there -- which the old substring predicate read as wiring
```

The first is a real omission and is repaired (`chmod +x`).
The second is not repairable from here: it is a **fixture pinned on committed corpus state** —
`T-306` made `P-31`'s `wired_in` a *use* rather than a *mention*, and it demonstrates the repair on the real tree by asserting that the tree contains a verify.sh **comment** naming a harness and no verify.sh **invocation** of one.
Wiring anything there falsifies that test's premise, and silently invalidating another task's fixture to buy a wiring is the wrong trade.

So the wiring is reverted and the exact `build.gradle.kts` hunk is handed over with this claim.
`C-0195`'s *a mutation killed only by a test that reads a mutable artifact is dated by that artifact* — met from the far side, as a **constraint on what a later task may change** rather than as an expiry.
It is also the second instance in three iterations of two pieces of correct work colliding only at the assembled `HEAD`, which is what the final pre-push run is for.
