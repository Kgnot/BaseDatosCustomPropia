package org.arbol.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.arbol.bussines.StopQuery;
import org.arbol.database.Database;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class TransitApiServer {

    private static final Logger logger = LoggerFactory.getLogger(TransitApiServer.class);
    private static final int DEFAULT_EXPORT_LIMIT = 10_000;
    private static final int MAX_EXPORT_LIMIT = 50_000;

    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);
        Database database = new Database();
        StopQuery stopQuery = new StopQuery(database);

        Server server = new Server(port);
        server.setHandler(new ApiHandler(stopQuery));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Cerrando servidor HTTP...");
                server.stop();
            } catch (Exception e) {
                logger.error("Error al detener Jetty", e);
            }
            database.close();
        }));

        server.start();
        logger.info("Servidor iniciado en http://localhost:{}", port);
        logger.info("Endpoint principal: GET /transitions/grouped");
        server.join();
    }

    private static int resolvePort(String[] args) {
        if (args != null && args.length > 0) {
            return Integer.parseInt(args[0]);
        }

        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort);
        }
        return 8080;
    }

    private static final class ApiHandler extends AbstractHandler {
        private final StopQuery stopQuery;
        private final ObjectMapper mapper = new ObjectMapper();

        private ApiHandler(StopQuery stopQuery) {
            this.stopQuery = stopQuery;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                return;
            }

            try {
                if ("/health".equals(target)) {
                    writeJson(response, HttpServletResponse.SC_OK, Map.of("status", "ok"));
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/transitions/grouped".equals(target)) {
                    long start = System.currentTimeMillis();
                    List<?> grouped = stopQuery.findGroupedConsecutiveStopEdges();
                    writeJson(response, HttpServletResponse.SC_OK, grouped);
                    logger.info("GET /transitions/grouped -> {} registros en {} ms", grouped.size(), System.currentTimeMillis() - start);
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/stops/active".equals(target)) {
                    List<?> activeStops = stopQuery.findActiveStops();
                    writeJson(response, HttpServletResponse.SC_OK, activeStops);
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/edges/consecutive".equals(target)) {
                    List<?> edges = stopQuery.findConsecutiveStopEdges();
                    int previewSize = Math.min(10, edges.size());
                    logger.info("GET /edges/consecutive -> {}", edges.subList(0, previewSize));
                    writeJson(response, HttpServletResponse.SC_OK, edges);
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/exports/status".equals(target)) {
                    writeJson(
                            response,
                            HttpServletResponse.SC_OK,
                            Map.of(
                                    "service", "trip-stop-edges-exporter",
                                    "status", "ok"
                            )
                    );
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/exports/trip-stop-edges".equals(target)) {
                    int limit = parseLimit(request.getParameter("limit"));
                    int offset = parseCursorToOffset(request.getParameter("cursor"));

                    long start = System.currentTimeMillis();
                    List<?> edges = stopQuery.findConsecutiveStopEdges();
                    int total = edges.size();

                    int fromIndex = Math.max(0, Math.min(offset, total));
                    int toIndex = Math.min(fromIndex + limit, total);
                    List<?> items = edges.subList(fromIndex, toIndex);

                    boolean hasMore = toIndex < total;
                    String nextCursor = hasMore ? encodeOffsetCursor(toIndex) : null;

                    writeJson(
                            response,
                            HttpServletResponse.SC_OK,
                            Map.of(
                                    "limit", limit,
                                    "count", items.size(),
                                    "total", total,
                                    "hasMore", hasMore,
                                    "nextCursor", nextCursor,
                                    "items", items
                            )
                    );

                    logger.info(
                            "GET /exports/trip-stop-edges?limit={}&offset={} -> {} items (total={}) en {} ms",
                            limit,
                            fromIndex,
                            items.size(),
                            total,
                            System.currentTimeMillis() - start
                    );

                    baseRequest.setHandled(true);
                    return;
                }

                writeJson(response, HttpServletResponse.SC_NOT_FOUND, Map.of("error", "Endpoint no encontrado"));
                baseRequest.setHandled(true);
            } catch (Exception e) {
                logger.error("Error manejando request {} {}", request.getMethod(), target, e);
                writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("error", "Error interno", "detail", e.getMessage()));
                baseRequest.setHandled(true);
            }
        }

        private int parseLimit(String rawLimit) {
            if (rawLimit == null || rawLimit.isBlank()) {
                return DEFAULT_EXPORT_LIMIT;
            }

            int parsed = Integer.parseInt(rawLimit);
            if (parsed <= 0) {
                throw new IllegalArgumentException("limit debe ser mayor a 0");
            }

            return Math.min(parsed, MAX_EXPORT_LIMIT);
        }

        private int parseCursorToOffset(String cursor) {
            if (cursor == null || cursor.isBlank()) {
                return 0;
            }

            try {
                String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                int offset = Integer.parseInt(decoded);
                if (offset < 0) {
                    throw new IllegalArgumentException("cursor inválido");
                }
                return offset;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("cursor inválido");
            }
        }

        private String encodeOffsetCursor(int offset) {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(String.valueOf(offset).getBytes(StandardCharsets.UTF_8));
        }

        private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            mapper.writeValue(response.getOutputStream(), body);
        }
    }
}


