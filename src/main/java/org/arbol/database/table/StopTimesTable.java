package org.arbol.database.table;

import org.arbol.database.models.StopTimes;
import org.arbol.database.models.key.StopTimesKey;
import org.arbol.logic.structures.table.Table;

public class StopTimesTable extends Table<StopTimesKey, StopTimes> {

    public StopTimesTable() {
        super("stop_times", 12);
    }
}
