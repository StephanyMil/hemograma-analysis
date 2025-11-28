package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.Notificacao;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.NotificacaoConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Controller
public class NotificacaoWebSocketController {

    @Autowired
    private NotificacaoConsumerService notificacaoService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Quando frontend conecta, envia notificações não lidas
     */
    @SubscribeMapping("/notificacoes.iniciais")
    public Map<String, Object> enviarNotificacoesIniciais() {
        List<Notificacao> notificacoesNaoLidas = notificacaoService.buscarNotificacoesNaoLidas();

        Map<String, Object> response = new HashMap<>();
        response.put("tipo", "NOTIFICACOES_INICIAIS");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("total", notificacoesNaoLidas.size());
        response.put("data", notificacoesNaoLidas.stream()
                .map(notificacaoService::converterNotificacaoParaMap)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * Estatísticas em tempo real
     */
    @MessageMapping("/estatisticas.atual")
    @SendTo("/topic/estatisticas-tempo-real")
    public Map<String, Object> obterEstatisticasTempoReal() {
        long totalNaoLidas = notificacaoService.contarNotificacoesNaoLidas();
        long totalNotificacoes = notificacaoService.contarTotalNotificacoes();

        Map<String, Object> stats = new HashMap<>();
        stats.put("tipo", "ESTATISTICAS_TEMPO_REAL");
        stats.put("timestamp", LocalDateTime.now().toString());
        stats.put("totalNotificacoes", totalNotificacoes);
        stats.put("naoLidas", totalNaoLidas);
        stats.put("lidas", totalNotificacoes - totalNaoLidas);

        return stats;
    }

    /**
     * Última notificação
     */
    @MessageMapping("/ultima.notificacao")
    @SendTo("/topic/ultima.notificacao")
    public Map<String, Object> obterUltimaNotificacao() {
        try {
            Notificacao ultima = notificacaoService.buscarUltimaNotificacao();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("tipo", "ULTIMA_NOTIFICACAO");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("notificacao", notificacaoService.converterNotificacaoParaMap(ultima));

            return response;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("mensagem", "Nenhuma notificação encontrada");
            return error;
        }
    }
}