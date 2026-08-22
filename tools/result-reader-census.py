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
# A mechanical reader census of gpd/results/ (task P-22).
#
#     tools/result-reader-census.py                  # human-readable census
#     tools/result-reader-census.py --file T-1d      # who reads one result file
#     tools/result-reader-census.py --emit           # write gpd/results/P-22-*.json
#     tools/result-reader-census.py --check          # gates; exit 1 on failure
#
# Why this exists.  `C-0073` audited `P-18`'s rounding propagation with
# `grep 'File("gpd/results/'` and concluded that it closed at one reader of `T-1d`.  There are
# three.  The two it missed run through `window/ResynthesisInputs.kt`, which builds its paths
# as `File(directory, "T-1d-scf-density-profile.json")` while its callers pass the *directory*:
# a search for the literal filename finds a call site that names no directory, and a search for
# the literal directory finds call sites that name no file.  Neither finds the edge, and two
# result files stood for an iteration built from inputs that had already been re-emitted
# underneath them (`CH-0092`).
#
# So the census is DERIVED, not grepped.  It is a static analysis of the Kotlin sources:
#
#   * comments are stripped first, because every study announces its own output in a KDoc line
#     and twenty-odd of them discuss other studies' files in prose;
#   * a result file counts as *referenced* only when its name is a string literal in the
#     argument list of a `File(...)` construction -- syntactic position, never substring, so a
#     `findings` string saying "read from gpd/results/T-130-*.json" is not a read;
#   * a reference is a WRITE when the constructed file is written (directly, or through a local
#     `val` that is), and a READ otherwise;
#   * reads propagate along a reference graph over top-level DECLARATIONS, so a study inherits
#     the reads of every declaration it can reach -- which is the edge a grep cannot see.
#
# The unit is a declaration and not a file, and that is not a refinement, it is the difference
# between a census and noise: at file granularity `DesignWindowStudy` -- which reads three
# result files -- comes out reading thirteen, because package `window` declares `ledger`,
# `array`, `reader` and `scalar` privately in several files at once.  Kotlin resolves a name in
# its own file first and a `private` top-level declaration is invisible outside it; both rules
# are applied here, and the file-level version of this script failed its own declared falsifier.
#
# `directReads` and `transitiveReads` are reported separately so the inclusion can be audited.
# The grep is a strict LOWER bound -- it finds every edge whose path is one literal -- and the
# self-test asserts the census is a superset of it.  On the tree this was written against the
# grep finds 41 of 61 read edges; the other 20 are assembled from a directory and a name.
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from emission_header import with_emission_header  # noqa: E402

# A result file is `T-<id>-<slug>.json` or `P-<id>-<slug>.json`, optionally prefixed by the
# directory.  A `*` cannot appear, which is what keeps `gpd/results/T-130-*.json` out.
RESULT_DIRECTORY = "gpd/results"
RESULT_FILE = re.compile(r"^[TP]-[0-9]+[a-z]?-[a-z0-9]+(?:-[a-z0-9]+)*\.json$")

SOURCE_ROOTS = (
    os.path.join("src", "main", "kotlin"),
    os.path.join("src", "test", "kotlin"),
)

CENSUS_FILE = os.path.join(RESULT_DIRECTORY, "P-22-result-reader-census.json")


# --- lexing ---------------------------------------------------------------------------------


def strip_comments(text):
    """Kotlin source with `//` and (nesting) `/* */` comments removed, string literals kept."""
    out = []
    i = 0
    n = len(text)
    depth = 0
    while i < n:
        if depth:
            if text.startswith("/*", i):
                depth += 1
                i += 2
            elif text.startswith("*/", i):
                depth -= 1
                i += 2
            else:
                if text[i] == "\n":
                    out.append("\n")
                i += 1
            continue
        if text.startswith("/*", i):
            depth = 1
            i += 2
        elif text.startswith("//", i):
            while i < n and text[i] != "\n":
                i += 1
        elif text.startswith('"""', i):
            end = text.find('"""', i + 3)
            end = n if end < 0 else end + 3
            out.append(text[i:end])
            i = end
        elif text[i] == '"':
            j = i + 1
            while j < n:
                if text[j] == "\\":
                    j += 2
                    continue
                if text[j] == '"' or text[j] == "\n":
                    j += 1
                    break
                j += 1
            out.append(text[i:j])
            i = j
        else:
            out.append(text[i])
            i += 1
    joined = "".join(out)
    return "\n".join(line.rstrip() for line in joined.split("\n"))


