package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "hemograma_id")
    private String hemogramaId;

    @Column(name = "paciente_id")
    private String pacienteId;

    private String regiao;
    private String estado;

    @Column(name = "faixa_etaria")
    private String faixaEtaria;

    private String sexo;

    @Column(name = "motivo_risco", columnDefinition = "TEXT")
    private String motivoRisco;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private Boolean lida = false;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}