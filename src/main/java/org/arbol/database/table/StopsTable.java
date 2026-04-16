package org.arbol.database.table;

import org.arbol.database.models.Stop;
import org.arbol.logic.structures.table.Table;

public class StopsTable extends Table<String, Stop> {
    public StopsTable() {
        super("stops", 16);
    }
}