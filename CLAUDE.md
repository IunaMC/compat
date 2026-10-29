# compat

Mod de compatibilidade do servidor IUNA (Minecraft 1.21.1, NeoForge 21.1.x), instalado apenas no servidor. Cada correção é um módulo (`CompatModule`); o primeiro, `permissions`, faz a ponte entre a `fabric-permissions-api` (usada pelos mods Fabric que rodam pelo Sinytra Connector) e o LuckPerms.

- **Idioma:** Código (identificadores, comentários, logs, chaves de config) em inglês; documentação, commits, PRs e issues em português do Brasil.
- **Commits:** use o módulo ou a área como escopo: `feat(permissions): …`, `fix(permissions): …`, `build: …`, `build(workflows): …`, `test: …`, `docs: …`.
- **Branches:** trabalhe na `devel/devel` (ou numa `devel/<tarefa>`, com PR para ela), depois abra um PR para a `main` e faça o merge com merge commit (`gh pr merge --merge`). Nunca use rebase-merge, porque ele recria os commits sem as assinaturas. A `devel/devel` é permanente, assim como a `main`: nunca a apague (nem com `--delete-branch`); depois do merge, execute nela `git merge --ff-only origin/main` e `git push`.
- **Versão (SemVer, no `mod_version` do `gradle.properties`):** MAJOR muda o comportamento ou exige outra versão do Minecraft/NeoForge; MINOR é um módulo ou uma opção nova; PATCH é uma correção. As notas ficam no `MUDANCAS.md`. A release é publicada com o envio de uma tag assinada `vX.Y.Z` (`docs/guias/publicar-versao.md`).
- **Compilação e testes:** `./gradlew build` (Java 21). O teste de contrato lê a `fabric-permissions-api` publicada; o teste de integração sobe o servidor do pack (`docs/guias/teste-no-servidor.md`).
- **Toda atualização do pack, do Minecraft ou do NeoForge** passa pelo roteiro de `docs/guias/atualizar-com-o-pack.md`.
- **Documentação:** índice em `docs/README.md`; padrão de escrita no [IunaMC/docs](https://github.com/IunaMC/docs/blob/main/referencia/padrao-de-escrita.md). Se um comando, workflow ou script mudar, atualize o guia correspondente no mesmo commit.

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
