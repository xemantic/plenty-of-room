# T-195 — the unpaired scaffold remainder as a DEFAULT body in the actuated gap

| | |
|---|---|
| **Leaf** | **`A7.4`** (the electrostatic load on the tile), with **`A8.2`** (what the tile is folded from) |
| **Claim reserved** | **`C-0125`** |
| **Challenges reserved** | **`CH-0147`**, **`CH-0148`** |
| **Result** | `gpd/results/T-195-scaffold-remainder.json` |
| **Verification type** | **literature** (which scaffold, read directly from a source already in `gpd/data/`) **+ logical/in-silico** (two cheap bounds and a 1-D Poisson-Boltzmann re-read) |
| **Maturity** | **TRL 1–3.** Nothing here is measured. `PASS` means model-consistent and traceable. |

---

## 1. Formulate

### The question

NDI's answer to decision 5 (2026-08-18) is *"M13, circular ~7–8 K nucleotides"*,
so a **circular scaffold longer than the tile needs** is the default rather than one of three options.
A coil of unpaired scaffold therefore sits beside a tile whose edge load [`C-0022`](../claims/C-0022-tile-edge-load-profile.md) solved with **nothing there**.

Three questions, in order:

1. **Which remainder is real?** `T-195`'s own row was queued at **5 569 nt** on the single-layer sheet.
   [`C-0109`](../claims/C-0109-four-layer-tile.md) spends 6 720 nt on a four-layer tile, leaving 529 on M13mp18;
   [`C-0119`](../claims/C-0119-honeycomb-raster-width.md) reads the literature scaffold for the published cross-section as **p8064**, leaving 1 344.
   This is a **specification** question with a **literature** answer already in the corpus.
2. **Does the remainder move `C-0022`'s edge load?** — charge, conformation, and where it sits.
3. **Where is it?** §3 does not say. If that cannot be answered, say so plainly.

### Numeric target and acceptance predicate

**`P1` — the scaffold.** The scaffold each candidate cross-section is folded from is quoted from a source
**read directly**, with its nucleotide count, and the remainder at the recommended tile is stated as a
number rather than a range of opinions. **FAIL** if the answer rests on recall.

**`P2` — the charge ledger.** The remainder's charge relative to the tile's own backbone charge, at the
**current** tile and every candidate scaffold, on **both** the bare and the Manning-renormalised reading,
with `C-0086`'s **1.66×** reproduced exactly at its own state. **FAIL** if the reproduction departs by
more than `1e−6` relative.

**`P3` — the cheap bound.** An **unconditional** upper bound on what the remainder can do to the tile's
gap-facing effective charge density: the whole remainder, Manning-renormalised, smeared onto the
**gap-facing plane** — the closest place any of it can be. **PASS** if that bound is smaller than
`C-0022`'s own standing model spreads (`C-0008`'s 7.2 % charge-reading ambiguity; `C-0009`'s
0.944–0.994 lattice correction; `C-0034`/`C-0100`'s ±14.7 % fringing). **FAIL** if it is larger — then
the field solve is owed.

**`P4` — the conformational bound.** The coil's size over the declared ssDNA bracket, and the slit
confinement free energy at the two ends of §3's gap range, with the **penetration count** — how many
nucleotides can sit inside the gap at a cost of order `k_BT`. **FAIL** if the coil is smaller than the gap
anywhere in the bracket, because then the expulsion argument does not exist.

**`P5` — the bias and the edge load, re-read.** The applied bias that holds the same force with and
without the worst-case perturbation, from a 1-D Poisson-Boltzmann gap solve at `C-0012`'s states; and the
collar's **width**, which is `q₀² ≥ κ² + (π/2h)²` and contains no surface charge. **FAIL** if the bias
moves by more than the bracket `C-0008` already carries for the same states.

### Locked units, geometry and sign conventions

- SI. Lengths **nm**, forces **pN**, charge in **e**, areal charge in **e/nm²**, energies in **k_BT**;
  `k_BT = 4.141947 pN·nm` at **T = 300 K**; pressure **pN/nm² = 1 MPa**.
- Aqueous **MgCl₂**, a **2:1** electrolyte: `I = 3c`. Three concentrations, 0.5 / 2 / 10 mM.
- **`z` runs from the electrode (`z = 0`) to the tile's gap-facing face (`z = h`)**; the gap `h` is §3's
  5–10 nm layer height minus the stroke, 2–10 nm.
- The tile is **negative**, the electrode **positive**; a *"gap-facing"* charge density is the tile's own
  charge **halved** (one of the two faces), which is `C-0022`'s own reading and is restated, not inherited.
