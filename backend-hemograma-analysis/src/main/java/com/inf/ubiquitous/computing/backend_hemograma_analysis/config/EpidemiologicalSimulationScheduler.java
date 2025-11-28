package com.inf.ubiquitous.computing.backend_hemograma_analysis.config;


import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.FhirIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Scheduler para simulações epidemiológicas automáticas
 * Executa a cada 30 minutos gerando lotes de casos sintéticos
 */
@Component
public class EpidemiologicalSimulationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EpidemiologicalSimulationScheduler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private FhirIntegrationService fhirIntegrationService;

    @Value("${simulation.batch.size:50}")
    private int batchSize;

    @Value("${simulation.enabled:true}")
    private boolean simulationEnabled;

    private int totalSimulations = 0;
    private int totalCasosComRisco = 0;

    /**
     * Executa simulação a cada 30 minutos
     * Cron: segundos minutos horas dia mês dia-da-semana
     * 0 0/30 * * * * = A cada 30 minutos
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void executarSimulacaoAgendada() {
        if (!simulationEnabled) {
            logger.info("⏸️  Simulação desabilitada via configuração");
            return;
        }

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        logger.info("⏰ INICIANDO SIMULAÇÃO AGENDADA - {}", timestamp);
        logger.info("📦 Tamanho do lote: {} casos", batchSize);

        try {
            long startTime = System.currentTimeMillis();

            // Executa simulação epidemiológica
            List<Map<String, Object>> resultados = fhirIntegrationService
                    .gerarSimulacaoEpidemiologica(batchSize);

            // Calcula estatísticas
            long casosComRisco = resultados.stream()
                    .filter(r -> Boolean.TRUE.equals(r.get("riscoHiv")))
                    .count();

            long duration = System.currentTimeMillis() - startTime;

            // Atualiza contadores globais
            totalSimulations++;
            totalCasosComRisco += casosComRisco;

            // Log detalhado dos resultados
            double percentualRisco = (casosComRisco * 100.0) / batchSize;
            double mediaGlobal = (totalCasosComRisco * 100.0) / (totalSimulations * batchSize);

            logger.info("✅ SIMULAÇÃO CONCLUÍDA em {}ms", duration);
            logger.info("📊 Resultados desta execução:");
            logger.info("   • Total de casos: {}", batchSize);
            logger.info("   • Casos com risco HIV: {} ({:.1f}%)", casosComRisco, percentualRisco);
            logger.info("   • Casos normais: {}", batchSize - casosComRisco);
            logger.info("📈 Estatísticas globais:");
            logger.info("   • Total de simulações: {}", totalSimulations);
            logger.info("   • Total de casos processados: {}", totalSimulations * batchSize);
            logger.info("   • Média de casos com risco: {:.1f}%", mediaGlobal);
            logger.info("   • Próxima execução: {} minutos", 30);
            logger.info("─────────────────────────────────────────────────────");

            // Alerta se percentual muito alto
            if (percentualRisco > 30.0) {
                logger.warn("⚠️  ALERTA: Taxa de risco acima do esperado ({:.1f}%)", percentualRisco);
            }

        } catch (Exception e) {
            logger.error("❌ ERRO na simulação agendada: {}", e.getMessage(), e);
        }
    }

    /**
     * Método alternativo: executa a cada 30 minutos usando fixedRate
     * Descomente para usar este ao invés do cron
     */
    // @Scheduled(fixedRate = 1800000) // 30 minutos em milissegundos
    public void executarSimulacaoFixedRate() {
        executarSimulacaoAgendada();
    }

    /**
     * Método alternativo: executa 30 minutos após término da última execução
     * Mais seguro se as simulações demorarem muito
     */
    // @Scheduled(fixedDelay = 1800000, initialDelay = 60000)
    public void executarSimulacaoFixedDelay() {
        executarSimulacaoAgendada();
    }

    /**
     * Execução imediata ao startup (opcional)
     */
    // @Scheduled(initialDelay = 10000, fixedRate = Long.MAX_VALUE)
    public void executarSimulacaoInicial() {
        if (simulationEnabled) {
            logger.info("🚀 Executando simulação inicial ao startup...");
            executarSimulacaoAgendada();
        }
    }

    /**
     * Retorna estatísticas do scheduler
     */
    public Map<String, Object> getEstatisticas() {
        return Map.of(
                "totalSimulacoes", totalSimulations,
                "totalCasosProcessados", totalSimulations * batchSize,
                "totalCasosComRisco", totalCasosComRisco,
                "mediaRisco", (totalCasosComRisco * 100.0) / (totalSimulations * batchSize),
                "enabled", simulationEnabled,
                "batchSize", batchSize
        );
    }

    /**
     * Reseta contadores (útil para testes)
     */
    public void resetarEstatisticas() {
        totalSimulations = 0;
        totalCasosComRisco = 0;
        logger.info("🔄 Estatísticas resetadas");
    }
}
