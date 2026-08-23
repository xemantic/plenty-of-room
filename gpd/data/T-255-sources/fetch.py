#!/usr/bin/env python3
"""T-255 retrieval driver. Records every URL with its HTTP status. Retained per SESSION-PROMPT."""
import json, os, sys, time, urllib.request, urllib.error

UA = "Mozilla/5.0 (X11; Linux x86_64) plenty-of-room/T-255 (research; claude@xemantic.com)"
LOG = []

def get(url, out=None, follow=True, timeout=60):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    rec = {"url": url, "status": None, "finalUrl": None, "bytes": 0, "contentType": None,
           "savedAs": out, "error": None}
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            data = r.read()
            rec["status"] = r.status
            rec["finalUrl"] = r.geturl()
            rec["bytes"] = len(data)
            rec["contentType"] = r.headers.get("Content-Type")
            if out:
                with open(out, "wb") as f:
                    f.write(data)
    except urllib.error.HTTPError as e:
        rec["status"] = e.code
        rec["error"] = str(e)
        rec["finalUrl"] = e.geturl() if hasattr(e, "geturl") else None
    except Exception as e:
        rec["error"] = repr(e)
    LOG.append(rec)
    print(json.dumps(rec), flush=True)
    return rec

if __name__ == "__main__":
    urls = json.load(open(sys.argv[1]))
    for u in urls:
        get(u["url"], u.get("out"))
        time.sleep(2)
    logname = sys.argv[2] if len(sys.argv) > 2 else "fetches.json"
    prev = json.load(open(logname)) if os.path.exists(logname) else []
    json.dump(prev + LOG, open(logname, "w"), indent=1)
