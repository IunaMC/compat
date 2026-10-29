# Confirmações no código

O que foi lido no código antes de escrever o mod: decompilação dos jars do pack
`3.9.0` (Fernflower) e, como segunda fonte, o código aberto dos projetos. Nada
aqui é palpite. Onde o código não responde, a linha diz **a confirmar**.

Versões lidas:

| Peça | Versão |
| --- | --- |
| Minecraft / NeoForge | 1.21.1 / 21.1.253 |
| Sinytra Connector | 2.0.0-beta.17+1.21.1 |
| LuckPerms (NeoForge) | 5.4.150 |
| Banhammer | 0.11.1+1.21 |
| Styled Chat | 2.6.1+1.21 |
| Styled Player List | 3.5.1+1.21 |
| Essential Commands | 0.35.2-mc1.21 (com ec-core 1.3.0) |
| EasyAuth | mc1.21-3.3.6 |
| fabric-permissions-api | 0.3.1 (embutida) |

As classes de mods Fabric aparecem com nomes *intermediary* (`class_2172` é
`SharedSuggestionProvider`, `class_2168` é `CommandSourceStack`, `method_9259`
é `hasPermission`): o jar publicado é assim, e o Connector remapeia na carga.

## (a) A API embutida e o que cada mod consome

### O evento existe e é um evento do Fabric API

`me.lucko.fabric.api.permissions.v0.PermissionCheckEvent` na 0.3.1:

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

O código-fonte (`lucko/fabric-permissions-api`, tag `v0.3.1`) tem a mesma
assinatura, com `net.minecraft.command.CommandSource` em Yarn. O primeiro
ouvinte que não devolve `DEFAULT` decide.

As outras três interfaces da mesma versão:

| Interface | Método | Retorno |
| --- | --- | --- |
| `OptionRequestEvent` | `onOptionRequest(class_2172, String)` | `Optional<String>` |
| `OfflinePermissionCheckEvent` | `onPermissionCheck(UUID, String)` | `CompletableFuture<TriState>` |
| `OfflineOptionRequestEvent` | `onOptionRequest(UUID, String)` | `CompletableFuture<Optional<String>>` |

### `Permissions.check(source, nó, nível)` é o evento, com o nível de reserva

`Permissions` (mesmo pacote):

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

Ou seja: sem ouvinte (ou com todos devolvendo `DEFAULT`), vale o padrão do mod,
um booleano ou `source.hasPermission(nível)`. `Options.get` e as consultas por
UUID usam os outros três eventos.

### O que cada mod do pack consome

Varredura de **todos** os jars do pack que rodam no servidor (310 arquivos
conferidos pelo hash; os 19 sem `url` no `.pw.toml`, que são `metadata:curseforge`,
não entraram, e nenhum é Fabric pelo nome). Só estes referenciam
`me/lucko/fabric/api/permissions`:

| Mod | Chamadas (classe: trecho) |
| --- | --- |
| Banhammer | `ConfigManager.requirePermissionOrOp`: `Permissions.check(player, permission, CONFIG.configData.defaultOpPermissionLevel)`. `Config.getDurationLimit`: `Permissions.check(source, "banhammer.duration." + chave)`. `PunishCommands`: `Permissions.check(source, "banhammer.ignoreTempLimit", 2)` e `"banhammer.seesilent", 1`. `BanHammerImpl:189`: `Permissions.check(player, "banhammer.seesilent", 3)`. `BHUtils.isPunishableBy`: `Permissions.check(source, "banhammer.can_ban_admins") \|\| !Permissions.check(profile, "banhammer.block_punishments").get()` (esta usa o evento offline). `GeneralCommands`: `Permissions.require("banhammer.commands.main", true)`, `reload`/`import`/`export`/`list` com nível 4 |
| Styled Chat | `StyledChatUtils`: `Permissions.check(source, "styledchat.format." + tag, usuárioSeguro ? 2 : 4)` (e `unsafe_format.`), `"styledchat.format.spoiler", 2`. `Commands`: `styledchat.main` (true), `reload` (3), `set` (2), `get` (2), `clear` (3), `tellform` (2). `ChatStyleData:55`: `Options.get(source, "styled_chat." + propriedade)` |
| Styled Player List | `Commands`: `styledplayerlist.main` e `switch` (true), `switch.others` (2), `reload` (3) |
| Essential Commands | `ECPerms.check`: `Permissions.getPermissionValue(source, nó).orElse(source.hasPermission(Math.max(2, nível)))`, só se `use_permissions_api=true` (está ligado nas configs); lista de nós em `ECPerms.Registry` |
| EasyAuth | `integrations/Permissions`: `Permissions.check(source, nó, padrão)`, só se `permissionsLoaded` (mod `fabric-permissions-api-v0` carregado). Nós `easyauth.commands.*` (19), nível 3/4 nos de administração e `true` em login, logout, register e account |

