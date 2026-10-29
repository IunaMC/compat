# Changelog

Release notes for each version, newest first. The Release workflow uses the
version's section as the release text. Versions follow
[SemVer](https://semver.org/).

## 0.1.0

First release.

- `permissions` module: bridges `fabric-permissions-api` to LuckPerms on
  NeoForge. The pack's Fabric mods that run through Sinytra Connector
  (Banhammer, Styled Chat, Styled Player List, Essential Commands and EasyAuth)
  now read the nodes and options defined in LuckPerms: `banhammer.duration.*`,
  `banhammer.ignoreTempLimit`, `styledchat.*`, `styled_chat.*` and the rest.
- Anything LuckPerms does not define keeps the calling mod's default: installing
  the mod changes nothing until someone sets a node.
- Offline-player and option queries, `config/iunacompat-common.toml` to turn
  each part off, and registration of the known nodes with LuckPerms (tab
  completion and the web editor).
- `/iunacompat` diagnostic command (`status`, `check`, `option`).
- Without LuckPerms or the Fabric API, the module stays idle and says so in the
  log.
