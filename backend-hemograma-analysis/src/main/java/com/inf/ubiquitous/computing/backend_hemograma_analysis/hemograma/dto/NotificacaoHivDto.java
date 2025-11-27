package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificacaoHivDto {
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("hemogramaId")
    private String hemogramaId;
    
    @JsonProperty("pacienteId")
    private String pacienteId;
    
    @JsonProperty("regiao")
    private String regiao;
    
    @JsonProperty("estado")
    private String estado;
    
    @JsonProperty("faixaEtaria")
    private String faixaEtaria;
    
    @JsonProperty("sexo")
    private String sexo;
    
    @JsonProperty("motivoRisco")
    private String motivoRisco;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Constructors
    public NotificacaoHivDto() {
        this.timestamp = LocalDateTime.now();
    }
    
    public NotificacaoHivDto(String tipo, String hemogramaId, String pacienteId, 
                            String regiao, String estado, String faixaEtaria, 
                            String sexo, String motivoRisco) {
        this.tipo = tipo;
        this.hemogramaId = hemogramaId;
        this.pacienteId = pacienteId;
        this.regiao = regiao;
        this.estado = estado;
        this.faixaEtaria = faixaEtaria;
        this.sexo = sexo;
        this.motivoRisco = motivoRisco;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters e Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getHemogramaId() { return hemogramaId; }
    public void setHemogramaId(String hemogramaId) { this.hemogramaId = hemogramaId; }
    
    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }
    
    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getFaixaEtaria() { return faixaEtaria; }
    public void setFaixaEtaria(String faixaEtaria) { this.faixaEtaria = faixaEtaria; }
    
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    
    public String getMotivoRisco() { return motivoRisco; }
    public void setMotivoRisco(String motivoRisco) { this.motivoRisco = motivoRisco; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "NotificacaoHivDto{" +
                "tipo='" + tipo + '\'' +
                ", hemogramaId='" + hemogramaId + '\'' +
                ", regiao='" + regiao + '\'' +
                ", motivoRisco='" + motivoRisco + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}