# T-306 — a mutation harness's OUTPUT FORMAT is an interface a sibling census depends on, and nothing declares it

| | |
|---|---|
| **Raised by** | the coordinator at the assembled `HEAD` of iteration 47, on the **third** instance in two iterations of one class: a harness and a census that reads harnesses, written by different agents, each correct and each green alone |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests and a mutation table over every predicate changed, in both directions |
| **Units** | none; every value below is an integer count, a shape name or a verdict |

## Formulate

[`tools/T-295-mutation-input-census.py`](../../tools/T-295-mutation-input-census.py) runs every mutation harness in `tools/` in two arms
and reads each harness's own **printed per-mutation rows**.
It reads them by trying **eight regular expressions** against every line,
first match wins, and the order of the tuple decides which.
Nothing anywhere declares what a given harness prints.

Three collisions in two iterations, each between a harness and that census, each by a different hand:

1. `T-297`'s harness takes a **snapshot directory** argument — it mutates Kotlin and must not edit a shared checkout — so run bare it prints its usage. A two-state census could only call that a `REFUSAL`, i.e. a defect. Repaired in `T-301` by a **derived** third state.
2. `T-300`'s ninth census family **orphaned a transcribed anchor** in `T-281`'s harness, which its author did not own. Repaired by quoting the block live.
3. `T-298`'s harness printed `killed <label> <detail>` with **no count** — a ninth shape the eight patterns do not carry — so the census refused **by design**. Repaired by moving the **harness**, deliberately: widening the parser to a fourth `killed` variant would have read a countless row as *0 named tests failed*, which is a `SURVIVOR`, the failure direction that flatters.

The queue offers two deliverable shapes and asks for a measurement before the choice.

**The measurement, taken first** (`tools/P-31-harness-census.py --json` for the roster, each harness run once for its output):

| | |
|---|---|
| harnesses declared | 15 |
| harnesses that run bare | 14 (`T-297` prints its usage — the `BY HAND` third state) |
| **distinct printed row shapes** | **6** — `killed-by` (4 harnesses, 71 rows), `killed-pair` (4, 76), `killed-n` (1, 21), `kind-row` (2, 94), `arrow` (2, 19), `of-row` (1, 22) |
| declared row patterns | 8, of which **6 fire today**; `survived` and `survives` fire on 0 rows because the corpus has 0 survivors, so they are contingent rather than dead |
| declared summary patterns | 3, **all three load-bearing** (`# N mutation(s)` on 11 harnesses, `coverage, N mutations` on `T-234`/`T-280`, `N retired` on `test-check-queue-vocabulary`) |
| **harnesses printing NO row count of their own** | **3** — `T-225`, `T-249`, `T-250` |

**The choice: a declared row shape per harness, in `P-31`'s table.** Three grounds, all measured:

- The six shapes are **not gratuitous**. `kind-row` carries `C-0176`'s `NARROW`/`WIDEN` direction and `killed-pair` carries two suites separately; a single required shape is a **ten-harness** output rewrite that would either lose that information or print every row twice.
- The `C-0176` objection — *a declared list is a dated object* — **is already discharged for this particular table.** `P-31.discovers_harnesses` fails the build on an undeclared `tools/` harness, so a harness written tomorrow cannot exist without a row, and the row will now have to name its shape. A declaration in a gated registry is not a census that stopped.
- What is actually missing is not a shape but a **teeth-bearing contract**, and the measurement finds two live holes a declaration alone does not close:
  - **cross-shape acceptance** — the census tries all eight patterns against every harness, so a harness that changes to *another* harness's shape is read silently, with different semantics;
  - **a silent under-count** — at the three harnesses that state no count, `reconcile` cannot see a partial shape change: both arms drop the same rows, the lengths agree, `stated is None`, and no refusal is raised. `reconcile(rows[:-1], rows[:-1], None)` returns no refusal today.

**Numeric target.** Each harness's printed row shape **declared** in `P-31`'s own table and used to parse **only** that harness; the row count made **mandatory**, so no harness can under-count silently; the `BY HAND` third state cross-checked declared-against-derived in both directions; and the two open halves of `T-301` and `T-305` closed on the same table.

**Acceptance predicates, falsifiable.**

