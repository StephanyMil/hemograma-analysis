package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
        this.fhirContext = FhirContext.forR4();
        this.jsonParser = fhirContext.newJsonParser();
    }

    /**
     * Processa um Bundle FHIR e extrai os dados de hemograma
     */
    public List<HemogramaData> processarNotificacaoFhir(String fhirJson) {
        logger.info("Iniciando processamento do FHIR JSON");
        
        List<HemogramaData> hemogramas = new ArrayList<>();
        
        try {
            // Parse do JSON FHIR
            Bundle bundle = jsonParser.parseResource(Bundle.class, fhirJson);
            logger.info("Bundle FHIR parseado com sucesso. Entries: {}", bundle.getEntry().size());
            
            // Processa cada entry do bundle
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Observation) {
                    Observation observation = (Observation) entry.getResource();
                    
                    // Verifica se é um hemograma baseado no código
                    if (isHemograma(observation)) {
                        HemogramaData hemograma = extrairDadosHemograma(observation);
                        if (hemograma != null) {
                            hemogramas.add(hemograma);
                            logger.info("Hemograma extraído: {}", hemograma);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar FHIR JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Erro no processamento FHIR", e);
        }
        
        logger.info("Processamento concluído. {} hemogramas encontrados", hemogramas.size());
        return hemogramas;
    }

    /**
     * Verifica se a Observation é um hemograma
     */
    private boolean isHemograma(Observation observation) {
        // Verifica por códigos LOINC comuns de hemograma
        return observation.getCode().getCoding().stream()
                .anyMatch(coding -> 
                    "58410-2".equals(coding.getCode()) || // CBC panel - Blood by Automated count
                    "57021-8".equals(coding.getCode()) || // CBC W Auto Differential panel
                    "hemograma".equalsIgnoreCase(coding.getDisplay()) ||
                    "complete blood count".equalsIgnoreCase(coding.getDisplay())
                );
    }

    /**
     * Extrai os dados específicos do hemograma
     */
    private HemogramaData extrairDadosHemograma(Observation observation) {
        try {
            HemogramaData hemograma = new HemogramaData();
            
            // ID da observação
            hemograma.setObservationId(observation.getId());
            
            // Data da observação
            if (observation.hasEffectiveDateTimeType()) {
                hemograma.setDataColeta(observation.getEffectiveDateTimeType().getValue());
            }
            
            // Extrai os componentes do hemograma
            for (Observation.ObservationComponentComponent component : observation.getComponent()) {
                String codigo = extrairCodigoComponent(component);
                BigDecimal valor = extrairValorComponent(component);
                String unidade = extrairUnidadeComponent(component);
                
                if (codigo != null && valor != null) {
                    mapearComponenteHemograma(hemograma, codigo, valor, unidade);
                }
            }
            
            return hemograma;
            
        } catch (Exception e) {
            logger.error("Erro ao extrair dados do hemograma: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extrai o código do componente
     */
    private String extrairCodigoComponent(Observation.ObservationComponentComponent component) {
        return component.getCode().getCoding().stream()
                .findFirst()
                .map(coding -> coding.getCode())
                .orElse(null);
    }

    /**
     * Extrai o valor numérico do componente
     */
    private BigDecimal extrairValorComponent(Observation.ObservationComponentComponent component) {
        if (component.hasValueQuantity()) {
            Quantity quantity = component.getValueQuantity();
            return quantity.getValue();
        }
        return null;
    }

    /**
     * Extrai a unidade do componente
     */
    private String extrairUnidadeComponent(Observation.ObservationComponentComponent component) {
        if (component.hasValueQuantity()) {
            return component.getValueQuantity().getUnit();
        }
        return null;
    }

    /**
     * Mapeia os códigos LOINC para os campos do hemograma
     */
    private void mapearComponenteHemograma(HemogramaData hemograma, String codigo, BigDecimal valor, String unidade) {
        switch (codigo) {
            case "6690-2": // Leucócitos
            case "33747-0": // Leucócitos por automated count
                hemograma.setLeucocitos(valor);
                hemograma.setUnidadeLeucocitos(unidade);
                break;
                
            case "718-7": // Hemoglobina
            case "30313-1": // Hemoglobina por automated count
                hemograma.setHemoglobina(valor);
                hemograma.setUnidadeHemoglobina(unidade);
                break;
                
            case "777-3": // Plaquetas
            case "26515-7": // Plaquetas por automated count
                hemograma.setPlaquetas(valor);
                hemograma.setUnidadePlaquetas(unidade);
                break;
                
            case "4544-3": // Hematócrito
            case "31100-1": // Hematócrito por automated count
                hemograma.setHematocrito(valor);
                hemograma.setUnidadeHematocrito(unidade);
                break;
                
            default:
                logger.debug("Código não mapeado: {}", codigo);
        }
    }

    /**
     * Classe para armazenar os dados do hemograma
     */
    public static class HemogramaData {
        private String observationId;
        private java.util.Date dataColeta;
        private BigDecimal leucocitos;
        private String unidadeLeucocitos;
        private BigDecimal hemoglobina;
        private String unidadeHemoglobina;
        private BigDecimal plaquetas;
        private String unidadePlaquetas;
        private BigDecimal hematocrito;
        private String unidadeHematocrito;

        // Getters e Setters
        public String getObservationId() { return observationId; }
        public void setObservationId(String observationId) { this.observationId = observationId; }
        
        public java.util.Date getDataColeta() { return dataColeta; }
        public void setDataColeta(java.util.Date dataColeta) { this.dataColeta = dataColeta; }
        
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
            return String.format("HemogramaData{id='%s', leucocitos=%s, hemoglobina=%s, plaquetas=%s, hematocrito=%s}", 
                    observationId, leucocitos, hemoglobina, plaquetas, hematocrito);
        }
    }
}