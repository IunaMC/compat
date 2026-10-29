# Mudanças

Notas de cada versão, da mais recente para a mais antiga. O workflow Release usa
a seção da versão como texto da release. As versões seguem o
[SemVer](https://semver.org/).

## 0.1.0

Primeira versão.

- Módulo `permissions`: faz a ponte entre a `fabric-permissions-api` e o
  LuckPerms no NeoForge. Os mods Fabric do pack que rodam pelo Sinytra Connector
  (Banhammer, Styled Chat, Styled Player List, Essential Commands e EasyAuth)
  passam a ler os nós e as opções definidos no LuckPerms: `banhammer.duration.*`,
  `banhammer.ignoreTempLimit`, `styledchat.*`, `styled_chat.*` e os demais.
- Tudo o que o LuckPerms não define mantém o padrão do consumidor, de modo que
  instalar o mod não altera nada até que alguém defina um nó.
- Consultas sobre jogadores offline e consultas de opção, o arquivo
  `config/iunacompat-common.toml` para desligar cada parte e o registro dos nós
  conhecidos no LuckPerms (autocompletar e editor web).
- Comando de diagnóstico `/iunacompat` (`status`, `check`, `option`).
- Sem o LuckPerms ou sem a API do Fabric, o módulo permanece inativo e registra
  o motivo no log.