def _string_spans(text):
    """(start, end, value) of every string literal in already comment-stripped text."""
    spans = []
    i = 0
    n = len(text)
    while i < n:
        if text.startswith('"""', i):
            end = text.find('"""', i + 3)
            end = n if end < 0 else end + 3
            spans.append((i, end, text[i + 3:max(i + 3, end - 3)]))
            i = end
        elif text[i] == '"':
            j = i + 1
            chunk = []
            while j < n:
                if text[j] == "\\":
                    chunk.append(text[j:j + 2])
                    j += 2
                    continue
                if text[j] == '"':
                    j += 1
                    break
                if text[j] == "\n":
                    break
                chunk.append(text[j])
                j += 1
            spans.append((i, j, "".join(chunk)))
            i = j
        else:
            i += 1
    return spans


def code_only(text):
    """Comment-stripped source with every string literal blanked, for identifier scanning."""
    stripped = strip_comments(text)
    out = list(stripped)
    for start, end, _ in _string_spans(stripped):
        for k in range(start, end):
            if out[k] != "\n":
                out[k] = " "
    return "".join(out)


def as_result_file(literal):
    """The basename if this string literal names a result file, else None."""
    name = literal
    if name.startswith(RESULT_DIRECTORY + "/"):
        name = name[len(RESULT_DIRECTORY) + 1:]
    if "/" in name or not RESULT_FILE.match(name):
        return None
    return name


def task_id(basename):
    """`T-1d-scf-density-profile.json` -> `T-1d`."""
    parts = basename.split("-")
    return "-".join(parts[:2]) if len(parts) >= 2 else basename


# --- File(...) construction sites -------------------------------------------------------------


def _file_calls(stripped):
    """(open_index, close_index, [literal, ...]) for every `File(...)` in stripped source."""
    calls = []
    for match in re.finditer(r"(?<![A-Za-z0-9_.])File\s*\(", stripped):
        start = match.end() - 1
        depth = 0
        i = start
        n = len(stripped)
        while i < n:
            character = stripped[i]
            if character == '"':
                spans = _string_spans(stripped[i:i + 4096])
                if spans and spans[0][0] == 0:
                    i += spans[0][1]
                    continue
            if character == "(":
                depth += 1
            elif character == ")":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        if i >= n:
            continue
        arguments = stripped[start + 1:i]
        literals = [value for _, _, value in _string_spans(arguments)]
        calls.append((match.start(), i, literals))
    return calls


def file_literals(text):
    """Result-file basenames constructed as a `File(...)` in this source, in order."""
    stripped = strip_comments(text)
    seen = []
    for _, _, literals in _file_calls(stripped):
        for literal in literals:
            name = as_result_file(literal)
            if name is not None and name not in seen:
                seen.append(name)
    return seen


_PRIVATE = re.compile(r"^(?:@\w+(?:\([^)\n]*\))?\s*)*private\b")

_BINDING = re.compile(
    r"(?:^|[\n{;])\s*(?:@\w+\s+)*(?:private\s+|internal\s+|public\s+)?(?:const\s+)?"
    r"(?:val|var)\s+([A-Za-z_][A-Za-z0-9_]*)\s*(?::\s*File\s*)?=\s*$"
)


def written_literals(text):
    """Result-file basenames this source WRITES: `File(x).writeText`, or a val that is."""
    stripped = strip_comments(text)
    written = set()
    for start, close, literals in _file_calls(stripped):
        names = [n for n in (as_result_file(v) for v in literals) if n]
        if not names:
            continue
        tail = stripped[close + 1:close + 40]
        direct = re.match(r"\s*\.\s*write", tail) is not None
        binding = _BINDING.search(stripped[max(0, start - 120):start])
        through_val = False
        if binding:
            variable = binding.group(1)
            through_val = (
                re.search(r"(?<![A-Za-z0-9_])%s\s*\.\s*write" % re.escape(variable), stripped)
                is not None
            )
        if direct or through_val:
            written.update(names)
    return written


