# Documentação

Documentação do mod `IUNA Compat`, que segue o padrão de escrita do
[IunaMC/docs](https://github.com/IunaMC/docs/blob/main/referencia/padrao-de-escrita.md).
O código (identificadores, comentários, logs e chaves de config) fica em inglês,
e a documentação, em português do Brasil.

## Guias

| Guia | Finalidade |
| --- | --- |
| [Compilar e testar](guias/compilar-e-testar.md) | Compilar o mod e executar os testes unitários e o teste de contrato |
| [Teste no servidor](guias/teste-no-servidor.md) | Subir o servidor do pack com o mod e conferir a ponte de ponta a ponta |
| [Publicar uma versão](guias/publicar-versao.md) | Levar uma mudança da `devel/devel` até uma release no GitHub |
| [Entrar no pack](guias/entrar-no-pack.md) | Incluir o jar da release no pack do packwiz |
| [Atualizar com o pack](guias/atualizar-com-o-pack.md) | O que fazer quando o pack, o Minecraft ou o NeoForge mudam |
| [Adicionar um módulo](guias/adicionar-modulo.md) | Incluir outra correção de compatibilidade |

## Referência

| Página | Conteúdo |
| --- | --- |
| [Requisitos](referencia/requisitos.md) | O que o mod deve fazer e o que fica fora do escopo |
| [Confirmações no código](referencia/confirmacoes-no-codigo.md) | O que foi lido nos jars e nos projetos de código aberto, com a classe e o trecho |
| [Workflows](referencia/workflows.md) | O que cada workflow faz e quando é executado |

## Explicação

| Página | Conteúdo |
| --- | --- |
| [Arquitetura e plano](explicacao/arquitetura.md) | Como as peças se encaixam, o plano de execução e a cadeia de manutenção |

## Decisões

| Decisão | Assunto |
| --- | --- |
| [0001](decisoes/0001-repositorio-proprio-e-nomes.md) | Repositório próprio, nome, identificador, pacote, licença e módulos |
| [0002](decisoes/0002-mod-neoforge-nativo-com-reflexao.md) | Mod NeoForge nativo, com a API do Fabric acessada por reflexão |
| [0003](decisoes/0003-falha-segura-e-dependencias.md) | LuckPerms opcional e falha sem efeito colateral |
| [0004](decisoes/0004-distribuicao-pela-release-do-github.md) | Distribuição pela release do GitHub, sem CurseForge nem Modrinth |
| [0005](decisoes/0005-versao-e-release.md) | SemVer, branches e releases por tag assinada |
| [0006](decisoes/0006-testes-e-deteccao-de-quebra.md) | Testes e detecção antecipada de quebras |
