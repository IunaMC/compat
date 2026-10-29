# 0004: distribuição pela release do GitHub, sem CurseForge nem Modrinth

- **Data:** 29/10/2026
- **Estado:** aceita
- **Origem:** criação deste repositório; contexto em
  [0008 do modpack](https://github.com/IunaMC/modpack/blob/main/docs/decisoes/0008-sem-distribuicao-no-modrinth.md)

## Contexto

O pack é distribuído só pela CurseForge, e o Modrinth é só fonte dos mods
(decisão 0008 do modpack). Todo `.pw.toml` que não é `server` tem
`[update.modrinth]` e `[update.curseforge]` para o mesmo arquivo. Mod que só roda
no servidor (`side = "server"`) fica fora do `.zip` da CurseForge e é instalado
pelo packwiz-installer no boot do servidor. O pack já tem um caminho para jar
que não vem das lojas: [arquivos externos](https://github.com/IunaMC/modpack/blob/main/docs/guias/arquivos-externos.md),
com jar anexado à release `arquivos-servidor` do próprio modpack.

## Decisão

- O jar é publicado **só na release do GitHub** deste repositório, com o
  SHA-256 e a atestação de procedência.
- O pack ganha `pack/mods/iuna-compat.pw.toml` com `side = "server"`, `[download]`
  apontando para a URL do asset da release (`url`, `hash-format = "sha256"`,
  `hash`) e **sem** `[update.*]`. Não há `[update.curseforge]` porque o mod não é
  de cliente e a CurseForge não o recebe.
- Cada versão do mod é um arquivo novo com a versão no nome; um asset nunca é
  substituído (o hash mudaria e versões antigas do pack, que apontam para a mesma
  URL, deixariam de instalar), como na regra de arquivos externos do modpack.
- Atualizar o mod no pack é manual: trocar `url`, `filename` e `hash` do
  `.pw.toml` (há um guia), porque o `packwiz update` só segue Modrinth.

## Alternativas descartadas

- **Publicar no Modrinth ou na CurseForge.** Exige página, moderação e descrição
  para um mod que só serve a este pack, e a CurseForge o veria como mod de
  cliente se aparecesse no `.zip`. A decisão 0008 do modpack tira o Modrinth da
  distribuição.
- **`pack.py externo` (anexar o jar na release do modpack).** Funciona, mas
  duplica o jar e separa a release do mod do seu repositório. Aqui a release é a
  fonte única; o modpack só aponta para ela. Se um dia o modpack quiser ser
  autossuficiente, o jar pode ser anexado lá também sem mudar nada neste repositório.
- **Link para o artefato do CI.** Expira e exige login.

## Consequências

- O pack passa a depender de `github.com/IunaMC/compat/releases` estar no ar,
  como já depende do Modrinth CDN e do GitHub para o `.mrpack`.
- Mudar o mod no pack é MINOR do pack (mod atualizado), com notas no
  `MUDANCAS.md` do modpack.
- O repositório precisa continuar público: o servidor baixa sem credencial.
