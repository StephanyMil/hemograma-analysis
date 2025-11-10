# Cronograma de Desenvolvimento - Sistema de Análise de Hemogramas
## 4 Semanas - Pacientes Alerta (Linfócitos > 400)

---

## 📋 **Contexto do Projeto**

### ✅ Já Implementado:
- Controllers para receber notificações FHIR (`FhirSubscriptionController`)
- Gerador de hemogramas sintéticos (`SyntheticHemogramController`)
- Parser FHIR para processar hemogramas
- Sistema de armazenamento em buffer (não persistente)
- API básica para consultar hemogramas (`HemogramaApiController`)

### 🎯 A Implementar:
1. **Gerador de Dados de Pacientes via FHIR** (mock)
2. **Endpoints Estatísticos** (por idade, região, sexo)
3. **Sistema de Notificações em Tempo Real** (WebSocket/SSE)
4. **Persistência apenas dos resultados estatísticos**
5. **Processamento contínuo** (1000 hemogramas/dia)

---

# 📅 ORGANIZAÇÃO POR SEMANAS

---

# 🗓️ **SEMANA 1: Fundação - Pacientes e Detecção de Alertas**
**Objetivo:** Criar infraestrutura básica para gerar pacientes, detectar alertas e persistir estatísticas

### **Segunda-feira (Dia 1-2)**
**Objetivo:** Criar modelo de domínio e gerador de pacientes FHIR

#### Tarefas:
- [ ] **Criar DTOs de Paciente**
  - `PacienteDto.java` (id, nome, idade, sexo, região, CPF)
  - `PacienteAlertaDto.java` (dados do paciente + dados do hemograma alerta)
  
- [ ] **Criar `SyntheticPatientGeneratorService`**
  - Gerar pacientes em formato FHIR Resource (Patient)
  - Distribuição realista por:
    - Idade (0-100 anos, com curva normal centrada em 45)
    - Sexo (50% M, 50% F)
    - Região (Norte, Nordeste, Centro-Oeste, Sudeste, Sul - proporção populacional brasileira)
  - Incluir dados fictícios: nome (Faker), CPF gerado, endereço por região

- [ ] **Criar `PatientFhirParserService`**
  - Parsear FHIR Patient Resource para `PacienteDto`
  - Extrair: idade, sexo, região (do endereço)

**Entregável:** Serviços funcionais para gerar e parsear pacientes FHIR

---

### **Terça-feira (Dia 3)**
**Objetivo:** Criar lógica de detecção de alerta e associação paciente-hemograma

#### Tarefas:
- [ ] **Atualizar `HemogramaDto`**
  - Adicionar campo `linfocitos` (BigDecimal)
  - Adicionar campo `pacienteId` (String)
  - Manter flag `riscoHiv` renomeada para `isAlerta`
  - Atualizar `motivoRisco` para `motivoAlerta` ("Linfócitos > 400")

- [ ] **Criar `AlertaDetectionService`**
  - Método `boolean isAlerta(HemogramaDto)` - verifica se linfócitos > 400
  - Método `PacienteAlertaDto criarAlerta(HemogramaDto, PacienteDto)`
  - Log de detecção de alertas

- [ ] **Atualizar `SyntheticHemogramGeneratorService`**
  - Gerar valor de linfócitos (distribuição: 80% normal 200-350, 20% alerta 400-800)
  - Associar hemograma a um `pacienteId`

**Entregável:** Sistema de detecção de alertas funcional

---

### **Quarta-feira (Dia 4)**
**Objetivo:** Criar controller de geração integrada

#### Tarefas:
- [ ] **Criar `SyntheticDataController`**
  - `POST /synthetic/generate-complete?quantidade=N`
    - Gera N pacientes FHIR
    - Gera N hemogramas FHIR associados aos pacientes
    - Detecta alertas
    - Retorna resumo: total gerado, alertas detectados
  
  - `GET /synthetic/test-alert`
    - Gera 1 paciente + 1 hemograma garantidamente alerta (linfócitos = 500)
    - Para testes do sistema de notificação

**Entregável:** Endpoint de geração completa funcionando

---

### **Quinta-feira (Dia 5)**
**Objetivo:** Modelar banco de dados para estatísticas

#### Tarefas:
- [ ] **Criar Entities JPA**
  - `EstatisticaAlertaPorIdade` (faixaEtaria, quantidade, dataCalculo)
    - Faixas: 0-18, 19-30, 31-45, 46-60, 61-75, 76+
  
  - `EstatisticaAlertaPorRegiao` (regiao, quantidade, dataCalculo)
    - Regiões: Norte, Nordeste, Centro-Oeste, Sudeste, Sul
  
  - `EstatisticaAlertaPorSexo` (sexo, quantidade, dataCalculo)
    - Sexo: M, F

