package com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.controller;


import com.inf.ubiquitous.computing.backend_hemograma_analysis.hapi.service.SyntheticToolHemogramGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tools")
public class ToolsController {

    private final SyntheticToolHemogramGeneratorService syntheticHemogramGeneratorService;

    public ToolsController(SyntheticToolHemogramGeneratorService syntheticHemogramGeneratorService) {
        this.syntheticHemogramGeneratorService = syntheticHemogramGeneratorService;
    }


    @PostMapping("/send-to-hapi")
    public ResponseEntity<Map<String, Object>> sendToHapi(
            @RequestParam(value = "qtde", defaultValue = "10") int quantity) {

        try {
            if (quantity <= 0 || quantity > 1000) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Quantidade deve estar entre 1 e 1000")
                );
            }

            List<String> createdIds = syntheticHemogramGeneratorService.generateAndSendToHapi(quantity);

            return ResponseEntity.ok(Map.of(
                    "message", "Observations enviadas com sucesso para o HAPI",
                    "quantidadeSolicitada", quantity,
                    "quantidadeCriada", createdIds.size(),
                    "idsCriados", createdIds
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Erro ao enviar observations para HAPI: " + e.getMessage())
            );
        }
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Tenta enviar uma observation de teste
            List<String> testResult = syntheticHemogramGeneratorService.generateAndSendToHapi(1);

            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "hapiFhirServer", "Conectado",
                    "testObservationCreated", !testResult.isEmpty()
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "DOWN",
                    "hapiFhirServer", "Desconectado",
                    "error", e.getMessage()
            ));
        }
    }
}
