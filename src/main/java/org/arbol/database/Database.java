package org.arbol.database;

import org.arbol.database.table.RoutesTable;
import org.arbol.database.table.StopTimesByStopTable;
import org.arbol.database.table.StopTimesTable;
import org.arbol.database.table.StopsTable;
import org.arbol.database.table.TripsTable;
import org.arbol.logic.structures.table.Table;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private final Map<String, Table<?, ?>> tables;

    public Database() {
        this.tables = new HashMap<>();
        // Registramos las tablas
        registerTable(new StopsTable());
        registerTable(new RoutesTable());
        registerTable(new TripsTable());
        registerTable(new StopTimesTable());
        registerTable(new StopTimesByStopTable());
    }

    private void registerTable(Table<?, ?> table) {
        tables.put(table.getName(), table);
    }

    @SuppressWarnings("unchecked")
    public <K extends Comparable<K> & Serializable, V extends Serializable> Table<K, V> getTable(String name) {
        return (Table<K, V>) tables.get(name);
    }

    public void close() {
        for (Table<?, ?> table : tables.values()) {
            table.close();
        }
    }
}