#: `{property: result file name}` of `structure/ResultInputs.kt`, the TYPED INPUT HANDLES of
#: `T-272`'s `P2`.  A study that declares `ResultInputs.T_3B` reads `T-3b-…json`, and this is the
#: derivation reading that declaration.  It is deliberately NOT recomputed from the file name: the
#: registry owns the collision rule (`T-119` names two files), and a second implementation of it
#: here would drift from the first at the next collision.
HANDLE_REGISTRY = "structure/ResultInputs.kt"

_HANDLE = re.compile(r'val ([A-Z0-9_]+): ResultInput = ResultInput\("[^"]+", "([^"]+)"\)')

_HANDLE_REFERENCE = re.compile(r"(?<![A-Za-z0-9_.])ResultInputs\.([A-Z][A-Z0-9_]*)")


def handle_table(sources):
    """`{property: result file name}` read out of the registry among `sources`, or `{}`."""
    for path, text in sources.items():
        if _relative(path) == HANDLE_REGISTRY:
            return {prop: name for prop, name in _HANDLE.findall(strip_comments(text))}
    return {}


def handle_literals(text, handles):
    """Result-file basenames this source reads through a typed handle."""
    stripped = code_only(strip_comments(text))
    return {handles[prop] for prop in _HANDLE_REFERENCE.findall(stripped) if prop in handles}


def read_literals(text, handles=None):
    """Result-file basenames this source READS -- every reference that is not its own output.

    Two shapes, and `T-272` added the second: a `File(...)` literal, and a typed
    `ResultInputs.T_3B` handle.  Both are subtracted against the source's own writes, because a
    study that reads its own output back would otherwise constrain the topological sort against
    itself.
    """
    literals = set(file_literals(text))
    if handles:
        literals |= handle_literals(text, handles)
    return literals - written_literals(text)


def has_main(text):
    """Whether this source declares a top-level `fun main(` -- i.e. whether it is a study."""
    return (
        re.search(r"(?:^|\n)(?:@\w+\s*)*(?:suspend\s+)?fun\s+main\s*\(", strip_comments(text))
        is not None
    )


# --- declarations, packages, references --------------------------------------------------------

_DECLARATION = re.compile(
    r"^(?:@\w+(?:\([^)\n]*\))?\s*)*"
    r"(?:public\s+|private\s+|internal\s+)?"
    r"(?:expect\s+|actual\s+)?"
    r"(?:(?:data|sealed|enum|annotation|value|abstract|open|inline|external|const|lateinit|"
    r"tailrec|operator|infix|suspend)\s+)*"
    r"(?:class|interface|object|fun|val|var|typealias)\s+"
    r"(?:<[^>\n]*>\s*)?"
    r"(?:[A-Za-z_][A-Za-z0-9_.<>?]*\.)?"
    r"([A-Za-z_][A-Za-z0-9_]*)",
    re.MULTILINE,
)


def top_level_declarations(text):
    """Names declared at column zero -- the symbols another file in the package can reference."""
    code = code_only(text)
    return {match.group(1) for match in _DECLARATION.finditer(code)}


def declaration_blocks(text):
    """[(name, source)] -- each top-level declaration and the text that belongs to it.

    A block runs from its own declaration keyword to the next column-zero declaration, which is
    enough to attribute a `File(...)` to the declaration that constructs it without matching a
    single brace.  **Attribution at this granularity is what keeps the census honest.**  At FILE
    granularity every source in package `window` inherits every other's inputs through one
    shared constant, and `DesignWindowStudy` -- which reads three result files -- comes out
    reading thirteen.  That was the declared falsifier of the file-level approach and it fired.
    """
    code = code_only(text)
    stripped = strip_comments(text)
    starts = [(match.start(), match.group(1)) for match in _DECLARATION.finditer(code)]
    blocks = []
    for index, (start, name) in enumerate(starts):
        end = starts[index + 1][0] if index + 1 < len(starts) else len(stripped)
        blocks.append((name, stripped[start:end]))
    return blocks


def package_of(text):
    match = re.search(r"^package\s+([\w.]+)", code_only(text), re.MULTILINE)
    return match.group(1) if match else ""


