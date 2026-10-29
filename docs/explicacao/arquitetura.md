# Arquitetura e plano

## Como as peças se ligam

```text
 mod Fabric (Banhammer, Styled Chat, ...)          NeoForge + LuckPerms
          │                                               ▲
          │ Permissions.check(source, nó, padrão)         │ API pública
          ▼                                               │
 fabric-permissions-api (uma só cópia, via Connector)     │
   PermissionCheckEvent.EVENT.invoker()                   │
          │                                               │
          ▼ Proxy registrado por reflexão                 │
 FabricPermissionsApi ──► PermissionBridge ──► LuckPermsBackend
   (confere o formato)      (decide, conta,      (usuário em memória,
                             nunca lança)          contextos do jogador)
```

- `IunaCompat` é a entrada: registra a config e o comando e liga os módulos no
  `FMLCommonSetupEvent`, quando todos os mods (e as classes dos mods Fabric) já existem.
- `CompatModule` é a unidade de compatibilidade: `id`, `description`, `enable()`
  (sem lançar) e `status()`. Os estados são `ENABLED`, `DEGRADED`, `DISABLED`,
  `UNAVAILABLE` e `FAILED`.
- `PermissionsModule` é o primeiro módulo. Decide se há o que fazer (config,
  LuckPerms, API do Fabric), monta ponte e backend e registra os ouvintes.
- `FabricPermissionsApi` é o **único** lugar que conhece o formato da API do Fabric.
- `PermissionBridge` e `PermissionBackend` não conhecem tipos do Fabric; só o
  `LuckPermsBackend` conhece o LuckPerms, e só é carregado se ele existe.

## Fluxo de uma consulta

1. O mod chama `Permissions.check(source, "banhammer.duration.1d")`.
2. A API invoca o `EVENT`; o `Proxy` chama `PermissionBridge.check`.
3. A ponte resolve a fonte: entidade `ServerPlayer` → jogador; qualquer outra
   coisa → sem opinião.
4. O backend pega o usuário do LuckPerms já em memória, as opções de consulta com
   os contextos atuais do jogador e lê o cache de permissões (`checkPermission`).
5. `TRUE`/`FALSE`/`DEFAULT` volta como o `TriState` da API. `DEFAULT` faz o mod
   usar o padrão dele.

## Plano de execução

| Passo | Resultado | Situação |
| --- | --- | --- |
| 1 | Confirmar no código a API, o Connector, o LuckPerms e o que cada mod consome | feito ([confirmações](../referencia/confirmacoes-no-codigo.md)) |
| 2 | Requisitos, decisões e este plano | feito |
| 3 | Projeto Gradle, módulo `permissions`, config, comando, testes | feito |
| 4 | CI (validar, integração, contrato, docs) e release assinada | feito |
| 5 | Repositório `IunaMC/compat`, primeira release e integração ao pack | ver [publicar](../guias/publicar-versao.md) e [entrar no pack](../guias/entrar-no-pack.md) |
| 6 | Propor os nós do LuckPerms que passam a valer (repositório privado de configs) | só proposta, fora deste repositório |

## Cadeia de manutenção

A ponte quebra por quatro motivos. Cada um tem um alarme.

| O que muda | Como aparece | Alarme |
| --- | --- | --- |
| A `fabric-permissions-api` (nova versão publicada) | O adaptador não reconhece o formato e deixa o módulo parado | Workflow *Contrato* semanal contra `latest.release` abre issue |
| A versão da API que os mods do pack embutem | Idem, ou outro objeto de evento | `FabricContractTest` (versão do pack) e `/iunacompat status` (cada mod mostra se usa o mesmo evento) |
| O pack (LuckPerms, Connector, mods Fabric, NeoForge) | Comportamento diferente que o teste unitário não vê | `pack-drift.py` semanal compara com `tested-with.json`; o *Integração* sobe o servidor |
| Minecraft ou NeoForge (novo major do pack) | Não compila (nomes de classe) ou o mod não carrega | `./gradlew build`; versão nova do mod com `mc<versão>` no nome |

O ciclo está no guia [atualizar com o pack](../guias/atualizar-com-o-pack.md).
