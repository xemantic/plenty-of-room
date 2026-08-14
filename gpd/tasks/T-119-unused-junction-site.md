# T-119 — Can a flexure hinge be built on a junction site the single-layer sheet does not use?

| | |
|---|---|
| **Raised by** | [`C-0054`](../claims/C-0054-consumed-crossover-sheet.md), *Challenges* item 1 — which names it **the one falsifier that would overturn the whole claim** and files it as a task rather than guessing at it |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the count belongs to |
| **Verification type** | **logical** (helical-phase arithmetic on measured and cited lattice constants — no mesh, no fitted parameter) **+ literature** (a recorded search, with every number flagged read-directly / abstract-only / not-found) **+ in-silico** (`C-0015`'s `CrossoverLayout`, `C-0053`'s interval scheduler and `C-0039`'s elastica **re-run as libraries**) |

---

## Formulate

### The question, stated so that it can fail

`C-0054`'s central premise is that **hinge use and interface use are exclusive at the site**, and from it
a pigeonhole: a connected 15-duplex sheet needs one retained crossover on each of its **14** interfaces,
so of the **56** crossovers `C-0015` inventories at most **42 (75.0 %)** can be spent as hinges — against
`C-0046`'s admissible **80–100 %**, which is why all three of its surviving designs sever the tile.

That pigeonhole has two premises, and they are not the same statement:

1. **Exclusivity at a site.** A reciprocal strand exchange has two strands and **two partners**; a site
   that exchanges with a flexure arm is not also exchanging with the neighbouring sheet duplex.
2. **The inventory.** The hinges must be drawn from the **56 crossovers the sheet itself builds**.

`C-0054` argues (1) and *assumes* (2). **This task tests (2).** A square-lattice helix has **four**
neighbour directions and a single-layer sheet occupies only **two** — that is exactly the per-helix
16 bp against the per-interface 32 bp. If the two unoccupied azimuths carry usable junction sites, the
hinge inventory is **larger than the sheet's own**, hinge use and interface use become **additive**,
`C-0054`'s ceiling stops binding and `CH-0066` falls.

### The acceptance predicate

Deliver exactly one of:

- **(a)** the motif exists — with the source **read directly**, and the consequence for `C-0054`'s
  42-crossover budget and `C-0053`'s 25-arm count **computed**, not asserted;
- **(b)** the site is geometrically **unusable** — with the helical-phase argument made quantitative,
  which *confirms* `C-0054` and is a result in its own right;
- **(c)** **not established** — with the query strings recorded, so that the negative is falsifiable by
  one paper.

### Units and conventions, restated rather than inherited

- Lengths **nm**, angles **degrees**, forces **pN**, stiffness **pN/nm**, moments **pN·nm**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- **Plan and space.** `x` runs **along** the helices, `y` **across** them in the sheet plane, `z`
  **normal** to the sheet and **positive upward** — away from the grafted layer and the electrode,
  which lie **below** the tile. §1's bias pulls the tile **down**.
- **A junction site** is a base pair at which a strand's backbone faces a neighbour position of the
  lattice, i.e. an allowed antiparallel-crossover position. It is a property of the **lattice**, not of
  a design: a site is *used* when a design builds a crossover on it and *unused* otherwise.
- **The azimuth naming is Ke et al.'s own** — `NORTH` (0 bp), `WEST` (8 bp), `SOUTH` (16 bp),
  `EAST` (24 bp), the compass reading of a 0.75-turn advance per 8 bp. Here `NORTH`/`SOUTH` are the
  **in-plane** neighbours `±y` and `EAST`/`WEST` the **out-of-plane** ones `+z`/`−z`.
- **The design twist is 10.667 bp/turn = 33.75°/bp** (`32 bp per 3 turns`), the square lattice's own,
  and the **preferred** B-DNA twist is **10.5 bp/turn = 34.286°/bp**. Both are CITED and both are read
  directly from Ke et al. (2009).
- **A hinge is a crossover between one pair of bodies**, and `k_θ` is that pair's **interhelical
  dihedral** spring (`C-0040`). Nothing here changes the constant; what may change is **which pair**.
- **The inventory of the sheet is `C-0015`'s** — 56 crossovers at the ten eight-column phases, 49 at
  the other twenty-two — and it is **re-derived** here from the azimuth arithmetic rather than cited.

---

## Plan

### The cheap bound, and the falsifier it is declared against

&nbsp;&nbsp;&nbsp;&nbsp;**`8 bp × 33.75°/bp = 270.0°` exactly — a quarter turn from the sheet's own plane.**

Four arithmetic operations. If the square lattice's unoccupied azimuths landed anywhere other than
`±90°` from the occupied ones, the perpendicular register would not exist, the answer would be **(b)**,
and the task would close in a paragraph. **Declared falsifier 1: an out-of-plane azimuth departing from
`±90°` by more than a crossover can absorb.**

The second cheap bound is the **register departure at the preferred twist**: because the departure is
*linear in the base-pair offset*, the 8 bp out-of-plane site is off-register by **half** what the 16 bp
in-plane site the sheet actually uses is off by. **Declared falsifier 2: the out-of-plane departure
exceeding the in-plane one.** If it fires, the unused site is the more strained of the two and (b)
follows.

### Method, and why it is this one

1. **Literature first, primary sources only.** The square lattice's crossover rule is a *published
   design rule*, so it is read from the papers that state it — Ke et al. (2009) for the square lattice
   and Douglas et al. (2009) for caDNAno's honeycomb analogue — and not recalled. `CLAUDE.md`'s rules
   govern: `pdftotext -layout` or EuropePMC/PMC full text, ~8 s between queries, **every number flagged
   read-directly / abstract-only / not-found**, and **every query string recorded** so that a negative
   is falsifiable by one paper. Cost: hours, and it is the acceptance predicate itself.
2. **The geometry as executable arithmetic, TDD.** The azimuth model is built independently of
   `CrossoverLayout` and then **required to reproduce it** — the in-plane sites it derives must equal
   `C-0015`'s inventory at every one of the 32 phases. That is the gate that makes the *unused* count
   trustworthy: two constructions, one of them already published, agreeing on the used half.
3. **The consequences, computed on the upstream pipelines rather than argued.** `C-0053`'s exact
   per-row interval scheduler and `C-0039`'s elastica are re-run on the **out-of-plane** root lattice,
   and `C-0054`'s pigeonhole is re-evaluated on the extended inventory.
4. **What is *not* done, and why.** No molecular mechanics. The question is whether a *site* exists and
   what its *inventory* is; a torsion-level check of the junction is `T-71`'s, and it can only make the
   answer worse, never better — the same argument `C-0029` made for deferring it.

### What would falsify this approach

- **F1** — an out-of-plane azimuth not at `±90°` (the perpendicular register does not exist) → **(b)**.
- **F2** — the out-of-plane register departure exceeding the in-plane one at the preferred twist → **(b)**.
- **F3** — the azimuth model failing to reproduce `C-0015`'s 56/49 inventory at any of the 32 phases
  (the model is wrong, and nothing derived from it may be quoted).
- **F4** — the out-of-plane inventory being **smaller** than the interface count it would have to
  replace (the escape exists but is too small to matter).
- **F5** — the out-of-plane arm placement failing to beat `C-0053`'s **25** (the escape exists and buys
  nothing, which is a finding and not a failure).
- **F6** — a published statement that a crossover at an unoccupied azimuth is forbidden or has been
  tried and failed → **(b)**, and the strongest possible form of it.
