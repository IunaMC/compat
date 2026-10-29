# Publicar uma versão

Objetivo: levar uma mudança da `devel/devel` até uma release no GitHub.

## Pré-requisitos

- `git` com assinatura configurada e a chave cadastrada no GitHub, já que a tag
  precisa aparecer como *verified*.
- `gh` autenticado.
- O teste de integração passou com a versão do pack em uso
  ([teste no servidor](teste-no-servidor.md)).

## Passos

1. Na `devel/devel`, aumente o `mod_version` no `gradle.properties` (SemVer) e
   escreva a seção `## X.Y.Z` no `MUDANCAS.md`. Faça o commit como
   `build: prepara a versão X.Y.Z`.
2. Opcional, para testar antes: envie uma tag de pré-lançamento na `devel/devel`:

   ```sh
   git tag -s vX.Y.Z-beta.1 -m "vX.Y.Z-beta.1" && git push origin vX.Y.Z-beta.1
   ```

   Nesse commit, o `mod_version` precisa ser `X.Y.Z-beta.1`.
3. Abra o PR da `devel/devel` para a `main` e aguarde até que Validar,
   Integração e *Entrada na main* fiquem verdes:

   ```sh
   gh pr create --base main --head devel/devel
   ```

4. Faça o merge com merge commit, sem apagar a branch:

   ```sh
   gh pr merge --merge
   ```

5. Atualize a `devel/devel`:

   ```sh
   git fetch && git switch devel/devel && git merge --ff-only origin/main && git push
   ```

6. Na `main`, crie e envie a tag assinada:

   ```sh
   git switch main && git pull --ff-only
   git tag -s vX.Y.Z -m "vX.Y.Z" && git push origin vX.Y.Z
   ```

## Conferência

O workflow *Release* termina verde, e a release `vX.Y.Z` contém o jar, o
`.sha256` e a atestação (`gh attestation verify <jar> --repo IunaMC/compat`).
Em seguida, siga o guia [entrar no pack](entrar-no-pack.md).

Se a tag estiver errada ou o workflow a tiver rejeitado, apague a tag remota
(`git push origin :refs/tags/vX.Y.Z`) e a local, corrija o problema e crie a tag
de novo. Nada é publicado enquanto o workflow não passar por todas as
verificações.
