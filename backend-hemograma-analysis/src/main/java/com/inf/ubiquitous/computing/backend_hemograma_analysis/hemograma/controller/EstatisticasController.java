package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.ContadorHiv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
     * GET /api/pacientes-risco
     * Listagem filtrada e paginada de contadores HIV
     *
     * Parâmetros:
     * - dataInicio: data inicial (formato: yyyy-MM-dd)
     * - dataFim: data final (formato: yyyy-MM-dd)
     * - regiao: Norte, Nordeste, Centro-Oeste, Sudeste, Sul
     * - estado: SP, RJ, MG, etc.
     * - faixaEtaria: 0-17, 18-29, 30-44, 45-59, 60-74, 75+
     * - sexo: M ou F
     * - page: número da página (começa em 0)
     * - size: tamanho da página (padrão: 20)
     * - sort: campo de ordenação (padrão: data,desc)
     */
    @GetMapping("/lista")
    public ResponseEntity<Map<String, Object>> listarContadoresFiltrados(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String regiao,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String faixaEtaria,
            @RequestParam(required = false) String sexo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "data,desc") String[] sort) {

        try {
            // Validações
            if (size > 100) {
                return ResponseEntity.badRequest().body(Map.of(
                        "erro", "Tamanho máximo da página é 100",
                        "status", "erro"
                ));
            }

            // Criar Pageable com ordenação
            Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

            // Buscar com filtros
            Page<ContadorHiv> resultado = contadorService.buscarContadoresFiltrados(
                    dataInicio, dataFim, regiao, estado, faixaEtaria, sexo, pageable
            );

            // Montar resposta
            Map<String, Object> response = new HashMap<>();
            response.put("contadores", resultado.getContent());
            response.put("paginacao", Map.of(
                    "paginaAtual", resultado.getNumber(),
                    "totalPaginas", resultado.getTotalPages(),
                    "totalElementos", resultado.getTotalElements(),
                    "tamanhoPagina", resultado.getSize(),
                    "primeiro", resultado.isFirst(),
                    "ultimo", resultado.isLast(),
                    "vazio", resultado.isEmpty()
            ));
            response.put("filtros", Map.of(
                    "dataInicio", dataInicio != null ? dataInicio.toString() : "não aplicado",
                    "dataFim", dataFim != null ? dataFim.toString() : "não aplicado",
                    "regiao", regiao != null ? regiao : "todas",
                    "estado", estado != null ? estado : "todos",
                    "faixaEtaria", faixaEtaria != null ? faixaEtaria : "todas",
                    "sexo", sexo != null ? sexo : "todos"
            ));

            // Estatísticas dos resultados filtrados
            int totalCasos = resultado.getContent().stream()
                    .mapToInt(ContadorHiv::getQuantidade)
                    .sum();

            response.put("estatisticas", Map.of(
                    "totalCasosNaPagina", totalCasos,
                    "totalRegistros", resultado.getTotalElements()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "erro", "Erro ao processar consulta: " + e.getMessage(),
                    "status", "erro"
            ));
        }
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