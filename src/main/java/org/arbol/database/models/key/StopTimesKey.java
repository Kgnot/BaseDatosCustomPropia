package org.arbol.database.models.key;

import java.io.Serializable;

public record StopTimesKey(String tripId, Integer stopSequence)
        implements Comparable<StopTimesKey>, Serializable {

    @Override
    public int compareTo(StopTimesKey other) {
        int cmp = this.tripId.compareTo(other.tripId);
        if (cmp != 0) return cmp;
        return this.stopSequence.compareTo(other.stopSequence);
    }

    // Helper para crear la clave desde strings del CSV
    public static StopTimesKey from(String tripId, String sequence) {
        return new StopTimesKey(tripId, Integer.parseInt(sequence));
    }
}