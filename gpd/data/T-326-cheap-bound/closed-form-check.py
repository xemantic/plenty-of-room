#!/usr/bin/env python3
"""T-326 cheap bound, prototype: the fit/sample gap in closed form.

Verifies, against a direct quadrature of both reconstructions of a honeycomb
face field, the three closed forms of `gpd/tasks/T-326-*.md` §2 --- the
right-hand-side gap between `evaluate`'s nearest-beam reconstruction and
`faceFunctional`'s owning-beam one, summed over the face's own vertical bonds.

It also measures the third convention (nearest-beam over the face rectangle)
and the `A : B : C = 0 : 1 : 6` piston collinearity.

Run:  python3 gpd/data/T-326-cheap-bound/closed-form-check.py
This is the PROTOTYPE the task file's section 2 numbers came from; the shipped
form is the Kotlin study, and this is retained so the derivation stays checkable
without a JVM.
"""
import numpy as np

D = 2.536            # nm, SAXS honeycomb bond length
P = 1.5 * D          # nm, in-plane raster-row pitch


def face_ladder(m, face_column=0):
    y = np.array([r * P + (0.5 * D if (r + face_column) % 2 == 0 else 0.0) for r in range(m)])
    return y - (y.min() + y.max()) / 2.0


def vertical_bond_pairs(y):
    gaps = np.diff(y)
    return [(i, i + 1) for i in range(len(y) - 1) if abs(gaps[i] - D) < 1e-9]


def _breaks(y):
    """Midpoints between consecutive axes --- the nearest-beam cell boundaries."""
    return [(y[i] + y[i + 1]) / 2.0 for i in range(len(y) - 1)]


def _owner(y, mid):
    return int(np.argmin(np.abs(y - mid)))


def _segment(mu, wv_r, ph_r, y_r, lo, hi):
    """EXACT integral of mu(y) * (w_r + ph_r*(y - y_r)) over [lo, hi].

    The reconstruction is piecewise linear in y and DISCONTINUOUS at the cell
    boundaries, so a uniform grid is only first-order there; every integral in
    this file is therefore taken in closed form.
    """
    m0 = hi - lo
    m1 = (hi * hi - lo * lo) / 2.0
    m2 = (hi ** 3 - lo ** 3) / 3.0
    if mu == "1":
        return wv_r * m0 + ph_r * (m1 - y_r * m0)
    return wv_r * m1 + ph_r * (m2 - y_r * m1)


def owning(y, wv, ph, mode):
    """Convention A: owning-beam reconstruction over the owning strips."""
    return sum(_segment(mode, wv[r], ph[r], y[r], y[r] - P / 2, y[r] + P / 2)
               for r in range(len(y)))


def _nearest_pieces(y, lo, hi):
    """[(low, high, owning beam)] tiling [lo, hi] under the nearest-beam rule."""
    edges = [lo] + [b for b in _breaks(y) if lo < b < hi] + [hi]
    return [(edges[k], edges[k + 1], _owner(y, (edges[k] + edges[k + 1]) / 2.0))
            for k in range(len(edges) - 1)]


def nearest_over_strips(y, wv, ph, mode):
    """Convention B: nearest-beam reconstruction over the owning strips."""
    total = 0.0
    for r in range(len(y)):
        for lo, hi, o in _nearest_pieces(y, y[r] - P / 2, y[r] + P / 2):
            total += _segment(mode, wv[o], ph[o], y[o], lo, hi)
    return total


def nearest_over_face(y, wv, ph, mode, m):
    """Convention C: nearest-beam reconstruction over the face rectangle."""
    ly = m * P
    return sum(_segment(mode, wv[o], ph[o], y[o], lo, hi)
               for lo, hi, o in _nearest_pieces(y, -ly / 2, ly / 2))


def predicted_piston(y, wv, ph):
    return (D * D / 16.0) * sum(ph[b] - ph[a] for a, b in vertical_bond_pairs(y))


def predicted_tilt_y(y, wv, ph):
    out = 0.0
    for a, b in vertical_bond_pairs(y):
        mid = 0.5 * (y[a] + y[b])
        out += (D * D / 16.0) * ((wv[b] - wv[a]) + mid * (ph[b] - ph[a]))
        out -= (D ** 3 / 32.0) * (ph[a] + ph[b])
    return out


