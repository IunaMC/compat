# Teste no servidor

Objetivo: subir o servidor real do pack com o jar recém-compilado e conferir a
ponte de ponta a ponta.

## Pré-requisitos

- `./gradlew build` já executado (o jar precisa estar em `build/libs/`).
- Java 21, `git`, `curl` e Python 3.
- Cerca de 5 GB de memória livre (defina `MEMORY=4G` se estiver no limite),
  3 GB de disco e de 5 a 10 minutos.

## Passos

```sh
./scripts/integration-test.sh
```

O script executa as seguintes etapas:

1. clona o `IunaMC/modpack` (`main`, ou `--modpack-ref REF`) e usa o
   `scripts/testar-servidor.sh` dele para instalar o NeoForge e os mods de
   servidor do pack em `servidor-teste/server/`;
2. coloca o jar do mod em `mods/` e escreve uma configuração do LuckPerms com
   armazenamento em arquivo, além de um script de teste do KubeJS
   (`scripts/integration/probe.js`);
3. sobe o servidor e controla o console por meio de um FIFO;
4. confere o `/iunacompat status`, define nós e opções com o `lp` e os lê de
   volta pela API do Fabric e pelo código do Banhammer
   (`ConfigManager.getDurationLimit`);
5. remove o que criou e desliga o servidor.

Para repetir o teste sem reinstalar, use `./scripts/integration-test.sh --reuse`.
Para usar uma cópia local do pack, use `--modpack ../iunamc.modpack`.

## Conferência

Cada linha `ok` corresponde a uma verificação aprovada, e a execução termina com
`All checks passed.` e código de saída 0. Em caso de falha, o script mostra o
padrão esperado e as últimas linhas do console. O log completo fica em
`servidor-teste/server.log`.

Se a porta 8765 já estiver ocupada, é porque outro processo a está usando (o
`testar-servidor.sh` do modpack serve o pack nela); encerre esse processo antes
de continuar.

## Depois que o teste passar

Se o pack mudou, registre o novo estado como testado:

```sh
python3 scripts/pack-drift.py --update
```

## O que o teste não cobre

O teste usa um jogador falso (`FakePlayerFactory`), e não um jogador conectado.
Esse jogador passa pelo mesmo caminho no LuckPerms e pelo mesmo código dos mods,
mas não pela rede, pelo login nem pelos contextos de um jogador real. Por isso,
antes de depender de um nó em produção, entre no servidor e execute um comando
que o utilize.
