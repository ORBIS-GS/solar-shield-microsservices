package com.solarshield.ingest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Representa um evento GST (GeoMagnetic Storm) retornado pela NASA DONKI.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoStormEvent implements Serializable {

    @JsonProperty("gstID")
    private String eventId;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("allKpIndex")
    private List<KpIndex> allKpIndex;

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getEventId()                       { return eventId; }
    public void   setEventId(String eventId)         { this.eventId = eventId; }

    public String getStartTime()                     { return startTime; }
    public void   setStartTime(String startTime)     { this.startTime = startTime; }

    public List<KpIndex> getAllKpIndex()              { return allKpIndex; }
    public void          setAllKpIndex(List<KpIndex> list) { this.allKpIndex = list; }

    /**
     * Retorna o maior índice Kp do evento. Se a lista estiver vazia, retorna 0.
     */
    public double getMaxKp() {
        if (allKpIndex == null || allKpIndex.isEmpty()) return 0.0;
        return allKpIndex.stream()
                .mapToDouble(KpIndex::getKpIndex)
                .max()
                .orElse(0.0);
    }

    // ── Inner class ───────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KpIndex implements Serializable {

        @JsonProperty("kpIndex")
        private double kpIndex;

        @JsonProperty("source")
        private String source;

        public double getKpIndex()               { return kpIndex; }
        public void   setKpIndex(double kpIndex) { this.kpIndex = kpIndex; }

        public String getSource()                { return source; }
        public void   setSource(String source)   { this.source = source; }
    }
}
