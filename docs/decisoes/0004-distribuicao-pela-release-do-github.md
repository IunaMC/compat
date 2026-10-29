# 0004: distribuição pela release do GitHub, sem CurseForge nem Modrinth

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório; contexto na
  [decisão 0008 do modpack](https://github.com/IunaMC/modpack/blob/main/docs/decisoes/0008-sem-distribuicao-no-modrinth.md)

## Contexto

O pack é distribuído apenas pela CurseForge, e o Modrinth serve somente como
fonte de mods (decisão 0008 do modpack). Todo `.pw.toml` que não é `server` tem
`[update.modrinth]` e `[update.curseforge]` para o mesmo arquivo. Um mod
exclusivo do servidor (`side = "server"`) fica fora do `.zip` da CurseForge e é
instalado pelo packwiz-installer quando o servidor sobe. O pack já tem um
caminho para jars que não vêm das lojas, os
[arquivos externos](https://github.com/IunaMC/modpack/blob/main/docs/guias/arquivos-externos.md),
em que o jar é anexado à release `arquivos-servidor` do próprio modpack.

## Decisão

- O jar é publicado **apenas na release do GitHub deste repositório**, com o
  SHA-256 e a atestação de procedência.
- O pack recebe o arquivo `pack/mods/iuna-compat.pw.toml`, com
  `side = "server"`, uma seção `[download]` que aponta para a URL do arquivo da
  release (`url`, `hash-format = "sha256"`, `hash`) e **nenhuma** seção
  `[update.*]`. Não há `[update.curseforge]` porque o mod não é de cliente e a
  CurseForge não o recebe.
- Cada versão do mod é um arquivo novo, com a versão no nome. Um arquivo da
  release nunca é substituído, porque o hash mudaria e as versões anteriores do
  pack, que apontam para a mesma URL, deixariam de instalar; essa é a mesma regra
  dos arquivos externos do modpack.
- A atualização do mod no pack é manual, com a troca de `url`, `filename` e
  `hash` no `.pw.toml` (há um guia para isso), já que o `packwiz update` só
  acompanha o Modrinth.

## Alternativas descartadas

- **Publicar no Modrinth ou na CurseForge.** Exige página de projeto, moderação
  e descrição para um mod que só serve a este pack, e a CurseForge o trataria
  como mod de cliente se ele aparecesse no `.zip`. Além disso, a decisão 0008 do
  modpack retira o Modrinth da distribuição.
- **`pack.py externo` (anexar o jar à release do modpack).** Funciona, mas
  duplica o jar e separa a release do mod do repositório dele. Aqui, a release é
  a fonte única, e o modpack apenas aponta para ela. Se um dia o modpack quiser
  ser autossuficiente, o jar também pode ser anexado lá, sem nenhuma mudança
  neste repositório.
- **Apontar para o artefato do CI.** O artefato expira e exige login.

## Consequências

- O pack passa a depender da disponibilidade de
  `github.com/IunaMC/compat/releases`, assim como já depende da CDN do Modrinth e
  do GitHub para o `.mrpack`.
- Uma mudança do mod no pack corresponde a uma versão MINOR do pack (mod
  atualizado), com notas no `MUDANCAS.md` do modpack.
- O repositório precisa continuar público, porque o servidor faz o download sem
  credenciais.
