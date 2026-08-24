#!/usr/bin/env python3
#
# Copyright 2026 Kazimierz Pogoda / Xemantic
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# `T-321` / `CH-0268` -- the DYNAMIC arm of the argument guard.
#
#     tools/T-321-dynamic-guard-probe.py --self-test
#     tools/T-321-dynamic-guard-probe.py --probe --ref <sha>
#     tools/T-321-dynamic-guard-probe.py --probe --ref <sha> --argument --nonsense
#
# `tools/cli_guard.py --check` reads a SOURCE for a refusal; this reads a RUN.  It exports every
# writer of a given ref into a throwaway `git archive` tree, hands each one an argument it does
# not recognise, and observes what the tree does.  It NEVER touches the checkout.
#
# THREE OBSERVATIONS, NOT ONE.  `CH-0268`'s own probe compared **checksums**, and recorded in its
# own §3 that a tool re-emitting its own file byte-for-byte is invisible to it.  A checksum is the
# wrong instrument for the question *did this tool write*: the cheap one is `st_mtime_ns`, which a
# write moves whether or not the bytes do.  So a run is classified
#
#     CREATED   a path that was not there before
#     DELETED   a path that is not there after
#     CONTENT   a path present in both whose bytes moved   <- all a checksum probe can see
#     TOUCHED   a path present in both whose bytes did NOT move and whose `st_mtime_ns` did
#
# and the fourth class is the blind spot, closed for the price of a `stat`.
#
# The cheap bound runs first (`SESSION-PROMPT.md`): the DETECTOR is a `stat` walk, and a `sha256`
# is taken only of the paths that walk says moved -- so the probe hashes a handful of files rather
# than the tree, once per writer that did something.
#
# The population is `cli_guard.writers()`, DELEGATED rather than re-derived (`C-0195`: a census
# must delegate what its subject is to the census that gates it), so the two arms are censuses of
# ONE population and their readings are comparable.  The predicate is today's and the premise set
# is the ref's, which is the only way to read a repaired defect at the state where it existed.
"""Run every writer with an unrecognised argument in a throwaway tree and observe the writes."""
import hashlib
import importlib.util
import os
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def _load(name, path):
    specification = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(module)
    return module


cli_guard = _load("cli_guard", os.path.join(ROOT, "tools", "cli_guard.py"))

#: What a cold session types first.  `CH-0268`'s own measurement used exactly this.
DEFAULT_ARGUMENT = "--help"

#: A tool that hangs is a tool that wrote nothing, and the one that hangs on the network is
#: `T-161-fetch-sources.py`, whose writes ARE the finding.  Recorded per run, never inferred.
DEFAULT_TIMEOUT_SECONDS = 120

#: An exit code that is not evidence of a failure BEFORE the write.  `0` is a clean run and `2` is
#: this repository's refusal code (`cli_guard.refuse_unknown_arguments` raises `SystemExit(2)`).
NOT_A_FAILURE = (0, 2)

#: THE OBSERVER'S OWN NOISE FLOOR, and it is not small.  CPython byte-compiles every module it
#: imports into `__pycache__/*.pyc`, so a tool that merely IMPORTS a sibling creates files -- and
#: an unfiltered probe reads that as the tool writing.  Measured at `CH-0268`'s own ref, the raw
#: reading is 24 writers of 44; filtered it is 5, which is what that challenge reports.  The
#: exclusion is a statement about the INSTRUMENT, so it is named here and tested, not inlined.
IGNORED = ("__pycache__",)


def ignored(relative_path):
    """Is this path the interpreter's own bytecode cache rather than a tool's output?"""
    parts = relative_path.replace(os.sep, "/").split("/")
    return any(part in IGNORED for part in parts) or relative_path.endswith(".pyc")


