package org.arbol.database.models;

import java.io.Serializable;

public record Trips(
        String routeId,
        String serviceId,
        String tripId,
        String shapeId
) implements Serializable {
    @Override
    public String toString() {
        return routeId + " - " + serviceId + " - " + tripId + " - " + shapeId;
    }
}