- [ ] **Criar Repositories**
  - `EstatisticaAlertaPorIdadeRepository`
  - `EstatisticaAlertaPorRegiaoRepository`
  - `EstatisticaAlertaPorSexoRepository`

- [ ] **Configurar PostgreSQL/MySQL**
  - Scripts de criação de tabelas
  - Índices para otimizar queries por data

**Entregável:** Modelo de dados para estatísticas pronto

---

### **Sexta-feira (Dia 6-7)**
**Objetivo:** Criar serviço de cálculo de estatísticas

#### Tarefas:
- [ ] **Criar `EstatisticasAlertaService`**
  - `calcularEstatisticasPorIdade(List<PacienteAlertaDto>)` → persiste no banco
  - `calcularEstatisticasPorRegiao(List<PacienteAlertaDto>)` → persiste no banco
  - `calcularEstatisticasPorSexo(List<PacienteAlertaDto>)` → persiste no banco
  - Método `processarNovoAlerta(PacienteAlertaDto)` - atualiza estatísticas incrementalmente

- [ ] **Criar lógica de agregação**
  - Agrupar alertas por faixa etária
  - Agrupar alertas por região
  - Agrupar alertas por sexo

- [ ] **Testes Unitários**
  - Testar cálculo de estatísticas com dados mockados

**Entregável:** Serviço de estatísticas completo e testado

---

## 🗓️ **SEMANA 2: APIs Estatísticas e Notificações**

### **Segunda-feira (Dia 8)**
**Objetivo:** Criar endpoints de estatísticas

#### Tarefas:
- [ ] **Criar `EstatisticasAlertaController`**
  - `GET /api/estatisticas/alertas/idade`
    - Retorna estatísticas agrupadas por faixa etária
    - Parâmetros: `dataInicio`, `dataFim` (opcional)
  
  - `GET /api/estatisticas/alertas/regiao`
    - Retorna estatísticas por região
  
  - `GET /api/estatisticas/alertas/sexo`
    - Retorna estatísticas por sexo
  
  - `GET /api/estatisticas/alertas/resumo`
    - Retorna resumo geral: total alertas, distribuição por idade/região/sexo

**Entregável:** Endpoints REST de estatísticas funcionais

---

### **Terça-feira (Dia 9-10)**
**Objetivo:** Implementar sistema de notificações em tempo real

#### Tarefas:
- [ ] **Escolher Tecnologia: Server-Sent Events (SSE)**
  - Mais simples que WebSocket
  - Unidirecional (servidor → cliente)
  - Ideal para notificações de alertas

- [ ] **Criar `NotificacaoAlertaService`**
  - Gerenciar lista de clientes conectados (`SseEmitter`)
  - Método `notificarNovoAlerta(PacienteAlertaDto)`
  - Método `registrarCliente()` → retorna `SseEmitter`

- [ ] **Criar `NotificacoesAlertaController`**
  - `GET /api/notificacoes/alertas/stream` (endpoint SSE)
    - Cliente se conecta e recebe notificações em tempo real
    - Retorna eventos no formato JSON:
      ```json
      {
        "tipo": "NOVO_ALERTA",
        "timestamp": "2025-11-10T14:30:00",
        "paciente": {
          "id": "PAC-123",
          "nome": "João Silva",
          "idade": 45,
          "sexo": "M",
          "regiao": "Sudeste"
        },
        "hemograma": {
          "observationId": "OBS-456",
          "linfocitos": 520,
          "dataColeta": "2025-11-10T14:25:00"
        }
      }
      ```

- [ ] **Integrar com `AlertaDetectionService`**
  - Ao detectar alerta, chamar `NotificacaoAlertaService.notificarNovoAlerta()`

**Entregável:** Sistema de notificações SSE funcionando

---

### **Quarta-feira (Dia 11)**
**Objetivo:** Criar job agendado para processamento contínuo

#### Tarefas:
- [ ] **Criar `ProcessamentoContinuoService`**
  - Usar `@Scheduled` do Spring
  - Job executado a cada 24 horas / número de lotes
    - Exemplo: 1000 hemogramas/dia = ~42 hemogramas/hora = processar a cada hora
  
  - Fluxo do job:
    1. Gerar lote de pacientes + hemogramas
    2. Detectar alertas
    3. Atualizar estatísticas
    4. Enviar notificações
    5. Descartar dados de pacientes/hemogramas (não persistir)

- [ ] **Configurar agendamento**
  - `@EnableScheduling` na aplicação
  - Configurar cron expression para execução contínua

**Entregável:** Job agendado funcionando

---

### **Quinta-feira (Dia 12)**
**Objetivo:** Testes de integração

#### Tarefas:
- [ ] **Criar testes de integração**
  - Testar fluxo completo: geração → detecção → estatísticas → notificação
  - Testar endpoints REST com dados mockados
  - Testar conexão SSE (simular cliente)

