"""F7: does the re-emitted exceedance agree with the one the cheap bound JOINED?"""
import json, sys
B="/tmp/plenty-of-room-W337b/gpd/results/"
FILES={"T-297":"T-297-the-common-mode-is-the-link.json","T-303":"T-303-what-link-stiffness-the-recovery-needs.json"}
# from gpd/data/T-337-cheap-bound/output.txt, section 2c
EXPECT=[("T-297","cells",21,0.0995),("T-303","ladder",5,0.0980),("T-303","ladder",11,0.0995),
        ("T-303","cells",177,0.0980),("T-303","cells",277,0.0995)]
bad=0
for tag,block,idx,joined in EXPECT:
    d=json.load(open(B+FILES[tag]))
    rec=d[block][idx]
    got=rec["exceedance"]
    ok = abs(got-joined) < 1e-12
    print("%-6s /%s/%-4d joined=%.6f  re-emitted=%.6f  %s  (x=%d)" %
          (tag,block,idx,joined,got,"AGREE" if ok else "DISAGREE", round(got*4000)))
    bad += 0 if ok else 1
print("F7 disagreements:", bad)
sys.exit(1 if bad else 0)
