# Estruturais

## Linhas não cobertas

|Classe|Linha|Justificativa|
|:---|:---|:---|
| PrincingService | 22-56 | Ao rodar os testes do pacote Java, devido a falta de rastreio consistente do JaCoCo (ferramenta de cobertura utilizada pelo Coverage) para campos estáticos complexos durante a carga da classe para a JVM, não é considerada a leitura das linhas envolvidas com a inicialização estática da classe. Isso não acontece caso rodemos o Coverage exclusivamente da classe de teste PrincingServiceTest, provando que todas as linhas que compõe o código da classe em produção são lidas normalmente.|