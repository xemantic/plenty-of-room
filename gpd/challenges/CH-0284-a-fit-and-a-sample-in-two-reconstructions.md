# CH-0284 — **`HoneycombDeflection` FITS its rigid plane in one reconstruction of the face field and SAMPLES the residual in another, and the gap is not free: ``5.0E-4`` relative on the collar load every free-tile reading in this corpus is taken under, and ``0.0067`` on a bond prestrain influence function — against `C-0180`'s tightest recovered cell, which clears `T-5b` by `0.426 %`**

**Against** the `HoneycombGrillage` / `HoneycombDeflection` construction as `T-330` leaves it — the least-squares fit is taken in [`areaInnerProduct`](../../src/main/kotlin/tile/HoneycombGrillage.kt)'s inner product where the face basis is **not** orthogonal, and the three independent projections, which fit `faceFunctional`'s pairing, are kept where it **is**.
**From** [`CH-0282`](CH-0282-a-dishing-fit-assumes-an-even-raster-row-count.md) §5, which records the inconsistency and gives it no number, and [`C-0219`](../claims/C-0219-a-dishing-fit-and-the-parity-of-its-basis.md) (`T-330`), which measures it.
**Kind** — a **convention** rather than a defect, and it is raised because a convention worth more than a live verdict's margin is not a convention anybody may leave unstated.

---

## 1. The statement

A honeycomb face carries a field on **beam axes**. Off an axis there is no element, so the class must choose a reconstruction, and it has two:

| | reconstruction | used by |
|---|---|---|
| **owning beam** | each beam owns a tributary strip of one row pitch **centred on its own axis**, and inside it the field is `W + Φ(y − y_beam)` | `assembleLoad`, `faceFunctional`, and therefore the three independent projections |
| **nearest beam** | the field at `(s, y)` is reconstructed from whichever face beam's axis is nearest | `evaluate`, and therefore `dishing`, `peakDishing` and every number this corpus reports |

The two agree **exactly on the three rigid modes** — both return `1`, `s` and `y` — so the Gram is one object either way, and the whole disagreement is in the **right-hand side**: `⟨mode_i, owningRecon(u)⟩` against `⟨mode_i, evaluate(u)⟩`.
On a corrugated face they differ because a `3d/2` strip reaches past the midpoint of a `d` gap.

So the class **fits** one reconstruction and **samples** the other, and has since `T-253`.

## 2. It is not free, and the number is the point

`CH-0282` §5 records it as *"under `1e−3` relative on a collar load"* at `10 × 6` and stops there.
Measured (`T-330`, `gpd/results/T-330-a-dishing-fit-and-the-parity-of-its-basis.json`, the `residue` block), at an orthogonal basis:

| load case | relative gap |
|---|---|
| `C-0022`'s solved collar, three enhancements | ``4.3E-4`–`5.0E-4`` |
| a point load at the face centre | ``4.7E-4`` |
| a unit bond prestrain | **``0.0067``** |
| a uniform pressure | not well posed — both readings are the solver's own noise |

`C-0180`'s tightest recovered coupled cell clears `T-5b` by **`0.426 %`**.
The collar reading is inside that margin by a factor of `**8.5**`; **the prestrain reading is not**, and a coupled surrogate's influence bank is built out of point-load and prestrain responses.
So an unconditional adoption is a change that **could move a live verdict**, on every one of the **15** even-`m` result files at once.

## 3. What `T-330` did instead, and why

It repaired the defect `CH-0282` actually raises — the three projections are not the least-squares fit at an odd raster-row count — and **branched** on the face's own exact orthogonality, so that an even-`m` reading is returned bit for bit.
That confines the blast radius to the **three** odd-`m` files and leaves this question open with a measured price rather than closed by a side effect.

`CLAUDE.md`'s own *a refusal recorded without its price is the thing that later reads as an oversight* is why the number above is in this challenge and not a caution.

## 4. What would settle it

One of:

- **Make `evaluate` use the tributary the load is assembled over.** Then fit and sample agree by construction, `faceFunctional` and `areaInnerProduct` coincide, and the branch in `faceRigidCoefficients` becomes unnecessary. It moves every dishing number this corpus has ever emitted, so it is a corpus-wide re-emission with a topological order, not an edit.
- **Adopt `areaInnerProduct` unconditionally.** Smaller, still 18 files, and it must be run against `C-0180`'s and `C-0208`'s tightest cells specifically, because the margin there is smaller than the prestrain-channel residue.
- **Show that the two reconstructions are equivalent for the quantity a flatness verdict is written on**, which nobody has attempted and which the numbers above make unlikely.

The queue row is `T-326`.

| | |
|---|---|
| **Status** | **RAISED**, iteration 52 |
| **Raised by** | [`C-0219`](../claims/C-0219-a-dishing-fit-and-the-parity-of-its-basis.md) (`T-330`) |
| **Severity** | **a convention, priced** — it moves no number today, and adopting it moves 18 result files and could cross `T-5b` at one recovered cell |
