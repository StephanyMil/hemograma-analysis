# 🩸 Sistema de Análise de Hemogramas - Guia de Acesso

## 📋 Visão Geral

Sistema completo para análise automática de hemogramas com detecção de risco HIV, composto por:
- **Container Docker** gerador de hemogramas sintéticos
- **HAPI FHIR Server** para armazenamento
- **Spring Boot API** para análise e endpoints

---

## 🚀 Como Subir o Sistema

### **1. Pré-requisitos**
- Docker e Docker Compose instalados
- Java 17+ e Maven (para Spring Boot)
- Porta 8080, 8090 e 5432 livres

### **2. Subir Containers Docker**
```bash
# Navegar para pasta do projeto
cd hemograma-analysis

# Subir containers (PostgreSQL + HAPI FHIR + Gerador)
docker-compose up -d

# Verificar se todos estão rodando
docker ps
```

**Containers esperados:**
- `hemograma_postgres` (porta 5432)
- `hapi_fhir` (porta 8090) 
- `hemograma_generator` (gera dados automaticamente)

### **3. Subir Spring Boot API**
```bash
# Em outro terminal
cd backend-hemograma-analysis

# Compilar e executar
mvn spring-boot:run
```

**Aguardar mensagem:** `Started Application in X seconds`

---

## 🌐 Endpoints Disponíveis

### **HAPI FHIR Server (Porta 8090)**
- **Base URL:** `http://localhost:8090/fhir`
- **Observations:** `GET http://localhost:8090/fhir/Observation?code=58410-2`
- **Subscriptions:** `GET http://localhost:8090/fhir/Subscription`

### **Spring Boot API (Porta 8080)**

#### **Endpoints Públicos (sem autenticação):**
```
GET http://localhost:8080/fhir/test
→ Resposta: "Controller FHIR funcionando"
```

#### **Endpoints com Autenticação:**
```
GET http://localhost:8080/fhir/metrics
GET http://localhost:8080/fhir/subscription/health
GET http://localhost:8080/api/hemogramas/recentes
```

**Credenciais:**
- **Username:** `user`
- **Password:** [ver logs do Spring Boot para senha gerada]

---

## 🔧 Como Testar

### **1. Verificar se dados estão sendo gerados**
```bash
# Ver logs do gerador
docker logs hemograma_generator

# Verificar observations no HAPI
curl "http://localhost:8090/fhir/Observation?code=58410-2&_count=3"
```

### **2. Testar Spring Boot**
```bash
# Teste básico (sem auth)
curl http://localhost:8080/fhir/test

# Métricas (com auth)
curl -u user:SENHA_GERADA http://localhost:8080/fhir/metrics
```

### **3. No Insomnia/Postman**

**Configurar Basic Auth:**
1. Username: `user`
2. Password: [copiar dos logs do Spring Boot]

**Endpoints principais:**
- `GET http://localhost:8080/fhir/metrics` - Ver estatísticas
- `GET http://localhost:8080/api/hemogramas/recentes` - Hemogramas processados

---

## 📊 Monitoramento

### **Logs Importantes**
```bash
# Spring Boot
tail -f logs/spring-boot.log

# Docker containers
docker logs -f hapi_fhir
docker logs -f hemograma_generator
```

### **Métricas Esperadas**
```json
{
  "totalRequests": 50+,
  "successfulRequests": 50+,
  "totalHemogramas": 10+,
  "hemogramasComRisco": 2+
}
```

---

## ⚙️ Configurações

### **Frequência de Geração**
- **Intervalo:** 60 segundos
- **Quantidade:** 5 hemogramas por ciclo
- **Risco HIV:** 20% dos casos simulados

### **Códigos LOINC Implementados**
- `58410-2` - Complete blood count (CBC)
- `26464-8` - Leukocytes
- `789-8` - Erythrocytes
- `718-7` - Hemoglobin
- E mais 20+ campos laboratoriais

---

## 🔍 Troubleshooting

### **Container não sobe:**
```bash
# Verificar portas ocupadas
netstat -ano | findstr :8090
netstat -ano | findstr :5432

# Recriar containers
docker-compose down
docker-compose up --build
```

### **Spring Boot não conecta:**
```bash
# Verificar se PostgreSQL está acessível
docker exec -it hemograma_postgres psql -U postgres -d hemograma_db

# Verificar logs de conexão
grep "HikariPool" logs/spring-boot.log
```

### **Webhooks não chegam:**
```bash
# Verificar subscription no HAPI
curl "http://localhost:8090/fhir/Subscription?status=active"

# Criar subscription manualmente
curl -X POST http://localhost:8090/fhir/Subscription \
  -H "Content-Type: application/json" \
  -d '{
    "resourceType": "Subscription",
    "status": "active", 
    "criteria": "Observation?code=58410-2",
    "channel": {
      "type": "rest-hook",
      "endpoint": "http://172.17.0.1:8080/fhir/subscription"
    }
  }'
```

### **Autenticação falhando:**
1. Verificar senha nos logs do Spring Boot:
   ```
   Using generated security password: [COPIAR_SENHA]
   ```
2. Configurar Basic Auth no Insomnia/Postman
3. A senha muda a cada restart do Spring Boot

---

## 📱 Próximos Passos

### **Para Frontend Mobile:**
- Endpoints prontos em: `http://localhost:8080/api/hemogramas/*`
- Autenticação: Basic Auth ou JWT (implementar)
- Campos disponíveis: 24 campos laboratoriais + risco HIV

### **Para Produção:**
- Configurar credenciais fixas
- SSL/HTTPS
- Load balancer
- Backup do PostgreSQL

---

## 👥 Contatos

**Desenvolvido por:** Aline Nunes  
**Data:** Outubro 2025  
**Projeto:** Sistema de Análise de Hemogramas com IA

---

## 🎯 Quick Start

```bash
# 1. Subir Docker
docker-compose up -d

# 2. Subir Spring Boot
cd backend-hemograma-analysis && mvn spring-boot:run

# 3. Testar
curl http://localhost:8080/fhir/test

# 4. Ver métricas (com auth)
curl -u user:SENHA http://localhost:8080/fhir/metrics
```

**Sistema funcionando quando:**
- ✅ 3 containers Docker ativos
- ✅ Spring Boot na porta 8080
- ✅ `/fhir/test` retorna "Controller FHIR funcionando"
- ✅ Métricas mostram requests > 0

🚀 **Sistema pronto para uso!**