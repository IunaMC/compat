# compat

Compatibility mod for the IUNA server (Minecraft 1.21.1, NeoForge 21.1.x), server side only. Each fix is a module (`CompatModule`); the first one, `permissions`, bridges `fabric-permissions-api` (used by Fabric mods that run through Sinytra Connector) to LuckPerms.

- **Language:** documentation and code are in English by owner decision for this repository; commits, PRs and issues stay in Brazilian Portuguese per the common rules. Code means identifiers, comments, log messages and config keys.
- **Commits:** use the module or area as the scope: `feat(permissions): …`, `fix(permissions): …`, `build: …`, `build(workflows): …`, `test: …`, `docs: …`.
- **Branches:** work on `devel/devel` (or on `devel/<task>` with a PR into it), then open a PR into `main` and merge with a merge commit (`gh pr merge --merge`). Never rebase-merge: it recreates the commits without signatures. `devel/devel` is permanent, like `main`: never delete it (no `--delete-branch` either); after the merge, run `git merge --ff-only origin/main` on it and `git push`.
- **Version (SemVer, in `mod_version` in `gradle.properties`):** MAJOR changes behavior or requires another Minecraft/NeoForge version; MINOR is a new module or option; PATCH is a fix. Notes go in `CHANGELOG.md`. A release is published by pushing a signed `vX.Y.Z` tag (`docs/guides/publish-a-version.md`).
- **Build and test:** `./gradlew build` (Java 21). The contract test reads the published `fabric-permissions-api`; the integration test starts the pack's server (`docs/guides/server-test.md`).
- **Every pack, Minecraft or NeoForge update** goes through the checklist in `docs/guides/update-with-the-pack.md`.
- **Documentation:** index in `docs/README.md`; writing standard in [IunaMC/docs](https://github.com/IunaMC/docs/blob/main/referencia/padrao-de-escrita.md). If a command, workflow or script changes, update its guide in the same commit.

<!-- regras-comuns:inicio -->
## Regras comuns da IunaMC

Texto oficial em
[IunaMC/docs](https://github.com/IunaMC/docs/blob/main/referencia/claude.md).
Não edite este bloco: mude lá e rode `scripts/regras-comuns.py sincronizar`.

- **Commits:** `git commit -S -s`, [Conventional Commits](https://www.conventionalcommits.org/pt-br/) em português. Nunca `Co-Authored-By` nem menção ao Claude, nem em commit, PR, issue ou release.
- **Idioma:** português do Brasil em código comentado, documentação, commits, PRs e issues. A única exceção é a descrição da CurseForge, só em inglês.
- **Repositório público:** nada de host, hardware, IP, DNS interno, caminho local nem segredo. Isso fica nos repositórios privados, e a página pública só diz que existe.
- **Uma sessão por tarefa:** tarefa sem relação com a anterior abre sessão nova, porque o histórico é reenviado a cada mensagem.
- **Antes de perguntar ao usuário:** leia a memória, o `git log` e as sessões anteriores. Pergunte só o que não estiver registrado.
- **Modelo e esforço:** a skill fixa o modelo no cabeçalho; sem skill, siga a tabela. Em subagente, passe o `model` dela.

  | Tarefa | Modelo | Esforço |
  | --- | --- | --- |
  | Mecânica: commit, mover arquivos, atualizar notas, conferir o CI | Haiku ou Sonnet | baixo |
  | Rotina com algum julgamento: revisar config, escrever guia | Sonnet | médio |
  | Decisão de projeto, depuração, análise de crash | Opus | alto |
<!-- regras-comuns:fim -->
