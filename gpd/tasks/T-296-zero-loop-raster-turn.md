# T-296 — Is a honeycomb raster turn built with ZERO unpaired nucleotides, and if not, what is conditional on it?

| | |
|---|---|
| **Leaf** | **`A8.2`** |
| **Raised by** | [`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md) (`T-291`) §10 and §11 |
| **Verification type** | **literature** (the built precedent's own scaffold accounting and the loops' own stated purpose, read directly from primary sources in `gpd/data/`; plus a recorded existence sweep) **+ logical** (a covalent reach bound on the measured backbone, exact integer scaffold arithmetic, and an exact freely-jointed-chain law — all closed forms) **+ in-silico** (parsing this repository's own committed `.sc` designs and the field's own reference generator) |

---

## Formulate

[`C-0175`](../claims/C-0175-drawable-raster-rim.md) §9 models a raster turn as
*"a turn carrying **zero** unpaired nucleotides"*, which **is** a scaffold crossover,
and that is the whole of what makes it a **covalent tie** at `s = ±L/2`.
The `435 + 59` element split, the `1.12×` stiffness,
[`CH-0228`](../challenges/CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md)'s prestrain,
[`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md)'s derived sign,
[`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) §4's coupled recovery and
[`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md)'s per-beam twist
all descend from it.

The only honeycomb blocks anybody has folded allot **28 nt per helix** to unpaired loops.
A turn through 28 unpaired nucleotides is a flexible **tether**:
no tie, no prestrain, no twist, and no `1.12×` either.
**The two readings are not a tolerance on one design, they are two designs, and the corpus has graded one.**

### Locked units and conventions

- Lengths **nm**, forces **pN**, energies **pN·nm and `k_BT`**, counts in **nucleotides** and **base pairs**.
- `k_BT = 4.141947 pN·nm` at **300 K**; rise **0.34 nm/bp**.
- Honeycomb interhelical distance **`d` = 2.536 nm** (`Gen1Tile.INTERHELICAL_HONEYCOMB`, SAXS).
- Phosphate radius **`r_P` = 0.9086378584708424 nm** and the intrastrand P···P step
  **0.6644805804152175 ± 0.0361629850 nm** (C2′-endo) — both **measured**, `T-71`, `MeasuredBackbone`.
- A **turn** joins the end of one helix to the end of the next along the raster path.
  A helix carries a front and a rear unpaired fragment of `L/2` each, so a turn's slack is `L`,
  the **per-helix** allotment — which is how the built blocks' accounting is written.
- **Route A** = every turn a scaffold crossover, zero unpaired slack.
  **Route B** = every turn an unpaired loop. `C-0140`'s and `C-0147`'s names, kept.

### Acceptance predicates

| id | predicate |
|---|---|
| **P1** | The cheap bound is **re-derived**, not inherited: `d − 2r_P` from the repository's own constants, against the measured C2′-endo step and its 99th percentile, reproducing `C-0147`'s `0.718724283 nm` and `+1.49997857 σ` at a relative departure `≤ 1e−9`, and its 6 nt worst-azimuth reach bound at departure `0.0` |
| **P2** | **Drawability is demonstrated, not asserted**: the committed `gpd/designs/gen1-block-honeycomb-10x6-102-109.sc` is parsed and shown to carry one scaffold strand, 60 domains and **zero** loopouts; and the field's own reference generator is shown to emit a square-lattice raster with zero loopouts, so the motif is the reference default on *some* lattice |
| **P3** | The built precedent's `126 = 98 + 28` allotment is read **directly** from its primary source and its **scope** established — one design or all seven |
| **P4** | The loops' own **stated purpose** is read directly from a primary source, and whatever purpose it names is checked against whether route A can discharge it another way |
| **P5** | A **recorded** existence sweep — at least 25 queries in at least 7 named families, query strings retained, every number flagged `read directly` / `abstract only` / `not found` |
| **P6** | The scaffold budget is emitted for the **drawable `102 / 109` raster** (which post-dates `C-0147`) at the built allowance and **inverted** for the largest affordable loop, on all three scaffolds, with `C-0151`'s own `6 330 / 919 / 15 nt` reproduced at departure `0.0` |
| **P7** | The conditionality is **quantified**: for each of `C-0175` §9, `C-0180` §4 and `C-0190`, the number that survives on route B and the number that does not, read out of the committed result files rather than restated |

### Declared falsifiers

| id | fires if |
|---|---|
| **F1** | the `n = 0` span falls **outside** the measured step's 99th percentile — the zero-loop turn is then not reachable and the answer is *they cannot* |
| **F2** | the committed honeycomb `.sc` carries a loopout — the corpus's own artifact is then not the design it claims |
| **F3** | the `126 = 98 + 28` allotment turns out to be **design-specific** rather than covering all seven cross-sections |
| **F4** | **a published honeycomb origami with zero-loop raster turns is found** — the answer is then *they can, with yield evidence*, and the conditionality section is unnecessary |
| **F5** | the primary source names turn **closure** (not aggregation) as the loops' purpose — the loops are then load-bearing and the answer is *they cannot* |
| **F6** | at the built 28 nt allowance the drawable `102 / 109` raster **fits M13mp18** — the two designs are then not separated by the scaffold |
| **F7** | removing the 59 ties reverses a flatness verdict at the **free-tile** level — the conditionality is then not merely quantitative |

`F4`, `F6` and `F7` are declared **open**: none of them is known before the run.

### What result would falsify this approach

Two things would.

**One:** if the reach bound refused (`F1`), the whole task collapses to *they cannot* and the
literature half is not worth running — a covalent impossibility outranks any precedent.

**Two:** if the built allowance's stated purpose were **turn closure** (`F5`), then no staple-side
remedy could substitute for it and route A would be a claim against a primary source rather than a
claim it is silent about. The task is worth running only because both of those can come back the
other way, and neither is knowable without reading.

---

## Plan

**The cheap bound runs first and it is one subtraction.** `d − 2r_P` against a measured
phosphodiester step decides *reachable / not reachable* before any literature is fetched, and
`C-0147` already has the machinery — so the honest spend is to **re-derive** it from the same
constants in an independent implementation and assert the reproduction, which is what makes it a
check rather than an inheritance.

**Then `gpd/data/` before any fetcher.** `CLAUDE.md` records this paying three times. The caDNAno
paper (`PMC2731887`) and the square-lattice multilayer paper (`PMC2821935`) are already in the
tree from `T-151` and `T-246`, and between them they carry the allotment, its scope, and the
loops' stated purpose. Everything past that is an **existence** question, which is a sweep.

**Then the arithmetic that nobody has done**, because the object post-dates the claim that would
have done it: `C-0147`'s budget is written on a **uniform 112 bp** row and the design this
programme now recommends is `C-0151`'s **two-length `102 / 109`** raster, whose paired total is
`6 330 nt`. One division says what loop each scaffold affords on *that* raster, and one
multiplication says whether the built allowance fits it at all.

**A Python emitter in `tools/`, not a Kotlin study.** Every number here is a closed form, an
integer, or a field read back out of a committed result file; the only iterative step is an
inverse Langevin by bisection, which is twenty lines and is cross-checked against `T-230`'s own
committed records. Writing it in Kotlin would need a `ResultInputs` handle and therefore an edit
to a shared main source that a sibling agent owns this iteration, for no numeric gain.
The Python mirror carries its own self-tests, per `CLAUDE.md`.

**Cost.** No solve, no lattice assembly, no Gradle. The sweep is ~30 EuropePMC queries at 8 s
apiece. The whole task is minutes of compute; what it costs is reading.
