package org.arbol.database.table;

import org.arbol.database.models.Trips;
import org.arbol.logic.structures.table.Table;

public class TripsTable extends Table<String, Trips> {
    public TripsTable() {
        super("trips", 16);
    }
}
