package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Query dinâmica com todos os filtros opcionais
     */
    @Query("SELECT c FROM ContadorHiv c WHERE " +
            "(:dataInicio IS NULL OR c.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR c.data <= :dataFim) AND " +
            "(:regiao IS NULL OR c.regiao = :regiao) AND " +
            "(:estado IS NULL OR c.estado = :estado) AND " +
            "(:faixaEtaria IS NULL OR c.faixaEtaria = :faixaEtaria) AND " +
            "(:sexo IS NULL OR c.sexo = :sexo)")
    Page<ContadorHiv> buscarComFiltros(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("regiao") String regiao,
            @Param("estado") String estado,
            @Param("faixaEtaria") String faixaEtaria,
            @Param("sexo") String sexo,
            Pageable pageable
    );

    /**
     * Soma total de casos com filtros
     */
    @Query("SELECT COALESCE(SUM(c.quantidade), 0) FROM ContadorHiv c WHERE " +
            "(:dataInicio IS NULL OR c.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR c.data <= :dataFim) AND " +
            "(:regiao IS NULL OR c.regiao = :regiao) AND " +
            "(:estado IS NULL OR c.estado = :estado) AND " +
            "(:faixaEtaria IS NULL OR c.faixaEtaria = :faixaEtaria) AND " +
            "(:sexo IS NULL OR c.sexo = :sexo)")
    Long somarCasosComFiltros(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("regiao") String regiao,
            @Param("estado") String estado,
            @Param("faixaEtaria") String faixaEtaria,
            @Param("sexo") String sexo
    );

    /**
     * Agrupa por região com filtros
     */
    @Query("SELECT c.regiao, SUM(c.quantidade) FROM ContadorHiv c WHERE " +
            "(:dataInicio IS NULL OR c.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR c.data <= :dataFim) AND " +
            "(:faixaEtaria IS NULL OR c.faixaEtaria = :faixaEtaria) AND " +
            "(:sexo IS NULL OR c.sexo = :sexo) " +
            "GROUP BY c.regiao ORDER BY SUM(c.quantidade) DESC")
    List<Object[]> agruparPorRegiaoComFiltros(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("faixaEtaria") String faixaEtaria,
            @Param("sexo") String sexo
    );

    /**
     * Agrupa por faixa etária com filtros
     */
    @Query("SELECT c.faixaEtaria, SUM(c.quantidade) FROM ContadorHiv c WHERE " +
            "(:dataInicio IS NULL OR c.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR c.data <= :dataFim) AND " +
            "(:regiao IS NULL OR c.regiao = :regiao) AND " +
            "(:sexo IS NULL OR c.sexo = :sexo) " +
            "GROUP BY c.faixaEtaria ORDER BY c.faixaEtaria")
    List<Object[]> agruparPorFaixaEtariaComFiltros(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("regiao") String regiao,
            @Param("sexo") String sexo
    );

    /**
     * Agrupa por sexo com filtros
     */
    @Query("SELECT c.sexo, SUM(c.quantidade) FROM ContadorHiv c WHERE " +
            "(:dataInicio IS NULL OR c.data >= :dataInicio) AND " +
            "(:dataFim IS NULL OR c.data <= :dataFim) AND " +
            "(:regiao IS NULL OR c.regiao = :regiao) AND " +
            "(:faixaEtaria IS NULL OR c.faixaEtaria = :faixaEtaria) " +
            "GROUP BY c.sexo ORDER BY c.sexo")
    List<Object[]> agruparPorSexoComFiltros(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("regiao") String regiao,
            @Param("faixaEtaria") String faixaEtaria
    );
}