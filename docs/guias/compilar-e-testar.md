# Compilar e testar

Objetivo: compilar o mod e rodar os testes que não precisam de servidor.

## Pré-requisitos

- Java 21 (`java -version`).
- Acesso à internet na primeira vez: o ModDevGradle baixa o Minecraft e o NeoForge
  (cerca de 800 MB, e alguns minutos para decompilar e recompilar o Minecraft).

## Passos

1. Compile e teste:

   ```sh
   ./gradlew build
   ```

2. O jar fica em `build/libs/iunacompat-<versão>-mc1.21.1.jar`.
3. Só os testes, ou só um deles:

   ```sh
   ./gradlew test
   ```

   ```sh
   ./gradlew test --tests '*PermissionBridgeTest*'
   ```

## Teste de contrato

`FabricContractTest` abre o jar publicado da `fabric-permissions-api` e confere o
formato dos eventos. Por padrão, só a versão que o pack embute (`contract_versions`
em `gradle.properties`). Para conferir outras, inclusive a mais nova:

```sh
./gradlew test --tests '*FabricContractTest*' -PcontractVersions=0.3.1,latest.release
```

Falhou com a versão nova: o adaptador (`permissions/fabric/FabricPermissionsApi`)
precisa ser ajustado antes de o pack atualizar para ela. Veja
[atualizar com o pack](atualizar-com-o-pack.md).

## Como verificar

`BUILD SUCCESSFUL` e todos os testes em `PASSED`. O relatório fica em
`build/reports/tests/test/index.html`.
