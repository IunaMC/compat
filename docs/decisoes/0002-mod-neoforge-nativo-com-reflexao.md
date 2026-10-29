# 0002: mod NeoForge nativo, com a API do Fabric acessada por reflexão

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório; evidências nas
  [confirmações no código](../referencia/confirmacoes-no-codigo.md)

## Contexto

Há dois caminhos para chegar à mesma ponte com o LuckPerms:

1. um mod **Fabric** carregado pelo Connector, compilado com o Loom contra a
   `fabric-permissions-api`, como o EasyAuth faz com o LuckPerms;
2. um mod **NeoForge** nativo.

Ficou confirmado no código que um mod Fabric carregado pelo Connector consegue
acessar a API do LuckPerms para NeoForge, que o Connector ignora qualquer jar que
tenha um `neoforge.mods.toml` e que os eventos são um único objeto compartilhado
([b](../referencia/confirmacoes-no-codigo.md#b-como-o-connector-carrega-esses-jars)).

## Decisão

O mod é NeoForge nativo (`javafml`, `@Mod(dist = DEDICATED_SERVER)`), compilado
com Gradle e ModDevGradle. A `fabric-permissions-api` **não é dependência de
compilação**: o adaptador (`FabricPermissionsApi`) a lê por reflexão, confere o
formato das quatro interfaces e registra um `java.lang.reflect.Proxy` em cada
evento. O LuckPerms entra como dependência `compileOnly` da API pública 5.4 e
como dependência opcional.

As razões são as seguintes:

- Há uma única cadeia de ferramentas (NeoForge) e um único jar para o
  Minecraft 1.21.1, de modo que atualizar o mod no pack é igual a atualizar
  qualquer mod NeoForge.
- Não há Loom, mapeamentos Yarn/intermediary nem dependência da versão exata da
  API do Fabric embutida nos mods (hoje a 0.3.1, amanhã outra).
- Se a API desaparecer ou mudar, o mod continua carregando e explica o motivo:
  uma divergência de formato vira um aviso no log e deixa o módulo inativo, em
  vez de provocar um `NoClassDefFoundError`.
- O teste de integração comprova o funcionamento no servidor real do pack, com o
  próprio código do Banhammer lendo um nó do LuckPerms.

## Alternativas descartadas

- **Mod Fabric pelo Connector.** Exige o Loom, depende de a transformação do
  Connector (mapeamentos, mixins) continuar aceitando o jar e amarra o nosso
  ciclo de releases ao do Connector. Além disso, não traz benefício, porque o
  evento já é acessível pelo lado do NeoForge.
- **Mixin em `Permissions.check`.** É mais invasivo e quebraria a cada versão dos
  mods; o evento é o ponto de extensão previsto pela API.
- **`PermissionAPI` / `PermissionGatherEvent` do NeoForge.** O LuckPerms já os
  implementa, mas os mods Fabric nunca os consultam.
- **Compilar contra a API do Fabric com *stubs* próprios.** Os tipos teriam de ser
  mantidos em sincronia à mão e, mesmo assim, continuariam falhando em tempo de
  execução se mudassem. A reflexão, somada ao teste de contrato que lê o jar
  real, dá o mesmo aviso sem exigir a manutenção de uma cópia.
- **Substituir os mods Fabric por equivalentes NeoForge.** Fora do escopo; o
  Banhammer e o Styled Chat não têm equivalente com o mesmo comportamento.

## Consequências

- A reflexão tem custo de manutenção, razão pela qual o formato esperado fica
  num único lugar (`FabricPermissionsApi`) e num teste que lê o jar publicado
  ([0006](0006-testes-e-deteccao-de-quebra.md)).
- Os objetos `Proxy` são criados uma única vez, na inicialização; cada consulta é
  uma chamada de interface comum, sem reflexão.
