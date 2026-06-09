package com.solarshield.ingest;

import com.solarshield.ingest.model.AlertMessage;
import com.solarshield.ingest.model.GeoStormEvent;
import com.solarshield.ingest.service.NasaIngestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários cobrindo RN1 (severidade + emergencyNotification).
 * Não sobe contexto Spring — testa a lógica pura de classificação.
 */
class SeverityClassificationTest {

    private NasaIngestService service;

    @BeforeEach
    void setUp() {
        // Instancia diretamente sem Spring para teste rápido de lógica de negócio
        service = new NasaIngestService(null, null);
    }

    // ── RN1: Kp ≤ 4 → "low" ──────────────────────────────────────────────────

    @Test
    @DisplayName("RN1 - Kp=3 deve retornar severity=low, emergency=false")
    void testLowSeverity() {
        GeoStormEvent event = buildEvent("GST-001", 3.0);
        AlertMessage alert = service.classify(event);

        assertEquals("low", alert.getSeverity());
        assertFalse(alert.isEmergencyNotification());
    }

    @Test
    @DisplayName("RN1 - Kp=4 (fronteira) deve retornar severity=low, emergency=false")
    void testLowSeverityBoundary() {
        GeoStormEvent event = buildEvent("GST-002", 4.0);
        AlertMessage alert = service.classify(event);

        assertEquals("low", alert.getSeverity());
        assertFalse(alert.isEmergencyNotification());
    }

    // ── RN1: 5 ≤ Kp ≤ 7 → "moderate" ────────────────────────────────────────

    @Test
    @DisplayName("RN1 - Kp=5 deve retornar severity=moderate, emergency=false")
    void testModerateSeverityLower() {
        GeoStormEvent event = buildEvent("GST-003", 5.0);
        AlertMessage alert = service.classify(event);

        assertEquals("moderate", alert.getSeverity());
        assertFalse(alert.isEmergencyNotification());
    }

    @Test
    @DisplayName("RN1 - Kp=7 (fronteira) deve retornar severity=moderate, emergency=false")
    void testModerateSeverityUpper() {
        GeoStormEvent event = buildEvent("GST-004", 7.0);
        AlertMessage alert = service.classify(event);

        assertEquals("moderate", alert.getSeverity());
        assertFalse(alert.isEmergencyNotification());
    }

    // ── RN1: Kp ≥ 8 → "severe" + emergencyNotification=true ─────────────────

    @Test
    @DisplayName("RN1 - Kp=8 deve retornar severity=severe, emergency=true")
    void testSevereSeverity() {
        GeoStormEvent event = buildEvent("GST-005", 8.0);
        AlertMessage alert = service.classify(event);

        assertEquals("severe", alert.getSeverity());
        assertTrue(alert.isEmergencyNotification());
    }

    @Test
    @DisplayName("RN1 - Kp=9 deve retornar severity=severe, emergency=true")
    void testSevereSeverityMax() {
        GeoStormEvent event = buildEvent("GST-006", 9.0);
        AlertMessage alert = service.classify(event);

        assertEquals("severe", alert.getSeverity());
        assertTrue(alert.isEmergencyNotification());
    }

    // ── Auxiliar ─────────────────────────────────────────────────────────────

    private GeoStormEvent buildEvent(String id, double kp) {
        GeoStormEvent event = new GeoStormEvent();
        event.setEventId(id);
        event.setStartTime("2024-05-10T00:00Z");

        GeoStormEvent.KpIndex kpIndex = new GeoStormEvent.KpIndex();
        kpIndex.setKpIndex(kp);
        kpIndex.setSource("NOAA");
        event.setAllKpIndex(List.of(kpIndex));

        return event;
    }
}
