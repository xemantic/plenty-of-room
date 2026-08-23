#!/usr/bin/env python3
"""T-255 -- classify every crossover of a caDNAno legacy design ALLOWED or FORCED against
caDNAno's own published rule, with no datum chosen and no lattice direction assumed.

The rule, quoted in `src/main/kotlin/tile/HoneycombBondClassResidues.kt` from the caDNAno paper
(Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, *Nucleic Acids Res.* 37:5001):

  "Our default rules allow antiparallel crossovers between ADJACENT staple helices only where the
   strand backbones arrive at points of closest proximity, which repeat every 21 base pairs if the
   helical twist is fixed at 10.5 base pairs per turn. Thus for a given staple helix, potential
   staple-crossover positions occur every seven base pairs, or two-thirds of a turn. Our default
   rules allow antiparallel crossovers between adjacent scaffold helices to occur five base pairs,
   or half a turn, upstream or downstream of allowed crossover positions for the associated staple
   helices."

TWO independent tests fall out of it, and they are reported separately because they fail
differently.

**Test A -- ADJACENCY.** "between adjacent helices". A crossover between two helices that are not
nearest neighbours on the caDNAno grid is forced by construction. This is pure geometry: it needs
no residue, no datum and no twist. It cannot be argued with.

**Test B -- REGISTER, per bond.** A crossover SITE occupies two consecutive bases `(o, o+1)`
(caDNAno renders one antiparallel crossover as two strand crossings; `CLAUDE.md` records the same
doubling in the field's own generator). So a crossing at base `i` lies on an allowed site iff
`i mod 21` is in `{r, r+1}` for the bond's own site residue `r`. **Testing the crossing residue
rather than the site level removes the whole question of which half of a site a lone crossing is**,
which is otherwise a convention -- a staple nicked at one half of a site leaves exactly one
crossing behind, and so does a raster turn at a helix end.

Per bond: the staple crossing residues must lie inside one `{r, r+1}` window; the scaffold ones
inside `{r-5, r-4} U {r+5, r+6}`. A bond failing either carries a forced crossover.

Test B's KNOWN BLIND SPOT, stated rather than discovered: a LONE crossing displaced by exactly
`+1 bp` lands on `r+1`, which the window admits. A doubled crossover displaced by 1 bp is caught
(its two crossings are `r+1, r+2`). So Test B is a LOWER bound on the forced count.

A bond carrying scaffold crossovers and no staple crossovers cannot be scored on B at all and is
reported `unscorable`, never silently `allowed`.
"""
from collections import Counter, defaultdict
import cadnano_legacy as C

CONST = C.parse_lattice_constants()
PERIOD = CONST["samePairPeriodBp"]
CLASS_STEP = CONST["classStepBp"]
OFFSET = CONST["scaffoldOffsetBp"]
CLASSES = CONST["classes"]


def _signed(delta):
    """`delta` mod PERIOD folded into `(-PERIOD/2, +PERIOD/2]`."""
    d = delta % PERIOD
    return d - PERIOD if d > PERIOD // 2 else d


def _signed_departure(residue, allowed):
    """The SMALLEST-MAGNITUDE signed base-pair departure from `residue` to any member of
    `allowed`. Ties (equal magnitude, opposite sign) take the positive one, deterministically."""
    return min((_signed(residue - w) for w in allowed), key=lambda x: (abs(x), -x))


def _window(r):
    return {r % PERIOD, (r + 1) % PERIOD}


def _scaffold_window(r):
    return _window(r + OFFSET) | _window(r - OFFSET)


def _fit_site_residue(residues):
    """The site residue `r` whose window `{r, r+1}` contains every observed crossing residue,
    or None if no single window does. Ties are broken by the smaller `r`, deterministically."""
    obs = set(residues)
    fits = [r for r in range(PERIOD) if obs <= _window(r)]
    return fits[0] if fits else None


