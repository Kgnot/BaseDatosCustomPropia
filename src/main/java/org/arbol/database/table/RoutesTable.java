package org.arbol.database.table;


import org.arbol.database.models.Route;
import org.arbol.logic.structures.table.Table;

public class RoutesTable extends Table<String, Route> {
    public RoutesTable() {
        super("routes", 16);
    }
}