#!/usr/bin/env bash
# Boots the real IUNA pack server with the freshly built jar and checks the permissions bridge
# end to end: LuckPerms data set with `lp`, read back through the Fabric API the way the pack's
# Fabric mods read it, and through Banhammer's own code.
#
# Usage: scripts/integration-test.sh [options]
#   --jar FILE        mod jar (default: newest build/libs/iunacompat-*-mc*.jar)
#   --modpack DIR     checkout of IunaMC/modpack (default: shallow clone of --modpack-ref)
#   --modpack-ref REF branch or tag to clone (default: main)
#   --work DIR        working folder (default: ./servidor-teste, ignored by git)
#   --reuse           the server in <work>/server is already installed: just start it
#
# Variables: MEMORY (default 5G), LIMIT seconds to reach "Done" (default 1200).
# The modpack's own scripts/testar-servidor.sh installs NeoForge and the pack's server mods
# (about 1.5 GB) and starts the server; this script drives its console through a FIFO.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAR=""
MODPACK=""
MODPACK_REF=main
WORK="$ROOT/servidor-teste"
REUSE=false
while (( $# )); do
  case "$1" in
    --jar) JAR="${2:?--jar needs a file}"; shift ;;
    --modpack) MODPACK="${2:?--modpack needs a folder}"; shift ;;
    --modpack-ref) MODPACK_REF="${2:?--modpack-ref needs a ref}"; shift ;;
    --work) WORK="${2:?--work needs a folder}"; shift ;;
    --reuse) REUSE=true ;;
    -h|--help) sed -n '2,/^set -euo/{/^set/d;s/^# \{0,1\}//;p}' "$0"; exit 0 ;;
    *) echo "Unknown option: $1 (see --help)" >&2; exit 2 ;;
  esac
  shift
done

MEMORY="${MEMORY:-5G}"
LIMIT="${LIMIT:-1200}"
[[ -n "$JAR" ]] || JAR="$(ls -t "$ROOT"/build/libs/iunacompat-*-mc*.jar 2>/dev/null | head -1 || true)"
[[ -f "$JAR" ]] || { echo "Mod jar not found. Run ./gradlew build first." >&2; exit 2; }
WORK="$(realpath -m "$WORK")"
SERVER="$WORK/server"
LOG="$WORK/server.log"
FIFO="$WORK/console"
mkdir -p "$SERVER/mods" "$SERVER/config/luckperms" "$SERVER/kubejs/server_scripts"

echo "==> Mod jar: $(basename "$JAR")"
rm -f "$SERVER"/mods/iunacompat-*.jar
cp "$JAR" "$SERVER/mods/"
# A server without the pack's configs has no LuckPerms storage choice: use plain files, which
# need no download at startup.
[[ -f "$SERVER/config/luckperms/luckperms.conf" ]] || printf 'storage-method = "yaml"\n' > "$SERVER/config/luckperms/luckperms.conf"
cp "$ROOT/scripts/integration/probe.js" "$SERVER/kubejs/server_scripts/zz_compat_probe.js"

rm -f "$FIFO" "$LOG"
mkfifo "$FIFO"
if $REUSE; then
  NEOFORGE="$(ls "$SERVER/libraries/net/neoforged/neoforge")"
  echo "==> Starting the existing server (NeoForge $NEOFORGE)"
  (cd "$SERVER" && exec java "-Xms${MEMORY}" "-Xmx${MEMORY}" "@libraries/net/neoforged/neoforge/$NEOFORGE/unix_args.txt" nogui < "$FIFO" > "$LOG" 2>&1) &
else
  if [[ -z "$MODPACK" ]]; then
    MODPACK="$WORK/modpack"
    rm -rf "$MODPACK"
    git clone --quiet --depth 1 --branch "$MODPACK_REF" https://github.com/IunaMC/modpack "$MODPACK"
  fi
  echo "==> Installing the pack and starting the server (IunaMC/modpack, scripts/testar-servidor.sh --manter)"
  (cd "$MODPACK" && MEMORIA="$MEMORY" exec bash scripts/testar-servidor.sh --manter "$SERVER" < "$FIFO" > "$LOG" 2>&1) &
fi
SERVER_PID=$!
exec 3> "$FIFO"
cleanup() {
  echo stop >&3 2>/dev/null || true
  exec 3>&- 2>/dev/null || true
  for _ in $(seq 60); do kill -0 "$SERVER_PID" 2>/dev/null || break; sleep 2; done
  kill "$SERVER_PID" 2>/dev/null || true
}
trap cleanup EXIT

strip() { sed 's/\x1b\[[0-9;]*m//g' "$LOG"; }

