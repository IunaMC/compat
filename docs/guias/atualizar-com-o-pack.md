# Atualizar com o pack

Objetivo: manter a ponte funcionando quando o pack, o Minecraft ou o NeoForge mudam.

## Quando usar

- Uma issue automática avisou que o pack mudou (*pack: versões mudaram...*) ou que
  a `fabric-permissions-api` mudou de formato (*contrato: ...*).
- O pack vai atualizar o LuckPerms, o Connector, o NeoForge ou um dos mods Fabric
  vigiados (Banhammer, Styled Chat, Styled Player List, Essential Commands, EasyAuth).

## Passos

1. Veja o que mudou:

   ```sh
   python3 scripts/pack-drift.py
   ```

2. Rode o teste de integração contra o pack novo (a branch ou a tag dele):

   ```sh
   ./scripts/integration-test.sh --modpack-ref <ref>
   ```

3. Se passou: registre o novo estado e faça o commit
   `build: registra o pack X.Y.Z como testado`:

   ```sh
   python3 scripts/pack-drift.py --update
   ```

4. Se falhou, ache a causa:
   - `/iunacompat status` com `problem:` ou `FAILED`: o formato da API mudou.
     Ajuste `FabricPermissionsApi` e o `FabricContractTest`, e rode
     `./gradlew test --tests '*FabricContractTest*' -PcontractVersions=<versão>`.
   - `consumer ...: uses ANOTHER copy of the event`: um mod passou a carregar a
     própria cópia da API. Ligue `debug.logChecks`, confira como o Connector
     resolveu os jars (`logs/debug.log`, `Found valid mod file ... fabric_permissions_api_v0`)
     e ajuste o adaptador.
   - Erro do LuckPerms: veja se a API pública mudou (`LuckPermsBackendTest`).
5. Minecraft ou NeoForge novo: suba `neo_version` e as faixas em `gradle.properties`,
   compile (`./gradlew build`), corrija o que quebrar, rode a integração e publique
   uma versão nova com o Minecraft no nome do jar
   ([publicar](publicar-versao.md)). Mudança que exija outra versão do Minecraft é MAJOR.
6. Versão nova do mod: [entrar no pack](entrar-no-pack.md).

## Como verificar

`python3 scripts/pack-drift.py` diz que o pack bate com o testado, o *Integração*
passa e `/iunacompat status` mostra todos os consumidores como `bridged`.
