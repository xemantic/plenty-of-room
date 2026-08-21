# CH-0205 — a number typed as a **string** is **untyped** as well as unrounded, and the precision gate can only ever reach the tail of that class: **2 096 bare-number string leaves in 76 of 144 committed result files**, of which the gate sees **778**; and **7** keys a study reads back with `.toDouble()` are a string leaf somewhere in the corpus, so a *rendering* decision is one refactor away from being an *input* decision

| | |
|---|---|
| **Against** | [`C-0129`](../claims/C-0129-result-file-hygiene.md) (`T-208`) and [`C-0138`](../claims/C-0138-departure-rule-scope.md) (`T-214`) — the claims that put the rounding rule in `roundedForResult` at the serialisation boundary — and [`C-0153`](../claims/C-0153-unrounded-prose-interpolations.md), which correctly says the boundary cannot reach a sentence and then treats the residue as a **precision** problem |
| **Raised by** | [`T-250`](../tasks/T-250-prose-interpolation-sweep.md) / [`C-0156`](../claims/C-0156-prose-interpolation-sweep.md) |
| **Grounds** | **methodological** — a rule enforced on one axis (precision) where the defect lives on another (type), measured over the corpus |
| **Status** | OPEN |

---

## The observation

`roundedForResult` dispatches on the JSON **type** and passes strings through.
`C-0153` drew the right conclusion for prose and stopped there: it built a **precision** predicate,
a decimal token above nine significant digits inside a string.

That predicate is a filter on a much larger class. Measured over the committed corpus:

| | count |
|---|---|
| string leaves whose **whole value** is a number, at any precision | **2 096** |
| files carrying at least one | **76 of 144** |
| of those, the ones the prose gate can see (above nine significant digits) | 778 |

The commonest homes are exactly the blocks a reader trusts most:
`/parameters` (442), `/runParameters` (216), `/convergence/*` (111), `/reproductions/*` (69) — and
`/designPoints/*/responses/*` (549), a whole solved response table rendered as text.

**`T-250` closes the 778 and cannot touch the other 1 318.** A `"kuhnLength" to peg.kuhnLength
.roundedForProse().toString()` is now rounded and is still a **string**, and a `"0.34"` that was
never over-precise is invisible to every instrument this repository owns.

## Why this is more than tidiness

A string is not merely unrounded — it is unchecked. Three consequences, in increasing order of cost:

1. **The departure gate cannot see it.** `--departures` walks JSON **numbers**; a departure emitted
   into a `parameters` map as text is outside its scope by construction, and `CH-0198`'s whole
   argument about the floor is about numeric leaves.
2. **A consumer must parse it back.** The corpus already does this **54 times** with a literal key
   (`getValue("…").jsonPrimitive.content.toDouble()`), and **7** of the keys so read are a string
   leaf somewhere in the corpus: `armLength`, `compositeFraction`, `edgeX`, `freeStrokeBuildable`,
   `minimalAzimuthalDepartureDegrees`, `recommendedAxialExtentBasePairs`, `value`.
3. **Nothing connects (1) and (2).** Checked at `HEAD`, every one of the seven reads resolves to a
   file where the key is a **number** — `T-188` reads `freeStrokeBuildable` out of `T-153`, where
   it is `5.15473846`, not out of its own file, where it is the string `"5.154738462"`. So the
   channel is **latent, not live**. It becomes live the moment a study is pointed at the other
   file, and at that instant a decision taken about **how a sentence renders** becomes a decision
   about **how many digits an input carries** — with no type, no schema and no gate in the way.

`T-250` walked straight into (3) and had to reason about it by hand: `T-178`'s `freeTileStroke` was
emitted with `"%.9f"`, which is nine **decimal places** and therefore ten significant digits for any
value above 1, and the repair had to check whether anything read it back before changing it.

## Why this is a challenge and not a repair

The repair is a **schema**, not an edit. Making `/parameters` a `Map<String, JsonElement>` would put
2 096 leaves back under the serialisation boundary and would move a **numeric** field in 76 files —
the one thing `T-250`'s own claim forbids, and a re-emission sweep larger than the one it just ran.
It also loses something real: a parameter block deliberately carries `"33.3333333 pN/nm"`, a number
**with its unit**, and that is a string on purpose.

So the honest form is a **partition**, and nobody has drawn it:

* a parameter value that is a bare number → should be a JSON number, and the boundary rounds it;
* a parameter value that is a number **with a unit or a sentence** → stays a string, and its
  numbers are a per-call-site judgement, which is what `T-250` mechanised.

## What would settle it

The cheap bound first, and it costs one pass: partition the 2 096 into bare and annotated, per file,
and count how many files the bare half touches. If the bare half is concentrated in a few
`parameters` blocks the repair is small and the sweep is bounded; if it is spread over 76 files it
is a corpus convention change and belongs in `CLAUDE.md` before it belongs in code.
