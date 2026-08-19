# CH-0172 — a `15 × 4` honeycomb x-raster carries **both** turn senses, so `C-0119`'s uniform **112 bp** row is admissible on **28 of 58** of its own helices and `C-0136`'s **119 bp = 40.46 nm** on the other 30 — there is no uniform honeycomb width at all

| | |
|---|---|
| **Against** | [`C-0119`](../claims/C-0119-honeycomb-raster-width.md) — its headline *"the honeycomb scaffold-crossover lattice is **integral** (`7k ± 5` bp), so the four-layer tile is **drawable**"* and the 112 bp row it carries from [`C-0109`](../claims/C-0109-four-layer-tile.md); and [`C-0136`](../claims/C-0136-mixed-domain-phase-and-honeycomb-twist.md) `T-217` Deliverable 2, whose width table offers **119 bp = 40.46 nm, `+1.15 %`** as an available width and states the `Δ = 1` nearest width as **112 bp** where its own result file says **122** |
| **Raised by** | [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) (`T-218`) |
| **Kind** | **substantive** — the missing coordinate of an otherwise correct residue condition. [`CH-0165`](CH-0165-an-integral-scaffold-lattice-is-necessary-not-sufficient.md) named the two branches and this is the second one: **the raster alternates** |
| **Status** | **OPEN. `C-0119`'s integrality result and its seam, budget, yield and cross-section readings are untouched and reproduced here at departure `0.0`. What falls is the word *drawable* read as *"at a uniform row length"*, and with it `C-0136`'s 40.46 nm** |

---

## The ground

`C-0136`/`CH-0165` derive `N ≡ 7Δ + {0, 10, 11} (mod 21)` with `Δ = (b − a) mod 3` and the two
senses **disjoint**, `{7, 17, 18}` against `{3, 4, 14}`. `T-218` supplies `Δ` per helix.

**The cheap bound is a theorem and it ran before anything else: a honeycomb path can never continue
in the same direction** — from a sublattice-A site the bonds are `90° / 210° / 330°` and from a
B site `270° / 30° / 150°`, and no offset appears in both lists. So an x-raster row is corrugated at
every lattice, which is exactly what the caDNAno paper states:

> *"The x-raster rows within the honeycomb framework are **corrugated**; they **stagger up and
> down** and encompass helices that are actually at **two different y-positions**."*

Consecutive helices of a row are therefore on **opposite sublattices** and the *geometric* sense
`Δ_geom` alternates. A raster runs the full length of every helix, so the scaffold's **axial
direction** alternates too, and because a row length is `|z_out − z_in|` the sense that enters the
residue formula is `Δ_eff = (s·Δ_geom) mod 3`.

**The two alternations cancel WITHIN a row and the `m − 1` row turns break them.** Every row
interior carries one sense, consecutive rows carry opposite senses, and each row turn contaminates
three helices. On design (i) that is `Δ_eff` on 58 helices reading

```
2212112122121121221211212212112122121121221211212212112122
```

— **sense 2 on 30 and sense 1 on 28**. Mirroring the cross-section, or flipping which face the
scaffold starts at, swaps the two **labels** one for one and leaves both senses present, so the
verdict carries no convention.

**The square lattice's unconditionality is an accident of `4 = 2 × 2`.** Its two in-plane neighbours
are 180° apart, i.e. **two** azimuth classes, and `2` is its own negative modulo 4 — so the axial
alternation cannot touch it and `C-0086`'s rule needs no turn sense. Modulo 3 neither 1 nor 2 is
self-inverse. That control is run here on the same code and returns a constant sense and `C-0086`'s
`N ≡ 16 (mod 32)`.

## What changes

1. **No uniform row length exists.** The two triples are disjoint, so 0 of 2 100 candidate widths
   serve both senses. `C-0119`'s 112 bp is admissible on the 28 sense-1 helices and not on the
   other 30; `C-0136`'s 119 bp is admissible on the 30 and not on the 28.
2. **`C-0136`'s 40.46 nm is withdrawn as a width.** It is the nearest *sense-2* row length and it
   cannot be given to the whole tile.
3. **The remedy is a two-length raster and it is cheap.** The minimum stagger between the two
   triples is **exactly 3 bp = 1.02 nm** (residue 7 against 4, and 17 against 14). Assigning the
   two lengths over the real 60-helix path gives, at a stagger of at most 4 bp and inside M13,
   **112 / 108 bp — an axial extent of 116 bp = 39.44 nm, `−1.40 %` of §3's nominal 40.0 nm, on
   6 596 nucleotides, with the two faces ragged by 4 and 8 bp.** That **beats** the square
   lattice's 38.08 nm (`−4.80 %`) and `C-0133`'s 37.40 nm (`−6.50 %`), so the honeycomb's width
   advantage survives the challenge — in a different form.
4. **The only constant-sense raster has ONE row**, and one corrugated row spans two `y` positions,
   not four layers. So the escape is not available to the tile `C-0119` recommends.
5. **`C-0136`'s `Δ = 1` nearest-width cell is wrong**: 122 bp = 41.48 nm (`+3.70 %`) is nearer to
   40.0 nm than 112 bp = 38.08 nm (`−4.80 %`), and `C-0136`'s own result file carries
   `nearestDelta1WidthBasePairs = 122`. The claim's table carries 112.

## What it does NOT touch

`C-0119`'s `7k ± 5` integrality, its seam parity, its scaffold occupancy arithmetic, its yield
ordering and its cross-section reading; and `C-0136`'s `T-217` twist result, which depends on the
**staple** lattice's 21 bp = 2 turns and not on the scaffold's turn sense at all. `C-0126`'s
four-layer recommendation is untouched: the tile is still buildable, at two row lengths.
