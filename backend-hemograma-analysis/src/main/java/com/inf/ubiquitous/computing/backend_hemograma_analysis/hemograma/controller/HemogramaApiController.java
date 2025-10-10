package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service.HemogramaFhirParserService.HemogramaData;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hemogramas")
public class HemogramaApiController {

    @Autowired
    private HemogramaStorageService storageService;

    // GET /api/hemogramas/recentes: retorna lista com campos mínimos (id, data, leucócitos, hemoglobina, plaquetas, hematócrito).
    public record HemogramaResumoDto(
            String id,
            Date data,
            BigDecimal leucocitos,
            BigDecimal hemoglobina,
            BigDecimal plaquetas,
            BigDecimal hematocrito
    ) {

        //Construtor de conversão para facilitar o mapeamento.
        public static HemogramaResumoDto from(HemogramaData data) {
            return new HemogramaResumoDto(
                    data.getObservationId(),
                    data.getDataColeta(),
                    data.getLeucocitos(),
                    data.getHemoglobina(),
                    data.getPlaquetas(),
                    data.getHematocrito()
            );
        }
    }

    // Endpoint para retornar os hemogramas mais recentes
    @GetMapping("/recentes")
    public List<HemogramaResumoDto> getHemogramasRecentes() {
        return storageService.getRecentHemogramas().stream()
                .map(HemogramaResumoDto::from)
                .collect(Collectors.toList());
    }

    // Endpoint para retornar o detalhe de um hemograma específico

    @GetMapping("/{id}")
    public ResponseEntity<HemogramaResumoDto> getHemogramaPorId(@PathVariable String id) {
        return storageService.findById(id)
                .map(HemogramaResumoDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}