# Server test

Goal: start the pack's real server with the freshly built jar and check the
bridge end to end.

## Prerequisites

- `./gradlew build` has run (the jar must be in `build/libs/`).
- Java 21, `git`, `curl` and Python 3.
- About 5 GB of free memory (set `MEMORY=4G` if that is tight), 3 GB of disk
  and 5 to 10 minutes.

## Steps

```sh
./scripts/integration-test.sh
```

The script:

1. clones `IunaMC/modpack` (`main`, or `--modpack-ref REF`) and uses its
   `scripts/testar-servidor.sh` to install NeoForge and the pack's server mods
   in `servidor-teste/server/`;
2. puts the mod jar in `mods/` and writes a LuckPerms config with file storage
   and a KubeJS test script (`scripts/integration/probe.js`);
3. starts the server and drives the console through a FIFO;
4. checks `/iunacompat status`, sets nodes and options with `lp` and reads them
   back through the Fabric API and Banhammer's code (`ConfigManager.getDurationLimit`);
5. cleans up what it created and stops the server.

To repeat without reinstalling: `./scripts/integration-test.sh --reuse`. To use a
local checkout of the pack: `--modpack ../iunamc.modpack`.

## Verify

Each `ok` line is a passed check; the run ends with `All checks passed.` and exit
code 0. A failure shows the expected pattern and the last lines of the console.
The full log is in `servidor-teste/server.log`.

If port 8765 is already in use (the modpack's `testar-servidor.sh` serves the
pack on it), another process is holding it: stop that process first.

## After it passes

If the pack changed, record the new state as tested:

```sh
python3 scripts/pack-drift.py --update
```

## What is not covered

The test uses a fake player (`FakePlayerFactory`), not a connected player: it
goes through the same LuckPerms path and the same mod code, but not through the
network, the login or a real player's contexts. Before relying on a node in
production, join the server and run a command that uses it.
