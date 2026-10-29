# Requisitos

## Problema

O LuckPerms para NeoForge atende a `PermissionAPI` do NeoForge e troca os
requisitos dos comandos por nós `command.*`. Ele não escuta os eventos da
`fabric-permissions-api` ([confirmação](confirmacoes-no-codigo.md#c-neoforge-nativo-ou-fabric-pelo-connector)).
Os mods Fabric do pack, que rodam pelo Sinytra Connector, consultam só esses
eventos. Resultado: nós como `banhammer.duration.1d`, `banhammer.ignoreTempLimit`,
`styledchat.*` e `styledplayerlist.*` não fazem efeito, e só o que o LuckPerms
injeta como nível 4 (`command.*`) funciona.

## Escopo

| # | Requisito | Situação |
| --- | --- | --- |
| R1 | Responder `PermissionCheckEvent` com o nó do LuckPerms do jogador, nos contextos atuais dele | feito |
| R2 | Responder `OptionRequestEvent` com meta, prefixo e sufixo | feito |
| R3 | Responder os eventos offline (`OfflinePermissionCheckEvent`, `OfflineOptionRequestEvent`) por UUID | feito; a consulta offline de opções falha dentro da API até a 0.3.3 (bug da própria API, corrigido na 0.4.1) |
| R4 | Console, bloco de comando, RCON e jogador sem usuário carregado: "sem opinião" | feito |
| R5 | O que o LuckPerms não define fica com o padrão do mod consultado | feito |
| R6 | Apresentar ao LuckPerms os nós conhecidos (autocompletar, `/lp tree`, editor web) sem conceder nada | feito |
| R7 | Estrutura em módulos independentes, para outras compatibilidades do pack | feito (`CompatModule`) |
| R8 | Configuração mínima para desligar o módulo e cada parte dele | feito |
| R9 | Diagnóstico para o administrador: o que o módulo faz e o que a API devolve | feito (`/iunacompat`) |

## Fora do escopo

- Conceder ou negar permissões: o mod só lê o que o LuckPerms já tem.
- Substituir o LuckPerms ou criar um sistema de permissões próprio.
- Mudar o nível de operador de comandos: isso é o `command.*` do LuckPerms.
- Cliente: o mod é só de servidor (`@Mod(dist = DEDICATED_SERVER)`).
- Corrigir bugs dos mods consultados (por exemplo, o nó `styledchat.format..type.*`
  com ponto duplo).

## Requisitos não funcionais

| Requisito | Como é atendido |
| --- | --- |
| Compatibilidade | Minecraft 1.21.1, NeoForge `[21.1.0,22)`, Java 21; LuckPerms 5.4 ou mais novo (API pública, compilada contra a 5.4) |
| Falha segura | Nada lança exceção para dentro de um mod Fabric: o erro é registrado e contado, e a resposta é "sem opinião" ([0003](../decisoes/0003-falha-segura-e-dependencias.md)) |
| Desempenho | Consulta de jogador online é uma leitura do cache do LuckPerms (sem alocação além da do próprio LuckPerms, sem E/S); a consulta offline lê do armazenamento só se o usuário não está em memória, fora da thread do servidor quando quem chama é assíncrono |
| Sem efeito colateral | Com o LuckPerms ou a API do Fabric ausentes, ou com a API num formato novo, o módulo fica parado e o servidor se comporta como sem o mod |
| Observabilidade | Log de início por módulo, contadores e `/iunacompat status`; `debug.logChecks` para registrar cada consulta |
| Manutenção | Teste de contrato contra a API publicada, teste de integração contra o pack, checagem semanal da API nova e do pack ([0006](../decisoes/0006-testes-e-deteccao-de-quebra.md)) |
| Privacidade | Nenhum dado sai do servidor; o repositório é público e não guarda host, caminho local nem segredo |

## Configuração

Arquivo `config/iunacompat-common.toml`. Chaves em [README](../../README.md#configuração).

## Logs

Logger do `slf4j` do Minecraft. Nível `INFO` no início de cada módulo; `WARN` para
diferenças no formato da API; `ERROR` com a pilha só na primeira falha do backend
(as seguintes viram `DEBUG` e contador, para não inundar o log).
