# Sistema de Análise Epidemiológica HIV - Hemograma FHIR

Sistema para detecção de risco HIV através de análise de hemogramas usando padrões FHIR, com contadores epidemiológicos agregados por região, idade e sexo.

## 🚀 Como executar o projeto

### 1. Iniciar Docker (HAPI FHIR + PostgreSQL)

```bash
cd hemograma-analysis
docker-compose up -d
```

**Verificar se está rodando:**
```bash
docker ps
```

Deve mostrar 3 containers:
- `hapi_fhir` (porta 8090)
- `hemograma_postgres` (porta 5432) 
- `hemograma_generator` (gerando dados)

### 2. Executar Spring Boot

```bash
cd backend-hemograma-analysis
./mvnw spring-boot:run
```

Ou pelo IDE: executar `BackendHemogramaAnalysisApplication`

**Verificar se está funcionando:**
- Spring Boot: http://localhost:8080/api/estatisticas/status
- HAPI FHIR: http://localhost:8090/fhir/Observation

## 📊 Endpoints da API

### **Status e Monitoramento**

- `GET /api/estatisticas/status` - Verifica se sistema está operacional
- `GET /api/estatisticas/resumo` - Resumo geral das estatísticas

### **Estatísticas Epidemiológicas**

- `GET /api/estatisticas/por-regiao` - Casos HIV por região brasileira
- `GET /api/estatisticas/por-idade` - Casos HIV por faixa etária
- `GET /api/estatisticas/por-sexo` - Casos HIV por sexo
- `GET /api/estatisticas/tendencia` - Evolução temporal (últimos 30 dias)

### **Dashboard Completo**

- `GET /api/estatisticas/dashboard` - Dados completos para visualização

## 🧪 Endpoints de Teste

### **Simulação Epidemiológica**

- `POST /api/estatisticas/simular?quantidade=10` - Simula 10 casos para teste
- `POST /api/estatisticas/caso-individual` - Processa um caso individual

### **HAPI FHIR**

- `GET /fhir/Observation` - Lista hemogramas gerados
- `GET /fhir/Patient` - Lista pacientes

## 🎯 O que foi implementado

### **Sistema de Contadores Epidemiológicos**
- **Entity ContadorHiv**: Armazena estatísticas agregadas (não dados individuais)
- **Agrupamento**: Por região, faixa etária, sexo e data
- **Índices otimizados**: Para consultas rápidas
- **Incremento automático**: Quando risco HIV é detectado

### **APIs RESTful**
- **Endpoints de estatísticas**: Para dashboard e relatórios
- **Segurança configurada**: Endpoints públicos para consulta
- **Tratamento de erros**: Responses consistentes
- **CORS habilitado**: Para integração frontend

### **Integração FHIR**
- **HAPI FHIR Server**: Gerenciamento de recursos FHIR
- **Gerador automático**: Hemogramas sintéticos brasileiros
- **PostgreSQL**: Persistência de dados e contadores
- **Docker**: Ambiente completo containerizado

### **Geração de Dados Brasileiros**
- **Pacientes realistas**: CPF, nomes, endereços brasileiros
- **Distribuição demográfica**: Por estado e região
- **Hemogramas sintéticos**: Valores dentro de faixas normais
- **Simulação epidemiológica**: Para demonstração

## 📋 Estrutura do Projeto

```
backend-hemograma-analysis/
├── src/main/java/.../hemograma/
│   ├── entity/           # ContadorHiv
│   ├── repository/       # ContadorHivRepository  
│   ├── service/          # Lógica de negócio
│   └── controller/       # APIs REST
└── docker-compose.yml    # HAPI FHIR + PostgreSQL
```

## 🔗 URLs Importantes

- **Spring Boot**: http://localhost:8080
- **HAPI FHIR**: http://localhost:8090/fhir
- **PostgreSQL**: localhost:5432

## 📈 Exemplo de Response

### GET /api/estatisticas/resumo

```json
{
  "totalCasos": 45,
  "casosHoje": 3,
  "casosUltimos7Dias": 12,
  "casosUltimos30Dias": 45,
  "dataAtualizacao": "2025-11-15"
}
```

### GET /api/estatisticas/dashboard

```json
{
  "resumo": { "totalCasos": 45, ... },
  "porRegiao": { "Sudeste": 18, "Nordeste": 12, ... },
  "porIdade": { "18-29": 8, "30-44": 15, ... },
  "porSexo": { "Masculino": 28, "Feminino": 17 },
  "ultimaAtualizacao": "2025-11-15T19:13:08Z"
}
```

## ⚡ Comandos Úteis

**Parar Docker:**
```bash
docker-compose down
```

**Ver logs do gerador:**
```bash
docker logs hemograma_generator -f
```

**Recompilar Spring Boot:**
```bash
./mvnw clean compile
```

---

> **Sistema desenvolvido para projeto acadêmico UFG - Análise Epidemiológica HIV usando FHIR**
