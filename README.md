# Sistema de Monitoramento de Hemogramas

Sistema de software para processamento automático de hemogramas, identificação de sinais relevantes e notificação de gestores de saúde pública em tempo real.

## Visão Geral

O sistema recebe hemogramas via padrão HL7 FHIR, realiza análises individuais e coletivas, e emite alertas para gestores de saúde quando padrões suspeitos são detectados.

## Arquitetura

- **Backend**: Spring Boot com recepção FHIR e análises
- **Frontend**: Aplicativo móvel Android para notificações
- **Banco de Dados**: PostgreSQL para persistência
- **Comunicação**: API REST e notificações push (Firebase)

## Equipe

- **Dupla 1 (Backend)**: ALINE AYUMI E ALINE NUNES
- **Dupla 2 (Frontend)**: ALINE LIMA E STEPHANY MILHOMEM

## Status do Projeto

### ✅ Marco 1 - Recepção FHIR (Concluído)
- Receptor de mensagens FHIR via subscription
- Parser de recursos Observation (hemogramas)
- Extração de parâmetros hematológicos

### 🔄 Marco 2 - Análise Individual (Em desenvolvimento)
- Detecção de desvios em parâmetros hematológicos
- Geração de alertas individuais

### ⏳ Marco 3 - Base Consolidada (Planejado)
- Persistência de hemogramas no banco de dados
- Sistema de armazenamento operacional

### ⏳ Marco 4 - Análise Coletiva (Planejado)
- Detecção de padrões em janelas deslizantes
- Alertas populacionais por região

## Tecnologias

### Backend
- Java 17
- Spring Boot 3.5.5
- HAPI FHIR 6.10.5
- PostgreSQL / H2 (desenvolvimento)
- Maven

### Frontend (Planejado)
- React Native
- Firebase Cloud Messaging
- APIs REST

## Execução

### Pré-requisitos
- Java 17+
- Maven 3.6+
- Docker (opcional)

### Backend
```bash
cd backend-hemograma-analysis
mvn spring-boot:run
```

### Testes
```bash
# Verificar se está funcionando
curl http://localhost:8080/fhir/test

# Testar processamento FHIR
curl -X POST http://localhost:8080/fhir/test-hemograma
```

## Documentação

- [Marco 1 - Recepção FHIR](backend-hemograma-analysis/docs/marco1-fhir.md)
- [Cronograma de Desenvolvimento]

## Estrutura do Projeto

```
hemograma-analysis/
├── backend-hemograma-analysis/     # API Spring Boot
│   ├── src/main/java/
│   │   └── .../user/
│   │       ├── controller/         # Endpoints FHIR
│   │       └── service/           # Processamento FHIR
│   └── docs/                      # Documentação técnica
├── frontend-app/                  # App React Native (futuro)
└── docs/                         # Documentação geral
```

## Funcionalidades Principais

### Sistema de Recepção FHIR
- Endpoints para subscription FHIR
- Processamento de Bundle FHIR
- Extração automática de parâmetros de hemograma

### Análise de Hemogramas (Em desenvolvimento)
- Valores de referência configuráveis
- Detecção de desvios individuais
- Análise de padrões coletivos

### Notificações (Planejado)
- Alertas push para gestores
- Interface mobile para consulta
- API REST para integração

## Padrões Técnicos

- **FHIR R4**: Interoperabilidade de dados de saúde
- **LOINC**: Códigos padronizados para parâmetros laboratoriais
- **HTTPS + mTLS**: Comunicação segura entre servidores
- **Clean Architecture**: Separação de responsabilidades

## Contribuição

1. Clone o repositório
2. Crie uma branch para sua feature
3. Implemente e teste as mudanças
4. Abra um Pull Request

## Licença

Projeto acadêmico - Engenharia de Software - Universidade [Nome da Universidade]