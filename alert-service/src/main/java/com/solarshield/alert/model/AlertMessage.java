package com.solarshield.alert.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * DTO que chega via RabbitMQ — espelho do AlertMessage publicado pelo ingest-service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertMessage implements Serializable {

    private String  eventId;
    private String  startTime;
    private double  maxKp;
    private String  severity;
    private boolean emergencyNotification;

    public AlertMessage() {}

    public AlertMessage(String eventId, String startTime, double maxKp,
                        String severity, boolean emergencyNotification) {
        this.eventId               = eventId;
        this.startTime             = startTime;
        this.maxKp                 = maxKp;
        this.severity              = severity;
        this.emergencyNotification = emergencyNotification;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public String  getEventId()                          { return eventId; }
    public void    setEventId(String eventId)            { this.eventId = eventId; }

    public String  getStartTime()                        { return startTime; }
    public void    setStartTime(String startTime)        { this.startTime = startTime; }

    public double  getMaxKp()                            { return maxKp; }
    public void    setMaxKp(double maxKp)                { this.maxKp = maxKp; }

    public String  getSeverity()                         { return severity; }
    public void    setSeverity(String severity)          { this.severity = severity; }

    public boolean isEmergencyNotification()             { return emergencyNotification; }
    public void    setEmergencyNotification(boolean v)   { this.emergencyNotification = v; }
}
