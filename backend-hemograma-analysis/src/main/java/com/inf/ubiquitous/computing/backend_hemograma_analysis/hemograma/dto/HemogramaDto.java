package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HemogramaDto {

    private String observationId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date dataColeta;

    // Contagens principais
    private BigDecimal leucocitos;           private String unidadeLeucocitos;
    private BigDecimal eritrocitos;          private String unidadeEritrocitos;
    private BigDecimal hemoglobina;          private String unidadeHemoglobina;
    private BigDecimal hematocrito;          private String unidadeHematocrito;
    private BigDecimal plaquetas;            private String unidadePlaquetas;

    // Índices hematimétricos
    private BigDecimal mcv;                  private String unidadeMcv;
    private BigDecimal mch;                  private String unidadeMch;
    private BigDecimal mchc;                 private String unidadeMchc;
    private BigDecimal rdwCv;                private String unidadeRdwCv;
    private BigDecimal rdwSd;                private String unidadeRdwSd;

    // Diferencial leucocitário (%)
    private BigDecimal neutrofilos;          private String unidadeNeutrofilos;
    private BigDecimal linfocitos;           private String unidadeLinfocitos;
    private BigDecimal monocitos;            private String unidadeMonocitos;
    private BigDecimal eosinofilos;          private String unidadeEosinofilos;
    private BigDecimal basofilos;            private String unidadeBasofilos;

    // Contagem absoluta (/μL)
    private BigDecimal neutrofilosAbs;       private String unidadeNeutrofilosAbs;
    private BigDecimal linfocitosAbs;        private String unidadeLinfocitosAbs;
    private BigDecimal monocitosAbs;         private String unidadeMonocitosAbs;
    private BigDecimal eosinofilosAbs;       private String unidadeEosinofilosAbs;
    private BigDecimal basofilosAbs;         private String unidadeBasofilosAbs;

    // Índices plaquetários
    private BigDecimal mpv;                  private String unidadeMpv;
    private BigDecimal pdw;                  private String unidadePdw;
    private BigDecimal plateletCount;        private String unidadePlateletCount;

    // Avaliação de risco
    private boolean riscoHiv;
    private String motivoRisco;

    // ================= Getters e Setters =================
    public String getObservationId() { return observationId; }
    public void setObservationId(String observationId) { this.observationId = observationId; }

    public Date getDataColeta() { return dataColeta; }
    public void setDataColeta(Date dataColeta) { this.dataColeta = dataColeta; }

    public BigDecimal getLeucocitos() { return leucocitos; }
    public void setLeucocitos(BigDecimal leucocitos) { this.leucocitos = leucocitos; }
    public String getUnidadeLeucocitos() { return unidadeLeucocitos; }
    public void setUnidadeLeucocitos(String unidadeLeucocitos) { this.unidadeLeucocitos = unidadeLeucocitos; }

    public BigDecimal getEritrocitos() { return eritrocitos; }
    public void setEritrocitos(BigDecimal eritrocitos) { this.eritrocitos = eritrocitos; }
    public String getUnidadeEritrocitos() { return unidadeEritrocitos; }
    public void setUnidadeEritrocitos(String unidadeEritrocitos) { this.unidadeEritrocitos = unidadeEritrocitos; }

    public BigDecimal getHemoglobina() { return hemoglobina; }
    public void setHemoglobina(BigDecimal hemoglobina) { this.hemoglobina = hemoglobina; }
    public String getUnidadeHemoglobina() { return unidadeHemoglobina; }
    public void setUnidadeHemoglobina(String unidadeHemoglobina) { this.unidadeHemoglobina = unidadeHemoglobina; }

    public BigDecimal getHematocrito() { return hematocrito; }
    public void setHematocrito(BigDecimal hematocrito) { this.hematocrito = hematocrito; }
    public String getUnidadeHematocrito() { return unidadeHematocrito; }
    public void setUnidadeHematocrito(String unidadeHematocrito) { this.unidadeHematocrito = unidadeHematocrito; }

    public BigDecimal getPlaquetas() { return plaquetas; }
    public void setPlaquetas(BigDecimal plaquetas) { this.plaquetas = plaquetas; }
    public String getUnidadePlaquetas() { return unidadePlaquetas; }
    public void setUnidadePlaquetas(String unidadePlaquetas) { this.unidadePlaquetas = unidadePlaquetas; }

    public BigDecimal getMcv() { return mcv; }
    public void setMcv(BigDecimal mcv) { this.mcv = mcv; }
    public String getUnidadeMcv() { return unidadeMcv; }
    public void setUnidadeMcv(String unidadeMcv) { this.unidadeMcv = unidadeMcv; }

    public BigDecimal getMch() { return mch; }
    public void setMch(BigDecimal mch) { this.mch = mch; }
    public String getUnidadeMch() { return unidadeMch; }
    public void setUnidadeMch(String unidadeMch) { this.unidadeMch = unidadeMch; }

    public BigDecimal getMchc() { return mchc; }
    public void setMchc(BigDecimal mchc) { this.mchc = mchc; }
    public String getUnidadeMchc() { return unidadeMchc; }
    public void setUnidadeMchc(String unidadeMchc) { this.unidadeMchc = unidadeMchc; }

    public BigDecimal getRdwCv() { return rdwCv; }
    public void setRdwCv(BigDecimal rdwCv) { this.rdwCv = rdwCv; }
    public String getUnidadeRdwCv() { return unidadeRdwCv; }
    public void setUnidadeRdwCv(String unidadeRdwCv) { this.unidadeRdwCv = unidadeRdwCv; }

    public BigDecimal getRdwSd() { return rdwSd; }
    public void setRdwSd(BigDecimal rdwSd) { this.rdwSd = rdwSd; }
    public String getUnidadeRdwSd() { return unidadeRdwSd; }
    public void setUnidadeRdwSd(String unidadeRdwSd) { this.unidadeRdwSd = unidadeRdwSd; }

    public BigDecimal getNeutrofilos() { return neutrofilos; }
    public void setNeutrofilos(BigDecimal neutrofilos) { this.neutrofilos = neutrofilos; }
    public String getUnidadeNeutrofilos() { return unidadeNeutrofilos; }
    public void setUnidadeNeutrofilos(String unidadeNeutrofilos) { this.unidadeNeutrofilos = unidadeNeutrofilos; }

    public BigDecimal getLinfocitos() { return linfocitos; }
    public void setLinfocitos(BigDecimal linfocitos) { this.linfocitos = linfocitos; }
    public String getUnidadeLinfocitos() { return unidadeLinfocitos; }
    public void setUnidadeLinfocitos(String unidadeLinfocitos) { this.unidadeLinfocitos = unidadeLinfocitos; }

    public BigDecimal getMonocitos() { return monocitos; }
    public void setMonocitos(BigDecimal monocitos) { this.monocitos = monocitos; }
    public String getUnidadeMonocitos() { return unidadeMonocitos; }
    public void setUnidadeMonocitos(String unidadeMonocitos) { this.unidadeMonocitos = unidadeMonocitos; }

    public BigDecimal getEosinofilos() { return eosinofilos; }
    public void setEosinofilos(BigDecimal eosinofilos) { this.eosinofilos = eosinofilos; }
    public String getUnidadeEosinofilos() { return unidadeEosinofilos; }
    public void setUnidadeEosinofilos(String unidadeEosinofilos) { this.unidadeEosinofilos = unidadeEosinofilos; }

    public BigDecimal getBasofilos() { return basofilos; }
    public void setBasofilos(BigDecimal basofilos) { this.basofilos = basofilos; }
    public String getUnidadeBasofilos() { return unidadeBasofilos; }
    public void setUnidadeBasofilos(String unidadeBasofilos) { this.unidadeBasofilos = unidadeBasofilos; }

    public BigDecimal getNeutrofilosAbs() { return neutrofilosAbs; }
    public void setNeutrofilosAbs(BigDecimal neutrofilosAbs) { this.neutrofilosAbs = neutrofilosAbs; }
    public String getUnidadeNeutrofilosAbs() { return unidadeNeutrofilosAbs; }
    public void setUnidadeNeutrofilosAbs(String unidadeNeutrofilosAbs) { this.unidadeNeutrofilosAbs = unidadeNeutrofilosAbs; }

    public BigDecimal getLinfocitosAbs() { return linfocitosAbs; }
    public void setLinfocitosAbs(BigDecimal linfocitosAbs) { this.linfocitosAbs = linfocitosAbs; }
    public String getUnidadeLinfocitosAbs() { return unidadeLinfocitosAbs; }
    public void setUnidadeLinfocitosAbs(String unidadeLinfocitosAbs) { this.unidadeLinfocitosAbs = unidadeLinfocitosAbs; }

    public BigDecimal getMonocitosAbs() { return monocitosAbs; }
    public void setMonocitosAbs(BigDecimal monocitosAbs) { this.monocitosAbs = monocitosAbs; }
    public String getUnidadeMonocitosAbs() { return unidadeMonocitosAbs; }
    public void setUnidadeMonocitosAbs(String unidadeMonocitosAbs) { this.unidadeMonocitosAbs = unidadeMonocitosAbs; }

    public BigDecimal getEosinofilosAbs() { return eosinofilosAbs; }
    public void setEosinofilosAbs(BigDecimal eosinofilosAbs) { this.eosinofilosAbs = eosinofilosAbs; }
    public String getUnidadeEosinofilosAbs() { return unidadeEosinofilosAbs; }
    public void setUnidadeEosinofilosAbs(String unidadeEosinofilosAbs) { this.unidadeEosinofilosAbs = unidadeEosinofilosAbs; }

    public BigDecimal getBasofilosAbs() { return basofilosAbs; }
    public void setBasofilosAbs(BigDecimal basofilosAbs) { this.basofilosAbs = basofilosAbs; }
    public String getUnidadeBasofilosAbs() { return unidadeBasofilosAbs; }
    public void setUnidadeBasofilosAbs(String unidadeBasofilosAbs) { this.unidadeBasofilosAbs = unidadeBasofilosAbs; }

    public BigDecimal getMpv() { return mpv; }
    public void setMpv(BigDecimal mpv) { this.mpv = mpv; }
    public String getUnidadeMpv() { return unidadeMpv; }
    public void setUnidadeMpv(String unidadeMpv) { this.unidadeMpv = unidadeMpv; }

    public BigDecimal getPdw() { return pdw; }
    public void setPdw(BigDecimal pdw) { this.pdw = pdw; }
    public String getUnidadePdw() { return unidadePdw; }
    public void setUnidadePdw(String unidadePdw) { this.unidadePdw = unidadePdw; }

    public BigDecimal getPlateletCount() { return plateletCount; }
    public void setPlateletCount(BigDecimal plateletCount) { this.plateletCount = plateletCount; }
    public String getUnidadePlateletCount() { return unidadePlateletCount; }
    public void setUnidadePlateletCount(String unidadePlateletCount) { this.unidadePlateletCount = unidadePlateletCount; }

    public boolean isRiscoHiv() { return riscoHiv; }
    public void setRiscoHiv(boolean riscoHiv) { this.riscoHiv = riscoHiv; }

    public String getMotivoRisco() { return motivoRisco; }
    public void setMotivoRisco(String motivoRisco) { this.motivoRisco = motivoRisco; }

    // ================= Métodos auxiliares =================

    /**
     * Verifica se o hemograma tem dados básicos suficientes
     */
    public boolean isDadosBasicosCompletos() {
        return observationId != null && 
               (leucocitos != null || hemoglobina != null || plaquetas != null);
    }

    /**
     * Retorna resumo clínico dos valores principais
     */
    public String getResumoClinico() {
        StringBuilder sb = new StringBuilder();
        
        if (leucocitos != null) sb.append("Leucócitos: ").append(leucocitos).append(" ");
        if (hemoglobina != null) sb.append("Hemoglobina: ").append(hemoglobina).append(" ");
        if (plaquetas != null) sb.append("Plaquetas: ").append(plaquetas).append(" ");
        
        if (riscoHiv) {
            sb.append("⚠️ RISCO HIV: ").append(motivoRisco);
        }
        
        return sb.toString().trim();
    }

    /**
     * Verifica se algum valor está fora dos ranges normais (simplificado)
     */
    public boolean hasValoresAlterados() {
        if (leucocitos != null && (leucocitos.doubleValue() < 4000 || leucocitos.doubleValue() > 11000)) return true;
        if (hemoglobina != null && (hemoglobina.doubleValue() < 12.0 || hemoglobina.doubleValue() > 17.0)) return true;
        if (plaquetas != null && (plaquetas.doubleValue() < 150000 || plaquetas.doubleValue() > 450000)) return true;
        
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HemogramaDto that = (HemogramaDto) o;
        return Objects.equals(observationId, that.observationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(observationId);
    }

    @Override
    public String toString() {
        return String.format("HemogramaDto{id='%s', dataColeta=%s, leucocitos=%s, hemoglobina=%s, riscoHiv=%s}",
                observationId, dataColeta, leucocitos, hemoglobina, riscoHiv);
    }
}