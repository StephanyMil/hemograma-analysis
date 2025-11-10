package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaFhirParserService.HemogramaData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Serviço para armazenar temporariamente os últimos hemogramas recebidos
@Service
public class HemogramaStorageService {
    private final Map<String, HemogramaData> hemogramaBuffer = new ConcurrentHashMap<>();

    // Adiciona um novo hemograma ao buffer. Se já existir um hemograma com o mesmo ID, ele será substituído.
    public void addHemograma(HemogramaData hemograma) {
        if (hemograma != null && hemograma.getObservationId() != null) {
            hemogramaBuffer.put(hemograma.getObservationId(), hemograma);
        }
    }

    // Retorna um hemograma específico pelo seu ID de observação.
    public Optional<HemogramaData> findById(String observationId) {
        return Optional.ofNullable(hemogramaBuffer.get(observationId));
    }

    private List<HemogramaData> getTodosHemogramasOrdenados() {
        return hemogramaBuffer.values().stream()
                .sorted(Comparator.comparing(HemogramaData::getDataColeta, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Page<HemogramaData> getRecentHemogramas(Pageable pageable) {

        List<HemogramaData> todosHemogramasOrdenados = getTodosHemogramasOrdenados();

        int total = todosHemogramasOrdenados.size();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        List<HemogramaData> paginatedList;

        if (start >= total) {
            paginatedList = List.of(); // Retorna lista vazia se a página estiver fora do alcance
        } else {
            paginatedList = todosHemogramasOrdenados.subList(start, end);
        }

        return new PageImpl<>(paginatedList, pageable, total);
    }
}