package com.solarshield.ingest.controller;

import com.solarshield.ingest.producer.AlertProducer;
import com.solarshield.ingest.model.AlertMessage;
import com.solarshield.ingest.model.GeoStormEvent;
import com.solarshield.ingest.service.NasaIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint de ingestão.
 * Rota via Nginx: POST /api/ingest/gst → POST http://ingest-service:8081/ingest/gst
 */
@RestController
@RequestMapping("/ingest")
public class IngestController {

    private static final Logger log = LoggerFactory.getLogger(IngestController.class);

    private final NasaIngestService nasaIngestService;
    private final AlertProducer alertProducer;

    public IngestController(NasaIngestService nasaIngestService, AlertProducer alertProducer) {
        this.nasaIngestService = nasaIngestService;
        this.alertProducer     = alertProducer;
    }

    /**
     * Dispara a busca de tempestades geomagnéticas na NASA,
     * classifica cada evento e publica no RabbitMQ.
     */
    @PostMapping("/gst")
    public ResponseEntity<Map<String, Object>> ingestGeoStorms() {
        log.info("Ingestão de eventos GST iniciada.");

        List<GeoStormEvent> events = nasaIngestService.fetchGeoStorms();

        Map<String, Object> response = new HashMap<>();

        if (events.isEmpty()) {
            response.put("message", "Nenhum evento GST encontrado no período.");
            response.put("published", 0);
            return ResponseEntity.ok(response);
        }

        int published = 0;
        for (GeoStormEvent event : events) {
            AlertMessage alert = nasaIngestService.classify(event);
            alertProducer.publish(alert);
            published++;
        }

        log.info("Ingestão concluída: {} eventos publicados no RabbitMQ.", published);

        response.put("message", "Ingestão concluída com sucesso.");
        response.put("published", published);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> r = new HashMap<>();
        r.put("status", "UP");
        r.put("service", "ingest-service");
        return ResponseEntity.ok(r);
    }
}
