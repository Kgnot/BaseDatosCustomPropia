package org.arbol.database.models;

import java.io.Serializable;

public record Route(
        String routeId,
        String shortName,
        String longName,
        String desc,
        Integer agencyId,
        String routeColor,
        String routeTextColor,
        Integer routeType
) implements Serializable {
    @Override
    public String toString() {
        return shortName + " - " + longName;
    }
}