def observe(root):
    """The CHEAP observation: `{relative path: (size, st_mtime_ns)}`.

    `.git` and the interpreter's bytecode cache are excluded -- the first because a `git archive`
    tree has none and the second because it is the observer's own footprint.
    """
    reading = {}
    for directory, subdirectories, names in os.walk(root):
        subdirectories[:] = [name for name in subdirectories
                             if name != ".git" and name not in IGNORED]
        for name in names:
            path = os.path.join(directory, name)
            relative = os.path.relpath(path, root)
            if ignored(relative):
                continue
            try:
                status = os.lstat(path)
            except OSError:
                continue
            reading[relative] = (status.st_size, status.st_mtime_ns)
    return reading


def mtime_granularity(directory, samples=4000):
    """The smallest positive `st_mtime_ns` step this filesystem actually resolves, in ns.

    THE MTIME INSTRUMENT HAS A FLOOR AND IT IS NOT ONE NANOSECOND.  Two writes closer together
    than one tick share a timestamp, so a byte-identical rewrite inside a tick is invisible to
    this observation exactly as it is to a checksum.  The floor is a property of the box, so it
    is MEASURED rather than assumed, and it is quotable beside any reading this probe makes.
    """
    path = os.path.join(directory, ".mtime-granularity-probe")
    stamps = []
    for _ in range(samples):
        with open(path, "w", encoding="utf-8") as handle:
            handle.write("x")
        stamps.append(os.lstat(path).st_mtime_ns)
    os.unlink(path)
    steps = [later - earlier for earlier, later in zip(stamps, stamps[1:]) if later > earlier]
    return min(steps) if steps else None


#: What to assume when the filesystem will not name its own resolution.  A self-test suite that
#: CRASHES on a bad value reports that as a survivor rather than as a kill (`T-306`), so the
#: fallback is what keeps the named test above the thing that fails.
FALLBACK_GRANULARITY_NS = 1_000_000


def resolvable_granularity(measured):
    """`measured`, or the fallback when the filesystem would not name one."""
    return measured or FALLBACK_GRANULARITY_NS


def interpreter_startup_seconds(samples=5):
    """How long a probed run takes to get going -- the interval the mtime floor must beat.

    A tool cannot write before its interpreter has started, so the smallest interval this probe
    ever has to resolve is a bare `python3 -c pass`.  The margin between that and
    `mtime_granularity` is what says the floor does not bite here.
    """
    import time
    started = time.monotonic()
    for _ in range(samples):
        subprocess.run([sys.executable, "-c", "pass"], stdout=subprocess.DEVNULL,
                       stderr=subprocess.DEVNULL, check=False)
    return (time.monotonic() - started) / samples


def moved_paths(before, after):
    """Every path whose presence, size or `st_mtime_ns` differs -- the detector's whole output.

    A byte-identical rewrite lands here, which is the half a checksum comparison never sees.
    """
    return sorted(path for path in set(before) | set(after)
                  if before.get(path) != after.get(path))


def digest(root, relative_paths):
    """`sha256` of each named path that exists and can be read."""
    digests = {}
    for relative in sorted(relative_paths):
        path = os.path.join(root, relative)
        if not os.path.isfile(path):
            continue
        try:
            with open(path, "rb") as handle:
                digests[relative] = hashlib.sha256(handle.read()).hexdigest()
        except OSError:
            digests[relative] = "UNREADABLE"
    return digests


def partition(moved, before_digest, after_digest):
    """Split the detector's output into created / deleted / content / touched.

    `before_digest` and `after_digest` carry only the moved paths -- the point of taking the
    cheap observation first.  A path missing from a side is a path that was not there.
    """
    both = [path for path in moved if path in before_digest and path in after_digest]
    return {
        "created": sorted(path for path in moved
                          if path not in before_digest and path in after_digest),
        "deleted": sorted(path for path in moved
                          if path in before_digest and path not in after_digest),
        "content": sorted(path for path in both
                          if before_digest[path] != after_digest[path]),
        "touched": sorted(path for path in both
                          if before_digest[path] == after_digest[path]),
    }


def wrote(record):
    """Did this run write at all, under ANY of the observations?"""
    return bool(record["created"] or record["content"]
                or record["touched"] or record["deleted"])


