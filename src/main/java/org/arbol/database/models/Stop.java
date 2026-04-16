package org.arbol.database.models;

import java.io.Serializable;

public record Stop(
        String stopId,
        String stopCode,
        String stopName,
        Double stopLat,
        Double stopLon
) implements Serializable {
    @Override
    public String toString() {
        return stopName + " (" + stopLat + ", " + stopLon + ")";
    }
}
