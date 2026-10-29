# Atualizar com o pack

Objetivo: manter a ponte funcionando quando o pack, o Minecraft ou o NeoForge
mudam.

## Quando usar

- Uma issue automática avisou que o pack mudou (*pack: versões mudaram...*) ou
  que a `fabric-permissions-api` mudou de formato (*contrato: ...*).
- O pack está prestes a atualizar o LuckPerms, o Connector, o NeoForge ou um dos
  mods Fabric acompanhados (Banhammer, Styled Chat, Styled Player List,
  Essential Commands e EasyAuth).

## Passos

1. Veja o que mudou:

   ```sh
   python3 scripts/pack-drift.py
   ```

2. Execute o teste de integração contra o pack novo (a branch ou a tag dele):

   ```sh
   ./scripts/integration-test.sh --modpack-ref <ref>
   ```

3. Se o teste passou, registre o novo estado e faça o commit como
   `build: registra o pack X.Y.Z como testado`:

   ```sh
   python3 scripts/pack-drift.py --update
   ```

4. Se o teste falhou, identifique a causa:
   - `/iunacompat status` mostra `problem:` ou `FAILED`: o formato da API mudou.
     Ajuste o `FabricPermissionsApi` e o `FabricContractTest` e depois execute
     `./gradlew test --tests '*FabricContractTest*' -PcontractVersions=<version>`.
   - `consumer ...: uses ANOTHER copy of the event`: um mod passou a carregar a
     própria cópia da API. Ative o `debug.logChecks`, confira como o Connector
     resolveu os jars (`logs/debug.log`, linha
     `Found valid mod file ... fabric_permissions_api_v0`) e ajuste o adaptador.
   - Erro do LuckPerms: verifique se a API pública mudou (`LuckPermsBackendTest`).
5. Nova versão do Minecraft ou do NeoForge: atualize o `neo_version` e as faixas
   de versão no `gradle.properties`, compile (`./gradlew build`), corrija o que
   quebrar, execute o teste de integração e publique uma nova versão com a versão
   do Minecraft no nome do jar ([publicar](publicar-versao.md)). Uma mudança que
   exige outra versão do Minecraft é MAJOR.
6. Nova versão do mod: siga o guia [entrar no pack](entrar-no-pack.md).

## Conferência

`python3 scripts/pack-drift.py` informa que o pack corresponde ao estado
testado, o *Integração* passa e `/iunacompat status` mostra todos os
consumidores como `bridged`.
