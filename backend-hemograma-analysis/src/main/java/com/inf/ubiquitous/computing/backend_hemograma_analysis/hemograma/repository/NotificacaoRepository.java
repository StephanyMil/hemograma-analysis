package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.repository;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Busca as últimas N notificações ordenadas por data
    List<Notificacao> findTop10ByOrderByDataCriacaoDesc();

    // Busca notificações não lidas
    List<Notificacao> findByLidaFalseOrderByDataCriacaoDesc();

    // Busca por tipo
    List<Notificacao> findByTipoOrderByDataCriacaoDesc(String tipo);

    // Busca por região
    List<Notificacao> findByRegiaoOrderByDataCriacaoDesc(String regiao);

    // Conta notificações não lidas
    long countByLidaFalse();

    // Busca com paginação
    Page<Notificacao> findAllByOrderByDataCriacaoDesc(Pageable pageable);

    // Busca por período
    @Query("SELECT n FROM Notificacao n WHERE n.dataCriacao BETWEEN :inicio AND :fim ORDER BY n.dataCriacao DESC")
    List<Notificacao> findByPeriodo(LocalDateTime inicio, LocalDateTime fim);


}