def imports_of(text):
    """(fully qualified names, wildcard packages) this source imports."""
    names = set()
    wildcards = set()
    for match in re.finditer(r"^import\s+([\w.]+)(\.\*)?", code_only(text), re.MULTILINE):
        if match.group(2):
            wildcards.add(match.group(1))
        else:
            names.add(match.group(1))
    return names, wildcards


def identifiers(text):
    return set(re.findall(r"[A-Za-z_][A-Za-z0-9_]*", code_only(text)))


# --- declared sources ---------------------------------------------------------------------------


def declared_sources(text):
    """The task ids a study declares in its `"sources"` parameter, or None if it declares none."""
    stripped = strip_comments(text)
    match = re.search(r'"sources"\s*to\s*', stripped)
    if not match:
        return None
    i = match.end()
    n = len(stripped)
    pieces = []
    # `"sources" to ("a, " + "b")` is as legal as `"sources" to "a, " + "b"`, and the parenthesised
    # form is what `T-189` wrote. Without this the walk below breaks on the `(` with `pieces` empty
    # and the study reads as declaring NOTHING -- which is the costly direction, because a study
    # that declares nothing is silently exempt from the declaration check it was meant to fail.
    # Same shape as `CH-0092`: an audit is only as complete as the SHAPE of the search performing it.
    depth = 0
    while i < n and stripped[i] in " \t\r\n(":
        if stripped[i] == "(":
            depth += 1
        i += 1
    while i < n:
        while i < n and stripped[i] in " \t\r\n":
            i += 1
        if i < n and stripped[i] == '"':
            spans = _string_spans(stripped[i:])
            if not spans or spans[0][0] != 0:
                break
            pieces.append(spans[0][2])
            i += spans[0][1]
        else:
            break
        while i < n and stripped[i] in " \t\r\n":
            i += 1
        if i < n and stripped[i] == "+":
            i += 1
            continue
        break
    declared = set()
    for token in "".join(pieces).split(","):
        token = token.strip()
        if not token:
            continue
        if token.startswith(RESULT_DIRECTORY + "/"):
            token = token[len(RESULT_DIRECTORY) + 1:]
        declared.add(task_id(token) if token.endswith(".json") else token)
    return declared


# --- the census ------------------------------------------------------------------------------


def _relative(path):
    for root in SOURCE_ROOTS:
        marker = root.replace(os.sep, "/") + "/"
        if marker in path.replace(os.sep, "/"):
            return path.replace(os.sep, "/").split(marker, 1)[1]
    return path.replace(os.sep, "/")