`LinguaBib` (embutida no Flan) importa `Permissions`, mas
`LinguaBib.permissionAPI` nunca é ligada na variante NeoForge (campo sem
atribuição no jar lido): inerte. Os mods NeoForge (Balm, ServerCore, Mekanism,
Simple Voice Chat, Spark, Waterframes, Nature's Compass, Fzzy Config) usam a
`PermissionAPI` do NeoForge, que o LuckPerms já atende.

## (b) Como o Connector carrega esses jars

- Cada mod Fabric traz a API em `META-INF/jars/fabric-permissions-api-0.3.1.jar`
  (Banhammer, Styled Chat, Styled Player List e, dentro do `ec-core`, o Essential
  Commands). O SHA-1 dos quatro é o mesmo.
- `ConnectorLocator.locateFabricMods` descobre os jars aninhados
  (`discoverNestedJarsRecursive`) e resolve tudo com o `ModResolver` do Fabric
  Loader (`DependencyResolver.resolveDependencies`): **uma instância por id de mod**,
  a de maior versão e, em empate, a de menor aninhamento (`modPrioComparator`).
- Confirmado no boot do servidor de teste (`logs/debug.log`): os quatro jars são
  transformados, mas só **um** é carregado:

  ```text
  Found valid mod file banhammer-0.11.1+1.21$fabric-permissions-api-0.3.1_mapped_moj_1.21.1.jar with {fabric_permissions_api_v0} mods - versions {0.3.1}
  ```

  O id do mod no lado NeoForge é `fabric_permissions_api_v0`. Logo o evento é
  um só objeto para todos os mods, e um ouvinte registrado nele vale para os
  cinco.
- O jar sai remapeado para nomes Mojang (`_mapped_moj_1.21.1`): em tempo de
  execução a interface recebe `SharedSuggestionProvider`, não `class_2172`.
- `FabricModsDiscoverer.isFabricModJar` ignora um jar que tenha
  `META-INF/neoforge.mods.toml`. Por isso o mod da IUNA é NeoForge puro, sem
  `fabric.mod.json`.

## (c) NeoForge nativo ou Fabric pelo Connector

- **Um mod Fabric pelo Connector alcança a API do LuckPerms NeoForge.** O
  EasyAuth já faz isso: `xyz.nikitacartes.easyauth.integrations.LuckPermsIntegration`
  usa `LuckPermsProvider.get().getContextManager().registerCalculator(...)`.
  O `inv_view` (NeoForge) usa a mesma API em `util/PermissionHandler`.
- **O caminho `PermissionAPI`/`PermissionGatherEvent` do NeoForge não resolve.**
  O LuckPerms o implementa (`NeoForgePermissionHandler`,
  `NeoForgePermissionHandlerListener.onPermissionGatherHandler`), mas os mods
  Fabric nunca perguntam por ele: eles chamam os eventos do Fabric.
- **O LuckPerms NeoForge não escuta o evento do Fabric.** No jar interno do
  LuckPerms NeoForge (`luckperms-neoforge.jarinjar`), nenhuma classe cita
  `me.lucko.fabric` e nenhum arquivo tem `fabric` no nome. O
  `PermissionCheckEvent` que aparece nas fontes é o do próprio LuckPerms
  (`common.verbose.event`), outra coisa. O ouvinte do Fabric existe só no
  LuckPerms **Fabric** (`FabricPermissionsApiV0Listener`).
- **O que o LuckPerms NeoForge faz com comandos** (`BrigadierInjector`): troca o
  `requirement` de cada nó por `InjectedPermissionRequirement`:

  ```java
  Tristate state = user.getCachedData().getPermissionData(queryOptions).checkPermission(this.permission);
  if (state != Tristate.UNDEFINED) {
     return state.asBoolean() && this.delegate.test(source.withPermission(4));
  }
  return this.delegate.test(source);
  ```

  Com o nó `command.<caminho>` liberado, o requisito original do mod roda com
  **nível 4**. É por isso que `Permissions.require("banhammer.commands.reload", 4)`
  deixa passar quem tem `command.banhammer.reload`. Fora do requisito (na hora de
  executar), o `source` volta ao nível real do jogador: é onde
  `banhammer.ignoreTempLimit` e `banhammer.duration.*` são consultados.
- **Escolha:** mod NeoForge nativo, API do Fabric por reflexão, LuckPerms pela
  API pública. Justificativa e alternativas em
  [0002](../decisoes/0002-mod-neoforge-nativo-com-reflexao.md).

## (d) A lógica de referência do LuckPerms Fabric

`FabricPermissionsApiV0Listener` (repositório `LuckPerms/LuckPerms`, ramo
`master`; a tag `v5.4` não tem o arquivo nesse caminho, **a confirmar** a versão
exata de 5.4.150). Registra os quatro eventos e:

| Consulta | Regra |
| --- | --- |
| Fonte com entidade `ServerPlayer` | `((MixinUser) player).luckperms$hasPermission(nó)`: usuário já em memória, contextos atuais do jogador, `CheckOrigin.PLATFORM_API_HAS_PERMISSION` |
| Console e outras fontes | `TriState.DEFAULT`, mas registra o nó no *permission registry* e no verbose (`offerPermissionCheckEvent`) |
| Jogador sem usuário carregado (jogador falso) | `Tristate.UNDEFINED` (`ServerPlayerMixin.luckperms$hasPermission`: "fake players will have our mixin, but won't have been initialised") |
| UUID offline | `lookupUser`: `getIfLoaded` ou `storage.loadUser(uuid, null)`, depois `getCachedData().getPermissionData()` |
| Opções | `getMetaOrChatMetaValue(chave)`: `prefix` e `suffix` são meta de chat, o resto é meta |
| `UNDEFINED` → `DEFAULT`, `TRUE` → `TRUE`, `FALSE` → `FALSE` | `fabricTristate` |

O que o NeoForge tem de igual (decompilação de `LuckPerms-NeoForge-5.4.150`):

- `NeoForgeContextManager extends InlineContextManager<ServerPlayer, ServerPlayer>`:
  `ContextManager.getQueryOptions(Object)` da **API pública** aceita um
  `ServerPlayer` e devolve as opções com o contexto atual do jogador.
- `PermissionCalculator.apply` chama `plugin.getPermissionRegistry().offer(nó)` a
  cada consulta, qualquer que seja a resposta: consultar pela API pública já
  alimenta o autocompletar, o `/lp tree` e o editor web. O verbose também
  registra (`PermissionCalculator.checkPermission` → `offerPermissionCheckEvent`);
  pela API pública a origem aparece como `LUCKPERMS_API`.
- `PermissionHolder.getCachedData()` existe para grupos: dá para apresentar nós
  conhecidos ao registro consultando o grupo `default`, sem criar nada.
- Um nó é sempre minúsculo no cálculo (`apply` faz `toLowerCase`): nós como
  `easyauth.commands.account.changePassword` casam com o que se grava no LuckPerms.

## (e) Efeitos de ligar a ponte

### O que muda

A ponte devolve `DEFAULT` para tudo que o LuckPerms não define. Só muda o que
estiver **definido**. Nas configs do servidor lidas (grupos do LuckPerms do
repositório privado de configs), nenhum grupo ou usuário define nós de
`banhammer.*`, `styledchat.*`, `styledplayerlist.*`, `essentialcommands.*` ou
`easyauth.*` (só `command.*`). Logo ligar a ponte **não muda nenhum
comportamento** até alguém gravar um desses nós.

### Nós por mod (o que cada um faz quando definido)

| Nó | Padrão do mod | Efeito de definir |
| --- | --- | --- |
| `banhammer.duration.<chave>` | nenhum: vale `defaultTempPunishmentDurationLimit` | `true` libera o limite da chave (`permissionTempLimit` do `config.json` do Banhammer; `defaultTempPunishmentDurationLimit = "-1"` vira `Long.MAX_VALUE`, sem limite); vale o maior dos liberados |
| `banhammer.ignoreTempLimit` | nível 2 (na execução, o nível real do jogador: 0) | `true` ignora o limite de duração |
| `banhammer.can_ban_admins` | `false` | `true` pune quem tem nível acima do seu |
| `banhammer.block_punishments` | `false` (consulta offline) | `true` no alvo impede puni-lo |
| `banhammer.seesilent` | nível 1 ou 3 | `true` vê punições silenciosas |
| `banhammer.punish.*`, `banhammer.unpunish.*` | `defaultOpPermissionLevel` (4 na config do servidor) | `false` bloqueia o comando mesmo com `command.*` liberado |
| `banhammer.commands.*` | `main` true, demais nível 4 | idem |
| `styledchat.format.<tag>`, `styledchat.unsafe_format.<tag>` | nível 2 (seguras) ou 4 | `true` libera a tag de formatação no chat |
| `styledchat.main/set/get/clear/reload/tellform` | true / 2 / 2 / 3 / 3 / 2 | idem |
| `styledplayerlist.*` | true / true / 2 / 3 | idem |
| `essentialcommands.*` | `hasPermission(max(2, nível))` | `false` nega; os de limite numérico (`...home.limit.<n>` e similares) passam a valer |
| `easyauth.commands.*` | true (jogador) ou 3/4 (administração) | `false` em `login`/`register` **tranca o jogador fora** |

### Riscos e como evitá-los

1. **Padrão `true` negado vira trava.** `easyauth.commands.login`,
   `easyauth.commands.register`, `styledchat.main`, `styledplayerlist.main` e
   `banhammer.commands.main` têm padrão `true`. Um `false` neles, ou um
   `easyauth.*` negado num grupo, bloqueia todos. Nunca negar esses nós no grupo
   `default`.
2. **Curinga liberando demais.** `banhammer.*` em `true` libera
   `banhammer.can_ban_admins` e `banhammer.ignoreTempLimit` junto com o resto, e o
   LuckPerms resolve curingas. Liberar nós explícitos.
3. **`command.*` negado não cobre nós de mod.** O `default` do servidor nega
   `command.*`, mas esse curinga só atinge nós que começam com `command.`.
   `banhammer.duration.*` e semelhantes continuam `DEFAULT` (portanto, do mod).
4. **Console.** Console, bloco de comando e RCON ficam sem opinião: continuam
   valendo o padrão do mod e o nível 4 do console.
5. **Jogador ainda sem usuário carregado** (login em andamento): sem opinião.
6. **Consulta offline bloqueia quem chama.** `BHUtils.isPunishableBy` chama
   `.get()` no futuro offline; a consulta lê o usuário do armazenamento do
   LuckPerms se ele não estiver em memória. É chamada de dentro de
   `CompletableFuture.runAsync` (`PunishCommands.punishCommand`), fora da thread
   do servidor.
7. **Erro de grafia em `styledchat`.** O mod monta `styledchat.format..type.<tipo>`
   (ponto duplo, `StyledChatUtils`); esse nó nunca é útil. Não é problema da ponte.
8. **Efeito só no que o jogador pode fazer a mais.** Como a ponte devolve
   `DEFAULT` quando nada está definido, ela nunca reabre o que o LuckPerms
   bloqueia hoje: o bloqueio atual vem de `command.*` e de `weight`/herança, que
   a ponte não toca.

As propostas de mudança de permissão no servidor não ficam aqui: este
repositório é público e os nós do servidor ficam no repositório privado de
configs.

## (f) Validação na prática

Servidor real do pack `3.9.0` (NeoForge 21.1.253, todos os mods de servidor), com o
mod `0.1.0`, LuckPerms em arquivos e o console. Resultados do
[teste de integração](../guias/teste-no-servidor.md):

| Conferência | Resultado |
| --- | --- |
| Módulo | `ENABLED: listening on 4 of 4 events` |
| Consumidores | Banhammer, Styled Chat, Styled Player List, Essential Commands e EasyAuth resolvem o **mesmo** `PermissionCheckEvent` que o mod (`Class.forName(evento, false, loaderDoMod)` igual à classe do mod) |
| Nó `true` / `false` / indefinido | `TRUE` / `FALSE` / `DEFAULT`, offline e para um jogador simulado |
| Herança de grupo e curinga | `banhammer.punish.ban` e `styledchat.format.*` herdados de um grupo valem |
| Opções | `styled_chat.display_name` → `Staff`; `prefix` → `[Mod]`; inexistente → vazio |
| Código real do Banhammer | `ConfigManager.getConfig().getDurationLimit(source)` devolve `2678400` (31 dias) com `banhammer.duration.example = true` no LuckPerms e "sem limite" (`Long.MAX_VALUE`) quando o nó é removido |

Achado: com a `fabric-permissions-api` 0.3.1, `OfflineOptionRequestEvent` lança
`NullPointerException` assim que há qualquer ouvinte (o combinador começa de
`CompletableFuture.completedFuture(null)` e o primeiro `thenCompose` lê
`value.isPresent()`). O código-fonte confirma o erro nas tags `v0.3.1` e `v0.3.3` e
a correção em `v0.4.1` (`completedFuture(Optional.empty())`). Nenhum mod do pack
usa essa consulta, então não há efeito hoje. O diagnóstico `/iunacompat option`
mostra a falha como `failed inside the API`.

Não coberto: um jogador conectado de verdade. O teste usa o jogador falso do
NeoForge; veja [o que não é coberto](../guias/teste-no-servidor.md#o-que-não-é-coberto).
