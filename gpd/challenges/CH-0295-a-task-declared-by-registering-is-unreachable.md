# CH-0295 — **`T-334`'s REACHABILITY PARSER READS ONE OF KOTLIN'S TWO WAYS OF DECLARING A GRADLE TASK, AND THE CORPUS HAPPENS TO USE ONLY THAT ONE — SO A GATE WIRED THE OTHER WAY READS AS *UNREACHABLE FROM `:test`*, WHICH IS THE CENSUS'S OWN NAME FOR A BY-HAND HARNESS.** Written `val x by tasks.registering(Exec::class)` and named in `dependsOn`, a new gate took `armOne` from `13 unreachable = 13 declared BY-HAND` to **`14` against `13`** and `--check` to **1 defect**; rewritten `tasks.register<Exec>("x")`, byte for byte the same wiring, it is clean. **0 of the 61 Exec tasks in `build.gradle.kts` use the first form**, which is why the hole is latent and why writing the 62nd is what found it

**Against** the reachability predicate of [`tools/T-334-gate-census.py`](../../tools/T-334-gate-census.py), filed by [`C-0222`](../claims/C-0222-the-gate-census-by-reachability.md) (`T-334`).
**Not against** any figure it has published: every Exec task in the corpus is declared in the form the parser reads, so `C-0222`'s counts, its `armOne` invariant and `C-0224`'s arm 2 are all correct at every state either has been run at. **The claim is upheld entire.**
**From** [`C-0226`](../claims/C-0226-a-working-tree-reading-at-the-emitter.md) (`T-340`), which hit it while wiring one `Exec` task.
**Kind** — **a parser that supports one declaration form cannot say it supports the language**, and the failure direction is the loud one: a correctly wired gate is reported as a by-hand harness and the census's own invariant fires.

---

## 1. What was measured

| | `val x by tasks.registering(Exec::class)` | `tasks.register<Exec>("x")` |
|---|---|---|
| named in `tasks.named("test") { dependsOn(...) }` | yes | yes |
| `armOne/unreachable` | **14** | **13** |
| `armOne/declaredByHand` | 13 | 13 |
| `tools/T-334-gate-census.py --check` | **1 defect** | **0 defects** |
| distinct tools that can fail a default run | 54 | **55** |

The two forms are interchangeable Kotlin and the wiring they produce is identical. Only the parser tells them apart.

## 2. Why it is latent rather than live

`grep -c 'by tasks.registering' build.gradle.kts` returns **0** and `grep -c 'tasks.register<Exec>'` returns **61**. Every task the census has ever counted is declared the way it reads, so no published figure moves and no gate has ever been miscounted. What the corpus has is a **convention nothing states and nothing enforces** — `CLAUDE.md`'s *a convention is not a mechanism*, on a Kotlin idiom.

## 3. Why the failure direction is worth naming anyway

An unreachable `Exec` task is exactly how `T-334` describes a **by-hand mutation harness**, and its `armOne` invariant is *the Exec tasks unreachable from `:test` EQUAL the harnesses `P-31` declares BY-HAND, in both directions*. So a gate wired in the unread form does not merely go uncounted: it is reported as a harness somebody deliberately kept out of the build, and the invariant that would catch a genuine wiring mistake fires on a correct one. A reader repairing that defect would go looking at `P-31`'s table.

## 4. What would settle it

Either the parser resolves both forms — `val <name> by tasks.registering(<Type>::class)` is one regular expression beside the one it already has — or the single supported form is **declared**, with a named test that refuses the other, so the convention becomes a mechanism. The second is cheaper and is arguably the better answer: one declaration form is easier to read than two.

| | |
|---|---|
| **Status** | **RAISED**, iteration 55 |
| **Raised by** | [`C-0226`](../claims/C-0226-a-working-tree-reading-at-the-emitter.md) (`T-340`) |
| **Moves** | nothing today. **No physics, no verdict, no published number** |
