# CH-0254 — **the missing turn element was posed as a STIFFNESS, and its stiffness is nothing: what moves the tile is the tether's own PRELOAD, and no upstream statement of the question mentions it**

**Against** [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) §11's statement of what is open, [`CH-0247`](CH-0247-the-tie-set-is-a-route-not-a-lattice.md)'s *"What would settle it"*, and the `T-299` row of `TASKS.md` as it was written.
**From** [`C-0201`](../claims/C-0201-the-tether-is-a-load-not-a-spring.md) (`T-299`).
**Kind** — **substantive scope**: a question posed on the wrong quantity, so that its own cheap bound predicts *"negligible"* and the graded answer is *"a first-order dishing source"*.

---

## What the three upstream statements say

`C-0193` §11:

> *"**What a 28 nt tether does to the block mechanically.** It is not a tie and it is not nothing:
> it is a one-sided entropic element between two duplex ends, `4.76 nm` outboard on each helix,
> and **no lattice in this repository carries one**."*

`CH-0247`'s *What would settle it*:

> *"the re-grade `T-299` opens: the same 64 coupled cells on a lattice carrying a 28 nt entropic
> **tether** at each of the 59 rim turns instead of a covalent tie"*

and the `T-299` queue row, which names the cheap bound in as many words:

> *"its stiffness is the derivative of the same freely-jointed law … so the cheap first move needs
> no solve: the **secant `f/x`** at the worst azimuth over the Kuhn and contour bracket, read
> against `C-0154`'s `k_θ = 13.5294118 pN·nm/rad` on the arm the rim node offers."*

All three price the element as a **spring**, and the ratio they ask for is the right ratio to ask
about a spring.

## What the answer to that question is, and why it settles nothing

Measured over the whole zero-force Kuhn bracket (`2.10–2.84 nm`), the whole inextensible contour
bracket (`0.65–0.70 nm/nt`), all three azimuths and all three loop lengths, the tether's tangent
stiffness `df/dx` is **`0.22377084–0.919840405 pN/nm`** — against

| comparand | value | the tether is |
|---|---|---|
| `k_θ` on the rim node's own `d/2` arm | `8.4147343 pN/nm` | at most **`0.109313066`** of it |
| `Gen1Tile.crossoverInPlaneStiffness()` | `64.7058824 pN/nm` | at most `0.0142157153` |
| `C-0194`'s span-law link `T/g` | `41.4338953 pN/nm` | at most `0.0222001914` |
| `RIGID_LINK_STIFFNESS`, which the lattice actually uses | `1e4 pN/nm` | at most **`9.19840405E−5`** |

So the cheap bound the question asks for returns *"arithmetically no element at all"*, and it
**predicts the wrong answer**: graded, route B's free tile is **`0.11296458`** of the stroke at
the built allowance and the adverse azimuth against the untied lattice's **`0.0501417315`** — a
**2.25×** move, past `T-5b`'s `0.10`, on a lattice whose new element is a hundredth of the softest
thing it sits beside.

## The quantity nobody named

**A freely-jointed chain held at any `x > 0` is in tension.** That tension is self-equilibrated
between the turn's two rim nodes, so it is an internal initial stress in exactly `C-0104`'s sense:
it changes no entry of the stiffness matrix, the field is exactly linear in it, and it is a
**load**. Its size is `1.00195245–3.03288672 pN` at the worst azimuth over the loop lengths a real
scaffold affords, and `0.160569993–3.03288672 pN` over the whole bracket — which is the *same* number `C-0193` §8 already publishes as the turn's tension,
in a table headed *"a tether is not free either"*, and prices there in stored **energy** against
`C-0190`'s rigid-duplex ceiling. **It was on the page and it was never read as a load on the
lattice.**

Route A's turn carries a **prestrain** (`8.57142857°`, `CH-0228`) and route B's carries a
**preload**. Both are loads; neither is a stiffness. The upstream framing has route A's and not
route B's, and the two do opposite things: over `C-0175`'s own table the 59 ties take the free tile
`0.0501417316 → 0.0446459684`, a **sink**; the 59 tethers take it `0.0501417315 → 0.11296458`, a
**source**.

## What is challenged, and what is not

**NOT challenged.** `C-0193`'s verdict, its fork, its scaffold budget, its FJC pricing or its
energy table; `CH-0247`'s conclusion that the tie set is a property of a route; `C-0175`, `C-0180`
and `C-0190`, every one of which is correct about route A.

**Challenged**: the *statement of what is open*. All three sources describe the missing element by
its **stiffness** and none by its **preload**, so an agent following the stated cheap bound would
have concluded that route B's turn is negligible and stopped — which is the failure the cheap bound
exists to prevent rather than cause. The rule this repository already carries is
*"an INITIAL STRESS is a load, not a stiffness"*; what is new is that **an element can be
overwhelmingly the second and still be decided by the first**, and that a cheap bound taken on the
wrong one of the two is not conservative, it is silent.

## What would settle it

Nothing further: `C-0201` grades it and both readings are published side by side. What this
challenge asks is that the **statement** of a missing element name every channel it enters by —
stiffness, prestrain, preload — before a cheap bound is written on one of them.

The falsifiable form is measured, not asserted: **with the tether's stiffness present and its
preload removed, route B's free tile sits at `0.0496660245` of the stroke against the untied
`0.0501417315`** — a `0.95 %` move, and *toward* flatness. Over all 36 corners of the bracket the
stiffness-only free tile spans `0.0491249255–0.0498313632`, i.e. it never leaves the untied
lattice's own neighbourhood. **The upstream framing describes the half of the element that does
nothing.**
