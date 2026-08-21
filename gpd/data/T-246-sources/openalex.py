#!/usr/bin/env python3
import sys, os, re, json, time, urllib.parse, urllib.request
OUT = os.path.dirname(os.path.abspath(__file__))
def slug(q): return re.sub(r'[^A-Za-z0-9]+','_',q).strip('_')[:70]
log=[]
for i,q in enumerate(json.load(open(sys.argv[1]))):
    if i: time.sleep(3)
    url="https://api.openalex.org/works?"+urllib.parse.urlencode(
        {"search":q,"per-page":25,"mailto":"morisil@xemantic.com"})
    req=urllib.request.Request(url, headers={"User-Agent":"T-246-research (morisil@xemantic.com)"})
    try:
        raw=urllib.request.urlopen(req,timeout=90).read().decode('utf-8','replace')
    except Exception as e:
        print("ERR", q, e); continue
    open(os.path.join(OUT,"openalex-%s.json"%slug(q)),"w").write(raw)
    d=json.loads(raw)
    n=d.get("meta",{}).get("count")
    print("COUNT %-6s %s" % (n, q))
    ent=[]
    for r in d.get("results",[])[:25]:
        t=(r.get("title") or "")[:105]; y=r.get("publication_year")
        doi=(r.get("doi") or "").replace("https://doi.org/","")
        print("   %-6s %-40s %s" % (y, doi[:40], t))
        ent.append({"title":r.get("title"),"year":y,"doi":doi,"id":r.get("id")})
    log.append({"query":q,"endpoint":"https://api.openalex.org/works?search=",
                "count":n,"file":"openalex-%s.json"%slug(q),"top":ent})
json.dump(log, open(os.path.join(OUT, sys.argv[2]),"w"), indent=1)
