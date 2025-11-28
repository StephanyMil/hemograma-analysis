package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.entity.Notificacao;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacao")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    /**
     * GET /api/notificacoes/ultimas
     * Retorna as 10 últimas notificações
     */
    @GetMapping("/casos-recentes")
    public ResponseEntity<Map<String, Object>> getUltimasNotificacoes() {
        List<Notificacao> notificacoes = notificacaoRepository.findTop10ByOrderByDataCriacaoDesc();
        long naoLidas = notificacaoRepository.countByLidaFalse();

        Map<String, Object> response = new HashMap<>();
        response.put("notificacoes", notificacoes);
        response.put("total", notificacoes.size());
        response.put("naoLidas", naoLidas);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notificacao/ultimas-24h
     * Retorna notificações das últimas 24 horas
     */
    @GetMapping("/ultimas-24h")
    public ResponseEntity<Map<String, Object>> getNotificacoesUltimas24h() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(24);
        LocalDateTime fim = LocalDateTime.now();

        List<Notificacao> notificacoes = notificacaoRepository.findByPeriodo(inicio, fim);
        long naoLidas = notificacoes.stream().filter(n -> !n.getLida()).count();

        // Agrupa por tipo para estatísticas
        Map<String, Long> porTipo = notificacoes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Notificacao::getTipo,
                        java.util.stream.Collectors.counting()
                ));

        // Agrupa por região para estatísticas
        Map<String, Long> porRegiao = notificacoes.stream()
                .filter(n -> n.getRegiao() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        Notificacao::getRegiao,
                        java.util.stream.Collectors.counting()
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("notificacoes", notificacoes);
        response.put("total", notificacoes.size());
        response.put("naoLidas", naoLidas);
        response.put("lidas", notificacoes.size() - naoLidas);
        response.put("periodo", "Últimas 24 horas");
        response.put("dataInicio", inicio);
        response.put("dataFim", fim);
        response.put("estatisticasPorTipo", porTipo);
        response.put("estatisticasPorRegiao", porRegiao);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notificacoes
     * Lista todas as notificações com paginação
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarNotificacoes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("dataCriacao").descending());
        Page<Notificacao> pageNotificacoes = notificacaoRepository.findAllByOrderByDataCriacaoDesc(pageRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("notificacoes", pageNotificacoes.getContent());
        response.put("currentPage", pageNotificacoes.getNumber());
        response.put("totalItems", pageNotificacoes.getTotalElements());
        response.put("totalPages", pageNotificacoes.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notificacoes/nao-lidas
     * Retorna apenas notificações não lidas
     */
    @GetMapping("/nao-lidas")
    public ResponseEntity<List<Notificacao>> getNotificacoesNaoLidas() {
        List<Notificacao> notificacoes = notificacaoRepository.findByLidaFalseOrderByDataCriacaoDesc();
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * GET /api/notificacoes/tipo/{tipo}
     * Filtra notificações por tipo
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Notificacao>> getNotificacoesPorTipo(@PathVariable String tipo) {
        List<Notificacao> notificacoes = notificacaoRepository.findByTipoOrderByDataCriacaoDesc(tipo);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * PUT /api/notificacoes/{id}/marcar-lida
     * Marca uma notificação como lida
     */
    @PutMapping("/{id}/marcar-lida")
    public ResponseEntity<Notificacao> marcarComoLida(@PathVariable Long id) {
        return notificacaoRepository.findById(id)
                .map(notificacao -> {
                    notificacao.setLida(true);
                    Notificacao atualizada = notificacaoRepository.save(notificacao);
                    return ResponseEntity.ok(atualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/notificacoes/marcar-todas-lidas
     * Marca todas as notificações como lidas
     */
    @PutMapping("/marcar-todas-lidas")
    public ResponseEntity<Map<String, Object>> marcarTodasComoLidas() {
        List<Notificacao> naoLidas = notificacaoRepository.findByLidaFalseOrderByDataCriacaoDesc();
        naoLidas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(naoLidas);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Todas as notificações foram marcadas como lidas");
        response.put("total", naoLidas.size());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notificacoes/estatisticas
     * Retorna estatísticas sobre as notificações
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> getEstatisticas() {
        long total = notificacaoRepository.count();
        long naoLidas = notificacaoRepository.countByLidaFalse();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("lidas", total - naoLidas);
        stats.put("naoLidas", naoLidas);

        return ResponseEntity.ok(stats);
    }

    /**
     * DELETE /api/notificacoes/{id}
     * Deleta uma notificação específica
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletarNotificacao(@PathVariable Long id) {
        return notificacaoRepository.findById(id)
                .map(notificacao -> {
                    notificacaoRepository.delete(notificacao);
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Notificação deletada com sucesso");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}