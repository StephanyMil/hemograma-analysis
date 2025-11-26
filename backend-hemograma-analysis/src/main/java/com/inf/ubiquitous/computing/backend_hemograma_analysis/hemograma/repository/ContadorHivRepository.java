package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.ContadorHiv;

/**
 * Repository para operações com contadores HIV agregados
 */
@Repository
public interface ContadorHivRepository extends JpaRepository<ContadorHiv, Long> {
    
    /**
     * Busca contador existente para incrementar (evita duplicatas)
     */
    Optional<ContadorHiv> findByDataAndFaixaEtariaAndSexoAndRegiaoAndEstado(
            LocalDate data, String faixaEtaria, String sexo, String regiao, String estado);
    
    /**
     * Estatísticas por região
     */
    @Query("SELECT c.regiao, SUM(c.quantidade) FROM ContadorHiv c GROUP BY c.regiao ORDER BY SUM(c.quantidade) DESC")
    List<Object[]> contarPorRegiao();
    
    /**
     * Estatísticas por faixa etária
     */
    @Query("SELECT c.faixaEtaria, SUM(c.quantidade) FROM ContadorHiv c GROUP BY c.faixaEtaria ORDER BY c.faixaEtaria")
    List<Object[]> contarPorFaixaEtaria();
    
    /**
     * Estatísticas por sexo
     */
    @Query("SELECT c.sexo, SUM(c.quantidade) FROM ContadorHiv c GROUP BY c.sexo")
    List<Object[]> contarPorSexo();
    
    /**
     * Total de casos
     */
    @Query("SELECT SUM(c.quantidade) FROM ContadorHiv c")
    Long contarTotal();
    
    /**
     * Casos por período
     */
    @Query("SELECT SUM(c.quantidade) FROM ContadorHiv c WHERE c.data BETWEEN :dataInicio AND :dataFim")
    Long contarPorPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);
    
    /**
     * Casos recentes (últimos N dias)
     */
    @Query("SELECT c.data, SUM(c.quantidade) FROM ContadorHiv c " +
           "WHERE c.data >= :dataLimite " +
           "GROUP BY c.data ORDER BY c.data DESC")
    List<Object[]> casosRecentes(@Param("dataLimite") LocalDate dataLimite);
}