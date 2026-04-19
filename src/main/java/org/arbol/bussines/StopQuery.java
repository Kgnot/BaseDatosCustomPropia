package org.arbol.bussines;

import org.arbol.database.Database;
import org.arbol.database.models.Route;
import org.arbol.database.models.Stop;
import org.arbol.database.models.StopTimes;
import org.arbol.database.models.Trips;
import org.arbol.database.models.key.StopTimesKey;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.structures.table.Table;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private Table<String, Trips> tripsTable() {
        return db.getTable("trips");
    }

    private Table<String, Route> routesTable() {
        return db.getTable("routes");
    }

    public List<Stop> findActiveStops() {
        logger.info("Ejecutando JOIN: Encontrando paradas activas...");

        Set<String> activeStopIds = new LinkedHashSet<>(stopTimesByStopTable().findAll());

        long start = System.currentTimeMillis();

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

    public List<TripStopEdge> findConsecutiveStopEdges() {
        logger.info("Ejecutando proyeccion de aristas consecutivas por trip...");

        List<TripStopEdge> edges = new ArrayList<>();
        Map<String, String> tripRouteCache = new HashMap<>();
        Map<String, Boolean> routeExistsCache = new HashMap<>();

        class PreviousEntryHolder {
            NodeElement<StopTimesKey, StopTimes> value;
        }
        PreviousEntryHolder previous = new PreviousEntryHolder();

        stopTimesTable().scanEntries((NodeElement<StopTimesKey, StopTimes> current) -> {
            NodeElement<StopTimesKey, StopTimes> prev = previous.value;

            if (prev != null) {
                StopTimesKey prevKey = prev.key();
                StopTimes prevValue = prev.value();
                StopTimesKey currentKey = current.key();
                StopTimes currentValue = current.value();

                boolean sameTrip = prevKey.tripId().equals(currentKey.tripId());
                boolean consecutive = currentKey.stopSequence() == prevKey.stopSequence() + 1;

                if (sameTrip && consecutive) {
                    long departureSec = prevValue.departureTime().getTime() / 1000L;
                    long arrivalSec = parseGtfsTimeToSeconds(currentValue.arrivalTime());
                    double travelMin = (arrivalSec - departureSec) / 60.0;

                    if (travelMin > 0) {
                        String tripId = currentKey.tripId();
                        String routeId = tripRouteCache.get(tripId);

                        if (routeId == null) {
                            Result<Trips, ?> tripResult = tripsTable().select(tripId);
                            if (tripResult.isSuccess()) {
                                routeId = tripResult.unwrap().routeId();
                                tripRouteCache.put(tripId, routeId);
                            }
                        }

                        if (routeId != null) {
                            Boolean routeExists = routeExistsCache.get(routeId);
                            if (routeExists == null) {
                                routeExists = routesTable().select(routeId).isSuccess();
                                routeExistsCache.put(routeId, routeExists);
                            }

                            if (routeExists) {
                                edges.add(new TripStopEdge(
                                        prevValue.stopId(),
                                        currentValue.stopId(),
                                        tripId,
                                        routeId,
                                        prevKey.stopSequence(),
                                        travelMin
                                ));
                            }
                        }
                    }
                }
            }

            previous.value = current;
        });

        edges.sort(
                Comparator.comparing(TripStopEdge::routeId)
                        .thenComparing(TripStopEdge::tripId)
                        .thenComparing(TripStopEdge::fromStopSequence)
        );

        logger.info("Proyeccion completada. Aristas calculadas: {}", edges.size());
        return edges;
    }

    public List<StopTransition> findGroupedConsecutiveStopEdges() {
        logger.info("Ejecutando GROUP BY de aristas consecutivas por par de paradas...");

        List<TripStopEdge> rawEdges = findConsecutiveStopEdges();
        Map<String, StopTransitionAccumulator> groups = new HashMap<>();

        for (TripStopEdge edge : rawEdges) {
            String groupKey = edge.fromStopId() + "|" + edge.toStopId();
            StopTransitionAccumulator acc = groups.computeIfAbsent(groupKey, k -> new StopTransitionAccumulator(edge.fromStopId(), edge.toStopId()));
            acc.add(edge.travelTimeMin());
        }

        List<StopTransition> result = new ArrayList<>();
        for (StopTransitionAccumulator acc : groups.values()) {
            result.add(acc.toRecord());
        }

        result.sort(
                Comparator.comparing(StopTransition::fromStopId)
                        .thenComparing(StopTransition::toStopId)
        );

        logger.info("GROUP BY completado. Pares unicos: {}", result.size());
        return result;
    }

    private static final class StopTransitionAccumulator {
        private final String fromStopId;
        private final String toStopId;
        private double sum;
        private int samples;

        private StopTransitionAccumulator(String fromStopId, String toStopId) {
            this.fromStopId = fromStopId;
            this.toStopId = toStopId;
        }

        private void add(double travelTimeMin) {
            this.sum += travelTimeMin;
            this.samples++;
        }

        private StopTransition toRecord() {
            return new StopTransition(fromStopId, toStopId, sum / samples, samples);
        }
    }

    private long parseGtfsTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Hora GTFS invalida: " + timeStr);
        }

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600L + minutes * 60L + seconds;
    }
}