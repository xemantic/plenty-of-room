#!/usr/bin/env python3
"""T-71 — extract a MEASURED B-DNA backbone survey from crystallographic coordinates.

Retained in this repository so the derivation of every constant in
`anchoring.BackboneTorsion` is reproducible and inspectable (SESSION-PROMPT §"You are not
bound to Kotlin").

What it does, in order:

1.  Asks the RCSB search API for X-ray structures whose polymer content is DNA only, at a
    stated resolution ceiling.  No hand-picked list, so the sample cannot be curated toward
    an answer.
2.  Downloads each entry's PDB file (model 1, altloc blank or A).
3.  For every deoxyribonucleotide computes the six backbone torsions alpha..zeta, the
    glycosidic chi, the five ring torsions nu0..nu4 and hence the pseudorotation phase P,
    plus the covalent geometry of the phosphodiester linkage it makes to the next residue.
4.  Fits, per chain and per step, the SCREW transform carrying residue i onto residue i+1
    (Kabsch on the shared heavy-atom set).  Its axis is the local helical axis; its
    translation is the rise and its rotation the twist.
5.  Expresses each residue's atoms in the local frame (radial, tangential, axial) anchored
    on that residue's OWN phosphorus, with the axial direction oriented 5'->3'.  Averaging
    those over a filtered population gives the rigid nucleotide TEMPLATE the Kotlin torsion
    check places into this project's stylised duplex model.
6.  Clusters the observed (alpha..zeta, chi) septets by k-means on the circular mean, giving
    the "populated regions" against which a junction's solved torsions are judged.

Output: a single JSON at the path given by --out (default gpd/data/T-71-bdna-backbone-survey.json).

Usage:
    python3 tools/T-71-bdna-backbone-survey.py --resolution 1.6 --limit 400

Requires numpy and network access to search.rcsb.org and files.rcsb.org.
"""

from __future__ import annotations

import argparse
import json
import math
import os
import sys
import time
import urllib.request
import urllib.error
from collections import defaultdict

import numpy as np

SEARCH_URL = "https://search.rcsb.org/rcsbsearch/v2/query"
FILE_URL = "https://files.rcsb.org/download/{}.pdb"

DNA_RESIDUES = {"DA", "DC", "DG", "DT"}
PURINES = {"DA", "DG"}

# The atoms the template carries.  Everything the phosphodiester closure needs, plus the
# glycosidic anchor so chi can be read, plus the ring so the pucker can be recomputed.
TEMPLATE_ATOMS = [
    "P", "OP1", "OP2", "O5'", "C5'", "C4'", "O4'", "C3'", "O3'", "C2'", "C1'",
    # The two base atoms the glycosidic torsion chi needs, under names that do not
    # depend on whether the residue is a purine or a pyrimidine: N9/C4 against N1/C2.
    "NGLY", "CGLY",
]

BACKBONE_REQUIRED = ["P", "O5'", "C5'", "C4'", "C3'", "O3'", "C1'", "O4'", "C2'"]


# ----------------------------------------------------------------------- small geometry


def dihedral(p0, p1, p2, p3):
    """Signed dihedral in degrees, IUPAC sign convention."""
    b0 = p0 - p1
    b1 = p2 - p1
    b2 = p3 - p2
    b1n = b1 / np.linalg.norm(b1)
    v = b0 - np.dot(b0, b1n) * b1n
    w = b2 - np.dot(b2, b1n) * b1n
    x = np.dot(v, w)
    y = np.dot(np.cross(b1n, v), w)
    return math.degrees(math.atan2(y, x))


def angle(p0, p1, p2):
    """Bond angle p0-p1-p2 in degrees."""
    a = p0 - p1
    b = p2 - p1
    c = np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))
    return math.degrees(math.acos(max(-1.0, min(1.0, c))))


def pseudorotation(nu):
    """Altona-Sundaralingam pseudorotation phase in degrees, 0..360, from nu0..nu4."""
    n0, n1, n2, n3, n4 = nu
    num = (n4 + n1) - (n3 + n0)
    den = 2.0 * n2 * (math.sin(math.radians(36.0)) + math.sin(math.radians(72.0)))
    # atan2 already carries the sign of nu2 through the denominator, so the classical
    # "+180 when nu2 < 0" correction to atan() must NOT be applied again here.
    return math.degrees(math.atan2(num, den)) % 360.0


