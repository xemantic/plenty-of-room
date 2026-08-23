#!/usr/bin/env python3
"""T-255 — a reader for the caDNAno **legacy** `.json` design format.

Why Python and not the Kotlin scadnano reader: the legacy format is a different document from
scadnano's, the arithmetic needed is integer and closed form, and a compile cycle under
four-agent contention costs minutes where this costs seconds. The lattice constants consumed
(21 bp period, 7 bp class step, +/-5 bp scaffold offset) are PARSED OUT of
`src/main/kotlin/tile/HoneycombBondClassResidues.kt` by `parse_lattice_constants`, not transcribed.

Format, as emitted by cadnano1/2 and as found in the cadnano.org gallery archives:

    {"name": ..., "vstrands": [ {"num": h, "row": r, "col": c,
                                 "scaf": [[ph, pb, nh, nb], ...],
                                 "stap": [[ph, pb, nh, nb], ...],
                                 "loop": [...], "skip": [...], ...}, ... ]}

`scaf[i]` gives the 5' neighbour `(ph, pb)` and the 3' neighbour `(nh, nb)` of base `i` on this
helix. `-1` means none. A **crossing** is an adjacency whose two ends are on different helices.
"""
import json
import os
import re
from collections import Counter, defaultdict

# ---------------------------------------------------------------- lattice constants, parsed

_KOTLIN = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "..", "..", "..", "src", "main", "kotlin", "tile", "HoneycombBondClassResidues.kt",
)


def parse_lattice_constants(path=_KOTLIN):
    """Read the three honeycomb constants out of the Kotlin source that declares them."""
    text = open(path, encoding="utf-8").read()
    out = {}
    for name, key in (
        ("ANY_AZIMUTH_STEP_BP", "classStepBp"),
        ("SAME_PAIR_PERIOD_BP", "samePairPeriodBp"),
        ("SCAFFOLD_OFFSET_BP", "scaffoldOffsetBp"),
        ("CLASSES", "classes"),
    ):
        m = re.search(r"const val %s: Int = (\d+)" % name, text)
        if not m:
            raise ValueError("could not parse %s out of %s" % (name, path))
        out[key] = int(m.group(1))
    return out


# ---------------------------------------------------------------- the design

NONE = -1