- [ ] **Testar performance**
  - Gerar 1000 hemogramas e medir tempo de processamento
  - Verificar uso de memória (dados não devem ficar em memória após processamento)

**Entregável:** Suite de testes de integração

---

### **Sexta-feira (Dia 13-14)**
**Objetivo:** Criar dashboard simples de monitoramento

#### Tarefas:
- [ ] **Criar `MonitoramentoController`**
  - `GET /api/monitor/status`
    - Total de hemogramas processados hoje
    - Total de alertas hoje
    - Taxa de alertas (%)
    - Último processamento
  
  - `GET /api/monitor/health`
    - Status do job agendado
    - Número de clientes conectados (SSE)
    - Estatísticas do banco

**Entregável:** Endpoints de monitoramento

---

## 🗓️ **SEMANA 3: Otimização e Documentação**

### **Segunda-feira (Dia 15)**
**Objetivo:** Otimizar consultas e índices do banco

#### Tarefas:
- [ ] **Otimização de Queries**
  - Adicionar índices compostos em `dataCalculo` + dimensão (idade/região/sexo)
  - Implementar cache para estatísticas frequentes (Spring Cache)

- [ ] **Otimizar detecção de alertas**
  - Processar hemogramas em batch (não um por um)
  - Usar streams paralelos para processar lotes grandes

**Entregável:** Sistema otimizado

---

### **Terça-feira (Dia 16)**
**Objetivo:** Criar documentação da API

#### Tarefas:
- [ ] **Configurar Swagger/OpenAPI**
  - Adicionar dependência `springdoc-openapi`
  - Documentar todos os endpoints
  - Adicionar exemplos de requests/responses

- [ ] **Criar README técnico**
  - Arquitetura do sistema
  - Fluxo de dados
  - Como executar
  - Exemplos de uso

**Entregável:** Documentação completa

---

### **Quarta-feira (Dia 17)**
**Objetivo:** Implementar logs estruturados e métricas

#### Tarefas:
- [ ] **Melhorar logging**
  - Adicionar trace IDs para rastreamento
  - Logs estruturados (JSON) com níveis adequados
  - Log de todos os alertas detectados

- [ ] **Adicionar métricas (Micrometer)**
  - Contador de alertas detectados
  - Tempo de processamento de hemogramas
  - Taxa de alertas por hora

**Entregável:** Observabilidade melhorada

---

### **Quinta-feira (Dia 18)**
**Objetivo:** Testes de carga

#### Tarefas:
- [ ] **Criar cenários de teste com JMeter/Gatling**
  - Simular 1000 hemogramas/hora
  - Simular 50 clientes conectados ao SSE
  - Medir latência dos endpoints de estatísticas

- [ ] **Ajustar configurações**
  - Pool de threads
  - Timeouts
  - Limites de memória

**Entregável:** Sistema validado para carga de produção

---

### **Sexta-feira (Dia 19-21)**
**Objetivo:** Refatoração e code review

#### Tarefas:
- [ ] **Refatorar código**
  - Eliminar duplicações
  - Melhorar nomes de variáveis/métodos
  - Adicionar comentários onde necessário

- [ ] **Code review**
  - Revisar segurança (validações de input)
  - Revisar tratamento de erros
  - Revisar estrutura de pacotes

**Entregável:** Código limpo e revisado

---

## 🗓️ **SEMANA 4: Frontend Integration & Deploy**

### **Segunda-feira (Dia 22)**
**Objetivo:** Criar exemplo de cliente SSE

#### Tarefas:
- [ ] **Criar página HTML de teste**
  - Conectar ao endpoint SSE
  - Exibir alertas em tempo real
  - Gráficos simples com Chart.js

- [ ] **Documentar integração frontend**
  - Exemplo em JavaScript puro
  - Exemplo em React
  - Como tratar reconexão automática

**Entregável:** Exemplo funcional de integração

---

### **Terça-feira (Dia 23)**
**Objetivo:** Configurar ambiente de produção

#### Tarefas:
- [ ] **Dockerizar aplicação**
  - Criar `Dockerfile`
  - Criar `docker-compose.yml` (app + banco)

- [ ] **Configurar variáveis de ambiente**
  - Separar configurações de dev/prod
  - Externalizar secrets

**Entregável:** Aplicação containerizada

---

### **Quarta-feira (Dia 24)**
**Objetivo:** Testes de aceitação

#### Tarefas:
- [ ] **Validar requisitos**
  - ✅ Gera pacientes FHIR mockados
  - ✅ Detecta alertas (linfócitos > 400)
  - ✅ Endpoints de estatísticas (idade, região, sexo)
  - ✅ Notificações em tempo real
  - ✅ Processamento contínuo (1000/dia)
  - ✅ Persiste apenas estatísticas (não hemogramas/pacientes)

