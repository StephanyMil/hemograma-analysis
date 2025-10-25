package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.HemogramaDto;

@Service
public class HemogramaStorageService {

    private static final Logger logger = LoggerFactory.getLogger(HemogramaStorageService.class);

    @Value("${hemograma.buffer.max-size:10000}")
    private int maxBufferSize;

    private final Map<String, HemogramaDto> hemogramaBuffer = new ConcurrentHashMap<>();

    /**
     * Adiciona um novo hemograma ao buffer com controle de tamanho
     */
    public void addHemograma(HemogramaDto hemograma) {
        if (hemograma != null && hemograma.getObservationId() != null) {
            
            if (hemogramaBuffer.size() >= maxBufferSize) {
                removeOldestHemograma();
                logger.warn("Buffer cheio! Removendo hemograma mais antigo. Tamanho: {}", hemogramaBuffer.size());
            }
            
            hemogramaBuffer.put(hemograma.getObservationId(), hemograma);
            
            if (hemograma.isRiscoHiv()) {
                logger.info("Hemograma com risco HIV armazenado: ID={}, Motivo={}", 
                           hemograma.getObservationId(), hemograma.getMotivoRisco());
            }
            
            logger.debug("Hemograma adicionado ao buffer. Total: {}", hemogramaBuffer.size());
        }
    }

    public Optional<HemogramaDto> findById(String observationId) {
        return Optional.ofNullable(hemogramaBuffer.get(observationId));
    }

    public List<HemogramaDto> getRecentHemogramas() {
        return hemogramaBuffer.values().stream()
                .sorted(Comparator.comparing(HemogramaDto::getDataColeta,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<HemogramaDto> getAllHemogramas() {
        return hemogramaBuffer.values().stream().collect(Collectors.toList());
    }

    public void clearHemogramas() {
        int tamanhoAnterior = hemogramaBuffer.size();
        hemogramaBuffer.clear();
        logger.info("Buffer limpo. {} hemogramas removidos", tamanhoAnterior);
    }

    /**
     * Retorna hemogramas que foram identificados com possível risco de HIV
     */
    public List<HemogramaDto> getHemogramasComRiscoHiv() {
        return hemogramaBuffer.values().stream()
                .filter(h -> h.isRiscoHiv())
                .sorted(Comparator.comparing(HemogramaDto::getDataColeta,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Retorna o número total de hemogramas no buffer
     */
    public int getTotalHemogramas() {
        return hemogramaBuffer.size();
    }

    /**
     * Retorna o número de hemogramas com risco HIV
     */
    public int getTotalHemogramasComRisco() {
        return (int) hemogramaBuffer.values().stream()
                .filter(h -> h.isRiscoHiv())
                .count();
    }

    /**
     * Retorna estatísticas completas do buffer
     */
    public BufferStats getEstatisticas() {
        int total = hemogramaBuffer.size();
        int comRisco = getTotalHemogramasComRisco();
        
        return new BufferStats(total, comRisco, maxBufferSize);
    }

    /**
     * Remove hemogramas mais antigos para manter o buffer no limite
     */
    private void removeOldestHemograma() {
        hemogramaBuffer.values().stream()
                .min(Comparator.comparing(HemogramaDto::getDataColeta, 
                     Comparator.nullsFirst(Comparator.naturalOrder())))
                .ifPresent(oldest -> {
                    hemogramaBuffer.remove(oldest.getObservationId());
                    logger.debug("Removido hemograma antigo: {}", oldest.getObservationId());
                });
    }

    /**
     * Remove e retorna os últimos N hemogramas (FIFO)
     */
    public List<HemogramaDto> consumirHemogramas(int quantidade) {
        List<HemogramaDto> todos = getRecentHemogramas();
        List<HemogramaDto> consumidos = todos.stream()
                .limit(quantidade)
                .collect(Collectors.toList());
        
        consumidos.forEach(h -> hemogramaBuffer.remove(h.getObservationId()));
        
        logger.info("Consumidos {} hemogramas do buffer. Restam: {}", 
                   consumidos.size(), hemogramaBuffer.size());
        
        return consumidos;
    }

    /**
     * Busca hemogramas por critérios específicos
     */
    public List<HemogramaDto> buscarPorCriterios(boolean apenasComRisco, int limite) {
        return hemogramaBuffer.values().stream()
                .filter(h -> !apenasComRisco || h.isRiscoHiv())
                .sorted(Comparator.comparing(HemogramaDto::getDataColeta,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Classe para retornar estatísticas do buffer
     */
    public static class BufferStats {
        private final int totalHemogramas;
        private final int hemogramasComRisco;
        private final int capacidadeMaxima;
        private final double percentualRisco;
        private final double percentualOcupacao;

        public BufferStats(int totalHemogramas, int hemogramasComRisco, int capacidadeMaxima) {
            this.totalHemogramas = totalHemogramas;
            this.hemogramasComRisco = hemogramasComRisco;
            this.capacidadeMaxima = capacidadeMaxima;
            this.percentualRisco = totalHemogramas > 0 ? 
                (double) hemogramasComRisco / totalHemogramas * 100 : 0.0;
            this.percentualOcupacao = (double) totalHemogramas / capacidadeMaxima * 100;
        }

        public int getTotalHemogramas() { return totalHemogramas; }
        public int getHemogramasComRisco() { return hemogramasComRisco; }
        public int getCapacidadeMaxima() { return capacidadeMaxima; }
        public double getPercentualRisco() { return percentualRisco; }
        public double getPercentualOcupacao() { return percentualOcupacao; }

        @Override
        public String toString() {
            return String.format("BufferStats{total=%d, comRisco=%d, percentualRisco=%.2f%%, ocupacao=%.2f%%}",
                    totalHemogramas, hemogramasComRisco, percentualRisco, percentualOcupacao);
        }
    }
}