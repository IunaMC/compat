# Confirmações no código

Esta página reúne o que foi lido no código antes de escrever o mod: os jars do
pack `3.9.0`, decompilados com o Fernflower, e, como segunda fonte, o código
aberto dos projetos. Nada aqui é suposição; onde o código não dá a resposta, a
linha indica **a confirmar**.

Versões lidas:

| Componente | Versão |
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

As classes dos mods Fabric aparecem com os nomes *intermediary* (`class_2172` é
`SharedSuggestionProvider`, `class_2168` é `CommandSourceStack` e `method_9259`
é `hasPermission`), porque é assim que o jar é publicado; o Connector faz o
remapeamento no carregamento.

## (a) A API embutida e o que cada mod consome

### O evento existe e é um evento da API do Fabric

`me.lucko.fabric.api.permissions.v0.PermissionCheckEvent` na versão 0.3.1:

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
assinatura, com `net.minecraft.command.CommandSource` nos nomes Yarn. Quem decide
é o primeiro ouvinte que não devolve `DEFAULT`.

As outras três interfaces da mesma versão:

| Interface | Método | Tipo de retorno |
| --- | --- | --- |
| `OptionRequestEvent` | `onOptionRequest(class_2172, String)` | `Optional<String>` |
| `OfflinePermissionCheckEvent` | `onPermissionCheck(UUID, String)` | `CompletableFuture<TriState>` |
| `OfflineOptionRequestEvent` | `onOptionRequest(UUID, String)` | `CompletableFuture<Optional<String>>` |

### `Permissions.check(source, node, level)` é o evento, com o nível como alternativa

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

Ou seja, sem nenhum ouvinte (ou com todos devolvendo `DEFAULT`), vale o padrão
do mod, que é um booleano ou `source.hasPermission(level)`. `Options.get` e as
consultas por UUID usam os outros três eventos.

### O que cada mod do pack consome

Foram varridos **todos** os jars do pack que rodam no servidor (310 arquivos
conferidos pelo hash). Os 19 que não têm `url` no `.pw.toml`, por serem
`metadata:curseforge`, ficaram de fora, e nenhum deles é um mod Fabric, a julgar
pelo nome. Apenas os mods abaixo referenciam `me/lucko/fabric/api/permissions`:

| Mod | Chamadas (classe: trecho) |
| --- | --- |
| Banhammer | `ConfigManager.requirePermissionOrOp`: `Permissions.check(player, permission, CONFIG.configData.defaultOpPermissionLevel)`. `Config.getDurationLimit`: `Permissions.check(source, "banhammer.duration." + key)`. `PunishCommands`: `Permissions.check(source, "banhammer.ignoreTempLimit", 2)` e `"banhammer.seesilent", 1`. `BanHammerImpl:189`: `Permissions.check(player, "banhammer.seesilent", 3)`. `BHUtils.isPunishableBy`: `Permissions.check(source, "banhammer.can_ban_admins") \|\| !Permissions.check(profile, "banhammer.block_punishments").get()` (esta usa o evento offline). `GeneralCommands`: `Permissions.require("banhammer.commands.main", true)` e `reload`/`import`/`export`/`list` no nível 4 |
| Styled Chat | `StyledChatUtils`: `Permissions.check(source, "styledchat.format." + tag, safeUser ? 2 : 4)` (e `unsafe_format.`), `"styledchat.format.spoiler", 2`. `Commands`: `styledchat.main` (true), `reload` (3), `set` (2), `get` (2), `clear` (3), `tellform` (2). `ChatStyleData:55`: `Options.get(source, "styled_chat." + property)` |
| Styled Player List | `Commands`: `styledplayerlist.main` e `switch` (true), `switch.others` (2), `reload` (3) |
| Essential Commands | `ECPerms.check`: `Permissions.getPermissionValue(source, node).orElse(source.hasPermission(Math.max(2, level)))`, apenas se `use_permissions_api=true` (está ligado nas configurações); a lista de nós está em `ECPerms.Registry` |
| EasyAuth | `integrations/Permissions`: `Permissions.check(source, node, default)`, apenas se `permissionsLoaded` (o mod `fabric-permissions-api-v0` está carregado). São 19 nós `easyauth.commands.*`, com nível 3/4 para os de administração e `true` para login, logout, register e account |

