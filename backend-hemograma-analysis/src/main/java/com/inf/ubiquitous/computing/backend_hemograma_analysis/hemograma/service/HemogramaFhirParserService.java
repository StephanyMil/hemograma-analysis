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

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.HemogramaDto;

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

    public List<HemogramaDto> processarNotificacaoFhir(String fhirJson) {
        logger.info("Iniciando processamento do FHIR JSON");
        List<HemogramaData> hemogramas = new ArrayList<>();
        int totalObservations = 0;

        try {
            final IBaseResource resource = jsonParser.parseResource(fhirJson);

            if (resource instanceof Bundle bundle) {
                logger.info("Bundle FHIR com {} entradas encontrado", bundle.getEntry().size());
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    if (entry.getResource() instanceof Observation obs) {
                        totalObservations++;
                        processObservation(hemogramas, obs);
                    }
                }
            } else if (resource instanceof Observation obs) {
                totalObservations = 1;
                processObservation(hemogramas, obs);
            }

        } catch (Exception e) {
            logger.error("Erro ao processar FHIR JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Erro no processamento FHIR", e);
        }

        logger.info("Processamento concluído. {} Observations analisadas, {} CBCs encontrados", totalObservations, hemogramas.size());
        
        if (totalObservations > 0 && hemogramas.size() == 0) {
            logger.warn("ATENÇÃO: Nenhum CBC (Complete Blood Count) foi encontrado nas Observations analisadas");
        }

        List<HemogramaDto> dtos = new ArrayList<>();
        for (HemogramaData h : hemogramas) {
            HemogramaDto dto = converterParaDto(h);
            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * Converte HemogramaData completo para HemogramaDto com todos os campos mapeados
     */
    private HemogramaDto converterParaDto(HemogramaData h) {
        HemogramaDto dto = new HemogramaDto();
        
        dto.setObservationId(h.getObservationId());
        dto.setDataColeta(h.getDataColeta());
        
        dto.setLeucocitos(h.getLeucocitos());
        dto.setEritrocitos(h.getHemacias());
        dto.setHemoglobina(h.getHemoglobina());
        dto.setHematocrito(h.getHematocrito());
        dto.setPlaquetas(h.getPlaquetas());
        
        dto.setMcv(h.getMcv());
        dto.setMch(h.getMch());
        dto.setMchc(h.getMchc());
        dto.setRdwCv(h.getRdwCv());
        dto.setRdwSd(h.getRdwSd());
        
        dto.setNeutrofilos(h.getNeutrofilos());
        dto.setLinfocitos(h.getLinfocitos());
        dto.setMonocitos(h.getMonocitos());
        dto.setEosinofilos(h.getEosinofilos());
        dto.setBasofilos(h.getBasofilos());
        
        dto.setNeutrofilosAbs(h.getNeutrofilosAbs());
        dto.setLinfocitosAbs(h.getLinfocitosAbs());
        dto.setMonocitosAbs(h.getMonocitosAbs());
        dto.setEosinofilosAbs(h.getEosinofilosAbs());
        dto.setBasofilosAbs(h.getBasofilosAbs());
        
        dto.setMpv(h.getMpv());
        dto.setPdw(h.getPdw());
        
        dto.setRiscoHiv(h.isPossivelRiscoHiv());
        if (h.isPossivelRiscoHiv()) {
            dto.setMotivoRisco(gerarMotivoRisco(h));
        }
        
        return dto;
    }

    /**
     * Gera explicação médica do motivo do risco HIV baseado nos valores laboratoriais
     */
    private String gerarMotivoRisco(HemogramaData h) {
        List<String> motivos = new ArrayList<>();
        
        if (h.getLeucocitos() != null && h.getLeucocitos().doubleValue() < 4000) {
            motivos.add("Leucopenia (< 4000/μL)");
        }
        
        if (h.getLinfocitos() != null && h.getLinfocitos().doubleValue() < 20) {
            motivos.add("Linfopenia (< 20%)");
        }
        
        if (h.getHemoglobina() != null && h.getHemoglobina().doubleValue() < 11) {
            motivos.add("Anemia (< 11 g/dL)");
        }
        
        if (h.getLinfocitosAbs() != null && h.getLinfocitosAbs().doubleValue() < 1000) {
            motivos.add("Linfopenia absoluta (< 1000/μL)");
        }
        
        return motivos.isEmpty() ? "Risco indeterminado" : String.join(", ", motivos);
    }

    private void processObservation(List<HemogramaData> hemogramas, Observation observation) {
        if (isHemograma(observation)) {
            HemogramaData h = extrairDadosHemograma(observation);
            if (h != null) {
                h.setPossivelRiscoHiv(avaliarRiscoHiv(h));
                hemogramas.add(h);
                logger.info("CBC processado: {}", h);
            }
        } else {
            logger.info("Observation ignorada (não é CBC): ID={}", observation.getId());
        }
    }

    private boolean isHemograma(Observation observation) {
        return observation.getCode().getCoding().stream().anyMatch(coding ->
                "58410-2".equals(coding.getCode()) ||
                "57021-8".equals(coding.getCode()) ||
                "hemograma".equalsIgnoreCase(coding.getDisplay()) ||
                "complete blood count".equalsIgnoreCase(coding.getDisplay())
        );
    }

    private HemogramaData extrairDadosHemograma(Observation observation) {
        HemogramaData hemograma = new HemogramaData();
        hemograma.setObservationId(observation.getId());

        Date coleta = null;
        if (observation.hasEffectiveDateTimeType()) coleta = observation.getEffectiveDateTimeType().getValue();
        else if (observation.hasIssued()) coleta = observation.getIssued();
        hemograma.setDataColeta(coleta);

        for (Observation.ObservationComponentComponent component : observation.getComponent()) {
            String codigo = extrairCodigoComponent(component);
            BigDecimal valor = extrairValorComponent(component);
            String unidade = extrairUnidadeComponent(component);

            if (codigo != null && valor != null) {
                mapearComponenteHemograma(hemograma, codigo, valor, unidade);
            }
        }

        return hemograma;
    }

    private String extrairCodigoComponent(Observation.ObservationComponentComponent component) {
        return component.getCode().getCoding().stream()
                .findFirst().map(c -> c.getCode()).orElse(null);
    }

    private BigDecimal extrairValorComponent(Observation.ObservationComponentComponent component) {
        if (component.hasValueQuantity()) {
            Quantity q = component.getValueQuantity();
            return q.getValue();
        }
        return null;
    }

    private String extrairUnidadeComponent(Observation.ObservationComponentComponent component) {
        if (component.hasValueQuantity())
            return component.getValueQuantity().getUnit();
        return null;
    }

    /**
     * Mapeia códigos LOINC para campos do hemograma. Inclui códigos alternativos comuns.
     */
    private void mapearComponenteHemograma(HemogramaData h, String codigo, BigDecimal valor, String unidade) {
        switch (codigo) {
            case "6690-2", "26464-8" -> h.setLeucocitos(valor);
            case "789-8" -> h.setHemacias(valor);
            case "718-7" -> h.setHemoglobina(valor);
            case "4544-3" -> h.setHematocrito(valor);
            case "777-3" -> h.setPlaquetas(valor);

            case "787-2" -> h.setMcv(valor);
            case "785-6" -> h.setMch(valor);
            case "786-4" -> h.setMchc(valor);
            case "788-0" -> h.setRdwCv(valor);
            case "21000-5" -> h.setRdwSd(valor);

            case "770-8", "33743-4" -> h.setNeutrofilos(valor);
            case "736-9", "26474-7" -> h.setLinfocitos(valor);
            case "5905-5" -> h.setMonocitos(valor);
            case "713-8" -> h.setEosinofilos(valor);
            case "706-2" -> h.setBasofilos(valor);

            case "751-8" -> h.setNeutrofilosAbs(valor);
            case "731-0" -> h.setLinfocitosAbs(valor);
            case "742-7" -> h.setMonocitosAbs(valor);
            case "711-2" -> h.setEosinofilosAbs(valor);
            case "704-7" -> h.setBasofilosAbs(valor);

            case "32623-1" -> h.setMpv(valor);
            case "49498-9" -> h.setPdw(valor);

            default -> logger.debug("Código LOINC não mapeado: {} = {} {}", codigo, valor, unidade);
        }
    }

    /**
     * Avalia risco HIV baseado em critérios clínicos: leucopenia, linfopenia, anemia
     */
    private boolean avaliarRiscoHiv(HemogramaData h) {
        if (h.getLeucocitos() == null && h.getLinfocitos() == null && h.getHemoglobina() == null) {
            return false;
        }

        boolean leucopenia = h.getLeucocitos() != null && h.getLeucocitos().doubleValue() < 4000;
        boolean linfopenia = h.getLinfocitos() != null && h.getLinfocitos().doubleValue() < 20;
        boolean anemia = h.getHemoglobina() != null && h.getHemoglobina().doubleValue() < 11;
        boolean linfopenia_abs = h.getLinfocitosAbs() != null && h.getLinfocitosAbs().doubleValue() < 1000;

        return leucopenia || linfopenia || anemia || linfopenia_abs;
    }

    public static class HemogramaData {
        private String observationId;
        private Date dataColeta;
        private BigDecimal leucocitos, hemacias, hemoglobina, hematocrito, plaquetas;
        private BigDecimal mcv, mch, mchc, rdwCv, rdwSd;
        private BigDecimal neutrofilos, linfocitos, monocitos, eosinofilos, basofilos;
        private BigDecimal neutrofilosAbs, linfocitosAbs, monocitosAbs, eosinofilosAbs, basofilosAbs;
        private BigDecimal mpv, pdw;
        private boolean possivelRiscoHiv;

        public String getObservationId() { return observationId; }
        public void setObservationId(String observationId) { this.observationId = observationId; }
        public Date getDataColeta() { return dataColeta; }
        public void setDataColeta(Date dataColeta) { this.dataColeta = dataColeta; }

        public BigDecimal getLeucocitos() { return leucocitos; }
        public void setLeucocitos(BigDecimal leucocitos) { this.leucocitos = leucocitos; }
        public BigDecimal getHemacias() { return hemacias; }
        public void setHemacias(BigDecimal hemacias) { this.hemacias = hemacias; }
        public BigDecimal getHemoglobina() { return hemoglobina; }
        public void setHemoglobina(BigDecimal hemoglobina) { this.hemoglobina = hemoglobina; }
        public BigDecimal getHematocrito() { return hematocrito; }
        public void setHematocrito(BigDecimal hematocrito) { this.hematocrito = hematocrito; }
        public BigDecimal getPlaquetas() { return plaquetas; }
        public void setPlaquetas(BigDecimal plaquetas) { this.plaquetas = plaquetas; }

        public BigDecimal getMcv() { return mcv; }
        public void setMcv(BigDecimal mcv) { this.mcv = mcv; }
        public BigDecimal getMch() { return mch; }
        public void setMch(BigDecimal mch) { this.mch = mch; }
        public BigDecimal getMchc() { return mchc; }
        public void setMchc(BigDecimal mchc) { this.mchc = mchc; }
        public BigDecimal getRdwCv() { return rdwCv; }
        public void setRdwCv(BigDecimal rdwCv) { this.rdwCv = rdwCv; }
        public BigDecimal getRdwSd() { return rdwSd; }
        public void setRdwSd(BigDecimal rdwSd) { this.rdwSd = rdwSd; }

        public BigDecimal getNeutrofilos() { return neutrofilos; }
        public void setNeutrofilos(BigDecimal neutrofilos) { this.neutrofilos = neutrofilos; }
        public BigDecimal getLinfocitos() { return linfocitos; }
        public void setLinfocitos(BigDecimal linfocitos) { this.linfocitos = linfocitos; }
        public BigDecimal getMonocitos() { return monocitos; }
        public void setMonocitos(BigDecimal monocitos) { this.monocitos = monocitos; }
        public BigDecimal getEosinofilos() { return eosinofilos; }
        public void setEosinofilos(BigDecimal eosinofilos) { this.eosinofilos = eosinofilos; }
        public BigDecimal getBasofilos() { return basofilos; }
        public void setBasofilos(BigDecimal basofilos) { this.basofilos = basofilos; }

        public BigDecimal getNeutrofilosAbs() { return neutrofilosAbs; }
        public void setNeutrofilosAbs(BigDecimal neutrofilosAbs) { this.neutrofilosAbs = neutrofilosAbs; }
        public BigDecimal getLinfocitosAbs() { return linfocitosAbs; }
        public void setLinfocitosAbs(BigDecimal linfocitosAbs) { this.linfocitosAbs = linfocitosAbs; }
        public BigDecimal getMonocitosAbs() { return monocitosAbs; }
        public void setMonocitosAbs(BigDecimal monocitosAbs) { this.monocitosAbs = monocitosAbs; }
        public BigDecimal getEosinofilosAbs() { return eosinofilosAbs; }
        public void setEosinofilosAbs(BigDecimal eosinofilosAbs) { this.eosinofilosAbs = eosinofilosAbs; }
        public BigDecimal getBasofilosAbs() { return basofilosAbs; }
        public void setBasofilosAbs(BigDecimal basofilosAbs) { this.basofilosAbs = basofilosAbs; }

        public BigDecimal getMpv() { return mpv; }
        public void setMpv(BigDecimal mpv) { this.mpv = mpv; }
        public BigDecimal getPdw() { return pdw; }
        public void setPdw(BigDecimal pdw) { this.pdw = pdw; }

        public boolean isPossivelRiscoHiv() { return possivelRiscoHiv; }
        public void setPossivelRiscoHiv(boolean possivelRiscoHiv) { this.possivelRiscoHiv = possivelRiscoHiv; }

        @Override
        public String toString() {
            return String.format("HemogramaData{id='%s', leucocitos=%s, linfocitos=%s, hemoglobina=%s, riscoHiv=%s}",
                    observationId, leucocitos, linfocitos, hemoglobina, possivelRiscoHiv);
        }
    }
}