# `T-303` — is a crossover's stiffness against a RELATIVE NORMAL DISPLACEMENT published anywhere?

`gpd/` was checked first, as `CLAUDE.md`'s research practice requires.
It has paid three times and it did not pay here:
the corpus's own crossover elastic constants are `k_θ` (`Gen1Tile.crossoverHingeStiffness`, Chen et al. 2014, **fitted to measurement**)
and `k_s` (`Gen1Tile.crossoverInPlaneStiffness`, **a construction**, and its KDoc says so in as many words:
*"Nothing in the accessible literature gives it in any form"*).

## What was searched

`query.py`, retained, eight EuropePMC queries with 8 s between them; `europepmc-queries.json` carries the hit counts and top 25 per query.

| query | hits |
|---|---|
| `DNA origami crossover elastic constant` | 62 |
| `Holliday junction stiffness molecular dynamics origami crossover` | 21 |
| `oxDNA crossover junction elasticity DNA origami` | 7 |
| `"DNA origami" AND "crossover" AND "spring constant"` | 35 |
| `"DNA origami" AND junction AND ("shear stiffness" OR "transverse stiffness")` | **0** |
| `all-atom molecular dynamics DNA origami crossover flexibility` | 52 |
| `"antiparallel crossover" DNA elasticity coarse-grained` | **0** |
| `DNA origami interhelical fluctuation junction stiffness` | 7 |

## What was read, and what it is about

| source | status | what it gives | why it is not the coordinate |
|---|---|---|---|
| Kaufhold, Pfeifer, Castro, Di Michele, *ACS Nano* **16**:8784 (2022), `PMC9245350` | **READ DIRECTLY** (PDF, `?pdf=render`) | metadynamics free-energy landscapes of a bistable Holliday junction and of *"a DNA origami-compliant joint"*, and its force response | the collective variable is a **joint angle** of a whole device, not the relative normal displacement of two crossover-bonded duplexes. No `pN/nm` and no spring constant anywhere in the text |
| Sengupta *et al.*, `PMC8789063` (2022) | **READ DIRECTLY** (PDF) | helical **base-pair-step** stiffness constants of an immobile four-way junction against canonical B-DNA | a base-pair step's six helicoidal coordinates inside one duplex, not an inter-duplex coordinate at all |
| Snodin *et al.*, *NAR* **47**:1585 (2019) | already in `gpd/data/T-137-weave-literature.md`, **READ DIRECTLY** there | the inter-helix **distance** and *"the standard deviation in this distance"* along a 2D tile, smallest at the junctions | the **line of centres**, which is the coordinate the link is *perpendicular* to. It bounds a different spring |
| Chen, Weng, Riccitelli, Cui, Irudayaraj & Choi, *JACS* **136**:6995 (2014) | already in `Gen1Tile`, **CITED, fitted to measurement** | `k_θ = 2αB/(100a)`, a **rotation** | the only crossover elastic constant ever fitted, and it is not a displacement |

## The finding

**No published number for a DNA-origami crossover's stiffness against a relative normal displacement of the two duplexes it joins was found**,
in any of all-atom MD, oxDNA, metadynamics or experiment.
That is the same absence `Gen1Tile.crossoverInPlaneStiffness`'s own KDoc records for the *in-plane slip*,
and it is why `T-303` closes the way this programme closes an unsourceable coefficient:
**a ceiling and a threshold instead of a value.**

## How the two PDFs were fetched

`curl -sL "https://europepmc.org/articles/<PMCID>?pdf=render" -o <PMCID>.pdf`, then
`pdftotext -layout`. Only the extracted text is retained here — the PDFs are 4.7 MB each and
neither turned out to carry the coordinate, so the text is the evidence and the URL is the
reproduction.
