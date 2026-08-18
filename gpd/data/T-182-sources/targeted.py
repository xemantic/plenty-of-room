"""T-182 — targeted title queries for the four papers the keyword sweep did not surface,
plus the fetches. Retained driver."""
import json, time, urllib.parse, urllib.request, os
BASE = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"
HERE = os.path.dirname(os.path.abspath(__file__))
Q = [
 'TITLE:"Coarse-grained modelling of the structural properties of DNA origami"',
 'TITLE:"In situ structure and dynamics of DNA origami determined through molecular dynamics simulations"',
 'TITLE:"Introducing improved structural properties and salt dependence into a coarse-grained model of DNA"',
 'TITLE:"Cryo-EM structure of a 3D DNA-origami object"',
 'TITLE:"Folding DNA into twisted and curved nanoscale shapes"',
 'TITLE:"Multilayer DNA origami packed on a square lattice"',
 '"DNA origami" AND "global twist" AND "per turn" AND degrees',
 'Snodin AND origami AND oxDNA',
 'Aksimentiev AND "DNA origami" AND twist',
 'Bathe AND "DNA origami" AND "finite element" AND twist',
]
out = {}
for q in Q:
    url = BASE + "?" + urllib.parse.urlencode({"query": q, "format": "json", "pageSize": 10, "resultType": "core"})
    data = {}
    for _ in range(4):
        try:
            with urllib.request.urlopen(url, timeout=90) as r:
                data = json.loads(r.read().decode()); break
        except Exception as e:
            data = {"error": str(e)}; time.sleep(8)
    hits = data.get("resultList", {}).get("result", [])
    out[q] = [{"id": h.get("id"), "title": h.get("title"), "year": h.get("pubYear"),
               "journal": (h.get("journalInfo") or {}).get("journal", {}).get("title"),
               "oa": h.get("isOpenAccess"), "pmcid": h.get("pmcid"), "doi": h.get("doi"),
               "abstract": (h.get("abstractText") or "")[:3000]} for h in hits]
    print("%-72s %d" % (q[:72], len(hits)), flush=True)
    for h in out[q][:3]:
        print("     ", h["year"], h.get("pmcid"), (h["title"] or "")[:80])
    time.sleep(8)
json.dump(out, open(os.path.join(HERE, "targeted-queries.json"), "w"), indent=1)
