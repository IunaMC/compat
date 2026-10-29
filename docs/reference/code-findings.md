# Code findings

What was read in the code before writing the mod: decompiled jars from pack
`3.9.0` (Fernflower) and, as a second source, the projects' open-source code.
Nothing here is a guess. Where the code gives no answer, the line says
**to be confirmed**.

Versions read:

| Component | Version |
| --- | --- |
| Minecraft / NeoForge | 1.21.1 / 21.1.253 |
| Sinytra Connector | 2.0.0-beta.17+1.21.1 |
| LuckPerms (NeoForge) | 5.4.150 |
| Banhammer | 0.11.1+1.21 |
| Styled Chat | 2.6.1+1.21 |
| Styled Player List | 3.5.1+1.21 |
| Essential Commands | 0.35.2-mc1.21 (with ec-core 1.3.0) |
| EasyAuth | mc1.21-3.3.6 |
| fabric-permissions-api | 0.3.1 (embedded) |

Fabric mod classes appear with *intermediary* names (`class_2172` is
`SharedSuggestionProvider`, `class_2168` is `CommandSourceStack`, `method_9259`
is `hasPermission`): that is how the published jar looks, and Connector remaps
it at load time.

## (a) The embedded API and what each mod consumes

### The event exists and is a Fabric API event

`me.lucko.fabric.api.permissions.v0.PermissionCheckEvent` in 0.3.1:

```java
public interface PermissionCheckEvent {
   Event<PermissionCheckEvent> EVENT = EventFactory.createArrayBacked(PermissionCheckEvent.class, (callbacks) -> (source, permission) -> {
         for(PermissionCheckEvent callback : callbacks) {
            TriState state = callback.onPermissionCheck(source, permission);
            if (state != TriState.DEFAULT) { return state; }
         }
         return TriState.DEFAULT; });
   @NotNull TriState onPermissionCheck(@NotNull class_2172 var1, @NotNull String var2);
}
```

The source code (`lucko/fabric-permissions-api`, tag `v0.3.1`) has the same
signature, with `net.minecraft.command.CommandSource` in Yarn. The first
listener that does not return `DEFAULT` decides.

The other three interfaces in the same version:

| Interface | Method | Return type |
| --- | --- | --- |
| `OptionRequestEvent` | `onOptionRequest(class_2172, String)` | `Optional<String>` |
| `OfflinePermissionCheckEvent` | `onPermissionCheck(UUID, String)` | `CompletableFuture<TriState>` |
| `OfflineOptionRequestEvent` | `onOptionRequest(UUID, String)` | `CompletableFuture<Optional<String>>` |

### `Permissions.check(source, node, level)` is the event, with the level as fallback

`Permissions` (same package):

```java
static @NotNull TriState getPermissionValue(@NotNull class_2172 source, @NotNull String permission) {
   return ((PermissionCheckEvent)PermissionCheckEvent.EVENT.invoker()).onPermissionCheck(source, permission);
}
static boolean check(@NotNull class_2172 source, @NotNull String permission, boolean defaultValue) {
   return getPermissionValue(source, permission).orElse(defaultValue);
}
static boolean check(@NotNull class_2172 source, @NotNull String permission, int defaultRequiredLevel) {
   return getPermissionValue(source, permission).orElseGet(() -> source.method_9259(defaultRequiredLevel));
}
```

In other words: with no listener (or with every listener returning `DEFAULT`),
the mod's default applies, either a boolean or `source.hasPermission(level)`.
`Options.get` and the UUID queries use the other three events.

### What each mod in the pack consumes

A scan of **every** jar in the pack that runs on the server (310 files verified by
hash; the 19 without a `url` in their `.pw.toml`, which are `metadata:curseforge`,
were not included, and none of them is a Fabric mod by name). Only these
reference `me/lucko/fabric/api/permissions`:

