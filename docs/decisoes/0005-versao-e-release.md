# 0005: SemVer, branches e releases por tag assinada

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório

## Contexto

O mod precisa de uma regra de versão, de um fluxo de branches igual ao dos
demais repositórios de código da IunaMC e de uma forma de publicar releases que
não exija guardar chaves de assinatura no CI.

## Decisão

- **Versão** (`mod_version` no `gradle.properties`, SemVer): MAJOR muda o
  comportamento ou exige outra versão do Minecraft ou do NeoForge; MINOR é um
  módulo ou uma opção nova; PATCH é uma correção. As notas ficam no
  `MUDANCAS.md`, com uma seção `## X.Y.Z` por versão, que o workflow usa como
  texto da release.
- **Nome do jar:** `iunacompat-<version>-mc<minecraft>.jar`, por exemplo
  `iunacompat-0.1.0-mc1.21.1.jar`.
- **Branches:** o mesmo fluxo dos demais repositórios de código. O trabalho é
  feito na `devel/devel` (ou numa `devel/<tarefa>`, com PR para ela); a `main`
  só aceita PRs da `devel/devel` ou de `docs/*`, com merge commit
  (`gh pr merge --merge`) e nunca com rebase-merge; a `devel/devel` é permanente.
- **Entrada na main** (um job do Validar): confere se a branch de origem é
  permitida, se a versão é estável e, quando o código mudou, se a versão é maior
  que a da `main`.
- **Release:** é disparada pelo envio da tag `vX.Y.Z` (estável, a partir de um
  commit da `main`) ou `vX.Y.Z-beta.N` (a partir de um commit da `devel/devel`).
  O workflow exige uma **tag anotada e verificada pelo GitHub** (assinatura GPG
  ou SSH de quem a criou), uma versão igual à do `gradle.properties`, que a
  release ainda não exista e que o commit esteja na branch certa. Em seguida,
  compila, testa, gera o SHA-256, atesta a procedência (Sigstore, pela action
  `actions/attest`) e cria a release.

## Alternativas descartadas

- **Uma release a cada push na `main`, como no modpack.** O modpack controla a
  versão pelo `pack.toml`; aqui, a tag é o ato explícito de publicar, e a
  assinatura fica com a pessoa, sem nenhuma chave de assinatura guardada no CI.
- **Assinar o jar com GPG no CI.** Exigiria guardar uma chave privada num
  segredo, ao passo que a atestação de procedência do GitHub comprova, sem
  segredo algum, que o jar saiu deste repositório e deste workflow.
- **Tags de versão sem beta.** A `devel/devel` precisa de um caminho de teste
  (`-beta.N`) antes da versão estável, como no modpack.

## Consequências

- Quem publica precisa ter a chave de assinatura cadastrada no GitHub.
- Uma tag errada não publica nada, porque o workflow falha antes de criar a
  release; basta apagar a tag remota e criá-la de novo.
