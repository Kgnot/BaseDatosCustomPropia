package org.arbol.bussines;

import java.io.Serializable;

public record StopTransition(
        String fromStopId,
        String toStopId,
        double travelTimeMin,
        int samples
) implements Serializable {
}

