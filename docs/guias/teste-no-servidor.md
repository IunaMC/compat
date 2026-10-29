# Teste no servidor

Objetivo: subir o servidor de verdade do pack com o jar recém-compilado e conferir
a ponte de ponta a ponta.

## Pré-requisitos

- `./gradlew build` feito (precisa do jar em `build/libs/`).
- Java 21, `git`, `curl` e Python 3.
- Cerca de 5 GB de memória livre (`MEMORY=4G` se estiver apertado), 3 GB de disco
  e 5 a 10 minutos.

## Passos

```sh
./scripts/integration-test.sh
```

O script:

1. clona o `IunaMC/modpack` (a `main`, ou `--modpack-ref REF`) e usa o
   `scripts/testar-servidor.sh` dele para instalar o NeoForge e os mods de servidor
   do pack em `servidor-teste/server/`;
2. põe o jar do mod em `mods/`, grava uma config do LuckPerms com armazenamento
   em arquivos e um script de teste do KubeJS (`scripts/integration/probe.js`);
3. sobe o servidor e dirige o console por um FIFO;
4. confere o `/iunacompat status`, define nós e opções com `lp` e lê de volta pela
   API do Fabric e pelo código do Banhammer (`ConfigManager.getDurationLimit`);
5. limpa o que criou e para o servidor.

Para repetir sem reinstalar: `./scripts/integration-test.sh --reuse`. Para usar um
checkout local do pack: `--modpack ../iunamc.modpack`.

## Como verificar

Cada linha `ok` é uma conferência; termina com `All checks passed.` e código 0.
Uma falha mostra o padrão esperado e as últimas linhas do console. O log inteiro
fica em `servidor-teste/server.log`.

Se a porta 8765 já estiver em uso (o `testar-servidor.sh` do modpack serve o pack
nela), outro processo a ocupa: pare-o antes.

## Depois de passar

Se o pack mudou, registre o novo estado como testado:

```sh
python3 scripts/pack-drift.py --update
```

## O que não é coberto

O teste usa um jogador falso (`FakePlayerFactory`), não um jogador conectado:
passa pelo mesmo caminho do LuckPerms e pelo mesmo código dos mods, mas não por
rede, login nem pelos contextos de um jogador real. Antes de depender de um nó em
produção, entre no servidor e rode um comando que o use.
