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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class NotificacaoConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoConsumerService.class);

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 🔥 CORRIGIDO: Listener principal para HIV detectado
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

            // Converte DTO para Entity e salva
            Notificacao notificacao = converterDtoParaEntidade(dto);
            notificacao = notificacaoRepository.save(notificacao);

            logger.info("✅ Notificação HIV salva no banco - ID: {} | Tipo: {}",
                    notificacao.getId(), notificacao.getTipo());

            // 🔥 ENVIA EM TEMPO REAL PARA O FRONTEND
            enviarNotificacaoTempoReal(notificacao);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("❌ ERRO ao processar notificação HIV: {}", e.getMessage(), e);
            // Em caso de erro, não confirma o offset para reprocessar
        }
    }

    /**
     * 🔥 MÉTODO PRINCIPAL: Envia notificação em tempo real para todos os frontends conectados
     */
    public void enviarNotificacaoTempoReal(Notificacao notificacao) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("tipo", "NOTIFICACAO_TEMPO_REAL");
            payload.put("acao", "NOVA_NOTIFICACAO");
            payload.put("timestamp", LocalDateTime.now().toString());
            payload.put("data", converterNotificacaoParaMap(notificacao));
            payload.put("mensagem", "Nova notificação recebida em tempo real");

            // Envia para todos os clientes conectados
            messagingTemplate.convertAndSend("/topic/notificacoes-tempo-real", payload);

            // Envia também para tópicos específicos
            messagingTemplate.convertAndSend("/topic/notificacoes", payload);
            messagingTemplate.convertAndSend("/topic/notificacoes/hiv", payload);

            logger.info("🔔 Notificação enviada em tempo real para frontend - ID: {}", notificacao.getId());

        } catch (Exception e) {
            logger.error("❌ Erro ao enviar notificação em tempo real: {}", e.getMessage(), e);
        }
    }

    /**
     * Converter Notificacao para Map
     */
    public Map<String, Object> converterNotificacaoParaMap(Notificacao notificacao) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", notificacao.getId());
        map.put("tipo", notificacao.getTipo());
        map.put("hemogramaId", notificacao.getHemogramaId());
        map.put("pacienteId", notificacao.getPacienteId());
        map.put("regiao", notificacao.getRegiao());
        map.put("estado", notificacao.getEstado());
        map.put("faixaEtaria", notificacao.getFaixaEtaria());
        map.put("sexo", notificacao.getSexo());
        map.put("motivoRisco", notificacao.getMotivoRisco());
        map.put("lida", notificacao.isLida());
        map.put("dataCriacao", notificacao.getDataCriacao() != null ?
                notificacao.getDataCriacao().toString() : null);

        return map;
    }

    /**
     * Busca a última notificação
     */
    public Notificacao buscarUltimaNotificacao() {
        return notificacaoRepository.findTopByOrderByDataCriacaoDesc()
                .orElseThrow(() -> new RuntimeException("Nenhuma notificação encontrada"));
    }

    /**
     * Listener para estatísticas
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
            enviarNotificacaoTempoReal(notificacao);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("❌ ERRO ao processar notificação de estatísticas: {}", e.getMessage(), e);
        }
    }

    /**
     * Listener para lote processado
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
            enviarNotificacaoTempoReal(notificacao);

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
     * Contar notificações não lidas
     */
    public long contarNotificacoesNaoLidas() {
        return notificacaoRepository.countByLidaFalse();
    }

    /**
     * Contar total de notificações
     */
    public long contarTotalNotificacoes() {
        return notificacaoRepository.count();
    }

    /**
     * Marcar notificação como lida
     */
    @Transactional
    public void marcarComoLida(Long notificacaoId) {
        notificacaoRepository.findById(notificacaoId).ifPresent(notificacao -> {
            notificacao.setLida(true);
            notificacaoRepository.save(notificacao);
            logger.info("✅ Notificação {} marcada como lida", notificacaoId);
        });
    }

    /**
     * Converter DTO para Entity
     */
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
        notificacao.setDataCriacao(LocalDateTime.now()); // 🔥 IMPORTANTE: Adiciona timestamp
        return notificacao;
    }
}