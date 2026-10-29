# Adicionar um módulo

Objetivo: incluir outra correção de compatibilidade no mod sem mexer nas que já
existem.

## Passos

1. Crie um subpacote `club.iuna.compat.<module>` com uma classe que implemente
   `CompatModule` (`id`, `description`, `enable`, `status`), respeitando as
   seguintes regras:
   - `enable()` nunca lança exceção: devolve um `ModuleStatus` com o estado e
     uma explicação de uma linha (`UNAVAILABLE` se o mod ajustado não estiver
     instalado).
   - As classes de outro mod nunca são carregadas fora do módulo, para que o mod
     continue carregando mesmo sem elas.
   - O módulo tem uma seção própria no `CompatConfig` (`<module>.enabled`, com
     padrão `true`).
2. Registre o módulo em `IunaCompat.MODULES`.
3. Escreva testes unitários para a lógica de decisão e, se o módulo ler o código
   de outro mod por reflexão, um teste de contrato nos moldes do
   `FabricContractTest`.
4. Confirme o comportamento no código do mod ajustado (decompilado ou aberto) e
   registre o que foi encontrado nas
   [confirmações no código](../referencia/confirmacoes-no-codigo.md) ou numa nova
   página de referência.
5. Acrescente verificações ao `scripts/integration-test.sh`.
6. Registre a decisão em `docs/decisoes/` caso alguma alternativa tenha sido
   descartada.
7. Aumente a parte MINOR do `mod_version` e escreva a nota no `MUDANCAS.md`.

## Conferência

`./gradlew build` e `./scripts/integration-test.sh` passam, e o módulo aparece
em `/iunacompat status`.
