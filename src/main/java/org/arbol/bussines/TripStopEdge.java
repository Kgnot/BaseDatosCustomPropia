package org.arbol.bussines;

import java.io.Serializable;

public record TripStopEdge(
        String fromStopId,
        String toStopId,
        String tripId,
        String routeId,
        Integer fromStopSequence,
        double travelTimeMin
) implements Serializable {
}

