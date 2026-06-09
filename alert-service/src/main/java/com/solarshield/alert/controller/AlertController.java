package com.solarshield.alert.controller;

import com.solarshield.alert.model.Alert;
import com.solarshield.alert.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint de consulta de alertas.
 * Rota via Nginx: GET /api/alerts → GET http://alert-service:8082/alerts
 */
@RestController
@RequestMapping("/alerts")
public class AlertController {

    private static final Logger log = LoggerFactory.getLogger(AlertController.class);

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * GET /alerts — Cache-Aside ativo via @Cacheable no AlertService.
     *
     * Como validar o cache nos logs do solar-alert:
     *   1ª chamada → verá "[CACHE MISS]" + "[CACHE WRITE] Gravando N alertas no Redis"
     *   2ª chamada (dentro de 60s) → nenhuma dessas linhas (dados vieram do Redis)
     */
    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        log.info("GET /alerts solicitado.");
        List<Alert> alerts = alertService.findAll();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count() {
        Map<String, Object> r = new HashMap<>();
        r.put("total", alertService.count());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> r = new HashMap<>();
        r.put("status", "UP");
        r.put("service", "alert-service");
        return ResponseEntity.ok(r);
    }
}
