# Entrar no pack

Objetivo: incluir (ou atualizar) o jar de uma release no pack `modpack`, que é
gerenciado pelo packwiz.

## Pré-requisitos

- A release `vX.Y.Z` existe neste repositório ([publicar](publicar-versao.md)).
- O teste de integração passou com o pack atual.
- O fluxo de trabalho do `modpack` deve ser seguido (um commit por mod,
  `devel/devel`, versão MINOR do pack).

## Passos

1. Baixe o jar e o `.sha256` da release e confira o hash:

   ```sh
   gh release download vX.Y.Z --repo IunaMC/compat --pattern 'iunacompat-*' --dir /tmp/iunacompat
   ```

   ```sh
   (cd /tmp/iunacompat && sha256sum -c *.sha256)
   ```

2. No `modpack`, crie ou atualize o arquivo `pack/mods/iuna-compat.pw.toml`:

   ```toml
   name = "IUNA Compat"
   filename = "iunacompat-X.Y.Z-mc1.21.1.jar"
   side = "server"

   [download]
   url = "https://github.com/IunaMC/compat/releases/download/vX.Y.Z/iunacompat-X.Y.Z-mc1.21.1.jar"
   hash-format = "sha256"
   hash = "<sha256 do jar>"
   ```

   O arquivo não tem `[update.*]`, porque o mod é exclusivo do servidor e não
   vai para a CurseForge ([decisão 0004](../decisoes/0004-distribuicao-pela-release-do-github.md)).
3. Execute `cd pack && packwiz refresh` e `python3 scripts/pack.py readme`.
4. Aumente a versão do pack (MINOR), escreva a entrada no `MUDANCAS.md` do
   modpack e faça o commit: `feat(mods): adiciona IUNA Compat X.Y.Z no servidor`
   (ou `atualiza`).
5. Execute o teste de inicialização do modpack (`/testar` ou
   `scripts/testar-servidor.sh`).

## Conferência

O workflow Validar do modpack passa, e o servidor sobe com
`[permissions] ENABLED` no log. Em produção, `/iunacompat status` mostra o
módulo e os consumidores.

Nenhum comportamento muda enquanto o LuckPerms não definir um nó para os mods
Fabric, como explica a
[referência](../referencia/confirmacoes-no-codigo.md#e-efeitos-de-ligar-a-ponte).