SQUARE_PERIOD = 32   # Ke, Douglas, Bathe, Shih, JACS 131:15903 -- four azimuths at 8 bp,
SQUARE_STEP = 8      # the same PAIR every 32 bp. Quoted in `CLAUDE.md`; not in the Kotlin source.


def period_fit(d, period):
    """Fraction of staple bonds all of whose crossing residues fall inside one `{r, r+1}` window
    at this period. A design's own lattice is the period this is 1.0 at."""
    bonds = defaultdict(list)
    for (ha, ia), (hb, ib) in d.crossings("stap"):
        bonds[(ha, hb)].append(ia % period)
    if not bonds:
        return 0.0
    good = 0
    for res in bonds.values():
        obs = set(res)
        if any(obs <= {r % period, (r + 1) % period} for r in range(period)):
            good += 1
    return good / len(bonds)


def lattice_by_period(d):
    """`honeycomb`, `square` or `undetermined`, from the register period the design's own staple
    bonds obey. Needs no parity and no threshold: it reports which of the two published periods
    fits more bonds, and `undetermined` when they tie."""
    h = period_fit(d, PERIOD)
    s = period_fit(d, SQUARE_PERIOD)
    if h > s:
        return "honeycomb", h, s
    if s > h:
        return "square", h, s
    return "undetermined", h, s


def alignment_census(d):
    """Test C. An antiparallel crossover joins two helices at the SAME base index -- that is what
    "the strand backbones arrive at points of closest proximity" means. A strand connection whose
    two endpoints sit at DIFFERENT base indices is not a lattice crossover at all; it is a manual
    connection, and its axial offset in base pairs is a number the file states outright.

    This test needs no residue, no window, no parity and no datum, and it cannot be argued with."""
    out = []
    for role in ("stap", "scaf"):
        for (ha, ia), (hb, ib) in d.crossings(role):
            if ia != ib:
                out.append({"role": role, "helixA": ha, "helixB": hb,
                            "baseA": ia, "baseB": ib, "axialOffsetBp": ib - ia})
    return out


def adjacency_census(d):
    """Test A. A crossover between two sites that are not NEAREST NEIGHBOURS on the design's own
    lattice is forced by construction.

    On the honeycomb a vertical `(+-1, 0)` pair is a neighbour at only ONE parity of `(row + col)`
    at its upper site; the other parity is a next-nearest pair, further apart than the lattice
    constant, and a crossover there is forced. The design's own majority parity supplies the datum,
    so nothing is asserted about which parity caDNAno calls which.

    Returns `(displacementCounter, forced)` where `forced` is a list of dicts."""
    disp = Counter()
    parity = Counter()
    edges = []
    for role in ("stap", "scaf"):
        for (ha, ia), (hb, ib) in d.crossings(role):
            ra, ca = d.pos[ha]
            rb, cb = d.pos[hb]
            dd = (abs(rb - ra), abs(cb - ca))
            disp[dd] += 1
            par = None
            if dd == (1, 0):
                lo = (ra, ca) if ra < rb else (rb, cb)
                par = (lo[0] + lo[1]) % 2
                parity[par] += 1
            edges.append((role, ha, hb, ia, dd, par))
    majority = parity.most_common(1)[0][0] if parity else None
    lat, _, _ = lattice_by_period(d)
    forced = []
    for role, ha, hb, ia, dd, par in edges:
        why = None
        if dd not in ((0, 1), (1, 0)):
            why = "not a nearest-neighbour pair on any lattice"
        elif dd == (1, 0) and lat == "honeycomb" and par is not None and par != majority:
            why = "a vertical pair at the minority honeycomb parity is next-nearest, not adjacent"
        if why:
            forced.append({"role": role, "helixA": ha, "helixB": hb, "base": ia,
                           "displacement": "%d,%d" % dd, "why": why})
    return disp, forced