def wrote_by_checksum(record):
    """Did this run write, under the checksum-only observation `CH-0268`'s probe could make?"""
    return bool(record["created"] or record["content"] or record["deleted"])


def failed_before_writing(record):
    """Wrote nothing, and not because it refused -- a SECOND way the dynamic arm under-reports.

    There is NO separate timeout clause here, and its absence is a measurement rather than an
    oversight: a run that timed out has no exit code at all (`run_one` records `None`), which is
    already outside `NOT_A_FAILURE`.  Written with `record["timedOut"] or ...` in front, the
    disjunct failed no named test under mutation -- a DUPLICATED rule, invisible to a mutation of
    either copy (`C-0179`).  The invariant it stood for is asserted directly instead.
    """
    return not wrote(record) and record["exitCode"] not in NOT_A_FAILURE


def summarise(records):
    """The readings this task exists to compare, and the two reasons they differ."""
    return {
        "wroteByChecksum": sorted(r["tool"] for r in records if wrote_by_checksum(r)),
        "wroteUnderAnyObservation": sorted(r["tool"] for r in records if wrote(r)),
        "wroteByMtimeOnly": sorted(r["tool"] for r in records
                                   if wrote(r) and not wrote_by_checksum(r)),
        "silentBecauseItFailedFirst": sorted(r["tool"] for r in records
                                             if failed_before_writing(r)),
    }


def resolve(ref, root=ROOT):
    return subprocess.run(["git", "-C", root, "rev-parse", ref],
                          stdout=subprocess.PIPE, check=True).stdout.decode().strip()


def export(ref, destination, root=ROOT):
    """`git archive <ref>` into `destination`.  Tracked files only, and no `.git`."""
    os.makedirs(destination, exist_ok=True)
    archive = subprocess.run(["git", "-C", root, "archive", ref],
                             stdout=subprocess.PIPE, check=True)
    subprocess.run(["tar", "-x", "-C", destination], input=archive.stdout, check=True)
    return destination


def run_one(tree, name, argument, timeout=DEFAULT_TIMEOUT_SECONDS):
    """Run one writer with `argument` inside `tree`; return its record, unhashed.

    The record's four classes are filled in by `probe`, which alone can restore the tree and so
    read the BEFORE bytes.  Here only the cheap observation is taken.
    """
    before = observe(tree)
    completed, timed_out = None, False
    try:
        completed = subprocess.run(
            [sys.executable, os.path.join("tools", name), argument],
            cwd=tree, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout)
    except subprocess.TimeoutExpired:
        timed_out = True
    after = observe(tree)
    return {
        "tool": name,
        "argument": argument,
        "timedOut": timed_out,
        "exitCode": None if completed is None else completed.returncode,
        "stderrTail": "" if completed is None
                      else completed.stderr.decode("utf-8", "replace").strip()[-240:],
        "movedPaths": moved_paths(before, after),
        "created": [], "deleted": [], "content": [], "touched": [],
    }


def probe(ref, argument=DEFAULT_ARGUMENT, tools=None, timeout=DEFAULT_TIMEOUT_SECONDS,
          root=ROOT, report=None):
    """Probe every writer of `ref`, each against a freshly restored tree.

    Returns `(resolved sha, [record, ...])`.  A record carries the four classes, the exit code
    and whether the run timed out -- so a tool that wrote nothing because it DIED first is
    distinguishable from one that wrote nothing because it refused.
    """
    resolved = resolve(ref, root)
    workspace = tempfile.mkdtemp(prefix="T-321-probe.")
    tree = os.path.join(workspace, "tree")
    records = []
    try:
        export(resolved, tree, root)
        names = list(tools) if tools is not None else cli_guard.writers(tree)
        for index, name in enumerate(names, start=1):
            if not os.path.isfile(os.path.join(tree, "tools", name)):
                continue
            if report is not None:
                report("[{}/{}] {}".format(index, len(names), name))
            record = run_one(tree, name, argument, timeout)
            if record["movedPaths"]:
                after_digest = digest(tree, record["movedPaths"])
                subprocess.run(["rm", "-rf", tree], check=True)
                export(resolved, tree, root)
                before_digest = digest(tree, record["movedPaths"])
                record.update(partition(record["movedPaths"], before_digest, after_digest))
            records.append(record)
    finally:
        subprocess.run(["rm", "-rf", workspace], check=False)
    return resolved, records