- **F1 — every harness declares the shapes it prints, and is parsed with those alone.** A row printed in a shape the harness did not declare is a **refusal** naming the harness, never a silently-read row.
- **F2 — the declaration is checked in BOTH directions.** A declared shape that the harness never prints is a defect (a stale declaration is how a registry rots, `C-0182`); a printed shape that is not declared is a defect too.
- **F3 — a row count is MANDATORY.** A harness that states no count of its own is a refusal, so the silent under-count at `T-225`, `T-249` and `T-250` becomes impossible. The three harnesses gain the `# N mutation(s), M survivor(s)` line eleven others already print.
- **F4 — the `BY HAND` state is declared as well as derived, and disagreement in either direction is a defect.** A harness declared `BY-HAND` that runs is a stale declaration; a harness that prints a usage line and is not declared `BY-HAND` is an undeclared one.
- **F5 — `T-301`: `wired_in` distinguishes a USE from a MENTION.** A basename inside a `commandLine(...)` span of `build.gradle.kts`, or in the command word of a `tools/verify.sh` line, is a use; an occurrence in a comment or in a description string is not. The predicate must move the live reading: `T-283-mutation-test.py` is reported *wired in `tools/verify.sh`* today off a **comment** at line 282.
- **F6 — `T-305`: the Kotlin harness is moved, declared and wired**, and whether its `BY HAND` state composes with `T-295`'s derived one is **measured and stated**, not assumed.
- **F7 — a mutation table over every predicate changed, in both directions, with a subtracted baseline.** Every classification must fail at least one **named** test when changed on its own; the mutation replaces a rule **wholesale**, never as an alternation with the original; and the unmutated copy is run first and its failures subtracted (`CH-0237`).
- **F8 — the gate reads 0 on the tree this lands on.** `tools/P-31-harness-census.py --check` and `tools/T-295-mutation-input-census.py --check` both exit 0, or the residue is named with the reason it cannot come clean.

**What would falsify the approach.**
If the six shapes turned out to be one shape wearing six spellings — if every harness's rows carried the same fields in the same order — then a declaration per harness would be bookkeeping and the single required shape would be right.
The measurement says otherwise: `kind-row` and `killed-pair` carry a field the canonical shape has nowhere to put.
And if making the row count mandatory refused a harness that cannot state one, the count would be the wrong contract; measured, all three that lack it are harnesses whose main loop already knows the number.

## Plan

**Cheap bound first, and it decided the shape of the work.** Running the fifteen harnesses once each and tallying which pattern matched each line costs about two minutes and no code. It returned 6 shapes over 14 harnesses and 3 harnesses with no stated count — which is what turned *"declare the shape"* into *"declare the shape **and** make the count mandatory"*, and what priced the alternative (a ten-harness migration) without attempting it.

**Method.**

1. `P-31.HARNESSES` gains a fifth field: the tuple of row-shape names the harness may print, or `("BY-HAND",)`. `P-31` gates that every declared name is one the census knows — by loading the census at test time rather than copying its names, because a duplicated rule is invisible to a mutation test of either copy.
2. `T-295.parse_rows` takes the declared shapes; `census` passes each harness its own. A line matching an **undeclared** shape is counted and reported, so the refusal names what happened rather than saying *"printed no row"*.
3. `reconcile` refuses on `stated is None`. Three harnesses gain a summary line.
4. The `BY HAND` third state is cross-checked against the declaration in both directions.
5. `wired_in` becomes a use/mention predicate over comment-stripped text, `commandLine(...)` spans and shell command words.
6. `gpd/data/T-299-mutation/mutate.py` moves to `tools/T-299-mutation-test.py`, gains the lowercase `usage:` line the derived third state keys on, is declared with a new adapter shape for its five-field rows, and is wired on a **separate** Gradle task that is **not** in `:test`'s dependency chain — one mutation is one Gradle `test` run, so its fourteen rows are ~15 minutes against the 0.7 s a Python harness takes.
7. A new harness, `tools/T-306-mutation-test.py`, mutates both censuses and runs both self-test suites per mutation, with a subtracted baseline.

**Cost.** Seven files in `tools/`, one in `build.gradle.kts`, no `src/` and no result re-emission. The alternative deliverable — one required row shape — is ten harnesses' output rewritten, every claim that quotes a row invalidated, and two carried fields with nowhere to go.