def register_census(d):
    """Test B."""
    stap = defaultdict(list)
    scaf = defaultdict(list)
    for (ha, ia), (hb, ib) in d.crossings("stap"):
        if ia == ib:  # a misaligned connection is Test C's finding, not a register datum
            stap[(ha, hb)].append(ia)
    for (ha, ia), (hb, ib) in d.crossings("scaf"):
        if ia == ib:
            scaf[(ha, hb)].append(ia)

    site = {}
    forced_stap, forced_scaf, unscorable = [], [], []
    offRegisterStapleBonds = 0
    for bond, idx in stap.items():
        res = [i % PERIOD for i in idx]
        r = _fit_site_residue(res)
        site[bond] = r
        if r is None:
            offRegisterStapleBonds += 1
            # the MINIMUM number of crossings that must move for this bond to obey the rule
            counts = [(sum(1 for x in res if x in _window(w)), -w) for w in range(PERIOD)]
            best, negw = max(counts)
            bestWindow = _window(-negw)
            excess = len(res) - best
            outliers = sorted(set(x for x in res if x not in bestWindow))
            forced_stap.append({
                "bestWindow": sorted(bestWindow),
                "outlierResidues": outliers,
                "departuresBp": [_signed_departure(x, bestWindow) for x in outliers],
                "helixA": bond[0], "helixB": bond[1], "crossings": len(res),
                "residues": sorted(set(res)),
                "minimumForcedCrossings": excess,
                "bases": sorted(idx),
            })
    for bond, idx in scaf.items():
        r = site.get(bond)
        if r is None:
            for i in sorted(idx):
                unscorable.append({
                    "helixA": bond[0], "helixB": bond[1], "base": i,
                    "why": ("no staple crossover on this bond" if bond not in stap
                            else "the bond's own staple register is itself off-rule"),
                })
            continue
        win = _scaffold_window(r)
        for i in sorted(idx):
            if i % PERIOD not in win:
                forced_scaf.append({
                    "helixA": bond[0], "helixB": bond[1], "base": i,
                    "residue": i % PERIOD, "siteResidue": r, "allowedResidues": sorted(win),
                    "departureBp": _signed_departure(i % PERIOD, win),
                })

    fitted = [r for r in site.values() if r is not None]
    used = sorted(set(fitted))
    classes_ok = len(used) <= CLASSES and all((x - used[0]) % CLASS_STEP == 0 for x in used)
    return {
        "stapleBonds": len(stap),
        "scaffoldBonds": len(scaf),
        "siteResiduesFitted": used,
        "bondsWithNoFittingWindow": sum(1 for r in site.values() if r is None),
        "siteResidueClassesConsistent": bool(classes_ok),
        "forcedStaple": forced_stap,
        "forcedScaffold": forced_scaf,
        "unscorableScaffold": unscorable,
    }


def census(d):
    disp, adj = adjacency_census(d)
    mis = alignment_census(d)
    reg = register_census(d)
    lat, hfit, sfit = lattice_by_period(d)
    out = {
        "helices": len(d.vstrands),
        "lattice": lat,
        "latticeByGridParity": d.lattice(),
        "honeycombPeriodFit": hfit,
        "squarePeriodFit": sfit,
        "displacements": {"%d,%d" % k: v for k, v in sorted(disp.items())},
        "stapleCrossings": len(d.crossings("stap")),
        "scaffoldCrossings": len(d.crossings("scaf")),
        "forcedByAdjacency": adj,
        "nForcedByAdjacency": len(adj),
        "misalignedConnections": mis,
        "nMisalignedConnections": len(mis),
    }
    out.update(reg)
    out["nOffRegisterStapleBonds"] = len(reg["forcedStaple"])
    out["nForcedStapleCrossings"] = sum(b["minimumForcedCrossings"] for b in reg["forcedStaple"])
    out["nForcedScaffoldCrossings"] = len(reg["forcedScaffold"])
    out["nUnscorableScaffold"] = len(reg["unscorableScaffold"])
    out["carriesForcedCrossover"] = bool(
        adj or mis or reg["forcedStaple"] or reg["forcedScaffold"])
    return out


