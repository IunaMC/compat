# Publicar uma versão

Objetivo: levar uma mudança da `devel/devel` à release no GitHub.

## Pré-requisitos

- `git` com assinatura configurada e a chave cadastrada no GitHub (a tag precisa
  aparecer como *verified*).
- `gh` logado.
- Teste de integração verde para a versão do pack em uso
  ([teste no servidor](teste-no-servidor.md)).

## Passos

1. Na `devel/devel`: suba `mod_version` no `gradle.properties` (SemVer) e escreva a
   seção `## X.Y.Z` no `MUDANCAS.md`. Commit `build: prepara a versão X.Y.Z`.
2. Opcional, para testar antes: tag de pré-lançamento na `devel/devel`:

   ```sh
   git tag -s vX.Y.Z-beta.1 -m "vX.Y.Z-beta.1" && git push origin vX.Y.Z-beta.1
   ```

   (o `mod_version` tem de ser `X.Y.Z-beta.1` nesse commit).
3. Abra o PR da `devel/devel` para a `main` e espere o Validar, a Integração e a
   *Entrada na main* ficarem verdes:

   ```sh
   gh pr create --base main --head devel/devel
   ```

4. Mescle com merge commit, sem apagar a branch:

   ```sh
   gh pr merge --merge
   ```

5. Atualize a `devel/devel`:

   ```sh
   git fetch && git switch devel/devel && git merge --ff-only origin/main && git push
   ```

6. Na `main`, crie e empurre a tag assinada:

   ```sh
   git switch main && git pull --ff-only
   git tag -s vX.Y.Z -m "vX.Y.Z" && git push origin vX.Y.Z
   ```

## Como verificar

O workflow *Release* termina verde e a release `vX.Y.Z` tem o jar, o `.sha256` e a
atestação (`gh attestation verify <jar> --repo IunaMC/compat`). Depois, siga
[entrar no pack](entrar-no-pack.md).

Tag errada ou workflow que recusou: apague a tag remota
(`git push origin :refs/tags/vX.Y.Z`) e a local, corrija e crie de novo. Nada é
publicado antes de o workflow passar nas conferências.
