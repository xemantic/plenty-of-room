#!/usr/bin/env python3
"""T-255 -- run the three tests over every design in the cadnano.org gallery archives."""
import json, os
import cadnano_legacy as C, forced_census as F

def main():
    rows = []
    for d in C.load_archives():
        r = F.census(d)
        r["design"] = d.label
        r["archive"] = d.archive
        r["path"] = d.archive + ".zip!" + d.member
        r["insertions"] = sum(sum(1 for x in v["loop"] if x) for v in d.vstrands)
        r["deletions"] = sum(sum(1 for x in v["skip"] if x) for v in d.vstrands)
        r["scaffoldBases"] = sum(d.occupied_count(v["num"], "scaf") for v in d.vstrands)
        r["stapleBases"] = sum(d.occupied_count(v["num"], "stap") for v in d.vstrands)
        none = [-1, -1, -1, -1]
        unpaired = 0
        for v in d.vstrands:
            stap = set(i for i, e in enumerate(v["stap"]) if e != none)
            unpaired += sum(1 for i, e in enumerate(v["scaf"])
                            if e != none and i not in stap)
        r["unpairedScaffoldBases"] = unpaired
        rows.append(r)
    rows.sort(key=lambda r: (["NAR09", "Science09", "Nature09"].index(r["archive"]), r["design"]))
    json.dump(rows, open(os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                      "census-raw.json"), "w"), indent=1)
    return rows


if __name__ == "__main__":
    from collections import Counter
    rows = main()
    h = "%-22s %-9s %4s %-10s %8s %6s %6s %5s %5s %5s %5s %5s %5s"
    print(h % ("design", "archive", "hel", "lattice", "ins/del", "stapX", "scafX",
               "MIS", "ADJ", "offB", "fStp", "fScf", "unsc"))
    for r in rows:
        print(h % (r["design"], r["archive"], r["helices"], str(r["lattice"]),
                   "%d/%d" % (r["insertions"], r["deletions"]),
                   r["stapleCrossings"], r["scaffoldCrossings"],
                   r["nMisalignedConnections"], r["nForcedByAdjacency"],
                   r["nOffRegisterStapleBonds"], r["nForcedStapleCrossings"],
                   r["nForcedScaffoldCrossings"], r["nUnscorableScaffold"]))
    n = sum(1 for r in rows if r["carriesForcedCrossover"])
    print("\ndesigns carrying a forced crossover: %d of %d" % (n, len(rows)))
    st, sc, mi = Counter(), Counter(), Counter()
    for r in rows:
        for b in r["forcedStaple"]:
            for dd in b["departuresBp"]:
                st[dd] += 1
        for x in r["forcedScaffold"]:
            sc[x["departureBp"]] += 1
        for x in r["misalignedConnections"]:
            mi[abs(x["axialOffsetBp"])] += 1
    print("forced staple departures  :", dict(sorted(st.items())))
    print("forced scaffold departures:", dict(sorted(sc.items())))
    print("misaligned |axial offset| :", dict(sorted(mi.items())))
