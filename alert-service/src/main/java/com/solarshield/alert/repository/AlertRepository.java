package com.solarshield.alert.repository;

import com.solarshield.alert.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazenamento em memória dos alertas.
 *
 * ConcurrentHashMap keyed por eventId garante idempotência (RN3):
 * a segunda inserção do mesmo eventId é ignorada e logada.
 */
@Repository
public class AlertRepository {

    private static final Logger log = LoggerFactory.getLogger(AlertRepository.class);

    private final ConcurrentHashMap<String, Alert> store = new ConcurrentHashMap<>();

    /**
     * Salva somente se o eventId ainda não existir.
     * @return true se salvo, false se duplicata (RN3)
     */
    public boolean saveIfAbsent(Alert alert) {
        Alert existing = store.putIfAbsent(alert.getEventId(), alert);
        if (existing != null) {
            log.warn("[DUPLICATA] Evento já processado, descartando | eventId={}", alert.getEventId());
            return false;
        }
        log.info("Alerta salvo | eventId={} | severity={}", alert.getEventId(), alert.getSeverity());
        return true;
    }

    public boolean exists(String eventId) {
        return store.containsKey(eventId);
    }

    public List<Alert> findAll() {
        Collection<Alert> values = store.values();
        return new ArrayList<>(values);
    }

    public int count() {
        return store.size();
    }
}
