package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serviço que gera hemogramas sintéticos (mockados) em formato FHIR Observation
 * para simular pacientes com diferentes condições, empacotados em um Bundle.
 * 🚨 VERSÃO CORRIGIDA: Agora gera casos com risco HIV automaticamente!
 */
@Service
public class SyntheticHemogramGeneratorService {

    private static final Random random = new Random();

    /**
     * Gera um único recurso FHIR Bundle contendo múltiplos hemogramas sintéticos.
     *
     * @param quantidade número de hemogramas a gerar dentro do Bundle.
     * @return Um Bundle FHIR em formato JSON (como String).
     */
    public String gerarHemogramasSinteticos(int quantidade) {
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("id", UUID.randomUUID().toString());
        bundle.put("type", "collection");

        List<Map<String, Object>> entries = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Map<String, Object> observation = gerarHemograma();

            Map<String, Object> entry = new HashMap<>();
            entry.put("fullUrl", "urn:uuid:" + observation.get("id"));
            entry.put("resource", observation);
            entries.add(entry);
        }

        bundle.put("entry", entries);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar JSON do Bundle de hemogramas", e);
        }
    }

    /**
     * 🚨 MÉTODO CORRIGIDO: Gera hemograma com chance de ter risco HIV
     */
    private Map<String, Object> gerarHemograma() {
        Map<String, Object> observation = new HashMap<>();
        observation.put("resourceType", "Observation");
        observation.put("id", UUID.randomUUID().toString());
        observation.put("status", "final");

        Map<String, Object> code = new HashMap<>();
        code.put("coding", List.of(Map.of(
                "system", "http://loinc.org",
                "code", "58410-2",
                "display", "CBC panel - Blood by Automated count"
        )));
        observation.put("code", code);
        observation.put("effectiveDateTime", gerarDataAleatoria());

        // 🎯 NOVA LÓGICA: 30% chance de gerar caso com risco HIV
        boolean gerarComRisco = random.nextDouble() < 0.30;
        
        List<Map<String, Object>> components = new ArrayList<>();

        if (gerarComRisco) {
            // 🚨 CASO COM RISCO HIV - Valores baixos que vão ativar detecção
            System.out.println("🚨 Gerando hemograma COM RISCO HIV - ID: " + observation.get("id"));
            
            // Valores que vão ativar os critérios de risco HIV
            components.add(criarComponente("6690-2", "Leukocytes", gerarValor(2000, 3900), "/µL"));  // < 4000
            components.add(criarComponente("718-7", "Hemoglobin", gerarValor(8.0, 10.9), "g/dL"));   // < 11
            components.add(criarComponente("736-9", "Lymphocytes", gerarValor(10.0, 19.0), "%"));    // < 20%
            components.add(criarComponente("731-0", "Lymphocytes (absolute)", gerarValor(500, 999), "/µL")); // < 1000
            
            // Outros valores podem ser normais ou ligeiramente alterados
            components.add(criarComponente("789-8", "Erythrocytes", gerarValor(3.5, 4.1), "milhões/mm³"));
            components.add(criarComponente("4544-3", "Hematocrit", gerarValor(30.0, 36.0), "%"));
            components.add(criarComponente("777-3", "Platelets", gerarValor(100000, 180000), "/µL"));
            
        } else {
            // ✅ CASO NORMAL - Valores dentro da normalidade
            components.add(criarComponente("6690-2", "Leukocytes", gerarValor(4000, 11000), "/µL"));
            components.add(criarComponente("718-7", "Hemoglobin", gerarValor(12.0, 17.0), "g/dL"));
            components.add(criarComponente("736-9", "Lymphocytes", gerarValor(20.0, 45.0), "%"));
            components.add(criarComponente("731-0", "Lymphocytes (absolute)", gerarValor(1000, 4800), "/µL"));
            
            components.add(criarComponente("789-8", "Erythrocytes", gerarValor(4.2, 5.9), "milhões/mm³"));
            components.add(criarComponente("4544-3", "Hematocrit", gerarValor(37.0, 50.0), "%"));
            components.add(criarComponente("777-3", "Platelets", gerarValor(150000, 450000), "/µL"));
        }

        // 📊 COMPONENTES COMUNS (sempre gerados normais)
        
        // Índices hematimétricos
        components.add(criarComponente("787-2", "Mean Corpuscular Volume (MCV)", gerarValor(80.0, 100.0), "fL"));
        components.add(criarComponente("785-6", "Mean Corpuscular Hemoglobin (MCH)", gerarValor(27.0, 33.0), "pg"));
        components.add(criarComponente("786-4", "Mean Corpuscular Hemoglobin Concentration (MCHC)", gerarValor(32.0, 36.0), "g/dL"));
        components.add(criarComponente("788-0", "Red Cell Distribution Width (RDW-CV)", gerarValor(11.5, 14.5), "%"));
        components.add(criarComponente("21000-5", "Red Cell Distribution Width (RDW-SD)", gerarValor(35.0, 50.0), "fL"));

        // Diferencial leucocitário (%) - outros tipos
        components.add(criarComponente("770-8", "Neutrophils", gerarValor(40.0, 75.0), "%"));
        components.add(criarComponente("5905-5", "Monocytes", gerarValor(2.0, 10.0), "%"));
        components.add(criarComponente("713-8", "Eosinophils", gerarValor(1.0, 6.0), "%"));
        components.add(criarComponente("706-2", "Basophils", gerarValor(0.0, 2.0), "%"));

        // Contagem absoluta de leucócitos (células/µL) - outros tipos
        components.add(criarComponente("751-8", "Neutrophils (absolute)", gerarValor(1800, 7800), "/µL"));
        components.add(criarComponente("742-7", "Monocytes (absolute)", gerarValor(200, 800), "/µL"));
        components.add(criarComponente("711-2", "Eosinophils (absolute)", gerarValor(50, 400), "/µL"));
        components.add(criarComponente("704-7", "Basophils (absolute)", gerarValor(0, 100), "/µL"));

        // Índices plaquetários
        components.add(criarComponente("32623-1", "Mean Platelet Volume (MPV)", gerarValor(7.5, 11.5), "fL"));
        components.add(criarComponente("49498-9", "Platelet Distribution Width (PDW)", gerarValor(9.0, 17.0), "fL"));
        components.add(criarComponente("777-3", "Platelet Count", gerarValor(150000, 450000), "/µL"));

        observation.put("component", components);
        return observation;
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