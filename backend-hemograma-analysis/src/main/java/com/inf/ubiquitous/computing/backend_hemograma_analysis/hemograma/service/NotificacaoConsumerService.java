package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.NotificacaoHivDto;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.Notificacao;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoConsumerService.class);

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Consome notificações de HIV detectado e envia via WebSocket
     */
    @KafkaListener(
            topics = "hiv-detectado",
            groupId = "notificacao-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumirNotificacaoHiv(
            @Payload NotificacaoHivDto dto,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            logger.info("📥 Recebendo notificação HIV - Partition: {} | Offset: {} | Região: {}",
                    partition, offset, dto.getRegiao());

            Notificacao notificacao = converterDtoParaEntidade(dto);
            notificacao = notificacaoRepository.save(notificacao);

            logger.info("✅ Notificação HIV salva no banco - ID: {} | Tipo: {}",
                    notificacao.getId(), notificacao.getTipo());

            // 🔥 ENVIA PARA TODOS OS CLIENTES CONECTADOS VIA WEBSOCKET
            messagingTemplate.convertAndSend("/topic/notificacoes", notificacao);

            // Envia também para o tópico específico de HIV
            messagingTemplate.convertAndSend("/topic/notificacoes/hiv", notificacao);

            // Se quiser enviar para uma região específica
            if (dto.getRegiao() != null) {
                messagingTemplate.convertAndSend("/topic/notificacoes/regiao/" + dto.getRegiao(), notificacao);
            }

            logger.info("🔔 Notificação enviada via WebSocket para clientes conectados");

            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("❌ ERRO ao processar notificação HIV: {}", e.getMessage(), e);
        }
    }

    /**
     * Consome notificações de estatísticas
     */
    @KafkaListener(
            topics = "estatisticas-hiv",
            groupId = "notificacao-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumirNotificacaoEstatisticas(
            @Payload NotificacaoHivDto dto,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            logger.info("📥 Recebendo notificação de estatísticas - Partition: {} | Offset: {}",
                    partition, offset);

            Notificacao notificacao = converterDtoParaEntidade(dto);
            notificacao = notificacaoRepository.save(notificacao);

            logger.info("✅ Notificação de estatísticas salva - ID: {}", notificacao.getId());

            // Envia via WebSocket
            messagingTemplate.convertAndSend("/topic/notificacoes", notificacao);
            messagingTemplate.convertAndSend("/topic/notificacoes/estatisticas", notificacao);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("❌ ERRO ao processar notificação de estatísticas: {}", e.getMessage(), e);
        }
    }

    /**
     * Consome notificações de lote processado
     */
    @KafkaListener(
            topics = "processamento-automatico",
            groupId = "notificacao-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumirNotificacaoLote(
            @Payload NotificacaoHivDto dto,
            Acknowledgment acknowledgment) {

        try {
            logger.info("📥 Recebendo notificação de lote processado");

            Notificacao notificacao = converterDtoParaEntidade(dto);
            notificacao = notificacaoRepository.save(notificacao);

            logger.info("✅ Notificação de lote salva - ID: {}", notificacao.getId());

            // Envia via WebSocket
            messagingTemplate.convertAndSend("/topic/notificacoes", notificacao);
            messagingTemplate.convertAndSend("/topic/notificacoes/lote", notificacao);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("❌ ERRO ao processar notificação de lote: {}", e.getMessage(), e);
        }
    }

    /**
     * Buscar notificações não lidas
     */
    public List<Notificacao> buscarNotificacoesNaoLidas() {
        return notificacaoRepository.findByLidaFalseOrderByDataCriacaoDesc();
    }

    /**
     * Marcar notificação como lida
     */
    @Transactional
    public void marcarComoLida(Long notificacaoId) {
        notificacaoRepository.findById(notificacaoId).ifPresent(notificacao -> {
            notificacao.setLida(true);
            notificacaoRepository.save(notificacao);
        });
    }

    private Notificacao converterDtoParaEntidade(NotificacaoHivDto dto) {
        Notificacao notificacao = new Notificacao();
        notificacao.setTipo(dto.getTipo());
        notificacao.setHemogramaId(dto.getHemogramaId());
        notificacao.setPacienteId(dto.getPacienteId());
        notificacao.setRegiao(dto.getRegiao());
        notificacao.setEstado(dto.getEstado());
        notificacao.setFaixaEtaria(dto.getFaixaEtaria());
        notificacao.setSexo(dto.getSexo());
        notificacao.setMotivoRisco(dto.getMotivoRisco());
        notificacao.setLida(false);
        return notificacao;
    }
}