package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service.HemogramaFhirParserService.HemogramaData;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Serviço para armazenar temporariamente os últimos hemogramas recebidos.

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

    // Retorna uma lista com os hemogramas mais recentes, ordenados por data de coleta.
    public List<HemogramaData> getRecentHemogramas() {
        return hemogramaBuffer.values().stream()
                .sorted(Comparator.comparing(HemogramaData::getDataColeta, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
}