package com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.model.Observation;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Service
public class FhirObservationService {

    private final ObjectMapper objectMapper;

    public FhirObservationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String convertToFhirObservation(Observation observation) {
        try {
            ObjectNode fhirObservation = objectMapper.createObjectNode();

            // Resource type
            fhirObservation.put("resourceType", "Observation");

            // ID
            if (observation.getId() != null) {
                fhirObservation.put("id", observation.getId());
            }

            // Status
            fhirObservation.put("status", observation.getStatus());

            // Category - laboratory
            ObjectNode category = objectMapper.createObjectNode();
            ObjectNode coding = objectMapper.createObjectNode();
            coding.put("system", "http://terminology.hl7.org/CodeSystem/observation-category");
            coding.put("code", "laboratory");
            coding.put("display", "Laboratory");
            category.set("coding", objectMapper.createArrayNode().add(coding));
            fhirObservation.set("category", objectMapper.createArrayNode().add(category));

            // Code - Hemograma
            ObjectNode code = objectMapper.createObjectNode();
            ObjectNode codeCoding = objectMapper.createObjectNode();
            codeCoding.put("system", "http://loinc.org");
            codeCoding.put("code", "58410-2");
            codeCoding.put("display", "Complete blood count panel");
            code.set("coding", objectMapper.createArrayNode().add(codeCoding));
            code.put("text", "Hemograma Completo");
            fhirObservation.set("code", code);

            // Subject (paciente de referência)
            ObjectNode subject = objectMapper.createObjectNode();
            subject.put("reference", "Patient/example");
            fhirObservation.set("subject", subject);

            // Effective date time
            String effectiveDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            fhirObservation.put("effectiveDateTime", effectiveDateTime);

            // Issued
            fhirObservation.put("issued", effectiveDateTime);

            // Components do hemograma
            if (observation.getComponents() != null && !observation.getComponents().isEmpty()) {
                fhirObservation.set("component", createComponents(observation));
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fhirObservation);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter para FHIR Observation", e);
        }
    }

    private com.fasterxml.jackson.databind.JsonNode createComponents(Observation observation) {
        var componentsArray = objectMapper.createArrayNode();

        observation.getComponents().forEach((code, component) -> {
            ObjectNode componentNode = objectMapper.createObjectNode();

            // Code do componente
            ObjectNode componentCode = objectMapper.createObjectNode();
            ObjectNode coding = objectMapper.createObjectNode();

            // Mapeia códigos LOINC para componentes do hemograma
            String loincCode = getLoincCodeForComponent(code);
            String displayName = getDisplayNameForComponent(code);

            coding.put("system", "http://loinc.org");
            coding.put("code", loincCode);
            coding.put("display", displayName);
            componentCode.set("coding", objectMapper.createArrayNode().add(coding));
            componentNode.set("code", componentCode);

            // Value
            ObjectNode valueQuantity = objectMapper.createObjectNode();
            valueQuantity.put("value", component.getValue());
            valueQuantity.put("unit", component.getUnit());
            valueQuantity.put("system", "http://unitsofmeasure.org");
            valueQuantity.put("code", getUcumCode(component.getUnit()));

            componentNode.set("valueQuantity", valueQuantity);

            componentsArray.add(componentNode);
        });

        return componentsArray;
    }

    private String getLoincCodeForComponent(String component) {
        return switch (component.toLowerCase()) {
            case "hemoglobin" -> "718-7";
            case "hematocrit" -> "4544-3";
            case "red-blood-cells" -> "789-8";
            case "white-blood-cells" -> "6690-2";
            case "platelets" -> "777-3";
            case "neutrophils" -> "770-8";
            case "lymphocytes" -> "736-9";
            case "monocytes" -> "5905-5";
            default -> "unknown";
        };
    }

    private String getDisplayNameForComponent(String component) {
        return switch (component.toLowerCase()) {
            case "hemoglobin" -> "Hemoglobin";
            case "hematocrit" -> "Hematocrit";
            case "red-blood-cells" -> "Erythrocytes";
            case "white-blood-cells" -> "Leukocytes";
            case "platelets" -> "Platelets";
            case "neutrophils" -> "Neutrophils";
            case "lymphocytes" -> "Lymphocytes";
            case "monocytes" -> "Monocytes";
            default -> component;
        };
    }

    private String getUcumCode(String unit) {
        return switch (unit) {
            case "g/dL" -> "g/dL";
            case "%" -> "%";
            case "million/mm³" -> "10*6/mm3";
            case "thousand/mm³" -> "10*3/mm3";
            default -> unit;
        };
    }
}