| Mod | Calls (class: excerpt) |
| --- | --- |
| Banhammer | `ConfigManager.requirePermissionOrOp`: `Permissions.check(player, permission, CONFIG.configData.defaultOpPermissionLevel)`. `Config.getDurationLimit`: `Permissions.check(source, "banhammer.duration." + key)`. `PunishCommands`: `Permissions.check(source, "banhammer.ignoreTempLimit", 2)` and `"banhammer.seesilent", 1`. `BanHammerImpl:189`: `Permissions.check(player, "banhammer.seesilent", 3)`. `BHUtils.isPunishableBy`: `Permissions.check(source, "banhammer.can_ban_admins") \|\| !Permissions.check(profile, "banhammer.block_punishments").get()` (this one uses the offline event). `GeneralCommands`: `Permissions.require("banhammer.commands.main", true)`, `reload`/`import`/`export`/`list` at level 4 |
| Styled Chat | `StyledChatUtils`: `Permissions.check(source, "styledchat.format." + tag, safeUser ? 2 : 4)` (and `unsafe_format.`), `"styledchat.format.spoiler", 2`. `Commands`: `styledchat.main` (true), `reload` (3), `set` (2), `get` (2), `clear` (3), `tellform` (2). `ChatStyleData:55`: `Options.get(source, "styled_chat." + property)` |
| Styled Player List | `Commands`: `styledplayerlist.main` and `switch` (true), `switch.others` (2), `reload` (3) |
| Essential Commands | `ECPerms.check`: `Permissions.getPermissionValue(source, node).orElse(source.hasPermission(Math.max(2, level)))`, only if `use_permissions_api=true` (it is on in the configs); node list in `ECPerms.Registry` |
| EasyAuth | `integrations/Permissions`: `Permissions.check(source, node, default)`, only if `permissionsLoaded` (the `fabric-permissions-api-v0` mod is loaded). `easyauth.commands.*` nodes (19), level 3/4 for the admin ones and `true` for login, logout, register and account |

`LinguaBib` (embedded in Flan) imports `Permissions`, but
`LinguaBib.permissionAPI` is never set in the NeoForge variant (the field is
never assigned in the jar that was read): it is inert. The NeoForge mods (Balm,
ServerCore, Mekanism, Simple Voice Chat, Spark, Waterframes, Nature's Compass,
Fzzy Config) use NeoForge's `PermissionAPI`, which LuckPerms already answers.

## (b) How Connector loads these jars

- Each Fabric mod ships the API in `META-INF/jars/fabric-permissions-api-0.3.1.jar`
  (Banhammer, Styled Chat, Styled Player List and, inside `ec-core`, Essential
  Commands). All four have the same SHA-1.
- `ConnectorLocator.locateFabricMods` discovers the nested jars
  (`discoverNestedJarsRecursive`) and resolves everything with the Fabric Loader
  `ModResolver` (`DependencyResolver.resolveDependencies`): **one instance per
  mod id**, the highest version and, on a tie, the least nested one
  (`modPrioComparator`).
- Confirmed when booting the test server (`logs/debug.log`): all four jars are
  transformed, but only **one** is loaded:

  ```text
  Found valid mod file banhammer-0.11.1+1.21$fabric-permissions-api-0.3.1_mapped_moj_1.21.1.jar with {fabric_permissions_api_v0} mods - versions {0.3.1}
  ```

  The mod id on the NeoForge side is `fabric_permissions_api_v0`. So the event is
  a single object for every mod, and a listener registered on it applies to all
  five.
- The jar is remapped to Mojang names (`_mapped_moj_1.21.1`): at run time the
  interface receives `SharedSuggestionProvider`, not `class_2172`.
- `FabricModsDiscoverer.isFabricModJar` skips any jar that has
  `META-INF/neoforge.mods.toml`. That is why the IUNA mod is pure NeoForge, with
  no `fabric.mod.json`.

## (c) Native NeoForge or Fabric through Connector

- **A Fabric mod loaded by Connector can reach the LuckPerms NeoForge API.**
  EasyAuth already does this: `xyz.nikitacartes.easyauth.integrations.LuckPermsIntegration`
  calls `LuckPermsProvider.get().getContextManager().registerCalculator(...)`.
  `inv_view` (NeoForge) uses the same API in `util/PermissionHandler`.
- **NeoForge's `PermissionAPI`/`PermissionGatherEvent` path does not solve it.**
  LuckPerms implements it (`NeoForgePermissionHandler`,
  `NeoForgePermissionHandlerListener.onPermissionGatherHandler`), but the Fabric
  mods never ask it: they call the Fabric events.
- **LuckPerms for NeoForge does not listen to the Fabric event.** In the inner
  jar of LuckPerms for NeoForge (`luckperms-neoforge.jarinjar`), no class
  mentions `me.lucko.fabric` and no file has `fabric` in its name. The
  `PermissionCheckEvent` that appears in the sources is LuckPerms' own
  (`common.verbose.event`), an unrelated class. The Fabric listener exists only
  in LuckPerms for **Fabric** (`FabricPermissionsApiV0Listener`).
