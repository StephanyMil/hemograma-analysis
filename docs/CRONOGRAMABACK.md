# Cronograma Detalhado Backend - Aline & Ayumi

## Status Atual (06/09)
- ✅ **Aline**: Marco 1 completo (Recepção FHIR + Parser)
- 🔄 **Ayumi**: Trabalhando na autenticação
- 🔄 **Aline**: CRUD usuário básico (pode pausar)

---

## Semana 1: 09/09 - 13/09 (Marco 2 - Parte 1)

### Segunda (09/09)
**Aline**:
- Finalizar básico do CRUD usuário (2h)
- Criar `HemogramaAnalysisService` - estrutura básica (4h)
- Definir tabela de valores de referência (2h)

**Ayumi**:
- Finalizar sistema de autenticação básico (6h)
- Configurar CORS para APIs (2h)

### Terça (10/09)
**Aline**:
- Implementar lógica de análise individual (6h)
- Criar enum `TipoDesvio` (ALTO, BAIXO, NORMAL) (1h)
- Integrar análise com parser FHIR existente (1h)

**Ayumi**:
- Criar estrutura básica do `AlertaController` (4h)
- Configurar Swagger/OpenAPI inicial (4h)

### Quarta (11/09)
**Aline**:
- Criar classe `AlertaIndividual` (em memória) (3h)
- Implementar geração de alertas no fluxo FHIR (4h)
- Testes unitários básicos (1h)

**Ayumi**:
- Implementar endpoints GET /api/alertas (4h)
- Criar estruturas JSON para frontend (3h)
- Documentar contratos da API (1h)

### Quinta (12/09)
**Aline**:
- Endpoint GET /api/hemogramas (3h)
- Endpoint GET /api/alertas/{id} (2h)
- Validação e tratamento de erros (3h)

**Ayumi**:
- Docker Compose com PostgreSQL (4h)
- Configurar ambiente de desenvolvimento (2h)
- Testes de integração dos endpoints (2h)

### Sexta (13/09)
**Aline**:
- Ajustes e correções de bugs (4h)
- Documentação dos endpoints criados (2h)
- Teste manual end-to-end (2h)

**Ayumi**:
- Finalizar Swagger com exemplos (3h)
- Testes de carga básicos (2h)
- Preparar demo para frontend (3h)

**ENTREGA**: APIs básicas funcionando para frontend iniciar integração

---

## Semana 2: 16/09 - 20/09 (Marco 2 - Finalização)

### Segunda (16/09)
**Aline**:
- Refinamentos na análise individual (4h)
- Implementar filtros básicos nos endpoints (3h)
- Otimizações de performance (1h)

**Ayumi**:
- Testes de integração com frontend (4h)
- Ajustar CORS e headers necessários (2h)
- Configurar logs estruturados (2h)

### Terça (17/09) - DATA CRÍTICA
**Aline**:
- Finalizar validações de entrada (3h)
- Implementar paginação básica (3h)
- Resolver issues de integração (2h)

**Ayumi**:
- Suporte à integração frontend (4h)
- Atualizar documentação da API (2h)
- Deploy em ambiente de desenvolvimento (2h)

**DEADLINE**: APIs funcionais para frontend (17/09 EOD)

### Quarta (18/09)
**Ambas**:
- Suporte à integração frontend (4h cada)
- Correções de bugs reportados (2h cada)
- Preparação para Marco 3 (2h cada)

### Quinta-Sexta (19-20/09)
**Aline**:
- Planejamento do banco PostgreSQL (4h)
- Criação das entidades JPA (4h)

**Ayumi**:
- Configurar migrations do banco (4h)
- Preparar testes com banco real (4h)

---

## Semana 3: 23/09 - 27/09 (Marco 3 - Parte 1)

### Segunda (23/09)
**Aline**:
- Modelar entidade `Hemograma` completa (4h)
- Modelar entidade `AlertaIndividual` (2h)
- Configurar relacionamentos JPA (2h)

**Ayumi**:
- Migrations iniciais PostgreSQL (4h)
- Configurar repositories Spring Data (3h)
- Atualizar configurações de ambiente (1h)

