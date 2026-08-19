# CH-0167 — The 123–214 % is a LEVEL, and 31 claims quote it as the error bar on a stiffness

| | |
|---|---|
| **Challenges** | The standing corpus-wide use of [`C-0005`](../claims/C-0005-mean-field-screening-validity.md)'s one-loop deviation as the uncertainty a stiffness margin, a fold margin or a window edge must be read against — **66 occurrences in 31 claims**, plus 8 in `ANSWERS.md`, 10 in `TASKS.md` and 6 in `DECISIONS-FOR-NDI.md`. Not `C-0005` itself, which states the quantity correctly. |
| **Raised by** | [`C-0137`](../claims/C-0137-beyond-mean-field-gap.md), task [`T-50`](../tasks/T-50-beyond-mean-field-gap.md) |
| **Raised** | 2026-08-19, iteration 32 |
| **Status** | **Open. No number of `C-0005` is wrong and no verdict reverses. What is challenged is the transfer, and it has been made 90 times.** |

---

## The statement being challenged

The canonical form, from `TASKS.md`'s own `T-50` row and repeated almost verbatim across the corpus:

> *"`C-0017`'s 1.19–1.42× margin sits inside `C-0005`'s 123–214 % electrostatic one-loop correction"*

and, as a maturity banner, in `C-0012`, `C-0017`, `C-0018`, `C-0021`, `C-0022`, `C-0023`, `C-0026`, `C-0027`, `C-0032`, `C-0033`, `C-0036`, `C-0047`, `C-0049`, `C-0050`, `C-0084`, `C-0091`, `C-0100`, `C-0110`, `C-0114`, `C-0132` and eleven others:

> *"`C-0005` puts the one-loop correction at 123–214 % of the leading term across this gap range, which is one to two orders of magnitude larger than the stability margin this task reports."*

## Why it is the wrong comparison

`C-0005`'s deviation is defined in its own text as **the ratio of the one-loop correction to the leading term of a PRESSURE**, `Ξ|P⁽¹⁾|/P_PB` — Naji et al.'s Eq. (19) over Eq. (13). It is a statement about the **level** of `μ − 1` in `|F_true| = μ(h)|F_PB|`.

A stability margin is `33.3333 / (|F_es|/ℓ − k_brush)` read at a **force-pinned** operating point, where `|F_es|` is fixed by the balance `|F_es| = 100 pN + P(g)A`. `C-0137` checks that pinning over `C-0017`'s own 54 records and finds the pinned force **identical across all three buffers at every `(model, height)`**, relative spread `0.0`. So the level of `μ` is absorbed into the bias and **the margin is not a function of it** — except through the two channels `C-0137` measures, which are `1.44 %` and `2.88×` the *gradient* threshold respectively.

The two quantities are not the same quantity, and the ratio of one to the other is not an error bar. `CH-0019` already made exactly this class of argument about the **polymer** loop parameter (*"two mean fields ... neither bounds the other"*); this is the same failure one level in, inside the electrostatic expansion itself.

## And three further things are wrong with the transfer

1. **It is a LIKE-CHARGED, COUNTERION-ONLY, SALT-FREE quantity.** Naji et al.'s Eqs. (13) and (19) are for two identically charged planar walls with their own counterions and no added salt. The actuated gap is **oppositely** charged, in 0.5–10 mM `MgCl₂`. `C-0005`'s own validity range says the attraction thresholds are *"not transferred"* to the tile-electrode pair — and then the *deviation ratio* is transferred, in the same claim and in thirty others.
2. **`μ = 1 − ratio` is NEGATIVE for every value in the band.** At 123–214 % the expansion has broken down, which `C-0005` says plainly; so the number cannot even be *used* as a multiplier, in either direction, without choosing a transfer that no source supplies. `C-0137` chooses the only one with a defensible sign for oppositely charged walls (`μ = 1 + ratio`, the correlation term adding to an existing attraction) and finds a **net margin of 1.0438** at `C-0017`'s binding state — still above one.
3. **For oppositely charged walls the loop expansion is exponentially better behaved, and this is published with Monte Carlo.** Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik give the weak-coupling validity criterion as `Ξ < D̃/ln D̃` for a repulsive mean-field pressure and, for the attractive (oppositely charged) branch, a bound whose *"right hand side here is exponentially large"* — concluding that *"for charged surfaces of opposite sign, the weak-coupling analysis performs far better"* — and back it with MC at `Ξ` up to **86**, where *"the simulation data and the analytical results nearly coincide for all rescaled separations"*. **`C-0005`'s alarm is calibrated on the like-charged problem.** (Read directly; `gpd/data/T-50-beyond-mean-field-literature.md` rows 25–28.)

## What follows, and what does not

**Does not follow.** That `C-0005` is wrong. It computes the right quantity, names it correctly, states its own geometry caveat, and lists *"no published `Ξ` criterion exists for oppositely charged walls"* as its own open item 4. Every number of it reproduced here to **0.14–0.47 %**.

**Does not follow.** That mean field is now established at the actuated gap. It is not, and `C-0137` says so. `Ξ = 17–24` at the duplex surface still has no systematic theory.

**Does follow.**

1. **Every maturity banner of the form *"the margin is inside `C-0005`'s 123–214 %"* is comparing a level with a stiffness and should be restated.** The correct qualifier is `C-0137`'s pair of thresholds — a force `1.48–2.22×` smaller, or a decay length `9.73 %` shorter — which are quantities of the same kind as the margin.
2. **`ANSWERS.md`, `DECISIONS-FOR-NDI.md` and `TASKS.md` carry it 24 times between them**, including in the passages that tell NDI what the programme's largest exposure is. Those are the ones that matter most.
3. **`C-0005`'s open item 4 is closed** by the Kanduč reference above, in the direction that favours this device.
4. **This is the ninth *"quote it with the state it is read at"* in this corpus, and the state is the QUANTITY'S OWN KIND.** A ratio of a correction to a leading term is an error bar on the thing that term *is*. Naming which quantity a percentage is a percentage **of** is as load-bearing as naming the compression a stiffness is read at.

## The remedy, and its cost

Not a mass edit. `C-0137` supplies the replacement qualifier; the honest minimum is to restate it in the **four** documents a reader outside the programme sees — `ANSWERS.md`, `DECISIONS-FOR-NDI.md`, `TASKS.md` and `C-0017` — and to leave the historical banners standing with a pointer, per `C-0071`'s *strike, never delete*. A per-claim sweep of the other 30 is queued as `T-220` rather than done here, because a banner that over-states an uncertainty is the **safe** direction and this challenge is not urgent in the way a wrong number would be.

## If this challenge is itself wrong

The way it fails is if the operating points that matter are **not** force-pinned. `C-0018`'s **free** load line is exactly that case, and there the level reaches the answer in full — so any banner attached to a *free-tile* quantity (a blocking force, a zero-bias resting position, `C-0021`'s well depth) is quoting the right error bar and should be left alone. The challenge is against its use on **held**, load-line quantities: stability floors, coupling margins, pull-in margins and the window edges built on them.
