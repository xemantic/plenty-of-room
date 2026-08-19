# T-217 — Can the four-layer HONEYCOMB tile be twist-corrected?

**Leaf:** `A8.2` (the plan and lattice model the anchoring array is written on).
**Claim reserved:** `C-0136` (shared with `T-216`).
**Challenges reserved:** `CH-0164`, `CH-0165` (shared with `T-216`).
**Raised by:** [`C-0133`](../claims/C-0133-twist-corrected-raster-row.md) *Still open* item 6.

---

## Formulate

### The question, exactly

`C-0133` proves that a **square-lattice** seamless raster row cannot be twist-corrected exactly —
`N = 21q/4` with `q` odd is never an integer — and that the residual is an invariant quarter of a
base pair, `8.5714°` of accumulated twist across the whole tile.

`C-0126` makes the **four-layer honeycomb** tile the live body, and `C-0119` establishes that it is
drawable from one circular M13 at 15 rows × 4 layers × 112 bp.
`CLAUDE.md` records that *"`10.67 bp/turn` is the **square** lattice and `10.5 bp/turn` the
**honeycomb**"*, and `C-0119` quotes the caDNAno paper directly: potential staple-crossover positions
recur every **seven base pairs** for a given helix and every **21 base pairs** for a given pair,
*"if the helical twist is fixed at 10.5 base pairs per turn"*.

**So: does the honeycomb lattice need a twist correction at all — and if it does not, what does its
connectivity quantisation cost instead?**

### The tension the task exists to resolve

If `10.5 bp/turn` is both the honeycomb's design twist and B-DNA's own preferred twist, then the
honeycomb's `Δω` is **exactly zero**, `C-0107`'s whole boundary layer has driver zero, and every
number in `C-0107`, `C-0104` and `C-0133` is a **square-lattice** number that does not transfer.
That would be a **favourable** structural argument for the recommended four-layer tile that this
programme has never made.

What does **not** transfer is the connectivity half.
`C-0086`'s odd-half-turn rule is a statement about a lattice whose neighbours sit at **180°**;
the honeycomb's three neighbours sit at **120°**, and `C-0119` records that the rule's failure there
is a **domain error rather than a prohibition** — the honeycomb quantises its scaffold crossover to
`7k ± 5` bp and the lattice is integral.
`C-0119` checks **integrality** and does not check **azimuth**: landing on the scaffold lattice is
necessary and is not sufficient, because the crossover must also point at the right neighbour.

### Locked conventions

- Lengths **nm**, angles **degrees**; rise **0.34 nm/bp**; `k_BT = 4.141947 pN·nm` at 300 K.
- **Natural twist** `ω_n = 360/10.5 = 34.2857 °/bp` — B-DNA's, this repository's locked value
  (`C-0015`, `C-0107`, `C-0133`).
- **Honeycomb design rules**, from the caDNAno paper (Douglas et al., *NAR* **37**:5001, PMC2731887,
  already in `gpd/data/T-151-sources/`), read directly:
  staple-crossover positions for a given helix every **7 bp**; for a given *pair* every **21 bp**;
  scaffold crossovers **5 bp** — *"or half a turn"* — upstream or downstream of the staple ones.
- A helix's three neighbour directions are indexed `0, 1, 2`, `120°` apart;
  the **turn pair** of a raster helix is `(a, b)` — the neighbour it receives the scaffold from and
  the neighbour it passes it to — and `Δ = (b − a) mod 3`.
- A **row** is one helix of `N` base pairs between its two scaffold crossovers.
- **Admissible** means both scaffold crossovers land on the honeycomb scaffold lattice **and** point
  at the neighbours the raster's path requires.

### Acceptance predicates

- **P1** — the twist-correction question is answered by exact arithmetic before any search:
  the honeycomb's design twist is derived from its own published azimuth period and compared with
  `ω_n`, and the accumulated register across a 112 bp row is quoted.
