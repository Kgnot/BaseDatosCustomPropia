package org.arbol.bussines;

import org.arbol.database.Database;
import org.arbol.database.models.Stop;
import org.arbol.database.models.StopTimes;
import org.arbol.database.models.key.StopTimesKey;
import org.arbol.logic.structures.table.Table;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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

    private Table<String, String> stopTimesByStopTable() {
        return db.getTable("stop_times_by_stop");
    }

    public List<Stop> findActiveStops() {
        logger.info("Ejecutando JOIN: Encontrando paradas activas...");

        Set<String> activeStopIds = new LinkedHashSet<>();

        long start = System.currentTimeMillis();
        activeStopIds.addAll(stopTimesByStopTable().findAll());

        // Bootstrap para bases persistidas antiguas sin índice secundario.
        if (activeStopIds.isEmpty()) {
            logger.warn("Indice stop_times_by_stop vacío, reconstruyendo desde stop_times...");
            stopTimesTable().scan(st -> {
                String stopId = st.stopId();
                if (stopId == null || stopId.isEmpty()) {
                    return;
                }
                if (activeStopIds.add(stopId)) {
                    stopTimesByStopTable().insert(stopId, stopId);
                }
            });
        }

        long timeScan = System.currentTimeMillis() - start;
        logger.info("Scan/Indice completado. Paradas activas encontradas: {}. Tiempo: {}s", activeStopIds.size(), timeScan / 1000);

        // Resolver por PK evita escanear toda la tabla stops.
        List<Stop> resultStops = new ArrayList<>();

        Set<String> addedStopIds = new HashSet<>();
        for (String stopId : activeStopIds) {
            Result<Stop, ?> stopResult = stopsTable().select(stopId);
            if (stopResult.isSuccess() && addedStopIds.add(stopId)) {
                resultStops.add(stopResult.unwrap());
            }
        }

        logger.info("Resultado final: {} paradas activas recuperadas.", resultStops.size());
        return resultStops;
    }
}