# ------------------------------------------------------------------ self-tests

def _vs(num, row, col, length):
    return {"num": num, "row": row, "col": col,
            "scaf": [[-1, -1, -1, -1] for _ in range(length)],
            "stap": [[-1, -1, -1, -1] for _ in range(length)],
            "loop": [0] * length, "skip": [0] * length,
            "stapLoop": [], "scafLoop": [], "stap_colors": []}


def _fixture(stap_levels, scaf_levels, length=160, singles=(), pos=((0, 0), (0, 1))):
    """Two helices with straight runs and a DOUBLE crossover at each named level.
    `singles` names levels rendered as a lone crossing at base `o + 1`."""
    (ra, ca), (rb, cb) = pos
    a, b = _vs(0, ra, ca, length), _vs(1, rb, cb, length)
    for v, h in ((a, 0), (b, 1)):
        for i in range(10, length - 10):
            v["scaf"][i] = [h, i - 1, h, i + 1]
            v["stap"][i] = [h, i + 1, h, i - 1]
    for role, levels in (("stap", stap_levels), ("scaf", scaf_levels)):
        for o in levels:
            for j in (0, 1):
                a[role][o + j] = [1, o + j, 0, o + j] if j == 0 else [0, o + j, 1, o + j]
                b[role][o + j] = [0, o + j, 1, o + j] if j == 0 else [1, o + j, 0, o + j]
    for role, o in singles:
        i = o + 1
        a[role][i] = [0, i - 1, 1, i]
        b[role][i] = [0, i, 1, i + 1]
    return C.LegacyDesign({"vstrands": [a, b], "name": "fixture"})


