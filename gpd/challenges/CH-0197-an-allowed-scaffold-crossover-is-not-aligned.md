# CH-0197 — **`C-0147`'s `n = 0` check reads a scaffold crossover as AZIMUTHALLY ALIGNED, and caDNAno's own rule does not place it there.** The exact half turn at 10.5 bp per turn is **5.25** bp and the rule offers **±5**, so every allowed scaffold crossover sits `8.57142857°` off the line of centres — spanning **0.787091706 nm** at **`+3.39051453 σ`** of the measured step, **outside** its 99th percentile, against the aligned reading's `+1.49997857 σ` inside it. **`C-0147`'s verdict survives and its ground moves**, and the corrected ground is the stronger one: the honeycomb lattice **demonstrably absorbs `8.57142857°` at every scaffold crossover of every origami ever folded**, which is an empirical calibration the corpus was not carrying

| | |
|---|---|
| **Against** | [`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md)'s `T-230` Deliverable 1 — *"a scaffold crossover is a turn with no unpaired nucleotides, so its span must be one phosphodiester step — and `d − 2r_P = 0.718724283 nm` sits at `+1.49997857 σ` … **and inside its 99th percentile**"* — and its `F1` row, which reads *"`+1.49997857 σ`, inside the 99th percentile"* |
| **Raised by** | [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) / [`T-246`](../tasks/T-246-forced-scaffold-crossover-price.md) §5, result [`gpd/results/T-246-forced-scaffold-crossover-price.json`](../results/T-246-forced-scaffold-crossover-price.json), section `allowedCrossoverReadings` |
| **Grounds** | **logical**, on one line of the primary source already in `gpd/data/`. `5.25 − 5 = 0.25` base pairs, and `0.25 × 240/7 = 8.57142857°`. No solve |
| **Kind** | **a missing convention, not an error.** `C-0147`'s `turnPhosphateSpan` is exact and is consumed unmodified here; what it was evaluated at is the *aligned* azimuth, which is caDNAno's **idealisation** of its own rule rather than the rule's own geometry. Both readings are defensible and the claim carried only one |
| **Status** | **raised.** `C-0147`'s reach bound, its 6 nt minimum, its 4.66666667× *"the allowance is a CHOICE"*, its worst-azimuth energies and the whole of `T-231` are untouched and are consumed unmodified |

---

## 1. The rule places the crossover, and it is not at zero

Douglas et al. (`PMC2731887`, **read directly**, `gpd/data/T-151-sources/`):

> *"Our default rules allow antiparallel crossovers between adjacent scaffold helices to occur
> **five base pairs, or half a turn**, upstream or downstream of allowed crossover positions for
> the associated staple helices."*

*"Five base pairs"* and *"half a turn"* are offered as the same thing and they are not: at the
10.5 bp/turn the same paragraph fixes, half a turn is **5.25** bp. So the two allowed scaffold
positions sit `0.25 bp` **short** and `0.25 bp` **long** of the exact antipode — `171.428571°` and
`188.571429°` against `180°` — and each is `8.57142857°` off the line of centres.

| reading | azimuth | span [nm] | σ of the measured C2′-endo step | inside P99? |
|---|---|---|---|---|
| `C-0147`'s — aligned, caDNAno's idealisation | `0°` | 0.718724283 | **+1.49997857** | **yes** |
| the rule's own geometry | `8.57142857°` | **0.787091706** | **+3.39051453** | **no** |

## 2. The verdict survives, on a stronger ground

`C-0147`'s `F1` asks whether *"a honeycomb scaffold crossover is impossible"*. It is not — honeycomb
origami folds by the tens of thousands — so the falsifier's **not firing is right at either
reading**. What changes is what the not-firing *means*.

- On the **aligned** reading, the rigid model says a crossover closes comfortably, and `+1.5 σ` is
  offered as *"the check that the geometry is being read right"*.
- On the **rule's own** reading the rigid model says it does **not** close, at `+3.39051453 σ` and 1.04010197
  of the measured 99th percentile — while the object demonstrably exists. **That is not a failure of
  the geometry; it is a measurement of how much the structure absorbs.** Every scaffold crossover in
  every honeycomb origami ever folded carries `8.57142857°` of azimuthal register that the rigid
  model cannot close, and folds anyway.

**That is an empirical calibration the corpus was not carrying, and it costs nothing to have.** It is
what lets `C-0152` say that a *forced* crossover, at `17.1428571°`, asks for exactly **twice** what
the lattice already demonstrably absorbs — a statement no new measurement can improve on and no
elastic model is needed for.

## 3. What is owed

- `C-0147`'s Deliverable 1 table gains a row, and its `n = 0` sentence gains the qualifier *"read at
  caDNAno's own idealisation of its rule"*.
- Its `F1` note should read the **pair**: `+1.49997857 σ` aligned and `+3.39051453 σ` at exact
  geometry, with the observation that the object exists at both — which is the ninth instance in
  this project of *"quote it with the state it is read at"*, and the state is a **convention inside
  a cited rule**.
- **Nothing numeric moves.** `C-0147`'s reach table is computed at azimuth *extremes* (`0/180` and
  `180/0`) and at the azimuth **average**, none of which this touches; its 6 nt reach bound, its
  1.00195245–1.46667915 pN and 0.518481856–0.7570064 `k_BT` are all read at the **worst** azimuth
  and are unaffected. No result file is re-emitted.

## 4. Scope

- The correction is **honeycomb-only**. The square lattice's crossovers are a different arithmetic
  (`10.67` bp/turn, 8 bp planes) and nothing here applies to them.
- Both readings are of the **same** cited sentence and the challenge does not prefer one: it asks
  that both travel, because the two disagree about whether a demonstrated object closes.
