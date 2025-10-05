package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service.HemogramaFhirParserService;

@RestController
@RequestMapping("/fhir") // endpoint final: /fhir/subscription
public class FhirSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(FhirSubscriptionController.class);

    @Autowired
    private HemogramaFhirParserService hemogramaParser;

    /**
     * Endpoint que recebe as notificações de Subscription (REST-hook) do HAPI FHIR.
     * Deixamos o "consumes" amplo para aceitar o que o servidor enviar.
     */
    @PostMapping(
        path = "/subscription",
        consumes = { "application/fhir+json", MediaType.APPLICATION_JSON_VALUE, "*/*" },
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> receiveSubscriptionNotification(
            @RequestBody(required = false) String fhirPayload,
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Accept", required = false) String accept,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        logger.info("=== 📡 Notificação FHIR Recebida ===");
        logger.info("Content-Type: {}", contentType);
        logger.info("Accept: {}", accept);
        logger.info("X-Request-Id: {}", requestId);

        if (fhirPayload == null || fhirPayload.isBlank()) {
            logger.warn("⚠️ Payload vazio recebido em /fhir/subscription");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payload vazio");
        }

        logger.info("Tamanho do payload: {} caracteres", fhirPayload.length());
        logger.debug("Conteúdo bruto (até 2k chars): {}", fhirPayload.length() > 2000 ? fhirPayload.substring(0, 2000) + "..." : fhirPayload);

        try {
            List<HemogramaFhirParserService.HemogramaData> hemogramas =
                    hemogramaParser.processarNotificacaoFhir(fhirPayload);

            if (hemogramas.isEmpty()) {
                logger.warn("Nenhum hemograma encontrado no payload FHIR");
                return ResponseEntity.ok("Bundle/Observation processado, mas nenhum hemograma encontrado");
            }

            hemogramas.forEach(h -> logger.info("Hemograma processado: {}", h));
            logger.info("✅ Processamento concluído com sucesso. {} hemograma(s) processado(s)", hemogramas.size());
            return ResponseEntity.ok("Processados " + hemogramas.size() + " hemogramas com sucesso");

        } catch (Exception e) {
            logger.error("❌ Erro ao processar notificação FHIR: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Erro no processamento: " + e.getMessage());
        }
    }

    /** Ping simples para conferir se o controller está no ar. */
    @GetMapping(path = "/subscription/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    /** Endpoint de teste para verificar se o controller está funcionando. */
    @GetMapping(path = "/test", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller FHIR funcionando!");
    }
}