- **What LuckPerms for NeoForge does with commands** (`BrigadierInjector`): it
  replaces the `requirement` of each node with `InjectedPermissionRequirement`:

  ```java
  Tristate state = user.getCachedData().getPermissionData(queryOptions).checkPermission(this.permission);
  if (state != Tristate.UNDEFINED) {
     return state.asBoolean() && this.delegate.test(source.withPermission(4));
  }
  return this.delegate.test(source);
  ```

  With the `command.<path>` node granted, the mod's original requirement runs at
  **level 4**. That is why `Permissions.require("banhammer.commands.reload", 4)`
  lets through anyone who has `command.banhammer.reload`. Outside the requirement
  (at execution time), the `source` goes back to the player's real level: that is
  where `banhammer.ignoreTempLimit` and `banhammer.duration.*` are checked.
- **Choice:** a native NeoForge mod, the Fabric API through reflection, LuckPerms
  through its public API. Rationale and alternatives in
  [0002](../decisions/0002-native-neoforge-mod-with-reflection.md).

## (d) The LuckPerms Fabric reference logic

`FabricPermissionsApiV0Listener` (repository `LuckPerms/LuckPerms`, branch
`master`; the `v5.4` tag does not have the file at that path, so the exact
version for 5.4.150 is **to be confirmed**). It registers the four events and:

| Query | Rule |
| --- | --- |
| Source with a `ServerPlayer` entity | `((MixinUser) player).luckperms$hasPermission(node)`: user already in memory, the player's current contexts, `CheckOrigin.PLATFORM_API_HAS_PERMISSION` |
| Console and other sources | `TriState.DEFAULT`, but it records the node in the *permission registry* and in verbose (`offerPermissionCheckEvent`) |
| Player with no loaded user (fake player) | `Tristate.UNDEFINED` (`ServerPlayerMixin.luckperms$hasPermission`: "fake players will have our mixin, but won't have been initialised") |
| Offline UUID | `lookupUser`: `getIfLoaded` or `storage.loadUser(uuid, null)`, then `getCachedData().getPermissionData()` |
| Options | `getMetaOrChatMetaValue(key)`: `prefix` and `suffix` are chat meta, everything else is meta |
| `UNDEFINED` → `DEFAULT`, `TRUE` → `TRUE`, `FALSE` → `FALSE` | `fabricTristate` |

What NeoForge has in common (decompiled `LuckPerms-NeoForge-5.4.150`):

- `NeoForgeContextManager extends InlineContextManager<ServerPlayer, ServerPlayer>`:
  `ContextManager.getQueryOptions(Object)` from the **public API** accepts a
  `ServerPlayer` and returns the options with the player's current context.
- `PermissionCalculator.apply` calls `plugin.getPermissionRegistry().offer(node)`
  on every query, whatever the answer: querying through the public API already
  feeds tab completion, `/lp tree` and the web editor. Verbose also records it
  (`PermissionCalculator.checkPermission` → `offerPermissionCheckEvent`);
  through the public API the origin shows as `LUCKPERMS_API`.
- `PermissionHolder.getCachedData()` exists for groups: known nodes can be
  registered by querying the `default` group, without creating anything.
- A node is always lowercased during calculation (`apply` calls `toLowerCase`):
  nodes such as `easyauth.commands.account.changePassword` match what is stored
  in LuckPerms.

## (e) Effects of enabling the bridge

### What changes

The bridge returns `DEFAULT` for anything LuckPerms does not define. Only what is
**defined** changes. In the server configs that were read (the LuckPerms groups
in the private config repository), no group or user defines `banhammer.*`,
`styledchat.*`, `styledplayerlist.*`, `essentialcommands.*` or `easyauth.*`
nodes (only `command.*`). So enabling the bridge **changes no behavior** until
someone sets one of those nodes.

### Nodes per mod (what each does when defined)

