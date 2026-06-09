package com.solarshield.alert;

import com.solarshield.alert.model.Alert;
import com.solarshield.alert.model.AlertMessage;
import com.solarshield.alert.repository.AlertRepository;
import com.solarshield.alert.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários cobrindo RN3 (idempotência por event_id).
 * Não sobe contexto Spring — testa lógica pura de negócio.
 */
class IdempotencyTest {

    private AlertRepository repository;
    private AlertService    service;

    @BeforeEach
    void setUp() {
        repository = new AlertRepository();
        // NoOpCacheManager desativa o Redis para o teste unitário
        CacheManager noOpCache = new NoOpCacheManager();
        service = new AlertService(repository, noOpCache);
    }

    @Test
    @DisplayName("RN3 - Primeiro evento com eventId único deve ser salvo")
    void testFirstEventIsSaved() {
        AlertMessage msg = buildMessage("GST-2024-001", "moderate");

        boolean result = service.processMessage(msg);

        assertTrue(result, "Primeiro evento deve ser salvo");
        assertEquals(1, repository.count());
    }

    @Test
    @DisplayName("RN3 - Segundo evento com mesmo eventId deve ser descartado")
    void testDuplicateEventIsDiscarded() {
        AlertMessage msg = buildMessage("GST-2024-001", "moderate");

        boolean first  = service.processMessage(msg);
        boolean second = service.processMessage(msg); // duplicata

        assertTrue(first,  "Primeiro evento deve ser aceito");
        assertFalse(second, "Duplicata deve ser descartada");
        assertEquals(1, repository.count(), "Apenas 1 alerta deve existir");
    }

    @Test
    @DisplayName("RN3 - Eventos com eventIds distintos devem ser salvos independentemente")
    void testDifferentEventIdsAreStoredSeparately() {
        boolean r1 = service.processMessage(buildMessage("GST-2024-001", "low"));
        boolean r2 = service.processMessage(buildMessage("GST-2024-002", "severe"));

        assertTrue(r1);
        assertTrue(r2);
        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("RN3 - Três envios do mesmo eventId resultam em apenas 1 alerta salvo")
    void testTripleDuplicateResultsInOneAlert() {
        AlertMessage msg = buildMessage("GST-2024-003", "severe");

        service.processMessage(msg);
        service.processMessage(msg); // duplicata 1
        service.processMessage(msg); // duplicata 2

        assertEquals(1, repository.count(), "Apenas 1 alerta mesmo com 3 envios");
    }

    // ── Auxiliar ─────────────────────────────────────────────────────────────
    private AlertMessage buildMessage(String eventId, String severity) {
        return new AlertMessage(eventId, "2024-05-10T00:00Z", 6.0,
                severity, "severe".equals(severity));
    }
}
