#!/usr/bin/env python3
"""Compares the versions the mod was last tested with against the current IUNA pack.

The permissions bridge depends on how the pack's Fabric mods, Sinytra Connector, LuckPerms and
NeoForge behave, so a change in any of them is a reason to run the integration test again.

Usage:
  scripts/pack-drift.py                 # exit 1 and print the differences if the pack moved
  scripts/pack-drift.py --update        # record the current pack as tested (after a green integration run)
  scripts/pack-drift.py --ref REF       # read the pack from this branch or tag (default: main)
"""
import json
import sys
import tomllib
import urllib.request
from pathlib import Path

TESTED = Path(__file__).resolve().parent.parent / "tested-with.json"
RAW = "https://raw.githubusercontent.com/IunaMC/modpack/{ref}/pack/{path}"
# Mods whose version matters to the bridge: who consumes the API, who provides it, who answers it.
WATCHED = [
    "banhammer", "styled-chat", "styledplayerlist", "essential-commands", "easyauth",
    "luckperms", "connector", "forgified-fabric-api",
]


def fetch(ref, path):
    with urllib.request.urlopen(RAW.format(ref=ref, path=path), timeout=60) as response:
        return tomllib.loads(response.read().decode("utf-8"))


def current(ref):
    pack = fetch(ref, "pack.toml")
    state = {
        "pack": pack["version"],
        "minecraft": pack["versions"]["minecraft"],
        "neoforge": pack["versions"]["neoforge"],
        "mods": {},
    }
    for slug in WATCHED:
        state["mods"][slug] = fetch(ref, f"mods/{slug}.pw.toml")["filename"]
    return state


def flatten(state):
    flat = {k: v for k, v in state.items() if k != "mods"}
    flat.update({f"mods.{slug}": name for slug, name in state["mods"].items()})
    return flat


def main(argv):
    ref = "main"
    if "--ref" in argv:
        ref = argv[argv.index("--ref") + 1]
    state = current(ref)
    if "--update" in argv:
        TESTED.write_text(json.dumps(state, indent=2) + "\n", encoding="utf-8")
        print(f"Recorded pack {state['pack']} ({ref}) in {TESTED.name}")
        return 0
    tested = json.loads(TESTED.read_text(encoding="utf-8"))
    old, new = flatten(tested), flatten(state)
    changes = [(k, old.get(k), new.get(k)) for k in sorted(old.keys() | new.keys()) if old.get(k) != new.get(k)]
    if not changes:
        print(f"The pack ({ref}) matches what was tested: {state['pack']}")
        return 0
    print(f"The pack ({ref}) moved since the last tested version:")
    for key, before, after in changes:
        print(f"  {key}: {before} -> {after}")
    print("Run scripts/integration-test.sh and, if it passes, scripts/pack-drift.py --update")
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
