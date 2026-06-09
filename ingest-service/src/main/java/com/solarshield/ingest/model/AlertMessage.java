package com.solarshield.ingest.model;

import java.io.Serializable;

/**
 * DTO publicado no RabbitMQ após classificação do evento.
 * Contrato entre ingest-service (producer) e alert-service (consumer).
 */
public class AlertMessage implements Serializable {

    private String  eventId;
    private String  startTime;
    private double  maxKp;
    private String  severity;
    private boolean emergencyNotification;

    // ── Construtor vazio (Jackson) ────────────────────────────────────────────
    public AlertMessage() {}

    // ── Builder estático ─────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AlertMessage obj = new AlertMessage();

        public Builder eventId(String v)               { obj.eventId = v;                return this; }
        public Builder startTime(String v)             { obj.startTime = v;              return this; }
        public Builder maxKp(double v)                 { obj.maxKp = v;                  return this; }
        public Builder severity(String v)              { obj.severity = v;               return this; }
        public Builder emergencyNotification(boolean v){ obj.emergencyNotification = v;  return this; }
        public AlertMessage build()                    { return obj; }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String  getEventId()                             { return eventId; }
    public void    setEventId(String eventId)               { this.eventId = eventId; }

    public String  getStartTime()                           { return startTime; }
    public void    setStartTime(String startTime)           { this.startTime = startTime; }

    public double  getMaxKp()                               { return maxKp; }
    public void    setMaxKp(double maxKp)                   { this.maxKp = maxKp; }

    public String  getSeverity()                            { return severity; }
    public void    setSeverity(String severity)             { this.severity = severity; }

    public boolean isEmergencyNotification()                { return emergencyNotification; }
    public void    setEmergencyNotification(boolean v)      { this.emergencyNotification = v; }
}
