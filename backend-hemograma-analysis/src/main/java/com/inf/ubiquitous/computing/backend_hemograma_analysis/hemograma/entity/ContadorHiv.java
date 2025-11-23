package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entity para armazenar contadores agregados de casos HIV por região, idade e sexo.
 * NÃO armazena dados individuais dos pacientes, apenas estatísticas agregadas.
 */
@Entity
@Table(name = "contador_hiv", 
       indexes = {
           @Index(name = "idx_contador_data_regiao", columnList = "data, regiao"),
           @Index(name = "idx_contador_demografia", columnList = "faixaEtaria, sexo, regiao")
       })
public class ContadorHiv {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate data;
    
    @Column(nullable = false, length = 20)
    private String faixaEtaria;  // "0-17", "18-29", "30-44", "45-59", "60-74", "75+"
    
    @Column(nullable = false, length = 1)
    private String sexo;  // "M" ou "F"
    
    @Column(nullable = false, length = 50)
    private String regiao;  // "Norte", "Nordeste", "Centro-Oeste", "Sudeste", "Sul"
    
    @Column(length = 50)
    private String estado;  // SP, RJ, MG, etc. (opcional para análises mais granulares)
    
    @Column(nullable = false)
    private Integer quantidade;  // Número de casos detectados
    
    @Column(nullable = false, updatable = false)
    private java.time.Instant criadoEm;
    
    @Column(nullable = false)
    private java.time.Instant atualizadoEm;
    
    // Construtores
    public ContadorHiv() {
        this.criadoEm = java.time.Instant.now();
        this.atualizadoEm = java.time.Instant.now();
    }
    
    public ContadorHiv(LocalDate data, String faixaEtaria, String sexo, String regiao, String estado, Integer quantidade) {
        this();
        this.data = data;
        this.faixaEtaria = faixaEtaria;
        this.sexo = sexo;
        this.regiao = regiao;
        this.estado = estado;
        this.quantidade = quantidade;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    
    public String getFaixaEtaria() { return faixaEtaria; }
    public void setFaixaEtaria(String faixaEtaria) { this.faixaEtaria = faixaEtaria; }
    
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    
    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { 
        this.quantidade = quantidade;
        this.atualizadoEm = java.time.Instant.now();
    }
    
    public java.time.Instant getCriadoEm() { return criadoEm; }
    public java.time.Instant getAtualizadoEm() { return atualizadoEm; }
    
    /**
     * Incrementa o contador em 1
     */
    public void incrementar() {
        this.quantidade++;
        this.atualizadoEm = java.time.Instant.now();
    }
    
    /**
     * Incrementa o contador por uma quantidade específica
     */
    public void incrementar(int valor) {
        this.quantidade += valor;
        this.atualizadoEm = java.time.Instant.now();
    }
    
    @Override
    public String toString() {
        return "ContadorHiv{" +
                "data=" + data +
                ", faixaEtaria='" + faixaEtaria + '\'' +
                ", sexo='" + sexo + '\'' +
                ", regiao='" + regiao + '\'' +
                ", estado='" + estado + '\'' +
                ", quantidade=" + quantidade +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ContadorHiv that = (ContadorHiv) obj;
        return data.equals(that.data) &&
               faixaEtaria.equals(that.faixaEtaria) &&
               sexo.equals(that.sexo) &&
               regiao.equals(that.regiao) &&
               (estado != null ? estado.equals(that.estado) : that.estado == null);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(data, faixaEtaria, sexo, regiao, estado);
    }
}