#!/usr/bin/env python3
"""T-246 EuropePMC query runner. Sleeps 8s between queries, retries on failure."""
import json, sys, time, re, os, urllib.parse, urllib.request

OUT = os.path.dirname(os.path.abspath(__file__))
BASE = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"

def slug(q):
    s = re.sub(r'[^A-Za-z0-9]+', '_', q)
    return s.strip('_')[:80]

def run(q, pagesize=25):
    url = BASE + "?" + urllib.parse.urlencode(
        {"query": q, "format": "json", "pageSize": pagesize, "resultType": "core"})
    last = None
    for attempt in range(5):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "curl/8.0 T-246-research"})
            with urllib.request.urlopen(req, timeout=90) as r:
                raw = r.read().decode("utf-8", errors="replace")
            data = json.loads(raw)   # HTML error page will raise here
            return data, raw
        except Exception as e:
            last = e
            sys.stderr.write("  retry %d for %r: %s\n" % (attempt+1, q, e))
            time.sleep(15)
    raise RuntimeError("failed after retries: %s (%s)" % (q, last))

def main(queries):
    log = []
    for i, q in enumerate(queries):
        if i: time.sleep(8)
        sys.stderr.write("[%d/%d] %s\n" % (i+1, len(queries), q))
        data, raw = run(q)
        fn = os.path.join(OUT, "search-%s.json" % slug(q))
        open(fn, "w").write(raw)
        hits = data.get("hitCount", -1)
        results = data.get("resultList", {}).get("result", [])
        entry = {"query": q, "endpoint": BASE, "hitCount": hits, "file": os.path.basename(fn),
                 "top": [{"title": r.get("title"), "year": r.get("pubYear"),
                          "pmcid": r.get("pmcid"), "doi": r.get("doi"),
                          "journal": (r.get("journalInfo") or {}).get("journal", {}).get("title"),
                          "isOpenAccess": r.get("isOpenAccess"),
                          "inEPMC": r.get("inEPMC")} for r in results]}
        log.append(entry)
        print("HITS %-6s %s" % (hits, q))
        for r in results[:25]:
            print("   %-12s %-6s %s" % (r.get("pmcid") or "-", r.get("pubYear") or "-",
                                        (r.get("title") or "")[:110]))
    return log

if __name__ == "__main__":
    qs = json.load(open(sys.argv[1]))
    out = sys.argv[2]
    log = main(qs)
    json.dump(log, open(os.path.join(OUT, out), "w"), indent=1)