def _selftest():
    ok = 0

    def check(name, cond):
        nonlocal ok
        assert cond, "FAILED: " + name
        ok += 1

    check("the 21 bp period is parsed out of the Kotlin source, not transcribed", PERIOD == 21)
    check("the 7 bp class step is parsed out of the Kotlin source", CLASS_STEP == 7)
    check("the 5 bp scaffold offset is parsed out of the Kotlin source", OFFSET == 5)
    check("the 3 neighbour classes are parsed out of the Kotlin source", CLASSES == 3)

    check("a crossover site's window is two consecutive residues", _window(20) == {20, 0})
    check("the scaffold window is the site window shifted both ways by five",
          _scaffold_window(20) == {4, 5, 15, 16})
    check("a residue set inside one window fits it", _fit_site_residue([20, 0]) == 20)
    check("a residue set spanning two base pairs fits no window",
          _fit_site_residue([20, 0, 1]) is None)
    check("a lone residue fits, and the fit is deterministic",
          _fit_site_residue([5]) == 4 and _fit_site_residue([5]) == _fit_site_residue([5]))

    r = census(_fixture([20, 41, 62], [15, 57]))
    check("a fixture built on the rule carries no forced crossover",
          r["carriesForcedCrossover"] is False)
    check("a fixture built on the rule has no adjacency violation", r["nForcedByAdjacency"] == 0)
    check("its fitted site residue is the one it was built at", r["siteResiduesFitted"] == [20])

    r = census(_fixture([20, 41, 62], [17, 57]))
    check("a scaffold crossover two base pairs off the rule is FORCED",
          r["nForcedScaffoldCrossings"] == 2 and r["nForcedStapleCrossings"] == 0)

    r = census(_fixture([20, 41, 64], [15]))
    check("a staple crossover two base pairs off the rule is FORCED", r["nForcedStapleCrossings"] > 0)
    r2 = census(_fixture([20, 41, 62, 83, 106], [15], length=200))
    check("an off-register staple bond names its own best window and its outlier's departure",
          r2["forcedStaple"][0]["bestWindow"] == [0, 20] and
          r2["forcedStaple"][0]["departuresBp"] == [1, 2])
    check("an off-register staple bond reports the MINIMUM number of CROSSINGS that must move, "
          "and one antiparallel crossover is two of them",
          r["nForcedStapleCrossings"] == 2 and r["nOffRegisterStapleBonds"] == 1)

    r = census(_fixture([20, 41], [15], pos=((0, 0), (0, 4))))
    check("a crossover between non-adjacent grid sites is FORCED by adjacency alone",
          r["nForcedByAdjacency"] == 6 and r["carriesForcedCrossover"] is True)
    check("the smallest-magnitude signed departure is taken, not the most negative",
          _signed_departure(19, [7, 8, 17, 18]) == 1)
    check("a departure of ten base pairs is reported as ten when nothing is nearer",
          _signed_departure(2, [12]) == -10)
    check("an equal-magnitude tie takes the positive departure, deterministically",
          _signed_departure(2, [12, 13]) == 10)
    check("a signed departure folds into half the period", _signed(19) == -2 and _signed(2) == 2)

    r = census(_fixture([20, 41, 62], [], singles=(("stap", 83),)))
    check("a LONE staple crossing on an allowed site is allowed, not forced",
          r["nForcedStapleCrossings"] == 0)
    r = census(_fixture([20, 41, 62], [], singles=(("stap", 85),)))
    check("a LONE staple crossing two base pairs off the site is FORCED",
          r["nForcedStapleCrossings"] > 0)

    r = census(_fixture([], [15, 57]))
    a, b = _vs(0, 0, 0, 60), _vs(1, 0, 1, 60)
    for v, hh in ((a, 0), (b, 1)):
        for i in range(10, 50):
            v["stap"][i] = [hh, i + 1, hh, i - 1]
    a["stap"][20] = [1, 31, 0, 19]
    b["stap"][31] = [1, 30, 0, 20]
    r = census(C.LegacyDesign({"vstrands": [a, b], "name": "misaligned"}))
    check("a strand connection between two DIFFERENT base indices is reported misaligned",
          r["nMisalignedConnections"] == 1 and
          r["misalignedConnections"][0]["axialOffsetBp"] == 11)
    check("a misaligned connection makes the design carry a forced crossover",
          r["carriesForcedCrossover"] is True)
    check("a misaligned connection is excluded from the register test's own datum",
          r["nForcedStapleCrossings"] == 0)

    r = census(_fixture([], [15, 57]))
    check("a scaffold bond with no staple crossover is UNSCORABLE, not allowed",
          r["nUnscorableScaffold"] == 4 and r["nForcedScaffoldCrossings"] == 0)

    r = census(_fixture([23, 44, 65], [18, 60]))
    check("the verdict is invariant under a datum shift of +3 base pairs",
          r["carriesForcedCrossover"] is False)

    d = [x for x in C.load_archives() if x.label == "ii_10x6"][0]
    check("the caDNAno paper's own 10x6 design parses as honeycomb by grid parity",
          d.lattice() == "honeycomb")
    check("and as honeycomb by register period, at a perfect fit",
          lattice_by_period(d) == ("honeycomb", 1.0, period_fit(d, SQUARE_PERIOD)))
    check("its 32 bp fit is strictly worse than its 21 bp fit",
          period_fit(d, SQUARE_PERIOD) < 1.0)
    check("it carries 60 helices", len(d.vstrands) == 60)
    check("its scaffold occupies 7560 bases -- 60 x 126, the paper's own allotment, exactly",
          sum(d.occupied_count(v["num"], "scaf") for v in d.vstrands) == 7560)
    r = census(d)
    check("EVERY staple bond of it fits one crossover-site window",
          r["bondsWithNoFittingWindow"] == 0)
    check("its site residues are at most three values seven apart",
          r["siteResidueClassesConsistent"])
    check("it carries ZERO forced crossovers on either test",
          r["carriesForcedCrossover"] is False and r["nForcedByAdjacency"] == 0)
    check("no scaffold bond of it is unscorable", r["nUnscorableScaffold"] == 0)
    print("self-test: %d assertions, all passed" % ok)
    return ok


if __name__ == "__main__":
    _selftest()
