package com.solarshield.ingest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarshield.ingest.model.AlertMessage;
import com.solarshield.ingest.model.GeoStormEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Busca eventos GST na NASA DONKI e os classifica conforme RN1.
 *
 * RN1 — Severidade:
 *   Kp ≤ 4   → "low"
 *   5 ≤ Kp ≤ 7 → "moderate"
 *   Kp ≥ 8   → "severe"  (emergencyNotification = true)
 */
@Service
public class NasaIngestService {

    private static final Logger log = LoggerFactory.getLogger(NasaIngestService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${nasa.api.key}")
    private String apiKey;

    @Value("${nasa.api.base-url}")
    private String baseUrl;

    @Value("${nasa.api.gst-endpoint}")
    private String gstEndpoint;

    public NasaIngestService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Chama a NASA DONKI com retry + backoff exponencial (RN: resiliência).
     * Até 3 tentativas: espera 1s → 2s → máx 10s.
     */
    @Retryable(
        retryFor  = Exception.class,
        maxAttemptsExpression = "${nasa.api.retry.max-attempts}",
        backoff   = @Backoff(
            delayExpression      = "${nasa.api.retry.initial-interval}",
            multiplierExpression = "${nasa.api.retry.multiplier}",
            maxDelayExpression   = "${nasa.api.retry.max-interval}"
        )
    )
    public List<GeoStormEvent> fetchGeoStorms() {
        String url = baseUrl + gstEndpoint + "?api_key=" + apiKey;
        log.info("Buscando eventos GST na NASA: {}", url.replace(apiKey, "***"));

        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                log.warn("Resposta vazia da NASA DONKI.");
                return Collections.emptyList();
            }
            GeoStormEvent[] events = objectMapper.readValue(response, GeoStormEvent[].class);
            log.info("Recebidos {} eventos GST da NASA.", events.length);
            return Arrays.asList(events);
        } catch (Exception e) {
            log.error("Erro ao buscar dados da NASA: {}", e.getMessage());
            throw new RuntimeException("Falha na chamada à NASA DONKI", e);
        }
    }

    /**
     * Aplica a RN1: classifica a severidade de um evento com base no maior índice Kp.
     */
    public AlertMessage classify(GeoStormEvent event) {
        double kp       = event.getMaxKp();
        String severity = classifySeverity(kp);
        boolean emergency = "severe".equals(severity);

        log.debug("Evento {} | Kp={} | severity={} | emergency={}",
                event.getEventId(), kp, severity, emergency);

        return AlertMessage.builder()
                .eventId(event.getEventId())
                .startTime(event.getStartTime())
                .maxKp(kp)
                .severity(severity)
                .emergencyNotification(emergency)
                .build();
    }

    /**
     * Lógica pura de classificação — isolada para testes unitários (RN1).
     */
    public String classifySeverity(double kp) {
        if (kp >= 8) return "severe";
        if (kp >= 5) return "moderate";
        return "low";
    }
}
