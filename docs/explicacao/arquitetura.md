# Arquitetura e plano

## Como as peças se encaixam

```text
 mod Fabric (Banhammer, Styled Chat, ...)          NeoForge + LuckPerms
          │                                               ▲
          │ Permissions.check(source, node, default)      │ API pública
          ▼                                               │
 fabric-permissions-api (uma só cópia, via Connector)     │
   PermissionCheckEvent.EVENT.invoker()                   │
          │                                               │
          ▼ Proxy registrado por reflexão                 │
 FabricPermissionsApi ──► PermissionBridge ──► LuckPermsBackend
   (confere o formato)      (decide, conta,      (usuário em memória,
                             nunca lança)          contextos do jogador)
```

- `IunaCompat` é o ponto de entrada: registra a configuração e o comando e ativa
  os módulos no `FMLCommonSetupEvent`, momento em que todos os mods (e as classes
  dos mods Fabric) já existem.
- `CompatModule` é a unidade de compatibilidade, com `id`, `description`,
  `enable()` (que nunca lança exceção) e `status()`. Os estados possíveis são
  `ENABLED`, `DEGRADED`, `DISABLED`, `UNAVAILABLE` e `FAILED`.
- `PermissionsModule` é o primeiro módulo. Ele verifica se há algo a fazer
  (configuração, LuckPerms, API do Fabric), monta a ponte e o *backend* e
  registra os ouvintes.
- `FabricPermissionsApi` é o **único** ponto do código que conhece o formato da
  API do Fabric.
- `PermissionBridge` e `PermissionBackend` não conhecem nenhum tipo do Fabric, e
  apenas `LuckPermsBackend` conhece o LuckPerms; essa classe, por sua vez, só é
  carregada quando o LuckPerms está presente.

## Fluxo de uma consulta

1. O consumidor chama `Permissions.check(source, "banhammer.duration.1d")`.
2. A API invoca o `EVENT`, e o `Proxy` repassa a chamada para
   `PermissionBridge.check`.
3. A ponte identifica a origem da consulta: uma entidade `ServerPlayer` é tratada
   como jogador, e qualquer outra origem recebe "sem opinião".
4. O *backend* obtém o usuário do LuckPerms que já está em memória, monta as
   opções de consulta a partir dos contextos atuais do jogador e lê o cache de
   permissões (`checkPermission`).
5. O resultado `TRUE`, `FALSE` ou `DEFAULT` volta como o `TriState` da API. Com
   `DEFAULT`, o consumidor usa o próprio padrão.

## Plano de execução

| Etapa | Resultado | Estado |
| --- | --- | --- |
| 1 | Confirmar no código a API, o Connector, o LuckPerms e o que cada mod consome | feito ([confirmações](../referencia/confirmacoes-no-codigo.md)) |
| 2 | Requisitos, decisões e este plano | feito |
| 3 | Projeto Gradle, módulo `permissions`, configuração, comando e testes | feito |
| 4 | CI (validação, integração, contrato e documentação) e release assinada | feito |
| 5 | Repositório `IunaMC/compat`, primeira release e entrada no pack | ver [publicar](../guias/publicar-versao.md) e [entrar no pack](../guias/entrar-no-pack.md) |
| 6 | Propor os nós do LuckPerms que passam a ter efeito (repositório privado de configuração) | apenas proposta, fora deste repositório |

## Cadeia de manutenção

A ponte pode quebrar por quatro motivos, e cada um deles tem um alarme próprio.

| O que muda | Como se manifesta | Alarme |
| --- | --- | --- |
| `fabric-permissions-api` (nova versão publicada) | O adaptador não reconhece o formato e deixa o módulo inativo | O workflow semanal *Contrato* executa o teste contra `latest.release` e abre uma issue |
| A versão da API embutida nos mods do pack | O mesmo sintoma, ou um objeto de evento diferente | `FabricContractTest` (versão do pack) e `/iunacompat status`, que mostra se cada mod usa o mesmo evento |
| O pack (LuckPerms, Connector, mods Fabric, NeoForge) | Um comportamento diferente, que os testes unitários não enxergam | O `pack-drift.py` semanal compara o pack com o `tested-with.json`, e o *Integração* sobe o servidor |
| Minecraft ou NeoForge (nova versão major do pack) | O código não compila (nomes de classes) ou o mod não carrega | `./gradlew build`; uma nova versão do mod, com `mc<versão>` no nome |

O ciclo completo está descrito no guia [atualizar com o pack](../guias/atualizar-com-o-pack.md).
