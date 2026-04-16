package org.arbol.database.models;

import java.io.Serializable;
import java.util.Date;

public record StopTimes(
        String tripId,
        String arrivalTime,
        Date departureTime,
        String stopId,
        Integer stopSequence,
        Integer timepoint

) implements Serializable {

    @Override
    public String toString() {
        return tripId + " - " + arrivalTime + " - " + departureTime + " - " + stopId + " - " + stopSequence + " - " + timepoint;
    }
}