def kabsch(a, b):
    """Rotation R and translation t with R @ a_i + t ~= b_i, least squares."""
    ca = a.mean(axis=0)
    cb = b.mean(axis=0)
    h = (a - ca).T @ (b - cb)
    u, _, vt = np.linalg.svd(h)
    d = np.sign(np.linalg.det(vt.T @ u.T))
    r = vt.T @ np.diag([1.0, 1.0, d]) @ u.T
    return r, cb - r @ ca


def screw(r, t):
    """Screw decomposition of a rigid motion: axis direction, a point on the axis, the
    rotation about it in degrees and the translation along it.

    Returns (axis_unit, point, twist_deg, rise) with the axis oriented so rise >= 0.
    """
    cos_theta = max(-1.0, min(1.0, 0.5 * (np.trace(r) - 1.0)))
    theta = math.acos(cos_theta)
    if theta < 1e-8:
        return None
    axis = np.array([
        r[2, 1] - r[1, 2],
        r[0, 2] - r[2, 0],
        r[1, 0] - r[0, 1],
    ]) / (2.0 * math.sin(theta))
    rise = float(np.dot(axis, t))
    if rise < 0.0:
        axis = -axis
        rise = -rise
        theta = theta  # magnitude unchanged; sign is carried by the axis orientation
    # A point on the axis: solve (I - R) x = t_perp in the plane normal to the axis.
    perp = t - np.dot(axis, t) * axis
    m = np.eye(3) - r
    point, *_ = np.linalg.lstsq(m + np.outer(axis, axis), perp, rcond=None)
    # Signed twist about the oriented axis.
    ref = np.array([1.0, 0.0, 0.0])
    if abs(np.dot(ref, axis)) > 0.9:
        ref = np.array([0.0, 1.0, 0.0])
    e1 = ref - np.dot(ref, axis) * axis
    e1 /= np.linalg.norm(e1)
    e2 = np.cross(axis, e1)
    rotated = r @ e1
    twist = math.degrees(math.atan2(np.dot(rotated, e2), np.dot(rotated, e1)))
    return axis, point, twist, rise


# ----------------------------------------------------------------------- RCSB access


