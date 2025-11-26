package com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.service.SyntheticToolHemogramGeneratorService;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaFhirParserService;

@RestController
@RequestMapping("/tools")
public class ToolsController {

    private final SyntheticToolHemogramGeneratorService syntheticHemogramGeneratorService;
    
    @Autowired
    private HemogramaFhirParserService hemogramaParserService;
    
    @Autowired
    private RestTemplate restTemplate;

    public ToolsController(SyntheticToolHemogramGeneratorService syntheticHemogramGeneratorService) {
        this.syntheticHemogramGeneratorService = syntheticHemogramGeneratorService;
    }

    @PostMapping("/send-to-hapi")
    public ResponseEntity<Map<String, Object>> sendToHapi(
            @RequestParam(value = "qtde", defaultValue = "10") int quantity) {

        try {
            if (quantity <= 0 || quantity > 1000) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Quantidade deve estar entre 1 e 1000")
                );
            }

            List<String> createdIds = syntheticHemogramGeneratorService.generateAndSendToHapi(quantity);

            return ResponseEntity.ok(Map.of(
                    "message", "Observations enviadas com sucesso para o HAPI",
                    "quantidadeSolicitada", quantity,
                    "quantidadeCriada", createdIds.size(),
                    "idsCriados", createdIds
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Erro ao enviar observations para HAPI: " + e.getMessage())
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Tenta enviar uma observation de teste
            List<String> testResult = syntheticHemogramGeneratorService.generateAndSendToHapi(1);

            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "hapiFhirServer", "Conectado",
                    "testObservationCreated", !testResult.isEmpty()
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "DOWN",
                    "hapiFhirServer", "Desconectado",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/processar-hapi-manual")
    public ResponseEntity<Map<String, Object>> processarHapiManual() {
        try {
            // 1. Busca observations reais do HAPI
            String hapiUrl = "http://localhost:8090/fhir/Observation?_count=20&_sort=-_lastUpdated";
            
            ResponseEntity<String> response = restTemplate.getForEntity(hapiUrl, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                return ResponseEntity.ok(Map.of(
                    "erro", "Não foi possível buscar observations do HAPI",
                    "status", "erro",
                    "hapiFhirUrl", hapiUrl
                ));
            }
            
            String fhirBundle = response.getBody();
            
            // 2. Processa através do parser (vai detectar HIV e incrementar contadores)
            var resultados = hemogramaParserService.processarNotificacaoFhir(fhirBundle);
            
            long casosHivDetectados = resultados.stream()
                .mapToLong(h -> h.isRiscoHiv() ? 1 : 0)
                .sum();
            
            // 3. Monta resposta
            return ResponseEntity.ok(Map.of(
                "status", "sucesso",
                "observationsEncontradas", contarObservations(fhirBundle),
                "hemogramasProcessados", resultados.size(),
                "casosHivDetectados", casosHivDetectados,
                "message", String.format("✅ Processamento concluído: %d hemogramas analisados, %d casos HIV detectados", 
                                       resultados.size(), casosHivDetectados),
                "detalhes", resultados.stream()
                    .filter(h -> h.isRiscoHiv())
                    .map(h -> Map.of(
                        "id", h.getObservationId(),
                        "motivo", h.getMotivoRisco()
                    ))
                    .toList()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "erro", e.getMessage(),
                "status", "erro",
                "stackTrace", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * 🆕 MÉTODO CORRIGIDO - Ativa webhook automático no HAPI FHIR
     * Cria subscription para que HAPI chame automaticamente o FhirSubscriptionController
     */
    @PostMapping("/ativar-webhook-automatico")
    public ResponseEntity<Map<String, Object>> ativarWebhookAutomatico() {
        try {
            // ✅ CORRIGIDO: Mudei o payload para application/json
            String subscriptionJson = """
            {
              "resourceType": "Subscription",
              "status": "active",
              "criteria": "Observation?code=58410-2",
              "channel": {
                "type": "rest-hook", 
                "endpoint": "http://host.docker.internal:8080/fhir/subscription",
                "payload": "application/json"
              }
            }
            """;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(subscriptionJson, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8090/fhir/Subscription", 
                HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                return ResponseEntity.ok(Map.of(
                    "status", "✅ WEBHOOK AUTOMÁTICO ATIVADO!",
                    "endpoint", "/fhir/subscription", 
                    "webhookController", "FhirSubscriptionController",
                    "message", "HAPI vai chamar automaticamente seu webhook quando observations forem criadas",
                    "subscriptionId", extrairIdSubscription(response.getBody()),
                    "comoTestar", "Faça POST /tools/send-to-hapi e monitore os logs do FhirSubscriptionController",
                    "payloadType", "application/json"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "erro", "Falha ao criar subscription no HAPI", 
                    "status", response.getStatusCode().toString(),
                    "body", response.getBody()
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "erro", "Erro ao configurar webhook: " + e.getMessage(),
                "status", "erro",
                "dica", "Verifique se o HAPI FHIR está rodando em http://localhost:8090"
            ));
        }
    }

    /**
     * 🆕 MÉTODO AUXILIAR - Verifica status das subscriptions ativas
     */
    @GetMapping("/verificar-subscriptions")
    public ResponseEntity<Map<String, Object>> verificarSubscriptions() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:8090/fhir/Subscription", String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                int totalSubscriptions = contarSubscriptions(body);
                
                return ResponseEntity.ok(Map.of(
                    "status", "sucesso",
                    "totalSubscriptions", totalSubscriptions,
                    "hapiFhirStatus", "conectado",
                    "subscriptionsAtivas", totalSubscriptions > 0,
                    "message", totalSubscriptions > 0 ? 
                        "Webhook automático está ativo!" : 
                        "Nenhuma subscription ativa. Use POST /tools/ativar-webhook-automatico"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "erro", "Falha ao consultar HAPI",
                    "status", response.getStatusCode()
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "erro", "Erro ao verificar subscriptions: " + e.getMessage(),
                "status", "erro"
            ));
        }
    }

    /**
     * 🆕 MÉTODO DE TESTE - Limpa todas as subscriptions (útil para reset)
     */
    @PostMapping("/limpar-subscriptions")
    public ResponseEntity<Map<String, Object>> limparSubscriptions() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:8090/fhir/Subscription", String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                // Esta implementação é simplificada - em produção seria mais robusta
                return ResponseEntity.ok(Map.of(
                    "status", "info",
                    "message", "Para limpar subscriptions, reinicie o HAPI FHIR ou use a interface administrativa"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "erro", "Falha ao acessar HAPI",
                    "status", response.getStatusCode()
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "erro", "Erro: " + e.getMessage(),
                "status", "erro"
            ));
        }
    }

    private int contarObservations(String fhirBundle) {
        try {
            return (int) fhirBundle.split("\"resourceType\"\\s*:\\s*\"Observation\"").length - 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int contarSubscriptions(String fhirBundle) {
        try {
            return (int) fhirBundle.split("\"resourceType\"\\s*:\\s*\"Subscription\"").length - 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String extrairIdSubscription(String responseBody) {
        try {
            if (responseBody != null && responseBody.contains("\"id\"")) {
                int start = responseBody.indexOf("\"id\"") + 5;
                int end = responseBody.indexOf("\"", start);
                return responseBody.substring(start, end);
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }
}