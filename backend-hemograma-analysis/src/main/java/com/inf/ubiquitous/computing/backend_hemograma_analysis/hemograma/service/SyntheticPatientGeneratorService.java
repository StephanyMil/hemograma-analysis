package com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.hemograma.dto.PacienteDto;

/**
 * Serviço que gera pacientes sintéticos com distribuições demográficas realistas do Brasil.
 */
@Service
public class SyntheticPatientGeneratorService {
    
    private static final Random random = new Random();
    
    // Distribuição de regiões brasileiras por população (IBGE)
    private static final Map<String, Double> DISTRIBUICAO_REGIOES = Map.of(
        "Sudeste", 0.418, "Nordeste", 0.272, "Sul", 0.146, "Norte", 0.086, "Centro-Oeste", 0.078
    );
    
    // Estados por região (simplificado)
    private static final Map<String, List<String>> ESTADOS_POR_REGIAO = Map.of(
        "Norte", Arrays.asList("AM", "PA", "AC", "RO", "RR", "AP", "TO"),
        "Nordeste", Arrays.asList("BA", "PE", "CE", "MA", "PB", "AL", "PI", "RN", "SE"),
        "Centro-Oeste", Arrays.asList("GO", "MT", "MS", "DF"),
        "Sudeste", Arrays.asList("SP", "RJ", "MG", "ES"),
        "Sul", Arrays.asList("PR", "RS", "SC")
    );
    
    // Nomes brasileiros
    private static final List<String> NOMES_MASCULINOS = Arrays.asList(
        "João", "José", "Carlos", "Paulo", "Pedro", "Lucas", "Gabriel", "Rafael", "Daniel", "Bruno"
    );
    
    private static final List<String> NOMES_FEMININOS = Arrays.asList(
        "Maria", "Ana", "Francisca", "Adriana", "Juliana", "Fernanda", "Aline", "Sandra", "Camila", "Amanda"
    );
    
    private static final List<String> SOBRENOMES = Arrays.asList(
        "Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima", "Costa"
    );
    
    /**
     * Gera um paciente sintético com dados realistas.
     */
    public PacienteDto gerarPacienteSintetico() {
        String sexo = random.nextBoolean() ? "M" : "F";
        String nome = gerarNomeCompleto(sexo);
        Integer idade = gerarIdadeRealistica();
        LocalDate dataNascimento = calcularDataNascimento(idade);
        String regiao = selecionarRegiao();
        String estado = selecionarEstado(regiao);
        String cidade = "Capital";
        String cpf = gerarCpfSintetico();
        String telefone = gerarTelefoneSintetico();
        String id = UUID.randomUUID().toString();
        
        return new PacienteDto(id, nome, idade, sexo, regiao, estado, cidade, cpf, dataNascimento, telefone);
    }
    
    /**
     * Gera múltiplos pacientes.
     */
    public List<PacienteDto> gerarPacientesSinteticos(int quantidade) {
        List<PacienteDto> pacientes = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            pacientes.add(gerarPacienteSintetico());
        }
        return pacientes;
    }
    
    /**
     * Gera paciente em formato FHIR JSON.
     */
    public String gerarPacienteFhir(PacienteDto paciente) {
        Map<String, Object> patient = new HashMap<>();
        patient.put("resourceType", "Patient");
        patient.put("id", paciente.getId());
        patient.put("gender", paciente.getSexo().equals("M") ? "male" : "female");
        patient.put("birthDate", paciente.getDataNascimento().toString());
        
        // Nome simplificado
        Map<String, Object> name = new HashMap<>();
        name.put("family", extrairSobrenome(paciente.getNome()));
        name.put("given", List.of(extrairPrimeiroNome(paciente.getNome())));
        patient.put("name", List.of(name));
        
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(patient);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar JSON FHIR", e);
        }
    }
    
    // Métodos auxiliares
    private String gerarNomeCompleto(String sexo) {
        List<String> nomes = sexo.equals("M") ? NOMES_MASCULINOS : NOMES_FEMININOS;
        return nomes.get(random.nextInt(nomes.size())) + " " + SOBRENOMES.get(random.nextInt(SOBRENOMES.size()));
    }
    
    private Integer gerarIdadeRealistica() {
        double r = random.nextDouble();
        if (r < 0.3) return random.nextInt(18);     // 0-17: 30%
        if (r < 0.7) return 18 + random.nextInt(42); // 18-59: 40%
        return 60 + random.nextInt(40);              // 60-99: 30%
    }
    
    private LocalDate calcularDataNascimento(Integer idade) {
        return LocalDate.now().minusYears(idade).minusDays(random.nextInt(365));
    }
    
    private String selecionarRegiao() {
        double r = random.nextDouble();
        double acumulado = 0.0;
        for (Map.Entry<String, Double> entry : DISTRIBUICAO_REGIOES.entrySet()) {
            acumulado += entry.getValue();
            if (r <= acumulado) return entry.getKey();
        }
        return "Sudeste";
    }
    
    private String selecionarEstado(String regiao) {
        List<String> estados = ESTADOS_POR_REGIAO.get(regiao);
        return estados.get(random.nextInt(estados.size()));
    }
    
    private String gerarCpfSintetico() {
        return String.format("%03d.%03d.%03d-%02d", 
            random.nextInt(1000), random.nextInt(1000), random.nextInt(1000), random.nextInt(100));
    }
    
    private String gerarTelefoneSintetico() {
        return String.format("(11) 9%04d-%04d", random.nextInt(10000), random.nextInt(10000));
    }
    
    private String extrairPrimeiroNome(String nomeCompleto) {
        return nomeCompleto.split(" ")[0];
    }
    
    private String extrairSobrenome(String nomeCompleto) {
        String[] partes = nomeCompleto.split(" ");
        return partes.length > 1 ? partes[partes.length - 1] : "";
    }
}