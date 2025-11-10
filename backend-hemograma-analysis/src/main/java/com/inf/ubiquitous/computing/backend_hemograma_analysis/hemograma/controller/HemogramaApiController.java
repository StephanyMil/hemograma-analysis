package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.HemogramaDto;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaStorageService;

@RestController
@RequestMapping("/api/hemogramas")
public class HemogramaApiController {

    @Autowired
    private HemogramaStorageService storageService;

    /**
     * DTO resumido de hemograma, usado nas listagens rápidas.
     * Inclui dados de risco HIV para destacar casos suspeitos no frontend.
     */
    public record HemogramaResumoDto(
            String id,
            Date data,
            BigDecimal leucocitos,
            BigDecimal hemoglobina,
            BigDecimal plaquetas,
            BigDecimal hematocrito,
            boolean riscoHiv,
            String motivoRisco
    ) {
        // Construtor de conversão para facilitar o mapeamento.
        public static HemogramaResumoDto from(HemogramaDto dto) {
            return new HemogramaResumoDto(
                    dto.getObservationId(),
                    dto.getDataColeta(),
                    dto.getLeucocitos(),
                    dto.getHemoglobina(),
                    dto.getPlaquetas(),
                    dto.getHematocrito(),
                    dto.isRiscoHiv(),
                    dto.getMotivoRisco()
            );
        }
    }

    /**
     * Endpoint que retorna lista resumida dos hemogramas armazenados.
     * Exemplo: GET /api/hemogramas/recentes
     */
    @GetMapping("/recentes")
    public List<HemogramaResumoDto> getHemogramasRecentes() {
        return storageService.getAllHemogramas().stream()
                .map(HemogramaResumoDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Endpoint que retorna o detalhe de um hemograma específico.
     * Exemplo: GET /api/hemogramas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HemogramaResumoDto> getHemogramaPorId(@PathVariable String id) {
        return storageService.findById(id)
                .map(HemogramaResumoDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}