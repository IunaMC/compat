# Entrar no pack

Objetivo: pôr (ou atualizar) o jar de uma release no pack packwiz do `modpack`.

## Pré-requisitos

- A release `vX.Y.Z` existe neste repositório ([publicar](publicar-versao.md)).
- Teste de integração verde com o pack atual.
- Seguir o fluxo do `modpack` (um commit por mod, `devel/devel`, MINOR do pack).

## Passos

1. Baixe o jar e o `.sha256` da release e confira:

   ```sh
   gh release download vX.Y.Z --repo IunaMC/compat --pattern 'iunacompat-*' --dir /tmp/iunacompat
   ```

   ```sh
   (cd /tmp/iunacompat && sha256sum -c *.sha256)
   ```

2. No `modpack`, crie ou atualize `pack/mods/iuna-compat.pw.toml`:

   ```toml
   name = "IUNA Compat"
   filename = "iunacompat-X.Y.Z-mc1.21.1.jar"
   side = "server"

   [download]
   url = "https://github.com/IunaMC/compat/releases/download/vX.Y.Z/iunacompat-X.Y.Z-mc1.21.1.jar"
   hash-format = "sha256"
   hash = "<sha256 do jar>"
   ```

   Sem `[update.*]`: o mod é só de servidor e não vai para a CurseForge
   ([decisão 0004](../decisoes/0004-distribuicao-pela-release-do-github.md)).
3. `cd pack && packwiz refresh` e `python3 scripts/pack.py readme`.
4. Suba a versão do pack (MINOR), escreva o `MUDANCAS.md` do modpack e faça o commit:
   `feat(mods): adiciona IUNA Compat X.Y.Z no servidor` (ou `atualiza`).
5. Rode o teste de boot do modpack (`/testar` ou `scripts/testar-servidor.sh`).

## Como verificar

O Validar do modpack passa e o servidor sobe com `[permissions] ENABLED` no log.
Em produção, `/iunacompat status` mostra o módulo e os mods consumidores.

Nada muda de comportamento até o LuckPerms definir um nó dos mods Fabric: veja a
[referência](../referencia/confirmacoes-no-codigo.md#e-efeitos-de-ligar-a-ponte).
