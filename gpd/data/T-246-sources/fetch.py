#!/usr/bin/env python3
"""T-246 full-text fetcher: tries fullTextXML then europepmc pdf=render."""
import sys, os, re, json, urllib.request, subprocess, time
OUT = os.path.dirname(os.path.abspath(__file__))
UA = {"User-Agent": "Mozilla/5.0 (X11; Linux x86_64) curl/8 T-246-research"}

def get(url, timeout=120):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return r.read(), r.status

def fetch(pmcid):
    rec = {"pmcid": pmcid, "routes": []}
    xurl = "https://www.ebi.ac.uk/europepmc/webservices/rest/%s/fullTextXML" % pmcid
    try:
        b, st = get(xurl)
        rec["routes"].append({"url": xurl, "status": st, "bytes": len(b)})
        if len(b) > 5000:
            p = os.path.join(OUT, "%s-fullTextXML.xml" % pmcid)
            open(p, "wb").write(b)
            txt = re.sub(r'<[^>]+>', ' ', b.decode('utf-8', 'replace'))
            txt = re.sub(r'[ \t]+', ' ', txt)
            open(os.path.join(OUT, "%s.txt" % pmcid), "w").write(txt)
            rec["saved"] = os.path.basename(p); rec["route"] = "fullTextXML"
            return rec
    except Exception as e:
        rec["routes"].append({"url": xurl, "error": str(e)})
    purl = "https://europepmc.org/articles/%s?pdf=render" % pmcid
    try:
        b, st = get(purl)
        rec["routes"].append({"url": purl, "status": st, "bytes": len(b)})
        if len(b) > 20000 and b[:4] == b'%PDF':
            p = os.path.join(OUT, "%s.pdf" % pmcid)
            open(p, "wb").write(b)
            subprocess.run(["pdftotext", "-layout", p, os.path.join(OUT, "%s.txt" % pmcid)],
                           check=False)
            rec["saved"] = os.path.basename(p); rec["route"] = "pdf=render"
            return rec
    except Exception as e:
        rec["routes"].append({"url": purl, "error": str(e)})
    rec["route"] = "FAILED"
    return rec

if __name__ == "__main__":
    log = []
    for i, pid in enumerate(sys.argv[1:]):
        if i: time.sleep(3)
        r = fetch(pid)
        print(pid, r["route"], r.get("saved", ""))
        log.append(r)
    lp = os.path.join(OUT, "fetches.json")
    old = json.load(open(lp)) if os.path.exists(lp) else []
    json.dump(old + log, open(lp, "w"), indent=1)