class LegacyDesign:
    def __init__(self, doc, label=None):
        self.label = label
        self.name = doc.get("name")
        self.vstrands = doc["vstrands"]
        self.byNum = {v["num"]: v for v in self.vstrands}
        self.pos = {v["num"]: (v["row"], v["col"]) for v in self.vstrands}
        self.length = len(self.vstrands[0]["scaf"]) if self.vstrands else 0

    # -- occupancy -------------------------------------------------------
    @staticmethod
    def _occupied(entry):
        return entry != [NONE, NONE, NONE, NONE]

    def occupied_range(self, num, role):
        """Inclusive [first, last] base index occupied by `role` on helix `num`, or None."""
        arr = self.byNum[num][role]
        idx = [i for i, e in enumerate(arr) if self._occupied(e)]
        return (idx[0], idx[-1]) if idx else None

    def occupied_count(self, num, role):
        return sum(1 for e in self.byNum[num][role] if self._occupied(e))

    # -- crossings -------------------------------------------------------
    def crossings(self, role):
        """Every inter-helix adjacency of `role`, deduplicated, as a sorted list of
        ((helixA, baseA), (helixB, baseB)) with the pair itself sorted."""
        seen = set()
        for v in self.vstrands:
            h = v["num"]
            for i, (ph, pb, nh, nb) in enumerate(v[role]):
                for oh, ob in ((ph, pb), (nh, nb)):
                    if oh == NONE or oh == h:
                        continue
                    edge = tuple(sorted(((h, i), (oh, ob))))
                    seen.add(edge)
        return sorted(seen)

    # -- geometry --------------------------------------------------------
    def displacements(self, role="stap"):
        """Counter over (drow, dcol) between crossing-connected helices."""
        c = Counter()
        for (ha, _), (hb, _) in self.crossings(role):
            ra, ca = self.pos[ha]
            rb, cb = self.pos[hb]
            c[(abs(rb - ra), abs(cb - ca))] += 1
        return c

    def lattice(self):
        """`honeycomb`, `square`, or None, decided by the design's own NEAREST-NEIGHBOUR crossings.

        Both lattices bond `(0, +-1)` and `(+-1, 0)` in caDNAno's `(row, col)` indexing. What
        separates them is a **parity**: on the honeycomb a vertical bond exists at only ONE parity
        of `(row + col)` at its upper site, because a honeycomb site has three neighbours and the
        third points the other way on the other sublattice. A square site has four, so both
        parities appear. Which parity is which is a datum and is NOT asserted.

        Crossings between NON-adjacent sites are excluded from this reading rather than allowed to
        void it -- they are Test A's own finding and are counted separately by
        `forced_census.adjacency_census`. A design is not lattice-less because it forces a
        crossover; it is a lattice design carrying a forced crossover."""
        parities = set()
        nearest = 0
        for role in ("stap", "scaf"):
            for (ha, _), (hb, _) in self.crossings(role):
                ra, ca = self.pos[ha]
                rb, cb = self.pos[hb]
                d = (abs(rb - ra), abs(cb - ca))
                if d not in ((0, 1), (1, 0)):
                    continue
                nearest += 1
                if d == (1, 0):
                    lo = (ra, ca) if ra < rb else (rb, cb)
                    parities.add((lo[0] + lo[1]) % 2)
        if not nearest or not parities:
            return None  # nothing to read, or a single-row design that cannot be told apart
        if len(parities) == 1:
            return "honeycomb"
        return "mixed-parity"  # decided by `forced_census.lattice_by_period`, not here

    # -- crossovers ------------------------------------------------------
    def crossovers(self, role):
        """caDNAno renders one antiparallel crossover as TWO strand crossings at consecutive
        bases `(o, o+1)` (`CLAUDE.md` records the same doubling in the field's own generator).
        Returns a list of `(helixA, helixB, level, kind)` where `level` is the lower base `o`
        and `kind` is `double` or `single`.

        A **single** crossing occurs where the partner base is off the end of the occupied
        range -- a raster turn. Its level is then `i` if `i + 1` is unoccupied on that helix and
        `i - 1` if `i - 1` is, so that the level is the `o` of the pair the lattice would have
        offered."""
        by = defaultdict(list)
        for (ha, ia), (hb, ib) in self.crossings(role):
            by[(ha, hb)].append((ia, ib))
        out = []
        for (ha, hb), idx in by.items():
            idx = sorted(idx)
            i = 0
            while i < len(idx):
                ia, ib = idx[i]
                if i + 1 < len(idx) and idx[i + 1][0] == ia + 1:
                    out.append((ha, hb, ia, "double"))
                    i += 2
                else:
                    arr = self.byNum[ha][role]
                    up = ia + 1 < len(arr) and self._occupied(arr[ia + 1])
                    dn = ia - 1 >= 0 and self._occupied(arr[ia - 1])
                    if up and not dn:
                        level = ia - 1
                    elif dn and not up:
                        level = ia
                    else:
                        level = ia
                    out.append((ha, hb, level, "single"))
                    i += 1
        return sorted(out)

    # -- residue census --------------------------------------------------
    def bond_residues(self, role, period):
        """{(helixA, helixB): Counter(residue mod period)} over that bond's crossings."""
        out = defaultdict(Counter)
        for (ha, ia), (hb, ib) in self.crossings(role):
            # a crossing is between the SAME base index on both helices in every caDNAno design;
            # assert it rather than assume it
            out[(ha, hb)][ia % period] += 1
            if ia != ib:
                out[(ha, hb)]["MISALIGNED:%d/%d" % (ia, ib)] += 1
        return out


def load(path, label=None):
    with open(path, encoding="utf-8") as f:
        return LegacyDesign(json.load(f), label=label or os.path.basename(path))


def load_archives(directory=None):
    """Every design in the three retained gallery `.zip` archives, read WITHOUT unpacking them.

    The archives are the primary artifacts and are what the gallery links; an unpacked copy would
    be 3.4x their size and a second thing to keep honest."""
    import zipfile
    directory = directory or os.path.dirname(os.path.abspath(__file__))
    out = []
    for archive in ("Science09.zip", "NAR09.zip", "Nature09.zip"):
        path = os.path.join(directory, archive)
        with zipfile.ZipFile(path) as zf:
            for name in sorted(zf.namelist()):
                if name.startswith("__MACOSX") or not name.endswith(".json"):
                    continue
                doc = json.loads(zf.read(name).decode("utf-8"))
                design = LegacyDesign(doc, label=os.path.basename(name)[:-5])
                design.archive = archive[:-4]
                design.member = name
                out.append(design)
    return out
