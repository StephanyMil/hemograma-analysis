### **Cronograma de Desenvolvimento: Sistema de Monitoramento de Hemogramas**

**Equipe:** 4 Pessoas (divididas em Dupla 1 e Dupla 2)

**Responsabilidades:**
* **Dupla 1: Time de Backend** (Java, Spring Boot, FHIR, Banco de Dados, API)
* **Dupla 2: Time de Frontend** (React Native, UI/UX, Consumo de API, Notificações)

---

### **Marco 1 - Recepção FHIR (Backend) e Estruturação (Frontend)**
**Prazo Final: 12/09**

| Atividades Principais | Responsáveis |
| :--- | :--- |
| **1. [Backend] Configuração e Receptor FHIR:** Setup do ambiente Spring Boot, Docker, e criação do endpoint para receber as `subscriptions` FHIR. | **Dupla 1** |
| **2. [Backend] Parser FHIR e Extração de Dados:** Implementar a lógica com HAPI-FHIR para extrair os parâmetros do hemograma (Leucócitos, etc.) do JSON. | **Dupla 1** |
| **3. [Frontend] Configuração do Ambiente e Estrutura:** Setup do ambiente React Native, bibliotecas de navegação e UI. Criação da estrutura de pastas e navegação básica do app. | **Dupla 2** |
| **4. [Frontend] Desenvolvimento de UI com Mocks:** Criar os componentes visuais e as telas principais (lista de alertas, detalhes do alerta) utilizando dados fictícios (mockados). | **Dupla 2** |
| **5. [Todos] Documentação Inicial:** Colaborar no README.md com as definições de arquitetura e instruções de setup para ambas as frentes. | **Todos** |

---

### **Marco 2 - Análise Individual (Backend) e UI (Frontend)**
**Prazo Final: 19/09**

| Atividades Principais | Responsáveis |
| :--- | :--- |
| **1. [Backend] Lógica de Análise Individual:** Implementar a classificação dos parâmetros do hemograma com base na tabela de referência para gerar alertas. | **Dupla 1** |
| **2. [Backend] Testes Unitários:** Escrever testes para a lógica de parsing e análise individual. | **Dupla 1** |
| **3. [Frontend] Finalização das Telas:** Concluir o desenvolvimento das interfaces do app, garantindo uma boa experiência de usuário, ainda com dados fictícios. | **Dupla 2** |
| **4. [Frontend] Definição do Contrato da API:** Definir as estruturas de dados (ex: interfaces TypeScript) que o app espera receber da API, para facilitar a integração futura. | **Dupla 2** |

---

### **Marco 3 - Base de Dados (Backend) e Preparação para Integração (Frontend)**
**Prazo Final: 03/10**

| Atividades Principais | Responsáveis |
| :--- | :--- |
| **1. [Backend] Modelagem e Persistência de Dados:** Modelar o banco de dados PostgreSQL, configurar o Spring Data JPA e implementar a lógica para salvar hemogramas e alertas. | **Dupla 1** |
| **2. [Backend] Testes da Camada de Persistência:** Criar testes para garantir que os dados estão sendo salvos e recuperados corretamente. | **Dupla 1** |
| **3. [Frontend] Gestão de Estado e Serviços:** Implementar a camada de gestão de estado (ex: Context API, Redux) e os serviços de cliente HTTP (ex: Axios) para consumir a futura API. | **Dupla 2** |
| **4. [Frontend] Lógica de Loading e Erro:** Preparar a UI para lidar com estados de carregamento (loading) e possíveis erros de comunicação com a API. | **Dupla 2** |

---

### **Marco 4 - Análise Coletiva e API (Backend) e Integração (Frontend)**
**Prazo Final: 17/10**

| Atividades Principais | Responsáveis |
| :--- | :--- |
| **1. [Backend] Lógica de Análise Coletiva:** Implementar os cálculos em janelas deslizantes e os gatilhos para gerar alertas coletivos. | **Dupla 1** |
| **2. [Backend] Desenvolvimento da API REST:** Construir e documentar (Swagger/OpenAPI) os endpoints para consulta de alertas individuais e coletivos. | **Dupla 1** |
| **3. [Frontend] Integração com a API REST:** Substituir todos os dados fictícios do app por chamadas reais aos endpoints desenvolvidos pela Dupla 1. | **Dupla 2** |
| **4. [Frontend] Visualização de Dados Reais:** Ajustar as telas para exibir corretamente os dados dinâmicos vindos do backend. | **Dupla 2** |

---

### **Entrega Final - Notificações e Apresentação**
**Prazo Final: 05/12**

| Atividades Principais | Responsáveis |
| :--- | :--- |
| **1. [Backend] Implementação do Envio de Notificações (FCM):** Desenvolver no backend a lógica para se comunicar com o Firebase Cloud Messaging e disparar as notificações push. | **Dupla 1** |
| **2. [Frontend] Recebimento de Notificações Push:** Implementar no app a lógica para receber, processar e exibir as notificações enviadas pelo backend. | **Dupla 2** |
| **3. [Frontend] Polimento e Finalização do App:** Realizar os ajustes finais de UI/UX, corrigir bugs e garantir a estabilidade da aplicação. | **Dupla 2** |
| **4. [Todos] Testes de Integração Ponta-a-Ponta:** Realizar testes conjuntos para validar o fluxo completo: do recebimento do hemograma no backend até a exibição da notificação no app. | **Todos** |
| **5. [Todos] Preparação da Apresentação Final:** Consolidar a documentação e preparar a demonstração completa do sistema. | **Todos** |
