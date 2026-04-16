package org.arbol.database.models;

import java.io.Serializable;
import java.util.Date;

public record StopTimes(
        String arrivalTime,
        Date departureTime,
        String stopId,
        Integer timepoint

) implements Serializable {

    @Override
    public String toString() {
        return arrivalTime + " - " + departureTime + " - " + stopId + " - " + timepoint;
    }
}