def rcsb_entries(resolution, limit):
    query = {
        "query": {
            "type": "group",
            "logical_operator": "and",
            "nodes": [
                {
                    "type": "terminal",
                    "service": "text",
                    "parameters": {
                        "attribute": "rcsb_entry_info.experimental_method",
                        "operator": "exact_match",
                        "value": "X-ray",
                    },
                },
                {
                    "type": "terminal",
                    "service": "text",
                    "parameters": {
                        "attribute": "rcsb_entry_info.resolution_combined",
                        "operator": "less_or_equal",
                        "value": resolution,
                    },
                },
                {
                    "type": "terminal",
                    "service": "text",
                    "parameters": {
                        "attribute": "rcsb_entry_info.polymer_composition",
                        "operator": "exact_match",
                        "value": "DNA",
                    },
                },
            ],
        },
        "return_type": "entry",
        "request_options": {
            "paginate": {"start": 0, "rows": limit},
            "sort": [{"sort_by": "rcsb_entry_info.resolution_combined", "direction": "asc"}],
        },
    }
    body = json.dumps(query).encode()
    request = urllib.request.Request(
        SEARCH_URL, data=body, headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(request, timeout=120) as handle:
        payload = json.load(handle)
    return [row["identifier"] for row in payload.get("result_set", [])]


def fetch_pdb(entry, cache):
    path = os.path.join(cache, entry + ".pdb")
    if os.path.exists(path):
        with open(path) as handle:
            return handle.read()
    for attempt in range(3):
        try:
            with urllib.request.urlopen(FILE_URL.format(entry), timeout=60) as handle:
                text = handle.read().decode("latin-1")
            with open(path, "w") as handle:
                handle.write(text)
            return text
        except urllib.error.HTTPError as error:
            if error.code == 404:
                return None
            time.sleep(2.0 * (attempt + 1))
        except Exception:
            time.sleep(2.0 * (attempt + 1))
    return None


# ----------------------------------------------------------------------- PDB parsing


def parse_residues(text):
    """Ordered per-chain lists of DNA residues, each a dict of atom name -> coordinate."""
    chains = defaultdict(list)
    seen = {}
    for line in text.splitlines():
        if line.startswith("ENDMDL"):
            break
        if not line.startswith("ATOM") and not line.startswith("HETATM"):
            continue
        resname = line[17:20].strip()
        if resname not in DNA_RESIDUES:
            continue
        altloc = line[16]
        if altloc not in (" ", "A"):
            continue
        chain = line[21]
        key = (chain, line[22:27])
        name = line[12:16].strip()
        try:
            xyz = np.array(
                [float(line[30:38]), float(line[38:46]), float(line[46:54])]
            )
        except ValueError:
            continue
        if key not in seen:
            residue = {"chain": chain, "id": line[22:27], "name": resname, "atoms": {}}
            seen[key] = residue
            chains[chain].append(residue)
        seen[key]["atoms"][name] = xyz
    return chains


def connected(previous, current):
    """True when previous.O3' and current.P are within a covalent phosphodiester bond."""
    if "O3'" not in previous["atoms"] or "P" not in current["atoms"]:
        return False
    d = np.linalg.norm(previous["atoms"]["O3'"] - current["atoms"]["P"])
    return 1.3 < d < 1.9


# ----------------------------------------------------------------------- the survey


def survey(entries, cache, verbose=True):
    residues = []
    steps = []
    linkage = []
    used_entries = []
    for n, entry in enumerate(entries):
        text = fetch_pdb(entry, cache)
        if text is None:
            continue
        chains = parse_residues(text)
        got = 0
        for chain, items in chains.items():
            for i, residue in enumerate(items):
                atoms = residue["atoms"]
                if any(a not in atoms for a in BACKBONE_REQUIRED):
                    continue
                nxt = items[i + 1] if i + 1 < len(items) else None
                prv = items[i - 1] if i > 0 else None
                if nxt is not None and (
                    not connected(residue, nxt)
                    or any(a not in nxt["atoms"] for a in ("P", "O5'", "C5'"))
                ):
                    nxt = None
                if prv is not None and (
                    not connected(prv, residue) or "O3'" not in prv["atoms"]
                ):
                    prv = None

                nu = [
                    dihedral(atoms["C4'"], atoms["O4'"], atoms["C1'"], atoms["C2'"]),
                    dihedral(atoms["O4'"], atoms["C1'"], atoms["C2'"], atoms["C3'"]),
                    dihedral(atoms["C1'"], atoms["C2'"], atoms["C3'"], atoms["C4'"]),
                    dihedral(atoms["C2'"], atoms["C3'"], atoms["C4'"], atoms["O4'"]),
                    dihedral(atoms["C3'"], atoms["C4'"], atoms["O4'"], atoms["C1'"]),
                ]
                phase = pseudorotation(nu)

                base_anchor = "N9" if residue["name"] in PURINES else "N1"
                base_second = "C4" if residue["name"] in PURINES else "C2"
                chi = None
                if base_anchor in atoms and base_second in atoms:
                    chi = dihedral(
                        atoms["O4'"], atoms["C1'"], atoms[base_anchor], atoms[base_second]
                    )

                alpha = beta = gamma = delta = epsilon = zeta = None
                gamma = dihedral(atoms["O5'"], atoms["C5'"], atoms["C4'"], atoms["C3'"])
                delta = dihedral(atoms["C5'"], atoms["C4'"], atoms["C3'"], atoms["O3'"])
                beta = dihedral(atoms["P"], atoms["O5'"], atoms["C5'"], atoms["C4'"])
                if prv is not None:
                    alpha = dihedral(
                        prv["atoms"]["O3'"], atoms["P"], atoms["O5'"], atoms["C5'"]
                    )
                if nxt is not None:
                    epsilon = dihedral(
                        atoms["C4'"], atoms["C3'"], atoms["O3'"], nxt["atoms"]["P"]
                    )
                    zeta = dihedral(
                        atoms["C3'"], atoms["O3'"], nxt["atoms"]["P"], nxt["atoms"]["O5'"]
                    )
                    linkage.append(
                        {
                            "o3p": float(
                                np.linalg.norm(atoms["O3'"] - nxt["atoms"]["P"])
                            ),
                            "po5": float(
                                np.linalg.norm(
                                    nxt["atoms"]["P"] - nxt["atoms"]["O5'"]
                                )
                            ),
                            "c3o3p": angle(
                                atoms["C3'"], atoms["O3'"], nxt["atoms"]["P"]
                            ),
                            "o3po5": angle(
                                atoms["O3'"], nxt["atoms"]["P"], nxt["atoms"]["O5'"]
                            ),
                            "po5c5": angle(
                                nxt["atoms"]["P"], nxt["atoms"]["O5'"], nxt["atoms"]["C5'"]
                            ),
                            "pp": float(
                                np.linalg.norm(atoms["P"] - nxt["atoms"]["P"])
                            ),
                            "phase": phase,
                        }
                    )

                record = {
                    "entry": entry,
                    "chain": chain,
                    "residue": residue["id"].strip(),
                    "name": residue["name"],
                    "alpha": alpha,
                    "beta": beta,
                    "gamma": gamma,
                    "delta": delta,
                    "epsilon": epsilon,
                    "zeta": zeta,
                    "chi": chi,
                    "phase": phase,
                }
                residues.append(record)
                got += 1

                # The local screw carrying a WINDOW of residues onto the next window.  A
                # single dinucleotide step is a poor estimator of the helical axis — roll,
                # slide and propeller twist move it by more than a degree per step — so the
                # window is what makes the template's axial coordinate meaningful.
                window = 3
                lo = i - 1
                hi = lo + window  # blocks are [lo, lo+window) -> [lo+1, lo+window+1)
                if nxt is not None and lo >= 0 and hi < len(items):
                    block = items[lo:hi + 1]
                    ok = all(
                        all(a in block[j]["atoms"] for a in BACKBONE_REQUIRED)
                        for j in range(len(block))
                    ) and all(connected(block[j], block[j + 1]) for j in range(len(block) - 1))
                    if not ok:
                        continue
                    shared = BACKBONE_REQUIRED
                    a = np.array(
                        [block[j]["atoms"][x] for j in range(window) for x in shared]
                    )
                    b = np.array(
                        [block[j + 1]["atoms"][x] for j in range(window) for x in shared]
                    )
                    r, t = kabsch(a, b)
                    rmsd = float(
                        np.sqrt((((a @ r.T) + t - b) ** 2).sum(axis=1).mean())
                    )
                    dec = screw(r, t)
                    if dec is None:
                        continue
                    axis, point, twist, rise = dec
                    radial_vector = atoms["P"] - point
                    radial_vector = radial_vector - np.dot(radial_vector, axis) * axis
                    radius = float(np.linalg.norm(radial_vector))
                    if radius < 1e-6:
                        continue
                    e_r = radial_vector / radius
                    e_t = np.cross(axis, e_r)
                    named = dict(atoms)
                    if base_anchor in atoms:
                        named["NGLY"] = atoms[base_anchor]
                    if base_second in atoms:
                        named["CGLY"] = atoms[base_second]
                    local = {}
                    for name in TEMPLATE_ATOMS:
                        if name not in named:
                            continue
                        d = named[name] - atoms["P"]
                        local[name] = [
                            float(np.dot(d, e_r)),
                            float(np.dot(d, e_t)),
                            float(np.dot(d, axis)),
                        ]
                    # The SUCCESSOR residue, in its OWN local frame about the SAME axis, plus
                    # the actual step between the two phosphates.  Reapplying one template at a
                    # fitted screw does not reproduce a real dinucleotide to better than ~0.15 A,
                    # which is eight bond-length sigmas — so the free limiting case has to carry
                    # the real neighbour rather than a copy of the residue itself.
                    rv2 = nxt["atoms"]["P"] - point
                    rv2 = rv2 - np.dot(rv2, axis) * axis
                    radius2 = float(np.linalg.norm(rv2))
                    if radius2 < 1e-6:
                        continue
                    e_r2 = rv2 / radius2
                    e_t2 = np.cross(axis, e_r2)
                    next_named = dict(nxt["atoms"])
                    next_purine = nxt["name"] in PURINES
                    if ("N9" if next_purine else "N1") in nxt["atoms"]:
                        next_named["NGLY"] = nxt["atoms"]["N9" if next_purine else "N1"]
                    if ("C4" if next_purine else "C2") in nxt["atoms"]:
                        next_named["CGLY"] = nxt["atoms"]["C4" if next_purine else "C2"]
                    next_local = {}
                    for name in TEMPLATE_ATOMS:
                        if name not in next_named:
                            continue
                        d = next_named[name] - nxt["atoms"]["P"]
                        next_local[name] = [
                            float(np.dot(d, e_r2)),
                            float(np.dot(d, e_t2)),
                            float(np.dot(d, axis)),
                        ]
                    step_twist = math.degrees(
                        math.atan2(float(np.dot(e_r2, e_t)), float(np.dot(e_r2, e_r)))
                    )
                    step_rise = float(
                        np.dot(nxt["atoms"]["P"] - atoms["P"], axis)
                    )
                    next_nu = [
                        dihedral(
                            nxt["atoms"]["C4'"], nxt["atoms"]["O4'"], nxt["atoms"]["C1'"],
                            nxt["atoms"]["C2'"]
                        ),
                        dihedral(
                            nxt["atoms"]["O4'"], nxt["atoms"]["C1'"], nxt["atoms"]["C2'"],
                            nxt["atoms"]["C3'"]
                        ),
                        dihedral(
                            nxt["atoms"]["C1'"], nxt["atoms"]["C2'"], nxt["atoms"]["C3'"],
                            nxt["atoms"]["C4'"]
                        ),
                        dihedral(
                            nxt["atoms"]["C2'"], nxt["atoms"]["C3'"], nxt["atoms"]["C4'"],
                            nxt["atoms"]["O4'"]
                        ),
                        dihedral(
                            nxt["atoms"]["C3'"], nxt["atoms"]["C4'"], nxt["atoms"]["O4'"],
                            nxt["atoms"]["C1'"]
                        ),
                    ]
                    next_torsions = {
                        "alpha": dihedral(
                            atoms["O3'"], nxt["atoms"]["P"], nxt["atoms"]["O5'"],
                            nxt["atoms"]["C5'"]
                        ),
                        "beta": dihedral(
                            nxt["atoms"]["P"], nxt["atoms"]["O5'"], nxt["atoms"]["C5'"],
                            nxt["atoms"]["C4'"]
                        ),
                        "gamma": dihedral(
                            nxt["atoms"]["O5'"], nxt["atoms"]["C5'"], nxt["atoms"]["C4'"],
                            nxt["atoms"]["C3'"]
                        ),
                        "delta": dihedral(
                            nxt["atoms"]["C5'"], nxt["atoms"]["C4'"], nxt["atoms"]["C3'"],
                            nxt["atoms"]["O3'"]
                        ),
                        "epsilon": epsilon,
                        "zeta": zeta,
                        "chi": (
                            dihedral(
                                nxt["atoms"]["O4'"], nxt["atoms"]["C1'"],
                                next_named["NGLY"], next_named["CGLY"]
                            )
                            if "NGLY" in next_named and "CGLY" in next_named
                            else None
                        ),
                    }
                    steps.append(
                        {
                            "entry": entry,
                            "rmsd": rmsd,
                            "nextLocal": next_local,
                            "nextRadius": radius2,
                            "nextPhase": pseudorotation(next_nu),
                            "nextTorsions": next_torsions,
                            "stepTwist": step_twist,
                            "stepRise": step_rise,
                            "twist": twist,
                            "rise": rise,
                            "radius": radius,
                            "phase": phase,
                            "local": local,
                            "residue": record,
                        }
                    )
        if got:
            used_entries.append(entry)
        if verbose and n % 25 == 0:
            print(
                f"  {n}/{len(entries)} entries, {len(residues)} residues, {len(steps)} steps",
                file=sys.stderr,
            )
    return residues, steps, linkage, used_entries


# ----------------------------------------------------------------------- statistics


def circular_mean(values):
    s = sum(math.sin(math.radians(v)) for v in values)
    c = sum(math.cos(math.radians(v)) for v in values)
    return math.degrees(math.atan2(s / len(values), c / len(values)))


def circular_sd(values):
    s = sum(math.sin(math.radians(v)) for v in values) / len(values)
    c = sum(math.cos(math.radians(v)) for v in values) / len(values)
    r = math.hypot(s, c)
    r = min(1.0, max(1e-12, r))
    return math.degrees(math.sqrt(-2.0 * math.log(r)))


def wrap(delta):
    return (delta + 180.0) % 360.0 - 180.0


def torsion_stats(values):
    values = [v for v in values if v is not None]
    if not values:
        return None
    mean = circular_mean(values)
    deviations = sorted(abs(wrap(v - mean)) for v in values)

    def pct(p):
        k = min(len(deviations) - 1, int(round(p * (len(deviations) - 1))))
        return deviations[k]

    return {
        "n": len(values),
        "mean": mean,
        "sd": circular_sd(values),
        "dev50": pct(0.50),
        "dev95": pct(0.95),
        "dev99": pct(0.99),
    }


def scalar_stats(values):
    values = sorted(values)
    if not values:
        return None
    arr = np.array(values)
    return {
        "n": len(values),
        "mean": float(arr.mean()),
        "sd": float(arr.std(ddof=1)) if len(values) > 1 else 0.0,
        "p1": float(np.percentile(arr, 1)),
        "p50": float(np.percentile(arr, 50)),
        "p99": float(np.percentile(arr, 99)),
    }


TORSIONS = ["alpha", "beta", "gamma", "delta", "epsilon", "zeta", "chi"]

HISTOGRAM_BINS = 36  # ten-degree bins over the whole turn, starting at -180


def histogram(values):
    """Counts of observed torsions in ten-degree bins from -180, the POPULATED-region test.

    A k-means class radius is a poor occupancy test: a diffuse class has a 99th-percentile
    radius of 150 degrees and admits almost anything.  A marginal histogram says instead how
    many residues have actually been SEEN with a torsion near the value in question, which is
    the statement a verdict wants to be able to make.
    """
    counts = [0] * HISTOGRAM_BINS
    total = 0
    for v in values:
        if v is None:
            continue
        folded = (v + 180.0) % 360.0
        counts[min(HISTOGRAM_BINS - 1, int(folded / (360.0 / HISTOGRAM_BINS)))] += 1
        total += 1
    return {"total": total, "counts": counts}


def kmeans_circular(records, k, seed=20260814, iterations=120):
    """k-means on the 7-torsion septet with a chord metric on the unit circle per angle."""
    rows = [r for r in records if all(r[t] is not None for t in TORSIONS)]
    if len(rows) < k:
        return []
    data = np.array(
        [
            [f(math.radians(r[t])) for t in TORSIONS for f in (math.cos, math.sin)]
            for r in rows
        ]
    )
    rng = np.random.default_rng(seed)
    # k-means++ seeding, deterministic under the fixed seed.
    centres = [data[rng.integers(len(data))]]
    for _ in range(k - 1):
        d = np.min(
            np.stack([((data - c) ** 2).sum(axis=1) for c in centres]), axis=0
        )
        total = d.sum()
        if total <= 0:
            centres.append(data[rng.integers(len(data))])
            continue
        centres.append(data[rng.choice(len(data), p=d / total)])
    centres = np.array(centres)
    labels = np.zeros(len(data), dtype=int)
    for _ in range(iterations):
        distances = np.stack([((data - c) ** 2).sum(axis=1) for c in centres])
        new = distances.argmin(axis=0)
        if np.array_equal(new, labels):
            break
        labels = new
        for j in range(k):
            member = data[labels == j]
            if len(member):
                centres[j] = member.mean(axis=0)
    out = []
    for j in range(k):
        member = [rows[i] for i in range(len(rows)) if labels[i] == j]
        if not member:
            continue
        centre = {t: circular_mean([r[t] for r in member]) for t in TORSIONS}
        spread = {t: circular_sd([r[t] for r in member]) for t in TORSIONS}
        radii = []
        for r in member:
            radii.append(
                max(abs(wrap(r[t] - centre[t])) for t in TORSIONS)
            )
        radii.sort()
        out.append(
            {
                "population": len(member),
                "fraction": len(member) / len(rows),
                "centre": centre,
                "sd": spread,
                "radius95": radii[min(len(radii) - 1, int(0.95 * (len(radii) - 1)))],
                "radius99": radii[min(len(radii) - 1, int(0.99 * (len(radii) - 1)))],
                "radiusMax": radii[-1],
            }
        )
    out.sort(key=lambda c: -c["population"])
    return out


# ----------------------------------------------------------------------- main


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--resolution", type=float, default=1.6)
    parser.add_argument("--limit", type=int, default=400)
    parser.add_argument("--clusters", type=int, default=12)
    parser.add_argument("--rmsd", type=float, default=0.35)
    parser.add_argument(
        "--cache",
        default=os.environ.get("T71_CACHE", "/tmp/T-71-pdb-cache"),
    )
    parser.add_argument("--out", default="gpd/data/T-71-bdna-backbone-survey.json")
    args = parser.parse_args()

    os.makedirs(args.cache, exist_ok=True)
    os.makedirs(os.path.dirname(args.out), exist_ok=True)

    print("querying RCSB ...", file=sys.stderr)
    entries = rcsb_entries(args.resolution, args.limit)
    print(f"  {len(entries)} entries", file=sys.stderr)

    residues, steps, linkage, used = survey(entries, args.cache)
    print(
        f"{len(residues)} residues, {len(steps)} steps, {len(linkage)} linkages",
        file=sys.stderr,
    )

    # B-form helical population: a right-handed step of B-like twist and rise, and a
    # south (C2'-endo) sugar.  A-form: north (C3'-endo) sugar with an A-like rise.
    # A step whose 3-residue window superposes badly onto the next is not a helix, so it
    # cannot define a helical frame; the RMSD ceiling is what makes the template sharp.
    helical = [s for s in steps if s["rmsd"] <= args.rmsd]
    b_steps = [
        s
        for s in helical
        if 30.0 <= s["twist"] <= 42.0
        and 3.0 <= s["rise"] <= 3.7
        and 130.0 <= s["phase"] <= 190.0
    ]
    a_steps = [
        s
        for s in helical
        if 26.0 <= s["twist"] <= 40.0
        and 2.2 <= s["rise"] <= 3.1
        and (s["phase"] <= 40.0 or s["phase"] >= 340.0)
    ]

    def template(population, label):
        """The template is a REAL measured nucleotide, not an average of many.

        Averaging local coordinates over a population whose frames carry a degree or two of
        noise CONTRACTS every internal bond — the mean of points scattered on a sphere lies
        inside it — and a template whose C5'-O5' bond is 3 % short is not a molecule.  So the
        mean is computed only to define the centre, and the template emitted is the population
        MEDOID: the one observed residue whose local coordinates are closest to that centre.
        Its internal covalent geometry is therefore exactly some real nucleotide's.
        """
        if not population:
            return None
        common = [
            name
            for name in TEMPLATE_ATOMS
            if sum(1 for s in population if name in s["local"]) >= 0.9 * len(population)
        ]
        members = [
            s
            for s in population
            if all(name in s["local"] for name in common)
            and all(name in s["nextLocal"] for name in common)
            and s["nextTorsions"]["chi"] is not None
        ]
        if not members:
            return None
        coordinates = np.array(
            [[s["local"][name] for name in common] for s in members]
        )
        centre = coordinates.mean(axis=0)
        rmsd = np.sqrt(((coordinates - centre) ** 2).sum(axis=2).mean(axis=1))
        pick = int(rmsd.argmin())
        medoid = members[pick]
        spread = coordinates.std(axis=0, ddof=1)
        atoms = {}
        for j, name in enumerate(common):
            atoms[name] = {
                "radial": float(medoid["local"][name][0]),
                "tangential": float(medoid["local"][name][1]),
                "axial": float(medoid["local"][name][2]),
                "mean": [float(centre[j, i]) for i in range(3)],
                "sd": [float(spread[j, i]) for i in range(3)],
                "n": len(members),
            }
        return {
            "medoid": {
                "entry": medoid["entry"],
                "chain": medoid["residue"]["chain"],
                "residue": medoid["residue"]["residue"],
                "name": medoid["residue"]["name"],
                "rmsdToCentre": float(rmsd[pick]),
                "nextLocal": medoid["nextLocal"],
                "nextRadius": medoid["nextRadius"],
                "nextPhase": medoid["nextPhase"],
                "nextTorsions": medoid["nextTorsions"],
                "stepTwist": medoid["stepTwist"],
                "stepRise": medoid["stepRise"],
                "torsions": {t: medoid["residue"][t] for t in TORSIONS},
                "phase": medoid["phase"],
                "twist": medoid["twist"],
                "rise": medoid["rise"],
                "radius": medoid["radius"],
            },
            "label": label,
            "n": len(population),
            "twist": scalar_stats([s["twist"] for s in population]),
            "rise": scalar_stats([s["rise"] for s in population]),
            "phosphateRadius": scalar_stats([s["radius"] for s in population]),
            "phase": scalar_stats([s["phase"] for s in population]),
            "rmsd": scalar_stats([s["rmsd"] for s in population]),
            "torsions": {
                t: torsion_stats([s["residue"][t] for s in population]) for t in TORSIONS
            },
            "atoms": atoms,
        }

    b_residues = [r for r in residues if 120.0 <= r["phase"] <= 200.0]
    result = {
        "provenance": {
            "script": "tools/T-71-bdna-backbone-survey.py",
            "search": SEARCH_URL,
            "windowResidues": 3,
            "rmsdCeiling": args.rmsd,
            "criteria": {
                "experimental_method": "X-ray",
                "resolution_combined_max": args.resolution,
                "polymer_composition": "DNA",
            },
            "entriesReturned": len(entries),
            "entriesUsed": len(used),
            "entryIds": used,
        },
        "counts": {
            "residues": len(residues),
            "residuesSouth": len(b_residues),
            "steps": len(steps),
            "stepsHelical": len(helical),
            "stepsBForm": len(b_steps),
            "stepsAForm": len(a_steps),
            "linkages": len(linkage),
        },
        "linkageGeometry": {
            "o3p": scalar_stats([l["o3p"] for l in linkage]),
            "po5": scalar_stats([l["po5"] for l in linkage]),
            "c3o3p": scalar_stats([l["c3o3p"] for l in linkage]),
            "o3po5": scalar_stats([l["o3po5"] for l in linkage]),
            "po5c5": scalar_stats([l["po5c5"] for l in linkage]),
            "ppAll": scalar_stats([l["pp"] for l in linkage]),
            "ppSouth": scalar_stats(
                [l["pp"] for l in linkage if 120.0 <= l["phase"] <= 200.0]
            ),
            "ppNorth": scalar_stats(
                [l["pp"] for l in linkage if l["phase"] <= 40.0 or l["phase"] >= 340.0]
            ),
        },
        "torsionsAll": {t: torsion_stats([r[t] for r in residues]) for t in TORSIONS},
        "histograms": {t: histogram([r[t] for r in residues]) for t in TORSIONS},
        "histogramBins": HISTOGRAM_BINS,
        "torsionsSouth": {t: torsion_stats([r[t] for r in b_residues]) for t in TORSIONS},
        "templateB": template(b_steps, "B-form, C2'-endo (south)"),
        "templateA": template(a_steps, "A-form, C3'-endo (north)"),
        "conformers": kmeans_circular(residues, args.clusters),
    }

    with open(args.out, "w") as handle:
        json.dump(result, handle, indent=1, sort_keys=True)
    print(f"wrote {args.out}", file=sys.stderr)


if __name__ == "__main__":
    main()
