Semana 1 — 05/10 a 11/10 (fechar a “ponta a ponta”)
Aline Nunes

 Revisar logs do parser (HemogramaFhirParserService) e garantir mensagens claras quando não encontrar CBC.

 Revisar endpoints e mensagem de erro que não foi gerada



Ayumi

 Criar Sender simples (service + command endpoint) para enviar Observations ao HAPI:

Endpoint sugerido: POST /tools/send-to-hapi?qtde=100

Internamente: chama SyntheticHemogramGeneratorService → para cada JSON, POST no HAPI Observation (http://localhost:8090/fhir/Observation)

Logar ids criados pelo HAPI.

 Criar um script .http (ou coleção Postman) com as chamadas de teste (gerar, enviar, health).

 PR: “feat: sender HAPI + coleção de testes”.

Aline Lima

 Criar endpoint de consulta para o frontend (sem persistir nada por enquanto):

GET /api/hemogramas/recentes: retorna lista com campos mínimos (id, data, leucócitos, hemoglobina, plaquetas, hematócrito).

Fonte dos dados: option A: consultar diretamente o HAPI (/Observation?code=58410-2&_sort=-date&_count=20) usando um client REST no back OU
option B: manter buffer em memória com os últimos recebidos via subscription (simples Map/List estática).

GET /api/hemogramas/{id}: retorna detalhe (mesmos campos).

 Incluir exemplos no rotas.md (depois do merge da Aline N.).

 PR: “feat: endpoints de leitura p/ frontend (recentes + detalhe)”.

Stephany

 Criar app RN (se já existe, só organizar pastas):

Telas: Dashboard, Lista, Detalhe.

Serviço HTTP (Axios) com baseURL configurável.

 Mock primeiro: arquivo mock/hemogramas.json com os mesmos campos que o endpoint do back vai expor.

 Gráficos (lib sugerida): react-native-svg + victory-native ou react-native-chart-kit.

Dashboard: 2 gráficos simples (ex.: distribuição de hemoglobina, trend de leucócitos).

 README do mobile com como rodar e prints.

 PR: “feat: dashboard + gráficos com mock + estrutura http”.

Checkpoint:

Enviar 100 Observations ao HAPI com o sender.

Receber via /fhir/subscription (log ok).

Front abrindo Dashboard com mock e conseguindo trocar para API se o endpoint /api/hemogramas/recentes já estiver pronto.

Semana 2 — 12/10 a 17/10 (travar demo e polir)
Aline Nunes

 Ajustar CORS (se o mobile rodar em outro host/porta).

 Revisar SecurityConfig (rotas que o mobile usa devem estar liberadas ou com basic auth documentada).

 rotas.md: adicionar erros comuns (ex.: 201 sem log, como vimos) e como depurar.

Ayumi

 Melhorar respostas do sender: retornar JSON com { enviados, criados: [ {id, status} ] }.

 Atalho de testes: endpoint GET /tools/hapi/ping para checar HAPI on-line (status 200).

Aline Lima

 Endpoint GET /api/metrics/resumo para o Dashboard:

Ex.: { totalObservations, mediaHemoglobina, mediaLeucocitos, ultimosDias:[{dia, qtd}] }

Fonte: computar em memória a partir do buffer OU consultar HAPI e agregar (simples).

 Paginação server-side em /api/hemogramas/recentes (parâmetros page, size).

Stephany

 Substituir mock pelo cliente HTTP real (feature flag: USE_API=true em .env).

 Tela de Lista consumindo /api/hemogramas/recentes com paginação/scroll.

 Tela Detalhe consumindo /api/hemogramas/{id}.

 Dashboard consumindo /api/metrics/resumo.

 Polimento de UI (loading, empty states, erros).