def build_census(sources):
    """The reader graph over a {path: source text} mapping.

    Keys of the returned `studies` map are paths relative to the source root.
    """
    files = {}
    blocks = []
    handles = handle_table(sources)
    for path, text in sorted(sources.items()):
        key = _relative(path)
        # The REGISTRY is not a reader.  Its own `all` list names every handle, so resolving them
        # there would make every study that so much as mentions `ResultInputs` inherit all 151
        # reads -- the exact over-inclusion this census's declaration granularity exists to avoid.
        block_handles = {} if key == HANDLE_REGISTRY else handles
        is_test = "/test/" in path.replace(os.sep, "/")
        own = []
        for name, body in declaration_blocks(text):
            own.append(len(blocks))
            blocks.append(
                {
                    "file": key,
                    "name": name,
                    "reads": read_literals(body, block_handles),
                    "writes": written_literals(body),
                    "identifiers": identifiers(body),
                    "isPrivate": _PRIVATE.match(body) is not None,
                }
            )
        files[key] = {
            "path": key,
            "isTest": is_test,
            "package": package_of(text),
            "imports": imports_of(text),
            "blockIndices": own,
            "isStudy": has_main(text) and not is_test,
            "declaredSources": declared_sources(text),
        }

    # name -> the declaration blocks that define it, never a study's own `main`
    by_name = {}
    for index, block in enumerate(blocks):
        if block["name"] == "main":
            continue
        by_name.setdefault(block["name"], []).append(index)

    edges = {}
    for index, block in enumerate(blocks):
        source_file = files[block["file"]]
        names, wildcards = source_file["imports"]
        targets = set()
        for identifier in block["identifiers"]:
            candidates = by_name.get(identifier, ())
            # Kotlin resolves a name in the declaring FILE first, and a `private` top-level
            # declaration is not visible outside it at all.  Both matter here: `ledger`,
            # `array`, `reader` and `scalar` are each declared privately in several files of
            # package `window`, and without this rule `DesignWindowStudy` -- which reads three
            # result files -- inherits thirteen through a name collision with a sibling study.
            local = [
                candidate
                for candidate in candidates
                if blocks[candidate]["file"] == block["file"]
            ]
            if local:
                targets.update(candidate for candidate in local if candidate != index)
                continue
            for candidate in candidates:
                target = blocks[candidate]
                target_file = files[target["file"]]
                if target_file["isTest"] or target["isPrivate"]:
                    continue
                visible = (
                    target_file["package"] == source_file["package"]
                    or target_file["package"] in wildcards
                    or (target_file["package"] + "." + identifier) in names
                )
                if visible:
                    targets.add(candidate)
        edges[index] = targets

    def closure(starts):
        seen = set(starts)
        stack = list(starts)
        while stack:
            for neighbour in edges.get(stack.pop(), ()):
                if neighbour not in seen:
                    seen.add(neighbour)
                    stack.append(neighbour)
        return seen

    def record_for(key, starts):
        reached = closure(starts)
        direct = set()
        transitive = set()
        writes = set()
        for index in reached:
            block = blocks[index]
            if block["file"] == key:
                direct |= block["reads"]
                writes |= block["writes"]
            else:
                transitive |= block["reads"]
        transitive -= direct
        declared = files[key]["declaredSources"]
        return {
            "reads": sorted(direct | transitive),
            "directReads": sorted(direct),
            "transitiveReads": sorted(transitive),
            "writes": sorted(writes)[0] if writes else None,
            "declaredSources": sorted(declared) if declared is not None else None,
            "reaches": sorted(
                {
                    blocks[index]["file"]
                    for index in reached
                    if blocks[index]["file"] != key and blocks[index]["reads"]
                }
            ),
            "reachesFiles": sorted(
                {blocks[index]["file"] for index in reached if blocks[index]["file"] != key}
            ),
        }

    studies = {}
    helpers = {}
    tests = {}
    for key, node in files.items():
        if node["isStudy"]:
            starts = [i for i in node["blockIndices"] if blocks[i]["name"] == "main"]
            studies[key] = record_for(key, starts)
        elif node["isTest"]:
            record = record_for(key, node["blockIndices"])
            if record["reads"]:
                tests[key] = record
        else:
            record = record_for(key, node["blockIndices"])
            if record["directReads"] or record["writes"]:
                helpers[key] = record

    readers = {}
    for key in sorted(studies):
        for name in studies[key]["reads"]:
            readers.setdefault(name, []).append(key)
    test_readers = {}
    for key in sorted(tests):
        for name in tests[key]["reads"]:
            test_readers.setdefault(name, []).append(key)

    written = {}
    for key in sorted(studies) + sorted(helpers):
        record = studies.get(key) or helpers[key]
        if record["writes"]:
            written.setdefault(record["writes"], []).append(key)

    return {
        "studies": dict(sorted(studies.items())),
        "helpers": dict(sorted(helpers.items())),
        "tests": dict(sorted(tests.items())),
        "readersOf": {name: readers[name] for name in sorted(readers)},
        "testReadersOf": {name: test_readers[name] for name in sorted(test_readers)},
        "writtenBy": {name: written[name] for name in sorted(written)},
    }


def census_of_tree(root):
    sources = {}
    for source_root in SOURCE_ROOTS:
        base = os.path.join(root, source_root)
        for directory, _, files in os.walk(base):
            for name in sorted(files):
                if name.endswith(".kt"):
                    path = os.path.join(directory, name)
                    with open(path, encoding="utf-8") as handle:
                        sources[os.path.relpath(path, root)] = handle.read()
    graph = build_census(sources)
    present = sorted(
        name
        for name in os.listdir(os.path.join(root, RESULT_DIRECTORY))
        if name.endswith(".json")
    )
    graph["resultFiles"] = present
    graph["unwrittenResultFiles"] = [
        name for name in present if name not in graph["writtenBy"]
    ]
    graph["missingResultFiles"] = sorted(
        {name for name in graph["readersOf"]} - set(present)
    )
    graph["roundingSites"] = _rounding_sites(graph)
    return graph


