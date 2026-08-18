#!/usr/bin/env python3
"""T-191 -- where a measured helix bundle sits between the NONE and RIGID limits.

DERIVED HERE, not published anywhere: the published quantity is a bending
persistence length; the composite fraction below is this repository's arithmetic
on it.  EI = l_b * kT.  Two limits:

  NONE  : EI = n * EI_duplex
  RIGID : EI = n * EI_duplex + S * sum(y_i^2)      (parallel-axis theorem)

fraction f = (EI_meas - EI_none) / (EI_rigid - EI_none)
"""
kT = 4.1419464  # pN nm at 300 K
S = 1100.0      # pN     -- CanDo (Kim et al., NAR 40:2862, read directly)
EId = 230.0     # pN nm2 -- CanDo


def ring(n, R):
    """sum y_i^2 for n helices on a circle of radius R (isotropic: n R^2 / 2)."""
    return n * R * R / 2.0


def square_grid(nx, ny, d):
    """sum y_i^2 for an nx-by-ny square array of pitch d, about a centroidal axis
    parallel to a row (worst/best identical for a square nx==ny)."""
    ys = [(i - (ny - 1) / 2.0) * d for i in range(ny)]
    return nx * sum(y * y for y in ys)


def report(label, n, sumy2, lb_nm, EId=EId, S=S):
    none = n * EId
    rigid = none + S * sumy2
    meas = lb_nm * kT
    f = (meas - none) / (rigid - none)
    print(f"{label:34s} n={n:3d}  sum y^2={sumy2:7.2f} nm^2   "
          f"EI none={none:8.0f}  rigid={rigid:9.0f}  meas={meas:8.0f} pN nm^2   "
          f"lb rigid={rigid/kT:7.0f} nm   fraction={f:.3f}   rigid/meas={rigid/meas:.2f}x")


print("=== MEASURED ===")
# Kauert 2011 (via Chhabra Table 1 l_b^expt, and PMC9494703's 15x/38x)
report("Kauert 6HB honeycomb d=2.536", 6, ring(6, 2.536), 1880)
report("Kauert 6HB honeycomb d=2.69",  6, ring(6, 2.690), 1880)
report("Kauert 4HB square    d=2.73",  4, square_grid(2, 2, 2.73), 740)
report("Kauert 4HB square    d=2.60",  4, square_grid(2, 2, 2.60), 740)
# Pfitzner 2013 6HB
report("Pfitzner 6HB honeycomb d=2.536", 6, ring(6, 2.536), 2000)
# Wang 2012 tile 6HB, in that paper's own convention (r=1 nm, R=2 nm, p_helix 50 nm)
report("Wang 2012 6HB tile   R=2.00",  6, ring(6, 2.00), 1000, EId=50*kT, S=4*50*kT/1.0**2)

print()
print("=== SIMULATED (for cross-check only) ===")
report("SNUPI 16HB square    d=2.60", 16, square_grid(4, 4, 2.60), 13063)
report("SNUPI 16HB square    d=2.73", 16, square_grid(4, 4, 2.73), 13063)
report("Chhabra oxDNA 6HB-MT d=2.536", 6, ring(6, 2.536), 4140)

print()
print("=== THE FOUR-LAYER TILE THE QUESTION IS ABOUT ===")
# 15 duplexes wide x 4 layers, honeycomb-ish stacking, layer pitch = 2.536*sqrt(3)/2
import math
for pitch, name in ((2.536, "layer pitch = d = 2.536"),
                    (2.536 * math.sqrt(3) / 2, "layer pitch = d*sqrt3/2 = 2.196")):
    ys = [(i - 1.5) * pitch for i in range(4)]
    sumy2 = 15 * sum(y * y for y in ys)
    none = 60 * EId
    rigid = none + S * sumy2
    print(f"  {name:34s} sum y^2={sumy2:8.2f}  EI none={none:8.0f}  rigid={rigid:10.0f}"
          f"  ratio={rigid/none:6.2f}x   at f=0.30 -> EI={none+0.30*(rigid-none):9.0f}"
          f" ({ (none+0.30*(rigid-none))/none:.2f}x NONE)")
