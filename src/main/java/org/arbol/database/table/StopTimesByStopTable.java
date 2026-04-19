package org.arbol.database.table;

import org.arbol.logic.structures.table.Table;

public class StopTimesByStopTable extends Table<String, String> {
    public StopTimesByStopTable() {
        super("stop_times_by_stop", 16);
    }
}