### Terça (24/09)
**Aline**:
- Migrar HemogramaFhirParserService para PostgreSQL (6h)
- Ajustar fluxo de persistência (2h)

**Ayumi**:
- Implementar `HemogramaRepository` (4h)
- Implementar `AlertaRepository` (3h)
- Criar queries customizadas (1h)

### Quarta (25/09)
**Aline**:
- Atualizar AlertaController para usar banco (4h)
- Implementar filtros avançados (3h)
- Testes com dados persistidos (1h)

**Ayumi**:
- Expandir endpoints da API (4h)
- Implementar busca por data/região (3h)
- Otimizar queries do banco (1h)

### Quinta (26/09)
**Aline**:
- Validações de negócio (3h)
- Implementar soft delete (2h)
- Performance tuning (3h)

**Ayumi**:
- Endpoints de estatísticas básicas (4h)
- Implementar agregações simples (3h)
- Documentar novas APIs (1h)

### Sexta (27/09)
**Aline**:
- Testes de integração completos (4h)
- Correção de bugs (2h)
- Validação de performance (2h)

**Ayumi**:
- Atualizar Swagger completo (3h)
- Guias de integração frontend (3h)
- Preparar ambiente de testes (2h)

---

## Semana 4: 30/09 - 04/10 (Marco 3 - Finalização)

### Segunda (30/09)
**Aline**:
- Índices de banco para otimização (3h)
- Cache para consultas frequentes (3h)
- Monitoramento de queries (2h)

**Ayumi**:
- Endpoints para dashboard (4h)
- Métricas de sistema (2h)
- Health checks (2h)

### Terça (01/10) - DATA CRÍTICA
**Aline**:
- Estabilizar todas as APIs (4h)
- Testes de carga (2h)
- Correções finais (2h)

**Ayumi**:
- Documentação final da API (4h)
- Deploy em staging (2h)
- Validação end-to-end (2h)

**DEADLINE**: APIs estáveis com PostgreSQL (01/10 EOD)

### Quarta-Quinta (02-03/10)
**Ambas**:
- Suporte à migração frontend (4h cada/dia)
- Resolução de issues de integração (2h cada/dia)
- Preparação para Marco 4 (2h cada/dia)

---

## Semana 5-6: 07/10 - 17/10 (Marco 4)

### Primeira Semana (07-11/10)
**Aline**:
- Implementar AnaliseColetivService (16h)
- Janelas deslizantes por região (8h)
- Algoritmos estatísticos (16h)

**Ayumi**:
- Configurar Firebase Cloud Messaging (16h)
- Endpoints para tokens de dispositivos (8h)
- Lógica de envio de notificações (16h)

### Segunda Semana (14-17/10)
**Aline**:
- APIs de análise coletiva (16h)
- Jobs agendados para análises (8h)
- Otimizações finais (16h)

**Ayumi**:
- APIs avançadas para dashboard (16h)
- Configurações de deploy (8h)
- Integração final com frontend (16h)

**DEADLINE**: Sistema completo (17/10)

---

## Marcos de Validação

### Marco 2 (19/09)
- [ ] APIs básicas funcionando
- [ ] Frontend pode consumir dados
- [ ] Análise individual implementada
- [ ] Documentação básica pronta

### Marco 3 (03/10)
- [ ] PostgreSQL configurado
- [ ] Dados persistindo corretamente
- [ ] APIs estáveis para frontend
- [ ] Performance adequada

### Marco 4 (17/10)
- [ ] Análise coletiva funcionando
- [ ] Notificações push implementadas
- [ ] Sistema completo integrado
- [ ] Pronto para demo final

---

## Dependências Críticas Frontend

- **17/09**: APIs básicas (lista hemogramas, lista alertas)
- **01/10**: APIs com banco PostgreSQL
- **12/10**: Firebase Cloud Messaging
- **14/10**: APIs de análise coletiva

---

## Backup Plans

**Se atrasar Marco 2**:
- Focar apenas em GET /api/alertas e GET /api/hemogramas
- Deixar filtros avançados para Marco 3

**Se atrasar Marco 3**:
- Manter H2 temporariamente
- Migrar para PostgreSQL no Marco 4

**Se atrasar Marco 4**:
- Priorizar análise coletiva
- FCM pode ser simplificado ou demo