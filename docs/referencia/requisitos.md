# Requisitos

## Problema

O LuckPerms para NeoForge responde à `PermissionAPI` do NeoForge e substitui os
requisitos dos comandos por nós `command.*`, mas não escuta os eventos da
`fabric-permissions-api` ([confirmação](confirmacoes-no-codigo.md#c-neoforge-nativo-ou-fabric-pelo-connector)).
Os mods Fabric do pack, que rodam pelo Sinytra Connector, consultam apenas esses
eventos. Por isso, nós como `banhammer.duration.1d`,
`banhammer.ignoreTempLimit`, `styledchat.*` e `styledplayerlist.*` não têm
efeito, e só funciona o que o LuckPerms injeta como nível 4 (`command.*`).

## Escopo

| # | Requisito | Estado |
| --- | --- | --- |
| R1 | Responder ao `PermissionCheckEvent` com o nó do jogador no LuckPerms, nos contextos atuais do jogador | feito |
| R2 | Responder ao `OptionRequestEvent` com meta, prefixo e sufixo | feito |
| R3 | Responder aos eventos offline (`OfflinePermissionCheckEvent`, `OfflineOptionRequestEvent`) pelo UUID | feito; a consulta de opção offline falha dentro da própria API até a 0.3.3 (um bug da API, corrigido na 0.4.1) |
| R4 | Console, bloco de comando, RCON e jogador sem usuário carregado recebem "sem opinião" | feito |
| R5 | Tudo o que o LuckPerms não define mantém o padrão do consumidor | feito |
| R6 | Registrar os nós conhecidos no LuckPerms (autocompletar, `/lp tree`, editor web) sem conceder nada | feito |
| R7 | Estrutura em módulos independentes, para acomodar outras correções de compatibilidade do pack | feito (`CompatModule`) |
| R8 | Configuração mínima para desligar o módulo e cada uma de suas partes | feito |
| R9 | Diagnóstico para o administrador: o que o módulo faz e o que a API devolve | feito (`/iunacompat`) |

## Fora do escopo

- Conceder ou negar permissões: o mod apenas lê o que já está no LuckPerms.
- Substituir o LuckPerms ou implementar um sistema de permissões próprio.
- Alterar o nível de operador dos comandos, que é papel dos nós `command.*` do
  LuckPerms.
- O cliente: o mod é exclusivo do servidor (`@Mod(dist = DEDICATED_SERVER)`).
- Corrigir bugs dos mods consultados (por exemplo, o nó
  `styledchat.format..type.*`, com ponto duplo).

## Requisitos não funcionais

| Requisito | Como é atendido |
| --- | --- |
| Compatibilidade | Minecraft 1.21.1, NeoForge `[21.1.0,22)`, Java 21; LuckPerms 5.4 ou mais recente (API pública, compilado contra a 5.4) |
| Falha segura | Nenhuma exceção é lançada dentro de um mod Fabric: o erro é registrado no log e contabilizado, e a resposta é "sem opinião" ([0003](../decisoes/0003-falha-segura-e-dependencias.md)) |
| Desempenho | A consulta sobre um jogador online é uma leitura do cache do LuckPerms (sem alocação além da que o próprio LuckPerms faz e sem E/S); a consulta offline só lê do armazenamento quando o usuário não está em memória e, se quem chama é assíncrono, fora da thread do servidor |
| Sem efeito colateral | Sem o LuckPerms, sem a API do Fabric ou com a API num formato novo, o módulo permanece inativo e o servidor se comporta como se o mod não estivesse instalado |
| Observabilidade | Uma linha de log por módulo na inicialização, contadores e `/iunacompat status`; `debug.logChecks` para registrar cada consulta |
| Manutenção | Teste de contrato contra a API publicada, teste de integração contra o pack e verificação semanal da versão mais recente da API e do pack ([0006](../decisoes/0006-testes-e-deteccao-de-quebra.md)) |
| Privacidade | Nenhum dado sai do servidor; o repositório é público e não guarda nomes de host, caminhos locais nem segredos |

## Configuração

O arquivo é `config/iunacompat-common.toml`, e as chaves estão listadas no
[README](../../README.md#configuração).

## Logs

É usado o logger `slf4j` do Minecraft, com nível `INFO` na inicialização de cada
módulo e `WARN` para diferenças no formato da API. O nível `ERROR`, com o
*stack trace*, aparece apenas na primeira falha do *backend*; as falhas
seguintes passam a `DEBUG` e a um contador, para que o log não seja inundado.
