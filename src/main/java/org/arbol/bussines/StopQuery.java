package org.arbol.bussines;

import org.arbol.database.Database;
import org.arbol.database.models.Stop;
import org.arbol.database.models.StopTimes;
import org.arbol.database.models.key.StopTimesKey;
import org.arbol.logic.structures.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopQuery {

    private static final Logger logger = LoggerFactory.getLogger(StopQuery.class);
    private final Database db;

    public StopQuery(Database db) {
        this.db = db;
    }

    private Table<StopTimesKey, StopTimes> stopTimesTable() {
        return db.getTable("stop_times");
    }

    private Table<String, Stop> stopsTable() {
        return db.getTable("stops");
    }

    public List<Stop> findActiveStops() {
        logger.info("Ejecutando JOIN: Encontrando paradas activas...");

        // Usamos los helpers tipados
        Set<String> activeStopIds = new HashSet<>();

        long start = System.currentTimeMillis();
        stopTimesTable().scan(st -> {
            activeStopIds.add(st.stopId());

            if (activeStopIds.size() % 100000 == 0) {
                System.out.print("X");
            }
        });

        long timeScan = System.currentTimeMillis() - start;
        logger.info("Scan completado. Paradas activas encontradas: {}. Tiempo: {}s", activeStopIds.size(), timeScan / 1000);

        // Paso 2: Filtrar Stops
        List<Stop> resultStops = new ArrayList<>();

        stopsTable().scan(stop -> {
            if (activeStopIds.contains(stop.stopId())) {
                resultStops.add(stop);
            }
        });

        logger.info("Resultado final: {} paradas activas recuperadas.", resultStops.size());
        return resultStops;
    }
}