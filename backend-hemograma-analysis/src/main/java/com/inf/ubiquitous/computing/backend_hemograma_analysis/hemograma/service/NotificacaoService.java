package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.NotificacaoHivDto;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.PacienteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    
    private static final String TOPIC_HIV_DETECTADO = "hiv-detectado";
    private static final String TOPIC_ESTATISTICAS = "estatisticas-hiv";
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Envia notificação quando um caso de risco HIV é detectado
     */
    public void enviarNotificacaoHivDetectado(String hemogramaId, PacienteDto paciente, String motivoRisco) {
        try {
            NotificacaoHivDto notificacao = new NotificacaoHivDto(
                "NOVO_CASO_HIV",
                hemogramaId,
                paciente.getId(),
                paciente.getRegiao(),
                paciente.getEstado(),
                determinarFaixaEtaria(paciente.getIdade()),
                paciente.getSexo(),
                motivoRisco
            );
            
            // Usa região como key para particionar messages por região
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(TOPIC_HIV_DETECTADO, paciente.getRegiao(), notificacao);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.warn("🚨 NOTIFICAÇÃO HIV KAFKA ENVIADA - Região: {} | Motivo: {} | Offset: {}", 
                               paciente.getRegiao(), motivoRisco, result.getRecordMetadata().offset());
                } else {
                    logger.error("❌ ERRO ao enviar notificação HIV para Kafka: {}", ex.getMessage());
                }
            });
            
        } catch (Exception e) {
            logger.error("❌ ERRO ao criar notificação HIV: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Envia notificação de estatísticas atualizadas
     */
    public void enviarNotificacaoEstatisticas(String regiao, long totalCasos) {
        try {
            NotificacaoHivDto notificacao = new NotificacaoHivDto();
            notificacao.setTipo("ESTATISTICAS_ATUALIZADAS");
            notificacao.setRegiao(regiao);
            notificacao.setMotivoRisco("Total casos: " + totalCasos);
            
            kafkaTemplate.send(TOPIC_ESTATISTICAS, regiao, notificacao);
            
            logger.info("📊 Notificação de estatísticas enviada para Kafka - Região: {} | Total: {}", 
                       regiao, totalCasos);
            
        } catch (Exception e) {
            logger.error("❌ ERRO ao enviar notificação de estatísticas: {}", e.getMessage());
        }
    }
    
    /**
     * Envia notificação de lote processado (para automação)
     */
    public void enviarNotificacaoLoteProcessado(int totalProcessados, int casosHivDetectados) {
        try {
            NotificacaoHivDto notificacao = new NotificacaoHivDto();
            notificacao.setTipo("LOTE_PROCESSADO");
            notificacao.setMotivoRisco(String.format("Processados: %d | HIV detectados: %d", 
                                                   totalProcessados, casosHivDetectados));
            
            kafkaTemplate.send("processamento-automatico", "sistema", notificacao);
            
            logger.info("⚙️ Notificação de lote processado enviada - Processados: {} | HIV: {}", 
                       totalProcessados, casosHivDetectados);
            
        } catch (Exception e) {
            logger.error("❌ ERRO ao enviar notificação de lote: {}", e.getMessage());
        }
    }
    
    private String determinarFaixaEtaria(int idade) {
        if (idade < 18) return "0-17";
        if (idade < 30) return "18-29";
        if (idade < 45) return "30-44";
        if (idade < 60) return "45-59";
        if (idade < 75) return "60-74";
        return "75+";
    }
}