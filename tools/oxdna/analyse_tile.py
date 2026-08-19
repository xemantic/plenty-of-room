#!/usr/bin/env python3
"""
Measure the Gen-1 tile's orthotropic plate rigidities from an oxDNA trajectory.

The quantities are the three `T-10` recovers in closed form:

    D_parallel = EI / d          along the helices
    D_perp     = k_theta * d / p across them
    D_k        = GJ / (4 d)      twist

METHOD.  For each frame the duplex centreline is taken as the midpoint of each
Watson-Crick pair, giving a `helices x row_bp` grid of points. Frames are
Kabsch-aligned onto the mean grid, which removes exactly the six rigid-body
degrees of freedom the plate energy is blind to, and the residual is projected
onto the three quadratic Legendre curvature modes

    phi_1 = P2(xi)      xi  = 2x/Lx    (cylindrical bending about y)
    phi_2 = P2(eta)     eta = 2y/Ly    (cylindrical bending about x)
    phi_3 = xi * eta                   (twist)

For the Huber orthotropic plate

    U = 1/2 [ D_x w_xx^2 + 2 D_1 w_xx w_yy + D_y w_yy^2 + 4 D_k w_xy^2 ]

these three modes have CONSTANT curvature, so the energy is exactly quadratic in
their amplitudes and equipartition gives K = kT C^-1 with

    D_x = K_11 Lx^4 / (144 S)        D_1 = K_12 Lx^2 Ly^2 / (144 S)
    D_y = K_22 Ly^4 / (144 S)        D_k = K_33 Lx^2 Ly^2 / (64 S)

Legendre polynomials are orthogonal on the rectangle, so higher bending modes do
not leak into these amplitudes; a higher-order fit is run beside it as a check.

The MEAN grid is reported separately: it carries the as-designed prestrain,
which for an untwist-corrected 112 bp raster the corpus predicts to be a global
twist.
"""

import argparse
import json
import math

import numpy as np

from spectral_fit import design_matrix, fit as spectral_fit, mode_list

OXDNA_LENGTH_NM = 0.8518          # oxDNA length unit in nm
OXDNA_ENERGY_PNNM = 41.42         # oxDNA energy unit in pN*nm
BOLTZMANN_PNNM = 4.141947         # k_B T at 300 K, in pN*nm (the repo's constant)


def read_configurations(path, max_frames=None, stride=1):
    """Yield (time, box, positions) with positions in nm, shape (N, 3)."""
    frame, time, box = [], None, None
    index = 0
    with open(path) as f:
        for line in f:
            if line.startswith('t ='):
                if frame:
                    if index % stride == 0:
                        yield time, box, np.array(frame) * OXDNA_LENGTH_NM
                    index += 1
                    if max_frames and index // stride >= max_frames:
                        return
                    frame = []
                time = float(line.split('=')[1])
            elif line.startswith('b ='):
                box = [float(v) for v in line.split('=')[1].split()]
            elif line.startswith('E ='):
                continue
            else:
                v = line.split()
                if len(v) >= 3:
                    frame.append([float(v[0]), float(v[1]), float(v[2])])
    if frame and index % stride == 0:
        yield time, box, np.array(frame) * OXDNA_LENGTH_NM


def pair_index(nucleotides, helices, row_bp):
    """(helix, offset) -> (forward index, reverse index)."""
    fwd = np.full((helices, row_bp), -1, dtype=int)
    rev = np.full((helices, row_bp), -1, dtype=int)
    for i, (h, o, is_fwd) in enumerate(nucleotides):
        (fwd if is_fwd else rev)[h][o] = i
    assert (fwd >= 0).all() and (rev >= 0).all(), 'lattice has an unpaired site'
    return fwd, rev


BROKEN_PAIR_NM = 1.5   # a designed pair further apart than this has opened


def centreline(pos, fwd, rev):
    """Duplex centreline as the midpoint of each Watson-Crick pair.

    Where a pair has OPENED the midpoint is meaningless -- it wanders off to
    wherever the unpaired base drifted -- so those sites are replaced by linear
    interpolation along the helix between the nearest intact sites. Without this
    a single frayed corner staple injects a metre-scale spike into the curvature
    field and swamps every mode amplitude. Returns (grid, broken_fraction).
    """
    a, b = pos[fwd], pos[rev]
    grid = 0.5 * (a + b)
    broken = np.linalg.norm(a - b, axis=-1) > BROKEN_PAIR_NM
    for h in range(grid.shape[0]):
        bad = np.flatnonzero(broken[h])
        good = np.flatnonzero(~broken[h])
        if len(bad) and len(good) > 1:
            for axis in range(3):
                grid[h, bad, axis] = np.interp(bad, good, grid[h, good, axis])
    return grid, float(broken.mean())


