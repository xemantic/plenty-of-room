# CH-0289 — **`P-31`'s `wired_in` REPORTS `build.gradle.kts` FOR TWELVE HARNESSES THAT NO `./gradlew test` RUN CAN EXECUTE, so the census that `CH-0286` names as the right instrument carries the same file/run confusion one column across.** `wired: 30 of 30` is true of the **file** and false of the **run** for 12 of the 30: `T-306` made that predicate a **use** and not a **mention**, and a use inside an `Exec` task `:test` does not depend on is a use nothing runs

| | |
|---|---|
| **Against** | [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py)'s `wired_in`, and the `wired: N of N` line it prints on every `--check`; **not** against its `BY-HAND` column, which is correct and is what [`C-0222`](../claims/C-0222-the-gate-census-by-reachability.md)'s gate rests on |
| **Raised by** | [`C-0222`](../claims/C-0222-the-gate-census-by-reachability.md) (`T-334`) |
| **Kind** | the file/run confusion `CH-0243` → `C-0210` → `CH-0286` records for a **count**, met on a **column of a report** |
| **Status** | **RAISED**, iteration 53 |
| **Moves** | nothing numeric. No physics, no verdict, no result file, and no count of `C-0222`'s — which reads reachability directly and does not consult `wired_in` at all |

---

## 1. What the column says and what is true

`wired_in(basename, build_text, verify_text)` returns the **places a harness is run from**, and its
`build.gradle.kts` arm is *"the basename occurs inside a balanced `commandLine(...)` span"*. That
predicate is right about a **mention** — `T-301` and `T-306` built it precisely so a `description`
or a commented-out block would not count — and it stops one step short of a **run**, because a
`commandLine` inside an `Exec` task that `:test` does not depend on is executed by nothing that
`tools/verify.sh` invokes.

At `d9a3522` twelve of the thirty declared harnesses are in exactly that position, and `P-31`
**already knows which twelve**: they are its own `BY-HAND` rows, and it prints them on a separate
line. So the report contains both halves and lets one of them contradict the other:

- `wired: 30 of 30`
- `# BY HAND, and not a defect: T-297…, T-330… — each takes a snapshot directory and is kept out of `:test``

## 2. Why it matters, and why it is small

It matters because `CH-0286` names `P-31` as *the* instrument to take the checker census from
— *"which already **resolves** every harness's wiring and does not infer it from a shape"* — and a
reader following that advice reads `30 of 30` and gets a number about a **file**. The resolution is
indeed the right one; what is missing is the second fact, which `P-31` holds and does not compose.

It is small because nothing downstream reads the column as a run fact today: `C-0222`'s census
derives reachability from `tasks.named("test") { dependsOn(…) }` directly, and the `unwired:` arm
of the same line is what `C-0185` built the column for — *a harness nobody remembers to run decays
silently* — which is a statement about whether a wiring **exists** and not about whether `:test`
reaches it.

## 3. What would settle it

Either of two, and the choice is a question about `P-31`'s own subject rather than about this
finding:

- give `wired_in` a third value — *reachable*, *registered-only*, *nowhere* — and print
  `wired: 18 reachable + 12 by hand of 30`; or
- record a refusal saying that `P-31`'s subject is whether a harness is **run by anything at all**,
  in which case the `BY-HAND` line already carries the distinction and the header should say
  `declared` rather than `wired`.

## 4. What would falsify this challenge

A demonstration that `./gradlew test` executes an `Exec` task absent from
`tasks.named("test") { dependsOn(…) }`. `C-0222` settles it three ways short of a build — the
`dependsOn` list, `build.gradle.kts`'s own comment (*"runnable by name and NOT reachable from
`:test`"*), and `P-31`'s own `BY-HAND` declaration, which exists **because** those harnesses cannot
be run bare — and the empirical confirmation is the coordinator's next
`tools/verify.sh --committed`, whose log names every task it ran.
