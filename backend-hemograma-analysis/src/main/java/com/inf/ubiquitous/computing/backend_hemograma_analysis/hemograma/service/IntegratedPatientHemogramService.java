package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.PacienteDto;

/**
 * Serviço que integra a geração de pacientes com hemogramas.
 */
@Service
public class IntegratedPatientHemogramService {
    
    @Autowired
    private SyntheticPatientGeneratorService patientGenerator;
    
    @Autowired
    private SyntheticHemogramGeneratorService hemogramGenerator;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Gera um paciente com seu hemograma associado.
     */
    public Map<String, Object> gerarPacienteComHemogramaSingle() {
        try {
            // Gera paciente
            PacienteDto paciente = patientGenerator.gerarPacienteSintetico();
            
            // Gera hemograma
            String hemogramaJson = hemogramGenerator.gerarHemogramasSinteticos(1);
            
            // Monta resultado
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("paciente", paciente);
            
            // Extrai primeiro hemograma do bundle
            JsonNode hemogramasBundle = objectMapper.readTree(hemogramaJson);
            JsonNode hemogramEntries = hemogramasBundle.get("entry");
            if (hemogramEntries != null && hemogramEntries.isArray() && hemogramEntries.size() > 0) {
                JsonNode primeiroHemograma = hemogramEntries.get(0).get("resource");
                resultado.put("hemograma", objectMapper.convertValue(primeiroHemograma, Map.class));
            }
            
            return resultado;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar paciente com hemograma: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gera lote de pacientes com hemogramas para processamento automático.
     */
    public List<Map<String, Object>> gerarLoteParaProcessamento(int tamanhoLote) {
        List<Map<String, Object>> lote = new ArrayList<>();
        
        for (int i = 0; i < tamanhoLote; i++) {
            Map<String, Object> item = gerarPacienteComHemogramaSingle();
            
            // Adiciona metadados para simulação de laboratório
            item.put("timestampColeta", java.time.Instant.now().toString());
            item.put("laboratorioOrigem", "LAB-" + (1000 + (int)(Math.random() * 9000)));
            
            lote.add(item);
        }
        
        return lote;
    }
    
    /**
     * Extrai dados demográficos para análise estatística.
     */
    public Map<String, String> extrairDadosDemograficos(Map<String, Object> pacienteComHemograma) {
        Map<String, String> demograficos = new HashMap<>();
        
        if (pacienteComHemograma.containsKey("paciente")) {
            PacienteDto paciente = (PacienteDto) pacienteComHemograma.get("paciente");
            demograficos.put("idade", String.valueOf(paciente.getIdade()));
            demograficos.put("sexo", paciente.getSexo());
            demograficos.put("regiao", paciente.getRegiao());
            demograficos.put("estado", paciente.getEstado());
            demograficos.put("faixaEtaria", calcularFaixaEtaria(paciente.getIdade()));
        }
        
        return demograficos;
    }
    
    /**
     * Calcula faixa etária para agrupamento estatístico.
     */
    private String calcularFaixaEtaria(Integer idade) {
        if (idade < 18) return "0-17";
        if (idade < 30) return "18-29";
        if (idade < 45) return "30-44";
        if (idade < 60) return "45-59";
        if (idade < 75) return "60-74";
        return "75+";
    }
}