def assert_contiguous(grid, label='trajectory'):
    """oxDNA writes ABSOLUTE positions -- `back_in_box` is off by default -- so no
    periodic unwrapping is needed, and applying one is actively wrong: a body
    longer than half the box gets folded onto itself. This asserts the property
    instead of assuming it, so a trajectory written with `back_in_box = 1` fails
    loudly rather than producing a plausible wrong rigidity."""
    step = np.linalg.norm(np.diff(grid, axis=-2), axis=-1)
    assert step.max() < 5.0, (
        f'{label}: consecutive base pairs {step.max():.1f} nm apart -- the '
        f'trajectory looks periodically wrapped, which this analysis cannot use')
    return grid


def kabsch(mobile, target):
    """Rotation+translation taking `mobile` onto `target` (both (n,3))."""
    mc, tc = mobile.mean(0), target.mean(0)
    h = (mobile - mc).T @ (target - tc)
    u, _, vt = np.linalg.svd(h)
    d = np.sign(np.linalg.det(vt.T @ u.T))
    r = vt.T @ np.diag([1.0, 1.0, d]) @ u.T
    return (mobile - mc) @ r.T + tc


def legendre_modes(xi, eta):
    """The three constant-curvature quadratic modes, evaluated on the grid."""
    p2 = lambda t: 1.5 * t * t - 0.5
    return np.stack([p2(xi), p2(eta), xi * eta], axis=-1)


def rigid_body_modes(xi, eta):
    return np.stack([np.ones_like(xi), xi, eta], axis=-1)


def lattice_observables(grids, pos_all, fwd, rev, helices, row_bp, columns):
    """Quantities that do not go through the plate reduction, so they can
    falsify it rather than inherit it."""
    # base-pair integrity: a designed pair further apart than `cutoff` has
    # melted. The 8 nt corner domains that crossover phase 8 forces are the
    # ones at risk, so this decides whether the crossover lattice survived.
    cutoff = 1.2
    sep = np.linalg.norm(pos_all[:, fwd] - pos_all[:, rev], axis=-1)
    intact = (sep < cutoff)
    edge = np.zeros((helices, row_bp), dtype=bool)
    edge[0, :] = edge[-1, :] = True
    edge[:, :8] = edge[:, -8:] = True

    # oxDNA's OWN equilibrium interhelical distance, against Fischer's SAXS 2.69
    across = np.linalg.norm(np.diff(grids, axis=1), axis=-1)   # (F, h-1, bp)
    # Interface b carries crossovers only at the columns of ITS OWN parity, so
    # averaging over all seven dilutes the sawtooth with columns where this
    # interface has no crossover at all -- which is exactly where the distance
    # is expected to be at its maximum.
    at_vals, mid_vals = [], []
    for b in range(across.shape[1]):
        own = [c for k, c in enumerate(columns) if k % 2 == b % 2]
        other = [c for k, c in enumerate(columns) if k % 2 != b % 2]
        if own:
            at_vals.append(across[:, b, own].mean())
        if other:
            mid_vals.append(across[:, b, other].mean())
    at_crossover = np.array(at_vals)
    midway = np.array(mid_vals)

    # interduplex bend angle per interface, the coordinate Snodin et al. report
    v = np.diff(grids, axis=1)                                  # (F, h-1, bp, 3)
    v = v / np.linalg.norm(v, axis=-1, keepdims=True)
    cosang = np.einsum('fibx,fibx->fib', v[:, :-1], v[:, 1:]).clip(-1, 1)
    angles = np.degrees(np.arccos(cosang))

    return {
        'basePairsIntact': float(intact.mean()),
        'basePairsIntactInterior': float(intact[:, ~edge].mean()),
        'basePairsIntactEdge': float(intact[:, edge].mean()),
        'interhelicalMeanNm': float(across.mean()),
        'interhelicalAtCrossoverNm': float(at_crossover.mean()),
        'interhelicalMidwayNm': float(midway.mean()),
        'interhelicalSdNm': float(across.mean(axis=0).std()),
        'interduplexAngleMeanDeg': float(angles.mean()),
        'interduplexAngleSdDeg': float(angles.std()),
    }


