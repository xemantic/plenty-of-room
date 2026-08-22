# DECISIONS-FOR-NDI

~~Six~~ ~~**Seven**~~ **Eight** decisions this programme cannot make for itself.
Six were sent on 2026-08-18 and all six came back in one pass;
the seventh is §7, raised in iteration 26 and **re-posed as 7a/7b in iteration 35**;
the **eighth is §8, raised in iteration 36** ([`T-242`](TASKS.md),
[`C-0146`](gpd/claims/C-0146-coupled-cells-at-the-two-length-raster.md)) —
*which of two width readings of the same folded block is the one §3's `40 × 40 nm` names.*
**Those are the questions here still outstanding.**

> **ALL SIX OF THE SIX THAT WERE SENT ARE ANSWERED — 2026-08-18, Jeremy Barton (NDI), by email through Kazik.**
> The answers are quoted verbatim in the block below and again in each section,
> and the interaction is recorded in [`JOURNAL.md`](JOURNAL.md).
> ~~**Nothing has been re-derived yet.** What follows records the answers, names what each one changes,
> and queues the work they open as `T-191`–`T-194`.~~
> **CORRECTED, iteration 27** ([`C-0124`](gpd/claims/C-0124-decision-file-drift.md), `T-184`) — **that sentence was true for about a day.**
> The work the answers opened is `T-191`–**`T-195`**, not `T-191`–`T-194`; ~~**four of the five are `DONE`**~~
> **ALL FIVE ARE `DONE`** (`T-195` closed hours later the same iteration, `C-0125`),
> and ten claims re-derived the line they opened across iterations 23–26 —
> [`C-0109`](gpd/claims/C-0109-four-layer-tile.md), [`C-0110`](gpd/claims/C-0110-device-b-tall-gap.md),
> [`C-0111`](gpd/claims/C-0111-gold-electrode-pzc.md), [`C-0114`](gpd/claims/C-0114-one-reserve.md),
> `C-0116`, `C-0118`, `C-0119`, [`C-0120`](gpd/claims/C-0120-cross-section-comparison.md), `C-0122` and `C-0123`.
> ~~`T-195` — the unpaired scaffold remainder as a body in the actuated gap — is the one still `TODO`~~ —
> **and it closed within hours of that sentence being written** ([`C-0125`](gpd/claims/C-0125-scaffold-remainder.md)),
> which is worth leaving visible: **this correction went stale faster than the text it corrected.**
> The remainder is **840 nt** (the 10 × 6 cross-section is folded from p7560, not M13mp18's 7 249 or the
> ~~15 × 4 design's p8064~~), and the four-layer tile had **already paid for it before the question was asked** —
> its gap-facing wall sits at **0.966331968** of the 2:1 saturated amplitude, so smearing the *entire*
> remainder onto it moves `σ_eff` by **0.0350791486** against **0.537733246** on the single-layer sheet,
> **15.3291419×** less and inside `C-0008`'s own charge-reading ambiguity.
> **AND THE SCAFFOLD ITSELF IS NOT ESTABLISHED, iteration 34**
> ([`CH-0180`](gpd/challenges/CH-0180-the-scaffold-pairing-contradicts-its-own-paper.md),
> [`C-0144`](gpd/claims/C-0144-honeycomb-correction-supersession.md)), which is why *"the 15 × 4 design's p8064"*
> is struck above. The caDNAno paper's Methods list says **p8064**; the paper's own main-text rule — 60 helices
> take p7560, 64 take p8064 — says **p7560**, and the two agree at **six of that paper's seven designs and
> disagree at exactly one, ours**. Under the Methods reading design (i) would leave **504 nucleotides** the
> paper's own *"126 bases per helix"* accounting has no line for. **A source contradiction, not a reading
> error**, and it is half of the re-posed §7.
> This file never named `T-195`, and the note that said so is now the thing that needed correcting.
> **So what follows records the answers AND what the programme has measured against them since.**
> The analysis under each decision is kept as written — struck where an answer or a measurement supersedes it,
> intact where it does not — because a decision file that only ever grows is not a record.

Each is a **specification** question rather than a modelling one:
no amount of calculation closes it,
because the thing that is missing is a statement of intent about the device,
not a number about the physics. 
They are collected here so they can be reviewed together and answered in one pass,
rather than one at a time across iterations.

~~They are live in [`TASKS.md`](TASKS.md) as `T-63`, `T-115`, `P-13`, `T-112`, `T-154` and `T-166`,
and this file is the reviewable form of the same six.~~
**CORRECTED, iteration 27.** All six of those rows now read **ANSWERED** in [`TASKS.md`](TASKS.md); none is live.
The seventh is carried in the queue as *Open questions for Kazik* item 12 rather than as a task row of its own,
its analysis task `T-199` being closed.
**The eighth is item 13, and it has a task row too — `T-242` — because unlike the seventh it was raised as a
task and not as a recommendation against itself.**

## The answers, and the three things that matter more than the answers

| # | asked | NDI's answer, verbatim | what it is |
|---|---|---|---|
| 1 | 0.5 mM MgCl₂? | *"concerningly below the typical experimental stability window of DNA origami … pushing a parameter hard that I've been reserving for additional operating margin. So… well identified."* | **PRICED, NOT GRANTED.** Reachable, and it spends a reserve NDI is holding for margin |
| 2 | a 17–26 nm layer? | *"beyond the regime I've bothered to examine … an interesting regime we've been reserving, again, for low MgCl₂ concentrations"* | **NOT EXAMINED — and contingent on the SAME reserve as decision 1** |
| 3 | which electrode? | *"Defaulting to template stripped gold for initial experiments."* | **ANSWERED** for the material. ~~The potential of zero charge is not given~~ — not by NDI; `C-0111` has since found it in the literature, and the residue is the **cell's** zero, not the electrode's |
| 4 | one device or two? | *"2 devices."* | **ANSWERED** |
| 5 | which scaffold? | *"M13, circular ~7-8K nucleotides … To use excess scaffold, just make the tile thicker. The 1700 nucleotide structure the agent is proposing seems… thin and low stiffness"* | **ANSWERED**, and it carries a criticism of the tile this programme has been modelling |
| 6 | a two-layer tile? | *"just make the tile thicker"* | **ANSWERED BY IMPLICATION — yes, and volunteered rather than granted** |

**1. Decisions 1 and 2 are ONE decision, this file asked them as two — and the reserve has only ONE
claimant, because we measured the other and it buys nothing.**
Both answers name the same reserve — *"additional operating margin"* bought with
*"additional work on stabilizing DNA origami at low salt"* — so the buffer and the tall layer are two
claimants on one budget line and NDI is entitled to spend it once.

~~Of the two spends, the one this programme can price is the layer — it buys a whole clause of §3, where the
buffer buys 1.35–1.75× that is smaller than the error bar it is quoted inside.~~
**WITHDRAWN, 2026-08-18, by our own measurement** ([`C-0110`](gpd/claims/C-0110-device-b-tall-gap.md),
[`C-0114`](gpd/claims/C-0114-one-reserve.md)).
We wrote that before anything in this programme had ever evaluated the **bias** at a tall gap.
Now that it has: **§3's 100 pN stops arriving across a gap of 13.6989179 nm at 0.5 mM** — *below the bottom*
of NDI's own 17–26 nm band, 1.241× short at 17 nm and 1.898× at 26 — so a tall layer is refused at
**96 of 96** states on §3's *acceptable* clause and admitted at **1 of 96** on the *desired* one, which is a
bracket disagreement rather than a design.
**A tall layer does not trade device A for device B. It loses both.**

What `C-0050` priced is real and it is a **displacement**: the uncoupled tile reaches a 10 nm stroke at
**52 of 96** tall states. The force does not follow it
([`CH-0127`](gpd/challenges/CH-0127-the-tall-layer-escape-is-kinematic-not-actuated.md)).

**So the ranking is not a ranking any more.**
The reserve's one remaining claimant is the buffer, worth up to **1.75×** at the state the device occupies
on **three** surviving routes (`C-0091`) that are **common mode** below `C-0005`'s 123–214 % one-loop
error — an error larger than any of them, so they do not diversify the exposure and must not be counted as
three pieces of evidence.
**RESTATED, 2026-08-19 (`CH-0167`, `C-0137`, `C-0139`): that percentage is an error bar on the LEVEL of the
electrostatic force, and all three routes are read at a FORCE-PINNED operating point, where a level
correction is absorbed into the bias rather than into the margin.**
The common-mode statement survives — what these three routes share is one field model — and its same-kind
measure is `C-0137`'s: a force **1.48–2.22× smaller**, or a decay length **9.73 % shorter**.
Nothing evaluable reaches either.
**And that is a ranking of the two BUYS, not of the two PRICES; only NDI holds the second column** — the
error the answer to decision 1 caught, and one this file will not repeat.
The re-issued question is in **§1+2** below.

**2. §3 specifies a ~10 nm tile and this programme has modelled a 2 nm one.**
§3's parameter row reads *"Tile thickness ~10 nm (single-layer honeycomb)"*, which cannot hold both ways —
`electrostatics/DnaOrigamiTile.kt` and `C-0021` both record the contradiction and carry **both** readings —
and every structural claim in the corpus took the thin one.
NDI's answer resolves it toward the thick reading, from a direction nobody asked about: the **scaffold**.

**3. M13 pays for exactly that tile.**
`C-0086` measures the sheet at **1 680** of M13's **7 249** nt — **4.31×** — and NDI's remedy for the excess is
*"just make the tile thicker"*.
Four layers at the honeycomb rise is ~~**9.61 nm**~~ **9.608 nm**, measured by
[`C-0109`](gpd/claims/C-0109-four-layer-tile.md), which now owns that number,
and `Gen1Tile.kt`'s own variant is already documented as
*"the ~10 nm thickness §3 also states"* — the reading was built, named and then never used.
So three independent statements — §3's thickness row, M13's 4.31× excess, and NDI's answer — describe one tile,
and it is not the tile the flatness negative was derived on.
~~`C-0006`'s own variant table already carries it: a four-layer honeycomb sheet has `D_∥` = **14 310.78 pN·nm**
against 85.50 and `D_⊥` ≥ **19.222** against 3.345, i.e. **167×** and **5.75×**.~~
**That pair is under [`CH-0124`](gpd/challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md), raised by `C-0109` and open**:
it is a **mixed state** — the composite ceiling on one axis and the independent floor on the other — not a bracket end.
`C-0109` measures the calibrated pair at `D_∥` = **4 547.17603** and `D_⊥` = **240.931249** pN·nm.
The Winkler reach is a fourth root of those, so `C-0058`'s 12.83 and 5.71 nm become ~46 and ~8.8 nm against a
38–40 nm tile — the regime in which a tile stops dishing because it is rigid on its own scale.
~~**That is a cheap bound and not a result**; it is queued as `T-191`, and it is the first unspent design axis
since iteration 20.~~
**`T-191` RAN, iteration 23, and the cheap bound became a result** ([`C-0109`](gpd/claims/C-0109-four-layer-tile.md)):
the four-layer tile dishes ~~**0.0577199433**~~ of the stroke under the same solved collar **with no coupling at all**,
inside `T-5b`'s 0.10 and against the single layer's **0.307902368**.
**The axis is spent**, and §7 is where it ended.
**RESTATED, iteration 35** ([`C-0141`](gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md),
[`CH-0174`](gpd/challenges/CH-0174-the-four-layer-cross-section-is-not-a-honeycomb.md)):
**the cross-section that number was solved on is not a honeycomb.**
A honeycomb's in-plane row pitch is `3d/2` and its layer pitch `d√3/2`, and only their **product** is the cell,
so every four-layer `edgeY` in this corpus was exactly **1.5×** too small.
Corrected, `15 × 4` dishes **0.0978155002** and `10 × 6` dishes **0.0240648102** — both still inside `T-5b`'s 0.10 uncoupled —
but `15 × 4`'s interlayer-coupling threshold moves to **0.276970522**, **inside** the measured 0.26–0.33 band,
where it dishes **0.101759944** and **fails**.
So *"the four-layer tile is flat with no coupling at all"* is a statement about **`10 × 6`**, and this file states it of the four-layer tile
([`CH-0181`](gpd/challenges/CH-0181-the-supersession-sweeps-own-ground-moved.md), which finds `C-0126` making the same substitution in the queue).


## How to read this

For each decision: what is being asked, why the programme cannot decide it,
what has been established either way, the options with their consequences,
what the programme would recommend and on what grounds,
and **what it costs to leave it open** — which is the part that is easy to miss.

~~**A one-sentence answer is enough for all six.**
None needs a document; three of them need a single word.~~
**WITHDRAWN AS A FRAMING, iteration 36 — and the withdrawal is the lesson, not the count.**
All six were answered in one pass and **three of them came back in a shape this document had not offered**:
a *price* (decision 1 — *"pushing a parameter hard that I've been reserving"*),
a *contingency on another decision* (decision 2 — the same reserve as decision 1),
and an answer volunteered inside a **different** question (decision 6, answered inside decision 5).
**The three that came back in the offered shape are the three with no cost dimension** — a material,
a device count and a scaffold.
A question whose admissible answers are enumerated cannot return the one that was not enumerated,
and the missing shape is a **price**.
So every question below now carries its price line **explicitly open**:
*and if this costs something, what?*

**UPDATED, iteration 26 — there is now a SEVENTH, and it is the only one this programme raised against its
own recommendation.** Decisions 1–6 were answered on 2026-08-18 and the work they opened ran four claims
further than anyone expected. It ends at a tile that is ~~**not** the one §3 describes~~ **0.929467162 of §3's
footprint, i.e. essentially the one §3 describes** (`C-0141`): `10 × 6` is ~~6.6×~~ **4.06×** flatter
than the four-layer tile now recommended — 0.0978155002 against 0.0240648102, a ratio constructed here and
stated by no claim — has **no** coupling-fraction threshold at all — so it removes the
last unmeasured dependency in the flatness verdict rather than bounding it — and is what the caDNAno paper's
own folding measurements recommend. ~~It costs **a third of the footprint**.~~
**WITHDRAWN, iteration 35** (`C-0141`, `C-0144`): **it costs no footprint at all.**
Corrected to the honeycomb's own two pitches, `10 × 6` is **38.08 × 37.504 nm — 0.929467162 of §3's 40.35 nm** and
`15 × 4` is **38.08 × 56.524 nm — 1.40084263×** it, so the ordering **reverses**: the tile §7 offered as the *cost*
is the one that fits, and the default it offered is the one that overruns §3 by 40 % **and stops being flat**.
That is §7, and **the question there is superseded as posed** — what is left is a confirmation and a new question about
the scaffold, not a trade.

**UPDATED AGAIN, iteration 36 — there is now an EIGHTH, and it is the first that this programme has no
recommendation for at all.** Decision 7's answer makes the tile a two-length honeycomb raster, and a
two-length raster has **two true widths**: the ~~112 bp = 38.08 nm~~ **`109 bp`** span every one of its rows has, and the
116 bp = 39.44 nm box the folded block occupies. §3 states one number. The difference between them is the
**inter-row stagger** — ~~3.57 %, a **1.36 nm**~~ **`7 bp = 2.38 nm` at the drawable raster** — which no model in this repository can represent, so nothing here can break the
tie ~~— and it is worth **six flat coupled cells of eight against three**, through a crossover column decided by
a 0.07 nm slack~~ ([`C-0146`](gpd/claims/C-0146-coupled-cells-at-the-two-length-raster.md),
[`CH-0185`](gpd/challenges/CH-0185-a-bounding-box-crossover-column.md)). That is §8.

**UPDATED AGAIN, iteration 38 — the recommended raster moved and §8's PRICE went to zero.**
The two-length pair is now **`102 / 109 bp`**, not 112 / 108, because 112 / 108 does not close on caDNAno's own
`±5 bp` scaffold-crossover rule and would need **10** of its 59 raster crossovers **forced**
([`C-0151`](gpd/claims/C-0151-closing-raster-selection.md),
[`C-0148`](gpd/claims/C-0148-face-bond-class-residues-and-row-span-columns.md)).
The **bounding box is unchanged** at 116 bp = 39.44 nm, so §7's width finding does not move; the row span
becomes `109 bp` and the whole cost of closure is **one crossover column**.
And §8's threshold goes with it: the eleventh and twelfth columns both belonged to the undrawable pair
([`CH-0195`](gpd/challenges/CH-0195-both-graded-column-counts-belong-to-an-undrawable-raster.md)), while the
drawable pair reads **10** columns at every guard convention — so **§8 is now a drafting question with no
margin on either side of it.**

**Nothing here is measured.** Every number is TRL 1–3:
model-consistent and traceable to a claim, never empirically demonstrated.

## At a glance

| # | Decision | Owner task | Cost of deferring | Programme's recommendation | **NDI's answer (2026-08-18)** |
|---|---|---|---|---|---|
| 1 | Is 0.5 mM MgCl₂ acceptable as the Gen-1 nominal buffer, instead of §3's 2 mM? | `T-63` | ~~The device sits **on** its own pull-in fold at 2 mM~~ — **withdrawn, `C-0084`**; the recommended element does not fold at 2 mM at all, on **any** elastica branch (`C-0092`). The cost of deferring is now a **margin**, 1.39× against 1.87× at worst, not a failure | **Adopt 0.5 mM — but as a PREFERENCE, not a requirement, and on THREE routes rather than six (`C-0091`)** | **PRICED, NOT GRANTED** — reachable, at the cost of origami stabilisation work and a reserve held for margin. 2 mM stays the nominal; `T-50` is **not** deleted |
| 2 | May the polymer layer be taller than 10 nm — 17–26 nm? | `T-115` | §3's *desired* ~10 nm stroke stays unreachable on §3's own stack | ~~**A yes/no is enough**~~ — **the branch is now closed by measurement, and the answer does not depend on the ruling** | **NOT EXAMINED**, and it draws the **same** reserve as decision 1. **We then measured it: a tall layer delivers §3's 100 pN across NO gap in NDI's band** (`C-0110`), so the reserve has one claimant and 1+2 collapse to the buffer alone |
| 3 | What is the electrode made of, and where is its potential of zero charge? | `P-13` | 2.6× on the one hold-down no design can remove | **Name a material**; the programme will not choose one | **TEMPLATE-STRIPPED GOLD.** The 2.6× bracket collapses onto its adverse end. ~~The **PZC is still not given**~~ — **corrected, iteration 27**: NDI did not give it and [`C-0111`](gpd/claims/C-0111-gold-electrode-pzc.md) has since read it out of the literature (**0.46–0.51 V vs SHE**, Au(111)). What is still open is not the electrode's PZC but **the cell's definition of zero bias**, which decides the *sign* of the force at rest |
| 4 | Which device does §3's *desired* clause ask for? | `T-112` | Every desired-stroke number in the corpus has to be quoted twice | **One device, placed at the acceptable clause** | **TWO DEVICES.** Device B is placed at 10 pN/nm and its binding constraint becomes `C-0017`'s stability floor |
| 5 | What is the scaffold — linear or circular, M13 or synthetic, and how long? | `T-154` | A seam costs 6–12 of 34 arm stations, and §3's 40.0 nm is not a buildable seamless width | **A purpose-length scaffold**; the choice is a fabrication decision, not a modelling one | **CIRCULAR M13, ~7–8 k nt** — so the remainder is the DEFAULT — and the remedy for the excess is *"just make the tile thicker"* |
| 6 | Does §3 admit a **two-layer** tile, or is single-layer a requirement? | `T-166` | The only coupling topology that narrows the flatness negative is multilayer, and it is the one motif here with a published precedent | **A yes/no is enough**; a *no* closes the last open recovery route | **YES, BY IMPLICATION**, and volunteered rather than granted. Reopens the body axis (`T-191`), not the coupling axis |
| **7** | ~~**NEW — is a 38 × 25 nm tile acceptable, in exchange for removing the last unmeasured dependency?**~~ **SUPERSEDED AS POSED, iteration 35** (`C-0144` §4, on `C-0141`) — **RE-POSED as two questions: confirm `10 × 6`, and name the scaffold** (§7) | item 12 of the queue's questions for Kazik — the only row here whose owner column names a decision rather than a task, its analysis having closed in iteration 25 (§7) | ~~Carrying the interlayer-coupling calibration as a live dependency on the flatness verdict, and the coupled result at 1 of 8 cells rather than 8 of 8~~ — **the cost of deferring has REVERSED SIGN**: the tile this question offered as its *default*, `15 × 4`, is **1.40084263×** §3's footprint, **fails** `T-5b` at the measured band's adverse end (**0.101759944**) and is **0 of 8** coupled cells at both ends of that band | ~~**`10 × 6` on both flatness and published folding yield — but the footprint is a specification and only NDI holds that column**~~ **`10 × 6` on all THREE criteria this programme can rank — flatness, published folding yield and now footprint, which is 0.929467162 of §3's. There is no trade left to weigh** — **and since iteration 39 the block is a committed scadnano FILE that the reference implementation loads with zero warnings and that reads `ADMISSIBLE` at ZERO forced crossovers** (`C-0160`, `C-0164`), so a *yes* is now given against an object rather than against a pair of integers. It carries **no staple set** and `ADMISSIBLE` is a statement about this repository's rules, not about foldability | *raised iteration 26; **superseded as posed and re-posed iteration 35**; **updated iteration 39**; awaiting* |
| **8** | **NEW — which width reading is the Gen-1 tile SPECIFIED to: the ~~112 bp = 38.08 nm~~ **`109 bp`** span every one of its rows has, or the 116 bp = 39.44 nm box the folded block occupies?** (§8) | `T-242`, and item 13 of the queue's questions for Kazik | ~~The two readings are **3.57 %** apart and the difference is a **stagger**, not a length. Deferring means a four-layer flatness verdict decided by a **0.07 nm** slack against a numerical guard — **6 flat cells of 8 against 3** (`C-0146`, `CH-0185`)~~ **RESTATED, iteration 38 (`C-0151`, `CH-0195`): the difference is the `7 bp = 2.38 nm` stagger of the DRAWABLE `102 / 109` raster, and the flatness threshold is WITHDRAWN — the eleventh and twelfth columns both belonged to a raster that does not close, and the drawable one reads 10 columns at every guard convention.** What deferring costs is now the drafting ambiguity alone — every plan margin in the corpus quoted at whichever width its author happened to take | **None. This programme cannot rank them**, because no model here has a per-row row length and both readings are true of the same object. What it can do is say what each costs, and ask **what a ~~`1.36 nm`~~ `2.38 nm` stagger costs NDI**. **RE-READ against iteration 39 (`C-0160`, `C-0164`): nothing moves it** — the block is now a file and **both** readings are re-derived on that same file, so drawing the object did not choose between them | *raised iteration 36; **re-read iteration 39**; awaiting* |

---

## 1. Is 0.5 mM MgCl₂ acceptable as the Gen-1 nominal buffer? (`T-63`)

> ### ANSWERED, 2026-08-18 — PRICED, NOT GRANTED
>
> **NDI, verbatim:** *"0.5 mM MgCl2 is concerningly below the typical experimental stability window of DNA
> origami. There are routes to stabilize against that kind of concentration, but's pushing a parameter hard
> that I've been reserving for additional operating margin. So… well identified."*
>
> **What that is.** Neither *yes* nor *hold 2 mM*.
> 0.5 mM is reachable and it is **costed**: it needs origami stabilisation work, and it spends a reserve NDI
> is holding for operating margin. Neither of those is a quantity this programme can compute.
> The ask is granted as *well identified* and refused as free.
>
> **What it changes.**
>
> - **2 mM stays the nominal.** Every margin in the corpus is owed at 2 mM, and 0.5 mM travels beside it as an
>   option with a named price rather than as a recommendation.
> - **`T-50` is NOT deleted.** This file said the one-word answer would delete it and that only *"hold 2 mM"*
>   would keep it. The answer is neither word, so the last unbounded exposure on the Gen-1 critical path —
>   `C-0005`'s `Ξ = 17–24`, where there is no systematic theory at all — stands exactly where it was.
>   That is the one prediction in this file the answer falsified.
>   **AND THEN IT WAS DONE, 2026-08-19 (`C-0137`), without the Monte Carlo.** `Ξ = 17–24` still has no
>   systematic theory; what changed is that the 123–214 % turned out to be an error bar on a **level**, and
>   at the force-pinned operating point every margin here is read at, a level is absorbed into the bias.
>   The exposure is bounded and the compute request is withdrawn. See decision 1's answer block above.
> - **The buy is small, and it is common mode.** At the state the device occupies the three surviving routes
>   are worth **1.35×, 1.57× and 1.75×** (`C-0091`), and all three are downstream of `C-0008`'s single
>   mean-field solve, whose one-loop error is **123–214 %** (`C-0005`) — larger than any of them.
>   (**RESTATED, `CH-0167`/`C-0137`**: a LEVEL, not the error bar on a margin. The common-mode exposure is the
>   *gradient*, whose same-kind threshold is a decay length **9.73 %** shorter.)
>   **Against a reserve NDI says it is holding for margin, this programme cannot argue that its own
>   1.35–1.75× is the better use of it** — and it now knows what the other claimant on that reserve is.
> - **The other claimant is decision 2**, which buys a whole clause of §3. See the block at the head of this
>   file: the two decisions are one, and the recommendation between them is the layer.

> ### CORRECTED, iteration 17 — this decision's severity is withdrawn, its recommendation stands
>
> **`C-0084` ([`CH-0098`](gpd/challenges/CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md)) removes the word *requirement* from this decision.**
> Everything below about *"the device sits on its own fold at 2 mM"* is `C-0032`'s finding about
> `C-0030`'s strain-**softening** coupled-standoff flexure — **an element `CH-0081` has since removed
> from the output role.** The element the programme actually recommends (`C-0071`'s `Q5`) is
> strain-**stiffening**, and at 2 mM it has **no pull-in fold at all**, at **0 of 6** layer models
> where the affine mandate folds at 6 of 6. On the bias axis its margin is **1.3877–7.3137** at 2 mM
> against **1.8706–10.9072** at 0.5 mM (the *tops* widened by `C-0092` in iteration 18, which found
> `C-0084`'s branch end to be a force-ladder artefact; both **minima** are unchanged); on the stroke axis the fold moves from 3.4104–4.1248 nm to
> **past 7.9097 nm**, and the binding ceiling changes owner from pull-in to `C-0002`'s `φ = 0.2`.
>
> **So 0.5 mM remains the recommendation and stops being a requirement.** It is bought for margin
> — a 1.4–2.6× reserve becomes 1.9–3.5×, and the stability floor falls from 23.41–27.91 to
> 3.86–15.94 pN/nm — rather than to keep the device off a fold it is not on. The 1–3 week Monte
> Carlo alternative (`T-50`) is correspondingly **less** forced than the text below says.
>
> This is `CLAUDE.md`'s own standing warning arriving in the deliverable: *a correction can be
> quoted against a stack that has already left the design.* ~~**A census of how many of the six
> routes below are read on withdrawn objects has never been taken** and is queued as `T-156`.~~

> ### CORRECTED AGAIN, iteration 18 — the count. The recommendation still stands
>
> **`C-0091` ([`CH-0106`](gpd/challenges/CH-0106-six-routes-are-three.md)) took the census, and
> the six independent routes are THREE.** One (`C-0032`) is withdrawn, as above. **Two are the
> other four, read again**: `T-2`'s bias clause carries `T-3`'s own blocking bias at **15 of 15**
> states at a departure of **0.0**, and `T-25`'s buffer comparison carries `T-16`'s and `T-4`'s
> extrema at **20 of 20** comparisons at a departure of **2.66e−08** — which is one file printing
> eight significant digits where the other prints nine. So `C-0016` is `C-0012` on a `σ` grid and
> `C-0027` is `C-0017` and `C-0018` with two corrections applied.
>
> **Two of the three survivors are also weaker than the text below says.**
> `C-0018`'s stated reason — *"0.5 mM removes the fold"* — is **void** on the recommended element,
> which has no fold at 2 mM to remove; it survives as a **1.35×** preference on the bias margin.
> And the *"factor of five"* below is read at **zero stroke**: at the operating point the device
> actually occupies (`L₀ − 3 nm`, 100 pN delivered) the same clause is **1.48–1.57×**.
>
> **Read at the state the device occupies, the three routes are worth 1.35×, 1.57× and 1.75×** —
> and they are **not three independent exposures**: all three are downstream of `C-0008`'s single
> mean-field solve, whose one-loop error (`C-0005`, **123–214 %**) is common mode over all of them
> and larger than each. ~~`T-50` is the only thing in the queue that would change that.~~
> **`T-50` DID change it, 2026-08-19 (`C-0137`, and `CH-0167` against the way it was being quoted):**
> the common-mode 123–214 % is a **level**, and the three
> routes are read at a **force-pinned** point where the level is absorbed into the bias. The common-mode
> exposure is the *gradient*, whose threshold is a 9.73 % shortening of the decay length — and nothing
> evaluable reaches it. The three routes are still not three independent exposures; what they share is now
> bounded.
>
> **Nothing here argues for 2 mM.** Every surviving route favours 0.5 mM, at every layer model.
> What is corrected is the count, the word *independent*, and the size of the largest number.

**The question.** §3 specifies 2 mM MgCl₂. Leaf `A2.2` names a low-screening operating point.
~~Six independent routes in this programme have converged on **0.5 mM**,
and as of iteration 5 it stopped being an improvement and became a requirement for the surviving coupling.~~
**Three independent routes converge on 0.5 mM** (`C-0091`), and it is a **preference** rather than a
requirement (`C-0084`, `CH-0098`).

**Why the programme cannot decide it.** Buffer composition is a specification of the experiment,
not an output of the model.

**What is established.**
  
- ~~**Six independent routes recommend it**: `C-0012` on the force clause, `C-0016` on the bias window,
  `C-0017` on the stability floor, `C-0018` on the usable bias, `C-0027` on the corrected margin,
  and `C-0032` on the realised coupling law.~~
  **THREE independent routes recommend it** (`C-0091`, `CH-0106`): `C-0012` on the force clause
  (**1.48–1.57×** at the operating point, 4.97× at zero stroke), `C-0017` on the static stability
  floor (**1.75×**, and the floor itself is element-independent), and `C-0018` on the coupled bias
  margin (**1.35×**, re-read on the recommended element, and on a *different* ground from the one
  `C-0018` states). `C-0016` is `C-0012`'s own number on a `σ` grid, `C-0027` is `C-0017` and
  `C-0018` corrected, and `C-0032` is withdrawn.
- **At 2 mM the device is placed on its own fold.** `C-0017`'s stability margin was banked on the
  coupling being strain-*stiffening*; `C-0030` established that the realised coupled-standoff flexure
  strain-*softens*. Re-running `C-0018`'s fold analysis on that law over 216 states, the 10 nm / 2 mM
  bias margin collapses from 1.007–1.032 to **1.0000–1.0019**, and the fold's stroke walks back from
  3.41–4.13 nm to 2.80–3.17 nm — **through §3's own 3 nm target at two of six layer models**.
- **At 0.5 mM the fold does not exist at all** (0 of 6 states, against 6 of 6 at 2 mM), and the
  corrected stability margin is **2.16–9.87×** against 1.23–1.53×. ~~It is the only margin in this
  repository that clears `C-0005`'s own 123–214 % mean-field error.~~
  **RESTATED, 2026-08-19 (`CH-0167`, `C-0137`): no margin has ever had to clear that percentage, because it
  is an error bar on a LEVEL and a margin is not a level.** What a margin is owed against is `C-0137`'s
  same-kind pair — a force **1.48–2.22× smaller** or a decay length **9.73 % shorter** — and at 0.5 mM the
  gradient threshold is `−0.1477 nm⁻¹`, **3.9× further away** than at 2 mM. The preference for 0.5 mM is
  unchanged; the reason it was being given was the wrong kind of number, and it was the *strongest* sentence
  in this file.
- ~~**It costs nothing.**~~ **It costs nothing THIS PROGRAMME CAN SEE — corrected 2026-08-18, and the
  answer is what corrected it.** `C-0007` puts the layer's buffer sensitivity at **≤ 0.4 %** of the modulus,
  so the window edges, the chain length, the stroke and the chemistry are all unchanged.
- **The obvious escape is priced and fails.** The adverse mounting is ~~42.4–61.0~~ **42.38–61.04** pN/nm, past
  ~~`C-0023`'s 40 pN/nm compliance ceiling~~ at **0 of 8** lengths. A shorter standoff lands 2.2 % short
  at ℓ = 5 nm and is excluded by the 10 pN unzip allowable at ℓ = 4 nm.
  **CORRECTED TWICE, iteration 36, and the verdict survives both times on a different ground.**
  *(i)* `42.4–61.0` is `C-0032`'s **42.38–61.04** rounded, and `C-0067` found and repaired exactly that token
  in `ANSWERS.md` **in iteration 12** — it stood here for twenty-four iterations because this file had no
  checker until `C-0115` gave it one, which is `CLAUDE.md`'s *a checker's default is part of its logic*.
  *(ii)* **`C-0023`'s 40 pN/nm ceiling has been withdrawn** ([`C-0049`](gpd/claims/C-0049-compliance-ceiling-stroke.md)):
  it is exactly `1.2 × (100 pN / 3 nm)`, so it carries the *acceptable* clause's stroke inside it and is
  3.33× too generous when read at the desired one.
  **The adverse mounting still fails, and it fails on `C-0035`'s ground instead** — both adverse mountings
  cannot place §3's own effort point at all. `ANSWERS.md` §1 carries both corrections and this file did not.

~~**The alternative if the answer is "hold 2 mM".** `T-50` — a beyond-mean-field treatment of the
actuated gap, costed at **1–3 weeks of primitive-model Monte Carlo** in a regime `C-0005` reports has
**no systematic theory at all** (`Ξ = 17–24`). It is the last unbounded exposure on the Gen-1 critical
path, and `CH-0019` established that nothing else in the queue can reach it.~~

**ANSWERED, 2026-08-19 (`C-0137`, `T-50` DONE) — and the Monte Carlo was not run.** The exposure was
bounded by an arithmetic instead: `C-0005`'s 123–214 % is a statement about the **level** of the
correction, and at the force-pinned operating point every margin in this file is read at, the level is
absorbed into the bias (checked over `C-0017`'s own 54 states — the pinned force is identical across all
three buffers at every `(model, height)`, relative spread `0.0`). The margin's own thresholds are that
the true force be **1.48–2.22× smaller**, or decay **9.73 % faster** at the 7 nm held gap, and every
channel that can be evaluated is empty, favourable, or worth **1.44 %** of the margin. `C-0005`'s own
open item — *"no published `Ξ` criterion exists for oppositely charged walls"* — is closed in the
**favourable** direction: for oppositely charged walls the weak-coupling expansion has an
*exponentially* larger validity bound, with published Monte Carlo at `Ξ` up to **86** where
Poisson-Boltzmann and strong coupling *"nearly coincide"*. **The `Ξ = 17–24` alarm is calibrated on the
LIKE-charged problem and this device is not that problem.**

**What deferring decision 1 now costs.** Less than this file said. 2 mM's margin is no longer carried
against an uncontrolled 123–214 %; it is carried against a threshold **1.48×** away in the force and
**9.73 %** away in the decay length, with nothing evaluable getting there. `C-0017`'s verdict is still
*not excluded rather than established* — that has not changed — but the reason is now the absence of a
systematic theory at `Ξ = 17–24`, not the absence of a bound.

---

## 2. May the polymer layer be taller than 10 nm — 17–26 nm? (`T-115`)

> ### ANSWERED, 2026-08-18 — NOT EXAMINED, AND IT IS THE SAME RESERVE AS DECISION 1
>
> **NDI, verbatim:** *"17-26 nm of polymer thickness is beyond the regime I've bothered to examine as the debye
> length of operation in 2 mM MgCl2 is only about 4 nm, so that thick polymer is far beyond what we've
> considered. It's an interesting regime we've been reserving, again, for low MgCl2 concentrations we'd buy
> with additional work on stabilizing DNA origami at low salt."*
>
> **What that is.** Not a refusal: an unexamined regime, held behind the same purchase as decision 1.
> So the answer to *"may the layer be taller"* is *"at 0.5 mM, and that is one decision with the buffer"*.
>
> ~~**The reason given is a real objection and this programme has not answered it.**~~
> **It has, in the block immediately below — struck here in iteration 27 because the sentence sat unstruck for four iterations.**
> §3's own parameter row gives ~4 nm at 2 mM and `C-0008` derives **3.93 nm**, so a 17–26 nm layer is
> **4.3–6.6 bulk Debye lengths** of separation, and ~~no claim here has ever evaluated the bias that would
> deliver §3's 100 pN across one.~~ **`C-0110` has, on `C-0008`'s own solver, over 96 states and 12 (gap, buffer) cells.**
> What this programme can say is that *"the"* Debye length is the wrong single number for the question —
> `C-0008` measures **three** in this stack, and the one in the actuated gap is counterion-dominated rather
> than buffer-set — so the objection has to be answered by a field solve at the tall gap rather than by a
> comparison of two lengths. ~~That is `T-192`, it runs on `C-0008`'s existing machinery, and it decides
> whether the reserve is worth spending here at all.~~ **That was `T-192`, and it ran.**
> **And the rebuttal in the sentence before it is the half that lost: counterion dominance is about ion
> CONTENT and never about the DECAY, which is what the objection was made of.**
>
> **What it changes.** The 16.63–26.12 nm threshold below stands as the *kinematic* requirement for a 10 nm
> dead-load stroke; what it does not carry is whether such a layer can be actuated at 2 mM. ~~Until `T-192`
> runs, this decision's *yes* is worth a stroke and an unknown bias.~~
>
> ### `T-192` HAS NOW RUN, AND THE OBJECTION IS UPHELD — 2026-08-18, [`C-0110`](gpd/claims/C-0110-device-b-tall-gap.md)
>
> **The answer is worse than the objection.** Measured on `C-0008`'s own solver, §3's 100 pN stops arriving
> across a gap of **13.6989179 nm at 0.5 mM** (11.8724439 at 1 mM, 10.1299463 at 2 mM) — *below the bottom*
> of the 17–26 nm band, which begins **1.241×** and ends **1.898×** beyond where the 0.5 mM reserve leaves
> it. 100 pN reaches a resting 17–26 nm gap at **0 of 12** (gap, buffer) cells.
>
> **This decision's *yes* is worth a stroke and NO force.** The uncoupled tile does reach a 10 nm stroke, at
> **52 of 96** tall states — so `C-0050`'s escape is real in *displacement* and empty in *force*, a split no
> claim here had made ([`CH-0127`](gpd/challenges/CH-0127-the-tall-layer-escape-is-kinematic-not-actuated.md)).
> §3's *acceptable* clause is refused at **96 of 96** tall states, because a 3 nm stroke from a 17–26 nm
> layer still leaves a 14–23 nm gap.
>
> **And the concession is ours.** This programme's standing rebuttal was that *"the Debye length is three
> numbers here, and the gap's is counterion-set"*. That is about ion **content** — still true, 6.37–38.94× —
> and never about the **decay**, which measures 3.6–6.4 nm against a counterion length of 1.54–1.91.
> Worse, diluting to 0.5 mM makes **NDI's own estimate optimistic** rather than conservative: `ℓ/λ_D` is
> 0.910–0.983 at 2 mM — NDI's ~4 nm is exactly right — and **0.649–0.819 at 0.5 mM**, because the far
> field is reached in `κh` and not in `h`.
>
> **`CH-0126` also falls out of it**: §3's own *"effort point ~20–25 nm above the electrode"* row cannot be
> met by a tall layer at all — the effort point lands at **32–41 nm**.
>
> **So no ruling is needed on this decision.** It is closed by measurement, in the direction that costs
> nothing to accept: a tall layer was the only route to §3's desired stroke, and it is not one.

**The question.** §3 specifies a 5–10 nm polymer layer. Is that a hard bound, or a nominal?

**Why the programme cannot decide it.** It is a stack geometry decision.

**What is established.**

- **A stroke is a compression of the layer**, so `s = L₀ − h < L₀` identically (`C-0050`). With §3's
  `L₀ ≤ 10 nm`, the *desired* ~10 nm stroke asks the layer for `h = 0`.
- The layer height at which a **100 pN dead-load stroke reaches 10 nm** is **16.63–26.12 nm** over
  `C-0003`'s six models at `σ = 0.024 nm⁻²`.
- **§3's own tile row already allows the effort point to sit ~20–25 nm above the electrode**, so the
  geometry is not absurd on its face.
- ~~**But nothing in this programme has evaluated a layer that tall.** A 17–26 nm layer moves
  `C-0002`'s crossover, `C-0005`'s screening validity, `C-0007`'s drainage and `C-0012`'s bias.~~
  **STRUCK, iteration 27 (`C-0124`, `T-184`) — this is the sentence this whole task exists for.**
  [`C-0110`](gpd/claims/C-0110-device-b-tall-gap.md) evaluated exactly that layer in iteration 23,
  and it sat here unstruck in the part of the section a reviewer actually reads while the answer
  block above it carried the measurement.
  **§3's 100 pN stops arriving across a gap of 13.6989179 nm at 0.5 mM**, below the bottom of NDI's band.

~~**Why a *no* is as useful as a *yes*.** A *no* closes the branch immediately and confirms what is
already this programme's most-repeated finding — that §3's desired stroke is unreachable on §3's own
stack, and the acceptable clause (3 nm) is what Gen-1 targets. A *yes* opens real work: it is a
substantial re-run across four claims, not a parameter change.~~
**Neither is needed. The branch is closed by measurement** — `C-0110`, and the direction it closed in
costs nothing to accept.

~~**What deferring costs.** This is the only route in the programme that can buy the desired stroke, so
while it is open the desired-stroke branch cannot be either pursued or closed.~~
**What deferring costs, corrected.** Nothing. It is no longer a route to the desired stroke at all:
it buys the **stroke** at 52 of 96 uncoupled states and **no force** at any of them
([`CH-0127`](gpd/challenges/CH-0127-the-tall-layer-escape-is-kinematic-not-actuated.md)),
and §3's own effort-point row cannot be met by a tall layer either (`CH-0126`).

---

## 1+2 (re-issued as one). The reserve, and its one remaining claimant (`T-194`)

> **This section replaces decisions 1 and 2 as a single decision.**
> They are kept above, unedited apart from struck text, because they are what the answers were given
> against. This is `T-194`, claim [`C-0114`](gpd/claims/C-0114-one-reserve.md), and every number in it is
> derived from a result file by [`tools/T-194-emit-result.py`](tools/T-194-emit-result.py) rather than
> transcribed.

**Why they are one.** Both answers name the same reserve, and the second says so in one word:
*"an interesting regime we've been reserving, **again**, for low MgCl₂ concentrations we'd buy with
additional work on stabilizing DNA origami at low salt."*
The tall layer is available *at* 0.5 mM, and 0.5 mM is bought with origami-stabilisation work.
**We asked as two questions what NDI can only spend once**, and that is this programme's own standing
failure — counting routes that are one number (`C-0091`) — arriving in the deliverable instead of in the
corpus.

**What each spend buys**, each read at the state the device occupies:

| | buffer: 2 mM → 0.5 mM | tall layer: 10 nm → 17–26 nm |
|---|---|---|
| owner | `C-0091` | `C-0110`, filed 2026-08-18 |
| routes named / surviving | 6 / **3** — one withdrawn, **two are the others read again** | — |
| best advantage at the operating point | **1.75104168×** | — |
| weakest surviving advantage | **1.2825845×** | — |
| the same clause at **zero stroke** | 4.96557132× — **not a state the device occupies** | — |
| §3's 100 pN reaches | — | **13.6989179 nm** at 0.5 mM |
| against NDI's 17–26 nm band | — | **1.241× short at 17 nm, 1.898× at 26** |
| §3 **acceptable** clause | delivered | **0 of 96** states |
| §3 **desired** clause (device B) | — | **1 of 96** — a bracket disagreement, not a design |
| stroke alone, uncoupled | — | **52 of 96** — real, and empty of force |

**So the reserve has one claimant.** The layer was withdrawn by our own measurement, not by a ruling.

**And the qualifier on what remains, stated rather than implied.** The three surviving buffer routes are
**not three pieces of evidence**: two of the six named routes are *transfers* carrying the others' own
numbers (departures `0.0` and `2.7e−8`), one is withdrawn, and `C-0005`'s one-loop correction — **123–214 %**
of the leading term — is **common mode to all three and larger than every one of the advantages**.
(**RESTATED, `CH-0167`/`C-0137`**: the count and the common mode stand; the percentage is a LEVEL and these
are margins, so the same-kind common-mode measure is the *gradient* — a decay length **9.73 %** shorter.)

### The decision we are actually asking for

> **Is a preference worth up to 1.75× at the operating point — carried against a common-mode field model
> whose own same-kind threshold is a decay length 9.73 % shorter — worth the origami-stabilisation work
> that buying 0.5 mM costs?**
> (~~*inside a 123–214 % error bar that is common mode to every route recommending it*~~ — **RESTATED,
> 2026-08-19, `CH-0167`/`C-0137`**: that percentage is an error bar on a LEVEL and the 1.75× is a margin
> read at a force-pinned point, so the two are not comparable and the question was harder to answer than it
> needed to be. The buy is still small and still common mode.)
>
> **And if it costs something, what?** That line is left open deliberately: the last time this file offered
> only yes/no shapes, three of six answers came back as a price, a contingency or an answer to a different
> question — and they were exactly the three with a cost dimension.

**Why we cannot answer it.** We can rank what the two spends **buy** and have just done so. We cannot rank
what they **cost**: both are bought with the same unpriced currency, and this programme has a column for
fabrication **yield** wherever it is published and **no column at all** for fabrication **cost**. Writing
*"it costs nothing"* of 0.5 mM — on `C-0007`'s ≤ 0.4 % layer sensitivity — was a statement about the
*modulus* standing in for a statement about the *price*, and the answer to decision 1 caught it.

**What deferring costs.** Nothing new. 2 mM stays the nominal and every margin in the corpus is owed there.
~~`T-50` stays on the queue exactly where it was.~~ **`T-50` is DONE as of 2026-08-19** (`C-0137`), bounded
rather than run — see decision 1.

---

## 3. What is the electrode made of, and where is its potential of zero charge? (`P-13`)

> ### ANSWERED, 2026-08-18 — TEMPLATE-STRIPPED GOLD. The PZC is still not given.
>
> **NDI, verbatim:** *"Defaulting to template stripped gold for initial experiments."*
>
> **What it changes.**
>
> - **The 2.6× bracket collapses onto its ADVERSE end.** Gold is the metal row of `C-0021`'s `M4` table:
>   `A_Au|water|Au` = **238.6–267.9 zJ** against alumina's 36.9, giving a hold-down of **10.4–17.2 pN at 5 nm**,
>   3.0–5.2 at 7 nm and 0.74–1.42 at 10 nm. Those stop being the top of a four-material bracket and become
>   the numbers.
> - **No finding moves.** `C-0021`'s verdict is a statement about the *shape* of a `1/h³` force: it integrates
>   to a **bounded** potential, the well is **4.82 `k_BT`** at 5 nm and **0.65** at 10 nm, and **0 of 54**
>   van-der-Waals-only states confine. A stable equilibrium is still not a confinement.
>   **The answer removes an uncertainty; it does not remove a result.**
> - **One caveat is discharged.** `C-0021` records that its retardation factor is *"sourced for gold only"*.
>   It is now sourced for the material in use.
> - **Template-stripped is a second statement, and it is favourable.** An atomically flat, Au(111)-textured
>   surface is the one case in which a smooth-wall Poisson-Boltzmann solve and a planar Hamaker slab are the
>   right idealisations rather than convenient ones.
> - **What is still open is the half that carries the zero-bias hold-down.** `C-0021` finds a contact potential
>   of **0.9–5.1 mV** — below anything a bench would call zero — supplies the *entire* thermal-scale hold-down,
>   and gold narrows where to look without supplying the number. ~~Queued as `T-193`; if the literature does not
>   pin a template-stripped gold PZC in mM MgCl₂, this comes back as a one-line ask.~~
>   **`T-193` ran in iteration 23 and it does come back as a one-line ask — but not this one.**
>   The literature *does* pin the number (**0.46–0.51 V vs SHE** for Au(111), read directly), and what is
>   missing is the **cell's** definition of zero rather than the electrode's. See the residue section below.

**The question.** §1 says *"patterned electrode"* and never says of what.

**Why the programme cannot decide it.** A better calculation of the wrong material is not an
improvement. The answer is bracketed and handed back rather than chosen.

**What is established.**
  
- **Metal against oxide is 2.6×** on the van der Waals hold-down — the one term no design can remove.
  That is larger than the DNA Hamaker constant (1.17× after the square root), larger than 
  retardation, and larger than the polymer in the gap.
- **Zero *applied* bias is not zero *charge*.** A contact potential of **0.9–5.1 mV** — below anything
  a bench would call zero — supplies the **entire** thermal-scale hold-down by itself. No §3 parameter
  fixes it.

~~**What an answer needs to be.** A material (gold, ITO, SiO₂, …) and, if known, its potential of zero
charge in this buffer. If the material is genuinely not yet chosen, saying so is also an answer — the
programme will then carry the bracket explicitly rather than implicitly.~~
**Answered: template-stripped gold.** The surviving ask is in the residue section below, and it is about
the **drive** rather than the material.

~~**What deferring costs.** `C-0021` already states the exposure as a bracket with a threshold rather
than a guess, so nothing is blocked. The number is simply softer than it needs to be.~~

### The residue this answer left, and it is a ONE-LINE ask (`T-193`, [`C-0111`](gpd/claims/C-0111-gold-electrode-pzc.md))

**The material half is answered and the PZC half now is too — from the literature, read directly.**
`E_pzc(Au(111)) = 0.46–0.51 V vs SHE` in 1 mM HClO₄ (Adnan *et al.*, *PCCP* **26**:21419, 2024;
template-stripped gold is (111)-dominated). Gold is the **stiffest** of the four candidate electrodes, so
NDI's answer collapses the 2.6× bracket onto its **adverse** end — and **no verdict moves**, verified rather
than inherited: 0 of 6 gold states confine, the deepest gold well in the box is 8.742 `k_BT` against a
10 `k_BT` criterion, and the reason is structural (a `1/h³` force integrates to a bounded potential, so
naming a material changes the amplitude and never the exponent).

**What is left is not a material property — it is the cell's definition of zero**, and it is worth a
sentence because it decides the **sign** of the force at rest:

> **Is *"zero bias"* in the Gen-1 cell 0 V against a named reference electrode, the cell at open circuit, or
> two identical electrodes shorted? Gold's potential of zero charge is 0.46–0.51 V vs SHE, and the residual
> field is negligible only within about 1–5 mV of it — so the three answers are hundreds of millivolts
> apart, and they differ in the SIGN of the force at rest.**

An electrode sitting at zero volt on the rational scale is **negatively charged**, and it **lifts** the
negatively charged tile rather than holding it down. The whole sign structure lives inside **11.2 mV** at
10 nm (lift −6.087, force-free −0.314, hold-down +5.102 mV) — 41× narrower than the offset a bench must
null — and the requirement is that the electrode be held within **5.102 mV** of its own PZC at the 10 nm
layer, **0.886 mV** at 5 nm.

**No number is quoted at the lifting sign**, because it falls outside `C-0005`'s point-ion boundary at 9 of
9 states; the answer is the threshold, not a force.

---

## 4. Which device does §3's *desired* clause ask for? (`T-112`)

> ### ANSWERED, 2026-08-18 — TWO DEVICES
>
> **NDI, verbatim:** *"2 devices."*
>
> **What it changes.**
>
> - **Every desired-stroke number read on the acceptable clause's coupling is read on a device that does not
>   exist.** The 333 pN at 10 nm, `C-0039`'s 699 pN, and the per-path allowables that follow from them all
>   belong to a 33.333 pN/nm coupling travelling 10 nm. Device B is `C-0046`'s **10 pN/nm** placement.
> - **The composed cap stops being a programme bound.** `δ ≤ F/|k_eff|` = **3.58–4.27 nm at 100 pN** is a
>   statement about **one** device, and it bounds device A.
> - **Device B's binding constraint is `C-0017`'s stability floor**, `|k_eff|` = **23.41–27.91 pN/nm** at the
>   10 nm layer in 2 mM: a 10 pN/nm coupling is refused at **2.34–2.79×**.
> - **And that refusal is quoted at a state, not at a device.** `|k_eff|` is read at the 10 nm layer in 2 mM —
>   and device B is exactly the device decision 2 says needs a **17–26 nm** layer, which decision 1 says needs
>   **low salt**. **The three answers converge on one object**: device B is the low-salt tall-layer device, and
>   ~~whether it exists is one evaluation of `|k_eff|` in a corner nobody has evaluated. `T-192`, and it is cheap.~~
>
> ### `T-192` RAN, AND DEVICE B IS REFUSED — iteration 23, [`C-0110`](gpd/claims/C-0110-device-b-tall-gap.md)
>
> **This section was still telling a reader that device B's existence was an open, cheap question, four
> iterations after it was measured** (struck in iteration 27, `C-0124`/`T-184`).
> The corner was evaluated: §3's 100 pN stops arriving across a gap of **13.6989179 nm at 0.5 mM**, so
> 100 pN reaches a resting 17–26 nm gap at **0 of 12** (gap, buffer) cells,
> §3's *acceptable* clause is refused at **96 of 96** tall states and the *desired* one admitted at **1 of 96**
> — a bracket disagreement rather than a design.
> **Device B as decision 2 conceived it does not exist**, and what survives of decision 4 is the part that
> needs no tall layer: the acceptable and desired clauses are placed at **33.333** and **10 pN/nm** and
> **every desired-stroke number in the corpus must be quoted with the clause that placed it.**

**The question.** §3 gives an acceptable clause (100 pN at 3 nm) and a desired one (~10 nm stroke).
Are these two operating points of **one** device, or two devices?

**Why the programme cannot decide it.** It is a reading of the requirement.

**What is established** (`CH-0059`).

- **Every claim in this programme reads the desired stroke on a coupling placed for the acceptable
  one**, `k_c = 33.333 pN/nm` — which must then deliver **333 pN** at 10 nm, and `C-0039` finds the
  real element needs **699 pN**.
- **`C-0017`'s own arithmetic says the desired clause's coupling is `100/10 = 10 pN/nm`** — a
  *different* device. `C-0046` shows the flexure **can** build it (arms 11.4–18.1 nm, 12 of 29 points
  clearing every element clause), and `C-0017`'s **stability floor refuses it at 2.34–2.79×**.
- **Composing the two clauses caps the stroke at 3.58–4.27 nm at 100 pN**, whatever the coupling is
  made of — a bound that names no coupling element at all.
  
**What deferring costs.** Every desired-stroke number in the corpus has to be quoted with the clause
that placed it, and a per-path allowable read at one clause is 3.33× the other. `C-0049` had to
withdraw a 40 pN/nm ceiling for exactly this reason.

---

## 5. What is the scaffold — linear or circular, M13 or synthetic, and how long? (`T-154`)

> ### ANSWERED, 2026-08-18 — CIRCULAR M13, AND THE REMEDY FOR THE EXCESS IS A THICKER TILE
>
> **NDI, verbatim:** *"Scaffold default is M13, circular ~7-8K nucleotides, but 50K nucleotide scaffold is
> available for larger systems if we need it for optimization. Have been assuming hierarchical assembly for
> complicated structures built of multiple 8K scaffolds. To use exess scaffold, just make the tile thicker.
> The 1700 nucleotide structure the agent is proposing seems… thin and low stiffness, though we have not
> assessed the specific details."*
>
> **What it changes, and this is the largest consequence of the whole reply.**
>
> - **Row three of the table below is the DEFAULT, not one of three options.** Circular M13, and the tile takes
>   **1 680** of **7 249** nt, so the **5 569 nt / 33.3 nm** unpaired coil carrying **1.66×** the sheet's own
>   charge is present unless something is done about it. `T-154`'s *modelling* half — carry that body into
>   `C-0022`'s edge solve — stops being optional.
> - **The programme's own recommendation is declined, implicitly.** It recommended a purpose-length 1 680 nt
>   scaffold; NDI's answer is to **spend** the scaffold rather than to shorten it.
> - **The criticism lands, and this repository already carries the measurement that corroborates it.**
>   *"Thin and low stiffness"* is what the field has measured about single-layer sheets: Fischer et al.'s SAXS
>   gives the single-layer sheet a fitted lattice width of **9.1 %** against **2.9 %** for a multilayer brick,
>   and Kube et al. could not solve a single-layer structure at all, *"due to excessive conformational
>   heterogeneity"* (`C-0072`).
> - **And §3 agrees with NDI rather than with this programme.** §3's parameter row is *"Tile thickness ~10 nm
>   (single-layer honeycomb)"* — a contradiction `electrostatics/DnaOrigamiTile.kt` and `C-0021` both record
>   and both carry **two readings** of. Every structural claim took the thin one. **4.31× of scaffold and
>   ~10 nm of thickness are the same tile**, and it is four layers.
> - **So the flatness negative is a result about a tile nobody is asking for.** `C-0006`'s own variant table
>   carries the thick one: ~~`D_∥` = **14 310.78 pN·nm** against 85.50, `D_⊥` ≥ **19.222** against 3.345 — 167×
>   and 5.75×~~ — **that pair is `CH-0124`, open, and it is a MIXED state, not a bracket end** — and the
>   Winkler reach is their fourth root. ~~`T-191`.~~
>   **`T-191` RAN** ([`C-0109`](gpd/claims/C-0109-four-layer-tile.md)): the calibrated pair is
>   `D_∥` = **4 547.17603** and `D_⊥` = **240.931249** pN·nm, and the four-layer tile dishes
>   ~~**0.0577199433**~~ of the stroke with no coupling at all.
>   **RESTATED, iteration 35 (`C-0141`): 0.0978155002 on `15 × 4` and 0.0240648102 on `10 × 6`** — the cross-section
>   those numbers were solved on is not a honeycomb. See the head of this file and §7.

**The question.** §1 and §3 specify a 40 × 40 nm single-layer origami tile and never say what scaffold folds it.
Until iteration 17 that looked like an implementation detail. It is not: it decides whether the tile has a
**seam**, and a seam costs arm stations.

**Why the programme cannot decide it.** It is a choice of starting material and a fabrication route.

**What is established** (`C-0086`, `C-0081`).

- **A seam is a parity on a tree, not a fabrication convention.** Crossovers join only *adjacent* duplexes, so a
  single-layer sheet's row-adjacency graph is a **path** — a tree — and a closed walk on a tree traverses every
  edge an even number of times. A **fully folded circular** scaffold therefore gives every row two segments,
  i.e. exactly one seam. Brute-forced: the path graph carries **2 Hamiltonian paths and ZERO Hamiltonian cycles**
  at every width from 3 to 12 duplexes.
- **So a seam needs two premises — circular *and* fully folded — and the Gen-1 tile fails the second anyway**:
  the sheet takes **1 680** of M13's **7 249** nt.
- **A seam is not free.** `C-0081` finds it deletes a weave extremum, putting **6–12 of `C-0063`'s 34** arm
  stations off the node, and the parity that *closes* takes the worst across-row clearance to **0.122724 nm** —
  and to **−0.002276 nm** at the measured phosphate girth, which is a clash.
- **Three specifications, each with a built precedent read directly from Rothemund (2006).**

All three rows are `C-0086`'s Deliverable 2, and every figure in them is quoted from it:

| specification | seams | cost |
|---|---|---|
| **linear** (`C-0086`) | **0** | linearisation by BsrBI digestion dropped Rothemund's own yield from **63 %** well-formed to **11 %**, which he attributes to strand breakage during digestion. A **synthetic or PCR** scaffold of the right length avoids the digestion entirely |
| **circular, fully folded** (`C-0086`, `C-0081`) | **1** | the seam, i.e. 6–12 of 34 stations and `C-0081`'s restored amplitude bracket |
| **circular, with remainder** (`C-0086`) | **0** | the remainder itself: **5 569 nt**, a **33.3 nm** unpaired coil carrying **1.66×** the sheet's own charge in the actuated gap. A purpose-built 1 680 nt circular scaffold removes it and needs only a **67 nt** return loop |

- **And a seamless raster quantises the tile WIDTH at 32 bp.** Admissible row lengths are odd multiples of
  16 bp — **16, 48, 80, 112, 144** — and **§3's 40.0 nm = 117.6 bp is not among them**. The nearest is
  **112 bp = 38.08 nm**, a 4.8 % narrower tile, which every plan margin in the corpus would have to be re-read
  at (`T-153`).
  **DOMAIN CORRECTION, iteration 27** ([`C-0119`](gpd/claims/C-0119-honeycomb-raster-width.md)): **that is the
  SQUARE lattice**, and the tile NDI's own answer produces is **honeycomb**, where the half turn is **5 bp**
  rather than 5.25 and the scaffold-crossover lattice is `7k ± 5` — integral. `C-0086`'s odd-half-turn rule
  admits **no** honeycomb row length at all, which is the rule being **outside its own domain** and not a
  prohibition. ~~**The 112 bp = 38.08 nm width survives and its ground has moved**~~, ~~and a seam is still forced —
  by the same tree parity, for a reason in the primary source rather than in the arithmetic above.~~
  **WITHDRAWN, iteration 40** ([`C-0168`](gpd/claims/C-0168-recommended-block-seam.md),
  [`CH-0212`](gpd/challenges/CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md)):
  the tree parity needs **two** premises and the block drops the second. Its two raster termini sit
  **35.504 nm = 14 d** apart, both at offset 7 and on the same face, and the **919 nt** this design
  leaves spare close the circle at **1.03–2.69 `k_BT`** across the whole ssDNA Kuhn bracket — against
  the **8.0 `k_BT`** the host sheet pays per crossover column. So **a seam is available and is not
  forced**, and the committed artifact (60 domains on 60 helices, a Hamiltonian path) is right as
  drawn. Every derivation of `C-0119` §4 is upheld and reproduces at departure `0.0`.
  **RESTATED, iterations 35–38** (`C-0140`, [`C-0151`](gpd/claims/C-0151-closing-raster-selection.md)): a
  honeycomb x-raster carries **both** turn senses, so it has **no uniform row length** — the drawable
  two-length pair is **`102 / 109 bp`**, whose rows span **`109 bp`** and whose block extent is
  **116 bp = 39.44 nm**, `−1.40 %` of §3's nominal. Which of the two a specification names is decision 8.

~~**What the programme would recommend.** A **purpose-length scaffold** — synthetic, PCR, or a circular 1 680 nt
construct with a short return loop — because it is the only option that is seamless *and* avoids both the
digestion yield penalty and the unpaired remainder. But this is a wet-lab cost judgement about scaffold
sourcing, which the programme cannot price.~~
**DECLINED, implicitly, by the answer above** — NDI's remedy is to **spend** the scaffold, not to shorten it —
and this file's own answer block says so while the recommendation stood here unstruck.

~~**What deferring costs.** `C-0081` and `C-0086` must both be carried in two readings, and `T-153` — a re-read of
every plan margin at 38.08 nm — cannot be scoped, because whether it is needed at all depends on this answer.~~
**CORRECTED, iteration 27.** `T-153` was scoped **and closed in iteration 18** —
[`C-0090`](gpd/claims/C-0090-buildable-raster-width.md) — five iterations before this file was last edited,
so *"cannot be scoped"* was already false when it was written. What deferring costs now is only that
`C-0081` and `C-0086` are carried in two readings.

---

## 6. Does §3 admit a two-layer tile, or is single-layer a requirement? (`T-166`)

> ### ANSWERED BY IMPLICATION, 2026-08-18 — YES, AND VOLUNTEERED RATHER THAN GRANTED
>
> **NDI answered five numbered questions and this one is answered inside the fifth:**
> *"To use exess scaffold, just make the tile thicker."*
>
> **Recorded as INFERRED rather than stated**, and it is the one answer worth confirming in a sentence —
> because the question asked whether a **second body** may be tied to the tile at many out-of-plane sites,
> and what was volunteered is that the **tile itself** may be thicker.
> **The second is the stronger permission**: `C-0093`'s shared stiff body is a body the tile is tied *to*, and
> a thicker tile is that body **fused** to the tile, which deletes the ties and the compliance in them.
>
> **What it changes.** The axis this section records as spent reopens — not on the **coupling**, where
> `C-0098` genuinely closed it, but on the **body being coupled**. The 90th-percentile dishing under measured
> staple dropout is a ratio to the stroke of a tile whose bending length is smaller than itself; `C-0006`'s
> four-layer row moves both bending lengths by a fourth root of 167× and 5.75×. `T-191` runs it.
>
> **The honest caveat is that a thicker tile is not free anywhere else**: it carries more charge into the
> actuated gap (`C-0022`), it is a different body for `C-0004`'s drainage, its dropout statistics are measured
> on a *single-layer* Rothemund rectangle (`C-0087`), and `C-0093` already found that a *buildable* four-layer
> body reads **0.100166871** where the rigid limit reads ~~0.0344~~ **0.0344013403** (`C-0093`, `C-0116`;
> **restored to full width, iteration 36** — `C-0115` repaired this same truncation in `ANSWERS.md`
> and the instance here survived it) — so body rigidity is first order and a *yes*
> opens work rather than delivering a win.
> **That 0.100166871 is under [`CH-0125`](gpd/challenges/CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md), raised by `C-0109` and OPEN**, which names
> *"`DECISIONS-FOR-NDI.md` (twice)"* as a carrier and says it should not be re-quoted without the challenge.
> The brick is mis-specified in three ways whose corrections **do not share a sign**, and the re-solve is owed.

**The question.** §1 and §3 name a single-layer origami tile. Is that a requirement, or the simplest thing
that came to mind when the problem was written?

**Why the programme cannot decide it.** It is a statement about what the device is allowed to be.

**What is established** (`C-0087`, `C-0089`, `C-0093`).

- **The flat Gen-1 tile is a zero-defect result.** Under the only per-staple incorporation statistics anybody
  has measured, **no array coupling is flat**: the best 90th-percentile dishing over 22 designs is **0.2845**
  against `T-5b`'s 0.10, and what refuses it is a **count** — a dropout *is* an increase in the attachment
  pitch, so the density demanded is 195 paths against the 34 the plan admits.
- **The one structural escape is a coupling that is not an array** (`C-0093`): tying the tile to a **stiff
  shared body** moves the mandate into the body's ground, frees each tie by 3.4×, and is **2.05× flatter**
  than the array at zero defects on identical stations. Under the dropout it reaches **0.24028028** — the
  best this programme has achieved — and still misses, needing **252 ties against 53**.
- **But that body is multilayer origami.** A body tied at many out-of-plane sites is a square-lattice second
  layer, which is *the one motif in this programme with a published precedent* — against the single-tie
  out-of-plane arm the recommendation currently rests on, which has none in 62 recorded queries.
- **A buildable body is not the rigid one**: a four-layer honeycomb brick reads **0.100166871**, *worse* than
  the array. Body rigidity is first order, so a *yes* opens real work rather than a free win.
  **This is `CH-0125`'s second carrier in this file, and the challenge is OPEN** — the brick's `D_⊥` is taken
  at the *uncoupled* value and its `D_∥` at the *rigid* one, so two of the three corrections run opposite ways
  and neither `C-0109` nor `CH-0125` claims to know the net.

**What the programme would recommend.** ~~Nothing yet — `T-165` has not been run~~ — **CORRECTED,
iteration 22.** `T-165` ran in iteration 20 and `C-0098` **closed that axis negatively**: searched on the
crossover sites the lattice actually supplies, the best 90th-percentile dishing is **0.375506727** at 100 %
exceedance — *worse* than the figure it was chasing, because that figure sat on an abstract 90-station grid
where the lattice offers at most 60. The distribution axis **shuts as `1/t`**, the shared body's stiff limit
being a kinematic constraint independent of the tie distribution.
**So this decision is narrower than when it was written**: what a *yes* buys is a body **2.05× flatter than
the array at ZERO DEFECTS**, not a fabricated flat tile. A *no* still closes the last open recovery route —
~~and the honest statement is that the programme now has **no unspent design axis at all**, only a fabrication
yield.~~
**STRUCK, iteration 27 — this file contradicted itself about it.** The head of this file says the four-layer
body *"is the first unspent design axis since iteration 20"* and this section says there is none;
both were written in iterations 22–23, and **the axis existed, was spent, and is what §7 is about.**

~~**What deferring costs.** Less than it did, because the axis is spent. The flatness answer stays *"flat as
designed, not shown flat as built"*, and what would move it is the per-site incorporation measurement in
[`ANSWERS.md`](ANSWERS.md) §5 — a bench measurement — rather than another coupling design.~~
**CORRECTED, iteration 27.** What moved it was **neither** a bench measurement **nor** another coupling design:
it was the **cross-section**. [`C-0118`](gpd/claims/C-0118-coupled-four-layer.md) grades 16 coupled cells under
`C-0087`'s measured staple dropout and ~~**9 are flat at the 90th percentile** — **all eight** of the `10 × 6`
ones, best **0.0278431488**, against **one of eight** on `15 × 4`~~ — and
[`C-0122`](gpd/claims/C-0122-honeycomb-station-lattice.md) shows the flatness survives being placed on the
honeycomb's own 21 bp station ladder.
**RE-GRADED at the corrected cross-section, iteration 34**
([`C-0142`](gpd/claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md),
[`CH-0176`](gpd/challenges/CH-0176-the-first-flat-coupled-tile-is-flat-at-half-its-cells.md)):
**four** of sixteen cells are flat, **all four on `10 × 6`**, and `15 × 4` is **0 of 8 at both ends of the measured
0.26–0.33 band** — its one flat cell moving 0.0882933461 → **0.145354102**.
The best cell moves 0.0278431488 → **0.0680677948** and is still the sparsest coupling tested; the cross-section's
worth falls 3.17109774× → **2.13543134×**.
**A coupled four-layer tile is still flat at the 90th percentile under the measured folding statistics** — and the
path count has stopped being a request: snapped onto `C-0141`'s honeycomb station ladder all sixteen placements are
realisable (worst snap **3.332 nm**, inside a 3.57 nm ceiling) and **three** of them are flat, all three **equal
springs**. **The per-site incorporation measurement is still wanted** — this is a
TRL 1–3 result under somebody else's measured statistics, not a built tile — but it is no longer the only
thing that could move the answer, and this paragraph said it was.

---

## 7. ~~NEW — is a 38 × 25 nm tile acceptable, in exchange for removing the last unmeasured dependency in the flatness verdict?~~ **SUPERSEDED AS POSED — RE-POSED BELOW**

> **SUPERSEDED AS POSED, iteration 35.**
> [`C-0144`](gpd/claims/C-0144-honeycomb-correction-supersession.md) §4, on
> [`C-0141`](gpd/claims/C-0141-honeycomb-station-lattice-and-placement.md) and
> [`C-0142`](gpd/claims/C-0142-coupled-cells-at-the-honeycomb-cross-section.md).
> **The trade this question offers does not exist, and the question is posed the wrong way round.**
> It asks NDI to accept a smaller tile in exchange for removing an unmeasured dependency.
> Corrected to the honeycomb's own two pitches, the tile it charges for — `10 × 6` — is
> **38.08 × 37.504 nm, 0.929467162 of §3's 40.35**, essentially §3's own square;
> and the *default* it offers, `15 × 4`, is **38.08 × 56.524 nm, 1.40084263×** it, **and is no longer flat**.
> **The tile offered as the cost is the one that fits.**
> All three criteria this programme can rank — folding yield, flatness, footprint — now point the same way,
> so **what NDI is owed here is a correction, not a trade.**

### 7 (re-posed, iteration 35) — two questions, and neither of them is a trade

**7a — Confirm `10 × 6`. There is nothing left to weigh, and this programme would rather be told so than assume it.**

| | `15 × 4` — what §7 offered as the default | `10 × 6` — what §7 charged a footprint for |
|---|---|---|
| footprint, across the helices | **56.524 nm — 1.40084263× §3's 40.35** | **37.504 nm — 0.929467162 of it** |
| footprint, along the helices | the row length is **not uniform** — `C-0140`; design (i)'s remedy is ~~112 / 108 bp~~ **`102 / 109 bp`, iteration 37 (`C-0151`)**, an axial extent of **39.44 nm**, `−1.40 %` of §3's nominal — **the extent is unchanged by the correction** | the same alternation (**29 / 29**); ~~its own two row lengths are not derived here~~ ~~**the same 112 / 108 pair, derived iteration 35**~~ **the same `102 / 109` pair, RESTATED iteration 38** (`C-0146` graded the whole `10 × 6` coupled family at 112 / 108 and `C-0151` re-graded it at the drawable pair; `C-0147`'s alternation is a property of the turn sense and not of the block's shape, and at `102 / 109` the two faces are **7 / 14 bp** rather than 4 / 8). **And the extent is a STAGGER, not a length — see §8** |
| folding, measured by Douglas et al. | sharp monomer band, 1 of 3 of seven | **the greatest fraction of defect-free objects** |
| free tile, uncoupled | **0.0978155002** of the stroke | **0.0240648102** |
| interlayer-coupling threshold | **`f` = 0.276970522 — INSIDE the measured 0.26–0.33 band** | none — flat at every `f` including 0 |
| at the band's adverse end, `f` = 0.26 | **0.101759944 — FAILS `T-5b`'s 0.10** | **0.0255589305 — flat** |
| coupled, under the measured staple dropout | **0 of 8 cells flat, at BOTH ends of the band** | **4 of 8**, best **0.0680677948** — on the abstract cross-section (`C-0142`). **Re-graded on the DRAWABLE `102 / 109` raster's own 10 crossover columns (`C-0151`): 2 of 8, and the RECOMMENDED cell — one column, ten paths, equal springs — is flat at both ends of the band, `0.0773373597` at `f` = 0.30 and `0.0821458169` at `f` = 0.26** |
| attachment stations its top face supplies | 90 | 60 |

Provenance, row by row: the footprint, the free-tile dishing and the threshold are `C-0141`'s and **all of them
moved in iteration 33**; the coupled row is `C-0142`'s and moved in iteration 34; the row length is `C-0140`'s;
the folding row is `C-0119`'s reading of Douglas et al.; the station counts are `C-0122`'s, **withdrawn upward
by `CH-0151` and restored by `C-0141`**.
**The question is therefore a confirmation**: is the Gen-1 tile specified as the `10 × 6` block?
A *yes* costs nothing that this programme can see. A *no* re-opens a flatness verdict that now **fails** at
one end of the only measured band there is.

**UPDATED, iteration 39 — the block this question names is now a FILE, and it passes a check it could not be
given before.** ([`C-0160`](gpd/claims/C-0160-scadnano-writer.md),
[`C-0164`](gpd/claims/C-0164-lattice-aware-buildability.md).)
`gpd/designs/gen1-block-honeycomb-10x6-102-109.sc` is a committed scadnano artifact — **60 helices, ten
corrugated x-raster rows of six, `102 / 109 bp`** — which the **reference** scadnano implementation (0.21.1)
loads with **zero warnings**, and on which the block's 55 stations, phase 16, 14 bp inter-row offset, 7 bp
stagger, 102 bp interface window and `116 bp = 39.44 nm` extent are re-derived rather than asserted.
`checkBuildability()` is now **lattice-aware**, and the honeycomb branch answers: the block reads
**`ADMISSIBLE` at ZERO forced crossovers**, its **59** raster crossovers reducing by their own bond class to
exactly two residues `{4, 14}` — ten apart, which *is* caDNAno's `±5 bp` closure condition — under one `b₀`.
**Two clauses travel with that word and neither is a formality.** `ADMISSIBLE` means *"every rule this
repository has for this design's lattice applies, and passes"*, and **not** that anything is foldable; and the
emitted block **carries no staple set**, so it is a lattice artifact rather than a design somebody could order.
**Nothing here changes the answer this question is asking for**, and nothing here is a new cost: what changed
is that a *yes* can now be given against an object rather than against a pair of integers in a study literal.

**7b — Which scaffold? The caDNAno paper contradicts itself at exactly the design this programme proposed.**

`C-0119` and `C-0125` read *"i: p8064"* from that paper's Methods list; its own main-text rule — 60 helices take
**p7560**, 64 take **p8064** — gives design (i) **p7560**. The list and the rule agree at **six of the paper's
seven designs and disagree at exactly one, ours**, and under the Methods reading design (i) would leave
**504 nucleotides** the paper's own *"126 bases per helix"* accounting has no line for
([`CH-0180`](gpd/challenges/CH-0180-the-scaffold-pairing-contradicts-its-own-paper.md)).
**This is a source contradiction, not a reading error, and no calculation closes it.**
It matters because the unpaired remainder — the body in the actuated gap that decision 5's answer made the
default — **is** the difference between the scaffold length and the tile's 6 720 nt (`C-0109`), so it is a different
number under each reading: `C-0125` gives **529 / 840 / 1 344 nt** for M13mp18's 7 249, p7560 and p8064.
If the main text governs, design (i) is a p7560 design and its remainder is **0** rather than **1 344 nt**.

**And the scaffold's turn allowance is a CHOICE, not a constraint — which is what decides between the two
routes** ([`C-0147`](gpd/claims/C-0147-honeycomb-turn-slack-and-ragged-face.md), `T-230`).
The caDNAno blocks spend **28 unpaired nucleotides** per raster turn. The **reach** bound is **6**: `n`
unpaired nucleotides make `n + 1` phosphodiester steps, the two anchoring phosphates of a worst-azimuth turn
are at most `d + 2r_P` = **4.35327572 nm** apart, and on `T-71`'s **measured** backbone step six nucleotides
reach that. **28 is 4.66666667× its own bound.**
**But the minimum is a CRITERION, not a number**, and the criteria bracket rather than agree — reach **6**,
the 10 pN unzip allowable **8–9**, one `k_BT` of stored free energy **16–22**, one piconewton of turn tension
**29–41** — and the built 28 lands **inside** that bracket, at **1.00195245–1.46667915 pN** and
**0.518481856–0.7570064 `k_BT`**, i.e. sub-thermal and about one piconewton. A slack turn is what 28 buys.

> **So *"the widest four-layer tile is 92 bp = 31.28 nm"* is a ceiling on ONE published allowance, not on the
> route.** At the reach bound the widest uniform row M13 pays for is **114 bp = 38.76 nm**.

**What M13 actually affords is exactly EIGHT nucleotides**: `60 × (112 + L) ≤ 7 249` gives `L ≤ 8`.
So a **uniform** 112 bp row — the route that needs no two-length raster and no ragged face at all — **fits, by
two nucleotides**, and fits only **strained**: an 8 nt turn sits at **0.777–0.837 of its own contour** and
carries **6.54349121–12.112167 pN**, at or past the 10 pN unzip allowable at the tight end of the ssDNA Kuhn
bracket, for **139–220 `k_BT`** over the block's 59 turns. `C-0140`'s recommendation of the two-length route
therefore **stands on a better reason than it had**: not that the uniform route does not fit, but that it fits
only strained. **p8064 removes the question**, affording **22 nt**, inside the one-`k_BT` band at both ends of
the bracket — which is a second reason 7b matters.

**And the yield half is DECLARED UNPRICEABLE.** No published measurement relates a scaffold **turn-loop
length** to folding yield; the three nearest are on different axes (Ke et al.'s 8 bp staple domain, Rothemund's
scaffold **linearisation**, Strauss et al.'s per-**staple** incorporation), and the only measured point on this
axis is the built blocks themselves. **The threshold is quoted instead: 8 nt.** At or below it a uniform 112 bp
row fits M13; above it it does not. That is the number a folding experiment would have to bracket, and this
programme cannot supply it.

**And the tile has no single row length.** `C-0140` proves a honeycomb x-raster carries **both** turn senses, so
there is no uniform honeycomb row length at all: design (i) ends at **30 helices in one sense and 28 in the
other**, and ~~the remedy costs 3 base pairs — **112 / 108 bp**~~ **the remedy is `102 / 109 bp` and the
minimum stagger it can carry is SEVEN base pairs, not three — RESTATED, iteration 37/38
([`C-0151`](gpd/claims/C-0151-closing-raster-selection.md), [`CH-0194`](gpd/challenges/CH-0194-the-filter-and-closure-are-disjoint.md));
see the closing paragraph of this section** — at an unchanged **axial extent of 116 bp = 39.44 nm, `−1.40 %`
of §3's nominal**, ~~which beats the square lattice's `−4.80 %`~~. **THAT COMPARISON IS CROSS-CONVENTION
([`CH-0191`](gpd/challenges/CH-0191-a-width-advantage-across-two-conventions.md), iteration 36): 39.44 nm is a
BOUNDING BOX and the square lattice's 38.08 nm is a ROW LENGTH.** A uniform square-lattice raster has no
stagger, so its two readings coincide; the honeycomb's do not. Read on the **row length** the honeycomb's rows
are **112 bp = 38.08 nm — exactly the square lattice's, so the advantage is EXACTLY ZERO**; read on the
**bounding box** the honeycomb wins by **1.36 nm**, and that 1.36 nm **is the stagger**, i.e. it wins by being
ragged. `10 × 6` carries the same alternation (**29 / 29**);
~~its own two row lengths are not derived here.~~
~~**They are, iteration 35 — the same 112 / 108 pair**~~ **They are — and the pair moved in iteration 37, to
`102 / 109`** ([`C-0146`](gpd/claims/C-0146-coupled-cells-at-the-two-length-raster.md),
[`C-0147`](gpd/claims/C-0147-honeycomb-turn-slack-and-ragged-face.md),
[`C-0151`](gpd/claims/C-0151-closing-raster-selection.md)): `C-0146` graded the whole `10 × 6`
coupled family at 112 / 108 and `C-0151` re-graded it at the drawable pair, and `C-0147` finds the ~~**identical** 4 bp front face, 8 bp rear face~~ **same construction of faces** and 116 bp extent on
`10 × 6` as on `15 × 4` — the raggedness being a property of the honeycomb's turn-sense alternation and not of
the block's shape. At `102 / 109` those two faces are **7 and 14 bp** rather than 4 and 8.
~~**So every `38.08 × …` in the analysis below is a withdrawn width.**~~
**RESTATED, iteration 36** (`C-0146` §1): **38.08 nm is not withdrawn — it is one of two readings of the same
block, and it is the one every raster row actually has.** Every x-raster row of the block spans ~~**112 bp =
38.08 nm exactly**~~ **the LARGER of its two lengths exactly** — at all ten rows and at **every one** of `C-0140`'s five candidate pairs, which is `112 bp = 38.08 nm` at 112 / 108 and **`109 bp`** at the drawable `102 / 109` (`C-0151` §9) — and the 116 bp extent
exceeds it by exactly the inter-row **STAGGER**, ~~a **4 bp = 1.36 nm**~~ **`4 bp = 1.36 nm` at the old pair and `7 bp = 2.38 nm` at the drawable one**, which no plate model in this repository can
represent. **Which of the two §3 names is decision 8, below.**

**And the 4 bp raggedness that follows costs §3's flatness EXACTLY NOTHING**
([`C-0147`](gpd/claims/C-0147-honeycomb-turn-slack-and-ragged-face.md), `T-231`).
A four-layer block's **gap-facing** surface is the outermost layer's sidewalls; a row length moves where a
helix **ends**, which is a coordinate in the tile plane at right angles to the gap. **So the two ragged faces
are the tile's RIM, the coefficient of the raggedness on the normal-direction flatness field is exactly zero,
and `T-5b`'s convention cannot read it at all.** The residual channel — a 2-row rim modulation at **7.608 nm**
against across-helix bending lengths of **17.2310927** (`15 × 4`) and **23.2114857 nm** (`10 × 6`) — is bounded
at **5.54399427e−05** and **1.68371917e−05** of the stroke, against the **0.0274976866** of headroom the
tightest flat coupled cell has: a margin of **496×**, consuming at most **0.2016 %** of it.
**What the raggedness does cost is plan budget**, and there it is not small: a 4 bp short row has **1.36 nm**
less axial extent outboard of a fixed root plane, against a `C-0141` outboard ceiling that **saturates at
2.380 nm** at 90 demanded paths — **0.571 of it**, and 0.036 at 10–15 paths.
**RESTATED, iteration 38** (`C-0151`; [`CH-0202`](gpd/challenges/CH-0202-the-plan-ceiling-escape-is-quoted-at-fifty-paths.md)):
the drawable pair's relief is **7 bp = 2.38 nm** rather than 4 bp, and the **saturated** ceiling it would be
charged against is **not reachable** at the determined ladder phase, which supplies **55 of 60** stations.
At 55 paths the ceiling is **3.06 nm** — a margin of **exactly 2 base-pair rises** over the relief — so the
axis still does not bind, on a tighter ground than `C-0151`'s own 50-path **4.604 nm**.
**The ragged-face COST has not been re-run at 7 bp** — its coefficient on §3's flatness is exactly zero either
way, the raggedness being on the rim, and the residual rim bound scales with the relief; `T-258` owns that
re-run.
What it **buys** is a published anti-stacking geometry: 1.36 nm clears the all-atom PMF's attractive limb
(0.65 nm) and its repulsive turn (1.30 nm), by **0.18 of a base-pair rise** — which by this project's own rule
is not a quotable margin. ~~**`CH-0187` is open on exactly that**~~ **ANSWERED, iteration 37** ([`C-0151`](gpd/claims/C-0151-closing-raster-selection.md)), and answered against the pair this paragraph goes on to recommend — **`101 / 109` does NOT close on caDNAno's own `±5 bp` scaffold-crossover rule**, so its width win is not available. Closure depends on the two row lengths only through their residues modulo 21; the family is 441 cases, exactly three residue pairs close, and all three differ by `14 (mod 21)`, so **a drawable raster carries a stagger of at least 7 bp** where the filter that produced `101 / 109` admits at most 4 — the filter and the closing family are **exactly disjoint** (`CH-0194`). The drawable recommendation is **`102 / 109` at the same 39.44 nm extent, for the price of one crossover column**, and it wins the stacking relief this paragraph is about at **3.18 rises** rather than 0.18, which *is* quotable. What follows is retained as the reasoning that was current before closure was known to bind, and it is struck rather than removed because the reversal is the instructive part:
~~among all pairs that fit M13 the width optimum
is **101 / 109 bp at `−0.55 %`**, with an 8 bp = 2.72 nm relief and **`+4.18` rises** of clearance, and it wins
three of four axes. **112 / 108 wins only the plan budget, and only for a design that needs a saturated path
count** — so anything quoting 112 / 108 as *the* honeycomb width must carry that condition.~~
**Re-scored inside the CLOSING family (`C-0151` §2a), `102 / 109` wins five axes of seven and the two it loses
are both bounded**: the row-derived **crossover column** count, 10 against 11, which is the whole cost of
closure; and the plan budget, where `C-0141`'s outboard ceiling **binds only at saturation** and the ladder
phase the closure rule determines makes saturation unreachable — a 60-of-60 placement is not available, and at
the **55** stations the determined lattice does supply, the ceiling is **3.06 nm** against the **2.38 nm**
relief, a margin of **exactly 2 base-pair rises**
([`CH-0202`](gpd/challenges/CH-0202-the-plan-ceiling-escape-is-quoted-at-fifty-paths.md); `C-0151` §2a quotes
the 50-path row, **4.604 nm**, which is below its own census).

> **And closure is a reason to PREFER `102 / 109`, not a proof that `112 / 108` is unbuildable — the
> distinction matters and this programme now has the number for it.**
> caDNAno's `±5 bp` rule is that software's **default**, and the same paper provides a tool to force a
> crossover anywhere and warns only that departure *"may lead to folding failure if too much deviation from
> canonical DNA geometry is implied"*.
> [`C-0152`](gpd/claims/C-0152-forced-scaffold-crossover-price.md) (`T-246`) prices the **elastic** half of
> that departure for the first time: a forced crossover is an **azimuth** rather than a count of base pairs,
> the smallest one the 21-residue lattice offers is **`17.1428571°`** — exactly **twice** the
> **`8.57142857°`** that an **allowed** scaffold crossover already carries at the exact 10.5 bp/turn geometry,
> which every honeycomb origami ever folded absorbs — and its ceiling is **`0.350894669 k_BT`**, **sub-thermal**
> (`2.84985806×` below one `k_BT`). All ten of `112 / 108`'s forced crossovers together cost **`0.438634952`**
> of **one** crossover column of the host sheet's own demonstrated currency, and origami folds.
> **So `112 / 108` is off-rule and cannot be argued out of a fold on elastic grounds.**
> What is **not** priced is the **kinetic** half — folding yield — and `C-0152` records a negative existence
> result for it over **68** queries in **7** declared families, the source that defines the operation saying in
> its own Discussion that its structural consequences are **not predicted**.
> **Nothing NDI has to decide turns on this**: `102 / 109` is on-rule, ties on width, and wins scaffold and
> stacking relief, so the trade is available only if the **eleventh** crossover column is worth taking a
> kinetic risk nobody can quote.

**What deferring costs, re-priced — and the sign has reversed.** Deferring used to mean carrying the
interlayer-coupling calibration as a live dependency. It now means carrying a **default that overruns §3's
footprint by 40 % and fails `T-5b` at the adverse end of the measured band**, and quoting a scaffold the source
does not agree with itself about.

---

**The analysis as it stood at iteration 26 is kept below**, struck where a number is withdrawn and otherwise
unedited, because the reversal is the instructive part: this is the one place in the programme where a
*specification trade* turned out to be an *arithmetic error in the programme's own model of the tile*.

**Its analysis is [`T-199`](TASKS.md), closed in iteration 25; the decision it raised is what is outstanding**,
and the queue carries that half as item 12 of its questions for Kazik rather than as a task row.
Rows 1–6 of the table above name a **specification** task in their owner column and this one names an
**analysis** task, which is the only structural difference between the seventh decision and the other six.

> **This decision did not exist when the other six were sent.** It is the consequence of NDI's own answer to
> decision 5 — *"just make the tile thicker"* — followed four claims further than anyone expected, and it is
> the only question on this list where **this programme has a recommendation it is not confident enough to
> act on alone.**

**The question.** §3 specifies a ~40 × 40 nm tile. The tile that follows from NDI's own answers is **four
honeycomb layers of fifteen duplex rows** — ~~38.08 × 38.04 nm, essentially §3's~~ **38.08 × 56.524 nm,
1.40084263× §3's** — and it is **design (i) of the caDNAno paper**, folded and gel-verified. That paper's own
conclusion is that a **different** cross-section of the same 60 helices folds better: **`10 × 6`**, ten raster
rows of six helices, which is ~~**38.08 × 25.36 nm**~~ **38.08 × 37.504 nm, 0.929467162 of §3's**.

**What each buys, in this programme's numbers.**

| | ~~`15 × 4` — essentially §3's tile~~ **`15 × 4` — 1.40084263× §3's** | `10 × 6` — the paper's recommendation |
|---|---|---|
| footprint | ~~38.08 × 38.04 nm~~ **38.08 × 56.524 nm — 1.40084263× §3's** | ~~**38.08 × 25.36 nm — 0.667 of it**~~ **38.08 × 37.504 nm — 0.929467162 of §3's** |
| folding, measured by Douglas et al. | sharp monomer band, 1 of 3 of seven | **the greatest fraction of defect-free objects** |
| free tile, uncoupled | ~~0.0577199433 of the stroke~~ **0.0978155002** | ~~**0.00874363524 — 6.6× flatter**~~ **0.0240648102 — 4.06× flatter, which is the ratio of the two cells of this row and is stated by no claim** |
| flatness threshold in the interlayer coupling | ~~`f` = 0.0788618807, cleared **3.29690337×**~~ **`f` = 0.276970522 — INSIDE the measured 0.26–0.33 band, and it FAILS at 0.26** | **none — flat at every `f` including 0** |
| coupled, under the measured staple dropout | ~~1 of 8 cells flat at the 90th percentile~~ **0 of 8, at both ends of the band** | ~~**8 of 8**, best 0.0278431488~~ **4 of 8**, best **0.0680677948** |
| attachment stations its top face supplies | 90 | 60 |

**Why the third row is not the important one.** `10 × 6` being flatter is a convenience. **`10 × 6` having no
threshold at all is a different kind of statement**: the `15 × 4` verdict depends on an interlayer-coupling
fraction calibrated on **rods** rather than slabs, and this programme has said plainly that a slab plausibly
realises less. On `10 × 6` that question **stops being load-bearing** — it is flat even with the layers fully
uncoupled. It is the only place in this programme where a design choice **removes** an unmeasured dependency
rather than bounding one.

**What it costs.** ~~A third of the area.~~ **NOTHING — WITHDRAWN, iteration 35 (`C-0141`): corrected, `10 × 6`
is 0.929467162 of §3's footprint and `15 × 4` is 1.40084263× it.** §3's 100 pN is specified **over the
footprint**, so a smaller tile is a higher pressure at the same force; `C-0022`'s edge collar was solved on the square, and its transfer to the new
aspect ratio is bounded at **1.32×** with the flatness surviving **2.27×** beyond that (`C-0123`) — so the
collar is not the obstruction. And the top face offers **60** attachment stations rather than 90, which is
ample for the ten the flattest coupling needs but is the axis on which a denser coupling would run out.

**What this programme recommends, and how far.** On flatness and on published folding yield, **`10 × 6`**.
Both criteria point the same way, which was not expected and is why a falsifier was declared against it.
**And the third criterion has since joined them, iteration 35 (`C-0141`): the footprint too**, which is what
takes this from a recommendation to a correction. **The footprint is still a specification and not a result** —
it changes the actuation force per unit area, the electrode area, and anything downstream that assumed a
~40 nm square. What has changed is **which way it points**: ~~**We can rank what the two tiles BUY and we cannot
rank what the smaller one COSTS you**~~ — corrected, `10 × 6` is **0.929467162** of §3's footprint and `15 × 4`
is **1.40084263×** it, so the tile this programme recommends is the one that **fits** and there is no cost to
rank. The column this programme has never had is still missing; it is simply no longer the column this
decision turns on.

~~**What deferring costs.** Little, and this is worth saying: the four-layer result stands either way, and
`15 × 4` is a buildable, published, gel-verified cross-section that is flat as designed. Deferring means
carrying the interlayer-coupling calibration as a live dependency on the flatness verdict, and carrying the
coupled-flatness result at 1 of 8 cells rather than 8 of 8.~~
**WITHDRAWN, iteration 35 — the four-layer result does NOT stand either way.** On the corrected cross-section
`15 × 4` dishes **0.101759944** at the measured band's adverse end and **fails** `T-5b`, and it is **0 of 8**
coupled cells at both ends of that band. See the re-priced cost at the head of this section.

**And if this costs something, what?** — the line decision 1 taught us to leave open.

---

## 8. NEW — which width is the Gen-1 tile SPECIFIED to: the row length or the bounding box? (`T-242`)

**RAISED, iteration 36.** It follows from decision 7's own answer, in the way decision 7 followed from
decision 5's: once the tile is a **two-length honeycomb raster**, ~~*"the tile is 38.08 nm along the helices"*~~
**a row-span reading** and *"the tile is 39.44 nm along the helices"* are **both true of the same folded object**, and §3 states one
number. **The row-span reading is `112 bp = 38.08 nm` at the superseded pair and `109 bp` at the drawable one
— see the block below.**

> **UPDATED, iteration 38 — the QUESTION survives the closing raster and every number in it moved**
> ([`C-0151`](gpd/claims/C-0151-closing-raster-selection.md),
> [`CH-0195`](gpd/challenges/CH-0195-both-graded-column-counts-belong-to-an-undrawable-raster.md)).
> The recommended pair is now **`102 / 109 bp`**, so the **bounding box is unchanged at 116 bp = 39.44 nm**
> and the **row span is 109 bp**, which is the box less the **7 bp = 2.38 nm** stagger rather than less
> 4 bp = 1.36 nm.
> **And the mechanism the question was priced on is gone**: the *twelfth* crossover column was a property of
> the bounding box at `112 / 108` (`C-0148` §4), and the eleventh belongs to a raster that does not close, so
> **both graded column counts belonged to an undrawable design**. At `102 / 109` the interface window is
> **102 bp** and the count is **10** at *all three* `EDGE_MARGIN` conventions — 0.05, 0.17 and 0.34 nm, slack
> 2.45 / 2.21 / 1.87 nm — so **the numerical guard decides nothing and no flatness cell now turns on the
> width convention at all.**
> **The question is therefore a drafting question and no longer a margin**, which is a simplification of it
> rather than a withdrawal: §3 still states one number and the object still has two.
> Everything below is the reasoning as it stood at iteration 36, struck where a number is superseded.

**The question.** §3's parameter table says *"Tile footprint — 40 × 40 nm (test tiles up to ~70 × 100 nm)"*,
with no convention attached — and **the tilde is on the test tiles, not on the nominal.**
This programme has written that row as *"~40 × 40 nm"* throughout, which quietly supplies a tolerance §3 does
not state; **§3 states the footprint without one**, which is what makes the ambiguity a question rather
than a rounding — ~~3.57 %~~ **the `7 bp = 2.38 nm` stagger of the drawable raster, iteration 38**.
For a two-length raster there are two readings, and they are not the same tile:

| reading | value | what it is |
|---|---|---|
| **row length** | ~~**112 bp = 38.08 nm**, `−4.80 %` of 40.0~~ **`109 bp` at the drawable `102 / 109` pair, iteration 37** | the span **every one** of the block's x-raster rows actually has — at all ten rows, and at **every one** of `C-0140`'s five candidate length pairs. It is the **larger** of the pair's two lengths, so it moves with the pair: `112 bp` at 112 / 108 and **`109 bp`** at `102 / 109` (`C-0151` §9) |
| **bounding box** | **116 bp = 39.44 nm**, `−1.40 %` of 40.0 | the axial extent the folded object occupies, i.e. the dimension a micrograph or an AFM trace measures. **Unchanged by the closing correction** — the two pairs give the same box |

~~**The `3.57 %` between them is not a length. It is a `4 bp = 1.36 nm` inter-row STAGGER**~~
**The difference between them is not a length. It is the inter-row STAGGER — `4 bp = 1.36 nm` at the
superseded pair and `7 bp = 2.38 nm` at the drawable one (RESTATED, iteration 38, `C-0151`)**
([`C-0146`](gpd/claims/C-0146-coupled-cells-at-the-two-length-raster.md) §1):
every raster row spans the **larger** of the two lengths exactly, and the block exceeds it by exactly the
stagger — 3 to 8 bp across the candidate pairs, and **at least 7 bp for any raster that closes**, because the
three closing residue classes all differ by `14 (mod 21)` (`C-0151` §1, `CH-0194`). **A short helix is recessed inside its own row's window
rather than shortening it** (`C-0147` reconciles the per-row and per-helix readings and they agree).

**Why this programme cannot decide it.** Every plate and grillage model in this repository takes a single
`lengthX`, so **the stagger is not representable at all** — not approximately, not coarsely. `C-0146` carries
both readings and prefers neither, and `C-0146` §9 records the choice as a specification question in as many
words. It is not a modelling gap that more work would close; it is a statement about which dimension of a
staggered object the specification is a statement about.

~~**What it is worth, so the question has a threshold.** It is **not** cosmetic, and the channel is the one
nobody would guess: the crossover-column count. A 116 bp box clears **eleven** honeycomb crossover pitches by
**0.07 nm** — one fifth of a base-pair rise — so `CrossoverLayout.EDGE_MARGIN` admits a **twelfth** column at
its standing 0.05 nm and refuses it at half a rise. Under the measured staple dropout at the 90th percentile
that one column is the difference between **six flat cells of eight and three**
([`CH-0185`](gpd/challenges/CH-0185-a-bounding-box-crossover-column.md)); the row-length reading gives **four**.
The recommended cell — one column, ten paths, equal springs on `10 × 6` — is flat at **all** of them
(**0.0662801686** at twelve columns, **0.0708759349** at eleven, **0.0708859619** and **0.0754995025** at the
measured band's adverse end), so **no recommendation turns on the answer; a margin does.**~~

**WITHDRAWN, iteration 38 — the threshold belonged to a raster that cannot be drawn**
([`CH-0195`](gpd/challenges/CH-0195-both-graded-column-counts-belong-to-an-undrawable-raster.md),
[`C-0151`](gpd/claims/C-0151-closing-raster-selection.md) §4).
The twelfth column is a property of the bounding box at `112 / 108` and the eleventh of the rows of the same
pair, and **`112 / 108` does not close on caDNAno's `±5 bp` rule**, so *both* graded counts were read at an
undrawable design. At the drawable `102 / 109` the interface window is **102 bp**, the count is **10**, and it
is **10** at all three `EDGE_MARGIN` conventions — 0.05, 0.17 and 0.34 nm, slack **2.45 / 2.21 / 1.87 nm** —
so the guard is **inert** and **the width convention decides no flatness cell at all**.
What the tenth-versus-eleventh column *is* worth is the cost of **closure**, not of the convention: **2** flat
cells of eight against 3, a uniform **1.0567397–1.09611647×** of the dishing, and the recommended cell — one
column, ten paths, equal springs on `10 × 6` — flat at both ends of the measured band, **0.0773373597** at
`f = 0.30` and **0.0821458169** at `f = 0.26` against `T-5b`'s 0.10.
**So the question is now purely a drafting one, and nothing this programme recommends turns on it.**

**The options, and what each costs.**

| option | what it means | what it costs |
|---|---|---|
| **A — the row length, ~~38.08 nm~~ `109 bp`** | §3's dimension is the span of a row; the block may protrude by the stagger | every plan margin in the corpus is quoted against a width ~~**1.36 nm**~~ **2.38 nm at the drawable pair** smaller than the object's own extent, so a body placed to the specification can overhang it |
| **B — the bounding box, 39.44 nm** | §3's dimension is the extent of the object | ~~the flatness verdict inherits a crossover column decided by a **0.07 nm** slack against a numerical guard, which is `CH-0185` and is a defect to repair, not a result~~ **NOTHING, iteration 38** — that cost belonged to `112 / 108`; at the drawable pair the count is row-derived and the guard is inert (`C-0151` §4) |
| **C — specify the row length AND a stagger tolerance** | two numbers instead of one | nothing this programme can see; it is the only option under which both readings are stated rather than one inferred |
| **D — require a uniform row** | no stagger exists | it is decision 7b's route B: it fits M13 by **two nucleotides** and only **strained**, at **6.54349121–12.112167 pN** per turn — at or past the 10 pN unzip allowable — unless the scaffold is p8064, which affords 22 nt and is itself **RE-POSED** as decision 7b (`CH-0180`) |

**Programme's recommendation: none, and that is the honest answer.** Both readings are true of the same block
and this repository has no model that can tell them apart. If pressed it would state **C**, because it is the
only option that does not require a reader to infer which of two true numbers was meant — but that is a
drafting preference, not a result.

**And the price line is left explicitly open.** This programme can rank what an option **buys** and has no
column at all for what it **costs** — `CLAUDE.md` records that lesson, and decision 1's answer taught it.
So the question is asked with its price line open rather than as a menu:

> **Which width is the Gen-1 tile specified to — the ~~112 bp = 38.08 nm~~ `109 bp` row span, the 116 bp =
> 39.44 nm block
> extent, or both with a stated stagger? And if this costs something — in metrology, in how a folded object is
> accepted at the bench, or in a downstream drawing this programme cannot see — what?**

**RESTATED, iteration 38.** The row span is `109 bp` at the drawable `102 / 109` raster and the block extent
is unchanged; the stagger a *closing* raster carries is at least **7 bp = 2.38 nm**, never 3 or 4.

**What deferring costs.** Every plan margin, every packing verdict and every four-layer flatness cell in the
corpus is quoted at whichever of the two widths its author happened to take~~, and the two differ by 3.57 %.
`T-243` is queued to derive the crossover-column count from the **row spans** rather than from `edgeX`, which
removes the guard's vote but not the question.~~
**RESTATED, iteration 38: `T-243` is DONE** (iteration 36, [`C-0148`](gpd/claims/C-0148-face-bond-class-residues-and-row-span-columns.md)),
and it did remove the guard's vote — a crossover column serves an **interface**, so its window is the
intersection of two row spans and not the bounding box, and at the drawable raster that window gives **10**
columns at every guard convention. **What deferring now costs is the drafting ambiguity alone**: two true
numbers for one dimension of one object, differing by the **7 bp = 2.38 nm** stagger, with **no margin and no
verdict on either side of it**.

**RE-READ AGAINST ITERATION 39, AND NOTHING IN IT MOVES THIS QUESTION — stated rather than left silent,
because a question put to a customer can carry a stale price.** The block is now a committed scadnano file
([`C-0160`](gpd/claims/C-0160-scadnano-writer.md)) and reads `ADMISSIBLE` at zero forced crossovers
([`C-0164`](gpd/claims/C-0164-lattice-aware-buildability.md)) — and **both** width readings are re-derived on
that same file, so drawing the object did not choose between them and could not have. There is still no margin
on either side and this programme still states no preference. What the file *does* add is that the ambiguity is
now inspectable rather than described: whoever answers can open the object in the field's own software and see
one dimension carrying two true numbers.


## Two questions that were on this list and have been DISCHARGED

Recorded because a list that only ever grows is not a record.
`C-0071` found both had stopped applying and nobody had noticed —
they were raised by the flexure-and-tie branch that `CH-0081` removed from the output role.

| was | question | why it no longer applies |
|---|---|---|
| `T-95` | May the output superstructure be perforated under each flexure midspan? | The recommended element (`C-0071`'s `Q5`) has no tie grid, so nothing crosses the standoff base plane |
| `T-102` | May §3's tile grow by 1.44× in area? | Raised by the flexure array's minimum body; that array is no longer the output stage | 

---

## Two asks that are resources rather than decisions

**Access.** Two sources would convert a bound into a number and neither is reachable by an automated
fetch (ACS, Elsevier, Springer and IOP all refuse; Crossref and EuropePMC serve only abstracts):

- **Boucher & Hines, *J. Polym. Sci. Polym. Phys. Ed.* 14:2241 (1976)** — the only study measuring
  Group II chlorides against PEO, and so the only source for the θ-versus-[MgCl₂] curve *including
  the minima* that make a linear salting-out coefficient ill-posed.
- **Any PEG/PEO salt study below 50 mM.** `C-0007` found none; every cloud-point and aqueous-two-phase
  paper works at 0.1–3 M. This may simply not exist rather than be paywalled.
  
Neither is blocking: `C-0007` bounds the effect they would pin at **≤ 0.4 %** of the layer modulus and
states it as a falsifiable threshold rather than a guess.

**Compute.** ~~`T-50` (above) and~~ `T-51` (field-theoretic simulation of the grafted layer, costed at
weeks) exceeds what one session can run on the current box, and is **not** warranted — `C-0019` bounds the
layer response at ≤ 9.4 % without it. ~~`T-50` is warranted only if decision 1 comes back as
"hold 2 mM".~~
**CORRECTED, 2026-08-18 — this is the one prediction in this file the answers falsified.**
Decision 1 came back as neither *"adopt 0.5 mM"* nor *"hold 2 mM"* but as **a price**, so 2 mM remained the
nominal and `T-50` remained warranted. A binary was assumed where the answer was a cost, which is the same
failure this file records elsewhere as *"a one-sentence answer is enough"*: it was, and the sentence was not
one of the two on offer.
**AND THEN IT WAS ANSWERED WITHOUT THE COMPUTE, 2026-08-19 (`C-0137`).** The multi-week Monte Carlo was
**not run** and is **not** superseded — the regime still has no systematic theory. What `T-50` returned
instead is `P-6`'s ceiling and threshold, and the reason that was available at all is that the quantity
`C-0005` bounds (a **level**) is not the quantity the margin is (a **stiffness at a force-pinned point**).
~~**No compute request stands against this programme today**; the only item still exceeding one session is
`T-9`, the crossover hinge constant from oxDNA, at days rather than weeks.~~
**CORRECTED, iteration 39 — that sentence names as outstanding a run this programme had already made, and it
stood for four iterations.** The **crossover hinge constant itself was run in iteration 35**
([`C-0157`](gpd/claims/C-0157-crossover-hinge-constant-from-oxdna.md),
`gpd/results/T-9-crossover-hinge-constant.json`): oxDNA2, five replicas, **a day on 8 cores**, bracketing `k_θ`
at **`5.62052112 – 25.9227606 pN·nm/rad`** with the corpus's fitted `13.5294118` inside it. **This is
`C-0067`'s standing failure in its own direction**: the sentence under-claims — it says a thing is still owed
that was delivered — and a reviewer checks the assertions, not the disclaimers.
**What is genuinely outstanding is now THREE oxDNA items, and one of them is new this iteration.**
`T-9` is **`PARTIALLY DONE`** in [`TASKS.md`](TASKS.md) and stays live on two of its three deliverables: the
crossover's **vertical / axial compliance** (`C-0009` models it rigid in `z`) and its **in-plane shear `k_s`**
(`C-0020`'s single undetermined input). The third is
[`CH-0209`](gpd/challenges/CH-0209-a-crossover-drawn-as-two-strand-crossings.md), raised in iteration 39: the
field's own reference generator draws every Rothemund crossover as **two** strand crossings at adjacent offsets
where this corpus draws **one**, and `C-0157` relaxed only the second. Settling it is one relaxation of the
shape `T-9` already has a driver for — the same 15 × 112 bp sheet with each crossover drawn as a pair — and
**all three of its possible outcomes are useful**. None of the three exceeds this box; what they cost is days.
**Nothing here is an ask of NDI.** It is a correction to this section's own record, kept visible because a file
that only ever grows is not a record.

---
~~Two~~ **Three** things worth flagging about the document itself:

It answers a question you didn't ask. The four decisions were previously scattered across TASKS.md rows written for agents, not for a reviewer — each one buried in a paragraph of provenance. This form
separates what is being asked from what the programme found, which is what makes it reviewable in one pass rather than four.

The discharged section is deliberate. C-0071 found two questions had been carried to NDI for iterations after they stopped applying, and C-0067 found the same failure in ANSWERS.md's "cannot answer" list —
an entry that stood seven iterations after being answered. A questions-for-NDI file is exactly the kind of document that only ever grows, so it needs a place to record removals or it will drift the same
way.

**This file is now checked, and it was not before.**
~~`ANSWERS.md` had four retained checkers on it — numbers, task statuses, challenge statuses and self-consistency
— and `DECISIONS-FOR-NDI.md`, which is the document NDI actually reads, had none.~~
**UPDATED, iteration 39 — there are now SEVEN, and every one of them reads this file.** Numbers, task
statuses, challenge statuses and self-consistency ([`tools/trace-answers.py`](tools/trace-answers.py), which
takes `--answers` and defaults to **both** documents); corpus links
([`tools/check-corpus-links.py`](tools/check-corpus-links.py)); Markdown table rendering
([`tools/check-markdown-tables.py`](tools/check-markdown-tables.py)); the challenge index
([`tools/check-challenge-index.py`](tools/check-challenge-index.py)); the queue's entry points
([`tools/check-entry-points.py`](tools/check-entry-points.py),
[`C-0163`](gpd/claims/C-0163-cold-start-entry-points.md)); result-file hygiene
([`tools/check-result-file-hygiene.py`](tools/check-result-file-hygiene.py),
[`C-0158`](gpd/claims/C-0158-prose-gate-red.md)); and, since iteration 39, **dangling identifiers** —
a bare challenge number written into a sentence being *neither a filename nor a link*, so no earlier gate
could see one
([`tools/check-corpus-identifiers.py`](tools/check-corpus-identifiers.py),
[`C-0166`](gpd/claims/C-0166-dangling-identifiers.md)). All seven are wired into
[`tools/verify.sh`](tools/verify.sh).
**A count of checkers is itself a self-describing number and this one was stale**, which is the same class
the paragraph above it records.
`T-184` ([`C-0124`](gpd/claims/C-0124-decision-file-drift.md)) enumerated **36** of its assertions and found
**23** needing an edit: fourteen stale outright, four superseded by an answer block in their own section and
left unstruck below it, three numbers carried by an open challenge that names this file by name, and two
places where the file contradicted itself.
Every one of them under-claims — *"nothing has evaluated"*, *"cannot be scoped"*, *"has not been run"* — which
is `C-0067`'s standing finding that **a deliverable that under-claims is as wrong as one that over-claims and
is far harder to catch**, because a reviewer checks the assertions and not the disclaimers.
The repair is that `tools/trace-answers.py` now reads **both** documents by default, and that a `~~struck~~`
statement is no longer read as a live one — without which the only repair this project permits leaves the flag
where it was.
