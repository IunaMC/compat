# 0005: SemVer, branches e release de tag assinada

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Decisão

- **Versão** (`mod_version` em `gradle.properties`, SemVer): MAJOR muda o
  comportamento ou exige outra versão do Minecraft ou do NeoForge; MINOR é módulo
  ou opção nova; PATCH é correção. Notas em `MUDANCAS.md`, uma seção `## X.Y.Z` por
  versão (o workflow a usa como texto da release).
- **Nome do jar:** `iunacompat-<versão>-mc<minecraft>.jar`, por exemplo
  `iunacompat-0.1.0-mc1.21.1.jar`.
- **Branches:** como os outros repositórios com código: trabalho na `devel/devel`
  (ou `devel/<tarefa>` com PR para ela); a `main` só recebe PR da `devel/devel` ou
  da `docs/*`, com merge commit (`gh pr merge --merge`), nunca rebase-merge; a
  `devel/devel` é permanente.
- **Entrada na main** (job no Validar): origem permitida, versão estável, versão
  maior que a da `main` quando o código mudou.
- **Release:** empurrar a tag `vX.Y.Z` (estável, de commit da `main`) ou
  `vX.Y.Z-beta.N` (de commit da `devel/devel`). O workflow exige tag **anotada e
  verificada pelo GitHub** (assinatura GPG ou SSH de quem a criou), versão igual à do
  `gradle.properties`, release inexistente e commit na branch certa. Depois
  compila, testa, gera o SHA-256, atesta a procedência (Sigstore, pela ação
  `actions/attest`) e cria a release.

## Alternativas descartadas

- **Release a cada push na `main`, como o modpack.** O modpack versiona por
  `pack.toml`; aqui a tag é o ato explícito de publicar, e a assinatura fica
  com a pessoa, sem chave de assinatura guardada no CI.
- **Assinar o jar com GPG no CI.** Exigiria guardar uma chave privada num
  secret. A atestação de procedência do GitHub prova que o jar saiu deste
  repositório e deste workflow sem segredo nenhum.
- **Versão no nome da tag sem beta.** A `devel/devel` precisa de um caminho de
  teste (`-beta.N`) antes da estável, como no modpack.

## Consequências

- Quem publica precisa ter a chave de assinatura cadastrada no GitHub.
- Uma tag errada não publica nada (o workflow falha antes de criar a release);
  apague a tag remota e crie de novo.