def _self_test():
    checks = []

    def ok(name, condition):
        checks.append((name, bool(condition)))

    # --- the detector -------------------------------------------------------------------------
    ok("an untouched tree moves no path",
       moved_paths({"a": (1, 1)}, {"a": (1, 1)}) == [])
    ok("a new path moves", moved_paths({}, {"a": (1, 1)}) == ["a"])
    ok("a removed path moves", moved_paths({"a": (1, 1)}, {}) == ["a"])
    ok("a size change moves", moved_paths({"a": (1, 9)}, {"a": (2, 9)}) == ["a"])
    ok("an mtime change at an UNCHANGED size moves -- the blind spot, in the detector",
       moved_paths({"a": (7, 1)}, {"a": (7, 5)}) == ["a"])
    ok("the detector is sorted and deduplicated",
       moved_paths({"b": (1, 1), "a": (1, 1)}, {"b": (2, 2), "a": (2, 2)}) == ["a", "b"])

    # --- the classifier -----------------------------------------------------------------------
    ok("a path in neither digest is neither created nor deleted",
       partition(["a"], {}, {}) == {"created": [], "deleted": [], "content": [], "touched": []})
    ok("a path only after is CREATED",
       partition(["a"], {}, {"a": "x"})["created"] == ["a"])
    ok("a path only before is DELETED",
       partition(["a"], {"a": "x"}, {})["deleted"] == ["a"])
    ok("a path whose bytes moved is CONTENT",
       partition(["a"], {"a": "x"}, {"a": "y"})["content"] == ["a"])
    ok("a byte-identical rewrite is TOUCHED and NOT content",
       partition(["a"], {"a": "x"}, {"a": "x"}) ==
       {"created": [], "deleted": [], "content": [], "touched": ["a"]})
    ok("an unreadable file where a readable one stood is CONTENT, not silently dropped",
       partition(["a"], {"a": "x"}, {"a": "UNREADABLE"})["content"] == ["a"])

    # --- the two readings ---------------------------------------------------------------------
    empty = {"created": [], "deleted": [], "content": [], "touched": [],
             "timedOut": False, "exitCode": 0}
    ok("an untouched tree is no write under either observation",
       not wrote(dict(empty)) and not wrote_by_checksum(dict(empty)))
    ok("CREATED is a write under both observations",
       wrote(dict(empty, created=["n"])) and wrote_by_checksum(dict(empty, created=["n"])))
    ok("DELETED is a write under both observations",
       wrote(dict(empty, deleted=["n"])) and wrote_by_checksum(dict(empty, deleted=["n"])))
    ok("CONTENT is a write under both observations",
       wrote(dict(empty, content=["n"])) and wrote_by_checksum(dict(empty, content=["n"])))
    ok("TOUCHED IS a write", wrote(dict(empty, touched=["n"])))
    ok("and TOUCHED is exactly what a checksum comparison cannot see -- CH-0268 section 3",
       not wrote_by_checksum(dict(empty, touched=["n"])))

    # --- the second under-report ----------------------------------------------------------------
    ok("a tool that wrote nothing because it CRASHED did not refuse",
       failed_before_writing(dict(empty, exitCode=1)))
    ok("a refusal (exit 2) is not a silent failure",
       not failed_before_writing(dict(empty, exitCode=2)))
    ok("a clean exit that wrote nothing is not a silent failure",
       not failed_before_writing(dict(empty, exitCode=0)))
    ok("a TIMEOUT is a silent failure, not a refusal",
       failed_before_writing(dict(empty, timedOut=True, exitCode=None)))
    ok("a tool that crashed AFTER writing is not counted as silent",
       not failed_before_writing(dict(empty, exitCode=1, content=["n"])))

    ok("summarise separates the checksum reading from the mtime one",
       summarise([dict(empty, tool="c", created=["n"]), dict(empty, tool="t", touched=["n"])])
       == {"wroteByChecksum": ["c"], "wroteUnderAnyObservation": ["c", "t"],
           "wroteByMtimeOnly": ["t"], "silentBecauseItFailedFirst": []})

    # --- delegation and the real filesystem -----------------------------------------------------
    ok("the population is DELEGATED to the gate's own census, not re-derived here",
       "writers" not in globals() and callable(cli_guard.writers))
    ok("and the delegated population is non-empty on this tree", len(cli_guard.writers()) > 0)
    ok("observe skips .git",
       not any(path.startswith(".git") for path in observe(os.path.join(ROOT, "tools"))))
    ok("a bytecode cache entry is the OBSERVER's footprint, not a write",
       ignored("tools/__pycache__/x.cpython-312.pyc"))
    ok("a .pyc anywhere is the observer's footprint",
       ignored("x.pyc") and ignored("a/b/x.pyc"))
    ok("anything INSIDE a bytecode cache directory is excluded, .pyc or not",
       ignored("tools/__pycache__/notes.txt"))
    ok("a real result file is NOT excluded",
       not ignored("gpd/results/T-194-one-reserve.json"))
    ok("a path merely CONTAINING the word is not excluded",
       not ignored("tools/__pycache__-notes.md"))

    import time
    with tempfile.TemporaryDirectory() as scratch:
        granularity = mtime_granularity(scratch)
        ok("the filesystem resolves SOME positive st_mtime_ns step", granularity is not None)
        ok("an unresolvable granularity falls back rather than stopping the suite",
           resolvable_granularity(None) == FALLBACK_GRANULARITY_NS)
        ok("and a measured granularity is used as measured",
           resolvable_granularity(4242) == 4242)
        # A suite that CRASHES on a bad value reports that as a survivor, not as a kill
        # (`T-306`): carry a fallback so the named test above is what fails, and the rest run.
        granularity = resolvable_granularity(granularity)
        path = os.path.join(scratch, "f.txt")
        with open(path, "w", encoding="utf-8") as handle:
            handle.write("one")
        first = observe(scratch)
        time.sleep(2.0 * granularity / 1e9)
        with open(path, "w", encoding="utf-8") as handle:
            handle.write("one")
        second = observe(scratch)
        ok("a REAL byte-identical rewrite, one tick later, moves st_mtime_ns",
           first["f.txt"][1] != second["f.txt"][1])
        moved = moved_paths(first, second)
        ok("and the detector sees it", moved == ["f.txt"])
        ok("and it classifies as TOUCHED against the real filesystem",
           partition(moved, {"f.txt": "same"}, {"f.txt": "same"})["touched"] == ["f.txt"])

        # THE FLOOR, ASSERTED IN BOTH DIRECTIONS.  Rewrites inside one tick are invisible to the
        # mtime observation -- so the instrument is NOT exact, and a named test says so rather
        # than letting a lucky tick boundary hide it (which is how this test first passed, and
        # then failed on the next run with nothing changed).  Asserted over a run of rewrites,
        # because whether any ONE pair straddles a tick is a coin toss and a test may not be.
        stamps = []
        for _ in range(200):
            with open(path, "w", encoding="utf-8") as handle:
                handle.write("one")
            stamps.append(os.lstat(path).st_mtime_ns)
        ok("rewrites inside one tick are INVISIBLE to mtime -- the floor is real, not notional",
           len(set(stamps)) < len(stamps))

        startup = interpreter_startup_seconds()
        ok("and the floor is far below the smallest interval this probe must resolve, "
           "which is one interpreter startup",
           granularity / 1e9 < startup / 5.0)

        with open(os.path.join(scratch, "g.txt"), "w", encoding="utf-8") as handle:
            handle.write("two")
        ok("digest reads a real file and skips one that is not there",
           set(digest(scratch, ["g.txt", "absent.txt"])) == {"g.txt"})

    # BEHAVIOURAL, not textual.  Written as `"refuse_unknown_arguments" in <own source>` this
    # test survived a mutation that deleted the CALL, because the name still occurs in the
    # comments above it -- `C-0179`'s *a test can be satisfied by the name of the thing it
    # protects*, in the one file whose subject is exactly that trap.
    refusal = subprocess.run([sys.executable, os.path.abspath(__file__), "--nonsense"],
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    ok("this probe REFUSES an argument it does not recognise, like the tools it probes",
       refusal.returncode == 2)
    ok("and it says so on stderr rather than emitting anything",
       b"unrecognised argument" in refusal.stderr and refusal.stdout == b"")
    bare = subprocess.run([sys.executable, os.path.abspath(__file__)],
                          stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    ok("and a bare invocation prints usage and refuses too, rather than probing something",
       bare.returncode == 2 and b"usage:" in bare.stderr)

    for name, passed in checks:
        print("{}  {}".format("ok  " if passed else "FAIL", name))
    failed = [name for name, passed in checks if not passed]
    print("# {} self-test(s), {} failure(s)".format(len(checks), len(failed)))
    return 1 if failed else 0


def _main(argv):
    ref, argument, timeout = "HEAD", DEFAULT_ARGUMENT, DEFAULT_TIMEOUT_SECONDS
    rest = list(argv)
    while rest:
        flag = rest.pop(0)
        if flag == "--ref":
            ref = rest.pop(0)
        elif flag == "--argument":
            argument = rest.pop(0)
        elif flag == "--timeout":
            timeout = int(rest.pop(0))
    resolved, records = probe(ref, argument, timeout=timeout,
                              report=lambda line: print(line, file=sys.stderr))
    reading = summarise(records)
    for record in records:
        print("TOOL   tools/{:38s} exit={:>4s} timedOut={:d} created={} content={} "
              "touched={} deleted={}".format(
                  record["tool"], str(record["exitCode"]), record["timedOut"],
                  len(record["created"]), len(record["content"]),
                  len(record["touched"]), len(record["deleted"])))
    for record in records:
        if wrote(record):
            print("WROTE  tools/{}  created={} content={} touched={} deleted={}".format(
                record["tool"], len(record["created"]), len(record["content"]),
                len(record["touched"]), len(record["deleted"])))
            for kind in ("created", "content", "touched", "deleted"):
                for path in record[kind]:
                    print("       {:8s} {}".format(kind.upper(), path))
    for tool in reading["silentBecauseItFailedFirst"]:
        record = next(r for r in records if r["tool"] == tool)
        print("SILENT tools/{}  exit={} timedOut={}  -- wrote nothing, and did not refuse".format(
            tool, record["exitCode"], record["timedOut"]))
    print("# ref {} ({}), argument {!r}".format(ref, resolved, argument))
    print("# {} writer(s) probed; {} wrote under a CHECKSUM observation; {} wrote under ANY "
          "observation; {} were invisible to a checksum".format(
              len(records), len(reading["wroteByChecksum"]),
              len(reading["wroteUnderAnyObservation"]), len(reading["wroteByMtimeOnly"])))
    print("# {} wrote nothing because the run FAILED before it could".format(
        len(reading["silentBecauseItFailedFirst"])))
    return 1 if reading["wroteUnderAnyObservation"] else 0


if __name__ == "__main__":
    cli_guard.refuse_unknown_arguments(
        "tools/T-321-dynamic-guard-probe.py [--self-test | --probe [--ref R] "
        "[--argument A] [--timeout S]]",
        recognised=("--self-test", "--probe", "--ref", "--argument", "--timeout"),
        allow_positional=True)
    if "--self-test" in sys.argv[1:]:
        raise SystemExit(_self_test())
    if "--probe" in sys.argv[1:]:
        raise SystemExit(_main([a for a in sys.argv[1:] if a != "--probe"]))
    sys.stderr.write(
        "usage: tools/T-321-dynamic-guard-probe.py [--self-test | --probe [--ref R]]\n")
    raise SystemExit(2)
