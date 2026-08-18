import json, time, urllib.parse, urllib.request, sys, os
BASE = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"
queries = [
 'DNA origami AND "twist strain" AND crossover',
 '"DNA origami" AND "residual stress"',
 '"DNA origami" AND prestrain',
 '"DNA nanostructure" AND "crossovers in tension"',
 '"DNA origami" AND "strain energy" AND crossover',
 '"DNA origami" AND "global twist" AND "base pairs per turn"',
 '"DNA origami" AND edge AND strain AND seam',
 'oxDNA AND "DNA origami" AND strain AND crossover',
 '"DNA origami" AND "internal stress"',
 '"molecular dynamics" AND "DNA origami" AND "twist" AND "crossover" AND "angle"',
]
out = {}
for q in queries:
    url = BASE + "?" + urllib.parse.urlencode({"query": q, "format": "json", "pageSize": 15, "resultType": "core"})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(url, timeout=60) as r:
                data = json.loads(r.read().decode())
            break
        except Exception as e:
            data = {"error": str(e)}
            time.sleep(8)
    hits = data.get("resultList", {}).get("result", [])
    out[q] = [{"id": h.get("id"), "title": h.get("title"), "year": h.get("pubYear"),
               "oa": h.get("isOpenAccess"), "pmcid": h.get("pmcid"),
               "abstract": (h.get("abstractText") or "")[:1200]} for h in hits]
    print("%-70s %d hits" % (q[:70], len(hits)), flush=True)
    time.sleep(8)
json.dump(out, open(os.path.join(os.path.dirname(__file__), "europepmc-queries.json"), "w"), indent=1)
