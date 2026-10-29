# Documentação

Documentação do mod `IUNA Compat`. O padrão de escrita é o do
[IunaMC/docs](https://github.com/IunaMC/docs/blob/main/referencia/padrao-de-escrita.md).
O código, os comentários e os logs do mod estão em inglês; a documentação, em
português.

## Guias

| Guia | Para quê |
| --- | --- |
| [Compilar e testar](guias/compilar-e-testar.md) | Compilar o mod e rodar os testes unitários e o de contrato |
| [Teste no servidor](guias/teste-no-servidor.md) | Subir o servidor do pack com o mod e conferir a ponte de ponta a ponta |
| [Publicar uma versão](guias/publicar-versao.md) | Da `devel/devel` à release no GitHub |
| [Entrar no pack](guias/entrar-no-pack.md) | Pôr o jar da release no pack packwiz |
| [Atualizar com o pack](guias/atualizar-com-o-pack.md) | O que fazer quando o pack, o Minecraft ou o NeoForge mudam |
| [Adicionar um módulo](guias/adicionar-modulo.md) | Acrescentar outra compatibilidade |

## Referência

| Página | Conteúdo |
| --- | --- |
| [Requisitos](referencia/requisitos.md) | O que o mod tem de fazer e o que não faz |
| [Confirmações no código](referencia/confirmacoes-no-codigo.md) | O que foi lido nos jars e nos projetos abertos, com a classe e o trecho |
| [Workflows](referencia/workflows.md) | O que cada workflow faz e quando roda |

## Explicação

| Página | Conteúdo |
| --- | --- |
| [Arquitetura e plano](explicacao/arquitetura.md) | Como as peças se ligam, o plano de execução e a cadeia de manutenção |

## Decisões

| Decisão | Assunto |
| --- | --- |
| [0001](decisoes/0001-repositorio-proprio-e-nomes.md) | Repositório próprio, nome, identificador, pacote, licença e módulos |
| [0002](decisoes/0002-mod-neoforge-nativo-com-reflexao.md) | Mod NeoForge nativo, API do Fabric por reflexão |
| [0003](decisoes/0003-falha-segura-e-dependencias.md) | LuckPerms opcional e falha sem efeito |
| [0004](decisoes/0004-distribuicao-pela-release-do-github.md) | Distribuição pela release do GitHub, sem CurseForge nem Modrinth |
| [0005](decisoes/0005-versao-e-release.md) | SemVer, branches e release de tag assinada |
| [0006](decisoes/0006-testes-e-deteccao-de-quebra.md) | Testes e detecção antecipada de quebra |
