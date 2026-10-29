# 0006: testes e detecção antecipada de quebras

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Contexto

O mod lê a `fabric-permissions-api` por reflexão e depende do comportamento do
LuckPerms, do Connector e dos mods Fabric do pack. Uma mudança em qualquer um
deles, portanto, não quebra a compilação e pode passar despercebida.

## Decisão

Os testes são organizados em quatro camadas, da mais barata à mais completa:

1. **Testes unitários** (`./gradlew test`, a cada push): cobrem as decisões da
   ponte (`PermissionBridgeTest`), a conversão de `Tristate` para `Verdict` e de
   opções para meta do LuckPerms contra uma API simulada (`LuckPermsBackendTest`)
   e o adaptador contra cópias do formato da API e variantes quebradas
   (`FabricPermissionsApiTest`).
2. **Teste de contrato** (`FabricContractTest`, a cada push): abre com o ASM o
   jar publicado da `fabric-permissions-api` (a versão embutida no pack) e
   confere exatamente o formato de que o adaptador depende. O workflow
   *Contrato* executa o mesmo teste toda semana contra a versão publicada mais
   recente e abre uma issue se ele falhar, antes que o pack passe a usar essa
   versão.
3. **Teste de integração** (`scripts/integration-test.sh`, workflow
   *Integração*): sobe o servidor real do pack com o jar e, usando o `lp` pelo
   console, confere os nós e as opções lidos pela API do Fabric (com um jogador
   online simulado e um UUID offline) e pelo código do Banhammer. É executado nos
   PRs para a `main`, toda semana e sob demanda.
4. **Mudanças no pack** (`scripts/pack-drift.py`, workflow *Contrato*): compara
   as versões do pack, do NeoForge e dos mods acompanhados com o
   `tested-with.json` e abre uma issue quando elas mudam. Depois de um teste de
   integração aprovado, a opção `--update` registra o novo estado.

## Alternativas descartadas

- **Apenas o teste de integração.** É lento (alguns minutos e 1,5 GB), pouco
  preciso sobre o que mudou e não é executado a cada commit.
- **Compilar contra a API do Fabric.** Um erro de assinatura só apareceria se
  alguém recompilasse contra a versão nova; veja a
  [0002](0002-mod-neoforge-nativo-com-reflexao.md).
- **Um jogador real no teste de integração.** Exigiria um cliente ou uma
  implementação completa do protocolo, inclusive da negociação de mods do
  NeoForge. O jogador falso do NeoForge (`FakePlayerFactory`) passa pelo mesmo
  caminho no LuckPerms (`getUser`, `ContextManager.getQueryOptions`) e pelo mesmo
  código dos mods.

## Consequências

- O teste não cobre o caminho de um jogador de fato conectado (rede, login,
  contextos reais). Essa lacuna está registrada nos
  [guias](../guias/teste-no-servidor.md#o-que-o-teste-não-cobre).
