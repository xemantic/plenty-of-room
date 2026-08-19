#!/usr/bin/env python3
"""
Self-test for the plate-rigidity estimator.

Synthesises a grid ensemble whose out-of-plane field is drawn from the Boltzmann
distribution of a KNOWN orthotropic plate, then checks that `analyse` returns
the rigidities it was built from. This is what makes the comparison against
`T-10` a measurement rather than an assertion: a wrong factor anywhere in the
projection, the covariance inversion or the mode-to-rigidity conversion shows up
here, not as a plausible number in a results table.
"""
import math
import sys

import numpy as np

from analyse_tile import analyse, BOLTZMANN_PNNM

HELICES, ROW_BP = 15, 112
RISE, PITCH = 0.34, 2.69


def synthesise(d_par, d_perp, d_k, frames, seed=0):
    rng = np.random.default_rng(seed)
    x = (np.arange(ROW_BP) - (ROW_BP - 1) / 2) * RISE
    y = (np.arange(HELICES) - (HELICES - 1) / 2) * PITCH
    lx, ly = x.max() - x.min(), y.max() - y.min()
    area = lx * ly

    k = np.diag([d_par * 144.0 * area / lx ** 4,
                 d_perp * 144.0 * area / ly ** 4,
                 d_k * 64.0 * area / (lx ** 2 * ly ** 2)])
    cov = BOLTZMANN_PNNM * np.linalg.inv(k)
    amps = rng.multivariate_normal(np.zeros(3), cov, size=frames)

    xi = 2.0 * x / lx
    eta = 2.0 * y / ly
    p2 = lambda t: 1.5 * t * t - 0.5
    modes = np.stack([
        np.repeat(p2(xi)[None, :], HELICES, axis=0),
        np.repeat(p2(eta)[:, None], ROW_BP, axis=1),
        eta[:, None] * xi[None, :],
    ])
    grids = np.zeros((frames, HELICES, ROW_BP, 3))
    grids[..., 0] = x[None, None, :]
    grids[..., 1] = y[None, :, None]
    grids[..., 2] = np.einsum('fm,mhb->fhb', amps, modes)
    return grids, dict(D_parallel=d_par, D_perpendicular=d_perp, D_k=d_k)


def synthesise_with_higher_modes(d_par, d_perp, d_k, frames, seed=0):
    """As `synthesise`, plus large-amplitude P3/P4 bending modes. Legendre
    polynomials are orthogonal on the rectangle, so these must NOT leak into the
    quadratic amplitudes; if they did, every rigidity would be biased by
    whatever higher-mode content the real trajectory happens to carry."""
    grids, truth = synthesise(d_par, d_perp, d_k, frames, seed)
    rng = np.random.default_rng(seed + 1)
    x = (np.arange(ROW_BP) - (ROW_BP - 1) / 2) * RISE
    y = (np.arange(HELICES) - (HELICES - 1) / 2) * PITCH
    xi = 2.0 * x / (x.max() - x.min())
    eta = 2.0 * y / (y.max() - y.min())
    p3 = lambda t: 2.5 * t ** 3 - 1.5 * t
    p4 = lambda t: (35 * t ** 4 - 30 * t ** 2 + 3) / 8.0
    contaminants = [np.repeat(p3(xi)[None, :], HELICES, axis=0),
                    np.repeat(p4(xi)[None, :], HELICES, axis=0),
                    np.repeat(p3(eta)[:, None], ROW_BP, axis=1),
                    p2_outer(eta, xi)]
    scale = np.abs(grids[..., 2]).std()
    for c in contaminants:
        grids[..., 2] += rng.normal(0.0, scale, size=(frames, 1, 1)) * c[None]
    return grids, truth


def p2_outer(eta, xi):
    p2 = lambda t: 1.5 * t * t - 0.5
    return p2(eta)[:, None] * p2(xi)[None, :]


