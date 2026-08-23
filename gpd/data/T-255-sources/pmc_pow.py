#!/usr/bin/env python3
"""PMC's SHA-256 proof-of-work gate (`cloudpmc-viewer-pow`), per `CLAUDE.md`."""
import hashlib, json, re, sys, urllib.request
UA = "Mozilla/5.0 (X11; Linux x86_64) plenty-of-room/T-255 (research; claude@xemantic.com)"

def solve(challenge, difficulty=4):
    p = "0" * difficulty
    n = 0
    while True:
        if hashlib.sha256(("%s%d" % (challenge, n)).encode()).hexdigest().startswith(p):
            return n
        n += 1

def fetch(url, out):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            body = r.read()
            status = r.status
    except urllib.error.HTTPError as e:
        body, status = e.read(), e.code
    txt = body.decode("utf-8", "replace")
    m = re.search(r'POW_CHALLENGE\s*=\s*["\']([^"\']+)["\']', txt)
    d = re.search(r'POW_DIFFICULTY\s*=\s*(\d+)', txt)
    if not m:
        open(out, "wb").write(body)
        return {"url": url, "status": status, "bytes": len(body), "pow": False, "savedAs": out}
    nonce = solve(m.group(1), int(d.group(1)) if d else 4)
    req = urllib.request.Request(url, headers={
        "User-Agent": UA,
        "Cookie": 'cloudpmc-viewer-pow="%s,%d"' % (m.group(1), nonce)})
    try:
        with urllib.request.urlopen(req, timeout=90) as r:
            body = r.read(); status = r.status
    except urllib.error.HTTPError as e:
        body, status = e.read(), e.code
    open(out, "wb").write(body)
    return {"url": url, "status": status, "bytes": len(body), "pow": True,
            "challenge": m.group(1), "nonce": nonce, "savedAs": out}

if __name__ == "__main__":
    print(json.dumps(fetch(sys.argv[1], sys.argv[2])))