- **P2** — the connectivity condition is **derived** rather than transferred: the admissible row
  lengths of a honeycomb raster, as an exact residue set modulo the azimuth period, with the same
  machinery reproducing `C-0086`'s square-lattice *"odd multiples of 16 bp"* as a gate.
- **P3** — the admissible widths near §3's 40.0 nm are listed, with the density of the honeycomb list
  against the square lattice's, and `C-0119`'s own 112 bp row checked against it.
- **P4** — the residual is quoted: whether the quarter base pair of `C-0133` survives on the
  honeycomb, and if so where it lives.
- **P5** — the sensitivity of the whole favourable result to the one constant it rests on:
  what a B-DNA twist other than exactly 10.5 bp/turn does to the accumulated register.

### Verification type

**Logical** (exact integer/residue arithmetic, asserted over the whole period rather than at one
point) **+ literature** (the honeycomb design rules, read directly from the primary source already
in this repository).

---

## Plan

### Method, and the cheap bound that runs first

1. **The twist question is one division.** The honeycomb's azimuth period is 21 bp and it is
   *"two-thirds of a turn"* per 7 bp, i.e. 2 turns per 21 bp — so `ω_d = 720/21`. Compare with
   `ω_n = 360/10.5`. If they are the same number, `Δω = 0` identically and the question is closed.
   **This runs before anything else and may close the task in one line.**
2. **Why the square lattice fails and the honeycomb does not** is then one observation about
   `10.5 = 21/2`: an **odd** multiple of a half turn is a quarter base pair off an integer, and an
   **even** one is not. The square lattice's connectivity demands an odd number of half turns; the
   honeycomb's azimuth period is **four** half turns.
3. **The connectivity condition is derived from the neighbour azimuths**, not transferred:
   scaffold crossovers to neighbour `j` sit at `7j ± 5 (mod 21)`, so a row length is admissible iff
   `N ≡ 7Δ + {0, 10, 11} (mod 21)` for the raster's own `Δ ≠ 0`.
   The same construction applied to the square lattice — four azimuths, 8 bp planes, 270° per plane,
   neighbours at 180° — must return `N ≡ 16 (mod 32)`, which is `C-0086`'s rule. That reproduction is
   the gate that makes this a derivation rather than a second rule.
4. **The residual** is then read on the one quantisation the honeycomb does make: caDNAno's 5 bp
   scaffold offset against the exact 5.25 bp half turn.

### Justification against cost

Every step is integer arithmetic over a period of 21 (or 32), asserted exhaustively rather than
sampled, and the literature is already in `gpd/data/T-151-sources/` — `CLAUDE.md`'s *check
`gpd/data/` before fetching anything*, which cost `C-0119` zero fetches and costs this task zero.
No solve is justified: the honeycomb's station lattice, plan ceilings and flatness are
**single-layer square-lattice** machinery in this repository (`CLAUDE.md`: *"`OrigamiGrillage` never
reads `layers`"*), so a dishing number computed on it would be a square-lattice number wearing a
honeycomb label. Saying so is the deliverable, not computing it.

### What would falsify this approach

- **F1** — the honeycomb's design twist differs from `ω_n = 360/10.5`.
  *(Would mean the honeycomb needs a correction after all and the favourable argument fails.)*
- **F2** — some **odd** multiple of a half turn at 10.5 bp/turn is an integer number of base pairs,
  or some **even** multiple of a half turn is not.
  *(The unification of `C-0133`'s theorem with this task's answer.)*
- **F3** — the derivation does not reproduce `C-0086`'s *"odd multiples of 16 bp"* when run on the
  square lattice's own azimuths.
- **F4** — no admissible honeycomb row length lies within 5 % of §3's 40.0 nm.
- **F5** — the honeycomb admissible list is not denser than the square lattice's.
- **F6** — the scaffold half-turn quantisation residual is not exactly a quarter of a base pair.
- **F7** — `C-0119`'s own 112 bp row is inadmissible at **every** turn pair.
  *(Would overturn `C-0119`'s drawability conclusion rather than qualify it.)*
