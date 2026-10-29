# 0003: LuckPerms opcional e falha sem efeito

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Contexto

O mod roda no meio de checagens de permissão de outros mods, que acontecem a
cada pacote de comandos e a cada execução. Uma exceção ali derrubaria o
comando, o chat ou o login. O pack também pode mudar: o LuckPerms ou os mods
Fabric podem sair.

## Decisão

- **LuckPerms é dependência opcional** (`type = "optional"`, `ordering = "AFTER"`,
  `[5.4,)`). Sem ele, o módulo `permissions` informa `UNAVAILABLE` e não registra nada.
  O mesmo vale quando nenhum mod Fabric embute a API (`absent`).
- **Formato novo da API = módulo parado e aviso.** O adaptador confere nome,
  parâmetros e retorno de cada interface. Uma interface diferente é ignorada e
  listada nos problemas; se a principal (`PermissionCheckEvent`) mudou, nada é registrado.
- **Nunca lançar para dentro do chamador.** `PermissionBridge` captura `Throwable`,
  conta o erro, registra a pilha só na primeira vez e responde "sem opinião"
  (`DEFAULT`, `Optional.empty()`), que mantém o padrão do mod consultado.
- **Sem opinião em vez de leitura bloqueante.** Jogador online cujo usuário
  ainda não está em memória recebe "sem opinião" (como no LuckPerms Fabric), em
  vez de uma leitura de disco na thread do servidor.
- **O mod nunca concede nada.** Ele só reflete o que o LuckPerms tem.

## Alternativas descartadas

- **Dependência obrigatória do LuckPerms.** O servidor não subiria sem ele,
  por causa de um módulo que só ajuda com ele.
- **Negar quando o backend falha** (`FALSE`). Uma falha do LuckPerms trancaria
  todos fora (por exemplo, `easyauth.commands.login` tem padrão `true`). "Sem
  opinião" devolve o comportamento de antes do mod.

## Consequências

- Quem opera o servidor vê o estado em `/iunacompat status` e no log de início.
- Um bug do LuckPerms ou da API aparece como contador de erros, não como
  falha de comando.
