package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hemograma")
public class HemogramaGeneratorController {

    private final Random random = new Random();

    @GetMapping("/gerar")
    public ResponseEntity<Map<String, Object>> gerarHemograma() {
        Map<String, Object> hemograma = new HashMap<>();

        hemograma.put("leucocitos", gerarValor(4000, 11000)); // /µL
        hemograma.put("hemoglobina", gerarValor(12.0, 16.0)); // g/dL
        hemograma.put("plaquetas", gerarValor(150000, 400000)); // /µL
        hemograma.put("hematocrito", gerarValor(36.0, 50.0)); // %
        hemograma.put("hemacias", gerarValor(4.2, 5.9)); // milhões/µL

        hemograma.put("observacao", "Hemograma gerado automaticamente para testes.");

        return ResponseEntity.ok(hemograma);
    }

    private double gerarValor(double min, double max) {
        return Math.round((min + (max - min) * random.nextDouble()) * 100.0) / 100.0;
    }

    private int gerarValor(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
