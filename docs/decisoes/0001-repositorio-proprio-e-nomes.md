# 0001: repositório próprio, nome, identificador, pacote, licença e módulos

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Contexto

Os mods Fabric do pack (Banhammer, Styled Chat, Styled Player List, Essential
Commands, EasyAuth) consultam a `fabric-permissions-api`, e o LuckPerms NeoForge
não a atende. A busca na web não achou uma ponte pronta. O pack já tem mods de
compatibilidade de terceiros (`*-compat.pw.toml`), e a ideia é ter uma camada
própria, que cresça com as necessidades do pack.

## Decisão

| Item | Escolha |
| --- | --- |
| Repositório | `IunaMC/compat`, público, `main` e `devel/devel` como os outros repositórios com código |
| Nome do mod | IUNA Compat |
| Identificador (`modId`) | `iunacompat` |
| Pacote Java | `club.iuna.compat` (domínio `iuna.club` da comunidade), um subpacote por módulo (`club.iuna.compat.permissions`) |
| Licença | MIT, igual ao `modpack` |
| Escopo | Camada de compatibilidade geral, só de servidor. O primeiro módulo é `permissions`; cada outra correção é um módulo novo, sem dependência entre eles |
| Versões | Minecraft 1.21.1, NeoForge `[21.1.0,22)`, Java 21 |

O nome genérico (`compat`) em vez de `permissions-bridge` é de propósito: o
usuário quer uma camada geral para o pack, e o módulo `permissions` é o
primeiro. O custo é um nome menos descritivo; o README e o `/iunacompat status`
dizem o que cada módulo faz.

## Alternativas descartadas

- **`iunamc.permissions-bridge` (um mod por correção).** Cada correção futura
  viraria outro repositório, outro jar e outra entrada no pack, com a mesma
  cadeia de CI e release repetida.
- **Pacote `io.github.iunamc`.** O projeto tem domínio próprio; o pacote segue o
  domínio.
- **Apache-2.0 ou GPL.** Sem motivo para diferir das outras licenças da IunaMC.
- **Contribuir com um ouvinte no próprio LuckPerms.** O LuckPerms NeoForge não
  carrega a `fabric-permissions-api` por desenho: ela só existe no pack por causa
  do Connector. Seria um pedido de longo prazo, fora do nosso controle. Pode ser
  feito em paralelo, sem depender dele.

## Consequências

- O pack passa a ter um mod próprio, mantido aqui. As regras de manutenção estão
  em [atualizar com o pack](../guias/atualizar-com-o-pack.md).
- Um módulo novo é uma classe que implementa `CompatModule` e uma linha em
  `IunaCompat`; veja [adicionar um módulo](../guias/adicionar-modulo.md).
