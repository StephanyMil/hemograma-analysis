package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service.SyntheticHemogramGeneratorService;

@RestController
@RequestMapping("/fhir/synthetic")
public class SyntheticHemogramController {

    @Autowired
    private SyntheticHemogramGeneratorService hemogramGeneratorService;

    @GetMapping("/generate")
    public String generate(@RequestParam(defaultValue = "5") int qtde) {
        return hemogramGeneratorService.gerarHemogramasSinteticos(qtde);
    }
}