- [ ] **Testes end-to-end**
  - Simular dia completo de processamento
  - Verificar consistência das estatísticas

**Entregável:** Sistema validado

---

### **Quinta-feira (Dia 25)**
**Objetivo:** Deploy em ambiente de homologação

#### Tarefas:
- [ ] **Deploy**
  - Configurar servidor (AWS/Azure/GCP ou local)
  - Subir aplicação e banco
  - Testar em ambiente real

- [ ] **Monitoramento**
  - Configurar logs centralizados
  - Configurar alertas de erro

**Entregável:** Sistema em homologação

---

### **Sexta-feira (Dia 26-28)**
**Objetivo:** Ajustes finais e entrega

#### Tarefas:
- [ ] **Ajustes finais**
  - Corrigir bugs encontrados em homologação
  - Ajustar performance se necessário

- [ ] **Documentação final**
  - Manual de operação
  - Guia de troubleshooting
  - Apresentação do projeto

- [ ] **Apresentação**
  - Demo ao vivo
  - Explicação da arquitetura
  - Métricas e resultados

**Entregável:** Sistema completo e documentado

---

## 📊 **Estrutura Final do Projeto**

```
src/main/java/...hemograma/
├── controller/
│   ├── FhirSubscriptionController.java (já existe)
│   ├── HemogramaApiController.java (já existe)
│   ├── SyntheticHemogramController.java (já existe)
│   ├── SyntheticDataController.java (NOVO)
│   ├── EstatisticasAlertaController.java (NOVO)
│   ├── NotificacoesAlertaController.java (NOVO)
│   └── MonitoramentoController.java (NOVO)
│
├── dto/
│   ├── HemogramaDto.java (atualizar)
│   ├── PacienteDto.java (NOVO)
│   ├── PacienteAlertaDto.java (NOVO)
│   └── EstatisticasDto.java (NOVO)
│
├── entity/
│   ├── EstatisticaAlertaPorIdade.java (NOVO)
│   ├── EstatisticaAlertaPorRegiao.java (NOVO)
│   └── EstatisticaAlertaPorSexo.java (NOVO)
│
├── repository/
│   ├── EstatisticaAlertaPorIdadeRepository.java (NOVO)
│   ├── EstatisticaAlertaPorRegiaoRepository.java (NOVO)
│   └── EstatisticaAlertaPorSexoRepository.java (NOVO)
│
├── service/
│   ├── HemogramaFhirParserService.java (já existe)
│   ├── HemogramaStorageService.java (já existe)
│   ├── SyntheticHemogramGeneratorService.java (já existe)
│   ├── SyntheticPatientGeneratorService.java (NOVO)
│   ├── PatientFhirParserService.java (NOVO)
│   ├── AlertaDetectionService.java (NOVO)
│   ├── EstatisticasAlertaService.java (NOVO)
│   ├── NotificacaoAlertaService.java (NOVO)
│   └── ProcessamentoContinuoService.java (NOVO)
│
└── config/
    ├── SchedulingConfig.java (NOVO)
    ├── CacheConfig.java (NOVO)
    └── SwaggerConfig.java (NOVO)
```

---

## 🎯 **Critérios de Sucesso**

### Funcionalidades Obrigatórias:
- ✅ Gerador de pacientes FHIR com dados realistas
- ✅ Detecção automática de alertas (linfócitos > 400)
- ✅ 3 endpoints de estatísticas (idade, região, sexo)
- ✅ Notificações em tempo real via SSE
- ✅ Processamento contínuo de 1000 hemogramas/dia
- ✅ Persistência apenas de estatísticas (não dados brutos)

### Qualidade:
- ✅ Cobertura de testes > 80%
- ✅ Documentação completa (Swagger + README)
- ✅ Logs estruturados e rastreáveis
- ✅ Performance adequada (processar 1000 hemogramas em < 5 minutos)

---

## 💡 **Observações Importantes**

1. **Não persistir dados brutos:** Pacientes e hemogramas são processados e descartados. Apenas estatísticas são salvas.

2. **Distribuição realista:** Garantir que 15-20% dos hemogramas gerem alertas (linfócitos > 400).

3. **Notificações:** SSE é mais simples que WebSocket para notificações unidirecionais.

4. **Escalabilidade:** Job agendado pode ser ajustado (ex: processar a cada hora em vez de uma vez ao dia).

5. **Monitoramento:** Sempre ter visibilidade do número de alertas detectados e estatísticas em tempo real.

---

## 📞 **Pontos de Atenção**

- **Semana 1:** Base fundamental - não pular etapas
- **Semana 2:** Sistema de notificações é crítico - testar bem
- **Semana 3:** Otimização é essencial para processar 1000/dia
- **Semana 4:** Deploy e validação - reservar tempo para imprevistos