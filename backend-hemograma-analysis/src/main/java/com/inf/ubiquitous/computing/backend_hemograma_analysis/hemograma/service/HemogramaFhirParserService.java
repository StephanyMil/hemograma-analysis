package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Service
public class HemogramaFhirParserService {

    private static final Logger logger = LoggerFactory.getLogger(HemogramaFhirParserService.class);

    private final FhirContext fhirContext;
    private final IParser jsonParser;

    public HemogramaFhirParserService() {
        this.fhirContext = FhirContext.forR4();           // thread-safe
        this.jsonParser  = fhirContext.newJsonParser();   // thread-safe
    }

    /**
     * Processa um payload FHIR (Bundle ou Observation) e extrai dados de hemograma.
     */
    public List<HemogramaData> processarNotificacaoFhir(String fhirJson) {
        logger.info("Iniciando processamento do FHIR JSON");
        List<HemogramaData> hemogramas = new ArrayList<>();

        try {
            final IBaseResource resource = jsonParser.parseResource(fhirJson);

            if (resource instanceof Bundle bundle) {
                logger.info("Bundle FHIR parseado com sucesso. Entries: {}", bundle.getEntry().size());

                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    if (entry.getResource() instanceof Observation obs) {
                        processObservation(hemogramas, obs);
                    }
                }
            } else if (resource instanceof Observation obs) {
                logger.info("Observation FHIR isolada recebida");
                processObservation(hemogramas, obs);
            } else {
                logger.warn("Recurso FHIR não suportado: {}", resource.getClass().getSimpleName());
            }

        } catch (Exception e) {
            logger.error("Erro ao processar FHIR JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Erro no processamento FHIR", e);
        }

