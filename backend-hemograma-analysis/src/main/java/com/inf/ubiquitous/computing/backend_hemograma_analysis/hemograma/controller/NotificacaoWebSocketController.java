package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;


import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.Notificacao;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.repository.NotificacaoRepository;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.NotificacaoConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class NotificacaoWebSocketController {

    @Autowired

    private NotificacaoConsumerService notificacaoService;
    private NotificacaoRepository notificacaoRepository;

    /**
     * Quando cliente se conecta, envia notificações não lidas
     */
    @SubscribeMapping("/topic/notificacoes")
    public List<Notificacao> enviarNotificacoesIniciais() {
        return notificacaoRepository.findByLidaFalseOrderByDataCriacaoDesc();
    }

    /**
     * Marcar notificação como lida
     */
    @MessageMapping("/notificacao.marcar-lida")
    @SendTo("/topic/notificacoes/atualizadas")
    public String marcarNotificacaoLida(Long notificacaoId) {
        notificacaoService.marcarComoLida(notificacaoId);
        return "Notificação " + notificacaoId + " marcada como lida";
    }

}