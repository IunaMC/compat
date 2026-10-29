# 0003: LuckPerms opcional e falha sem efeito colateral

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Contexto

O mod é executado dentro das checagens de permissão de outros mods, que ocorrem
a cada pacote de comandos e a cada execução de comando. Uma exceção nesse ponto
quebraria o comando, o chat ou o login. Além disso, o pack pode mudar, e o
LuckPerms ou os mods Fabric podem ser removidos.

## Decisão

- **O LuckPerms é uma dependência opcional** (`type = "optional"`,
  `ordering = "AFTER"`, `[5.4,)`). Sem ele, o módulo `permissions` informa
  `UNAVAILABLE` e não registra nada. O mesmo vale quando nenhum mod Fabric traz
  a API embutida (`absent`).
- **Formato novo da API significa módulo inativo e aviso.** O adaptador confere
  o nome, os parâmetros e o tipo de retorno de cada interface. Uma interface
  divergente é ignorada e listada entre os problemas; se a principal
  (`PermissionCheckEvent`) mudou, nada é registrado.
- **Nenhuma exceção chega a quem chama.** O `PermissionBridge` captura
  `Throwable`, contabiliza o erro, registra o *stack trace* apenas na primeira vez
  e responde "sem opinião" (`DEFAULT`, `Optional.empty()`), o que preserva o
  padrão do consumidor.
- **"Sem opinião" em vez de uma leitura bloqueante.** Um jogador online cujo
  usuário ainda não está em memória recebe "sem opinião" (como no LuckPerms para
  Fabric), em vez de provocar uma leitura de disco na thread do servidor.
- **O mod nunca concede nada.** Ele apenas reflete o que está no LuckPerms.

## Alternativas descartadas

- **Dependência obrigatória do LuckPerms.** O servidor deixaria de subir sem ele
  por causa de um módulo que só tem utilidade quando ele está presente.
- **Negar quando o *backend* falha** (`FALSE`). Uma falha do LuckPerms
  bloquearia todos os jogadores (por exemplo, `easyauth.commands.login` tem
  padrão `true`). Com "sem opinião", o comportamento volta a ser o de antes do
  mod.

## Consequências

- Os operadores do servidor veem o estado em `/iunacompat status` e no log de
  inicialização.
- Um bug do LuckPerms ou da API aparece como um contador de erros, e não como um
  comando que falha.
