"""T-182 — retained fetcher. Records the URL, the HTTP status and the byte count for every
attempt, so a NOT FOUND is falsifiable. CLAUDE.md: europepmc.org/articles/PMC<id>?pdf=render is
the reliable route; fullTextXML returns zero bytes for several readable articles."""
import json, os, time, urllib.request, urllib.error
HERE = os.path.dirname(os.path.abspath(__file__))
UA = {"User-Agent": "Mozilla/5.0 (X11; Linux x86_64) research-fetch/1.0"}
TARGETS = [
    ("PMC6379721-snodin2019.pdf", "https://europepmc.org/articles/PMC6379721?pdf=render"),
    ("PMC3864285-yoo-aksimentiev2013.pdf", "https://europepmc.org/articles/PMC3864285?pdf=render"),
    ("PMC2737683-dietz2009.pdf", "https://europepmc.org/articles/PMC2737683?pdf=render"),
    ("PMC9127610-floppy2d-cryoem.pdf", "https://europepmc.org/articles/PMC9127610?pdf=render"),
    ("PMC3523823-bai2012-cryoem.pdf", "https://europepmc.org/articles/PMC3523823?pdf=render"),
    ("PMC9127610-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC9127610/fullTextXML"),
    ("PMC3864285-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC3864285/fullTextXML"),
    ("PMC2737683-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2737683/fullTextXML"),
    ("baker2018-acsnano.html", "https://pubs.acs.org/doi/10.1021/acsnano.8b01669"),
    ("li2012-langmuir.html", "https://pubs.acs.org/doi/10.1021/la204446c"),
    ("crossref-baker2018.json", "https://api.crossref.org/works/10.1021/acsnano.8b01669"),
    ("crossref-li2012.json", "https://api.crossref.org/works/10.1021/la204446c"),
]
log = []
for name, url in TARGETS:
    rec = {"file": name, "url": url}
    try:
        req = urllib.request.Request(url, headers=UA)
        with urllib.request.urlopen(req, timeout=120) as r:
            body = r.read()
            rec["status"] = r.status
            rec["bytes"] = len(body)
            if len(body) > 0:
                open(os.path.join(HERE, name), "wb").write(body)
    except urllib.error.HTTPError as e:
        rec["status"] = e.code
        rec["bytes"] = 0
        rec["error"] = str(e)
    except Exception as e:
        rec["status"] = None
        rec["bytes"] = 0
        rec["error"] = str(e)
    print("%-40s %s %s" % (name, rec.get("status"), rec.get("bytes")), flush=True)
    log.append(rec)
    time.sleep(4)
json.dump(log, open(os.path.join(HERE, "fetches.json"), "w"), indent=1)
