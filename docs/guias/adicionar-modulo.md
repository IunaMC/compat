# Adicionar um módulo

Objetivo: acrescentar outra compatibilidade ao mod sem mexer nas existentes.

## Passos

1. Crie um subpacote `club.iuna.compat.<modulo>` com uma classe que implemente
   `CompatModule` (`id`, `description`, `enable`, `status`). Regras:
   - `enable()` nunca lança: devolve `ModuleStatus` com o estado e uma linha de
     explicação (`UNAVAILABLE` se o mod que ele ajusta não está instalado).
   - Nada de classe de outro mod carregada fora do módulo (para o mod continuar
     carregando sem ele).
   - Uma seção própria no `CompatConfig` (`<modulo>.enabled`, padrão `true`).
2. Registre o módulo em `IunaCompat.MODULES`.
3. Escreva os testes unitários do que decide, e, se o módulo lê código de outro
   mod por reflexão, um teste de contrato como o `FabricContractTest`.
4. Confirme o comportamento no código do mod ajustado (decompilação ou fonte
   aberta) e registre em [confirmações](../referencia/confirmacoes-no-codigo.md) ou
   numa página nova da referência.
5. Acrescente conferências ao `scripts/integration-test.sh`.
6. Registre a decisão em `docs/decisoes/` se houver alternativa descartada.
7. MINOR no `mod_version` e nota no `MUDANCAS.md`.

## Como verificar

`./gradlew build`, `./scripts/integration-test.sh` e o módulo aparecendo em
`/iunacompat status`.