def analyse(grids, helices, row_bp, segments=None, degree=4):
    """grids: (F, helices, row_bp, 3) in nm."""
    # The tile is free in the box: it translates and rotates, so a mean taken
    # over RAW frames is a blur of many orientations and every displacement
    # measured against it would be meaningless. Build the reference by
    # iteratively aligning to it -- the standard fixed-point construction.
    flat = grids.reshape(len(grids), -1, 3)
    flat_mean = flat[0].copy()
    for _ in range(5):
        aligned = np.array([kabsch(f, flat_mean) for f in flat])
        new_mean = aligned.mean(0)
        shift = np.abs(new_mean - flat_mean).max()
        flat_mean = new_mean
        if shift < 1e-6:
            break
    mean = flat_mean.reshape(grids.shape[1], grids.shape[2], 3)

    # In-plane frame taken from the LATTICE, not from the principal axes of the
    # point cloud: the tile is very nearly square, so a principal-axis ordering
    # silently swaps `along` and `across` and would report D_parallel as
    # D_perpendicular. `ex` is the helix axis by construction.
    ex = (mean[:, -1] - mean[:, 0]).mean(0)
    ex /= np.linalg.norm(ex)
    ey = (mean[-1, :] - mean[0, :]).mean(0)
    ey -= ex * (ey @ ex)
    ey /= np.linalg.norm(ey)
    en = np.cross(ex, ey)
    centred = flat_mean - flat_mean.mean(0)
    x = centred @ ex
    y = centred @ ey
    lx, ly = x.max() - x.min(), y.max() - y.min()

    xi = 2.0 * (x - x.mean()) / lx
    eta = 2.0 * (y - y.mean()) / ly
    modes = legendre_modes(xi, eta)
    rigid = rigid_body_modes(xi, eta)
    basis = np.concatenate([rigid, modes], axis=1)     # (n, 6)
    pinv = np.linalg.pinv(basis)

    amps, w_all = [], []
    for g in grids:
        aligned = kabsch(g.reshape(-1, 3), flat_mean)
        w = (aligned - flat_mean) @ en
        w_all.append(w)
        amps.append((pinv @ w)[3:])                    # drop rigid-body part
    amps = np.array(amps)
    w_all = np.array(w_all)

    area = lx * ly

    def rigidities_of(sample):
        c = np.cov(sample.T)
        if np.linalg.matrix_rank(c, tol=1e-18) < c.shape[0]:
            raise ValueError(
                'the three curvature-mode amplitudes have a singular covariance: '
                'the trajectory carries no independent fluctuation in at least '
                'one of them, so no rigidity can be read from it')
        k = BOLTZMANN_PNNM * np.linalg.inv(c)
        return {
            'D_parallel': k[0, 0] * lx ** 4 / (144.0 * area),
            'D_perpendicular': k[1, 1] * ly ** 4 / (144.0 * area),
            'D_coupling': k[0, 1] * lx ** 2 * ly ** 2 / (144.0 * area),
            'D_k': k[2, 2] * lx ** 2 * ly ** 2 / (64.0 * area),
        }, c, k

    rigidities, cov, stiff = rigidities_of(amps)
    half = len(amps) // 2
    first, _, _ = rigidities_of(amps[:half])
    second, _, _ = rigidities_of(amps[half:])

    # Integrated autocorrelation of each mode amplitude, so the frame count is
    # not mistaken for a sample count. A mode whose correlation time approaches
    # the trajectory length is NOT measured, however smooth its variance looks.
    def autocorr_time(x):
        x = x - x.mean()
        n = len(x)
        c0 = (x * x).mean()
        if c0 <= 0:
            return float('nan')
        tau, total = 1.0, 0.0
        for lag in range(1, n // 4):
            r = (x[:-lag] * x[lag:]).mean() / c0
            if r <= 0.0:
                break
            total += r
        tau += 2.0 * total
        return tau

    # Replicas are concatenated, so an autocorrelation taken across the whole
    # series would run over the seams between independent runs. Take it inside
    # each replica and average.
    bounds, o = [], 0
    for n in (segments or [len(amps)]):
        bounds.append((o, o + n))
        o += n
    usable = [(lo, hi) for lo, hi in bounds if hi - lo >= 40]
    if not usable:          # too few frames per replica to segment: use them all
        usable = [(0, len(amps))]
    taus = [float(np.mean([autocorr_time(amps[lo:hi, i]) for lo, hi in usable]))
            for i in range(amps.shape[1])]
    effective = [len(amps) / t if t and t == t else float('nan') for t in taus]

    # mean shape: the as-designed prestrain, in the same three modes
    mean_w = (flat_mean - flat_mean.mean(0)) @ en
    mean_amp = (pinv @ mean_w)[3:]
    # Global twist of the mean shape. For w = a3 * xi * eta with xi = 2x/lx and
    # eta = 2y/ly, dw/dy = 4 a3 x / (lx ly), so the cross-slope runs from
    # -2 a3/ly at one end of the row to +2 a3/ly at the other: the end-to-end
    # twist is atan(4 a3 / ly), carrying NO factor of lx.
    # This is the coordinate the corpus's register accumulation lives on -- an
    # untwist-corrected 112 bp square-lattice raster is predicted to carry a
    # global twist because its 10.67 bp/turn design twist is not B-DNA's.
    mean_shape = {
        'meanBendAlongHelicesNm': float(mean_amp[0]),
        'meanBendAcrossHelicesNm': float(mean_amp[1]),
        'meanTwistAmplitudeNm': float(mean_amp[2]),
        'meanTwistDegreesOverTile':
            float(math.degrees(math.atan(4.0 * mean_amp[2] / ly))),
    }

    per_replica_rigidities = []
    o = 0
    for n in (segments or []):
        if n > 40:
            r, _, _ = rigidities_of(amps[o:o + n])
            per_replica_rigidities.append({k: float(v) for k, v in r.items()})
        o += n

    # The quadratic modes above are the tile's SOFTEST and therefore its
    # slowest. The same rigidities also set every higher bending mode, and those
    # decorrelate faster, so a maximum-likelihood fit over the whole spectrum is
    # the better-sampled reading of the same physics. Both are reported: they
    # must agree, and where they do not it is the sampling that is being
    # measured rather than the plate.
    basis_full, n_rigid = design_matrix(degree, xi, eta)
    pinv_full = np.linalg.pinv(basis_full)
    spec_amps = np.array([(pinv_full @ w)[n_rigid:] for w in w_all])
    try:
        spectral = spectral_fit(spec_amps, lx, ly, degree)
        spectral['autocorrelationTimeFrames'] = [
            float(np.mean([autocorr_time(spec_amps[lo:hi, i]) for lo, hi in usable]))
            for i in range(min(6, spec_amps.shape[1]))]
    except Exception as exc:                       # a fit can legitimately fail
        spectral = {'error': str(exc)}

    rmsf = np.sqrt((w_all ** 2).mean(0))
    grid_rmsf = rmsf.reshape(helices, row_bp)

    return {
        'frames': int(grids.shape[0]),
        'convergence': {
            'perReplica': per_replica_rigidities,
            'autocorrelationTimeFrames': [float(t) for t in taus],
            'effectiveSamples': [float(e) for e in effective],
            'firstHalf': {k: float(v) for k, v in first.items()},
            'secondHalf': {k: float(v) for k, v in second.items()},
            'halfSplitRatio': {k: float(second[k] / first[k]) if first[k] else None
                               for k in first},
        },
        'lengthAlongHelicesNm': float(lx),
        'lengthAcrossHelicesNm': float(ly),
        'areaNm2': float(area),
        'amplitudeCovarianceNm2': cov.tolist(),
        'modeStiffnessPnPerNm': stiff.tolist(),
        'rigidities': {k: float(v) for k, v in rigidities.items()},
        'spectralRigidities': spectral,
        'meanShape': mean_shape,
        'outOfPlaneRmsNm': float(np.sqrt((w_all ** 2).mean())),
        'outOfPlaneRmsCentreNm': float(grid_rmsf[helices // 2, row_bp // 2]),
        'outOfPlaneRmsCornerNm': float(np.mean([
            grid_rmsf[0, 0], grid_rmsf[0, -1], grid_rmsf[-1, 0], grid_rmsf[-1, -1]])),
    }


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--traj', required=True, nargs='+',
                   help='one or more replica trajectories, pooled')
    p.add_argument('--nucleotides', required=True)
    p.add_argument('--helices', type=int, default=15)
    p.add_argument('--row-bp', type=int, default=112)
    p.add_argument('--skip', type=int, default=0, help='equilibration frames to drop')
    p.add_argument('--stride', type=int, default=1)
    p.add_argument('--out', default=None)
    p.add_argument('--columns', default='8,24,40,56,72,88,104')
    a = p.parse_args()

    nucleotides = json.load(open(a.nucleotides))
    fwd, rev = pair_index(nucleotides, a.helices, a.row_bp)

    grids, raws, per_replica, broken = [], [], [], []
    for path in a.traj:
        n0 = len(grids)
        for i, (t, box, pos) in enumerate(read_configurations(path, stride=a.stride)):
            if i < a.skip:
                continue
            raws.append(pos)
            g, frac = centreline(pos, fwd, rev)
            broken.append(frac)
            grids.append(assert_contiguous(g, f'{path} frame {i}'))
        per_replica.append(len(grids) - n0)
    if len(set(per_replica)) > 1:
        print(f'# replicas contribute {per_replica} frames')
    grids = np.array(grids)
    raws = np.array(raws)
    if len(grids) < 20:
        raise SystemExit(f'only {len(grids)} frames after skipping; need >= 20')

    columns = [int(c) for c in a.columns.split(',')]
    result = analyse(grids, a.helices, a.row_bp, per_replica)
    result['lattice'] = lattice_observables(grids, raws, fwd, rev, a.helices,
                                            a.row_bp, columns)
    result['replicas'] = per_replica
    result['brokenPairFraction'] = float(np.mean(broken))
    text = json.dumps(result, indent=1)
    print(text)
    if a.out:
        open(a.out, 'w').write(text)


if __name__ == '__main__':
    main()
