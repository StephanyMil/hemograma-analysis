package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service.HemogramaFhirParserService;

@RestController
@RequestMapping("/fhir")
public class FhirSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(FhirSubscriptionController.class);
    
    @Autowired
    private HemogramaFhirParserService hemogramaParser;

    /**
     * Endpoint que recebe as notificações de subscription do servidor FHIR
     * Quando um novo hemograma (Observation) é criado, o servidor FHIR chama este endpoint
     */
    @PostMapping("/subscription")
    public ResponseEntity<String> receiveSubscriptionNotification(@RequestBody String fhirBundle) {
        
        logger.info("=== NOTIFICAÇÃO FHIR RECEBIDA ===");
        logger.info("Tamanho do payload: {} caracteres", fhirBundle.length());
        
        try {
            // Processa o FHIR Bundle e extrai os hemogramas
            List<HemogramaFhirParserService.HemogramaData> hemogramas = 
                hemogramaParser.processarNotificacaoFhir(fhirBundle);
            
            if (hemogramas.isEmpty()) {
                logger.warn("Nenhum hemograma encontrado no Bundle FHIR");
                return ResponseEntity.ok("Bundle processado, mas nenhum hemograma encontrado");
            }
            
            // Por enquanto, apenas logamos os dados extraídos
            // No próximo marco, vamos salvar no banco de dados
            for (HemogramaFhirParserService.HemogramaData hemograma : hemogramas) {
                logger.info("Hemograma processado: {}", hemograma);
                
                // Aqui você vai implementar a lógica para:
                // 1. Salvar no banco de dados (Marco 3)
                // 2. Análise individual (Marco 2)
                // 3. Análise coletiva (Marco 4)
            }
            
            logger.info("Processamento concluído com sucesso. {} hemogramas processados", hemogramas.size());
            return ResponseEntity.ok(String.format("Processados %d hemogramas com sucesso", hemogramas.size()));
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação FHIR: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erro no processamento: " + e.getMessage());
        }
    }

    /**
     * Endpoint de teste para verificar se o controller está funcionando
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller FHIR funcionando!");
    }
    
    /**
     * Endpoint de teste para simular o recebimento de um hemograma FHIR
     */
    @PostMapping("/test-hemograma")
    public ResponseEntity<String> testHemograma() {
        
        String exemploFhir = """
        {
          "resourceType": "Bundle",
          "id": "hemograma-bundle-exemplo",
          "type": "collection",
          "entry": [
            {
              "resource": {
                "resourceType": "Observation",
                "id": "hemograma-123",
                "status": "final",
                "code": {
                  "coding": [
                    {
                      "system": "http://loinc.org",
                      "code": "58410-2",
                      "display": "CBC panel - Blood by Automated count"
                    }
                  ]
                },
                "effectiveDateTime": "2025-09-06T10:30:00Z",
                "component": [
                  {
                    "code": {
                      "coding": [
                        {
                          "system": "http://loinc.org",
                          "code": "6690-2",
                          "display": "Leukocytes"
                        }
                      ]
                    },
                    "valueQuantity": {
                      "value": 8500,
                      "unit": "/µL"
                    }
                  },
                  {
                    "code": {
                      "coding": [
                        {
                          "system": "http://loinc.org",
                          "code": "718-7",
                          "display": "Hemoglobin"
                        }
                      ]
                    },
                    "valueQuantity": {
                      "value": 14.2,
                      "unit": "g/dL"
                    }
                  },
                  {
                    "code": {
                      "coding": [
                        {
                          "system": "http://loinc.org",
                          "code": "777-3",
                          "display": "Platelets"
                        }
                      ]
                    },
                    "valueQuantity": {
                      "value": 250000,
                      "unit": "/µL"
                    }
                  },
                  {
                    "code": {
                      "coding": [
                        {
                          "system": "http://loinc.org",
                          "code": "4544-3",
                          "display": "Hematocrit"
                        }
                      ]
                    },
                    "valueQuantity": {
                      "value": 42,
                      "unit": "%"
                    }
                  }
                ]
              }
            }
          ]
        }
        """;
        
        logger.info("Testando processamento com exemplo de hemograma FHIR");
        return receiveSubscriptionNotification(exemploFhir);
    }
}