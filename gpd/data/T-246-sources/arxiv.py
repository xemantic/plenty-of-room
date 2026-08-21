#!/usr/bin/env python3
import sys, os, re, json, time, urllib.parse, urllib.request
OUT = os.path.dirname(os.path.abspath(__file__))
def slug(q): return re.sub(r'[^A-Za-z0-9]+','_',q).strip('_')[:70]
def run(q, n=25):
    url = "https://export.arxiv.org/api/query?" + urllib.parse.urlencode(
        {"search_query": q, "max_results": n, "sortBy":"relevance"})
    req = urllib.request.Request(url, headers={"User-Agent":"curl/8 T-246"})
    with urllib.request.urlopen(req, timeout=90) as r: return r.read().decode('utf-8','replace')
log=[]
for i,q in enumerate(json.load(open(sys.argv[1]))):
    if i: time.sleep(4)
    x = run(q)
    open(os.path.join(OUT,"arxiv-%s.xml"%slug(q)),"w").write(x)
    ids = re.findall(r'<id>http://arxiv.org/abs/([^<]+)</id>', x)
    tis = [re.sub(r'\s+',' ',t).strip() for t in re.findall(r'<entry>.*?<title>(.*?)</title>', x, re.S)]
    tot = re.search(r'opensearch:totalResults[^>]*>(\d+)<', x)
    print("TOTAL %-5s %s" % (tot.group(1) if tot else '?', q))
    for a,b in zip(ids,tis): print("   %-14s %s" % (a, b[:110]))
    log.append({"query":q,"endpoint":"https://export.arxiv.org/api/query",
                "totalResults": int(tot.group(1)) if tot else None,
                "file":"arxiv-%s.xml"%slug(q),
                "entries":[{"id":a,"title":b} for a,b in zip(ids,tis)]})
json.dump(log, open(os.path.join(OUT, sys.argv[2]),"w"), indent=1)
