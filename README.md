# IUNA Compat

Camada de compatibilidade só de servidor para o [pack IUNA](https://github.com/IunaMC/modpack)
(Minecraft 1.21.1, NeoForge 21.1.x). Cada correção é um módulo independente.

[English](README.en.md)

## Módulos

| Módulo | O que resolve |
| --- | --- |
| `permissions` | Liga a `fabric-permissions-api` ao LuckPerms. Mods Fabric que rodam pelo Sinytra Connector (Banhammer, Styled Chat, Styled Player List, Essential Commands, EasyAuth) consultam nós como `banhammer.duration.1d`, mas o LuckPerms para NeoForge só responde à `PermissionAPI` do NeoForge e aos nós `command.*`. Sem este mod, esses nós são ignorados. |

## Requisitos

- Minecraft 1.21.1 e NeoForge 21.1.x.
- LuckPerms 5.4 ou mais novo, para o módulo `permissions`. Sem ele (ou sem nenhum
  mod Fabric que embuta a `fabric-permissions-api`) o módulo fica parado e
  avisa no log; o servidor se comporta como sem o mod.
- Só no servidor. O jogador não instala nada.

## Como funciona

O mod registra ouvintes nos quatro eventos da `fabric-permissions-api`
(`PermissionCheckEvent`, `OptionRequestEvent` e as duas versões offline) e
responde pela API pública do LuckPerms, como o ouvinte oficial do LuckPerms
para Fabric. Quando o LuckPerms não define o nó, a resposta é "sem opinião" e o
mod consultado mantém o próprio padrão: ligar o mod não muda nada até alguém
gravar um nó. O porquê de cada escolha está em [`docs/`](docs/README.md).

## Configuração

`config/iunacompat-common.toml`, criado na primeira subida.

| Chave | Padrão | Efeito |
| --- | --- | --- |
| `permissions.enabled` | `true` | Liga ou desliga o módulo |
| `permissions.options` | `true` | Responde também opções (meta, prefixo e sufixo) |
| `permissions.offline` | `true` | Responde também consultas de jogadores offline |
| `permissions.registerKnownNodes` | `true` | Apresenta ao LuckPerms os nós conhecidos, para o autocompletar e o editor web (não concede nada) |
| `debug.logChecks` | `false` | Registra cada consulta no log (muito verboso) |

## Diagnóstico

Pelo console ou com nível de operador 4:

```text
/iunacompat status
/iunacompat check <uuid> <nó> [online]
/iunacompat option <uuid> <chave> [online]
```

`check` e `option` perguntam à API do Fabric como um mod pergunta, então a
resposta é a que o Banhammer, o Styled Chat e os outros recebem.

## Compilar

```sh
./gradlew build
```

Java 21. O jar sai em `build/libs/`. Os testes e o teste de integração estão em
[`docs/guias`](docs/README.md#guias).

## Licença

[MIT](LICENSE).
