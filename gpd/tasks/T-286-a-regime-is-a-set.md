# T-286 — a regime is a SET of solved states, and the two absences are different values

| | |
|---|---|
| **Leaf** | `A8.2` — the design question inside step 6 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | [`CH-0224`](../challenges/CH-0224-a-regime-cannot-name-a-swept-buffer.md), filed by [`C-0172`](../claims/C-0172-typed-handles-and-the-emission-header.md) (`T-272`) on the first attempt to put the regime block on every record |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The question, and what it is NOT

`CH-0224` is a **design** challenge, not a sweep.
It says that [`environment/Regime`](../../src/main/kotlin/environment/Regime.kt) has exactly two states — one molarity or none —
and that the corpus is made of the two shapes it does not admit: a sweep over buffers, and a point in `(buffer, gap, bias)`.
The consequence it measures is that the emitted block is `null` on exactly the results a `P4` gate exists to refuse.

**The sweep is [`T-272`](T-272-emission-layer-remainder.md)**, whose row prices one pass at ≥ 7 h.
This task answers the design question and lands the type change that follows from it,
demonstrated on a **small named set** of studies, and it deliberately re-emits nothing else.

### The numeric target

| | |
|---|---|
| **today** | the number of committed result files whose `emission.regime` is non-`null` |
| **the target** | that number, and the *reason* it is what it is, stated as a census the claim can be checked against rather than as an opinion |

`CH-0224` asserts *"the key is emitted on every record and is `null` on all of them"*.
That is a count over `gpd/results/`, and it is the cheap bound.

### Acceptance predicates

| | predicate |
|---|---|
| **F1** | **the census is measured before the design is chosen** — how many committed files carry an emission header, how many carry a non-`null` regime, how many studies have an environment coordinate at all, how many of those sweep the buffer, and how many result-file **read edges** land on a swept file. A design chosen before that census is a preference |
| **F2** | **the two absences are different values in the emitted JSON**, per `CLAUDE.md`'s *a `null` that means "no requirement" and a `null` that means "not stated" are different values* — and the physical claim `Regime`'s own KDoc documents (a **stated** regime whose buffer is `null`) remains a third, distinct value |
| **F3** | **the type change moves no committed byte on any study that does not adopt it.** Asserted of the sources — every existing call site passes `null` and `null` must still serialise as JSON `null` — and then *measured* by re-running named studies, not argued |
| **F4** | **the demonstration covers all three states on real studies**: a study that sweeps the buffer, a study that fixes one, and a study whose `null` buffer is the documented physical claim. Each is named, re-run, and its movement reported against `git show HEAD:<path>` |
| **F5** | **the consumer side is measured and the answer is allowed to be "it cannot be a gate"**. How many read edges a file-granular regime gate can refuse, and — separately — how many it *cannot*, with the residue named |
| **F6** | any new schema key **censused over the committed corpus before the first line of the emitter** (`CLAUDE.md`; it cost `T-272` a whole sweep once) |

### What would falsify the approach

Three things, declared before the work:

1. **A single widened `Regime` is enough.** If the buffer can be widened to a set *inside* `Regime` without the height and bias intervals becoming a union that admits a `(buffer, height)` pair no record was solved at, then `CH-0224`'s repair 2 is the cheap one and a second type is waste.
2. **The corpus does not sweep.** If the studies that sweep the buffer are a minority of the electrolyte studies, or if the read edges into their files are a minority of the corpus's, then the block as designed is nearly right and the residue is a handful of rows.
3. **`null` is already unambiguous.** If no committed study would ever emit a *stated* regime carrying a `null` buffer, then the JSON `null` has only two meanings, not three, and a two-valued repair suffices.

### Units and conventions

Unchanged and locked. 300 K, `k_BT = 4.142 pN·nm`; lengths nm, forces pN, molarities mM.
**No physics changes here.** Every field this task can move is a schema field; a number that moves for any other reason is the finding.

---

## 2. Plan

### The cheap bound runs first, and it is a census with no JVM

Three counts, all from the committed tree, none needing a solve:

* `gpd/results/*.json` — files, files carrying `emission`, files whose `emission.regime` is non-`null`;
* `src/main/kotlin/**/*Study.kt` — studies naming `MagnesiumChlorideBuffer`, split into *sweeps a list* and *fixes one*;
* `tools/result-reader-census.py` — read edges landing on the files those studies write.

The third is the one that decides `F5`, and it is the one nobody has taken:
a gate's value is a property of its **consumers**, and the reader graph is already derived.

### Why the census must be read and not regexed

A mechanical scan for `listOf(0.5, 2.0, …)` inside an electrolyte study is a **role** inference from a **type**,
which is `CLAUDE.md`'s own standing trap:
`anchoring/GoldElectrodePzcStudy` carries `listOf(2.0, 10.0)` and those are `C-0021`'s two readings of §3's **tile thickness in nm**.
So the classification is read per study, the *count* of studies naming the buffer is asserted mechanically,
and the evidence line for each classification is recorded so it cannot go stale silently.

### The method, and its justification against cost

The three repairs `CH-0224` §4 prices are a per-record regime, a set-valued buffer inside `Regime`, and a family beside it.
The first is `T-272`'s sweep with a wider edit; the second and third are one type each.

This task takes the **arity** change and defends it against the other two by measurement rather than by taste,
because the decisive evidence is already in the corpus:
`actuator/TallGapDeviceBStudy` solves one buffer list over its tall heights and a **different** buffer list over its fold heights,
so a single `Regime` with a set-valued buffer would have to carry the **union** of both height ranges
and would then admit a `(buffer, height)` pair no record of that file was solved at.
That is a counterexample, it costs one `grep`, and it runs before any code.

### What is deliberately not done

* **No corpus sweep.** 132 call sites pass `null` today and they keep passing `null`; their meaning changes in the KDoc and in the census, not in the JSON.
* **`Regime`'s refusal predicate is not touched.** `reasonToRefuse` compares buffers by equality and that is `C-0159`'s claim; this task changes how many regimes a *file* may state, not what one regime refuses.
* **No claim's number is amended**, because none moves.
