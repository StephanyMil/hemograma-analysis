package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.HemogramaDto;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaStorageService;

/**
 * 🚨 Controller específico para hemogramas com RISCO HIV
 * Este é o controller que estava faltando no seu sistema!
 */
@RestController
@RequestMapping("/api/hemogramas")
@CrossOrigin(origins = "*")
public class HemogramaHivController {
    
    @Autowired
    private HemogramaStorageService storageService;
    
    /**
     * 🎯 ENDPOINT PRINCIPAL - Lista hemogramas com risco HIV
     * Este é o endpoint que o frontend precisa para gerar os gráficos!
     */
    @GetMapping("/com-risco-hiv")
    public ResponseEntity<Map<String, Object>> getHemogramasComRiscoHiv(
            @RequestParam(defaultValue = "50") int limite) {
        
        List<HemogramaDto> hemogramasComRisco = storageService.getHemogramasComRiscoHiv()
                .stream()
                .limit(limite)
                .collect(Collectors.toList());
        
        // Monta resposta detalhada para o frontend
        Map<String, Object> response = Map.of(
            "hemogramasComRisco", hemogramasComRisco,
            "totalEncontrados", hemogramasComRisco.size(),
            "totalNoBuffer", storageService.getTotalHemogramas(),
            "percentualRisco", calcularPercentualRisco(),
            "ultimaAtualizacao", java.time.Instant.now().toString(),
            "status", "sucesso"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 📊 Endpoint para dados do gráfico - formato específico para charts
     */
    @GetMapping("/risco-hiv/grafico")
    public ResponseEntity<Map<String, Object>> getDadosGrafico() {
        
        List<HemogramaDto> hemogramasComRisco = storageService.getHemogramasComRiscoHiv();
        
        // Agrupa por motivo de risco para o gráfico
        Map<String, Long> porMotivo = hemogramasComRisco.stream()
            .collect(Collectors.groupingBy(
                h -> h.getMotivoRisco() != null ? h.getMotivoRisco() : "Motivo não especificado",
                Collectors.counting()
            ));
        
        // Agrupa por data para timeline
        Map<String, Long> porData = hemogramasComRisco.stream()
            .collect(Collectors.groupingBy(
                h -> h.getDataColeta() != null ? 
                    h.getDataColeta().toString().substring(0, 10) : // yyyy-MM-dd
                    java.time.LocalDate.now().toString(),
                Collectors.counting()
            ));
        
        Map<String, Object> dadosGrafico = Map.of(
            "porMotivo", porMotivo,
            "porData", porData,
            "totalCasos", hemogramasComRisco.size(),
            "timestamp", java.time.Instant.now().toString()
        );
        
        return ResponseEntity.ok(dadosGrafico);
    }
    
    /**
     * 🔍 Detalhes de um hemograma específico com risco HIV
     */
    @GetMapping("/risco-hiv/{id}")
    public ResponseEntity<HemogramaDto> getDetalhesHemogramaRisco(@PathVariable String id) {
        
        return storageService.findById(id)
            .filter(h -> h.isRiscoHiv()) // Só retorna se tiver risco HIV
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 📈 Estatísticas resumidas sobre hemogramas com risco HIV
     */
    @GetMapping("/risco-hiv/estatisticas")
    public ResponseEntity<Map<String, Object>> getEstatisticasRiscoHiv() {
        
        var stats = storageService.getEstatisticas();
        List<HemogramaDto> hemogramasComRisco = storageService.getHemogramasComRiscoHiv();
        
        // Calcula estatísticas por motivo
        Map<String, Long> motivosCount = hemogramasComRisco.stream()
            .collect(Collectors.groupingBy(
                h -> h.getMotivoRisco() != null ? h.getMotivoRisco() : "Não especificado",
                Collectors.counting()
            ));
        
        Map<String, Object> estatisticas = Map.of(
            "totalHemogramas", stats.getTotalHemogramas(),
            "hemogramasComRisco", stats.getHemogramasComRisco(),
            "percentualRisco", stats.getPercentualRisco(),
            "motivosMaisComuns", motivosCount,
            "capacidadeBuffer", stats.getCapacidadeMaxima(),
            "ocupacaoBuffer", stats.getPercentualOcupacao()
        );
        
        return ResponseEntity.ok(estatisticas);
    }
    
    /**
     * 🔄 Status do sistema de detecção HIV
     */
    @GetMapping("/risco-hiv/status")
    public ResponseEntity<Map<String, Object>> getStatusSistema() {
        
        var stats = storageService.getEstatisticas();
        
        Map<String, Object> status = Map.of(
            "sistemaAtivo", true,
            "hemogramasNoBuffer", stats.getTotalHemogramas(),
            "casosHivDetectados", stats.getHemogramasComRisco(),
            "ultimaVerificacao", java.time.Instant.now().toString(),
            "endpoints", Map.of(
                "listaComRisco", "/api/hemogramas/com-risco-hiv",
                "dadosGrafico", "/api/hemogramas/risco-hiv/grafico",
                "estatisticas", "/api/hemogramas/risco-hiv/estatisticas"
            )
        );
        
        return ResponseEntity.ok(status);
    }
    
    private double calcularPercentualRisco() {
        var stats = storageService.getEstatisticas();
        return stats.getPercentualRisco();
    }
}