        logger.info("Processamento concluído. {} hemograma(s) encontrado(s)", hemogramas.size());
        return hemogramas;
    }

    private void processObservation(List<HemogramaData> hemogramas, Observation observation) {
        if (isHemograma(observation)) {
            HemogramaData h = extrairDadosHemograma(observation);
            if (h != null) {
                hemogramas.add(h);
                logger.info("Hemograma extraído: {}", h);
            }
        } else {
            logger.debug("Observation ignorada: não é hemograma (code/display não corresponde a CBC).");
        }
    }

    /**
     * Verifica se a Observation é um hemograma (CBC).
     */
    private boolean isHemograma(Observation observation) {
        return observation.getCode().getCoding().stream().anyMatch(coding ->
                "58410-2".equals(coding.getCode()) ||   // CBC panel - Automated count
                "57021-8".equals(coding.getCode()) ||   // CBC w/ Auto Differential panel
                "hemograma".equalsIgnoreCase(coding.getDisplay()) ||
                "complete blood count".equalsIgnoreCase(coding.getDisplay())
        );
    }

    /**
     * Extrai os dados específicos do hemograma.
     */
    private HemogramaData extrairDadosHemograma(Observation observation) {
        try {
            HemogramaData hemograma = new HemogramaData();

            // ID da observação
            hemograma.setObservationId(observation.getId());

            // Data da observação
            Date coleta = null;
            if (observation.hasEffectiveDateTimeType()) {
                coleta = observation.getEffectiveDateTimeType().getValue();
            } else if (observation.hasIssued()) {
                coleta = observation.getIssued();
            }
            hemograma.setDataColeta(coleta);

            // Componentes
            for (Observation.ObservationComponentComponent component : observation.getComponent()) {
                String codigo  = extrairCodigoComponent(component);
                BigDecimal val = extrairValorComponent(component);
                String unidade = extrairUnidadeComponent(component);

                if (codigo != null && val != null) {
                    mapearComponenteHemograma(hemograma, codigo, val, unidade);
                }
            }

            return hemograma;

        } catch (Exception e) {
            logger.error("Erro ao extrair dados do hemograma: {}", e.getMessage(), e);
            return null;
        }
    }

    private String extrairCodigoComponent(Observation.ObservationComponentComponent component) {
        return component.getCode().getCoding().stream()
                .findFirst()
                .map(c -> c.getCode())
                .orElse(null);
    }

    private BigDecimal extrairValorComponent(Observation.ObservationComponentComponent component) {
        if (component.hasValueQuantity()) {
            Quantity q = component.getValueQuantity();
            return q.getValue();
        }
        return null;
    }

    private String extrairUnidadeComponent(Observation.ObservationComponentComponent component) {
        if (component.hasValueQuantity()) {
            return component.getValueQuantity().getUnit();
        }
        return null;
    }

    /**
     * Mapeia códigos LOINC → campos do hemograma.
     */
    private void mapearComponenteHemograma(HemogramaData hemograma, String codigo, BigDecimal valor, String unidade) {
        switch (codigo) {
            case "6690-2":    // Leucócitos
            case "33747-0":   // Leucócitos (auto)
                hemograma.setLeucocitos(valor);
                hemograma.setUnidadeLeucocitos(unidade);
                break;

            case "718-7":     // Hemoglobina
            case "30313-1":   // Hemoglobina (auto)
                hemograma.setHemoglobina(valor);
                hemograma.setUnidadeHemoglobina(unidade);
                break;

            case "777-3":     // Plaquetas
            case "26515-7":   // Plaquetas (auto)
                hemograma.setPlaquetas(valor);
                hemograma.setUnidadePlaquetas(unidade);
                break;

            case "4544-3":    // Hematócrito
            case "31100-1":   // Hematócrito (auto)
                hemograma.setHematocrito(valor);
                hemograma.setUnidadeHematocrito(unidade);
                break;

            default:
                logger.debug("Código não mapeado no parser: {}", codigo);
        }
    }

    /**
     * DTO simples para os dados do hemograma.
     */
    public static class HemogramaData {
        private String observationId;
        private Date   dataColeta;
        private BigDecimal leucocitos;
        private String unidadeLeucocitos;
        private BigDecimal hemoglobina;
        private String unidadeHemoglobina;
        private BigDecimal plaquetas;
        private String unidadePlaquetas;
        private BigDecimal hematocrito;
        private String unidadeHematocrito;

        public String getObservationId() { return observationId; }
        public void setObservationId(String observationId) { this.observationId = observationId; }

        public Date getDataColeta() { return dataColeta; }
        public void setDataColeta(Date dataColeta) { this.dataColeta = dataColeta; }

        public BigDecimal getLeucocitos() { return leucocitos; }
        public void setLeucocitos(BigDecimal leucocitos) { this.leucocitos = leucocitos; }

        public String getUnidadeLeucocitos() { return unidadeLeucocitos; }
        public void setUnidadeLeucocitos(String unidadeLeucocitos) { this.unidadeLeucocitos = unidadeLeucocitos; }

        public BigDecimal getHemoglobina() { return hemoglobina; }
        public void setHemoglobina(BigDecimal hemoglobina) { this.hemoglobina = hemoglobina; }

        public String getUnidadeHemoglobina() { return unidadeHemoglobina; }
        public void setUnidadeHemoglobina(String unidadeHemoglobina) { this.unidadeHemoglobina = unidadeHemoglobina; }

        public BigDecimal getPlaquetas() { return plaquetas; }
        public void setPlaquetas(BigDecimal plaquetas) { this.plaquetas = plaquetas; }

        public String getUnidadePlaquetas() { return unidadePlaquetas; }
        public void setUnidadePlaquetas(String unidadePlaquetas) { this.unidadePlaquetas = unidadePlaquetas; }

        public BigDecimal getHematocrito() { return hematocrito; }
        public void setHematocrito(BigDecimal hematocrito) { this.hematocrito = hematocrito; }

        public String getUnidadeHematocrito() { return unidadeHematocrito; }
        public void setUnidadeHematocrito(String unidadeHematocrito) { this.unidadeHematocrito = unidadeHematocrito; }

        @Override
        public String toString() {
            return String.format(
                "HemogramaData{id='%s', leucocitos=%s %s, hemoglobina=%s %s, plaquetas=%s %s, hematocrito=%s %s}",
                observationId,
                leucocitos, unidadeLeucocitos,
                hemoglobina, unidadeHemoglobina,
                plaquetas, unidadePlaquetas,
                hematocrito, unidadeHematocrito
            );
        }
    }
}
