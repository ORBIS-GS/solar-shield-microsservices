package com.solarshield.alert.service;

import com.solarshield.alert.model.Alert;
import com.solarshield.alert.model.AlertMessage;
import com.solarshield.alert.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Lógica de negócio do alert-service.
 *
 * processMessage() — aplica idempotência (RN3) e invalida o cache após novo alerta
 * findAll()        — Cache-Aside Redis com TTL=60s, logs explícitos de MISS e gravação
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository repository;
    private final CacheManager    cacheManager;

    public AlertService(AlertRepository repository, CacheManager cacheManager) {
        this.repository   = repository;
        this.cacheManager = cacheManager;
    }

    /**
     * Processa mensagem do RabbitMQ.
     * RN3: se eventId já existe, descarta e loga duplicata.
     * Após salvar um alerta novo, invalida o cache Redis para que a próxima
     * chamada a GET /alerts reflita os dados atualizados.
     */
    public boolean processMessage(AlertMessage message) {
        if (repository.exists(message.getEventId())) {
            log.warn("[RN3] Duplicata ignorada | eventId={}", message.getEventId());
            return false;
        }

        Alert alert = Alert.builder()
                .eventId(message.getEventId())
                .startTime(message.getStartTime())
                .maxKp(message.getMaxKp())
                .severity(message.getSeverity())
                .emergencyNotification(message.isEmergencyNotification())
                .receivedAt(Instant.now().toString())
                .build();

        boolean saved = repository.saveIfAbsent(alert);

        if (saved) {
            // Invalida o cache para que o próximo GET /alerts refaça a busca
            Cache cache = cacheManager.getCache("alerts");
            if (cache != null) {
                cache.evict("all");
                log.info("[CACHE EVICT] Cache 'alerts' invalidado após novo alerta | eventId={}",
                        message.getEventId());
            }
        }

        return saved;
    }

    /**
     * Retorna todos os alertas — padrão Cache-Aside com Redis.
     *
     * Fluxo:
     *   1. Spring verifica se existe a chave "all" no cache "alerts" no Redis
     *   2. MISS → executa o método, loga, grava no Redis com TTL=60s e retorna
     *   3. HIT  → retorna direto do Redis sem executar o método (sem log aqui)
     *
     * TTL justificado: eventos de clima espacial têm granularidade horária na NASA DONKI;
     * 60s equilibra frescor dos dados com redução de carga no repositório em memória.
     */
    @Cacheable(value = "alerts", key = "'all'")
    public List<Alert> findAll() {
        log.info("[CACHE MISS] Buscando alertas do repositório em memória...");
        List<Alert> alerts = repository.findAll();
        // Este log aparece APENAS quando o dado vai ser gravado no Redis (após o miss)
        log.info("[CACHE WRITE] Gravando {} alertas no Redis com TTL=60s", alerts.size());
        return alerts;
    }

    public int count() {
        return repository.count();
    }
}