_GAUSS6_NODES, _GAUSS6_WEIGHTS = np.polynomial.legendre.leggauss(6)


def gauss6_over_strips(y, wv, ph, mode):
    """Convention B AS THE CLASS COMPUTES IT --- `HoneycombGrillage.QUADRATURE_POINTS`
    is 6 and `integrateOverFace` does NOT split the strip at the nearest-beam
    boundary, so a smooth rule is applied to a discontinuous integrand."""
    total = 0.0
    for r in range(len(y)):
        lo, hi = y[r] - P / 2, y[r] + P / 2
        pts = (lo + hi) / 2 + (hi - lo) / 2 * _GAUSS6_NODES
        wts = _GAUSS6_WEIGHTS * (hi - lo) / 2
        i = np.argmin(np.abs(pts[:, None] - y[None, :]), axis=1)
        f = wv[i] + ph[i] * (pts - y[i])
        total += np.sum(wts * (np.ones_like(pts) if mode == "1" else pts) * f)
    return total


def main():
    rng = np.random.default_rng(20260825)
    worst = 0.0
    print("closed form against the direct quadrature difference B - A")
    for m in (3, 4, 5, 6, 10, 11, 14, 15, 16):
        for col in (0, 1):
            y = face_ladder(m, col)
            for _ in range(3):
                wv, ph = rng.normal(size=m), rng.normal(size=m)
                # The two sides can both be near zero on a cancelling draw, so the
                # departure is taken ABSOLUTELY against the one scale in the problem
                # (CLAUDE.md: comparing two quantities meant to be zero relatively
                # compares their noise).
                scale = P * abs(wv).max() + P * P * abs(ph).max()
                for mode, pred in (("1", predicted_piston(y, wv, ph)),
                                   ("y", predicted_tilt_y(y, wv, ph))):
                    got = nearest_over_strips(y, wv, ph, mode) - owning(y, wv, ph, mode)
                    worst = max(worst, abs(pred - got) / scale)
            print("  m=%2d col=%d bonds=%d  worst so far %.3e" % (m, col, len(vertical_bond_pairs(y)), worst))
    print("worst scaled departure over all readings: %.3e" % worst)

    print("\nlimiting cases (a pure piston and a pure y must give exactly zero)")
    for m in (10, 15):
        y = face_ladder(m)
        for name, wv, ph in (("piston", np.ones(m), np.zeros(m)),
                             ("tiltY", y.copy(), np.ones(m))):
            for mode in ("1", "y"):
                got = nearest_over_strips(y, wv, ph, mode) - owning(y, wv, ph, mode)
                print("  m=%2d %-6s mode=%s  gap %+.3e" % (m, name, mode, got))

    print("\nthe class's own 6-point Gauss rule against the exact piecewise integral")
    print("(the integrand is DISCONTINUOUS at every nearest-beam boundary)")
    ratios = []
    for m in (4, 6, 10, 14, 15):
        for col in (0, 1):
            y = face_ladder(m, col)
            for _ in range(2):
                wv, ph = rng.normal(size=m), rng.normal(size=m)
                a = owning(y, wv, ph, "1")
                exact = nearest_over_strips(y, wv, ph, "1") - a
                gauss = gauss6_over_strips(y, wv, ph, "1") - a
                ratios.append(gauss / exact)
    print("  gauss6 / exact on the piston gap: %.6f to %.6f over %d readings"
          % (min(ratios), max(ratios), len(ratios)))

    print("\npiston collinearity (C - A) / (B - A), expected exactly 6 at even m, faceColumn 0")
    for m in (4, 6, 10, 14, 16):
        for col in (0, 1):
            y = face_ladder(m, col)
            wv, ph = rng.normal(size=m), rng.normal(size=m)
            a = owning(y, wv, ph, "1")
            b = nearest_over_strips(y, wv, ph, "1")
            c = nearest_over_face(y, wv, ph, "1", m)
            print("  m=%2d col=%d  ratio %.6f" % (m, col, (c - a) / (b - a)))


if __name__ == "__main__":
    main()
