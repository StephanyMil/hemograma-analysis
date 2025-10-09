package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serviço que gera hemogramas sintéticos (mockados) em formato FHIR Observation
 * para simular pacientes com diferentes condições.
 */
@Service
public class SyntheticHemogramGeneratorService {

    private static final Random random = new Random();

    /**
     * Gera uma lista de hemogramas sintéticos completos (em JSON FHIR)
     * 
     * @param quantidade número de hemogramas a gerar
     * @return lista de hemogramas em formato JSON (como Strings)
     */
    public List<String> gerarHemogramasSinteticos(int quantidade) {
        List<String> hemogramas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            hemogramas.add(gerarHemograma());
        }
        return hemogramas;
    }

    /**
     * Gera um hemograma sintético no formato FHIR Observation
     */
    private String gerarHemograma() {
        Map<String, Object> observation = new HashMap<>();
        observation.put("resourceType", "Observation");
        observation.put("status", "final");

        Map<String, Object> code = new HashMap<>();
        code.put("coding", List.of(Map.of(
            "system", "http://loinc.org",
            "code", "58410-2",
            "display", "CBC panel - Blood by Automated count"
        )));
        observation.put("code", code);

        observation.put("effectiveDateTime", gerarDataAleatoria());

        // Componentes do hemograma
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(criarComponente("6690-2", "Leukocytes", gerarValor(3500, 11000), "/µL"));
        components.add(criarComponente("718-7", "Hemoglobin", gerarValor(11.0, 16.0), "g/dL"));
        components.add(criarComponente("777-3", "Platelets", gerarValor(150000, 400000), "/µL"));
        components.add(criarComponente("4544-3", "Hematocrit", gerarValor(35.0, 47.0), "%"));
        components.add(criarComponente("789-8", "Erythrocytes", gerarValor(3.8, 5.3), "milhões/mm³"));

        observation.put("component", components);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(observation);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar JSON do hemograma", e);
        }
    }

    private Map<String, Object> criarComponente(String code, String display, double value, String unit) {
        Map<String, Object> component = new HashMap<>();
        component.put("code", Map.of(
            "coding", List.of(Map.of(
                "system", "http://loinc.org",
                "code", code,
                "display", display
            ))
        ));
        component.put("valueQuantity", Map.of(
            "value", value,
            "unit", unit
        ));
        return component;
    }

    private double gerarValor(double min, double max) {
        return Math.round((min + (max - min) * random.nextDouble()) * 100.0) / 100.0;
    }

    private String gerarDataAleatoria() {
        int ano = 2024 + random.nextInt(2);
        int mes = 1 + random.nextInt(12);
        int dia = 1 + random.nextInt(28);
        return String.format("%d-%02d-%02dT%02d:%02d:00Z", ano, mes, dia, random.nextInt(24), random.nextInt(60));
    }
}
