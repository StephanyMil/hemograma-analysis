package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.ContadorHiv;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.repository.ContadorHivRepository;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.PacienteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service para gerenciar contadores epidemiológicos HIV
 */
@Service
public class ContadorHivService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContadorHivService.class);
    
    @Autowired
    private ContadorHivRepository contadorRepository;
    
    @Autowired
    private NotificacaoService notificacaoService;

    /**
     * Incrementa contador para um caso HIV detectado
     */
    @Transactional
    public void incrementarContador(PacienteDto paciente) {
        incrementarContador(paciente, "Risco HIV detectado");
    }
    
    /**
     * Incrementa contador para um caso HIV detectado com motivo específico
     */
    @Transactional
    public void incrementarContador(PacienteDto paciente, String motivoRisco) {
        try {
            LocalDate hoje = LocalDate.now();
            String faixaEtaria = calcularFaixaEtaria(paciente.getIdade());
            
            // Busca contador existente para hoje
            Optional<ContadorHiv> contadorExistente = contadorRepository
                .findByDataAndFaixaEtariaAndSexoAndRegiaoAndEstado(
                    hoje, faixaEtaria, paciente.getSexo(), 
                    paciente.getRegiao(), paciente.getEstado()
                );
            
            long novoTotal = 0;
            
            if (contadorExistente.isPresent()) {
                // Incrementa contador existente
                ContadorHiv contador = contadorExistente.get();
                contador.incrementar();
                contadorRepository.save(contador);
                novoTotal = contador.getQuantidade();
                
                logger.info("Contador HIV incrementado: {} - Total: {}", 
                           contador.toString(), contador.getQuantidade());
            } else {
                // Cria novo contador
                ContadorHiv novoContador = new ContadorHiv(
                    hoje, faixaEtaria, paciente.getSexo(), 
                    paciente.getRegiao(), paciente.getEstado(), 1
                );
                contadorRepository.save(novoContador);
                novoTotal = 1;
                
                logger.info("Novo contador HIV criado: {}", novoContador.toString());
            }
            
            // 🆕 ENVIA NOTIFICAÇÃO KAFKA
            try {
                notificacaoService.enviarNotificacaoHivDetectado(
                    "COUNTER-" + System.currentTimeMillis(), 
                    paciente, 
                    motivoRisco
                );
                
                // Envia também notificação de estatísticas atualizadas
                long totalRegiao = calcularTotalCasosPorRegiao(paciente.getRegiao());
                notificacaoService.enviarNotificacaoEstatisticas(paciente.getRegiao(), totalRegiao);
                
            } catch (Exception kafkaError) {
                logger.error("Erro ao enviar notificação Kafka (contador salvo com sucesso): {}", 
                           kafkaError.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Erro ao incrementar contador HIV: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Calcula total de casos por região para notificações
     */
    private long calcularTotalCasosPorRegiao(String regiao) {
        try {
            List<Object[]> dados = contadorRepository.contarPorRegiao();
            for (Object[] linha : dados) {
                if (regiao.equals((String) linha[0])) {
                    return ((Number) linha[1]).longValue();
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao calcular total por região: {}", e.getMessage());
        }
        return 0L;
    }
    
    /**
     * Calcula faixa etária para agrupamento
     */
    private String calcularFaixaEtaria(Integer idade) {
        if (idade < 18) return "0-17";
        if (idade < 30) return "18-29";
        if (idade < 45) return "30-44";
        if (idade < 60) return "45-59";
        if (idade < 75) return "60-74";
        return "75+";
    }
    
    /**
     * Retorna estatísticas por região
     */
    public Map<String, Object> obterEstatisticasPorRegiao() {
        List<Object[]> dados = contadorRepository.contarPorRegiao();
        Map<String, Object> resultado = new HashMap<>();
        
        for (Object[] linha : dados) {
            resultado.put((String) linha[0], ((Number) linha[1]).longValue());
        }
        
        return resultado;
    }
    
    /**
     * Retorna estatísticas por faixa etária
     */
    public Map<String, Object> obterEstatisticasPorIdade() {
        List<Object[]> dados = contadorRepository.contarPorFaixaEtaria();
        Map<String, Object> resultado = new HashMap<>();
        
        for (Object[] linha : dados) {
            resultado.put((String) linha[0], ((Number) linha[1]).longValue());
        }
        
        return resultado;
    }
    
    /**
     * Retorna estatísticas por sexo
     */
    public Map<String, Object> obterEstatisticasPorSexo() {
        List<Object[]> dados = contadorRepository.contarPorSexo();
        Map<String, Object> resultado = new HashMap<>();
        
        for (Object[] linha : dados) {
            String sexo = (String) linha[0];
            String sexoDisplay = "M".equals(sexo) ? "Masculino" : "Feminino";
            resultado.put(sexoDisplay, ((Number) linha[1]).longValue());
        }
        
        return resultado;
    }
    
    /**
     * Retorna resumo geral das estatísticas
     */
    public Map<String, Object> obterResumoEstatisticas() {
        Map<String, Object> resumo = new HashMap<>();
        
        Long totalCasos = contadorRepository.contarTotal();
        resumo.put("totalCasos", totalCasos != null ? totalCasos : 0);
        
        // Casos hoje
        Long casosHoje = contadorRepository.contarPorPeriodo(LocalDate.now(), LocalDate.now());
        resumo.put("casosHoje", casosHoje != null ? casosHoje : 0);
        
        // Casos últimos 7 dias
        LocalDate seteDiasAtras = LocalDate.now().minusDays(7);
        Long casosUltimos7Dias = contadorRepository.contarPorPeriodo(seteDiasAtras, LocalDate.now());
        resumo.put("casosUltimos7Dias", casosUltimos7Dias != null ? casosUltimos7Dias : 0);
        
        // Casos últimos 30 dias
        LocalDate trintaDiasAtras = LocalDate.now().minusDays(30);
        Long casosUltimos30Dias = contadorRepository.contarPorPeriodo(trintaDiasAtras, LocalDate.now());
        resumo.put("casosUltimos30Dias", casosUltimos30Dias != null ? casosUltimos30Dias : 0);
        
        resumo.put("dataAtualizacao", LocalDate.now().toString());
        
        return resumo;
    }
    
    /**
     * Retorna tendência temporal (últimos 30 dias)
     */
    public Map<String, Object> obterTendenciaTemporal() {
        LocalDate dataLimite = LocalDate.now().minusDays(30);
        List<Object[]> dados = contadorRepository.casosRecentes(dataLimite);
        
        Map<String, Object> resultado = new HashMap<>();
        for (Object[] linha : dados) {
            LocalDate data = (LocalDate) linha[0];
            Long quantidade = ((Number) linha[1]).longValue();
            resultado.put(data.toString(), quantidade);
        }
        
        return resultado;
    }
}