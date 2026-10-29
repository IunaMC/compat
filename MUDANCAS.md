# Mudanças

Notas de cada versão, da mais nova para a mais antiga. O workflow Release usa a
seção da versão como texto da release. Versões seguem o
[SemVer](https://semver.org/lang/pt-BR/).

## 0.1.0

Primeira versão.

- Módulo `permissions`: liga a `fabric-permissions-api` ao LuckPerms no NeoForge.
  Os mods Fabric do pack que rodam pelo Sinytra Connector (Banhammer, Styled
  Chat, Styled Player List, Essential Commands e EasyAuth) passam a ler os nós e
  as opções definidos no LuckPerms: `banhammer.duration.*`,
  `banhammer.ignoreTempLimit`, `styledchat.*`, `styled_chat.*` e os demais.
- O que o LuckPerms não define continua com o padrão do mod consultado: ligar o
  mod não muda nada até alguém gravar um nó.
- Consultas de jogadores offline e de opções, `config/iunacompat-common.toml`
  para desligar cada parte, e apresentação dos nós conhecidos ao LuckPerms
  (autocompletar e editor web).
- Comando de diagnóstico `/iunacompat` (`status`, `check`, `option`).
- Sem o LuckPerms ou sem a API do Fabric o módulo fica parado e avisa no log.
