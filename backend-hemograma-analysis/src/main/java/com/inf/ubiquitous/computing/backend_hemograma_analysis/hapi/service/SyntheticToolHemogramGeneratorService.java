package com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.model.Observation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SyntheticToolHemogramGeneratorService {

    private static final Logger logger = Logger.getLogger(SyntheticToolHemogramGeneratorService.class.getName());

    @Value("${hapi.fhir.server.url:http://localhost:8090/fhir}")
    private String hapiFhirServerUrl;

    private final RestTemplate restTemplate;
    private final FhirObservationService fhirObservationService;

    public SyntheticToolHemogramGeneratorService(RestTemplate restTemplate, FhirObservationService fhirObservationService) {
        this.restTemplate = restTemplate;
        this.fhirObservationService = fhirObservationService;
    }

    public List<String> generateAndSendToHapi(int quantity) {
        List<String> createdIds = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            try {
                // Gera um hemograma sintético
                Observation observation = generateSyntheticHemogram(i);

                // Envia para o HAPI FHIR server
                String createdId = sendObservationToHapi(observation);

                if (createdId != null) {
                    createdIds.add(createdId);
                    logger.info("Observation criada com ID: " + createdId);
                }

            } catch (Exception e) {
                logger.severe("Erro ao criar observation " + i + ": " + e.getMessage());
            }
        }

        return createdIds;
    }

    private Observation generateSyntheticHemogram(int index) {
        Observation observation = new Observation();
        observation.setId("synthetic-hemogram-" + System.currentTimeMillis() + "-" + index);
        observation.setStatus("final");

        // Adiciona componentes do hemograma
        observation.addComponent("hemoglobin", "g/dL", generateValue(12.0, 16.0));
        observation.addComponent("hematocrit", "%", generateValue(36.0, 48.0));
        observation.addComponent("red-blood-cells", "million/mm³", generateValue(4.2, 5.9));
        observation.addComponent("white-blood-cells", "thousand/mm³", generateValue(4.5, 11.0));
        observation.addComponent("platelets", "thousand/mm³", generateValue(150.0, 450.0));
        observation.addComponent("neutrophils", "%", generateValue(40.0, 75.0));
        observation.addComponent("lymphocytes", "%", generateValue(20.0, 50.0));
        observation.addComponent("monocytes", "%", generateValue(2.0, 10.0));

        return observation;
    }

    private double generateValue(double min, double max) {
        return min + (Math.random() * (max - min));
    }

    private String sendObservationToHapi(Observation observation) {
        try {
            // Converte para FHIR Observation resource
            String fhirObservationJson = fhirObservationService.convertToFhirObservation(observation);

            // Configura headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<String> request = new HttpEntity<>(fhirObservationJson, headers);

            // Faz POST para o HAPI FHIR server
            String url = hapiFhirServerUrl + "/Observation";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                // Extrai o ID da resposta do HAPI
                return extractIdFromResponse(response.getBody());
            } else {
                logger.warning("Falha ao criar observation. Status: " + response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            logger.severe("Erro ao enviar para HAPI: " + e.getMessage());
            return null;
        }
    }

    private String extractIdFromResponse(String responseBody) {
        try {
            // Simples extração do ID - você pode usar uma biblioteca JSON para parsing mais robusto
            if (responseBody.contains("\"id\"")) {
                int start = responseBody.indexOf("\"id\"") + 5;
                int end = responseBody.indexOf("\"", start);
                return responseBody.substring(start, end);
            }
        } catch (Exception e) {
            logger.warning("Não foi possível extrair ID da resposta: " + e.getMessage());
        }
        return "unknown-id";
    }
}