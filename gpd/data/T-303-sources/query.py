#!/usr/bin/env python3
"""T-303 -- is there a PUBLISHED number for a DNA-origami crossover's stiffness against a
RELATIVE NORMAL DISPLACEMENT of the two duplexes it joins (all-atom, oxDNA or experiment)?

CLAUDE.md's research practice: gpd/ is checked first (it has paid three times), every query
string is recorded, and every number is flagged read directly / abstract only / not found.
"""
import json, time, urllib.parse, urllib.request, pathlib

OUT = pathlib.Path(__file__).parent
BASE = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"

QUERIES = [
    'DNA origami crossover elastic constant',
    'Holliday junction stiffness molecular dynamics origami crossover',
    'oxDNA crossover junction elasticity DNA origami',
    '"DNA origami" AND "crossover" AND "spring constant"',
    '"DNA origami" AND junction AND ("shear stiffness" OR "transverse stiffness")',
    'all-atom molecular dynamics DNA origami crossover flexibility',
    '"antiparallel crossover" DNA elasticity coarse-grained',
    'DNA origami interhelical fluctuation junction stiffness',
]

results = {}
for q in QUERIES:
    url = BASE + "?" + urllib.parse.urlencode(
        {"query": q, "format": "json", "pageSize": 25, "resultType": "core"}
    )
    try:
        with urllib.request.urlopen(url, timeout=60) as r:
            body = json.load(r)
        hits = [
            {
                "id": h.get("id"),
                "pmcid": h.get("pmcid"),
                "doi": h.get("doi"),
                "title": h.get("title"),
                "journal": (h.get("journalInfo") or {}).get("journal", {}).get("title"),
                "year": h.get("pubYear"),
                "isOpenAccess": h.get("isOpenAccess"),
            }
            for h in body.get("resultList", {}).get("result", [])
        ]
        results[q] = {"hitCount": body.get("hitCount"), "top": hits}
    except Exception as exc:                                    # noqa: BLE001
        results[q] = {"error": repr(exc)}
    time.sleep(8)

(OUT / "europepmc-queries.json").write_text(json.dumps(results, indent=1) + "\n")
print("wrote", OUT / "europepmc-queries.json")
for q, r in results.items():
    print(f"{r.get('hitCount', 'ERR')!s:>8}  {q}")
