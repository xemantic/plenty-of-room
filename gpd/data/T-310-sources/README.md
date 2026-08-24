# `T-310` — is a crossover's stiffness against a change of the INTERHELICAL SEPARATION published anywhere?

This is **not** `T-303`'s question, and that is the reason it is asked again.

`T-303` searched for a crossover's stiffness against a relative **normal displacement** and found none;
its own reading of Snodin et al. sets the *other* eigenvalue aside in one clause —
*"the inter-helix **distance** … which is the line of centres, the coordinate the link is **perpendicular** to"*.
`CH-0259` is precisely about that perpendicular coordinate:
at the 300 of 435 bonds that run through a `10 × 6` block's thickness, `unitZ² = 0.75`,
so three quarters of the link is the **radial** mechanism — a change of the separation — and one quarter the shear.
The separation is a coordinate the field **does** measure the mean of.

`gpd/` was checked first, as `CLAUDE.md`'s research practice requires, and it paid twice:
`gpd/data/T-137-weave-literature.md` already carries Snodin et al. and Bai et al. read directly,
and `electrostatics/DuplexPairSeparation.kt` already carries the **measured** Mg²⁺ DNA–DNA equation of state
(Meng, Timsina, Bull, Andresen & Qiu, *Biophys. J.* **118**:3019, 2020),
which is where this task's one measured term comes from.

## What was searched

`query.py`, retained, eight EuropePMC queries with 8 s between them, naming the **separation** rather than the shear;
`europepmc-queries.json` carries the hit counts and the top 25 per query.

| query | hits |
|---|---|
| `"DNA origami" AND "interhelical distance" AND (fluctuation OR variance OR "standard deviation")` | 5 |
| `"DNA origami" AND crossover AND ("interhelical" OR "inter-helix") AND stiffness` | 19 |
| `oxDNA "DNA origami" inter-helix distance distribution junction` | 7 |
| `all-atom molecular dynamics DNA origami interhelical spacing free energy` | 13 |
| `"DNA nanostructure" AND "helix-helix" AND (compressibility OR "elastic modulus")` | 2 |
| `DNA origami lattice constant fluctuation SAXS Debye-Waller` | 1 |
| `"four-way junction" AND ("stacking-unstacking" OR "arm separation") AND free energy landscape` | **0** |
| `coarse-grained model DNA origami crossover spring constant interhelical separation` | 2 |

Every query but the last two returns the same small set,
and the two candidates on the coordinate are `PMC3864285` and `PMC6379721`.

## What was read, and what it is about

| source | status | what it gives | why it is not the number |
|---|---|---|---|
| Yoo & Aksimentiev, *PNAS* **110**:20101 (2013), `PMC3864285` | **READ DIRECTLY** (PDF, `?pdf=render`; extracted text retained here) | all-atom MD of a honeycomb and a square monolith; Fig. 2D is *"the distribution of local inter-DNA distances"*, Fig. 3B *"the distribution of the intra- and interhelical distances between the four base pairs nearest to the junction … averaged over all staple crossovers"*, and Fig. 4E the bending and twist moduli `α₁,₂,₃` | the elastic constants it **fits** are the **bundle**'s — *"the generalized torsional energy density, `Eᵢ(s) = k_B T αᵢ(ωᵢ − ω⁰ᵢ)²/2`, where `α₁,₂` are the bending moduli and `α₃` is the twist modulus"* — and the inter-duplex distance appears only as a **distribution**, with no `σ` in the text and no spring constant anywhere. Worse for an equipartition reading, that distribution runs `18–30 Å`, which is the **deterministic weave** (`CLAUDE.md`: *a deterministic pattern whose phase you know is not a tolerance*), so `k_BT/σ²` taken on it would measure the sawtooth and not the junction |
| Snodin *et al.*, *NAR* **47**:1585 (2019), `PMC6379721` | already in `gpd/data/T-137-weave-literature.md`, **READ DIRECTLY** there | the inter-helix distance along a 2D tile and, verbatim, *"the fluctuations, which are smallest at the junctions and largest at the midpoints between the junctions, are significantly smaller in magnitude than the variation in the interhelical distance due to the weave pattern itself"* | that is the right coordinate at the right place and it is **qualitative**: *"significantly smaller"* than `1.5 nm` is not a `σ`, and equipartition on it returns a bound with no number in it |
| Meng, Timsina, Bull, Andresen & Qiu, *Biophys. J.* **118**:3019 (2020) | already in `gpd/data/T-139-dna-dna-force-literature.md`, **READ DIRECTLY** there, and in `electrostatics/DuplexPairSeparation.kt` as `MengMagnesium` | `Π(d) = Π_R e^(−d/λ)` with `Π_R = 201.8 GPa` and `λ = 2.4 Å`, osmotic stress plus X-ray diffraction at 20 mM MgCl₂, data down to `24.5 Å` | this **is** on the radial coordinate and it is used: `V″(d)` over one crossover's 21 bp of interface is the study's only **measured** term. It measures the **duplex pair**, not the crossover, so it is a *term* of the radial constant and not the constant |

## The finding

**No published number for a DNA-origami crossover's stiffness against a change of the interhelical separation was found**,
in all-atom MD, oxDNA, metadynamics, SAXS or experiment —
the same absence `T-303` recorded on the perpendicular coordinate,
and the same absence `Gen1Tile.crossoverInPlaneStiffness`'s own KDoc records for the in-plane slip.

What is new is that the radial axis has a **measured term** where the transverse one has none,
and that the two published distributions of the coordinate are both unusable for equipartition **for a stated reason**:
one is dominated by a deterministic pattern and the other is quoted only in words.

So `T-310` closes the way `T-303` did and the way this programme closes an unsourceable coefficient:
**a bracket and a threshold instead of a value** — with one term of the bracket now measured.

## How the PDF was fetched

`curl -sL "https://europepmc.org/articles/PMC3864285?pdf=render" -o PMC3864285.pdf`, then `pdftotext -layout`.
Only the extracted text is retained here — the PDF is 2.1 MB and the coordinate is not in it,
so the text is the evidence and the URL is the reproduction.
