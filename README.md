# plenty-of-room

Evidence corpus from agentic-loop runs against the NDI Gen-1 DNA-origami actuator simulation programme

[<img alt="GitHub Release Date" src="https://img.shields.io/github/release-date/xemantic/plenty-of-room">](https://github.com/xemantic/plenty-of-room/releases)
[<img alt="license" src="https://img.shields.io/github/license/xemantic/plenty-of-room?color=blue">](https://github.com/xemantic/plenty-of-room/blob/main/LICENSE)

[<img alt="GitHub Actions Workflow Status" src="https://img.shields.io/github/actions/workflow/status/xemantic/plenty-of-room/build-main.yml">](https://github.com/xemantic/plenty-of-room/actions/workflows/build-main.yml)
[<img alt="GitHub branch check runs" src="https://img.shields.io/github/check-runs/xemantic/plenty-of-room/main">](https://github.com/xemantic/plenty-of-room/actions/workflows/build-main.yml)
[<img alt="GitHub commits since latest release" src="https://img.shields.io/github/commits-since/xemantic/plenty-of-room/latest">](https://github.com/xemantic/plenty-of-room/commits/main/)
[<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/xemantic/plenty-of-room">](https://github.com/xemantic/plenty-of-room/commits/main/)

[<img alt="GitHub contributors" src="https://img.shields.io/github/contributors/xemantic/plenty-of-room">](https://github.com/xemantic/plenty-of-room/graphs/contributors)
[<img alt="GitHub commit activity" src="https://img.shields.io/github/commit-activity/t/xemantic/plenty-of-room">](https://github.com/xemantic/plenty-of-room/commits/main/)
[<img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/xemantic/plenty-of-room">]()
[<img alt="GitHub Created At" src="https://img.shields.io/github/created-at/xemantic/plenty-of-room">](https://github.com/xemantic/plenty-of-room/commits)
[<img alt="kotlin version" src="https://img.shields.io/badge/dynamic/toml?url=https%3A%2F%2Fraw.githubusercontent.com%2Fxemantic%2Fplenty-of-room%2Fmain%2Fgradle%2Flibs.versions.toml&query=versions.kotlin&label=kotlin">](https://kotlinlang.org/docs/releases.html)
[<img alt="discord users online" src="https://img.shields.io/discord/811561179280965673">](https://discord.gg/vQktqqN2Vn)
[![Bluesky](https://img.shields.io/badge/Bluesky-0285FF?logo=bluesky&logoColor=fff)](https://bsky.app/profile/xemantic.com)

## Why?

Agentic loops running against the NDI Gen-1 DNA-origami actuator simulation programme produce far more material than any single run can be judged by —
traces, intermediate artifacts, simulation outputs, and the reasoning that connected them.
This project collects that material as an evidence corpus,
so that claims about what the loops actually did can be checked against the record instead of against recollection.

The name is a nod to Feynman's
[There's Plenty of Room at the Bottom](https://en.wikipedia.org/wiki/There%27s_Plenty_of_Room_at_the_Bottom).

## What is in here

The project fulfils [the NDI Gen-1 actuator simulation problem](third-party/2026-08-ndi-gen1-problem-definition.md) —
Nano Dynamics Institute's electrically addressable DNA-origami nanomechanics, posed as eight tasks with acceptance predicates and no prescribed method.

NDI runs a **Formulate → Plan → Execute → Verify** loop and is explicit that the loop, not just the answers, is what is being evaluated.
So the loop is the structure of this repository:

| Path | What it is |
|---|---|
| [ANSWERS.md](ANSWERS.md) | **Start here.** The eight tasks of §6 and the open questions of §4, answered in NDI's own terms, with the claim behind each number. |
| [SESSION-PROMPT.md](SESSION-PROMPT.md) | The standing instruction for one iteration. Start a run with `/loop read @SESSION-PROMPT.md and follow the instructions in it`. |
| [TASKS.md](TASKS.md) | The live queue. Process blockers outrank cheap wins. |
| [JOURNAL.md](JOURNAL.md) | Every interaction, decision, and surprise, in order. |
| [gpd/](gpd/README.md) | The record: tasks with their four stages, machine-readable results, verified claims with provenance, and challenges. |
| `src/` | The numeric models and their entry points, in Kotlin/JVM. Tests are written first. |

Status is **TRL 1–3** throughout. A claim marked `PASS` is model-consistent and traceable; nothing here is measured.

### Running a study

Each task adds its own entry point rather than competing for the single `application` main class:

```shell
./gradlew test
./gradlew study -Pstudy=brush.BrushStiffnessStudyKt   # T-1, layer stiffness
./gradlew study -Pstudy=material.PegMaterialStudyKt   # P-3, PEG/water parameter sheet
```

Results land in [gpd/results/](gpd/results/) as JSON, with every parameter of the run alongside the numbers.
The files carry no timestamp, so a re-run that changes nothing produces no diff.

## Development

See [DEVELOPMENT.md](DEVELOPMENT.md) for maintenance notes —
updating the gradle wrapper and the project dependencies.

## Documentation conventions

All the Markdown files in this project are authored with
[semantic line breaks](https://sembr.org/).
Each sentence starts on its own line,
and long sentences may be split further at clause boundaries.
This keeps `git diff` and code review focused on the sentence that actually changed,
instead of on a whole reflowed paragraph.

There is no maximum line length,
and paragraphs are never hard-wrapped to a fixed column.
Line length is a rendering concern,
so it is left to the editor.

### Markdown soft wrapping in the IDE

**IntelliJ IDEA**:
`Settings` → `Editor` → `General` → `Soft Wraps`,
enable `Soft-wrap these files` and make sure the mask contains `*.md`
(the default mask already does).
To toggle it for the file at hand only,
use `View` → `Active Editor` → `Soft-Wrap`.

**VS Code**:
add the following to your `settings.json`:

```json
{
  "[markdown]": {
    "editor.wordWrap": "on"
  }
}
```

Alternatively toggle it for the current file with `Alt`+`Z` (`Option`+`Z` on macOS).