echo "==> Waiting for the server (limit ${LIMIT}s)"
start=$SECONDS
until strip | grep -qE 'Done \([0-9.]+s\)!'; do
  if ! kill -0 "$SERVER_PID" 2>/dev/null || (( SECONDS - start > LIMIT )); then
    echo "::error::The server did not become ready." >&2
    strip | tail -60 >&2
    exit 1
  fi
  sleep 5
done
echo "    ready in $((SECONDS - start))s"

failures=0
# ask <console command> <extended regex that the output must match> [description]
ask() {
  local command="$1" pattern="$2" label="${3:-$1}" before out
  before="$(strip | wc -l)"
  echo "$command" >&3
  for _ in $(seq 40); do
    sleep 1
    out="$(strip | tail -n +$((before + 1)))"
    if grep -qE -- "$pattern" <<< "$out"; then
      echo "  ok    $label"
      return 0
    fi
  done
  echo "  FAIL  $label"
  echo "        expected /$pattern/ after: $command"
  sed 's/^/        | /' <<< "$out" | tail -8
  failures=$((failures + 1))
}

U=11111111-1111-1111-1111-111111111111
echo "==> Module status"
ask "iunacompat status" '\[permissions\] ENABLED: listening on 4 of 4 events' "module enabled on all four events"
for mod in Banhammer "Styled Chat" "Styled Player List" "Essential Commands" EasyAuth; do
  ask "iunacompat status" "consumer $mod: uses the same event \(bridged\)" "$mod reaches the bridged event"
done

echo "==> Permissions through the Fabric API"
ask "lp user $U clear" 'cleared' "reset the test user"
ask "lp user $U permission set banhammer.duration.1d true" 'Set banhammer.duration.1d to true' "set a node to true"
ask "lp user $U permission set easyauth.commands.login false" 'Set easyauth.commands.login to false' "set a node to false"
ask "iunacompat check $U banhammer.duration.1d online" 'offline=TRUE online=TRUE' "true node, offline and online"
ask "iunacompat check $U easyauth.commands.login online" 'offline=FALSE online=FALSE' "false node, offline and online"
ask "iunacompat check $U easyauth.commands.register online" 'offline=DEFAULT online=DEFAULT' "unset node leaves the mod's default"
ask "lp creategroup compattest" 'created' "create a group"
ask "lp group compattest permission set banhammer.punish.ban true" 'Set banhammer.punish.ban to true' "set a node on the group"
ask "lp group compattest permission set styledchat.format.* true" 'Set styledchat.format.\* to true' "set a wildcard on the group"
ask "lp user $U parent add compattest" 'now inherits permissions from compattest' "inherit the group"
ask "iunacompat check $U banhammer.punish.ban online" 'offline=TRUE online=TRUE' "inherited node"
ask "iunacompat check $U styledchat.format.bold online" 'offline=TRUE online=TRUE' "inherited wildcard"
ask "iunacompat check 22222222-2222-2222-2222-222222222222 banhammer.duration.1d" 'offline=DEFAULT' "unknown user has no opinion"

echo "==> Options through the Fabric API"
ask "lp user $U meta set styled_chat.display_name Staff" 'Set meta key' "set a meta value"
ask "lp user $U meta addprefix 100 [Mod]" 'prefix' "set a prefix"
ask "iunacompat option $U styled_chat.display_name online" 'online=Staff' "meta option"
ask "iunacompat option $U prefix online" 'online=\[Mod\]' "prefix option"
ask "iunacompat option $U nothing online" 'online=<none>' "missing option"

echo "==> Banhammer's own code"
ask "lp user $U permission set banhammer.duration.example true" 'Set banhammer.duration.example to true' "grant the duration limit node"
ask "iunacompat check $U banhammer.duration.example online" 'online=TRUE' "load the user"
ask "zzprobe" 'ZZPROBE banhammer.durationLimit=2678400' "Banhammer applies the 31d limit from LuckPerms"
ask "lp user $U permission unset banhammer.duration.example" 'Unset banhammer.duration.example' "withdraw the node"
ask "iunacompat check $U banhammer.duration.example online" 'online=DEFAULT' "node is unset again"
ask "zzprobe" 'ZZPROBE banhammer.durationLimit=unlimited' "Banhammer falls back to its default"

echo "==> Clean up"
ask "lp user $U clear" 'cleared' "reset the test user"
ask "lp deletegroup compattest" 'deleted' "remove the group"

if (( failures )); then
  echo "::error::$failures check(s) failed." >&2
  exit 1
fi
echo "All checks passed."
