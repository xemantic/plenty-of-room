#!/usr/bin/env python3
"""T-310 -- is there a PUBLISHED number for a DNA-origami crossover's stiffness against a change
of the INTERHELICAL SEPARATION -- the RADIAL coordinate, along the line of centres?

This is NOT `T-303`'s question. `T-303` searched for a crossover's stiffness against a relative
NORMAL displacement and found none; the coordinate this task needs is the one `T-303` explicitly
set aside as *"the line of centres, which is the coordinate the link is perpendicular to"*, and
which Snodin et al. do measure the mean and the standard deviation of. So the question is asked
again, on the other eigenvalue of the same tensor, with query strings that name the separation
rather than the shear.

CLAUDE.md's research practice: `gpd/` is checked first, every query string is recorded, and every
number is flagged read directly / abstract only / not found.
"""
import json, time, urllib.parse, urllib.request, pathlib

OUT = pathlib.Path(__file__).parent
BASE = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"

QUERIES = [
    '"DNA origami" AND "interhelical distance" AND (fluctuation OR variance OR "standard deviation")',
    '"DNA origami" AND crossover AND ("interhelical" OR "inter-helix") AND stiffness',
    'oxDNA "DNA origami" inter-helix distance distribution junction',
    'all-atom molecular dynamics DNA origami interhelical spacing free energy',
    '"DNA nanostructure" AND "helix-helix" AND (compressibility OR "elastic modulus")',
    'DNA origami lattice constant fluctuation SAXS Debye-Waller',
    '"four-way junction" AND ("stacking-unstacking" OR "arm separation") AND free energy landscape',
    'coarse-grained model DNA origami crossover spring constant interhelical separation',
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
