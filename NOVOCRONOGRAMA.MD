# 🎯 Cronograma Final - Projeto Análise HIV

**Período: 14/11 a 25/11 (11 dias úteis)**  
**Meta: Sistema de contadores epidemiológicos funcionando**

---

## ✅ **JÁ IMPLEMENTADO (70%)**

### **Backend:**
- ✅ Spring Boot + PostgreSQL + HAPI FHIR funcionando
- ✅ `SyntheticHemogramGeneratorService` - gera hemogramas sintéticos
- ✅ `HemogramaFhirParserService` - **JÁ TEM detecção risco HIV!**
- ✅ APIs básicas: `/api/hemogramas/recentes` e `/{id}`
- ✅ DTOs com campos `riscoHiv` e `motivoRisco`
- ✅ Webhooks HAPI → Spring Boot (90% funcional)

### **Frontend:**
- ✅ React Native app completo com navegação
- ✅ Integração com APIs backend funcionando  
- ✅ Autenticação JWT + telas principais
- ✅ Componentes para gráficos preparados

---

## 🚨 **O QUE FALTA (30%)**

### **📊 Sistema Core:**
1. **Gerador de Pacientes FHIR** - criar pacientes com região, idade, sexo
2. **Sistema de Contadores** - salvar estatísticas agregadas (não dados individuais)
3. **Kafka** - notificações quando detecta novos casos HIV
4. **APIs Estatísticas** - endpoints para dashboard com contadores
5. **Dashboard Tempo Real** - frontend consumindo estatísticas + notificações

---

# 📅 **CRONOGRAMA**

## **SEMANA 1: 14/11 a 20/11**

### **👩‍💻 BACKEND (Aline Nunes + Ayumi)**

**Aline Nunes (4 tarefas):**

1. **Gerador de Pacientes FHIR**
   - Criar `PacienteDto` (id, nome, idade, sexo, região, cpf)
   - Criar `SyntheticPatientGeneratorService` 
   - Distribuição realista: idade (0-100), sexo (M/F), regiões brasileiras
   - Integrar com `SyntheticHemogramGeneratorService`

2. **Sistema de Contadores**
   - Entity `ContadorHiv` (data, faixaEtaria, sexo, regiao, quantidade)
   - Repository + Service para incrementar contadores
   - Lógica: detectou HIV → incrementa contador (não salva hemograma individual)

3. **Kafka Producer**
   - Configurar Kafka no Spring Boot
   - Criar `NotificacaoService` 
   - Enviar evento quando detecta novo caso HIV
   - Payload: {"tipo": "NOVO_CASO_HIV", "regiao": "X", "timestamp": "..."}

4. **APIs de Estatísticas**
   - `GET /api/estatisticas/por-regiao` - contadores por região
   - `GET /api/estatisticas/por-idade` - contadores por faixa etária  
   - `GET /api/estatisticas/resumo` - total casos + tendência

**Ayumi (4 tarefas):**

1. **Kafka Consumer**
   - Configurar consumer Spring Boot
   - Receber notificações e logar/processar
   - Preparar estrutura para frontend consumir

2. **Processamento Automático**
   - `@Scheduled` a cada 30 minutos
   - Gerar 5-10 pacientes + hemogramas
   - Detectar HIV + incrementar contadores + enviar Kafka
   - Simular laboratório real

3. **APIs de Busca e Filtros**
   - `GET /api/pacientes-risco?dataInicio=X&regiao=Y`
   - Filtros por período, região, faixa etária
   - Paginação para grandes volumes

4. **Validações e Robustez**
   - Validar dados FHIR de entrada
   - Tratamento de erros robusto
   - Logs detalhados para debug
   - Health checks dos sistemas

---

### **🎨 FRONTEND (Stephany + Aline Lima)**

**Stephany (4 tarefas):**

1. **Dashboard Estatísticas**
   - Consumir APIs `/api/estatisticas/*`
   - Gráficos por região (mapa ou barras)
   - Gráficos por faixa etária
   - Cards com totais e percentuais

2. **Notificações Tempo Real**
   - Integrar Kafka consumer no React Native
   - Toast/Alert quando novo caso detectado
   - Lista de notificações recentes
   - Badge com contador não lidas

