# 0002: mod NeoForge nativo, API do Fabric por reflexão

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório; evidências em
  [confirmações no código](../referencia/confirmacoes-no-codigo.md)

## Contexto

Dois caminhos levam a mesma ponte ao LuckPerms:

1. mod **Fabric** carregado pelo Connector, compilado com Loom contra a
   `fabric-permissions-api`, como o EasyAuth faz com o LuckPerms;
2. mod **NeoForge** nativo.

Confirmado no código: um mod Fabric pelo Connector alcança a API do LuckPerms
NeoForge; o Connector ignora jar que tenha `neoforge.mods.toml`; os eventos são
um só objeto compartilhado ([b](../referencia/confirmacoes-no-codigo.md#b-como-o-connector-carrega-esses-jars)).

## Decisão

Mod NeoForge nativo (`javafml`, `@Mod(dist = DEDICATED_SERVER)`), Gradle com
ModDevGradle. A `fabric-permissions-api` **não é dependência de compilação**: o
adaptador (`FabricPermissionsApi`) a lê por reflexão, confere o formato das
quatro interfaces e registra `java.lang.reflect.Proxy` nos eventos. O LuckPerms
entra como `compileOnly` da API pública 5.4 e como dependência opcional.

Motivos:

- Um só ferramental (NeoForge) e um só jar para o Minecraft 1.21.1; a atualização
  do pack é a de um mod NeoForge comum.
- Sem Loom, sem mapeamento Yarn/intermediary, sem depender da versão exata da
  API do Fabric que os mods embutem (a 0.3.1 hoje, outra amanhã).
- O mod continua carregando, e diz por quê, se a API sumir ou mudar: o erro de
  formato vira aviso no log e o módulo fica parado, em vez de `NoClassDefFoundError`.
- A prova de que funciona está no teste de integração: o servidor real do pack,
  com o código do Banhammer lendo um nó do LuckPerms.

## Alternativas descartadas

- **Mod Fabric pelo Connector.** Exige Loom, depende de a transformação do
  Connector (mapeamentos, mixins) continuar aceitando o jar e acopla o nosso
  ciclo ao dele. Não traz nenhum ganho: o evento já é acessível do lado NeoForge.
- **Mixin em `Permissions.check`.** Seria mais invasivo e quebraria a cada
  versão dos mods; o evento é o ponto de extensão previsto da API.
- **`PermissionAPI` / `PermissionGatherEvent` do NeoForge.** O LuckPerms já a
  implementa, mas os mods Fabric nunca a consultam.
- **Compilar contra a API do Fabric com stubs próprios.** Os tipos teriam de ser
  mantidos em sincronia à mão e continuariam falhando na ligação em tempo de
  execução se mudassem. A reflexão, mais o teste de contrato lendo o jar real,
  dá o mesmo aviso sem manter cópia.
- **Substituir os mods Fabric por equivalentes NeoForge.** Fora do escopo; o
  Banhammer e o Styled Chat não têm equivalente com o mesmo comportamento.

## Consequências

- Reflexão tem custo de manutenção: o formato esperado está num só lugar
  (`FabricPermissionsApi`) e num teste que lê o jar publicado
  ([0006](0006-testes-e-deteccao-de-quebra.md)).
- Os `Proxy` são criados uma vez, no início; cada consulta é uma chamada de
  interface comum (sem reflexão na rota quente).
