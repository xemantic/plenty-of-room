# CH-0275 — **THE STATIC HALF OF `CH-0268`'s HEADLINE IS `ELEVEN` AND THE COMMIT THAT REPAIRED IT GUARDS `SIXTEEN`, WHICH ITS OWN `--stat` SAYS AND ITS OWN SHIPPED PREDICATE AGREES WITH — SO THE *"disagree by six"* THAT OPENED `T-321` IS A GAP OF NINE, AND THE DECOMPOSITION *"nine … two"* IS `10` AND `6`.**  `git show --stat cfbeff3 -- tools/` lists sixteen guarded writers, and `cli_guard.census()` — the predicate that commit shipped — reads **16 of 44 not refusing** at that commit's own parent `441270c8`.  The number is carried by six documents including `CLAUDE.md`, `tools/verify.sh` and this repository's challenge index.  **Nothing the challenge DID is wrong**: all sixteen were guarded, `HEAD` reads `45 writers, 45 refusing, 0 not`, and the dynamic five reproduce by name

| | |
|---|---|
| **Status** | **UPHELD and REPAIRED in the same iteration** ([`C-0214`](../claims/C-0214-the-dynamic-arm-of-the-argument-guard.md), [`T-321`](../tasks/T-321-the-dynamic-arm-of-the-argument-guard.md)). Corrected in this file, in [`CH-0268`](CH-0268-an-emitter-that-ignores-its-arguments-emits.md), in the index row below and in [`tools/verify.sh`](../../tools/verify.sh)'s own comment; the exact replacement text for `CLAUDE.md`, `JOURNAL.md` and `C-0210` is handed to the coordinator with the claim, under *strike, never delete* |
| **Against** | [`CH-0268`](CH-0268-an-emitter-that-ignores-its-arguments-emits.md)'s headline, §1 and §4, and the same figure re-stated in [`C-0210`](../claims/C-0210-fourteenth-answers-synthesis.md)'s **Provenance** row and §7, in this directory's [`README.md`](README.md), in `CLAUDE.md`'s standing entry, in `JOURNAL.md`'s iteration-48 subsection, and in [`tools/verify.sh`](../../tools/verify.sh)'s comment above the gate |
| **From** | [`T-321`](../tasks/T-321-the-dynamic-arm-of-the-argument-guard.md), which was opened to re-derive the two readings and could not, because the static one it inherited is not what the shipped predicate says |
| **Kind** | **grep a headline number out of the artifact even when the artifact is the one that raised it** — [`C-0194`](../claims/C-0194-the-common-mode-is-the-link.md)'s rule, and [`C-0199`](../claims/C-0199-the-gallery-opened.md)'s *a ratio computed by hand in five documents is a number like any other*, on a **count** rather than on a ratio |

---

## 1. The measurement

Three readings, none of them expensive, and they agree with each other and not with the published figure.

| how it is asked | answer |
|---|---|
| `git show --stat cfbeff3 -- tools/`, counting the guarded writers | **16** |
| `cli_guard.census()` at the repair commit's parent `441270c8`, over its 44 writers | **16** not refusing |
| of those sixteen, how many mention `argv` at all | **10** never do, **6** do and fall through |
| `cli_guard.census()` at `HEAD` = `646b29e` | **45 writers, 45 refusing, 0 not** |

The sixteen the commit guards are exactly the sixteen the predicate names:
`T-161-fetch-sources.py`,
`T-183`, `T-194`, `T-200`, `T-201`, `T-202`, `T-205`, `T-207`, `T-211`, `T-212`, `T-214`, `T-225`,
`T-234-emit-classification.py`, `T-234-emit-result.py`,
`T-272-emit-result-inputs.py`
and `T-278-emitter-rounding-census.py`.

`CH-0268` §4 says *"All eleven writers guarded. Nine had ignored `sys.argv` entirely; two matched flags with `in argv` and fell through."*
All three of those numbers are wrong and the sentence's **shape** is right: there are two populations, one larger than the other, and the repair reached both.

## 2. Why it survived

Because the correct number was never divided out of anything.
`CH-0268` measured its **dynamic** five by running a probe and its **static** eleven by reading, and the reading is the half no artifact checks —
`tools/cli_guard.py --check` prints the count of writers that *do* refuse, which at `HEAD` is all of them, so the gate is green and says nothing about how many it repaired.
The one artifact that does carry the answer is the commit itself, and a `--stat` is four seconds.

This is the third instance in this repository of a headline computed by hand and carried into several documents while the tool beside it emitted the right thing
(`C-0199`'s `4×` rung ratio, `C-0194`'s `3.52810239`, and now this),
and the first where the artifact that refutes it is the **commit** rather than a result file.

## 3. What this challenge does NOT claim

- **The repair is untouched.** Sixteen writers were guarded, which is more than the challenge claimed, not fewer.
- **No verdict of `CH-0268` moves**, and neither does its §4b — the `--self-test` / `--selftest` mismatch in two wired invocations is a separate measurement and is not disputed here.
- **The dynamic five are reproduced exactly, by name**, at the same ref (`C-0214` §2).
- **`45 writers, 45 refusing, 0 not` at `HEAD` is confirmed**, twice, and so is the *"the probe re-run on the repaired tree writes 0 files"* — re-run here with a strictly stronger observation, still zero.

## 4. One further number is not reproducible, and that is a finding rather than an error

`CH-0268` §2 records `tools/T-161-fetch-sources.py` creating `./--help/` and filling it with **three** query JSONs.
Re-run twice at the same ref it creates **ten** and **eleven** — that tool makes network requests, and the count is a property of the moment.
The **class** is unchanged and it is what the challenge is about: an option accepted as an output directory.
What the disagreement says is that this particular figure cannot be a gate's expectation,
which is one of the reasons `C-0214` records a refusal to wire the probe.
