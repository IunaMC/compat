# Compilar e testar

Objetivo: compilar o mod e executar os testes que não dependem de um servidor.

## Pré-requisitos

- Java 21 (`java -version`).
- Acesso à internet na primeira vez, porque o ModDevGradle baixa o Minecraft e o
  NeoForge (cerca de 800 MB, além de alguns minutos para decompilar e recompilar
  o Minecraft).

## Passos

1. Compile e teste:

   ```sh
   ./gradlew build
   ```

2. O jar fica em `build/libs/iunacompat-<version>-mc1.21.1.jar`.
3. Para executar apenas os testes, ou um teste específico:

   ```sh
   ./gradlew test
   ```

   ```sh
   ./gradlew test --tests '*PermissionBridgeTest*'
   ```

## Teste de contrato

O `FabricContractTest` abre o jar publicado da `fabric-permissions-api` e confere
o formato dos eventos. Por padrão, é verificada apenas a versão embutida no pack
(`contract_versions` no `gradle.properties`). Para verificar outras versões,
inclusive a mais recente, execute:

```sh
./gradlew test --tests '*FabricContractTest*' -PcontractVersions=0.3.1,latest.release
```

Se o teste falhar com a versão nova, o adaptador
(`permissions/fabric/FabricPermissionsApi`) precisa ser ajustado antes que o pack
passe a usá-la. Veja o guia [atualizar com o pack](atualizar-com-o-pack.md).

## Conferência

O Gradle termina com `BUILD SUCCESSFUL` e todos os testes aparecem como
`PASSED`. O relatório fica em `build/reports/tests/test/index.html`.
