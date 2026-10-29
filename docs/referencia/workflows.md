# Workflows

Os workflows ficam em `.github/workflows/`. As *actions* são fixadas na versão
major e são as mesmas usadas no `modpack`.

| Workflow | Quando é executado | O que faz |
| --- | --- | --- |
| `validar.yaml` | push (exceto mudanças apenas na documentação) e PR | Valida o wrapper do Gradle; confere o `mod_version` e a seção correspondente no `MUDANCAS.md`; executa `./gradlew build` (compilação, testes unitários, teste de contrato e jar); guarda o jar. Nos PRs para a `main`, o job *Entrada na main* confere a branch de origem, se a versão é estável e se ela é maior que a da `main` |
| `integracao.yaml` | PRs para a `main` que alteram o código, toda segunda-feira e sob demanda (com uma branch ou tag do pack) | Compila o mod e executa `scripts/integration-test.sh`, que instala o pack com o `testar-servidor.sh` do modpack, sobe o servidor e confere a ponte. Guarda o log |
| `contrato.yaml` | toda segunda-feira e sob demanda | Executa o teste de contrato com a versão do pack e com a versão mais recente da `fabric-permissions-api`; compara o pack com o `tested-with.json`; abre uma issue se algo mudou |
| `docs.yaml` | push e PR que alteram arquivos `.md` | `lychee --offline` (links e âncoras) e `markdownlint-cli2` |
| `regras-claude.yaml` | push e PR que alteram o `CLAUDE.md` | Confere o bloco de regras comuns da IunaMC |
| `release.yaml` | push de uma tag `v*` | Confere a tag (formato, versão, se é anotada e verificada, se está na branch certa), compila, testa, gera o SHA-256, atesta a procedência e cria a release com o texto do `MUDANCAS.md` |

Nenhum segredo é necessário: o `GITHUB_TOKEN` basta, e a atestação usa o OIDC do
próprio GitHub.
