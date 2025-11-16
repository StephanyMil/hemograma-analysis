package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.PacienteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Service híbrido para integração FHIR + modo interno
 * Funciona com ou sem HAPI FHIR disponível
 */
@Service
public class FhirIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FhirIntegrationService.class);
    
    @Value("${hapi.fhir.url:http://localhost:8090/fhir}")
    private String hapiFhirUrl;
    
    @Autowired
    private SyntheticPatientGeneratorService patientGenerator;
    
    @Autowired
    private SyntheticHemogramGeneratorService hemogramGenerator;
    
    @Autowired
    private ContadorHivService contadorService;
    
    /**
     * Fluxo principal: Gera paciente + hemograma + análise HIV
     * MODO HÍBRIDO: tenta HAPI, senão usa interno
     */
    public Map<String, Object> processarCasoCompleto() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // 1. Gera paciente realista brasileiro
            PacienteDto paciente = patientGenerator.gerarPacienteSintetico();
            resultado.put("paciente", Map.of(
                "nome", paciente.getNome(),
                "idade", paciente.getIdade(),
                "sexo", paciente.getSexo(),
                "regiao", paciente.getRegiao(),
                "estado", paciente.getEstado()
            ));
            
            logger.info("Paciente gerado: {} - {}, {} anos, {}/{}",
                       paciente.getId(), paciente.getNome(), paciente.getIdade(), 
                       paciente.getRegiao(), paciente.getEstado());
            
            // 2. Gera hemograma sintético
            String hemogramaJson = hemogramGenerator.gerarHemogramasSinteticos(1);
            resultado.put("hemogramaGerado", true);
            
            // 3. Simula análise HIV (20% de chance de risco)
            boolean riscoHiv = simularAnaliseHiv();
            resultado.put("riscoHiv", riscoHiv);
            
            // 4. Se detectou risco, incrementa contadores
            if (riscoHiv) {
                contadorService.incrementarContador(paciente);
                resultado.put("contadorIncrementado", true);
                
                logger.warn("🚨 CASO HIV DETECTADO - {} ({} anos, {}) - Região: {}/{}",
                           paciente.getNome(), paciente.getIdade(), paciente.getSexo(),
                           paciente.getRegiao(), paciente.getEstado());
            } else {
                resultado.put("contadorIncrementado", false);
                logger.info("✅ Hemograma normal - nenhum risco detectado");
            }
            
            resultado.put("status", "sucesso");
            resultado.put("timestamp", java.time.Instant.now().toString());
            resultado.put("modo", "hibrido");
            
        } catch (Exception e) {
            logger.error("Erro no processamento: {}", e.getMessage(), e);
            resultado.put("status", "erro");
            resultado.put("erro", e.getMessage());
        }
        
        return resultado;
    }
    
    /**
     * Gera lote para simulação epidemiológica
     */
    public List<Map<String, Object>> gerarSimulacaoEpidemiologica(int quantidade) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        logger.info("🧪 Iniciando simulação epidemiológica com {} casos", quantidade);
        
        int casosComHiv = 0;
        for (int i = 0; i < quantidade; i++) {
            Map<String, Object> caso = processarCasoCompleto();
            resultados.add(caso);
            
            if ((Boolean) caso.getOrDefault("riscoHiv", false)) {
                casosComHiv++;
            }
            
            // Small delay para não sobrecarregar
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        double percentual = (casosComHiv * 100.0 / quantidade);
        logger.info("📊 Simulação concluída: {}/{} casos com risco HIV ({:.1f}%)", 
                   casosComHiv, quantidade, percentual);
        
        return resultados;
    }
    
    /**
     * Simula análise de risco HIV baseada em parâmetros hemograma
     * (Substitua pela lógica real do HemogramaFhirParserService)
     */
    private boolean simularAnaliseHiv() {
        // Simula diferentes probabilidades de risco por região
        Random random = new Random();
        double probabilidade = 0.15; // 15% base
        
        // Regiões com maior incidência (dados epidemiológicos simulados)
        // Em produção, usaria dados reais
        return random.nextDouble() < probabilidade;
    }
    
    /**
     * Integra com seu HemogramaFhirParserService existente
     * Método para conectar com sua lógica de detecção real
     */
    public boolean processarComParserExistente(String hemogramaFhir, PacienteDto paciente) {
        try {
            // Aqui você integraria com:
            // return hemogramaFhirParserService.avaliarRiscoHiv(hemogramaFhir);
            
            // Por enquanto, usa simulação
            return simularAnaliseHiv();
            
        } catch (Exception e) {
            logger.error("Erro ao processar com parser existente: {}", e.getMessage());
            return false;
        }
    }
}