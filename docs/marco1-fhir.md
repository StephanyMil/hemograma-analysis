# Marco 1 - Recepção FHIR: Sistema de Monitoramento de Hemogramas

## Visão Geral

O Marco 1 implementa a base fundamental do sistema: a capacidade de receber e processar mensagens FHIR contendo dados de hemogramas. Este componente atua como o ponto de entrada para todos os dados laboratoriais que alimentarão as análises posteriores do sistema.

## Arquitetura Implementada

### Componentes Principais

#### 1. FhirSubscriptionController
- **Localização**: `com.inf.ubiquitous.computing.backend_hemograma_analysis.user.controller`
- **Responsabilidade**: Receber notificações HTTP do servidor FHIR
- **Endpoints implementados**:
  - `POST /fhir/subscription` - Endpoint principal para receber notificações FHIR
  - `GET /fhir/test` - Endpoint de verificação de funcionamento
  - `POST /fhir/test-hemograma` - Endpoint de teste com dados fictícios

#### 2. HemogramaFhirParserService
- **Localização**: `com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service`
- **Responsabilidade**: Processar e extrair dados de mensagens FHIR
- **Funcionalidades**:
  - Parse de Bundle FHIR em formato JSON
  - Identificação de recursos Observation relacionados a hemogramas
  - Extração de parâmetros hematológicos usando códigos LOINC
  - Mapeamento de dados para estrutura interna (HemogramaData)

### Fluxo de Processamento Atual

```
Servidor FHIR → POST /fhir/subscription → FhirSubscriptionController 
    ↓
HemogramaFhirParserService.processarNotificacaoFhir()
    ↓
Parsing do Bundle FHIR (JSON → Objetos Java)
    ↓
Identificação de Observations de hemograma (códigos LOINC)
    ↓
Extração de componentes: leucócitos, hemoglobina, plaquetas, hematócrito
    ↓
Criação de objetos HemogramaData
    ↓
Log dos resultados processados
```

## Dados Processados

### Parâmetros Hematológicos Suportados

| Parâmetro | Códigos LOINC Suportados | Unidade Esperada |
|-----------|-------------------------|------------------|
| Leucócitos | 6690-2, 33747-0 | /µL |
| Hemoglobina | 718-7, 30313-1 | g/dL |
| Plaquetas | 777-3, 26515-7 | /µL |
| Hematócrito | 4544-3, 31100-1 | % |

### Estrutura de Dados (HemogramaData)

```java
public class HemogramaData {
    private String observationId;        // ID da observação FHIR
    private Date dataColeta;            // Data/hora da coleta
    private BigDecimal leucocitos;      // Contagem de leucócitos
    private String unidadeLeucocitos;   // Unidade de medida
    private BigDecimal hemoglobina;     // Nível de hemoglobina
    private String unidadeHemoglobina;  // Unidade de medida
    private BigDecimal plaquetas;       // Contagem de plaquetas
    private String unidadePlaquetas;    // Unidade de medida
    private BigDecimal hematocrito;     // Percentual de hematócrito
    private String unidadeHematocrito;  // Unidade de medida
}
```

## Estado Atual (Marco 1 Concluído)

### Funcionalidades Implementadas
- ✅ Recepção de notificações FHIR via HTTP POST
- ✅ Parse de mensagens Bundle FHIR em formato JSON
- ✅ Identificação automática de observações de hemograma
- ✅ Extração de parâmetros hematológicos com códigos LOINC
- ✅ Logging detalhado do processamento
- ✅ Tratamento de erros e validações básicas
- ✅ Endpoint de teste com dados fictícios para validação

### Limitações Atuais
- 🔄 Dados processados são apenas logados (não persistidos)
- 🔄 Não há análise dos valores extraídos
- 🔄 Não há detecção de padrões ou alertas
- 🔄 Autenticação FHIR temporariamente desabilitada para desenvolvimento

### Exemplo de Processamento

Quando um hemograma é recebido, o sistema gera logs como:
```
INFO - Iniciando processamento do FHIR JSON
INFO - Bundle FHIR parseado com sucesso. Entries: 1
INFO - Hemograma extraído: HemogramaData{id='hemograma-123', leucocitos=8500, hemoglobina=14.2, plaquetas=250000, hematocrito=42}
INFO - Processamento concluído. 1 hemogramas encontrados
```

## Evolução Planejada

### Marco 2 - Análise Individual (Próximo)
**Objetivo**: Implementar análise de cada hemograma individual contra valores de referência

**Adições planejadas**:
- Serviço de análise individual (`HemogramaAnalysisService`)
- Tabela de valores de referência:
  - Leucócitos: 4.000-11.000 /µL
  - Hemoglobina: 12.0-17.5 g/dL  
  - Plaquetas: 150.000-450.000 /µL
  - Hematócrito: 36-52%
- Geração de alertas para valores fora da faixa normal
- Integração com o fluxo de processamento existente

### Marco 3 - Base Consolidada
**Objetivo**: Implementar persistência de dados

**Adições planejadas**:
- Entidades JPA para hemogramas e alertas
- Repositórios Spring Data
- Migração do processamento para incluir persistência
- Configuração do banco de dados PostgreSQL

### Marco 4 - Análise Coletiva
**Objetivo**: Implementar detecção de padrões em janelas deslizantes

**Adições planejadas**:
- Agregação por região geográfica
- Análise em janelas de tempo deslizantes
- Cálculo de indicadores estatísticos (média, desvio, tendência)
- Detecção de padrões anômalos populacionais
- Geração de alertas coletivos

## Tecnologias Utilizadas

- **Spring Boot 3.5.5**: Framework principal
- **HAPI FHIR 6.10.5**: Biblioteca para processamento FHIR R4
- **H2 Database**: Banco em memória para desenvolvimento
- **SLF4J**: Sistema de logging
- **Maven**: Gerenciamento de dependências

## Configurações Importantes

### application.properties
```properties
# Banco em memória para desenvolvimento
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true

# Segurança temporariamente flexibilizada
spring.security.enabled=false

# Logs detalhados para FHIR
logging.level.ca.uhn.fhir=DEBUG
```

## Testes e Validação

### Teste Manual
```bash
# Verificação básica
curl http://localhost:8080/fhir/test

# Teste com hemograma fictício
curl -X POST http://localhost:8080/fhir/test-hemograma
```

### Logs Esperados
O sistema deve gerar logs detalhados mostrando:
- Recebimento da mensagem FHIR
- Parse bem-sucedido do Bundle
- Extração dos parâmetros hematológicos
- Quantidade de hemogramas processados

## Próximos Passos

1. **Marco 2**: Implementar análise individual com detecção de desvios
2. **Configuração de Segurança**: Implementar autenticação mTLS adequada
3. **Integração com Servidor FHIR Real**: Testar com dados laboratoriais reais
4. **Monitoramento**: Adicionar métricas de performance e saúde do sistema