def downstream_of(graph, names):
    """Every result file reachable from `names` through the reader graph, transitively.

    This is the question `P-18` asked and `C-0073` answered by hand: if these files are
    re-emitted, what else moves?  A file moves when a study that reads it is re-run, and that
    study's own output then moves, so the answer is a closure and not a list of readers.
    """
    seen = set(names)
    frontier = list(names)
    while frontier:
        current = frontier.pop()
        for reader in graph["readersOf"].get(current, ()):
            produced = graph["studies"][reader]["writes"]
            if produced and produced not in seen:
                seen.add(produced)
                frontier.append(produced)
    return sorted(seen - set(names))


def _rounding_sites(graph):
    """Each `*Rounding*.kt` site, the studies that use it, and everything downstream.

    `P-19` ranks the rounding sites `P-18` measured and did not change.  Its ranking was
    written on `C-0073`'s reader census, which `CH-0092` corrected, so it is re-derived here
    rather than restated: a site's cost is the closure of its own emissions, not its file count.
    """
    sites = {}
    for key, record in graph["studies"].items():
        for reached in record["reachesFiles"]:
            if "Rounding" not in os.path.basename(reached):
                continue
            sites.setdefault(reached, set()).add(key)
    out = []
    for site in sorted(sites):
        studies = sorted(sites[site])
        emits = sorted(
            {graph["studies"][key]["writes"] for key in studies if graph["studies"][key]["writes"]}
        )
        downstream = downstream_of(graph, emits)
        tests = sorted(
            {
                test
                for name in emits + downstream
                for test in graph["testReadersOf"].get(name, ())
            }
        )
        out.append(
            {
                "site": site,
                "studies": studies,
                "emits": emits,
                "downstreamResultFiles": downstream,
                "testsReadingThem": tests,
            }
        )
    return out


# --- gates ---------------------------------------------------------------------------------


def check_declarations(graph):
    """Every study that declares `sources` must declare exactly the task ids it reads."""
    problems = []
    for key, record in sorted(graph["studies"].items()):
        declared = record["declaredSources"]
        if declared is None:
            continue
        read = sorted({task_id(name) for name in record["reads"]})
        if sorted(declared) != read:
            extra = sorted(set(declared) - set(read))
            missing = sorted(set(read) - set(declared))
            problems.append(
                "%s declares %s and reads %s%s%s"
                % (
                    key,
                    sorted(declared),
                    read,
                    ("; declared but not read: %s" % extra) if extra else "",
                    ("; read but not declared: %s" % missing) if missing else "",
                )
            )
    return problems


def check_writes(graph):
    """No result file may be written by two studies."""
    return [
        "%s is written by %s" % (name, writers)
        for name, writers in sorted(graph["writtenBy"].items())
        if len(writers) > 1
    ]


def check_drift(current, baseline):
    """A study the census already knows about must not silently change what it reads."""
    problems = []
    for key, was in sorted(baseline.get("studies", {}).items()):
        now = current.get("studies", {}).get(key)
        if now is None:
            # A study can be absent because it was deleted or because `--drop-file` removed it
            # from a snapshot. Reported by `removed_studies`, never a failure: the check must
            # not turn another agent's diagnostic drop into a verification failure.
            continue
        if now["reads"] != was["reads"]:
            problems.append(
                "%s read %s and now reads %s; re-emit gpd/results/P-22-result-reader-census.json"
                % (key, was["reads"], now["reads"])
            )
        if now["writes"] != was["writes"]:
            problems.append(
                "%s wrote %s and now writes %s" % (key, was["writes"], now["writes"])
            )
    return problems


def new_studies(current, baseline):
    return sorted(set(current.get("studies", {})) - set(baseline.get("studies", {})))


def removed_studies(current, baseline):
    return sorted(set(baseline.get("studies", {})) - set(current.get("studies", {})))


# --- entry point ------------------------------------------------------------------------------