| Node | Mod default | Effect of setting it |
| --- | --- | --- |
| `banhammer.duration.<key>` | none: `defaultTempPunishmentDurationLimit` applies | `true` grants that key's limit (`permissionTempLimit` in Banhammer's `config.json`; `defaultTempPunishmentDurationLimit = "-1"` becomes `Long.MAX_VALUE`, no limit); the largest granted limit wins |
| `banhammer.ignoreTempLimit` | level 2 (at execution time, the player's real level: 0) | `true` ignores the duration limit |
| `banhammer.can_ban_admins` | `false` | `true` allows punishing players with a higher level than yours |
| `banhammer.block_punishments` | `false` (offline query) | `true` on the target prevents punishing them |
| `banhammer.seesilent` | level 1 or 3 | `true` shows silent punishments |
| `banhammer.punish.*`, `banhammer.unpunish.*` | `defaultOpPermissionLevel` (4 in the server config) | `false` blocks the command even with `command.*` granted |
| `banhammer.commands.*` | `main` true, the rest level 4 | same |
| `styledchat.format.<tag>`, `styledchat.unsafe_format.<tag>` | level 2 (safe tags) or 4 | `true` allows the formatting tag in chat |
| `styledchat.main/set/get/clear/reload/tellform` | true / 2 / 2 / 3 / 3 / 2 | same |
| `styledplayerlist.*` | true / true / 2 / 3 | same |
| `essentialcommands.*` | `hasPermission(max(2, level))` | `false` denies; the numeric-limit nodes (`...home.limit.<n>` and similar) start to take effect |
| `easyauth.commands.*` | true (player) or 3/4 (admin) | `false` on `login`/`register` **locks the player out** |

### Risks and how to avoid them

1. **A denied `true` default becomes a lock.** `easyauth.commands.login`,
   `easyauth.commands.register`, `styledchat.main`, `styledplayerlist.main` and
   `banhammer.commands.main` default to `true`. A `false` on them, or a denied
   `easyauth.*` in a group, blocks everyone. Never deny these nodes in the
   `default` group.
2. **A wildcard granting too much.** `banhammer.*` set to `true` grants
   `banhammer.can_ban_admins` and `banhammer.ignoreTempLimit` along with
   everything else, and LuckPerms resolves wildcards. Grant explicit nodes.
3. **A denied `command.*` does not cover mod nodes.** The server's `default`
   group denies `command.*`, but that wildcard only matches nodes that start with
   `command.`. `banhammer.duration.*` and similar nodes stay `DEFAULT` (so the
   mod decides).
4. **Console.** Console, command blocks and RCON get no opinion: the mod's
   default and the console's level 4 still apply.
5. **A player whose user is not loaded yet** (login in progress): no opinion.
6. **An offline query blocks the caller.** `BHUtils.isPunishableBy` calls
   `.get()` on the offline future; the query reads the user from LuckPerms
   storage if it is not in memory. It is called from inside
   `CompletableFuture.runAsync` (`PunishCommands.punishCommand`), off the server
   thread.
7. **Typo in `styledchat`.** The mod builds `styledchat.format..type.<type>`
   (double dot, `StyledChatUtils`); that node is never useful. It is not a bridge
   problem.
8. **Effects only on what a player can additionally do.** Because the bridge
   returns `DEFAULT` when nothing is defined, it never reopens anything LuckPerms
   blocks today: the current blocking comes from `command.*` and from
   `weight`/inheritance, which the bridge does not touch.

Proposed permission changes for the server are not kept here: this repository is
public, and the server's nodes live in the private config repository.

## (f) Validation in practice

The pack's real server, version `3.9.0` (NeoForge 21.1.253, every server mod),
with mod `0.1.0`, LuckPerms with file storage and the console. Results of the
[integration test](../guides/server-test.md):

| Check | Result |
| --- | --- |
| Module | `ENABLED: listening on 4 of 4 events` |
| Consumers | Banhammer, Styled Chat, Styled Player List, Essential Commands and EasyAuth resolve the **same** `PermissionCheckEvent` as the mod (`Class.forName(event, false, modLoader)` equals the mod's class) |
| `true` / `false` / undefined node | `TRUE` / `FALSE` / `DEFAULT`, offline and for a simulated player |
| Group inheritance and wildcard | `banhammer.punish.ban` and `styledchat.format.*` inherited from a group take effect |
| Options | `styled_chat.display_name` → `Staff`; `prefix` → `[Mod]`; missing → empty |
| Banhammer's real code | `ConfigManager.getConfig().getDurationLimit(source)` returns `2678400` (31 days) with `banhammer.duration.example = true` in LuckPerms and "no limit" (`Long.MAX_VALUE`) when the node is removed |

Finding: with `fabric-permissions-api` 0.3.1, `OfflineOptionRequestEvent` throws
`NullPointerException` as soon as any listener is registered (the combiner starts
from `CompletableFuture.completedFuture(null)` and the first `thenCompose` reads
`value.isPresent()`). The source code confirms the bug in tags `v0.3.1` and
`v0.3.3` and the fix in `v0.4.1` (`completedFuture(Optional.empty())`). No mod in
the pack uses this query, so it has no effect today. The `/iunacompat option`
diagnostic reports the failure as `failed inside the API`.

Not covered: a truly connected player. The test uses NeoForge's fake player; see
[what is not covered](../guides/server-test.md#what-is-not-covered).
