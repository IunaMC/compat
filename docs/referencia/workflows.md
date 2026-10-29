# Workflows

Em `.github/workflows/`. Ações fixadas por versão maior, as mesmas do `modpack`.

| Workflow | Quando roda | O que faz |
| --- | --- | --- |
| `validar.yaml` | push (menos só-documentação) e PR | Valida o wrapper do Gradle; confere `mod_version` e a seção do `MUDANCAS.md`; `./gradlew build` (compila, testes unitários, teste de contrato, jar); guarda o jar. Em PR para a `main`, o job *Entrada na main* confere origem, versão estável e versão maior que a da `main` |
| `integracao.yaml` | PR para a `main` que mexe em código, toda segunda-feira, sob demanda (com a branch ou tag do pack) | Compila e roda `scripts/integration-test.sh`: instala o pack com o `testar-servidor.sh` do modpack, sobe o servidor e confere a ponte. Guarda o log |
| `contrato.yaml` | toda segunda-feira, sob demanda | Testa o contrato com a versão do pack e a mais nova da `fabric-permissions-api`; compara o pack com `tested-with.json`; abre issue se algo mudou |
| `docs.yaml` | push e PR que mudam `.md` | `lychee --offline` (links e âncoras) e `markdownlint-cli2` |
| `regras-claude.yaml` | push e PR que mudam `CLAUDE.md` | Confere o bloco de regras comuns da IunaMC |
| `release.yaml` | push de tag `v*` | Confere a tag (formato, versão, anotada, verificada, branch certa), compila, testa, gera o SHA-256, atesta a procedência e cria a release com o texto do `MUDANCAS.md` |

Segredos: nenhum. O `GITHUB_TOKEN` basta; a atestação usa o OIDC do próprio GitHub.
