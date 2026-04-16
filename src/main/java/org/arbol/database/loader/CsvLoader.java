package org.arbol.database.loader;


import org.arbol.database.Database;
import org.arbol.database.models.Route;
import org.arbol.database.models.Stop;
import org.arbol.database.models.StopTimes;
import org.arbol.database.models.Trips;
import org.arbol.database.models.key.StopTimesKey;
import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.arbol.utils.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CsvLoader {
    private static final Logger logger = LoggerFactory.getLogger(CsvLoader.class);
    private final Database db;

    public CsvLoader(Database db) {
        this.db = db;
    }

    public void loadStops(String filePath) {
        logger.info("Cargando Stops desde {}...", filePath);
        Table<String, Stop> table = db.getTable("stops");
        int inserted = 0;
        int duplicates = 0;
        int malformed = 0;
        int lineNumber = 1;

        try (BufferedReader br = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            br.readLine(); // Skip Header

            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    List<String> parts = parseCsvLine(line);
                    if (parts.size() < 5) {
                        malformed++;
                        continue;
                    }

                    String stopId = parts.get(0).trim();
                    String stopCode = parts.get(1).trim();
                    String stopName = parts.get(2).trim();
                    double lat = Double.parseDouble(parts.get(3).trim());
                    double lon = Double.parseDouble(parts.get(4).trim());

                    Stop stop = new Stop(stopId, stopCode, stopName, lat, lon);
                    Result<Void, NodeError.DuplicateKeyError> result = table.insert(stopId, stop);
                    if (result.isSuccess()) {
                        inserted++;
                    } else {
                        duplicates++;
                    }
                } catch (RuntimeException e) {
                    malformed++;
                    logger.debug("Línea {} inválida en stops: {}", lineNumber, line);
                }

                if (inserted > 0 && inserted % 1000 == 0) {
                    System.out.print("."); // Progreso visual
                }
            }
        } catch (IOException e) {
            logger.error("Error leyendo stops", e);
        }
        logger.info("Stops cargados: insertados={}, duplicados={}, inválidos={}", inserted, duplicates, malformed);
    }

    public void loadRoutes(String filePath) {
        logger.info("Cargando Routes desde {}...", filePath);
        Table<String, Route> table = db.getTable("routes");
        int inserted = 0;
        int duplicates = 0;
        int malformed = 0;
        int lineNumber = 1;

        try (BufferedReader br = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    List<String> p = parseCsvLine(line);
                    if (p.size() < 8) {
                        malformed++;
                        continue;
                    }

                    String routeId = p.get(0).trim();
                    String shortName = p.get(1).trim();
                    String longName = p.get(2).trim();
                    String desc = p.get(3).trim();
                    Integer agencyId = Integer.parseInt(p.get(4).trim());
                    String color = p.get(5).trim();
                    String textColor = p.get(6).trim();
                    Integer type = Integer.parseInt(p.get(7).trim());

                    Route route = new Route(routeId, shortName, longName, desc, agencyId, color, textColor, type);
                    Result<Void, NodeError.DuplicateKeyError> result = table.insert(routeId, route);
                    if (result.isSuccess()) {
                        inserted++;
                    } else {
                        duplicates++;
                    }
                } catch (RuntimeException e) {
                    malformed++;
                    logger.debug("Línea {} inválida en routes: {}", lineNumber, line);
                }
            }
        } catch (IOException e) {
            logger.error("Error leyendo routes", e);
        }
        logger.info("Routes cargados: insertados={}, duplicados={}, inválidos={}", inserted, duplicates, malformed);
    }

    public void loadTrips(String filePath) {
        logger.info("Cargando Trips desde {}...", filePath);
        Table<String, Trips> table = db.getTable("trips");
        int inserted = 0;
        int duplicates = 0;
        int malformed = 0;
        int insertErrors = 0;
        int lineNumber = 1;

        try (BufferedReader br = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    List<String> p = parseCsvLine(line);
                    if (p.size() < 4) {
                        malformed++;
                        continue;
                    }

                    String routeId = p.get(0).trim();
                    String serviceId = p.get(1).trim();
                    String tripId = p.get(2).trim();
                    String shapeId = p.get(3).trim();

                    Trips trip = new Trips(routeId, serviceId, tripId, shapeId);
                    try {
                        Result<Void, NodeError.DuplicateKeyError> result = table.insert(tripId, trip);
                        if (result.isSuccess()) {
                            inserted++;
                        } else {
                            duplicates++;
                        }
                    } catch (RuntimeException e) {
                        insertErrors++;
                        logger.error(
                                "Error insertando trip en línea {}. trip_id={}. Causa: {}",
                                lineNumber,
                                tripId,
                                e.getMessage(),
                                e
                        );
                    }
                } catch (RuntimeException e) {
                    malformed++;
                    logger.debug("Línea {} inválida en trips: {}. Causa: {}", lineNumber, line, e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Error leyendo trips", e);
        }
        logger.info(
                "Trips cargados: insertados={}, duplicados={}, inválidos={}, erroresInserción={}",
                inserted,
                duplicates,
                malformed,
                insertErrors
        );
    }

    public void loadStopTimes(String filePath) {
        logger.warn("INICIANDO CARGA MASIVA DE STOP_TIMES. Esto tomará tiempo...");
        Table<StopTimesKey, StopTimes> table = db.getTable("stop_times");
        int inserted = 0;
        int duplicates = 0;
        int malformed = 0;
        int lineNumber = 1;
        long startTime = System.currentTimeMillis();

        try (BufferedReader br = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    List<String> p = parseCsvLine(line);
                    if (p.size() < 5) {
                        malformed++;
                        continue;
                    }

                    String tripId = p.get(0).trim();
                    String arrivalStr = p.get(1).trim();
                    String departureStr = p.get(2).trim();
                    String stopId = p.get(3).trim();
                    String seqStr = p.get(4).trim();
                    String pointStr = p.size() > 5 ? p.get(5).trim() : "0";

                    StopTimesKey key = StopTimesKey.from(tripId, seqStr);

                    StopTimes st = new StopTimes(
                            tripId,
                            arrivalStr,
                            parseTime(departureStr),
                            stopId,
                            Integer.parseInt(seqStr),
                            Integer.parseInt(pointStr)
                    );

                    Result<Void, NodeError.DuplicateKeyError> result = table.insert(key, st);
                    if (result.isSuccess()) {
                        inserted++;
                    } else {
                        duplicates++;
                    }
                } catch (RuntimeException e) {
                    malformed++;
                    logger.debug("Línea {} inválida en stop_times", lineNumber);
                }

                if (inserted > 0 && inserted % 5000 == 0) {
                    System.out.print("X");
                }
            }
        } catch (IOException e) {
            logger.error("Error leyendo stop_times", e);
        }

        long endTime = System.currentTimeMillis();
        logger.info(
                "StopTimes cargados: insertados={}, duplicados={}, inválidos={}. Tiempo: {} segundos",
                inserted,
                duplicates,
                malformed,
                (endTime - startTime) / 1000
        );
    }

    // Utilidad para parsear horas GTFS (ej: 25:30:00) a Date
    private Date parseTime(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato de hora inválido: " + timeStr);
        }

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        if (hours < 0 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59) {
            throw new IllegalArgumentException("Hora GTFS fuera de rango: " + timeStr);
        }

        long totalSeconds = hours * 3600L + minutes * 60L + seconds;
        return new Date(totalSeconds * 1000L);
    }

    // Parser CSV simple para comillas y comas escapadas.
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());
        return fields;
    }
}