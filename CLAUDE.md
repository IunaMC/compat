# compat

Mod de compatibilidade do servidor IUNA (Minecraft 1.21.1, NeoForge 21.1.x), só de servidor. Cada correção é um módulo (`CompatModule`); o primeiro, `permissions`, liga a `fabric-permissions-api` (usada por mods Fabric que rodam pelo Sinytra Connector) ao LuckPerms.

- **Código em inglês** (identificadores, comentários, logs, chaves de config); documentação, commits, PRs e issues em português do Brasil.
- **Commits:** o módulo ou a área como escopo: `feat(permissions): …`, `fix(permissions): …`, `build: …`, `build(workflows): …`, `test: …`, `docs: …`.
- **Branches:** trabalho na `devel/devel` (ou em `devel/<tarefa>` com PR para ela), PR para a `main` com merge commit (`gh pr merge --merge`). Nunca rebase-merge: ele recria os commits sem assinatura. A `devel/devel` é permanente como a `main`: nunca apague (nem `--delete-branch`); depois do merge, `git merge --ff-only origin/main` nela e `git push`.
- **Versão (SemVer, em `mod_version` do `gradle.properties`):** MAJOR muda o comportamento ou exige outra versão do Minecraft/NeoForge; MINOR é módulo novo ou opção nova; PATCH é correção. Notas no `MUDANCAS.md`. A release sai ao empurrar a tag `vX.Y.Z` assinada (`docs/guias/publicar-versao.md`).
- **Compilar e testar:** `./gradlew build` (Java 21). O teste de contrato lê a `fabric-permissions-api` publicada; o teste de integração sobe o servidor do pack (`docs/guias/teste-no-servidor.md`).
- **Cada atualização do pack, do Minecraft ou do NeoForge** passa pelo checklist de `docs/guias/atualizar-com-o-pack.md`.
- **Documentação:** índice em `docs/README.md`; padrão de escrita no [IunaMC/docs](https://github.com/IunaMC/docs/blob/main/referencia/padrao-de-escrita.md). Mudou comando, workflow ou script: atualize o guia no mesmo commit.


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
