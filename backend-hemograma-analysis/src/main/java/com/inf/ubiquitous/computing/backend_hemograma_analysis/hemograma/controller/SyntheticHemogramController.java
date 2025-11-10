package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.HemogramaDto;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaFhirParserService;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaStorageService;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.SyntheticHemogramGeneratorService;

@RestController
@RequestMapping("/fhir/synthetic")
public class SyntheticHemogramController {

    private static final Logger logger = LoggerFactory.getLogger(SyntheticHemogramController.class);

    @Value("${synthetic.hemograma.max-quantity:100}")
    private int maxQuantity;

    @Autowired
    private SyntheticHemogramGeneratorService hemogramGeneratorService;

    @Autowired
    private HemogramaFhirParserService fhirParser;

    @Autowired
    private HemogramaStorageService storageService;

    /**
     * Gera hemogramas sintéticos em formato FHIR (apenas JSON bruto)
     */
    @GetMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateRawFhir(
            @RequestParam(name = "quantidade", defaultValue = "5") int quantidade) {

        try {
            if (!isValidQuantity(quantidade)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Quantidade deve ser entre 1 e " + maxQuantity));
            }

            logger.info("Gerando {} hemogramas sintéticos (formato FHIR bruto)", quantidade);
            
            long startTime = System.currentTimeMillis();
            String fhirBundle = hemogramGeneratorService.gerarHemogramasSinteticos(quantidade);
            long generationTime = System.currentTimeMillis() - startTime;

            logger.info("Hemogramas sintéticos gerados em {}ms", generationTime);

            return ResponseEntity.ok()
                .header("X-Generation-Time-Ms", String.valueOf(generationTime))
                .header("X-Hemograma-Count", String.valueOf(quantidade))
                .body(fhirBundle);

        } catch (Exception e) {
            logger.error("Erro ao gerar hemogramas sintéticos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Erro na geração: " + e.getMessage()));
        }
    }

    /**
     * Gera hemogramas sintéticos, processa via parser e armazena no buffer
     */
    @PostMapping(value = "/generate-and-store", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateAndStore(
            @RequestParam(name = "quantidade", defaultValue = "5") int quantidade) {

        try {
            if (!isValidQuantity(quantidade)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Quantidade deve ser entre 1 e " + maxQuantity));
            }

            logger.info("Gerando e armazenando {} hemogramas sintéticos", quantidade);
            
            long startTime = System.currentTimeMillis();
            
            // Gerar FHIR Bundle
            String fhirBundle = hemogramGeneratorService.gerarHemogramasSinteticos(quantidade);
            
            // Processar via parser
            List<HemogramaDto> hemogramas = fhirParser.processarNotificacaoFhir(fhirBundle);
            
            // Armazenar no buffer
            int hemogramasComRisco = 0;
            for (HemogramaDto h : hemogramas) {
                storageService.addHemograma(h);
                if (h.isRiscoHiv()) {
                    hemogramasComRisco++;
                }
            }
            
            long totalTime = System.currentTimeMillis() - startTime;

            var response = new GenerationResult(
                hemogramas.size(),
                hemogramasComRisco,
                totalTime,
                storageService.getTotalHemogramas(),
                "Hemogramas sintéticos gerados e armazenados com sucesso"
            );

            logger.info("Processo completo: {} hemogramas gerados, {} com risco HIV, tempo: {}ms", 
                       hemogramas.size(), hemogramasComRisco, totalTime);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erro no processo completo de geração: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Erro no processo: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de teste rápido
     */
    @GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> testGeneration() {
        try {
            logger.info("Teste de geração rápida");
            
            String fhirBundle = hemogramGeneratorService.gerarHemogramasSinteticos(1);
            List<HemogramaDto> hemogramas = fhirParser.processarNotificacaoFhir(fhirBundle);
            
            if (hemogramas.isEmpty()) {
                return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Falha no teste: nenhum hemograma gerado"));
            }

            HemogramaDto primeiro = hemogramas.get(0);
            
            var testResult = new TestResult(
                "OK",
                primeiro.getObservationId(),
                primeiro.getLeucocitos() != null,
                primeiro.getHemoglobina() != null,
                primeiro.isRiscoHiv(),
                "Geração sintética funcionando corretamente"
            );

            return ResponseEntity.ok(testResult);

        } catch (Exception e) {
            logger.error("Erro no teste de geração: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Teste falhou: " + e.getMessage()));
        }
    }

    /**
     * Informações sobre limites e configurações
     */
    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getInfo() {
        var info = new GeneratorInfo(
            maxQuantity,
            "Gerador de hemogramas sintéticos para testes",
            storageService.getTotalHemogramas(),
            List.of(
                "GET /generate?quantidade=N - Gera FHIR Bundle bruto",
                "POST /generate-and-store?quantidade=N - Gera, processa e armazena",
                "GET /test - Teste rápido",
                "GET /info - Esta informação"
            )
        );
        
        return ResponseEntity.ok(info);
    }

    private boolean isValidQuantity(int quantidade) {
        return quantidade > 0 && quantidade <= maxQuantity;
    }

    private Object createErrorResponse(String message) {
        return new ErrorResponse("error", message);
    }

    /**
     * Resultado da geração com armazenamento
     */
    public record GenerationResult(
        int hemogramasGerados,
        int hemogramasComRisco,
        long tempoProcessamentoMs,
        int totalNoBuffer,
        String message
    ) {}

    /**
     * Resultado do teste
     */
    public record TestResult(
        String status,
        String observationId,
        boolean temLeucocitos,
        boolean temHemoglobina,
        boolean temRiscoHiv,
        String message
    ) {}

    /**
     * Informações do gerador
     */
    public record GeneratorInfo(
        int quantidadeMaxima,
        String descricao,
        int hemogramasNoBuffer,
        List<String> endpoints
    ) {}

    /**
     * Resposta de erro
     */
    public record ErrorResponse(String status, String message) {}
}