def _emit(root, graph):
    payload = {
        "task": "P-22",
        "claim": "C-0082",
        "what": (
            "The reader graph over gpd/results/, derived from the Kotlin sources rather than "
            "grepped. A read is a result-file string literal in the argument list of a File(...) "
            "construction, propagated along top-level references; comments and prose strings are "
            "not reads. See tools/result-reader-census.py and CH-0092."
        ),
        "studyCount": len(graph["studies"]),
        "helperCount": len(graph["helpers"]),
        "resultFileCount": len(graph.get("resultFiles", [])),
        "declaringStudyCount": sum(
            1 for r in graph["studies"].values() if r["declaredSources"] is not None
        ),
        "transitiveEdgeCount": sum(
            len(r["transitiveReads"]) for r in graph["studies"].values()
        ),
        "directEdgeCount": sum(len(r["directReads"]) for r in graph["studies"].values()),
        "studies": graph["studies"],
        "helpers": graph["helpers"],
        "tests": graph["tests"],
        "readersOf": graph["readersOf"],
        "testReadersOf": graph["testReadersOf"],
        "writtenBy": graph["writtenBy"],
        "resultFiles": graph.get("resultFiles", []),
        "unwrittenResultFiles": graph.get("unwrittenResultFiles", []),
        "missingResultFiles": graph.get("missingResultFiles", []),
        "roundingSites": graph.get("roundingSites", []),
    }
    path = os.path.join(root, CENSUS_FILE)
    with open(path, "w", encoding="utf-8") as handle:
        # `T-272`'s `P3`/`P4`. This census is about the corpus and not about a device, so it is on
        # no crossover lattice and in no regime -- and both are written out, because an omission
        # and a statement of absence read alike in a file and are not the same fact.
        json.dump(with_emission_header(payload, "none"), handle, indent=2, sort_keys=False)
        handle.write("\n")
    return path


def main(argv):
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    if "--root" in argv:
        root = argv[argv.index("--root") + 1]
    graph = census_of_tree(root)

    if "--emit" in argv:
        print("written: %s" % _emit(root, graph))
        return 0

    if "--file" in argv:
        wanted = argv[argv.index("--file") + 1]
        for name in graph["resultFiles"]:
            if name.startswith(wanted):
                print("%s" % name)
                print("  written by : %s" % (graph["writtenBy"].get(name) or ["nothing"]))
                print("  read by    : %s" % (graph["readersOf"].get(name) or ["nothing"]))
                print("  tests       : %s" % (graph["testReadersOf"].get(name) or ["nothing"]))
        return 0

    if "--check" in argv:
        problems = check_declarations(graph) + check_writes(graph)
        baseline_path = os.path.join(root, CENSUS_FILE)
        if os.path.exists(baseline_path):
            with open(baseline_path, encoding="utf-8") as handle:
                baseline = json.load(handle)
            problems += check_drift(graph, baseline)
            for label, moved in (
                ("not in the census", new_studies(graph, baseline)),
                ("in the census but not in the tree", removed_studies(graph, baseline)),
            ):
                if moved:
                    print("note: %d study/studies %s: %s" % (len(moved), label, ", ".join(moved)))
                    print("      run tools/result-reader-census.py --emit and commit the result")
        else:
            problems.append("no census at %s; run --emit" % CENSUS_FILE)
        if problems:
            print("result-reader census: %d problem(s)" % len(problems))
            for problem in problems:
                print("  %s" % problem)
            return 1
        print(
            "result-reader census ok: %d studies, %d declaring, %d direct + %d transitive read "
            "edges" % (
                len(graph["studies"]),
                sum(1 for r in graph["studies"].values() if r["declaredSources"] is not None),
                sum(len(r["directReads"]) for r in graph["studies"].values()),
                sum(len(r["transitiveReads"]) for r in graph["studies"].values()),
            )
        )
        return 0

    print("%d studies, %d helpers, %d result files" % (
        len(graph["studies"]), len(graph["helpers"]), len(graph["resultFiles"])
    ))
    print()
    print("readers, most-read first:")
    for name, readers in sorted(
        graph["readersOf"].items(), key=lambda item: (-len(item[1]), item[0])
    ):
        print("  %-52s %d  %s" % (name, len(readers), ", ".join(readers)))
    if graph["testReadersOf"]:
        print()
        print("result files read by TESTS:")
        for name, readers in sorted(graph["testReadersOf"].items()):
            print("  %-52s %d  %s" % (name, len(readers), ", ".join(readers)))
    if graph["unwrittenResultFiles"]:
        print()
        print("result files no study writes: %s" % ", ".join(graph["unwrittenResultFiles"]))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
