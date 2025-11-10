package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaFhirParserService.HemogramaData;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.Date;

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

    @GetMapping("/recentes")
    public Page<HemogramaResumoDto> getHemogramasRecentes(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<HemogramaData> paginaDeDados = storageService.getRecentHemogramas(pageable);

        // Converte Page<HemogramaData> para Page<HemogramaResumoDto>
        return paginaDeDados.map(HemogramaResumoDto::from);
    }

    // Endpoint para retornar o detalhe de um hemograma específico pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<HemogramaResumoDto> getHemogramaPorId(@PathVariable String id) {
        return storageService.findById(id)
                .map(HemogramaResumoDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}