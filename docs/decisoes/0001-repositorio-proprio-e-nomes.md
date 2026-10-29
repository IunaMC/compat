# 0001: repositório próprio, nome, identificador, pacote, licença e módulos

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Contexto

Os mods Fabric do pack (Banhammer, Styled Chat, Styled Player List, Essential
Commands e EasyAuth) consultam a `fabric-permissions-api`, à qual o LuckPerms
para NeoForge não responde. Uma busca na web não encontrou nenhuma ponte pronta.
Como o pack já inclui mods de compatibilidade de terceiros (`*-compat.pw.toml`),
o objetivo passou a ser uma camada própria, que cresça conforme as necessidades
do pack.

## Decisão

| Item | Escolha |
| --- | --- |
| Repositório | `IunaMC/compat`, público, com `main` e `devel/devel`, como os demais repositórios de código |
| Nome do mod | IUNA Compat |
| Identificador (`modId`) | `iunacompat` |
| Pacote Java | `club.iuna.compat` (a partir do domínio `iuna.club`, da comunidade), com um subpacote por módulo (`club.iuna.compat.permissions`) |
| Licença | MIT, a mesma do `modpack` |
| Escopo | Camada geral de compatibilidade, apenas no servidor. O primeiro módulo é o `permissions`; cada nova correção é um novo módulo, sem dependências entre módulos |
| Versões | Minecraft 1.21.1, NeoForge `[21.1.0,22)`, Java 21 |

O nome genérico (`compat`), em vez de `permissions-bridge`, é intencional: o
dono do projeto quer uma camada geral para o pack, e o `permissions` é apenas o
primeiro módulo. Em contrapartida, o nome é menos descritivo, e cabe ao README e
ao `/iunacompat status` explicar o que cada módulo faz.

## Alternativas descartadas

- **`iunamc.permissions-bridge` (um mod por correção).** Cada correção futura
  exigiria outro repositório, outro jar e outra entrada no pack, repetindo toda
  a cadeia de CI e de release.
- **Pacote `io.github.iunamc`.** O projeto tem domínio próprio, e o pacote o
  acompanha.
- **Apache-2.0 ou GPL.** Não há motivo para destoar das demais licenças da
  IunaMC.
- **Contribuir com um ouvinte para o próprio LuckPerms.** O LuckPerms para
  NeoForge não carrega a `fabric-permissions-api` por decisão de projeto, já que
  a API só existe no pack por causa do Connector. Seria um pedido de longo prazo,
  fora do nosso controle; ainda assim, pode ser feito em paralelo, sem que este
  mod dependa dele.

## Consequências

- O pack passa a ter um mod próprio, mantido aqui. As regras de manutenção estão
  no guia [atualizar com o pack](../guias/atualizar-com-o-pack.md).
- Um novo módulo consiste em uma classe que implementa `CompatModule` e uma
  linha em `IunaCompat`; veja o guia [adicionar um módulo](../guias/adicionar-modulo.md).
