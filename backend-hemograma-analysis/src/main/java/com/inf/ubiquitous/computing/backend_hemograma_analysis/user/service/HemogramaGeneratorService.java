package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.springframework.stereotype.Service;

@Service
public class HemogramaGeneratorService {

    private static final Random RND = new Random();

    public List<Observation> gerar(int quantidade) {
        List<Observation> list = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            list.add(gerarUm());
        }
        return list;
    }

    public Observation gerarUm() {
        Observation obs = new Observation();
        obs.setStatus(Observation.ObservationStatus.FINAL);

        // code = CBC (58410-2)
        obs.setCode(new CodeableConcept().addCoding(new Coding()
                .setSystem("http://loinc.org")
                .setCode("58410-2")
                .setDisplay("CBC panel - Blood by Automated count")));

        // effective[x]
        obs.setEffective(new DateTimeType(OffsetDateTime.now().toString()));

        // Componentes
        obs.addComponent(criarComponente("6690-2", "Leukocytes", randomRange(4000, 11000), "/µL"));
        obs.addComponent(criarComponente("718-7",  "Hemoglobin", randomRange(11.0, 16.0), "g/dL"));
        obs.addComponent(criarComponente("777-3",  "Platelets",  randomRange(150000, 400000), "/µL"));
        obs.addComponent(criarComponente("4544-3", "Hematocrit", randomRange(35.0, 47.0), "%"));
        obs.addComponent(criarComponente("789-8",  "Erythrocytes", randomRange(3.8, 5.3), "milhões/mm³"));

        return obs;
    }

    private ObservationComponentComponent criarComponente(String loinc, String display, double valor, String unidade) {
        ObservationComponentComponent c = new ObservationComponentComponent();

        c.setCode(new CodeableConcept().addCoding(
                new Coding().setSystem("http://loinc.org").setCode(loinc).setDisplay(display)));

        Quantity q = new Quantity()
                .setValue(BigDecimal.valueOf(valor))
                .setUnit(unidade);

        // component.value[x]
        c.setValue(q);
        return c;
    }

    private double randomRange(double min, double max) {
        return Math.round((min + (max - min) * RND.nextDouble()) * 100.0) / 100.0;
    }
}
