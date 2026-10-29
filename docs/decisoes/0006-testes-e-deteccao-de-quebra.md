# 0006: testes e detecção antecipada de quebra

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Contexto

O mod lê a `fabric-permissions-api` por reflexão e depende de comportamento do
LuckPerms, do Connector e dos mods Fabric do pack. Uma mudança em qualquer um
não quebra a compilação.

## Decisão

Quatro camadas, da mais barata à mais completa:

1. **Testes unitários** (`./gradlew test`, a cada push): decisão da ponte
   (`PermissionBridgeTest`), mapeamento de `Tristate` para `Verdict` e de opções
   para a meta do LuckPerms com a API simulada (`LuckPermsBackendTest`), adaptador
   contra cópias do formato da API e variantes quebradas (`FabricPermissionsApiTest`).
2. **Teste de contrato** (`FabricContractTest`, a cada push): abre o jar publicado
   da `fabric-permissions-api` (a versão que o pack embute) com ASM e confere
   o formato exato em que o adaptador se apoia. O workflow *Contrato* roda o mesmo
   teste toda semana contra a versão mais nova publicada e abre uma issue se falhar,
   antes de o pack atualizar para ela.
3. **Teste de integração** (`scripts/integration-test.sh`, workflow *Integração*):
   sobe o servidor real do pack com o jar e confere, com `lp` pelo console, nós e
   opções lidos pela API do Fabric (jogador online simulado e UUID offline) e pelo
   código do Banhammer. Roda em PR para a `main`, toda semana e sob demanda.
4. **Deriva do pack** (`scripts/pack-drift.py`, workflow *Contrato*): compara as
   versões do pack, do NeoForge e dos mods vigiados com `tested-with.json` e abre
   uma issue quando mudam. Depois de um teste de integração verde, `--update`
   registra o novo estado.

## Alternativas descartadas

- **Só teste de integração.** Lento (minutos e 1,5 GB), pouco preciso para dizer
  o que mudou, e não roda a cada commit.
- **Compilar contra a API do Fabric.** Um erro de assinatura só apareceria se
  alguém recompilasse com a versão nova; ver [0002](0002-mod-neoforge-nativo-com-reflexao.md).
- **Jogador de verdade no teste de integração.** Exige cliente ou protocolo
  completo com a negociação de mods do NeoForge. O jogador falso do NeoForge
  (`FakePlayerFactory`) passa pelo mesmo caminho do LuckPerms (`getUser`,
  `ContextManager.getQueryOptions`) e pelo mesmo código dos mods.

## Consequências

- O que o teste não cobre: o caminho de um jogador conectado de verdade (rede,
  login, contextos reais). Está listado nos [guias](../guias/teste-no-servidor.md#o-que-não-é-coberto).
