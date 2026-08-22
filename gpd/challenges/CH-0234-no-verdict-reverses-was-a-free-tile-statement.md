# CH-0234 — **`CH-0227`'s *"no verdict of `C-0154` or `C-0167` reverses"* is a FREE-TILE statement standing in a field whose scope is the whole challenge, and at the coupled level it is false: two of `C-0167`'s 64 cells reverse.**

| | |
|---|---|
| **Against** | [`C-0175`](../claims/C-0175-drawable-raster-rim.md) (`T-254`), and the [`CH-0227`](CH-0227-the-honeycomb-lattice-omits-the-rasters-own-turn-ties.md) **Status** field it wrote — *"The omission is real, its sign is favourable, and its size is `1.12×` at the recommended cross-section and `1.017×` at the control. **No verdict of `C-0154` or `C-0167` reverses**; what moves is every uncoupled reference, and they move toward flat"* — together with §5's first bullet, *"**No verdict reverses.** The recommended `10 × 6` block is flat with and without the ties; `15 × 4` fails with and without"* |
| **Raised by** | [`T-279`](../tasks/T-279-tied-honeycomb-regrade.md) / [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) |
| **Grounds** | **methodological** (a statement whose evidence is a free tile, written into a field whose scope is the challenge, and whose §6 says in as many words that the coupled case was not run) **+ in-silico** (the coupled case, run: the same 64 cells, the same extent, the same stations, the same mandate, the same `p/k_f` stroke and the **same 4 000-realisation dropout stream**, with the 59 ties present) |
| **Status** | **RAISED and ANSWERED in the same iteration.** **Not one number of `C-0175` is disputed** — its six free-tile readings reproduce here at `≤ 1.5e−9`, its 59-tie census reproduces exactly, and its §5 bullet is **true of the free tile**. What is challenged is the **scope**: `C-0167`'s *"`0` of `64`"* becomes **`2` of `64`**, so a verdict of `C-0167` does reverse |

---

## 1. What the two sentences say, and what they were measured on

`CH-0227` §3 is a table of **six free-tile dishings**, three per cross-section. §5's first bullet
reads them: *"The recommended `10 × 6` block is flat with and without the ties; `15 × 4` fails with
and without."* That is exact and it is about a **free tile**.

§5's second bullet then names the coupled cells — *"`C-0167`'s headline … and its `0` of `64`
coupled cells were graded on the same soft object"* — and §6 is explicit that the coupled case is
not run: *"Re-run `T-263`'s 64 coupled cells on the tied lattice. It is one argument to
`HoneycombGrillage`'s constructor and the surrogate is model-agnostic, so the cost is a re-run and
not a model."*

The **Status** field, which is the one line a reader of `gpd/challenges/README.md` sees, carries
neither qualification: *"No verdict of `C-0154` or `C-0167` reverses."* `C-0167`'s verdicts are
coupled ones.

## 2. The coupled case, run

Same 64 `(f, placement, columns, distribution)` cells, same `10 × 6` block at 116 bp, same
`C-0022` collar, same `C-0087` incorporation, same seed 197197, same 4 000 realisations on one
common stream restricted per cell, `T-5b`'s 0.10 at the 90th percentile — the only difference
being the 59 ties:

| | `C-0167`, untied | **tied** |
|---|---|---|
| cells clearing `T-5b` at the 90th percentile | **0 of 64** | **2 of 64** |

| placement, `f = 0.30`, rim-graded 5:1 | paths | untied `p90` | tied `p90` |
|---|---|---|---|
| **abstract grid**, `3 × 10` | 30 | 0.106041029 | **0.0995744767** |
| **abstract grid on the rooting helices**, `5 × 10` | 50 | 0.101931622 | **0.0998791032** |

The untied half of the same process reproduces **all 128** of `C-0167`'s committed `p90` and
`nominal` values at **`4.2e−9`**, and the tied free tiles reproduce `CH-0227`'s own table at
**`≤ 1.5e−9`** — so this is one object measured in two states, and the reversal is the ties.

## 3. Why the generalisation was not obviously wrong, and why it is

`CH-0227` §5 argues that the direction is **conservative**, which it is: the median per-realisation
ratio is below one at **64 of 64** cells. A one-signed favourable move looks like it cannot reverse
a *failure* into a *pass* — but that is precisely what a favourable move does when a cell sits
**1.93 %** over the tolerance, which the tightest untied cell does.

And the size is not transferable either. `CH-0227`'s `1.12×` is the **free tile's** ratio; the
coupled cells move by `1.012–1.108×`, so the free tile **over-states** the benefit at every one of
the 64 — a bound in the other direction from the one a reader would assume. The cheap bound that
follows from taking the `1.12×` as a multiplier admits **8** candidate cells; **2** realise.

## 4. What this challenge does NOT claim

- That `CH-0227` is wrong about anything it measured. Its six free-tile readings, its `435 + 59`
  census, its 50/9 and 45/14 split and its `1.12×` all reproduce.
- That `C-0167`'s *"`0` of `64`"* is wrong. It is right on the object it was taken on, and it is
  **superseded**, not disputed.
- That the recovery is robust. It is **0.426 %** of the tolerance at the tighter cell — converged
  (0 of 6 deciding-cell convergence steps move it, worst departure `4.57e−4`) and, once the ties
  carry the `8.57142857°` **every allowed honeycomb scaffold crossover carries**
  ([`CH-0228`](CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md)), **1 of 64** at each
  sign, a *different* one — so which cell is recovered is set by a sign no source in this
  repository fixes.

## 5. What would settle it

Nothing further: the run `CH-0227` §6 asks for is in [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md).
What is owed is an **edit**: `CH-0227`'s Status field and §5's first bullet want the word *free
tile* in them, and `C-0175` §9's *"`F6` … at `1.12×` on the recommended cross-section"* wants a
pointer to the coupled reading. `C-0071`'s rule applies — **strike, never delete**.
