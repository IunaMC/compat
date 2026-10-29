# IUNA Compat

Camada de compatibilidade do [pack IUNA](https://github.com/IunaMC/modpack)
(Minecraft 1.21.1, NeoForge 21.1.x), instalada apenas no servidor. Cada correção
é um módulo independente.

[English](README.en.md)

## Módulos

| Módulo | O que corrige |
| --- | --- |
| `permissions` | Faz a ponte entre a `fabric-permissions-api` e o LuckPerms. Os mods Fabric que rodam pelo Sinytra Connector (Banhammer, Styled Chat, Styled Player List, Essential Commands e EasyAuth) consultam nós como `banhammer.duration.1d`, mas o LuckPerms para NeoForge responde apenas à `PermissionAPI` do NeoForge e aos nós `command.*`. Sem este mod, esses nós não têm efeito. |

## Requisitos

- Minecraft 1.21.1 e NeoForge 21.1.x.
- LuckPerms 5.4 ou mais recente, para o módulo `permissions`. Sem ele, ou sem
  nenhum mod Fabric que traga a `fabric-permissions-api` embutida, o módulo
  permanece inativo e registra o motivo no log; nesse caso, o servidor se
  comporta como se o mod não estivesse instalado.
- Apenas no servidor. Os jogadores não precisam instalar nada.

## Como funciona

O mod registra ouvintes nos quatro eventos da `fabric-permissions-api`
(`PermissionCheckEvent`, `OptionRequestEvent` e as duas variantes offline) e
responde por meio da API pública do LuckPerms, da mesma forma que o ouvinte
oficial do LuckPerms para Fabric. Quando o LuckPerms não define o nó consultado,
a resposta é "sem opinião", e o mod que fez a consulta, chamado aqui de
consumidor, mantém o próprio padrão. Portanto, instalar o mod não altera nada
até que alguém defina um nó. As razões de cada escolha estão documentadas em
[`docs/`](docs/README.md).

## Configuração

O arquivo `config/iunacompat-common.toml` é criado na primeira inicialização do
servidor.

| Chave | Padrão | Efeito |
| --- | --- | --- |
| `permissions.enabled` | `true` | Liga ou desliga o módulo |
| `permissions.options` | `true` | Responde também às consultas de opção (meta, prefixo e sufixo) |
| `permissions.offline` | `true` | Responde também às consultas sobre jogadores offline |
| `permissions.registerKnownNodes` | `true` | Registra os nós conhecidos no LuckPerms, para o autocompletar e o editor web (não concede nada) |
| `debug.logChecks` | `false` | Registra cada consulta no log (gera um volume grande de mensagens) |

## Diagnóstico

Os comandos abaixo podem ser executados pelo console ou com nível de operador 4:

```text
/iunacompat status
/iunacompat check <uuid> <node> [online]
/iunacompat option <uuid> <key> [online]
```

`check` e `option` consultam a API do Fabric da mesma forma que um mod
consulta, de modo que a resposta é exatamente a que o Banhammer, o Styled Chat e
os demais recebem.

## Compilação

```sh
./gradlew build
```

É necessário o Java 21. O jar é gerado em `build/libs/`. Os testes unitários e o
teste de integração são descritos nos [guias](docs/README.md#guias).

## Licença

[MIT](LICENSE).
