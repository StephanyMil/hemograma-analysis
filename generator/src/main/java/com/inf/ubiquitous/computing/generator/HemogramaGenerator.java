package com.inf.ubiquitous.computing.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Container Java independente que gera hemogramas FHIR sintéticos
 * e os envia para o HAPI FHIR Server automaticamente.
 * 
 * Baseado no SyntheticHemogramGeneratorService do Spring Boot.
 */
public class HemogramaGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HemogramaGenerator.class);
    private static final Random random = new Random();
    
    // Configurações do ambiente
    private final String hapiUrl;
    private final int generationInterval;
    private final int patientsPerBatch;
    private final int hivRiskPercentage;
    
    // HTTP Client
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Contadores
    private int totalGenerated = 0;
    private int totalSent = 0;
    private int totalErrors = 0;
    private int totalWithHivRisk = 0;

    public HemogramaGenerator() {
        // Ler configurações do ambiente
        this.hapiUrl = System.getenv().getOrDefault("HAPI_URL", "http://hapi:8080/fhir");
        this.generationInterval = Integer.parseInt(System.getenv().getOrDefault("GENERATION_INTERVAL", "60"));
        this.patientsPerBatch = Integer.parseInt(System.getenv().getOrDefault("PATIENTS_PER_BATCH", "5"));
        this.hivRiskPercentage = Integer.parseInt(System.getenv().getOrDefault("HIV_RISK_PERCENTAGE", "20"));
        
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        
        logger.info("Gerador de hemogramas iniciado");
        logger.info("HAPI URL: {}", hapiUrl);
        logger.info("Intervalo: {}s, Lote: {}, Risco HIV: {}%", generationInterval, patientsPerBatch, hivRiskPercentage);
    }

    /**
     * Gera um hemograma FHIR Observation sintético
     * Baseado no método do SyntheticHemogramGeneratorService
     */
    private Map<String, Object> gerarHemograma() {
        Map<String, Object> observation = new HashMap<>();
        observation.put("resourceType", "Observation");
        observation.put("id", UUID.randomUUID().toString());
        observation.put("status", "final");

        // Código do hemograma
        Map<String, Object> code = new HashMap<>();
        code.put("coding", List.of(Map.of(
                "system", "http://loinc.org",
                "code", "58410-2",
                "display", "CBC panel - Blood by Automated count"
        )));
        observation.put("code", code);
        observation.put("effectiveDateTime", gerarDataAleatoria());

        // Determinar se terá risco HIV
        boolean withHivRisk = random.nextInt(100) < hivRiskPercentage;
        if (withHivRisk) {
            totalWithHivRisk++;
        }

        // Componentes do hemograma (24 campos)
        List<Map<String, Object>> components = new ArrayList<>();

        // Principais - valores ajustados para risco HIV se necessário
        double leucocitos = withHivRisk ? gerarValor(2000, 3900) : gerarValor(4000, 11000);
        double hemoglobina = withHivRisk ? gerarValor(8.0, 10.9) : gerarValor(12.0, 17.0);
        double linfocitos = withHivRisk ? gerarValor(10, 19) : gerarValor(20.0, 45.0);
        double linfocitosAbs = withHivRisk ? gerarValor(500, 999) : gerarValor(1000, 4800);

        components.add(criarComponente("6690-2", "Leukocytes", leucocitos, "/µL"));
        components.add(criarComponente("789-8", "Erythrocytes", gerarValor(4.2, 5.9), "milhões/mm³"));
        components.add(criarComponente("718-7", "Hemoglobin", hemoglobina, "g/dL"));
        components.add(criarComponente("4544-3", "Hematocrit", gerarValor(37.0, 50.0), "%"));
        components.add(criarComponente("777-3", "Platelets", gerarValor(150000, 450000), "/µL"));

        // Índices hematimétricos
        components.add(criarComponente("787-2", "Mean Corpuscular Volume (MCV)", gerarValor(80.0, 100.0), "fL"));
        components.add(criarComponente("785-6", "Mean Corpuscular Hemoglobin (MCH)", gerarValor(27.0, 33.0), "pg"));
        components.add(criarComponente("786-4", "Mean Corpuscular Hemoglobin Concentration (MCHC)", gerarValor(32.0, 36.0), "g/dL"));
        components.add(criarComponente("788-0", "Red Cell Distribution Width (RDW-CV)", gerarValor(11.5, 14.5), "%"));
        components.add(criarComponente("21000-5", "Red Cell Distribution Width (RDW-SD)", gerarValor(35.0, 50.0), "fL"));

        // Diferencial leucocitário (%)
        components.add(criarComponente("770-8", "Neutrophils", gerarValor(40.0, 75.0), "%"));
        components.add(criarComponente("736-9", "Lymphocytes", linfocitos, "%"));
        components.add(criarComponente("5905-5", "Monocytes", gerarValor(2.0, 10.0), "%"));
        components.add(criarComponente("713-8", "Eosinophils", gerarValor(1.0, 6.0), "%"));
        components.add(criarComponente("706-2", "Basophils", gerarValor(0.0, 2.0), "%"));

        // Contagem absoluta de leucócitos (células/µL)
        components.add(criarComponente("751-8", "Neutrophils (absolute)", gerarValor(1800, 7800), "/µL"));
        components.add(criarComponente("731-0", "Lymphocytes (absolute)", linfocitosAbs, "/µL"));
        components.add(criarComponente("742-7", "Monocytes (absolute)", gerarValor(200, 800), "/µL"));
        components.add(criarComponente("711-2", "Eosinophils (absolute)", gerarValor(50, 400), "/µL"));
        components.add(criarComponente("704-7", "Basophils (absolute)", gerarValor(0, 100), "/µL"));

        // Plaquetas – volume e índices
        components.add(criarComponente("32623-1", "Mean Platelet Volume (MPV)", gerarValor(7.5, 11.5), "fL"));
        components.add(criarComponente("49498-9", "Platelet Distribution Width (PDW)", gerarValor(9.0, 17.0), "fL"));

        observation.put("component", components);
        
        if (withHivRisk) {
            logger.warn("Hemograma com risco HIV gerado - ID: {}", observation.get("id"));
        }
        
        return observation;
    }

    private Map<String, Object> criarComponente(String code, String display, double value, String unit) {
        Map<String, Object> component = new HashMap<>();
        component.put("code", Map.of(
                "coding", List.of(Map.of(
                        "system", "http://loinc.org",
                        "code", code,
                        "display", display
                ))
        ));
        component.put("valueQuantity", Map.of(
                "value", Math.round(value * 100.0) / 100.0,
                "unit", unit
        ));
        return component;
    }

    private double gerarValor(double min, double max) {
        return Math.round((min + (max - min) * random.nextDouble()) * 100.0) / 100.0;
    }

    private String gerarDataAleatoria() {
        int ano = 2024 + random.nextInt(2);
        int mes = 1 + random.nextInt(12);
        int dia = 1 + random.nextInt(28);
        return String.format("%d-%02d-%02dT%02d:%02d:00Z", ano, mes, dia, random.nextInt(24), random.nextInt(60));
    }

    /**
     * Envia hemograma para HAPI FHIR
     */
    private boolean enviarParaHapi(Map<String, Object> observation) {
        try {
            String json = objectMapper.writeValueAsString(observation);
            
            HttpPost post = new HttpPost(hapiUrl + "/Observation");
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            post.setHeader("Content-Type", "application/fhir+json");
            post.setHeader("Accept", "application/fhir+json");
            
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                int statusCode = response.getCode();
                if (statusCode == 200 || statusCode == 201) {
                    logger.info("Hemograma enviado com sucesso - ID: {}", observation.get("id"));
                    totalSent++;
                    return true;
                } else {
                    logger.error("Erro ao enviar hemograma - Status: {}", statusCode);
                    totalErrors++;
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Exceção ao enviar hemograma: {}", e.getMessage());
            totalErrors++;
            return false;
        }
    }

    /**
     * Gera um lote de hemogramas
     */
    private void gerarLote() {
        logger.info("Iniciando geração de {} hemogramas", patientsPerBatch);
        
        for (int i = 0; i < patientsPerBatch; i++) {
            Map<String, Object> hemograma = gerarHemograma();
            totalGenerated++;
            enviarParaHapi(hemograma);
            
            // Pequeno delay entre envios
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        logger.info("Lote concluído - Total: gerados={}, enviados={}, erros={}, comRiscoHIV={}",
                   totalGenerated, totalSent, totalErrors, totalWithHivRisk);
    }

    /**
     * Execução principal
     */
    public void executar() {
        logger.info("Iniciando geração automática de hemogramas");
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        // Gerar primeiro lote imediatamente
        gerarLote();
        
        // Agendar execução periódica
        scheduler.scheduleAtFixedRate(this::gerarLote, generationInterval, generationInterval, TimeUnit.SECONDS);
        
        // Manter aplicação rodando
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Encerrando gerador de hemogramas");
            scheduler.shutdown();
        }));
    }

    public static void main(String[] args) {
        HemogramaGenerator generator = new HemogramaGenerator();
        generator.executar();
    }
}