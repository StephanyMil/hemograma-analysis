Backend Hemograma Analysis — README

### Autenticação

#### API de Autenticação - Documentação

Base URL: `/api/auth`

---

#### 1. Verificar Primeiro Usuário

Verifica se o sistema já possui usuários cadastrados.

**Endpoint:** `GET /api/auth/is-first-user`

**Autenticação:** Não requerida

 Resposta de Sucesso (200 OK)

```json
{
  "isFirstUser": true,
  "userCount": 0
}
```

**Campos da resposta:**
- `isFirstUser` (boolean): `true` se não há usuários no sistema, `false` caso contrário
- `userCount` (number): Quantidade total de usuários cadastrados
---

#### 2. Registrar Primeiro Usuário

Registra o primeiro usuário do sistema. Este endpoint **só funciona quando não há usuários cadastrados**.

**Endpoint:** `POST /api/auth/register-first-user`

**Autenticação:** Não requerida

#### Request Body

```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Campos obrigatórios:**
- `name` (string): Nome completo do usuário
- `email` (string): Email válido do usuário
- `password` (string): Senha do usuário

### Respostas

 Sucesso (201 Created)

```json
{
  "id": "uuid-do-usuario",
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "$2a$10$hashedPassword..."
}
```

 Erro - Sistema já possui usuários (403 Forbidden)

```json
"System already has users. This endpoint is only for initial setup."
```

#### Erro - Email já em uso (409 Conflict)

```json
"Email already in use."
```

#### Erro - Validação (400 Bad Request)

Retornado quando campos obrigatórios estão ausentes ou inválidos.

---

## 3. Login

Autentica um usuário e retorna um token JWT.

**Endpoint:** `POST /api/auth/login`

**Autenticação:** Não requerida

### Request Body

```json
{
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Campos obrigatórios:**
- `email` (string): Email do usuário
- `password` (string): Senha do usuário

### Respostas

#### Sucesso (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful!"
}
```

**Campos da resposta:**
- `token` (string): Token JWT para autenticação nas próximas requisições
- `message` (string): Mensagem de sucesso
---

#### 4. Logout

Limpa o contexto de segurança do usuário no servidor.

**Endpoint:** `POST /api/auth/logout`

**Autenticação:** Não requerida (mas recomendado enviar o token)

 Resposta de Sucesso (200 OK)

```json
"Logout successful!"
```

---

## Notas Importantes

- **Token JWT:** Após o login bem-sucedido, use o token retornado no header `Authorization: Bearer {token}` para todas as requisições autenticadas
- **Validade do Token:** Verifique com o backend a validade/expiração do token
- **Segurança:** Nunca exponha senhas em logs ou console do navegador
- **HTTPS:** Em produção, sempre use HTTPS para proteger as credenciais durante a transmissão

## 1) O que este projeto faz (estado atual)

Recebe notificações FHIR (REST-hook) vindas do HAPI FHIR e parseia um Bundle com Observations de hemograma (CBC).

Possui endpoints de teste/saúde.

Gera hemogramas mockados:

Um hemograma simples (não-FHIR), só com números aleatórios.

N hemogramas FHIR Observation (JSON) para inspeção (retorna uma lista de strings JSON).

Importante: não há persistência em banco para hemogramas no estado atual; o parser processa e loga os dados.

2) Subir os serviços com Docker

Arquivo: docker-compose.yml

Serviços:

HAPI FHIR (container hapi_fhir) → porta 8090 no host (mapeada para 8080 no container)

PostgreSQL (container hemograma_postgres) → porta 5432 no host

Subir só o Postgres
docker compose -f docker-compose.yml up -d postgres

Subir o HAPI FHIR
docker compose -f docker-compose.yml up -d hapi

Verificar containers
docker ps


HAPI: abrir em http://localhost:8090

Postgres: localhost:5432 (db hemograma_db, user postgres, senha postgres)

Obs.: o HAPI está configurado, do lado dele, para enviar REST-hook para
http://host.docker.internal:8080/fhir/subscription (endereço do seu backend rodando localmente).

3) Rodar o backend (Spring Boot)

Escolha uma opção:

a) Via IDE (IntelliJ/VS Code/Eclipse)

Rodar a classe principal (ex.: BackendHemogramaAnalysisApplication).

Porta padrão: 8080.

b) Via Maven
mvn spring-boot:run

c) Via JAR
mvn clean package
java -jar target/<seu-jar>.jar

4) Endpoints disponíveis (o que já existe)
Health / Teste do controller FHIR

GET http://localhost:8080/fhir/subscription/health → retorna ok

GET http://localhost:8080/fhir/test → retorna Controller FHIR funcionando!

Receber notificação do HAPI (REST-hook)

POST http://localhost:8080/fhir/subscription

Body: Bundle FHIR enviado pelo HAPI (não incluímos exemplo aqui porque quem envia é o HAPI).

Respostas típicas:

200 OK — “Processados X hemogramas com sucesso”

200 OK — “Bundle processado, mas nenhum hemograma encontrado”

400 BAD REQUEST — “Payload vazio”

500 INTERNAL SERVER ERROR — erro de parse

Se tudo ocorrer bem, você verá logs no terminal com:
“Bundle FHIR parseado…”, “Hemograma processado: …” e o total de hemogramas.

Gerar hemograma simples (não-FHIR)

GET http://localhost:8080/hemograma/gerar

Sem body. Retorna JSON com campos: leucocitos, hemoglobina, plaquetas, hematocrito, hemacias, observacao.

Gerar N hemogramas FHIR Observation (lista de JSON em String)

GET http://localhost:8080/fhir/synthetic/generate?qtde=5

Sem body. Retorna uma lista de strings. Cada string já é um JSON FHIR (Observation) com componentes (LOINC corretos).

5) O que cada classe faz (sem alterações)
FhirSubscriptionController (...user.controller)

Rotas:

POST /fhir/subscription

GET /fhir/subscription/health

GET /fhir/test

Função: recebe o payload FHIR do HAPI, loga informações, chama o parser e retorna um texto com o resultado.

Observação: no arquivo atual existem duas funções @PostMapping("/subscription"). Mantenha apenas uma para evitar ambiguidade de rota.

HemogramaFhirParserService (...user.service)

Função: usa HAPI FHIR para parsear o Bundle e extrair hemogramas (CBC).

Como identifica CBC: códigos LOINC (58410-2, 57021-8) ou display “hemograma”/“complete blood count”.

Componentes mapeados (lê valueQuantity.value e unit):

Leucócitos: 6690-2 ou 33747-0

Hemoglobina: 718-7 ou 30313-1

Plaquetas: 777-3 ou 26515-7

Hematócrito: 4544-3 ou 31100-1

Retorno: lista de HemogramaData (classe interna com id, data, valores e unidades).

HemogramaGeneratorController (...user.controller)

Rota: GET /hemograma/gerar

Função: gera um hemograma simples (não-FHIR) com números aleatórios em JSON. Útil para teste rápido.

SyntheticHemogramController (...user.controller)

Rota: GET /fhir/synthetic/generate?qtde=N

Função: chama o service e retorna N JSON FHIR (cada item como string).

SyntheticHemogramGeneratorService (...user.service)

Função: monta hemogramas FHIR Observation (JSON) com componentes e effectiveDateTime aleatórios.
Retorna uma lista de strings JSON (pretty-printed).

HemogramaGeneratorService (...user.service)

Função: gera objetos Observation (HAPI FHIR) em memória.
Não possui endpoint público; serve como utilitário.

SecurityConfig (...user.service)

Função: desativa CSRF para testes e libera:

/fhir/synthetic/**

/fhir/subscription/**

/fhir/test-hemograma

/fhir/test

Demais rotas ficam sob HTTP Basic (se existirem).