- A **remainder** is the scaffold's nucleotides that no staple pairs: `scaffold − duplexes × basePairsPerRow`.
- A **nucleotide is one phosphate**, i.e. one elementary charge before condensation.

---

## 2. Plan

### Method, and the cost that justifies it

**Step 0 — read `gpd/data/` before fetching anything.** `CLAUDE.md` records that this has paid four times.
`gpd/data/T-151-sources/PMC2731887-fullTextXML.xml` is the caDNAno paper, fetched two iterations before
`C-0119` needed it; it names the scaffolds per design in its Methods. Cost: one `grep`.

**Step 1 — the cheap bound, before any field solve.** Two of them, and either alone may settle the task:

- **Saturation.** `CLAUDE.md`: *"a charge-saturated surface makes its own charge ambiguity irrelevant —
  check for saturation BEFORE spending an iteration resolving a charge model."* The tile sits at 85–98 %
  of the 2:1 saturated far-field amplitude. Put the **entire** remainder on the gap-facing plane and read
  `σ_eff` again. This is an unconditional bound: no conformation, no placement, no field solve, and it is
  **conservative in every argument** — the closest plane, the largest surviving charge fraction, the whole
  chain.
- **Confinement.** An ideal chain in a slit of width `d` pays `π² R_g²/d²`; a swollen one `(R_F/d)^{5/3}`.
  If the coil is much larger than the gap, it is expelled, and the number of nucleotides that can enter at
  `~k_BT` is `6d²/(π² b c)` — a closed form, no solve.

**Step 2 — the re-read, only for what the bound leaves.** A **1-D** Poisson-Boltzmann gap solve
(`PoissonBoltzmannGap`, tridiagonal damped Newton, already in the tree) at nominal and perturbed tile
charge, at `C-0012`'s three states, giving `Δ|F_es|` at fixed bias and `ΔV` at fixed force through the
Stern series. Cost: ~12 tridiagonal solves. The **2-D edge solve is deliberately NOT re-run**: the collar's
width is `1/q₀` with `q₀² ≥ κ² + (π/2h)²`, which carries **no surface charge at all**, so within linear
theory a uniform change of the tile's charge cannot move the collar's width — only its level, and a level
change at a force-pinned operating point is absorbed by the bias (`CLAUDE.md`, the force-pinned rule).
Re-running a 2-D solve to measure a quantity a closed form says is exactly zero is the expensive way to
learn nothing.

### Why the Manning fractions are not the same on the two bodies

The sheet is duplex DNA: one charge per **0.17 nm** of axis, `ξ_M = l_B/b = 4.20`, so `q ξ_M = 8.4` and
**11.90 %** survives. The remainder is **single-stranded**: one charge per **0.57–0.70 nm** of contour,
`q ξ_M = 2.04–2.51`, so **39.9–49.0 %** survives — **3.35–4.12×** more per nucleotide. `C-0086` compared
the two on **bare** charge. Both readings are carried; the Manning one is the load-bearing one and it runs
**against** the answer, which is why it is the one the bound is taken on.

### What would falsify this approach

| # | falsifier | what it would mean |
|---|---|---|
| **F1** | the worst-case `σ_eff` perturbation exceeds `C-0022`'s standing model spreads | the cheap bound does not settle it and the 2-D edge solve is owed |
| **F2** | `C-0086`'s 33.332 nm and 1.66× do not reproduce from their own inputs | the ledger is not measuring what the standing claim measured |
| **F3** | the coil is smaller than the gap anywhere in the ssDNA bracket | there is no expulsion argument and the coil may sit in the gap |
| **F4** | the bias re-read moves further than `C-0008`'s own bracket at the same state | the level change is not absorbable and the operating point moves |
| **F5** | the four-layer tile does **not** reduce the exposure — the remainder ratio fails to fall | `T-191` did not spend the excess and the original 1.66× stands |

### What this task does NOT do

- It does not locate the coil. §3 fixes no attachment point for the scaffold's unpaired arc, and no
  calculation here can supply one. What it does instead is make the answer **independent** of the location,
  by bounding the worst location.
- It does not re-run the 2-D edge solve (see above), and it does not price the PEG layer the coil would
  have to displace — a **positive** term, omitted, which makes the expulsion bound conservative.
- It does not price folding: Rothemund's measured verdict on a long remainder is favourable
  (*"long, unfolded single-stranded sections of the scaffold do not adversely affect folding"*), and
  `C-0086` already carries it.
