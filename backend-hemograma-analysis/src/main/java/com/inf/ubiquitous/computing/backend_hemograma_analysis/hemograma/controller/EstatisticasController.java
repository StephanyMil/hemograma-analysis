package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.ContadorHivService;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.FhirIntegrationService;

/**
 * Controller para APIs de estatísticas epidemiológicas
 */
@RestController
@RequestMapping("/api/estatisticas")
@CrossOrigin(origins = "*")
public class EstatisticasController {
    
    @Autowired
    private ContadorHivService contadorService;
    
    @Autowired
    private FhirIntegrationService fhirIntegrationService;
    
    /**
     * GET /api/estatisticas/por-regiao - contadores por região
     */
    @GetMapping("/por-regiao")
    public ResponseEntity<Map<String, Object>> obterEstatisticasPorRegiao() {
        Map<String, Object> estatisticas = contadorService.obterEstatisticasPorRegiao();
        return ResponseEntity.ok(estatisticas);
    }
    
    /**
     * GET /api/estatisticas/por-idade - contadores por faixa etária
     */
    @GetMapping("/por-idade")
    public ResponseEntity<Map<String, Object>> obterEstatisticasPorIdade() {
        Map<String, Object> estatisticas = contadorService.obterEstatisticasPorIdade();
        return ResponseEntity.ok(estatisticas);
    }
    
    /**
     * GET /api/estatisticas/por-sexo - contadores por sexo
     */
    @GetMapping("/por-sexo")
    public ResponseEntity<Map<String, Object>> obterEstatisticasPorSexo() {
        Map<String, Object> estatisticas = contadorService.obterEstatisticasPorSexo();
        return ResponseEntity.ok(estatisticas);
    }
    
    /**
     * GET /api/estatisticas/resumo - total casos + tendência
     */
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> obterResumoEstatisticas() {
        Map<String, Object> resumo = contadorService.obterResumoEstatisticas();
        return ResponseEntity.ok(resumo);
    }
    
    /**
     * GET /api/estatisticas/dashboard - dados completos para dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> obterDadosDashboard() {
        Map<String, Object> dashboard = Map.of(
            "resumo", contadorService.obterResumoEstatisticas(),
            "porRegiao", contadorService.obterEstatisticasPorRegiao(),
            "porIdade", contadorService.obterEstatisticasPorIdade(),
            "porSexo", contadorService.obterEstatisticasPorSexo(),
            "ultimaAtualizacao", java.time.Instant.now().toString()
        );
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * POST /api/estatisticas/simular - gera casos para demonstração
     */
    @PostMapping("/simular")
    public ResponseEntity<Map<String, Object>> simularCasosEpidemiologicos(
            @RequestParam(defaultValue = "10") int quantidade) {
        
        if (quantidade > 50) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Máximo 50 casos por simulação", "status", "erro"));
        }
        
        List<Map<String, Object>> resultados = 
            fhirIntegrationService.gerarSimulacaoEpidemiologica(quantidade);
        
        // Calcula resumo da simulação
        long casosComHiv = resultados.stream()
            .mapToLong(r -> (Boolean) r.getOrDefault("riscoHiv", false) ? 1 : 0)
            .sum();
        
        double percentualRisco = quantidade > 0 ? 
            Math.round((casosComHiv * 100.0 / quantidade) * 100.0) / 100.0 : 0.0;
        
        Map<String, Object> resumoSimulacao = Map.of(
            "status", "sucesso",
            "totalSimulado", quantidade,
            "casosComRiscoHiv", casosComHiv,
            "casosNormais", quantidade - casosComHiv,
            "percentualRisco", percentualRisco,
            "timestamp", java.time.Instant.now().toString(),
            "msg", "Simulação epidemiológica concluída com sucesso"
        );
        
        return ResponseEntity.ok(resumoSimulacao);
    }
    
    /**
     * GET /api/estatisticas/status - status do sistema
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> obterStatusSistema() {
        Map<String, Object> status = Map.of(
            "sistema", "operacional",
            "contadores", "ativo", 
            "timestamp", java.time.Instant.now().toString(),
            "endpoints", Map.of(
                "dashboard", "/api/estatisticas/dashboard",
                "resumo", "/api/estatisticas/resumo"
            )
        );
        
        return ResponseEntity.ok(status);
    }
}