package com.solarshield.alert.model;

import java.io.Serializable;

/**
 * Alerta persistido em memória após processamento do consumer.
 * Serializable necessário para o Redis Cache.
 */
public class Alert implements Serializable {

    private String  eventId;
    private String  startTime;
    private double  maxKp;
    private String  severity;
    private boolean emergencyNotification;
    private String  receivedAt;

    public Alert() {}

    // ── Builder ───────────────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Alert obj = new Alert();

        public Builder eventId(String v)                { obj.eventId = v;                return this; }
        public Builder startTime(String v)              { obj.startTime = v;              return this; }
        public Builder maxKp(double v)                  { obj.maxKp = v;                  return this; }
        public Builder severity(String v)               { obj.severity = v;               return this; }
        public Builder emergencyNotification(boolean v) { obj.emergencyNotification = v;  return this; }
        public Builder receivedAt(String v)             { obj.receivedAt = v;             return this; }
        public Alert build()                            { return obj; }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String  getEventId()                              { return eventId; }
    public void    setEventId(String eventId)                { this.eventId = eventId; }

    public String  getStartTime()                            { return startTime; }
    public void    setStartTime(String startTime)            { this.startTime = startTime; }

    public double  getMaxKp()                                { return maxKp; }
    public void    setMaxKp(double maxKp)                    { this.maxKp = maxKp; }

    public String  getSeverity()                             { return severity; }
    public void    setSeverity(String severity)              { this.severity = severity; }

    public boolean isEmergencyNotification()                 { return emergencyNotification; }
    public void    setEmergencyNotification(boolean v)       { this.emergencyNotification = v; }

    public String  getReceivedAt()                           { return receivedAt; }
    public void    setReceivedAt(String receivedAt)          { this.receivedAt = receivedAt; }
}
