package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.HemogramaDto;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaFhirParserService;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaStorageService;

@RestController
@RequestMapping("/fhir")
public class FhirSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(FhirSubscriptionController.class);

    @Value("${fhir.payload.max-size:1048576}")
    private int maxPayloadSize;

    @Autowired
    private HemogramaFhirParserService hemogramaParser;

    @Autowired
    private HemogramaStorageService hemogramaStorageService;

    private final AtomicLong requestCounter = new AtomicLong(0);
    private final AtomicLong successCounter = new AtomicLong(0);
    private final AtomicLong errorCounter = new AtomicLong(0);

   @PostMapping(
    path = "/subscription",
    consumes = "*/*",
    produces = MediaType.APPLICATION_JSON_VALUE
)
    public ResponseEntity<?> receiveSubscriptionNotification(
            @RequestBody(required = false) String fhirPayload,
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Accept", required = false) String accept,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        long requestNumber = requestCounter.incrementAndGet();
        String traceId = requestId != null ? requestId : "REQ-" + requestNumber;
        
        try {
            MDC.put("traceId", traceId);
            long startTime = System.currentTimeMillis();

            logger.info("Notificacao FHIR recebida - Trace: {}, Content-Type: {}", traceId, contentType);
logger.info("Payload recebido - Trace: {}: {}", traceId, fhirPayload);

            if (!isValidPayload(fhirPayload)) {
                errorCounter.incrementAndGet();
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Payload invalido ou muito grande"));
            }

            List<HemogramaDto> hemogramas = processarPayload(fhirPayload);
            
            if (hemogramas.isEmpty()) {
                logger.warn("Nenhum hemograma encontrado no payload - Trace: {}", traceId);
                return ResponseEntity.ok(createSuccessResponse(0, "Nenhum hemograma encontrado"));
            }

            armazenarHemogramas(hemogramas, traceId);
            
            long processTime = System.currentTimeMillis() - startTime;
            successCounter.incrementAndGet();
            
            logger.info("Processamento concluido - Trace: {}, Hemogramas: {}, Tempo: {}ms", 
                       traceId, hemogramas.size(), processTime);

            return ResponseEntity.ok(createSuccessResponse(hemogramas.size(), 
                "Hemogramas processados com sucesso"));

        } catch (FhirParsingException e) {
            errorCounter.incrementAndGet();
            logger.error("Erro no parsing FHIR - Trace: {}, Erro: {}", traceId, e.getMessage());
            return ResponseEntity.unprocessableEntity()
                .body(createErrorResponse("Erro no formato FHIR: " + e.getMessage()));

        } catch (StorageException e) {
            errorCounter.incrementAndGet();
            logger.error("Erro no armazenamento - Trace: {}, Erro: {}", traceId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Erro no armazenamento: " + e.getMessage()));

        } catch (Exception e) {
            errorCounter.incrementAndGet();
            logger.error("Erro interno - Trace: {}, Erro: {}", traceId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Erro interno do servidor"));

        } finally {
            MDC.clear();
        }
    }

    private boolean isValidPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            logger.warn("Payload vazio recebido");
            return false;
        }

        if (payload.length() > maxPayloadSize) {
            logger.warn("Payload muito grande: {} bytes (maximo: {})", payload.length(), maxPayloadSize);
            return false;
        }

        return true;
    }

    private List<HemogramaDto> processarPayload(String fhirPayload) throws FhirParsingException {
        try {
            return hemogramaParser.processarNotificacaoFhir(fhirPayload);
        } catch (Exception e) {
            throw new FhirParsingException("Falha no processamento FHIR: " + e.getMessage(), e);
        }
    }

    /**
     * Armazena hemogramas e destaca casos com risco HIV para auditoria médica
     */
    private void armazenarHemogramas(List<HemogramaDto> hemogramas, String traceId) throws StorageException {
        try {
            int riscoCount = 0;
            for (HemogramaDto h : hemogramas) {
                hemogramaStorageService.addHemograma(h);
                if (h.isRiscoHiv()) {
                    riscoCount++;
                    logger.info("Hemograma com risco HIV detectado - Trace: {}, ID: {}", 
                               traceId, h.getObservationId());
                }
            }
            
            if (riscoCount > 0) {
                logger.warn("{} hemograma(s) com risco HIV de {} processados - Trace: {}", 
                           riscoCount, hemogramas.size(), traceId);
            }
            
        } catch (Exception e) {
            throw new StorageException("Falha no armazenamento: " + e.getMessage(), e);
        }
    }

    @GetMapping(path = "/subscription/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> health() {
        try {
            var stats = hemogramaStorageService.getEstatisticas();
            
            var healthInfo = new HealthInfo(
                "OK",
                LocalDateTime.now(),
                requestCounter.get(),
                successCounter.get(),
                errorCounter.get(),
                stats.getTotalHemogramas(),
                stats.getHemogramasComRisco()
            );
            
            return ResponseEntity.ok(healthInfo);
            
        } catch (Exception e) {
            logger.error("Erro no health check: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Health check falhou"));
        }
    }

    @GetMapping(path = "/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller FHIR funcionando");
    }

    @GetMapping(path = "/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> metrics() {
        try {
            logger.debug("Obtendo métricas do sistema");
            
            var metrics = new SystemMetrics(
                requestCounter.get(),
                successCounter.get(),
                errorCounter.get(),
                hemogramaStorageService.getTotalHemogramas(),
                hemogramaStorageService.getTotalHemogramasComRisco()
            );
            
            logger.debug("Métricas obtidas com sucesso: requests={}, hemogramas={}", 
                        metrics.totalRequests(), metrics.totalHemogramas());
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("Erro ao obter métricas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Erro ao obter métricas: " + e.getMessage()));
        }
    }

    private Object createSuccessResponse(int count, String message) {
        return new ApiResponse("success", message, count, null);
    }

    private Object createErrorResponse(String message) {
        return new ApiResponse("error", message, 0, null);
    }

    public static class FhirParsingException extends Exception {
        public FhirParsingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class StorageException extends Exception {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public record ApiResponse(String status, String message, int count, Object data) {}

    public record HealthInfo(
        String status,
        LocalDateTime timestamp,
        long totalRequests,
        long successfulRequests,
        long errorRequests,
        int hemogramasInBuffer,
        int hemogramasWithRisk
    ) {}

    public record SystemMetrics(
        long totalRequests,
        long successfulRequests,
        long errorRequests,
        int totalHemogramas,
        int hemogramasComRisco
    ) {}
}