# C-0122 — **`C-0118`'s largest caveat is discharged: the honeycomb supplies the stations, and the flatness survives being placed on the real lattice.** All **8** demands fit — `15 × 4` offers **90** upward stations and `10 × 6` offers **60**, against 10 to 75 asked for — and snapping every station to the honeycomb's own **21 bp = 7.140 nm** ladder leaves **all four `10 × 6` cells flat at the 90th percentile**, three of them slightly *better*. Both declared falsifiers failed to fire. **And the census carries a finding nobody was looking for: a deeper block offers FEWER stations.** At a fixed 60 helices the count is set by the **face**, not the helix count, so the flatter, stiffer `10 × 6` supplies 60 where `15 × 4` supplies 90 — **a thicker tile buys rigidity and spends attachment lattice**

> **Annotated, iteration 34 ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0175`](../challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md); swept under [`T-234`](../tasks/T-234-honeycomb-correction-supersession.md)).**
> **This claim's 90 and 60 are RESTORED at departure `0.0` and every reason given for them is withdrawn**,
> including `CH-0151`'s iteration-28 correction to 132/90 in the annotation below.
> A honeycomb face carries exactly **one** rooting azimuth per helix, at **30°** from the normal with the sign alternating,
> **no perpendicular root anywhere**, an across-helix station pitch of `3d/2` = 3.804 nm rather than `d`,
> and a **forced 7 bp stagger** between adjacent station rows.
> See the iteration-33 annotation in the same bullet below.

| | |
|---|---|
| **Task** | [`T-203`](../tasks/T-203-honeycomb-station-lattice.md) — what attachment lattice does a honeycomb block's top face offer? |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (a lattice census derived from the primary rules) **+ in-silico** (the snapped re-grading, paired on one dropout stream) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Verdict** | **PASS on all four predicates. Neither falsifier fired.** `F1`: every one of `C-0118`'s path counts fits, with margin. `F2`: snapping to the ladder does **not** destroy the flatness — 4 of 4 `10 × 6` cells stay flat and 3 improve. So `C-0118`'s path counts stop being a *request* and become a **demonstration on the count**, with the placement checked rather than assumed. |
| **Provenance** | [`gpd/results/T-203-honeycomb-station-lattice.json`](../results/T-203-honeycomb-station-lattice.json), produced by `tile.HoneycombStationLatticeStudyKt`; model [`tile/HoneycombStationLattice.kt`](../../src/main/kotlin/tile/HoneycombStationLattice.kt), tests [`tile/HoneycombStationLatticeTest.kt`](../../src/test/kotlin/tile/HoneycombStationLatticeTest.kt) (8, written first and watched to fail). |
| **Conditions** | Honeycomb at 10.5 bp/turn, 112 bp span, `d` = 2.536 nm; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V; `C-0017`'s mandate with equal springs; `C-0087`'s measured dropout, 4 000 realisations, seed 203203, the even and snapped grids sharing one stream. |
| **Consumes** | [`C-0119`](C-0119-honeycomb-raster-width.md) (the primary honeycomb rules), [`C-0118`](C-0118-coupled-four-layer.md) (the cells whose demand this censuses), [`C-0120`](C-0120-cross-section-comparison.md) (the two cross-sections), [`C-0087`](C-0087-position-dependent-staple-dropout.md), [`C-0022`](C-0022-tile-edge-load-profile.md) |
| **Constrains** | Nothing numerically. **No claim is contradicted and no challenge is raised** — `C-0118`'s caveat is *discharged*, which is a strengthening of that claim rather than a correction to it. |

---

## 1. The square lattice does not transfer, and the trap is inside one sentence

| | square lattice | honeycomb |
|---|---|---|
| azimuths per helix | 4 | **3** |
| next crossover position on a helix | 8 bp | **7 bp** |
| the same adjacent pair again | 32 bp | **21 bp** |

**An attachment roots on ONE azimuth, so its ladder is the 21 bp period and not the 7 bp step** — a factor of
three, and both numbers appear in the same sentence of the source:

> *"potential staple-crossover positions occur every **seven base pairs**, or two-thirds of a turn"* … *"which
> repeat every **21 base pairs**"*

Asserted as a test (`SAME_PAIR_PERIOD_BP == ANY_AZIMUTH_STEP_BP × AZIMUTHS`) rather than left to the reader.

---

## 2. The census, and the finding nobody was looking for

Only the **top face** counts: a buried helix has all three azimuths occupied by neighbours, so it has no free
direction to root on. That is the slab analogue of the square lattice's *"a single-layer sheet occupies two of
its four azimuths and the other two point out of the plane"*.

| | helices | top face | stations/helix | **available** | perpendicular / oblique roots |
|---|---|---|---|---|---|
| `15 × 4` | 60 | **15** | 6 | **90** | 8 / 7 |
| `10 × 6` | 60 | **10** | 6 | **60** | 5 / 5 |

**A deeper block offers fewer stations.** At a fixed 60 helices the census is set by the **face**, not the
helix count — so `10 × 6`, the cross-section that is 6.6× flatter and threshold-free, supplies **60** where
`15 × 4` supplies **90**. **A thicker tile buys rigidity and spends attachment lattice**, which is the opposite
of what a reader expects and is the one place `C-0120`'s recommendation costs something structural rather than
specificational.

It is not binding here — `10 × 6`'s best cell needs **ten** — but it is the axis on which a future coupling
could run out of room, and nothing before this claim would have shown it.

---

## 3. Every demand fits, and snapping does not cost the flatness

| | paths | of | fits | even-grid p90 | **snapped p90** | |
|---|---|---|---|---|---|---|
| `10 × 6`, 1 col | 10 | 60 | yes | 0.027843149 | **0.030703735** | **flat** |
| `10 × 6`, 2 col | 20 | 60 | yes | 0.054919090 | **0.057825814** | **flat** |
| `10 × 6`, 3 col | 30 | 60 | yes | 0.046080098 | **0.0434052379** | **flat** |
| `10 × 6`, 5 col | 50 | 60 | yes | 0.040313225 | **0.036806057** | **flat** |
| `15 × 4`, 1–5 col | 15–75 | 90 | yes | 0.121968651–0.153785450 | 0.113942541–0.157439649 | not flat either way |

**All four `10 × 6` cells stay flat on the honeycomb's own ladder, and three of the four improve** — snapping
is not a cost at all there. The `15 × 4` rows are not flat on either grid because this study grades **equal
springs only**; `C-0118`'s single flat `15 × 4` cell used the rim grading, which is not re-graded here.

The even-grid readings are re-derived under this study's own seed and the paired comparison shares it, so the
snapped/even difference is a design difference and not two draws.

---

## 4. A defect found by this claim's own link check, and the checker that now guards it

Verifying `C-0122`'s own cross-references turned up a broken one — `C-0022-tile-edge-load.md` for what is
actually `C-0022-tile-edge-load-profile.md`. A corpus-wide sweep then found **30**, in claims, challenges,
task files, a manifest and `gpd/README.md`, accumulated across many iterations. **All 30 are repaired and
[`tools/check-corpus-links.py`](../../tools/check-corpus-links.py) now guards the class**, with 11 self-tests
wired into `./gradlew test` and the check itself in `tools/verify.sh` beside the table checker. The corpus
reads **0 broken links in 362 files**.

It is `C-0083`'s Markdown-table class exactly: invisible to the numeric trace, the status check and the
self-consistency check; silent at the point of writing; and cheap to mechanise. `CLAUDE.md` already records
the general form — *"a defect's LOCATION is a number like any other … grep the string out of the tree before
accepting a filename from a claim"* — and this makes it a gate rather than a discipline.

**Two of the thirty were mine, written earlier in this same session** (`C-0116` and `C-0120`), which is why
they were found: the ad-hoc link check I had been running matched `../claims/…` and **not** same-directory
links, so it reported *"none"* on files that had them. **The first version of the shipped checker was wrong
too**, and in a way worth recording: it special-cased `../../` as *"the repository root"*, which is true for
`gpd/claims/x.md` and false for a manifest two levels deep — 15 false positives on its first real run.
Replaced by one `normpath` resolver against the file's own directory, with no depth assumptions.

---

## 5. Validity range, and what this does NOT establish

- **It counts STATIONS, not a placement.** Whether a chosen subset is centro-symmetric, clear of the seam, or
  compatible with the scaffold raster is not asked. A census without a routing is well posed for the count and
  not for the design.
- ~~**Half the top-face helices carry only OBLIQUE free azimuths** (7 of 15, 5 of 10)~~ — **withdrawn by
  [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md)/[`CH-0175`](../challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md),
  which finds *every* face helix carrying exactly one rooting azimuth at 30° and **no perpendicular root anywhere** —
  and **this corpus has
  never priced an oblique attachment against a perpendicular one.** ~~The count is unaffected — every top-face
  helix has exactly one free direction either way~~ — but the *stiffness* of a root on an oblique azimuth is an
  open question this claim raises and does not answer.

  > **Annotated, iteration 28 (`C-0128`, [`CH-0151`](../challenges/CH-0151-an-oblique-helix-has-two-free-azimuths-not-one.md)).**
  > The struck clause is withdrawn: an **oblique** helix has **two** free azimuths and a perpendicular one has
  > one, and the two 21 bp ladders of an oblique helix are offset by 7 bp so they interleave rather than
  > collide. Counting both gives **132** stations on `15 × 4` and **90** on `10 × 6`, **1.46666667×** and
  > **1.5×** the table above. **No verdict here moves and two findings are strengthened** — all 8 demands still
  > fit, with more margin, and *a deeper block offers fewer stations* survives (90 against 132). The numbers
  > **90** and **60** remain correct as the count at **one azimuth per helix**, which a design may prefer:
  > two roots 7 bp apart on one duplex is the domain length Ke et al. report as a folding-yield cost.
  > The **stiffness** question this bullet raises is answered by `C-0128`.

  > **Annotated again, iteration 33 ([`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md), [`CH-0175`](../challenges/CH-0175-the-face-azimuth-is-thirty-degrees-and-there-is-one-of-it.md)).**
  > **`CH-0151`'s correction is itself withdrawn and this claim's 90 and 60 are restored, at
  > departure `0.0`.** A free azimuth is a lattice neighbour that is **absent**, and on a full
  > `m × n` block the oblique sublattice's `±60°` pair points at the **other** sublattice's helices
  > in its own x-raster row, which are present. The real face — the one normal to the thin
  > cross-section direction — gives every one of its `m` helices **exactly one** rooting azimuth, at
  > **30°** from the normal with the sign alternating. So this claim's **count** stands and its
  > **perpendicular/oblique split** does not: **there is no perpendicular root anywhere on the
  > face**. The `acrossHelixPitch` emitted here is `d`; the face's station pitch is `3d/2` = 3.804 nm
  > ([`CH-0174`](../challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md)).
- **Equal springs only**, as `C-0118`'s best cells use.
- **Nothing here re-derives a rigidity, a threshold, a collar or a dishing field**; it re-grades on moved
  stations using machinery those claims established.
