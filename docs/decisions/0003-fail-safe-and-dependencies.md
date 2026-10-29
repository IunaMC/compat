# 0003: optional LuckPerms and failure without side effects

- **Date:** 2026-10-29
- **Status:** accepted
- **Origin:** creation of this repository

## Context

The mod runs inside other mods' permission checks, which happen on every command
packet and every command execution. An exception there would break the command,
the chat or the login. The pack can also change: LuckPerms or the Fabric mods
may be removed.

## Decision

- **LuckPerms is an optional dependency** (`type = "optional"`, `ordering = "AFTER"`,
  `[5.4,)`). Without it, the `permissions` module reports `UNAVAILABLE` and
  registers nothing. The same applies when no Fabric mod embeds the API (`absent`).
- **New API shape = idle module and a warning.** The adapter checks the name,
  parameters and return type of each interface. A mismatched interface is skipped
  and listed among the problems; if the main one (`PermissionCheckEvent`)
  changed, nothing is registered.
- **Never throw into the caller.** `PermissionBridge` catches `Throwable`, counts
  the error, logs the stack trace only the first time and answers "no opinion"
  (`DEFAULT`, `Optional.empty()`), which keeps the calling mod's default.
- **No opinion instead of a blocking read.** An online player whose user is not
  yet in memory gets "no opinion" (as in LuckPerms for Fabric) instead of a disk
  read on the server thread.
- **The mod never grants anything.** It only reflects what LuckPerms has.

## Alternatives rejected

- **Required dependency on LuckPerms.** The server would not start without it,
  because of a module that is only useful with it.
- **Deny when the backend fails** (`FALSE`). A LuckPerms failure would lock
  everyone out (for example, `easyauth.commands.login` defaults to `true`). "No
  opinion" restores the behavior from before the mod.

## Consequences

- Server operators see the state in `/iunacompat status` and in the startup log.
- A LuckPerms or API bug shows up as an error counter, not as a failed command.