A `LinguaBib` (embutida no Flan) importa `Permissions`, mas o campo
`LinguaBib.permissionAPI` nunca recebe valor na variante NeoForge (não há
nenhuma atribuição a ele no jar lido), de modo que essa integração não tem
efeito. Os mods NeoForge (Balm, ServerCore, Mekanism, Simple Voice Chat, Spark,
Waterframes, Nature's Compass e Fzzy Config) usam a `PermissionAPI` do NeoForge,
à qual o LuckPerms já responde.

## (b) Como o Connector carrega esses jars

- Cada mod Fabric traz a API em `META-INF/jars/fabric-permissions-api-0.3.1.jar`
  (Banhammer, Styled Chat, Styled Player List e, dentro do `ec-core`, Essential
  Commands). Os quatro arquivos têm o mesmo SHA-1.
- O `ConnectorLocator.locateFabricMods` descobre os jars aninhados
  (`discoverNestedJarsRecursive`) e resolve tudo com o `ModResolver` do Fabric
  Loader (`DependencyResolver.resolveDependencies`): **uma instância por id de
  mod**, a de versão mais alta e, em caso de empate, a menos aninhada
  (`modPrioComparator`).
- Na inicialização do servidor de teste, o `logs/debug.log` confirmou que os
  quatro jars são transformados, mas apenas **um** é carregado:

  ```text
  Found valid mod file banhammer-0.11.1+1.21$fabric-permissions-api-0.3.1_mapped_moj_1.21.1.jar with {fabric_permissions_api_v0} mods - versions {0.3.1}
  ```

  No lado do NeoForge, o id do mod é `fabric_permissions_api_v0`. Portanto, o
  evento é um único objeto para todos os mods, e um ouvinte registrado nele vale
  para os cinco.
- O jar é remapeado para os nomes da Mojang (`_mapped_moj_1.21.1`), de modo que,
  em tempo de execução, a interface recebe `SharedSuggestionProvider`, e não
  `class_2172`.
- O `FabricModsDiscoverer.isFabricModJar` ignora qualquer jar que tenha
  `META-INF/neoforge.mods.toml`. É por isso que o mod da IUNA é NeoForge puro,
  sem `fabric.mod.json`.

## (c) NeoForge nativo ou Fabric pelo Connector

- **Um mod Fabric carregado pelo Connector consegue acessar a API do LuckPerms
  para NeoForge.** O EasyAuth já faz isso:
  `xyz.nikitacartes.easyauth.integrations.LuckPermsIntegration` chama
  `LuckPermsProvider.get().getContextManager().registerCalculator(...)`. O
  `inv_view` (NeoForge) usa a mesma API em `util/PermissionHandler`.
- **O caminho da `PermissionAPI`/`PermissionGatherEvent` do NeoForge não resolve
  o problema.** O LuckPerms o implementa (`NeoForgePermissionHandler`,
  `NeoForgePermissionHandlerListener.onPermissionGatherHandler`), mas os mods
  Fabric nunca o consultam, porque chamam os eventos do Fabric.
- **O LuckPerms para NeoForge não escuta o evento do Fabric.** No jar interno do
  LuckPerms para NeoForge (`luckperms-neoforge.jarinjar`), nenhuma classe menciona
  `me.lucko.fabric` e nenhum arquivo tem `fabric` no nome. O
  `PermissionCheckEvent` que aparece nos fontes é do próprio LuckPerms
  (`common.verbose.event`), uma classe sem relação com a da API. O ouvinte do
  Fabric existe apenas no LuckPerms para **Fabric**
  (`FabricPermissionsApiV0Listener`).
- **O que o LuckPerms para NeoForge faz com os comandos** (`BrigadierInjector`):
  substitui o `requirement` de cada nó por `InjectedPermissionRequirement`:

  ```java
  Tristate state = user.getCachedData().getPermissionData(queryOptions).checkPermission(this.permission);
  if (state != Tristate.UNDEFINED) {
     return state.asBoolean() && this.delegate.test(source.withPermission(4));
  }
  return this.delegate.test(source);
  ```

  Com o nó `command.<path>` concedido, o requisito original do mod é avaliado no
  **nível 4**. Por isso, `Permissions.require("banhammer.commands.reload", 4)`
  deixa passar qualquer um que tenha `command.banhammer.reload`. Fora do
  requisito, ou seja, no momento da execução, o `source` volta ao nível real do
  jogador, e é nesse momento que `banhammer.ignoreTempLimit` e
  `banhammer.duration.*` são verificados.
- **Escolha:** um mod NeoForge nativo, com a API do Fabric acessada por reflexão
  e o LuckPerms pela API pública. A justificativa e as alternativas estão na
  [0002](../decisoes/0002-mod-neoforge-nativo-com-reflexao.md).

## (d) A lógica de referência do LuckPerms para Fabric

O `FabricPermissionsApiV0Listener` (repositório `LuckPerms/LuckPerms`, branch
`master`; a tag `v5.4` não tem o arquivo nesse caminho, então a versão exata
correspondente à 5.4.150 está **a confirmar**) registra os quatro eventos e
segue estas regras:

| Consulta | Regra |
| --- | --- |
| Origem com uma entidade `ServerPlayer` | `((MixinUser) player).luckperms$hasPermission(node)`: usuário já em memória, contextos atuais do jogador, `CheckOrigin.PLATFORM_API_HAS_PERMISSION` |
| Console e outras origens | `TriState.DEFAULT`, mas o nó é registrado no *permission registry* e no *verbose* (`offerPermissionCheckEvent`) |
| Jogador sem usuário carregado (jogador falso) | `Tristate.UNDEFINED` (`ServerPlayerMixin.luckperms$hasPermission`: "fake players will have our mixin, but won't have been initialised") |
| UUID offline | `lookupUser`: `getIfLoaded` ou `storage.loadUser(uuid, null)` e, em seguida, `getCachedData().getPermissionData()` |
| Opções | `getMetaOrChatMetaValue(key)`: `prefix` e `suffix` são meta de chat; todo o resto é meta comum |
| `UNDEFINED` → `DEFAULT`, `TRUE` → `TRUE`, `FALSE` → `FALSE` | `fabricTristate` |

O que o NeoForge tem em comum com essa lógica (`LuckPerms-NeoForge-5.4.150`
decompilado):

- `NeoForgeContextManager extends InlineContextManager<ServerPlayer, ServerPlayer>`:
  o `ContextManager.getQueryOptions(Object)` da **API pública** aceita um
  `ServerPlayer` e devolve as opções de consulta com o contexto atual do jogador.
- O `PermissionCalculator.apply` chama `plugin.getPermissionRegistry().offer(node)`
  em toda consulta, qualquer que seja a resposta. Assim, consultar pela API
  pública já alimenta o autocompletar, o `/lp tree` e o editor web. O *verbose*
  também registra a consulta (`PermissionCalculator.checkPermission` →
  `offerPermissionCheckEvent`); pela API pública, a origem aparece como
  `LUCKPERMS_API`.
- O `PermissionHolder.getCachedData()` também existe para grupos, de modo que os
  nós conhecidos podem ser registrados por meio de uma consulta ao grupo
  `default`, sem criar nada.
- No cálculo, o nó é sempre convertido para minúsculas (`apply` chama
  `toLowerCase`). Por isso, nós como `easyauth.commands.account.changePassword`
  correspondem ao que está gravado no LuckPerms.

## (e) Efeitos de ligar a ponte

### O que muda

A ponte devolve `DEFAULT` para tudo o que o LuckPerms não define, de modo que
só muda o que estiver **definido**. Nas configurações do servidor que foram
lidas (os grupos do LuckPerms no repositório privado de configuração), nenhum
grupo ou usuário define nós `banhammer.*`, `styledchat.*`, `styledplayerlist.*`,
`essentialcommands.*` ou `easyauth.*`, apenas `command.*`. Portanto, ligar a
ponte **não altera nenhum comportamento** até que alguém defina um desses nós.

### Nós por mod (o que cada um faz quando definido)

| Nó | Padrão do mod | Efeito de defini-lo |
| --- | --- | --- |
| `banhammer.duration.<key>` | nenhum: vale `defaultTempPunishmentDurationLimit` | `true` concede o limite dessa chave (`permissionTempLimit` no `config.json` do Banhammer; `defaultTempPunishmentDurationLimit = "-1"` vira `Long.MAX_VALUE`, sem limite); vale o maior limite concedido |
| `banhammer.ignoreTempLimit` | nível 2 (na execução, o nível real do jogador: 0) | `true` ignora o limite de duração |
| `banhammer.can_ban_admins` | `false` | `true` permite punir jogadores de nível mais alto que o seu |
| `banhammer.block_punishments` | `false` (consulta offline) | `true` no alvo impede que ele seja punido |
| `banhammer.seesilent` | nível 1 ou 3 | `true` mostra as punições silenciosas |
| `banhammer.punish.*`, `banhammer.unpunish.*` | `defaultOpPermissionLevel` (4 na configuração do servidor) | `false` bloqueia o comando mesmo com `command.*` concedido |
| `banhammer.commands.*` | `main` true, os demais nível 4 | idem |
| `styledchat.format.<tag>`, `styledchat.unsafe_format.<tag>` | nível 2 (tags seguras) ou 4 | `true` permite a tag de formatação no chat |
| `styledchat.main/set/get/clear/reload/tellform` | true / 2 / 2 / 3 / 3 / 2 | idem |
| `styledplayerlist.*` | true / true / 2 / 3 | idem |
| `essentialcommands.*` | `hasPermission(max(2, level))` | `false` nega; os nós de limite numérico (`...home.limit.<n>` e semelhantes) passam a ter efeito |
| `easyauth.commands.*` | true (jogador) ou 3/4 (administração) | `false` em `login`/`register` **impede o jogador de entrar** |

### Riscos e como evitá-los

1. **Negar um nó com padrão `true` vira bloqueio.** `easyauth.commands.login`,
   `easyauth.commands.register`, `styledchat.main`, `styledplayerlist.main` e
   `banhammer.commands.main` têm padrão `true`. Um `false` neles, ou um
   `easyauth.*` negado num grupo, bloqueia todo mundo. Esses nós nunca devem ser
   negados no grupo `default`.
2. **Curinga que concede demais.** `banhammer.*` com `true` concede
   `banhammer.can_ban_admins` e `banhammer.ignoreTempLimit` junto com todo o
   resto, já que o LuckPerms resolve curingas. Recomenda-se conceder nós
   explícitos.
3. **Um `command.*` negado não cobre os nós dos mods.** O grupo `default` do
   servidor nega `command.*`, mas esse curinga só alcança nós que começam com
   `command.`. `banhammer.duration.*` e nós semelhantes continuam `DEFAULT`, ou
   seja, quem decide é o mod.
4. **Console.** Console, blocos de comando e RCON recebem "sem opinião", de modo
   que continuam valendo o padrão do mod e o nível 4 do console.
5. **Jogador cujo usuário ainda não foi carregado** (login em andamento): recebe
   "sem opinião".
6. **A consulta offline bloqueia quem chama.** `BHUtils.isPunishableBy` chama
   `.get()` no *future* offline, e a consulta lê o usuário do armazenamento do
   LuckPerms se ele não estiver em memória. Como a chamada é feita dentro de
   `CompletableFuture.runAsync` (`PunishCommands.punishCommand`), ela ocorre fora
   da thread do servidor.
7. **Erro de digitação no `styledchat`.** O mod monta
   `styledchat.format..type.<type>` (com ponto duplo, em `StyledChatUtils`), e
   esse nó nunca tem utilidade. Não se trata de um problema da ponte.
8. **Efeito apenas sobre o que um jogador pode fazer a mais.** Como a ponte
   devolve `DEFAULT` quando nada está definido, ela nunca reabre o que o
   LuckPerms bloqueia hoje: o bloqueio atual vem do `command.*` e do
   `weight`/herança, que a ponte não toca.

As mudanças de permissão propostas para o servidor não ficam aqui, porque este
repositório é público e os nós do servidor ficam no repositório privado de
configuração.

## (f) Validação na prática

O teste foi feito no servidor real do pack, versão `3.9.0` (NeoForge 21.1.253,
com todos os mods de servidor), com o mod `0.1.0`, o LuckPerms com armazenamento
em arquivo e o console. Resultados do
[teste de integração](../guias/teste-no-servidor.md):

| Verificação | Resultado |
| --- | --- |
| Módulo | `ENABLED: listening on 4 of 4 events` |
| Consumidores | Banhammer, Styled Chat, Styled Player List, Essential Commands e EasyAuth resolvem o **mesmo** `PermissionCheckEvent` que o mod (`Class.forName(event, false, modLoader)` é igual à classe do mod) |
| Nó `true` / `false` / indefinido | `TRUE` / `FALSE` / `DEFAULT`, offline e para um jogador simulado |
| Herança de grupo e curinga | `banhammer.punish.ban` e `styledchat.format.*` herdados de um grupo têm efeito |
| Opções | `styled_chat.display_name` → `Staff`; `prefix` → `[Mod]`; ausente → vazio |
| Código real do Banhammer | `ConfigManager.getConfig().getDurationLimit(source)` devolve `2678400` (31 dias) com `banhammer.duration.example = true` no LuckPerms e "sem limite" (`Long.MAX_VALUE`) quando o nó é removido |

Achado: com a `fabric-permissions-api` 0.3.1, o `OfflineOptionRequestEvent`
lança `NullPointerException` assim que qualquer ouvinte é registrado, porque o
combinador parte de `CompletableFuture.completedFuture(null)` e o primeiro
`thenCompose` lê `value.isPresent()`. O código-fonte confirma o bug nas tags
`v0.3.1` e `v0.3.3` e a correção na `v0.4.1`
(`completedFuture(Optional.empty())`). Nenhum mod do pack usa essa consulta,
então o bug não tem efeito hoje. O diagnóstico `/iunacompat option` informa a
falha como `failed inside the API`.

Fica de fora a validação com um jogador de fato conectado, já que o teste usa o
jogador falso do NeoForge; veja
[o que o teste não cobre](../guias/teste-no-servidor.md#o-que-o-teste-não-cobre).