def main():
    failures = []
    for case in [(85.5019, 3.3450, 42.7509),      # T-10's own closed forms
                 (69.0, 12.0, 35.0),              # a plausible oxDNA outcome
                 (200.0, 200.0, 200.0)]:          # isotropic control
        grids, truth = synthesise(*case, frames=20000, seed=hash(case) % 2**31)
        got = analyse(grids, HELICES, ROW_BP)['rigidities']
        print(f'\ninput  D_par={case[0]:9.4f}  D_perp={case[1]:9.4f}  D_k={case[2]:9.4f}')
        print(f'output D_par={got["D_parallel"]:9.4f}  '
              f'D_perp={got["D_perpendicular"]:9.4f}  D_k={got["D_k"]:9.4f}')
        for key, want in truth.items():
            ratio = got[key] / want
            ok = 0.95 < ratio < 1.05
            print(f'   {key:18s} ratio {ratio:.4f}  {"ok" if ok else "FAIL"}')
            if not ok:
                failures.append((case, key, ratio))
    grids, truth = synthesise_with_higher_modes(85.5019, 3.3450, 42.7509,
                                                frames=20000, seed=11)
    got = analyse(grids, HELICES, ROW_BP)['rigidities']
    print('\nwith large P3/P4/P2xP2 contamination added:')
    for key, want in truth.items():
        ratio = got[key] / want
        ok = 0.95 < ratio < 1.05
        print(f'   {key:18s} ratio {ratio:.4f}  {"ok" if ok else "FAIL"}')
        if not ok:
            failures.append(('higher modes', key, ratio))

    # The mean shape carries the as-designed register strain, so the twist
    # readout has to be exact, not merely monotone: it is compared against the
    # corpus's predicted accumulation for an untwist-corrected 112 bp raster.
    x = (np.arange(ROW_BP) - (ROW_BP - 1) / 2) * RISE
    y = (np.arange(HELICES) - (HELICES - 1) / 2) * PITCH
    lx, ly = x.max() - x.min(), y.max() - y.min()
    print('\nmean-shape twist recovery:')
    for want_deg in (5.0, 30.0, -12.5):
        a3 = math.tan(math.radians(want_deg)) * ly / 4.0
        grids = np.zeros((60, HELICES, ROW_BP, 3))
        grids[..., 0] = x[None, None, :]
        grids[..., 1] = y[None, :, None]
        rng = np.random.default_rng(int(abs(want_deg) * 10))
        grids[..., 2] = (a3 * (2 * x[None, None, :] / lx)
                         * (2 * y[None, :, None] / ly)
                         + rng.normal(0.0, 0.05, grids.shape[:3]))
        got = analyse(grids, HELICES, ROW_BP)['meanShape']['meanTwistDegreesOverTile']
        ok = abs(got - want_deg) < 0.01
        print(f'   {want_deg:+7.2f} deg -> {got:+7.3f} deg  {"ok" if ok else "FAIL"}')
        if not ok:
            failures.append(('twist', want_deg, got))

    # Production frames are a freely diffusing, freely rotating tile. The
    # reference structure is therefore built by iterative alignment, and the
    # rigidities must be EXACTLY invariant under rigid-body motion -- if they
    # are not, the mean is a blur of orientations and every displacement
    # measured against it is meaningless.
    grids, truth = synthesise(85.5019, 3.3450, 42.7509, frames=3000, seed=5)
    plain = analyse(grids, HELICES, ROW_BP)['rigidities']
    rng = np.random.default_rng(7)
    moved = np.empty_like(grids)
    for f in range(len(grids)):
        v = rng.normal(size=3)
        v /= np.linalg.norm(v)
        th = rng.uniform(0, 2 * np.pi)
        k = np.array([[0, -v[2], v[1]], [v[2], 0, -v[0]], [-v[1], v[0], 0]])
        rot = np.eye(3) + np.sin(th) * k + (1 - np.cos(th)) * k @ k
        flat = grids[f].reshape(-1, 3) @ rot.T + rng.normal(0, 30, 3)
        moved[f] = flat.reshape(grids.shape[1], grids.shape[2], 3)
    shifted = analyse(moved, HELICES, ROW_BP)['rigidities']
    print('\nrigid-body invariance (random rotation + 30 nm translation):')
    for key in truth:
        rel = abs(shifted[key] - plain[key]) / plain[key]
        ok = rel < 1e-6
        print(f'   {key:18s} relative change {rel:.2e}  {"ok" if ok else "FAIL"}')
        if not ok:
            failures.append(('rigid-body', key, rel))

    if failures:
        print(f'\n{len(failures)} FAILURES')
        return 1
    print('\nall cases recovered within 5% -- estimator validated')
    return 0


if __name__ == '__main__':
    sys.exit(main())