3. **Tela de Análises Detalhadas**
   - Filtros por período, região, idade
   - Exportar dados (JSON para análise)
   - Comparação períodos (mês atual vs anterior)
   - Gráficos de tendência temporal

4. **Polish UI/UX**
   - Loading states em todas as telas
   - Error handling com retry
   - Pull to refresh nos dados
   - Otimização performance listas

**Aline Lima (4 tarefas):**

1. **Web Dashboard Básico**
   - Página HTML simples para desktop
   - Consumir mesmas APIs de estatísticas
   - Gráficos com Chart.js ou similar
   - Responsivo para apresentação

2. **Sistema de Usuários Completo**
   - CRUD usuários no backend (se não tiver)
   - Tela gestão completa no mobile
   - Permissões básicas (admin vs user)
   - Validações de formulário

3. **Tela de Relatórios**
   - Relatório epidemiológico formatado
   - Resumo executivo com insights
   - Comparação entre regiões
   - Export para apresentação

4. **Integração e Testes**
   - Testar fluxo completo end-to-end
   - Validar todas as integrações
   - Documentar bugs e corrigir
   - Preparar dados para demo

---

## **SEMANA 2: 21/11 a 25/11 - FINALIZAÇÃO**

### **21/11 (Quinta):**
- **Todos:** Integração final + testes sistema completo
- **Todos:** Ajustes baseados em testes

### **22/11 (Sexta):**
- **Backend:** Performance tuning + logs finais
- **Frontend:** Build final + teste dispositivos

### **23/11 (Sábado):**
- **Todos:** Documentação final + README
- **Todos:** Preparação apresentação

### **24/11 (Domingo):**
- **Todos:** Ensaio da apresentação
- **Todos:** Últimos ajustes

### **25/11 (Segunda):**
- **🎉 ENTREGA FINAL**

---

## 🎯 **RESULTADO ESPERADO**

### **Sistema Final:**
1. **Recebe hemogramas** → analisa campos HIV
2. **Detecta casos suspeitos** → incrementa contadores no banco
3. **Envia notificação Kafka** → alert tempo real
4. **Dashboard mostra estatísticas** → gráficos por região/idade
5. **Processamento automático** → simula laboratório real

### **Fluxo Completo:**
```
Laboratório (simulado) → Hemograma FHIR → 
Análise HIV → Contador++ → Kafka → 
Dashboard atualiza → Professor impressionado! 🎉
```

### **Dados no Banco:**
- **NÃO:** hemogramas individuais 
- **SIM:** contadores agregados (ex: "Sudeste, 30-45 anos, Mulheres: 15 casos")

### **Notificações:**
- **Kafka:** "Novo caso detectado na região X"
- **Mobile:** Toast notification em tempo real
- **Web:** Badge atualiza automaticamente

---

## 📊 **ENTREGAS POR SEMANA**

### **Fim Semana 1 (20/11):**
- ✅ Geração pacientes + contadores funcionando
- ✅ Kafka producer/consumer operacional  
- ✅ APIs estatísticas retornando dados reais
- ✅ Dashboard frontend consumindo APIs

### **Fim Semana 2 (25/11):**
- ✅ Sistema completo funcionando
- ✅ Demo ao vivo preparada
- ✅ Documentação completa
- ✅ **PROJETO ENTREGUE**

---

## 💡 **DIFERENCIAIS DO PROJETO**

### **Inovação Técnica:**
- **Sistema epidemiológico real** (não CRUD simples)
- **FHIR + Kafka** (tecnologias profissionais)
- **Tempo real** (contadores atualizando automaticamente)
- **Mobile + Web** (multiplataforma)

### **Impacto Social:**
- **Saúde pública** (detecção precoce HIV)
- **Análise epidemiológica** (padrões por região/idade)
- **Sistema laboratório** (aplicação real)

**Professor vai ficar impressionado com a complexidade técnica e relevância social!** 🚀

---

**🎯 Status: 70% → 100% em 11 dias**  
**📊 Foco: Sistema de contadores + notificações tempo real**  
**🎉 Meta: Demonstração funcionando ao vivo no dia 25/11**
